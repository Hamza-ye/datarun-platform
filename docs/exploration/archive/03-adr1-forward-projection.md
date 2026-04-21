> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-001**. For a navigated reading guide, see [guide-adr-001.md](../guide-adr-001.md).
# ADR-1 Forward Projection: How Each Option Shapes ADRs 2–5

> This document looks past ADR-1. It does not revisit the ADR-1 analysis or re-decide it. It projects each surviving option (Snapshots A, Events B, Action Log C) forward through the four downstream ADRs to reveal where each option creates space, where it creates friction, and whether any option leads to a structural dead end.
>
> Purpose: inform the ADR-1 decision by making its downstream consequences visible.

---

## How To Read This Document

For each downstream ADR (2–5), I walk through three things:

1. **What the ADR must decide** — independent of ADR-1's choice
2. **How each ADR-1 option shapes the solution space** — what gets easier, harder, or foreclosed
3. **Irreversibility pressure** — if ADR-1 sets you on a path here, can you course-correct, or are you locked in?

At the end, a summary shows the full propagation picture and where the options diverge most sharply.

---

## ADR-2: Identity and Conflict Resolution

### What must be decided

- How subjects (people, places, things) are identified across devices
- How duplicate real-world subjects (same facility, two UUIDs) are detected and merged
- How conflicts — concurrent writes about the same subject by disconnected parties — are detected
- What constitutes a "conflict" vs. independent additive work

### Under Snapshots (A)

**Identity merge**: Two snapshot chains must be joined. A merge snapshot must contain the combined state from both chains. This is a content problem — the merge snapshot carries ALL fields from BOTH chains. If the chains diverged (different corrections, different status), the merge snapshot's `data` field must represent the resolved state. Who writes that content? A human, choosing field by field? This is the DHIS2 ERROR/WARNING wall — it pushes conflict resolution into manual data editing.

**Conflict detection**: A conflict exists when two unresolved chain heads exist for the same subject (fork in the chain). Detection is structural but coarse — you know there's a fork, but you only see two full-state snapshots. The resolver sees "version A says X" and "version B says Y" but not *why* they diverge or *what action* caused the divergence.

**Shaping effect**: ADR-2 must solve merge-by-full-state, which means the conflict resolution UI must present two complete records side by side and let a human pick field by field. This is expensive to build, hard for low-literacy users, and scales poorly as record complexity grows.

### Under Events (B)

**Identity merge**: A `subjects_merged` event links two subject streams. All future events target the canonical subject ID. Historical events under the old ID remain valid and are associated via the merge record. No content rewriting — the projection layer simply unions the two event streams when computing state for the merged subject.

**Conflict detection**: Two concurrent events on the same subject are visible as entries in the event stream that lack a causal ordering between them (detected via hybrid logical clock or vector clock). The conflict context is granular: "CHV-A recorded temp=38.5" while "CHV-B recorded temp=37.2" — not two competing full states.

**Shaping effect**: ADR-2 gets more surgical tools. Conflict resolution can be event-level: "keep both observations," "discard one," or "supersede with a resolution." The resolver sees actions, not competing states. The merge model is additive (link streams) not content-rewriting (build a combined snapshot).

### Under Action Log (C)

**Identity merge**: Same as Events — a merge log entry links the two subject IDs. The materialized views for both subjects are consolidated into one. The log retains the full history under both IDs.

**Conflict detection**: Same as Events for the log layer — concurrent entries are detectable. But the materialized view is already a single resolved state. If two conflicting log entries arrive, the view must decide what to show *right now* — it can't leave the view in a "two heads" state the way snapshots can or the way an event stream naturally represents concurrency. The application must either block the view update until resolution, or pick a tentative winner and flag the conflict.

**Shaping effect**: ADR-2 faces a tension: the log supports rich conflict detection, but the materialized view demands immediate single-state resolution. The view layer must implement conflict-awareness — either by supporting a "pending resolution" state in the view schema, or by splitting into "tentative view" and "resolved view." This is solvable but adds a concept (view-level conflict state) that Events avoid entirely.

### Irreversibility

| Dimension | A → B/C migration | B → C migration | C → B migration |
|---|---|---|---|
| Identity merge records | Must be reinterpreted from snapshot chains to events/log entries. Lossy — the merge snapshot's content decisions are embedded in data, not in a separate merge record | Add materialized views alongside event store. Non-destructive. Moderate effort | Promote log to primary source of truth, demote views to derived. Requires proving the log is actually complete (dual-write bugs may have caused gaps). Risky |
| Conflict resolution history | Lost in snapshot model — the resolution is baked into the merge snapshot. Cannot extract "what was the conflict" from the resolved state | Preserved in both — events and log entries retain the conflict and the resolution as separate records | Same as B |

**Key finding**: Moving from B to C is straightforward (add views). Moving from C to B requires trusting the log's completeness — which the dual-write model doesn't guarantee. Moving from A to either B or C requires reinterpreting history, which is lossy.

---

## ADR-3: Authorization and Selective Sync

### What must be decided

- How access rules (role + scope + context) are represented
- How they are enforced on-device against local state
- How sync scope is determined — what data flows to which device
- How stale access rules (role changed while offline) are handled

### Under Snapshots (A)

**Sync scope**: Device receives the latest snapshot chain heads for subjects in its assigned scope. To enforce context-dependent access (S04: "can review but not edit"), the device must evaluate rules against the snapshot's status field. This is a lookup — chain head has status "pending_review," user has "reviewer" role, access granted.

**Stale access**: If a role is revoked while offline, the device continues enforcing the old role. Work done under the old role produces new snapshots. On sync, these snapshots carry the author's role-at-time-of-action. The server can detect "this person no longer holds this role" and flag the work, but the snapshots themselves are valid and immutable.

**Shaping effect**: Selective sync under snapshots is conceptually simple (send chain heads for assigned subjects) but payload-heavy (full snapshots). If a supervisor needs visibility into 50 workers × 200 subjects each, they receive 10,000 chain heads — each a full record. This is the CommCare scaling wall: "Case sync at scale becomes expensive."

### Under Events (B)

**Sync scope**: Device receives events for subjects in its assigned scope. To show current state, the device computes projections locally. For a supervisor who needs read-only visibility (not the full event history), the server could send pre-computed projections instead of raw events — a "summary sync" for read-only scopes vs. "full sync" for active work.

**Stale access**: Same situation — role revoked while offline. Events carry author-role-at-time-of-action. But events are more granular: a review event from a now-revoked reviewer is a single small record, not a full state snapshot. The server can invalidate or flag just that event without cascading through a snapshot chain.

**Shaping effect**: Events open a two-tier sync strategy: full event streams for subjects the user actively works on, and compressed projections for subjects they only oversee. This directly addresses the hierarchical visibility scaling problem (S05, S09, S14, S21). Snapshots force one-size-fits-all — every consumer gets the same full records regardless of whether they're an author or an observer.

### Under Action Log (C)

**Sync scope**: Similar to Events — log entries transfer for active subjects. But the materialized views are also on-device, maintained by the application. The sync can transfer either log entries (which require local re-projection) or pre-computed view updates. This is flexible but means two sync paths — one for the log and one for views.

**Stale access**: Same as B for detection. But if the materialized view has already been updated based on a revoked user's action, the view must be corrected on sync. With Events, you recompute the projection excluding the invalidated event. With Action Log, you must apply a corrective update to the view — which is itself a new write, creating a potential cascade.

**Shaping effect**: ADR-3 under Action Log must design a view-correction mechanism for post-sync rule enforcement. Events handle this naturally (just reproject); snapshots handle it coarsely (flag the chain head); Action Log falls between — the view must be actively repaired.

### Irreversibility

The sync protocol design is costly to change but not impossible. The key lock-in is:

- **Events enable two-tier sync** (full streams for workers, summaries for supervisors). If you start with snapshots and need this later, you must add a summarization layer on top — effectively building the projection infrastructure you avoided.
- **Action Log's dual sync paths** (log entries + view updates) create a protocol surface area that compounds over time. Each new feature adds to both paths.

---

## ADR-4: Configuration Paradigm and Boundary

### What must be decided

- How operational activities are defined (what to capture, who is responsible, what the oversight structure is)
- Where the configuration ceiling sits (what is configurable vs. what requires development)
- How configuration changes propagate to devices and coexist with in-progress work
- How expressive the configuration language is without becoming a programming language (the T2 tension)

### Under Snapshots (A)

**Configuration scope**: Configuration defines record shapes (what fields, what types, what validation) and assignment rules (who owns which subjects). The configuration is essentially: "this activity produces snapshots of this shape, assigned to these people, reviewable by these people."

**Configuration change propagation**: New shapes apply to new snapshots. Old snapshots under old shapes are unaffected (they're immutable). This is clean — the snapshot model handles schema evolution well.

**The T2 tension**: Configuration must define what can be captured. This is naturally bounded — shapes, fields, validation rules. The snapshot model doesn't ask configuration to define behavior, because all behavior is "create a new snapshot." Review? New snapshot. Transfer? New snapshot. Correction? New snapshot. The configuration vocabulary stays small, but the expressiveness ceiling is low — you can't configure different behaviors for different actions because there's only one action: "make a snapshot."

**Shaping effect**: ADR-4 under snapshots is simple — maybe too simple. The configuration defines shapes and assignments, period. But this simplicity is deceptive: it pushes behavioral complexity into the client application. The app must know that a snapshot with `corrects: X` means "correction," while one with `supersedes: X` and `status: approved` means "review." These semantic rules live in application code, not in configuration. The result: "set up, not built" applies to data shapes but not to operational behavior. New behaviors require code changes.

### Under Events (B)

**Configuration scope**: Configuration must define more things:
1. **Shapes** — what data each event type carries
2. **Event types** — what kinds of actions exist (capture, correct, review, transfer, acknowledge)
3. **Projection rules** — how events compose into current state (which fields are overwritten by corrections, what "approved" means for the state progression)
4. **Composition rules** — which event types are valid for which activities, in which sequences

**Configuration change propagation**: Each event carries its shape version. New event types can be added without changing old ones — events from the old type set remain valid. Adding new projection rules requires updating the projection logic on devices, which happens on sync.

**The T2 tension**: This is where Events make ADR-4 harder. The configuration must express projection rules — "when a `review_completed` event arrives for a subject, set the subject's status to the event's `decision` field." This is dangerously close to a programming language. The question is whether projection rules can be expressed as a constrained vocabulary (finite set of projection operations: set-field, increment-counter, transition-status, add-to-list) or whether they require Turing-complete expressions.

**But there is a mitigating structure**: the event types themselves bound the projection rules. If the platform defines a fixed set of primitive event types (captured, corrected, reviewed, transferred, acknowledged, linked, escalated), the projection rules for each type can be fixed in the platform code. Configuration selects which event types an activity uses, not how they project. New event types = platform evolution, not configuration.

**Shaping effect**: ADR-4 under events has higher upfront complexity but clearer boundaries. Configuration is: "this activity uses these event types, with these shapes, assigned to these people, on this schedule." The event types are the platform's vocabulary — adding a new event type is a platform-level decision, not a deployment-level configuration. This draws a clean line for the configuration boundary: configurable (shapes, assignments, schedules, which event types to use) vs. platform evolution (new event types, new projection behaviors).

### Under Action Log (C)

**Configuration scope**: Similar to Events for the log side — log entry types, shapes, assignment rules. Additionally, configuration must define the materialized view schema — what the "current state" view looks like for each activity.

**Configuration change propagation**: Log entry types evolve like events. But materialized view schemas are harder — if the view schema changes (new computed field, restructured status), existing views on devices must be migrated or recomputed. With Events, this is free (reproject). With Action Log, view migration is an explicit operation that must be designed and tested.

**The T2 tension**: Action Log faces the SAME tension as Events (log entry types + projection-like logic for views) PLUS an additional one: the view schema is a separate configuration concern. The configuration must express both "what actions are possible" and "what should the current-state summary look like." These two aspects interact — every new action type must have a corresponding view update rule.

**Shaping effect**: ADR-4 under Action Log has the highest configuration surface area. Two schemas (log entries + views), two evolution paths, and the coupling between them. This is manageable, but it means the configuration boundary question is asked twice: "what log entry types are configurable?" AND "what view update rules are configurable?"

### Irreversibility

The configuration paradigm is the most human-visible decision — it determines what deployers interact with. Changing it means retraining every administrator and rebuilding every deployment's configuration.

- **Snapshots → Events**: Configuration must expand from "shapes" to "shapes + event types + projection rules." Every existing deployment's configuration must be migrated to express the same behavior in a richer vocabulary. Costly but not impossible.
- **Events → Action Log**: Configuration must expand to cover view schemas. This is additive — existing configuration keeps working, views are layered on top.
- **Action Log → Events**: Configuration must shrink — view schemas become derivable from projection rules. Existing view-specific configuration becomes obsolete. But devices that were maintaining views as co-primary must switch to projection-as-primary. This is a runtime behavior change that could surface bugs where the log was incomplete.

**Key finding**: Events (B) and Action Log (C) share most of the configuration surface. The main difference is that C adds view schema configuration. Moving from B to C is additive; moving from C to B means proving you can remove the view schema dependency without breaking deployments.

---

## ADR-5: State Progression and Workflow

### What must be decided

- How work moves through stages (draft → reviewed → approved; sent → received → confirmed)
- Whether state machines are explicit platform primitives or emergent from record patterns
- How offline transitions interact with concurrent transitions by others
- How multi-step approval chains (S11) and multi-hop distributions (S14) are modeled

### Under Snapshots (A)

**State model**: State is a field in the snapshot. Transitioning state = creating a new snapshot with the new status value and all the original data copied forward. The platform must enforce valid transitions — you can't go from "approved" to "draft" — but this enforcement is application logic applied when creating a new snapshot.

**Multi-step chains (S11)**: Five approval steps = five snapshots, each a full copy of the data, each with a different status and a different `by` field. The approval history is the chain of snapshots. To reconstruct "who approved at step 3?" you traverse the chain to the third snapshot.

**Offline workflow conflicts**: Two supervisors approve the same case offline. Both create a new snapshot with status "approved_by_step_2." On sync, two chain heads exist. The conflict is visible but resolution requires creating a THIRD snapshot that picks a winner. The loser's approval snapshot remains in the chain but is superseded — its approval action is invalidated by the merge.

**Shaping effect**: ADR-5 under snapshots has a fundamental modeling problem: **the approval action and the data it acts on are fused into one record.** Supervisor A's approval can't be preserved independently of the data it was applied to. If the data is later corrected, A's approval snapshot still contains the old data — it's meaningful as a historical record but misleading if someone reads it as "A approved this data."

### Under Events (B)

**State model**: State is a projection. Events like `review_completed`, `approval_granted`, `transfer_sent`, `transfer_received` fire as actions. The current state is computed by applying these events in sequence. A state machine is literally a projection rule: "given events [captured, reviewed, approved], current status = approved."

**Multi-step chains (S11)**: Five approval steps = five events of type `approval_step_completed`, each carrying the step number, the decision, and the reviewer. The data itself is separate — it was captured by a `record_captured` event and may have been modified by `field_corrected` events. Approvals act on the data but don't contain it.

**Offline workflow conflicts**: Two supervisors approve the same case offline. Two `approval_step_completed` events for the same step arrive on sync. The conflict is precise: "same step approved by two different people." Resolution options are granular — keep one, keep both (dual approval), or require re-review. The data is untouched; only the approval actions conflict.

**Shaping effect**: ADR-5 under events is the cleanest. Data and workflow are separate concerns that compose on the same subject. State machines project naturally from event sequences. Multi-step, multi-actor workflows are modeled as sequences of typed events with the projection computing current state. This is the architecture's natural fit for S04, S07, S08, S11, S14.

### Under Action Log (C)

**State model**: Workflow transitions are log entries. Current state lives in the materialized view. The view's status field is updated by application logic when a workflow log entry is written.

**Multi-step chains (S11)**: Same log entries as Events. The materialized view tracks "current step" and "step history." Each approval adds a log entry and updates the view.

**Offline workflow conflicts**: Two concurrent approvals produce two log entries. But the materialized view on each device has already been updated to reflect "approved." When both sync to the server, the server has two log entries (clear conflict) but must also reconcile two view states that each independently advanced past the conflict point. The view reconciliation is the hard part — which device's view is "correct"? Neither — both were based on partial information. The server must recompute the view from the log, which means treating the log as the source of truth for this operation — effectively behaving like Event Sourcing for conflict cases.

**Shaping effect**: ADR-5 under Action Log works for the happy path but falls back to event-sourcing semantics under conflict. This means you build the projection infrastructure anyway — just only for the hard cases. The question becomes: if you need projection for conflicts, do you gain anything by NOT using it for the normal path?

### Irreversibility

- **Snapshots → Events/Action Log**: Every workflow transition that was expressed as a full snapshot must be reinterpreted as a discrete action. Historical data can be migrated (extract the status changes from the snapshot chain) but the extraction is imperfect — you can't always distinguish "data was corrected AND status changed" from "status changed and the data difference is incidental."
- **Events → Action Log**: Add views. Non-destructive.
- **Action Log → Events**: Remove view dependency. Risky only if application code relies on views for workflow logic rather than the log.

---

## The Full Propagation Picture

| Downstream ADR | Snapshots (A) | Events (B) | Action Log (C) |
|---|---|---|---|
| **ADR-2: Identity** | Merge = content-rewriting (hard). Conflict context = coarse (two full states). | Merge = stream-linking (clean). Conflict context = granular (two actions). | Same as B for log. View merge adds complexity. |
| **ADR-3: Auth/Sync** | One-size-fits-all sync (full snapshots). Hierarchical visibility scaling problem. | Two-tier sync possible (events for workers, summaries for supervisors). | Dual sync paths (log + views). Flexible but higher protocol surface area. |
| **ADR-4: Configuration** | Simple but ceiling is low. Behavior lives in app code, not configuration. Undermines V2 for workflows. | Higher upfront complexity. Clearer boundary: shapes are configurable, event types are platform vocabulary. | Highest configuration surface. Two schemas, two evolution paths, coupling between them. |
| **ADR-5: Workflow** | Data and workflow fused. Approval actions contain full state copies. Conflict resolution is lossy. | Data and workflow separated. State machines are projections. Cleanest fit for S04/S07/S08/S11/S14. | Happy path works. Conflict cases fall back to projection anyway. Question: why maintain two paths? |

---

## Where the Options Diverge Most

### The narrowing insight

**Snapshots (A) create a structural ceiling that surfaces in ADR-4 and ADR-5.** The ceiling isn't a disaster — it means more behavior lives in application code and less in configuration. But for a platform whose central promise is "set up, not built" (V2), this is a load-bearing commitment that undercuts the vision. Workflows would work, but they'd be hard-coded patterns, not configured ones.

**Action Log (C) converges toward Events (B) under pressure.** Every time a hard case appears — conflict resolution (ADR-2), view reconciliation after offline conflicts (ADR-5), hierarchical visibility sync (ADR-3) — the Action Log reaches for the same tool: recompute from the log. If you're recomputing from the log in all the hard cases, the materialized views buy you simplicity only for the easy cases. The engineering effort to maintain TWO paths (view-primary for normal operations, log-primary for conflicts) may exceed the effort of just committing to projections everywhere.

**Events (B) pay their cost upfront and evenly.** Every downstream ADR is slightly harder at the start (you must design projections, event types, the sync protocol for event streams) but none of them hits a wall. The projection infrastructure is the recurring cost, but it's the SAME cost everywhere — not a bifurcated cost that varies by case complexity.

### The irreversibility gradient

```
A → B:  Costly. Historical data migration is lossy for workflows. Configuration must expand.
A → C:  Costly. Same migration issues, plus view schema design.
B → C:  Cheap. Add materialized views alongside events. Non-destructive, additive.
C → B:  Moderate. Must prove log completeness (dual-write gap risk). Remove view dependency.
B → A:  Destructive. Throw away granular history. Why would you.
C → A:  Destructive. Same.
```

The safest irreversibility position is **B (Events)**:
- You can move to C later (add views) cheaply if projections prove too expensive on low-end devices
- You cannot move to B from A without data loss
- You cannot move to B from C without trusting that the dual-write log is gap-free

### Where Option B could fail

To be honest about the risk: Events could fail if **projection infrastructure proves too complex for the team to build and maintain reliably on low-end Android.** This is a real risk — not because projection is theoretically hard, but because:

1. The projection layer on-device must handle events from multiple schema versions
2. It must handle out-of-order event arrival gracefully
3. It must be fast enough on low-end hardware for every screen load
4. Every bug in the projector corrupts the user's view of current state

If this risk materializes, the escape hatch is B → C: keep events as the source of truth but shift to application-maintained views for the read path — essentially adding the Action Log's view layer on top of the event store. This is additive, not destructive.

### Where Option C could fail

Action Log could fail if **dual-write consistency proves hard to maintain across the codebase.** Every developer writes to two stores on every action. A missed view update = stale state visible to users. A missed log entry = lost traceability. Over a large codebase with many event types, this is a discipline-dependent guarantee — it works if the team is rigorous, it degrades silently if they're not.

### Where Option A could fail

Snapshots could fail at **ADR-5** — workflow scenarios that require distinguishing "the action that happened" from "the state it produced." This is not speculative; it's structural. S11 (multi-step approval) and S14 (multi-level distribution) fundamentally need this distinction. Snapshots fuse them.

---

## Summary

| | Best case | Worst case | Escape hatch |
|---|---|---|---|
| **Snapshots (A)** | Simple capture works cleanly. Low infrastructure cost. | Workflows are hard-coded, not configured. V2 is undercut. ADR-5 hits a modeling wall. | Migrate to B or C. Costly, lossy for workflow history. |
| **Events (B)** | All downstream ADRs fit naturally. Single source of truth. Workflows compose. | Projection infrastructure is complex for the team. On-device projection bugs corrupt views. | Add materialized views (move toward C) without losing the event store. Non-destructive. |
| **Action Log (C)** | Pragmatic starting point. Lower barrier for developers. | Converges toward event-sourcing under pressure. Dual-write consistency degrades silently. | Promote log to primary (move toward B). Requires proving log completeness. |

**The asymmetry**: Events can retreat toward Action Log cheaply. Action Log cannot advance toward Events safely. Snapshots cannot go anywhere without loss.
