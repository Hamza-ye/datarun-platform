# Access Exceptions And Shared-Device Scope Exploration

Status: accepted exploration
Date: 2026-06-05
Authority: none. This is routing analysis only. CDL, BAR, contracts, IDRs, and code remain the authority surfaces.

## Executive Recommendation

Keep NW-050 settled. Assignment create/end command authority remains `assignment_admin.create` and `assignment_admin.end`, evaluated server-side from `assignment_admin_capabilities`; one active assignment must both grant the command and contain the requested create/end scope. Do not reopen assignment-admin command capability for access exceptions.

Do not implement a broad "access exception" feature. Split the pressure into separate decision families:

| Family | Recommendation |
|---|---|
| Auditor/special read visibility | Current-scope read visibility can be modeled as ordinary assignments when it fits `geographic`, `subject_list`, and `activity` axes. Broad cross-boundary audit, historical reconstruction, redaction, or special non-retention behavior needs a successor product/security decision. |
| Emergency/special write authority | Ordinary temporary write authority should use time-bounded assignments plus role-action permissions. Any bypass of assignment scope, role-action, resolver equality, or accept-and-flag needs a successor security/platform decision. Assignment-admin command capability is insufficient because it only authorizes assignment create/end, not domain work. |
| Shared-device actor sessions | Route as authentication/session lifecycle plus local data-retention partitioning. Events must remain authored by the currently authenticated actor. Do not treat shared device as a scope mechanism. |
| Grace behavior | Keep authority unchanged. Existing stale/offline work is accepted and flagged. Product UX may warn or explain grace, but it must not silently authorize stale work, rewrite live-sync watermarks, or make mobile authoritative rejection the rule. |
| Subject/query/custom scope | Existing `subject_list` is sufficient for explicit named subject sets. Dynamic cohorts, query scopes, relationship scopes, or custom containment cross BAR-108 and need a platform-owned scope decision. |
| Device expiry and retained local data | Route as retention/security under CDL-037, CDL-046, and BAR-106. Do not mutate server event history, do not rely on UI hiding for sensitive data, and do not turn expiry into live-sync authority. |
| IdP group/claim authority | Keep rejected as direct authority. Future use as provisioning input would need a separate production-auth decision and conversion into Datarun-owned facts before it affects access. |

Nothing in this exploration is a current production blocker for the accepted baseline after NW-050. Several items can become blockers for a specific product launch or deployment posture: broad auditor history, shared physical devices, strict local-data expiry, emergency override writes, and dynamic/query scope. Those need successor decisions before implementation.

## Current Baseline After NW-050

| Area | Current baseline |
|---|---|
| Production principal-to-actor binding | BAR-104 is accepted. Valid provider credentials resolve only through explicit active `(issuer, subject) -> actor_id` bindings. Groups, roles, resource claims, JWT `actor_id`, and custom claims are not sync, assignment, resolver, or admin authority. |
| Assignment-admin command capability | IDR-029/NW-050 selected and implemented `assignment_admin.create` and `assignment_admin.end` outside `activities[*].roles`. The server filters active assignments by command policy before IDR-024 containment. Absent policy denies ordinary assignment-admin commands. |
| Fixed assignment scope mechanisms | CDL-055 and IDR-024 keep platform-fixed `geographic`, `subject_list`, and `activity` axes. Composition is AND within one assignment, OR across assignments for access, and command-plus-containment must come from one active assignment for assignment admin. |
| Live sync and subject-history split | CDL-031 keeps normal sync equal to current access scope. `/api/sync/subject-history` is separate, subject/activity-bound, request-time authorized on every page, and does not mutate normal device watermarks. |
| Accept-and-flag stale/offline behavior | CDL-035 and S19 evidence preserve structurally valid stale/offline work by accepting it, flagging `temporal_authority_expired` or `role_stale` as applicable, and keeping unresolved flagged events out of authoritative state/policy participation. |
| Exact designated-resolver equality | IDR-026 runtime enforcement requires a `conflict_resolved/v1` actor to exactly match the flag's `designated_resolver`. Non-designated resolution is persisted for audit, does not clear the flag, and is itself flagged. Resolver reassignment and auto-resolution remain separate future decisions. |
| Mobile actor and retention model | Mobile stores one server-resolved actor id/token, refreshes `/api/auth/me`, and purges out-of-scope other-device events only through current selective-retention behavior. There is no shared-device session switch, per-actor local store partition, or product-grade expiry model. |

## Pressure Taxonomy

| Pressure | Domain need | Current support | Authority/sync/projection risk | Touches | Recommended route |
|---|---|---|---|---|---|
| Auditor/special read access | Inspect work across normal hierarchy, sometimes across regions or older history. | Ordinary assignments can grant current-scope visibility within fixed axes. Subject-history exists only for subject/activity repair after current assignment. | Broad auditor sync can place excess data on devices; historical audit can turn live sync into audit pull; read-only roles do not hard-prevent rooted/offline write attempts, they only allow server flags/exclusion. | BAR-106 if redaction/retention; BAR-108 if new scope; normal sync authority; no envelope change. | NW-051 special read/write access decision. Start by deciding if current-scope assignment visibility is enough or if a platform-owned auditor/audit-history mechanism is needed. |
| Emergency/special write access | Allow exceptional domain work under pressure. | Time-bounded assignments plus role-action permissions can model ordinary temporary authority. Stale offline work is accepted and flagged. | A bypass can undermine assignment-derived authority, role-action checks, resolver equality, and audit reconstruction. | BAR-108 if new scope/override; resolver reassignment/auto-resolution if it clears flags; no assignment-admin reopening. | NW-051. Treat emergency write as separate from read visibility. If it bypasses current assignment/role semantics, require a security/platform decision before implementation. |
| Shared-device actor sessions | Multiple people use one physical device without cross-authoring or leaking data. | Production auth binds one bearer credential to one actor. Mobile has a single stored actor id/token and local watermark. | Actor switching can misattribute events, combine local datasets across users, leak prior actor data, or corrupt watermarks if not partitioned. | BAR-104 auth boundary; CDL-031 sync; CDL-037 retention; no new scope. | NW-052 shared-device session lifecycle decision. Split auth/session switching from local data partition and purge semantics. |
| Grace-period behavior | Let users finish work around transfers, leave, or stale offline windows. | Assignment `valid_to` and end events define authority windows; stale/offline work is accepted and flagged. UX warnings can be advisory. | Silent grace changes authority; mobile rejection loses evidence; watermark rewrites break live-sync/subject-history split. | CDL-035; normal sync watermarks; mobile authoritative rejection guardrail. | Defer runtime authority changes. Grace UX/advisory may be product work after NW-047 if it does not change authority, sync, or rejection. |
| Subject-scope variants | Assign responsibility to explicit sets or handoff subjects without geography. | `subject_list` covers explicit named subjects; subject-history backfill supports current assigned subject/activity history. | Relationship/dynamic cohorts can become hidden query scopes and leak data when membership changes. | BAR-108; CDL-055; possible retention implications. | NW-053 subject/query/custom scope decision only if explicit subject lists are insufficient. |
| Query/custom scope pressure | Grant access by arbitrary filters, cohorts, attributes, or deployment logic. | Not supported as a scope mechanism. Deployer config can define shapes/activities, not scope logic. | Crosses mechanism/instance boundary; config-as-code or deployer containment scripts are data-leak risks. | BAR-108; config anti-pattern guardrails; possibly contracts/config if packaged. | NW-053. Require platform-owned scope semantics, deterministic containment, and security tests before any implementation. |
| Device data expiry or retained local data | Remove or partition local data after scope contraction, device handoff, worker exit, or sensitivity deadlines. | Selective retention keeps own-device events, purges provably out-of-scope other-device events for subject-list scopes, and preserves system/assignment/identity/integrity facts. | Hide-only is not security; expiry can corrupt sync/projection if it mutates watermarks or purges needed local authority facts. | BAR-106; CDL-037; CDL-046; live sync/subject-history split. | NW-054 retention/security decision. Treat as local retention/security, not live-sync authority or server event deletion. |
| IdP group/claim authority requests | Use provider groups/roles/claims for access, admin, resolver, or shared-device authority. | Explicitly rejected as direct authority by IDR-027/028 and BAR-104. | Creates a second authority plane outside assignment projections and resolver rules. | BAR-104 successor only; no BAR-108 unless converted to scope facts. | Keep non-authority. Future use can only be provisioning input routed through a production-auth successor decision. |

## Path Comparison

| Path | Benefit | Cost/risk | Fit | Recommendation |
|---|---|---|---|---|
| Keep current model and defer | Preserves accepted baseline, assignment-derived access, sync/access equality, and NW-050. | Does not satisfy broad audit, shared-device, dynamic scope, or strict retention product needs. | Best default until a deployment names a concrete need. | Keep as current baseline. |
| Model read-only auditor visibility as normal assignments | No new scope mechanism; uses existing sync/access boundary; can be proven with current containment. | Only covers current-scope visibility. Server still accepts structurally valid unauthorized writes and flags them; it is not a hard no-write channel on compromised clients. | Good for simple scoped read visibility. | Allowed as configuration/product posture if within fixed axes; no runtime platform change from this slice. |
| Create platform-owned special-access mechanism | Could support audit-only, cross-boundary, or emergency semantics explicitly. | High security and contract risk; may affect sync delivery, retention, resolver visibility, and projection participation. | Only if ordinary assignments are insufficient. | Route through NW-051, not implementation. |
| Introduce shared-device session switching without changing scope authority | Preserves actor-bound events and assignment-derived scope while addressing real physical-device workflows. | Needs session lifecycle, token storage, local store partition or purge, per-actor watermark treatment, and offline edge decisions. | Likely needed for shared-device deployments. | Route through NW-052. |
| Add grace UX/advisory only | Helps users understand stale/offline or transfer windows without changing authority. | Product copy can drift into authority if not guarded. | Safe if companion-only and advisory. | Can be future UX work under NW-047; no server/mobile rejection or sync changes. |
| Promote new subject/query/custom scope | Can address dynamic cohorts or complex organizational access. | Security-sensitive platform mechanism; deployer-defined logic risks data leaks and config-as-code. | Only with measured pressure beyond `subject_list`. | Route through NW-053 and BAR-108. |
| Handle device data expiry as retention/security | Separates local data handling from canonical event history and live sync. | Needs careful crash-safe local-store, sensitivity, and audit tradeoffs. | Correct route for S24-style pressure. | Route through NW-054 and BAR-106. |

## Questions Answered

| Question | Answer |
|---|---|
| Which access-exception pressures are production blockers, and which are product evolution? | None block the accepted baseline after NW-050. Deployment-specific blockers are broad audit/history access, shared physical devices, strict device-data expiry, emergency override writes, and dynamic/query scope. Product evolution items are simple scoped auditor views, grace advisory UX, and subject-list ergonomics when current mechanisms suffice. |
| Can auditor/special read visibility be modeled with existing assignments, or does it require a new platform-owned mechanism? | Current-scope read visibility can be modeled with existing assignments when the auditor's visibility fits `geographic`, `subject_list`, and `activity`. Broad historical audit, cross-boundary special access, no-local-retention views, redaction, or dynamic access needs a platform-owned successor mechanism or explicit deferral. |
| Does any access exception require write authority, and why is assignment-admin command capability insufficient? | Emergency/special domain work may require write authority. If it is ordinary temporary work, use assignments plus role-action permissions. Assignment-admin command capability is insufficient because it only controls assignment create/end; it does not authorize `capture`, `review`, `alert`, `task_created`, `task_completed`, resolver reassignment, or conflict resolution. |
| Is shared-device support authentication/session, device-retention, scope, or a combination? | It is a combination of authentication/session lifecycle and device-retention/local-store partitioning. It must not become scope authority. Each event must remain bound to the currently authenticated actor, and prior actors' data must not leak through shared local storage. |
| Should grace behavior change authority, or remain accept-and-flag plus advisory UX? | It should remain accept-and-flag plus advisory UX unless a successor security/platform decision explicitly changes authority. Do not silently authorize stale work, rewrite normal watermarks, or make mobile authoritative rejection. |
| Are subject-scope variants sufficient under existing `subject_list`, or is there pressure for a new scope mechanism? | Explicit named subject sets are sufficient under `subject_list`. Dynamic subject cohorts, relationship-derived subjects, query filters, or computed membership are real BAR-108 pressure and need a new platform-owned scope decision if promoted. |
| Do query/custom scopes cross the mechanism/instance boundary or create config-as-code risk? | Yes. Any deployer-defined containment query/script crosses CDL-055 and the configuration anti-pattern guardrails. It must be routed as a platform-owned mechanism, not as ordinary deployment config. |
| How should device data expiry and retained local data route relative to BAR-106, live sync, and subject-history? | Treat expiry as retention/security under CDL-037, CDL-046, and BAR-106. It must not delete canonical server events, mutate normal live-sync watermarks, or expand subject-history into audit pull. |
| What tests or probes would prove each recommended successor without overbuilding it? | See successor test table below. Start with focused boundary tests around current assignments, actor binding, pull visibility, subject-history isolation, local retention, and negative guardrails before adding new runtime behavior. |

## Successor Tests Or Probes

| Successor | Minimal proof to require before implementation acceptance |
|---|---|
| NW-051 special read/write access | Prove scoped auditor assignment can pull only authorized current data; prove no-write role attempts are accepted-and-flagged and excluded from state; prove broad audit/history is not normal pull; prove emergency write does not bypass role/action/scope/resolver rules unless explicitly decided. |
| NW-052 shared-device sessions | Prove actor switch refreshes `/api/auth/me`; unpushed events cannot be authored by a prior actor; local data is partitioned or purged before the next actor sees it; watermarks cannot leak or skip data across actors; IdP groups/claims remain non-authority. |
| NW-053 subject/query/custom scope | Prove existing `subject_list` handles explicit sets; if a new mechanism is selected, prove deterministic containment, no deployer scripts, no query-as-config authority, no normal sync data leak, and assignment-admin containment across the new mechanism. |
| NW-054 device retention/security | Prove scope contraction does not delete canonical server history; local purge is crash-safe and preserves own provenance and required authority facts; sensitive out-of-scope data is not merely hidden; subject-history and normal sync cursors remain independent. |

## Successor Routing

NW-049 should be marked accepted. It does not authorize implementation. Add these successor decision rows/prompts:

| ID | Title | Suggested status | Purpose |
|---|---|---|---|
| NW-051 | Decide special read/write access boundary | `future_decision` | Decide whether auditor/current read visibility remains ordinary assignments, whether a platform-owned special-access/audit-history mechanism is needed, and whether emergency writes are ordinary temporary assignments or a separate override decision. |
| NW-052 | Decide shared-device session lifecycle | `future_decision` | Decide session switching, authenticated actor refresh, local data partition/purge, and per-actor watermark handling without changing scope authority. |
| NW-053 | Decide subject/query/custom scope boundary | `future_decision` | Decide whether current `subject_list` is sufficient or whether a BAR-108 platform-owned scope mechanism should be designed. |
| NW-054 | Decide device data expiry and retained local data boundary | `future_decision` | Decide retention/security behavior beyond current selective retention, including sensitivity and local expiry, without changing live sync or server event history. |

## Stop Or Report Concerns

No contradiction was found among the current CDL slices, IDRs, BAR rows, contracts, or inspected code on scope, sync, production-auth, assignment-admin authority, or resolver equality.

Implementation remains blocked for any path that would require new envelope fields/types, IdP group/claim direct authority, new scope mechanisms, normal live-sync watermark rewrites, resolver reassignment, auto-resolution, mobile authoritative rejection, or device retention/security behavior beyond current BAR-106/BAR-108 authority.
