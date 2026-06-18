# Product/Admin Surface Forward Plan

Status: non-authoritative routing artifact
Document type: routing_artifact
Owner: architecture steward
Source: NW-056 product standing map; 2026-06-18 steward routing request after NW-068, NW-069, NW-070, and NW-074 acceptance
Authority: none. Use the CDL, contracts, BAR, accepted platform specs, and selected NW rows for authority.
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`; `docs/agent-working-surface/platform-next-work-backlog.md`; `docs/agent-working-surface/operational-ux-layering-companion.md`; `docs/specifications/platform/configuration-package-and-shapes.md`; `docs/specifications/platform/expression-language.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/production-auth-principal-binding.md`

## Purpose

This artifact routes the next product/admin surface work without selecting or
accepting any new backlog row. It is a planning map for the next few bounded
work items after the durable NW-068 through NW-070 behavior specs and the
NW-074 stale-reference cleanup landed.

The goal is to start Candidate 1 product/admin work while keeping production
admin authority, web admin/config productization, and mobile UX polish from
being bundled into one unsafe implementation slice.

## Current Backlog Standing

| Candidate route from NW-056 | Current backlog status | Recommended promotion |
|---|---|---|
| Production web admin authentication and admin-authority model | No standalone NW row. It exists as a candidate successor route inside accepted NW-056. | Promote first as a P1 `future_decision` or product/platform decision row. |
| Web admin/config UX productization over accepted config surfaces | No standalone NW row. It exists as a candidate successor route inside accepted NW-056. | Promote after the admin-auth decision for production implementation. Product design may be routed earlier only with the development-console caveat. |
| Mobile operational vocabulary and navigation polish | No standalone NW row. It exists as a candidate successor route inside accepted NW-056. | Promote as a P2 product design/implementation row if scoped to vocabulary/navigation over accepted behavior. Select NW-071 first if the slice depends on shared-device session or local-state semantics. |

Existing rows that must not be silently absorbed:

- NW-071 remains the route for shared-device session and local-state durable
  behavior before work relies on IDR-030-era prose as normative.
- NW-072 remains the route for conflict flag and resolution durable behavior
  before conflict UI, flag reporting, batch resolution, auto-resolution,
  resolver reassignment, or flag-category/resolvability changes.
- NW-073 remains the route for pattern registry/projection durable behavior
  before pattern traversal, workflow projection changes, pattern APIs, or
  pattern product work.
- NW-044, NW-045, NW-053, and NW-054 remain future-decision routes for
  reporting/import-export, conflict automation/batch handling, new
  subject/query/custom scope, and device retention/security.
- NW-067 R12 blocks repeatable production deployment acceptance, not design or
  decision routing for product/admin surfaces. Do not claim real production
  readiness from these product/admin routes while NW-067 remains blocked.

## Priority Order

1. **Production web admin authentication and admin-authority model.**
   This is the first decision route because production web admin/config
   productization depends on an authenticated admin actor, command authority,
   and audit boundary. It must not implement UI or online binding-admin APIs
   before the authority model is selected.

2. **Mobile operational vocabulary and navigation polish, if narrowly scoped.**
   This can proceed in parallel with or immediately after the admin-auth
   decision if it stays within accepted mobile behavior: work list, activity
   selection, capture, sync/freshness language, warning copy, and handoff
   explanations. If the slice makes shared-device switching, actor-local
   partitions, sealed pending work, token/session material, or local-state
   compatibility normative, run NW-071 first.

3. **Web admin/config UX productization over accepted config surfaces.**
   Product design can start with an explicit development-console caveat, but
   production implementation should wait for the admin-auth decision. Keep the
   first web slice on accepted config, expression, assignment, location,
   subject, and single-item operational review surfaces. Do not include
   reporting dashboards, batch conflict operations, online binding-admin APIs,
   new scopes, or retention/security.

## Work Item A: Admin Auth Decision

Recommended backlog row:

| Field | Value |
|---|---|
| Title | Decide production web admin authentication and admin-authority model |
| Type | `future_decision` or `product_platform_decision` |
| Priority | P1 |
| Depends on | NW-056; NW-068; NW-069; NW-070; NW-074; BAR-104; NW-050; NW-047 |
| Output | Bounded decision route selecting production web admin authentication, authenticated admin actor context, admin command authority, audit semantics, and development-console containment. Use `docs/documentation-organization.md` to choose the durable home when the row is promoted, and do not leave accepted authority only in `artifacts/`. |

Decision questions:

- Which admin surfaces are in scope first: `/admin`, `/admin/config`,
  assignment administration, config publishing, flag inspection/resolution, or
  subject/location inspection?
- Is admin actor context resolved through the accepted bearer/OIDC/JWKS
  principal-binding path, and how is it carried into HTML/admin requests?
- Which admin actions are ordinary accepted assignment-admin commands, which
  are config-publish/admin operations, and which remain deployment-managed
  procedures?
- What audit trail is required for admin login, config publish, assignment
  commands, and conflict resolution without inventing mutable state truth?
- How are development-only fixed actors and dev admin routes contained or
  disabled in production mode?

Stop and report if the route proposes any of these:

- IdP groups, roles, resource claims, custom claims, or JWT `actor_id` as
  platform, assignment, resolver, binding-admin, or web-admin authority;
- request-body actor IDs or the fixed development admin actor as production
  authority;
- online production principal-binding admin APIs/UI inside this decision
  without a separately selected binding-admin authority route;
- blanket root/admin power that bypasses accepted assignment-admin,
  resolver, config-publish, or production-auth boundaries.

Acceptance boundary:

- A decision is enough for this row. Runtime code, schemas, contracts, and
  product screens belong in successor implementation rows.
- Use a CDL successor only if the selected model creates new structural
  authority, new process-boundary contracts, new actor identity sources, or
  a new authority primitive. If the model stays inside accepted principal
  binding plus explicit admin command semantics, route the durable output as a
  product/platform/security specification or other explicitly selected durable
  decision home, not as a generic non-binding artifact.

## Work Item B: Mobile Vocabulary And Navigation

Recommended backlog row:

| Field | Value |
|---|---|
| Title | Productize mobile operational vocabulary and navigation polish |
| Type | `product_design_then_implementation` |
| Priority | P2 |
| Depends on | NW-047; NW-056; NW-055; NW-059; NW-060; NW-061; NW-062; BAR-008; BAR-010; BAR-011 |
| Conditional depends on | NW-071 if shared-device session/local-state semantics are used as durable behavior; mobile OIDC/login decision if provider login is in scope; NW-054 if retention, offboarding, decommissioning, local encryption, no-local-retention, or sealed recovery is in scope |
| Output | Product/UX specification or bounded implementation prompt for mobile work list, activity selection, capture, warnings, sync/freshness, handoff context, and navigation labels over accepted constructs. |

Safe first mobile scope:

- replace platform-ish labels with NW-047 product vocabulary;
- clarify saved-local, waiting-to-sync, syncing, synced, failed, retry, missing
  setup/forms, missing assignment, ready-to-capture, correction, warning, and
  freshness language from accepted NW-059 through NW-062 behavior;
- improve navigation among setup, work list, capture, correction, sync status,
  and history/handoff context without changing stored state or sync semantics.

Forbidden in this row:

- OAuth/OIDC browser login, refresh-token lifecycle, provider selection, or
  Keycloak UX;
- mobile-selected actor authority, JWT `actor_id` authority, or provider claim
  authority;
- advisory warnings becoming authoritative rejection of structurally valid
  state or policy anomalies;
- new event types, envelope fields, sync protocol fields, local retention
  commitments, cross-actor local recovery, or entity lifecycle claims.

Expected verification when implementation lands:

- focused Flutter widget/state tests for touched screens and navigation states;
- targeted sync/setup/capture tests when labels or recovery actions depend on
  existing `AppState`, `SyncResult`, config, assignment, or pending-event state;
- full mobile test suite when the slice touches shared navigation or shared
  state helpers.

## Work Item C: Web Admin/Config UX

Recommended backlog row:

| Field | Value |
|---|---|
| Title | Productize web admin/config UX over accepted config surfaces |
| Type | `product_design_then_implementation` |
| Priority | P2 |
| Depends on | Work Item A accepted for production implementation; NW-047; NW-056; NW-068; NW-069; NW-070; NW-074; BAR-010; BAR-011; NW-050 |
| Conditional depends on | NW-072 if conflict queues, flag reporting, batch resolution, or resolver behavior is productized; NW-073 if pattern progress/projection or pattern APIs are productized; NW-044 if reporting dashboards/export/import are included; NW-053 if new subject/query/custom scope is needed; NW-054 if retention/security is included |
| Output | Product specification and/or bounded implementation prompt for shape/activity/expression/severity/config publish, assignment management, location/subject inspection, and safe operational review over accepted contracts and specs. |

Safe first web scope:

- productize config authoring and publishing around accepted form shapes,
  activities, role-action maps, expressions, severity, sensitivity,
  uniqueness declarations, package validation, and atomic config delivery;
- productize assignment create/end UX around accepted
  `assignment_admin.create` and `assignment_admin.end` command capability plus
  same-assignment containment;
- expose validation errors, publish status, config version, and operator
  review language without changing package schemas or deploy-time validation;
- keep subject/location/flag views observational unless their command behavior
  has an accepted route.

Production release blockers:

- Work Item A must be accepted before `/admin` or `/admin/config` can be
  treated as production web admin surfaces.
- Real production deployment claims still require the production deployment
  route and NW-067 acceptance or successor evidence.

Forbidden in this row:

- deployer-authored scripts, functions, dynamic queries, state machines,
  custom scope logic, or trigger execution;
- new envelope fields/types, config-package sections, shape-format changes, or
  platform payload schemas as deployer form shapes;
- online principal-binding admin APIs/UI unless a separate binding-admin route
  selects them;
- broad audit/history reads, reporting warehouse/API, batch conflict
  resolution, auto-resolution, resolver reassignment, IdP claim authority, or
  new scope mechanisms.

Expected verification when implementation lands:

- server tests for admin authentication and authorization gates selected by
  Work Item A;
- focused controller/UI tests for changed admin/config flows;
- existing config validation/package tests when the UI edits shapes,
  activities, expressions, severity, sensitivity, uniqueness, or publish flow;
- assignment containment and command-capability tests when assignment-admin UI
  changes;
- conflict/resolver tests only if a later row explicitly productizes conflict
  review behavior.

## Dependency Chain

```text
accepted NW-068/NW-069/NW-070/NW-074
  -> Work Item A admin-auth/admin-authority decision
      -> production web admin/config implementation

NW-047 + accepted mobile UX/runtime slices
  -> Work Item B mobile vocabulary/navigation polish
      -> NW-071 first only if shared-device session/local-state semantics become normative

NW-072/NW-073/NW-044/NW-053/NW-054
  -> only when the selected product slice touches those deferred surfaces
```

## Stewardship Instructions For Next Selection

When selecting the next bounded task, add one backlog row at a time and write a
prompt with:

- exact files to read;
- authority and accepted specs;
- forbidden work;
- expected durable output home;
- tests or documentation checks;
- stop-and-report conditions;
- commit role and NW trailer expectations if commits are authorized.

Do not mark a candidate route accepted because this artifact exists. Acceptance
requires the selected row's durable output and verification evidence.
