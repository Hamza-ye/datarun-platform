# NW-124 - Run PC2 Synthetic Walkthrough/Proof Packet

## Goal

Capture synthetic, non-sensitive proof evidence for Product Candidate 2 after
NW-123 marked PC2 `synthetic-demo-ready, not proof-complete, not
real-production-ready`.

This is product-validation / owner-review evidence work. It must not implement
runtime behavior, change product scope, or approve real production.

## User Value / Why Now

NW-122 accepted the bounded one-item Single Work-Linked Attention Review loop.
NW-123 found no required polish before proof and selected a synthetic proof
packet as the next route. NW-124 should turn that readiness into one reviewable
evidence packet for owner decision-making.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/artifacts/NW-123-pc2-post-nw122-demo-standing-and-successor-selection.md`
- PR #38 summary and accepted NW-122 validation evidence from status/backlog
- `docs/agent-working-surface/validation-matrix.md`

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if proof evidence creates pressure for reporting, queue/multi-item review,
automation, resolver reassignment, resolver eligibility broadening, real
production, retention/security, tenant/control-plane, contracts, sync/access,
or another gap-trigger surface.

Do not open broad architecture history unless a stop condition fires.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-124-pc2-synthetic-walkthrough-proof.md
```

The artifact should:

- restate that PC2 is synthetic-demo-ready, not proof-complete, not
  real-production-ready at the start of the packet;
- name the synthetic/non-sensitive fixture or example used;
- use S21 supervisor review and/or S27 logistics discrepancy only as examples
  within the one-item PC2 boundary;
- walk the PC2 journey from visible `Needs review` cue to one attention review
  page and one manual decision;
- classify each proof beat as `PASS`, `FRICTION`, `NOT_RUN`, or
  `OUT-OF-SCOPE`;
- confirm exact designated-reviewer authority and no UI/body actor authority;
- confirm source work remains append-only and resolution is separate evidence;
- record any friction as candidate follow-up pressure only;
- confirm no real users or real organizational data were used;
- confirm no real-production approval was granted;
- recommend exactly one next route: park, one small bounded polish, real-use
  preparation through NW-093, or another explicitly bounded owner route.

## Allowed Changes

- Add the NW-124 synthetic proof artifact.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Update `docs/agent-working-surface/artifacts/README.md`.
- Add one successor prompt only if NW-124 selects a concrete next route.

## Forbidden Changes

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register changes, product spec changes, platform spec changes,
real-production approval, reporting/export, conflict queue/list workflow,
batch review, resolver reassignment, automation, resolver eligibility
broadening, retention/security promises, entity lifecycle, tenant/control-plane
work, mobile code, or server/web-admin implementation.

Do not use real users or real organizational data. If real-use preparation
becomes selected, stop and route through NW-093 before continuing.

## Acceptance Criteria

NW-124 is accepted only when:

- one PC2 synthetic proof evidence packet exists;
- each proof beat has clear standing;
- proof remains one work-linked attention item and does not become a queue,
  report, batch workflow, or broad conflict console;
- real-production standing remains blocked unless NW-093 is selected later;
- status/backlog reflect the resulting route and no active implementation gate
  is opened by accident;
- validation evidence is docs-only and exact unless the packet explicitly
  records a manual runtime inspection.

## Validation

Run docs-only validation unless the packet inspects runtime manually:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-124" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-124-pc2-synthetic-walkthrough-proof.md
grep -n "synthetic-demo-ready" docs/agent-working-surface/artifacts/NW-124-pc2-synthetic-walkthrough-proof.md
```

Runtime tests are skipped because this is docs/product-validation only unless
a future selected packet explicitly adds manual environment evidence.

## Stop Conditions

Stop and report if the work requires:

- real users or real organizational data;
- real-production approval inside NW-124;
- reporting dashboards, exports, imports, warehouses, analytics, broad read
  APIs, completeness semantics, or drilldown;
- queue/list/multi-item review, broad conflict console, filters, batch review,
  resolver reassignment, automation, auto-resolution, or flag reporting;
- resolver eligibility broadening beyond exact stored `designated_resolver`
  equality for the opened item;
- retention/security/offboarding promises;
- entity lifecycle;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, sync, validation-policy, CI,
  BAR, CDL, or gap-register changes;
- runtime implementation.

## Commit Boundary

Use a docs/product-validation commit if NW-124 lands. Do not combine NW-124
with runtime implementation, product-spec changes, platform-spec changes,
validation-policy changes, CI changes, BAR/CDL/gap-register updates, or
unrelated cleanup.
