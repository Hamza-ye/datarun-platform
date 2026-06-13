# NW-063 Agent Prompt: Analyze Production Deployment And Operations Readiness

You are working in `/home/hamza/datarun-platform`.

## Goal

Produce a bounded production deployment and operations-hardening analysis that
selects one recommended first reference deployment and defines the runbook,
rehearsal, evidence, and successor work needed to operate it repeatably.

Exit target:

```text
Datarun has one accepted operations-readiness map that separates deployer
policy, platform specification, engineering/tooling, and architecture gaps,
then routes a bounded runbook/rehearsal successor without claiming turnkey
production readiness.
```

This is analysis and routing work only. Do not implement infrastructure,
runtime behavior, schemas, APIs, migrations, authentication changes, mobile
behavior, or production configuration in this slice.

## Recommended Work Shape

Use one analysis artifact followed by separately accepted successors. Do not
create a workshop, role chain, stage chain, or gate-review packet set.

1. Inventory what can be deployed and operated today.
2. Compare realistic reference-deployment options and their tradeoffs.
3. Classify every gap through the decision-anchor gap-routing playbook.
4. Recommend one first reference deployment and explicit policy assumptions.
5. Define the runbook sections, rehearsal scenarios, and evidence required.
6. Add only the bounded successor rows needed to write/tool/rehearse the
   accepted option.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/documentation-organization.md`
5. `docs/commit-workflow.md`
6. `docs/specifications/README.md`
7. `docs/operations/README.md`
8. `docs/agent-working-surface/decision-anchor-layer/README.md`
9. `docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md`
   - Focus on DEC-EVENT-01, DEC-AUTH-02, DEC-AUTH-03, DEC-AUTH-05,
     DEC-CONFIG-03, DEC-CONFIG-08, DEC-PROJECTION-01, and DEC-BOUNDARY-01.
10. `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
   - Focus on classification, closure trace, architecture escalation, and
     GAP-OPS-01.
11. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-001 through BAR-015 and BAR-104.
12. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-040, NW-054 through NW-056, NW-059 through NW-063.
13. `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`
   - Read deployment, provisioning, operations, and successor-route sections.
14. `docs/workshops/first-deployment/summary.md`
15. `docs/scenarios/scenario-user-fit-packets/foundational-product-fit-readiness-and-validation-matrix.md`
    - Read operational policy, device loss, retention, access, setup, and
      reporting freshness rows.
16. Implemented/deployment surfaces for inspection:
    - `docker-compose.yml`
    - `docker-compose.test.yml`
    - `server/Dockerfile`
    - `server/src/main/resources/application.properties`
    - `.github/workflows/server-ci.yml`
    - `server/pom.xml`
    - `server/src/main/resources/db/migration/`
17. Production provisioning and bootstrap surfaces discovered from:
    - `PrincipalBindingManifestProvisioner`
    - `PrincipalBindingProvisioningRunner`
    - assignment bootstrap/config publication code and tests
    - application environment/property references
18. Contracts only when a proposed procedure crosses them:
    - `contracts/sync-protocol.md`
    - `contracts/config-package.schema.json`
19. Use `scripts/query_cdl.py` only for narrow authority checks exposed by the
    analysis. Do not load the whole CDL.

Do not read broad architecture/history docs unless a directly read source
exposes drift that cannot be resolved from this packet.

## Expected Artifact

Create:

```text
docs/agent-working-surface/artifacts/NW-063-production-deployment-ops-hardening-map.md
```

The artifact must include:

### 1. Current Asset Inventory

Use a table:

```text
Surface | Current asset | Proven capability | Missing production procedure or control | Classification | Route
```

Cover at least:

- server image and runtime configuration;
- PostgreSQL provisioning and persistence;
- TLS, DNS, and reverse proxy;
- secrets and credential rotation;
- OIDC/JWKS provider configuration;
- principal-binding manifest application and audit;
- config validation/publication;
- assignment bootstrap;
- Flyway migration, upgrade, compatibility, and rollback posture;
- backup, restore, disaster recovery, RPO, and RTO;
- health checks, logs, metrics, alerting, capacity, and SLOs;
- incident response, support escalation, and evidence retention;
- field-device onboarding, sync verification, and known device-loss boundary.

### 2. Reference Deployment Options

Compare at least:

- a single-host/container deployment with explicit external TLS and durable
  PostgreSQL operations;
- an orchestrated deployment such as Kubernetes;
- a managed application/database deployment.

For each option state prerequisites, operator burden, portability, recovery
model, observability needs, unsupported assumptions, and what repository work
would be required. Recommend exactly one first reference target. Do not
implement all options.

### 3. Decision And Classification Register

For every unresolved item classify it as:

- product/problem evidence;
- architecture decision;
- platform-spec detail;
- implementation/tooling;
- operational policy.

Record the owner, input needed, durable output home, and whether it blocks the
first rehearsal. Escalate only if the proposal changes accepted runtime
semantics, contracts, authority, event truth, sync/access behavior, or
configuration boundaries.

### 4. Runbook And Rehearsal Outline

Define the required runbook sections and rehearsal evidence. Include at least:

- clean install and environment validation;
- initial database migration;
- TLS/provider/secrets setup;
- principal-binding manifest dry run/application;
- config publication and assignment bootstrap;
- field-device connection and sync smoke test;
- backup and restore into a clean environment;
- application and schema upgrade;
- failed deployment or migration response;
- credential/JWKS/secret rotation;
- monitoring alert and incident triage;
- operator handoff and rollback/forward-fix decision.

For every rehearsal state prerequisites, commands or procedure shape, expected
observable result, retained evidence, failure stop condition, and cleanup.

### 5. Recommendation And Successors

Recommend a bounded sequence, normally:

1. write the accepted reference-deployment runbook and supporting
   configuration/tooling;
2. execute a clean-environment deployment/backup/restore/upgrade rehearsal;
3. close only the evidence-backed operations claim.

Add no more than four successor rows. Separate operational policy from
implementation/tooling when they have different acceptance evidence. Do not
create successors for speculative alternate deployment targets.

Route accepted successor outputs to:

- deployment-owner choices under `docs/operations/policies/`;
- executable deployment/recovery procedures under
  `docs/operations/runbooks/`;
- rehearsal plans and dated execution evidence under
  `docs/operations/rehearsals/`;
- stable platform behavior needed by those procedures under
  `docs/specifications/platform/`;
- implementation/tooling changes under `docs/implementation/`, code, tests,
  and deployment assets.

Use semantic filenames for durable outputs. Keep NW prefixes on prompts and
the non-binding NW-063 map only.

### 6. Verification Ledger

Record files inspected, commands run, assumptions, evidence reused, tests not
rerun, drift found, and unresolved decisions.

## Questions To Answer

1. What is the smallest honest production reference deployment?
2. Which choices must be made before a runbook can be executable?
3. Which procedures can use current behavior unchanged?
4. Which gaps require repository tooling or runtime implementation?
5. What RPO/RTO, secrets, monitoring, support, and ownership policies are
   deployment decisions rather than platform semantics?
6. What exact evidence is needed before changing
   `operator_deployable_with_constraints` to a stronger claim?
7. Which unresolved items route to NW-054 or an authentication/admin decision
   instead of this lane?

## Guardrails

- Treat BAR-001 through BAR-015 and BAR-104 as accepted kernel standing, not
  turnkey operations evidence.
- Do not use `docker-compose.yml` as production hardening evidence.
- Preserve append-only server event history and projection rebuildability.
- Preserve assignment-derived access, explicit principal binding, and
  provider group/claim/JWT `actor_id` non-authority.
- Preserve normal sync watermarks and the separate subject-history surface.
- Keep device expiry, decommissioning, sealed-partition recovery, local
  encryption, redaction, and token/session retention in NW-054/BAR-106 unless
  this analysis only names their operational dependency.
- Do not invent migration rollback support. Determine the actual posture and
  distinguish forward fix, application rollback, and database restore.
- Do not choose vendor-specific infrastructure without recording portability
  and ownership consequences.
- Do not claim production readiness without a successful clean-environment
  rehearsal and retained evidence.

## Forbidden Work

- Do not edit runtime code, tests, schemas, migrations, contracts, Docker
  packaging, CI, deployment manifests, environment files, or secrets.
- Do not write the final production runbook in this analysis slice.
- Do not change CDL, add an IDR, accept a BAR row, or resolve NW-054.
- Do not combine web admin authentication, mobile OIDC login, reporting,
  retention/security implementation, or conflict automation into this lane.
- Do not create a broad production-readiness program or recreate the retired
  first-deployment workshop chronology.

## Verification

Run:

```bash
git diff --check
```

Inspect Markdown tables and verify every proposed successor has one
classification, durable output, evidence requirement, and stop condition.

## Backlog And Status Updates

If the artifact lands:

- mark NW-063 `accepted`;
- update its exit condition with the artifact, recommendation, and evidence;
- update `docs/status.md` Current Routing with the selected reference
  deployment and next bounded successor;
- add only the accepted recommendation's successor rows/prompts;
- add the artifact to `docs/agent-working-surface/artifacts/README.md`;
- update GAP-OPS-01 with the selected route;
- require every durable successor output to be listed in its nearest
  specification or operations `README.md` index.

Do not mark a successor accepted until its own runbook/tooling/rehearsal
evidence lands.

## Commit Boundary

The NW-063 route is a separate prior commit. Follow
`docs/commit-workflow.md`.

If the analysis reaches a complete recommendation and no human/steward option
acceptance remains, use the docs-only exception for one outcome/acceptance
commit:

```text
docs(ops): map production deployment readiness

NW: NW-063
```

The commit should contain only the NW-063 artifact and tightly related routing
updates.

If deployment-owner input is still required to select the reference target,
commit the artifact with NW-063 left `in_review`, then use a separate
`docs(status): accept production deployment target` commit after that input.

No checkpoint belongs to this slice.

## Stop And Report

Stop and report if:

- current code, contracts, BAR, or NW evidence disagree on a procedure needed
  for the recommended deployment;
- an executable runbook requires a runtime contract or authority change;
- a backup/restore or migration claim cannot be verified honestly;
- the first reference target cannot be selected without deployment-owner
  input on hosting, RPO/RTO, compliance, or operational staffing;
- the route expands into admin auth, mobile login, NW-054 security behavior,
  or multiple deployment targets.
