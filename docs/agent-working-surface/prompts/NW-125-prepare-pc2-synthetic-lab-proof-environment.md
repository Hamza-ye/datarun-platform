# NW-125 - Prepare PC2 Synthetic Lab Proof Environment

## Goal

Prepare a bounded, synthetic/non-sensitive lab environment that can support a
future PC2 live browser walkthrough proof from current `main`.

This is environment/provisioning preparation for owner-review evidence. It is
not the live PC2 proof itself, not production approval, and not a runtime
feature implementation.

## User Value / Why Now

NW-124 accepted the evidence-supported PC2 proof packet but explicitly marked
live browser/manual runtime inspection as `NOT_RUN`. The owner now requires
live browser proof before considering PC2 closed.

Read-only lab inspection shows the existing reference deployment is reachable
again, but it is not currently PC2-suitable:

- SSH works through `nmcp@datarun-app.lab`, `nmcp@datarun-db1.lab`, and
  `nmcp@keycloak.lab`.
- The current app container is the accepted R12 reference deployment image from
  source revision `757d6c8d386f760693157c3e1388c877efdf6a0e`, which predates
  the accepted PC2 web-admin review implementation.
- `/web-admin/operational` currently returns `404` on the app host loopback
  port, so the deployed lab app cannot prove the PC2 browser journey as-is.
- The app service is bound to app-host loopback ports, so browser access from
  the operator VM will likely require an SSH tunnel.

The next useful step is therefore not to run proof against the old R12 app. It
is to prepare an isolated PC2 proof environment without disturbing retained
operations rehearsal evidence or the accepted reference deployment standing.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/artifacts/NW-124-pc2-synthetic-walkthrough-proof.md`
- accepted PR #38 / NW-122 implementation and validation evidence from
  status/backlog
- `docs/operations/rehearsals/2026-06-18-production-deployment-r12-fresh-session-rerun.md`
- `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
- `docs/operations/runbooks/production-deployment-runbook.md`
- `docs/agent-working-surface/validation-matrix.md`
- lab runtime setup files only as needed, such as `deploy/reference/`,
  `docker-compose.yml`, and existing lab override/config surfaces

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if preparation would require reporting, queue/multi-item review,
automation, resolver reassignment, resolver eligibility broadening, real
production, retention/security, tenant/control-plane, contracts, sync/access,
or another gap-trigger surface.

Do not open broad architecture history unless a stop condition fires.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-125-pc2-synthetic-lab-proof-environment.md
```

The artifact should:

- state whether an isolated PC2 synthetic lab proof environment was prepared;
- record the lab hosts inspected or used;
- record the current retained R12 reference deployment standing before any
  preparation;
- identify how the PC2 environment is isolated from the accepted R12 reference
  deployment, retained evidence roots, active runtime config, DB roles, and
  monitoring evidence;
- identify the source commit/image used for the PC2 environment;
- record the app URL or SSH tunnel command intended for browser access;
- record whether `/web-admin/operational` is reachable in the prepared
  environment;
- record whether a synthetic principal/session/provisioning path exists for a
  later live browser proof;
- record any blocker honestly as `NOT_READY` rather than treating environment
  access as proof;
- confirm no real users or real organizational data were used;
- confirm no real-production approval was granted;
- recommend exactly one next route: PC2 live browser walkthrough proof, a
  small bounded environment/provisioning follow-up, a standard
  implementation-tooling route, NW-093 real-use preparation, or park.

## Operations Continuity Boundary

Preserve accepted operations rehearsal continuity.

Do not mutate, delete, overwrite, or repurpose:

- retained evidence under `/opt/datarun-lab/evidence/`;
- the accepted R12 reference deployment evidence roots;
- `/opt/datarun-lab/runtime-config-nw076-g2`;
- accepted R12 token-cleanup standing;
- accepted monitoring/alert evidence;
- DB1 recovery/backup evidence or retained disabled rotation roles.

Prefer an isolated PC2 proof stack, for example a separate compose project,
ports, runtime config directory, database/schema/volume, and evidence root. If
isolation is not available without changing the accepted reference deployment,
stop and recommend a separate bounded environment/provisioning route.

## Docker / Maven Cache Boundary

Docker/Maven cache work is not selected by NW-125.

The current reason for NW-125 is lab environment suitability, not local build
convenience. The lab has previously built/deployed the reference image during
accepted rehearsals, so do not add Dockerfile or BuildKit cache changes inside
NW-125.

If server-image dependency fetching blocks preparation of the PC2 lab
environment, record the blocker and recommend a separate standard
implementation-tooling NW. Such a route may consider a Docker BuildKit Maven
cache mount only if it proves final runtime image behavior and packaged
resources remain unchanged.

## Allowed Changes

- Add the NW-125 environment-prep artifact.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Update `docs/agent-working-surface/artifacts/README.md` after the artifact
  exists.
- Add one successor prompt only if NW-125 selects a concrete next route.
- Run read-only lab inspection commands.
- Prepare an isolated synthetic/non-sensitive PC2 proof environment only if it
  does not disturb accepted operations rehearsal evidence or the active R12
  reference deployment.

## Forbidden Changes

No application runtime code, tests, contracts, schemas, migrations, CI,
validation policy, BAR, CDL, gap-register changes, product spec changes,
platform spec changes, Dockerfile/build-tooling changes, real-production
approval, reporting/export, conflict queue/list workflow, batch review,
resolver reassignment, automation, resolver eligibility broadening,
retention/security promises, entity lifecycle, tenant/control-plane work,
mobile code, or server/web-admin implementation.

Do not use real users or real organizational data. If real-use preparation
becomes selected, stop and route through NW-093 before continuing.

## Acceptance Criteria

NW-125 is accepted only when:

- one PC2 lab-environment preparation artifact exists;
- the artifact states whether a PC2-suitable lab environment is ready;
- accepted R12 reference deployment/evidence continuity is preserved;
- no proof is claimed merely from SSH access or old reference-app availability;
- the next route is exactly one of the allowed successor outcomes;
- real-production standing remains blocked unless NW-093 is selected later;
- status/backlog reflect the resulting route and no active implementation gate
  is opened by accident;
- validation evidence follows the validation matrix for docs-only and
  manual/ops evidence.

## Validation

Always run docs validation for the artifact/control-surface changes:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-125" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-125-pc2-synthetic-lab-proof-environment.md
grep -n "R12 reference deployment" docs/agent-working-surface/artifacts/NW-125-pc2-synthetic-lab-proof-environment.md
```

If lab preparation happens, the artifact must also record:

- exact inspection/preparation commands or actions;
- environment name and commit/image reference;
- isolation boundary from the accepted R12 reference deployment;
- app URL or SSH tunnel command intended for later browser proof;
- readiness of `/web-admin/operational`;
- cleanup state or intentionally retained synthetic lab state.

Runtime automated tests are skipped unless NW-125 changes runtime code, which
is forbidden.

## Stop Conditions

Stop and report if the work requires:

- real users or real organizational data;
- real-production approval inside NW-125;
- mutation of accepted R12 evidence/config/runtime standing;
- application runtime code, Dockerfile/build-tooling, schema, contract,
  migration, CI, or validation-policy changes;
- a new local dev-login shortcut, principal-binding shortcut, or auth bypass;
- reporting dashboards, exports, imports, warehouses, analytics, broad read
  APIs, completeness semantics, or drilldown;
- queue/list/multi-item review, broad conflict console, filters, batch review,
  resolver reassignment, automation, auto-resolution, or flag reporting;
- resolver eligibility broadening beyond exact stored `designated_resolver`
  equality for the opened item;
- retention/security/offboarding promises;
- entity lifecycle;
- tenant/control-plane work;
- BAR, CDL, or gap-register changes.

## Commit Boundary

Use an environment-preparation / owner-review evidence commit if NW-125 lands.
Do not combine NW-125 with live browser proof, runtime implementation,
Dockerfile/build-tooling changes, product-spec changes, platform-spec changes,
validation-policy changes, CI changes, BAR/CDL/gap-register updates, or
unrelated cleanup.
