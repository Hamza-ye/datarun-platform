# Datarun Architecture Description

> The definitive reference for what the Datarun platform architecture IS. Organized by architectural views — not by decision history. Self-contained; reading the ADRs is not required, though they are linked for traceability.

---

## 1. Purpose

This Architecture Description consolidates 61 decided architectural positions from 5 Architecture Decision Records into a single reference organized by what gets built, not by when it was decided.

**What it describes:**

- 11 platform primitives and their invariants — [primitives.md](primitives.md)
- 4 workflow pattern specifications (state machines, transitions, parameterization) — [patterns.md](patterns.md)
- 21 inter-primitive contracts with ordering guarantees — [contracts.md](contracts.md)
- 8 cross-cutting concerns spanning multiple primitives — [cross-cutting.md](cross-cutting.md)
- 29 boundary classifications: what's decided, specification-grade, implementation-grade, or outside — [boundary.md](boundary.md)

**What it does NOT contain:**

- API signatures, interface definitions, or data format schemas
- Protocol sequence diagrams or state transition diagrams
- Component internals, algorithms, or performance targets
- Grammar definitions or parser specifications
- Decision history or exploration reasoning (see [exploration/](../exploration/) for those)

---

## 2. Vision Commitments

The architecture serves five commitments from the platform vision:

| # | Commitment | Architectural expression |
|---|-----------|--------------------------|
| V1 | Works without connectivity | Events created offline with full envelope. No operation requires a server roundtrip. Sync is for exchange, not operation. |
| V2 | Set up, not built | Four-layer configuration gradient (L0–L3). The simplest scenario requires 2 artifacts and zero code. |
| V3 | Trustworthy records | Append-only events. Full provenance in every envelope. Accept-and-flag — never silently discard. |
| V4 | One system, not many | 11 primitives compose for all 21 scenarios. Same contracts, same patterns, same integrity guarantees. |
| V5 | Grows without breaking | Additive schema evolution. Platform-fixed patterns compose without modification. Complexity budgets prevent accretion. |

---

## 3. Confirmed Principles

Seven working principles, all confirmed through all 5 ADRs:

| # | Principle | Core expression |
|---|-----------|-----------------|
| P1 | Offline is the default | Client-generated UUIDs, device-local sequence numbers, advisory-only timestamps |
| P2 | Configuration has boundaries | Four-layer gradient with hard complexity budgets, explicit code boundary |
| P3 | Records are append-only | Immutable events, alias-based merge, flag rather than reject |
| P4 | Patterns compose | One subject-level pattern per activity, event-level compose freely, cross-activity via `activity_ref` |
| P5 | Conflicts are surfaced | Accept-and-flag as universal mechanism, detect-before-act, single-writer resolution |
| P6 | Authority is contextual and auditable | Assignment-based access, authority-as-projection, scope = sync |
| P7 | The simplest scenario stays simple | S00 = 2 config artifacts. Complexity budgets prevent creep. |

Full detail: [principles.md](../principles.md)

---

## 4. Architecture at a Glance

**11 primitives** organized in 5 architectural layers:

| Layer | Primitives |
|-------|-----------|
| Storage & Projection | Event Store, Projection Engine |
| Identity & Integrity | Identity Resolver, Conflict Detector |
| Authorization & Sync | Scope Resolver |
| Configuration | Shape Registry, Expression Evaluator, Trigger Engine, Deploy-time Validator, Config Packager |
| Workflow | Pattern Registry |

Plus one advisory component (Command Validator) that enforces no invariant of its own.

**21 contracts**: Every inter-primitive interaction is a traced guarantee. 15 decided directly by ADRs, 6 derived from decided positions.

**8 cross-cutting concerns**: Event envelope, accept-and-flag lifecycle, detect-before-act pipeline, four-layer configuration gradient, configuration delivery pipeline, sync contract, unified flag catalog, aggregation interface.

**29 boundary classifications**: 5 specification-grade (must be consolidated before implementation), 15 implementation-grade (architecture intentionally left open), 8 outside (explicitly excluded), 1 resolved.

---

## 5. Glossary

| Term | Definition |
|------|-----------|
| **Primitive** | A platform component with a distinct invariant that no other primitive shares |
| **Event** | The atomic unit of data. Immutable, self-describing, carries an 11-field envelope |
| **Projection** | Current state derived from the event stream. Always rebuildable from events |
| **Contract** | A guarantee one primitive makes to another, traceable to decided positions |
| **Flag** | An anomaly raised against an event. Never causes rejection. 9 categories |
| **Envelope** | The 11-field metadata structure every event carries |
| **Shape** | A typed, versioned payload schema, referenced via `shape_ref` in the envelope |
| **Pattern** | A platform-fixed workflow skeleton, deployer-selected and parameterized |
| **Activity** | A deployment-defined unit of work, referenced via optional `activity_ref` |
| **Scope** | The authorization and sync boundary for an actor. 3 platform-fixed types |
| **Layer** | One of four configuration expressiveness levels (L0 Assembly, L1 Shape, L2 Logic, L3 Policy) |
| **Accept-and-flag** | The universal anomaly handling mechanism. Events are never rejected; anomalies are surfaced as flags |
| **Detect-before-act** | The ordering guarantee that prevents downstream cascade from flagged events |
| **Deployer** | The person or team that configures the platform for a specific operational context |

---

## 6. Reading Guide

| Order | File | What you learn |
|-------|------|----------------|
| 1 | This README | Architecture scope, commitments, glossary |
| 2 | [primitives.md](primitives.md) | The 11 building blocks, their invariants, what each owns |
| 3 | [contracts.md](contracts.md) | How primitives interact: 21 guarantees and dependency structure |
| 4 | [cross-cutting.md](cross-cutting.md) | 8 concerns that span multiple primitives and views |
| 5 | [boundary.md](boundary.md) | Where the architecture ends: decided, deferred, or excluded |

---

## 7. Traceability

Every constraint in this description traces to a source:

| Prefix | Meaning | Example |
|--------|---------|---------|
| **[N-SX]** | ADR-00N sub-decision X | [1-S1] = ADR-001, sub-decision 1 |
| **[N-STX]** | Documented strategy in ADR-00N | [1-ST2] = ADR-001, strategy 2 |
| **[EX]** | Emergent position from consolidation | [E1] = emergent position 1 |
| **[CX]** | Inter-primitive contract | [C1] = contract 1 |

**Source ADRs**: [ADR-001](../adrs/adr-001-offline-data-model.md) · [ADR-002](../adrs/adr-002-identity-conflict.md) · [ADR-003](../adrs/adr-003-authorization-sync.md) · [ADR-004](../adrs/adr-004-configuration-boundary.md) · [ADR-005](../adrs/adr-005-state-progression.md)

**Consolidation artifacts**: [Decision Harvest](../exploration/24-decision-harvest.md) · [Boundary Mapping](../exploration/25-boundary-mapping.md) · [Contract Extraction](../exploration/26-contract-extraction.md) · [Gap Identification](../exploration/27-gap-identification.md)
