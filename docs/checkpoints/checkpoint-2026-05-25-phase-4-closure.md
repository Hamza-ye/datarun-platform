# Project Checkpoint — 2026-05-25 (Phase 4 Closure)

---

## 1. Bearing

- **Anchor commit**: `8475955 docs(status): close phase 4 audit`
- **Phase**: Phase 4 (Workflow & Policies) — **COMPLETE** at the anchor commit.
- **Verification at closure**:
  - `mvn test` from `server/`: 271 passed.
  - `flutter test` from `mobile/`: 91 passed.
  - `git diff --check`: passed.
- **Important caveat**: this checkpoint describes the committed Phase 4 closure state. The current worktree contains uncommitted FP-010/platform payload contract exploration and must not be treated as accepted architecture until reviewed.

Phase 4 closed after role-action enforcement, severity overrides, assignment-administration hardening, domain uniqueness, platform pattern registry and binding validation, pattern definition delivery, rebuildable pattern-state projection, subject-history backfill for `ongoing_resolution`, IDR-026 resolver enforcement, `transition_violation` detection, P04 responsibility-binding scenario coverage, and a status/catalog close-out audit.

---

## 2. Commit Ledger

Recent committed milestones:

| Commit | Meaning |
|--------|---------|
| `8475955` | Phase 4 completion audit; status, phase spec, and flag catalog reconciled. |
| `af5b558` | P04 scenario-grade Responsibility Binding integration test landed. |
| `70bd8d9` | Phase 4.6 `transition_violation` detection landed. |
| `4c8afd9` | IDR-026 / FP-009 single-writer conflict resolution landed. |
| `6cf2491` | FP-011 authentication actor mapping and group non-authority recorded. |

The clean committed baseline after `8475955` has Phase 4 complete, FP-010 still open, and FP-011 still open.

---

## 3. Current Worktree Overlay

`git status --short` at checkpoint creation showed uncommitted exploratory changes:

```text
 M AGENTS.md
 M docs/flagged-positions.md
 M docs/implementation/execution-plan.md
 M docs/implementation/phases/phase-4.md
 M docs/status.md
 M server/pom.xml
 M server/src/main/java/dev/datarun/server/config/ActivityService.java
 M server/src/main/java/dev/datarun/server/config/ConfigAdminController.java
 M server/src/main/java/dev/datarun/server/config/PlatformShapeBootstrap.java
 M server/src/main/java/dev/datarun/server/config/ShapePayloadValidator.java
 M server/src/main/java/dev/datarun/server/config/ShapeService.java
 M server/src/main/java/dev/datarun/server/event/EventRepository.java
 M server/src/test/java/dev/datarun/server/contracts/PlatformShapeBootstrapTest.java
?? docs/implementation/phases/post-phase-4-contract-architecture-analysis.md
?? server/src/main/java/dev/datarun/server/config/PlatformPayloadContractValidator.java
?? server/src/test/java/dev/datarun/server/contracts/PlatformPayloadEmissionContractIntegrationTest.java
?? server/src/test/java/dev/datarun/server/contracts/PlatformPayloadShapeContractTest.java
```

Treat this overlay as **suspect**. It appears to attempt FP-010 by bundling `contracts/shapes/*.schema.json`, adding platform payload contract validation, hiding platform shapes from deployer UI, rejecting platform shape refs in activity bindings, and marking FP-010 resolved in docs. Those may be directionally useful, but they are not part of this checkpoint's accepted baseline.

Do not build subsequent planning on the dirty docs' claim that FP-010 is resolved until the code, tests, schema semantics, admin behavior, and mobile/server contract implications are reviewed.

---

## 4. Phase 4 Closure State

### Landed Work

| Area | Closure state |
|------|---------------|
| Role-action enforcement | Server accepts disallowed work events and emits `role_stale`; mobile remains advisory; assignment lifecycle commands stay out of `activities[*].roles`. |
| Severity | Platform defaults plus deployment-wide L0 `flag_severity_overrides`; severity does not alter resolvability. |
| Assignment administration | Multi-axis containment, authenticated actor binding, explicit bootstrap/root semantics, null-activity semantics, and dev-only HTML admin boundary landed. |
| Domain uniqueness | Shape-declared uniqueness validation/package preservation, server accept-and-flag detector, duplicate-basis exclusion, accepted-resolution re-inclusion, and mobile advisory duplicate checks landed. |
| Pattern registry | Platform-bundled pattern binding metadata and deploy-time binding validation landed. |
| Pattern contracts | `contracts/pattern-definition.schema.json` and `contracts/patterns/*.json` are canonical and delivered through config packages when referenced. |
| Pattern projection | Server and mobile rebuild pattern state from active bindings and packaged definitions; unresolved/rejected flags exclude, canonical accepted resolutions re-admit only through legal transitions. |
| Subject-history backfill | Separate `/api/sync/subject-history` surface landed; cursor is independent of normal live-sync watermark and does not mutate `device_sync_state`. |
| Resolver enforcement | Runtime `designated_resolver`, bearer-bound `/api/conflicts/**`, exact canonical resolver equality, and unauthorized-resolution `scope_violation` landed. |
| Transition detection | Server emits `transition_violation` after domain uniqueness using IDR-020 transition matching and IDR-026 resolver routing; accept-and-flag, no auto-resolution. |
| P04 Responsibility Binding | Scenario-grade reassignment campaign coverage landed in `ResponsibilityBindingScenarioIntegrationTest`. |

### Explicit Non-Goals Preserved

- No new envelope fields or envelope `type` values.
- No durable workflow-state tables.
- No auto-resolution.
- No resolver reassignment.
- No normal sync backfill or watermark rewrite.
- No Keycloak/OIDC/JWT/group/claim authority.
- No `subject_ref.type = "process"` usage for Phase 4 pattern instances.
- S06/entity lifecycle remains explicitly deferred.

---

## 5. Flagged Positions

At committed Phase 4 closure:

| FP | Status | Notes |
|----|--------|-------|
| FP-001 | RESOLVED | `role_stale` uses projection-derived role/action authority. |
| FP-002 | RESOLVED | `subject_lifecycle` table read discipline resolved by event-derived lifecycle. |
| FP-003 | RESOLVED | Envelope schema parity test exists. |
| FP-004 | OPEN | Future `assignment_ref` envelope-field question; no current forcing function. |
| FP-005 | RESOLVED | Subject-history backfill separated from normal live sync. |
| FP-006 | RESOLVED | Superseded ended assignment false positive fixed. |
| FP-007 | RESOLVED | Multi-axis containment and null-activity semantics landed. |
| FP-008 | RESOLVED | Assignment command actor identity binding landed. |
| FP-009 | RESOLVED | Runtime resolver designation and single-writer resolution landed. |
| FP-010 | OPEN | Platform-bundled payload shape contract parity. Recommended next candidate, but current dirty implementation is unreviewed. |
| FP-011 | OPEN | Production Keycloak/OIDC principal-to-actor mapping and group non-authority. Do not take unless production auth is explicitly in scope. |

---

## 6. Risk Pulse

| Risk | Current read | Mitigation |
|------|--------------|------------|
| Contract drift for platform payload shapes | Active through FP-010 at the committed baseline. | Review FP-010 deliberately; do not accept dirty changes just because they compile or update docs. |
| Authentication authority drift | Active through FP-011, but not triggered. | Keep bearer actor-token model until production auth/OIDC is scheduled with a decision artifact. |
| Workflow projection cost | Watch item, not active blocker. | Keep rebuildable/on-demand projection; use ADR-001 B-to-C only after measured pressure. |
| Backfill/audit leakage | Controlled at closure. | Keep `/api/sync/subject-history` separate and request-time scoped; normal pull remains live-only. |
| Pattern/event review conflation | Controlled at closure. | Preserve event-level review overlay separation from subject-level `ongoing_resolution`. |

---

## 7. March Orders

1. **Review the dirty FP-010 overlay before doing anything else.**
   - Inspect `PlatformPayloadContractValidator`, changes to `EventRepository.insert`, `ShapePayloadValidator`, `ShapeService`, `ActivityService`, admin UI filtering, and tests.
   - Verify whether runtime validation of server-emitted platform events is architecturally safe and operationally tolerable.
   - Check whether using `contracts/shapes/*.schema.json` as runtime validators creates schema/DSL confusion or breaks existing platform mirror semantics.

2. **Decide FP-010 deliberately.**
   - Accept, revise, or discard the dirty implementation.
   - If accepted, ensure tests prove all six platform-bundled payload contracts are bundled, runtime/emission paths validate correctly, platform mirrors cannot drift silently, and deployer-admin surfaces cannot edit or bind platform payload shapes incorrectly.
   - If rejected, restore docs to the committed baseline claim: FP-010 remains open.

3. **Do not take FP-011 yet.**
   - It is blocked until production authentication/OIDC work is explicitly scheduled.
   - No Keycloak/JWT/group/claim authority should be introduced as incidental cleanup.

4. **Before starting a new product phase, run a fresh status pass.**
   - Read `AGENTS.md`, `docs/status.md`, `docs/flagged-positions.md`, relevant ADRs/IDRs, and contracts.
   - Verify the worktree is either clean or intentionally carrying reviewed FP-010 changes.

