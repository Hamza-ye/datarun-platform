# NW-165 - Run Live Mobile Application OIDC Login Smoke

## Goal

Execute the actual Flutter mobile application on an Android device or emulator
against the live local Keycloak issuer and prove the mobile login path end to
end through actor-session activation and authenticated access.

This is execution work, not a planning, documentation-preflight, architecture,
or mocked-client task.

## Inputs

Read:

- `docs/status.md` Current Routing.
- `docs/agent-working-surface/platform-next-work-backlog.md` row NW-165.
- `docs/agent-working-surface/artifacts/NW-164-local-keycloak-bound-login-evidence.md`.
- `deploy/reference/local-keycloak/README.md`.
- `docs/specifications/platform/mobile-oidc-login-and-token-lifecycle.md`.
- Mobile auth/session/sync code and tests relevant to external-user-agent
  OIDC, callback handling, token exchange, `/api/auth/me`, config access, and
  sync access.

Read nested `AGENTS.md` files before touching mobile, server, or deployment
directories under their scope.

## Required Execution

Use the existing live local Keycloak issuer and Datarun app from NW-164 unless
they are demonstrably unavailable. Preserve retained lab deployments.

Run the real Flutter app on an Android device or emulator and prove:

1. System-browser/external-user-agent authorization-code + PKCE is used.
2. The OIDC redirect callback returns into the Flutter application.
3. The app performs token exchange.
4. The resulting token resolves the actor through live `/api/auth/me`.
5. The mobile actor session becomes active in the application.
6. Authenticated config access succeeds.
7. Authenticated sync access succeeds as far as the current configured fixture
   permits.

Complete every independent step if one path fails. A server or provider proof
already accepted by NW-164 does not need to be repeated except as needed to
support the live mobile-app run.

## Authority Boundary

Use the accepted mobile OIDC + PKCE implementation and explicit principal
binding behavior. Datarun authority remains the server-resolved active
`(issuer, subject) -> actor_id` binding.

Do not add or change auth semantics. Do not introduce IdP group, role, claim,
JWT `actor_id`, UI actor selection, account import, or request-body authority.

## Non-Goals

Do not:

- create new auth semantics, architecture decisions, CDL/BAR/gap-register
  changes, contracts, schemas, or stored event meaning;
- import legacy accounts or passwords;
- import or replay submitted records;
- run a documentation preflight instead of the live mobile app smoke;
- select production cutover, real users/data, cloud hosting, managed identity,
  managed database, external monitoring, or remote support dependencies;
- broaden into reporting/import/export, stock ledger correctness, review
  workflow, tenant/control-plane, new scope work, or product vocabulary
  decisions.

## Expected Output

On success:

- record secret-safe evidence for the Android device/emulator, app build/run
  context, external-user-agent handoff, callback, token exchange,
  `/api/auth/me` actor resolution, actor-session activation, authenticated
  config access, and authenticated sync access;
- update `docs/status.md` and
  `docs/agent-working-surface/platform-next-work-backlog.md` with the result,
  validation evidence, and either one concrete successor, owner decision
  pending, or `No successor selected`.

If live execution cannot proceed, stop only the failed path with a precise
blocker naming the exact action, observed evidence, and missing
capability/input/access/dependency. Complete any remaining independent checks.

## Validation

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
```

For mobile/runtime work, run the narrowest relevant Flutter test first, then
the required full mobile gate and Android compile gate from `mobile/AGENTS.md`
or `docs/agent-working-surface/validation-matrix.md`.

The live acceptance evidence must include the Android device/emulator identity,
the app/package under test, live issuer, Datarun base URL, resolved actor id,
and the exact commands or UI automation used. Do not retain tokens, refresh
tokens, authorization codes, cookies, client secrets, passwords, or private
keys.
