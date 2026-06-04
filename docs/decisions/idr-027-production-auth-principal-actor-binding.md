---
id: idr-027
title: Production auth principal-to-actor binding
status: active
date: 2026-06-04
phase: post-phase-4-stabilization
type: decision
reversal-cost: high
touches: [server/authorization, server/sync, server/integrity, mobile/data, contracts]
superseded-by: ~
evolves: IDR-016, IDR-024, IDR-026
commit: ~
tags: [authorization, authentication, sync, production-auth, fp-011]
---

# Production Auth Principal-To-Actor Binding

## Context

IDR-016 introduced the Phase 2 actor-token table as a development mechanism for
resolving `Authorization: Bearer <token>` to an `actor_id`. That mechanism
unblocked scope-filtered sync, but it was not the production auth decision.

FP-011 records the unresolved production-auth risk: when a production identity
provider is introduced, identity-provider principals, groups, roles, and token
claims must not become direct platform authority. CDL-030 through CDL-035 keep
authority assignment-derived, and CDL-032 prohibits canonical authority claims in
the event envelope. CDL-018 records event authorship, not authorization
eligibility.

The successor implementation needs a production-facing auth boundary without
adding new envelope fields, without treating Keycloak/OIDC groups as authority,
and without replacing event/assignment-derived scope and resolver authority.

## Decision

Production auth resolves a validated authentication principal through an explicit
binding:

```text
(issuer, subject) -> actor_id
```

The binding is stored as supporting identity lookup state. It is not an
assignment, role, scope, resolver grant, or event authority fact.

The server exposes an authenticated actor context for actor-scoped endpoints.
Current modes are:

| Mode | Credential | Actor resolution |
|------|------------|------------------|
| `dev-token` | IDR-016 static bearer token | `actor_tokens.token -> actor_id` |
| `jwt` | locally validated HS256 JWT for NW-037 testability | `(iss, sub) -> actor_id` through `auth_principal_bindings` |
| `oidc-jwks` | asymmetric provider JWT validated by configured issuer, audience, and JWKS URI | `(iss, sub) -> actor_id` through `auth_principal_bindings` |

The local HS256 validator exists only to prove the server-side boundary without
requiring a live Keycloak/JWKS deployment in the NW-037 slice. NW-038 adds the
real OIDC/JWKS provider-validation mode behind the same authenticated-actor
resolver boundary.

## Binding Rules

- `iss` and `sub` identify the authentication principal.
- An active `auth_principal_bindings` row maps that principal to exactly one
  platform `actor_id`.
- Groups, roles, realm/resource access claims, and JWT `actor_id` claims do not
  grant platform authority.
- Platform access remains assignment-derived through Scope Resolver.
- Conflict resolution remains exact designated-resolver equality from IDR-026.
- Assignment create/end containment remains IDR-024 scope containment.

## Sync Push Authorship

`/api/sync/push` is bearer-authenticated in production auth mode. When a bearer
credential resolves to an actor, every client-pushed human-authored event in the
batch must have `actor_ref.id` equal to that actor UUID.

The server rejects the batch before persistence when any event:

- omits `actor_ref.id`;
- uses client-authored `system:*` authorship;
- uses a non-UUID human actor id;
- uses a UUID different from the authenticated actor.

This preserves CDL-018 authorship without allowing the device to forge another
human or system author. Server-generated events keep using platform-owned
system actors.

## Mobile Boundary

Mobile setup and sync must ask the server for `/api/auth/me` and store the
returned `actor_id` before assembling future events. Sync includes the bearer
credential on push, pull, and config requests.

This does not make mobile authoritative. It only aligns local event authorship
with the server-resolved actor so the server-side binding check can reject drift.

## Rejected Alternatives

- Direct group, role, or claim authority from the identity provider.
- Direct JWT `actor_id` authority without an explicit binding row.
- Device/session switching that allows a single local store to alternate actors
  without a separate shared-device design.
- New envelope authority fields or device-authored authority context.

## Consequences

- FP-011 is not fully resolved by this foundation because operational binding
  administration/provisioning remains successor work, and any group/claim
  authority model still requires a separate decision.
- BAR-104 must remain future-decision until implementation and tests cover the
  chosen production identity-provider deployment path.
- Dev-token mode remains available for local development and existing tests.
- Production JWT/OIDC modes prove that a valid credential is necessary but not
  sufficient for access; assignment history still decides visibility and action
  authority.

## Guards

- `ProductionAuthIntegrationTest` proves OIDC/JWKS mapped principal resolution,
  missing/malformed/bad-signature/unknown-kid/wrong-issuer/wrong-audience/
  expired/not-yet-valid/unsupported-algorithm rejection before push
  persistence, group and claim non-authority, authenticated push requirement,
  actor-ref binding, and resolver non-authority from claims.
- `LocalJwtAuthCompatibilityIntegrationTest` proves the NW-037 local HS256 JWT
  mode still resolves explicit principal bindings and rejects unmapped push
  before persistence.
- Mobile sync tests prove `/api/auth/me` actor refresh and bearer-authenticated
  push.
- Event assembler tests prove assembled events use the locally stored server
  actor id.

## Traces

- FP-011: Authentication principal-to-actor mapping and group non-authority.
- BAR-104: production auth acceptance remains open.
- NW-035: design/evaluation record.
- NW-037: bounded implementation slice.
- NW-038: OIDC/JWKS auth-provider boundary.
- CDL-006, CDL-018, CDL-030, CDL-031, CDL-032, CDL-034, CDL-035, CDL-055.
- IDR-016, IDR-024, IDR-026.
