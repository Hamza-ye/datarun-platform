# First Deployment Planning Workshop Control

Status: active human workshop-control surface

Date: 2026-06-12

Workshop lead role: Workshop Lead / Delivery Evidence Facilitator

Authority: none. This file coordinates the planning workshop. It does not
change CDL, contracts, BAR, NW, `docs/status.md`, schemas, APIs, code, or
implementation authority. It may be cited by human operators and role workers.
Implementation agents should receive bounded task packets derived from approved
workshop outputs, not this whole file by default.

## Operating Premise

This workshop is a multi-stage planning workshop for the first product
deployment. It is not an architecture-steward-only review and it is not an
implementation sprint kickoff until the evidence gates are satisfied.

The workshop lead keeps the whole loop moving:

- product outcome;
- architecture standing;
- user experience and vocabulary;
- system and mobile feasibility;
- delivery plan;
- QA evidence;
- risks;
- scope control;
- bounded agent task packets.

Architecture stewardship is an accountability in the room. It may be held by a
separate Architecture Steward role or by the Software Architect when splitting
the roles would create process friction. Either way, the accountability owns
source order, authority, routing, and stop conditions. It does not own the
product outcome or delivery plan by itself.

## Current Standing Snapshot

As of the workshop kickoff snapshot:

- Repository tree was clean before this control file was added.
- `docs/status.md` says Phase 4 is complete and post-Phase-4 stabilization
  routes through `docs/agent-working-surface/`.
- BAR-001 through BAR-015 and BAR-104 are accepted.
- Runtime scenario evidence includes NW-025/S19, NW-026/S00, NW-029/S21,
  NW-030/S27, NW-032/S23, NW-033/S26, and NW-042/S22.
- NW-057 accepted the fixed `context.*` expression property boundary.
- NW-056 states the platform is an accepted field-operations kernel and can be
  operator-deployed with constraints, but it is not a turnkey production
  product.
- Scenario user-fit packets are product/problem evidence only.
- Product-needed gaps remain visible routed lanes, especially S06/entity
  lifecycle, production auth/admin/mobile login, retention/security,
  reporting/import-export, conflict review UX, and ops readiness.

## Stage Plan

| Stage | Name | Lead role | Other roles | Entry condition | Exit artifact |
|---|---|---|---|---|---|
| 0 | Readiness and packet setup | Workshop Lead | Steward as source-order reviewer | Clean/understood repo state and current standing links available. | Readiness snapshot, starting packet, role briefs, lane register. |
| 1 | Standing and stop conditions | Software Architect carrying steward accountability, or Architecture Steward | Reality Checker | Active sources loaded and current routing understood. | Authority map, accepted/runtime/routed status table, forbidden-work list. |
| 2 | Product outcome and scope pressure | Product Manager | Workshop Lead, Reality Checker | Stage 1 authority map available. | First-deployment outcome, product lanes, non-goals, validation questions. |
| 3 | UX and vocabulary bridge | UX Architect | Product Manager, Steward | Product outcome drafted. | User journey map, state language, vocabulary guardrails, UX evidence gaps. |
| 4 | System boundary bridge | Software Architect | Steward, UX Architect | UX/product lanes drafted. | Technical dependency map, contract/code boundary map, successor route map. |
| 5 | Mobile/offline feasibility bridge | Mobile App Builder | UX Architect, Software Architect | Mobile-relevant journeys and constraints available. | Mobile feasibility notes, Flutter/manual test targets, offline/sync risks. |
| 6 | Reality and evidence pressure test | Reality Checker, Test Results Analyzer | All roles | Product, UX, system, and mobile outputs available. | Claim-status table, QA/evidence matrix, release gates, blocked list. |
| 7 | Delivery plan and change control | Project Shepherd | Workshop Lead, all roles | Claims and evidence gates classified. | Milestone roadmap, dependency map, decision calendar, change-control rules. |
| 8 | Agent task-packet backlog | Workshop Lead | Steward, Test Results Analyzer, Project Shepherd | Approved milestones and gates available. | Bounded task packets with authority, files, tests, stop conditions. |

## Role Timing Rules

- Product Manager enters after the steward accountability states the current standing, so
  product ambition is framed against reality without being suppressed by it.
- UX Architect enters after the product outcome, before software architecture,
  because UX must expose user journeys and vocabulary risks before technical
  slicing.
- Software Architect enters after UX, because system boundaries should answer
  concrete product/user flows rather than abstract capability lists.
- Mobile App Builder enters after UX and Software Architect, because mobile
  feasibility depends on both user journey expectations and accepted platform
  boundaries.
- Reality Checker and Test Results Analyzer enter after the role outputs exist,
  so they can classify claims and evidence instead of arguing hypotheticals.
- Project Shepherd enters after claim/evidence classification, so milestones
  are based on gates and dependencies, not wishful sequencing.
- Workshop Lead stays active across all stages and owns the control table,
  agenda, unresolved-input register, and final artifact consolidation.
- If Software Architect carries stewardship, keep the outputs separate:
  Stage 1 must produce source order, accepted/routed standing, and stop
  conditions; Stage 4 must later produce technical dependency and boundary
  mapping after product and UX inputs exist.

## Role Dispatch Guardrail

Every dispatched role worker receives repo routing context, but that context is
not a role assignment. `AGENTS.md`, `docs/status.md`, and the working surface
tell agents how to read the repository; they do not make every worker the
Architecture Steward.

Use this instruction in every role-worker prompt:

```txt
Your assigned workshop role is [ROLE]. Do not assume Architecture Steward
accountability unless this prompt explicitly assigns it. Use AGENTS.md,
docs/status.md, BAR/NW, and the working surface as source-order context only.
Do not produce authority decisions, implementation authorization, or stop
conditions outside your assigned role.
```

Only the Stage 1 worker, or a Software Architect explicitly carrying the Stage
1 stewardship accountability, should produce the source-order brief,
accepted/routed standing, and forbidden-work list. Other roles may identify
risks or questions for the steward, but they should not recast themselves as
the steward.

Detailed reusable role packets live in
`docs/reviews/first-deployment-workshop-role-packets.md`.

## Facilitation Protocol

The workshop runs as staged evidence collection and synthesis, not as open
debate.

The protocol exists to protect the product outcome from drift, overclaim, and
hidden gaps. It must not protect the protocol for its own sake. If a real
product need is not currently accepted or evidenced, keep it visible as a lane
with route, owner, decision point, and evidence need; do not use process
language to make it disappear.

Rules:

- Each role produces a bounded packet in its assigned stage.
- No participant changes role midstream. If they see a concern outside their
  role, they write it as a question or risk for the owner role.
- Workshop Lead accepts, rejects, or parks each role output based on role fit,
  evidence labeling, and route discipline.
- Architecture/stewardship questions are answered by the steward
  accountability only.
- Product decisions are owned by Product Manager, then checked against
  authority and evidence.
- UX vocabulary may guide screens, validation, and task packets, but it does
  not become platform vocabulary.
- Reality and QA roles may challenge production claims, but they must preserve
  product-needed gaps as visible routed lanes.
- Delivery planning starts only after claim status and evidence gates are
  classified.
- Any unresolved or off-stage topic goes into the unresolved-input register or
  lane register; it does not derail the current stage.
- Implementation remains blocked until a lane has route, owner, evidence,
  acceptance gate, expected tests, and stop conditions.

## Initial Lane Register

| Lane | Product need | Current standing | Route | Current workshop status |
|---|---|---|---|---|
| Candidate 1 basic operational capture | First bounded product/spec slice for configured capture, assignment-scoped access, offline save/sync, optional subject-linked capture, correction basics, freshness, and unresolved issue visibility. | Kernel capabilities accepted across BAR-001..015 and scenario runtime probes; product surface partial. | Candidate 1 platform spec, then validation and bounded task packets. | Ready for Product Manager and UX Architect framing. |
| S06/entity lifecycle | Known set of things, discovered-unit lifecycle, active/inactive/retired states, merge/split UX, lifecycle vocabulary. | BAR-105 deferred from accepted baseline; near-future product-deployment lane, not hidden as unscheduled work. | BAR-105/S06 successor product/platform decision before implementation. | Visible lane; needs Product Manager priority and Steward route. |
| Production auth/admin/mobile login | Real admin auth, mobile OIDC/Keycloak login, token lifecycle, online binding-admin product needs. | BAR-104 server-side kernel accepted; mobile login UX and production admin auth not productized. | Product/platform decisions for admin auth and mobile OIDC lifecycle; no IdP group/claim authority. | Visible lane; needs PM, Software Architect, Mobile App Builder, ops evidence. |
| Retention/security/device lifecycle | Expiry, decommissioning, sealed-partition recovery, local encryption, no-local-retention/redaction. | Shared-device partitions accepted; broader retention/security remains BAR-106/NW-054. | NW-054/BAR-106 decision. | Visible lane; needs security/ops evidence before production claims. |
| Reporting/import-export | Dashboards, report APIs, export/import, aggregates, freshness/drill-back product claims. | S26 proves traceable report inputs only; no production reporting surface. | NW-044 decision before product reporting/API/export implementation. | Visible lane; needs product scope and route decision. |
| Conflict review UX | Resolver queues, humane review, resolver-steward eligibility, batch handling, automation, resolver reassignment, auto-resolution pressure. | Single-flag resolver equality accepted; current steward eligibility uses a role-name fallback heuristic and is not product-ready authority. Batch/automation/reassignment are not accepted. | Single-flag UX can be designed after admin-auth route; steward eligibility must be made explicit before productizing conflict/admin UX; batch/automation through NW-045/BAR-102/BAR-103. | Visible lane; needs UX, evidence, and resolver-policy split. |
| Subject/query/custom scope | Auditor/reporting/admin views may pressure the current scope model. | Current scope axes remain geography, subject-list, activity, and temporal bounds; BAR-108 is future decision. | NW-053/BAR-108 before any new scope mechanism or query/custom scope. | Visible risk lane; keep explicit during reporting, auditor, and admin planning. |
| Ops readiness | TLS, secrets, backup/restore, migration rollback, observability, support, auth manifests, assignment bootstrap, config publish. | Operator-deployable-with-constraints; turnkey ops not started. | Ops readiness runbook and rehearsal evidence. | Visible lane; required before production wording. |

## Claim Status Vocabulary

Use these labels in every role output and workshop artifact:

| Status | Meaning |
|---|---|
| `accepted` | Accepted into current baseline with BAR/NW evidence. |
| `runtime-evidenced` | A named scenario probe proves current constructs can run the path; not necessarily a finished product workflow. |
| `product-surface-partial` | Underlying capability exists, but UX/admin/mobile/product surface is incomplete or development-only. |
| `operator-deployable-with-constraints` | A skilled operator can deploy with external process, infrastructure, and runbooks. |
| `needs-decision` | Productization or implementation needs a successor decision route first. |
| `blocked` | A required input, decision, owner, or evidence item is missing. |
| `out-of-scope` | Explicitly outside the current product/deployment slice. |

## Evidence Taxonomy

| Evidence class | Required use |
|---|---|
| Authority evidence | CDL/IDR/BAR/NW standing that permits or routes the work. |
| Contract evidence | `contracts/` schemas, protocol docs, parity tests, and fixture compatibility. |
| Code inspection evidence | Current implementation follows the claimed invariant. |
| Automated test evidence | Targeted Maven/Flutter tests for touched boundaries and broader regressions for shared behavior. |
| Scenario runtime probes | Runtime evidence that accepted constructs can run named scenarios. |
| Product/SME validation | User/SME evidence for vocabulary, workflow fit, and acceptance criteria. |
| UX evidence | Journey walkthroughs, prototypes/screenshots, state/error flows, accessibility/localization evidence where claimed. |
| Ops evidence | Runbook rehearsals for deployment, auth manifests, assignment bootstrap, config publish, backup/restore, monitoring, and incident handling. |
| Release evidence | Staging rehearsal, release checklist, known-risk register, and go/no-go decision. |

## Stage 0 Readiness Snapshot

| Checklist area | Status | Notes |
|---|---|---|
| Surface state | Ready | Repo was clean at kickoff; active standing sources are linked below. |
| Authority and routing | Ready | CDL/contracts/BAR/NW source order and stop conditions are known. |
| Product management | Needs role input | Product Manager must confirm first-deployment outcome, target users, non-goals, and validation questions. |
| UX architecture | Complete as facilitation draft | Stage 3 UX packet exists at `docs/reviews/first-deployment-workshop-stage-3-ux.md`; a future UX Architect may refine it. |
| Software architecture | Complete | Stage 4 Software Architecture packet exists at `docs/reviews/first-deployment-workshop-stage-4-software-architecture.md`. |
| Mobile app building | Complete | Stage 5 Mobile packet exists at `docs/reviews/first-deployment-workshop-stage-5-mobile.md`. |
| QA and evidence | Complete | Stage 6 Pressure Test packet exists at `docs/reviews/first-deployment-workshop-stage-6-pressure-test.md`. |
| Delivery planning | Complete | Stage 7 Delivery Plan exists at `docs/reviews/first-deployment-workshop-stage-7-delivery-plan.md`. |
| Task-packet backlog | Complete | Stage 8 Task-Packet Backlog exists at `docs/reviews/first-deployment-workshop-stage-8-task-packet-backlog.md`. |

## Completed Stage Outputs

### Stage 1 Standing And Stop Conditions

Status: complete.

Owner: Software Architect carrying Architecture Steward accountability.

Source order for this workshop remains CDL and contracts first, then the
decision anchor layer, BAR, NW backlog, NW-056, and this control file. NW-056
and this file are coordination/product-standing surfaces; neither authorizes
implementation.

Accepted first-deployment kernel standing:

| Capability | Accepted evidence | Product meaning | Not accepted by this |
|---|---|---|---|
| Append-only capture/correction | BAR-001, BAR-002, BAR-015, NW-026/S00 | Existing envelope, closed event-type vocabulary, append-only record/correction behavior, historical location-path stability. | New envelope fields/types or mutable record state. |
| Push/pull sync and subject history | BAR-003, BAR-004, BAR-007, NW-025/S19 | Idempotent push, ordered/scoped pull, independent subject-history cursor, assignment-derived access. | Historical pull replacing normal sync or new scope mechanisms. |
| Assignment containment/commands | BAR-007, IDR-029, NW-050 | Assignment create/end are command-capable and same-assignment contained. | Broad admin authority, IdP-derived authority, emergency override. |
| Identity merge/split and aliases | BAR-009 | Merge/split are event-based; aliases are rebuildable; historical subject refs are not rewritten. | General entity lifecycle or discovered-unit lifecycle. |
| Config packages and expressions | BAR-010, BAR-011, NW-057 | Atomic packages, deploy-time validation, fixed seven `context.*` refs, mobile current/pending promotion. | Config-as-code, deployer state machines, custom context/query namespace. |
| Patterns and projections | BAR-012, BAR-014 | Platform-owned pattern definitions and rebuildable server/mobile projection equivalence. | Durable workflow-state authority or arbitrary pattern traversal. |
| Flags/conflict/resolution | BAR-006, BAR-013 | Accept-and-flag, resolver equality, unresolved-flag exclusion, exact resolver re-inclusion. | Auto-resolution, resolver reassignment, batch resolution. |
| Mobile local/offline substrate | BAR-008, NW-055 | Selective retain, advisory warnings, shared-device actor partitions, actor-scoped sync state. | Mobile authoritative rejection, retention/security/decommissioning policy. |
| Production provider auth kernel | BAR-104, NW-037, NW-038, NW-040 | OIDC/JWKS tokens authenticate only through explicit active `(issuer, subject) -> actor_id` bindings; provisioning is manifest-managed and audited. | Mobile OIDC login UX, production admin auth, online binding-admin API, IdP group/claim authority. |

Runtime scenario evidence summary:

| Scenario | Proves | Does not prove |
|---|---|---|
| S00 | Structured capture, appended correction, idempotent retry, watermark pull, existing conflict mechanics. | Finished capture UX or mutable correction model. |
| S19 | Stale offline work is accepted and flagged; normal pull stays current-scope; subject history has independent cursor. | Retention policy, offline recovery UX, broad audit/history pull. |
| S21 | Supervisor visibility, review advancement, unauthorized-review flagging, exact resolver re-inclusion. | Production review queue, auto-resolution, resolver reassignment. |
| S22 | Coordinated campaign through existing assignments, capture, handoff, transfer pattern, scoped sync, duplicate flags, test-local progress/freshness aggregation. | Entity lifecycle, custom campaign scope, report API, trigger engine. |
| S23 | Invalid setup fails before publish; valid config packages atomically; version coexistence and mobile promotion work. | Product config builder, approval workflow, production admin auth. |
| S26 | Scoped report inputs can expose timestamps, watermarks, unresolved flag counts, resolver inclusion/exclusion, drill-back links. | Production dashboard/API, reporting warehouse, export/import model. |
| S27 | Logistics transfer uses existing transfer pattern, discrepancy handling, scoped sync, and transition flags. | Custom custody scope, logistics screens, auto-resolution. |

Stage 1 added one control-table risk lane: subject/query/custom scope under
NW-053/BAR-108. Keep it explicit because it can leak through reporting,
auditor visibility, admin UX, and conflict review.

Stage 1 stop conditions:

- No new envelope fields or event `type` values.
- No durable workflow-state authority or mutable canonical status.
- No normal sync watermark rewrites or live-sync replacement with historical
  pull.
- No IdP group/claim/role/JWT `actor_id` authority.
- No production admin authority, online binding-admin API, or mobile
  OIDC/token lifecycle without routed decisions.
- No S06/entity lifecycle, new scope mechanisms, retention/security,
  auto-resolution, resolver reassignment, emergency override, or batch
  resolution without successor authority.
- No mobile authoritative rejection of structurally valid policy/state
  anomalies.
- No deployer config as scripts, access logic, state machines, trigger
  execution, or query authority.
- No dev admin surface or dev compose treated as production hardening.
- Stop on source drift, contract/runtime disagreement, or product terms
  becoming architecture.

### Stage 2 Product Outcome And Scope Pressure

Status: complete.

Owner: Product Manager.

First-deployment outcome:

> First deployment should prove Datarun as an operator-managed
> field-operations product: people acting in setup, field execution, and review
> contexts can set up assigned capture work, complete it offline or online, and
> see the latest synced view with freshness and unresolved issues. It is not a
> turnkey production product until auth/admin, mobile login, retention/security,
> reporting, and ops runbooks are evidenced.

Operational surface labels:

| Acting context | Job |
|---|---|
| Acting as coordinator/setup owner | Define what needs capture, who is responsible, and when work is ready. |
| Acting as field user | See assigned work, capture records, optionally link records to known things, work offline, and trust save/sync state. |
| Acting as supervisor/reviewer | Understand latest synced progress, freshness, corrections, duplicates, and work needing review. |
| Acting as operator/admin | Provision deployment inputs, users/bindings, assignments, config, and support procedures. |
| Acting in support role | Help with failed sync, lost/stale devices, access changes, and unclear review states. |

These labels are persona lenses, not identity categories, authority primitives,
fixed UI modules, config namespaces, or implementation boundaries. Authority
must be described as current actor plus active assignment, role, scope, time,
and activity/context, producing available actions, visible data, and a projected
operational surface.

Candidate 1 product promise:

> Teams can collect assigned field records reliably, even with intermittent
> connectivity, without losing original context or hiding work that needs
> review.

Candidate 1 non-goals:

- No full registry/entity lifecycle, merge/split UX, or canonical
  active/inactive lifecycle state.
- No turnkey production claim.
- No production web admin auth, mobile OIDC login, or online binding-admin UI.
- No reporting dashboard/API/export/import productization.
- No retention/security promises beyond currently evidenced behavior.
- No advanced conflict queue, batch handling, automation, resolver
  reassignment, or auto-resolution.
- No product term becoming platform authority.
- No persona label becoming a fixed module, hard role category, access rule, or
  implementation slice.

Product-fit risks:

- Architecture terms such as assignment, sync, stale, projection, and flag may
  not match user language.
- Persona labels may harden into product/spec categories if task packets do not
  say which current authority context backs the surface.
- Offline trust may fail if local save, pending sync, synced, failed, and
  needs-review states are unclear.
- Subject-linked capture may accidentally pull full registry lifecycle into
  Candidate 1.
- Supervisors may treat latest synced views as live truth.
- Setup/config may be too technical for coordinators.
- Missing-subject and correction flows may create false confidence if review
  ownership is unclear.
- First deployment may be oversold without ops/security evidence.

Product acceptance criteria:

- Coordinator can explain and validate the basic setup flow without
  architecture vocabulary.
- Field user can complete standalone and subject-linked capture under assigned
  work.
- Field user understands local save, pending sync, synced, failed, and
  needs-review states.
- Missing-subject handling keeps work moving without creating canonical
  lifecycle truth.
- Supervisor can distinguish latest synced state from live field reality.
- Corrections and review states preserve the original record in user-visible
  language.
- Candidate 1 explicitly labels product-fit caveats and routed lanes before
  implementation is authorized.

### Stage 3 UX And Vocabulary Bridge

Status: complete as facilitation draft.

Owner: Workshop Lead / Delivery Evidence Facilitator wearing a temporary UX
Facilitation hat.

Reason: the first UX Architect worker dispatch failed due usage limit before
producing a packet. To keep the workshop moving without unstructured debate,
Stage 3 was completed as a bounded UX-stage draft and recorded separately at
`docs/reviews/first-deployment-workshop-stage-3-ux.md`.

Stage 3 decides user journey shape, product vocabulary candidates, state and
recovery language, UX risks, validation artifacts, and questions for Stage 4
and Stage 5. It does not decide architecture authority or implementation.

Key UX boundaries:

- Product language is presentation and validation language only.
- Candidate 1 can include optional subject-linked capture, but not canonical
  entity lifecycle.
- Missing-subject capture must remain unpromoted/candidate/review-oriented
  until S06/BAR-105 is routed.
- Local save, waiting to sync, synced, failed sync, needs review, access ended,
  and latest synced view must be distinct user states.
- Latest synced view must not be described as live field truth.
- Needs-review and attention-item UX must preserve accept-and-flag and exact
  resolver semantics.
- Reporting dashboards, broad audit views, custom filters, and aggregate
  drill-down remain routed through NW-044/NW-053 when they exceed Candidate 1.

Stage 3 evidence required before implementation:

- journey walkthroughs for coordinator, field user, supervisor, and
  operator/support roles;
- low-fidelity setup, work list, capture, sync state, correction, and review
  screens;
- vocabulary validation for assigned work, known thing, saved locally, waiting
  to sync, synced, failed, needs review, access ended, and latest synced view;
- offline/failed-sync, missing-subject, supervisor freshness, and shared-device
  switch walkthroughs where claimed.

### Stage 4 Software Architecture Boundary Map

Status: complete.

Owner: Software Architect.

Stage 4 maps Candidate 1 to existing system boundaries and successor routes.
The full packet is recorded at
`docs/reviews/first-deployment-workshop-stage-4-software-architecture.md`.

Candidate 1 technical boundary:

| Surface | Boundary |
|---|---|
| Contracts | Reuse existing envelope, sync protocol, flag catalog, deployer shape DSL, config package schema, platform payload schemas, pattern definitions, and shared fixtures. No new envelope fields/types, config sections, report contract, or scope contract. |
| Server | Reuse append-only event store, config publication, sync, assignment authorization, identity alias projection, integrity detection, and resolver services. Compose views from existing projections/events rather than durable workflow state. |
| Mobile | Reuse setup/connect, actor-resolved session, per-actor partitions, local pending events, work list, form capture, sync panel, advisory warnings, and selective retention. Mobile remains advisory for policy/state anomalies. |
| Config | Use deployer shapes, activities, role-action maps, severity overrides, expressions, and platform pattern bindings delivered by atomic config packages. No config scripts, deployer state machines, custom query namespaces, or scope logic. |
| Sync | Reuse idempotent push, ordered scoped pull, actor-scoped `device_sync_state`, independent subject-history cursor, and assignment-derived scope. Latest synced view is not live truth. |
| Identity | Use production principal binding for actor identity and existing subject refs/aliases for optional subject-linked capture. Missing-subject capture must remain unlinked/candidate/review-oriented, not canonical lifecycle. |
| Authorization | Reuse assignment-derived access, fixed scope axes, and assignment-admin command capability where applicable. No IdP group/claim/JWT `actor_id` authority. |
| Integrity | Reuse accept-and-flag, flag catalog, unresolved-flag exclusion, exact designated-resolver resolution, and event traceability. No direct flag mutation, auto-resolution, or resolver reassignment. |
| Admin surfaces | Current web admin/config surfaces are product-surface-partial/development-only. Production admin auth and online binding-admin UI remain successor routes. |

Stage 4 sequencing notes:

- UX view models should be adapter-level compositions over existing events,
  projections, flags, sync metadata, and assignment scope.
- If multiple shipped components need a new shared screen shape, route bounded
  view-model/contract work before implementation.
- Production admin auth precedes productionizing admin/config/review surfaces.
- Mobile OIDC/token lifecycle precedes any mobile-login production claim.
- Retention/security, reporting/import-export, and subject/query/custom scope
  remain NW-054/BAR-106, NW-044, and NW-053/BAR-108 lanes respectively.
- QA must keep accepted kernel, runtime scenario evidence,
  product-surface-partial UI, and operator-deployable-with-constraints ops
  separate.

Stage 4 drift risks:

- UX terms becoming fields, event types, durable state, scope mechanisms, or
  flag categories.
- Missing-subject capture becoming canonical entity lifecycle.
- Freshness UI implying live truth.
- Admin UX relying on dev actor, request-body actor, IdP roles/groups/claims,
  or JWT `actor_id`.
- Mobile warnings becoming authoritative rejection.
- Config UX adding scripts, custom traversal, loops, query authority, or
  deployer-authored state machines.
- Review UX bypassing exact designated-resolver semantics.
- Reporting/audit UX adding broad history reads, export/import contracts, or
  query/custom scope.
- Ops language treating dev compose/admin screens as production hardening.

### Stage 5 Mobile Feasibility

Status: complete.

Owner: Mobile App Builder.

Stage 5 assesses current Flutter/mobile feasibility and evidence needs. The
full packet is recorded at
`docs/reviews/first-deployment-workshop-stage-5-mobile.md`.

Candidate 1 mobile feasibility:

| Flow | Feasibility | Boundary |
|---|---|---|
| Setup/connect | Feasible as constrained raw bearer setup through `/api/auth/me` actor resolution. | Product-surface-partial, not production mobile login. |
| Work list | Feasible as a subject-centric list with active assignment roles, capture count, flag count, latest timestamp, pending-sync count, and sync entry point. | Product IA still needs assigned-work language. |
| Capture | Feasible through shape-driven forms, config promotion, defaults/show conditions, field warnings, and advisory role-action warnings. | Mobile warnings remain advisory. |
| Optional subject link | Feasible from subject detail; standalone capture currently creates a new subject UUID. | Candidate 1 copy/spec must keep standalone capture as unlinked/candidate capture, not S06 lifecycle truth. |
| Offline save | Feasible by writing a local pending event. | Label as saved locally, not submitted or server received. |
| Sync | Feasible through actor refresh, push, pull, config fetch, watermarks, pending counts, and error messages. | UX needs clearer waiting/syncing/synced/failed/synced-with-issue states. |
| Correction | Feasible only as another append-only capture path over the same subject. | Dedicated correction UX is not productized and must not imply in-place editing. |
| Review/freshness | Partial. Mobile can show local flags, flagged events, last sync, and latest timestamps. | No production review queue or live truth view. |

Stage 5 mobile risks:

- Local pending events survive failed sync, but UX must clearly say saved on
  this device and support retry.
- Server remains responsible for accepting and flagging valid stale work;
  mobile warnings stay advisory.
- Actor partitions must never read another actor's mutable local data.
- Shared-device switching may drain or seal pending work; recovery and
  decommissioning remain NW-054/BAR-106.
- Selective retain is not a security, expiry, decommissioning, or deletion
  promise.
- Unauthorized or actor-drift checks preserve pending work; UX cannot claim
  token refresh exists.
- Last sync and latest local timestamp are not live field truth or supervisor
  approval.

Stage 5 evidence targets:

- Flutter targets: `sync_service_test`, `event_assembler_test`,
  `activity_role_actions_test`, `selective_retain_test`,
  `projection_equivalence_test`, `projection_engine_test`, `form_engine_test`,
  `config_store_test`, `context_resolver_test`, and
  `expression_evaluator_test`.
- Candidate widget/integration targets: setup success/failure, offline save,
  advisory warning without blocking save, sync success/failure/unauthorized,
  append-only subject-detail capture, shared-device switch isolation, work-list
  pending/flag/latest/empty states.
- Manual walkthroughs: invalid token, valid setup, offline capture, app
  restart, sync retry, failed sync preservation, subject-linked and
  missing-known-thing capture, appended correction, stale/access-changed save,
  shared-device switch, and supervisor freshness interpretation.

### Stage 6 Reality And Evidence Pressure Test

Status: complete.

Owners: Reality Checker and Test Results Analyzer.

Stage 6 classifies claims, evidence, missing evidence, release gates, and
production wording risk. The full packet is recorded at
`docs/reviews/first-deployment-workshop-stage-6-pressure-test.md`.

Stage 6 claim summary:

| Claim | Status |
|---|---|
| Core append-only capture/correction kernel exists | `accepted` |
| Offline sync, scoped pull, subject-history support exist | `accepted` |
| Candidate 1 operational capture can be framed over current kernel | `runtime-evidenced` |
| Mobile setup/work list/capture/offline save/sync is feasible | `product-surface-partial` |
| Principal-binding provisioning can be operated by skilled deployers | `operator-deployable-with-constraints` |
| Mobile OIDC/Keycloak login is ready | `needs-decision` |
| Production web admin authentication is ready | `needs-decision` |
| Reporting/dashboard/API/export readiness | `needs-decision` |
| Retention/security/device lifecycle readiness | `needs-decision` |
| Conflict review queue/batch/auto-resolution is ready | `needs-decision` |
| Subject/query/custom scope through UI filters is ready | `needs-decision` |
| S06/entity lifecycle inside Candidate 1 | `out-of-scope` |
| Turnkey production product readiness | `blocked` |
| Ops readiness for production wording | `blocked` |

Stage 6 recommendations:

- Stage 7 delivery planning is GO.
- Candidate 1 product/spec and UX validation is CONDITIONAL GO.
- Candidate 1 implementation packets are NO-GO until scoped task packets
  include source files, contracts, tests, walkthroughs, excluded successor
  lanes, and stop/report conditions.
- Constrained operator-managed pilot is NO-GO until staging, mobile manual
  matrix, ops runbooks, auth manifest rehearsal, config/assignment bootstrap,
  and support paths are evidenced.
- Turnkey production product is NO-GO.
- Successor lanes are NO-GO for implementation until decisions and evidence
  plans exist.

Stage 6 product-preservation rule:

- S06/entity lifecycle, production auth/admin/mobile login,
  retention/security, reporting/import-export, conflict review queues,
  subject/query/custom scope, and ops readiness are real product pressure
  lanes. Their missing evidence becomes explicit decisions, tests,
  walkthroughs, and release gates, not quiet exclusions.

Stage 7 must answer the S06 timing question:

- If Candidate 1 includes only optional subject link or missing-known-thing
  capture, keep S06 explicitly excluded with UX copy and tests that prevent
  lifecycle overclaim.
- If first deployment requires maintained known things, lifecycle states, or
  discovered-unit stewardship, schedule an earlier BAR-105/S06
  product/platform decision.

### Stage 7 Delivery Planning

Status: complete.

Owner: Project Shepherd.

Stage 7 defines the milestone roadmap, dependencies, owners, decision calendar,
change-control rules, and Stage 8 task-packet structure. The full packet is
recorded at
`docs/reviews/first-deployment-workshop-stage-7-delivery-plan.md`.

Milestone roadmap:

| Milestone | Purpose | Exit gate |
|---|---|---|
| M0 Workshop delivery baseline | Confirm lane register, owner map, claim labels, and change-control rules. | Workshop Lead accepts Stage 7 packet. |
| M1 Candidate 1 product/spec and UX validation | Define bounded operational capture over existing kernel. | Product/UX validation artifacts, S06 timing decision, explicit exclusions. |
| M2 Evidence and packet-gate design | Define automated tests, probes, walkthroughs, ops checks, source files, contracts, and stop conditions. | No implementation packet is missing evidence class, owner, route, or exclusion list. |
| M3 Stage 8 task-packet approval | Workshop Lead packages narrow agent tasks. | Each packet is one lane only and has authority/routing, files, expected tests, manual evidence, stop conditions, and commit boundary. |
| M4 Candidate 1 implementation slices | Execute bounded UI/view-model/mobile/server tasks separately, only after M3 approval. | Targeted tests and walkthrough evidence pass for each slice. |
| M5 Integrated staging rehearsal | Setup/config publish, assignment bootstrap, auth manifest operation, mobile offline/sync/correction, unresolved issue visibility, support paths. | Staging evidence and known-risk register. |
| M6 First constrained operator-managed deployment go/no-go | Decide constrained deployment readiness, not turnkey product readiness. | Staging pass, mobile manual matrix, ops runbooks, auth manifest rehearsal, config/assignment bootstrap, backup/restore/rollback/monitoring/support evidence. |

Stage 7 delivery rules:

- Candidate 1 remains `CONDITIONAL GO` for product/spec and UX validation only.
- Candidate 1 implementation remains `NO-GO` until Stage 8 produces scoped
  packets with files, contracts, tests, walkthroughs, excluded lanes, gates,
  and stop/report conditions.
- S06 timing must be decided before Candidate 1 packet freeze.
- No Stage 8 packet may mix Candidate 1 with auth, reporting,
  retention/security, S06 lifecycle, conflict automation, or custom scope.
- Constrained deployment is an evidence milestone, not a synonym for turnkey
  production.

### Stage 8 Task-Packet Backlog

Status: complete.

Owner: Workshop Lead / Delivery Evidence Facilitator.

Stage 8 converts workshop outputs into bounded future task-packet candidates.
The full backlog is recorded at
`docs/reviews/first-deployment-workshop-stage-8-task-packet-backlog.md`.

Immediate packet backlog:

| Packet ID | Lane | Status | Owner role | Purpose |
|---|---|---|---|---|
| FD-PKT-001 | S06 timing decision | `needs-decision` | Product Manager + steward accountability | Decide whether Candidate 1 excludes lifecycle with UX copy/tests or moves BAR-105/S06 decision before Candidate 1 implementation planning. |
| FD-PKT-002 | Candidate 1 product/spec and UX validation | `conditional-go` for spec/validation | Product Manager + UX owner | Produce bounded Candidate 1 product/spec, validation questions, vocabulary tests, journey walkthrough requirements, and explicit exclusions. |
| FD-PKT-003 | Candidate 1 evidence plan | `conditional-go` for evidence design | Test Results Analyzer | Convert Candidate 1 acceptance criteria into automated tests, scenario probes, manual walkthroughs, ops checks, and release gates. |
| FD-PKT-004 | Candidate 1 mobile/offline validation packet | `product-surface-partial` | Mobile App Builder | Define mobile UX/spec evidence for setup/connect, offline save, sync failure, shared-device switch, correction, and freshness. |
| FD-PKT-005 | Candidate 1 view-model/contract assessment | `needs-routing-check` | Software Architect + steward accountability | Decide whether Candidate 1 needs only adapter-level UI composition or a bounded shared view-model contract route before implementation. |
| FD-PKT-006 | Ops readiness runbook plan | `blocked` for production claims | Project Shepherd + Test Results Analyzer | Define TLS/secrets, backup/restore, migration rollback, monitoring, incident/support, auth manifest, config publish, and assignment bootstrap rehearsal plan. |
| FD-PKT-007 | Integrated staging rehearsal plan | `blocked` for constrained deployment | Project Shepherd + QA owner | Define staging scenario tying server, mobile, config publish, auth manifest, assignment bootstrap, offline/sync/correction, unresolved issue visibility, and support paths. |

Stage 8 result:

- Workshop planning control is complete.
- Implementation remains blocked until individual packets are written,
  reviewed against the packet gate, and explicitly dispatched.
- The immediate next non-implementation action is FD-PKT-001: S06 timing
  decision for Candidate 1 freeze.

## Starting Input Packet

Use these as the compact starting packet for the workshop:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/README.md`
- `docs/agent-working-surface/decision-anchor-layer/README.md`
- `docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/agent-working-surface/baseline-acceptance-register.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`
- `docs/agent-working-surface/operational-ux-layering-companion.md`
- `docs/agent-working-surface/escape-hatch-register.md`
- `docs/scenarios/scenario-user-fit-packets/README.md`
- `docs/scenarios/scenario-user-fit-packets/scenario-user-fit-synthesis-across-s00-s01-s06-s06b-access-control-S19.md`
- `docs/scenarios/scenario-user-fit-packets/foundational-product-fit-readiness-and-validation-matrix.md`
- relevant scenario packet for the lane under discussion
- `docs/reviews/pre-workshop-readiness-checklist.md`
- `docs/reviews/scenario-user-fit-packets-standing-review-and-playbook.md`
- `docs/reviews/first-deployment-workshop-role-packets.md`
- `docs/reviews/first-deployment-workshop-stage-3-ux.md`
- `docs/reviews/first-deployment-workshop-stage-4-software-architecture.md`
- `docs/reviews/first-deployment-workshop-stage-5-mobile.md`
- `docs/reviews/first-deployment-workshop-stage-6-pressure-test.md`
- `docs/reviews/first-deployment-workshop-stage-7-delivery-plan.md`
- `docs/reviews/first-deployment-workshop-stage-8-task-packet-backlog.md`

## Role Briefs

### Architecture Steward Accountability

This accountability can be held by a separate Architecture Steward or by the
Software Architect.

Produce:

- current source-order brief;
- accepted baseline table;
- runtime scenario evidence table;
- routed/future-decision lanes;
- forbidden-work list;
- stop conditions.

Do not produce:

- product roadmap;
- UX vocabulary as architecture;
- implementation authorization for routed lanes.

### Product Manager

Produce:

- first-deployment outcome statement;
- target deployment personas/jobs;
- Candidate 1 product promise and non-goals;
- visible product lanes, including S06/entity lifecycle if needed early;
- SME/user validation questions;
- product acceptance criteria.

Do not produce:

- architecture primitives;
- implementation tasks before route/evidence gates;
- hidden "later" buckets for product-needed gaps.

### UX Architect

Produce:

- journey map for setup, assignment, capture, offline save, sync, subject
  lookup, correction, review, access ended, unresolved issue visibility, and
  support/error recovery;
- product vocabulary and state language;
- UX risks;
- validation artifacts needed.

Do not produce:

- platform vocabulary changes;
- authority rules;
- UI decisions that bypass accepted assignment/auth/sync boundaries.

### Software Architect

Produce:

- technical dependency map;
- contract/code boundary map;
- route map for successor lanes;
- architecture risk list;
- implementation caveats for future task packets.

Do not produce:

- code changes;
- envelope/type/schema changes without authority;
- successor implementation authorization without accepted route.

If also carrying the Architecture Steward accountability, produce the Stage 1
standing packet before doing Stage 4 system-boundary work. Do not collapse the
two outputs into one vague architecture opinion.

### Mobile App Builder

Produce:

- mobile feasibility notes for Candidate 1 and visible successor lanes;
- offline/sync/shared-device/auth risks;
- Flutter test targets and manual mobile walkthroughs;
- mobile implementation caveats.

Do not produce:

- mobile authoritative rejection of structurally valid events;
- local actor/scope authority;
- OIDC/token lifecycle behavior without a routed decision.

### Reality Checker

Produce:

- claim-status table;
- overstatement risks;
- blocked/needs-decision list;
- production-claim stop conditions.

Do not produce:

- delivery plan;
- product outcome;
- un-routed implementation permission.

### Test Results Analyzer

Produce:

- QA/evidence matrix;
- test inventory;
- missing-evidence list;
- release-readiness gates;
- go/no-go recommendation per milestone.

Do not produce:

- scope decisions;
- authority decisions;
- product claims that lack evidence.

### Project Shepherd

Produce:

- milestone roadmap;
- dependency map;
- owner/role matrix;
- decision calendar;
- change-control rules;
- task-packet backlog structure.

Do not produce:

- authority exceptions;
- production claims without evidence gates;
- implementation tasks that combine unrelated successor lanes.

## Control Tables To Maintain During The Workshop

### Lane Control Table

| Lane | Product need | Current standing | Evidence | Risk | Decision needed | Owner role | Milestone | Acceptance gate | Stop condition |
|---|---|---|---|---|---|---|---|---|---|
| Candidate 1 | TBD by PM | BAR/NW accepted kernel + partial product surfaces | BAR/NW + scenario probes + UX/SME validation needed | Product overclaim | Candidate 1 spec approval | PM + Steward + Project Shepherd | M1/M2/M3 | Spec, validation, tests | Stop if product terms become architecture. |
| S06/entity lifecycle | TBD by PM | BAR-105 deferred from baseline, near-future lane | Scenario/user-fit evidence only so far | Hidden product dependency | BAR-105/S06 successor route | PM + Steward | M6 or earlier if deployment requires | Decision + evidence plan | Stop if candidate subject becomes canonical lifecycle state. |
| Admin/mobile auth | TBD by PM/Mobile | BAR-104 accepted server kernel; product surfaces partial/not started | BAR-104 + missing UX/ops evidence | Production auth overclaim | Admin auth/mobile OIDC decisions | Software Architect + Mobile App Builder | M4/M5/M6 | Route + tests + runbooks | Stop if IdP claims/groups become authority. |
| Retention/security | TBD by PM/Ops | NW-054/BAR-106 needed | Current selective retention/actor partitions only | Data/security production overclaim | NW-054/BAR-106 | Software Architect + Reality Checker | M4/M6 | Security/ops decision + tests | Stop if local deletion/redaction is improvised. |
| Reporting/import-export | TBD by PM | NW-044 needed; S26 inputs only | NW-033/S26 runtime inputs | Aggregate access/export overclaim | NW-044 | PM + Software Architect | M6 | Decision + report model | Stop if dashboard/API/export is built before route. |
| Conflict review UX | TBD by PM/UX | Single-flag resolver equality accepted; steward eligibility not productized | BAR-006/BAR-013 + scenario probes | Resolver/automation drift and role-name fallback drift | GAP-CONFLICT-03 for steward eligibility; NW-045 for batch/automation | UX + Software Architect | M5/M6 | UX route + tests | Stop if role-name substrings become product authority, or if auto-resolution/reassignment appears. |
| Subject/query/custom scope | TBD by PM/UX/Software Architect | Current fixed scope axes only; BAR-108 future decision | NW-053/BAR-108 route, no accepted new mechanism | Hidden scope expansion | NW-053/BAR-108 | Software Architect + Reality Checker | M6 if needed | Decision + security tests | Stop if query/custom scope appears through reporting, audit, admin, or review UX. |
| Ops readiness | TBD by Project Shepherd | Operator-deployable-with-constraints | Missing runbook/rehearsal evidence | Turnkey claim without ops | Ops runbook route | Project Shepherd + Test Results Analyzer | M4 | Rehearsal/checklist | Stop if dev compose/admin console is treated as production hardening. |

### Decision Log

| ID | Date | Decision | Role owner | Evidence | Follow-up |
|---|---|---|---|---|---|
| D-001 | 2026-06-12 | Workshop will be managed by Workshop Lead / Delivery Evidence Facilitator, not the architecture steward. | Workshop Lead | User direction + this control file | Keep steward scoped to authority and stop conditions. |
| D-002 | 2026-06-12 | Software Architect may carry Architecture Steward accountability when splitting the role would create friction. | Workshop Lead | User direction + Stage 1 packet | Keep Stage 1 standing output separate from Stage 4 system-boundary output. |
| D-003 | 2026-06-12 | Subject/query/custom scope is added as an explicit risk lane. | Software Architect carrying steward accountability | Stage 1 advice; NW-053/BAR-108 | Track during reporting, auditor, admin, and conflict-review planning. |
| D-004 | 2026-06-12 | Every dispatched worker must receive an explicit role boundary and must not inherit stewardship from repo context. | Workshop Lead | User direction after Stage 1/2 dispatch | Use the role-packets file for future dispatches. |
| D-005 | 2026-06-12 | Stage 3 UX is accepted as a facilitation draft after UX worker usage-limit interruption. | Workshop Lead | Stage 3 packet | Stage 4 Software Architect must map UX states to accepted constructs or successor routes. |
| D-006 | 2026-06-12 | Stage 4 Software Architecture is complete and routes Candidate 1 through existing kernel constructs only. | Software Architect | Stage 4 packet | Stage 5 Mobile App Builder must pressure-test current mobile feasibility and test targets. |
| D-007 | 2026-06-12 | Stage 5 Mobile is complete and classifies Candidate 1 mobile as feasible over accepted kernel but product-surface-partial. | Mobile App Builder | Stage 5 packet | Stage 6 must pressure-test mobile evidence and production-readiness wording. |
| D-008 | 2026-06-12 | Stage 6 pressure test is complete; evidence is sufficient for delivery planning but not implementation or production claims. | Reality Checker and Test Results Analyzer | Stage 6 packet | Stage 7 Project Shepherd must plan lanes, gates, owners, dependencies, and S06 timing. |
| D-009 | 2026-06-12 | Stage 7 delivery planning is complete; Candidate 1 remains conditional-go for product/spec validation and no-go for implementation until Stage 8 packet gates exist. | Project Shepherd | Stage 7 packet | Workshop Lead produces Stage 8 task-packet backlog. |
| D-010 | 2026-06-12 | Stage 8 task-packet backlog is complete; the immediate next non-implementation action is FD-PKT-001 S06 timing decision. | Workshop Lead | Stage 8 packet | Do not dispatch implementation until individual packets are written and gated. |

### Unresolved Input Register

| Input | Needed by | Current status | Owner role | Next action |
|---|---|---|---|---|
| First-deployment target users and outcome | Stage 2 | Received Stage 2 draft | Product Manager | Feed into UX Architect journey/vocabulary work. |
| UX journey/state language | Stage 3 | Complete as facilitation draft | Workshop Lead / future UX Architect | Future UX Architect may refine; Stage 4 can proceed now. |
| Technical dependency map | Stage 4 | Complete | Software Architect | Feed into Mobile App Builder feasibility work. |
| Mobile feasibility and test targets | Stage 5 | Complete | Mobile App Builder | Feed into Reality Checker and Test Results Analyzer. |
| Claim/evidence matrix | Stage 6 | Complete | Reality Checker and Test Results Analyzer | Feed into Project Shepherd delivery planning. |
| Milestone roadmap and decision calendar | Stage 7 | Complete | Project Shepherd | Feed into Stage 8 task-packet backlog. |
| Task-packet backlog | Stage 8 | Complete | Workshop Lead | Next action is FD-PKT-001 S06 timing decision. |

## Go / No-Go For Full Workshop

Current result: `workshop_planning_complete`, not yet `go_for_implementation`.

Reason:

- Stage 1 standing and Stage 2 product outcome are complete.
- The first Stage 3 UX worker dispatch failed due usage limit before producing
  a packet; Stage 3 has now been completed as a Workshop Lead UX facilitation
  draft.
- Stage 4 software boundary mapping is complete.
- Stage 5 mobile feasibility is complete.
- Stage 6 QA/reality pressure testing is complete.
- Stage 7 delivery planning is complete.
- Stage 8 task-packet backlog is complete.
- Candidate 1 implementation remains blocked until individual packets are
  written, reviewed against the packet gate, and explicitly dispatched.
- The next correct action is FD-PKT-001 S06 timing decision, not
  implementation.

The workshop becomes ready for delivery planning only after Stages 1 through 6
produce their outputs and the lane control table is updated.

## Next Action

Remaining workshop lead action:

1. Prepare FD-PKT-001 S06 timing decision packet when the user is ready.

After each role output, the Workshop Lead updates this control file or a
successor workshop record with accepted outputs, unresolved inputs, lane
statuses, and next-role prompts.
