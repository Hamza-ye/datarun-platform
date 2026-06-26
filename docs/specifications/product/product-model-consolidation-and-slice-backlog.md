# Product Model Consolidation And Slice Backlog

Status: active planning surface
Document type: product_planning
Owner: product steward
Source: NW-169
Authority: derived planning surface only; creates no accepted product behavior, platform authority, implementation scope, runtime behavior, architecture decision, contract/schema/sync change, BAR/CDL/gap-register standing, real-production approval, product-candidate handoff, legacy import, account import, submitted-record replay, or production cutover by itself.
Last reviewed: 2026-06-26
Supersedes: none
Related: docs/specifications/product/product-goal-and-representative-journeys.md; docs/agent-working-surface/product-journey-and-slice-sequencing.md; docs/reviews/scenario-baseline-pressure-map.md; docs/reviews/viability-closure-review.md; docs/scenarios/README.md; docs/scenarios/06-entity-registry-lifecycle.md; docs/scenarios/scenario-user-fit-packets/s06-user-fit-maintaining-a-known-set-of-things.md; docs/scenarios/scenario-user-fit-packets/s06b-user-fit-when-the-shape-of-information-changes.md; docs/specifications/platform/assignment-scope-and-administration.md; docs/agent-working-surface/baseline-acceptance-register.md; docs/agent-working-surface/platform-next-work-backlog.md; docs/status.md

## 1. Current Position Closure

NW-169 closes the planning position after NW-168. It does not reopen accepted
runtime evidence and does not select implementation. The current product
standing is:

- The active Product Goal is one configurable field-operations system for
  local/on-prem production use, not separate health, logistics, stock,
  campaign, facility, or legacy-form products.
- The complete Phase 1 portfolio is in scope for planning: S00-S14, S19,
  S20-S27, and S22. S15, S16, and S18 remain Phase 2/deferred pressure.
- S00/S19, S21/S26, S27/S22, and S23 are accepted control lanes with runtime or
  product evidence. They are regression anchors, not the whole product.
- S06/S06b are the primary unresolved foundational stress test. They are M2
  product pressure, not a generic footnote and not a direct implementation
  target from this document.

### Already Evidenced

The following should be treated as current evidence, not absent work:

| Area | Current evidence | Product interpretation |
|---|---|---|
| Authenticated actor and local mobile login | NW-164 live local Keycloak/web-admin proof and NW-165 live Flutter Android OIDC login proof. | The login/auth component is evidenced for the local issuer path. It is not a production cutover claim and does not harden Keycloak runtime assumptions. |
| Setup/config/package delivery | BAR-010, NW-032/S23, NW-034, NW-068, and NW-088. | Bounded setup, validation, shape version coexistence, atomic package delivery, and production web-admin config workflow exist as components. |
| Assignment and scoped access | BAR-003, BAR-007, NW-050, NW-069, production `/web-admin/assignments`, and S22 evidence. | Assignment-derived responsibility, scope-filtered sync, and assignment-admin command containment are evidenced components. |
| Offline capture, sync, stale authority, and correction | BAR-002, BAR-003, BAR-008, NW-025/S19, NW-026/S00, NW-059, NW-060, NW-061, and NW-062. | Field work can be saved locally, synced, flagged when stale, and corrected append-only as a component. |
| Review, flags, and attention boundaries | BAR-006, BAR-012, BAR-013, NW-029/S21, and NW-072. | Human review, exact resolver behavior, unresolved exclusion, and bounded attention read behavior are evidenced enough for constrained slices. |
| Scoped standing/report inputs | NW-033/S26 and PC3 snapshot evidence. | Freshness inputs, unresolved issue treatment, scoped aggregate visibility, and drill-back are evidenced as bounded read surfaces, not as broad reporting/API/export. |
| Domain-neutral handoff/distribution | NW-030/S27 and NW-042/S22. | Transfer, discrepancy, campaign/grouped-location composition, and subject-history handoff have scenario-grade evidence under current constructs. |
| Identity foundations | BAR-009, identity contracts, subject aliases, merge/split append events, and subject-history backfill. | Subject identity and lineage mechanisms exist. Generic registry lifecycle product behavior does not. |
| Shape/version mechanics | BAR-010, NW-032/S23, NW-034, and configuration package specs. | Records can remain tied to shape versions and devices can receive atomic packages. User-facing shape-change lifecycle and reporting semantics remain partial. |

### Do Not Re-Prove Or Rewrite By Default

Future work should not re-prove these by default:

- append-only event storage, closed envelope type vocabulary, platform payload
  boundary, shape_ref domain discrimination, and idempotent sync;
- assignment containment, scope-filtered pull, subject-history backfill
  separation, and current accepted scope axes;
- OIDC/JWKS validation through explicit active `(issuer, subject) -> actor_id`
  binding and non-authority of IdP groups/claims/JWT actor IDs;
- local mobile OIDC login evidence against the accepted local issuer;
- config package atomicity, deploy-time validation, shape version coexistence,
  and mobile current/pending package promotion;
- exact designated-resolver behavior, flag resolvability defaults,
  accept-and-flag detector ordering, and unresolved-flag exclusion;
- S00/S19, S21/S26, S27/S22, and S23 as accepted control lanes.

Only re-run or extend these when the selected slice changes the touched
surface, uses the evidence as a release gate, or exposes drift.

### Production-Capable As Components

The platform has multiple production-capable components in the selected
local/on-prem lane:

- login/auth and explicit principal binding;
- production web-admin session and command-gated config/assignment surfaces;
- assignment-scoped access and sync;
- mobile actor session, offline capture, local save/sync status, and
  append-only correction;
- bounded review, attention, freshness, scoped standing, and handoff surfaces;
- reference deployment/rehearsal tooling as synthetic reference evidence.

Component-capable does not mean one production-usable journey is closed.

### Not Yet Closed As One Production-Usable Journey

The missing M1 closure is not a missing mechanism. It is the absence of one
accepted product journey that ties the evidenced components together:

1. coordinator sets up one activity;
2. owner/operator provisions one local actor and responsibility;
3. field user logs in on mobile;
4. field user receives scoped work, captures offline, syncs, and corrects;
5. supervisor/owner sees scoped standing, freshness, and attention;
6. operations/support evidence shows how this path is run and diagnosed;
7. explicit non-goals prevent claims about real users/data, legacy import,
   submitted-record replay, retention/security promises, Keycloak hardening,
   and production cutover.

That closure should be the first implementation route considered after
NW-169.

## 2. Leverage Point Analysis

### Smallest Next Intervention

The smallest next intervention that unlocks the most progress is an M1 closure
slice: one owner-operated local/on-prem field-work journey that composes the
already-evidenced auth, setup, assignment, mobile capture/sync, correction,
freshness, review/attention, and operations/support surfaces into one
production-usable path.

This is high leverage because it:

- converts proven components into a user-visible product increment;
- keeps S00/S19, S21/S26, S27/S22, and S23 as regression anchors;
- avoids dragging S06 lifecycle into the first usable baseline;
- creates the acceptance pattern that later M2-M5 slices must satisfy;
- exposes only concrete integration or product-language gaps instead of
  re-opening architecture.

### Leverage Point Versus Implementation Temptation

The leverage point is journey closure. The implementation temptation is to
start building S06 lifecycle or richer registry screens because S06/S06b are
foundational and visibly incomplete.

That temptation is unsafe because the S06 user-fit packet leaves product
decisions open: lifecycle vocabulary, candidate promotion, duplicate review,
edit authority, merge/split UX, verification, and offline missing-known-thing
rules. Implementing those directly would create lifecycle authority by drift.

### M1 Closure Or M2 Precondition Work

The next implementation should be M1 closure, not M2 implementation and not
S06 implementation.

M2 precondition work is still needed, but it should be a decision/spec slice
through S06/BAR-105/NW-021 before any known-set lifecycle implementation. The
recommended sequence is:

1. Select an M1 closure implementation/validation slice.
2. In the M1 stop conditions, forbid any lifecycle, candidate promotion,
   duplicate, merge/split UX, or semantic place authority.
3. Promote an M2 S06/BAR-105/NW-021 decision/spec route when a selected slice
   needs maintained known things, lifecycle vocabulary, or candidate promotion.

## 3. Product Model Consolidation

The product model is a user-facing layer over accepted platform mechanisms. It
does not create platform authority.

| Product concept | User meaning | Current backing | Boundary |
|---|---|---|---|
| Activity | A configured kind of work a person performs. | Config package activity, shapes, role-action maps, optional pattern binding. | Not an event type or authority source by itself. |
| Activity entry | A submitted record of work or observation. | Append-only event with shape_ref, payload, actor_ref, subject_ref, activity_ref. | Not mutable current state. |
| Known thing | A real-world subject users select or maintain when relevant. | subject_ref, identity aliases, subject-history, projections. | Not generic lifecycle authority until S06/BAR-105/NW-021. |
| Candidate known thing | A safe placeholder that lets work continue when lookup fails. | Client-generated subject ID and ordinary capture can support candidate evidence. | Promotion/rejection is not accepted lifecycle behavior yet. |
| Assignment | User-visible responsibility and access. | Assignment events, active assignment projection, scope-filtered sync, command capabilities. | Not IdP group/claim authority and not activity role-action authority. |
| Work item | Something visible for action, review, continuation, or support. | Events, projections, flags, pattern state, assignments. | Not a new task engine, event type, scope, or table by default. |
| Attention item | A visible issue or uncertainty needing review. | Flags, resolver routing, mobile advisory warnings, freshness/projection signals. | Does not imply auto-resolution, batch resolution, or resolver reassignment. |
| Freshness | How current a visible view is relative to sync and event history. | event timestamps, projected timestamps, sync watermarks, config version. | Not a real-time guarantee. |
| Handoff | Continuity across actors, assignments, devices, or custody/responsibility. | Assignment changes, subject-history, transfer pattern, scoped sync. | Not new custody scope or broad audit pull by default. |
| Report view | Scoped read-side standing with uncertainty and traceability. | Rebuildable projections, scoped event access, flag source links. | Not a warehouse, export/import API, or aggregate authority. |
| Shape change | A change in what information is collected. | Versioned shape_ref, config package versioning, deploy-time validation. | Not arbitrary migration language or historical reinterpretation. |

## 4. Interaction Grammar

All future slices should reuse this grammar before inventing screens or
mechanisms:

| Interaction | User action | Visible state | Validation expectation | Stop route |
|---|---|---|---|---|
| Set up work | Configure activity, shape, roles, warnings, review expectations, and rollout. | Draft, validation result, approved/published package, current package. | Config validation, package delivery, role-action boundary, old/new version evidence when touched. | Stop for arbitrary code, scripts, custom context refs, trigger execution, or unsafe shape changes. |
| Assign responsibility | Create or end contained responsibility for actor, scope, and role. | Active, ended, out of scope, stale/offline caveat. | Assignment containment, command capability, actor-bound command, scoped sync. | Stop for new scope, IdP claim authority, mobile authority, emergency/grace scope. |
| Log in and activate actor | User authenticates and app resolves server actor. | Signed in, actor active, session expired, re-login needed. | OIDC/JWKS, explicit principal binding, `/api/auth/me`, mobile actor partition. | Stop for claim/group/JWT actor authority or production cutover claims. |
| Find or select known thing | User searches/selects a subject for work. | Selected, not found, needs verification, duplicate candidate. | Existing subject_ref/history evidence only for lifecycle-neutral lookup. | Stop for promotion, inactive/retired truth, merge/split UX, or semantic place authority without S06 route. |
| Capture offline | User records work under available config. | Saved locally, waiting to sync, synced, failed, stale caveat. | Append-only event, idempotent push, old config version validity, accept-and-flag. | Stop for device rejection of structurally valid state/policy anomalies. |
| Correct | User appends a correction or amended record. | Original retained, correction submitted, review needed when flagged. | Append-only correction, event history, current projection after flag handling. | Stop for mutation, rewrite, or hidden historical edit. |
| Review and resolve attention | Reviewer sees issue and records judgment. | Pending review, accepted, rejected, returned, unresolved, resolver-unassigned. | Exact resolver authority, unresolved exclusion/re-inclusion, scoped access. | Stop for auto-resolution, batch resolution, resolver reassignment, or non-designated resolution. |
| Handoff | Responsibility or custody moves to another actor/scope. | New owner, prior context, pending/stale caveats, discrepancy. | Assignment history, subject-history, transfer pattern, scoped sync. | Stop for custom custody scope or broad historical pull. |
| Report standing | User reads progress, freshness, unresolved items, and trace targets. | Fresh/stale, missing, pending, not collected in this version, unresolved issue. | Scoped projection, freshness inputs, unresolved treatment, event drill-back. | Stop for warehouse/export/import/API, aggregate access bypass, or broad audit. |
| Evolve shape | Coordinator changes information collected. | v1/v2 or product-safe equivalent, changed fields, old work still valid. | Shape version coexistence, old offline work acceptance, validation. | Stop for arbitrary migration, historical reinterpretation, or broad cross-version reporting without route. |

## 5. S06/S06b Risk Analysis

The table breaks entity lifecycle into routeable sub-problems. Existing
evidence means "do not claim absent"; missing product decision means "do not
implement by drift."

| Sub-problem | Existing evidence | Missing product decision | Architecture/platform risk | Likely validation gate | Safe first slice | Stop condition |
|---|---|---|---|---|---|---|
| Known thing lookup | subject_ref, subject-history, assignment-scoped sync, S01-compatible foundations, current admin subject list evidence. | Product vocabulary for "known thing"; lookup confirmation cues; whether lookup is central, mobile, or both. | Search UX may imply registry truth or broad read access beyond current scope. | Scoped lookup only returns authorized subjects; selected subject_ref survives offline capture and sync. | Lifecycle-neutral subject-linked capture in M1 or M2 that selects an existing subject only. | Stop if lookup requires new scope, semantic place authority, broad audit/search, or unscoped registry browsing. |
| Missing-known-thing candidate | Client-generated IDs, append-only capture, accept-and-flag, domain uniqueness, offline work support. | Whether field users create canonical entries or only candidate artifacts; required evidence for "not found." | Candidate can become lifecycle authority or duplicate source without review. | Candidate is visible as unpromoted, traceable, scoped, and reviewable; no automatic canonical promotion. | Unpromoted missing-known-thing candidate tied to one activity/subject type. | Stop if candidate becomes active registry truth, affects assignments/scope, or triggers merge/promotion without BAR-105/NW-021. |
| Candidate promotion/rejection | Conflict resolution and exact resolver behavior; identity merge/split mechanisms exist. | Who can promote/reject; required comparison data; what rejection does to captured work. | Promotion changes operational truth and may affect access, reporting, and future work. | Promotion/rejection emits append-only auditable facts and preserves original references. | Product/platform decision slice only; no runtime implementation from NW-169. | Stop until S06/BAR-105/NW-021 defines lifecycle authority and acceptance tests. |
| Active/inactive/moved/retired vocabulary | Append-only events, projections, subject refs, BAR-015 historical location_path immutability. | Vocabulary and semantics: inactive, closed, moved, retired, replaced, needs verification, active again. | Lifecycle states may be confused with location trees, subject aliases, assignment scope, or deletion. | Historical records remain intelligible; new work avoids or warns on selected lifecycle states under accepted rules. | Decision/spec route defining the minimal lifecycle vocabulary for one subject type. | Stop if lifecycle changes rewrite history, hide references, mutate events, or depend on location_path as semantic truth. |
| Duplicate stewardship | Domain uniqueness flags, manual conflict resolution, identity alias projection, raw-reference detection. | Candidate queue language, matching evidence, stewardship role, when duplicates remain unresolved. | Duplicate UX can bypass exact resolver authority or create destructive auto-merge. | Duplicate candidates stay visible and unresolved until exact authorized review; projections handle unresolved state. | Review-only duplicate candidate list over one subject type. | Stop for automatic merge, batch destructive resolution, or non-designated resolver path without NW-045/S06 route. |
| Merge/split UX | BAR-009; subjects_merged/v1 and subject_split/v1 contracts; merge/split online-only server-validated. | Who sees merge/split; what evidence is shown; correction path for wrong merge; user wording. | UX may hide online-only requirement, imply unmerge, or rewrite historical subject refs. | Online-only command; original refs retained; alias projection shown; split archives source and creates successors. | Product spec/prototype decision for steward-only UX, not implementation. | Stop if UX implies unmerge, offline merge/split, reference rewrite, direct DB edit, or ordinary field action. |
| Shape evolution | BAR-010, NW-032/S23, NW-034, shape_ref versioning, atomic packages, current/pending mobile promotion. | Coordinator rules for add/deprecate/rename/split/new shape; approval and change note expectations. | Tooling may become arbitrary migrations/scripts or change historical interpretation. | v1 records remain valid; v2 applies to new work; old offline v1 work syncs after v2 deployment. | One shape-change slice for additive/deprecation change on one activity. | Stop for arbitrary transformation, custom code, migration semantics, or new expression/context namespace. |
| Old/new comparability | S26 freshness/report inputs; shape versions; reporting route NW-044 visible. | How reports label missing, not collected in this version, deprecated, renamed, and non-comparable. | Reports can mislead users or create aggregate authority beyond detail access. | Report shows version-aware labels and never treats old missing fields as errors by default. | Version-aware review/report wording for one field addition/deprecation. | Stop for broad reporting/API/export/warehouse, aggregate bypass, or historical reinterpretation without NW-044. |
| Lifecycle authority | Assignment, role-action, command capability, exact resolver, auth principal binding. | Who may create, edit, verify, deactivate, promote, merge, split, or retire known things. | Authority can drift into IdP claims, UI-only roles, activity roles, or mobile decisions. | Server-side authenticated actor plus explicit assignment/command authority; mobile advisory only. | Decision/spec route defining lifecycle authority for one subject type. | Stop for IdP group/claim/JWT actor authority, mobile-authoritative lifecycle, or request-body actor authority. |
| Subject-as-scope | Existing subject_list scope, assignment containment, subject-history backfill. | Whether selected known things are merely work subjects or become scope boundaries for responsibility. | Subject selection can become hidden sync scope, query scope, or custom containment. | Existing subject_list assignments authorize only named subject IDs; no dynamic query or semantic group scope. | M1 may use existing subject_list only if already assigned. | Stop for query-as-config, dynamic cohorts, derived subject groups, custody scope, or hidden sync scope without NW-053/BAR-108. |
| Location/path guardrail | Geographic scope axis, materialized path, BAR-015, assignment-scope spec. | Whether place-like subjects need lifecycle separate from geographic scope infrastructure. | location or location_path can be mistaken for facility/village/district/warehouse truth. | Stored location_path is used only for historical geographic access/sync scope; lifecycle goes through S06 route. | Keep M1 geographic scope infrastructure only; no place lifecycle. | Stop if place-like known things, facilities, villages, districts, warehouses, or moved/retired places need lifecycle without S06/BAR-105/NW-021. |

## 6. Location/Path Guardrail

Current `location` / `location_path` behavior is accepted only as geographic
scope and sync/access infrastructure.

It must not be treated as semantic authority for known things, facilities,
villages, districts, warehouses, sites, service points, delivery points, or
entity lifecycle.

Accepted standing:

- `location_path` is server-managed infrastructure metadata for write-time
  geographic access/sync interpretation.
- Historical non-null `events.location_path` is not rewritten after insert.
- Location reparenting or subject-location correction affects future events,
  not prior event scope interpretation.
- Subject-list scope works by subject ID and does not require geographic
  location semantics.

Guardrail:

- If a place-like subject needs active/inactive, moved, retired, merged, split,
  verification, duplicate stewardship, candidate promotion, or lifecycle
  review, route through S06/BAR-105/NW-021 before implementation.
- If a place-like subject requires new scope semantics beyond current
  geographic, subject_list, and activity axes, route through NW-053/BAR-108.

## 7. Ordered Product Backlog

These are candidate slices for selection after NW-169. NW-169 does not select
or start any implementation slice.

No slice may erase, downgrade, ignore, or reclassify accepted evidence. Each
slice must cite the already-evidenced surfaces it relies on and only add new
evidence for the touched behavior.

| Order | Slice | Status | User value | Required decision before implementation | Evidence to preserve | Route |
|---|---|---|---|---|---|---|
| M1.1 | Owner-operated local/on-prem core field-work closure | Implementation-ready after a bounded task packet | One production-usable path: setup, actor binding, assignment, mobile login, offline capture, sync, correction, scoped standing, attention, support evidence. | No new product/platform decision if lifecycle, import, retention/security, Keycloak hardening, and cutover remain non-goals. | NW-164/NW-165, NW-032/NW-088, NW-050/NW-069, NW-025/NW-026/NW-059-NW-062, NW-029/NW-033/NW-072. | First recommended post-NW-169 implementation/validation slice. |
| M1.2 | M1 product-language and support closure | Implementation-ready as part of or immediately after M1.1 | Users and operator see safe language for saved, waiting, synced, stale, scoped, pending review, and support/diagnosis. | None if no new runtime behavior or policy claims. | Current mobile sync/capture copy evidence and operational UX guardrail. | Include in M1.1 if small; otherwise follow-up polish slice. |
| M2.1 | S06 known-things lifecycle decision | Decision first | Defines minimal known-thing model, lifecycle vocabulary, authority, candidate, duplicate, and merge/split UX boundaries. | Yes: S06/BAR-105/NW-021 product/platform decision. | BAR-009 identity mechanisms, BAR-003/007 scope, BAR-015 location_path guardrail. | First M2 precondition; no runtime implementation. |
| M2.2 | Lifecycle-neutral subject-linked capture | Conditional implementation-ready | Field user selects an existing known thing and captures against it without lifecycle changes. | Only if lookup stays scoped and lifecycle-neutral; otherwise M2.1 first. | S01-compatible subject_ref/history evidence. | Can be a narrow bridge slice if M1 needs subject context. |
| M2.3 | Missing-known-thing candidate and steward review | Decision first | Field work continues when lookup fails, and a steward reviews candidate evidence. | Yes: candidate/promotion/rejection authority from M2.1. | Append-only capture, domain uniqueness, exact resolver behavior. | After M2.1. |
| M2.4 | Shape evolution for one activity | Decision/spec first, then implementable | Coordinator deploys v2, old v1 offline work syncs, review/reporting stays version-aware. | Yes: shape-change rules, in-progress behavior, and report labels. | BAR-010/NW-032 shape version mechanics. | M2/S06b platform/product spec slice, then implementation. |
| M3.1 | Supervisor review and attention closure | Implementation-ready after M1 | One review path with pending, accepted, rejected/returned, stale caveats, exact resolver handling. | None if no batch/automation/reassignment. | NW-029/S21, NW-072, BAR-006/012/013. | M3 implementation slice. |
| M3.2 | Responsibility transfer and worker handoff | Implementation-ready after M1 with policy caveats | Successor sees current work plus bounded context; stale work remains visible without false authority. | Retention/offboarding policy only if decommissioning/local-data promises enter scope. | NW-042/S22, PC4 handoff evidence, NW-055 shared-device partitions. | M3 implementation slice; route NW-054 if retention/security expands. |
| M4.1 | Domain-neutral distribution/handoff | Implementation-ready after M1/M3 | Dispatch, receipt, discrepancy, review, and scoped progress for one flow. | None if current scopes and manual review suffice. | NW-030/S27 transfer evidence and NW-072 flag behavior. | M4 implementation slice. |
| M4.2 | Campaign/grouped-location coordination | Decision first if discovered-unit lifecycle or custom campaign scope is needed | Coordinator tracks grouped work, supply flow, reassignment, freshness, and progress. | S06/BAR-105/NW-021 for discovered-unit lifecycle; NW-053 if scope axes are insufficient. | NW-042/S22, assignment/sync/freshness evidence. | M4 constrained implementation only after boundaries are explicit. |
| M5.1 | Scoped recurring report standing | Decision/spec first | Shows due/missing, freshness, unresolved issue treatment, and version-aware labels for one recurring activity. | Recurrence semantics and cross-version labels first; NW-044 for broad reporting/export/API. | NW-033/S26 and PC3 snapshot evidence. | M5 bounded reporting slice. |
| M5.2 | Conditional attention without trigger execution | Deferred/decision first | Shows condition-derived attention as advisory/reporting, not automatic work. | BAR-101/CDL-042 before runtime trigger execution. | Expression and projection evidence. | Start with non-executing report/advisory only. |
| M5.3 | Cross-flow linking | Decision first if new structural references are proposed | Shows related context across two activities. | BAR-107 if new envelope fields are proposed; NW-044 for broad reporting. | subject_ref/activity_ref/payload/config evidence. | Bounded product/platform spec before implementation. |
| M5.4 | Retention/security/device lifecycle posture | Deferred/decision first | Honest support posture for device loss, offboarding, local data, and sensitivity. | NW-054/BAR-106 before retention/security promises. | NW-055/shared-device and selective retention evidence. | Security/platform decision before product claims. |

## 8. NW-167 Mismatch Classification

| Mismatch class | Current examples | Route |
|---|---|---|
| Missing product or UI behavior | M1 journey closure, product-safe wording, known-thing lookup cues, shape-change warnings, version-aware report labels. | Product spec or bounded implementation slice. |
| Missing reusable interaction pattern | Missing-known-thing candidate review, duplicate stewardship, lifecycle review, responsibility handoff vocabulary. | Product decision/spec; implement only after selected. |
| Deployment-specific configuration | Domain labels, activity names, role labels, shapes, warning expressions, severity overrides, campaign/logistics/facility wording. | Configuration and product copy; do not promote to platform vocabulary. |
| Genuine platform-mechanism or architecture gap | Entity lifecycle authority, new scope mechanisms, broad reporting/export/import, trigger execution, auto-resolution, new envelope fields. | S06/BAR-105/NW-021, NW-053, NW-044, BAR-101/CDL-042, NW-045, BAR-107 as applicable. |
| Unsupported assumption requiring evidence | Real users/data, legacy account import, submitted-record replay, Keycloak cutover hardening, retention/security promises, cross-version comparability. | Explicit owner decision, operations route, NW-166, NW-054, or M2/M5 decision slice. |

## 9. Product-Level Definition of Done

Every M1-M5 implementation slice is done only when each applicable row is
answered with evidence, not assertion.

| DoD area | Required outcome |
|---|---|
| User-visible outcome | The target user can complete the named journey step end to end with product-safe language and no fixture/domain term promoted as shared Datarun vocabulary. |
| Security/authorization | Authenticated actor, assignment/scope, command capability, resolver authority, and non-authority boundaries are explicitly checked. IdP claims/groups/JWT actor IDs and UI-only roles do not grant authority. |
| Offline/sync | Offline behavior, local save, sync status, old config behavior, stale authority, subject-history separation, and normal pull watermark impact are checked where relevant. |
| Freshness | The slice states what is current, stale, pending, locally saved, synced, missing, unresolved, or not collected in this version. It avoids false real-time or live-truth claims. |
| Review/attention | Flags, warnings, review states, designated resolver behavior, unresolved exclusion/re-inclusion, and resolver-unassigned handling are visible where relevant. No auto-resolution, batch resolution, or resolver reassignment is implied unless routed. |
| Operations/support | The owner/operator can provision, run, observe, diagnose, and recover the slice within the local/on-prem posture, or the missing operation is recorded as a deferral with trigger. |
| Validation evidence | The selected task packet names touched-surface tests or docs checks, expected manual/probe evidence, and skipped-gate rationale. Passing tests alone do not prove product fitness. |
| Explicit deferrals | Every deferred capability records reason, consequence, trigger, and route. Deferrals must not hide a future risk or call an unselected route "blocked" by default. |

Minimum acceptance text for a future slice:

```text
User-visible outcome:
Security/authorization checked:
Offline/sync checked:
Freshness checked:
Review/attention checked:
Operations/support checked:
Validation evidence:
Explicit deferrals and routes:
Accepted evidence preserved:
Stop conditions:
```

## 10. Recommendation

Recommended first post-NW-169 route:

`M1.1 - Owner-operated local/on-prem core field-work closure`

This is a candidate route for selection, not selected by this document. It is
the first implementation/validation slice because it turns accepted components
into one production-usable journey while keeping S06/S06b routed as M2
decision work.

Recommended first M2 precondition route:

`M2.1 - S06 known-things lifecycle decision through S06/BAR-105/NW-021`

This should be selected before any slice implements candidate promotion,
active/inactive/moved/retired vocabulary, duplicate stewardship, merge/split UX,
or place-like lifecycle semantics.

## 11. Explicit Non-Selections

NW-169 does not select:

- runtime implementation;
- S06 implementation;
- M1 implementation by itself;
- product-candidate handoff;
- accepted product behavior;
- UI/component model acceptance;
- architecture/platform decision;
- contract/schema/sync change;
- BAR/CDL/gap-register change;
- real-production approval;
- domain-specific product vocabulary acceptance;
- location/location_path semantic authority for known things;
- legacy data import;
- account import;
- submitted-record replay;
- production cutover.
