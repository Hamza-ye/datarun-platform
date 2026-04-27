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
- **2026-04-24** (Ship-1 retro §3.2): `ScopeResolver` confirmed to be event-replay with no cache (Javadoc explicit, no projection table). Gate part 1 met by construction. Gate part 2 (test that would fail under a cache-based implementation) **not yet authored** — Ship-1's W-2 covers correctness at the current scale but does not exercise temporal divergence (role-X-then-Y-then-replay-back-to-X). Carried as live debt.
- **2026-04-25** (Ship-2 R-4 sweep): does not block Ship-2 (S06 merge/split — identity, not authority). Stays OPEN; next re-evaluation when role-action enforcement first lands (Ship-5 judgment / approvals under the 7-Ship map). The temporal-divergence test remains the outstanding piece of the gate; lands at the Ship that first depends on it.
- **2026-04-27** (Ship-3 closeout Wave 1 G-7' + Wave 2-A): the original `role_stale` substrate (pre-convergence `dev.datarun.server.integrity.ConflictDetector` lines 226–234, per the original 2026-04-21 line cite) was discarded at Ship-1's clean-slate rebuild and never reinstated under `dev.datarun.ship1`. `grep -r role_stale server/` returns zero matches at Ship-3 HEAD. The 2026-04-24 / 2026-04-25 retro entries silently re-aimed the FP at "scope reconstruction" without re-writing the gate text — a Rule R-1 silent-deferral failure that the closeout caught. The *intent* of gate-2 (projection-time correctness, demonstrated to defeat any cache/request-time regression) is closed by Wave-1 G-7' commit [`8be0ae2`](../server/src/test/java/dev/datarun/ship1/acceptance/ScopeViolationTemporalDivergenceTest.java) — `ScopeViolationTemporalDivergenceTest`'s Event-C forcing property exercises the same invariant against `scope_violation` (the substrate that does exist). The `role_stale` half — a `role_stale` detector that wires [`ScopeResolver.hasRoleAt(...)`](../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) (Ship-2, currently caller-less) into the [`ConflictDetector`](../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) push path — is a Ship-5 concern (judgment / approvals / role-action enforcement). Carved out as [FP-017](#fp-017--role_stale-detector-wiring-successor-to-fp-001) below; FP-001 to be marked **SUPERSEDED** by FP-017 in PM Wave 2-B.

---

## FP-002 — `subject_lifecycle` table read-discipline audit

**Status**: RESOLVED
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
- **2026-04-25** (Ship-2 R-4 sweep): **BLOCKS Ship-2**. Ship-2 = S06 registry lifecycle + merge/split under the 7-Ship map; merge/split is the first time subject-lifecycle state is exercised on the platform. The pre-convergence V3 table no longer exists in `server/src/main/resources/db/migration/V1__ship1_schema.sql` — the slate is clean. **Ship-2 spec must explicitly choose** either (a) no `subject_lifecycle` table; lifecycle is projected from events on demand (the `ScopeResolver` precedent), or (b) a rebuildable cache with the gate's three conditions baked into the spec and the acceptance walkthrough. The choice and its proof are a Ship-2 spec deliverable; closure of FP-002 is a Ship-2 retro deliverable.
- **2026-04-25** (Ship-2 OQ-1): path (a) locked at spec close — no `subject_lifecycle` table; lifecycle replays `subjects_merged/v1` / `subject_split/v1` on demand (the [`ScopeResolver`](../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) precedent). Closure evidence deferred to Ship-2 retro.
- **2026-04-26** (Ship-2 close): **RESOLVED — option (a)**. Gate met by mechanical inspection at Ship-2 HEAD:
  1. *No `subject_lifecycle` table*: `git diff ship-1b..HEAD -- server/src/main/resources/db/migration/` returns no lines (zero migration touches in Ship-2; `V1__ship1_schema.sql` carries no such table).
  2. *No source file reads `subject_lifecycle` as a state source*: `git grep -l subject_lifecycle server/` returns zero matches. The structural test would fail under any silent re-introduction.
  3. *Reads project from events on demand*: the `coordinator` recognition path commits at `ecf3ece` ([`ScopeResolver.hasRoleAt`](../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java)) replays `assignment_created/v1` / `assignment_ended/v1`; the alias projection commits at `17461d9` ([`SubjectAliasProjector`](../server/src/main/java/dev/datarun/ship1/admin/SubjectAliasProjector.java)) rebuilds eagerly per request from `subjects_merged/v1` / `subject_split/v1`. Both follow the `ScopeResolver` precedent — no cache, no projection table.

  Cache is intentionally absent. Escape hatch ADR-001 §S2 (B→C) remains available if a future Ship's fixture surfaces read cost; FP-002's gate is already specified for that path.

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

## FP-006 — S7↔S8 attribution semantics in the corrective-split case

**Status**: OPEN
**Opened**: 2026-04-25 by Ship-2 spec review (intra-ADR-002 tension surfaced during partner-mode pressure-test)
**Blocks**: the first Ship that introduces device flows referencing existing subjects by UUID (likely Ship-3 shape evolution if devices carry subject UUIDs by ID; certainly Ship-4 case management)
**Severity**: A — touches ADR-002 §S7 and §S8 structural constraints; resolution may require ADR-002-R supersession

### Context

ADR-002 §S7 and §S8 are individually consistent but produce an under-specified seam in the **corrective-split** case (§S7's prescribed remedy for a wrong merge):

- [§S7](adrs/adr-002-identity-conflict.md#s7-no-subjectsunmerged--wrong-merges-use-corrective-split): *"Post-merge events that were recorded against the surviving ID remain attributed to the surviving subject by default. Manual re-attribution... is **optional, not required**."*
- [§S8](adrs/adr-002-identity-conflict.md#s8-split-freezes-history-source-is-permanently-archived): *"A `SubjectSplit` event archives the source subject (terminal lifecycle state). All historical events remain attributed to the source_id."*

In the corrective-split case, the surviving subject **becomes** the split source. Pre-split captures against the surviving subject (good-faith captures during the wrong-merge window) are now attributed to an **archived** identity. §S7's "optional re-attribution" promise was implicitly conditioned on the surviving subject staying *active*; the corrective-split path voids that condition. The captures are correctly preserved (immutability + accept-and-flag), but the projection-layer surface and the operator workflow consequences are unspecified:

1. Does the read path show those captures under the archived source's projection (which has no living state) or under one of the successors (which violates §S8 "historical events remain attributed to the source_id")?
2. Successors emerge with empty projection state (no name, no village, no demographics) until fresh captures are authored against them. What does a coordinator do *operationally* immediately after a corrective split? Is bootstrap-capture a workflow obligation or a UI affordance?
3. If field workers were holding offline captures referencing the (now archived) surviving subject when the corrective split happened, those captures sync against the archived ID. §S14 says accept-and-flag — but §S7's "by default" attribution promise is no longer meaningful because there is no living "default" subject to attribute to.

This is **intra-ADR-002 tension**, not cross-ADR conflict. ADR-007/008/009 do not contradict ADR-002 here; the tension is inside ADR-002 between two adjacent positions that were never exercised together until Ship-2's W-4.

### Trigger

Any of the following lifts this item to `BLOCKS`:

1. Any Ship spec that introduces a device flow capturing events against existing subjects by UUID reference (not Ship-1's fresh-`subject_id`-per-capture pattern). Likely Ship-3 if shape evolution introduces UUID-referenced flows; certainly Ship-4 case management.
2. A Ship-2-or-later retro that observes a real coordinator workflow case where the empty-successor consequence breaks operator trust or produces a data-quality incident.
3. Any read-path implementation work that must answer question (1) above for projection rendering.

### Gate

All of the following must be true:

1. The seam is resolved by one of: (a) ADR-002-R supersession that explicitly addresses the corrective-split case (read-path attribution rule + workflow obligations), (b) a documented strategy-level position recorded in the ledger that does not require ADR change because the resolution turns out to be implementation-grade (with proof that no §S is altered), or (c) a new ADR addressing the post-corrective-split workflow surface (case management / re-attribution UI).
2. A test exists that exercises the offending sequence: wrong merge → corrective split → offline capture pre-archive → sync → read-path resolution. The test asserts the read-path behaviour matches the resolved position from (1), not whatever the implementation happens to do.
3. Operator workflow for empty-successor bootstrap is documented (if (1)(c) was chosen) or explicitly declared out-of-scope-for-now with the next-Ship gate named.

### Resolution log

- **2026-04-25**: Opened by Ship-2 spec partner-mode review. Ship-2 observes the empty-successor invariant in W-4 (§6.4) but does not stress S7↔S8 because Ship-1's CHV flow generates fresh `subject_id` per capture and never references existing subjects by UUID — the offline-capture-against-now-archived-source path is not constructible in the current device flow. Carried forward; not closed by Ship-2.
- **2026-04-26** (Ship-2 close): status check — **stays OPEN**. Ship-2's W-4 (`Ship2WalkthroughAcceptanceTest#W4_*`) confirmed the empty-successor invariant operationally (post-split projection of either successor returns no payload-derived state; original capture stays attributed to the archived source). The offline-capture-against-archived-source path remains structurally unconstructible in Ship-1's CHV flow, so the seam is not exercised. Carries to Ship-3 (if shape evolution introduces UUID-referenced flows) or Ship-4 (case management) at the latest. No ADR-002 §S re-decided.

---

## FP-007 — Contract↔server-resource shape drift not enforced

**Status**: RESOLVED
**Opened**: 2026-04-25 by Ship-2 spec partner-mode review (pre-build close-out)
**Blocks**: any Ship that edits shapes or the envelope (Ship-2 onward)
**Severity**: B — projection of contracts into the server runtime; same failure class as FP-003 (envelope parity), one layer over

### Context

Two parallel shape directories exist as independently-maintained copies: `contracts/shapes/*.schema.json` (the language-neutral contract source) and `server/src/main/resources/schemas/shapes/*.schema.json` (the server runtime bundle loaded by `ShapePayloadValidator`). The two trees are byte-identical today (verified 2026-04-25, `diff -r contracts/shapes server/src/main/resources/schemas/shapes` → empty), but parity is maintained by manual copy convention. **No drift gate, no build step, no CI check enforces the copy.** A single-sided edit during any future Ship will silently diverge the contract source from the runtime bundle — the validator will accept payloads that violate the contract, or reject payloads the contract permits, without any signal.

This is the same failure class FP-003 closed for `envelope.schema.json` (single-file parity test), one layer over: shapes are a multi-file tree, and the parity test at FP-003 does not extend to it.

### Trigger

Ship-2 is the first Ship that mutates a shape (`subject_split.schema.json` arity edit, §6 commitment 4). Every Ship from here forward that touches shapes inherits the silent-drift risk until the gate closes.

### Gate

One of the following must be true, and the chosen mechanism must run on the developer build path (not just CI), so the drift cannot reach a commit:

1. **(a)** Drift-gate diff check (`diff -r contracts/shapes server/src/main/resources/schemas/shapes`) added to `scripts/check-convergence.sh`, with the script running as part of pre-merge verification (existing convergence gate already runs on every Ship close).
2. **(b)** Build-time copy at Maven `generate-resources` phase (or equivalent), deleting the duplicated server tree; `contracts/shapes/` becomes the single source.
3. **(c)** Ship-2 spec §6 declares the manual-copy convention and the coding agent edits both folders in the same commit. **Interim discipline only** — does not close the FP, only documents the discipline pending (a) or (b).

Resolution path — **chosen 2026-04-25 → (a)** at Ship-2 OQ-4. The actual `scripts/check-convergence.sh` edit lands in Ship-2's first build commit alongside the `subject_split.schema.json` arity edit so the gate is in place before the schema diverges. FP-007 closes at Ship-2 retro when the gate is observed PASS at Ship-2 close.

### Resolution log

- **2026-04-25**: Opened by Ship-2 spec close-out review. Byte-identity verified at open time.
- **2026-04-25**: Path (a) chosen at Ship-2 OQ-4. Gate implementation deferred to Ship-2's first build commit. Closure pending Ship-2 retro.
- **2026-04-26** (Ship-2 close): **RESOLVED**. Drift-gate check 4 (`contracts/shapes` ↔ `server/src/main/resources/schemas/shapes` parity) landed at commit [`5cbb183`](../scripts/check-convergence.sh) (the first build commit of Ship-2's range, ahead of the `subject_split` arity edit at `f7f0e8a` / `48049e2`). Gate observed PASS at Ship-2 close; the two trees are byte-identical (Ship-2 mutated both in lock-step under the new gate). The gate runs on every Ship close from here forward.

---

## FP-008 — `conflict_detected` payload lacks root_cause trace metadata

**Status**: OPEN
**Opened**: 2026-04-25 by Ship-2 spec partner-mode review (against phase-3 classification archive)
**Blocks**: the first Ship that emits a `conflict_detected/v1` event whose source-of-badness is distinct from `source_event_id` — likely Ship-3 if shape evolution introduces UUID-referenced flows, certainly Ship-4 case management; structurally required by Ship-5 batch resolution. **Does not block Ship-2** (Ship-2 emits no flag categories where `source_event_id != trigger`).
**Severity**: B — observability / resolution-pipeline metadata; non-breaking to add later, and the early flag corpus does not accrue trace-less flags (Ship-1 + Ship-2 emit only categories where `source_event_id` IS the trigger)

### Context

`contracts/shapes/conflict_detected.schema.json` defines payload fields `{source_event_id, flag_category, resolvability, designated_resolver, reason}`. There is no `root_cause`, `trigger_type`, or `trigger_event_id` field. Phase-3 classification (`docs/exploration/archive/09-adr2-phase3-classification-results.md`, Bucket 2 items A3/M7) called for structured root-cause metadata to enable batch resolution; this was deferred at the time as "strategy" and never propagated into ADR-007 or ADR-008 or the shape schema.

For Ship-1's two flag categories (`scope_violation`, `identity_conflict`), `source_event_id` alone is sufficient — the trigger **is** the source event (the offending capture is what got detected). For Ship-2 onward, this stops being true. Stale-reference flags (charter §Flag catalog #2) can be caused by a **merge or split event distinct from the offending capture**: a CHV captures against subject `S_X` while online, then a coordinator merges `S_X → S_Y`, then the next sync re-evaluates the existing capture and flags it. `source_event_id` names the bad event but not the identity-evolution event that **caused** the badness. Ship-5's batch-resolution by root cause ("approve all flags caused by merge M") is structurally blocked without this metadata.

### Mitigations in place

`additionalProperties: true` on the schema means optional fields can be added later without breaking existing validators or persisted events — the addition is non-breaking at the schema level. The cost of deferral is that the **early flag corpus** (any flag emitted before the field lands) lacks the trace, so historical batch-resolution would have to fall back to category-level grouping for that corpus.

### Trigger

Either:

1. Ship-5 spec opens (judgment / batch resolution is in scope) — at that point the field is load-bearing and must exist before any Ship-5 walkthrough.
2. Ship-2-or-later retro records that diagnostic quality on a stale-reference flag was insufficient to debug a real case — promotes the FP from "Ship-5 only" to "next Ship after observation".

### Gate

All of the following must be true:

1. `conflict_detected/v1` payload schema carries an optional `root_cause` (or `trigger_type` + `trigger_event_id`) field set, with documented semantics for which flag categories require it (e.g., stale-reference: required; scope/identity: optional).
2. Server emission code populates the field for every flag category whose source-of-badness is distinct from the source event itself.
3. A test asserts that a stale-reference flag emitted in response to a merge/split event carries the merge/split event's UUID as `trigger_event_id`.
4. Ledger row for `conflict_detected/v1` records the schema delta and classification (STABLE remains; field addition is non-breaking).

Resolution path — **chosen 2026-04-25 → (c)** at Ship-2 OQ-5:

- **(a)** ~~Add the field in Ship-2.~~ **Rejected**: lands a schema field with no Ship-2 emission site that populates it (§3.1 R3 — stale-reference flags not observable in Ship-2's slice). Speculative engineering; `additionalProperties: true` makes deferral free.
- **(b)** ~~Defer to Ship-5.~~ **Rejected**: Ship-3/4 emits stale-reference flags first, which is the producer side. Ship-5 consumes. Add the field at the producing Ship, not the consuming Ship.
- **(c) — CHOSEN.** Defer to the first Ship that emits a `conflict_detected/v1` flag whose source-of-badness is distinct from `source_event_id`. That Ship adds the schema field, populates it on emission, and lands the walkthrough asserting `trigger_event_id`. Cost of deferral past Ship-2 is zero in practice — Ship-1's two flag categories and Ship-2's emissions all have `source_event_id == trigger`, so the early flag corpus is not trace-less.

### Resolution log

- **2026-04-25**: Opened by Ship-2 spec close-out review.
- **2026-04-25**: Path (c) chosen at Ship-2 OQ-5. `Blocks:` field rewritten to name the producing Ship as the gate. Stays OPEN; closure is the first Ship that emits a stale-reference (or other non-self-trigger) flag.
- **2026-04-26** (Ship-2 close): status check — **stays OPEN per path (c)**. Ship-2 emitted no `conflict_detected/v1` events whose `source_event_id` differs from the trigger event (the only flags in Ship-2's corpus continue to be the Ship-1 categories `scope_violation` and `identity_conflict`, both self-triggering). The early flag corpus stays trace-clean. Carries to Ship-3 / Ship-4 per OQ-5.

---

## FP-009 — `ConflictDetector` field-name coupling

**Status**: RESOLVED
**Opened**: 2026-04-27 by Ship-3 spec lock (Cleaving A)
**Blocks**: Ship-3 close (closure asserted at retro via W-6)
**Severity**: B — Ship-1 expedient that becomes load-bearing the moment a shape change touches identity-key fields

### Context

[`server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java`](../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) hard-codes the payload field-name lookups (`village_ref` and `household_name`) inside `detect(...)` for the two Ship-1 detectors:

```java
UUID villageRef     = optUuid(capture.payload().path("village_ref").asText(null));
String householdName = normalize(capture.payload().path("household_name").asText(""));
```

`village_ref` and `household_name` are literal string keys in the `household_observation/v1` payload. There is no shape-declared uniqueness rule (Q7a / [ADR-004 §S14](adrs/adr-004-configuration-boundary.md#s14-deployer-parameterized-policies)) feeding the detector — the coupling is a Ship-1 expedient. Ship-3 evolves `household_observation` to v2, which is the first time this coupling can break by mistake.

### Trigger

Already triggered by Ship-3 spec opening. Resolution work happens *inside* Ship-3 (negative form: prove the coupling does not break under v2).

### Gate

Both:

1. Ship-3 spec [§6 sub-decision 2](ships/ship-3.md#61-sub-decisions) forbids removal or rename of `village_ref` and `household_name` in v2. The v2 schema preserves both field names verbatim.
2. Ship-3 W-6 (additive happy path) asserts the detector continues to fire on duplicate-household across mixed v1/v2 events. Closes at Ship-3 tag if W-6 passes.

### Trigger if not closed

Any future shape change that touches identity-key fields (rename, type change, removal) reopens FP-009 with mandatory shape-declared-uniqueness work (Q7a). That work folds into [FP-012](#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) gate (b)/(c) — the deployer-authoring surface is where uniqueness rules become declarable, and the detector becomes shape-driven rather than field-name-coupled.

### Resolution log

- **2026-04-27**: Opened by Ship-3 spec §6 lock. Closure deferred to Ship-3 retro.
- **2026-04-27** (Ship-3 close): **RESOLVED**. Gate met by mechanical inspection at Ship-3 HEAD:
  1. *`ConflictDetector` unchanged across Ship-3*: `git diff ship-2..ship-3 -- server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java` returns zero lines. The `detect(...)` payload field-name reads on `village_ref` and `household_name` are the literal Ship-1 strings, untouched.
  2. *W-6 passes against a mixed v1+v2 corpus*: [`Ship3WalkthroughAcceptanceTest#walkthrough_W6_*`](../server/src/test/java/dev/datarun/ship1/acceptance/Ship3WalkthroughAcceptanceTest.java) drives the v1-current direction (a v1 capture whose prior duplicate was recorded under v2) and observes `identity_conflict`. The lookup continues to function across the v1/v2 boundary because v2 preserves both field names verbatim per [ship-3 §6.1 sub-decision 2](ships/ship-3.md#61-sub-decisions).

  **Asymmetry note** (recorded so a future agent reads it as a known property, not a regression): the v2-current direction of mixed-version detection is *not* asserted by W-6. The detector's entry guard at [`ConflictDetector.java`](../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) line 35 — `HOUSEHOLD_SHAPE = "household_observation/v1"` — pins activation to v1-current captures; a v2-current capture whose prior duplicate was recorded under v1 (or v2) does not currently re-enter the detector. The **field-name-coupling concern** that opened FP-009 is closed by this resolution (identity-key field names are preserved across v1/v2). The **version-pinning expedient** is a deeper FP-009-shaped surface that does *not* require a new FP because it folds into [FP-012](#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) gate (b)/(c) — the deployer-authoring surface is where shape-declared uniqueness rules become declarable and the detector becomes shape-driven rather than version-pinned.

---

## FP-010 — Cross-version projection composition contract

**Status**: OPEN
**Opened**: 2026-04-27 by Ship-3 spec lock (Cleaving A)
**Blocks**: the first Ship that needs a breaking shape change OR a cross-version aggregation in the admin/projection layer beyond per-event routing on `shape_ref`
**Severity**: B — projection-composition contract is unspecified for non-additive evolution and for multi-version aggregation

### Context

[ADR-004 §S10](adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) commits to additive evolution + deprecation + breaking changes (the "exceptional path" with "explicit deployer acknowledgment and server-side migration support"). [ADR-004 §S1](adrs/adr-004-configuration-boundary.md#s1-shape-reference-in-envelope) ensures events are self-describing. *How* the projection engine composes a multi-version stream — beyond the trivial per-event routing on `shape_ref` that Ship-3 W-6 / W-10 exercises — is not specified. ADR-004's "What This Does NOT Decide" table explicitly classifies "Projection merge strategy across schema versions" as Implementation, but Ship-3's scope deliberately excludes both breaking changes and cross-version aggregation; the contract surfaces only when a Ship needs them.

Carries forward through Ship-3 (the additive-only slice does not exercise it). The next Ship that lands a breaking change or admin/projection logic that aggregates across `shape_ref` versions is the trigger.

### Trigger

Either:

1. A Ship spec opens whose scenarios require a breaking shape change (field removal, type change) — [ADR-004 §S10](adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) "exceptional path."
2. An admin/projection or analytics requirement lands that must aggregate over a multi-version event corpus and present a unified view (e.g., a coordinator dashboard summing across v1+v2 captures with non-trivial field reconciliation).
3. A retro observes that per-event routing on `shape_ref` is insufficient for an operator-facing read path.

### Gate

All three must be true:

1. Cross-version projection composition contract is specified (either as a successor ADR position, or as a concretely-decided implementation pattern recorded in the ledger with proof that no §S is altered) — covering: per-event routing (Ship-3 baseline), field-reconciliation across versions for aggregations, and breaking-change migration semantics (§S10 exceptional path).
2. A test exists that exercises a v1 + v2 (and ideally v1 + v2 + breaking-vN) stream through the chosen composition, asserting fidelity for each version and the documented merge semantics for aggregations.
3. Admin read-path code branches on `shape_ref` (never on envelope `type` — F-A2) and the contract is reflected in inline comments or ledger row.

### Resolution log

- **2026-04-27**: Opened by Ship-3 spec §6 lock. Carried through Ship-3; not exercised by the additive-only slice.

---

## FP-011 — `household_observation` directory classification (re-deferral)

**Status**: OPEN (re-deferred)
**Opened**: 2026-04-27 by Ship-3 spec lock (Cleaving A) — surfaced under F-C1
**Blocks**: same Ship that closes [FP-012](#fp-012--deployer-authoring-surface-for-shapestriggerspolicies)
**Severity**: C — classification hygiene; no functional drift today

### Context

[`contracts/shapes/household_observation.schema.json`](../contracts/shapes/household_observation.schema.json) is described in its own header as a "Ship-1 deployer shape." Per [ADR-009 §S1](adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) duality rule, deployer-authored shape *instances* are CONFIG, not platform PRIMITIVES. `household_observation` therefore should not live in the same directory as platform-bundled shapes (`subjects_merged/v1`, `subject_split/v1`, `assignment_created/v1`, `assignment_ended/v1`, `conflict_detected/v1`, `conflict_resolved/v1`). This is a [F-C1](adrs/adr-009-platform-fixed-vs-deployer-configured.md) violation: the directory layout conflates mechanism and instance.

### Why re-defer rather than fix now

Resolving the directory split today is restructuring without load. The split only becomes operationally meaningful when deployer-CONFIG shapes are *first* persisted outside the JAR-bundled fixture — which is the scope of [FP-012](#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) (deployer-authoring surface). Splitting the directories before then would introduce two paths (`contracts/shapes/platform/` and `contracts/shapes/deployer/` or equivalent) that the runtime has no reason to distinguish, plus a second copy under `server/src/main/resources/schemas/shapes/` that [FP-007](#fp-007--contractserver-resource-shape-drift-not-enforced)'s drift-gate must continue to police. Net effect: motion without traction.

### Trigger

[FP-012](#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) trigger fires (whichever sub-trigger lands first).

### Gate

Both:

1. Directory split lands in the same Ship that closes FP-012: deployer-CONFIG shapes (`household_observation` and any siblings) are persisted in the deployer-authoring surface (DB or filesystem); platform-bundled shapes remain in the `contracts/shapes/` tree (or a `contracts/shapes/platform/` subdirectory).
2. FP-007's drift-gate is updated in lock-step to police the new directory layout — no schema can land in only one tree.

### Resolution log

- **2026-04-27**: Opened by Ship-3 §6 sub-decision 3 lock. Re-deferred per gate above; the JAR-bundled fixture continues one Ship as a named expedient.

---

## FP-012 — Deployer-authoring surface for shapes/triggers/policies

**Status**: OPEN
**Opened**: 2026-04-27 by Ship-3 spec lock (Cleaving A)
**Blocks**: the first Ship matching any of the three triggers below
**Severity**: A — [ADR-004 §S6](adrs/adr-004-configuration-boundary.md#s6-atomic-configuration-delivery) / [§S10](adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) / [§S13](adrs/adr-004-configuration-boundary.md#s13-complexity-budgets) / [§S14](adrs/adr-004-configuration-boundary.md#s14-deployer-parameterized-policies) architecture is decided; the build is not

### Context

[ADR-004](adrs/adr-004-configuration-boundary.md) commits the platform to a deployer-authored shape/trigger/policy surface: shapes versioned via `shape_ref` (§S1, §S10), atomic configuration delivery to devices with at-most-2-version coexistence (§S6), deploy-time validation enforcing complexity budgets (§S13), deployer-parameterized policies — flag severity, domain uniqueness (Q7a), scope composition, sensitivity classification Q12 (§S14). Ship-1 implemented the runtime *enough* for one shape; Ship-3 ([§6 sub-decision 3](ships/ship-3.md#61-sub-decisions)) extends the JAR-bundled fixture by one Ship as a named expedient. **The deployer-authoring surface itself — REST endpoint, file format, validation pipeline, atomic-package format, on-device application semantics — has never been built.** Ship-3's [§3.2 DR-2](ships/ship-3.md#32-domain-realism-risks) records the trigger evidence: walkthroughs cannot exercise runtime authoring because no such path exists, and §S13 budget enforcement consequently lands as a unit test rather than HTTP.

### Trigger (whichever fires first)

1. **Non-fixture shape required.** A Ship spec opens whose scenarios require a shape that the JAR-bundled fixture cannot ship — either because deployer-specific values are needed at deploy time (multi-tenant) or because the shape is authored by a non-engineering deployer.
2. **Q7a or Q12 declaration required.** A Ship spec opens whose scenarios require shape-declared uniqueness rules (Q7a / [ADR-004 §S14](adrs/adr-004-configuration-boundary.md#s14-deployer-parameterized-policies)) or shape/activity-level sensitivity classification (Q12 / §S14) — the validator cannot honor declarations that have nowhere to be declared.
3. **Deployer onboarding owned.** A Ship spec opens whose scenarios cover deployer onboarding (S22-class scenarios — naming, parameterization, package upload, deploy-time validation feedback).

### Gate (all required at close)

1. **(a) Shape registry persisted outside the JAR.** DB table or deployer-controlled filesystem path (deployer's choice), separate from `server/src/main/resources/schemas/shapes/`. Platform-bundled shapes may continue in the JAR; deployer-CONFIG shapes do not.
2. **(b) Admin endpoint accepts shape definition** with deploy-time validation per [§S13](adrs/adr-004-configuration-boundary.md#s13-complexity-budgets) (60-field budget; predicate / trigger budgets; naming rules; change classification per [§S10](adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) — additive / deprecation / breaking).
3. **(c) Q7a uniqueness rules + Q12 sensitivity declarations** authored on the shape are honored by the validator and by downstream detectors. Specifically: `ConflictDetector`'s identity-key lookup (FP-009) becomes shape-declared rather than field-name-coupled.
4. **(d) Atomic package delivery** per [§S6](adrs/adr-004-configuration-boundary.md#s6-atomic-configuration-delivery) with the wire-versioning scheme from [FP-013](#fp-013--config-package-wire-versioning-scheme) — at-most-2-version on-device coexistence; in-progress work under previous version completes before new version applies.
5. **(e) Walkthrough.** A coordinator-role actor registers v3 of a shape via HTTP; two devices receive the new package atomically; the cascade-table per [`docs/exploration/file-15`](exploration/) Check (b) blocks an invalid breaking change at the validator (test asserts the rejection, not the accept).
6. **(f) [FP-011](#fp-011--household_observation-directory-classification-re-deferral) directory split lands alongside.** Platform-bundled vs deployer-CONFIG directory separation is part of this Ship, not the next.

### Resolution log

- **2026-04-27**: Opened by Ship-3 §6 sub-decision 3 lock. Ship-1 + Ship-3 use the JAR-bundled fixture as a named expedient; the architecture (ADR-004 §S6/§S10/§S13/§S14) is not under question — the build is.

---

## FP-013 — Config-package wire-versioning scheme

**Status**: OPEN
**Opened**: 2026-04-27 by Ship-3 spec lock (Cleaving A)
**Blocks**: same Ship as [FP-012](#fp-012--deployer-authoring-surface-for-shapestriggerspolicies); delivery of a config package to a real device is the first time wire format is load-bearing
**Severity**: B — [ADR-004 §S6](adrs/adr-004-configuration-boundary.md#s6-atomic-configuration-delivery) commits atomicity but not wire format

### Context

[ADR-004 §S6](adrs/adr-004-configuration-boundary.md#s6-atomic-configuration-delivery) commits: *"Configuration is delivered to devices as an atomic package at sync time. The device applies the new configuration only after in-progress work under the previous configuration completes. At most two configuration versions coexist on-device: current and previous."* What the *wire format* of an atomic package is — package version field, monotonic ordering rule, signature/integrity, fragment/page boundaries for large packages, on-device persistence schema for the `current/previous` pair — is not specified. The file-15 action item from the convergence-phase ADR-writing session 4 to specify this scheme never landed; the gap is currently masked by Ship-3 simulating §S6 with two HTTP devices on different `shape_ref` values rather than delivering an actual config bundle.

### Trigger

[FP-012](#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) trigger fires. Wire format becomes load-bearing the moment a real device receives a non-fixture config package.

### Gate

All three must be true:

1. Wire-versioning scheme specified — at minimum: package version field (monotonic), payload (shape definitions, trigger definitions, policy values), atomic-application rule (`current` / `previous` slots; in-flight work pinned to the slot it started under), validation contract (server-rejects-malformed; device-rejects-non-monotonic).
2. Scheme tested in the same Ship as FP-012 — a walkthrough delivers package vN+1 to a device that has package vN, asserts at-most-2-version coexistence, asserts in-progress work under vN completes before vN+1 applies.
3. Scheme reflected in a contract artifact under `contracts/` (analogous to [`contracts/sync-protocol.md`](../contracts/sync-protocol.md)) so future Ships have a settled cite, not a buried implementation detail.

### Resolution log

- **2026-04-27**: Opened by Ship-3 spec §6 sub-decision 3 lock. Ship-3 simulates §S6 atomicity at the HTTP layer (two devices on different `shape_ref` values) without exercising package wire format; FP-013 records the gap.

---

## FP-014 — Scope-eval pull-class temporal-anchor disambiguation

**Status**: OPEN
**Opened**: 2026-04-27 by Ship-3 closeout (Wave 2-A)
**Blocks**: Ship-4 (S04 corrections / S08 case-management opens — both depend on historical event access)
**Severity**: A — touches [ADR-003 §S2](adrs/adr-003-authorization-sync.md#s2) ("sync scope = access scope"); ADR position is under-specified for pull-class

### Context

The push path already honors event-time semantics: [`ConflictDetector.scope_violation`](../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) at line 67 evaluates the actor's scope at `capture.timestamp()` (event-time, [ADR-003 §S3](adrs/adr-003-authorization-sync.md#s3) compliant; verified 2026-04-27 by Wave-1 G-7' — this is the correct anchor and the substrate is clean).

The pull path uses request-time. [`SyncController.pull`](../server/src/main/java/dev/datarun/ship1/sync/SyncController.java) at line 114 calls `OffsetDateTime.now()`; [`ConfigController`](../server/src/main/java/dev/datarun/ship1/config/ConfigController.java) at line 44 does the same. ADR-003 §S2 commits "sync scope = access scope" but never disambiguates *which time* anchors the scope evaluation on a pull. Three operational pull-classes have different correct anchors, and the platform conflates them:

- **Live-sync pull** (the device-online steady state). A CHV reassigned away from V1 must NOT receive new V1 events on subsequent live-sync pulls. **Request-time scope is correct.** Domain anchor: [`access-control-scenario.md`](access-control-scenario.md) "Access can be temporary... When the reason for temporary access ends, the expanded access should end too."
- **Historical / audit pull** (an auditor reconstructing what happened). The auditor must see the event corpus that the actor's then-current scope authorized at event-time. **Event-time scope is correct.** Domain anchor: same scenario, "every action attributable to a specific person acting in a specific role at a specific time" — audit ⇒ event-time reconstruction.
- **Case-bound pull** ([S08](scenarios/08-case-management.md) case management — a case spans an actor reassignment). A CHV who picks up a case mid-stream must see the full case history. **Subject-anchored scope (the case's persisting `subject_ref`), not actor-time-anchored, is correct.**

The reviews' first-cut prescription ("flip pull to event-time") would break live-sync correctness. The right resolution is **disambiguation by pull-class**, encoded in ADR-003 (or a successor ADR-003-R) before any code change. The push-path cleanness (`capture.timestamp()`) MUST be preserved — push-path correctness is not under question.

### Trigger

Ship-4 spec opening (S04 corrections introduce historical-pull need; S08 case-management introduces case-bound pull). Whichever Ship-4 scope crystallises first.

### Gate

All required:

1. ADR-003-R (or a documented strategy-level position with proof no §S is altered) disambiguates the temporal anchor by pull-class: live-sync = request-time, historical/audit = event-time, case-bound = subject-anchored. Each pull endpoint's class is named explicitly.
2. [`SyncController.pull`](../server/src/main/java/dev/datarun/ship1/sync/SyncController.java) and [`ConfigController`](../server/src/main/java/dev/datarun/ship1/config/ConfigController.java) paths are routed by pull-class; tests assert each anchor independently. The push-path cleanness ([`ConflictDetector.java`](../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) line 67, `capture.timestamp()`) is preserved verbatim.
3. The flag-detection asymmetry between push and pull is reconciled: a capture accepted under push-time projection must remain visible under any pull-class that includes the event under that pull's anchor semantics.
4. Walkthrough: actor reassignment + event before reassignment + (live-sync pull post-reassignment ⇒ event excluded) + (audit pull post-reassignment ⇒ event included). Both must pass.

### Resolution log

- **2026-04-27** (Ship-3 closeout Wave 2-A): Opened. Promoted from Ship-1 retro §3.3 observation that lay un-FP'd for 3 Ships (carried forward as live observation per Rule R-1 candidate-violation; documented as silent-deferral failure in Wave 2-B retro §5). Push-path cleanness confirmed via Wave-1 G-7' ([`ConflictDetector.java`](../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) line 67 uses `capture.timestamp()`). Pull-path request-time confirmed at [`SyncController.java`](../server/src/main/java/dev/datarun/ship1/sync/SyncController.java) line 114 and [`ConfigController.java`](../server/src/main/java/dev/datarun/ship1/config/ConfigController.java) line 44.

---

## FP-016 — Fixture-event schema regression check (drift-gate scope expansion)

**Status**: OPEN
**Opened**: 2026-04-27 by Ship-3 closeout (Wave 2-A) — promoted from SC-08 / C2-04 reviewer findings
**Blocks**: any Ship that introduces a non-additive shape change ([ADR-004 §S10](adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) exceptional path) — likely Ship-N where N > 4 unless an earlier Ship needs it
**Severity**: B — drift-gate observability gap

### Context

[`scripts/check-convergence.sh`](../scripts/check-convergence.sh) validates byte-identity between [`contracts/shapes/`](../contracts/shapes/) and [`server/src/main/resources/schemas/shapes/`](../server/src/main/resources/schemas/shapes/) ([FP-007](#fp-007--contractserver-resource-shape-drift-not-enforced) close-out, drift-check #4). This catches *file-level* divergence between the two trees.

It does NOT catch *behavioral* schema regression: a schema edit that weakens a constraint (drops a required field, broadens an enum, makes an optional field required) and is mirrored in both trees passes the drift gate but breaks fixture events that were valid under the prior schema.

[ADR-004 §S10](adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) commits to additive evolution as the default; non-additive changes are the "exceptional path" with explicit acknowledgment. A regression check against persisted fixture events is the concrete test that the exceptional path is in fact exceptional.

### Trigger

Any Ship that proposes a non-additive shape change OR Ship-4 if its scenarios introduce shape edits that any reviewer flags as potentially non-additive.

### Gate

All required:

1. A fixture set of representative events per shape lives in [`contracts/fixtures/`](../contracts/fixtures/) (or extend the existing directory). Each fixture is a valid event under the current shape version at the time it was added; the file carries the shape version it was authored against.
2. A regression check (test or `check-convergence.sh` step 5) loads each fixture and validates against the *current* schema for the same shape version. Pass = backward-compatible. Fail = behavioral regression detected; the change is non-additive and requires the §S10 exceptional-path rationale.
3. Drift-gate scope statement (header comment in [`scripts/check-convergence.sh`](../scripts/check-convergence.sh)) explicitly references this gate as the second half of "behavioral conformance."

### Resolution log

- **2026-04-27**: Opened by Ship-3 closeout Wave 2-A.

---

## FP-017 — `role_stale` detector wiring (successor to FP-001)

**Status**: OPEN
**Opened**: 2026-04-27 by Ship-3 closeout (Wave 2-A) — successor to [FP-001](#fp-001--role_stale-projection-derived-role-verification)
**Blocks**: the first Ship that requires role-action enforcement (Ship-5 territory under the current map: judgment / approvals / multi-step review per [S04](scenarios/04-supervisor-review.md) + S11)
**Severity**: A — [ADR-003 §S3](adrs/adr-003-authorization-sync.md#s3) (authority-as-projection) requires the substrate to exist before role-keyed detection lands

### Context

[`ScopeResolver.hasRoleAt(actorId, role, at)`](../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) exists (Ship-2 commit `ecf3ece`) and replays `assignment_changed` events, but has **no callers**. There is no `role_stale` path in [`ConflictDetector`](../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) — the pre-convergence detector was discarded at Ship-1 rebuild. The platform supports the invariant *structurally* (event-replay machinery + projection-time anchor on `scope_violation`) but the *role-keyed* detector itself is unbuilt. [ADR-006 §S2](adrs/adr-006-flag-semantics.md) catalog row 5 (`role_stale`, `manual_only`) carries a contract for a detector that does not exist. R-7 (§S–implementation parity, see [Rule R-7](#rule-r-7-simplementation-parity)) would have caught this at Ship-1.

### Trigger

Any Ship spec whose scenarios require role-action enforcement (a CHV cannot review their own work; only a supervisor can approve; etc.). Specifically: [S04](scenarios/04-supervisor-review.md) supervisor review's reviewer-authority check, S11 multi-step approval chain, and any future Ship that asserts "actor X cannot perform action Y because they no longer hold role R".

### Gate

All required:

1. `role_stale` detector wired into [`ConflictDetector`](../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) push path, calling `ScopeResolver.hasRoleAt(actorId, requiredRole, capture.timestamp())`. Anchor MUST be `capture.timestamp()` (projection-time, [ADR-003 §S3](adrs/adr-003-authorization-sync.md#s3)) — same precedent as `scope_violation`. **No** `Instant.now()` and **no** cache.
2. Detector emits `conflict_detected/v1` with `payload.flag_category = "role_stale"`, `actor_ref = "system:conflict_detector/role_stale"` ([ADR-008 §S2](adrs/adr-008-envelope-reference-fields.md) system-author format).
3. Test analogous to `ScopeViolationTemporalDivergenceTest`: actor A holds role X at T1; admin changes role to Y at T2; capture at T_C with T1 < T_C < T2 (offline, late push) MUST NOT fire `role_stale`; capture at T3 > T2 under role X (now stale) MUST fire `role_stale`. Forcing property documented inline.
4. [FP-018](#fp-018--assignment_endedv1-validated-but-never-consumed-in-scope-reconstruction) (`assignment_ended/v1` consumption) must be resolved before this gate, OR the test must construct role transitions via the same workaround Wave-1 G-7' used (`valid_to` set on the original `assignment_created/v1` event). The dependency is recorded explicitly in this gate so Ship-5 doesn't carry both at once.

### Resolution log

- **2026-04-27**: Opened by Ship-3 closeout Wave 2-A. Successor to [FP-001](#fp-001--role_stale-projection-derived-role-verification).

---

## FP-018 — `assignment_ended/v1` validated but never consumed in scope reconstruction

**Status**: OPEN
**Opened**: 2026-04-27 by Wave-1 G-7' code-reading finding (commit `8be0ae2`)
**Blocks**: the first Ship requiring operational end-of-assignment (assignment ends as a separate event, not a pre-set `valid_to` on creation). Likely Ship-4 (S04 supervisor reassignment scenarios) or Ship-5 ([FP-017](#fp-017--role_stale-detector-wiring-successor-to-fp-001) role-action enforcement).
**Severity**: A — domain-correctness gap; [P04](behavioral_patterns.md) (Responsibility Binding) variation "Whether responsibility... can be delegated or transferred" requires this path

### Context

[`contracts/shapes/assignment_ended.schema.json`](../contracts/shapes/assignment_ended.schema.json) exists and validates events of `shape_ref = "assignment_ended/v1"`. Such events are accepted by the push path and stored. [`ScopeResolver.activeAssignments(actorId, at)`](../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) at line 53 iterates **only** `findByShapeRefPrefix("assignment_created/")`. `assignment_ended/v1` events are never read in scope reconstruction.

Practical consequence: the only way to end an assignment in current code is to pre-set `valid_to` on the original `assignment_created/v1` event. But [ADR-001 §S1](adrs/adr-001-offline-data-model.md#s1) is append-only — modifying the original event is structurally forbidden. Tests construct assignments with `valid_to` pre-populated; production has no append-only path that effectively ends an assignment.

Domain anchor: [`access-control-scenario.md`](access-control-scenario.md) "Access can be temporary... When the reason for temporary access ends, the expanded access should end too. But everything done during that period remains on record." Append-only + temporary-access compose only if `assignment_ended/v1` is honored as the end-event.

Why this wasn't caught earlier: [S03](scenarios/03-user-based-assignment.md) (assignment) walkthroughs at Ship-1 covered creation only; Ship-2 added `hasRoleAt` (caller-less); Ship-3 was shape-evolution. No Ship has yet exercised "actor's authority ends, late offline event arrives".

### Trigger

Any Ship spec with a scenario that ends an assignment as a discrete event (not a pre-planned `valid_to`).

### Gate

All required:

1. [`ScopeResolver`](../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) reads both `assignment_created/v1` AND `assignment_ended/v1` events. The composition rule for "end" is documented: an `assignment_ended/v1` event with `target.actor_id = A`, `scope_ref = S`, `at = T_end` removes `(A, S)` from the active set for any `at > T_end`.
2. Test: create assignment of A→V1 at T0; emit `assignment_ended/v1` at T_end; assert `activeGeographicScopes(A, T < T_end)` includes V1; `activeGeographicScopes(A, T > T_end)` excludes V1.
3. Offline-late case: an `assignment_ended/v1` event arrives via push at wall-clock W > T_end. Captures with `event.timestamp ∈ (T_end, W)` against V1 must fire `scope_violation` (A's scope at event-time excludes V1).

### Resolution log

- **2026-04-27**: Opened by Wave-1 G-7' agent finding (commit `8be0ae2` test report, anomaly #1). Ship-3 closeout chose to flag rather than fix because the gap is not load-bearing in Ship-1/2/3 (tests construct end via pre-set `valid_to`; production has not yet exercised end-of-assignment). The fix is bounded but lands when the first Ship needs it.

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

### Candidate Rule R-6 (NOT yet adopted) — Intra-ADR seam discipline

**Status**: open trip-wire, not a Standing Rule. Triggered by FP-006 (Ship-2 spec review, 2026-04-25).

If a second intra-ADR seam (tension between two §S of the same ADR, or between an ADR and the charter/ledger/later-Decided ADR) is surfaced by a Ship pressure-test or retro — not handled by R-1 because the agent reasoned past it as "interpretable" — promote this candidate to Standing Rule. Proposed text in [`docs/ships/ship-2.md`](ships/ship-2.md) §9 OQ-2.

---

## References from Other Documents

This register is referenced from:

- [`CLAUDE.md`](../CLAUDE.md) — Agent onboarding pointer
- [`docs/status.md`](status.md) — Carried-debt section
- [`docs/agent-workflow/lessons.md`](agent-workflow/lessons.md) — L-2 (register discipline)
- Any phase spec that creates an FP entry (e.g., [phase-3e.md](implementation/phases/phase-3e.md) §10)

If you add a new FP item, add or update the backlinks above so the register is reachable from every likely entry point.
