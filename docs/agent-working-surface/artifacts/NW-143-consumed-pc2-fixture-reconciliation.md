# NW-143 Consumed PC2 Fixture Reconciliation

Status: non-authoritative product-validation / owner-review reconciliation artifact

Date: 2026-06-23

## Classification

`READY`

The consumed NW-141 fixture state is reconciled, the stopped NW-142 result is
preserved, and exactly one clean retry route is selected. NW-143 did not create
a replacement fixture and did not submit any resolution decision.

## R12 continuity

Before PC2 inspection, R12 was inspected read-only and preserved:

- App host: `vm-datarun-app`.
- R12 app container:
  `datarun-reference-server-1|localhost:5000/datarun/server|Up 18 hours (healthy)|127.0.0.1:18080->8080/tcp, 127.0.0.1:18081->8081/tcp`.
- R12 image/revision/version:
  `localhost:5000/datarun/server@sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`,
  source revision `757d6c8d386f760693157c3e1388c877efdf6a0e`,
  version `nw067-candidate`.
- R12 readiness on `127.0.0.1:18081`: HTTP 200, `{"status":"UP"}`.
- R12 `/api/auth/me` without token: HTTP 401.
- R12 `/web-admin/operational`: HTTP 404.
- R12 evidence root and runtime config remained present:
  `/opt/datarun-lab/evidence/NW-067-R12-2026-06-18`,
  `/opt/datarun-lab/runtime-config-nw076-g2`.
- DB1 host `vm-datarun-db-14` retained the R12 evidence root.
- Ops host `vm-datarun-ops-01` retained the R12 evidence root; `datarun-keycloak`
  and `datarun-alertmanager` were up.

After NW-143 PC2 read-only inspection, R12 was inspected read-only again and
remained unchanged:

- App host R12 app container stayed healthy on the same image digest, source
  revision, version, and loopback ports.
- R12 readiness remained HTTP 200 with `{"status":"UP"}`.
- R12 `/api/auth/me` without token remained HTTP 401.
- R12 `/web-admin/operational` remained HTTP 404.
- R12 evidence root and runtime config remained present.
- DB1 host `vm-datarun-db-14` retained the R12 evidence root.
- Ops host `vm-datarun-ops-01` showed `datarun-keycloak` and
  `datarun-alertmanager` up and retained the R12 evidence root.

No R12 state, R12 Keycloak state, real users, real data, or production secrets
were mutated or used.

## PC2 Standing

The retained isolated PC2 stack still matched the expected infrastructure and
auth standing:

- Source path: `/home/nmcp/datarun-platform-pc2-src`.
- Source revision: `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Compose project: `datarun-pc2-nw125`.
- App port: `127.0.0.1:28080`.
- Synthetic OIDC provider port: `172.17.0.1:28090`.
- Server image revision label:
  `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Server image version: `nw126-pc2-synthetic`.
- PC2 readiness: HTTP 200, `{"status":"UP"}`.
- `/api/auth/me` without token: HTTP 401.
- `/web-admin/operational` without session: HTTP 302 to `/web-admin/login`.
- `/web-admin/login`: HTTP 302 to the synthetic OIDC authorization endpoint.
- OIDC discovery on the synthetic provider: HTTP 200.
- Active principal binding count for the synthetic issuer/subject/actor: `1`.
- Reviewed config granted exactly `web_admin.access` and
  `web_admin.read_scoped` to actor
  `33333333-3333-4333-8333-333333333333`.

## Consumed Fixture Standing

The consumed NW-141 fixture is intact as history but not available as an
unresolved review item:

- Source work event:
  `14114114-3141-4141-9141-141141141141`, shape `visit/v1`,
  type `capture`, activity `field_visit`, subject
  `14114114-2141-4141-9141-141141141141`.
- Fixture flag:
  `14114114-4141-4141-9141-141141141141`, shape `conflict_detected/v1`,
  category `role_stale`, source event
  `14114114-3141-4141-9141-141141141141`, designated resolver
  `33333333-3333-4333-8333-333333333333`.
- NW-141 cleanup resolution:
  `f5c57b88-c285-46c1-b40b-52d8618dc1e5`, accepted, flag event
  `21d8d6b3-e9b3-327e-a4f2-5d19044e9101`.
- Manual consumed-fixture resolution:
  `bcafd46a-d439-4548-b51d-bc8ec9dddb09`, accepted, flag event
  `14114114-4141-4141-9141-141141141141`, actor
  `33333333-3333-4333-8333-333333333333`, reason `ok, manual`.

Current unresolved standing:

- Unresolved conflict flags: `0`.
- Unresolved NW-141 fixture flag count: `0`.
- Unresolved flags for the NW-141 source work: `0`.

## Owner Manual Evidence

No owner-supplied manual browser evidence was recorded in the repository for
the already-submitted `ok, manual` resolution beyond the retained database
resolution event itself.

The consumed fixture proves that an accepted resolution was submitted by the
synthetic actor, but it does not provide the bounded NW-142 before/after browser
evidence that was requested. Therefore the already-consumed fixture should be
treated as informal/manual state evidence only, not as the completed NW-142
live browser proof.

## Replacement Fixture Approval

No replacement fixture was created in NW-143.

Hamza asked to finish the route and keep it cleanly controlled, but NW-143's
selected prompt only allows replacement creation with explicit approval inside
the replacement/proof route. The clean next route is therefore a single
successor that explicitly creates one replacement synthetic fixture and runs
the bounded proof.

## Retained And Cleanup State

- Retained PC2 compose project: `datarun-pc2-nw125`.
- Retained app port: `127.0.0.1:28080`.
- Retained synthetic OIDC provider port: `172.17.0.1:28090`.
- Retained consumed fixture source, flag, and resolution events in the isolated
  PC2 DB.
- No PC2 replacement fixture was added in NW-143.
- No R12 cleanup was needed because R12 was not mutated.

## Next Route

Exactly one next route is selected:

`NW-144 - Create replacement PC2 fixture and run live browser proof`

## Validation

- `git diff --check` passed.
- `rg "NW-143" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md` passed.
- `test -f docs/agent-working-surface/artifacts/NW-143-consumed-pc2-fixture-reconciliation.md` passed.
- `grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-143-consumed-pc2-fixture-reconciliation.md` passed.
- Runtime automated tests skipped because NW-143 changed no runtime code, tests,
  contracts, schemas, migrations, CI behavior, validation policy, product or
  platform specs, BAR, CDL, gap register, mobile code, or server/web-admin
  implementation.
