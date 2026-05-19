# Authentication And Actor Mapping Decision Brief

Status: Stakeholder brief; accepted outcome recorded in `04-accepted-pre-specification-decisions.md`; context only for current baseline assessment

Audience: stakeholders and decision reviewers. This brief explains the tradeoff; it is not the agent-facing instruction surface.

## Decision

How should authentication accounts, users, and user groups map to the platform's actor, assignment, role, and scope model during initial controlled specification work?

## Why Now

`00-baseline-validation-and-full-stack-readiness.md` classified authentication mapping as requiring a pre-specification decision or hold-back. If this is left vague, implementation or platform-spec section drafting may accidentally make user accounts, user groups, or identity-provider claims into authority sources, contradicting ADR-003's assignment-derived access model.

## Baseline Constraints

- ADR-002 separates `actor_ref` from `device_id`; device identity is not actor identity.
- ADR-003 closes access as assignment-derived and sync scope as access scope.
- ADR-003 keeps authority context projection-derived and adds no `authority_context` field in the current baseline.
- ADR-004 rejects deployer-authored arbitrary access-control logic.
- `../professional-baseline/09-identity-boundary-control.md` states Identity / Lineage does not own actor provisioning, authentication, assignment validity, role, scope, authority, or sync delivery scope.
- `../professional-baseline/12-adr008-reference-fields-assessment.md` treats `actor_ref` as the authorship field, not as device identity or envelope type.

## Options Considered

| Option | Description | Assessment |
|---|---|---|
| A. Accounts authenticate, actors author, assignments authorize | Authenticated principals map to actor references. Assignments, roles, and scopes remain the effective authority model. Groups may later help provisioning but do not directly grant event/data access. | Recommended initial decision. Preserves ADR-003 and keeps auth separate from authority. |
| B. User groups directly grant access | Groups become the primary authorization model for sync, actions, and visibility. | Not acceptable under current baseline. It risks becoming arbitrary access-control logic and bypassing assignment-derived access. |
| C. External IdP claims directly drive authority | OAuth/SAML/IdP claims become scopes, roles, or sync filters directly. | Too risky now. Useful for authentication and provisioning later, but not as direct authority without a formal decision. |
| D. No real authentication initially | Use static actor tokens or seeded actors only, deferring account model. | Possible for prototypes, but unsafe as platform-spec section drafting language because it does not define the mapping decision. |

## Product Owner View

Stakeholder benefit:

- Users need a familiar login/account experience.
- Admins need a manageable way to provision people and responsibilities.
- Deployers may expect groups because they are familiar from common admin systems.

Stakeholder harm:

- If groups are not direct authority, admin tooling must explain that groups/templates help create assignments rather than replace them.
- Some deployers may expect "put user in group = gets access" behavior.

Adoption risk:

- Moderate if the admin UX is unclear.
- Lower if user groups are framed as provisioning templates or operational teams, while assignments remain visible as the actual responsibility grants.

First deployment need:

- Reliable actor login/session mapping and actor provisioning.
- Simple assignment management.
- No full enterprise IAM required before core platform behavior is proven.

## Architecture Steward View

Baseline impact:

- Option A preserves ADR-002 and ADR-003: `actor_ref` records authorship; assignments define authority; sync scope follows access scope.
- Options B and C risk contradicting assignment-derived authority and no arbitrary access-control logic.

New invariants:

- Authenticated principal maps to one or more actor identities under controlled session rules.
- Effective authority remains derived from active assignments, roles, scopes, and platform-fixed containment semantics.

Coupling risk:

- High if user groups become a second authority system.
- High if IdP claims directly alter sync scope without assignment events or projection.
- Medium if shared-device sessions are included prematurely.

Reversibility:

- Option A is reversible toward richer authentication and IdP integration.
- Direct group/claim authority is hard to unwind once data access and audit semantics depend on it.

## Delivery Lead View

Implementation difficulty:

- Option A supports a small implementation path: account/session authenticates, account resolves to actor, sync uses actor assignments.
- Full group/IdP authority would require broad policy engine and security testing.

Build order:

1. Define account-to-actor mapping decision.
2. Implement simple actor authentication or session model.
3. Keep assignment management as the source of effective access.
4. Add groups later as provisioning templates if needed.

Testing burden:

- Need tests that authenticated actor cannot sync beyond assignment-derived scope.
- Need tests that group membership alone does not grant data access.
- Need tests that device identity does not substitute for actor identity.

Operational complexity:

- Accounts, actors, assignments, and devices must be explained as separate concepts.
- Admin UX can hide some complexity but must not erase the underlying distinction.

## Challenge Reviewer View

Hidden assumptions:

- One account may map to one actor initially, but future shared devices, service accounts, contractors, or role transitions may need more nuance.
- Groups might mean operational teams, permission templates, IdP groups, reporting cohorts, or assignment batches. These are not the same.

Worst-case failure:

- A group change silently changes historical authority interpretation.
- Sync scope uses group membership instead of assignment projection.
- A device token is treated as actor identity, corrupting authorship and accountability.

What can be deferred:

- Enterprise SSO.
- IdP group sync.
- User-group provisioning templates.
- Shared-device multi-actor session model.
- Account recovery and advanced credential lifecycle.

What needs stakeholder or domain input:

- Whether first deployments expect shared devices.
- Whether first deployments require external identity providers.
- Whether admin staff think in terms of teams, roles, locations, activities, or groups.

## Proposed Decision

Authentication proves that a principal may act as an actor. It does not define what the actor may see or do. Effective authority remains assignment-derived through roles, scopes, activity/context, and platform-fixed containment semantics. User groups, if introduced, are provisioning or admin-convenience structures only; they do not directly grant sync scope or action authority.

Outcome:

- Accepted as `PREOP-002` in `04-accepted-pre-specification-decisions.md`, with groups framed as later operational teams or provisioning templates if needed.

## Explicit Deferrals

- full user account schema
- external IdP integration
- IdP group synchronization
- user groups as provisioning templates
- shared-device multi-actor session semantics
- account recovery and credential lifecycle
- service-account conventions beyond existing system actor conventions

## Reopen Trigger

Reopen before implementation if the first deployment requires shared devices, external identity provider integration, or group-managed authorization.

Reopen before platform-spec drafting authorization/sync if a proposed spec would let account, group, or IdP claims directly grant event access without assignment-derived authority.

## Required Follow-Up

- Carry authentication/account-to-actor mapping as an explicit hold-back in the platform-spec section drafting plan.
- Do not add `user_id` or `group_id` to the event envelope without formal change control.
- Do not make user groups authority sources in final authorization/sync sections.
- When implementation planning starts, define a small account/session-to-actor mechanism that feeds the existing Assignment / Authority / Sync boundary.
