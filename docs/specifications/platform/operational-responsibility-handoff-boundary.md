# Operational Responsibility Handoff Boundary

Status: accepted
Document type: platform_spec
Owner: handoff/platform verifier
Source: NW-134 row in
`docs/agent-working-surface/platform-next-work-backlog.md` and
`docs/agent-working-surface/prompts/NW-134-specify-pc4-operational-responsibility-handoff-boundary.md`;
PC4 PM handoff in
`docs/specifications/product/product-candidate-4-pm-handoff.md`; S25 worker
transfer pressure; S27 logistics handoff cross-check; S22 campaign reassignment
continuity example; S19 stale offline authority evidence
Authority: accepted assignment, shared-device/local-state, conflict/flag, and
scoped operational snapshot platform specifications; relevant contracts and
BAR standing as referenced by those specifications
Last reviewed: 2026-06-22
Supersedes: none
Related: `docs/specifications/product/product-candidate-4-pm-handoff.md`;
`docs/specifications/platform/assignment-scope-and-administration.md`;
`docs/specifications/platform/shared-device-session-and-local-state.md`;
`docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`;
`docs/specifications/platform/scoped-operational-report-snapshot-boundary.md`;
`docs/scenarios/25-worker-onboarding-transfer-and-exit.md`;
`docs/scenarios/27-logistics-distribution-composite.md`;
`docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`;
`docs/scenarios/19-offline-capture-and-sync.md`;
`docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`;
`docs/agent-working-surface/validation-matrix.md`

## Purpose

This specification accepts the bounded Product Candidate 4 platform behavior
for one `Operational Responsibility Handoff` before implementation. It defines
the handoff scope, authority inputs, successor-visible context, late offline
work standing, subject-history/sync/projection/actor-local boundaries, caveats,
and no-leakage expectations needed for a bounded implementation route.

This specification does not implement PC4, add runtime code, add tests, add
contracts or schemas, change migrations, change CI or validation policy, mutate
BAR/CDL/gap-register standing, approve real production, mutate the lab, run PC2
live browser proof, accept retention/security/offboarding promises, add
tenant-aware runtime behavior, accept broad reporting/import/export, create a
conflict queue/list/batch workflow, change pattern projection/API behavior, or
accept entity lifecycle.

## Selected Boundary

The selected PC4 boundary is:

```text
Successor start packet for current assigned work, with bounded prior context
and caveated late-work standing.
```

The handoff is one continuity surface. It helps a successor actor understand
what they are currently responsible for, what immediately relevant prior work
is visible to continue that responsibility, and what caveats apply because work
may be late, stale, unresolved, incomplete, or unknown.

It is not a worker-offboarding policy, retained-data promise, broad
audit/history reader, reporting dashboard, assignment-policy editor, conflict
operations console, pattern workflow product, entity lifecycle surface, tenant
runtime feature, or production approval route.

## Boundary Comparison

| Candidate boundary | NW-134 decision | Reason |
|---|---|---|
| Assignment-only handoff context | Rejected as too narrow. | Assignment facts alone do not give a successor enough prior context to continue work. |
| Successor start packet for current assigned work | Selected. | It is the smallest coherent PC4 outcome after PC1-PC3: continue current work after responsibility changes. |
| Late-offline-work handoff standing | Included inside the selected boundary. | Late work after responsibility changes is central to S25/S19, but it is a caveated standing, not a separate product. |
| Supervisor/operator handoff caveat view | Included only as caveats on the selected boundary. | Supervisors may need to see unknown/stale/unresolved standing, but PC4 must not become reporting or queues. |
| Non-health logistics handoff example | Used as the domain-neutral cross-check. | S27 validates the wording outside health vocabulary without selecting custody-specific scope or supply-chain productization. |
| Campaign reassignment continuation example | Used as secondary continuity evidence. | S22 proves reassignment continuity pressure, but discovered-unit lifecycle, completion, trigger, and custom scope semantics stay out. |

## Product Behavior Spec Decision

PC4 does not need a separate accepted product behavior specification now. The
accepted product inputs for a first implementation are the PC4 PM handoff plus
this platform boundary.

A separate product behavior specification becomes the right next route only if
one of these triggers appears:

- the handoff grows beyond one successor-start continuity surface;
- product copy needs owner-approved workflow promises beyond the wording
  guardrails below;
- the proof target requires retention, offboarding, worker-exit, or former-user
  data promises;
- real users/data or production approval are proposed;
- queues, reporting, pattern workflow behavior, entity lifecycle, new scope, or
  tenant-aware behavior becomes selected.

## Users And Authority

The handoff may be used by a successor field worker, successor reviewer,
assignment coordinator, supervisor/operator, or deployment owner. Persona
labels do not grant authority.

Accepted authority inputs:

1. server-resolved actor identity from the accepted auth/session boundary;
2. current active assignments reconstructed from accepted assignment events;
3. accepted assignment scope axes: geographic, subject-list, and activity,
   plus target actor and active-time standing;
4. existing assignment-admin create/end command capability and containment when
   a coordinator changes responsibility;
5. accepted web-admin session and scoped-read standing for any web-admin
   handoff view;
6. accepted mobile active actor session and actor-local partition for field
   handoff context;
7. exact stored `designated_resolver` equality when an unresolved attention
   item can be reviewed.

Not accepted as authority:

- IdP groups, roles, resource claims, custom claims, or JWT `actor_id`;
- UI-selected actor, browser-selected actor, request-body actor, or local-only
  actor identity;
- generic admin/root labels, supervisor labels, coordinator labels, assignment
  role labels alone, report-view labels, or product persona labels;
- tenant/workspace selection;
- another actor's sealed pending work or local partition contents.

## Handoff Scope

The handoff scope is the successor actor's current authorized responsibility in
the current managed-isolation lane:

- one customer-facing Organization;
- one managed single-tenant Datarun deployment;
- one internal/default Workspace;
- the successor actor's current accepted assignment scope.

The handoff may cover only current assigned work and bounded prior context
needed to continue that current responsibility. All context selection must
apply accepted assignment-scope predicates before choosing rows, counts,
latest-time indicators, caveats, trace targets, or empty states.

Out-of-scope records must not affect visible context, counts, caveats,
latest-work labels, hidden-record hints, or unknown/stale standing.

Future-dated scheduled activation remains caveated by the assignment spec: do
not build product/API commitments that rely on future-dated activation without
a follow-up route and guard tests.

## Successor-Visible Current Work And Prior Context

The first implementation may present a bounded handoff context with these
contents:

| Content | Accepted meaning | Boundary |
|---|---|---|
| Current responsibility summary | Product-safe summary of the successor actor's current assigned work. | No raw scope-axis, tenant, workspace, watermark, or internal role-authority vocabulary. |
| Current assigned work | The visible current work or activities the successor can continue under current assignment scope. | No broad work list, reporting dashboard, arbitrary filters, or hidden out-of-scope totals. |
| Bounded prior context | Prior visible source work for the same currently authorized subject/activity slice that is needed to continue work. | Not actor history, geography history, broad subject-history browser, or audit pull. |
| Handoff caveats | Product-safe standing for unresolved attention, stale/late synced work, incomplete context, or unknown freshness. | No completion/all-clear, all-devices-current, SLA, or production-readiness claim. |
| Limited trace targets | Existing accepted scoped operational work, one accepted attention context, or product-safe source-work labels/times. | Trace targets must re-check current scope and must not expose out-of-scope existence. |

Bounded prior context is useful only when it helps the successor continue the
current assignment. It must not become a general "everything the prior worker
did" view.

## Late Offline Work After Responsibility Changes

Late offline work remains append-only and traceable under accepted behavior:

- structurally valid outgoing-actor work may be persisted when it syncs after a
  central responsibility change;
- server-side authority checks may flag that work as
  `temporal_authority_expired`, `role_stale`, `scope_violation`, or another
  accepted attention category when applicable;
- the original actor attribution remains intact;
- canonical resolution and unresolved-flag state-exclusion behavior remain
  governed by the accepted conflict/flag boundary;
- successor context may show late synced work only when it is visible inside
  the successor actor's current scope and caveated in product-safe language.

Late work must not be silently treated as clean current authority merely
because it arrived. It also must not disappear or be rewritten because
responsibility changed.

The handoff must not:

- transfer sealed pending work between actor partitions;
- reauthor outgoing-actor work as successor work;
- push another actor's pending queue;
- rewrite normal sync watermarks;
- delete or mutate server event history;
- promise purge, encryption, redaction, erasure, no-local-retention, or
  offboarding cleanup.

## Subject-History, Sync, Projection, And Actor-Local Boundaries

| Area | Accepted PC4 boundary |
|---|---|
| Normal sync | Pull remains bearer-authenticated, actor-scoped, watermark-based, and evaluated against current assignments at request time. It is not a historical audit pull. |
| Subject-history | May support bounded repair or prior context only for currently authorized subject/activity slices, with authorization checked on every page and an independent cursor. It must not mutate normal pull watermarks or become actor/geography/activity history. |
| Projection | Handoff context may use accepted rebuildable projections and current conflict/flag exclusion behavior. It must not add durable workflow-state authority, pattern projection changes, or new state tables. |
| Actor-local state | Mobile handoff context uses only the active actor partition. Prior actors' local events, projections, pending queues, watermarks, cursors, and tokens remain hidden and unpushable to the successor. |
| Sealed pending work | A prior actor's sealed pending work remains sealed until same-actor resume or a separately accepted recovery path exists. PC4 does not create admin recovery, export, or cross-actor transfer. |
| Shared config blobs | Immutable package blobs may be shared only under the existing shared-device spec. Active/pending config state remains actor-local. |

## Caveat States

The handoff must be explicit when context is not settled.

| Caveat | Product-safe meaning | Must not imply |
|---|---|---|
| `needs attention` | Visible work has unresolved attention under accepted conflict/flag behavior. | Queue/list review, batch action, auto-resolution, resolver reassignment, or generic supervisor authority. |
| `late synced work` | Work became centrally visible after responsibility changed or after the successor context was prepared. | That the work is clean, invalid, deleted, or transferred to the successor. |
| `stale responsibility` | A synced item may have been created after central authority changed. | Discipline, fraud, automatic rejection, or offboarding policy. |
| `context incomplete` | The handoff can show only currently visible accepted context. | All devices current, all work complete, or no hidden records exist. |
| `freshness unknown` | Accepted inputs do not prove how current the handoff context is. | SLA breach, overdue standing, or real-time device heartbeat. |
| `not currently resolvable` | The accepted attention item has no current human resolution route, or the session actor is not the exact designated resolver. | Root/admin override, fallback resolver, or reassignment authority. |

## No-Leakage And No-Broad-Audit Expectations

The handoff must apply current accepted access before all selection,
aggregation, ordering, latest-time, caveat, and trace decisions.

Required rules:

- no hidden out-of-scope counts, totals, row gaps, "more records exist", or
  unauthorized-history hints;
- empty means no visible handoff context for the current actor, not no
  Organization work;
- trace targets must re-check current session, command/read capability, and
  accepted assignment/designated-resolver visibility before rendering;
- subject-history use must stay subject/activity-bound and current-scope
  authorized;
- normal sync must not be converted into historical pull;
- a handoff view must not expose another actor's local partition, sealed
  pending work, token/session material, local projection state, or sync cursor.

Broad actor history, assignment audit history, geography/activity history,
subject-history browsers, raw event-store browsers, report APIs, exports,
imports, warehouses, hidden-total hints, and broad drilldown remain out of
scope.

## Scenario Use

| Scenario | PC4 use | Boundary preserved |
|---|---|---|
| S25 worker onboarding, transfer, leave, and exit | Primary synthetic example. The successor receives current responsibility plus bounded prior context; stale offline work remains traceable and caveated. | No local purge, encryption, erasure, no-local-retention, exit/offboarding procedure, or former-worker retained-data promise. |
| S27 logistics distribution across multiple handoffs | Domain-neutral cross-check. Chain-of-responsibility and discrepancy context must be explainable without health vocabulary. | No custody-specific scope, supply-chain product bundle, auto-resolution, pattern API, or broad operational history. |
| S22 coordinated distribution campaign | Secondary continuity example. Reassignment during execution can show current work plus what was already done. | No discovered-unit lifecycle, campaign completion semantics, trigger execution, custom campaign scope, or broad reporting. |
| S19 offline capture and sync | Late offline work validates the stale/late caveat model. | No sync protocol change, normal-watermark rewrite, broad audit pull, or mobile authoritative rejection. |

## Product Wording Guardrails

Product-facing wording may use:

- `Operational Responsibility Handoff`;
- `handoff context`;
- `current assigned work`;
- `prior context`;
- `late synced work`;
- `needs attention`;
- `context incomplete`;
- `freshness unknown`;
- `not currently resolvable`;
- `designated reviewer`.

Product-facing wording must not use or imply:

- tenant, workspace selector, sync watermark, subject-history cursor,
  actor partition, projection repair, event store, flag table, resolver
  internals, pattern engine, query boundary, retained-data mechanism, or
  local-state storage as product authority;
- complete history, broad audit trail, all devices current, all clear,
  complete, overdue, SLA, report-ready, production-ready, or real-time;
- securely erased, encrypted, purged, retained for a fixed period, recoverable
  by admin, no local copy, no-local-retention, offboarded, or safe for real
  production;
- generic admin, root, supervisor, coordinator, IdP claim/group, assignment
  role alone, or UI-selected person as authority.

## Non-Goals

NW-134 and the first PC4 implementation do not select or accept:

- PC2 live browser proof, PC2 lab reconciliation, or lab mutation;
- real users/data, provider/region/jurisdiction/support, compliance/security,
  continuity, go/no-go, or production approval;
- worker offboarding policy, local expiry, device decommissioning,
  sealed-partition recovery, local encryption, token/session retention,
  no-local-retention, erasure, redaction, sensitivity handling, administrator
  recovery/export, or former-worker retained-data promises;
- new subject/query/custom scope, cross-activity cohort materialization,
  query-as-config authority, hidden sync scope, auditor scope, emergency scope,
  or grace scope;
- broad reporting, import, export, warehouse, analytics, report APIs, report
  catalog, saved views, dashboards, arbitrary filters, completeness,
  completion, cadence, percentages, drilldown, or interoperability reporting;
- conflict queues, lists, filters, multi-item review, batch workflow,
  pending-match queues, conflict automation, auto-resolution, resolver
  reassignment, broad conflict console, or resolver eligibility broadening;
- pattern traversal/reporting, pattern inventory expansion, pattern API work,
  pattern migration, workflow projection changes, trigger execution, or durable
  workflow-state tables;
- S06/entity lifecycle, maintained known-set registry, discovered-unit
  lifecycle, deactivation, candidates, duplicates, merge/split UX, or registry
  stewardship;
- tenant-aware runtime, managed control plane, workspace-scoped config, tenant
  sync context, pooled storage, tenant isolation harness, or UI tenant choice;
- contracts, schemas, envelope fields/types, assignment payload changes, sync
  protocol changes, migrations, validation policy, CI, BAR, CDL, or gap-register
  mutations.

## Preserved Trigger Routes

| Route | Preserved trigger | NW-134 decision |
|---|---|---|
| NW-093 | Real users/data, provider, region, jurisdiction, support, compliance/security, continuity, or go/no-go. | Not selected; still blocked. |
| NW-126 | Lab hostname or fixed-IP SSH access restored enough to inspect R12 before touching retained PC2 state. | Not selected; still blocked. |
| NW-044 | Broad reporting/import/export, warehouse, analytics, broad report APIs, report catalog, cadence, completion, completeness, drilldown, or interoperability reporting. | Not selected; PC4 is handoff continuity, not reporting. |
| NW-045 | Batch decisions, pending-match queues, auto-resolution, resolver reassignment, or conflict automation. | Not selected; manual exact-resolver semantics remain. |
| NW-053 | Handoff needs access not representable by accepted assignment scope and boundary semantics. | Not selected; accepted assignment scope and boundary semantics are sufficient for the first handoff boundary. |
| NW-054 | Retention, expiry, encryption, erasure, redaction, no-local-retention, offboarding, sensitivity, or former-worker retained-data promises. | Not selected; PC4 makes no retention/security promise. |
| NW-073 | Pattern traversal/reporting, inventory expansion, projection change, pattern API/product work, or normative pattern behavior dependency. | Not selected; S22/S27 are examples only. |
| NW-021 | Maintained known set, discovered-unit lifecycle, deactivation, candidate/duplicate stewardship, or merge/split UX. | Not selected; PC4 continues assigned work, not entity lifecycle. |
| NW-036 | Combined lifecycle, trigger execution, reporting, analytics, or other broad future-surface package. | Not selected; PC4 remains one handoff boundary. |
| NW-094, NW-095, NW-096, NW-097, NW-098 | Managed control plane, tenant-aware identity/runtime/storage/sync/config, tenant isolation proof, or UI tenant choice. | Not selected; managed-isolation lane remains. |

## Security / Secure SDLC Gates

- Real users/data require NW-093 first.
- Handoff authority must remain server-resolved actor identity plus accepted
  assignment scope and exact designated-resolver standing where applicable.
- Product copy must not imply IdP group/claim/JWT `actor_id`, generic admin,
  assignment role alone, UI-selected actor, tenant/workspace selection, or
  former-worker local-state access as authority.
- Retention/security/offboarding claims require NW-054 before product,
  implementation, proof, or customer-facing wording can rely on them.
- Broad audit/history, emergency access, special read/write access, or dynamic
  access scope remains outside PC4 unless a successor product/security decision
  selects it.
- Secure SDLC review is a real-use gate, not a PC4 implementation prerequisite
  for synthetic/non-sensitive proof.

## Reliability / Operations Gates

- Synthetic/non-sensitive proof is allowed after implementation.
- PC1, PC2, and PC3 remain synthetic-demo-ready only, not
  real-production-ready.
- PC2 live-lab proof remains blocked under NW-126 and is not advanced by PC4.
- Real-use operations readiness, provider/region, jurisdiction, support,
  continuity, compliance/security, and go/no-go remain NW-093-gated.
- No operations policy, runbook, rehearsal record, backup, monitoring,
  incident-response, support, or lab standing changes in NW-134.

## Validation Gates And Known Debt

For NW-134 itself, docs-only validation is sufficient because only
specification, routing, and index files change.

Future PC4 implementation must use the validation matrix for the touched
runtime surfaces. If the successor touches server web-admin behavior, run the
focused web-admin/server tests and full server gate required by the matrix. If
it touches mobile UI or local-state behavior, run the relevant focused Flutter
tests and full mobile gate, with Android compile only when native/platform/auth
surfaces are touched.

Known validation standing remains unchanged:

- `flutter analyze` is known-red and non-blocking until fixed or baselined;
- PC2 live browser proof is blocked under NW-126;
- real-production proof and Secure SDLC approval remain NW-093-gated;
- no validation-policy or CI behavior changes are accepted by NW-134.

## Implementation Successor Readiness

Binary readiness call:

```text
One implementation successor is ready.
```

The selected successor is:

```text
NW-135 - Implement PC4 operational responsibility handoff.
```

NW-135 is ready because this spec accepts one bounded handoff context, accepted
authority inputs, successor-visible current work and prior context, late offline
work caveats, subject-history/sync/projection/actor-local boundaries,
no-leakage expectations, product-safe wording, preserved trigger routes, and
validation gates.

NW-135 must stop before implementation if it needs any prerequisite listed in
the preserved trigger routes, including NW-093, NW-126, NW-044, NW-045, NW-053,
NW-054, NW-073, NW-021, NW-036, or NW-094 through NW-098.

## Implementation Acceptance Criteria For NW-135

A later NW-135 implementation may be accepted only if it:

- implements one bounded handoff context for the PC4
  `Operational Responsibility Handoff`;
- shows current assigned work and bounded prior context for the successor actor
  inside accepted current assignment scope;
- caveats late synced work, stale authority, unresolved attention, incomplete
  context, and unknown freshness without overclaiming completeness;
- applies accepted scope before selecting context, caveats, latest times, trace
  targets, or empty states;
- uses accepted subject-history behavior only for subject/activity-bound,
  current-scope-authorized repair or prior context;
- preserves actor-local partitions and sealed pending work boundaries;
- preserves exact designated-resolver behavior for attention and treats
  `resolver_unassigned` as blocked/not currently resolvable;
- uses product-safe wording from this spec;
- includes focused tests for successor visibility, no-leakage/out-of-scope
  denial, late offline/stale caveats, unresolved attention treatment, and
  actor-local/sealed-work boundaries for touched surfaces;
- runs required full gates from the validation matrix for touched runtime
  surfaces;
- stays out of retained-data promises, new scope, broad audit/history,
  reporting/import/export, conflict queue/list/batch/automation, pattern
  projection/API changes, entity lifecycle, tenant/control-plane, contracts,
  schemas, migrations, sync protocol changes, BAR, CDL, validation-policy
  changes, lab mutation, real users/data, and production approval.

## Acceptance Evidence For NW-134

NW-134 is docs-only platform-specification work.

Required validation:

```bash
git diff --check
rg "NW-134" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/specifications/platform/operational-responsibility-handoff-boundary.md
rg "Operational Responsibility Handoff" docs/specifications/platform/README.md
test -f docs/agent-working-surface/prompts/NW-135-implement-pc4-operational-responsibility-handoff.md
rg "NW-135" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/specifications/product/product-candidate-4-pm-handoff.md docs/agent-working-surface/prompts/NW-135-implement-pc4-operational-responsibility-handoff.md
rg "NW-093|NW-126|NW-044|NW-045|NW-053|NW-054|NW-073|NW-021|NW-036|NW-094|NW-095|NW-096|NW-097|NW-098" docs/specifications/platform/operational-responsibility-handoff-boundary.md
```

Runtime tests are skipped for NW-134 because this specification changes no
runtime code, tests, contracts, schemas, migrations, CI behavior, validation
policy, product behavior spec, BAR, CDL, gap register, mobile code,
server/web-admin implementation, reporting/import/export behavior, conflict
automation, pattern behavior, entity lifecycle, retention/security/offboarding
policy, tenant/control-plane behavior, real-production approval, real
users/data, lab state, or PC2 live proof.
