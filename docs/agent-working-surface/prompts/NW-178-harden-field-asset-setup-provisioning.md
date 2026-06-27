# NW-178 - Harden Lifecycle-Neutral Field-Asset Setup/Provisioning

Status: ready prompt
Document type: execution_packet
Owner: implementation agent
Source: NW-177 post-M2.2 field-asset standing and next-route selection
Authority: selected implementation/tooling packet for field-asset setup/provisioning hardening only. It does not create lifecycle authority, candidate disposition, registry import/export, broad provisioning authority, production cutover, real-user/data approval, contract/schema/sync changes, BAR/CDL/gap-register changes, or runtime behavior beyond the selected bounded setup path.

## User-Visible Outcome

A setup owner can apply the field-assets pilot package as a controlled,
repeatable setup step for a small synthetic or owner-approved asset set. After
setup, a field user assigned to those assets can find only authorized assets,
capture against a selected asset, or save unpromoted missing-asset candidate
evidence for read-only review.

This packet exists because NW-173, NW-174, and NW-176 prove the lifecycle-neutral
M2.2 behavior, but the current field-assets package is still proof material. It
uses test-assisted/direct setup steps around `subject_locations`, scoped
assignments, and seed capture events. The next delivery-critical gap is making
that setup path operationally usable without adding lifecycle or registry scope.

## Files To Read

Read first:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-172
  through NW-178
- `docs/agent-working-surface/artifacts/NW-177-post-m22-field-asset-standing-and-next-route-selection.md`
- `docs/specifications/product/lifecycle-neutral-known-thing-lookup-and-candidate-capture.md`
- `deploy/reference/pilot-packages/field-assets/README.md`
- `deploy/reference/pilot-packages/field-assets/seeded-field-assets.synthetic.json`
- `deploy/reference/provisioning-inputs.md`
- `docs/agent-working-surface/validation-matrix.md`
- this prompt

Read implementation code/tests only as needed for touched surfaces:

- `server/src/main/java/dev/datarun/server/ops/provisioning/`
- `server/src/test/java/dev/datarun/server/ops/provisioning/FieldAssetSetupSeedPackageIntegrationTest.java`
- field-asset lookup/candidate/scoped sync tests under `server/src/test/java/dev/datarun/server/authorization/`
- mobile field-asset lookup tests only if mobile behavior is touched

Read nested `server/AGENTS.md`, `mobile/AGENTS.md`, or `contracts/AGENTS.md`
before touching those surfaces.

Do not read or modify CDL/gap-register/BAR surfaces unless a stop condition
fires.

## Accepted Evidence To Preserve

Preserve the accepted M2.2 chain:

- NW-172 product boundary: `field_asset` is configured product vocabulary, not
  a platform primitive.
- NW-173 runtime boundary: mobile lookup is scoped to assigned `subject_list`
  assets; selected assets use existing `subject_ref`; failed lookup writes
  unpromoted `asset_candidate_evidence` on a generated submitted-record
  subject; candidate payload omits `field_asset` and does not enter lookup
  truth.
- NW-174 package boundary: `deploy/reference/pilot-packages/field-assets/` is a
  synthetic setup/seed proof, not registry import/export, lifecycle, duplicate,
  merge/split, or assignment authority.
- NW-176 review boundary: authorized reviewers can inspect candidate evidence
  read-only; out-of-scope actors cannot; no promote/reject/approve/create
  official asset/lifecycle/duplicate/merge/split command is exposed.

## Required Work

Implement the smallest setup/provisioning hardening that makes the existing
field-assets package operationally usable for a controlled rehearsal.

Minimum expected outcomes:

1. A clean controlled environment can apply the field-assets package through
   documented command(s) or one reviewed bounded provisioning path.
2. Setup no longer depends on test-only direct DB steps for normal operator use.
3. Stable asset subject IDs, seed event IDs, labels, geography mapping,
   reviewed config, setup-owner/field/reviewer assignment scope, and normal
   seed capture behavior are preserved.
4. Exact reapply is idempotent or rejects drift without partial mutation.
5. Assigned field actor sees only assigned seeded assets.
6. Reviewer sees scoped seeded assets and unpromoted candidate evidence.
7. Out-of-scope actor cannot browse all seeded assets.
8. Selected asset capture keeps existing `subject_ref`.
9. Failed lookup candidate evidence stays outside known-asset lookup truth.
10. The docs state the recovery posture for a wrong synthetic or owner-approved
    seed without inventing lifecycle/delete/edit semantics.

## Allowed Changes

Allowed, if needed:

- field-assets package docs/manifest material;
- documented provisioning command examples or bounded setup wrapper;
- narrowly scoped one-shot provisioning support for this accepted package only;
- focused server provisioning tests and existing field-asset evidence tests;
- status/backlog acceptance updates after execution evidence exists.

Prefer existing one-shot provisioning, reviewed config, assignment bootstrap,
subject identity, subject-location mapping, and normal sync/event paths.

## Forbidden Work

Do not implement or accept:

- candidate promotion/rejection, disposition state, or official-asset creation;
- lifecycle state, active/inactive/retired/moved/replaced/verified truth;
- duplicate stewardship/resolution or merge/split UX;
- semantic `location`, place lifecycle, facility registry, or place registry;
- broad registry browsing, import/export, bulk loader, or source-of-truth
  registry framework;
- new `subject_ref` types, envelope fields, event types, contracts, schemas,
  migrations, or sync protocol behavior;
- assignment/scope expansion beyond existing geography, subject-list, activity,
  and temporal mechanisms;
- deployer-authored lifecycle state machines or arbitrary setup scripts as
  authority;
- BAR/CDL/gap-register changes;
- real-data import/replay, legacy account import, retention/security promises,
  production cutover, Keycloak hardening, or broad real-user rollout.

## Stop Conditions

Stop and route a successor decision if hardening requires any of these:

- generic registry import/export or bulk-load authority;
- lifecycle, candidate disposition, duplicate, merge/split, or semantic place
  behavior;
- new subject identity semantics or new `subject_ref` type;
- new scope/query/custom cohort behavior;
- contract/schema/sync/migration changes;
- deployer-authored lifecycle or state-machine power;
- rollback by mutating/deleting append-only seed events as the normal path.

If the package can only be made useful by adding lifecycle or candidate
disposition, stop and route M2.3/S06 instead of stretching NW-178.

## Required Validation

Use the validation matrix for touched surfaces.

Expected checks:

```bash
jq empty deploy/reference/pilot-packages/field-assets/*.json
git diff --check
cd server && ./mvnw -Dtest=FieldAssetSetupSeedPackageIntegrationTest,ScopeFilteredSyncIntegrationTest,WebAdminOperationalReportSnapshotIntegrationTest test
```

If server behavior changes, run the full server gate:

```bash
cd server && ./mvnw test
```

If mobile lookup behavior changes, run:

```bash
cd mobile && flutter test test/field_asset_lookup_test.dart
cd mobile && flutter test
```

Runtime/live smoke is not the selected evidence for NW-178. A later controlled
field-asset rehearsal may be selected after this package can be applied through
the hardened setup path or if the owner explicitly chooses a manual rehearsal.

## Acceptance Boundary

NW-178 is accepted only when status/backlog evidence records:

- exact setup/provisioning path used;
- idempotence or drift-rejection behavior;
- scoped field/reviewer/out-of-scope evidence;
- candidate evidence remains unpromoted and outside lookup truth;
- no lifecycle, promotion/rejection, duplicate, merge/split, semantic location,
  registry/import-export, contract/schema/sync, BAR/CDL/gap-register, real-data,
  production cutover, or Keycloak-hardening scope landed;
- exact validation commands and results.
