# NW-035 Agent Prompt: Evaluate Production Auth FP-011 Successor Phase

You are working in `/home/hamza/datarun-platform`.

## Goal

Record the production-auth successor design for FP-011 / BAR-104 so future
implementation work has a bounded historical reference.

This file is the accepted NW-035 design/evaluation artifact only. It does not
resolve FP-011, accept BAR-104, or authorize production authentication
implementation by itself.

Exit target for successor implementation:

```text
Production authentication maps an authenticated principal to one Datarun actor
without letting IdP groups, realm roles, client roles, or JWT claims become
direct sync scope, action authority, assignment-administration authority, or
conflict-resolution authority.
```

## Files To Read

Read only this packet by default for the first implementation slice:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-035 and any successor row created from it.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-104.
6. `docs/flagged-positions.md`
   - Read FP-011 only.
7. `docs/implementation/module-interfaces.md`
   - Read `Scope Resolver`, `Conflict Detector`, and sync-adjacent authority sections.
8. `docs/decisions/idr-016-actor-token-table.md`
9. `docs/decisions/idr-024-multi-axis-assignment-containment.md`
10. `docs/decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md`
11. CDL slices only for:
    - `CDL-006`
    - `CDL-018`
    - `CDL-030`
    - `CDL-031`
    - `CDL-032`
    - `CDL-034`
    - `CDL-035`
    - `CDL-055`
12. `contracts/sync-protocol.md`
13. Current server/mobile auth, sync, assignment, and conflict code paths named
    by the implementation task.

## Design Result

Selected model:

- Authentication proves that a production principal may act as one Datarun actor.
- The production mapping is an explicit Datarun-owned binding:

```text
(issuer, subject) -> actor_id
```

- A principal binding row is lookup/supporting state, not authority over data.
- Multiple principals may bind to the same actor if product needs account
  migration or multiple login methods.
- One active principal must not bind to multiple actors unless a later
  shared-device or actor-switching decision defines session selection.
- Effective authority remains derived from assignment events, assignment roles,
  platform scope axes, and platform resolver rules.

Rejected for the first production-auth successor:

- IdP groups, realm roles, client roles, or custom JWT claims as direct sync
  scope, work-action authority, assignment-administration authority, resolver
  authority, or root/admin authority.
- A JWT `actor_id` claim as the only source of actor identity. A claim may be
  compared to a Datarun binding later, but it must not replace the binding.
- Shared-device, multi-actor session switching, or "choose actor at login"
  behavior. That is a separate FP-011-triggered decision.
- New event envelope fields such as `user_id`, `account_id`, `group_id`, or
  equivalent identity-provider fields.

Allowed later, but not in the first implementation slice:

- IdP group/claim data may be used as provisioning or admin-convenience input
  only if a later decision defines how that input emits ordinary assignment or
  assignment-administration events before authority changes.

## Current Findings

- `/api/sync/pull`, `/api/sync/subject-history`, `/api/sync/config`,
  `/api/assignments/**`, and `/api/conflicts/**` are bearer actor-bound through
  `ActorTokenInterceptor`.
- `/api/sync/push` is not bearer-protected today; it derives the actor for
  authorization flagging from the first pushed event's `actor_ref`.
- Mobile stores an `actorToken` for bearer headers, but its local `actorId` is
  generated independently and used in new event `actor_ref`.
- `/api/assignments/**` and `/api/conflicts/**` already ignore spoofed
  request-body actor IDs and use the bearer-resolved actor context.
- `/api/actors/**` is a development token-management surface and must not become
  production admin authority through IdP groups or claims.

## Expected First Implementation Boundary

The first successor implementation should:

- Add a provider-neutral authenticated actor context shared by sync, assignment,
  conflict, and config endpoints.
- Add `GET /api/auth/me` returning the resolved `actor_id` so mobile can store a
  server-confirmed actor identity.
- Add production-auth mode configuration with current dev-token behavior as the
  compatibility default, plus an explicit production mode for validated JWT/OIDC
  principal resolution.
- Add internal binding storage for active `(issuer, subject) -> actor_id`
  mappings.
- Protect `/api/sync/push` in production-auth mode and reject any client-pushed
  human event whose `actor_ref.id` differs from the resolved actor.
- Reject client-pushed `system:*` authorship.
- Disable or development-profile `/api/actors/**` token minting outside
  dev/migration mode.
- Update `contracts/sync-protocol.md` for authenticated push and actor-ref
  mismatch behavior.
- Add `IDR-027` for the production principal-to-actor mapping decision during
  that implementation slice.

The first implementation should not require a live Keycloak deployment if a
local JWT test resolver can prove the security semantics. Provider deployment
and mobile OIDC login UX may remain a later operational slice.

## Acceptance Points

Implementation acceptance requires tests proving:

- A valid production principal with no active assignment-derived authority
  cannot pull scoped data, request subject history, create or end assignments,
  or canonically resolve conflicts merely because of groups, roles, or claims.
- A principal with admin-looking IdP groups/roles but no assignment receives no
  direct Datarun authority.
- Pushed human events are rejected when `actor_ref.id` does not match the
  authenticated actor.
- Client-pushed `system:*` authorship is rejected.
- Missing bearer, invalid token/JWT, and unmapped principal cases persist no
  pushed events.
- Existing assignment and conflict body-spoofing protections remain green.
- Mobile sends bearer auth on push and uses `/api/auth/me` to align local event
  `actor_ref` with the server-resolved actor.
- The event envelope stays at the accepted 11-field contract, with no account,
  user, group, or IdP fields.

## Expected Tests

Server-focused successor tests should include the nearest auth, sync,
assignment, and conflict integration slices, for example:

```bash
cd server
./mvnw -Dtest=SyncControllerIntegrationTest,ScopeFilteredSyncIntegrationTest,AssignmentContainmentIntegrationTest,ConflictResolutionIntegrationTest,AuthFlagIntegrationTest test
```

Add focused production-auth tests for:

- principal binding resolution;
- group/claim non-authority;
- push actor mismatch rejection;
- client `system:*` rejection;
- dev-token compatibility mode where intentionally retained.

Mobile tests should cover:

```bash
cd mobile
flutter test test/event_assembler_test.dart
flutter test test/config_store_test.dart
```

Adjust exact mobile tests to the files touched by the implementation.

## BAR And Backlog Handling

- Do not mark FP-011 resolved until the implementation and tests satisfy the
  FP-011 gate.
- Do not mark BAR-104 accepted until code/runtime evidence proves production
  principal-to-actor mapping and group/claim non-authority.
- If this design is only recorded, NW-035 may be accepted as design/evaluation
  complete while FP-011 and BAR-104 remain open/future-decision.
- If implementation starts, create or update a successor NW row rather than
  overloading this design artifact as runtime evidence.

## Forbidden Work

- Do not add envelope fields or event `type` values.
- Do not add `user_id`, `account_id`, `group_id`, or equivalent IdP fields to
  event envelopes or platform payload schemas.
- Do not treat IdP groups, realm roles, client roles, or JWT claims as direct
  authority.
- Do not add a new scope mechanism.
- Do not implement resolver reassignment or auto-resolution.
- Do not reject structurally valid offline policy anomalies unless an existing
  decision explicitly requires structural rejection.
- Do not make `/api/actors/**` a production admin authority surface via IdP
  groups or claims.

## Stop And Report

Stop and report if:

- Product requires group/claim membership to grant sync scope, work actions,
  assignment administration, conflict resolver status, or root/admin authority
  directly.
- The implementation appears to need new envelope fields, new event types, new
  scope mechanisms, shared-device actor switching, or account/group fields in
  platform payloads.
- Production push must remain unauthenticated.
- Current contracts, code, or IDRs disagree on whether push/authored
  `actor_ref` binding should be structural rejection or accept-and-flag.
- Admin/provisioning cannot be expressed without a separate assignment-admin
  authority decision.

## First Implementation Handoff Prompt

Implement NW-035A production-auth foundation only.

Read `AGENTS.md`, `docs/status.md` Current Routing,
`docs/flagged-positions.md` FP-011,
`docs/agent-working-surface/baseline-acceptance-register.md` BAR-104,
`docs/implementation/module-interfaces.md` auth/sync/conflict sections,
`docs/decisions/idr-016-actor-token-table.md`,
`docs/decisions/idr-024-multi-axis-assignment-containment.md`,
`docs/decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md`,
the CDL slices listed above, `contracts/sync-protocol.md`, and the current
server/mobile auth paths.

Goal: add `IDR-027`, introduce provider-neutral authenticated actor context,
add `/api/auth/me`, protect `/api/sync/push`, enforce pushed `actor_ref`
binding, update mobile setup/sync to use server-confirmed actor ID and bearer
push, and add negative tests proving groups/claims are not authority.

Keep OIDC provider deployment and mobile OIDC login UX out of this first slice
unless the server-side JWT resolver can be added with local test tokens and no
Keycloak dependency.

Forbidden: no envelope/schema expansion, no group/claim authority, no resolver
reassignment, no auto-resolution, no new scope mechanism, no production admin
authority through IdP groups.
