# Authoritative Architecture Map

## Context Capsule

- Recovery mode: archaeological reconstruction only; no redesign, no alternatives, no reopening.
- Anchor file: `002-phase0-decision-register.md`.
- Immediate input source:
  - `007-phase5-cross-lineage-vocabulary.md`.
- Supporting recovered sources:
  - `003-phase1-adr2-identity-conflict-recovery.md`
  - `004-phase2-adr3-auth-sync-recovery.md`
  - `005-phase3-adr4-config-boundary-recovery.md`
  - `006-phase4-adr5-state-progression-recovery.md`
- Phase target:
  - Revised Phase 6: final authoritative architecture reference.
- Document status:
  - This is not an ADR.
  - This is not an implementation design.
  - This is the vocabulary, primitive-boundary, and interaction reference recovered from ADR-001 through ADR-005.
- Settled outputs:
  - One-page architectural closure.
  - Final event contract.
  - Platform vocabulary map.
  - Primitive taxonomy and boundary map.
  - Identity, conflict, authorization, configuration, and workflow architecture maps.
  - Cross-primitive interaction flows.
  - Decision-boundary classification map.
  - Open evolution register.
  - Negative boundary register.
- Rejected / excluded:
  - Any term that appears only in exploration without ADR sub-decision support.
  - Rejected alternatives from the recovery passes.
  - Implementation mechanisms such as database tables, queues, indexes, caches, UI widgets, local storage partitions, authoring-file formats, and deployment tooling internals.
- Deferred / open evolution:
  - Exact Pattern Registry inventory.
  - Pattern migration mechanics.
  - Additional platform-fixed patterns.
  - Additional `context.*` values.
  - Additional bounded auto-resolution policies.
  - Richer flag/source-chain UX.
  - Priority sync, pagination, backfill, regulatory encryption/redaction/erasure, and multi-tenant naming strategy.
- Terms locked in this pass:
  - No new platform runtime primitive is introduced here.
  - This file locks the recovered architecture map as a reference organization of already-settled ADR vocabulary.

---

## 1. Reading Rules

This document is the missing reference layer between ADRs and implementation.

Use it to answer:

- what each platform term means;
- which primitive owns which responsibility;
- where a responsibility stops;
- which fields are durable event contracts;
- what is configuration versus platform evolution;
- what is structural, strategy-protecting, initial strategy, or implementation detail.

Do not use it to infer:

- database schemas;
- service boundaries;
- API shapes;
- UI screens;
- queue topology;
- cache/index strategy;
- exact pattern skeleton inventory;
- exact config authoring syntax.

Those are implementation or platform-specification concerns unless explicitly listed as settled architecture below.

---

## 2. Core Architectural Closure

The recovered architecture closes around one invariant:

```txt
Events are durable facts.
Projections derive meaning.
Configuration bounds what deployers may express.
Runtime services guard invariants before downstream action.
```

Expanded by ADR:

| ADR | Architectural closure |
|---|---|
| ADR-001 | The platform stores typed immutable events as the append-only source of truth; projections are rebuildable; sync moves immutable events. |
| ADR-002 | Events carry typed identity references and causal metadata; subject lineage is append-only; stale or conflicting events are accepted and flagged; raw references are checked before alias projection. |
| ADR-003 | Authorization is assignment-based and projection-derived; sync scope equals access scope; authority is not stored in the event envelope. |
| ADR-004 | The platform/deployment boundary is fixed through event types, `shape_ref`, `activity_ref`, four configuration layers, bounded expression language, fixed scope types, and server-only triggers. |
| ADR-005 | Workflow state is projection-derived through platform-fixed patterns; invalid transitions are flagged, not rejected; source-chain traversal and bounded auto-resolution extend the flag model. |

Nothing after ADR-001 changes the source-of-truth rule.

---

## 3. Final Event Contract

### 3.1 Envelope fields

The final event envelope has exactly these 11 recovered fields:

```txt
id
 type
 shape_ref
 activity_ref
 subject_ref
 actor_ref
 device_id
 device_seq
 sync_watermark
 timestamp
 payload
```

### 3.2 Field ownership

| Field | Presence | Meaning | ADR anchor |
|---|---:|---|---|
| `id` | Mandatory | Client-generated UUID for the event. | ADR-001 S3 |
| `type` | Mandatory | Platform-fixed structural processing type. | ADR-001 S5; ADR-004 S3 |
| `shape_ref` | Mandatory | Payload schema reference, format `{shape_name}/v{version}`. | ADR-004 S1 |
| `activity_ref` | Optional | Activity instance identifier; correlation context. | ADR-004 S2 |
| `subject_ref` | Mandatory | Typed identity reference to what the event is about. | ADR-002 S2 |
| `actor_ref` | Mandatory | Typed identity reference to human actor or system actor. | ADR-002 S2; ADR-004 S4 |
| `device_id` | Mandatory | Hardware-bound device identity. | ADR-002 S1/S5 |
| `device_seq` | Mandatory | Durable monotonic sequence per device. | ADR-002 S1/S4 |
| `sync_watermark` | Mandatory | Last known server sync position at event creation; null until first sync. | ADR-002 S1/S4 |
| `timestamp` | Mandatory | Advisory device time / human-facing capture time. | ADR-001 S5; ADR-002 S3 |
| `payload` | Mandatory | Shape-validated event data. | ADR-001 S5; ADR-004 S10 |

### 3.3 Event type vocabulary

The platform-fixed structural `type` values are:

```txt
capture
review
alert
task_created
task_completed
assignment_changed
```

Rules:

- `type` expresses platform processing behavior, not deployment domain meaning.
- Domain meaning belongs in `shape_ref`, `activity_ref`, payload, and configuration.
- New structural event types are platform evolution only, and only when new platform processing behavior is required.
- `status_changed` is not part of ADR-005's settled structural vocabulary.

### 3.4 Deliberate non-fields

| Non-field | Architectural reason |
|---|---|
| `authority_context` | Authority is projection-derived from the assignment timeline. |
| `assignment_ref` / `assignment_refs` | Rejected authority-context envelope designs. |
| `pattern_ref` | Pattern is derived from activity + shape configuration. |
| `current_state` | Workflow state is projection-derived, never stored in events. |
| `transition_validity` | Computed by runtime services and expressed through flags. |
| `resolvability` | Platform-level classification of flag categories, not an event envelope field. |
| `context.*` values | Pre-resolved form context; not stored event data. |

---

## 4. Platform Vocabulary Map

### 4.1 Storage vocabulary

| Term | Definition | Boundary |
|---|---|---|
| event | Typed immutable fact written to the event store. | Source of truth, not a read model. |
| event store | Append-only durable store of events. | Does not store current state as truth. |
| projection | Rebuildable derived state from events and configuration. | Can be recomputed; not authoritative over events. |
| correction | New event that references or supersedes earlier event content. | Never an in-place update. |
| write-path discipline | Every state change enters through the event store. | Prevents hidden mutable side channels. |
| sync unit | Immutable event. | Sync does not exchange mutable snapshots as truth. |

### 4.2 Identity vocabulary

| Term | Definition | Boundary |
|---|---|---|
| typed identity reference | `{type, id}` pointer to a platform identity. | Untyped UUIDs are not enough. |
| subject | Real-world thing an event is about. | Owns lineage/alias semantics, not authorization. |
| actor | Person or system identity that authors or performs work. | Separate from device and assignment. |
| process | Operational process identity. | Not the same as activity, pattern, campaign definition, or trigger process. |
| assignment | Identity category whose ADR-003 semantics are authorization grants. | Role alone is insufficient; scope and time matter. |
| `device_id` | Hardware-bound identity for causal sequence namespace. | Not actor-bound or account-bound. |
| `device_seq` | Durable per-device monotonic sequence. | Not a global order. |
| `sync_watermark` | Server sync position known to device when event was created. | Used for staleness/concurrency detection. |
| `device_time` | Advisory timestamp. | Not structurally trusted for ordering. |

### 4.3 Conflict and flag vocabulary

| Term | Definition | Boundary |
|---|---|---|
| accept-and-flag | Store event, surface anomaly through flag. | Rejecting stale offline work is not the model. |
| flag | Event-linked anomaly requiring classification and handling. | Category, severity, resolvability, and resolver are distinct. |
| `ConflictDetected` | Flag event/record identifying a conflict and designated resolver. | Resolution is single-writer. |
| `ConflictResolved` | Resolution event authored by designated resolver or system auto-resolution. | Conflicting resolutions are not co-equal. |
| designated resolver | Exactly one resolver for a conflict instance. | Avoids recursive meta-conflicts. |
| detect-before-act | Conflict/flag evaluation precedes policy execution. | Flagged events do not trigger downstream policy until allowed/resolved. |
| raw-reference detection | Conflict detection evaluates original references as written. | Alias resolution happens later in projection. |
| flagged-event exclusion | Unresolved flagged events are visible but excluded from workflow state derivation. | They remain in timeline. |
| flag resolvability | Platform-level category: `auto_eligible` or `manual_only`. | Not deployer-configurable. |

### 4.4 Authorization vocabulary

| Term | Definition | Boundary |
|---|---|---|
| assignment-based access | Access model based on active assignments. | Not generic RBAC alone and not arbitrary ABAC. |
| scope-containment test | Access target must be contained by assignment scope. | Scope containment logic is platform-controlled. |
| sync scope = access scope | Devices receive exactly authorized data. | Sync is not an independent data distribution mechanism. |
| authority-as-projection | Authority reconstructed from assignment timeline. | No authority envelope field. |
| assignment timeline | Event-derived history of assignment grants/changes. | Source for authority projection. |
| alias-respects-original-scope | Authorization evaluates original subject reference where aliasing crosses scope. | Avoids post-merge privilege distortion. |
| scope-containment invariant | New assignment scope must be within creator authority. | Prevents privilege escalation. |
| selective-retain | Initial strategy for handling local data after scope contraction. | Device policy, not event rewriting. |

### 4.5 Configuration vocabulary

| Term | Definition | Boundary |
|---|---|---|
| shape | Typed payload schema. | Domain data structure, not event type. |
| shape version | Specific schema version referenced by events. | Historical interpretation remains stable. |
| shape registry | Registry for shape definitions and versions. | Storage/authoring mechanics can evolve. |
| activity instance | Deployer-configured operational context such as campaign or routine program. | Referenced by optional `activity_ref`. |
| activity definition | L0 configuration artifact. | Definition can evolve; event retains ID only. |
| sensitivity classification | Shape/activity-level sensitivity category. | Field-level sensitivity is rejected in ADR-004. |
| domain uniqueness | Deployer-parameterized policy for cross-event uniqueness. | Produces flags; does not create custom conflict engine. |
| config package | Atomic set of configuration artifacts delivered on sync. | Devices do not run partial config. |

### 4.6 Workflow vocabulary

| Term | Definition | Boundary |
|---|---|---|
| projection-derived state machine | State computed from events, pattern definitions, config version, and flag status. | State is not stored and invalid transitions are not rejected. |
| Pattern Registry | Platform-fixed registry of workflow skeletons. | Deployer selects/parameterizes; deployer does not author state machines. |
| pattern | Platform-defined workflow skeleton. | Exact inventory can evolve. |
| subject-level pattern | Pattern deriving lifecycle state for a subject in an activity. | At most one subject-level pattern per activity. |
| event-level pattern | Pattern deriving state for a source event or sub-flow. | Can compose freely. |
| source-only flagging | Only root-cause event receives the stored flag. | Downstream events receive computed warnings, not copied flags. |
| source-chain traversal | Projection walks `source_event_ref` lineage to surface upstream flag state. | Projection behavior, not stored flag propagation. |
| Command Validator | Advisory validator and server-side flag-generation boundary for transition validity. | Does not reject offline events. |
| auto-resolution | Bounded L3b subtype for `auto_eligible` flags. | Not available for `manual_only` flags. |

---

## 5. Primitive Taxonomy

### 5.1 Structural contracts

Structural contracts are stored-event or protocol-level commitments. Changing them requires migration, permanent dual semantics, or broad protocol change.

| Contract | Owns | Does not own |
|---|---|---|
| Event Store | Append-only immutable facts and write-path discipline. | Current state, policy choices, UI state. |
| Event Envelope | Durable event interpretation fields. | Authority context, current state, pattern ref. |
| Event Type Vocabulary | Platform processing categories. | Domain event names. |
| Typed Identity Reference | `{type,id}` pointer format. | Category-specific policy. |
| Causal Ordering Contract | `device_id`, `device_seq`, `sync_watermark`, advisory timestamp. | Global total order. |
| Subject Lineage Contract | Merge aliasing, split archival, lineage DAG. | Physical rewrite or symmetric unmerge. |
| Shape Reference Contract | Mandatory historical schema reference. | Shape authoring UI or registry storage internals. |
| Activity Reference Contract | Optional activity-instance correlation. | Mandatory provenance or pattern selection field. |
| System Actor Contract | Auditable system actor identity. | Anonymous automation writes. |
| Assignment Access Contract | Assignment-based access and sync=access. | Authority envelope fields or arbitrary access code. |

### 5.2 Strategy-protecting runtime services

These protect structural invariants. Their internal implementation can evolve, but their architectural boundary is settled.

| Service | Owns | Does not own |
|---|---|---|
| Subject Identity Resolver | Merge/split validation and lineage DAG constraints. | Physical event rewriting. |
| Conflict Detector | Raw-reference checks, causal/stale/conflict/domain/transition flag creation. | Human resolution judgment. |
| Conflict Resolver | Single-writer resolution flow. | Competing canonical resolutions. |
| Assignment Resolver | Active assignment timeline, role/scope/time authority projection. | Envelope authority assertions. |
| Scope Resolver | Platform-fixed scope containment. | Deployer-authored containment functions. |
| Sync Scope Resolver | Data delivery equal to access scope. | Independent sync distribution policy. |
| Trigger Engine | Server-only 3a/3b trigger execution. | Device trigger execution or recursive trigger chains. |
| Config Package Validator | Dependency, budget, version, scope, trigger, and pattern validation before delivery. | Runtime repair of invalid config. |
| Command Validator | Advisory transition warnings and server-side flag generation. | Rejection of offline writes. |
| Auto-resolution Engine | Bounded L3b resolution for eligible flags. | Manual-only flag resolution or unbounded rules. |

### 5.3 Projection / read-model primitives

| Projection | Derives | Does not derive as stored truth |
|---|---|---|
| Subject Projection | Current subject view from event stream and lineage. | Mutable subject record. |
| Alias Table Projection | Retired-to-surviving subject mapping with transitive closure. | Rewriting event references. |
| Assignment Timeline Projection | Actor authority over time. | Authority envelope fields. |
| Sync Scope Projection | Authorized data set for device/actor. | Unscoped data delivery. |
| Workflow State Projection | Pattern-derived current state. | Stored `current_state`. |
| Flag Queue Projection | Review/resolution work list. | New flag semantics outside categories. |
| Source Chain Projection | Upstream flag state for downstream events. | Stored propagated flags. |
| Reporting/Analytics Projections | Read-side summaries. | Source-of-truth facts. |

### 5.4 Configuration artifacts

| Artifact | Layer | Deployer can | Deployer cannot |
|---|---|---|---|
| Activity definition | L0 Assembly | Select shapes, roles, scopes, patterns, schedules, policy parameters. | Define new platform primitives. |
| Shape definition | L1 Shape | Define payload schema and allowed field-level validation within shape model. | Replace `shape_ref` or self-describe payloads in events. |
| Logic rule | L2 Logic | Express bounded form warnings/visibility/defaults using expression language. | Cause side effects or execute arbitrary code. |
| Trigger definition | L3 Policy | Configure bounded server-side reactions/deadlines. | Run on device or recurse. |
| Pattern binding | L0 / L3b parameters | Select platform pattern and fill slots. | Author state machine. |
| Policy parameter | L3 bounded policy surface | Configure severity/uniqueness/sensitivity within fixed bounds. | Redefine flag mechanism or access logic. |
| Sensitivity classification | Policy surface | Mark shape/activity sensitivity. | Configure field-level sensitivity. |

### 5.5 Implementation concerns

These may be necessary to build the system but are not settled architecture primitives:

- database schema;
- table names;
- indexes;
- queues;
- cache invalidation;
- local storage partitioning;
- UI widgets;
- authoring-file syntax;
- sync pagination and batching;
- deployment tooling UI;
- projection materialization strategy;
- exact service boundaries;
- transport protocol details.

---

## 6. Identity Architecture

### 6.1 Shared reference protocol

All platform identity references use:

```txt
{type, id}
```

Settled identity categories:

```txt
subject
actor
process
assignment
```

### 6.2 Subject identity lifecycle

| Stage | Settled semantics |
|---|---|
| Created | Subject ID is client-generated and valid offline. |
| Referenced | Events reference subject through typed `subject_ref`. |
| Mutated | Attribute changes are events; identity stays stable. |
| Ambiguous duplicate | Candidate duplicate may be flagged; both identities remain until resolution. |
| Merged | `SubjectsMerged` maps `retired_id → surviving_id`; historical events are unchanged. |
| Retired by merge | Retired ID resolves in projection; no new references should target it after devices learn merge. |
| Split | `SubjectSplit` archives source and creates successors for future events. |
| Archived by split | Source is terminal; history remains frozen under source ID. |
| Stale referenced | Event is accepted and flagged, not rejected. |

### 6.3 Actor identity lifecycle

| Stage | Settled semantics |
|---|---|
| Provisioned | Actor identity is distinct from subject identity. |
| Referenced | Events carry actor reference separately from device. |
| Role changes | Role changes do not change actor identity. |
| Assignment changes | Authority changes through assignment timeline. |
| Device changes | Actor may use many devices; device identity remains hardware-bound. |
| System-authored | System events use auditable actor references such as `system:trigger/{trigger_id}` or `system:auto_resolution/{policy_id}`. |

### 6.4 Process identity lifecycle

| Stage | Settled semantics |
|---|---|
| Created | Process identity can be referenced as an operational process. |
| Referenced | Events may point to process identity through typed references. |
| Distinct from activity | Process identity is not `activity_ref`, activity definition, workflow pattern, or trigger. |
| Evolves | Process state is event/projection territory, not identity mutation. |

### 6.5 Assignment identity lifecycle

| Stage | Settled semantics |
|---|---|
| Created | Assignment is an identity category and an authorization grant. |
| Active | Binds actor, role, scope, and time. |
| Composed | Multiple active assignments can compose effective authority. |
| Changed | Assignment changes through events, especially `assignment_changed`. |
| Stale on device | Offline device may act under stale assignment; server accepts and flags. |
| Ended/expired | No longer grants current authority; past work remains auditable. |

### 6.6 Lineage rules

| Rule | Settled boundary |
|---|---|
| Merge is alias-in-projection. | Historical events are not re-referenced. |
| Unmerge is rejected. | Wrong merge is corrected through split. |
| Split freezes history. | Source is archived; successors receive future events. |
| Lineage graph is acyclic. | Active/archived lifecycle constraints prevent cycles. |
| Merge/split are online-only. | Server validates identity operations. |

---

## 7. Conflict Detection and Resolution Model

### 7.1 Detection pipeline

Conflict and flag detection runs after event acceptance and before downstream policy/action.

```txt
incoming event
  -> accepted into event store
  -> raw-reference checks
  -> causal/staleness checks
  -> authorization checks
  -> domain uniqueness checks
  -> workflow transition checks
  -> flag creation when needed
  -> projection / policy / trigger only if allowed by flag state
```

### 7.2 Flag category register

| Flag category | Source | Default handling / resolvability |
|---|---|---|
| `identity_conflict` | ADR-002 | `manual_only` |
| `concurrent_state_change` | ADR-002 | `manual_only` |
| `stale_reference` | ADR-002 | `auto_eligible` |
| `scope_violation` | ADR-003 | `manual_only` |
| `ScopeStaleFlag` | ADR-003 | informational default |
| `RoleStaleFlag` | ADR-003 | blocking for capability-restricted actions |
| `TemporalAuthorityExpiredFlag` | ADR-003 | informational default |
| `domain_uniqueness_violation` | ADR-004 | `manual_only` |
| `transition_violation` | ADR-005 | `auto_eligible` |

### 7.3 Flag semantics

| Dimension | Meaning |
|---|---|
| Category | What kind of anomaly was detected. |
| Severity | Whether downstream action is blocked or informational under policy. |
| Resolvability | Whether platform permits auto-resolution. |
| Resolver | Who may resolve this specific flag instance. |
| Source event | The root event that caused the flag. |

These dimensions are not interchangeable.

### 7.4 Resolution rules

- Every `ConflictDetected` designates exactly one resolver.
- Only the designated resolver's `ConflictResolved` is canonical.
- Unauthorized or competing resolutions are themselves handled as violations, not co-equal outcomes.
- `manual_only` flags require human judgment.
- `auto_eligible` flags may be resolved by bounded platform auto-resolution policy.
- Auto-resolution uses auditable system actor identity.

### 7.5 Detect-before-act

Settled rule:

```txt
Flag detection precedes downstream policy execution.
```

Consequences:

- flagged events do not trigger downstream L3 policy until allowed/resolved;
- unresolved transition-flagged events do not alter workflow state;
- source-chain projections can warn downstream work without stored flag propagation;
- resolution can later re-derive projections.

---

## 8. Authorization and Selective Sync Architecture

### 8.1 Assignment access rule

Recovered access rule:

```txt
access_allowed(actor, action, target) =
  actor has an active assignment
  whose scope contains the target
  and whose role permits the action
```

Role-action permission tables are configuration/platform-boundary territory, not an event-envelope feature.

### 8.2 Assignment grant shape

An assignment binds:

- actor;
- role;
- scope;
- temporal interval;
- optional operational process/activity context.

### 8.3 Scope types

Platform-fixed scope types:

```txt
geographic
subject_list
activity
```

Rules:

- Deployers select and compose within fixed scope types.
- Deployers do not define containment functions.
- Scope containment is platform-controlled.

### 8.4 Sync equals access

Settled rule:

```txt
sync_scope(actor/device) = access_scope(actor)
```

Consequences:

- if the device has data, it is because sync determined the actor/device was authorized to receive it;
- authorization and sync cannot drift into separate policies;
- scope expansion delivers newly authorized data;
- scope contraction removes or limits future access according to selective-retain strategy;
- stale offline work is accepted and flagged on sync.

### 8.5 Authority-as-projection

Authority is reconstructed from assignment history.

```txt
authority_at(event) = projection(assignment_timeline, actor_ref, subject_ref, event causal context)
```

The event does not assert its own authority. The event records who acted, what they acted on, and when/cause-context metadata sufficient for later reconstruction.

### 8.6 Scope-crossing alias rule

When subject aliasing crosses scopes:

```txt
authorization uses original subject_ref as written
```

Alias resolution for read models does not retroactively change whether the actor was authorized at creation time.

---

## 9. Configuration Boundary

### 9.1 Four-layer gradient

| Layer | Name | Deployer can do | Boundary |
|---|---|---|---|
| L0 | Assembly | Select activities, patterns, roles, scopes, schedules, policy parameters. | No new platform primitives. |
| L1 | Shape | Define payload schemas and versioned shape changes. | No embedded programs; no schema-free payloads. |
| L2 | Logic | Form-scoped warnings/visibility/defaults using bounded expressions. | No side effects. |
| L3 | Policy | Server-side bounded triggers, deadlines, uniqueness, severity, auto-resolution parameters. | No arbitrary code; L3 to code is platform boundary. |

### 9.2 Shape model

Rules:

- every event has `shape_ref`;
- `shape_ref` pins interpretation of payload forever;
- shapes are authored as deltas but stored as full snapshots;
- additive changes are normal;
- deprecation is default for removal-like changes;
- breaking changes require explicit migration/platform handling;
- historical events remain valid under their referenced shape version.

### 9.3 Activity model

Rules:

- `activity_ref` is optional;
- human-authored activity-context events are normally auto-populated by the device;
- null is valid when no activity context exists or historical import lacks provenance;
- activity definition can evolve without rewriting events;
- `activity_ref` is not `pattern_ref`.

### 9.4 Expression language

ADR-004 expression scopes:

```txt
payload.*
entity.*
event.*
```

ADR-005 adds closed read-only `context.*` values:

```txt
context.subject_state
context.subject_pattern
context.activity_stage
context.actor.role
context.actor.scope_name
context.days_since_last_event
context.event_count
```

Rules:

- operators and field references only;
- zero functions in ADR-004;
- no loops;
- no user-defined abstractions;
- no dynamic cross-subject queries;
- no side effects.

### 9.5 Trigger architecture

| Trigger class | Meaning | Boundary |
|---|---|---|
| event-reaction trigger (3a) | Server-side reaction during sync/ingestion. | At most one output per source event. |
| deadline-check trigger (3b) | Server-side scheduled/async deadline check. | Bounded, not recursive. |
| auto-resolution | ADR-005 L3b subtype for eligible flags. | Uses loop guards and system actor identity. |

Trigger constraints:

- server-only;
- no device-side trigger engine;
- trigger DAG max path length 2;
- no recursive trigger chains;
- output events are normal events with auditable `actor_ref`.

### 9.6 Complexity budgets

Settled initial budgets:

| Budget | Limit |
|---|---:|
| fields per shape | 60 |
| predicates per condition | 3 |
| triggers per event type | 5 |
| triggers per deployment | 50 |
| escalation depth | 2 levels |
| config versions on device | current + previous |

Budgets are initial strategy / platform validation policy. They can evolve without changing historical events.

### 9.7 Sensitivity boundary

Sensitivity levels:

```txt
standard
elevated
restricted
```

Rules:

- sensitivity is shape/activity-level;
- field-level sensitivity is rejected at ADR-004 boundary;
- encryption, erasure, redaction, and de-identification are future platform evolution, not recovered settled behavior.

---

## 10. State Progression and Workflow Architecture

### 10.1 State rule

```txt
state = projection(event_stream, pattern_definition, config_version, flag_status)
```

State is:

- derived;
- rebuildable;
- not stored in events;
- not an envelope field;
- not a reason to reject offline work.

### 10.2 Pattern Registry

The Pattern Registry owns:

- state machine skeletons;
- participant role declarations;
- parameterization points;
- valid transition rules;
- projection specification;
- transition validity inputs for conflict detection.

It does not own:

- deployer-authored state machines;
- arbitrary transition code;
- stored current state;
- custom envelope fields;
- exact forever-frozen initial inventory.

### 10.3 Composition rules

| Rule | Settled meaning | Enforcement |
|---|---|---|
| One subject-level pattern per activity | An activity binds at most one lifecycle state machine to a subject. | Deploy-time validation. |
| Event-level patterns compose freely | Review/approval states attach to specific events. | Projection Engine. |
| Approval sub-flows embed | Approval is scoped to a source/submission event. | Config + projection. |
| Cross-activity linking uses `activity_ref` | Patterns do not secretly span activities. | Envelope/payload refs. |
| Shape-to-pattern mapping unique within activity | No two patterns claim same shape within one activity. | Deploy-time validation. |

### 10.4 Transition violation

A `transition_violation` occurs when an event implies a state transition invalid under the applicable pattern.

Pipeline:

```txt
event accepted
  -> pattern resolved from activity + shape mapping
  -> current projected state read
  -> transition validity checked
  -> invalid transition creates transition_violation flag
  -> event remains visible
  -> event excluded from workflow state until resolved
```

### 10.5 Source-chain model

Rules:

- downstream events may reference source events through `source_event_ref`;
- only root-cause event receives stored flag;
- downstream warnings are computed by source-chain traversal;
- resolving the root flag clears computed downstream warnings;
- stored flag propagation is rejected.

### 10.6 Auto-resolution

Rules:

- auto-resolution is an L3b subtype;
- only `auto_eligible` categories can be targeted;
- `manual_only` categories require human resolution;
- system actor format is `system:auto_resolution/{policy_id}`;
- loop guards prevent recursive/unbounded behavior;
- auto-resolution and manual resolution are mutually exclusive per flag instance once canonical resolution exists.

---

## 11. Cross-Primitive Interaction Flows

### 11.1 Basic capture path

```txt
1. Config package delivered atomically to device.
2. Actor opens activity/form.
3. Device loads shape, L2 logic, activity/pattern context, and local assignment projection.
4. Device performs local role/scope checks and advisory validation.
5. Device writes immutable event locally.
6. Event carries the 11-field envelope.
7. Event syncs to server.
8. Server accepts event.
9. Conflict Detector evaluates raw refs, causal order, authorization, domain uniqueness, transition validity.
10. If clean, projections update and eligible triggers may run.
11. If flagged, detect-before-act blocks downstream action according to flag state/severity.
12. Sync Scope Resolver sends authorized deltas to devices.
```

### 11.2 Merge path

```txt
1. Server-authorized actor issues merge.
2. Subject Identity Resolver validates active operands and acyclic lineage.
3. SubjectsMerged event records retired_id -> surviving_id.
4. Historical events remain unchanged.
5. Alias Table Projection resolves retired_id for read models.
6. Conflict Detector still evaluates future stale events using raw refs.
7. Authorization uses original subject_ref where aliasing crosses scope.
```

### 11.3 Split path

```txt
1. Server-authorized actor issues corrective split.
2. Subject Identity Resolver archives source permanently.
3. Successor subject IDs receive future events.
4. Historical events remain under source_id.
5. Source archive is terminal.
6. Projection can show source history and successor futures without rewriting events.
```

### 11.4 Scope change path

```txt
1. assignment_changed event updates assignment timeline.
2. Authority Projection derives new actor authority.
3. Sync Scope Resolver expands or contracts authorized dataset.
4. Device may still create offline events under stale assignment.
5. Server accepts and flags stale-authority work.
6. Selective-retain governs local data after contraction.
```

### 11.5 Configuration update path

```txt
1. Deployer changes config within platform bounds.
2. Config Package Validator checks dependencies, budgets, version coexistence, trigger DAG, scope rules, and pattern mappings.
3. Atomic config package is delivered on sync.
4. Device keeps current + previous config for in-progress work.
5. Events keep the shape_ref active at capture time.
6. Projections interpret historical events against referenced shape versions.
```

### 11.6 Workflow transition path

```txt
1. Event arrives with shape_ref and optional activity_ref.
2. Projection Engine resolves applicable pattern via activity + shape mapping.
3. Conflict Detector checks implied transition validity.
4. Valid event can participate in workflow state projection.
5. Invalid event creates transition_violation flag.
6. Flagged event remains visible but excluded from derived state.
7. Resolution later determines whether projection includes it.
```

### 11.7 Source-chain path

```txt
1. Source event creates downstream task/review/alert through workflow or trigger.
2. Downstream event references source via source_event_ref.
3. If source is later flagged, downstream events are not copied with flags.
4. Source Chain Projection walks references and surfaces computed upstream-warning state.
5. Resolving root flag clears computed downstream warnings.
```

### 11.8 Auto-resolution path

```txt
1. Flag category is auto_eligible.
2. L3b auto-resolution policy evaluates bounded condition.
3. Server writes resolution event as system:auto_resolution/{policy_id}.
4. Loop guards prevent recursion.
5. Manual-only flags remain outside auto-resolution.
```

---

## 12. Decision Boundary Map

### 12.1 Structural constraints

| Area | Structural commitments |
|---|---|
| Storage | Append-only writes; event as atomic write; client UUIDs; event sync unit; base envelope. |
| Identity/conflict | Typed refs; causal fields; advisory device time; hardware device ID; merge/split lineage; raw-reference detection; accept-and-flag. |
| Authorization/sync | Assignment-based access; sync scope equals access scope; authority not in envelope; alias-respects-original-scope. |
| Configuration/event contract | `shape_ref`; `activity_ref`; fixed event type vocabulary. |
| Workflow | No new structural event field or type added by ADR-005. |

### 12.2 Strategy-protecting constraints

| Area | Strategy-protecting commitments |
|---|---|
| Identity/conflict | Online-only merge/split; single-writer conflict resolution; detect-before-act. |
| Authorization | Scope-containment invariant; online-only conflict resolution; authorization flags under detect-before-act. |
| Configuration | System actor format; server-only triggers; atomic config delivery; no deployer auth logic; fixed scope types; no field-level sensitivity. |
| Workflow | `transition_violation`; flagged-event exclusion; platform-level flag resolvability. |

### 12.3 Initial strategies

| Area | Initial strategies |
|---|---|
| Authorization | Tiered projection; auth staleness flags; selective-retain. |
| Configuration | Four-layer gradient; shape authoring/storage; expression language; trigger architecture; budgets; policy parameters. |
| Workflow | Projection-derived state machines; Command Validator; Pattern Registry; composition rules; source-only flagging; `context.*`; auto-resolution. |

---

## 13. Negative Boundary Register

Rejected alternatives remain useful only as boundary evidence.

| Rejected / excluded path | Final boundary |
|---|---|
| Mutable-in-place records | All writes append new events. |
| Physical re-reference after merge | Merge is alias-in-projection. |
| `SubjectsUnmerged` | Wrong merge correction is `SubjectSplit`. |
| Device-time structural ordering | `device_time` is advisory. |
| Account-bound device identity | `device_id` is hardware-bound. |
| Authority context in envelope | Authority is projection-derived. |
| Single or variable assignment refs in envelope | Assignment timeline reconstructs authority. |
| Sync independent of access | Sync scope equals access scope. |
| Deployer-authored event types | Event `type` is platform-fixed. |
| Self-describing payloads replacing `shape_ref` | Historical schema is referenced through `shape_ref`. |
| Mandatory `activity_ref` for all events | Optional, system-populated when context exists. |
| Deployer-authored access logic | Scope types and containment are platform-fixed. |
| Field-level sensitivity | Sensitivity is shape/activity-level. |
| Device-side triggers | Triggers are server-only. |
| Recursive trigger chains | Trigger DAG max path length 2. |
| Deployer-authored state machines | Pattern Registry is platform-fixed. |
| Stored `current_state` | State is projection-derived. |
| `status_changed` as ADR-005 type | Existing event types + shapes/patterns express state progression. |
| `pattern_ref` envelope field | Pattern derives from activity + shape mapping. |
| Dynamic `context.*` queries | `context.*` is closed, read-only, pre-resolved. |
| Flag propagation downstream | Source-only flagging + source-chain projection. |
| Auto-resolution for manual-only flags | Human judgment required. |

---

## 14. Open Evolution Register

### 14.1 Platform evolution that does not violate accepted decisions

| Front                                   | Allowed direction                                                                   |
| --------------------------------------- | ----------------------------------------------------------------------------------- |
| New structural event type               | Only if new platform processing behavior is proven; vocabulary remains append-only. |
| New platform pattern                    | Platform ships it; deployers select/parameterize it.                                |
| Additional `context.*` value            | Platform-fixed, read-only, pre-resolved, append-only extension.                     |
| Additional auto-resolution policy       | Within L3b guardrails and only for eligible categories.                             |
| Regulatory encryption/redaction/erasure | Separate platform mechanisms; must not silently redefine event immutability.        |
| Multi-tenant naming                     | Preserve `shape_ref` / `activity_ref` parse safety and historical interpretation.   |
| Complexity budget changes               | Platform validation evolution; historical events unchanged.                         |

### 14.2 Expansion of explicitly open fronts

| Front | Classification |
|---|---|
| Exact Pattern Registry inventory | Open platform specification. |
| Pattern migration mechanics | Platform evolution / implementation strategy. |
| Flag queue ergonomics and source-chain visualization | Platform evolution. |
| Domain conflict resolution strategies | Bounded by ADR-004/ADR-005; cannot bypass manual-only categories. |
| Priority sync / pagination / backfill | Sync implementation strategy. |

### 14.3 Underexplored or not yet settled

| Front | Why not settled |
|---|---|
| Actor-as-subject delivery rule | Excluded as settled ADR-003 structure; may later be sync filter or scope evolution. |
| Auditor/query access | Requires separate access model work. |
| Shared-device storage partitioning | Implementation/security design. |
| Exact role-action table artifact | Role permits action is required; exact artifact shape is not recovered as standalone primitive. |
| Exact config authoring syntax | Authoring format is implementation/tooling. |

---

## 15. S00 Simplicity Check

The simplest case remains simple.

Minimum configuration:

```txt
shape: household_observation/v1
activity: household_survey
pattern: capture_only
assignment: field_worker + geographic scope
```

Resulting event:

```txt
type = capture
shape_ref = household_observation/v1
activity_ref = household_survey or null depending on capture context
subject_ref = target subject
actor_ref = field worker
device_id/device_seq/sync_watermark/timestamp = device envelope metadata
payload = observed fields
```

S00 does not require:

- custom event type;
- custom access-control code;
- custom trigger;
- state-machine authoring;
- `pattern_ref`;
- authority context field;
- field-level sensitivity;
- auto-resolution policy;
- workflow flag propagation.

The full architecture does not leak into the basic capture case.

---

## 16. Implementation Guardrails

Implementation work should preserve these boundaries:

1. Do not add event-envelope fields without an ADR-level structural decision.
2. Do not store current workflow state as authoritative truth.
3. Do not evaluate authorization from event-authored authority assertions.
4. Do not resolve aliases before conflict detection.
5. Do not fire triggers before flag detection gates downstream action.
6. Do not let deployer configuration become arbitrary code.
7. Do not add scope containment functions authored by deployers.
8. Do not propagate stored flags downstream when source-chain projection suffices.
9. Do not convert examples of patterns into frozen architecture inventory.
10. Do not treat database/service/API shape as architecture unless mapped to a settled primitive boundary.

---

## 17. Source Index

| Source | Role in this map |
|---|---|
| `001-recovery-strategy.md` | Defines final-output scope, contamination controls, and success criteria. |
| `002-phase0-decision-register.md` | Verification anchor for ADR-001 through ADR-005 sub-decisions, final event envelope, flag register, and classification key. |
| `003-phase1-adr2-identity-conflict-recovery.md` | Identity, conflict, causal ordering, accept-and-flag, raw-reference, merge/split recovery. |
| `004-phase2-adr3-auth-sync-recovery.md` | Assignment access, sync scope, authority projection, authorization flags, selective-retain recovery. |
| `005-phase3-adr4-config-boundary-recovery.md` | Event type vocabulary, `shape_ref`, `activity_ref`, configuration gradient, triggers, scope/sensitivity recovery. |
| `006-phase4-adr5-state-progression-recovery.md` | Workflow/state, Pattern Registry, transition flags, `context.*`, source-chain, auto-resolution recovery. |
| `007-phase5-cross-lineage-vocabulary.md` | Immediate source for consolidated vocabulary, term collisions, primitive taxonomy, boundary map, and interaction model. |

---

## 18. Output Closure

This architecture map is authoritative as a vocabulary and boundary reference because it satisfies the recovery success criteria:

1. Terms trace to ADR sub-decisions or recovered phase files anchored by those sub-decisions.
2. Rejected alternatives are retained only as negative boundary evidence.
3. Primitive taxonomy follows structural / strategy-protecting / initial strategy classification.
4. Interaction flows are derived from ADR composition rules and recovered phase outputs.
5. Implementation details remain outside settled architecture unless they map to a primitive boundary.
6. S00 remains simple.

The recovered platform architecture can be summarized as:

```txt
Write immutable events.
Reference identities explicitly.
Detect conflicts before action.
Authorize through assignment projections.
Configure within bounded layers.
Derive state through projections and patterns.
Keep implementation details below the architecture boundary.
```
