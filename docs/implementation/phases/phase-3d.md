# Phase 3d: Phase 3 Close-Out

> Close the Phase 3 carried debt before Phase 4 begins. Four narrow deliverables: finish one unmet 3a quality gate, surface two decided-but-empty config fields, verify one thin-evidence resolver, refresh the codebase map. No new architectural scope.

**Exercises**: ADR-4 S2 (`activity_ref` population), ADR-4 S8 (sensitivity surface), ADR-5 S8 (`context.*` resolution). Contracts C5, C14, C20 (all previously claimed; verifying and completing surface). No new IDs, no new primitives.

**Primitives touched**: Config Packager (adds `sensitivity_classifications` surface), Expression Evaluator (completes `context.*` resolver), Event assembler on device (auto-`activity_ref`).

---

## 1. Why Phase 3d Exists

Phase 3c was declared complete on 2026-04-13 (test count 153 + 54). A cross-reference audit on 2026-04-21 (see [checkpoints/](../../checkpoints/) and session audit notes) found:

- **One Phase 3a quality gate was never met** and was punted into Phase 4.0. Phase 4.0 was rolled back because IDR-020 violated architecture. The rollback removed the code without reopening the Phase 3 gate. The gate has been silently open for ~8 days.
- **Two Phase 3 config-package surfaces** (`flag_severity_overrides`, `sensitivity_classifications`) ship as permanent empty maps. One is legitimately Phase 4 territory (severity), the other (sensitivity) is Phase 3 debt.
- **One Phase 3b quality gate** (context.* resolution for `event_count`, `days_since_last_event`, `actor.role`, `actor.scope_name`) passed by manual inspection, not test.
- **One Phase 3a quality gate** (`activity_ref` auto-population) passed by manual inspection, not test.
- **CLAUDE.md drifted** — claimed 157 tests + rolled-back role-action code still present.

Phase 3d is the narrowest possible slice that closes these. It is **not** a place to design role-action enforcement (that is IDR-021, post-3d) or pattern state machines (IDR-020 rewrite, post-3d). It closes honest debt only.

---

## 2. What's In Scope

| # | Item | Audit code | Closes | Kind |
|---|------|-----------|--------|------|
| 3d.1 | `activity_ref` auto-populated in device event assembler when form is opened within an activity; add mobile test | D2 | Phase 3a QG | Bug fix + test |
| 3d.2 | `sensitivity_classifications` emitted by `ConfigPackager` from `shapes.sensitivity` + `activities.sensitivity`; device parses and stores. No enforcement. | D4 | ADR-4 S8 surface | Wire-format completion |
| 3d.3 | `context.*` resolver: implement or verify `event_count`, `days_since_last_event`, `actor.role`, `actor.scope_name` resolution from device projection at form-open. Add test fixture. | D6 | Phase 3b QG (C5) | Verification + fix |
| 3d.4 | Refresh [CLAUDE.md](../../../CLAUDE.md) codebase map: test counts, no role-action claims, accurate file list | D7 | Agent contract | Docs |

## 3. What's Explicitly Out of Scope

- **Role-action enforcement** (D1). Requires IDR-021. Architectural question, not a surface completion. Split from IDR-020.
- **`flag_severity_overrides` implementation** (D3). Requires IDR-022 (deployer authoring surface for severity). Defer with IDR-022.
- **`domain_uniqueness_violation`** (D5). Requires IDR-022 (shape-declared uniqueness CD path).
- **Command Validator** (D8). Phase 4 advisory; no Phase 3 obligation.
- **Pattern State Machines** (IDR-020 rewrite). Separate work item.

These are tracked as open debt carried into Phase 4 prep.

---

## 4. Design Decisions

Three small Lean/Leaf decisions. No Locks. No IDRs required.

### DD-1 (Lean): How does the device know "I am inside an activity" when opening a form?

**Context**: [FormScreen](../../../mobile/lib/presentation/screens/form_screen.dart) currently takes an optional `activityRef`. The WorkListScreen dispatches via shape selection (single shape → direct, multiple → dialog). Activity context is available at dispatch time but not always forwarded.

**Resolved**: `FormScreen` constructor takes `activityRef` as required where dispatch is from an activity context, nullable otherwise. `EventAssembler.assemble()` accepts and writes it to envelope. No new state layer.

**Reversibility**: Lean. Internal to mobile. No wire-format change.

### DD-2 (Leaf): Where does sensitivity live in the config package JSON?

**Resolved**: Use the shape already defined in [idr-019](../../decisions/idr-019-config-package.md) — `sensitivity_classifications: {shapes: {...}, activities: {...}}`. Populate from `shapes.sensitivity` and `activities.sensitivity` columns. Device stores alongside shape/activity definitions. No enforcement path in Phase 3d — the surface exists so L0 authoring is meaningful and future enforcement (blocking, admin visibility, export redaction) has a seam.

**Reversibility**: Leaf. Additive. Forward-compatible parsing already established ([idr-019](../../decisions/idr-019-config-package.md) rule 6).

### DD-3 (Lean): Where does the `context.*` resolver live on device?

**Resolved**: New class `mobile/lib/data/context_resolver.dart` — pure function `resolveContext(subjectId, activityRef, assignments, events) → Map<String, dynamic>` returning the 7 platform-fixed properties. Called by `FormScreen` on open, passed into `ExpressionEvaluator.evaluateCondition/evaluateValue` as the `context` sub-map.

Resolutions in Phase 3d:

| Property | Source | Phase 3d behavior |
|---|---|---|
| `subject_state` | Pattern projection | `null` (no patterns yet) |
| `subject_pattern` | Pattern projection | `null` (no patterns yet) |
| `activity_stage` | Pattern projection | `null` (no patterns yet) |
| `actor.role` | Active assignment on device | Resolve from `EventStore.getActiveAssignments()` for the actor |
| `actor.scope_name` | Active assignment's location path / activity list | Resolve from `ActiveAssignment` |
| `days_since_last_event` | `ProjectionEngine.getSubjectDetail(subjectId)` | Max timestamp → now delta in days |
| `event_count` | Same | Count events for subject (post-alias, flag-excluded) |

Expressions referencing null properties already handle null safely ([idr-018](../../decisions/idr-018-expression-grammar.md) rule 4).

**Reversibility**: Lean. Behind contract C5. Caching is IG-6 (no cache in 3d — compute at form-open).

---

## 5. Deliverables

### Server

- `ConfigPackager.publish()` populates `sensitivity_classifications.shapes` keyed by `{name}/v{version}` from `shapes.sensitivity` column and `sensitivity_classifications.activities` keyed by activity name from `activities.sensitivity` column.
- No schema migration needed (columns already exist from V5).
- Contract test: config package for a deployment with one `elevated` shape + one `restricted` activity emits both in `sensitivity_classifications`.

### Mobile

- `EventAssembler.assemble(subjectId, shapeRef, payload, {activityRef})` — `activityRef` parameter added to envelope construction.
- `FormScreen` passes `activityRef` through when opened from activity context; `WorkListScreen` + `SubjectDetailScreen` forward it.
- `ContextResolver` class per DD-3.
- `ConfigStore` parses and exposes `sensitivity_classifications` (no behavior attached — accessor only).
- `FormScreen` constructs context via `ContextResolver` before first `ExpressionEvaluator` call.

### Tests

- **3d.1**: Mobile test — open form inside an activity, assemble event, assert `activity_ref` equals the activity ref. Open form outside any activity, assert `activity_ref` is null.
- **3d.2**: Server test — publish config with `elevated` shape and `restricted` activity; GET `/api/sync/config`; assert `sensitivity_classifications.shapes["foo/v1"] == "elevated"` and `...activities.bar == "restricted"`. Mobile test — `ConfigStore.applyConfig` with the above package; assert accessors return correct classifications.
- **3d.3**: Mobile test — given a subject with 3 events, latest 4 days ago, assert `context.event_count == 3` and `context.days_since_last_event == 4`. Given an actor with an active assignment, assert `context.actor.role` and `context.actor.scope_name` resolve non-null. Given a subject with no events, assert `context.event_count == 0` and `context.days_since_last_event == null`.
- **3d.4**: No test; docs update.

### Docs

- [CLAUDE.md](../../../CLAUDE.md) codebase map sync.
- [status.md](../../status.md) — mark Phase 3d complete; update test counts; remove "Phase 4: NOT STARTED" blocker line and replace with explicit "blocked on IDR-020 + IDR-021".
- This file remains the Phase 3d spec.

---

## 6. Quality Gates

All must pass. Phase 3d is not complete until every item is green.

- [ ] **3d.1**: Event captured within an activity → `activity_ref` populated (originally Phase 3a QG, now genuinely tested — D2)
- [ ] **3d.2**: Config package for a deployment with mixed sensitivities emits `sensitivity_classifications` correctly; device parses and exposes (D4)
- [ ] **3d.3**: `context.event_count`, `context.days_since_last_event`, `context.actor.role`, `context.actor.scope_name` all resolve correctly against local projection + assignments on device. Null properties handled without error by evaluator. (Phase 3b QG re-verified — D6)
- [ ] **3d.4**: [CLAUDE.md](../../../CLAUDE.md) codebase map reflects current code — no role_action_mismatch, accurate test counts, accurate file list (D7)
- [ ] No regression: all Phase 0+1+2+3a+3b+3c tests still pass
- [ ] Running tally of Phase 3 carried debt is reduced to the items formally deferred to IDR-021/022 (see §3)

---

## 7. Carried Debt (Leaving Phase 3d)

Phase 3d does **not** close. These are tracked here so Phase 4 planning inherits them cleanly.

| Debt | Origin | Action | Priority |
|------|--------|--------|----------|
| Role-action enforcement | Phase 3a QG (D1) | **IDR-021** before Phase 4 spec | Blocker |
| Flag severity authoring + surface | ADR-3 S7 / ADR-4 S14 (D3) | **IDR-022** | Blocker for severity-driven flags |
| `domain_uniqueness_violation` flag | ADR-4 S14 (D5) | **IDR-022** | Same surface as D3 |
| Command Validator (device advisory) | boundary §2 (D8) | Phase 4 | Advisory only |
| Pattern state machine rep | Phase 4.0 rollback | **IDR-020 (rewrite)** | Blocker for Phase 4 spec |

### Planned IDR Sequence (post-3d)

```
IDR-020 (rewrite) — Pattern State Machine Representation
    ↓ independent of
IDR-021 — Role-Action Enforcement Model     (closes D1)
    ↓ independent of
IDR-022 — Flag Severity + Domain Uniqueness (closes D3 + D5)
    ↓
Phase 4 spec (patterns + triggers + enforcement)
```

IDR-020, 021, 022 are independent — they can be drafted in parallel. They unblock Phase 4 spec, not each other.

---

## 8. Reversibility Triage

| Decision | Bucket | Why |
|----------|--------|-----|
| DD-1: activity_ref plumbing | **Lean** | Internal mobile plumbing. No wire format change. Parameter added to existing function. |
| DD-2: sensitivity in config package | **Leaf** | Wire format was already reserved ([idr-019](../../decisions/idr-019-config-package.md)). Populating a pre-declared map key. |
| DD-3: context resolver layout | **Lean** | Behind C5. Can be refactored any time without wire or contract impact. |

No Locks in Phase 3d. By design — this is close-out, not new architecture.

---

## 9. Journal

Entries recorded here as 3d progresses. Empty at spec authoring time.

- **2026-04-21**: Phase 3d proposed in audit (session memory `phase-3c-audit-2026-04-21.md`). Spec authored.
