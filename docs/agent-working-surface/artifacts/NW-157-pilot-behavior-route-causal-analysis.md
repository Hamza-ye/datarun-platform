# NW-157 - Pilot Behavior Route Causal Analysis

Status: accepted diagnostic artifact / planning note
Document type: planning / control_reconciliation / causal_analysis
Authority: non-authoritative diagnostic evidence only; does not accept runtime
behavior, change CDL/contracts/BAR/gap register, approve production, or select
an implementation successor.
Date: 2026-06-25
Owner: product/engineering steward
Inputs: `.review/diagnose.md`; parallel diagnostic drafts
`.review/untracked-user-notes/analysis/NW-ee-pilot-behavior-route-causal-analysis.md`
and
`.review/untracked-user-notes/analysis/NW-xx-pilot-behavior-route-causal-analysis.md`;
repo evidence named by the diagnosis packet; PR #63 final accepted state after
NW-156 correction.

## Authority And Evidence Trace

This artifact follows the diagnosis packet authority/evidence order. The order
is not a license to turn implementation-standing or progress surfaces into
architecture authority.

1. CDL / canonical architecture decision ledger is the architecture authority.
2. Contracts are wire/process authority unless they conflict with the CDL.
3. Accepted durable product/platform specs are evidence of accepted behavior
   below CDL/contracts.
4. Accepted baseline, gap, status, backlog, and routing surfaces are
   implementation-standing evidence. They record accepted standing, blockers,
   and routing state; they do not create or override architecture, product, or
   contract authority.
5. Code and tests are current behavior evidence, not automatic authority. If
   code/tests conflict with CDL or accepted architecture without formal
   decision trace, classify them as implementation evidence plus assumption
   debt.
6. Prompts, artifacts, PR bodies, and historical notes are progress evidence,
   provenance, or audit inputs. They are not architecture authority.

The two parallel diagnostic drafts agree on the core diagnosis: the pilot route
drift is a process/control-plane framing problem around behavior-slice
selection, amplified by domain vocabulary, not a CDL/contract/BAR/gap-register
authority breach. This artifact reconciles those drafts and the repo evidence;
it does not independently accept product behavior.

## 1. Symptom Inventory

| # | Symptom | Evidence | Classification |
|---|---|---|---|
| S1 | Domain vocabulary promoted into active route framing (`stock operations`, `supervisor stock operations view`, `stocktake`) | NW-147 first-flow selection; NW-149 PM handoff; NW-153 prompt/backlog row; server/mobile test names and constants | Process / framing |
| S2 | Legacy sanitized form evidence treated as product direction instead of compatibility/fixture input | NW-146 evidence packet through NW-147 stocktake selection and NW-148 through NW-152 proof chain | Process / framing |
| S3 | Stocktake / stock operations became the apparent goal instead of a behavior-proof fixture | NW-149 ordered backlog item 4; NW-153 goal text; tests asserting `stock_operations` / `stocktake_line/v1` as proof oracle | Process / evidence misuse |
| S4 | "Not production" and blocked-approval wording created blocked-by-default interpretation | NW-093 blocked package; NW-150 `synthetic-assumptions.json`; status do-not-start lists | Process constraint / amplifier |
| S5 | Future-decision routes read as forbidden rather than routable when not triggered | NW-044, NW-045, NW-054, NW-053, NW-021, NW-073, and tenant/control-plane guardrails in status/backlog | Process constraint / amplifier |
| S6 | Product/report boundary expanded before platform spec selection | NW-153 report-page `Configured work details`; NW-128 snapshot-only boundary; NW-155/156 correction | Product gap + process |
| S7 | PR #63 drifted from generic scoped report snapshot into configured detail view before NW-156 corrected it | NW-155 classified PR #63 as too broad; NW-156 rejected report-page record list; final PR #63 uses `/web-admin/operational/evidence` | Recovery issue |
| S8 | `subject_ref` / pilot stock-scope subject assumptions were under-specified until NW-151 | NW-150 package `subject_binding = null`; NW-151 PM/package wording; mobile smoke hardcoded `pilotStockScopeSubjectId` | Inherited debt + product gap |
| S9 | Tests acted like product authority | `SyntheticStockOperationsFirstFlowIntegrationTest`, `StockOperationsPilotPackageIntegrationTest`, `stocktake_capture_smoke_test.dart` carry fixture vocabulary and proof oracles | Process / evidence misuse |
| S10 | Direct event/projection read paths required containment review | `EventRepository.findBySubjectId` via `/api/subjects`; NW-154 production-hides broad helpers; bounded `findScopedVisibleWorkEvent` and report queries remain | Quality/security risk, contained |
| S11 | Noisy Maven logs obscure validation triage | validation-operability notes and observed full-server test volume | Quality / validation-operability, separate |
| S12 | Status/backlog standing lagged the NW-156/PR #63 correction | NW-153 historical wording described report-page details while NW-156/PR #63 accepted a separate evidence route | Recovery / documentation drift |

## 2. Symptom Clusters

### Cluster A - Missing behavior-proof frame

Symptoms: S1, S2, S3, S9.

NW-147 selected a domain-named first flow from sanitized legacy forms before a
stable behavior-slice map existed as the gate for implementation. Downstream
prompts and tests optimized for "stock operations" progress instead of proving
generic configured operational behavior. NW-155 corrected this by mapping the
pilot target to seven behavior slices and treating stock artifacts as fixture
evidence, not product authority.

### Cluster B - Boundary decided during implementation

Symptoms: S6, S7, S12.

NW-128 made `/web-admin/operational/report` a scoped standing snapshot, not a
record browser. NW-153 implemented configured details directly on that report
surface before a product/platform boundary was selected. NW-155 paused and
classified PR #63; NW-156 accepted the separate one-item evidence route; final
PR #63 moved configured detail rendering to `/web-admin/operational/evidence`.

This is implementation ahead of the accepted platform boundary. It is not a CDL
breach.

### Cluster C - Control-plane wording overload

Symptoms: S4, S5.

NW-093 correctly blocks real production. Long do-not-start lists are also
correct as guardrails. Without a positive behavior-slice frame, those guardrails
can push agents into either over-building from tests or over-blocking unselected
routes. This amplified route ambiguity but did not directly cause the report
page drift.

### Cluster D - Pilot mapping debt

Symptom: S8.

The pre-established stock-scope subject is pilot mapping context. It does not
create a stock, warehouse, ledger, catalog, stocktake-session, or new platform
scope mechanism. NW-151/NW-152 closed the smoke-path assumption enough for the
fixture proof.

### Cluster E - Read-path containment

Symptom: S10.

NW-154 contains broad `/api/subjects/**` raw helper reads outside production.
The accepted evidence route uses bounded queries that reapply current scope
before returning payload data. Current containment is enough for the accepted
boundary, but new read surfaces need the same spec/interface alignment.

### Cluster F - Validation operability

Symptom: S11.

Maven log volume is a real validation-operability risk, but neither diagnostic
draft ties it causally to the pilot behavior route drift. It should stay
separate unless future evidence connects it.

## 3. Candidate Causal Mechanisms

| Mechanism | Explains | Limit |
|---|---|---|
| M1: No proactive behavior-slice gate before NW-155 | S1, S2, S3, S9 and the repeated domain-first prompts | Needs M2 to explain report-boundary drift |
| M2: Product/platform boundary selected after implementation started | S6, S7, S12 and PR #63 correction | Does not explain original stock-flow selection |
| M3: Legacy evidence flowed into product naming | S2 and NW-147 stocktake framing | NW-147 still recorded non-goals; downstream prompts lost the frame |
| M4: Tests/fixtures elevated to acceptance authority | S3, S9 | Tests are valid evidence when tied to a selected behavior slice |
| M5: Deferred/future rows misread as total freeze | S4, S5 | Several synthetic proof slices still landed, so this is amplifier not root |
| M6: Missing subject-selection product surface | S8 | Bounded to pilot mapping and closed for smoke path |
| M7: Read API containment weakness | S10 | NW-154 and NW-156/PR #63 final state contain the current paths |
| M8: Maven log noise blocked progress | S11 | Independent validation-operability issue |

Most explanatory mechanism pair: M1 + M2.

## 4. Most Likely Core Issue, With Evidence

The core issue is that the pilot route lacked a mandatory behavior-slice
control point before implementation prompts and PRs. That control point needs
to separate:

- the platform behavior being proved;
- the fixture/evidence used to prove it;
- the accepted product/platform boundary for the surface being touched.

Evidence chain:

1. The vision and viability evidence emphasize "set up, not built", one
   coherent system, and domain-agnostic behavior. The pilot was valid pressure,
   but the route used legacy form portfolio -> stock flow name before a
   behavior-slice checklist became the control point.
2. NW-146 and NW-093 correctly kept real users/data and production approval
   blocked. They did not authorize stock-domain primitives or real production.
3. NW-148 through NW-152 produced valid synthetic proof evidence for configured
   capture, mobile pending work, authenticated sync, and scoped visibility only
   if read as fixture evidence.
4. NW-153 selected a domain-framed implementation and put configured details on
   the report page before the NW-156 boundary existed.
5. NW-155 converted the route back to platform behavior slices. NW-156 accepted
   the evidence-inspection boundary. Final PR #63 aligns with that correction:
   report page link only, detail rendering on `/web-admin/operational/evidence`,
   tests asserting the report does not contain inline `Configured work details`.
6. No evidence shows a CDL, contract, BAR, or gap-register breach. Code/tests
   that reflected the report-page detail approach are implementation evidence
   and assumption debt corrected by NW-156/PR #63, not authority.

What this is not:

- not a confirmed runtime bug;
- not a missing CDL decision;
- not a stock ledger/review workflow/warehouse/catalog route;
- not a reason to amend contracts, BAR, CDL, gap register, or accepted specs;
- not proof that Maven log volume caused the pilot drift.

## 5. Issue Classification

| Issue | Classification | Result |
|---|---|---|
| Behavior-slice frame missing until NW-155 | Process constraint | Primary root cause |
| NW-153 / PR #63 report-page detail drift | Product gap + recovery issue | Corrected by NW-156/final PR #63 |
| Domain vocabulary in route/prompt names | Process constraint | Symptom of missing behavior frame |
| Legacy evidence converted into flow naming | Process constraint | Source of fixture/product confusion |
| Tests used as product direction | Process constraint | Evidence misuse; tests remain current behavior evidence only |
| `subject_ref` pilot stock-scope mapping | Inherited debt / product gap | Closed enough for synthetic smoke, not a platform mechanism |
| Broad subject/event helper reads | Quality/security risk | Contained by NW-154 for current accepted boundary |
| Report inline details vs NW-128 | Recovery issue / assumption debt | Corrected by NW-156/final PR #63 |
| Maven log volume | Quality/security risk | Separate validation-operability route |
| Slices 6 and 7 unproven | Product gap | Expected remaining pilot boundary work |
| Architecture decision | Not required | No formal decision route proven necessary |
| Runtime bug | Not confirmed | No runtime-code fix selected by this diagnosis |

## 6. Recommended Intervention Point

Intervene at row/prompt selection before implementation starts.

Required gate for future pilot work:

1. Name the behavior slice from the NW-155 map in platform terms.
2. Cite the accepted spec/boundary if the slice touches web-admin, reporting,
   evidence, sync, or read APIs.
3. Keep fixture/domain vocabulary in tests, deploy/reference packages, and
   historical artifact titles, not in reusable UI/service/model names or prompt
   goals.
4. State whether the task adds new product/platform behavior or re-proves an
   accepted slice with a fixture.
5. Use a NW-155-style classification before merging any PR that broadens report,
   evidence, sync, read API, review, stock-domain, or production surfaces.

## 7. Options

| Option | Meaning for this diagnosis | Recommendation |
|---|---|---|
| Fix now | Reconcile artifact path/index/status/backlog wording so NW-157 is no longer pending and the old `-merge.md` filename is gone | Appropriate side action for artifact consistency |
| Analyze further | Compare slice 6 vs slice 7 operationally before owner/reviewer selection | Optional only if owner/reviewer cannot select from current evidence |
| Route to decision | Create a formal decision/spec route for a general configured-work browser, reporting warehouse, review workflow, stock ledger, new scope, or production path | Not indicated by this diagnosis |
| Defer | Maven log hardening, broad reporting/import-export, review queues, tenant/control-plane, retention/security, stock ledger | Correct deferral unless separately selected |

Do not amend CDL, contracts, BAR, gap register, accepted specs, runtime code, or
tests in this diagnostic pass.

## 8. Acceptance Criteria For Proving This Diagnosis Was Correct

The diagnosis is validated if the next pilot cycle shows all of the following:

1. No implementation prompt uses a domain-first goal such as `stock operations
   view` without a behavior-slice ID and accepted boundary citation.
2. No report-page expansion adds configured record lists or inline field tables
   without a new accepted platform spec superseding NW-128/NW-156.
3. Fixture changes do not create product-direction backlog rows by themselves;
   they attach as evidence to a selected slice.
4. Owner/reviewer selection names exactly one next behavior slice before any
   implementation starts.
5. Web-admin operational PRs are classifiable as either re-proof of an accepted
   slice or spec-first work before merge.
6. Status/backlog wording reflects NW-156/PR #63 final evidence route and does
   not keep stale report-page detail claims as active standing.

The diagnosis would be weakened if the next stall is caused by a pure
implementation defect without prompt, route, boundary, or evidence-authority
ambiguity.

## 9. Recommended Next Route

Explicit result: **no implementation successor is selected by this artifact**.

Owner/reviewer decision is required before the next NW row. The available
candidate behavior slices surfaced by the evidence are:

- slice 6: principal-binding / local OIDC preflight note;
- slice 7: local/on-prem operational preflight boundary note.

Both candidates are planning/boundary work only. Neither requires NW-093 to be
unblocked. This artifact does not choose between them and does not authorize
login/principal-binding implementation, local/on-prem preflight
implementation, import/export, reporting warehouse, review workflow,
tenant/control-plane, stock ledger, stock-domain UI, real users/data, or
production cutover.

## Appendix A - Slice Standing

| Slice | Standing | Evidence / gap |
|---|---|---|
| 1. Configured structured capture | Accepted synthetic proof | NW-148, NW-150, NW-152 |
| 2. Offline local pending work | Accepted synthetic proof | NW-059/NW-060, NW-152 |
| 3. Authenticated sync | Accepted synthetic proof | NW-037 through NW-040/NW-070, NW-148, NW-152 |
| 4. Assignment-scoped visibility | Accepted synthetic proof | NW-129, NW-148, NW-151, NW-154 |
| 5. Scoped evidence inspection | Accepted after correction | NW-156 spec and final PR #63 evidence route |
| 6. Explicit principal binding / login | Unproven for pilot operation | Synthetic bindings only; needs owner/reviewer-selected boundary/preflight note |
| 7. Local/on-prem operational preflight | Unproven | NW-093 blocked package; no accepted readiness boundary yet |

## Appendix B - Evidence Inputs Checked

- Diagnosis packet: `.review/diagnose.md`.
- Parallel diagnostic drafts:
  `.review/untracked-user-notes/analysis/NW-ee-pilot-behavior-route-causal-analysis.md`
  and
  `.review/untracked-user-notes/analysis/NW-xx-pilot-behavior-route-causal-analysis.md`.
- Required repository evidence named by the packet, including CDL README,
  module interfaces, decision-anchor layer files, product vision, viability
  reviews, scenarios index, status/backlog, NW-155 route note, NW-156 scoped
  configured-work evidence inspection spec, and the stock operations PM
  handoff.
- Code/test evidence only for classification: `EventRepository`,
  `ScopedOperationalReportSnapshotService`, web-admin operational report and
  evidence templates, `SyntheticStockOperationsFirstFlowIntegrationTest`,
  `StockOperationsPilotPackageIntegrationTest`, and
  `mobile/test/stocktake_capture_smoke_test.dart`.

## Appendix C - Validation

Diagnostic/docs-only validation:

- `git diff --check`
- `test -f docs/agent-working-surface/artifacts/NW-157-pilot-behavior-route-causal-analysis.md`
- `rg "NW-157|causal analysis|pilot behavior" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/agent-working-surface/artifacts/README.md`

Runtime tests skipped because this is diagnostic/docs-only.
