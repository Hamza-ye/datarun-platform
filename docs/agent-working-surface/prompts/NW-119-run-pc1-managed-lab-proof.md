# NW-119 - Run PC1 managed lab proof

## Goal

Capture managed lab proof evidence for Product Candidate 1 using the NW-118
managed lab boundary and synthetic/non-sensitive data only.

This is product-validation / owner-review evidence work. It must not implement
runtime behavior, change product scope, use real users or real organizational
data, or approve real production.

## User value / why now

NW-117 passed the internal synthetic demo proof across all 16 NW-111 sequences,
and NW-118 selected a bounded synthetic managed lab proof route. NW-119 should
turn that boundary into one reviewable lab evidence packet for owner
decision-making.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-1.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/agent-working-surface/artifacts/NW-118-pc1-managed-lab-proof-boundary.md`
- `docs/agent-working-surface/artifacts/NW-117-pc1-internal-synthetic-demo-proof.md`
- `docs/agent-working-surface/artifacts/NW-111-pc1-synthetic-demo-walkthrough.md`
- `docs/agent-working-surface/validation-matrix.md`

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if the lab proof evidence creates pressure for real production,
reporting, retention/security, tenant/control-plane, contracts, sync/access,
or another gap-trigger surface.

Do not open broad architecture history unless a stop condition fires.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-119-pc1-managed-lab-proof.md
```

The artifact should:

- restate that PC1 is synthetic-demo-ready, not real-production-ready;
- name the lab organization label and environment owner from NW-118;
- name the synthetic fixture/data source used;
- review all 16 NW-111 sequences;
- classify each sequence as `PASS`, `FRICTION`, `NOT_RUN`, or
  `OUT-OF-SCOPE`;
- record evidence notes for each sequence;
- record any friction as candidate follow-up pressure only;
- confirm no real users or real organizational data were used;
- confirm no real-production approval was granted;
- state whether the owner should repeat the lab proof, select NW-093 for
  real-use preparation, select a bounded polish/follow-up route, or park.

## Allowed changes

- Add the NW-119 managed lab proof artifact.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Update `docs/agent-working-surface/artifacts/README.md`.
- Add one successor prompt only if NW-119 selects a concrete next route.

## Forbidden changes

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register changes, product spec changes, platform spec changes,
real-production approval, reporting/export, conflict workflow,
retention/security promises, entity lifecycle, tenant/control-plane work,
mobile code, or server/web-admin implementation.

Do not use real users or real organizational data. If real-use preparation
becomes selected, stop and route through NW-093 before continuing.

## Acceptance criteria

NW-119 is accepted only when:

- one managed lab proof evidence packet exists;
- all 16 sequences are reviewed with clear per-sequence standing or not-run
  rationale;
- real-production standing remains blocked unless NW-093 is selected later;
- status/backlog reflect the resulting route and no active implementation gate
  is opened by accident;
- validation evidence is docs-only and exact unless the selected lab packet
  explicitly supplies manual environment evidence.

## Validation

Run docs-only validation:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-119" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-119-pc1-managed-lab-proof.md
grep -n "synthetic-demo-ready, not real-production-ready" docs/agent-working-surface/artifacts/NW-119-pc1-managed-lab-proof.md
```

Runtime tests are skipped because this is docs/product-validation only unless
the selected packet explicitly adds manual environment evidence.

## Stop conditions

Stop and report if the work requires:

- real users or real organizational data;
- real-production approval inside NW-119;
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

Use a docs/product-validation commit if NW-119 lands. Do not combine NW-119
with runtime implementation, product-spec changes, platform-spec changes,
validation-policy changes, CI changes, BAR/CDL/gap-register updates, or
unrelated cleanup.
