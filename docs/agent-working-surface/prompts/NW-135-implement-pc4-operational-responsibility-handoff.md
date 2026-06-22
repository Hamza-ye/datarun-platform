# NW-135 - Implement PC4 Operational Responsibility Handoff

## Goal

Implement the Product Candidate 4 boundary selected by accepted NW-133 and
enabled by accepted NW-134:

```text
Product Candidate 4 - Operational Responsibility Handoff
```

This NW is runtime implementation work, but only for one bounded handoff
context: a successor actor can see current assigned work, bounded prior context,
and caveats for late/offline, stale, unresolved, incomplete, or unknown
standing. It must not become reporting, broad audit/history, worker
offboarding, retention/security policy, conflict queue/list/batch workflow,
pattern product work, entity lifecycle, tenant/control-plane behavior,
production approval, real-users/data proof, or PC2 live-lab proof.

## User Value / Why Now

PC1 proves the basic operational loop, PC2 proves one work-linked attention
review, and PC3 proves one scoped operational snapshot. NW-133 selected PC4
because the next coherent product question is continuity after responsibility
changes. NW-134 accepted the prerequisite platform boundary and found one
implementation successor ready.

Use S25 worker transfer as the primary synthetic example. Use S27 logistics
handoff as the domain-neutral cross-check. Use S22 campaign reassignment only
as a secondary continuity example.

## Inputs

Read these first:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-4-pm-handoff.md`
- `docs/specifications/platform/operational-responsibility-handoff-boundary.md`
- `docs/specifications/platform/assignment-scope-and-administration.md`
- `docs/specifications/platform/shared-device-session-and-local-state.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/specifications/platform/scoped-operational-report-snapshot-boundary.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/commit-workflow.md`
- `server/AGENTS.md` if server/web-admin code is touched
- `mobile/AGENTS.md` if mobile code is touched

Read only implementation surfaces needed for the bounded handoff context. Start
from existing web-admin operational, assignment, sync, subject-history,
conflict/flag, and mobile work/local-state patterns before adding new code.

Do not read contracts, schemas, migrations, old IDRs, broad architecture
history, tenant/control-plane docs, or lab/operations surfaces unless a stop
condition fires.

## Required Implementation Shape

Implement the smallest product surface that satisfies the accepted NW-134
handoff boundary:

- show current assigned work for the successor actor;
- show bounded prior context for currently authorized subject/activity slices;
- caveat late synced work, stale authority, unresolved attention, incomplete
  context, and unknown freshness in product-safe language;
- apply accepted assignment scope before selecting context, caveats, latest
  times, trace targets, or empty states;
- keep subject-history use subject/activity-bound and current-scope
  authorized;
- preserve actor-local partitions and sealed pending work boundaries;
- preserve exact stored `designated_resolver` behavior for attention items.

Use server-rendered web-admin and/or mobile surfaces only where needed for this
one handoff context. Do not create broad APIs, report surfaces, queues, or new
sync protocol behavior.

## Allowed Changes

Use the narrowest implementation surface that satisfies the prompt:

- bounded server/web-admin or mobile UI/service changes for one handoff
  context;
- a typed, narrow read/query boundary if implementation needs a new handoff
  read model;
- focused server and/or mobile tests for touched behavior;
- status/backlog acceptance updates after implementation validation.

## Forbidden Changes

Do not modify or add:

- contracts, schemas, migrations, envelope fields/types, assignment payloads,
  sync protocol behavior, validation policy, or CI;
- BAR, CDL, gap-register standing, architecture decisions, or broad
  documentation cleanup;
- retention/security/offboarding promises, local expiry, device
  decommissioning, sealed-partition recovery, local encryption, token/session
  retention, no-local-retention, erasure, redaction, sensitivity handling, or
  administrator recovery/export of another actor's local work;
- new subject/query/custom scope, cross-activity cohort materialization,
  query-as-config authority, hidden sync scope, auditor scope, emergency scope,
  or grace scope;
- broad reporting, import/export, warehouse, analytics, report APIs, report
  catalog, dashboards, arbitrary filters, cadence, completion, completeness,
  percentages, drilldown, or interoperability reporting;
- conflict queue/list/multi-item ergonomics, batch review, pending-match
  queues, conflict automation, auto-resolution, resolver reassignment, broad
  conflict console, or resolver eligibility broadening;
- pattern traversal/reporting, inventory expansion, pattern APIs, workflow
  projection changes, trigger execution, or durable workflow-state tables;
- S06/entity lifecycle, maintained known-set registry, discovered-unit
  lifecycle, deactivation, candidates, duplicates, merge/split UX, or registry
  stewardship;
- tenant-aware runtime, managed control plane, workspace-scoped config, tenant
  sync context, pooled storage, tenant isolation harness, or UI tenant choice;
- real users/data, production approval, provider/region/jurisdiction/support,
  compliance/security approval, continuity approval, go/no-go standing, PC2
  live proof, or lab mutation.

## Acceptance Criteria

NW-135 is accepted only when the implementation:

- provides one bounded PC4 handoff context;
- uses accepted server-resolved actor identity, assignment scope, web-admin
  session/scoped-read standing where applicable, and mobile active actor
  session where applicable;
- does not use IdP groups/claims, UI-selected actors, generic admin/root
  labels, assignment role labels alone, tenant/workspace selection, or another
  actor's local partition as authority;
- shows current assigned work and bounded prior context without exposing
  out-of-scope records or hidden-record hints;
- caveats late synced work, stale authority, unresolved attention, incomplete
  context, and unknown freshness without all-clear/completion/reporting claims;
- preserves subject-history/live-sync separation and normal watermark behavior;
- preserves actor-local partitioning and sealed pending work boundaries;
- handles `resolver_unassigned` as blocked/not currently resolvable and does
  not invent fallback resolver authority;
- includes focused tests for scope/no-leakage, late offline/stale caveats,
  unresolved attention treatment, and touched server/mobile surfaces;
- runs required full validation gates from the validation matrix for touched
  runtime surfaces;
- updates backlog/status with exact validation evidence after runtime
  validation passes.

## Validation

Use the validation matrix for the touched runtime surfaces. Run the narrowest
focused test first, then the required full gate.

Expected starting commands:

```bash
cd /home/hamza/datarun-platform
git status --short
git diff --check
```

If server/web-admin behavior changes:

```bash
cd /home/hamza/datarun-platform
docker compose -f docker-compose.test.yml up -d test-db

cd /home/hamza/datarun-platform/server
./mvnw -Dtest=<FocusedHandoffOrOperationalTest> test
./mvnw test
```

If mobile behavior changes:

```bash
cd /home/hamza/datarun-platform/mobile
flutter test test/<focused_handoff_or_workflow_test>.dart
flutter test
```

Run Android compile only if native/platform/auth/plugin surfaces are touched.
Report exact commands, cwd, result, test counts and duration when available,
skipped-gate rationale, and CI links when available.

## Stop Conditions

Stop and report before implementation or before continuing if the work
requires:

- real users/data or production approval;
- PC2 live browser proof or lab mutation;
- retention/security/offboarding promises or recovery/export of another actor's
  sealed local work;
- new scope mechanisms, hidden sync/access scope, broad audit/history, or
  normal sync watermark rewrites;
- broad reporting/import/export/warehouse/API/catalog/filter/cadence/completion
  behavior;
- conflict queue/list/batch/automation, resolver reassignment, resolver
  eligibility broadening, or fallback resolver authority;
- pattern traversal/reporting, inventory expansion, projection changes, pattern
  API/product behavior, trigger execution, or durable workflow-state authority;
- entity lifecycle;
- tenant/control-plane or tenant-aware runtime behavior;
- contracts, schemas, migrations, envelope fields/types, sync protocol,
  authority-source changes, validation-policy/CI changes, BAR, CDL, or
  gap-register mutation.

## Commit Boundary

Use implementation and acceptance commits according to
`docs/commit-workflow.md`. Do not combine NW-135 with product-spec/platform-spec
changes, validation-policy changes, CI changes, BAR/CDL/gap-register updates,
lab work, production approval, or unrelated cleanup.
