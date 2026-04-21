# Cross-Cutting Concerns

> 8 concerns that span multiple primitives and cannot be fully described within any single view. Each is traced to decided positions and decomposed into the contracts that provide its guarantees.

---

## 1. Event Envelope

The 11-field envelope is the universal contract between every primitive. Every component reads it; only the Event Store writes it.

| Field | Type | Presence | Source | Primary consumers |
|-------|------|----------|--------|-------------------|
| `id` | UUID | Mandatory | [1-S3], [1-S5] | All (global unique reference) |
| `type` | enum(6) | Mandatory | [4-S3] | Trigger Engine (processing behavior), Conflict Detector |
| `shape_ref` | `{name}/v{N}` | Mandatory | [4-S1] | Shape Registry (validation), Projection Engine (multi-version routing) |
| `activity_ref` | `[a-z][a-z0-9_]*` | Optional | [4-S2] | Scope Resolver (activity filtering), Pattern Registry (pattern binding) |
| `subject_ref` | `{type, id}` | Mandatory | [2-S2] | Projection Engine (subject grouping), Identity Resolver (alias), Scope Resolver (authorization) |
| `actor_ref` | `{type, id}` | Mandatory | [2-S2] | Scope Resolver (authorization), audit trail |
| `device_id` | UUID | Mandatory | [2-S5] | Identity Resolver (device provenance), sync protocol |
| `device_seq` | integer | Mandatory | [2-S1] | Causal ordering (intra-device), conflict detection |
| `sync_watermark` | version | Mandatory | [2-S1] | Causal ordering (cross-device concurrency), auto-resolution |
| `timestamp` | datetime | Mandatory | [2-S3] | Advisory only — display and audit. No ordering or correctness depends on it. |
| `payload` | object | Mandatory | [1-S5] | Shape-specific data (validated against `shape_ref`) |

**Type vocabulary** [4-S3], [E2]: Platform-fixed, closed, append-only. 6 initial types:

| Type | Processing behavior |
|------|-------------------|
| `capture` | Primary data collection |
| `review` | Judgment on a prior event |
| `alert` | System- or trigger-generated notification |
| `task_created` | Work item assignment |
| `task_completed` | Work item resolution |
| `assignment_changed` | Scope/role modification |

Types represent processing behavior, not domain meaning. Domain meaning lives in shapes [4-S3].

**Envelope finalization** [E1]: Five ADRs added fields (ADR-1: 4 fields, ADR-2: +5 fields, ADR-3: +0, ADR-4: +2, ADR-5: +0) reaching 11 total. The extensibility clause [1-S5] was never invoked. The envelope is architecturally stable.

---

## 2. Accept-and-Flag Lifecycle

The universal anomaly handling mechanism [E3]. All deviations from expected state — identity, authorization, state transitions, domain rules — are surfaced as flags on events, never cause rejection.

**Pipeline** (each stage is a contract guarantee):

| Stage | What happens | Contract |
|-------|-------------|----------|
| Event persisted | Event written regardless of anomaly | [C3]: ES accepts all events |
| State available | Projected state ready for evaluation | [C4]: PE → CD |
| Detection | Anomaly identified, flag created with designated resolver | [C8]: CD → PE |
| Exclusion | Flagged event visible in timeline but excluded from state derivation | [C8]: CD → PE, [5-S2] |
| Policy gating | Flagged event does not trigger policies | [C9]: CD → TE, [2-S12] |
| Auto-resolution | `auto_eligible` flags watched for enabling events within time window | [C9]: CD → TE, [5-S9] |
| Resolution persisted | `ConflictResolved` event created (online-only) | [C21]: TE → ES (auto), [3-S6] (manual) |
| State re-derived | Post-resolution: state re-derived including the previously-excluded event | [C2]: ES → PE |

The pipeline forms a closed loop: ES → CD (detect) → PE (exclude) → TE (auto-resolve) → ES (persist) → PE (re-derive).

**Resolution paths**:

| Flag resolvability | Path |
|-------------------|------|
| `auto_eligible` | Auto-resolution policy watches for enabling event within time window. System actor: `system:auto_resolution/{policy_id}`. If deadline expires: escalate or mark for manual review. |
| `manual_only` | Designated resolver reviews and creates `ConflictResolved` event. Online-only [3-S6]. Accepted → state re-derived including event. Rejected → state unchanged. |

---

## 3. Detect-Before-Act Pipeline

The ordering guarantee that prevents downstream cascade from uncertain data [E4]. Enforced at three levels, all served by two contracts (C8 and C9):

| Level | What it prevents | Contract | Source |
|-------|-----------------|----------|--------|
| Policy execution | Flagged events do not trigger L3a/L3b triggers | [C9]: CD → TE | [2-S12] |
| State derivation | Flagged events do not advance state machines | [C8]: CD → PE | [5-S2] |
| Authorization | Auth-flagged events do not trigger downstream work; blocking vs. informational configurable | [C9]: CD → TE | [3-S7] |

The Conflict Detector is the sole provider of the detect-before-act guarantee.

---

## 4. Four-Layer Configuration Gradient

How deployer-authored configuration maps to platform primitives [4-S9]. Each layer is defined by its side effects, not its complexity.

| Layer | Name | Side effects | Primitives involved |
|-------|------|-------------|---------------------|
| L0 | Assembly | None — structural wiring | Pattern Registry (pattern selection), Scope Resolver (scope composition), Config Packager (flag severity, sensitivity) |
| L1 | Shape | None — schema and data mapping | Shape Registry (field definitions), Projection Engine (projection rules) |
| L2 | Logic | Form-scoped only — no persistent records | Expression Evaluator (show/hide, defaults, warnings) |
| L3 | Policy | System-scoped — creates persistent events | Trigger Engine (L3a event-reaction, L3b deadline-check, auto-resolution) |
| Code | Platform evolution | Unbounded | All primitives — new types, patterns, scope types, `context.*` properties |

**Boundary principle**: A capability's layer is determined by its side effects, not its complexity. L2 conditions may be intricate but they only affect form display. L3 triggers may be simple but they create persistent events.

**Code boundary**: When a deployment's needs exceed L3, the answer is platform evolution (new patterns, new types, new `context.*` properties) — not a workaround. The boundary is visible and signposted, not discovered by hitting walls [P2].

---

## 5. Configuration Delivery Pipeline

End-to-end flow from deployer authoring to device operation. Involves 3 primitives:

| Step | What happens | Primitive | Contract |
|------|-------------|-----------|----------|
| Author | Deployer creates shapes, activities, triggers, patterns, rules | — (external input) | — |
| Validate | All budgets and composition rules checked | Deploy-time Validator | [C13], [C18] |
| Package | Validated config assembled into atomic payload with all shape versions | Config Packager | [C14], [C19] |
| Deliver | Device receives package at next sync | Config Packager | [C20] |
| Apply | Device applies new config after completing in-progress work under old config | — (device-side) | [4-S6] |

The Deploy-time Validator is the acceptance gate [C19]. Rejection is binary — either the package passes all checks or it does not ship. This prevents partial or invalid configuration from reaching any device.

---

## 6. Sync Contract

Sync is not a primitive. It is a set of 8 cross-primitive guarantees, each traced to an existing contract:

| # | Guarantee | Providing contract | Source |
|---|-----------|-------------------|--------|
| SY-1 | Events are the sync unit. Sync transfers events the receiver hasn't seen. | [C1]: ES → ALL | [1-S4] |
| SY-2 | Sync is idempotent — receiving the same event twice is a no-op. | [C1]: ES → ALL | [1-S4] |
| SY-3 | Sync is append-only — the server never instructs deletion or modification. | [C1]: ES → ALL | [1-S4] |
| SY-4 | Sync is order-independent — events carry their own ordering metadata. | [C1]: ES → ALL | [1-S4], [2-S1] |
| SY-5 | Sync payload is scope-filtered — device receives exactly authorized data. | [C10]: SR → sync | [3-S2] |
| SY-6 | Conflict detection runs before policy execution on synced events. | [C9]: CD → TE | [2-S12] |
| SY-7 | Configuration is delivered atomically at sync. At most 2 versions coexist. | [C20]: CP → sync | [4-S6] |
| SY-8 | Merge/split/resolution events sync like any other event, filtered by scope. | [C1], [C10] | [3-S2], [2-S10], [3-S6] |

The sync contract is fully decomposable into guarantees from Event Store, Scope Resolver, Conflict Detector, and Config Packager. No hidden subsystem or untraced guarantee exists.

---

## 7. Unified Flag Catalog

9 flag categories across 4 ADRs [E5], [SG-2]. Naming normalized to `snake_case` per the convention established by ADR-002. Resolvability is platform-defined [5-S3], not deployer-configurable. Severity (blocking vs. informational) is deployer-configurable [4-S14].

| # | Category | Source | Resolvability | Typical cause |
|---|----------|--------|---------------|---------------|
| 1 | `identity_conflict` | ADR-2 | `manual_only` | Potential duplicate subjects |
| 2 | `stale_reference` | ADR-2 | `auto_eligible` | Entity updated after event creation |
| 3 | `concurrent_state_change` | ADR-2 | `manual_only` | Two actors changed same subject concurrently |
| 4 | `scope_violation` | ADR-3 | `manual_only` | Potential unauthorized access |
| 5 | `role_stale` | ADR-3 | `manual_only` | Actor's role changed; capability-restricted actions may be affected |
| 6 | `temporal_authority_expired` | ADR-3 | `auto_eligible` | Assignment ended, worker didn't know — timing overlap |
| 7 | `domain_uniqueness_violation` | ADR-4 | `manual_only` | Business rule violation |
| 8 | `transition_violation` | ADR-5 | `auto_eligible` | Invalid state transition — usually offline timing overlap |
| 9 | *(reserved)* | — | — | Catalog is append-only per the accept-and-flag pattern |

**Naming note**: ADR-003's original names (`ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag`) normalized to `snake_case`. `scope_stale` renamed to `scope_violation` because ADR-003 [3-S7] treats scope changes as a security concern (blocking by default), aligning with "violation" over "stale."

**Resolvability note**: `auto_eligible` flags (2, 6, 8) share a structural pattern — they're typically timing overlaps where a subsequent event confirms the original is valid. `manual_only` flags (1, 3, 4, 5, 7) require human judgment about domain context.

---

## 8. Aggregation Interface

Cross-subject aggregation (dashboards, campaign progress, reporting) is classified as outside the architecture (see [boundary.md](boundary.md)). The architecture declares the interface points where aggregation connects:

| Constraint | Source | What it means for aggregation |
|-----------|--------|-------------------------------|
| Input is per-subject projection state | [C2], [C4] | Aggregation reads from projections, not from events directly |
| Flagged-event-exclusion inherited | [C8], [5-S2] | Aggregation based on projection state automatically excludes flagged events. The integrity guarantee extends to any downstream consumer. |
| Does not write to Event Store | [1-ST2] | Aggregation is a read-only downstream consumer. Write-path discipline prohibits independent writes. |

What aggregation looks like to deployers — which aggregate views exist, how they're defined, how they're queried — is not constrained by any ADR. Multiple valid models exist: pre-defined dashboards, deployer-configured aggregate rules, SQL-based reporting, external BI integration. The architecture constrains the input surface and the integrity guarantee. Everything else is outside.
