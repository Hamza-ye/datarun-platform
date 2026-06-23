# NW-152 - Mobile Stocktake Capture Smoke

## Goal

Prove the first field-user mobile path for the stock operations pilot package:
capture/offline/sync of one flat `stocktake_line/v1` work item, or stop with
the exact missing mobile surface.

This is a bounded mobile/product-validation implementation slice. It must use
the NW-150 package skeleton, the NW-151 subject-anchor boundary, and existing
Datarun config, auth, assignment, local-state, and sync mechanisms. It is not a
real pilot launch.

## Inputs

Read:

- `docs/status.md` Current Routing.
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-148
  through NW-152.
- `docs/specifications/product/stock-operations-pilot-pm-handoff.md`.
- `deploy/reference/pilot-packages/stock-operations/README.md`.
- `deploy/reference/pilot-packages/stock-operations/reviewed-config.json`.
- `deploy/reference/pilot-packages/stock-operations/synthetic-assumptions.json`.
- `docs/agent-working-surface/prompts/NW-151-bind-stock-operations-subject-anchor-boundary-and-harden-proof-oracle.md`.
- `server/src/test/java/dev/datarun/server/ops/provisioning/StockOperationsPilotPackageIntegrationTest.java`.
- `server/src/test/java/dev/datarun/server/e2e/SyntheticStockOperationsFirstFlowIntegrationTest.java`.
- `docs/agent-working-surface/validation-matrix.md`.

Read nested `mobile/AGENTS.md` before touching mobile files. Read
`server/AGENTS.md` only if the implementation touches server files.

## Required Behavior

Use the NW-150 package shape/activity:

- shape: `stocktake_line/v1`;
- fields: `stocktake_date`, `stock_category`, `quantity`;
- activity: `stock_operations`;
- ordinary field-worker capture only.

Use the NW-151 subject-anchor boundary:

- every stocktake event must carry `subject_ref.type = "subject"`;
- `subject_ref.id` must be a pre-established pilot stock-scope subject;
- pre-established means stable subject UUID plus `subject_locations` mapping
  under selected pilot geography plus worker/supervisor assignments covering
  that geography/activity plus capture/session/operator context that can stamp
  it into `subject_ref`;
- the pilot subject represents the counted stock-holding location or storage
  point for this pilot mapping only;
- `stocktake_line/v1` has `subject_binding = null`, so mobile capture must
  select or stamp the subject from capture/session/operator context, not from a
  payload field.

Prove, with the narrowest existing mobile test surface that fits the app, that a
field worker can:

- receive or use a config containing the packaged stocktake-line shape;
- safely select or stamp the pre-established pilot stock-scope subject;
- capture a stocktake-line item while offline or before sync;
- retain the pending work in the actor-local mobile store;
- sync the work through the existing authenticated sync path; and
- preserve flat event semantics without stock ledger or review workflow logic.

If the current mobile UI/test harness cannot express this exact smoke safely,
produce a precise stop report naming the missing mobile surface and the smallest
successor needed.

## Product Boundary

NW-152 may claim only a mobile stocktake capture smoke for synthetic or
separately approved data. It must not claim full pilot provisioning or
controlled operational use.

Supervisor standing remains scoped read visibility through assignment scope and
existing web-admin access/read commands. Do not add a review workflow, review
queue, supervisor review action, or `event.type = "review"`.

Ordinary worker/supervisor assignment creation remains an operator step through
the accepted assignment-admin workflow.

## Guardrails

Do not add:

- real users or real stock data;
- account import;
- submitted-record import/replay;
- local Keycloak or principal-binding pilot path;
- legacy form importer;
- repeatable-section platform support;
- production approval or controlled operational use;
- review workflow, review queue, or review-event authority;
- stock ledger correctness or production stock truth;
- broad reporting/import/export;
- queue/list/batch automation;
- tenant/control-plane work;
- new platform mechanisms.

Also do not change envelope fields/types, stored event meaning, contracts,
schemas, sync protocol semantics, new scope mechanisms, validation policy, BAR,
CDL, gap-register standing, or production readiness standing. If one of those
changes appears necessary, stop and route before implementation.

## Expected Output

One of:

1. A focused mobile smoke test or runtime proof for `stocktake_line/v1`
   capture/offline/sync using synthetic/non-sensitive data and existing
   mechanisms; or
2. A precise stop report naming the missing mobile surface and a bounded
   successor route.

On success, update status/backlog acceptance evidence and select exactly one
next route:

```text
NW-153 - Supervisor stock operations view
```

If a real blocker is found, stop and name the blocker instead of selecting
NW-153 by default.

## Validation

Use `docs/agent-working-surface/validation-matrix.md` for touched surfaces.

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
```

For mobile changes, run the narrowest focused Flutter test first, then the full
mobile gate required by the validation matrix:

```bash
cd /home/hamza/datarun-platform/mobile
flutter test <focused test path>
flutter test
```

Android compile is required only if native/platform/auth/plugin surfaces are
touched. If server code is touched, run the focused server test and required
broader server gate for that surface.

## Stop Conditions

Stop and report before work that requires:

- real users, real stock data, raw legacy data, account import, or submitted
  record import/replay;
- local Keycloak/principal-binding pilot implementation;
- a legacy form importer or repeatable-section platform support;
- production approval, controlled operational use, cutover, stock ledger
  correctness, or production stock truth;
- broad reporting/import/export, queue/list/batch automation, or review queues;
- tenant/control-plane, tenant-aware runtime/storage/sync/config/auth, or new
  scope mechanisms;
- contract/schema/sync/envelope changes, BAR/CDL/gap-register changes, or
  validation-policy changes.
