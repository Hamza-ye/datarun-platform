# NW-044 Reporting Aggregation Import/Export Boundary Review

Status: review / pre-decision analysis

Date: 2026-06-06

Authority note: this review does not change CDL authority, BAR status,
contracts, schemas, APIs, runtime behavior, backlog priority, or accepted
baseline standing. The CDL remains the architecture authority. This review is a
bounded input for the existing NW-044 future-decision route.

## 1. Purpose

This review answers one question before any reporting work starts:

> What does "reporting aggregation plus structured import/export" mean for the
> current platform, what is it anchored on, and what boundary should the next
> bounded slice produce?

The answer is not "build a dashboard." The current platform has enough accepted
kernel behavior to support a scoped report-view design, but it does not have an
accepted reporting API, reporting warehouse, export contract, import contract,
or interoperability mechanism. NW-044 should therefore begin as a
product/platform boundary decision that splits those concerns before
implementation.

## 2. Current Standing

NW-056 classifies reporting/aggregate oversight as
`scenario_runtime_evidenced`, with production dashboard/API work
`blocked_by_future_decision`.

The accepted S26 runtime probe proves that current constructs can expose report
inputs and traceability:

- in-scope report input events are visible and out-of-scope events are excluded;
- event timestamps, latest projected timestamps, and pull watermarks can support
  freshness language;
- unresolved flagged review events stay out of projection/aggregate counts while
  unresolved flag counts remain visible;
- exact designated-resolver acceptance re-includes a source event, while
  rejection keeps it excluded;
- drill-back remains event-based through subject timelines, flag source links,
  and resolution events.

The same probe deliberately did not add a production reporting subsystem:

- no reporting warehouse;
- no report API;
- no export/import contract;
- no new scope mechanism;
- no envelope/schema changes;
- no trigger execution, resolver reassignment, or auto-resolution.

Code inspection aligns with that status: there is no production reporting
module/API/dashboard in `server/src/main/java` or `mobile/lib`. The accepted S26
aggregation is test-local helper logic over existing projections.

## 3. Product And Platform Need

The platform vision is broader than capture. It is meant to let organizations
collect information, coordinate work, track progress, and maintain
accountability across people, places, and time, while staying one coherent
system with consistent concepts and trustworthy history.

The operational constraints make reporting unavoidable:

- supervisors and coordinators need to see across many workers, places, and
  activities;
- oversight is eventually consistent, not live, so freshness must be visible;
- reports drive operational decisions such as follow-up, redirection, review,
  and closure;
- auditors and external systems need records to be producible, but their access
  patterns can cut across normal scopes;
- interoperability is a boundary condition: Datarun must be able to exchange
  structured records with external systems, without adopting external standards
  as its internal storage model.

Scenario 26 makes the reporting requirement concrete:

- aggregate views of progress, missing work, and pending review;
- visible freshness/staleness of available information;
- clear treatment of questionable or unresolved records;
- viewer-scoped reporting;
- drill-back from aggregate numbers to records and responsibility chains.

## 4. CDL Anchors

The current CDL supports scoped read-side reporting but tightly bounds how far
that can go without a successor decision.

| Anchor | What it means for reporting/import/export |
| --- | --- |
| CDL-001 | Operational truth lives in immutable events. Reports and exports must trace back to events; imports that create Datarun records must append events, not mutate state. |
| CDL-002 | Reporting projections may be materialized for performance, but they are derived and rebuildable. A report count must not become canonical truth. |
| CDL-003 | Structurally valid anomalous events are accepted and flagged. Reports must account for questionable records instead of pretending they were rejected. |
| CDL-004 | Flagged events remain visible in timelines/audit but are excluded from policy/state participation while unresolved. Reports need both excluded-from-clean-count semantics and visible unresolved-issue counts. |
| CDL-006 / CDL-007 / CDL-012 | No new envelope fields, no new event `type` values, and no derived runtime fields in the envelope. Reporting cannot add `report_id`, `campaign_id`, `workflow_state`, `authority_context`, `scope`, or `sensitivity` to events without architecture authority. |
| CDL-008 / CDL-039 | `shape_ref` and shape version coexistence are the stable interpretation path. Reports, exports, and imports must preserve versioned shape meaning. |
| CDL-009 / CDL-056 | `activity_ref` is optional and must preserve provenance honesty. Reports may group by activity when present; import tooling must not fabricate activity attribution. |
| CDL-010 / CDL-011 | `device_id`, `device_sequence`, and `sync_watermark` carry causal metadata; `device_time` is advisory. Freshness and audit displays may use timestamps with caveats, but correctness cannot depend on device clocks. |
| CDL-021 | Sync transfers immutable events idempotently by scope. Reporting/export work must not turn normal live sync into broad audit/history pull. |
| CDL-030 / CDL-031 / CDL-032 / CDL-055 | Access and sync scope are assignment-derived, and scope mechanisms are platform-fixed. A report surface must be scoped by current platform authority, not UI filters, IdP claims, query scripts, or external report roles. |
| CDL-037 / CDL-046 | Retention, sensitivity, redaction, encryption, and compliance handling are not solved by current reporting. Export and broad audit/reporting surfaces must route through BAR-106 where sensitivity or retention policy matters. |
| CDL-038 / CDL-044 | Configuration has an L3-to-code ceiling, and trigger/config complexity budgets are enforced by platform code. Deployer-authored reporting logic must not become a script/query language unless a platform mechanism is explicitly designed. |
| CDL-041 | Config packages are atomic and versioned. Report interpretation must tolerate old and new shape/activity versions coexisting. |
| CDL-047 / CDL-049 | Workflow state is projection-derived from platform patterns. Reports can show pattern state and progress, but must not store mutable status truth or let deployers author state machines. |
| CDL-051 | Flag cascade is source-only. Report views may show downstream indicators via projection traversal, but should not multiply flags. |
| CDL section 14 | Import tooling details are deferred implementation/specification and must preserve provenance honesty. Human resolver UX, auto-resolution UX, field-level sensitivity/redaction, projection performance strategy, and import tooling are not current canonical decisions. |

## 5. Archive Scan Through The Exploration Framework

The archive is provenance, not authority. The useful method from
`00-exploration-framework.md` is to narrow options, identify assumptions, use the
irreversibility filter, and avoid promoting exploration leans into current
decisions.

ADR-3 exploration contributes these reporting-relevant lessons:

- sync scope equals access scope for field data; broad local datasets hidden by
  UI are unsafe;
- projection location is tiered: field devices use local projections,
  supervisors may need hybrid raw-event drill-down plus summary views, and
  coordinators naturally query server-side projections;
- report summaries must include freshness metadata, otherwise "no activity" and
  "not yet synced" are indistinguishable;
- supervisor/coordinator projection strategies are evolvable implementation
  strategies, not event-envelope commitments;
- auditor and broad history access remain special pressure, not a current
  authority model.

ADR-4 exploration contributes these lessons:

- external standards such as FHIR must be export/mapping concerns, not internal
  storage models;
- configuration must not become a programming language or a second authority
  pipeline;
- shape versions and activity attribution are central to report interpretation;
- aggregate analytics and custom projection logic belong beyond ordinary
  deployer configuration unless a platform capability is explicitly designed;
- old exploration text proposed more structural event types than the current
  CDL allows, so current work must follow the CDL's closed six-value vocabulary.

ADR-5 exploration contributes these lessons:

- workflow state is projection-derived, not a stored status field;
- `status_changed` and `pattern_ref` are not needed in the envelope;
- unresolved flagged events should be visible but excluded from clean state;
- source-only flagging means report views should show upstream flagged lineage
  instead of multiplying root flags;
- auto-resolution remains deferred despite some flag categories being
  `auto_eligible`.

## 6. Requirement Shape

NW-044 should separate four related but different surfaces.

### 6.1 Scoped Report Views

This is the closest first product slice.

A scoped report view is a read-side aggregation over current events,
projections, flags, sync metadata, assignments, activity configuration, and
pattern state. It can be materialized for performance if rebuildable.

Minimum requirement set:

- authenticated viewer and assignment-derived scope evaluation;
- grouping by safe existing dimensions: subject/location/activity/shape/pattern
  where the data exists and is authorized;
- clear freshness: latest event watermark, latest projected watermark or
  equivalent consistency marker, latest contributing sync observation when
  available, and timestamp caveats;
- clean count vs. unresolved/questionable count distinction;
- accepted/rejected flag resolution semantics matching the flag catalog;
- drill-back from a count to contributing subject/event timelines, flags,
  source event ids, and resolution events;
- version-aware interpretation of `shape_ref` and `activity_ref`;
- sensitivity/retention caveats if data is elevated or restricted.

Forbidden in the first report-view decision:

- canonical aggregate table as operational truth;
- report-specific event type or envelope fields;
- unscoped broad audit/history read;
- deployer-authored SQL/query-as-access/config scripts;
- external export/import contract;
- dashboard state as source of truth;
- auto-resolution, resolver reassignment, or batch conflict resolution.

### 6.2 Reporting Warehouse / Analytics Store

This is not the same as a scoped report view.

A warehouse may be reasonable later for performance, historical snapshots, BI
tools, large aggregates, or external reporting. But it creates separate
questions:

- rebuild source and reconciliation against event truth;
- snapshot semantics and retention;
- sensitivity, redaction, and jurisdiction handling;
- whether warehouse records are inside or outside Datarun operational truth;
- access model for analysts and auditors;
- export lineage and drill-back guarantees.

This should remain explicitly deferred until a scoped report-view model proves
what operational questions actually need warehouse support.

### 6.3 Structured Export

Export is a process-boundary artifact, not just a dashboard download.

Export must answer:

- who is authorized to export which data and why;
- whether export is raw events, projected rows, report-view rows, or a bundle;
- how shape/activity/config versions travel with the export;
- how unresolved flags, rejected events, source-only cascade indicators, and
  resolution events are represented;
- how sensitivity, redaction, retention, and jurisdiction constraints are
  applied;
- whether external standards are direct mappings or separate adapters;
- how drill-back/provenance survives outside Datarun.

Current CDL permits export specification work, but not accidental changes to
the event envelope, scope model, or sensitivity/redaction behavior.

### 6.4 Structured Import / Event Ingestion

Import is riskier than export because it can create operational records.

Import must answer:

- is the input already Datarun-native event envelopes, or external records that
  must be mapped into Datarun events;
- who or what is the `actor_ref`, especially for system/import actors;
- how `device_id`, `device_sequence`, and idempotent event ids are assigned;
- which `shape_ref` version is used and how payload validation runs;
- when `activity_ref` is honest and when it must be `null`;
- how source-system provenance is preserved without adding envelope fields;
- whether imported records can be flagged by ordinary integrity detectors;
- how duplicate import retries are made idempotent.

The CDL section 14 deferred boundary is especially relevant: import tooling
details must preserve provenance honesty. Import must not fabricate activity,
authority, time, actor, or workflow state.

## 7. Recommended Next Slice

Do NW-044 as a decision-routing artifact, not as implementation.

Recommended output:

1. A bounded NW-044 artifact under
   `docs/agent-working-surface/artifacts/`.
2. It should compare and split these lanes:
   - scoped report-view model;
   - reporting warehouse/export;
   - structured import/event ingestion;
   - external interoperability mappings.
3. It should select the first implementable successor as a scoped report-view
   model only, unless product/security stakeholders explicitly need export or
   import first.
4. It should produce a second implementation prompt only after the report-view
   model names its stable server-side view model/API boundary and tests.

Recommended first implementation after the decision:

- server-side scoped report-view endpoint or service, if authorized;
- built from current projections/events/flags/sync metadata;
- no new contracts unless the decision explicitly creates one;
- no warehouse;
- no export/import;
- targeted integration test extending S26-style coverage from test-local helper
  aggregation to the selected report-view service.

## 8. Boundary And Responsibility

| Concern | Owner for first slice | Boundary |
| --- | --- | --- |
| Product reporting vocabulary | Operational UX companion / NW-044 artifact | May use "report view", "freshness", "pending review", and "drill-back"; cannot become platform authority. |
| Report-view semantics | Product/platform decision | Defines clean/questionable counts, freshness markers, drill-back, and scoped inclusion. |
| Report-view computation | Projection/read-side service | Derived and rebuildable; events remain truth. |
| Access control | Existing assignment/scope resolver | Report visibility equals authorized scope unless a successor access decision exists. |
| Flag treatment | Existing flag catalog and conflict-resolution semantics | Unresolved flags visible and excluded; accepted re-includes; rejected excludes. |
| Export contract | Future product/platform/security decision | Must include sensitivity, provenance, version, and authorization semantics. |
| Import contract | Future product/platform/security decision | Must preserve provenance honesty and validate through existing envelope/shape/payload rules. |
| Interoperability adapters | Future integration specification | External standards are mappings, not Datarun internal storage models. |
| Warehouse/BI | Future architecture/ops decision | Optimization or external analytics surface, not canonical truth. |

## 9. Stop Conditions

Stop and surface drift if a proposed next step:

- adds an envelope field or event `type`;
- treats report rows, dashboard state, or warehouse rows as canonical operational
  truth;
- changes normal sync into broad historical/audit pull;
- creates a new scope type or query-as-access mechanism;
- uses UI filters, IdP groups/claims, or report roles as authority;
- hides freshness, unresolved flag treatment, or drill-back;
- exports restricted/sensitive data without BAR-106/security routing;
- imports records by fabricating activity, actor, authority, or workflow state;
- implements warehouse/export/import before a decision splits their contracts.

## 10. Handoff Prompt For The Next Agent

Goal: Produce `docs/agent-working-surface/artifacts/NW-044-reporting-aggregation-import-export-boundary.md` as a future-decision artifact. Do not implement code.

Files to read:

- `AGENTS.md`
- `docs/status.md` Current Routing only
- `docs/agent-working-surface/README.md`
- `docs/agent-working-surface/platform-next-work-backlog.md` row NW-044 and row NW-033
- `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`
- `docs/reviews/NW-044-reporting-aggregation-import-export-boundary-review.md`
- `docs/agent-working-surface/operational-ux-layering-companion.md`
- `docs/scenarios/26-operational-reporting-and-aggregate-oversight.md`
- `contracts/envelope.schema.json`
- `contracts/sync-protocol.md`
- `contracts/flag-catalog.md`
- `contracts/config-package.schema.json`
- `contracts/shape-format.schema.json`
- focused CDL rows: CDL-001, CDL-002, CDL-003, CDL-004, CDL-006, CDL-007,
  CDL-008, CDL-009, CDL-010, CDL-011, CDL-012, CDL-021, CDL-030, CDL-031,
  CDL-032, CDL-037, CDL-038, CDL-039, CDL-041, CDL-046, CDL-047, CDL-049,
  CDL-051, CDL-055, CDL-056, plus section 14 deferred boundary

Use archive material only as provenance:

- `docs/exploration/archive/00-exploration-framework.md`
- targeted ADR-3/4/5 snippets for sync scope, projection location, freshness,
  export-as-mapping, config anti-patterns, workflow projection, and source-only
  flagging

Expected artifact:

- state the current reporting capability and evidence;
- split scoped report views from warehouse/export/import/interoperability;
- compare at least three routes: report-view-first, export/import-first, and
  dashboard-now;
- select a recommended route and name forbidden work;
- define the next implementation prompt only if the selected route is bounded
  enough;
- keep BAR/CDL/contracts unchanged.

Stop-and-report conditions:

- any source claims production reporting API/warehouse/export/import already
  exists;
- a required report behavior depends on new envelope fields/types, new scope,
  broad audit/history pull, or sensitivity/redaction behavior;
- the decision cannot preserve provenance honesty for import/export.

## 11. Verification Notes

Sources inspected for this review:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/implementation/module-interfaces.md`
- `docs/agent-working-surface/README.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/baseline-acceptance-register.md`
- `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`
- `docs/agent-working-surface/operational-ux-layering-companion.md`
- `docs/agent-working-surface/architecture-rationale-and-routing-companion.md`
- `docs/README.md`
- `docs/constraints.md`
- `docs/scenarios/26-operational-reporting-and-aggregate-oversight.md`
- `contracts/envelope.schema.json`
- `contracts/sync-protocol.md`
- `contracts/flag-catalog.md`
- `contracts/config-package.schema.json`
- `contracts/shape-format.schema.json`
- `contracts/pattern-definition.schema.json`
- `server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java`
- targeted ADR-3/4/5 exploration archive files requested by the prompt

Commands included:

- `python3 scripts/query_cdl.py --help`
- `python3 scripts/query_cdl.py --format concise`
- focused `python3 scripts/query_cdl.py --id ... --format full`
- focused `sed` and `rg` reads for the files above

No Maven, Flutter, or runtime tests were run because this is a documentation
review and no runtime files were changed.
