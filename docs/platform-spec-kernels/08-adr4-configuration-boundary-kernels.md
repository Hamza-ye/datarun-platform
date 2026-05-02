# ADR-004 Configuration Boundary Kernel Staging

Status: Iteration 27 staging

This temporary staging file holds ADR-004 configuration-boundary lineage kernels. It is not a final atomic document.

## Staged Kernels

## Kernel: ADR-004 Session 1 Scoping Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/13-adr4-session1-scoping.md` is ADR-004 Session 1 scoping, prior-art, and anti-pattern exploration. It defines ADR-004's decision surface, identifies irreversible questions, gathers comparable-platform lessons, and proposes a configuration-gradient hypothesis for later scenario testing. It does not make final ADR-004 decisions.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / supersession notice
- `docs/exploration/archive/13-adr4-session1-scoping.md` / opening purpose
- `docs/exploration/archive/13-adr4-session1-scoping.md` / `## 7. Session 2 Charter`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels extracted from ADR-004 Session 1.

Non-goals:

Does not decide final event type ownership, schema versioning, activity references, expression language, trigger scope, or configuration delivery.

Forbidden interpretations:

- Do not treat prior-art lessons as final platform design.
- Do not treat the configuration-gradient hypothesis as ADR-settled.

Open edges:

Sessions 2 through 4 and ADR-004 must test, revise, classify, and close these findings.

Platform specification note:

Use this source as ADR-004 decision-surface and guardrail lineage.

## Kernel: ADR-004 Decision Surface

Status: Open
Kind: open-question

Specification statement:

ADR-004 must decide where the platform ends and deployment configuration begins across twelve questions: event type vocabulary ownership, data shape definition, schema versioning, configuration versioning and on-device coexistence, configuration-versus-code boundary, event-triggered action scope, domain conflict rule configuration, role-action permission tables, per-flag severity, scope type extensibility, activity/correlation model, and sensitive-subject classification.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / `## 1. What ADR-4 Must Decide`
- `docs/exploration/archive/13-adr4-session1-scoping.md` / `### 1.1 The Full Decision Surface`

Closure basis:

Open ADR-004 decision surface.

Scope:

Applies to ADR-001, ADR-002, and ADR-003 deferrals into ADR-004.

Non-goals:

Does not decide any of the twelve questions.

Forbidden interpretations:

- Do not reduce ADR-004 to only data-shape configuration.
- Do not let role permissions, flag severity, scope types, and sensitive-subject policy disappear as implementation details.

Open edges:

Each question must be classified by later ADR-004 sources as platform-fixed, deployer-configurable, strategy, implementation, or deferred.

Platform specification note:

Use as the ADR-004 scope checklist.

## Kernel: ADR-004 Irreversibility Focus Set

Status: Candidate
Kind: invariant

Specification statement:

ADR-004 Session 1 identifies Q1 event type vocabulary, Q3 schema versioning scheme, and Q11 activity/correlation reference as the configuration questions with potential stored-event irreversibility. Q1 affects the stored event `type`; Q3 affects the stored schema/version reference; Q11 may add an activity/correlation envelope field.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / `### 1.2 Which Questions Touch the Envelope?`
- `docs/exploration/archive/13-adr4-session1-scoping.md` / `## 5. Decision Map: What's Permanent vs. What's Evolvable`

Closure basis:

Candidate ADR-004 irreversibility map. Later ADR-004 sources must confirm whether Q11 becomes an envelope field and how Q1/Q3 close.

Scope:

Applies to event type vocabulary, schema version references, activity references, and stress-test depth for ADR-004.

Non-goals:

Does not decide platform-fixed versus deployment-defined vocabulary, exact version reference format, or whether activity is stored in the envelope.

Forbidden interpretations:

- Do not apply equal adversarial depth to all ADR-004 questions when only Q1/Q3/Q11 may touch stored events.
- Do not treat evolvable questions as unimportant; they remain boundary decisions.

Open edges:

Sessions 2 and 3 must test Q1, Q3, and Q11 most rigorously.

Platform specification note:

Use as ADR-004 stress-depth guidance.

## Kernel: Configuration Boundary Anti-Pattern Set

Status: Candidate
Kind: forbidden-interpretation

Specification statement:

ADR-004 Session 1 identifies six configuration failure modes to guard against: inner-platform effect, ad hoc half-language growth, configuration-specialist trap, schema-evolution trap, trigger-escalation trap, and overlapping-authority trap.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / `## 3. Anti-Pattern Catalog`

Closure basis:

Candidate ADR-004 guardrail set from prior-art analysis.

Scope:

Applies to configuration layers, expression languages, triggers, schema evolution, visual tooling, specialist tooling, and automation mechanisms.

Non-goals:

Does not define final limits or tooling.

Forbidden interpretations:

- Do not allow configuration to become an unbounded programming language with weaker tooling.
- Do not create multiple mechanisms that can configure the same behavior without a single artifact pipeline and explicit precedence.
- Do not make initial setup easy while leaving schema evolution undefined.

Open edges:

Later ADR-004 sources must decide which anti-pattern guards become hard constraints versus design guidance.

Platform specification note:

Use as ADR-004 forbidden-pattern lineage.

## Kernel: Expression And Trigger Ceiling Candidate

Status: Candidate
Kind: configuration-boundary

Specification statement:

The configuration layer should define an expressiveness ceiling before designing the expression language. Session 1 proposes pure functions over known data, no loops, no side effects, no user-defined abstractions, non-recursive triggers, finite evaluation, and bounded side effects that create at most one pre-defined event.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / AP-1 guard
- `docs/exploration/archive/13-adr4-session1-scoping.md` / AP-2 guard
- `docs/exploration/archive/13-adr4-session1-scoping.md` / AP-5 guard
- `docs/exploration/archive/13-adr4-session1-scoping.md` / Layer 2 and Layer 3 hypotheses

Closure basis:

Candidate ADR-004 configuration-boundary rule.

Scope:

Applies to expression language, validation rules, computed values, skip logic, triggers, deadline policies, and event-triggered actions.

Non-goals:

Does not choose the expression language or final trigger artifact format.

Forbidden interpretations:

- Do not add loops, recursion, user-defined functions, arbitrary side effects, or unbounded trigger chains to configuration without classifying it as code/platform evolution.
- Do not use function-count growth as a substitute for a code escape hatch.

Open edges:

Session 2 must test whether S12 and selected scenarios fit inside this ceiling.

Platform specification note:

Use as candidate boundary between configuration and programming.

## Kernel: Schema Evolution First-Class Requirement

Status: Candidate
Kind: invariant

Specification statement:

Schema evolution must be modeled as first-class configuration behavior, with explicit additive, deprecation, and breaking change types; migration rules; and coexistence guarantees for devices and events captured under different configuration versions.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / AP-4 guard
- `docs/exploration/archive/13-adr4-session1-scoping.md` / ADR-1 inherited constraints
- `docs/exploration/archive/13-adr4-session1-scoping.md` / Q3 and Q4

Closure basis:

Candidate ADR-004 requirement from prior-art failure modes and offline constraints.

Scope:

Applies to shape changes, event payload compatibility, schema version references, in-flight events, and devices that have not yet synced a new configuration.

Non-goals:

Does not decide exact version reference format or migration mechanism.

Forbidden interpretations:

- Do not allow removal, rename, or type changes to create undefined behavior for historical events or offline devices.
- Do not treat shape versioning as only documentation.

Open edges:

Q3 and Q4 require later closure.

Platform specification note:

Use as schema-versioning and coexistence lineage.

## Kernel: Unified Configuration Artifact Pipeline Candidate

Status: Candidate
Kind: invariant

Specification statement:

All configuration layers should produce artifacts through the same registry, format, validation, versioning, and deployment pipeline. Visual tools and specialist authoring tools may differ, but they must emit the same underlying artifacts rather than parallel structures.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / AP-6 guard
- `docs/exploration/archive/13-adr4-session1-scoping.md` / `### 6.3 AP-6 Compliance: Unified Artifact Pipeline`

Closure basis:

Candidate ADR-004 invariant.

Scope:

Applies to visual builders, declarative schemas, expressions, policy files, registries, namespaces, versioning, and deployment.

Non-goals:

Does not require a single UI or equal editing privileges across all layers.

Forbidden interpretations:

- Do not create separate "visual" and "code/config" authorities over the same configuration domain.
- Do not allow overlapping automation systems for one concern.

Open edges:

Later ADR-004 sources must define artifact format, registry, namespace, and versioning.

Platform specification note:

Use as anti-overlap configuration architecture lineage.

## Kernel: Configuration Gradient Hypothesis

Status: Candidate
Kind: configuration-boundary

Specification statement:

ADR-004 Session 1 proposes a four-layer configuration gradient: Layer 0 assembly by operations managers, Layer 1 shape definition by configuration specialists, Layer 2 pure local logic by configuration specialists, and Layer 3 bounded policy by platform specialists or code. It also adds a second dimension: whether a layer parameterizes existing platform vocabulary or extends the vocabulary.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / `## 6. The Configuration Gradient`
- `docs/exploration/archive/13-adr4-session1-scoping.md` / `### 6.0 Two Dimensions, Not One`
- `docs/exploration/archive/13-adr4-session1-scoping.md` / `### 6.1 The Four Layers`

Closure basis:

Candidate ADR-004 architecture hypothesis for Session 2 testing.

Scope:

Applies to deployer roles, authoring interfaces, configuration artifacts, vocabulary extension, expression power, policy power, and code escape hatches.

Non-goals:

Does not decide final layer count, limits, or authoring formats.

Forbidden interpretations:

- Do not collapse all configuration into one undifferentiated mechanism.
- Do not let operations-manager assembly and specialist vocabulary extension use conflicting artifact authorities.

Open edges:

Session 2 must validate the gradient against concrete scenario walkthroughs.

Platform specification note:

Use as candidate organization for configuration capabilities, not final platform-spec structure.

## Kernel: Configuration Complexity Budget Candidate

Status: Candidate
Kind: configuration-boundary

Specification statement:

Session 1 proposes explicit complexity budgets for configuration: examples include max components per activity, max fields per shape, max validation rules per field, max expression nesting and length, max built-in functions, max triggers per event type, non-recursive trigger depth, and bounded escalation depth.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / Layer 0 through Layer 3 complexity budgets
- `docs/exploration/archive/13-adr4-session1-scoping.md` / Salesforce prior-art lesson on governor limits

Closure basis:

Candidate boundary-enforcement mechanism.

Scope:

Applies to activity assembly, shapes, expressions, triggers, deadlines, and deployment-wide configuration scale.

Non-goals:

Does not settle exact numeric limits.

Forbidden interpretations:

- Do not rely only on advisory prose when hard complexity ceilings are needed to keep the configuration boundary honest.
- Do not let visual configuration hide complexity without measurable limits.

Open edges:

Later ADR-004 sources must validate whether these limits are appropriate and which become hard platform limits.

Platform specification note:

Use as candidate governor-limit lineage for configuration.

## Kernel: Domain-Agnostic Configuration Vocabulary Guard

Status: Candidate
Kind: forbidden-interpretation

Specification statement:

The internal configuration vocabulary must remain domain-agnostic. Domain-specific standards or terms may be used for export and interoperability mappings, but they should not become the internal storage or configuration model.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / OpenSRP lesson
- `docs/exploration/archive/13-adr4-session1-scoping.md` / Vision and constraints stack

Closure basis:

Candidate ADR-004 guardrail carried from domain-agnostic vision.

Scope:

Applies to event vocabulary, shapes, scopes, activities, integration mappings, and configuration authoring language.

Non-goals:

Does not decide export formats or interoperability adapters.

Forbidden interpretations:

- Do not bake health-specific standards or terms into the platform's internal configuration model.
- Do not require deployers to understand external domain standards to configure basic activities.

Open edges:

ADR-004 must decide how domain-defined vocabulary relates to platform-fixed vocabulary.

Platform specification note:

Use as configuration vocabulary guardrail.

## Kernel: ADR-004 Session 2 Charter

Status: Open
Kind: open-question

Specification statement:

Session 2 must test the configuration framework through S00, S06 plus schema evolution, S09, S12, and S20. For each scenario, it should produce artifact sketches, layer classification, boundary-wall points, and natural versus forced configuration findings.

Source basis:

- `docs/exploration/archive/13-adr4-session1-scoping.md` / `## 7. Session 2 Charter`
- `docs/exploration/archive/13-adr4-session1-scoping.md` / `## 8. Open Questions for Session 2`

Closure basis:

Open Session 2 work plan.

Scope:

Applies to ADR-004 next-source extraction and scenario walkthrough validation.

Non-goals:

Does not decide the open questions.

Forbidden interpretations:

- Do not proceed to ADR-004 final closure without scenario-grounded artifact sketches.

Open edges:

Event type vocabulary, activity as first-class concept, shape authoring format, expression language identity, and configuration delivery model remain open.

Platform specification note:

Use as the immediate handoff to ADR-004 Session 2.

## Kernel: ADR-004 Session 2 Scenario Walkthrough Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` is ADR-004 Session 2 scenario walkthrough evidence. It tests the Session 1 configuration-gradient hypothesis against selected scenarios, forms positions on Q1/Q3/Q11 and several evolvable questions, revises the gradient, and defines Session 3 stress-test targets. It does not make final ADR-004 decisions.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / supersession notice
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / opening purpose
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `## 10. Key Findings Summary`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels extracted from ADR-004 Session 2.

Non-goals:

Does not decide final ADR-004 envelope fields, configuration layer boundaries, artifact formats, or platform pattern inventory.

Forbidden interpretations:

- Do not treat Session 2 `Decided` labels as ADR-settled; the source itself says they are pending Session 3 stress test.
- Do not treat hypothetical artifact syntax as final format.

Open edges:

Session 3 must stress-test the positions formed here, and ADR-004 must verify final closure.

Platform specification note:

Use as scenario-grounded evidence for ADR-004.

## Kernel: Platform Structural Type Vocabulary Candidate

Status: Conditional
Kind: contract

Specification statement:

ADR-004 Session 2 forms a hybrid Q1 position: the stored event `type` is a platform-fixed closed structural vocabulary, while deployment-specific meaning is expressed through deployment-defined `shape_ref` values. The platform routes and processes events by structural type; deployers extend vocabulary through shapes, not event types.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S00 envelope observations
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `### 6.2 Positions on Envelope Questions` / Q1
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `## 10. Key Findings Summary`

Closure basis:

Conditional Session 2 position pending Session 3 stress test and ADR-004 verification.

Scope:

Applies to stored event `type`, platform routing semantics, deployer vocabulary extension, and shape-based domain meaning.

Non-goals:

Does not settle the complete structural type list.

Forbidden interpretations:

- Do not allow deployments to invent arbitrary structural event types under this candidate.
- Do not encode domain-specific event meaning directly in the platform `type` field.

Open edges:

Session 3 must test structural type vocabulary completeness.

Platform specification note:

Use as the current Q1 candidate.

## Kernel: Shape Reference Envelope Candidate

Status: Conditional
Kind: contract

Specification statement:

ADR-004 Session 2 forms a Q3 position that every event carries mandatory `shape_ref` in the format `{shape_name}/v{version}`. The shape registry maps that reference to a full shape snapshot; shapes may be authored as deltas but are stored as full versioned snapshots. All shape versions remain valid forever.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S00 envelope observations
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S06 schema evolution walkthrough
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `### 6.2 Positions on Envelope Questions` / Q3
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / Session 3 irreversibility filter

Closure basis:

Conditional high-irreversibility Session 2 position pending Session 3 stress test and ADR-004 verification.

Scope:

Applies to event envelope, shape registry, payload validation, projection across versions, and offline config coexistence.

Non-goals:

Does not decide final namespace syntax, registry storage implementation, or authoring format.

Forbidden interpretations:

- Do not store payloads without a shape/version reference if ADR-004 carries this candidate forward.
- Do not invalidate old events because a newer shape version exists.
- Do not treat shape deltas as the device-resolved runtime representation when the candidate says full snapshots are stored.

Open edges:

Session 3 must stress removal, rename, type changes, and format recovery cost.

Platform specification note:

Use as the current Q3 candidate.

## Kernel: Optional Activity Reference Envelope Candidate

Status: Conditional
Kind: contract

Specification statement:

ADR-004 Session 2 forms a Q11 position that events may carry optional `activity_ref` when shape alone does not disambiguate the operational context, especially campaigns and overlapping programs. Events whose shape uniquely identifies context may omit it or carry null.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S06 Q11 discussion
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S09 activity reference wall
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S20 activity reference check
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `### 6.2 Positions on Envelope Questions` / Q11

Closure basis:

Conditional medium-irreversibility Session 2 position pending Session 3 stress test and ADR-004 verification.

Scope:

Applies to campaigns, overlapping activities, reporting, progress monitoring, cross-activity coordination, and event envelope design.

Non-goals:

Does not decide final activity identity format or whether activity is a process identity, configuration object, or another reference type.

Forbidden interpretations:

- Do not infer campaign membership only from timestamp, actor assignment, or scope when the same shape can be used by multiple activities.
- Do not force activity reference overhead on scenarios where shape alone disambiguates.

Open edges:

Session 3 must stress multiple overlapping activities sharing the same shape.

Platform specification note:

Use as the current Q11 candidate.

## Kernel: Shape Version Coexistence Rule Candidate

Status: Conditional
Kind: invariant

Specification statement:

In-progress work completes under the shape version it started with. Devices apply new configuration after completing in-progress work and may keep current plus previous configuration versions for coexistence. Events captured offline under older shape versions remain valid when synced later.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S06 schema evolution scenario
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / Q4 position

Closure basis:

Conditional Session 2 configuration-delivery position.

Scope:

Applies to offline devices, in-progress forms, config package sync, shape version validity, and projection across versions.

Non-goals:

Does not decide exact config package format or whether "current + previous" is the final retention limit.

Forbidden interpretations:

- Do not switch an in-progress capture to a newer shape mid-form.
- Do not reject events from offline devices solely because they used the previously active shape version.

Open edges:

Session 3 must stress breaking schema changes and offline coexistence limits.

Platform specification note:

Use as Q4 configuration coexistence lineage.

## Kernel: ADR-004 Gradient Validation Result

Status: Conditional
Kind: configuration-boundary

Specification statement:

Session 2 validates the four-layer configuration gradient across S00, S06, S09, S12, and S20, with revisions: Layer 3 splits into event-reaction and deadline-check policies; Layer 1 expands to include cross-shape projection rules; Layer 0 needs platform-provided campaign patterns; trigger payload mapping is limited to static values and direct field references; Layer 2 expressions may read the event payload and one subject entity's attributes.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `### 6.1 Gradient Hypothesis Validation`
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `## 10. Key Findings Summary`

Closure basis:

Conditional Session 2 validation pending Session 3 stress test and ADR-004 verification.

Scope:

Applies to configuration layers, artifact types, trigger limits, projection rules, entity attribute access, and platform pattern inventory.

Non-goals:

Does not settle final artifact formats or exact hard numeric limits.

Forbidden interpretations:

- Do not keep the original Session 1 gradient unchanged after Session 2.
- Do not put aggregate campaign progress or cross-shape derivation into arbitrary trigger payload logic.

Open edges:

Session 3 must stress projection-rule scoping, shape complexity, expression data scope, and escalation depth.

Platform specification note:

Use as revised gradient lineage.

## Kernel: Bounded Trigger Engine Candidate

Status: Conditional
Kind: algorithm

Specification statement:

Session 2 identifies a Trigger Engine candidate with two bounded policy subtypes: event reactions evaluated synchronously at ingestion, and deadline checks evaluated asynchronously server-side. Both produce at most one event, are non-recursive, obey predicate/trigger/escalation limits, and restrict trigger output payload mapping to static values plus direct field references.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S12 walkthrough
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / Layer 3 sub-type revision
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / primitives map update

Closure basis:

Conditional candidate primitive pending Session 3 stress test and ADR-004 verification.

Scope:

Applies to event-triggered actions, deadline escalation, generated task/alert/escalation events, response matching, and trigger complexity limits.

Non-goals:

Does not decide final trigger artifact syntax or full event type vocabulary.

Forbidden interpretations:

- Do not allow trigger output payload mapping to perform expressions, lookups, or arbitrary computation.
- Do not allow unbounded trigger chains or device-side deadline evaluation across incomplete event timelines.

Open edges:

Session 3 must stress escalation depth and trigger composition.

Platform specification note:

Use as trigger primitive lineage.

## Kernel: Projection Rule Artifact Candidate

Status: Candidate
Kind: primitive

Specification statement:

Session 2 discovers projection rules as a new Layer 1 artifact type. Projection rules declare cross-shape data relationships for the projection engine, such as deriving supply usage from treatment fields in encounter events, without creating synthetic trigger events.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S20 cross-activity data flow
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `### 6.4 New Artifacts Discovered`
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / Session 3 unresolved questions

Closure basis:

Candidate artifact type pending Session 3 stress testing and ADR-004 verification.

Scope:

Applies to cross-shape projection composition, supply derivation, mapping tables, and projection engine configuration.

Non-goals:

Does not decide whether all cross-activity data flows can be handled through projection rules.

Forbidden interpretations:

- Do not implement cross-shape derivation by adding lookup/computation power to trigger payload maps if projection rules are the chosen path.
- Do not allow projection-rule chaining without later explicit closure.

Open edges:

Projection rule count, chaining, and scope limits need Session 3 stress testing.

Platform specification note:

Use as cross-shape projection configuration lineage.

## Kernel: Campaign Progress Platform Capability Candidate

Status: Candidate
Kind: configuration-boundary

Specification statement:

Session 2 identifies campaign progress monitoring as a platform-provided capability rather than deployer-built configuration. Deployers parameterize targets, scope granularity, thresholds, and reporting frequency, while the platform performs aggregate progress projection and alerting.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S09 progress monitoring wall
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `### 6.4 New Artifacts Discovered`
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / Session 3 unresolved questions

Closure basis:

Candidate platform capability pending Session 3 and ADR-004 verification.

Scope:

Applies to campaigns, aggregate progress monitoring, targets, coordinator dashboards, and platform pattern inventory.

Non-goals:

Does not decide full inventory of platform-provided patterns.

Forbidden interpretations:

- Do not expose arbitrary aggregate query expressions in Layer 3 merely to implement campaign progress.
- Do not force every recurring aggregate operational pattern into deployment-authored triggers.

Open edges:

Session 3 must decide which operational patterns belong in the platform capability library.

Platform specification note:

Use as platform-pattern lineage.

## Kernel: Role Action Permission Activity Parameter Candidate

Status: Conditional
Kind: configuration-boundary

Specification statement:

Session 2 forms a Q8 position that role-action permission mappings are Layer 0 activity parameters. Each activity declares which roles can perform which actions on which shapes, and the permission table is delivered to devices as part of the configuration package.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / Q8 position
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / S20 walkthrough
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / primitives map update

Closure basis:

Conditional Session 2 position pending later ADR-004 closure.

Scope:

Applies to ADR-003 role-action permission deferral, activity definitions, command validation, and synced configuration.

Non-goals:

Does not define complete permission table syntax or all role semantics.

Forbidden interpretations:

- Do not require an expression language for basic role-action permissions.
- Do not hide role-action permissions outside deployable configuration artifacts.

Open edges:

ADR-004 must verify final configuration location and delivery.

Platform specification note:

Use as Q8 lineage.

## Kernel: ADR-004 Session 2 Unstressed Question Set

Status: Open
Kind: open-question

Specification statement:

Session 2 did not resolve Q7 conflict rule configuration, Q9 flag-type severity configuration, Q10 scope type extensibility, or Q12 sensitive-subject classification. It recommends later stress testing or deferral according to each question's risk and evolvability.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `### 6.5 What the Walk-throughs Didn't Stress`

Closure basis:

Open Session 2 gap set.

Scope:

Applies to ADR-002 conflict deferrals, ADR-003 flag/scope/sensitivity deferrals, and ADR-004 remaining decision surface.

Non-goals:

Does not decide these questions.

Forbidden interpretations:

- Do not infer closure from absence of stress in S00/S06/S09/S12/S20.

Open edges:

Session 3 or later owning ADR sections must stress or explicitly defer these items.

Platform specification note:

Use as ADR-004 residual question tracker.

## Kernel: ADR-004 Session 3 Charter

Status: Open
Kind: open-question

Specification statement:

Session 3 must stress-test the Session 2 positions by applying irreversibility tests to `shape_ref` and `activity_ref`, then attacking shape complexity, cross-entity expressions, trigger/deadline chains, multi-activity shared shapes, and breaking schema changes. It must also examine platform pattern inventory, shape format, projection-rule scoping, and structural type vocabulary completeness.

Source basis:

- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` / `## 9. Session 3 Charter`

Closure basis:

Open handoff to ADR-004 Session 3.

Scope:

Applies to next ADR-004 exploration sources and final ADR-004 closure.

Non-goals:

Does not decide Session 3 outcomes.

Forbidden interpretations:

- Do not promote Q1/Q3/Q11 positions to final closure before Session 3 stress testing and ADR-004 verification.

Open edges:

Next source must stress-test or structurally classify these positions.

Platform specification note:

Use as the immediate handoff to ADR-004 Session 3.
