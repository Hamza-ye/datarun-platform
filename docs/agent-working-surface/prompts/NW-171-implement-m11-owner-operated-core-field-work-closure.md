# NW-171 - Implement M1.1 Owner-Operated Local/On-Prem Core Field-Work Closure

Status: ready prompt
Document type: execution_packet
Owner: implementation agent
Source: NW-170 route selection
Authority: selected implementation/validation packet for M1.1 only; creates no accepted product behavior, production cutover, real-use approval, platform authority, contract change, or operations promise until executed, reviewed, and accepted.

## User-Visible Outcome

An owner/operator can run one local/on-prem Datarun field-work path end to end:
set up one neutral activity, bind one local login principal to an actor, assign
scoped responsibility, have a field user log in on the mobile app, receive
scoped work, capture offline, sync, append a correction, and have the
owner/supervisor see scoped standing, freshness, and attention while also
knowing how to run and diagnose the selected local/on-prem posture.

Use stable HTTPS domain routing as pilot-enabling infrastructure:

- `app.nmcpye.org` for Datarun web/API;
- `auth.nmcpye.org` for Keycloak/OIDC.

Those names are available and must not be documented as blockers. If the
implementation deliberately chooses one host/path or the same DNS VM instead,
record the exact selected posture and reason in the evidence.

This is production-usable journey evidence for the selected M1.1 path. It is
not production cutover, legacy account import, submitted-record replay,
retention/security approval, Keycloak hardening, or broad real-user rollout.

## Exact Journey Boundary

Milestone: M1.1 - owner-operated local/on-prem core field-work closure.

Selected vertical path: one lifecycle-neutral "field check" activity. Treat
"field check" as neutral product language for an assigned operational activity,
not as a domain module or platform vocabulary.

Actors and responsibilities:

- Owner/operator/admin/support contact: Hamza in the initial owner-operated
  posture.
- Coordinator/setup owner: configures and publishes the single activity.
- Field user actor: authenticates through local Keycloak/OIDC and performs the
  assigned mobile work.
- Supervisor/owner actor: reads scoped standing, freshness, and attention.
  This may be the same human operating a distinct actor only when the evidence
  states the actor/principal boundary explicitly.

Journey steps:

1. Configure and publish one field-check activity and one record shape using
   existing setup/config/package delivery behavior.
2. Provision or reuse one explicit active `(issuer, subject) -> actor_id`
   principal binding for the selected local Keycloak issuer.
3. Create one contained assignment/responsibility for the field user and one
   scoped owner/supervisor read path using accepted assignment axes.
4. Log in on the mobile app with system-browser PKCE and activate the resolved
   server actor through `/api/auth/me`.
5. Pull configuration and scoped work, then capture at least one field-check
   record while offline or under controlled offline simulation.
6. Show local save/waiting/sync standing, sync the record, and append one
   correction without mutating the original event.
7. Show the owner/supervisor scoped standing with freshness and at least one
   existing attention or review cue from accepted flag/review behavior.
8. Record owner/operator run and diagnosis evidence for the selected local
   posture, including how auth, app/API, assignment, sync, and attention were
   checked.

Allowed evidence sources: synthetic, neutral extracted, or explicitly
owner-approved bounded real-use evidence. Legacy-derived examples must be
translated into neutral product language: actors, objects, workflow, records,
constraints, and expected behavior.

## Files To Read

Read first:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-170 and
  NW-171
- `docs/specifications/product/product-goal-and-representative-journeys.md`
- `docs/specifications/product/product-model-consolidation-and-slice-backlog.md`
- `docs/agent-working-surface/product-journey-and-slice-sequencing.md`
- `docs/agent-working-surface/validation-matrix.md`
- this prompt

Read accepted evidence and specs needed for touched surfaces:

- `docs/agent-working-surface/artifacts/NW-163-real-use-posture-and-principal-binding-preflight.md`
- `docs/agent-working-surface/artifacts/NW-164-local-keycloak-bound-login-evidence.md`
- `docs/agent-working-surface/artifacts/NW-165-live-mobile-oidc-login-smoke-evidence.md`
- `docs/specifications/platform/production-auth-principal-binding.md`
- `docs/specifications/platform/assignment-scope-and-administration.md`
- `docs/specifications/platform/configuration-package-and-shapes.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/specifications/platform/shared-device-session-and-local-state.md`
- `docs/operations/policies/first-reference-deployment-policy.md`
- local/on-prem deployment, server, mobile, and web-admin code/tests only as
  required by the selected changes

Read nested `AGENTS.md` files before touching `server/`, `mobile/`,
`contracts/`, or deployment/operations surfaces under their scope.

Do not read historical prompt files as route authority for this packet.

## Accepted Evidence To Preserve

Preserve and cite these as accepted evidence instead of re-proving them by
default:

- NW-163 owner-operated local/on-prem posture and principal-binding preflight.
- NW-164 local Keycloak/OIDC and explicit principal binding proof.
- NW-165 live Flutter Android OIDC login, actor activation, config access, and
  sync access proof.
- BAR-010, NW-032, NW-034, NW-068, and NW-088 setup/config/package delivery.
- BAR-003, BAR-007, NW-050, NW-069, NW-090, and S22 evidence for assignment,
  responsibility, and scoped sync.
- BAR-002, BAR-003, BAR-008, NW-025, NW-026, NW-059, NW-060, NW-061, and
  NW-062 for offline capture, sync status, stale authority, and append-only
  correction.
- BAR-006, BAR-012, BAR-013, NW-029, NW-033, NW-072, NW-114, and scoped
  operational view evidence for attention, freshness, and review/read
  boundaries.
- NW-065 through NW-067 and NW-075 through NW-081 as synthetic reference
  run/diagnose evidence where the selected local/on-prem posture relies on
  those operations surfaces.

Do not convert fixture names, old stock/health/logistics labels, or legacy form
names into shared Datarun vocabulary.

## Allowed Changes

Use the narrowest changes needed to make and prove the selected M1.1 journey:

- local/on-prem configuration, provisioning input, setup, and assignment
  material for the one neutral field-check activity;
- narrowly scoped server/web-admin/mobile/deployment changes only when current
  behavior cannot execute the selected M1.1 path as accepted;
- focused tests for changed behavior;
- one secret-safe evidence artifact for the executed M1.1 proof, expected at
  `docs/agent-working-surface/artifacts/NW-171-m11-owner-operated-core-field-work-closure-evidence.md`;
- status and backlog acceptance updates after execution evidence exists.

If no runtime code change is needed, produce the evidence packet and docs/status
updates only. Existing accepted control lanes do not need to be re-proved unless
they are touched, used as the acceptance gate, or show drift.

## Forbidden Changes

Do not:

- implement S06 lifecycle;
- implement candidate promotion, duplicate stewardship, merge/split UX, or
  place-like lifecycle semantics;
- change contracts, schemas, sync protocol, envelope fields/types, BAR, CDL,
  gap register, or authority rules;
- promote product labels into authority, identity, scope, contract, or storage
  primitives;
- harden Keycloak cutover unless NW-166 is selected;
- import legacy accounts or passwords;
- import or replay submitted records;
- claim real-production cutover, broad real-user rollout, retention/security
  promises, off-host disaster recovery, or independent human continuity;
- add reporting/export/import, broad audit, reconciliation/read-model
  expansion, tenant/control-plane work, or broad web-admin surfaces;
- add new scope axes, query-as-config authority, custom custody scope, or
  hidden sync scope;
- add trigger execution, automatic work creation, auto-resolution, batch
  resolution, or resolver reassignment;
- add IdP group/claim/JWT `actor_id`, request-body actor, UI-only role, or
  mobile-side authority.

## Acceptance Criteria

NW-171 is accepted only when the evidence answers every applicable row below:

```text
User-visible outcome:
Security/authorization checked:
Offline/sync checked:
Freshness checked:
Review/attention checked:
Operations/support checked:
Validation evidence:
Explicit deferrals and routes:
Accepted evidence preserved:
Stop conditions:
```

Minimum acceptance:

- The selected field-check activity is configured and visible through accepted
  setup/package behavior.
- The selected field user authenticates through local Keycloak/OIDC and resolves
  through an explicit active principal binding.
- Assignment-derived responsibility controls the visible work; no IdP claim,
  UI-selected actor, request-body actor, or mobile decision grants authority.
- Mobile shows scoped work and can capture at least one record offline or under
  controlled offline simulation.
- Local save, waiting/syncing/synced, stale caveat where relevant, and correction
  states are visible without false live-truth claims.
- Sync persists the original record and the correction append-only without
  mutating history.
- Owner/supervisor scoped standing shows freshness and at least one accepted
  attention/review cue without becoming broad reporting/export/import.
- Operator evidence names the exact local/on-prem posture, including
  `app.nmcpye.org` and `auth.nmcpye.org` unless a one-host/path posture was
  explicitly selected with rationale.
- Any real-use evidence is explicitly owner-approved and bounded; otherwise the
  proof uses synthetic or neutral extracted evidence.
- Runtime tests are run only for touched runtime surfaces, plus the required
  full gates for those surfaces.

## Validation Commands

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-171|M1.1|owner-operated local/on-prem core field-work closure" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/agent-working-surface/prompts/NW-171-implement-m11-owner-operated-core-field-work-closure.md
```

For docs/evidence-only execution, also run:

```bash
cd /home/hamza/datarun-platform
test -f docs/agent-working-surface/artifacts/NW-171-m11-owner-operated-core-field-work-closure-evidence.md
rg "User-visible outcome:|Security/authorization checked:|Operations/support checked:|Accepted evidence preserved:" docs/agent-working-surface/artifacts/NW-171-m11-owner-operated-core-field-work-closure-evidence.md
```

If a blocker is claimed, validate the exact format:

```bash
cd /home/hamza/datarun-platform
rg "Blocked action:|Evidence:|Smallest missing capability/fact:|Independent work that can continue:|Owner question, if absolutely required:" docs/agent-working-surface/artifacts/NW-171-m11-owner-operated-core-field-work-closure-evidence.md docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
```

If server, web-admin, assignment, config, sync, auth, or attention behavior is
changed, start the test database and run focused server evidence before any
full server gate:

```bash
cd /home/hamza/datarun-platform
docker compose -f docker-compose.test.yml up -d test-db
cd /home/hamza/datarun-platform/server
./mvnw -Dtest=ProductionAuthIntegrationTest,ConfigIntegrationTest,WebAdminAssignmentWorkflowIntegrationTest,ScopeFilteredSyncIntegrationTest,ResponsibilityBindingScenarioIntegrationTest,WebAdminOperationalViewIntegrationTest,ConflictResolutionIntegrationTest test
./mvnw test
```

If mobile login, actor session, work readiness, capture, correction, sync
status, local state, native/platform/auth, or plugin behavior is changed, run:

```bash
cd /home/hamza/datarun-platform/mobile
flutter test test/app_state_session_test.dart test/work_readiness_test.dart test/capture_handoff_test.dart test/correction_flow_test.dart test/sync_panel_test.dart
flutter test
cd /home/hamza/datarun-platform/mobile/android
timeout 900s ./gradlew :app:compileDebugKotlin --console=plain --no-daemon --stacktrace
```

`flutter analyze` may be reported but remains known-red/non-blocking unless a
separate selected route changes that standing.

Manual/live evidence must record exact commands, device/emulator identity when
used, issuer, Datarun base URL, resolved actor IDs, URLs probed, result codes,
and secret-safe logs. Do not retain tokens, passwords, authorization codes,
cookies, client secrets, private keys, or other sensitive values.

Runtime tests are skipped only when no runtime code, contracts, schemas,
migrations, CI behavior, server/mobile/deployment code, or operations policy
changed; record that rationale explicitly.

## Stop Conditions

Stop and report before implementation if the selected path requires any of the
forbidden changes above.

Stop and route instead of implementing if the work requires:

- maintained known things, active/inactive/moved/retired truth, missing-known-
  thing promotion, duplicate stewardship, merge/split UX, or place-like
  lifecycle semantics;
- new scope semantics beyond accepted geography, subject-list, activity, and
  temporal assignment axes;
- broad reporting/export/import, audit, warehouse, reconciliation, or aggregate
  access beyond underlying scoped detail access;
- Keycloak cutover hardening or a hardened-production Keycloak claim;
- retention/security/device lifecycle/offboarding promises;
- trigger execution, auto-resolution, batch resolution, or resolver
  reassignment;
- tenant/control-plane or managed SaaS behavior.

If a blocker is real, use exactly this format and continue any independent work
that remains possible:

```text
Blocked action:
Evidence:
Smallest missing capability/fact:
Independent work that can continue:
Owner question, if absolutely required:
```

Do not use broad "pilot blocked" or "not production-ready" language as a
substitute for the exact impossible action and evidence.

## Explicit Deferrals And Routes

- S06 known-things lifecycle, candidate promotion/rejection, duplicate
  stewardship, merge/split UX, and place-like lifecycle semantics:
  S06/BAR-105/NW-021 before implementation.
- Keycloak non-dev start mode, durable external Keycloak database, and
  production cutover hardening: NW-166.
- Retention/security/device lifecycle/offboarding promises: NW-054/BAR-106.
- Broad reporting/export/import/reconciliation/read-model expansion:
  NW-044.
- Conflict automation, batch resolution, auto-resolution, and resolver
  reassignment: NW-045.
- New subject/query/custom/custody scope: NW-053/BAR-108.
- Tenant-aware runtime/storage/sync or managed SaaS control plane:
  NW-094 through NW-098 when triggered.
- Pattern registry/projection durable extraction: NW-073 only if the selected
  implementation depends on accepted pattern behavior as normative.
- Legacy account import and submitted-record import/replay: not selected by
  NW-171; require a separate owner-selected route.
- Real-production approval and cutover: not selected by NW-171; require a
  separate owner-selected route after the M1.1 evidence is reviewed.

## Commit Boundary

Use implementation commits only for actual runtime or test changes. Use a
separate acceptance/status commit when NW-171 evidence is complete and status or
backlog standing changes.

Do not combine NW-171 execution with NW-170 route-selection changes,
S06/BAR-105/NW-021 decision work, NW-166 hardening, retention/security policy,
tenant/control-plane work, broad reporting/import/export, or checkpoint work.

Suggested implementation commit subject when runtime code changes:

```text
feat(product): close M1.1 field-work journey

NW: NW-171
```

Suggested evidence-only commit subject if no runtime code changes:

```text
docs(product): prove M1.1 field-work journey

NW: NW-171
```

Suggested acceptance commit subject:

```text
docs(status): accept M1.1 field-work closure

NW: NW-171
```
