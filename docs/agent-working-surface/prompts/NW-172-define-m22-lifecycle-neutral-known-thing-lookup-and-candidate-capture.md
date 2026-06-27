# NW-172 - Define M2.2 Lifecycle-Neutral Known-Thing Lookup And Candidate Capture

Status: ready prompt
Document type: execution_packet
Owner: PM Product Planner / product-platform boundary steward
Source: NW-172; NW-021 accepted M2.1 decision-routing artifact; BAR-105; S06/S06b; M2.2
Authority: selected product/platform spec or task-packet route only; creates no accepted runtime implementation, contract/schema/sync change, BAR/CDL/gap-register change, lifecycle authority, production approval, real-user rollout, or real-data import/use by itself

## Goal

Define the bounded M2.2 route for lifecycle-neutral known-thing lookup,
confirmation, and unpromoted missing-known-thing candidate capture/review
evidence before any runtime implementation starts.

The output must make the first M2.2 implementation safe to execute without
turning the non-binding NW-021 artifact into authority and without starting
full S06 lifecycle work.

## Decision Carried From NW-021

NW-021 is accepted as M2.1 decision-routing only. Use
`docs/agent-working-surface/artifacts/NW-021-s06-known-things-lifecycle-decision-routing.md`
as the route input and evidence synthesis, not as product behavior authority,
platform behavior authority, or runtime implementation permission.

M2.2 is selected as the next route:

- lifecycle-neutral scoped lookup and confirmation for one configured subject
  type;
- optional unpromoted missing-known-thing candidate evidence when lookup fails;
- review evidence visibility only, not candidate promotion/rejection or
  canonical lifecycle truth.

## User-Visible Outcome To Define

A field user can find and confirm the known thing they are recording about.
If they cannot find it, they can save unpromoted evidence that a candidate
known thing may need later review.

The user-facing copy must be product-safe. Prefer wording such as:

- "Find asset"
- "Confirm this is the asset for this record"
- "I cannot find the asset"
- "Save as candidate for review"
- "Needs review before it can be used as a known asset"

Avoid wording that implies official creation, approval, rejection, lifecycle
truth, registry authority, duplicate resolution, merge, split, place movement,
or semantic location behavior.

## Read First

Required:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-021,
  NW-044, NW-045, NW-053, NW-054, NW-073, NW-166, NW-171, and NW-172
- `docs/agent-working-surface/artifacts/NW-021-s06-known-things-lifecycle-decision-routing.md`
- `docs/agent-working-surface/baseline-acceptance-register.md` BAR-009,
  BAR-010, BAR-012, BAR-015, and BAR-105
- `docs/agent-working-surface/validation-matrix.md`
- `docs/documentation-organization.md`
- `docs/specifications/product/product-model-consolidation-and-slice-backlog.md`
- `docs/specifications/platform/assignment-scope-and-administration.md`
- `docs/scenarios/06-entity-registry-lifecycle.md`
- `docs/scenarios/scenario-user-fit-packets/s06-user-fit-maintaining-a-known-set-of-things.md`
- `docs/scenarios/scenario-user-fit-packets/s06b-user-fit-when-the-shape-of-information-changes.md`

Do not redo NW-021 analysis. Do not read the full CDL unless a stop condition
fires. Use only the smallest relevant CDL slices if boundary wording needs
checking: CDL-016/017, CDL-022 through CDL-027, CDL-030 through CDL-035,
CDL-038 through CDL-041, CDL-055, and CDL-056.

## Required Output

Create one bounded output before runtime implementation:

- a product spec if the main missing authority is user-facing lookup,
  confirmation, candidate wording, and first-slice product behavior;
- a platform spec if the main missing authority is scoped lookup, assignment,
  stale/offline, sync/reference preservation, or review-evidence boundary;
- a successor task packet only if the spec boundary is already sufficiently
  explicit and the next work is a bounded implementation packet.

Do not edit runtime code, tests, contracts, schemas, migrations, sync protocol,
BAR, CDL, or gap-register standing in NW-172. If the work proves one of those
surfaces must change, stop and create a successor route instead of continuing.

Update `docs/status.md`, `docs/agent-working-surface/platform-next-work-backlog.md`,
and the relevant specification or artifact index for the output you create.

## Required Definitions

Define each item explicitly in the output.

1. Product-safe lookup and confirmation wording.
   - Define the noun users see for the first slice.
   - Define lookup empty-state wording.
   - Define selected-known-thing confirmation wording.
   - Define missing-known-thing candidate wording.
   - State which lifecycle, registry, duplicate, merge/split, place, and
     location words are intentionally not accepted.

2. One configured subject type.
   - Use exactly one configured subject type for the first route.
   - Default to configured `field_asset` with user-facing noun "asset" unless
     the evidence you read justifies a different single neutral noun.
   - Treat the type as deployer configuration over existing subject identity,
     not as a platform primitive, new table, new `subject_ref` type, new
     registry model, semantic `location`, or place lifecycle.
   - State whether the type is selected only for M2.2 validation or also for a
     later implementation packet.

3. Unpromoted candidate evidence.
   - Define the minimum evidence captured when lookup fails: display label,
     selected activity/shape context, actor/session provenance, assignment or
     scope context, capture timestamp, stale/offline standing when applicable,
     and the original submitted record reference.
   - Define how review surfaces may show "candidate" or "needs review" as
     evidence standing.
   - State that candidate evidence does not become official registry truth,
     does not affect assignment scope, does not change reporting authority,
     does not become future lookup truth, and does not promote or reject itself.

4. Scoped access and assignment expectations.
   - Define that lookup lists are scoped to the actor's accepted assignment,
     role/action, activity, geography or subject-list scope, and temporal
     standing where relevant.
   - Define who may capture candidate evidence when lookup fails.
   - Define that broad unscoped registry browsing, query-as-config authority,
     IdP-claim authority, request-body actor authority, and hidden sync scope
     expansion are out of scope.

5. Stale/offline behavior.
   - Define what the user sees when the known-thing list is stale, incomplete,
     unavailable, or offline.
   - Define whether an offline user may save candidate evidence and how that
     evidence is marked after sync.
   - Define that stale/offline standing does not grant promotion, rejection,
     duplicate resolution, merge/split, lifecycle, or place authority.

6. Validation gates.
   - Define docs/spec validation for this route.
   - If you create an implementation task packet, define future runtime gates
     that must prove scoped lookup, confirmation, failed-lookup candidate
     capture, offline/stale candidate marking, sync/reference preservation,
     review-evidence visibility, and absence of broad/unscoped lookup.
   - Define negative gates proving no candidate promotion/rejection, canonical
     lifecycle truth, duplicate resolution, merge/split UX, place lifecycle,
     semantic `location`, new `subject_ref` type, or deployer-authored
     lifecycle state machine was introduced.

## Forbidden Work

Do not implement or accept:

- full S06 lifecycle;
- candidate promotion or rejection;
- canonical lifecycle truth;
- duplicate stewardship or duplicate resolution;
- merge/split UX;
- place lifecycle or place-like subject lifecycle;
- semantic `location` behavior;
- new `subject_ref` types;
- deployer-authored lifecycle state machines;
- new contracts, schemas, migrations, sync protocol behavior, event types, or
  envelope fields;
- assignment/scope expansion beyond accepted mechanisms;
- BAR/CDL/gap-register changes;
- runtime code from the NW-021 artifact;
- broad registry import/export, legacy account import, submitted-record replay,
  real-user rollout, real-data import/use, or production cutover.

## Stop Conditions

Stop and route follow-up work instead of continuing if M2.2 cannot be defined
without any of the following:

- candidate promotion/rejection semantics;
- duplicate matching or resolution beyond review evidence;
- merge/split operations or UX;
- a maintained place lifecycle;
- semantic reinterpretation of `location` or historical `location_path`;
- new `subject_ref` types, event meanings, schema/contract changes, or sync
  protocol changes;
- deployer-authored lifecycle state machines;
- broad unscoped lookup or dynamic query scope;
- sensitive person/household retention/security promises.

## Validation To Run

For a docs/spec-only NW-172 result, run:

- `git diff --check`
- `git diff --cached --check`
- `rg -n "NW-172|lifecycle-neutral known-thing|unpromoted candidate|field_asset|asset" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md <created-output-path>`
- `rg -n "promotion|rejection|canonical lifecycle|duplicate|merge|split|place lifecycle|semantic .*location|subject_ref|state machine" <created-output-path>`

Runtime tests are skipped unless a future successor explicitly selects runtime
implementation.
