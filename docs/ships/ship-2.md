# Ship-2 — Registry Lifecycle & Merge/Split (S06)

> **Status**: spec draft — pre-fills from cites (§1, §2, §3.1, §4, §5, §6.5, §7). User-owned sections (§3.2, §6, §6.4) retro-first per the orchestrator-skill slim shape. Hard rule H1: no code begins until this spec is reviewed and all cited ADRs are Decided.
>
> **Opened**: 2026-04-25
> **Tag target**: `ship-2`
> **Parent**: [`ship-1b.md`](ship-1b.md) (tag `ship-1b` does not move; Ship-2 opens against Ship-1's server as inherited through Ship-1b).
> **Ship-size split source**: [`docs/ships/README.md` § Ship-2 size split — RESOLVED 2026-04-25](README.md#ship-2-size-split--resolved-2026-04-25). Ship-2 = **S06 identity (registry lifecycle + merge/split) only**. S06b shape evolution → Ship-3. S08 case management → Ship-4.

---

## 1. Scenarios delivered

Ship-2 delivers the **identity half of [S06](../scenarios/06-entity-registry-lifecycle.md)**: maintaining a recognised set of subjects across their lifecycle — creation, evolution, deactivation, and reconciliation of mistaken duplicates via merge, and corrective split. Continues under the [S19](../scenarios/19-offline-capture-and-sync.md) offline constraint inherited from prior Ships.

| Scenario | What Ship-2 delivers |
|---|---|
| [S06](../scenarios/06-entity-registry-lifecycle.md) first half | A subject registry that tracks identity lineage. Duplicate-subject reconciliation via `SubjectsMerged` produces an alias projection where queries for either ID resolve to the surviving subject. Wrong-merge correction via `SubjectSplit` archives the source and emits active successors (S07). Post-split events against the archived source are accepted and flagged (S14). |
| [S19](../scenarios/19-offline-capture-and-sync.md) | Duplicate *registrations* remain an offline operation (detected at sync via `identity_conflict` from Ship-1). Merge and split themselves are coordinator-only, online-only ([ADR-002 §S10](../adrs/adr-002-identity-conflict.md#s10-subjectsmerged-and-subjectsplit-are-online-only-operations)) and are NOT offline-executable in this Ship. |

### Delivery surface (H8)

Scripted HTTP simulation against the real server (Ship-1 precedent). Coordinator-level operations are server-addressed, not device-addressed; a mobile surface for merge/split is deliberately not in scope (see §6.5). Acceptance gate: a `WalkthroughAcceptanceTest` extension modelled on Ship-1's, driving the Ship-2 walkthroughs against `TestRestTemplate`.

### Scenarios deliberately not delivered

- **S06b — shape evolution**: deferred to Ship-3 under the 7-Ship split.
- **S08 — case management (long-running subjects with responsibility shifts)**: deferred to Ship-4.
- **S04 — supervisor review / approvals**: Ship-5.
- **S11 — judgment**: Ship-5.
- Everything downstream (S07 transfer, S09 campaigns, S02/S10/S12 reactive layer).

---

## 2. ADRs exercised

Ship-1 exercised ADR-001, ADR-003, ADR-006, ADR-007, ADR-008, ADR-009 §S1/§S2. Ship-1b put ADR-002 §S1 and §S5 under real device load. Ship-2 is the first exercise of the identity-evolution half of ADR-002.

| ADR §S | What it commits | How Ship-2 exercises it |
|---|---|---|
| [ADR-002 §S6](../adrs/adr-002-identity-conflict.md#s6-merge-is-alias-in-projection-never-re-reference) | Merge is alias-in-projection, never re-reference: `SubjectsMerged` emits a `retired_id → surviving_id` alias event; no existing event is modified. Eager transitive closure. | Envelope `type=capture` per [ADR-007 §S2](../adrs/adr-007-envelope-type-closure.md#s2-identity-and-integrity-domain-facts-are-expressed-as-platform-bundled-shapes). `subjects_merged/v1` event accepted; alias projection rebuilds on read; queries against `retired_id` return `surviving_id`'s state; eager closure verified (A→B, B→C ⇒ A→C single-hop). |
| [ADR-002 §S7](../adrs/adr-002-identity-conflict.md#s7-no-subjectsunmerged--wrong-merges-use-corrective-split) | No `SubjectsUnmerged`. Wrong-merge correction is a `SubjectSplit` on the surviving subject creating successors for the conflated entities. | Operationally requires **two** successors (one per conflated entity) — see schema-edit note on §S8 row. §S7 prose "a new successor" is sloppy; archiving the source without recovering both lineages loses identity. Walkthrough W-4 proves the correction path is split-of-survivor with both successors emerging. |
| [ADR-002 §S8](../adrs/adr-002-identity-conflict.md#s8-split-freezes-history-source-is-permanently-archived) | Split freezes history: source is archived (terminal lifecycle). Historical events remain attributed to `source_id`. New events go to successors. | Envelope `type=capture` per [ADR-007 §S2](../adrs/adr-007-envelope-type-closure.md#s2-identity-and-integrity-domain-facts-are-expressed-as-platform-bundled-shapes). **Schema edit required**: `contracts/shapes/subject_split.schema.json` `successor_id` (single) → `successor_ids: array, minItems: 2` (matches §S8 plural prose; required by §S7 wrong-merge correction case). `subject_split/v1` event transitions source to `archived`; historical events unchanged. |
| [ADR-002 §S9](../adrs/adr-002-identity-conflict.md#s9-lineage-graph-acyclicity-by-construction) | Lineage DAG by construction: merge operands must be `active`; archived is terminal. | Attempt to merge an archived subject → server rejects pre-write (pre-write structural rule, not accept-and-flag). Attempt to split an already-archived subject → rejected. Cycle construction is impossible. |
| [ADR-002 §S10](../adrs/adr-002-identity-conflict.md#s10-subjectsmerged-and-subjectsplit-are-online-only-operations) | Merge and split are **online-only**: executed only through a server-validated transaction. No offline execution. | Merge/split endpoints are server-validated; no offline path exists on the device. Duplicate *registrations* remain offline (Ship-1 mechanism unchanged). |
| [ADR-002 §S13](../adrs/adr-002-identity-conflict.md#s13-conflict-detection-uses-raw-references) | Conflict Detector uses raw references; alias resolution happens only in the projection layer after detection. | Post-merge `identity_conflict` detection continues to fire on the `subject_id` the device wrote, not the surviving alias. Projection-side read resolves. |
| [ADR-002 §S14](../adrs/adr-002-identity-conflict.md#s14-events-are-never-rejected-for-state-staleness) | Events are never rejected for state staleness. Post-split / post-merge / against-archived events accepted and flagged. | Walkthrough exercises the "worker was offline when merge happened" case: events arrive against retired IDs; accepted; alias-resolved; no rejection. Events against archived sources accepted and flagged. |
| [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) | State is a projection of events; escape hatch B→C (cacheable projection) requires the cache be rebuildable and never authoritative. | **Blocking FP-002.** The subject-lifecycle surface either projects from events on demand (the `ScopeResolver` precedent from Ship-1) or introduces a rebuildable cache. The Ship-2 choice and its proof is a §6 spec deliverable and a retro deliverable (see §5). |

### Out of scope (explicitly not exercised)

- ADR-002 §S11 (single-writer conflict resolution) — lands first at Ship-5 (judgment / resolution UX). Ship-2 emits `conflict_detected` events; it does not exercise `conflict_resolved` as a first-class resolution workflow.
- ADR-002 §S12 (detection before policy execution) — no reactive policies exist yet; lands at Ship-7.
- ADR-004 §S10 (shape evolution) — Ship-3.
- ADR-005 (state progression, review) — Ship-5.
- Any device UI for merge/split — §6.5.

---

## 3. ADRs at risk of supersession

### 3.1 Structural risks

| Risk | ADR position at risk | Observable that would force supersession |
|---|---|---|
| **R1 — Alias projection correctness under eager transitive closure** | [ADR-002 §S6](../adrs/adr-002-identity-conflict.md#s6-merge-is-alias-in-projection-never-re-reference) | A chain A→B→C does not resolve single-hop; or concurrent merges produce a state where the alias table contradicts the event stream on rebuild. Forces either a clarification of rebuild semantics or an ADR on concurrent-merge serialisation. |
| **R2 — Lineage DAG enforcement under race** | [ADR-002 §S9](../adrs/adr-002-identity-conflict.md#s9-lineage-graph-acyclicity-by-construction) | Two coordinators submit near-simultaneous merge/split commands that, if applied in either order, produce a valid graph, but if interleaved produce a cycle or an operation on an archived subject. S10 (online-only, server-validated transaction) is the intended guard; Ship-2 puts it under load for the first time. |
| **R3 — Post-archive accept-and-flag discipline** | [ADR-002 §S8](../adrs/adr-002-identity-conflict.md#s8-split-freezes-history-source-is-permanently-archived), [§S14](../adrs/adr-002-identity-conflict.md#s14-events-are-never-rejected-for-state-staleness) | **Live invariant, not observable in Ship-2's slice.** §S14 says events against archived sources are accepted and flagged with `flag_category=stale_reference` (charter §Flag catalog #2, ADR-002, auto_eligible — existing category, not new). But Ship-1's CHV flow generates a fresh `subject_id` per capture and never references an existing subject by UUID. The flow that *would* reference an archived UUID does not exist until Ship-3/4 introduces subject pick-lists. R3 carries forward as a Ship-3/4 risk; not exercised here. |
| **R4 — Projection rebuild cost** | [ADR-002 "Risks accepted"](../adrs/adr-002-identity-conflict.md#consequences) + [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) | Batch merges of scale N exceed a rebuild-time budget that the Ship-2 fixture can still detect. If observed on the Ship-2 fixture, it is a Ship-2 retro note; if a structural budget must be declared, it is an ADR addendum. |
| **R5 — Subject lifecycle storage discipline (FP-002)** | [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) | A read path treats a lifecycle cache as authoritative without a rebuild proof. Ship-2's spec §6 must pick a position; the retro must show the proof. If neither is possible, ADR-001 §S2's escape hatch B→C was misapplied and needs an ADR. |
| **R6 — Conflict Detector alias-blindness** | [ADR-002 §S13](../adrs/adr-002-identity-conflict.md#s13-conflict-detection-uses-raw-references) | **Live invariant, not stressed by Ship-2's slice.** §S13 governs how the detector treats events that reference *retired* IDs — raw refs, not alias-resolved. Ship-1's identity_conflict heuristic is name+village (no UUID reference), so alias resolution is irrelevant to detection here. R6 first becomes load-bearing at Ship-3/4 alongside R3. |

### 3.2 Domain-realism risks

Not enumerated pre-build. Categories to watch and record in the retro §4 as they surface during build: **coordinator workflow ergonomics** (what information does a coordinator need at merge/split time that the current envelope doesn't surface?), **operator confusion between "duplicate registration" and "subject evolution"** (same real-world trigger, two distinct platform responses), **merge/split audit trail completeness** (can we reconstruct *why* a merge happened, not just *that* it did?), **post-corrective-split coordinator workflow** (after a wrong-merge correction, both successors are born with empty projection state per [ADR-002 §S8](../adrs/adr-002-identity-conflict.md#s8-split-freezes-history-source-is-permanently-archived) — historical events stay attributed to the archived source; what does the coordinator do next? bootstrap captures? UI affordance pointing to archived-source history? this is a Ship-2-observable consequence to record, not pre-speculate). Not speculated pre-build.

---

## 4. Ledger concepts touched

| Concept | Current | Ship-2 impact |
|---|---|---|
| `subject` [PRIMITIVE] | STABLE | First exercise of lifecycle transitions (active → archived). |
| `subject_lifecycle` [CLASSIFICATION-TBD] | pre-convergence V3 artifact, now absent from `V1__ship1_schema.sql` | Ship-2 chooses: no table (project from events on demand, ScopeResolver precedent) or rebuildable cache. Ledger row asserted and classified in retro. |
| `subjects_merged/v1` [PLATFORM-BUNDLED shape] | platform-bundled per [ADR-007 §S2](../adrs/adr-007-envelope-type-closure.md#s2-identity-and-integrity-domain-facts-are-expressed-as-platform-bundled-shapes); `contracts/shapes/subjects_merged.schema.json` | First emission in live flow. Envelope `type=capture`. |
| `subject_split/v1` [PLATFORM-BUNDLED shape] | platform-bundled per [ADR-007 §S2](../adrs/adr-007-envelope-type-closure.md#s2-identity-and-integrity-domain-facts-are-expressed-as-platform-bundled-shapes); `contracts/shapes/subject_split.schema.json` | First emission in live flow. Envelope `type=capture`. **Schema edit**: `successor_id` → `successor_ids: array, minItems: 2`. |
| `alias_projection` [DERIVED] | not previously materialised | First materialisation. Rebuild procedure documented and tested (FP-002 gate pattern). |
| `coordinator` [actor; instance row] | not previously seeded | New row in `dev_bootstrap` and `actor_tokens`. One human-UUID actor, one bearer token, auths merge/split endpoints. No `actor_tokens.kind` discriminator (one-row simplicity). Scope-filtering, multi-coordinator, and `designated_resolver` deferred to Ship-5 where §S11 becomes load-bearing. |
| `conflict_detected/v1` | STABLE (Ship-1) | **Not extended.** No new flag category. R3 (post-archive) would emit `flag_category=stale_reference` (existing catalog #2) but is not observable in Ship-2's slice (see §3.1 R3). `designated_resolver` continues to be omitted (consistent with Ship-1; §S11 lands at Ship-5). |

Expected classification changes: `subject_lifecycle` row is either created or explicitly declined; an `alias_projection` row is created (DERIVED). Any ADR-002 §S6–§S14 row that moves from STABLE to DISPUTED is a supersession trigger and forces an ADR.

---

## 5. Flagged positions — consult ([Rule R-4](../flagged-positions.md))

R-4 sweep ran 2026-04-25 against the pre-draft state. Results:

| FP | Status | Ship-2 interaction |
|---|---|---|
| [FP-001](../flagged-positions.md#fp-001--role_stale-projection-derived-role-verification) — `role_stale` / scope-from-cache | OPEN | **Does not block.** Ship-2 is identity, not authority. Temporal-divergence test for the gate lands at Ship-5 (first role-action enforcement). |
| [FP-002](../flagged-positions.md#fp-002--subject_lifecycle-table-read-discipline-audit) — `subject_lifecycle` read-discipline | **OPEN, BLOCKS Ship-2** — path locked at spec close 2026-04-25 (option a, project-from-events). Closure evidence at retro. | §6 commitment 6 locks option (a). Walkthroughs do not need to exercise rebuild because there is no cache to rebuild. |
| [FP-003](../flagged-positions.md) — envelope schema parity | RESOLVED | No action. |
| [FP-004](../flagged-positions.md#fp-004--assignment_ref-as-potential-future-envelope-field) — `assignment_ref` envelope field | OPEN | **Does not block.** Merge/split targets subjects, not assignments. |
| [FP-005](../flagged-positions.md#fp-005--corrections-surface-is-unassigned-in-the-5-ship-map) — corrections surface | OPEN | **Does not block.** Ship-2 does not deliver S04; correction flow is a distinct concern from merge/split (reconciles two *captures*, not two *subjects*). |
| [FP-006](../flagged-positions.md#fp-006--s7s8-attribution-semantics-in-the-corrective-split-case) — S7↔S8 attribution under corrective split | **OPENED 2026-04-25 by this Ship-2 spec review** | **Does not block Ship-2.** Ship-1's CHV flow generates fresh `subject_id` per capture and never references existing subjects by UUID, so the S7↔S8 tension (post-corrective-split historical events stay attributed to an archived source despite §S7 promising re-attribution is "optional") is not observable in this slice. Same root cause as §3.1 R3/R6. Gates the first Ship that introduces UUID-referenced flows against subjects (likely Ship-3 shape evolution if devices carry subject UUIDs by ID, certainly Ship-4 case management). |
| [FP-007](../flagged-positions.md#fp-007--contractserver-resource-shape-drift-not-enforced) — contract↔server-resource shape drift not enforced | **OPENED 2026-04-25 by this Ship-2 spec close-out** — path locked to (a) at OQ-4 | **Does not block Ship-2 acceptance.** Path (a) drift-gate diff in `scripts/check-convergence.sh`. Implementation lands in Ship-2's first build commit; closure at Ship-2 retro. |
| [FP-008](../flagged-positions.md#fp-008--conflict_detected-payload-lacks-root_cause-trace-metadata) — `conflict_detected` lacks root_cause trace metadata | **OPENED 2026-04-25 by this Ship-2 spec close-out** | **Does not block Ship-2 acceptance.** Ship-1's two flag categories (scope_violation, identity_conflict) are self-trigger; trace metadata only becomes load-bearing when a stale-reference flag's trigger is distinct from `source_event_id` — first observable when UUID-referenced flows land (Ship-3/4) and structurally required for Ship-5 batch resolution. Resolution surfaced as OQ-5. |

**Inherited open RFS items from Ship-1**:

- **RFS-1** (naïve identity heuristic — `identity_conflict` uses `normalized(household_name) + village_id`): **actively exercised by Ship-2**. Merge/split is the platform's corrective answer to the heuristic's false-positives. Ship-2 retro records whether the heuristic-plus-merge path held up or produced its own failure modes.
- **RFS-2** (village-on-payload): unchanged. Ship-2 does not alter shapes.
- **RFS-3** (schema duplication): unchanged.

**New FPs anticipated**: likely one on coordinator audit-trail completeness if §3.2 domain-realism observations promote it. No new FPs assumed pre-build.

---

## 6. Slice

> **USER-OWNED. Retro-first.** Substantive commitments below were locked at spec pressure-test 2026-04-25 after §S7/§S8/§S11 verification. User finalises wording.

**Substantive commitments (locked at spec time):**

1. **Coordinator actor.** `dev_bootstrap` seeds one new actor of conceptual role `coordinator`: human UUID, bearer token in `actor_tokens`. **Coordinator recognition is a projection over `assignment_created/v1` events**, not a column or a separate table — the same authority primitive every other actor's role is established by ([ADR-003](../adrs/adr-003-authorization-sync.md) precedent). `dev_bootstrap` emits one `assignment_created/v1` with `target_actor.id=<coordinator_uuid>`, `role="coordinator"`, all-null `scope` dimensions (the existing schema already permits this; no contract touch). The merge/split auth predicate is `ScopeResolver.activeAssignments(actorId, now).stream().anyMatch(a -> "coordinator".equals(a.role()))` — projection-derived, no cache (FP-002 path a precedent). No `kind` discriminator on `actor_tokens`. Single global coordinator (no scope filtering, no multi-coordinator). Future-compat: Ship-5's `designated_resolver` lands by adding scope dimensions to this row, never by introducing a parallel authority surface.
2. **Merge/split endpoints.** New endpoints under the `/admin/` URL space (e.g. `POST /admin/subjects/merge`, `POST /admin/subjects/split`), bearer-token-authed by the coordinator's token (NOT admin session, NOT CHV tokens). Endpoints validate §S9 preconditions (operands active; archive-as-target rejected) **pre-write** and reject with structural error — distinct from §S14 accept-and-flag for capture events.
3. **Emitted events**: envelope `type=capture`, `actor_ref = <coordinator_uuid>` (human UUID, NOT `system:` — these are human-authored decisions per F-B4). `shape_ref` = `subjects_merged/v1` or `subject_split/v1`.
4. **Schema edit**: `contracts/shapes/subject_split.schema.json` — `successor_id` (single) → `successor_ids: array, minItems: 2`. One contract touch. Forced by §S7 wrong-merge-correction case (recovering both lineages from a wrong merge requires two successors atomically; sequential single-successor splits archive the source after the first emission and lose the second lineage).
5. **Alias resolver** readable on existing read paths (`/api/sync/pull`, `/admin/events`). Eager transitive closure per §S6.
6. **FP-002 storage decision — LOCKED 2026-04-25 to option (a):** no `subject_lifecycle` table; subject lifecycle is projected from events on demand by replaying `subjects_merged/v1` and `subject_split/v1` (the [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) precedent). No projection cache, no rebuild procedure, no cache-coherence surface. Rationale: Ship-2 fixtures cannot surface read-cost pressure; introducing a cache today is speculative and adds coherence risk + a rebuild test with no failing case to author. If a future Ship's fixture surfaces read cost, escape hatch B→C remains available with FP-002's gate already specified. **Closure evidence at retro**: schema migration contains zero `subject_lifecycle`-shaped tables (assert by SQL); the read path queries the event store, not a derived table (assert by code grep).
7. **Server-authored envelope discipline ([ADR-002 §S1](../adrs/adr-002-identity-conflict.md#s1-event-envelope--causal-ordering-fields), [§S4](../adrs/adr-002-identity-conflict.md#s4-device-sequence-and-sync-watermark-persistence), [§S5](../adrs/adr-002-identity-conflict.md#s5-device-identity-is-hardware-bound)).** Merge/split events are server-authored under a coordinator decision — the *server* is the device that authored the record; the *coordinator* is the actor whose decision it represents. Concrete commitments: `device_id = 00000000-0000-0000-0000-000000000001` (the existing reserved server-device UUID introduced for Ship-1 ConflictDetector); `device_seq` from the existing `server_device_seq` Postgres sequence (Ship-1 satisfies §S4 durability via Postgres); `sync_watermark = null` (server-authored events have no upstream-server they're catching up to); `actor_ref = <coordinator_uuid>` (human, not `system:` per F-B4); `subject_id` = the surviving subject (merge) or the source subject (split). One emission channel, shared with ConflictDetector — no new reserved UUIDs introduced.

**Not in scope** (clarifications):
- No §S14 post-archive flag emission code path (R3 not observable; defer until Ship-3/4 introduces UUID-referenced flows).
- No `designated_resolver` field on emitted flags (§S11 → Ship-5).
- No coordinator scope filtering (→ Ship-5).

Starting point: Ship-1's server package (`dev.datarun.ship1`). Ship-2 extends it. Forking to `dev.datarun.ship2` is rejected as unnecessary churn unless build surfaces a forcing reason (recorded at retro).

Out of scope: see §6.5.

---

## 6.4 Acceptance walkthroughs

> **USER-OWNED.** Three walkthroughs locked at pressure-test 2026-04-25. Each asserts on the ADR §S it exercises, uses `shape_ref` as the flag discriminator (never envelope `type`; F-A2/F-A4/F-B4), and is observable at the HTTP boundary. Walkthroughs are **field-shaped** — they exercise paths Ship-1's actual flow can produce, not protocol fanfic.

**Setup** (preceding all walkthroughs): bootstrap with Ship-1's CHV-A + CHV-B in village-1 / village-2 plus the new `coordinator` actor token. CHV-A captures "Khan" (subject `S_X`) in village-1; CHV-B captures "Khan" in village-1 (subject `S_Y`). Ship-1's name+village heuristic emits `identity_conflict` flag. Both captures persist (accept-and-flag); subjects `S_X` and `S_Y` both exist in the registry as Ship-1 leaves them.

- **W-3 (reactive merge after `identity_conflict`, S06 §S6).** Coordinator (authed by coordinator bearer token) calls `POST /admin/subjects/merge` with `surviving_id=S_Y`, `retired_id=S_X`. Assert: `subjects_merged/v1` event persisted with envelope `type=capture` and `actor_ref=<coordinator_uuid>` (NOT `system:`). On subsequent `/api/sync/pull`, queries that resolve subject by `S_X`'s UUID return `S_Y`'s projected state (alias). Historical events for `S_X` remain present with original `subject_id` (raw, not rewritten). Eager closure: a follow-up merge `S_Y → S_Z` produces single-hop lookup `S_X → S_Z` directly.

- **W-4 (wrong-merge correction via multi-successor split, §S7 + §S8 — exercises schema edit).** Coordinator discovers W-3's merge was wrong (`S_X` and `S_Y` were genuinely distinct entities, name-collision in same village). Coordinator calls `POST /admin/subjects/split` against `S_Y` with `successor_ids=[S_Y_prime, S_X_prime]` (client-generated UUIDs per [ADR-002 §S2](../adrs/adr-002-identity-conflict.md#s2-event-envelope--typed-identity-references); server validates §S9 preconditions atomically per [§S10](../adrs/adr-002-identity-conflict.md#s10-subjectsmerged-and-subjectsplit-are-online-only-operations)). Assert: `subject_split/v1` event persisted with `successor_ids` as a 2-element array; `S_Y` transitions to `archived`; both `S_Y_prime` and `S_X_prime` are active successors visible on subsequent `/api/sync/pull`; historical events for `S_X` and `S_Y` remain attributed to their original `subject_id` ([§S8](../adrs/adr-002-identity-conflict.md#s8-split-freezes-history-source-is-permanently-archived): archived sources keep their history). **Empty-successor invariant** (intentional consequence of §S8): immediately post-split, projecting either successor on `/admin/events` returns no payload-derived state (no `household_name`, no `village_ref`); the original Khan capture remains visibly attributed to archived `S_Y`, not to either successor. This is the correct outcome under immutability + §S8 — successors are bare identities until fresh captures are authored against them. Reject the empty-array variant pre-validation: `successor_ids` of length 0 returns 4xx structural error.

- **W-5 (lineage DAG enforcement, §S9).** Continuing from W-4. Coordinator attempts `POST /admin/subjects/merge` with `S_Y` (now archived) as either operand. Assert: server returns 4xx structural error pre-write — NOT an accept-and-flag. Coordinator attempts to split `S_Y` again. Assert: 4xx structural error pre-write. The error response distinguishes itself from a capture-event flag emission (no `conflict_detected/v1` is written for these rejections; nothing persists in `events`).

§3.1 R4 (projection rebuild cost) is not a dedicated walkthrough — if the W-3/W-4 fixture renders rebuild cost observable, retro §3 records it; if not, R4 carries forward.

§3.1 R3 (post-archive captures) and R6 (§S13 raw refs) are explicitly **not walkthroughs** — Ship-1's CHV flow does not produce events that reference subject UUIDs by ID, so neither risk is observable in this slice. Both carry forward to Ship-3/4.

---

## 6.5 What is deliberately not built

- No device UI for merge/split (ADR-002 §S10: coordinator-level, online-only; CHV mobile surface is inappropriate).
- No shape evolution (S06b → Ship-3).
- No case management (S08 → Ship-4).
- No review / resolution workflow (Ship-5).
- No reactive policies, auto-resolution, triggers (Ship-7).
- No transfer-of-subject across scopes (Ship-6 under the 7-Ship map; orthogonal to merge/split).
- No CHV-initiated corrections of captures (FP-005; distinct concern).
- No assignment churn beyond Ship-1's existing model.
- No new envelope fields. No new envelope type values (vocabulary remains closed at six).

---

## 7. Retro criteria

Ship-2 is done when all of the following hold.

1. All §6.4 walkthroughs (W-3, W-4, W-5) pass against the live server via HTTP; test class extends or mirrors `WalkthroughAcceptanceTest`.
2. Every commit in the Ship-2 range cites at least one scenario ID (`S06`, `S19`). `git log --oneline ship-1b..HEAD` greppable.
3. **FP-002 gate met (option a, locked at spec close)**: `git diff ship-1b..HEAD -- server/src/main/resources/db/migration/` contains no `subject_lifecycle` table; server code grep `git grep -l subject_lifecycle server/` returns no source files reading it as a state source (a structural test that would fail if a cache were silently introduced); subject-lifecycle reads project from events on demand per the `ScopeResolver` precedent.
4. **Contract diff scoped**: `git diff ship-1b..HEAD -- contracts/envelope.schema.json` → 0 lines. `git diff ship-1b..HEAD -- contracts/shapes/` shows **only** the planned `subject_split.schema.json` arity edit (`successor_id` → `successor_ids` array). No other contract files modified. **FP-007 gate met (option a, locked at spec close)**: `scripts/check-convergence.sh` includes a `diff -r contracts/shapes server/src/main/resources/schemas/shapes` step that fails on any divergence; gate runs as part of every Ship close-out and PASSes at Ship-2 close. The two trees agree byte-for-byte at tag time.
5. **Coordinator actor present**: `dev_bootstrap` seeds a coordinator row with human UUID + bearer token. Merge/split endpoints reject requests without that token. `actor_ref` on every emitted `subjects_merged/v1` / `subject_split/v1` event is the coordinator's UUID — grep server code + persisted events: zero matches for `system:` actor_ref on these shapes (would violate F-B4).
6. **Envelope `type=capture` on merge/split**: grep persisted merge/split events: 100% have `type='capture'`. No envelope `type` vocabulary extension (F2). No branching of integrity logic on `type` (F-A2).
7. **Server-authored envelope fields ([ADR-002 §S1/§S4/§S5](../adrs/adr-002-identity-conflict.md#s1-event-envelope--causal-ordering-fields))**: every persisted `subjects_merged/v1` / `subject_split/v1` event has `device_id = 00000000-0000-0000-0000-000000000001` (server reserved UUID, same as ConflictDetector); `device_seq` drawn from `server_device_seq`; `sync_watermark = null`. No new reserved device UUIDs introduced. Verifiable by SQL: `SELECT DISTINCT device_id FROM events WHERE shape_ref IN ('subjects_merged/v1','subject_split/v1')` returns exactly the server UUID.
8. No new ADR drafted unless one of R1–R6 (§3.1) or a §3.2 domain reality triggered one. If one did: ADR merged before tag; ledger updated; charter regenerated; drift gate PASS.
9. Ledger: rows for `subject_lifecycle` (asserted-or-declined per FP-002 choice), `alias_projection`, and `coordinator` exist with classifications. Any row that changed classification has a history entry.
10. Charter regenerated if (9) produced a delta. Drift gate PASS.
11. Retro note filed at `docs/ships/ship-2-retro.md` covering: walkthroughs passed, R1–R6 assessment + §3.2 observations, implementation-grade choices, FPs touched (FP-002 closure evidence explicit; FP-006 status check), ledger deltas, handoff to Ship-3.
12. `ship-2` tag applied. Parent `ship-1b` and grandparent `ship-1` tags do **not** move.

---

## 8. Hand-off to Ship-3

*(Populated at retro.)*

Expected handoffs:

- Any R1–R6 observation that remains live under larger fixtures.
- FP status updates: FP-002 closed or explicitly re-deferred; FP-001 unchanged; any new FPs opened by §3.2 observations.
- The alias-projection rebuild procedure will be inherited by Ship-3's version-diverse projection concern (shape evolution) — Ship-3 spec §2 should cite it.
- Whether a new `conflict_detected/v1` category (or a new `shape_ref`) was introduced for "against archived source", and if so, its classification in the ledger.

---

## 9. Open questions — must be picked up before first build commit

> Read this section every time you reopen the spec. These are not retro-deferrable.

### OQ-1 — FP-002 subject-lifecycle storage choice — RESOLVED 2026-04-25 → option (a)

**Decision**: option (a), project from events on demand. No `subject_lifecycle` table. Lifecycle replays `subjects_merged/v1` / `subject_split/v1` at read time, the [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) precedent.

**Rationale**: Ship-2 fixtures cannot surface read-cost pressure on a small event corpus; introducing a cache today is speculative engineering with a coherence-risk surface and a rebuild test that has no failing case to author. ADR-001 §S2 escape hatch B→C remains available if a future Ship's fixture surfaces read cost; FP-002's gate is already specified for that path.

**Spec impact**: §6 commitment 6 rewritten to lock option (a). §7 retro criterion 3 rewritten to assert (i) no `subject_lifecycle`-shaped table in the schema migration, (ii) no source file in `server/` reads such a table as a state source. FP-002 closure evidence is mechanical at retro.

### OQ-2 — Should the FP register gain a Standing Rule R-6 formalising intra-ADR seam discipline?

The FP-006 (S7↔S8 corrective-split tension) episode surfaced a class of drift that the existing rules R-1..R-5 cover **in principle** (R-1: "observe a position correct today but drift-prone → add an FP") but not **explicitly by name**. The candidate addition would read approximately:

> **Rule R-6 — Intra-ADR seams must be FP'd, not interpreted.** When a Ship pressure-test or retro surfaces a tension between two §S of the same ADR (or between an ADR and the charter / ledger / a later-Decided ADR), an FP entry MUST be opened before the seam is acted upon. The orchestrator does not navigate the seam by interpretation; the FP gate decides whether resolution is implementation-grade or requires ADR-N-R supersession.

**Why deferred, not formalised today**: one instance (FP-006) is an episode, not a pattern. If a second instance surfaces in Ship-3 / Ship-4, that's the forcing function. Formalising on one instance is ceremony.

**Decision needed from**: user, but only if a second instance surfaces. Until then this OQ stays open as a trip-wire for the next person reading this spec.

### OQ-3 — Broken-link cleanup side-quest

[`docs/scenarios/19-offline-first.md`](../scenarios/) does not exist; the file is [`19-offline-capture-and-sync.md`](../scenarios/19-offline-capture-and-sync.md). The wrong link survives in tagged Ship-1 docs ([ship-1.md](ship-1.md), [ship-1b.md](ship-1b.md), [ship-1-retro.md](ship-1-retro.md), [ship-1b-retro.md](ship-1b-retro.md)) and [`CLAUDE.md`](../../CLAUDE.md) line 18. Fixed in Ship-2 spec; not fixed in tagged docs. **Routing**: side-quest, no ADR surface, single small commit — either run before Ship-2 build opens, or fold into Ship-2's first commit. **Decision needed from**: user.

### OQ-4 — FP-007 contract↔server-resource shape drift enforcement — RESOLVED 2026-04-25 → option (a)

**Decision**: option (a), drift-gate diff in `scripts/check-convergence.sh`. Add a `diff -r contracts/shapes server/src/main/resources/schemas/shapes` step to the existing convergence gate; the gate fails on any divergence and runs at every Ship close.

**Rationale**: (a) is one shell line in a script that already runs at Ship close — closes FP-007 with minimum surface. (b) is structurally cleaner (single source of truth) but costs build-system rewiring (`maven-resources-plugin` at `generate-resources`, classpath-load verification, doc updates) for a problem that hasn't accumulated real friction yet; available as a future cleanup once duplication is felt. (c) does not close the FP. (defer) carries silent-drift risk into the Ship that mutates a shape — exactly Ship-2 — which is wrong.

**Spec impact**: §7 retro criterion 4 grows by one clause: `scripts/check-convergence.sh` includes the shape-tree diff and PASSes at Ship-2 close. No §6 change. The actual `scripts/check-convergence.sh` edit lands in Ship-2's first build commit alongside the `subject_split.schema.json` arity edit so the new gate is in place before the schema diverges.

### OQ-5 — FP-008 `conflict_detected` root_cause field — RESOLVED 2026-04-25 → option (c)

**Decision**: option (c), defer to the first Ship that emits a `conflict_detected/v1` event whose source-of-badness is distinct from `source_event_id` (likely Ship-3 if shape evolution introduces UUID-referenced flows; certainly Ship-4 case management; structurally required by Ship-5 batch resolution). That Ship adds the field as part of its own contract diff scope, populates it on emission, and lands a walkthrough that asserts the trigger UUID. Ship-5 then has a populated field to batch-resolve against.

**Why (a) was rejected**: adding the field in Ship-2 lands a schema field that *no Ship-2 emission path populates* — the §3.1 R3 reasoning explicitly says stale-reference flags are not observable in Ship-2's slice. Schema additions ahead of an emission site are speculative engineering. Worse, `additionalProperties: true` cuts the *other* way: precisely because the addition is non-breaking, deferring costs nothing.

**Why (b) was rejected**: Ship-5 is too late. Ship-3/4 introduces UUID-referenced flows where stale-reference flags first emit; that's the Ship where the field becomes load-bearing on the *producer* side. Pushing the schema change to Ship-5 means Ship-3/4 emits trace-less flags into the corpus that Ship-5 then has to consume. Wrong shape — the field should be added by the producing Ship, not the consuming Ship.

**Why (c) is correct**: the cost of deferring past Ship-2 is zero in practice because Ship-1's two flag categories trivially have `source_event_id == trigger`, and Ship-2 emits no flag categories where that identity breaks. The early flag corpus carries no missing trace metadata because no emitted flag has a missing trace.

**Spec impact**: none on Ship-2. FP-008's `Blocks:` field and resolution path text are rewritten in `docs/flagged-positions.md` to reflect (c) as chosen and to name the triggering Ship explicitly.
