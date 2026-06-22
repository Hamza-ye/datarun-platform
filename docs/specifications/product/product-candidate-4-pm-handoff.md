# Product Candidate 4 PM Handoff

Status: active PM handoff surface
Document type: product_handoff
Owner: product steward
Source: NW-133; NW-132 post-PC3 progress health; accepted PC1, PC2, and PC3
PM handoffs; scenario baseline pressure map; viability closure review
Authority: derived planning surface only; does not add product behavior,
runtime implementation, production approval, architecture authority,
validation policy, contracts, schemas, BAR, CDL, or gap-register standing
Last reviewed: 2026-06-22
Supersedes: none
Related: `product-candidate-1-pm-handoff.md`;
`product-candidate-2-pm-handoff.md`;
`product-candidate-3-pm-handoff.md`;
`../../agent-working-surface/platform-next-work-backlog.md`;
`../../agent-working-surface/prompts/NW-134-specify-pc4-operational-responsibility-handoff-boundary.md`

## Purpose

This handoff selects exactly one Product Candidate 4 boundary and gives a PM
planning surface for sequencing it professionally. It does not implement PC4,
accept product behavior, approve production, change validation policy, change
architecture authority, or mutate the gap register.

The selected boundary is:

```text
Product Candidate 4 - Operational Responsibility Handoff
```

## Product Goal

Help one Organization continue operational work when responsibility changes:
an assignment coordinator can move or cover responsibility, the successor
worker or reviewer can understand the current assigned work and enough prior
context to continue, and stale offline work remains traceable without turning
the candidate into retention/security policy, entity lifecycle, broad
reporting, tenant-aware runtime, or conflict automation.

## Why This Boundary Wins

| Candidate front | Standing | Why not selected as PC4 |
|---|---|---|
| Setup/admin polish | Useful but incremental. | PC1 already proves setup/config as a first vertical, and no PC1-PC3 proof recorded setup friction that should preempt a new candidate. Richer setup editing remains a bounded polish route only when concrete friction appears. |
| Assignment/admin operations polish | Selected only in narrowed product form. | PC4 is not generic assignment UI polish; it selects the user problem created when responsibility changes and work must continue. |
| Mobile field workflow polish | Useful but incremental. | PC1 already proves login, get-work, capture, correction, and sync. Mobile polish should follow observed proof gaps, not define PC4 by itself. |
| Live/manual proof or demo follow-up | Not selected. | PC1-PC3 proof routes are parked as synthetic-demo-ready, and PC2 live-lab proof remains externally blocked under NW-126. |
| Reporting/import/export/aggregate expansion beyond PC3 | Not selected. | PC3 deliberately stayed to one scoped snapshot. Broader reporting, import/export, warehouse, report APIs, catalog, drilldown, cadence, completion, or completeness semantics still route through NW-044 or a later bounded reporting spec. |
| Conflict queue/list/multi-item ergonomics beyond PC2 | Not selected. | PC2 stays one work-linked attention item. Queue/list/multi-item ergonomics require a selected GAP-CONFLICT-01 successor before implementation. |
| Conflict automation, batch workflow, resolver reassignment, or auto-resolution | Not selected. | These remain NW-045 / BAR-102 / BAR-103 future-decision work. PC4 may preserve unresolved handoff attention but does not automate or batch it. |
| Pattern registry/projection follow-through | Not selected. | S27 and S22 provide useful handoff examples, but PC4 does not select pattern traversal, pattern inventory expansion, projection changes, or pattern API/product work. NW-073 remains trigger-based. |
| S06/entity lifecycle | Not selected. | PC4 is about continuing assigned operational work, not maintaining a known-set registry, discovered-unit lifecycle, deactivation, candidate/duplicate stewardship, or merge/split UX. NW-021 remains trigger-based. |
| S22/S27 coordinated campaign, logistics, transfer, and composite product fronts | Selected in narrowed form. | PC4 uses their handoff/continuity pressure without selecting supply-chain productization, custody-specific scope, pattern expansion, or campaign completion semantics. |
| S24/S25 retention, offboarding, worker transfer, or handoff-policy pressure | Selected only for handoff continuity. | S25 is the strongest evidence for this candidate, but PC4 excludes local purge, encryption, redaction, erasure, no-local-retention, and exit/offboarding policy promises. Those remain NW-054. |
| S02/S09 recurring or campaign standing pressure | Not selected. | Cadence, deadlines, current-period obligations, completion, and recurring reporting remain reporting/trigger work, not PC4. |
| Tenant/control-plane | Not selected. | Managed-isolation remains the current product lane. NW-094 through NW-098 stay trigger-based. |
| Engineering quality, security, reliability, or operations routes | Not selected as product candidates. | They remain gates or prerequisites when their trigger appears, especially NW-093 before real users/data. |

PC4 wins because it is the smallest coherent next user outcome after PC1's
basic loop, PC2's one-item attention review, and PC3's scoped snapshot: work
can now be configured, assigned, captured, reviewed, and summarized, but the
product story still lacks a bounded continuity path when responsibility moves
from one actor to another.

## Target Deployment / Customer Archetype

- Customer or organization shape: one customer-facing Organization.
- Deployment lane: one managed single-tenant Datarun deployment with one
  internal/default Workspace.
- Real-production standing: not approved; real users/data remain behind
  NW-093.
- PC1, PC2, and PC3 standing: parked as `synthetic-demo-ready, not
  real-production-ready`.
- PC2 lab standing: live PC2 browser proof remains blocked by NW-126 until lab
  hostname or fixed-IP SSH access is restored and R12 is inspected first.
- Internal vocabulary that must not leak into product language: tenant,
  workspace, sync watermark, subject-history cursor, actor partition,
  projection repair, resolver internals, event store, flag table, pattern
  engine, and retention mechanism.
- Approval route if real users or data are involved: NW-093 before real use.

## Primary Users / Jobs

| User | Job | Notes |
|---|---|---|
| Assignment coordinator | Move, end, or cover responsibility without losing continuity of current work. | Uses accepted assignment create/end semantics; no new command authority. |
| Successor field worker or reviewer | Start assigned work with enough product-safe context to continue. | Context must come from accepted assignment, sync, subject-history, and projection boundaries. |
| Outgoing or temporarily absent worker | Sync late work without having it disappear or silently count as normal current authority. | Stale work remains traceable and may be flagged under accepted behavior. |
| Supervisor/operator | Understand whether handoff-critical work is ready to continue and where unresolved attention remains. | This is not a queue, report suite, or audit/history reader. |
| Deployment owner | Keep proof, real-use approval, security, reliability, and tenant/control-plane standing separate. | Synthetic/non-sensitive proof remains allowed; real use requires NW-093. |

## In-Scope Journeys

- Coordinate one responsibility handoff or temporary coverage change inside
  accepted assignment axes: geography, subject list, activity, time, and actor.
- Let the successor actor open Datarun after assignment change and understand
  current assigned work plus bounded prior context needed to continue.
- Preserve late offline work from the outgoing actor as traceable evidence,
  with accepted stale-authority or conflict standing when applicable.
- Show product-safe handoff caveats such as incomplete context, unresolved
  attention, or stale/unknown standing without claiming all-device completeness.
- Use synthetic examples from S25, S22, and S27 to validate that the handoff
  language works for worker transfer, coordinated campaign continuation, and
  non-health logistics handoffs.

## Explicit Non-Goals

| Non-goal | Deferred route | Do-not-cross boundary |
|---|---|---|
| Real users, real organizational data, provider/region/jurisdiction/support, compliance/security, continuity, or go/no-go | NW-093 | PC4 planning and synthetic proof do not approve production. |
| PC2 live browser proof or lab reconciliation | NW-126 | Do not touch lab state or close PC2 lab debt from PC4. |
| Local expiry, device decommissioning, sealed recovery, local encryption, token/session retention, no-local-retention, erasure, redaction, sensitivity, or offboarding promises | NW-054 | Assignment handoff is not retention/security policy. |
| New subject/query/custom scope, cross-activity cohort materialization, or hidden sync/access scope | NW-053 | Preserve accepted assignment-derived access. |
| S06/entity lifecycle, maintained known-set registry, discovered-unit lifecycle, deactivation, candidate/duplicate stewardship, or merge/split UX | NW-021 | Continuing work about subjects is not accepting entity lifecycle. |
| Combined entity lifecycle, trigger execution, reporting, analytics, or other broad future-surface expansion | NW-036 | Do not bundle multiple deferred surfaces into PC4. |
| Broad reporting, import/export, warehouse, analytics, broad report APIs, report catalog, cadence, completion, or drilldown | NW-044 | Handoff context is not a reporting product. |
| Conflict queue/list/multi-item ergonomics | GAP-CONFLICT-01 successor route | PC4 may surface unresolved attention but does not create a review queue. |
| Conflict automation, batch workflow, resolver reassignment, or auto-resolution | NW-045 | Preserve manual/exact-resolver semantics. |
| Pattern traversal/reporting, inventory expansion, workflow projection change, or pattern API/product work | NW-073 | Use S22/S27 as examples only unless a later route selects pattern work. |
| Managed control plane, tenant-aware runtime/storage/sync/config/auth, pooled storage, tenant isolation harness, or UI tenant choice | NW-094 through NW-098 | Keep one-Organization managed-isolation vocabulary only. |
| Envelope fields/types, stored event meaning, contracts, schemas, sync protocol, migrations, BAR, CDL, gap-register standing, validation policy, or CI | Gap playbook / owning route | Stop before architecture or contract changes. |

## Current Standing

| Product slice | Current standing | Accepted NW/source | Remaining route | PM interpretation |
|---|---|---|---|---|
| PC1 operational capture loop | Synthetic-demo-ready, not real-production-ready. | NW-119; PC1 PM handoff | Real use still routes through NW-093. | PC4 can build on setup, assignment, mobile capture/correction/sync, and minimal operational visibility without repeating PC1. |
| PC2 single attention review | Synthetic-demo-ready, not real-production-ready; live browser/lab proof blocked externally. | NW-122 through NW-126; PC2 PM handoff | NW-126 remains blocked PC2 lab debt. | PC4 can preserve unresolved attention standing but must not create queue/list/batch review. |
| PC3 scoped operational snapshot | Synthetic-demo-ready, not real-production-ready; no proof friction recorded. | NW-129 through NW-131; PC3 PM handoff | Future PC3 work needs a new bounded route. | PC4 can reuse the idea of scoped standing, but not broaden into reporting. |
| Assignment create/end workflow | Accepted for assignment administration under command capability and containment. | NW-090; NW-069 | No generic assignment policy editor or new command authority. | PC4 should use accepted responsibility-change semantics instead of inventing new authority. |
| Shared-device session and actor-local state | Accepted current behavior. | NW-071; NW-101 | Retention/security beyond current behavior routes through NW-054. | Handoff can rely on current actor partitioning vocabulary but cannot promise exit purge or encryption. |
| Subject-history backfill | Accepted as a separate authorized repair surface with independent cursor. | Phase 4 / status; gap playbook | Handoff package contents still need specification. | Successor context may need subject-history-derived repair, but normal sync must not become broad audit pull. |
| S25 worker transfer/exit pressure | Scenario pressure is accepted as problem-space evidence. | Scenario README; scenario pressure map | Retention/exit promises route through NW-054. | This is the primary PC4 user pressure when narrowed to continuity. |
| S22/S27 campaign/logistics handoff pressure | Accepted runtime probes exist for S22 and S27-style composition. | NW-042/S22; NW-030/S27; pressure map | Pattern, custom scope, lifecycle, reporting, and automation remain separate. | Use as synthetic examples, not as domain-specific product expansion. |
| GAP-SYNC-01 handoff contents | Open platform-spec detail gap. | Gap routing playbook | NW-134 selected as the prerequisite boundary spec. | PC4 implementation must wait for a bounded handoff boundary before delivery. |
| Real production | Blocked. | NW-093 | Select only on real-use trigger. | PC4 remains synthetic planning until real-use approval is selected. |
| Tenant/control-plane | Deferred/trigger-based. | NW-083; NW-094 through NW-098 | Select only on managed control-plane or tenant-aware runtime trigger. | PC4 stays in the managed-isolation lane. |

## Scenario-To-Slice Map

| Scenario pressure | User value | Product-candidate journey | Current support | Candidate NW route | Next product decision | Do-not-cross boundary |
|---|---|---|---|---|---|---|
| S25 worker onboarding, transfer, leave, and exit | Responsibility transfers preserve continuity. | Successor actor receives current assigned work and bounded prior context. | Assignment create/end, actor-scoped sync, subject-history backfill, and stale-authority evidence exist. | NW-134 handoff boundary specification. | What context is enough for a successor without becoming retention/security policy? | No local purge, encryption, erasure, no-local-retention, or exit policy claims. |
| S22 coordinated campaign reassignment | Work started by one operator may be continued by another. | Assignment handoff supports campaign continuation where the unit set may shift. | S22 runtime evidence exists; assignment-derived access is accepted. | NW-134 examples and boundary. | Which campaign-continuity context is product-safe? | No S06 discovered-unit lifecycle, completion semantics, trigger execution, or custom campaign scope. |
| S27 logistics distribution across handoffs | Transfer responsibility and discrepancies need continuity across actors. | Use non-health handoff example to validate domain-neutral language. | S27 runtime probe proves non-health transfer/discrepancy evidence with manual review. | NW-134 examples only unless pattern work is later selected. | Can the handoff boundary explain chain-of-responsibility continuity without productizing logistics? | No custody-specific new scope, pattern API, auto-resolution, or supply-chain product bundle. |
| S19 offline capture and sync | Late offline work must not disappear after responsibility changes. | Preserve stale work and flag/caveat current authority. | Accepted stale-authority runtime evidence. | NW-134 stale/offline handoff rules. | What does the successor see when prior work arrives late? | No sync protocol change, historical pull conversion, or watermark rewrite. |
| S03 designated responsibility | The right person is accountable for current work. | Coordinator changes assignment and successor acts under current responsibility. | Accepted assignment scope and administration spec. | NW-134 uses accepted authority. | How should product copy explain responsibility without leaking implementation terms? | No new command capability, generic admin authority, or assignment role drift. |
| S26 operational reporting and aggregate oversight | Supervisor wants current standing during handoff. | Handoff may show narrow standing/caveats. | PC3 snapshot exists as bounded evidence. | Future only if handoff needs snapshot integration. | Keep handoff context distinct from reporting. | No report API, export/import, drilldown, cadence, completion, or all-clear claim. |
| S24 long-running data lifecycle | Organizations ask what data remains over time. | Out of scope except as a stop trigger. | Current local-state behavior exists; broader retention is future-decision. | NW-054 if selected. | Does a proof target require a retention/offboarding promise? | Do not answer retention/security inside PC4. |
| S06 entity lifecycle | Known-set continuity may be requested later. | Out of scope. | Future-decision only. | NW-021. | Keep deferred unless owner selects lifecycle. | No maintained registry, deactivation, candidate/duplicate, merge/split UX. |

## Candidate NW Decomposition Routes

These are candidate routes only unless a later NW marks the route accepted.
Promote at most one bounded successor at a time.

| Candidate route | Suggested priority | User value / why now | Input sources | Output expected | Acceptance evidence | Stop condition |
|---|---|---|---|---|---|---|
| NW-134 - Specify PC4 operational responsibility handoff boundary | Selected P1 successor | PC4 touches handoff contents, subject-history repair, actor partitions, sync context, stale authority, and product wording; implementation needs an accepted boundary first. | This handoff; S25/S22/S27/S19 evidence; accepted assignment, shared-device, conflict, and PC3 specs; validation matrix; gap playbook. | Platform boundary under `docs/specifications/platform/` defining handoff scope, context contents, caveats, and implementation readiness. | Docs-only validation, platform index check, status/backlog trace, no runtime behavior changed. | Stop on new scope mechanisms, sync protocol changes, retention/security promises, entity lifecycle, broad reporting, pattern changes, tenant/control-plane, real users/data, or architecture authority changes. |
| Implement PC4 operational responsibility handoff | Future after NW-134 | Gives coordinator/successor/supervisor the selected handoff experience. | Accepted NW-134 boundary; accepted web-admin/mobile/session/sync/assignment surfaces. | Bounded implementation prompt and runtime tests. | Focused server/mobile/web-admin tests plus required full gates from validation matrix. | Do not start before NW-134 acceptance; stop on contract/schema/sync/tenant/retention/reporting drift. |
| PC4 synthetic proof packet | Future after implementation | Lets PM/owner validate the handoff journey with synthetic evidence. | PC4 handoff; accepted implementation evidence. | Product-validation artifact or proof route. | Docs-only proof or selected live synthetic proof. | Stop on real users/data, lab mutation, or production approval. |
| Handoff vocabulary/example packet | Candidate | Helps explain S25/S22/S27 continuity in product-safe language. | Operational UX companion; scenarios; PC4 handoff. | Product examples or copy guidance. | Product review notes. | Do not turn examples into contracts, scopes, or domain-specific platform concepts. |
| Retention/offboarding boundary | Future decision | Needed only if owner asks what former workers retain or what must be purged. | S24/S25; shared-device/local-state spec; NW-054. | NW-054 or bounded security/retention route. | Accepted policy/spec before promises. | Do not fold into PC4 continuity. |
| Subject/query/custom scope boundary | Future decision | Needed only if handoff cannot be represented by accepted assignment axes. | S22/S25/S27; NW-053. | Scope decision route. | Accepted decision before implementation. | Do not add hidden sync scope or query-as-config authority. |
| Broad reporting/import/export boundary | Future decision | Needed only if handoff asks for dashboards, completion, drilldown, exports, or report APIs. | S26; NW-044. | Reporting/import/export boundary route. | Accepted decision before implementation. | Do not fold broad reporting into handoff. |
| Pattern registry/projection follow-through | Conditional future | Needed only if PC4 depends on pattern traversal/reporting, inventory expansion, projection change, or pattern API work. | S22/S27; NW-073. | Durable pattern behavior extraction. | Accepted NW-073 before dependent work. | Do not select by label alone. |

## Selected Successor Route

Exactly one successor route is selected:

```text
NW-134 - Specify PC4 operational responsibility handoff boundary
```

NW-134 must specify the prerequisite product/platform boundary before any PC4
implementation. It should define the handoff scope, successor-visible context,
late offline work treatment, unresolved attention/caveat wording, no-leakage
expectations, and implementation readiness without accepting retention/security
policy, new scope, entity lifecycle, reporting expansion, pattern changes,
tenant/control-plane behavior, real users/data, contracts, schemas, sync
protocol changes, BAR, CDL, or gap-register changes.

## Product-Level Definition Of Done

| Journey | Done when user can | Evidence category | Required guardrail | Detailed validation owner |
|---|---|---|---|---|
| Coordinate handoff | Move or cover responsibility using accepted assignment semantics. | Product walkthrough plus focused tests after implementation. | No new command authority, generic admin bypass, or online policy editor. | Agent validation matrix. |
| Start as successor | Sign in or resume and understand current assigned work plus bounded prior context needed to continue. | Focused server/mobile/web-admin evidence after implementation. | No hidden sync scope, broad audit/history, or retention promise. | Agent validation matrix. |
| Preserve late work | See late outgoing-worker work as traceable and caveated when authority changed. | Stale-authority/conflict evidence after implementation. | No event mutation, sync watermark rewrite, or silent authority approval. | Agent validation matrix. |
| Understand caveats | See incomplete/stale/unresolved handoff standing in product-safe terms. | Product review and rendering tests after implementation. | No completion/all-clear/reporting claims. | Agent validation matrix. |
| Stay bounded | Handoff avoids retention/offboarding policy, entity lifecycle, reporting, pattern expansion, tenant/control-plane, and real-production claims. | Product/reviewer inspection and route checks. | Use the owning NW routes when a trigger appears. | Agent validation matrix. |

## Validation Matrix Link

- Detailed validation matrix:
  [agent validation matrix](../../agent-working-surface/validation-matrix.md).
- Known red checks: `flutter analyze` remains known-red and non-blocking until
  fixed or baselined.
- Checks that belong to future validation/CI reset: analyzer cleanup or
  baseline, server log-volume reduction, mobile fake/harness cleanup, and
  shared fixture/contract parity improvements only when selected or triggered.

For this NW-133 handoff itself, docs-only validation is sufficient:

```bash
git diff --check
rg "NW-133" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/specifications/product/product-candidate-4-pm-handoff.md
rg "Product Candidate 4 PM Handoff" docs/specifications/product/README.md
test -f docs/agent-working-surface/prompts/NW-134-specify-pc4-operational-responsibility-handoff-boundary.md
rg "NW-134" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
```

Runtime tests are skipped because the handoff changes no runtime code, tests,
contracts, schemas, CI behavior, validation policy, product behavior
acceptance, BAR, CDL, or gap-register standing.

## Security / Secure SDLC Gates

- Real users/data require NW-093 first.
- Product copy must not imply IdP group/claim/JWT `actor_id`, generic admin,
  UI-selected actor, or tenant/workspace selection authority.
- Handoff context must not imply local purge, encryption, redaction, erasure,
  no-local-retention, or sensitivity-specific behavior.
- Broad audit/history, emergency write, special read/write access, or
  cross-activity access remains outside PC4 unless a successor
  product/security decision selects it.
- Secure SDLC review is a real-use gate, not a PC4-selection blocker.

## Reliability / Operations Gates

- Synthetic reference deployment evidence remains current baseline only.
- PC4 demos/proofs may use synthetic/non-sensitive evidence.
- PC2 live-lab proof remains blocked under NW-126 and must not be claimed by
  PC4 selection.
- Real-use operations readiness, support, continuity, provider/region, and
  go/no-go remain NW-093-gated.
- No operations runbook, deployment policy, backup, monitoring, incident
  response, or support standing changes in NW-133.

## Architecture / Platform Prerequisites

NW-134 is the required PC4 prerequisite before implementation. It must define
the bounded product/platform behavior for:

- handoff scope and accepted authority inputs;
- successor-visible current work and prior context;
- late offline work treatment after responsibility changes;
- subject-history, sync, projection, and actor-local state boundaries;
- unresolved attention, stale, incomplete, or unknown caveats;
- no-leakage and no-broad-audit/history expectations;
- user-facing wording guardrails;
- implementation readiness and validation evidence.

NW-134 must stop and route instead of specifying PC4 if it requires new
envelope fields/types, stored event meaning changes, sync/access scope changes,
new subject/query/custom scope mechanisms, durable workflow-state authority,
reporting warehouse or export/import contracts, broad audit/history access,
pattern traversal/projection/API changes that trigger NW-073, retention/security
promises that trigger NW-054, entity lifecycle that triggers NW-021,
combined lifecycle/trigger/reporting/analytics expansion that triggers NW-036,
tenant-aware runtime behavior, or real production approval.

## NW-093 Production Approval Standing

NW-093 remains blocked. Select it only when concrete real users, real
organizational data, provider, region, jurisdiction, support,
compliance/security, continuity, or go/no-go pressure appears.

PC4 planning and synthetic proof do not approve production.

## NW-126 PC2 Live-Lab Proof Standing

NW-126 remains blocked until lab hostname or fixed-IP SSH access is restored
enough to inspect R12 before touching retained PC2 state.

PC4 selection does not close PC2 live-lab proof debt, does not run a live
browser proof, and does not authorize lab mutation.

## NW-044 Reporting / Import / Export Standing

NW-044 remains the route for broad reporting, structured export/import,
warehouse, analytics storage, broad report APIs, report catalog, cadence,
completion, completeness, drilldown, saved views, or interoperability reporting.

PC4 handoff context must stay narrower than reporting: it may explain what a
successor needs to continue work, but it must not create a reporting product.

## NW-073 Pattern Registry / Projection Standing

NW-073 remains unselected. Select it only if PC4 or another future route
depends on pattern traversal/reporting, inventory expansion, projection change,
pattern API/product work, or accepted pattern behavior as normative.

PC4 may use S22/S27 as examples without selecting pattern work.

## NW-094 Through NW-098 Tenant / Control-Plane Standing

Managed deployment/control plane is parked but visible, not forgotten. Current
product route remains one Organization mapped to one managed single-tenant
deployment with one internal/default Workspace.

Tenant-aware auth, workspace-scoped config, tenant sync context, local
partition keys, storage backfills, pooled `tenant_id` predicates, UI tenant
choice, isolation harnesses, and envelope changes remain unselected until
NW-094 through NW-098 are selected.

## Not Selected Now

| Not selected by NW-133 | Trigger to select later | Required wording now |
|---|---|---|
| PC4 implementation | After NW-134 accepts the bounded handoff behavior and confirms implementation prerequisites. | Implementation successor is separate. |
| NW-093 real-production approval | Real users/data/provider/region/jurisdiction/support/compliance/security/go-no-go appears. | Synthetic planning only. |
| NW-126 PC2 live-lab proof | Lab access restored enough to inspect R12 first under NW-126. | PC4 does not close PC2 lab debt. |
| NW-044 broad reporting/import/export/aggregate | Product asks for exports, imports, warehouse, broad report APIs, report catalog, analytics, interoperability, drilldown, cadence, completion, or completeness. | PC4 is handoff continuity, not reporting. |
| Conflict queue/list/multi-item ergonomics | Product asks for queues, lists, filters, triage, or multi-item attention handling. | PC4 may show caveats but does not create a queue. |
| NW-045 conflict automation/batch/resolver reassignment | Product requires batch decisions, pending-match queues, auto-resolution, resolver reassignment, or conflict automation. | Current review semantics remain manual and exact-resolver. |
| NW-073 pattern registry/projection follow-through | PC4 boundary or implementation relies on pattern traversal/reporting, inventory expansion, projection change, or pattern API work. | Not selected unless actually needed. |
| NW-021 S06/entity lifecycle | Product selects maintained known set, discovered-unit lifecycle, deactivation, merge/split UX, or registry stewardship. | PC4 continues work; it does not maintain entity lifecycle. |
| NW-036 broader future-surface expansion | Product selects a combined lifecycle, trigger execution, reporting, analytics, or other broad deferred package. | PC4 is one handoff continuity candidate, not a bundle. |
| NW-053 subject/query/custom scope | Handoff needs access not representable by accepted assignment axes. | Do not add hidden sync scope or query-as-config authority. |
| NW-054 retention/security/offboarding | Product asks for local expiry, encryption, redaction, erasure, no-local-retention, offboarding, sensitivity, or former-worker retained-data promises. | PC4 is not retention/security policy. |
| NW-094 through NW-098 tenant/control-plane | Multi-customer control plane, tenant selection, tenant-aware runtime, pooled storage, or isolation-test pressure appears. | Managed-isolation remains current route. |
| Setup/admin polish | Concrete setup proof friction appears. | Keep config-package authority fixed. |
| Mobile field workflow polish | Concrete mobile handoff or field-loop friction appears after boundary selection. | Keep sync, event, authority, and retention semantics fixed. |

## Implementation Successor Readiness

No implementation successor is selected by this handoff.

NW-134 must land the handoff boundary first. A bounded implementation successor
can be prepared only after NW-134 confirms that PC4 can be delivered without
new scope mechanisms, sync protocol changes, retention/security promises,
entity lifecycle, broad reporting, pattern projection/API changes,
tenant/control-plane behavior, real users/data, production approval, contracts,
schemas, BAR, CDL, or gap-register changes.

## Owner Decisions

- Confirm the PC4 label `Operational Responsibility Handoff` is the desired
  candidate boundary, not generic assignment UI polish or worker offboarding
  policy.
- Choose the first synthetic proof example: S25 worker transfer, S22 campaign
  reassignment, S27 logistics handoff, or a paired S25/S27 example without
  widening the boundary.
- Decide what product-safe handoff words should be used for successor context,
  prior work, late offline work, stale authority, and unresolved attention.
- Confirm that retention/security/offboarding questions remain out of scope
  unless NW-054 is selected.
- Confirm no real users/data are involved; otherwise stop and select NW-093.

## Stop Conditions

Stop and route before implementation if the candidate:

- changes product behavior outside this handoff or an accepted product spec;
- changes architecture authority, contracts, schemas, envelope fields, sync
  protocol, validation/CI policy, or production approval;
- changes sync/access scope or introduces a new scope mechanism;
- converts normal sync or subject-history backfill into broad audit/history
  pull;
- accepts local expiry, encryption, redaction, erasure, no-local-retention,
  offboarding, or sensitivity promises;
- accepts broad reporting/import/export/aggregate behavior;
- creates conflict queue/list/multi-item review, batch behavior, resolver
  reassignment, or auto-resolution;
- relies on pattern registry/projection behavior as normative without NW-073;
- promotes S06/entity lifecycle;
- adds tenant-aware runtime behavior or managed control-plane behavior;
- claims real-production readiness without NW-093;
- mutates BAR, CDL, or the gap register.

## Future-PC Closure Checklist

- Accepted product/platform surfaces linked: PC4 has this PM handoff only; the
  NW-134 platform boundary is selected as the next prerequisite.
- Handoff review path: NW-133 PR review/merge is the acceptance gate.
- Current standing table matches accepted status/backlog rows.
- Scenario map covers S25, S22, S27, S19, S03, S26, and deferred S24/S06.
- Candidate routes are candidates only.
- Exactly one successor route is selected: NW-134.
- Product-level DoD points to the detailed validation owner.
- Owner decisions are explicit.
- Stop conditions are present.
- Status/backlog updated for NW-133 acceptance.
