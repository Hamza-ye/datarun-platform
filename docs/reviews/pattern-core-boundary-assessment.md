# Pattern Core Boundary Assessment

Status: review / stewardship assessment

Date: 2026-06-06

Authority note: this review does not change CDL authority, IDR status, BAR
status, contracts, schemas, APIs, runtime behavior, backlog priority, or
accepted baseline standing. It is a bounded input for pattern-facing work,
NW-044 reporting/import-export exploration, and NW-046 flag-cascade/pattern
traversal exploration. The CDL remains the architecture authority.

## 1. Direct Assessment

The reading is correct.

The current architecture treats the event store, envelope, append-only
immutability, sync, assignment-derived access, identity, conflict detection,
resolver semantics, flag lifecycle, and rebuildable projection rule as core
platform capabilities. They are high-irreversibility surfaces and must stay
stable, reusable, and non-negotiable unless the CDL is deliberately evolved.

Patterns are not another copy of that core. IDR-020 through IDR-025 keep
patterns as platform-owned bounded mechanisms that use core capabilities
through contracts:

- pattern state is derived from events, activity bindings, and platform pattern
  definitions;
- events do not store `pattern_ref`, `workflow_state`, `current_state`, or
  `status_changed`;
- deployers bind activities to platform-owned pattern definitions, but do not
  author transition tables or pattern mechanisms;
- transition invalidity is accepted and flagged through the normal conflict
  model, not rejected and not resolved by a private pattern path;
- activity role-actions, assignment administration, scope containment, and
  resolver authority remain platform capabilities outside pattern ownership;
- config packages deliver referenced pattern definitions so server and mobile
  interpret the same contract version.

So the current path is a safe go, with a guard. It preserves the intended
boundary today, but pattern-specific projection behavior is the live coupling
watchpoint. If every future pattern requires mirrored server/mobile code
branches or if product reporting treats `pattern_specific` fields as generic
platform API concepts too early, the bounded model can collapse into a hard
core without an explicit decision.

## 2. Evidence Checked

Primary authority and routing:

- `docs/status.md` current routing: IDR-020 through IDR-030 are active, BAR-012
  through BAR-014 are accepted, and NW-044/NW-046 remain future-decision
  routes.
- `docs/agent-working-surface/README.md`: future work must stop on new envelope
  fields/types, durable workflow-state authority, deployer-authored state
  machines, trigger/auto-resolution/resolver-reassignment promotion, or
  reporting/import-export promotion without successor decision.
- `docs/implementation/module-interfaces.md`: Projection Engine and Pattern
  Registry own rebuildable projection and platform pattern definitions, with
  durable workflow-state tables and deployer-authored state machines forbidden.
- `docs/agent-working-surface/architecture-rationale-and-routing-companion.md`:
  platform mechanisms must stay separate from deployer instances, and
  configuration must not become a deployer-authored processing pipeline.

Pattern and adjacent IDRs:

- IDR-020 selects platform-bundled pattern definitions, activity-level pattern
  bindings, projection-derived state, accept-and-flag transition evaluation,
  and no event/envelope workflow status.
- IDR-021 keeps role-action enforcement activity-scoped and separate from
  pattern state.
- IDR-022 keeps domain uniqueness, severity, and detection ordering outside
  pattern transitions.
- IDR-023 keeps assignment administration out of activity role-actions while
  allowing pattern projection to consume assignment facts where the platform
  pattern needs them.
- IDR-024 keeps assignment containment and scope authority as core platform
  access behavior, not pattern behavior.
- IDR-025 makes pattern definitions canonical platform contract artifacts under
  `contracts/patterns/`, delivered through config packages and preserved by
  mobile.
- IDR-026 routes `transition_violation` through the normal single-writer
  conflict resolver model.

Code and contract reality:

- `contracts/pattern-definition.schema.json` defines pattern semantics,
  states, transitions, and projections as platform-owned contract content.
- Server and mobile projection engines both use generic binding parsing,
  transition matching, unresolved-flag exclusion, and derived output. This
  supports the boundary.
- Server and mobile also contain explicit `binding.ref` branches for
  `pattern_specific` projection fields:
  `server/src/main/java/dev/datarun/server/projection/PatternStateProjection.java`
  lines 605-684 and
  `mobile/lib/data/pattern_projection.dart` lines 540-628. This is acceptable
  for the current bounded catalog, but it is the strongest coupling signal.

## 3. Core vs Pattern Boundary

| Surface | True core capability | Must remain bounded pattern-level construct |
| --- | --- | --- |
| Event truth | Append-only event store, idempotent insert, immutable envelopes, sync watermarks, event ordering. | Pattern progress/status must never become event truth. |
| Envelope vocabulary | Closed event `type` vocabulary, required envelope fields, `shape_ref`, `activity_ref`, `actor_ref`, `subject_ref`, causal metadata. | No `pattern_ref`, `workflow_state`, `current_state`, `status_changed`, report status, or pattern-specific envelope fields. |
| Shape/config interpretation | Versioned `shape_ref`, deployer form-shape DSL, atomic config package delivery, activity configuration. | Pattern bindings reference platform patterns; deployers do not define transitions, effects, projection semantics, or pattern code. |
| Projection model | Derived/rebuildable projections; flagged-source exclusion; server/mobile projection equivalence where mobile needs offline behavior. | Pattern state JSON, current state, pending time, progress markers, and `pattern_specific` fields are derived view data. |
| Pattern registry mechanism | Platform-owned registry, pattern-definition schema, versioned pattern refs, config package delivery. | The catalog entries, states, transitions, participant roles, parameters, and pattern-specific projection fields can evolve by new pattern versions or bounded platform release. |
| Transition detection | Detect-before-act, accept structurally valid events, emit normal `conflict_detected/v1` flags for invalid transitions. | A pattern may define legal transitions, but it does not reject events or own a private conflict lifecycle. |
| Conflict and flags | Flag catalog, resolver routing, exact designated-resolver equality, accepted/rejected state-participation semantics. | Pattern-raised conflicts use core categories such as `transition_violation`; pattern-specific review language stays presentation-level. |
| Assignment and scope | Assignment events, containment, assignment-admin command capability, scope-filtered sync, subject-history authorization. | Patterns may consume assignment facts as inputs, but cannot define scope, authorize assignment admin, or add activity actions. |
| Role-action policy | Platform-defined five work actions under activity roles; server authoritative, mobile advisory. | Pattern participant-role requirements may reference actions, but pattern definitions do not expand the action vocabulary or own enforcement authority. |
| Sync/offline | Scope-filtered pull, append-only push, actor/device partitioning, mobile advisory behavior. | Offline pattern projection may show progress, but cannot reject offline work or rewrite sync history. |
| Reporting | Rebuildable scoped report views can read events, projections, flags, pattern state, and sync metadata. | Pattern traversal, cascade indicators, pattern-specific aggregation, report dashboard/API, export/import, and warehouse behavior need NW-044/NW-046 or successor decisions before becoming durable contracts. |
| UI vocabulary | Shared product language can present work items, progress, pending review, attention items, and report views. | Product labels do not become core event types, status columns, resolver rules, pattern mechanisms, or scope concepts. |

## 4. Risks If The Boundary Is Not Explicit

1. Pattern-specific branches become the de facto core.
   The current server/mobile mirrored `binding.ref` branches are manageable for
   four patterns. If pattern growth continues by adding special code paths
   without a promotion rule, the catalog becomes hard to evolve and hard to
   disable or replace.

2. `pattern_specific` fields become public platform vocabulary too early.
   Fields such as approval chains, current assignee, transit time, or review
   outcome are useful, but if report APIs or UI contracts treat them as generic
   platform fields, later pattern versioning becomes expensive.

3. Pattern semantics become a second inner platform.
   `contracts/pattern-definition.schema.json` currently has a tiny closed
   `semantics` object and open-ended `branch`/`attributes` objects. If this
   grows into deployer-authored logic, it would violate CDL-038 and the
   architecture-rationale companion's config-as-code guardrail.

4. Assignment facts blur into pattern authority.
   `ongoing_resolution/v1` consumes assignment events for `current_assignee`.
   That is acceptable as derived pattern state. It becomes unsafe if agents
   infer that patterns own assignment administration, scope, or activity
   role-actions.

5. Reporting pressure can harden pattern traversal into core by accident.
   S26-style aggregation needs progress, freshness, unresolved flags, and
   drill-back. Without NW-044/NW-046 boundaries, report work may store mutable
   workflow status, create unbounded traversal APIs, or bypass source-only
   flag-cascade rules.

6. Auto-resolution and trigger pressure can attach to patterns prematurely.
   Transition violations may be auto-eligible in the broader CDL model, but
   active implementation still keeps auto-resolution, triggers, and resolver
   reassignment deferred. Pattern work must not smuggle those behaviors in.

7. UI coherence can be mistaken for architecture sameness.
   A shared product language is desirable. It must translate across patterns
   through operational UX concepts, not force every pattern into one generic
   stored status model.

## 5. Recommended Guardrails

Use these checks for every future pattern, reporting, UI, or import/export
slice.

| Guardrail | Required check |
| --- | --- |
| No event-core leakage | The slice must not add envelope fields, event `type` values, stored `pattern_ref`, stored `workflow_state`, `current_state`, or `status_changed`. |
| Pattern ownership remains platform-owned | A new pattern mechanism or semantic behavior is platform evolution. It needs a pattern definition version and routed decision/release path, not deployer config. |
| Deployer binding only | Deployer config may bind refs, shape roles, participant roles, activation roles, and bounded parameters. It must not author transitions, effects, scripts, traversal rules, or projection code. |
| Projection is rebuildable | Pattern state and report inputs must rebuild from events, assignments, config package content, flags, and pattern definitions. Materialization is only an optimization with a rebuild source. |
| Pattern output is namespaced | Pattern-specific fields stay under pattern state or an explicit pattern-scoped report model. They do not become generic subject fields or global report columns without a decision. |
| Conflict path is core | Transition violations and pattern-facing conflicts must emit normal flags and use exact designated-resolver semantics. No pattern-private resolver, batch bypass, or direct flag mutation. |
| Assignment stays core | Pattern logic may consume assignment events as facts. It must not expand activity actions, authorize assignment create/end, define scope, or infer access from UI/product labels. |
| Offline remains advisory | Mobile pattern behavior may warn and project locally. It must not become authoritative rejection for structurally valid anomalies. |
| Reporting stays scoped and traceable | Report views must show freshness, unresolved/questionable counts, clean-count exclusion, and drill-back. Warehouses, exports, imports, and broad audit/history reads need NW-044 or successor authority. |
| Pattern traversal is routed | Generic source-chain/downstream indicators, pattern traversal reporting, and fixed inventory expansion should route through NW-046 unless the scope is purely test-local. |
| Server/mobile parity is explicit | Any pattern behavior needed offline must have shared fixtures or equivalent parity evidence. Special server/mobile branches require a documented reason and tests. |
| Semantics vocabulary stays closed | New `semantics`, `branch`, or `attributes` forms must be reviewed as a platform semantic extension, not casual pattern data. If the third or fourth ad hoc semantic appears, route a dedicated design. |

## 6. Safe Go / Stop Conditions

Safe go for current IDR-020 through IDR-025:

- continue using the accepted pattern registry, binding, config-package, and
  rebuildable projection path;
- continue presenting shared operational vocabulary through the UX companion;
- continue using pattern state as derived input to scoped views and runtime
  probes;
- continue routing transition problems through normal flag/resolver semantics.

Stop and route before implementation if a slice needs any of these:

- a new event field, event type, or process-boundary schema field for pattern
  identity, status, reporting, or traversal;
- deployer-authored state machines, transition tables, scripts, queries, or
  traversal rules;
- durable workflow state used as authority;
- generic report API/warehouse/export/import contract;
- pattern traversal or source-chain indicators beyond current projection/test
  helpers;
- auto-resolution, resolver reassignment, batch resolution, or trigger
  execution;
- entity lifecycle, custom scope, sensitivity/redaction, or broad audit/history
  reads.

## 7. Required Updates And Routing

No immediate CDL, BAR, or IDR status change is required. The current pattern
IDRs remain safe under the accepted architecture.

Recommended working-surface treatment:

- Keep this review in `docs/reviews/` as pressure and routing context.
- Future reporting work should cite this review plus the NW-044 reporting
  boundary review before producing a report-view API, dashboard contract,
  warehouse, export, or import path.
- Future pattern traversal or generic downstream indicator work should route
  through existing NW-046.
- Future UI/product work should cite NW-047 and keep product terms as
  presentation language.
- Future pattern implementation agents should read IDR-020, IDR-025, IDR-026,
  BAR-012/BAR-014 evidence, `contracts/pattern-definition.schema.json`,
  `contracts/patterns/*.json`, this review, and the touched server/mobile
  projection code.

Companion/baseline updates that are useful but should be separate:

- Add a short "Pattern evolution boundary" entry to the architecture-rationale
  companion in a future documentation-control slice, pointing to the same
  no-envelope/no-status/no-deployer-state-machine/no-private-resolver guardrail.
- Add a pointer from any future NW-046 prompt to this review so traversal
  reporting does not treat pattern-specific projection fields as generic core
  concepts.
- If the next product slice needs production reporting, NW-044 should produce
  a bounded report-view decision before UI or API implementation.

## 8. Candidate Future NW

Existing route first:

- NW-046 should own generic flag cascade indicators, pattern traversal
  reporting, and fixed pattern inventory expansion.
- NW-044 should own scoped reporting, report APIs/views, warehouses, export,
  import, and interoperability boundaries.

Create a separate NW only if pattern catalog expansion is about to add new
pattern semantics beyond the current definition contract or another mirrored
server/mobile branch.

Candidate title: Decide pattern extension and projection guardrail

Type: stewardship_design or future_decision

Priority: P3

Depends on: CDL-047, CDL-048, CDL-049, CDL-050, CDL-051, IDR-020, IDR-025,
IDR-026, BAR-012, BAR-014, NW-046, NW-047

Expected artifact: an architecture-rationale companion update or IDR deciding
how new pattern semantics are added: data-driven definition extension,
platform-owned code hook, or new pattern ref version. It must include a
server/mobile parity checklist, reporting exposure rules for
`pattern_specific`, and stop conditions for deployer-authored state machines or
unbounded traversal.

Handoff prompt:

> Assess the next requested pattern expansion against CDL-047 through CDL-051,
> IDR-020, IDR-025, IDR-026, BAR-012, BAR-014, NW-046, NW-047, and
> `docs/reviews/pattern-core-boundary-assessment.md`. Decide whether the
> current `contracts/pattern-definition.schema.json` contract can express the
> need without new core semantics. If not, compare bounded options for a
> platform-owned semantic extension, a new pattern ref version, or an explicit
> deferral. Preserve append-only events, no workflow status in the envelope,
> projection-derived state, accept-and-flag transition handling, exact resolver
> semantics, deployer binding-only configuration, and server/mobile parity.
> Produce either a companion update or IDR; do not implement runtime code in
> the decision slice.

## 9. Bottom Line

Patterns are currently bounded platform mechanisms that plug into core
capabilities through clean contracts. They are not becoming the event store,
envelope, identity model, sync model, assignment model, or conflict model.

The boundary will stay healthy if future work keeps pattern behavior
projection-derived, contract-versioned, platform-owned, and namespaced, while
routing reporting/traversal/export/import pressure through NW-044 and NW-046
before it becomes durable product or process-boundary API surface.
