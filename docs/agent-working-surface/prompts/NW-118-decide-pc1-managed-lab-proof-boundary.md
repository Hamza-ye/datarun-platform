# NW-118 - Decide PC1 managed lab proof boundary

## Goal

Decide the bounded Product Candidate 1 managed lab proof boundary after NW-117
passed the internal synthetic demo proof.

This is product-planning / owner-decision work only. It must define whether a
managed lab proof can proceed, what evidence it would require, and what remains
blocked. It must not run a lab, use real users or real organizational data,
approve real production, or implement runtime behavior.

## User value / why now

NW-117 reviewed all 16 NW-111 sequences as `PASS` using synthetic/non-sensitive
data. The next useful owner decision is to decide the managed lab proof
boundary before any lab environment, operator, support path, data boundary, or
acceptance criteria are assumed.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-1.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/agent-working-surface/artifacts/NW-117-pc1-internal-synthetic-demo-proof.md`
- `docs/agent-working-surface/artifacts/NW-116-pc1-proof-target-decision.md`
- `docs/agent-working-surface/artifacts/NW-115-pc1-post-nw114-demo-standing-and-successor-selection.md`
- `docs/agent-working-surface/artifacts/NW-111-pc1-synthetic-demo-walkthrough.md`
- `docs/agent-working-surface/validation-matrix.md`

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if the decision creates pressure for real production, reporting,
retention/security, tenant/control-plane, contracts, sync/access, or another
gap-trigger surface.

Do not open broad architecture history unless a stop condition fires.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-118-pc1-managed-lab-proof-boundary.md
```

The artifact should:

- restate that PC1 is synthetic-demo-ready, not real-production-ready;
- state whether the owner should proceed to a managed lab proof, select NW-093
  for real-use preparation, repeat the internal synthetic demo, or park;
- if managed lab proof is selected, name the required lab boundary before any
  lab run: lab organization label, environment owner, operator/contact path,
  data boundary, fixture/data source, acceptance criteria, evidence owner, and
  stop conditions;
- reject real users or real organizational data unless NW-093 is selected
  first;
- preserve the reporting, retention/security, tenant/control-plane, conflict
  workflow, and real-production boundaries;
- state whether a successor NW/prompt is needed;
- update status/backlog consistently with the selected route.

## Allowed changes

- Add the NW-118 managed lab proof boundary artifact.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Update `docs/agent-working-surface/artifacts/README.md`.
- Add one successor prompt only if NW-118 selects a concrete next route.

## Forbidden changes

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register changes, product spec changes, platform spec changes,
real-production approval, reporting/export, conflict workflow,
retention/security promises, entity lifecycle, tenant/control-plane work,
mobile code, or server/web-admin implementation.

Do not use real users or real organizational data. If a managed lab proof would
use real users, real organizational data, provider/region/jurisdiction
selection, support commitment, compliance/security review, continuity planning,
or go/no-go production ownership, stop and route through NW-093 before
continuing.

## Acceptance criteria

NW-118 is accepted only when:

- one managed lab proof boundary artifact exists;
- the owner route is explicit: managed lab proof, NW-093 real-use preparation,
  repeat internal synthetic demo, or park;
- managed lab prerequisites are named or explicitly identified as missing;
- real-production standing remains blocked unless NW-093 is selected later;
- status/backlog reflect the resulting route and no active implementation gate
  is opened by accident;
- validation evidence is docs-only and exact.

## Validation

Run docs-only validation:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-118" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-118-pc1-managed-lab-proof-boundary.md
grep -n "synthetic-demo-ready, not real-production-ready" docs/agent-working-surface/artifacts/NW-118-pc1-managed-lab-proof-boundary.md
```

Runtime tests are skipped because this is docs/product-planning only unless a
future selected packet explicitly adds manual lab evidence.

## Stop conditions

Stop and report if the work requires:

- real users or real organizational data without selecting NW-093 first;
- real-production approval inside NW-118;
- reporting dashboards, exports, imports, warehouses, analytics, broad read
  APIs, completeness semantics, or drilldown;
- retention/security/offboarding promises;
- entity lifecycle;
- conflict automation, batch review, resolver reassignment, auto-resolution,
  flag reporting, or conflict workflow;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, sync, validation-policy, CI,
  BAR, CDL, or gap-register changes;
- runtime implementation.

## Commit boundary

Use a docs/product-planning commit if NW-118 lands. Do not combine NW-118 with
runtime implementation, product-spec changes, platform-spec changes,
validation-policy changes, CI changes, BAR/CDL/gap-register updates, or
unrelated cleanup.
