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
- **2026-05-22**: RESOLVED. `ConflictDetector.evaluateAuth(...)` now receives push `last_pull_watermark` and derives `role_stale` by replaying `assignment_created/v1` events up to `min(event.sync_watermark, push.last_pull_watermark)`, matching the assignment event timeline/knowledge-horizon semantics rather than reading current cache/snapshot state. `AuthFlagIntegrationTest.roleStale_usesDeviceKnowledgeWatermark_notPushWatermark` proves the cache/push-watermark shortcut fails the gate: event A before the role change stays clean, while event B pushed after the role change with a pre-change knowledge watermark receives `role_stale`.

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
**Blocks**: IDR-021 (Role-Action Enforcement), Phase 4 `ongoing_resolution` implementation
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

### Resolution log

- **2026-05-22**: Opened. Current repo read found live pull uses active assignments at request time and watermark pagination; no current subject-history backfill or audit pull class is specified.
- **2026-05-22**: Live contraction portion verified. `ScopeFilteredSyncIntegrationTest.liveSyncContraction_reassignedAway_doesNotDeliverNewOldScopeEvents` proves normal `/api/sync/pull` is request-time scoped: after reassignment away from a geographic scope, a later pull from the actor's prior watermark does not deliver new events from the old scope. Remaining before role-action code begins: decide/test subject-history backfill for already-active `ongoing_resolution` subjects, and classify audit/historical pull as out of Phase 4 or as a separate pull class/API. Neither may be folded silently into live sync.

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
