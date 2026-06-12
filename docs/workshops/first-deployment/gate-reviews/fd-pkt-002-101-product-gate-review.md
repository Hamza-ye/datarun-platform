# FD-PKT-002 / FD-PKT-101 Product Gate Review

Role: Product Manager for first-deployment gate review

Date: 2026-06-13

Gate status: FD-PKT-002 is product-gate acceptable with conditions. Candidate 1 implementation remains dependent on Project Shepherd consolidation into bounded implementation packets.

Files reviewed:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/first-deployment-task-packet-router.md`
- `docs/workshops/first-deployment/README.md`
- `docs/workshops/first-deployment/stage-8-task-packet-backlog.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-001-s06-timing-decision-record.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-002-candidate-1-product-spec-ux-validation.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-003-candidate-1-evidence-plan.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-004-candidate-1-mobile-offline-validation.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-005-candidate-1-view-model-contract-assessment.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-101-s06-entity-lifecycle-discovery.md`

## Product Gate Finding For FD-PKT-002

FD-PKT-002 is acceptable as a Product Manager gate for Candidate 1 product/spec and UX validation. It preserves the right first-deployment promise: reliable assigned capture, offline local save, sync recovery, append-only correction, optional linking to an existing known thing, and review-oriented handling for unlinked or missing-known-thing cases.

The packet is acceptable because it keeps Candidate 1 S01-compatible and does not claim implementation readiness. Its product promise is bounded to current accepted constructs and adapter-level wording, while explicitly excluding lifecycle truth, registry stewardship, duplicate workflow, merge/split UX, production auth/admin/mobile login, retention/security, reporting/export/import, custom scope, and conflict automation.

The product risk is not technical ambiguity alone. The real risk is user misunderstanding: first-deployment users may read "known thing", "candidate", "missing", or "duplicate suspected" as evidence that Datarun maintains a canonical registry or lifecycle state. FD-PKT-002 handles this risk correctly by requiring vocabulary validation, journey walkthroughs, claim-wording checks, and an explicit S06 dependency marker before any implementation dispatch.

Product gate finding: accept FD-PKT-002 for review consolidation and downstream evidence planning, not as implementation authorization.

## S06 Decision Recommendation For FD-PKT-101

Recommendation: split S06 into a minimal Candidate 1 prerequisite plus later lifecycle work.

Minimal prerequisite before Candidate 1 implementation packet dispatch:

- Product and SME evidence must prove Candidate 1 can honestly operate with only existing known-thing links and unpromoted candidate or unlinked capture evidence.
- The product team must define "known thing" for Candidate 1 as validated user-facing copy only, not as a maintained registry promise, source-of-truth model, contract field, identity category, lifecycle state, or stewardship workflow.
- The first-deployment known-set source must be evidenced with deployment-like examples, but Candidate 1 must not claim to create, maintain, verify, deactivate, close, move, merge, split, or retire that known set.
- Missing-known-thing and candidate capture must be validated as preserved review evidence, not registry creation, automatic matching, candidate promotion, or discovered-unit lifecycle.
- Duplicate-suspected wording may remain review-oriented only if users do not infer duplicate workflow, merge/split UX, auto-merge, auto-resolution, or resolver reassignment.

Later S06 lifecycle work remains deferred and must route through BAR-105/S06 successor decision work if needed. That later work includes maintained known things, source and authority of the known set, field discovery as registry creation, active/inactive/retired/closed/moved/verified lifecycle states, registry stewardship, duplicate handling workflow, candidate promotion, merge/split governance, lifecycle tests, and any contract/code/model change.

This split keeps product pressure visible. It does not bury S06 as "later"; it narrows the immediate gate to Candidate 1 honesty and keeps the broader lifecycle decision named, owned, evidenced, and routed.

## Required Product/SME Evidence Before Implementation

Before any Candidate 1 implementation packet is dispatched, the Project Shepherd packet should require:

- A bounded Candidate 1 value statement signed off by Product and SME: the first deployment is useful without maintained lifecycle truth, production readiness, reporting, retention/security, custom scope, conflict automation, or registry stewardship.
- At least two deployment-like known-set examples naming the thing type, user term, source, owner, update path, offline/stale behavior, and duplicate or lifecycle pain.
- A known-set source note showing whether examples come from import, setup-owner entry, external registry, paper/spreadsheet cleanup, field discovery, or a mixed process, with an explicit statement that Candidate 1 does not maintain that source.
- Walkthrough evidence for standalone capture, subject-linked capture, missing-known-thing or candidate capture, offline local save, failed sync with pending preservation, append-only correction, latest-synced supervisor interpretation, and operator/support recovery. Shared-device walkthrough evidence is required only if Candidate 1 claims shared-device use.
- Vocabulary validation for setup, assigned work, record, known thing, linked record, unlinked record, candidate, missing known thing, saved locally, waiting to sync, synced, failed to sync, correction, latest synced, needs review, access ended, and duplicate suspected.
- Negative vocabulary validation for active, inactive, retired, closed, moved, verified, current, canonical, merged, and split, proving those words are absent from Candidate 1 truth claims or routed to S06.
- Duplicate and merge/split examples, even if excluded, to prove Candidate 1 can use duplicate-suspected review language without promising duplicate workflow, auto-merge, merge/split UX, or resolver reassignment.
- A state-to-source checklist mapping every Candidate 1 visible state to existing backing sources and marking adapter composition versus existing contract reuse.
- A contract-pressure check confirming Candidate 1 needs no new envelope field, envelope type, schema, sync protocol meaning, config package key, flag category, platform payload shape, fixture, API response field, durable workflow state, scope mechanism, or S06 data model.
- An unresolved-risk list with owner, route, and decision point for all S06 evidence gaps that remain after the minimal prerequisite.

## Required Wording Limits For Candidate 1

Allowed Candidate 1 wording, subject to validation:

- assigned work
- record or capture
- known thing as product copy only
- linked record or subject link
- unlinked record
- candidate for review
- missing known thing
- saved locally or saved on this device
- waiting to sync, synced, failed to sync
- correction or update as append-only language
- latest synced, latest known here, or last synced
- needs review or issue
- duplicate suspected as review-oriented language only
- access ended or access changed as advisory language

Candidate 1 must not claim or imply:

- maintained known things or maintained registry truth
- canonical registry creation
- discovered-unit lifecycle
- active, inactive, retired, closed, moved, verified, current, canonical, merged, or split lifecycle state
- registry stewardship, verification policy, candidate promotion, automatic matching, duplicate workflow, merge/split UX, auto-merge, auto-resolution, or resolver reassignment
- production web admin auth, production mobile login, OIDC/Keycloak login UX, token refresh/logout, online binding-admin UI/API, or IdP group/claim authority
- retention/security guarantees, deletion, device decommissioning, sealed recovery, local encryption, no-local-retention, redaction, or device-loss recovery
- reporting/export/import, broad audit/history, aggregate access, custom/query scope, or auditor/report filters as authority
- latest synced as live field truth, supervisor approval, full reporting truth, or audit completeness
- local mobile warnings as hard rejection or mobile-side authority
- "production ready", "turnkey", or unconstrained deployment readiness

## Explicit Implementation Unblock Position

UNBLOCK WITH CONDITIONS.

This is not implementation authorization. It means the Product Manager does not see FD-PKT-002 as a reason to block Project Shepherd consolidation, provided the implementation packet carries the conditions below and the minimal S06 prerequisite is resolved first.

What remains blocked:

- Full S06/entity lifecycle, including maintained known things, field-discovered registry creation, lifecycle states, registry stewardship, duplicate workflow, candidate promotion, merge/split UX, and lifecycle contracts/code/tests.
- Production web admin auth, mobile OIDC/Keycloak login, token lifecycle, online production binding-admin APIs, retention/security/device lifecycle, reporting/export/import, broad audit/history, custom/query scope, conflict review queues, resolver reassignment, auto-resolution, and ops readiness claims.
- Any implementation packet that requires new contract/schema/API meaning, new envelope fields or types, new flag categories, new scope mechanisms, durable workflow state, or a shared S06 data model.

What a later implementation packet may safely do, after Project Shepherd consolidation:

- Implement only a bounded Candidate 1 S01-compatible capture surface over existing accepted constructs.
- Use adapter/view composition over existing events, projections, flags, sync metadata, assignment scope, config, subject refs/history where available, and mobile actor partitions.
- Support standalone capture, optional link to an existing known thing, unlinked/candidate capture as review evidence, offline local save, sync status, failed-sync pending preservation, append-only correction, latest-synced/freshness display, needs-review visibility, and advisory access-ended or stale-access language.
- Include shared-device UX only if FD-PKT-004 evidence is carried and the packet stays inside the accepted single-active-actor, actor-partition, drain-or-seal boundary without retention/security claims.

## Conditions That Must Be Carried Into Any Implementation Packet

- State that Candidate 1 is S01-compatible and excludes S06 lifecycle behavior.
- Include the minimal S06 prerequisite result: known-thing definition for Candidate 1, known-set example evidence, candidate-only validation result, and PM/SME signoff on deferring full lifecycle behavior.
- Name exact files, contracts, accepted constructs reused, targeted tests, manual walkthrough evidence, excluded successor lanes, forbidden work, stop conditions, and commit boundary.
- Keep workshop and scenario material as product/problem evidence only, not implementation authority.
- Preserve CDL/contracts/status/BAR/NW as binding authority.
- Use Candidate 1 wording only as validated UI/product copy, not platform vocabulary, contract fields, API truth, role/action authority, identity categories, config namespaces, or module boundaries.
- Include a claim-wording review that checks for banned lifecycle, production-readiness, retention/security, reporting/export/import, auth/login, custom scope, and automation claims.
- Include state-to-source mapping for all visible statuses before implementation work starts.
- Route before implementation if a shared view model, new endpoint, new response field, contract/schema/fixture change, S06 model, lifecycle state, custom scope, production reporting, or authority change is needed.
- Include manual walkthrough expectations and Product/SME evidence references from FD-PKT-003 and FD-PKT-004 where the touched surface is mobile/offline.
- Keep production readiness lanes separate and visible through FD-PKT-006/007/108 or successor packets.

## Stop/Route Conditions

Stop and route if:

- Candidate 1 cannot remain honest without maintained known things, lifecycle state, discovered-unit stewardship, registry stewardship, duplicate stewardship, merge/split UX, lifecycle words, or candidate promotion.
- Users or SMEs do not understand candidate/unlinked/missing-known-thing capture as review evidence rather than registry creation or lifecycle truth.
- Product copy requires active, inactive, retired, closed, moved, verified, current, canonical, merged, or split as day-one truth.
- Known-set source, field discovery, duplicate handling, verification, closure, movement, merge, or split becomes necessary for the first implementation slice.
- Any implementation packet proposes `lifecycle_status`, `known_thing_status`, `candidate_status`, `registry_state`, `verification_state`, `duplicate_state`, `merge_state`, or `split_state`.
- Any new envelope field, envelope type, schema, protocol meaning, config package key, flag category, platform payload shape, fixture, API response field, durable workflow state, scope mechanism, authority rule, or S06 data model is needed.
- Latest synced becomes live truth, approved truth, full reporting truth, or audit completeness.
- Local mobile warnings become hard rejection or mobile-side authority.
- Failed-sync wording implies data loss, server rejection, token refresh/logout, production login, guaranteed recovery, or retention/security behavior.
- Shared-device wording requires sealed recovery, decommissioning, local encryption, retention/security, or cross-actor visibility.
- Needs-review wording becomes a production queue, direct flag mutation, broad review authority, resolver reassignment, batch bypass, or auto-resolution.
- Persona or acting-context labels become identity categories, access rules, fixed modules, config namespaces, product-area boundaries, authority primitives, or implementation service boundaries.
- S06 is hidden as vague later work without owner, evidence, route, decision point, and named risk.
