# 23 — Architecture Consolidation Framework

> Methodology for the post-architecture phase. Bridges 5 decided ADRs to a single Architecture Description without inventing behavioral commitments or crossing into implementation.

---

## 1. The Problem This Solves

The architecture exploration phase is complete: 5 ADRs decided, 52 sub-decisions committed, 21 exploration sessions documented. The primitives inventory (doc 22) catalogs the 11 building blocks that emerged.

The next artifact is an **Architecture Description** — a consolidated reference organized by what gets built, not by the decisions that shaped it. The risk at this transition:

**Two failure modes:**

1. **Behavioral contract invention.** Writing inter-primitive behavior that no ADR decided. The ADRs commit constraints. Inter-primitive contracts must be *derived* from those constraints, not asserted. If a contract isn't traceable to a decided position, it's either a specification-grade consolidation task (and must be flagged as such) or it's being invented (and must stop).

2. **Implementation crossing.** Pinning decisions the architecture intentionally left open. The architecture decided "operators + field references, zero functions" — that's a constraint. A formal grammar definition is implementation. The architecture decided "sync scope = access scope" — that's a constraint. A sync protocol sequence diagram is implementation. The consolidation must distinguish what the architecture *binds* from what it *leaves open*.

**Why the primitives inventory is necessary but insufficient:**

The inventory (doc 22) is a **structural catalog** — it answers "what are the parts?" It does not answer:

- What does each part promise to the others? *(contracts)*
- What's architecturally decided vs. specification-grade vs. implementation? *(altitude)*
- What's inside the architecture boundary and what's outside? *(boundary)*
- Is the catalog complete relative to the decided positions? *(coverage)*

---

## 2. Terminology

| Term | Meaning in this phase |
|------|----------------------|
| **Architecture Description** | The consolidated output — describes what the architecture IS. Not a specification. ISO/IEC/IEEE 42010 sense. |
| **Decided position** | A sub-decision from an ADR, classified as structural constraint, strategy-protecting constraint, or initial strategy. The raw material for consolidation. |
| **Contract** | A guarantee one primitive makes to another, traceable to one or more decided positions. Format: provider → consumer, guarantee, source. |
| **Altitude** | The level of detail appropriate for a given concern: architecture-grade (constraint), specification-grade (must be pinned before implementation, but not an irreversible decision), implementation-grade (intentionally left open). |
| **Boundary** | The explicit classification of a concern as inside or outside the architecture. "Deferred" without a boundary is not acceptable — every deferred item must state whether it's deferred-inside (specification-grade) or deferred-outside (not this architecture). |

---

## 3. Relationship to Prior Frameworks

The exploration framework (doc 00) defined the two-pass process for the architecture phase:

- **Pass 1**: Explore in dependency order → directional leans
- **Pass 2**: Write ADRs in dependency order → committed decisions

The consolidation framework adds a **Pass 3**: Consolidate committed decisions into an Architecture Description.

```
Pass 1: Explore (docs 01–21)
    ↓
Pass 2: Decide (ADRs 001–005)
    ↓
Pass 3: Consolidate (this framework)
    ↓
Architecture Description
```

Pass 3 is **not exploration**. No new positions are taken. No new constraints are committed. If consolidation reveals a gap that requires a new position, it routes to a focused exploration session — it does not resolve the gap inline.

Pass 3 is **not specification**. The output describes architectural contracts and boundaries. It does not define APIs, data formats, protocol sequences, or component internals.

---

## 4. Method: Five Steps

Each step is session-sized. Each produces a reviewable artifact. Each builds on the previous. None invents — all extract and reconcile from existing material.

### Step 1: Decision Harvest

**Input**: 5 ADRs, 21 exploration documents, primitives inventory (doc 22).

**Process**: Walk every ADR systematically and extract each committed position into a flat table:

| Column | Content |
|--------|---------|
| Source | ADR-N, sub-decision ID (e.g., ADR-2 S12) |
| Statement | The decided position, one sentence |
| Classification | Structural constraint / strategy-protecting / initial strategy |
| Primitives affected | Which of the 11 primitives this position binds |
| Cross-references | Other sub-decisions this one extends, protects, or depends on |

**What it catches**:
- Positions concluded in exploration that were absorbed into ADRs but not surfaced in the inventory
- "What This Does NOT Decide" items that need explicit boundary markers
- Carry-forward items resolved in later ADRs (e.g., ADR-4 resolving questions deferred from ADR-2/3)
- Implicit positions (e.g., "the envelope is finalized at 11 fields" — never stated as a sub-decision but emerges from the combination of 5 ADRs adding zero-to-many fields each)

**Output**: Flat decision table. Expected ~52 sub-decisions, plus a small number of implicit/emergent positions.

**Completion test**: Every sub-decision across all 5 ADRs appears in the table. Every "What This Does NOT Decide" item from every ADR is accounted for (either resolved by a later ADR or flagged for boundary mapping in Step 2).

---

### Step 2: Boundary Mapping

**Input**: Decision harvest (Step 1), primitives inventory (doc 22), ADR "What This Does NOT Decide" sections, scenarios (for coverage check).

**Process**: For every concern that touches the architecture — decided positions, deferred items, inventory open questions (doc 22 §7), and cross-cutting capabilities — classify into exactly one category:

| Category | Meaning | Architecture Description treatment |
|----------|---------|-------------------------------------|
| **Decided** | An ADR committed this position. | State as-is. Trace to source. |
| **Specification-grade** | Not decided by ADR, but must be consolidated (not invented) before the description is complete. The existing material contains enough to derive the answer. | Consolidate from decided positions. Flag the derivation logic. |
| **Implementation-grade** | The architecture intentionally left this open. Multiple valid implementations exist within the constraints. | State the boundary: "the architecture constrains X; implementation chooses among Y." |
| **Outside** | Explicitly not part of this architecture. Named so it doesn't leak in. | State the exclusion and, if relevant, the interface points where this concern touches the architecture boundary. |

**Specific concerns that must be classified here** (non-exhaustive — the harvest will surface others):

| Concern | Likely category | Why it needs explicit classification |
|---------|----------------|--------------------------------------|
| Expression language grammar/syntax | Implementation-grade | Architecture decided constraints (operators, field refs, zero functions, 3-predicate limit). Grammar is implementation. |
| Sync protocol behavior | Specification-grade | Cross-primitive guarantees (idempotent delivery, scope-filtered, detect-before-act ordering, atomic config). Not a component — a contract. |
| Reporting and aggregation | Decided here | Must be classified inside or outside. "Deferred" without boundary will leak. |
| Pattern skeleton schemas | Implementation-grade | Architecture decided what patterns provide. Schema format is implementation. |
| Configuration authoring format | Implementation-grade | Architecture decided the gradient. YAML/visual/JSON is implementation. |
| Projection rebuild strategy | Implementation-grade | Architecture decided projections are rebuildable. Strategy is implementation. |
| Data archival/retention | Outside or specification-grade | Must be classified. |
| Source-chain traversal depth | Specification-grade or implementation-grade | Architecture decided source-only flagging. Depth limits need classification. |

**Output**: Boundary table covering every concern surfaced by Steps 1–2.

**Completion test**: Zero items remain "deferred without category." Every item from ADR "What This Does NOT Decide" sections has a category. Every item from doc 22 §7 has a category.

---

### Step 3: Contract Extraction

**Input**: Decision harvest (Step 1), boundary map (Step 2), primitives inventory (doc 22 §4 inputs/outputs).

**Process**: For every pair of primitives that interact (identified from inventory inputs/outputs), extract the inter-primitive guarantees. Each contract has a fixed format:

| Field | Content |
|-------|---------|
| Provider | The primitive making the guarantee |
| Consumer | The primitive relying on the guarantee |
| Guarantee | What the provider promises, stated as an invariant |
| Requires | What the provider needs from others to fulfill the guarantee |
| Source | Decided position(s) from the harvest that authorize this contract |
| Classification | Decided / specification-grade (derived from decided positions) |

**Rules**:
- Every guarantee must trace to at least one decided position from the harvest. If it doesn't, it's either specification-grade (derivable from existing decisions — flag the derivation) or being invented (stop and flag as a gap).
- Contracts describe guarantees, not behavior. "Conflict Detector guarantees: no flagged event reaches trigger evaluation" is a contract. "Conflict Detector evaluates events during sync processing before passing them to the Trigger Engine" is behavior — it describes HOW the guarantee is fulfilled, which is implementation.
- Contracts are directional. "A guarantees X to B" and "B guarantees Y to A" are separate contracts.

**Output**: Contract table. Expected 15–25 contracts across the 11 primitives.

**Completion test**: Every input/output edge from the inventory (doc 22) is covered by at least one contract. No contract lacks a source trace.

---

### Step 4: Gap Identification

**Input**: All prior outputs (harvest, boundary map, contracts).

**Process**: With the three artifacts in hand, identify what's missing:

**4a. Contract gaps**: Two primitives interact (per inventory), but no decided position covers their contract. Diagnosis:
- Was it resolved in an exploration session without surfacing into an ADR? → Extract from exploration, classify as specification-grade.
- Was it genuinely never addressed? → Flag. Determine if it needs exploration or is consolidation-resolvable.

**4b. Boundary gaps**: A concern is classified as "specification-grade" but has no decided constraint to anchor it. Diagnosis:
- Is the anchor implicit in the combination of multiple decided positions? → Document the derivation.
- Is the concern genuinely unresolved? → Flag for focused exploration.

**4c. Coverage gaps**: Walk the 12 behavioral patterns (behavioral_patterns.md) and the 21 scenarios against the contract table. Does every pattern have a home? Does every scenario decompose into contracted interactions? Gaps here indicate either:
- A primitive is missing (unlikely at this stage — but testable)
- A contract is under-specified (more likely)
- The concern is outside the architecture (classify it)

**4d. Outside-boundary interfaces**: For each concern classified as "outside," determine whether the architecture must declare an interface to it (what the platform provides that the outside concern connects to) or whether it's truly external (no interface needed).

**Output**: Gap register. Each entry:

| Field | Content |
|-------|---------|
| Gap | What's missing |
| Type | Contract gap / boundary gap / coverage gap / interface gap |
| Evidence | What surfaced it (which step, which cross-reference) |
| Response | Consolidate from existing material / lightweight exploration needed / no action (correctly deferred) |

**Completion test**: Every gap has a response. No response is "decide later." Gaps requiring exploration are scoped to a single focused session each.

---

### Step 5: Architecture Description

**Input**: All prior outputs. Any focused explorations triggered by Step 4 gaps (completed before this step begins).

**Process**: Write the Architecture Description. Organized by **architectural views**, not by decision history or implementation components.

**Three views:**

| View | What it describes | Content source |
|------|-------------------|----------------|
| **Structural** | Primitives, their invariants, what they own | Inventory (doc 22), refined by harvest |
| **Behavioral** | Inter-primitive contracts, lifecycle flows, ordering guarantees | Contract table (Step 3), cross-cutting concerns |
| **Boundary** | What's decided, what's specification-grade, what's implementation, what's outside | Boundary map (Step 2), gap register (Step 4) |

**What the Architecture Description contains:**
- What each primitive guarantees
- What each primitive requires
- What constraints bind each primitive (traced to ADR sub-decisions)
- How cross-cutting concerns flow through the primitive graph
- Where the architecture ends and implementation begins
- What's explicitly outside the architecture

**What the Architecture Description does NOT contain:**
- API signatures or interface definitions
- Data format schemas (YAML, JSON, SQL)
- Grammar definitions or parser specifications
- Protocol sequence diagrams or state transition diagrams
- Component internal structure or algorithms
- Performance targets or resource budgets

**File structure**: To be determined after Steps 1–4 reveal the natural organization. The inventory proposed a 6-file layer-based split; this may hold or may be revised based on what the contracts and boundaries reveal.

**Completion test**: Every decided position from the harvest appears in the description (traced). Every contract from Step 3 appears (stated). Every boundary classification from Step 2 appears (declared). The description is self-contained — reading it does not require reading the ADRs, though it links to them for traceability.

---

## 5. What Each Step Prevents

| Risk | Which step catches it |
|------|-----------------------|
| Inventing behavior no ADR decided | Step 3 (contracts must trace to harvest) |
| Crossing into implementation | Step 2 (boundary map classifies altitude) |
| Missing a decided position | Step 1 (systematic extraction with completion test) |
| Deferring without boundary ("we'll figure it out") | Step 2 (no unclassified items survive) |
| Missing an exploration that already resolved a concern | Step 1 + Step 4 (harvest covers explorations; gap identification cross-checks) |
| Asserting inter-primitive contracts from thin air | Step 3 (source trace required for every contract) |
| Leaving reporting, archival, etc. in limbo | Step 2 (must be classified inside or outside) |

---

## 6. Session Plan

| Session | Step | Key output | Estimated size |
|---------|------|-----------|----------------|
| 1 | Decision Harvest | Flat table of ~52+ sub-decisions with classifications and primitive mappings | ~200 lines |
| 2 | Boundary Mapping | Every concern classified: decided / spec-grade / impl-grade / outside | ~150 lines |
| 3 | Contract Extraction | ~15–25 inter-primitive contracts with ADR traceability | ~200 lines |
| 4 | Gap Identification | Gap register with typed gaps and recommended responses | ~100 lines |
| 4+ | Focused explorations | Only if Step 4 identifies gaps requiring exploration (0–2 sessions expected) | Variable |
| 5+ | Architecture Description | One section per session, in dependency order | ~1,000–1,200 lines total |

Steps 1–4 produce documents in `docs/exploration/` (numbered 24–27). Step 5+ produces documents in a directory determined after Step 4.

---

## 7. Relationship to Other Artifacts

| Artifact | Role in consolidation |
|----------|----------------------|
| ADRs (001–005) | Source of truth. Every position in the harvest traces here. |
| Exploration docs (01–21) | Evidence trail. Consulted when a harvest item needs clarification or when a gap might already be resolved. |
| Primitives inventory (doc 22) | Structural input. The inventory's primitive catalog is refined, not replaced. |
| Behavioral patterns (behavioral_patterns.md) | Coverage test input. Patterns are checked against contracts in Step 4. |
| Scenarios (S00–S21) | Coverage test input. Scenarios are checked against contracts in Step 4. |
| Checkpoints | Progress tracking. Checkpoints during consolidation reference this framework and report which step is current. |

**What this framework supersedes**: The specification structure proposal in doc 22 §6–§8 was premature — it proposed a file layout before contracts, boundaries, and gaps were identified. The consolidation framework replaces that plan. The inventory's structural catalog (§3–§5) remains valid input.

---

## 8. Completion Criteria

The consolidation phase is complete when:

- [ ] Every decided position appears in the Architecture Description, traced to its ADR
- [ ] Every inter-primitive interaction is covered by a contract with source trace
- [ ] Every "deferred" item has a boundary classification (decided / spec-grade / impl-grade / outside)
- [ ] Every behavioral pattern and scenario decomposes into contracted interactions (or is classified as outside)
- [ ] Zero unresolved gaps remain in the gap register
- [ ] The Architecture Description is self-contained — evaluable without reading ADRs
