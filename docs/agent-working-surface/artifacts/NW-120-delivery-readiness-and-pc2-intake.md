# NW-120 Delivery Readiness And PC2 Intake

Status: non-authoritative product-planning and engineering-quality intake artifact
Document type: product_planning_artifact / engineering_quality_intake
Source: NW-120 handoff; accepted NW-119 PC1 managed lab proof; active backlog, validation, gap-routing, scenario, viability, and code-boundary inputs
Authority: routing and classification only; does not select PC2, accept product behavior, approve real production, change validation policy, or authorize implementation
Last reviewed: 2026-06-21

## 1. Current Standing

The current routing panel matches the expected NW-120 starting point:

- Last accepted NW: `NW-119 PC1 managed lab proof`.
- Active implementation gate: none.
- Active process/control slice: none.
- Current blocker: none.
- PC1 proof route: parked after synthetic managed-lab proof.
- Real users/data: still blocked behind `NW-093`.

The handoff named `docs/viability_assessment.md`; that file is not present in
this repository. The matching required viability input read for NW-120 was
`docs/viability-assessment.md`.

Sources read for this artifact:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/mobile-analyzer-known-issues.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/scenarios/README.md`
- `docs/viability-assessment.md`
- `docs/reviews/scenario-baseline-pressure-map.md`
- `docs/reviews/viability-closure-review.md`
- `server/src/main/java/dev/datarun/server/event/EventRepository.java`
- `server/src/main/java/dev/datarun/server/authorization/WebAdminOperationalViewService.java`
- `docs/agent-working-surface/artifacts/README.md`
- `docs/agent-working-surface/prompts/README.md`
- `docs/agent-working-surface/artifacts/NW-119-pc1-managed-lab-proof.md`
- `docs/agent-working-surface/prompts/NW-119-run-pc1-managed-lab-proof.md`

## 2. What PC1 Proved

PC1 has proved a bounded, synthetic, non-sensitive product journey:

- setup/config workflow through production `/web-admin/config`;
- assignment administration through production `/web-admin/assignments`;
- mobile external-user-agent OIDC/PKCE login flow and actor activation;
- mobile get-work, readiness, capture, local save, correction, and sync path;
- minimal read-only `/web-admin/operational` latest visible synced work and
  one generic attention cue for the final supervisor/reviewer demo beat;
- synthetic internal and managed-lab proof review across all 16 NW-111
  sequences with `PASS` standing;
- PC1 remains `synthetic-demo-ready, not real-production-ready`.

This is enough to stop repeating PC1 proof work and move to professional PC2
candidate selection.

## 3. What PC1 Did Not Prove

PC1 did not prove:

- real users, real organizational data, customer data, production secrets, or
  real-production go/no-go approval;
- provider, region, jurisdiction, support, compliance/security, or continuity
  commitments;
- broad reporting, import/export, warehouse, analytics, completeness, or
  drilldown behavior;
- conflict queue workflow, batch review, resolver reassignment, or runtime
  auto-resolution;
- S06/entity lifecycle, maintained registry stewardship, deactivation,
  discovered-unit lifecycle, or bulk registry changes;
- tenant-aware runtime, pooled storage, tenant sync context, workspace-scoped
  config, control-plane lifecycle, or tenant isolation evidence;
- local retention/security/offboarding, field-level sensitivity, encryption,
  redaction, erasure, or no-local-retention policy;
- `flutter analyze` green standing;
- production Secure SDLC completion;
- real-use reliability/operations readiness beyond synthetic reference
  rehearsal evidence.

## 4. Code-Boundary Debt Assessment

`EventRepository` remains the event-store access surface for append, sync pull,
subject history, rebuildable projection support, duplicate/integrity lookup,
and the NW-114 minimal PC1 operational latest-work read. NW-114 explicitly
tolerated one single-item operational read query and stated that it is not
precedent for a second operational read surface, list, filter, aggregate,
drill-back, export, multi-item attention view, or reporting-style view.

`WebAdminOperationalViewService` now has two relevant boundaries:

- it uses `EventRepository.findLatestVisibleSubjectWorkEvent(...)` for latest
  visible work after assignment-scope predicates;
- it reaches through `EventRepository.getJdbcTemplate()` to query unresolved
  attention for that event.

The direct `JdbcTemplate` reach-through is code-boundary debt. It should not be
fixed inside NW-120 because NW-120 is docs-only, but it must shape PC2 intake:
if PC2 extends operational reads, reporting, conflict visibility, drill-back, or
attention surfaces, the selected PC2 must route a read-model/query-boundary
prerequisite before implementation.

## 5. Validation Gate Hardening Assessment

The validation matrix says docs-only work requires `git diff --check`, targeted
file checks, targeted greps, and skipped runtime-test rationale. Runtime tests
are not required for NW-120 because no runtime code, tests, contracts, CI,
validation policy, or product scope changes.

Known validation hardening items are visible but not blocking PC2 selection:

- `flutter analyze` is known-red with 7 recorded issues and is not a hard gate
  until fixed or baselined.
- Server log-volume / validation-operability is a candidate follow-up route.
- Mobile fake/harness cleanup is a candidate follow-up route.
- Shared fixture / contract parity improvements are a candidate follow-up
  route and become a prerequisite when a selected PC2 changes cross-runtime
  contracts, projections, sync, config, or shared fixtures.

## 6. Security / Secure SDLC Assessment

Security is not a reason to block PC2 selection, but it is a hard gate before
real use. NW-093 remains the real-production approval route for provider,
region, jurisdiction, real users/data, support, compliance/security, continuity,
real IdP path, and go/no-go.

PC2 selection must preserve these guardrails:

- no IdP group/claim/JWT `actor_id` authority without successor decision;
- no emergency/special write bypass without selected special-access route;
- no broad audit/history or redacted/no-local-retention read surface without
  product/security decision;
- no tenant/user/organization selector treated as authority before tenant and
  control-plane routes are selected.

## 7. Reliability / Operations Readiness Assessment

Synthetic reference deployment and recovery evidence exists, including the
NW-063 through NW-067 route and NW-075 through NW-081 adapter records. That
evidence supports bounded synthetic reference confidence only.

Reliability/operations readiness becomes a gate before real use, not before PC2
selection. Any PC2 candidate that proposes real users/data, provider/region,
jurisdiction, support, production continuity, or live customer operation must
stop and select NW-093 first.

## 8. Architecture-Risk Assessment

Architecture risk is not a call to reopen the whole architecture. It is a route
selector. PC2 intake must re-run gap classification when a candidate touches:

- broad read/reporting/aggregate semantics;
- sync/access scope or new subject/query/custom scope mechanisms;
- stored event meaning, envelope fields/types, or historical interpretation;
- conflict resolver truth, resolver reassignment, batch resolution, or
  auto-resolution;
- durable workflow-state authority, pattern traversal, or pattern inventory
  expansion;
- tenant-aware runtime/storage/sync/config/auth;
- local retention/security behavior with compatibility impact.

If a PC2 candidate crosses one of these triggers, the selected PC2 must include
the owning architecture/platform route before implementation.

## 9. Production Approval / NW-093 Standing

NW-093 remains `blocked`.

Trigger:

```text
Concrete real users, real organizational data, provider, region, jurisdiction,
or support commitment appears.
```

Do not claim real-production readiness from PC1 proof, synthetic managed-lab
proof, synthetic reference rehearsal, or PC2 selection.

## 10. Tenant / Managed Deployment Standing

The current product standing remains the PC1 managed-isolation route: one
customer-facing Organization maps to one managed single-tenant Datarun
deployment with one internal/default Workspace.

NW-094 through NW-098 remain visible but unselected:

- NW-094: managed-deployment SaaS control-plane boundary.
- NW-095: tenant-aware identity, membership, and actor model.
- NW-096: tenant data isolation and sync/config partitioning strategy.
- NW-097: singleton tenant/default workspace scaffold.
- NW-098: tenant isolation test harness.

PC2 must not introduce tenant-aware runtime predicates, tenant sync context,
workspace-scoped config, local partition-key changes, storage backfills, pooled
`tenant_id` semantics, envelope changes, or UI tenant selection by drift.

## 11. Scenario-To-PC Candidate Fronts

PC2 selection should compare candidate fronts without selecting implementation
inside NW-120:

| Candidate front | Evidence base | Main prerequisite before delivery |
|---|---|---|
| Setup/admin polish | NW-088, S23, PC1 proof | Keep config-package authority fixed; route setup-lifecycle semantic changes separately. |
| Assignment/admin operations polish | NW-090, PC1 proof | Preserve command capability and containment; no generic/root admin bypass. |
| Mobile field workflow polish | NW-101, PC1 proof, S00/S19 | Keep analyzer known-red visible; use mobile tests and harness cleanup as needed. |
| Readiness/freshness/attention expansion | NW-114, S21/S26 | Select read-model/query/reporting boundary before any second operational read surface. |
| Reporting/import/export/aggregate oversight | NW-033/S26, GAP-PROJECTION-01/02 | Select NW-044 or bounded reporting spec before delivery. |
| Conflict queue or resolution workflow | NW-029/S21, NW-030/S27, flag evidence | Select NW-072 for current durable behavior; select NW-045 for automation/batch. |
| Pattern registry/projection follow-through | NW-030/S27, NW-032/S23, BAR-012/014 | Select NW-073 before dependent pattern product/API/projection work. |
| S06/entity lifecycle | S01/S06/S22 pressure | Select NW-021 before registry lifecycle implementation. |
| Tenant/control-plane | NW-083, GAP-PRODUCT-01 | Select NW-094 through NW-098 only when the trigger appears. |

## 12. Required Classification

| Item | Classification | Route / trigger |
|---|---|---|
| EventRepository / operational read-model boundary | must address before PC2 selection | NW-121 must decide whether the PC2 candidate needs read-model/reporting/query-boundary work before delivery. Any second operational read surface triggers a selected boundary route. |
| WebAdminOperationalViewService direct `JdbcTemplate` reach-through | quality hardening route | Clean up as bounded code-boundary hardening, or promote to prerequisite if selected PC2 extends operational reads/attention. |
| `flutter analyze` known-red standing | quality hardening route | Future analyzer cleanup or baseline before making analyzer a blocking gate. |
| Server log-volume / validation-operability concerns | quality hardening route | Candidate validation/ops hardening route; becomes real-use gate only when NW-093 is selected. |
| Mobile fake/harness cleanup | quality hardening route | Future mobile test harness route; use before broad mobile workflow expansion if fake quality obscures evidence. |
| Shared fixture / contract parity | prerequisite inside selected PC2 | Required when selected PC2 touches contracts, projections, sync, config, pattern behavior, or cross-runtime fixtures. |
| Secure SDLC / security pass | security/reliability gate before real use | Gate before real users/data or production approval; route through NW-093/security review trigger. |
| Reliability / operations readiness | security/reliability gate before real use | Synthetic evidence exists; real-use readiness remains NW-093-gated. |
| Architecture risk reduction | architecture/platform decision route | Use the gap-routing playbook before implementation when PC2 crosses structural triggers. |
| Production approval path `NW-093` | future/blocked route with explicit trigger | Select only on concrete real users/data, provider, region, jurisdiction, or support commitment. |
| Tenant / managed deployment control plane `NW-094` through `NW-098` | future/blocked route with explicit trigger | Select only for multi-customer managed deployment, SaaS control plane, tenant selection, tenant-aware runtime, pooled storage, or isolation-test pressure. |
| Reporting / import / export / aggregate oversight `NW-044` | future/blocked route with explicit trigger | Candidate PC2 front, but not selected by NW-120. Select if PC2 chooses reporting/import/export/aggregate work. |
| Conflict detection/resolution/queue/batch/automation `NW-072` and `NW-045` | prerequisite inside selected PC2 | NW-072 before conflict UI/flag reporting/current behavior dependence; NW-045 before batch/automation/resolver-reassignment work. |
| Pattern registry/projection follow-through `NW-073` | prerequisite inside selected PC2 | Select before pattern traversal/reporting, inventory expansion, projection changes, or product/API work. |
| S06/entity lifecycle `NW-021` | future/blocked route with explicit trigger | Candidate PC2 front, but must be selected explicitly before registry lifecycle delivery. |
| Scenario-to-PC follow-through from S00-S27 | must address before PC2 selection | NW-121 should map scenario evidence to one PC2 boundary and explicit non-goals/triggers. |

## 13. Recommended Delivery Lanes

Use these lanes to keep selection professional without turning NW-121 into a
multi-track implementation plan:

| Lane | Purpose | Selection rule |
|---|---|---|
| PC2 product selection lane | Choose exactly one PC2 product candidate boundary and PM handoff. | Run NW-121 next. |
| Product capability front lane | Compare setup/admin, mobile workflow, operational visibility, reporting, conflict, pattern, lifecycle, and tenant fronts. | Select one bounded PC2, not a bundle. |
| Validation hardening lane | Analyzer, log-volume, mobile fake/harness, shared fixtures. | Promote only when the selected PC2 needs the gate or evidence is unreliable without it. |
| Security / real-use lane | Secure SDLC, real IdP/support/compliance, production go/no-go. | Select NW-093 before real users/data. |
| Reliability / ops lane | Deployment, monitoring, backup/recovery, continuity. | Treat synthetic reference evidence as current baseline, real-use evidence as NW-093-gated. |
| Architecture/platform lane | Read-model, reporting, conflict automation, pattern, S06, scope, tenant/control-plane. | Select owning route before implementation when triggered. |

## 14. Recommended Successor NW

Recommended successor, exactly one:

```text
NW-121 - Select PC2 product candidate boundary and PM handoff
```

Rationale: the evidence does not show that analyzer cleanup, log-volume
hardening, mobile harness cleanup, shared fixture parity, Secure SDLC,
reliability/ops hardening, tenant/control-plane work, reporting/import/export,
conflict automation, pattern follow-through, or S06/entity lifecycle must be
completed before choosing a PC2 candidate. They must instead be explicit gates,
prerequisites, non-goals, or triggers in the PC2 selection/handoff.

NW-121 should not implement. Its job is to select one PC2 boundary and produce
the PM handoff with:

- scenario front chosen;
- explicit non-goals;
- validation gates;
- security/reliability gates;
- architecture/platform prerequisites;
- code-boundary guardrails;
- tenant/control-plane wording;
- one recommended implementation successor only if the handoff proves one is
  ready.

## 15. Not Selected Now

| Not selected by NW-120 | Trigger to select later | Required wording now |
|---|---|---|
| NW-093 real-production approval | Real users/data, provider, region, jurisdiction, support, compliance/security, continuity, or go/no-go pressure. | PC1/PC2 planning is not real-production approval. |
| NW-094 managed-deployment SaaS control plane | Multi-customer managed deployment or SaaS control-plane work becomes active. | Managed-isolation is current route, not forgotten control-plane work. |
| NW-095 through NW-098 tenant-aware internals/isolation | Tenant selection, pooled runtime, tenant-aware storage/sync/config/auth, scaffold, or isolation-test pressure. | Do not add tenant predicates, workspace config, tenant sync context, or UI tenant choice by drift. |
| NW-044 reporting/import/export/aggregate | PC2 selects reporting/import/export/aggregate oversight or a second operational read/reporting surface. | Current S26 evidence is constrained; reporting product behavior remains unselected. |
| NW-072 conflict/flag durable behavior extraction | Conflict UI, flag reporting, resolver eligibility, or current flag behavior must become normative for PC2. | Current flag behavior is evidence, not a new conflict product. |
| NW-045 conflict automation/batch/resolver reassignment | Batch resolution, auto-resolution, resolver reassignment, or conflict automation pressure appears. | Manual exact-resolver semantics remain current. |
| NW-073 pattern registry/projection extraction | Pattern traversal/reporting, inventory expansion, projection change, or pattern API/product work is selected. | Pattern definitions/projections are current evidence, not broad workflow-state authority. |
| NW-021 S06/entity lifecycle | PC2 selects maintained known-set registry lifecycle, discovered-unit lifecycle, deactivation, merge/split UX, or bulk registry changes. | Recording about a subject is not accepting S06 lifecycle. |
| Analyzer cleanup/baseline | Analyzer must become blocking or mobile work needs clean analyzer evidence. | `flutter analyze` is known-red and non-blocking today. |
| Server log-volume / validation-operability hardening | Validation evidence becomes noisy enough to obscure acceptance, or real-use operations route requires it. | Candidate hardening, not PC2-selection blocker. |
| Mobile fake/harness cleanup | Mobile tests expand and fakes obscure meaningful evidence. | Candidate hardening, not product behavior. |
| Shared fixture / contract parity hardening | Selected PC2 changes cross-runtime contracts/projections/sync/config/pattern behavior. | Parity is a prerequisite for such PC2 delivery, not a broad preselection blocker. |
| Direct operational read cleanup | PC2 extends `/web-admin/operational`, attention, reporting, drill-back, or read-model behavior. | The NW-114 query remains a one-off tolerated PC1 proof until a selected route changes it. |

## 16. Control-Plane Wording Recommendations

Use this wording in NW-121 and later PC2 handoffs:

- "Managed deployment/control plane is parked but visible, not forgotten."
- "Current product route remains one Organization mapped to one managed
  single-tenant deployment with one internal/default Workspace."
- "Tenant-aware auth, workspace-scoped config, tenant sync context, local
  partition keys, storage backfills, pooled `tenant_id` predicates, and envelope
  changes remain unselected until NW-094 through NW-098 are selected."
- "A PC2 candidate may use the current managed-isolation vocabulary only as a
  product boundary; it must not introduce tenant-aware runtime behavior by
  naming alone."
- "Any real users/data or production commitment still routes through NW-093
  before use, even if PC2 selection is otherwise accepted."

## 17. Stop-Condition Check

No stop condition fired. NW-120 did not require choosing real users/data,
provider, region, jurisdiction, support, production readiness, tenant-aware
runtime decisions, auth/security authority changes, event semantics,
repository architecture changes, validation gate changes, reporting/import/export
acceptance, conflict automation/batch acceptance, architecture reopening, or
SPEC-* resurrection.
