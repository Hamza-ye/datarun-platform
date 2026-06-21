# NW-127 - Select PC3 Product Candidate Boundary And PM Handoff

## Goal

Select exactly one Product Candidate 3 boundary and produce the PM handoff /
routing output for that candidate.

This is product-planning and product-handoff work only. It must not implement
PC3, change runtime behavior, accept product behavior beyond the selected
planning boundary, approve real production, or open architecture/control-plane
work by drift.

## Why Now

PC2 has evidence-supported synthetic proof through accepted NW-122 through
NW-124. NW-125 accepted that the PC2 live-lab proof environment is `NOT_READY`
and preserved the lab debt instead of hiding it. NW-126 remains blocked until
lab hostname or fixed-IP SSH access is restored enough to inspect R12 before
touching retained PC2 state.

The product lane can therefore move to the next bounded candidate without
claiming PC2 live-lab proof, losing the NW-126 debt, or starting real-use work.

## Inputs

Read these surfaces first:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/product/product-candidate-handoff-template.md`
- `docs/agent-working-surface/artifacts/NW-120-delivery-readiness-and-pc2-intake.md`
- `docs/agent-working-surface/artifacts/NW-124-pc2-synthetic-walkthrough-proof.md`
- `docs/agent-working-surface/artifacts/NW-125-pc2-synthetic-lab-proof-environment.md`
- `docs/scenarios/README.md`
- `docs/reviews/scenario-baseline-pressure-map.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`

Use specific scenario files, product artifacts, code files, or tests only if
the candidate comparison needs them. Do not reopen broad architecture history
unless a gap-routing trigger fires.

## Expected Output

Primary output if PC3 is selected:

```text
docs/specifications/product/product-candidate-3-pm-handoff.md
```

Use `docs/specifications/product/product-candidate-handoff-template.md` as the
structure.

This PM handoff is a durable planning surface only. It does not accept product
behavior, runtime implementation, production approval, architecture authority,
validation policy, contracts, schemas, BAR, CDL, or gap-register standing.

Use this artifact path only if NW-127 concludes that PC3 is not ready to select
and instead parks/blocks selection or routes a prerequisite:

```text
docs/agent-working-surface/artifacts/NW-127-pc3-product-candidate-boundary-and-pm-handoff.md
```

In that case, do not claim PC3 has been selected.

The output must include:

- selected PC3 boundary, exactly one;
- why that boundary wins over other candidate fronts;
- scenario evidence used;
- explicit non-goals;
- validation gates and known validation debt;
- Secure SDLC / security gates;
- reliability / operations gates;
- architecture/platform prerequisites;
- NW-093 production approval standing;
- NW-126 blocked PC2 live-lab proof debt standing;
- NW-094 through NW-098 tenant/control-plane standing;
- not-selected-now table with triggers;
- whether a single implementation successor is ready, or why not.

## Candidate Fronts To Compare

Start from remaining product fronts after PC1 and PC2, including but not
limited to:

- setup/admin polish;
- assignment/admin operations polish;
- mobile field workflow polish;
- readiness/freshness/attention expansion beyond PC2;
- reporting/import/export/aggregate oversight;
- conflict queue or resolution workflow beyond PC2's one-item review;
- pattern registry/projection follow-through;
- S06/entity lifecycle;
- tenant/control-plane;
- delivery-readiness/security/reliability proof work that should remain a
  product-planning route rather than runtime implementation.

Select one bounded PC3 candidate, not a bundle.

## Guardrails

- Real users/data require NW-093 first.
- PC2 live browser proof requires NW-126 to be unblocked and accepted first.
- Reporting/import/export/aggregate work requires NW-044 or a bounded
  reporting spec route before delivery.
- Conflict queue/list/multi-item review routes through GAP-CONFLICT-01 before
  implementation.
- Conflict automation, batch workflow, resolver reassignment, or
  auto-resolution requires NW-045.
- Resolver eligibility broadening requires the GAP-CONFLICT-03 successor route.
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
conflict automation/batch acceptance, conflict queue/list implementation,
pattern projection changes, entity lifecycle acceptance, live PC2 browser
proof, lab mutation, or broad documentation cleanup.

## Validation

Run docs-only validation unless the selected output explicitly changes a
non-doc surface:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-127" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md

# If PC3 is selected:
test -f docs/specifications/product/product-candidate-3-pm-handoff.md

# If PC3 is not selected:
test -f docs/agent-working-surface/artifacts/NW-127-pc3-product-candidate-boundary-and-pm-handoff.md
```

Run the file check that matches the selected outcome and add grep/index checks
required by the local convention.

Runtime tests should be skipped with rationale for docs-only planning work.

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
- accepting reporting/import/export behavior;
- accepting conflict automation/batch behavior;
- reopening architecture decisions;
- using SPEC-* as active control.
