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
