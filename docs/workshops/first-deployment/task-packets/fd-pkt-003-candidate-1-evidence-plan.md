# FD-PKT-003 Candidate 1 Evidence Plan

Status: prepared task-packet draft

Date: 2026-06-13

Authority: none. This packet classifies evidence, gates, test candidates,
manual walkthroughs, and release-readiness checks for Candidate 1. It does not
define product scope, change architecture authority, authorize implementation,
edit contracts, run tests, or approve release.

## 1. Header

Packet ID: FD-PKT-003

Lane: Candidate 1 evidence plan

Assigned role: Test Results Analyzer

Claim status: `conditional-go` for evidence design only. Candidate 1
implementation remains `no-go`.

Objective: convert Candidate 1 claims from FD-PKT-002 and S06 dependency
claims from FD-PKT-101 into evidence classes, automated test candidates,
manual walkthroughs, product/SME validation needs, release gates, and
downstream packet inputs without deciding or implementing Candidate 1 or S06.

Authority and source order:

1. CDL/contracts and active status remain binding authority. Current routing
   and contract guardrails take precedence over workshop language.
2. FD-PKT-001, FD-PKT-002, and FD-PKT-101 are packet inputs for this evidence
   plan.
3. Scenario/user-fit docs and workshop docs are product evidence context only,
   not architecture authority.
4. BAR/NW standing may be referenced only as current accepted/deferred/future
   standing, not expanded into new implementation scope.

Allowed files/contracts:

- Allowed write: this file only.
- Allowed source context: the files named in the FD-PKT-003 dispatch.
- Allowed contract stance: reference existing envelope, sync protocol, flag
  catalog, shape-format, config-package, platform payload shape, pattern
  definition, and shared fixture boundaries only as guardrails.
- No contract edits, schema edits, code edits, router/status/backlog edits,
  BAR/NW/CDL edits, test fixture edits, or runtime behavior changes.
- No server/mobile tests for this docs packet.

Commit boundary: docs-only packet creation. Do not commit.

## 2. Role Boundary

Test Results Analyzer may:

- classify claims into evidence classes;
- map existing evidence cited by routed sources;
- identify missing automated, manual, product, ops, and release evidence;
- define gates, stop conditions, and downstream evidence needs;
- mark no-go conditions where evidence or authority is missing.

Test Results Analyzer must not:

- decide product scope, S06 timing, architecture primitives, contract shape, or
  implementation design;
- authorize Candidate 1 implementation, S06 implementation, or release;
- erase Candidate 1 product need because evidence is thin;
- hide S06 as vague later work;
- expand BAR/NW standing into new behavior;
- treat persona labels as durable actor identity, access authority, modules,
  config namespaces, product-area boundaries, or service boundaries.

Operational labels such as setup owner, field user, supervisor/reviewer,
operator/support, and auditor are acting contexts only. Evidence artifacts must
map them as:

```txt
actor + active assignment + role + scope + time + activity/context
-> available actions and visible data
-> projected operational surface
```

## 3. Evidence Strategy Summary

Evidence gaps become visible evidence work, release gates, or routed decision
lanes. They are not reasons to erase Candidate 1 product need.

Candidate 1 remains S01-compatible. Its evidence plan can cover standalone
capture, optional subject-linked capture, unpromoted missing-known-thing or
candidate capture, offline local save, sync, append-only correction, latest
synced interpretation, needs-review visibility, and constrained
operator/support recovery.

S06 remains visible as an architecture/product decision dependency. FD-PKT-003
must require proof that users understand candidate evidence versus canonical
known-thing truth, but it must not decide or implement S06.

Evidence classes used in this packet:

| Evidence class | What it can support |
|---|---|
| Authority evidence | CDL/contracts/status/BAR/NW routing allows the claim or marks it deferred. |
| Contract evidence | Existing schemas, protocol docs, and parity boundaries remain compatible. |
| Code inspection evidence | Current implementation matches the claimed invariant. |
| Automated test evidence | Targeted server/mobile/regression tests pass for the touched boundary. |
| Scenario runtime probes | Accepted constructs can run named scenarios; this is not finished UX proof. |
| Manual walkthrough evidence | Realistic scripts show users can complete and interpret the workflow. |
| Product/SME validation | Deployment users and SMEs validate language, workflow, and acceptance. |
| Ops evidence | Runbooks and rehearsals cover setup, recovery, monitoring, and support where claimed. |
| Release evidence | Staging rehearsal, known-risk register, checklist signoff, and go/no-go exist. |
| Claim wording evidence | User-facing and packet wording avoids unsupported lifecycle, authority, and production claims. |

No statistical quality conclusion is made in this docs packet because no test
execution data is generated here. Downstream evidence runs should record
sample size, pass/fail count, environment, defects found, rerun history, and
residual risk so release claims can be assessed from data rather than
assertion.

## 4. Claim-to-Evidence Matrix

### Candidate 1 Claims From FD-PKT-002

| Claim | Current status from sources | Evidence classes required | Gate or stop condition |
|---|---|---|---|
| Candidate 1 is a basic operational capture path over the accepted kernel. | `conditional-go` for product/spec; implementation `no-go`. Kernel evidence is accepted, but UX/product evidence is partial. | Authority, contract, automated, scenario runtime, manual, product/SME, claim wording. | Stop if the claim becomes product-ready or implementation-ready before FD-PKT-002 through FD-PKT-005 are gated and FD-PKT-101 is resolved or explicitly excluded. |
| Setup owner can frame assigned work and publish expectation without architecture vocabulary. | Product validation needed. Existing config/package standing supports feasibility, not product comprehension. | Product/SME, manual walkthrough, ops evidence, claim wording. | Stop if setup language implies production admin auth, custom scope, deployer-authored access logic, or config internals as user-facing product truth. |
| Field user sees assigned work because of current responsibility/scope. | Assignment-derived access and sync/access scope are accepted standing. Product IA still needs validation. | Authority, contract, automated, manual, product/SME. | Stop if UI labels, IdP groups, claims, JWT `actor_id`, or persona names become access authority. |
| Standalone capture can create a basic record. | Candidate 1 may include basic standalone capture. | Contract, automated, scenario runtime, manual, product/SME. | Stop if record/capture wording creates a new envelope field, event type, mutable row model, or in-place edit expectation. |
| Optional subject-linked capture is allowed. | S01-compatible if it links a record to an existing known thing. | Authority, contract, scenario runtime, manual, product/SME, claim wording. | Stop if subject link becomes canonical registry lifecycle truth, active/inactive state, verification truth, or merge/split behavior. |
| Missing-known-thing capture can keep work moving. | Allowed only as unpromoted candidate/capture evidence for review. | Product/SME, manual walkthrough, claim wording, S06 gate evidence. | Stop if candidate/unlinked/missing-known-thing language implies registry creation, automatic matching, or discovered-unit lifecycle. |
| Local save and offline confidence are understandable. | Mobile local save/pending behavior is feasible; product wording remains partial. | Automated mobile target, manual walkthrough, product/SME, claim wording. | Stop if saved locally implies server received, reviewed, approved, globally complete, retained, or recoverable after device loss. |
| Sync states are understandable. | Sync/pull/push kernel exists; UX state language needs validation. | Automated mobile/server targets, manual walkthrough, product/SME, ops evidence. | Stop if synced implies approval, live field truth, conflict-free status, or complete reporting truth. |
| Failed sync preserves user confidence and pending work. | Mobile feasibility exists; recovery UX and support path need evidence. | Automated mobile target, manual walkthrough, product/SME, ops evidence. | Stop if failed sync implies data loss, server rejection, token refresh, mobile OIDC login, sealed recovery, or retention/security behavior. |
| Append-only correction is understandable. | Kernel supports append-only behavior; dedicated correction UX is not productized. | Authority, contract, automated, manual, product/SME, claim wording. | Stop if correction implies erasing history, editing records in place, resolver reassignment, or auto-resolution. |
| Needs-review and unresolved issue visibility are useful. | Existing flags/projections support basic visibility; production queues and automation are out of scope. | Authority, automated, scenario runtime, manual, product/SME, claim wording. | Stop if needs review implies hard rejection, batch queue, direct flag mutation, auto-resolution, resolver reassignment, or broad review authority. |
| Latest synced can be distinguished from live truth. | Product-surface partial. Requires explicit comprehension evidence. | Manual walkthrough, product/SME, scenario/manual evidence, claim wording. | Stop if latest synced is described as live field reality, supervisor approval, full reporting truth, or audit completeness. |
| Access-ended/stale-access language is understandable. | Accept-and-flag/advisory posture applies; user-facing recovery is partial. | Manual walkthrough, product/SME, automated target where later scoped, ops evidence. | Stop if stale access causes mobile authoritative rejection or implies deletion, security retention, or decommissioning. |
| Shared-device switch can be claimed only if validated. | Actor partitions are accepted standing; detailed UX/evidence belongs to FD-PKT-004. | Authority, automated mobile target, manual walkthrough, ops evidence, claim wording. | If shared devices are claimed, gate on A-to-B switch, pending preservation/drain-or-seal, partition isolation, and no retention/security overclaim. |
| Operator/support recovery can be framed for constrained deployment. | Operator-deployable-with-constraints standing exists; turnkey production is blocked. | Ops evidence, manual walkthrough, product/SME, release evidence, claim wording. | Stop if support language implies production admin auth, online binding-admin UI/API, device recovery guarantee, retention/security, or turnkey production readiness. |

### S06 Dependency Claims From FD-PKT-101

| S06 dependency claim | Current status from sources | Evidence classes required | Gate or stop condition |
|---|---|---|---|
| First deployment may need maintained known things. | `needs-decision`; product pressure is credible but not proven. | Product/SME, registry artifacts, manual walkthrough, authority routing. | Candidate 1 implementation must not dispatch until FD-PKT-101 chooses, promotes, splits, or explicitly defers this dependency. |
| Known thing definition and initial known-set source are open questions. | Discovery needed before lifecycle claims. | Product/SME, registry artifacts, ops evidence if source process is claimed. | Stop if Candidate 1 assumes import, registry stewardship, field-created registry items, or source authority without S06 routing. |
| Field discovery semantics are undecided. | Candidate evidence is allowed; registry creation is not. | Product/SME, missing-known-thing walkthrough, claim wording. | Stop if field discovery creates canonical registry truth, lifecycle state, automatic match, or candidate promotion behavior. |
| Lifecycle vocabulary may be operationally important. | Vocabulary evidence needed; lifecycle behavior not authorized. | Product/SME vocabulary tests, claim wording, authority routing. | Stop if active, inactive, retired, closed, moved, verified, merged, or split become day-one truth without routed successor decision. |
| Duplicate handling and merge/split expectations need evidence. | Duplicate-suspected language may be review-oriented only. | Product examples, manual walkthrough, claim wording, authority routing. | Stop if duplicate, merge, split, or review language implies auto-merge, auto-resolution, resolver reassignment, or merge/split UX. |
| If S06 is promoted, contracts/code/tests must be mapped first. | Future successor route only. | Authority, contract, code inspection, automated test plan, manual walkthrough. | Stop if any implementation packet touches S06 data model, lifecycle state shape, contract change, or registry stewardship before routing. |

## 5. Automated Test Candidate Matrix

This packet does not run tests and does not prescribe exact commands beyond
test names or boundaries already cited by sources. Implementation packets must
choose exact commands for their touched surface.

| Boundary | Existing evidence known from routed sources | Future automated target | Notes and gate |
|---|---|---|---|
| Event envelope and contract parity | Current contract guidance names the 11-field envelope, closed 6-value `type` vocabulary, and `EnvelopeSchemaParityTest`. | Targeted contract/parity checks if any future packet touches envelope-adjacent behavior. | No new envelope fields or event types are allowed by FD-PKT-003. |
| Config package, shape format, platform payload schemas, context refs | BAR-010/BAR-011/NW-034/NW-057 standing covers config/package and fixed `context.*` boundary. | Targeted config/package/expression tests where a future implementation packet touches the boundary. | Unknown `context.*` refs remain deploy-time invalid; no new vocabulary here. |
| Append-only capture/correction and projections | BAR-001 through BAR-015 and S00 support kernel behavior. | Targeted server tests for capture, correction, projection, and history behavior. | Correction tests must prove append-only behavior, not in-place edit. |
| Assignment-derived access and sync scope | Accepted baseline plus NW-050 for assignment-admin command boundary. | Targeted assignment, sync, auth, and containment tests. | No IdP group/claim/JWT actor authority and no UI-only access rules. |
| Flags and needs-review visibility | S21/S26-style evidence supports review inputs; production queues are not accepted. | Targeted flag/projection/review-permission tests if touched. | No auto-resolution, resolver reassignment, direct flag mutation, or batch bypass. |
| Scenario runtime probe coverage | Stage 6 says Candidate 1 should reuse S00/S19/S21/S23. | Bounded end-to-end Candidate 1 probe covering setup, assignment, standalone capture, subject link, candidate capture, offline save, sync, correction, unresolved issue, and latest-synced interpretation. | Probe design belongs to later staging/evidence work; do not add new primitives. |
| Mobile setup/connect | Stage 5 says raw bearer setup verifies `/api/auth/me` and activates returned actor session. | Widget/integration target for setup success and setup failure. | Must not claim production mobile OIDC/Keycloak login. |
| Mobile offline save and pending data | Stage 5 lists useful targets: `sync_service_test`, `event_assembler_test`, `selective_retain_test`, `projection_equivalence_test`, `projection_engine_test`, `form_engine_test`, `config_store_test`, `context_resolver_test`, `expression_evaluator_test`, and `activity_role_actions_test`. | Widget/integration target proving offline save creates one pending event with active actor/session data. | Saved locally must not imply server receipt or retention/security. |
| Mobile advisory warnings | Stage 5 says mobile warnings remain advisory. | Widget/integration target proving advisory warning displays but does not block save. | Mobile must not become authoritative rejection. |
| Sync failure, unauthorized, actor drift, no connection | Stage 5 says pending work is preserved and unauthorized/actor-drift checks stop before push. | Widget/integration target for success, push failure, unauthorized, actor drift, and no-connection with pending preservation. | Must not imply token refresh/logout or mobile login exists. |
| Mobile correction | Stage 5 says subject-detail capture appends another event rather than editing history. | Widget/integration target proving correction adds an event/update. | No in-place edit or history rewrite. |
| Mobile work list state | Stage 5 names pending count, flag count, latest timestamp, empty/no-config states. | Widget/integration target for counts, latest timestamp, sync entry point, and empty/no-config states. | State labels must pass FD-PKT-002 vocabulary checks. |
| Shared-device switch if claimed | NW-055 accepted actor partitions; Stage 5 says switch drains or seals A and isolates B. | Widget/integration target for A-to-B switch, pending preservation/drain-or-seal, isolation, and safe resume. | Retention, expiry, decommissioning, sealed recovery, and local encryption remain FD-PKT-104/NW-054. |
| Claim wording and banned terms | FD-PKT-002/101 require banned lifecycle and production-readiness claims to be absent. | Static review, UI copy test, or approval checklist for lifecycle, authority, production, retention, reporting, and auth wording. | Stop if banned terms are needed to make Candidate 1 honest. |

## 6. Manual Walkthrough Matrix

Each walkthrough must record acting context, authority mapping, script,
vocabulary tested, observed comprehension, defects or risks, S06/successor
triggers, and pass/revise/route outcome.

| Walkthrough | Evidence to capture | Pass criterion | Gate or route |
|---|---|---|---|
| Setup | Setup owner explains setup artifact, assigned work, publish handoff, and responsibility model without architecture vocabulary. | User can describe what will be collected, who sees work, and what publish means. | Route if setup requires production admin auth, custom scope, or config-builder complexity. |
| Standalone capture | Field user opens assigned work, records a basic entry, saves locally while offline, syncs later, and interprets state language. | User distinguishes saved locally, waiting to sync, syncing, synced, synced with issue, and failed to sync. | Route if standalone capture needs new event vocabulary, hard validation authority, or product terms that imply platform primitives. |
| Subject-linked capture | Field user finds and confirms a known thing, records against it, and later sees linked record in latest-synced context. | User understands the record refers to an existing known thing without inferring lifecycle truth. | Route through FD-PKT-101 if known-set source, verification, active/inactive state, or subject history expectations become required. |
| Missing-known-thing or candidate capture | Field user cannot find the thing, saves unlinked/candidate evidence, and understands it needs matching/review. | User does not interpret candidate evidence as registry creation, verification, automatic match, or lifecycle state. | This is an S06 gate. Stop if users cannot understand candidate-only handling. |
| Failed sync | Field user sees failed sync, understands work remains saved locally, retries or follows support path. | User does not infer data loss, server rejection, approval, or token refresh behavior. | Feed FD-PKT-004 and ops/support packets; route mobile auth if provider login or token lifecycle is required. |
| Correction | User appends a correction/update and understands original history remains traceable. | User distinguishes correction from in-place editing, replacement, cancellation, and approval. | Route if correction requires mutable history, resolver reassignment, or domain conflict automation. |
| Supervisor freshness | Supervisor/reviewer reads latest synced view, timestamps, unresolved issue signals, and pending limitations. | User distinguishes latest synced from live field truth and full reporting truth. | Route reporting/import-export if dashboard/API/report freshness semantics are needed. |
| Needs-review | Supervisor/reviewer sees an issue and understands available action, unresolved status, and non-designated resolution limits. | User understands view-only versus resolution authority and exact designated-resolver limits. | Route conflict-review successor if queues, batch, auto-resolution, resolver reassignment, or broad review UX is required. |
| Access-ended/stale access | User sees no longer assigned/access changed language and understands valid saved work may be kept and marked for review. | User does not infer deletion, device decommissioning, security erasure, or mobile-side rejection. | Feed FD-PKT-004; route FD-PKT-104 for retention/security/device lifecycle claims. |
| Shared-device if claimed | Actor A has pending work, device switches to actor B, B cannot see A partition, then A resumes safely or A is sealed. | Users understand active actor, pending warning, isolation, and no cross-actor access. | Required only if shared devices are claimed; route FD-PKT-104 if recovery/decommissioning/retention is needed. |
| Operator/support recovery | Operator/support explains invalid token, setup/connect problem, sync failure, access ended, and pending work using constrained-deployment language. | Support path is realistic and does not promise production auth, broad data access, device recovery, or retention/security. | Route FD-PKT-006/007/108 for ops readiness and constrained deployment evidence. |

## 7. Product/SME Validation Matrix

| Validation area | Evidence to collect | Required output | Stop or route condition |
|---|---|---|---|
| Candidate 1 outcome | Product Manager and SME agreement on the smallest first-deployment outcome worth using. | Bounded value statement with explicit non-goals. | Stop if value depends on production auth, reporting, retention/security, S06 lifecycle, custom scope, or conflict automation. |
| Vocabulary | User comprehension for setup, form/checklist/activity, assigned work, record, known thing, unlinked/candidate, saved locally, waiting to sync, synced, failed to sync, correction, latest synced, needs review, access ended. | Approved glossary or unresolved vocabulary list with owner and evidence need. | Route if user terms must become platform vocabulary, contract fields, event types, scope mechanisms, or authority rules. |
| Setup flow | How setup starts, who approves it, what is too technical, common mistakes, and publish handoff. | Setup script and comprehension result. | Route if setup needs production admin UX/auth, config authoring syntax, or online binding-admin behavior. |
| Assignment/responsibility | Who grants/ends responsibility, how users know work belongs to them, and what role differences matter first. | Minimal role-action evidence and authority mapping. | Route if access depends on query/custom scope, UI filters, broad auditor views, emergency bypass, or IdP groups/claims. |
| Known-set source | Examples of initial known-set artifacts, owner, update path, import/setup/external registry/field discovery/mixed source. | S06 input pack for FD-PKT-101. | Candidate 1 implementation remains blocked if this evidence shows maintained known things are required. |
| Subject lookup and confirmation | How users identify the right thing: name, code, location, QR/barcode, map, list, local knowledge, or other cues. | Confirmation cue checklist and wrong-link risk list. | Route if lookup needs registry stewardship, lifecycle state, duplicate workflow, or merge/split UX. |
| Missing known thing | What users do when missing, stale, duplicated, renamed, moved, or ambiguous. | Candidate-only acceptance or S06 promotion trigger. | Stop if candidate-only wording is not understandable or acceptable. |
| Offline confidence | Time offline, local status words, failed sync recovery, support path, and lost-device expectations. | Offline confidence evidence for FD-PKT-004. | Route retention/security/device-loss policy if guarantees beyond local pending preservation are needed. |
| Correction | Who may correct, when, what reason is needed, whether offline correction is acceptable, and what supervisors see. | Correction language and authority evidence. | Route if correction requires mutable history, broad correction authority, workflow state, or conflict automation. |
| Supervisor/reviewer interpretation | Freshness indicator, unresolved issue needs, view-only/resolution authority, duplicate-suspected examples. | Freshness and needs-review comprehension evidence. | Route reporting or conflict-review successor lanes if product claim exceeds current basics. |
| Operator/support | Realistic support path for invalid token, setup/connect problem, sync failure, access ended, and pending work. | Constrained-deployment support script and risk list. | Route production auth/admin/mobile login or ops readiness if support needs those capabilities. |

## 8. Mobile/Offline Evidence Needs Feeding FD-PKT-004

FD-PKT-004 should use this packet as input, but FD-PKT-003 does not define
mobile implementation.

Evidence needs for FD-PKT-004:

- Setup/connect validation over raw bearer plus `/api/auth/me`, clearly marked
  as constrained setup and not productized mobile OIDC/Keycloak login.
- Work-list evidence for assigned work, pending count, flag count, latest
  timestamp, sync entry point, and empty/no-config states.
- Offline save evidence that one local pending event is preserved with active
  actor/session data and clear saved-on-this-device wording.
- Sync state evidence for waiting, syncing, synced, synced with issue, failed,
  unauthorized, actor drift, and no connection.
- Pending-work preservation evidence through restart, failed sync, auth
  failure, and retry.
- Advisory warning evidence proving warnings display without blocking save.
- Correction evidence proving an appended record/update, not an in-place edit.
- Missing-known-thing evidence that standalone or missing capture remains
  unlinked/candidate unless FD-PKT-101 promotes S06.
- Shared-device evidence only if claimed: A-to-B switch, drain-or-seal,
  partition isolation, safe resume, and no retention/security overclaim.
- Freshness evidence tying latest synced and latest local timestamp to user
  comprehension, not live truth.
- Manual device/offline matrix scope: invalid token, valid setup, offline
  capture, app restart, sync retry, unauthorized/actor drift, pending
  preservation, stale/access-changed save, and support path.

FD-PKT-004 must stop if mobile validation requires production login, token
refresh/logout, secure storage, local encryption, decommissioning,
sealed-partition recovery, authoritative mobile rejection, new scope filters,
or S06 lifecycle behavior.

## 9. View-Model/Contract Evidence Questions Feeding FD-PKT-005

FD-PKT-005 should answer these questions without assuming a new contract is
needed:

1. Can Candidate 1 compose saved locally, waiting to sync, synced, failed,
   issue, correction, latest synced, and access-ended states from existing
   events, projections, flags, sync metadata, assignment scope, and config?
2. Which state labels are purely UI/view composition, and which would require a
   stable shared view-model shape across server/mobile?
3. Is subject-history needed in Candidate 1, or can subject-linked capture stay
   as adapter/view composition over existing subject refs/history where
   available?
4. Can missing-known-thing/candidate capture be represented without a new S06
   data model, lifecycle state shape, candidate promotion workflow, or registry
   contract?
5. Can needs-review visibility be expressed from existing flags/projections
   without a production review queue, resolver reassignment, or auto-resolution
   surface?
6. Can latest-synced/freshness be represented from existing sync/projection
   metadata without report/API/export freshness semantics?
7. Does any Candidate 1 surface need new envelope fields, event types, schema
   fields, shape/package keys, `context.*` refs, scope mechanisms, durable
   workflow state, or authority rules?
8. Which exact contracts, fixtures, code paths, and tests would be touched if a
   stable view-model contract is required?

FD-PKT-005 must stop if the answer requires contract/schema changes, new API
meaning, a shared S06 model, lifecycle state, custom scope, production
reporting, or authority movement before routing.

## 10. S06 Gate Evidence

FD-PKT-003 requires these S06 gates before Candidate 1 implementation
dispatch:

| Gate | Evidence needed | Pass condition | Failure route |
|---|---|---|---|
| Candidate versus canonical understanding | Product/SME vocabulary tests and missing-known-thing walkthrough. | Users understand candidate/unlinked evidence as review input, not registry truth. | FD-PKT-101 must promote, split, or defer with explicit risk signoff before implementation. |
| Known thing definition | At least deployment-like examples naming thing type, user term, owner, update path, offline behavior, and pain points. | Product Manager can state what a known thing means for first deployment or explicitly keep it out. | BAR-105/S06 successor routing. |
| Known-set source | Sample artifacts: import, setup-owner entry, external registry, paper/spreadsheet cleanup, field discovery, or mixed process. | Source and authority can be excluded from Candidate 1 or bounded for successor decision. | S06 route if source/authority is needed for day one. |
| Field discovery semantics | Examples of missing, stale, duplicated, renamed, moved, or ambiguous thing cases. | Candidate-only handling is validated or S06 promotion trigger is documented. | S06 route if field discovery must create registry items or candidate subjects. |
| Lifecycle vocabulary | Tests for active, inactive, retired, closed, moved, verified, merged, split, duplicate, and needs review. | Candidate 1 surfaces avoid lifecycle truth or FD-PKT-101 promotes the minimal needed behavior. | BAR-105/S06 successor route. |
| Duplicate and merge/split expectations | Obvious duplicate, ambiguous duplicate, harmless duplicate, wrong link, wrong merge, true split, movement/closure, and stale offline update examples. | Duplicate-suspected can stay review-oriented without auto-merge or merge/split UX. | S06 or conflict-review successor route if behavior is required. |
| Stale/offline known-set behavior | Walkthrough showing old work is preserved and review-visible without rejection or lifecycle truth. | Users understand stale/offline work as preserved evidence needing review. | Route S06 or FD-PKT-104 depending on whether lifecycle or retention/security is the blocker. |
| Dispatch decision | FD-PKT-101 chooses Option A, B, C, or D, or an equivalent explicit decision accepted by workshop control. | Candidate 1 implementation packet can name S06 as excluded, promoted, split, or pre-release-gated. | Candidate 1 implementation remains no-go. |

## 11. Release/Readiness Gate Model

No lane is release-ready unless every applicable gate passes for that lane.
For Candidate 1, these gates are evidence requirements, not approval.

| Gate | Candidate 1 requirement | S06/successor handling |
|---|---|---|
| Authority gate | Current routing, CDL/contracts, BAR/NW standing, and FD-PKT packet state allow the claim. | BAR-105/S06 remains `needs-decision` until FD-PKT-101 resolves or explicitly excludes it. |
| Contract gate | No envelope, type, schema, protocol, config package, shape, pattern, fixture, scope, or authority drift is needed. | Any required S06 contract/data-model change routes before implementation. |
| Test gate | Existing evidence is mapped and future targeted server/mobile tests are named by boundary. Exact commands belong to later implementation packets. | Lifecycle, duplicate, merge/split, candidate promotion, and registry stewardship need routed test plans if promoted. |
| Scenario/manual gate | S00/S19/S21/S23 feasibility is reused where applicable, and Candidate 1 manual walkthroughs pass. | Candidate-vs-canonical, stale/offline known-set, duplicate-suspected, and lifecycle vocabulary gates must pass or route. |
| Product gate | Product/SME validation confirms vocabulary, setup, offline confidence, correction, freshness, needs-review, support, and acceptance. | Product/SME validation decides whether S06 is excluded, promoted, split, or pre-release-gated. |
| Ops/security gate | Operator/support language is constrained and does not claim production hardening. | Production auth, mobile login, retention/security, reporting, backup/restore, monitoring, and support runbooks stay in successor ops packets unless claimed. |
| Claim wording gate | Candidate 1 wording avoids lifecycle truth, production readiness, live truth, hard rejection, data-loss, retention/security, reporting/export, auth/login, custom scope, and automation overclaims. | Any required lifecycle or production wording triggers stop and route. |

## 12. Go/No-Go Recommendation By Packet

| Packet or dispatch point | Recommendation | Reason |
|---|---|---|
| FD-PKT-002 | CONDITIONAL GO for review/acceptance, not implementation. | Product/spec and UX validation can proceed as S01-compatible with explicit exclusions and Option C dependency marker. |
| FD-PKT-003 | GO for evidence-design review when this file is complete. | This packet maps claims to evidence and gates without editing code, running tests, or deciding S06. |
| FD-PKT-004 | CONDITIONAL GO after FD-PKT-003 review. | Mobile/offline validation can be scoped from these evidence needs, but must avoid login, retention/security, authoritative rejection, and S06 lifecycle. |
| FD-PKT-005 | CONDITIONAL GO after FD-PKT-003 review. | View-model/contract assessment is needed before implementation, but must first test whether adapter/view composition is enough. |
| FD-PKT-101 | GO and REQUIRED before Candidate 1 implementation dispatch. | S06 discovery/decision must choose, promote, split, or explicitly defer/exclude the dependency. |
| Candidate 1 implementation dispatch | NO-GO. | Implementation remains blocked until FD-PKT-002 through FD-PKT-005 are gated, FD-PKT-101 resolves or explicitly excludes the S06 dependency, exact implementation packets name files/contracts/tests/stop conditions, and no successor lane is mixed in. |

## 13. Forbidden Work

- Do not run server/mobile tests for this docs packet.
- Do not edit code, contracts, schemas, APIs, fixtures, status, routers,
  backlogs, BAR/NW, CDL, workshop control files, or runtime behavior.
- Do not commit.
- Do not implement or authorize Candidate 1 implementation.
- Do not decide, implement, or authorize S06.
- Do not create product scope; classify evidence only.
- Do not use missing evidence to erase Candidate 1 product need.
- Do not add or imply envelope fields, event types, scope mechanisms, identity
  categories, durable workflow state, stored current truth, deployer-authored
  access logic, deployer-authored state machines, scripts, custom traversals,
  device-side triggers, expression-function vocabulary, expanded `context.*`
  refs, or new pattern inventory.
- Do not add or imply canonical entity lifecycle, active/inactive/retired state
  truth, discovered-unit lifecycle, registry stewardship, duplicate handling
  workflow, movement/closure/retirement behavior, merge/split UX, candidate
  promotion, or verification policy.
- Do not add or imply production auth/admin/mobile login, online
  binding-admin UI/API, token lifecycle, retention/security, reporting/export,
  import, broad audit/history access, aggregate access divergence, custom/query
  scope, conflict automation, resolver reassignment, auto-resolution, or
  production readiness.
- Do not make mobile warnings authoritative rejection.
- Do not make latest synced live truth.
- Do not make needs review a production review queue or hard rejection.
- Do not make persona labels identity categories, authority primitives, config
  namespaces, fixed product modules, product-area boundaries, or implementation
  service boundaries.
- Do not route work back under a retired first-deployment review path. Current
  workshop home is `docs/workshops/first-deployment/`.

## 14. Stop And Report Conditions

Stop and report if:

- Active status, routed packet inputs, scenario/user-fit context, or workshop
  docs conflict in a way that changes this packet boundary.
- Drafting this evidence plan requires editing any file other than this one.
- Product validation requires a contract, code, schema, API, fixture, or
  runtime behavior change before implementation routing.
- Candidate 1 cannot honestly remain S01-compatible without maintained known
  things, lifecycle state, discovered-unit stewardship, registry stewardship,
  duplicate stewardship, merge/split UX, lifecycle words, or candidate
  promotion.
- S06 is hidden as vague later work without owner, evidence, route, and
  decision point.
- A user-facing term becomes an event field, event type, scope mechanism, flag
  category, schema/contract field, durable workflow state, authority rule, or
  shared API meaning.
- Latest synced, saved locally, failed sync, needs review, correction, or
  access ended implies live truth, server receipt, approval, rejection, data
  loss, deletion, retention/security, or history rewrite.
- Production-readiness, production auth/admin/mobile login, retention/security,
  reporting/export/import, conflict automation, resolver reassignment,
  auto-resolution, custom/query scope, or ops readiness enters Candidate 1.
- Persona labels harden into identity categories, fixed modules, access rules,
  config namespaces, product-area boundaries, or implementation service
  boundaries.
- Any stale retired first-deployment review path is needed for this packet.

## 15. Done Definition

FD-PKT-003 is done when:

1. This file exists at
   `docs/workshops/first-deployment/task-packets/fd-pkt-003-candidate-1-evidence-plan.md`.
2. No other file is edited.
3. Required sections 1 through 16 are present.
4. Candidate 1 claims from FD-PKT-002 and S06 dependency claims from
   FD-PKT-101 are mapped to evidence classes and gates.
5. Automated test candidates are identified by boundary without inventing exact
   commands.
6. Manual walkthrough, Product/SME, mobile/offline, view-model/contract, S06
   gate, release/readiness, go/no-go, forbidden-work, stop-condition, and
   downstream-impact sections are explicit.
7. The packet preserves Candidate 1 product need, S01 compatibility, S06
   visibility, and persona-label acting-context boundaries.
8. The file contains no retired first-deployment review path reference.
9. `git diff --check` passes.
10. No server/mobile tests are run.

## 16. Downstream Packet Impacts

| Downstream packet or lane | Impact |
|---|---|
| FD-PKT-002 | Use this evidence plan to tighten product/spec acceptance language, vocabulary evidence, walkthrough artifacts, and claim wording gates. Do not add implementation scope. |
| FD-PKT-004 | Define mobile/offline validation from section 8: setup/connect, offline save, failed sync, pending preservation, correction, freshness, access-ended, and shared-device if claimed. Keep mobile login, retention/security, and S06 lifecycle out. |
| FD-PKT-005 | Use section 9 to assess adapter/view composition versus a routed view-model/contract need. Stop on contract/schema/API/S06 model pressure. |
| FD-PKT-101 | Use section 10 as the Candidate 1 implementation stop gate. FD-PKT-101 must choose, promote, split, or explicitly defer/exclude S06 before dispatch. |
| FD-PKT-006/007/108 | Carry ops readiness, staging rehearsal, auth manifest, assignment bootstrap, config publish, backup/restore, rollback, monitoring, incident/support, and constrained deployment evidence. Do not make FD-PKT-003 a production-readiness packet. |
| Later Candidate 1 implementation packets | Must receive one bounded surface only, with exact files/contracts, accepted constructs reused, targeted tests, manual evidence, excluded successor lanes, forbidden work, stop conditions, and commit boundary. |
| Scenario/product evidence work | Treat missing evidence as product/SME validation, manual walkthrough artifacts, scenario probes, or routed decision inputs. Do not convert missing evidence into product silence. |
