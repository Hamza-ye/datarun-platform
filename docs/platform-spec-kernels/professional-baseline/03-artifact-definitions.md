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
- current owner or likely decision path
- baseline item affected
- why it is still open
- whether ADR-006-R through ADR-009 may contain relevant assessment material

Allowed classifications:

- architecture decision gap
- platform-spec detail gap
- implementation/tooling gap
- operational policy gap
- later-source assessment gap

## System Boundary Map

Purpose:

- route settled mechanisms, gaps, later claims, and future spec artifacts through explicit engineering boundaries

Current file:

- `07-system-boundary-map.md`

Each boundary should include:

- what it owns
- what it does not own
- inputs and outputs
- how crossing the boundary is allowed
- settled mechanisms
- open/deferred items
- forbidden coupling

Rules:

- it is generated from the closure overlay, architecture baseline, and gap register
- viability primitive groupings may be used only as lineage/context, not authority
- every gap should have one primary owning boundary
- ADR-006-R through ADR-009 claims must be classified against boundaries before they can affect the baseline
- boundary names are routing surfaces, not implementation module names

## Platform Spec Skeleton

Purpose:

- provide the outline for final platform specification documents

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
- routed through the system boundary map
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

## Final Atomic Kernel Files

Purpose:

- split rest-state kernels into final categories after conflicts are resolved

Target groups:

- primitives
- contracts
- invariants
- algorithms
- configuration
- interactions
- forbidden-patterns
- open-questions
- rejected-alternatives

Rules:

- do not create these until rest state is proven
- do not split by ADR number
- every settled kernel must retain source basis
