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
- not generated directly from ADRs or exploration files
- should be stable enough for engineering review

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
