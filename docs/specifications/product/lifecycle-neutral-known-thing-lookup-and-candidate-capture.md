# Lifecycle-Neutral Known-Thing Lookup And Candidate Capture

Status: accepted
Document type: product_spec
Owner: product steward
Source: NW-172 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-172-define-m22-lifecycle-neutral-known-thing-lookup-and-candidate-capture.md`
Authority: accepted for M2.2 user-visible lookup, confirmation, unpromoted candidate evidence language, first-slice subject-type selection, scoped access expectations, stale/offline product behavior, and validation gates only; operates within accepted contracts, BAR standing, `docs/specifications/platform/assignment-scope-and-administration.md`, `docs/specifications/platform/configuration-package-and-shapes.md`, and `docs/agent-working-surface/artifacts/NW-021-s06-known-things-lifecycle-decision-routing.md` as routing evidence
Last reviewed: 2026-06-27
Supersedes: none
Related: `docs/specifications/product/product-goal-and-representative-journeys.md`; `docs/specifications/product/product-model-consolidation-and-slice-backlog.md`; `docs/agent-working-surface/artifacts/NW-021-s06-known-things-lifecycle-decision-routing.md`; `docs/agent-working-surface/baseline-acceptance-register.md`; `docs/scenarios/06-entity-registry-lifecycle.md`; `docs/scenarios/scenario-user-fit-packets/s06-user-fit-maintaining-a-known-set-of-things.md`; `docs/scenarios/scenario-user-fit-packets/s06b-user-fit-when-the-shape-of-information-changes.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/configuration-package-and-shapes.md`

## Purpose

This specification defines the M2.2 product/platform boundary for the first
lifecycle-neutral known-thing lookup route. It lets a field user find and
confirm the known thing they are recording about. If they cannot find it, the
user can save unpromoted evidence that a candidate known thing may need later
review.

This is the durable product output for NW-172. It does not implement runtime
behavior, create a registry lifecycle, accept candidate promotion or rejection,
change contracts, change schemas, change sync protocol behavior, change BAR,
CDL, or gap-register standing, approve real users/data, or authorize production
cutover.

## User Problem And Actors

The first M2.2 slice addresses one narrow user problem:

```text
I need to record work about the right known thing. If I cannot find it in the
list I am allowed to use, I need a safe way to keep working and leave evidence
for later review without making a new official thing.
```

Actors are product roles only. They do not grant platform authority:

| Actor | Product responsibility in M2.2 | Authority boundary |
|---|---|---|
| Field user | Search or select an assigned asset, confirm the record is about it, or save missing-asset candidate evidence. | Authenticated actor session plus current accepted assignment, role/action, activity, scope, and stale/offline checks. |
| Reviewer or steward | See candidate or needs-review evidence after sync and decide whether follow-up routing is needed. | Review visibility only in M2.2; no promotion, rejection, merge, split, duplicate resolution, or lifecycle command is accepted. |
| Coordinator/setup owner | Configure the first subject type and activity/shape context used by the slice. | Deployer configuration over existing subject identity and accepted config-package behavior; not a new platform primitive. |

## Included Behavior

M2.2 accepts this product behavior for the first slice:

1. A field user can find an asset from the known things already available in
   the user's assigned work context.
2. The user can confirm that the current activity entry is about the selected
   asset.
3. If the user cannot find the asset, the user can save candidate evidence for
   review while continuing the activity entry.
4. Candidate evidence is visible later as `candidate` or `needs review`
   evidence standing.
5. Offline or stale-list use is allowed only with visible caveats and later
   review marking; it does not create new authority.

This specification accepts product language and validation expectations. It
does not choose a screen layout, API shape, storage table, sync payload change,
or implementation technique.

## Product-Safe Wording

The first-slice user-facing noun is `asset`. The configured subject type is
`field_asset`.

Preferred user-facing copy:

| Situation | Accepted wording |
|---|---|
| Lookup action | `Find asset` |
| Lookup helper | `Search the assets available for this work.` |
| Empty state | `No matching assets in your assigned work.` |
| Stale list caveat | `This asset list may be out of date.` |
| Offline caveat | `You are using the last saved asset list.` |
| Selected confirmation | `Confirm this is the asset for this record.` |
| Missing action | `I cannot find the asset.` |
| Candidate save action | `Save as candidate for review.` |
| Candidate standing | `Needs review before it can be used as a known asset.` |
| Review-surface label | `Candidate asset` or `Needs review` |

The first M2.2 route intentionally does not accept these as user-facing product
claims or platform behavior:

| Word or phrase | Boundary |
|---|---|
| `Create asset`, `official asset`, `approved asset`, `registered asset` | Implies canonical creation, approval, or registry truth. Use candidate wording instead. |
| `Reject candidate`, `approve candidate`, `promote candidate` | Candidate promotion/rejection is a later M2.3 route. |
| `Duplicate`, `same as`, `merge`, `split`, `unmerge` | Duplicate stewardship and merge/split UX are not accepted in M2.2. |
| `Active`, `inactive`, `retired`, `closed`, `moved`, `replaced`, `verified` | Lifecycle truth is not accepted in M2.2. |
| `Facility registry`, `place registry`, `location lifecycle` | Place-like lifecycle and semantic `location` behavior are not accepted in M2.2. |
| `subject_ref`, projection, alias, lineage, state machine | Architecture/platform terms are not user-facing product copy for this route. |

## Configured Subject Type

The first route uses exactly one configured subject type:

| Field | M2.2 selection |
|---|---|
| Configured subject type | `field_asset` |
| User-facing noun | `asset` |
| Selection standing | Selected for M2.2 validation and for a later bounded implementation packet if that packet is selected. |
| Authority standing | Deployer configuration over existing subject identity. |

`field_asset` is not:

- a platform primitive;
- a new table;
- a new event envelope field;
- a new `subject_ref` type;
- a registry model;
- semantic `location` behavior;
- a place lifecycle;
- an accepted lifecycle state machine.

Future subject types may be configured by later selected routes, but M2.2
validation uses only `field_asset` so acceptance is not confused with a generic
registry lifecycle.

## Unpromoted Candidate Evidence

When lookup fails, M2.2 allows saving unpromoted candidate evidence. The
minimum evidence for the first implementation packet is:

| Evidence item | Meaning |
|---|---|
| Display label | The user's label for the possible asset, shown as review evidence. |
| Activity context | The selected activity and shape context used for the submitted record. |
| Actor/session provenance | The authenticated actor/session that saved the candidate evidence. |
| Assignment or scope context | The assignment/scope context available to the actor at capture time or sync review time. |
| Capture timestamp | The field capture time for the candidate evidence. |
| Stale/offline standing | Whether the lookup list was stale, incomplete, unavailable, or offline. |
| Original submitted record reference | The activity entry or submitted record that contains or references the candidate evidence. |

Review surfaces may show the evidence as `candidate`, `candidate asset`, or
`needs review`. They may show the originating record, actor/session provenance,
scope context, and stale/offline caveat needed to understand the evidence.

Candidate evidence does not:

- become official registry truth;
- add an asset to future lookup lists by itself;
- affect assignment scope;
- change reporting authority;
- change canonical lifecycle standing;
- promote or reject itself;
- create duplicate-resolution, merge, split, place, or lifecycle authority.

## Scoped Access And Assignment Expectations

Lookup and candidate capture must remain inside accepted assignment and access
boundaries.

Lookup lists are scoped to the authenticated actor's accepted assignment
standing:

- assignment-derived geography, subject-list, and activity axes;
- temporal standing, including stale offline work caveats where relevant;
- role/action expectations for the selected activity;
- current request-time scope for online lookup and sync review;
- local assigned work context for offline lookup, with visible caveats.

A field user may save candidate evidence only when the user is authenticated or
operating inside an accepted local actor session and the activity entry itself
is structurally valid under the user's available configuration and assigned
work context. Stale or ended authority is handled as accepted stale/offline
standing and later server-side review or flag evidence, not as mobile-created
authority.

M2.2 does not authorize:

- broad unscoped registry browsing;
- query-as-config authority;
- dynamic subject cohorts;
- hidden sync scope expansion;
- IdP group/claim/JWT actor authority;
- request-body actor authority;
- UI-only role authority;
- assignment/scope expansion beyond accepted geography, subject-list,
  activity, and temporal mechanisms.

If a later route cannot implement M2.2 with accepted assignment axes, it must
route through NW-053/BAR-108 before runtime work.

## Stale And Offline Behavior

The product must make list freshness visible without blocking safe work by
default.

| Condition | User-visible behavior | Candidate behavior |
|---|---|---|
| Current online list | User can find and confirm an assigned asset. | Missing asset can be saved as candidate evidence. |
| Stale list | User sees that the asset list may be out of date. | Candidate evidence carries stale-list standing. |
| Offline list | User sees that the last saved asset list is being used. | Candidate evidence may be saved offline and carries offline standing after sync. |
| Lookup unavailable | User sees that assets cannot be refreshed now. | Candidate evidence may be saved if the activity entry is otherwise valid. |
| Incomplete scoped list | User sees no matching assets in assigned work, not a global absence claim. | Candidate evidence records the scoped/incomplete-list context. |

Offline candidate evidence is allowed for this route because M2.2 is evidence
capture, not official asset creation. After sync, the candidate remains
candidate or needs-review evidence until a later selected route defines any
promotion, rejection, duplicate, or lifecycle handling.

Stale/offline standing does not grant:

- candidate promotion or rejection;
- duplicate resolution;
- merge/split authority;
- active/inactive/retired/moved/replaced/verified lifecycle authority;
- place lifecycle authority;
- reporting or assignment scope authority;
- new subject identity or `subject_ref` semantics.

## Product Acceptance Criteria

A future M2.2 implementation packet must prove these user outcomes:

1. A field user can find one authorized `field_asset` and confirm that the
   record is about that asset.
2. The selected asset reference survives local save, sync, review visibility,
   and trace display without broad lookup or hidden scope expansion.
3. A field user who cannot find the asset can save candidate evidence with the
   required minimum evidence fields.
4. Candidate evidence is visible later as candidate or needs-review evidence
   only.
5. Stale, incomplete, unavailable, and offline lookup conditions show product
   caveats and preserve the evidence standing after sync.
6. Scope and authority checks use accepted authenticated actor, assignment,
   role/action, activity, geography, subject-list, and temporal boundaries.
7. The implementation proves the absence of candidate promotion/rejection,
   canonical lifecycle truth, duplicate resolution, merge/split UX, place
   lifecycle, semantic `location`, new `subject_ref` types, and deployer-authored
   lifecycle state machines.

## Platform And Contract Guardrails

| Surface | M2.2 guardrail |
|---|---|
| Product vocabulary | Use `asset`, `candidate asset`, and `needs review`; do not expose architecture vocabulary to field users. |
| Subject identity | Use existing subject identity and preserved references only; no new `subject_ref` type. |
| Assignment/access | Use accepted assignment-derived geography, subject-list, activity, and temporal boundaries. |
| Sync/offline | Preserve original selected or candidate reference and stale/offline evidence; do not rewrite normal sync watermarks. |
| Config/shape | Use existing deployer configuration and shape/version mechanics; no schema or contract change in NW-172. |
| Review evidence | Show candidate or needs-review evidence only; no candidate promotion/rejection command. |
| Location | Current `location` and `location_path` remain geographic scope infrastructure, not semantic place truth. |
| Reporting | Candidate evidence does not change reporting authority or future lookup truth. |
| Authority | No IdP claims, request-body actor IDs, UI-only roles, or mobile decisions become authority. |

## Explicit Non-Goals

M2.2 does not include:

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

## Validation Gates

NW-172 itself is a docs/spec route. Required validation for NW-172 acceptance:

```bash
git diff --check
git diff --cached --check
rg -n "NW-172|lifecycle-neutral known-thing|unpromoted candidate|field_asset|asset" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/specifications/product/lifecycle-neutral-known-thing-lookup-and-candidate-capture.md
rg -n "promotion|rejection|canonical lifecycle|duplicate|merge|split|place lifecycle|semantic .*location|subject_ref|state machine" docs/specifications/product/lifecycle-neutral-known-thing-lookup-and-candidate-capture.md
```

A later implementation packet must add focused runtime validation for:

- scoped lookup returning only authorized `field_asset` subjects;
- confirmation preserving the selected asset reference through local save,
  sync, review visibility, and trace display;
- failed-lookup candidate capture with all minimum evidence fields;
- stale-list, incomplete-list, lookup-unavailable, and offline candidate
  marking;
- sync/reference preservation without contract/schema/sync protocol changes;
- review-evidence visibility as candidate or needs-review only;
- absence of broad/unscoped lookup or hidden sync scope expansion.

Negative runtime gates must prove no:

- candidate promotion or rejection;
- canonical lifecycle truth;
- duplicate resolution;
- merge/split UX;
- place lifecycle;
- semantic `location` behavior;
- new `subject_ref` type;
- deployer-authored lifecycle state machine.

Runtime tests are skipped for NW-172 because this route changes only durable
product documentation, status, backlog, and the product spec index.

## Deferrals And Wake-Up Conditions

| Deferred item | Why not M2.2 | Wake-up trigger | Route |
|---|---|---|---|
| Candidate promotion/rejection | Changes official truth and future lookup behavior. | A steward must make a candidate usable as a known asset or reject it. | M2.3 product/platform spec. |
| Duplicate stewardship or resolution | Requires comparison rules, resolver authority, and user workflow. | Users need duplicate review, matching, or same-thing decisions. | S06 duplicate stewardship route; NW-045 if automation/batch enters. |
| Merge/split UX | Existing identity mechanisms are not a field-user workflow. | Steward-facing merge/split commands are needed. | Merge/split UX product/platform spec under BAR-009 boundaries. |
| Lifecycle vocabulary as truth | Active/inactive/retired/moved/replaced/verified affects future work. | Work must avoid, warn, route around, or report lifecycle standing. | S06 lifecycle vocabulary spec. |
| Place-like lifecycle | `location_path` is not semantic place truth. | Facilities, villages, warehouses, service points, or delivery points need lifecycle. | S06 place-like subject route; NW-053 if scope changes. |
| Shape evolution behavior | M2.2 only preserves activity/shape context as evidence. | Users need v1/v2 rollout, old-shape work handling, or report comparability. | S06b product/platform spec; NW-044 for broad reporting/export/API. |
| Retention/security promises | Subject sensitivity depends on type and deployment policy. | Person/household or sensitive asset handling needs a claim. | NW-054/BAR-106. |
| Import/export or legacy replay | Source-of-truth and compatibility questions are broader. | Legacy registry import, account import, submitted-record replay, or export is selected. | NW-044 plus selected real-production/data route. |

## Successor Standing

NW-172 selects no runtime implementation by itself. A later M2.2 implementation
packet may use this specification as the product boundary if it keeps to
`field_asset`, lifecycle-neutral lookup, unpromoted candidate evidence, accepted
assignment/scope mechanisms, and the validation gates above.
