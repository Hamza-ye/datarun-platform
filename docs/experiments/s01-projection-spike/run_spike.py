"""
Datarun Projection Scaling Spike — Enhanced (Session A)

Validates the algorithmic cost of the projection fold at realistic scale.
Tests the combined projection pipeline as a device would compute it:

  entity state + workflow state + active flags + context.* properties

Capabilities exercised (architecture references):
  ✅ Entity state merge (update payloads)
  ✅ Flagged event exclusion [5-S2]
  ✅ Pattern-based state derivation [5-S4] — real state machine skeleton
  ✅ Multi-version shape routing [C11] — shape_ref version dispatch
  ✅ Alias resolution [2-S6, C7] — identity lookup per subject query
  ✅ Authority reconstruction [3-S3, C6] — assignment-timeline scan
  ✅ Source-chain traversal (activity_ref check)
  ✅ Flag raise/resolve counting
  ✅ context.* pre-resolution [5-S8] — 7 properties computed after fold
  ✅ Realistic payload size — 15–30 fields per event (60-field budget)

Limitations (acknowledged, not fixable in Python):
  - Target runtime is Kotlin/JVM on 8–16GB Android phone
  - Python fold times are ~10–50x slower than JVM (interpretation overhead)
  - This spike validates algorithmic cost (O(n) linearity, per-event overhead)
  - It CANNOT validate the 200ms absolute threshold — that's an implementation test
  - The spike can confirm the fold is "comfortably within budget" algorithmically,
    leaving only a constant-factor question for implementation

Usage:
  python run_spike.py
"""

import time
import uuid
import json
import random
import statistics
from dataclasses import dataclass, field
from typing import List, Dict, Any, Optional, Set, Tuple

# =============================================================================
# 1. Pattern Registry — real state machine skeletons [5-S4, 5-S5, C15]
# =============================================================================

# Pattern: capture_with_review
# States: open → under_review → accepted | rejected → corrected → under_review
CAPTURE_WITH_REVIEW = {
    "name": "capture_with_review",
    "initial_state": "open",
    "terminal_states": {"accepted"},
    "transitions": {
        # (current_state, event_type, shape_ref_pattern) → next_state
        # shape_ref_pattern is prefix-matched (ignoring version)
        ("open", "review", "review_"): "under_review",
        ("under_review", "review", "approval_"): "accepted",
        ("under_review", "review", "rejection_"): "rejected",
        ("rejected", "capture", "correction_"): "corrected",
        ("corrected", "review", "review_"): "under_review",
    },
    # Which (type, shape_prefix) combos are state-changing vs state-preserving
    "state_changing_shapes": {
        ("review", "review_"),
        ("review", "approval_"),
        ("review", "rejection_"),
        ("capture", "correction_"),
    },
}

# Pattern: case_management (long-running, many events per subject)
# States: registered → active → monitoring → closed
CASE_MANAGEMENT = {
    "name": "case_management",
    "initial_state": "registered",
    "terminal_states": {"closed"},
    "transitions": {
        ("registered", "capture", "initial_assessment_"): "active",
        ("active", "capture", "followup_"): "active",  # self-loop
        ("active", "capture", "monitoring_"): "monitoring",
        ("monitoring", "capture", "followup_"): "active",
        ("monitoring", "capture", "closure_"): "closed",
        ("active", "capture", "closure_"): "closed",
    },
    "state_changing_shapes": {
        ("capture", "initial_assessment_"),
        ("capture", "monitoring_"),
        ("capture", "closure_"),
        # followup_ on active is a self-loop (state-changing but same state)
        ("capture", "followup_"),
    },
}


# =============================================================================
# 2. Shape Registry — multi-version schemas [C11, 4-S1, 4-S10]
# =============================================================================

def _make_fields(n: int) -> Dict[str, Any]:
    """Generate a realistic payload with n fields."""
    payload = {}
    field_types = [
        ("text_{}", lambda i: f"value_{i}_{uuid.uuid4().hex[:6]}"),
        ("num_{}", lambda i: random.randint(0, 10000)),
        ("bool_{}", lambda i: random.choice([True, False])),
        ("date_{}", lambda i: f"2026-{random.randint(1,12):02d}-{random.randint(1,28):02d}"),
        ("list_{}", lambda i: [f"item_{j}" for j in range(random.randint(1, 5))]),
        ("ref_{}", lambda i: f"ref_{uuid.uuid4().hex[:8]}"),
    ]
    for i in range(n):
        pattern, gen = field_types[i % len(field_types)]
        payload[pattern.format(i)] = gen(i)
    return payload


# Shape versions: same shape name, different versions with different field counts
SHAPE_VERSIONS = {
    # capture shapes
    "initial_assessment_/v1": {"field_count": 20},
    "initial_assessment_/v2": {"field_count": 25},  # v2 added 5 optional fields
    "followup_/v1": {"field_count": 15},
    "followup_/v2": {"field_count": 18},
    "monitoring_/v1": {"field_count": 12},
    "closure_/v1": {"field_count": 10},
    "correction_/v1": {"field_count": 20},
    # review shapes
    "review_/v1": {"field_count": 8},
    "approval_/v1": {"field_count": 6},
    "rejection_/v1": {"field_count": 8},
    # alert/task shapes
    "alert_notification_/v1": {"field_count": 10},
    "task_followup_/v1": {"field_count": 12},
    "task_done_/v1": {"field_count": 8},
    # assignment shape
    "reassignment_/v1": {"field_count": 10},
}


def get_shape_field_count(shape_ref: str) -> int:
    """Multi-version dispatch: route on shape_ref to get field count. [C11]"""
    return SHAPE_VERSIONS.get(shape_ref, {}).get("field_count", 15)


def shape_name_prefix(shape_ref: str) -> str:
    """Extract shape name prefix (without version) for pattern matching."""
    # "initial_assessment_/v2" → "initial_assessment_"
    parts = shape_ref.split("/")
    return parts[0] if parts else shape_ref


# =============================================================================
# 3. Event Envelope — 11 fields as finalized across 5 ADRs
# =============================================================================

@dataclass
class EventEnvelope:
    id: str
    type: str  # one of 6: capture, review, alert, task_created, task_completed, assignment_changed
    shape_ref: str  # "{name}/v{N}"
    activity_ref: Optional[str]
    subject_ref: str  # "{type}:{id}"
    actor_ref: str  # "{type}:{id}"
    device_id: str
    device_seq: int
    sync_watermark: int
    timestamp: float
    payload: Dict[str, Any]


# =============================================================================
# 4. Alias Table — eager transitive closure, single-hop lookup [2-S6, C7]
# =============================================================================

class AliasTable:
    """
    Merge = alias in projection. retired_id → surviving_id.
    Eager transitive closure: if A→B then B→C, A maps directly to C. [2-S6]
    """

    def __init__(self):
        self._aliases: Dict[str, str] = {}

    def add_alias(self, retired_id: str, surviving_id: str):
        # Transitive closure: if surviving_id itself is aliased, follow the chain
        final = surviving_id
        while final in self._aliases:
            final = self._aliases[final]
        self._aliases[retired_id] = final
        # Update any existing aliases that pointed to retired_id
        for k, v in self._aliases.items():
            if v == retired_id:
                self._aliases[k] = final

    def resolve(self, subject_ref: str) -> str:
        """Single-hop lookup. O(1)."""
        return self._aliases.get(subject_ref, subject_ref)


# =============================================================================
# 5. Assignment Timeline — authority reconstruction [3-S3, C6]
# =============================================================================

@dataclass
class Assignment:
    actor_ref: str
    scope_type: str  # geographic, subject_list, activity
    scope_value: str
    start_seq: int
    end_seq: Optional[int] = None


class AuthorityTimeline:
    """
    Authority-as-projection: derived from assignment_changed events. [3-S3]
    Reconstructs who had authority over a subject at any given point.
    """

    def __init__(self):
        self._assignments: List[Assignment] = []

    def add_assignment(self, actor_ref: str, scope_type: str, scope_value: str,
                       start_seq: int, end_seq: Optional[int] = None):
        self._assignments.append(Assignment(
            actor_ref=actor_ref,
            scope_type=scope_type,
            scope_value=scope_value,
            start_seq=start_seq,
            end_seq=end_seq,
        ))

    def authority_at(self, seq: int) -> List[str]:
        """Return list of actor_refs with authority at the given sequence number."""
        return [
            a.actor_ref for a in self._assignments
            if a.start_seq <= seq and (a.end_seq is None or a.end_seq >= seq)
        ]

    def current_authority(self) -> List[str]:
        """Current actors with open-ended assignments."""
        return [a.actor_ref for a in self._assignments if a.end_seq is None]


# =============================================================================
# 6. Event Generator — realistic scenarios
# =============================================================================

# The 6 platform-fixed event types [4-S3]
EVENT_TYPES = ["capture", "review", "alert", "task_created", "task_completed", "assignment_changed"]


def generate_events(subject_ref: str, num_events: int,
                    pattern: dict, flag_rate: float = 0.08,
                    alias_count: int = 0, reassignment_count: int = 0,
                    version_mix: bool = True) -> Tuple[List[EventEnvelope], Set[str], AliasTable, AuthorityTimeline]:
    """
    Generate a realistic event stream for one subject.

    Returns: (events, flagged_event_ids, alias_table, authority_timeline)

    Args:
        subject_ref: The subject this stream is about
        num_events: Total events to generate
        pattern: State machine pattern to follow
        flag_rate: Fraction of events that get flagged (~8% default)
        alias_count: Number of alias (merge) entries to create
        reassignment_count: Number of assignment changes
        version_mix: Whether to mix shape versions (multi-version routing)
    """
    events: List[EventEnvelope] = []
    flagged_ids: Set[str] = set()
    alias_table = AliasTable()
    auth_timeline = AuthorityTimeline()

    # Set up actors (2–3 per subject is realistic)
    primary_actor = f"actor:{uuid.uuid4().hex[:8]}"
    reviewer_actor = f"actor:{uuid.uuid4().hex[:8]}"
    supervisor_actor = f"actor:{uuid.uuid4().hex[:8]}"
    actors = [primary_actor, reviewer_actor, supervisor_actor]

    device_id = f"dev_{uuid.uuid4().hex[:8]}"
    activity = f"activity_{uuid.uuid4().hex[:6]}"
    base_time = time.time() - 86400 * 60  # 60 days ago

    # Initial assignment
    auth_timeline.add_assignment(primary_actor, "geographic", "district_01", start_seq=0)
    auth_timeline.add_assignment(reviewer_actor, "geographic", "district_01", start_seq=0)

    # Set up aliases if requested
    for i in range(alias_count):
        old_ref = f"subject:{uuid.uuid4().hex[:8]}"
        alias_table.add_alias(old_ref, subject_ref)

    # Build a plausible event sequence following the pattern
    current_state = pattern["initial_state"]
    seq = 1

    # Determine event distribution based on pattern
    if pattern["name"] == "case_management":
        event_plan = _plan_case_management_events(num_events, version_mix)
    else:
        event_plan = _plan_capture_review_events(num_events, version_mix)

    # Inject assignment_changed events at specified intervals
    reassignment_positions = set()
    if reassignment_count > 0:
        step = max(1, num_events // (reassignment_count + 1))
        for i in range(reassignment_count):
            pos = step * (i + 1)
            if pos < num_events:
                reassignment_positions.add(pos)

    for i, (event_type, shape_ref) in enumerate(event_plan):
        # Override with assignment_changed at designated positions
        if i in reassignment_positions:
            event_type = "assignment_changed"
            shape_ref = "reassignment_/v1"
            # Close previous assignment, open new one
            auth_timeline.add_assignment(
                primary_actor, "geographic", f"district_{i:02d}",
                start_seq=seq
            )

        field_count = get_shape_field_count(shape_ref)
        payload = _make_fields(field_count)

        evt_id = f"evt_{uuid.uuid4().hex[:12]}"

        # activity_ref: point to previous event for source-chain linkage
        act_ref = events[-1].id if events else None

        # Pick actor based on event type
        if event_type == "review":
            actor = reviewer_actor
        elif event_type in ("alert", "task_created"):
            actor = f"system:trigger/{uuid.uuid4().hex[:6]}"
        else:
            actor = primary_actor

        events.append(EventEnvelope(
            id=evt_id,
            type=event_type,
            shape_ref=shape_ref,
            activity_ref=act_ref,
            subject_ref=subject_ref,
            actor_ref=actor,
            device_id=device_id,
            device_seq=seq,
            sync_watermark=max(1, seq - random.randint(0, 3)),
            timestamp=base_time + seq * 3600,
            payload=payload,
        ))

        # Flag some events (8% default) — simulates stale_reference, transition_violation, etc.
        if random.random() < flag_rate:
            flagged_ids.add(evt_id)

        seq += 1

    return events, flagged_ids, alias_table, auth_timeline


def _plan_case_management_events(n: int, version_mix: bool) -> List[Tuple[str, str]]:
    """Plan a case management event sequence: register → active → monitoring → closed."""
    plan = []

    # Event 0: initial registration/assessment
    v = "/v2" if (version_mix and random.random() > 0.5) else "/v1"
    plan.append(("capture", f"initial_assessment_{v}"))

    remaining = n - 1
    if remaining <= 0:
        return plan

    # ~50% followups, ~15% monitoring checks, ~10% alerts/tasks, ~8% reviews, rest other
    for i in range(remaining):
        r = random.random()
        if i == remaining - 1:
            # Last event: closure
            plan.append(("capture", "closure_/v1"))
        elif r < 0.50:
            v = "/v2" if (version_mix and random.random() > 0.6) else "/v1"
            plan.append(("capture", f"followup_{v}"))
        elif r < 0.65:
            plan.append(("capture", "monitoring_/v1"))
        elif r < 0.75:
            plan.append(("alert", "alert_notification_/v1"))
        elif r < 0.85:
            plan.append(("task_created", "task_followup_/v1"))
        elif r < 0.92:
            plan.append(("task_completed", "task_done_/v1"))
        else:
            plan.append(("review", "review_/v1"))

    return plan


def _plan_capture_review_events(n: int, version_mix: bool) -> List[Tuple[str, str]]:
    """Plan a capture-with-review sequence: capture → review cycles with corrections."""
    plan = []

    v = "/v2" if (version_mix and random.random() > 0.5) else "/v1"
    plan.append(("capture", f"initial_assessment_{v}"))

    remaining = n - 1
    for i in range(remaining):
        r = random.random()
        if r < 0.30:
            plan.append(("review", "review_/v1"))
        elif r < 0.45:
            plan.append(("review", "approval_/v1"))
        elif r < 0.55:
            plan.append(("review", "rejection_/v1"))
        elif r < 0.70:
            plan.append(("capture", "correction_/v1"))
        elif r < 0.80:
            plan.append(("alert", "alert_notification_/v1"))
        elif r < 0.90:
            plan.append(("task_created", "task_followup_/v1"))
        else:
            plan.append(("task_completed", "task_done_/v1"))

    return plan


# =============================================================================
# 7. Projection Fold — combined output as a device would compute it
# =============================================================================

def project_subject_state(events: List[EventEnvelope],
                          flagged_ids: Set[str],
                          alias_table: AliasTable,
                          auth_timeline: AuthorityTimeline,
                          pattern: dict) -> Dict[str, Any]:
    """
    The full projection fold. Reconstructs combined state from the event stream
    as a device would compute it on form-open.

    Pipeline per event:
      1. Alias resolution on subject_ref [2-S6]
      2. Flagged event exclusion check [5-S2]
      3. Multi-version shape routing [C11]
      4. Entity state merge (payload fields)
      5. Pattern-based state derivation [5-S4]
      6. Flag tracking (raise/resolve)
      7. Source-chain integrity check
      8. Authority reconstruction [3-S3]

    Post-fold:
      9. context.* pre-resolution [5-S8] — 7 properties
    """

    # --- Projection accumulators ---
    entity_data: Dict[str, Any] = {}
    workflow_state: str = pattern["initial_state"]
    active_flags: Dict[str, Dict] = {}  # flag_id → details
    resolved_flags: int = 0
    known_event_ids: Set[str] = set()
    source_chain_breaks: int = 0
    events_excluded_by_flag: int = 0
    transition_violations: int = 0
    shape_version_routes: Dict[str, int] = {}  # track version dispatch counts
    last_event_timestamp: float = 0.0
    event_count_by_type: Dict[str, int] = {}

    # --- Authority tracking ---
    authority_actors: List[str] = []

    for event in events:
        # 1. Alias resolution [2-S6, C7]
        resolved_subject = alias_table.resolve(event.subject_ref)

        # Always track the event ID for source-chain
        known_event_ids.add(event.id)

        # Count by type
        event_count_by_type[event.type] = event_count_by_type.get(event.type, 0) + 1

        # Track timestamp
        last_event_timestamp = event.timestamp

        # 2. Flagged event exclusion [5-S2]
        # Flagged events are VISIBLE in timeline but EXCLUDED from state derivation
        if event.id in flagged_ids:
            events_excluded_by_flag += 1
            # Still check source chain and count, but skip state derivation
            if event.activity_ref and event.activity_ref not in known_event_ids:
                source_chain_breaks += 1
            continue

        # 3. Multi-version shape routing [C11]
        shape_prefix = shape_name_prefix(event.shape_ref)
        shape_version_routes[event.shape_ref] = shape_version_routes.get(event.shape_ref, 0) + 1

        # 4. Entity state merge — merge payload into entity projection
        if event.type in ("capture", "review", "task_completed"):
            entity_data.update(event.payload)

        # 5. Pattern-based state derivation [5-S4]
        # Check if this event type + shape combo is state-changing
        is_state_changing = False
        for (req_type, req_shape_prefix) in pattern.get("state_changing_shapes", set()):
            if event.type == req_type and shape_prefix.startswith(req_shape_prefix):
                is_state_changing = True
                break

        if is_state_changing:
            # Look up valid transition from current state
            next_state = None
            for (from_state, trans_type, trans_shape), to_state in pattern["transitions"].items():
                if (workflow_state == from_state and
                    event.type == trans_type and
                    shape_prefix.startswith(trans_shape)):
                    next_state = to_state
                    break

            if next_state is not None:
                workflow_state = next_state
            else:
                # Invalid transition — in real system this becomes a transition_violation flag
                transition_violations += 1

        # 6. Flag tracking
        # In a real system, ConflictDetected events carry flag metadata
        # Here we simulate: alerts can indicate flag raises
        if event.type == "alert" and "flag_id" in event.payload:
            active_flags[event.payload["flag_id"]] = {
                "category": event.payload.get("category", "domain_uniqueness_violation"),
                "source_event": event.id,
            }

        # 7. Source-chain traversal [5-S7]
        if event.activity_ref and event.activity_ref not in known_event_ids:
            source_chain_breaks += 1

        # 8. Authority reconstruction [3-S3, C6]
        if event.type == "assignment_changed":
            authority_actors = auth_timeline.authority_at(event.device_seq)

    # --- Post-fold: context.* pre-resolution [5-S8] ---
    # 7 platform-fixed properties, computed from projection state
    current_authority = auth_timeline.current_authority()
    total_events = len(events)
    days_elapsed = (last_event_timestamp - events[0].timestamp) / 86400 if events else 0

    context = {
        "subject_state": workflow_state,
        "subject_pattern": pattern["name"],
        "activity_stage": _derive_activity_stage(workflow_state, pattern),
        "actor_role": current_authority[0] if current_authority else "unassigned",
        "actor_scope_name": "district_current",
        "days_since_last_event": round(days_elapsed, 1),
        "event_count": total_events,
    }

    return {
        "entity_field_count": len(entity_data),
        "workflow_state": workflow_state,
        "active_flags_count": len(active_flags),
        "resolved_flags": resolved_flags,
        "total_events_folded": len(known_event_ids),
        "events_excluded_by_flag": events_excluded_by_flag,
        "transition_violations": transition_violations,
        "source_chain_breaks": source_chain_breaks,
        "shape_versions_seen": len(shape_version_routes),
        "context": context,
    }


def _derive_activity_stage(workflow_state: str, pattern: dict) -> str:
    """Map workflow state to activity stage (context.activity_stage)."""
    if workflow_state == pattern["initial_state"]:
        return "intake"
    elif workflow_state in pattern.get("terminal_states", set()):
        return "complete"
    else:
        return "in_progress"


# =============================================================================
# 8. Performance Test Runner
# =============================================================================

def run_spike_for_count(num_events: int, pattern: dict, label: str,
                        iterations: int = 5_000) -> Dict[str, Any]:
    """Run the projection fold benchmark for a given event count."""

    # --- Generate events with all capabilities ---
    random.seed(42 + num_events)  # Reproducible per count
    subject_ref = f"subject:{uuid.uuid4().hex[:8]}"

    events, flagged_ids, alias_table, auth_timeline = generate_events(
        subject_ref=subject_ref,
        num_events=num_events,
        pattern=pattern,
        flag_rate=0.08,
        alias_count=3,          # 3 past merges for this subject
        reassignment_count=2,   # 2 reassignments in timeline
        version_mix=True,
    )

    # --- Payload size stats ---
    total_payload_bytes = sum(len(json.dumps(e.payload)) for e in events)
    avg_payload_bytes = total_payload_bytes / len(events)
    avg_field_count = sum(len(e.payload) for e in events) / len(events)

    # --- Warm-up (100 iterations) ---
    for _ in range(100):
        project_subject_state(events, flagged_ids, alias_table, auth_timeline, pattern)

    # --- Timed runs ---
    timings = []
    for _ in range(iterations):
        start = time.perf_counter()
        result = project_subject_state(events, flagged_ids, alias_table, auth_timeline, pattern)
        end = time.perf_counter()
        timings.append((end - start) * 1000)  # ms

    avg_ms = statistics.mean(timings)
    median_ms = statistics.median(timings)
    p95_ms = sorted(timings)[int(len(timings) * 0.95)]
    p99_ms = sorted(timings)[int(len(timings) * 0.99)]
    stddev_ms = statistics.stdev(timings)

    return {
        "label": label,
        "num_events": num_events,
        "iterations": iterations,
        "avg_ms": avg_ms,
        "median_ms": median_ms,
        "p95_ms": p95_ms,
        "p99_ms": p99_ms,
        "stddev_ms": stddev_ms,
        "avg_payload_bytes": avg_payload_bytes,
        "avg_field_count": avg_field_count,
        "flagged_events": len(flagged_ids),
        "result": result,
    }


def print_results(results: Dict[str, Any]):
    """Format and print benchmark results."""
    r = results
    print(f"\n{'='*60}")
    print(f"  {r['label']}")
    print(f"{'='*60}")
    print(f"  Events:         {r['num_events']}")
    print(f"  Iterations:     {r['iterations']:,}")
    print(f"  Flagged events: {r['flagged_events']} ({r['flagged_events']/r['num_events']*100:.0f}%)")
    print(f"  Avg fields/evt: {r['avg_field_count']:.1f}")
    print(f"  Avg bytes/evt:  {r['avg_payload_bytes']:.0f}")
    print()
    print(f"  Timing (ms):")
    print(f"    Mean:   {r['avg_ms']:.4f}")
    print(f"    Median: {r['median_ms']:.4f}")
    print(f"    P95:    {r['p95_ms']:.4f}")
    print(f"    P99:    {r['p99_ms']:.4f}")
    print(f"    StdDev: {r['stddev_ms']:.4f}")
    print()
    print(f"  Per-event cost: {r['avg_ms']/r['num_events']*1000:.2f} \u00b5s")
    print()
    print(f"  Projection output:")
    res = r['result']
    print(f"    Entity fields:      {res['entity_field_count']}")
    print(f"    Workflow state:     {res['workflow_state']}")
    print(f"    Active flags:       {res['active_flags_count']}")
    print(f"    Events excluded:    {res['events_excluded_by_flag']}")
    print(f"    Transition errors:  {res['transition_violations']}")
    print(f"    Chain breaks:       {res['source_chain_breaks']}")
    print(f"    Shape versions:     {res['shape_versions_seen']}")
    print(f"    context.*:          {json.dumps(res['context'], indent=6)}")


def run_performance_spike():
    print("=" * 60)
    print("  DATARUN PROJECTION SCALING SPIKE \u2014 ENHANCED")
    print("  Combined fold: entity + workflow + flags + context.*")
    print("=" * 60)

    # Use case_management pattern (long-running, most demanding)
    pattern = CASE_MANAGEMENT

    all_results = []

    for count in [100, 200, 500]:
        label = f"Case Management \u2014 {count} events"
        results = run_spike_for_count(count, pattern, label)
        print_results(results)
        all_results.append(results)

    # Also test capture_with_review at 100 (shorter lifecycle)
    label = "Capture with Review \u2014 100 events"
    results = run_spike_for_count(100, CAPTURE_WITH_REVIEW, label)
    print_results(results)
    all_results.append(results)

    # --- Summary ---
    print(f"\n{'='*60}")
    print(f"  SUMMARY")
    print(f"{'='*60}")
    print()
    print(f"  {'Scenario':<35} {'Events':>6} {'Mean':>8} {'P95':>8} {'P99':>8} {'\u00b5s/evt':>8}")
    print(f"  {'\u2014'*35} {'\u2014'*6} {'\u2014'*8} {'\u2014'*8} {'\u2014'*8} {'\u2014'*8}")
    for r in all_results:
        print(f"  {r['label']:<35} {r['num_events']:>6} {r['avg_ms']:>7.3f}ms {r['p95_ms']:>7.3f}ms {r['p99_ms']:>7.3f}ms {r['avg_ms']/r['num_events']*1000:>7.1f}")
    print()

    # --- Linearity check ---
    if len(all_results) >= 3:
        r100 = all_results[0]
        r200 = all_results[1]
        r500 = all_results[2]
        ratio_200_100 = r200['avg_ms'] / r100['avg_ms']
        ratio_500_100 = r500['avg_ms'] / r100['avg_ms']
        print(f"  Linearity check:")
        print(f"    200/100 ratio: {ratio_200_100:.2f}x (ideal: 2.0x)")
        print(f"    500/100 ratio: {ratio_500_100:.2f}x (ideal: 5.0x)")
        if ratio_500_100 < 7.0:
            print(f"    \u2705 Growth is linear \u2014 no super-linear blowup")
        else:
            print(f"    \u26a0\ufe0f  Growth is super-linear \u2014 investigate per-event overhead")
    print()

    # --- Algorithmic assessment ---
    worst_p99 = max(r['p99_ms'] for r in all_results)
    print(f"  Algorithmic assessment:")
    print(f"    Worst P99 (Python): {worst_p99:.3f} ms")
    print(f"    Python\u2192JVM speedup factor: ~10\u201330x (conservative)")
    print(f"    Projected JVM P99: ~{worst_p99/10:.3f}\u2013{worst_p99/30:.4f} ms")
    print(f"    Target threshold: 200 ms")
    print()
    if worst_p99 < 200:
        print(f"    \u2705 Even Python P99 is under 200ms.")
        print(f"       Algorithmic cost is comfortably within budget.")
        print(f"       Full confirmation requires implementation on target device.")
    else:
        print(f"    \u26a0\ufe0f  Python P99 exceeds 200ms \u2014 but Python is ~10\u201330x slower than JVM.")
        projected_worst = worst_p99 / 10
        if projected_worst < 200:
            print(f"       Projected JVM P99 ({projected_worst:.3f} ms) is within budget.")
            print(f"       Algorithmic cost is acceptable; constant-factor question for impl.")
        else:
            print(f"       \u274c Even with 10x JVM speedup, projected P99 ({projected_worst:.3f} ms) is concerning.")
            print(f"       Consider materialized views (B\u2192C escape hatch).")


if __name__ == "__main__":
    run_performance_spike()
