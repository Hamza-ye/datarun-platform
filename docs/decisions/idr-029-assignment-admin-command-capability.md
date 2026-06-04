---
id: idr-029
title: Assignment-admin command capability
status: active
date: 2026-06-05
phase: post-phase-4-stabilization
type: decision
reversal-cost: medium
touches: [server/authorization, server/config, mobile/data, contracts]
superseded-by: ~
evolves: IDR-021, IDR-023, IDR-024, IDR-027, IDR-028
commit: ~
tags: [authorization, assignment, command-capability, configuration]
---

# Assignment-Admin Command Capability

## Context

The current assignment-administration model is containment-only. `/api/assignments`
create and end commands are bearer-bound to the authenticated actor, ignore
request-body actor IDs as command authority, and then enforce IDR-024
multi-axis containment against the requested or target assignment scope.

That remains the accepted baseline for current runtime evidence. It prevents
scope expansion, preserves the IDR-023 separation between activity work actions
and assignment lifecycle administration, and supports accepted S22/S25-style
handoff when commands stay with root or covering coordinators.

The model is too coarse for broader production assignment-admin UI/API
exposure. An actor with covering scope is not distinguished from a coordinator
or assignment administrator. Containment answers "is the target scope inside
this actor's authority?" but not "may this actor attempt assignment
administration at all?"

## Decision

Add a platform-owned assignment-admin command capability model outside
`activities[*].roles`.

The command vocabulary is fixed to:

- `assignment_admin.create`
- `assignment_admin.end`

These are product/platform command names, not envelope `type` values, not
activity work actions, and not deployer-defined verbs.

The policy lives in a dedicated deployment-configured server policy surface
named `assignment_admin_capabilities`. Its semantics are a role-to-command map
keyed by existing assignment role labels:

```json
{
  "schema_version": 1,
  "roles": {
    "coordinator": ["assignment_admin.create", "assignment_admin.end"],
    "handoff_lead": ["assignment_admin.create"]
  }
}
```

The exact storage mechanism is an implementation detail for the successor
slice, but the semantics are fixed:

- command names are platform-owned and validated by the server;
- role labels are deployment-owned assignment role strings already present in
  `assignment_created/v1` payloads;
- absent role entries and absent command names deny the command;
- unknown command names are invalid configuration;
- the map is not part of `activities[*].roles`.

Ordinary actor command evaluation is server-side and ordered as:

1. Resolve the bearer credential through `AuthenticatedActorResolver`.
2. Reconstruct the actor's active assignments from assignment events.
3. Select active assignments whose role label grants the required command in
   `assignment_admin_capabilities`.
4. If no active assignment grants the command, deny before containment.
5. Run IDR-024 containment using only command-capable assignments. One active
   assignment must both grant the requested command and contain the requested
   create scope or target end-assignment scope across all platform-fixed axes.
6. Append the immutable `assignment_changed` event using the authenticated
   actor as `actor_ref`.

Capability from one assignment must not be combined with scope containment from
another assignment. This preserves CDL-030's role-plus-scope model and IDR-024's
"one active covering assignment" containment rule.

`assignment_admin.create` authorizes an actor to attempt assignment creation
only within a contained target scope. `assignment_admin.end` authorizes an actor
to attempt ending an existing assignment only when the target assignment's scope
is contained by a command-capable active assignment.

The server evaluates the current assignment-admin policy at command time.
Assignment events keep recording role label, target actor, scope, validity, and
end reason only. Command capability is not stored in the event envelope or in
assignment payloads.

## Scope

This decision covers:

- assignment creation through the online assignment-admin command path;
- assignment ending through the online assignment-admin command path;
- the command vocabulary and deployment policy semantics;
- bootstrap/provisioning/root boundaries for assignment administration;
- future guard tests for the runtime implementation.

Initial bootstrap remains an explicit provisioning path, not ordinary actor
authority. A production actor with no active assignments has no assignment-admin
authority merely because the system needs setup.

Ordinary root-like actors are modeled as actors with active assignments whose
scope is unrestricted on all axes and whose role label grants the relevant
assignment-admin commands. A separate deployment/provisioning command path may
bypass ordinary actor policy only when it is explicitly named, auditable, and
not reachable by spoofing request-body actor fields.

Central or provisioning-only administration remains an allowed deployment
posture: a deployment may grant no ordinary assignment roles these commands and
instead use an explicit provisioning/admin process. That posture is not the
default platform model because the platform also needs field handoff and
coordinator delegation without new scope mechanisms.

Existing accepted assignment events remain compatible. `assignment_created/v1`
continues to carry `target_actor`, `role`, `scope`, `valid_from`, and `valid_to`.
`assignment_ended/v1` continues to carry `reason`. No envelope field, envelope
`type`, `assignment_ref`, or platform payload field is added by this decision.

## Non-Authority Boundary

Assignment-admin command capability is not:

- `activities[*].roles`;
- activity role-action permission;
- an event envelope `type`;
- a device-authored authority claim;
- an IdP group, role, realm/client role, resource claim, JWT `actor_id`, or
  custom token claim;
- request-body actor authority;
- UI/product vocabulary authority;
- conflict resolver designation, resolver reassignment, or auto-resolution;
- a new geographic, subject, activity, query, custom, auditor, or grace scope
  mechanism.

Mobile may use delivered or fetched capability information only for advisory
display and UI affordances. Mobile must not become the authority boundary for
assignment create/end.

## Alternatives

### Current containment-only model

Keep create/end authorized solely by authenticated actor plus IDR-024
containment. This remains accepted baseline behavior for existing probes, but it
does not distinguish same-scope field actors from coordinators before exposing a
production assignment-admin surface.

Rejected as the future production model.

### Separate command-capability policy independent of assignments

Add actor-level or principal-level command grants separate from assignment role
labels. This directly expresses "may administer assignments" but risks creating
a second authority plane that must be reconciled with scope, production auth,
and audit semantics.

Rejected for the first implementation because Datarun already treats
assignment as the atomic authority grant.

### Assignment role-to-command policy

Use assignment role labels as deployment-owned policy keys and platform-owned
assignment-admin command names as values. Server code still owns containment and
command evaluation. This closes the same-scope command risk without making
activity mandatory and without inventing new scope mechanisms.

Selected.

### Central/provisioning-only administration

Allow assignment administration only through a deployment/provisioning process
or system actor. This is a valid deployment posture for high-control
environments, but it is too rigid as the platform default because field handoff
and coordinator delegation are accepted operational pressures.

Allowed as an explicit deployment posture, not selected as the general model.

### Event-payload command capability

Store command capabilities or admin policy snapshots inside
`assignment_created/v1` payloads. This would make authorization policy durable
event truth and would create audit/time semantics that are not needed for online
assignment-admin commands.

Rejected. If future audit needs policy provenance, route a dedicated
administrative operation-history or policy-version decision. Do not add
assignment payload fields by default.

## Consequences

A successor implementation needs a server-side configuration and validation
surface for `assignment_admin_capabilities`, plus assignment service/controller
checks before IDR-024 containment. It does not need envelope or assignment
payload contract changes.

Config package contract changes are needed only if the implementation delivers
the policy to mobile or other clients as a known package section for advisory
display. If delivered, `contracts/config-package.schema.json` and contract tests
must be updated. If server enforcement is implemented without package delivery,
mobile can remain unchanged.

Existing assignment events and persisted assignment role labels remain valid.
Deployments that want ordinary actors to administer assignments must configure
which assignment roles grant `assignment_admin.create` and
`assignment_admin.end`. Deployments that want the prior containment-only
behavior must make that posture explicit by granting commands to the relevant
roles; it is not inferred from containment alone.

Existing containment, spoofed-actor, activity-role-boundary, S22 handoff, normal
sync, subject-history, and resolver-equality tests should remain as guard tests.
Future implementation tests must add command-capability setup rather than
weakening containment or activity role-action assertions.

NW-049 remains separate. Auditor/special access, shared-device actor switching,
grace behavior, subject-scope variants, query/custom scope pressure, and device
data-expiry questions are not decided here.

## Guard Tests For Implementation

The successor implementation must prove:

- an actor with covering scope but no `assignment_admin.create` capability
  cannot create an assignment;
- an actor with covering scope but no `assignment_admin.end` capability cannot
  end an assignment;
- an actor with `assignment_admin.create` can create only inside one active
  command-capable covering assignment;
- an actor with `assignment_admin.end` can end only target assignments inside
  one active command-capable covering assignment;
- command capability from one assignment cannot be combined with scope from a
  different assignment;
- out-of-scope create/end fails even when the actor has the command capability;
- spoofed request-body actor fields do not grant command authority;
- IdP groups, roles, resource claims, custom claims, and JWT `actor_id` do not
  grant command authority;
- `assignment_changed` remains invalid in `activities[*].roles`;
- normal sync, subject-history backfill, conflict resolver equality, and S22
  handoff behavior remain unchanged;
- mobile behavior, if any, is advisory/display only.

## Follow-Up Route

Add a bounded implementation slice for this selected model. That slice should
implement server-side `assignment_admin_capabilities`, command checks before
containment, deployment/config validation, and focused tests. It must not
implement NW-049 access exceptions, online production binding-admin APIs,
resolver reassignment, auto-resolution, new scope mechanisms, envelope changes,
or authoritative mobile rejection.

## Traces

- CDL: CDL-030, CDL-031, CDL-032, CDL-034, CDL-035, CDL-055.
- IDR: IDR-021, IDR-023, IDR-024, IDR-027, IDR-028.
- Working surface: NW-043, NW-048, NW-049.
- Files: `contracts/shapes/assignment_created.schema.json`,
  `contracts/shapes/assignment_ended.schema.json`,
  `contracts/config-package.schema.json`,
  `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`,
  `server/src/main/java/dev/datarun/server/authorization/AssignmentController.java`,
  `server/src/main/java/dev/datarun/server/authorization/ScopeResolver.java`,
  `server/src/main/java/dev/datarun/server/config/DeployTimeValidator.java`,
  `server/src/main/java/dev/datarun/server/config/ConfigPackager.java`.
