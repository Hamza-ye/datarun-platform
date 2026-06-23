# NW-150 - Stock Operations Pilot Package Skeleton

## Goal

Create a reusable non-production/pilot stock operations package skeleton from
the NW-148 test-only proof.

This is implementation/package work, not another planning artifact. The output
should make the stocktake-line pilot setup reusable outside a single test while
preserving the local/on-prem, synthetic/non-sensitive, non-production boundary.

## Inputs

Read:

- `docs/status.md` Current Routing.
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-148,
  NW-149, and NW-150.
- `docs/implementation/module-interfaces.md`
- `docs/specifications/product/stock-operations-pilot-pm-handoff.md`.
- `server/src/test/java/dev/datarun/server/e2e/SyntheticStockOperationsFirstFlowIntegrationTest.java`.
- Existing config/provisioning/package patterns as needed, especially:
  - `deploy/reference/provisioning-inputs.md`;
  - `server/src/main/java/dev/datarun/server/ops/provisioning/ReviewedConfigProvisioner.java`;
  - `server/src/main/java/dev/datarun/server/ops/provisioning/OneShotProvisioningService.java`;
  - `server/src/test/java/dev/datarun/server/ops/provisioning/OneShotProvisioningIntegrationTest.java`;
  - `server/src/test/java/dev/datarun/server/config/ConfigIntegrationTest.java`;
  - `server/src/test/java/dev/datarun/server/e2e/SyntheticStockOperationsFirstFlowIntegrationTest.java`;
  - existing shape/activity/config package tests if the implementation touches
    deployer shape or config-package behavior.
- `docs/agent-working-surface/validation-matrix.md`.

Read nested `server/AGENTS.md`, `mobile/AGENTS.md`, or `contracts/AGENTS.md`
only if the implementation touches those surfaces.

## Required Behavior

Deliver one bounded package skeleton that makes the stocktake-line pilot setup
reusable outside a single test.

Include, as appropriate to existing repo patterns:

- a flat `stocktake_line/v1` shape definition with:
  - `stocktake_date`;
  - `stock_category`;
  - `quantity`;
- a `stock_operations` activity definition;
- seed/provisioning fixture or package material that can be loaded by the
  existing reviewed config/provisioning path, or a precise stop report if the
  current path cannot support this safely;
- synthetic worker, supervisor, assignment, and principal-binding assumptions;
- usage notes for non-production local/on-prem pilot setup;
- focused validation proving the package can be loaded, published, or exercised.

Prefer existing reviewed provisioning/config package mechanisms over inventing
a new package runner. Keep reusable package material separate from real pilot
data. If a small runtime or test helper is needed to make the package loadable,
keep it directly tied to this stocktake-line skeleton.

## Architecture Binding

`stocktake_line/v1` is deployer-authored shape configuration. It is not a
platform payload contract, stock ledger model, or new event type.

`stock_operations` is a deployer activity instance. It is not a platform
mechanism, processing pipeline, tenant boundary, stock truth source, or workflow
pattern.

Use the existing reviewed config/provisioning mechanisms as platform mechanisms.
The NW-150 output must be package/provisioning material only.

Do not emit or introduce `event.type='review'` in this slice. Supervisor standing
in NW-150 is read-only scoped visibility through existing operational/report
surfaces, not a review workflow.

No workflow pattern is selected for NW-150. If later stock operations need human
review, transfer acknowledgment, multi-step approval, or discrepancy resolution,
route a successor that explicitly maps to the existing pattern contracts:
`capture_with_review/v1`, `transfer_with_acknowledgment/v1`,
`multi_step_approval/v1`, or `ongoing_resolution/v1`.

Treat `SyntheticStockOperationsFirstFlowIntegrationTest` as implementation
evidence, not architecture authority.

## Product Boundary

Use PR #58 / NW-148 as evidence that the backend stocktake-line path already
works with synthetic data. NW-148 proved a server-side synthetic
`stocktake_line/v1` path through config, auth/assignment, `/api/sync/push`,
clean acceptance, and scoped operational report visibility.

That proof is not the whole pilot increment. NW-150 should produce the reusable
package skeleton only; mobile capture, supervisor detail view, local Keycloak
pilot path, local on-prem preflight, and owner go/no-go remain later backlog
items.

## Guardrails

Do not add:

- real data;
- account import;
- submitted-record import/replay;
- legacy form importer;
- repeatable-section platform support;
- production approval;
- stock ledger correctness;
- broad reporting/import/export;
- queue/batch automation;
- tenant/control-plane work.

Also do not change envelope fields/types, stored event meaning, sync protocol,
new scope mechanisms, validation policy, BAR, CDL, gap-register standing,
or production readiness standing.

## Expected Output

One of:

1. A reusable non-production/pilot stock operations package skeleton with
   focused validation proving it can be loaded, published, or exercised; or
2. A precise stop report naming the exact existing-package/provisioning surface
   that blocks a safe reusable skeleton.

The preferred successful output should include:

- package/provisioning material for `stocktake_line/v1` and `stock_operations`;
- documentation or usage notes for synthetic local/on-prem setup assumptions;
- focused test or runtime proof;
- status/backlog acceptance evidence after validation.

## Expected Successor

If implementation succeeds without finding a real blocker, select exactly one
next route:

```text
NW-151 - Mobile stocktake capture smoke
```

If implementation reveals a real blocker, stop and name the blocker instead of
selecting NW-151 by default.

## Validation

Use `docs/agent-working-surface/validation-matrix.md` for touched surfaces.

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
```

For package/provisioning/config implementation, run the narrowest relevant
focused server test first, then the required broader gate for touched server
config/provisioning surfaces unless the task stops on a precise blocker before
code changes.

Expected focused evidence should prove the package material can be loaded,
published, or exercised. If contracts, config-package schemas, mobile code, or
runtime sync behavior are touched, run the additional gates required by the
validation matrix.

## Stop Conditions

Stop and report before work that requires:

- real users, real stock data, raw legacy data, account import, or submitted
  record import/replay;
- a legacy form importer or repeatable-section platform support;
- production approval, controlled operational use, cutover, stock ledger
  correctness, or production stock truth;
- broad reporting/import/export, queue/list/batch automation, or review queues;
- tenant/control-plane, tenant-aware runtime/storage/sync/config/auth, or new
  scope mechanisms;
- contract/schema/sync/envelope changes, BAR/CDL/gap-register changes, or
  validation-policy changes.
