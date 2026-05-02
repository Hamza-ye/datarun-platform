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
