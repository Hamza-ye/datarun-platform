# NW-180 - Run Controlled Field-Asset Online Local/On-Prem Synthetic Smoke

Status: ready prompt
Document type: execution_packet
Owner: implementation agent
Source: NW-179 controlled field-asset rehearsal route selection
Authority: selected operational/product-validation smoke only. It does not
create lifecycle authority, candidate disposition, registry import/export,
runtime implementation scope, production cutover, real-user/data approval,
contract/schema/sync changes, BAR/CDL/gap-register changes, or broad rollout.

## User/Deployment Outcome

Prove the hardened field-assets package works in the intended online local/on-prem
deployment path, not just in repo tests or a manual synthetic walkthrough.

This smoke is worth running only if it exercises deployment/runtime/integration
risk that cheaper evidence cannot prove:

- release-image one-shot provisioning against the live deployment DB/config
  state;
- exact field-assets seed reapply/idempotence in that environment;
- actual authenticated field actor asset lookup, selected-asset capture, sync,
  and missing-asset candidate capture;
- reviewer read-only inspection of seeded assets and candidate evidence; and
- out-of-scope denial through the live access path.

If the available work would only repeat local repo tests, a local test DB,
direct DB setup, or a manual synthetic PC-style walkthrough, stop and recommend
skipping NW-180. Do not spend a night on evidence the repo already has.

## Files To Read

Read first:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-172
  through NW-180
- `docs/specifications/product/lifecycle-neutral-known-thing-lookup-and-candidate-capture.md`
- `docs/agent-working-surface/artifacts/NW-177-post-m22-field-asset-standing-and-next-route-selection.md`
- `docs/agent-working-surface/prompts/NW-178-harden-field-asset-setup-provisioning.md`
- `deploy/reference/pilot-packages/field-assets/README.md`
- `deploy/reference/pilot-packages/field-assets/reviewed-config.json`
- `deploy/reference/pilot-packages/field-assets/assignment-bootstrap.setup-owner.json`
- `deploy/reference/pilot-packages/field-assets/seeded-field-assets.synthetic.json`
- `deploy/reference/provisioning-inputs.md`
- `docs/agent-working-surface/validation-matrix.md`
- this prompt

Read nested `server/AGENTS.md`, `mobile/AGENTS.md`, or `contracts/AGENTS.md`
only before touching those surfaces. Runtime code changes are not expected.

## Data Boundary

Use the existing synthetic seed set by default.

Owner-approved non-sensitive assets are allowed only if execution records the
approval and the asset set remains small, controlled, and lifecycle-neutral.
Do not import real operational datasets, legacy data, submitted-record replay,
or sensitive person/household data.

## Required Smallest Smoke

Run exactly one bounded online local/on-prem synthetic operational smoke:

1. Confirm the target is the intended online local/on-prem deployment path, not
   a local repo or test DB substitute. Record the host/path classification.
2. Apply `reviewed-config.json` through `config-publish` using the accepted
   one-shot release-image command path.
3. Apply `assignment-bootstrap.setup-owner.json` through `assignment-bootstrap`
   using the accepted one-shot release-image command path.
4. Apply `seeded-field-assets.synthetic.json` through `field-assets-seed` using
   the accepted one-shot release-image command path.
5. Reapply `field-assets-seed` once and record exact reuse/idempotence evidence.
6. Bind or use only accepted authenticated principals needed for the synthetic
   field/reviewer actors. If this cannot be done with existing accepted
   principal-binding/account setup, stop instead of hardening Keycloak or
   inventing account-import work.
7. Run one actual field actor mobile lookup/capture path:
   - field actor sees only the authorized seeded asset;
   - field actor captures against the selected asset;
   - selected capture preserves existing `subject_ref`;
   - capture syncs to the live deployment path.
8. Run one actual missing-asset candidate path:
   - field actor saves unpromoted candidate evidence;
   - candidate evidence syncs;
   - candidate evidence remains outside known-asset lookup truth.
9. Verify reviewer read-only inspection through existing web-admin/configured
   work evidence paths.
10. Verify out-of-scope actor denial through the live access path.

## Evidence To Produce

If the smoke runs, create one dated operations rehearsal record under
`docs/operations/rehearsals/`.

The record must include secret-safe evidence only:

- target deployment path and whether probes used public proxy or LAN-local
  resolution;
- exact commands, cwd/host context, exit status, and timestamps;
- command output summaries including `command`, `status`, `operator_id`,
  `evidence_id`, and `input_sha256` where emitted;
- seed reapply reused counts or equivalent idempotence proof;
- actor IDs and asset IDs used;
- mobile selected-asset lookup/capture/sync proof;
- mobile missing-asset candidate/sync proof;
- reviewer read-only inspection proof;
- out-of-scope denial proof;
- explicit statement that candidate evidence stayed unpromoted and outside
  lookup truth;
- explicit statement that no lifecycle, promotion/rejection, duplicate,
  merge/split, semantic place/location, registry/import-export, new
  `subject_ref`, new scope, contract/schema/sync, Keycloak hardening,
  real-data/import, production cutover, retention/security, broad runtime, or
  broad rollout work occurred.

## Decision Unlocked

Successful NW-180 evidence unlocks only this standing:

```text
The hardened field-assets package is operator-usable in the intended local/on-prem
deployment path for a controlled synthetic or owner-approved non-sensitive asset
pilot.
```

It does not unlock real-data import, broad real-user rollout, production
cutover, candidate promotion/rejection, lifecycle, duplicate stewardship,
merge/split UX, semantic place/location behavior, registry/import-export, or
retention/security claims.

## Forbidden Work

Do not implement or accept:

- broad runtime work or code changes to make the rehearsal pass;
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
- BAR/CDL/gap-register changes;
- real-data import/replay, legacy account import, retention/security promises,
  production cutover, Keycloak hardening, or broad real-user rollout.

## Stop Conditions

Stop and report the exact blocker instead of substituting weaker evidence if:

- the smoke cannot run against the intended online local/on-prem deployment path;
- the only available evidence would be local repo/test-DB/manual synthetic work
  already covered by NW-173/NW-174/NW-176/NW-178;
- authenticated field/reviewer access requires Keycloak hardening, account
  import, IdP claim/group authority, or other auth scope beyond accepted
  principal binding;
- execution requires real operational data, real users, real-data import, or
  submitted-record replay;
- any step requires lifecycle, candidate disposition, duplicate, merge/split,
  semantic place/location, registry import/export, new `subject_ref`, new
  scope, contract/schema/sync, migration, or runtime code changes.

If a runtime defect is discovered, capture the smallest secret-safe evidence and
route a follow-up. Do not fix it inside NW-180 unless a separate selected route
authorizes that work.

## Validation

Expected docs/package checks for the NW-180 evidence change:

```bash
jq empty deploy/reference/pilot-packages/field-assets/*.json
git diff --check
```

Run runtime tests only if a separately selected fix changes runtime code. The
normal NW-180 path should produce operational evidence, not code.
