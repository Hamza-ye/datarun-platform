# Flagged Positions — Living Register

> Deferred verification items and quiet-decision markers that must not be forgotten.
> This file is **append-only**. Items move to `RESOLVED` with a resolution log entry — never deleted.
>
> **When to consult**: before drafting any new IDR, before starting any new phase, during any close-out audit. Every agent working on this platform is expected to read this register as part of onboarding to a new phase.

---

## Why This File Exists

Platform work is executed by multiple AI agents across sessions. Agents do not automatically carry context between sessions. When an item is "deferred," the decision to defer it is the easy part — **remembering to pick it up later** is the hard part. The Phase 1/2 envelope-type-vocabulary drift (resolved by [ADR-002 Addendum](adrs/adr-002-addendum-type-vocabulary.md) on 2026-04-21) is a concrete example of what happens when deferrals slip through:

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

**Status**: OPEN
**Opened**: 2026-04-21 by Phase 3e review pass (audit finding A3)
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
- **2026-04-24**: Re-scoped by [Ship-1 spec](ships/ship-1.md) §5. Original gate references discarded pre-convergence code. New gate: Ship-1 retro must confirm scope reconstruction replays `assignment_changed` events (no cache, no envelope field, no snapshot), and a test exists that would fail under a cache-based implementation. Closure deferred to Ship-1 retro; `Blocks:` field (IDR-021) is obsolete — role-action enforcement is a Ship-3 or later concern under the new cadence.

---

## FP-002 — `subject_lifecycle` table read-discipline audit

**Status**: OPEN
**Opened**: 2026-04-21 by Phase 3e review pass (audit finding B3)
**Blocks**: Phase 4 (not a specific IDR — pattern state machines will interact with identity lifecycle)
**Severity**: B — projection discipline

### Context

The V3 Flyway migration introduced a `subject_lifecycle` table, populated during merge/split operations. Per ADR-1 S2 and ADR-5 S4, **state is always a projection of events, never an independent source of truth**. The escape hatch B→C explicitly permits projection caches — but only if every read is defensive (the cache can be rebuilt from events) and there is no read path that treats the cache as authoritative.

It is currently unverified whether `subject_lifecycle` is used as a write-only cache or whether any read path treats it as the state of record. If a read path treats it as authoritative, that is a silent stored-state drift — the same failure class as Phase 1/2 type-vocabulary, different layer.

### Trigger

Before Phase 4 implementation begins. Phase 4 adds pattern state machines that interact with subject identity — any existing sloppiness around identity state discipline will be load-bearing by the time patterns land, and will be much harder to unwind then.

### Gate

All three must be true:

1. Every read of `subject_lifecycle` is classified as (a) defensive/cacheable or (b) authoritative. If any read is authoritative, it is rewritten to read from events + alias projection instead.
2. A rebuild procedure exists (even if only documented) that regenerates `subject_lifecycle` contents from the event store, and a test proves the rebuild produces identical rows.
3. V3 migration file carries a comment: *"Projection cache. Rebuildable from events. Never the state of record."*

### Resolution log

- **2026-04-21**: Opened.
- **2026-04-24**: Confirmed out of scope for Ship-1 (no merge/split). `Blocks:` field updated conceptually — not Phase 4, but Ship-2 (long-running subjects + merge/split). The V3 migration and the code it audits are pre-convergence artifacts; gate is re-assessed against Ship-2's implementation at Ship-2 start.

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

## FP-005 — Corrections surface is unassigned in the 5-Ship map

**Status**: OPEN
**Opened**: 2026-04-24 by post-Ship-1 ADR-1 × Ship-1 coverage scan
**Blocks**: any Ship that would need corrections to exist without naming them; currently no Ship has them scheduled
**Severity**: A — the behaviour is ADR-001-decided but has no delivery plan

### Context

ADR-001 §S1 commits: *"Corrections, reviews, status changes, and amendments produce new records that reference earlier ones."* S00's "what makes this hard" section elaborates: *"A record may need to be corrected after it was considered complete. The correction must be traceable — who changed what, when, and why — without erasing the original. Corrections made while offline add further complexity."*

This is a first-class platform behaviour — not a nice-to-have. A grep of `docs/scenarios/` for `correction`, `amendment`, `amend`, `supersed` finds the word only in S00's edge-case paragraph and in S04's reviewer workflow ("send back for correction"). **No scenario is dedicated to the CHV-initiated correction flow.** S04 covers reviewer-initiated send-back; the ADR-001 §S1 clause names a broader behaviour.

Consequence: the 5-Ship map (`docs/ships/README.md`) has no Ship where the correction event (a new record referencing an earlier one with a "this supersedes X" shape) is first exercised. The ADR-002 §S6–§S11 merge/split shapes (Ship-2) are a different concern — they reconcile two subjects, not two captures of the same subject. The ADR-005 review flow (Ship-3/5 under current map) is reviewer-driven, not CHV-driven.

### Trigger

Any of the following lifts this item to `BLOCKS`:

1. A Ship spec is opened whose scenarios implicitly require corrections (e.g., S04 supervisor review sending back → CHV must be able to amend).
2. A retro surfaces a real field case where a CHV wants to fix a prior capture and the platform has no path.
3. Cleanup pass before Ship-3 open, if Ship-3 includes S04.

### Gate

All of the following must be true:

1. A scenario (existing or new) is explicitly assigned the CHV-correction behaviour in `docs/ships/README.md` with a named Ship.
2. A shape (or shape family) for the correction event exists under `contracts/shapes/` with a payload that carries at minimum: reference to the original event, reason for correction, new values.
3. The Ship's walkthrough exercises: original capture → correction capture offline → sync → server accepts both, projection reflects the corrected state, audit trail preserves the original.
4. ADR-001 §S1 is re-read to confirm whether the correction shape needs an envelope-level position (likely reusing `subject_ref` channel with a payload-level `corrects_event_id`, but that is a design decision the triggering Ship must make).

### Resolution log

- **2026-04-24**: Opened by post-Ship-1 coverage scan. No current forcing function; current-map Ship-3 (S04 supervisor review) is the earliest likely trigger.

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
