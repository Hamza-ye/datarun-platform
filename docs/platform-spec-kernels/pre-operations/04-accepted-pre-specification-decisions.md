# Accepted Pre-Platform specification section drafting Decisions

Status: Accepted decision record

Audience: platform-spec section drafting planning and implementation agents.

Use this file as the current pre-specification decision record. The briefs are stakeholder rationale and do not override this record.

## Active Decisions

| ID | Topic | Decision | Hold Back |
|---|---|---|---|
| PREOP-001 | Deployment and tenancy | Initial platform-spec section drafting assumes one deployment context per runtime/database/configuration namespace. Deployment or tenant context stays outside the event envelope. | Cloud multi-tenancy, cross-tenant administration, tenant migration, shared-runtime hosting, deployment packaging UX, data residency mechanics |
| PREOP-002 | Authentication and actor mapping | Authentication proves that a principal may act as an actor. Assignments, roles, scopes, activity/context, and platform-fixed containment semantics remain the source of effective authority. | Full account schema, external IdP integration, IdP group sync, operational-team/provisioning-template schema, shared-device sessions, account recovery, advanced credential lifecycle |

## Agent Instructions

- Carry PREOP-001 and PREOP-002 into platform-spec section drafting planning.
- Do not add `tenant_id`, `deployment_id`, `user_id`, or `group_id` to the event envelope without formal change control.
- Do not make account, group, identity-provider claim, or tenant fields direct authority sources.
- Treat user groups, if introduced later, as operational teams or provisioning templates only.
- Treat the stakeholder briefs as rationale only; this file is the accepted decision surface.

## Decision Details

### PREOP-001: Deployment And Tenancy Decision

Status: Accepted

Accepted date: 2026-05-06

Source brief: `02-deployment-tenancy-decision-brief.md`

Platform specification section drafting impact:

- Event-envelope sections must preserve the current envelope and reject tenant/deployment fields unless formal change control reopens the envelope.
- Storage, sync, configuration, reporting, and deployment planning must carry a hold-back for later cloud/shared-runtime tenancy.
- Any future deployment/ops section must state that tenant isolation is not yet a closed platform mechanism.

Reopen before implementation if the first target product must support multiple independent organizations inside one shared runtime or database.

Reopen before deployment/ops platform-spec section drafting if self-host packaging, data residency, or managed cloud operations require a stronger deployment identity contract.

### PREOP-002: Authentication And Actor Mapping Decision

Status: Accepted

Accepted date: 2026-05-06

Source brief: `03-authentication-actor-mapping-decision-brief.md`

Platform specification section drafting impact:

- Authorization/sync sections must preserve assignment-derived access and sync scope as access scope.
- Event-envelope sections must preserve `actor_ref` as authorship and `device_id` as device/app-installation identity.
- Admin/configuration sections may later discuss groups as provisioning convenience, but must not make groups authority sources.

Reopen before implementation if the first deployment requires shared devices, external identity-provider integration, or group-managed authorization.

Reopen before authorization/sync platform-spec section drafting if a proposed spec would let account, group, or identity-provider claims directly grant event access without assignment-derived authority.
