# 001 — Spec Claims: Domain Invariants & Implementation Claims

> **Purpose**: Pre-Ship-4 audit. Extracts verifiable claims from governing
> documents so that Ship-4 spec authoring starts from a known-good baseline.
> Generated 2026-04-28.
>
> **Methodology**: Part A extracts domain invariants (behavioral/structural
> properties the system must exhibit from the user/domain perspective). Part B
> extracts implementation claims (decisions from the Charter and ADRs that
> carry implementation-level weight). Part C flags orphans and gaps.

---

## Part A — Domain Invariants

### INVARIANT-1: Offline-First Operations

**Source**: [docs/README.md](../../README.md) (V1), [docs/principles.md](../../principles.md) (P1), [docs/constraints.md](../../constraints.md) §Connectivity
**Statement**: "The platform must always allow every field operation — data capture, form rendering, subject lookup, and local validation — to complete without network connectivity."
**Scenarios that depend on it**: S00, S01, S02, S04, S05, S06, S07, S08, S09, S10, S12, S14, S16, S19, S20, S21, S22
**ADR grounding**: ADR-001 (immutable events, client-generated UUIDs), ADR-002 §S14 (never rejected for staleness), ADR-004 §S6 (atomic config delivery), ADR-005 §S4 (advisory command validator)

### INVARIANT-2: Append-Only Records

**Source**: [docs/principles.md](../../principles.md) (P3), [docs/README.md](../../README.md) (V3)
**Statement**: "The platform must never mutate or delete a persisted event. All corrections, resolutions, and state changes are new events appended to the stream."
**Scenarios that depend on it**: S00, S01, S04, S06, S07, S08, S11, S19, S22
**ADR grounding**: ADR-001 §S1 (immutable event store), ADR-005 §S4 (state as projection), ADR-006 §S1 (flags appended, not modifications)

### INVARIANT-3: Accept-and-Flag (No State-Based Rejection)

**Source**: [docs/charter.md](../../charter.md) §Invariants, [docs/principles.md](../../principles.md) (P5)
**Statement**: "The platform must always accept a validly-structured event, regardless of state anomalies. Anomalies surface as flag events alongside the accepted event — never as rejections or modifications."
**Scenarios that depend on it**: S01, S04, S06, S07, S08, S11, S19, S20, S22
**ADR grounding**: ADR-002 §S14, ADR-006 §S1 (canonical statement), ADR-005 §S1 (transition_violation flag)

### INVARIANT-4: Trustworthy, Auditable Records

**Source**: [docs/README.md](../../README.md) (V3), [docs/principles.md](../../principles.md) (P6)
**Statement**: "The platform must always attribute every event to a traceable author (human or system) and maintain a complete, tamper-evident audit trail. Every state transition, flag, and resolution is attributable."
**Scenarios that depend on it**: S00, S01, S04, S05, S08, S11, S20, S21
**ADR grounding**: ADR-002 §S2 (actor_ref mandatory), ADR-004 §S4 (system actor convention), ADR-005 §S9 (auto-resolution actor identity), ADR-007 §S2 (authorship rule for integrity shapes)

### INVARIANT-5: Configuration Has Boundaries

**Source**: [docs/README.md](../../README.md) (V2), [docs/principles.md](../../principles.md) (P2)
**Statement**: "The platform must always expose a visible, documented boundary between what deployers configure (L0–L3) and what requires platform evolution. Deployers must never hit invisible walls."
**Scenarios that depend on it**: S00, S02, S09, S10, S12
**ADR grounding**: ADR-004 §S9 (four-layer gradient), ADR-004 §S13 (complexity budgets), ADR-005 §S5 (pattern registry closed)

### INVARIANT-6: Simplest Scenario Stays Simple

**Source**: [docs/principles.md](../../principles.md) (P7)
**Statement**: "The platform must never require more than one shape definition and one activity definition for S00 (basic structured capture). Advanced machinery (patterns, triggers, flags, composition) adds zero overhead to the simple case."
**Scenarios that depend on it**: S00
**ADR grounding**: ADR-004 §Principles confirmed (P7), ADR-005 §Principles confirmed (P7)

### INVARIANT-7: Assignment-Based Access Control

**Source**: [docs/principles.md](../../principles.md) (P6), [docs/behavioral_patterns.md](../../behavioral_patterns.md) §BP-03
**Statement**: "The platform must always determine data visibility through explicit assignments (scope + role). No actor sees data outside their assigned scope. Sync delivers only scope-filtered events."
**Scenarios that depend on it**: S03, S04, S09, S14, S16, S20, S21, S22
**ADR grounding**: ADR-003 §S1/§S2 (assignment-based access, sync=scope), ADR-004 §S7 (no deployer-authored access logic), ADR-009 §S2 (scope as PRIMITIVE)

### INVARIANT-8: Conflict Surfacing, Not Silent Resolution

**Source**: [docs/principles.md](../../principles.md) (P5)
**Statement**: "The platform must never silently resolve a data conflict. All anomalies surface as flags visible to authorized actors. Auto-resolution produces explicit, auditable events traceable to system actors."
**Scenarios that depend on it**: S01, S06, S08, S19, S22
**ADR grounding**: ADR-002 §S12 (detect-before-act), ADR-005 §S7 (source-only flagging), ADR-005 §S9 (auto-resolution as L3b), ADR-006 §S2 (flags as canonical surface)

### INVARIANT-9: Envelope Closure

**Source**: [docs/charter.md](../../charter.md) §Invariants
**Statement**: "The platform must never extend the event envelope beyond its current 11 fields or the type vocabulary beyond its current 6 values without an architecture-grade ADR."
**Scenarios that depend on it**: All (universal contract)
**ADR grounding**: ADR-004 §S3 (type vocabulary), ADR-007 §S1 (canonical closure), ADR-004 S1/S2 (shape_ref, activity_ref finalized)

### INVARIANT-10: Composition Over Invention

**Source**: [docs/principles.md](../../principles.md) (P4)
**Statement**: "The platform must always compose existing primitives to handle new scenarios rather than inventing new structural mechanisms. Shapes compose with events, activities with assignments, triggers with accept-and-flag."
**Scenarios that depend on it**: S07, S08, S09, S13, S14, S15, S22
**ADR grounding**: ADR-004 §Principles confirmed (P4), ADR-005 §S6 (composition rules)

### INVARIANT-11: Detect-Before-Act

**Source**: [docs/charter.md](../../charter.md) §Cross-cutting rules
**Statement**: "The platform must never allow a flagged event to (a) trigger policies, (b) advance state machines, or (c) authorize downstream work until the flag is resolved."
**Scenarios that depend on it**: S04, S08, S11, S12
**ADR grounding**: ADR-002 §S12, ADR-005 §S2 (flagged events excluded from state derivation), ADR-006 §S1

### INVARIANT-12: Duality Rule (Mechanism vs. Instance)

**Source**: [docs/charter.md](../../charter.md) §Invariants
**Statement**: "The platform must always classify mechanism (PRIMITIVE) and instance (CONFIG) separately when a concept exposes both a platform-fixed closure and a deployer-parameterized surface."
**Scenarios that depend on it**: All (classification discipline)
**ADR grounding**: ADR-009 §S1

---

## Part B — Implementation Claims

### CLAIM-1: Client-Generated UUIDs

**Source**: [ADR-001 §S3](../../adrs/adr-001-offline-data-model.md)
**Claim**: "Events carry client-generated UUIDs as their primary identity. No server round-trip is required for ID assignment."
**Grounds invariant(s)**: INVARIANT-1 (offline), INVARIANT-2 (append-only)
**Status**: EXERCISED-MET (Ship-1 onward)

### CLAIM-2: Causal Ordering via (device_id, device_seq)

**Source**: [ADR-002 §S1](../../adrs/adr-002-identity-conflict.md)
**Claim**: "Per-device monotonic sequence numbers establish causal ordering. Combined with sync_watermark, they enable deterministic conflict detection."
**Grounds invariant(s)**: INVARIANT-3 (accept-and-flag), INVARIANT-4 (auditability)
**Status**: EXERCISED-MET (Ship-1 onward)

### CLAIM-3: Four Identity Categories in subject_ref

**Source**: [ADR-002 §S2](../../adrs/adr-002-identity-conflict.md), [ADR-008 §S1](../../adrs/adr-008-envelope-reference-fields.md)
**Claim**: "subject_ref.type uses a closed 4-value enum: `subject`, `actor`, `process` (RESERVED), `assignment`. Extension requires an ADR."
**Grounds invariant(s)**: INVARIANT-9 (envelope closure)
**Status**: EXERCISED-MET for `subject`; `assignment` exercised Ship-1b+; `process` RESERVED (decided-unexercised)

### CLAIM-4: Sync = Scope (Authorization as Projection)

**Source**: [ADR-003 §S2](../../adrs/adr-003-authorization-sync.md)
**Claim**: "An actor's sync payload is exactly the events within their assigned scope. Sync is the enforcement mechanism for authorization."
**Grounds invariant(s)**: INVARIANT-7 (assignment-based access)
**Status**: EXERCISED-MET (Ship-1b onward)

### CLAIM-5: Shape Reference in Envelope

**Source**: [ADR-004 §S1](../../adrs/adr-004-configuration-boundary.md)
**Claim**: "Every event carries `shape_ref` (`{name}/v{N}`). Shapes are versioned, additive-by-default, and remain valid forever."
**Grounds invariant(s)**: INVARIANT-2 (append-only), INVARIANT-5 (config boundaries)
**Status**: EXERCISED-MET (Ship-1 onward; evolution exercised Ship-3)

### CLAIM-6: Six-Type Structural Event Vocabulary

**Source**: [ADR-004 §S3](../../adrs/adr-004-configuration-boundary.md), [ADR-007 §S1](../../adrs/adr-007-envelope-type-closure.md)
**Claim**: "The envelope `type` is closed at 6 values: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`. New types require different platform processing behavior."
**Grounds invariant(s)**: INVARIANT-9 (envelope closure), INVARIANT-10 (composition)
**Status**: EXERCISED-MET for `capture`, `assignment_changed`, `alert`; `review` partially exercised (Ship-2 w/ walkthrough); `task_created`/`task_completed` DECIDED-UNEXERCISED

### CLAIM-7: Triggers Execute Server-Only

**Source**: [ADR-004 §S5](../../adrs/adr-004-configuration-boundary.md)
**Claim**: "Both event-reaction (L3a) and deadline-check (L3b) triggers evaluate and fire exclusively on the server."
**Grounds invariant(s)**: INVARIANT-1 (offline — devices stay simple)
**Status**: DECIDED-UNEXERCISED (no Ship has built the trigger engine)

### CLAIM-8: Atomic Configuration Delivery

**Source**: [ADR-004 §S6](../../adrs/adr-004-configuration-boundary.md)
**Claim**: "Configuration is delivered atomically at sync. At most 2 versions coexist on-device."
**Grounds invariant(s)**: INVARIANT-1 (offline), INVARIANT-5 (config boundaries)
**Status**: DECIDED-UNEXERCISED (device-side config management not yet built)

### CLAIM-9: Complexity Budgets (Deploy-Time Enforcement)

**Source**: [ADR-004 §S13](../../adrs/adr-004-configuration-boundary.md)
**Claim**: "Hard limits: 60 fields/shape, 3 predicates/condition, 5 triggers/event-type, 50 triggers/deployment, 2-level escalation depth."
**Grounds invariant(s)**: INVARIANT-5 (config boundaries)
**Status**: DEFERRED — ledger row `field_count_budget` demoted STABLE→DEFERRED per R-7 at Ship-3 closeout. FP-012b tracks HTTP enforcement gap.

### CLAIM-10: State Machines as Projection Patterns

**Source**: [ADR-005 §S4](../../adrs/adr-005-state-progression.md)
**Claim**: "Subject lifecycle state is derived from the event sequence by the projection engine. State is never stored in events. The platform flags violations — never enforces transitions."
**Grounds invariant(s)**: INVARIANT-1 (offline), INVARIANT-2 (append-only), INVARIANT-3 (accept-and-flag)
**Status**: DECIDED-UNEXERCISED (Ship-4 candidate — S04/S08 scope)

### CLAIM-11: Pattern Registry (Platform-Fixed, Deployer-Parameterized)

**Source**: [ADR-005 §S5](../../adrs/adr-005-state-progression.md), [ADR-009 §S3](../../adrs/adr-009-platform-fixed-vs-deployer-configured.md)
**Claim**: "Workflow patterns are a closed vocabulary. Four existence proofs: capture_with_review, case_management, multi_step_approval, transfer_with_acknowledgment."
**Grounds invariant(s)**: INVARIANT-5 (config boundaries), INVARIANT-12 (duality)
**Status**: DECIDED-UNEXERCISED (Ship-4 candidate — S04 exercises capture_with_review)

### CLAIM-12: Source-Only Flagging

**Source**: [ADR-005 §S7](../../adrs/adr-005-state-progression.md)
**Claim**: "Only root-cause events receive flags. Downstream contamination is a computed projection property via source-chain traversal, not additional stored flags."
**Grounds invariant(s)**: INVARIANT-8 (conflict surfacing)
**Status**: DECIDED-UNEXERCISED

### CLAIM-13: Context Expression Scope

**Source**: [ADR-005 §S8](../../adrs/adr-005-state-progression.md)
**Claim**: "The expression evaluator gains `context.*` (7 properties) in form evaluation context, all pre-resolvable on-device."
**Grounds invariant(s)**: INVARIANT-1 (offline), INVARIANT-5 (config boundaries)
**Status**: DECIDED-UNEXERCISED

### CLAIM-14: Auto-Resolution as L3b Sub-Type

**Source**: [ADR-005 §S9](../../adrs/adr-005-state-progression.md)
**Claim**: "Auto-resolution policies use the L3b deadline-check execution model. Only `auto_eligible` flag types can be targeted."
**Grounds invariant(s)**: INVARIANT-8 (conflict surfacing), INVARIANT-4 (auditability)
**Status**: DECIDED-UNEXERCISED

### CLAIM-15: Platform-Bundled Integrity Shapes

**Source**: [ADR-007 §S2, §S5](../../adrs/adr-007-envelope-type-closure.md)
**Claim**: "Four shapes are platform-bundled: `conflict_detected/v1`, `conflict_resolved/v1`, `subjects_merged/v1`, `subject_split/v1`. Registered at platform init, not deployer-authored."
**Grounds invariant(s)**: INVARIANT-9 (envelope closure), INVARIANT-8 (conflict surfacing)
**Status**: EXERCISED-MET for `conflict_detected/v1` and `subjects_merged/v1`; `conflict_resolved/v1` partially exercised (Ship-2); `subject_split/v1` DECIDED-UNEXERCISED

### CLAIM-16: Consumer Filtering on shape_ref Not type

**Source**: [ADR-007 §S3](../../adrs/adr-007-envelope-type-closure.md)
**Claim**: "Any code identifying integrity/identity events MUST filter on `shape_ref`, not `type`."
**Grounds invariant(s)**: INVARIANT-9 (envelope closure)
**Status**: EXERCISED-MET (Ship-3 corrections addressed pre-existing violations)

### CLAIM-17: Deterministic Flag Identity

**Source**: [ADR-007 §S4](../../adrs/adr-007-envelope-type-closure.md)
**Claim**: "Flag event UUIDs are derived via UUIDv5(DATARUN_FLAG_NS, source_event_id | shape_ref | flag_category)."
**Grounds invariant(s)**: INVARIANT-4 (auditability), INVARIANT-2 (append-only — idempotent flag creation)
**Status**: EXERCISED-MET (Ship-2 onward)

### CLAIM-18: Assignment Ending via Discrete Event

**Source**: [ADR-001 §S1](../../adrs/adr-001-offline-data-model.md) (append-only), contracts (`assignment_ended/v1`)
**Claim**: "Ending an assignment requires an `assignment_ended/v1` event. Pre-setting `valid_to` on the original event violates append-only."
**Grounds invariant(s)**: INVARIANT-2 (append-only), INVARIANT-7 (access control)
**Status**: EXERCISED-VIOLATED — FP-018 documents that `ScopeResolver` reads only `assignment_created/v1`; `assignment_ended/v1` events are never consumed. Ship-4 candidate for resolution.

---

## Part C — Orphan Claims & Uncovered Invariants

### ORPHAN CLAIM — OC-1: role_stale Projection Wiring

**Source**: FP-017 (supersedes FP-001)
**Description**: ADR-003 commits to a `role_stale` flag category (flag catalog row 5). The detector that produces this flag was discarded at Ship-1 rebuild and never reinstated. The concept-ledger row exists but no code path exercises it.
**Grounds invariant**: INVARIANT-3 (accept-and-flag requires detection machinery for all flag categories)
**Risk**: Stale-role captures are accepted without flagging — silent anomaly, violates INVARIANT-8.

### ORPHAN CLAIM — OC-2: field_count_budget HTTP Enforcement

**Source**: FP-012b
**Description**: ADR-004 §S13 commits to deploy-time validation of the 60-field budget. No HTTP endpoint enforces this. The ledger row was demoted STABLE→DEFERRED at Ship-3 closeout under R-7.
**Grounds invariant**: INVARIANT-5 (config boundaries — budgets are the primary defense against AP-1)
**Risk**: A deployer can ship an over-budget shape without rejection.

### ORPHAN CLAIM — OC-3: temporal_authority_expired Flag Detection

**Source**: Flag catalog row 6, ADR-003
**Description**: The `temporal_authority_expired` flag category exists in the catalog but no detection logic exists in code. Related to FP-018 (assignment_ended not consumed).
**Grounds invariant**: INVARIANT-7 (access control depends on temporal bounds being enforced)
**Risk**: An actor whose temporal access has expired continues to sync and capture without flags.

### UNCOVERED INVARIANT — UI-1: Pattern Inventory Undefined

**Source**: ADR-005 §S5 ("inventory is implementation scope")
**Description**: INVARIANT-5 (config boundaries) and INVARIANT-6 (simplest stays simple) both depend on patterns being available. The four existence proofs are validated in ADR exploration but no formal pattern schema or registry implementation exists. Ship-4 (S04 scope) needs `capture_with_review`.
**Dependent scenarios**: S04, S08, S11, S07/S14

### UNCOVERED INVARIANT — UI-2: Expression Language Implementation

**Source**: ADR-004 §S11, ADR-005 §S8
**Description**: INVARIANT-1 (offline) depends on on-device expression evaluation for L2 logic. No expression evaluator exists. `context.*` scope (CLAIM-13) compounds this gap.
**Dependent scenarios**: S10, S12

### UNCOVERED INVARIANT — UI-3: Trigger Engine

**Source**: ADR-004 §S12, ADR-005 §S9
**Description**: INVARIANT-8 (conflict surfacing) and the reactive behavioral patterns (BP-07, BP-08, BP-09) all depend on the trigger engine. No L3a or L3b processing exists.
**Dependent scenarios**: S02 (deadline), S09, S10, S12, S16

### UNCOVERED INVARIANT — UI-4: Sensitivity Classification Enforcement

**Source**: ADR-004 §S14 (sensitivity levels: standard, elevated, restricted)
**Description**: Sensitivity classification affects sync scope filtering and device retention. No implementation exists. The ledger row `sensitive-subject-classification` is DEFERRED.
**Dependent scenarios**: S08 (medical case data), S16 (crisis data)

---

## Summary Matrix

### Invariant → Claim Coverage

| Invariant | Grounding Claims | Status |
|---|---|---|
| INV-1 Offline-First | C-1, C-7, C-8, C-13 | C-1 met; C-7/C-8/C-13 unexercised |
| INV-2 Append-Only | C-1, C-5, C-17, C-18 | C-1/C-5/C-17 met; **C-18 VIOLATED (FP-018)** |
| INV-3 Accept-and-Flag | C-2, C-10 | C-2 met; C-10 unexercised |
| INV-4 Auditability | C-2, C-14, C-17 | C-2/C-17 met; C-14 unexercised |
| INV-5 Config Boundaries | C-5, C-8, C-9, C-11 | C-5 met; C-8/C-11 unexercised; **C-9 DEFERRED (FP-012b)** |
| INV-6 Simplest Stays Simple | (no claim at risk) | Met — S00 validated |
| INV-7 Assignment Access | C-4, C-18 | C-4 met; **C-18 VIOLATED (FP-018)** |
| INV-8 Conflict Surfacing | C-12, C-14, C-15 | C-15 partially met; C-12/C-14 unexercised |
| INV-9 Envelope Closure | C-3, C-6, C-15, C-16 | C-3/C-16 met; C-6 partially met; C-15 partially met |
| INV-10 Composition | C-6, C-11 | C-6 partially met; C-11 unexercised |
| INV-11 Detect-Before-Act | C-10, C-12 | Both unexercised |
| INV-12 Duality Rule | C-11 | Unexercised |

### Ship-4 Relevance (S04 + S08 scope)

| Item | Ship-4 Impact |
|---|---|
| CLAIM-10 (state machines as projection) | **First exercise** — S04 review cycle, S08 case lifecycle |
| CLAIM-11 (pattern registry) | **First exercise** — `capture_with_review` for S04 |
| CLAIM-18 (assignment_ended) | **Resolution candidate** — FP-018 gate |
| OC-1 (role_stale wiring) | **Candidate** — if S04 reviewer role changes mid-cycle |
| OC-3 (temporal_authority) | **Candidate** — if S08 case transfers change actor scope |
| UI-1 (pattern inventory) | **Blocker** — S04 needs `capture_with_review` skeleton |
