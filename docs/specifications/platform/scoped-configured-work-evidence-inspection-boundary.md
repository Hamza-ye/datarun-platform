# Scoped Configured Work Evidence Inspection Boundary

Status: accepted
Document type: platform_spec
Owner: reporting/platform verifier
Source: NW-156 row in `docs/agent-working-surface/platform-next-work-backlog.md`; NW-155 route note; PR #63 draft diff
Authority: `docs/specifications/platform/scoped-operational-report-snapshot-boundary.md`; `docs/specifications/platform/configuration-package-and-shapes.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
Last reviewed: 2026-06-25
Supersedes: none
Related: `docs/viability-assessment.md`; `docs/scenarios/README.md`; `docs/reviews/scenario-baseline-pressure-map.md`; `docs/agent-working-surface/artifacts/NW-155-reframe-pilot-behavioral-proof-path-and-pr63-classification.md`

## Purpose

This note selects the first bounded way a scoped web-admin reader may inspect
one configured work item as evidence behind the existing scoped operational
report surface.

The goal is platform behavior: prove that configured structured work can be
inspected through the same assignment scope, web-admin authority, configuration,
and evidence-freshness concepts already accepted by Datarun. It is not a
domain UI, stock UI, reporting warehouse, record browser, subject browser,
review workflow, or production approval path.

## Selected Boundary

Selected: a separate read-only scoped evidence detail route linked from the
existing `/web-admin/operational/report` limited trace context.

The report page remains a scoped standing snapshot. It may expose a link or
action for one selected visible work item when the snapshot query already
selected that item as a trace target. It must not render a table/list of
configured records on the report page.

The first implementation should inspect one selected work item at a time. The
route may use an internal event identifier or opaque token as an implementation
detail, but the page must not present internal identifiers as product content.

Rejected for this boundary:

- report-page section listing configured records;
- multi-record configured work browser;
- arbitrary filters, search, sorting, saved views, export, or report API;
- domain-named controller/template/model behavior.

## Authority And Scope Recheck

Every evidence-detail request must re-evaluate current authority before
rendering:

1. valid web-admin browser session;
2. `web_admin.access`;
3. `web_admin.read_scoped`;
4. accepted current assignment scope over the selected event's subject,
   write-time location, and activity;
5. accepted designated-resolver visibility if the selected item is reached from
   an attention context.

Scope is checked before payload reading and before configured-field rendering.
Out-of-scope or non-existent targets must return the same neutral not-found or
no-visible-item behavior. The response must not reveal whether hidden records
exist through wording, counts, gaps, row positions, or totals.

This boundary does not add a new scope mechanism. It uses the platform-fixed
assignment axes: geography, subject list, and activity.

## Allowed Content

The detail page may show:

- generic heading/copy such as `Configured work evidence` or
  `Visible work evidence`;
- activity label/ref from configured activity context;
- record type label/ref from the configured shape version;
- configured field labels and values allowed by this note;
- source work time and/or server received time with freshness caveats;
- scoped visibility caveat.

Configured field rendering is limited to the selected event payload and the
matching configured deployer shape version:

- render only fields declared in that configured shape version;
- preserve configured `display_order`, falling back to field name order;
- use configured field label keys where present, otherwise the configured field
  name;
- render scalar configured values and bounded configured multi-select values as
  text;
- show `Not recorded` only for configured fields that are expected but absent
  or null;
- do not render raw payload keys that are not declared in the configured shape;
- do not render platform payload shapes as deployer form fields.

The page must not show raw subject identifiers, actor identifiers, device
identifiers, device sequence, sync watermark, write-time `location_path`, raw
event JSON, or internal event identifiers as product content. If an internal
identifier is used to route the request, it remains an implementation detail.

## Sensitivity Handling

Configuration preserves shape and activity sensitivity as `standard`,
`elevated`, or `restricted`. This boundary does not create field-level
sensitivity, encryption, redaction, retention, or export behavior.

First implementation may render configured field values only when both the
activity and shape are `standard`. If either is `elevated` or `restricted`, the
route may show the generic activity/record type context and freshness caveat,
but must suppress configured field values until a selected security/retention
route defines sensitivity-specific inspection behavior.

Do not treat UI hiding as a complete sensitivity control. Any need for
redaction, no-local-retention, sensitivity-specific retention, export control,
or field-level sensitivity routes through NW-054 or a selected security route.

## Freshness Wording

Freshness is evidence freshness only. The detail route may show:

- selected work time when the event carries product-safe work time;
- server received/synced time;
- `Latest synced/received`;
- `Coverage not measured`;
- `Visible through current assignment scope`.

The route must not claim all devices are current, all work is complete, the
selected record is operational truth, a due period is complete, or a production
inventory/reporting fact is proven.

## PR #63 Classification

PR #63 is classified as: replace with a narrower implementation.

Rationale:

- The generic service direction in PR #63 is closer to accepted platform
  concepts than a stock-specific UI would be.
- The report-page section listing up to ten configured work rows is too broad
  for the accepted report boundary and for this NW-156 boundary.
- The replacement implementation should remove the report-page record list and
  implement one scoped evidence detail route for one selected visible work item.
- Reusable naming must remain generic; fixture vocabulary may remain only in
  tests and fixture files.

PR #63 must not merge as-is.

## Non-Goals

NW-156 does not select or accept:

- broad reporting, report warehouse, dashboard, saved views, arbitrary filters,
  public report APIs, export, or import;
- raw event timeline, raw subject timeline, subject browser, actor browser,
  geography/activity history browser, or audit-history product;
- multiple configured record lists or browse/search behavior;
- review workflow, review events, resolver reassignment, batch resolution,
  auto-resolution, or queue/list review;
- stock/domain UI concepts, stock ledger, catalog, warehouse lifecycle,
  session lifecycle, or production stock truth;
- new envelope fields/types, new scope mechanisms, sync protocol changes,
  config package schema changes, migrations, contracts, or BAR/CDL changes;
- field-level sensitivity, redaction, retention, encryption, erasure,
  no-local-retention, or export control;
- real users/data, production approval, provider/region/jurisdiction approval,
  local/on-prem preflight, or login/principal-binding implementation.

## Implementation Successor Criteria

A successor implementation may proceed only if it:

- keeps `/web-admin/operational/report` as the scoped standing snapshot;
- adds one read-only scoped evidence detail route for one selected configured
  work item;
- rechecks session, command authority, assignment scope, and resolver
  visibility at detail request time;
- renders only configured fields from the selected event's matching shape
  version and only when activity and shape sensitivity are `standard`;
- hides internal subject, actor, device, location, watermark, event, and raw
  payload identifiers from product content;
- uses freshness/caveat wording from this note;
- includes focused web-admin tests for access denial, out-of-scope neutral
  behavior, hidden identifiers, sensitivity suppression, and configured field
  rendering;
- runs the required server gate from the validation matrix for web-admin
  behavior changes.
