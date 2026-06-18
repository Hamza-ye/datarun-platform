# Multi-Tenancy Architecture Analysis

Status: non-binding exploration and routing artifact
Date: 2026-06-18
Owner/context: architecture review for future multi-tenancy model

This artifact does not change the Canonical Decision Ledger, contracts,
baseline acceptance standing, runtime behavior, schemas, or backlog status. It
records repository-grounded analysis and suggested routing for a future
multi-tenancy decision path.

## Executive Summary

The current platform is effectively single-tenant at the deployment level. The
implicit boundary is one deployment with one event stream, one location tree,
one configuration package stream, one deployment configuration store, one
principal-binding namespace, one actor namespace, and one sync/access universe.
Within that deployment, access is mature and intentionally assignment-scoped:
actor plus active assignment plus role/action plus geographic, subject-list,
activity, and time scope.

The safest product model is not "organizational unit equals tenant." The safer
professional model is:

```text
Account / Organization
  owns one or more Tenants, usually 1:1 at first
Tenant
  is the SaaS security, administration, entitlement, and data-isolation boundary
Workspace / Operational Deployment
  is the container for scenarios, workflows, config packages, locations,
  subjects, events, assignments, and reporting within a tenant
Organization Unit / Team / Department
  is an internal management grouping inside a tenant, not a data-isolation
  boundary by itself
Location Hierarchy
  is the operational geography/resource hierarchy already used for assignment
  scope, not a tenant hierarchy
Assignment Scope
  is the per-actor operational authority boundary inside a workspace
```

Recommended default: treat the first SaaS multi-customer posture as managed
single-tenant deployments, one isolated deployment per customer organization.
Conceptually, that deployment is a tenant with one default workspace, but the
workspace concept should not become an in-app product surface until product
evidence proves customers need multiple operational workspaces under one
organization. This keeps strong tenant isolation through separate
deployment/database/schema/stamp boundaries while the codebase lacks explicit
tenant context and tenant-isolation tests. Design the future model so a later
database-per-tenant or pooled implementation can add `tenant_id` and
`workspace_id` storage metadata without putting tenant fields into the event
envelope.

Do not use the existing `locations` hierarchy as the tenant model. Locations are
assignment and reporting scopes. Making locations into tenants would conflate
resource hierarchy, administrative geography, legal/customer ownership, and
security isolation.

## Repository Review Findings

### Reviewed Sources

Repository sources reviewed:

- `docs/status.md`, especially Current Routing.
- `docs/implementation/module-interfaces.md`.
- `docs/agent-working-surface/decision-anchor-layer/README.md`.
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`.
- `docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md`.
- `docs/agent-working-surface/baseline-acceptance-register.md`.
- `docs/agent-working-surface/platform-next-work-backlog.md`.
- CDL slices via `scripts/query_cdl.py`, especially CDL-001 through CDL-005,
  CDL-030 through CDL-038, CDL-041, CDL-046, CDL-055, and CDL-056.
- `contracts/sync-protocol.md`.
- `contracts/flag-catalog.md`.
- `contracts/config-package.schema.json`.
- `contracts/shapes/assignment_created.schema.json`.
- `contracts/shapes/assignment_ended.schema.json`.
- Platform specs:
  - `docs/specifications/platform/assignment-scope-and-administration.md`
  - `docs/specifications/platform/production-auth-principal-binding.md`
  - `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
  - `docs/specifications/platform/configuration-package-and-shapes.md`
- Migrations V1 through V10 under `server/src/main/resources/db/migration/`.
- Server code in event, sync, authorization, config, admin, and principal
  binding paths.
- Relevant scenarios and product vocabulary guardrails:
  - `docs/scenarios/03-user-based-assignment.md`
  - `docs/scenarios/15-cross-program-overlays.md`
  - `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`
  - `docs/scenarios/24-long-running-deployment-data-lifecycle.md`
  - `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`
  - `docs/scenarios/26-operational-reporting-and-aggregate-oversight.md`
  - `docs/agent-working-surface/operational-ux-layering-companion.md`

External reference sources reviewed:

- AWS Well-Architected SaaS Lens, tenant isolation:
  https://docs.aws.amazon.com/wellarchitected/latest/saas-lens/tenant-isolation.html
- AWS Well-Architected SaaS Lens, SaaS identity:
  https://docs.aws.amazon.com/wellarchitected/latest/saas-lens/saas-identity.html
- AWS Well-Architected SaaS Lens, silo/pool/bridge models:
  https://docs.aws.amazon.com/wellarchitected/latest/saas-lens/silo-pool-and-bridge-models.html
- AWS Well-Architected SaaS Lens, tenant concept:
  https://docs.aws.amazon.com/wellarchitected/latest/saas-lens/tenant.html
- Microsoft Azure Architecture Center, multitenant storage/data approaches:
  https://learn.microsoft.com/en-us/azure/architecture/guide/multitenant/approaches/storage-data
- Microsoft Azure Architecture Center, multitenant checklist:
  https://learn.microsoft.com/en-us/azure/architecture/guide/multitenant/checklist
- PostgreSQL row security documentation:
  https://www.postgresql.org/docs/current/ddl-rowsecurity.html

### Current Assumptions

The CDL has no accepted tenant decision. A targeted CDL search for `tenant`
returned no matching decisions. The architecture authority that does exist is
about event truth, assignment-derived access, sync scope, configuration
boundaries, and fixed scope mechanisms.

Current access model:

- CDL-030: access is assignment-based.
- CDL-031: sync scope equals access scope.
- CDL-032: authority is projected from assignment timelines and context, not
  stored in the event envelope.
- CDL-034: assignment creation must enforce scope containment.
- CDL-055: current scope mechanisms are platform-fixed: `geographic`,
  `subject_list`, and `activity`.

Current configuration model:

- Configuration is deployment-wide in behavior and table shape.
- `config_packages.version` is global.
- `deployment_config.config_key` is global.
- `flag_severity_overrides` are explicitly deployment-wide.
- `assignment_admin_capabilities` is a deployment-configured server-side policy.
- The config package includes shapes, activities, expressions, severity
  overrides, sensitivity classifications, and pattern definitions as one atomic
  full snapshot.

Current identity/auth model:

- Production auth resolves a bearer credential through explicit
  `(issuer, subject) -> actor_id` principal binding.
- IdP groups, roles, resource claims, custom claims, and JWT `actor_id` are
  explicitly non-authority.
- Current active binding uniqueness is by `(issuer, subject)`, not by tenant.
- There is no separate platform user, account, tenant membership, or workspace
  membership model.

Current operational data model:

- `events` has no `tenant_id`, `workspace_id`, or deployment key.
- `events.sync_watermark` is global.
- `UNIQUE(device_id, device_seq)` is global.
- `locations` is one global materialized-path hierarchy.
- `subject_locations` maps subjects to one global location tree.
- `subject_aliases` is global.
- `shapes`, `activities`, `expression_rules`, and `config_packages` are global.
- `actor_tokens` and `auth_principal_bindings` are global.
- `device_sync_state` is keyed by `(device_id, actor_id)`, not by tenant or
  workspace.
- Projection code such as `EventRepository.findAllOrdered()` reads the whole
  event stream.

### Current Single-Tenant Pressure Points

The following surfaces would block or endanger a pooled multi-tenant model
unless they gain an explicit tenant/workspace boundary:

| Surface | Current assumption | Multi-tenant risk |
|---|---|---|
| Event store | One global event stream and watermark | Missing tenant filter leaks events, flags, identity lineage, and assignments. |
| Assignment events | Assignment identity is `subject_ref.id`; target actor has no tenant context | Root/unrestricted assignment could become cross-tenant root if not bounded. |
| Sync pull | Computes current actor assignments without tenant context | Pull can scan or return events from the wrong tenant if filters are incomplete. |
| Config package | One latest package from all active shapes/activities | A tenant can receive another tenant's workflow/config/sensitivity profile. |
| Principal binding | One active `(issuer, subject) -> actor_id` | One registered user cannot safely belong to multiple tenants without a tenant selector or membership model. |
| Actor IDs | Actor UUIDs are global operational authors | Actor/account/user concepts are not separated enough for multi-tenant membership. |
| Location tree | One global hierarchy | Location branches are access scopes, not legal/customer boundaries. |
| Subject aliases | One global alias projection | Cross-tenant merges/splits must be impossible unless explicitly designed. |
| Domain uniqueness | Duplicate basis reads prior events globally by shape | Tenant A data can affect Tenant B uniqueness and flags. |
| Reporting/projections | Whole-stream rebuilds exist | Broad read models must be tenant-filtered or physically isolated. |
| Device state | `(device_id, actor_id)` only | A shared device used across tenants needs tenant/workspace-local partitions. |
| Admin UI | Development-only fixed dev actor | Production admin work needs tenant-aware session and command authority. |

### Existing Gaps Already Relevant

The backlog and gap playbook already expose related future-decision lanes:

- GAP-PRODUCT-01: multi-tenant naming strategy is open.
- BAR-108/NW-053: new subject/query/custom scope mechanisms are future
  decisions.
- BAR-106/NW-054: field-level sensitivity, encryption, redaction, local expiry,
  and retained data are future decisions.
- NW-044: reporting aggregation/import/export boundary remains future work.
- NW-071 through NW-073: durable specs for shared-device sessions, conflict
  flags/resolution, and pattern registry/projection remain candidate rows.
- NW-079 selected production web admin auth/authority but did not implement it
  and did not add tenant administration.

## Model Options And Trade-Offs

### Option A: Managed Single-Tenant Deployments / Silo

Model:

- Each customer organization gets a separate deployment, database, config
  universe, event stream, location tree, principal-binding namespace, backup
  scope, OIDC settings, and operational runbook context.
- The current single-tenant code remains mostly intact.
- A control plane may later create, list, operate, and bill deployments.

Pros:

- Strongest near-term isolation.
- Lowest risk to the event model, sync protocol, config packaging, and current
  assignment logic.
- Aligns with high-sensitivity deployments and early production hardening.
- Easy rollback and per-customer operational control.

Cons:

- Higher infrastructure and operations cost per tenant.
- Cross-tenant users require separate principal binding per deployment or a
  control-plane login model.
- Cross-tenant analytics, fleet administration, and tenant lifecycle automation
  need a separate control plane.
- Does not by itself solve customer-facing "organization/workspace" product UX.

Fit:

- Best first production posture if customer data is sensitive or tenant volume
  is low.
- Best fit with the current repository because it preserves current schema and
  runtime assumptions.
- Good bridge while tenant-aware application code, mobile local partitioning,
  and isolation tests are designed.

### Option B: Shared App, Database-Per-Tenant

Model:

- One runtime routes each request to a tenant database after resolving tenant
  context from host, path, session, or another server-validated selector.
- Current schema mostly survives inside each tenant database.

Pros:

- Stronger isolation than pooled rows while improving operations over fully
  separate stacks.
- Avoids tenant columns in the immutable event stream during the first
  multi-customer step.
- Supports per-tenant backup/restore and data residency more cleanly than
  pooled rows.

Cons:

- Needs tenant routing, tenant migration orchestration, connection pool
  management, per-tenant OIDC/principal binding, support tooling, and mobile
  partition rules.
- Cross-tenant reporting remains a separate control-plane/read-side concern.

Fit:

- Good second step after managed single-tenant deployments.
- Safer than pooled rows for this platform's current event/config/sync model.

### Option C: Pooled Tenant-Per-Organization

Model:

- One shared application/database.
- Every tenant-owned row has `tenant_id`.
- Requests carry a resolved tenant context.
- Tenant isolation is enforced in repository queries, service checks, and
  ideally database row-level controls.

Pros:

- Common SaaS operating model.
- Efficient for many small tenants.
- Centralized operations and easier uniform rollout.
- Enables shared control-plane features.

Cons:

- High migration complexity for this repository.
- Every query path, projection, sync cursor, uniqueness detector, config
  publisher, principal binding, and admin action must become tenant-aware.
- One missing filter can leak data.
- Current principal-binding model does not support multi-tenant membership.

Fit:

- Good long-term SaaS model, but not safe as the first step unless preceded by
  tenant-context scaffolding and isolation tests.

### Option D: Tenant Plus Workspaces / Operational Deployments

Model:

- Tenant is the customer/security boundary.
- Workspace is the operational container for scenarios/workflows, event data,
  config packages, locations, subjects, assignments, and reporting.
- Initial state can be one tenant with one workspace. Later, a tenant may own
  multiple workspaces for separate programs, regions, projects, sandboxes, or
  lifecycle stages.

Pros:

- Cleanly separates legal/customer isolation from operational organization.
- Maps closely to the current "deployment" implementation while giving room for
  multiple scenario/workflow areas under one customer.
- Lets internal org units, departments, and locations remain in-tenant
  structures instead of becoming tenants.
- Supports both silo and pooled storage implementations.

Cons:

- Requires a decision about whether config packages are tenant-scoped,
  workspace-scoped, or both.
- Adds one more domain concept.
- Cross-workspace reporting and shared subjects need explicit future decisions.

Fit:

- Best conceptual target for this platform.
- Good target vocabulary, implemented initially as tenant:workspace = 1:1.
- Do not productize multiple workspaces inside one Datarun deployment until
  concrete product pressure proves they are needed.

### Option E: Hierarchical Tenants / Parent-Child Accounts

Model:

- Parent tenants own or administer child tenants or subaccounts.
- Useful for enterprise divisions, reseller models, or regional subsidiaries.

Pros:

- Fits complex enterprise procurement and delegated administration.
- Can support roll-up billing and controlled fleet administration.

Cons:

- Easy to confuse with internal org units and location hierarchies.
- Authorization, reporting, entitlements, and data residency rules become much
  harder.
- Current platform has no authority model for cross-tenant administration.

Fit:

- Defer. Add only after the tenant/workspace model is proven.

### Option F: Location Or Administrative Unit As Tenant

Model:

- A location branch, district, facility, department, or internal administrative
  unit is treated as a tenant.

Pros:

- Appears to reuse existing location and assignment structures.

Cons:

- Conflates resource scope with customer/legal/security isolation.
- Reparenting or reorganizing a location becomes a tenant migration.
- Cross-location workflows, supervisors, transfers, and reporting become
  cross-tenant operations by accident.
- It weakens the current clean assignment-scope model.

Fit:

- Not recommended.

## Recommended Model

Use this conceptual model:

```text
Account
  Commercial/billing/contract entity. May own one or more tenants later.

Tenant
  Security and data-isolation boundary. Usually customer-facing as
  "Organization" at first.

Workspace
  Operational container inside a tenant. Owns scenarios/workflows,
  deployer-authored configuration, locations, subjects, events, assignments,
  workflow projections, and scoped reports.

Organization Unit / Team / Department
  In-tenant management grouping for people and administration. Does not grant
  operational data access by itself.

Location Hierarchy
  In-workspace operational geography/resource hierarchy. Used by assignment
  scope and reporting.

Assignment
  Event-derived operational grant: actor, role, scope, validity period.

Membership
  Relationship between a registered platform user/principal and a tenant or
  workspace. It allows entry/admin context. It is not enough to see operational
  data unless paired with assignments or explicit admin capabilities.
```

Customer-facing wording can call a tenant an "Organization." Internally, keep
the term "tenant" for the hard isolation boundary. Avoid "organizational unit"
as the top-level isolation term because it already means departments, teams, or
administrative subdivisions in many products.

### Why This Is Safer

The current platform already has a strong in-deployment authorization model.
It should be preserved and placed inside a tenant/workspace boundary, not
stretched to become the tenant boundary.

Tenant isolation must be above assignments. Assignments decide what an actor can
see and do inside a workspace. They should never be responsible for preventing
Tenant A from seeing Tenant B.

Workspace gives a durable name for the current "deployment" concept without
forcing every customer organization to be exactly one operational universe
forever.

Implementation default:

- First customer-facing SaaS posture: managed single-tenant deployment per
  customer organization.
- Control-plane concept: organization/account owns one or more deployments.
- In-platform concept: current Datarun deployment remains one tenant/default
  workspace until a routed decision introduces multiple in-platform workspaces.
- Migration posture: avoid pooled event/config/auth data until tenant context,
  mobile partitioning, and tenant-isolation tests exist.

### What Should Be Visible Now But Deferred

Make these concepts explicit in architecture and backlog routing now:

- Tenants are isolation boundaries.
- Workspaces contain scenarios/workflows/config/data.
- Users may belong to multiple tenants, but every request has one selected
  tenant/workspace context.
- Cross-tenant collaboration is not supported by default.
- Org units and locations are not tenants.
- Future pooled storage is possible, but only after tenant context and
  isolation tests exist.

Defer these until concrete pressure exists:

- Hierarchical parent-child tenants.
- Cross-tenant shared workspaces.
- Cross-tenant subject identity resolution.
- Tenant-to-tenant data sharing.
- Global analytics over multiple tenants.
- IdP group/claim authority.
- Field-level sensitivity, encryption, redaction, and erasure.
- Broad audit/history and reporting warehouses.

## Authorization And Boundary Guidance

### User, Principal, Actor, Membership

Recommended distinctions:

| Concept | Meaning |
|---|---|
| Principal | External login identity from an issuer and subject. |
| Platform User | Human account profile, if the product needs one. |
| Tenant Member | User/principal is allowed to enter a tenant and hold tenant capabilities. |
| Workspace Member | User/principal is allowed to enter a workspace and hold workspace capabilities. |
| Actor | Operational author identity used in event `actor_ref`. Tenant/workspace-scoped in meaning, UUID globally unique in storage. |
| Assignment | Event-derived operational authority for work data. |
| Capability | Server-side command permission such as `config_admin.publish` or `assignment_admin.create`. |
| Activity Role | Activity work-action mapping from config package, limited to current action vocabulary. |

The current direct `(issuer, subject) -> actor_id` binding is sufficient for a
single tenant. For multi-tenant SaaS, use one of these successor models:

1. `issuer + subject + tenant_id -> actor_id`
   - Request must include selected tenant context.
   - Simple migration from current binding.
   - Good if actor is tenant-scoped.

2. `issuer + subject -> platform_user_id`, then membership selects
   `tenant_id/workspace_id/actor_id`
   - Better long-term account model.
   - Supports one login across many tenants.
   - Requires more product/account work.

Either model must keep IdP groups and claims as non-authority unless a
successor decision explicitly changes that.

For the managed single-tenant default, keep the current direct
`(issuer, subject) -> actor_id` binding inside each isolated deployment. Add
cross-deployment/platform-user membership only in the SaaS control plane or a
later Datarun tenant-aware auth decision.

### Roles And Permissions

Use separate role/capability layers:

- Tenant admin capabilities: tenant settings, invitations, billing/service tier,
  workspace creation, tenant audit metadata.
- Workspace admin capabilities: config author/validate/review/approve/publish,
  location management, scoped admin shell access.
- Assignment-admin capabilities: existing `assignment_admin.create` and
  `assignment_admin.end` plus containment.
- Activity roles: existing `capture`, `review`, `alert`, `task_created`,
  `task_completed` for operational work events.
- Resolver authority: exact designated-resolver equality remains separate.

Do not combine these into one generic role table without capability boundaries.
The repo already has explicit non-authority warnings for IdP roles, product
personas, and activity roles.

### Cross-Tenant Collaboration

Default: unsupported.

Allowed first pattern:

- Invite the same platform user/principal into another tenant.
- They select that tenant/workspace context.
- Work authored there uses that tenant/workspace actor and assignments.

Do not initially support:

- One event visible in multiple tenants.
- Cross-tenant subject aliases/merge/split.
- Cross-tenant assignment grants.
- Cross-tenant normal sync.
- Cross-tenant report drill-back.

If cross-tenant collaboration becomes a product requirement, route it as a
separate architecture decision. It changes identity, event interpretation,
audit, retention, and support boundaries.

## Data Isolation And Platform Capabilities

### Isolation Level

Recommended path:

1. First production multi-customer posture: silo or bridge.
   - Separate deployment/database/schema/stamp per tenant or per tenant group.
   - Existing code can remain closer to current behavior.
   - Good for high-sensitivity data and early SaaS.

2. Future pooled model only after tenant-aware scaffolding.
   - Add `tenant_id` and likely `workspace_id` to all tenant-owned tables.
   - Resolve tenant/workspace context before any handler logic.
   - Enforce tenant predicates in every query.
   - Add database defense-in-depth, such as PostgreSQL row-level security where
     appropriate.
   - Add automated isolation tests and query review gates.

### Tenant-Specific Configuration

Current config is deployment-wide. In the target model, make config
workspace-scoped by default:

- A workspace owns deployer shapes, activities, expressions, sensitivity
  classifications, pattern bindings, severity overrides, and config packages.
- Config package version is per workspace.
- `/api/sync/config` returns the latest package for the selected workspace.
- Config package body does not need `tenant_id` if the process boundary already
  carries tenant/workspace context.

Tenant-level config should be limited to tenant administration, entitlements,
identity settings, service plan, and workspace defaults.

### Feature Entitlement And Service-Level Access

Model entitlements outside the event stream:

```text
tenant_entitlements(
  tenant_id,
  feature_key,
  state,
  limit_json,
  effective_from,
  effective_to,
  updated_by,
  updated_at
)

tenant_service_plan(
  tenant_id,
  plan_key,
  region,
  data_residency_class,
  backup_profile,
  support_tier,
  status
)
```

Entitlements decide whether a capability can be used at all. They do not grant
assignment scope, resolver authority, or event authorship.

### Sensitivity

Current accepted sensitivity is shape/activity-level configuration with
`standard`, `elevated`, and `restricted`. Keep that model within each workspace.
Do not claim field-level redaction, encryption, erasure, no-local-retention, or
regulatory compliance until BAR-106/NW-054 or a successor route defines it.

### Leakage And Privilege Risks

High-risk paths to test before pooling:

- Wrong tenant config package returned from `/api/sync/config`.
- Sync pull missing tenant filter.
- Subject-history backfill crossing tenant.
- Domain uniqueness detector comparing across tenants.
- Conflict resolver queues crossing tenants.
- Assignment containment treating unrestricted scope as global platform root.
- Principal binding selecting the wrong tenant actor.
- Admin publish or assignment create using a tenant-independent capability.
- Shared device partitions missing tenant/workspace in local storage keys.
- Projection rebuilds using whole-stream reads without tenant partition.
- Report aggregates bypassing event-level access.
- Mobile local state keyed only by server URL and actor rather than by
  tenant/deployment/workspace plus actor.

## Example Target Entities

Conceptual tables, not an implementation prescription:

```sql
tenants (
  id uuid primary key,
  slug text unique not null,
  display_name text not null,
  status text not null,
  created_at timestamptz not null
);

workspaces (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  slug text not null,
  display_name text not null,
  status text not null,
  default_timezone text,
  unique (tenant_id, slug)
);

auth_principals (
  id uuid primary key,
  issuer text not null,
  subject text not null,
  unique (issuer, subject)
);

tenant_memberships (
  tenant_id uuid not null references tenants(id),
  principal_id uuid not null references auth_principals(id),
  actor_id uuid not null,
  status text not null,
  tenant_capabilities jsonb not null default '{}',
  primary key (tenant_id, principal_id)
);

workspace_memberships (
  tenant_id uuid not null,
  workspace_id uuid not null,
  actor_id uuid not null,
  workspace_capabilities jsonb not null default '{}',
  status text not null,
  primary key (workspace_id, actor_id)
);
```

Tenant-owned current tables would gain boundary metadata:

```text
events: tenant_id, workspace_id, existing event columns
locations: tenant_id, workspace_id, existing columns
subject_locations: tenant_id, workspace_id, existing columns
subject_aliases: tenant_id, workspace_id, existing columns
shapes: tenant_id, workspace_id, existing columns
activities: tenant_id, workspace_id, existing columns
expression_rules: tenant_id, workspace_id, existing columns
config_packages: tenant_id, workspace_id, version, package_json, ...
deployment_config: tenant_id, workspace_id nullable depending on key
device_sync_state: tenant_id, workspace_id, device_id, actor_id, ...
auth_principal_bindings: tenant_id, issuer, subject, actor_id, active
```

Tenant/workspace metadata should be server-side process context and storage
metadata. Do not add tenant fields to the event envelope unless a successor CDL
decision explicitly changes the envelope contract.

## API And Contract Impact

Likely API changes:

- Tenant/workspace selection becomes explicit through host, path, or a
  server-validated header. Path or host is easier to reason about than a
  free-form body field.
- `/api/auth/me` either returns selectable memberships or returns actor context
  for the selected tenant/workspace.
- Sync endpoints become tenant/workspace-contextual.
- Config endpoint returns the selected workspace's config package.
- Assignment APIs operate only inside selected workspace context.
- Location APIs operate only inside selected workspace context.
- Admin APIs require tenant/workspace admin capabilities plus existing command
  checks.

Contract considerations:

- The event envelope should remain unchanged in the first design.
- `contracts/sync-protocol.md` would need a successor update for tenant or
  workspace context at the process boundary.
- `contracts/config-package.schema.json` may not need tenant/workspace keys if
  the endpoint context owns delivery. Add package keys only if devices must
  display or validate the context offline.
- Shared fixtures may need tenant/workspace wrappers only when cross-tenant or
  pooled behavior becomes testable.

## Phased Migration Strategy

### Phase 0: Decision And Vocabulary

- Promote a future NW row for multi-tenant vocabulary and boundaries.
- Decide that first SaaS posture is managed single-tenant deployments unless a
  concrete customer/operational requirement justifies in-app multi-workspace or
  pooled data earlier.
- Decide tenant, organization, workspace, org unit, location, assignment scope,
  and membership terms.
- Decide whether current "deployment" becomes "workspace", "tenant", or a
  1:1 tenant-workspace bootstrap.
- Define negative boundaries: locations are not tenants; org units are not
  data-isolation boundaries; assignments are not tenant isolation.

### Phase 1: Singleton Tenant/Workspace Scaffold

- If staying with managed single-tenant deployments, create a control-plane
  deployment registry before changing the Datarun event/config schema.
- If introducing tenant-aware Datarun internals, create one default tenant and
  one default workspace for existing data.
- Add internal request context objects before user-visible multi-tenancy.
- Keep current Datarun runtime behavior unchanged.
- Add tests proving existing single-tenant behavior still works.

### Phase 2: Auth And Membership Context

- Replace or extend direct principal-to-actor resolution with tenant-aware
  resolution.
- Add selected tenant/workspace context to `/api/auth/me` and mobile setup.
- Preserve IdP group/claim non-authority.
- Add tests for a principal belonging to two tenants and selecting exactly one.

### Phase 3: Storage Boundary

- Backfill tenant/workspace metadata onto tenant-owned tables.
- Add tenant/workspace indexes and constraints.
- Gate every repository query through tenant/workspace context.
- Add isolation tests for sync, config, assignment, conflict, identity,
  projection, reporting inputs, and admin.

### Phase 4: Config And Admin

- Scope config packages to workspace.
- Scope admin capabilities to tenant/workspace.
- Add tenant/workspace-aware production admin shell once NW-079 successors are
  implemented.
- Add feature entitlement checks separately from assignment and activity roles.

### Phase 5: Optional Pooling

- Only after all code paths are tenant-aware, decide whether to pool tenants in
  one database.
- Use a defense-in-depth strategy: service checks, query constraints, tenant
  context tests, and database row-level security or schema/database separation
  where appropriate.
- For high-sensitivity tenants, keep a silo/stamp even if small tenants are
  pooled.

### Phase 6: Advanced Models

- Multi-workspace tenants.
- Cross-workspace reporting.
- Parent-child account structures.
- Cross-tenant collaboration.
- Global fleet analytics.

Each of these should be its own decision route.

## Assumptions

- The platform wants to become SaaS while preserving the current offline-first,
  append-only, assignment-scoped architecture.
- Existing event envelopes should not gain tenant fields unless formally
  routed through architecture authority.
- Current "deployment" behavior is closer to an operational workspace than to a
  complete SaaS tenant/account model.
- Real customer data may include sensitive personal or operational data, so
  tenant isolation should be treated as a security boundary, not only a product
  grouping.

## Open Questions

- Is the commercial/customer-facing top-level concept called "Organization"?
- Can one customer organization run multiple independent operational
  deployments/workspaces?
- Do customers need separate sandboxes, test workspaces, or regions under one
  tenant?
- Does a registered platform user need one profile across tenants, or is a
  tenant-scoped actor enough?
- Are there reseller, parent-child, or ministry/sub-ministry account needs?
- Are any tenants expected to require physical data isolation, separate
  encryption keys, or separate regions?
- Should config packages be tenant-scoped, workspace-scoped, or both?
- Should locations ever be shared across workspaces?
- Does product need cross-tenant collaboration, or is invite into another
  tenant sufficient?
- What are the first billing/entitlement concepts: feature flags, limits,
  service tier, support tier, storage tier, or data residency?

## Decisions Needing Confirmation

1. Tenant is the hard isolation boundary.
2. Organization is the customer-facing name for tenant at first.
3. Workspace is the operational container for scenarios/workflows/config/data.
4. The current single deployment migrates to one tenant plus one workspace.
5. Org units/teams/departments are management groupings, not data isolation.
6. Locations remain operational/resource scope, not tenant hierarchy.
7. Registered users may belong to multiple tenants, but each request selects
   one tenant/workspace.
8. Cross-tenant collaboration is invite-based only at first.
9. First implementation path uses managed single-tenant deployments or
   database-per-tenant bridge isolation before pooled storage.
10. Multiple in-platform workspaces are deferred until product evidence proves
    need.
11. No tenant field is added to the event envelope in the first model.

## Suggested NW Routing

This analysis should become backlog-visible. Suggested rows, using next
available IDs rather than editing the backlog here:

| Suggested row | Type | Priority | Depends on | Exit condition |
|---|---|---|---|---|
| Decide SaaS tenant/workspace vocabulary and isolation boundary | product_platform_decision | P1 | GAP-PRODUCT-01; CDL-030/031/055; NW-069/070/079 | Durable decision artifact defines tenant, organization, workspace, org unit, location, membership, actor, assignment, entitlement, and negative boundaries. |
| Decide managed-deployment SaaS control-plane boundary | architecture_decision_gap / operational_policy_gap | P1 | tenant vocabulary decision; NW-063 through NW-067 operations surfaces | Select deployment registry, tenant/deployment ownership, per-tenant OIDC/config/provisioning, backup/restore scope, support access, and evidence boundaries outside the event store. |
| Decide tenant-aware identity, membership, and actor model | architecture_decision_gap | P1 | tenant/workspace boundary decision; NW-070 | Select principal-to-user-to-membership or `(issuer, subject, tenant) -> actor` model; define `/api/auth/me`, tenant selection, mobile setup, and non-authority boundaries. |
| Decide tenant data isolation and sync/config partitioning strategy | architecture_decision_gap | P1 | tenant/workspace boundary decision; NW-069/070; contracts | Select silo/bridge/pooled strategy, event-store tenant metadata plan, sync protocol context, config package scoping, watermark implications, and isolation tests. |
| Decide tenant/deployment-aware mobile local partitioning | architecture_decision_gap / implementation_tooling | P1 | tenant isolation decision; NW-071 candidate; NW-070 | Define local partition keys across server/deployment/tenant/workspace plus actor, token/session material, watermarks, config state, sealed pending work, and safe switching. |
| Implement singleton tenant/workspace scaffold | implementation_tooling | P2 | above decisions accepted | Existing data and behavior run under one default tenant/workspace with no user-visible semantic change and passing current server/mobile tests. |
| Build tenant isolation test harness | implementation_tooling | P1 | singleton scaffold | Tests prove sync, config, assignment, auth, conflict, projection, identity, and admin paths cannot cross tenant/workspace boundaries. |
| Decide tenant entitlement and service-tier model | platform_spec_detail_gap | P3 | tenant/workspace boundary decision | Specify feature entitlements, service tier, capability gating, operational ownership, and non-authority relation to assignments. |

Existing rows should remain separate and not be absorbed into multi-tenancy:

- NW-053 for new subject/query/custom scope mechanisms.
- NW-054 for retention, expiry, local encryption, redaction, and retained data.
- NW-044 for reporting aggregation/import/export.
- NW-071 through NW-073 for shared-device, conflict/flag, and pattern durable
  specs when those accepted behaviors become prerequisites.

Stop condition for future implementation: if a proposed implementation uses
location hierarchy, org unit membership, IdP claims, UI-selected organization,
or assignment scope as the tenant isolation boundary, stop and route back to
architecture.
