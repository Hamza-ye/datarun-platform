# Candidate 1 Gate Consolidation

Status: Project Shepherd gate consolidation

Date: 2026-06-13

Role: Project Shepherd

Authority: none. This consolidation records owner-role gate-review outcomes
and routes the next first-deployment action. It does not authorize
implementation, release, code edits, contract edits, schema edits, API edits,
runtime behavior changes, or S06 lifecycle behavior.

## Source Reviews

| Gate | Owner role | Result |
|---|---|---|
| Product/S06 | Product Manager | `UNBLOCK WITH CONDITIONS`; split S06 into a minimal Candidate 1 honesty prerequisite plus later lifecycle work. |
| UX | UX owner / UX Architect | `UNBLOCK WITH CONDITIONS`; Candidate 1 vocabulary may be user-facing copy only, backed by existing state. |
| Evidence | Test Results Analyzer | Accepted for evidence-gate use; `UNBLOCK WITH CONDITIONS` for bounded packet drafting only. |
| Reality | Reality Checker | `NEEDS WORK`; broad implementation blocked, only a future one-surface packet may be considered. |
| Mobile | Mobile App Builder | Conditional pass; mobile position is `UNBLOCK WITH CONDITIONS` for adapter/view composition only. |
| Architecture | Software Architect + steward accountability | Accepted with conditions; adapter/view composition is defensible, no shared view-model contract/API/schema required for the first slice. |

## Consolidated Gate Decision

Consolidated status: `CONDITIONAL PACKET-DRAFT UNBLOCK`.

Meaning:

- Candidate 1 broad implementation remains blocked.
- Code dispatch remains blocked.
- Release readiness remains blocked.
- S06 full lifecycle remains blocked and routed through BAR-105/S06 successor
  work.
- Project Shepherd may prepare the next non-implementation prerequisite and,
  after it is accepted, draft a one-surface Candidate 1 implementation packet.

The most restrictive owner finding controls the route: Reality Checker marked
the packet set `NEEDS WORK`, and Product Manager required a minimal S06 honesty
prerequisite before implementation dispatch. That prerequisite must be handled
before any implementation packet is dispatched.

## Required Next Action

Next action: draft and review a minimal S06 honesty prerequisite record.

Suggested packet ID: `FD-PKT-101A`.

Owner role: Product Manager + UX owner, with steward accountability.

Objective: prove or reject the narrow claim that Candidate 1 can honestly
proceed without implementing maintained known things, lifecycle states,
registry stewardship, duplicate workflow, candidate promotion, or merge/split
UX.

The prerequisite must record:

- Candidate 1 definition of `known thing` as product copy only.
- At least two deployment-like known-set examples with source, owner, update
  path, offline/stale behavior, and duplicate or lifecycle pain.
- Product/SME signoff that Candidate 1 can use existing known-thing links and
  unlinked/candidate capture evidence without maintaining the known set.
- Vocabulary evidence for linked, unlinked, candidate, missing known thing,
  duplicate suspected, needs review, latest synced, saved locally, failed to
  sync, correction, and access changed.
- Negative vocabulary check proving active, inactive, retired, closed, moved,
  verified, current, canonical, merged, and split are absent from Candidate 1
  truth claims or routed to S06.
- Explicit S06 disposition for Candidate 1: excluded, promoted, split, or
  deferred with risk signoff.

## What May Be Drafted After The Prerequisite

If FD-PKT-101A is accepted with S06 excluded/deferred/split in a way that keeps
Candidate 1 honest, Project Shepherd may draft exactly one bounded
implementation packet for the first Candidate 1 surface.

The first implementation packet should prefer a mobile adapter/view-composition
slice because the owner reviews found no current shared contract/API pressure
and because mobile/offline state is the clearest first user-facing surface.

Allowed first-slice candidates:

- setup/connect copy and active actor display over constrained raw bearer plus
  `/api/auth/me`;
- local pending, waiting to sync, syncing, synced, failed, unauthorized, actor
  drift, no-connection, and saved-on-this-device labels over existing mobile
  state;
- work-list status composition over existing assignments, config, flags,
  projections, timestamps, and local pending state;
- append-only correction wording over existing capture/correction behavior.

Shared-device behavior must be excluded from the first implementation packet
unless it is explicitly claimed and evidence-gated.

## Conditions For Any Implementation Packet

Any Candidate 1 implementation packet must include:

- one bounded surface only;
- exact files/contracts allowed;
- accepted constructs reused;
- state-to-source mapping for every visible label;
- targeted automated test commands for the touched boundary;
- required manual walkthrough evidence;
- excluded successor lanes;
- forbidden work;
- stop/report conditions;
- commit boundary;
- claim-wording checks for lifecycle, production-readiness, live-truth,
  hard-rejection, retention/security, reporting/export/import, auth/login,
  custom-scope, conflict automation, resolver reassignment, and
  auto-resolution overclaims.

## Blocked Or Excluded Work

These remain blocked for Candidate 1 implementation unless separately routed:

- maintained known things, canonical registry creation, lifecycle states,
  discovered-unit lifecycle, registry stewardship, candidate promotion,
  duplicate workflow, automatic matching, merge/split UX, and S06 lifecycle
  contracts/code/tests;
- production web admin auth, online binding-admin UI/API, mobile
  OIDC/Keycloak login, token lifecycle, secure storage, offline re-auth, and
  IdP group/claim/JWT `actor_id` authority;
- retention/security/device lifecycle, local encryption, expiry,
  decommissioning, sealed recovery, redaction/no-local-retention, and
  token/session retention;
- reporting dashboards/APIs/export/import, broad audit/history, aggregate
  access, and reporting freshness semantics;
- conflict review queues, direct flag mutation, batch handling, resolver
  reassignment, pending-match derivations, and auto-resolution;
- custom/query scope, auditor/report filters as authority, special access,
  emergency override, or new scope mechanisms;
- ops readiness, staging rehearsal, TLS/secrets, backup/restore, rollback,
  monitoring, incident response, runbooks, or production-readiness claims.

## Stop Conditions

Stop and route before implementation if:

- Candidate 1 cannot honestly remain S01-compatible without S06 lifecycle
  behavior;
- users or SMEs cannot understand candidate/unlinked/missing-known-thing
  capture as preserved review evidence rather than registry creation;
- any Candidate 1 term becomes an event field, event type, schema/contract
  field, API response meaning, scope mechanism, flag category, durable state,
  authority rule, or shared platform vocabulary;
- latest synced becomes live truth, saved locally becomes server receipt,
  failed sync becomes data loss or server rejection, needs review becomes a
  production review queue, correction becomes history rewrite, or access ended
  becomes deletion/security erasure;
- any implementation proposal needs a new contract, schema, API, fixture,
  shared view-model contract, durable read model, local authority, new scope,
  or S06 data model.
