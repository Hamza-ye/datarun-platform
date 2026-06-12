# First Deployment Workshop Stage 7 Delivery Plan

Status: workshop-stage-output

Date: 2026-06-12

Role: Project Shepherd

Authority: none. This packet owns delivery sequencing, dependency visibility,
owner mapping, change control, and task-packet structure for Stage 7. It does
not authorize implementation, create architecture authority, decide product
scope, or collapse routed product needs into vague follow-up work.

## 1. Project Shepherd Role Boundary

Candidate 1 remains `CONDITIONAL GO` for product/spec and UX validation only.
Candidate 1 implementation remains `NO-GO` until Stage 8 produces scoped
packets with files, contracts, tests, walkthroughs, excluded lanes, gates, and
stop/report conditions.

## 2. Milestone Roadmap

| Milestone | Purpose | Exit gate |
|---|---|---|
| M0 Workshop delivery baseline | Confirm lane register, owner map, claim labels, and change-control rules. | Workshop Lead accepts Stage 7 packet. |
| M1 Candidate 1 product/spec and UX validation | Define bounded operational capture over existing kernel: setup, assigned work, capture, local save, sync, correction, optional subject link, freshness, unresolved issue visibility. | Product/UX validation artifacts, FD-PKT-001 S06 timing decision, explicit exclusions or dependency marker. |
| M2 Evidence and packet-gate design | Test Results Analyzer and steward accountability define required automated tests, scenario probes, manual walkthroughs, ops checks, source files, contracts, and stop conditions. | No implementation packet is missing evidence class, owner, route, or exclusion list. |
| M3 Stage 8 task-packet approval | Workshop Lead packages narrow agent tasks. | Each packet is one lane only and has authority/routing, files, expected tests, manual evidence, stop conditions, and commit boundary. |
| M4 Candidate 1 implementation slices | Execute bounded UI/view-model/mobile/server tasks separately, only after M3 approval. | Targeted tests and walkthrough evidence pass for each slice. |
| M5 Integrated staging rehearsal | Setup/config publish, assignment bootstrap, auth manifest operation, mobile offline/sync/correction, unresolved issue visibility, support paths. | Staging evidence and known-risk register. |
| M6 First constrained operator-managed deployment go/no-go | Decide constrained deployment readiness, not turnkey product readiness. | Staging pass, mobile manual matrix, ops runbooks, auth manifest rehearsal, config/assignment bootstrap, backup/restore/rollback/monitoring/support evidence. |

Visible successor lanes stay separate:

- S06/entity lifecycle;
- production auth/admin/mobile login;
- retention/security/device lifecycle;
- reporting/import-export;
- conflict review queues;
- subject/query/custom scope;
- ops readiness.

## 3. Dependency Map

| Lane | Dependency |
|---|---|
| Candidate 1 | Existing accepted kernel, UX/product validation, FD-PKT-001 S06 timing decision, and Stage 8 packet gates. It must not depend on production auth, reporting, retention/security, or conflict automation. |
| S06/entity lifecycle | Product Manager priority plus BAR-105/S06 product/platform decision. FD-PKT-001 must decide whether Candidate 1 stays first as S01-compatible, S06 moves before Candidate 1 implementation planning, or S06 discovery runs in parallel before the implementation gate. Stop if maintained known things, lifecycle states, or discovered-unit stewardship are required before Candidate 1 freeze. |
| Production auth/admin/mobile login | Production admin auth decision, mobile OIDC/token lifecycle decision, Keycloak/ops profile, and security evidence. |
| Retention/security/device lifecycle | NW-054/BAR-106 decision before expiry, decommissioning, sealed recovery, local encryption, token retention, or no-local-retention claims. |
| Reporting/import-export | NW-044 before dashboard/API/export/import, aggregation, broad audit/history, or report contract work. |
| Conflict review queues | Current single-flag semantics for basics; resolver-steward eligibility, batch, automation, resolver reassignment, and auto-resolution require explicit route before productization. |
| Subject/query/custom scope | NW-053/BAR-108 before UI filters, reporting filters, auditor views, or custom query scope become authority. |
| Ops readiness | Runbooks and rehearsal evidence before production wording or constrained deployment go/no-go. |

## 4. Owner / Role Matrix

| Role | Owns |
|---|---|
| Workshop Lead | Stage output acceptance, lane register, unresolved-input register, and Stage 8 dispatch. |
| Product Manager | Product acceptance, Candidate 1 promise/non-goals, S06 timing need, and validation questions. |
| UX Architect or UX facilitation owner | Vocabulary, journey walkthroughs, state language, and UX evidence artifacts. |
| Steward accountability | Source order, route checks, authority questions, and stop-condition validation. |
| Software Architect | Technical boundary mapping and implementation-surface feasibility. |
| Mobile App Builder | Mobile feasibility, Flutter evidence targets, and mobile caveats. |
| Reality Checker / Test Results Analyzer | Claim labels, evidence matrix, release gates, and overclaim pressure. |
| Project Shepherd | Milestone sequencing, dependencies, decision calendar, and change-control discipline. |
| Implementation agents | Receive only Stage 8 bounded packets; they do not infer authority from this plan. |

## 5. Decision Calendar

| Timing | Decision | Owner |
|---|---|---|
| 2026-06-12 | Stage 7 roadmap and change-control packet completed. | Project Shepherd |
| 2026-06-13 | FD-PKT-001 recorded Option C: Candidate 1 product/spec and UX validation may proceed with S06 discovery in parallel before implementation dispatch. | Product Manager with steward accountability |
| Before M2 exit | Candidate 1 evidence plan approved. | Test Results Analyzer with Workshop Lead |
| Before M3 dispatch | Stage 8 packet-gate approval. | Workshop Lead with steward/source-order check |
| Before M4 starts | Each implementation packet receives explicit go/no-go. | Workshop Lead; steward answers authority questions only |
| Before M5 | Staging environment, manual mobile matrix, and ops rehearsal scope confirmed. | Workshop Lead / Project Shepherd |
| Before M6 | Constrained deployment go/no-go. | Workshop Lead with Product Manager, ops/security, QA, and steward accountability consulted |

Successor decision checkpoints stay separate: auth/admin/mobile login,
retention/security, reporting/import-export, conflict queues,
subject/query/custom scope, and ops readiness.

## 6. Change-Control Rules

- Every change request must name lane, owner, product need, claim status,
  decision route, evidence need, dependencies, excluded lanes, tests,
  walkthroughs, stop condition, and approval point.
- No change may combine unrelated successor lanes into one implementation
  slice.
- No product-needed gap may be renamed as vague follow-up; it stays a lane with
  owner, decision point, evidence need, dependency, and stop condition.
- Product wording cannot become architecture, contract, event type, scope
  mechanism, durable state, or authority without routed approval.
- Persona labels cannot become product modules, config namespaces,
  implementation boundaries, or hard role categories. Describe the current
  authority context that projects each surface.
- Production-readiness wording requires matching ops/security/release evidence.
- Any S06 pressure inside Candidate 1 triggers FD-PKT-001 before
  implementation planning continues. The decision must keep S06 visible with
  owner, evidence need, milestone, and route; it must not bury S06 under vague
  follow-up wording.

## 7. Agent Task-Packet Backlog Structure

Each Stage 8 packet should contain:

- packet ID;
- lane;
- objective;
- claim status;
- owner;
- source-order references;
- exact files/contracts allowed;
- accepted constructs reused;
- dependencies;
- excluded successor lanes;
- forbidden work;
- expected tests;
- manual walkthroughs;
- evidence class;
- stop/report conditions;
- commit boundary;
- done definition.

Backlog groups stay lane-specific:

- Candidate 1 product/spec and UX validation;
- Candidate 1 mobile/offline evidence;
- Candidate 1 view-model/UI slices;
- staging rehearsal;
- ops readiness;
- S06/entity lifecycle decision;
- production auth/admin/mobile login;
- retention/security;
- reporting/import-export;
- conflict review queues;
- subject/query/custom scope.

## 8. Risks To Timeline Visibility

- S06 timing is the highest sequencing risk because optional subject link can
  quietly become lifecycle scope.
- Production auth/admin/mobile login can block production wording even if
  Candidate 1 UX works.
- Ops readiness can become the critical path if runbooks, staging,
  backup/restore, rollback, monitoring, and support paths are not scheduled
  early.
- Evidence work may be underestimated because manual
  mobile/offline/shared-device walkthroughs are not substitutes for automated
  regression slices.
- Reporting, conflict queues, resolver-steward eligibility, and custom scope can
  leak into supervisor visibility unless explicitly separated.
- Coordinator/setup owner, field user, supervisor/reviewer, operator/admin,
  support, and auditor labels can harden into fixed product areas unless each
  packet treats them as acting contexts backed by current authority.
- The UX facilitation draft may need dedicated UX review before implementation
  packets if the Workshop Lead requires stronger product evidence.

## 9. Advice To Workshop Lead

- Use the recorded FD-PKT-001 Option C boundary before Candidate 1 packet
  freeze.
- Reject any Stage 8 packet that mixes Candidate 1 with auth, reporting,
  retention/security, S06 lifecycle, conflict automation, or custom scope.
- Keep the lane register visible in every milestone review, including lanes
  that are product-needed but not implementation-ready.
- Require claim labels in all packet titles or headers.
- Treat constrained deployment as an evidence milestone, not a synonym for
  turnkey production.
