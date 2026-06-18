# NW-083 Tenant/Workspace Vocabulary And Managed-Isolation Boundary

Status: accepted for routing
Document type: routing_artifact
Owner: project shepherd
Source: NW-083 row and `docs/agent-working-surface/prompts/NW-083-decide-tenant-workspace-vocabulary-and-managed-isolation-boundary.md`
Authority: none for runtime architecture, product behavior, contracts, schemas, operations approval, or implementation; accepted only as Product Candidate 1 routing and classification under the CDL, contracts, accepted specifications, BAR, operations documents, and NW backlog
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/agent-working-surface/artifacts/NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md`; `docs/agent-working-surface/artifacts/2026-06-18-multi-tenancy-architecture-analysis.md`; `docs/agent-working-surface/artifacts/product-candidate-1-orchestration-note.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/production-auth-principal-binding.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md`; `contracts/envelope.schema.json`; `contracts/sync-protocol.md`; `contracts/config-package.schema.json`

## Executive Summary

NW-083 accepts a reversible Product Candidate 1 tenancy route:

```text
one customer-facing Organization
-> one managed single-tenant Datarun deployment
-> one internal/default Workspace
```

This does not make the application tenant-aware. It does not add tenant or
workspace fields, schemas, contracts, auth behavior, sync context, storage
metadata, mobile partitions, UI, operations approval, or production readiness.

The accepted first lane is managed single-tenant isolation at the deployment
boundary. That means each customer organization is isolated by its deployment,
database, deployment configuration, principal-binding namespace, OIDC
settings, backup/recovery scope, monitoring context, and operational evidence
until a later route accepts a bridge or pooled model.

Tenant-aware auth, workspace-scoped config, tenant sync context, mobile local
partition keys, storage backfills, and pooled `tenant_id` predicates are
low-reversibility changes. They remain successor decisions. Envelope changes
remain effectively blocked unless a formal CDL successor explicitly authorizes
them.

## Authority Review

Targeted CDL searches found no accepted `tenant` or `workspace` decision.
Accepted authority remains:

- CDL-006 and CDL-007: the event envelope and `type` vocabulary are closed.
- CDL-012: derived runtime concepts do not become envelope fields.
- CDL-030 through CDL-034 and CDL-055: access is assignment-based, sync scope
  equals access scope, authority is projected from assignment timelines, and
  scope mechanisms are platform-fixed.
- CDL-038, CDL-041, CDL-046, and CDL-056: configuration remains bounded,
  atomic at sync, sensitivity is shape/activity-level configuration, and
  activity is deployer configuration.

Accepted platform specs preserve the same boundary:

- assignment scope is authorization inside the current deployment/workspace
  universe, not tenant isolation;
- production auth maps provider principals only through explicit
  `(issuer, subject) -> actor_id` binding;
- production web admin authority is command-specific and does not collapse
  admin, assignment, resolver, support, deployment-owner, or activity-role
  authority;
- config package and sync contracts do not carry tenant or workspace context.

Operations evidence accepts only synthetic reference-deployment rehearsal
standing. Real production, real users, real organizational data, real
notification destination, provider/region/jurisdiction/data choices, and
independent continuity remain separate routes.

## Classification

| Pressure | NW-083 classification | Durable route selected |
|---|---|---|
| Product Candidate 1 top-level wording | Product/spec detail gap | Use `Organization` as the default Product Candidate 1 product-spec term in NW-084 unless product review changes it. |
| Tenant/workspace vocabulary and negative boundaries | Product/platform decision routing | Accepted here only as routing vocabulary. Binding product wording goes to NW-084; binding platform behavior requires later specs or architecture. |
| First isolation posture | Operational policy gap plus architecture-sensitive routing | Managed single-tenant deployment is the reversible lane. Control-plane authority and real production require NW-094/NW-093. |
| Tenant as a structural platform primitive | Architecture decision gap | Not accepted here. Route before tenant-aware internals. |
| Tenant/workspace auth, sync, config, storage, or mobile context | Architecture/contract/platform gap | Deferred to NW-095, NW-096, NW-071, NW-097, and NW-098 as appropriate. |

No critical unresolved owner decision blocks this routing result. Hamza supplied
the reversibility direction for the managed single-tenant lane. Escalate again
only when a successor proposes a low-reversibility structural change or a real
production commitment.

## Vocabulary Boundary

| Term | Accepted NW-083 routing meaning | Product Candidate 1 exposure |
|---|---|---|
| Account | Commercial, billing, or contract relationship that may own one or more organizations/tenants later. | Out of first product spec unless billing/entitlement pressure appears. |
| Tenant | Internal hard security, administration, entitlement, and data-isolation boundary. | Keep internal. Do not show `tenant` in PC1 copy by default. |
| Organization | Customer-facing label for the first tenant concept. | Use as default top-level product term in NW-084. |
| Workspace | Operational container inside a tenant for workflows, config, locations, subjects, events, assignments, projections, and scoped reporting. | Keep internal/default for PC1. Do not expose multi-workspace UI by default. |
| Org unit/team/department | Management grouping inside an organization/tenant. | Optional future product wording only; never isolation. |
| Location hierarchy | Operational geography/resource hierarchy inside the default workspace. | Product-visible as operational structure where needed; never tenant hierarchy. |
| Principal | External login identity from issuer and subject. | Internal/security term. |
| Platform user | Possible future cross-tenant human account profile. | Deferred. |
| Membership | Relationship allowing tenant/workspace entry or admin context. | Deferred; not enough to see data without assignments or explicit command capability. |
| Actor | Operational author identity used in event `actor_ref`. | Internal/platform term. |
| Assignment | Event-derived operational authority inside the default workspace. | Product wording may use responsibility, route, workload, assignment, or task ownership in NW-084. |
| Entitlement/service tier | Feature or service-plan gating outside the event stream. | Deferred unless PC1 needs service-tier language. |

## Negative Boundaries

| Not a tenant boundary | Reason |
|---|---|
| Location hierarchy | Locations are operational/resource scope for assignments and reporting. Reparenting a location must not become tenant migration. |
| Org unit/team/department | These are management groupings inside an organization. They do not provide data isolation. |
| Assignment scope | Assignments authorize work inside a workspace. They cannot be responsible for separating customers. |
| UI-selected organization | UI selection is not authority. Server-validated context is required in any future tenant-aware model. |
| IdP groups, roles, claims, or JWT `actor_id` | Accepted production auth rejects these as platform authority. |
| Event envelope fields | Tenant/workspace must not be added to the envelope without a CDL and contract successor. |
| Generic admin role | Tenant admin, workspace admin, config admin, assignment admin, resolver, support, deployment owner, and activity roles remain separate authority concepts. |

## Managed-Isolation Posture

The accepted Product Candidate 1 execution lane is managed single-tenant
deployment:

- one customer-facing organization maps to one Datarun deployment;
- that deployment has one internal/default workspace;
- deployment/database/config/auth/backup/monitoring boundaries carry
  isolation first;
- the current single-tenant runtime remains intact;
- a later SaaS control plane may map accounts/organizations to deployments
  without writing Datarun event truth;
- bridge or pooled models remain future decisions after tenant context and
  isolation tests exist.

Rejected for this lane:

- pooled row-level multi-tenancy as the first step;
- adding `tenant_id` or `workspace_id` to events or envelope schemas now;
- changing `/api/sync/*`, `/api/sync/config`, assignment APIs, admin APIs, or
  mobile local-store keys now;
- using locations, org units, assignments, UI selection, or IdP claims as the
  isolation mechanism;
- claiming real production approval from synthetic reference evidence.

## Product Candidate 1 Implications

NW-084 should specify Product Candidate 1 as a one-organization,
default-workspace operational candidate. Product copy may use Organization as
the top-level customer-facing term. Product copy should not expose Tenant,
multiple Workspaces, pooled SaaS, cross-tenant collaboration, or real
production readiness.

PC1 implementation can proceed on web admin login/session, admin command
gates, S23 setup/config productization, assignment administration, and mobile
operational language only through their own rows. Tenant-aware internals are
not prerequisites for those first PC1 implementation slices when the
managed-deployment lane is used.

## Successor Route

| NW | Status after NW-083 | Purpose | Stop condition |
|---|---|---|---|
| NW-084 | ready | Specify Product Candidate 1 user-visible scope, actors, journeys, language, and acceptance criteria. | Product behavior remains only in artifacts/backlog or claims real production. |
| NW-085 | candidate | Decide mobile OIDC/login and token lifecycle before external field users rely on mobile login. | IdP claims, UI-selected actor, or retained-token policy becomes authority. |
| NW-086 | candidate | Implement production web admin login/session boundary from accepted NW-079. | Adds config/admin expansion, tenant-aware internals, online binding admin, or claim authority. |
| NW-087 | blocked on NW-086 | Implement web/config admin command capability and scoped shell gate. | Collapses command capabilities into a generic admin role. |
| NW-093 | blocked until real users/data are proposed | Decide real-production approval package. | Uses synthetic rehearsal, synthetic webhook, or lab token path as production proof. |
| NW-094 | candidate | Decide managed-deployment SaaS control-plane boundary. | Control plane writes Datarun event truth or becomes platform authority without decision. |
| NW-095 | future decision | Decide tenant-aware identity, membership, and actor model. | IdP groups/claims or unchecked UI tenant selection become authority. |
| NW-096 | future decision | Decide tenant data isolation and sync/config partitioning. | Pooled storage or contract changes start before architecture/contract route. |
| NW-097 | blocked | Implement singleton tenant/default-workspace scaffold only if accepted internals require it. | User-visible multi-workspace or pooled behavior appears. |
| NW-098 | blocked | Build tenant isolation test harness before pooling. | Tests prove only UI hiding instead of data-access isolation. |

## Acceptance Criteria

NW-083 is accepted only for routing when:

- the vocabulary and negative boundaries above are recorded;
- managed single-tenant deployment is selected as the reversible first lane;
- low-reversibility tenant-aware changes are routed to successors;
- the backlog row records the bounded result and no-code/no-contract evidence;
- NW-084 is available as the next actionable Product Candidate 1 row;
- no runtime code, schemas, contracts, tenant storage, auth/session behavior,
  sync/config partitioning, mobile partitioning, BAR, CDL, operations evidence,
  or real-production approval changed;
- `git diff --check` passes.

## Stop Conditions

Stop and route before work:

- changes event envelope fields or `type` values;
- changes sync/config/admin process contracts for tenant/workspace context;
- adds tenant/workspace columns, storage backfills, query predicates, RLS, or
  pooled storage;
- changes principal binding to tenant-aware auth or platform-user membership;
- changes mobile local partition keys;
- exposes multi-workspace UI or cross-tenant collaboration;
- claims real production readiness or handles real users/data without
  NW-093;
- treats locations, org units, assignment scope, UI selection, IdP claims, or a
  generic admin role as isolation or authority.
