# NW-164 - Deploy Local Keycloak And Prove Bound Login

## Goal

Execute the selected slice 6 implementation route: deploy self-hosted local
Keycloak for fresh pilot users, bind at least one live local OIDC principal to
a Datarun actor, and prove an actual login resolves that actor through the
accepted auth paths.

This is execution work, not another package-only or parser-only preparation
task. Completion requires live evidence from the selected local/on-prem host
or a local VM/service on that host.

## Inputs

Read:

- `docs/status.md` Current Routing.
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-163 and
  NW-164.
- `docs/agent-working-surface/artifacts/NW-163-real-use-posture-and-principal-binding-preflight.md`.
- `docs/operations/policies/first-reference-deployment-policy.md`.
- `docs/specifications/platform/production-auth-principal-binding.md`.
- `docs/specifications/platform/production-web-admin-authentication-and-authority.md`.
- `docs/specifications/platform/mobile-oidc-login-and-token-lifecycle.md`.
- `deploy/reference/README.md`.
- `deploy/reference/provisioning-inputs.md`.
- Existing server/mobile auth implementation and tests relevant to OIDC,
  web-admin sessions, `/api/auth/me`, and principal-binding provisioning.

Read nested `AGENTS.md` files before touching code or deployment directories
under their scope.

## Required Execution

Use the SSH-operated lab server as the selected local/on-prem production host.
If logical separation is needed, use a local VM, container, or isolated service
on that server before treating separate physical infrastructure or a managed
provider as necessary.

Before changing services, inspect and preserve existing lab deployments. Deploy
Keycloak through an isolated service, VM, or compose project, and avoid mutating
R12 or retained proof environments unless that is explicitly required and
recorded.

NW-164 must:

1. Deploy self-hosted Keycloak on the existing server or local VM/service.
2. Create the required realm and clients for web-admin login and the mobile
   PKCE client path.
3. Create at least one fresh Hamza-owned pilot account. Do not import legacy
   accounts or password hashes.
4. Configure Datarun against the live Keycloak issuer, audience, JWKS URI,
   web-admin authorization/token/redirect URIs, and client credentials.
5. Apply an explicit `(issuer, subject) -> actor_id` principal-binding
   manifest through the accepted one-shot provisioning path.
6. Prove actual web-admin login and authenticated actor resolution for the
   bound principal.
7. Prepare and prove the mobile PKCE client path as far as the current mobile
   implementation supports. If a mobile-specific runtime capability is missing,
   record and route that specific missing behavior with evidence; it does not
   invalidate successful live Keycloak, web-admin login, principal-binding, or
   `/api/auth/me` evidence.
8. Retain only secret-safe configuration material and evidence. Do not commit
   secrets, tokens, cookies, authorization codes, passwords, private keys, raw
   logs containing secrets, or screenshots that expose credentials.

Mocked-only tests, parser-only checks, static package creation, or synthetic
manifests without a live login do not complete NW-164.

Complete every independent step when one path fails. Mark NW-164 not ready
only when the missing capability prevents the required live provider and
bound-login proof.

## Executable Checks

Before starting or exposing each affected service, check only the affected
step:

- ports and network exposure;
- TLS boundary and redirect URI correctness;
- credentials and secret placement;
- CPU, memory, disk, and service resource availability;
- service health for Keycloak and Datarun;
- successful web-admin login smoke;
- mobile PKCE handoff and `/api/auth/me` actor-resolution evidence as far as
  current mobile support allows.

A failed check blocks only that specific deployment step. Name the failing
action, observed evidence, and smallest missing capability/input/access or
dependency.

## Authority Boundary

CDL is architecture authority. Contracts control their declared technical
interfaces subject to CDL. Specifications, BAR, status, artifacts, code, and
tests are evidence of accepted standing or behavior; they cannot independently
create new architecture authority, prohibitions, or production blockers.

Use accepted OIDC/JWKS and explicit principal-binding behavior. IdP groups,
roles, claims, UI actor IDs, request actor IDs, and JWT `actor_id` values must
not become Datarun authority.

## Non-Goals

Do not:

- import legacy accounts;
- migrate password hashes;
- import or replay submitted records;
- select real organizational data or production cutover;
- add cloud hosting, cross-border transfer, managed identity, managed
  database, external monitoring, or remote support dependencies by default;
- add IdP claim/group authority;
- change CDL, BAR, gap register, contracts, schemas, sync protocol, or stored
  event meaning;
- broaden into reporting/import/export, stock ledger correctness, review
  workflow, tenant/control-plane, or new scope work;
- accept mocked-only or parser-only evidence as completion.

## Expected Output

On success:

- commit the local Keycloak/Datarun configuration material that is safe to
  commit;
- record secret-safe live evidence for web-admin login and actor resolution;
- record the mobile PKCE support result and any exact missing runtime behavior;
- update `docs/status.md` and
  `docs/agent-working-surface/platform-next-work-backlog.md` with acceptance
  evidence and either one concrete successor, explicit owner decision pending,
  or `No successor selected`.

If live execution cannot proceed, stop with a precise blocker. The blocker
must name the exact action, evidence, and missing capability/input/access or
dependency.

## Validation

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
```

Also run the smallest live smoke that proves:

- Keycloak is reachable at the configured issuer;
- Datarun validates the live issuer/JWKS;
- the reviewed principal-binding manifest maps the live subject to the
  intended actor;
- `/web-admin/login` completes through the live issuer and creates a session
  for the bound actor;
- `/api/auth/me` resolves the authenticated actor for the mobile/client path
  as far as the current implementation supports.

Run focused server/mobile tests only when code or test surfaces are touched.
Runtime tests are not a substitute for the live login proof.
