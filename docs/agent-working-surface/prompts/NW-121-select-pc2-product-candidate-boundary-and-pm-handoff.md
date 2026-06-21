# NW-121 - Select PC2 product candidate boundary and PM handoff

## Goal

Select exactly one Product Candidate 2 boundary and produce the PM handoff /
routing output for that candidate.

This is product-planning and product-handoff work. It must not implement PC2,
change runtime behavior, accept product behavior beyond the selected planning
boundary, approve real production, or open tenant/control-plane/runtime
architecture work by drift.

## Why Now

NW-119 parked the PC1 proof route after synthetic managed-lab evidence. NW-120
classified delivery-readiness and PC2 intake concerns and selected this one
successor. The next professional step is not hardening everything first; it is
choosing one bounded PC2 candidate while making the required gates and triggers
explicit.

## Inputs

Read these surfaces first:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-120-delivery-readiness-and-pc2-intake.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/specifications/product/product-candidate-handoff-template.md`
- `docs/scenarios/README.md`
- `docs/reviews/scenario-baseline-pressure-map.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`

Use the specific scenario files, PC1 artifacts, code files, or tests only if the
candidate comparison needs them. Do not reopen broad architecture history unless
a gap-routing trigger fires.

## Expected Output

Primary output if PC2 is selected:

```text
docs/specifications/product/product-candidate-2-pm-handoff.md
```

Use `docs/specifications/product/product-candidate-handoff-template.md` as the
structure.

This PM handoff is a durable planning surface only. It does not accept product
behavior, runtime implementation, production approval, architecture authority,
validation policy, contracts, schemas, BAR, CDL, or gap-register standing.

Use this artifact path only if NW-121 concludes that PC2 is not ready to select
and instead parks/blocks selection or routes a prerequisite:

```text
docs/agent-working-surface/artifacts/NW-121-pc2-product-candidate-boundary-and-pm-handoff.md
```

In that case, do not claim PC2 has been selected.

The output must include:

- selected PC2 boundary, exactly one;
- why that boundary wins over other candidate fronts;
- scenario evidence used;
- explicit non-goals;
- validation gates and known validation debt;
- Secure SDLC / security gates;
- reliability / operations gates;
- architecture/platform prerequisites;
- code-boundary guardrails, including EventRepository/read-model and
  WebAdminOperationalViewService reach-through concerns;
- NW-093 production approval standing;
- NW-094 through NW-098 tenant/control-plane standing;
- not-selected-now table with triggers;
- whether a single implementation successor is ready, or why not.

## Candidate Fronts To Compare

- Setup/admin polish.
- Assignment/admin operations polish.
- Mobile field workflow polish.
- Readiness/freshness/attention expansion.
- Reporting/import/export/aggregate oversight.
- Conflict queue or resolution workflow.
- Pattern registry/projection follow-through.
- S06/entity lifecycle.
- Tenant/control-plane.

Select one bounded PC2 candidate, not a bundle.

## Guardrails

- Real users/data require NW-093 first.
- Reporting/import/export/aggregate work requires NW-044 or a bounded reporting
  spec route before delivery.
- Conflict UI/current flag behavior dependence requires NW-072; automation,
  batch, resolver reassignment, or auto-resolution requires NW-045.
- Pattern traversal/reporting/inventory/projection/API work requires NW-073.
- S06/entity lifecycle requires NW-021.
- Managed control-plane or tenant-aware runtime work requires NW-094 through
  NW-098 as applicable.
- A second operational read surface, reporting-like view, drill-back,
  multi-item attention view, export, or aggregate must route the read-model /
  query boundary before implementation.

## Forbidden Work

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register mutation, production approval, real users/data,
tenant-aware runtime implementation, reporting/import/export acceptance,
conflict automation/batch acceptance, pattern projection changes, entity
lifecycle acceptance, or broad documentation cleanup.

## Validation

Run docs-only validation unless the selected output explicitly changes a
non-doc surface:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-121" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md

# If PC2 is selected:
test -f docs/specifications/product/product-candidate-2-pm-handoff.md

# If PC2 is not selected:
test -f docs/agent-working-surface/artifacts/NW-121-pc2-product-candidate-boundary-and-pm-handoff.md
```

Run the file check that matches the selected outcome and add grep/index checks
required by the local convention.

Runtime tests should be skipped with rationale for docs-only planning work.

## Stop Conditions

Stop and report if the work requires:

- choosing real users/data/provider/region/jurisdiction/support;
- changing production readiness standing;
- making tenant-aware runtime decisions;
- changing auth/security authority;
- changing event semantics or repository architecture;
- changing validation gates;
- accepting reporting/import/export behavior;
- accepting conflict automation/batch behavior;
- reopening architecture decisions;
- using SPEC-* as active control.
