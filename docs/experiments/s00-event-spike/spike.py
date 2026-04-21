"""
S00 Event Spike — Feel the event model.

Not a framework. Not reusable. A walkthrough that makes ADR-001's
decisions concrete: append-only events, projections, corrections,
conflicts, replay.

Run: python3 spike.py
"""

import sqlite3
import uuid
import json
from datetime import datetime, timezone, timedelta

DB = ":memory:"  # throwaway — this is a spike


# ──────────────────────────────────────────────
# Setup: the event store
# ──────────────────────────────────────────────

def create_event_store(conn):
    """
    One table. This is the event log — the single source of truth.
    
    Columns map to ADR-001's minimum envelope:
      - id:         client-generated UUID
      - type:       what kind of action (record_captured, field_corrected, ...)
      - subject_id: which subject this event is about
      - ref:        optional reference to a prior event (for corrections, reviews)
      - payload:    action-specific data (JSON)
      - author:     who performed the action
      - timestamp:  when the action was performed (device time)
    """
    conn.execute("""
        CREATE TABLE events (
            id          TEXT PRIMARY KEY,
            type        TEXT NOT NULL,
            subject_id  TEXT NOT NULL,
            ref         TEXT,
            payload     TEXT NOT NULL,
            author      TEXT NOT NULL,
            timestamp   TEXT NOT NULL
        )
    """)
    # The projection table — derived, rebuildable, not a source of truth.
    conn.execute("""
        CREATE TABLE projections (
            subject_id  TEXT PRIMARY KEY,
            state       TEXT NOT NULL,
            version     INTEGER NOT NULL,  -- how many events produced this state
            built_from  TEXT NOT NULL       -- JSON list of event IDs
        )
    """)
    conn.commit()


def write_event(conn, event_type, subject_id, payload, author, ref=None, timestamp=None):
    """Write one immutable event. Returns the event dict."""
    event = {
        "id": str(uuid.uuid4()),
        "type": event_type,
        "subject_id": subject_id,
        "ref": ref,
        "payload": payload,
        "author": author,
        "timestamp": (timestamp or datetime.now(timezone.utc)).isoformat(),
    }
    conn.execute(
        "INSERT INTO events (id, type, subject_id, ref, payload, author, timestamp) "
        "VALUES (:id, :type, :subject_id, :ref, :payload, :author, :timestamp)",
        {**event, "payload": json.dumps(event["payload"])},
    )
    conn.commit()
    return event


def get_events_for_subject(conn, subject_id):
    """Get all events for a subject, ordered by timestamp."""
    rows = conn.execute(
        "SELECT id, type, subject_id, ref, payload, author, timestamp "
        "FROM events WHERE subject_id = ? ORDER BY timestamp",
        (subject_id,),
    ).fetchall()
    return [
        {
            "id": r[0], "type": r[1], "subject_id": r[2], "ref": r[3],
            "payload": json.loads(r[4]), "author": r[5], "timestamp": r[6],
        }
        for r in rows
    ]


# ──────────────────────────────────────────────
# Projection: compute current state from events
# ──────────────────────────────────────────────

def project(events):
    """
    Apply events in order to produce current state.
    
    This is the core question: what does it feel like to derive
    state from events instead of reading a row?
    """
    state = {}
    event_ids = []

    for evt in events:
        event_ids.append(evt["id"])

        if evt["type"] == "record_captured":
            # Initial capture — payload IS the state
            state = dict(evt["payload"])

        elif evt["type"] == "field_corrected":
            # Correction — apply the delta
            field = evt["payload"]["field"]
            state[field] = evt["payload"]["now"]

        elif evt["type"] == "review_completed":
            state["_review_status"] = evt["payload"]["decision"]
            state["_reviewed_by"] = evt["author"]

    return state, event_ids


def rebuild_projection(conn, subject_id):
    """Rebuild one subject's projection from its events."""
    events = get_events_for_subject(conn, subject_id)
    if not events:
        return None
    state, event_ids = project(events)
    conn.execute(
        "INSERT OR REPLACE INTO projections (subject_id, state, version, built_from) "
        "VALUES (?, ?, ?, ?)",
        (subject_id, json.dumps(state), len(event_ids), json.dumps(event_ids)),
    )
    conn.commit()
    return state


def read_projection(conn, subject_id):
    """Read the materialized projection (the fast path)."""
    row = conn.execute(
        "SELECT state, version FROM projections WHERE subject_id = ?",
        (subject_id,),
    ).fetchone()
    if row:
        return json.loads(row[0]), row[1]
    return None, 0


def detect_conflicts(events):
    """
    Detect events that correct the same field on the same original event
    from different authors.
    
    This is a NAIVE conflict detector — the point is to feel what
    conflict detection requires, not to build the final version.
    """
    # Group corrections by (ref, field)
    corrections_by_target = {}
    for evt in events:
        if evt["type"] == "field_corrected" and evt["ref"]:
            key = (evt["ref"], evt["payload"]["field"])
            corrections_by_target.setdefault(key, []).append(evt)

    conflicts = []
    for key, corrections in corrections_by_target.items():
        if len(corrections) > 1:
            authors = set(c["author"] for c in corrections)
            if len(authors) > 1:  # same author correcting twice isn't a conflict
                conflicts.append({
                    "field": key[1],
                    "original_event": key[0],
                    "competing_corrections": corrections,
                })
    return conflicts


# ──────────────────────────────────────────────
# Pretty printing
# ──────────────────────────────────────────────

def print_event(evt, indent=2):
    prefix = " " * indent
    print(f"{prefix}id:      {evt['id'][:12]}...")
    print(f"{prefix}type:    {evt['type']}")
    print(f"{prefix}subject: {evt['subject_id'][:12]}...")
    if evt.get("ref"):
        print(f"{prefix}ref:     {evt['ref'][:12]}...")
    print(f"{prefix}author:  {evt['author']}")
    print(f"{prefix}time:    {evt['timestamp']}")
    print(f"{prefix}payload: {json.dumps(evt['payload'])}")


def print_state(state, label="Current state"):
    print(f"  {label}:")
    for k, v in state.items():
        print(f"    {k}: {v}")


def section(title):
    print()
    print("=" * 60)
    print(f"  {title}")
    print("=" * 60)
    print()


# ──────────────────────────────────────────────
# The walkthrough
# ──────────────────────────────────────────────

def main():
    conn = sqlite3.connect(DB)
    create_event_store(conn)

    subject_id = str(uuid.uuid4())  # A household being observed
    base_time = datetime(2026, 4, 8, 9, 0, 0, tzinfo=timezone.utc)

    # ── Step 1: Basic capture ──────────────────

    section("STEP 1: Basic Capture")
    print("A CHV visits a household and records an observation.")
    print()

    evt1 = write_event(
        conn,
        event_type="record_captured",
        subject_id=subject_id,
        payload={"temp": 38.5, "cough": True, "weight_kg": 12.3},
        author="chv-042",
        timestamp=base_time,
    )

    print("Event written:")
    print_event(evt1)
    print()
    print(f"Events in store: {conn.execute('SELECT COUNT(*) FROM events').fetchone()[0]}")
    print(f"Projections:     {conn.execute('SELECT COUNT(*) FROM projections').fetchone()[0]}")
    print()
    print("Observation: the event is stored. No projection yet.")
    print("The event IS the data. Nothing else exists.")

    # ── Step 2: Build projection ───────────────

    section("STEP 2: Projection — compute current state")
    print("To show the record on screen, we project: apply all events → state.")
    print()

    state = rebuild_projection(conn, subject_id)
    print_state(state)
    print()

    cached_state, version = read_projection(conn, subject_id)
    print(f"  Projection version: {version} event(s)")
    print(f"  Cached state matches computed: {cached_state == state}")
    print()
    print("Observation: projection is a computation, not a lookup.")
    print("For 1 event, trivial. Question: how does this feel at 50 events?")

    # ── Step 3: Correction ─────────────────────

    section("STEP 3: Correction — append, don't overwrite")
    print("The CHV realizes they misread the thermometer. They correct the temp.")
    print("The original event is NOT modified. A new event references it.")
    print()

    evt2 = write_event(
        conn,
        event_type="field_corrected",
        subject_id=subject_id,
        payload={
            "field": "temp",
            "was": 38.5,
            "now": 37.5,
            "reason": "misread thermometer",
        },
        author="chv-042",
        ref=evt1["id"],
        timestamp=base_time + timedelta(hours=1),
    )

    print("Correction event written:")
    print_event(evt2)
    print()

    # Verify original is untouched
    original = get_events_for_subject(conn, subject_id)[0]
    print(f"Original event payload: {json.dumps(original['payload'])}")
    print(f"Original temp value:    {original['payload']['temp']}  ← untouched")
    print()

    # Rebuild projection
    state = rebuild_projection(conn, subject_id)
    print("Projection after correction:")
    print_state(state)
    print()
    print("Observation: append-only correction works. The original is preserved.")
    print("The projection shows the corrected value. Both events exist.")
    print(f"Events in store: {conn.execute('SELECT COUNT(*) FROM events').fetchone()[0]}")

    # ── Step 3b: Review ────────────────────────

    section("STEP 3b: Supervisor review")
    print("A supervisor reviews and approves the corrected record.")
    print()

    evt3 = write_event(
        conn,
        event_type="review_completed",
        subject_id=subject_id,
        payload={"decision": "approved", "notes": None},
        author="sup-007",
        ref=evt2["id"],
        timestamp=base_time + timedelta(hours=5),
    )

    print("Review event written:")
    print_event(evt3)
    print()

    state = rebuild_projection(conn, subject_id)
    print("Projection after review:")
    print_state(state)
    print()
    print("Observation: review is just another event. No special machinery.")
    print("The projection now shows review status alongside the data.")

    # ── Step 4: Conflict ───────────────────────

    section("STEP 4: Conflict — two offline corrections to the same field")
    print("Two CHVs independently correct the weight (both were offline).")
    print("CHV-042 says weight was 13.0. CHV-088 says weight was 11.8.")
    print("Both reference the original capture event.")
    print()

    evt4a = write_event(
        conn,
        event_type="field_corrected",
        subject_id=subject_id,
        payload={
            "field": "weight_kg",
            "was": 12.3,
            "now": 13.0,
            "reason": "re-weighed child",
        },
        author="chv-042",
        ref=evt1["id"],
        timestamp=base_time + timedelta(hours=2),
    )

    evt4b = write_event(
        conn,
        event_type="field_corrected",
        subject_id=subject_id,
        payload={
            "field": "weight_kg",
            "was": 12.3,
            "now": 11.8,
            "reason": "scale was miscalibrated, re-measured",
        },
        author="chv-088",
        ref=evt1["id"],
        timestamp=base_time + timedelta(hours=2, minutes=15),
    )

    print("Correction A (chv-042):")
    print_event(evt4a)
    print()
    print("Correction B (chv-088):")
    print_event(evt4b)
    print()

    # What does the projection show?
    state = rebuild_projection(conn, subject_id)
    print("Projection after both corrections (naive — last event wins):")
    print_state(state)
    print()

    # Detect conflicts
    events = get_events_for_subject(conn, subject_id)
    conflicts = detect_conflicts(events)

    if conflicts:
        print(f"CONFLICTS DETECTED: {len(conflicts)}")
        for c in conflicts:
            print(f"  Field: {c['field']}")
            print(f"  Original event: {c['original_event'][:12]}...")
            for corr in c["competing_corrections"]:
                print(f"    {corr['author']}: {corr['payload']['was']} → {corr['payload']['now']} "
                      f"({corr['payload']['reason']})")
    print()
    print("Observation: the conflict is DETECTABLE because both events exist.")
    print("The projection picked a winner (last-write by timestamp), but nothing")
    print("was lost. A conflict resolution event could supersede both.")
    print()
    print("KEY INSIGHT: With snapshots, you'd see two competing full records and")
    print("have to diff them field by field. With events, you see exactly which")
    print("field is contested and why each person changed it.")

    # ── Step 5: Replay — the source-of-truth test ──

    section("STEP 5: Replay — drop projections, rebuild from events")
    print("Delete ALL projections. Rebuild from the event log only.")
    print("If the result matches, the event log is truly the single source of truth.")
    print()

    # Save current projection for comparison
    state_before, version_before = read_projection(conn, subject_id)

    # Nuke projections
    conn.execute("DELETE FROM projections")
    conn.commit()
    print(f"Projections after delete: {conn.execute('SELECT COUNT(*) FROM projections').fetchone()[0]}")
    print(f"Events still in store:    {conn.execute('SELECT COUNT(*) FROM events').fetchone()[0]}")
    print()

    # Rebuild
    state_after = rebuild_projection(conn, subject_id)
    _, version_after = read_projection(conn, subject_id)

    print("Rebuilt projection:")
    print_state(state_after)
    print()
    print(f"Matches pre-delete projection: {state_before == state_after}")
    print(f"Events used:                   {version_after}")
    print()
    print("Observation: projections are truly derived. The event log alone is")
    print("sufficient to reconstruct all state. This is the escape hatch working.")

    # ── Summary ────────────────────────────────

    section("FULL EVENT LOG")
    print(f"Total events: {conn.execute('SELECT COUNT(*) FROM events').fetchone()[0]}")
    print()
    for i, evt in enumerate(get_events_for_subject(conn, subject_id), 1):
        print(f"  [{i}] {evt['type']:<20} by {evt['author']:<10} "
              f"ref={'→' + evt['ref'][:8] + '...' if evt['ref'] else '(none)':<16} "
              f"{json.dumps(evt['payload'])}")

    print()
    section("QUESTIONS TO SIT WITH")
    print("  1. The projection is simple now (3 event types, direct field mapping).")
    print("     What happens when you add 10 event types? Does project() become")
    print("     a monster, or does it stay a clean switch/match?")
    print()
    print("  2. Conflict detection worked here because both corrections referenced")
    print("     the same original event and the same field. What if they didn't")
    print("     share a ref? What if the conflict is semantic, not structural?")
    print()
    print("  3. Timestamp ordering worked for 2 events from the same day.")
    print("     What if device clocks are wrong? What if events arrive at the")
    print("     server in a different order than they were created?")
    print()
    print("  4. The envelope had: id, type, subject_id, ref, payload, author,")
    print("     timestamp. Was anything missing? Was anything unnecessary?")
    print()
    print("  5. The projection rebuild was instant for 5 events. At what point")
    print("     does it become a problem? 100 events? 1000? Per subject, or")
    print("     across all subjects on the device?")
    print()

    conn.close()


if __name__ == "__main__":
    main()
