# Product Candidate 2 PM Handoff

Status: active PM handoff surface
Document type: product_handoff
Owner: product steward
Source: NW-121; NW-120 delivery readiness and PC2 intake; accepted PC1 product spec and PM handoff; scenario-to-baseline pressure map
Authority: derived planning surface only; does not add product behavior, runtime implementation, production approval, architecture authority, validation policy, contracts, schemas, BAR, CDL, or gap-register standing
Last reviewed: 2026-06-21

## Purpose

This handoff selects exactly one Product Candidate 2 boundary and gives a PM
planning surface for sequencing it professionally. It does not implement PC2,
accept product behavior, approve production, change validation policy, change
architecture authority, or mutate the gap register.

The selected boundary is:

```text
Product Candidate 2 - Single Work-Linked Attention Review
```

## Product Goal

Turn Product Candidate 1's visible `Needs review` cue into a bounded manual
attention-review loop for one Organization: a scoped supervisor/reviewer can
open one unresolved attention item attached to visible work, inspect the source
work and reason, and record a manual resolution decision through accepted
resolver authority. PC2 must keep this as a work-linked review loop, not a
reporting product, conflict operations center, batch workflow, auto-resolution
engine, or broad audit/history reader.

## Why This Boundary Wins

| Candidate front | Standing | Why not selected as PC2 |
|---|---|---|
| Setup/admin polish | Useful but incremental. | PC1 already proves setup/config as a first vertical. Richer editor polish is a later implementation/tooling route, not the next product candidate. |
| Assignment/admin operations polish | Useful but incremental. | PC1 already proves create/end responsibility. Further polish does not create a new coherent product outcome. |
| Mobile field workflow polish | Useful but incremental. | PC1 already proves the field loop. Mobile polish should follow evidence gaps, not define PC2 by itself. |
| Readiness/freshness/attention expansion | Selected in narrowed form. | PC2 selects the actionable attention-review part only; broader freshness/reporting remains unselected. |
| Reporting/import/export/aggregate oversight | Not selected. | S26 evidence exists, but reporting needs NW-044/read-model and freshness/completeness/drilldown boundaries before delivery. |
| Conflict queue or resolution workflow | Not selected as broad conflict operations. | PC2 selects one work-linked manual attention loop only. Batch, queue operations, resolver reassignment, and automation remain out. |
| Pattern registry/projection follow-through | Not selected. | Pattern work needs NW-073 before dependent product/API/projection expansion. |
| S06/entity lifecycle | Not selected. | Entity lifecycle remains NW-021 future-decision work. |
| Tenant/control-plane | Not selected. | Managed deployment/control plane and tenant-aware internals remain NW-094 through NW-098 trigger-based work. |

PC2 wins because it is the smallest coherent next user value after PC1:
PC1 can show that something needs review; PC2 makes that one surfaced issue
actionable while preserving all deferred boundaries.

## Target Deployment / Customer Archetype

- Customer or organization shape: one customer-facing Organization.
- Deployment lane: one managed single-tenant Datarun deployment with one
  internal/default Workspace.
- Real-production standing: not approved; real users/data remain behind
  NW-093.
- Internal vocabulary that must not leak into product language: tenant,
  workspace, resolver internals, event store, conflict shape names, flag table,
  sync watermark, and projection implementation.
- Approval route if real users or data are involved: NW-093 before real use.

## Primary Users / Jobs

| User | Job | Notes |
|---|---|---|
| Supervisor/reviewer | Understand why visible work needs review and record one manual decision. | Authority must come from exact designated-resolver behavior, not job title or UI role. |
| Field user | Continue capture/correction/sync without the review loop mutating their submitted work. | PC2 must preserve append-only source work and server-side evaluation. |
| Assignment coordinator | Keep responsibility boundaries clear while review work remains scoped. | No new assignment command or scope mechanism. |
| Organization operator | See that surfaced issues can be handled in Datarun without becoming a reporting suite. | Product proof remains synthetic/non-sensitive unless NW-093 is selected. |
| Deployment owner | Keep proof, security, reliability, and production approval separate. | Real-use and operations claims remain separately gated. |

## In-Scope Journeys

- Open one unresolved attention cue attached to visible scoped work.
- See product-safe context for the source work: work type, activity, subject
  reference, server-received time, work time, attention category, severity, and
  reason when available.
- See who is allowed to resolve the item in product-safe wording without
  exposing implementation resolver mechanics.
- Record one manual resolution decision only when the session actor is the
  exact designated resolver.
- Return to the scoped operational work context with clear resolved/unresolved
  standing for that item.
- Preserve append-only review evidence and existing source work history.

## Explicit Non-Goals

| Non-goal | Deferred route | Do-not-cross boundary |
|---|---|---|
| Reporting dashboards, imports, exports, warehouses, aggregate analytics, completeness, or drilldown | NW-044 | Do not turn attention review into reporting. |
| Conflict queue operations, batch review, pending-match queues, resolver reassignment, or auto-resolution | NW-045 after NW-072 if needed | Manual one-item review only. |
| New flag categories, resolver authority, or conflict schema changes | NW-072 / architecture route if needed | Preserve exact designated-resolver equality. |
| Pattern traversal/reporting, inventory expansion, or workflow-state tables | NW-073 | Do not add durable workflow-state authority. |
| Entity lifecycle, known-set registry, candidates, deactivation, merge/split UX | NW-021 | Recording about a subject remains distinct from S06 lifecycle. |
| Tenant-aware runtime, pooled storage, workspace-scoped config, tenant sync context, tenant isolation harness | NW-094 through NW-098 | Managed-isolation vocabulary only; no runtime tenant drift. |
| Real users, real organizational data, provider/region/jurisdiction/support, compliance/security, go/no-go | NW-093 | Synthetic planning/proof only until real-use trigger. |
| Retention/security/offboarding promises, local encryption, erasure, redaction, no-local-retention | NW-054 | Do not make retained-data claims inside PC2. |
| Auth/security authority changes or IdP group/claim/JWT `actor_id` authority | Security/platform decision | Principal binding remains explicit `(issuer, subject) -> actor_id`. |

## Current Standing

| Product slice | Current standing | Accepted NW/source | Remaining route | PM interpretation |
|---|---|---|---|---|
| PC1 proof | Accepted as synthetic-demo-ready, not real-production-ready. | NW-119 | None for PC1 proof; real use routes through NW-093. | PC2 can build on PC1 proof without repeating it. |
| Minimal attention cue | Accepted as one generic cue attached to latest visible work. | NW-114 | Not a precedent for more operational read surfaces. | PC2 should make the one cue actionable, not broaden reads by drift. |
| Conflict/flag behavior | Runtime evidence exists, but durable current behavior extraction remains candidate. | NW-029, NW-030, NW-033, NW-072 candidate | Select NW-072 before PC2 implementation. | Product UI must not depend on scattered/current code behavior as normative first. |
| Resolver equality | Accepted current behavior requires exact designated-resolver resolution. | Conflict/runtime evidence; gap playbook | NW-072 should extract current behavior and resolver eligibility limits. | Product copy must avoid generic supervisor/root-admin authority. |
| Reporting freshness | S26 proves constrained inputs; reporting semantics remain open. | NW-033; GAP-PROJECTION-02 | NW-044 before reporting/completeness/drilldown. | PC2 is not a reporting candidate. |
| EventRepository/read-model boundary | NW-114 tolerated one latest-work query only. | NW-114; NW-120 | PC2 prerequisite must route read-model/query boundary. | A second attention read requires deliberate boundary work first. |
| WebAdminOperationalViewService reach-through | Direct `JdbcTemplate` attention query is known code-boundary debt. | NW-120 | Clean up inside the prerequisite/implementation sequence. | Do not extend reach-through as product precedent. |
| Mobile analyzer | Known-red, non-blocking. | NW-109; validation matrix | Future analyzer cleanup/baseline. | Not a PC2 selection blocker. |
| Real production | Blocked. | NW-093 | Select only on real-use trigger. | PC2 planning remains non-production. |
| Tenant/control-plane | Deferred/trigger-based. | NW-083; NW-094 through NW-098 | Select only on managed control-plane or tenant-aware runtime trigger. | PC2 stays in managed-isolation lane. |

## Scenario-to-Slice Map

| Scenario pressure | User value | PC2 journey | Current support | Candidate NW route | Next product decision | Do-not-cross boundary |
|---|---|---|---|---|---|---|
| S21 supervisor review | Supervisor can act on questionable reviewed work. | Open one work-linked attention item and record a manual decision. | Runtime probe proves scoped review, unresolved flag exclusion, and exact resolver re-inclusion. | NW-072 prerequisite, then bounded implementation. | What exact one-item review context is enough for PM proof? | No overdue automation, resolver reassignment, or broad review queue. |
| S27 logistics discrepancy | Non-health discrepancy review can be manual and traceable. | Review one discrepancy-like attention item without health-domain assumptions. | Runtime probe proves transfer discrepancy review and exact resolver handling. | NW-072 prerequisite. | Which synthetic discrepancy example best explains PC2? | No auto-resolution or custody-specific new scope. |
| S26 operational oversight | Supervisors need visible unresolved issues with freshness caveats. | Preserve latest visible work context while making one issue actionable. | Runtime probe proves constrained freshness/flag inputs without warehouse/report API. | NW-044 only if PC2 drifts into reporting. | Keep PC2 as review, not reporting. | No aggregate completeness, drilldown product, export, or warehouse. |
| S00 structured capture | Source work remains trustworthy and append-only. | Review decisions do not mutate source captures. | Runtime probe proves append-only correction/idempotency. | Implementation tests after prerequisite. | How should product copy explain source work vs resolution? | No event mutation or new correction linkage. |
| S19 offline/stale authority | Offline work may sync later and need human attention. | Review one stale/flagged work item after sync. | Runtime probe proves stale work is persisted and flagged. | NW-072 prerequisite. | What stale-work example belongs in PC2 proof? | No mobile authoritative rejection or sync protocol change. |
| S23 setup/config | Activities can define reviewable work without custom development. | PC2 uses existing configured work and platform attention behavior. | Runtime probe proves bounded setup/config delivery. | None unless product asks for new config semantics. | Do configured examples need copy polish? | No scripts, trigger engine, config-package changes, or dynamic queries. |
| S06 entity lifecycle | Registry lifecycle may create attention pressure. | Deferred from PC2. | Future-decision only. | NW-021. | Keep deferred. | No known-set lifecycle or merge/split UX. |

## Candidate NW Decomposition Routes

These are candidate routes only. Promote at most one bounded row at a time.

| Candidate route | Suggested priority | User value / why now | Input sources | Output expected | Acceptance evidence | Stop condition |
|---|---|---|---|---|---|---|
| PC2 prerequisite: extract conflict/flag durable behavior for single attention review | P0 | Productizing review depends on current flag/resolution behavior being durable and bounded. | NW-072 candidate; S21/S27/S26 evidence; gap playbook GAP-CONFLICT rows; NW-120 code-boundary assessment. | Durable current-behavior extraction and implementation guardrails for one-item review. | Docs/spec/platform or routed artifact evidence, status/backlog trace, no runtime behavior unless separately selected. | Stop if resolver authority, flag schema, automation, batch, or architecture changes are required. |
| PC2 prerequisite: operational attention read-model boundary | P0 | PC2 needs a second attention read beyond the NW-114 one-off. | NW-114 boundary note; EventRepository and WebAdminOperationalViewService concerns; NW-120. | Decision whether attention reads live in repository, read model, or bounded query service before UI. | Docs-only boundary evidence or implementation prompt after prerequisite. | Stop before reporting, drilldown, aggregate, export, broad audit, or repository architecture changes. |
| Implement single work-linked attention review | P1 after prerequisites | Makes PC1 `Needs review` cue actionable. | Accepted prerequisite route; web-admin session/command/access standing; S21/S27 examples. | Bounded implementation prompt for one-item review UI/API using existing exact resolver semantics. | Focused web-admin/conflict tests plus full relevant server gate as required by validation matrix. | Stop on batch, auto-resolution, resolver reassignment, new scopes, reporting, or contract/schema changes. |
| PC2 vocabulary and proof example packet | P2 | Helps PM/reviewer validate non-health and supervisor examples before implementation. | S21/S27; operational UX vocabulary companion; PC1/PC2 handoffs. | Synthetic examples and accepted/rejected product terms. | Product review notes or docs-only artifact. | Stop if examples become platform vocabulary or contracts. |
| Reporting boundary follow-up | Future | Needed only if reviewers ask for lists, dashboards, completeness, drilldown, exports, or aggregate views. | S26; NW-044; GAP-PROJECTION rows. | NW-044-style reporting/import/export boundary route. | Accepted boundary before reporting implementation. | Do not fold into PC2 single attention review. |
| Conflict automation/batch follow-up | Future | Needed only if one-item manual review proves insufficient. | NW-045; GAP-CONFLICT-02. | Automation/batch/resolver reassignment decision route. | Accepted decision before implementation. | Do not weaken exact resolver equality or mutate flags directly. |

## Product-Level Definition Of Done

| Journey | Done when user can | Evidence category | Required guardrail | Detailed validation owner |
|---|---|---|---|---|
| Open attention | Open one unresolved attention item attached to visible scoped work. | Product walkthrough plus focused web-admin evidence after implementation. | Scope predicates apply before visibility; no broad read API. | Agent validation matrix. |
| Understand context | See source work, activity, subject reference, received/work time, attention category/severity/reason. | Focused rendering/controller tests after implementation. | Product-safe wording only; no event-store vocabulary. | Agent validation matrix. |
| Resolve manually | Record one manual decision only as exact designated resolver. | Auth/conflict denial and success tests after implementation. | No IdP claim, generic admin, assignment role, or UI-selected actor authority. | Agent validation matrix. |
| Preserve history | Source work remains append-only and resolution is separate evidence. | Existing conflict/event tests plus focused regression after implementation. | No event mutation, flag mutation, schema change, or direct table rewrite. | Agent validation matrix. |
| Stay out of reporting | Review loop does not present dashboards, aggregate completeness, exports, imports, or warehouse semantics. | Product/reviewer inspection and tests for bounded routes after implementation. | Route reporting through NW-044. | Agent validation matrix. |

## Validation Matrix Link

- Detailed validation matrix:
  [agent validation matrix](../../agent-working-surface/validation-matrix.md).
- Known red checks: `flutter analyze` remains known-red and non-blocking until
  fixed or baselined.
- Checks that belong to future validation/CI reset: analyzer cleanup or
  baseline, server log-volume reduction, mobile fake/harness cleanup, and
  shared fixture/contract parity improvements only when selected or triggered.

For this NW-121 handoff itself, docs-only validation is sufficient:

```bash
git diff --check
rg "NW-121" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/specifications/product/product-candidate-2-pm-handoff.md
```

Runtime tests are skipped because the handoff changes no runtime code, tests,
contracts, schemas, CI behavior, validation policy, product behavior
acceptance, BAR, CDL, or gap-register standing.

## Security / Secure SDLC Gates

- Real users/data require NW-093 first.
- Product copy must not imply IdP group/claim/JWT `actor_id`, generic admin,
  assignment role, or browser-selected actor authority.
- Broad audit/history, emergency write, redacted/no-local-retention, or special
  read/write access remains outside PC2 unless a successor product/security
  decision selects it.
- Secure SDLC review is a real-use gate, not a PC2-selection blocker.

## Reliability / Operations Gates

- Synthetic reference deployment evidence remains current baseline only.
- PC2 demos/proofs may use synthetic/non-sensitive evidence.
- Real-use operations readiness, support, continuity, provider/region, and
  go/no-go remain NW-093-gated.
- No operations runbook, deployment policy, backup, monitoring, or incident
  response standing changes in NW-121.

## Architecture / Platform Prerequisites

Before implementing PC2, select the PC2-scoped NW-072 prerequisite to extract
current conflict/flag behavior and define the read-model/query boundary for
work-linked attention review.

The prerequisite must answer:

- what current flag/resolution behavior is normative enough for product copy;
- how exact designated-resolver authority is represented to users;
- whether resolver eligibility fallback is product-safe or needs a successor
  route;
- where attention-read queries belong so WebAdminOperationalViewService does
  not extend direct `JdbcTemplate` reach-through;
- which behavior remains out of scope: batch, automation, resolver
  reassignment, reporting, import/export, and broad audit/history.

## NW-093 Production Approval Standing

NW-093 remains blocked. Select it only when concrete real users, real
organizational data, provider, region, jurisdiction, support,
compliance/security, continuity, or go/no-go pressure appears.

PC2 planning and synthetic proof do not approve production.

## NW-094 Through NW-098 Tenant / Control-Plane Standing

Managed deployment/control plane is parked but visible, not forgotten. Current
product route remains one Organization mapped to one managed single-tenant
deployment with one internal/default Workspace.

Tenant-aware auth, workspace-scoped config, tenant sync context, local
partition keys, storage backfills, pooled `tenant_id` predicates, UI tenant
choice, isolation harnesses, and envelope changes remain unselected until
NW-094 through NW-098 are selected.

## Not Selected Now

| Not selected by NW-121 | Trigger to select later | Required wording now |
|---|---|---|
| PC2 implementation | After NW-072/conflict durable behavior and attention read-model prerequisite is accepted. | Implementation successor is not ready yet. |
| NW-044 reporting/import/export/aggregate | Reviewer asks for dashboards, lists, aggregates, completeness, exports, imports, drilldown, or warehouse semantics. | PC2 is one-item review, not reporting. |
| NW-045 conflict automation/batch/resolver reassignment | Product requires batch decisions, pending-match queues, auto-resolution, or resolver reassignment. | Current PC2 is manual exact-resolver review only. |
| NW-073 pattern registry/projection follow-through | Product needs pattern traversal/reporting, inventory expansion, projection change, or pattern API work. | PC2 consumes existing attention evidence only after prerequisite. |
| NW-021 S06/entity lifecycle | Product selects maintained known set, discovered-unit lifecycle, deactivation, merge/split UX, or registry stewardship. | PC2 reviews work-linked attention, not entity lifecycle. |
| NW-093 real-production approval | Real users/data/provider/region/jurisdiction/support/compliance/security/go-no-go appears. | Synthetic planning only. |
| NW-094 through NW-098 tenant/control-plane | Multi-customer control plane, tenant selection, tenant-aware runtime, pooled storage, or isolation-test pressure appears. | Managed-isolation remains current route. |
| Analyzer cleanup/baseline | Analyzer must become blocking or mobile work needs clean analyzer evidence. | Known-red, non-blocking today. |
| Mobile fake/harness cleanup | Mobile tests expand and fakes obscure evidence. | Not needed before selecting PC2. |
| Shared fixture / contract parity hardening | Selected work changes cross-runtime contracts/projections/sync/config/pattern behavior. | Required only when triggered. |

## Implementation Successor Readiness

No implementation successor is ready now.

The prerequisite route must come first: select the PC2-scoped NW-072
conflict/flag durable-behavior and operational attention read-model boundary
route before implementing any review UI/API. After that prerequisite is
accepted, one bounded implementation successor can be prepared for single
work-linked attention review.

## Owner Decisions

- Confirm PC2 uses the `Single Work-Linked Attention Review` boundary, not a
  queue, dashboard, or reporting product.
- Choose the synthetic proof example: S21 supervisor review, S27 logistics
  discrepancy, or both as examples without widening the product boundary.
- Decide whether the first review action should expose product words such as
  `resolve`, `accept`, `reject`, or `mark reviewed` after NW-072 extracts
  exact current semantics.
- Decide whether resolver eligibility fallback is acceptable for synthetic PC2
  proof or requires a separate successor route before implementation.
- Confirm no real users/data are involved; otherwise stop and select NW-093.

## Stop Conditions

Stop and route before implementation if the candidate:

- changes product behavior outside this handoff or an accepted product spec;
- changes architecture authority, contracts, schemas, envelope fields, sync
  protocol, validation/CI policy, or production approval;
- turns current flag/resolution code behavior into product authority before
  NW-072 extraction;
- claims real-production readiness without NW-093;
- adds tenant-aware runtime behavior or managed control-plane behavior;
- accepts reporting/import/export/aggregate behavior;
- accepts conflict automation, batch behavior, resolver reassignment, or
  auto-resolution;
- promotes S06/entity lifecycle;
- mutates BAR, CDL, or the gap register.

## Future-PC Closure Checklist

- Accepted product spec linked: PC2 has this PM handoff only; an accepted
  product behavior spec remains future work if the owner wants one.
- Handoff reviewed by product steward: pending NW-121 PR review.
- Current standing table matches accepted status/backlog rows.
- Scenario map covers S21, S27, S26, S00, S19, S23, and deferred S06.
- Candidate routes are candidates only.
- Product-level DoD points to the detailed validation owner.
- Owner decisions are explicit.
- Stop conditions are present.
- Status/backlog updated for NW-121 acceptance.
