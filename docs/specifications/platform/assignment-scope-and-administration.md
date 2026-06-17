# Assignment Scope And Administration

Status: accepted
Document type: platform_spec
Owner: authorization/sync verifier
Source: NW-069 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-069-extract-assignment-scope-and-administration-durable-behavior.md`
Authority: `contracts/shapes/assignment_created.schema.json`; `contracts/shapes/assignment_ended.schema.json`; `contracts/sync-protocol.md`; `contracts/config-package.schema.json`; `contracts/flag-catalog.md`; BAR-003, BAR-006, BAR-007, BAR-010, BAR-011, BAR-013, BAR-014, and BAR-104; IDR-013, IDR-014, IDR-015, IDR-021, IDR-023, IDR-024, and IDR-029 as historical decision inputs; implementation evidence in `docs/implementation/module-interfaces.md`
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/specifications/platform/configuration-package-and-shapes.md`; `docs/specifications/platform/expression-language.md`; `docs/implementation/module-interfaces.md`; `docs/agent-working-surface/artifacts/architecture-classification-drift-audit.md`; `docs/agent-working-surface/artifacts/idr-durable-surface-routing-audit.md`; `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`; `docs/decisions/idr-013-assignment-payload.md`; `docs/decisions/idr-014-materialized-path-locations.md`; `docs/decisions/idr-015-scope-filtered-sync-query.md`; `docs/decisions/idr-021-role-action-enforcement-model.md`; `docs/decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md`; `docs/decisions/idr-024-multi-axis-assignment-containment.md`; `docs/decisions/idr-029-assignment-admin-command-capability.md`; `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`; `server/src/main/java/dev/datarun/server/authorization/ActiveAssignment.java`; `server/src/main/java/dev/datarun/server/authorization/ScopeResolver.java`; `server/src/main/java/dev/datarun/server/authorization/AssignmentAdminCapabilityService.java`; `server/src/main/java/dev/datarun/server/sync/SyncController.java`; `server/src/main/java/dev/datarun/server/event/EventRepository.java`; `server/src/main/java/dev/datarun/server/config/DeployTimeValidator.java`; `server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java`; `server/src/test/java/dev/datarun/server/authorization/AssignmentContainmentIntegrationTest.java`; `server/src/test/java/dev/datarun/server/authorization/ScopeFilteredSyncIntegrationTest.java`; `server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java`; `server/src/test/java/dev/datarun/server/integrity/AuthFlagIntegrationTest.java`; `server/src/test/java/dev/datarun/server/config/DeployTimeValidatorTest.java`

## Purpose

This specification records accepted platform behavior for assignment payload
meaning, assignment-derived access, scope-filtered sync, activity role-action
boundaries, multi-axis assignment containment, and assignment-administration
command capability.

It extracts durable behavior from accepted IDR-era inputs, BAR/NW evidence,
contracts, module boundaries, and implementation tests. It does not change
runtime behavior, schemas, protocol shape, event envelope fields, assignment
payload fields, scope mechanisms, authority sources, activity action
vocabulary, or mobile authority.

## Contract And Trace Decision

The existing contracts remain the authoritative surfaces for process and wire
shapes:

| Surface | Contract-owned content |
|---|---|
| `contracts/shapes/assignment_created.schema.json` | Required assignment-created payload fields and closed payload object structure. |
| `contracts/shapes/assignment_ended.schema.json` | Required assignment-ended payload fields and closed payload object structure. |
| `contracts/sync-protocol.md` | Push, pull, config, and subject-history endpoint shape, authentication, watermarks, cursors, and error vocabulary. |
| `contracts/config-package.schema.json` | Config package shape, `activities[*].roles`, and the activity work-action enum. |
| `contracts/flag-catalog.md` | Authorization flag categories, default resolvability/severity, detection ordering, and state-exclusion semantics. |

This platform spec owns the accepted prose behavior those contracts do not
fully express: assignment identity, active-assignment reconstruction, scope
composition, null and empty-list semantics, write-time location scope,
scope-filtered inclusion categories, activity role-action authority split,
command-capability checks, and non-authority boundaries.

Implementation details such as SQL shape, indexes, table names, Java helper
structure, scan limits, REST controller class names, and exact exception text
are evidence only unless named here as accepted behavior. The accepted target
is the behavior, not the current helper layout.

`contracts/sync-protocol.md` already carries enough process-boundary language
for this extraction: normal pull is actor-scoped, subject-history is a separate
authorized backfill surface, and watermarks/cursors are contract-owned there.
NW-069 does not require a sync protocol change. The detailed inclusion
predicates belong here because they are platform authorization behavior over
the existing protocol shape.

IDR-013, IDR-014, IDR-015, IDR-021, IDR-023, IDR-024, and IDR-029 remain
historical implementation provenance and trace inputs. After this extraction,
use this specification plus the contracts above as the durable target for
assignment/scope/administration behavior. Do not treat old IDR prose as a
parallel active specification.

## Assignment Payload And Identity

Assignment lifecycle facts are immutable `assignment_changed` events:

- `assignment_created/v1` creates one assignment.
- `assignment_ended/v1` ends one assignment.
- The assignment identity is the event envelope `subject_ref.id` where
  `subject_ref.type = "assignment"`.
- An end event targets the assignment by sharing the same `subject_ref.id`.
- There is no separate `assignment_id` field in either assignment payload.

The payload schemas own field structure. The accepted meaning is:

- `target_actor` is the actor receiving the assignment.
- `role` is a deployment-owned opaque assignment role label.
- `scope` carries the platform-fixed axes `geographic`, `subject_list`, and
  `activity`.
- `valid_from` records the intended effective time.
- `valid_to = null` means indefinite until ended; a non-null `valid_to` is the
  expiry boundary.
- `assignment_ended/v1.reason` is optional human/process context, not
  authority.

The command actor is the envelope `actor_ref`, not a payload field. Assignment
creation and ending must use the authenticated actor or an explicit
bootstrap/provisioning/system actor path. Request-body actor IDs, IdP claims,
and mobile-selected actors do not create assignment authority.

## Active Assignments And Temporal Authority

Assignment authority is reconstructed from the event timeline. An assignment
is active for the current accepted runtime path when:

- an `assignment_created/v1` event exists for the actor;
- no `assignment_ended/v1` event exists with the same assignment
  `subject_ref.id`;
- `valid_to` is null or has not expired.

Accepted runtime evidence covers already-effective assignments, explicit end
events, and `valid_to` expiry behavior. `valid_from` remains required payload
metadata and records intended effectiveness, but the named acceptance evidence
does not establish future-dated scheduled activation as a separate product or
platform behavior. Do not build adjacent product/API commitments that rely on
future-dated activation without a follow-up implementation/spec route and guard
tests.

For offline pushed work, authorization detection is accept-and-flag:

- structurally valid work events are persisted before authorization flags;
- `temporal_authority_expired` is evaluated before `scope_violation`;
- this prevents an actor whose assignment ended while offline from being
  misclassified as a scope violator when the stale event was covered by the
  ended assignment;
- `role_stale` is evaluated after temporal and scope checks.

The flag category defaults, resolvability, designated-resolver expectations,
and state-exclusion rules remain owned by `contracts/flag-catalog.md` and the
conflict/flag surfaces. This spec records only the assignment authority behavior
that feeds those flags.

## Scope Composition

The assignment scope axes are platform-fixed:

| Axis | Restricted value | Unrestricted value |
|---|---|---|
| `geographic` | one location UUID | `null` |
| `subject_list` | non-empty array of subject UUIDs | `null` |
| `activity` | non-empty array of activity refs | `null` |

Within one assignment, all non-null scope axes compose with AND. An event or
requested assignment scope is covered by that assignment only when every
restricted axis passes.

Across assignments, visibility and work-action authority compose with OR. An
event is covered when at least one active assignment covers it. Authority from
different assignments must not be combined to satisfy different axes for one
assignment-administration command.

`null` is the only unrestricted value for `subject_list` and `activity`.
Empty arrays are rejected for assignment creation and are not equivalent to
`null`.

Axis behavior:

- A restricted geographic axis covers subject events whose write-time
  `location_path` is within the assignment location path. `geographic = null`
  imposes no geographic restriction.
- A restricted subject-list axis covers only listed subject IDs.
  `subject_list = null` imposes no subject-list restriction.
- A restricted activity axis covers only listed activity refs.
  `activity = null` imposes no activity restriction.
- Ordinary activity work events with `activity_ref = null` are not authorized
  by activity-restricted assignments.

Subject-list-only, activity-only, or mixed-axis assignments do not imply root
authority. An assignment may be unrestricted on one axis while remaining
restricted on another.

## Write-Time Location Scope

Event `location_path` is server-managed infrastructure metadata. It is not an
event envelope field and not a client-authored payload value.

Accepted behavior:

- when a subject event is inserted, the server resolves `location_path` from
  the subject's current location reference;
- assignment events and non-subject events have no subject-derived
  `location_path`;
- once a non-null event `location_path` is set, later location reparenting or
  subject-location correction must not rewrite that historical event path;
- future subject events use the current subject-location path at insert time;
- controlled backfill is allowed only for events that had no `location_path`
  because the subject location was unknown at insertion.

This preserves historical scope interpretation for geographic sync/access.
Subject-list-only scopes are evaluated by subject ID and do not require
geographic location.

## Scope-Filtered Sync

Normal pull uses the current authenticated actor and current active assignments
at request time. It is not a historical audit pull and does not replay old
scope after reassignment.

Normal pull includes only events authorized by the actor's current scope:

- subject events whose write-time location, subject ID, and activity pass one
  active assignment's axes;
- assignment lifecycle events targeting the pulling actor, so devices can
  reconstruct their own scope;
- integrity and identity events only when their source or subject context is
  in the actor's current scope.

Candidate query mechanics may scan by broad geographic paths or unrestricted
scope first, but final inclusion must still enforce the accepted scope axes
unless the actor has fully unrestricted current scope. Pagination must scan
past filtered rows so a page can return the next authorized event rather than
leaking or stopping at unauthorized candidates.

Subject-history backfill is a separate endpoint and cursor surface. It is
authorized on every page against the actor's current assignment authority for
the requested subject/activity. It does not call or mutate normal pull, does not
rewrite normal live-sync watermarks, and does not update `device_sync_state`.

Subject-history backfill is subject-bound:

- it returns the requested subject/activity history when currently authorized;
- related conflict flags and resolutions are included only when their source
  event belongs to the requested subject/activity slice;
- assignment lifecycle events are included only when the assignment scope
  explicitly names the requested subject alias group and is activity-unrestricted
  or includes the requested activity;
- broad geographic assignment history is not exposed as audit pull.

Broad audit/history access, arbitrary historical reconstruction by actor,
geography, activity, or time range, and redacted/no-local-retention audit views
remain future routes. They are not implied by normal pull or subject-history
backfill.

## Activity Role-Action Boundary

Activity role-action mappings are activity work configuration. The accepted
activity work-action vocabulary is:

- `capture`;
- `review`;
- `alert`;
- `task_created`;
- `task_completed`.

The config package schema owns the package shape and action enum for
`activities[*].roles`. Deploy-time validation rejects unknown actions and
empty role-action lists.

`assignment_changed` is not an activity role action. It must not appear inside
`activities[*].roles`, and `role_stale` must not be emitted for assignment
lifecycle events. Assignment lifecycle commands are online assignment
administration, not offline activity work.

For ordinary work events:

- server-side authorization detection is authoritative;
- mobile role-action behavior is advisory only;
- a structurally valid event whose role/action authority is stale or absent is
  accepted and flagged rather than rejected by mobile;
- action permission is granted only inside a covering assignment's own scope;
- permissions OR across covering assignments, but an assignment role does not
  grant permission outside that assignment's scope.

`role_stale` means action-authority mismatch, not every role-label change. A
role change is clean for an attempted action when both horizon and current
covering authority permit that action.

## Assignment Administration

Assignment creation and ending are online command paths that append immutable
`assignment_changed` events. They are not activity work actions and not
mobile-authoritative operations.

Create containment:

- one active assignment for the command actor must grant
  `assignment_admin.create`;
- that same active assignment must contain the requested new assignment scope
  across geographic, subject-list, and activity axes;
- command capability from one assignment cannot be combined with scope from a
  different assignment;
- a requested unrestricted axis (`null`) requires the covering assignment to be
  unrestricted on that same axis, unless an explicit bootstrap/root path is in
  use.

End containment:

- one active assignment for the command actor must grant
  `assignment_admin.end`;
- that same active assignment must contain the target assignment's scope across
  geographic, subject-list, and activity axes;
- narrower or disjoint actors cannot end broader or unrelated assignments.

Bootstrap/root authority must be explicit:

- initial bootstrap/provisioning is a named path with a system/provisioning
  actor boundary;
- a production actor with no active assignments has no assignment-admin
  authority merely because setup is needed;
- root-like ordinary actors are represented by active assignments that are
  unrestricted on all axes and whose role grants the required command;
- request-body spoofing cannot reach bootstrap or root authority.

## Assignment-Admin Command Capability

Assignment-admin command capability is a server-side deployment-configured
policy surface named `assignment_admin_capabilities`.

The platform-owned command names are:

- `assignment_admin.create`;
- `assignment_admin.end`.

The policy maps deployment-owned assignment role labels to these platform-owned
commands. Absent policies, absent role entries, and missing command names deny
the command. Unknown command names are invalid configuration.

The server evaluates command capability at command time from the actor's
current active assignments before containment. Assignment events store role and
scope, not command capability. The command policy is not delivered as a known
config package section in the accepted baseline, and NW-069 does not add a
config package contract key.

Assignment-admin command capability is not:

- `activities[*].roles`;
- an activity role-action permission;
- an event envelope `type`;
- an assignment payload field;
- a mobile-authored claim;
- request-body actor authority;
- an IdP group, IdP role, resource claim, custom JWT claim, or JWT `actor_id`;
- resolver authority, resolver reassignment, conflict resolution, or
  auto-resolution authority;
- a new geographic, subject, activity, query, custom, auditor, grace, or
  emergency scope mechanism;
- audit/history read authority or domain work authority.

## Server And Mobile Authority Split

Server-side evaluation is authoritative for assignment reconstruction,
scope-filtered pull, subject-history authorization, role-action flags,
assignment create/end, assignment-admin command capability, authenticated actor
binding, and flag emission.

Mobile may use assignments, config, role maps, and future display hints for
advisory UX, local projection, hiding unavailable affordances, or warning the
user. Mobile must not reject structurally valid policy/state anomalies as the
authority boundary, mint actor identity, grant command authority, rewrite
pending event authorship, or bypass server assignment/scope checks.

Production authentication maps provider principals to actor IDs only through
the accepted authenticated actor resolver boundary. IdP groups, roles, resource
claims, custom claims, and JWT `actor_id` claims remain non-authority for sync,
assignment, resolver, and assignment-admin behavior.

## Acceptance Evidence

BAR-003 and BAR-007 are the primary baseline evidence for scoped sync and
assignment containment. They record idempotent push, ordered pull,
pagination/watermark behavior, scope-filtered delivery, creator containment,
actor-bound assignment commands, three-axis scope filtering, and reassignment
behavior.

BAR-006 and `contracts/flag-catalog.md` are the flag/resolver evidence for
authorization categories, resolvability, designated resolver expectations, and
state exclusion. BAR-013 records accept-and-flag detector ordering and
state-exclusion behavior. BAR-014 records server/mobile projection equivalence.

BAR-010 is the config package delivery evidence for activity role-action maps,
schema-bounded action vocabulary, and mobile package preservation. BAR-011 is
related config/evaluator evidence and does not add assignment authority.
BAR-104 preserves production-auth principal binding and group/claim
non-authority.

Additional NW/runtime evidence:

- NW-025/S19 proves stale offline authority is persisted and flagged without
  normal live-sync watermark rewrites.
- NW-029/S21 proves scoped supervisor review and role-action handling without
  new authority primitives.
- NW-030/S27 proves logistics-style transfer behavior uses existing assignment
  axes and activity roles.
- NW-033/S26 proves scoped report inputs remain under event-level access
  constraints.
- NW-042/S22 proves reassignment, handoff, subject-history separation, and
  `assignment_changed` exclusion from activity roles using existing constructs.
- NW-050 proves server-side `assignment_admin_capabilities`, deny-all absent
  policy behavior, same-assignment command-plus-containment enforcement, and
  preservation of sync, subject-history, resolver equality, payload schemas,
  and mobile non-authority.

The referenced server tests remain the guard evidence. This specification is
the durable behavior target, not a replacement for those tests.

## Classification Of Source Details

| Detail | Durable classification |
|---|---|
| Assignment payload fields and closed payload object shapes | Existing contract authority in `contracts/shapes/assignment_created.schema.json` and `contracts/shapes/assignment_ended.schema.json`. |
| Assignment identity as envelope `subject_ref.id`, no payload `assignment_id`, and end-by-shared-assignment subject ref | Accepted platform behavior. |
| Role label as opaque assignment role string | Accepted platform behavior; role labels may key server-side policies but do not store capabilities in assignment payloads. |
| Active assignment reconstruction from assignment events, end events, and expiry | Accepted platform behavior, with future-dated activation caveat noted above. |
| AND within one assignment and OR across assignments | Accepted platform behavior. |
| `null` as unrestricted axis and empty arrays rejected/non-equivalent | Contract plus accepted platform behavior. |
| Write-time `location_path` and non-rewrite boundary | Accepted platform behavior; SQL/storage mechanics are implementation evidence. |
| Pull endpoint request/response, watermarks, auth, config discovery, and subject-history endpoint shape | Existing process-boundary authority in `contracts/sync-protocol.md`. |
| Normal pull inclusion predicates and subject-history separation from live sync | Accepted platform behavior. |
| Activity work-action vocabulary and `activities[*].roles` package shape | Existing contract authority in `contracts/config-package.schema.json`, with accepted platform prose for the role-action boundary. |
| `assignment_changed` exclusion from activity role-action config | Accepted platform behavior. |
| Authorization flag names, ordering, severity/resolvability, and unresolved-flag state exclusion | Existing contract authority in `contracts/flag-catalog.md`; assignment-specific ordering is recorded here. |
| `assignment_admin.create` and `assignment_admin.end` command names | Accepted platform behavior. |
| `assignment_admin_capabilities` role-to-command semantics | Accepted server-side platform behavior. |
| `deployment_config` table name and exact Java service/helper layout | Implementation evidence only. |
| Query/index mechanics for scoped pull and subject-history pages | Implementation evidence only unless a later performance or compatibility route makes them normative. |
| IDR text that frames these behaviors as active decisions | Historical provenance after this extraction. |

## Non-Goals

This spec does not authorize:

- new envelope fields, envelope `type` values, assignment payload fields, or
  assignment refs;
- new geographic, subject, activity, query, custom, auditor, grace, or
  emergency scope mechanisms;
- deployer-authored scope scripts, dynamic query authority, or containment
  logic in configuration;
- `assignment_changed` in `activities[*].roles`;
- assignment-admin authority from activity role-actions;
- mobile authoritative rejection of structurally valid events;
- mobile-selected actor identity or cross-actor request signing;
- IdP group/claim/role/JWT `actor_id` authority;
- resolver reassignment, conflict-resolution authority, auto-resolution, or
  conflict resolver equality changes;
- broad audit/history pull, reporting warehouse access, export/import
  semantics, or aggregate visibility outside event-level access;
- online production binding-admin APIs or production assignment-admin UI/API
  expansion beyond the accepted server command path;
- retention, expiry, local encryption, redaction, erasure, sealed-partition
  recovery, or no-local-retention security behavior.

## Future Routes And Escalation Triggers

Route a successor platform, contract, security, operations, or architecture
decision before:

- changing assignment payload shape, assignment identity, envelope fields, or
  envelope type vocabulary;
- weakening containment-before-append or allowing command capability and scope
  to combine across assignments;
- adding future-dated scheduled activation behavior that product/API work will
  rely on;
- adding subject/query/custom scope, cross-activity cohorts, dynamic auditor
  scope, special read/write access, emergency override, or grace-scope
  semantics;
- turning `activities[*].roles` into assignment-administration authority;
- delivering assignment-admin capability as a known config package section;
- adding broad audit/history APIs or normal-pull historical reconstruction;
- making mobile the authority boundary for rejection, scope, assignment-admin
  command checks, or actor identity;
- treating IdP groups, roles, resource claims, custom claims, or JWT
  `actor_id` as platform authority;
- changing flag category semantics, resolver equality, resolver reassignment,
  or auto-resolution behavior;
- introducing retention/security behavior such as expiry windows, local
  encryption, redaction, erasure, decommissioning, or sealed-partition recovery.
