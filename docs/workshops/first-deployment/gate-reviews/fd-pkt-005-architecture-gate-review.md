# FD-PKT-005 Architecture Gate Review

## Header

Role: Software Architect with steward-accountability guardrails.

Date: 2026-06-13.

Gate status: accepted with conditions for Project Shepherd consolidation. This review does not authorize implementation.

Files reviewed:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/first-deployment-task-packet-router.md`
- `docs/workshops/first-deployment/README.md`
- `docs/workshops/first-deployment/stage-4-software-architecture.md`
- `docs/workshops/first-deployment/stage-8-task-packet-backlog.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-002-candidate-1-product-spec-ux-validation.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-003-candidate-1-evidence-plan.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-004-candidate-1-mobile-offline-validation.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-005-candidate-1-view-model-contract-assessment.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-101-s06-entity-lifecycle-discovery.md`

Optional contracts were not opened. The reviewed packets did not create a contract-pressure claim that required deeper contract inspection for this gate.

## Architecture Gate Finding For FD-PKT-005

FD-PKT-005 is acceptable as the Candidate 1 architecture/view-model gate before bounded implementation packet drafting.

The packet correctly classifies Candidate 1 visible states as either existing contract reuse or adapter-level composition over current events, projections, flags, sync metadata, assignment/config state, subject references, and mobile actor partitions. It also names route-needed triggers for shared view-model, contract, schema, API, mobile, authority, S06, and server read-model pressure.

The packet preserves the active routing posture: Candidate 1 implementation remains blocked until FD-PKT-002 through FD-PKT-005 are gated, FD-PKT-101 resolves/promotes/splits/explicitly excludes the S06 dependency, and Project Shepherd consolidates the result into bounded implementation packets.

No architecture drift was found in FD-PKT-005 that requires blocking the view-model gate itself.

## Adapter/View Composition Decision

Default adapter/view composition is defensible for the first implementation slice.

No shared view-model contract, new API, new schema, new fixture, new event vocabulary, durable workflow state, stored current-truth table, or new platform payload shape is required by the current Candidate 1 packet inputs.

The defensible default is:

- mobile UI labels compose from local event store, pending push state, sync metadata, flags/advisories, timestamps, config, assignment visibility, and active actor partition;
- server-facing read surfaces, if any are later proposed, compose current events, projections, flags, assignments, config, sync metadata, and subject refs where already supported;
- product words such as saved locally, waiting to sync, latest synced, needs review, candidate, linked, and unlinked remain user-facing interpretation, not platform facts;
- mobile remains advisory for local warnings and does not become actor, scope, lifecycle, or policy authority.

A shared view-model route is needed only if later implementation inspection proves multiple shipped components need one stable cross-process shape with semantic meaning beyond local adapter mapping.

## Contract/API Pressure Finding

No current server/API contract pressure blocks bounded implementation packet drafting.

The first implementation slice may proceed to packet drafting on the assumption that existing contracts are reused and no API/schema change is planned. That assumption must be explicit in the implementation packet.

Implementation must block and route before coding if any proposal needs:

- new envelope fields, envelope `type` values, platform payload shapes, flag categories, shape-format fields, config package keys, pattern definitions, shared fixtures, or expanded `context.*` refs;
- altered sync semantics, rewritten watermarks, cross-actor pull, broad audit pull, report/export/import APIs, or new sync/status endpoints;
- new API response fields that encode Candidate 1 UI labels as platform facts;
- stable shared JSON view-model contracts consumed by multiple shipped surfaces;
- durable workflow state, stored current truth, lifecycle status, candidate promotion state, registry state, or report aggregate truth;
- custom/query scope, auditor/report scope, special access, emergency override, or any authority movement away from authenticated actor plus assignment-derived access;
- conflict review queues, direct flag mutation, resolver reassignment, batch handling, or auto-resolution.

## Required Conditions For Any Implementation Packet

Any later implementation packet must be written as one bounded surface and must include:

- exact files and contracts allowed;
- accepted constructs reused;
- state-to-source mapping for every visible Candidate 1 label;
- explicit statement that no shared view-model contract/API/schema is being introduced unless routed first;
- targeted tests and manual walkthrough evidence appropriate to the touched surface;
- excluded successor lanes;
- forbidden work;
- stop/report conditions;
- commit boundary.

The implementation packet must keep Product/SME vocabulary validation separate from platform vocabulary. Acting-context labels must continue to map through actor, active assignment, role, scope, time, activity/context, available actions, visible data, and projected surface.

Project Shepherd should not dispatch implementation until FD-PKT-101 has chosen, promoted, split, or explicitly excluded/deferred the S06 dependency for Candidate 1 honesty.

## Architecture Claims That Remain Blocked/Excluded

The following remain outside Candidate 1 and must not enter implementation through view-model naming:

- S06/entity lifecycle, maintained known-set source, discovered-unit lifecycle, lifecycle status, registry stewardship, candidate promotion, duplicate workflow, automatic matching, and merge/split UX;
- new scope mechanisms, subject/query/custom scope, auditor/report filters as authority, special access, and emergency override semantics;
- reporting dashboards, reporting APIs, export/import, broad audit/history access, aggregate access divergence, and report freshness semantics;
- conflict automation, production review queues, batch handling, resolver reassignment, direct flag mutation, pending-match derivations, and auto-resolution;
- production web admin auth, online binding-admin UI/API, mobile OIDC/Keycloak login, token lifecycle, secure storage, offline re-auth, and IdP group/claim/JWT `actor_id` authority;
- retention/security/device lifecycle, local encryption, expiry, decommissioning, sealed recovery, redaction/no-local-retention, and token/session retention policy;
- durable workflow-state tables, stored current-truth tables, deployer-authored state machines, deployer-authored access logic, scripts, custom traversals, device-side triggers, and new pattern inventory;
- ops readiness, constrained-deployment release claims, TLS/secrets, backup/restore, migration rollback, monitoring, incident response, runbooks, and staging rehearsal.

## Explicit Implementation Unblock Position

UNBLOCK WITH CONDITIONS.

This position unblocks Project Shepherd consolidation and bounded implementation packet drafting only. It does not authorize implementation, commits, tests, code edits, contract edits, schema edits, API edits, or runtime behavior changes.

The architecture position is that FD-PKT-005 clears the view-model/contract gate because adapter/view composition is sufficient by default and no current shared contract/API/schema pressure was found. The unblock remains conditional on the required implementation-packet conditions above and on S06 being resolved, promoted, split, or explicitly excluded/deferred before dispatch.

## Stop/Route Conditions

Stop and route through Project Shepherd if any bounded implementation packet:

- cannot keep Candidate 1 S01-compatible without S06 lifecycle behavior;
- needs known-set source authority, lifecycle vocabulary as truth, registry stewardship, candidate promotion, duplicate workflow, or merge/split UX;
- turns a UI/product term into an event field, event type, contract/schema field, API response meaning, scope mechanism, flag category, durable state, authority rule, or shared platform vocabulary;
- requires server/API changes beyond adapter/read composition over existing accepted constructs;
- makes mobile warnings authoritative rejection or creates local actor/scope/lifecycle authority;
- makes latest synced live truth, saved locally server receipt, needs review a production queue, failed sync data loss, correction history rewrite, or access ended deletion/security erasure;
- introduces production auth/admin/mobile login, retention/security, reporting/export/import, conflict automation, custom/query scope, ops readiness, or production-readiness wording;
- treats persona or operational labels as identity categories, authority primitives, config namespaces, fixed modules, product-area boundaries, access rules, or service boundaries.

Route targets:

- FD-PKT-101/BAR-105 for S06/entity lifecycle, known-set source, candidate promotion, duplicate handling, lifecycle vocabulary, registry stewardship, and merge/split pressure.
- FD-PKT-102/103 for production web admin auth, online binding-admin UI/API, mobile OIDC/Keycloak login, token lifecycle, secure storage, offline re-auth, or IdP claim/group authority.
- FD-PKT-104 for retention/security/device lifecycle, local encryption, expiry, decommissioning, sealed recovery, redaction/no-local-retention, or token/session retention policy.
- FD-PKT-105 for reporting dashboards/APIs/export/import, broad audit/history, aggregate access, or report freshness semantics.
- FD-PKT-106 for conflict review queues, direct flag mutation, batch handling, resolver reassignment, pending-match derivations, or auto-resolution.
- FD-PKT-107 for subject/query/custom scope, auditor/report filters as authority, special access, or emergency override semantics.
- FD-PKT-006/007/108 for ops runbooks, staging rehearsal, monitoring, incident response, backup/restore, migration rollback, constrained-deployment evidence, or production-readiness claims.
