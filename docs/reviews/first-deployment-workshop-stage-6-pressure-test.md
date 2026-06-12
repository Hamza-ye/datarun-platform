# First Deployment Workshop Stage 6 Pressure Test Packet

Status: workshop-stage-output

Date: 2026-06-12

Roles: Reality Checker and Test Results Analyzer

Authority: none. This packet classifies claims, evidence, missing evidence,
release gates, and production wording risk. It does not define product scope,
create architecture authority, approve implementation, or replace Product
Manager, Project Shepherd, Workshop Lead, or steward accountability.

## 1. Stage 6 Role Boundaries

Reality Checker classifies claims, evidence pressure, and production wording
risk. It does not act as Architecture Steward, set product priority, or approve
implementation.

Test Results Analyzer classifies evidence, gaps, test targets, walkthroughs,
ops checks, and release gates. It does not define product scope, create
architecture authority, or approve implementation.

Both roles must preserve product-needed lanes. Missing evidence is not a reason
to erase product need; it becomes planned evidence work, a release gate, or a
routed decision lane.

## 2. Claim Status Table

| Major claim | Status | Reality check |
|---|---|---|
| Core append-only capture/correction kernel exists | `accepted` | BAR-001..015 standing and S00 evidence support the kernel claim, not polished UX. |
| Offline sync, scoped pull, subject-history support exist | `accepted` | Accepted kernel behavior; user-facing recovery/freshness language remains partial. |
| Candidate 1 operational capture can be framed over current kernel | `runtime-evidenced` | Scenario probes support the path, but Candidate 1 is not yet a validated product spec or implementation packet. |
| Mobile setup/work list/capture/offline save/sync is feasible | `product-surface-partial` | Current Flutter flows exist, but raw bearer setup, skeletal states, and missing polished UX prevent product-readiness claims. |
| Mobile OIDC/Keycloak login is ready | `needs-decision` | Server auth kernel exists; mobile provider login, refresh/logout, secure storage, and token lifecycle are not decided or built. |
| Web admin/config can support design exploration | `product-surface-partial` | Existing HTML admin/config surfaces are development-only and cannot be production admin surfaces. |
| Production web admin authentication is ready | `needs-decision` | Admin auth/authority/audit model remains a successor decision. |
| Principal-binding provisioning can be operated by skilled deployers | `operator-deployable-with-constraints` | Manifest-managed binding is accepted, but depends on operator process, secrets, review, and runbooks. |
| Deployment is turnkey production-ready | `blocked` | Contradicted by NW-056 and workshop control: ops hardening, admin auth, mobile login, retention/security, reporting, and runbooks are missing. |
| Reporting/dashboard/API/export readiness | `needs-decision` | S26 proves report inputs only; NW-044 route required before production reporting surfaces. |
| Retention/security/device lifecycle readiness | `needs-decision` | BAR-106/NW-054 owns expiry, decommissioning, local encryption, redaction, and sealed recovery. |
| S06/entity lifecycle implementation inside Candidate 1 | `out-of-scope` | Optional S01-compatible subject link is allowed; lifecycle truth, discovered units, active/inactive/retired state remain BAR-105/S06 lane. |
| S06 timing relative to Candidate 1 | `needs-decision` | FD-PKT-001 must decide whether Candidate 1 stays first as S01-compatible, S06 moves before Candidate 1 implementation planning, or S06 discovery runs in parallel before the implementation gate. |
| Conflict review basic semantics exist | `accepted` | Single-flag resolver semantics are accepted; product review queues and batch/automation are not. |
| Conflict review queue/batch/auto-resolution is ready | `needs-decision` | NW-045/BAR-102/BAR-103 route required. |
| Subject/query/custom scope can be added through UI filters | `needs-decision` | NW-053/BAR-108 required before any new scope mechanism or query/custom authority. |
| Ops readiness for production wording | `blocked` | No evidenced TLS/secrets/backup/restore/rollback/monitoring/support rehearsal package yet. |

## 3. Existing Evidence Inventory

| Evidence area | Standing |
|---|---|
| Baseline kernel | BAR-001 through BAR-015 and BAR-104 are `baseline_accepted`. |
| Scenario probes | S00, S19, S21, S22, S23, S26, and S27 are runtime-evidenced through NW-025, NW-026, NW-029, NW-030, NW-032, NW-033, and NW-042. |
| Config/expression boundary | BAR-010, BAR-011, NW-034, and NW-057 cover config packages, deployer shape schema, expression fixtures, and fixed `context.*` refs. |
| Auth kernel | BAR-104, NW-037, NW-038, and NW-040 cover server OIDC/JWKS validation, explicit principal bindings, and manifest provisioning. |
| Assignment/admin command kernel | NW-050 covers `assignment_admin.create` and `assignment_admin.end` command capability with same-assignment containment. |
| Mobile substrate | BAR-008, BAR-010 through BAR-014, NW-024, and NW-055 cover selective retain, advisory warnings, projection/config behavior, and actor partitions. |
| Product standing | NW-056 classifies the platform as an accepted field-operations kernel, operator-deployable with constraints, but not a turnkey product. |
| UX/mobile workshop evidence | Stage 3 and Stage 5 identify required walkthroughs, vocabulary validation, Flutter targets, and manual mobile flows. |

## 4. QA / Evidence Matrix By Lane

| Lane | Current evidence | Missing before release-ready claim |
|---|---|---|
| Candidate 1 basic operational capture | Strong kernel evidence: append-only capture/correction, sync, assignment scope, config, flags, projections, mobile local save. Runtime probes S00/S19/S21/S23 apply. | UX validation, manual end-to-end walkthroughs, staging run, support paths, accessibility/localization if claimed, ops checks, and explicit claim wording. |
| Mobile/offline/shared-device Candidate 1 | Mobile feasibility is `product-surface-partial`; Flutter evidence exists for sync/config/projection/retention/actor partitions. | Widget/integration tests for setup, offline save, failed sync, unauthorized sync, correction, shared-device switch, pending preservation, and manual device matrix. |
| S06/entity lifecycle | BAR-105 is deferred; no accepted lifecycle product/kernel evidence. Product need remains visible. | FD-PKT-001 timing decision, then product/platform decision if promoted; contract/test plan, lifecycle walkthroughs, migration and merge/split UX evidence. |
| Production auth/admin/mobile login | Server auth kernel accepted; web admin and mobile login are not productized. | Production admin auth decision, mobile OIDC/token lifecycle decision, Keycloak ops profile, end-to-end auth tests, security/ops checks. |
| Retention/security/device lifecycle | Selective retain and actor partitions accepted. BAR-106/NW-054 remains future decision. | Expiry, decommissioning, sealed recovery, local encryption, token/session retention, redaction/no-local-retention evidence, security review. |
| Reporting/import-export | S26 proves traceable scoped report inputs only. | NW-044 decision, stable report/API/export model, freshness/drill-back UX, scope/security tests, manual reporting walkthrough. |
| Conflict review UX | Single-flag resolver semantics accepted; S21/S26 cover review behavior. | Production review queue UX, resolver-visible manual walkthroughs, admin auth gate, batch/automation decision through NW-045 if needed. |
| Subject/query/custom scope | Current fixed axes accepted; BAR-108/NW-053 future decision for new scope. | Evidence that UI/report filters do not become authority; successor decision before any query/custom scope. |
| Ops readiness | Operator-deployable-with-constraints per NW-056. | TLS/secrets, backup/restore, migration rollback, observability, incident/support runbooks, auth manifest rehearsal, config/assignment bootstrap rehearsal. |

## 5. Missing Evidence List

- Product/SME validation for Candidate 1 vocabulary, states, and
  first-deployment acceptance.
- UX walkthroughs for coordinator setup, field capture/offline/sync,
  supervisor freshness, correction, unresolved issue, access ended, and
  shared-device switch.
- Mobile widget/integration evidence for the Stage 5 target flows.
- Manual device/offline matrix for invalid token, valid setup, offline capture,
  restart, sync retry, unauthorized/actor drift, and pending-work preservation.
- Staging evidence tying server, mobile, config publish, auth manifest,
  assignment bootstrap, and sync together.
- Production admin auth evidence.
- Mobile OIDC/Keycloak login and token lifecycle evidence.
- Retention/security/decommissioning evidence under NW-054/BAR-106.
- Reporting/import-export decision and tests under NW-044.
- Ops runbook rehearsal evidence for backup/restore, rollback, monitoring,
  secrets, TLS, incident response, and support escalation.

## 6. Tests, Walkthroughs, Ops Checks, And Release Gates

Automated tests:

- Rerun targeted Maven slices for envelope, config, sync, assignment, auth,
  flags, and projection.
- Rerun targeted Flutter slices named in Stage 5.
- Add widget/integration tests for setup success/failure, offline save,
  advisory non-blocking save, sync failure preservation, unauthorized sync,
  append-only correction, work-list counts/states, and shared-device actor
  isolation.

Scenario probes:

- Candidate 1 should reuse S00/S19/S21/S23.
- Add a bounded end-to-end staging probe combining setup, assignment, offline
  capture, sync, correction, unresolved issue, and latest-synced supervisor
  view without adding new primitives.

Manual walkthroughs:

- coordinator setup;
- field offline capture;
- missing-known-thing capture;
- correction;
- failed sync retry;
- stale/access-changed save;
- supervisor freshness interpretation;
- issue review;
- shared-device A-to-B switch;
- support recovery.

Ops checks:

- auth manifest apply/rotate/deactivate rehearsal;
- assignment bootstrap;
- config publish/rollback rehearsal;
- TLS/secrets check;
- database backup/restore;
- Flyway rollback plan;
- monitoring/alerting;
- incident/support runbook;
- known-risk register.

Release gates:

- No lane is release-ready unless authority/routing, contract fit, automated
  tests, scenario/manual evidence, product/UX validation, security/ops checks,
  and claim wording all pass for that lane.

## 7. Go / No-Go Recommendation By Milestone

| Milestone | Recommendation |
|---|---|
| Stage 7 delivery planning | GO. Evidence is sufficient to plan lanes, gates, owners, and dependencies. |
| Candidate 1 product/spec and UX validation | CONDITIONAL GO. Use accepted constructs, but require vocabulary and journey evidence before implementation packets. |
| Candidate 1 implementation packets | NO-GO until scoped task packets include source files, contracts, tests, walkthroughs, excluded successor lanes, and stop/report conditions. |
| Constrained operator-managed pilot | NO-GO until staging, mobile manual matrix, ops runbooks, auth manifest rehearsal, config/assignment bootstrap, and support paths are evidenced. |
| Turnkey production product | NO-GO. Admin auth, mobile OIDC login, retention/security, reporting/import-export, ops hardening, and product surfaces remain incomplete. |
| Successor lanes | NO-GO for implementation until their product/platform/security decisions and evidence plans exist. |

## 8. Product Need Preservation Notes

These product pressure lanes must remain visible:

- S06/entity lifecycle;
- production auth/admin/mobile login;
- retention/security/device lifecycle;
- reporting/import-export;
- conflict review queues;
- subject/query/custom scope;
- ops readiness.

Their missing evidence should become explicit decisions, tests, walkthroughs,
and release gates, not quiet exclusions from the plan.

Candidate 1 can move forward only by naming these as excluded or gated
surfaces. That preserves the product need while preventing unsupported
production claims.

## 9. Stage 7 Planning Instructions

Stage 7 should focus on evidence gates per lane, not optimistic sequencing.
Every milestone must state:

- claim status;
- required evidence class;
- owner;
- decision dependency;
- stop condition.

For subject-linked or missing-known-thing capture, Stage 7 and FD-PKT-001 must
either:

- keep Candidate 1 first as an S01-compatible slice with UX copy and tests that
  prevent lifecycle overclaim;
- schedule an earlier BAR-105/S06 product/platform decision if first deployment
  requires maintained known things, lifecycle states, or discovered-unit
  stewardship; or
- run S06 discovery in parallel and block implementation packet dispatch until
  the dependency is resolved.
