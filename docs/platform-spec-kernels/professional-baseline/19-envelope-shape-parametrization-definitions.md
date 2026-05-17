# Envelope, Shape, And Parametrization Definitions

Status: Platform-spec definition file

This file provides compact definitions for platform-spec writers. It is derived from ADR-004 closure, ADR-005 reinforcement, and the boundary-control overlay in `18-envelope-shape-parametrization-boundary-control.md`.

It does not add platform behavior. It defines how to read already-accepted boundaries.

## Core Definitions

### Event Envelope

The event envelope is the stable contract every event carries so the platform can store, sync, route, attribute, and interpret immutable facts.

The envelope is platform-owned. Adding envelope fields, changing field meaning, or adding structural reference categories requires formal change control.

### Envelope `type`

Envelope `type` is the platform-owned processing-pipeline discriminator.

Current values:

| Type | Processing meaning |
|---|---|
| `capture` | Data record entering validation and projection; ordinarily human-authored, with system-authored capture only where a specific platform mapping accepts it |
| `review` | Human judgment on another event, with source linking and review-status projection |
| `alert` | Condition notification or anomaly routed for attention |
| `task_created` | Work assignment requiring tracking or expected response |
| `task_completed` | Response to a task, linked to the source task |
| `assignment_changed` | Scope or role modification affecting assignment and sync-scope projection |

`type` does not mean domain fact, role, lifecycle state, product surface, authorship, online/offline class, tenant, deployment, or authority.

### `shape_ref`

`shape_ref` identifies the payload schema and version an event conforms to.

Format:

```text
{shape_name}/v{version}
```

Shape names match:

```text
[a-z][a-z0-9_]*
```

Illustrative examples, not final shape inventory:

```text
household_observation/v1
case_intake/v1
review_decision/v1
```

`shape_ref` answers "what fact shape is this payload?" It does not answer "which pipeline processes this event?" That is `type`.

### Payload

Payload is the shape-conforming fact body carried by an event.

Payload fields express domain data, source-event links where the shape needs them, review reasons, alert metadata, task details, or similar fact content. Payload does not change the envelope contract.

### `activity_ref`

`activity_ref` identifies the deployer-configured activity instance in which the event was produced, or null when no activity context is known.

It distinguishes same-shape work across activities, campaigns, routines, or configured operational contexts.

`activity_ref` is not a pattern reference, tenant/deployment reference, work-item identity, or immutable authority snapshot.

### `actor_ref`

`actor_ref` identifies who or what authored the event.

Human actors and system actors both author events. System actor values follow the `system:{source_type}/{source_id}` convention.

Do not infer role class, permission, or product persona from `actor_ref` alone. Effective authority comes from assignments, roles, scopes, activity/context, time, projections, and sync scope.

### Subject Reference

`subject_ref` identifies what the event is about. It is a reference contract, not ownership of every lifecycle involving that referent.

Subject identity, process state, assignment authority, and activity configuration remain separate boundaries.

### Shape

A shape is a typed payload schema. Shapes can be deployer-defined or platform-bundled.

Shape addition and shape versioning are the normal way to add new fact vocabulary without changing the envelope `type` vocabulary.

### Platform-Bundled Shape

A platform-bundled shape is a shape supplied by platform code for a platform-owned fact. It is still identified by `shape_ref`; it does not become a new envelope `type`.

This definition does not accept a final platform-bundled shape inventory. Exact bundled shapes remain governed by the relevant identity, flag, event-envelope, and platform-spec sections.

### Activity

An activity is a deployer-configured L0 instance that binds shapes, pattern selections, role/action mappings, scope parameters, schedules, deadlines, thresholds, or policy values.

An activity is configuration. The `activity_ref` field is the envelope contract that points to the configured instance.

### Pattern

A pattern is a platform-fixed workflow skeleton that deployers select and parameterize.

The Pattern Registry mechanism is platform-owned. A configured activity that selects a pattern is deployer configuration.

Patterns may define participant capacities, structural event types involved, valid transitions, projections, and parameterization points. Participant capacities are not actor subclasses.

### Projection

A projection is derived state or a read model computed from events plus configuration and pattern definitions.

Review queues, review status, current interpretation, oversight counts, pending/stale labels, and workflow state are projections or product surfaces unless a formal decision says otherwise.

### Deployer Parameterization

Deployer parameterization means selecting values inside platform-owned mechanisms.

Examples:

- shape names and fields
- activity identifiers
- role labels and role-action mappings
- assignment scope values
- pattern bindings
- deadlines and thresholds
- flag severity values
- sensitivity levels
- domain uniqueness rule values

Parameterization does not let deployers author envelope fields, event type values, scope containment logic, state-machine mechanisms, access-control programs, or arbitrary platform code.

## Boundary Matrix

| Question | Use |
|---|---|
| Which platform pipeline handles this event? | `type` |
| What fact schema/version does the payload carry? | `shape_ref` |
| Who or what authored it? | `actor_ref` |
| What is it about? | `subject_ref` |
| Which configured activity produced it? | `activity_ref` |
| What operation or workflow state is currently visible? | projection/pattern-derived state |
| Who may see or act on it? | assignment-derived access and sync scope |
| Which role label appears in UI? | product/deployer vocabulary mapped to assignments and capacities |
| Can this work happen offline? | operation class plus sync/authority rules |

## Review Disambiguation

Use this table whenever `review` appears:

| Term | Means | Does not mean |
|---|---|---|
| `type=review` | Envelope processing path for human judgment on another event | Reviewer actor class or Review service boundary |
| `capture_with_review` | Pattern skeleton for submit/review/return/accept behavior | Full Pattern Registry inventory |
| review decision | Payload fact carried by a review shape | New type value |
| review queue | Projection/product surface | Canonical work-item storage |
| reviewer/supervisor | Configured role/capacity/operational label | Platform-owned actor subclass |

## Extension Rules

### Add A Shape When

Add a shape when the platform or deployer needs to record a new fact structure but the processing pipeline is already covered by an existing `type`.

Examples:

- case intake
- stock receipt
- service feedback
- conflict detected
- conflict resolved
- review decision
- return reason

### Add A Pattern When

Add a pattern when the platform needs a new workflow skeleton: states, transition rules, projections, and participant capacities that cannot be represented by an existing pattern plus parameterization.

Adding a pattern is platform evolution. It is not deployer-authored configuration.

### Add Configuration When

Add configuration when a deployer selects, names, binds, scopes, or parameterizes existing platform mechanisms.

Examples:

- bind `case_intake/v1` to an intake shape role
- bind a deployer role label to reviewer capacity
- set a review deadline
- configure activity scope
- set flag severity

### Add An Envelope Type Only When

Add an envelope `type` only when a new event class needs genuinely different platform processing behavior that cannot be expressed through existing types, shapes, references, pattern definitions, projections, or configuration.

This is architecture-grade and requires formal change control.

## Forbidden Encodings

Never encode these as envelope `type` values:

- case opened
- case resolved
- stock received
- feedback submitted
- referral accepted
- conflict detected
- conflict resolved
- subjects merged
- subject split
- submitted
- approved
- returned
- closed
- reopened
- supervisor action
- coordinator action
- auditor action
- campaign event
- routine event
- pending sync
- stale
- work item
- review item

Use `shape_ref`, payload, activity context, patterns, projections, assignments, and product vocabulary instead.

## Selected-Slice Interpretation

For the selected first slice:

```text
Assigned offline capture -> sync visibility -> authorized review -> returned correction -> evidence/history -> minimal freshness-aware oversight
```

Interpretation:

- capture is a `capture` event with the configured information shape
- review is a `review` event carrying a judgment/reason shape
- returned correction is workflow/pattern state plus new immutable events, not mutation of the original event
- review queue is projection-derived
- reviewer is a configured capacity/role under assignment-derived authority
- oversight counts are freshness-aware read models
- no fixed `Supervisor`, `Coordinator`, `Reviewer`, or `WorkItem` core class is implied
- the minimal review loop may use a narrow `capture_with_review` pattern, but does not close full Pattern Registry inventory or schema

## Spec Drafting Rule

When in doubt, keep the axes separate:

```text
processing -> type
fact schema -> shape_ref
authorship -> actor_ref
subject/aboutness -> subject_ref
activity context -> activity_ref
workflow behavior -> pattern + projection
authority -> assignment + scope + sync
deployer variation -> configuration
user wording -> product/deployer vocabulary
```
