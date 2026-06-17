# Production Auth Principal Binding

Status: accepted
Document type: platform_spec
Owner: auth/security verifier
Source: NW-070 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-070-extract-production-auth-principal-binding-durable-behavior.md`
Authority: BAR-003, BAR-006, BAR-007, BAR-104, and BAR-108; `contracts/sync-protocol.md`; IDR-016, IDR-027, and IDR-028 as historical decision inputs; production deployment policy/runbook/rehearsal evidence as operational inputs; implementation evidence in `docs/implementation/module-interfaces.md`
Last reviewed: 2026-06-18
Supersedes: none
Related: `contracts/sync-protocol.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/configuration-package-and-shapes.md`; `docs/implementation/module-interfaces.md`; `docs/agent-working-surface/artifacts/architecture-classification-drift-audit.md`; `docs/agent-working-surface/artifacts/idr-durable-surface-routing-audit.md`; `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`; `docs/decisions/idr-016-actor-token-table.md`; `docs/decisions/idr-027-production-auth-principal-actor-binding.md`; `docs/decisions/idr-028-production-principal-binding-administration.md`; `docs/operations/policies/first-reference-deployment-policy.md`; `docs/operations/runbooks/production-deployment-runbook.md`; `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`; `docs/operations/rehearsals/2026-06-17-keycloak-jwks-rotation-adapter.md`; `server/src/main/java/dev/datarun/server/authorization/AuthenticatedActorResolver.java`; `server/src/main/java/dev/datarun/server/authorization/AuthPrincipalBindingRepository.java`; `server/src/main/java/dev/datarun/server/authorization/OidcJwksTokenValidator.java`; `server/src/main/java/dev/datarun/server/authorization/PrincipalBindingManifestProvisioner.java`; `server/src/main/java/dev/datarun/server/authorization/ActorTokenInterceptor.java`; `server/src/main/java/dev/datarun/server/authorization/AuthMeController.java`; `server/src/main/java/dev/datarun/server/sync/SyncController.java`; `server/src/test/java/dev/datarun/server/authorization/ProductionAuthIntegrationTest.java`; `server/src/test/java/dev/datarun/server/authorization/LocalJwtAuthCompatibilityIntegrationTest.java`; `server/src/test/java/dev/datarun/server/authorization/ScopeFilteredSyncIntegrationTest.java`; `server/src/test/java/dev/datarun/server/authorization/AssignmentContainmentIntegrationTest.java`; `server/src/test/java/dev/datarun/server/integrity/ConflictResolutionIntegrationTest.java`; `mobile/lib/data/device_identity.dart`; `mobile/lib/data/event_assembler.dart`; `mobile/lib/data/sync_service.dart`; `mobile/test/event_assembler_test.dart`; `mobile/test/sync_service_test.dart`

## Purpose

This specification records accepted platform/security behavior for production
authentication, authenticated actor context, explicit principal-to-actor
binding, bearer-bound sync/API actor resolution, OIDC/JWKS validation,
deployment-managed principal-binding provisioning, and group/claim
non-authority.

It extracts durable behavior from accepted IDR-era inputs, BAR/NW evidence,
contracts, operations surfaces, module boundaries, and implementation tests.
It does not change runtime behavior, schemas, protocol shape, event envelope
fields, scope mechanisms, assignment authority, resolver authority, operations
policy, runbooks, or rehearsal standing.

## Contract And Trace Decision

The existing sync contract owns the process-boundary API language for sync and
config surfaces:

| Surface | Contract-owned content |
|---|---|
| `contracts/sync-protocol.md` push | Bearer requirement in production-auth mode, development unauthenticated-push compatibility note, push request/response shape, actor-binding error vocabulary, watermarks, and persistence semantics. |
| `contracts/sync-protocol.md` pull | Bearer requirement, actor-scoped pull shape, `device_id` and `config_version` bookkeeping, pagination, and error vocabulary. |
| `contracts/sync-protocol.md` config | Bearer requirement, `ETag` and `If-None-Match`, `200`/`304` behavior, and package-body contract link. |
| `contracts/sync-protocol.md` subject-history | Bearer requirement, independent cursor, per-page authorization, live-sync isolation, and subject-bound audit limits. |

`contracts/sync-protocol.md` already carries enough process-boundary auth
language for push, pull, config, and subject-history. NW-070 does not require a
sync protocol change. This platform spec owns the accepted prose boundary that
the contract does not fully express: production auth modes, principal binding
semantics, OIDC/JWKS validation scope, bearer-bound actor context shared by
non-sync APIs, deployment-managed binding provisioning behavior, and explicit
non-authority boundaries.

Operations documents own deployment-owner choices, secrets, token acquisition,
command execution, evidence retention, rotation procedure, incident handling,
and rehearsal proof. This spec links those surfaces without moving owner
process or executable commands into platform authority.

Implementation details such as Java class layout, property names, table names,
SQL statements, JWKS library choice, Docker command wiring, and exact
deployment file paths are evidence only unless this spec names the behavior as
accepted.

IDR-016, IDR-027, and IDR-028 remain historical implementation provenance and
trace inputs. After NW-070 review accepts this extraction, use this
specification, the sync contract, BAR-104, operations docs, and current module
boundaries as the durable target for production-auth principal-binding
behavior. Do not treat old IDR prose as a parallel active specification.

## Authentication Modes

The platform has one authenticated-actor resolver boundary for actor-scoped
API logic. It resolves a bearer credential to one platform `actor_id` before
sync, config, assignment, conflict, and `/api/auth/me` logic runs.

Accepted modes:

| Mode | Accepted role |
|---|---|
| `dev-token` | Development compatibility from IDR-016. Static bearer tokens resolve through development token lookup. This is not production authority. |
| `jwt` | Local HS256 JWT compatibility/testing mode that validates a configured issuer/audience/secret and resolves `(iss, sub)` through explicit bindings. This is not the accepted production provider mode. |
| `oidc-jwks` | Accepted production auth-provider mode. Asymmetric provider JWTs are validated by configured issuer, audience, and JWKS URI, then resolved through explicit bindings. |

Production runtime must use `oidc-jwks` and must not permit unauthenticated
development push. Development admin/token surfaces are not production binding
administration.

## Principal Binding Semantics

Production auth maps an authentication principal to a platform actor only
through an explicit active binding:

```text
(issuer, subject) -> actor_id
```

Accepted behavior:

- `issuer` and `subject` identify the provider principal.
- A valid provider credential is necessary but not sufficient for platform
  access.
- An active binding maps one principal to at most one actor at a time.
- Multiple active principals may map to the same actor, for example during
  provider subject rotation overlap.
- If a valid principal has no active binding, actor resolution fails and
  actor-scoped APIs reject the request before platform action.
- Binding lookup support rows are authentication support state, not assignment
  events, scope grants, resolver grants, or event authority facts.
- Assignment, scope, role-action, assignment-administration, resolver, and
  conflict-resolution authority remain governed by their accepted platform
  rules after the actor is resolved.

Principal binding selects the authenticated actor context used by existing
actor-bound endpoints. It does not create new actor identity categories, new
scope mechanisms, or direct provider-driven authority.

## OIDC/JWKS Validation Boundary

The accepted production provider boundary is server-side OIDC/JWKS JWT
validation followed by explicit principal binding.

Accepted validation behavior for `oidc-jwks`:

- the deployment configures issuer, audience, and JWKS URI;
- the JWKS URI supplies verification keys for asymmetric JWT signatures;
- supported asymmetric algorithms are the accepted RSA, RSA-PSS, and ECDSA
  JOSE algorithms exercised by the current validator boundary;
- wrong issuer, wrong audience, missing subject, unknown key, bad signature,
  expired token, not-yet-valid token outside the accepted skew, malformed JWT,
  and unsupported algorithm are rejected before platform action;
- expiration is required for OIDC/JWKS tokens;
- `groups`, realm roles, client/resource roles, resource claims, custom
  claims, and JWT `actor_id` claims are ignored for platform authority.

The current acceptance evidence proves Keycloak-style JWKS validation, signing
key rotation with passive old-key overlap, and unchanged explicit actor
binding through rotation. It does not select a production IdP overlap/removal
policy, mobile OAuth/OIDC login UX, or IdP-driven provisioning authority.

## Authenticated API Context

The same bearer-to-actor boundary applies to these accepted actor-scoped API
surfaces:

- `GET /api/auth/me`;
- `POST /api/sync/push`;
- `POST /api/sync/pull`;
- `GET /api/sync/config`;
- `POST /api/sync/subject-history`;
- assignment create/end/list APIs under `/api/assignments`;
- conflict list, resolution, and manual identity-conflict APIs under
  `/api/conflicts`.

`/api/auth/me` returns the server-resolved `actor_id` and an auth-source label.
Clients use that response to align local actor state with server resolution.
The auth-source label is diagnostic context; it is not authority.

Assignment and conflict APIs use the authenticated actor context set by the
bearer resolver. Request-body actor IDs must not grant creator, resolver,
assignment-admin, or binding-admin authority. Tests prove spoofed body actor IDs
do not grant canonical conflict resolution or assignment administration.

Config package delivery uses the same bearer boundary as sync. A config fetch
does not create per-device authority and does not mutate normal pull
watermarks. Device-reported config versions remain sync/config observability,
not actor authority.

## Sync Push Authorship

In production-auth mode, push is bearer-authenticated and client-authored human
events must match the authenticated actor before persistence.

Accepted behavior:

- every event in an authenticated pushed batch must carry `actor_ref.id` equal
  to the resolved actor UUID;
- a missing actor ID, blank actor ID, non-UUID human actor ID, client-authored
  `system:*` actor, or UUID different from the authenticated actor rejects the
  whole batch with `actor_binding_failed`;
- actor-binding failure occurs before any event in the batch is persisted;
- server-generated events may still use platform-owned `system:*` actors;
- structurally valid but unauthorized work is still accepted-and-flagged only
  after the event has passed authentication, actor binding, envelope
  validation, and payload validation.

This preserves event authorship without allowing a device to forge another
human actor or a system actor.

## Mobile Actor Alignment

Mobile actor state aligns with server-resolved actor context:

- mobile setup/sync resolves the bearer credential through `/api/auth/me`;
- the active actor session stores the returned server actor id with the bearer
  credential;
- event assembly uses the active server-resolved actor id for `actor_ref.id`;
- sync refreshes `/api/auth/me` before push and stops before pushing when the
  credential is unauthorized, the actor id is missing, or the server-resolved
  actor differs from the active local session;
- push, pull, config, and subject-history requests use the active session's
  bearer credential;
- mutable local state remains actor-partitioned under the shared-device
  session boundary.

This is alignment, not mobile authority. A UI-selected actor, stale local
actor id, or local session label cannot override server bearer resolution.
Mobile OAuth/OIDC login UX remains outside this specification.

## Principal-Binding Provisioning

The accepted production administration path for auth principal bindings is
deployment-managed manifest provisioning. It is file-driven, auditable, and
server-side. It is not an online product admin API, not the development
`/api/actors/**` token surface, and not direct operator SQL authority.

Manifest behavior:

- a manifest identifies a version/source and a list of desired operations;
- each operation includes operation id, issuer, subject, actor id, active or
  inactive desired state, and reason;
- the manifest is validated before application;
- missing version/source, missing operations, duplicate operation ids, multiple
  operations for the same principal in one manifest, missing reasons,
  malformed actor UUIDs, and unsupported states are rejected before partial
  application;
- application is transactional and serialized so concurrent manifests cannot
  leave multiple active bindings for one principal.

Operation behavior:

- create: an active operation creates an active binding when no active row
  exists for the principal;
- rotate: adding a new active principal for the same actor is allowed, and the
  old principal may remain active during a reviewed overlap window;
- deactivate: an inactive operation disables the matching active principal
  without deleting historical operation or binding history;
- rebind/correction: an active operation for an already-active principal and a
  different actor deactivates the previous mapping and creates the new active
  mapping, retaining previous binding and previous actor metadata;
- correction of a wrong binding never rewrites events already persisted with
  the previously resolved `actor_ref`.

Idempotency and audit behavior:

- reapplying the same operation id with the same normalized operation hash is a
  skipped idempotent operation;
- reusing an operation id with different normalized content is rejected;
- exact manifest reapplication does not duplicate effective changes, active
  lookup rows, or audit rows;
- every newly applied operation writes append-only audit/history metadata,
  including operation id/hash, manifest version/source/content hash,
  applied-by identity, principal, target actor, desired state, reason, previous
  binding/actor when present, resulting binding, and whether state changed;
- active binding rows are lookup support for authentication; the auditable
  operation history plus current active projection is the administration
  record.

An optional startup manifest runner and the one-shot provisioning command are
implementation/tooling paths for applying the same accepted manifest behavior.
The owner process, command invocation, evidence identifiers, secret handling,
and approval records belong in operations docs.

## Operations Split

The platform/security behavior above is not an operations runbook. Operations
docs remain the durable home for:

- named owners and solo-owner approval model;
- provider, tenant, region, DNS/TLS, PostgreSQL, monitoring, and evidence
  selections;
- secret storage, token acquisition, rotation, revocation, and emergency
  response;
- principal-binding manifest review and privileged input handling;
- exact one-shot command recipes and SQL evidence collection;
- backup/restore, rollback/forward-fix, monitoring, alerting, incident, and
  rehearsal procedure;
- synthetic evidence and real-production approval boundaries.

The first reference deployment policy and production deployment runbook cannot
grant platform actor, assignment, resolver, or principal-binding authority.
They also cannot make IdP groups, roles, JWT `actor_id`, or other claims direct
platform authority.

The 2026-06-17 production deployment rehearsal is partial evidence for the
reference environment and does not accept real production. The 2026-06-17
Keycloak/JWKS rotation adapter is accepted evidence for synthetic signing-key
rotation behavior and unchanged explicit actor binding, not a real-production
IdP policy.

## Acceptance Evidence

BAR-104 is the baseline acceptance anchor for this specification. It records
NW-037 principal-binding foundation, NW-038 OIDC/JWKS validation, and NW-040
deployment-managed manifest provisioning with append-only operation history,
active lookup support rows, idempotency, advisory-lock serialization, create,
rotate, deactivate, rebind behavior, and group/claim/JWT `actor_id`
non-authority.

Focused tests cited by BAR-104 include:

- `ProductionAuthIntegrationTest`;
- `LocalJwtAuthCompatibilityIntegrationTest`;
- `SyncControllerIntegrationTest`;
- `ScopeFilteredSyncIntegrationTest`;
- `AssignmentContainmentIntegrationTest`;
- `ConflictResolutionIntegrationTest`;
- `SubjectHistoryBackfillIntegrationTest`.

Additional mobile evidence proves `/api/auth/me` actor refresh, bearer-auth
on push/pull/config, active actor-session mismatch stopping push, and event
assembly using the server-resolved actor id.

Operational evidence includes the accepted production deployment policy, the
accepted production deployment runbook, the partial 2026-06-17 reference
environment rehearsal, and the accepted 2026-06-17 Keycloak/JWKS rotation
adapter.

## Classification Of Extracted Details

| Detail | Durable classification |
|---|---|
| Push, pull, config, and subject-history request/response shape, bearer requirement, watermarks, cursors, and endpoint error vocabulary | Existing process-boundary authority in `contracts/sync-protocol.md`. |
| Config package body shape | Existing contract authority in `contracts/config-package.schema.json`. |
| Assignment and conflict payload shapes and flag catalog behavior | Existing contract authority in `contracts/shapes/` and `contracts/flag-catalog.md`; see assignment and conflict/flag surfaces. |
| Auth modes, shared authenticated-actor resolver boundary, explicit `(issuer, subject) -> actor_id` semantics, group/claim non-authority, and push actor-ref equality | Accepted platform/security behavior owned here. |
| OIDC/JWKS issuer, audience, JWKS, key/signature, time, subject, and asymmetric-algorithm validation boundary | Accepted platform/security behavior owned here at the level proven by current tests. |
| Deployment-managed principal-binding manifest provisioning, idempotency, concurrency serialization, operation history, active lookup support rows, and create/rotate/deactivate/rebind semantics | Accepted platform/security behavior owned here, with operator process and commands owned by operations docs. |
| Owner approvals, secret handling, token acquisition, exact commands, evidence retention, backup/restore, monitoring, rotation, incident response, and real-production approval | Operational policy, runbook, and rehearsal evidence under `docs/operations/`. |
| Java class names, property keys, table names, SQL text, JWKS library, Docker/Compose command wiring, and exact test helper fixtures | Implementation/tooling evidence only. |
| IDR-016 static token table | Development compatibility provenance, not production authority. |
| IDR-027 and IDR-028 prose | Historical accepted-decision inputs; this spec plus contracts/operations docs are the durable behavior home after extraction. |

## Non-Authority Boundaries

The following do not grant platform authority unless a successor decision
explicitly changes the model:

- IdP groups;
- realm roles;
- client roles;
- resource roles;
- resource claims;
- custom claims;
- JWT `actor_id`;
- request-body actor IDs;
- UI-selected or mobile-selected actors;
- product/persona labels such as admin, operator, coordinator, supervisor,
  reviewer, support role, or auditor;
- deployment-owner, host, database, IdP, cloud, release, or incident roles;
- possession of provider credentials without an active explicit binding and
  without assignment/scope/resolver authority;
- online production binding-admin API or UI claims, because no such accepted
  production surface exists in this slice.

Platform access remains actor plus active assignment plus role/action plus
scope plus time plus resolver rules where applicable.

## Non-Goals

This specification does not authorize:

- online production principal-binding admin APIs or UI;
- direct operator SQL as the accepted binding administration path;
- IdP groups, roles, resource claims, custom claims, or JWT `actor_id` as
  direct platform authority or binding-admin authority;
- mobile OAuth/OIDC login UX;
- production web-admin authentication;
- new actor authority sources;
- new scope mechanisms;
- assignment-admin authority beyond the accepted assignment-admin spec;
- resolver reassignment or new resolver authority;
- emergency override authority;
- new envelope fields or envelope type values;
- changing sync access behavior, normal watermarks, subject-history boundaries,
  assignment authority, conflict resolver equality, or projection semantics;
- claiming real-production approval, independent human continuity, backup,
  restore, monitoring, alerting, rotation, rollback, or recovery readiness
  from this platform spec.

## Escalation Triggers

Route a successor decision or owning durable surface before work that:

- makes any IdP group, role, claim, resource claim, custom claim, or JWT
  `actor_id` platform authority;
- uses provider data as provisioning input in a way that changes actor,
  assignment, resolver, or binding authority;
- adds online production binding administration or production admin UI/API
  authority;
- changes authenticated actor resolution, push actor-ref equality, or
  pre-persistence rejection semantics;
- changes bearer requirements for sync/config/subject-history/assignment/
  conflict APIs;
- adds actor identity fields, provider fields, group fields, or authority
  claims to the event envelope;
- creates new scope mechanisms, emergency bypasses, resolver reassignment,
  broad audit/history access, or mobile authoritative rejection;
- moves owner process, secret management, command execution, evidence, or
  real-production approval into platform behavior;
- requires changing `contracts/sync-protocol.md` or another contract shape.

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
for classification before implementation.
