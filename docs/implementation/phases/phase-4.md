# Phase 4: Workflow & Policies

> Implementation-grade plan for Phase 4 after the Phase 4.0 rollback and the IDR-020/021/022 prep pass. This spec authorizes Phase 4 implementation sequencing, but it does not itself start code work.

**Exercises**: [IDR-020](../../decisions/idr-020-pattern-state-machine-representation.md) (Pattern State Machine Representation), [IDR-021](../../decisions/idr-021-role-action-enforcement-model.md) (Role-Action Enforcement Model), [IDR-022](../../decisions/idr-022-flag-severity-and-domain-uniqueness.md) (Flag Severity + Domain Uniqueness), [ADR-003](../../adrs/adr-003-authorization-sync.md) S1/S3/S5/S7, [ADR-004](../../adrs/adr-004-configuration-boundary.md) S6/S7/S9/S14, [ADR-005](../../adrs/adr-005-state-progression.md) S1-S9, and [ADR-006](../../adrs/adr-006-flag-semantics.md) S1-S4.

**Primitives touched**: Config Package, Deploy-Time Validator, Shape Registry, Pattern Registry, Projection Engine, Conflict Detector, Conflict Resolution, Assignment Authority Projection, Sync Pull, Mobile Config Store, Mobile Projection Engine, Mobile Advisory Validators.

**Not introducing new envelope fields. Not introducing new envelope types. Not introducing deployer-authored state machines. Not changing normal live sync into historical pull.**

---

## 1. Why Phase 4 Exists

Phase 3 completed configuration delivery, shape validation, expressions, device context resolution, envelope-type closure, and the current identity/authorization flag pipeline. Phase 4 adds the first workflow and policy behavior that depends on those surfaces:

- activity-scoped role-action enforcement from `activities[*].roles`;
- projection-derived workflow pattern state from platform-bundled pattern definitions;
- `transition_violation` detection for invalid pattern transitions;
- deployment-wide flag severity config through `flag_severity_overrides`;
- shape-declared domain uniqueness detection through `shapes[*].uniqueness`;
- device advisory UX for role-action, uniqueness, and pattern-state warnings;
- subject-history backfill required before long-running `ongoing_resolution` subjects can be safely assigned to actors who have already advanced their normal sync watermark past the subject's historical events.

Phase 4 is not a general policy engine. It activates already-decided Phase 4 surfaces while preserving the invariants from ADR-001 through ADR-006: append-only events, accept-and-flag for state anomalies, authority as projection, sync scope equals access scope, closed envelope type vocabulary, and state as projection.

---

## 2. Contradiction Pass

The following constraints are load-bearing. Phase 4 implementation must fail review if it contradicts any item.

### FP-005 Boundary

FP-005 remains `IN_PROGRESS` and routed, not resolved. Phase 4 must implement its route exactly:

1. Normal `/api/sync/pull` remains request-time scoped against current active assignments. It must not become historical reconstruction, subject-history replay, or audit pull.
2. `ongoing_resolution` cannot be implemented for already-active long-running subjects until subject-history backfill is specified and tested.
3. Subject-history backfill is separate from role-action enforcement, flag severity, domain uniqueness, and pattern transition logic.
4. Audit/historical pull is out of Phase 4 live sync. If audit reconstruction is needed later, it requires a successor decision and a separate pull class/API.
5. The backfill design must define idempotence/cursor behavior without lowering the normal sync watermark, request-time authorization on every page, alias handling after merge/split, activity filtering for pattern state keys, and visibility of assignment/transfer events needed by subject-level projections.

### IDR-020 Boundary

- Pattern definitions are platform-bundled; deployers bind shapes, participant roles, and parameters only.
- No event stores `current_state`, `pattern_ref`, or any workflow-state field.
- Pattern state identity is `(subject_ref, activity_ref, binding.ref)` for subject-level patterns and `(source_event_id, binding.ref)` for event-level patterns.
- Phase 4 must not use `subject_ref.type = "process"` for pattern instances.
- `transition_violation` is accept-and-flag: structurally valid events are persisted and flagged, never rejected for transition reasons.
- Unresolved flagged events remain excluded from pattern-state derivation.
- `entity_lifecycle` is separate from `ongoing_resolution`. Scenario 06 support requires implementing `entity_lifecycle` or explicitly deferring S06; it must not be approximated by `ongoing_resolution`.

### IDR-021 Boundary

- Role-action permissions are activity-scoped L0 config in `activities[*].roles`.
- The action vocabulary for Phase 4 is the closed six envelope `type` values: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`.
- Server-side role-action checks are authoritative; mobile checks are advisory UX only.
- Offline-pushed work is accepted and flagged as `role_stale` when horizon authority or current authority does not permit the attempted action.
- `role_stale` means action-authority mismatch, not any role-label change.
- Role-action enforcement must not alter `/api/sync/pull`, subject-history backfill, or audit pull behavior.

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
| 4.1 | Role-action config validation, packaging, mobile parsing, advisory device gating, and authoritative server `role_stale` semantics | IDR-021 | Policy enforcement |
| 4.2 | Flag severity defaults and deployment-wide `flag_severity_overrides` validation, packaging, server/mobile interpretation | IDR-022 | Config + workflow gating |
| 4.3 | Shape uniqueness schema, DtV validation, advisory device duplicate warnings, server-side `domain_uniqueness_violation` detector | IDR-022 | Shape policy |
| 4.4 | Platform Pattern Registry and activity pattern binding validation | IDR-020 | Workflow registry |
| 4.5 | Server and mobile pattern-state projection, including unresolved-flag exclusion and state re-derivation after resolution | IDR-020 / ADR-005 | Projection |
| 4.6 | Server `transition_violation` detector in the push pipeline | IDR-020 / ADR-005 | Conflict detection |
| 4.7 | Subject-history backfill decision and implementation before `ongoing_resolution` support | FP-005 / IDR-020 | Sync support |
| 4.8 | Phase 4 shared fixtures and quality gates spanning server/mobile equivalence | IDR-020/021/022 | Test infrastructure |

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
- `entity_lifecycle` implementation unless explicitly promoted by a Phase 4 sub-scope before S06 support is claimed.
- General trigger engine or auto-resolution implementation unless a later Phase 4 addendum explicitly schedules it. `transition_violation` remains `auto_eligible`, but auto-resolution policy execution is not required for the first Phase 4 workflow slice.

---

## 5. Implementation Slices

### 4.1 Role-Action Enforcement

Server work:

- Validate `activities[*].roles` as `{role: [action...]}` where each role is non-empty and each action is one of the six closed envelope `type` values.
- Reject empty action lists and unknown action names at deploy time.
- Preserve `activities[*].roles` unchanged in the config package.
- Revise `ConflictDetector` role-action logic so `role_stale` is emitted when either:
  - no covering assignment at `min(event.sync_watermark, push.last_pull_watermark)` granted a role permitting `event.type`; or
  - no current covering assignment grants a role permitting `event.type`.
- Keep role-action evaluation scoped to covering assignments. Multiple covering assignments compose by OR; roles do not grant authority outside their own assignment scope.
- Keep assignment mutation online: `assignment_changed` requires role-action permission and ADR-003 S5 scope containment.

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
- Assignment creation checks both role-action permission and scope containment.
- Boundary test proves role-action work does not change `/api/sync/pull` into backfill or audit pull.

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
  - `ongoing_resolution/v1`, gated behind the FP-005 backfill prerequisite;
  - `multi_step_approval/v1`;
  - `transfer_with_acknowledgment/v1`.
- Keep `entity_lifecycle` deferred unless explicitly promoted. Do not claim S06 support without it.
- Store pattern definitions in platform code or bundled resources. Deployer config references definitions by `ref`; it does not define states or transitions.

Binding validation work:

- Interpret `activities[*].pattern` as a pattern-binding set with nullable `subject` and array `event`.
- Reject more than one subject-level binding per activity.
- Reject duplicate transition-bound shape ownership within an activity.
- Allow event-level overlay patterns through activation-bound shape lists.
- Validate each binding's `composition` matches the platform definition.
- Validate required shape roles, participant roles, parameters, and role-action prerequisites.
- Allow `shape_roles` arrays so old shape versions remain projectable after shape evolution.
- Ensure deprecated-but-known shape versions in `shape_roles` remain valid for projection even when not available for new capture.

Tests:

- Valid binding set packages to server and mobile unchanged.
- Two subject-level bindings in one activity are rejected.
- Duplicate transition-bound shape ownership is rejected.
- Missing required shape roles and participant roles are rejected.
- Deprecated shape versions bound to a shape role project correctly.
- Participant roles mapped to transitions must have the structural actions needed by those transitions.

### 4.5 Pattern-State Projection

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
  - `ongoing_resolution` after backfill support exists;
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
- No new envelope `type` appears.
- Pattern identity never uses event-carried `pattern_ref` or `subject_ref.type = "process"`.
- S06 registry gate remains deferred unless `entity_lifecycle` is promoted; if promoted, `verified` plus update projects to `active` with no `transition_violation`.

### 4.7 Subject-History Backfill for `ongoing_resolution`

This slice must happen before `ongoing_resolution` is enabled for already-active long-running subjects.

Decision required before code:

- Either specify and implement a subject-bound backfill path, or explicitly document that Phase 4 does not support assigning already-active long-running subjects to actors whose normal sync watermark has passed the subject's historical events.
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
- `ConfigPackager`: populated `flag_severity_overrides`; active `shapes[*].uniqueness`; pattern binding sets; unchanged role-action maps.
- `ConflictDetector`: revised `role_stale`, new `domain_uniqueness_violation`, new `transition_violation`, enforced detection ordering.
- `ConflictResolutionService` / projection reads: severity-aware operational surfaces while keeping resolvability fixed.
- `PatternRegistry`: platform-bundled definitions and generic transition matcher.
- `SubjectProjection` or a workflow projection component: flag-aware state derivation.
- `SyncController`: preserve request-time live pull; add a distinct subject-history backfill surface only after the FP-005 decision is recorded.
- Tests for each quality gate below.

### Mobile

- Config parser/store for role-action maps, severity overrides, uniqueness constraints, and pattern bindings.
- Advisory role-action gating.
- Advisory uniqueness warnings from local accepted data.
- Pattern-state projection using the same fixtures as server.
- Advisory pattern transition warnings/command validation.
- Context resolver updates for `context.subject_state`, `context.subject_pattern`, and `context.activity_stage` once pattern projection exists.
- Tests matching server fixtures where practical.

### Contracts and Docs

- Update `contracts/flag-catalog.md` during implementation to point categories 7 and 8 at IDR-022/IDR-020 and record final resolver routing.
- Add or update shared projection/flag fixtures for role-action, uniqueness, severity, and pattern state.
- Update `docs/status.md` only when implementation begins or lands; the existence of this spec alone does not make Phase 4 started.

---

## 8. Quality Gates

Phase 4 is not complete until every applicable gate is green.

### Role-Action Gates

- [ ] Config packaging preserves `activities[*].roles`.
- [ ] Device advisory behavior prevents or warns on disallowed actions without replacing server detection.
- [ ] Server accepts action-disallowed events and emits `role_stale`.
- [ ] Role label changes do not flag when both horizon and current roles permit the action.
- [ ] Role-action timeline uses `min(event.sync_watermark, push.last_pull_watermark)`.
- [ ] Multiple assignments OR only across covering scopes.
- [ ] `assignment_changed` requires role-action permission plus scope containment.
- [ ] `/api/sync/pull` remains normal live sync, not backfill or audit pull.

### Severity Gates

- [ ] `flag_severity_overrides` accepts only known categories and `blocking`/`informational`.
- [ ] Reserved category and nested per-activity severity config are rejected.
- [ ] Changing severity does not change `manual_only` vs `auto_eligible`.
- [ ] Unresolved flagged source events stay excluded from authoritative projections regardless of severity.

### Domain Uniqueness Gates

- [ ] DtV rejects unknown fields, non-scalar fields, unsupported periods, and expression-like uniqueness definitions.
- [ ] Device uniqueness checks are advisory only.
- [ ] Server accepts, persists, and flags duplicates.
- [ ] `domain_uniqueness_violation` targets only the incoming conflicting event.
- [ ] Resolving the flag as accepted re-derives projections including the event.
- [ ] Uniqueness implementation does not alter normal live sync or FP-005 backfill behavior.

### Pattern Gates

- [ ] Pattern definitions are platform-bundled, not deployer-authored.
- [ ] One subject-level binding per activity is enforced.
- [ ] Duplicate transition-bound shape ownership is rejected.
- [ ] Deprecated-but-known shape versions bound to one `shape_role` remain projectable.
- [ ] Unresolved flagged events do not advance pattern state.
- [ ] Resolving a flag as accepted re-derives state including the event.
- [ ] `transition_violation` emits with `type = "alert"` and `shape_ref = "conflict_detected/v1"`.
- [ ] No new envelope type or envelope field is introduced.
- [ ] Pattern identity never uses `subject_ref.type = "process"`.
- [ ] `entity_lifecycle` is implemented before S06 support is claimed, or S06 remains explicitly deferred.

### FP-005 Gates

- [ ] Live contraction stays request-time scoped.
- [ ] Subject-history backfill is decided and tested before `ongoing_resolution` is enabled for already-active subjects.
- [ ] Backfill cursor behavior is idempotent and independent of the normal sync watermark.
- [ ] Backfill authorization is evaluated at request time for every page.
- [ ] Alias handling and activity filtering are covered by tests.
- [ ] Audit/historical pull remains out of Phase 4 live sync unless a successor decision creates a separate pull class/API.

### Regression Gates

- [ ] Existing Phase 0-3e server tests still pass.
- [ ] Existing Phase 0-3e mobile tests still pass.
- [ ] Shared server/mobile projection fixtures pass for Phase 4 pattern state.
- [ ] `git diff --check` passes.

---

## 9. Sequencing

Recommended implementation order:

1. Config validation/package foundations: role-action validation, severity overrides, uniqueness schema validation, pattern binding validation.
2. Role-action server enforcement and mobile advisory behavior.
3. Flag severity interpretation and tests.
4. Domain uniqueness detector and mobile advisory uniqueness.
5. Pattern Registry and binding validation.
6. Pattern-state projection without `ongoing_resolution` enabled for already-active subject assignment.
7. FP-005 subject-history backfill decision and implementation.
8. Enable `ongoing_resolution` projection and transition detection.
9. Add remaining pattern detectors and mobile advisory transition warnings.
10. Close docs/status/catalog updates and Phase 4 completion audit.

This order keeps the security-sensitive role-action work independent of FP-005, activates severity and uniqueness before pattern transitions need ordered flag behavior, and prevents `ongoing_resolution` from landing before the subject-history backfill gap is closed.

---

## 10. Reversibility Triage

| Decision / Workstream | Bucket | Why |
|-----------------------|--------|-----|
| Role-action enforcement | Execution of IDR-021 | Semantics are active; implementation can refactor without stored-data migration. |
| Flag severity overrides | Execution of IDR-022 | Flat L0 config, no stored-event change. |
| Domain uniqueness constraints | Execution of IDR-022 | Shape config plus flag emission; accepted events remain immutable. |
| Pattern Registry definitions | Execution of IDR-020 / ADR-005 | Platform-bundled pattern inventory can grow by platform release; no deployer-authored state machines. |
| Pattern-state projection | Lean until B->C table | Rebuildable derived state; no durable authority table initially. |
| Subject-history backfill API | Lean/Medium | Separate sync surface. Reversible before deployed clients depend on it; must be specified carefully because cursor semantics become client-visible. |
| Durable workflow-state projection table | Deferred B->C | Only allowed after measured read-cost pressure. |

---

## 11. Risks & Mitigations

| Risk | Likelihood | Mitigation |
|------|:---:|------------|
| `ongoing_resolution` lands without history backfill and produces wrong state for newly assigned long-running subjects. | High if unchecked | Gate `ongoing_resolution` behind FP-005 backfill decision/tests. |
| Role-action enforcement accidentally uses current role only. | Medium | Timeline tests must prove horizon authority uses `min(event.sync_watermark, push.last_pull_watermark)`. |
| Severity gets conflated with resolvability. | Medium | Dedicated tests prove severity changes do not alter `manual_only` / `auto_eligible`. |
| `device_action` for uniqueness becomes server policy. | Medium | Server detector tests assert accept-and-flag regardless of device hint. |
| Pattern transition detection includes the incoming unresolved event in current state. | Medium | Ordering gate and transition tests derive current state before applying the incoming event. |
| Pattern binding grows into deployer-authored state machines. | Medium | DtV accepts only refs, role bindings, shape-role arrays, and parameters; no transition tables in L0 config. |
| Backfill becomes an audit API by accident. | Medium | FP-005 tests restrict it to authorized subject-bound history and keep normal live pull request-time scoped. |
| Workflow projection rebuild becomes slow on low-end devices. | Medium | Start rebuildable/on-demand; use ADR-001 B->C only after measured threshold is exceeded. |

---

## 12. Journal

- **2026-05-22**: Spec drafted after Phase 4 prep through IDR-022. No implementation started.
