# Architecture Lineage Kernel Staging

Status: Iteration 13 staging split

This temporary staging file holds architecture-landscape and ADR-lineage candidate kernels. It is not a final atomic document and does not make exploration findings authoritative by itself.

## Staged Kernels

## Kernel: Architecture Landscape Superseded Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/01-architecture-landscape.md` is a raw, superseded exploration document. It maps viable architecture space, prior-art lessons, coupled decisions, and candidate ADR ordering, but does not make final decisions.

Source basis:

- `docs/exploration/archive/01-architecture-landscape.md` / supersession notice
- `docs/exploration/archive/01-architecture-landscape.md` / opening description

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels derived from `01-architecture-landscape.md`.

Non-goals:

Does not decide final storage, sync, configuration, identity, authorization, or workflow architecture.

Forbidden interpretations:

- Do not treat landscape-family analysis as final architecture.
- Do not treat candidate primitives or hybrid direction as ADR-settled.
- Do not use this document to override later ADR decisions.

Open edges:

Final closure remains with ADR-specific exploration and ADR files.

Platform specification note:

Use this source for lineage and rationale density, not as direct platform specification authority.

## Kernel: Constraint Filter Survivors

Status: Candidate
Kind: conditional-validity

Specification statement:

After applying the early constraints, viable architecture candidates must support substantial on-device logic and storage, interpreted configuration rather than hard-coded behavior, immutable or append-only records, selective sync, and composable configuration. These are exploration survivors, not final selected mechanisms.

Source basis:

- `docs/exploration/archive/01-architecture-landscape.md` / `## 1. What the Constraints Rule Out`

Closure basis:

Candidate exploration finding. Later ADRs must decide which mechanisms are actually selected.

Scope:

Applies to architecture-family filtering after vision, constraints, scenarios, viability, and principles.

Non-goals:

Does not decide thick-client architecture details, metadata model, event sourcing, sync contract, or configuration language.

Forbidden interpretations:

- Do not treat every survivor as a final requirement until ADRs close it.
- Do not revive eliminated families without new evidence and explicit later decision.

Open edges:

Final mechanism selection remains open until ADR extraction.

Platform specification note:

Use as lineage for why later decisions focus on offline-capable, configurable, traceable, selective-sync-compatible designs.

## Kernel: Prior-Art Failure Mode Set

Status: Candidate
Kind: rejected-alternative

Specification statement:

Prior art exposed repeated failure modes: configuration complexity becoming specialist-only, flat or form-dominant models failing to compose, offline conflicts becoming user-hostile, domain-standard lock-in, weak schema evolution under long offline periods, supervisor-scale sync pressure, and analytics/oversight being bolted on.

Source basis:

- `docs/exploration/archive/01-architecture-landscape.md` / `## 2. Prior Art — What Comparable Platforms Did and Where They Hit Walls`
- `docs/exploration/archive/01-architecture-landscape.md` / `### Cross-Cutting Lessons`

Closure basis:

Candidate exploration evidence and guardrail set. Later ADRs determine which lessons become binding constraints.

Scope:

Applies to DHIS2, CommCare, ODK/Kobo, OpenSRP, and cross-cutting lessons drawn from them.

Non-goals:

Does not reject all techniques used by those platforms. The document records both strengths and walls.

Forbidden interpretations:

- Do not adopt one prior-art platform's dominant primitive as the whole Datarun architecture without addressing its known wall.
- Do not make configuration specialist expertise the hidden replacement for developer effort.
- Do not lock the core model to a single domain standard.

Open edges:

Which prior-art lessons become final platform constraints remains to be determined by ADR extraction.

Platform specification note:

Use as rationale for avoiding form-only, domain-locked, flat-metadata, or bolted-on workflow/analytics designs.

## Kernel: Hybrid Architecture Candidate

Status: Candidate
Kind: conditional-validity

Specification statement:

The landscape exploration identifies a hybrid as the most viable candidate direction: metadata-driven configuration for setup, immutable/event-style storage for trustworthy records and offline reconciliation, and a composable vocabulary for activity behavior. The candidate is explicitly not final; the unresolved question is how simply these ideas can be combined.

Source basis:

- `docs/exploration/archive/01-architecture-landscape.md` / `### The Honest Assessment`
- `docs/exploration/archive/01-architecture-landscape.md` / `## Summary`

Closure basis:

Candidate exploration direction. Final architecture depends on ADR closure.

Scope:

Applies to early architecture-family selection and tradeoff framing.

Non-goals:

Does not decide exact event model, metadata schema, primitive vocabulary, projection model, or configuration language.

Forbidden interpretations:

- Do not treat the hybrid as already accepted architecture.
- Do not combine three architectural ideas without preserving simplicity pressure.

Open edges:

The balance among metadata, immutable storage, and composition remains to be closed by ADRs.

Platform specification note:

Use as lineage for the eventual combination of configuration, record history, and composition concepts if ADRs confirm them.

## Kernel: Offline Data Model Dependency Root

Status: Candidate
Kind: interaction-rule

Specification statement:

The landscape exploration identifies the offline data model as the root of the architecture dependency tree because storage and sync choices constrain schema evolution, configuration evaluation, identity, conflict resolution, authorization, and selective sync.

Source basis:

- `docs/exploration/archive/01-architecture-landscape.md` / `## 5. Critical Decision Intersections`
- `docs/exploration/archive/01-architecture-landscape.md` / `### ADR-1: Offline Data Model`

Closure basis:

Candidate decision-order finding. Later ADR-001 extraction must confirm, refine, or correct this.

Scope:

Applies to exploration sequencing and dependency analysis.

Non-goals:

Does not decide the offline data model itself.

Forbidden interpretations:

- Do not treat configuration boundary visibility as proof it can be decided before storage dependencies are understood.
- Do not decide downstream identity, sync, config, or workflow mechanisms before acknowledging their storage dependency.

Open edges:

Final dependency closure remains to be verified against ADR-001 and downstream ADRs.

Platform specification note:

Use as lineage for why storage/event/reference contracts may precede more visible configuration concerns.

## Kernel: Configuration Boundary Depends On Upstream Decisions

Status: Candidate
Kind: configuration-boundary

Specification statement:

Although configuration boundary collapse is the most visible viability risk, the landscape exploration argues that the configuration paradigm cannot be finalized first because it depends on the offline data model, identity model, and authorization model.

Source basis:

- `docs/exploration/archive/01-architecture-landscape.md` / `### Intersection 2: Configuration Paradigm x Offline Constraint`
- `docs/exploration/archive/01-architecture-landscape.md` / `### ADR-4: Configuration Paradigm and Boundary`

Closure basis:

Candidate decision-order correction. Later ADR ordering and ADR-004 extraction must confirm or refine it.

Scope:

Applies to configuration exploration sequencing.

Non-goals:

Does not decide the configuration boundary itself.

Forbidden interpretations:

- Do not ignore configuration-boundary risk.
- Do not finalize configuration semantics before knowing what data, identity, and authority are available offline.

Open edges:

Final configuration boundary remains to be extracted from ADR-specific exploration and ADR-004.

Platform specification note:

Use as lineage for why configuration is both critical and downstream of lower-level contracts.

## Kernel: Critical Decision Coupling Map

Status: Candidate
Kind: interaction-rule

Specification statement:

The landscape exploration identifies coupled decision pairs: offline data model with schema evolution; configuration paradigm with offline constraint; identity model with conflict resolution; authorization model with offline enforcement and selective sync.

Source basis:

- `docs/exploration/archive/01-architecture-landscape.md` / `## 5. Critical Decision Intersections`

Closure basis:

Candidate coupling map. Later exploration and ADRs must validate or revise each coupling.

Scope:

Applies to decision sequencing and cross-decision dependency checks.

Non-goals:

Does not close the outcome of any coupled decision.

Forbidden interpretations:

- Do not analyze these decision areas as independent if later sources preserve the coupling.
- Do not promote a downstream mechanism without its upstream dependency.

Open edges:

Each coupling remains to be checked against ADR-specific exploration and final ADR decisions.

Platform specification note:

Use as lineage for dependency-aware platform specification sections.

## Kernel: ADR Exploration Sequence Candidate

Status: Candidate
Kind: interaction-rule

Specification statement:

The landscape exploration proposes the first ADR sequence: offline data model; identity and conflict resolution; authorization and selective sync; configuration paradigm and boundary; state progression and workflow.

Source basis:

- `docs/exploration/archive/01-architecture-landscape.md` / `## 6. Decision Sequence for /ade`

Closure basis:

Candidate exploration sequence. Final authority comes from the ADR files and their associated exploration conclusions.

Scope:

Applies to lineage of why ADR work was explored in dependency order.

Non-goals:

Does not decide any ADR outcome.

Forbidden interpretations:

- Do not treat the sequence as implementation phases.
- Do not assume each proposed ADR scope stayed unchanged until the final ADR without checking later sources.

Open edges:

Final ADR scope and closure remain to be extracted from the ADR exploration files and ADR bodies.

Platform specification note:

Use as lineage for the platform specification's dependency ordering, only after ADR extraction confirms the resulting decisions.

