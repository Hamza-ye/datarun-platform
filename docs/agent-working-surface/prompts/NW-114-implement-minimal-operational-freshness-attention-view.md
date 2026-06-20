# NW-114 - Implement minimal operational freshness/attention view

## Goal

Implement the smallest Product Candidate 1 supervisor/reviewer observation
selected by NW-113: latest synced work, freshness wording, and one narrow
read-only attention cue if present.

This NW is runtime implementation, but only for the minimal scoped operational
view. It must not broaden into reporting, export, analytics, broad audit,
conflict operations, retention/security, entity lifecycle, tenant/control-plane
work, contracts, schemas, sync changes, or product-spec changes.

## User value / why now

NW-113 found that the current accepted standing is not enough to demonstrate
the PC1 synthetic demo final beat, and that deferring the beat would leave the
only NW-112 friction unresolved.

NW-114 should make the final PC1 demo beat observable without claiming a
reporting product or conflict-review workflow.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/artifacts/NW-113-minimal-scoped-operational-freshness-attention-view.md`
- `docs/agent-working-surface/artifacts/NW-112-pc1-demo-review-and-successor-selection.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/specifications/product/product-candidate-1.md`
- `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
- `docs/specifications/platform/assignment-scope-and-administration.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/commit-workflow.md`
- `server/AGENTS.md`

Read only the server code, templates, and tests needed for the selected view.
Do not read CDL, old IDRs, broad architecture, mobile code, contracts, schemas,
or migrations unless a stop condition fires.

## Allowed changes

Use the narrowest implementation surface that satisfies the NW-113 boundary.
Expected allowed surfaces are:

- server-rendered web-admin route/controller/template/service code for a
  read-only scoped operational view;
- focused server tests for authentication, authorization, scoped visibility,
  freshness wording, and the optional read-only attention cue;
- backlog/status acceptance updates after validation.

## Forbidden changes

Do not modify:

- mobile code;
- contracts, schemas, migrations, envelope fields/types, or sync protocol;
- product specs, platform specs, PM handoff, validation matrix, CI, BAR, CDL, or
  gap register;
- reporting/export/import/warehouse/analytics surfaces;
- conflict resolution workflows, batch review, resolver reassignment,
  auto-resolution, or flag reporting;
- retention/security/offboarding policy;
- entity lifecycle, known-set, duplicate, merge, split, or registry workflows;
- tenant/control-plane behavior or tenant/workspace selectors.

Do not add new authority sources. Use accepted web-admin session, command-gate,
and scoped-read standing only. Stop if the view cannot be implemented without
changing authority, sync scope, contracts, schemas, or event meaning.

## Acceptance criteria

NW-114 is accepted only when the implementation:

- lets a supervisor/reviewer observe the latest synced work for the accepted
  PC1 scope;
- shows product-safe freshness wording for that narrow observation;
- shows at most one read-only `Needs review` cue if an unresolved attention
  item is present;
- preserves authorization and scoped visibility;
- stays out of reporting/export, broad audit, conflict operations,
  retention/security, entity lifecycle, tenant/control-plane, contract, schema,
  envelope, and sync changes;
- includes focused tests for the implemented behavior and denial/no-leakage
  cases;
- updates backlog/status consistently with exact validation evidence.

## Validation

Use the validation matrix for server behavior and web-admin UI/template work.
Run the narrowest focused test first, then the required server gate for the
touched surface.

Expected commands:

```bash
cd /home/hamza/datarun-platform
git status --short
git diff --check

cd /home/hamza/datarun-platform/server
./mvnw -Dtest=<FocusedWebAdminOperationalViewTest> test
./mvnw test
```

Adjust the focused test class name to the implementation, and report exact
test counts, duration when available, skipped-gate rationale if any, and CI
links when available.

## Stop conditions

Stop and report if implementation requires:

- real domain or pilot selection;
- real users or real organizational data;
- product-scope change;
- reporting dashboards, exports, imports, warehouses, analytics, or broad read
  APIs;
- retention/security promises;
- entity lifecycle;
- conflict automation, batch review, resolver reassignment, auto-resolution, or
  flag reporting;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, or sync changes;
- architecture/gap routing.

## Commit boundary

Use implementation and acceptance commits according to `docs/commit-workflow.md`.
Do not combine NW-114 with product-spec changes, platform-spec changes,
validation-policy changes, CI changes, BAR/CDL/gap-register updates, or
unrelated cleanup.
