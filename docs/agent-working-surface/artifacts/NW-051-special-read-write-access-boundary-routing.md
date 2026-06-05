# Special Read/Write Access Boundary Routing

Status: accepted routing
Date: 2026-06-05
Authority: none. This is routing analysis only. CDL, BAR, contracts, IDRs, and code remain the authority surfaces.

## Executive Route

Do not create a new special-access mechanism in this slice.

Simple current-scope auditor visibility remains ordinary assignment/config posture. If an auditor or observer's needed visibility fits the existing `geographic`, `subject_list`, `activity`, and temporal assignment axes, grant a normal assignment for that scope and configure the role with no or limited activity work actions. Normal sync remains equal to current assignment-derived access scope.

Broad audit/history access is explicitly deferred. Cross-boundary inspection, arbitrary historical reconstruction, report-style audit pulls, redaction/no-local-retention views, or sensitivity-specific audit behavior need a successor product/security decision before implementation. That successor must define a separate read surface or platform-owned mechanism without turning normal pull or subject-history backfill into broad audit delivery.

Emergency or special write authority is ordinary temporary authority only when it is represented as time-bounded assignments plus existing role-action permissions. Any bypass of assignment scope, activity role-action checks, exact resolver equality, accept-and-flag semantics, or current scope mechanisms is deferred until a successor security/platform decision explicitly selects it.

NW-050 stays settled. `assignment_admin.create` and `assignment_admin.end` authorize only assignment create/end commands through the server-side command-capability policy plus IDR-024 containment. They do not authorize domain work, read expansion, conflict resolution, resolver reassignment, auto-resolution, audit-history access, or emergency override semantics.

No successor backlog row or prompt is added from this slice. A future row should be added only when a concrete deployment/product/security need names broad audit/history access or override writes as an implementation target.

## Questions Answered

| Question | Answer |
|---|---|
| Is current-scope auditor visibility adequately modeled as ordinary assignments with no or limited work actions? | Yes, when the needed visibility fits the existing assignment axes. Use ordinary assignment scope for visibility and activity role-action config to avoid or limit work actions. This does not create a hard client-side no-write channel: a forged or stale structurally valid write is still accepted, flagged, and excluded until exact resolution. |
| If broad audit/history is needed, is it a separate read surface, platform-owned mechanism, or explicit deferral? | Explicit deferral for now. If promoted, it must be a separately decided read surface or platform-owned mechanism. It must not be normal `/api/sync/pull`, must not mutate normal watermarks, must not broaden `/api/sync/subject-history`, and must account for BAR-106 retention/sensitivity and BAR-108 scope pressure if it crosses current axes. |
| Does emergency/special write authority fit ordinary time-bounded assignments plus role-action permissions? | Yes for ordinary temporary coverage, response, or handoff work. Create/end assignments through NW-050-authorized administrators, set bounded validity, and rely on server role-action detection. Emergency authority that bypasses scope, role-action, resolver equality, or accept-and-flag is not authorized by current decisions. |
| What is unsafe to authorize through assignment-admin command capability? | Domain work actions, audit/history pulls, special read visibility, conflict resolution, resolver reassignment, auto-resolution, mobile authoritative rejection, IdP group/claim authority, new scope mechanisms, normal sync watermark changes, retention deletion, and any emergency override bypass. Assignment-admin command capability is only a create/end assignment command gate. |
| What tests would prove the chosen route without weakening sync/access equality or resolver equality? | See the route test table below. The proof should stay negative and boundary-focused unless a future decision promotes a new surface. |

## Route Test Expectations

| Route | Minimal proof before implementation acceptance |
|---|---|
| Scoped auditor as ordinary assignment | Actor with an auditor/observer assignment pulls only current authorized events for the assignment scope; out-of-scope events are absent from normal pull; activity role-action config gives no unintended write authority; mobile UI affordances remain advisory only. |
| Unauthorized or stale auditor write | A structurally valid write by a role lacking the attempted action is persisted, emits the existing authorization flag such as `role_stale` or `scope_violation`, and remains excluded from projections until exact designated-resolver acceptance. |
| Broad audit/history remains out of normal sync | Normal pull never returns arbitrary historical or cross-boundary records; subject-history remains subject/activity-bound and request-time authorized on every page; normal device watermarks are not lowered or rewritten. |
| Ordinary emergency coverage | A time-bounded assignment grants only contained scope and configured activity actions during its validity window; after end/expiry, stale offline work remains accepted-and-flagged rather than rejected or silently authorized. |
| Assignment-admin boundary | `assignment_admin.create` and `assignment_admin.end` allow only assignment lifecycle commands when one active assignment both grants the command and contains the target scope; those commands do not authorize domain events or conflict-resolution actions. |
| Production auth non-authority | OIDC/JWT groups, roles, resource claims, custom claims, and JWT `actor_id` claims do not grant read, write, assignment-admin, resolver, or emergency authority. Only explicit principal binding plus assignment-derived authority counts. |
| Resolver equality | A conflict resolution is canonical only when authored by the exact `designated_resolver`; special read visibility, assignment-admin capability, broad admin vocabulary, or emergency posture cannot clear a flag or reassign the resolver. |

## Deferred Product/Security Decisions

| Deferred surface | Decision needed before implementation |
|---|---|
| Broad audit/history read | Decide whether audit is a server-only read API, export/report surface, device-delivered package, or another platform-owned mechanism; define authorization, pagination, retention/redaction, sensitivity handling, and proof that normal sync/access equality remains intact. |
| Emergency override write | Decide whether any bypass exists at all. If yes, define exact scope, duration, actor binding, event/projection/flag behavior, resolver interaction, audit trail, and tests proving it does not become ambient authority. |
| Audit no-local-retention or redacted view | Decide local storage, redaction, sensitivity, and device purge behavior under BAR-106/CDL-037/CDL-046 rather than relying on UI hiding or broad sync. |
| Cross-axis or dynamic auditor scope | Route to BAR-108/NW-053-style scope mechanism decision if existing `geographic`, `subject_list`, and `activity` axes cannot express the visibility. |

## Stop Or Report Concerns

No contradiction was found among the inspected routing artifact, IDRs, CDL slices, sync contract, flag catalog, module boundaries, and scenario pressure.

Implementation remains blocked for any path that would require new scope mechanisms, envelope/schema changes, IdP group/claim authority, normal sync watermark rewrites, resolver reassignment, auto-resolution, mobile authoritative rejection, auditor/audit-history APIs, report APIs, or emergency override semantics before a product/security decision exists.
