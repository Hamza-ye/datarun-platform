# First Deployment Workshop Stage 8 Task-Packet Backlog

Status: workshop-stage-output

Date: 2026-06-12

Role: Workshop Lead / Delivery Evidence Facilitator

Authority: none. This backlog converts workshop outputs into bounded future
task-packet candidates. It does not authorize implementation, change CDL,
contracts, BAR, NW, status, schemas, APIs, or code.

## 1. Stage 8 Boundary

Stage 8 owns task-packet structure and dispatch readiness. It does not make
architecture decisions, product decisions, or implementation approvals.

No implementation agent should receive this whole workshop record as an
instruction to build. Implementation agents should receive one bounded packet
only after its gate is satisfied.

## 2. Packet Gate

No packet can be dispatched unless it has:

- packet ID;
- lane;
- objective;
- claim status;
- owner role;
- source-order references;
- exact files/contracts allowed;
- accepted constructs reused;
- operational/persona labels used, if any, with authority backing;
- dependencies;
- excluded successor lanes;
- forbidden work;
- expected tests or validation evidence;
- manual walkthroughs where applicable;
- evidence class;
- stop/report conditions;
- commit boundary when code/docs edits are allowed;
- done definition.

## 3. Immediate Stage 8 Backlog

These are the first packets to prepare. They are planning/spec/evidence packets,
not implementation packets.

| Packet ID | Lane | Claim status | Owner role | Purpose | Gate |
|---|---|---|---|---|---|
| FD-PKT-001 | S06 timing decision | `needs-decision` | Product Manager + steward accountability | Decide whether Candidate 1 excludes lifecycle with UX copy/tests or moves BAR-105/S06 decision before Candidate 1 implementation planning. | Must complete before Candidate 1 packet freeze. |
| FD-PKT-002 | Candidate 1 product/spec and UX validation | `conditional-go` for spec/validation | Product Manager + UX owner | Produce bounded Candidate 1 product/spec, validation questions, vocabulary tests, journey walkthrough requirements, and explicit exclusions. | S06 timing decision recorded. |
| FD-PKT-003 | Candidate 1 evidence plan | `conditional-go` for evidence design | Test Results Analyzer | Convert Candidate 1 acceptance criteria into automated tests, scenario probes, manual walkthroughs, ops checks, and release gates. | FD-PKT-002 draft available. |
| FD-PKT-004 | Candidate 1 mobile/offline validation packet | `product-surface-partial` | Mobile App Builder | Define mobile UX/spec evidence for setup/connect, offline save, sync failure, shared-device switch, correction, and freshness. | FD-PKT-002 and FD-PKT-003 available. |
| FD-PKT-005 | Candidate 1 view-model/contract assessment | `needs-routing-check` | Software Architect + steward accountability | Decide whether Candidate 1 needs only adapter-level UI composition or a bounded shared view-model contract route before implementation. | Stage 4 question resolved before UI/server implementation packets. |
| FD-PKT-006 | Ops readiness runbook plan | `blocked` for production claims | Project Shepherd + Test Results Analyzer | Define TLS/secrets, backup/restore, migration rollback, monitoring, incident/support, auth manifest, config publish, and assignment bootstrap rehearsal plan. | Required before constrained deployment go/no-go. |
| FD-PKT-007 | Integrated staging rehearsal plan | `blocked` for constrained deployment | Project Shepherd + QA owner | Define staging scenario tying server, mobile, config publish, auth manifest, assignment bootstrap, offline/sync/correction, unresolved issue visibility, and support paths. | Required before M5 staging rehearsal. |

## 4. Successor-Lane Backlog

These lanes must stay visible. They are not implementation-ready until their
decision/evidence packets are complete.

| Packet ID | Lane | Claim status | Owner role | Purpose | Stop condition |
|---|---|---|---|---|---|
| FD-PKT-101 | S06/entity lifecycle | `needs-decision` | Product Manager + steward accountability | Product/platform decision for maintained known things, discovered-unit lifecycle, active/inactive/retired state, merge/split UX, and lifecycle evidence. | Stop if lifecycle truth is added through Candidate 1 subject-link copy or UI state. |
| FD-PKT-102 | Production web admin auth and admin authority | `needs-decision` | Software Architect + security/ops owner | Decide production admin authentication, admin authority/audit, and dev-console replacement/containment. | Stop if dev admin actor, request-body actor, or IdP claims/groups become authority. |
| FD-PKT-103 | Mobile OIDC/Keycloak login and token lifecycle | `needs-decision` | Mobile App Builder + Software Architect | Decide provider login, refresh/logout, token expiry, secure storage, shared-device switch UX, and offline re-auth behavior. | Stop if mobile UI, JWT `actor_id`, provider claims, groups, or roles become actor/scope authority. |
| FD-PKT-104 | Retention/security/device lifecycle | `needs-decision` | Security/platform owner | Route NW-054/BAR-106 for expiry, decommissioning, sealed recovery, local encryption, token/session retention, redaction/no-local-retention. | Stop if selective retain is described as security deletion, expiry, or decommissioning. |
| FD-PKT-105 | Reporting/import-export | `needs-decision` | Product Manager + Software Architect | Route NW-044 for reporting dashboard/API/export/import, freshness, drill-back, aggregate access, and scoped report model. | Stop if S26 inputs are treated as production reporting surface. |
| FD-PKT-106 | Conflict review queues and automation | `needs-decision` | Product Manager + Software Architect | Route humane review queues, resolver-steward eligibility, batch handling, pending-match derivations, resolver reassignment, and auto-resolution. | Stop if UI bypasses exact designated-resolver semantics, mutates flags directly, or treats role-name substrings as product-ready resolver authority. |
| FD-PKT-107 | Subject/query/custom scope | `needs-decision` | Steward accountability + Software Architect | Route NW-053/BAR-108 for auditor/reporting/admin filters or custom/query scope pressure. | Stop if UI filters, report filters, or admin convenience become access authority. |
| FD-PKT-108 | Ops readiness and constrained deployment | `blocked` for production claims | Project Shepherd + ops/QA owner | Build runbooks and rehearsal evidence for constrained operator-managed deployment. | Stop if dev compose/admin screens are treated as production hardening. |

## 5. Candidate 1 Implementation Packet Groups

These groups are not ready to dispatch yet. They become candidates only after
FD-PKT-001 through FD-PKT-005 complete.

| Group | Future packet examples | Required before dispatch |
|---|---|---|
| Product/spec docs | Candidate 1 platform spec, UX copy glossary, validation checklist. | S06 timing decision, Product Manager approval, UX validation plan. |
| Mobile UI/evidence | Setup/connect polish, work-list states, offline save/sync states, shared-device switch warning, append-only correction UX. | Mobile evidence plan, view-model decision, Flutter/manual test expectations. |
| Server/view-model composition | Candidate 1 read model or endpoint assessment if needed. | Software Architect view-model/contract assessment; no new contract without route. |
| Web admin/config UX design | Setup/config/assignment/review product design over current constructs. | Production admin auth decision before productionization; dev-console caveat preserved. |
| Staging/evidence | End-to-end staging probe, manual walkthrough runbook, known-risk register. | Ops runbook scope and test matrix. |

## 6. Required Packet Header

Use this header for every future task packet:

```txt
Packet ID:
Lane:
Assigned role:
Claim status:
Objective:

Authority and source order:
- CDL/contracts first where applicable.
- BAR/NW for accepted standing.
- Scenario/user-fit/workshop docs are evidence and routing context only.

Operational/persona labels used:
- If using coordinator/setup owner, field user, supervisor/reviewer,
  operator/admin, support role, auditor, or similar labels, state that they are
  acting contexts only.
- Back each label with actor + active assignment + role + scope + time +
  activity/context -> available actions and visible data -> projected
  operational surface.

Allowed files/contracts:

Accepted constructs reused:

Excluded successor lanes:

Forbidden work:

Expected evidence:

Manual walkthroughs:

Stop and report if:
- Persona labels are treated as identity categories, authority primitives,
  fixed UI modules, config namespaces, product-area boundaries, or
  implementation service boundaries.

Done definition:

Commit boundary:
```

## 7. Stage 8 Completion Result

The workshop is complete for planning control when this backlog is accepted by
the Workshop Lead.

Implementation remains blocked until individual packets are written, reviewed
against this backlog, and explicitly dispatched.

The immediate next non-implementation action is FD-PKT-001: S06 timing decision
for Candidate 1 freeze.
