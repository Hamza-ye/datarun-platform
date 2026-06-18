# Project Checkpoint - 2026-06-18 - Post Reference Deployment Rehearsal

## 1. Bearing

- **Anchor commit**: `8f4a013 docs(architecture): record multi-tenancy routing analysis`; last accepted ops/status standing commit is `2d8d047 test(ops): accept production deployment rehearsal`.
- **Phase**: Post-Phase-4 stabilization; synthetic first reference deployment rehearsal is accepted, durable-behavior cleanup is current, and production web admin auth is decided but not implemented.
- **Momentum**: `ADVANCING` - the project converted first-deployment operations from policy/tooling/runbook into composite rehearsal evidence while keeping real production and new platform boundaries explicitly unproven.
- **Last milestone**: NW-067 accepted by composite synthetic reference-environment evidence after NW-075 through NW-081 closed backup, rotation, alerting, recovery-point, and fresh-session token-path gaps.
- **Horizon**: choose one bounded successor: real-production approval route, NW-079 implementation, multi-tenancy decision routing, or one of the remaining candidate durable-surface extractions.

Since the previous checkpoint at `0f07c30`, the main movement was operational:
NW-063 through NW-067 established and rehearsed the first reference deployment
lane, then NW-075 through NW-081 repaired the partial cold-recovery/adaptor gaps.
The second movement was documentation control: accepted behavior was moved out
of IDR-era scatter into durable specs and active references. NW-079 selected a
production web admin auth and authority model, and `8f4a013` committed a
non-binding multi-tenancy analysis that still needs explicit routing before it
can become backlog-visible or authoritative.

## 2. Standing Snapshot

### Accepted Standing

| Standing | Rows or evidence | Current meaning |
|---|---|---|
| `baseline_accepted` | BAR-001 through BAR-015 and BAR-104 | Core event, sync, authority, identity, config, projection, integrity, mobile retention, and production OIDC/JWT/Keycloak authority remain accepted. |
| `accepted scenario probes` | NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, NW-033/S26, NW-042/S22 | Scenario-grade runtime evidence exists without promoting deferred primitives. |
| `accepted ops route` | NW-063, NW-064, NW-065, NW-066 | Reference target, owner policy, hardened reference tooling, production deployment runbook, and rehearsal plan are accepted. |
| `accepted synthetic rehearsal` | NW-067 plus NW-075 through NW-081 | R1-R12 synthetic reference deployment evidence is accepted; real production and independent human continuity remain unproven. |
| `accepted durable specs` | NW-068, NW-069, NW-070 | Configuration, assignment/scope/admin, and production auth/principal-binding behavior now has durable platform specs and indexed routes. |
| `accepted hygiene` | NW-074 | Active routers, decision anchors, module-interface references, decision indexes, and active comments route accepted NW-068 through NW-070 behavior through specs/contracts before IDR provenance. |
| `accepted admin-auth decision` | NW-079 | Production web admin model is selected: OIDC/JWKS browser login, server-managed session, explicit-principal-bound admin actor context, command-specific authority, S23 setup authority split, and dev-console containment. No runtime UI/session/API code changed. |

### Candidate, Deferred, And Future-Decision Standing

| Route | Standing | Boundary |
|---|---|---|
| NW-071 | `candidate` | Shared-device session/local-state durable spec remains needed before work depends on IDR-030 behavior as normative, especially tenant/deployment-aware mobile partitioning. |
| NW-072 | `candidate` | Conflict flag/resolution durable spec remains needed before conflict UI, flag reporting, batch resolution, resolver reassignment, auto-resolution, or flag/resolvability changes. |
| NW-073 | `candidate` | Pattern registry/projection durable spec remains needed before pattern API, traversal/reporting, inventory expansion, or workflow projection changes. |
| BAR-101 / BAR-102 | `deferred` | General trigger execution and auto-resolution are not current baseline behavior. |
| BAR-103 / BAR-107 / BAR-108 | `future_decision` | Resolver reassignment, new envelope fields/types, and new scope mechanisms need successor authority before implementation. |
| BAR-105 / NW-021 | `deferred` / `future_decision` | S06/entity lifecycle remains visible but not promoted. |
| BAR-106 / NW-054 | `future_decision` | Field-level sensitivity, local expiry, device decommissioning, sealed recovery, local encryption, redaction/no-local-retention, and token/session retention remain undecided. |
| NW-044 / NW-045 / NW-046 / NW-053 | `future_decision` | Reporting/import-export, conflict automation/batch handling, generic flag cascade/pattern traversal, and subject/query/custom scope are not active implementation routes. |

### Non-Authority Overlay

| Artifact | Standing | Required next step |
|---|---|---|
| `docs/agent-working-surface/artifacts/2026-06-18-multi-tenancy-architecture-analysis.md` | Committed non-binding exploration/routing artifact in `8f4a013`; no CDL, contract, BAR, backlog, runtime, schema, or status standing changed. | Promote an explicit NW/gap route before treating tenant/workspace vocabulary, silo/bridge/pool choice, membership, or isolation tests as accepted work. |

## 3. Recent Movement

| Commit(s) | Meaning | Evidence |
|---|---|---|
| `b68ebe7` | Standardized durable documentation and commit workflow. | `docs/documentation-organization.md` and `docs/commit-workflow.md` became active standards used by later ops/spec/checkpoint work. |
| `a19ccc0`, `c402e1`, `cfb6c6a`, `6465772` | Routed deployment readiness, mapped first reference target, proposed and accepted the first reference deployment policy. | NW-063 selected the single Linux app host plus external PostgreSQL 16 reference target; NW-064 accepted provider-neutral solo-owner policy with 1-hour RPO and 8-hour RTO. |
| `617f895`, `e458b73`, `b6d6515`, `3dc09b5`, `795076b` | Hardened image/runtime/deploy assets/provisioning and accepted reference tooling. | Full server suite passed 339 tests; focused provisioning/auth/config/assignment suites passed 125 tests; image verification and digest-selected reference preflight passed. |
| `c4698f6`, `2cf6bd3`, `78d0f88`, `622b5c4` | Wrote and accepted production deployment runbook and rehearsal plan, then aligned the rehearsal gate to solo fresh-session cold recovery. | Bash blocks passed `bash -n`; `git diff --check` passed; NW-066 accepted runbook/plan but did not claim rehearsal evidence. |
| `2fd7498`, `43f0cfb`, `3b4dfb0`, `287131a` | Assessed and extracted configuration durable behavior. | NW-068 accepted expression/config-package specs and `/api/sync/config` protocol notes with no runtime or schema change. |
| `9a6ab76`, `757d6c8` | Routed remaining durable-surface follow-ups and clarified future-decision triggers. | NW-071 through NW-073 stayed candidate; NW-044, NW-045, NW-046, NW-053, and NW-054 stayed future-decision. |
| `baa59fa`, `035fd9e`, `1dcbd8f`, `e370508`, `13c90fc`, `4a0f115`, `62c1ffe`, `1ad6b5e` | Recorded partial NW-067 rehearsal, routed adapter blockers, and accepted encrypted backup, JWKS rotation, DB credential rotation, and monitoring alert adapters. | NW-075 through NW-078 accepted backup encryption/restore, Keycloak/JWKS rotation, DB credential rotation, and alert delivery lab adapters. |
| `d528774`, `f650c51`, `329a740`, `aafbac3`, `52e20e9` | Assessed and cleaned working-surface process hygiene. | Active routing clarified flagged positions as provenance, daily routing, and solo AI agent operating guidance. |
| `0ef754c`, `5494576`, `2ea43b3`, `db9b1bd`, `4e0447a`, `fed22cc` | Routed, extracted, accepted, and reference-cleaned assignment/auth durable specs. | NW-069, NW-070, and NW-074 accepted; old IDR-era references are no longer the first active route for accepted config/assignment/auth behavior. |
| `8afcab1`, `d082bf5`, `a987cbc`, `cdb339e` | Routed and accepted production web admin authentication and admin-authority model. | NW-079 spec accepted; verification was `git diff --check`; no runtime code, UI, sessions, filters, APIs, contracts, schemas, BAR, or CDL changed. |
| `6e16e49`, `ddb1479`, `dcaf526`, `1b0b54d`, `2d8d047` | Recorded failed fresh-session cold recovery, routed/fixed blockers, and accepted NW-067 composite rehearsal standing. | NW-080 refreshed encrypted backup and cleared stale RPO; NW-081 documented fresh-session token path; R12 rerun passed with protected auth/config/pull and clean secret scans. |
| `8f4a013` | Committed multi-tenancy architecture analysis as non-binding exploration. | Artifact records tenant/workspace model analysis and suggested routing; no accepted standing changed. |

### Removed Or Superseded Active Surfaces

| Surface | Current replacement |
|---|---|
| Phase 4 review drafts and old workshop chronology | BAR, platform NW backlog, and closed first-deployment router. |
| Old architecture rationale companion as active route | Decision-anchor layer plus CDL/contracts/BAR/backlog source order. |
| `docs/flagged-positions.md` as active intake | Gap playbook, BAR, NW backlog, durable specs/contracts, and commit evidence. |
| IDR-era behavior scatter for accepted NW-068 through NW-070 surfaces | Durable platform specs plus contracts, with IDRs as provenance. |
| Initial partial NW-067 standing | Composite accepted NW-067 record, accepted NW-075 through NW-081 adapters, and 2026-06-18 R12 rerun evidence. |

## 4. Architecture Guardrails

### Source Order

| Order | Source | Role |
|---|---|---|
| 1 | CDL sliced through its README or `scripts/query_cdl.py` | Architecture authority. |
| 2 | `contracts/` | Process-boundary envelope, sync, flag, shape, config, pattern, and fixture contracts. |
| 3 | `docs/agent-working-surface/decision-anchor-layer/` | DEC anchors and gap routing. |
| 4 | BAR and platform NW backlog | Accepted standing, evidence, and future-work routes. |
| 5 | `docs/specifications/` and `docs/operations/` | Accepted product/platform behavior, policies, runbooks, and rehearsal records when routed. |
| 6 | Operational UX companion and escape-hatch register | Product vocabulary guardrail and measured evolution routes only. |
| 7 | Scenarios, IDRs, phase files, checkpoints, and artifacts | Problem pressure and provenance unless explicitly promoted. |

### Non-Negotiables

| Guardrail | Current implication |
|---|---|
| No new envelope fields or event `type` values without successor authority. | Multi-tenancy analysis explicitly rejects adding tenant fields to the event envelope in the first route. |
| Event log remains canonical; projections remain derived and rebuildable. | Ops/runbook/reporting/admin work cannot create durable workflow-state truth by procedure or UI. |
| Authority remains server-derived from assignments and explicit principal bindings. | IdP groups/claims/roles, JWT `actor_id`, request-body actors, UI-selected organizations, and locations are not authority. |
| Current scope mechanisms stay platform-fixed. | Geography, subject-list, activity, and time remain the accepted assignment axes; tenant/workspace is not a new scope shortcut until decided. |
| Structurally valid anomalies are accepted and flagged. | Ops smoke and admin UI work must not turn advisory/mobile/client checks into authoritative rejection. |
| Production web admin remains unimplemented. | NW-079 authorizes a model for successors; development admin/config/token/bootstrap surfaces stay hidden in production until a successor implements the protected boundary. |
| Real production is not accepted. | NW-067 accepts a synthetic reference rehearsal only; provider, region, jurisdiction/classification, notification/login, and organizational review still gate real deployment. |

All escape hatches remain `inactive_until_triggered`; no measured EH trigger was
claimed by the ops work, NW-079, or the multi-tenancy analysis.

## 5. Risk Pulse

### New Or Elevated Risks

| Risk | Severity | Trigger | Mitigation | Needs backlog row |
|---|---:|---|---|---|
| Synthetic reference evidence is overread as production approval | A | NW-067 accepted composite rehearsal evidence and could be mistaken for real-production readiness. | Keep real production blockers explicit: provider/region, jurisdiction/data classification, communication/login, organizational review, and fresh environment evidence. | No; current status and ops docs already carry the boundary. |
| Multi-tenancy artifact is treated as accepted architecture | A | `8f4a013` committed a detailed analysis without adding a backlog row or durable decision. | Promote a bounded NW/gap route before changing terms, schemas, auth, sync, config, mobile partitioning, or tenant isolation. | Yes, if multi-tenancy is the next selected lane. |
| NW-079 implementation expands beyond the accepted model | A | A successor might build broad admin UI, config publication, assignment admin, or online binding admin in one pass. | Promote one implementation row at a time; start with login/session/protected shell if selected. | Yes, for each implementation successor. |
| Lab monitoring/alerting is mistaken for production incident readiness | B | NW-078 used a Hamza-approved synthetic webhook recipient and lab Prometheus/Alertmanager. | Select real notification destination, escalation, and service-coverage decisions before production claim. | Only when production operations are selected. |
| RPO evidence can go stale between rehearsals | B | R12 used a fresh backup with RPO age `1981` seconds, but backup freshness is time-sensitive. | Repeat backup freshness and recovery-point checks immediately before any production go/no-go. | No; runbook/rehearsal route covers it. |

### Resolved Or De-Risked Items

| Item | Severity before | What changed | Residual risk |
|---|---:|---|---|
| Clean server image omitted root contract resources | A | NW-065 corrected packaging and verified canonical contract resources in the release image. | Future packaging changes need the same image/resource checks. |
| No accepted first reference deployment operating policy | A | NW-064 accepted solo-owner policy, RPO/RTO, backup, secrets, alerts, incident, release/recovery, and evidence controls. | Independent human continuity remains unproven until triggered. |
| No tested reference deployment runbook | A | NW-066 accepted indexed runbook and rehearsal plan tied to NW-065 tooling. | Runbook evidence remains synthetic until real production route. |
| Backup encryption/PITR was unproven | A | NW-075 proved encrypted backup restore; NW-080 refreshed encrypted recovery point and RPO. | Real provider/storage selection still pending. |
| DB credential, JWKS rotation, and alert delivery blocked NW-067 | A | NW-076, NW-077, and NW-078 accepted focused adapters. | Real production destinations and operator coverage still pending. |
| Fresh-session R12 failed on stale backup and token path | A | NW-080 and NW-081 fixed blockers; R12 rerun accepted. | Future cold recovery must still be rehearsed against current artifacts. |
| Production web admin authority was ambiguous | B | NW-079 selected the browser/session/admin-authority model and explicit non-goals. | No production implementation exists yet. |
| IDR-era routing drift for accepted behaviors | B | NW-074 drained active stale references for NW-068 through NW-070 behavior. | NW-071 through NW-073 remain candidate durable-surface extractions. |

## 6. Scenario And Product Pressure

| Pressure | Current classification | Movement since `0f07c30` | Remaining route |
|---|---|---|---|
| S23 setup/config | Accepted runtime evidence plus accepted admin-auth decision | NW-079 maps author, validate, readiness review, approve, publish, and responsibility administration authority. | Productize setup workflow only through a successor implementation row; no deployer scripts or new config schema. |
| S19 offline/sync | Accepted runtime evidence | NW-067 protected smoke proved `/api/auth/me`, `/api/sync/config`, and `/api/sync/pull` in the reference environment. | Mobile OIDC/login UX and token lifecycle remain separate product/security work. |
| S24/S25 retained data and worker lifecycle | Active pressure, not current baseline expansion | Ops work exercised recovery and shared-device-adjacent auth surfaces, but did not decide retention/security. | NW-054 and, when needed, NW-071 before expiry, decommissioning, local encryption, sealed recovery, or tenant-aware partitions. |
| S26 reporting/aggregate oversight | Runtime-evidenced inputs only | Ops monitoring exists for infrastructure signals, not product reporting/export. | NW-044 before durable reporting API, warehouse, export/import, or cross-tenant analytics. |
| Production web administration | Model accepted, implementation absent | NW-079 selects first model and successor rows. | Promote one implementation row at a time; development console remains contained. |
| Multi-tenancy / organization / workspace vocabulary | GAP-PRODUCT-01 remains open; artifact is non-binding | `8f4a013` analyzes account/tenant/workspace/org-unit/location/assignment distinctions and recommends silo/bridge first. | Promote a backlog/gap route before tenant/workspace terms become accepted product or platform language. |
| S06/entity lifecycle | Deferred/future decision | No change. | NW-021/BAR-105 before known-set, lifecycle, active/inactive/retired truth, or lifecycle UX. |

## 7. Verification Ledger

| Verification | Accepted or informed |
|---|---|
| NW-065 full server suite: `./mvnw test` | Passed 339 tests for reference tooling acceptance. |
| NW-065 focused server suites | Passed 125 provisioning/auth/config/assignment tests. |
| NW-065 image/resource/preflight checks | Packaged-JAR success/failure smokes, `scripts/verify-server-image.sh`, and full digest-selected reference preflight passed against PostgreSQL 16. |
| NW-066 runbook checks | Every Bash block passed `bash -n`; `git diff --check` passed; independent review returned `ACCEPT`. |
| NW-067 R1-R9 rehearsal | Clean install, migration repeatability after adapter correction, TLS/OIDC negative checks, provisioning idempotency, sync smoke, restore timing `rpo_seconds=4` and `rto_seconds=214`, previous-to-candidate upgrade, and failed-start containment. |
| NW-075 encrypted backup adapter | pgBackRest reported `cipher: aes-256-cbc`; restored DB counts matched source; readiness/auth/config/pull smoke passed. |
| NW-076 DB credential rotation adapter | Runtime roles g1 and g2 passed app smoke; g1 was disabled and rejected; g2 remained active with original runtime role retained as recovery. |
| NW-077 JWKS rotation adapter | New-key and old-token auth smoke returned HTTP 200 during overlap; no IdP group/claim/JWT `actor_id` authority was introduced. |
| NW-078 monitoring adapter | Six synthetic alert groups delivered with owner/runbook labels and resolved notifications; final Prometheus/Alertmanager checks showed zero active alerts. |
| NW-080 recovery-point refresh | Encrypted diff backup `20260617-022519F_20260618-131808D`; RPO age `66` seconds at refresh; zero active backup/critical alerts. |
| NW-081 fresh-session token path | Short-lived Keycloak direct-grant token path proved protected `/api/auth/me`, `/api/sync/config`, and `/api/sync/pull` with HTTP 200 and token cleanup. |
| NW-067 R12 rerun | App readiness/liveness `UP`, all monitoring targets `up`, zero critical/backup alerts, DB1 Flyway V1-V10 with 8 events/max watermark 9, encrypted backup RPO age `1981` seconds, protected smoke HTTP 200, clean secret scans. |
| NW-068, NW-069, NW-070, NW-074, NW-079 docs/spec/hygiene slices | `git diff --check` passed; no runtime behavior or contract/schema authority changed unless explicitly recorded in the row. |
| Multi-tenancy artifact commit | `git diff --cached --check` passed before `8f4a013`; artifact remains non-binding. |
| Checkpoint hygiene | `git diff --check` passed after this checkpoint was written. |

## Working Tree Overlay

Before writing this checkpoint, `git status --short` was clean after committing
`8f4a013`. The only dirty work created by this task is this checkpoint file
until it is staged and committed as `docs(checkpoint): snapshot post reference
deployment rehearsal`.

## 8. March Orders

1. **Promote the multi-tenancy analysis only through an explicit route.**
   - Why now: the analysis is useful and now committed, but it is not accepted
     architecture, product language, or backlog standing.
   - Expected artifact: one NW/gap row and bounded prompt for tenant/workspace
     vocabulary and isolation boundary, with clear dependencies on GAP-PRODUCT-01,
     NW-069, NW-070, NW-079, and relevant CDL rows.
   - Scope: decide terms and negative boundaries first: tenant, organization,
     workspace, org unit, location, membership, actor, assignment, entitlement,
     and why locations/assignments/IdP claims are not tenant isolation.
   - Stop condition: implementation starts by adding tenant columns, envelope
     fields, location-as-tenant semantics, IdP group authority, or pooled query
     filters before the decision route is accepted.

2. **If web admin is selected, implement only the NW-079 login/session boundary first.**
   - Why now: the model is accepted, but production web admin runtime behavior
     does not exist and dev admin surfaces remain production-contained.
   - Expected artifact: one implementation row plus code/tests for OIDC/JWKS
     browser login, server session, CSRF/logout/session expiry, principal-binding
     revalidation, protected shell, and login/session audit.
   - Scope: minimal protected admin shell and session authority boundary; no
     config authoring/publish UI, assignment admin expansion, online binding
     admin, or reporting/export.
   - Stop condition: IdP groups/claims/roles, JWT `actor_id`, UI-selected actor,
     or request-body actor becomes platform/admin authority.

3. **Before any real production claim, run the production-decision route.**
   - Why now: NW-067 accepts a synthetic reference rehearsal, not customer
     production approval.
   - Expected artifact: deployment-owner decision/update covering provider,
     region, jurisdiction/data classification, real notification destination,
     production login/communication, support coverage, evidence retention, and
     a fresh rehearsal or go/no-go record.
   - Scope: operations policy/runbook/rehearsal evidence only; runtime changes
     require separate rows.
   - Stop condition: lab SSH hosts, synthetic webhook, synthetic principal, or
     old R12 evidence is treated as enough for real production.

4. **Select NW-071 before tenant-aware mobile or shared-device partition work.**
   - Why now: multi-tenancy, retained data, and shared physical devices all
     depend on precise local partition boundaries.
   - Expected artifact: indexed platform spec for single-active-actor sessions,
     drain-or-seal switching, actor-resolved refresh/resume, per-actor mutable
     partitions, actor-scoped sync bookkeeping, immutable shared config blobs,
     and sealed pending-work boundaries.
   - Scope: durable current behavior only; coordinate with NW-054 if expiry,
     decommissioning, sealed recovery, encryption, token/session retention, or
     no-local-retention is in scope.
   - Stop condition: local deletion, cross-actor recovery, tenant/workspace key
     changes, or retention promises are implemented as cleanup without a
     security/platform decision.

5. **Keep reporting, conflict automation, patterns, and entity lifecycle unpromoted until selected.**
   - Why now: the operational milestone may tempt broad product expansion.
   - Expected artifact: no change unless the owner selects NW-044, NW-045,
     NW-046, NW-072, NW-073, or NW-021 as the next bounded work item.
   - Scope: preserve current candidate/future-decision standing and use the
     existing scenario pressure as input, not authority.
   - Stop condition: UI design, rehearsal urgency, or scenario wording becomes
     authority for reporting warehouses, batch resolution, pattern traversal,
     new scopes, or lifecycle truth.
