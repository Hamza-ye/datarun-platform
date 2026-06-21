# NW-122 - Implement PC2 single work-linked attention review

## Goal

Implement the Product Candidate 2 boundary selected by accepted NW-121 and
enabled by the accepted NW-072 prerequisite:

```text
Product Candidate 2 - Single Work-Linked Attention Review
```

This NW is runtime implementation work, but only for one bounded
server-rendered web-admin review loop over a single visible unresolved
work-linked attention item. It must not become a reporting surface, review
queue, batch workflow, conflict operations console, resolver reassignment
tool, auto-resolution engine, broad audit/history reader, tenant/control-plane
change, production approval, or real-users/data proof.

## User value / why now

Accepted NW-121 selected PC2 as the next coherent product candidate: PC1 can
show that one visible work item needs review, and PC2 should let the
designated reviewer act on that one item.

NW-072 accepted the required conflict/flag current behavior and operational
attention query boundary. The next bounded implementation successor is now
ready: make the one work-linked attention item reviewable without widening into
queues, reporting, automation, or resolver-policy changes.

## Inputs

Read these first:

- `docs/status.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/commit-workflow.md`
- `server/AGENTS.md`

Read only the implementation surfaces needed for the bounded change:

- `server/src/main/java/dev/datarun/server/event/EventRepository.java`
- `server/src/main/java/dev/datarun/server/authorization/WebAdminOperationalViewService.java`
- `server/src/main/java/dev/datarun/server/authorization/WebAdminOperationalViewController.java`
- `server/src/main/resources/templates/web-admin/operational.html`
- `server/src/test/java/dev/datarun/server/authorization/WebAdminOperationalViewIntegrationTest.java`
- `server/src/main/java/dev/datarun/server/integrity/ConflictResolutionService.java`
- `server/src/main/java/dev/datarun/server/integrity/ResolverRoutingService.java`
- `server/src/main/java/dev/datarun/server/integrity/ConflictController.java`
- `server/src/test/java/dev/datarun/server/integrity/ConflictResolutionIntegrationTest.java`

Use related web-admin controller/template/test files only for existing local
patterns such as session gates, CSRF-protected forms, flash messages, and
focused integration tests. Do not read mobile code, contracts, schemas,
migrations, old IDRs, broad architecture history, or tenant/control-plane
surfaces unless a stop condition fires.

## Required implementation shape

Implement the smallest server-rendered web-admin path that lets a scoped
supervisor/reviewer:

- open one unresolved attention item attached to visible scoped work;
- see product-safe context for the source work and attention reason;
- see resolver standing in product-safe language;
- record one manual resolution decision only when the session actor is the
  exact stored `designated_resolver`;
- return to the scoped operational work context with resolved/unresolved
  standing for that item.

Expected implementation direction:

- Keep the product surface under the production web-admin session boundary.
- Use server-rendered Spring MVC/Thymeleaf only, unless an accepted existing
  surface requires otherwise.
- Keep page visibility behind existing web-admin session and scoped-read
  standing.
- Use the session actor from `WebAdminSessionService`; do not accept actor IDs
  from form fields, query parameters, request bodies, browser state, IdP claims,
  or assignment role labels.
- Use exact stored `designated_resolver` equality as the only resolution
  authority for the item.
- Use accepted NW-072 wording: `designated reviewer`, `assigned reviewer for
  this item`, or `this item is assigned to another reviewer`. Do not expose
  actor IDs, flag-table mechanics, conflict shape names, or resolver internals
  as product language.
- Treat `resolver_unassigned` as blocked/not currently resolvable. Do not
  invent fallback authority, root/admin override, or resolver reassignment.
- Use existing `ConflictResolutionService.resolve(...)` semantics for the
  append-only `conflict_resolved/v1` event when resolving, preserving canonical
  exact-resolver behavior.

## Attention query boundary

NW-072 explicitly forbids extending `WebAdminOperationalViewService` direct
`JdbcTemplate` reach-through for a second operational attention read surface.

The implementation must introduce or use a typed, narrow operational attention
query/read-model boundary before rendering or resolving the PC2 item. It may be
a named `EventRepository` query method or a dedicated query service, but it
must not expose `JdbcTemplate` as product read API and must not add another
direct attention SQL query inside `WebAdminOperationalViewService`.

The boundary must:

- apply accepted assignment-scope predicates before returning source work or
  attention details;
- return only one unresolved attention item attached to visible scoped work;
- treat a flag as unresolved only when no canonical exact-resolver
  `conflict_resolved/v1` exists for it;
- return product-safe resolver standing such as `can_resolve` or
  `assigned_to_current_actor`, without exposing internal resolver IDs in UI
  copy;
- return enough one-item context: source work identity, activity, subject
  reference, server received/work time when available, category/severity/reason
  in product-safe form, and allowed action standing;
- avoid lists, filters, sort controls, dashboards, aggregate counts, exports,
  imports, drillback, audit history, broad subject history, or reporting-style
  completeness/freshness semantics.

## Allowed changes

Use the narrowest implementation surface that satisfies the prompt:

- server-rendered web-admin controller/service/template changes for one
  work-linked attention review item;
- a typed attention query/read-model boundary as described above;
- focused server integration tests for the selected behavior;
- status/backlog acceptance updates after implementation validation.

The implementation may reuse the existing `/web-admin/operational` context or
add a bounded sub-path for the one item. Do not create a broad conflict console
or general `/web-admin/conflicts` product surface.

## Forbidden changes

Do not modify or add:

- reporting dashboards, reporting lists, imports, exports, warehouses,
  aggregate analytics, completeness, drilldown, or broad read APIs;
- review queues, multi-item attention views, filters, batch workflow, conflict
  automation, auto-resolution, pending-match queues, resolver reassignment, or
  root/admin override;
- broad conflict console behavior;
- new resolver eligibility policy, resolver authority, or resolver fallback
  product promise;
- new flag categories, resolution outcomes, event types, schema fields,
  contract shapes, envelope fields/types, sync protocol behavior, or
  migrations;
- mobile code;
- tenant-aware runtime, managed control-plane, workspace-scoped config,
  tenant sync context, pooled storage, tenant isolation harnesses, or tenant UI;
- production approval, real users/data, provider/region/jurisdiction/support,
  compliance/security approval, continuity approval, or go/no-go standing;
- BAR, CDL, validation-matrix, gap-register, or architecture-decision changes
  unless a stop condition fires and the work is rerouted.

Do not add command-authority vocabulary, provisioning policy, IdP group/claim
authority, generic admin authority, or assignment-role authority for resolving
attention items. If a separate web-admin command capability is required before
this review action is safe, stop and route a security/platform successor
instead of adding it inside NW-122.

## Acceptance criteria

NW-122 is accepted only when the implementation:

- shows at most one unresolved work-linked attention item attached to visible
  scoped work;
- applies scope before returning or rendering source work and attention detail;
- handles the no-item and no-scoped-work states with product-safe wording;
- shows enough product-safe work context without leaking event-store,
  flag-table, shape, or resolver-internal vocabulary;
- allows a canonical manual resolution only when the session actor is the exact
  stored `designated_resolver`;
- prevents a non-designated actor from clearing the item and does not let body
  or UI actor spoofing affect resolution authority;
- preserves append-only source work and existing conflict resolution event
  behavior;
- handles `resolver_unassigned` as blocked/not currently resolvable;
- introduces or uses the bounded typed attention query/read-model boundary and
  does not extend direct `JdbcTemplate` reach-through in
  `WebAdminOperationalViewService`;
- stays out of reporting, queues, batch, automation, resolver reassignment,
  broad conflict operations, schema/contract/sync changes, tenant/control-plane
  work, real users/data, and production approval;
- updates backlog/status with exact validation evidence after runtime
  validation passes.

## Focused tests

Add or extend focused server tests to cover:

- authenticated web-admin session and scoped-read gate for the PC2 review path;
- scoped visibility: out-of-scope work and flags do not render;
- unresolved state: resolved/canonical items no longer appear as reviewable;
- canonical exact-resolver resolution clears the item by accepted conflict
  semantics;
- non-designated actor cannot clear the item, including body/UI actor spoofing;
- `resolver_unassigned` renders a blocked/not currently resolvable state and
  has no fallback resolution path;
- no broad read/reporting behavior appears: no queue, multi-item list, filters,
  aggregate counts, exports/imports, drilldown, or audit/history surface.

Expected starting point:

- extend or add a focused web-admin integration test near
  `WebAdminOperationalViewIntegrationTest`;
- keep or add focused conflict/resolution assertions near
  `ConflictResolutionIntegrationTest` only when needed for the web-admin path;
- preserve the existing conflict API tests; do not productize the bearer-token
  `/api/conflicts` route as the PC2 user surface.

## Validation

Use the validation matrix for server behavior and web-admin UI/template work.
Run the narrowest focused test first, then the required server gate for the
touched surface.

Expected commands:

```bash
cd /home/hamza/datarun-platform
git status --short
git diff --check

cd /home/hamza/datarun-platform/server
./mvnw -Dtest=WebAdminOperationalViewIntegrationTest,ConflictResolutionIntegrationTest test
./mvnw test
```

If the implementation creates a new focused test class, include it in the
focused command. Report exact test counts, duration when available,
skipped-gate rationale if any, and CI links when available.

## Stop conditions

Stop and report before implementation or before continuing if the work
requires:

- real users/data or production approval;
- product scope beyond one work-linked attention item;
- a queue, list, filter, batch workflow, automation, auto-resolution,
  reassignment, broad conflict console, or generic conflict operations product;
- reporting/import/export/warehouse/analytics, aggregate completeness,
  drilldown, broad audit/history, or broad read API;
- resolver eligibility promises beyond exact stored `designated_resolver`;
- treating resolver fallback role labels as product authority;
- resolving `resolver_unassigned` items without a selected reassignment route;
- new command authority vocabulary or provisioning policy;
- contract, schema, migration, event type, envelope, sync protocol, authority,
  tenant/control-plane, BAR, CDL, validation-matrix, or gap-register changes;
- extending `WebAdminOperationalViewService` direct `JdbcTemplate` attention
  reach-through instead of using a typed narrow query boundary.

## Commit boundary

Follow `docs/commit-workflow.md`.

Use implementation and acceptance commits as appropriate. Do not combine NW-122
with reporting, automation, resolver policy, contract/schema/sync, tenant,
production, validation-policy, BAR/CDL/gap-register, or unrelated cleanup work.
