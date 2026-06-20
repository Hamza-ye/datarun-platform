# NW-117 - Run PC1 internal synthetic demo proof

## Goal

Capture internal synthetic demo evidence for Product Candidate 1 after NW-116
selected `internal synthetic demo` as the next proof target.

This is product-validation / owner-review evidence work. It must not implement
runtime behavior, change product scope, or approve real production.

## User value / why now

NW-115 marked all 16 NW-111 sequences clear, and NW-116 selected an internal
synthetic demo as the next proof target. NW-117 should turn that readiness into
one reviewable evidence packet for owner decision-making.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-1.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/agent-working-surface/artifacts/NW-116-pc1-proof-target-decision.md`
- `docs/agent-working-surface/artifacts/NW-115-pc1-post-nw114-demo-standing-and-successor-selection.md`
- `docs/agent-working-surface/artifacts/NW-111-pc1-synthetic-demo-walkthrough.md`
- `docs/agent-working-surface/validation-matrix.md`

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if the demo evidence creates pressure for reporting, real production,
retention/security, tenant/control-plane, contracts, sync/access, or another
gap-trigger surface.

Do not open broad architecture history unless a stop condition fires.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-117-pc1-internal-synthetic-demo-proof.md
```

The artifact should:

- restate that PC1 is synthetic-demo-ready, not real-production-ready;
- name the synthetic fixture used;
- review all 16 NW-111 sequences;
- classify each sequence as `PASS`, `FRICTION`, `NOT_RUN`, or
  `OUT-OF-SCOPE`;
- record evidence notes for each sequence;
- record any friction as candidate follow-up pressure only;
- confirm no real users or real organizational data were used;
- confirm no real-production approval was granted;
- state whether the owner should proceed to managed lab proof, select NW-093
  for real-use preparation, repeat the internal demo, or park.

## Allowed changes

- Add the NW-117 internal synthetic demo proof artifact.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Update `docs/agent-working-surface/artifacts/README.md`.
- Add one successor prompt only if NW-117 selects a concrete next route.

## Forbidden changes

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register changes, product spec changes, platform spec changes,
real-production approval, reporting/export, conflict workflow,
retention/security promises, entity lifecycle, tenant/control-plane work,
mobile code, or server/web-admin implementation.

Do not use real users or real organizational data. If real-use preparation
becomes selected, stop and route through NW-093 before continuing.

## Acceptance criteria

NW-117 is accepted only when:

- one internal synthetic demo evidence packet exists;
- all 16 sequences are reviewed with clear per-sequence standing;
- real-production standing remains blocked unless NW-093 is selected later;
- status/backlog reflect the resulting route and no active implementation gate
  is opened by accident;
- validation evidence is docs-only and exact.

## Validation

Run docs-only validation:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-117" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-117-pc1-internal-synthetic-demo-proof.md
grep -n "synthetic-demo-ready, not real-production-ready" docs/agent-working-surface/artifacts/NW-117-pc1-internal-synthetic-demo-proof.md
```

Runtime tests are skipped because this is docs/product-validation only unless
a future selected packet explicitly adds manual environment evidence.

## Stop conditions

Stop and report if the work requires:

- real users or real organizational data;
- real-production approval inside NW-117;
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

Use a docs/product-validation commit if NW-117 lands. Do not combine NW-117
with runtime implementation, product-spec changes, platform-spec changes,
validation-policy changes, CI changes, BAR/CDL/gap-register updates, or
unrelated cleanup.
