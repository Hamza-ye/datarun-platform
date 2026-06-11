# Phase 5: Cross-Lineage Vocabulary Consolidation

## Context Capsule

- Recovery mode: archaeological reconstruction only; no redesign, no alternatives, no reopening.
- Anchor file: `002-phase0-decision-register.md`.
- Upstream recovered sources:
  - `003-phase1-adr2-identity-conflict-recovery.md`
  - `004-phase2-adr3-auth-sync-recovery.md`
  - `005-phase3-adr4-config-boundary-recovery.md`
  - `006-phase4-adr5-state-progression-recovery.md`
- Revised phase target:
  - Original Phase 4 was cross-lineage vocabulary consolidation.
  - ADR-005 was inserted as a dedicated recovery pass.
  - This file is therefore revised Phase 5.
- Inputs used:
  - Phase 0 decision register: ADR-001 through ADR-005 sub-decision anchors.
  - Phase 1 ADR-002 recovery: identity + conflict vocabulary.
  - Phase 2 ADR-003 recovery: authorization + selective sync vocabulary.
  - Phase 3 ADR-004 recovery: configuration boundary vocabulary.
  - Phase 4 ADR-005 recovery: workflow/state progression vocabulary.
- Settled outputs:
  - Unified platform vocabulary across ADR-001 through ADR-005.
  - Term-collision resolution table.
  - Primitive taxonomy: structural contracts, runtime services, projections, configuration artifacts, policies, and implementation concerns.
  - Primitive boundary map.
  - Cross-primitive interaction model.
  - Decision-boundary classification map.
- Rejected / excluded:
  - Any term that appears only in exploration without ADR sub-decision support.
  - Rejected alternatives from prior passes, including authority context in envelope, unmerge, deployer-authored structural types, deployer-authored state machines, field-level sensitivity, trigger recursion, and `status_changed` as ADR-005 structural type.
  - Implementation mechanisms such as database tables, queues, caches, UI widgets, authoring-file formats, storage partitioning, and index strategies.
- Deferred / open evolution:
  - Exact Pattern Registry inventory and skeleton definitions.
  - Additional platform-fixed pattern types.
  - Additional `context.*` values.
  - Additional auto-resolution policies within ADR-005 guardrails.
  - Richer source-chain visualization and flag queue ergonomics.
  - Priority sync, pagination, backfill, regulatory encryption/redaction/erasure, and multi-tenant naming strategy.
- Terms locked in this pass:
  - No new architecture terms are introduced as settled primitives.
  - This pass locks consolidation labels only: `structural contract`, `runtime service`, `projection/read model`, `configuration artifact`, `policy surface`, `implementation concern`, and `term collision`.
  - These are recovery-document labels used to organize ADR vocabulary; they are not new platform runtime primitives.

---

## 1. Consolidation Checkpoint

The consolidation pass is bounded by two rules from the recovery strategy:

1. Every term must trace to a specific ADR sub-decision.
2. Cross-lineage merging happens only after independent recovery for ADR-002 through ADR-005.

The revised input set is:

| Input | Recovery scope | Consolidation role |
|---|---|---|
| `002-phase0-decision-register.md` | ADR-001 through ADR-005 sub-decision anchors. | Verification gate. |
| `003-phase1-adr2-identity-conflict-recovery.md` | Identity, conflict, causal ordering, accept-and-flag, merge/split. | Identity/conflict vocabulary. |
| `004-phase2-adr3-auth-sync-recovery.md` | Assignment-based access, sync scope, authority projection, auth flags. | Authorization/sync vocabulary. |
| `005-phase3-adr4-config-boundary-recovery.md` | Event type vocabulary, shapes, activities, config gradient, triggers, scope types, sensitivity. | Configuration/platform boundary vocabulary. |
| `006-phase4-adr5-state-progression-recovery.md` | State progression, pattern registry, transition flags, source-chain traversal, `context.*`, auto-resolution. | Workflow/state vocabulary. |

This file does not return to exploration sources except through the recovered phase files. The phase files already performed the ADR-anchored extraction and rejection filtering.

---

## 2. Consolidation Rules

### 2.1 First verified occurrence wins

When a term is introduced by an earlier ADR and refined by a later ADR, the earlier meaning remains the base meaning.

Example:

```txt
assignment
  ADR-002: typed identity category
  ADR-003: atomic authorization grant

Consolidated meaning:
  Assignment is a typed identity category whose settled ADR-003 role is an authorization grant binding actor, role, scope, and time.
```

ADR-003 refines the term; it does not replace ADR-002.

### 2.2 Later ADRs may extend but not redefine upstream contracts

Examples:

| Upstream term | Later extension | Consolidated rule |
|---|---|---|
| `event` | Shapes, activity, triggers, workflow state. | Event remains immutable source-of-truth record. Later layers interpret or create events; they do not make events mutable. |
| `accept-and-flag` | Auth flags, domain uniqueness, transition violations. | Later flag types reuse the mechanism. They do not introduce rejection for stale state. |
| `detect-before-act` | Flag severity, triggers, workflow state derivation. | Flagged events do not trigger downstream policies and do not alter derived state until resolved. |
| `authority-as-projection` | Activity, sensitivity, pattern roles, `context.actor.*`. | Later configuration can inform authority projection, but authority stays out of the envelope. |
| `shape_ref` | Shape-to-pattern mapping. | Pattern derivation uses shape/activity configuration; no `pattern_ref` envelope field is added. |

### 2.3 Rejected alternatives remain negative boundary evidence

Rejected alternatives are retained only as boundary markers.

They do not become primitives, contracts, or configuration capabilities.

Examples:

| Rejected path | Consolidated boundary |
|---|---|
| `SubjectsUnmerged` | Wrong merge correction is `SubjectSplit`; merge is not symmetric. |
| Authority context in event envelope | Authority is reconstructed from assignment timeline. |
| Deployer-authored event types | Event `type` remains platform-fixed and processing-behavior oriented. |
| Deployer-authored scope containment functions | Scope types and containment remain platform-fixed. |
| Field-level sensitivity | Sensitivity classification is shape/activity-level. |
| Deployer-authored state machines | Patterns are platform-fixed skeletons selected and parameterized at L0. |
| `status_changed` as ADR-005 type | State progression is projection-derived from existing event types. |

### 2.4 Classification controls the vocabulary boundary

Each term is classified by the highest permanence level it participates in:

| Classification              | Meaning in this consolidation                                                                                                                         |
| --------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| Structural contract         | Stored-event or protocol contract. Changing it requires migration, permanent dual semantics, or broad protocol change.                                |
| Strategy-protecting service | Runtime rule that guards a structural invariant or prevents configuration from crossing into unsafe behavior. Usually server-side or validation-side. |
| Initial strategy            | Current architecture position that can evolve through code/config validation changes without migrating historical events.                             |
| Configuration artifact      | Deployer-authored or deployer-parameterized artifact within platform-fixed bounds.                                                                    |
| Projection/read model       | Rebuildable derived state from events + config + flags.                                                                                               |
| Implementation concern      | Mechanism that may matter to build quality but is not settled architecture vocabulary.                                                                |

---

## 3. Unified Architectural Closure

The recovered architecture has one central invariant:

```txt
Events are the durable facts.
Projections derive meaning.
Configuration bounds what deployers may express.
Runtime services guard invariants before downstream action.
```

Expanded:

1. ADR-001 locks immutable events, append-only writes, client-generated UUIDs, event-as-sync-unit, and rebuildable projections.
2. ADR-002 locks typed identity references, causal ordering, raw-reference conflict detection, accept-and-flag, and subject lineage semantics.
3. ADR-003 locks assignment-based access, sync scope as access scope, authority-as-projection, and authorization flag handling.
4. ADR-004 locks the platform/deployment boundary through `shape_ref`, `activity_ref`, fixed event types, four-layer configuration gradient, server-only triggers, fixed scope types, and bounded sensitivity.
5. ADR-005 locks state progression as projection-derived pattern behavior, adds transition flags, source-chain traversal, bounded `context.*`, and auto-resolution guardrails.

Nothing in ADR-002 through ADR-005 changes the ADR-001 source-of-truth rule.

---

## 4. Event Contract Consolidation

### 4.1 Final event envelope

The recovered final event envelope is:

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

### 4.2 Field ownership map

| Field | Consolidated meaning | ADR anchor |
|---|---|---|
| `id` | Client-generated UUID for the event. | ADR-001 S3 |
| `type` | Platform-fixed structural event type; processing behavior, not domain meaning. | ADR-001 S5 / ADR-004 S3 |
| `shape_ref` | Mandatory payload-schema reference, format `{shape_name}/v{version}`. | ADR-004 S1 |
| `activity_ref` | Optional activity instance identifier. | ADR-004 S2 |
| `subject_ref` | Typed reference to the subject the event is about. | ADR-002 S2 |
| `actor_ref` | Typed reference to human actor or system actor. | ADR-004 S4 |
| `device_id` | Hardware-bound device identity. | ADR-002 S1/S5 |
| `device_seq` | Durable monotonic per-device event sequence. | ADR-002 S1/S4 |
| `sync_watermark` | Last known server sync position at event creation. | ADR-002 S1/S4 |
| `timestamp` | Advisory device time / human-facing time. | ADR-002 S3 |
| `payload` | Shape-validated event data. | ADR-001 S5 / ADR-004 S10 |

### 4.3 Non-fields deliberately excluded

| Non-field | Reason excluded |
|---|---|
| `authority_context` | Authority is projection-derived from assignment timeline. |
| `assignment_ref` / `assignment_refs` | Rejected with authority-context envelope designs. |
| `pattern_ref` | Pattern participation is derived from activity + shape configuration. |
| `current_state` | State is projection-derived, never stored in event. |
| `status_changed` | Not added as a structural event type by ADR-005. |
| Field-level sensitivity markers | Sensitivity is shape/activity-level. |

---

## 5. Event Type Vocabulary Consolidation

### 5.1 Settled event type set

```txt
type ∈ {
  capture,
  review,
  alert,
  task_created,
  task_completed,
  assignment_changed
}
```

### 5.2 Type semantics

| Type | Platform meaning | Boundary |
|---|---|---|
| `capture` | Records structured data under a shape. | Domain action names such as case opened, stock received, facility updated are shapes/activities, not event types. |
| `review` | Records review/assessment/approval of a source event or work item. | Review state is event-level workflow state, not a new structural type. |
| `alert` | Records system/human attention signal. | Alert is structural behavior; domain alert content is payload/shape. |
| `task_created` | Creates a work item/deadline. | Trigger-created tasks use standard system actor identity. |
| `task_completed` | Completes a work item/deadline. | Completion affects projections and patterns. |
| `assignment_changed` | Changes authority/scope assignment state. | Does not carry arbitrary domain payload semantics. |

### 5.3 New type rule

A new structural event type is justified only when the platform processing behavior differs.

If the difference is domain meaning, use:

```txt
capture + shape_ref + activity_ref
```

If the difference is workflow state, use:

```txt
existing event type + shape/activity mapping + projection pattern
```

---

## 6. Unified Identity Vocabulary

### 6.1 Typed identity reference

All identity references use:

```txt
{type, id}
```

The four settled identity categories are:

```txt
subject
actor
process
assignment
```

### 6.2 Identity categories

| Category | Consolidated definition | Primary lifecycle owner | Boundary |
|---|---|---|---|
| `subject` | Real-world thing the event is about. | Subject Registry / Subject Identity Resolver. | Owns lineage, alias, split/archive semantics; does not own auth or workflow state. |
| `actor` | Human or system performer/author of work. | Actor registry / system actor protocol. | Actor identity is not authority. Authority comes from assignment timeline. |
| `process` | Operational process instance or chain referenced by events. | Process/activity/workflow projections. | Not identical to `activity_ref`; process is identity, activity is configuration/context. |
| `assignment` | Identity category that ADR-003 refines into the atomic authorization grant. | Assignment Registry / Assignment Timeline. | Assignment is not a role alone and not an envelope authority assertion. |

### 6.3 Subject lineage vocabulary

| Term | Definition | ADR anchor |
|---|---|---|
| `SubjectsMerged` | Event that maps `retired_id → surviving_id`. | ADR-002 S6 |
| `retired_id` | Subject ID retired by merge but preserved in history. | ADR-002 S6 |
| `surviving_id` | Active subject ID that projections resolve to after merge. | ADR-002 S6 |
| `alias mapping` | Projection rule resolving retired IDs to surviving IDs. | ADR-002 S6/S13 |
| `SubjectSplit` | Corrective event that archives source and creates successors. | ADR-002 S7/S8 |
| `successor` | New subject ID receiving future events after split. | ADR-002 S8 |
| `active` | Subject lifecycle state eligible for merge/split participation. | ADR-002 S9 |
| `archived` | Terminal subject lifecycle state after split. | ADR-002 S8/S9 |
| `lineage DAG` | Acyclic subject lineage graph enforced by validation. | ADR-002 S9 |

### 6.4 Identity resolution rule

```txt
Conflict detection uses raw references.
Projection may resolve aliases afterward.
Authorization uses original subject_ref where aliasing exists.
```

This combines:

- ADR-002 raw-reference detection;
- ADR-002 alias-in-projection;
- ADR-003 alias-respects-original-scope.

---

## 7. Conflict and Flag Vocabulary Consolidation

### 7.1 Conflict mechanism

The base conflict mechanism is:

```txt
accept event
run detection before policy execution
attach/create flag if anomaly found
block downstream action if flag semantics require it
resolve through designated pathway
rebuild projections
```

### 7.2 Settled conflict/flag categories

| Category | Source lineage | Consolidated meaning | Default resolvability / handling |
|---|---|---|---|
| `identity_conflict` / duplicate identity | ADR-002 | Multiple subject IDs plausibly represent same real-world entity. | Manual review; merge if true duplicate. |
| `concurrent_state_change` | ADR-002 | Causally concurrent incompatible changes for same subject/state. | Manual-only; designated resolver. |
| `stale_reference` | ADR-002 | Event references stale subject/state relative to sync watermark. | Auto-eligible where bounded policy applies. |
| `scope_violation` | ADR-003 | Actor performed work outside valid scope/authority. | Manual-only. |
| `ScopeStaleFlag` | ADR-003 | Device acted under stale scope information. | Severity policy; often informational/default. |
| `RoleStaleFlag` | ADR-003 | Device acted under stale role/capability. | Severity policy; blocking for capability-restricted cases. |
| `TemporalAuthorityExpiredFlag` | ADR-003 | Actor operated after assignment authority expired. | Severity policy; often informational/default. |
| `domain_uniqueness_violation` | ADR-004 | Deployer-configured shape/activity uniqueness rule violated. | Manual-only in recovered initial classification. |
| `transition_violation` | ADR-005 | Event implies invalid workflow state transition under pattern. | Auto-eligible, with bounded policy. |

### 7.3 Dimensions that must not be collapsed

A flag has several independent dimensions:

| Dimension | Examples | Owner |
|---|---|---|
| Category | `stale_reference`, `transition_violation`, `scope_violation`. | Platform vocabulary / configured policy boundary. |
| Severity | informational, blocking, deployment-level severity map. | ADR-004 deployer-parameterized policy. |
| Resolvability | `auto_eligible`, `manual_only`. | ADR-005 platform-level classification. |
| Source event | Root event causing the flag. | ADR-002 / ADR-005 source-chain model. |
| Resolver | Designated human/system resolver. | ADR-002 single-writer / ADR-003 resolver authority / ADR-005 auto-resolution. |

Do not treat severity and resolvability as the same thing.

A flag may be informational but manual-only, or blocking but auto-eligible, depending on category and policy.

### 7.4 Detect-before-act consolidation

```txt
No downstream policy execution from unresolved flagged events.
No workflow state derivation from unresolved flagged events.
```

ADR-002 establishes the first rule.

ADR-005 extends the same uncertainty boundary to projection-derived workflow state.

---

## 8. Authorization and Sync Vocabulary Consolidation

### 8.1 Assignment-based access

Consolidated rule:

```txt
access_allowed(actor, action, target) =
  actor has an active assignment
  whose scope contains the target
  and whose role permits the action
```

### 8.2 Assignment grant semantics

An assignment binds:

- actor;
- role;
- scope;
- temporal interval;
- optional operational context.

It is an identity category from ADR-002 and an authorization primitive from ADR-003.

### 8.3 Authority-as-projection

Authority is reconstructed from:

```txt
assignment timeline
+ actor identity
+ target subject/activity/scope
+ event context
+ config-defined role/action/scope policy within platform bounds
```

Authority is not an envelope assertion.

### 8.4 Sync scope

```txt
sync scope = access scope
```

The device receives exactly the data the actor is authorized to hold offline.

Sync is therefore not an independent replication feature. It is the materialization of access control onto offline devices.

### 8.5 Scope vocabulary

| Scope term | Meaning | Boundary |
|---|---|---|
| `geographic` | Platform-fixed geographic containment. | Deployer does not write containment code. |
| `subject_list` | Explicit list/set of subjects. | Platform-defined membership semantics. |
| `activity` | Activity-scoped authority/access. | Activity is both config context and scope type name; disambiguate by context. |
| scope composition | Combination of platform-fixed scope types. | Deployer parameterizes, platform evaluates. |
| scope contraction | Actor loses access to previously held data. | Selective-retain strategy applies; implementation details are not architecture. |

### 8.6 Sensitivity vocabulary

| Term | Meaning | Boundary |
|---|---|---|
| `standard` | Baseline sensitivity. | Shape/activity-level. |
| `elevated` | Higher handling requirement. | Shape/activity-level. |
| `restricted` | Strictest platform-set sensitivity class. | Shape/activity-level. |
| field-level sensitivity | Rejected in ADR-004. | Regulatory mechanisms may evolve separately. |

---

## 9. Configuration Vocabulary Consolidation

### 9.1 Four-layer gradient

| Layer | Name | Owns | Boundary |
|---|---|---|---|
| L0 | Assembly | Activities, pattern selection, role/scope/schedule/policy parameterization. | Selects platform-provided structures; does not author runtime engines. |
| L1 | Shape | Payload schema, field definitions, versioning, shape-local validation/uniqueness. | Defines data shape, not workflow engine. |
| L2 | Logic | Form warnings, conditional visibility, field references, pre-resolved context. | No side effects, no functions, no dynamic cross-entity queries. |
| L3 | Policy | Server-side triggers, deadlines, auto-resolution, domain uniqueness/severity policy. | Bounded server behavior; no arbitrary code. |
| Code | Platform evolution | New primitives, new event types, new patterns, new engines. | Requires platform development discipline. |

### 9.2 Shape vocabulary

| Term | Definition | Boundary |
|---|---|---|
| `shape` | Typed payload schema. | Not an event, not a workflow state. |
| `shape_ref` | Mandatory envelope reference to shape version. | Stored forever in events. |
| `shape version` | Version used to interpret historical payloads. | Old versions remain readable. |
| `shape registry` | Registry used by devices, validation, projection. | Storage/sync implementation can evolve. |
| `shape evolution` | Additive/deprecation/default lifecycle. | Breaking changes are exceptional and explicit. |
| `breaking change` | Remove/rename/change type of field or other incompatible schema change. | Requires migration/acknowledgment mechanics, not silent mutation. |

### 9.3 Activity vocabulary

| Term | Definition | Boundary |
|---|---|---|
| `activity_ref` | Optional event reference to activity instance. | ID-only context, not full activity definition. |
| `activity instance` | Deployer-configured operational unit such as campaign or routine program. | Definition can evolve; historical events keep ID. |
| `activity` scope type | Platform-fixed scope type. | Do not confuse with `activity_ref` field. |

### 9.4 Expression vocabulary

| Term | Definition | Boundary |
|---|---|---|
| expression language | Operators + field references; zero functions in ADR-004. | Not a query language or programming language. |
| `payload.*` | Current event payload fields. | ADR-004. |
| `entity.*` | Projected entity/subject values. | ADR-004 bounded scope. |
| `event.*` | Current event metadata. | ADR-004. |
| `context.*` | ADR-005 pre-resolved form-context values. | Closed, platform-fixed, read-only. |

### 9.5 `context.*` values

```txt
context.subject_state
context.subject_pattern
context.activity_stage
context.actor.role
context.actor.scope_name
context.days_since_last_event
context.event_count
```

These are read-only values resolved before form evaluation.

They do not allow dynamic cross-subject or cross-event queries.

### 9.6 Trigger vocabulary

| Term | Definition | Boundary |
|---|---|---|
| event-reaction trigger (3a) | Server-side reaction during sync/ingestion; at most one output per source event. | No device-side trigger engine. |
| deadline-check trigger (3b) | Server-side async/scheduled check. | Bounded, not recursive. |
| trigger DAG | Trigger dependency graph. | Max path length 2. |
| system trigger actor | `system:trigger/{trigger_id}`. | Auditable system actor identity. |
| auto-resolution | ADR-005 L3b subtype. | Uses `system:auto_resolution/{policy_id}` and loop guards. |

---

## 10. Workflow / State Progression Vocabulary Consolidation

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

The Pattern Registry is the platform-fixed vocabulary of workflow skeletons.

It owns:

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
- custom envelope fields.

### 10.3 Pattern composition rules

| Rule | Consolidated wording |
|---|---|
| One subject-level pattern per activity | An activity binds at most one subject-level lifecycle state machine to a subject. |
| Event-level patterns compose freely | Review/approval states attach to individual events without competing with subject lifecycle state. |
| Approval sub-flows embed | Approval is scoped to a submission/source event, not a competing subject lifecycle. |
| Cross-activity linking uses `activity_ref` | Patterns do not secretly span activities. |
| Shape-to-pattern mapping unique within activity | No two patterns in same activity claim same shape. |

### 10.4 Source-chain vocabulary

| Term | Meaning |
|---|---|
| `source_event_ref` | Reference from downstream event to source event. |
| source-only flagging | Only the root-cause event is flagged. |
| source-chain traversal | Projection walks source references to surface upstream flag state. |

---

## 11. Primitive Taxonomy

This taxonomy is the main output of consolidation.

It classifies recovered vocabulary by architectural role.

### 11.1 Structural contracts

Structural contracts are stored-event or protocol-level commitments.

| Primitive / contract | Owns | Does not own | ADR anchors |
|---|---|---|---|
| Event Store | Append-only immutable facts and write-path discipline. | Current state, policy decisions, read models. | ADR-001 S1/S2 |
| Event Envelope | Durable fields required to interpret/sync/project events. | Authority context, pattern ref, current state. | ADR-001 S5; ADR-002; ADR-004 |
| Event Type Vocabulary | Platform-fixed structural processing types. | Domain event names. | ADR-004 S3 |
| Typed Identity Reference | `{type,id}` identity pointer protocol. | Per-category lifecycle semantics beyond identity. | ADR-002 S2 |
| Causal Ordering Contract | `device_id`, `device_sequence`, `sync_watermark`, advisory `device_time`. | Global total order across devices. | ADR-002 S1/S3/S4/S5 |
| Subject Lineage Contract | Merge aliasing, split archival, lineage DAG. | Physical rewriting or symmetric unmerge. | ADR-002 S6-S9 |
| Shape Reference Contract | Mandatory `shape_ref` format and historical schema interpretation. | Shape authoring UI/storage mechanics. | ADR-004 S1/S10 |
| Activity Reference Contract | Optional `activity_ref` as activity instance correlation. | Mandatory provenance or pattern reference. | ADR-004 S2 |
| System Actor Contract | `system:{source_type}/{source_id}` actor identity. | Anonymous system writes. | ADR-004 S4 / ADR-005 S9 |

### 11.2 Strategy-protecting runtime services

These services protect structural invariants without necessarily being stored-event schema.

| Primitive / service           | Owns                                                                                                                    | Does not own                                                    | ADR anchors                                          |
| ----------------------------- | ----------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------- | ---------------------------------------------------- |
| Subject Identity Resolver     | Merge/split validation, lineage DAG enforcement, online-only identity correction.                                       | Authorization, workflow state, domain matching policy details.  | ADR-002 S6-S10                                       |
| Conflict Detector             | Raw-reference detection, causal conflict checks, stale refs, auth/domain/workflow flag sources before policy execution. | Alias projection, downstream trigger execution, human judgment. | ADR-002 S11-S14; ADR-003 S7; ADR-004 S14; ADR-005 S1 |
| Conflict Resolver             | Single-writer resolution and canonical resolution semantics.                                                            | Offline conflict resolution, multi-resolver races.              | ADR-002 S11; ADR-003 S6                              |
| Authorization Evaluator       | Assignment-based role/scope/time check.                                                                                 | Envelope authority assertions, deployer-authored access logic.  | ADR-003 S1/S3/S5                                     |
| Sync Scope Resolver           | Computes data delivered to device from access scope.                                                                    | Independent replication outside access.                         | ADR-003 S2/S10                                       |
| Config Package Validator      | Atomic config consistency, dependency validation, budget enforcement.                                                   | Arbitrary partial runtime mutation.                             | ADR-004 S6/S13                                       |
| Trigger Engine                | Server-only L3a/L3b execution within DAG/depth bounds.                                                                  | Device-side trigger execution, recursion, unbounded automation. | ADR-004 S5/S12; ADR-005 S9                           |
| Scope Type Registry           | Platform-fixed scope containment semantics.                                                                             | Deployer-defined containment functions.                         | ADR-004 S7                                           |
| Sensitivity Classifier        | Shape/activity-level sensitivity labels.                                                                                | Field-level regulatory enforcement.                             | ADR-004 S8                                           |
| Flag Resolvability Classifier | `auto_eligible` / `manual_only` classification.                                                                         | Deployer reclassification of manual flags.                      | ADR-005 S3                                           |

### 11.3 Projection / read-model primitives

Projections are rebuildable derived views from events, config, and flags.

| Projection                     | Derives                                        | Inputs                                                      | Boundary                                            |
| ------------------------------ | ---------------------------------------------- | ----------------------------------------------------------- | --------------------------------------------------- |
| Subject Projection             | Current subject view.                          | Event stream, shape registry, alias mapping, flags.         | Does not rewrite historical events.                 |
| Alias Table Projection         | Retired-to-surviving subject resolution.       | `SubjectsMerged`, `SubjectSplit`.                           | Runs after raw-reference detection.                 |
| Assignment Timeline Projection | Actor assignments over time.                   | Assignment change events.                                   | Source of authority, not envelope.                  |
| Authority Projection           | Whether actor had authority in context.        | Assignment timeline, scope registry, original subject refs. | Does not depend on device assertion.                |
| Sync Projection                | Data set authorized for device.                | Authority projection + sync watermark.                      | Sync equals access.                                 |
| Workflow State Projection      | Current state under pattern.                   | Events, Pattern Registry, flags, config.                    | Excludes unresolved flagged events.                 |
| Source Chain Projection        | Downstream lineage and upstream flag warnings. | `source_event_ref` chains.                                  | Computes warnings; does not propagate stored flags. |
| Review / Flag Queue Projection | Human operational queues.                      | Flags, resolver assignments, source chain, identities.      | Ergonomics can evolve.                              |

### 11.4 Configuration artifacts

Configuration artifacts are deployer-authored or deployer-parameterized within platform-fixed boundaries.

| Artifact | Layer | Settled boundary |
|---|---:|---|
| Shape definition | L1 | Deployer defines payload schema; platform owns shape versioning semantics and field-type system. |
| Activity definition | L0 | Deployer assembles shape/pattern/roles/scopes/schedules; platform owns runtime semantics. |
| Logic rule | L2 | Bounded expressions over allowed scopes; no functions, no side effects. |
| Trigger definition | L3 | Server-only, bounded, non-recursive. |
| Domain uniqueness rule | L1/L3 policy surface | Shape/activity-level uniqueness; server authoritative; reuses flag infrastructure. |
| Flag severity map | L3 policy surface | Deployer parameterizes severity; platform controls flag category/resolvability. |
| Sensitivity label | L0/L1 policy surface | Shape/activity-level `standard`/`elevated`/`restricted`. |
| Pattern selection | L0 | Deployer selects platform-fixed pattern and fills slots; does not author pattern engine. |
| Auto-resolution policy parameter | L3b | Deployer may parameterize bounded policies for auto-eligible flags only. |

### 11.5 Implementation concerns

These are not consolidated architecture primitives.

| Concern | Why excluded |
|---|---|
| SQLite schema / indexes | Implementation of event store and projections. |
| Queue implementation | Operational mechanics for sync/flags/triggers. |
| Visual authoring UI | Must emit same underlying artifacts; not architecture vocabulary. |
| YAML/JSON syntax | Authoring format, not semantic boundary. |
| Local storage partitioning | Implementation detail; shared-device concerns remain outside architecture vocabulary. |
| Projection cache strategy | Optimization; projections remain rebuildable. |
| Pagination/backfill mechanisms | Sync optimization; sync=access remains. |
| Regulatory encryption/redaction/erasure | Platform evolution beyond current ADR-004 sensitivity boundary. |

---

## 12. Term Collision Resolution

### 12.1 `assignment`

| Source | Meaning |
|---|---|
| ADR-002 | Typed identity category. |
| ADR-003 | Atomic authorization grant binding actor, role, scope, and time. |

Consolidated resolution:

```txt
Assignment is both an identity category and the platform's authorization grant primitive.
```

ADR-003 refines the lifecycle and semantics of the ADR-002 identity category.

### 12.2 `activity`

| Usage | Meaning |
|---|---|
| `activity_ref` | Optional event-envelope field referencing an activity instance. |
| `activity instance` | Deployer-configured operational unit. |
| `activity` scope type | Platform-fixed scope category for access containment. |
| Activity definition | L0 configuration artifact. |

Consolidated rule:

```txt
Use the full phrase when ambiguity matters:
activity_ref field
activity instance
activity scope type
activity definition
```

### 12.3 `process`

`process` remains a typed identity category from ADR-002.

It must not be collapsed into:

- `activity_ref`;
- workflow pattern;
- trigger process;
- campaign definition.

Consolidated rule:

```txt
process identity = referenced operational process instance
activity = configured operational context
pattern = platform workflow skeleton
```

### 12.4 `type`

| Term | Meaning |
|---|---|
| event `type` | Six-value structural processing vocabulary. |
| identity `type` | Discriminator in `{type,id}` reference. |
| field type | Shape schema field type. |
| flag type/category | Flag category such as `transition_violation`. |

Consolidated rule:

```txt
Never use bare "type" in implementation docs without qualifier.
```

### 12.5 `state`

| Usage | Status |
|---|---|
| Current subject/workflow state | Projection-derived. |
| State stored in event | Rejected. |
| State machine skeleton | Pattern Registry. |
| Lifecycle state `active` / `archived` | Subject lineage validation vocabulary. |

Consolidated rule:

```txt
Workflow state is projection-derived.
Subject lineage state is validation state.
Neither is mutable stored current state.
```

### 12.6 `pattern`

ADR-004 used pattern language in configuration exploration.

ADR-005 locks the settled meaning:

```txt
Pattern = platform-fixed workflow skeleton selected and parameterized at L0.
```

Do not treat scenario pattern examples as a closed inventory unless a later platform specification freezes them.

### 12.7 `flag`

The generic flag mechanism spans identity, authorization, domain uniqueness, and workflow.

Consolidated rule:

```txt
flag category ≠ severity ≠ resolvability ≠ resolver assignment
```

### 12.8 `trigger`

| Usage | Meaning |
|---|---|
| Trigger definition | L3 config artifact. |
| Trigger Engine | Server-side runtime service. |
| `trigger` source type | System actor source type in `system:trigger/{trigger_id}`. |

Consolidated rule:

```txt
Trigger-produced events are normal events with auditable system actor identity.
```

### 12.9 `context`

ADR-004 deferred `context.*`.

ADR-005 locks a closed set of seven read-only pre-resolved values.

Consolidated rule:

```txt
context.* is not a query escape hatch.
```

---

## 13. Boundary Map by Architectural Area

### 13.1 Storage boundary

| In boundary | Out of boundary |
|---|---|
| Immutable events. | Mutable current-state records. |
| Append-only corrections. | In-place edits/deletes. |
| Rebuildable projections. | Projection as source of truth. |
| Event-as-sync-unit. | Snapshot-as-authoritative sync unit. |

### 13.2 Identity boundary

| In boundary | Out of boundary |
|---|---|
| Typed identity references. | Untyped UUID pointers. |
| Subject merge by alias projection. | Physical re-reference of historical events. |
| Corrective split. | Symmetric unmerge. |
| Raw-reference detection. | Alias-before-detection. |
| Hardware-bound device identity. | Account-bound device identity. |

### 13.3 Conflict boundary

| In boundary | Out of boundary |
|---|---|
| Accept-and-flag. | Reject stale offline events. |
| Single designated resolver. | Multi-writer resolution. |
| Detect-before-act. | Fire policies before conflict detection. |
| Flag categories and severity/resolvability separation. | Treat all flags as equivalent. |

### 13.4 Authorization boundary

| In boundary | Out of boundary |
|---|---|
| Assignment-based access. | Role-only access. |
| Scope-containment test. | Arbitrary deployer auth code. |
| Authority-as-projection. | Envelope authority assertion. |
| Sync scope = access scope. | Sync as separate replication permission. |
| Alias-respects-original-scope. | Authorizing post-alias surviving scope only. |

### 13.5 Configuration boundary

| In boundary | Out of boundary |
|---|---|
| L0-L3 gradient. | Configuration as arbitrary programming. |
| Shape/activity/pattern assembly. | Deployer-defined platform primitives. |
| Server-only bounded triggers. | Device triggers or recursive automation. |
| Fixed scope types. | Deployer-defined containment functions. |
| Shape/activity sensitivity. | Field-level sensitivity configuration. |

### 13.6 Workflow boundary

| In boundary | Out of boundary |
|---|---|
| Projection-derived state. | Stored current state. |
| Pattern Registry. | Deployer-authored state machines. |
| Transition flags. | Transition rejection. |
| Source-only flagging. | Propagated downstream stored flags. |
| Bounded `context.*`. | Dynamic workflow query language. |
| Auto-resolution for auto-eligible flags. | Unbounded auto-resolution rule engine. |

---

## 14. Cross-Primitive Interaction Model

### 14.1 Basic capture path

```txt
1. Config Package delivered atomically to device.
2. Actor opens Activity.
3. Device loads Shape + L2 Logic + Pattern/context as needed.
4. Device checks local Assignment projection for role/scope permission.
5. Device writes immutable Event locally.
6. Event envelope includes identity, shape, activity, actor, device sequence, watermark, timestamp, payload.
7. Event syncs to server.
8. Server accepts event.
9. Conflict Detector evaluates raw refs, causal ordering, authorization, domain uniqueness, transition validity.
10. If clean, projections update and eligible triggers may run.
11. If flagged, detect-before-act blocks downstream policy and workflow-state derivation until resolution.
12. Sync Scope Resolver sends authorized deltas to devices.
```

### 14.2 Merge path

```txt
1. Server-authorized actor issues merge.
2. Subject Identity Resolver validates both subjects active and lineage acyclic.
3. SubjectsMerged event records retired_id → surviving_id.
4. Historical events remain unchanged under original subject_ref.
5. Conflict Detector still evaluates future incoming stale events using raw references.
6. Alias Table Projection resolves retired_id to surviving_id for read models.
7. Authorization uses original subject_ref where scope-crossing aliasing exists.
```

### 14.3 Split path

```txt
1. Server-authorized actor issues corrective split.
2. Subject Identity Resolver archives source permanently.
3. Successor subject IDs receive new future events.
4. Historical events stay with source_id.
5. Projection may attribute future work to successors.
6. Source archive remains terminal; no reactivation, merge-into, or second split.
```

### 14.4 Scope change path

```txt
1. assignment_changed event updates assignment timeline.
2. Authority Projection derives new actor scope.
3. Sync Scope Resolver contracts or expands authorized dataset.
4. Device may still create offline events under stale assignment state.
5. Server accepts and flags stale-authority work on sync.
6. Selective-retain strategy governs local data after contraction.
```

### 14.5 Configuration update path

```txt
1. Deployer changes shapes/activities/logic/triggers/policies within platform bounds.
2. Config Package Validator checks dependencies, budgets, version coexistence, shape refs, trigger DAG, pattern mappings.
3. Atomic config package is delivered on sync.
4. Device maintains current + previous config for in-progress work.
5. Events continue carrying shape_ref of the schema active at capture time.
6. Projections interpret historical events against their referenced shape version.
```

### 14.6 Workflow transition path

```txt
1. Event arrives with shape_ref/activity_ref.
2. Projection Engine determines applicable Pattern via activity + shape mapping.
3. Conflict Detector checks whether implied transition is valid.
4. If valid, Workflow State Projection can include the event.
5. If invalid, transition_violation flag is created.
6. Flagged event remains visible but excluded from workflow state.
7. Resolution may later include or exclude it from derived state.
```

### 14.7 Source-chain / downstream work path

```txt
1. Source event creates downstream task/review/alert through trigger or workflow.
2. Downstream event references source via source_event_ref.
3. If source is later flagged, downstream events are not given copied flags.
4. Source Chain Projection walks references and surfaces upstream-warning state.
5. Resolving root flag clears computed downstream warnings.
```

### 14.8 Auto-resolution path

```txt
1. Flag category is classified auto_eligible.
2. L3b auto-resolution policy checks bounded conditions.
3. Server writes standard resolution event as system:auto_resolution/{policy_id}.
4. Loop guards prevent recursive or unbounded resolution behavior.
5. Manual-only flags cannot be targeted by deployer-configured auto-resolution.
```

---

## 15. Decision Boundary Classification Map

### 15.1 Structural constraints

| Area | Structural commitments |
|---|---|
| ADR-001 storage | Append-only writes; event as atomic write; client UUIDs; event sync unit; base envelope. |
| ADR-002 identity/conflict | Causal fields; typed identity refs; advisory device time; hardware device ID; merge/split events and lineage rules; raw-reference detection; accept-and-flag. |
| ADR-003 auth/sync | Assignment-based access model; sync scope equals access scope; authority not in envelope; alias-respects-original-scope. |
| ADR-004 config/event contract | `shape_ref`; `activity_ref`; fixed event `type` vocabulary. |
| ADR-005 workflow | None added to event envelope or type vocabulary. |

### 15.2 Strategy-protecting constraints

| Area | Strategy-protecting commitments |
|---|---|
| ADR-002 | Online-only merge/split; single-writer conflict resolution; detect-before-act. |
| ADR-003 | Scope-containment invariant; online-only conflict resolution; detect-before-act extends to authorization flags. |
| ADR-004 | System actor format; server-only triggers; atomic config delivery; no deployer auth logic; fixed scope types; no field-level sensitivity. |
| ADR-005 | `transition_violation`; flagged-event exclusion; flag resolvability classification. |

### 15.3 Initial strategies

| Area | Initial strategies |
|---|---|
| ADR-003 | Tiered projection; auth staleness flags; selective-retain. |
| ADR-004 | Four-layer gradient; shape authoring/storage; expression language; trigger architecture; complexity budgets; deployer policy parameters. |
| ADR-005 | Projection-derived state machines; Command Validator; Pattern Registry; composition rules; source-only flagging; `context.*`; auto-resolution. |

---

## 16. Consolidated Open Evolution Register

These are valid future work areas only if classified correctly.

### 16.1 Platform evolution that does not violate accepted decisions

| Item | Allowed direction |
|---|---|
| New structural event type | Only if new platform processing behavior is proven. Append-only type vocabulary. |
| New pattern type | Platform evolution; deployers may select after platform ships it. |
| Additional `context.*` values | Platform-fixed, read-only, append-only vocabulary extension. |
| Additional auto-resolution policies | Within L3b guardrails and auto-eligible categories. |
| Regulatory encryption/redaction/erasure | Separate platform mechanisms; must not redefine event immutability silently. |
| Multi-tenant namespacing | Preserve `shape_ref`/`activity_ref` parse safety and historical interpretation. |
| Complexity budget changes | Platform validation evolution; historical events unchanged. |

### 16.2 Expansion of explicitly open fronts

| Front                                 | Source                                                                                            |
| ------------------------------------- | ------------------------------------------------------------------------------------------------- |
| Exact Pattern Registry inventory      | ADR-005 deferred/open.                                                                            |
| Pattern migration mechanics           | ADR-005 platform specification / implementation.                                                  |
| Richer flag queue ergonomics          | ADR-002/ADR-005 open operational area.                                                            |
| Domain conflict resolution strategies | ADR-004 deferred to ADR-005; ADR-005 allows bounded auto-resolution only for eligible categories. |

### 16.3 Underexplored or not settled

| Front | Why not settled |
|---|---|
| Actor-as-subject delivery rule | ADR-003 excluded as settled structure; may become sync filter or future scope rule. |
| Auditor/query access | ADR-003 excluded/deferred; requires separate access model work. |
| Shared-device storage partitioning | Implementation/security design, not recovered architecture vocabulary. |
| Role-action table shape | ADR-003 requires role permits action, ADR-004 rejects deployer-authored access logic; exact table artifact is not recovered here as standalone primitive. |

---

## 17. S00 Simplicity Check

S00 remains simple under the consolidated vocabulary.

Minimum path:

```txt
shape: household_observation/v1
activity: household_survey
pattern: capture_only
assignment: field_worker + geographic scope
```

The resulting event is:

```txt
type = capture
shape_ref = household_observation/v1
activity_ref = household_survey or null depending on capture context
subject_ref = target subject
actor_ref = field worker
device_id/device_seq/sync_watermark/timestamp = device envelope metadata
payload = observed fields
```

What S00 does not require:

- custom event type;
- custom access-control code;
- custom trigger;
- state machine authoring;
- `pattern_ref`;
- authority context field;
- field-level sensitivity;
- auto-resolution policy;
- workflow flag propagation.

The full vocabulary does not leak into the simplest case.

---

## 18. Output Summary

This file consolidates the independently recovered ADR-002 through ADR-005 vocabulary into one cross-lineage map.

The core result is:

```txt
Structural contracts define durable facts.
Strategy-protecting services guard invariants.
Configuration artifacts parameterize platform-fixed capabilities.
Projections derive current meaning.
Implementation concerns remain outside architecture vocabulary.
```

The consolidated primitive map is now sufficient input for the final architecture reference.

Next revised phase:

```txt
008-authoritative-architecture-map.md
```
