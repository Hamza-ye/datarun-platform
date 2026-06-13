# NW-065 Agent Prompt: Implement Reference Deployment And Provisioning Tooling

You are working in `/home/hamza/datarun-platform`.

## Goal

Make the accepted NW-063 reference target executable without using development
admin surfaces or changing accepted runtime semantics.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/implementation/module-interfaces.md` relevant server sections
4. `docs/commit-workflow.md`
5. NW-063 artifact
6. Accepted NW-064 policy
7. NW-065 backlog row
8. Current Docker, Maven, application properties, CI, migration, auth
   provisioning, config publication, and assignment bootstrap surfaces named
   by NW-063

## Required Outcomes

- Correct the clean image build so canonical root contract resources are
  packaged and verify their presence/startup behavior.
- Add a fail-closed production runtime profile or equivalent validation that
  rejects development auth/default credentials and missing required OIDC,
  database, and provisioning settings.
- Harden the image and reference deployment assets for the selected target:
  non-root runtime, controlled ports, external TLS proxy boundary, external
  PostgreSQL, secret injection placeholders, restart/shutdown behavior,
  resource limits, and immutable release identification.
- Add health/readiness and metrics/log signals sufficient for the accepted
  NW-064 policy, with tests.
- Add production-safe one-shot or deployment-managed tooling for:
  - principal-binding validation/application using the accepted provisioner;
  - config validation/publication using `DeployTimeValidator` and
    `ConfigPackager`;
  - the single allowed initial assignment bootstrap using
    `createInitialBootstrapAssignment(...)`.
- Add CI verification for the release image and required resources.
- Document implementation boundaries in the nearest existing implementation
  surface only where behavior actually changes.

The tooling may accept reviewed files and explicit operator identity/evidence
inputs. It must not create an online production admin API.

## Tests

At minimum:

- clean image/resource packaging test;
- production profile validation tests;
- health/readiness tests;
- provisioning success, idempotency, invalid-input, and one-time bootstrap
  tests;
- focused auth/config/assignment regression suites;
- full server suite;
- image build and startup smoke test against PostgreSQL 16.

Record exact commands and counts in the NW exit condition.

## Guardrails

- Preserve BAR-001 through BAR-015 and BAR-104.
- Preserve explicit principal binding and group/claim/JWT `actor_id`
  non-authority.
- Preserve assignment-derived authority and the one-time bootstrap boundary.
- Preserve config validation, package atomicity, and version semantics.
- Do not add Flyway undo migrations or claim database rollback.
- Do not implement Kubernetes, a managed-provider adapter, mobile login,
  production web admin auth, NW-054 behavior, reporting, or conflict
  automation.

## Commit Flow

Use independently reviewable implementation commits with `NW: NW-065`, then a
separate status acceptance commit after all evidence passes.

## Stop And Report

Stop if the target requires a new event/contract/authority source, online
admin APIs, direct database mutation as an operator procedure, or rollback
semantics unsupported by the current platform.
