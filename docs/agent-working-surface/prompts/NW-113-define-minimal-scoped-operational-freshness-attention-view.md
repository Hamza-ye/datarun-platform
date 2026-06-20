# NW-113 — Define minimal scoped operational freshness/attention view

## Goal

Define the smallest supervisor/reviewer observation needed to complete the PC1
synthetic demo final beat: latest synced work, freshness wording, and one
narrow attention cue if present.

This NW is product-validation/product-planning only. It does not authorize
runtime implementation.

## User value / why now

NW-112 found that the setup, assignment, and mobile field-loop steps are clear
from accepted standing, while the final supervisor/reviewer beat remains the
only concrete PC1 proof friction.

Defining this boundary lets the next demo show or defer the final PC1 loop
without drifting into reporting/export, analytics, broad audit, or conflict
operations.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/artifacts/NW-112-pc1-demo-review-and-successor-selection.md`
- `docs/agent-working-surface/artifacts/NW-111-pc1-synthetic-demo-walkthrough.md`
- `docs/agent-working-surface/artifacts/NW-110-pc1-product-journey-smoke-definition.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/specifications/product/product-candidate-1.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/artifacts/README.md`
- `docs/commit-workflow.md`

Do not read CDL, old IDRs, broad architecture, phase history, server code,
mobile code, tests, contracts, schemas, or migrations unless a stop condition
fires.

## Allowed changes

Create one product-validation artifact:

- `docs/agent-working-surface/artifacts/NW-113-minimal-scoped-operational-freshness-attention-view.md`

Update:

- `docs/agent-working-surface/artifacts/README.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/status.md`

Optional only if the NW-113 decision selects a separate implementation route:

- one successor prompt for NW-114

## Forbidden changes

Do not modify:

- runtime code
- tests
- CI
- contracts
- schemas
- migrations
- mobile code
- server code
- product specs
- platform specs
- PM handoff
- validation matrix
- skills
- AGENTS files
- steward guide
- BAR
- CDL
- gap register

Do not implement the view in NW-113.

## Acceptance criteria

NW-113 is accepted only when the artifact:

- defines the smallest PC1 supervisor/reviewer observation for latest synced
  work, freshness wording, and one narrow attention cue if present;
- states whether current accepted standing is enough for the synthetic demo,
  the final beat should be deferred, or one separate implementation successor
  should be selected;
- keeps the route out of reporting/export, analytics, warehouse, broad audit,
  conflict operations, retention/security, entity lifecycle, real production,
  and tenant/control-plane work;
- records exact non-goals and stop conditions;
- updates artifact trace, backlog, and status consistently;
- leaves any implementation as a separately selected successor NW.

## Validation

Run:

```bash
cd /home/hamza/datarun-platform

git status --short
git diff --check

test -f docs/agent-working-surface/artifacts/NW-113-minimal-scoped-operational-freshness-attention-view.md

grep -n "Status: non-authoritative product-validation artifact" docs/agent-working-surface/artifacts/NW-113-minimal-scoped-operational-freshness-attention-view.md
grep -n "Selected Boundary" docs/agent-working-surface/artifacts/NW-113-minimal-scoped-operational-freshness-attention-view.md
grep -n "Stop Conditions" docs/agent-working-surface/artifacts/NW-113-minimal-scoped-operational-freshness-attention-view.md

grep -n "NW-113-minimal-scoped-operational-freshness-attention-view.md" docs/agent-working-surface/artifacts/README.md
grep -n "NW-113" docs/agent-working-surface/platform-next-work-backlog.md
grep -n "NW-113" docs/status.md
```

Runtime tests are skipped unless a future, separate implementation NW changes
runtime behavior.

## Stop conditions

Stop and report if the route requires:

- real domain or pilot selection;
- real users or real organizational data;
- product-scope change;
- runtime implementation inside NW-113;
- reporting dashboards, exports, imports, warehouses, analytics, or broad read
  APIs;
- retention/security promises;
- entity lifecycle;
- conflict automation, batch review, resolver reassignment, auto-resolution, or
  flag reporting;
- tenant/control-plane work;
- contract, schema, envelope, or sync changes;
- architecture/gap routing.

## Commit boundary

Use one docs-only commit if NW-113 lands as a product-validation artifact.

Do not combine NW-113 with runtime implementation, product-spec changes,
platform-spec changes, validation-policy changes, CI changes, BAR/CDL/gap
register updates, or a successor implementation commit.
