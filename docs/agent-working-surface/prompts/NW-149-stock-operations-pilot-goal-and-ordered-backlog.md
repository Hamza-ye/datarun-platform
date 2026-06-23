# NW-149 - Stock Operations Pilot Goal And Ordered Backlog

## Goal

Create one concise PM handoff for the first local/on-prem stock operations
pilot in a lane not currently covered by the legacy system.

This is product-planning and route-selection work only. It must not implement
the pilot, approve real production, approve real users/data, change runtime
behavior, change contracts/schemas/sync semantics, or create a broad matrix.

## Inputs

Read only:

- `docs/status.md` Current Routing and the NW-148 standing.
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-147,
  NW-148, and NW-149.
- `docs/agent-working-surface/prompts/NW-148-implement-synthetic-stock-operations-first-flow.md`.
- `server/src/test/java/dev/datarun/server/e2e/SyntheticStockOperationsFirstFlowIntegrationTest.java`.
- `docs/agent-working-surface/validation-matrix.md`.
- `docs/documentation-organization.md`.
- `docs/specifications/product/product-candidate-handoff-template.md` for
  metadata shape only.

Do not reopen broad architecture history unless a stop condition fires.

## Product Goal To Encode

Datarun should run the first local/on-prem stock operations pilot in a lane not
currently covered by the legacy system.

Field users can capture stock operation events on Datarun, sync them to the
local/on-prem server, and supervisors can inspect scoped operational standing,
while the owner can operate the environment safely with explicit auth, data,
support, backup/restore, and go/no-go gates.

## Initial Scope

- Stocktake line first.
- Receipt, issue, and disbursement later only as ordered backlog items.
- Flat Datarun events.
- No legacy form importer.
- No repeatable-section platform work.
- No account import.
- No raw legacy submitted data.
- No cross-border/cloud/remote support data access by default.

## Expected Output

Create one short handoff:

```text
docs/specifications/product/stock-operations-pilot-pm-handoff.md
```

Keep it concise. No broad matrix and no thousand-line artifact.

The handoff must include these sections:

- Product Goal.
- first users/jobs.
- first usable increment.
- Definition of Done.
- ordered Product Backlog.
- explicit security and reliability gates.
- current proof evidence from PR #58 / NW-148.
- exactly one next implementation route.

## Required Handoff Content

The current proof evidence must say that PR #58 / NW-148 keeps
`SyntheticStockOperationsFirstFlowIntegrationTest` as valid runtime evidence:
it proves a server-side synthetic `stocktake_line/v1` path through config,
auth/assignment, `/api/sync/push`, clean acceptance, and scoped operational
report visibility. Also state that a server-only test is evidence, not the
whole pilot increment.

The Definition of Done for a pilot slice is:

- user-visible behavior or executable runtime proof;
- focused tests;
- full required gate passing;
- security/auth boundary explicit;
- reliability/ops impact explicit;
- no raw real data unless approved;
- status/backlog updated;
- one next route selected or clearly parked.

The ordered Product Backlog must keep this order:

1. Stock operations pilot package skeleton.
   Create a reusable non-production/pilot config/provisioning package from the
   test-only stocktake proof.
2. Mobile stocktake capture smoke.
   Prove field-user mobile capture/offline/sync for `stocktake_line/v1`, or
   identify the exact missing mobile surface.
3. Supervisor stock operations view.
   Show stocktake line details, not only aggregate counts, using scoped
   authority.
4. Local Keycloak/principal-binding pilot path.
   Prove self-hosted Keycloak plus explicit Datarun principal bindings for
   pilot worker/supervisor users.
5. Local on-prem operational preflight.
   Backup/restore, monitoring, secrets, support path, and smoke evidence for
   the selected host.
6. Owner go/no-go for limited pilot.
   Only after the above evidence exists.

The exactly one next implementation route should be the first backlog item:

```text
NW-150 - Stock operations pilot package skeleton
```

## Guardrails

- Preserve NW-093 real-production blocking for real users/data, account import,
  submitted-record import/replay, production cutover, and controlled
  operational use until the required owner gates exist.
- Keep Datarun authority on explicit principal binding; do not promote IdP
  groups, claims, UI-selected actors, or imported accounts to authority.
- Keep local/on-prem as the pilot assumption. Do not select cloud hosting,
  cross-border transfer, managed external backup, external monitoring export,
  or remote support data access by default.
- Do not select a legacy form importer, repeatable-section platform work,
  account import, raw submitted-record import, stock ledger correctness,
  production stock truth, broad reporting/import-export, queue/batch
  automation, entity lifecycle, retention/security/offboarding, or
  tenant/control-plane work.

## Updates

- Add the product handoff to `docs/specifications/product/README.md`.
- Update `docs/status.md` Current Routing and What's Next with the NW-149
  outcome and exactly one next implementation route.
- Update `docs/agent-working-surface/platform-next-work-backlog.md` with the
  NW-149 exit evidence and the single successor route.

Do not mark implementation accepted just because the handoff exists.

## Validation

Run docs-only validation:

```bash
cd /home/hamza/datarun-platform
git diff --check
test -f docs/specifications/product/stock-operations-pilot-pm-handoff.md
rg "Product Goal|first usable increment|Definition of Done|ordered Product Backlog" docs/specifications/product/stock-operations-pilot-pm-handoff.md
rg "NW-149|NW-150|stock operations pilot" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
rg "Stock Operations Pilot PM Handoff" docs/specifications/product/README.md
```

Runtime tests are skipped for NW-149 itself because it is product-planning
docs-only work and changes no runtime code, tests, contracts, schemas, mobile
code, server code, CI behavior, validation policy, BAR, CDL, or gap-register
standing.

## Stop Conditions

Stop and report if the work requires:

- real pilot users, real data, raw legacy submitted data, account import, or
  cutover approval;
- architecture authority, contract/schema/sync semantics, stored event meaning,
  or new scope mechanisms;
- retention/security/offboarding promises beyond the explicit pilot gates;
- cloud/cross-border/remote-support data access decisions;
- implementation before the PM handoff selects the single next route.
