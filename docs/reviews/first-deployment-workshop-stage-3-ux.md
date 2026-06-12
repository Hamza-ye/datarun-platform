# First Deployment Workshop Stage 3 UX Packet

Status: workshop-stage-output

Date: 2026-06-12

Role: UX Facilitation draft produced by Workshop Lead / Delivery Evidence
Facilitator after the Stage 3 UX worker dispatch failed due usage limit.

Authority: none. This packet is UX/product evidence for workshop synthesis. It
does not change CDL, contracts, BAR, NW, status, schemas, APIs, code, or
implementation authority. A future UX Architect can amend or replace it, but
until then it is sufficient to unblock Stage 4 system-boundary mapping.

## 1. UX Role Boundary

This Stage 3 packet decides how Candidate 1 should be understood, named, and
validated from a user-experience perspective.

It does decide:

- user journey shape for Candidate 1;
- product vocabulary candidates;
- state and recovery language that users must understand;
- UX risks and validation artifacts;
- questions for Product Manager, Software Architect, and Mobile App Builder.

It does not decide:

- platform vocabulary;
- architecture authority;
- event envelope fields or event types;
- scope, auth, resolver, retention, reporting, or entity-lifecycle semantics;
- implementation authorization.

Product terms in this packet are screen and validation language only. They must
map to accepted constructs or route to successor decisions before
implementation.

Operational/persona labels are also screen and validation language only. A
person may act as setup owner, field recorder, reviewer, operator, support, or
audit participant in different contexts if current authority allows it. UX work
must describe the backing authority as actor + active assignment + role + scope
+ time + activity/context before any persona label reaches a task packet.

## 2. Candidate 1 Journey Map

| Journey step | User goal | Candidate UX behavior | Current backing | UX validation need |
|---|---|---|---|---|
| Setup | Coordinator defines what field teams should collect. | Use product terms such as form, checklist, assigned work, warning, required field, and publish. Avoid exposing config-package internals. | BAR-010, BAR-011, NW-032/S23. | Can coordinators explain setup without architecture vocabulary? |
| Assignment | Coordinator gives responsibility to the right people. | Present responsibility as route, area, work list, or assigned work depending on domain. Show effective scope without implying IdP group authority. | BAR-007, NW-050, NW-042/S22. | Do users understand who can create/end work and why a record appears? |
| Work list | Field user sees assigned work. | Group by practical work context: place, subject/known thing, activity, due/attention state where available. | Assignment-derived access, mobile local projections, scoped sync. | Does the list help workers decide what to do next offline? |
| Capture | Field user records configured information. | Use record, form, checklist, save, and submit/sync language. Preserve the idea that records are appended, not overwritten. | BAR-001, BAR-002, NW-026/S00. | Can users complete standalone capture without needing platform terms? |
| Optional subject link | Field user links a record to a known thing when available. | Use known thing, person, household, item, location, or domain label. Missing item should keep work moving without implying canonical registry creation. | BAR-009 alias/subject support; S06/BAR-105 routed lane. | Can users distinguish linking an existing thing from creating lifecycle truth? |
| Offline save | Field user saves work without connectivity. | State should say saved on this device or saved locally. It must not imply supervisor has seen it. | BAR-003, BAR-008, NW-025/S19. | Do users trust local save without mistaking it for server sync? |
| Sync | Field user or app sends saved work later. | State should distinguish waiting to sync, syncing, synced, failed to sync, and synced with issue. | BAR-003, BAR-104, NW-055. | Can users recover from failed sync and know when to ask for help? |
| Correction | User appends a correction or updated observation. | Use correction or updated record language while showing original record remains part of history. | BAR-002, NW-026/S00. | Do users understand correction does not erase prior data? |
| Review / unresolved issue | Supervisor sees work that needs attention. | Use needs review, issue, duplicate, stale access, or discrepancy. Avoid implying auto-resolution or non-designated resolution. | BAR-006, BAR-013, NW-029/S21, NW-033/S26. | Can reviewers tell what needs action, who can act, and what remains unresolved? |
| Freshness | Supervisor interprets latest synced view. | Show last synced, latest known, last updated, and unresolved issues. Avoid live-truth wording. | NW-033/S26, sync watermarks/projection timestamps. | Can supervisors distinguish latest synced state from live field reality? |
| Access ended | User no longer has active responsibility. | Explain access ended or no longer assigned without deleting central truth. Local retained/expired behavior is not decided in Candidate 1. | Assignment history; NW-054/BAR-106 for retention/security. | Do users know why work disappeared or became read-only/attention-only? |

## 3. Product Vocabulary And State Language

| Product term | Safe user-facing meaning | Backing construct | Avoid saying / implying |
|---|---|---|---|
| Record | A submitted or saved activity entry. | Event payload under a shape. | Mutable row, envelope, event type. |
| Form / checklist | What the user fills out. | Deployer shape and activity config. | Platform payload schema or code. |
| Assigned work | Work visible because the user is responsible for it. | Assignment-derived access/sync scope. | IdP group, UI-only permission, custom query scope. |
| Known thing | Something a record can refer to. | Existing subject ref / alias projection where available. | Full entity lifecycle, active/inactive registry state. |
| Saved locally | Work is stored on this device. | Mobile local event store/pending push. | Server received it, supervisor can see it. |
| Waiting to sync | Work is ready to send when conditions allow. | Pending push/sync state. | Guaranteed accepted by server. |
| Synced | Server has received the work for the resolved actor. | Successful bearer-bound push. | Reviewed, approved, conflict-free, globally complete. |
| Failed to sync | Work did not reach server yet. | Sync error/pending local data. | Data lost. |
| Needs review | A record/issue requires human attention. | Flag, review event, resolver route, projection exclusion. | Auto-resolution, resolver reassignment, hard rejection. |
| Issue / attention item | Something may need correction, review, or explanation. | Server flag or mobile advisory warning. | Mobile canonical flag when only advisory. |
| Latest synced view | The newest server-side view available to this user. | Projection/sync metadata. | Live field truth. |
| Correction | A new entry that clarifies or supersedes prior information. | Append-only event/correction pattern. | Editing history in place. |
| Access ended | The user no longer has current responsibility/scope. | Assignment end/current assignment resolution. | Local deletion policy or no-local-retention behavior. |

Terms to avoid in user-facing Candidate 1 copy unless translated:

- envelope;
- projection;
- sync watermark;
- subject-history cursor;
- shape ref;
- resolver equality;
- temporal authority;
- actor partition;
- config package.

Terms that may appear in support/admin documentation with explanation:

- assignment;
- sync;
- subject;
- flag;
- actor;
- principal binding.

## 4. Critical Error / Recovery States

| State | User-facing explanation | UX rule | Routed caveat |
|---|---|---|---|
| Failed sync | "This work is still saved on this device. It has not reached the server." | Give retry and support path; do not imply loss. | Mobile/offline implementation must preserve actor/session boundaries. |
| Stale access | "You saved this when your access may have changed. It was kept and marked for review." | Explain accept-and-flag without blaming the user. | Do not turn stale access into mobile rejection. |
| Missing subject / known thing | "The thing is not in your list. Save the record as needing matching or review." | Keep capture moving; visually distinguish unlinked/candidate item. | Must not silently create canonical lifecycle state; S06/BAR-105 route. |
| Duplicate/conflict | "This may match existing work or needs a reviewer decision." | Show review path and source context. | No batch/auto-resolution without NW-045/BAR-102/BAR-103. |
| Access ended | "You are no longer assigned to this work." | Explain why work is unavailable or no longer current. | Retention, redaction, local expiry, and sealed recovery route through NW-054/BAR-106. |
| Shared-device switch | "Switching user keeps each person's saved work separate." | Make current user visible; warn before switching with pending work. | Mobile must not create local actor authority; use server-resolved actor sessions. |
| Latest synced view uncertainty | "This is the latest synced information, not necessarily live field reality." | Show timestamp/freshness and unresolved issue counts. | Production reporting/dashboard route remains NW-044. |
| Review not allowed | "You can view this, but another responsible reviewer must resolve it." | Explain action availability without exposing internals. | Preserve exact designated-resolver semantics. |

## 5. UX Risks And Validation Needs

| Risk | Why it matters | Validation approach |
|---|---|---|
| Users misunderstand offline states | Loss of trust or duplicate work. | Walkthrough with no network, failed sync, retry, and eventual sync. |
| Subject-linked capture becomes lifecycle expectation | Candidate 1 may accidentally absorb S06. | Test missing-known-thing language with users and product owner. |
| Supervisors overtrust latest synced view | Bad decisions if field reality has moved on. | Validate freshness labels and unresolved issue treatment. |
| Setup owner sees config as too technical | Candidate 1 product fit fails despite accepted kernel. | Prototype setup flow and run coordinator comprehension test. |
| Review ownership is unclear | Work needing attention may stall. | Validate needs-review, issue, duplicate, and responsible reviewer copy. |
| Shared-device identity is unclear | Wrong-person work risk. | Validate current-user banner, switch flow, pending-work warning. |
| Access ended feels like data loss | Support load and trust risk. | Validate access-ended explanation and support path. |
| Product language leaks into architecture | Future agents may implement labels as primitives. | Keep architecture backing column in every UX spec/task packet. |
| Persona labels become fixed product modules | The product may overbuild coordinator, supervisor, auditor, or field-worker-specific surfaces instead of deriving surfaces from current authority. | Validate labels as acting contexts and require authority backing in task packets. |

## 6. UX Evidence Artifacts Needed Before Implementation

- Candidate 1 journey walkthrough for people acting in setup, field execution,
  review, operator/admin, and support contexts.
- Low-fidelity setup, work list, capture, sync state, correction, and review
  flow screens.
- Vocabulary test for record/form/checklist, assigned work, known thing,
  saved locally, waiting to sync, synced, failed, needs review, access ended,
  latest synced view.
- Offline and failed-sync scenario walkthrough.
- Missing-subject / unlinked-known-thing walkthrough.
- Supervisor freshness and unresolved-issue interpretation walkthrough.
- Shared-device switch walkthrough if shared devices are in first deployment.
- UX caveat list mapped to routed lanes so implementers do not infer missing
  architecture.
- Accessibility and localization checklist for claimed production surfaces.

## 7. Questions For Product Manager

- Which domain labels should replace generic known thing in the first
  deployment, if any?
- Is subject-linked capture mandatory for Candidate 1, or optional in the
  first deployment promise?
- Which statuses do users already understand: saved, submitted, uploaded,
  synced, reviewed, approved, issue, correction?
- Does first deployment require shared devices, or is it a visible near-future
  lane?
- Does first deployment require maintained known things beyond lookup/linking?
  If yes, S06/BAR-105 may need an earlier milestone.
- Which review cases matter most: stale access, duplicate, correction,
  discrepancy, or unauthorized review?
- What support path is realistic when sync fails or access ends?

## 8. Questions For Software Architect And Mobile App Builder

Software Architect:

- What view-model/API shape can support Candidate 1 screens without creating a
  new contract too early?
- Which UX states are already derivable from accepted constructs, and which
  need successor routes?
- How should unlinked/missing-subject capture be represented without canonical
  lifecycle state?
- Where must admin/auth constraints appear in UX task packets?
- What is the safe minimum for freshness and unresolved issue display before
  NW-044 reporting work?

Mobile App Builder:

- Which local states are available today for saved locally, waiting to sync,
  synced, failed, stale/access issue, and current actor?
- What pending-work warning is feasible during shared-device switching?
- What manual mobile walkthroughs should QA use for offline save/sync and
  actor switching?
- What mobile screens currently exist and where are product UX gaps biggest?
- What must wait for mobile OIDC/token lifecycle routing?

## 9. Advice To Workshop Lead

Stage 4 Software Architect must preserve these UX boundaries:

- UX state language is presentation language, not platform state.
- Candidate 1 may include optional subject-linked capture, but not canonical
  entity lifecycle unless FD-PKT-001 moves S06/BAR-105 before implementation
  planning.
- Missing-subject capture must stay unpromoted/candidate/review-oriented until
  S06/BAR-105 is routed.
- Freshness and latest synced view must avoid live-truth claims.
- Needs review and attention item must preserve exact resolver and
  accept-and-flag semantics.
- Any UX need for reporting dashboards, broad audit views, custom filters, or
  aggregate drill-down should route through NW-044/NW-053, not slip into
  Candidate 1.

Stage 5 Mobile App Builder must preserve:

- mobile warnings are advisory unless server accepted behavior says otherwise;
- local save is not server sync;
- actor/session identity must be visible enough to avoid wrong-person work;
- failed sync must give recovery without implying data loss;
- access-ended and retention behavior must not be invented in mobile UX.

Workshop control recommendation:

- Mark Stage 3 complete as a facilitation draft.
- Ask Stage 4 Software Architect to identify which UX states can be backed by
  current code/contracts and which need task-packet caveats or successor
  routes.
- Require FD-PKT-001 before Candidate 1 packet freeze if the product promise
  depends on subject-linked or missing-known-thing behavior.
- Keep UX Architect review as a future refinement item if a dedicated UX role
  becomes available again.
