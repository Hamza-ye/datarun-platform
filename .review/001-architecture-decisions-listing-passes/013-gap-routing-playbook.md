# 013 — Gap Routing Playbook

## Context Capsule

* Artifact: `013-gap-routing-playbook.md`
* Pass: Pass 4 — Gap Routing Playbook
* Status: Draft gap-routing artifact for project-source inclusion
* Mode: Gap classification and routing only; no redesign, no reopening, no new architecture decisions.
* Current authority note:

  * This playbook is downstream of `011` and `012`.
  * Current architecture authority remains the Canonical Decision Ledger.
  * Contracts govern crossed wire/process boundaries, and BAR/NW/IDR evidence remains validation input until folded by the catch-up waves.
  * `002`, `007`, and `008` remain recovery lineage for the original extraction pass.
  * This file still contains stable rules and known-gap examples; Wave 4 of `017` will split volatile known gaps into a separate register.
* Recovery verification anchor:

  * `002-phase0-decision-register.md`
* Recovered architecture-map reference:

  * `008-authoritative-architecture-map.md`
* Consumes previous artifacts:

  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
  * `011-core-architecture-decision-records.md`
  * `012-vocabulary-anchor-map.md`
* Input sources:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
  * `011-core-architecture-decision-records.md`
  * `012-vocabulary-anchor-map.md`
  * `platform-introduction-problem-definition-and-user-scenario-index.md`
  * `scenarios.md`
* Supporting lineage sources:

  * `003-phase1-adr2-identity-conflict-recovery.md`
  * `004-phase2-adr3-auth-sync-recovery.md`
  * `005-phase3-adr4-config-boundary-recovery.md`
  * `006-phase4-adr5-state-progression-recovery.md`
* Purpose:

  * Define how future gaps, proposals, scenario pressures, and platform-spec questions are routed.
  * Prevent product/problem, platform-spec, implementation, tooling, and operational-policy work from being mistaken for architecture.
  * Make future gap closure mechanically traceable through vocabulary terms and DEC anchors.
* Scope:

  * Define allowed gap classifications.
  * Define closure path per classification.
  * Define architecture escalation triggers.
  * Define routing algorithm.
  * Provide gap record template.
  * Point current known gaps to `013-known-gap-routing-register.md`.
  * Preserve embedded recovery-pass examples only as legacy detail until physical pruning.
  * Show how to consume `011` and `012` before proposing changes.
* Non-goals:

  * Do not close the listed gaps.
  * Do not write new ADR decisions.
  * Do not revise `011` decision records.
  * Do not revise `012` vocabulary anchors.
  * Do not define platform-spec language for individual gaps.
  * Do not define implementation mechanisms.
  * Do not define operational policies.
  * Do not thicken product scenarios.
* Settled outputs:

  * Gap classification set.
  * Closure path set.
  * Routing algorithm.
  * Escalation triggers.
  * Gap record template.
  * Known-gap register pointer.
  * Proposal classification examples.
* Rejected / excluded:

  * Treating open fronts as already-settled architecture.
  * Treating scenario pressure as architecture without DEC anchor.
  * Treating implementation/tooling details as architecture.
  * Treating operational governance choices as architecture.
  * Treating platform-spec detail as architecture when it stays within accepted boundaries.
* Deferred / open:

  * Coherence audit.
  * Full line-level source citation audit, if later needed.
  * Any formal decision memo or ADR for architecture decision gaps.
  * Any platform-spec detailing documents.
  * Any engineering designs, spikes, or implementation tickets.
  * Any operational policy documents.
  * Any scenario thickening/product discovery artifacts.
* Terms or decisions locked:

  * No new platform runtime term is introduced by this pass.
  * This pass locks routing procedure only, subject to coherence audit.
* Count confirmation:

  * `011` has been corrected/audited to 36 normalized decision records.
  * `012` and `013` map against the actual DEC ID set, including `DEC-WORKFLOW-07`.
  * No DEC ID is removed or renumbered by this correction.
* Next-pass handoff:

  * Pass 5 should produce `014-architecture-decision-coherence-audit.md`.
  * The audit should verify decision counts, source anchors, vocabulary mappings, negative boundaries, known-gap routing, and S00 simplicity protection.

---

## 1. Pass Checkpoint

This pass follows the Pass 4 scope from `009-decision-anchor-extraction-charter.md`.

Required output:

```txt
gap / proposal / scenario pressure
→ affected vocabulary
→ owning DEC anchor(s)
→ boundary checks
→ gap classification
→ closure path
```

This playbook is operational. It is meant to be used before writing:

* architecture decision memos;
* platform-spec sections;
* implementation designs;
* operational policies;
* product/problem discovery artifacts.

---

## 2. Core Routing Rule

Use this rule for every future gap:

```txt
Do not ask “what design do we want?”
First ask “what boundary does this touch?”
```

Routing sequence:

```txt
1. Name the gap.
2. Identify affected vocabulary.
3. Look up primary DEC owner in 012.
4. Read owning DEC record in 011.
5. Check negative boundaries.
6. Check whether a structural contract changes.
7. Classify the gap.
8. Choose the closure path.
9. Escalate only if the closure path requires it.
```

---

## 3. Allowed Gap Classifications

Only these classifications are allowed.

| Classification               | Use when                                                                                                                                                                                                                 | Closure path                             |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------- |
| Architecture decision gap    | The issue may change a settled architecture boundary, stored event contract, identity reference rule, sync/access authority, workflow truth model, trigger behavior, deployer configuration power, or negative boundary. | Formal architecture decision.            |
| Platform-spec detail gap     | The issue needs precise product/platform behavior under an accepted architecture boundary and does not change the boundary.                                                                                              | Platform-spec detailing.                 |
| Implementation/tooling gap   | The issue concerns database, APIs, SDKs, local storage, queues, indexes, UI, config syntax, migration mechanics, build tooling, or engineering mechanics without changing architecture.                                  | Implementation/tooling design.           |
| Operational policy gap       | The issue concerns who is allowed to do something, review practice, escalation, support procedure, retention window, governance, or organizational rule without changing architecture.                                   | Operational policy definition.           |
| Product/problem evidence gap | The issue exists because the scenario, user journey, operational evidence, acceptance criteria, edge cases, or real-world artifacts are too thin to safely specify behavior or architecture.                             | Product discovery / scenario thickening. |

No other gap classification should be introduced unless the coherence audit updates this taxonomy.

---

## 4. Closure Paths

Use exactly these closure paths.

### 4.1 Formal architecture decision

Use when the gap is an architecture decision gap.

Required output:

```txt
Focused decision memo or ADR-style note
that explicitly names the baseline item it changes or extends.
```

Must include:

* affected DEC anchor(s);
* affected vocabulary;
* current accepted boundary;
* proposed change;
* negative boundaries checked;
* historical/deployed-device impact;
* S00 simplicity impact;
* migration or dual-semantics implications;
* accepted/rejected alternatives.

### 4.2 Platform-spec detailing

Use when the gap is a platform-spec detail gap.

Required output:

```txt
Platform-spec language under an existing architecture boundary.
```

No new ADR is needed unless the detail changes the boundary.

Must include:

* owning DEC anchor(s);
* vocabulary terms used;
* behavior under normal path;
* behavior under offline/stale/conflict path;
* acceptance criteria;
* non-responsibilities;
* examples that remain domain-neutral.

### 4.3 Implementation/tooling design

Use when the gap is an implementation/tooling gap.

Required output:

```txt
Engineering design, prototype, spike, or tickets
that preserve baseline constraints.
```

Must include:

* affected DEC anchor(s);
* architectural guardrails;
* data/storage/API/tooling choices;
* migration/compatibility notes where relevant;
* tests proving boundary preservation.

### 4.4 Operational policy definition

Use when the gap is an operational policy gap.

Required output:

```txt
Product/operations policy.
```

Escalate only if the policy requires architecture change.

Must include:

* decision authority;
* review/escalation procedure;
* resolver or role practice;
* retention or support procedure;
* audit expectations;
* exception handling.

### 4.5 Product discovery / scenario thickening

Use when the gap is a product/problem evidence gap.

Required output:

```txt
SME validation, concrete journeys, edge cases,
acceptance criteria, artifacts, deployment archetypes,
and scenario phasing.
```

Escalate only if the thickened scenario creates architecture pressure.

Must include:

* actors;
* operational journey;
* artifacts used in the real world;
* offline/stale/conflict cases;
* scale/sensitivity assumptions;
* acceptance criteria;
* unresolved questions.

---

## 5. Architecture Escalation Triggers

Classify as an architecture decision gap if the proposal changes any of the following.

### 5.1 Event source-of-truth or envelope

Escalate if it changes:

* append-only event source of truth;
* event as atomic write unit;
* event as sync unit;
* event identity;
* event envelope fields;
* stored event semantics;
* event type vocabulary;
* payload/schema interpretation contract.

Relevant DEC anchors:

* `DEC-EVENT-01`
* `DEC-EVENT-02`
* `DEC-EVENT-03`
* `DEC-EVENT-04`
* `DEC-CONFIG-01`
* `DEC-CONFIG-02`

### 5.2 Identity and lineage

Escalate if it changes:

* typed identity reference protocol;
* identity category set;
* subject merge/split semantics;
* alias resolution order;
* raw-reference detection;
* original subject_ref authorization rule;
* causal metadata contract.

Relevant DEC anchors:

* `DEC-IDENTITY-01`
* `DEC-IDENTITY-02`
* `DEC-IDENTITY-03`
* `DEC-IDENTITY-04`

### 5.3 Conflict, flags, and resolution

Escalate if it changes:

* accept-and-flag model;
* detect-before-act;
* single-writer resolution;
* flag dimensions;
* platform-level resolvability;
* manual_only vs auto_eligible boundary;
* source-only flagging;
* stored downstream flag propagation rejection.

Relevant DEC anchors:

* `DEC-CONFLICT-01`
* `DEC-CONFLICT-02`
* `DEC-CONFLICT-03`
* `DEC-CONFLICT-04`
* `DEC-WORKFLOW-06`
* `DEC-WORKFLOW-07`

### 5.4 Authorization and sync

Escalate if it changes:

* assignment-based access;
* sync scope equals access scope;
* authority-as-projection;
* assignment timeline authority;
* scope-containment invariant;
* alias-respects-original-scope;
* selective-retain baseline if changed into a new access contract.

Relevant DEC anchors:

* `DEC-AUTH-01`
* `DEC-AUTH-02`
* `DEC-AUTH-03`
* `DEC-AUTH-04`
* `DEC-AUTH-05`
* `DEC-IDENTITY-04`

### 5.5 Configuration boundary

Escalate if it changes:

* shape_ref mandatory historical schema contract;
* activity_ref optional activity-instance contract;
* event type closure;
* system actor protocol;
* L0–L3 configuration gradient;
* deployer configuration power;
* expression language scope;
* server-only trigger model;
* fixed scope types;
* sensitivity boundary.

Relevant DEC anchors:

* `DEC-CONFIG-01`
* `DEC-CONFIG-02`
* `DEC-CONFIG-03`
* `DEC-CONFIG-04`
* `DEC-CONFIG-05`
* `DEC-CONFIG-06`
* `DEC-CONFIG-07`
* `DEC-CONFIG-08`

### 5.6 Workflow and state progression

Escalate if it changes:

* projection-derived workflow state;
* rejected stored current_state;
* Pattern Registry platform-fixed boundary;
* deployer-authored state machine rejection;
* pattern composition constraints;
* transition_violation semantics;
* context.* closed vocabulary;
* auto-resolution guardrails.

Relevant DEC anchors:

* `DEC-WORKFLOW-01`
* `DEC-WORKFLOW-02`
* `DEC-WORKFLOW-03`
* `DEC-WORKFLOW-04`
* `DEC-WORKFLOW-05`
* `DEC-WORKFLOW-07`

### 5.7 Projection/read-model semantics

Escalate if it changes:

* projection as derived read model;
* reporting/analytics projections as read-side summaries;
* projections not being source truth;
* aggregate access bypassing event/access semantics.

Relevant DEC anchors:

* `DEC-PROJECTION-01`
* `DEC-PROJECTION-02`
* `DEC-AUTH-02`

### 5.8 Negative boundary or S00 simplicity

Escalate if it violates:

* negative boundary register;
* S00 simplicity baseline;
* basic capture remaining simple.

Relevant DEC anchors:

* `DEC-BOUNDARY-01`
* `DEC-BOUNDARY-02`

---

## 6. Non-Architecture Routing Rules

### 6.1 Platform-spec detail, not architecture

Classify as platform-spec detail when the proposal:

* fills in behavior under a DEC anchor;
* defines user-visible platform semantics without changing event fields or authority model;
* defines exact pattern skeletons under Pattern Registry;
* defines role-action tables under assignment-based access;
* defines reporting semantics under reporting projections;
* defines setup lifecycle under config package validation;
* defines handoff package content under access/sync/projection boundaries.

Test:

```txt
Can this be specified without changing any DEC decision?
```

If yes, platform-spec detail.

---

### 6.2 Implementation/tooling, not architecture

Classify as implementation/tooling when the proposal concerns:

* database schema;
* tables;
* indexes;
* queues;
* local storage;
* API shape;
* SDK behavior;
* config authoring syntax;
* form renderer UI;
* admin UI;
* report dashboard UI;
* sync batching;
* sync pagination;
* retry mechanics;
* projection materialization;
* migration implementation.

Test:

```txt
Could two implementations choose different mechanisms
while preserving all DEC decisions?
```

If yes, implementation/tooling.

---

### 6.3 Operational policy, not architecture

Classify as operational policy when the proposal concerns:

* who may approve;
* who may resolve;
* who reviews;
* who escalates;
* support procedures;
* governance rules;
* retention windows;
* offboarding procedures;
* assignment review practice;
* audit procedure.

Test:

```txt
Is this about organizational authority/practice
rather than platform invariant?
```

If yes, operational policy.

Escalate only if the policy requires new platform behavior that changes a DEC boundary.

---

### 6.4 Product/problem evidence, not architecture

Classify as product/problem evidence when:

* scenario journey is thin;
* actor needs are unclear;
* real-world artifacts are missing;
* acceptance criteria are missing;
* edge cases are not known;
* deployment archetypes are not defined;
* operational pressure is asserted but not demonstrated.

Test:

```txt
Do we know enough real-world behavior to specify or decide?
```

If no, product/problem evidence.

---

## 7. Gap Record Template

Use this template for every future gap.

```md
## GAP-<AREA>-<NN>: <Short name>

Classification:
Baseline-extension category:
Current owner or likely decision path:
Affected vocabulary:
Affected DEC anchors:
Baseline item affected:
Why it is still open:
Negative boundaries checked:
Architecture escalation trigger:
Closure path:
Closure subtype:
Required output:
Notes:
```

### Required field meaning

| Field                                 | Meaning                                                                       |
| ------------------------------------- | ----------------------------------------------------------------------------- |
| Short name                            | Stable label for discussion.                                                  |
| Classification                        | One of the five allowed classifications.                                      |
| Baseline-extension category           | One of: `Platform evolution that does not violate accepted decisions`, `Expansion of an explicitly open front`, `Work on a front that is still underexplored or not settled`, or `Not applicable`. |
| Current owner or likely decision path | Product, platform spec, architecture, engineering, operations, or mixed path. |
| Affected vocabulary                   | Terms from `012-vocabulary-anchor-map.md`.                                    |
| Affected DEC anchors                  | DEC records from `011-core-architecture-decision-records.md`.                 |
| Baseline item affected                | Existing architecture/spec/problem item touched.                              |
| Why it is still open                  | Reason the current baseline does not settle it.                               |
| Negative boundaries checked           | Rejected paths relevant to the gap.                                           |
| Architecture escalation trigger       | Condition under which it becomes architecture.                                |
| Closure path                          | One of the approved closure paths.                                            |
| Closure subtype                       | Optional note such as `platform evolution memo`, `platform-spec detail`, `engineering spike`, or `policy document`; must not replace the approved closure path. |
| Required output                       | Concrete artifact needed to close it.                                         |
| Notes                                 | Constraints, examples, or dependencies.                                       |

---

## 7.1 Stable Routing Rules Absorbed From Companion

This section transfers the active routing role of `docs/agent-working-surface/architecture-rationale-and-routing-companion.md` into `013`. It is compressed intentionally; source-to-card history and exploration provenance remain reference material.

### Authority And Use Rule

This playbook is a router, not a decision ledger.

Use it to:

* classify proposed work before implementation;
* decide whether a request needs architecture, platform-spec, implementation, policy, or product/problem evidence;
* check irreversibility and negative boundaries;
* prepare bounded implementation prompts.

Do not use it to override the CDL, contracts, accepted baseline evidence, or patched `011`/`012` anchors.

### Irreversibility Filter

Escalate to architecture when a proposal:

* changes stored event/envelope semantics;
* creates permanent dual semantics or deployed-device compatibility burden;
* moves authority, state, or resolver truth into a new durable source;
* changes sync/access scope;
* weakens deploy-time validation or the mechanism/instance split;
* promotes an open/future-decision front into runtime behavior.

If none of those tests fire, route as platform-spec, implementation/tooling, operational policy, or product/problem evidence.

### Configuration Guardrails

Reject or escalate proposals that:

* turn deployer config into arbitrary code;
* let config author access-control logic, containment functions, resolver authority, triggers with side effects on devices, or workflow state machines outside platform-owned bounds;
* bypass config-package atomicity, dependency validation, or version coexistence.

Allowed configuration work stays inside the platform-fixed mechanism/deployer instance split and preserves deploy-time validation.

### Config Lifecycle And Cascade Rule

Runtime behavior should follow two lifecycle shapes:

* event-coupled facts stay immutable and correction-based;
* config-package changes publish as atomic snapshots with device-safe promotion.

Dependencies must cascade through validation before publish. A dependent config artifact must not silently survive when its provider becomes invalid or incompatible.

### Device/Server Evaluation Contract

Server evaluation is authoritative for acceptance, conflict detection, resolver authority, assignment administration, production auth, and L3 policy.

Device evaluation is advisory for UX and projection repair. Device logic may warn, hide, stage, or project, but must not become the source of authority or reject structurally valid policy/state anomalies that the platform should accept and flag.

### Do-Not-Promote Reminders

Do not promote these into authority without the named route:

* UI vocabulary;
* scenario labels;
* implementation table/API names;
* IdP claims or groups;
* mobile-selected actor identity;
* config authoring syntax;
* local retention mechanics;
* reporting aggregates;
* broad audit/history access;
* emergency override semantics.

### Implementation Prompt Checklist

Before implementation, a task packet should state:

* goal;
* files to read, capped and justified;
* authority and guardrails;
* forbidden work;
* expected boundary;
* targeted tests;
* commit boundary;
* stop-and-report conditions.

### Architecture Test Seed Backlog

When a route changes behavior, prefer tests that pin:

* envelope vocabulary and schema parity;
* projection rebuildability/equivalence;
* identity merge/split/alias ordering;
* scope-filtered sync and subject-history separation;
* assignment containment and command capability;
* config package contract and deploy-time validation;
* pattern projection and transition-violation behavior;
* resolver equality and unauthorized-resolution handling.

### Escape-Hatch And Platform-Evolution Routing

Use `docs/agent-working-surface/escape-hatch-register.md` only when a documented B-to-C escape hatch has been explicitly activated.

If no escape hatch is active, route platform evolution through:

```txt
013 classification
-> 013-known-gap-routing-register if covered
-> architecture/platform-spec/policy/product artifact
-> bounded implementation prompt
```

Do not implement an escape hatch from pressure alone.

---

# 8. Legacy Embedded Known-Gap Detail

The active known-gap surface is now:

```txt
.review/001-architecture-decisions-listing-passes/013-known-gap-routing-register.md
```

The detailed examples below are retained as recovery-pass reference until physical pruning. They are not the active known-gap register.

---

## 8.1 Auditor/query access

Classification: Architecture decision gap

Baseline-extension category: Work on a front that is still underexplored or not settled

Current owner or likely decision path:

```txt
Architecture decision first,
then platform-spec and operational policy.
```

Affected vocabulary:

* `sync scope = access scope`
* `access scope`
* `authority-as-projection`
* `assignment timeline`
* `reporting/analytics projections`
* `auditor/query access`

Affected DEC anchors:

* `DEC-AUTH-01`
* `DEC-AUTH-02`
* `DEC-AUTH-03`
* `DEC-PROJECTION-02`

Baseline item affected:

```txt
Sync scope equals access scope.
Authority is projection-derived from assignment timeline.
```

Why it is still open:

```txt
Auditor/query access cuts across normal assignment scopes.
The accepted baseline does not settle whether this is an assignment type,
a query-only access surface, a reporting-only scope, or a separate access model.
```

Negative boundaries checked:

* sync independent of access is rejected;
* authority_context in envelope is rejected;
* arbitrary deployer-authored access logic is rejected;
* aggregate access bypassing access rules is rejected.

Architecture escalation trigger:

```txt
Any auditor/query model that allows visibility outside normal assignment-derived access
or changes sync=access requires formal architecture decision.
```

Closure path:

```txt
Formal architecture decision
```

Required output:

```txt
Focused decision memo defining auditor/query access relationship to assignment,
sync scope, reporting projections, and operational audit policy.
```

---

## 8.2 Aggregate access semantics

Classification: Architecture decision gap

Baseline-extension category: Work on a front that is still underexplored or not settled

Current owner or likely decision path:

```txt
Architecture triage first, then platform-spec.
```

Affected vocabulary:

* `reporting/analytics projections`
* `read-side summaries`
* `access scope`
* `sync scope = access scope`
* `flag-aware reporting`

Affected DEC anchors:

* `DEC-PROJECTION-02`
* `DEC-AUTH-02`
* `DEC-AUTH-03`
* `DEC-CONFLICT-04`

Baseline item affected:

```txt
Reporting projections are read-side summaries.
Sync and access are coupled.
```

Why it is still open:

```txt
The baseline says reporting/analytics projections are read-side summaries,
but does not settle whether an actor may see aggregates derived from events
they could not otherwise access.
```

Negative boundaries checked:

* aggregate access bypassing access rules is outside the accepted baseline;
* reporting summary as source truth is rejected;
* hiding unresolved issues inside aggregates without specification is rejected.

Architecture escalation trigger:

```txt
Any proposal to expose aggregate facts beyond event-level access scope.
```

Closure path:

```txt
Formal architecture decision
```

Closure subtype:

```txt
Platform-spec detailing only if later triage confirms aggregates inherit event-level access.
```

Required output:

```txt
Architecture memo or reporting platform spec defining access, freshness,
flag treatment, and drilldown behavior.
```

---

## 8.3 Actor-as-subject delivery rule

Classification: Architecture decision gap

Baseline-extension category: Work on a front that is still underexplored or not settled

Current owner or likely decision path:

```txt
Architecture triage with product/problem evidence support.
```

Affected vocabulary:

* `actor`
* `subject`
* `sync scope = access scope`
* `assignment-based access`
* `access scope`
* `actor-as-subject visibility`

Affected DEC anchors:

* `DEC-IDENTITY-01`
* `DEC-AUTH-01`
* `DEC-AUTH-02`
* `DEC-AUTH-03`

Baseline item affected:

```txt
Actor and subject are separate identity categories.
Sync scope equals access scope.
```

Why it is still open:

```txt
The baseline rejects treating actor identity as authority.
It also does not settle whether actors automatically receive records where they are the subject,
especially when normal assignment scope would not deliver those records.
```

Negative boundaries checked:

* actor identity as authority is rejected;
* sync independent of access is rejected;
* adding a new identity category is not required by default.

Architecture escalation trigger:

```txt
Any rule that delivers data outside assignment-derived access,
or creates a new non-assignment sync dimension.
```

Closure path:

```txt
Formal architecture decision
```

Closure subtype:

```txt
Product discovery / scenario thickening first if concrete journeys are unclear.
```

Required output:

```txt
Scenario-thickening note plus architecture decision if the final rule changes sync/access.
```

---

## 8.4 Exact role-action table artifact

Classification: Platform-spec detail gap

Baseline-extension category: Not applicable

Current owner or likely decision path:

```txt
Platform specification.
```

Affected vocabulary:

* `assignment-based access`
* `role permits action`
* `scope-containment test`
* `scope composition`

Affected DEC anchors:

* `DEC-AUTH-01`
* `DEC-CONFIG-08`

Baseline item affected:

```txt
Access requires active assignment, scope containment, and role permission.
Exact role-action tables are not defined.
```

Why it is still open:

```txt
ADR-003 settles the access model but defers role-action permission tables.
```

Negative boundaries checked:

* role-only RBAC is rejected;
* deployer-authored access logic is rejected;
* arbitrary ABAC is rejected.

Architecture escalation trigger:

```txt
Only if the table mechanism lets deployers author arbitrary access-control logic
or changes the assignment/scope containment model.
```

Closure path:

```txt
Platform-spec detailing
```

Required output:

```txt
Role-action matrix specification under assignment-based access.
```

---

## 8.5 Exact Pattern Registry inventory

Classification: Platform-spec detail gap

Baseline-extension category: Expansion of an explicitly open front

Current owner or likely decision path:

```txt
Platform specification under Pattern Registry.
Architecture decision only if new platform processing behavior is required.
```

Affected vocabulary:

* `Pattern Registry`
* `pattern`
* `state machine skeleton`
* `subject-level pattern`
* `event-level pattern`
* `composition rules`

Affected DEC anchors:

* `DEC-WORKFLOW-02`
* `DEC-WORKFLOW-03`
* `DEC-WORKFLOW-01`

Baseline item affected:

```txt
Pattern Registry is platform-fixed; exact inventory is open.
```

Why it is still open:

```txt
Accepted architecture settles the boundary but not the exact skeleton catalog.
```

Negative boundaries checked:

* deployer-authored state machines are rejected;
* frozen scenario pattern examples as architecture inventory are rejected;
* pattern_ref in envelope is rejected.

Architecture escalation trigger:

```txt
A new pattern requires a new structural event type, new envelope field,
new deployer-authored state machine capability, or different workflow truth model.
```

Closure path:

```txt
Platform-spec detailing
```

Closure subtype:

```txt
Platform evolution only for new pattern types shipped by the platform.
```

Required output:

```txt
Pattern Registry platform-spec section defining skeletons, slots, transition tables,
composition constraints, and examples.
```

---

## 8.6 Pattern migration mechanics

Classification: Implementation/tooling gap

Baseline-extension category: Expansion of an explicitly open front

Current owner or likely decision path:

```txt
Engineering design first, architecture review only if semantics change.
```

Affected vocabulary:

* `Pattern Registry`
* `pattern`
* `workflow state projection`
* `config version`
* `projection`
* `rebuildable`

Affected DEC anchors:

* `DEC-WORKFLOW-02`
* `DEC-WORKFLOW-01`
* `DEC-PROJECTION-01`
* `DEC-CONFIG-08`

Baseline item affected:

```txt
Patterns are platform-fixed and selected/parameterized through config.
Workflow state is projection-derived.
```

Why it is still open:

```txt
The baseline does not define how pattern definitions evolve across deployed configurations.
```

Negative boundaries checked:

* stored current_state is rejected;
* pattern_ref in envelope is rejected;
* event rewriting for pattern migration is rejected.

Architecture escalation trigger:

```txt
Any migration that changes historical workflow interpretation,
requires stored state as truth, or adds event envelope fields.
```

Closure path:

```txt
Implementation/tooling design
```

Closure subtype:

```txt
Architecture review only if historical interpretation or stored state semantics change.
```

Required output:

```txt
Engineering design or spike for pattern versioning and projection rebuild behavior.
```

---

## 8.7 Additional `context.*` values

Classification: Architecture decision gap

Baseline-extension category: Platform evolution that does not violate accepted decisions

Current owner or likely decision path:

```txt
Platform evolution proposal.
```

Affected vocabulary:

* `context.*`
* `context.subject_state`
* `context.actor.role`
* `expression language`
* pre-resolved context

Affected DEC anchors:

* `DEC-WORKFLOW-05`
* `DEC-CONFIG-06`
* `DEC-EVENT-03`

Baseline item affected:

```txt
context.* is a closed, pre-resolved, read-only expression scope.
```

Why it is still open:

```txt
Architecture allows additional platform-fixed context values but does not define them.
```

Negative boundaries checked:

* deployer-defined context.* is rejected;
* dynamic query behavior is rejected;
* stored context values as event facts are rejected.

Architecture escalation trigger:

```txt
If the new context value is deployer-defined, dynamic, recursive, side-effectful,
or stored in the event envelope.
```

Closure path:

```txt
Formal architecture decision
```

Closure subtype:

```txt
Platform evolution memo for closed platform-fixed context vocabulary.
```

Required output:

```txt
Platform evolution note defining the new context value, source projection,
offline behavior, performance bounds, and negative-boundary compliance.
```

---

## 8.8 Additional auto-resolution policies

Classification: Platform-spec detail gap

Baseline-extension category: Platform evolution that does not violate accepted decisions

Current owner or likely decision path:

```txt
Platform evolution under auto-resolution guardrails.
```

Affected vocabulary:

* `auto-resolution`
* `auto_eligible`
* `manual_only`
* `system:auto_resolution/{policy_id}`
* `flag resolvability`

Affected DEC anchors:

* `DEC-WORKFLOW-07`
* `DEC-CONFLICT-04`
* `DEC-CONFIG-03`

Baseline item affected:

```txt
Auto-resolution is bounded L3b and only applies to auto_eligible flags.
```

Why it is still open:

```txt
The baseline defines the boundary but not every possible auto-resolution policy.
```

Negative boundaries checked:

* auto-resolution of manual-only flags is rejected;
* auto-resolution as unbounded rule engine is rejected;
* device-side auto-resolution is rejected;
* recursive resolution loops are rejected.

Architecture escalation trigger:

```txt
Any policy that touches manual_only categories,
uses unbounded logic, or creates recursive downstream effects.
```

Closure path:

```txt
Platform-spec detailing
```

Closure subtype:

```txt
Platform evolution detail within accepted auto_eligible/L3b guardrails.
```

Required output:

```txt
Policy-specific platform evolution note defining eligibility,
inputs, system actor, source event handling, and loop prevention.
```

---

## 8.9 Flag queue ergonomics

Classification: Platform-spec detail gap

Baseline-extension category: Expansion of an explicitly open front

Current owner or likely decision path:

```txt
Platform spec for semantics; tooling/UI design for presentation.
```

Affected vocabulary:

* `flag`
* `flag queue projection`
* `resolver`
* `source_event_ref`
* `source-chain traversal`
* `single-writer resolution`

Affected DEC anchors:

* `DEC-CONFLICT-03`
* `DEC-CONFLICT-04`
* `DEC-WORKFLOW-06`
* `DEC-PROJECTION-01`

Baseline item affected:

```txt
Flags have source/resolver dimensions and source-chain traversal.
```

Why it is still open:

```txt
Architecture settles flag semantics but not backlog grouping,
review ergonomics, or queue UX.
```

Negative boundaries checked:

* collapsing severity and resolvability is rejected;
* multiple co-equal resolvers are rejected;
* stored downstream flag propagation is rejected.

Architecture escalation trigger:

```txt
Only if queue behavior changes resolver authority, flag dimensions,
or stored flag propagation semantics.
```

Closure path:

```txt
Platform-spec detailing
```

Closure subtype:

```txt
Implementation/tooling design for UX.
```

Required output:

```txt
Flag queue platform spec and UX/tooling design.
```

---

## 8.10 Domain conflict resolution strategies

Classification: Platform-spec detail gap

Baseline-extension category: Expansion of an explicitly open front

Current owner or likely decision path:

```txt
Platform spec first; platform evolution if new policy machinery is needed.
```

Affected vocabulary:

* `domain_uniqueness_violation`
* `flag`
* `severity`
* `manual_only`
* `auto_eligible`
* `accept-and-flag`

Affected DEC anchors:

* `DEC-CONFLICT-01`
* `DEC-CONFLICT-04`
* `DEC-CONFIG-08`
* `DEC-WORKFLOW-07`

Baseline item affected:

```txt
Domain uniqueness and conflict categories reuse the flag model.
```

Why it is still open:

```txt
Architecture defines bounded flag and policy surfaces,
but does not specify every domain conflict strategy.
```

Negative boundaries checked:

* deployer-defined flag mechanism is rejected;
* deployer-configured resolvability is rejected;
* auto-resolution of manual-only flags is rejected;
* stale event rejection is rejected.

Architecture escalation trigger:

```txt
Any strategy that introduces new flag dimensions, new event types,
new deployer-authored logic, or auto-resolves manual-only cases.
```

Closure path:

```txt
Platform-spec detailing
```

Closure subtype:

```txt
Platform evolution only if new policy machinery is needed; formal architecture decision if structural contracts change.
```

Required output:

```txt
Domain conflict strategy specification with category, severity,
resolvability, resolver, source-chain, and projection effects.
```

---

## 8.11 Config authoring syntax

Classification: Implementation/tooling gap

Baseline-extension category: Not applicable

Current owner or likely decision path:

```txt
Engineering/tooling design.
```

Affected vocabulary:

* `L0 Assembly`
* `L1 Shape`
* `L2 Logic`
* `L3 Policy`
* `expression language`
* `config package`

Affected DEC anchors:

* `DEC-CONFIG-05`
* `DEC-CONFIG-06`
* `DEC-CONFIG-08`

Baseline item affected:

```txt
Configuration has bounded layers and atomic delivery.
```

Why it is still open:

```txt
Architecture defines configuration boundaries, not authoring syntax.
```

Negative boundaries checked:

* configuration as arbitrary code is rejected;
* expressions as programming language are rejected;
* deployer-authored state machines are rejected.

Architecture escalation trigger:

```txt
If syntax allows arbitrary code, unbounded functions, dynamic queries,
deployer-authored access logic, or deployer-authored state machines.
```

Closure path:

```txt
Implementation/tooling design
```

Required output:

```txt
Config authoring format/tooling design with validation against DEC-CONFIG guardrails.
```

---

## 8.12 Setup lifecycle for new operational activity

Classification: Platform-spec detail gap

Baseline-extension category: Not applicable

Current owner or likely decision path:

```txt
Platform specification, supported by product/problem evidence from scenario 23.
```

Affected vocabulary:

* `activity definition`
* `activity instance`
* `config package`
* `atomic config delivery`
* `Config Package Validator`
* `shape`
* `pattern`

Affected DEC anchors:

* `DEC-CONFIG-02`
* `DEC-CONFIG-04`
* `DEC-CONFIG-08`
* `DEC-WORKFLOW-02`

Baseline item affected:

```txt
Configuration is bounded and delivered atomically.
```

Why it is still open:

```txt
Architecture defines config boundaries but not draft/review/validate/publish lifecycle.
```

Negative boundaries checked:

* partial config delivery is rejected;
* invalid config repair at runtime is rejected;
* deployer-authored platform primitives are rejected.

Architecture escalation trigger:

```txt
Only if setup lifecycle introduces new configuration powers
or changes atomic delivery/config coexistence semantics.
```

Closure path:

```txt
Platform-spec detailing
```

Required output:

```txt
Operational activity setup lifecycle spec:
draft, validate, preview, publish, version coexistence, rollback/retirement policy,
offline delivery behavior, and acceptance criteria.
```

---

## 8.13 Reporting freshness semantics

Classification: Platform-spec detail gap

Baseline-extension category: Not applicable

Current owner or likely decision path:

```txt
Platform specification.
```

Affected vocabulary:

* `reporting/analytics projections`
* freshness
* flag-aware reporting
* read-side summaries
* `sync_watermark`
* unresolved flagged event

Affected DEC anchors:

* `DEC-PROJECTION-02`
* `DEC-IDENTITY-02`
* `DEC-CONFLICT-04`
* `DEC-AUTH-02`

Baseline item affected:

```txt
Reporting projections are read-side summaries and oversight is eventually consistent.
```

Why it is still open:

```txt
Architecture does not define freshness labels, completeness semantics,
stale data display, or unresolved flag treatment inside reports.
```

Negative boundaries checked:

* report summary as source truth is rejected;
* hiding unresolved issues inside aggregates without specification is rejected;
* aggregate access bypassing access rules is rejected.

Architecture escalation trigger:

```txt
If reports become authoritative source truth or bypass access scope.
```

Closure path:

```txt
Platform-spec detailing
```

Required output:

```txt
Reporting freshness and completeness spec:
last sync, data age, unresolved flags, drilldown, access inheritance,
and aggregate confidence semantics.
```

---

## 8.14 Handoff package contents

Classification: Platform-spec detail gap

Baseline-extension category: Not applicable

Current owner or likely decision path:

```txt
Platform specification with scenario thickening for transfer/custody journeys.
```

Affected vocabulary:

* `process`
* `assignment`
* `activity_ref`
* `source_event_ref`
* `event-level pattern`
* `sync scope = access scope`

Affected DEC anchors:

* `DEC-IDENTITY-01`
* `DEC-WORKFLOW-03`
* `DEC-WORKFLOW-06`
* `DEC-AUTH-02`
* `DEC-PROJECTION-01`

Baseline item affected:

```txt
Process identity, source-chain traversal, and access-scoped sync exist,
but handoff package content is not defined.
```

Why it is still open:

```txt
Architecture supports handoff/custody composition but does not specify
which records, projections, source chains, or summaries must be included.
```

Negative boundaries checked:

* stored downstream flag propagation is rejected;
* sync independent of access is rejected;
* process identity as activity/pattern is rejected.

Architecture escalation trigger:

```txt
If handoff requires delivery outside access scope,
new structural event fields, or new event type behavior.
```

Closure path:

```txt
Platform-spec detailing
```

Required output:

```txt
Handoff package platform spec defining included source events,
projections, unresolved flags, receipt/discrepancy behavior, and offline sync handling.
```

---

## 8.15 Retention windows

Classification: Operational policy gap

Baseline-extension category: Not applicable

Current owner or likely decision path:

```txt
Operations/product policy, with architecture review for erasure/redaction requirements.
```

Affected vocabulary:

* `selective-retain`
* `scope contraction`
* local retention
* `event store`
* `append-only`

Affected DEC anchors:

* `DEC-AUTH-05`
* `DEC-EVENT-01`
* `DEC-AUTH-02`

Baseline item affected:

```txt
Selective-retain is initial strategy for scope contraction.
Events are append-only source truth.
```

Why it is still open:

```txt
Architecture does not define per-domain retention durations,
legal retention windows, or device purge schedules.
```

Negative boundaries checked:

* retain-indefinitely is rejected as baseline;
* retain-but-hide as sufficient for sensitive data is rejected;
* event rewriting after scope contraction is rejected.

Architecture escalation trigger:

```txt
If retention policy requires deletion, redaction, erasure,
or reinterpretation of durable event facts.
```

Closure path:

```txt
Operational policy definition
```

Closure subtype:

```txt
Architecture escalation for regulatory erasure/redaction mechanisms.
```

Required output:

```txt
Retention policy with data classes, windows, purge procedures,
audit needs, and escalation for regulatory erasure/redaction.
```

---

## 8.16 Worker offboarding / exit procedure

Classification: Operational policy gap

Baseline-extension category: Not applicable

Current owner or likely decision path:

```txt
Operational policy first; platform spec for system behavior.
```

Affected vocabulary:

* `assignment`
* `scope contraction`
* `selective-retain`
* `sync scope = access scope`
* `actor`

Affected DEC anchors:

* `DEC-AUTH-01`
* `DEC-AUTH-02`
* `DEC-AUTH-05`
* `DEC-IDENTITY-01`

Baseline item affected:

```txt
Assignments grant access; scope contraction uses selective-retain.
```

Why it is still open:

```txt
Architecture does not define HR/offboarding procedure,
handoff responsibility, support workflow, or device cleanup practice.
```

Negative boundaries checked:

* actor identity as authority is rejected;
* retain-indefinitely is rejected;
* event rewriting after scope contraction is rejected.

Architecture escalation trigger:

```txt
If offboarding requires new access semantics,
data visibility outside assignment access,
or event erasure semantics.
```

Closure path:

```txt
Operational policy definition
```

Closure subtype:

```txt
Platform-spec detailing for device/session/sync behavior.
```

Required output:

```txt
Offboarding policy plus platform behavior spec for assignment end,
pending unsynced work, local retention, device cleanup, and audit.
```

---

## 8.17 Regulatory encryption/redaction/erasure

Classification: Architecture decision gap

Baseline-extension category: Platform evolution that does not violate accepted decisions

Current owner or likely decision path:

```txt
Architecture/platform evolution plus operational/legal policy.
```

Affected vocabulary:

* `append-only`
* `event store`
* `payload`
* `sensitivity classification`
* `selective-retain`
* `restricted`

Affected DEC anchors:

* `DEC-EVENT-01`
* `DEC-EVENT-03`
* `DEC-CONFIG-08`
* `DEC-AUTH-05`

Baseline item affected:

```txt
Events are append-only durable facts.
Sensitivity is shape/activity-level.
Selective-retain handles local scope contraction.
```

Why it is still open:

```txt
The accepted baseline explicitly leaves regulatory encryption,
redaction, erasure, and de-identification as separate evolution fronts.
```

Negative boundaries checked:

* mutable-in-place source truth is rejected;
* silent historical deletion is outside baseline;
* field-level sensitivity is rejected in current ADR-004 baseline.

Architecture escalation trigger:

```txt
Always escalate if regulatory handling changes event immutability,
historical interpretation, payload availability, or sensitivity boundary.
```

Closure path:

```txt
Formal architecture decision
```

Closure subtype:

```txt
Platform evolution memo plus operational/legal policy.
```

Required output:

```txt
Regulatory data-handling architecture memo and policy:
classification, encryption/redaction/erasure semantics, audit impact,
historical interpretation, local device handling, and export behavior.
```

---

## 8.18 Multi-tenant naming strategy

Classification: Architecture decision gap

Baseline-extension category: Platform evolution that does not violate accepted decisions

Current owner or likely decision path:

```txt
Platform evolution.
```

Affected vocabulary:

* `shape_ref`
* `activity_ref`
* `shape_name`
* `activity_instance_id`
* parse-safe identifiers

Affected DEC anchors:

* `DEC-CONFIG-01`
* `DEC-CONFIG-02`
* `DEC-EVENT-03`

Baseline item affected:

```txt
shape_ref and activity_ref are durable event interpretation fields.
```

Why it is still open:

```txt
Architecture preserves parse-safe identifiers but does not define multi-tenant namespace strategy.
```

Negative boundaries checked:

* silent reinterpretation under new shape is rejected;
* changing historical identifier semantics is rejected;
* self-describing payload as replacement is rejected.

Architecture escalation trigger:

```txt
If naming change alters stored event interpretation or requires historical migration.
```

Closure path:

```txt
Formal architecture decision
```

Closure subtype:

```txt
Platform evolution memo for durable naming semantics.
```

Required output:

```txt
Naming strategy note preserving parse safety, historical interpretation,
tenant isolation, migration path, and compatibility.
```

---

## 8.19 Complexity budget changes

Classification: Platform-spec detail gap

Baseline-extension category: Platform evolution that does not violate accepted decisions

Current owner or likely decision path:

```txt
Platform evolution, with product/platform evidence.
```

Affected vocabulary:

* `complexity budgets`
* `Config Package Validator`
* `expression language`
* `trigger DAG`
* `max path length 2`

Affected DEC anchors:

* `DEC-CONFIG-08`
* `DEC-CONFIG-06`
* `DEC-CONFIG-07`
* `DEC-CONFIG-05`

Baseline item affected:

```txt
Complexity budgets are initial strategy and config validation guardrails.
```

Why it is still open:

```txt
Budgets may need recalibration after real deployments.
```

Negative boundaries checked:

* unbounded complexity budgets are rejected;
* recursive trigger chains are rejected;
* configuration as arbitrary code is rejected.

Architecture escalation trigger:

```txt
If budget change weakens core guardrails enough to permit rejected paths
or breaks S00 simplicity.
```

Closure path:

```txt
Platform-spec detailing
```

Closure subtype:

```txt
Platform evolution detail within existing config-boundary guardrails.
```

Required output:

```txt
Budget recalibration note with evidence, device/offline impact,
config validation impact, and S00 simplicity check.
```

---

## 8.20 Cross-activity cohort materialization

Classification: Platform-spec detail gap

Baseline-extension category: Expansion of an explicitly open front

Current owner or likely decision path:

```txt
Platform spec first if it stays under assignment/access/activity boundaries.
Formal architecture decision if it changes access/sync semantics.
```

Affected vocabulary:

* `activity_ref`
* `activity instance`
* `assignment`
* `sync scope = access scope`
* `subject_list`
* `activity` scope type
* `reporting/analytics projections`

Affected DEC anchors:

* `DEC-CONFIG-02`
* `DEC-AUTH-01`
* `DEC-AUTH-02`
* `DEC-CONFIG-08`
* `DEC-PROJECTION-02`

Baseline item affected:

```txt
Activity_ref is optional activity-instance correlation.
Access derives from assignments and platform-fixed scope containment.
```

Why it is still open:

```txt
The baseline supports activities, subject_list scope, activity scope type,
and assignment-based access, but does not specify how cohorts from one activity
are materialized as targets or scopes for another activity.
```

Negative boundaries checked:

* sync independent of access is rejected;
* authority_context or assignment refs in envelope are rejected;
* deployer-authored access logic is rejected;
* mandatory activity_ref for all events is rejected.

Architecture escalation trigger:

```txt
If cohort materialization gives access to subjects outside assignment-derived scope,
changes sync=access, introduces dynamic cross-event queries as authority,
or requires new stored event fields.
```

Closure path:

```txt
Platform-spec detailing
```

Closure subtype:

```txt
Architecture escalation if materialization changes access/sync authority.
```

Required output:

```txt
Cross-activity cohort materialization spec defining source activity,
target activity, subject selection boundary, assignment/scope interaction,
offline sync behavior, stale cohort handling, and audit trace.
```

---

## 8.21 Cross-activity subject access for a second actor

Classification: Platform-spec detail gap

Baseline-extension category: Expansion of an explicitly open front

Current owner or likely decision path:

```txt
Platform spec if access is granted through existing assignments/scopes.
Architecture decision if a new authority path is introduced.
```

Affected vocabulary:

* `assignment-based access`
* `scope-containment test`
* `sync scope = access scope`
* `activity_ref`
* `subject_list`
* `activity` scope type

Affected DEC anchors:

* `DEC-AUTH-01`
* `DEC-AUTH-02`
* `DEC-CONFIG-02`
* `DEC-CONFIG-08`

Baseline item affected:

```txt
Second actor access must come from active assignment whose scope contains target.
```

Why it is still open:

```txt
The accepted baseline does not specify every viable way to configure
cross-activity target access or bulk assignment setup.
```

Negative boundaries checked:

* authority_context in envelope is rejected;
* assignment_ref in envelope is rejected;
* sync independent of access is rejected;
* deployer-authored access logic is rejected.

Architecture escalation trigger:

```txt
If access is granted without assignment/scope containment,
or if event history from activity A becomes visible in activity B
through a rule not expressible as platform-fixed scope/access behavior.
```

Closure path:

```txt
Platform-spec detailing
```

Closure subtype:

```txt
Architecture escalation if authority model changes.
```

Required output:

```txt
Cross-activity access spec showing how second actor receives access through
assignment, subject_list/activity scope, sync delivery, and audit behavior.
```

---

## 8.22 Scenario phasing for S23–S27

Classification: Product/problem evidence gap

Baseline-extension category: Work on a front that is still underexplored or not settled

Current owner or likely decision path:

```txt
Product/platform planning.
```

Affected vocabulary:

* scenario phasing
* architectural significance
* product/problem evidence
* setup lifecycle
* data lifecycle
* aggregate oversight
* logistics handoff

Affected DEC anchors:

* none directly unless a scenario pressure is converted into architecture;
* likely references: `DEC-CONFIG-08`, `DEC-AUTH-05`, `DEC-PROJECTION-02`, `DEC-WORKFLOW-03`

Baseline item affected:

```txt
Scenario material defines problem space.
It is not architecture unless mapped to accepted decisions.
```

Why it is still open:

```txt
S23–S27 are present as scenario pressures but need explicit phasing,
significance classification, acceptance criteria, and scenario thickening.
```

Negative boundaries checked:

* scenario pressure as architecture is rejected;
* implementation convenience as architecture is rejected.

Architecture escalation trigger:

```txt
Only after thickened scenarios create pressure on accepted architecture boundaries.
```

Closure path:

```txt
Product discovery / scenario thickening
```

Required output:

```txt
Scenario phasing and significance note for S23–S27,
with actors, journeys, artifacts, edge cases, acceptance criteria,
and escalation notes.
```

---

# 9. Proposal Classification Examples

This section gives examples of how to classify future proposals.

---

## 9.1 “Add `review_status` to the event envelope”

Classification: Architecture decision gap

Affected DEC anchors:

* `DEC-EVENT-03`
* `DEC-WORKFLOW-01`
* `DEC-BOUNDARY-01`

Reason:

```txt
Adds envelope field and risks storing derived state.
```

Closure:

```txt
Formal architecture decision.
```

Likely answer under current baseline:

```txt
Rejected unless a new formal decision changes the envelope.
Use existing event types, shape payload, review events, and projections.
```

---

## 9.2 “Define the exact approval workflow skeleton”

Classification: Platform-spec detail gap

Affected DEC anchors:

* `DEC-WORKFLOW-02`
* `DEC-WORKFLOW-03`
* `DEC-WORKFLOW-01`

Reason:

```txt
Exact Pattern Registry skeleton is open platform-spec work,
provided it remains platform-fixed and selected/parameterized by deployers.
```

Closure:

```txt
Platform-spec detailing.
```

---

## 9.3 “Let deployers define their own state machines”

Classification: Architecture decision gap

Affected DEC anchors:

* `DEC-WORKFLOW-02`
* `DEC-CONFIG-05`
* `DEC-BOUNDARY-01`

Reason:

```txt
Contradicts negative boundary: deployer-authored state machines rejected.
```

Closure:

```txt
Formal architecture decision if reopened.
```

---

## 9.4 “Choose PostgreSQL tables for event storage”

Classification: Implementation/tooling gap

Affected DEC anchors:

* `DEC-EVENT-01`
* `DEC-EVENT-02`
* `DEC-EVENT-03`

Reason:

```txt
Storage mechanics do not change append-only event truth if they preserve event contract.
```

Closure:

```txt
Implementation/tooling design.
```

---

## 9.5 “Define who is allowed to merge duplicate subjects”

Classification: Operational policy gap

Affected DEC anchors:

* `DEC-IDENTITY-03`
* `DEC-CONFLICT-03`
* `DEC-AUTH-01`

Reason:

```txt
Architecture defines merge/split and single-writer resolution.
Who receives merge authority is policy/spec unless it changes the authority model.
```

Closure:

```txt
Operational policy definition with platform-spec permission table.
```

---

## 9.6 “Create an admin screen for flag queues”

Classification: Implementation/tooling gap

Affected DEC anchors:

* `DEC-CONFLICT-04`
* `DEC-WORKFLOW-06`

Reason:

```txt
UI is tooling, unless it changes flag semantics or resolver authority.
```

Closure:

```txt
Implementation/tooling design.
```

---

## 9.7 “Expose national aggregate totals to auditors without row-level access”

Classification: Architecture decision gap

Affected DEC anchors:

* `DEC-AUTH-02`
* `DEC-AUTH-03`
* `DEC-PROJECTION-02`

Reason:

```txt
Potentially exposes aggregate projection outside event-level access scope.
```

Closure:

```txt
Formal architecture decision.
```

---

## 9.8 “Add `context.last_review_result` to form expressions”

Classification: Architecture decision gap

Affected DEC anchors:

* `DEC-WORKFLOW-05`
* `DEC-CONFIG-06`
* `DEC-PROJECTION-01`

Reason:

```txt
Additional context.* value must be platform-fixed, pre-resolved, read-only,
append-only, and not a dynamic query escape hatch.
```

Closure:

```txt
Formal architecture decision with platform evolution memo.
```

---

## 9.9 “Use a spreadsheet format for config authoring”

Classification: Implementation/tooling gap

Affected DEC anchors:

* `DEC-CONFIG-05`
* `DEC-CONFIG-06`
* `DEC-CONFIG-08`

Reason:

```txt
Authoring syntax is tooling if the generated config obeys accepted boundaries.
```

Closure:

```txt
Implementation/tooling design.
```

---

## 9.10 “Bulk assign 1,000 prior-year subjects to a new activity”

Classification: Platform-spec detail gap

Affected DEC anchors:

* `DEC-AUTH-01`
* `DEC-AUTH-02`
* `DEC-CONFIG-08`
* `DEC-CONFIG-02`

Reason:

```txt
Bulk assignment/materialization can be platform-spec detail if it creates
ordinary assignment/scope artifacts under existing containment rules.
It escalates if it bypasses scope containment or sync=access.
```

Closure:

```txt
Platform-spec detailing unless authority semantics change.
```

---

# 10. Routing Decision Tree

Use this decision tree during future discussions.

```txt
Start
 |
 |-- Does it change stored event fields, event semantics, event type vocabulary,
 |   identity refs, causal metadata, sync unit, or historical interpretation?
 |      |-- yes → Architecture decision gap
 |      |-- no
 |
 |-- Does it change assignment-based access, sync=access, authority projection,
 |   scope containment, or alias-respects-original-scope?
 |      |-- yes → Architecture decision gap
 |      |-- no
 |
 |-- Does it contradict a negative boundary?
 |      |-- yes → Architecture decision gap
 |      |-- no
 |
 |-- Does it change workflow truth, Pattern Registry boundary,
 |   trigger behavior, context.* boundary, or auto-resolution guardrails?
 |      |-- yes → Architecture decision gap
 |      |-- no
 |
 |-- Does it define behavior under an accepted DEC boundary?
 |      |-- yes → Platform-spec detail gap
 |      |-- no
 |
 |-- Is it about APIs, DBs, queues, indexes, UI, SDKs,
 |   local storage, syntax, or tooling?
 |      |-- yes → Implementation/tooling gap
 |      |-- no
 |
 |-- Is it about who may do something, support/review/escalation,
 |   retention windows, or governance practice?
 |      |-- yes → Operational policy gap
 |      |-- no
 |
 |-- Is the scenario/problem too thin to specify safely?
 |      |-- yes → Product/problem evidence gap
 |      |-- no
 |
 → classify as unresolved; write a gap note and route through architecture triage.
```

---

# 11. S00 Simplicity Check

Before accepting any platform-spec or architecture proposal, check:

```txt
Does this make S00 require more than:
shape + activity + capture_only + assignment?
```

If yes, inspect against `DEC-BOUNDARY-02`.

S00 must not require:

* custom event type;
* custom access code;
* custom trigger;
* deployer-authored state machine;
* pattern_ref;
* authority_context;
* field-level sensitivity;
* workflow flag propagation;
* auto-resolution;
* complex role/action machinery beyond baseline assignment.

If the proposal burdens S00, classify as:

```txt
Architecture decision gap
```

unless it can be scoped away from the S00 path.

---

# 12. Required “Before Architecture” Checklist

Before writing any architecture proposal, answer:

```txt
1. Which vocabulary terms does this touch?
2. What is the primary DEC owner for each term?
3. Does it change a structural contract?
4. Does it weaken a strategy-protecting service?
5. Does it only detail an initial strategy?
6. Does it contradict a negative boundary?
7. Does it require migrating historical events?
8. Does it break deployed devices or offline work?
9. Does it make S00 more complex?
10. Is this really architecture, or platform spec / implementation / policy / product evidence?
```

Only proceed to architecture if the answers show a true boundary change.

---

# 13. Gap Closure Output Matrix

| Gap classification           | Required output                                            | Architecture needed?                              |
| ---------------------------- | ---------------------------------------------------------- | ------------------------------------------------- |
| Architecture decision gap    | Focused decision memo or ADR-style note                    | Yes                                               |
| Platform-spec detail gap     | Platform-spec section under DEC boundary                   | No, unless boundary changes                       |
| Implementation/tooling gap   | Engineering design, prototype, spike, or tickets           | No, unless boundary changes                       |
| Operational policy gap       | Product/operations policy                                  | No, unless policy requires architecture support   |
| Product/problem evidence gap | Scenario thickening / SME validation / acceptance criteria | No, unless evidence creates architecture pressure |

---

# 14. Pass 4 Quality Gate Check

| Gate item                                          | Status     |
| -------------------------------------------------- | ---------- |
| Gap classifications defined.                       | Satisfied. |
| Closure paths defined.                             | Satisfied. |
| Architecture escalation triggers defined.          | Satisfied. |
| Gap record template provided.                      | Satisfied. |
| Known current gaps routed.                         | Satisfied. |
| Vocabulary map consumed.                           | Satisfied. |
| Decision records consumed.                         | Satisfied. |
| No known gap closed accidentally.                  | Satisfied. |
| No implementation detail promoted to architecture. | Satisfied. |
| Open fronts remain open unless routed.             | Satisfied. |
| `011` final decision count is confirmed as 36.     | Satisfied. |

---

# 15. Handoff Capsule

* This pass produced:

  * `013-gap-routing-playbook.md`
  * allowed gap classifications;
  * closure paths;
  * architecture escalation triggers;
  * non-architecture routing rules;
  * gap record template;
  * known-gap routing table;
  * proposal classification examples;
  * routing decision tree;
  * S00 simplicity check;
  * before-architecture checklist;
  * gap closure output matrix.
* Stable items for next pass:

  * five allowed gap classifications;
  * five closure paths;
  * known-gap routing table;
  * architecture escalation trigger list;
  * S00 simplicity check;
  * use of `012` as vocabulary lookup before classification.
* Items not yet stable:

  * final coherence verdict;
  * final `011` decision count;
  * final confirmation that every DEC maps cleanly to `002` or `008`;
  * final confirmation that every vocabulary term in `012` maps correctly;
  * final confirmation that known gaps route without hidden contradiction.
* Required next input:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
  * `011-core-architecture-decision-records.md`
  * `012-vocabulary-anchor-map.md`
  * `013-gap-routing-playbook.md`
* Known audit targets:

  * Verify `011` final count remains 36 and `DEC-WORKFLOW-07` remains present.
  * Verify that `013` gap classifications match project instructions exactly.
  * Verify no open front has been treated as settled.
  * Verify S00 simplicity remains protected.
  * Verify known architecture decision gaps are not routed as platform-spec details.
  * Verify implementation/tooling examples do not weaken architecture boundaries.
* Do not reinterpret:

  * This artifact does not close gaps.
  * This artifact does not make auditor/query access settled.
  * This artifact does not make aggregate access settled.
  * This artifact does not define Pattern Registry inventory.
  * This artifact does not define cross-activity cohort materialization.
  * This artifact does not revise event, auth, sync, config, workflow, or projection boundaries.
  * This artifact does not override the CDL, contracts, current accepted baseline evidence, or the original `002`/`007`/`008` recovery lineage.
* Next pass should start from:

  * `014-architecture-decision-coherence-audit.md`
  * Scope: verify artifacts `009` through `013` against `002`, `007`, `008`, and the project drift-control rules.
  * The audit should be allowed to recommend corrections, but not silently rewrite previous artifacts.
