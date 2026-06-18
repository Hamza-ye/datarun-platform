# Product Candidate 1

Status: accepted
Document type: product_spec
Owner: product steward
Source: NW-084 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-084-specify-product-candidate-1-user-visible-milestone.md`
Authority: accepted only for user-visible Product Candidate 1 scope, language, journeys, exclusions, and acceptance criteria; operates within NW-083 managed-isolation routing, accepted platform specifications, contracts, BAR standing, operations documents, and the NW backlog
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/agent-working-surface/artifacts/NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md`; `docs/agent-working-surface/artifacts/NW-083-tenant-workspace-vocabulary-and-managed-isolation-boundary.md`; `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`; `docs/agent-working-surface/artifacts/product-admin-surface-forward-plan.md`; `docs/agent-working-surface/operational-ux-layering-companion.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md`; `docs/specifications/platform/production-auth-principal-binding.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/configuration-package-and-shapes.md`; `docs/specifications/platform/expression-language.md`

## Purpose

Product Candidate 1 is the first coherent Datarun product experience. It is a
candidate milestone for one customer-facing Organization running one managed
single-tenant Datarun deployment with one internal/default Workspace.

This specification accepts the user-visible scope, language, journeys,
exclusions, and product acceptance criteria for that candidate. It does not
implement runtime behavior, web UI, mobile UI, schemas, contracts, migrations,
tenant-aware internals, operations evidence, or real-production approval.

Product Candidate 1 is not pooled SaaS and not a real-production approval. It
can be demonstrated with synthetic or non-sensitive candidate evidence. Real
users or real organizational data require the separate real-production route.

## Product Boundary

The candidate is a one-Organization operational loop:

```text
Organization setup
-> authenticated web admin shell
-> setup/config author, validate, review, approve, publish
-> assignment of responsibilities
-> mobile field work, sync, correction, and handoff
-> optional resolver-visible single-flag review after its own platform route
```

The user-facing product may say `Organization`. It should not expose `Tenant`
or multiple `Workspaces` in Product Candidate 1 copy or navigation. Workspace
is an internal/default container for the candidate.

Managed deployment isolation carries the first organization boundary. The app
does not become tenant-aware in Product Candidate 1. Tenant-aware auth,
workspace-scoped config, tenant sync context, mobile tenant partitions, pooled
`tenant_id` predicates, and event-envelope changes remain successor routes.

## Actors And Contexts

Persona labels help describe work. They do not grant platform authority.
Server-resolved actor context, assignment scope, command capability, resolver
rules, and operations policy remain the accepted authority sources.

| Actor/context | Product role in Product Candidate 1 | Authority boundary |
|---|---|---|
| Organization operator | Owns the candidate Organization from a product perspective. | Managed deployment and operations policy; not a runtime tenant authority. |
| Setup owner / config author | Defines forms, activities, warning rules, severity/sensitivity declarations, and setup changes. | Future web admin session plus `config_admin.author`; accepted config and expression specs. |
| Setup reviewer | Reviews a validated setup for operational readiness before approval. | `config_admin.readiness_review`; review is audit context, not workflow-state truth. |
| Setup approver / publisher | Approves a validated setup hash and publishes one atomic package. | `config_admin.approve` and `config_admin.publish`; publish must revalidate. |
| Assignment coordinator | Creates and ends responsibilities for field users. | Existing `assignment_admin.create` / `assignment_admin.end` plus same-assignment containment. |
| Field user | Gets assigned work, captures activity entries, sees sync/readiness states, appends corrections, and continues handoff context. | Server-resolved actor session, assignment-derived access, accepted mobile actor partition behavior. |
| Reviewer / resolver | Inspects and resolves one designated attention item or flag when routed. | Existing designated-resolver equality; conflict UI needs the conflict durable-surface route first. |
| Scoped observer | Reads scoped operational views without mutation. | Existing assignment-derived access or accepted non-mutating admin read capability. |
| Support operator | May assist with scoped, non-mutating inspection when explicitly authorized. | No broad audit/support read authority in PC1; broad support reads need a future route. |
| Deployment owner | Operates infrastructure, provider configuration, manifests, backup, monitoring, and evidence. | Operations policy/runbook/rehearsal surfaces; not product UI authority by title. |

## User-Visible Terminology

| Product term | Product Candidate 1 meaning | Must not become |
|---|---|---|
| Organization | The customer-facing environment for one managed deployment. | Event field, sync context, UI-selected authority, or pooled tenant selector. |
| Setup | The deployer-authored forms, activities, role-action maps, expressions, severity/sensitivity declarations, and accepted package content. | Deployer code, scripts, trigger engine, custom scope logic, or mutable runtime truth. |
| Draft setup | Candidate setup being edited before publication. | A config package visible to devices. |
| Validated setup | Candidate setup that passed machine validation. | Human approval or production deployment approval. |
| Readiness review | Human check that a validated setup is operationally ready for field rollout. | Workflow-state truth, reporting approval, or real-production approval. |
| Approved setup | A validated candidate hash approved for publication. | Permission to publish changed content without revalidation. |
| Published setup | The latest atomic configuration package delivered to devices. | Partial package, per-device variant, or tenant/workspace-scoped config. |
| Responsibility | Work authority assigned to a person within the Organization. | IdP group, UI permission, activity role-action, or tenant isolation. |
| Assignment | Product-visible synonym for a responsibility where precise wording helps. | New scope mechanism or request-body actor authority. |
| Route / coverage | Friendly wording for assigned geography, subject list, or activity coverage. | Custom scope, query-as-config authority, or location-as-tenant model. |
| Work item | Something a field user can inspect, continue, capture, or correct. | Task table, event type, workflow truth, or sync scope. |
| Activity entry | A submitted configured record. | Mutable form state or overwritten record. |
| Correction | A new append-only activity entry that corrects prior captured information. | Event mutation, durable correction linkage, or envelope metadata change. |
| Handoff | Continuity of work context across assignments, actors, devices, or custody transitions. | Broad historical pull, watermark rewrite, or cross-actor local data access. |
| Attention item | A warning, flag, discrepancy, stale condition, or unresolved issue to inspect. | Auto-resolution, propagated root flag, or non-designated resolver authority. |
| Pending review | A record, flag, discrepancy, or transition needing human inspection. | Batch resolution, resolver reassignment, or canonical mutable status. |
| Saved locally | Captured on device but not yet synced. | Server acceptance or cross-device visibility. |
| Waiting to sync | Pending local work exists and sync has not completed. | A failed or blocked state by itself. |
| Syncing | Device is attempting push/pull/config sync. | Production connectivity guarantee. |
| Synced | Last sync completed for the actor/device context shown. | Proof that all other actors/devices are current. |
| Needs retry | Last sync attempt failed and the user can retry. | Server rejection of saved local events unless the server says so. |
| Get work | User action to sync and refresh setup, assignments, and visible work. | New authority, broad audit pull, or tenant selection. |
| Missing setup/forms | Device lacks usable current setup or forms. | Runtime config mutation or production outage classification. |
| Missing assignment | The actor has no locally visible assignment for the selected work. | Hard mobile rejection of structurally valid events. |
| Ready to capture | Device has enough local setup and work context to start configured capture. | Guarantee that server authority will remain current offline. |

## In-Scope Journeys

### 1. Enter The Organization

The product presents one Organization. It may show the Organization name in
web and mobile copy. It must not present Tenant, workspace selection, pooled
SaaS tenant switching, cross-tenant collaboration, or tenant-aware internals.

### 2. Sign In To Web Admin

The web admin experience starts with OIDC/JWKS browser login and a
server-managed session. The user sees a protected admin shell only after the
server resolves the provider principal through explicit principal binding.

The first implementation may be a minimal protected shell. Config authoring,
assignment administration, and review actions remain gated by later
command-specific rows.

### 3. Prepare Setup

Setup owners can work toward a published setup using these product states:

1. Draft setup is edited.
2. Validation checks the full candidate.
3. Readiness review records whether the candidate is operationally ready.
4. Approval binds to the validated candidate content.
5. Publish revalidates and emits one atomic package.

Invalid setup must not publish. Validation failures should be explained as
setup issues to fix, not as runtime event rejection. Changing candidate content
after approval requires validation and approval again.

### 4. Assign Responsibilities

Assignment coordinators can create and end responsibilities for field users
inside accepted assignment containment. Product copy may describe this as
responsibility, route, coverage, assignment, or workload depending on the
screen.

The product must make clear that responsibilities govern who can work where
and on what. It must not imply that UI labels, IdP groups, locations, org
units, or product roles grant authority.

### 5. Do Field Work On Mobile

Field users can:

- connect or resume an actor session through the accepted server-resolved
  actor boundary;
- get work by syncing setup, assignments, and authorized work;
- see missing setup/forms, missing assignment, get-work, syncing, retry, and
  ready-to-capture states;
- capture configured activity entries offline or online;
- see saved-local, waiting-to-sync, syncing, synced, failed, and retry states;
- keep pending local work visible after capture;
- append corrections without mutating prior records;
- use handoff context where accepted subject-history and assignment behavior
  support it.

For internal or synthetic candidate demonstrations, the current bearer setup
path may remain the temporary mobile connection path. External field-user
Product Candidate 1 requires a separate mobile OIDC/login and token lifecycle
decision before it is presented as product-ready login.

### 6. Review Attention Items

Product Candidate 1 may include resolver-visible single-flag inspection and
single-item resolution as a successor-gated slice. It must preserve exact
designated-resolver equality and append-only resolution evidence.

Before implementing conflict review UI or making flag/resolution behavior
normative in product copy, select the conflict durable-surface route. Batch
resolution, pending-match queues, resolver reassignment, and auto-resolution
remain out of scope.

## Explicit Non-Goals

Product Candidate 1 does not include:

- real-production approval, real organizational data approval, compliance
  approval, support commitment, provider/region/jurisdiction selection, or
  real notification path;
- pooled SaaS, tenant switching, tenant-aware runtime internals,
  cross-tenant collaboration, multiple workspaces, or global fleet analytics;
- tenant/workspace fields in the event envelope, sync context, config package,
  assignment payloads, or mobile local partition keys;
- online production principal-binding administration APIs or UI;
- mobile OAuth/OIDC login, refresh-token lifecycle, provider selection, secure
  storage policy, or shared-device login UX unless NW-085 is selected;
- reporting dashboards, report APIs, warehouses, export/import, broad
  aggregate views, or broad audit/history reads;
- retention/security promises such as no-local-retention, local encryption,
  decommissioning, sealed-partition recovery, erasure, redaction, or token
  retention policy beyond accepted web session behavior;
- conflict batch handling, pending-match queues, resolver reassignment,
  auto-resolution, or non-designated resolver authority;
- new subject/query/custom scopes, deployer scope scripts, dynamic query
  authority, or custom containment logic;
- entity lifecycle, trigger execution, deployer-authored functions/scripts,
  custom state machines, or custom processing pipelines.

## Platform And Contract Guardrails

| Surface | Product Candidate 1 guardrail |
|---|---|
| Event envelope | No new fields and no new `type` values. Product terms remain product language. |
| Config package | Published setup stays one atomic package under accepted schema and sync/config behavior. |
| Shape DSL | Deployer form shapes remain flat, versioned, and schema-bounded. Platform payload schemas are not form shapes. |
| Expressions | Warnings/defaults/show conditions remain pure bounded AST rules, not scripts or authority. |
| Assignment | Assignment scope authorizes work inside the default workspace; it is not tenant isolation. |
| Sync/access | Normal pull remains request-time actor-scoped and assignment-derived. Do not sync broad data and hide it in UI. |
| Auth | Provider principal maps only through explicit active `(issuer, subject) -> actor_id` binding. |
| Web admin | Browser sessions carry server-resolved actor context; command authority is server-evaluated and command-specific. |
| Mobile | Mobile labels and warnings remain advisory; server remains authoritative for auth, scope, flags, and persistence. |
| Operations | Synthetic reference evidence can support candidate demos; real production needs NW-093. |

## Product Acceptance Criteria

Product Candidate 1 is accepted as a product milestone only when successor
implementation rows prove the following without changing contracts or claiming
real production:

1. The product presents one Organization and no tenant/multi-workspace
   selection.
2. Web admin login uses OIDC/JWKS browser login, a server-managed session,
   explicit principal binding, CSRF/logout/expiry behavior, and secret-safe
   login/session audit.
3. The production development console, fixed development admin actor, dev
   token/bootstrap surfaces, request-body actor IDs, and IdP claims do not
   become production authority.
4. Setup can move from draft to validated, readiness-reviewed, approved, and
   published while preserving accepted config/package/expression behavior.
5. Assignment administration uses accepted create/end command capability and
   same-assignment containment.
6. A field user can get work, understand missing setup/forms and missing
   assignment states, capture an activity entry, see saved-local/waiting/
   syncing/synced/failed states, retry sync, and append a correction.
7. Mobile field work preserves server-resolved actor alignment, per-actor
   partitions, append-only events, and advisory warning behavior.
8. Optional single-flag review, if included, preserves exact designated
   resolver equality and excludes batch/automation/reassignment behavior.
9. Evidence comes from focused server/mobile tests, documentation verification,
   and synthetic/non-sensitive candidate demonstrations. It is not real
   production proof.

## Implementation Successors

| Route | Status after NW-084 | Purpose |
|---|---|---|
| NW-086 | ready candidate to promote first | Implement web admin OIDC/JWKS login, session, CSRF, logout, expiry, principal-binding revalidation, admin actor context, login/session audit, and minimal protected shell. |
| NW-087 | blocked on NW-086 | Implement `web_admin.access`, `web_admin.read_scoped`, `config_admin.*` command policy and scoped shell gate. |
| Setup/config workflow row | candidate after NW-087 | Productize draft, validate, readiness review, approve, and publish over accepted config contracts. |
| Assignment admin UX row | candidate after NW-087 | Productize assignment create/end over accepted command capability and containment. |
| Mobile vocabulary/navigation row | candidate after NW-084 | Productize mobile get-work, readiness, capture, sync, correction, warning, and handoff language over accepted mobile behavior. |
| NW-085 | candidate | Decide mobile OIDC/login and token lifecycle before external field users rely on product login. |
| NW-071 | candidate | Extract shared-device/local-state behavior before mobile/session/tenant work relies on IDR-era details as normative. |
| NW-072 | candidate | Extract conflict flag/resolution behavior before conflict UI, flag reporting, batch handling, resolver reassignment, or auto-resolution. |
| Real-production route NW-093 | blocked until real users/data are proposed | Decide provider, region, data classification, support, notification, compliance/security review, continuity, and go/no-go. |
| Multi-tenant/control-plane routes NW-094 through NW-098 | future/candidate/blocked | Use only when moving beyond the managed single-tenant lane. |

## Stop Conditions

Stop and route before product or implementation work:

- exposes Tenant or multiple Workspaces as Product Candidate 1 behavior;
- uses location hierarchy, org units, assignment scope, UI-selected
  organization, IdP claims, or a generic admin role as isolation or authority;
- changes event envelope fields, event `type` values, sync/config/admin
  process contracts, config package shape, shape format, or assignment payloads;
- adds tenant-aware auth, tenant/workspace storage columns, tenant sync
  context, mobile tenant partitions, pooled predicates, or storage backfills;
- claims real production readiness or handles real users/data without NW-093;
- turns mobile warnings into authoritative rejection;
- turns readiness review, product state labels, report labels, or setup copy
  into platform workflow truth;
- bundles reporting/export, retention/security, conflict automation, online
  principal-binding admin, new scopes, or production approval into a PC1
  implementation row.
