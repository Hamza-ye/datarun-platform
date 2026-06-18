# NW-083 Agent Prompt: Decide Tenant/Workspace Vocabulary And Managed-Isolation Boundary

You are working in `/home/hamza/datarun-platform`.

## Goal

Produce the bounded tenant/workspace vocabulary and first isolation-boundary
decision route for Product Candidate 1 before any tenant-aware implementation,
product copy, storage partitioning, auth/session change, sync/config context
change, or multi-customer deployment work begins.

Exit target:

```text
Datarun has a backlog-visible, correctly classified route for tenant,
organization, workspace, org unit, location, membership, actor, assignment,
entitlement, and managed-isolation terminology and boundaries.
```

This is decision and routing work. It is not implementation, not a schema
change, not a contract change, not a production approval, and not a full
Product Candidate 1 product specification.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/agent-working-surface/README.md`
6. `docs/agent-working-surface/platform-next-work-backlog.md` row NW-083,
   row NW-082, and the trigger map
7. `docs/agent-working-surface/artifacts/product-candidate-1-orchestration-note.md`
8. `docs/agent-working-surface/artifacts/NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md`
9. `docs/agent-working-surface/artifacts/2026-06-18-multi-tenancy-architecture-analysis.md`
10. `docs/agent-working-surface/decision-anchor-layer/README.md`
11. `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
12. `docs/specifications/README.md`
13. `docs/specifications/product/README.md`
14. `docs/specifications/platform/README.md`
15. `docs/specifications/platform/assignment-scope-and-administration.md`
16. `docs/specifications/platform/production-auth-principal-binding.md`
17. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
18. `docs/specifications/platform/configuration-package-and-shapes.md`
19. `docs/implementation/module-interfaces.md`
20. Relevant contracts:
    - `contracts/envelope.schema.json`
    - `contracts/sync-protocol.md`
    - `contracts/config-package.schema.json`
    - `contracts/shape-format.schema.json`

Use CDL slices only for exact authority checks exposed by this work. Start
with targeted searches for `tenant`, `workspace`, `organization`,
assignment-derived access, sync/access equivalence, authority projection,
fixed scope mechanisms, event envelope closure, configuration boundaries, and
projection rebuildability. Use `scripts/query_cdl.py`; do not read or rewrite
the whole CDL.

Do not read broad history unless a concrete authority conflict routes you
there.

## Current Standing To Preserve

- NW-082 is accepted only as a non-binding routing artifact. It is not
  accepted tenant architecture, product behavior, contract authority, or
  production readiness.
- There is no accepted CDL tenant/workspace decision in the current packet.
- Accepted assignment scope remains authorization inside the current
  deployment/workspace universe, not tenant isolation.
- Accepted production auth maps provider principals only through explicit
  principal binding. IdP groups, roles, claims, resource claims, custom claims,
  JWT `actor_id`, and UI-selected context are not platform authority.
- Accepted production web admin authority is command-specific. Do not collapse
  tenant admin, workspace admin, config admin, assignment admin, resolver
  authority, support access, deployment owner, and activity roles into one
  generic admin.
- Synthetic reference-deployment evidence does not prove real production
  readiness.

`docs/status.md` still contains older first-deployment "recommended next move"
language. Treat the NW backlog plus NW-082/NW-083 packet as the current PC1
lane route for this task; do not rewrite status unless this slice materially
changes accepted standing and the repo workflow allows it.

## Questions To Answer

1. Does the proposal remain a non-binding routing decision, or does it accept a
   durable product, platform, architecture, contract, or operations boundary?
2. Which classification applies under the gap playbook:
   product/problem evidence gap, architecture decision gap, platform-spec
   detail gap, implementation/tooling gap, operational policy gap, or a split?
3. What is the selected vocabulary for:
   - account;
   - tenant;
   - organization;
   - workspace;
   - org unit, team, or department;
   - location hierarchy;
   - principal;
   - platform user if needed;
   - membership;
   - actor;
   - assignment;
   - entitlement or service tier?
4. Which terms are user-visible for Product Candidate 1, and which remain
   internal platform or operations terms?
5. Is Product Candidate 1 a one-organization/default-workspace candidate, a
   managed single-tenant deployment posture, a bridge-isolated posture, or
   something else? What is explicitly not selected?
6. Does this slice make "organization" the customer-facing name for tenant, or
   does it require Hamza/product confirmation before using that word in
   product copy?
7. Does "workspace" become a product surface in Product Candidate 1, or remain
   the internal/default operational container until a later product spec?
8. What are the negative boundaries:
   locations are not tenants; org units are not tenants; assignment scope is
   not tenant isolation; UI-selected organization is not authority; IdP claims
   are not tenant authority; tenant/workspace fields are not added to the
   event envelope?
9. Which successor routes are required before implementation:
   Product Candidate 1 product spec, managed-deployment control-plane
   boundary, tenant-aware identity/membership, tenant data isolation and
   sync/config partitioning, mobile local partitioning, singleton scaffold,
   tenant isolation harness, real-production approval?
10. What must remain deferred: pooled storage, cross-tenant collaboration,
    cross-tenant subject identity, global analytics, reporting/export,
    retention/security, conflict batch handling, online principal-binding
    admin, new scope mechanisms, and real-production approval?

## Expected Durable Output

Select the durable output home by classification. Do not leave binding
behavior only in a prompt, backlog row, or non-binding artifact.

Preferred routing:

- If accepting tenant/workspace as structural isolation authority, route a CDL
  successor or explicitly selected architecture decision artifact first.
- If accepting exact platform-detail behavior inside settled architecture,
  create and index a platform specification under
  `docs/specifications/platform/`.
- If accepting user-visible Product Candidate 1 wording, create and index a
  product specification under `docs/specifications/product/`.
- If accepting managed single-tenant deployment or control-plane operating
  choices, route an operations policy or control-plane architecture successor.
- If owner/product confirmation is missing, create a non-binding routing
  artifact under:

```text
docs/agent-working-surface/artifacts/NW-083-tenant-workspace-vocabulary-and-managed-isolation-boundary.md
```

and leave NW-083 not accepted, with the missing decision named.

The output, whether binding or non-binding, must include:

- document metadata appropriate to its home;
- source and authority review;
- explicit classification and why;
- selected or proposed vocabulary table;
- negative-boundary table;
- managed-isolation posture and rejected alternatives;
- Product Candidate 1 implications;
- successor route table;
- acceptance criteria;
- verification and stop conditions;
- any exact escalation question for Hamza if a real owner decision is needed.

## Guardrails

- Do not implement code, migrations, runtime tenant context, request routing,
  tests, web UI, mobile UI, auth/session behavior, storage metadata, or
  deployment tooling in this slice.
- Do not alter contracts, schemas, event envelope fields, envelope `type`
  values, sync protocol semantics, config-package shape, assignment payloads,
  flag catalog, BAR, CDL, operations evidence, or accepted specs unless the
  output explicitly routes that as a successor and the task is re-scoped.
- Do not use location hierarchy, org unit membership, assignment scope,
  UI-selected organization, or IdP claims as tenant isolation.
- Do not add tenant/workspace fields to the event envelope.
- Do not treat assignment-derived access as tenant isolation.
- Do not claim real production readiness from synthetic reference-deployment
  evidence.
- Do not create new first-deployment workshop stages.
- Do not collapse all admin roles into one generic admin.
- Do not absorb NW-071, NW-072, NW-073, NW-044, NW-053, NW-054, mobile OIDC,
  reporting, retention/security, conflict automation, or production approval
  into this slice.

Stop and report if the work cannot decide customer-facing terminology,
commercial account posture, managed-deployment commitment, or structural
architecture without Hamza/product input.

## Verification

Run:

```bash
git diff --check
```

Also verify:

- any new durable spec is indexed from the nearest README;
- any non-binding artifact clearly states status, authority boundary, and
  successor route;
- the NW-083 backlog row links the prompt and selected output;
- no runtime code, schemas, fixtures, migrations, contracts, BAR, CDL,
  operations evidence, status acceptance, or unrelated docs changed unless the
  routing explicitly selected and justified that output;
- `docs/status.md` is not changed only to chase a recommendation line unless
  accepted standing materially changed and the repo workflow allows it.

## Commit Flow

If commits are requested, use separate commits:

```text
docs(product): route tenant workspace boundary
docs(architecture): decide tenant workspace boundary
docs(status): accept tenant workspace boundary
```

Use the NW trailer on commits owned by this route:

```text
NW: NW-083
```

Do not mark NW-083 accepted until the selected output exists, verification is
complete, and any required owner/product acceptance boundary is satisfied. Do
not start tenant-aware implementation or Product Candidate 1 product copy
until the required successor decision/spec route is accepted or explicitly
scoped as non-binding.

## Stop And Report

Stop if the slice starts implementation, changes contracts or schemas, claims
production readiness, chooses tenant/workspace architecture only in a
non-binding artifact while treating it as accepted, or requires Hamza to select
customer-facing terminology, commercial account posture, data-isolation
commitment, or real-production posture.
