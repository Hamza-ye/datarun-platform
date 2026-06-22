# NW-138 - Polish PC4 Handoff Subject UUID And Trace Target Copy

## Goal

Implement one small bounded polish/fix for the accepted
`/web-admin/operational/handoff` surface after NW-137 recorded proof friction
around raw subject UUID display and trace target copy.

This is a narrow web-admin product-UI polish route. It must not change access,
scope, authority, data selection, event meaning, contracts, schemas, sync
behavior, product/platform specs, validation policy, BAR, CDL, gap-register
standing, mobile behavior, or real-production standing.

## User Value / Why Now

NW-137 captured the PC4 synthetic walkthrough/proof. Core S25 handoff beats
passed from accepted NW-135 evidence, with S27 as domain-neutral cross-check
and S22 as secondary continuity evidence. The proof recorded exactly one
concrete next route: make the visible subject UUID and trace target copy easier
for an owner to interpret without inventing subject display-label authority,
entity lifecycle, broad history, drilldown, reporting, or new scope.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-137-pc4-synthetic-walkthrough-proof.md`
- `docs/specifications/product/product-candidate-4-pm-handoff.md`
- `docs/specifications/platform/operational-responsibility-handoff-boundary.md`
- accepted NW-135 implementation standing from status/backlog and
  `WebAdminOperationalHandoffIntegrationTest`
- `docs/agent-working-surface/validation-matrix.md`
- `server/AGENTS.md` if server/web-admin files are touched

Use S25 worker transfer as the primary proof-friction example. Use S27 only as
the domain-neutral copy cross-check and S22 only as secondary continuity
evidence if wording needs it.

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if the polish requires subject display-label authority, subject registry,
entity lifecycle, new scope, broad history/drilldown, reporting/import/export,
queue/list/batch/automation, resolver reassignment, fallback resolver
authority, retention/security/offboarding promises, contracts, sync/access, or
another gap-trigger surface.

## Output

Deliver one small bounded diff over the existing PC4 handoff surface.

Expected implementation shape:

- clarify the visible subject UUID label/helper copy so an owner understands
  the identifier is the scoped subject UUID for the currently visible handoff
  context;
- clarify trace target wording/copy so an owner understands the link opens the
  existing scoped operational context, not broad audit/history drilldown;
- preserve existing web-admin session, `web_admin.access`, and
  `web_admin.read_scoped` gates;
- preserve assignment-scope-before-selection and no-leakage behavior;
- preserve read-only/no-mutation behavior;
- preserve resolver-unassigned/no fallback authority behavior;
- update focused tests for the changed copy.

Prefer copy/helper/label clarification over adding new data sources. Do not
replace the subject UUID with a fabricated label or introduce a new display
authority.

## Allowed Changes

- Narrow server/web-admin handoff template/view-model/copy changes needed for
  the subject UUID and trace target wording.
- Focused web-admin handoff tests for the changed copy.
- `docs/status.md`.
- `docs/agent-working-surface/platform-next-work-backlog.md`.
- Add one successor prompt only if NW-138 selects a concrete next route.

## Forbidden Changes

No product/platform spec changes, runtime authority changes, access changes,
scope changes, new subject/query/custom scope, subject registry, subject
display-label authority, entity lifecycle, broad history/drilldown, reporting,
export/import, warehouse/API/catalog behavior, dashboards, arbitrary filters,
conflict queue/list/multi-item review, batch workflow, resolver reassignment,
fallback resolver authority, automation, resolver eligibility broadening,
retention/security/offboarding promises, contracts, schemas, migrations, sync
protocol changes, CI, validation policy, BAR, CDL, gap-register changes, lab
mutation, PC2 live proof, real users/data, real-production approval,
tenant/control-plane work, mobile code, pattern projection/API work, or
unrelated server/web-admin implementation.

## Acceptance Criteria

NW-138 is accepted only when:

- the PC4 handoff subject UUID wording is clearer for owner review while still
  using accepted scoped information only;
- the PC4 handoff trace target wording is clearer and remains limited to
  existing scoped operational context;
- access gates, scope-before-selection, no-leakage, read-only/no-mutation, and
  resolver-unassigned/no-fallback behavior remain covered;
- no subject display-label authority, subject registry, entity lifecycle, new
  scope, reporting, queue/list/batch/automation, retention/security/offboarding
  promise, contract/schema/sync change, BAR, CDL, gap-register mutation, lab
  mutation, real users/data, or production approval is introduced;
- status/backlog reflect the resulting route.

## Validation

Run the narrowest focused check first, then the required gate for the touched
surface:

```bash
cd /home/hamza/datarun-platform
git diff --check

cd /home/hamza/datarun-platform/server
./mvnw -Dtest=WebAdminOperationalHandoffIntegrationTest test
```

If NW-138 changes service/controller/access/data-selection behavior beyond
copy/template/test polish, run the full server gate:

```bash
cd /home/hamza/datarun-platform/server
./mvnw test
```

Document any skipped full-gate rationale exactly. Runtime tests are not skipped
if server/web-admin implementation or tests change.

## Stop Conditions

Stop and report if the polish requires:

- a subject display-label source, subject registry, entity lifecycle, known-set
  maintenance, duplicate stewardship, merge/split UX, or deactivation;
- new subject/query/custom scope or hidden sync/access scope;
- broad history, audit drilldown, reporting, export/import, warehouse, report
  API, report catalog, arbitrary filters, completion, percentages, cadence, or
  all-clear claims;
- conflict queue/list/multi-item review, broad conflict console, batch review,
  resolver reassignment, fallback resolver authority, automation, or
  auto-resolution;
- retention/security/offboarding promises;
- real users/data or real-production approval;
- PC2 live browser proof or lab mutation;
- pattern traversal/reporting, pattern inventory expansion, workflow
  projection changes, or pattern API/product work;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, sync, validation-policy, CI,
  BAR, CDL, or gap-register changes.

## Commit Boundary

Use a bounded implementation/product-UI polish commit if NW-138 lands. Do not
combine NW-138 with broader reporting, subject/entity, retention/security,
conflict workflow, product spec, platform spec, validation-policy, CI, BAR,
CDL, gap-register, lab, mobile, or unrelated cleanup work.
