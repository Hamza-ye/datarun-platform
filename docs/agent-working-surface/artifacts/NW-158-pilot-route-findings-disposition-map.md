# NW-158 - Pilot Route Findings Disposition Map

Status: accepted control-plane cleanup artifact
Document type: control_reconciliation / disposition_map
Authority: non-authoritative disposition map only; makes NW-157 findings visible
in active routing surfaces but does not accept runtime behavior, amend CDL,
contracts, BAR, gap register, or accepted specs, approve production, or select
slice 6 or slice 7.
Date: 2026-06-25
Owner: product/engineering steward
Inputs: `docs/agent-working-surface/artifacts/NW-157-pilot-behavior-route-causal-analysis.md`;
`docs/status.md`; `docs/agent-working-surface/platform-next-work-backlog.md`;
`docs/agent-working-surface/artifacts/README.md`;
`docs/agent-working-surface/README.md`; `docs/commit-workflow.md`;
`docs/documentation-organization.md`;
`docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`;
`docs/agent-working-surface/skills/software-architect-boundary-reviewer.md`;
`docs/specifications/platform/scoped-configured-work-evidence-inspection-boundary.md`

## Purpose

NW-157 is accepted diagnostic evidence, not a durable active-control surface.
This map turns every NW-157 symptom into an explicit disposition, route, or
trigger so no finding remains discoverable only through the diagnostic artifact.

This cleanup selects no implementation successor. Slice 6 (principal-binding /
local OIDC preflight note) and slice 7 (local/on-prem operational preflight
boundary note) remain candidate documentation/boundary routes only.

## Reusable Behavior-Slice Gate

Before any pilot implementation route starts, the selected NW row or prompt
must name:

1. the platform behavior slice from the accepted NW-155 proof map;
2. the fixture role, including whether any domain vocabulary is fixture-only;
3. the accepted boundary or spec being used;
4. non-goals, especially production approval, real users/data, stock/domain UI,
   broad reporting/import-export, new scope, and runtime cutover;
5. the validation gate that proves the behavior slice without treating tests or
   fixture names as product authority.

Future-decision rows are not forbidden work. They are visible routes that must
be selected only when their trigger condition is active.

## Disposition Map

| Finding | Risk if ignored | Current evidence | Disposition | Priority | Owner lane | Next trigger/action |
|---|---|---|---|---|---|---|
| S1 domain vocabulary promoted into active route framing | Domain fixture words become product direction and route names before platform behavior is selected. | NW-147, NW-149, NW-153, and test names used stock/stocktake vocabulary; NW-155 reframed the pilot into seven platform behavior slices. | Fixed now by the NW-158 behavior-slice gate in status/backlog: name the behavior slice before implementation or domain terms. | P0 | Product/engineering steward | Trigger on any pilot prompt, backlog row, PR title, or status update that leads with domain-first framing instead of the behavior slice. |
| S2 legacy evidence treated as product direction | Sanitized legacy forms could create Datarun product primitives, import scope, or stock-domain semantics by implication. | NW-146 evidence packet and NW-147 first-flow selection fed NW-148 through NW-152 proof work; NW-157 classifies this as framing, not architecture authority. | Fixed now by the behavior-slice gate and fixture-role requirement. Legacy evidence may be compatibility/fixture input only unless a separate route selects product behavior. | P0 | Product/engineering steward | Trigger on any use of legacy fields/forms/accounts/submitted records as product requirements; route import/export through NW-044 and real users/data through NW-093. |
| S3 stocktake became apparent goal instead of behavior fixture | The pilot could optimize for stocktake UI/package completion instead of proving configured capture, sync, scope, and evidence behavior. | NW-149 backlog item 4, NW-153 wording, and `stock_operations` / `stocktake_line/v1` proof oracles; NW-155 maps them to behavior slices. | Fixed now by the gate: every pilot row must state fixture role and behavior slice, and must keep fixture vocabulary out of reusable UI/service/model goals. | P0 | Product/engineering steward | Trigger on any row or PR that treats stocktake, stock operations, warehouse, ledger, item/catalog, or session lifecycle as the goal without selected product/spec authority. |
| S4 "not production" wording causing blocked-by-default behavior | Correct safety language can be misread as "do nothing" instead of "route under explicit trigger." | NW-093 blocked package, NW-150 synthetic assumptions, and status do-not-start lists; NW-157 classifies this as an amplifier. | Visible backlog route: NW-159 candidate control-plane wording cleanup. Current status/backlog state future-decision and not-production routes are not forbidden; they need triggers. | P1 | Product/engineering steward | Select NW-159 if prompts/reviews stall because "not production" or do-not-start wording is read as a total freeze rather than route discipline. |
| S5 future-decision routes read as forbidden | Deferred work may be over-blocked, causing either selection hesitation or unsafe attempts to work around guardrails. | NW-044, NW-045, NW-054, NW-053, NW-021, NW-073, and NW-094 through NW-098 remain future-decision or candidate routes. | Visible route and trigger: existing future-decision rows remain active routes; NW-159 handles wording cleanup if the "forbidden" reading recurs. | P1 | Product/engineering steward | Trigger the named row only when its concrete pressure appears; otherwise state "not forbidden, not selected" in prompts/status. |
| S6 product/report boundary expanded before spec | Report page could become a configured record browser without accepted platform boundary. | NW-153 added report-page details; NW-128 was snapshot-only; NW-155 paused and NW-156 specified the separate evidence detail boundary. | Already closed by NW-156 platform spec and final PR #63 implementation of `/web-admin/operational/evidence`; NW-158 keeps recurrence route visible. | P0 | Reporting/platform verifier | Any report-page record list, browser, export, warehouse, filter, or broad read proposal routes through NW-044 or a new platform/product spec before implementation. |
| S7 PR #63 drift before NW-156 correction | PR implementation could become authority before route correction and status/backlog closure caught up. | NW-155 classified PR #63 as too broad; NW-156 rejected report-page records; final PR #63 implemented the scoped evidence route. | Already closed by NW-155, NW-156, and final PR #63. Visible backlog route: NW-161 candidate PR/status post-merge closure checklist. | P1 | Product/engineering steward | Select NW-161 if a PR changes accepted standing, narrows/broadens a route, or leaves stale successor/status wording after merge. |
| S8 subject_ref / pilot stock-scope subject assumptions | Pilot subject semantics could become a hidden stock/location/scope mechanism. | NW-150 had `subject_binding = null`; NW-151 made the pre-established pilot stock-scope subject explicit; NW-152 proved the mobile smoke path. | Already closed for the synthetic smoke path by NW-151 and NW-152. Deferred beyond that with a visible trigger. | P2 | Product/platform steward | Trigger if the next pilot slice needs new subject semantics, non-stock subject anchors, process-subject emission, or custom subject selection; route through NW-053 or a selected product/platform spec. |
| S9 tests acting like product authority | Fixture tests can become proof of product scope instead of evidence for a selected behavior slice. | `SyntheticStockOperationsFirstFlowIntegrationTest`, `StockOperationsPilotPackageIntegrationTest`, and `stocktake_capture_smoke_test.dart` carry fixture vocabulary and proof oracles. | Visible backlog route: NW-160 candidate test/fixture authority guard. Current gate requires validation evidence to be tied to a named behavior slice. | P1 | Engineering steward | Select NW-160 if a prompt, PR, or review cites tests/fixtures as product authority or uses test fixture names to justify user-visible scope. |
| S10 direct event/projection read path containment | Broad read helpers could bypass scoped read boundaries or leak raw timelines/product identifiers. | NW-154 production-hides `/api/subjects/**`; NW-156 and PR #63 bound the evidence-detail path to current scope and hidden identifiers. | Current path already contained by NW-154 plus NW-156/PR #63. Deferred recurrence with visible trigger. | P2 | Reporting/platform verifier | Trigger on any new web/read surface, event/projection query, raw timeline, subject browser, report API, export, or broad audit/history request; route through NW-044 or a boundary spec if broad. |
| S11 noisy Maven logs / validation-operability risk | Validation output can consume review context or hide the real failure signal. | NW-157 found Maven log volume real but separate from route drift; existing evidence includes full-server validation volume and Surefire warnings. | Intentionally deferred with owner-visible rationale. Visible backlog route: NW-162 candidate validation-operability cleanup. | P2 | Engineering/tooling steward | Select NW-162 only when Maven/server validation output hides failure signal, consumes review context, or materially slows review triage. |
| S12 status/backlog lag after route correction | Stale active-surface wording can preserve superseded routes after a correction has landed. | NW-153 historical wording remained visible after NW-156/PR #63 accepted the separate evidence route; NW-157 flagged the lag. | Fixed now by NW-158 fold-forward status/backlog updates and candidate NW-161 closure checklist for recurrence. | P0 | Product/engineering steward | Trigger on any accepted PR/NW that changes selected route, final surface, or successor standing; update status/backlog/artifact index in the acceptance pass. |

## Active Routes Added Or Reused

| Route | Disposition role | Trigger |
|---|---|---|
| NW-158 | Accepted control-plane cleanup and behavior-slice gate. | Already executed by this artifact and status/backlog fold-forward. |
| NW-159 | Candidate control-plane wording cleanup for "not production" and future-decision wording. | Select when blocked-by-default or forbidden-route wording recurs. |
| NW-160 | Candidate test/fixture authority guard. | Select when tests or fixture names are cited as product authority. |
| NW-161 | Candidate PR/status post-merge closure checklist. | Select when route correction or PR merge leaves stale status/backlog/successor wording. |
| NW-162 | Candidate validation-operability cleanup. | Select when Maven/server validation output hides failure signal or consumes review context. |
| NW-044 | Existing future-decision route for reporting aggregation, broad read APIs, import/export, warehouse/export, and product reporting pressure. | Trigger when broad reporting/import/export/read-model pressure becomes concrete. |
| NW-053 | Existing future-decision route for new subject/query/custom scope mechanisms. | Trigger when a pilot slice needs subject semantics beyond accepted assignment axes and explicit subject anchors. |
| NW-093 | Existing blocked real-production approval package. | Trigger before real users/data, account import, submitted-record import/replay, controlled operational use, or production cutover. |

## Closure Standing

- Fixed now: S1, S2, S3, S12.
- Already closed by existing evidence: S6 by NW-156 plus final PR #63; S7 by
  NW-155, NW-156, and final PR #63; S8 for the smoke path by NW-151 and
  NW-152; S10 for current accepted paths by NW-154 plus NW-156/PR #63.
- Visible backlog route: S4 and S5 through NW-159, S9 through NW-160, S7/S12
  recurrence through NW-161, S11 through NW-162.
- Explicit trigger condition: S8, S10, and S11.
- Future decision route: broad reporting/import-export/read APIs through
  NW-044; new subject/query/custom scope through NW-053; real users/data and
  production approval through NW-093.
- Intentionally deferred: S11 until validation-operability risk actually
  affects review signal; S8 beyond the accepted synthetic smoke path; S10
  beyond current contained paths.

## Non-Goals

NW-158 does not implement runtime code, amend CDL, contracts, BAR, gap register,
accepted specs, validation policy, or CI. It does not approve production, real
users/data, account import, submitted-record import/replay, login/principal
binding implementation, local/on-prem preflight implementation, stock/domain UI,
stock ledger/catalog/warehouse/session lifecycle, broad reporting/import-export,
new scope, tenant/control-plane work, or selection of slice 6 or slice 7.
