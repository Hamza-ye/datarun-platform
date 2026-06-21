# Conflict Flag Resolution And Attention Query Boundary

Status: accepted
Document type: platform_spec
Owner: integrity/platform verifier
Source: NW-072 row in `docs/agent-working-surface/platform-next-work-backlog.md`; PC2 PM handoff in `docs/specifications/product/product-candidate-2-pm-handoff.md`; NW-029/S21, NW-030/S27, and NW-033/S26 scenario runtime evidence
Authority: `contracts/flag-catalog.md`; `contracts/shapes/conflict_detected.schema.json`; `contracts/shapes/conflict_resolved.schema.json`; BAR-006 and BAR-013; IDR-021, IDR-022, and IDR-026 as historical decision inputs; accepted assignment/auth platform specs; NW-114 and NW-120 operational read-boundary notes
Last reviewed: 2026-06-21
Supersedes: none
Related: `docs/specifications/product/product-candidate-2-pm-handoff.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/production-auth-principal-binding.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md`; `contracts/flag-catalog.md`; `contracts/sync-protocol.md`; `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`; `docs/agent-working-surface/validation-matrix.md`; `docs/decisions/idr-021-role-action-enforcement-model.md`; `docs/decisions/idr-022-flag-severity-and-domain-uniqueness.md`; `docs/decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md`; `server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java`; `server/src/main/java/dev/datarun/server/integrity/DomainUniquenessDetector.java`; `server/src/main/java/dev/datarun/server/integrity/ConflictResolutionService.java`; `server/src/main/java/dev/datarun/server/integrity/ConflictController.java`; `server/src/main/java/dev/datarun/server/integrity/ResolverRoutingService.java`; `server/src/main/java/dev/datarun/server/event/EventRepository.java`; `server/src/main/java/dev/datarun/server/authorization/WebAdminOperationalViewService.java`; `server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java`; `server/src/test/java/dev/datarun/server/authorization/WebAdminOperationalViewIntegrationTest.java`; `server/src/test/java/dev/datarun/server/integrity/ConflictResolutionIntegrationTest.java`; `server/src/test/java/dev/datarun/server/integrity/DomainUniquenessIntegrationTest.java`; `server/src/test/java/dev/datarun/server/integrity/TransitionViolationIntegrationTest.java`; `server/src/test/java/dev/datarun/server/authorization/ProductionAuthIntegrationTest.java`

## Purpose

This specification extracts the current conflict/flag and operational-attention
behavior needed before Product Candidate 2 can implement the selected
`Single Work-Linked Attention Review` boundary.

It answers the PC2 prerequisite questions:

- what current flag/resolution behavior is normative enough for PC2 product
  copy;
- how exact designated-resolver authority is represented to users;
- whether resolver eligibility fallback is product-safe;
- where attention-read queries belong so `WebAdminOperationalViewService` does
  not extend direct `JdbcTemplate` reach-through;
- what remains out of scope.

This specification does not implement PC2, add a review UI/API, change
runtime behavior, change schemas/contracts/sync, mutate BAR/CDL/gap-register
standing, approve production, add reporting/import/export, or accept
automation, batch review, or resolver reassignment.

## Scope

The accepted scope is intentionally narrow:

```text
PC2-scoped conflict/flag durable behavior and operational attention
read-model/query boundary for Single Work-Linked Attention Review.
```

This is a platform-detail prerequisite for one manual, work-linked attention
review loop. It is not a conflict operations product, reporting product,
import/export surface, broad audit/history reader, batch workflow, or
auto-resolution mechanism.

## Contract And Trace Decision

The existing contracts remain authoritative for process and wire shapes:

| Surface | Contract-owned content |
|---|---|
| `contracts/flag-catalog.md` | Flag categories, default severity, resolvability, detection ordering, state-exclusion rule, and canonical resolution summary. |
| `contracts/shapes/conflict_detected.schema.json` | `conflict_detected/v1` payload shape. |
| `contracts/shapes/conflict_resolved.schema.json` | `conflict_resolved/v1` payload shape. |
| `contracts/sync-protocol.md` | Authenticated sync/API boundary, watermarks, cursors, and subject-history split where applicable. |

This platform spec owns the prose behavior that PC2 needs but the contracts do
not fully express: product-safe interpretation of unresolved attention,
designated-resolver authority, resolver fallback limits, canonical resolution
semantics for product copy, and the read/query boundary for a second
operational attention surface.

Implementation details such as exact SQL text, Java class layout, endpoint
template names, table indexes, and product UI wording are evidence only unless
this spec names the behavior as accepted. The accepted target is the behavior
and boundary, not the current helper shape.

IDR-021, IDR-022, and IDR-026 remain historical provenance and decision input.
After NW-072 acceptance, use this specification plus the contracts above as
the durable platform target for PC2-scoped conflict/flag and attention-query
behavior.

## Normative Flag And Resolution Behavior For PC2

The following current behavior is normative enough for PC2 product copy:

| Behavior | Accepted PC2 interpretation |
|---|---|
| A flag is an append-only `conflict_detected/v1` event targeting one `source_event_id`. | Product copy may say the source work has an unresolved attention item. |
| A flag carries one `flag_category`, effective severity, platform-owned resolvability, and a reason when available. | Product copy may translate category/severity/reason into safe review context, but raw category names are platform vocabulary unless a product wording route accepts them. |
| Unresolved flagged source events are excluded from authoritative projections/state wherever detect-before-act applies. | Product copy may say unresolved attention prevents the flagged work from being treated as settled where the platform derives state from it. |
| A canonical `conflict_resolved/v1` clears a flag only when authored by the exact `designated_resolver`. | Product copy may say the item can be resolved only by its designated reviewer. |
| `accepted` resolution admits the flagged source event into state derivation. | Product copy may say the reviewer accepted the source work for downstream use. |
| `rejected` resolution keeps the flagged source event excluded. | Product copy may say the reviewer rejected or did not accept the source work for downstream use. |
| `reclassified` remains identity-conflict-specific unless a future route broadens it. | PC2 must not present reclassification as a generic review action. |
| A non-designated resolution attempt is persisted but does not clear the target flag. | Product copy may say only the designated reviewer can complete the decision; non-designated attempts are not successful resolutions. |
| A non-designated resolution attempt creates a deterministic `scope_violation` flag on the resolution event. | PC2 must not treat this as user-facing automation, disciplinary workflow, or a new review queue. |
| `auto_eligible` means a future policy may resolve a category if explicitly selected. | PC2 must not imply current auto-resolution. |
| `resolver_unassigned` is an explicit no-human-route sentinel. | PC2 must not allow or imply fallback resolution when no human resolver is assigned. |

The current generic `Needs review` cue from NW-114 remains safe as a narrow
signal. PC2 may refine it only inside the selected one-item review boundary.

## Designated Resolver Representation

Platform authority is exact actor equality:

```text
session actor == flag.payload.designated_resolver
```

PC2 product/UI language should represent this as one of these bounded ideas:

- `designated reviewer`;
- `assigned reviewer for this item`;
- `reviewer assigned to this attention item`;
- `this item is assigned to another reviewer` when the session actor can see
  context but is not the resolver.

PC2 must not represent resolver authority as:

- generic supervisor authority;
- generic admin/root authority;
- assignment role label alone;
- IdP group, IdP role, JWT/resource claim, or JWT `actor_id`;
- browser-selected user, request-body actor, or UI role choice;
- current visibility or scope alone.

The UI may derive whether the session actor can resolve the item from the
stored `designated_resolver`, but it should not expose internal actor IDs,
resolver internals, flag table mechanics, or conflict shape names as product
language.

## Resolver Eligibility Fallback Standing

Current runtime resolver selection is strong enough to rely on the stored
`designated_resolver` value once a flag exists. It is not strong enough to make
product promises about who should be selected as resolver in all cases.

Current resolver routing evidence:

- first prefers an activity role that permits the platform `review` action
  when that role exists for the source activity;
- otherwise uses role-name fallback heuristics such as `admin`,
  `supervisor`, `coordinator`, `steward`, `reviewer`, `manager`, `lead`, or
  `resolver`;
- chooses a nearest eligible steward across the source and implicated event
  contexts;
- uses `system:resolver_unassigned/<category>` or
  `system:resolver_unassigned/multiple_flags` when no human route is found.

Accepted PC2 conclusion:

```text
The stored designated resolver is product-safe for deciding who may complete a
specific item. The resolver eligibility fallback used to choose that resolver
is not product-safe as a general authority promise.
```

Therefore PC2 may say that an item is assigned to its designated reviewer. PC2
must not say that all supervisors, coordinators, admins, reviewers, managers,
or leads can resolve items by virtue of those labels. If product copy,
production hardening, or implementation needs explicit resolver eligibility
semantics beyond the stored resolver for one item, stop and route a successor
eligibility decision from GAP-CONFLICT-03 before changing resolver authority.

If PC2 encounters `resolver_unassigned`, it must display a stop/blocked review
state or route a prerequisite. It must not invent reassignment, root/admin
override, or fallback resolution.

## Operational Attention Read-Model / Query Boundary

NW-114 tolerated one minimal latest-work operational query for PC1 and a
single generic attention cue. NW-120 identified `WebAdminOperationalViewService`
direct `JdbcTemplate` reach-through for unresolved attention as code-boundary
debt. PC2 would be a second operational attention read surface, so it needs an
explicit query boundary before implementation.

Accepted boundary for a PC2 successor:

- `WebAdminOperationalViewService` must not grow additional direct
  `JdbcTemplate` attention queries.
- A PC2 implementation must introduce or use a bounded operational
  attention-query/read-model boundary before rendering or resolving a review
  item.
- That boundary may live as a named repository/query method or a dedicated
  query service, but it must be typed and narrow. It must not expose
  `JdbcTemplate` as the product read API.
- The query may read the event store because flags, resolutions, and source
  work are append-only events. That does not make a broad reporting or audit
  read model accepted.

Minimum query behavior for PC2:

| Query concern | Accepted boundary |
|---|---|
| Scope | Apply the session actor's accepted assignment-scope predicates before returning source work or attention details. |
| Source linkage | Return only attention items whose `source_event_id` is the visible source work selected by the bounded operational view, or re-check equivalent source visibility before returning details. |
| Unresolved check | Treat a flag as unresolved only when no canonical exact-resolver `conflict_resolved/v1` exists for it. |
| Resolver check | Return product-safe resolver standing for the session actor, such as `can_resolve` or `assigned_to_current_actor`, without exposing internal resolver IDs as product copy. |
| Detail budget | Return enough context for one review item: source work identity, activity, subject reference, received/work time if available, category/severity/reason in product-safe form, and the allowed action standing. |
| Limit | PC2 is one-item review. Multi-item queues, filters, sort orders, dashboards, aggregate counts, exports, drillback, and audit history remain out of scope. |

If the first PC2 implementation needs a list, queue, filter, aggregate,
drilldown, report, import/export path, or broad subject-history/audit read, it
must stop and route through NW-044, NW-045, or another selected successor
before implementation.

## Scenario Evidence Consumed

| Evidence | Accepted PC2 use | Boundary preserved |
|---|---|---|
| NW-029 / S21 supervisor review | Proves scoped review pressure, unresolved flag exclusion, non-designated resolution denial by effect, and exact designated-resolver re-inclusion for a work-linked review case. | No broad review queue, overdue automation, resolver reassignment, or new scope mechanism. |
| NW-030 / S27 logistics transfer | Proves the same manual review model works outside health vocabulary, including discrepancy-like review and transition-flag pressure. | No custody-specific scope, auto-resolution, batch resolution, or health-domain platform semantics. |
| NW-033 / S26 reporting/aggregate oversight | Proves report inputs can expose timestamps, watermarks, unresolved flag counts, exact resolver re-inclusion, and event drill-back as evidence. | PC2 does not accept reporting, aggregate completeness, exports, imports, warehouse, or drilldown product behavior. |
| NW-114 minimal operational view | Proves a scoped, read-only `Needs review` cue can be shown for one visible latest work item. | The one-off query does not authorize a second read surface without this NW-072 boundary. |

## Non-Goals

NW-072 does not select or accept:

- PC2 runtime implementation;
- review UI/API;
- new product behavior beyond the PC2 handoff;
- reporting dashboards, aggregate analytics, completeness, freshness reports,
  drilldown, import, export, warehouse, or broad read APIs;
- conflict queue operations, batch review, pending-match queues, bulk commands,
  auto-resolution, resolver reassignment, or root/admin override;
- new flag categories, flag schema changes, resolver authority changes,
  payload schema tightening, contracts, envelope fields/types, sync protocol,
  or migration work;
- direct flag mutation;
- durable workflow-state tables;
- S06/entity lifecycle, known-set registry, deactivation, candidates,
  merge/split UX, or registry stewardship;
- tenant-aware runtime, pooled storage, workspace-scoped config, tenant sync
  context, tenant isolation harness, or managed control plane;
- real users/data, provider/region/jurisdiction/support, compliance/security,
  continuity, or production approval.

## Successor Implementation Criteria

A later PC2 implementation successor may be prepared only if it stays inside
all of these conditions:

- implements one work-linked attention review item for
  `Single Work-Linked Attention Review`;
- uses the accepted designated-resolver equality rule;
- represents resolver standing with product-safe language;
- handles `resolver_unassigned` as blocked/not currently resolvable rather than
  fallback authority;
- introduces or uses the bounded operational attention query boundary above;
- includes focused tests for scope, unresolved/canonical resolution behavior,
  non-designated denial by effect, and no broad read/reporting exposure;
- keeps runtime behavior, contracts, schemas, BAR, CDL, gap register,
  production approval, tenant/control-plane, reporting/import/export,
  automation, batch, and reassignment out of scope unless separately selected.

This specification does not create that successor prompt or implement it.

## Escalation Triggers

Stop and route before implementation if a candidate requires:

- changing resolver eligibility, resolver authority, or reassignment behavior;
- treating resolver fallback role labels as product authority;
- resolving `resolver_unassigned` items without a selected reassignment route;
- adding or tightening conflict payload schemas;
- adding new flag categories, resolution outcomes, event types, envelope
  fields, or sync protocol behavior;
- accepting auto-resolution or batch resolution;
- adding broad conflict list/queue ergonomics beyond one work-linked item;
- adding reporting, aggregate completeness, import/export, drilldown, or broad
  audit/history access;
- changing assignment/scope semantics or actor authority;
- real users/data or production approval;
- tenant/control-plane behavior;
- BAR, CDL, or gap-register mutation.

## Acceptance Evidence

NW-072 is docs-only prerequisite/specification work.

Required validation:

```bash
git diff --check
rg "NW-072" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
rg "Single Work-Linked Attention Review" docs/specifications/product/product-candidate-2-pm-handoff.md
```

Runtime tests are skipped for NW-072 because this specification changes no
runtime code, tests, contracts, schemas, migrations, CI behavior, validation
policy, product behavior acceptance, BAR, CDL, gap-register standing, mobile
code, server/web-admin implementation, reporting/import/export behavior,
conflict automation, batch behavior, resolver reassignment, tenant/control
plane, real-production approval, or real users/data.
