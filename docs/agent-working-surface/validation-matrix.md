# Agent Validation Matrix

Status: active validation-control surface
Document type: validation_control
Owner: product/engineering steward
Source: NW-107; 2026-06-19 Test / CI / Validation Strategy Audit; AGENTS.md nested validation split; PC1 PM handoff
Authority: validation routing and evidence surface only; does not change runtime behavior, product scope, CI implementation, test code, or acceptance standing by itself
Last reviewed: 2026-06-20

## 1 Purpose

This matrix maps touched surfaces to required validation evidence. It
distinguishes CI-backed, local-required, manual/ops, currently-red, and future
gate checks. It does not itself add tests, analyzer baselines, or runtime
behavior. NW-109 adds mobile CI for green mobile gates; analyzer cleanup or
baseline remains a future selected route.

## 2 Evidence Format

Every NW acceptance must state:

- command;
- working directory;
- result;
- test count and duration when available;
- CI link when available;
- skipped-gate rationale;
- whether the gap register was touched;
- whether the artifact trace was touched;
- whether the active control panel was updated.

## 3 Validation Levels

| Level | Meaning |
|---|---|
| `focused` | Narrow command proving the touched behavior or document surface. |
| `full local` | Broad local gate for the touched runtime or product area. |
| `CI-backed` | Gate currently replayed by GitHub Actions for matching paths. |
| `manual/ops` | Environment-specific or human-reviewed evidence. |
| `currently red / not blocking` | Known-red check that may be reported but is not a hard gate yet. |
| `future gate` | Gate to define in a later selected NW before it can block acceptance. |

## 4 Required Validation By Touched Surface

| Touched surface | Focused check | Full gate | CI-backed today | Blocks NW acceptance? | Evidence required | Notes |
|---|---|---|---|---|---|---|
| docs-only/status/backlog/artifacts/spec docs | `git diff --check`; targeted path/grep checks for required text | None unless the task names a broader doc check | No | Yes for diff errors or missing required text | Commands, cwd, grep/file-check result, skipped runtime-test rationale | Do not mark implementation accepted only because docs changed. |
| product handoff/product planning | `git diff --check`; grep required metadata/sections; compare wording to accepted product spec/PM handoff | None unless product behavior spec changes are selected | No | Yes for missing metadata, authority wording, or non-goal drift | Commands, cwd, source docs used, skip rationale | Planning surfaces do not add product behavior or backlog acceptance. |
| PC1 product-journey smoke/proof | Matrix wording only until the smoke route is selected | Future gate | No | No until created and accepted | Candidate route or explicit deferral | PC1 product journey smoke is a future gate, not an NW-107 gate. |
| server behavior | Relevant `./mvnw -Dtest=<RelevantTestClass> test` | `./mvnw test` or `./mvnw verify --batch-mode --no-transfer-progress` | Yes when server/workflow paths trigger server CI | Yes for server behavior changes | Focused and full command result or CI link, test counts/duration | Start local test DB before local integration tests. |
| server auth/security/admin | Relevant auth/admin/web-admin tests; include denial/no-mutation cases when authority changes | Full server gate | Yes when server/workflow paths trigger server CI | Yes | Focused auth/admin result, full gate or CI link | Do not promote IdP claims, request actors, or generic root admin as authority. |
| web-admin UI/templates/product vocabulary | Relevant web-admin server tests or template/rendering tests when available; product wording checked against PC1 PM handoff when user-visible copy changes | Server full gate when backend/controller/session/command behavior changes | Server CI only when server/workflow paths trigger it | Yes for user-visible web-admin behavior changes if no focused evidence is supplied; no for choosing Angular/SPA/frontend stack | Focused command/result, wording source, skipped full-gate rationale if docs/copy only | Thymeleaf/server-rendered HTML is current implementation shape, not permanent product strategy. Do not introduce Angular/SPA/frontend build tooling unless a selected NW explicitly routes UI delivery architecture. |
| server packaging/image/ops | Packaging/release focused tests when available; `scripts/verify-server-image.sh` | Server verify plus image verification | Yes in server CI for matching paths | Yes for packaging/release/ops changes | Command result or CI link; image tag/revision when relevant | Reference deployment rehearsal is manual/ops, not every-PR gate. |
| contracts/schemas/fixtures | Focused server contract/schema/projection tests plus affected mobile fixture/projection tests | Server full gate; mobile full gate when mobile consumption changes | Server CI and mobile CI for matching contracts paths | Yes when contract meaning or shared fixtures change | Server/mobile focused commands, full gates, fixture names, CI links when available | Contracts are not generated code; keep server/mobile interpretation in sync. |
| mobile Dart behavior | Relevant `flutter test test/<file>_test.dart` | `flutter test` | Yes through `.github/workflows/mobile-ci.yml` for matching mobile/contracts/workflow paths | Yes for mobile behavior changes | Focused and full mobile results, test count/duration, CI link when available | `flutter analyze` may be reported but is known-red and not blocking. |
| mobile UI/widget/workflow | Relevant widget/workflow test file | `flutter test` | Yes through `.github/workflows/mobile-ci.yml` for matching mobile/contracts/workflow paths | Yes for user-visible mobile behavior changes | Focused widget result plus full mobile result, CI link when available | Prefer typed fakes/shared harnesses over broad `noSuchMethod` fakes. |
| mobile native/platform/auth/plugin | Relevant Flutter auth/platform tests | `flutter test`; Android compile | Yes through `.github/workflows/mobile-ci.yml` for matching mobile/contracts/workflow paths | Yes for native/platform/auth/plugin changes | Flutter results, Android compile result, CI link when available | Run from `mobile/android`; `mobile/gradlew` is absent. |
| cross-runtime sync/config/projection | Server sync/config/projection tests and mobile projection/fixture tests | Server full gate plus mobile full gate | Server CI and mobile CI for matching paths | Yes | Both runtime commands, fixture names, skipped side rationale if any, CI links when available | Use shared fixtures when a contract or projection crosses runtimes. |
| operations/rehearsal evidence | Exact runbook/rehearsal checks named by the task | Manual/ops evidence packet | No | Yes only when the NW explicitly scopes ops/rehearsal acceptance | Commands, host/context, result, evidence path | Reference deployment rehearsals are manual/ops gates, not default PR gates. |
| CI/workflow changes | Inspect workflow path triggers and command names | Relevant local command that mirrors the workflow where possible | The changed workflow is CI-backed only after push/PR | Yes for workflow syntax/intent errors | Local mirror command, PR CI link when available | NW-109 adds mobile CI; known-red analyzer remains outside blocking CI until fixed or baselined. |

## 5 Command Reference

Docs:

```bash
git diff --check
```

Server:

```bash
cd /home/hamza/datarun-platform
docker compose -f docker-compose.test.yml up -d test-db

cd /home/hamza/datarun-platform/server
./mvnw -Dtest=<RelevantTestClass> test
./mvnw test
./mvnw verify --batch-mode --no-transfer-progress
```

Server image:

```bash
cd /home/hamza/datarun-platform
scripts/verify-server-image.sh
```

Mobile:

```bash
cd /home/hamza/datarun-platform/mobile
flutter test test/<file>_test.dart
flutter test
flutter analyze
```

Android:

```bash
cd /home/hamza/datarun-platform/mobile/android
timeout 900s ./gradlew :app:compileDebugKotlin --console=plain --no-daemon --stacktrace
```

## 6 Known Current Gate Standing

- Server full test/verify is CI-backed for matching server/contracts/workflow
  paths through `.github/workflows/server-ci.yml`.
- Server image verification is CI-backed by the server workflow.
- Mobile CI is present in `.github/workflows/mobile-ci.yml` for matching
  mobile/contracts/workflow paths.
- Mobile full tests are CI-backed by mobile CI for matching paths.
- `flutter analyze` is known-red with 7 observed issues and must not be claimed
  as a hard acceptance gate until fixed or baselined. Current standing is in
  `docs/agent-working-surface/mobile-analyzer-known-issues.md`.
- Android compile is CI-backed by mobile CI for matching paths.
- PC1 product journey smoke is a future gate to define from the PM handoff.
- Ops/reference deployment rehearsals are manual/ops gates, not default PR
  gates.

## 7 Acceptance Rules

- A focused check proves touched behavior; it does not replace the full gate
  unless the task explicitly limits acceptance to investigation.
- A currently red gate can be reported as evidence but must not block unless a
  baseline or fix has been accepted.
- A future gate cannot be required until created and routed.
- CI-backed gates should be linked in acceptance when relevant.
- Local-only gates must include exact command and result.
- Manual/ops gates require explicit NW scope.
- Do not mark implementation accepted only because docs changed.
- Do not skip a relevant gate without recording rationale.

## 8 Product Journey And Smoke Gates

PC1 product journey smoke is a candidate route, not an accepted validation gate
yet. A future product smoke should cover setup/config to assignment to mobile
get-work/capture/correction/sync to freshness/attention. Defining that smoke is
allowed as a future NW, not as part of NW-107 unless kept to matrix wording
only.

## 9 Immediate Follow-Up Routes

Candidate routes only after NW-109:

- analyzer cleanup or baseline;
- PC1 product journey smoke definition;
- server log-volume reduction;
- mobile fake/harness cleanup;
- shared fixture/contract parity improvements.

NW-109 is the accepted mobile CI/analyzer path row when this matrix is updated
with mobile CI standing. Do not create accepted backlog rows for the remaining
candidate routes without selecting them separately.
