# NW-126 - Reconcile And Complete PC2 Synthetic Lab Proof Environment

## Goal

Reconcile the interrupted NW-125 PC2 synthetic lab preparation and either
complete a PC2-suitable isolated lab proof environment or honestly classify it
as still `NOT_READY`.

This is environment/provisioning preparation only. It is not the PC2 live
browser proof and not runtime feature implementation.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-125-pc2-synthetic-lab-proof-environment.md`
- `docs/agent-working-surface/prompts/NW-125-prepare-pc2-synthetic-lab-proof-environment.md`
- `docs/operations/rehearsals/2026-06-18-production-deployment-r12-fresh-session-rerun.md`
- `docker-compose.yml`
- lab runtime state on `datarun-app.lab` / `192.168.1.213` only as needed

Do not open broad architecture history unless a stop condition fires.

## Required Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-126-pc2-synthetic-lab-environment-reconciliation.md
```

The artifact must record:

- whether hostname and fixed-IP lab access work;
- R12 reference deployment standing before any cleanup or continuation;
- any retained NW-125 PC2 source, container, image, volume, or compose state;
- whether that retained state was cleaned, retained, or completed;
- source commit/image used for the final PC2 environment, if any;
- isolation boundary from R12 evidence, config, runtime, DB, and monitoring;
- app URL or SSH tunnel path for the future browser proof;
- whether `/web-admin/operational` is reachable;
- whether a synthetic web-admin principal/session/provisioning path exists;
- cleanup or retained synthetic state;
- exact next route: live browser proof, another bounded env follow-up,
  implementation-tooling route, NW-093, or park.

## Required First Checks

Before touching any PC2 state:

1. Inspect `datarun-app.lab` by hostname if available, then fixed IP
   `192.168.1.213` if needed.
2. Record current R12 reference deployment state, including container/project,
   image digest/revision labels, R12 ports, and relevant health/protected
   endpoint standing.
3. Confirm no command targets retained R12 evidence roots, active runtime
   config, monitoring evidence, backup evidence, or accepted token cleanup
   standing.

## Allowed Work

- Read-only lab inspection.
- Cleanup of isolated NW-125 PC2 state only if it is under the separate
  PC2 paths/project named by NW-125, such as
  `/home/nmcp/datarun-platform-pc2-src` and `datarun-pc2-nw125`.
- Recreate or complete an isolated synthetic/non-sensitive PC2 environment from
  current `main` if R12 continuity is preserved.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Update `docs/agent-working-surface/artifacts/README.md` after the artifact
  exists.
- Add one successor prompt only if NW-126 selects a concrete next route.

## Forbidden Work

Do not run the PC2 live browser proof.

Do not mutate or repurpose the accepted R12 reference deployment, retained R12
evidence, active R12 runtime config, DB recovery/backup evidence, monitoring
evidence, or token cleanup standing.

No real users, real organizational data, production secrets, production
approval, application runtime code, Dockerfile/build-tooling changes, schemas,
tests, CI, product specs, platform specs, BAR, CDL, gap-register changes,
auth bypass, dev-login shortcut, reporting/import/export, queue/list/multi-item
review, automation, batch workflow, resolver reassignment, resolver eligibility
broadening, tenant/control-plane work, mobile code, or server/web-admin feature
implementation.

If Docker/Maven image dependency fetching blocks preparation, record that
blocker and recommend a separate standard implementation-tooling NW. Do not fix
Docker/Maven cache behavior inside NW-126.

## Acceptance Criteria

NW-126 is accepted only when:

- one reconciliation artifact exists;
- R12 continuity is inspected before PC2 cleanup or continuation;
- retained NW-125 PC2 state is accounted for;
- PC2 environment readiness is honestly stated as ready or `NOT_READY`;
- no live browser proof is claimed;
- no real-production standing changes;
- next route is exactly one allowed successor outcome;
- docs validation passes.

## Validation

```bash
git diff --check
rg "NW-126" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-126-pc2-synthetic-lab-environment-reconciliation.md
grep -n "R12 reference deployment" docs/agent-working-surface/artifacts/NW-126-pc2-synthetic-lab-environment-reconciliation.md
```

Runtime automated tests are skipped unless runtime code changes, which are
forbidden in NW-126.
