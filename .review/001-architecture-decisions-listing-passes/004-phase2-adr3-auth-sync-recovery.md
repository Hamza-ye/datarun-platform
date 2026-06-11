# Phase 2: ADR-003 Authorization + Selective Sync Recovery

## Context Capsule

- Recovery mode: archaeological reconstruction only; no redesign, no alternatives, no reopening.
- Anchor file: `002-phase0-decision-register.md`.
- Upstream recovered source: `003-phase1-adr2-identity-conflict-recovery.md`.
- ADR target: ADR-003 S1–S10.
- Source files read:
  - `12-adr3-course-correction.md`
  - `10-adr3-phase1-policy-scenarios.md`
  - `11-adr3-phase2-stress-test.md`
- Reading order used: `12 → 10 → 11`.
- Settled outputs:
  - Assignment-based access model and assignment lifecycle semantics.
  - Scope containment semantics.
  - Sync scope as access scope.
  - Authority-as-projection and assignment timeline reconstruction.
  - Rejection reasoning for authority context in the event envelope.
  - Alias-respects-original-scope rule.
  - Scope-containment invariant on assignment creation.
  - Online-only conflict resolution.
  - Authorization flag types and their interaction with detect-before-act.
  - Tiered projection strategy and selective-retain on scope contraction.
- Rejected / excluded:
  - Authority context as event-envelope field.
  - Single `assignment_ref` in the envelope.
  - Variable-length `assignment_refs` list in the envelope.
  - Bounded `primary_assignment_ref` / `secondary_assignment_ref` envelope design.
  - Treating device-local authorization assertions as verified facts.
  - Treating sync scope as independent from access scope.
  - Retain-indefinitely on scope contraction.
  - Retain-but-hide as sufficient for sensitive data.
  - Actor-as-subject visibility as a new settled ADR-003 structural assignment type.
  - Auditor/query-scope access as settled ADR-003 structure.
- Deferred / open evolution:
  - Role-action permission tables → ADR-004.
  - Final platform-fixed scope type vocabulary → ADR-004.
  - Per-flag-type severity configuration → ADR-004.
  - Sensitive-subject classification → ADR-004.
  - Source-only flag cascade / downstream workflow effects → ADR-005.
  - Auto-resolution policy beyond watermark-based scope staleness → ADR-005 / platform evolution.
  - Device shared-storage partitioning → implementation strategy.
- Terms locked in this pass:
  - `assignment-based access`
  - `scope-containment test`
  - `sync scope = access scope`
  - `authority-as-projection`
  - `assignment timeline`
  - `alias-respects-original-scope`
  - `scope-containment invariant`
  - `privilege escalation prevention`
  - `ScopeStaleFlag`
  - `RoleStaleFlag`
  - `TemporalAuthorityExpiredFlag`
  - `tiered projection`
  - `selective-retain`
  - `scope contraction`

---

## 1. ADR Checkpoint

This pass is bounded by ADR-003 S1–S10 from the Phase 0 register.

| ADR-003 ID | Recovery use in this pass |
|---|---|
| S1 | Recover assignment-based access and the single scope-containment test. |
| S2 | Recover why sync scope equals access scope. |
| S3 | Recover why authority context is projection-derived, not stored in the event envelope. |
| S4 | Recover alias-respects-original-scope. |
| S5 | Recover scope-containment invariant on assignment creation. |
| S6 | Recover why conflict resolution is online-only. |
| S7 | Recover authorization flag types and detect-before-act extension to all flags. |
| S8 | Recover tiered projection location. |
| S9 | Recover authorization staleness as accept-and-flag plus watermark-based scope auto-resolution. |
| S10 | Recover selective-retain on scope contraction. |

Everything below maps to one or more of these entries. Items that only appear in exploration but do not map to S1–S10 are marked rejected, provisional, deferred, or implementation strategy.

---

## 2. Upstream Boundary from Phase 1

Phase 1 locked ADR-002 identity and conflict semantics. ADR-003 builds on them but does not redefine them.

Relevant inherited constraints:

| ADR-002 term / rule | How ADR-003 uses it | Boundary |
|---|---|---|
| `actor` | The identity that performs work and receives assignments. | Actor identity is not authorization by itself. |
| `assignment` | A typed identity category available for responsibility grants. | ADR-002 only established the category; ADR-003 recovers its access-control semantics. |
| `subject_ref` | The target of work and scope evaluation. | Authorization must use the original raw subject reference where aliasing exists. |
| `device_id` / `device_sequence` / `sync_watermark` | Inputs to stale-authority detection. | `device_time` remains advisory. |
| `accept-and-flag` | Base model for stale authorization work. | Events are not rejected merely because authority was stale. |
| `detect-before-act` | Base processing guarantee extended to authorization flags. | Policies do not fire on flagged events until resolution if the flag blocks execution. |
| `single-writer resolution` | Conflict has one resolver. | ADR-003 decides resolver authority and constrains resolution to online validation. |
| raw-reference detection | Detection runs before projection aliasing. | ADR-003 adds alias-respects-original-scope for authorization. |

---

## 3. Assignment Model

### 3.1 Definition

An `Assignment` is the atomic authorization grant.

It binds:

- an `actor`;
- a `scope`;
- a `role`;
- a temporal interval;
- optionally an operational process context such as a campaign.

Recovered rule:

```txt
access_allowed(actor, action, target) =
  actor has an active assignment
  whose scope contains the target
  and whose role permits the action
```

ADR-003 S1 locks the model at the level of `assignment-based access` and `scope-containment test`. Role-action permission tables are explicitly deferred to ADR-004.

### 3.2 Assignment lifecycle

| Stage | Semantics | ADR-003 boundary |
|---|---|---|
| Created | A coordinator or authorized administrative actor creates an assignment. | Creation is server-side and must pass the scope-containment invariant. |
| Active | Assignment grants role-scoped authority over a scope. | Device-local checks use locally synced active assignments. |
| Composed | Multiple assignments may be active, e.g. standing + campaign. | Effective scope is additive by assignment composition; final scope-type vocabulary is not locked here. |
| Temporally bounded | Assignment may have `valid_from` / `valid_until` or equivalent effective bounds. | Device checks can warn, but server-side sync validation is authoritative because `device_time` is advisory. |
| Changed | Assignment can be ended, replaced, expanded, contracted, or overlaid. | Scope changes drive sync changes. |
| Stale on device | Offline devices may continue operating under old assignment state. | Events are accepted and flagged on sync. |
| Ended / expired | Assignment no longer grants current access. | Inbound sync contracts; outbound stale work is flagged, not rejected. |

### 3.3 What assignment is not

Assignment is not equivalent to RBAC. A role without scope is insufficient.

Assignment is also not generic ABAC. ADR-003 recovers a structured grant: actor + role + scope + time. The structure is deliberate because offline devices need a small local authorization model that can be reconstructed from events and sync state.

### 3.4 Assignment primitive boundary

| Responsibility | In boundary | Out of boundary |
|---|---|---|
| Grant authority | Binds actor to scope with role and time bounds. | Does not define deployer-authored permission tables. |
| Drive sync | Determines which data the device receives. | Does not itself implement delta sync, pagination, or bandwidth optimization. |
| Support audit reconstruction | Participates in assignment timeline projection. | Does not need to be copied into every event envelope. |
| Support stale-authority detection | Server compares event knowledge state against assignment timeline. | Does not make device-local assertions verified facts. |
| Prevent privilege escalation | New assignments must be contained within creator scope. | Does not encode every future exception such as auditor access in ADR-003. |

---

## 4. Scope Model and Containment Semantics

### 4.1 Settled ADR-003 containment model

ADR-003 locks the containment interface, not every future scope vocabulary detail.

A scope must support this question:

```txt
scope_contains(assignment.scope, target_context) -> boolean
```

Where `target_context` is usually derived from the event’s original `subject_ref` and subject attributes available to projection/sync evaluation.

### 4.2 Containment semantics

| Scope concern | Recovered semantics | Classification |
|---|---|---|
| Geographic responsibility | Baseline scenario: actor assigned to a village, area, district, or organizational node. | Settled as core containment shape in ADR-003 lineage. |
| Subject set / list responsibility | Scenario pressure: responsibility can be over an explicit set of subjects. | Open / later scope vocabulary; containment rule remains the same. |
| Campaign / activity grant | Time-bound additive grant layered over standing scope. | ADR-003 settles additive assignment composition; final activity scope vocabulary is later ADR-004. |
| Actor-as-subject visibility | “Events about me” is a second sync dimension. | Explored; not locked as a new ADR-003 structural assignment type. Can be sync-filter rule or later scope extension. |
| Query/auditor scope | Cross-cutting temporary access. | Explored; deferred. Not settled ADR-003 baseline. |

### 4.3 Scope composition

Settled containment rule:

```txt
actor.effective_scope = union(active_assignment.scope for actor)
```

Access is permitted when at least one active assignment contains the target and the relevant role-action rule permits the action.

However, ADR-003 S3 rejects the idea that the specific assignment(s) authorizing an event must be stored in the event envelope. Authority is reconstructed from the assignment timeline.

### 4.4 Scope containment invariant on assignment creation

ADR-003 S5 locks this rule:

```txt
new_assignment.scope ⊆ creating_actor.effective_scope
```

This is a strategy-protecting invariant. It prevents lateral privilege escalation where a coordinator with authority over one district grants a worker access to another district.

The invariant is server-side. It must be checked before writing assignment-change events.

### 4.5 Alias-respects-original-scope

ADR-003 S4 locks this rule:

```txt
Authorization is evaluated against the original subject_ref as written,
not the alias-resolved surviving subject.
```

Reason:

- ADR-002 merge aliases `retired_id → surviving_id` in projection.
- A worker may have been authorized for the original subject’s scope.
- The surviving subject may belong to a different scope after merge.
- If authorization were evaluated only after alias resolution, a legitimate event could silently project into a scope the actor was never authorized to access.

The original subject reference is immutable. Therefore it is the only stable authorization anchor.

---

## 5. Sync Scope = Access Scope

### 5.1 Forcing function from scenarios

The scenarios forced a shift from “authorization as a local policy engine” to “authorization through scoped sync.”

Recovered rule:

```txt
The device can act on the data it has.
Therefore the server must only sync data the actor is authorized to hold.
```

A field device does not carry the full dataset. It receives the subset computed from the actor’s active assignments. That subset becomes both:

1. the data available for offline work;
2. the effective authorization boundary while offline.

### 5.2 Why this is structural

If an actor is authorized for data but the device does not receive it, offline work fails.

If the device receives data outside the actor’s scope, offline access is already leaked because the data exists locally.

Therefore, ADR-003 S2 locks:

```txt
sync_scope(actor) = access_scope(actor)
```

### 5.3 Consequences

| Consequence                                     | Meaning                                                                                      | Classification                                          |
| ----------------------------------------------- | -------------------------------------------------------------------------------------------- | ------------------------------------------------------- |
| Subject-scoped sync, not author-scoped sync     | A worker receives all events for subjects in scope, regardless of who authored them.         | Settled ADR-003 behavior.                               |
| Sync is server-authoritative                    | Server computes inbound data from current assignment timeline.                               | Settled ADR-003 behavior.                               |
| Device-local checks are lightweight             | Device checks local assignment and local registry; it does not need a general policy engine. | Settled boundary.                                       |
| Scope expansion is additive                     | New assignment grants cause new data to sync.                                                | Settled interaction; delivery optimization is strategy. |
| Scope contraction is subtractive                | Data already on-device must be removed, hidden, or retained selectively.                     | ADR-003 S10 chooses selective-retain baseline.          |
| Supervisor/coordinator projections vary by tier | Different actor tiers need different projection locations.                                   | ADR-003 S8 initial strategy.                            |

### 5.4 Scope expansion vs contraction

Expansion is easy: deliver new assignments and data.

Contraction is harder because the device may already hold data it should no longer expose.

ADR-003 S10 locks the initial strategy:

```txt
scope contraction -> selective-retain
```

Recovered meaning:

- retain events authored by the actor for continuity and audit;
- remove or stop exposing events authored by others for out-of-scope subjects;
- do not treat retain-but-hide as a sufficient security model for sensitive data;
- do not retain all historical scoped data indefinitely.

This is an initial strategy because device storage and purge mechanics can evolve without changing stored events.

---

## 6. Authority Kept Out of the Event Envelope

### 6.1 Provisional envelope idea from Phase 1

Phase 1 discovered a plausible authority context:

```txt
authority_context = actor_ref + assignment_ref(s) + optional process_ref
```

This was provisional.

It was useful because it exposed the problem: campaign, standing, and process-scoped work need authority attribution. But the final ADR-003 position rejects storing that attribution as an event-envelope field.

### 6.2 Options explored

| Option | Status | Reason |
|---|---|---|
| Single `assignment_ref` in envelope | `[REJECTED]` | Cannot represent standing + campaign authority without choosing one and losing provenance. |
| Variable-length `assignment_refs` list | `[REJECTED]` | Creates immutable variable-length envelope schema and forward-compatibility burden. |
| Compound authority context with assignment refs and process ref | `[REJECTED]` | Works functionally, but over-engineers every event for uncommon multi-grant cases. |
| Bounded primary/secondary assignment refs | `[PROVISIONAL → ADR-003 S3 rejects envelope authority]` | Stress test resolution before course correction; final ADR chose no authority envelope field. |
| No `authority_context`; derive on sync | `[SETTLED]` | Authority is projection from assignment timeline; no new envelope fields. |

### 6.3 Final rule: authority-as-projection

ADR-003 S3 locks:

```txt
No authority_context field is added by ADR-003.
Authority is reconstructed from the assignment timeline.
```

The server reconstructs:

```txt
At event creation knowledge-state K,
actor A had active assignment(s) whose scope contained original subject_ref S,
and whose role permitted action X.
```

Inputs:

- `actor_ref` from the event;
- original `subject_ref` from the event;
- `device_id`, `device_sequence`, and `sync_watermark` from ADR-002;
- assignment events for the actor;
- scope model and role-action rules.

### 6.4 Why rejection is boundary-defining

The rejected envelope design taught these boundary rules:

| Rejection lesson | Final boundary |
|---|---|
| Storing assignment refs in every event turns a derived relation into permanent stored state. | Keep authority derived. |
| Device authority context is only an assertion of local belief, not a server-verified fact. | Server validates against assignment timeline on sync. |
| Variable-length assignment lists make immutable envelope compatibility harder. | Do not put assignment lists in envelope. |
| Campaign/standing attribution can be derived from assignment history and activity/process context. | Derivation belongs in projection/sync logic. |
| System-generated events would need special authority handling if authority_context were mandatory. | ADR-004 later handles system actor identity; ADR-003 does not solve it with envelope authority. |

### 6.5 Retreat path

The course correction notes that adding authority fields later would be possible because the envelope is extensible. That is not the settled architecture. It is an evolvability note only.

For this recovery pass, authority fields are not part of the event contract.

---

## 7. Authorization Staleness and Flag Types

### 7.1 Base rule

Authorization staleness uses ADR-002’s accept-and-flag model.

```txt
Offline event created under stale local authority state
  -> accepted on sync
  -> flagged for authorization staleness
  -> policy execution blocked if the flag type/severity requires it
```

### 7.2 Flag categories recovered for ADR-003

| Flag                           | Constitutes                                                                                     | Default recovered semantics                                                                   | ADR anchor                   |
| ------------------------------ | ----------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------- | ---------------------------- |
| `ScopeStaleFlag`               | Actor’s assignment scope changed while device was offline; event was created against old scope. | Usually informational / auto-resolvable when watermark proves actor could not know of change. | S7, S9                       |
| `RoleStaleFlag`                | Actor’s role changed while offline; event used old role capability.                             | Blocking for capability-restricted actions until resolved.                                    | S7, S9                       |
| `TemporalAuthorityExpiredFlag` | Time-bound assignment or campaign expired while device was offline.                             | Stale-authority flag; can be auto-resolved by watermark logic in some cases.                  | S7, S9                       |
| `scope_violation`              | Event is outside valid authorization scope, not merely stale local knowledge.                   | Manual review / stronger authorization flag class.                                            | S7/S9, Phase 0 flag register |

> Name convention established: snake_case, table follows the immutable exploration files naming.

### 7.3 Detect-before-act extension

ADR-003 S7 extends ADR-002 S12:

```txt
Detect-before-act applies to all flag types,
including authorization flags.
```

Reason:

- A stale-role event may trigger downstream policies, such as supply deduction or task creation.
- If policies fire before the authorization flag is detected/resolved, the system creates immutable downstream consequences from potentially invalid work.
- Therefore authorization flags must be generated and classified before policy execution.

### 7.4 Severity is not fully locked in ADR-003

The mechanism is locked:

```txt
blocking flags block policy execution before act.
```

But per-flag severity configuration is deferred to ADR-004. ADR-003 introduces the flag types and the detect-before-act extension; it does not make every authorization flag permanently blocking.

### 7.5 Watermark-based scope auto-resolution

ADR-003 S9 recovers an initial strategy:

```txt
If event.sync_watermark < triggering AssignmentEnded/Changed watermark,
then the actor demonstrably could not have known the scope had changed.
```

Such `ScopeStaleFlag` cases can be auto-resolved as valid under prior scope.

This is platform logic over ADR-002 causal metadata, not a new event-envelope field.

---

## 8. Conflict Resolution Authority

ADR-002 S11 locked one designated resolver per conflict but deferred who that resolver can be.

ADR-003 recovers two constraints:

1. Resolver authority is assignment-derived.
2. `ConflictResolved` creation is online-only.

### 8.1 Resolver authority

Resolver authority is not a separate identity system. It derives from the same assignment model:

```txt
resolver(conflict) = actor with active assignment
  whose scope contains the conflict target
  and whose role permits resolution
```

For cross-scope conflicts, resolver selection must choose an actor whose scope covers all relevant parties. Exact resolver policy remains implementation/configuration, but the authorization basis is assignment-derived.

### 8.2 Online-only resolution

ADR-003 S6 locks conflict resolution as online-only.

Reason:

- Offline conflict resolution under stale resolver authority creates meta-flags: flags on `ConflictResolved` events.
- Meta-flag chains are operationally damaging.
- Online validation terminates the chain by checking resolver authority before writing the resolution.

This extends the ADR-002 precedent that merge/split are online-only server-validated operations.

---

## 9. Tiered Projection Location

ADR-003 S8 locks an initial projection-location strategy:

| Actor tier | Projection location | Recovered semantics |
|---|---|---|
| Field worker | Device | Local projection from synced subject events. Offline work must remain possible. |
| Supervisor | Hybrid | Raw events for drill-down/offline review; server summaries for dashboards and aggregation. |
| Coordinator | Server | Reliable connectivity and larger scope make server-side projections appropriate. |

This is not a structural event-store decision. It is an initial strategy that can evolve without changing stored events.

Boundary:

- ADR-001 says projections are rebuildable.
- ADR-003 decides where projections are normally maintained for different actor tiers.
- Full rebuild is recovery; incremental projection is normal operation.

---

## 10. Primitive Boundaries Recovered from ADR-003

| Primitive / component | Settled responsibility | Explicit non-responsibility |
|---|---|---|
| Assignment Registry | Stores assignment events and builds active assignment timeline. | Does not store authority context in each work event. |
| Authorization Evaluator | Applies scope-containment and role-action check against active assignments. | Does not act as a general deployer-authored policy engine. |
| Sync Scope Resolver | Computes inbound data from assignment-derived access scope. | Does not deliver data outside access scope for later filtering. |
| Authority Projection | Reconstructs authority at sync/projection time from assignment timeline. | Does not rely on device-stored authority assertions as facts. |
| Scope Containment Validator | Enforces `new_assignment.scope ⊆ creating_actor.scope`. | Does not define every possible future scope type. |
| Conflict Resolution Gate | Allows `ConflictResolved` only through online server validation. | Does not permit offline resolver writes as canonical. |
| Authorization Flagger | Emits `ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag`, and related auth flags. | Does not execute downstream policy before flag classification. |
| Projection Tier Manager | Chooses field/supervisor/coordinator projection location strategy. | Does not change the event source of truth. |
| Scope Contraction Handler | Applies selective-retain on contraction. | Does not treat retain-but-hide as sufficient for sensitive data. |

---

## 11. Rejected / Excluded Paths

| Path | Status | Reason |
|---|---|---|
| `authority_context` event-envelope field | `[REJECTED]` | Final ADR-003 S3 keeps authority as projection and adds no envelope fields. |
| Single envelope `assignment_ref` | `[REJECTED]` | Fails standing + campaign composition without losing attribution. |
| Variable-length envelope `assignment_refs` | `[REJECTED]` | Creates permanent variable-length envelope schema. |
| Bounded two-ref authority envelope | `[PROVISIONAL → ADR-003 S3]` | Stress-test result superseded by course correction; final is no authority envelope. |
| Device authority assertion as verified fact | `[REJECTED]` | Device has stale local state; server must re-derive and validate. |
| Sync scope separate from access scope | `[REJECTED]` | Offline devices can access any local data; delivering extra data leaks access. |
| Retain indefinitely after scope contraction | `[REJECTED]` | Device becomes broader data store than current authorization allows. |
| Retain-but-hide as sufficient security | `[REJECTED]` | UI hiding is not durable protection on vulnerable devices. |
| Offline conflict resolution | `[REJECTED]` | Produces stale-authority resolution and meta-flag chains. |
| Actor-as-subject visibility as settled ADR-003 assignment type | `[EXCLUDED / deferred]` | Explored as pressure; final course correction reclassifies as sync filter or later extension, not ADR-003 structural lock. |
| Auditor/query scope as settled ADR-003 type | `[EXCLUDED / deferred]` | Explored as exception; later configuration/scope vocabulary decision. |
| Actor-partitioned local storage as architecture vocabulary | `[EXCLUDED / implementation]` | Device storage schema is implementation, not ADR-003 vocabulary. |

---

## 12. Deferred / Open Evolution

| Item | Classification | Why it remains outside Phase 2 lock |
|---|---|---|
| Role-action permission tables | Expansion of explicitly open front → ADR-004. | ADR-003 says role must permit action; ADR-004 defines configurable tables. |
| Final scope type vocabulary | Expansion of explicitly open front → ADR-004. | ADR-003 locks containment interface, not final platform-fixed type set. |
| Per-flag severity configuration | Expansion of explicitly open front → ADR-004. | ADR-003 extends detect-before-act; deployer policy decides severity within platform rules. |
| Sensitive-subject classification | Expansion of explicitly open front → ADR-004. | Sensitivity affects sync/retention policy, not ADR-003 event structure. |
| Priority sync / pagination / historical backfill | Platform evolution that does not violate accepted decisions. | Sync optimization; stored events unchanged. |
| Batch flag resolution | Platform evolution that does not violate accepted decisions. | Read-model/workflow optimization. |
| Actor-as-subject delivery rule | Work on an underexplored front. | Needed for assessments about actors; can be sync filter or later scope rule. |
| Auditor access | Work on an underexplored front. | Requires cross-cutting read-only temporary scope; not settled in ADR-003. |
| Data purge implementation | Implementation strategy. | Selective-retain is strategy; crash-safe purge mechanics are app implementation. |
| Shared-device local partitioning | Implementation strategy. | Important but not platform vocabulary. |
| Conflict cascade after flag resolution | Expansion of explicitly open front → ADR-005. | Workflow/source-chain behavior belongs to state progression. |

---

## 13. Terms Locked by ADR-003

| Term | Definition | ADR anchor |
|---|---|---|
| `assignment-based access` | Authorization model where an assignment binds actor, role, scope, and time; access reduces to role plus scope containment. | S1 |
| `scope-containment test` | Boolean evaluation that checks whether an assignment scope contains the target subject/context. | S1 |
| `sync scope = access scope` | Server syncs exactly the data the actor is authorized to hold offline. | S2 |
| `authority-as-projection` | Authority is derived from assignment timeline and event context, not stored as envelope field. | S3 |
| `assignment timeline` | Projection of assignment events over time for an actor. | S3 |
| `alias-respects-original-scope` | Authorization uses the event’s original subject reference, not alias-resolved surviving subject. | S4 |
| `scope-containment invariant` | New assignment scope must be contained within creator’s effective scope. | S5 |
| `privilege escalation prevention` | Security effect of enforcing scope containment on assignment creation. | S5 |
| `online-only conflict resolution` | `ConflictResolved` creation must be server-validated online. | S6 |
| `ScopeStaleFlag` | Authorization flag for work created under stale assignment scope. | S7/S9 |
| `RoleStaleFlag` | Authorization flag for work created under stale role capability. | S7/S9 |
| `TemporalAuthorityExpiredFlag` | Authorization flag for work created under expired time-bound authority. | S7/S9 |
| `detect-before-act` for auth flags | Flag generation/classification precedes policy execution for all flag types. | S7 |
| `tiered projection` | Field=device, supervisor=hybrid, coordinator=server projection-location strategy. | S8 |
| `watermark-based auto-resolution` | Auto-resolution strategy where `sync_watermark` proves actor could not know an assignment change. | S9 |
| `selective-retain` | Scope contraction strategy retaining actor-authored history while removing/hiding out-of-scope data authored by others. | S10 |
| `scope contraction` | Reduction of actor’s assignment scope that requires inbound sync reduction and device data handling. | S10 |

---

## 14. Compact Interaction Model

```txt
AssignmentCreated / AssignmentChanged / AssignmentEnded
        │
        ▼
Assignment Timeline Projection
        │
        ├──► Authorization Evaluator
        │       └── role + scope-containment test
        │
        ├──► Sync Scope Resolver
        │       └── device receives authorized data only
        │
        ├──► Authority Projection
        │       └── reconstructs authority for event at sync/projection time
        │
        └──► Scope Contraction Handler
                └── selective-retain when access narrows

Incoming Event
        │
        ▼
Use original subject_ref + actor_ref + sync_watermark
        │
        ▼
Authorization Flagger
        │
        ├── ScopeStaleFlag
        ├── RoleStaleFlag
        └── TemporalAuthorityExpiredFlag
        │
        ▼
Detect-before-act gate
        │
        ├── if blocking flag unresolved: no downstream policy execution
        └── if clean / resolved / informational: projection and policies continue per configuration
```

---

## 15. Phase 2 Closure

ADR-003 recovers one central architectural position:

> Authorization is an assignment-timeline projection that controls sync scope; it is not an event-envelope assertion.

The settled model is deliberately small:

- assign actors to scopes with roles and time bounds;
- compute access through a containment test;
- sync only authorized data;
- reconstruct authority from the assignment timeline;
- accept stale offline work and flag it;
- block downstream policy execution through detect-before-act when authorization flags require it;
- keep conflict resolution online-only;
- manage contraction through selective-retain.

The remaining complexity is intentionally pushed to ADR-004 configuration policy and ADR-005 workflow behavior, not silently absorbed into ADR-003.
