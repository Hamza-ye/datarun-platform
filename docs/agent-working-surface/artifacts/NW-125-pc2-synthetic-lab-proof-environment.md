# NW-125 PC2 Synthetic Lab Proof Environment

Status: accepted environment-preparation evidence; PC2 lab proof environment
`NOT_READY`
Owner: Hamza
Source: `docs/agent-working-surface/prompts/NW-125-prepare-pc2-synthetic-lab-proof-environment.md`
Date: 2026-06-21

## Result

NW-125 did not produce a PC2-suitable live proof environment.

The accepted R12 reference deployment was inspected before any preparation and
confirmed unsuitable for PC2 proof as-is because it predates the accepted PC2
web-admin attention review implementation and returns `404` for
`/web-admin/operational`.

An isolated PC2 stack preparation was started on the app lab host from current
`main`, using a separate source checkout, compose project, DB port, app port,
and database volume. The Docker build/run output was too large for the session
to retain fully, and subsequent SSH/DNS/direct-IP access to all lab hosts failed
before the PC2 stack state, `/web-admin/operational` reachability, synthetic
web-admin session path, or cleanup state could be verified.

Classification: `NOT_READY`.

Next route: `NW-126 - Reconcile and complete PC2 synthetic lab proof
environment`.

## Lab Hosts Inspected Or Used

| Host | Role | NW-125 use | Standing |
|---|---|---|---|
| `datarun-app.lab` / `192.168.1.213` | R12 app host and intended isolated PC2 app host | Inspected R12 containers and ports; cloned current `main`; created an isolated compose override; started an isolated `datarun-pc2-nw125` compose build/run attempt. | Later SSH by hostname failed with DNS errors and by direct IP timed out; PC2 state not verified. |
| `datarun-db1.lab` / `192.168.1.214` | R12 source DB host | Inspected as part of lab reachability and R12 continuity context. | Later SSH by hostname failed with DNS errors and by direct IP timed out. |
| `keycloak.lab` / `192.168.1.217` | R12 ops/Keycloak/monitoring host | Inspected Keycloak/ops host reachability and container standing before prep. | Later SSH by hostname failed with DNS errors and by direct IP timed out. |

No real users, real organizational data, production secrets, or production
approval were used.

## Retained R12 Standing Before Prep

The R12 reference deployment remained the accepted synthetic reference
deployment before NW-125 preparation started:

- accepted evidence root: `/opt/datarun-lab/evidence/NW-067-R12-2026-06-18`;
- active runtime config: `/opt/datarun-lab/runtime-config-nw076-g2`;
- app container: `datarun-reference-server-1`;
- app compose project: `datarun-reference`;
- app image digest:
  `sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`;
- app source revision label:
  `757d6c8d386f760693157c3e1388c877efdf6a0e`;
- app loopback ports: `127.0.0.1:18080->8080` and
  `127.0.0.1:18081->8081`;
- app readiness/liveness were expected on `18081`, not `18080`;
- `/api/auth/me` on `18080` returned protected/unauthenticated status;
- `/web-admin/operational` on `18080` returned `404`.

This artifact preserves the R12 reference deployment as accepted operations
rehearsal evidence only. R12 cannot be used as PC2 live proof because its image
predates PC2.

## Isolation Boundary

NW-125 did not target the accepted R12 evidence roots, runtime config, compose
project, app ports, DB roles, monitoring evidence, backup evidence, or token
cleanup state.

The attempted PC2 environment was intentionally separate:

- source checkout: `/home/nmcp/datarun-platform-pc2-src`;
- source commit: `9dee62d8019d13076ce359db3be999d4db916200`;
- compose project: `datarun-pc2-nw125`;
- compose files: repository `docker-compose.yml` plus
  `/home/nmcp/datarun-platform-pc2-src/nw125-compose.override.yml`;
- DB port: `127.0.0.1:25432->5432`;
- app port: `127.0.0.1:28080->8080`;
- expected browser tunnel path:
  `ssh -N -L 28080:127.0.0.1:28080 nmcp@datarun-app.lab`;
- expected local browser URL after tunnel:
  `http://127.0.0.1:28080/web-admin/operational`.

Post-prep R12 continuity was not re-verified because app-host access failed
after the build/run attempt. The successor route must inspect R12 first and
must clean, retain, or complete any isolated PC2 state without changing R12
standing.

## Source Commit And Image

The PC2 source checkout was cloned from GitHub `main` and resolved to:

```text
9dee62d8019d13076ce359db3be999d4db916200
```

The attempted PC2 runtime image/container could not be verified after the
Docker command output was truncated and lab access failed. Therefore NW-125 does
not claim an immutable PC2 image digest, running container, healthy application,
or source-label match.

## Readiness Checks

| Check | Result | Evidence |
|---|---|---|
| R12 reference deployment inspected before prep | `PASS` | R12 app container, image digest, source revision, loopback ports, health port split, and `/web-admin/operational` 404 were observed before prep. |
| R12 suitable for PC2 proof | `FAIL` | R12 source revision predates PC2 and `/web-admin/operational` returns `404`. |
| Isolated PC2 source checkout from current `main` | `PASS` | `/home/nmcp/datarun-platform-pc2-src` cloned at commit `9dee62d8019d13076ce359db3be999d4db916200`. |
| Isolated PC2 compose project/ports selected | `PASS` | `datarun-pc2-nw125`, DB loopback port `25432`, app loopback port `28080`. |
| PC2 container/image built and running | `NOT_READY` | Build/run command output was truncated and later SSH access failed before `docker compose ps` could be read. |
| `/web-admin/operational` on PC2 stack reachable | `NOT_READY` | Could not verify `http://127.0.0.1:28080/web-admin/operational`. |
| Synthetic web-admin principal/session/provisioning path | `NOT_READY` | No PC2-specific synthetic browser session path was proven. R12 rotated bearer-principal evidence was not repurposed. |
| Cleanup or retained synthetic state | `NOT_READY` | PC2 source checkout and possible compose/build state may remain on the app host; cleanup/retention could not be verified. |
| Docker/Maven dependency fetching blocker | `NOT_PROVEN` | The blocker observed by NW-125 was lab reachability loss after build initiation, not a verified Maven/image dependency-fetch failure. |

## Exact Preparation Actions

Read-only R12 inspection happened first, then the isolated PC2 prep attempted:

```bash
ssh -o BatchMode=yes -o ConnectTimeout=8 nmcp@datarun-app.lab '... docker ps ...'
ssh -o BatchMode=yes -o ConnectTimeout=8 nmcp@datarun-db1.lab 'hostname'
ssh -o BatchMode=yes -o ConnectTimeout=8 nmcp@keycloak.lab 'hostname'
ssh -o BatchMode=yes -o ConnectTimeout=8 nmcp@datarun-app.lab 'git clone --branch main --depth 1 https://github.com/Hamza-ye/datarun-platform.git /home/nmcp/datarun-platform-pc2-src'
ssh -o BatchMode=yes -o ConnectTimeout=8 nmcp@datarun-app.lab 'cd /home/nmcp/datarun-platform-pc2-src && docker compose -p datarun-pc2-nw125 -f docker-compose.yml -f nw125-compose.override.yml up -d --build db server'
```

Follow-up inspection commands then failed:

```text
ssh: Could not resolve hostname datarun-app.lab: Name or service not known
ssh: Could not resolve hostname datarun-db1.lab: Name or service not known
ssh: Could not resolve hostname keycloak.lab: Name or service not known
ssh: connect to host 192.168.1.213 port 22: Connection timed out
ssh: connect to host 192.168.1.214 port 22: Connection timed out
ssh: connect to host 192.168.1.217 port 22: Connection timed out
```

## Boundaries Preserved

- No PC2 live browser proof was run.
- No real users or real organizational data were used.
- No real-production approval was granted.
- No application runtime code, Dockerfile/build-tooling, schemas, tests, CI,
  product specs, platform specs, BAR, CDL, or gap-register changes were made.
- No auth bypass, dev-login shortcut, or principal-binding shortcut was added.
- No reporting/import/export, queue/list/multi-item review, automation,
  batch workflow, resolver reassignment, resolver eligibility broadening, or
  tenant/control-plane work was selected.

## Successor Route

Select exactly one next route:

`NW-126 - Reconcile and complete PC2 synthetic lab proof environment`.

NW-126 should inspect the app host by hostname or fixed IP, verify the accepted
R12 reference deployment first, inspect any retained `datarun-pc2-nw125` state,
then either clean and recreate or complete the isolated PC2 environment from
current `main`. It must prove `/web-admin/operational` reachability and a
synthetic web-admin session/provisioning path before selecting a later live
browser proof. If image dependency fetching is the actual blocker, NW-126
should stop and recommend a separate standard implementation-tooling route
rather than changing build tooling inside the environment route.
