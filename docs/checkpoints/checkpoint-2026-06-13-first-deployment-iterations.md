# Project Checkpoint - 2026-06-13 - First-Deployment Iterations

## 1. Bearing

- **Anchor commit**: `9f2c3c9 docs(status): accept append-only correction UX`
- **Phase**: Post-Phase-4 stabilization; first-deployment workshop closed and
  four bounded mobile product-surface iterations accepted.
- **Momentum**: `ADVANCING` - the workshop was reduced to a usable route, then
  NW-059 through NW-062 landed with focused and full-suite evidence.
- **Last milestone**: NW-062 append-only correction UX was accepted after
  correcting effective-change detection and alias-aware subject handling.
- **Horizon**: select and sequence one next bounded deployment/product lane;
  this checkpoint does not make that selection.

The first-deployment effort has moved from workshop planning into tested mobile
behavior. The useful workshop conclusions now live in
`docs/workshops/first-deployment/summary.md`; the removed stage, role, packet,
and gate-review chain is provenance in git history, not an active route.

Candidate 1 now has an honest offline capture surface covering sync status,
post-save handoff, work readiness, and append-only correction. This is still a
partial product surface over an accepted kernel, not evidence of turnkey
production readiness.

## 2. Standing Snapshot

### Accepted Standing

| Standing | Rows or evidence | Current meaning |
|---|---|---|
| `baseline_accepted` | BAR-001 through BAR-015 and BAR-104 | Core event, sync, authority, identity, config, projection, integrity, mobile retention, and production provider-token boundaries remain accepted. |
| `accepted runtime probes` | NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, NW-033/S26, NW-042/S22 | The current kernel has scenario-grade evidence without promoting deferred primitives. |
| `superseded` | NW-058 | The detailed first-deployment packet/gate chronology was retired after becoming an implementation blocker. |
| `accepted product surface` | NW-059 | Mobile distinguishes saved-local, waiting, syncing, synced, and failed sync states without presenting failed attempts as successful sync. |
| `accepted product surface` | NW-060 | Successful capture returns to the surviving screen, refreshes current state, confirms local save, and keeps pending work visible. |
| `accepted product surface` | NW-061 | The work list explains get-work, syncing, retry, missing setup/forms, missing assignment, and ready-to-capture states from existing local state. |
| `accepted product surface` | NW-062 | Eligible history records offer append-only correction using the same subject, exact shape, and activity while preserving the original event. |

### Current First-Deployment Claim Boundary

| Claim | Standing |
|---|---|
| Append-only configured capture and correction | `accepted` |
| Scoped offline sync and pending-event preservation | `accepted` |
| Candidate 1 operational capture over current constructs | `runtime-evidenced` |
| Mobile setup, work list, capture, correction, and sync UX | `product-surface-partial` |
| Production mobile OIDC/login and production web admin auth | `needs-decision` |
| Retention/security, reporting/export, and scaled conflict operations | `needs-decision` |
| Turnkey production readiness | `blocked` pending separate product and operations evidence |

### Deferred Or Future-Decision Lanes

| Route | Standing | Boundary |
|---|---|---|
| BAR-105 / NW-021 S06 entity lifecycle | `deferred` / `future_decision` | Required before maintained known-set, active/inactive/retired truth, registry stewardship, or lifecycle UX. |
| BAR-106 / NW-054 device data lifecycle | `future_decision` | Owns expiry, decommissioning, sealed-partition recovery, local encryption, redaction/no-local-retention, and token/session retention. |
| NW-044 reporting and import/export | `future_decision` | S26 proves report inputs, not a production reporting API, warehouse, export, or import boundary. |
| NW-045 conflict automation and batch handling | `future_decision` | Must preserve exact designated-resolver equality and per-flag event semantics. |
| BAR-108 / NW-053 new scope mechanisms | `future_decision` | Current geography, subject-list, activity, and temporal assignment axes remain the only accepted scope mechanisms. |
| Production web admin auth and mobile OIDC/login | candidate routes in NW-056 | Must be selected and bounded before implementation; provider claims and UI choices are not platform authority. |
| Operations runbook and rehearsal | candidate route in NW-056 | Needed before claiming operator-deployable-with-constraints has become repeatable production operations. |

No baseline acceptance candidate or first-deployment implementation task is
currently active. NW-056 candidate routes become backlog rows only when the
steward selects one.

## 3. Recent Movement

| Commit | Meaning | Evidence |
|---|---|---|
| `feee9ba` | Collapsed the workshop into a summary, a small router, and one bounded implementation task. Removed 6,862 lines of active packet, role, stage, and gate-review material. | NW-058 is `superseded`; removed material remains available in git history only. |
| `8692607` / `d234007` | Implemented and accepted mobile sync-status presentation as NW-059. | 13 focused tests, 114 full mobile tests, clean touched-file analysis. |
| `b1fad6d` / `5dad1c9` / `b070033` | Routed, implemented, and accepted offline capture handoff as NW-060. | 12 focused tests, 119 full mobile tests, clean touched-file analysis. |
| `2de4711` / `1b4ca81` / `e6cde18` | Routed, implemented, and accepted mobile work readiness as NW-061. | 19 focused tests, 126 full mobile tests, clean touched-file analysis. |
| `703470d` / `9e8670a` / `9f2c3c9` | Routed, implemented, reviewed, and accepted append-only correction UX as NW-062. | 10 focused tests, 131 full mobile tests, clean touched-file analysis. |

The June 12 checkpoint's FD-PKT march orders are historical. Commit `feee9ba`
superseded that packet chain because it was obstructing implementation.
Current first-deployment routing starts from the summary and completed bounded
tasks, not from reconstructed workshop chronology.

### Iteration Outcome

| Iteration | User problem closed | Explicitly unchanged |
|---|---|---|
| NW-059 sync status | Users can distinguish local save, waiting, active sync, successful sync, and failure. | Sync protocol, watermarks, persisted workflow state, authority. |
| NW-060 capture handoff | A successful offline capture confirms on the surviving screen and remains visible as pending. | Contracts, retention, entity lifecycle, subject-link authority. |
| NW-061 work readiness | Users can understand whether to get work, retry, obtain setup/assignment, or capture. | Persisted readiness state, login, sync semantics, mobile authority. |
| NW-062 correction | Users can prefill and append a changed correction while retaining original history. | Event mutation, durable correction linkage, new envelope/type/payload metadata, shape inference, entity lifecycle. |

## 4. Architecture Guardrails

### Source Order

| Order | Source | Role |
|---|---|---|
| 1 | CDL, sliced through its README or `scripts/query_cdl.py` | Architecture authority. |
| 2 | `contracts/` | Process-boundary envelope, sync, flag, shape, config, pattern, and fixture contracts. |
| 3 | `docs/agent-working-surface/decision-anchor-layer/` | DEC anchors and gap routing. |
| 4 | BAR and platform NW backlog | Accepted standing, evidence, and future-work routes. |
| 5 | Operational UX companion | Non-authoritative product vocabulary and UI layering guardrail. |
| 6 | Escape-hatch register | Measured evolution routes only; not implementation permission. |
| 7 | Scenarios, phase files, IDRs, checkpoints, and workshop history | Product pressure and provenance when explicitly routed. |

### Non-Negotiables

| Guardrail | Practical meaning for the next slice |
|---|---|
| No new envelope fields or event `type` values without successor authority. | Product labels, correction terminology, login concepts, and reporting terms do not become event vocabulary. |
| Events remain append-only and canonical. | Corrections append; UI and projections do not rewrite original records. |
| Projections remain derived and rebuildable. | Readiness, progress, report inputs, and UI status do not become durable workflow truth. |
| Mobile remains advisory. | Missing assignment, stale state, or warnings do not become mobile-authored authoritative rejection. |
| Authority remains server-derived. | Assignments and explicit principal bindings govern authority; IdP groups/claims/JWT `actor_id`, request bodies, and UI-selected actors do not. |
| Sync semantics remain stable unless separately decided. | Product polish does not rewrite normal watermarks or turn subject-history into broad audit pull. |
| S06 remains separate. | Candidate 1 UX cannot imply maintained registry/lifecycle truth. |
| Product evidence is not production-readiness evidence. | The four mobile slices do not settle admin auth, mobile login, retention, reporting, backup, monitoring, or runbooks. |

All escape hatches remain `inactive_until_triggered`; no measured trigger was
claimed by the workshop or implementation iterations.

Production auth remains accepted only for validated provider tokens followed
by explicit active `(issuer, subject) -> actor_id` binding. That does not
provide mobile OAuth/OIDC login UX, production web admin authentication,
online principal-binding administration, or IdP group/claim authority.

## 5. Risk Pulse

### New Or Elevated Risks

| Risk | Severity | Trigger | Mitigation | Needs backlog row |
|---|---:|---|---|---|
| First-deployment success is overread as turnkey readiness | A | The four mobile slices are used to claim production deployment completion. | Keep NW-056 and the first-deployment claim table attached to planning; require separate auth, operations, retention, and reporting evidence. | No; existing candidate/future routes cover it. |
| Next-lane ambiguity recreates workshop sprawl | B | Several product, auth, security, and operations concerns are opened together without one selected route. | Select one bounded successor, create one task packet, and preserve stop conditions. | Only after a lane is selected. |
| Production access remains incomplete | A | A deployment needs authenticated web administration or provider-driven mobile login. | Decide either the production admin-auth boundary or mobile OIDC/token lifecycle as a bounded route before implementation. | Candidate route should become a row when selected. |
| Device exit and retained-data promises exceed current behavior | A | Shared-device deployment needs expiry, decommissioning, recovery, encryption, or no-local-retention behavior. | Route NW-054/BAR-106 before UX, policy, or deletion implementation. | Already routed by NW-054. |
| Correction lineage is inferred from proximity | C | Product requirements later need an exact correction-to-original relationship. | Keep current wording honest; route durable linkage as an architecture/contract question instead of adding hidden metadata. | No, unless concrete product pressure appears. |

### Resolved Or De-Risked Items

| Item | Severity before | What changed | Residual risk |
|---|---:|---|---|
| Workshop packet/gate chain blocked implementation | A | `feee9ba` consolidated durable conclusions and retired the active chronology. | Future sessions must resist recreating role/stage/gate packets. |
| Failed sync looked like successful freshness | A | NW-059 preserves the last successful timestamp and presents failed pending work honestly. | Network recovery and production login UX remain partial. |
| Successful capture had weak continuation feedback | B | NW-060 returns an explicit save result, refreshes state, and confirms local pending status. | Broader handoff/history UX remains partial. |
| Users could not tell whether the device was ready | B | NW-061 derives clear readiness states from existing config, assignment, and sync state. | Assignment remains advisory on device; server authority still decides. |
| Correction risked duplicate or wrong-subject append | A | NW-062 compares effective payloads, blocks reverted duplicates, and preserves the selected event subject ref. | There is intentionally no durable correction linkage. |

## 6. Scenario And Product Pressure

| Pressure | Current classification | Current product movement | Remaining route |
|---|---|---|---|
| S00 structured capture/correction | Accepted runtime evidence plus accepted mobile UX slice | Capture and append-only correction now have direct mobile affordances and history-preserving copy. | Do not add mutable history or correction metadata without authority. |
| S19 offline operation | Accepted runtime evidence plus accepted mobile UX slices | Local save, pending work, failure, retry, successful-sync freshness, and continuation are visible. | Retention, decommissioning, secure token lifecycle, and production network operations remain separate. |
| S01 subject-linked capture | Supported within current identity constructs | Subject detail can capture and correct against existing refs and alias-aware history. | Maintained known-set or lifecycle truth remains S06/BAR-105. |
| S06 entity lifecycle | Product-needed but deferred | The workshop kept this lane visible without absorbing it into Candidate 1. | NW-021/BAR-105 successor decision before lifecycle implementation. |
| S21 conflict/review operations | Accepted kernel/runtime evidence | Current mobile work did not productize reviewer queues or scaled resolution. | NW-045 and production admin-auth route. |
| S23 setup/config | Accepted kernel/runtime evidence | Work readiness can explain missing setup/forms. | Product-grade config authoring and production admin auth remain partial. |
| S24/S25 data and worker lifecycle | Active product pressure with partial kernel support | Actor partitions, assignments, selective retention, and handoff cover only part of the journey. | NW-054 for expiry, exit, recovery, encryption, and retained local data. |
| S26 reporting | Runtime-evidenced inputs only | No reporting surface was added by these iterations. | NW-044 before durable dashboard/API/export/import work. |

The wording correction since the previous checkpoint is important: first
deployment no longer routes through FD-PKT stages and gates. The workshop is
closed, four implementation tasks are accepted, and the next product pressure
must be selected through the normal BAR/NW and decision-anchor surfaces.

## 7. Verification Ledger

| Verification | Result |
|---|---|
| NW-059 focused mobile suite | `flutter test test/sync_panel_test.dart test/sync_service_test.dart` passed 13 tests. |
| NW-059 full mobile suite | `flutter test` passed 114 tests; touched-file analysis was clean. |
| NW-060 focused mobile suite | `flutter test test/capture_handoff_test.dart test/sync_panel_test.dart` passed 12 tests. |
| NW-060 full mobile suite | `flutter test` passed 119 tests; touched-file analysis was clean. |
| NW-061 focused mobile suite | `flutter test test/work_readiness_test.dart test/capture_handoff_test.dart test/sync_panel_test.dart` passed 19 tests. |
| NW-061 full mobile suite | `flutter test` passed 126 tests; touched-file analysis was clean. |
| NW-062 focused mobile suite | `flutter test test/correction_flow_test.dart test/capture_handoff_test.dart` passed 10 tests. |
| NW-062 full mobile suite | `flutter test` passed 131 tests. |
| NW-062 touched-file analysis | `dart analyze` over the changed screens and focused tests reported no issues. |
| Review cleanup | `git diff --check` passed before the NW-062 implementation and status commits. |
| Checkpoint diff hygiene | `git diff --check` passed after this checkpoint was written. |
| Checkpoint basis | Worktree was clean before this checkpoint; current branch was 24 commits ahead of `origin/main`; recent history was anchored on `9f2c3c9`. |

## 8. March Orders

1. **Hold one explicit next-route selection discussion.**
   - Why now: the workshop and four mobile iterations are complete, while
     operations, admin auth, mobile login, retention, and reporting are
     separate concerns with different authority boundaries.
   - Expected artifact: one selected route promoted to a backlog row and one
     bounded task or decision packet with files, guardrails, tests, commit
     boundary, and stop conditions.
   - Scope: compare the NW-056 P1 candidates and the first-deployment lane
     register; choose one primary outcome and dependencies.
   - Stop condition: the proposal bundles general "production readiness" into
     one implementation stream or recreates workshop role/stage/gate chains.

2. **If repeatable deployment operations are selected, route the runbook and
   rehearsal first.**
   - Why now: the kernel is operator-deployable with constraints, but backup,
     restore, upgrades, TLS, secrets, monitoring, incident response, config
     publication, binding manifests, and assignment bootstrap are not a tested
     operating procedure.
   - Expected artifact: a bounded operations-readiness backlog row plus
     deployment checklist, rehearsal plan, and evidence requirements.
   - Scope: operational documentation and rehearsal evidence over accepted
     runtime behavior.
   - Stop condition: the route treats development compose files as production
     hardening or changes runtime contracts incidentally.

3. **If production access is selected, choose web admin auth or mobile OIDC
   login as the first decision, not both.**
   - Why now: server OIDC/JWKS validation is accepted, but neither production
     admin authority nor mobile provider login/token lifecycle is settled as a
     product flow.
   - Expected artifact: one bounded product/platform decision defining actor
     resolution, token/session lifecycle, authority, audit, error recovery,
     and explicit deferrals.
   - Scope: BAR-104, IDR-027, IDR-028, IDR-029 or IDR-030 as applicable, plus
     the exact web or mobile surface selected.
   - Stop condition: IdP groups/claims/roles, JWT `actor_id`, request-body actor
     IDs, fixed dev actors, or UI selection become platform authority.

4. **If device/security readiness is selected, run NW-054 before UX or data
   deletion work.**
   - Why now: shared-device partitions exist, but exit, expiry,
     decommissioning, recovery, local encryption, redaction, and token/session
     retention remain unresolved.
   - Expected artifact: a security/platform decision with explicit local-data,
     server-history, recovery, and operational-policy boundaries.
   - Scope: BAR-106, IDR-030, NW-055, S24, S25, and current selective-retention
     behavior.
   - Stop condition: canonical server history is deleted, normal sync
     watermarks are rewritten, or one actor can recover another actor's pending
     work without explicit security authority.

5. **Keep lower-priority expansion routes unpromoted until pressure is
   selected.**
   - Why now: reporting/import-export, conflict batch handling, custom scope,
     and entity lifecycle are real needs, but none is the active task.
   - Expected artifact: no change until the route-selection discussion names
     NW-044, NW-045, NW-053, or NW-021 as the next bounded outcome.
   - Scope: preserve current backlog standing and scenario pressure.
   - Stop condition: UI design, deployment urgency, or scenario wording is
     treated as authority to implement a future-decision surface.
