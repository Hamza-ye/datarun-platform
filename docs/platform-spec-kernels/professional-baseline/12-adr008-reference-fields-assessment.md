# ADR-008 Reference Fields Assessment

Status: Later-source assessment against accepted ADR-001 through ADR-005 baseline

This document assesses `../../adrs/adr-008-envelope-reference-fields.md` through the accepted baseline, identity boundary-routing input, and architecture responsibility map. ADR-008 is assessment material only. It does not supersede ADR-001 through ADR-005 automatically.

## Source Basis

Assessment inputs:

- `02-change-control.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`
- `08-baseline-acceptance-check.md`
- `09-identity-boundary-control.md`
- `11-adr007-envelope-type-assessment.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `../../adrs/adr-002-identity-conflict.md`, only to verify the ADR-002 typed-reference decision
- `../../adrs/adr-004-configuration-boundary.md`, only to verify `activity_ref` and system actor convention
- `../../adrs/adr-008-envelope-reference-fields.md`

Not used as authority:

- ADR-008 references to convergence inventory, charter, ledger, flagged positions, or other post-convergence tracking artifacts.
- ADR-009 claims. Those remain unassessed until their own pass.

## Assessment Scope

ADR-008 addresses another known broadness failure: reference fields were being classified as if the field and the thing it points to were the same object.

The safe reading is:

- a `*_ref` field is an envelope contract
- the referent has its own lifecycle owner and classification
- `actor_ref` authorship is separate from device identity and envelope `type`
- `activity_ref` is a deployer-chosen configuration reference, not a platform structural type
- reference categories do not grant Identity / Lineage ownership over actor, assignment, process, activity, or workflow lifecycles
- `actor_ref` does not encode product role labels, responsibility classes, or the offline/online operation boundary

This is mostly consistent with the accepted baseline and strongly reinforces `09-identity-boundary-control.md`.

## Responsibility Routing

| Claim Area | Primary Responsibility Area | Affected Areas | Reason |
|---|---|---|---|
| reference field contract shape | Event Envelope / Schema | Identity / Lineage; Assignment / Authority / Sync; Configuration | Envelope owns the field contract; lifecycle owners consume references. |
| reference is not referent | Event Envelope / Schema | All referent-owning boundaries | Prevents contract fields from becoming lifecycle ownership claims. |
| `subject_ref` typed reference contract | Event Envelope / Schema | Identity / Lineage; Assignment / Authority / Sync; Projection / Workflow State | Carries typed reference values without assigning all lifecycle semantics to Identity / Lineage. |
| `actor_ref` human/system authorship contract | Event Envelope / Schema | Assignment / Authority / Sync; Trigger / Reactivity; Flag / Resolution | Authorship reference supports authority and system-authored events without becoming device identity. |
| `activity_ref` optional deployer-chosen reference | Event Envelope / Schema | Configuration; Assignment / Authority / Sync; Projection / Workflow State | Activity value is deployer configuration; envelope field is platform contract. |
| possible future dedicated `assignment_ref` | Event Envelope / Schema | Assignment / Authority / Sync | Future structural reference question; not closed by the accepted baseline. |

## Claim Classification

| ADR-008 Claim | Classification | Assessment |
|---|---|---|
| S1: `subject_ref` is a contract envelope field with typed `{type, id}` shape | Consistent elaboration, with bounded spec-detail value | ADR-002 already closes typed identity references. This is safe if read as envelope/reference contract, not as Identity / Lineage ownership over every referent. |
| S1: `subject_ref.type` enum contains `subject`, `actor`, `process`, and `assignment` | Consistent elaboration plus active-emission detail pending future controlled work | ADR-002 names these four typed reference categories. The exact field serialization and active emission set should be handled under the event-envelope/reference responsibility, preserving `09`'s rule that categories are not ownership categories. |
| S1: `process` is reserved with no current emission site | Deferred implementation/spec detail | Compatible with the current responsibility routing of process identity to Projection / Workflow State. Do not treat this as a settled process lifecycle decision. |
| S1: extending the typed-reference enum is architecture-grade | Consistent elaboration | Compatible with the accepted envelope stability rule. New reference categories would affect stored event interpretation and need formal decision. |
| S2: `actor_ref` supports human UUID and `system:{source_type}/{source_id}` forms | Consistent elaboration | Compatible with ADR-004 system actor convention and ADR-005 auto-resolution attribution. It must not collapse actor identity into device identity. |
| S2: `actor_ref.source_type` is evolvable, not a closed enum | Consistent elaboration plus deferred platform convention detail | Safe as long as the envelope field shape stays stable and new source types do not add envelope fields or redefine authority. |
| S2: human/system authorship discriminator is `system:` prefix | Open-gap closure candidate | Useful contract detail, but final acceptance should be coordinated with ADR-007 shape/type and ADR-009 mechanism/configuration classification. |
| S3: `activity_ref` is optional, deployer-chosen, and auto-populated from activity context | Consistent elaboration plus implementation/spec detail | Compatible with ADR-004 optional `activity_ref`. Auto-population behavior is spec/implementation detail, not a new architecture baseline by itself. |
| S3: activity instance is config while `activity_ref` is contract | Consistent elaboration | Strongly aligned with the reference-vs-referent rule and the platform/deployer boundary. |
| S4: reference is not referent | Consistent elaboration | Safe and important carry-forward. It matches the current identity boundary-routing input and should constrain any future controlled specification work. |
| F-B1: never classify a `*_ref` field and referent in the same row | Consistent elaboration | Safe as a classification constraint. |
| F-B2: never extend `subject_ref.type` without an ADR | Consistent elaboration | Safe as an envelope stability constraint. |
| F-B3: never treat `actor_ref.source_type` as closed enum | Consistent elaboration | Safe if scoped to source-type vocabulary only, not the envelope field shape. |
| F-B4: do not use envelope `type` for authorship | Consistent elaboration | Reinforces ADR-007 assessment. Authorship belongs in `actor_ref`; processing belongs in `type`. |
| Future dedicated `assignment_ref` question | Deferred architecture decision candidate | No current baseline change. If assignment references need a separate envelope field rather than the existing typed-reference channel, that is an Event Envelope / Schema decision with Assignment / Authority / Sync impact. |

## Accepted Carry-Forward Candidates

The following ADR-008 material is safe to carry forward as candidate future specification language:

- Reference fields are envelope contracts.
- Referents have separate lifecycle owners and classifications.
- `subject_ref`, `actor_ref`, and `activity_ref` must not be treated as primitives by themselves.
- `actor_ref` is the authorship field; `device_id` is not actor identity.
- `actor_ref` is not a role taxonomy or permission shortcut.
- `activity_ref` points to deployer configuration and must not become a structural event type.
- Extending the typed-reference category set is an architecture-grade change.

These candidates narrow broadness and reinforce the accepted baseline.

## Items Not Safe To Absorb Yet

- Exact final serialization for every reference field.
- Active emission sites for `process` references.
- Any lifecycle semantics for process, assignment, activity, or actor referents.
- A dedicated `assignment_ref` field.
- Final human/system authorship discriminator behavior if ADR-009 changes mechanism/configuration classification.

These should remain candidates under Event Envelope / Schema, Identity / Lineage, Assignment / Authority / Sync, Configuration, and Projection / Workflow State ownership.

## Baseline Impact

No ADR-001 through ADR-005 baseline item should be changed by this assessment alone.

No new gap is required from ADR-008 by itself. Existing gaps and open/deferred items already cover the dependent areas:

- `Event schema and versioning tooling`
- `Shared Device Actor Scope`
- `Exact Pattern Registry inventory`
- `Formal Pattern Registry schema format`
- `Configuration authoring and deployment UX`

## Completed Follow-Up

ADR-009 was assessed in `13-adr009-duality-rule-assessment.md`.

If this assessment is later reused, preserve the reference-versus-referent distinction without turning reference fields into broad lifecycle ownership.
