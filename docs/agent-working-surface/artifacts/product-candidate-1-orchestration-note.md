# Product Candidate 1 Orchestration Note

Status: non-binding stewardship note
Document type: routing_artifact
Owner: project shepherd
Source: NW-082 accepted non-binding routing artifact, NW-083 accepted tenancy routing, and NW-084 accepted product specification
Authority: none; preserves routing continuity under the CDL, contracts, accepted specifications, BAR, NW backlog, and operations documents
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/agent-working-surface/artifacts/NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md`; `docs/agent-working-surface/artifacts/NW-083-tenant-workspace-vocabulary-and-managed-isolation-boundary.md`; `docs/specifications/product/product-candidate-1.md`; `docs/agent-working-surface/platform-next-work-backlog.md`; `docs/agent-working-surface/artifacts/2026-06-18-multi-tenancy-architecture-analysis.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md`

## Current

- NW-082 is accepted only as a non-binding routing artifact. It does not accept
  Product Candidate 1 product behavior, tenant-aware runtime architecture,
  contract authority, implementation, or production readiness.
- NW-083 is accepted for routing only: the reversible PC1 lane is one
  customer-facing Organization mapped to one managed single-tenant Datarun
  deployment with one internal/default Workspace.
- NW-084 is accepted as the Product Candidate 1 product specification at
  `docs/specifications/product/product-candidate-1.md`.
- The next actionable work is NW-086: route and run the production web admin
  login/session boundary as the first PC1 implementation lane.
- Sidecar agents collected bounded evidence for product vocabulary,
  platform/contracts, and ops/control-plane reversibility. Final decisions
  remained local to the orchestrator.

## Next

Route and run:

```text
NW-086: Implement production web admin login and session boundary
```

NW-086 should use a bounded prompt and implement only browser OIDC/JWKS login,
server session, CSRF, logout, expiry, principal-binding revalidation, admin
actor context, login/session audit, and a minimal protected shell. Config UI
expansion, command policy, assignment administration, mobile login, and real
production remain separate rows.

## Blocked Until Routed

- Admin command policy and scoped shell gates until NW-087 lands.
- Productized setup/config workflow and assignment admin UX until their
  implementation rows are selected after NW-087.
- Tenant-aware auth, membership, storage, sync/config context, admin context,
  and mobile local partitioning.
- Pooled multi-tenant storage and tenant isolation test harness.
- Real-production approval for real users or real organizational data.

## Must Not Do Yet

- Do not implement code or change schemas/contracts from NW-082, NW-083, or
  this note.
- Do not use locations, org units, assignment scope, UI-selected organization,
  or IdP claims as tenant isolation.
- Do not add tenant/workspace fields to the event envelope.
- Do not collapse admin roles into a generic admin.
- Do not claim real production readiness from synthetic reference-deployment
  evidence.
- Do not fold config UI expansion, admin command gates, mobile OIDC,
  reporting/export, retention/security, conflict automation, online
  principal-binding admin, new scopes, or production approval into NW-086.
