# Product Candidate 3 PM Handoff

Status: active PM handoff surface
Document type: product_handoff
Owner: product steward
Source: NW-127; accepted PC1 and PC2 handoffs; NW-120 PC2 intake; NW-124 PC2 synthetic walkthrough proof; NW-125 PC2 lab environment standing; scenario baseline pressure map; viability closure review
Authority: derived planning surface only; does not add product behavior, runtime implementation, production approval, architecture authority, validation policy, contracts, schemas, BAR, CDL, or gap-register standing
Last reviewed: 2026-06-22
Supersedes: none
Related: `product-candidate-1.md`; `product-candidate-1-pm-handoff.md`; `product-candidate-2-pm-handoff.md`; `../platform/scoped-operational-report-snapshot-boundary.md`; `../../agent-working-surface/platform-next-work-backlog.md`; `../../agent-working-surface/prompts/NW-128-specify-pc3-scoped-operational-report-snapshot-boundary.md`

## Purpose

This handoff selects exactly one Product Candidate 3 boundary and gives a PM
planning surface for sequencing it professionally. It does not implement PC3,
accept product behavior, approve production, change validation policy, change
architecture authority, or mutate the gap register.

The selected boundary is:

```text
Product Candidate 3 - Scoped Operational Report Snapshot
```

## Product Goal

Give a scoped supervisor or organization operator one read-only report snapshot
for the current Organization: enough freshness, progress, unresolved-issue
treatment, and traceability context to understand current operational standing
without becoming a reporting suite, export/import system, analytics warehouse,
broad audit/history reader, conflict queue, batch workflow, or tenant-aware
control-plane surface.

## Why This Boundary Wins

| Candidate front | Standing | Why not selected as PC3 |
|---|---|---|
| Setup/admin polish | Useful but incremental. | PC1 already proves setup/config as a first vertical; richer editing can be selected later when setup friction is the proof blocker. |
| Assignment/admin operations polish | Useful but incremental. | PC1 already proves responsibility create/end; polish does not create the next coherent product outcome by itself. |
| Mobile field workflow polish | Useful but incremental. | PC1 already proves get-work, capture, correction, sync, and login; mobile polish should follow observed proof gaps. |
| Readiness/freshness/attention expansion beyond PC2 | Selected in narrowed form. | PC3 selects the reporting/freshness snapshot part only, not a larger operational console. |
| Reporting/import/export/aggregate oversight | Selected in bounded form only. | The selected boundary is one scoped read-only snapshot. Structured export/import, warehouse, broad report APIs, and analytics remain unselected. |
| Conflict queue or resolution workflow beyond PC2 | Not selected. | PC2 already accepted one work-linked manual review item. Queue/list/multi-item ergonomics, batch, automation, and resolver reassignment remain separate routes. |
| Pattern registry/projection follow-through | Not selected. | PC3 must not rely on pattern traversal, inventory expansion, pattern API work, or projection changes. NW-073 is selected only if a later route proves this PC3 boundary actually depends on pattern registry/projection behavior. |
| S06/entity lifecycle | Not selected. | Known-set lifecycle, deactivation, candidate/duplicate stewardship, and merge/split UX remain NW-021 future-decision work. |
| Tenant/control-plane | Not selected. | Managed deployment/control plane and tenant-aware internals remain NW-094 through NW-098 trigger-based work. |
| Delivery-readiness/security/reliability proof work | Not selected as PC3. | Real users/data, support, compliance/security, continuity, and go/no-go still require NW-093. PC2 live-lab proof remains blocked under NW-126. |

This boundary wins because PC1 proves that work can be configured, assigned,
captured, corrected, synced, and surfaced; PC2 proves that one visible issue can
be reviewed manually; the next coherent product question is whether a
supervisor or operator can understand current scoped standing without hunting
through individual records.

## Target Deployment / Customer Archetype

- Customer or organization shape: one customer-facing Organization.
- Deployment lane: one managed single-tenant Datarun deployment with one
  internal/default Workspace.
- Real-production standing: not approved; real users/data remain behind
  NW-093.
- PC2 lab standing: live PC2 browser proof remains blocked by NW-126 until lab
  hostname or fixed-IP SSH access is restored and R12 is inspected first.
- Internal vocabulary that must not leak into product language: tenant,
  workspace, event store, projection implementation, sync watermark, flag table,
  resolver internals, report warehouse, and query boundary.
- Approval route if real users or data are involved: NW-093 before real use.

## Primary Users / Jobs

| User | Job | Notes |
|---|---|---|
| Supervisor/reviewer | Understand current scoped operational standing and unresolved issues. | The snapshot is read-only and must preserve existing assignment-derived visibility. |
| Organization operator | See whether configured work is moving, stale, or blocked enough to need follow-up. | This is not real-production approval or a support/SLA surface. |
| Assignment coordinator | Interpret operational coverage without changing assignment authority. | The snapshot must not create new scope mechanisms or command authority. |
| Setup owner/config author | Check whether setup-driven work produces interpretable operational evidence. | The snapshot must not add config scripts, report schemas, or pattern mechanisms. |
| Deployment owner | Keep proof, real-use approval, security, and reliability standing separate. | Synthetic evidence is allowed; real-use claims require NW-093. |

## In-Scope Journeys

- Open one scoped, read-only operational report snapshot for the current
  Organization and current authorized scope.
- See freshness context for the snapshot, such as last source-work time, latest
  projected input time, server-received time, sync-watermark context, or an
  explicit unknown/stale standing as selected by the prerequisite boundary.
- See progress or standing for configured work in product-safe terms without
  exposing platform internals.
- See how unresolved issues are treated: affected work may be excluded,
  caveated, or counted as unresolved according to the prerequisite boundary.
- Trace from the snapshot to existing accepted work or attention context without
  opening a broad audit/history reader.
- Understand that the snapshot is scoped and may be incomplete or stale.

## Explicit Non-Goals

| Non-goal | Deferred route | Do-not-cross boundary |
|---|---|---|
| Structured export, import, warehouse, analytics storage, broad report APIs, or interoperability reporting | NW-044 | PC3 is one read-only snapshot, not reporting infrastructure. |
| Dashboards with arbitrary filters, multi-report catalog, saved views, or report designer | NW-044 or a later bounded product route | Do not turn PC3 into a reporting suite. |
| Conflict queue/list/multi-item review, batch workflow, resolver reassignment, or auto-resolution | GAP-CONFLICT-01 successor or NW-045 as applicable | Keep PC2 one-item review separate from PC3 snapshot. |
| Pattern traversal/reporting, inventory expansion, pattern API work, or projection changes | NW-073 if the selected work actually depends on it | Do not select NW-073 by label alone. |
| Entity lifecycle, known-set registry, candidates, deactivation, merge/split UX, or discovered-unit lifecycle | NW-021 | Recording/reporting about subjects is not S06 lifecycle acceptance. |
| New scopes, subject/query/custom scope, cross-activity cohort materialization, or hidden sync scope | NW-053 | Preserve accepted assignment-derived access. |
| Retention/security/offboarding promises, local encryption, erasure, redaction, or no-local-retention | NW-054 | A snapshot must not become a retained-data or privacy guarantee. |
| Tenant-aware runtime, pooled storage, workspace-scoped config, tenant sync context, tenant isolation harness | NW-094 through NW-098 | Managed-isolation vocabulary only; no runtime tenant drift. |
| Real users/data, provider/region/jurisdiction/support, compliance/security, continuity, or go/no-go | NW-093 | Synthetic planning/proof only until real-use trigger. |

## Current Standing

| Product slice | Current standing | Accepted NW/source | Remaining route | PM interpretation |
|---|---|---|---|---|
| PC1 operational loop | Synthetic-demo-ready, not real-production-ready. | NW-119; PC1 spec/handoff | Real use still routes through NW-093. | PC3 can build from accepted setup, assignment, mobile, and minimal operational visibility. |
| PC2 attention review | Synthetic-demo-ready from implementation/test evidence; live browser proof blocked externally. | NW-122 through NW-125 | NW-126 remains blocked PC2 lab debt. | PC3 can be selected without claiming PC2 live-lab proof. |
| Minimal operational view | Latest visible work and one read-only cue were accepted for PC1; one work-linked review item was accepted for PC2. | NW-114; NW-122 | A reporting-like snapshot needs a boundary before implementation. | Existing operational views are not precedent for broad reporting. |
| S26 reporting evidence | Runtime probe proves constrained freshness inputs, unresolved-flag handling, scoped visibility, and event traceability without a warehouse. | NW-033; scenario pressure map | PC3 must specify the product/platform boundary before delivery. | S26 is the main evidence for PC3, not an accepted reporting product. |
| Reporting/import/export | Deferred/future decision. | NW-044; GAP-PROJECTION-01/02 | Use NW-044 for broad reporting/export/import, or a bounded reporting spec for this PC3 snapshot. | PC3 selects the bounded spec route, not the broad NW-044 route. |
| Conflict/flag current behavior | Current one-item behavior accepted for PC2. | NW-072; NW-122 | Queue/list/multi-item and automation remain future. | PC3 may show unresolved issue treatment, not resolve many issues. |
| Pattern behavior | Current patterns have runtime evidence, but durable pattern follow-through remains a candidate route. | BAR-012/014; NW-073 candidate | Select NW-073 only if PC3 boundary depends on pattern behavior as normative. | PC3 is scoped to report freshness/standing, not pattern inventory. |
| Real production | Blocked. | NW-093 | Select only on real-use trigger. | PC3 planning remains non-production. |
| Tenant/control-plane | Deferred/trigger-based. | NW-083; NW-094 through NW-098 | Select only on managed control-plane or tenant-aware runtime trigger. | PC3 stays in managed-isolation lane. |

## Scenario-to-Slice Map

| Scenario pressure | User value | PC3 journey | Current support | Candidate NW route | Next product decision | Do-not-cross boundary |
|---|---|---|---|---|---|---|
| S26 operational reporting and aggregate oversight | Supervisor sees freshness, unresolved issue treatment, scoped visibility, and traceability. | Open one scoped operational report snapshot. | NW-033 runtime probe proves the ingredients without report infrastructure. | NW-128 selected prerequisite boundary. | Which freshness, completeness, unresolved-issue, and trace paths are product-safe for PC3? | No warehouse, export/import, broad report API, or hidden access expansion. |
| S02 recurring reporting | Operators may need expected reporting standing over time. | Snapshot may show current period standing only if boundary keeps it bounded. | Scenario pressure exists; broad cadence obligations are not accepted product behavior. | NW-128 may include a limited current-period definition or defer cadence. | Is current-period standing in scope, or future? | No trigger engine, recurring obligation automation, or report designer. |
| S21 supervisor review / PC2 | Existing attention review should inform unresolved issue treatment. | Snapshot may show unresolved issue counts or a link to accepted attention context. | PC2 one-item review accepted. | NW-128 must avoid queue/list/multi-item review. | What issue treatment is visible without creating a queue? | No batch, automation, reassignment, or broad conflict console. |
| S00 structured capture | Source records remain trustworthy and append-only. | Snapshot derives from accepted source work and does not mutate it. | Runtime probe proves append-only correction and idempotency. | Future implementation tests after NW-128. | What source-work trace is enough? | No event mutation or new correction linkage. |
| S19 offline sync | Freshness and staleness must be visible when devices sync late. | Snapshot shows stale/unknown/fresh wording selected by NW-128. | Runtime probe proves stale work is persisted and flagged. | NW-128 freshness boundary. | Which freshness signals are understandable and not overclaimed? | No sync protocol or watermark rewrite. |
| S23 setup/config | Configured work should produce interpretable standing. | Snapshot uses accepted setup/activity vocabulary. | Runtime probe proves bounded setup/config delivery. | None unless setup semantics change. | Which setup labels are safe to show? | No config scripts, schema changes, or dynamic queries. |
| S22/S27 campaign/logistics composites | Non-health and coordinated work need progress visibility. | Snapshot can use synthetic examples without domain-specific platform semantics. | Runtime probes prove current constructs across campaign and logistics examples. | NW-128 examples only. | Which example best explains PC3 proof? | No custody-specific scope, domain-coded platform concepts, or pattern inventory expansion. |
| S06 entity lifecycle | Maintained known-set standing may be requested later. | Deferred from PC3. | Future-decision only. | NW-021. | Keep deferred. | No registry lifecycle or merge/split UX. |

## Candidate NW Decomposition Routes

These are candidate routes only unless a later NW marks the route accepted.
Promote at most one bounded successor at a time.

| Candidate route | Suggested priority | User value / why now | Input sources | Output expected | Acceptance evidence | Stop condition |
|---|---|---|---|---|---|---|
| NW-128 - Specify PC3 scoped operational report snapshot boundary | Accepted prerequisite | PC3 is reporting-like and needed freshness, completeness, unresolved-issue treatment, access, traceability, and read-query semantics before implementation. | This handoff; NW-033/S26; PC2 handoff and proof; validation matrix; gap playbook. | Accepted platform boundary at `../platform/scoped-operational-report-snapshot-boundary.md`; implementation readiness call. | Docs-only validation; platform/product index checks; status/backlog trace. | Stop on export/import, warehouse, broad audit, new scope, pattern dependency requiring NW-073, or production approval. |
| NW-129 - Implement PC3 scoped operational report snapshot | Selected P1 successor | Gives the supervisor/operator the selected one-snapshot experience. | Accepted NW-128 boundary; existing web-admin patterns; accepted access/conflict specs. | One read-only server-rendered `/web-admin/operational/report` snapshot and focused tests. | Focused web-admin/reporting tests plus required server gate. | Stop on broad reporting, queue/list review, contracts/schemas/sync, tenant/control-plane, real users/data. |
| PC3 synthetic proof packet | P2 after implementation | Lets PM/owner validate the snapshot journey with synthetic evidence. | PC3 handoff; accepted implementation evidence. | Product-validation artifact or proof route. | Docs-only proof or live synthetic proof if environment is selected. | Stop on real users/data or PC2 lab proof debt. |
| NW-044 broad reporting/import/export boundary | Future | Needed if product asks for exports, imports, warehouses, analytics, broad report APIs, or report catalog. | S26 and reporting pressure. | Broad reporting/product-platform decision route. | Accepted decision before implementation. | Do not fold broad reporting into PC3. |
| Conflict queue/list/multi-item ergonomics | Future | Needed only if one-item attention handling is insufficient. | PC2 standing; GAP-CONFLICT-01. | Bounded queue/multi-item route. | Accepted route before implementation. | Do not add batch, automation, resolver reassignment, or broad conflict console. |
| NW-073 pattern registry/projection follow-through | Conditional future | Needed only if NW-128 or implementation depends on pattern registry/projection behavior as normative. | Pattern evidence; backlog trigger map. | Durable pattern behavior extraction. | Accepted NW-073 before dependent work. | Do not select by label alone. |

## Selected Successor Route

Exactly one successor route is selected:

```text
NW-129 - Implement PC3 scoped operational report snapshot
```

NW-128 accepted the required platform boundary for one current scoped
operational standing snapshot with limited traceability. It also confirmed
that no NW-044, NW-073, NW-053, NW-054, NW-093, NW-126, or NW-094 through
NW-098 prerequisite is selected for the first PC3 implementation.

## Product-Level Definition Of Done

| Journey | Done when user can | Evidence category | Required guardrail | Detailed validation owner |
|---|---|---|---|---|
| Open snapshot | Open one scoped read-only operational report snapshot. | Product walkthrough plus focused web-admin evidence after implementation. | Scope applies before visibility; no broad report API. | Agent validation matrix. |
| Understand freshness | See fresh/stale/unknown context without overclaiming completeness. | Focused rendering/controller tests after implementation. | No sync watermark rewrite or real-time guarantee. | Agent validation matrix. |
| Understand unresolved issue treatment | See how unresolved issues affect counts, standing, or trace links. | Focused tests and product review after implementation. | No queue/list review, batch, automation, or resolver reassignment. | Agent validation matrix. |
| Trace safely | Follow from snapshot context to accepted work or attention detail without broad audit/history. | Focused scoped-read/no-leakage tests after implementation. | No hidden sync scope, broad subject-history reader, or export. | Agent validation matrix. |
| Stay bounded | Snapshot avoids dashboards, report catalogs, exports, imports, warehouse, analytics, and tenant/control-plane behavior. | Product/reviewer inspection and route checks. | Route broader reporting through NW-044. | Agent validation matrix. |

## Validation Matrix Link

- Detailed validation matrix:
  [agent validation matrix](../../agent-working-surface/validation-matrix.md).
- Known red checks: `flutter analyze` remains known-red and non-blocking until
  fixed or baselined.
- Checks that belong to future validation/CI reset: analyzer cleanup or
  baseline, server log-volume reduction, mobile fake/harness cleanup, and
  shared fixture/contract parity improvements only when selected or triggered.

For this NW-127 handoff itself, docs-only validation is sufficient:

```bash
git diff --check
rg "NW-127" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/specifications/product/product-candidate-3-pm-handoff.md
rg "Product Candidate 3 PM Handoff" docs/specifications/product/README.md
```

Runtime tests are skipped because the handoff changes no runtime code, tests,
contracts, schemas, CI behavior, validation policy, product behavior
acceptance, BAR, CDL, or gap-register standing.

## Security / Secure SDLC Gates

- Real users/data require NW-093 first.
- Product copy must not imply IdP group/claim/JWT `actor_id`, generic admin,
  assignment role, UI-selected tenant, or report-view authority.
- Broad audit/history, special read/write access, redacted/no-local-retention,
  erasure, encryption, or sensitivity-specific local retention remains outside
  PC3 unless a successor product/security decision selects it.
- Secure SDLC review is a real-use gate, not a PC3-selection blocker.

## Reliability / Operations Gates

- Synthetic reference deployment evidence remains current baseline only.
- PC3 demos/proofs may use synthetic/non-sensitive evidence.
- PC2 live-lab proof remains blocked under NW-126 and must not be claimed by
  PC3 selection.
- Real-use operations readiness, support, continuity, provider/region, and
  go/no-go remain NW-093-gated.
- No operations runbook, deployment policy, backup, monitoring, or incident
  response standing changes in NW-127.

## Architecture / Platform Prerequisites

NW-128 is the required PC3 prerequisite before implementation. It must define
the bounded product/platform behavior for:

- snapshot scope and access;
- freshness and staleness wording;
- completeness and uncertainty caveats;
- unresolved issue exclusion/count/link treatment;
- traceability to existing work or attention context;
- read-model/query boundary;
- denial/no-leakage expectations;
- validation evidence required for implementation.

NW-128 must stop and route instead of specifying PC3 if it requires new
envelope fields/types, stored event meaning changes, sync/access scope changes,
new scope mechanisms, durable workflow-state authority, reporting warehouse or
export/import contracts, broad audit/history access, pattern traversal or
projection changes that trigger NW-073, tenant-aware runtime behavior, or real
production approval.

## NW-093 Production Approval Standing

NW-093 remains blocked. Select it only when concrete real users, real
organizational data, provider, region, jurisdiction, support,
compliance/security, continuity, or go/no-go pressure appears.

PC3 planning and synthetic proof do not approve production.

## NW-126 PC2 Live-Lab Proof Standing

NW-126 remains blocked until lab hostname or fixed-IP SSH access is restored
enough to inspect R12 before touching retained PC2 state.

PC3 selection does not close PC2 live-lab proof debt, does not run a live
browser proof, and does not authorize lab mutation.

## NW-094 Through NW-098 Tenant / Control-Plane Standing

Managed deployment/control plane is parked but visible, not forgotten. Current
product route remains one Organization mapped to one managed single-tenant
deployment with one internal/default Workspace.

Tenant-aware auth, workspace-scoped config, tenant sync context, local
partition keys, storage backfills, pooled `tenant_id` predicates, UI tenant
choice, isolation harnesses, and envelope changes remain unselected until
NW-094 through NW-098 are selected.

## Not Selected Now

| Not selected by NW-127 | Trigger to select later | Required wording now |
|---|---|---|
| PC3 implementation | After NW-128 accepts the bounded snapshot behavior and confirms implementation prerequisites. | Implementation successor is separate. |
| NW-044 broad reporting/import/export/aggregate | Product asks for exports, imports, report warehouse, analytics storage, broad report APIs, report catalog, or interoperability reporting. | PC3 is one scoped snapshot, not reporting infrastructure. |
| Conflict queue/list/multi-item review | Product asks for queues, lists, filters, triage, or multi-item attention ergonomics. | PC3 may show issue treatment but does not create a queue. |
| NW-045 conflict automation/batch/resolver reassignment | Product requires batch decisions, pending-match queues, auto-resolution, or resolver reassignment. | Current review semantics remain manual and exact-resolver. |
| NW-073 pattern registry/projection follow-through | PC3 boundary or implementation relies on pattern traversal/reporting, inventory expansion, projection change, or pattern API work. | Not selected unless actually needed. |
| NW-021 S06/entity lifecycle | Product selects maintained known set, discovered-unit lifecycle, deactivation, merge/split UX, or registry stewardship. | PC3 reports current work standing, not entity lifecycle. |
| NW-053 subject/query/custom scope | Snapshot needs access not representable by accepted assignment axes. | Do not add hidden sync scope or query-as-config authority. |
| NW-054 retention/security | Product asks for local expiry, encryption, redaction, erasure, no-local-retention, offboarding, or sensitivity promises. | Snapshot is not a retention/security claim. |
| NW-093 real-production approval | Real users/data/provider/region/jurisdiction/support/compliance/security/go-no-go appears. | Synthetic planning only. |
| NW-094 through NW-098 tenant/control-plane | Multi-customer control plane, tenant selection, tenant-aware runtime, pooled storage, or isolation-test pressure appears. | Managed-isolation remains current route. |
| PC2 live browser proof | Lab access restored enough to inspect R12 first under NW-126. | PC3 does not close PC2 lab debt. |

## Implementation Successor Readiness

As of accepted NW-128, one implementation successor is ready:

```text
NW-129 - Implement PC3 scoped operational report snapshot
```

NW-129 must build one read-only, server-rendered
`/web-admin/operational/report` snapshot from the accepted NW-128 platform
boundary. It must stay out of broad reporting/export/import/warehouse/analytics,
conflict queues, batch/automation/reassignment, pattern expansion, S06
lifecycle, new scope mechanisms, retention/security promises,
tenant/control-plane behavior, real users/data, production approval,
contracts/schemas/sync changes, BAR, CDL, and validation-policy changes.

## Owner Decisions

- Confirm the PC3 label `Scoped Operational Report Snapshot` is the desired
  candidate boundary, not a dashboard or reporting suite.
- Choose the first synthetic example for PC3 proof: S26 operational reporting,
  S22 coordinated campaign, or S27 logistics standing.
- Accepted NW-128 decision: the first snapshot avoids current-period, cadence,
  deadline, overdue, and completion language until a later reporting route.
- Accepted NW-128 decision: unresolved issue treatment uses clean counts,
  excluded-unresolved counts, unresolved issue counts, and at most a limited
  link to accepted one-item attention context.
- Confirm no real users/data are involved; otherwise stop and select NW-093.

## Stop Conditions

Stop and route before implementation if the candidate:

- changes product behavior outside this handoff or an accepted product spec;
- changes architecture authority, contracts, schemas, envelope fields, sync
  protocol, validation/CI policy, or production approval;
- accepts broad reporting/import/export/aggregate behavior;
- creates a reporting warehouse, broad report API, report catalog, or analytics
  subsystem;
- creates conflict queue/list/multi-item review, batch behavior, resolver
  reassignment, or auto-resolution;
- relies on pattern registry/projection behavior as normative without NW-073;
- promotes S06/entity lifecycle;
- changes sync/access scope or introduces a new scope mechanism;
- adds tenant-aware runtime behavior or managed control-plane behavior;
- claims real-production readiness without NW-093;
- mutates BAR, CDL, or the gap register.

## Future-PC Closure Checklist

- Accepted product/platform surfaces linked: PC3 has this PM handoff plus the
  NW-128 platform boundary; an accepted product behavior spec remains future
  work if the owner wants one or the snapshot expands.
- Handoff review path: NW-127 PR review/merge is the acceptance gate.
- Current standing table matches accepted status/backlog rows.
- Scenario map covers S26, S02, S21, S00, S19, S23, S22/S27, and deferred S06.
- Candidate routes are candidates only.
- Exactly one successor route is selected: NW-129.
- Product-level DoD points to the detailed validation owner.
- Owner decisions are explicit.
- Stop conditions are present.
- Status/backlog updated for NW-127 acceptance.
