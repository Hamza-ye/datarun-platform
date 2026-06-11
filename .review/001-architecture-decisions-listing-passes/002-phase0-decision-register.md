# Phase 0: Decision Register — Verification Anchor

> Every extraction pass in Phases 1–3 must map findings to entries in this register.
> If a finding doesn't map here, it is **not settled** and must be skipped.

---

## ADR-001: Offline Data Model (5 sub-decisions)

| ID | Classification | Decision | Vocabulary Introduced | Defers To |
|----|---------------|----------|----------------------|-----------|
| S1 | Structural | All writes are append-only | `append-only`, `correction` (as new event referencing prior) | — |
| S2 | Structural | Atomic write unit is a typed, immutable event. Current state is projection from event stream. Write-path discipline: every state change enters through event store. Projections are always rebuildable. | `event`, `projection`, `event store`, `write-path discipline` | — |
| S3 | Structural | All IDs are client-generated UUIDs | `client-generated UUID` | Duplicate real-world subjects → ADR-2 |
| S4 | Structural | Sync unit is the immutable event. Idempotent, append-only, order-independent. | `sync unit`, `idempotent sync` | Sync scope → ADR-3 |
| S5 | Structural | Event envelope guarantees: identity, type, payload, timestamp decided here. Subject association, event references, schema versioning, authorship, authority context reserved for downstream ADRs. Envelope extensibility. | `event envelope`, `envelope extensibility` | Subject → ADR-2, Schema → ADR-4, Auth → ADR-3 |

---

## ADR-002: Identity Model and Conflict Resolution (14 sub-decisions)

| ID | Classification | Decision | Vocabulary Introduced | Defers To |
|----|---------------|----------|----------------------|-----------|
| S1 | Structural | Every event carries `device_id`, `device_sequence`, `sync_watermark` for causal ordering. Per-subject causality evaluation. | `device_id`, `device_sequence`, `sync_watermark`, `causal ordering`, `concurrency detection` | — |
| S2 | Structural | All identity refs carry `{type, id}`. Four categories: `subject`, `actor`, `process`, `assignment`. | `typed identity reference`, `subject`, `actor`, `process`, `assignment` (as identity types) | — |
| S3 | Structural | `device_time` is advisory only. No structural dependency on device clock. | `device_time` (advisory) | — |
| S4 | Structural | `device_sequence` and `sync_watermark` MUST persist to durable storage. `(device_id, device_sequence)` is globally unique. | — | — |
| S5 | Structural | `device_id` is hardware-bound, not user-bound. New device = new device_id. | `hardware-bound device identity` | — |
| S6 | Structural | Merge = alias-in-projection via `SubjectsMerged` event. `retired_id → surviving_id`. No re-reference. Eager transitive closure. | `SubjectsMerged`, `alias mapping`, `retired_id`, `surviving_id`, `transitive closure` | — |
| S7 | Structural | No `SubjectsUnmerged`. Wrong merges corrected by `SubjectSplit`. | `corrective split` | — |
| S8 | Structural | `SubjectSplit` archives source permanently. Historical events stay with source_id. New events go to successors. | `SubjectSplit`, `archived` (lifecycle state), `successor` | — |
| S9 | Structural | Lineage graph acyclicity enforced: merge operands must be active; archived is terminal. | `lineage DAG`, `active` (lifecycle state) | — |
| S10 | Strategy-protecting | Merge and split are online-only server-validated. | `online-only operations` | — |
| S11 | Strategy-protecting | Single-writer conflict resolution. Every `ConflictDetected` designates exactly one resolver. | `ConflictDetected`, `ConflictResolved`, `designated resolver`, `single-writer resolution` | Who can be resolver → ADR-3 |
| S12 | Strategy-protecting | Conflict detection runs BEFORE policy execution during sync. Flagged events don't trigger policies until resolved. | `detect-before-act`, `flag` (on event) | Cascade behavior → ADR-5 |
| S13 | Structural | Conflict detection uses raw `subject_id` as written. Alias resolution only in projection layer after detection. | `raw-reference detection` | — |
| S14 | Structural | Events are NEVER rejected for state staleness. Accept and flag. | `accept-and-flag`, `state staleness` | — |

### ADR-002 Vocabulary to Recover from Exploration

- [ ] Identity taxonomy: full lifecycle semantics for each of the 4 identity categories
- [ ] Conflict type taxonomy: what constitutes each type of conflict
- [ ] Causal ordering: how device_sequence + sync_watermark were discovered and why alternatives were rejected
- [ ] Accept-and-flag: the under-load reasoning that forced this mechanism
- [ ] Alias table: boundary between detection (raw refs) and projection (resolved refs)
- [ ] Merge/split: why unmerge was rejected, why split freezes history

---

## ADR-003: Authorization and Selective Sync (10 sub-decisions)

| ID | Classification | Decision | Vocabulary Introduced | Defers To |
|----|---------------|----------|----------------------|-----------|
| S1 | Structural | Assignment-based access control. Single scope-containment test. | `assignment-based access`, `scope-containment test` | Role-action tables → ADR-4 |
| S2 | Structural | Sync scope = access scope. Device gets exactly authorized data. | `sync scope = access scope` | — |
| S3 | Structural | Authority context is a projection, NOT an envelope field. No new envelope fields from ADR-003. | `authority-as-projection`, `assignment timeline` | — |
| S4 | Structural | Alias-respects-original-scope. Authorization uses original `subject_ref`. | `alias-respects-original-scope` | — |
| S5 | Strategy-protecting | Scope-containment invariant on assignment creation (new ⊆ creator's scope). | `scope-containment invariant`, `privilege escalation prevention` | — |
| S6 | Strategy-protecting | Conflict resolution is online-only. | — (extends ADR-002 S10 precedent) | — |
| S7 | Strategy-protecting | Detect-before-act extends to ALL flag types including authorization flags. | `ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag` | Flag severity config → ADR-4 |
| S8 | Initial strategy | Tiered projection location (field=device, supervisor=hybrid, coordinator=server). | `tiered projection` | — |
| S9 | Initial strategy | Authorization staleness uses accept-and-flag. New flag types for auth. Watermark-based auto-resolution for scope. | — | — |
| S10 | Initial strategy | Scope change data handling: selective-retain on contraction. | `selective-retain`, `scope contraction` | — |

### ADR-003 Vocabulary to Recover from Exploration

- [ ] Assignment model: how assignments bind actors to scopes with temporal boundaries
- [ ] Why authority was kept OUT of the envelope (the exploration-then-rejection reasoning)
- [ ] Scope types and containment semantics
- [ ] Authorization flag types and their interaction with detect-before-act
- [ ] Sync = access: the forcing function from scenarios

---

## ADR-004: Configuration Boundary (14 sub-decisions)

| ID | Classification | Decision | Vocabulary Introduced | Defers To |
|----|---------------|----------|----------------------|-----------|
| S1 | Structural | `shape_ref` mandatory in every event. Format: `{shape_name}/v{version}`. | `shape_ref`, `shape`, `shape version`, `shape registry` | — |
| S2 | Structural | `activity_ref` optional in events. Deployer-chosen identifier. | `activity_ref`, `activity instance` | — |
| S3 | Structural | `type` field: platform-fixed, closed, append-only, 6-value vocabulary. Types represent processing behavior, not domain meaning. | 6 types: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed` | `status_changed` evaluation → ADR-5 |
| S4 | Strategy-protecting | System actor identity: `system:{source_type}/{source_id}`. | `system actor`, `trigger` source type | — |
| S5 | Strategy-protecting | All triggers execute server-only. | `server-only triggers` | — |
| S6 | Strategy-protecting | Atomic configuration delivery to devices. At most 2 versions coexist. | `atomic config delivery`, `config version` | — |
| S7 | Strategy-protecting | No deployer-authored access control logic. 3 platform-fixed scope types. | `geographic`, `subject_list`, `activity` (scope types) | — |
| S8 | Strategy-protecting | No field-level sensitivity. Sensitivity at shape/activity level. | `sensitivity classification` (shape-level) | — |
| S9 | Initial strategy | Four-layer configuration gradient: L0 Assembly, L1 Shape, L2 Logic, L3 Policy. Side-effect boundary between layers. | `L0 Assembly`, `L1 Shape`, `L2 Logic`, `L3 Policy`, `four-layer gradient`, `L3→code boundary` | — |
| S10 | Initial strategy | Shapes: typed payload schemas, versioned, delta-authored, snapshot-stored. Deprecation-only default evolution. | `shape definition`, `shape evolution`, `deprecation-only`, `breaking change` | — |
| S11 | Initial strategy | One expression language, two contexts (form L2, trigger L3). Operators + field refs only. Zero functions. Scopes: `payload.*`, `entity.*`, `event.*`. | `expression language`, `payload.*`, `entity.*`, `event.*` scopes | `context.*` → ADR-5 |
| S12 | Initial strategy | Trigger architecture: 3a event-reaction (sync, 1-in-1-out), 3b deadline-check (async). Non-recursive. DAG max path 2. | `event-reaction trigger (3a)`, `deadline-check trigger (3b)`, `trigger DAG`, `max path length 2` | — |
| S13 | Initial strategy | Complexity budgets: 60 fields/shape, 3 predicates/condition, 5 triggers/event-type, 50 triggers/deployment, 2-level escalation. | `complexity budgets` | — |
| S14 | Initial strategy | Deployer-parameterized policies: flag severity, domain uniqueness, scope composition, sensitivity levels. | `domain_uniqueness_violation`, `standard`/`elevated`/`restricted` sensitivity | Auto-resolution → ADR-5 |

### ADR-004 Vocabulary to Recover from Exploration

- [ ] Event type vocabulary closure: why 6 types, how domain meaning vs processing behavior was discovered
- [ ] Shape model: how shapes relate to events, activities, patterns
- [ ] Four-layer gradient: the anti-pattern catalog (AP-1 through AP-6) that forced it
- [ ] Expression language: why zero functions, why one language for two contexts
- [ ] Trigger architecture: why non-recursive, why max path 2, why server-only
- [ ] Complexity budgets: calibration from scenario walk-throughs

---

## ADR-005: State Progression and Workflow (9 sub-decisions)

| ID | Classification | Decision | Vocabulary Introduced | Defers To |
|----|---------------|----------|----------------------|-----------|
| S1 | Strategy-protecting | `transition_violation` flag category. | `transition_violation` | — |
| S2 | Strategy-protecting | Flagged events excluded from state machine evaluation in projection. Visible in timeline, excluded from state derivation. | `flagged-event exclusion` | — |
| S3 | Strategy-protecting | Flag resolvability classification: `auto_eligible` or `manual_only`. Platform-level, not deployer-configurable. | `auto_eligible`, `manual_only`, `flag resolvability` | — |
| S4 | Initial strategy | State machines are projection patterns, not enforced. State never stored in events. Command Validator is advisory. | `projection-derived state machine`, `Command Validator` (advisory) | — |
| S5 | Initial strategy | Pattern Registry: platform-fixed workflow skeletons, deployer-selected and parameterized at L0. Closed vocabulary. | `Pattern Registry`, `pattern`, `participant roles`, `state machine skeleton`, `parameterization points` | — |
| S6 | Initial strategy | 5 composition rules: one subject-level pattern/activity, event-level compose freely, approval sub-flows embed, cross-activity via activity_ref, shape-to-pattern unique. | `subject-level pattern`, `event-level pattern`, `composition rules` | — |
| S7 | Initial strategy | Source-only flagging. No flag propagation to downstream events. Source-chain traversal in projection. | `source-only flagging`, `source-chain traversal`, `source_event_ref` | — |
| S8 | Initial strategy | `context.*` expression scope: 7 pre-resolved read-only values. Platform-fixed, closed, append-only. | `context.*` scope, 7 properties: `subject_state`, `subject_pattern`, `activity_stage`, `actor.role`, `actor.scope_name`, `days_since_last_event`, `event_count` | — |
| S9 | Initial strategy | Auto-resolution as L3b sub-type. Uses `system:auto_resolution/{policy_id}`. Three loop-prevention guards. | `auto-resolution`, `auto_resolution` source type | — |

### ADR-005 Note

ADR-005 exploration files (19, 20, 21) are NOT in scope for this recovery. ADR-005 itself is authoritative and complete. Its vocabulary will be integrated in Phase 4.

---

## Event Envelope — Final Form (11 fields, 4 ADRs)

| Field | Source | Presence | Notes |
|-------|--------|----------|-------|
| `id` | ADR-1 S3 | Mandatory | Client-generated UUID |
| `type` | ADR-1 S5 + ADR-4 S3 | Mandatory | 6-value closed vocabulary |
| `shape_ref` | ADR-4 S1 | Mandatory | `{name}/v{version}` |
| `activity_ref` | ADR-4 S2 | Optional | Deployer-chosen identifier |
| `subject_ref` | ADR-2 S2 | Mandatory | `{type: "subject", id: UUID}` |
| `actor_ref` | ADR-2 S2 | Mandatory | `{type: "actor", id: UUID}` or `system:...` |
| `device_id` | ADR-2 S1/S5 | Mandatory | Hardware-bound UUID |
| `device_seq` | ADR-2 S1/S4 | Mandatory | Monotonically increasing per device |
| `sync_watermark` | ADR-2 S1 | Mandatory | Null until first sync |
| `timestamp` | ADR-1 S5 + ADR-2 S3 | Mandatory | Advisory device_time |
| `payload` | ADR-1 S5 | Mandatory | Action-specific data |

---

## Flag Category Register

| Flag Category | Source | Resolvability (ADR-005 S3) |
|---------------|--------|---------------------------|
| `identity_conflict` | ADR-002 | `manual_only` |
| `concurrent_state_change` | ADR-002 | `manual_only` |
| `stale_reference` | ADR-002 | `auto_eligible` |
| `scope_violation` | ADR-003 S7/S9 | `manual_only` |
| `ScopeStaleFlag` | ADR-003 S7/S9 | informational default |
| `RoleStaleFlag` | ADR-003 S7/S9 | blocking for capability-restricted |
| `TemporalAuthorityExpiredFlag` | ADR-003 S7/S9 | informational default |
| `domain_uniqueness_violation` | ADR-004 S14 | `manual_only` |
| `transition_violation` | ADR-005 S1 | `auto_eligible` |

---

## Classification Key

- **Structural constraint**: Cannot change without data migration across deployed devices. In every stored event forever.
- **Strategy-protecting constraint**: Guards structural invariants through server-side logic. The invariant is permanent; the implementation can evolve.
- **Initial strategy**: Documents current approach. Can evolve without affecting stored events or breaking deployed devices.
