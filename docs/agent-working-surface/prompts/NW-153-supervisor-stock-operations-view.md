# NW-153 - Supervisor Stock Operations View

## Goal

Implement a bounded read-only supervisor web-admin view that shows scoped
stocktake-line details for the accepted `stock_operations` package after
NW-152 proved mobile capture/offline/sync.

This is a narrow product UI implementation slice. It should make the first
supervisor stock operations detail view executable without turning stock
operations into a stock ledger, item catalog, warehouse lifecycle, stocktake
session lifecycle, review workflow, broad reporting surface, or production
approval route.

## Inputs

Read:

- `docs/status.md` Current Routing.
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-148
  through NW-153.
- `docs/specifications/product/stock-operations-pilot-pm-handoff.md`.
- `deploy/reference/pilot-packages/stock-operations/reviewed-config.json`.
- `deploy/reference/pilot-packages/stock-operations/synthetic-assumptions.json`.
- `mobile/test/stocktake_capture_smoke_test.dart`.
- Relevant existing web-admin scoped operational/report code and tests,
  especially the current operational view/report service, controller,
  templates, and integration tests.
- `docs/agent-working-surface/validation-matrix.md`.

Read nested `server/AGENTS.md` before touching server code. Read other nested
`AGENTS.md` files only if those surfaces are touched.

## Required Behavior

Implement one bounded read-only web-admin surface for stock operations
supervisor visibility.

The view must:

- require a valid web-admin session plus `web_admin.access` and
  `web_admin.read_scoped`;
- apply accepted assignment/scope visibility before showing stocktake-line
  details;
- show only flat `stocktake_line/v1` evidence:
  - `stocktake_date`;
  - `stock_category`;
  - `quantity`;
  - accepted activity/shape context for `stock_operations` and
    `stocktake_line/v1`;
  - product-safe freshness or visibility wording if exposed;
- keep the subject anchor product-safe, without turning raw subject IDs into
  product concepts unless existing UI patterns require them;
- remain read-only and avoid mutation paths.

Use existing scoped operational/report mechanics and tests where practical.
If the current scoped-read surface cannot safely expose the required details,
stop and name the exact missing surface rather than creating a broader report
or new scope mechanism.

## Product Boundary

NW-153 may claim only scoped supervisor visibility into stocktake-line details
for synthetic or separately approved stock operations evidence. It does not
approve real users, real stock data, production readiness, controlled
operational use, stock ledger correctness, warehouse lifecycle, item/catalog
modeling, stocktake session lifecycle, broad reporting, or a review workflow.

Supervisor standing remains scoped read visibility through assignment scope and
existing web-admin access/read commands. Do not add a review queue, supervisor
review action, or `event.type = "review"`.

Ordinary worker/supervisor assignment creation remains an operator step through
the accepted assignment-admin workflow.

## Guardrails

Do not add:

- review workflow;
- `event.type = "review"`;
- stock ledger correctness;
- item catalog;
- warehouse lifecycle;
- stocktake session lifecycle;
- broad reporting/export/import;
- queue/list/batch automation;
- new scope mechanism;
- contracts/schema/sync/CDL/BAR/gap-register changes;
- real users/data;
- Keycloak/provider setup;
- production approval;
- tenant/control-plane behavior.

Also do not change envelope fields/types, stored event meaning, sync protocol
semantics, validation policy, or production readiness standing. If one of those
changes appears necessary, stop and route before implementation.

## Expected Output

One of:

1. A focused implementation PR for the bounded supervisor stock operations
   view, with tests and acceptance evidence; or
2. A stop report naming the exact missing scoped-read surface and one bounded
   successor.

On success, update status/backlog acceptance evidence and select exactly one
next route from the stock operations pilot ordered backlog. If a real blocker
is found, stop and name the blocker instead of selecting a successor by
default.

## Validation

Use `docs/agent-working-surface/validation-matrix.md` for touched surfaces.

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
```

For server/web-admin code changes, run the narrowest focused web-admin or
operational-view test first, then the required broader server gate for the
touched surface unless the task stops on a precise blocker before code changes
are accepted.

If mobile, contracts, schemas, sync behavior, native/platform/auth/plugin code,
or other surfaces are touched unexpectedly, run the additional gates required
by the validation matrix and record the reason the surface was touched.

## Stop Conditions

Stop and report before work that requires:

- real users, real stock data, raw legacy data, account import, or submitted
  record import/replay;
- local Keycloak/principal-binding pilot implementation;
- a legacy form importer or repeatable-section platform support;
- production approval, controlled operational use, cutover, stock ledger
  correctness, or production stock truth;
- broad reporting/import/export, queue/list/batch automation, or review queues;
- warehouse lifecycle, item catalog, stocktake session lifecycle, or stock
  ledger state;
- tenant/control-plane, tenant-aware runtime/storage/sync/config/auth, or new
  scope mechanisms;
- contract/schema/sync/envelope changes, BAR/CDL/gap-register changes, or
  validation-policy changes.
