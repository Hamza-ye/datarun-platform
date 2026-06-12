# Candidate 1 Reality Gate Review

Role: Reality Checker for the Datarun Platform first-deployment gate review

Date: 2026-06-13

Gate status: NEEDS WORK

Files reviewed:

- `AGENTS.md`
- `docs/status.md` Current Routing only
- `docs/agent-working-surface/first-deployment-task-packet-router.md`
- `docs/workshops/first-deployment/README.md`
- `docs/workshops/first-deployment/stage-6-pressure-test.md`
- `docs/workshops/first-deployment/stage-7-delivery-plan.md`
- `docs/workshops/first-deployment/stage-8-task-packet-backlog.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-002-candidate-1-product-spec-ux-validation.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-003-candidate-1-evidence-plan.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-004-candidate-1-mobile-offline-validation.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-005-candidate-1-view-model-contract-assessment.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-101-s06-entity-lifecycle-discovery.md`

Scope note: this review does not authorize implementation. It is a gate input
for Project Shepherd consolidation.

## Reality Gate Finding

Candidate 1 is not ready for broad implementation. The packet set is disciplined
about authority, exclusions, and claim wording, but it still has unresolved
product and S06 evidence dependencies. The defensible path is not "build
Candidate 1"; it is to consolidate a bounded implementation packet only after
the packet gate is accepted and the S06 dependency is resolved, promoted,
split, or explicitly excluded with evidence and risk signoff.

There is a narrow future lane that may be safe: adapter/view composition over
existing accepted constructs for a single Candidate 1 surface. That lane must
stay S01-compatible and must not create new contracts, schemas, event
vocabulary, scope mechanisms, durable workflow state, mobile authority,
production auth, reporting, retention/security, or entity lifecycle behavior.

Reality status: NEEDS WORK. The packets make a conditional narrow lane
plausible, but they do not yet provide implementation dispatch evidence.

## Top Risks Blocking Broad Implementation

1. Hidden S06 risk remains the largest blocker. FD-PKT-101 is still a
   discovery and decision seed. It has not selected whether Candidate 1 excludes
   S06, promotes a minimal successor, splits S06 into named packets, or carries
   a pre-release gate. Any "known thing", "candidate", "duplicate", "closed",
   "moved", "inactive", "verified", "merged", or "split" language can quietly
   become lifecycle truth.
2. Product-readiness evidence is missing. The packets require Product/SME
   validation, vocabulary tests, journey walkthroughs, mobile/manual matrices,
   support scripts, and comprehension evidence. They do not show completed
   evidence yet.
3. Implementation-mixing risk is high. Candidate 1 can easily absorb
   production admin auth, mobile OIDC/login, reporting, retention/security,
   conflict queues, custom scope, or ops readiness unless each packet remains
   one lane with exact exclusions.
4. Protocol-over-product risk is real. The accepted kernel can support basic
   capture/sync/correction mechanics, but raw bearer setup, latest synced,
   needs review, failed sync, and access-ended language are not product-ready
   until users understand them without architecture vocabulary.
5. Contract and read-model drift risk remains. FD-PKT-005 recommends adapter
   composition, but any shared view-model, new API response field, schema
   field, envelope fact, scope rule, or stored current-truth table would need
   routing before implementation.
6. Mobile authority overclaim risk remains. Mobile warnings must stay advisory,
   local save must not imply server receipt or retention, synced must not imply
   approval or live truth, and shared-device behavior must not imply recovery,
   decommissioning, encryption, or retention guarantees.

## Narrow Slice That May Be Safe To Dispatch If Any

A future bounded implementation packet may be safe only if it is one narrow
adapter/view-composition surface, for example:

- Candidate 1 mobile setup/connect copy and active actor display over raw
  bearer plus `/api/auth/me`;
- offline save, pending, failed sync, retry, and local-preservation labels over
  existing mobile pending state and sync metadata;
- work-list issue, pending count, latest timestamp, and empty/no-config state
  composition over existing assignments, config, flags, projections, and local
  state;
- append-only correction UX over existing capture/correction behavior;
- needs-review or latest-synced display over existing flags, projections, and
  sync timestamps, without report/dashboard semantics.

This is safe only as a future packet, not as direct dispatch from this review.
The packet must choose one surface, name exact files/contracts, and prove that
the state can be composed from existing sources. Server/API work should be
avoided unless implementation inspection proves current endpoints already
provide the needed facts without new response semantics.

## Required Conditions For Any Implementation Packet

1. Project Shepherd must gate FD-PKT-002, FD-PKT-003, FD-PKT-004, FD-PKT-005,
   and FD-PKT-101 against the Stage 8 packet gate.
2. FD-PKT-101 must produce an explicit S06 position before implementation:
   exclude S06 from Candidate 1, promote a bounded minimal successor first,
   split S06 and identify which split blocks Candidate 1, or defer with a
   pre-release gate and product risk signoff.
3. Product/SME evidence must show that candidate, unlinked, missing-known-thing,
   linked record, latest synced, needs review, correction, failed sync, and
   access changed are understood without lifecycle, live-truth, approval,
   rejection, or registry implications.
4. The implementation packet must cover one lane and one surface only. It must
   name exact files/contracts allowed, accepted constructs reused, forbidden
   work, expected tests, manual evidence, stop conditions, and commit boundary.
5. A state-to-source checklist must map every visible state to existing events,
   projections, flags, sync metadata, assignments, config, subject refs where
   already supported, and mobile actor partitions.
6. Contract pressure review must confirm no envelope, event type, schema,
   config package, platform payload, pattern, fixture, API, scope, authority,
   durable-state, or shared view-model change is required.
7. Targeted automated test commands and manual walkthrough evidence must be
   specified by the implementation packet for the touched surface. This review
   did not run tests.
8. The packet must preserve visible successor lanes with owner, route, evidence
   need, and stop condition. Missing product evidence must become evidence work,
   not product silence.

## Claims That Must Stay Out Of Candidate 1

- Turnkey production readiness or constrained deployment go/no-go.
- Production web admin auth, online binding-admin UI/API, mobile
  OIDC/Keycloak login, token refresh/logout, token expiry, secure storage, or
  IdP group/claim/JWT `actor_id` authority.
- Retention/security/device lifecycle, local encryption, decommissioning,
  sealed recovery, no-local-retention, redaction, or device-loss recovery.
- Reporting dashboards, report APIs, export/import, broad audit/history,
  aggregate access, or reporting freshness semantics.
- Canonical known-thing registry lifecycle, maintained known-set authority,
  active/inactive/retired/closed/moved/verified truth, candidate promotion,
  registry stewardship, duplicate workflow, merge/split UX, or automatic
  matching.
- Conflict review queues, batch handling, direct flag mutation, resolver
  reassignment, auto-resolution, or broad review authority.
- Custom/query scope, auditor/report filters as access authority, emergency
  bypasses, or special read/write access.
- Latest synced as live field truth, approval, full report truth, or audit
  completeness.
- Saved locally as server receipt, durable retention, recovery guarantee, or
  conflict-free status.
- Mobile warnings as authoritative rejection or local access authority.
- Persona labels as identity categories, fixed modules, config namespaces,
  access rules, product-area boundaries, or service boundaries.

## Explicit Implementation Unblock Position

UNBLOCK WITH CONDITIONS

Meaning: Project Shepherd may consolidate toward a bounded Candidate 1
implementation packet only if the required conditions above are satisfied. This
position does not authorize implementation execution. Broad Candidate 1
implementation remains blocked, and code dispatch remains blocked until a
single-surface packet passes the gate with exact files, tests, exclusions, and
stop conditions.

## Stop/Route Conditions

Stop and route before implementation if:

- Candidate 1 cannot honestly remain S01-compatible without maintained known
  things, lifecycle state, discovered-unit stewardship, registry stewardship,
  duplicate stewardship, merge/split UX, lifecycle words, or candidate
  promotion.
- S06 is hidden as vague later work instead of owner, evidence, route, and
  decision point.
- Candidate 1 copy, UI, tests, or implementation fields turn known thing,
  candidate, unlinked, duplicate suspected, active, inactive, closed, moved,
  retired, verified, merged, or split into platform truth.
- Any user-facing term becomes an event field, event type, scope mechanism,
  flag category, schema/contract field, durable workflow state, authority rule,
  or shared API meaning.
- A shared view-model contract, new endpoint, new API response field meaning,
  new fixture, or durable read model is needed; route through FD-PKT-005 and
  the appropriate architecture/source-order path first.
- Mobile behavior requires production login, token lifecycle, secure storage,
  local encryption, sealed recovery, decommissioning, cross-actor visibility,
  or local authoritative rejection.
- Production auth/admin/mobile login enters scope; route to FD-PKT-102 or
  FD-PKT-103.
- Retention/security/device lifecycle enters scope; route to FD-PKT-104.
- Reporting, export/import, broad audit/history, aggregate access, or report
  freshness enters scope; route to FD-PKT-105.
- Conflict queues, automation, resolver reassignment, auto-resolution, or
  direct flag mutation enters scope; route to FD-PKT-106.
- Custom/query scope, auditor/report filters as authority, or special access
  enters scope; route to FD-PKT-107.
- Ops readiness, staging rehearsal, TLS/secrets, backup/restore, rollback,
  monitoring, incident response, or production wording enters scope; route to
  FD-PKT-006, FD-PKT-007, or FD-PKT-108.
- Active routing sources, contracts, BAR/NW standing, or packet inputs conflict
  in a way that changes the Candidate 1 boundary.
