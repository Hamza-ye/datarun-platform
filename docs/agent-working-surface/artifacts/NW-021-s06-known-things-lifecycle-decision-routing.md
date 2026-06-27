# NW-021 - S06 Known Things Lifecycle Decision Routing

Status: non-authoritative decision/spec-routing artifact
Document type: product_platform_routing / decision_synthesis
Authority: planning evidence only; does not accept product behavior, platform
behavior, architecture authority, contracts, schemas, BAR/CDL/gap-register
standing, validation policy, runtime implementation, production approval, or
known-set lifecycle behavior by itself
Date: 2026-06-27
Owner: PM product planner / product-platform decision steward
Source: NW-021; BAR-105; M2.1 from NW-168/NW-169;
`docs/agent-working-surface/prompts/NW-021-revisit-s06-entity-lifecycle-decision.md`

## Decision Standing

NW-171 is accepted and no implementation successor is active. NW-021 remains
M2.1 decision/spec-routing work only. The correct durable home for this output
is a non-binding artifact because the work classifies lifecycle pressure and
routes successors; it does not yet accept user-visible lifecycle behavior or
exact platform behavior.

The first implementable route after this artifact should be lifecycle-neutral:
scoped known-thing lookup and confirmation for one configured subject type, with
an optional unpromoted missing-known-thing candidate capture path if the task
packet keeps promotion, rejection, duplicate resolution, merge/split UX,
active/inactive truth, and place-like lifecycle out of scope.

Do not implement full S06 lifecycle from this artifact. Candidate promotion,
canonical lifecycle vocabulary, duplicate stewardship beyond review evidence,
merge/split UX, place-like subject lifecycle, and cross-version reporting
comparability need a selected product/platform spec route first.

## User-Visible Outcome

Users should understand this slice as:

- A worker can look up the real-world thing they are working about.
- If they find the right thing, they can record work about it.
- If they cannot find it, they can save evidence that a possible new thing was
  encountered without making it official immediately.
- A reviewer or steward can later see that evidence and decide the next route.
- Old records remain understandable even if the thing is later merged, split,
  retired, moved, or verified differently.
- Records created under an older information setup remain valid under the setup
  that existed when the record was made.

Architecture terms such as `subject_ref`, projection, alias, lineage, pattern,
or scope explain how the platform can support that outcome. They are not the
primary user language for the first slice.

## Source Evidence Used

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-021,
  NW-036, NW-044, NW-045, NW-053, NW-054, NW-073, NW-166, and NW-171
- `docs/agent-working-surface/baseline-acceptance-register.md` BAR-009,
  BAR-010, BAR-012, BAR-015, and BAR-105
- `docs/agent-working-surface/validation-matrix.md`
- `docs/documentation-organization.md`
- `docs/scenarios/README.md`
- `docs/scenarios/06-entity-registry-lifecycle.md`
- `docs/scenarios/scenario-user-fit-packets/s06-user-fit-maintaining-a-known-set-of-things.md`
- `docs/scenarios/scenario-user-fit-packets/s06b-user-fit-when-the-shape-of-information-changes.md`
- `docs/scenarios/scenario-user-fit-packets/scenario-user-fit-synthesis-across-s00-s01-s06-s06b-access-control-S19.md`
- `docs/specifications/product/product-goal-and-representative-journeys.md`
- `docs/specifications/product/product-model-consolidation-and-slice-backlog.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/specifications/platform/assignment-scope-and-administration.md`
- CDL slices 005, 016, 017, 022 through 027, 030 through 035, 038 through
  050, 055, and 056 via `scripts/query_cdl.py`
- `contracts/pattern-definition.schema.json`
- `contracts/patterns/capture_with_review.v1.json`
- `contracts/patterns/ongoing_resolution.v1.json`
- `contracts/patterns/multi_step_approval.v1.json`
- `contracts/patterns/transfer_with_acknowledgment.v1.json`
- `.review/untracked-user-notes/exploration/further-exploration/known-things-and-registry-lifecycle-product-discovery-charter.md`
  as an evidence-only checklist for noun families and discovery dimensions

## Accepted Current Evidence Preserved

| Evidence | Preserved meaning | Not promoted by NW-021 |
|---|---|---|
| NW-171 | M1.1 lifecycle-neutral field-work proof is accepted; no implementation successor is active. | No known-set lifecycle, candidate promotion, duplicate stewardship, merge/split UX, or place lifecycle. |
| BAR-009 | Merge/split append identity facts exist; aliases/projections preserve original references; merge/split are online-only. | No user-facing merge/split workflow, unmerge, offline merge/split, or automatic duplicate resolution. |
| BAR-010 | Atomic config packages, shape version coexistence, deploy-time validation, and old/new config mechanics are accepted. | No arbitrary migration scripts, historical reinterpretation, or shape-change product workflow. |
| BAR-012 | Platform-owned patterns and projection-derived state are accepted for the current pattern inventory. | No deployer-authored lifecycle state machines or arbitrary lifecycle states. |
| BAR-015 | `location_path` is historical geographic scope/projection infrastructure and is not rewritten after insert. | No semantic location model, place lifecycle authority, or facility/village/warehouse truth. |
| BAR-105 | S06/entity lifecycle is deferred pending product/platform decision. | No lifecycle implementation until the selected route accepts exact behavior. |

## Decision Questions

| Question | NW-021 answer |
|---|---|
| 1. What are user-facing known things? | Real-world things an organization repeatedly recognizes across work: a site, facility, household, person, equipment, asset, organization unit, group, stock/resource node, case-like thing, or other maintained subject when records, assignments, review, or reporting need continuity over time. The first slice should use one configured subject type and product-safe labels such as "known thing", "site", or "record about this thing" depending on the selected deployment. |
| 2. What is merely configured content? | A configured record, list item, option, label, activity name, report grouping, deployment vocabulary, role label, warning expression, shape field, or domain fixture term is not a known thing by itself. It becomes known-thing pressure only when users must look it up, record about it over time, preserve identity/history, assign responsibility to it, or steward ambiguity. |
| 3. What can become a subject/entity? | A thing can become a subject when the selected slice proves it needs durable identity, subject-linked records, lookup/confirmation, scoped access, preserved original references, or lineage. It must not become a subject merely because it appears as a label, enum, report column, location name, or fixture word. "Entity" is not accepted as a user-facing primitive for the first slice; use "known thing" or a deployment-specific noun. |
| 4. What do lifecycle terms mean in user language? | Candidate: possible new thing saved for review, not official. Duplicate: two entries may represent the same thing. Merge: steward says two identities are the same while old references remain understandable. Split: steward separates one identity into successor identities while freezing prior history. Inactive/retired: avoid for new work unless allowed, but keep old work readable. Moved: place or responsibility changed; do not rewrite old records. Replaced: old thing is no longer the thing to use and another may be used going forward. Needs verification: current details are not trusted enough without review. Lifecycle: how the organization keeps the working list usable over time. |
| 5. Which terms are accepted now, examples, or rejected? | Accepted as planning/product language now: known thing, record about this thing, configured record/list/option, candidate known thing, duplicate candidate, needs review, old version, not collected in this version. Examples to test: facility, site, village, district, warehouse, service point, delivery point, household, person, equipment, asset, responsibility unit, group/cohort, stock/resource node, case/process-like thing. Rejected for first slice: core entity table, semantic `location`, arbitrary lifecycle states, deployer-authored state machines/scripts, unmerge, offline merge/split, new `subject_ref` types, IdP-claim authority, request-body actor authority, broad registry import/export, and location/path as place truth. |
| 6. How are place-like things treated? | Default: as geographic scope/projection infrastructure when using current `location` / `location_path`. A facility, village, district, warehouse, service point, or delivery point becomes a place-like subject only when a selected S06 route says users maintain it as a known thing with lookup/history/lifecycle. |
| 7. How is current `location` / `location_path` treated? | It remains server-managed geographic scope/sync/access infrastructure. It is not semantic authority for facilities, villages, warehouses, service points, delivery points, movement, retirement, verification, or lifecycle. Historical `location_path` stays an insert-time scope interpretation, not a current place model. |
| 8. Who may create, verify, deactivate, promote, reject, merge, split, or retire? | Authority must be expressed through authenticated actor, active assignment, role, scope, time, platform-owned command capability, and exact resolver boundaries. Persona labels and IdP claims do not grant authority. First slice: ordinary assigned actors may capture work and, if selected, save unpromoted candidate evidence. Promotion, rejection, verification, deactivation, retirement, and place lifecycle need future platform-owned lifecycle commands or an accepted equivalent. Merge/split remain online-only existing identity operations guarded by accepted identity authority and a future steward UX decision. |
| 9. What must be implemented first and what is deferred? | Implement first only lifecycle-neutral scoped lookup/confirmation for one subject type plus optional unpromoted candidate capture. Defer canonical candidate promotion/rejection, lifecycle vocabulary as truth, duplicate stewardship beyond evidence surfacing, merge/split UX, place-like lifecycle, subject-as-scope expansion beyond current `subject_list`, and broad cross-version reporting until successor routes are selected. |

## Pattern And Projection Mechanism Check

| Pressure | Existing product vocabulary only | Deployer binding to existing pattern | New platform-owned pattern candidate | Projection/read-model over events | Existing identity mechanism | Scope/access evolution | Architecture/CDL change | Result |
|---|---:|---:|---:|---:|---:|---:|---:|---|
| Lookup and confirmation | Yes | No | No | Yes | Yes, via `subject_ref` and aliases | Existing scope only | No | Implementable if lifecycle-neutral and scoped. |
| Missing-known-thing candidate evidence | Yes | No | Maybe later | Yes | Client-generated subject evidence is possible but not canonical | Existing scope only | No if unpromoted | Implementable only as unpromoted evidence. |
| Candidate promotion/rejection | No | No | Yes | Yes | May use identity mechanisms | Needs command authority | Maybe if new truth/refs | Successor spec required. |
| Duplicate candidate review | Yes | Maybe review pattern for evidence only | Maybe later | Yes | Yes for merge/split | Resolver/assignment authority | No for review; maybe for new automation | Review evidence can route first; resolution needs spec. |
| Merge | No | No | No | Alias projection | Yes | Steward authority needed | No if existing contracts suffice | Existing identity mechanism; UX deferred. |
| Split | No | No | No | Lineage projection | Yes | Steward authority needed | No if existing contracts suffice | Existing identity mechanism; UX deferred. |
| Inactive/retired/moved/replaced/needs verification | Yes | No | Yes if reusable lifecycle | Yes | No | Needs command authority | Maybe if stored truth changes | Vocabulary first; truth deferred. |
| Place-like lifecycle | No | No | Maybe | Yes | Maybe | Maybe NW-053 if scope changes | Maybe if semantic location added | Deferred; do not use `location_path` as truth. |
| Shape evolution | Yes | Deployer shape/config binding | No for additive/deprecation | Yes | No | Existing scope | Only for historical reinterpretation | Existing config mechanics; product/reporting spec needed. |
| Cross-version reporting/comparability | Yes | No | No | Yes | No | Existing scoped reporting only | NW-044/architecture if access bypass | Successor reporting/platform route. |
| Offline stale known-thing behavior | Yes | No | No | Yes | Yes for aliases | Existing accept-and-flag | No if accepting stale work | Preserve accept-and-flag; do not reject on device. |

Current pattern inventory does not contain an entity lifecycle pattern. Existing
patterns can support review, approval, long-running resolution, or transfer
work, but they must not be stretched into a generic lifecycle state machine.
If reusable lifecycle behavior is needed, it should be a platform-owned pattern
or platform spec candidate, not deployer-authored lifecycle configuration.

## Required Classification Table

Each row has exactly one primary category.

| Lifecycle pressure | Primary category | Secondary notes |
|---|---|---|
| Known thing lookup and confirmation | product vocabulary / UX decision | Backed by scoped projected lists over accepted subjects/aliases; no broad registry browse. |
| Configured list/record/option versus known thing | product vocabulary / UX decision | Classify labels/options/activities/report groups as configuration unless identity continuity is required. |
| Candidate known thing when lookup fails | product vocabulary / UX decision | Allowed first only as unpromoted candidate evidence; no canonical registry truth. |
| Candidate promotion or rejection | deferred / not needed for selected slice | Needs accepted lifecycle authority/commands and review semantics before runtime implementation. |
| Duplicate candidate review | product vocabulary / UX decision | Review evidence can be shown; matching, resolution, and automatic merge remain deferred. |
| Merge | existing identity mechanism | Online-only, append-only, alias-in-projection; no historical rewrite or unmerge. |
| Split | existing identity mechanism | Online-only split with frozen source history and successors; UX deferred. |
| Inactive/retired/moved/replaced/needs-verification vocabulary | product vocabulary / UX decision | Safe as labels/examples now; platform truth requires successor spec. |
| Lifecycle authority | assignment/scope/access extension | Must use actor plus active assignment plus command/resolver boundary; no persona or IdP claim authority. |
| Place-like subjects | deferred / not needed for selected slice | Keep place lifecycle out of first implementation; route explicitly if selected. |
| Subject-as-scope pressure | assignment/scope/access extension | Existing `subject_list` is allowed; dynamic/query/custom scope routes to NW-053/BAR-108. |
| Shape evolution and old/new record meaning | deployer configuration binding | Existing shape refs, versions, atomic packages, and validation support additive/deprecation paths. |
| Cross-version reporting/comparability | projection/read-model behavior | Needs bounded report labels; broad reporting/API/export routes to NW-044. |
| Offline stale known-thing behavior | projection/read-model behavior | Accept structurally valid stale work, preserve original references, surface review/freshness caveats. |

## Authority Boundary Map

| Pressure | Authority surface | Boundary |
|---|---|---|
| `subject_ref` and preserved original references | CDL / contract authority | CDL-016/CDL-017 and existing contracts govern reference meaning. |
| Merge/split/lineage | CDL / contract authority plus current implementation evidence | CDL-022 through CDL-027 and BAR-009 govern identity correction; NW-021 does not change it. |
| Known-thing product language | planning evidence only | Product/backlog/status docs are evidence until accepted product spec route lands. |
| Lifecycle-neutral lookup | current implementation evidence plus future product/platform spec | Existing subject/projection mechanisms may support it, but product wording and scoped UX still need a selected route. |
| Unpromoted candidate capture | planning evidence only | May be routed as first implementation if it creates no canonical lifecycle truth. |
| Candidate promotion/rejection | unresolved decision gap | Requires accepted product/platform lifecycle authority before implementation. |
| Duplicate review and duplicate queue | accepted platform spec/current evidence for flags plus unresolved product UX | Do not bypass exact resolver authority or automate resolution without NW-045/S06 successor. |
| Lifecycle states as truth | unresolved decision gap | Do not create arbitrary state machines, mutable current-state truth, or deployer-authored lifecycle states. |
| Assignment/scope/access | accepted platform spec | `docs/specifications/platform/assignment-scope-and-administration.md` and CDL-030 through CDL-035/055 govern current authority. |
| Place-like geography | accepted platform spec/current implementation evidence | Current `location` / `location_path` remains scope infrastructure only; place lifecycle is unresolved. |
| Shape versions and config packages | contract/schema authority plus accepted platform spec/evidence | BAR-010, CDL-038 through CDL-046/056, and config specs govern existing mechanics. |
| Pattern registry/projection | contract/schema authority plus BAR-012 | Existing platform patterns are fixed inventory; lifecycle pattern is not accepted. |
| Cross-version reporting | unresolved decision gap / NW-044 if broad | Bounded read-model labels may be platform spec; broad reporting/export/import/API needs NW-044. |
| Runtime implementation | none selected | NW-021 selects no code change by itself. |

## First Implementable Slice

Selected first implementation shape, to be turned into a separate bounded task
packet before code:

`M2.2 lifecycle-neutral known-thing lookup and unpromoted candidate capture`

Minimum user outcome:

1. A field user with current assignment scope can search or select one
   configured subject type they are allowed to work with.
2. The user can confirm "this record is about this thing".
3. If lookup fails, the user can save an unpromoted candidate with enough
   evidence for later review.
4. Sync preserves the original selected or candidate reference and the shape
   version used at capture time.
5. Review surfaces may show "candidate" or "needs review" as evidence standing
   only; they must not promote, reject, merge, split, deactivate, retire, move,
   or verify the thing.

Implementation stop conditions:

- candidate becomes official registry truth;
- candidate affects assignment scope, reporting authority, or future work by
  default;
- inactive/retired/moved/replaced/needs-verification labels become stored
  lifecycle truth;
- merge/split UX or commands are added;
- place-like subjects are modeled through `location` / `location_path`;
- new `subject_ref` types, envelope fields, event types, contract changes, or
  arbitrary lifecycle state machines are proposed;
- lookup requires unscoped browsing, dynamic query scopes, broad audit, or
  report/export access.

## Required Product/Platform Spec Route

Before runtime implementation, create a bounded successor product/platform spec
or task packet for the first slice. It should define:

- product-safe lookup and confirmation wording;
- which configured subject type is in scope;
- what candidate evidence contains and how it is displayed as unpromoted;
- assignment and role/action conditions for capturing candidate evidence;
- stale/offline behavior and freshness labels;
- shape-version expectations for candidate or selected known-thing records;
- validation gates proving scoped lookup, offline capture, sync, and no
  promotion/lifecycle side effects.

If the successor wants promotion, rejection, duplicate stewardship, merge/split
UX, inactive/retired/moved/replaced truth, place lifecycle, or shape-evolution
comparability, it must split those into later specs.

## Successor Route

Selected successor:

`M2.2 - Lifecycle-neutral known-thing lookup and unpromoted candidate capture/review evidence`

Recommended durable home for the successor specification:

- Product behavior and user language: `docs/specifications/product/`
- Exact platform behavior inside current architecture, if needed:
  `docs/specifications/platform/`
- If the route is still comparing alternatives or parking pieces, use another
  bounded artifact before specs.

Candidate follow-on routes after M2.2:

| Route | Trigger |
|---|---|
| M2.3 candidate steward review and promotion/rejection | Users must turn unpromoted candidates into maintained known things or reject them while preserving captured work. |
| S06 lifecycle vocabulary spec | Users need active/inactive/retired/moved/replaced/needs-verification as accepted product behavior. |
| Duplicate stewardship and merge/split UX spec | Users need side-by-side duplicate review or steward-facing merge/split commands. |
| S06b shape evolution spec | Users need v1/v2 rollout, old-shape in-progress behavior, or shape-change warnings. |
| NW-044 reporting boundary | Reports must compare old/new shapes broadly, export, import, warehouse, or expose APIs. |
| NW-053 subject/query/custom scope | Known things become dynamic cohorts, query scopes, custody scopes, or hidden sync scopes. |
| Architecture/CDL decision | Any route proposes new envelope fields/types, new `subject_ref` types, historical reinterpretation, semantic `location`, durable workflow-state truth, or deployer-authored lifecycle state machines. |

## Explicit Deferrals With Trigger

| Deferred item | Reason | Trigger | Route |
|---|---|---|---|
| Canonical candidate promotion/rejection | Changes operational truth and future lookup behavior. | Steward must make a candidate official or reject it. | M2.3 product/platform spec. |
| Active/inactive/retired/moved/replaced truth | Needs product semantics and authority. | New work must avoid, warn on, or route around a lifecycle state. | S06 lifecycle vocabulary spec; architecture if stored truth changes. |
| Duplicate stewardship queue | Review UX and resolver eligibility are not accepted. | Users need duplicate comparison or unresolved duplicate standing. | S06 duplicate stewardship spec; NW-045 for automation/batch. |
| Merge/split UX | Identity mechanism exists but user workflow is not accepted. | Steward must merge or split through product UI. | Merge/split UX product/platform spec; existing BAR-009 boundaries. |
| Place-like subject lifecycle | `location_path` is not semantic place truth. | Facilities, villages, districts, warehouses, service points, or delivery points need lifecycle. | S06 place-like subject route; NW-053 if scope changes. |
| Subject-as-scope beyond explicit lists | Dynamic groups/queries can leak data. | Existing `subject_list` is insufficient. | NW-053/BAR-108. |
| Shape-change lifecycle | Existing mechanics do not define user rollout rules. | Coordinator needs add/deprecate/rename/split/new-shape guidance. | S06b product/platform spec. |
| Cross-version reporting comparability | Reports can create false certainty. | Users compare fields across shape versions or need export/API. | Bounded reporting spec or NW-044. |
| Retention/security of known things | Sensitivity varies by subject type. | Person/household or sensitive known things require local retention/security claims. | NW-054/BAR-106. |
| Import/export of known things | Source-of-truth and compatibility questions are broader. | Legacy registry import, account import, submitted-record replay, or export is selected. | NW-044 plus real-production/data routes as applicable. |

## Rejected / Not A Risk For This Slice

| Item | Disposition |
|---|---|
| Core entity table | Rejected for first slice; not needed for lifecycle-neutral lookup/candidate evidence. |
| Semantic `location` model | Rejected for first slice; current location/path remains geographic scope infrastructure. |
| New envelope fields or event types | Rejected unless a formal architecture route selects them. |
| New `subject_ref` types | Rejected for first slice. |
| Deployer-authored lifecycle state machines or arbitrary states | Rejected; reusable lifecycle must be platform-owned if needed. |
| Mutable current-state truth | Rejected; current views remain derived/projection behavior. |
| Offline merge/split | Rejected by accepted identity boundaries. |
| Unmerge | Rejected by accepted identity boundaries. |
| IdP group/claim/JWT actor authority | Rejected; authority remains explicit principal binding plus assignment/command/resolver boundaries. |
| Request-body actor authority | Rejected. |
| Broad unscoped registry browsing | Rejected; lookup must respect assignment/scope. |
| Treating fixture/domain names as platform vocabulary | Rejected; use them as examples only. |
| Android/device proof gaps from NW-171 | Not a risk for NW-021; NW-171 accepted the needed M1.1 proof. |
| Keycloak hardening | Not selected; route only through NW-166 or cutover-hardening trigger. |

## Residual Follow-Up Visibility

| Residual | Classification | Route or standing |
|---|---|---|
| Draft successor packet/spec for M2.2 lifecycle-neutral lookup plus unpromoted candidate capture | selected successor | Create as the next bounded route before implementation. |
| Candidate promotion/rejection | selected successor only after M2.2 pressure | M2.3 spec if official candidate handling is needed. |
| Lifecycle vocabulary as behavior | explicit deferral with trigger | S06 lifecycle spec when active/inactive/retired/moved/replaced/needs-verification must affect work. |
| Duplicate candidate matching/review | candidate row | S06 duplicate stewardship spec; NW-045 if automation/batch enters. |
| Merge/split steward UX | explicit deferral with trigger | Existing identity mechanism, but UX spec required. |
| Place-like subject lifecycle | explicit deferral with trigger | S06 place-like subject route; do not reuse `location_path`. |
| Dynamic subject scopes/cohorts | explicit deferral with trigger | NW-053/BAR-108. |
| Shape evolution UX and old-shape in-progress behavior | candidate row | S06b product/platform spec. |
| Cross-version reporting/export/import | explicit deferral with trigger | NW-044. |
| Retention/security for sensitive known things | explicit deferral with trigger | NW-054/BAR-106. |
| Runtime code changes in NW-021 | rejected / not a risk | No runtime implementation belongs in this route. |
| BAR/CDL/gap-register edits in NW-021 | rejected / not a risk | Prompt forbids these changes. |

## Validation

Validation category: docs-only/product-planning routing artifact.

Checks run:

- `git diff --cached --check` passed.
- `rg -n "Decision Standing|User-Visible Outcome|Source Evidence Used|Accepted Current Evidence Preserved|Decision Questions|Pattern And Projection Mechanism Check|Required Classification Table|Authority Boundary Map|First Implementable Slice|Required Product/Platform Spec Route|Successor Route|Explicit Deferrals With Trigger|Rejected / Not A Risk|Residual Follow-Up Visibility|Validation" docs/agent-working-surface/artifacts/NW-021-s06-known-things-lifecycle-decision-routing.md` passed.
- `rg -n "NW-171 is accepted|no implementation successor|M2.2|lifecycle-neutral|candidate promotion|location_path|subject_ref|Gap register touched: no|BAR touched: no|CDL touched: no|Contracts touched: no|Runtime code touched: no" docs/agent-working-surface/artifacts/NW-021-s06-known-things-lifecycle-decision-routing.md` passed.

Runtime tests are skipped because NW-021 changes no runtime code, tests,
contracts, schemas, migrations, CI behavior, validation policy, accepted
product/platform behavior, BAR, CDL, gap register, mobile code, server code,
operations policy, sync protocol, authority model, Keycloak posture,
production approval, or known-set lifecycle implementation.

Gap register touched: no.
BAR touched: no.
CDL touched: no.
Contracts touched: no.
Runtime code touched: no.
Artifact trace touched: yes; this artifact is the bounded output. The artifact
index and active control panel were not updated to preserve the prompt's
exactly-one-output boundary in this draft.
Active control panel updated: no.
