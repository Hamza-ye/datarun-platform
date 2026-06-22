# NW-133 - Select PC4 Product Candidate Boundary And PM Handoff

## Goal

Select exactly one Product Candidate 4 boundary and produce the PM handoff /
routing output for that candidate, or explicitly park PC4 selection if the
evidence does not support a bounded candidate.

This is product-planning and product-handoff work only. It must not implement
PC4, change runtime behavior, accept product behavior beyond the selected
planning boundary, approve real production, or open architecture,
tenant/control-plane, lab, security, reliability, or validation-hardening work
by drift.

## Why Now

NW-132 reviewed post-PC3 progress health and selected this one successor.
PC1, PC2, and PC3 are all parked as synthetic-demo-ready and not
real-production-ready. No active evidence shows that engineering hardening,
Secure SDLC, reliability/ops readiness, NW-093, NW-126, NW-073, tenant/control
plane, architecture risk reduction, or broad reporting/import/export should
preempt the next bounded product-candidate comparison.

## Inputs

Read these surfaces first:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-132-post-pc3-progress-health-and-next-route-selection.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/product/product-candidate-3-pm-handoff.md`
- `docs/specifications/product/product-candidate-handoff-template.md`
- `docs/agent-working-surface/artifacts/NW-120-delivery-readiness-and-pc2-intake.md`
- `docs/agent-working-surface/artifacts/NW-131-pc3-synthetic-walkthrough-proof.md`
- `docs/scenarios/README.md`
- `docs/reviews/scenario-baseline-pressure-map.md`
- `docs/reviews/viability-closure-review.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`

Use specific scenario files, product artifacts, code files, or tests only if
the candidate comparison needs them. Do not reopen broad architecture history
unless a gap-routing trigger fires.

## Expected Output

Primary output if PC4 is selected:

```text
docs/specifications/product/product-candidate-4-pm-handoff.md
```

Use `docs/specifications/product/product-candidate-handoff-template.md` as the
structure.

This PM handoff is a durable planning surface only. It does not accept product
behavior, runtime implementation, production approval, architecture authority,
validation policy, contracts, schemas, BAR, CDL, or gap-register standing.

Use this artifact path only if NW-133 concludes that PC4 is not ready to select
and instead parks/blocks selection or routes a prerequisite:

```text
docs/agent-working-surface/artifacts/NW-133-pc4-product-candidate-boundary-and-pm-handoff.md
```

In that case, do not claim PC4 has been selected.

The output must include:

- selected PC4 boundary, exactly one, or an explicit no-selection/park call;
- why that boundary wins over other candidate fronts, or why none wins;
- scenario evidence used;
- explicit non-goals;
- validation gates and known validation debt;
- Secure SDLC / security gates;
- reliability / operations gates;
- architecture/platform prerequisites;
- PC1, PC2, and PC3 parked synthetic-only standing;
- NW-093 production approval standing;
- NW-126 blocked PC2 live-lab proof debt standing;
- NW-044 broad reporting/import/export standing;
- NW-073 pattern registry/projection standing;
- NW-094 through NW-098 tenant/control-plane standing;
- not-selected-now table with triggers;
- whether a single successor is ready, or why not.

## Candidate Fronts To Compare

Start from remaining product fronts after PC1, PC2, and PC3, including but not
limited to:

- setup/admin polish;
- assignment/admin operations polish;
- mobile field workflow polish;
- live/manual proof or demo-quality follow-up;
- reporting/import/export/aggregate expansion beyond PC3;
- conflict queue/list/multi-item ergonomics beyond PC2;
- conflict automation, batch workflow, resolver reassignment, or
  auto-resolution;
- pattern registry/projection follow-through;
- S06/entity lifecycle;
- S22/S27 coordinated campaign, logistics, transfer, handoff, or
  domain-agnostic composite product fronts;
- S24/S25 retention, offboarding, worker transfer, or handoff-policy pressure;
- S02/S09 recurring or campaign standing pressure;
- tenant/control-plane;
- engineering quality, security, reliability, or operations routes that should
  remain prerequisites or triggers rather than product candidates.

Select one bounded PC4 candidate, not a bundle. Do not select from anxiety.

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
- Retention/security/offboarding promises require NW-054 or a bounded security
  route.
- New subject/query/custom scope requires NW-053.
- Managed control-plane or tenant-aware runtime work requires NW-094 through
  NW-098 as applicable.
- A new operational read surface, reporting-like view, drill-back, multi-item
  attention view, export, aggregate, or broad audit/history surface must route
  the read-model/query boundary before implementation.

## Forbidden Work

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register mutation, production approval, real users/data,
tenant-aware runtime implementation, reporting/import/export acceptance,
conflict automation/batch acceptance, conflict queue/list implementation,
pattern projection changes, entity lifecycle acceptance, retention/security
promises, live PC2 browser proof, lab mutation, or broad documentation cleanup.

## Validation

Run docs-only validation unless the selected output explicitly changes a
non-doc surface:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-133" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md

# If PC4 is selected:
test -f docs/specifications/product/product-candidate-4-pm-handoff.md

# If PC4 is not selected:
test -f docs/agent-working-surface/artifacts/NW-133-pc4-product-candidate-boundary-and-pm-handoff.md
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
- changing pattern contracts/projection behavior before NW-073;
- accepting retention/security/offboarding promises;
- accepting S06/entity lifecycle;
- reopening architecture decisions;
- using SPEC-* as active control.

## Commit Boundary

Use a docs/product-planning commit if NW-133 lands. Do not combine NW-133 with
runtime implementation, product behavior acceptance, platform-spec changes
outside a selected prerequisite, validation-policy changes, CI changes,
BAR/CDL/gap-register updates, lab work, real-production approval, or unrelated
cleanup.
