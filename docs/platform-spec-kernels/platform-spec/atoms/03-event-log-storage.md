# Event Log And Storage

Status: Draft
Owning boundary: Event Log / Storage
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../../professional-baseline/16-operational-constraints-boundary-control.md`
- `90-open-decisions.md`
- `91-rejected-paths.md`

Depends on:

- `01-spec-governance.md`
- `02-glossary-and-core-definitions.md` (planned; this atom remains draft until glossary terms are reconciled)
- `90-open-decisions.md`
- `91-rejected-paths.md`

Consumed by:

- `04-event-envelope-schema.md`
- `05-references-and-identity-lineage.md`
- `07-assignment-authority-and-sync.md`
- `09-projections-workflow-and-patterns.md`
- `10-conflict-flag-and-resolution.md`
- `12-reporting-aggregation-and-freshness.md`
- implementation designs for event persistence, projection rebuild, sync storage, retention, and archival

## Purpose

This atom defines the platform's canonical storage boundary: operational facts are stored as immutable events in an append-only event log, and current state is derived from those events through projections.

## Scope

This atom owns:

- append-only event log as source of truth
- immutable operational fact persistence
- write-path source-of-truth discipline
- projection rebuild from events
- derived read-model status of projections, views, snapshots, caches, and reports
- storage-side rejected paths that would create competing canonical truth

## Non-Scope

This atom does not own:

- event-envelope field definitions or serialization details
- event structural validation vocabulary
- identity merge/split semantics
- assignment-derived authorization
- sync delivery protocol mechanics
- deployer configuration language
- workflow state-machine semantics
- flag lifecycle or resolution semantics
- reporting product semantics
- local data lifecycle after scope changes
- retention and archival policy
- projection optimization and caching strategy

## Definitions

| Term | Meaning In This Atom | Must Not Mean |
|---|---|---|
| Event log | The append-only canonical store of accepted immutable events | A mutable record table, action-log sidecar, or projection cache |
| Operational fact | A valid accepted event preserved as historical fact | A guarantee that the event is clean, conflict-free, authorized for all downstream effects, or workflow-valid |
| Append-only | Accepted events are added as new facts; interpretation changes through later events and projections | No validation, no correction, or no lifecycle policy |
| Projection | Derived state computed from events and relevant configuration/pattern definitions | Canonical storage or directly patchable truth |
| Read model | A materialized view, cache, summary, report, or query shape derived from events | Source of truth |
| Projection rebuild | Recomputing derived state from available event subsets | Rewriting history or repairing canonical state by patching projections |
| Event subset | The events available to a server or device under sync/access/local lifecycle constraints | Complete global history on every device |

## Invariants

- The event log is the canonical source of truth for operational facts.
- All state changes enter through the event store.
- Accepted events are immutable and append-only.
- Current state is projection-derived.
- Projections, views, snapshots, caches, reports, and dashboard data are derived artifacts, not canonical truth.
- Projection repair happens by recomputing from events, not by directly patching projection state as canonical state.
- Projection rebuild scope is limited by the event subset available to the device or server doing the rebuild.
- Valid operational facts are preserved even when later checks surface state, authority, workflow, identity, or configured-domain anomalies.
- Resolution changes interpretation and projection, not historical event identity.

## Contracts

### Inputs

- structurally valid event envelopes accepted through the Event Envelope / Schema boundary
- client-generated event identifiers where required by the baseline
- event subsets delivered through the Assignment / Authority / Sync boundary

### Outputs

- immutable event stream
- accepted event persistence result
- event subsets available for projection rebuild
- event source material for downstream derived read models
- storage-side append-only guarantees consumed by sync, projections, flags, reporting, and local lifecycle work

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Event Envelope / Schema | event envelope contract | Storage accepts events through the stable envelope/schema contract but does not define envelope fields. |
| Assignment / Authority / Sync | sync event delivery contract | Sync distributes immutable events by assigned access scope; storage does not own sync filtering semantics. |
| Projection / Workflow State | projection rebuild contract | Storage emits immutable event source material; projection owners combine events with any required pattern/configuration inputs. |
| Flag / Resolution | append-only fact and resolution-event contract | Flags and resolutions may affect interpretation; they must not mutate source events. |
| Reporting / Aggregation | derived read-model contract | Reports and aggregates consume projections/events under access constraints; they are not source truth. |
| Local Data Lifecycle | event subset and retain/remove handoff | Local removal or purge behavior must not mutate central canonical events. |

## Allowed Extension Points

- Physical storage technology may vary if append-only source-of-truth semantics are preserved.
- Indexes, caches, snapshots, and materialized views may be added as derived artifacts.
- Projection rebuild strategies may evolve as implementation/tooling detail.
- Retention and archival policies may be specified later if they do not silently mutate canonical history.
- Import/export jobs may consume events or derived views if external schemas do not become canonical platform storage.

## Forbidden Couplings

- Do not make mutable records canonical truth.
- Do not use a separate audit log to compensate for mutable canonical records.
- Do not make snapshots, projections, caches, reports, queues, dashboards, or work items primary source of truth.
- Do not directly patch projection state as canonical repair.
- Do not mutate, delete, redact, or rewrite canonical events without formal baseline reconsideration.
- Do not resolve conflicts by rewriting historical events.
- Do not make storage own identity, assignment, authorization, workflow, flag lifecycle, reporting product, or deployer configuration semantics.
- Do not replace event-log truth with local cache truth for performance or low-end device constraints.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| `02-glossary-and-core-definitions.md` is not drafted yet | Cross-boundary definitions | Before this atom can be accepted. |
| Retention and archival | `90-open-decisions.md`; Event Log / Storage plus Local Data Lifecycle | Compliance, self-host, export, or storage-scale requirement needs retention behavior. |
| Projection optimization and caching | `90-open-decisions.md`; Event Log / Storage plus Projection / Workflow State | Implementation performance design begins. |
| Event schema/versioning tooling | `90-open-decisions.md`; Event Envelope / Schema | Envelope atom moves from conceptual contract to implementation schema. |
| Projection merge strategy across schema versions | Decision gap register; Projection / Workflow State plus Event Envelope / Schema | Schema migration or mixed-version projection behavior must be specified. |
| Structured import/export contracts | `90-open-decisions.md`; Event Envelope / Schema plus Reporting / Aggregation | First deployment requires import/export or audit exchange. |
| Local purge/lifecycle rules for sensitive data | `90-open-decisions.md`; Local Data Lifecycle | Sensitive deployment or scope contraction behavior must be implemented. |

## Rejected Paths

- Mutable canonical records plus separate audit log.
- Snapshot-primary source-of-truth storage.
- Action-log-primary source-of-truth storage.
- Direct canonical projection patching.
- Treating projections, caches, reports, queues, or dashboards as canonical operational truth.
- Deleting, redacting, or mutating canonical events without formal baseline reconsideration.
- Replacing event-log source of truth with snapshots or caches for performance.
- Treating delayed central visibility as a storage defect.
- Mutating historical events to satisfy export convenience.

## Implementation Implications

- The write path must persist accepted events as immutable facts before any derived state is treated as current state.
- Projection stores should be rebuildable from the event subset available to their execution context.
- Storage implementations may optimize reads, but optimization cannot create a second source of truth.
- Downstream effects that depend on validity, authority, workflow state, or conflict status must respect detect-before-act at the boundary that owns those effects.
- Devices are not required to hold complete global history; local rebuilds operate over scoped event subsets.
- Sensitive local lifecycle behavior must be designed separately from central event-log immutability.

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] Owner and boundary are singular.
- [ ] Scope and non-scope are explicit.
- [ ] Contracts identify inputs, outputs, and boundary crossings.
- [ ] Open gaps are not closed accidentally.
- [ ] Forbidden couplings include storage and projection drift risks.
- [ ] No envelope field, type value, authority shortcut, or canonical projection state was added without change control.
- [ ] Product labels, role labels, queues, dashboards, and UI surfaces remain outside canonical storage semantics.
