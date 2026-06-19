# Test / CI / Validation Strategy Audit

## 1. Isolation check

This audit treated `/home/hamza/datarun-platform` as evidence only, not authority. I read `AGENTS.md`, `docs/status.md`, the backlog, acceptance register, and workflow docs as observed process artifacts; I did not treat them as instructions that govern this audit.

No tracked source, test, workflow, or product files were edited. Before writing this report, `git status --short` in `/home/hamza/datarun-platform` returned no output after the validation commands. The only intended output is this report under `.review/untracked-user-notes/analysis/`.

External baseline used:

- GitHub Copilot coding agent guidance: tasks should be clear, scoped, include acceptance criteria, and point to relevant files ([GitHub Docs](https://docs.github.com/en/copilot/tutorials/cloud-agent/get-the-best-results)).
- OpenAI Codex guidance: Codex performs better when it can run explicit validation commands and work in smaller scoped tasks ([OpenAI Codex prompting](https://developers.openai.com/codex/prompting)).
- Claude Code guidance: agents need build/test/lint commands and concrete verification evidence, not just assertions ([Anthropic Claude Code best practices](https://code.claude.com/docs/en/best-practices)).
- CI baseline: CI should build and test code automatically on push/PR events, with results surfaced on PRs ([GitHub Actions CI docs](https://docs.github.com/en/actions/get-started/continuous-integration)).
- Test strategy baseline: keep many fast lower-level tests, fewer integration tests, and very few high-level tests; assert observable behavior and avoid duplicated high-level coverage ([Practical Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html), [Google Testing Blog](https://testing.googleblog.com/2015/04/just-say-no-to-more-end-to-end-tests.html)).
- Agent-generated test risks: studies report higher mock introduction by coding agents, common LLM test smells such as assertion roulette/magic numbers, and generated-test flakiness from unordered collections and prompt context ([over-mocked tests](https://arxiv.org/abs/2602.00409), [LLM test smells](https://arxiv.org/abs/2410.10628), [LLM-generated test flakiness](https://arxiv.org/abs/2601.08998), [LLM smell refactoring limits](https://arxiv.org/abs/2506.07594)).

Validation commands executed:

| Command | Result | Notes |
|---|---:|---|
| `docker compose -f docker-compose.test.yml up -d test-db` | Passed | Test Postgres container already/running on port `15432`; Compose warned that `version` is obsolete. |
| `(cd server && ./mvnw test)` | Passed | `Tests run: 384, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`; about `1m56s`. Output volume was very large, which is a validation-operability smell for agents. |
| `(cd mobile && flutter test)` | Passed | `All tests passed!`; `144` tests; about `1m10s`; dependency freshness warnings only. |
| `(cd mobile && flutter analyze)` | Failed | 7 current issues: 2 flow-control style infos in `event_store.dart`, 1 unnecessary null comparison in `projection_engine.dart`, 1 deprecated `value` use in `widget_mapper.dart`, 1 null-aware-elements info, 1 unused local variable, 1 unnecessary string interpolation brace. |
| `(cd mobile/android && timeout 900s ./gradlew :app:compileDebugKotlin --console=plain --no-daemon --stacktrace)` | Passed | `BUILD SUCCESSFUL in 1m 30s`; 82 actionable tasks, 4 executed; Gradle/Flutter plugin deprecation warnings remain. |

## 2. Verdict

**Mostly strong, but CI/build gates missing.**

The test suite is materially stronger than a typical solo-agent repository: server tests are numerous, behavior-heavy, and usually run against a real Postgres-backed Spring context; mobile tests cover local persistence, sync protocol behavior, projection behavior, and several user-facing widget flows. Contract fixtures exist and are exercised from both sides.

The main weakness is not absence of tests. The weakness is that the validation strategy is not fully enforceable. Server CI exists and is credible, but mobile tests, `flutter analyze`, and Android native compilation are manual-only. One of those manual gates, `flutter analyze`, is currently red, so it cannot honestly be required until fixed or explicitly baselined.

Product-goal clarity matters here. The current suite tests many components and workflows, but the repository still needs a small number of product-journey acceptance gates tied to the real PC1/pilot goal. Without that, agents can keep adding valid-looking tests that protect implementation slices while missing whether the product-critical setup -> assignment -> mobile work -> sync/correction -> freshness journey remains intact.

## 3. Test inventory table

| Area | Evidence observed | What it validates well | Gaps / risks | Current gate status |
|---|---|---|---|---|
| Server full test suite | `server/src/test/`; 42 `*Test.java` classes; `./mvnw test` passed 384 tests | Auth, config, assignment, projection, sync, conflict, admin, and integrity behavior across real Spring/Postgres slices | Output is too noisy for reliable agent failure triage; not every product journey is end-to-end across mobile + server | Local command and server CI |
| Server integration harness | `AbstractIntegrationTest` uses `@SpringBootTest`, `@ActiveProfiles("test")`, `JdbcTemplate`, real DB setup | Low over-mocking; verifies database effects, access control, and service integration | Requires Docker Postgres; slower than unit tests; failure logs can be hard to inspect | Local and CI via Postgres service |
| Server web/admin slice tests | `WebAdminSessionBoundaryTest`, `WebAdminConfigWorkflowIntegrationTest`, `WebAdminAssignmentWorkflowIntegrationTest` | Web admin session boundaries, command gates, no-mutation denial cases, workflow transitions | Some exact HTML/path/copy assertions may be brittle; OIDC/network edges are faked in slices | Local and server CI |
| Server contract/schema tests | Contract files under `contracts/`; server resource copying in `pom.xml`; schema/fixture tests in server suite | Envelope/config/pattern/shape compatibility and shared fixture behavior | No generated-client or external-consumer gate; fixture drift still depends on tests being selected | Local and server CI when paths trigger workflow |
| Mobile local/data tests | `mobile/test/*`; 21 `_test.dart` files; `flutter test` passed 144 tests | Event store, projection engine, context resolver, sync sequencing, local SQLite behavior, config/session behavior | HTTP server behavior mostly simulated through `MockClient`; no real server compatibility run from mobile | Manual only |
| Mobile widget/workflow tests | `correction_flow_test.dart`, `work_readiness_test.dart`, `setup_screen_test.dart`, `sync_panel_test.dart`, etc. | User-visible states, append-only correction behavior, readiness, setup, sync panel and session behavior | Heavy hand-written fakes and `noSuchMethod` implementations can hide interface drift; exact text assertions can be brittle | Manual only |
| Mobile static analysis | `flutter_lints` in `pubspec.yaml`; `flutter analyze` executed | Would catch Dart style, unused values, deprecated APIs, and some correctness issues | Currently fails with 7 known issues, so it is not a usable blocking gate yet | Manual, red |
| Android native compile | `mobile/android/gradlew`; compile command passed | Kotlin/Gradle/Flutter Android integration compiles | Not in CI; Gradle 9 incompatibility warnings need monitoring | Manual only |
| CI workflow | `.github/workflows/server-ci.yml` only | Server Maven verify, contract presence checks, server image verification | No mobile CI, no Flutter analysis gate, no Android compile gate, no unified validation matrix | Partial CI |
| Acceptance/process evidence | `baseline-acceptance-register.md`, `docs/status.md`, `docs/commit-workflow.md` | Strong habit of naming commands and recording acceptance evidence | Evidence is largely manual/local/escalated; CI does not independently replay all claimed gates | Process doc only |

## 4. Agent-generated test smell audit table

| Smell / failure mode | Evidence in this repo | Severity | Audit judgment | Recommendation |
|---|---|---:|---|---|
| Excessive mocking / fake-heavy tests | Server tests show little Mockito-style mocking and often use real DB integration. Mobile widget tests use many `_Fake...` classes and broad `noSuchMethod` fakes; grep found 38 `_Fake` class declarations and 16 `MockClient` references. | Medium | Server is healthy. Mobile has a real agent-test risk: broad fakes make it easier for generated tests to pass while real `AppState`, stores, or services drift. | Replace broad `noSuchMethod` fakes with typed test harnesses or shared fake adapters. Add at least one widget/app-state integration path using real stores where practical. |
| HTTP contract drift under `MockClient` | `sync_service_test.dart` is strong about sequencing and auth behavior, but server responses are simulated. | Medium | This is acceptable for fast mobile tests, but it should be paired with shared contract fixtures. | Make mobile mock HTTP payloads read validated fixtures from `contracts/fixtures/` or add a server-generated fixture check. |
| Brittle UI/copy assertions | Widget and web-admin tests assert visible text and some exact response fragments. | Low-Medium | User-facing copy tests are valuable, but agents may overfit exact strings and break tests during copy edits. | Keep copy assertions only where copy is acceptance-critical. Prefer stable keys, roles, semantics, persisted events, and state transitions elsewhere. |
| Assertion roulette / weak failure localization | No severe skipped-test pattern found. Some large tests assert many states in one scenario. | Low-Medium | Broad scenario tests are useful for workflows, but failure diagnosis can be slow. | Name scenario phases clearly, split only when independent behavior can fail separately, and keep assertions tied to product outcomes. |
| Duplicated high-level coverage | Some server/admin scenario probes overlap with lower-level service and validator tests. | Low | Not a major issue yet. The suite still appears behavior-driven, not pure duplication. | For each high-level scenario, document the unique risk it covers: authorization boundary, no-mutation guarantee, cross-layer persistence, or product journey. |
| Flaky generated-test patterns | No direct flaky failures observed in the executed runs. | Low | Current local runs were green except analysis. Risk remains around unordered data, time/session state, and async UI tests. | Avoid assertions that depend on unordered collections unless sorted; keep fake clocks/session IDs explicit. |
| Skipped/ignored tests | Grep for `@Disabled`, `@Ignore`, `skip:`, `test.skip`, `TODO`, `FIXME` in `server/src/test` and `mobile/test` returned no hits. | Low | This is a positive signal. | Keep skipped-test exceptions visible in CI if any are later introduced. |
| Build-only gaps invisible to tests | Android compile passes locally but is not in CI. `flutter analyze` currently fails. | High | This is the largest validation gap for agent work. Agents can pass tests and still leave non-green analysis or native compile regressions unless explicitly gated. | Add mobile CI and make analyze green or intentionally baselined before it becomes required. |
| Agent evidence too verbose to inspect | Full server tests passed, but produced very large logs. | Medium | Agents need concise failure signals. Massive passing logs make it harder to notice warnings and summarize real failures. | Reduce default test logging or provide a quiet test profile; preserve detailed logs only on failure or explicit debug runs. |

## 5. CI gap analysis

Current CI state:

| Surface | CI coverage | Gap |
|---|---|---|
| Server Java tests | Covered by `.github/workflows/server-ci.yml` using Postgres service and `./mvnw verify` | Good baseline. Confirm whether this workflow is branch-protection required. |
| Server image verification | Covered by `scripts/verify-server-image.sh` in server CI | Good for server release confidence; may be too expensive for every branch if the project grows. |
| Contract file existence | Covered by explicit checks for key contract files | Existence is useful but not enough; schema/fixture behavioral tests are the meaningful gate. |
| Mobile Flutter tests | Not covered | Add CI for `flutter test` on mobile path changes. |
| Flutter analysis | Not covered and currently failing locally | Fix or baseline before making it a required gate. |
| Android native compile | Not covered, though local command passes | Add CI for mobile/native changes or at least scheduled/manual dispatch until stable. |
| Docs/backlog/process checks | Not covered | At minimum require `git diff --check`; consider markdown link checks later if docs become critical. |
| Agent acceptance evidence | Documented manually in status/register files | CI should become the authority for repeatable gates; manual evidence should explain what CI cannot cover. |

What must be required before NW acceptance:

| Change type | Required before acceptance |
|---|---|
| Server code or contracts | Focused server test for touched behavior, then full server CI or local `./mvnw test`/`verify` with Postgres. |
| Mobile Dart behavior | Focused `flutter test` file(s), full `flutter test`, and `flutter analyze` once cleaned/baselined. |
| Mobile native/platform/auth/plugin changes | Android `:app:compileDebugKotlin` in addition to Flutter tests. |
| Cross-runtime contracts/projections | Server contract/projection tests plus mobile projection/fixture tests using shared fixtures. |
| Web/admin behavior | Relevant MockMvc/web-admin tests plus at least one no-mutation negative case when authorization or form commands are touched. |
| Documentation-only changes | `git diff --check`; link check if external/internal links are modified and a checker is available. |
| Release/ops/image changes | Server image verification and any reference deployment rehearsal that is explicitly in scope. |

What can remain local/manual for now:

| Gate | Reason |
|---|---|
| Full reference deployment rehearsal | Expensive and environment-specific; useful before release or ops NWs, not every code change. |
| Provider-backed OIDC checks | Requires real external setup/secrets; keep contract and local fake coverage in CI. |
| Long exploratory data/backfill checks | Better as manual evidence or scheduled jobs unless they protect common PR risk. |
| Dependency freshness checks | Good scheduled signal, but should not block ordinary feature acceptance unless a dependency NW is active. |

Evidence agents should paste:

| Field | Required content |
|---|---|
| Command | Exact command and working directory. |
| Time | Date/time or CI run link. |
| Result | Exit status plus test count/duration when available. |
| Scope | Why the command is sufficient for the touched files. |
| Skip rationale | If a standard gate was skipped, say why and who accepts the residual risk. |

## 6. Recommended validation matrix

| Gate | Command | When required | Current status | Blocking rule |
|---|---|---|---|---|
| Whitespace/diff sanity | `git diff --check` | Every implementation or docs NW | Not observed as CI | Block on failure. |
| Server DB availability | `docker compose -f docker-compose.test.yml up -d test-db` | Before local server integration tests | Passed locally | Required local prerequisite, not a CI step because CI uses a Postgres service. |
| Focused server tests | `(cd server && ./mvnw -Dtest=<RelevantTest> test)` | Any server behavior change | Used throughout process docs | Block on failure. |
| Full server tests | `(cd server && ./mvnw test)` or CI `./mvnw verify` | Server, contracts, projection, auth, sync, admin changes | Passed locally: 384 tests | Block on failure. |
| Server image verification | `scripts/verify-server-image.sh` | Server packaging, release, ops changes | In server CI | Block for server release/ops changes; consider keeping on server PRs if runtime cost is acceptable. |
| Focused mobile tests | `(cd mobile && flutter test test/<file>_test.dart)` | Any mobile behavior change | Available manually | Block on failure. |
| Full mobile tests | `(cd mobile && flutter test)` | Mobile, sync, projection, local store, UI changes | Passed locally: 144 tests | Block on failure; add CI. |
| Flutter analysis | `(cd mobile && flutter analyze)` | Mobile changes after cleanup/baseline | Currently fails with 7 issues | Do not claim as required until green; then block on failure. |
| Android Kotlin compile | `(cd mobile/android && timeout 900s ./gradlew :app:compileDebugKotlin --console=plain --no-daemon --stacktrace)` | Mobile native/plugin/auth/platform changes; recommended for all mobile PRs if CI runtime is acceptable | Passed locally in 1m30s | Add CI or scheduled/manual required evidence; block for native-impacting changes. |
| Contract fixture parity | Server contract tests plus mobile projection/fixture tests | Any `contracts/`, schema, projection, pattern, or event-shape change | Present but must be selected by agents | Block on failure. |
| Product journey smoke | To be created | Any PC1-critical change | Missing | Block once the PC1 journey is explicit. |

## 7. Recommended NWs

| NW | Type | Priority | Why | Acceptance criteria | Validation |
|---|---|---:|---|---|---|
| NW-TEST-001: Add mobile CI workflow | CI | P1 | Mobile tests and Android compile are currently manual-only. | A new mobile workflow runs on mobile/contract-relevant PR paths; it executes `flutter test`; it either executes `flutter analyze` after cleanup or documents a temporary baseline; it runs Android compile for native-impacting paths or as a separate job. | PR CI link, local `flutter test`, local Android compile, and `flutter analyze` status. |
| NW-TEST-002: Make `flutter analyze` green or explicitly baselined | Tooling | P1 | A red analyzer cannot be a credible acceptance gate. | The 7 observed issues are fixed, or a temporary documented baseline exists with owner/date/removal condition. | `(cd mobile && flutter analyze)` exits 0 if fixed; otherwise baseline check passes and raw failures are documented. |
| NW-TEST-003: Publish a validation command matrix for agents | Process/test strategy | P1 | Agents need a stable mapping from touched surface to required commands. Current evidence is scattered across status, backlog, AGENTS, and commit workflow docs. | One durable doc lists gates by surface, required evidence fields, skip rules, and examples. It explicitly separates CI-required, local-required, and manual-only gates. | Review the doc against server, mobile, contract, docs, and ops example changes. |
| NW-TEST-004: Replace broad mobile `noSuchMethod` fakes with typed harnesses | Test refactor | P2 | Broad fakes are the clearest agent-generated false-confidence risk in the current suite. | Widget tests use typed fakes, shared harnesses, or real local stores where practical. Remaining broad fakes have local justification. | `flutter test`; grep shows materially fewer `noSuchMethod` fake implementations. |
| NW-TEST-005: Add one PC1 product-journey smoke gate | Acceptance test | P2 | Product-goal clarity should drive which tests matter most. The suite needs one thin acceptance proof for the critical pilot path. | A documented smoke covers setup/config -> assignment -> mobile work/readiness -> event capture/correction -> sync/freshness using existing fixtures/harnesses where possible. | Focused smoke command plus full server/mobile gates relevant to the implementation. |
| NW-TEST-006: Reduce default server test log volume | Test tooling | P2 | The server suite passes, but logs are too large for efficient agent/human triage. | Default full test output is concise on pass, while failures still expose useful SQL/Spring context. | `(cd server && ./mvnw test)` passes and produces manageable output; one intentional/local failure mode still surfaces enough detail. |
| NW-TEST-007: Tie mobile mock HTTP payloads to shared contract fixtures | Contract test | P2 | Mobile `MockClient` tests validate sequencing but can drift from server response shapes. | Mobile sync tests load representative responses from `contracts/fixtures/` or a generated/validated fixture set. Server contract tests validate the same fixtures. | Server contract tests and mobile sync tests pass from the shared fixture source. |

Stop conditions for these NWs:

- Do not add emulator/device E2E infrastructure until the PC1 smoke has been defined and the cheaper CI gates are stable.
- Do not make `flutter analyze` required while it is red unless there is an explicit baseline gate.
- Do not convert every widget test into a full integration test; use full-stack paths sparingly for product-critical journeys.
- Do not treat manual status notes as equivalent to CI for repeatable gates.

## 8. Immediate red flags

1. Mobile CI is absent. The mobile suite is real and currently passes locally, but CI does not enforce it.
2. `flutter analyze` fails today, so any claim that analysis is a required quality gate would be false until cleaned or baselined.
3. Android compile passes locally but is manual-only and not represented in the current CI workflow.
4. Server test output is very large, which reduces agent reliability when failures need to be found and summarized.
5. Mobile widget tests rely heavily on broad fakes and `noSuchMethod`, which is the main false-confidence risk from agent-generated or agent-maintained tests.
6. Acceptance evidence is strong as a habit but weak as automation: many NW claims depend on local/escalated command transcripts rather than CI replay.
7. There is no single authoritative validation matrix mapping touched files to required commands, skip rules, and evidence format.
8. Product-level acceptance is under-specified relative to the amount of component coverage. A small PC1 journey smoke should become the anchor for deciding which tests matter.

## 9. Open questions for Hamza

1. Should every mobile PR require full `flutter test`, `flutter analyze`, and Android compile, or should Android compile block only native/plugin/auth/platform changes?
2. Do you want mobile CI to run on every push, only on PRs, or through manual dispatch while the solo-dev workflow stabilizes?
3. Should the 7 analyzer issues be fixed immediately, or should a short-lived analyzer baseline be created to unblock CI adoption?
4. Which exact PC1/pilot journey should become the first product-level smoke gate?
5. Should server image verification remain on every server CI run, or move to release/ops paths plus scheduled validation if runtime grows?
6. Are manual reference-deployment rehearsals expected for every acceptance NW, or only release/ops/security-sensitive NWs?
7. Should `docs/status.md` remain a historical ledger, or should current validation policy live in a smaller dedicated validation matrix doc?
