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

## Kernel: ADR-001 Exploration Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/02-adr1-offline-data-model.md` is a superseded ADR-001 exploration file. It explores the foundational offline storage primitive and narrows the decision space, but it explicitly does not make the final decision.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / supersession notice
- `docs/exploration/archive/02-adr1-offline-data-model.md` / opening description

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to ADR-001 lineage kernels extracted from this file.

Non-goals:

Does not select snapshots, events, action log, projection strategy, causal ordering, or downstream identity/sync/configuration/workflow mechanisms.

Forbidden interpretations:

- Do not treat the central storage option choice as final until ADR-001 is extracted.
- Do not treat examples in the exploration as platform interfaces.
- Do not use this file to override ADR-001.

Open edges:

Final offline data model closure remains with ADR-001 and any later approved sources that carry it forward.

Platform specification note:

Use this source for ADR-001 lineage, forced constraints, rejected directions, and open decision inputs.

## Kernel: Offline Data Model Irreversibility

Status: Settled
Kind: conditional-validity

Specification statement:

The offline data model is a high-irreversibility decision because changing the storage primitive after implementation would require data migration across many intermittently connected devices.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## Why This Is First`

Closure basis:

Settled as ADR-001 exploration methodology and risk classification.

Scope:

Applies to storage primitive, sync unit, conflict semantics, schema evolution, identity generation, and downstream architectural dependency sequencing.

Non-goals:

Does not decide the storage primitive.

Forbidden interpretations:

- Do not treat the offline data model as an implementation detail that can be safely deferred.
- Do not change the storage primitive without considering deployed-device migration.

Open edges:

The chosen storage primitive and its migration obligations remain to be confirmed by ADR-001.

Platform specification note:

Use as rationale for making the storage contract foundational in the platform specification.

## Kernel: Offline Data Model Subdecision Coupling

Status: Settled
Kind: interaction-rule

Specification statement:

The offline data model decomposes into coupled subdecisions: record mutability, write granularity, identity generation, sync unit, and conflict semantics. Record mutability constrains write granularity, write granularity constrains sync unit, sync unit constrains conflict semantics, and identity generation intersects with conflict detection.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## The Sub-Decisions`

Closure basis:

Settled as ADR-001 exploration structure.

Scope:

Applies to dependency-aware extraction of ADR-001 and downstream ADRs.

Non-goals:

Does not decide the outcome of each subdecision.

Forbidden interpretations:

- Do not extract record mutability, write granularity, sync unit, and conflict semantics as independent choices when source evidence treats them as coupled.

Open edges:

Final subdecision outcomes remain to be closed by ADR-001 and downstream ADRs.

Platform specification note:

Use as the dependency map for the offline storage section of the platform specification.

## Kernel: Append-Only Storage Forced

Status: Conditional
Kind: invariant

Specification statement:

Given the approved ground-truth pressure for trustworthy records and traceable corrections, mutable-in-place records with a separate audit log fail structurally. Records must be append-only: once written, a record is not modified or deleted; corrections, reviews, status changes, and amendments add new records that reference earlier records.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## S1: Record Mutability — What the Constraints Force`

Closure basis:

Conditional ADR-001 exploration conclusion. It is forcefully concluded by the exploration, but final ADR-level closure waits for ADR-001 extraction.

Scope:

Applies to operational record writes, corrections, review/status changes, and amendments.

Non-goals:

Does not decide whether the appended unit is a snapshot, event, or action-log entry.

Forbidden interpretations:

- Do not use mutable current-state rows plus a bolt-on audit log as the canonical storage model.
- Do not overwrite or delete prior operational records to express correction.

Open edges:

ADR-001 must confirm whether this becomes a settled platform invariant and what appended unit is selected.

Platform specification note:

Likely platform-spec invariant, pending ADR-001 closure.

## Kernel: Storage Unit Option Set

Status: Open
Kind: open-question

Specification statement:

After append-only storage is forced, the central ADR-001 exploration choice is the atomic appended unit: immutable snapshots, immutable events, or a pragmatic unified action log with materialized current-state views.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## S2: Write Granularity — The Real Choice`
- `docs/exploration/archive/02-adr1-offline-data-model.md` / `### The pivotal question`
- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## Decision Inputs Summary`

Closure basis:

Open in this exploration. Final selection belongs to ADR-001.

Scope:

Applies to the storage primitive and read/write model.

Non-goals:

Does not decide projection infrastructure, causal ordering, or conflict policies.

Forbidden interpretations:

- Do not treat a surviving option as selected.
- Do not retain snapshots as equally viable without the workflow caveat stated by the exploration.

Open edges:

ADR-001 must choose or refine the storage unit.

Platform specification note:

Use as lineage for the selected storage primitive and rejected alternatives.

## Kernel: Snapshot Workflow Weakness

Status: Conditional
Kind: rejected-alternative

Specification statement:

Immutable snapshots remain viable only if significant workflow scenarios are absent. Because Phase 1 includes review, transfers, long-running cases, multi-step approvals, and multi-level distribution, snapshots are weak for the platform's explored scope: workflow transitions force full-state copies to express small semantic actions.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `### S04/S11 — Review and Multi-Step Approval`
- `docs/exploration/archive/02-adr1-offline-data-model.md` / `### S07/S14 — Transfers and Multi-Level Distribution`
- `docs/exploration/archive/02-adr1-offline-data-model.md` / `### The pivotal question`

Closure basis:

Conditional exploration rejection. Final rejection depends on ADR-001 closure.

Scope:

Applies to immutable snapshot as the primary write model for workflow-heavy Phase 1 behavior.

Non-goals:

Does not reject snapshots as materialized views, read models, repair artifacts, or derived representations.

Forbidden interpretations:

- Do not model workflow actions as full-state replacements without acknowledging the duplication and semantic contortion cost.

Open edges:

ADR-001 must confirm whether snapshots are rejected as the primary appended unit.

Platform specification note:

Use as rejected-alternative lineage if ADR-001 chooses events or action log.

## Kernel: Client-Generated Identity Forced

Status: Conditional
Kind: invariant

Specification statement:

Offline-first creation forces client-generated identifiers for new records and subjects. Server-allocated sequential IDs require a network roundtrip, while preallocated pools add failure modes during extended offline work. Duplicate real-world subjects remain a domain-layer detection and human-resolution problem, not a storage-identifier problem.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## S3: Identity Generation — What Offline Forces`

Closure basis:

Conditional ADR-001 exploration conclusion. Final ADR-level closure waits for ADR-001 and ADR-002 extraction.

Scope:

Applies to record and subject identifier generation under disconnected operation.

Non-goals:

Does not decide duplicate detection rules, subject merge/split behavior, or identity lifecycle semantics.

Forbidden interpretations:

- Do not require a server roundtrip to create a record or subject offline.
- Do not treat UUID assignment as solving duplicate real-world identity.

Open edges:

ADR-001 must confirm the storage-side identifier rule; ADR-002 must close identity semantics.

Platform specification note:

Use as probable storage/identity invariant pending ADR confirmation.

## Kernel: Immutable Record Sync Shape

Status: Conditional
Kind: interaction-rule

Specification statement:

Given append-only records with client-generated IDs, sync naturally transfers immutable records the receiver has not seen, filtered by scope. Such sync is idempotent, append-only, order-independent, and scoped.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## S4: Sync Unit — What Follows from the Above`

Closure basis:

Conditional ADR-001 exploration conclusion. Sync scope and authorization details remain downstream.

Scope:

Applies to the storage-level sync unit shared by snapshots, events, and action-log entries.

Non-goals:

Does not decide selective-sync rules, authorization evaluation, transport protocol, or payload fields.

Forbidden interpretations:

- Do not make server sync mutate or delete device records as part of the base storage model.
- Do not assume arrival order is the same as logical or causal order.

Open edges:

ADR-001 must confirm immutable-record sync; ADR-003 must close scope and authorization behavior.

Platform specification note:

Use as candidate sync contract lineage pending ADR closure.

## Kernel: Conflict Surfacing Default

Status: Conditional
Kind: interaction-rule

Specification statement:

Conflict semantics must detect concurrent writes and surface conflicts for human resolution by default. Last-write-wins and invisible automatic merge are eliminated. Automatic resolution is valid only where the system can prove the conflict is structurally safe; otherwise human triage is required.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## S5: Conflict Semantics — What Principle P5 Forces`

Closure basis:

Conditional ADR-001 exploration conclusion. Final conflict-resolution policy belongs to later ADRs.

Scope:

Applies to concurrent writes touching the same subject or overlapping operational scope.

Non-goals:

Does not decide the conflict-resolution UI, policy catalog, merge algorithm, or flag semantics.

Forbidden interpretations:

- Do not use last-write-wins for operational conflicts.
- Do not hide automatic resolution where judgment is required.

Open edges:

ADR-001 may confirm storage-level conflict visibility; later ADRs must close conflict semantics and resolution policy.

Platform specification note:

Use as lineage for eventual conflict contract and rejected silent-resolution alternatives.

## Kernel: Materialized Reads Required For Performance

Status: Conditional
Kind: invariant

Specification statement:

For low-end field devices, current state must be precomputed or materialized for normal reads. Pure on-demand replay for every screen load is not acceptable; events remain performant only if materialized views are used for reads.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## Device Storage and Performance Realities`

Closure basis:

Conditional exploration conclusion, dependent on choosing events or action log.

Scope:

Applies to current-state reads on devices, especially field-worker screen loads.

Non-goals:

Does not decide materialized view schema, rebuild algorithm, projection ownership, or consistency contract.

Forbidden interpretations:

- Do not require routine screen loads to replay long event histories.
- Do not treat storage capacity as the primary differentiator among the explored options.

Open edges:

ADR-001 must close the read model and projection/materialized-view obligations.

Platform specification note:

Use as a performance invariant if the final model stores event-like entries.

## Kernel: ADR-001 Downstream Constraint Set

Status: Conditional
Kind: interaction-rule

Specification statement:

The ADR-001 exploration identifies downstream constraints that follow regardless of the final storage option: subject IDs are client-generated UUIDs; duplicate detection is domain-layer; sync transfers immutable records filtered by scope; authorization must evaluate against local state; configuration metadata is separate from operational data and produces immutable records; and workflow state transitions should be typed actions if events or action log are chosen.

Source basis:

- `docs/exploration/archive/02-adr1-offline-data-model.md` / `## How This Constrains Downstream Decisions`

Closure basis:

Conditional ADR-001 exploration map. Downstream ADRs must confirm, refine, or reject each constraint.

Scope:

Applies to ADR-002 identity, ADR-003 authorization/sync, ADR-004 configuration, and ADR-005 workflow lineage.

Non-goals:

Does not decide the downstream ADRs.

Forbidden interpretations:

- Do not let ADR-001 exploration decide conflict policies, sync scope, configuration expression, or workflow-state-machine details.

Open edges:

Later exploration files and ADRs must close each downstream constraint.

Platform specification note:

Use as a dependency checklist while extracting ADR-002 through ADR-005.
