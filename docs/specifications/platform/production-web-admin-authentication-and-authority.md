# Production Web Admin Authentication And Authority

Status: accepted
Document type: platform_spec
Owner: platform/security verifier
Source: NW-079 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-079-decide-production-web-admin-authentication-and-admin-authority.md`
Authority: BAR-010, BAR-011, BAR-104; `contracts/sync-protocol.md`; `contracts/config-package.schema.json`; `contracts/shape-format.schema.json`; `contracts/flag-catalog.md`; `docs/specifications/platform/production-auth-principal-binding.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/configuration-package-and-shapes.md`; `docs/specifications/platform/expression-language.md`
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/scenarios/23-configure-new-operational-activity.md`; `docs/agent-working-surface/artifacts/product-admin-surface-forward-plan.md`; `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`; `docs/agent-working-surface/operational-ux-layering-companion.md`; `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`; `docs/specifications/platform/production-auth-principal-binding.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/configuration-package-and-shapes.md`; `docs/specifications/platform/expression-language.md`; `docs/implementation/module-interfaces.md`; `contracts/sync-protocol.md`; `contracts/config-package.schema.json`; `contracts/shape-format.schema.json`; `contracts/flag-catalog.md`; `server/src/main/java/dev/datarun/server/authorization/WebConfig.java`; `server/src/main/java/dev/datarun/server/admin/AdminController.java`; `server/src/main/java/dev/datarun/server/config/ConfigAdminController.java`; `server/src/main/java/dev/datarun/server/admin/DevBootstrapController.java`; `server/src/main/java/dev/datarun/server/ops/ProductionDevelopmentSurfaceFilter.java`; `server/src/test/java/dev/datarun/server/ops/ProductionDevelopmentSurfaceFilterTest.java`; `server/src/test/java/dev/datarun/server/config/ConfigIntegrationTest.java`

## Purpose

This specification selects the first production web admin authentication and
admin-authority model. It exists before any production web admin UI
implementation so that future admin screens, sessions, command handlers, and
audit rows have one durable target.

It does not implement web UI, controllers, filters, sessions, migrations,
schemas, tests, or contracts. It does not make the current `/admin` or
`/admin/config` development consoles production-ready.

## Decision Summary

The selected first production web admin model is:

```text
OIDC/JWKS browser login
-> server-managed session cookie
-> server-resolved platform actor context through explicit principal binding
-> command-specific admin authority evaluated by the server
-> append-only operation history or existing event audit, depending on action type
```

The browser session is only a transport for a server-resolved actor context. It
is not a platform authority source. The platform actor is still resolved only
through the accepted production auth boundary:

```text
(issuer, subject) -> actor_id
```

IdP groups, IdP roles, resource claims, custom claims, JWT `actor_id`,
request-body actor IDs, UI-selected actors, and the fixed development admin
actor are not platform, assignment, resolver, binding-admin, or web-admin
authority.

The first web admin authority model uses layered checks:

1. the request has a valid server session created from accepted OIDC/JWKS
   validation and explicit principal binding;
2. the bound actor is allowed to enter the production admin shell;
3. the actor has the exact command capability for the requested admin action;
4. action-specific platform rules still apply, such as assignment
   command-plus-containment or exact designated-resolver equality.

## Admin Personas And Contexts

Persona names are product and operating-context labels. They are not actor
identity categories and do not grant authority by themselves.

| Persona or context | First production web scope | Authority source |
|---|---|---|
| Setup owner / config author | In scope for authoring candidate shapes, activities, expression rules, sensitivity, severity, and pattern bindings. | Server-resolved actor plus explicit config-admin command capability. |
| Setup reviewer / readiness reviewer | In scope for reviewing a candidate setup and recording readiness or rejection against the candidate content hash. | Server-resolved actor plus explicit review or approval command capability. |
| Config publisher | In scope for publishing an approved and still-valid candidate package. | Server-resolved actor plus explicit publish command capability. |
| Assignment coordinator | In scope for assignment create/end through accepted assignment administration. | Existing `assignment_admin.create` and `assignment_admin.end` command capability plus same-assignment containment. |
| Reviewer / resolver | In scope for single-flag inspection and resolution where the actor is the designated resolver. | Existing conflict resolver equality and actor-bound conflict APIs. |
| Support operator | In scope only for scoped, non-mutating inspection if the actor otherwise has access to the underlying data. | Server-resolved actor plus existing assignment/scope rules. Broad support/audit reads are deferred. |
| Read-only observer | In scope only for scoped read surfaces that do not bypass assignment-derived access. | Server-resolved actor plus existing assignment/scope rules. |
| Deployment owner | Not an online web-admin authority by title. Deployment owner process remains in operations docs. | Operations policy and deployment-managed tooling; web authority only if the deployment owner is also bound as an actor with explicit command capability. |

## Admin Task Inventory

| Task | First production status | Required authority |
|---|---|---|
| Admin shell access | In scope. | Valid web admin session plus `web_admin.access` or equivalent server-side shell-access capability. |
| Config edit / authoring | In scope for accepted S23-style setup. | `config_admin.author`; edits remain candidate source config until published. |
| Candidate validation | In scope. | `config_admin.validate` or a stronger config-admin capability. Validation is machine enforcement, not human approval. |
| Readiness review | In scope. | `config_admin.readiness_review`; records human review against a candidate hash and validation result. |
| Setup approval | In scope. | `config_admin.approve`; approves a specific validated candidate hash for field rollout. |
| Config publish | In scope. | `config_admin.publish`; publishes only the approved, still-valid candidate as one atomic package. |
| Assignment create/end | In scope. | Existing `assignment_admin.create` / `assignment_admin.end` plus containment from the accepted assignment spec. |
| Location inspection | In scope only as scoped/reference inspection needed for setup or assignment work. | Existing assignment/scope access or explicit non-mutating admin read capability that does not expose broad audit/history. |
| Subject inspection | In scope only for assignment-scoped operational inspection. | Existing assignment-derived access. Broad subject/audit views are deferred. |
| Flag inspection/resolution | In scope only for resolver-visible single-flag work. | Existing resolver routing and exact designated-resolver equality. |
| Development bootstrap containment | In scope as containment only. | Production must continue to hide development admin/token/bootstrap surfaces. |
| Principal-binding administration | Deferred. | Deployment-managed manifest provisioning remains the accepted production path. |
| Production deployment approval | Deferred to operations. | Operations policy/runbook/rehearsal and Hamza approval authority. |

## Selected Browser And Session Authentication

The selected browser mode is OIDC/JWKS login with a server-managed session
cookie.

Required semantics:

- the server initiates and completes the browser login flow with the configured
  provider;
- provider tokens are validated server-side by accepted OIDC/JWKS issuer,
  audience, signature, key, time, and subject checks;
- the validated provider principal is resolved through the explicit active
  `(issuer, subject) -> actor_id` principal binding;
- login fails before session creation when the principal is unbound,
  deactivated, invalid, expired, wrong issuer, wrong audience, or otherwise
  rejected by the accepted validator boundary;
- the session stores an opaque session identifier in the browser and
  server-side session state containing the resolved actor id, issuer, subject,
  auth source, login time, expiry, and audit/session metadata;
- the session cookie must be `Secure`, `HttpOnly`, and same-site constrained;
- state-changing admin requests require CSRF protection tied to the session;
- logout invalidates the server session;
- session expiry or principal-binding deactivation prevents further admin
  action before command execution.

The implementation may choose exact session storage, cookie name, timeout, and
OIDC flow library as implementation details if the behavior above remains true.
No browser request may supply an actor id that overrides the server-resolved
session actor.

### Rejected Alternatives

| Alternative | Reason rejected for first production web admin |
|---|---|
| Bearer token only | Keeps production browser admin tied to raw secret handling, lacks a product login/session boundary, and increases CSRF/clipboard/leak risk for human web workflows. Bearer credentials remain accepted for actor-scoped APIs and mobile setup paths. |
| Reverse-proxy authenticated identity forwarded to the app | Adds a deployment-specific trusted-header boundary and risks treating proxy headers or provider claims as authority. It can be reconsidered only if a future route binds forwarded principals through the same explicit principal-binding semantics and hardens header trust. |
| Development admin remains dev-only and production web admin is deferred | Preserves safety but blocks web admin/config productization. NW-079 selects a production model so successor UI implementation can start without using the fixed dev admin actor. |
| IdP groups, roles, claims, or JWT `actor_id` | Explicitly rejected. They are not platform actor, assignment, resolver, binding-admin, or web-admin authority. |

## Admin Actor Context

Production web admin requests resolve to an admin actor context before any
admin action runs.

Accepted actor-context fields:

- `actor_id`, resolved from explicit principal binding;
- `issuer` and `subject`, retained for audit and revalidation;
- `auth_source`, such as `oidc-jwks-principal`, for diagnostics;
- `session_id` or equivalent opaque session handle;
- `login_time`, `last_seen_time`, and expiry metadata;
- request correlation metadata suitable for secret-safe audit.

Non-authority fields:

- provider display name, email, groups, roles, resource claims, custom claims,
  and JWT `actor_id`;
- browser-selected account labels;
- form body actor ids;
- the fixed development admin actor id;
- session id by itself.

Every state-changing handler must receive the actor context from the server
session, not from request bodies or UI state. If the session principal no
longer resolves to the same active actor binding, the request is rejected before
platform action and the session is invalidated or forced through login again.

## Command Authority Model

Production web admin is not blanket root access. It is a command surface over a
server-resolved actor.

The first implementation must provide a server-side admin command capability
policy for production web/config commands. The policy is deployment-configured
server state, not a config-package section, not mobile-visible authority, and
not an IdP claim. The exact table or `deployment_config` key is an
implementation detail; the accepted behavior is command evaluation by the
server from current actor context and deployment-reviewed policy.

Command names selected by this spec:

| Command | Meaning |
|---|---|
| `web_admin.access` | Enter the production admin shell. Grants no data read or mutation by itself. |
| `web_admin.read_scoped` | Inspect admin views only where the actor already has scoped access or designated resolver visibility. Does not grant broad audit/history. |
| `config_admin.author` | Create or edit candidate deployer shapes, activities, expression rules, sensitivity classifications, severity overrides, and accepted config declarations. |
| `config_admin.validate` | Run and view server deploy-time validation for a candidate. |
| `config_admin.readiness_review` | Record readiness review outcome for a validated candidate. |
| `config_admin.approve` | Approve a validated candidate hash for field rollout. |
| `config_admin.publish` | Publish the approved candidate as the latest atomic config package. |

Assignment administration keeps the already accepted command names:

- `assignment_admin.create`;
- `assignment_admin.end`.

These assignment commands still require one active assignment for the command
actor that both grants the command and contains the requested or target
assignment scope. A web UI must call the same authority path as the accepted
assignment API. Web-admin shell access, config-admin authority, or IdP metadata
must not bypass assignment containment.

Conflict resolution keeps the accepted resolver model. A web UI may expose
single-flag work where the actor is the designated resolver, but canonical
resolution still requires exact designated-resolver equality. A web-admin
capability must not create resolver reassignment, batch resolution, or
auto-resolution authority.

Because current config packages are deployment-wide, `config_admin.*` commands
for publishable setup are deployment-wide commands. A successor
implementation must avoid pretending that a geographic or subject-scoped actor
can publish a partial config package unless a future route introduces scoped
configuration behavior.

## S23 Setup Authority Mapping

The S23 setup path is split into distinct authority points.

| S23 action | Authority | Required behavior |
|---|---|---|
| Author new or changed setup | `config_admin.author` | Edits create or update candidate source config. They do not publish a package and do not reach devices. |
| Validate candidate setup | `config_admin.validate` | Server deploy-time validation checks the full candidate. Failures block approval and publish. Validation cannot be weakened by UI choice. |
| Readiness review | `config_admin.readiness_review` | Human review records whether the candidate is operationally ready, including form completeness, role-action intent, warning clarity, and responsibility plan. |
| Approve for field rollout | `config_admin.approve` | Approval binds to the exact candidate content hash and latest passing validation result. Changing candidate content invalidates prior approval. |
| Publish atomic package | `config_admin.publish` | The server revalidates, confirms approval still matches the candidate hash, and stores one monotonic full package snapshot. |
| Assign responsibility | `assignment_admin.create` / `assignment_admin.end` | Responsibility uses accepted assignment events, command capability, and containment. It is not an activity role-action and not config-publish authority. |
| Review field readiness after publish | `config_admin.readiness_review` plus existing scoped read/assignment evidence | Readiness review may inspect package version, validation result, and planned responsibility. It must not become reporting/export, production deployment approval, or broad audit read authority. |

One person may hold multiple capabilities in the initial solo-owner or
small-team model when operations policy allows it, but each action remains
separately audited. Future separation-of-duty policy can require distinct
actors without changing the platform command semantics.

## Preserving Accepted Configuration Behavior

Production web admin must preserve the accepted configuration and expression
behavior:

- invalid or incomplete setup is caught before package publication and before
  field workers depend on it;
- publication is all-or-nothing and creates one monotonic full package
  snapshot;
- known config-package sections stay governed by
  `contracts/config-package.schema.json`;
- deployer form shapes stay governed by `contracts/shape-format.schema.json`;
- platform payload schemas are not deployer form shapes;
- workers may temporarily hold different config versions;
- in-progress mobile forms continue under the config that was current when the
  form opened;
- older shape versions remain available for historical rendering and
  projection;
- warning rules remain expression rules, not scripts, custom processing,
  triggers, dynamic queries, or mobile authoritative rejection;
- `activities[*].roles` keeps the accepted activity work-action vocabulary and
  does not grant assignment or admin commands;
- new config-package keys, deployer-authored code, trigger execution, custom
  scope logic, and per-device package variants require successor routing.

## Audit Semantics

Admin audit has two accepted forms:

1. existing domain/event audit where the platform already represents the
   action as append-only events;
2. append-only admin operation history for browser/session/config actions that
   are not domain events.

Audit records must be secret-safe. They must not store bearer tokens, provider
tokens, session cookies, passwords, private keys, or full sensitive request
bodies.

| Surface | Required audit posture |
|---|---|
| Login and session | Append-only session/login operation history or equivalent secret-safe security log with actor id when resolved, issuer, subject, result, reason, session id hash, timestamps, request correlation, and logout/expiry/invalidation. Failed unmapped or invalid principal attempts are recorded without granting an actor. |
| Admin shell access | Secret-safe operation history or request audit showing actor, session, route class, result, and denial reason. Shell access alone is not command authority. |
| Config authoring | Append-only admin operation history for create/update/deprecate/delete-style edits, including actor, command, candidate id or content hash, target artifact, before/after content hash where applicable, timestamp, and reason or comment when supplied. |
| Config validation | Append-only operation history for validation run, candidate hash, pass/fail result, violation summary hash or bounded summary, actor, and timestamp. Failed validation blocks approval and publish. |
| Readiness review and approval | Append-only operation history for review/approval decision, actor, candidate hash, latest validation reference, decision, reason/comment, and timestamp. |
| Config publish | `config_packages.published_by` or successor metadata must be populated with the server-resolved actor. Append-only operation history records candidate hash, approved validation reference, package version, published actor, and whether a package was created. |
| Assignment commands | Canonical audit remains the emitted `assignment_changed` event with `actor_ref` equal to the server-resolved actor. Web operation history may add session/request correlation but cannot replace the event. |
| Flag resolution | Canonical audit remains the emitted `conflict_resolved/v1` review event. Only exact designated-resolver equality clears the flag. Web operation history may add session/request correlation. |
| Development surface containment | Production attempts to development admin/token/bootstrap routes remain denied as hidden development surfaces and are recorded only through secret-safe request/security logs or metrics, not domain events. |

Admin operation history is support state for accountability. It does not create
assignment scope, resolver authority, actor identity, event truth, or config
package wire contract.

## Development Console Containment

The current HTML admin/config console is development-only evidence:

- `/admin` and `/admin/config` are not production-ready;
- current form-based assignment and flag actions use a fixed development admin
  actor and must not be treated as production authority;
- `/admin/dev` development bootstrap creates dev tokens and root-scope test
  assignments and is not production provisioning;
- production profile currently hides `/admin/**` and `/api/actors/**` with the
  production development-surface filter.

Until a successor implements this specification, production must continue to
hide development admin, config, token, and bootstrap surfaces. A successor may
replace the hidden `/admin` surface with an authenticated production web admin
surface only after it proves the selected session, actor-context, command
authority, CSRF, and audit requirements.

Development environments may keep the current fixed dev actor and dev-token
surfaces for local compatibility. Those paths remain non-authority for
production.

## Non-Goals And Deferred Surfaces

This specification does not authorize:

- online production principal-binding administration APIs or UI;
- IdP group, role, resource-claim, custom-claim, or JWT `actor_id` authority;
- mobile OIDC/Keycloak login, refresh-token lifecycle, or shared-device login
  UX;
- reporting dashboards, reporting APIs, warehouses, import/export, or broad
  aggregate views;
- broad audit/history read APIs, arbitrary historical reconstruction, or
  no-local-retention/redacted audit views;
- data retention, local encryption, device decommissioning, sealed-partition
  recovery, token/session retention policy beyond web-session behavior named
  here, or regulatory erasure/redaction;
- conflict batch handling, pending-match queues, resolver reassignment,
  auto-resolution, or non-designated resolver authority;
- new geographic, subject, query, custom, auditor, grace, emergency, or
  provider-claim scope mechanisms;
- entity lifecycle, trigger execution, deployer-authored functions/scripts,
  custom state machines, or custom processing pipelines;
- envelope field or envelope `type` changes;
- config-package schema changes or deployer shape-format changes;
- production deployment approval, provider/region selection, backup/restore
  proof, monitoring ownership, incident process, or operations policy changes.

## Acceptance Criteria For Successor Implementation

A production web admin implementation successor must prove at least:

- unauthenticated browser requests cannot reach production admin actions;
- OIDC/JWKS login validates provider tokens server-side and rejects invalid,
  expired, wrong-issuer, wrong-audience, malformed, and unmapped principals;
- server sessions resolve to one explicit principal-bound actor and reject
  request-body or UI-selected actor spoofing;
- IdP groups, roles, claims, and JWT `actor_id` do not grant web-admin,
  assignment, resolver, or binding-admin authority;
- command handlers enforce `web_admin.access` and the exact command capability
  required by the action;
- config authoring, validation, readiness review, approval, and publish remain
  separate auditable actions;
- publish revalidates and publishes only an approved matching candidate hash;
- failed validation creates no config package;
- config package body and deployer shape format remain contract-compatible;
- assignment create/end from web uses accepted assignment service behavior,
  command capability, and containment;
- flag resolution from web preserves exact designated-resolver equality;
- state-changing admin requests require CSRF protection;
- session logout, expiry, principal-binding deactivation, and command denial
  stop action before mutation;
- audit records are append-only, actor-bound, session-correlated, and
  secret-safe;
- production development-surface containment remains in place for dev token
  and bootstrap surfaces;
- current contracts, schemas, event envelope fields/types, and operations
  policies are unchanged unless a separate route explicitly changes them.

## Successor Implementation Rows To Promote

Promote one implementation row at a time. Suggested successor rows:

| Candidate row | Type | Scope |
|---|---|---|
| Implement production web admin login and session boundary | implementation | Browser OIDC/JWKS login, server session, CSRF, logout, session expiry, principal-binding revalidation, admin actor context, and login/session audit. No admin product UI expansion beyond a minimal protected shell. |
| Implement admin command capability and scoped admin shell gate | implementation | Server-side web/config admin command policy, `web_admin.access`, `web_admin.read_scoped`, `config_admin.*` evaluation, denial behavior, and tests proving IdP claim non-authority. |
| Productize S23 config setup workflow | product_design_then_implementation | Candidate authoring, validation, readiness review, approval, publish, audit history, and package publication over accepted config contracts. No new config-package schema or deployer programming features. |
| Productize assignment administration in web admin | product_design_then_implementation | Web assignment create/end over existing `assignment_admin.create/end` and containment. No new scope mechanisms or root bypass. |
| Productize resolver-visible single-flag review | product_design_then_implementation | Resolver-visible flag list/detail/single resolution over exact designated-resolver semantics. No batch handling, resolver reassignment, or auto-resolution. |
| Define broad support/audit reads if needed | future_decision | Only if support operators need broad audit/history, redacted/no-local-retention, reporting, or cross-scope inspection beyond existing assignment/resolver access. |

Online principal-binding administration, mobile login, reporting/export,
retention/security, conflict batch handling, new scopes, and production
deployment approval remain separate routes and must not be absorbed into these
implementation rows.

## Escalation Triggers

Route a successor architecture, contract, operations, or product/security
decision before:

- using IdP groups, roles, claims, JWT `actor_id`, request body, UI state, or a
  fixed dev actor as platform authority;
- adding online principal-binding administration;
- changing the sync protocol, config-package schema, shape-format schema, flag
  catalog, envelope fields, or event `type` vocabulary;
- adding new scope mechanisms or scoped config-package variants;
- granting broad audit/history, reporting/export, or support reads that bypass
  event-level access;
- adding resolver reassignment, auto-resolution, batch conflict resolution, or
  emergency/special write bypass;
- turning readiness review, approval, or product labels into workflow-state
  truth;
- moving production deployment approval, provider choice, backup/restore,
  monitoring, retention/security, or incident ownership from operations docs
  into platform authority.
