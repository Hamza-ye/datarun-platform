# NW-082 Product Candidate 1 Milestone Boundary And Multi-Tenancy Routing

Status: non-binding routing artifact
Document type: routing_artifact
Owner: architecture steward
Source: NW-082 row and `docs/agent-working-surface/prompts/NW-082-decide-product-candidate-1-boundary-and-multi-tenancy-routing.md`
Authority: none; routes successor product, platform, architecture, contract, operations, and implementation work under the CDL, contracts, BAR, accepted specifications, operations documents, and NW backlog
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/agent-working-surface/platform-next-work-backlog.md`; `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`; `docs/agent-working-surface/artifacts/product-admin-surface-forward-plan.md`; `docs/agent-working-surface/artifacts/2026-06-18-multi-tenancy-architecture-analysis.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md`; `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`; `docs/operations/rehearsals/2026-06-18-production-deployment-r12-fresh-session-rerun.md`

## 1. Executive Summary

NW-082 should close as a non-binding routing map, not as accepted product
behavior or tenant architecture. It selects a safe Product Candidate 1 route:
a single-tenant/default-workspace operational candidate that productizes the
accepted kernel, admin-auth decision, mobile operational flows, and synthetic
reference-deployment evidence without claiming full SaaS multi-tenancy or real
production readiness.

The first Product Candidate 1 milestone should not be "pooled SaaS." The safe
boundary is:

- one customer-facing organization mapped to one hard tenant boundary;
- one default workspace/deployment for operational data, configuration,
  locations, subjects, assignments, and workflow projections;
- managed single-tenant deployment or bridge isolation as the first
  multi-customer posture;
- assignment scope remains authorization inside a workspace, not tenant
  isolation;
- no tenant or workspace fields in the event envelope in the first route.

This artifact does not make those terms binding. The first actionable successor
must decide tenant/workspace vocabulary and the managed-deployment isolation
boundary in the correct durable home before implementation or product copy
depends on it.

Product Candidate 1 is viable only as a sequenced milestone. It needs, in
order, a tenant/workspace boundary decision, a product specification for the
candidate experience, production web admin login/session implementation,
admin command gates, mobile login/session and/or mobile vocabulary decisions,
and real-production operations approval only if real users or real data enter
the environment.

## 2. Authority And Standing Review

### Source Order Used

| Source | Standing for NW-082 |
|---|---|
| CDL slices via `scripts/query_cdl.py` | No accepted `tenant` or `workspace` decision was found. Relevant authority remains event truth, closed envelope, assignment-derived access, sync/access equivalence, projected authority, fixed scope mechanisms, configuration boundaries, sensitivity, and rebuildable projections. |
| `contracts/` | Event envelope, sync protocol, config package, shape format, flag catalog, and assignment payload schemas remain unchanged and authoritative for process and wire surfaces. |
| Decision-anchor layer | Multi-tenancy is architecture-sensitive pressure because it can change sync/access behavior, authority boundaries, storage partitioning, local storage compatibility, and contracts. Route before implementation. |
| BAR and NW backlog | BAR-001 through BAR-015 and BAR-104 are accepted. NW-067, NW-068, NW-069, NW-070, NW-074, NW-079, NW-080, and NW-081 are accepted inputs. NW-071 through NW-073 remain candidates; NW-044, NW-045, NW-053, and NW-054 remain future-decision routes. |
| Accepted specs | Configuration, expression, assignment/scope/admin, production auth principal binding, and production web admin auth/authority have accepted platform specs. No accepted product specs exist yet under `docs/specifications/product/`. |
| Operations docs | The reference deployment policy, runbook, rehearsal plan, NW-067 composite rehearsal record, and R12 rerun are accepted for a synthetic reference environment only. |

### Accepted Standing

| Area | Current accepted standing | Product Candidate 1 meaning |
|---|---|---|
| Server/runtime kernel | Append-only event store, envelope validation, idempotent sync, assignment-derived access, subject-history backfill, config package delivery, expression validation, conflict flags, pattern projections, production auth principal binding, and assignment-admin command capability are accepted. | PC1 may build product/admin flows over these behaviors without changing contracts or primitives. |
| Mobile runtime | Actor sessions, per-actor local partitions, sync states, offline capture handoff, work readiness, and append-only correction UX are accepted by NW-055 and NW-059 through NW-062. | PC1 may productize labels/navigation over accepted behavior, but mobile OIDC/login and retention/security still need successors. |
| Web/admin authority | NW-079 accepts OIDC/JWKS browser login, server-managed session, explicit-principal-bound admin actor context, command-specific authority, audit posture, and dev-console containment. | PC1 can implement login/session and command-gated admin surfaces one row at a time. Current `/admin` remains development-only. |
| Operations | NW-063 through NW-067 and NW-075 through NW-081 accept policy, tooling, runbook, rehearsal plan, backup/PITR adapter, credential/JWKS rotation, monitoring adapter, RPO refresh, token path, and R12 synthetic rehearsal. | PC1 may rely on synthetic reference evidence for a lab/reference candidate, not for real production. |
| Configuration and assignment | Accepted specs allow config package authoring/publish behavior, activity roles, expression rules, assignment create/end command capability, and same-assignment containment. | PC1 can productize S23-style setup and assignment admin after admin auth/session implementation. |

### Non-Binding Or Deferred Standing

| Surface | Standing | Boundary |
|---|---|---|
| Product Candidate 1 scope in this file | Non-binding routing recommendation | Must become a product spec under `docs/specifications/product/` before user-visible behavior is accepted. |
| Multi-tenancy analysis | Non-binding exploration input | Must become a successor product/platform/architecture decision before tenant/workspace terms or isolation model are binding. |
| Product/admin forward plan | Non-authoritative routing artifact | Useful for sequencing; it contains a stale NW-067 production-readiness blocker statement because NW-067 is now accepted for synthetic rehearsal only. Do not rely on that stale line as current standing. |
| NW-071 | Candidate platform-spec extraction | Required before work relies normatively on shared-device/local-state details, especially tenant/deployment-aware mobile partitioning. |
| NW-072 | Candidate platform-spec extraction | Required before conflict UI, flag reporting, batch handling, resolver reassignment, auto-resolution, or flag/resolvability changes. |
| NW-073 | Candidate platform-spec extraction | Required before pattern traversal/reporting, pattern product APIs, or workflow projection changes depend on accepted behavior. |
| NW-044 | Future decision | Reporting aggregation, broad read APIs, warehouses, export/import, and product reporting remain out of PC1 unless promoted. |
| NW-045 | Future decision | Conflict automation, batch resolution, pending-match queues, and auto-resolution remain out. |
| NW-053 | Future decision | New subject/query/custom scope mechanisms remain out. |
| NW-054 | Future decision | Expiry, decommissioning, local encryption, redaction, no-local-retention, sealed recovery, and broader retention/security remain out. |
| Real production | Not accepted | Provider, region, jurisdiction, data classification, real notification/login, organizational review, support/continuity, and fresh environment evidence remain required. |

### Scratch Ledger

| Ledger | Current entries |
|---|---|
| Accepted standing | BAR-001 through BAR-015; BAR-104; NW-067 synthetic rehearsal; NW-068 through NW-070 platform specs; NW-074 hygiene; NW-079 admin-auth spec; NW-075 through NW-081 ops adapters/fixes; mobile NW-055 and NW-059 through NW-062. |
| Non-binding/deferred standing | This artifact; multi-tenancy analysis; product-admin forward plan; NW-071 through NW-073 candidates; NW-044, NW-045, NW-053, NW-054 future decisions; real production unapproved. |
| Open decisions | Product Candidate 1 product spec; tenant/organization/workspace terminology; managed-deployment versus tenant-aware internals; identity/membership model; mobile OIDC/session; retention/security; real-production approval. |
| Implementation candidates | Production web admin login/session; admin command gate; S23 config setup workflow; mobile vocabulary/navigation; mobile OIDC/login; assignment admin web UX; resolver-visible single-flag review; singleton tenant/workspace scaffold only after decisions. |
| Stop conditions | Code implementation, contract/schema/envelope changes, production-readiness claim, tenant/workspace architecture only in this artifact, generic admin role collapse, location/org-unit/assignment/IdP claim used as tenant isolation, pooled storage before isolation tests. |

## 3. Product Candidate 1 Scope

### Recommended Milestone Boundary

Product Candidate 1 should be a tenant-contained operational candidate, not a
full SaaS launch and not real production by default.

The milestone is reached when Datarun can demonstrate, behind explicit
accepted routes, a single organization/default workspace running a coherent
operator and field-user loop:

1. A production-authenticated web admin shell exists with OIDC/JWKS browser
   login, server session, CSRF/logout/expiry, principal-binding revalidation,
   admin actor context, and login/session audit.
2. Web admin command gates enforce `web_admin.access`,
   `web_admin.read_scoped`, and `config_admin.*` policy without IdP claim
   authority or fixed dev actor authority.
3. S23 setup/config flow is productized over accepted config package, shape,
   expression, validation, readiness-review, approval, and publish behavior.
4. Assignment create/end UX uses existing `assignment_admin.create` and
   `assignment_admin.end` plus same-assignment containment.
5. Mobile field workflow has product-grade language and navigation over
   saved-local, waiting, syncing, synced, failed, retry, missing setup/forms,
   missing assignment, ready-to-capture, correction, warning, freshness, and
   handoff states.
6. Mobile login/session is either explicitly out of the first internal
   candidate or selected by a separate mobile OIDC/Keycloak decision and
   implementation before external users rely on it.
7. Synthetic reference deployment evidence remains current for lab/reference
   demonstrations; real users or real organizational data require the separate
   real-production approval route.

### Explicitly In Scope After Successor Decisions

| Scope item | Route needed before acceptance |
|---|---|
| Product Candidate 1 user-visible terminology, actors, journeys, states, and acceptance criteria | Product spec under `docs/specifications/product/`, indexed from the product README. |
| Tenant/organization/workspace vocabulary and negative boundaries | Product/platform/architecture decision successor under the decision-anchor route. |
| Production web admin login/session and protected shell | Implementation row under accepted NW-079 spec. |
| Admin command policy and scoped shell gate | Implementation row after login/session. |
| S23 config setup workflow | Product spec/design plus implementation rows over accepted config/expression specs. |
| Assignment administration UX | Product spec/design plus implementation over accepted assignment spec. |
| Mobile operational vocabulary/navigation | Product design/implementation over accepted mobile slices; coordinate NW-071 if shared-device/local-state semantics become normative. |
| Mobile OIDC/login and token lifecycle | Product/platform/security decision and implementation; not implied by NW-079. |
| Real-production go/no-go | Operations policy/runbook/rehearsal successor with provider, region, data, support, notification, and compliance selections. |

### Explicitly Out Of Scope

- Pooled multi-tenant storage.
- Cross-tenant collaboration, shared workspaces, cross-tenant subject identity,
  tenant-to-tenant sharing, and global fleet analytics.
- Tenant fields in the event envelope or new envelope `type` values.
- Location hierarchy, org unit membership, assignment scope, UI-selected
  organization, or IdP claims as tenant isolation.
- Online production principal-binding admin APIs/UI.
- Reporting dashboards, reporting APIs, warehouses, export/import, and broad
  aggregate views.
- Conflict batch handling, pending-match queues, resolver reassignment,
  auto-resolution, or non-designated resolver authority.
- New scope mechanisms, deployer scope scripts, query-as-config authority, or
  custom containment logic.
- Entity lifecycle.
- Retention/security promises beyond accepted selective retention and actor
  partitions.
- Real production readiness without the operations approval route.

## 4. Multi-Tenancy Routing

### Current Position

The current platform is effectively single-tenant at deployment level:
one event stream, one config package stream, one location tree, one
principal-binding namespace, one actor namespace, and one sync/access universe.
Assignment scope is mature inside that universe, but it is not a tenant
isolation model.

Target vocabulary to route, not yet accept:

| Concept | Recommended meaning |
|---|---|
| Account | Commercial/billing/contract entity; may own one or more tenants later. |
| Tenant | Hard security, administration, entitlement, and data-isolation boundary. |
| Organization | Likely customer-facing name for tenant at first, pending product confirmation. |
| Workspace | Operational container for scenarios/workflows, config, locations, subjects, events, assignments, projections, and scoped reporting inside a tenant. |
| Org unit/team/department | In-tenant management grouping; not a data-isolation boundary. |
| Location hierarchy | In-workspace operational geography/resource hierarchy used by assignment scope and reporting. |
| Assignment | Event-derived operational authority inside a workspace. |
| Membership | Relationship allowing entry/admin context for a tenant or workspace; not enough to see operational data without assignments or explicit admin capabilities. |

### Classification

| Pressure | Classification | Durable route |
|---|---|---|
| Tenant/organization/workspace naming and negative boundaries | Product/problem evidence gap plus platform-spec detail gap; architecture decision gap if made a structural isolation primitive | Successor decision artifact or product/platform specs, indexed if accepted. |
| Managed single-tenant deployment as first SaaS posture | Operational policy gap plus architecture decision gap if it defines control-plane authority | Operations policy/control-plane decision successor. |
| Tenant-aware auth and membership | Architecture decision gap plus platform/security spec | Decide `(issuer, subject, tenant_id) -> actor_id` versus principal/platform-user membership. |
| Tenant/workspace sync and config context | Architecture decision gap plus contract gap | Update `contracts/sync-protocol.md` only after accepted decision; do not change envelope first. |
| Tenant-aware mobile local partitioning | Architecture decision gap plus implementation/tooling gap | Select NW-071 first, then decide partition keys including deployment/tenant/workspace/actor. |
| Pooled storage | Architecture decision gap plus implementation/tooling gap | Only after tenant context, storage metadata, query gates, mobile partitions, and isolation tests exist. |

### Decision Sequence

1. Decide vocabulary and negative boundaries.
2. Decide first isolation posture: managed single-tenant deployment or bridge
   isolation before pooled rows.
3. Decide whether Product Candidate 1 exposes workspace in product UI or keeps
   it as an internal/default-workspace concept.
4. Decide identity/membership and tenant selection.
5. Decide process boundary for tenant/workspace context in auth, sync, config,
   assignment, admin, and mobile setup.
6. Decide tenant/deployment-aware mobile local partitioning.
7. Add singleton tenant/workspace scaffold only after the above decisions.
8. Build tenant isolation test harness before pooled storage.
9. Consider pooled storage only after isolation tests pass.
10. Defer cross-tenant collaboration, global analytics, and parent-child
    tenants to separate future routes.

### Forbidden Shortcuts

- Do not use `locations` as tenants.
- Do not use org units, teams, departments, or IdP groups as tenants.
- Do not use assignment scope as tenant isolation.
- Do not sync broad data and hide it in UI; sync filtering remains the access
  boundary.
- Do not put tenant/workspace in the event envelope without successor
  architecture and contract authority.
- Do not create one generic admin role that covers tenant admin, workspace
  admin, config admin, assignment admin, activity roles, resolver authority,
  deployment owner, and support access.

## 5. Work Sequence

| Order | ID placeholder | Title | Type | Priority | Depends on | Required input files | Intended durable output home | Implementation impact | Verification/evidence | Successor tasks | Stop conditions |
|---:|---|---|---|---|---|---|---|---|---|---|---|
| 1 | NW-083 | Decide tenant/workspace vocabulary and managed-isolation boundary | `product_platform_decision` / `architecture_decision_gap` | P1 | NW-082; GAP-PRODUCT-01; accepted NW-069/NW-070/NW-079; multi-tenancy analysis | `docs/agent-working-surface/artifacts/2026-06-18-multi-tenancy-architecture-analysis.md`; decision-anchor layer; accepted assignment/auth/admin specs; relevant CDL slices; contracts envelope/sync/config | CDL successor or explicitly selected architecture decision if structural; product/platform specs if user-facing or platform-detail terms are accepted | None directly | `git diff --check`; owner acceptance for terms; explicit no-code/no-contract diff | PC1 product spec; managed-deployment control-plane decision; tenant-aware auth decision | Implementation starts; terms accepted only in artifact; location/org unit/assignment/IdP claim used as tenant isolation; tenant envelope fields proposed |
| 2 | NW-084 | Specify Product Candidate 1 user-visible milestone | `product_spec` | P1 | NW-082; NW-083; NW-047; NW-056; NW-079; accepted mobile NW-059 through NW-062 | Product/admin forward plan; NW-056 map; product README; accepted platform specs; ops policy/runbook/rehearsals | `docs/specifications/product/product-candidate-1.md` plus product README index | None directly | `git diff --check`; product owner/steward acceptance; acceptance criteria trace | Admin implementation rows; mobile UX rows; ops go/no-go route | Product behavior left only in artifact/backlog; real production claimed; deferred reporting/retention/conflict automation absorbed |
| 3 | NW-071 | Extract shared-device session and local-state durable behavior | `platform_spec` | P2 | Existing NW-071 candidate; NW-052/NW-055; BAR-104/BAR-106 | NW-071 prompt when promoted; mobile actor/session code paths; module interfaces; accepted auth spec | `docs/specifications/platform/` plus platform README index | None directly | `git diff --check`; no runtime/schema diff | Mobile login/session; tenant-aware local partitioning | Expiry, decommissioning, encryption, token retention, or no-local-retention decided here instead of NW-054 |
| 4 | NW-085 | Decide mobile OIDC/login and token lifecycle | `product_platform_decision` / security | P1 | NW-070; NW-071 if local-state semantics used; NW-084; NW-054 if retention/security claims included | Production auth spec; NW-079 non-goals; mobile actor/session surfaces; sync protocol; operations token path evidence | Product spec and/or platform/security spec; possible future contract route if API shape changes | None directly | `git diff --check`; explicit tests required for successor implementation | Mobile login implementation; shared-device switch UX | Mobile-selected actor, JWT `actor_id`, IdP claims, or provider groups become authority; retention promises smuggled in |
| 5 | NW-086 | Implement production web admin login and session boundary | `implementation` | P1 | NW-079; NW-083 if admin context names tenant/workspace; NW-084 if product shell language is in scope | Production web admin spec; production auth spec; module interfaces; server admin/auth code paths | Code/tests; possible implementation notes only | Adds browser login/session/protected shell, CSRF, logout, expiry, revalidation, login/session audit | Focused server auth/session/security tests; production dev-surface containment tests; `git diff --check`; full relevant server suite as scoped | Admin command gate; S23 config workflow | Adds config UI expansion, online binding admin, IdP claim authority, dev fixed actor authority, or contract/schema changes |
| 6 | NW-087 | Implement admin command capability and scoped shell gate | `implementation` | P1 | NW-086; NW-079; NW-069/NW-070 | Production web admin spec; assignment spec; config specs; server admin/config controllers | Code/tests | Adds `web_admin.access`, `web_admin.read_scoped`, `config_admin.*` policy evaluation and denial behavior | Focused authorization/controller tests; claim non-authority tests; `git diff --check` | S23 config workflow; assignment admin UX; scoped observation views | Generic root/admin role; command policy delivered as config package; assignment containment bypass |
| 7 | NW-088 | Productize S23 config setup workflow | `product_design_then_implementation` | P2 | NW-084; NW-087; NW-068 expression/config specs; BAR-010/BAR-011 | Product spec; config/expression specs; contracts config/shape schemas; existing config admin code | Product spec update if needed; code/tests | Candidate authoring, validation, readiness review, approval, publish, and audit over accepted config behavior | Controller/UI tests; config validation/package tests; no contract diff unless routed | Assignment admin UX; mobile setup flow | New config-package keys, deployer scripts, dynamic queries, custom state machines, trigger execution, or per-device config variants |
| 8 | NW-089 | Productize mobile operational vocabulary and navigation | `product_design_then_implementation` | P2 | NW-084; NW-047; NW-055; NW-059 through NW-062; NW-071 if local-state semantics are normative | Mobile screens/tests; product spec; operational UX companion; accepted mobile rows | Product spec update if behavior/language accepted; mobile code/tests | UI copy/navigation/state polish over accepted sync/readiness/capture/correction/handoff behavior | Focused Flutter widget/state tests; full mobile suite when shared state/navigation touched; `git diff --check` | Mobile OIDC login implementation if selected | Advisory warnings become rejection; stored state/sync semantics change; retention/offboarding claims added |
| 9 | NW-090 | Productize assignment administration in web admin | `product_design_then_implementation` | P2 | NW-087; NW-069; NW-050; NW-084 | Assignment spec; production web admin spec; assignment API/tests | Product spec update if needed; code/tests | Web create/end UX over accepted command capability and containment | Assignment containment/command tests; controller/UI tests; `git diff --check` | Scoped setup operations | Root bypass; new scope mechanisms; combining command capability and scope across assignments |
| 10 | NW-091 | Extract conflict flag/resolution durable behavior before conflict UI | `platform_spec` | P2 | Existing NW-072 candidate; BAR-006/BAR-013; `contracts/flag-catalog.md` | NW-072 prompt; flag catalog; conflict APIs/tests; module interfaces | `docs/specifications/platform/` plus platform README index | None directly | `git diff --check`; no runtime/schema diff | Resolver-visible single-flag review UX | Batch resolution, auto-resolution, resolver reassignment, or flag catalog changes included |
| 11 | NW-092 | Productize resolver-visible single-flag review | `product_design_then_implementation` | P3 | NW-091; NW-087; NW-079 | Conflict/flag spec; production web admin spec; flag catalog | Product spec update if needed; code/tests | Resolver-visible list/detail/single resolution only | Conflict/resolver equality tests; controller/UI tests; `git diff --check` | Batch handling route through NW-045 only if needed | Non-designated resolver clears flags; batch/auto/reassignment sneaks in |
| 12 | NW-093 | Decide real-production approval package | `operational_policy` / `rehearsal_record` | P1 when real users/data are proposed | NW-067; NW-075 through NW-081; NW-084; deployment-owner input | Ops policy/runbook/rehearsal docs; selected provider/region/data/support/login evidence | `docs/operations/policies/`, `docs/operations/runbooks/`, and `docs/operations/rehearsals/` | None unless separate tooling gaps are routed | Provider-specific rehearsal; fresh backup/RPO/alert/token checks; owner go/no-go | Real production claimed from lab evidence; unknown data classification/jurisdiction/login/support; synthetic webhook or synthetic principal reused as production proof |
| 13 | NW-094 | Decide managed-deployment SaaS control-plane boundary | `architecture_decision_gap` / `operational_policy_gap` | P1 if multi-customer SaaS is selected | NW-083; NW-093 if real deployment context exists | Multi-tenancy analysis; ops policy/runbook; production auth spec | Architecture decision and/or ops policy | None directly | `git diff --check`; explicit no event/config schema changes unless routed | Tenant-aware auth; deployment registry tooling | Control plane writes Datarun event truth; deployment owner becomes platform authority |
| 14 | NW-095 | Decide tenant-aware identity, membership, and actor model | `architecture_decision_gap` / `platform_spec` | P1 | NW-083; NW-094; NW-070 | Production auth spec; sync contract; admin spec; multi-tenancy analysis | Architecture decision plus platform/security spec; contract successor if API changes | None directly | `git diff --check`; model examples and negative tests specified | Tenant-aware auth implementation; mobile setup changes | IdP groups/claims authority; ambiguous multi-tenant principal binding; UI-selected tenant as unchecked authority |
| 15 | NW-096 | Decide tenant data isolation and sync/config partitioning strategy | `architecture_decision_gap` / `contract_gap` | P1 before tenant-aware internals | NW-083; NW-095; NW-069/NW-070; contracts | Envelope, sync, config package contracts; module interfaces; multi-tenancy analysis | Architecture decision plus `contracts/sync-protocol.md` successor if process context changes | None directly | Contract diff checks; isolation test plan | Singleton scaffold; isolation harness | Pooled rows before context/tests; tenant fields in envelope; missing tenant filter risk ignored |
| 16 | NW-097 | Implement singleton tenant/default workspace scaffold | `implementation_tooling` | P2 | NW-083; NW-095/NW-096 if internals selected | Accepted tenant decisions; server/mobile module paths; migrations if routed | Code/tests; implementation docs only if needed | Adds one default tenant/workspace context without user-visible multi-tenancy | Full affected server/mobile tests; tenant no-op compatibility tests; `git diff --check` | Tenant isolation harness | User-visible multi-workspace claims; pooled storage; contract drift without accepted route |
| 17 | NW-098 | Build tenant isolation test harness | `implementation_tooling` | P1 before pooling | NW-096; NW-097 | Sync/config/auth/assignment/conflict/projection/admin code paths | Code/tests | Adds isolation tests across tenant/workspace contexts | Tests proving no cross-tenant sync/config/auth/assignment/conflict/projection/admin leakage | Optional pooled storage decision | Any leak remains unblocked; tests only cover UI hiding instead of data access |

## 6. Risk-Managed Routing

| Risk | Severity | Trigger | Mitigation | Required route | Blocks Product Candidate 1 |
|---|---|---|---|---|---|
| Non-binding artifact treated as accepted product/platform behavior | A | Product or implementation uses this file as authority | Promote product/platform/architecture successors before behavior depends on it | NW-083 and NW-084 | Yes, for accepted PC1 behavior |
| Synthetic rehearsal overread as production approval | A | NW-067/R12 evidence used for real users or real data | Keep real-production approval route separate and mandatory | NW-093 | Blocks real production; not a synthetic PC1 demo |
| Tenant isolation implemented with location, org unit, assignment, UI org, or IdP claim | A | Shortcut proposed in design/code | Stop and route to tenant/workspace decision | NW-083/NW-096 | Yes |
| Pooled multi-tenant storage starts before tenant context and isolation tests | A | `tenant_id` columns or pooled filters added ad hoc | Decide partitioning, scaffold, and harness first | NW-096 through NW-098 | Yes for multi-tenant PC1 |
| Tenant/workspace fields added to event envelope | A | Envelope/schema change proposed | Route CDL/contract successor; first model should keep envelope unchanged | Architecture/contract route | Yes |
| Generic admin role collapses authority layers | A | Tenant admin, workspace admin, config admin, assignment admin, resolver, support, and deployment owner become one role | Preserve command-specific capabilities and existing resolver/assignment boundaries | NW-079 successors; NW-083 if tenant admin added | Yes for admin work |
| Production web admin built from development console/fixed actor | A | `/admin` or `/admin/config` exposed in production without NW-079 implementation | Implement session, command gate, CSRF, audit, and dev containment first | NW-086/NW-087 | Yes |
| Mobile OIDC/login gap ignored | B | External users expected to use raw bearer setup | Decide and implement mobile login or scope PC1 as internal/synthetic | NW-085 | Blocks external-user PC1 |
| Shared-device/local-state details become normative from IDR prose | B | Mobile tenant/session work relies on unextracted behavior | Select NW-071 first | NW-071 | Blocks tenant-aware mobile work |
| Retention/security promises added through UX | A | Offboarding, no-local-retention, local encryption, expiry, decommissioning, or sealed recovery appears in product copy | Route NW-054 before product or implementation | NW-054 | Yes if such claims are in PC1 |
| Reporting/export absorbed into candidate | B | Dashboard/API/export/warehouse added to PC1 | Route NW-044 first | NW-044 | Yes for reporting surfaces |
| Conflict batch/automation absorbed into candidate | B | Batch resolution, pending-match queues, auto-resolution, or reassignment added | Route NW-072 then NW-045 as needed | NW-072/NW-045 | Yes for conflict expansion |
| Contract drift from tenant/workspace context | A | Sync/config/admin API context changes without contract update | Decide contract boundary first; update contracts only in successor | NW-096 plus contract route | Yes |
| Product-admin forward plan stale NW-067 wording misleads sequencing | C | Agent treats NW-067 as blocked or as real-production accepted | Use backlog/ops index as current standing: synthetic accepted, real production unapproved | Artifact note or hygiene route if selected | No, but can misroute |
| Real notification/login/support path missing | B | Production approval tries to reuse lab webhook or synthetic token path | Select production notification, token/login, and support/continuity evidence | NW-093 | Blocks real production |

## 7. Recommended First Actionable NW

Recommended first successor:

```text
NW-083: Decide tenant/workspace vocabulary and managed-isolation boundary
Type: product_platform_decision / architecture_decision_gap
Priority: P1
Depends on: NW-082; GAP-PRODUCT-01; accepted NW-069, NW-070, NW-079; multi-tenancy analysis
Output: durable decision/spec route that defines tenant, organization, workspace,
org unit, location, membership, actor, assignment, entitlement, negative
boundaries, and first isolation posture.
```

Why first: Product Candidate 1 cannot safely use "organization" or "workspace"
as product language, nor route multi-customer deployment, until tenant
isolation and negative boundaries are explicit. This does not block all
implementation forever, but it should precede product copy, tenant-aware
admin/session work, control-plane work, and any tenant/storage/API change.

Acceptance boundary for NW-083:

- durable output is in the correct home, not only in `artifacts/`;
- no code, schema, contract, envelope, sync, config package, auth, mobile
  local-store, or production ops change;
- explicit classification states whether CDL successor, platform spec,
  product spec, operations policy, or a combination is selected;
- if the result is still non-binding, it must say so and route the next
  durable successor;
- owner/product confirmation is required for customer-facing terminology.

## 8. Follow-Up NW Candidates

| Candidate | Purpose | Promote when |
|---|---|---|
| Product Candidate 1 product specification | Accept user-visible scope, actors, language, journeys, and product acceptance criteria. | NW-083 settles terminology or explicitly chooses default wording. |
| Production web admin login/session implementation | Build the minimal protected shell from NW-079. | Product Candidate 1 implementation begins. |
| Admin command gate implementation | Add `web_admin.*` and `config_admin.*` server policy enforcement. | Login/session shell exists. |
| Mobile OIDC/login and token lifecycle decision | Decide provider login, refresh/logout, secure storage, shared-device login UX, and actor alignment. | External field-user PC1 is selected or raw bearer setup becomes unacceptable. |
| Mobile operational vocabulary/navigation implementation | Productize existing mobile sync/readiness/capture/correction states. | Product spec accepts language and no login/retention changes are bundled. |
| S23 config setup workflow | Productize author/validate/review/approve/publish. | Admin session and command gate exist. |
| Assignment admin web UX | Productize create/end over accepted containment. | Admin command gate exists. |
| NW-071 shared-device/local-state spec | Make current behavior durable before tenant-aware mobile or shared-device work. | Any mobile/session/tenant local partition work relies on it. |
| NW-072 conflict flag/resolution spec | Make current flag/resolver behavior durable. | Conflict UI, flag reporting, or resolver-visible review enters PC1. |
| NW-073 pattern registry/projection spec | Make pattern/projection behavior durable. | Pattern product/API/progress work enters PC1. |
| Real-production approval package | Decide provider, region, data classification, support, notifications, login, and go/no-go. | Real users or real organizational data are proposed. |
| Managed-deployment control plane boundary | Decide how multiple isolated customer deployments are created, owned, operated, and billed. | SaaS multi-customer posture is selected. |
| Tenant-aware identity/membership model | Support one principal across multiple tenants/workspaces. | Multi-tenant login or tenant selection is selected. |
| Tenant isolation and sync/config partitioning | Decide storage/process context before tenant-aware internals. | Moving beyond managed single-tenant/bridge isolation. |
| Tenant isolation test harness | Prove no cross-tenant leaks. | Before any pooled storage or tenant-aware runtime acceptance. |

## 9. Files Likely To Change

### NW-082 Slice

- `docs/agent-working-surface/artifacts/NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md`
- `docs/agent-working-surface/platform-next-work-backlog.md` for the NW-082 trace only

### Likely Successor Documentation

- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/prompts/NW-083-*.md` and later successor prompts
- `docs/specifications/product/README.md`
- `docs/specifications/product/product-candidate-1.md`
- `docs/specifications/platform/README.md`
- Future platform specs for tenant/workspace boundary, mobile auth/session, shared-device/local-state, conflict/flag, and pattern/projection behavior
- `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and new rehearsal/go-no-go records only for real-production approval
- CDL successor or explicitly routed architecture decision artifact if tenant/workspace becomes structural authority
- `contracts/sync-protocol.md` if tenant/workspace context changes process-boundary sync/config/auth/admin behavior
- `contracts/config-package.schema.json` only if package body context changes; avoid in the first route unless needed

### Likely Successor Implementation Files

- Server auth/session/admin surfaces under `server/src/main/java/dev/datarun/server/authorization/`, `server/src/main/java/dev/datarun/server/admin/`, and `server/src/main/java/dev/datarun/server/config/`
- Server tests under `server/src/test/java/dev/datarun/server/authorization/`, `server/src/test/java/dev/datarun/server/config/`, `server/src/test/java/dev/datarun/server/admin/`, and related integration suites
- Mobile setup/session/sync/screens under `mobile/lib/data/`, `mobile/lib/presentation/`, and matching `mobile/test/`
- Migrations only after accepted decisions require new storage
- Deployment/reference tooling only after an operations successor selects concrete production adapters

## 10. Final Recommendation

Accept NW-082 only as this non-binding routing artifact. Do not treat this file
as accepted product scope, tenant architecture, contract authority, or
production readiness.

The Product Candidate 1 route should proceed as:

```text
tenant/workspace vocabulary and managed-isolation decision
-> Product Candidate 1 product specification
-> production web admin login/session
-> admin command gate
-> S23 setup/config and assignment admin productization
-> mobile vocabulary/navigation and mobile login/session as separately routed
-> real-production approval only if real users or real data are proposed
```

Multi-tenancy should proceed as managed single-tenant deployment or bridge
isolation first. Tenant is the hard isolation boundary, organization is the
likely customer-facing term, workspace is the operational container, and
assignments remain workspace-internal authorization. Locations, org units,
IdP claims, UI-selected organizations, and assignment scopes must not become
tenant isolation. Pooled storage, cross-tenant collaboration, global analytics,
tenant-aware membership, and tenant-aware mobile partitions remain successor
work.

The next concrete action is NW-083: decide tenant/workspace vocabulary and the
managed-isolation boundary in the correct durable home before product copy,
tenant-aware implementation, or multi-customer deployment work depends on it.
