# NW-139 - Prepare PC2 Synthetic Web-Admin Session Provisioning

## Goal

Prepare, inside the retained isolated PC2 lab stack, a synthetic web-admin
principal/session/provisioning path suitable for a later PC2 live browser
walkthrough proof.

This is still environment/provisioning preparation. It is not the live browser
proof, not production approval, and not runtime feature implementation.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-126-pc2-synthetic-lab-environment-reconciliation.md`
- `docs/agent-working-surface/artifacts/NW-125-pc2-synthetic-lab-proof-environment.md`
- `deploy/reference/provisioning-inputs.md`
- `deploy/reference/README.md`
- `docs/agent-working-surface/validation-matrix.md`
- retained isolated PC2 state on the app host:
  `/home/nmcp/datarun-platform-pc2-src`,
  compose project `datarun-pc2-nw125`, app port `127.0.0.1:28080`,
  DB port `127.0.0.1:25432`

Use the gap-routing playbook only if the work would require new authority
semantics, contract/schema changes, reporting/export breadth, resolver
reassignment, tenant/control-plane work, retention/security claims, or another
stop-trigger surface.

## Required First Steps

1. Inspect R12 continuity again before changing retained PC2 state.
2. Confirm fixed-IP and/or hostname SSH access.
3. Confirm the retained PC2 stack still matches NW-126 standing:
   source revision, compose project, image, ports, volume, network, health,
   `/web-admin/operational`, and `/web-admin/login`.
4. Preserve R12 evidence, runtime config, monitoring evidence,
   DB recovery/backup evidence, token cleanup standing, and Keycloak standing.

## Allowed

- Read-only R12 inspection.
- Cleanup or completion only of isolated `datarun-pc2-nw125` state.
- Add or update isolated synthetic PC2 runtime/provisioning files on the lab
  host when they do not touch R12 or product source.
- Use accepted deployment-managed provisioning commands to apply synthetic
  principal binding and reviewed config to the isolated PC2 database.
- Configure a synthetic OIDC/browser-login path only if it is isolated,
  non-sensitive, and does not mutate or repurpose R12 Keycloak state.
- Update status, backlog, and artifact index.
- Add one successor prompt only if exactly one next route is selected.

## Forbidden

No PC2 live browser walkthrough proof inside NW-139.

No real users/data, production approval, auth bypass, dev-login shortcut,
runtime app code, Dockerfile/build-tooling changes, schemas, tests, CI,
product/platform specs, BAR, CDL, gap register, reporting/export, queue/list
review, automation, resolver reassignment, tenant/control-plane, mobile code,
server feature implementation, or all-PC proof.

Do not mutate R12 reference deployment state, R12 evidence roots, R12 runtime
config, R12 monitoring evidence, R12 DB recovery/backup evidence, R12 token
cleanup evidence, or R12 Keycloak realm/client/user state.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-139-pc2-synthetic-web-admin-session-provisioning.md
```

The artifact must state:

- whether the synthetic web-admin session/provisioning path is `READY` or
  `NOT_READY`;
- app URL or SSH tunnel path for future browser proof;
- whether `/web-admin/operational` is reachable;
- whether `/web-admin/login` starts a configured synthetic OIDC flow;
- whether a synthetic principal binding exists;
- whether reviewed config grants only the needed synthetic web-admin commands;
- cleanup or retained synthetic state;
- R12 continuity standing before and after;
- exactly one next route.

Expected next route:

- If ready, select a separate PC2 live browser walkthrough proof route.
- If still blocked, record `NOT_READY` and the exact blocker.
- If image or dependency fetching is the blocker, select a separate
  implementation-tooling route.

## Validation

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-139" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-139-pc2-synthetic-web-admin-session-provisioning.md
grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-139-pc2-synthetic-web-admin-session-provisioning.md
```

Runtime automated tests are skipped unless NW-139 changes runtime code, which
is forbidden.
