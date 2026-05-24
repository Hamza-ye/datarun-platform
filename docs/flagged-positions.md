# Flagged Positions — Living Register

> Deferred verification items and quiet-decision markers that must not be forgotten.
> This file is **append-only**. Items move to `RESOLVED` with a resolution log entry — never deleted.
>
> **When to consult**: before drafting any new IDR, before starting any new phase, during any close-out audit. Every agent working on this platform is expected to read this register as part of onboarding to a new phase.

---

## Why This File Exists

Platform work is executed by multiple AI agents across sessions. Agents do not automatically carry context between sessions. When an item is "deferred," the decision to defer it is the easy part — **remembering to pick it up later** is the hard part. The Phase 1/2 envelope-type-vocabulary drift (resolved by [ADR-007](adrs/adr-007-envelope-type-closure.md) on 2026-04-23) is a concrete example of what happens when deferrals slip through:

- A decision is made at time T.
- Code is written at time T+N that silently contradicts the decision.
- No mechanism flags the contradiction until a later audit finds it.
- Fixing it becomes a retrofit (Phase 3e) rather than a one-line correction.

This register is the counter-mechanism: every deferred verification item, every quiet position that future work might contradict, every architectural precedent that needs defending — all recorded here with an explicit **gate** that must pass before the item is considered closed.

---

## Format (for every entry)

```
## FP-NNN — Short name
Status: OPEN | IN_PROGRESS | RESOLVED | SUPERSEDED
Opened: YYYY-MM-DD by <source>
Blocks: <IDR / Phase / nothing>
Severity: A (blocks architecture) | B (blocks an IDR) | C (cleanup hygiene)

### Context
What was observed, and why it matters.

### Trigger
When this item should be picked up. Usually "before IDR-NNN" or "before Phase N".

### Gate
The specific, verifiable outcome that proves the item is resolved. If the gate is not met, the item stays OPEN. No soft closures.

### Resolution log
Dated entries as work progresses. When RESOLVED, the final entry cites the commit or artifact that closes it.
```

---

## Active Register

---

## FP-001 — `role_stale` projection-derived role verification

**Status**: RESOLVED
**Opened**: 2026-04-21 by Phase 3e review pass (audit finding A3)
**Resolved**: 2026-05-22 by projection-derived knowledge-watermark role check
**Blocks**: IDR-021 (Role-Action Enforcement)
**Severity**: A — touches ADR-3 S3 structural constraint

### Context

ADR-3 S3 is a **Structural** constraint: *"Authority context is a projection, not an envelope field."* The existing `role_stale` detection in `server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java` (lines 226–234) compares an actor's current role against their role at the time of the event. **It is not verified** whether this comparison reconstructs the role-as-of-event from the assignment event timeline, or reads it from a cache, or uses some other source.

If the implementation quietly violates S3 (reads role from anything other than replayed assignment events), IDR-021 would inherit the drift and cascade it into role-action enforcement — the same failure mode as the Phase 1/2 envelope-type drift. There is no existing test that would fail if role were read from a cache rather than derived, so the correctness here is load-bearing but unproven.

### Trigger

Before IDR-021 drafting begins. One hour of focused code reading, plus one integration test.

### Gate

All three must be true:

1. Code read confirms `role_stale` detection reconstructs the actor's role-at-event-time by replaying `assignment_changed` events up to the event's causal position, using projection semantics — not reading from any cache, envelope field, or snapshot.
2. A new integration test exists that cannot pass under a cache-based implementation: push event A with role X → admin changes role to Y via `assignment_changed` → push event B (B's creation watermark predates the change) → assert `role_stale` fires on B only, and only under projection-based derivation.
3. If the code does not meet (1), it is fixed as part of closing this FP — not punted to IDR-021.

### Resolution log

- **2026-04-21**: Opened.
- **2026-05-22**: RESOLVED. `ConflictDetector.evaluateAuth(...)` now receives push `last_pull_watermark` and derives `role_stale` by replaying `assignment_created/v1` events up to `min(event.sync_watermark, push.last_pull_watermark)`, matching the assignment event timeline/knowledge-horizon semantics rather than reading current cache/snapshot state. Current role-action tests cover the projection-derived gate: `AuthFlagIntegrationTest.roleAction_horizonRoleWithoutAction_roleStaleEvenIfCurrentAllows` proves a later promotion cannot authorize an older review event with a pre-promotion knowledge watermark, while `AuthFlagIntegrationTest.roleAction_roleLabelChangeBothPermitAction_noRoleStale` and `AuthFlagIntegrationTest.roleAction_currentRoleWithoutAction_roleStale` prove current action authority is evaluated by permission, not role-label drift.

---

## FP-002 — `subject_lifecycle` table removal

**Status**: RESOLVED
**Opened**: 2026-04-21 by Phase 3e review pass (audit finding B3)
**Resolved**: 2026-05-21 by event-derived lifecycle projection
**Reopened**: 2026-05-21 by ADR-002 lifecycle parity review
**Blocks**: Phase 4 (not a specific IDR — pattern state machines will interact with identity lifecycle)
**Severity**: B — projection discipline

### Context

The V3 Flyway migration introduced a `subject_lifecycle` table, populated during merge/split operations. Per ADR-1 S2 and ADR-5 S4, **state is always a projection of events, never an independent source of truth**. The escape hatch B→C permits projection caches only after a read-cost pressure justifies them.

The 2026-05-21 read-discipline audit found the table was implemented as a disciplined cache, but the stricter ADR-001/002 posture is simpler: no `subject_lifecycle` cache by default. Subject identity lifecycle should be projected from `subjects_merged/v1` and `subject_split/v1` events on demand, following the `ScopeResolver` precedent. B→C remains available only if a future fixture proves lifecycle projection cost is real.

### Trigger

Before Phase 4 implementation begins. Phase 4 adds pattern state machines that interact with subject identity; keeping identity lifecycle event-derived prevents ADR-005 workflow state from accidentally growing around an identity cache.

### Gate

All four must be true:

1. `subject_lifecycle` is removed from the schema and server code.
2. Merge/split precondition checks project active/archived lifecycle from identity events inside the write transaction, with transaction-scoped locking around the involved subject IDs.
3. Split-archived sources are excluded from active subject projections while their historical event stream remains accessible.
4. Post-split events referencing the archived source are accepted and flagged, not rejected or silently projected into a successor.

### Resolution log

- **2026-04-21**: Opened.
- **2026-05-21**: Resolved. `subject_lifecycle` is classified as a projection cache used for merge/split precondition locking; `IdentityService.rebuildSubjectLifecycleFromEvents()` rebuilds it from `subjects_merged/v1` and `subject_split/v1` events; `IdentityResolverIntegrationTest.subjectLifecycleProjection_rebuildsFromIdentityLifecycleEvents` proves the cache can be discarded and reconstructed with identical rows; V3 migration now carries the projection-cache warning.
- **2026-05-21**: Reopened. The cache is disciplined, but not necessary yet. Preferred direction is no `subject_lifecycle` table: project identity lifecycle from events on demand and keep ADR-001 B→C as a future performance escape hatch.
- **2026-05-21**: Resolved. `subject_lifecycle` was removed from V3, server code, and tests. Merge/split preconditions now project lifecycle from identity events inside the write transaction under subject-scoped advisory locks; active subject projection excludes merged/split sources while historical event streams remain readable; post-split source events are accepted and flagged as `stale_reference`.

---

## FP-003 — Envelope schema parity test (meta-drift protection)

**Status**: RESOLVED
**Opened**: 2026-04-21 by Phase 3e review pass (audit finding B4)
**Blocks**: Phase 3e Commit 3 (folded into 3e.5)
**Severity**: C — cleanup hygiene, but directly prevents a repeat of the root-cause drift

### Context

Two envelope schema files exist as independently-maintained copies: `contracts/envelope.schema.json` and `server/src/main/resources/envelope.schema.json`. Nothing enforces that they agree. The Phase 1/2 type-vocabulary drift was present in both because they were edited together — but nothing structural prevents one from being updated without the other, and that is the exact kind of invisible failure this register exists to prevent.

### Trigger

Phase 3e Commit 3 (docs). Already folded into scope — tracked here so that if the test is deferred for any reason, the deferral is explicit, not silent.

### Gate

A JUnit test `EnvelopeSchemaParityTest` exists in the server test suite that reads both schema files and asserts byte-for-byte equality (normalized for trailing newline). Test fails if they diverge.

### Resolution log

- **2026-04-21**: Opened. Folded into Phase 3e.5 as an in-scope deliverable.
- **2026-04-21**: RESOLVED. `server/src/test/java/dev/datarun/server/contracts/EnvelopeSchemaParityTest.java` landed in Phase 3e Commit 3. The test reads both files with `Files.readString`, normalizes trailing newlines only, and fails the build on any other divergence. Gate met.

---

## FP-004 — `assignment_ref` as potential future envelope field

**Status**: OPEN
**Opened**: 2026-04-23 by ADR-008 drafting pass (convergence round 1)
**Blocks**: any future ADR/work that introduces an Assignment-targeting emission site distinct from the current `subject_ref.type = "assignment"` channel
**Severity**: B — architecture-grade question, no current forcing function

### Context

ADR-008 §S1 settles `subject_ref` as a CONTRACT with a closed four-value type enum including `assignment`. This covers all current emission sites that target an assignment as the referent of an event. The harvest (Group 2, `actor-ref` section) notes that if Assignment evolves into a reference type with emission sites that do not fit the `subject_ref.type = "assignment"` channel — for example, events that reference *both* a subject and an assignment distinctly — a structural design decision surfaces: parameterize existing fields, or add a dedicated `assignment_ref` envelope field.

No archive material commits either way. No current operational surface forces the question.

### Trigger

Any of the following lifts this item to `BLOCKS`:

1. A proposal or discovery that an event needs to reference a subject *and* an assignment distinctly in the same envelope.
2. A deployer or platform request to correlate events to assignment lifecycle without collapsing into the subject channel.
3. Any ADR draft that touches assignment authority, assignment projection, or the assignment shape pair (`assignment_created/v1`, `assignment_ended/v1`) in a way that implies a dedicated ref.

### Gate

A successor ADR must exist, and either:

- **(resolve by decision)** explicitly close the question (parameterize vs. dedicated field) with rationale, **or**
- **(resolve by subsumption)** demonstrate that the forcing case can be handled under the existing `subject_ref` contract and record that reading as canonical.

### Resolution log

- **2026-04-23**: Opened by ADR-008 §S4 / Alt-4. No current forcing function; filed to prevent silent deferral per R-1.

---

## FP-005 — Scoped pull temporal anchor and subject-history backfill

**Status**: IN_PROGRESS
**Opened**: 2026-05-22 by ADR-003 / Phase 4 readiness review
**Blocks**: Phase 4 `ongoing_resolution` implementation. IDR-021 drafting is unblocked by the 2026-05-22 route below; role-action code must not absorb subject-history backfill or audit pull.
**Severity**: A — touches ADR-003 S2 ("sync scope = access scope") and ADR-003 S3 authority-as-projection

### Context

A rolled-back Ship-era FP raised a real ambiguity that still has a current-repo analogue: scope evaluation has different correct anchors depending on the pull class. The old text must not be imported verbatim because its repository, code paths, and "ships" strategy no longer apply, but the underlying question remains load-bearing for IDR-021 and Phase 4.

Current code has only one live sync pull path. `server/src/main/java/dev/datarun/server/sync/SyncController.java` computes scope with `scopeResolver.getActiveAssignments(actorId)` during pull, and `ActiveAssignment.isActive()` evaluates current assignment activity. That is correct for **live-sync contraction**: after reassignment away from a scope, a normal pull should not deliver new events from the old scope.

The unresolved risk is **subject-history backfill** for long-running subjects. Normal pull is watermark-based: it returns events with `sync_watermark > since_watermark`. If an actor already synced to watermark 500 and is then assigned a long-running `ongoing_resolution` subject whose history lives at watermarks 100-200, normal live pull will not return the prior timeline. Phase 4 pattern state derivation needs the full subject history to compute `current_state`, so subject-bound assignment may require a distinct backfill behavior rather than relying on the live-sync watermark path.

Historical/audit pull is also not implemented. ADR-003 explicitly deferred auditor access, so it should not be silently folded into live sync. If audit reconstruction is needed, it needs an explicit pull class/API or an explicit out-of-Phase-4 deferral.

### Trigger

Before drafting IDR-021, and again before the first Phase 4 implementation commit for `ongoing_resolution`.

### Gate

All four must be true:

1. **Live contraction stays request-time scoped**: an integration test proves that after an actor is reassigned away from a scope, a later normal pull does not deliver new events from the old scope.
2. **Subject-history backfill is decided and tested**: either:
   - a subject-bound backfill path exists and is tested: actor already synced past a subject's historical watermarks -> actor receives a new `subject_list` assignment for that subject -> next appropriate sync/backfill returns the subject's prior timeline needed for `ongoing_resolution` projection; or
   - Phase 4 explicitly does not support assigning an already-active long-running subject to an actor with a high watermark, and the limitation is documented in the Phase 4 spec.
3. **Audit/historical pull is classified**: a decision artifact states whether audit reconstruction is out of Phase 4 or requires a separate pull class/API. It must not be implied by live sync.
4. **Push-path authority semantics are not assumed from the old FP**: IDR-021 either resolves this through FP-001 or adds tests that prove role/action checks reconstruct authority from the assignment event timeline at the intended event causal position, not from a cache, envelope field, or unexamined request-time shortcut.

### Phase 4 route

This FP is explicitly routed, not fully resolved:

1. **Normal live sync remains request-time scoped.** `/api/sync/pull` must continue to evaluate current active assignments at request time and must not become a historical reconstruction channel.
2. **Subject-history backfill is required for `ongoing_resolution`.** Phase 4 must introduce or specify a distinct subject-bound history/backfill behavior before implementing `ongoing_resolution`, because a newly assigned actor with a high normal sync watermark cannot derive a long-running subject's state from live pull alone.
3. **Backfill is separate from role-action enforcement.** IDR-021 must not write role-action enforcement around this gap or make live pull historical to cover it.
4. **Audit/historical pull is out of Phase 4 live sync.** If audit reconstruction is needed later, it requires a separate pull class/API or successor decision; it must not be silently folded into normal sync or subject-history backfill.
5. **Backfill design points for Phase 4 spec/IDR.** The future backfill decision must define idempotence/cursor behavior without lowering the normal sync watermark, request-time authorization on every page, alias handling after merge/split, activity filtering for pattern state keys, and how assignment/transfer events become visible to subject-level projections.

### Resolution log

- **2026-05-22**: Opened. Current repo read found live pull uses active assignments at request time and watermark pagination; no current subject-history backfill or audit pull class is specified.
- **2026-05-22**: Live contraction portion verified. `ScopeFilteredSyncIntegrationTest.liveSyncContraction_reassignedAway_doesNotDeliverNewOldScopeEvents` proves normal `/api/sync/pull` is request-time scoped: after reassignment away from a geographic scope, a later pull from the actor's prior watermark does not deliver new events from the old scope. Remaining before role-action code begins: decide/test subject-history backfill for already-active `ongoing_resolution` subjects, and classify audit/historical pull as out of Phase 4 or as a separate pull class/API. Neither may be folded silently into live sync.
- **2026-05-22**: ROUTED for IDR-021. FP-001 satisfies the push-path authority semantics needed before drafting IDR-021. FP-005 remains `IN_PROGRESS` for Phase 4 `ongoing_resolution`, but no longer blocks IDR-021 drafting because the route above explicitly keeps live-sync contraction request-time scoped, requires a separate subject-history backfill decision before `ongoing_resolution` implementation, and classifies audit/historical pull as out of Phase 4 live sync unless a successor decision introduces a separate pull class/API.

---

## FP-006 — `temporal_authority_expired` superseded-assignment false positive

**Status**: RESOLVED
**Opened**: 2026-05-22 by Phase 4 challenge/code-readiness review
**Resolved**: 2026-05-22 by temporal authority knowledge-horizon gate
**Blocks**: Phase 4 role-action implementation and Phase 4 detection-order work
**Severity**: A — false auth flags exclude otherwise valid events from Phase 4 projections

### Context

Current `ConflictDetector.evaluateAuth(...)` iterates all ended assignments for the pushing actor and emits `temporal_authority_expired` when an ended assignment covered the event's subject/activity. The code obtains the ended assignment watermark but does not compare it against the event's effective knowledge horizon or check whether a replacement covering assignment was visible to the actor before capture.

That means a common reassignment/role-change path can over-flag:

1. Actor has assignment A covering subject/activity.
2. Server ends assignment A.
3. Server creates assignment B for the same actor covering the same subject/activity.
4. Actor syncs after B exists.
5. Actor captures and pushes work under B.
6. Auth CD can still flag the event as `temporal_authority_expired` because assignment A is ended and once covered the event.

Phase 4 makes this more dangerous because unresolved flags are excluded from pattern-state and uniqueness-derived authoritative projections. A false temporal flag on otherwise valid work would suppress valid Phase 4 state transitions and could mask role-action behavior.

### Trigger

Before implementing IDR-021 role-action enforcement or adding Phase 4 domain uniqueness / pattern transition passes after authorization CD.

### Gate

All three must be true:

1. An integration test proves replacement assignment visibility: assignment A covers actor/scope/activity -> actor syncs -> A ends -> assignment B covers the same actor/scope/activity -> actor syncs past B -> actor pushes an event in that scope -> no `temporal_authority_expired` is emitted from ended A.
2. An integration test preserves the real stale-temporal case: actor syncs under assignment A -> A ends -> actor does not sync the ending/replacement authority -> actor pushes an event created under stale authority -> `temporal_authority_expired` is emitted.
3. Auth CD uses assignment timeline knowledge explicitly enough that Phase 4 role-action evaluation can run after temporal/scope checks without inheriting false temporal flags from superseded assignments.

### Resolution log

- **2026-05-22**: Opened during Phase 4 challenge review. Code read found `ConflictDetector.evaluateAuth(...)` computes `endedWatermark` but does not use it to distinguish stale authority from a superseded assignment that the actor has already synced past.
- **2026-05-22**: RESOLVED. `ConflictDetector.evaluateAuth(...)` now emits `temporal_authority_expired` for an ended covering assignment only when `assignment_ended.sync_watermark > min(event.sync_watermark, push.last_pull_watermark)`, so actors who synced past an assignment end and replacement do not inherit false temporal flags. `AuthFlagIntegrationTest.replacementAssignmentSynced_noTemporalAuthorityExpiredFromEndedAssignment` proves the replacement visibility case, and `AuthFlagIntegrationTest.assignmentEndsAfterActorSync_withoutResync_temporalAuthorityExpiredFlagged` preserves the real stale temporal case.

---

## FP-007 — Multi-axis assignment containment and null-activity semantics

**Status**: RESOLVED
**Opened**: 2026-05-22 by assignment administration scope-containment review
**Blocks**: Phase 4 assignment-administration hardening; any implementation pass that claims ADR-003 S5 containment complete
**Severity**: A — touches ADR-003 S5 and ADR-004/ADR-009 platform-fixed scope semantics

### Context

ADR-003 S5 requires `new_assignment.scope <= creating_actor.assignment.scope`. ADR-004 S7 and ADR-009 S2 define the platform-fixed scope axes as `geographic`, `subject_list`, and `activity`, with AND composition across non-null axes and `null` meaning unrestricted only on that axis. IDR-013 already carries all three axes in `assignment_created/v1`.

Current code does not enforce that full shape. `AssignmentService.createAssignment(...)` accepts `geographicId`, `subjectList`, and `activityList`, but `validateScopeContainment(...)` validates only geography. This can allow a creator restricted by subject list or activity to create broader assignments on the ignored axes. A subject-list-only assignment with `geographic = null` can also be misread as root/admin-like if containment looks only at geography.

`ActiveAssignment.containsActivity(...)` currently treats `activity_ref = null` as passing even when the assignment has a non-null activity restriction. That conflicts with the ADR-004 S7 reading that an activity scope axis means "event.activity_ref in actor's permitted activities" for ordinary activity work. IDR-023 correctly excludes `assignment_changed` from `activities[*].roles`; this FP must not be resolved by moving assignment administration into activity role-action config.

### Trigger

Before implementing assignment-administration hardening, before claiming Phase 4 assignment authority gates complete, and before any future IDR or code path changes assignment create/end authorization.

### Gate

All of the following must be true:

1. A decision artifact states multi-axis containment semantics for assignment creation across `geographic`, `subject_list`, and `activity`.
2. Assignment creation rejects broader geography, subject-list, or activity scopes unless the creator has a single covering assignment that contains the requested scope on every axis, or explicit bootstrap/root authority.
3. `geographic = null`, `subject_list = null`, and `activity = null` in a new assignment each require unrestricted authority on that same axis or explicit bootstrap/root authority.
4. Subject-list-only assignments are proven not to imply root/admin authority merely because their geographic axis is null.
5. Bootstrap/root authority is explicit enough that "creator has no active assignments" is not silently treated as general production authority.
6. Ordinary work events with `activity_ref = null` are not authorized by activity-restricted assignments; platform/system/identity/assignment events remain separately classified.
7. Assignment ending requires target-assignment authority or explicit bootstrap/root authority, not merely a request actor ID.
8. Tests cover the above behavior and prove `assignment_changed` remains outside `activities[*].roles`.

### Resolution log

- **2026-05-22**: Opened. Code read confirmed geography-only assignment creation containment in `AssignmentService`, null-activity wildcard behavior in `ActiveAssignment`, and the need for explicit bootstrap/root semantics before implementation. Routed to [IDR-024](decisions/idr-024-multi-axis-assignment-containment.md) for the decision/doc stop.
- **2026-05-22**: RESOLVED. `AssignmentService.createAssignment(...)` now validates `geographic`, `subject_list`, and `activity` containment against one active covering creator assignment, rejects empty subject/activity arrays, and removes the requester-personal no-assignment bootstrap. `createInitialBootstrapAssignment(...)` provides an explicit initial bootstrap path bounded to the no-assignment-created system state, and `endAssignment(...)` now requires authority over the target assignment scope. `ActiveAssignment` no longer treats empty lists as unrestricted or null activity as passing activity-restricted assignments. Coverage landed in `AssignmentContainmentIntegrationTest`, `AuthFlagIntegrationTest.activityRestrictedAssignment_nullActivityWorkEvent_scopeViolation`, and the existing `DeployTimeValidatorTest.activityRoles_assignmentChangedRejected` gate.

---

## FP-008 — Assignment command actor identity binding

**Status**: RESOLVED
**Opened**: 2026-05-22 by assignment command boundary review after FP-007
**Resolved**: 2026-05-22 by authenticated assignment command actor binding
**Blocks**: Phase 4.3 domain uniqueness entry; any production exposure of assignment command endpoints
**Severity**: A — otherwise ADR-003 S5/IDR-024 containment can be evaluated for a spoofed actor

### Context

FP-007 closed multi-axis containment inside `AssignmentService`, but the ordinary command boundary still let callers supply the authority actor. `AssignmentController` accepted `creator_actor_id` on create and `actor_id` on end, while `WebConfig` only token-bound sync pull/config endpoints. If `/api/assignments` was exposed, a caller could ask the service to evaluate containment for a different actor than the authenticated caller.

The HTML `AdminController` assignment forms had the same shape: they accepted creator/actor IDs as request parameters even though this repo has no production admin authentication yet. That must not be mistaken for production assignment-administration semantics.

### Trigger

Before starting Phase 4.3 domain uniqueness, and before any deployment exposes ordinary assignment command endpoints beyond a trusted development environment.

### Gate

All five must be true:

1. Ordinary `/api/assignments` create/end requests require authenticated actor context from token/session/request state.
2. Create/end authority is evaluated for the authenticated actor, not `creator_actor_id` or `actor_id` request-body values.
3. The ordinary assignment API cannot reach the explicit initial bootstrap path by spoofing command actor fields.
4. Tests cover unauthenticated rejection, spoofed actor rejection, insufficient authenticated scope rejection, and successful covering multi-axis authenticated scope.
5. The HTML admin assignment surface either binds to authenticated admin/root actor context or is explicitly documented as non-production/dev-only while no admin auth exists.

### Resolution log

- **2026-05-22**: RESOLVED. `WebConfig` now token-binds `/api/assignments` and `/api/assignments/**`. `AssignmentController` reads the acting actor from `ActorTokenInterceptor.ACTOR_ID_ATTR`, ignores legacy `creator_actor_id`/`actor_id` request fields, and calls only the ordinary `AssignmentService.createAssignment(...)`/`endAssignment(...)` flow. `AssignmentContainmentIntegrationTest` covers unauthenticated create/end rejection, spoofed create/end actor rejection, insufficient authenticated scope rejection, covering multi-axis authenticated success, and ordinary API inability to reach initial bootstrap by spoofing. `AdminController` assignment commands are marked development-only and bind to a fixed dev admin actor until real admin auth exists; assignment forms no longer accept creator/ending actor IDs.

---

## FP-009 — Conflict resolver designation and single-writer resolution enforcement

**Status**: OPEN
**Opened**: 2026-05-24 by ADR-002 S11 parity review before Phase 4.4
**Blocks**: Phase 4.6 `transition_violation` detector; any resolver-routing, conflict-resolution authority, or auto-resolution slice; Phase 4 close-out if implemented flag categories still have unresolved resolver routing
**Severity**: A — touches ADR-002 S11 single-writer conflict resolution and prevents recursive resolution conflicts

### Context

ADR-002 S11 requires every `ConflictDetected` event to designate exactly one resolver identity. Only a `ConflictResolved` event authored by that designated resolver is canonical; resolution events from other actors are accepted but flagged as unauthorized. ADR-007 later canonicalized these as `conflict_detected/v1` and `conflict_resolved/v1` shapes under the closed envelope type vocabulary, but it did not remove the S11 obligation.

Current implementation and contracts have partial resolver metadata only. `contracts/flag-catalog.md` still lists newer categories such as `domain_uniqueness_violation` and `transition_violation` with designated resolver `TBD`, and current resolution paths do not enforce single-writer resolver authority. That is acceptable only while the active slice does not depend on resolver routing. It must not become a silent IDR-level omission.

Phase 4.4 pattern registry and binding validation does not emit `transition_violation`, create flags, resolve flags, route resolvers, or run auto-resolution, so this FP does not block 4.4 if the slice stays clean. It becomes blocking before any slice that emits resolver-dependent flags or claims conflict-resolution authority.

### Trigger

Before any of the following:

1. Implementing Phase 4.6 `transition_violation` detection.
2. Defining or using designated resolver routing for any flag category.
3. Enforcing conflict-resolution authority.
4. Implementing auto-resolution policies or system-authored `conflict_resolved/v1` events.
5. Closing Phase 4 while any implemented flag category still has unresolved resolver routing.

### Gate

All of the following must be true:

1. A decision artifact defines resolver routing for every implemented flag category, including `domain_uniqueness_violation` and `transition_violation` if their detectors are active.
2. Every emitted `conflict_detected/v1` has exactly one designated resolver identity or a deliberately defined system resolver identity.
3. `conflict_resolved/v1` authored by a non-designated resolver is accepted but flagged as unauthorized, rather than treated as canonical.
4. Resolver reassignment, if supported, is an explicit administrative event or explicitly deferred; it is not an implicit fallback.
5. Auto-resolution, if implemented, authors standard `conflict_resolved/v1` events through a designated system resolver path and does not bypass S11.
6. Tests cover authorized manual resolution, unauthorized resolution flagging, resolver routing for active categories, and system auto-resolution authority if auto-resolution is implemented.

### Resolution log

- **2026-05-24**: Opened. Routed as carried ADR debt, not a Phase 4.4 blocker. Phase 4.4 may proceed only if it remains limited to platform pattern registry and binding validation. This FP blocks transition-violation emission, resolver routing, conflict-resolution authority enforcement, auto-resolution, and Phase 4 close-out until the gate is met or explicitly re-deferred with a recorded reason.

---

## FP-010 — Platform-bundled payload shape contract parity

**Status**: OPEN
**Opened**: 2026-05-24 by IDR-025 contract-delivery review
**Blocks**: any slice that changes platform-bundled event payload shapes or claims platform payload shape contract parity; production contract-hygiene close-out
**Severity**: C — contract hygiene, but prevents cross-boundary drift

### Context

Platform-bundled event payload shapes are real cross-boundary contracts, but they are not workflow pattern definitions. They live under `contracts/shapes/` and govern platform-emitted or platform-administered events such as `assignment_created/v1`, `assignment_ended/v1`, `conflict_detected/v1`, `conflict_resolved/v1`, `subjects_merged/v1`, and `subject_split/v1`.

The IDR-025 pattern-definition work deliberately kept workflow pattern definitions separate from payload shape contracts. That was correct, but it leaves a known contract-hygiene risk: runtime server code still mirrors some platform-bundled payload shape definitions in code, and emission sites build payloads independently from the JSON Schema files. `PlatformShapeBootstrap` documents the mirror for the four identity/integrity shapes, but the mirror is not currently parity-tested against `contracts/shapes/*.schema.json`. Assignment payloads have the same drift risk through their command/emission paths.

This does not block Phase 4.5 pattern-state projection if that slice consumes only packaged pattern definitions and existing event payload fields. It must be closed or explicitly re-routed before changing platform-bundled payload schemas, claiming contract parity for payload shapes, or doing a production contract-hygiene close-out.

### Trigger

Before any of the following:

1. Changing any file under `contracts/shapes/`.
2. Changing payload fields emitted for `assignment_created/v1`, `assignment_ended/v1`, `conflict_detected/v1`, `conflict_resolved/v1`, `subjects_merged/v1`, or `subject_split/v1`.
3. Loading platform-bundled payload shapes from contract files at runtime.
4. Claiming that platform payload shape contracts are tested, not trusted.
5. Production contract-hygiene close-out.

### Gate

All of the following must be true:

1. A parity or loading strategy exists for all six platform-bundled payload shapes: `assignment_created/v1`, `assignment_ended/v1`, `conflict_detected/v1`, `conflict_resolved/v1`, `subjects_merged/v1`, and `subject_split/v1`.
2. Either the server runtime loads platform shape definitions from `contracts/shapes/*.schema.json`, or tests prove the runtime mirrors and emission payloads stay aligned with those contract files.
3. Server-emitted platform payloads are validated against the relevant platform payload contract, or a deliberate permissive boundary is documented with tests that prove required fields and stable semantics cannot drift silently.
4. Mobile/server classifiers and shared fixtures remain aligned with the platform shape refs and their versioned payload contracts.
5. CI fails if a platform-bundled payload shape contract changes without updating the relevant runtime mirror, emission path, or fixture coverage.

### Resolution log

- **2026-05-24**: Opened as a separate contract-hygiene follow-up after IDR-025. Pattern definitions are workflow contracts under `contracts/patterns/`; platform-bundled payload shapes remain event payload contracts under `contracts/shapes/` and need their own parity/loading gate.

---

## Standing Register Rules

These rules govern how the register is used. They are not items — they are the discipline.

### Rule R-1: No silent deferral

If an agent, during any phase, observes a position that is "almost certainly right but not verified" or "correct today but could drift under future work," the agent MUST add an FP entry before closing the phase. Not adding an entry and trusting memory is the failure mode that produced Phase 1/2 drift. **Silent deferral is a forbidden pattern.**

### Rule R-2: Gates are verifiable, not aspirational

Every gate must be expressible as "X is true" where X can be checked by reading code, running a test, or grepping for a string. "We believe this is fine" is not a gate. "Test FooTest asserts Y" is a gate.

### Rule R-3: Status changes only with evidence

Moving an item from `OPEN` to `RESOLVED` requires the resolution log to cite a commit SHA, test name, or artifact path that makes the gate pass. The orchestrating agent (not a subagent) is responsible for the status transition.

### Rule R-4: Consult before writing an IDR or starting a phase

Before any of the following, the active agent MUST grep/read this register for items whose `Blocks:` field names the upcoming work:

- Drafting a new IDR
- Starting a new phase spec
- Beginning the first commit of a new phase
- Publishing a close-out audit

Items that block the upcoming work must be resolved (or explicitly re-deferred with justification recorded in the item's log) before proceeding.

### Rule R-5: `SUPERSEDED` status for orphaned items

If an architectural change (a new ADR, an addendum, a phase spec) makes an FP item obsolete, mark it `SUPERSEDED` with a pointer to the artifact that absorbed it. Do not delete. History matters for traceability.

---

## References from Other Documents

This register is referenced from:

- [`CLAUDE.md`](../CLAUDE.md) — Agent onboarding pointer
- [`docs/status.md`](status.md) — Carried-debt section
- [`docs/agent-workflow/lessons.md`](agent-workflow/lessons.md) — L-2 (register discipline)
- Any phase spec that creates an FP entry (e.g., [phase-3e.md](implementation/phases/phase-3e.md) §10)

If you add a new FP item, add or update the backlinks above so the register is reachable from every likely entry point.
