# NW-056 Product Standing And Production Readiness Map

Status: accepted

Date: 2026-06-05

Authority note: this artifact is a product-facing standing and routing map. It
does not change CDL authority, contracts, runtime behavior, schemas, APIs, UI,
mobile behavior, BAR status, or backlog priority by itself. The accepted kernel
status remains the BAR and backlog evidence.

## 1. Executive Product Standing

Status terms used below:

- `production_kernel_accepted`: current backend/mobile kernel capability is in
  the accepted baseline with BAR/backlog evidence.
- `scenario_runtime_evidenced`: a named scenario has targeted runtime probe
  evidence, but this does not mean the scenario is a finished product workflow.
- `operator_deployable_with_constraints`: a technically skilled deployment team
  can operate the kernel with explicit constraints and external hardening.
- `product_surface_partial`: the underlying capability exists, but the user
  surface is development-only, skeletal, or not product-ready.
- `blocked_by_future_decision`: productization would need a successor
  product/platform/security decision before implementation.
- `not_started`: no accepted current implementation or product surface exists.

- `production_kernel_accepted`: Datarun is currently a field-operations kernel
  for append-only structured capture, assignment-derived access, offline sync,
  config package delivery, rebuildable projections, conflict flags, bounded
  workflow patterns, production provider-token validation, explicit
  principal-to-actor binding, and shared-device actor partitions.
- `scenario_runtime_evidenced`: accepted runtime probes cover S00, S19, S21,
  S22, S23, S26, and S27. These prove current constructs can run meaningful
  structured capture, offline reconciliation, supervisor review, coordinated
  campaign, setup/config, scoped reporting inputs, and logistics transfer
  paths.
- `operator_deployable_with_constraints`: a real deployment is possible as an
  operator-managed API/kernel deployment when the operator supplies production
  infrastructure, OIDC/JWKS configuration, explicit principal-binding
  manifests, assignment/config setup, backup/monitoring, TLS, secrets, and
  operational runbooks. It is not a turnkey product deployment.
- `product_surface_partial`: the mobile app has setup, raw bearer credential
  verification, sync, work list, form capture, local advisories, and per-actor
  session data behavior, but it is not a polished mobile product and does not
  implement OIDC/Keycloak login UX. The server HTML admin/config console is
  explicitly development-only.
- `blocked_by_future_decision`: production admin authentication, online
  principal-binding admin APIs/UI, reporting warehouse/API, import/export,
  broad audit/history reads, batch conflict resolution, retention/security,
  sealed-partition recovery, new scope mechanisms, IdP claim/group authority,
  auto-resolution, resolver reassignment, triggers, and entity lifecycle remain
  successor routes.
- `not_started`: production-grade web admin authentication, mobile OAuth/OIDC
  login, Keycloak deployment automation, backup/restore/upgrade runbooks,
  observability, no-local-retention/redaction UX, and product-grade reporting
  dashboard surfaces are not implemented.

## 2. Capability Matrix

| Capability | User-facing meaning | Current backing | Evidence | Product readiness | Missing before productization | Route |
|---|---|---|---|---|---|---|
| Structured capture and correction | Workers can record configured information and append corrections without erasing prior records. | 11-field envelope, deployer shape DSL, append-only Event Store, mobile shape-driven form assembly. | BAR-001, BAR-002, BAR-010, NW-026/S00. | `production_kernel_accepted`; mobile `product_surface_partial`. | Product copy, form IA, correction UX, accessibility/localization, and admin-safe shape authoring UX. | Start UX over existing constructs; do not add event types or mutable form state. |
| Offline sync and scope-filtered pull | Field work can happen offline and later reconcile within assignment-derived access. | Sync push/pull contract, idempotent event insert, actor-bound push, scoped pull, actor-scoped `device_sync_state`. | BAR-003, BAR-007, BAR-008, NW-025/S19, NW-055. | `production_kernel_accepted`; mobile `product_surface_partial`. | Sync status language, failure recovery UX, operational runbook, production network/TLS/secrets posture. | Product UX can start; retention and recovery route through NW-054. |
| Subject history and handoff context | A newly responsible worker can get bounded prior subject context without rewriting normal live-sync watermarks. | `/api/sync/subject-history`, independent cursor, request-time authorization, alias-aware history. | BAR-004, NW-025/S19, NW-042/S22. | `production_kernel_accepted`; product handoff UX partial. | Handoff screens, history explanation, stale/freshness indicators, retained-history policy. | Start UX for bounded subject handoff; broad audit/history remains future decision. |
| Assignment create/end and command capability | Coordinators can create/end responsibilities only when command-capable and contained in one active assignment. | Assignment events, Scope Resolver, IDR-024 containment, `assignment_admin_capabilities`, IDR-029. | BAR-007, NW-050, NW-042/S22. | `production_kernel_accepted`; admin surface `product_surface_partial`. | Production admin auth, product-grade assignment management UX, bootstrap/runbook clarity. | Web UX can be designed around current commands; production admin auth needs decision first. |
| Conflict/flag review and designated resolver behavior | Questionable records stay traceable; exact designated resolver decisions control state participation. | Flag catalog, conflict events, Conflict Resolution Service, IDR-026 resolver equality. | BAR-006, BAR-013, NW-029/S21, NW-033/S26, NW-042/S22. | `production_kernel_accepted`; review UI development-only. | Production conflict review UI, resolver-visible queues, batch handling decision, operator training. | Single-flag UX can start after admin-auth route; batch/automation route through NW-045. |
| Deployer shapes, activities, expressions, severity, config publishing, and config delivery | Coordinators can configure what to collect, active activities, warnings, severity overrides, and publish atomic packages. | Shape Registry, Config Packager, expression rules, config package schema, mobile two-slot promotion. | BAR-010, BAR-011, NW-032/S23, NW-034. | `production_kernel_accepted`; config admin `product_surface_partial`. | Product-grade config builder, validation explanation, review/approval workflow, production admin auth. | Start web config UX from current contract; avoid scripts, custom state machines, or new scope logic. |
| Platform workflow patterns and projection | Users can see progress-like derived state for bounded platform-owned patterns. | Platform pattern registry, pattern-definition schema, server/mobile rebuildable projection. | BAR-012, BAR-014, NW-029/S21, NW-030/S27, NW-042/S22. | `production_kernel_accepted`; product vocabulary guarded by NW-047. | Product IA for progress/pending/blocked labels, clear flag-exclusion treatment, no mutable status truth. | Start vocabulary/product design using NW-047; new patterns or traversal route through NW-046. |
| Reporting/aggregate oversight | Coordinators can derive scoped, traceable report inputs from current projections and sync metadata. | Rebuildable projections, event timestamps, flags, scoped sync, test-local aggregation. | NW-033/S26; BAR-006, BAR-007, BAR-012, BAR-013, BAR-014. | `scenario_runtime_evidenced`; production dashboard `blocked_by_future_decision`. | Stable report view/API decision, freshness UX, drill-back model, export/import boundary. | NW-044 for reporting/import/export boundary before product dashboard/API implementation. |
| Production OIDC/JWT/Keycloak authority | Provider JWTs can authenticate only through explicit active principal bindings. | `oidc-jwks` validation by issuer/audience/JWKS URI plus `(issuer, subject) -> actor_id` lookup. | BAR-104, NW-037, NW-038, NW-040, IDR-027, IDR-028. | `production_kernel_accepted`; login UX `not_started`. | Keycloak deployment profile, mobile OAuth/OIDC login/token lifecycle, web admin auth model. | Provider integration can start at ops/design level; no group/claim/JWT `actor_id` authority. |
| Principal-binding provisioning | Operators can provision, rotate, deactivate, and audit provider-principal bindings through manifest application. | Deployment-managed manifest runner, append-only operation history, active binding support rows. | BAR-104, NW-040, IDR-028. | `operator_deployable_with_constraints`. | Deployment runbooks, manifest review workflow, production secrets/process ownership. | Ops hardening route; online binding-admin API/UI needs separate decision first. |
| Shared-device actor sessions | One physical device can separate actor-local mutable data and push with the correct actor credential. | IDR-030, mobile active actor session, per-actor token/watermark/cursor keys, per-actor local partitions, server `(device_id, actor_id)` sync state. | NW-052, NW-055. | `production_kernel_accepted`; switch UX partial. | Polished switch/sign-out UX, token refresh/lifecycle, expiry, sealed-partition recovery. | UX can start over IDR-030; retention/recovery route through NW-054. |
| Mobile setup, sync, work list, form capture, advisory warnings | Field users can connect with a bearer credential, sync, see subjects, fill forms, and see advisory warnings. | `SetupScreen`, `WorkListScreen`, `FormScreen`, `SyncPanel`, `DeviceIdentity`, `SyncService`. | Code inspection in NW-056 plus BAR-008, BAR-010, BAR-011, NW-055. | `product_surface_partial`. | OIDC login, product vocabulary, navigation, accessibility/localization, shared-device user flow, polished error recovery. | Start mobile UX/vocabulary polish now; login and retention/security need decisions. |
| Web admin subject/flag/assignment/location/config surfaces | Operators can inspect subjects/flags, create dev assignments, manage locations, author config, and publish packages in development. | Thymeleaf `/admin` and `/admin/config` controllers. | Code inspection in NW-056; comments mark HTML admin as development-only. | `product_surface_partial`; production admin auth `not_started`. | Authenticated production admin, role/command-aware UI, audit-oriented operator flows, hardened deployment posture. | Start product design only with dev-console caveat; production admin auth decision first. |
| Deployment packaging and operations | The server can be built and run with Postgres using Docker artifacts. | `server/Dockerfile`, `docker-compose.yml`, Flyway migrations, environment properties. | Code inspection in NW-056; BAR tests prove runtime behavior. | `operator_deployable_with_constraints`; turnkey ops `not_started`. | TLS/reverse proxy, secrets, backup/restore, observability, migration/rollback runbook, production compose/Kubernetes profile. | Ops readiness route; do not treat dev compose as production hardening. |

## 3. Runnable Scenario Map

| Scenario | What can run today | Evidence level | Product/UI work remaining |
|---|---|---|---|
| S00 structured capture | Valid configured capture persists through the existing envelope; correction is a second append-only event; duplicate retry is idempotent; pull is watermark ordered; concurrent same-subject work raises existing conflict mechanics. | `scenario_runtime_evidenced`: NW-026 plus BAR-001/BAR-002/BAR-003. | Form IA, correction affordance, user-facing trace/history language, product-grade validation messages. |
| S19 offline/stale authority | Structurally valid stale offline work is accepted, persisted, and flagged with existing temporal/role categories; normal pull stays current-scope and subject-history has an independent cursor. | `scenario_runtime_evidenced`: NW-025 plus BAR-003/BAR-004/BAR-007/BAR-008. | Offline confidence UX, stale authority explanation, sync recovery, retention policy for old/out-of-scope data. |
| S21 supervisor review | In-scope supervisor visibility works, cross-scope visibility is excluded, review advances configured pattern state, unauthorized review is accepted and flagged, and exact resolver acceptance re-includes state. | `scenario_runtime_evidenced`: NW-029 plus BAR-006/BAR-007/BAR-012/BAR-013/BAR-014. | Supervisor review queue UX, pending-review language, conflict resolution UX, no auto-resolution or resolver reassignment. |
| S22 coordinated distribution campaign | Existing assignments, capture, subject-history handoff, domain uniqueness flags, transfer pattern, scoped sync, and test-local progress/freshness aggregation support a constrained coordinated campaign. | `scenario_runtime_evidenced`: NW-042. | Campaign/product IA, progress display, grouped-location UX, supply-flow screens, no discovered-unit lifecycle or custom campaign scope without decision. |
| S23 setup/config | Invalid setup fails before publish; valid shapes/activity/status/roles/expressions/severity/pattern binding package atomically; old and new versions remain interpretable; mobile current/pending promotion works. | `scenario_runtime_evidenced`: NW-032 plus BAR-010/BAR-011/BAR-012/BAR-014. | Product-grade config builder, approval/review flow, safer editors than raw JSON, production admin auth. |
| S26 reporting/aggregate oversight | Current projections and scoped inputs can expose timestamps, watermarks, unresolved flag counts, exact resolver re-inclusion/exclusion, scoped inclusion/exclusion, and drill-back links in tests. | `scenario_runtime_evidenced`: NW-033. | Product reporting/dashboard route, stable report API/view model, freshness UX, export/import boundary. NW-044 before production reporting expansion. |
| S27 logistics transfer | Existing transfer pattern supports dispatch, partial receipt, discrepancy report, discrepancy resolution, scoped sync, and out-of-order transition flagging without health-specific semantics. | `scenario_runtime_evidenced`: NW-030 plus BAR-012/BAR-013/BAR-014. | Logistics labels/screens, custody/handoff UX, discrepancy review UX, no custom custody scope or auto-resolution without decision. |
| S24 long-running deployment data lifecycle | Current kernel has selective retention, actor partitions, subject-history backfill, and immutable central events, but not a complete lifecycle/retention product. | Scenario pressure only; BAR-106 and NW-054 remain future-decision routes. | Decide local expiry, no-local-retention/redaction, encryption, decommissioning, sealed-partition recovery, backup/audit handling. |
| S25 worker onboarding/transfer/exit | Current assignments, scope-filtered sync, subject history, stale-authority flags, assignment-admin commands, and shared-device partitions cover part of onboarding/transfer/handoff pressure. | Scenario pressure plus partial accepted evidence from NW-025, NW-042, NW-050, NW-055. | Worker lifecycle UX, account/login workflow, exit/decommission policy, retained local data handling. NW-054 and admin-auth/mobile-login routes remain. |

## 4. Production Deployment Readiness

Direct answer: a real production deployment is possible now only as
`yes_with_constraints` for an operator-managed platform/kernel deployment. It
is `no` for a turnkey production product with hardened admin UI, mobile OIDC
login, reporting dashboard, retention/security policy, observability, backup,
upgrade, and support runbooks.

| Area | Readiness | Concrete constraints |
|---|---|---|
| Platform/kernel readiness | `yes` | BAR-001 through BAR-015 and BAR-104 are accepted. Runtime behavior is backed by targeted tests and scenario probes. This does not imply product polish. |
| Deployment manifest/provisioning readiness | `yes_with_constraints` | Principal-binding manifest provisioning is accepted, but deployment teams must own manifest review, secret handling, bootstrap actor/assignment/config setup, and operational process. |
| Admin/operator readiness | `no` | `/admin` and `/admin/config` are development HTML surfaces. `AdminController` and `WebConfig` explicitly state production admin auth is not wired. |
| IdP/Keycloak integration readiness | `yes_with_constraints` | Server can validate configured OIDC/JWKS issuer/audience/JWKS tokens and map only explicit active `(issuer, subject)` bindings. There is no Keycloak deployment automation, group/claim authority, or product admin model. |
| Mobile login/session readiness | `yes_with_constraints` | Mobile verifies raw bearer credentials through `/api/auth/me` and uses server-resolved actor sessions. It does not implement OAuth/OIDC browser login, refresh-token lifecycle, Keycloak provider selection, or polished shared-device switch UX. |
| Data retention/security readiness | `needs_decision` | BAR-106/NW-054 still owns expiry, decommissioning, local encryption, sensitivity/redaction, no-local-retention views, sealed-partition recovery, and broader retention/security. |
| Observability/backup/upgrade readiness | `no` | Dockerfile, Flyway, and dev compose exist, but there is no production deployment profile, monitoring/alerting, backup/restore, disaster recovery, migration rollback, capacity, or SLO runbook. |
| Support/runbook readiness | `no` | Operator procedures for config promotion, auth manifest application, assignment bootstrap, incident handling, support escalation, data recovery, and field-device lifecycle are not documented as product runbooks. |

## 5. UI/UX Start Decision

| Product surface | Start decision | Boundary |
|---|---|---|
| Web admin/config UX | `start_after_NW_056_output` | Safe to design/productize around existing shapes, activities, expressions, severity, config publish, assignments, locations, subjects, and flags. Must preserve development-console caveat until production admin auth lands. |
| Production admin authentication for web admin | `needs_decision_first` | Needs admin identity/authority/audit model. Do not infer authority from IdP groups, request body, or current dev admin actor. |
| Online production principal-binding admin API/UI | `needs_decision_first` | IDR-028 selected deployment-managed manifests and explicitly deferred online admin APIs/UI. |
| Mobile login via OIDC/Keycloak | `needs_decision_first` | Server provider validation is accepted, but mobile token acquisition, refresh, logout, shared-device switch, and secure storage need a bounded mobile auth decision/design. |
| Mobile shared-device actor switch UX | `start_after_NW_056_output` | Runtime/session partition boundary is accepted. UX can start if it uses `/api/auth/me` or prior server-resolved sessions and does not create actor authority locally. |
| Mobile operational vocabulary and navigation polish | `start_after_NW_056_output` | Use NW-047 terms over current constructs. Warnings remain advisory, not authoritative rejection. |
| Reporting/dashboard UX | `needs_decision_first` | S26 proves current inputs, not a production report API/warehouse/export model. Route through NW-044 before building durable product reporting surfaces. |
| Conflict review/batch handling UX | `needs_decision_first` | Single-flag review can be designed after admin auth, but batch handling, automation, pending-match queues, and auto-resolution route through NW-045. |
| Device retention/sealed partition recovery UX | `needs_decision_first` | IDR-030 intentionally leaves expiry, decommissioning, token retention, recovery, and local encryption to BAR-106/NW-054. |

## 6. Vocabulary And Information Architecture

Use NW-047 as the UX/product vocabulary companion. Product labels may improve
navigation and comprehension, but they must map to existing constructs and must
not become platform authority.

| Product term | Safe product meaning | Safe backing construct | Must not become |
|---|---|---|---|
| Campaign | A time-bounded operational work set shown to coordinators and field teams. | Configured activities, assignments, location/subject scope, capture events, transfer pattern, projections. | New scope type, new event type, platform campaign primitive, trigger engine, or custom lifecycle. |
| Handoff | Continuity of work/custody/responsibility across actors or assignments. | Assignment history, subject-history backfill, transfer-with-acknowledgment pattern, scoped sync. | Broad historical pull, normal watermark rewrite, new scope mechanism, or cross-actor local data access. |
| Progress | Derived view of how a work set, subject, location, or transfer is moving. | Rebuildable projections, pattern state, event timestamps, sync watermarks, flags. | Durable mutable status column, `status_changed` event, or reason to reject structurally valid offline work. |
| Pending review | A record, flag, transition, or discrepancy needing human inspection. | Flags, resolver routing, review events, pattern/review projections. | Auto-resolution, resolver reassignment, batch bypass, or non-designated resolver authority. |
| Blocked | User-facing warning that work needs attention before it is operationally clean. | Unresolved flag exclusion, advisory warnings, projection state. | Canonical blocked-state table, automatic rejection, or trigger side effect. |
| Route | A user-friendly way to describe assigned places/workload. | Assignment scope over platform-fixed geography/subject/activity axes. | IdP group authority, custom/query scope, or UI-only permission. |
| Worker | A human using the product in an actor session. | `actor_ref` authorship, authenticated actor context, explicit principal binding, assignment-derived access. | Provider user record as platform authority, JWT `actor_id`, or UI-selected actor authority. |
| Provider | External authentication system that proves a principal. | OIDC/JWKS issuer/audience/JWKS validation plus explicit active principal binding. | Assignment authority, resolver authority, binding-admin authority, or scope source. |
| Keycloak group | Optional IdP metadata that may help humans organize identity outside Datarun. | None for current authority. Future provisioning-input use would require a successor decision. | Direct assignment scope, command authority, resolver authority, or actor mapping. |
| Admin | A person or process operating setup, assignment, auth manifest, or review surfaces. | Existing assignment-admin command capability, deployment provisioning, future admin auth model. | Fixed dev actor as production authority, IdP role authority, or blanket root power. |

## 7. Product Gaps And Successor Routes

No new backlog rows or prompts are added by NW-056. Existing routes are named
where they already exist. Candidate routes below should become backlog rows only
when the steward chooses them as the next slice.

Title: Production deployment runbook and ops hardening map

Type: ops_readiness_review

Priority: P1

Depends on: NW-056, BAR-001 through BAR-015, BAR-104, NW-040

Expected artifact: deployment checklist/runbook covering TLS/reverse proxy,
secrets, database backup/restore, Flyway migration/rollback, monitoring,
incident handling, config publish, auth manifest application, and assignment
bootstrap.

Why now: the kernel is operator-deployable with constraints, but not turnkey.

Stop condition: stop if the route tries to use `docker-compose.yml` as
production hardening or changes runtime contracts.

---

Title: Production web admin authentication and admin-authority model

Type: future_decision

Priority: P1

Depends on: BAR-104, IDR-027, IDR-028, IDR-029, NW-050, NW-056

Expected artifact: IDR or bounded decision for production admin
authentication, admin role/command authority, audit semantics, and dev-console
replacement/containment.

Why now: web admin/config UX cannot be productionized safely while `/admin`
uses a fixed dev actor and no production admin auth.

Stop condition: stop if IdP groups/roles/claims, request-body actor fields, or
the fixed dev admin actor are proposed as production authority.

---

Title: Mobile OIDC/Keycloak login and token lifecycle decision

Type: product_platform_decision

Priority: P1

Depends on: BAR-104, IDR-030, NW-055, NW-056

Expected artifact: mobile auth/session decision for provider login, token
refresh/logout, `/api/auth/me` actor resolution, secure storage, shared-device
switching, and error recovery.

Why now: server-side OIDC/JWKS validation is accepted, but mobile still asks
for a raw bearer credential.

Stop condition: stop if mobile UI, JWT `actor_id`, or provider claims become
actor/scope authority.

---

Title: Device data expiry, decommissioning, and sealed-partition recovery

Type: future_decision

Priority: P1

Depends on: existing NW-054, BAR-106, IDR-030, NW-055, S24, S25

Expected artifact: NW-054 decision covering local expiry, token/session
retention, encryption, no-local-retention/redacted views, decommissioning, and
sealed-partition recovery without server event deletion.

Why now: shared-device partitions landed, but the product cannot responsibly
promise exit/decommissioning or sensitive local retention behavior yet.

Stop condition: stop if the route deletes canonical server event history,
rewrites normal sync watermarks, or lets one actor recover another actor's
pending work without a security decision.

---

Title: Web admin/config UX productization over accepted config surfaces

Type: product_design_then_implementation

Priority: P2

Depends on: NW-047, NW-056, BAR-010, NW-050, production admin-auth decision for
production release

Expected artifact: bounded UX/design prompt for shape/activity/expression/
severity/config publish, assignment management, and flag review over existing
contracts.

Why now: the config/package kernel is accepted and current UI is too raw for
coordinators.

Stop condition: stop if UX introduces scripts, deployer-authored state
machines, new scope logic, new envelope fields/types, or new config-package
contract sections without routing.

---

Title: Mobile operational vocabulary and navigation polish

Type: product_design_then_implementation

Priority: P2

Depends on: NW-047, NW-056, NW-055, BAR-008, BAR-010, BAR-011

Expected artifact: mobile UX IA and implementation prompt for work list,
activity selection, capture, warnings, sync, freshness, handoff context, and
shared-device switch language.

Why now: mobile kernel behavior is accepted, but screen language remains
platform-ish and setup/sync flows are skeletal.

Stop condition: stop if advisory warnings become authoritative rejection or
product labels become platform authority.

---

Title: Reporting aggregation plus structured import/export boundary.

Type: existing future_decision route

Priority: P2

Depends on: existing NW-044, NW-033/S26, BAR-106, BAR-107

Expected artifact: product/platform decision splitting scoped report views,
reporting warehouse/export, structured import, and external interoperability.

Why now: S26 proves report inputs and traceability, not a production reporting
API or export/import product, Domain reality will need reports, exports, and interoperability.

Stop condition: stop if the route adds a reporting warehouse/API, import/export
contract, new envelope fields/types, or broad audit/history read without
decision authority.

---

Title: Conflict review queue and batch handling boundary

Type: existing future_decision route

Priority: P3

Depends on: existing NW-045, BAR-006, BAR-013, IDR-026

Expected artifact: decision comparing manual queues, batch commands,
pending-match derivations, and narrow auto-resolution without direct flag
mutation.

Why now: single-flag resolver behavior is accepted, but production operators
will need humane review queues and may ask for batch behavior.

Stop condition: stop if non-designated actors resolve flags, direct flag
mutation appears, resolver reassignment is assumed, or auto-resolution is
implemented without route.

---

Title: Subject/query/custom scope pressure checkpoint

Type: existing future_decision route

Priority: P4

Depends on: existing NW-053, BAR-108, access-control scenario, S22, S25

Expected artifact: decision on whether current geography/subject-list/activity
scope is enough or whether a platform-owned new scope mechanism is needed.

Why now: most current product routes should avoid new scope, but auditor and
cross-axis requests may reappear during UI/product design.

Stop condition: stop if deployer scripts, query-as-config, IdP groups, or UI
filters are proposed as access authority.

## 8. Verification Ledger

Source files inspected:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/README.md`
- `docs/agent-working-surface/baseline-acceptance-register.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/operational-ux-layering-companion.md`
- `docs/implementation/module-interfaces.md`
- `docs/README.md`
- `docs/constraints.md`
- `docs/scenarios/README.md`
- `docs/access-control-scenario.md`
- `docs/scenarios/00-basic-structured-capture.md`
- `docs/scenarios/19-offline-capture-and-sync.md`
- `docs/scenarios/21-chv-supervisor-operations.md`
- `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`
- `docs/scenarios/23-configure-new-operational-activity.md`
- `docs/scenarios/24-long-running-deployment-data-lifecycle.md`
- `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`
- `docs/scenarios/26-operational-reporting-and-aggregate-oversight.md`
- `docs/scenarios/27-logistics-distribution-composite.md`
- `contracts/sync-protocol.md`
- `contracts/config-package.schema.json`
- `contracts/shape-format.schema.json`
- `contracts/flag-catalog.md`
- `contracts/pattern-definition.schema.json`
- `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
- `docs/decisions/idr-028-production-principal-binding-administration.md`
- `docs/decisions/idr-029-assignment-admin-command-capability.md`
- `docs/decisions/idr-030-shared-device-session-lifecycle.md`
- `server/src/main/java/dev/datarun/server/admin/AdminController.java`
- `server/src/main/java/dev/datarun/server/config/ConfigAdminController.java`
- `server/src/main/java/dev/datarun/server/authorization/WebConfig.java`
- `server/src/main/java/dev/datarun/server/authorization/ActorTokenInterceptor.java`
- `server/src/main/resources/application.properties`
- `docker-compose.yml`
- `server/Dockerfile`
- `mobile/lib/presentation/screens/setup_screen.dart`
- `mobile/lib/presentation/screens/work_list_screen.dart`
- `mobile/lib/presentation/screens/form_screen.dart`
- `mobile/lib/presentation/widgets/sync_panel.dart`
- `mobile/lib/data/device_identity.dart`
- `mobile/lib/data/sync_service.dart`

Commands run:

- `git status --short`
- Focused `sed`, `rg`, and `wc -l` reads for the files above
- `python3 scripts/query_cdl.py --id CDL-001,CDL-002,CDL-006,CDL-007,CDL-008,CDL-010,CDL-018,CDL-021,CDL-030,CDL-031,CDL-032,CDL-034,CDL-035,CDL-037,CDL-038,CDL-039,CDL-041,CDL-042,CDL-044,CDL-046,CDL-047,CDL-048,CDL-049,CDL-050,CDL-051,CDL-052,CDL-053,CDL-054,CDL-055,CDL-056 --format concise`
- `git diff --check` passed

Test evidence reused from BAR/NW rows:

- BAR-001 through BAR-015 and BAR-104 accepted evidence.
- NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, NW-033/S26, NW-042/S22.
- NW-037, NW-038, NW-040 for production-auth and principal-binding evidence.
- NW-050 for assignment-admin command-capability evidence.
- NW-055 for shared-device actor partition evidence.

Tests not rerun and why:

- Maven and Flutter tests were not rerun for this slice because NW-056 changed
  documentation only and the prompt explicitly said not to run Maven/Flutter
  unless runtime/test files changed.

Drift or uncertainty:

- No source drift was found that required stopping.
- The main uncertainty is product sequencing, not platform status: multiple UI
  slices can start as design work, but production release of admin, login,
  reporting, and retention/security surfaces remains gated by the successor
  decisions named above.
