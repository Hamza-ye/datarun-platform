# ADR-003: Authorization and Selective Sync

> Status: **Decided**
> Date: 2026-04-12
> Promoted: 2026-04-12
> Exploration: [Reading Guide](../exploration/guide-adr-003.md) · Raw: [10](../exploration/archive/10-adr3-phase1-policy-scenarios.md) (Phase 1), [12](../exploration/archive/12-adr3-course-correction.md) (Correction), [11](../exploration/archive/11-adr3-phase2-stress-test.md) (Phase 2)

---

## Context

ADR-001 established immutable events as the storage primitive and reserved envelope space for authority context (S5). ADR-002 established four typed identity categories — including **Assignment**, a temporal binding of actor to scope — and the accept-and-flag conflict model (S14).

ADR-002 explicitly deferred four concerns to ADR-003:

1. What data flows to which device (sync scope)
2. What authorization model governs who can perform which operations
3. What sync topology is used
4. Where projections live (device, server, or both)

Authorization is the least irreversible of the first three ADRs. The storage model (ADR-001) is in every event forever. The identity model (ADR-002) added permanent envelope fields. The authorization model is mostly enforced at sync time by the server — logic that can evolve without migrating stored events or breaking deployed devices. This ADR commits only what must be locked now.

---

## Decision

The platform uses **assignment-based access control**, **sync scope as the offline authorization mechanism**, and **authority-as-projection derived from the assignment timeline**. Four structural constraints (S1–S4) lock what cannot change without data migration. Three strategy-protecting constraints (S5–S7) guard structural invariants through server-side logic. Three initial strategies (S8–S10) document current approaches that can evolve independently.

### Structural Constraints

#### S1: Assignment-Based Access Control

**Every access rule reduces to: "Does the actor have an active assignment whose scope contains the target entity and whose role permits the intended action?"**

The access check is a single scope-containment test: `actor.assignment.scope ⊇ subject.location`. Role qualification is a secondary filter. This is not generic RBAC (roles need scope) and not generic ABAC (the assignment is a structured, typed grant — not open-ended attributes). Specific roles and their permitted actions are deployment configuration (ADR-4).

#### S2: Sync Scope Equals Access Scope

**A device receives exactly the data its actor is authorized to act on — no more, no less.** The server computes the sync payload from the actor's active assignment(s) at sync time.

- The device does not need a policy engine — it works with what it has, and it only has authorized data.
- Data outside the worker's scope is never delivered to their device.
- When scope changes (reassignment), the next sync adjusts the payload.

Device-side "access control" is a UI concern, not a policy enforcement concern.

#### S3: Authority Context Is a Projection, Not an Envelope Field

**The event envelope gains NO new fields from ADR-003.** Authority context — which assignment(s) authorized a given event — is derived at query time from the assignment event timeline, not stored in the event.

This works because assignment events are always on the server before the work events they authorize: assignments are created by online actors, workers receive them via sync, and work events sync back to a server that already has the authorizing assignment(s).

If projection-based authority reconstruction proves insufficient at scale, an `authority_context` field can be added to new events via ADR-001 S5 (envelope extensibility). Old events remain valid — the server reconstructs their authority from the timeline. The retreat path is cheap.

#### S4: Alias-Respects-Original-Scope

**When evaluating authorization for an event, the platform uses the original `subject_ref` as written in the event — not the post-merge surviving subject's scope.**

An event about Subject S1 (in Village X) is authorized against Village X, even if S1 has since been merged into S2 (in Village Y). The merge is an identity resolution, not an authorization grant. This is locked by the event's immutable `subject_ref` — the original reference is what the event contains; evaluation against it is the only correct interpretation. Permanent constraint.

### Strategy-Protecting Constraints

These guard structural invariants through server-side logic. The invariants they protect cannot change; the implementation can evolve.

#### S5: Scope-Containment Invariant on Assignment Creation

**An `AssignmentCreated` command is validated server-side: the new assignment's scope MUST be contained within the creating actor's own assignment scope.** `new_assignment.scope ⊆ creating_actor.assignment.scope`.

Protects against privilege escalation: without this, a compromised coordinator could create assignments spanning the entire hierarchy, causing the sync engine to deliver all data to any device. Enforced the same way ADR-002 S9 enforces acyclicity — server-side precondition validation. Can be relaxed for specific role types (e.g., super-coordinators) without structural changes.

#### S6: Conflict Resolution Is Online-Only

**`ConflictResolved` events can only be created through a server-validated transaction.** Extends ADR-002 S10's precedent (merge/split are online-only) to conflict resolution.

Protects against meta-flag chains: without this, an offline resolver whose authority has changed creates a resolution event that is itself flagged, producing a recursive chain. Can be relaxed for specific low-severity flag types if deployments need field-level resolution.

#### S7: Detect-Before-Act Extends to Authorization Flags

**ADR-002 S12's detect-before-act guarantee extends to ALL flag types, including authorization flags (`ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag`).**

Protects against irreversible downstream damage: without this, a trainee's treatment events (created under a stale role) trigger supply deductions before review. The mechanism is ADR-002 S12; this extends its scope. Which flag types are **blocking** vs. **informational** is deployment-configurable. Initial configuration: `ScopeStaleFlag` informational, `RoleStaleFlag` blocking for capability-restricted actions, `TemporalAuthorityExpiredFlag` informational.

### Initial Strategies (evolvable)

These document current approaches. Each can evolve without affecting stored events.

#### S8: Tiered Projection Location

| Tier | Projection source | Rationale |
|------|-------------------|-----------|
| Field workers | Device-local, from raw events | Small subject set. ADR-001's escape hatch available. |
| Supervisors | Hybrid: raw events for review visits + server-computed summaries | Need both detail and aggregate. Phase 2 validated: 60K events → 60s full rebuild, 0.15s incremental. |
| Coordinators | Server-computed, accessed online | Reliable broadband (C.3). |

Implies direct sync topology (every device syncs with server, no relay through supervisors).

#### S9: Authorization Staleness Handling

Uses the same accept-and-flag mechanism as identity staleness (ADR-002 S14). New flag types: `ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag`. Blocking behavior governed by S7.

Designated resolver follows ADR-002 S11 pattern. When multiple flags affect a single event, all assigned to one resolver (broadest scope). Watermark-based auto-resolution for `ScopeStaleFlag`: events whose `sync_watermark` is older than the triggering `AssignmentEnded` watermark are auto-resolvable as "valid under prior scope."

#### S10: Data Handling on Scope Change

Scope expansion is additive. Scope contraction: **selective retain** — own events retained, others' events about out-of-scope subjects are candidates for removal. Data removal is device-side policy, not sync protocol instruction.

- Non-sensitive data: retain-all acceptable.
- Sensitive personal data: selective purge recommended (crash-safe, journaled).
- Retain-but-hide: **not recommended** for sensitive data (physically accessible on rooted devices).

---

## What This Does NOT Decide

| Concern | Belongs to | Why deferred |
|---------|-----------|--------------|
| Subject-based scope (case management) | ADR-4 / Implementation | New assignment configuration, not structural change. |
| Auditor access (cross-hierarchy, read-only) | ADR-4 / Implementation | New role type + query-based scope — assignment configuration, not structural. |
| Device sharing — multiple actors on one device | Implementation | Device-side app architecture. Per-actor sync sessions fix watermark corruption (Phase 2 Combo β). |
| Sync pagination, priority ordering, bandwidth | Implementation | Protocol optimization. S2 constrains *what* syncs; implementation decides *how*. |
| Assessment visibility to assessed worker | Strategy / ADR-4 | Sync filter configuration (geographic + "about me" dimension). |
| Sensitive-subject classification | ADR-4 | Deployment-specific. Sync scope (S2) can incorporate sensitivity as a filter. |
| Grace period for expired temporal authority | Strategy | Watermark-based auto-resolution (S9) replaces time-window grace periods. |
| Role-action permission tables | ADR-4 | Synced configuration defining which actions each role permits. |
| Cross-level visibility in distribution | Strategy / ADR-4 | Sync scope filter, configurable per deployment. |

---

## Consequences

### What is now constrained

- **ADR-4 (Configuration)**: Must determine how assignments and scope definitions are configured, how role-action permission tables are defined and synced, and how per-flag-type severity (S7) is configured per deployment.

- **ADR-5 (Workflow)**: Workflow state machines operate within assignment scope. Workflow transitions that change authority produce assignment events triggering sync scope adjustments. Conflict resolution workflows must be online-only (S6). Specific items deferred:
  - Cascade behavior when conflict resolution invalidates downstream workflow state
  - How workflow transitions interact with assignment-change events
  - Whether blocking flags (S7) suspend in-progress workflow steps or only prevent new ones
  - Interaction between detect-before-act and multi-step approval chains

- **Event envelope**: Gains **no new fields**. Authority is derived from the assignment timeline. Revisitable via ADR-001 S5.

- **Sync protocol**: Must compute per-actor scope from active assignments, filter events by scope, detect authorization staleness, and enforce detect-before-act for blocking flags. Assignment creation must validate scope-containment (S5).

- **Authorization after merges**: All checks use the original `subject_ref` (S4). Alias resolution is a projection concern, not an authority concern.

### Risks accepted

- **Authority reconstruction may be slow at scale.** Mitigation: per-actor caching (server-side) or adding `authority_context` to envelope (ADR-001 S5). Revisit trigger: >50ms per event attributable to authority reconstruction.

- **Selective-retain (S10) accumulates own-event data.** Revisit trigger: out-of-scope own events exceed 10% of total app storage.

- **Blocking flags (S7) may delay legitimate work.** Revisit trigger: >5% of blocking flags resolved as "valid — should not have been blocked."

- **Supervisor hybrid projection (S8) is least-validated.** Both projection location and summary computation are server-side and evolvable. Revisit trigger: supervisor sync time exceeds 30 seconds for deployments with 100+ supervisors, or projection rebuild cost exceeds 5% of total server CPU.

- **Shared-device causal ordering has known corruption.** Per-actor sync sessions fix this (protocol strategy). Until then, shared-device deployments should document unreliable cross-actor ordering.

- **First-sync on 2G may be prohibitive without priority sync.** Priority sync (recent events first, historical backfill) is server-side optimization, not structural.

### Principles confirmed

- **P1 (Offline is the default)**: No authorization check blocks offline capture. Sync scope = access scope means the device has everything needed.
- **P4 (Composition)**: No new structural mechanisms — authorization composes existing primitives (assignment, accept-and-flag, detect-before-act, projection).
- **P6 (Authority is contextual and auditable)**: Full audit chain: event → actor → assignments at event time → scope validity → flag if stale.
- **P7 (Simplest scenario stays simple)**: S00 adds zero authorization interactions. No new envelope fields.

---

## Traceability

| Sub-decision | Classification | Key forcing inputs |
|---|---|---|
| S1 (assignment-based access) | Structural constraint | Phase 1 EQ-3, ADR-002 S2 |
| S2 (sync = access scope) | Structural constraint | Phase 1 EQ-2, P1, C.4 |
| S3 (no new envelope fields) | Structural constraint | ADR-001 S2/S5, Phase 2 E2/E3/E5 |
| S4 (alias-respects-scope) | Structural constraint | Phase 2 A1 (HS-12), Combo α, ADR-002 S13 |
| S5 (scope-containment on create) | Strategy-protecting | Phase 2 A3, invariant I2 |
| S6 (online-only resolution) | Strategy-protecting | Phase 2 C4, ADR-002 S10 precedent |
| S7 (detect-before-act extension) | Strategy-protecting | Phase 2 C2, ADR-002 S12 |
| S8 (tiered projection) | Initial strategy | Phase 2 performance validation |
| S9 (staleness handling) | Initial strategy | ADR-002 S14 (accept-and-flag) |
| S10 (scope change data handling) | Initial strategy | Deployment sensitivity variance |

---

## Next Decision

**ADR-4: Configuration Boundary.**

ADR-003 established that authorization is assignment-based, scope follows the geographic hierarchy, authority context is derived from the assignment timeline, and assignment creation requires scope-containment validation. ADR-4 must now decide:

1. What is configurable by deploying organizations vs. fixed by the platform (the V2 boundary)
2. How activity-specific data shapes are defined, versioned, and synced to devices
3. Whether scope types (geographic, subject-based, query-based) are platform-fixed or deployment-configurable
4. How event type vocabularies are defined — platform-provided vs. deployment-defined
5. How role-action permission tables are defined and delivered to devices
6. How per-flag-type severity (S7 blocking vs. informational) is configured per deployment
7. Where the boundary sits between "set up" (configuration) and "require platform evolution" (code change)

Inputs: ADR-001 (storage model), ADR-002 (identity model), ADR-003 (this decision), constraints.md (V2: "set up, not built"), viability assessment Risk R1 (configuration boundary collapse), Tension T2 (configuration simplicity vs. expressive power).
