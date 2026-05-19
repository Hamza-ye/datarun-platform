# ADR-009 Duality Rule Assessment

Status: Later-source assessment against accepted ADR-001 through ADR-005 baseline

This document assesses `../../adrs/adr-009-platform-fixed-vs-deployer-configured.md` through the accepted baseline and architecture responsibility map. ADR-009 is assessment material only. It does not supersede ADR-001 through ADR-005 automatically.

## Source Basis

Assessment inputs:

- `02-change-control.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`
- `08-baseline-acceptance-check.md`
- `11-adr007-envelope-type-assessment.md`
- `12-adr008-reference-fields-assessment.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `../../adrs/adr-003-authorization-sync.md`, only to verify assignment/scope closure
- `../../adrs/adr-004-configuration-boundary.md`, only to verify scope-type closure and activity/configuration gradient
- `../../adrs/adr-005-state-progression.md`, only to verify Pattern Registry closure
- `../../adrs/adr-009-platform-fixed-vs-deployer-configured.md`

Not used as authority:

- ADR-009 references to convergence inventory, phase topology, charter, ledger, or architecture rewrite plans.

## Assessment Scope

ADR-009 names a classification rule already latent in ADR-003, ADR-004, and ADR-005:

- platform-fixed mechanism: closed vocabulary, grammar, protocol, containment test, or typed interface owned by the platform
- deployer-configured instance: named, parameterized occurrence assembled and shipped by a deployer within bounded configuration

The rule is useful because prior implementation work conflated mechanism and instance, causing broadness drift. The safe reading is that ADR-009 is classification constraint material for future controlled specification or implementation assessment, not a new runtime subsystem.

## Responsibility Routing

| Claim Area | Primary Responsibility Area | Affected Areas | Reason |
|---|---|---|---|
| mechanism/instance duality rule | Configuration | Event Envelope / Schema; Assignment / Authority / Sync; Projection / Workflow State | Configuration owns the platform/deployer boundary, while specific mechanisms live in their owning boundaries. |
| scope mechanism | Assignment / Authority / Sync | Configuration | Scope containment and sync/access semantics are platform-owned; deployers configure instances within fixed scope types. |
| scope instances | Configuration | Assignment / Authority / Sync | Instance values are deployer configuration consumed by the authorization/sync boundary. |
| pattern mechanism | Projection / Workflow State | Configuration | Pattern Registry is platform-owned workflow mechanism. |
| pattern/activity instances | Configuration | Projection / Workflow State | Deployers bind patterns, shapes, roles, scopes, and parameters into activities. |
| activity as deployer-configured instance | Configuration | Event Envelope / Schema; Projection / Workflow State; Assignment / Authority / Sync | Activity is L0 configuration; `activity_ref` remains the envelope contract that points to it. |

Concrete role labels and pattern participant bindings sit on the instance side of this split. They may configure who can participate in a workflow capacity, but they do not create platform-fixed actor subclasses.

## Claim Classification

| ADR-009 Claim | Classification | Assessment |
|---|---|---|
| S1: split platform-fixed mechanism from deployer-configured instance | Consistent elaboration | This formalizes the baseline's platform/deployer boundary and should govern future controlled specification or implementation assessment. It does not change ADR-001 through ADR-005; it prevents one concept row or future specification section from mixing closure semantics with deployer configuration. |
| S1: mechanism is platform-owned and extension is architecture-grade | Consistent elaboration | Compatible with structural event type closure, fixed scope types, bounded configuration, and platform-owned Pattern Registry. |
| S1: instance is deployer-authored configuration | Consistent elaboration | Compatible with ADR-004's four-layer gradient and deployer policy surfaces. Must stay bounded; instance authoring cannot become deployer-authored platform logic. |
| S2: `scope` mechanism is platform-fixed | Consistent elaboration | ADR-003 and ADR-004 already close assignment-derived access, sync scope as access scope, and platform-fixed scope types. This clarifies classification only. |
| S2: concrete scope instances remain configuration | Consistent elaboration plus future specification detail | Safe if treated as deployer-provided values under platform-owned containment semantics. Does not close subject-based scope or auditor access beyond existing gaps. |
| S3: `pattern` mechanism is platform-fixed | Consistent elaboration | ADR-005 already closes Pattern Registry as platform-owned workflow primitive. ADR-009 clarifies why deployers select patterns rather than author new mechanisms. |
| S3: pattern-related instance rows remain separate | Consistent elaboration plus future specification detail | Safe as classification guidance. Exact Pattern Registry inventory and formal schema remain P1 gaps. |
| S4: `activity` is deployer-configured instance | Consistent elaboration | ADR-004 places activities in bounded configuration; ADR-008 separates `activity_ref` field contract from the activity referent. This is safe and useful. |
| S4: `activity_ref` remains contract while `activity` is config | Consistent elaboration | Reinforces ADR-008 assessment and prevents reference/referent conflation. |
| F-C1: do not classify mechanism as config or instance as primitive | Consistent elaboration | Safe as a forbidden-pattern constraint for future controlled specification and later claim assessment. |
| General future use of the duality rule | Consistent elaboration with change-control limit | Safe as a classification test. It must not be used to close a new mechanism without checking whether that mechanism is actually open, closed, or disputed under the baseline. |

## Accepted Carry-Forward Candidates

The following ADR-009 material is safe to carry forward as candidate future specification language:

- Split mechanism and instance whenever a concept has both platform-owned closure semantics and deployer-authored occurrences.
- Platform-fixed mechanisms are not deployer knobs.
- Deployer-configured instances are not platform primitives.
- Scope mechanism belongs to Assignment / Authority / Sync; concrete scope instance values are configuration.
- Pattern mechanism belongs to Projection / Workflow State; deployer pattern bindings are configuration.
- Activity is configuration; `activity_ref` is the envelope contract that points to it.
- Product/deployer role labels and pattern participant mappings are instance/configuration vocabulary, not platform-owned actor types.

These candidates directly reduce the implementation broadness that produced earlier agent confusion.

## Items Not Safe To Absorb Yet

- Exact Pattern Registry inventory.
- Formal pattern schema format.
- Subject-based scope or auditor access semantics.
- Any new scope type beyond the accepted baseline's fixed scope composition.
- Treating the duality rule as permission to create new platform mechanisms without formal closure.

These remain governed by the existing gap register and change-control rules.

## Baseline Impact

No ADR-001 through ADR-005 baseline item should be changed by this assessment alone.

No new gap is required from ADR-009 by itself. Existing gaps already cover the dependent areas:

- `Subject-Based Scope And Auditor Access`
- `Exact Pattern Registry Inventory`
- `Formal Pattern Registry Schema Format`
- `Configuration Authoring And Deployment UX`

## Future Classification Check

If this material is later used for controlled specification work, do not create a single section that mixes:

- a platform-fixed mechanism and deployer-authored instances
- a reference field and its referent
- an envelope processing axis and domain fact shapes
- a subject-lineage lifecycle and actor/assignment/process/activity lifecycles
- a product role label or pattern participant name and a platform actor class

ADR-007, ADR-008, and ADR-009 together form a useful classification triad:

- ADR-007: `type` is not domain fact.
- ADR-008: reference is not referent.
- ADR-009: mechanism is not instance.

## Completed Follow-Up

The later-source assessment sequence is closed for ADR-006-R through ADR-009.

If this assessment is later reused, use the accepted carry-forward candidates and explicit hold-backs from `10`, `11`, `12`, and this assessment together with the routed findings in `15` and `16`.
