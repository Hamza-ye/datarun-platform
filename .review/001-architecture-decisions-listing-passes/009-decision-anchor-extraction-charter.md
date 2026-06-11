# 009 — Decision Anchor Extraction Charter

## Context Capsule

* Artifact: `009-decision-anchor-extraction-charter.md`
* Pass: Pass 0 — Extraction Charter
* Status: Draft for project-source inclusion
* Mode: Architecture-anchor organization only; no redesign, no reopening, no new architecture decisions.
* Current authority note:

  * The original pass was extracted from `002` and `008`.
  * Current repo authority is the Canonical Decision Ledger, with contracts and accepted BAR/NW/IDR evidence used as validation inputs during catch-up.
  * This charter is a method artifact; it does not override the CDL, contracts, or current accepted baseline evidence.
* Recovery verification anchor:

  * `002-phase0-decision-register.md`
* Recovered architecture-map reference:

  * `008-authoritative-architecture-map.md`
* Input sources:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * `00-exploration-framework.md`
  * `Project Instructions and Drift Control.txt`
* Consumes previous artifacts:

  * Existing ADR recovery files `003` through `008`
  * Existing multi-pass decision-anchor plan
* Purpose:

  * Define how core architecture decisions will be extracted, named, normalized, recorded, and made consumable by future vocabulary work and gap-closure work.
* Scope:

  * Establish the extraction method.
  * Establish artifact names and pass sequence.
  * Establish decision-domain taxonomy.
  * Establish decision-record template.
  * Establish permanence classes.
  * Establish inclusion and exclusion rules.
  * Establish how future gaps consume the decision anchors.
* Non-goals:

  * Do not extract candidate decisions in this pass.
  * Do not normalize decision granularity in this pass.
  * Do not write final decision records in this pass.
  * Do not change accepted ADR decisions.
  * Do not introduce new architecture.
  * Do not convert implementation details, platform-spec details, operational policy, or product/problem evidence into architecture.
* Settled outputs:

  * A stable extraction charter.
  * Artifact sequence for passes 1 through 6.
  * Decision-domain taxonomy.
  * Decision-record template.
  * Routing rules for later use.
  * Quality gates for closure.
* Rejected / excluded:

  * One-pass extraction.
  * One decision per ADR.
  * One decision per ADR sub-decision.
  * Treating open fronts as settled architecture.
  * Treating vocabulary labels as runtime primitives.
  * Treating implementation mechanisms as architecture.
* Deferred / open:

  * Actual candidate decision inventory.
  * Granularity normalization.
  * Final decision records.
  * Vocabulary anchor map.
  * Gap routing playbook.
  * Coherence audit.
* Terms or decisions locked:

  * No new platform runtime term is locked by this charter.
  * This charter only locks the extraction process for the next artifacts.
* Next-pass handoff:

  * Pass 1 should create `010-candidate-architecture-decision-inventory.md`.
  * Pass 1 should inventory candidate decisions only; it should not polish, normalize, or route gaps.

---

## 1. Purpose

This charter defines the method for producing a stable decision-anchor layer for the Datarun platform architecture.

The decision-anchor layer will sit between:

```txt
002-phase0-decision-register.md
008-authoritative-architecture-map.md
```

and future work such as:

```txt
platform-spec detailing
vocabulary maintenance
gap closure
implementation design
operational policy definition
product/problem evidence thickening
```

The goal is to make future work mechanically traceable:

```txt
gap / scenario / platform-spec question
→ affected vocabulary
→ anchored architecture decision
→ owns / does-not-own / negative boundary
→ correct closure path
```

This charter does not create new architecture. It defines how already-settled architecture will be repackaged into reusable decision anchors.

---

## 2. Source Hierarchy

Use the following hierarchy for the original extraction pass, with the current catch-up overlay shown first:

```txt
docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md
  = current architecture decision authority

contracts/
  = implementation-facing wire/process boundary authority where crossed

docs/agent-working-surface/baseline-acceptance-register.md
docs/agent-working-surface/platform-next-work-backlog.md
docs/decisions/
  = validation, runtime evidence, and decision-provenance inputs during catch-up

002-phase0-decision-register.md
  = recovery verification anchor for this extraction pass

008-authoritative-architecture-map.md
  = recovered top-level architecture map for this extraction pass

003–006 recovery files
  = supporting lineage and rationale trail

007-phase5-cross-lineage-vocabulary.md
  = consolidated vocabulary and primitive taxonomy input

platform-introduction-problem-definition-and-user-scenario-index.md
scenarios.md
  = product/problem and scenario pressure inputs, not architecture unless mapped to accepted decisions

01-architecture-landscape.md
  = non-decision landscape and prior-art input

009–014 decision-anchor artifacts
  = derived consumable layer for future vocabulary and gap closure
```

If any new decision-anchor artifact conflicts with the CDL, contracts, or accepted baseline evidence, the new artifact is stale and must be patched.

Within the original recovery lineage, if a derived artifact conflicts with `002` or `008`, the derived artifact is wrong.

If a claim does not map to accepted decisions, the CDL/successor authority, contracts where applicable, or accepted baseline evidence, it is not settled architecture.

---

## 3. Artifact Sequence

Use this sequence:

```txt
009-decision-anchor-extraction-charter.md
010-candidate-architecture-decision-inventory.md
011-core-architecture-decision-records.md
012-vocabulary-anchor-map.md
013-gap-routing-playbook.md
014-architecture-decision-coherence-audit.md
```

### 3.1 Pass 0 — Extraction Charter

Output:

```txt
009-decision-anchor-extraction-charter.md
```

Purpose:

* Lock extraction scope.
* Confirm artifact sequence.
* Define decision domains.
* Define decision template.
* Define permanence classes.
* Define inclusion/exclusion rules.
* Define quality gates.

This pass does not extract decisions.

### 3.2 Pass 1 — Candidate Architecture Decision Inventory

Output:

```txt
010-candidate-architecture-decision-inventory.md
```

Purpose:

* Inventory candidate decision anchors from `002`, `007`, and `008`.
* Assign each candidate to one primary domain.
* Record source anchors.
* Record candidate permanence.
* Record vocabulary affected.
* Record possible overlaps.

This pass is inventory only.

It must not:

* finalize decision records;
* route gaps;
* invent new decision boundaries;
* settle open fronts.

### 3.3 Pass 2 — Core Architecture Decision Records

Output:

```txt
011-core-architecture-decision-records.md
```

Purpose:

* Normalize candidate granularity.
* Merge duplicate candidates.
* Split overlarge candidates.
* Remove vocabulary-only entries.
* Remove implementation/tooling entries.
* Remove open fronts that are not settled decisions.
* Write final decision records using the standard template.

This is the main stable decision-anchor artifact.

### 3.4 Pass 3 — Vocabulary Anchor Map

Output:

```txt
012-vocabulary-anchor-map.md
```

Purpose:

* Map each locked vocabulary term to one or more decision anchors.
* Identify the primary owning decision.
* Identify supporting decisions.
* Identify negative boundaries.
* Identify whether each term is structural, strategy-protecting, initial strategy, projection/read model, configuration artifact, policy surface, implementation concern, or product/problem term.

This pass protects vocabulary maintenance from architecture drift.

### 3.5 Pass 4 — Gap Routing Playbook

Output:

```txt
013-gap-routing-playbook.md
```

Purpose:

* Define how future gaps are classified.
* Define closure path per gap type.
* Define escalation triggers.
* Provide routing examples using known current gaps.
* Show how to consume decision anchors before proposing changes.

This pass makes the decision-anchor layer operational.

### 3.6 Pass 5 — Coherence Audit

Output:

```txt
014-architecture-decision-coherence-audit.md
```

Purpose:

* Verify every decision anchor maps to `002` or `008`.
* Verify every decision has one primary domain.
* Verify every decision has a permanence class.
* Verify every locked vocabulary term maps to a decision.
* Verify no rejected alternative is treated as settled.
* Verify no implementation detail is promoted to architecture.
* Verify known gaps route cleanly.
* Verify the S00 simplicity baseline remains protected.

---

## 4. Decision Domains

Use these domains for all decision records.

| Domain                                 | Code         | Scope                                                                                                                    |
| -------------------------------------- | ------------ | ------------------------------------------------------------------------------------------------------------------------ |
| Source-of-truth and event model        | `EVENT`      | Append-only writes, event as atomic fact, event envelope, sync unit, durable event interpretation.                       |
| Identity and lineage                   | `IDENTITY`   | Typed identity references, subject/actor/process/assignment identity, subject merge/split, causal metadata.              |
| Conflict, flags, and resolution        | `CONFLICT`   | Accept-and-flag, detect-before-act, flag categories, flag dimensions, single-writer resolution.                          |
| Authorization and sync                 | `AUTH`       | Assignment access, sync scope equals access scope, authority projection, scope containment, selective-retain.            |
| Configuration boundary                 | `CONFIG`     | Shape/activity, event type closure, L0–L3 gradient, expressions, triggers, sensitivity, deployer parameterization.       |
| Workflow and state progression         | `WORKFLOW`   | Projection-derived state, Pattern Registry, composition rules, source-chain traversal, auto-resolution.                  |
| Projection/read-model semantics        | `PROJECTION` | Rebuildable projections, assignment timeline, alias projection, workflow state projection, reporting/analytics boundary. |
| Runtime invariant guards               | `GUARD`      | Runtime services that protect accepted invariants before downstream action.                                              |
| Open evolution and negative boundaries | `BOUNDARY`   | Rejected alternatives, open fronts, underexplored areas, escalation triggers.                                            |

A decision record must have exactly one primary domain.

It may list related domains, but ownership must be singular.

---

## 5. Decision Granularity Rule

Use this rule:

```txt
One decision record = one stable architecture boundary that future work may need to consume, preserve, or challenge.
```

Avoid both extremes:

```txt
Too coarse:
  one decision per ADR

Too fine:
  one decision per ADR sub-decision
```

Expected final size:

```txt
approximately 25–35 decision records
```

A decision record is too large if it combines independent boundaries that future gaps may challenge separately.

A decision record is too small if it only restates a field or vocabulary term without defining an architectural boundary.

---

## 6. Permanence Classes

Every decision record must declare one permanence class.

### 6.1 Structural

Use when the decision touches stored-event or protocol-level contracts.

Structural decisions usually involve:

* event envelope fields;
* event identity;
* event type semantics;
* identity reference shape;
* sync unit;
* durable causal metadata;
* immutable historical interpretation;
* append-only source-of-truth behavior.

Changing a structural decision likely requires:

* migration;
* permanent dual semantics;
* historical reinterpretation;
* deployed-device compatibility handling.

### 6.2 Strategy-protecting

Use when the decision protects a structural invariant but can evolve internally.

Strategy-protecting decisions usually involve:

* server-side validation;
* conflict detection order;
* authorization invariant checks;
* trigger execution location;
* config package validation;
* deployer boundary enforcement;
* flag handling rules;
* auto-resolution guardrails.

Changing internal implementation may be possible, but weakening the protected invariant requires architecture review.

### 6.3 Initial strategy

Use when the current architecture documents a preferred strategy that can evolve without changing historical events or breaking deployed devices.

Initial strategies usually involve:

* projection placement;
* selective-retain baseline;
* complexity budgets;
* initial expression restrictions;
* current trigger subtypes;
* initial Pattern Registry examples;
* current policy parameter defaults.

Initial strategy is still part of the documented baseline, but it is more evolvable than structural or strategy-protecting decisions.

---

## 7. Standard Decision Record Template

Use this template in `011-core-architecture-decision-records.md`.

```md
## DEC-<DOMAIN>-<NN>: <Decision name>

Status: Settled | Open | Superseded
Permanence: Structural | Strategy-protecting | Initial strategy
Primary domain:
Related domains:
Source anchor:
  - ADR-XXX S<n>
  - 008 section reference
Decision:
Owns:
Does not own:
Vocabulary anchored:
Negative boundaries:
Downstream consumers:
  Product/problem evidence:
  Platform spec:
  Implementation/tooling:
  Operational policy:
Escalation triggers:
Open follow-up:
Notes:
```

### 7.1 Required Fields

Each decision record must include:

* status;
* permanence;
* primary domain;
* source anchor;
* decision statement;
* owns;
* does not own;
* vocabulary anchored;
* downstream consumers;
* escalation triggers.

### 7.2 Optional Fields

A decision may include:

* related domains;
* negative boundaries;
* open follow-up;
* notes.

Optional fields should not become rationale essays.

---

## 8. Inclusion Rules

A candidate decision may be included only if it satisfies at least one of these:

1. It maps to one or more accepted ADR sub-decisions in `002-phase0-decision-register.md`.
2. It appears in `008-authoritative-architecture-map.md` as settled architecture derived from accepted ADRs.
3. It is a consolidated boundary in `007-phase5-cross-lineage-vocabulary.md` and traces back to accepted ADR sub-decisions.

A candidate decision must also satisfy all of these:

* It defines an architectural boundary, not merely a term.
* It is useful for future gap routing.
* It has a clear owner domain.
* It can state what it owns and does not own.
* It can state what would trigger escalation.

---

## 9. Exclusion Rules

Exclude the following from decision records:

### 9.1 Product/problem evidence

Exclude:

* thin scenario details;
* user journey assumptions;
* acceptance criteria not converted into architecture;
* SME validation findings;
* deployment archetypes.

Route as:

```txt
Product/problem evidence gap
```

### 9.2 Platform-spec detail

Exclude:

* exact pattern skeleton inventory;
* report freshness semantics;
* handoff package contents;
* setup approval lifecycle;
* discrepancy handling details;
* exact flag queue ergonomics.

Route as:

```txt
Platform-spec detail gap
```

unless it changes architecture.

### 9.3 Implementation/tooling

Exclude:

* database schemas;
* APIs;
* service boundaries;
* queues;
* caches;
* local storage mechanics;
* index strategy;
* config authoring syntax;
* deployment tooling UI;
* SDK behavior.

Route as:

```txt
Implementation/tooling gap
```

unless it changes accepted architecture.

### 9.4 Operational policy

Exclude:

* review procedures;
* support workflow;
* resolver assignment practice;
* offboarding rules;
* retention windows;
* escalation procedure;
* governance practice.

Route as:

```txt
Operational policy gap
```

unless it changes accepted architecture.

### 9.5 Open fronts

Exclude as settled decisions:

* exact Pattern Registry inventory;
* auditor/query access;
* aggregate access if it differs from event access;
* shared-device storage partitioning;
* role-action artifact shape;
* config authoring syntax;
* pattern migration mechanics;
* priority sync/pagination/backfill;
* regulatory encryption/redaction/erasure mechanics.

Route according to the gap classification that applies.

---

## 10. Gap Classification Set

Future gaps must be classified into exactly one of these:

| Classification               | Use when                                                                                                                                                                                                    | Closure path                             |
| ---------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------- |
| Product/problem evidence gap | User/problem intent, scenario fit, operational evidence, edge cases, acceptance criteria, or deployment archetypes are insufficient.                                                                        | Product discovery / scenario thickening. |
| Architecture decision gap    | A proposal changes structural contracts, event semantics, identity references, sync/access behavior, authorization authority, workflow state truth, trigger behavior, or deployer configuration boundaries. | Formal architecture decision.            |
| Platform-spec detail gap     | The behavior is needed but fits under accepted architecture without changing boundaries.                                                                                                                    | Platform-spec detailing.                 |
| Implementation/tooling gap   | The issue concerns APIs, storage, service boundaries, queues, indexes, local mechanics, config syntax, UI, SDKs, migration implementation, or tooling.                                                      | Implementation/tooling design.           |
| Operational policy gap       | The issue concerns who may do something, review process, resolver practice, escalation, retention policy, support workflow, governance, or operations procedure.                                            | Operational policy definition.           |

---

## 11. Architecture Escalation Checks

Before treating any proposal as architecture, run these checks:

1. Does it change stored event fields?
2. Does it change event semantics?
3. Does it change identity reference semantics?
4. Does it change sync/access behavior?
5. Does it change authorization authority?
6. Does it change workflow state truth?
7. Does it change trigger behavior?
8. Does it change deployer configuration boundaries?
9. Does it require migrating historical events?
10. Does it break deployed devices?
11. Does it contradict a negative boundary?
12. Does it make the simplest scenario more complex than the S00 simplicity baseline?
13. Is it actually platform architecture, or only platform specification, implementation, tooling, UI, operational policy, or product/problem evidence?

If any structural contract changes, route as:

```txt
Architecture decision gap
```

---

## 12. Artifact Consumption Rules

### 12.1 For Product/Problem Evidence Work

Use decision anchors to avoid inventing architecture while thickening scenarios.

Product work may say:

```txt
This scenario pressures DEC-AUTH-02.
```

Product work must not say:

```txt
This scenario changes DEC-AUTH-02.
```

Changing a decision requires architecture decision work.

### 12.2 For Platform-Spec Work

Platform specs must list:

* decision anchors consumed;
* boundaries inherited;
* configuration surface allowed;
* explicit non-goals;
* escalation triggers.

A platform spec may define behavior under a decision.

It may not silently extend the decision.

### 12.3 For Implementation/Tooling Work

Implementation designs must list:

* architecture decisions preserved;
* platform-spec inputs consumed;
* implementation choices that remain replaceable;
* any architecture pressure discovered.

Implementation choices must not become architecture unless formally promoted.

### 12.4 For Operational Policy Work

Operational policies must list:

* decision anchors they rely on;
* human/process rules they define;
* whether any rule pressures architecture.

Operational policy must not redefine access, sync, event truth, or workflow state truth.

### 12.5 For Future Architecture Decisions

A future architecture memo or ADR-style note must name:

* which existing decision it extends or changes;
* which baseline item is affected;
* why platform-spec, implementation, operational policy, or product evidence work is insufficient;
* migration/deployed-device implications;
* negative boundaries being revised or preserved.

---

## 13. Non-Negotiable Boundaries to Preserve

These boundaries remain non-negotiable unless a formal architecture decision changes them:

* Events are durable append-only facts.
* Projections derive meaning and current state.
* Configuration is bounded and must not become arbitrary code.
* Runtime services guard invariants before downstream action.
* Event envelope fields must not be added casually.
* Authority is projection-derived, not stored in the event envelope.
* Sync scope equals access scope.
* Conflict and flag detection happen before downstream action.
* Aliases are resolved in projection after raw-reference detection.
* Workflow state is projection-derived and not stored as event truth.
* Deployer-authored event types remain outside the accepted baseline.
* Deployer-authored access-control logic remains outside the accepted baseline.
* Deployer-authored state machines remain outside the accepted baseline.
* Field-level sensitivity remains outside the accepted baseline.
* Recursive triggers remain outside the accepted baseline.
* Device-side triggers remain outside the accepted baseline.
* Stored downstream flag propagation remains outside the accepted baseline.
* Auto-resolution of manual-only flags remains outside the accepted baseline.

---

## 14. S00 Simplicity Guard

Every decision-anchor pass must preserve the S00 simplicity baseline.

The simplest structured capture scenario must not require:

* custom event types;
* custom access-control code;
* custom triggers;
* deployer-authored state machines;
* `pattern_ref`;
* `authority_context`;
* field-level sensitivity;
* auto-resolution policy;
* workflow flag propagation.

If a candidate decision makes S00 more complex, flag it during normalization.

---

## 15. Pass-Level Quality Gates

### 15.1 Pass 1 Quality Gate

The candidate inventory is ready only when:

* every candidate has a source anchor;
* every candidate has one proposed primary domain;
* every candidate has a proposed permanence class;
* every candidate lists vocabulary affected;
* overlaps are explicitly marked;
* no candidate is presented as final.

### 15.2 Pass 2 Quality Gate

The decision records are ready only when:

* every record maps to `002` or `008`;
* every record has one primary domain;
* every record has a permanence class;
* every record states owns / does-not-own;
* every record lists vocabulary anchored;
* every record lists downstream consumers;
* every record lists escalation triggers;
* open fronts are not treated as settled decisions.

### 15.3 Pass 3 Quality Gate

The vocabulary anchor map is ready only when:

* every locked term maps to at least one decision;
* every important term has one primary owner decision;
* term collisions are explicitly handled;
* negative boundaries are linked to terms where relevant;
* implementation concerns remain marked as implementation concerns.

### 15.4 Pass 4 Quality Gate

The gap routing playbook is ready only when:

* each gap classification has a closure path;
* each classification has routing tests;
* known current gaps route cleanly;
* architecture escalation triggers are explicit;
* product/problem evidence gaps are not misrouted as platform-spec details.

### 15.5 Pass 5 Quality Gate

The coherence audit is ready only when:

* all decision records pass source-anchor checks;
* all vocabulary terms pass anchor checks;
* all known gaps route cleanly;
* no rejected alternative is treated as settled;
* no implementation detail is promoted;
* S00 simplicity remains protected.

---

## 16. Handoff Capsule

* This pass produced:

  * The decision-anchor extraction charter.
  * The artifact sequence for the decision-anchor package.
  * The decision-domain taxonomy.
  * The decision-record template.
  * The permanence class definitions.
  * Inclusion and exclusion rules.
  * Gap classification and closure-path rules.
  * Architecture escalation checks.
  * Quality gates for later passes.
* Stable items for next pass:

  * Artifact sequence:

    * `010-candidate-architecture-decision-inventory.md`
    * `011-core-architecture-decision-records.md`
    * `012-vocabulary-anchor-map.md`
    * `013-gap-routing-playbook.md`
    * `014-architecture-decision-coherence-audit.md`
  * Decision domains:

    * `EVENT`
    * `IDENTITY`
    * `CONFLICT`
    * `AUTH`
    * `CONFIG`
    * `WORKFLOW`
    * `PROJECTION`
    * `GUARD`
    * `BOUNDARY`
  * Permanence classes:

    * Structural
    * Strategy-protecting
    * Initial strategy
  * Gap classifications:

    * Product/problem evidence gap
    * Architecture decision gap
    * Platform-spec detail gap
    * Implementation/tooling gap
    * Operational policy gap
* Items not yet stable:

  * Candidate decision list.
  * Final decision records.
  * Vocabulary-to-decision map.
  * Known gap routing examples.
  * Final coherence audit.
* Required next input:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * this charter
* Known risks:

  * Over-splitting decisions into ADR sub-decision replicas.
  * Over-merging decisions into broad ADR-level buckets.
  * Promoting open fronts to settled decisions.
  * Treating implementation or platform-spec details as architecture.
  * Losing S00 simplicity while organizing complex decisions.
* Do not reinterpret:

  * This charter does not create architecture.
  * This charter does not settle any new platform term.
  * This charter does not override the CDL, contracts, current accepted baseline evidence, or the original `002`/`008` recovery lineage.
* Next pass should start from:

  * `010-candidate-architecture-decision-inventory.md`
  * Scope: candidate inventory only.
  * No final decision records.
  * No vocabulary anchor map.
  * No gap routing playbook.
