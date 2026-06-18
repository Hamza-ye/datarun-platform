# NW-084 Agent Prompt: Specify Product Candidate 1 User-Visible Milestone

You are working in `/home/hamza/datarun-platform`.

## Goal

Create the Product Candidate 1 product specification before implementation
agents build more user-visible web admin, mobile, or setup flows.

Exit target:

```text
Datarun has an accepted product specification for the one-organization,
default-workspace Product Candidate 1 experience, with user-visible scope,
actors, journeys, language, acceptance criteria, exclusions, and successor
implementation rows.
```

This is product specification work. It is not runtime implementation, not
tenant-aware architecture, not mobile OIDC implementation, not production
approval, and not a contract/schema change.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-082
   through NW-087 and NW-093 through NW-098, plus the trigger map
6. `docs/agent-working-surface/artifacts/product-candidate-1-orchestration-note.md`
7. `docs/agent-working-surface/artifacts/NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md`
8. `docs/agent-working-surface/artifacts/NW-083-tenant-workspace-vocabulary-and-managed-isolation-boundary.md`
9. `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`
10. `docs/agent-working-surface/artifacts/product-admin-surface-forward-plan.md`
11. `docs/agent-working-surface/operational-ux-layering-companion.md`
12. `docs/specifications/README.md`
13. `docs/specifications/product/README.md`
14. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
15. `docs/specifications/platform/assignment-scope-and-administration.md`
16. `docs/specifications/platform/configuration-package-and-shapes.md`
17. `docs/specifications/platform/expression-language.md`
18. `docs/specifications/platform/production-auth-principal-binding.md`
19. Accepted mobile rows in the backlog for NW-055 and NW-059 through NW-062

Open operations docs only if the product spec tries to claim real production,
real user/data readiness, provider/region, support, notification, or approval.
Use CDL slices only if a proposed product behavior changes architecture,
contracts, authority, sync/access, or envelope meaning.

## Product Starting Point

Use the NW-083 route:

```text
one customer-facing Organization
-> one managed single-tenant Datarun deployment
-> one internal/default Workspace
```

Use `Organization` as the default top-level customer-facing term unless the
spec records an explicit reason to escalate to Hamza. Keep `Tenant` and
multi-`Workspace` out of user-visible PC1 copy by default.

## Questions To Answer

1. Who are the first Product Candidate 1 actors and operating contexts:
   setup owner, config reviewer/approver, assignment coordinator, field user,
   reviewer/resolver, scoped observer, support operator, deployment owner?
2. Which journeys are in scope for the first candidate:
   login/admin shell, setup/config author/validate/review/approve/publish,
   assignment create/end, mobile get-work/capture/sync/correction, and
   resolver-visible single-flag review?
3. Which journeys are intentionally out of scope:
   multi-workspace UI, pooled SaaS, reporting/export, retention/security,
   conflict batch/automation, online principal-binding admin, mobile OIDC
   implementation, real production approval, new scopes, entity lifecycle?
4. What product language should be used for organization, setup, published
   setup, responsibilities, assignment, sync states, missing setup/forms,
   missing assignment, corrections, warnings, and handoff?
5. Which accepted platform specs constrain each user-visible behavior?
6. Which implementation rows can start after the product spec, and which
   require separate decisions first?
7. What acceptance criteria prove the candidate experience without claiming
   real production readiness?

## Expected Durable Output

Create:

```text
docs/specifications/product/product-candidate-1.md
```

Also update:

- `docs/specifications/product/README.md`;
- `docs/agent-working-surface/platform-next-work-backlog.md` row NW-084;
- `docs/status.md` Current Routing only if the accepted product spec changes
  current routing standing.

The product specification must include:

- required metadata from `docs/documentation-organization.md`;
- product scope and non-goals;
- actor/context table;
- user-visible terminology table;
- in-scope journeys and states;
- explicit out-of-scope/deferred surfaces;
- platform/contract/operations guardrails;
- Product Candidate 1 acceptance criteria;
- implementation successor table;
- stop conditions and escalation triggers.

## Guardrails

- Do not implement code, tests, UI, schemas, contracts, migrations, auth,
  tenant storage, mobile local partitioning, operations evidence, or real
  production approval.
- Do not expose Tenant or multiple Workspaces as product behavior unless you
  stop and route the decision.
- Do not claim real production readiness, real user/data approval, compliance,
  retention/security, no-local-retention, encryption, or support coverage.
- Do not add tenant/workspace fields to event envelopes or productize tenant
  internals from NW-083.
- Do not use location hierarchy, org units, assignment scope, UI-selected
  organization, IdP groups/claims, or a generic admin role as tenant
  isolation.
- Do not bundle mobile OIDC, reporting/export, retention/security, conflict
  automation, online principal-binding admin, new scopes, or production
  approval into this spec.

## Verification

Run:

```bash
git diff --check
```

Also verify:

- the product spec is indexed from `docs/specifications/product/README.md`;
- the NW-084 row links the prompt, product spec, and verification;
- no runtime code, schemas, fixtures, migrations, contracts, BAR, CDL,
  operations evidence, or unrelated docs changed;
- the product spec does not treat NW-082 or NW-083 artifacts as runtime,
  contract, or production authority.

## Commit Flow

If commits are requested, use:

```text
docs(product): specify product candidate 1
```

Use the NW trailer:

```text
NW: NW-084
```

Do not start implementation until NW-084 is accepted and the relevant
successor implementation row is selected.
