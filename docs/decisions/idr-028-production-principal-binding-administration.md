---
id: idr-028
title: Production principal-binding administration
status: active
date: 2026-06-04
phase: post-phase-4-stabilization
type: decision
reversal-cost: medium
touches: [server/authorization, server/admin, deployment]
superseded-by: ~
evolves: IDR-027
commit: ~
tags: [authorization, authentication, production-auth, provisioning, fp-011]
---

# Production Principal-Binding Administration

## Context

IDR-027 decided that production authentication resolves a validated provider
principal through an explicit Datarun-owned binding:

```text
(issuer, subject) -> actor_id
```

NW-038 added the real OIDC/JWKS provider-validation boundary behind
`AuthenticatedActorResolver`, but the production path for creating, rotating,
deactivating, auditing, and bootstrapping those bindings remained open. FP-011
continues to block production-auth acceptance until principal mapping is
operationally defined and IdP groups, roles, and token claims remain
non-authority.

## Decision

The first production administration path for auth principal bindings is
deployment-managed binding provisioning.

The deployment supplies a deterministic binding manifest, and an explicit
server-side provisioning runner applies that manifest to Datarun-owned binding
storage. The runner may be invoked by the deployment pipeline, release
automation, or an operator-run server command. It is not an online product admin
API, it is not the development `/api/actors/**` token surface, and it is not
driven by identity-provider groups or claims as direct platform authority.

## Binding Manifest

The manifest records desired principal-binding operations. Each entry identifies
at minimum:

- `issuer`
- `subject`
- `actor_id`
- desired state: active or inactive
- operation reason
- stable operation id or manifest version

Implementation may choose the concrete file format in NW-040, but it must be
deterministic, reviewable in the deployment change process, and safe to reapply.

## Bootstrap

Initial production bootstrap is a manifest application.

The first manifest binds known provider principals to existing platform actor
UUIDs. The binding only proves which actor the principal may authenticate as.
It does not create assignment scope, assignment-administration authority,
conflict resolver authority, or any new actor authority. Those remain governed
by assignment events and platform resolver rules.

If a deployment has no active binding for a valid provider principal, production
auth rejects the request as `unmapped_principal`.

## Create, Rotate, Deactivate, And Rebind

Create:

- If no active row exists for `(issuer, subject)`, applying an active manifest
  entry creates an active binding to `actor_id`.
- Reapplying the same desired active binding is idempotent.

Rotate:

- Provider subject rotation is modeled by adding a new active
  `(issuer, new subject) -> actor_id` binding.
- The old principal may remain active during a planned overlap window or be
  deactivated in the same manifest.
- Multiple active principals may map to the same actor.

Deactivate:

- Deactivation makes a principal unusable for future authentication.
- Deactivation does not delete historical binding rows or mutate historical
  events authored while the binding was active.

Rebind:

- Rebinding the same active `(issuer, subject)` to a different `actor_id` is an
  explicit operation requiring a reason.
- Rebinding deactivates the previous active mapping and creates the new desired
  active mapping.
- At any instant, one active principal maps to at most one actor.

Correction:

- A wrong binding is corrected by deactivating the wrong binding and applying
  the correct binding in a later operation.
- Events already persisted with the previously resolved `actor_ref` are not
  rewritten. Any unauthorized work caused by the wrong binding is handled by
  existing audit, flag, and operational response paths rather than envelope
  mutation.

## Audit And Storage

Binding administration must be auditable as an append-only operation history.
NW-040 may implement this as a separate binding-operation table plus active
support rows, or as an equivalent schema that preserves every operation without
hard deletes.

Each applied operation must retain:

- issuer, subject, target actor, and desired state
- operation id or manifest version
- manifest source identity and content hash
- applied timestamp
- applied-by deployment actor or system identity
- reason
- previous active binding id and previous actor, when applicable

Current `auth_principal_bindings` rows are supporting lookup state. The
production administration authority is the audited operation history plus the
current active binding projection, not mutable unaudited SQL updates.

## Idempotency And Concurrency

The provisioning runner must validate the manifest before applying it.

Validation rejects duplicate desired active mappings for the same
`(issuer, subject)`, malformed actor UUIDs, missing reasons, and ambiguous
operations. Application runs in one transaction and serializes concurrent
applications using a database lock or equivalent repository-level guard.

Reapplying the same manifest produces the same active state without duplicating
effective changes. A later manifest with a new operation id may intentionally
change state.

## Non-Authority Boundary

Principal binding is authentication lookup only. It is not:

- assignment scope
- work-action authority
- assignment-administration authority
- conflict resolver designation
- root or deployment admin authority
- event authorship beyond selecting the single authenticated actor used by
  existing actor-bound endpoints

JWT `actor_id`, groups, realm roles, client roles, resource claims, and custom
IdP claims remain non-authority. A future decision may use IdP group or claim
data as provisioning input only if that decision defines how the input is
converted into ordinary Datarun assignment or administration facts before it
affects authority.

## Rejected Alternatives

- Manual production SQL updates as the accepted administration path. Manual SQL
  is not deterministic, repeatable, or sufficiently auditable for production
  binding operations.
- Online production admin API or UI for binding administration in this slice.
  That requires a separate admin-authority model and audit semantics.
- Keycloak/OIDC groups, roles, or claims as direct binding or platform authority.
- JWT `actor_id` as the source of actor mapping without an explicit binding.
- Extending the event envelope with account, user, group, or provider fields.

## Consequences

- NW-040 is unblocked as an implementation slice for deployment-managed binding
  provisioning, manifest validation, audit history, active binding projection,
  and tests.
- FP-011 remains open until NW-040 proves create, rotate, deactivate, audit,
  idempotency, concurrency, and continued group/claim non-authority.
- After NW-040 lands, FP-011 may be closed for production provider integration
  and principal-binding administration if the test gate is satisfied. Any future
  group/claim authority model remains a separate FP-011 successor decision.
- BAR-104 remains `future_decision` until runtime evidence proves the full
  production auth gate.

## Guards

NW-040 must add focused tests proving:

- manifest create, rotate, deactivate, and rebind behavior
- idempotent reapplication
- rejected duplicate or ambiguous manifest entries
- serialized/conflict-safe application
- audit metadata retention
- active binding lookup still resolves only explicit active bindings
- groups, roles, JWT `actor_id`, and IdP claims do not grant sync scope, action
  authority, assignment-administration authority, resolver authority, or binding
  administration authority
- `/api/actors/**` remains development-token administration only

## Traces

- FP-011: Authentication principal-to-actor mapping and group non-authority.
- BAR-104: production OIDC/JWT/Keycloak authority.
- NW-039: decision slice.
- NW-040: selected implementation slice.
- IDR-027: production auth principal-to-actor binding.
- CDL-018, CDL-030, CDL-031, CDL-032, CDL-034, CDL-035.
