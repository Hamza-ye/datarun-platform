# ADR-007 Envelope Type Assessment

Status: Later-source assessment against accepted ADR-001 through ADR-005 baseline

This document assesses `../../adrs/adr-007-envelope-type-closure.md` through the accepted baseline and validated boundary map. ADR-007 is assessment material only. It does not supersede ADR-001 through ADR-005 automatically.

## Source Basis

Assessment inputs:

- `02-change-control.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`
- `08-baseline-acceptance-check.md`
- `10-adr006r-flag-semantics-assessment.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `../../adrs/adr-004-configuration-boundary.md`, only to verify the ADR-004 structural event type decision
- `../../adrs/adr-007-envelope-type-closure.md`

Not used as authority:

- ADR-007 references to convergence inventory, architecture docs, phase specs, charter, ledger, ADR-002 Addendum, or other drift-era implementation artifacts.
- ADR-008 through ADR-009 claims. Those remain unassessed until their own passes.
- ADR-006/ADR-006-R claims except where already classified in `10-adr006r-flag-semantics-assessment.md`.

## Assessment Scope

ADR-007 addresses one of the known broadness failures: envelope `type` was being used as a domain/integrity fact vocabulary instead of as the closed platform processing vocabulary settled by ADR-004.

The safe reading is:

- `type` stays a platform-owned processing axis.
- `shape_ref` identifies the specific fact carried by the event payload.
- authorship stays in `actor_ref`.
- identity and integrity facts must not become new envelope types.

This is mostly consistent elaboration of the accepted ADR-001 through ADR-005 baseline.

## Boundary Routing

| Claim Area | Primary Boundary | Secondary Boundaries | Reason |
|---|---|---|---|
| closed six-value `type` vocabulary | Event Envelope / Schema | Configuration | Restates ADR-004 structural event type closure. |
| `type` as processing pipeline, not domain fact | Event Envelope / Schema | Configuration; Flag / Resolution; Identity / Lineage | Prevents domain/integrity facts from expanding envelope structure. |
| identity/integrity facts expressed by `shape_ref` | Event Envelope / Schema | Configuration; Identity / Lineage; Flag / Resolution | Routes fact discrimination through shape contract rather than structural type. |
| consumer filtering by `shape_ref` for domain facts | Event Envelope / Schema | All consumers of event facts | Provides a safe consumer rule for avoiding type-vocabulary drift. |
| deterministic flag identity includes `shape_ref` | Flag / Resolution | Event Envelope / Schema | Ties flag identity to the fact shape, but depends on general flag semantics still being assessed. |
| platform-bundled integrity/identity shapes | Configuration | Event Envelope / Schema; Identity / Lineage; Flag / Resolution | Raises platform-owned shape obligations that should be reconciled with ADR-009 duality before final atomization. |

## Claim Classification

| ADR-007 Claim | Classification | Assessment |
|---|---|---|
| S1: envelope `type` vocabulary is closed at six values | Consistent elaboration | ADR-004 already fixes six structural event types and ADR-005 adds no structural event type. ADR-007 is safe as a canonical restatement if ADR-004 remains the first-decision baseline cite. |
| S1: `type`, `shape_ref`, and `actor_ref` are orthogonal axes | Consistent elaboration | Compatible with ADR-004 `shape_ref`, system actor convention, and the identity boundary-control split. This directly prevents the broadness failure where domain fact, processing pipeline, and authorship collapse into one field. |
| S2: `conflict_detected/v1`, `conflict_resolved/v1`, `subjects_merged/v1`, and `subject_split/v1` are `shape_ref` values, not `type` values | Consistent elaboration plus deferred verification | The negative rule is safe: these must not become envelope type values. The exact bundled shape inventory should remain candidate language until ADR-008/009 are assessed and general flag semantics are closed. |
| S2: `conflict_detected/v1` rides `type=alert` | Open-gap closure candidate | Plausible under ADR-004 `alert` semantics and ADR-006-R candidate flag semantics, but depends on accepting general flag event-stream representation. Do not promote independently of the flag-semantics closure. |
| S2: manual `conflict_resolved/v1` rides `type=review`; auto-resolution rides `type=capture` | Open-gap closure candidate | This is a precise mapping for resolution facts. It is compatible with ADR-005 auto-resolution actor attribution, but final acceptance depends on closing general flag resolution semantics. |
| S2: `subjects_merged/v1` and `subject_split/v1` ride `type=capture` | Consistent elaboration plus identity-spec candidate | Compatible with identity lineage as append-only facts and with not adding envelope types. Exact shape contracts should be captured in identity/lineage spec only after alias-cycle handling is decided or explicitly deferred. |
| S3: consumers identify identity/integrity facts by `shape_ref`, not `type` | Consistent elaboration | Safe and useful as a boundary guardrail. It preserves `type` as processing vocabulary and prevents implementation modules from reintroducing pre-convergence drift. |
| S4: deterministic flag identity includes `shape_ref` | Open-gap closure candidate plus deferred spec detail | Compatible with shape-based fact discrimination, but belongs to Flag / Resolution and depends on general flag identity semantics. Do not absorb before ADR-006-R/ADR-007/ADR-008/ADR-009 are jointly classified for flags. |
| S5: platform-bundled shape registry obligation for four integrity/identity shapes | Open-gap closure candidate | Directionally compatible with platform-owned mechanisms and deployer-configured shapes, but needs ADR-009 duality assessment before final classification. It must not be read as deployers authoring platform integrity shapes. |
| Forbidden patterns F-A1 through F-A4 | Consistent elaboration | Safe as guardrails: do not add envelope `type` values, do not discriminate domain facts by `type`, do not encode authorship in `type`, and escalate type-vocabulary expansion. |
| Forbidden pattern F-A5 | Open-gap closure candidate | Useful guardrail, but shape-across-type authorship rules should be accepted only alongside the final shape/reference classification from ADR-008/009. |
| Supersession of ADR-002 Addendum | Deferred source-management detail | The addendum is not part of the accepted ADR-001 through ADR-005 baseline authority for this pass. ADR-007 can be used as assessment material without accepting repository-wide supersession mechanics. |

## Accepted Carry-Forward Candidates

The following ADR-007 material is safe to carry forward as candidate platform-spec language:

- The envelope `type` vocabulary remains the six ADR-004 structural processing values.
- `type` answers processing pipeline, not domain fact, authorship, lifecycle ownership, or conflict category.
- Domain/integrity/identity fact discrimination uses `shape_ref`.
- Consumers must not key identity or integrity fact behavior off extra `type` values such as `conflict_detected`, `conflict_resolved`, `subjects_merged`, or `subject_split`.
- Adding an envelope `type` value is an architecture-grade change.

These candidates reinforce the accepted baseline rather than changing it.

## Items Not Safe To Absorb Yet

- A fully closed platform-bundled integrity/identity shape inventory.
- Final flag event identity semantics.
- Final resolution-event type mapping for all manual and automated cases.
- A general rule for shapes spanning multiple envelope types.
- Any claim that relies on ADR-008 or ADR-009 before those assessments are complete.

These should be held as candidates for the Event Envelope / Schema, Configuration, Identity / Lineage, and Flag / Resolution atomization passes.

## Baseline Impact

No ADR-001 through ADR-005 baseline item should be changed by this assessment alone.

No new gap is required from ADR-007 by itself. Existing gaps already cover the dependent areas:

- `General flag semantics`
- `Alias-Cycle Enforcement And Resolution Semantics`
- `Event schema and versioning tooling`
- `Configuration authoring and deployment UX`

## Completed Follow-Up

ADR-008 was assessed in `12-adr008-reference-fields-assessment.md`.

During atomization, use this assessment to preserve the `type` versus `shape_ref` separation and the listed envelope guardrails.
