# Concept Ledger

> **Lifespan**: temporary scaffolding. Archived at Phase 4 freeze.
>
> The ledger is the single inventory of every named concept in the platform.
> The charter is mechanically derivable from it: each STABLE row produces one
> charter claim.

## Schema

Every named concept (primitive, invariant, contract, flag, algorithm, config
knob, derived value, reserved/deferred item) has exactly one row.

| field | meaning |
|---|---|
| `concept` | kebab-case canonical name. Unique. |
| `classification` | one of: PRIMITIVE / INVARIANT / CONTRACT / CONFIG / FLAG / ALGORITHM / DERIVED / RESERVED / OBSOLETE / OPEN |
| `settled-by` | ADR cite (`ADR-NNN §X`) or `—` if OPEN |
| `status` | OPEN / PROPOSED / STABLE / DEFERRED / SUPERSEDED |
| `introduced-in` | first doc that named the concept |
| `history` | append-only list of `(round, classification, settled-by)` tuples |
| `notes` | optional one-liner; blockers, FP cites, gotchas |

## Rules

1. **Append-only history.** A concept's classification can change across
   rounds, but the history column records every change. Never delete.
2. **Supersession trigger.** If a round changes a concept's classification, the
   ADR previously listed in `settled-by` is superseded per `supersede-rules.md`.
3. **STABLE definition.** A row is STABLE only after a full round in which its
   classification did not change AND no upstream concept it depends on changed.
4. **DEFERRED definition.** A row is DEFERRED when a deliberate decision is
   made not to settle it in this convergence pass. Must name the blocking
   condition (future ADR, future phase, external dependency).
5. **OBSOLETE definition.** A row is OBSOLETE when a concept turns out not to
   exist in the platform at all (was a misnamed thing, a duplicate, or
   abandoned). Row stays for traceability.
6. **Phase 4 freeze precondition.** Every row is STABLE, DEFERRED, or OBSOLETE.
   Zero rows in OPEN, PROPOSED, or SUPERSEDED status.

## Classifications — definitions

- **PRIMITIVE**: a load-bearing object/concept the platform is built on
  (Event, Subject, Actor, Device). Cannot be reduced to other concepts.
- **INVARIANT**: a property that always holds and which other code may rely on
  (envelope-has-11-fields, append-only-events).
- **CONTRACT**: a shared interface/format (envelope schema, sync protocol).
- **CONFIG**: a knob that varies per-deployment or per-tenant.
- **FLAG**: an integrity/auth signal raised by detection (scope_violation).
- **ALGORITHM**: a named computational procedure (conflict detection sweep,
  scope containment check).
- **DERIVED**: something computed from other concepts; not stored.
- **RESERVED**: named but not currently emitted/used (envelope `process` type).
- **OBSOLETE**: was a concept once; no longer exists.
- **OPEN**: classification not yet decided.

## Rows

**Phase 0 inventory summary (round 0)**

- Raw rows across 4 clusters: 486
- Canonical concepts after dedupe: 269
- Status breakdown:
  - PROPOSED (single classification agreed by all sources that mentioned it): 244
  - OPEN (classification=OPEN or DISPUTED, awaiting Phase 2 resolution): 22
  - DEFERRED (explicit deferral — blocking condition named): 3
- Classification breakdown:
  - ALGORITHM: 49, CONFIG: 42, PRIMITIVE: 39, DERIVED: 33, INVARIANT: 32, CONTRACT: 29
  - FLAG: 15, OPEN: 13, DISPUTED: 9, RESERVED: 8
- Size note: target was 150–200 unique concepts. Actual 269 reflects that the platform's ADR stress-tests (archive/ 00–21) introduce many named micro-concepts (anti-patterns, composition rules, trap names). Phase 1 topological sort will likely reveal synonyms to merge (`role-stale`/`role-staleness`, `scope-stale`/`scope-violation`, `workflow-pattern`/`pattern`). Do not prune pre-sort.

**Phase 2 round 2 — quiet scan complete (2026-04-23)**: no ADRs drafted, no classifications changed, no upstream ADR superseded. The 21 round-1-touched rows promoted to STABLE per ledger rule 3. Final counts: **STABLE 251, DEFERRED 7, OBSOLETE 11, OPEN 0, PROPOSED 0, DISPUTED 0**. Total 269. Every platform concept is now settled, deferred, or obsolete; zero rows await a further decision in this convergence pass. Charter is closure-ready; phase-2 exit criterion satisfied.

**Ship-2 close-out (2026-04-26)**: three new STABLE rows added in the lifecycle / merge-split slice — `subject-lifecycle` (DERIVED, projected from events on demand per FP-002 (a); cache intentionally absent), `alias-projection` (DERIVED, materialised from events by [`SubjectAliasProjector`](../../server/src/main/java/dev/datarun/ship1/admin/SubjectAliasProjector.java) on read; eager transitive closure per ADR-002 §S6), `coordinator` (CONFIG, deployer-seeded actor instance; recognition is the existing `assignment-changed` PRIMITIVE, no parallel mechanism per ADR-009 §S1). No ADR drafted, no classification changed for any pre-existing row. Counts: **STABLE 254, DEFERRED 7, OBSOLETE 11, OPEN 0, PROPOSED 0, DISPUTED 0**. Total 272.

**Ship-3 close-out (2026-04-27)**: four new STABLE rows added in the shape-evolution slice — `household_observation/v2` (CONFIG, deployer-instance shape per ADR-009 §S1; additive evolution per ADR-004 §S10), `shape_registry` (CONFIG-storage, JAR-bundled pattern (a) per ship-3 §6.1 sub-decision 3; ADR-004 §S1 / §S10), `multi_version_projection` (DERIVED, per-request `shape_ref` routing with no cache; ADR-001 §S2 / ADR-004 §S10), `field_count_budget` (PRIMITIVE-adjacent, deploy-time validation rule enforced at registry-load; ADR-004 §S13). Each is distinct from the like-named abstract row created in earlier rounds (`shape-registry`, `multi-version-projection`, `field-budget`) — Ship-3 instantiations of the same concepts in live flow, following the Ship-2 `alias-projection` vs `merge-alias-projection` precedent. No ADR drafted, no classification changed for any pre-existing row. Counts: **STABLE 258, DEFERRED 7, OBSOLETE 11, OPEN 0, PROPOSED 0, DISPUTED 0**. Total 276.

**Phase 2 round 1 — fixpoint close-out (2026-04-23)**: after ADR-009 landed, a full ledger scan applied three operations:

1. **Topology buckets enacted** (queued from round 0, triaged in [phase-1-topology.md](inventory/phase-1-topology.md)):
   - Bucket A (10 rows) → OBSOLETE. Anti-pattern alerts + convergence-protocol process steps; not platform concepts.
   - Bucket B (1 row: `sensitive-subject-classification`) → DEFERRED. Blocker: Phase 4 workflow & policy design surface.
   - Bucket D (1 row: `projection-rebuild-strategy`) → DEFERRED. Blocker: IDR track after flag lifecycle settles (IG-authority, not ADR-authority).

2. **Fixpoint promotion** (ledger rule 3): every PROPOSED row whose classification did not change in round 1 and whose upstream ADR was not superseded was promoted to STABLE — **230 rows**.

3. **Round-1-touched rows stay PROPOSED** — 21 rows: the four charter-invariant settlements (accept-and-flag, flag, flag-creation-location, conflict-detection), seven class-membership flag cites (concurrent-state-change, stale-reference, temporal-authority-expired, scope-violation, role-stale), the five envelope-type canonicalizations (conflict-detected, conflict-resolved, subjects-merged, subject-split, type-vocabulary), the four reference-field canonicalizations (subject-ref, actor-ref, activity-ref, process-identity), and the three duality canonicalizations (scope, pattern, activity). Promotion of these requires one additional quiet round (or the Phase 2 → Phase 3 transition).

**Status after round 1 close-out**: STABLE 230, PROPOSED 21, DEFERRED 7 (5 pre-existing + Bucket-B + Bucket-D), OBSOLETE 11 (pre-existing `role-staleness` + 10 Bucket-A), OPEN 0, DISPUTED 0. Total 269. Zero open forward-refs. Drift gate PASS.

**Phase 2 round 1 — ADR-009 landed (2026-04-23)**: platform-fixed mechanism vs. deployer-configured instance duality canonicalized. `scope` DISPUTED → PRIMITIVE (§S2). `pattern` DISPUTED → PRIMITIVE (§S3). `activity` DISPUTED → CONFIG (§S4). The duality rule is recorded as a charter invariant (§S1) and a forbidden pattern (F-C1), subsuming future concepts exhibiting the same shape without a new ADR. No supersessions — ADR-003 §S7, ADR-004 §S7/§S9, ADR-005 §S5 all re-cited. Round 1 is now closure-ready: all nine DISPUTED rows from round 0 are classified.

**Phase 2 round 1 — ADR-008 landed (2026-04-23)**: envelope reference fields canonicalized. `subject-ref`, `actor-ref`, `activity-ref` all DISPUTED → CONTRACT. `process-identity` re-cited as RESERVED under ADR-008 §S1. Reference-vs-referent rule stated explicitly (F-B1). Forward item FP-004 opened for potential `assignment_ref` evolution. No supersedes on ADR-001/002/004 — all re-cited.

**Phase 2 round 1 — ADR-007 landed (2026-04-23)**: envelope type closure + integrity shape canonicalization. Settles `conflict-detected` (CONTRACT, DISPUTED→settled); re-cites `conflict-resolved`, `subjects-merged`, `subject-split` (CONTRACT) and `type-vocabulary` (INVARIANT). **Absorbs ADR-002 Addendum** — Addendum retained as archival artifact with `Superseded-By: ADR-007` header + `>>> ABSORBED INTO ADR-007` annotation. ADR-002 main untouched (Addendum pointer becomes one-hop redirect). Kills the Addendum pattern, which was the single largest source of drift that motivated this convergence pass.

**Phase 2 round 1 — ADR-006 landed (2026-04-22)**: flag semantics. Settles `accept-and-flag` (INVARIANT), `flag` (INVARIANT), `flag-creation-location` (INVARIANT), `conflict-detection` (ALGORITHM confirmed). Dedupes `role-staleness` → OBSOLETE (canonical `role-stale`). Sweeps 7 flag-catalog rows with class-membership cite. No supersessions (ADR-002 S14 refined, not replaced). Next: ADR-007.

**Phase 1 topological sort (round 0, finalized)** — 22 OPEN rows triaged into four action buckets in [phase-1-topology.md](inventory/phase-1-topology.md):

- **Bucket A (10 rows) → OBSOLETE** in Phase 2: 7 anti-pattern alerts + 3 convergence-process steps. Not platform concepts.
- **Bucket B (1 row) → DEFERRED** to Phase 4: `sensitive-subject-classification`.
- **Bucket C (10 rows) → 4-ADR queue**: ADR-006 (flag semantics) → ADR-007 (envelope type closure / conflict-detected) → ADR-008 (envelope ref fields) → ADR-009 (platform-fixed vs deployer-configured duality).
- **Bucket D (1 row) → IDR**: `projection-rebuild-strategy` (implementation-grade, not ADR-authority).

Status changes enacted in Phase 2 when each ADR commits. Ledger rows remain OPEN in round 0. See topology doc for ordering rationale, upstream deps, expected supersessions (ADR-002 → ADR-002-R, ADR-004 partial supersede by ADR-009), and origin-layer source weighting.

**Archive-harvest companion (round 0.5)** — the 9 DISPUTED rows were back-filled with targeted readings of `exploration/archive/` stress-tests at the authority order established 2026-04-22 (root docs > ADRs > archive > architecture). Findings in [disputes-harvest.md](inventory/disputes-harvest.md):

- **Cross-cutting finding**: all 9 disputes share one structure — platform-fixed mechanism + deployer-configured instance. A single classification is lossy by construction; the `notes` column records the subordinate reading. Phase 2 may collapse to 2–4 ADRs (one on the duality itself, a few per-concept).
- **Likely already-settled**: `conflict-detected` (CONTRACT per ADR-002 Addendum — should be STABLE by round 1).
- **Subagent artifacts, not real disputes**: `actor-ref` and `activity-ref` `RESERVED` readings have no archive evidence. Recommend CONTRACT in Phase 2.
- **Category errors**: `subject-ref` dispute dissolves once the referent (PRIMITIVE subject) is separated from the reference (CONTRACT field).
- **Genuine architecture-level**: `scope`/`pattern`/`activity` share the duality and will drive the Phase 2 cross-cutting ADR.
- Top 10 DISPUTED concepts (Phase 2 ADR priority queue):
  - `accept-and-flag`: A:ALGORITHM, B:INVARIANT, C1:ALGORITHM, C2:ALGORITHM — ambiguity between "the invariant that we never reject" and "the algorithm for detect+append"
  - `activity`: A:PRIMITIVE, C1:CONFIG
  - `activity-ref`: A:CONTRACT, B:RESERVED, C1:CONTRACT
  - `actor-ref`: A:CONTRACT, B:RESERVED, C1:CONTRACT
  - `conflict-detected`: A:CONTRACT, B:SHAPE(→CONTRACT), C1:CONFIG, C2:PRIMITIVE — platform-bundled shape vs. flag vs. envelope-type confusion (see ADR-002 Addendum)
  - `flag`: B:DERIVED, C1:INVARIANT, C2:PRIMITIVE
  - `pattern`: A:PRIMITIVE, B:CONFIG, C2:PRIMITIVE — workflow template: platform-fixed primitive or deployer knob?
  - `scope`: C1:PRIMITIVE, C2:CONFIG
  - `subject-ref`: A:PRIMITIVE, C1:CONTRACT, C2:PRIMITIVE
  - `role-stale` vs `role-staleness` vs `scope-stale` vs `scope-violation`: near-synonyms in the flag vocabulary — need canonical names in Phase 2.

The 18 DISPUTED rows are the natural input to Phase 1 (topological sort) and Phase 2 (ADR drafting). Every PROPOSED row awaits STABLE certification; no row is STABLE in round 0 by construction.

| concept | classification | settled-by | status | introduced-in | history | notes |
|---|---|---|---|---|---|---|
| accept-and-flag | INVARIANT | ADR-006 §S1 | STABLE | ADR-002 | round 0: DISPUTED (A:ALGORITHM, B:INVARIANT, C1:ALGORITHM, C2:ALGORITHM); round 1: INVARIANT (ADR-006 §S1); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Property vs. procedure split: property is INVARIANT (this row), procedure lives at `conflict-detection` (ALGORITHM). Canonical over prior ADR-002 S14 cite. |
| active-assignment | DERIVED | — | STABLE | phases/phase-2.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | — |
| activity | CONFIG | ADR-009 §S4 | STABLE | ADR-004 | round 0: DISPUTED (A:PRIMITIVE, C1:CONFIG); round 1: CONFIG (ADR-009 §S4); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Deployer-assembled L0 instance (pattern selection + shape bindings + role mappings + scope params). Envelope field `activity-ref` is CONTRACT in its own row (ADR-008 §S3) — reference-vs-referent orthogonality (ADR-008 §S4). |
| activity-ref | CONTRACT | ADR-008 §S3 | STABLE | ADR-004 | round 0: DISPUTED (A:CONTRACT, B:RESERVED, C1:CONTRACT); round 1: CONTRACT (ADR-008 §S3); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Optional-with-auto-populate deployer-chosen identifier (`[a-z][a-z0-9_]*` or null). Field is CONTRACT; activity *instance* is CONFIG (ADR-004 §S9) — separate rows. B:RESERVED was a subagent artifact. |
| actor | INVARIANT | — | STABLE | adr-002-identity-conflict.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | persistent identity category |
| actor-ref | CONTRACT | ADR-008 §S2 | STABLE | ADR-004 | round 0: DISPUTED (A:CONTRACT, B:RESERVED, C1:CONTRACT); round 1: CONTRACT (ADR-008 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Two admissible forms: human UUID and `system:{source_type}/{source_id}`. `source_type` is an evolvable platform vocabulary, not a closed enum. Discriminator: `startswith("system:")`. B:RESERVED was a subagent artifact. |
| actor-token | CONTRACT | — | STABLE | phases/phase-2.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | — |
| alias-cache | DERIVED | — | STABLE | phases/phase-1.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | in-memory map for merge resolution |
| alias-mapping | DERIVED | — | STABLE | adr-002-identity-conflict.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | merge resolution projection |
| alias-projection | DERIVED | ADR-002 §S6 | STABLE | ships/ship-2.md | Ship-2 (2026-04-26): CREATED — DERIVED (ADR-002 §S6) | First materialisation in live flow. [`SubjectAliasProjector`](../../server/src/main/java/dev/datarun/ship1/admin/SubjectAliasProjector.java) rebuilds eagerly per request from `subjects_merged/v1` / `subject_split/v1` events; eager transitive closure (A→B, B→C ⇒ A→C single-hop). No persistent table; not authoritative state — events remain the source of truth. Distinct from the abstract `merge-alias-projection` strategy row. |
| alias-resolution | ALGORITHM | — | STABLE | raw-B | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | single-hop lookup |
| alias-table | CONTRACT | — | STABLE | phases/phase-1.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | materialized persistence layer |
| alert | CONTRACT | — | STABLE | phases/phase-4.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | envelope type (Phase 4) |
| anti-pattern | OBSOLETE | — | OBSOLETE | ADR-004 Session 3 | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | 6 catalogued pitfalls |
| append-only | INVARIANT | — | STABLE | phases/phase-0.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | foundational write discipline |
| approval-chain | ALGORITHM | — | STABLE | ADR-005 Session 1 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | multi-level workflow |
| assignment | INVARIANT | — | STABLE | ADR-001 | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | temporal identity category |
| assignment-based-access | CONTRACT | — | STABLE | raw-B | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | scope bindings via events |
| assignment-changed | CONTRACT | — | STABLE | ADR-003 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | envelope type |
| assignment-created | CONFIG | — | STABLE | shapes/assignment_created.schema.json | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | shape definition |
| assignment-ended | CONFIG | — | STABLE | shapes/assignment_ended.schema.json | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | shape definition |
| assignment-lifecycle | DERIVED | — | STABLE | phases/phase-2.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | projected from events |
| assignment-resolver | PRIMITIVE | — | STABLE | ADR-005 Session 1 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | validates role-holders |
| authority-as-projection | ALGORITHM | — | STABLE | ADR-003 S12 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | reconstructed from timeline |
| authority-context | DERIVED | — | STABLE | ADR-003 | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | per-event authorization |
| auto-eligible-flag | CONFIG | — | STABLE | phases/phase-2.md | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | resolvability classification |
| auto-resolution | ALGORITHM | — | STABLE | ADR-005 Session 1 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L3b deadline-watch |
| auto-resolution-policy | ALGORITHM | — | STABLE | phases/phase-4.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | auto-eligible flag resolution |
| boundary-mapping | OBSOLETE | — | OBSOLETE | raw-B | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | consolidation framework step |
| breaking-change | CONFIG | — | STABLE | ADR-004 Session 2 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | schema evolution mode |
| capture | CONTRACT | — | STABLE | phases/phase-0.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | envelope type |
| capture-only | PRIMITIVE | — | STABLE | ADR-004 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | operational pattern |
| capture-with-review | PRIMITIVE | — | STABLE | ADR-004 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | operational pattern |
| case-management | CONFIG | — | STABLE | ADR-005 Session 1 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | workflow pattern |
| causal-ordering | ALGORITHM | — | STABLE | ADR-002 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | device_seq + sync_watermark |
| command-validator | DERIVED | — | STABLE | primitives.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | device-side advisory |
| competing-patterns | INVARIANT | — | STABLE | phases/phase-4.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | composition rule 5 |
| complex-composition | ALGORITHM | — | STABLE | ADR-005 Session 2 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | multi-pattern on same subject |
| composition-rule | CONTRACT | — | STABLE | phases/phase-4.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 5 pattern interaction rules |
| concurrent-state-change | FLAG | ADR-006 §S2 | STABLE | ADR-002 S7 | round 0: FLAG (inventory); round 1: class-membership cited (ADR-006 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | structural conflict |
| config-gradient | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | four-layer configuration model |
| config-package | CONTRACT | — | STABLE | phases/phase-3.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | atomic delivery structure |
| config-packager | PRIMITIVE | — | STABLE | adr-004-configuration-boundary.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | assembles atomic deliverable |
| config-version | DERIVED | — | STABLE | phases/phase-3.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | monotonic versioning |
| configuration-boundary | INVARIANT | — | STABLE | adr-004-configuration-boundary.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | four-layer separation |
| configuration-delivery-pipeline | ALGORITHM | — | STABLE | adr-004-configuration-boundary.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | author→validate→package→deliver→apply |
| configuration-specialist-trap | OBSOLETE | — | OBSOLETE | ADR-004 Session 3 | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | anti-pattern alert |
| conflict | FLAG | — | STABLE | ADR-001 | round 0: FLAG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | anomaly for resolution |
| conflict-detected | CONTRACT | ADR-007 §S2 | STABLE | ADR-002 | round 0: DISPUTED (A:CONTRACT, B:SHAPE→CONTRACT, C1:CONFIG, C2:PRIMITIVE); round 1: CONTRACT (ADR-007 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Platform-bundled integrity shape. ADR-007 absorbs the ADR-002 Addendum and makes this the canonical cite. |
| conflict-detection | ALGORITHM | ADR-006 §S3 | STABLE | ADR-002 | round 0: ALGORITHM (inventory); round 1: ALGORITHM confirmed (ADR-006 §S3); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Procedure that enforces the §S1 invariant. Extensible/relocatable without changing the invariant. |
| conflict-detector | PRIMITIVE | — | STABLE | adr-002-identity-conflict.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | evaluates anomalies |
| conflict-resolution | ALGORITHM | — | STABLE | ADR-002 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | ConflictResolved workflow |
| conflict-resolved | CONTRACT | ADR-007 §S2 | STABLE | ADR-002 | round 0: CONTRACT (inventory); round 1: CONTRACT re-cited (ADR-007 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Platform-bundled shape; spans type=review (human) and type=capture (system:auto_resolution). |
| context-properties | DERIVED | — | STABLE | raw-B | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 7 platform-fixed values |
| context-resolver | PRIMITIVE | — | STABLE | phases/phase-3d.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | resolves context.* at form-open |
| context-scope | DERIVED | — | STABLE | adr-005-state-progression.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | form evaluation scope |
| contract | INVARIANT | — | STABLE | contracts.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | inter-primitive guarantee |
| coordinator | CONFIG | ADR-009 §S1 | STABLE | ships/ship-2.md | Ship-2 (2026-04-26): CREATED — CONFIG (ADR-009 §S1) | Deployer-authored actor instance (one human UUID + bearer token, seeded by [`DevBootstrapController`](../../server/src/main/java/dev/datarun/ship1/admin/DevBootstrapController.java) per ship-2 §6 commitment 1). Recognition is a projection over `assignment_created/v1` events with `role="coordinator"` — the existing PRIMITIVE `assignment-changed` mechanism is reused; **no parallel mechanism row** per ADR-009 §S1 duality (mechanism PRIMITIVE / instance CONFIG; F-C1). Future-compat: Ship-5 `designated_resolver` lands as scope dimensions on this row, never a parallel surface. |
| cross-activity-link | ALGORITHM | — | STABLE | phases/phase-4.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | composition rule 4 |
| cross-cutting | INVARIANT | — | STABLE | cross-cutting.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 8 spanning concerns |
| custom-shape | DERIVED | — | STABLE | phases/phase-3.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | deployer-authored definition |
| deadline-check | CONFIG | — | STABLE | ADR-004 S7 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L3b trigger type |
| deadline-trigger | CONFIG | — | STABLE | ADR-005 Session 1 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | time-based reaction |
| decision-harvest | OBSOLETE | — | OBSOLETE | raw-B | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | convergence framework step |
| default-expression | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L2 form rule |
| deploy-time-validator | PRIMITIVE | — | STABLE | ADR-004 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | complexity gating |
| deployment-parameterization | CONFIG | — | STABLE | raw-B | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | deployer configuration |
| deprecated-shape | DERIVED | — | STABLE | phases/phase-3.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | unavailable for new events |
| deprecation-change | CONFIG | — | STABLE | ADR-004 Session 2 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | schema evolution mode |
| deprecation-only-evolution | ALGORITHM | — | STABLE | raw-B | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | default shape versioning strategy |
| detect-before-act | INVARIANT | — | STABLE | ADR-002 S12 | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | flagged events excluded from state/policy |
| device-id | CONTRACT | — | STABLE | ADR-001 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | hardware-bound UUID |
| device-identity | RESERVED | — | STABLE | raw-B | round 0: RESERVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | hardware-bound persisted identity |
| device-seq | CONTRACT | — | STABLE | ADR-001 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | per-device monotonic integer |
| device-sequence | CONTRACT | — | STABLE | adr-002-identity-conflict.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | synonym of device-seq |
| device-sharing | RESERVED | — | DEFERRED | phases/phase-2.md | round 0: DEFERRED (inventory) | per-actor sync sessions (IG-14) |
| device-time | INVARIANT | — | STABLE | ADR-001 S5 | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | advisory-only timestamp |
| domain-uniqueness-constraint | CONFIG | — | STABLE | ADR-004 Session 3 Part 4 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | deployer-defined rule |
| domain-uniqueness-violation | FLAG | ADR-006 §S2 | DEFERRED | ADR-004 Session 3 Part 4 | round 0: FLAG (inventory); round 1: class-membership cited (ADR-006 §S2) | flag category (entry 7); emission deferred to Phase 4 per flag-catalog |
| draft-config | DERIVED | — | STABLE | phases/phase-3.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | pending package on device |
| duplicate-candidate-identified | FLAG | — | STABLE | ADR-002 S9 | round 0: FLAG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | merge nomination event |
| entity-lifecycle | PRIMITIVE | — | STABLE | ADR-005 Session 2 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | pattern for static entities |
| envelope | CONTRACT | — | STABLE | ADR-001 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 11-field event wrapper |
| event | PRIMITIVE | — | STABLE | ADR-001 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | immutable record |
| event-assembler | ALGORITHM | — | STABLE | phases/phase-3d.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | constructs 11-field envelope |
| event-id | CONTRACT | — | STABLE | adr-001-offline-data-model.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | client-generated UUID |
| event-level-pattern | CONFIG | — | STABLE | ADR-005 Session 1 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | per-event state pattern |
| event-level-state | DERIVED | — | STABLE | ADR-005 Session 2 | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | event-scoped state |
| event-reaction | ALGORITHM | — | STABLE | ADR-004 S7 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L3a synchronous trigger |
| event-reaction-trigger | CONFIG | — | STABLE | ADR-005 Session 1 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L3a trigger type |
| event-ref | CONTRACT | — | STABLE | adr-001-offline-data-model.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | cross-event reference |
| event-sourcing | INVARIANT | — | STABLE | phases/phase-0.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | all state from events |
| event-store | PRIMITIVE | — | STABLE | ADR-001 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | append-only persistence |
| event-type | CONTRACT | — | STABLE | ADR-004 S3 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | closed 6-value vocabulary |
| expression-evaluator | PRIMITIVE | — | STABLE | ADR-004 S8 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | pure-function AST evaluator |
| expression-language | ALGORITHM | — | STABLE | ADR-004 S8 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | single language, two contexts |
| file-pattern | RESERVED | — | DEFERRED | phases/phase-4.md | round 0: DEFERRED (inventory) | entity_lifecycle alternative (Phase 4+) |
| field-budget | INVARIANT | — | STABLE | ADR-004 S10 | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 60-field limit per shape |
| field-reference | CONTRACT | — | STABLE | ADR-004 S8 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | expression component |
| field_count_budget | PRIMITIVE | ADR-004 §S13 | STABLE | ships/ship-3.md | Ship-3 (2026-04-27): CREATED — PRIMITIVE (ADR-004 §S13) | Ship-3 instantiation of the abstract `field-budget` row as a deploy-time validation rule. Enforced at registry-load by [`ShapePayloadValidator.enforceFieldCountBudget`](../../server/src/main/java/dev/datarun/ship1/event/ShapePayloadValidator.java) (top-level `properties` count; recursion not flattened — counting convention documented in the method javadoc and in [`FieldCountBudgetTest`](../../server/src/test/java/dev/datarun/ship1/event/FieldCountBudgetTest.java) class comment). PRIMITIVE-adjacent: not a runtime concept and not a payload concept — a build-time invariant projected into a server primitive. Distinct from the abstract `field-budget` INVARIANT row. |
| filter-predicate | ALGORITHM | — | STABLE | phases/phase-3.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | boolean condition in expression |
| flag | INVARIANT | ADR-006 §S2 | STABLE | ADR-001 | round 0: DISPUTED (B:DERIVED, C1:INVARIANT, C2:PRIMITIVE); round 1: INVARIANT (ADR-006 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Event-stream canonicality (scoped, not global): on-stream anomaly records take flag form with no parallel record-surface; off-stream surfaces (metrics/telemetry/logs) are out of scope. Instances are DERIVED; catalog members are CONFIG. |
| flag-cascade-contamination | DERIVED | — | STABLE | ADR-005 Session 2 | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | projection property |
| flag-catalog | CONTRACT | — | STABLE | flag-catalog.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 9-category register |
| flag-category | CONTRACT | — | STABLE | conflict_detected.schema.json | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | enumerated anomaly type |
| flag-creation-location | INVARIANT | ADR-006 §S4 | STABLE | raw-B | round 0: OPEN (inventory); round 1: INVARIANT (ADR-006 §S4); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Server-side by default; additively evolvable to device. Canonicalizes `architecture/boundary.md §SG-1`. |
| flag-exclusion | ALGORITHM | — | STABLE | phases/phase-1.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | flagged from state derivation |
| flag-resolvability-classification | DERIVED | — | STABLE | raw-B | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | auto_eligible vs manual_only |
| flag-severity | CONFIG | — | STABLE | phases/phase-4.md | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | blocking vs informational |
| flagged-event-exclusion | ALGORITHM | — | STABLE | phases/phase-1.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | remove from state |
| form-context | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | expression evaluation scope |
| form-engine | ALGORITHM | — | STABLE | phases/phase-3.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | shape→form renderer |
| form-logic | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L2 expressions |
| forward-compatibility | INVARIANT | — | STABLE | phases/phase-3.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | config package design |
| four-layer-gradient | CONFIG | — | STABLE | ADR-004 S5 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L0-L3 configuration |
| gap-identification | OBSOLETE | — | OBSOLETE | raw-B | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | convergence framework step |
| geographic-scope | CONFIG | — | STABLE | ADR-003 S3 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | location hierarchy type |
| greenspun-rule | OBSOLETE | — | OBSOLETE | ADR-004 Session 3 | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | anti-pattern alert |
| hardware-bound-identity | INVARIANT | — | STABLE | adr-002-identity-conflict.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | device_id uniqueness |
| household_observation/v2 | CONFIG | ADR-009 §S1, ADR-004 §S10 | STABLE | ships/ship-3.md | Ship-3 (2026-04-27): CREATED — CONFIG (ADR-009 §S1, ADR-004 §S10) | Deployer-instance shape per [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) duality (the deployer named `household_observation` and authored its evolution; v2 is the next member of the same instance lineage, not a platform primitive). Additive evolution per [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) — adds `head_of_household_phone`, deprecates `visit_notes`. Identity-key field names (`village_ref`, `household_name`) preserved verbatim per ship-3 §6.1 sub-decision 2; this is what closes [FP-009](../flagged-positions.md#fp-009--conflictdetector-field-name-coupling) at Ship-3. v1 schema is *not* deleted — both versions coexist in the registry forever per §S10. |
| identity-conflict | FLAG | — | STABLE | ADR-002 S9 | round 0: FLAG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | probable duplicate subjects |
| identity-lifecycle | DERIVED | — | STABLE | phases/phase-1.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | merge/split transitions |
| identity-resolver | PRIMITIVE | — | STABLE | ADR-002 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | merge/split/alias management |
| identity-type-taxonomy | INVARIANT | — | STABLE | adr-002-identity-conflict.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 4 identity categories |
| informational-flag | CONFIG | — | STABLE | phases/phase-4.md | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | non-blocking severity |
| inner-platform-effect | OBSOLETE | — | OBSOLETE | ADR-004 Session 3 | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | anti-pattern alert |
| invariant | INVARIANT | — | STABLE | primitives.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | structural guarantee |
| json-schema | CONTRACT | — | STABLE | phases/phase-0.md | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | language-neutral contracts |
| knowledge-horizon | ALGORITHM | — | STABLE | phases/phase-1.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | device awareness cutoff |
| l0-assembly | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | configuration layer |
| l1-shape | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | configuration layer |
| l2-form-logic | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | configuration layer |
| l3-policy | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | configuration layer |
| lifecycle-state | DERIVED | — | STABLE | ADR-005 Session 2 | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | entity_lifecycle pattern state |
| lineage-dag-acyclic | INVARIANT | — | STABLE | raw-B | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | merge operands construction |
| lineage-graph | DERIVED | — | STABLE | adr-002-identity-conflict.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | identity merge/split history |
| location-hierarchy | DERIVED | — | STABLE | phases/phase-2.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | materialized path trees |
| logic-rule | CONFIG | — | STABLE | ADR-004 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L2 expression-based rule |
| manual-identity-conflict | DERIVED | — | STABLE | phases/phase-1.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | admin-triggered flag |
| manual-only | FLAG | — | STABLE | ADR-005 Session 1 | round 0: FLAG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | resolvability classification |
| manual-only-flag | CONFIG | — | STABLE | phases/phase-2.md | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | requires human resolution |
| materialized-path | ALGORITHM | — | STABLE | ADR-003 S3 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | hierarchy containment test |
| merge | ALGORITHM | — | STABLE | ADR-001 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | subject identity operation |
| merge-alias-projection | ALGORITHM | — | STABLE | raw-B | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | alias-in-projection strategy |
| merge-subject | ALGORITHM | — | STABLE | adr-002-identity-conflict.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | online-only operation |
| multi-actor-handoff | ALGORITHM | — | STABLE | ADR-005 Session 1 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | workflow step |
| multi-device | DERIVED | — | STABLE | phases/phase-1.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | concurrent capture scenario |
| multi-level-distribution | CONFIG | — | STABLE | raw-B | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | raw PATTERN→CONFIG (workflow pattern) |
| multi-step-approval | CONFIG | — | STABLE | ADR-005 Session 1 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | workflow pattern |
| multi-version-projection | ALGORITHM | — | STABLE | phases/phase-3.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | union projection |
| multi_version_projection | DERIVED | ADR-001 §S2, ADR-004 §S10 | STABLE | ships/ship-3.md | Ship-3 (2026-04-27): CREATED — DERIVED (ADR-001 §S2, ADR-004 §S10) | Ship-3 materialisation of the abstract `multi-version-projection` ALGORITHM row in live flow. Per-request projection routing on `shape_ref` (validation in [`ShapePayloadValidator`](../../server/src/main/java/dev/datarun/ship1/event/ShapePayloadValidator.java); pull-side scope filter via prefix match on `shape_ref` in [`SyncController.inScopeForPull`](../../server/src/main/java/dev/datarun/ship1/sync/SyncController.java); admin timeline via per-row `shape_ref` discrimination in `events.html`). Branching is on `shape_ref` everywhere (F-A2 honoured). **No projection cache** — the FP-002 (a) pattern continues; reads project from the event store on demand. Distinct from the abstract `multi-version-projection` ALGORITHM row. |
| no-pattern-activity | CONFIG | — | STABLE | adr-004-configuration-boundary.md | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | pattern: none |
| no-unmerge | INVARIANT | — | STABLE | raw-B | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | SubjectUnmerged does not exist |
| offline-first | INVARIANT | — | STABLE | principles.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | work disconnected |
| offline-first-architecture | INVARIANT | — | STABLE | adr-001-offline-data-model.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | events offline-capable |
| offline-local-state | PRIMITIVE | — | STABLE | phases/phase-0.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | complete device copy |
| online-only | RESERVED | — | STABLE | phases/phase-1.md | round 0: RESERVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | merge/split/resolution |
| operator-type-compatibility | ALGORITHM | — | STABLE | phases/phase-3.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | DtV rule |
| order-independent-sync | INVARIANT | — | STABLE | adr-001-offline-data-model.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | events carry ordering metadata |
| overlapping-authority-trap | OBSOLETE | — | OBSOLETE | ADR-004 Session 3 | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | anti-pattern alert |
| pattern | PRIMITIVE | ADR-009 §S3 | STABLE | ADR-004 | round 0: DISPUTED (A:PRIMITIVE, B:CONFIG, C2:PRIMITIVE); round 1: PRIMITIVE (ADR-009 §S3); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Platform-fixed mechanism (closed registry per ADR-005 §S5, deployer-referenced not deployer-authored). Parameters and roles stay CONFIG in their own rows. `workflow-pattern` is a synonym pointer (not promoted). |
| pattern-composition | ALGORITHM | — | STABLE | ADR-005 Session 2 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | composition rules |
| pattern-composition-rule | CONFIG | — | STABLE | raw-B | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 5 interaction rules |
| pattern-registry | PRIMITIVE | — | STABLE | ADR-005 Session 1 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | platform-fixed workflows |
| pattern-role | CONFIG | — | STABLE | ADR-005 Session 1 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | abstract role in pattern |
| pattern-state | DERIVED | — | STABLE | phases/phase-4.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | derived workflow state |
| pending-config | DERIVED | — | STABLE | phases/phase-3.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | staged package |
| periodic-capture | PRIMITIVE | — | STABLE | ADR-004 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | scheduled pattern |
| policy-routing | ALGORITHM | — | STABLE | phases/phase-4.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | non-recursive triggers |
| predicate | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | atomic comparison |
| predicate-budget | CONFIG | — | STABLE | phases/phase-3.md | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 3-predicate limit |
| process | PRIMITIVE | — | STABLE | ADR-001 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | identity category |
| process-identity | RESERVED | ADR-008 §S1 | STABLE | envelope.schema.json | round 0: RESERVED (inventory); round 1: RESERVED re-cited (ADR-008 §S1); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | `subject_ref.type = "process"` — present in the enum, no current emission site. Active set extension is architecture-grade. |
| projection | DERIVED | — | STABLE | ADR-001 | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | computed current state |
| projection-derived-state | INVARIANT | — | STABLE | adr-005-state-progression.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | never stored in payloads |
| projection-engine | PRIMITIVE | — | STABLE | ADR-001 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | state derivation |
| projection-equivalence | CONTRACT | — | STABLE | contracts/fixtures/projection-equivalence.json | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | server/mobile parity |
| projection-rebuild-strategy | OPEN | — | DEFERRED | raw-B | round 0: OPEN (inventory); round 1: DEFERRED (topology Bucket-D — IG-authority; blocker: IDR track after flag lifecycle settles) | Blocker: IDR track after flag lifecycle (ADR-006) settles — implementation-grade, not ADR-authority. |
| projection-rule | DERIVED | — | STABLE | ADR-004 Session 2 | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | source→derived mapping |
| provisional-release | RESERVED | — | DEFERRED | phases/phase-3.md | round 0: DEFERRED (inventory) | breaking change tooling (IG-7) |
| query-based-scope | CONFIG | — | STABLE | ADR-003 S3 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | conditional data access |
| raw-reference | ALGORITHM | — | STABLE | adr-002-identity-conflict.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | CD uses original subject_ref |
| read-discipline | RESERVED | — | STABLE | phases/phase-1.md | round 0: RESERVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | all reads defensive |
| replay | ALGORITHM | — | STABLE | phases/phase-0.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | reconstruct from events |
| resolvability-classification | INVARIANT | — | STABLE | adr-005-state-progression.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | auto_eligible or manual_only |
| responsibility-binding | DERIVED | — | STABLE | phases/phase-2.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | actor→scope assignment |
| review | CONTRACT | — | STABLE | ADR-004 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | envelope type |
| review-state | DERIVED | — | STABLE | ADR-005 Session 2 | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | event-level state |
| role | DERIVED | — | STABLE | phases/phase-4.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | actor capability class |
| role-action-enforcement | ALGORITHM | — | STABLE | phases/phase-4.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | IDR-021 implementation |
| role-action-permission | CONFIG | — | STABLE | ADR-003 S8 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | role×action matrix |
| role-action-table | CONFIG | — | STABLE | raw-B | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | deployer matrix |
| role-stale | FLAG | ADR-006 §S2 | STABLE | ADR-003 S7 | round 0: FLAG (inventory); round 1: class-membership cited (ADR-006 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | role changed flag; canonical over `role-staleness` (OBSOLETE) |
| role-staleness | OBSOLETE | ADR-006 §Consequences | OBSOLETE | ADR-003 S7 | round 0: FLAG (inventory); round 1: OBSOLETE (ADR-006 §Consequences) | Synonym of `role-stale` — deduped in ADR-006. Row retained for traceability per ledger rule 5. |
| s00 | RESERVED | — | STABLE | principles.md | round 0: RESERVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | simplicity benchmark |
| schema-evolution-trap | OBSOLETE | — | OBSOLETE | ADR-004 Session 3 | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | anti-pattern alert |
| schema-versioning | CONFIG | — | STABLE | ADR-004 S6 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | shape_name/v{N} format |
| scope | PRIMITIVE | ADR-009 §S2 | STABLE | ADR-003 | round 0: DISPUTED (C1:PRIMITIVE, C2:CONFIG); round 1: PRIMITIVE (ADR-009 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Platform-fixed mechanism — scope-type registry closed at 3 values (ADR-003 §S7, ADR-004 §S7). Security-critical closure (leak-vulnerability surface). Instance rows `geographic-scope`, `subject-list-scope`, `scope-composition`, `scope-type`, `temporal-access-bounds` stay CONFIG. |
| scope-composition | CONFIG | — | STABLE | adr-003-authorization-sync.md | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | combining scope types |
| scope-containment | ALGORITHM | — | STABLE | ADR-003 S5 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | access control test |
| scope-containment-invariant | INVARIANT | — | STABLE | ADR-003 S5 | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | foundational rule |
| scope-containment-test | ALGORITHM | — | STABLE | raw-B | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | scope filtering |
| scope-equality | INVARIANT | — | STABLE | adr-003-authorization-sync.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | sync = access scope |
| scope-resolver | PRIMITIVE | — | STABLE | ADR-003 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | determines scope |
| scope-staleness | FLAG | — | STABLE | ADR-003 S7 | round 0: FLAG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | reassigned actor flag |
| scope-stale | FLAG | — | STABLE | raw-B | round 0: FLAG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | alt name for scope_violation |
| scope-subject-based | CONFIG | — | STABLE | ADR-003 S3 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | explicit subject list |
| scope-type | CONFIG | — | STABLE | raw-B | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | platform-fixed vocabulary |
| scope-violation | FLAG | ADR-006 §S2 | STABLE | ADR-003 S7 | round 0: FLAG (inventory); round 1: class-membership cited (ADR-006 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | unauthorized access flag category |
| selective-retain | ALGORITHM | — | STABLE | ADR-003 S6 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | device purge strategy |
| sensitivity-classification | CONFIG | — | STABLE | phases/phase-3d.md | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | shape/activity sensitivity |
| sensitive-subject-classification | OPEN | — | DEFERRED | raw-B | round 0: OPEN (inventory); round 1: DEFERRED (topology Bucket-B — subject-level sensitivity; blocker: Phase 4 workflow & policy) | Blocker: Phase 4 workflow & policy design surface. |
| shape | PRIMITIVE | — | STABLE | ADR-004 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | payload schema |
| shape-binding | ALGORITHM | — | STABLE | phases/phase-4.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | transition vs activation |
| shape-definition | CONFIG | — | STABLE | raw-B | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | versioned schema |
| shape-payload-validator | PRIMITIVE | — | STABLE | ADR-004 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | validates against shape |
| shape-ref | CONTRACT | — | STABLE | ADR-004 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | mandatory envelope field |
| shape-registry | PRIMITIVE | — | STABLE | ADR-004 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | stores definitions |
| shape-role | DERIVED | — | STABLE | phases/phase-4.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | abstract→concrete mapping |
| shape-version | DERIVED | — | STABLE | phases/phase-3.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | timestamped snapshot |
| shape-versioning | CONFIG | — | STABLE | ADR-004 S6 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | additive/deprecation/breaking |
| shape_registry | CONFIG | ADR-004 §S1, ADR-004 §S10 | STABLE | ships/ship-3.md | Ship-3 (2026-04-27): CREATED — CONFIG-storage (ADR-004 §S1, ADR-004 §S10) | Ship-3 instantiation of the abstract `shape-registry` PRIMITIVE row as the storage of deployer-authored shapes. **Pattern (a) per ship-3 §6.1 sub-decision 3** — JAR-bundled fixture continued one Ship as a named expedient: schemas live under `server/src/main/resources/schemas/shapes/` (mirrored at `contracts/shapes/` under [FP-007](../flagged-positions.md#fp-007--contractserver-resource-shape-drift-not-enforced)'s drift gate), classpath-loaded at boot by [`ShapePayloadValidator`](../../server/src/main/java/dev/datarun/ship1/event/ShapePayloadValidator.java), no projection cache. All versions remain in the registry forever per [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution). Persistence-outside-the-JAR lands at the Ship that closes [FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) (deployer-authoring surface) alongside [FP-011](../flagged-positions.md#fp-011--household_observation-directory-classification-re-deferral) directory split. Distinct from the abstract `shape-registry` PRIMITIVE row. |
| show-condition | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L2 visibility rule |
| single-source-of-truth | INVARIANT | — | STABLE | raw-B | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | events authoritative |
| single-writer-resolution | INVARIANT | — | STABLE | raw-B | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | one resolver per flag |
| source-chain-traversal | ALGORITHM | — | STABLE | ADR-005 Session 2 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | upstream flag detection |
| source-only-flagging | ALGORITHM | — | STABLE | ADR-005 Session 2 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | root-cause flagging |
| split | ALGORITHM | — | STABLE | ADR-001 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | subject identity operation |
| split-freezes-history | INVARIANT | — | STABLE | raw-B | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | source archival |
| split-subject | ALGORITHM | — | STABLE | adr-002-identity-conflict.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | online-only operation |
| stale-reference | FLAG | ADR-006 §S2 | STABLE | ADR-002 S8 | round 0: FLAG (inventory); round 1: class-membership cited (ADR-006 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | entity update lag |
| state-as-projection | ALGORITHM | — | STABLE | ADR-005 Session 1 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | derived from events |
| state-machine | PRIMITIVE | — | STABLE | ADR-005 Session 1 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | named states + transitions |
| state-progression | ALGORITHM | — | STABLE | phases/phase-4.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | work lifecycle |
| subject | PRIMITIVE | — | STABLE | ADR-001 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | identity category |
| subject-based-scope | CONFIG | — | STABLE | ADR-003 S3 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | explicit list type |
| subject-binding | DERIVED | — | STABLE | phases/phase-3.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | shape field mapping |
| subject-deactivated | FLAG | — | STABLE | ADR-002 S9 | round 0: FLAG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | decommissioned entity |
| subject-identity-resolver | PRIMITIVE | — | STABLE | ADR-002 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | merge/split validator |
| subject-level-pattern | CONFIG | — | STABLE | raw-B | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | subject-scoped workflow |
| subject-level-state | DERIVED | — | STABLE | ADR-005 Session 2 | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | lifecycle projection |
| subject-lifecycle | DERIVED | ADR-001 §S2 | STABLE | ships/ship-2.md | Ship-2 (2026-04-26): CREATED — DERIVED (ADR-001 §S2) | **Cache intentionally declined** per FP-002 (a) locked at Ship-2 OQ-1; lifecycle is replayed from `subjects_merged/v1` / `subject_split/v1` on demand (the [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) precedent). No `subject_lifecycle` table. ADR-001 §S2 escape hatch B→C remains available if a future Ship's fixture surfaces read cost; until then the row is DERIVED, not a CONFIG cache. Closest conventional ledger status (no "DECLINED-CACHE" slot exists); the cache-declination decision is recorded in this note. |
| subject-list-scope | CONFIG | — | STABLE | ADR-003 S3 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | explicit subject type |
| subject-ref | CONTRACT | ADR-008 §S1 | STABLE | ADR-001 | round 0: DISPUTED (A:PRIMITIVE, C1:CONTRACT, C2:PRIMITIVE); round 1: CONTRACT (ADR-008 §S1); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Reference-vs-referent rule: `subject_ref` is the envelope field contract; `subject` (separate row) remains PRIMITIVE. Type enum closed at 4 values; `process` RESERVED (no current emission). |
| subject-split | CONTRACT | ADR-007 §S2 | STABLE | ADR-002 S9 | round 0: CONTRACT (inventory); round 1: CONTRACT re-cited (ADR-007 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Platform-bundled shape on envelope type=capture. |
| subjects-merged | CONTRACT | ADR-007 §S2 | STABLE | ADR-002 S9 | round 0: CONTRACT (inventory); round 1: CONTRACT re-cited (ADR-007 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | Platform-bundled shape on envelope type=capture. |
| sync-contract | CONTRACT | — | STABLE | raw-B | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 8 guarantees |
| sync-protocol | ALGORITHM | — | STABLE | phases/phase-0.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | watermark-based exchange |
| sync-scope | INVARIANT | — | STABLE | ADR-003 | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | data device receives |
| sync-unit | INVARIANT | — | STABLE | adr-001-offline-data-model.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | immutable event |
| sync-watermark | CONTRACT | — | STABLE | ADR-001 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | server sequence number |
| system-actor | ALGORITHM | — | STABLE | ADR-004 S4 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | system: prefix format |
| sweep-job | ALGORITHM | — | STABLE | phases/phase-1.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | periodic CD re-evaluation |
| task-completed | CONTRACT | — | STABLE | ADR-004 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | envelope type |
| task-created | CONTRACT | — | STABLE | ADR-004 | round 0: CONTRACT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | envelope type |
| temporal-access-bounds | CONFIG | — | STABLE | ADR-003 S4 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | valid_from/valid_until |
| temporal-authority-expired | FLAG | ADR-006 §S2 | STABLE | ADR-003 S7 | round 0: FLAG (inventory); round 1: class-membership cited (ADR-006 §S2); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | assignment ended |
| three-tier-hierarchy | CONFIG | — | STABLE | ADR-003 S3 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | organizational structure |
| transaction-boundary | ALGORITHM | — | STABLE | phases/phase-1.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | 2-Tx pipeline |
| transfer-with-acknowledgment | CONFIG | — | STABLE | ADR-005 Session 2 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | handoff pattern |
| transition-violation | FLAG | ADR-006 §S2 | DEFERRED | ADR-005 Session 1 | round 0: FLAG (inventory); round 1: class-membership cited (ADR-006 §S2) | invalid state transition; emission deferred to Phase 4 per flag-catalog |
| trigger | PRIMITIVE | — | STABLE | ADR-004 S7 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L3 reactive rule |
| trigger-budget | CONFIG | — | STABLE | phases/phase-4.md | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | hard complexity limits |
| trigger-context | PRIMITIVE | — | STABLE | phases/phase-4.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | expression evaluation |
| trigger-engine | PRIMITIVE | — | STABLE | ADR-004 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | server-side processing |
| trigger-escalation-trap | OBSOLETE | — | OBSOLETE | ADR-004 Session 3 | round 0: OPEN (inventory); round 1: OBSOLETE (topology Bucket-A — anti-pattern alert or convergence-protocol step, not a platform concept) | anti-pattern alert |
| trustee-records | INVARIANT | — | STABLE | principles.md | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | audit trail requirement |
| two-slot-config | ALGORITHM | — | STABLE | phases/phase-3.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | current + pending |
| two-tier-sync | ALGORITHM | — | STABLE | ADR-003 S6 | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | asymmetric model |
| type-vocabulary | INVARIANT | ADR-007 §S1 | STABLE | ADR-004 S3 | round 0: INVARIANT (inventory); round 1: INVARIANT re-cited (ADR-007 §S1); round 2: STABLE (quiet-scan promotion — no upstream ADR churn, no classification change) | 6-value envelope type closure. ADR-004 §S3 remains first-decision cite; ADR-007 §S1 is the canonical cite. |
| union-projection | ALGORITHM | — | STABLE | phases/phase-3.md | round 0: ALGORITHM (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | multi-version shapes |
| validation-engine | PRIMITIVE | — | STABLE | ADR-004 | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | constraint evaluator |
| warning-expression | PRIMITIVE | — | STABLE | phases/phase-3.md | round 0: PRIMITIVE (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | L2 conditional warning |
| watermark | DERIVED | — | STABLE | phases/phase-0.md | round 0: DERIVED (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | pagination key |
| workflow-pattern | CONFIG | — | STABLE | ADR-005 Session 1 | round 0: CONFIG (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | synonym of pattern |
| write-path-discipline | INVARIANT | — | STABLE | ADR-001 | round 0: INVARIANT (inventory); round 1: STABLE (fixpoint — unchanged under round 1, upstream ADR not superseded) | events sole write path |

