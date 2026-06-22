# NW-131 - Run PC3 Synthetic Walkthrough/Proof

## Goal

Capture synthetic, non-sensitive proof evidence for Product Candidate 3 after
NW-130 marked the accepted `/web-admin/operational/report` snapshot
`synthetic-demo-ready, not proof-complete, not real-production-ready`.

This is product-validation / owner-review evidence work. It must not implement
runtime behavior, change product scope, run live proof, run lab proof, or
approve real production.

## User Value / Why Now

NW-129 accepted the bounded PC3 Scoped Operational Report Snapshot
implementation. NW-130 found no required polish before proof and selected a
single synthetic proof packet. NW-131 should turn that readiness into one
reviewable evidence packet for owner decision-making.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-3-pm-handoff.md`
- `docs/specifications/platform/scoped-operational-report-snapshot-boundary.md`
- `docs/agent-working-surface/artifacts/NW-130-pc3-post-nw129-snapshot-standing-and-successor-selection.md`
- accepted NW-129 implementation standing from status/backlog and commits
  `6336d7e` and `8b4daaf`
- `docs/agent-working-surface/validation-matrix.md`

Use S26 operational reporting and aggregate oversight as the proof example. It
is the main PC3 scenario pressure and directly exercises scoped standing,
freshness, unresolved issue treatment, traceability, and completeness caveats.
Use S22 coordinated campaign or S27 logistics only as secondary explanatory
examples if the proof artifact records why S26 alone is insufficient.

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if proof evidence creates pressure for broad reporting/import/export,
queue/list/multi-item review, automation, resolver reassignment, resolver
eligibility broadening, pattern traversal/projection work, real production,
retention/security, tenant/control-plane, contracts, sync/access, or another
gap-trigger surface.

Do not open broad architecture history unless a stop condition fires.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-131-pc3-synthetic-walkthrough-proof.md
```

The artifact should:

- restate that PC3 is `synthetic-demo-ready, not proof-complete, not
  real-production-ready` at the start of the packet;
- name the synthetic/non-sensitive S26 example used;
- walk the PC3 journey from web-admin session to opening
  `/web-admin/operational/report` and interpreting the snapshot;
- classify each proof beat as `PASS`, `FRICTION`, `NOT_RUN`, or
  `OUT-OF-SCOPE`;
- cover access gates, scoped/no-leakage standing, scope-before-aggregate
  ordering, freshness states, completeness caveats, unresolved issue
  treatment, limited traceability, and read-model/query boundaries;
- confirm no broad report API, export/import, warehouse, report catalog,
  arbitrary filters, cadence, percentages, completion rates, or all-clear
  claims were used;
- confirm no conflict queue/list/batch/automation/resolver reassignment path
  was used;
- record any friction as candidate follow-up pressure only;
- confirm no real users or real organizational data were used;
- confirm no real-production approval was granted;
- recommend exactly one next route: park, one small bounded polish/fix,
  real-use preparation through NW-093, or another explicitly bounded owner
  route.

## Allowed Changes

- Add the NW-131 synthetic proof artifact.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Update `docs/agent-working-surface/artifacts/README.md`.
- Add one successor prompt only if NW-131 selects a concrete next route.

## Forbidden Changes

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register changes, product spec changes, platform spec changes,
real-production approval, real users/data, PC2 live proof, lab mutation,
reporting/export/import/warehouse/API/catalog behavior, dashboards, arbitrary
filters, conflict queue/list/multi-item review, batch workflow, resolver
reassignment, automation, resolver eligibility broadening,
retention/security/offboarding promises, entity lifecycle, new scope
mechanisms, pattern projection/API work, tenant/control-plane work, mobile
code, or server/web-admin feature implementation.

Do not use real users or real organizational data. If real-use preparation
becomes selected, stop and route through NW-093 before continuing.

## Acceptance Criteria

NW-131 is accepted only when:

- one PC3 synthetic proof evidence artifact exists;
- each proof beat has clear standing;
- the proof remains one scoped report snapshot and does not become a reporting
  suite, export/import route, broad read API, report catalog, queue/list review,
  batch workflow, or production approval;
- real-production standing remains blocked behind NW-093 unless a later owner
  decision selects that route;
- status/backlog reflect the resulting route and no active implementation gate
  is opened by accident;
- validation evidence is docs-only and exact unless the packet explicitly
  records a manual runtime inspection.

## Validation

Run docs-only validation unless the packet inspects runtime manually:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-131" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-131-pc3-synthetic-walkthrough-proof.md
grep -n "synthetic-demo-ready" docs/agent-working-surface/artifacts/NW-131-pc3-synthetic-walkthrough-proof.md
```

Runtime tests are skipped because this is docs/product-validation only unless
a future selected packet explicitly adds manual environment evidence.

## Stop Conditions

Stop and report if the work requires:

- real users or real organizational data;
- real-production approval inside NW-131;
- PC2 live browser proof or lab mutation;
- reporting dashboards, exports, imports, warehouses, analytics, broad read
  APIs, report catalogs, saved views, arbitrary filters, completeness
  semantics, cadence/overdue/deadline semantics, percentages, completion rates,
  all-clear claims, or drilldown;
- queue/list/multi-item review, broad conflict console, filters, batch review,
  resolver reassignment, automation, auto-resolution, or flag reporting;
- resolver eligibility broadening beyond accepted exact stored
  `designated_resolver` behavior;
- pattern traversal/reporting, pattern inventory expansion, workflow
  projection changes, or pattern API/product work;
- new subject/query/custom scope or hidden sync/access scope;
- retention/security/offboarding promises;
- entity lifecycle;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, sync, validation-policy, CI,
  BAR, CDL, or gap-register changes;
- runtime implementation.

## Commit Boundary

Use a docs/product-validation commit if NW-131 lands. Do not combine NW-131
with runtime implementation, product-spec changes, platform-spec changes,
validation-policy changes, CI changes, BAR/CDL/gap-register updates, or
unrelated cleanup.
