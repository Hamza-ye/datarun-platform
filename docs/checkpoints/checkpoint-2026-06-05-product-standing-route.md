# Project Checkpoint - 2026-06-05 (Product Standing Route)

---

## 1. Bearing

- **Anchor commit**: `4192297 feat(mobile): partition shared device actor sessions`
- **Phase**: Post-Phase-4 stabilization.
- **Momentum**: `ADVANCING` - the platform baseline is accepted through BAR-001 through BAR-015 and BAR-104, and NW-055 has landed shared-device actor partitions.
- **Last milestone**: NW-055 accepted IDR-030 shared-device actor partitions across mobile local stores and server actor-scoped sync bookkeeping.
- **Horizon**: create an actionable product-standing and production-readiness map before starting broad web admin, mobile UX, login/provider, or deployment-hardening work.

Datarun currently has an accepted platform kernel with strong runtime evidence for core sync, configuration, authority, identity, integrity, workflow projection, production auth binding, assignment-admin authority, and shared-device actor partitioning. It is not yet a complete packaged product surface: production admin auth, mobile OIDC/Keycloak login UX, deployment runbooks, retention/security decisions, reporting/export product boundaries, and polished operational vocabulary/screens remain routed work. Today's checkpoint adds NW-056 as the next product-readiness analysis route so the project can decide what UI/product work can start without accidentally promoting deferred architecture.

## 2. Standing Snapshot

### Accepted Platform Standing

| Standing | Rows or evidence | Meaning |
|---|---|---|
| `baseline_accepted` | BAR-001 through BAR-015 | Event envelope/type closure, append-only store, sync/backfill, payload contracts, flags/resolvers, assignment/scope, mobile selective retention, identity, config, expressions, patterns, transition/domain detection, projection equivalence, and historical location-path immutability are accepted. |
| `baseline_accepted` | BAR-104 | Production OIDC/JWT/Keycloak authority is accepted only through explicit `(issuer, subject) -> actor_id` binding, OIDC/JWKS validation, and deployment-managed binding provisioning. |
| `accepted runtime probes` | NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, NW-033/S26, NW-042/S22 | Current code has scenario-grade evidence for structured capture, offline/stale authority, supervisor review, logistics transfer, setup/config, reporting/aggregate oversight, and coordinated distribution campaign pressure. |
| `accepted auth/admin evolution` | NW-048, NW-050, NW-052, NW-055 | Assignment-admin command capability and shared-device actor partitions are settled within current authority guardrails. |
| `accepted UX routing companion` | NW-047 | Operational UI/product vocabulary has a non-authoritative companion; concrete UI work must use it before design/code. |

### Product Surface Snapshot

| Surface | Current standing | Product implication |
|---|---|---|
| Server runtime APIs | Strong platform/kernel standing | Sync, config, assignment, conflict, auth-me, identity, subject, and location APIs exist, but product packaging/runbooks still need a readiness map. |
| Web admin/config | Partial development/admin surface | Thymeleaf screens exist for subjects, flags, assignments, locations, shapes, activities, expressions, severity, and config publish. `WebConfig` and `AdminController` state the HTML admin console is development-only until production admin auth lands. |
| Mobile app | Functional prototype surface | Setup takes server URL plus bearer credential, verifies `/api/auth/me`, stores actor session, syncs, shows work list, opens forms, and uses per-actor partitions. It is not yet a production OIDC/Keycloak login UX or polished field-worker product. |
| Production deployment | Kernel-capable with constraints | Server Dockerfile and dev compose exist; production OIDC/JWKS and binding manifests exist. Production hardening, secrets, backups, observability, runbooks, admin auth, and retention/security are not accepted product surfaces. |
| Reporting/product dashboards | Scenario-evidenced, not productized | S26 proves bounded freshness/drill-back semantics; it does not accept a reporting API, warehouse, export/import contract, or dashboard product. |
| Retention/security | Future decision | BAR-106 and NW-054 remain the route for expiry, decommissioning, sealed partition recovery, local encryption, token/session retention, and redaction/no-local-retention behavior. |

### Active Or Ready Routes

| Route | Status | Why it matters now |
|---|---|---|
| NW-056 Product standing and production readiness map | `ready` | Needed before deciding which product/UI/deployment surfaces can start and which need successor decisions. |
| NW-054 Device data expiry and retained local data boundary | `future_decision` | Needed if production/security planning requires expiry, sealed partitions, decommissioning, encryption, or token/session retention policy. |
| NW-044 Reporting aggregation/import-export boundary | `future_decision` | Needed before general reporting APIs, dashboards backed by new storage, export, or import/event ingestion. |
| NW-045 Domain conflict automation and batch resolution | `future_decision` | Needed before batch/pending-match UX changes resolver or auto-resolution boundaries. |
| NW-053 Subject/query/custom scope boundary | `future_decision` | Needed before any new product scope mechanism beyond geography, subject-list, activity, and time-bounded assignments. |

## 3. Recent Movement

| Commit | Meaning | Evidence |
|---|---|---|
| `4192297` | NW-055 implemented shared-device actor partitions. | Status records full `flutter test` at 107 tests and full `./mvnw test` at 321 tests, plus focused sync/auth/config/backfill/scenario evidence. |
| `3a0b9ab` | Updated agent routing docs and status clarity. | `docs/status.md`, `AGENTS.md`, and query-tool docs now point at current routing surfaces. Working tree still has an uncommitted AGENTS.md overlay, noted below. |
| `65dcce1` | Accepted the architecture rationale companion. | Working-surface README includes the companion as non-authoritative routing/test-intent context. |
| `ed90a1d` | Decided shared-device sessions. | IDR-030 selected single-active-actor sessions, drain-or-seal switching, and per-actor local partitions. |
| `cf1e420` | Routed the shared-device decision. | NW-052 prompt/route prepared the IDR-030 decision slice. |
| `0420233` | Decided special access boundary. | NW-051 kept simple current-scope auditor visibility as ordinary assignments and deferred broad audit/history and special write access. |
| `0742f9c` | Organized working-surface artifacts. | Artifact placement conventions are now clearer for future non-authoritative NW outputs. |
| Checkpoint work | Added NW-056 route and this checkpoint. | New prompt and backlog row route product-standing analysis without changing runtime code or authority. |

## 4. Architecture Guardrails

### Source Order

| Order | Source | Role |
|---|---|---|
| 1 | CDL via README/JSON/query tool | Architecture authority. |
| 2 | Architecture rationale companion | Non-authoritative routing, test-intent, and change-classification context. |
| 3 | Operational UX layering companion | Non-authoritative product vocabulary and UI layering guardrail. |
| 4 | Escape-hatch register | Inactive measured evolution triggers, not implementation permission. |
| 5 | BAR and platform backlog | Current accepted/candidate/deferred/future-decision implementation standing. |
| 6 | Contracts and module interfaces | Process-boundary contracts and implemented module boundaries. |
| 7 | Scenarios and constraints | Product pressure and problem-space coverage, not implementation authority. |

### Non-Negotiable Guardrails

| Guardrail | Product implication |
|---|---|
| No new envelope fields or event `type` values without successor authority. | UI/product labels such as progress, handoff, campaign, login, admin, or provider must not become event/schema vocabulary. |
| Events are canonical; projections are rebuildable. | Dashboards and UI queues must be read-side interpretations unless routed as platform evolution. |
| Structurally valid state/policy anomalies are accepted and flagged. | Mobile UX may warn, but must not reject anomalies that server should accept-and-flag. |
| Authority stays event/assignment-derived. | Keycloak/OIDC groups, roles, resource claims, and JWT `actor_id` are not platform authority. |
| Scope mechanisms are platform-fixed. | Auditor, campaign, custody, query, or custom scopes need successor routing before implementation. |
| Production binding admin is deployment-managed. | Online principal-binding admin UI/API is not accepted yet. |
| HTML admin console is development-only. | Web admin/config UX can be designed, but production admin authentication and command authority must be routed explicitly. |

### Escape Hatch Status

All escape hatches remain `inactive_until_triggered`. No checkpoint work measured a trigger for materialized projections, persisted authority snapshots, actor-scoped ordering metadata, shape migration tooling, expression expansion, or pattern inventory expansion.

### Auth And Provider Boundary

Production auth is accepted for validated provider JWTs only after explicit `(issuer, subject) -> actor_id` binding lookup. That proves the server-side provider boundary and actor mapping. It does not provide mobile OIDC login UX, online binding administration, web admin authentication, IdP group authority, or product-ready Keycloak operations.

## 5. Risk Pulse

### New Or Elevated Risks

| Risk | Severity | Trigger | Mitigation | Needs backlog row |
|---|---:|---|---|---|
| Product surface overclaim | B | Accepted platform rows could be mistaken for complete user-facing product readiness. | NW-056 must classify kernel, scenario evidence, operator deployment, and UX surfaces separately. | Yes: NW-056 added. |
| Production deployment overclaim | B | Dockerfile/dev compose plus OIDC/JWKS support can look like production hardening. | NW-056 production readiness section must separate auth capability from secrets, backups, observability, runbooks, admin auth, and retention/security. | Covered by NW-056; successors likely. |
| UI vocabulary leakage | B | Product terms for campaign, progress, pending review, provider, admin, or blocked work could become platform authority. | Use NW-047 companion before UI/product design. | Covered by NW-056. |
| Mobile login/provider ambiguity | B | Mobile currently accepts a bearer credential, not a full OIDC/Keycloak login flow. | Route mobile login UX separately after NW-056 identifies the needed contract and session behavior. | Likely successor from NW-056. |
| Retention/security gap | A | Production/shared-device pressure raises expiry, sealed partition, token retention, encryption, and redaction questions. | Run NW-054 or make a security/platform decision before implementing purge/recovery UX. | Existing NW-054/BAR-106. |

### Resolved Or De-Risked Items

| Item | Severity before | What changed | Residual risk |
|---|---:|---|---|
| Production auth actor mapping | B | NW-037 through NW-040 accepted explicit principal binding, OIDC/JWKS validation, and deployment-managed provisioning. | Product login/admin UX and IdP claim authority remain separate. |
| Assignment-admin authority | B | NW-048/NW-050 added command capability plus containment enforcement. | Broad special access and emergency write bypasses remain deferred. |
| Shared-device actor partitions | B | NW-052/NW-055 accepted and implemented per-actor local partitions and actor-scoped server bookkeeping. | Expiry, decommissioning, sealed recovery, and retention/security remain NW-054. |
| Operational UX vocabulary boundary | C | NW-047 companion now exists and is part of source order for product/UI slices. | Concrete screens still need a product-standing map and bounded prompts. |

## 6. Scenario And Product Pressure

| Scenario or pressure | Current classification | What can run today | Product/UI caveat |
|---|---|---|---|
| S00 structured capture | Accepted runtime evidence | Structured capture, correction append, idempotent replay, ordered pull. | Needs product-grade form UX, vocabulary, and setup polish. |
| S19 offline/stale authority | Accepted runtime evidence | Offline work persists and stale authority emits existing flags without live-sync watermark drift. | Needs field-worker warning language and supervisor handling UX. |
| S21 supervisor review | Accepted runtime evidence | Scoped supervisor review, unauthorized review flagging, projection exclusion/re-inclusion by resolver action. | Needs review queue/screen design; no resolver reassignment or batch bypass. |
| S22 coordinated distribution campaign | Accepted runtime evidence | Existing constructs cover campaign/distribution probe with assignments, subject history, transfer pattern, flags, and scoped sync. | Campaign/progress words stay UX/config terms; entity lifecycle, triggers, custom scope, and reporting warehouse remain deferred. |
| S23 setup/config | Accepted runtime evidence | Shape/activity/expression/severity/pattern config can validate, publish, package, and reach mobile. | Web config UI exists but is JSON-heavy and development-oriented; product-grade admin UX needs routing. |
| S26 reporting/aggregate oversight | Accepted runtime evidence | Freshness, unresolved flag treatment, scoped inputs, and drill-back are proven in scenario tests. | No accepted general reporting API, dashboard, export, import, or warehouse. |
| S27 logistics transfer | Accepted runtime evidence | Transfer-with-acknowledgment pattern covers dispatch/receipt/discrepancy with transition flags and scoped sync. | No logistics-specific platform semantics or custody scope. |
| S24 long-running data lifecycle | Active pressure | Current selective retention and shared-device partitions help. | BAR-106/NW-054 must decide expiry, local encryption, redaction, sealed partitions, and retained local data. |
| S25 worker onboarding/transfer/exit | Active pressure | Assignment history, subject-history, stale flagging, and shared-device partitions cover parts of the flow. | Worker lifecycle UX, exit/decommissioning, and retention policy still need routing. |

## 7. Verification Ledger

| Verification | Result |
|---|---|
| Source routing read | `AGENTS.md`, `docs/status.md` Current Routing, working-surface README/BAR/backlog, latest checkpoint, scenarios README, module interfaces, UX companion, rationale companion, README/constraints, IDR-027/028, and focused admin/mobile/deployment code were inspected. |
| Git anchor read | `git log --oneline -8` and `git show --stat -1 HEAD` confirm the checkpoint is based on `4192297`. |
| Focused source check | `WebConfig` and `AdminController` confirm the HTML admin console is development-only; `ConfigAdminController` confirms config admin screens exist; mobile setup confirms bearer-credential `/api/auth/me` setup rather than full OIDC login UX; `docker-compose.yml` is development-grade. |
| Runtime tests | No Maven or Flutter tests were run because this checkpoint and NW route add documentation only. Existing evidence is reused from BAR/backlog rows. |
| Diff hygiene | `git diff --check` is required after writing this checkpoint and NW prompt. |

## Dirty Worktree Overlay

| File | Status | Interpretation |
|---|---|---|
| `AGENTS.md` | Modified before this checkpoint work | The diff adds contract/test co-update guidance. It aligns with current routing, but it was not created by this checkpoint pass and is left untouched. |

## 8. March Orders

1. **Run NW-056 product standing and production readiness map.**
   - Why now: the project needs a complete product picture before starting broad UI/admin/mobile/login/provider work.
   - Expected artifact: `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`.
   - Scope: analysis, source inspection, capability matrix, scenario map, production readiness classification, and successor routes.
   - Stop condition: the analysis requires changing runtime behavior, adding contracts, or claiming product readiness not backed by BAR/backlog evidence.

2. **Route retention/security through NW-054 before production data-handling UX.**
   - Why now: shared-device partitions landed, but expiry, sealed recovery, decommissioning, local encryption, and token/session retention remain unresolved.
   - Expected artifact: NW-054 retention boundary artifact or a stop report naming the missing security/platform decision.
   - Scope: local retained data, selective purge, sealed partitions, token/session retention, and sensitivity pressure.
   - Stop condition: proposed path deletes server history, rewrites normal sync watermarks, or treats UI hiding as sufficient security.

3. **Start web admin/config UX only after NW-056 defines the product boundary.**
   - Why now: config publishing works, but the existing HTML admin is development-oriented and not production-authenticated.
   - Expected artifact: bounded successor prompt for web admin/config UX and production admin auth assumptions.
   - Scope: screen IA, safer config authoring, validation feedback, publish workflow, and development-vs-production admin boundary.
   - Stop condition: screen design requires online binding-admin authority, new config mechanisms, new scope types, or IdP claim authority.

4. **Start mobile UX/login/provider work only after NW-056 separates login from authority.**
   - Why now: mobile currently verifies a bearer credential through `/api/auth/me`, but a product user expects provider login, actor switching, session recovery, and understandable offline warnings.
   - Expected artifact: bounded mobile login/session UX prompt, likely depending on IDR-027/030 and any NW-054 output.
   - Scope: OIDC/Keycloak login UX, active actor display, shared-device switch affordances, sync status, form/work-list vocabulary, and advisory warnings.
   - Stop condition: mobile UI becomes authoritative for actor identity, scope, or rejection of structurally valid work.

5. **Keep reporting/export/import and conflict batch UX separate from product polish.**
   - Why now: S26 and conflict resolution are evidenced, but general dashboards, exports, imports, pending-match, and batch resolution touch deeper platform boundaries.
   - Expected artifact: NW-044 and NW-045 style exploration before implementation.
   - Scope: reporting read models, export/import contracts, batch commands, pending-match queues, and resolver semantics.
   - Stop condition: proposal creates canonical aggregate truth, bypasses exact designated-resolver equality, or adds auto-resolution/resolver reassignment by UI convenience.
