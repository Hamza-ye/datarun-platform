# NW-021 - Revisit S06 Entity Lifecycle Decision

Status: ready prompt
Document type: execution_packet
Owner: PM Product Planner / product-platform decision steward
Source: NW-021; BAR-105; M2.1 from NW-168/NW-169; post-NW-171 route selection
Authority: selected decision/spec-routing packet only; creates no accepted product behavior, platform authority, runtime implementation, contract change, BAR/CDL/gap-register change, or production approval by itself

## Goal

Select the bounded M2.1 decision route for S06/S06b known things, entity
lifecycle, candidate/duplicate handling, merge/split UX boundaries, and
place-like subject pressure before any lifecycle implementation starts.

The output must let the next implementation route know exactly what can be
built first and what remains deferred, without turning product labels into
authority, identity, scope, contract, storage, or architecture primitives.

## User-Visible Outcome

The product can describe, in user terms, how an organization keeps a known set
of things usable over time: users know what they can look up, what they can
record about, what happens when the thing is missing, duplicated, inactive,
retired, moved, merged, split, or still needs verification, and which parts are
not yet product behavior.

Use product/user language first. Architecture terms such as `subject_ref`,
projection, alias, lineage, state machine, scope, or lifecycle authority may be
used only after the user-facing meaning is clear.

## Read First

Required:

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

Pattern/projection mechanism check:

Required CDL authority slices:

- CDL-005: mechanisms and instances are classified separately
- CDL-016 and CDL-017: references and `subject_ref` contract
- CDL-022 through CDL-027: duplicate identity, merge, split, lineage,
  online-only identity operations
- CDL-030 through CDL-035: assignment-based access, sync scope, authority
  projection, original subject scope, containment, stale authorization
- CDL-038 through CDL-050: configuration gradient, shapes, packages, server
  policy, pattern registry, pattern composition
- CDL-055 and CDL-056: scope mechanism/config split and activity as deployer
  configuration

Do not read the full CDL unless a stop condition fires.

- `contracts/pattern-definition.schema.json`
- `contracts/patterns/`

Evidence-only checklist:

- `.review/untracked-user-notes/exploration/further-exploration/known-things-and-registry-lifecycle-product-discovery-charter.md`
  if present. Use this old owner-supplied untracked charter only as a noun and
  lifecycle discovery checklist. The allowed checklist use is:
  places; people/households; equipment/assets; responsibility units;
  groups/cohorts; stock/resource nodes; cases/process-like things; and the
  discovery dimensions identity, creation source, lookup, details and shape,
  lifecycle states, change/correction, duplicates and ambiguity, offline
  behavior, access/responsibility, and reporting/audit. Do not import its
  conclusions as product vocabulary, architecture, registry UI, lifecycle state
  machine, database model, or platform contract.

Do not read the whole repository by default.

## Decision Questions

The output must answer all of these:

1. What are user-facing "known things" for this product slice?
2. What is merely a configured record, configured list item, option, label,
   activity content, report grouping, or deployment vocabulary?
3. What can become a subject/entity for Datarun behavior, and under what
   evidence or decision boundary?
4. What do candidate, duplicate, merge, split, inactive, retired, moved,
   replaced, needs verification, and lifecycle mean in user language?
5. Which terms are accepted now, which are only examples to test, and which are
   rejected for the first slice?
6. How are place-like things such as facilities, sites, villages, districts,
   warehouses, service points, or delivery points treated?
7. How is current `location` / `location_path` treated as geographic
   scope/projection infrastructure unless the output explicitly routes a
   place-like subject lifecycle decision?
8. Who may create, verify, deactivate, promote, reject, merge, split, or retire
   a known thing, expressed through accepted actor, assignment, role, scope,
   command, and resolver boundaries rather than persona labels or IdP claims?
9. What must be implemented first, if anything, and what remains deferred with a
   trigger?

## Pattern And Projection Mechanism Check

Before treating known-set/entity lifecycle as a core semantic model, evaluate
whether each S06/S06b pressure can be expressed as:

1. existing product vocabulary only;
2. deployer activity/shape/role binding to an existing platform pattern;
3. a new platform-owned pattern mechanism/spec;
4. projection/read-model behavior over existing append-only events;
5. identity mechanism behavior already covered by merge/split/alias decisions;
6. scope/access evolution;
7. architecture/CDL-level change.

Do not assume entity lifecycle requires:

- a core entity table;
- a semantic `location` model;
- new envelope fields or event types;
- new `subject_ref` types;
- deployer-authored lifecycle state machines;
- deployer-authored arbitrary lifecycle states;
- mutable current-state truth.

If a reusable lifecycle pattern is needed, classify it as a platform-owned
pattern candidate that would need durable platform specification and validation,
not plain deployer configuration.

## Required Classification Table

Classify each lifecycle pressure as exactly one primary category, with optional
secondary notes:

- product vocabulary / UX decision
- deployer configuration binding
- platform-owned pattern candidate
- projection/read-model behavior
- existing identity mechanism
- assignment/scope/access extension
- architecture decision required
- deferred / not needed for selected slice

At minimum classify:

- known thing lookup and confirmation;
- configured list/record/option versus known thing;
- candidate known thing when lookup fails;
- candidate promotion or rejection;
- duplicate candidate review;
- merge;
- split;
- inactive/retired/moved/replaced/needs-verification vocabulary;
- lifecycle authority;
- place-like subjects;
- subject-as-scope pressure;
- shape evolution and old/new record meaning;
- cross-version reporting/comparability;
- offline stale known-thing behavior.

## Authority Boundary Map

The output must state, for each major lifecycle pressure, which authority
surface controls it:

- CDL / architecture authority
- contract/schema authority
- accepted platform spec
- accepted product spec
- current implementation evidence
- planning evidence only
- owner/product evidence only
- unresolved decision gap

This map must make clear that product/backlog/status docs are evidence unless
they are accepted durable specs or explicitly route to authority.

## Output

Create exactly one bounded output, choosing the correct durable home:

- If the work is still routing, comparison, evidence synthesis, or deferral,
  create a non-binding artifact under `docs/agent-working-surface/artifacts/`.
- If it accepts user-visible behavior, create or update the product spec under
  `docs/specifications/product/`.
- If it accepts exact platform behavior inside current architecture and
  contract boundaries, create or update the platform spec under
  `docs/specifications/platform/`.
- If it requires architecture/CDL, contract/schema, sync/access, authority, or
  stored event meaning changes, stop and route the smallest architecture
  decision. Do not patch CDL, contracts, BAR, or the gap register inside NW-021
  unless the task is explicitly reselected for that route.

The output must include:

- user-visible outcome;
- source evidence used;
- accepted current evidence preserved;
- the decision/classification table;
- authority boundary map;
- first implementable slice or explicit park decision;
- required product/platform spec route, if any;
- successor route, if any;
- explicit deferrals with trigger;
- rejected/not-a-risk items;
- validation category and skipped runtime-test rationale.

## Preserve Follow-Up Visibility

Every residual discovered during NW-021 must be classified as one of:

- current-slice fix;
- selected successor;
- candidate row;
- explicit deferral with trigger;
- rejected / not a risk.

Do not bury residuals in prose. Do not call unselected work blocked unless the
exact impossible action and evidence are named.

## Forbidden

Do not:

- implement entity lifecycle;
- change runtime code;
- change contracts, schemas, sync protocol, stored event meaning, envelope
  fields/types, BAR, CDL, validation policy, CI, or gap-register standing;
- turn S06 into a broad architecture pass;
- select Keycloak hardening unless NW-166 or a cutover/hardening route is
  explicitly selected;
- block product progress with generic "not production" language;
- promote fixture names, legacy labels, domain examples, persona labels, IdP
  claims, request-body actors, UI-only roles, location labels, or test names into
  platform authority;
- treat current `location` / `location_path` as semantic authority for
  facilities, villages, warehouses, service points, delivery points, or
  lifecycle;
- implement deployer-authored arbitrary state machines or lifecycle scripts.

## Acceptance Criteria

NW-021 is ready for acceptance only when the output:

- confirms NW-171 is accepted and no implementation successor is active;
- preserves NW-021/M2.1 as decision/spec-routing work, not runtime work;
- answers every decision question above;
- classifies every required lifecycle pressure using the required categories;
- states whether a first implementation should be lifecycle-neutral lookup,
  unpromoted candidate capture, steward review, duplicate review, lifecycle
  vocabulary, shape evolution, or an explicit park;
- records what remains deferred with trigger and route;
- states explicitly whether product specs, platform specs, architecture/CDL,
  contracts, BAR, gap register, runtime code, server code, mobile code, and
  operations policy were touched.

## Validation

Run docs-only validation unless NW-021 is explicitly reselected to change a
durable product/platform spec:

```bash
cd /home/hamza/datarun-platform
git diff --check
test -f docs/agent-working-surface/prompts/NW-021-revisit-s06-entity-lifecycle-decision.md
rg "NW-021|M2.1|S06|BAR-105" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/agent-working-surface/prompts/NW-021-revisit-s06-entity-lifecycle-decision.md
rg "known things|candidate|duplicate|merge|split|inactive|retired|location_path|platform-owned pattern candidate" docs/agent-working-surface/prompts/NW-021-revisit-s06-entity-lifecycle-decision.md
```

Runtime tests are skipped for this route-selection prompt because it changes no
runtime code, tests, contracts, schemas, migrations, CI behavior, validation
policy, BAR, CDL, gap-register standing, mobile code, server code, or operations
policy.

## Stop Conditions

Stop and report instead of widening the task if the decision requires:

- a new envelope field, event type, `subject_ref` type, sync protocol field, or
  contract/schema change;
- a new authority source, new scope mechanism, query-as-config containment, IdP
  claim authority, or mobile-authoritative lifecycle decision;
- a semantic location model rather than current geographic scope/projection
  infrastructure;
- a platform-owned lifecycle pattern that must be specified before routing
  implementation;
- broad reporting/import/export/API/warehouse work;
- real users/data, legacy account import, submitted-record replay, retention or
  security promises, Keycloak cutover hardening, or production cutover approval.
