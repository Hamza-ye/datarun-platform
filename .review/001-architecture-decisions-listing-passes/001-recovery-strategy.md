# Architecture Recovery Strategy

> **Objective**: Reconstruct the settled architecture, vocabulary, taxonomy, primitive boundaries, and interaction model from the original exploration lineage that produced ADR-001 → ADR-005.
>
> **Constraint**: Archaeological reconstruction only — no redesign, no alternatives, no reopening.

---

## Problem Diagnosis

### Why drift happened

The exploration files contain rich, scenario-grounded reasoning that was **compressed** into ADRs. Two layers were never consolidated:

1. **Missing Vocabulary Layer** — The ADRs define constraints but never explicitly name and map the architectural building blocks those constraints shape. Primitives, their responsibilities, boundaries, and interactions remained implicit.

2. **Missing Decision Boundary Discipline** — No explicit distinction between irreversible architectural constraints vs. evolvable implementation strategies. This caused implementation details, exploratory alternatives, settled concepts, and rejected paths to intermix.

### What confusion this produced

AI agents repeatedly misunderstood:

- **Identity model**: Which identity categories exist, their lifecycles, how they relate
- **Conflict detection/resolution**: What gets flagged, when, by whom, what blocks what
- **Authorization boundaries**: What's in the envelope vs. projection, scope mechanics
- **Fixed vocabulary**: What's platform-fixed vs. deployer-configured, the type vocabulary
- **Primitive boundaries**: Where one primitive ends and another begins

### Root cause

The ADRs preserved *decisions* but not *proof density*. The exploration files preserved *reasoning* but mixed it with rejected alternatives. No document existed that held only the settled reasoning at the vocabulary and boundary level.

---

## Source File Inventory

### ADR-002 Lineage (Identity + Conflict) — 1,998 lines

| File | Lines | Content | Risk |
|------|-------|---------|------|
| [05](file:///home/hamza/datarun-platform/docs/exploration/archive/05-adr2-event-storm-identity.md) | 644 | Event storms for S01, S06, S19, S03, S07. Identity taxonomy discovery, conflict type taxonomy, aggregate map | Contains exploratory acts with both accepted and rejected paths |
| [07](file:///home/hamza/datarun-platform/docs/exploration/archive/07-adr2-phase2-stress-test-results.md) | 825 | Stress tests: accept-and-flag (A1-A6), alias table (B1-B6), causal ordering (C1-C5), combination tests (α,β,γ), invariant survival, missing paths | Heavy branching — rejected mechanisms (e.g. unmerge B6) mixed with accepted ones |
| [09](file:///home/hamza/datarun-platform/docs/exploration/archive/09-adr2-phase3-classification-results.md) | 529 | Classification into 4 buckets, S00 simplicity validation, ADR-2 decision skeleton | **Cleanest file** — already classifies what's settled vs. deferred |

### ADR-003 Lineage (Authorization + Sync) — 1,753 lines

| File | Lines | Content | Risk |
|------|-------|---------|------|
| [10](file:///home/hamza/datarun-platform/docs/exploration/archive/10-adr3-phase1-policy-scenarios.md) | 685 | Scenario walkthroughs (S20/S21, S03, S09, S14, S19) through auth lens. Cross-cutting synthesis (Q1-Q5), hot spots, emergent questions (EQ1-EQ6) | Emergent questions contain provisional positions that were later refined |
| [11](file:///home/hamza/datarun-platform/docs/exploration/archive/11-adr3-phase2-stress-test.md) | 823 | Stress tests: access rules (A1-A5), sync scope (B1-B5), stale rules (C1-C5), projections (D1-D4), envelope authority (E1-E5), combo scenarios (α,β,γ,δ), invariant survival | Authority-in-envelope was explored then rejected — high contamination risk |
| [12](file:///home/hamza/datarun-platform/docs/exploration/archive/12-adr3-course-correction.md) | 245 | Course correction — refinements after stress test | Short, likely contains important pivots |

### ADR-004 Lineage (Configuration + Structure) — 3,653 lines

| File | Lines | Content | Risk |
|------|-------|---------|------|
| [14](file:///home/hamza/datarun-platform/docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md) | 1,158 | 5 scenario walkthroughs through config lens, 12 questions discovered | **Largest file**. Questions are provisional — many evolved through Sessions 3-4 |
| [15](file:///home/hamza/datarun-platform/docs/exploration/archive/15-adr4-session3-part1-structural-coherence.md) | 924 | Structural coherence audit across all positions | Contains checks that confirmed or modified positions |
| [16](file:///home/hamza/datarun-platform/docs/exploration/archive/16-adr4-session3-part2-irreversibility-filter.md) | 470 | Irreversibility filter applied to 24 candidate positions | **High-value** — explicitly separates structural from evolvable |
| [17](file:///home/hamza/datarun-platform/docs/exploration/archive/17-adr4-session3-part3-adversarial-stress-tests.md) | 506 | 3 adversarial attacks on envelope positions + 3 light validations | Attack outcomes confirm or modify positions |
| [18](file:///home/hamza/datarun-platform/docs/exploration/archive/18-adr4-session3-part4-remaining-q-resolution.md) | 595 | Resolution of Q7, Q9, Q10, Q12. Carry-forwards from Part 3. Complete synthesis with all positions | **Second cleanest file** — contains final synthesis table |

**Total: ~7,400 lines across 11 files.**

---

## Extraction Methodology

### Core Principle: ADR-Anchored Extraction

Every extraction pass uses the settled ADR sub-decisions as verification anchors. We read the exploration to recover **the reasoning, vocabulary, and boundaries behind** specific ADR sub-decisions — not to discover new positions.

### Contamination Prevention Rules

1. **ADR checkpoint**: Before extracting from any file, list the specific ADR sub-decisions we're looking for. Only extract material that maps to those sub-decisions.
2. **Reject-tag discipline**: When a rejected alternative is encountered, note it as `[REJECTED: reason]` and move on. Do not incorporate rejected reasoning into the vocabulary.
3. **Provisional-tag discipline**: When a provisional position is encountered that was later refined, note it as `[PROVISIONAL → final position in ADR-X SY]` and use only the final form.
4. **No forward-contamination**: Do not let ADR-003 exploration findings reshape ADR-002 vocabulary. Each lineage is extracted independently first.
5. **Vocabulary lock**: Once a term is extracted and verified against the ADR, it is locked. Later files cannot redefine it.

---

## Phase Plan

### Phase 0: Build the Verification Anchor

**Input**: ADR-001 through ADR-005 (already read)

**Action**: Create a structured register of every settled sub-decision across all 5 ADRs, organized by:

- Sub-decision ID (e.g., ADR-002 S6)
- Classification (structural / strategy-protecting / initial strategy)
- The vocabulary terms it introduces or constrains
- What it explicitly defers

**Output**: A **Decision Register** — the mechanical checklist every extraction pass is verified against.

**Why this phase exists**: Without the register, extraction becomes subjective. The register makes it mechanical: "Did exploration file X produce reasoning that maps to ADR-Y SZ? If yes, extract the vocabulary and boundary reasoning. If no, skip."

**Estimated effort**: Synthesize from already-read ADRs. No new file reads needed.

---

### Phase 1: ADR-002 Lineage — Identity + Conflict Model

**Target sub-decisions**: ADR-002 S1–S14

**Reading order**: 09 → 05 → 07 (reverse — start with the cleanest classification, then fill in the reasoning)

#### Step 1a: Read file 09 (529 lines)

- This file already classifies findings into Bucket 1 (ADR constraints), Bucket 2 (strategies), Bucket 3 (deferred), Bucket 4 (risks)
- Extract: the classification rationale for each item, the S00 simplicity walk-through, the decision skeleton
- This gives us the **map** of what's settled before we dive into the dense exploration

#### Step 1b: Read file 05 (644 lines), guided by 09's map

- Extract only: identity taxonomy (4 categories), conflict type taxonomy, causal ordering discovery, aggregate map
- Skip: exploratory acts that didn't survive (check against ADR-002)
- Key vocabulary to recover: subject, actor, process, assignment identity categories; conflict types; event storm aggregates

#### Step 1c: Read file 07 (825 lines), guided by 09's map

- Extract only: stress test findings that became ADR-002 constraints (A2→S11, B2→S13, B4→S9, B6→S7, C3→S3, M8→S5, etc.)
- Skip: findings classified as strategies or deferred (already mapped by 09)
- Key vocabulary to recover: accept-and-flag mechanics under load, alias resolution boundaries, single-writer reasoning, detect-before-act reasoning

#### Phase 1 Output

- Identity architecture vocabulary and boundaries
- Conflict detection and resolution model vocabulary
- Causal ordering semantics
- Verified against ADR-002 S1–S14

---

### Phase 2: ADR-003 Lineage — Authorization + Access Control

**Target sub-decisions**: ADR-003 S1–S10

**Reading order**: 12 → 10 → 11 (start with the correction to know what pivoted, then fill in)

#### Step 2a: Read file 12 (245 lines)

- Short file — likely contains the key pivot points from Phase 2 stress testing
- Extract: what was corrected and why
- This tells us which Phase 1 positions were revised before becoming ADR-003

#### Step 2b: Read file 10 (685 lines), guided by 12's corrections

- Extract only: emergent questions (EQ1-EQ6) that became ADR-003 sub-decisions, cross-cutting synthesis positions that survived
- Key vocabulary to recover: assignment model, sync scope semantics, scope types, authority context reasoning
- Skip: hot spots that were resolved as "not structural" or deferred

#### Step 2c: Read file 11 (823 lines), guided by 12's corrections

- Extract only: findings that became ADR-003 constraints
- **Critical**: Section E (Authority Context in Envelope) — this was explored and **rejected** (ADR-003 S3: no new envelope fields). Must extract the *rejection reasoning* as boundary-defining evidence, not the rejected design.
- Key vocabulary to recover: scope containment, assignment-based access, sync=access reasoning, flag type extensions

#### Phase 2 Output

- Authorization architecture vocabulary and boundaries
- Scope model and containment semantics
- Authority context derivation model
- Verified against ADR-003 S1–S10

---

### Phase 3: ADR-004 Lineage — Configuration + Structural Coherence

**Target sub-decisions**: ADR-004 S1–S14

**Reading order**: 18 → 16 → 15 → 17 → 14 (start with final synthesis, work backward)

#### Step 3a: Read file 18 (595 lines)

- Contains the complete synthesis table of all positions after Session 3
- Extract: final position on each question (Q7, Q9, Q10, Q12), carry-forwards, envelope status
- This gives us the **final vocabulary** before ADR-004 was written

#### Step 3b: Read file 16 (470 lines)

- Irreversibility filter — explicitly classifies 24 positions as structural vs. evolvable
- Extract: the classification of each position, the boundary reasoning
- **High-value for the missing decision boundary discipline**

#### Step 3c: Read file 15 (924 lines)

- Structural coherence audit
- Extract only: checks that confirmed positions, checks that modified positions
- Key vocabulary to recover: pattern registry concept, composition rules, expression language boundaries

#### Step 3d: Read file 17 (506 lines)

- Adversarial attacks on the 3 envelope positions (activity_ref, shape_ref, type vocabulary)
- Extract: attack outcomes that confirmed or modified the positions
- Key vocabulary to recover: type vocabulary closure reasoning, shape_ref format reasoning

#### Step 3e: Read file 14 (1,158 lines) — selective

- **Do not read front-to-back**. Use the section headers to target only:
  - Scenario walkthroughs that produced vocabulary still present in ADR-004
  - The 12 questions (Q1-Q12) as discovered — but only their final form (verified against 18's synthesis)
- Skip: exploratory reasoning that was superseded by Sessions 3-4

#### Phase 3 Output

- Configuration boundary vocabulary and architecture
- Event type vocabulary and closure reasoning
- Shape model and versioning semantics
- Trigger architecture and limits
- Expression language boundaries
- Complexity budgets and their calibration
- Verified against ADR-004 S1–S14

---

### Phase 4: Cross-Lineage Vocabulary Consolidation

**Input**: Phase 1, 2, 3 outputs + ADR-005 (already read, no exploration files assigned)

**Action**:

1. Merge vocabulary from all three lineages
2. Resolve any terminology inconsistencies (same concept, different names across lineages)
3. Add ADR-005 vocabulary (patterns, composition rules, context scope, auto-resolution) — ADR-005's exploration files are not in scope, but the ADR itself is authoritative
4. Build the primitive taxonomy: what is a primitive, what is a contract, what is configuration
5. Map primitive boundaries: where each primitive starts and stops
6. Map primitive interactions: how primitives compose

**Contamination check**: Every term in the consolidated vocabulary must trace to a specific ADR sub-decision. If a term exists only in the exploration but was never encoded into an ADR, it is flagged as "exploration-only — not settled" and excluded.

#### Phase 4 Output

- Unified platform vocabulary
- Primitive taxonomy with boundaries
- Interaction model (how primitives compose)

---

### Phase 5: Produce the Authoritative Architecture Map

**Input**: Phase 4 output

**Action**: Write the single document that should have existed — the consolidated architecture reference containing:

1. **Platform Vocabulary** — every fixed term, its definition, its source ADR
2. **Primitive Taxonomy** — what is a primitive, what is a contract, what is configuration, what is implementation
3. **Identity Architecture** — the 4 identity categories, their lifecycles, their boundaries
4. **Event Contract** — the 11-field envelope, type vocabulary, shape_ref, activity_ref
5. **Conflict Detection & Resolution Model** — flag categories, detection pipeline, resolution rules, accept-and-flag mechanics
6. **Authorization & Access Control** — assignment model, scope types, sync=access, authority-as-projection
7. **Configuration Boundary** — four-layer gradient, expression language, triggers, budgets
8. **State Progression** — projection-derived state, pattern registry, composition rules
9. **Interaction Model** — how all of the above compose together
10. **Decision Boundary Map** — what's irreversible (structural), what's guarded (strategy-protecting), what's evolvable (initial strategy)

**Format**: This is NOT an ADR. It is a **vocabulary and boundary reference** — the missing layer between ADRs and implementation.

---

## Risk Controls

| Risk | Mitigation |
|------|-----------|
| Exploration file contains rejected alternative that reads like settled reasoning | ADR checkpoint: every extracted position must map to a specific sub-decision |
| Terminology drifts between lineages (e.g., "process" in ADR-002 vs. later usage) | Vocabulary lock: first verified occurrence wins; conflicts are flagged explicitly |
| Extraction scope creeps into implementation details | Classification filter: if it's not about vocabulary, boundaries, or interactions, it's implementation — skip |
| File is too large to process without losing context | Reverse reading order (clean → dense) ensures the classification frame is established before the dense material |
| Cross-lineage contamination (ADR-003 findings reshape ADR-002 terms) | Independent extraction per lineage; merging only in Phase 4 under explicit rules |

---

## Estimated Effort per Phase

| Phase | New lines to read | Primary risk | Dependency |
|-------|-------------------|--------------|------------|
| 0 | 0 (synthesis from ADRs) | Incomplete register | None |
| 1 | ~1,998 | Rejected alternatives in file 07 | Phase 0 |
| 2 | ~1,753 | Rejected envelope authority in file 11 | Phase 0 |
| 3 | ~3,653 | File 14 is 1,158 lines with heavy exploration | Phase 0 |
| 4 | 0 (synthesis) | Terminology conflicts | Phases 1-3 |
| 5 | 0 (authoring) | Scope creep into new architecture | Phase 4 |

> [!IMPORTANT]
> Phases 1, 2, and 3 can run in parallel since they are independently anchored by the Phase 0 register and have no cross-dependencies until Phase 4.

---

## Success Criteria

The final output is authoritative if and only if:

1. **Every term** traces to a specific ADR sub-decision
2. **No term** exists only in exploration files without ADR encoding
3. **No rejected alternative** appears as settled vocabulary
4. **The primitive taxonomy** is consistent with the ADR classification (structural / strategy-protecting / initial strategy)
5. **The interaction model** is derivable from ADR composition rules without inventing new relationships
6. **S00 (basic capture)** remains simple under the full vocabulary — the litmus test
