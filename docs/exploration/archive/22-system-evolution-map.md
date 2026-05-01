# System Evolution Map

> Reconstruction date: 2026-05-01
> Scope: `docs/README.md` -> scenarios -> constraints/viability/principles/patterns -> exploration archive/guides -> ADR-001..009 -> architecture/implementation/decision logs -> convergence -> charter/ships/flagged positions.
>
> This file is a historical reconstruction. It is not the current source of truth. For current settled state, start at `docs/charter.md`.

## Executive Reconstruction

The system thinking moved through seven phases:

1. **Exploration**: domain-pure operational scenarios were narrowed into behavioral patterns and provisional principles. The key belief was that a domain-agnostic operations platform was viable if complexity stayed compositional.
2. **ADR 001-005**: the architecture was locked through dependency-ordered constraint decisions: immutable events, identity/conflict, authorization/sync, configuration boundary, and workflow.
3. **Drift**: implementation and architecture reference docs expanded the ADR conclusions into a large component map, phase plan, and IDR schedule. Some claims became too implementation-shaped, some deferred positions were not carried forward mechanically, and agents began inheriting an oversized surface.
4. **Convergence**: a protocol, ledger, and charter rebuilt the decision surface from named concepts, cites, and supersession rules.
5. **ADR 006-007**: convergence discovered that flag semantics and envelope type vocabulary needed architecture-grade closure. ADR-006 split invariant/property from algorithm/procedure; ADR-007 closed the envelope `type` vocabulary and moved identity/integrity facts into bundled shapes.
6. **ADR 008-009**: reference fields and platform-fixed-vs-deployer-configured duality were canonicalized. This retired the biggest classification confusion: mechanism and instance had been collapsed into one mental bucket.
7. **Ships**: the project changed unit of work from implementation phases to scenario-driven Ships. Ships test ADR claims through thin vertical slices, retro, ledger updates, charter regeneration, and flagged-position closure or creation.

The most important shift: early thinking asked "what platform architecture could support the whole problem space?" Later thinking asks "which decided positions survive contact with scenario slices, and which positions need supersession?"

## Timeline

### 1. Exploration

**Primary artifacts**

- `docs/README.md`
- `docs/scenarios/README.md` and scenario files
- `docs/constraints.md`
- `docs/viability-assessment.md`
- `docs/principles.md`
- `docs/behavioral_patterns.md`
- `docs/exploration/archive/00-exploration-framework.md`
- `docs/exploration/archive/*.md` (the framework generally followed `00-exploration-framework.md` though each exploration needed to adapt it)
- `docs/exploration/guide-adr-001.md` through `guide-adr-005.md` (reference indexes for the `exploration/archived` files)

**What was believed**

- The product was a domain-agnostic operational substrate: collect information, coordinate work, track progress, preserve accountability.
- Scenarios had to stay domain-pure and solution-independent. They described field reality, not constructs.
- The recurring problem space decomposed into a small set of behaviors: structured recording, subject linkage, responsibility, hierarchy, review, transfer, state progression, condition-triggered action, cross-reference, shape evolution, offline work.
- Offline-first was not a feature. It was the environmental default.
- "Set up, not built" was a real promise, but dangerous unless the configuration boundary was explicit.
- The simplest scenario, S00, had to remain simple no matter how much machinery existed elsewhere.

**What was decided**

- Phase 1 scenarios were coherent enough for platform architecture.
- Scenarios 15, 16, and 18 were deferred because they would distort the initial core.
- Behavioral patterns became the first narrowing layer.
- Principles became provisional hypotheses to test through ADRs.
- The exploration method required dependency order, explicit assumptions, an irreversibility filter, and audit before commitment.

**What was lost or distorted**

- The viability assessment said the configuration boundary should be first, but the later ADR sequence made offline data model the root. That was not necessarily wrong, but the reason is easy to lose: ADR-001 exploration found the data model was the dependency root for identity, sync, configuration, and workflow.
- Domain-agnosticism was supported by domain-pure scenarios, but the strongest composite examples remained health-oriented CHV workflows (just examples, the platform should never assume a health-orientation in its core or architecture, always domain agnostic processes, names, patterns, etc).
- Setup/admin experience was recognized as the weakest-tested vision promise, but later implementation docs could make it look already solved by tooling plans.
- Exploration evidence was rich and scenario-grounded, but most of that proof was compressed away in ADRs.

### 2. ADR 001-005

**Primary artifacts**

- `docs/adrs/adr-001-offline-data-model.md`
- `docs/adrs/adr-002-identity-conflict.md`
- `docs/adrs/adr-003-authorization-sync.md`
- `docs/adrs/adr-004-configuration-boundary.md`
- `docs/adrs/adr-005-state-progression.md`
- `docs/architecture/primitives.md`
- `docs/architecture/patterns.md`

**What was believed**

- Stored events and offline contracts are the most irreversible layer.
- Most complexity should live in projections, server-side procedures, policy, or configuration, not in permanent envelope shape.
- Append-only event history is the integrity anchor.
- State is derived. Identity aliases are projection behavior. Authority is projection behavior. Workflow state is projection behavior.
- Configuration should combine platform-fixed mechanisms, not become general-purpose programming.

**What was decided**

- ADR-001: all writes are immutable append-only events; client-generated UUIDs; sync unit is the event; current state is projection.
- ADR-002: causal ordering fields, typed identity refs, advisory device time, hardware-bound device ID, alias-in-projection merge, split freezes history, acyclic lineage, online-only merge/split, single-writer conflict resolution, detect-before-policy, raw-reference conflict detection, accept state-stale events.
- ADR-003: assignment-based access; sync scope equals access scope; authority context is a projection, not an envelope field; alias respects original scope; scope containment; conflict resolution online-only; detect-before-act extends to authorization flags.
- ADR-004: `shape_ref`, `activity_ref`, closed structural type vocabulary, system actor convention, server-only triggers, atomic config delivery, no deployer-authored access logic, no field-level sensitivity, four-layer configuration gradient, shape versioning, expression language, trigger limits, complexity budgets, parameterized policies.
- ADR-005: transition violation flags, flagged-event exclusion, flag resolvability classification, projection-derived state machines, platform-fixed Pattern Registry, composition rules, source-only flagging, `context.*`, auto-resolution as L3b.

**What was lost or distorted**

- The ADRs preserved decisions but not the proof density. Important exploration insights were left in guides/archive:
  - prior-art failure modes from DHIS2, CommCare, ODK, OpenSRP, Salesforce;
  - per-scenario attack paths proving constraints;
  - irreversibility gradients;
  - anti-pattern catalogs;
  - "overcall" corrections where stress tests had over-promoted evolvable strategies into permanent-sounding commitments.
- Architecture docs then expanded the ADRs into 11 "primitives." Later convergence found only `scope` and `pattern` should remain PRIMITIVE under ADR-009; the rest are implementation components or contracts.
- The mental model began to mix architecture-grade invariants, implementation modules, and deployer configuration in the same vocabulary.

### 3. Drift

**Primary artifacts**

- `docs/implementation/README.md`
- `docs/implementation/plan.md`
- `docs/implementation/phases/*`
- `docs/decisions/README.md`
- `docs/decisions/INDEX.md`
- `docs/flagged-positions.md`
- Git history from 2026-04-17 through 2026-04-21:
  - `6de7bc6` Phase 0b foundation build
  - `3d41bcd` Phase 1a/1b conflict detector and identity resolver
  - `0e22275` Phase 1 complete
  - `32833a7` Phase 2b multi-actor scope
  - `4ad68b4` Phase 3 complete
  - `b4e25f9` Phase 4.0 status and guardrails
  - `afe9f47` Phase 4.0 rolled back, baseline phase-3c
  - `d2b4cbb` ADR-002 Addendum
  - `fb2d4d4` Phase 3e spec and flagged-position register
  - `b3cba6d` Phase 3e audit fixes

**What was believed**

- The ADR architecture could be built as a phased system: core loop, identity/integrity, authorization, configuration, workflow.
- IDRs could safely carry implementation-grade choices while ADRs carried architectural constraints.
- A broad architecture reference would help agents know where to work.

**What was decided**

- A technology stack was chosen: Flutter/SQLite, Spring Boot/PostgreSQL, Angular originally, REST/JSON, JSON Schema.
- IDRs recorded concrete implementation choices: test infrastructure, schema validation, concurrency detection, server event producer, alias table, conflict intercept, assignment payload, materialized paths, scope-filtered sync, actor token table, shape storage, expression grammar, config package.
- A flagged-position register was created because deferrals were not surviving agent handoff.

**What was lost or distorted**

- Phase milestones began to masquerade as architectural confidence. "Phase complete" did not always mean ADR positions had been exercised through scenario acceptance.
- The ADR-002 Addendum existed because envelope type vocabulary drift had already happened: identity/integrity facts were being treated like envelope types instead of shapes carried by the fixed envelope pipeline.
- The architecture surface was too large for agents: ADRs, architecture docs, implementation plan, phase specs, IDRs, status docs, CLAUDE/status tracking, and flagged positions all competed as "current."
- Flagged positions were meant as debt control, but they also became a second living truth surface. The user expectation that `flagged-positions.md` should stop did not hold; it continued because Ships still needed an explicit memory mechanism for unresolved tensions.

### 4. Convergence

**Primary artifacts**

- `docs/convergence/README.md`
- `docs/convergence/protocol.md`
- `docs/convergence/concept-ledger.md`
- `docs/convergence/supersede-rules.md`
- `docs/convergence/annotation-conventions.md`
- `docs/convergence/cold-start.md`
- `docs/charter.md`
- Git history:
  - `17d7806` convergence scaffolding
  - `095a1c5` Phase 0 inventory
  - `f04ccfa` archive harvest
  - `72fa216` topology sort and ADR queue

**What was believed**

- The system needed a mechanical convergence protocol, not more prose.
- Every named concept needed classification, status, owner, and citation.
- Current truth had to be generated from decided ADRs and ledger rows, not inferred from older architecture docs.
- Drift is not just bad reasoning; it is missing lifecycle machinery.

**What was decided**

- Convergence created a ledger and charter.
- Claims without Decided ADR cites were suspect.
- Supersession became explicit.
- Fresh agents should read charter and ledger first.
- Architecture docs were pruned or marked historical where they contradicted the charter.

**What was lost or distorted**

- Convergence necessarily compressed the exploratory richness even further. It stabilized "what is settled" but cannot substitute for why a position was hard-won.
- Some implementation-grade details were purged from ADRs, correctly, but this can make future implementers think the ADR is thinner than the actual reasoning.
- The concept-ledger model improved classification but added another artifact agents must understand.

### 5. ADR 006-007

**Primary artifacts**

- `docs/adrs/adr-006-flag-semantics.md`
- `docs/adrs/adr-006-flag-semantics-R.md`
- `docs/adrs/adr-007-envelope-type-closure.md`
- `docs/adrs/adr-002-addendum-type-vocabulary.md`
- Git history:
  - `ca85fd4` ADR-006 flag semantics
  - `4528083` ADR-006 S2 refinement
  - `e231fbf` ADR-007 envelope type closure
  - `d132db1` purge implementation refs from ADR-006/007
  - `f2ccf86` ADR-006-R alias-cycle prevention
  - `53030ca` cycle-guard contract

**What was believed**

- "Flag" had been used ambiguously: sometimes as invariant anomaly surface, sometimes as detector procedure, sometimes as storage/UX object.
- Envelope `type` was at risk of becoming an open domain fact vocabulary.
- Identity and integrity events needed canonical expression without reopening the envelope.

**What was decided**

- Accept-and-flag is an invariant.
- Flag is the canonical event-stream anomaly surface, not a parallel anomaly table on the event stream.
- Conflict detection is the algorithm.
- Server-side flag creation is the default, with device-side creation additively evolvable.
- Envelope `type` is closed at six values.
- Identity/integrity facts are platform-bundled shapes, not new envelope types.
- Consumer filtering must use `shape_ref` for fact discrimination, not `type`.
- Deterministic flag identity includes `shape_ref`.
- ADR-006-R adds alias-cycle prevention as a push-path guard with `cycle_violation`.

**What was lost or distorted**

- The original ADR-002 Addendum was corrective, not just additive. Without that context, ADR-007 can look like a normal refinement instead of a response to drift.
- The flag lifecycle now spans ADR-002, ADR-003, ADR-004, ADR-005, ADR-006, and ADR-006-R. The charter makes this readable; isolated ADR reading does not.

### 6. ADR 008-009

**Primary artifacts**

- `docs/adrs/adr-008-envelope-reference-fields.md`
- `docs/adrs/adr-009-platform-fixed-vs-deployer-configured.md`
- `docs/charter.md`
- Git history:
  - `5b5b1aa` ADR-008 reference fields
  - `b9a709d` ADR-009 duality rule
  - `7ae13f1` round-1 fixpoint closeout
  - `99a1f6f` round-2 quiet scan
  - `585ce21` charter populated, architecture pruned, Ship cadence adopted

**What was believed**

- Reference fields are contracts; referents are domain/platform objects. Collapsing them creates category errors.
- Some concepts have two faces: a platform-fixed mechanism and a deployer-configured instance.
- Agents need classification discipline more than they need more component diagrams.

**What was decided**

- `subject_ref`, `actor_ref`, and `activity_ref` are envelope contract fields.
- Reference is not referent.
- `subject_ref.type` is closed unless an ADR changes it.
- `actor_ref` can represent human UUIDs or `system:{source_type}/{source_id}`.
- `activity_ref` is optional deployer-chosen configuration.
- ADR-009 duality rule: when a concept exposes both closed mechanism and parameterized instance, split the ledger row. Mechanism can be PRIMITIVE; instance can be CONFIG.
- `scope` and `pattern` are primitives; `activity` is config.

**What was lost or distorted**

- The old "11 primitives" architecture became historical, but some files remain in place for continuity. This is a high-risk zone for agent confusion.
- The charter became the real model, but older docs still have emotionally authoritative names like "Structural View" and "Architecture Description."

### 7. Ships

**Primary artifacts**

- `docs/ships/README.md`
- `docs/ships/ship-1.md`, `ship-1b.md`, `ship-2.md`, `ship-3.md`, `ship-4.md`
- `docs/ships/*-retro.md`
- `docs/reviews/system/*`
- `docs/flagged-positions.md`
- `docs/charter.md`
- Git history:
  - `a93c801` Ship-1 spec
  - `633c6fb` Ship-1 sync protocol/retro/charter close
  - `67eb5b8` Ship-1b retro on real Flutter client
  - `ad908c6` Ship-2 spec
  - `042a4f7` Ship-2 closeout
  - `75c7374` through `2c201b0` Ship-3 spec/retro/charter
  - `7ecbee6` Standing Rule R-7
  - `4f8d5c1` charter regenerated with R-7
  - `b998cff`, `2b2ee9b`, `cb316ec` Ship-4 draft iterations

**What was believed**

- Scenario acceptance, not phase completion, is the correct proof unit.
- A Ship delivers a thin vertical scenario slice and deliberately parks the rest.
- ADRs are only trustworthy when their Section S claims are exercised or explicitly left unexercised.
- Retros are where architecture learns from implementation.

**What was decided**

- No code begins until Ship spec exists and cited ADRs are Decided.
- Every Ship declares scenarios, ADRs exercised, ADRs at risk, ledger concepts touched, out-of-scope assertions, and retro criteria.
- Walkthroughs follow scenario prose and are acceptance criteria.
- Retro updates ADRs if needed, ledger, charter, and flagged positions.
- R-7 requires Section-S-to-implementation parity tracking: `decided-unexercised`, `exercised-met`, `exercised-violated`.

**What was lost or distorted**

- `flagged-positions.md` did not stop. It became the append-only mechanism for unresolved verification and quiet-decision markers in the Ship cadence.
- The ship cadence reduced agent confusion, but created more ceremony: spec, walkthrough, retro, parity, ledger, charter, FP updates.
- Some old phase language remains in docs and history. It is historical unless the charter/ships cite it.

## decision_lineage

### From Problem Space To Constraints

| Lineage step | Input | Output | Meaning |
|---|---|---|---|
| Vision | `docs/README.md` | five core commitments plus domain-agnostic ambition | The platform is an operational substrate, not a domain app. |
| Scenarios | `docs/scenarios/*` | domain-pure use cases | Reality before constructs. |
| Constraints | `docs/constraints.md` | offline, scale, sensitivity, interoperability, responsiveness | Operational boundaries that invalidate easy designs. |
| Viability | `docs/viability-assessment.md` | conditional GO, risks R1-R5 | The platform is coherent if configuration and reactivity are bounded. |
| Patterns | `docs/behavioral_patterns.md` | 12 recurring behaviors | First narrowing: recurring behavior, not architecture. |
| Principles | `docs/principles.md` | P1-P7 | Decision hypotheses later confirmed by ADRs. |

### From Exploration To ADRs

| ADR | Exploration discovery | Final commitment |
|---|---|---|
| ADR-001 | Event sourcing won because snapshots lose traceability and action-log converges toward event sourcing under pressure. | Immutable typed events; append-only write path; event sync unit; client UUIDs. |
| ADR-002 | Identity/conflict stress tests proved accept-and-flag, raw refs, causal ordering, alias projection, and single-writer resolution. | Identity lifecycle and conflict constraints. |
| ADR-003 | Irreversibility filter showed authorization needed zero new envelope fields. | Assignment-based access; sync scope equals access scope; authority as projection. |
| ADR-004 | Four-layer gradient bounded configuration power and kept S12 from becoming a rules engine. | Shape/activity refs, server-only triggers, config layers, budgets. |
| ADR-005 | State machines emerged as fixed patterns, not deployer-authored workflows. | Projection-derived state, Pattern Registry, composition rules, flag interaction. |
| ADR-006 | "Flag" needed semantic closure. | Accept-and-flag invariant, flag surface, conflict-detection algorithm, server default. |
| ADR-007 | Envelope type drift needed closure. | Six-value `type`, fact discrimination by `shape_ref`, bundled integrity/identity shapes. |
| ADR-008 | Reference fields and referents were being confused. | `subject_ref`, `actor_ref`, `activity_ref` as envelope contracts. |
| ADR-009 | Mechanism/config instance duality was the key classification error. | Split mechanism from deployer instance; `scope` and `pattern` primitive, `activity` config. |

### From ADRs To Ships

| Old model | New model |
|---|---|
| Implementation phases prove progress. | Ships prove scenario slices. |
| Architecture docs summarize current design. | Charter/ledger summarize current decided state. |
| IDRs carry implementation choices. | IDRs still carry choices, but only inside Ship/charter discipline. |
| Flagged positions are temporary debt notes. | Flagged positions are append-only Ship memory until resolved/superseded. |
| Agents read many docs to infer truth. | Agents start with charter, ledger, and current Ship spec. |

## lost_exploration_insights

These insights are important because they explain why the current constraints exist. They are not always visible from ADR text alone.

1. **Prior-art lessons were central, then compressed.**
   - DHIS2 warned about metadata walls.
   - CommCare warned about form-as-everything ceilings.
   - ODK warned about weak case management.
   - OpenSRP warned about domain-model lock-in.
   - Salesforce warned about metadata plus flow power and complexity.

2. **ADR-001 was won by asymmetry, not aesthetics.**
   - Snapshots make correction and identity history lossy.
   - Action log looks simpler but converges toward event sourcing once projections and conflicts matter.
   - Event sourcing was chosen because it preserved the best escape hatches.

3. **ADR-002 constraints were proof-driven.**
   - Accept-and-flag came from downstream-work-before-review failure.
   - Single-writer resolution came from conflicting resolutions.
   - Device sequence came from clock-reset and same-watermark cases.
   - Raw-reference detection came from alias timing cases.

4. **ADR-003 was intentionally small.**
   - The key discovery was "do not put authority_context in the envelope."
   - Most authorization machinery is server/projection strategy, not permanent event contract.

5. **ADR-004's anti-pattern catalog is a hidden design guard.**
   - Config-as-code, vocabulary creep, implicit coupling, version coupling, ghost dependencies, and complexity blind spots were used as tests.
   - The four-layer gradient is the answer to the viability risk R1.

6. **ADR-005 patterns were discovered from scenario storms, not invented as framework features.**
   - `capture_with_review`, `case_management`, `multi_step_approval`, and `transfer_with_acknowledgment` emerged from S04, S08, S11, S07/S14.
   - The "no deployer-authored state machines" rule is the important boundary.

7. **The simplicity proof matters.**
   - S00 was repeatedly walked through to ensure identity, sync, config, and workflow machinery did not become mandatory ceremony.

8. **Convergence preserved decisions but not full reasoning.**
   - The charter is current truth.
   - The exploration guides are still needed when asking "why was this position chosen?"

## contradictions_map

| Contradiction / tension | Where it appeared | What actually happened | Current handling |
|---|---|---|---|
| Viability said configuration boundary first, ADR sequence started with offline data model. | `viability-assessment.md` vs ADR-001 guide. | Exploration found offline data model was the dependency root for identity, sync, config, and workflow. | Treat as resolved by ADR-001 exploration, but remember R1 still lives at the L3 boundary. |
| "Architecture has 11 primitives" vs charter says only `scope` and `pattern` are primitives. | `architecture/primitives.md` vs `charter.md`/ADR-009. | Convergence reclassified most "primitives" as implementation components/contracts. | Charter wins. `primitives.md` is historical unless rewritten. |
| Envelope `type` as domain fact vs envelope `type` as closed pipeline selector. | Phase 1/2 drift, ADR-002 Addendum, ADR-007. | Identity/integrity facts were moved to bundled shapes. | ADR-007 wins. Filter on `shape_ref`, not `type`. |
| Flag as invariant vs flag as algorithm/procedure/location. | ADR-002/003/005 wording, convergence. | ADR-006 split accept-and-flag, flag surface, conflict detection, and flag creation location. | ADR-006/006-R and charter lifecycle win. |
| Authority in envelope vs authority as projection. | ADR-003 stress test. | Envelope field was rejected. | ADR-003 S3 wins; authority reconstructed from assignment timeline. |
| Setup not built vs no setup scenario. | Vision/viability. | Admin/setup experience remained a blind spot, later approximated by ADR-004 and implementation plans. | Needs Ship evidence when config authoring is exercised. |
| Domain-agnostic claim vs health-heavy composites. | Scenarios 20/21. | Core scenarios are domain-pure, composites are health. | Not fatal; add non-health walkthrough when useful. |
| Phase completion vs scenario proof. | Implementation phases/status docs. | Phase labels gave false confidence; rollback and convergence followed. | Ships replace phases as proof unit. |
| Flagged positions should stop vs flagged positions continue. | User expectation, convergence/ships. | It continued because unresolved verification needs append-only memory across agents. | Keep, but treat as register, not source of settled truth. |
| `field_count_budget` stable vs unexercised. | Ship-3 closeout. | R-7 demoted it from STABLE to DEFERRED because Section-S implementation parity was not met. | R-7 governs: no stable claim without exercise or explicit decided-unexercised status. |
| Corrective split preserves history vs operator needs usable attribution after wrong merge. | FP-006, ADR-002 S7/S8. | Intra-ADR tension surfaced only when Ship-2 exercised merge/split. | Still flagged; requires Ship/ADR resolution before dependent read paths. |
| Corrections are ADR-decided vs no dedicated delivery plan. | ADR-001 S1, FP-005. | Corrections were named but not assigned to a Ship. | Still flagged; likely Ship-4/S04 surface. |

## Current Mental Model

Read the current system as four layers:

1. **Event contract**: immutable event envelope, closed `type`, mandatory `shape_ref`, typed references, append-only sync.
2. **Projection discipline**: current state, identity aliasing, authority, workflow state, and flag contamination are derived from events.
3. **Configuration boundary**: deployers assemble activities, shapes, policies, and pattern instances within the four-layer gradient. They do not author platform mechanisms.
4. **Ship cadence**: every scenario slice tests a subset of ADR claims and feeds back through retro, parity review, ledger update, charter regeneration, and flagged-position maintenance.

The platform is no longer best understood as "11 primitives to implement in phases." It is better understood as:

- a small permanent event/reference contract;
- a projection-first operating model;
- a closed set of platform mechanisms with deployer-configured instances;
- a Ship loop that converts scenario evidence into stable or revised decisions.

## Agent Read Order After This Reconstruction

For current work:

1. `docs/charter.md`
2. `docs/convergence/concept-ledger.md`
3. current `docs/ships/ship-N.md`
4. `docs/flagged-positions.md`
5. relevant ADRs
6. relevant exploration guide only if the "why" is unclear

For historical reasoning:

1. `docs/README.md`
2. `docs/scenarios/README.md` plus relevant scenario files
3. `docs/constraints.md`
4. `docs/viability-assessment.md`
5. `docs/principles.md`
6. `docs/behavioral_patterns.md`
7. `docs/exploration/archive/00-exploration-framework.md`
8. `docs/exploration/guide-adr-00N.md`
9. ADR body
10. convergence ADRs and charter

## Final Answer To "What Actually Happened To My System Thinking?"

Your system thinking started broad and disciplined: domain-pure scenarios, behavior extraction, provisional principles, and stress-tested ADRs. It then became over-expanded during implementation: the conclusions were translated into too many simultaneous truth surfaces, and agents inherited both the settled architecture and its speculative scaffolding as if they were equally current.

Convergence was the repair. It did not invent a new architecture; it rebuilt the memory system around the architecture. It separated invariant from algorithm, contract from referent, mechanism from configured instance, and scenario proof from phase progress. Ships then changed the project from "build the architecture plan" to "exercise decided claims through scenario slices."

The main thing lost along the way was not intelligence. It was lineage. The proof trails, rejected alternatives, and exact reasons behind constraints became scattered across archives, ADRs, implementation docs, and retros. This artifact exists to reconnect them.
