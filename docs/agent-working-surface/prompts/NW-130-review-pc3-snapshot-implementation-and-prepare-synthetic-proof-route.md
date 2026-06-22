# NW-130 - Review PC3 Snapshot Implementation And Prepare Synthetic Proof Route

## Goal

Review the accepted NW-129 `/web-admin/operational/report` implementation
against the PC3 PM handoff and accepted NW-128 platform boundary. If the
implementation standing is sufficient, prepare exactly one bounded PC3
synthetic walkthrough/proof route.

This is product-validation / implementation-review work. It must not implement
runtime behavior, change product scope, run live proof, run lab proof, or
approve real production.

## User Value / Why Now

NW-129 delivered the first PC3 `Scoped Operational Report Snapshot`. The next
useful product evidence is a fresh-context review that says whether the
snapshot is ready for synthetic owner proof and, if it is, gives the next agent
one bounded proof packet to run.

This review keeps PC3 focused on one scoped snapshot. It must not broaden PC3
into reporting infrastructure, report APIs, exports/imports, conflict queues,
batch/automation, pattern projection work, new scope mechanisms,
tenant/control-plane work, PC2 live-lab proof, real users/data, or production
approval.

## Inputs

Read these surfaces first:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-3-pm-handoff.md`
- `docs/specifications/platform/scoped-operational-report-snapshot-boundary.md`
- `docs/agent-working-surface/validation-matrix.md`
- `server/AGENTS.md`
- NW-129 accepted implementation standing from commits `6336d7e` and
  `8b4daaf`

Review these implementation files as needed:

- `server/src/main/java/dev/datarun/server/authorization/ScopedOperationalReportSnapshotService.java`
- `server/src/main/java/dev/datarun/server/authorization/WebAdminOperationalViewController.java`
- `server/src/main/java/dev/datarun/server/authorization/WebAdminSessionController.java`
- `server/src/main/java/dev/datarun/server/event/EventRepository.java`
- `server/src/main/resources/templates/web-admin/operational-report.html`
- `server/src/main/resources/templates/web-admin/operational.html`
- `server/src/test/java/dev/datarun/server/authorization/WebAdminOperationalReportSnapshotIntegrationTest.java`

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if a stop condition fires or the review finds boundary ambiguity that
cannot stay inside accepted PC3 scope.

Do not reopen broad architecture history unless a stop condition fires.

## Expected Output

Create one non-authoritative product-validation artifact:

```text
docs/agent-working-surface/artifacts/NW-130-pc3-post-nw129-snapshot-standing-and-successor-selection.md
```

The artifact should:

- state that NW-130 is a review/selection artifact, not product behavior,
  runtime behavior, validation policy, or production approval;
- map NW-129 implementation standing to the PC3 PM handoff and accepted NW-128
  boundary;
- review access gates, no-leakage behavior, scope-before-aggregate ordering,
  freshness states, completeness caveats, unresolved issue treatment, limited
  traceability, and typed read-model/query boundary;
- account for NW-129 validation evidence from focused and full server gates;
- classify the PC3 snapshot standing as `synthetic-demo-ready`, `friction`,
  `blocked`, or `out-of-scope`;
- if ready, state `synthetic-demo-ready, not proof-complete, not
  real-production-ready`;
- record any friction as candidate follow-up pressure only unless it blocks
  proof;
- route any stop-trigger pressure to the existing preserved routes instead of
  burying it;
- recommend exactly one next route.

If the review finds the implementation sufficient for proof, add one successor
prompt:

```text
docs/agent-working-surface/prompts/NW-131-run-pc3-synthetic-walkthrough-proof.md
```

The recommended proof example is S26 operational reporting, because it is the
main PC3 scenario pressure. Use S22 coordinated campaign or S27 logistics only
if the review records why that example better explains the PC3 proof.

## Allowed Changes

- Add the NW-130 product-validation artifact.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Update `docs/agent-working-surface/artifacts/README.md`.
- Add exactly one successor prompt only if NW-130 selects a concrete next
  route.

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

NW-130 is accepted only when:

- one PC3 post-NW-129 standing artifact exists;
- the review checks NW-129 against the PC3 handoff and NW-128 platform
  boundary;
- the review explicitly covers access, scope/no-leakage, freshness,
  unresolved-issue treatment, trace limits, and read-model/query boundaries;
- runtime/code standing remains unchanged;
- the next route is exactly one of: PC3 synthetic proof, one small bounded
  polish/fix route, park, or an explicit stop route;
- real-production standing remains blocked behind NW-093 unless a later owner
  decision selects that route.

## Validation

Run docs-only validation unless the work unexpectedly changes code:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-130" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-130-pc3-post-nw129-snapshot-standing-and-successor-selection.md
```

If NW-130 adds a successor prompt, also run:

```bash
test -f docs/agent-working-surface/prompts/NW-131-run-pc3-synthetic-walkthrough-proof.md
rg "NW-131" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/agent-working-surface/prompts/NW-131-run-pc3-synthetic-walkthrough-proof.md
```

Runtime tests are skipped because NW-130 is product-validation/routing work
unless a later selected implementation route changes runtime behavior.

## Stop Conditions

Stop and report if the work requires:

- runtime implementation or test changes inside NW-130;
- real users or real organizational data;
- real-production approval;
- PC2 live browser proof or lab mutation;
- broad reporting dashboards, exports, imports, warehouses, analytics, report
  APIs, report catalogs, saved views, arbitrary filters, completeness
  semantics, or drilldown;
- conflict queue/list/multi-item review, broad conflict console, filters,
  batch review, resolver reassignment, automation, auto-resolution, or flag
  reporting;
- resolver eligibility broadening beyond accepted exact stored
  `designated_resolver` behavior;
- pattern traversal/reporting, pattern inventory expansion, workflow
  projection changes, or pattern API/product work;
- new subject/query/custom scope or hidden sync/access scope;
- retention/security/offboarding promises;
- entity lifecycle;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, sync, validation-policy, CI,
  BAR, CDL, or gap-register changes.

## Commit Boundary

Use a docs/product-validation commit if NW-130 lands. Do not combine NW-130
with runtime implementation, product-spec changes, platform-spec changes,
validation-policy changes, CI changes, BAR/CDL/gap-register updates, or
unrelated cleanup.
