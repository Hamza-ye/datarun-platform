# NW-151 - Bind Stock Operations Subject-Anchor Boundary And Harden Proof Oracle

## Goal

Before mobile stocktake capture, make the `stocktake_line/v1` subject anchor
explicit and mechanically guarded.

The goal is not to decide a full stock domain model. The goal is to prevent
hidden test assumptions around `subject_ref`, supervisor assignment, and scoped
report visibility from becoming product authority by accident.

## Inputs

Read:

- `docs/status.md` Current Routing.
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-147
  through NW-151.
- `docs/agent-working-surface/artifacts/NW-147-legacy-first-flow-compatibility-and-data-boundary.md`.
- `docs/agent-working-surface/prompts/NW-148-implement-synthetic-stock-operations-first-flow.md`.
- `docs/agent-working-surface/prompts/NW-149-stock-operations-pilot-goal-and-ordered-backlog.md`.
- `docs/agent-working-surface/prompts/NW-150-stock-operations-pilot-package-skeleton.md`.
- `docs/specifications/product/stock-operations-pilot-pm-handoff.md`.
- `contracts/envelope.schema.json`.
- `docs/architecture/adrs-decisions-canonical-ledger/README.md`.
- `docs/specifications/platform/configuration-package-and-shapes.md`.
- `docs/specifications/platform/assignment-scope-and-administration.md`.
- `docs/implementation/module-interfaces.md`.
- `server/src/main/java/dev/datarun/server/event/EventRepository.java`.
- `server/src/main/java/dev/datarun/server/authorization/ActiveAssignment.java`.
- `server/src/test/java/dev/datarun/server/e2e/SyntheticStockOperationsFirstFlowIntegrationTest.java`.
- PR #60 package files under
  `deploy/reference/pilot-packages/stock-operations/`.
- `docs/agent-working-surface/validation-matrix.md`.

Read nested `server/AGENTS.md` before touching server test files. Read
`mobile/AGENTS.md` only if mobile files are touched.

## Required Behavior

Patch the PM handoff and package wording so it says:

- `subject_ref` is required architecture;
- `stocktake_line/v1` is deployer shape configuration;
- `stock_operations` is deployer activity configuration;
- `subject_binding = null`, so subject stamping comes from
  capture/session/operator context;
- the first pilot anchor is a pre-established pilot stock-scope subject
  representing the counted stock-holding location or storage point;
- pre-established means stable subject UUID plus `subject_locations` mapping
  under selected pilot geography plus worker/supervisor assignments covering
  that geography/activity plus capture/session/operator context that can stamp
  it into `subject_ref`;
- this is not warehouse lifecycle, stock ledger, item catalog, stocktake
  session lifecycle, production stock truth, `process` subject emission, or a
  new platform mechanism.

Patch `SyntheticStockOperationsFirstFlowIntegrationTest` so the hidden subject
is no longer anonymous. Use `pilotStockScopeSubjectId` or a similarly bounded
name, not `warehouseSubject`.

Add assertions that stocktake events:

- have `subject_ref.type == "subject"`;
- have `subject_ref.id` equal to the pilot stock-scope subject;
- get scoped visibility through that subject's location path;
- do not rely on activity `review` for supervisor scoped standing.

If removing `roles.putArray("supervisor").add("review")` breaks the proof,
stop and report the coupling. Do not silently keep review authority.

## Product Boundary

NW-151 may claim only subject-anchor clarification and proof-oracle hardening
for the stock operations pilot package. It does not approve mobile capture,
real users/data, production readiness, controlled operational use, stock ledger
correctness, warehouse lifecycle, item/catalog modeling, stocktake session
lifecycle, a review workflow, or new platform mechanisms.

Supervisor standing remains scoped read visibility through assignment scope and
existing web-admin access/read commands. Do not add a review workflow, review
queue, supervisor review action, or `event.type = "review"`.

## Expected Output

One of:

1. Bounded PM handoff/package wording plus focused server proof hardening; or
2. A precise stop report naming the hidden coupling that blocks the subject
   anchor from being safely stated.

On success, update status/backlog acceptance evidence and select exactly one
next route:

```text
NW-152 - Mobile stocktake capture smoke
```

NW-152 must prove mobile can select or stamp the pre-established pilot
stock-scope subject, or stop with the exact missing mobile surface.

## Validation

Use `docs/agent-working-surface/validation-matrix.md` for touched surfaces.

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
```

For the server proof hardening, run the focused server test first:

```bash
cd /home/hamza/datarun-platform/server
./mvnw -Dtest=SyntheticStockOperationsFirstFlowIntegrationTest test
```

Run the required broader server gate unless the task stops on a precise
blocker before server test changes are accepted:

```bash
cd /home/hamza/datarun-platform/server
./mvnw test
```

Package JSON changes must pass `jq empty` on the touched package JSON files.

## Stop Conditions

Stop and report before work that requires:

- real users, real stock data, raw legacy data, account import, or submitted
  record import/replay;
- mobile capture implementation before the subject-anchor boundary is accepted;
- local Keycloak/principal-binding pilot implementation;
- a legacy form importer or repeatable-section platform support;
- production approval, controlled operational use, cutover, stock ledger
  correctness, or production stock truth;
- broad reporting/import/export, queue/list/batch automation, or review queues;
- tenant/control-plane, tenant-aware runtime/storage/sync/config/auth, or new
  scope mechanisms;
- contract/schema/sync/envelope changes, BAR/CDL/gap-register changes, or
  validation-policy changes.
