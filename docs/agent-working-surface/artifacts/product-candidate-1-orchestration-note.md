# Product Candidate 1 Orchestration Note

Status: non-binding stewardship note
Document type: routing_artifact
Owner: project shepherd
Source: NW-082 accepted non-binding routing artifact and NW-083 orchestration handoff
Authority: none; preserves routing continuity under the CDL, contracts, accepted specifications, BAR, NW backlog, and operations documents
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/agent-working-surface/artifacts/NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md`; `docs/agent-working-surface/platform-next-work-backlog.md`; `docs/agent-working-surface/prompts/NW-083-decide-tenant-workspace-vocabulary-and-managed-isolation-boundary.md`; `docs/agent-working-surface/artifacts/2026-06-18-multi-tenancy-architecture-analysis.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md`

## Current

- NW-082 is accepted only as a non-binding routing artifact. It does not accept
  Product Candidate 1 product behavior, tenant-aware runtime architecture,
  contract authority, implementation, or production readiness.
- NW-083 is accepted for routing only: the reversible PC1 lane is one
  customer-facing Organization mapped to one managed single-tenant Datarun
  deployment with one internal/default Workspace.
- The next actionable work is NW-084: specify Product Candidate 1 user-visible
  scope and acceptance criteria under `docs/specifications/product/`.
- Sidecar agents collected bounded evidence for product vocabulary,
  platform/contracts, and ops/control-plane reversibility. Final decisions
  remained local to the orchestrator.

## Next

Run:

```text
docs/agent-working-surface/prompts/NW-084-specify-product-candidate-1-user-visible-milestone.md
```

NW-084 should create and index
`docs/specifications/product/product-candidate-1.md`. It should use
Organization as the default top-level customer-facing term, keep Tenant and
multi-Workspace internals out of product copy, and avoid real-production
claims.

## Blocked Until Routed

- Product Candidate 1 product specification and user-visible copy until
  NW-084 lands.
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
- Do not fold mobile OIDC, reporting/export, retention/security, conflict
  automation, online principal-binding admin, new scopes, or production
  approval into NW-084.
