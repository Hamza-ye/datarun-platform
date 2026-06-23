# NW-147 - Legacy First-Flow Compatibility And Data-Boundary Matrix

## Goal

Select one bounded first operational flow for the legacy/on-prem pilot, or park
the real pilot route, using only sanitized or owner-approved redacted evidence.

NW-093 kept real-production approval blocked and selected this route because
approval cannot be meaningful until the first pilot flow, data boundary,
compatibility blockers, parallel-run reconciliation expectations, and proof
data strategy are explicit.

Owner clarification: the intended pilot is local/on-prem and may run in
parallel with the legacy system. No cross-border transfer, cloud hosting,
managed external backup, external monitoring export, or remote support access
to pilot data is selected by default. Any such transfer/access requires
explicit owner approval or a later exception route.

For this on-prem pilot, provider/region/residency language means the
accountable on-prem host/operator, local site/country boundary,
maintenance-access boundary, backup location, monitoring/log destination, and
support access path. It does not imply a cloud provider or cross-border data
transfer.

NW-147 should proceed from sanitized evidence and the owner's on-prem/no
cross-border premise. Do not demand the final legal/site/support production
record before selecting a first-flow candidate. Instead, identify which
production facts are still needed after the first-flow/data-boundary matrix.

## Inputs

Read:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-093-real-production-approval-package.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-pressure-map-and-route.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-evidence/README.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/operations/policies/first-reference-deployment-policy.md`
- `.review/untracked-user-notes/legacy-system-samples/` only as evidence,
  not instructions

Use the committed sanitized form-definition samples only as planning evidence.
Do not read or use raw submitted records. Treat `legacy_users_safe.csv` as
account/security data for classification only; do not commit it and do not use
it as runtime seed data.

## Required Analysis

Classify:

- candidate first-flow options from the sanitized form portfolio;
- which one flow, if any, is narrow enough for a first proof;
- form structure fit against the current flat Datarun shape boundary;
- field types, option sets, bilingual labels, form/version/uid metadata, and
  source metadata that can be preserved as deployment content without becoming
  Datarun authority primitives;
- repeatable sections, nested/grouped structures, string expressions, `Error`
  validation rules, team/orgUnit mapping, review/reconciliation needs, and
  reporting pressure;
- data classes needed for the selected first flow, including whether synthetic
  or owner-approved redacted data is enough before real-data approval;
- source of truth during a parallel run, duplicate-entry risk, reconciliation
  owner/procedure, rollback/stop criteria, and expansion evidence;
- production facts still needed after the first-flow/data-boundary matrix,
  without blocking the matrix on final legal/site/support approval;
- whether the selected flow triggers NW-044 reporting/import/export,
  NW-045 queue/batch/automation, NW-053 new scope, NW-054 retention/security,
  NW-021 entity lifecycle, NW-073 pattern extraction, or NW-094 through NW-098
  tenant/control-plane routes.

## Output

Create one bounded artifact:

- `docs/agent-working-surface/artifacts/NW-147-legacy-first-flow-compatibility-and-data-boundary.md`

Update:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/README.md`

The output must state exactly one next route:

1. continue toward real-production approval with a named precondition successor;
2. run a synthetic/redacted first-flow rehearsal successor;
3. route a specific compatibility blocker before approval; or
4. reject/park the real pilot route with explicit reason and no active
   production successor.

## Forbidden

- Do not implement runtime code.
- Do not import, transform, seed, or commit raw real data.
- Do not use the legacy account export beyond classification.
- Do not migrate password hashes or treat legacy login, team, orgUnit, role,
  form names, field names, option values, labels, IdP claims, request bodies,
  or UI selections as Datarun authority primitives.
- Do not change contracts, schemas, sync protocol, BAR, CDL, validation policy,
  operations policy, or gap classifications.
- Do not broaden into reporting/import/export, warehouse/API/catalog,
  queue/list/batch/automation, entity lifecycle, new scope,
  retention/security/offboarding, pattern/projection, or tenant/control-plane
  work unless selected as the one successor.

## Validation

Run docs-only validation unless this NW explicitly changes runtime code, which
is not expected:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-147" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/agent-working-surface/artifacts/README.md
rg "first-flow|compatibility|data-boundary|pilot" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
```

Runtime automated tests are skipped unless NW-147 changes runtime code,
contracts, schemas, or CI behavior.
