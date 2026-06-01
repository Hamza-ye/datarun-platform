# Architecture Rationale and Routing Companion

Status: **Non-authoritative architecture companion**
Authority: `canonical-decision-ledger.md` remains the architecture authority.
Purpose: preserve scenario-grounded reasoning, routing rules, test intent, and escape-hatch context that were compressed out of the canonical decision surface.

---

## 0. Authority and use rule

This file is a **rationale router**, not a second decision ledger.

Use this file when:

- slicing post-Phase-4 work;
- reviewing implementation proposals for architectural drift;
- deciding whether a request is configuration, platform evolution, or implementation detail;
- designing boundary tests and scenario verification tests;
- writing implementation prompts that need the reasons behind CDL constraints;
- evaluating whether an escape hatch has actually been triggered.

Do **not** use this file to:

- override the canonical decision ledger;
- infer a current decision that the CDL does not state;
- promote deferred items into active scope;
- treat exploration examples as current implementation requirements;
- treat implementation-phase claims as runtime proof;
- force agents to read old explorations to know current truth.

Resolution rule:

```text
If this file conflicts with canonical-decision-ledger.md, the CDL wins.
If this file conflicts with the Baseline Acceptance Register, the CDL wins on architecture and the register governs current baseline status.
If this file describes a rationale but no CDL row supports it, treat it as advisory context only.
```

---

## 1. Source basis

This companion extracts durable reasoning from:

- `00-exploration-framework.md`
- `13-adr4-session1-scoping.md`
- `15-adr4-session3-part1-structural-coherence.md`
- `16-adr4-session3-part2-irreversibility-filter.md`
- `17-adr4-session3-part3-adversarial-stress-tests.md`
- `18-adr4-session3-part4-remaining-q-resolution.md`
- `config-boundary-13-18-exploration-README.md`
- `canonical-decision-ledger.md`
- phase implementation files only as provenance and verification leads, not authority

The source explorations remain archived/provenance. This file preserves only the parts needed for future routing, tests, and review discipline.

---

## 2. Relationship to implementation status

This companion does not classify implementation acceptance. It identifies routing concerns, boundary tests, and verification intent.

Current implementation acceptance belongs in:

```text
docs/agent-working-surface/baseline-acceptance-register.md
```

Legacy audit files may be used only as historical inputs for building that register. They are not active agent-facing status surfaces.

---

## 3. Professional position

The CDL is structurally sound as the canonical authority surface. It should not be expanded into a long rationale archive.

What was compressed away is different:

```text
why the boundary exists
what failure mode it protects against
how to route future changes
what tests should catch regression
which escape hatches are legitimate
which tempting implementation shortcuts are forbidden
```

That missing layer matters most after Phase 4 because the platform now has many interacting mechanisms: eventing, identity, flags, assignments, sync, config packages, role-action policy, workflow patterns, projections, and resolution.

The correct repair is this companion artifact, not making exploration files authoritative again.

---

## 4. Decision-routing workflow

Every architecture-sensitive work item should be routed through this sequence before implementation.

```text
1. Identify scenario pressure.
2. Identify CDL rows touched.
3. Classify mechanism vs configuration instance.
4. Apply irreversibility filter.
5. Classify evaluation location: device, server, or both.
6. Identify state participation impact.
7. Identify sync/authority impact.
8. Identify config-package impact.
9. Identify current baseline status.
10. Define architecture tests and scenario checks.
11. Decide route: implement, verify baseline candidate, spec clarify, CDL successor decision, or defer.
```

### 3.1 Routing template

Use this template for future work items.

```yaml
work_item:
scenario_pressure:
cdl_rows_touched:
capability_area:
mechanism_or_instance:
change_class:
  stored_state_impact:
  contract_surface_impact:
  wrong_choice_recovery:
evaluation_location:
  device:
  server:
  both:
authority_visibility_sync_impact:
projection_impact:
flag_lifecycle_impact:
configuration_impact:
deferred_boundary_risk:
architecture_tests_required:
runtime_scenario_required:
baseline_status_before:
baseline_status_after_expected:
route:
  - implement_inside_existing_boundary
  - verify_baseline_candidate
  - write_spec_clarification
  - create_successor_cdl_decision
  - defer_as_platform_evolution
```

### 3.2 Route definitions

| Route | Use when | Example |
|---|---|---|
| `implement_inside_existing_boundary` | CDL already authorizes the mechanism and the change is implementation-local. | Improve projection query performance without changing event semantics. |
| `verify_baseline_candidate` | A capability is listed as a baseline candidate and needs code/runtime acceptance evidence. | Verify resolver routing for each new flag category. |
| `write_spec_clarification` | CDL decision is clear, but implementation contract needs detail. | Clarify config-package validation messages. |
| `create_successor_cdl_decision` | Change touches canonical contracts or current platform-evolution boundary. | New envelope field, new event type, new scope type. |
| `defer_as_platform_evolution` | Need is real but not active scope. | Field-level sensitivity/encryption/redaction. |

---

## 5. Irreversibility filter

The irreversibility filter is the main mechanism for preventing accidental architecture change.

### 4.1 The three tests

| Test | Question | If yes |
|---|---|---|
| Stored-state impact | If this changes after offline devices have created data, must stored events or local stores be transformed? | Architecture-grade constraint. |
| Contract-surface impact | Do more than two independent components need to agree on this shape/meaning? | Treat as contract-level. |
| Wrong-choice recovery | Would recovery require data migration, protocol change, or coordinated mobile/server upgrade? | Do not treat as implementation detail. |

### 4.2 Classification

| Classification | Meaning | Route |
|---|---|---|
| Structural constraint | Permanent or near-permanent contract. | CDL or successor decision. |
| Strategy-protecting constraint | Not necessarily stored forever, but protects a core invariant. | CDL/spec clarification plus tests. |
| Initial strategy | Evolvable implementation or package strategy. | Implementation/spec, baseline status tracked. |
| Leaf implementation | Local code/tooling choice. | Implementation issue only. |

### 4.3 Common classifications in this platform

| Change | Likely classification | Default route |
|---|---|---|
| Add envelope field | Structural constraint | Successor CDL decision. |
| Add envelope `type` | Structural constraint | Successor CDL decision. |
| Change `shape_ref` grammar | Structural constraint | Successor CDL decision + migration plan. |
| Make `activity_ref` mandatory | Structural constraint change | Successor CDL decision. |
| Add shape field type | Platform mechanism change | Spec + deploy-time validation + mobile/server tests. |
| Add expression function | Strategy-protecting boundary risk | Boundary review; likely platform evolution. |
| Add scope type | Security-sensitive platform evolution | Successor decision. |
| Add deployer-authored scope logic | Rejected boundary crossing | Do not implement. |
| Add pattern mechanism | Platform evolution | Successor platform mechanism spec. |
| Add pattern binding | Configuration instance | Implement/spec inside config package boundary. |
| Add role-action mapping | Configuration instance using platform mechanism | Deploy-time validation + server authoritative tests. |
| Optimize projection storage | Initial strategy | Code/baseline review; event stream remains canonical. |
| Add field-level sensitivity | Platform evolution | Defer unless explicit successor design. |
| Add auto-resolution category | Platform-owned resolvability change | Successor platform decision. |

---

## 6. Configuration anti-pattern guardrails

The configuration boundary exists to support “set up, not built” without creating a second programming platform inside deployment config.

### 5.1 Anti-pattern catalog

| Anti-pattern | Smell | Review response |
|---|---|---|
| Config-as-code | Deployer config needs loops, functions, scripts, recursion, or custom execution order. | Route to platform mechanism or reject. |
| Vocabulary creep | Deployer asks for new event types, action types, scope types, or state names as ordinary config. | Keep structural vocabulary platform-owned. |
| Implicit coupling | One config artifact silently depends on another without declared reference. | Require explicit dependency and deploy-time validation. |
| Version coupling | Old events become invalid when config changes. | Preserve versioned interpretation or migrate explicitly. |
| Ghost dependencies | Broken config is discovered on device or at runtime. | Fail deploy-time validation. |
| Complexity blind spots | Large shapes/rules/triggers become unreviewable but still deploy. | Enforce budgets and warnings. |
| Inner-platform effect | Activity or pattern config becomes a deployer-authored processing pipeline. | Stop and route to platform evolution. |

### 5.2 Review questions

```text
Does this config feature need a debugger?
Does it need loops, recursion, functions, or custom operators?
Can two deployments define incompatible meanings for the same structural concept?
Can a broken reference reach a device?
Can historical events become uninterpretable?
Can a configuration bug leak unauthorized data?
Can an activity redefine platform processing semantics?
```

Any “yes” answer means the work must not proceed as ordinary configuration.

---

## 7. Configuration artifact lifecycle model

The exploration found two lifecycle families. This distinction should stay visible.

### 6.1 Event-coupled lifecycle

| Artifact | Event reference | Versioning | Change effect |
|---|---|---|---|
| Shape | `shape_ref` | Explicit in every event: `{shape_name}/v{version}` | Old events remain under old shape forever. |
| Platform payload shape | `shape_ref` | Platform-owned version | Old events remain interpretable. |

Rules:

```text
Event-coupled artifacts are never interpreted by latest config.
All referenced versions must remain available.
Breaking changes require explicit versioning/migration treatment.
```

### 6.2 Config-package lifecycle

| Artifact | Event reference | Versioning | Change effect |
|---|---|---|---|
| Activity | ID-only via optional `activity_ref` | Package-level, not individual event version | Future behavior changes; historical events retain activity identity. |
| Expression rule | None | Package-level | Future form behavior. |
| Pattern binding | None as event field; resolved through activity/config | Package-level | Future/derived projection behavior. |
| Role-action map | None as event field | Package-level | Future authority evaluation. |
| Severity override | None as event field | Package-level | Flag handling/presentation. |
| Sensitivity classification | None as event field | Package-level | Sync/retention/display/export behavior where implemented. |

Rules:

```text
Config-package artifacts are delivered atomically.
Broken package dependencies fail before publication.
Config changes affect future behavior unless a separate migration/backfill is explicitly defined.
Historical event meaning is not changed by package mutation.
```

---

## 8. Configuration dependency graph and cascade rules

### 7.1 Dependency graph

```text
Shape  <- Logic Rule        (references shape fields)
Shape  <- Activity          (activity includes shape refs)
Shape  <- Trigger/Policy    (conditions/output payloads reference shapes/fields)
Shape  <- Projection Rule   (source shape and field refs)
Pattern <- Activity         (activity binds platform pattern)
Trigger <- Trigger          (deadline check watches output; bounded path only)
Activity <- Campaign        (campaign is specialized activity composition)
```

In the current canonical model, treat “trigger/policy” carefully: general trigger execution and auto-resolution execution may still be deferred depending on current scope. Do not promote deferred execution by copying exploration terms.

### 7.2 Uniform cascade rule

All config dependency failures are caught at deploy-time validation.

| Upstream change | Possible break | Required behavior |
|---|---|---|
| Shape field removed/deprecated | Expression references invalid field. | Reject package. |
| Shape field removed/deprecated | Policy/trigger condition references invalid field. | Reject package. |
| Shape retired | Activity references unavailable shape. | Reject package. |
| Pattern mechanism unavailable | Activity references missing pattern. | Reject package or require platform migration. |
| Pattern binding wrong | Shape role missing or incompatible. | Reject package. |
| Role-action map invalid | Unknown action or assignment role mismatch. | Reject package. |
| Trigger graph cycle | Unbounded side effects. | Reject package. |
| Trigger graph path > allowed bound | Policy chain too deep. | Reject package. |

### 7.3 Architecture tests

Create/keep tests that prove:

```text
- deploy-time validator rejects expressions referencing removed fields;
- deploy-time validator rejects activity references to retired shapes;
- deploy-time validator rejects unknown action names;
- deploy-time validator rejects pattern binding with missing shape roles;
- config package is all-or-nothing;
- devices never receive a partially valid package;
- mobile ignores unknown additive package keys without changing known behavior;
- package versioning does not reinterpret historical events.
```

---

## 9. Device/server evaluation contract

### 8.1 Default split

| Capability | Device | Server | Rule |
|---|---:|---:|---|
| Event creation | Yes | Yes | Device creates offline work; server creates system events where authorized. |
| Envelope/payload structural validation | Yes | Yes | Device for UX, server for authority. |
| L2 form logic | Yes | No | Immediate form behavior must work offline. |
| Projection | Scoped/local | Complete/server | Device projection is scoped by sync. |
| Scope computation for sync | No | Yes | Server decides what data reaches devices. |
| Conflict detection | No | Yes | Requires authoritative view and appends flag events. |
| Merge/split | No | Yes | Online-only identity operations. |
| Assignment administration | Advisory/no | Yes | Server-side authority path. |
| Role-action enforcement | Advisory | Authoritative | Device can warn/hide; server accepts-and-flags. |
| Domain uniqueness | Advisory | Authoritative | Device can warn from local data; server emits flag. |
| Workflow pattern state | Local scoped view | Complete server view | Both derive; server is authoritative for complete dataset. |
| L3 policy side effects | No by default | Yes | Persistent side effects belong on server unless a successor decision says otherwise. |

### 8.2 Rule

```text
If a mechanism creates persistent events, changes policy participation, resolves flags, or affects sync visibility, default it to server-side authority.
Device behavior may be advisory or local projection unless the CDL explicitly makes it authoritative.
```

### 8.3 Why this matters

Device-side policy side effects create three risks:

1. incomplete knowledge while offline;
2. duplicate side effects when server replays/evaluates;
3. divergence across mobile versions and config versions.

Use device checks for usability. Use server checks for correctness.

---

## 10. Rationale cards

Each card below is a reusable review unit. Cards do not introduce decisions; they attach context, routing, and test intent to CDL-governed decisions.

---

### ARC-001 — Canonical authority closure

```yaml
status: active_context
source:
  - canonical-decision-ledger.md
cdl_rows:
  - CDL-000
purpose: Keep one decision surface authoritative while preserving context elsewhere.
routing_rule: Old explorations can explain why, but they cannot decide what.
tests:
  - implementation prompts name CDL as authority
  - no prompt asks agents to compare ADR chronology for current truth
  - companion files contain explicit non-authority header
```

Review question:

```text
Is the proposed work using exploration as proof of current authority?
```

If yes, stop and route back to the CDL.

---

### ARC-002 — Operational truth and derived state

```yaml
status: active_context
source:
  - canonical-decision-ledger.md
cdl_rows:
  - CDL-001
  - CDL-002
  - CDL-003
  - CDL-004
purpose: Preserve event-led architecture under implementation pressure.
routing_rule: Durable operational facts append as events; fast reads remain projections.
architecture_tests:
  - no update/delete path for existing operational events
  - projection rebuild produces same logical state
  - unresolved flagged events visible in timeline but excluded from current/pattern state
```

Review question:

```text
Is this change creating a second source of truth?
```

If yes, reject or reframe as event append plus projection.

---

### ARC-003 — Mechanism/instance split

```yaml
status: active_context
source:
  - canonical-decision-ledger.md
cdl_rows:
  - CDL-005
  - CDL-055
  - CDL-056
purpose: Keep platform-owned semantics separate from deployer-authored instances.
routing_rule: Deployer configuration selects and parameterizes platform mechanisms; it does not redefine them.
examples:
  mechanism:
    - envelope contract
    - type vocabulary
    - shape_ref grammar
    - scope containment semantics
    - expression grammar
    - platform pattern mechanism
  instance:
    - shape version authored by deployer
    - activity instance
    - assignment scope instance
    - pattern binding
    - severity override
```

Review question:

```text
Is this proposal letting deployment config redefine platform semantics?
```

If yes, route to platform evolution or reject.

---

### ARC-004 — Envelope-touching change filter

```yaml
status: active_context
source:
  - 16-adr4-session3-part2-irreversibility-filter.md
  - canonical-decision-ledger.md
cdl_rows:
  - CDL-006
  - CDL-007
  - CDL-008
  - CDL-009
  - CDL-010
  - CDL-011
  - CDL-012
purpose: Prevent accidental permanent data-contract changes.
routing_rule: Any envelope change is architecture-grade by default.
architecture_tests:
  - schema parity test across contract copies
  - illegal type values rejected
  - no hidden derived fields accepted
  - causal metadata not ordered by wall clock
```

Review question:

```text
Would this change require old events or offline-device events to be transformed?
```

If yes, successor CDL decision plus migration/evidence plan.

---

### ARC-005 — Activity attribution and provenance honesty

```yaml
status: active_context
source:
  - 17-adr4-session3-part3-adversarial-stress-tests.md
cdl_rows:
  - CDL-009
  - CDL-056
purpose: Preserve activity attribution without fabricating provenance.
routing_rule: Stamp activity_ref when context exists; allow null when provenance is honestly absent.
architecture_tests:
  - human capture from activity UI stamps activity_ref
  - import without activity context may keep null
  - trigger/system output inherits source activity_ref where applicable
  - same-shape multi-activity config warns or validates attribution path
forbidden:
  - infer activity from timestamp
  - infer activity from actor alone
  - infer activity from scope alone
  - require fabricated activity for historical imports
```

Review question:

```text
Is the implementation guessing activity after the fact?
```

If yes, reject.

---

### ARC-006 — Shape version permanence

```yaml
status: active_context
source:
  - 17-adr4-session3-part3-adversarial-stress-tests.md
cdl_rows:
  - CDL-008
  - CDL-039
  - CDL-040
  - CDL-041
purpose: Preserve historical interpretability across offline windows and schema evolution.
routing_rule: Old events are interpreted under their original shape version forever.
architecture_tests:
  - old shape versions remain loadable
  - deprecated shape versions still validate historical events
  - projection handles mixed shape versions
  - breaking changes do not reinterpret old payloads
```

Review question:

```text
Does this proposal depend on latest-schema interpretation of old events?
```

If yes, reject or design explicit migration.

---

### ARC-007 — Closed event type vocabulary

```yaml
status: active_context
source:
  - 17-adr4-session3-part3-adversarial-stress-tests.md
  - canonical-decision-ledger.md
cdl_rows:
  - CDL-007
  - CDL-013
  - CDL-014
purpose: Prevent vocabulary creep in pipeline routing.
routing_rule: Domain facts use shape_ref; type changes only when platform pipeline behavior is genuinely new.
architecture_tests:
  - conflict_detected is never an envelope type
  - conflict_resolved is never an envelope type
  - subjects_merged is never an envelope type
  - subject_split is never an envelope type
  - consumers use shape_ref for domain discrimination
```

Before proposing a new event type, prove all five:

```text
1. shape_ref cannot express the domain fact.
2. payload cannot express the domain detail.
3. activity_ref cannot express the context.
4. projection cannot derive the state.
5. processing pipeline behavior is genuinely new.
```

---

### ARC-008 — Config boundary and expression ceiling

```yaml
status: active_context
source:
  - 13-adr4-session1-scoping.md
  - 15-adr4-session3-part1-structural-coherence.md
cdl_rows:
  - CDL-038
  - CDL-043
  - CDL-044
  - CDL-052
purpose: Prevent expressions from becoming a programming language.
routing_rule: Expressions compare known fields/context. Computation belongs in projection or platform mechanisms.
route_to_projection_or_platform_when_needed:
  - date math
  - string manipulation
  - aggregation
  - cross-event scans
  - joins
  - recursion
  - user-defined functions
architecture_tests:
  - expressions use allowed operators only
  - expressions stay within predicate budget
  - context.* exposes fixed pre-resolved properties only
  - server/mobile evaluators produce same result on shared fixtures
```

Review question:

```text
Would a deployer need a debugger to understand this expression?
```

If yes, it is crossing the boundary.

---

### ARC-009 — Deploy-time validation over runtime discovery

```yaml
status: active_context
source:
  - 15-adr4-session3-part1-structural-coherence.md
cdl_rows:
  - CDL-044
purpose: Prevent broken config from reaching devices.
routing_rule: Config dependency failures are rejected before package publication.
architecture_tests:
  - broken field refs fail publish
  - broken shape refs fail publish
  - invalid pattern bindings fail publish
  - unknown action names fail publish
  - over-budget expressions/triggers fail publish
  - package publication is atomic
```

Review question:

```text
Can this break only after the package reaches the device?
```

If yes, validation is missing.

---

### ARC-010 — Server authority for side effects

```yaml
status: active_context
source:
  - 15-adr4-session3-part1-structural-coherence.md
  - canonical-decision-ledger.md
cdl_rows:
  - CDL-042
  - CDL-053
  - CDL-054
purpose: Keep persistent side effects authoritative and replayable.
routing_rule: Device may advise; server emits authoritative flags, resolutions, and policy output events.
architecture_tests:
  - device does not create canonical conflict flags unless explicitly authorized by successor decision
  - device role-action failure is advisory only
  - server accepts-and-flags unauthorized/stale work
  - auto-resolution emits conflict_resolved/v1 rather than mutating flag state
```

Review question:

```text
Does the device create durable policy consequences from incomplete local knowledge?
```

If yes, reject or route to successor platform design.

---

### ARC-011 — Scope mechanism as security boundary

```yaml
status: active_context
source:
  - 18-adr4-session3-part4-remaining-q-resolution.md
  - canonical-decision-ledger.md
cdl_rows:
  - CDL-030
  - CDL-031
  - CDL-032
  - CDL-034
  - CDL-055
purpose: Prevent custom configuration from leaking data.
routing_rule: Scope types and containment semantics are platform-owned.
architecture_tests:
  - sync payload is filtered server-side
  - custom scope scripts are rejected
  - assignment creation containment is server validated
  - new scope types require platform evolution
```

Review question:

```text
Can a deployer bug redefine who receives data?
```

If yes, the design is unsafe.

---

### ARC-012 — Domain uniqueness detects; resolution decides elsewhere

```yaml
status: active_context
source:
  - 18-adr4-session3-part4-remaining-q-resolution.md
cdl_rows:
  - CDL-045
  - CDL-053
  - CDL-054
purpose: Keep detection separate from resolution policy.
routing_rule: Shape uniqueness may detect and flag; it may not choose winners, merge payloads, or resolve.
architecture_tests:
  - server accepts uniqueness violations and emits domain_uniqueness_violation
  - device uniqueness check is advisory
  - uniqueness rule cannot specify auto-resolution precedence
  - manual-only categories cannot be made auto-resolvable by config
```

Review question:

```text
Is this shape rule deciding conflict outcome instead of detecting anomaly?
```

If yes, split detection from resolution.

---

### ARC-013 — Flag lifecycle and detect-before-act

```yaml
status: active_context
source:
  - canonical-decision-ledger.md
cdl_rows:
  - CDL-003
  - CDL-004
  - CDL-028
  - CDL-035
  - CDL-051
  - CDL-053
purpose: Preserve uncertain evidence without producing unsafe downstream effects.
routing_rule: Unresolved flags are visible but exclude source events from authoritative state and policy participation.
architecture_tests:
  - flagged event remains in timeline
  - flagged event excluded from current-state projection
  - flagged event excluded from pattern-state derivation
  - accepted resolution re-includes source event
  - rejected resolution keeps source excluded
  - downstream contamination is computed, not redundantly flagged
```

Review question:

```text
Does this work item let unresolved uncertainty advance state or fire policy?
```

If yes, reject.

---

### ARC-014 — Workflow state is derived from platform patterns

```yaml
status: active_context
source:
  - canonical-decision-ledger.md
  - 15-adr4-session3-part1-structural-coherence.md
cdl_rows:
  - CDL-047
  - CDL-048
  - CDL-049
  - CDL-050
purpose: Prevent workflow from becoming mutable status or deployer-authored state machines.
routing_rule: Pattern mechanisms are platform-owned; deployers bind shapes/roles/parameters.
architecture_tests:
  - no workflow_state/current_state field in events
  - no status_changed event type
  - transition violations are accepted and flagged
  - deployer cannot define transition table as config
  - pattern state rebuilds from events + pattern binding
```

Review question:

```text
Is workflow state being stored as truth instead of projected?
```

If yes, reject.

---

### ARC-015 — Sensitivity and retention routing

```yaml
status: active_context
source:
  - 18-adr4-session3-part4-remaining-q-resolution.md
  - canonical-decision-ledger.md
cdl_rows:
  - CDL-037
  - CDL-046
purpose: Prevent partial sensitivity features from becoming false security.
routing_rule: Shape/activity sensitivity can guide package/sync/retention behavior; field-level sensitivity and cryptographic controls require platform evolution.
architecture_tests:
  - scope contraction does not delete canonical server history
  - sensitive retained local data is not merely hidden in UI when purge is required
  - field-level sensitivity is not introduced through ad hoc config
  - export/redaction/encryption behavior is explicitly scoped before implementation
```

Review question:

```text
Is this claiming compliance/security through UI hiding only?
```

If yes, reject or route to platform security design.

---

## 11. Architecture test seed backlog

These are not all implementation tests. They are **boundary tests** that protect the architecture.

### 10.1 Event/envelope tests

```text
- Reject any envelope type outside the six-value set.
- Reject hidden derived envelope fields where schema allows strict validation.
- Accept platform-bundled integrity/identity facts only through shape_ref mapping.
- Verify system actor discrimination uses actor_ref.id prefix, not new actor type.
- Verify device_time does not drive conflict or projection ordering.
```

### 10.2 Projection tests

```text
- Rebuild projection from events and compare to materialized projection.
- Flagged source event visible in timeline but excluded from current state.
- Accepted resolution re-includes source event.
- Rejected resolution keeps source excluded.
- Workflow pattern projection excludes unresolved flagged events.
- Downstream source-chain contamination is computed without redundant flag cascade.
```

### 10.3 Identity tests

```text
- Merge appends subjects_merged/v1 and does not rewrite old subject_ref values.
- Split appends subject_split/v1 and does not reassign historical events automatically.
- Alias projection rebuilds from lineage events.
- Stale-reference detection uses raw subject_ref before alias normalization.
- Lineage cycles cannot be constructed.
```

### 10.4 Assignment/authority/sync tests

```text
- Actor with no active assignment receives no ordinary scoped data.
- Assignment creation fails when requested scope exceeds creator authority.
- Scope containment uses platform mechanism, not deployer script.
- Sync does not deliver unauthorized data and rely on UI hiding.
- Own assignment events required for local authority projection are delivered under the established sync rule.
- Scope contraction purges or retains according to policy without mutating canonical server events.
```

### 10.5 Configuration package tests

```text
- Broken field reference blocks package publish.
- Retired shape referenced by activity blocks package publish.
- Unknown role-action action blocks package publish.
- Same-shape multi-activity deployment produces warning or configured disambiguation guard.
- Mobile applies config atomically; no partial package state.
- Unknown additive package keys are ignored safely.
- At-most-two-version behavior does not invalidate in-progress old-shape work.
```

### 10.6 Expression tests

```text
- Expressions cannot call functions.
- Expressions cannot exceed predicate/depth limits.
- context.* accepts only fixed pre-resolved properties.
- Form-context refs and trigger-context refs remain namespace-restricted.
- Server/mobile evaluator fixtures match exactly.
```

### 10.7 Workflow/policy tests

```text
- Pattern state is derived, not stored in events.
- Transition violation accepts source event and emits conflict_detected/v1.
- Role-action disallowed offline work is accepted and flagged role_stale.
- Device role-action checks are advisory only.
- General trigger/auto-resolution execution is not assumed unless explicitly in scope.
- assignment_changed cannot be authorized through activity role-action mappings.
```

---

## 12. Escape-hatch and platform-evolution routing table

The active escape-hatch register is `docs/agent-working-surface/escape-hatch-register.md`. Use it to route measured evolution pressure. It is not architecture authority, and it does not authorize implementation by itself.

| Pressure | Do not do | Correct route |
|---|---|---|
| Claimed escape-hatch trigger | Treat pressure as a TODO or permission to implement | Check the register, require measured evidence, then route to successor decision or bounded plan. |
| Need more event categories | Add deployer event types | Prove new pipeline behavior; successor CDL decision. |
| Need activity attribution everywhere | Make `activity_ref` mandatory | Keep optional; enforce auto-stamping in UI; preserve null for honest unknowns. |
| Need complex form calculations | Add expression functions | Precompute projection/context properties or platform mechanism. |
| Need cross-event validation | Add device-only rule | Device advisory + server authoritative accept-and-flag. |
| Need custom access behavior | Add scope script | New platform-owned scope mechanism decision. |
| Need case workflow variant | Let deployer write state machine | Add/bind platform pattern or create platform pattern mechanism. |
| Need sensitive field redaction | Hide field in UI | Platform security/export/retention design. |
| Need faster projection | Make projection authoritative | Optimize/rebuildable projection; use the register only if the measured trigger is claimed. |
| Need conflict auto-resolution | Let deployer mark any category auto | Platform resolvability change + policy validation. |
| Need audit reconstruction beyond live sync | Change normal sync into historical pull | Separate backfill/audit API/surface. |

---

## 13. “Do not promote” reminders

These are common drift points. Treat them as review checklist items.

```text
Do not promote new envelope fields.
Do not promote new envelope type values.
Do not promote new identity reference types.
Do not promote deployer-authored scope logic.
Do not promote deployer-authored pattern mechanisms or transition tables.
Do not promote field-level sensitivity/encryption/redaction without platform design.
Do not promote device-side flag pre-creation.
Do not promote device-side L3 trigger execution.
Do not promote mutable workflow status.
Do not promote latest-shape reinterpretation of historical events.
Do not accept implementation assertions into the baseline without acceptance evidence.
```

---

## 14. How to use this in post-Phase-4 work

### 13.1 For a new feature request

1. Map it to a scenario pressure.
2. Identify whether it is a new mechanism or new configuration instance.
3. Apply the irreversibility filter.
4. Check anti-pattern catalog.
5. Select a route.
6. Write boundary tests before implementation.
7. Update the Baseline Acceptance Register after verification.

### 13.2 For an implementation prompt

Every agent prompt that modifies architecture-sensitive code should include:

```text
Authority: canonical-decision-ledger.md.
Context: architecture-rationale-and-routing-companion.md is non-authoritative rationale only.
Baseline: baseline-acceptance-register.md records current acceptance status.
Rule: do not promote deferred items; do not introduce envelope/type/config-boundary changes without explicit successor decision.
```

### 13.3 For scenario walkthroughs

Scenario walkthroughs should produce:

```text
business pressure
CDL rows exercised
capabilities touched
dependencies touched
mechanism/instance split
state participation risks
sync/authority risks
configuration risks
architecture tests
runtime evidence needs
```

---

## 15. Source-to-card map

| Source | Preserved here as |
|---|---|
| `00-exploration-framework.md` | Decision-routing workflow; irreversibility and assumption discipline. |
| `13-adr4-session1-scoping.md` | Anti-pattern guardrails; configuration boundary pressure; prior-art caution. |
| `15-adr4-session3-part1-structural-coherence.md` | Lifecycle model; dependency graph; device/server split; expression ceiling; deploy-time cascade rule. |
| `16-adr4-session3-part2-irreversibility-filter.md` | Stored-state / contract-surface / recovery-cost filter; structural vs strategy classification. |
| `17-adr4-session3-part3-adversarial-stress-tests.md` | `activity_ref` optionality proof; shape versioning proof; event type vocabulary proof. |
| `18-adr4-session3-part4-remaining-q-resolution.md` | Domain uniqueness vs resolution split; scope extensibility boundary; sensitivity routing. |
| `canonical-decision-ledger.md` | Authority closure; current binding constraints; must-not-happen and deferred boundaries. |
| `baseline-acceptance-register.md` | Current baseline status and acceptance evidence. |

---

## 16. Review checklist before accepting this file into docs

```text
[ ] Every card cites only rationale, not new decisions.
[ ] Every active architectural constraint is backed by a CDL row.
[ ] No deferred item is described as implemented or active.
[ ] No phase-file assertion is treated as baseline acceptance without verification.
[ ] Mechanism/instance distinction is explicit.
[ ] Device advisory vs server authoritative split is explicit.
[ ] All architecture-test seeds are framed as boundary checks, not proof of current implementation.
[ ] The file header states CDL authority clearly.
```

---

## 17. Steward note

This file exists because compact authority surfaces and implementation prompts serve different purposes.

The CDL answers:

```text
What is the current decision?
```

This companion answers:

```text
Why is this boundary here?
How should the next change be routed?
What should tests protect?
Which escape hatches are legitimate?
```

Do not merge these two roles. Keeping them separate is what prevents both authority drift and rationale loss.
