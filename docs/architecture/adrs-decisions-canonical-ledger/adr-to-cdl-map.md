# ADR-to-CDL Reference Map

> [!WARNING]
> **RETIRED / NON-AUTHORITATIVE ARCHIVAL REFERENCE**
> This file is NOT an active architectural decision surface. It exists solely to assist in routing historical documentation references pointing to retired ADRs (001–009) to their corresponding active definitions in the **[Canonical Decision Ledger](../architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md)** (CDL).
> Do NOT read or use this file to guide current development or system behavior unless verifying historical context. Check **[Canonical Decision Ledger](../architecture/adrs-decisions-canonical-ledger/README.md)** for how to effeciently access and retrieve decisions' details.

---

## Mapping of Retired ADR Decisions to the Canonical Decision Ledger (CDL)

This mapping details where the sub-decisions (`S1`, `S2`, etc.) of retired ADR-001 through ADR-009 have been consolidated and formalized in the active [Canonical Decision Ledger](../architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md) (CDL).

### 1. ADR-001: Offline Data Model

| ADR-001 Section | Title / Content | CDL Mapping | Status |
| :--- | :--- | :--- | :--- |
| **S1** | Record Mutability | **CDL-001** (Immutable event stream), **CDL-019** (Typed immutable events) | Consolidated |
| **S2** | Write Granularity | **CDL-002** (Projections rebuildable), **CDL-019** (Typed immutable events) | Consolidated |
| **S3** | Identity Generation | **CDL-020** (Client-generated UUIDs) | Consolidated |
| **S4** | Sync Unit | **CDL-021** (Idempotent event sync) | Consolidated |
| **S5** | Event Envelope Guarantees | **CDL-006** (11 envelope fields) | Consolidated |

### 2. ADR-002: Identity Model and Conflict Resolution

| ADR-002 Section | Title / Content | CDL Mapping | Status |
| :--- | :--- | :--- | :--- |
| **S1** | Event Envelope — Causal Ordering Fields | **CDL-010** (Causal metadata - `device_id`, `device_sequence`, `sync_watermark`) | Consolidated |
| **S2** | Event Envelope — Typed Identity References | **CDL-017** (`subject_ref` typed reference contract) | Consolidated |
| **S3** | `device_time` Is Advisory | **CDL-011** (`device_time` is advisory) | Consolidated |
| **S4** | Device Sequence and Sync Watermark Persistence | **CDL-010** (Durable sequence survival across boot/crash) | Consolidated |
| **S5** | Device Identity Is Hardware-Bound | **CDL-010** (`device_id` identifies physical device, not actor) | Consolidated |
| **S6** | Merge Is Alias-in-Projection, Never Re-Reference | **CDL-023** (Identity merge is alias-in-projection) | Consolidated |
| **S7** | No `SubjectsUnmerged` — Corrective Split | **CDL-025** (No unmerge operation exists) | Consolidated |
| **S8** | Split Freezes History; Source Permanently Archived | **CDL-024** (Split freezes history) | Consolidated |
| **S9** | Lineage Graph Acyclicity By Construction | **CDL-026** (Identity lineage is acyclic DAG) | Consolidated |
| **S10** | Merges and Splits Are Online-Only | **CDL-027** (Merge/split online-only) | Consolidated |
| **S11** | Single-Writer Conflict Resolution | **CDL-028** (Conflict resolution single-writer) | Consolidated |
| **S12** | Conflict Detection Before Policy Execution | **CDL-004** (Detect-before-act policy exclusion) | Consolidated |
| **S13** | Conflict Detection Uses Raw References | **CDL-029** (Conflict detection uses raw references) | Consolidated |
| **S14** | Events Are Never Rejected for State Staleness | **CDL-003** (Valid state-stale events accepted and flagged), **CDL-022** | Consolidated |

### 3. ADR-003: Authorization and Selective Sync

| ADR-003 Section | Title / Content | CDL Mapping | Status |
| :--- | :--- | :--- | :--- |
| **S1** | Assignment-Based Access Control | **CDL-030** (Access is assignment-based) | Consolidated |
| **S2** | Sync Scope Equals Access Scope | **CDL-031** (Sync scope equals access scope) | Consolidated |
| **S3** | Authority Context Is Projected, Not Stored | **CDL-012** (No derived envelope fields), **CDL-032** (Authority is projected) | Consolidated |
| **S4** | Alias-Respects-Original-Scope | **CDL-033** (Authorization uses original subject scope) | Consolidated |
| **S5** | Scope-Containment Invariant on Assignment Creation | **CDL-034** (Assignment creation scope containment) | Consolidated |
| **S6** | Conflict Resolution Is Online-Only | **CDL-028** (Conflict resolution server-validated) | Consolidated |
| **S7** | Detect-Before-Act Extends to Authorization Flags | **CDL-004** (Policy exclusion for unresolved flags), **CDL-035** | Consolidated |
| **S8** | Tiered Projection Location | **CDL-036** (Tiered projection location) | Consolidated |
| **S9** | Authorization Staleness Handling | **CDL-035** (Authorization staleness accepted and flagged) | Consolidated |
| **S10** | Data Handling on Scope Change | **CDL-037** (Scope contraction device retention policy) | Consolidated |

### 4. ADR-004: Configuration Boundary

| ADR-004 Section | Title / Content | CDL Mapping | Status |
| :--- | :--- | :--- | :--- |
| **S1** | Shape Reference in Envelope | **CDL-008** (`shape_ref` versioned URI) | Consolidated |
| **S2** | Activity Reference in Envelope | **CDL-009** (`activity_ref` optional field) | Consolidated |
| **S3** | Structural Event Type Vocabulary | **CDL-007** (Envelope type is closed 6-value vocabulary) | Consolidated |
| **S4** | System Actor Identity Convention | **CDL-018** (`actor_ref` system prefix system:) | Consolidated |
| **S5** | All Triggers Execute Server-Only | **CDL-042** (L3 policy executes server-only) | Consolidated |
| **S6** | Atomic Configuration Delivery | **CDL-041** (Atomic configuration packages at sync) | Consolidated |
| **S7** | No Deployer-Authored Access Control Logic | **CDL-055** (Scope mechanism platform-fixed) | Consolidated |
| **S8** | No Field-Level Sensitivity Classification | **CDL-046** (Sensitivity is shape/activity level) | Consolidated |
| **S9** | Four-Layer Configuration Gradient | **CDL-038** (Four-layer configuration gradient) | Consolidated |
| **S10** | Shape Definition, Versioning, and Evolution | **CDL-039** (Shapes deployer-defined version coexistence), **CDL-040** | Consolidated |
| **S11** | Expression Language and Logic Rules | **CDL-043** (Expression language bounded) | Consolidated |
| **S12** | Trigger Architecture and Limits | **CDL-042** (L3 server-only), **CDL-049** (Triggers non-recursive) | Consolidated |
| **S13** | Complexity Budgets | **CDL-044** (Complexity budgets enforced) | Consolidated |
| **S14** | Deployer-Parameterized Policies | **CDL-045** (Domain uniqueness), **CDL-046** (Sensitivity parameters) | Consolidated |

### 5. ADR-005: State Progression and Workflow

| ADR-005 Section | Title / Content | CDL Mapping | Status |
| :--- | :--- | :--- | :--- |
| **S1** | Transition Violation Flag Category | **CDL-048** (`transition_violation` flag category) | Consolidated |
| **S2** | Flagged Events Excluded from State Machine Evaluation | **CDL-004** (Flagged events excluded from state derivation) | Consolidated |
| **S3** | Flag Resolvability Classification | **CDL-054** (Flag resolvability is platform-classified) | Consolidated |
| **S4** | State Machines as Projection Patterns | **CDL-047** (Workflow state is projection-derived) | Consolidated |
| **S5** | Pattern Registry | **CDL-049** (Pattern registry is platform-fixed) | Consolidated |
| **S6** | Pattern Composition Rules | **CDL-050** (Pattern composition rules) | Consolidated |
| **S7** | Source-Only Flagging | **CDL-051** (Source-only flagging workflow cascade) | Consolidated |
| **S8** | Context Expression Scope | **CDL-052** (`context.*` form context namespace) | Consolidated |
| **S9** | Auto-Resolution as L3b Sub-Type | **CDL-053** (Auto-resolution is L3b policy) | Consolidated |

### 6. ADR-006: Flag Semantics — Invariant Property, Algorithmic Procedure

| ADR-006 Section | Title / Content | CDL Mapping | Status |
| :--- | :--- | :--- | :--- |
| **S1** | `accept-and-flag` is an INVARIANT | **CDL-003** (Valid state-stale events accepted and flagged) | Consolidated |
| **S2** | `flag` is an INVARIANT (as a class) | **CDL-022** (Flag represents stream anomalies) | Consolidated |
| **S3** | `conflict-detection` is the ALGORITHM | **CDL-022** (Derived by Conflict Detector processing) | Consolidated |
| **S4** | `flag-creation-location` — server-side | **CDL-022** (Server-side detection pipeline) | Consolidated |

### 7. ADR-007: Envelope Type Closure & Integrity Shapes

| ADR-007 Section | Title / Content | CDL Mapping | Status |
| :--- | :--- | :--- | :--- |
| **S1** | Closed 6-Value Type Vocabulary | **CDL-007** (Envelope type is closed 6-value vocabulary) | Consolidated |
| **S2** | Platform-Bundled Integrity/Identity Shapes | **CDL-014** (Platform-bundled integrity and identity shapes) | Consolidated |
| **S3** | Consumer Filtering Rule (use `shape_ref`) | **CDL-013** (Domain discrimination uses `shape_ref`, not `type`) | Consolidated |
| **S4** | Deterministic Flag Identity | **CDL-015** (Deterministic flag identity) | Consolidated |
| **S5** | Shape Registry Registration Obligation | **CDL-014** (Platform-bundled shapes are platform registered) | Consolidated |

### 8. ADR-008: Envelope Reference Fields

| ADR-008 Section | Title / Content | CDL Mapping | Status |
| :--- | :--- | :--- | :--- |
| **S1** | `subject_ref` is a CONTRACT | **CDL-017** (`subject_ref` typed reference contract) | Consolidated |
| **S2** | `actor_ref` is a CONTRACT | **CDL-018** (`actor_ref` human/system author contract) | Consolidated |
| **S3** | `activity_ref` is a CONTRACT | **CDL-009** (`activity_ref` optional field) | Consolidated |
| **S4** | Reference is not Referent (orthogonality) | **CDL-016** (Reference vs referent classification split) | Consolidated |

### 9. ADR-009: Platform-Fixed Mechanism vs. Deployer-Configured Instance

| ADR-009 Section | Title / Content | CDL Mapping | Status |
| :--- | :--- | :--- | :--- |
| **S1** | Duality Rule (Mechanism vs Instance) | **CDL-005** (Mechanisms and instances classified separately) | Consolidated |
| **S2** | `scope` is PRIMITIVE | **CDL-055** (Scope mechanism platform-fixed; instances config) | Consolidated |
| **S3** | `pattern` is PRIMITIVE | **CDL-049** (Pattern registry platform-fixed; activities config) | Consolidated |
| **S4** | `activity` is CONFIG | **CDL-056** (Activity is deployer configuration) | Consolidated |
