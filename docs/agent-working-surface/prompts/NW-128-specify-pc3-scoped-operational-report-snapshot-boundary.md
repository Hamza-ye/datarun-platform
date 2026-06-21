# NW-128 - Specify PC3 Scoped Operational Report Snapshot Boundary

## Goal

Specify the prerequisite product/platform boundary for the Product Candidate 3
boundary selected by NW-127:

```text
Product Candidate 3 - Scoped Operational Report Snapshot
```

This is specification and routing work only. It must not implement PC3, change
runtime behavior, accept broad reporting/import/export behavior, approve real
production, close PC2 live-lab proof debt, mutate the lab, or open
tenant/control-plane/runtime architecture work by drift.

## Why Now

NW-127 selected PC3 as one scoped read-only operational report snapshot. That
candidate is reporting-like, so implementation must not start until the
freshness, completeness, unresolved-issue treatment, access, traceability, and
read-model/query boundary are specified.

## Inputs

Read these surfaces first:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-3-pm-handoff.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/operational-ux-layering-companion.md`
- `docs/scenarios/README.md`
- `docs/reviews/scenario-baseline-pressure-map.md`
- `docs/reviews/viability-closure-review.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/specifications/product/product-candidate-handoff-template.md`
- `docs/documentation-organization.md`

Use specific scenario files, code files, tests, contracts, or platform specs
only if the boundary comparison needs them. Do not reopen broad architecture
history unless a gap-routing trigger fires.

## Expected Output

Primary durable output:

```text
docs/specifications/platform/scoped-operational-report-snapshot-boundary.md
```

Use `docs/documentation-organization.md` and the platform specification index
requirements.

The output must include:

- exact PC3 snapshot scope;
- access and no-leakage boundary;
- freshness and staleness semantics;
- completeness and uncertainty caveats;
- unresolved issue exclusion/count/link treatment;
- allowed traceability to source work or accepted attention context;
- read-model/query boundary;
- user-facing wording guardrails;
- explicit non-goals;
- validation gates and known validation debt;
- Secure SDLC / security gates;
- reliability / operations gates;
- architecture escalation triggers;
- NW-093 production approval standing;
- NW-126 blocked PC2 live-lab proof debt standing;
- NW-094 through NW-098 tenant/control-plane standing;
- not-selected-now table with triggers;
- whether a single implementation successor is ready, or why not.

Update:

- `docs/specifications/platform/README.md`
- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`

Update `docs/specifications/product/product-candidate-3-pm-handoff.md` only if
NW-128 changes the selected boundary wording or implementation readiness call.

## Candidate Boundaries To Compare

Compare only bounded versions of the PC3 snapshot:

- current scoped standing snapshot;
- current-period reporting snapshot;
- freshness-only operational summary;
- unresolved-issue treatment summary;
- traceable snapshot with limited links to accepted work/attention context.

The selected boundary must remain one PC3 snapshot, not a bundle.

## Guardrails

- Real users/data require NW-093 first.
- PC2 live browser proof requires NW-126 to be unblocked and accepted first.
- Broad reporting, structured export/import, warehouse, analytics, report API,
  report catalog, or interoperability work requires NW-044.
- Queue/list/multi-item attention review routes through GAP-CONFLICT-01 before
  implementation.
- Conflict automation, batch workflow, resolver reassignment, or
  auto-resolution requires NW-045.
- Pattern traversal/reporting, pattern inventory expansion, workflow projection
  changes, or pattern API/product work requires NW-073.
- S06/entity lifecycle requires NW-021.
- New subject/query/custom scope or hidden sync/access scope requires NW-053.
- Retention/security/offboarding/local sensitivity promises require NW-054.
- Managed control-plane or tenant-aware runtime work requires NW-094 through
  NW-098 as applicable.
- A second operational read surface, reporting-like view, drill-back,
  multi-item attention view, export, or aggregate must route the read-model /
  query boundary before implementation.

## Forbidden Work

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register mutation, production approval, real users/data,
tenant-aware runtime implementation, reporting/import/export acceptance,
reporting warehouse/API/catalog acceptance, conflict automation/batch
acceptance, conflict queue/list implementation, pattern projection changes,
entity lifecycle acceptance, live PC2 browser proof, lab mutation, or broad
documentation cleanup.

Do not select NW-073 unless the chosen PC3 boundary actually depends on pattern
registry/projection behavior.

## Validation

Run docs-only validation unless the selected output explicitly changes a
non-doc surface:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-128" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/specifications/platform/scoped-operational-report-snapshot-boundary.md
rg "Scoped Operational Report Snapshot" docs/specifications/platform/README.md
```

Add targeted grep/index checks required by the local convention.

Runtime tests should be skipped with rationale for docs-only specification
work.

## Stop Conditions

Stop and report if the work requires:

- choosing real users/data/provider/region/jurisdiction/support;
- changing production readiness standing;
- running PC2 live browser proof;
- touching the lab or retained PC2 environment state;
- making tenant-aware runtime decisions;
- changing auth/security authority;
- changing event semantics or repository architecture;
- changing validation gates;
- accepting broad reporting/import/export behavior;
- accepting conflict automation/batch behavior;
- accepting queue/list/multi-item review behavior;
- accepting pattern projection changes or pattern API/product behavior without
  NW-073;
- accepting entity lifecycle;
- reopening architecture decisions;
- using SPEC-* as active control.
