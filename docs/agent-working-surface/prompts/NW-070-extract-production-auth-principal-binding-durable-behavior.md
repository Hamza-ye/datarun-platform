# NW-070 Agent Prompt: Extract Production Auth Principal-Binding Durable Behavior

You are working in `/home/hamza/datarun-platform`.

## Goal

Create the durable platform/security specification route for accepted
production authentication, principal-to-actor binding, bearer-bound actor
resolution, OIDC/JWKS validation, deployment-managed principal-binding
provisioning, and group/claim non-authority behavior currently scattered across
IDRs, BAR evidence, module boundaries, operations docs, deployment tooling, and
tests.

This is a specification extraction task. It is not an implementation task, not
a behavior-change task, not an operations runbook rewrite, and not an old-doc
cleanup pass.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/agent-working-surface/platform-next-work-backlog.md` NW-070
6. `docs/agent-working-surface/artifacts/architecture-classification-drift-audit.md`
7. `docs/agent-working-surface/artifacts/idr-durable-surface-routing-audit.md`
8. `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
9. `docs/implementation/module-interfaces.md` sections for Scope Resolver,
   Authenticated Actor Resolver, Production Runtime Boundary, Sync Surfaces,
   One-Shot Provisioning, and Mobile Actor Session And Local Store
10. BAR-003, BAR-006, BAR-007, BAR-104, and BAR-108 in
    `docs/agent-working-surface/baseline-acceptance-register.md`
11. `docs/decisions/idr-016-actor-token-table.md`
12. `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
13. `docs/decisions/idr-028-production-principal-binding-administration.md`
14. `contracts/sync-protocol.md`
15. `docs/operations/policies/first-reference-deployment-policy.md`
16. `docs/operations/runbooks/production-deployment-runbook.md`
17. `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
18. `docs/operations/rehearsals/2026-06-17-keycloak-jwks-rotation-adapter.md`
19. Relevant implementation/test evidence:
    - `server/src/main/java/dev/datarun/server/authorization/AuthenticatedActorResolver.java`
    - `server/src/main/java/dev/datarun/server/authorization/AuthProperties.java`
    - `server/src/main/java/dev/datarun/server/authorization/AuthPrincipalBindingRepository.java`
    - `server/src/main/java/dev/datarun/server/authorization/JwtPrincipalTokenValidator.java`
    - `server/src/main/java/dev/datarun/server/authorization/OidcJwksTokenValidator.java`
    - `server/src/main/java/dev/datarun/server/authorization/PrincipalBindingManifestProvisioner.java`
    - `server/src/main/java/dev/datarun/server/authorization/PrincipalBindingProvisioningRunner.java`
    - `server/src/main/java/dev/datarun/server/authorization/ActorTokenInterceptor.java`
    - `server/src/main/java/dev/datarun/server/authorization/AuthMeController.java`
    - `server/src/main/java/dev/datarun/server/sync/SyncController.java`
    - `server/src/test/java/dev/datarun/server/authorization/ProductionAuthIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/authorization/LocalJwtAuthCompatibilityIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/authorization/ScopeFilteredSyncIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/authorization/AssignmentContainmentIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/integrity/ConflictResolutionIntegrationTest.java`

Use CDL slices only if one of the sources above exposes a concrete authority
conflict. Do not read or rewrite the whole CDL.

## Expected Output

Create or update only the durable surfaces needed for this slice:

- platform/security specification under
  `docs/specifications/platform/production-auth-principal-binding.md`;
- `docs/specifications/platform/README.md` index entry;
- backlog/status fold-forward only if the NW is fully accepted after
  verification and review.

The platform spec should use the required document metadata from
`docs/documentation-organization.md` and should record accepted behavior for:

- dev-token mode as development compatibility, not production authority;
- production auth modes and the shared authenticated-actor resolver boundary;
- explicit `(issuer, subject) -> actor_id` binding semantics;
- OIDC/JWKS issuer, audience, JWKS, key, time, and algorithm validation
  boundary at the level already accepted by tests;
- `/api/auth/me` actor resolution behavior and mobile authorship alignment;
- bearer-bound push/config/pull/subject-history/assignment/conflict API actor
  context where currently accepted;
- sync push actor-ref equality and pre-persistence rejection of missing,
  malformed, system-authored, or mismatched client human authorship;
- deployment-managed principal-binding provisioning: manifest application,
  create, rotate, deactivate, rebind/correction, idempotency, concurrency
  serialization, append-only operation history, and active lookup support rows;
- the difference between platform/security behavior and operations-owned owner
  process, secrets, commands, evidence, and runbook execution;
- explicit non-authority of groups, roles, realm/client roles, resource claims,
  custom claims, JWT `actor_id`, request-body actor ids, mobile-selected actors,
  online admin UI/API claims, and deployment/product labels.

## Required Decisions Inside The Slice

Decide and state explicitly:

- which parts are owned by existing contracts, especially
  `contracts/sync-protocol.md`;
- which parts are accepted platform/security behavior that need durable prose
  because contracts and operations docs do not express the boundary;
- which parts remain operational policy/procedure/rehearsal evidence and must
  stay in `docs/operations/`;
- which parts remain implementation evidence only, such as exact Java classes,
  property names, repository details, or deployment command wiring;
- whether `contracts/sync-protocol.md` already carries enough process-boundary
  auth language for push, pull, config, and subject-history. Update it only if
  a narrow documentation-only trace correction is required; do not change
  protocol semantics;
- how IDR-016/027/028 remain historical implementation provenance and inputs
  after extraction.

## Guardrails

- Do not change runtime code, JSON schemas, fixtures, tests, BAR, CDL, old IDR
  text, phase files, operations policy, runbook, or rehearsal records.
- Do not add online production binding-admin APIs, production admin UI,
  operator SQL authority, direct IdP group/claim authority, new actor authority
  sources, new scope mechanisms, assignment-admin authority, resolver authority,
  emergency override authority, or envelope fields/types.
- Do not treat provider credentials as sufficient for access; assignment,
  scope, role-action, and resolver authority remain existing platform
  decisions.
- Do not turn JWT `actor_id`, groups, roles, resource claims, or custom claims
  into direct platform authority or binding-admin authority.
- Do not move owner process, secret handling, command recipes, backup/restore,
  monitoring, rotation, or rehearsal proof out of operations docs into this
  platform spec.
- Do not use deployment/product labels as platform authority primitives.
- Stop if extraction appears to require a new authority source, changed auth
  semantics, changed sync access behavior, or weakened group/claim
  non-authority.
- Do not duplicate protocol tables or implementation source; link to contracts,
  operations docs, and code/tests.

## Verification

Run:

```bash
git diff --check
```

Also verify that:

- `docs/documentation-organization.md` and `docs/commit-workflow.md` are not
  changed;
- the new durable spec is indexed from
  `docs/specifications/platform/README.md`;
- links to contracts, BAR/status evidence, IDRs, operations docs, module
  boundaries, and test evidence are valid by path search;
- no runtime code, schema, fixture, test, old IDR, phase file, BAR, CDL,
  operations policy/runbook/rehearsal record, or status/backlog diff exists
  unless explicitly reviewed and justified.

## Commit Flow

Use separate commits for route, durable specification, optional contract
documentation, and status acceptance if commits are requested. Include:

```text
NW: NW-070
```

Do not mark NW-070 accepted until durable outputs and verification are
complete, reviewed, and folded forward.
