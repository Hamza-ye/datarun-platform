# FD-PKT-002 Candidate 1 Product/Spec And UX Validation

Status: prepared task-packet draft

Date: 2026-06-13

Authority: none. This packet prepares product/spec and UX validation. It does
not authorize implementation, change CDL, BAR, NW, contracts, schemas, APIs,
runtime behavior, event vocabulary, or product scope by itself.

## 1. Header

Packet ID: FD-PKT-002

Lane: Candidate 1 product/spec and UX validation

Assigned role: Product Manager + UX owner

Claim status: `conditional-go` for product/spec and UX validation only. Candidate 1 implementation remains `no-go`.

Objective: define the bounded Candidate 1 product promise, UX vocabulary, validation questions, walkthrough requirements, acceptance criteria, evidence handoff, exclusions, and Option C S06 dependency marker for the first-deployment workshop.

Authority and source order:

1. CDL/contracts remain authority where applicable. This packet does not open, change, or supersede contracts.
2. `AGENTS.md` and `docs/status.md` Current Routing define current entry/routing posture.
3. `docs/agent-working-surface/first-deployment-task-packet-router.md` controls first-deployment packet sequencing.
4. `docs/agent-working-surface/operational-ux-layering-companion.md` supplies non-authoritative UX/product vocabulary guardrails.
5. Workshop stage files and FD-PKT-001 are planning and evidence context only.
6. Scenario/user-fit packets are product/problem evidence only.
7. BAR/NW standing is accepted only where carried through the routed status/workshop sources; this packet does not alter BAR/NW.

Allowed files/contracts:

- Allowed write: this file only.
- Allowed source context: the files named in the FD-PKT-002 dispatch.
- Allowed contract stance: reference current envelope, sync, flag, deployer shape/config package, platform payload, pattern, and fixture boundaries only as existing guardrails. No contract edits, new schema rows, new event vocabulary, or implementation authorization.

Commit boundary: docs-only packet creation. Do not edit code, router/status/backlog files, contracts, schemas, APIs, or runtime behavior. Do not commit.

## 2. Role Boundary

Product Manager + UX owner decides:

- Candidate 1 product outcome, promise, and non-goals.
- Candidate 1 included flows and visible excluded lanes.
- Product vocabulary and UX state language for validation.
- Acting-context validation questions for setup owner, field user, supervisor/reviewer, and operator/support contexts.
- Minimum journey walkthroughs and UX/product validation done definition.
- Evidence handoff expectations for FD-PKT-003.

Product Manager + UX owner does not decide:

- Architecture primitives, event envelope fields, event types, scope mechanisms, runtime authority, resolver semantics, contracts, schemas, APIs, durable workflow state, or implementation shape.
- S06/entity lifecycle authorization, registry stewardship, discovered-unit lifecycle, canonical active/inactive/retired state, or merge/split UX.
- Production web admin auth, mobile OIDC/Keycloak login, token lifecycle, retention/security, reporting/export/import, conflict automation, resolver reassignment, auto-resolution, custom/query scope, or ops readiness.
- Implementation tasks, files to edit, test commands, release approval, or commits.

## 3. Candidate 1 Product Promise

Candidate 1 promises a basic operational capture path for first deployment: a setup owner can frame assigned work; a field user can see assigned work, capture a record, save locally when offline, sync later, append a correction, and optionally link a record to a known thing; a supervisor/reviewer can interpret latest synced information and needs-review signals without mistaking them for live truth or lifecycle truth.

Candidate 1 remains S01-compatible. Subject-linked capture and missing-known-thing handling are allowed only as linked/unlinked/candidate capture language over current constructs, not as canonical entity lifecycle.

Non-goals:

- No implementation dispatch or product-readiness claim.
- No canonical known-thing registry lifecycle, discovered-unit lifecycle, active/inactive/retired state truth, registry stewardship, or merge/split UX.
- No production admin/mobile auth, retention/security, reporting/export/import, conflict automation, custom scope, or ops-readiness claim.
- No new platform vocabulary, contracts, schema fields, envelope fields, event types, scope mechanisms, or durable workflow state.

## 4. Candidate 1 Scope

Included Candidate 1 validation flows:

- Setup comprehension: setup owner can describe the form/checklist/activity, required fields, publish expectation, and responsibility model without architecture vocabulary.
- Assigned work: field user understands why work appears and what responsibility or scope it represents.
- Standalone capture: field user can create a basic record without a subject link.
- Optional subject-linked capture: field user can link a record to an existing known thing using domain language.
- Missing-known-thing capture: field user can continue with an unlinked/candidate record for review without creating registry truth.
- Local save and offline confidence: field user understands saved on this device, waiting to sync, syncing, synced, synced with issue, and failed to sync.
- Sync failure recovery: field user understands the work is preserved locally and what recovery/support path exists.
- Append-only correction: user understands a correction adds a new record/update and does not erase prior history.
- Needs review and unresolved issue visibility: supervisor/reviewer can see what needs attention without auto-resolution or resolver reassignment.
- Latest synced view: supervisor/reviewer can distinguish latest synced information from live field reality.
- Access ended/stale access explanation: user understands no longer assigned/access changed language without interpreting it as deletion or rejection.
- Shared-device switch language only if first deployment claims shared devices; detailed mobile/offline evidence belongs to FD-PKT-004.

Explicitly excluded successor lanes:

- FD-PKT-101: S06/entity lifecycle, maintained known things, discovered-unit lifecycle, registry stewardship, lifecycle words, duplicate stewardship, merge/split UX.
- FD-PKT-102 and FD-PKT-103: production web admin auth, admin authority, mobile OIDC/Keycloak login, token lifecycle, online binding-admin UI/API.
- FD-PKT-104: retention/security/device lifecycle, expiry, decommissioning, sealed recovery, local encryption, token/session retention, redaction/no-local-retention.
- FD-PKT-105: reporting dashboards/APIs/export/import, broad audit/history, aggregate access, durable report contracts.
- FD-PKT-106: conflict review queues, batch handling, pending-match derivations, conflict automation, resolver reassignment, auto-resolution.
- FD-PKT-107: subject/query/custom scope, auditor/report filters as authority, special read/write bypasses.
- FD-PKT-108: ops readiness and constrained deployment claims.

## 5. Option C S06 Dependency Marker

FD-PKT-001 selected Option C: Candidate 1 product/spec and UX validation may proceed while S06 discovery runs in parallel, but Candidate 1 implementation must not dispatch until the S06 dependency is answered.

Candidate 1 can proceed now because its promise is S01-compatible if:

- subject-linked capture means linking a record to an existing known thing;
- missing-known-thing means unlinked/candidate evidence for review;
- candidate language does not become registry creation;
- latest synced, needs review, duplicate suspected, and correction remain review-oriented UX language;
- lifecycle terms such as active, inactive, retired, closed, moved, verified, merged, and split do not become day-one truth.

Implementation remains blocked on:

- FD-PKT-101 answering what a known thing is for first deployment, where the known set comes from, whether field discovery creates candidate evidence or registry items, whether lifecycle words are required, who stewards duplicates/merge/split/closure, and which contracts/code/tests would be touched if S06 is promoted.
- FD-PKT-003 translating this packet into evidence gates.
- FD-PKT-004 defining mobile/offline validation.
- FD-PKT-005 assessing whether adapter-level view composition is enough or a routed shared view-model/contract decision is needed.

Stop if Candidate 1 cannot honestly stay S01-compatible without S06 product decisions.

## 6. Product Vocabulary And UX State Language

Operational labels are acting contexts only. They are not actor identity categories, fixed roles, authority primitives, modules, config namespaces, product-area boundaries, or implementation service boundaries. Every validation artifact that uses setup owner, field user, supervisor/reviewer, operator/support, auditor, or similar labels must map them as:

```txt
actor + active assignment + role + scope + time + activity/context
-> available actions and visible data
-> projected operational surface
```

Authority backing for acting contexts:

| Acting context | Product meaning | Authority backing to state in validation |
|---|---|---|
| Setup owner | Person preparing the first-deployment setup, forms/checklists, responsibility model, and publish handoff. | Actor plus current assignment/authorized setup or assignment-admin context. If assignment create/end is in scope, keep it inside the platform-owned assignment-admin command boundary; do not promote it into activity role-actions. Production admin auth remains excluded. |
| Field user | Person doing assigned capture work, often offline. | Actor plus active assignment, role-action allowance, scope, time, and activity/context. Visibility and actions derive from current assignment/scope, not IdP groups or UI labels. |
| Supervisor/reviewer | Person inspecting latest synced work, issues, corrections, freshness, and allowed review actions. | Actor plus active assignment, review/resolution role where applicable, scope, time, activity/context, and exact designated-resolver semantics for resolution. Role-name substrings are not authority. |
| Operator/support | Person helping with setup, credentials, sync failure, access-ended explanation, or deployment operation. | Actor plus assigned/support/operational authority for the specific context. Support language must not imply broad data access, sealed-partition recovery, production admin authority, or security/retention behavior. |

Candidate 1 vocabulary:

| Surface/state | User-facing language to validate | Meaning for users | Current backing to cite | Must not imply |
|---|---|---|---|---|
| Setup | Setup, form, checklist, activity, publish | What teams will collect and when it becomes available. | Deployer shape/activity/config package and assignment setup boundaries. | Config internals, deployer scripts, production admin auth, or custom scope. |
| Assigned work | Assigned work, route, area, work list, responsibility | Work visible because the user is currently responsible for it. | Assignment-derived access and sync scope. | IdP group authority, UI-only permission, query/custom scope, or hard persona module. |
| Capture | Record, form entry, checklist entry | A user-authored activity entry saved or synced. | Append-only event payload under current shape/activity config. | Mutable row, envelope vocabulary, event type, in-place edit. |
| Subject link | Known thing, linked record, or domain label such as site, household, asset, facility, person | The record refers to a recognizable existing thing. | Existing subject reference/alias/projection support where available. | Full registry lifecycle, active/inactive state, verified registry truth. |
| Missing known thing | Not in the list, unlinked record, candidate for review, needs matching | Work can continue when the thing is missing or stale locally. | Unpromoted candidate/capture evidence and review-oriented handling; S06 dependency remains open. | Canonical registry creation, discovered-unit lifecycle, automatic match/merge. |
| Local save | Saved on this device, saved locally | Work is preserved locally and can sync later. | Mobile local event store/pending push. | Server received it, supervisor can see it, reviewed, approved, or conflict-free. |
| Sync | Waiting to sync, syncing, synced, synced with issue | Work is queued, being sent, or received by the server; issues may still need review. | Pending push, bearer-bound push/pull, flags/projections/sync metadata. | Approved, globally complete, live truth, or reviewed. |
| Failure | Failed to sync, still saved on this device | Work has not reached the server yet, but is not lost. | Sync error plus preserved pending local data. | Data loss, rejection, or local authority over server acceptance. |
| Correction | Correction, update, amended record | A new entry clarifies or supersedes prior information. | Append-only event/correction behavior and projections. | Erasing original history or editing records in place. |
| Latest synced | Latest synced view, last synced, latest known here | The newest server-side/projected information available to this user. | Sync/projection timestamps and scoped pull metadata. | Live field reality, complete reporting truth, or supervisor approval. |
| Needs review | Needs review, issue, attention item, duplicate suspected | A record/issue needs human inspection or allowed resolution. | Flags, review events, resolver route, projections. | Hard rejection, auto-resolution, resolver reassignment, batch bypass. |

## 7. Validation Questions

### Setup Owner Acting Context

- What first-deployment outcome would make Candidate 1 worth using before any successor lanes ship?
- What do setup owners call the setup artifact: form, checklist, activity, register, campaign, workflow, report, or something domain-specific?
- What do setup owners need to configure or approve before field use, and which parts are too technical today?
- Where does the initial known set come from: operator import, setup-owner entry, external registry, field discovery, or a mixed process?
- What setup mistakes are common enough that Candidate 1 must prevent or explain?
- Who grants, changes, or ends responsibility, and how do setup owners explain why work appears for a field user?
- Which lifecycle or registry words are already used operationally, and do any of them need FD-PKT-101 before Candidate 1 implementation?

### Field User Acting Context

- What do field users call a record and the thing they are recording about?
- Can field users complete standalone capture without understanding architecture terms?
- How do field users identify the right known thing: name, code, QR/barcode, geography, map, list, local knowledge, or another cue?
- What minimum confirmation details prevent wrong-subject selection?
- What should happen when the known thing is missing or stale while offline?
- Do field users understand saved locally, waiting to sync, synced, synced with issue, failed to sync, reviewed, and approved as different states?
- What wording gives confidence after failed sync without implying the server has received the work?
- What do field users call a correction: update, fix, amend, resubmit, replace, cancel, or something else?
- Which warnings should advise but not block saving?

### Supervisor/Reviewer Acting Context

- Can supervisors distinguish latest synced information from live field reality?
- Which freshness indicator changes supervisor decisions?
- What does needs review mean in the domain: duplicate, stale access, wrong link, missing data, discrepancy, unauthorized review, or another issue?
- What information does a reviewer need before acting on a duplicate suspected or wrong-link issue?
- Who is allowed to resolve which issue, and how should the UI explain view-only versus resolution authority?
- Can supervisors understand that corrections preserve original history?
- Which S06 examples must be collected now even if Candidate 1 excludes merge/split and lifecycle UX?

### Operator/Support Acting Context

- What support path is realistic when sync fails, a token is invalid, access has ended, or pending work remains on a shared device?
- What language should support use to explain saved locally versus synced without promising data recovery or production retention behavior?
- What setup, assignment, credential, and config-publish checks must support understand before a first deployment walkthrough?
- Which operator actions require separate production auth/admin/mobile login or ops-readiness lanes before product claims?
- What evidence would make support comfortable that Candidate 1 is operator-deployable with constraints, not turnkey production-ready?

## 8. Journey Walkthrough Requirements

Minimum walkthroughs before Candidate 1 implementation dispatch:

- Setup walkthrough: setup owner explains setup artifact, assigned work, publish handoff, and responsibility model without architecture vocabulary.
- Standalone capture walkthrough: field user opens assigned work, records a basic entry, saves locally offline, syncs later, and sees the resulting state language.
- Subject-linked capture walkthrough: field user finds and confirms a known thing, records against it, and later sees the linked record in latest synced context.
- Missing-known-thing walkthrough: field user cannot find the thing, saves an unlinked/candidate record, and understands it needs matching/review without becoming registry truth.
- Failed sync recovery walkthrough: field user sees failed to sync, understands work is still saved locally, retries or follows support path, and does not mistake failure for data loss.
- Correction walkthrough: user appends a correction/update and understands the original record remains traceable.
- Supervisor freshness walkthrough: supervisor/reviewer reads latest synced view, unresolved issue counts/signals, and timestamps without treating them as live field truth.
- Needs-review walkthrough: supervisor/reviewer sees an issue and understands what action is available, what remains unresolved, and why non-designated resolution is unavailable.
- Access-ended/stale-access walkthrough: user understands no longer assigned/access changed language and that valid saved work may be kept and marked for review.
- Shared-device walkthrough if claimed: switch from actor A to actor B with pending work warning/isolation, then resume safely, without implying retention/security behavior.
- Operator/support recovery walkthrough: invalid token, setup/connect problem, sync failure, and access-ended support explanation using constrained-deployment language.

Each walkthrough must record: acting context, authority mapping, scenario script, exact vocabulary tested, participant/SME feedback, comprehension risks, S06/successor-lane triggers, and pass/revise/route outcome.

## 9. Acceptance Criteria For FD-PKT-002

FD-PKT-002 is done when product/spec and UX validation, not implementation, has the following:

1. Candidate 1 product promise, non-goals, included flows, and excluded successor lanes are documented and remain S01-compatible.
2. Option C S06 dependency marker is explicit, with FD-PKT-101 questions and implementation blockers named.
3. Product vocabulary and UX state language are validated or marked with specific unanswered questions, and no user-facing term becomes platform vocabulary or authority.
4. Acting-context labels are backed by actor + active assignment + role + scope + time + activity/context in validation notes.
5. Setup owner, field user, supervisor/reviewer, and operator/support questions are answered or carried forward with owner, evidence needed, and decision route.
6. Minimum journey walkthroughs have scripts and expected evidence; completed walkthroughs record comprehension result, remaining risk, and route.
7. S06-sensitive language is tested for candidate versus canonical known-thing understanding.
8. Latest synced, saved locally, waiting to sync, synced, failed to sync, needs review, access ended, and correction are distinguishable to users.
9. Downstream FD-PKT-003 evidence needs are concrete enough to convert into tests, probes, manual walkthroughs, ops checks, and release gates.
10. No implementation tasks, code files, contract changes, production claims, or commits are authorized by this packet.

## 10. Evidence Needed For FD-PKT-003

FD-PKT-003 should convert this packet into evidence design in these buckets:

Automated tests:

- Existing kernel coverage mapping for append-only capture/correction, config/package behavior, assignment-derived scope, sync, flags, projections, and auth boundaries.
- Candidate UI/copy tests or review checks proving banned lifecycle words and production-readiness claims are absent from Candidate 1 surfaces.
- Mobile/widget/integration targets for setup success/failure, offline save, pending preservation, sync failure, unauthorized/actor-drift recovery, advisory non-blocking save, append-only correction, work-list states, and shared-device actor isolation where claimed.

Scenario probes:

- Reuse S00/S19/S21/S23 where they support Candidate 1.
- Add a bounded end-to-end Candidate 1 probe: setup, assignment, standalone capture, optional subject link, missing-known-thing candidate capture, offline local save, sync, correction, unresolved issue visibility, and latest-synced supervisor interpretation.
- Add an S06 gate probe or evidence checklist showing users do not mistake candidate/unlinked capture for registry lifecycle truth.

Manual walkthroughs:

- Convert the walkthroughs in section 8 into facilitator scripts, expected observations, pass/fail/reroute criteria, and evidence artifacts.
- Include vocabulary comprehension for setup, assigned work, capture, known thing, missing known thing, saved locally, waiting to sync, synced, failed to sync, correction, latest synced, and needs review.

Ops checks:

- Confirm operator/support recovery language for invalid token, setup/connect failure, sync failure, access ended, and pending work does not imply production auth, retention/security, or sealed recovery.
- Confirm setup/config publish, assignment bootstrap, and auth manifest handling are treated as constrained operator process, not turnkey product readiness.
- Carry ops hardening, backup/restore, TLS/secrets, monitoring, incident response, and deployment rehearsal to ops readiness packets rather than Candidate 1.

Release gates:

- Candidate 1 cannot move to implementation dispatch until FD-PKT-002 through FD-PKT-005 are gated and FD-PKT-101 resolves or explicitly excludes the S06 dependency.
- No lane is release-ready unless authority/routing, contract fit, product/UX validation, automated evidence, manual evidence, security/ops checks where relevant, and claim wording all pass.
- Stop gate for any lifecycle, custom scope, reporting/export, production auth/admin/mobile login, retention/security, conflict automation, resolver reassignment, auto-resolution, or production-readiness wording.

## 11. Forbidden Work

- Do not edit code, contracts, schemas, APIs, status, routers, backlogs, BAR/NW, CDL, or workshop control files.
- Do not create implementation tasks or authorize implementation.
- Do not commit.
- Do not create canonical entity lifecycle, active/inactive/retired state truth, discovered-unit lifecycle, registry stewardship, or merge/split UX.
- Do not add or imply new envelope fields, event types, scope mechanisms, durable workflow state, deployer-authored access logic, deployer-authored state machines, scripts, custom traversals, or expression-function vocabulary.
- Do not add or imply production auth/admin/mobile login, online binding-admin UI/API, token lifecycle, retention/security, reporting/export/import, broad audit/history access, aggregate access divergence, conflict automation, resolver reassignment, or auto-resolution.
- Do not make mobile warnings authoritative rejection.
- Do not make latest synced view live truth.
- Do not make missing-known-thing/candidate capture canonical registry truth.
- Do not make persona labels identity categories, authority primitives, config namespaces, fixed product modules, product-area boundaries, or implementation service boundaries.
- Do not hide S06 under vague later wording.

## 12. Stop And Report Conditions

Stop and report if:

- Candidate 1 copy, vocabulary, or walkthroughs require maintained known things, lifecycle state, discovered-unit stewardship, registry stewardship, duplicate stewardship, merge/split UX, or lifecycle words before FD-PKT-101 resolves S06.
- S06 is renamed as vague later work without owner, route, evidence need, and decision point.
- A user-facing term is being promoted into an event field, event type, scope mechanism, flag category, contract/schema field, durable workflow state, authority rule, or shared API meaning.
- Product validation needs a contract, code, schema, or runtime change before it can be honestly specified.
- Persona/operational labels harden into identity categories, fixed modules, access rules, config namespaces, product-area boundaries, or implementation service boundaries.
- Latest synced, needs review, saved locally, failed sync, or correction language implies live truth, rejection, approval, data loss, or history rewrite.
- Production-readiness, production auth/admin/mobile login, retention/security, reporting/export/import, conflict automation, resolver reassignment, auto-resolution, or custom scope enters Candidate 1.
- The active routing sources conflict with this packet.

## 13. Downstream Packet Impacts

| Packet | Impact |
|---|---|
| FD-PKT-003 | Convert this packet into evidence gates: automated tests, scenario probes, manual walkthroughs, ops checks, release gates, claim wording checks, and the S06 implementation stop gate. |
| FD-PKT-004 | Use this vocabulary and walkthrough list to define mobile/offline validation for setup/connect, local save, failed sync, pending preservation, shared-device switch, correction, and freshness. Keep standalone/missing-known-thing capture unlinked/candidate and avoid mobile auth/login or retention/security claims. |
| FD-PKT-005 | Assess whether Candidate 1 can use adapter-level view composition over existing events, projections, flags, sync metadata, and assignment scope. If multiple components need a stable shared shape, new contract, or S06 data model, route before implementation. |
| FD-PKT-101 | Open and answer S06/entity lifecycle discovery before Candidate 1 implementation dispatch. It must decide or explicitly exclude maintained known things, known-set source/authority, field discovery semantics, lifecycle vocabulary, registry stewardship, duplicate handling, merge/split expectations, and touched contracts/code/tests if promoted. |
