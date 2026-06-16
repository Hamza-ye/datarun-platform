# Assignment Admin Authority Exploration

Status: accepted exploration
Date: 2026-06-04
Authority: none. This is routing analysis only. CDL, BAR, contracts, IDRs, and code remain the authority surfaces.

## Direct Answer

Keeping assignment administration outside activity role-actions leaves a clean model. It avoids making optional `activity_ref` and `activities[*].roles` globally load-bearing for authority administration.

The current model is clean but coarse: assignment create/end is authenticated and scope-contained, but it has no separate command-capability check beyond containment. That is sufficient for current baseline probes and near-term field responsibility management when assignment administration stays with root/covering coordinators. It is not sufficient as a production product surface if any actor with covering field scope must not be allowed to create or end same-scope assignments.

Recommendation: keep the current containment-only model accepted for the current baseline, but route a successor durable decision/spec before exposing or expanding production assignment-admin UI/API behavior. Split access exceptions into separate successor exploration because auditor access, shared devices, grace periods, subject-scope variants, and new scope mechanisms are not the same problem as assignment create/end command authority.

## Current Implemented Model

| Area | Current model |
|---|---|
| Assignment command authority | `/api/assignments` is bearer-bound. Create/end uses the authenticated actor, not request-body actor IDs. Creation requires requested `geographic`, `subject_list`, and `activity` scope to be contained by one active creator assignment, or by the explicit initial bootstrap path. Ending requires target-assignment authority over the target assignment scope. |
| Current gap | Containment prevents scope expansion, but role/capability does not limit who may administer assignments inside their own scope. A field actor with covering scope is not distinguished from a coordinator by the command path itself. |
| Activity role-action authority | `activities[*].roles` authorizes only `capture`, `review`, `alert`, `task_created`, and `task_completed`. `assignment_changed` is invalid there. Server accepts structurally valid work events and flags `role_stale`; mobile warnings are advisory only. |
| Scope axes | Platform-fixed axes are `geographic`, `subject_list`, and `activity`. AND within one assignment, OR across assignments. `null` is unrestricted on an axis; empty arrays are invalid. Separate assignments do not union across axes for assignment creation containment. |
| Scoped sync | Normal pull is current actor-scope filtered and watermark-based. Sync scope equals access scope. Subject-history backfill is a separate subject/activity cursor surface with request-time authorization on every page and no mutation of normal live-sync watermarks. |
| Conflict resolver inclusion | Flags designate one resolver. Only exact designated-resolver resolutions are canonical. Unauthorized resolution attempts are accepted and flagged. Assignment administration does not authorize resolver reassignment or auto-resolution. |
| Production principal binding | OIDC/JWT/Keycloak credentials authenticate to one platform actor through explicit `(issuer, subject) -> actor_id` bindings. Groups, roles, resource claims, JWT `actor_id`, request-body actors, and UI labels are not platform authority. |
| Operational UX vocabulary | NW-047 allows product words such as assignment, handoff, pending review, attention item, and report view as non-authoritative UX vocabulary only. These terms must not define sync, assignment command authority, resolver equality, or projection correctness. |

## Questions Answered

| Question | Answer |
|---|---|
| Does keeping assignment administration outside activity roles make scoped assignment permissions harder to model? | No. It makes them easier to keep secure because assignment administration remains an online command path with scope containment instead of an activity work action. The unresolved part is a command-capability layer, not activity-role expansion. |
| Does containment-only suffice near-term? | Yes for current baseline and S22/S25-style responsibility management when assignment changes are made by root or covering coordinators. No for a production assignment-admin surface where same-scope field actors must not delegate, end, or reshape responsibility. |
| Minimal command-authority vocabulary or policy shape? | Candidate vocabulary for an IDR: `assignment_admin.create` and `assignment_admin.end`, with bootstrap/provisioning kept explicit and separate. It should live outside `activities[*].roles`, be evaluated server-side before containment, and preserve IDR-024 axis containment. The IDR should decide whether this is assignment-role policy, deployment admin policy, or another platform-owned assignment-admin policy surface. |
| What must remain outside? | `activities[*].roles`, IdP groups/claims/JWT `actor_id`, request-body actor IDs, envelope fields such as `authority_context` or `assignment_ref`, resolver reassignment, auto-resolution, and UI/product vocabulary. |
| Which access exceptions belong here? | Temporary responsibility grants that are ordinary assignment create/end commands belong in assignment-admin authority. Auditor/special access, shared-device actor switching, grace-period behavior, subject-scope variants, and new scope mechanisms should be separate decisions. |
| What tests or probes would prove the recommended path? | After an IDR: deny a covering-scope actor lacking assignment-admin capability; allow a capable actor only within contained scope; deny out-of-scope create/end; prove spoofed body actors and IdP claims/groups do not grant command authority; preserve normal sync, subject-history, resolver equality, and S22 handoff behavior. |
| What UX terms must wait for NW-047? | None for this slice because NW-047 is accepted. Future screens may use NW-047 vocabulary, but no UX term may become authority. |

## Archive-To-Current Reconciliation

| Source | Archive claim or pressure | Current status | Remaining gap | Route |
|---|---|---|---|---|
| Access-control scenario | Authority is contextual, temporary, hierarchical, and may include auditors or special cross-boundary visibility. | Assignment-based access, scoped sync, production principal binding, and accept-and-flag staleness are accepted. | Auditor/special access and temporary exceptions beyond ordinary assignments are not accepted. | Separate access-exception successor. |
| S25 worker onboarding/transfer/exit | Transfers need continuity, stale offline work must remain traceable, successors need enough history. | Assignment end/create, stale authority flags, scoped pull, subject-history backfill, and selective retention are accepted. | No new command capability for who may administer transfer beyond containment. | Assignment-admin IDR if product exposes transfer administration beyond root/covering coordinator use. |
| S22 coordinated campaign | Supervisory reassignment, overlapping work, distribution handoff, and offline stale work create high pressure. | NW-042 proved these with existing constructs and no new scope, UI, trigger, or activity-action expansion. | Full discovered-unit lifecycle, custom campaign/custody scope, and general reporting remain deferred elsewhere. | No assignment-admin implementation now; keep S22 evidence as current sufficiency proof. |
| Archive 10 | Coordinator assignment commands are server-side, within scope; assignment is the atomic authorization grant; reassignment while offline is accepted and flagged. | IDR-023/024, CDL-030/034/035, code, and tests implement the server-side containment and stale-flag path. | Command role/capability remains coarse. | Successor assignment-admin command-authority IDR. |
| Archive 10 | Subject history on reassignment and sync scope equals access scope are core handoff pressures. | BAR-003/BAR-004 accepted normal scoped pull and separate subject-history backfill. | No broad audit pull; no arbitrary historical reconstruction. | Keep separate from assignment-admin; route audit access separately. |
| Archive 10/11 | Auditors, shared devices, grace periods, query-like access, and data expiry are access exceptions. | BAR-104 rejects IdP claim authority; BAR-108 keeps new scope mechanisms future-decision. | No accepted auditor/special access, shared-device session model, or grace behavior. | Separate access-exception successor. |
| Archive 11 | Misconfigured coordinator can escalate scope unless assignment create validates containment. | CDL-034 and IDR-024 require containment; code/test evidence covers all three axes. | Containment does not distinguish field vs coordinator command authority inside the same scope. | Assignment-admin IDR. |
| Archive 11 | Concurrent role grants can accumulate privileges. | Current role-action semantics OR across covering assignments for work actions; assignment command path has no role/capability layer. | Role/capability compatibility for assignment administration is undecided. | Assignment-admin IDR, not `activities[*].roles`. |
| Archive 12 | Do not store `authority_context`; authority is projection from assignment timeline and sync context. | CDL-032 accepted; no envelope authority field is active. | Future command capability must not add `authority_context` or `assignment_ref`. | Guardrail for all successors. |
| Archive 15 | Scope computation is server-side; assignment resolution exists on device/server; patterns do not provide scope/role bindings; scope types are platform-fixed. | Module boundaries and CDL-055 match this. | New scope mechanisms remain future-decision. | Access-exception successor only if needed. |

## Risk Split

| Risk area | Current risk | Routing |
|---|---|---|
| Authorization | Same-scope actors are not command-capability distinguished on assignment create/end. This is acceptable for current probes but unsafe for broad product exposure. | Assignment-admin command-authority IDR before production UI/API expansion. |
| Projection | Authority stays reconstructable from events; adding envelope authority fields would create durable drift. | Preserve CDL-032; projections may display authority context but cannot store it as envelope truth. |
| Offline sync | Stale work is accepted and flagged; sync remains current-scope/watermark based. | No normal watermark rewrites. Keep subject-history separate. |
| Reassignment | Current end/create plus subject-history supports handoff. Same-scope command capability and role compatibility remain undecided. | IDR for command authority; S25 probe only after decision if needed. |
| Resolver inclusion | Assignment admin cannot imply resolver reassignment or broad conflict admin. | Preserve IDR-026 exact resolver equality; route resolver reassignment separately. |
| Access exceptions | Auditor/special access, shared-device sessions, grace behavior, and query/custom scope can leak data if folded into ordinary assignments casually. | Separate future-decision exploration tied to BAR-108 and production-auth guardrails. |
| UX semantics | Product terms can obscure authority boundaries. | Use NW-047 vocabulary only as presentation language. |

## Path Comparison

| Path | Benefit | Security/architecture cost | Fit | Recommendation |
|---|---|---|---|---|
| Keep current containment-only assignment administration | Already implemented and tested; blocks scope expansion; sufficient for S22/S25 current probes. | Any actor with covering scope can administer same/narrower assignments if the API is exposed to them. | Good as current baseline, poor as durable product admin model. | Keep for now; do not broaden UI/API exposure without successor durable decision/spec. |
| Add separate scoped assignment-admin command capability outside `activities[*].roles` | Directly closes same-scope command risk while preserving activity role-action boundary. | Needs policy surface, validation rules, tests, and migration/default behavior. | Best likely future path. | Route NW-048 decision. |
| Attach command capabilities to assignment/config policy while preserving containment | Lets deployers configure which assignment roles may create/end assignments within platform rules. | Must prevent deployer-defined containment logic and avoid turning activity roles into admin roles. | Plausible implementation option after successor decision. | Compare in NW-048. |
| Central/provisioning-only administration, no field assignment-admin UI | Strongest security posture and audit simplicity. | Operationally rigid; may not satisfy field transfer/coverage needs. | Viable for production deployments that centralize admin. | Keep as NW-048 alternative, not default. |
| Route access exceptions separately | Prevents auditor/shared-device/grace/new-scope concerns from contaminating command authority. | Requires additional exploration/decisions before those surfaces ship. | Required by BAR-108 and NW-047 guardrails. | Route NW-049 as separate future-decision exploration. |

## Recommendation

1. Mark NW-043 accepted as an exploration/routing slice.
2. Preserve current containment-only assignment administration as the accepted baseline for existing probes.
3. Add NW-048 as the next assignment-admin authority decision route. Output should be an IDR, not implementation.
4. Add NW-049 as a separate access-exception exploration route for auditor/special access, shared-device actor sessions, grace behavior, subject-scope variants, and new scope mechanisms.
5. Do not implement runtime changes until NW-048 or another successor decision selects the policy surface and tests.

## Successor Rows

| ID | Title | Type | Suggested status | Purpose |
|---|---|---|---|---|
| NW-048 | Decide assignment-admin command capability model | future_decision / IDR | `ready` | Choose whether and how `assignment_admin.create` and `assignment_admin.end` style authority exists outside activity role-actions, before any production assignment-admin UI/API expansion. |
| NW-049 | Explore access exceptions and shared-device scope | future_decision | `future_decision` | Split auditor/special access, shared-device actor sessions, grace behavior, subject-scope variants, and new scope mechanisms from assignment-admin command authority. |

## Stop Conditions For Successors

- Proposed path adds envelope fields, envelope types, or `assignment_ref`.
- Proposed path expands `activities[*].roles` beyond the IDR-023 five work actions.
- Proposed path infers authority from IdP groups/claims, JWT `actor_id`, request-body actor IDs, or UI vocabulary.
- Proposed path reassigns conflict resolvers, auto-resolves manual-only flags, or rewrites normal live-sync watermarks.
- Proposed path defines new scope mechanisms without BAR-108/CDL successor authority.
