# NW-116 - Decide PC1 proof target after synthetic demo readiness

## Goal

Decide the next Product Candidate 1 proof target now that NW-115 marks PC1 as:

```text
synthetic-demo-ready, not real-production-ready
```

This is product-planning and owner-decision work only. It must choose exactly
one next proof target and preserve the real-production boundary.

## User value / why now

NW-115 found all 16 NW-111 synthetic demo sequences clear after accepted
NW-114. The next useful decision is not more polish by default; it is choosing
what proof target the owner wants the cleared synthetic demo to support.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-1.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/agent-working-surface/artifacts/NW-115-pc1-post-nw114-demo-standing-and-successor-selection.md`
- `docs/agent-working-surface/artifacts/NW-111-pc1-synthetic-demo-walkthrough.md`
- `docs/agent-working-surface/validation-matrix.md`

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if the selected route would touch reporting, real-production approval,
retention/security, tenant/control-plane, contracts, sync/access, or another
gap-trigger surface.

Do not open broad architecture history unless a stop condition fires.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-116-pc1-proof-target-decision.md
```

The artifact should:

- restate that PC1 is synthetic-demo-ready, not real-production-ready;
- choose exactly one proof target:
  - internal synthetic demo;
  - managed lab proof;
  - real-use preparation, which must route through NW-093 before real users or
    real organizational data;
- state why the other proof targets were not selected now;
- name the evidence expected for the chosen target;
- state whether a successor NW/prompt is needed;
- preserve the reporting, retention/security, tenant/control-plane, conflict
  workflow, and real-production boundaries;
- update status/backlog consistently with the selected proof route.

## Allowed changes

- Add the NW-116 proof-target decision artifact.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Add one successor prompt only if NW-116 selects a concrete next route.
- Update `docs/agent-working-surface/artifacts/README.md` for the new artifact.

## Forbidden changes

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register changes, product spec changes, platform spec changes,
real-production approval, reporting/export, conflict workflow,
retention/security promises, entity lifecycle, tenant/control-plane work,
mobile code, or server/web-admin implementation.

Do not claim real-production readiness from synthetic-demo readiness. If the
owner selects real-use preparation, route through NW-093 before any real users,
real organizational data, provider/region/jurisdiction, support, compliance,
security, or go/no-go commitment.

## Acceptance criteria

NW-116 is accepted only when:

- one proof target is selected or explicitly parked;
- real-production standing remains blocked unless NW-093 is selected;
- the artifact names the evidence required for the selected target;
- status/backlog reflect the selected route and no active implementation gate
  is opened by accident;
- validation evidence is docs-only and exact.

## Validation

Run docs-only validation:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-116" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-116-pc1-proof-target-decision.md
grep -n "synthetic-demo-ready, not real-production-ready" docs/agent-working-surface/artifacts/NW-116-pc1-proof-target-decision.md
```

Runtime tests are skipped because this is docs/product-planning only.

## Stop conditions

Stop and report if the decision requires:

- real users or real organizational data without selecting NW-093 first;
- real-production approval inside NW-116;
- reporting dashboards, exports, imports, warehouses, analytics, broad read
  APIs, completeness semantics, or drilldown;
- retention/security/offboarding promises;
- entity lifecycle;
- conflict automation, batch review, resolver reassignment, auto-resolution,
  flag reporting, or conflict workflow;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, sync, validation-policy, CI,
  BAR, CDL, or gap-register changes.

## Commit boundary

Use a docs/product-planning commit if NW-116 lands. Do not combine NW-116 with
runtime implementation, product-spec changes, platform-spec changes,
validation-policy changes, CI changes, BAR/CDL/gap-register updates, or
unrelated cleanup.
