# 02 Event Log And Storage Model

Status: Draft
Owning boundary: Event Log / Storage
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/16-operational-constraints-boundary-control.md`
- `../../professional-baseline/20-platform-spec-outline.md`

Depends on:

- `00-specification-source-authority.md`
- `01-core-definitions-and-boundary-vocabulary.md`
- `90-open-decisions-and-gap-register-citations.md`
- `91-rejected-alternatives.md`

Consumed by:

- `03-event-envelope-schema-and-references.md`
- identity, authority/sync, projection/workflow, flag/resolution, reporting, and local lifecycle sections
- engineering design for event persistence only after acceptance

## Purpose

This section defines the platform storage model: accepted operational facts are stored as immutable events in an append-only event log, and current state is derived from events through projections.

## Scope

This section owns:

- event log as canonical source of operational facts
- append-only acceptance discipline
- immutable event persistence
- projection-derived state rule
- storage-side rejected paths that would create competing canonical truth

## Non-Scope

This section does not own:

- event-envelope field definitions or serialization
- event structural validation details
- schema/versioning tooling
- projection optimization, caching, or rebuild algorithms
- retention, archival, deletion, redaction, or purge policy
- sync delivery mechanics
- identity, authorization, workflow, flag, reporting, configuration, or local lifecycle behavior

## Definitions

| Term | Meaning In This Section | Must Not Mean |
|---|---|---|
| Event log | Append-only canonical store of accepted events | Mutable record table, audit sidecar, projection cache, or queue store |
| Accepted event | Structurally valid event persisted as an immutable operational fact | Proof that the event is state-clean, authority-clean, workflow-valid, or conflict-free |
| Append-only | Accepted events are added as new facts; later interpretation changes through later events and projections | No validation, no correction path, no retention policy, or no lifecycle policy |
| Projection | Derived state/read model computed from events and required configuration/patterns | Canonical truth or directly patchable source state |
| Event subset | Events available to a server or device under sync, access, and local lifecycle constraints | Complete global history on every device |

## Invariants

- The event log is the canonical source of truth for accepted operational facts.
- Accepted events are immutable and append-only.
- All state changes that become canonical operational facts enter through the event store.
- Current state, workflow state, reports, queues, dashboards, snapshots, caches, and product statuses are derived artifacts.
- Projection repair happens by recomputing from events, not by patching projection state as canonical truth.
- Resolution changes interpretation and projection, not historical event identity.
- Validly structured events may later be flagged for state, authority, workflow, identity-lineage, or configured-domain anomalies without mutating the source event.
- Devices are not required to hold complete global history; local rebuilds operate over scoped event subsets.

## Contracts

### Inputs

- structurally valid event envelopes from Event Envelope / Schema
- client-generated event identifiers where required by the accepted baseline
- event subsets delivered through Assignment / Authority / Sync

### Outputs

- immutable persisted event stream
- accepted-event persistence result
- source material for projections, sync, flags, reporting, and local lifecycle decisions

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Event Envelope / Schema | structurally valid event envelope | Storage persists accepted envelopes; it does not define fields or schema tooling. |
| Assignment / Authority / Sync | immutable event stream and event subsets | Sync filters delivery by access scope; storage does not own sync mechanics. |
| Projection / Workflow State | event source material | Projection owners combine events with configuration and patterns. |
| Flag / Resolution | source event and resolution event facts | Flags/resolutions affect interpretation; they must not mutate source events. |
| Reporting / Aggregation | derived read-model source material | Reports are not source truth. |
| Local Data Lifecycle | event subset handoff | Local remove/purge behavior must not mutate central canonical events. |

## Allowed Extension Points

- Physical storage technology may vary if append-only source-of-truth semantics are preserved.
- Indexes, caches, snapshots, and materialized views may be added as derived artifacts.
- Projection rebuild strategies may evolve as implementation/tooling detail.
- Retention/archive policy may be specified later if it does not silently mutate canonical history.

## Forbidden Couplings

- Do not make mutable records canonical truth.
- Do not use a separate audit log to compensate for mutable canonical records.
- Do not make snapshots, projections, caches, queues, dashboards, or reports primary source of truth.
- Do not directly patch projection state as canonical repair.
- Do not mutate, delete, redact, or rewrite accepted events without formal baseline reconsideration.
- Do not resolve conflicts by rewriting historical events.
- Do not replace event-log truth with local cache truth for performance or low-end device constraints.
- Do not treat delayed central visibility as a storage defect.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Retention and archival | `05`; Event Log / Storage plus Local Data Lifecycle | A section defines deletion, redaction, archival, legal hold, export retention, or canonical-history mutation. |
| Projection performance and caching | `05`; Event Log / Storage plus Projection / Workflow State | Implementation performance design begins. |
| Low-end device scale and offline performance | `05`; Local Data Lifecycle plus Assignment / Authority / Sync | Device constraints require changing local event subsets, rebuild behavior, or storage budgets. |
| Structured import/export compatibility | `05`; Event Envelope / Schema plus Reporting / Aggregation | External exchange or audit export becomes part of the selected slice. |
| Event schema and versioning tooling | `05`; Event Envelope / Schema plus implementation tooling | Append validation needs concrete schema/version tooling. |

## Rejected Paths

- Mutable canonical records plus separate audit log.
- Snapshot-primary or action-log-primary source-of-truth storage.
- Direct canonical projection patching.
- Treating projections, caches, reports, queues, dashboards, or work items as canonical operational truth.
- Deleting, redacting, or mutating canonical events without formal baseline reconsideration.
- Mutating historical events to satisfy export convenience.

## Implementation Implications

- Write-path design must persist accepted events as immutable facts before derived state is treated as current.
- Storage optimization may improve read performance but cannot create a second source of truth.
- Append-to-log implementation is not ready from this section alone; it also needs accepted source authority, vocabulary, envelope/schema/reference, open-decision, and rejected-alternative coverage.

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] Open `05` gaps are cited and not closed.
- [ ] Projection/read-model state remains derived.
- [ ] Retention, archival, caching, and performance policy are constrained or held back.
- [ ] No rejected storage path is reintroduced.
