# NW-148 - Implement Synthetic Stock Operations First-Flow

## Goal

Implement or configure the smallest executable stock operations vertical slice
using existing Datarun capabilities.

Selected boundary from NW-147:

```text
Stock operations starter slice using flat Datarun events.
Minimum event: warehouse stocktake line.
Optional only if already cheap within the same slice: receipt/issue line.
Each legacy repeatable row maps to one Datarun event.
```

This is implementation/proof work, not another planning packet. Do not create
another planning artifact. If the existing runtime cannot execute the path,
stop and identify the exact missing implementation surface.

## Inputs

Read:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-147-legacy-first-flow-compatibility-and-data-boundary.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-evidence/README.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-evidence/supply_wh_mids_stocktake_901_form.json`
- `docs/specifications/platform/configuration-package-and-shapes.md`
- `docs/specifications/platform/expression-language.md`
- `contracts/shape-format.schema.json`
- `contracts/config-package.schema.json`
- `docs/agent-working-surface/validation-matrix.md`
- Existing server config/package/shape/sync/read tests needed for touched code.
- Existing mobile dynamic form tests only if the mobile dynamic form path is
  required and already present.

Also read nested `server/AGENTS.md`, `mobile/AGENTS.md`, or
`contracts/AGENTS.md` only when touching those surfaces.

## Required Worker Behavior

1. Inspect existing config/package/shape/test patterns before editing.
2. Define a flat stocktake-line shape/activity using the existing shape format:
   - date;
   - stock category;
   - quantity;
   - optional source metadata/display labels only.
3. Use synthetic data only.
4. Use existing auth, assignment, config, sync, and read surfaces.
5. Prove one end-to-end path:
   - config exists/published or test-provisioned;
   - synthetic actor can submit stocktake-line work;
   - submitted event is accepted;
   - scoped supervisor/operational view or report can observe it;
   - validation evidence is recorded.
6. If no runtime code is needed, run a live/integration proof and record it.
7. If the existing runtime cannot execute this path, stop and identify the
   exact missing implementation surface. Do not create another planning
   artifact.

## Allowed Implementation Scope

- Server tests, fixtures, or config provisioning if needed.
- Mobile tests only if the mobile dynamic form path is required and already
  present.
- Web-admin/read-view use only if existing surfaces support it.
- Small runtime fix only if it is directly required for this stocktake-line
  path.

## Forbidden

- No raw real data.
- No user/account export.
- No password migration.
- No legacy form importer.
- No repeatable-section support.
- No submitted-record import/replay.
- No broad reporting/export/import.
- No queue/batch automation.
- No tenant/control-plane.
- No full legacy form parity claim.
- No stock ledger correctness claim.
- No production readiness claim.
- No new architecture/control documents unless a real blocker is found.

## Product Boundary

The first Datarun pilot does not need to run as a shadow duplicate of an
existing legacy flow. Legacy can continue where it already runs, while Datarun
runs in a new or non-legacy-covered operational lane. Do not make dual-entry
reconciliation the default blocker.

Repeatable legacy rows should be modeled as multiple Datarun events. Do not
require repeatable-section platform support or a legacy form importer for this
first slice.

## Expected Output

One of:

1. Code/test/config/runtime proof that the synthetic stocktake-line vertical
   slice executes end to end; or
2. A precise stop report naming the exact missing implementation surface that
   blocks the path.

Do not end with another matrix, rehearsal-plan artifact, or planning-only
successor.

## Validation

Use `docs/agent-working-surface/validation-matrix.md` for touched surfaces.

Minimum expected validation:

```bash
cd /home/hamza/datarun-platform
git diff --check
```

If server code/tests/config fixtures are touched, run the narrowest relevant
server test first, then the required full server gate unless the task stops on
a precise implementation blocker before code changes.

If mobile code/tests are touched, run the narrowest relevant Flutter test first,
then the required mobile gate. `flutter analyze` remains known-red and is not a
hard gate unless separately selected.

Runtime tests are not optional if the slice changes runtime behavior. If no
runtime code is needed, the live/integration proof is the focused evidence.
