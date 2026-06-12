# Scenario User-Fit Packets Standing Review And Playbook

Status: review/playbook

Date: 2026-06-12

Authority: none. This file is a human operator guide for using
`docs/scenarios/scenario-user-fit-packets/` with the active working surface.
It is not part of default agent routing unless a task packet explicitly cites
it. It does not change CDL, BAR, contracts, backlog status, schemas, APIs, or
implementation authority.

## Review Result

The packet set is useful as product/problem evidence. It correctly separates
many user-fit questions from architecture fit, and it gives a good first frame
for Candidate 1, "Basic Operational Capture Setup Spec."

The main issue was not bad reasoning; it was missing current standing. The
source packet did not have the full BAR/NW overlay, so several surfaces could
have been overread:

| Surface | Correction |
|---|---|
| S06 / registry lifecycle | Treat as a near-future product-deployment lane, deferred only from the current accepted baseline while surrounding slices stabilize. Subject-linked capture and bounded history are usable, but full entity lifecycle, discovered-unit lifecycle, canonical active/inactive state, and merge/split UX need a routed BAR-105/S06 successor before implementation. FD-PKT-001 must decide timing; it must not use protocol wording to hide the product need. |
| Missing-subject path | Candidate 1 may keep work moving through an unpromoted candidate/capture artifact. It must not silently create canonical registry lifecycle state. |
| Reporting and aggregates | S26 proves current inputs, not production reporting APIs, dashboards, warehouses, exports, or aggregate access divergence. Use NW-044 before productizing those. |
| Access and audit | Ordinary scoped auditor visibility can use assignments. Broad audit/history reads, query/custom scope, special writes, and emergency bypasses need successor routing. |
| Assignment administration | IDR-029/NW-050 command capabilities are outside `activities[*].roles`; do not promote assignment-admin commands into activity role-actions. |
| Production auth | Explicit principal binding is accepted. Mobile OIDC login UX, production web admin auth, online binding-admin APIs/UI, and IdP group/claim authority are not. |
| Shared devices and retention | Actor partitions are accepted. Expiry, decommissioning, sealed-partition recovery, local encryption, and no-local-retention/redacted views route through NW-054/BAR-106. |
| Shape/config context | NW-057 fixes the `context.*` property boundary; unknown refs are deploy-time invalid. Null-safe runtime evaluation is not permission for new context vocabulary. |
| Escape hatches | Escape hatches are measured triggers only. They do not authorize implementation. |

## Human Use Of The Packets

Use the packet set in this order:

1. Read `docs/scenarios/scenario-user-fit-packets/README.md`.
2. Read the synthesis and readiness matrix:
   - `scenario-user-fit-synthesis-across-s00-s01-s06-s06b-access-control-S19.md`
   - `foundational-product-fit-readiness-and-validation-matrix.md`
3. Read only the scenario packet for the surface being worked.
4. Check active standing before drafting implementation prompts:
   - `docs/status.md` Current Routing
   - `docs/agent-working-surface/decision-anchor-layer/README.md`
   - `docs/agent-working-surface/baseline-acceptance-register.md`
   - `docs/agent-working-surface/platform-next-work-backlog.md`
   - `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`
   - `docs/agent-working-surface/operational-ux-layering-companion.md`
5. Use `scripts/query_cdl.py` only for exact CDL rows needed by the surface.

Do not assume working agents will see this review file. They normally enter
through `AGENTS.md`, `docs/status.md`, and the active working surface. For
implementation, copy the relevant constraints into a narrow task packet or
explicitly cite this file.

## Human Handoff Playbook

Use this prompt frame when handing work to a coding agent:

```txt
Goal:
Draft or implement only [one bounded surface].

Files to read:
- AGENTS.md
- docs/status.md Current Routing
- docs/scenarios/scenario-user-fit-packets/README.md
- docs/scenarios/scenario-user-fit-packets/foundational-product-fit-readiness-and-validation-matrix.md
- docs/scenarios/scenario-user-fit-packets/[specific packet].md
- docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md
- exact contracts/code paths named by the task

Authority:
- CDL first, sliced with scripts/query_cdl.py.
- Contracts for process-boundary shapes/protocols.
- BAR/NW for accepted implementation standing.
- Scenario packets are product/problem evidence only.

Allowed work:
- [state exact docs/spec/code files]
- Preserve append-only event truth, assignment-derived access, sync/access
  equivalence, accept-and-flag, projection-derived state, and bounded config.

Forbidden work:
- No new envelope fields or event types.
- No deployer-authored access logic, triggers, scripts, state machines, or
  expression functions.
- No field-level sensitivity/redaction/no-local-retention behavior without
  NW-054/BAR-106.
- No entity lifecycle/discovered-unit lifecycle/merge-split UX without an
  S06/BAR-105 successor route.
- No production reporting API/export/import/dashboard expansion without NW-044.
- No broad audit/history reads, query/custom scope, or special write bypass
  without successor routing.
- No IdP group/claim/JWT actor authority.
- No auto-resolution, resolver reassignment, or mobile authoritative rejection.

Expected tests:
- Docs-only: rg checks plus git diff --check.
- Code/contracts: targeted tests for the touched boundary before broader suites.

Stop and report if:
- Product vocabulary becomes an architecture primitive.
- The task needs a routed decision/planning surface that is outside the current slice.
- Current BAR/NW standing conflicts with the requested change.
- A candidate subject becomes canonical lifecycle state.
- A report/summary would expose data outside detail/event access.
```

## Recommended Way Forward

1. Use the recorded FD-PKT-001 Option C timing boundary:
   Candidate 1 product/spec may proceed as an S01-compatible slice with an
   explicit S06 dependency marker, while S06 discovery runs in parallel before
   Candidate 1 implementation dispatch.
2. Produce Candidate 1 as a constrained platform-spec draft, not implementation:
   basic operational capture, optional subject-linked capture, assignment-scoped
   access, offline save/sync, shape version preservation, basic correction,
   freshness, and unresolved issue visibility.
3. Run an SME/product validation pass on the Candidate 1 vocabulary:
   record/form/checklist, assigned work, subject/known thing, saved locally,
   waiting to sync, synced, needs review, old version, access ended.
4. Run the deployment-planning workshop with all near-future lanes visible:
   Candidate 1, S06/BAR-105 entity lifecycle, production admin/mobile auth,
   NW-054 retention/security, NW-044 reporting/import/export, and
   conflict-review UX. Candidate 1 can remain the first implementation slice
   without hiding the rest of the roadmap.
5. Keep each successor as one bounded prompt with a stop list. Do not combine
   entity lifecycle, reporting, retention, and production auth in the same
   implementation slice.
6. Before any coding task, require the agent to state the active BAR/NW evidence
   it is relying on and the routed planning lanes it is excluding from that
   slice.

## Planning Workshop Notes

The product-deployment plan should be built like a professional delivery plan,
not as an endless stabilization queue. Keep the big picture visible:

| Planning track | Output |
|---|---|
| Product deployment milestones | Ordered milestones from current standing to first deployment, including Candidate 1 and near-future S06/BAR-105 entity lifecycle. |
| Work breakdown | Bounded tasks with owners, dependencies, forbidden work, and explicit stop conditions. |
| QA strategy | Required targeted tests, scenario probes, contract checks, manual validation, and regression gates per milestone. |
| Acceptance evidence | BAR/NW evidence, SME/product validation, runtime probes, operational runbooks, and release checklists. |
| Risk management | Risk register for auth/admin, retention/security, reporting/export, entity lifecycle, offline recovery, data sensitivity, and support operations. |
| Change management | Rules for promoting a routed surface into implementation, changing scope, handling drift, and updating working-surface status. |
| Timeline visibility | Milestone estimates, sequencing rationale, parallelization options, and explicit decisions about what is inside the first deployment versus follow-up releases. |

Entity lifecycle should appear in that plan as a named near-future milestone.
Its current BAR-105 status means "needs routed decision and evidence before
implementation," not "outside the early deployment product."

## Multi-Role Workshop Loop

Do not run the planning workshop as a steward-only architecture review. The
steward owns authority and routing discipline, but the product-deployment plan
needs multiple roles to see the same standing point from different angles.

| Role | Primary responsibility | Must produce |
|---|---|---|
| Architecture steward | Preserve CDL/BAR/contracts authority, route gaps, prevent product terms from becoming architecture. | Source-order briefing, forbidden-work list, routed-lane map, stop conditions. |
| Senior Product Manager / Product Manager | Own first-deployment product outcome, user value, scope tradeoffs, validation needs, and product-fit priority. | Product outcome statement, deployment personas/jobs, product lanes, validation questions, acceptance-by-user criteria. |
| UX Architect | Bridge product goals into user journeys, vocabulary, state language, error states, and validation artifacts without turning product terms into platform authority. | Journey map, vocabulary list, interaction risks, UX evidence gaps. |
| Software Architect | Bridge UX/product lanes into accepted architecture, contracts, code boundaries, successor routes, and implementation constraints. | Technical dependency map, route map, contract/code impact list, architecture risk list. |
| Mobile App Builder | Validate mobile/offline/shared-device/auth implications after UX and architecture boundaries are clear. | Mobile feasibility notes, Flutter/test targets, offline/sync risk list, implementation caveats. |
| Project Shepherd | Turn the product and routing map into milestones, dependencies, owners, dates, and change-control checkpoints. | Milestone plan, work-breakdown structure, dependency map, decision calendar, communication cadence. |
| Reality Checker | Challenge readiness claims and prevent optimistic production wording without evidence. | Risk register, claim-status table, evidence gaps, blocked/needs-decision list, production-claim stop conditions. |
| Test Results Analyzer | Turn acceptance into evidence, test gates, scenario probes, manual validation, and release readiness checks. | QA matrix, test inventory, missing evidence list, per-milestone acceptance checklist, release gate. |

### Role-Agent Synthesis

Start the workshop from this shared premise: first deployment should prove an
operator-managed field-operations product on top of the accepted platform
kernel. It can be deployable with constraints; it should not be described as a
turnkey production product until admin auth, mobile login, retention/security,
reporting, observability, backup/restore, support, and release evidence are in
place.

The day-one planning lanes are:

| Lane | Planning stance |
|---|---|
| Candidate 1 | Basic operational capture, assignment-scoped access, offline save/sync, optional S01-compatible subject-linked capture, correction basics, freshness, and unresolved issue visibility. |
| S06/entity lifecycle | Near-future product-deployment lane. FD-PKT-001 decides whether it stays after Candidate 1 spec, moves before Candidate 1 implementation planning, or runs as parallel discovery before the implementation gate. It needs BAR-105/S06 successor routing before implementation and must not be hidden behind "later" wording. |
| Production auth/admin/mobile login | Server OIDC/JWKS kernel is accepted, but admin auth, mobile OIDC UX, token refresh/logout, secure storage, and binding-admin operations are productization lanes. |
| Retention/security/device lifecycle | Route expiry, decommissioning, sealed partition recovery, local encryption, no-local-retention, and redaction through NW-054/BAR-106. |
| Reporting/import/export | S26 proves traceable inputs only. Route production dashboards, APIs, warehouses, export/import, and aggregate access through NW-044. |
| Conflict review UX | Existing single-flag behavior is usable evidence; queues, batch handling, automation, resolver reassignment, and auto-resolution need routed decisions. |
| Ops readiness | TLS, secrets, backup/restore, migration rollback, observability, support, auth manifest application, assignment bootstrap, and config publish need rehearsed runbooks. |

Use these status labels consistently in the workshop and in follow-up task
packets: `accepted`, `runtime-evidenced`, `product-surface-partial`,
`operator-deployable-with-constraints`, `needs-decision`, and `blocked`.

Evidence should be classified before claims are made:

| Evidence class | What it proves |
|---|---|
| Authority evidence | CDL/IDR/BAR/NW standing allows and routes the work. |
| Contract evidence | Schemas, protocol docs, and parity tests preserve boundary compatibility. |
| Code inspection evidence | Current implementation matches the claimed invariant. |
| Automated test evidence | Targeted Maven/Flutter suites and regression gates pass for the touched boundary. |
| Scenario runtime probes | Accepted constructs can run named scenarios; this does not prove finished UX. |
| Product/SME validation | Language, workflows, and acceptance criteria fit deployment users. |
| Ops evidence | Runbooks and rehearsals cover deployment, recovery, monitoring, and support. |
| Release evidence | Staging rehearsal, checklist signoff, known-risk register, and go/no-go decision exist. |

### Milestone Skeleton

Use this as the first planning structure, then adjust after owner/capacity and
validation inputs are known:

| Milestone | Output |
|---|---|
| M0 Standing alignment | Current standing brief, first-deployment definition, claim-status table, and input gaps. |
| M1 Candidate 1 platform spec | Bounded Candidate 1 spec with validation caveats, explicit exclusions, and acceptance evidence. |
| M2 Product/SME validation | Vocabulary, setup flow, offline confidence, subject lookup, correction language, and support workflow validation. |
| M3 Implementation-ready task packets | One bounded packet per surface, with authority, files, dependencies, tests, stop conditions, and commit boundary. |
| M4 Operator deployment readiness | Auth manifest, assignment bootstrap, config publish, TLS/secrets, backup/restore, monitoring, and support runbooks. |
| M5 Product UX over accepted kernel | Mobile capture/offline/shared-device polish and web admin/config UX, with production gates clearly marked. |
| M6 Successor decision lanes | S06/BAR-105 lifecycle, production admin/mobile auth, NW-054 retention/security, NW-044 reporting, and conflict-review decisions. |

### Release Readiness Gates

Do not call a milestone release-ready until every applicable gate is satisfied:

1. Authority gate: BAR/NW/IDR route is accepted or explicitly scheduled.
2. Contract gate: no envelope, type, schema, or protocol drift exists without
   authority.
3. Test gate: targeted tests for the touched boundary pass, plus broader
   regression gates for shared behavior.
4. Scenario gate: the named user workflow has runtime or manual scenario
   evidence.
5. Product gate: SME/user validation confirms language and workflow fit.
6. Security/ops gate: auth, secrets, retention, backup, monitoring, and
   recovery checklists are complete where claimed.
7. Claim gate: every release claim is labeled accepted, evidenced, partial,
   routed, blocked, or out of scope.

### Required Starting Inputs

The workshop should start with these inputs already collected and linked. If an
input is missing, the first workshop output should name it as a blocking
pre-work item rather than guessing.

| Input | Why it is needed |
|---|---|
| `AGENTS.md` and `docs/status.md` Current Routing | Establish default source order and current baseline standing. |
| Decision anchor layer README, architecture anchors, and gap-routing playbook | Keep architecture-sensitive work routed instead of improvised. |
| BAR and platform next-work backlog | Separate accepted baseline, deferred baseline, successor decisions, and runtime evidence. |
| NW-056 product standing map | Start from the current production/product readiness map, not memory. |
| Scenario user-fit README, synthesis, readiness matrix, and relevant packet files | Bring product-fit pressure and user vocabulary into planning. |
| Operational UX layering companion | Keep product terms useful without making them authority. |
| Escape hatch register | Keep measured evolution paths visible without authorizing implementation. |
| `docs/workshops/first-deployment/readiness-checklist.md` | Confirm the surface, role inputs, lane register, and evidence gates are ready before planning begins. |
| Current git status and uncommitted diff summary | Prevent planning against a stale or half-applied working surface. |
| Existing test inventory and recent accepted evidence | Let QA plan from actual tests, not assumed coverage. |
| Product/SME evidence available outside the repo | Identify what user validation exists and what still needs discovery. |

### Opening Sequence

1. Steward states current standing: accepted baseline, runtime scenario evidence,
   routed lanes, and forbidden work.
2. Product Manager states the first-deployment outcome and product-fit
   priorities, including S06/BAR-105 as a near-future lane if needed for the
   deployment.
3. UX Architect maps the product outcome into user journeys, state language,
   error/recovery flows, and UX validation gaps without promoting user terms
   into architecture.
4. Software Architect maps product and UX lanes to accepted contracts, code
   boundaries, technical dependencies, and successor decision routes.
5. Mobile App Builder pressure-tests mobile/offline/shared-device/auth
   implications and identifies mobile test targets and implementation caveats.
6. Reality Checker marks every major claim as accepted, evidenced, partial,
   routed, unknown, or blocked.
7. Test Results Analyzer maps each claim to existing evidence, missing tests,
   manual validation, or scenario probes.
8. Project Shepherd turns the visible lanes into milestones, dependencies,
   decision points, and change-control gates.
9. The group cuts Candidate 1 and near-future lanes without hiding any product
   need in vague "later" language.

### FD-PKT-001 Starting Rule

FD-PKT-001 should start from the entity-lifecycle architecture consolidation:
the architecture can support lifecycle pressure by splitting it across identity,
projection, conflict, assignment/access, shape/config, workflow/pattern,
access/sync, and reporting/freshness lanes. It is not one new primitive and it
is not implementation-ready by protocol alone.

The Product Manager owns the timing call. The steward accountability owns the
guardrails and route. A valid outcome may keep Candidate 1 first, but only if
Candidate 1 is explicitly S01-compatible and S06 remains visible with owner,
milestone, evidence need, and successor route.

### Required Workshop Outputs

The workshop is not complete until it produces:

| Output | Minimum content |
|---|---|
| First-deployment outcome | Who it serves, what work it enables, what is explicitly not promised. |
| Milestone roadmap | Candidate 1 plus visible near-future lanes such as S06/BAR-105, production auth/admin/mobile login, NW-054 retention/security, NW-044 reporting/import/export, and conflict-review UX. |
| Routed-lane register | Each product-needed gap, owner role, route, trigger/input, evidence needed, and planned decision point. |
| QA and acceptance matrix | Tests, scenario probes, manual validation, release checks, and BAR/NW evidence required per milestone. |
| Risk and change-control register | Risks, assumptions, drift triggers, escalation route, and who can approve scope changes. |
| Agent task-packet backlog | Bounded future prompts with files to read, authority, forbidden work, expected tests, and stop conditions. |
| Timeline view | Sequencing, rough effort bands, parallelization opportunities, and decision deadlines. |

## Surface Cleanup Rule

Untracked or unreferenced review scratch can be removed after the standing
review is captured here and in the packet README. Do not remove active
working-surface files, scenario sources, contracts, BAR/NW artifacts, or any
user-authored uncommitted work unless the user explicitly asks for that exact
cleanup.
