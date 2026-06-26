# NW-165 Live Mobile OIDC Login Smoke Evidence

Status: accepted implementation evidence
Date: 2026-06-26

## Outcome

NW-165 is complete. The actual Flutter Android app ran on emulator
`emulator-5554` against the existing NW-164 local Keycloak and Datarun
deployments, used the system browser for OIDC authorization-code + PKCE,
returned through the Android app callback, exchanged the code in the app,
resolved the actor through live `/api/auth/me`, activated the mobile actor
session, and reached authenticated config/sync access.

The final browser login reused the prior Chrome/Keycloak browser session, so
the user did not re-enter the `hamza-pilot` password during the successful
final pass. This is expected system-browser behavior and was recorded as part
of the smoke. Earlier in the same run the Keycloak login page was reached and
completed through Chrome.

No Keycloak productionization, account import, real-data cutover, production
actor selection, stock/product vocabulary selection, or new auth semantics were
added.

## Live Runtime

Reused NW-164 deployments:

- Keycloak: `https://keycloak.lab:28443/realms/datarun-local`
- Datarun app: `https://datarun-app.lab:28443`
- Mobile client: `datarun-mobile`
- Redirect URI: `dev.datarun.mobile://oauth2redirect`
- Bound actor: `15000000-0000-4000-8000-000000000001`

Emulator evidence:

- `emulator -list-avds` showed `dev_phone` and `dev_phone2`.
- `/dev/kvm` existed and KVM acceleration was available.
- `adb devices -l` showed `emulator-5554` as
  `sdk_gphone64_x86_64`.
- The final app foreground state was
  `dev.datarun.datarun_mobile/.MainActivity`.

## Live Flow Evidence

System-browser PKCE and callback:

- The app launched Chrome for the OIDC authorization flow.
- Android logcat observed the callback intent into
  `dev.datarun.datarun_mobile/.MainActivity` with
  `dat=dev.datarun.mobile://oauth2redirect/...`.
- The initial unpatched app reached the callback but showed the generic
  "Sign in could not be completed" toast and remained on the sign-in screen.
  The failing boundary was the mobile runtime network/TLS path after callback,
  not Android intent routing.

Token exchange and actor resolution:

- After the bounded runtime fix, the same live mobile flow returned to the app.
- The app advanced past setup into the authenticated work screen.
- Non-secret app metadata showed:

```text
flutter.server_url=https://datarun-app.lab:28443
flutter.active_actor_id=15000000-0000-4000-8000-000000000001
flutter.device_id=fc7fdb5a-3074-4594-945c-b829a5ab796d
flutter.actor_session.15000000-0000-4000-8000-000000000001.token_expires_at=2026-06-26T03:14:10.293543Z
flutter.actor_session.15000000-0000-4000-8000-000000000001.oidc_config=Keycloak auth endpoint, token endpoint, client_id datarun-mobile, redirect dev.datarun.mobile://oauth2redirect, scopes openid/email/profile
```

Token values, refresh tokens, authorization codes, cookies, and passwords were
not recorded.

Authenticated server requests from `datarun-local-app-server-1`:

```text
2026-06-26T03:09:11.775Z GET /api/auth/me 200 actor_id=15000000-0000-4000-8000-000000000001 request_id=378cf45e-a6db-422c-8b8a-e827a9cb302e
2026-06-26T03:09:12.469Z GET /api/auth/me 200 actor_id=15000000-0000-4000-8000-000000000001 request_id=906ac729-5514-4ae1-b3e0-f457c409f513
2026-06-26T03:09:12.634Z POST /api/sync/pull 200 actor_id=15000000-0000-4000-8000-000000000001 request_id=9d9417d5-e9e8-4f03-9f28-73f705344879
2026-06-26T03:09:13.235Z GET /api/sync/config 200 actor_id=15000000-0000-4000-8000-000000000001 request_id=bb2661fe-ebc6-4e2e-9eea-e637acb9a143
```

After tightening the debug client to use an empty trust store and exact
certificate pins, the final installed build produced:

```text
2026-06-26T03:26:37.700Z GET /api/auth/me 200 actor_id=15000000-0000-4000-8000-000000000001 request_id=73258cb9-d82a-463f-870b-3419f0363912
2026-06-26T03:26:40.306Z GET /api/auth/me 200 actor_id=15000000-0000-4000-8000-000000000001 request_id=089c61f1-f9b3-4843-812e-14d0058b2f6a
2026-06-26T03:26:40.511Z POST /api/sync/pull 200 actor_id=15000000-0000-4000-8000-000000000001 request_id=1662a79e-2405-40d1-8334-c0ea8ad47571
2026-06-26T03:26:40.643Z GET /api/sync/config 304 actor_id=15000000-0000-4000-8000-000000000001 request_id=10d99dd2-d8e0-4d15-9c9e-b9bb300a27ec
```

The final `/api/sync/config` response was `304` because the config had already
been downloaded and cached from the earlier authenticated `200` response.

UI evidence:

- Before the sync-client fix, the authenticated work screen showed
  "Couldn't get work" / "No connection".
- After the sync-client fix and "Get Work", the app showed role
  `pilot_admin`, "Ready to capture", and the add action. This proves the live
  authenticated config/sync path crossed the server and enabled configured
  work.
- Screenshot files collected locally during the run included:
  `/tmp/nw165-app-after-callback-fail.png`,
  `/tmp/nw165-after-successful-login.png`,
  `/tmp/nw165-after-sync-client-patch.png`, and
  `/tmp/nw165-after-get-work.png`.
- Final exact-pin screenshots included
  `/tmp/nw165-final-exact-pin-start.png` and
  `/tmp/nw165-final-exact-pin-after-sync-now.png`; the latter shows
  "Sync complete", `0 records sent`, `0 updates received`, and
  `Last successful sync: 06:26`.

## Runtime Fix Applied

The live emulator could not install the lab CA as a system trust anchor:
`adb root` worked, but `adb remount` failed because the emulator image was not
bootloader-unlocked. Chrome could proceed through its certificate interstitial,
but Dart `HttpClient` did not trust the lab TLS certificates.

The bounded mobile/runtime fix adds a debug-only pinned TLS client:

- `DATARUN_DEBUG_TRUST_LAB_TLS=true` enables the path only in assert-enabled
  debug execution.
- `DATARUN_DEBUG_TRUSTED_TLS_SHA256` supplies exact per-host certificate
  SHA-256 pins.
- Release/profile behavior does not enable the trust override.
- The client verifies the peer certificate DER fingerprint for each host and
  does not accept arbitrary certificates.

This client is now used by:

- OIDC token exchange.
- Mobile `/api/auth/me` actor resolution.
- Sync `/api/auth/me` identity refresh.
- Authenticated `/api/sync/pull`.
- Authenticated `/api/sync/config`.
- Authenticated `/api/sync/push` when pending work exists.

Focused tests were added for:

- debug TLS pins disabled without the explicit debug define;
- token-exchange network failure reported at the token-exchange boundary
  without leaking code/state values;
- `/api/auth/me` network failure reported at the actor-resolution boundary
  without creating a session.

## Validation

Focused Flutter tests:

```text
cwd: mobile
command: flutter test test/auth_handoff_test.dart test/auth_service_test.dart test/mobile_http_client_test.dart test/sync_service_test.dart
result: PASS, 17 tests
```

Full Flutter tests:

```text
cwd: mobile
command: flutter test
result: PASS, 148 tests, 01:05
```

Android compile gate:

```text
cwd: mobile/android
command: ./gradlew :app:compileDebugKotlin --console=plain --no-daemon --stacktrace
result: PASS, BUILD SUCCESSFUL in 2m 17s, 82 actionable tasks
```

Live smoke:

```text
cwd: mobile
command: flutter run -d emulator-5554 --debug --no-resident \
  --dart-define=DATARUN_DEBUG_TRUST_LAB_TLS=true \
  --dart-define=DATARUN_DEBUG_TRUSTED_TLS_SHA256=keycloak.lab=<sha256>,datarun-app.lab=<sha256>
result: PASS; app reached authenticated ready-to-capture screen, final sync panel showed "Sync complete", and server logs showed authenticated /api/auth/me 200, /api/sync/pull 200, and /api/sync/config 304 after the earlier authenticated config 200.
```

`flutter analyze` was not used as a hard gate because the mobile validation
matrix marks it known-red and non-blocking until those findings are fixed or
baselined.

## Secret Safety

This artifact does not contain passwords, token values, refresh tokens,
authorization code values, cookies, authorization headers, client secrets, or
Keycloak session values.

The `hamza-pilot` password remains stored only on `keycloak.lab` at:

```text
/home/nmcp/datarun-local-keycloak/secrets/hamza-pilot-password
```

with `0600` permissions for `nmcp:nmcp`.

## Pre-Cutover Hardening Candidate

NW-165 intentionally did not productionize Keycloak or the local Datarun app
deployment. A concrete pre-cutover hardening candidate is now recorded as
NW-166: replace `start-dev`/embedded-database Keycloak runtime assumptions
with a non-dev local/on-prem Keycloak start mode and durable external database
before any production cutover claim.

NW-166 does not block this accepted live mobile proof.
