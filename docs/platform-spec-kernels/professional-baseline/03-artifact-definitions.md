# Artifact Definitions

This document defines the artifacts needed to move from extraction evidence to engineering-ready platform specification work.

## Evidence Archive

Purpose:

- preserve source-specific extraction, lineage, rationale, closure basis, and conflicts

Current files:

- `../02-domain-requirement-kernels.md`
- `../03-behavioral-viability-principle-kernels.md`
- `../04-architecture-lineage-kernels.md`
- `../05-methodology-and-extraction-rules.md`
- `../06-adr2-identity-conflict-kernels.md`
- `../07-adr3-authorization-sync-kernels.md`
- `../08-adr4-configuration-boundary-kernels.md`
- `../09-adr5-state-progression-kernels.md`

Rules:

- do not build directly from the evidence archive
- do not continue expanding it unless a missing source must be preserved
- use it to verify baseline items and disputes

## Closure Overlay

Purpose:

- compact the evidence archive into the current accepted closure surface

Current file:

- `../10-adr1-5-rest-state-closure-register.md`

Rules:

- it is the source map for baseline drafting
- it is not the final platform specification
- it must point back to evidence anchors
- it must separate settled, open, rejected, deferred, and dispute lanes

## Architecture Baseline v0

Purpose:

- give engineering a concise current architecture baseline

Current file:

- `04-architecture-baseline-v0.md`

Expected content:

- storage model
- event envelope
- identity/reference model
- conflict/stale-event handling
- authorization and sync
- configuration boundary
- projection and workflow
- closed flag interactions from ADR-001 through ADR-005
- open decisions and deferred details

Rules:

- no ADR narrative
- no exploration rationale unless needed to state a boundary
- no ADR-006-R through ADR-009 closure
- no implementation design beyond the accepted baseline

## Decision Gap Register

Purpose:

- identify what still needs a decision before final spec or implementation

Current file:

- `05-decision-gap-register.md`

Each gap should include:

- short name
- classification
- primary responsibility area
- affected areas
- baseline item affected
- why it is still open
- closure path, priority, and platform-spec handling
- hold-back reopen triggers where later-source material is valid but not safe to absorb yet
- whether ADR-006-R through ADR-009 may contain relevant assessment material

Allowed classifications:

- architecture decision gap
- platform-spec detail gap
- implementation/tooling gap
- operational policy gap
- product validation gap
- later-source assessment gap

## Architecture Responsibility Map

Purpose:

- route settled mechanisms, gaps, later claims, and future spec artifacts through explicit architecture responsibility areas

Current file:

- `07-system-boundary-map.md`

The map should include:

- the document altitude it occupies
- classification of earlier boundary candidates
- retained responsibility areas
- demoted or routed areas
- cross-boundary contracts
- post-baseline assessment routing rules
- disposition of the earlier boundary-map treatment

Rules:

- it is generated from the closure overlay, architecture baseline, and gap register
- viability primitive groupings may be used only as lineage/context, not authority
- every gap should have one primary responsibility owner in `05-decision-gap-register.md`
- ADR-006-R through ADR-009 claims must be classified against responsibility areas before they can affect the baseline
- responsibility names are routing surfaces, not implementation module names

## Platform Specification Outline

Purpose:

- provide the top-level outline for final platform specification documents before detailed spec sections are accepted

Current file:

- `20-platform-spec-outline.md`

Expected sections:

- event model
- event envelope
- identity and references
- sync and authorization
- configuration model
- projection and workflow
- flags and conflict surfacing
- open decisions
- rejected alternatives

Rules:

- generated from the accepted architecture baseline and gap register
- routed through the architecture responsibility map
- not generated directly from ADRs or exploration files
- should be stable enough for engineering review

## Baseline Stabilization Plan

Purpose:

- define the operating order for accepting the baseline, assigning closure paths, triaging gaps, and assessing later ADRs without broad re-extraction

Current file:

- `06-baseline-stabilization-plan.md`

Rules:

- use it before assessing ADR-006-R through ADR-009
- do not use it as source evidence for platform behavior
- update it only when the operating process changes

## Baseline Acceptance Check

Purpose:

- record whether the ADR-001 through ADR-005 baseline is stable enough to build from

Current file:

- `08-baseline-acceptance-check.md`

Expected content:

- accepted closed baseline items
- accepted open gaps
- accepted rejected paths
- suspicious or disputed items
- immediate next order

Rules:

- it is a sign-off artifact, not another extraction pass
- it is based only on the closure register, architecture baseline, and decision gap register
- it may prioritize accepted gaps, but it must not close them

## Identity Boundary Control

Purpose:

- prevent ADR-002's broad identity/conflict decision shape from becoming broad implementation coupling

Current file:

- `09-identity-boundary-control.md`

Expected content:

- accepted ADR-002 core
- reference-category interpretation
- responsibility split by owning area
- identity-owned and identity-forbidden areas
- dependency-aware ADR boundary checks
- implementation constraints
- open coupling risks

Rules:

- it must not re-decide ADR-002
- it must preserve ADR-003 through ADR-005 assumptions
- it must separate reference protocol from lifecycle ownership
- it must keep process, assignment, authority, conflict resolution, and reporting outside subject-lineage ownership

## Implementation-Facing Specification Sections

Purpose:

- turn the accepted baseline and gap register into buildable platform specification sections after the platform-spec outline is stable
- keep implementation guidance out of baseline artifacts

Target groups:

- normative behavior
- contracts and data shapes
- invariants
- algorithms and derivation rules
- configuration surfaces
- cross-boundary interactions
- forbidden patterns
- open decisions
- rejected alternatives

Rules:

- do not create or accept detailed spec sections until the platform-spec outline is stable
- do not split by ADR number or by a local process label
- every settled statement must retain source basis
- every open decision must cite `05-decision-gap-register.md`
