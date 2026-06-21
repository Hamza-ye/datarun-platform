# Scoped Operational Report Snapshot Boundary

Status: accepted
Document type: platform_spec
Owner: reporting/platform verifier
Source: NW-128 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-128-specify-pc3-scoped-operational-report-snapshot-boundary.md`; PC3 PM handoff in `docs/specifications/product/product-candidate-3-pm-handoff.md`; NW-033/S26 scenario runtime evidence; NW-114 and NW-122 web-admin operational-view evidence
Authority: accepted assignment, web-admin authority, and conflict/flag platform specifications; relevant contracts/BAR standing as referenced by those specs
Last reviewed: 2026-06-22
Supersedes: none
Related: `docs/specifications/product/product-candidate-3-pm-handoff.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md`; `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`; `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`; `docs/agent-working-surface/validation-matrix.md`; `docs/scenarios/26-operational-reporting-and-aggregate-oversight.md`; `docs/reviews/scenario-baseline-pressure-map.md`

## Purpose

This specification accepts the bounded Product Candidate 3 platform behavior
for one `Scoped Operational Report Snapshot` before implementation. It is a
product delivery-readiness boundary: it gives an implementer enough exact
scope, data treatment, access, traceability, and read-query semantics to build
the first snapshot without guessing.

This specification does not implement PC3, add runtime code, add tests, add
migrations, add contracts or schemas, change CI or validation policy, approve
real production, mutate the lab, run PC2 live browser proof, accept broad
reporting/import/export, create a report warehouse/API/catalog, create a
conflict queue/list/batch workflow, change tenant/control-plane behavior, or
mutate BAR, CDL, or the gap register.

## Selected Boundary

The selected PC3 boundary is:

```text
Current scoped operational standing snapshot with limited traceability.
```

The first implementation target is one read-only, server-rendered web-admin
page under the existing production web-admin surface:

```text
GET /web-admin/operational/report
```

The route name is an implementation target, not a public reporting API. It
must stay behind the accepted web-admin browser session boundary and must not
create `/api/reports`, report catalog, saved reports, exports, imports,
analytics storage, or a warehouse.

## Boundary Comparison

| Candidate boundary | NW-128 decision | Reason |
|---|---|---|
| Current scoped standing snapshot | Selected. | It answers the PC3 user question with current accepted data, access, and conflict behavior. |
| Current-period reporting snapshot | Deferred. | PC3 does not accept cadence, recurring obligations, due periods, deadlines, or report-period completion semantics. |
| Freshness-only summary | Rejected as too narrow. | Freshness without counts, issue treatment, and trace context does not satisfy PC3. |
| Unresolved-issue summary | Rejected as PC2 drift. | Issue handling may be counted and caveated, but queue/list/review ergonomics remain outside PC3. |
| Traceable snapshot with limited links | Included inside selected boundary. | Limited trace context is needed, but broad audit/history and drilldown are not accepted. |

## Product Behavior Spec Decision

PC3 does not need a separate accepted product behavior specification now.

The accepted product inputs for the first implementation are the PC3 PM
handoff plus this platform boundary. This is enough because the first PC3
delivery is one constrained read-only snapshot, not a suite of user flows,
copy variants, recurring-report commitments, or owner-approved real-use
behavior.

A PC3 product behavior specification becomes the right next route only if one
of these triggers appears:

- the snapshot expands beyond one page;
- product copy needs accepted period, deadline, overdue, or completion
  language;
- owner review needs a durable user-facing acceptance spec separate from the
  PM handoff;
- multiple report views, saved views, exports, imports, dashboards, or report
  catalogs are selected;
- real users/data or production approval is proposed.

Until then, implementation should build from this spec and the PC3 handoff.

## Users And Authority

The snapshot is for a scoped supervisor, reviewer, organization operator, or
read-only observer who already has accepted web-admin access. Persona labels
do not grant authority.

Required access checks:

1. valid production web-admin session;
2. `web_admin.access`;
3. `web_admin.read_scoped`;
4. accepted assignment-derived visibility or designated-resolver visibility
   for every event, count, row, and trace target included.

`web_admin.access` by itself does not grant data read authority. IdP groups,
IdP roles, JWT/resource claims, JWT `actor_id`, browser-selected user ids,
request-body actors, generic admin/root labels, report-view labels, tenant
selection, and product personas do not grant access.

## Snapshot Scope

The snapshot covers only the current managed-isolation lane:

- one customer-facing Organization;
- one managed single-tenant Datarun deployment;
- one internal/default Workspace;
- the session actor's current authorized scope.

The query must apply accepted assignment-scope predicates before aggregation,
ordering, limiting, counting, latest-time selection, issue counting, or trace
target selection. Out-of-scope records must not affect visible counts, latest
timestamps, data-status labels, unresolved issue counts, trace links, or
"hidden records exist" hints.

The snapshot is generated on request. It is not a durable report run, scheduled
job, materialized report warehouse, export artifact, or retained audit packet.

## Snapshot Content

The first implementation must render one bounded snapshot with these sections:

| Section | Required content | Boundary |
|---|---|---|
| Snapshot header | Snapshot title, `snapshot_as_of`, scoped-view caveat, and data-status label. | No production, SLA, or all-devices-current claim. |
| Freshness context | Latest visible input time and latest clean work time when known; unknown/no-visible-input standing otherwise. | No recurring deadline, overdue, device heartbeat, or real-time guarantee. |
| Activity standing rows | One row per visible configured activity with visible clean source count, excluded-unresolved source count, unresolved issue count, latest times, and standing caveat. | No arbitrary filters, report designer, saved views, or cross-activity custom scope. |
| Unresolved issue treatment | Count visible unresolved attention and identify whether affected source work is excluded from clean counts. | No queue/list, batch, automation, reassignment, or broad conflict console. |
| Limited trace context | Optional bounded links to existing scoped operational work or one accepted attention context. | No broad audit/history, subject-history browser, export, or drilldown product. |

The page may show an empty state when no visible source work exists. Empty does
not mean the Organization has no work. It means no in-scope source work was
visible to the session actor for this snapshot.

## Freshness And Staleness Semantics

Freshness in PC3 is evidence freshness, not operational completeness, not a
device-heartbeat SLA, and not proof that every offline device has synced.

Accepted timestamp inputs:

- `snapshot_as_of`: server time when the snapshot query is evaluated;
- latest visible source-work time, when the source event carries product-safe
  work time;
- latest visible server-received time for source work;
- latest visible attention/resolution input time when it changes the issue
  standing shown by the snapshot;
- latest clean source-work time after unresolved blocking flags are excluded,
  when available.

The first implementation must use these states:

| State | Meaning | Required product-safe treatment |
|---|---|---|
| `known_latest_input` | At least one in-scope input has a known server-received or source-work time. | Show the latest known timestamp or relative age and say what it represents. |
| `no_visible_input` | No in-scope source work or issue input is visible to the actor. | Show an empty-scoped standing, not zero Organization work. |
| `unknown_latest_input` | The query cannot determine the relevant timestamp from accepted inputs. | Say freshness is unknown; do not infer current or stale. |

Thresholded `fresh`, `stale`, `late`, `overdue`, or `current-period complete`
classification is not accepted for PC3. Product copy may caveat that the
snapshot may be incomplete or stale, but it must not compute a stale bucket,
deadline, or SLA without a later selected product/reporting route.

If implementation needs device heartbeats, recurring report cadence, expected
submission counts, due-period logic, or automatic missed-period standing, stop
and route through NW-044 or another selected reporting/cadence decision before
implementation.

## Completeness And Uncertainty

The snapshot reports observed scoped standing only. It must not claim that work
is complete, all expected reports are present, all devices are synced, or all
field work has occurred.

Accepted PC3 completeness terms:

- `visible clean source count`: in-scope source work not excluded by an
  unresolved blocking attention item under accepted conflict/projection rules;
- `excluded unresolved source count`: in-scope source work affected by
  unresolved attention and therefore not counted as clean;
- `unresolved issue count`: in-scope unresolved attention items visible under
  accepted scope and resolver visibility;
- `coverage not measured`: expected denominator, due period, or required
  report total is not accepted for PC3.

The snapshot must show uncertainty when expected totals are not accepted. It
must not convert observed counts into percentages, completion rates, compliance
rates, missing counts, or "all clear" claims.

## Unresolved Issue Treatment

The snapshot uses the accepted conflict/flag behavior from
`conflict-flag-resolution-and-attention-query-boundary.md`.

Normative PC3 treatment:

- a source event with unresolved blocking attention is not counted as clean
  standing where accepted projection/current-state behavior excludes it;
- the unresolved attention remains visible as a count if the source work or
  attention item is visible to the session actor;
- canonical `accepted` resolution re-includes the source work in clean standing
  where accepted projection/current-state behavior does so;
- canonical `rejected` resolution keeps the source work out of clean standing;
- non-designated resolution attempts do not clear the target flag and must not
  be treated as successful decisions;
- `resolver_unassigned` remains blocked/not currently resolvable and must not
  create fallback authority.

The snapshot may show one link to the existing accepted PC2 one-item attention
context when there is a single selected visible item or when the existing
operational attention query selects one representative item. It must not show
an issue queue, list, filter, sort order, triage workflow, batch action, auto
resolution, resolver reassignment, or broad conflict console.

## Traceability Boundary

Traceability means enough scoped context for a reviewer to understand what
evidence produced the snapshot. It is not a broad audit/history reader.

Allowed trace targets:

- existing scoped operational latest-work context;
- existing accepted one-item attention context from PC2;
- source-work label, activity label, received/work time, and safe product
  context returned by the bounded query;
- internal event identifiers only as non-copy implementation details when
  required to re-query a visible item.

Required trace rules:

- every trace target must re-check current session, `web_admin.access`,
  `web_admin.read_scoped`, and accepted assignment/designated-resolver
  visibility before rendering;
- trace links must not expose out-of-scope existence through 404/403 wording,
  counts, row gaps, or hidden totals;
- trace must not open arbitrary subject history, broad actor history, broad
  geography/activity history, raw event-store browsers, exports, imports, or
  report APIs.

## Read-Model And Query Boundary

The implementation must introduce or use one typed, bounded
`ScopedOperationalReportSnapshot` read boundary. The exact Java class name is
an implementation detail, but the boundary must be explicit and narrow.

Minimum input:

- server-resolved session actor;
- snapshot evaluation time;
- current accepted assignment-scope context;
- accepted web-admin command capability result.

Minimum output:

- `snapshot_as_of`;
- data-status state from this spec;
- scoped-view caveat;
- activity standing rows;
- latest visible input time and latest clean source-work time when known;
- clean, excluded-unresolved, and unresolved issue counts;
- optional bounded trace targets that are already scope-checked.

Query invariants:

- apply scope before aggregate and latest calculations;
- do not let out-of-scope records affect count, latest-time, empty-state, or
  issue-standing results;
- derive from existing append-only events, accepted projections, or bounded
  repository/query methods;
- do not create durable workflow state, reporting tables, report runs, export
  files, warehouse storage, public report APIs, sync protocol changes, contract
  changes, migrations, or schema changes;
- keep controller/template code away from direct ad hoc SQL reach-through for
  the product query; use a named query service or repository method.

If the query needs pattern traversal, inventory expansion, workflow projection
changes, pattern APIs, or pattern behavior as normative beyond already
accepted projection evidence, stop and select NW-073 first. NW-128 does not
select NW-073 because the accepted snapshot boundary does not depend on new or
normative pattern registry/projection behavior.

## Product Wording Guardrails

Product-facing wording may use:

- `Scoped Operational Report Snapshot`;
- `Current scoped standing`;
- `Latest visible input`;
- `Clean source work`;
- `Needs attention`;
- `Freshness unknown`;
- `No visible input`;
- `Coverage not measured`.

Product-facing wording must not use or imply:

- tenant, workspace selector, event store, projection implementation, query
  boundary, sync watermark as product authority, flag table, resolver internals,
  report warehouse, report API, all devices current, real-time, complete,
  overdue, SLA, production ready, or all-clear standing;
- generic admin, IdP group/claim, assignment role alone, or report-view role
  as authority;
- hidden out-of-scope counts or "there are records you cannot see" hints.

## Non-Goals

NW-128 and the first PC3 implementation do not select or accept:

- broad reporting, import, export, warehouse, analytics, report catalog, saved
  views, arbitrary filters, dashboards, interoperability reporting, or public
  reporting APIs;
- current-period/cadence/deadline/overdue/completion semantics;
- conflict queue/list/multi-item review, batch workflow, auto-resolution,
  resolver reassignment, pending-match queues, or broad conflict console;
- pattern traversal/reporting, inventory expansion, pattern API work, pattern
  migration, or projection changes;
- S06/entity lifecycle, known-set registry, candidates, deactivation,
  duplicate stewardship, merge/split UX, or discovered-unit lifecycle;
- new scopes, subject/query/custom scope, cross-activity cohort materializing,
  query-as-config authority, or hidden sync scope;
- retention/security/offboarding promises, local encryption, erasure,
  redaction, no-local-retention, or sensitivity-specific retention claims;
- tenant-aware runtime, pooled storage, workspace-scoped config, tenant sync
  context, tenant isolation harness, managed control plane, or UI tenant
  selection;
- real users/data, provider/region/jurisdiction/support, compliance/security,
  continuity, or go/no-go approval;
- PC2 live browser proof, lab mutation, or NW-126 unblocking.

## Preserved Trigger Routes

| Route | Preserved trigger | NW-128 decision |
|---|---|---|
| NW-093 | Real users/data, provider, region, jurisdiction, support, compliance/security, continuity, or go/no-go. | Not selected; still blocked. |
| NW-126 | Lab hostname or fixed-IP SSH access restored enough to inspect R12 before touching retained PC2 state. | Not selected; still blocked. |
| NW-044 | Broad reporting/import/export, warehouse, analytics, broad report APIs, report catalog, or cadence/completeness reporting. | Not selected; PC3 is one bounded snapshot. |
| NW-073 | Pattern traversal/reporting, inventory expansion, projection change, pattern API/product work, or normative pattern behavior dependency. | Not selected; PC3 avoids that dependency. |
| NW-053 | Access cannot be represented by accepted assignment axes. | Not selected; accepted scope is sufficient. |
| NW-054 | Retention, expiry, encryption, erasure, redaction, offboarding, or no-local-retention claims. | Not selected; PC3 makes no retention/security promise. |
| NW-094, NW-095, NW-096, NW-097, NW-098 | Managed control plane, tenant-aware identity/runtime/storage/sync/config, or tenant isolation proof. | Not selected; managed-isolation lane remains. |

## Implementation Successor Readiness

Binary readiness call:

```text
One implementation successor is ready.
```

The selected successor is:

```text
NW-129 - Implement PC3 scoped operational report snapshot.
```

NW-129 is ready because this spec accepts the one-page boundary, access model,
freshness treatment, completeness caveats, unresolved issue treatment,
traceability limits, read-query semantics, non-goals, and stop conditions.

NW-129 must stop before implementation if it needs any prerequisite listed in
the preserved trigger routes, including NW-044, NW-073, NW-053, NW-054,
NW-093, NW-126, or NW-094 through NW-098.

## Implementation Acceptance Criteria For NW-129

A later NW-129 implementation may be accepted only if it:

- implements one read-only server-rendered `/web-admin/operational/report`
  snapshot;
- requires web-admin session plus `web_admin.access` and
  `web_admin.read_scoped`;
- applies accepted scope before all count/latest/trace calculations;
- renders data-status states from this spec without thresholded stale/overdue
  or completion claims;
- shows clean, excluded-unresolved, and unresolved issue counts consistently
  with accepted flag/resolution behavior;
- keeps issue handling to counts and at most one accepted existing attention
  context link;
- uses a typed bounded read-model/query boundary;
- includes focused tests for access denial, no-leakage aggregation, freshness
  unknown/no-visible-input/known states, unresolved issue exclusion/counting,
  and trace target scope re-checks;
- runs the required server gate from the validation matrix for web-admin
  behavior changes.

## Acceptance Evidence For NW-128

NW-128 is docs-only platform-specification work.

Required validation:

```bash
git diff --check
rg "NW-128" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/specifications/platform/scoped-operational-report-snapshot-boundary.md
rg "Scoped Operational Report Snapshot" docs/specifications/platform/README.md
rg "NW-129" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/specifications/product/product-candidate-3-pm-handoff.md
rg "NW-073|NW-044|NW-053|NW-054|NW-093|NW-126|NW-094|NW-095|NW-096|NW-097|NW-098" docs/specifications/platform/scoped-operational-report-snapshot-boundary.md
```

Runtime tests are skipped for NW-128 because this specification changes no
runtime code, tests, contracts, schemas, migrations, CI behavior, validation
policy, product behavior spec, BAR, CDL, gap register, mobile code,
server/web-admin implementation, reporting/import/export behavior, conflict
automation, tenant/control-plane behavior, real-production approval, real
users/data, lab state, or PC2 live proof.
