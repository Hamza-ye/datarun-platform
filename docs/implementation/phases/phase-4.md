# Phase 4: Workflow & Policies

> Implementation-grade plan for Phase 4 after the Phase 4.0 rollback and the IDR-020/021/022 prep pass. This spec authorizes Phase 4 implementation sequencing, but it does not itself start code work.

**Exercises**: [IDR-020](../../decisions/idr-020-pattern-state-machine-representation.md) (Pattern State Machine Representation), [IDR-021](../../decisions/idr-021-role-action-enforcement-model.md) (Role-Action Enforcement Model), [IDR-022](../../decisions/idr-022-flag-severity-and-domain-uniqueness.md) (Flag Severity + Domain Uniqueness), [IDR-023](../../decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md) (Role-Action Domain Boundary and Assignment Administration), [IDR-024](../../decisions/idr-024-multi-axis-assignment-containment.md) (Multi-axis Assignment Containment), [ADR-003](../../adrs/adr-003-authorization-sync.md) S1/S3/S5/S7, [ADR-004](../../adrs/adr-004-configuration-boundary.md) S6/S7/S9/S14, [ADR-005](../../adrs/adr-005-state-progression.md) S1-S9, and [ADR-006](../../adrs/adr-006-flag-semantics.md) S1-S4.

**Primitives touched**: Config Package, Deploy-Time Validator, Shape Registry, Pattern Registry, Projection Engine, Conflict Detector, Conflict Resolution, Assignment Authority Projection, Sync Pull, Mobile Config Store, Mobile Projection Engine, Mobile Advisory Validators.

**Not introducing new envelope fields. Not introducing new envelope types. Not introducing deployer-authored state machines. Not changing normal live sync into historical pull. Not making activity mandatory. Not authorizing `assignment_changed` through `activities[*].roles`. Not trusting assignment command actor IDs from request bodies.**

---

## 1. Why Phase 4 Exists

Phase 3 completed configuration delivery, shape validation, expressions, device context resolution, envelope-type closure, and the current identity/authorization flag pipeline. Phase 4 adds the first workflow and policy behavior that depends on those surfaces:

- activity-scoped role-action enforcement from `activities[*].roles`;
- projection-derived workflow pattern state from platform-bundled pattern definitions;
- `transition_violation` detection for invalid pattern transitions;
- deployment-wide flag severity config through `flag_severity_overrides`;
- shape-declared domain uniqueness detection through `shapes[*].uniqueness`;
- device advisory UX for role-action, uniqueness, and pattern-state warnings;
- multi-axis assignment creation/end containment across geography, subject list, and activity;
- subject-history backfill required before `ongoing_resolution` is implemented or enabled.

Phase 4 is not a general policy engine. It activates already-decided Phase 4 surfaces while preserving the invariants from ADR-001 through ADR-006: append-only events, accept-and-flag for state anomalies, authority as projection, sync scope equals access scope, closed envelope type vocabulary, and state as projection.

---

## 2. Contradiction Pass

The following constraints are load-bearing. Phase 4 implementation must fail review if it contradicts any item.

### FP-005 Boundary

FP-005 remains `IN_PROGRESS` and routed, not resolved. Phase 4 must implement its route exactly:

1. Normal `/api/sync/pull` remains request-time scoped against current active assignments. It must not become historical reconstruction, subject-history replay, or audit pull.
2. `ongoing_resolution` cannot be implemented or enabled until subject-history backfill is specified and tested.
3. Subject-history backfill is separate from role-action enforcement, flag severity, domain uniqueness, and pattern transition logic.
4. Audit/historical pull is out of Phase 4 live sync. If audit reconstruction is needed later, it requires a successor decision and a separate pull class/API.
5. The backfill design must define idempotence/cursor behavior without lowering the normal sync watermark, request-time authorization on every page, alias handling after merge/split, activity filtering for pattern state keys, and visibility of assignment/transfer events needed by subject-level projections.

### FP-006 Boundary

FP-006 is resolved. Authorization CD now gates `temporal_authority_expired` on the assignment-ended watermark being newer than `min(event.sync_watermark, push.last_pull_watermark)`, so a superseded ended assignment does not over-flag after the actor has synced replacement authority. Phase 4 role-action, domain uniqueness, and pattern transition passes must preserve this precision because unresolved flags exclude otherwise valid events from authoritative projections.

### IDR-020 Boundary

- Pattern definitions are platform-bundled; deployers bind shapes, participant roles, and parameters only.
- Event-level reviewability is orthogonal to subject-level patterns. `ongoing_resolution` events can be reviewed through event-level overlays such as `capture_with_review`, the same as events from any other activity, once `ongoing_resolution` itself is allowed past FP-005.
- No event stores `current_state`, `pattern_ref`, or any workflow-state field.
- Pattern state identity is `(subject_ref, activity_ref, binding.ref)` for subject-level patterns and `(source_event_id, binding.ref)` for event-level patterns.
- Phase 4 must not use `subject_ref.type = "process"` for pattern instances.
- `transition_violation` is accept-and-flag: structurally valid events are persisted and flagged, never rejected for transition reasons.
- Unresolved flagged events remain excluded from pattern-state derivation.
- `entity_lifecycle` is separate from `ongoing_resolution`. Scenario 06 support requires implementing `entity_lifecycle` or explicitly deferring S06; it must not be approximated by `ongoing_resolution`.

### IDR-021 Boundary

- Role-action permissions are activity-scoped L0 config in `activities[*].roles`.
- The action vocabulary for activity role-action is the five activity work envelope `type` values: `capture`, `review`, `alert`, `task_created`, and `task_completed`.
- `assignment_changed` is not valid in `activities[*].roles`; assignment lifecycle commands are authority administration, not activity-scoped work.
- Server-side role-action checks are authoritative; mobile checks are advisory UX only.
- Offline-pushed work is accepted and flagged as `role_stale` when horizon authority or current authority does not permit the attempted action.
- `role_stale` means action-authority mismatch, not any role-label change.
- Role-action enforcement must not alter `/api/sync/pull`, subject-history backfill, or audit pull behavior.
- Activity remains optional at the envelope/model level. Phase 4 must not turn activity into the root of all authorization.

### IDR-024 / FP-007 Boundary

- Assignment creation containment applies across all three platform-fixed scope axes: `geographic`, `subject_list`, and `activity`.
- A requested unrestricted axis (`null`) requires creator authority unrestricted on that same axis or explicit bootstrap/root authority.
- Requested subject-list and activity values must be subsets of the covering creator assignment when that creator assignment is restricted on the corresponding axis.
- Containment is evaluated against one covering creator assignment; Phase 4 must not implicitly union separate creator assignments across axes.
- Subject-list-only assignments do not imply root/admin authority merely because their geographic axis is null.
- Bootstrap/root behavior must be explicit and bounded; an arbitrary actor with no active assignments is not production root authority.
- Activity-restricted assignments do not authorize ordinary null-activity work events. Activity remains optional, but null-activity work needs unrestricted activity authority or a separately decided import/baseline/system rule.
- Assignment ending needs target-assignment authority or explicit bootstrap/root authority, not merely a request actor ID.
- This boundary must not reintroduce `assignment_changed` into `activities[*].roles` and must not fold assignment administration into role-action enforcement.

### FP-008 Boundary

- Ordinary `/api/assignments` create/end command authority must be bound to authenticated token/session/request actor context.
- `creator_actor_id` and `actor_id` request-body fields are not authority inputs for ordinary assignment commands; request bodies identify the target actor, target assignment, target scope, validity window, and reason only.
- The explicit initial bootstrap path remains separate from ordinary assignment commands and must not be reachable by spoofing request-body actor fields.
- Until production admin auth exists, HTML admin assignment forms are development-only and must not be treated as production assignment-administration semantics.

### IDR-022 Boundary

- Severity config is a flat deployment-wide `flag_severity_overrides` map.
- Valid severity values are `blocking` and `informational`.
- Severity is separate from resolvability. `manual_only` vs `auto_eligible` remains platform-owned.
- Phase 4 does not introduce per-activity severity overrides.
- Domain uniqueness remains a shape-declared L1 constraint under `shapes[*].uniqueness`.
- Phase 4 uses `device_action` as advisory device UX only. The older `action` wording must not become server authority.
- Server uniqueness behavior is always accept-and-flag.
- `domain_uniqueness_violation` uses `type = "alert"`, `shape_ref = "conflict_detected/v1"`, payload `flag_category = "domain_uniqueness_violation"`, and `source_event_id` targeting the incoming conflicting event.
- `domain_uniqueness_violation` is `manual_only`.
- Detection order is structural/shape validation, identity/lifecycle, authorization including role-action, domain uniqueness, then pattern transition.

---

## 3. What's In Scope

| # | Item | Source | Kind |
|---|------|--------|------|
| 4.1 | Role-action config validation, packaging, mobile parsing, advisory device gating, and authoritative server `role_stale` semantics | IDR-021 / IDR-023 | Policy enforcement |
| 4.2 | Flag severity defaults and deployment-wide `flag_severity_overrides` validation, packaging, server/mobile interpretation | IDR-022 | Config + workflow gating (**landed**) |
| 4.3 | Shape uniqueness schema, DtV validation, advisory device duplicate warnings, server-side `domain_uniqueness_violation` detector | IDR-022 | Shape policy (**landed**) |
| 4.4 | Platform Pattern Registry and activity pattern binding validation | IDR-020 | Workflow registry |
| 4.5 | Server and mobile pattern-state projection, including unresolved-flag exclusion and state re-derivation after resolution | IDR-020 / ADR-005 | Projection |
| 4.6 | Server `transition_violation` detector in the push pipeline | IDR-020 / ADR-005 | Conflict detection |
| 4.7 | Subject-history backfill decision and implementation before any `ongoing_resolution` support | FP-005 / IDR-020 | Sync support |
| 4.8 | Phase 4 shared fixtures and quality gates spanning server/mobile equivalence | IDR-020/021/022/023/024 | Test infrastructure |
| 4.9 | Multi-axis assignment creation/end containment and null-activity work-event semantics | IDR-024 / FP-007 | Assignment administration |

---

## 4. What's Explicitly Out of Scope

- Audit/historical pull in normal live sync.
- Any new envelope `type`.
- Any new envelope field.
- Deployer-authored state-machine definitions or deployer-authored transition tables.
- `subject_ref.type = "process"` activation for workflow instances.
- Per-activity flag severity in Phase 4.
- Rejection of structurally valid domain uniqueness violations.
- Rejection of structurally valid pattern transition violations.
- Folding FP-005 backfill/audit behavior into role-action enforcement, flag severity, domain uniqueness, or transition checks.
- Folding assignment administration into activity role-action enforcement.
- Making `activity_ref` mandatory to avoid null-activity semantics.
- Treating `assignment_changed` as valid in `activities[*].roles`.
- `entity_lifecycle` implementation unless explicitly promoted by a Phase 4 sub-scope before S06 support is claimed.
- General trigger engine or auto-resolution implementation unless a later Phase 4 addendum explicitly schedules it. `transition_violation` remains `auto_eligible`, but auto-resolution policy execution is not required for the first Phase 4 workflow slice.

---

## 5. Implementation Slices

### 4.1 Role-Action Enforcement

Server work:

- Validate `activities[*].roles` as `{role: [action...]}` where each role is non-empty and each action is one of the five activity work envelope `type` values.
- Reject empty action lists and unknown action names at deploy time.
- Reject `assignment_changed` in activity role-action config.
- Preserve `activities[*].roles` unchanged in the config package.
- Revise `ConflictDetector` role-action logic so `role_stale` is emitted when either:
  - no covering assignment at `min(event.sync_watermark, push.last_pull_watermark)` granted a role permitting `event.type`; or
  - no current covering assignment grants a role permitting `event.type`.
- Keep role-action evaluation scoped to covering assignments. Multiple covering assignments compose by OR; roles do not grant authority outside their own assignment scope.
- Keep assignment lifecycle commands out of the activity role-action slice. Create/end assignment commands still append immutable `assignment_changed` events through the online authority path; this Phase 4 slice does not change their authorization behavior.

Mobile work:

- Parse and store activity role-action mappings from the config package.
- Hide, disable, or warn for unavailable actions based on current local assignment role and activity.
- Treat all device checks as advisory. The device must not create authoritative flags and must not rely on advisory checks for correctness.

Tests:

- Config package preserves the roles map.
- Unknown actions and empty lists are rejected by DtV.
- Device advisory UI prevents opening/submitting a disallowed review path under current config.
- Server accepts a disallowed pushed event and emits `role_stale`.
- Role-label changes do not flag if both horizon and current roles permit the action.
- Timeline authority uses `min(event.sync_watermark, push.last_pull_watermark)`.
- Two assignments OR permissions only inside their own scopes.
- Activity role-action config rejects `assignment_changed`.
- Assignment lifecycle create/end behavior is unchanged by the activity role-action slice.
- Boundary test proves role-action work does not change `/api/sync/pull` into backfill or audit pull.

#### Assignment Administration Boundary

IDR-023 resolves the earlier assignment lifecycle command gate ambiguity by excluding `assignment_changed` from `activities[*].roles`. IDR-024 then hardens the separate assignment-administration path by deciding multi-axis containment and null-activity semantics.

Assignment lifecycle commands are authority administration. They remain online commands that append immutable `assignment_changed` events, and Phase 4 role-action does not reinterpret them as offline work actions or emit `role_stale` for them. Existing ADR-003 S5 scope-containment and provisioning/bootstrap behavior are hardened by IDR-024 for the create/end command path.

Assignment-administration hardening follows IDR-024: creation containment across geography, subject list, and activity; explicit bootstrap/root authority; ordinary null-activity work not authorized by activity-restricted assignments; and end-assignment target authority. It must not make optional activity the universal authorization anchor.

FP-008 is resolved: ordinary assignment create/end requests bind the acting actor from authenticated request context, not request-body actor IDs. Bootstrap/root provisioning remains a separate explicit path. The unauthenticated HTML admin assignment console is development-only until a production admin/root actor binding exists.

### 4.2 Flag Severity

Server/config work:

- Define platform defaults:

| Flag category | Default severity |
|---------------|------------------|
| `concurrent_state_change` | `blocking` |
| `stale_reference` | `informational` |
| `identity_conflict` | `blocking` |
| `scope_violation` | `blocking` |
| `temporal_authority_expired` | `informational` |
| `role_stale` | `blocking` |
| `domain_uniqueness_violation` | `blocking` |
| `transition_violation` | `informational` |

- Populate `flag_severity_overrides` from L0 deployment config.
- Reject unknown categories, the reserved catalog slot, invalid severity values, and nested per-activity severity objects.
- Compute effective severity as `override(category) ?? default(category)`.
- Keep resolvability fixed by the flag catalog and ADR-005 S3.

Operational behavior:

- `blocking` prevents downstream policy execution and user-facing workflow decisions that depend on the flagged event or its source chain.
- `informational` stays visible in timelines, audit surfaces, and review queues, but does not by itself block unrelated work.
- All unresolved flagged source events remain excluded from authoritative projections regardless of severity.

Tests:

- Valid overrides are delivered atomically to mobile.
- Invalid override keys and values are rejected.
- Per-activity severity config is rejected.
- Changing severity does not change `manual_only` vs `auto_eligible`.
- Projection exclusion remains category-agnostic and severity-independent.

### 4.3 Domain Uniqueness

Shape/config work:

- Activate `shapes[*].uniqueness` as a declarative L1 constraint.
- Support an initial schema with:
  - `scope`: list of platform-understood key dimensions such as `subject_ref`, `activity_ref`, and `payload.<field_name>`;
  - optional `period` with supported calendar period values and deployment timezone behavior;
  - `device_action` as advisory UX only.
- Reject uniqueness definitions that reference unknown fields, non-scalar payload fields, unsupported periods, expression-like constructs, conditionals, or custom containment logic.
- Treat old `action` wording as non-authoritative. Phase 4 config should use `device_action`.

Server detector work:

- During push processing, after identity/lifecycle and authorization/role-action checks, find existing authoritative events with the same normalized uniqueness key and window.
- Exclude unresolved flagged events from the authoritative duplicate basis unless their flag has been resolved as accepted.
- Use identity-normalized subject equivalence for duplicate detection where appropriate, without changing authorization semantics against the original `subject_ref`.
- Accept the incoming event and emit `domain_uniqueness_violation` targeting only that incoming event.
- Emit as `type = "alert"`, `shape_ref = "conflict_detected/v1"`, payload `flag_category = "domain_uniqueness_violation"`, `source_event_id`, `constraint_ref`, target `shape_ref`, target `activity_ref`, normalized/redacted uniqueness key, period/window metadata, visible `conflicting_event_ids`, and detector version.

Mobile work:

- Check uniqueness optimistically against locally synced and locally accepted events.
- Surface `device_action` warnings or confirmation UX.
- Treat device results as advisory only; a missed local warning must not suppress server detection.

Tests:

- DtV rejects invalid uniqueness definitions.
- Device can warn locally, but server still flags a duplicate missed by the device.
- Server accepts and persists the duplicate before emitting the flag.
- The flag targets only the incoming conflicting event.
- Resolving the flag as accepted re-derives projections including that event.
- Domain uniqueness checks do not alter live sync or subject-history backfill behavior.

### 4.4 Pattern Registry and Binding Validation

Registry work:

- Implement a small platform-bundled registry for the initial pattern definitions from `docs/architecture/patterns.md`:
  - `capture_with_review/v1`;
  - `ongoing_resolution/v1`, not implemented or enabled until the FP-005 backfill prerequisite is closed;
  - `multi_step_approval/v1`;
  - `transfer_with_acknowledgment/v1`.
- Keep `entity_lifecycle` deferred unless explicitly promoted. Do not claim S06 support without it.
- Store pattern definitions as canonical platform contract artifacts under `contracts/patterns/`, loaded by the server registry and delivered to devices through the atomic config package. Deployer config references definitions by `ref`; it does not define states or transitions.
- Preserve reviewability as composition: event-level review patterns may overlay subject-level patterns, including `ongoing_resolution`, without consuming the subject-level slot.

Binding validation work:

- Interpret `activities[*].pattern` as a pattern-binding set with nullable `subject` and array `event`.
- Reject more than one subject-level binding per activity.
- Reject duplicate transition-bound shape ownership within an activity.
- Allow event-level overlay patterns through activation-bound shape lists.
- Validate each binding's `composition` matches the platform definition.
- Validate required shape roles, participant roles, parameters, and activity work role-action prerequisites.
- Allow `shape_roles` arrays so old shape versions remain projectable after shape evolution.
- Ensure deprecated-but-known shape versions in `shape_roles` remain valid for projection even when not available for new capture.

Tests:

- Valid binding set packages to server and mobile unchanged.
- Two subject-level bindings in one activity are rejected.
- Duplicate transition-bound shape ownership is rejected.
- Missing required shape roles and participant roles are rejected.
- Deprecated shape versions bound to a shape role project correctly.
- Participant roles mapped to activity work transitions must have the structural actions needed by those transitions.
- Assignment lifecycle transitions consume `assignment_changed` projection facts but are not authorized through `activities[*].roles`.
- `capture_with_review` can overlay events owned by a subject-level pattern without making the subject-level pattern own review state.

### 4.5 Pattern-State Projection

Definition delivery work:

- Use `contracts/pattern-definition.schema.json` and `contracts/patterns/*.json` as the canonical pattern definition contract.
- Package referenced definitions under top-level `pattern_definitions` in the atomic config package.
- Mobile reads packaged definitions from the active config slot; it does not maintain a separate hardcoded runtime registry.
- Keep pattern definitions platform-owned and generated by the platform package path, never accepted from deployer-authored activity config.

Projection work:

- Derive state from event streams plus pattern bindings. Do not write state to event payloads or envelope fields.
- Server and mobile projection engines must use the same state keys:
  - subject-level: `(subject_ref, activity_ref, binding.ref)`;
  - event-level: `(source_event_id, binding.ref)`.
- Exclude unresolved flagged events from state derivation.
- Include events whose flags are resolved as `accepted`.
- Keep rejected events excluded.
- Preserve timeline visibility for all events, including flagged events.
- Compute universal projections: `current_state`, `pending_since`, and `time_in_state`.
- Compute pattern-specific projections where listed in `docs/architecture/patterns.md`.
- Support source-chain flag visibility for downstream events without creating propagated flags.

Initial storage posture:

- Start with on-demand or rebuildable in-process projection structures.
- Do not add a durable workflow-state table in the first implementation unless a measured performance gate proves the ADR-001 B->C escape hatch is needed.

Tests:

- Server/mobile projection equivalence for at least:
  - no-pattern activity;
  - `capture_with_review`;
  - `ongoing_resolution` only after backfill support exists;
  - `multi_step_approval`;
  - `transfer_with_acknowledgment`.
- Unresolved flagged events do not advance `current_state`.
- Resolving as accepted re-derives state including the event.
- Resolving as rejected keeps state unchanged.
- Source-chain contamination is visible in projections without downstream flag creation.

### 4.6 Pattern Transition Detection

Detector work:

- Run after domain uniqueness checks.
- Resolve applicable pattern bindings from `activity_ref`, `shape_ref`, event `type`, and the activity pattern-binding set.
- Derive current state without including the incoming unresolved event.
- Emit `transition_violation` when an event maps to a pattern transition role but the current state does not allow that transition.
- Do not flag events that belong to no pattern binding in the activity.
- Do not reject structurally valid transition violations.
- Emit `transition_violation` as `type = "alert"`, `shape_ref = "conflict_detected/v1"`, payload `flag_category = "transition_violation"`.

Tests:

- Ongoing-resolution contrast: `closed` or `resolved` plus ordinary `interaction` is accepted, flagged, and excluded from state until resolution.
- Shape-evolution: old and new shape versions bound to one role both project.
- Event-level review states derive independently from subject-level state.
- Events inside `ongoing_resolution` remain reviewable through event-level overlays once `ongoing_resolution` is enabled; review state must not be collapsed into the subject-level lifecycle state.
- No new envelope `type` appears.
- Pattern identity never uses event-carried `pattern_ref` or `subject_ref.type = "process"`.
- S06 registry gate remains deferred unless `entity_lifecycle` is promoted; if promoted, `verified` plus update projects to `active` with no `transition_violation`.

### 4.7 Subject-History Backfill for `ongoing_resolution`

This slice must happen before `ongoing_resolution` is implemented or enabled.

Decision required before code:

- Either specify and implement a subject-bound backfill path, or explicitly document that Phase 4 does not support `ongoing_resolution`.
- The preferred Phase 4 direction is a distinct subject-history backfill behavior, not a mutation of normal live pull.

Minimum backfill requirements if implemented:

- Separate request/cursor surface from normal `last_pull_watermark`.
- Does not lower or reuse the normal sync watermark.
- Idempotent pagination with a cursor independent of live sync.
- Request-time authorization on every page.
- Subject alias handling after merge/split.
- Activity filtering compatible with pattern state key `(subject_ref, activity_ref, binding.ref)`.
- Includes assignment/transfer events needed by subject-level projections where the actor is authorized to receive them.
- Does not become audit/historical pull for arbitrary scopes or actors.

Tests:

- Existing live contraction test stays green: after reassignment away, normal pull does not deliver new events from old scope.
- Actor synced past watermark 500 receives a new `subject_list` assignment for a subject whose relevant events are watermarks 100-200; the backfill path returns the subject history needed for pattern state derivation.
- Backfill page authorization is evaluated at request time.
- Alias chains are included correctly after merge/split.
- Backfill cursor retries are idempotent.
- Normal sync watermark is unchanged by backfill.
- Audit-style broad historical reconstruction remains unavailable unless a successor decision adds it.

---

### 4.9 Multi-axis Assignment Containment

This slice hardens the online assignment-administration path decided by IDR-024. It must not be implemented as activity role-action enforcement.

Server work:

- Revise `AssignmentService.createAssignment(...)` containment to evaluate `geographic`, `subject_list`, and `activity` together against a single covering creator assignment.
- Reject requested unrestricted axes (`null`) unless the covering creator assignment is unrestricted on the same axis or explicit bootstrap/root authority is present.
- Validate subject-list and activity subsets when the covering creator assignment is restricted on those axes.
- Treat empty subject/activity arrays as invalid or empty grants, never as unrestricted scope.
- Make bootstrap/root authority explicit and bounded so an arbitrary actor with no active assignments is not production root authority.
- Revise ordinary work-event activity checks so `activity_ref = null` is not authorized by activity-restricted assignments.
- Keep platform/system/identity/assignment events classified separately from ordinary activity work events.
- Revise `endAssignment(...)` authorization so ending an assignment requires target-assignment authority or explicit bootstrap/root authority.

Tests:

- Restricted geographic creator cannot create a geographically unrestricted assignment.
- Restricted subject-list creator cannot create subject-unrestricted or outside-subject assignments.
- Restricted activity creator cannot create activity-unrestricted or outside-activity assignments.
- Subject-list-only creator with `geographic = null` is not treated as root/admin.
- Explicit bootstrap/root path can create broad initial authority, while an arbitrary no-assignment actor cannot.
- Activity-restricted assignments do not authorize ordinary null-activity work events.
- `assignment_changed` remains invalid in `activities[*].roles`.
- Ending an assignment outside the actor's target-assignment authority is rejected.

---

## 6. Detection Ordering

Phase 4 push processing order:

1. Structural envelope and shape validation before persistence.
2. Identity/lifecycle checks.
3. Authorization checks, including IDR-021 role-action checks.
4. Domain uniqueness checks.
5. IDR-020 pattern transition checks.

The detector may emit multiple independent flags for one event. Later checks must not derive state from an event that already has an unresolved prior flag in the same processing pass.

---

## 7. Deliverables

### Server

- `DeployTimeValidator`: role-action validation, severity override validation, uniqueness validation, pattern-binding validation.
- `ConfigPackager`: populated `flag_severity_overrides`; active `shapes[*].uniqueness`; pattern binding sets; referenced `pattern_definitions`; unchanged role-action maps.
- `ConflictDetector`: revised `role_stale`, new `domain_uniqueness_violation`, new `transition_violation`, enforced detection ordering.
- `ConflictResolutionService` / projection reads: severity-aware operational surfaces while keeping resolvability fixed.
- `PatternRegistry`: contract-backed platform definitions and generic transition matcher.
- `SubjectProjection` or a workflow projection component: flag-aware state derivation.
- `AssignmentService` / `ActiveAssignment`: IDR-024 multi-axis assignment create/end containment and ordinary null-activity work-event semantics.
- `SyncController`: preserve request-time live pull; add a distinct subject-history backfill surface only after the FP-005 decision is recorded.
- Tests for each quality gate below.

### Mobile

- Config parser/store for role-action maps, severity overrides, uniqueness constraints, pattern bindings, and packaged pattern definitions.
- Advisory role-action gating.
- Advisory uniqueness warnings from local accepted data.
- Pattern-state projection using the same fixtures as server.
- Advisory pattern transition warnings/command validation.
- Context resolver updates for `context.subject_state`, `context.subject_pattern`, and `context.activity_stage` once pattern projection exists.
- Tests matching server fixtures where practical.

### Contracts and Docs

- Keep `contracts/flag-catalog.md` aligned with categories 7 and 8. Category pointers to IDR-022/IDR-020 are present; resolver routing stays `TBD` until a dedicated resolver-routing decision or implementation slice lands. [FP-009](../../flagged-positions.md#fp-009--conflict-resolver-designation-and-single-writer-resolution-enforcement) tracks the ADR-002 S11 single-writer resolver gate and blocks resolver-dependent slices.
- Keep `contracts/pattern-definition.schema.json` and `contracts/patterns/*.json` aligned with IDR-025 and `docs/architecture/patterns.md`.
- Update assignment scope contract/tests during IDR-024 implementation if needed so empty subject/activity arrays cannot be interpreted as unrestricted.
- Add or update shared projection/flag fixtures for role-action, uniqueness, severity, and pattern state.
- Update `docs/status.md` only when implementation begins or lands; the existence of this spec alone does not make Phase 4 started.

---

## 8. Quality Gates

Phase 4 is not complete until every applicable gate is green.

### Role-Action Gates

- [x] Config packaging preserves `activities[*].roles`.
- [x] Device advisory behavior prevents or warns on disallowed actions without replacing server detection.
- [x] Server accepts action-disallowed events and emits `role_stale`.
- [x] Role label changes do not flag when both horizon and current roles permit the action.
- [x] Role-action timeline uses `min(event.sync_watermark, push.last_pull_watermark)`.
- [x] Multiple assignments OR only across covering scopes.
- [x] `activities[*].roles` rejects `assignment_changed`.
- [x] Assignment lifecycle create/end behavior is not silently changed by the activity role-action slice.
- [x] `/api/sync/pull` remains normal live sync, not backfill or audit pull.
- [x] FP-006 is resolved: superseded ended assignments do not produce false `temporal_authority_expired` after the actor has synced replacement authority.

### Assignment Administration Gates

- [x] Assignment creation evaluates containment across `geographic`, `subject_list`, and `activity`.
- [x] Requested unrestricted axes require matching unrestricted creator authority or explicit bootstrap/root authority.
- [x] Requested subject-list and activity values must be subsets of the covering creator assignment when the creator is restricted on that axis.
- [x] Containment uses one covering creator assignment, not an implicit union across separate assignments.
- [x] Subject-list-only assignments do not imply root/admin authority.
- [x] Bootstrap/root authority is explicit and bounded; an arbitrary actor with no active assignments cannot create broad production authority.
- [x] Ordinary null-activity work events are not authorized by activity-restricted assignments.
- [x] Assignment ending requires target-assignment authority or explicit bootstrap/root authority.
- [x] `assignment_changed` remains excluded from `activities[*].roles`.
- [x] Ordinary `/api/assignments` create/end commands use authenticated request actor context, not request-body actor IDs.
- [x] Spoofed `creator_actor_id` / `actor_id` fields do not grant assignment authority.
- [x] Ordinary assignment commands cannot reach the explicit initial bootstrap path by spoofing request-body actor fields.
- [x] HTML admin assignment commands are documented as development-only until production admin/root actor binding exists.

### Severity Gates

- [x] `flag_severity_overrides` accepts only known categories and `blocking`/`informational`.
- [x] Reserved category and nested per-activity severity config are rejected.
- [x] Changing severity does not change `manual_only` vs `auto_eligible`.
- [x] Unresolved flagged source events stay excluded from authoritative projections regardless of severity.

### Domain Uniqueness Gates

- [x] DtV rejects unknown fields, non-scalar fields, unsupported periods, and expression-like uniqueness definitions.
- [x] Device uniqueness checks are advisory only.
- [x] Server accepts, persists, and flags duplicates.
- [x] `domain_uniqueness_violation` targets only the incoming conflicting event.
- [x] Resolving the flag as accepted re-derives projections including the event.
- [x] Uniqueness implementation does not alter normal live sync or FP-005 backfill behavior.

### Pattern Gates

- [x] Pattern binding metadata is platform-bundled, not deployer-authored.
- [x] One subject-level binding per activity is enforced.
- [x] Duplicate transition-bound shape ownership is rejected.
- [x] Binding composition must match the platform pattern definition.
- [x] Missing required shape roles and participant roles are rejected.
- [x] Participant role-action prerequisites are checked for activity work transitions.
- [x] Deprecated-but-known shape versions bound to one `shape_role` remain valid in bindings/packages for future projection.
- [x] Pattern definitions have canonical contract files under `contracts/patterns/`.
- [x] Pattern contract files validate against `contracts/pattern-definition.schema.json`.
- [x] Referenced pattern definitions are delivered in the atomic config package.
- [x] Mobile preserves packaged pattern definitions from the active config slot.
- [x] Unresolved flagged events do not advance pattern state.
- [x] Resolving a flag as accepted re-derives state including the event.
- [ ] `transition_violation` emits with `type = "alert"` and `shape_ref = "conflict_detected/v1"`.
- [x] No new envelope type or envelope field is introduced.
- [x] Pattern identity never uses `subject_ref.type = "process"`.
- [ ] `entity_lifecycle` is implemented before S06 support is claimed, or S06 remains explicitly deferred.

### FP-005 Gates

- [ ] Live contraction stays request-time scoped.
- [ ] Subject-history backfill is decided and tested before `ongoing_resolution` is implemented or enabled.
- [ ] Backfill cursor behavior is idempotent and independent of the normal sync watermark.
- [ ] Backfill authorization is evaluated at request time for every page.
- [ ] Alias handling and activity filtering are covered by tests.
- [ ] Audit/historical pull remains out of Phase 4 live sync unless a successor decision creates a separate pull class/API.

### Scenario-Grade Responsibility Binding Gates

- [ ] P04 Responsibility Binding has a scenario-grade reassignment campaign test or shared fixture covering S03/S09/S20 scale: coordinated campaign work, overlapping responsibility areas, mid-campaign reassignment, scope-filtered sync after reassignment, and role-action authority across the reassignment boundary. Existing primitive assignment and live-contraction tests do not close this gate.

### Regression Gates

- [x] Existing Phase 0-3e server tests still pass.
- [x] Existing Phase 0-3e mobile tests still pass.
- [x] Shared server/mobile projection fixtures pass for Phase 4 pattern state.
- [x] `git diff --check` passes.

---

## 9. Sequencing

Phase 4.1 role-action server enforcement and mobile advisory behavior has landed. Phase 4.2 flag severity has landed. IDR-024/FP-007 assignment containment hardening and FP-008 assignment command identity binding have landed. Phase 4.3 domain uniqueness has landed. Phase 4.4 registry/binding validation and IDR-025 pattern definition delivery have landed. Phase 4.5 enabled-binding pattern-state projection has landed without `ongoing_resolution`, `transition_violation`, resolver routing, normal sync backfill, or durable workflow-state tables. Recommended remaining implementation order:

1. FP-005 subject-history backfill decision and implementation.
2. Enable `ongoing_resolution` projection only after FP-005 is resolved.
3. Add pattern transition detection and mobile advisory transition warnings after the FP-009 resolver gate is addressed.
4. Add the scenario-grade P04 Responsibility Binding reassignment campaign gate before Phase 4 close-out.
5. Close docs/status/catalog updates and Phase 4 completion audit.

This order keeps landed role-action, severity, assignment-administration, uniqueness, registry, and enabled-binding projection work independent of FP-005, and prevents `ongoing_resolution` from landing before the subject-history backfill gap is closed. The P04 scenario-grade campaign gate is required before Phase 4 completion, but it does not block remaining workflow-policy slices unless implementation uncovers a direct dependency.

---

## 10. Reversibility Triage

| Decision / Workstream | Bucket | Why |
|-----------------------|--------|-----|
| Role-action enforcement | Execution of IDR-021 | Semantics are active; implementation can refactor without stored-data migration. |
| Flag severity overrides | Execution of IDR-022 | Flat L0 config, no stored-event change. |
| Assignment containment hardening | Execution of IDR-024 | Server-side command and containment semantics; no stored-event change, but security-sensitive. |
| Domain uniqueness constraints | Execution of IDR-022 | Shape config plus flag emission; accepted events remain immutable. |
| Pattern Registry definitions | Execution of IDR-020 / ADR-005 | Platform-bundled pattern inventory can grow by platform release; no deployer-authored state machines. |
| Pattern Definition Contract and Delivery | Execution of IDR-025 / ADR-005 / ADR-009 | Cross-device contract; high reversal once server/mobile projection consumes packaged definitions. |
| Pattern-state projection | Lean until B->C table | Rebuildable derived state; no durable authority table initially. |
| Subject-history backfill API | Lean/Medium | Separate sync surface. Reversible before deployed clients depend on it; must be specified carefully because cursor semantics become client-visible. |
| Durable workflow-state projection table | Deferred B->C | Only allowed after measured read-cost pressure. |

---

## 11. Risks & Mitigations

| Risk | Likelihood | Mitigation |
|------|:---:|------------|
| `ongoing_resolution` lands without history backfill and produces wrong state for newly assigned long-running subjects. | High if unchecked | Gate all `ongoing_resolution` implementation and enablement behind FP-005 backfill decision/tests. |
| Role-action enforcement accidentally uses current role only. | Medium | Timeline tests must prove horizon authority uses `min(event.sync_watermark, push.last_pull_watermark)`. |
| Severity gets conflated with resolvability. | Medium | Dedicated tests prove severity changes do not alter `manual_only` / `auto_eligible`. |
| `device_action` for uniqueness becomes server policy. | Medium | Server detector tests assert accept-and-flag regardless of device hint. |
| Pattern transition detection includes the incoming unresolved event in current state. | Medium | Ordering gate and transition tests derive current state before applying the incoming event. |
| Pattern binding grows into deployer-authored state machines. | Medium | DtV accepts only refs, role bindings, shape-role arrays, and parameters; no transition tables in L0 config. |
| Backfill becomes an audit API by accident. | Medium | FP-005 tests restrict it to authorized subject-bound history and keep normal live pull request-time scoped. |
| Geography-only assignment containment lets restricted coordinators mint broader subject/activity authority. | High until fixed | IDR-024 gates require all-axis containment, explicit bootstrap/root authority, and end-assignment target authority before assignment administration is complete. |
| Workflow projection rebuild becomes slow on low-end devices. | Medium | Start rebuildable/on-demand; use ADR-001 B->C only after measured threshold is exceeded. |

---

## 12. Journal

- **2026-05-22**: Spec drafted after Phase 4 prep through IDR-022; initial entry preceded code work.
- **2026-05-22**: Phase 4.1 role-action server enforcement and mobile advisory role-action gating landed. Phase 4 remains in progress.
- **2026-05-22**: Recorded missing scenario-grade P04 Responsibility Binding reassignment campaign gate for S03/S09/S20 scale coverage.
- **2026-05-22**: Phase 4.2 flag severity landed: platform defaults, deployment-wide L0 `flag_severity_overrides`, validation/package delivery, server/mobile effective severity interpretation, fixed resolvability preservation, and severity-independent projection exclusion tests.
- **2026-05-22**: IDR-024/FP-007 recorded the multi-axis assignment containment gap and routed assignment-administration hardening before the remaining Phase 4 workflow-policy slices.
- **2026-05-23**: Phase 4.3 domain uniqueness landed: `shapes[*].uniqueness` validation/package preservation, server `domain_uniqueness_violation` accept-and-flag detection after identity/auth checks, unresolved-flag exclusion from the duplicate basis, accepted-resolution re-inclusion, and mobile advisory duplicate checks. FP-005 remains open and `ongoing_resolution` remains unimplemented.
- **2026-05-24**: Phase 4.4 pattern registry and binding validation landed: platform-bundled binding metadata for `capture_with_review/v1`, `ongoing_resolution/v1`, `multi_step_approval/v1`, and `transfer_with_acknowledgment/v1`; deploy-time validation for pattern refs, composition, subject/event binding shape, required shape/participant roles, parameters, participant role-action prerequisites, and duplicate transition-bound shape ownership; config package and mobile raw binding preservation. Executable transition specs remain Phase 4.5 work. `ongoing_resolution/v1` remains registered but disabled until FP-005 closes. FP-009 remains open and no `transition_violation`, resolver routing, conflict-resolution authority enforcement, or auto-resolution was implemented.
- **2026-05-24**: IDR-025 pattern definition contract/delivery landed: `contracts/pattern-definition.schema.json`, canonical `contracts/patterns/*.json`, server registry loading from packaged contract resources, config package `pattern_definitions` delivery for referenced refs, and mobile packaged-definition preservation. No pattern-state projection, `transition_violation`, resolver routing, conflict-resolution authority enforcement, or auto-resolution was implemented.
- **2026-05-24**: Phase 4.5 enabled-binding pattern-state projection landed for `capture_with_review/v1`, `multi_step_approval/v1`, and `transfer_with_acknowledgment/v1`: server `PatternStateProjection` rebuilds on demand from active activity bindings and contract-backed pattern definitions; mobile `PatternProjectionEngine` reads ConfigStore bindings and packaged `pattern_definitions`; shared fixture coverage now spans no-pattern, enabled patterns, unresolved flag exclusion, accepted re-inclusion, and rejected exclusion. `ongoing_resolution/v1`, normal sync backfill, `transition_violation`, resolver routing, conflict-resolution authority enforcement, auto-resolution, and durable workflow-state tables remain unimplemented.
