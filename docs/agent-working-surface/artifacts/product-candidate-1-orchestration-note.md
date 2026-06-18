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
  Product Candidate 1 product behavior, tenant/workspace architecture,
  contract authority, implementation, or production readiness.
- The Product Candidate 1 lane now starts with NW-083: decide
  tenant/workspace vocabulary and the managed-isolation boundary.
- `docs/status.md` still contains older first-deployment "recommended next
  move" language. For Product Candidate 1 orchestration, use the NW backlog,
  NW-082 artifact, and NW-083 prompt as the current lane route.
- No subagents were dispatched for the NW-083 handoff; the evidence packet was
  bounded enough for local orchestration.

## Next

Run:

```text
docs/agent-working-surface/prompts/NW-083-decide-tenant-workspace-vocabulary-and-managed-isolation-boundary.md
```

NW-083 should explicitly choose the durable home before accepting anything:
CDL successor or architecture decision for structural isolation authority,
platform spec for settled platform-detail behavior, product spec for
user-visible terminology, operations/control-plane successor for managed
deployment choices, or a non-binding artifact if owner input remains missing.

## Blocked Until Routed

- Product Candidate 1 product specification and user-visible copy.
- Tenant-aware auth, membership, storage, sync/config context, admin context,
  and mobile local partitioning.
- Pooled multi-tenant storage and tenant isolation test harness.
- Real-production approval for real users or real organizational data.

## Must Not Do Yet

- Do not implement code or change schemas/contracts from NW-082 or this note.
- Do not use locations, org units, assignment scope, UI-selected organization,
  or IdP claims as tenant isolation.
- Do not add tenant/workspace fields to the event envelope.
- Do not collapse admin roles into a generic admin.
- Do not claim real production readiness from synthetic reference-deployment
  evidence.
- Do not fold mobile OIDC, reporting/export, retention/security, conflict
  automation, online principal-binding admin, new scopes, or production
  approval into NW-083.
