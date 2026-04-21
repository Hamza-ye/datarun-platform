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
| accept-and-flag | DISPUTED | — | OPEN | ADR-002 | round 0: DISPUTED (A:ALGORITHM, B:INVARIANT, C1:ALGORITHM, C2:ALGORITHM) | 3 sources ALGORITHM, 1 INVARIANT. Harvest: the property is INVARIANT (ADR-002 S14 / archive-09 Q12); the detection procedure is ALGORITHM (conflict-detection, separate row). Recommend: INVARIANT. See [disputes-harvest.md §Group-1](inventory/disputes-harvest.md) |
| active-assignment | DERIVED | — | PROPOSED | phases/phase-2.md | round 0: DERIVED (inventory) | — |
| activity | DISPUTED | — | OPEN | ADR-004 | round 0: DISPUTED (A:PRIMITIVE, C1:CONFIG) | Harvest: deployer-bundled L0 assembly (shape+pattern+role+scope). Archive 13 Q11 framed as config. Recommend: CONFIG (activity-ref stays CONTRACT). See [disputes-harvest.md §Group-3](inventory/disputes-harvest.md) |
| activity-ref | DISPUTED | — | OPEN | ADR-004 | round 0: DISPUTED (A:CONTRACT, B:RESERVED, C1:CONTRACT) | Harvest: fully live (Phase 3d.1). Subagent B RESERVED framing is artifact, no archive evidence. Recommend: CONTRACT. See [disputes-harvest.md §Group-2](inventory/disputes-harvest.md) |
| actor | INVARIANT | — | PROPOSED | adr-002-identity-conflict.md | round 0: INVARIANT (inventory) | persistent identity category |
| actor-ref | DISPUTED | — | OPEN | ADR-004 | round 0: DISPUTED (A:CONTRACT, B:RESERVED, C1:CONTRACT) | Harvest: human UUID + `system:{component}/{id}` both live (ADR-004 S4, Archive 16/18). Recommend: CONTRACT. See [disputes-harvest.md §Group-2](inventory/disputes-harvest.md) |
| actor-token | CONTRACT | — | PROPOSED | phases/phase-2.md | round 0: CONTRACT (inventory) | — |
| alias-cache | DERIVED | — | PROPOSED | phases/phase-1.md | round 0: DERIVED (inventory) | in-memory map for merge resolution |
| alias-mapping | DERIVED | — | PROPOSED | adr-002-identity-conflict.md | round 0: DERIVED (inventory) | merge resolution projection |
| alias-resolution | ALGORITHM | — | PROPOSED | raw-B | round 0: ALGORITHM (inventory) | single-hop lookup |
| alias-table | CONTRACT | — | PROPOSED | phases/phase-1.md | round 0: CONTRACT (inventory) | materialized persistence layer |
| alert | CONTRACT | — | PROPOSED | phases/phase-4.md | round 0: CONTRACT (inventory) | envelope type (Phase 4) |
| anti-pattern | OPEN | — | OPEN | ADR-004 Session 3 | round 0: OPEN (inventory) | 6 catalogued pitfalls |
| append-only | INVARIANT | — | PROPOSED | phases/phase-0.md | round 0: INVARIANT (inventory) | foundational write discipline |
| approval-chain | ALGORITHM | — | PROPOSED | ADR-005 Session 1 | round 0: ALGORITHM (inventory) | multi-level workflow |
| assignment | INVARIANT | — | PROPOSED | ADR-001 | round 0: INVARIANT (inventory) | temporal identity category |
| assignment-based-access | CONTRACT | — | PROPOSED | raw-B | round 0: CONTRACT (inventory) | scope bindings via events |
| assignment-changed | CONTRACT | — | PROPOSED | ADR-003 | round 0: CONTRACT (inventory) | envelope type |
| assignment-created | CONFIG | — | PROPOSED | shapes/assignment_created.schema.json | round 0: CONFIG (inventory) | shape definition |
| assignment-ended | CONFIG | — | PROPOSED | shapes/assignment_ended.schema.json | round 0: CONFIG (inventory) | shape definition |
| assignment-lifecycle | DERIVED | — | PROPOSED | phases/phase-2.md | round 0: DERIVED (inventory) | projected from events |
| assignment-resolver | PRIMITIVE | — | PROPOSED | ADR-005 Session 1 | round 0: PRIMITIVE (inventory) | validates role-holders |
| authority-as-projection | ALGORITHM | — | PROPOSED | ADR-003 S12 | round 0: ALGORITHM (inventory) | reconstructed from timeline |
| authority-context | DERIVED | — | PROPOSED | ADR-003 | round 0: DERIVED (inventory) | per-event authorization |
| auto-eligible-flag | CONFIG | — | PROPOSED | phases/phase-2.md | round 0: CONFIG (inventory) | resolvability classification |
| auto-resolution | ALGORITHM | — | PROPOSED | ADR-005 Session 1 | round 0: ALGORITHM (inventory) | L3b deadline-watch |
| auto-resolution-policy | ALGORITHM | — | PROPOSED | phases/phase-4.md | round 0: ALGORITHM (inventory) | auto-eligible flag resolution |
| boundary-mapping | OPEN | — | OPEN | raw-B | round 0: OPEN (inventory) | consolidation framework step |
| breaking-change | CONFIG | — | PROPOSED | ADR-004 Session 2 | round 0: CONFIG (inventory) | schema evolution mode |
| capture | CONTRACT | — | PROPOSED | phases/phase-0.md | round 0: CONTRACT (inventory) | envelope type |
| capture-only | PRIMITIVE | — | PROPOSED | ADR-004 | round 0: PRIMITIVE (inventory) | operational pattern |
| capture-with-review | PRIMITIVE | — | PROPOSED | ADR-004 | round 0: PRIMITIVE (inventory) | operational pattern |
| case-management | CONFIG | — | PROPOSED | ADR-005 Session 1 | round 0: CONFIG (inventory) | workflow pattern |
| causal-ordering | ALGORITHM | — | PROPOSED | ADR-002 | round 0: ALGORITHM (inventory) | device_seq + sync_watermark |
| command-validator | DERIVED | — | PROPOSED | primitives.md | round 0: DERIVED (inventory) | device-side advisory |
| competing-patterns | INVARIANT | — | PROPOSED | phases/phase-4.md | round 0: INVARIANT (inventory) | composition rule 5 |
| complex-composition | ALGORITHM | — | PROPOSED | ADR-005 Session 2 | round 0: ALGORITHM (inventory) | multi-pattern on same subject |
| composition-rule | CONTRACT | — | PROPOSED | phases/phase-4.md | round 0: CONTRACT (inventory) | 5 pattern interaction rules |
| concurrent-state-change | FLAG | — | PROPOSED | ADR-002 S7 | round 0: FLAG (inventory) | structural conflict |
| config-gradient | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | four-layer configuration model |
| config-package | CONTRACT | — | PROPOSED | phases/phase-3.md | round 0: CONTRACT (inventory) | atomic delivery structure |
| config-packager | PRIMITIVE | — | PROPOSED | adr-004-configuration-boundary.md | round 0: PRIMITIVE (inventory) | assembles atomic deliverable |
| config-version | DERIVED | — | PROPOSED | phases/phase-3.md | round 0: DERIVED (inventory) | monotonic versioning |
| configuration-boundary | INVARIANT | — | PROPOSED | adr-004-configuration-boundary.md | round 0: INVARIANT (inventory) | four-layer separation |
| configuration-delivery-pipeline | ALGORITHM | — | PROPOSED | adr-004-configuration-boundary.md | round 0: ALGORITHM (inventory) | author→validate→package→deliver→apply |
| configuration-specialist-trap | OPEN | — | OPEN | ADR-004 Session 3 | round 0: OPEN (inventory) | anti-pattern alert |
| conflict | FLAG | — | PROPOSED | ADR-001 | round 0: FLAG (inventory) | anomaly for resolution |
| conflict-detected | DISPUTED | — | OPEN | ADR-002 | round 0: DISPUTED (A:CONTRACT, B:SHAPE→CONTRACT, C1:CONFIG, C2:PRIMITIVE) | Harvest: already settled by ADR-002 Addendum (2026-04-21) as platform-bundled shape. Dispute = un-migrated prose layers. Recommend: CONTRACT, resolve to STABLE round 1. See [disputes-harvest.md §Group-1](inventory/disputes-harvest.md) |
| conflict-detection | ALGORITHM | — | PROPOSED | ADR-002 | round 0: ALGORITHM (inventory) | process raising flags |
| conflict-detector | PRIMITIVE | — | PROPOSED | adr-002-identity-conflict.md | round 0: PRIMITIVE (inventory) | evaluates anomalies |
| conflict-resolution | ALGORITHM | — | PROPOSED | ADR-002 | round 0: ALGORITHM (inventory) | ConflictResolved workflow |
| conflict-resolved | CONTRACT | — | PROPOSED | ADR-002 | round 0: CONTRACT (inventory) | platform-bundled shape |
| context-properties | DERIVED | — | PROPOSED | raw-B | round 0: DERIVED (inventory) | 7 platform-fixed values |
| context-resolver | PRIMITIVE | — | PROPOSED | phases/phase-3d.md | round 0: PRIMITIVE (inventory) | resolves context.* at form-open |
| context-scope | DERIVED | — | PROPOSED | adr-005-state-progression.md | round 0: DERIVED (inventory) | form evaluation scope |
| contract | INVARIANT | — | PROPOSED | contracts.md | round 0: INVARIANT (inventory) | inter-primitive guarantee |
| cross-activity-link | ALGORITHM | — | PROPOSED | phases/phase-4.md | round 0: ALGORITHM (inventory) | composition rule 4 |
| cross-cutting | INVARIANT | — | PROPOSED | cross-cutting.md | round 0: INVARIANT (inventory) | 8 spanning concerns |
| custom-shape | DERIVED | — | PROPOSED | phases/phase-3.md | round 0: DERIVED (inventory) | deployer-authored definition |
| deadline-check | CONFIG | — | PROPOSED | ADR-004 S7 | round 0: CONFIG (inventory) | L3b trigger type |
| deadline-trigger | CONFIG | — | PROPOSED | ADR-005 Session 1 | round 0: CONFIG (inventory) | time-based reaction |
| decision-harvest | OPEN | — | OPEN | raw-B | round 0: OPEN (inventory) | convergence framework step |
| default-expression | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | L2 form rule |
| deploy-time-validator | PRIMITIVE | — | PROPOSED | ADR-004 | round 0: PRIMITIVE (inventory) | complexity gating |
| deployment-parameterization | CONFIG | — | PROPOSED | raw-B | round 0: CONFIG (inventory) | deployer configuration |
| deprecated-shape | DERIVED | — | PROPOSED | phases/phase-3.md | round 0: DERIVED (inventory) | unavailable for new events |
| deprecation-change | CONFIG | — | PROPOSED | ADR-004 Session 2 | round 0: CONFIG (inventory) | schema evolution mode |
| deprecation-only-evolution | ALGORITHM | — | PROPOSED | raw-B | round 0: ALGORITHM (inventory) | default shape versioning strategy |
| detect-before-act | INVARIANT | — | PROPOSED | ADR-002 S12 | round 0: INVARIANT (inventory) | flagged events excluded from state/policy |
| device-id | CONTRACT | — | PROPOSED | ADR-001 | round 0: CONTRACT (inventory) | hardware-bound UUID |
| device-identity | RESERVED | — | PROPOSED | raw-B | round 0: RESERVED (inventory) | hardware-bound persisted identity |
| device-seq | CONTRACT | — | PROPOSED | ADR-001 | round 0: CONTRACT (inventory) | per-device monotonic integer |
| device-sequence | CONTRACT | — | PROPOSED | adr-002-identity-conflict.md | round 0: CONTRACT (inventory) | synonym of device-seq |
| device-sharing | RESERVED | — | DEFERRED | phases/phase-2.md | round 0: DEFERRED (inventory) | per-actor sync sessions (IG-14) |
| device-time | INVARIANT | — | PROPOSED | ADR-001 S5 | round 0: INVARIANT (inventory) | advisory-only timestamp |
| domain-uniqueness-constraint | CONFIG | — | PROPOSED | ADR-004 Session 3 Part 4 | round 0: CONFIG (inventory) | deployer-defined rule |
| domain-uniqueness-violation | FLAG | — | PROPOSED | ADR-004 Session 3 Part 4 | round 0: FLAG (inventory) | flag category (entry 7) |
| draft-config | DERIVED | — | PROPOSED | phases/phase-3.md | round 0: DERIVED (inventory) | pending package on device |
| duplicate-candidate-identified | FLAG | — | PROPOSED | ADR-002 S9 | round 0: FLAG (inventory) | merge nomination event |
| entity-lifecycle | PRIMITIVE | — | PROPOSED | ADR-005 Session 2 | round 0: PRIMITIVE (inventory) | pattern for static entities |
| envelope | CONTRACT | — | PROPOSED | ADR-001 | round 0: CONTRACT (inventory) | 11-field event wrapper |
| event | PRIMITIVE | — | PROPOSED | ADR-001 | round 0: PRIMITIVE (inventory) | immutable record |
| event-assembler | ALGORITHM | — | PROPOSED | phases/phase-3d.md | round 0: ALGORITHM (inventory) | constructs 11-field envelope |
| event-id | CONTRACT | — | PROPOSED | adr-001-offline-data-model.md | round 0: CONTRACT (inventory) | client-generated UUID |
| event-level-pattern | CONFIG | — | PROPOSED | ADR-005 Session 1 | round 0: CONFIG (inventory) | per-event state pattern |
| event-level-state | DERIVED | — | PROPOSED | ADR-005 Session 2 | round 0: DERIVED (inventory) | event-scoped state |
| event-reaction | ALGORITHM | — | PROPOSED | ADR-004 S7 | round 0: ALGORITHM (inventory) | L3a synchronous trigger |
| event-reaction-trigger | CONFIG | — | PROPOSED | ADR-005 Session 1 | round 0: CONFIG (inventory) | L3a trigger type |
| event-ref | CONTRACT | — | PROPOSED | adr-001-offline-data-model.md | round 0: CONTRACT (inventory) | cross-event reference |
| event-sourcing | INVARIANT | — | PROPOSED | phases/phase-0.md | round 0: INVARIANT (inventory) | all state from events |
| event-store | PRIMITIVE | — | PROPOSED | ADR-001 | round 0: PRIMITIVE (inventory) | append-only persistence |
| event-type | CONTRACT | — | PROPOSED | ADR-004 S3 | round 0: CONTRACT (inventory) | closed 6-value vocabulary |
| expression-evaluator | PRIMITIVE | — | PROPOSED | ADR-004 S8 | round 0: PRIMITIVE (inventory) | pure-function AST evaluator |
| expression-language | ALGORITHM | — | PROPOSED | ADR-004 S8 | round 0: ALGORITHM (inventory) | single language, two contexts |
| file-pattern | RESERVED | — | DEFERRED | phases/phase-4.md | round 0: DEFERRED (inventory) | entity_lifecycle alternative (Phase 4+) |
| field-budget | INVARIANT | — | PROPOSED | ADR-004 S10 | round 0: INVARIANT (inventory) | 60-field limit per shape |
| field-reference | CONTRACT | — | PROPOSED | ADR-004 S8 | round 0: CONTRACT (inventory) | expression component |
| filter-predicate | ALGORITHM | — | PROPOSED | phases/phase-3.md | round 0: ALGORITHM (inventory) | boolean condition in expression |
| flag | DISPUTED | — | OPEN | ADR-001 | round 0: DISPUTED (B:DERIVED, C1:INVARIANT, C2:PRIMITIVE) | Harvest: all 3 readings true at different levels (invariant-property / derived-instance / primitive-category). Archive 09 Q12 locks INVARIANT. Recommend: INVARIANT; delegate DERIVED to `conflict-detector`, PRIMITIVE to `alert`/`type-vocabulary`. See [disputes-harvest.md §Group-1](inventory/disputes-harvest.md) |
| flag-cascade-contamination | DERIVED | — | PROPOSED | ADR-005 Session 2 | round 0: DERIVED (inventory) | projection property |
| flag-catalog | CONTRACT | — | PROPOSED | flag-catalog.md | round 0: CONTRACT (inventory) | 9-category register |
| flag-category | CONTRACT | — | PROPOSED | conflict_detected.schema.json | round 0: CONTRACT (inventory) | enumerated anomaly type |
| flag-creation-location | OPEN | — | OPEN | raw-B | round 0: OPEN (inventory) | server vs device decision |
| flag-exclusion | ALGORITHM | — | PROPOSED | phases/phase-1.md | round 0: ALGORITHM (inventory) | flagged from state derivation |
| flag-resolvability-classification | DERIVED | — | PROPOSED | raw-B | round 0: DERIVED (inventory) | auto_eligible vs manual_only |
| flag-severity | CONFIG | — | PROPOSED | phases/phase-4.md | round 0: CONFIG (inventory) | blocking vs informational |
| flagged-event-exclusion | ALGORITHM | — | PROPOSED | phases/phase-1.md | round 0: ALGORITHM (inventory) | remove from state |
| form-context | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | expression evaluation scope |
| form-engine | ALGORITHM | — | PROPOSED | phases/phase-3.md | round 0: ALGORITHM (inventory) | shape→form renderer |
| form-logic | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | L2 expressions |
| forward-compatibility | INVARIANT | — | PROPOSED | phases/phase-3.md | round 0: INVARIANT (inventory) | config package design |
| four-layer-gradient | CONFIG | — | PROPOSED | ADR-004 S5 | round 0: CONFIG (inventory) | L0-L3 configuration |
| gap-identification | OPEN | — | OPEN | raw-B | round 0: OPEN (inventory) | convergence framework step |
| geographic-scope | CONFIG | — | PROPOSED | ADR-003 S3 | round 0: CONFIG (inventory) | location hierarchy type |
| greenspun-rule | OPEN | — | OPEN | ADR-004 Session 3 | round 0: OPEN (inventory) | anti-pattern alert |
| hardware-bound-identity | INVARIANT | — | PROPOSED | adr-002-identity-conflict.md | round 0: INVARIANT (inventory) | device_id uniqueness |
| identity-conflict | FLAG | — | PROPOSED | ADR-002 S9 | round 0: FLAG (inventory) | probable duplicate subjects |
| identity-lifecycle | DERIVED | — | PROPOSED | phases/phase-1.md | round 0: DERIVED (inventory) | merge/split transitions |
| identity-resolver | PRIMITIVE | — | PROPOSED | ADR-002 | round 0: PRIMITIVE (inventory) | merge/split/alias management |
| identity-type-taxonomy | INVARIANT | — | PROPOSED | adr-002-identity-conflict.md | round 0: INVARIANT (inventory) | 4 identity categories |
| informational-flag | CONFIG | — | PROPOSED | phases/phase-4.md | round 0: CONFIG (inventory) | non-blocking severity |
| inner-platform-effect | OPEN | — | OPEN | ADR-004 Session 3 | round 0: OPEN (inventory) | anti-pattern alert |
| invariant | INVARIANT | — | PROPOSED | primitives.md | round 0: INVARIANT (inventory) | structural guarantee |
| json-schema | CONTRACT | — | PROPOSED | phases/phase-0.md | round 0: CONTRACT (inventory) | language-neutral contracts |
| knowledge-horizon | ALGORITHM | — | PROPOSED | phases/phase-1.md | round 0: ALGORITHM (inventory) | device awareness cutoff |
| l0-assembly | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | configuration layer |
| l1-shape | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | configuration layer |
| l2-form-logic | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | configuration layer |
| l3-policy | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | configuration layer |
| lifecycle-state | DERIVED | — | PROPOSED | ADR-005 Session 2 | round 0: DERIVED (inventory) | entity_lifecycle pattern state |
| lineage-dag-acyclic | INVARIANT | — | PROPOSED | raw-B | round 0: INVARIANT (inventory) | merge operands construction |
| lineage-graph | DERIVED | — | PROPOSED | adr-002-identity-conflict.md | round 0: DERIVED (inventory) | identity merge/split history |
| location-hierarchy | DERIVED | — | PROPOSED | phases/phase-2.md | round 0: DERIVED (inventory) | materialized path trees |
| logic-rule | CONFIG | — | PROPOSED | ADR-004 | round 0: CONFIG (inventory) | L2 expression-based rule |
| manual-identity-conflict | DERIVED | — | PROPOSED | phases/phase-1.md | round 0: DERIVED (inventory) | admin-triggered flag |
| manual-only | FLAG | — | PROPOSED | ADR-005 Session 1 | round 0: FLAG (inventory) | resolvability classification |
| manual-only-flag | CONFIG | — | PROPOSED | phases/phase-2.md | round 0: CONFIG (inventory) | requires human resolution |
| materialized-path | ALGORITHM | — | PROPOSED | ADR-003 S3 | round 0: ALGORITHM (inventory) | hierarchy containment test |
| merge | ALGORITHM | — | PROPOSED | ADR-001 | round 0: ALGORITHM (inventory) | subject identity operation |
| merge-alias-projection | ALGORITHM | — | PROPOSED | raw-B | round 0: ALGORITHM (inventory) | alias-in-projection strategy |
| merge-subject | ALGORITHM | — | PROPOSED | adr-002-identity-conflict.md | round 0: ALGORITHM (inventory) | online-only operation |
| multi-actor-handoff | ALGORITHM | — | PROPOSED | ADR-005 Session 1 | round 0: ALGORITHM (inventory) | workflow step |
| multi-device | DERIVED | — | PROPOSED | phases/phase-1.md | round 0: DERIVED (inventory) | concurrent capture scenario |
| multi-level-distribution | CONFIG | — | PROPOSED | raw-B | round 0: CONFIG (inventory) | raw PATTERN→CONFIG (workflow pattern) |
| multi-step-approval | CONFIG | — | PROPOSED | ADR-005 Session 1 | round 0: CONFIG (inventory) | workflow pattern |
| multi-version-projection | ALGORITHM | — | PROPOSED | phases/phase-3.md | round 0: ALGORITHM (inventory) | union projection |
| no-pattern-activity | CONFIG | — | PROPOSED | adr-004-configuration-boundary.md | round 0: CONFIG (inventory) | pattern: none |
| no-unmerge | INVARIANT | — | PROPOSED | raw-B | round 0: INVARIANT (inventory) | SubjectUnmerged does not exist |
| offline-first | INVARIANT | — | PROPOSED | principles.md | round 0: INVARIANT (inventory) | work disconnected |
| offline-first-architecture | INVARIANT | — | PROPOSED | adr-001-offline-data-model.md | round 0: INVARIANT (inventory) | events offline-capable |
| offline-local-state | PRIMITIVE | — | PROPOSED | phases/phase-0.md | round 0: PRIMITIVE (inventory) | complete device copy |
| online-only | RESERVED | — | PROPOSED | phases/phase-1.md | round 0: RESERVED (inventory) | merge/split/resolution |
| operator-type-compatibility | ALGORITHM | — | PROPOSED | phases/phase-3.md | round 0: ALGORITHM (inventory) | DtV rule |
| order-independent-sync | INVARIANT | — | PROPOSED | adr-001-offline-data-model.md | round 0: INVARIANT (inventory) | events carry ordering metadata |
| overlapping-authority-trap | OPEN | — | OPEN | ADR-004 Session 3 | round 0: OPEN (inventory) | anti-pattern alert |
| pattern | DISPUTED | — | OPEN | ADR-004 | round 0: DISPUTED (A:PRIMITIVE, B:CONFIG, C2:PRIMITIVE) | Harvest: ADR-005 S5 — closed registry, deployer-referenced, not deployer-authored. Recommend: PRIMITIVE (consistent with `pattern-registry`). Parameters stay CONFIG. See [disputes-harvest.md §Group-3](inventory/disputes-harvest.md) |
| pattern-composition | ALGORITHM | — | PROPOSED | ADR-005 Session 2 | round 0: ALGORITHM (inventory) | composition rules |
| pattern-composition-rule | CONFIG | — | PROPOSED | raw-B | round 0: CONFIG (inventory) | 5 interaction rules |
| pattern-registry | PRIMITIVE | — | PROPOSED | ADR-005 Session 1 | round 0: PRIMITIVE (inventory) | platform-fixed workflows |
| pattern-role | CONFIG | — | PROPOSED | ADR-005 Session 1 | round 0: CONFIG (inventory) | abstract role in pattern |
| pattern-state | DERIVED | — | PROPOSED | phases/phase-4.md | round 0: DERIVED (inventory) | derived workflow state |
| pending-config | DERIVED | — | PROPOSED | phases/phase-3.md | round 0: DERIVED (inventory) | staged package |
| periodic-capture | PRIMITIVE | — | PROPOSED | ADR-004 | round 0: PRIMITIVE (inventory) | scheduled pattern |
| policy-routing | ALGORITHM | — | PROPOSED | phases/phase-4.md | round 0: ALGORITHM (inventory) | non-recursive triggers |
| predicate | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | atomic comparison |
| predicate-budget | CONFIG | — | PROPOSED | phases/phase-3.md | round 0: CONFIG (inventory) | 3-predicate limit |
| process | PRIMITIVE | — | PROPOSED | ADR-001 | round 0: PRIMITIVE (inventory) | identity category |
| process-identity | RESERVED | — | PROPOSED | envelope.schema.json | round 0: RESERVED (inventory) | subject_ref type (Phase 4+) |
| projection | DERIVED | — | PROPOSED | ADR-001 | round 0: DERIVED (inventory) | computed current state |
| projection-derived-state | INVARIANT | — | PROPOSED | adr-005-state-progression.md | round 0: INVARIANT (inventory) | never stored in payloads |
| projection-engine | PRIMITIVE | — | PROPOSED | ADR-001 | round 0: PRIMITIVE (inventory) | state derivation |
| projection-equivalence | CONTRACT | — | PROPOSED | contracts/fixtures/projection-equivalence.json | round 0: CONTRACT (inventory) | server/mobile parity |
| projection-rebuild-strategy | OPEN | — | OPEN | raw-B | round 0: OPEN (inventory) | implementation decision |
| projection-rule | DERIVED | — | PROPOSED | ADR-004 Session 2 | round 0: DERIVED (inventory) | source→derived mapping |
| provisional-release | RESERVED | — | DEFERRED | phases/phase-3.md | round 0: DEFERRED (inventory) | breaking change tooling (IG-7) |
| query-based-scope | CONFIG | — | PROPOSED | ADR-003 S3 | round 0: CONFIG (inventory) | conditional data access |
| raw-reference | ALGORITHM | — | PROPOSED | adr-002-identity-conflict.md | round 0: ALGORITHM (inventory) | CD uses original subject_ref |
| read-discipline | RESERVED | — | PROPOSED | phases/phase-1.md | round 0: RESERVED (inventory) | all reads defensive |
| replay | ALGORITHM | — | PROPOSED | phases/phase-0.md | round 0: ALGORITHM (inventory) | reconstruct from events |
| resolvability-classification | INVARIANT | — | PROPOSED | adr-005-state-progression.md | round 0: INVARIANT (inventory) | auto_eligible or manual_only |
| responsibility-binding | DERIVED | — | PROPOSED | phases/phase-2.md | round 0: DERIVED (inventory) | actor→scope assignment |
| review | CONTRACT | — | PROPOSED | ADR-004 | round 0: CONTRACT (inventory) | envelope type |
| review-state | DERIVED | — | PROPOSED | ADR-005 Session 2 | round 0: DERIVED (inventory) | event-level state |
| role | DERIVED | — | PROPOSED | phases/phase-4.md | round 0: DERIVED (inventory) | actor capability class |
| role-action-enforcement | ALGORITHM | — | PROPOSED | phases/phase-4.md | round 0: ALGORITHM (inventory) | IDR-021 implementation |
| role-action-permission | CONFIG | — | PROPOSED | ADR-003 S8 | round 0: CONFIG (inventory) | role×action matrix |
| role-action-table | CONFIG | — | PROPOSED | raw-B | round 0: CONFIG (inventory) | deployer matrix |
| role-stale | FLAG | — | PROPOSED | ADR-003 S7 | round 0: FLAG (inventory) | role changed flag |
| role-staleness | FLAG | — | PROPOSED | ADR-003 S7 | round 0: FLAG (inventory) | synonym of role-stale (dedupe in Phase 2) |
| s00 | RESERVED | — | PROPOSED | principles.md | round 0: RESERVED (inventory) | simplicity benchmark |
| schema-evolution-trap | OPEN | — | OPEN | ADR-004 Session 3 | round 0: OPEN (inventory) | anti-pattern alert |
| schema-versioning | CONFIG | — | PROPOSED | ADR-004 S6 | round 0: CONFIG (inventory) | shape_name/v{N} format |
| scope | DISPUTED | — | OPEN | ADR-003 | round 0: DISPUTED (C1:PRIMITIVE, C2:CONFIG) | Harvest: root-docs (authority) frames PRIMITIVE (P6, access-control-scenario §33). ADR-004 S7 closes scope-type registry but scope model stays platform-fixed. Recommend: PRIMITIVE; scope-type/composition stay CONFIG. See [disputes-harvest.md §Group-3](inventory/disputes-harvest.md) |
| scope-composition | CONFIG | — | PROPOSED | adr-003-authorization-sync.md | round 0: CONFIG (inventory) | combining scope types |
| scope-containment | ALGORITHM | — | PROPOSED | ADR-003 S5 | round 0: ALGORITHM (inventory) | access control test |
| scope-containment-invariant | INVARIANT | — | PROPOSED | ADR-003 S5 | round 0: INVARIANT (inventory) | foundational rule |
| scope-containment-test | ALGORITHM | — | PROPOSED | raw-B | round 0: ALGORITHM (inventory) | scope filtering |
| scope-equality | INVARIANT | — | PROPOSED | adr-003-authorization-sync.md | round 0: INVARIANT (inventory) | sync = access scope |
| scope-resolver | PRIMITIVE | — | PROPOSED | ADR-003 | round 0: PRIMITIVE (inventory) | determines scope |
| scope-staleness | FLAG | — | PROPOSED | ADR-003 S7 | round 0: FLAG (inventory) | reassigned actor flag |
| scope-stale | FLAG | — | PROPOSED | raw-B | round 0: FLAG (inventory) | alt name for scope_violation |
| scope-subject-based | CONFIG | — | PROPOSED | ADR-003 S3 | round 0: CONFIG (inventory) | explicit subject list |
| scope-type | CONFIG | — | PROPOSED | raw-B | round 0: CONFIG (inventory) | platform-fixed vocabulary |
| scope-violation | FLAG | — | PROPOSED | ADR-003 S7 | round 0: FLAG (inventory) | unauthorized access |
| selective-retain | ALGORITHM | — | PROPOSED | ADR-003 S6 | round 0: ALGORITHM (inventory) | device purge strategy |
| sensitivity-classification | CONFIG | — | PROPOSED | phases/phase-3d.md | round 0: CONFIG (inventory) | shape/activity sensitivity |
| sensitive-subject-classification | OPEN | — | OPEN | raw-B | round 0: OPEN (inventory) | subject-level dimension |
| shape | PRIMITIVE | — | PROPOSED | ADR-004 | round 0: PRIMITIVE (inventory) | payload schema |
| shape-binding | ALGORITHM | — | PROPOSED | phases/phase-4.md | round 0: ALGORITHM (inventory) | transition vs activation |
| shape-definition | CONFIG | — | PROPOSED | raw-B | round 0: CONFIG (inventory) | versioned schema |
| shape-payload-validator | PRIMITIVE | — | PROPOSED | ADR-004 | round 0: PRIMITIVE (inventory) | validates against shape |
| shape-ref | CONTRACT | — | PROPOSED | ADR-004 | round 0: CONTRACT (inventory) | mandatory envelope field |
| shape-registry | PRIMITIVE | — | PROPOSED | ADR-004 | round 0: PRIMITIVE (inventory) | stores definitions |
| shape-role | DERIVED | — | PROPOSED | phases/phase-4.md | round 0: DERIVED (inventory) | abstract→concrete mapping |
| shape-version | DERIVED | — | PROPOSED | phases/phase-3.md | round 0: DERIVED (inventory) | timestamped snapshot |
| shape-versioning | CONFIG | — | PROPOSED | ADR-004 S6 | round 0: CONFIG (inventory) | additive/deprecation/breaking |
| show-condition | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | L2 visibility rule |
| single-source-of-truth | INVARIANT | — | PROPOSED | raw-B | round 0: INVARIANT (inventory) | events authoritative |
| single-writer-resolution | INVARIANT | — | PROPOSED | raw-B | round 0: INVARIANT (inventory) | one resolver per flag |
| source-chain-traversal | ALGORITHM | — | PROPOSED | ADR-005 Session 2 | round 0: ALGORITHM (inventory) | upstream flag detection |
| source-only-flagging | ALGORITHM | — | PROPOSED | ADR-005 Session 2 | round 0: ALGORITHM (inventory) | root-cause flagging |
| split | ALGORITHM | — | PROPOSED | ADR-001 | round 0: ALGORITHM (inventory) | subject identity operation |
| split-freezes-history | INVARIANT | — | PROPOSED | raw-B | round 0: INVARIANT (inventory) | source archival |
| split-subject | ALGORITHM | — | PROPOSED | adr-002-identity-conflict.md | round 0: ALGORITHM (inventory) | online-only operation |
| stale-reference | FLAG | — | PROPOSED | ADR-002 S8 | round 0: FLAG (inventory) | entity update lag |
| state-as-projection | ALGORITHM | — | PROPOSED | ADR-005 Session 1 | round 0: ALGORITHM (inventory) | derived from events |
| state-machine | PRIMITIVE | — | PROPOSED | ADR-005 Session 1 | round 0: PRIMITIVE (inventory) | named states + transitions |
| state-progression | ALGORITHM | — | PROPOSED | phases/phase-4.md | round 0: ALGORITHM (inventory) | work lifecycle |
| subject | PRIMITIVE | — | PROPOSED | ADR-001 | round 0: PRIMITIVE (inventory) | identity category |
| subject-based-scope | CONFIG | — | PROPOSED | ADR-003 S3 | round 0: CONFIG (inventory) | explicit list type |
| subject-binding | DERIVED | — | PROPOSED | phases/phase-3.md | round 0: DERIVED (inventory) | shape field mapping |
| subject-deactivated | FLAG | — | PROPOSED | ADR-002 S9 | round 0: FLAG (inventory) | decommissioned entity |
| subject-identity-resolver | PRIMITIVE | — | PROPOSED | ADR-002 | round 0: PRIMITIVE (inventory) | merge/split validator |
| subject-level-pattern | CONFIG | — | PROPOSED | raw-B | round 0: CONFIG (inventory) | subject-scoped workflow |
| subject-level-state | DERIVED | — | PROPOSED | ADR-005 Session 2 | round 0: DERIVED (inventory) | lifecycle projection |
| subject-list-scope | CONFIG | — | PROPOSED | ADR-003 S3 | round 0: CONFIG (inventory) | explicit subject type |
| subject-ref | DISPUTED | — | OPEN | ADR-001 | round 0: DISPUTED (A:PRIMITIVE, C1:CONTRACT, C2:PRIMITIVE) | Harvest: category error — `subject` is PRIMITIVE, `subject_ref` is the CONTRACT by which events reference subjects. Reserved `process` type in enum is separate row. Recommend: CONTRACT. See [disputes-harvest.md §Group-2](inventory/disputes-harvest.md) |
| subject-split | CONTRACT | — | PROPOSED | ADR-002 S9 | round 0: CONTRACT (inventory) | platform-bundled shape |
| subjects-merged | CONTRACT | — | PROPOSED | ADR-002 S9 | round 0: CONTRACT (inventory) | platform-bundled shape |
| sync-contract | CONTRACT | — | PROPOSED | raw-B | round 0: CONTRACT (inventory) | 8 guarantees |
| sync-protocol | ALGORITHM | — | PROPOSED | phases/phase-0.md | round 0: ALGORITHM (inventory) | watermark-based exchange |
| sync-scope | INVARIANT | — | PROPOSED | ADR-003 | round 0: INVARIANT (inventory) | data device receives |
| sync-unit | INVARIANT | — | PROPOSED | adr-001-offline-data-model.md | round 0: INVARIANT (inventory) | immutable event |
| sync-watermark | CONTRACT | — | PROPOSED | ADR-001 | round 0: CONTRACT (inventory) | server sequence number |
| system-actor | ALGORITHM | — | PROPOSED | ADR-004 S4 | round 0: ALGORITHM (inventory) | system: prefix format |
| sweep-job | ALGORITHM | — | PROPOSED | phases/phase-1.md | round 0: ALGORITHM (inventory) | periodic CD re-evaluation |
| task-completed | CONTRACT | — | PROPOSED | ADR-004 | round 0: CONTRACT (inventory) | envelope type |
| task-created | CONTRACT | — | PROPOSED | ADR-004 | round 0: CONTRACT (inventory) | envelope type |
| temporal-access-bounds | CONFIG | — | PROPOSED | ADR-003 S4 | round 0: CONFIG (inventory) | valid_from/valid_until |
| temporal-authority-expired | FLAG | — | PROPOSED | ADR-003 S7 | round 0: FLAG (inventory) | assignment ended |
| three-tier-hierarchy | CONFIG | — | PROPOSED | ADR-003 S3 | round 0: CONFIG (inventory) | organizational structure |
| transaction-boundary | ALGORITHM | — | PROPOSED | phases/phase-1.md | round 0: ALGORITHM (inventory) | 2-Tx pipeline |
| transfer-with-acknowledgment | CONFIG | — | PROPOSED | ADR-005 Session 2 | round 0: CONFIG (inventory) | handoff pattern |
| transition-violation | FLAG | — | PROPOSED | ADR-005 Session 1 | round 0: FLAG (inventory) | invalid state transition |
| trigger | PRIMITIVE | — | PROPOSED | ADR-004 S7 | round 0: PRIMITIVE (inventory) | L3 reactive rule |
| trigger-budget | CONFIG | — | PROPOSED | phases/phase-4.md | round 0: CONFIG (inventory) | hard complexity limits |
| trigger-context | PRIMITIVE | — | PROPOSED | phases/phase-4.md | round 0: PRIMITIVE (inventory) | expression evaluation |
| trigger-engine | PRIMITIVE | — | PROPOSED | ADR-004 | round 0: PRIMITIVE (inventory) | server-side processing |
| trigger-escalation-trap | OPEN | — | OPEN | ADR-004 Session 3 | round 0: OPEN (inventory) | anti-pattern alert |
| trustee-records | INVARIANT | — | PROPOSED | principles.md | round 0: INVARIANT (inventory) | audit trail requirement |
| two-slot-config | ALGORITHM | — | PROPOSED | phases/phase-3.md | round 0: ALGORITHM (inventory) | current + pending |
| two-tier-sync | ALGORITHM | — | PROPOSED | ADR-003 S6 | round 0: ALGORITHM (inventory) | asymmetric model |
| type-vocabulary | INVARIANT | — | PROPOSED | ADR-004 S3 | round 0: INVARIANT (inventory) | 6-value envelope type |
| union-projection | ALGORITHM | — | PROPOSED | phases/phase-3.md | round 0: ALGORITHM (inventory) | multi-version shapes |
| validation-engine | PRIMITIVE | — | PROPOSED | ADR-004 | round 0: PRIMITIVE (inventory) | constraint evaluator |
| warning-expression | PRIMITIVE | — | PROPOSED | phases/phase-3.md | round 0: PRIMITIVE (inventory) | L2 conditional warning |
| watermark | DERIVED | — | PROPOSED | phases/phase-0.md | round 0: DERIVED (inventory) | pagination key |
| workflow-pattern | CONFIG | — | PROPOSED | ADR-005 Session 1 | round 0: CONFIG (inventory) | synonym of pattern |
| write-path-discipline | INVARIANT | — | PROPOSED | ADR-001 | round 0: INVARIANT (inventory) | events sole write path |

