# NW-177 - Post-M2.2 Field-Asset Standing And Next Route Selection

Status: non-authoritative routing artifact
Date: 2026-06-28
Type: product-planning / review-standing route
Source: owner-selected NW-177 task; NW-172 through NW-176 accepted evidence; PRs #75 through #79
Authority: none. This artifact records evidence and route selection only. It does not accept product/runtime behavior, implement code, approve production cutover, approve real users/data, change contracts/schemas/sync behavior, change BAR/CDL/gap-register standing, or create lifecycle authority.

## Evidence Read

Repository evidence used:

- `docs/status.md` Current Routing and Current Routing Detail.
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-021 and NW-171 through NW-176.
- `docs/specifications/product/lifecycle-neutral-known-thing-lookup-and-candidate-capture.md`.
- `deploy/reference/pilot-packages/field-assets/README.md`.
- `deploy/reference/pilot-packages/field-assets/seeded-field-assets.synthetic.json`.
- `server/src/test/java/dev/datarun/server/authorization/ScopeFilteredSyncIntegrationTest.java`.
- `server/src/test/java/dev/datarun/server/authorization/WebAdminOperationalReportSnapshotIntegrationTest.java`.
- `server/src/test/java/dev/datarun/server/ops/provisioning/FieldAssetSetupSeedPackageIntegrationTest.java`.
- `mobile/test/field_asset_lookup_test.dart`.
- GitHub PR summaries for #75 through #79.

PR evidence:

| PR | NW | Standing | Evidence used |
|---|---|---|---|
| #75 | NW-172 | Merged 2026-06-27 | Created the M2.2 product spec for lifecycle-neutral `field_asset` lookup, unpromoted candidate evidence, product-safe wording, validation gates, and explicit non-goals. Docs-only checks; no runtime code. |
| #76 | NW-173 | Merged 2026-06-27 | Implemented the smallest runtime slice: mobile scoped lookup, selected asset capture through existing `subject_ref`, failed lookup candidate evidence, server fallback location-path stamping only for the exact configured `field_asset` candidate path, scoped reviewer evidence, and negative coverage for no promotion/lifecycle/duplicate truth. GitHub checks were green. |
| #77 | NW-174 route | Merged 2026-06-27 | Selected the setup/seed proof route after NW-173 and explicitly kept candidate disposition and lifecycle unselected. Docs-only checks. |
| #78 | NW-174 proof | Merged 2026-06-27 | Added `deploy/reference/pilot-packages/field-assets/` and `FieldAssetSetupSeedPackageIntegrationTest`; proved reviewed config, setup-owner bootstrap, subject locations, normal sync seed captures, assignment scope, selected lookup, candidate exclusion from lookup truth, and scoped reviewer visibility. GitHub checks were green. |
| #79 | NW-176 | Merged 2026-06-27 | Closed candidate evidence review visibility with configured-work evidence copy/tests; authorized reviewer can inspect candidate evidence, out-of-scope actor cannot, and no promote/reject/approve/lifecycle/duplicate/merge/split command appears. GitHub checks were green. |

## Standing Answers

1. Is the current `field_asset` path usable for a controlled field rehearsal with synthetic or owner-approved assets?

Yes, but only in a controlled bounded sense. NW-173 proves runtime behavior for scoped mobile lookup, selected capture, failed-lookup candidate evidence, sync, and scoped review. NW-174 proves that a synthetic field-assets package can seed stable assets through accepted subject identity, `subject_locations`, normal sync capture events, and assignment subject-list/activity/geography scope. NW-176 proves authorized reviewer visibility and out-of-scope denial.

The important limit is setup/provisioning. `FieldAssetSetupSeedPackageIntegrationTest` still performs package setup with test-assisted/direct steps around `subject_locations`, assignments, and seed capture events. The package README calls the seed file synthetic pilot fixture material, not registry import/export or a bulk loader. That makes M2.2 usable for a controlled rehearsal when the owner explicitly approves the seed set and setup steps, but not yet operationally clean as a repeatable package an operator can apply without test-only/manual glue.

2. Is candidate promotion/rejection required before that rehearsal?

No. The accepted M2.2 product spec allows unpromoted candidate evidence so work can continue. NW-176 closed the reviewer-inspection gap enough that follow-up can remain manual and outside-system for now. Candidate promotion/rejection becomes required only when a candidate must become future lookup truth, must be rejected inside Datarun, or must affect reporting, assignment, lifecycle, duplicate handling, or merge/split behavior.

3. Did NW-173/NW-174/NW-176 create a `field_asset`-specific mechanism that must be generalized, documented, or quarantined?

Quarantine, not generalize now.

- `field_asset` is the first configured subject type selected for M2.2 validation. It remains configured product vocabulary over existing subject identity, not a platform primitive.
- `asset_candidate_evidence` and the server fallback `location_path` stamping are intentionally narrow. Tests prove the fallback applies only to the exact configured `field_asset` candidate path and does not bypass unrelated subject-binding validation or broaden scoped sync.
- `deploy/reference/pilot-packages/field-assets/seeded-field-assets.synthetic.json` is proof material. It should not be treated as a registry import/export format, lifecycle model, duplicate workflow, or assignment authority.
- The package should be documented and mechanically guarded as lifecycle-neutral field-asset setup/provisioning. Generalizing it into entity lifecycle, registry import/export, candidate disposition, or semantic place handling is a stop condition.

4. Which next route should be selected?

Select exactly one successor: NW-178 bounded field-asset setup/provisioning hardening.

Among the listed alternatives:

- Controlled live field-asset runtime rehearsal is deferred. It would mostly spend runtime effort on behavior already covered by accepted focused tests unless the package is first made operationally applyable or the owner explicitly chooses a manual rehearsal.
- Candidate disposition decision/spec is deferred. There is no current evidence that promotion/rejection is needed before controlled rehearsal.
- Terminology glossary/index is deferred. NW-176 did not expose blocking ambiguous known-asset/candidate wording.
- Platform-owned `entity_lifecycle` pattern/spec is rejected for now as too broad and not the smallest current gap.
- Return to M1.1/local runtime hardening is deferred to NW-166-style triggers: cutover preparation or explicit hardening need. It is not the current `field_asset` gap.

The repository evidence exposes a smaller blocker than any live smoke or lifecycle route: the M2.2 path is behaviorally parked, but its field-assets setup package is not yet operationally hard enough to apply as a clean controlled rehearsal substrate.

## Forward And Backward Implications

Taking NW-178 means:

- The field-assets package can become an operator-usable controlled setup path for a small static asset set.
- Stable `subject_id`, `seed_event_id`, display label, geography mapping, reviewed config, assignment scope, and seed capture behavior become the practical identity substrate for a rehearsal.
- The route must keep the package lifecycle-neutral and exact-scope. It should not create a generic registry importer or asset lifecycle API.
- Real or owner-approved assets seeded this way become sticky append-only identity facts. Wrong seeds require bounded recovery notes, not silent rewrite.

Deferring NW-178 means:

- M2.2 remains parked in a useful test/manual setup state.
- A live field-asset rehearsal would depend on manual/test-shaped setup work and would not add much evidence beyond existing tests unless it discovers environment-specific defects.
- M2.3 candidate disposition remains premature because there is not yet a clean operational initial known set that needs in-system disposition.

## Open Gaps Touching This Feature

Current open gaps that touch this exact surface:

- Candidate promotion/rejection: deferred to M2.3 only when candidate evidence must become or be rejected as lookup truth.
- Lifecycle vocabulary: deferred until work must avoid, warn, route around, or report active/inactive/moved/retired/verified-style standing.
- Duplicate stewardship and merge/split UX: deferred until duplicate review or identity commands become a user workflow need.
- Semantic place/location behavior: deferred because `location_path` remains geographic access/sync infrastructure, not facility/place truth.
- Registry/import/export: deferred because the field-assets seed package is not a registry file, bulk loader, or source-of-truth import/export path.
- Subject/query/custom scope: deferred to NW-053 if setup needs dynamic subject cohorts, query-as-config, or hidden sync scope.
- Retention/security: deferred to NW-054 if sensitive person/household/asset handling or local data promises enter scope.

## Realistic Provisioned Asset Types

Good fit for this route:

- Static equipment or operational assets such as pumps, devices, wells, generators, vehicles, or inspection targets.
- Stock-holding locations or storage points only when treated as pre-approved known work subjects, not as stock ledger truth.
- Delivery or service points only when the route remains static lookup/capture and does not claim facility/place lifecycle.

Risky or not a fit for this route:

- ITN households or household members, because they trigger sensitivity, candidate lifecycle, duplicate, retention, and real-data pressure.
- Teams, users, volunteers, or organizations, because they overlap actor/principal/assignment authority.
- Commodities, stock categories, batches, lots, and ledger items, because they belong to configured options or stock-ledger/import/reporting decisions, not `field_asset` lookup truth.
- Facilities/villages/warehouses as semantic places if they need moved/retired/verified/lifecycle behavior.

## Selected Successor

Selected successor: NW-178 harden lifecycle-neutral field-asset setup/provisioning.

Scope:

- Make the existing `deploy/reference/pilot-packages/field-assets/` package operationally usable for a small controlled synthetic or owner-approved asset set.
- Provide a reviewed, executable, repeatable setup/provisioning path for the package using existing subject identity, `subject_locations`, reviewed config, assignment scope, and normal sync/event behavior.
- Preserve exact reapply/idempotence or explicit no-duplicate safeguards for stable asset and seed event IDs.
- Document rollback/recovery notes for a wrong synthetic/owner-approved seed without adding lifecycle/delete/edit semantics.

Non-goals:

- No live smoke as the selected work.
- No candidate promotion/rejection or disposition state.
- No lifecycle truth, duplicate stewardship/resolution, merge/split UX, semantic place/location behavior, registry/import/export, new `subject_ref`, contract/schema/sync changes, migrations, BAR/CDL/gap-register changes, real-data import/replay, retention/security promises, production cutover, or Keycloak hardening.

Likely files/areas for NW-178:

- `docs/agent-working-surface/prompts/NW-178-harden-field-asset-setup-provisioning.md`.
- `deploy/reference/pilot-packages/field-assets/`.
- `deploy/reference/provisioning-inputs.md`.
- Existing one-shot provisioning and tests under `server/src/main/java/dev/datarun/server/ops/provisioning/` and `server/src/test/java/dev/datarun/server/ops/provisioning/` if implementation is selected.
- Focused scoped sync/configured-work evidence tests if behavior is touched.

Acceptance criteria for NW-178:

- A clean controlled environment can apply the field-assets package through documented command(s) or one reviewed bounded provisioning path without test-only direct DB setup.
- Stable asset IDs, seed event IDs, labels, geography, reviewed config, setup-owner/field/reviewer assignment scope, and seed capture behavior are preserved.
- Exact reapply is idempotent or rejects drift without partial mutation.
- Assigned field actor sees only assigned seeded assets; reviewer sees scoped seeded assets and candidate evidence; out-of-scope actor cannot browse all assets.
- Selected asset capture preserves the existing `subject_ref`.
- Failed lookup candidate evidence remains unpromoted and outside known-asset lookup truth.
- No promote/reject/approve/create-official-asset/lifecycle/duplicate/merge/split/import/export command is exposed.

Executable checks for NW-178:

- `jq empty deploy/reference/pilot-packages/field-assets/*.json`.
- `git diff --check`.
- Focused server provisioning/package test covering the executable setup path.
- Existing focused server evidence tests: `./mvnw -Dtest=FieldAssetSetupSeedPackageIntegrationTest,ScopeFilteredSyncIntegrationTest,WebAdminOperationalReportSnapshotIntegrationTest test`.
- Full server gate if server behavior changes.
- Mobile `flutter test test/field_asset_lookup_test.dart` only if mobile lookup behavior is touched.

Stop/return trigger:

Stop and return to the M2 anchor if implementation requires generic registry import/export, lifecycle state, candidate disposition, duplicate matching, merge/split UX, semantic place behavior, new scope, new subject reference semantics, schema/contract/sync changes, or deployer-authored lifecycle/state-machine power.

## NW-177 Exit Evidence

NW-177 is docs/control work only.

Done evidence required:

- One non-authoritative NW-177 artifact.
- Artifact index updated.
- `docs/status.md` updated with selected successor, deferrals, and stop conditions.
- Backlog updated with accepted NW-177 standing and selected NW-178 successor row.
- NW-178 successor prompt added so the route is executable and not buried in this artifact.
- `git diff --check` passes.
