> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-003**. For a navigated reading guide, see [guide-adr-003.md](../guide-adr-003.md).
# ADR-003: Irreversibility Filter and Stress Test Reconciliation

> Phase: 3 of 3
> Date: 2026-04-11
> Input: [10-adr3-policy-scenarios.md](10-adr3-policy-scenarios.md), [11-adr3-stress-test.md](11-adr3-stress-test.md)
> Contribution: Introduced the irreversibility filter methodology (now codified in [00-exploration-framework.md](00-exploration-framework.md)), classified all Phase 1 findings by permanence, evaluated the authority-context envelope question, and reconciled the stress test findings after Option (c) was chosen.

This document was produced in two stages. The first (sections 1–5) applied the irreversibility filter to Phase 1 findings and evaluated the envelope question independently. The second (section 6) was written after the Phase 2 stress test completed in parallel, reconciling its findings against the Option (c) decision the filter had already converged on.


## The Core Problem

The Phase 2 prompt was treating ADR-003 as if it were ADR-002 — a decision where most findings touch the event envelope and are therefore permanent. But Phase 1 already showed that this isn't the case.

Let me apply the irreversibility boundary test to every Phase 1 finding, right now, and sort what's actually permanent from what can change later.

---

## Irreversibility Filter: What Actually Touches the Envelope?

The test: **"If we commit to this and later discover it's wrong, can we change it without migrating stored events or breaking deployed devices?"**

### Genuinely Irreversible (touches event envelope)

| Finding | Why irreversible | What must be decided |
|---------|-----------------|---------------------|
| **EQ-1: Authority context in envelope** | ADR-001 S5 explicitly reserves envelope space for "authority context — whatever ADR-3 requires." Once events are stored with a specific authority_context structure, that structure is in every event forever. | What fields go in the authority_context? Is it `assignment_ref`? A list? A compound structure? |
| **HS-7: Scope composition** (partially) | Only the part that determines what goes in the envelope. If events carry a single `assignment_ref`, campaigns need a different mechanism. If events carry a list, the envelope is more complex but more flexible. | Single reference or list in the envelope? |

**That's it.** Two items, and HS-7 is really a sub-question of EQ-1. The envelope question is: **what does `authority_context` contain?**

### NOT Irreversible (sync protocol, projection, or policy — all changeable)

| Finding | Why changeable | Category |
|---------|---------------|----------|
| **HS-1**: Subject history completeness on reassignment | Sync payload decision. Can start with "full history" and later add "recent-only first sync" without changing stored events. | Sync strategy |
| **HS-2**: Assessment visibility to CHV | Sync scope filter. Whether the CHV receives supervisor assessment events is a server-side sync filter that can change per deployment. | Sync strategy |
| **HS-3**: Data removal on scope narrowing | Device-side behavior. Purge vs. hide vs. retain affects the app, not the event store. Can be changed in an app update without data migration. | Device policy |
| **HS-4**: Projection location for supervisors | Server-side architecture. Whether the server sends raw events or pre-computed summaries is a sync protocol optimization that can evolve. | Sync strategy |
| **HS-5**: Stale-scope flag severity | Flag processing policy. Whether `ScopeStaleFlag` blocks downstream policies is a server-side rule. Can be changed without affecting stored events. | Processing policy |
| **HS-6**: Scope transition atomicity | Sync protocol behavior. Whether assignment changes and new data arrive together is an implementation concern. Can be improved over versions. | Sync protocol |
| **HS-8**: Temporal enforcement is server-side only | Already determined by ADR-002 S3 (device_time is advisory). This isn't even an ADR-003 decision — it's a consequence of ADR-002. | Already decided |
| **HS-9**: Grace period for expired authority | Deployment-configurable policy. Obviously not an envelope concern. | Configuration |
| **HS-10**: Cross-level visibility boundaries | Sync scope filter per level. Server-side, changeable per deployment. | Sync strategy |
| **HS-11**: Upward visibility aggregation | Projection architecture. Whether coordinators query raw events or summaries is a server optimization. | Projection strategy |
| **HS-12**: Scope-crossing merge | Interaction between ADR-002 alias mechanism and sync scope. Handled in the projection layer — the alias is already an event (ADR-002 S6), the scope filter at sync time determines what's visible. | Sync/projection |
| **HS-13**: Orphaned conflicts | Operational workflow for conflict re-designation. Does not touch the envelope. | Processing policy |
| **EQ-2**: Sync scope = access control | Architectural principle, not an envelope field. How the server computes sync scope is protocol logic. | Architecture |
| **EQ-3**: Assignment-based access control | The authorization *model* — how rules are evaluated. This is server-side logic. The only part that touches the envelope is EQ-1. | Architecture |
| **EQ-4**: Expansion vs. contraction asymmetry | Observation about sync behavior, not a decision. | Principle |
| **EQ-5**: Device sharing | Device-side multi-user behavior. Can evolve per app version. | Device policy |
| **EQ-6**: Auditor access | Scope model extension. Whether auditors use geographic or query-based scope is a first-class question, but it doesn't change the envelope. It changes how assignments are defined. | Assignment model |

---

## What This Means

**ADR-003 has exactly ONE irreversible decision: what goes in the event envelope's authority_context.**

Everything else is sync protocol, projection architecture, device policy, or processing strategy. All of those are changeable after deployment without migrating stored events — they're Bucket 2 or Bucket 3 by definition.

This is fundamentally different from ADR-002, where identity references, causal ordering fields, and the conflict detection model all touched the envelope. ADR-002 had ~8 envelope-level decisions. ADR-003 has ~1.

### Why the stress test was overkill

The Phase 2 prompt tried to stress-test 5 mechanisms with 25+ attack paths because ADR-002's stress test did the same. But ADR-002's mechanisms (accept-and-flag, alias table, device-sequence ordering) all touched stored events. ADR-003's mechanisms (sync scope computation, projection location, temporal enforcement, stale-rule handling) mostly don't.

Stress-testing evolvable strategies as if they're permanent commitments doesn't make the decisions better — it makes them feel bigger and more locked-in than they need to be. That's the mud spiral you sensed.

---

## Resolution

The filter showed ADR-003's decision surface is narrower than ADR-002's — ~1 envelope question vs. ~8. Two options were considered:

- **Option A**: Skip full stress test. Decide the envelope question directly, write a lean ADR committing only the envelope decision + architectural principles as leans + strategies under "Strategy" headings.
- **Option B**: Run a minimal targeted stress test on the envelope options only, then classify.

**What happened**: The stress test (Phase 2) was already running in parallel when this filter was developed. Option A's spirit guided the ADR — commit the envelope decision and architectural constraints, document everything else as strategies. The stress test findings were reconciled retroactively (see "Stress Test Impact" below) and surfaced four genuine constraints the filter alone would have missed (scope-containment, alias-respects-scope, online-only resolution, detect-before-act extension).

**Lesson codified in framework**: The irreversibility filter should run *before* Phase 2, not in parallel. It scopes the stress test so adversarial depth is proportional to the actual constraint surface. See [00-exploration-framework.md](00-exploration-framework.md) § "Irreversibility Filter."

---

## The Envelope Question — Let's Settle It

The three options, with trade-offs:

### Option (a): Single `assignment_ref`
```
authority_context: {
  assignment_ref: {type: assignment, id: uuid}
}
```
- **Pro**: Simplest. Fixed-width. One event, one assignment.
- **Con**: When a worker has standing + campaign assignments and creates a campaign observation, which assignment goes in the field? If campaign, standing operations need their own assignment ref. If standing, campaign attribution is lost.
- **Verdict**: Doesn't survive S09 (campaign) without workarounds.

### Option (b): Compound authority context
```
authority_context: {
  assignment_refs: [{type: assignment, id: uuid}, ...],
  process_ref: {type: process, id: uuid} | null
}
```
- **Pro**: Handles campaigns naturally (both assignments listed). Process ref enables campaign reporting. All authority traceable.
- **Con**: Variable-length list. Slightly larger envelope. Most events will have exactly 1 assignment (the standing one) — the list is overhead for the common case.
- **Verdict**: Works but may over-engineer the envelope for the 95% case.

### Option (c): No authority_context in envelope — derive on sync
```
// Event envelope has NO authority_context field.
// On sync, the server reconstructs authority:
//   "At device_sequence N, actor X had assignment(s) Y active"
// Authority is a projection, not a stored fact.
```
- **Pro**: Smallest envelope. No variable-length fields. Authority is always reconstructed from the (already-stored) assignment event timeline — same pattern as projections.
- **Con**: The device can't display "which assignment authorized this" without reconstructing locally. If the assignment timeline is incomplete on-device (partial sync), the authority is ambiguous. **Also**: assignment events might not have synced in order — the reconstruction depends on having the full assignment history for the actor.
- **Verdict**: Cleanest architecturally (authority IS a projection), but reconstruction correctness depends on sync completeness.

> [!IMPORTANT]
> Option (c) is the most interesting because it aligns with ADR-001's core principle: projections are derived from events. Authority-as-projection means no new envelope fields, no variable-length structures, and the same reconstruction guarantees that already apply to subject projections.
>
> But it requires that assignment events are reliably synced before (or with) the work events they authorize. If a work event syncs before its authorizing assignment event, the server can't reconstruct authority at that moment. This is a sync ordering question, not an envelope question.

The choice between (b) and (c) is the real decision. And it can be evaluated without a 25-attack-path stress test.

---

## Stress Test Impact and Classification

The stress test (Phase 2) was completed independently while the course correction above was being developed. The stress test assumed authority_context as an envelope field (Mechanism E) — a path the course correction had already rejected in favor of Option (c). This section classifies the stress test findings against the Option (c) decision and identifies what the ADR must adopt.

### The Big Picture

The stress test assumed **Mechanism E** — authority_context as an envelope field.  
Our ADR-003 chose **Option (c)** — authority as projection, no envelope field.

This mismatch actually resolves several of the stress test's hardest problems (E2, E5) while leaving the genuine structural findings intact. Here's the classification.

---

### Findings That Strengthen Our Option (c) Choice

These findings from the stress test are **arguments FOR** authority-as-projection:

| Finding | Why it supports our decision |
|---------|------------------------------|
| **E2**: Variable-length assignment ref list is problematic | We avoid this entirely — no envelope field, no schema problem. |
| **E3**: Authority context is "an assertion, not a verified fact" | If the server always re-derives authority anyway, storing the device's assertion adds redundant data. Derive the fact. |
| **E5**: System events need a special platform actor for authority_context | Eliminated. No field = no need for a platform actor. System events are identified by event type. |
| **HS-7 resolution**: Max 2 refs (primary + secondary) | We sidestep the whole primary/secondary debate — authority is derived, not stored. |

**Net effect**: The stress test's entire Mechanism E section (5 findings) is resolved by our Option (c). This confirms the choice.

---

### Findings That Require ADR-003 Updates (Constraint-level)

These findings are genuine structural constraints that our draft missed:

#### 1. A3: Server-Side Scope-Containment Invariant on AssignmentCreated

**What**: A coordinator must not be able to create assignments outside their own scope. `new_assignment.scope ⊆ creating_actor.assignment.scope`.

**Why it's a constraint**: Without this, any compromised coordinator account can grant national-scope access to any worker. This is a privilege escalation vector that can't be "flagged and resolved" — the damage (data exposure) happens the moment the sync delivers the data.

**ADR-003 impact**: Committed as ADR-003 S4.

#### 2. Alias-Respects-Original-Scope Rule (A1, HS-12, Combo α)

**What**: Authorization of an event is evaluated against the subject's scope at the time of event creation, not the post-merge surviving scope.

**Why it's a constraint**: Without this, a scope-crossing merge silently projects events into unauthorized scopes. A worker's events about Subject S1 (authorized, in their village) get projected into S2's stream (unauthorized, different village) via alias — without any scope-crossing flag.

**ADR-003 impact**: Committed as ADR-003 S5. Integrates cleanly with Option (c) — the server reconstructs authority from the assignment timeline at the event's creation time, against the original subject_ref.

#### 3. Conflict Resolution Is Online-Only (C4, HS-13)

**What**: `ConflictResolved` events can only be created through a server-validated transaction — same as ADR-002 S10 (merge/split are online-only).

**Why it's a constraint**: Without this, an offline resolver whose own authority has changed creates a resolution that is itself flagged, creating a meta-flag chain. Online-only terminates the chain at depth 0.

**ADR-003 impact**: Committed as ADR-003 S6.

#### 4. Detect-Before-Act Extended to Authorization Flags (C2)

**What**: ADR-002 S12's guarantee — "flagged events do not trigger policy execution until resolved" — must extend to authorization flags, not just identity conflicts.

**Why it matters**: A trainee's treatment events (created offline under stale role) must NOT trigger supply deduction before a coordinator reviews whether the treatments are valid. If policies fire first, the deduction is immutable and correction requires additional events.

**Classification nuance**: The stress test calls this Bucket 1. I think it's **Bucket 1 for the extension of S12's scope**, but **Bucket 2 for which specific flag types block policies**. The structural constraint is: "the detect-before-act guarantee applies to all flag types generated during sync processing." Which flags are configured as blocking vs. informational is a deployment policy — but the guarantee that blocking flags DO block must be structural.

**ADR-003 impact**: Committed as ADR-003 S7. The framework (detect-before-act covers all flag types) is a constraint. The per-flag-type severity configuration (blocking vs. informational) remains a deployment strategy.

---

### Findings Correctly Classified as Strategies (No ADR-003 Constraint Change)

| Finding | Our classification | Why strategy, not constraint |
|---------|-------------------|------------------------------|
| **B3**: First-sync bandwidth on 2G | Strategy (priority sync) | Server-side delivery optimization. Can be added, changed, or removed without affecting stored events. |
| **B4**: Data removal on scope contraction | Strategy S5 (deployment policy) | The stress test argues for selective purge. Valid — but the *choice* between purge, selective-retain, and retain is per-deployment. Platform provides the mechanism; deployment chooses the policy. |
| **D2**: Last-sync-time in supervisor summaries | Strategy (UI metadata) | Server-computed summary content can evolve. |
| **C1**: Batch auto-resolution for ScopeStaleFlags | Strategy | Server-side resolution logic. The watermark-based auto-resolution rule is elegant but is processing logic, not stored data. |
| **Combo β**: Shared device watermark corruption | Acknowledged limitation / Strategy | Per-actor sync sessions fix this without changing the event envelope. Shared devices are common but the fix is sync protocol, not event structure. |

---

### Findings That the Stress Test Overcalls

| Finding | Stress test classification | My assessment | Why |
|---------|---------------------------|---------------|-----|
| **C5**: Sensitive-subject classification as Bucket 1 | Constraint (Bucket 1) | **Strategy with a constraint envelope** | What counts as "sensitive" is deployment-specific. The platform needs the *ability* to classify subjects and modify sync behavior — but this is sync filter configuration (ADR-4), not ADR-003 structure. |
| **Actor-partitioned local storage** | Constraint (Bucket 1) | **Implementation** | This is a device-side SQLite schema decision. Important, yes. But it doesn't affect the event model, the sync protocol, or the server. It's app architecture, not platform architecture. |
| **HS-2**: Actor-as-subject visibility scope | Constraint extension | **ADR-4 / Implementation** | "Events about me" is a sync filter dimension. The assignment model doesn't need a new type — the sync protocol needs a filter rule: "also deliver events where subject_ref = actor's own actor_id." This is server-side sync logic, not a structural assignment change. |
| **Combo δ**: Auditor access requires 4 structural additions | BREAKS | **Deferred correctly** | Auditors are coordinators with broader scope + read-only + time-limited. "Read-only" is a role capability, not a new structural mechanism. The evolvability check passes: auditor access can be added later as a new role type + assignment configuration. |

---

## Evolvability Analysis

The following checks were performed during exploration and inform the ADR's sub-decision classifications. The ADR records the classification result; the analysis lives here.

### S1 (Assignment-Based Access Control)
Can new scope types be added? **Yes** — subject-based scope (for case management) or query-based scope (for auditors) would be new assignment configurations, not structural changes. The access check is still scope-containment; only the scope definition changes.

### S2 (Sync Scope = Access Scope)
Can sync scope computation change? **Yes** — the algorithm that determines "which events go to which device" is server-side logic. It can be optimized (paginated sync, priority ordering, delta compression) without changing stored events.

### S3 (Authority-as-Projection)
Can we add authority fields to the envelope later? **Yes** — ADR-001 S5 explicitly provides for envelope extension. If projection-based authority reconstruction proves insufficient (e.g., performance at scale, or assignment timeline reconstruction becomes too complex), an `authority_context` field can be added to new events. Old events without the field remain valid — the server reconstructs their authority from the timeline. The retreat path is cheap.

### S4 (Alias-Respects-Original-Scope)
Can this rule be changed? **No** — it's locked by the event's immutable `subject_ref`. The original reference is what the event contains; evaluation against it is the only correct interpretation. Permanent constraint, intentionally so.

### S5 (Scope-Containment on Assignment Creation)
Can this invariant be relaxed? **Yes** — if a deployment needs "super-coordinator" roles that can assign across the full hierarchy, the scope-containment check can be relaxed for specific role types. The check is server-side logic. But the default must be containment, not permissiveness.

### S6 (Conflict Resolution Online-Only)
Can this be relaxed? **Potentially** — if a deployment needs field-level conflict resolution (e.g., a supervisor resolves a flag during an offline review visit), the constraint could be relaxed for specific flag types with low severity. The default must be online-only for the same reasons ADR-002 S10 established for merge/split.

### S7 (Detect-Before-Act Extension)
Can this be changed? **Yes** for configuration, **no** for the mechanism. The structural guarantee (detect-before-act covers all flag types) is permanent. The per-flag-type severity configuration (blocking vs. informational) is deployment-level and evolvable. New flag types can be introduced and classified without structural changes.
