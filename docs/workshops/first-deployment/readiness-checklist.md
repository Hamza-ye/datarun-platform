# Pre-Workshop Readiness Checklist

Status: review/checklist

Date: 2026-06-12

Authority: none. This file is a human workshop-management checklist. It does
not change CDL, contracts, BAR, NW, status, schemas, APIs, or implementation
authority. Agents should use it only when a task packet explicitly routes them
to it.

## Purpose

Use this checklist before the first-deployment planning workshop. The goal is
to make sure the working surface, product evidence, role inputs, and evidence
gates are ready before the room starts making plans.

Do not start the workshop as a general brainstorming session. Start only when
the current standing point, product-fit pressure, authority stack, QA evidence,
and unresolved lanes are visible enough for the roles to make grounded
decisions.

## Role Placement Map

| Workshop point | Role | Why this role enters here | Required output |
|---|---|---|---|
| Pre-workshop setup | Workshop Lead | Coordinates inputs, agenda, role prompts, decision rules, and output format. | Filled readiness checklist, agenda, initial lane register, open-input list. |
| Pre-workshop setup and opening | Architecture Steward | Establishes source order, accepted baseline, routed lanes, and forbidden work before product or implementation claims expand. | Standing brief, authority map, stop conditions. |
| Opening product frame | Product Manager | Defines who first deployment serves, what product outcome matters, and how tradeoffs should be judged. | First-deployment outcome, product lanes, non-goals, validation questions. |
| Product-to-experience bridge | UX Architect | Converts product outcome into user journeys, vocabulary, state language, error states, and validation needs without making UX terms architecture. | Journey map, vocabulary list, interaction risks, UX evidence gaps. |
| Experience-to-system bridge | Software Architect | Maps product and UX lanes onto current architecture, contracts, code boundaries, and successor decision routes. | Technical dependency map, route map, implementation constraints, risk list. |
| Mobile feasibility bridge | Mobile App Builder | Tests the mobile/offline/shared-device/auth implications after UX and architecture boundaries are known. | Mobile feasibility notes, Flutter/test targets, offline/sync risk list, implementation caveats. |
| Claim pressure test | Reality Checker | Challenges overstatement and marks claims as accepted, evidenced, partial, routed, unknown, or blocked. | Claim-status table, risk register, blocked/needs-decision list. |
| Evidence planning | Test Results Analyzer | Maps each claim and milestone to tests, probes, manual validation, release gates, and missing evidence. | QA matrix, test inventory, missing-evidence list, release gate. |
| Delivery planning | Project Shepherd | Converts the validated lanes into milestones, dependencies, owners, dates, and change-control checkpoints. | Milestone roadmap, dependency map, decision calendar, task-packet backlog. |

## Readiness Checklist

### 1. Surface State

- [ ] `git status --short` captured and understood.
- [ ] No untracked scratch files need cleanup before the workshop.
- [ ] Staged and unstaged docs changes are known; no one is planning against a
      stale surface.
- [ ] `AGENTS.md` and `docs/status.md` Current Routing are available as the
      starting source order.
- [ ] Decision anchor layer README, architecture anchors, gap-routing playbook,
      BAR, platform next-work backlog, and NW-056 are linked in the workshop
      packet.
- [ ] Scenario user-fit README, synthesis, readiness matrix, and relevant
      scenario packets are linked as product/problem evidence.
- [ ] `docs/reviews/scenario-user-fit-packets-standing-review-and-playbook.md`
      is linked for human workshop management.
- [ ] `docs/workshops/first-deployment/task-packets/fd-pkt-001-s06-timing-decision.md`
      is linked before Candidate 1 packet freeze.

### 2. Authority And Routing

- [ ] The workshop packet states that CDL/contracts/BAR/NW are authority and
      scenario packets are product/problem evidence.
- [ ] Candidate 1 is framed as a constrained first-deployment slice, not a
      container for every product need.
- [ ] S06/entity lifecycle is visible as a near-future product-deployment lane,
      not hidden as vague later work.
- [ ] FD-PKT-001 is scheduled to decide whether Candidate 1 stays first as an
      S01-compatible slice, S06/BAR-105 moves earlier, or S06 discovery runs in
      parallel before implementation gates.
- [ ] Production auth/admin/mobile login, retention/security, reporting/import
      and export, conflict review UX, and ops readiness are all visible routed
      lanes.
- [ ] Forbidden work is listed before implementation planning begins.
- [ ] The route for promoting a lane into implementation is explicit: owner,
      decision route, evidence needed, acceptance gate, and stop conditions.

### 3. Product Management Readiness

- [ ] Product Manager has a draft first-deployment outcome statement.
- [ ] Target deployment users and jobs are named: coordinator/setup owner,
      field user, supervisor/reviewer, operator/admin, and support role where
      applicable.
- [ ] Product non-goals are stated, especially "not turnkey production product"
      unless production evidence exists.
- [ ] Product-fit assumptions are listed separately from accepted platform
      standing.
- [ ] SME/user validation questions are ready for vocabulary, setup flow,
      offline confidence, subject lookup, correction, access ended, and support
      workflows.

### 4. UX Architecture Readiness

- [ ] UX Architect has the candidate first-deployment journeys to review:
      setup, assignment, capture, offline save, sync, subject lookup,
      correction, review, access ended, and unresolved issue visibility.
- [ ] Product vocabulary is drafted in user terms and explicitly marked as not
      architecture authority.
- [ ] State language is ready for review: saved locally, waiting to sync,
      synced, needs review, stale/old version, access ended, and unresolved
      issue.
- [ ] UX risks are listed for offline ambiguity, stale authority, shared-device
      actor switching, missing subject, correction meaning, and review queues.
- [ ] Required UX evidence is named: walkthroughs, screenshots or prototypes,
      role-specific flows, error states, and accessibility/localization checks
      where claimed.

### 5. Software Architecture Readiness

- [ ] Software Architect has the active standing brief and knows which
      decisions are accepted, runtime-evidenced, or routed.
- [ ] Candidate 1 technical boundaries are mapped to contracts, server, mobile,
      config, sync, identity, authorization, and integrity surfaces.
- [ ] Successor lanes have route references before any implementation claim is
      made: BAR-105/S06, NW-054/BAR-106, NW-044, production auth/admin/mobile
      login, and conflict-review decisions.
- [ ] Contract drift risks are listed, especially envelope fields/types, shape
      schemas, config package shape, sync protocol, and flag catalog behavior.
- [ ] Stop conditions are ready for product vocabulary becoming architecture,
      query/custom scope, deployer scripts, triggers, new state machines,
      IdP claim authority, direct flag mutation, resolver reassignment, and
      auto-resolution.

### 6. Mobile App Builder Readiness

- [ ] Mobile App Builder has the mobile-relevant journeys and current mobile
      standing: offline save/sync, local projections, assignment-scoped access,
      shared-device actor partitions, freshness, and unresolved issue display.
- [ ] Mobile OIDC/login is treated as a productization lane, not assumed done
      by server OIDC/JWKS acceptance.
- [ ] Offline and sync failure states have product language and test targets.
- [ ] Shared-device UX questions are ready: actor switch, stale data,
      partition visibility, access ended, and device handoff.
- [ ] Mobile implementation risk list distinguishes UI polish, data behavior,
      local retention/security, and authority boundaries.
- [ ] Flutter test targets and manual mobile walkthroughs are identified before
      mobile work is scheduled.

### 7. QA And Evidence Readiness

- [ ] Existing runtime scenario probes are listed separately from product UX
      validation.
- [ ] Test Results Analyzer has a test inventory for server, mobile, contracts,
      and manual release checks.
- [ ] Evidence classes are agreed: authority, contract, code inspection,
      automated test, scenario probe, product/SME validation, ops evidence,
      and release evidence.
- [ ] Each major lane has a missing-evidence item or a known accepted evidence
      reference.
- [ ] Release-readiness gates are ready: authority, contract, test, scenario,
      product, security/ops, and claim labeling.

### 8. Delivery Planning Readiness

- [ ] Project Shepherd has an initial lane register with owner role, route,
      dependency, trigger/input, evidence needed, and decision point.
- [ ] Milestone labels are ready: M0 standing alignment, M1 Candidate 1 spec,
      M2 product/SME validation, M3 implementation task packets, M4 operator
      deployment readiness, M5 product UX over accepted kernel, M6 successor
      decision lanes.
- [ ] Change-control rules are ready for scope additions, authority drift,
      evidence failures, and production-claim changes.
- [ ] The workshop has a decision log format and an unresolved-input register.
- [ ] Follow-up artifacts have destinations: human record under `docs/reviews/`
      and only routed implementation packets for agent-facing work.

## Go / No-Go Gate

Start the workshop only when these are true:

- [ ] Current standing packet is linked and understood.
- [ ] Role outputs are defined before discussion begins.
- [ ] Product outcome, authority map, lane register, and stop conditions are
      drafted.
- [ ] S06/entity lifecycle and other product-needed gaps are visible in the
      planning lanes.
- [ ] Candidate 1/S06 timing is treated as a product decision checked by
      steward guardrails, not as protocol closure.
- [ ] Evidence taxonomy and release gates are ready.
- [ ] The group agrees that product terms can guide UX and validation but do
      not become architecture without routed authority.

If any item is missing, start with a short pre-work session to fill that input
instead of pretending the planning workshop is ready.
