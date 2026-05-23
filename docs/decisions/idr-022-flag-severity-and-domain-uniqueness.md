---
id: idr-022
title: Flag Severity + Domain Uniqueness
status: active
date: 2026-05-22
phase: 4-prep
type: decision
reversal-cost: medium
touches: [server/config, server/integrity, server/projection, mobile/data, mobile/domain, contracts]
superseded-by: ~
evolves: ADR-003 S7, ADR-004 S14, ADR-005 S3
commit: ~
tags: [configuration, flags, conflict, uniqueness, phase-4]
---

# Flag Severity + Domain Uniqueness

## Context

ADR-003 extends detect-before-act to authorization flags and says blocking vs informational behavior is deployment-configurable. ADR-004 places flag severity at Layer 0 and domain uniqueness constraints at Layer 1. IDR-017 and IDR-019 already carry the Phase 4 stubs: `shapes[*].uniqueness` and top-level `flag_severity_overrides`. ADR-005 fixes resolvability as platform policy: `manual_only` vs `auto_eligible` is not deployer-configurable.

Phase 4 prep has now separated the adjacent concerns. IDR-020 owns pattern state and `transition_violation`. IDR-021 owns role-action enforcement and the revised `role_stale` semantics. FP-005 remains routed but unresolved for `ongoing_resolution` subject-history backfill; this IDR must not turn live sync into backfill or audit pull.

## Decision

### Flag Severity Authoring

Flag severity is Layer 0 deployment configuration, delivered through the existing IDR-019 package key:

```json
{
  "flag_severity_overrides": {
    "role_stale": "blocking",
    "temporal_authority_expired": "informational",
    "domain_uniqueness_violation": "blocking"
  }
}
```

The only valid severity values are `blocking` and `informational`. The map is flat and deployment-wide in Phase 4. It may mention only existing flag categories from `contracts/flag-catalog.md`; the reserved slot cannot be addressed. Missing entries use platform defaults.

Initial platform defaults:

| Flag category | Default severity |
|---------------|------------------|
| `concurrent_state_change` | `blocking` |
| `stale_reference` | `informational` |
| `identity_conflict` | `blocking` |
| `scope_violation` | `blocking` |
| `temporal_authority_expired` | `informational` |
| `role_stale` | `blocking` |
| `domain_uniqueness_violation` | `blocking` |
| `transition_violation` | `informational` |

Severity is separate from resolvability:

- `manual_only` vs `auto_eligible` is platform-defined by ADR-005 S3 and cannot be overridden by deployers.
- Severity controls how unresolved flags affect operational workflow and review priority.
- Resolvability controls whether a system auto-resolution policy may close the flag.
- A flag can be `manual_only` and `informational`, or `auto_eligible` and `blocking`, if a later deployment default or override chooses that combination.

`blocking` means unresolved flags must prevent downstream policy execution and user-facing workflow decisions that depend on the flagged event or its source chain. `informational` means the flag remains visible in timelines, audit surfaces, and review queues but does not by itself block unrelated work from continuing.

Severity does not change the canonical projection rule for uncertain events. Until a flag is resolved, flagged source events remain excluded from authoritative state derivation wherever detect-before-act applies, including subject projection and pattern-state projection. This preserves ADR-005 S2 and the current category-agnostic exclusion rule. Severity affects operational gating around the flag, not whether unresolved uncertain data becomes authoritative state.

Phase 4 does not introduce per-activity severity overrides. The package remains a deployment-wide map because flags can be cross-activity through source chains, identity aliases, and shared subjects; nested per-activity severity would make review behavior depend on context instead of flag meaning. If real deployments need finer policy later, a successor IDR may add an explicit per-activity or per-rule severity surface.

### Domain Uniqueness Constraints

Domain uniqueness remains a shape-declared Layer 1 constraint. Phase 4 interprets the existing `uniqueness` stub as a declarative constraint object on a shape version. It is not an expression, trigger, role-action rule, or pattern transition.

The Phase 4 schema should support at least this shape:

```json
{
  "scope": ["subject_ref", "activity_ref"],
  "period": {
    "type": "calendar_week",
    "timezone": "deployment"
  },
  "device_action": "warn"
}
```

`scope` is a list of platform-understood key dimensions such as `subject_ref`, `activity_ref`, or `payload.<field_name>`. Payload fields used in the scope must exist on the same shape version and must be scalar. No expressions, conditionals, cross-subject queries, or custom containment logic are allowed in uniqueness definitions.

`period` is optional. Without a period, uniqueness is lifetime-scoped for the chosen key. With a period, the server derives a deterministic window from the event timestamp using the declared period and deployment timezone. Phase 4 should start with calendar periods needed by the initial workflows, not an open-ended temporal language.

`device_action` replaces the ambiguous ADR-004/IDR-017 word `action` for Phase 4 implementation. It is an optimistic device UX hint only, with values such as `warn` or a stricter local confirmation mode. It does not reject structurally valid events, does not alter server authority, and does not override `flag_severity_overrides`. The server always uses accept-and-flag for domain uniqueness anomalies.

The server is authoritative. The device may check uniqueness against locally synced, locally accepted events to reduce avoidable flags, but offline devices may be missing remote events and must treat the result as advisory. A device warning or lack of warning has no evidentiary effect on the server detector.

### `domain_uniqueness_violation`

A server-side uniqueness detector emits `domain_uniqueness_violation` when a structurally valid event would create a duplicate under its shape's declared uniqueness constraint.

The flag is emitted as the standard event-stream flag representation:

- envelope `type = "alert"`;
- `shape_ref = "conflict_detected/v1"`;
- payload `flag_category = "domain_uniqueness_violation"`;
- resolvability `manual_only`.

The flag targets the incoming conflicting event, not every event in the duplicate set. Prior accepted events remain authoritative unless separately flagged for their own reasons. Resolution is manual; `accepted` admits the event into projections despite the uniqueness conflict, while `rejected` keeps it excluded. `reclassified` remains limited to identity-conflict handling unless a later decision broadens resolution semantics.

The flag payload should be sufficient for review without adding envelope fields. It should include:

- `source_event_id`, following `conflict_detected/v1`, for the targeted conflicting event;
- `constraint_ref`, stable within the shape version;
- `shape_ref` and `activity_ref` of the target event;
- a normalized uniqueness key, redacted or hashed if it would expose sensitive payload values;
- period/window metadata when a period is declared;
- `conflicting_event_ids` visible to the resolver under current scope;
- a detector version or rule version for audit/debugging.

Domain uniqueness checks use identity-normalized projection semantics where appropriate, but they do not change authorization semantics. ADR-003 S4 still governs authorization against the event's original `subject_ref`; uniqueness can use alias-aware subject equivalence because it is a domain data-quality check, not an access grant.

### Detection Ordering

For Phase 4 push processing, server detection order is:

1. structural envelope and shape validation before persistence;
2. identity/lifecycle checks;
3. authorization checks, including IDR-021 role-action checks;
4. domain uniqueness checks;
5. IDR-020 pattern transition checks.

This order keeps the concerns separate. Identity and authorization anomalies are discovered before domain policy. Domain duplicates are discovered before pattern transition evaluation so an unresolved duplicate does not advance state and then create misleading transition results. Pattern evaluation must ignore events with unresolved prior flags when deriving current state, consistent with IDR-020 and ADR-005 S2.

The detector may emit multiple flags for one event if multiple independent anomalies apply, but Phase 4 should avoid deriving later checks from state that already includes the unresolved event being checked.

## Alternatives Rejected

- **Per-activity severity in Phase 4** - conflicts with IDR-019's flat package surface and makes a single flag category behave differently depending on activity context. Keep this as a successor-IDR growth path.
- **Make uniqueness `action` the authoritative severity** - moves severity into L1 shape authoring and contradicts ADR-004's Layer 0 severity surface. Shape uniqueness may guide device UX, but server severity comes from category defaults plus `flag_severity_overrides`.
- **Reject server-side uniqueness violations** - violates ADR-006 S1 and the accept-and-flag model. A duplicate is a state anomaly, not a malformed envelope.
- **Implement uniqueness as an L3 trigger** - triggers create new events; uniqueness is a detector over incoming events. Treating it as L3 would blur conflict detection and policy automation.
- **Fold domain uniqueness into pattern transition rules** - uniqueness is about duplicate facts under shape-declared keys. Pattern transitions are about valid state movement under IDR-020.
- **Fold severity into resolvability** - they answer different questions: operational effect while unresolved vs who or what may resolve the flag.
- **Use subject-history backfill to support uniqueness** - FP-005 remains scoped to `ongoing_resolution` history/backfill. Uniqueness detection reads the server event store during push and does not change live sync pull semantics.

## Phase 4 Quality Gates

- Config packaging gate: `flag_severity_overrides` is populated from L0 deployment config, delivered atomically, and rejected if it references unknown categories, reserved slots, or invalid severity values.
- Severity/resolvability gate: tests prove that changing severity does not change `manual_only` vs `auto_eligible` eligibility.
- Deployment-wide gate: Phase 4 accepts the flat severity map and rejects per-activity nested severity config.
- Shape validation gate: DtV rejects uniqueness scopes that reference unknown fields, non-scalar fields, unsupported period values, or expression-like constructs.
- Device optimism gate: device-side uniqueness warnings can be produced from local data, but a server-side duplicate missed by the device still produces `domain_uniqueness_violation`.
- Server authority gate: the same duplicate event is accepted, persisted, and flagged; it is not rejected at sync.
- Ordering gate: identity/auth checks run before uniqueness, uniqueness runs before pattern transition checks, and unresolved flagged events do not advance pattern state.
- Projection gate: an unresolved `domain_uniqueness_violation` excludes only the targeted conflicting event from authoritative projections; resolving it as accepted re-derives projections including the event.
- Boundary gate: no IDR-022 implementation changes `/api/sync/pull` into subject-history backfill or audit/historical pull.

## Consequences

- IDR-022 closes the remaining Phase 4 prep decision surface for severity and domain uniqueness. The next document should be the Phase 4 implementation spec.
- `flag_severity_overrides` remains a stable top-level package key and becomes populated in Phase 4.
- `shapes[*].uniqueness` becomes an active L1 shape contract, but its device action is advisory and its server behavior is always accept-and-flag.
- `domain_uniqueness_violation` is manual-only and separate from role-action, transition, and subject-history backfill behavior.
- The flag catalog points category 7 at this IDR after Phase 4.3. Resolver routing remains `TBD` until a dedicated resolver-routing decision or implementation slice lands.

## Traces

- ADR: [ADR-003 S7](../adrs/adr-003-authorization-sync.md), [ADR-004 S9/S14](../adrs/adr-004-configuration-boundary.md), [ADR-005 S2/S3](../adrs/adr-005-state-progression.md), [ADR-006 S1-S4](../adrs/adr-006-flag-semantics.md)
- IDR: [IDR-017](idr-017-shape-storage.md), [IDR-019](idr-019-config-package.md), [IDR-020](idr-020-pattern-state-machine-representation.md), [IDR-021](idr-021-role-action-enforcement-model.md)
- Register: [FP-005](../flagged-positions.md)
- Contract: [contracts/flag-catalog.md](../../contracts/flag-catalog.md)
