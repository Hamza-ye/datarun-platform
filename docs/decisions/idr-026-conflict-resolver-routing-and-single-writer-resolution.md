---
id: idr-026
title: Conflict resolver routing and single-writer resolution
status: active
date: 2026-05-24
phase: 4-prep
type: decision
reversal-cost: high
touches: [server/integrity, server/authorization, server/admin, server/projection, contracts]
superseded-by: ~
evolves: ADR-002 S11, ADR-003 S6/S7/S9, ADR-007 S2-S5, ADR-008 S2/S4, ADR-009 S2-S4, IDR-020, IDR-022
commit: ~
tags: [conflict, authorization, resolver-routing, phase-4]
---

# Conflict Resolver Routing and Single-Writer Resolution

## Context

FP-009 records a live drift between ADR-002 S11 and the current implementation.
ADR-002 S11 requires every `conflict_detected/v1` flag to designate exactly one
resolver identity, and only a `conflict_resolved/v1` authored by that resolver is
canonical. ADR-003 S6 makes conflict resolution online-only. ADR-003 S9 also
requires multiple flags on one source event to converge to one resolver.

The current code only partially carries this metadata. Some detector flags omit
`designated_resolver`, `domain_uniqueness_violation` has no resolver route, and
the production conflict API still accepts body-supplied actor IDs. This is
acceptable only as pre-FP-009 debt. It must be resolved before adding
`transition_violation`, conflict-resolution authority enforcement, or
auto-resolution.

This decision does not implement runtime code. It defines the route the next
coding pass must implement.

## Decision

### Resolver Field

Every new `conflict_detected/v1` emitted after the FP-009 implementation pass
must include:

```json
{
  "designated_resolver": {
    "type": "actor",
    "id": "<uuid-or-system-actor-id>"
  }
}
```

Human resolvers use their actor UUID as `id`. System resolvers use ADR-008 S2's
system actor form, `system:{source_type}/{source_id}`.

The detector author remains separate from the resolver. A flag emitted by
`system:conflict_detector/domain_uniqueness_violation` may designate a human
activity supervisor as resolver. Code must not infer resolver identity from the
flag event's `actor_ref`.

`contracts/shapes/conflict_detected.schema.json` is not changed by this
decision. `designated_resolver` is now a semantic requirement for new emitted
flags, but making it JSON-Schema-required is deferred to FP-010 so this decision
does not take partial platform-bundled payload shape contract hygiene.

### System Actor IDs

The following system actor IDs are reserved by this decision:

| Use | Actor ID |
|-----|----------|
| Detector authorship for flags | `system:conflict_detector/{flag_category}` |
| Auto-resolution for stale references, when implemented and active | `system:auto_resolution/stale_reference_alias_v1` |
| Auto-resolution for temporal authority overlap, when implemented and active | `system:auto_resolution/temporal_authority_prior_scope_v1` |
| Auto-resolution for late-valid transition evidence, when implemented and active | `system:auto_resolution/transition_late_confirmation_v1` |
| Explicit no-human-route sentinel for one category | `system:resolver_unassigned/{flag_category}` |
| Explicit no-human-route sentinel when one source event has multiple categories and no common resolver | `system:resolver_unassigned/multiple_flags` |

The `resolver_unassigned` actors are not fallback resolvers. They are a hard
stop that makes the flag visible but non-canonical-resolvable until a future
resolver reassignment surface exists. This preserves ADR-002 S11 without
silently granting authority to a random broader actor.

Production APIs must not let callers impersonate a `resolver_unassigned` actor
to clear the flag. The sentinel is a holding designation for orphaned routing,
not a system-authorized review decision.

### Auto-Eligible Does Not Mean System-Owned

`auto_eligible` means a category may be closed by an auto-resolution policy if
an active policy exists. It does not itself designate the system as resolver.

A system-authored `conflict_resolved/v1` is canonical only when the target flag
already designates the exact `system:auto_resolution/{policy_id}` actor. If a
flag was routed to a human resolver and a later auto policy wants to resolve it,
the platform must first create an explicit resolver reassignment event. No
implicit system takeover is allowed.

Before auto-resolution exists, `stale_reference`,
`temporal_authority_expired`, and `transition_violation` are routed to their
human resolver policies below unless no human route exists, in which case they
use the explicit `resolver_unassigned` sentinel.

### Resolver Identity Function

The resolver identity function is computed per `source_event_id`, not per
detector module. It uses the source event, the category-specific context, and
current assignment state at detection time.

Candidate human resolvers must satisfy all of these constraints:

| Constraint | Rule |
|------------|------|
| Stewardship tier | The candidate is an oversight/stewardship actor for the affected work, not merely a field/capture actor with visibility. Scope alone is necessary but not sufficient. A source-event actor is not eligible to resolve a flag about their own ordinary work unless the category route explicitly identifies them as a coordinator/steward and the authenticated actor has that authority. |
| Subject context | The candidate has current assignment authority over the source event's subject context. For authorization checks, the original `subject_ref` is authoritative; alias normalization may help find current stewardship but cannot expand access beyond ADR-003 S4. |
| Activity context | If the source event has `activity_ref`, the candidate assignment is activity-unrestricted or includes that activity. If the source event has `activity_ref = null`, an activity-restricted assignment does not qualify for ordinary work-event resolution. |
| Conflict context | For flags involving other events, such as domain uniqueness, the candidate can see and act on the source event and the relevant conflicting event set visible to resolution. |
| Online authority | Resolution is online-only. Candidate selection is server-side and must not depend on request-body actor IDs. |

When several candidate humans qualify, the chosen resolver is the nearest
eligible common steward: the least-broad single actor assignment that covers all
contexts for the source event and any implicated events. This keeps routine
data-quality and workflow conflicts with the local supervisor/steward instead
of routing them to national/root actors simply because those actors also have
scope.

If no least-broad common steward exists at the immediate scope, routing walks
upward until one eligible actor covers every relevant geographic, subject-list,
and activity axis. Implementations must use a deterministic tie-breaker, such
as actor UUID, after equivalent scope breadth and eligibility are equal.

The older "broadest scope" shorthand in ADR-003 S9 is interpreted here as
"broad enough to cover every implicated context," not "the least restricted
actor in the deployment." A conflict about one village should go to the village
or district supervisor who covers that village, not to a national coordinator,
unless the conflict actually spans scopes that require national visibility.

When several flags target the same source event in one processing pass, all of
those flags must carry the same `designated_resolver`. If a later detector adds
another unresolved flag for a source event that already has an unresolved flag,
it must copy the existing resolver unless a future explicit reassignment event
has changed it. If candidate policies disagree, choose the nearest common
eligible steward that covers all category contexts. If no common resolver
exists, use `system:resolver_unassigned/multiple_flags`.

### Category Routing

The prior `system` entries in `contracts/flag-catalog.md` for manual categories
were stale shorthand. System ownership is explicit only for active auto policies
or for the unassigned sentinel.

| Category | Resolver route | Catalog correction |
|----------|----------------|--------------------|
| `concurrent_state_change` | Human source-event resolver for the subject and activity context. If concurrent events span distinct visible contexts, choose the nearest common steward who can inspect both sides. | Existing `system` entry is stale shorthand. |
| `stale_reference` | Human identity/source-event resolver for the original subject context unless an active auto policy designates `system:auto_resolution/stale_reference_alias_v1`. | Existing `system` entry is stale shorthand unless that exact auto policy is active. |
| `identity_conflict` | Human identity resolver. A manual flag creator may be the designated resolver only when the authenticated creator is eligible for the source subject context; otherwise route through the normal identity resolver function. | Existing `system` entry is stale shorthand. |
| `scope_violation` | Nearest common human steward with current authority over the source event's subject/activity context and the implicated stale/out-of-scope assignment context. | Existing "broadest-scope actor" entry meant common coverage; IDR-026 makes it least-broad eligible common stewardship. |
| `temporal_authority_expired` | Same nearest common human steward as scope violations unless an active auto policy designates `system:auto_resolution/temporal_authority_prior_scope_v1`. | Existing "broadest-scope actor" entry meant common coverage; auto eligibility does not imply system ownership. |
| `role_stale` | Human activity supervisor or role-authority steward for the actor's source subject/activity context. In current assignment terms, this is the nearest current human steward for that actor's covering activity scope. | Existing "supervisor actor" entry is directionally correct but must account for activity scope. |
| `domain_uniqueness_violation` | Human domain/activity data steward who can see the incoming source event and the relevant `conflicting_event_ids`. This is product-review work, not system cleanup. | Replaces `TBD`. |
| `transition_violation` | Human workflow/activity supervisor for the subject pattern instance before auto-resolution exists. If a later active auto policy owns a deterministic transition case, it may designate `system:auto_resolution/transition_late_confirmation_v1` for new flags. | Replaces `TBD`; auto eligibility does not imply system ownership. |

### Canonical Resolution

A `conflict_resolved/v1` is canonical for a flag only if all of these are true:

1. The resolution event has `payload.flag_event_id` equal to the flag event ID.
2. The target flag is a `conflict_detected/v1` event.
3. The target flag has exactly one `payload.designated_resolver`.
4. The resolution event's `actor_ref.type` and `actor_ref.id` exactly match the
   target flag's `designated_resolver.type` and `designated_resolver.id`.

Projection, flag lists, and "already resolved" checks must count only canonical
resolutions. A non-designated `conflict_resolved/v1` is persisted for audit but
does not clear the original flag, does not admit the original source event into
state, and does not block the designated resolver from resolving later.

### Unauthorized Resolution Attempts

A non-designated `conflict_resolved/v1` is itself an authorization anomaly. The
server accepts it, then emits a deterministic `scope_violation` flag targeting
the unauthorized resolution event as its `source_event_id`.

This uses the existing flag catalog. No new `unauthorized_resolution` category
is introduced. This is a resolver-authority subcase of `scope_violation`: the
actor may be able to see the subject but is outside the single-writer resolution
scope for this flag. The unauthorized-resolution flag's `designated_resolver`
is the original flag's designated resolver. If the original flag has no
resolver, the unauthorized-resolution flag uses the same `resolver_unassigned`
sentinel.

This avoids recursive ambiguity:

- the original flag remains unresolved until its designated resolver authors a
  canonical `conflict_resolved/v1`;
- the unauthorized resolution flag targets the unauthorized resolution event,
  not the original domain event;
- resolving or rejecting the unauthorized-resolution flag never clears the
  original flag.

This does not contradict ADR-005 S7 source-only flagging. The platform is not
propagating a root flag from the original source event to a downstream event.
The unauthorized `conflict_resolved/v1` is the root event of a new
authorization anomaly required by ADR-002 S11. No additional flags are created
for events merely because they descend from either the original flag or the
unauthorized-resolution flag.

### API and Auth Binding

Production `/api/conflicts/**` endpoints must become bearer-token actor-bound in
the FP-009 implementation pass.

`POST /api/conflicts/{flagId}/resolve` must take the acting resolver from
authenticated request context. A request-body `actor_id` is a legacy field only;
it must not influence authority. The implementation may reject it or ignore it,
but tests must prove spoofed body actor IDs do not grant resolution authority.

`POST /api/conflicts/identity` must also take the flagging actor from
authenticated request context. The authenticated actor is the flag author. The
designated resolver is then produced by the category routing function above, not
blindly trusted from the request body.

`GET /api/conflicts` should return the authenticated actor's resolvable queue by
default: unresolved flags whose `designated_resolver.id` matches the actor ID.
Broader admin list behavior requires a future production admin/root auth model.

The HTML `/admin/flags` surface remains development-only until production admin
authentication exists. It may bind to a fixed development actor for local manual
testing, but that actor is not production authority and tests must not cite the
HTML surface as the production authorization model.

### Resolver Reassignment

Resolver reassignment is explicitly deferred. This decision does not introduce
a new event shape, envelope field, or admin command for reassignment.

If the designated resolver is unavailable, the flag remains assigned. There is
no implicit fallback to a supervisor, root actor, system actor, or request-body
actor. A successor decision must define the reassignment event surface before
the platform can move an unresolved flag from one resolver to another.

## Alternatives Rejected

- **Keep `system` as shorthand for "review queue"** - violates ADR-002 S11
  because `system` is not an actor identity and cannot author a human judgment.
- **Let any scoped admin resolution clear a flag** - reopens the meta-conflict
  ADR-002 S11 was written to prevent.
- **Use request-body actor IDs for conflict resolution** - repeats the FP-008
  assignment-command bug on the conflict surface.
- **Treat `auto_eligible` as automatic system designation** - would strand flags
  before auto policies exist and would let future policies take over
  human-designated flags without reassignment.
- **Add a new `unauthorized_resolution` category** - the catalog has a reserved
  growth slot requiring ADR-level authority. Existing `scope_violation` covers
  resolver-authority violations as a scoped authorization subcase.
- **Make `designated_resolver` schema-required in this pass** - correct in
  principle, but doing so touches `contracts/shapes/*` and should be handled
  with FP-010's payload-shape parity/loading gate.

## Implementation Checklist

- Add a resolver-routing component used by all conflict detectors.
- Update `ConflictDetector` so `concurrent_state_change`, `stale_reference`,
  `scope_violation`, `temporal_authority_expired`, and `role_stale` always emit
  one `designated_resolver`.
- Update `DomainUniquenessDetector` so `domain_uniqueness_violation` emits a
  domain/activity resolver with visibility to the conflicting set.
- When `transition_violation` is implemented later, route it through IDR-026
  before emitting any flags.
- Change canonical resolution checks to require resolver actor equality.
- Make non-designated resolution events persist but fail to clear the original
  flag.
- Emit deterministic `scope_violation` flags for non-designated resolution
  events.
- Bind `/api/conflicts/**` to bearer-token actor context in `WebConfig`.
- Remove conflict-resolution authority from request-body `actor_id`; tolerate
  or reject the legacy field only as non-authority input.
- Keep `/admin/flags` documented and tested as development-only until
  production admin auth lands.
- Update `contracts/flag-catalog.md` resolver wording to point to this IDR.
- Do not implement auto-resolution, resolver reassignment, or
  `transition_violation` detection in the FP-009 enforcement pass unless a
  separate implementation slice explicitly takes them.
- Do not touch `contracts/shapes/*` unless deliberately taking FP-010.

## Test Matrix

| Area | Required coverage |
|------|-------------------|
| Category routing | One test per active category proving emitted flags include exactly one `designated_resolver` and the route matches this IDR. |
| Domain uniqueness | Duplicate event flag designates a human domain/activity resolver who can see the source and conflicting events. |
| Multi-flag convergence | One source event with multiple flags gets the same resolver on every flag. |
| Authorized resolution | A `conflict_resolved/v1` authored by the designated resolver clears the flag and re-derives projections according to the supported resolution semantics. Pattern projections re-admit through canonical `accepted`; `reclassified` remains identity-conflict subject-attribution behavior unless a later decision broadens it. |
| Unauthorized resolution | A non-designated resolver's `conflict_resolved/v1` is persisted, does not clear the original flag, and creates a `scope_violation` flag on the resolution event. |
| API auth | `/api/conflicts/**` rejects missing/invalid bearer tokens on production endpoints. |
| Body spoofing | A request-body `actor_id` cannot make a non-designated bearer actor canonical. |
| Legacy missing resolver | A flag with no `designated_resolver` is not silently resolved as canonical by production paths. |
| Dev admin boundary | HTML admin flag resolution remains documented as development-only and is not treated as production actor binding. |
| Auto eligibility | `auto_eligible` categories route to humans before auto policies exist; no system-authored canonical resolution occurs without exact system designation. |
| Contract boundary | No new envelope `type`, no new envelope field, no durable workflow-state table, and no `contracts/shapes/*` mutation unless FP-010 is intentionally taken. |

## Deferred Items

- Resolver reassignment event surface. A successor decision must define the
  event shape, admin API, authorization, and projection behavior.
- Auto-resolution policy mechanics. This IDR reserves system actor IDs and
  canonicality rules, but does not implement any policy.
- JSON Schema tightening for `designated_resolver`. FP-010 owns platform
  payload shape parity and should make this contract required when it closes.
- Production admin/root auth. Until then, broad admin views and HTML admin
  actions remain development-only.

No unresolved question blocks the next FP-009 coding pass. The remaining work
above is implementation or deliberately deferred successor scope.

## Consequences

- FP-009 is routed but remains open until runtime emission, enforcement, API
  auth, and tests land.
- `domain_uniqueness_violation` and future `transition_violation` now have
  explicit human resolver routes.
- Existing flag-catalog `system` entries for manual review categories are
  corrected as stale shorthand.
- Auto-resolution cannot bypass S11. A system actor can clear a flag only when
  the flag designates that exact system actor.
- The next coding pass can enforce single-writer resolution without changing
  envelope fields or taking FP-010.

## Traces

- ADR: [ADR-002 S11](../adrs/adr-002-identity-conflict.md),
  [ADR-003 S6/S7/S9](../adrs/adr-003-authorization-sync.md),
  [ADR-007 S2-S5](../adrs/adr-007-envelope-type-closure.md),
  [ADR-008 S2/S4](../adrs/adr-008-envelope-reference-fields.md),
  [ADR-009 S2-S4](../adrs/adr-009-platform-fixed-vs-deployer-configured.md)
- IDR: [IDR-020](idr-020-pattern-state-machine-representation.md),
  [IDR-022](idr-022-flag-severity-and-domain-uniqueness.md),
  [IDR-024](idr-024-multi-axis-assignment-containment.md)
- Register: [FP-009](../flagged-positions.md#fp-009--conflict-resolver-designation-and-single-writer-resolution-enforcement),
  [FP-010](../flagged-positions.md#fp-010--platform-bundled-payload-shape-contract-parity)
- Contract: [contracts/flag-catalog.md](../../contracts/flag-catalog.md)
