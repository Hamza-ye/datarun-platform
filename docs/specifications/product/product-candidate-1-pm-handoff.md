# Product Candidate 1 PM Handoff

Status: active PM handoff surface
Document type: product_handoff
Owner: product steward
Source: NW-106; accepted PC1 product spec; 2026-06-19 product-goal/PM-handoff audit
Authority: derived planning surface only; does not add product behavior, architecture authority, implementation standing, validation policy, or production approval
Last reviewed: 2026-06-19

## 1 Purpose

This handoff gives a product manager one concise surface for Product Candidate
1 planning. It translates accepted PC1 scope, accepted implementation standing,
and post-audit product/PM evidence into product choices, candidate next-work
routes, and stop conditions.

This document is not the accepted product behavior spec. Use
`product-candidate-1.md` for accepted PC1 behavior. Use this handoff to decide
which bounded NW row to select next after the remaining post-audit reset rows
are accepted or explicitly parked.

## 2 Product Goal

Enable one organization in a managed single-tenant Datarun deployment to set up
and run a basic operational capture loop: web admins prepare and publish
validated setup, assign responsibilities, field users receive assigned work,
capture and correct activity entries offline or online, sync later, and
supervisors see latest synced work with freshness and unresolved attention
items, without custom software development.

## 3 Target Deployment And Boundary

PC1 targets one customer-facing Organization in one managed single-tenant
Datarun deployment with one internal/default Workspace. Product language should
say Organization; tenant/workspace language stays internal unless a later
tenant-aware route selects otherwise.

PC1 can support PM planning, synthetic walkthroughs, product demos, and a
first proof-target decision. It does not approve real production with real users
or real organizational data. Real production still requires the NW-093 package:
provider, region, jurisdiction, support, data classification, real IdP path,
compliance/security review, continuity, and go/no-go ownership.

## 4 Primary Users And Jobs

| User | Product job |
|---|---|
| Organization operator | Know whether Datarun can run the organization's basic capture loop. |
| Setup owner/config author | Prepare forms, activities, validation rules, and package candidates. |
| Setup reviewer/approver | Review readiness, approve exact setup, and publish safely. |
| Assignment coordinator | Give the right field users responsibility for the right work and end responsibility when needed. |
| Field user | Sign in, get assigned work, capture entries offline or online, correct entries, and sync later. |
| Supervisor/reviewer | See latest synced work, freshness, and unresolved attention items without treating the view as a full reporting product. |
| Deployment owner | Keep proof targets, managed-deployment decisions, and real-production approval separate. |

## 5 In-Scope PC1 Journeys

- Organization-level setup in the managed single-tenant/default-Workspace lane.
- Web-admin login/session, shell access, and command-gated setup/config work.
- Setup/config author, validate, review, approve, and publish workflow.
- Assignment create/end workflow for assigning responsibility.
- Mobile external login for field users.
- Mobile get-work/readiness, capture, saved-local/waiting/syncing/synced/failed
  states, correction, and handoff context over accepted behavior.
- Supervisor-visible latest synced work with freshness and unresolved attention
  items, if kept as a scoped operational view rather than reporting/export.
- Optional single-item attention/flag review only after its owning route is
  selected and bounded.

## 6 Explicit Non-Goals / Do-Not-Start

- Real-production approval for real users or real organizational data.
- Pooled SaaS, tenant-aware runtime internals, tenant/workspace selectors, or a
  managed-deployment control plane inside the Datarun runtime.
- Reporting dashboards, exports, imports, warehouses, aggregate analytics, or
  broad read APIs.
- Retention/security/offboarding promises beyond currently accepted local-state
  and token-placement limits.
- Entity lifecycle, known-set authority, candidate/duplicate/merge/split
  workflows, or S06 expansion.
- Conflict batch resolution, auto-resolution, resolver reassignment, or flag
  reporting.
- Online principal-binding admin UI/API.
- New scopes, deployer scripts, dynamic queries, custom state machines,
  trigger execution, contract/schema/envelope changes, or sync protocol changes.
- Validation/CI policy, AGENTS files, steward guide, BAR, CDL, gap register, or
  artifact trace changes from this handoff.

## 7 Current PC1 Standing

| Product slice | Current standing | Accepted NW/source | Remaining route | PM interpretation |
|---|---|---|---|---|
| PC1 spec | Accepted product behavior and exclusions. | NW-084; `product-candidate-1.md` | Use this handoff for planning only. | PC1 is a basic operational capture loop, not a full product suite. |
| Web admin login/session | Accepted implementation for `/web-admin` browser session. | NW-086; NW-099 | None for the basic shell; future polish routes separately. | Admins can have a production-shaped session boundary for PC1 demos/proofs. |
| Web admin command gate | Accepted explicit server-side command capabilities. | NW-087; NW-100 | Do not add root/admin bypass or browser policy editing. | PM can plan command-gated admin journeys without treating IdP claims as authority. |
| Setup/config workflow | Accepted first vertical under `/web-admin/config`. | NW-088; NW-068 | Structured-editor polish may be a future product route. | Setup is usable as a JSON candidate workflow; polish is not required to define PC1. |
| Assignment admin | Accepted `/web-admin/assignments` create/end workflow. | NW-090; NW-069 | Future usability polish only if product proof needs it. | Coordinators can assign and end responsibility through accepted command semantics. |
| Mobile external login | Accepted mobile external-user-agent OIDC implementation. | NW-085; NW-101; NW-070; NW-071 | Real production still needs NW-093; retention/security still routes through NW-054. | Field-user login exists for PC1, but not as real-production approval. |
| Mobile work/capture/sync/correction states | Accepted mobile slices for sync presentation, offline capture, readiness, and correction. | NW-059; NW-060; NW-061; NW-062; NW-101 | Product polish route if demo evidence shows gaps. | PM should validate the full field loop, not reopen platform semantics. |
| Single-flag review | Scenario/runtime pressure exists; productized PC1 review remains successor-gated. | S21 evidence; NW-056 map; NW-072 candidate | Select a single-flag/attention decision before productizing review UI or reporting. | Keep as optional attention scope, not conflict operations. |
| Reporting/freshness | Freshness and latest-synced language accepted in narrow surfaces; reporting/export deferred. | NW-059; S26 evidence; NW-044 future route | Decide minimal operational freshness/attention view, or defer reporting. | "Supervisor sees latest synced work" is not a dashboard/export promise. |
| Real production | Blocked for real users/data. Synthetic reference deployment is accepted evidence only. | NW-067; NW-075-NW-081; NW-093 blocked | Select NW-093 only when a concrete real-use target exists. | Demo/proof planning may continue; real production claims may not. |
| Tenant/control-plane future work | Managed-isolation lane selected; tenant-aware/control-plane work deferred. | NW-083; NW-094-NW-098 | Select only if multi-customer/control-plane/tenant-aware pressure appears. | PC1 should not become SaaS or tenant-aware by drift. |

## 8 Scenario-to-PC1 Slice Map

| Scenario pressure | User value | PC1 journey | Current support | Candidate NW route | Next product decision | Do-not-cross boundary |
|---|---|---|---|---|---|---|
| S00 basic capture/correction | Field user records and corrects work. | Mobile get-work, capture, correction, sync. | Accepted mobile slices cover the core loop. | PC1 product journey smoke definition; mobile polish if evidence shows gaps. | Is the current loop demo-ready for one proof target? | Do not add new event semantics or correction linkage. |
| S23 setup/config | Setup owner publishes usable setup. | Web-admin config author/validate/review/approve/publish. | Accepted `/web-admin/config` first vertical. | Setup/config structured-editor or demo-data polish, if needed. | Is JSON candidate workflow acceptable for PC1 proof? | Do not add config schema keys, scripts, triggers, or dynamic queries. |
| S19 offline sync | Field work survives intermittent connectivity. | Saved-local, waiting, sync, failed, synced. | Accepted mobile sync states and server sync behavior. | Product smoke and mobile readiness polish. | What offline proof must a PM/demo show? | Do not change sync protocol or local retention policy. |
| S01 subject-linked capture | Users attach work to a relevant subject when supported. | Subject-linked capture inside accepted assignment/config behavior. | Accepted baseline supports subject refs; S06 lifecycle remains deferred. | Vocabulary validation packet and proof walkthrough. | Is subject-linked capture required in PC1 proof? | Do not add known-set, candidate, duplicate, merge, or split authority. |
| S06 entity lifecycle | Operators may want registry continuity. | Deferred from PC1. | Future-decision only. | Entity lifecycle future lane decision. | Keep deferred or promote as a later product candidate? | Do not smuggle lifecycle into capture, setup, or reporting. |
| S21 single attention item | Supervisor resolves one unresolved issue. | Optional attention/review slice. | Kernel evidence exists; productized review remains gated. | Single-flag review/attention item decision. | Is one manual attention path needed for PC1 proof? | Do not add batch, auto-resolution, resolver reassignment, or flag reporting. |
| S22/S27 validation examples | Setup/reviewers need examples that prove rules and handoff. | Setup validation plus activity/work transfer examples. | Scenario evidence exists; vocabulary needs PM validation. | Vocabulary validation packet; product demo script. | Which examples best explain PC1 without platform terms? | Do not turn examples into new contracts or workflow primitives. |
| S26 latest synced oversight | Supervisors need current operational visibility. | Freshness plus unresolved attention view. | Freshness language exists; reporting route deferred. | Minimal scoped operational freshness/attention view; reporting boundary decision. | Is a simple operational view enough, or is reporting a later candidate? | Do not create dashboards, exports, warehouses, or broad read APIs. |
| S24 retention | Organization asks what remains on device/server. | Deferred policy question. | NW-054 future decision. | Retention/offboarding boundary decision. | Does proof target require a retention promise? | Do not promise deletion, encryption, redaction, or no-local-retention here. |
| S25 onboarding/offboarding | Coordinators need worker responsibility changes. | Assignment create/end and login/session boundaries. | Accepted assignment admin and mobile login support basic responsibility changes. | Retention/offboarding boundary decision if device data/access promises arise. | Is assignment end enough for PC1, or is offboarding policy needed? | Do not add identity lifecycle, tenant membership, or retained-data promises. |

## 9 Candidate NW Decomposition Routes

These are candidate routes, not accepted backlog rows. Promote only one bounded
row at a time.

| Candidate route | Suggested priority | User value / why now | Input sources | Output expected | Acceptance evidence | Stop condition |
|---|---|---|---|---|---|---|
| PC1 product journey smoke definition | Candidate P0 | Gives PM the shortest setup-to-sync proof path. | PC1 spec; current standing table; S00/S19/S23. | One bounded smoke route and required user steps. | PM-readable journey checklist or prompt; no accepted runtime claim. | No runtime behavior or validation-policy change. |
| Product demo script/synthetic walkthrough | Candidate P0 | Lets reviewer and PM see the accepted PC1 story without hunting through docs. | PC1 spec; product audit; accepted web/mobile slices. | Synthetic demo script with setup, assignment, field capture, sync, and freshness beats. | Script/walkthrough reviewed as synthetic evidence only. | Synthetic proof only; no real-production claim. |
| First deployment proof target decision | Candidate P1 | Decides whether next proof is demo, lab-managed pilot, or real-use preparation. | NW-067 evidence; NW-093 blocker; PC1 boundary. | Chosen proof target and explicit real-use trigger. | Decision note or NW route that names the proof target. | Real users/data trigger NW-093. |
| PM backlog view from PC1 handoff | Candidate P0 | Turns scattered candidate routes into a PM-prioritized planning queue. | This handoff; owner decisions; scenario map. | Candidate queue with owner priority and dependencies. | PM backlog surface or one promoted bounded NW row. | Candidate queue only; not accepted NW scope. |
| Vocabulary validation packet | Candidate P1 | Checks whether PC1 language works for real domain examples. | Product audit; NW-047 vocabulary; S22/S27 examples. | Small set of product terms, examples, and rejected platform terms. | Owner/domain review notes or candidate prompt. | No platform vocabulary or contract rewrite. |
| Mobile get-work/readiness/capture/correction polish | Candidate P1 | Closes field-loop demo gaps if current mobile flow is too rough. | NW-059-NW-062; NW-101; S00/S19. | Bounded polish prompt for touched mobile screens/states. | Focused mobile tests and PM walkthrough evidence. | No sync, event, authority, retention, or login semantics change. |
| Setup/config structured-editor polish | Candidate P2 | Reduces setup friction if JSON editing blocks PC1 proof. | NW-088; NW-068; S23. | Bounded structured-editor/polish prompt over accepted config. | Focused server/UI tests plus setup walkthrough. | No config package/schema/expression changes. |
| Assignment admin proof polish | Candidate P2 | Helps coordinators understand create/end responsibility in proof. | NW-090; NW-069; S25. | Bounded assignment UX/copy/polish prompt. | Focused workflow tests plus coordinator walkthrough. | No new scopes, command authorities, or online policy editing. |
| Minimal scoped operational freshness/attention view | Candidate P1 | Lets supervisors see latest synced work and unresolved attention without reporting scope. | NW-059; S21/S26; PC1 goal. | Decision or prompt for a narrow operational view. | PM proof of latest-synced/freshness/attention wording. | No reporting dashboard/export/import/warehouse. |
| Single-flag review/attention item decision | Candidate P2 | Decides whether one manual attention path belongs in PC1 proof. | S21 evidence; NW-056; NW-072 candidate. | Decision route before any review UI/product work. | Accepted decision or explicit deferral. | No batch, auto-resolution, resolver reassignment, or flag schema change. |
| Reporting boundary decision | Future decision | Prevents freshness/attention from drifting into reporting/export. | S26; NW-044; product audit. | Boundary decision: outside PC1 or later product candidate. | Accepted NW-044-style route before reporting work. | Route through NW-044 before reporting work. |
| Retention/offboarding boundary decision | Future decision | Answers proof-target questions about retained device/server data. | S24/S25; NW-054; NW-071/NW-085 limits. | Retention/offboarding decision or explicit deferral. | Accepted NW-054-style route before promises. | Route through NW-054 before promises or implementation. |
| Entity lifecycle future lane decision | Future decision | Keeps S06 pressure visible without smuggling lifecycle into PC1. | S06; S01; NW-021; PC1 non-goals. | Decision to defer or promote a later product candidate. | Accepted decision or explicit deferral. | Do not modify PC1 capture/setup scope. |
| Real-production approval package | Future decision | Prepares go/no-go only when real users/data are concrete. | NW-067; NW-075-NW-081; NW-093. | Real-production approval route with provider/region/support/data decisions. | Accepted NW-093 package before real use. | Must use NW-093; synthetic evidence is insufficient. |
| Managed-deployment/control-plane decision | Future decision | Separates PC1 managed isolation from SaaS/control-plane work. | NW-083; NW-094-NW-098. | Control-plane or tenant-aware decision route only if triggered. | Accepted NW-094-NW-098 route before implementation. | Use NW-094-NW-098; do not add runtime tenant drift. |

## 10 Product-Level Definition Of Done

Future NW-107 should own detailed validation commands and CI/test matrix
policy. This table is product-level only.

| Journey | Done when user can | Evidence category | Required guardrail | Detailed validation owner |
|---|---|---|---|---|
| Setup/config | Author, validate, review, approve, and publish a basic setup. | Product walkthrough plus existing server tests. | No schema/expression/config-package drift. | Future NW-107 validation matrix. |
| Web admin access | Sign in, enter shell, and see denial when not authorized. | Existing web-admin auth/command-gate tests plus walkthrough. | No IdP claim/group/root-admin authority. | Future NW-107 validation matrix. |
| Assignment | Create and end field responsibility inside accepted containment. | Existing assignment workflow tests plus PM scenario proof. | No new scopes or browser-selected command actor. | Future NW-107 validation matrix. |
| Mobile work loop | Sign in, get assigned work, capture offline/online, correct, and sync. | Flutter tests plus product smoke walkthrough. | No sync protocol, retention, or event-shape changes. | Future NW-107 validation matrix. |
| Freshness/attention | See latest synced status and unresolved attention without overclaiming reporting. | Scenario proof and scoped UI evidence if selected. | No dashboard/export/broad read API. | Future NW-107 validation matrix. |
| Proof target | Demonstrate the selected PC1 proof route end to end. | Synthetic/demo/proof evidence packet. | Real users/data trigger NW-093 first. | Product steward plus future validation owner. |

## 11 Owner Decisions

- Which proof target comes first: internal synthetic demo, managed lab pilot, or
  preparation for a real organization?
- Which domain vocabulary/examples should validate the PC1 story?
- Is JSON setup candidate editing acceptable for the first proof, or is
  structured setup polish required?
- Is a minimal latest-synced/freshness/attention view required before proof, or
  is it a later product candidate?
- Is one single-flag attention review path in PC1, or should review stay
  deferred?
- Does onboarding/offboarding need only assignment create/end, or does it need
  retention/security policy first?
- Should S06/entity lifecycle stay deferred for PC1?
- Who owns the PM backlog view and priority order after NW-107 through NW-109
  are accepted or explicitly parked?

## 12 How To Select The Next Product NW

Do not select product implementation until NW-107 through NW-109 are accepted
or explicitly parked.

After that, select the next product NW by:

1. Pick one owner decision or proof gap from this handoff.
2. Confirm the route stays inside accepted PC1 behavior or explicitly names the
   required future-decision route.
3. Convert only that route into one bounded NW row and prompt.
4. Keep candidate routes as planning inputs; do not treat them as accepted
   backlog rows.
5. Stop before implementation if the route touches real production, reporting,
   retention/security, entity lifecycle, conflict automation, tenant/control
   plane, contracts, architecture authority, or validation/CI policy without
   the owning NW being selected first.
