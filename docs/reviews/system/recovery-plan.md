# Recovery Plan — Senior Architect + Project Shepherd

---

## 1. System State

**Fragile**

- Architecture is sound. ADRs 001–009 are correct and internally consistent.
- Code delivers its intended behavioral slices (46/46 tests pass).
- Fragility is in triage quality and process discipline, not in design.

Specific fragility indicators:
- One live detection gap in production (SC-01: v2 identity conflict)
- One false STABLE claim in the concept ledger (SC-05: `field_count_budget`)
- One mega-FP with no milestone and 5+ sub-obligations (SC-02: FP-012)
- One behavioral asymmetry carried as observation for 3 Ships (SC-04: scope eval)
- One foundational ADR (ADR-005) with zero production coverage
- Three-Ship deferred test gate (SC-06: FP-001)

---

## 2. Mode Decision

**Continue — with mandatory pre-Ship-4 cleanup gate**

Do not pause. Do not reset. The architecture is valid and the ship cadence is effective.

Justification:
- All Phase 1 scenario coverage to date is correct.
- The foundational invariants (append-only, accept-and-flag, type vocabulary closed) are intact.
- The fragility items are bounded and individually fixable.
- A pause risks losing cadence; a controlled reset would require re-converging on decisions already correctly made.

The pre-Ship-4 gate is not optional. Starting Ship-4 (ADR-005 first exercise) with the current state of SC-01, SC-03, and SC-05 would compound existing fragility under implementation pressure.

---

## 3. Recovery Flow (Sequential)

**Step 1 — Re-anchor on domain**
Read: Constraints doc, behavioral patterns P01–P11, Access Control scenario. Confirm Ship-4's intended scenario(s) address a real Phase 1 behavioral need. Do not proceed if the scenario choice cannot be traced to a specific behavioral pattern.

**Step 2 — Re-validate ADR intent**
For each of ADR-001 through ADR-005: confirm the intended §S positions are met by existing code. Specifically: walk ADR-005 §S1–§S9 against the codebase. Mark each position as (a) met, (b) first exercised by Ship-4, or (c) unmet with no Ship planned.

**Step 3 — Re-check convergence artifacts**
- Correct the `field_count_budget` ledger row from STABLE to DEFERRED.
- Open a new Flagged Position for the v2-current identity conflict detection gap (promote from SC-01).
- Open a new Flagged Position for scope eval asymmetry (promote from SC-04/SC-09).
- Open a new Flagged Position for alias cycle detection (promote from SC-07/C2-03).
- Decompose FP-012 into individual FPs (see pm.md SC-02).

**Step 4 — Re-evaluate Ship-1 → Ship-3 slices vs intent**
Confirm: the shepherd verdicts in `docs/reviews/ship-*/shepherd.md` match the retro record. No re-delivery needed — but any "Aligned with drift signal" verdict must have a concrete correction action before Ship-4 starts.

**Step 5 — Define corrected baseline (END STATE of Ship-3)**
Ship-3 end state, corrected:
- S00, S01, S03, S06, S06b delivered and passing (46/46 tests)
- v2 identity conflict detection **gap exists** (not resolved; new FP opened)
- Scope eval asymmetry **gap exists** (not resolved; new FP opened)
- ADR-005: zero production coverage (documented; Ship-4 first exercise)
- FP-012 decomposed into individual FPs
- `field_count_budget` ledger row corrected to DEFERRED

---

## 4. Immediate Actions (Before Any New Ship)

1. **Fix SC-01**: Parameterize `ConflictDetector` to detect on all `household_observation/*` versions. Add test.
2. **Fix SC-05**: Correct ledger row `field_count_budget` from STABLE to DEFERRED. Open FP for §S6 implementation.
3. **Fix SC-06/FP-001**: Author the temporal divergence integration test. 2-hour bounded task. No more deferrals.
4. **Promote SC-04 to FP**: Scope evaluation asymmetry becomes a Flagged Position with gate: pull path uses event-time scope.
5. **Promote SC-07 to FP**: Alias cycle detection becomes a Flagged Position. Add cycle guard to merge path (engineering).
6. **Decompose FP-012**: Replace with 4–5 individual FPs, each with a single gate and trigger condition.
7. **Add C2-05 test**: One negative test that verifies `subjects_unmerged` is rejected.
8. **Walk ADR-005 §S positions**: Document which are first exercised by Ship-4 and which remain deferred.

---

## 5. Rhythm Redefinition

1. **Pre-ship**: Evaluate all OPEN FPs against the new Ship's scope. Any FP with a matching trigger must either close this Ship or be explicitly re-deferred with a reason and a later trigger.
2. **Pre-ship**: Walk the relevant ADR §S positions against the current codebase. Any unmet §S position that the Ship will first exercise must appear as a Ship sub-decision.
3. **In-ship**: Every retro observation that names a behavioral gap must either become an FP or be dismissed with a written reason. No silent carry-forward.
4. **In-ship**: Ledger rows added at Ship close must name the code artifact they describe. "STABLE" requires working code, not just a decided concept.
5. **Post-ship retro**: Retro must include an explicit section: "What was deferred and why?" Each deferred item must have a named trigger.
6. **Post-ship retro**: Retro must confirm every FP that was evaluated this Ship and record the evaluation result (triggered / not triggered / re-deferred with reason).
7. **Standing constraint**: No mega-FPs. A single FP with more than 2 sub-obligations must be decomposed before the Ship that opened it closes.
8. **Standing constraint**: No STABLE ledger row for a mechanism that is not implemented. If the concept is decided but unbuilt, status is DEFERRED.

---

## 6. Disturbance Control

**Must NOT be changed:**
- The 11-field envelope (ADR-001 §S3 + ADR-008, INVARIANT)
- The 6-value type vocabulary (ADR-007 §S1, INVARIANT)
- The append-only event store (ADR-001 §S1, foundational)
- The accept-and-flag invariant (ADR-006 §S1)
- The `actor_ref` system format: `system:{source_type}/{source_id}` (ADR-008 §S2)
- Stored events of any shape version (append-only; no migration of payload content)

**Can be safely corrected:**
- `ConflictDetector` shape version check (additive; constant → set)
- Concept ledger row for `field_count_budget` (text correction)
- FP-012 decomposition (text operation; no code)
- Merge guard cycle detection (additive server-side check)
- Temporal divergence test (new test; no code change to production path)
- `subject_split/v1` optional `correction_of` field (additive shape evolution)

**How to avoid cascading rewrites:**
- Fix at the narrowest scope. `ConflictDetector` fix is one constant → set change; it does not require touching validation, routing, or projection.
- Do not combine corrections with new feature work. Each fix is a standalone commit with its own test.
- Run full test suite (46 tests) after each individual fix before proceeding.

---

## 7. Validation Strategy

**Mental walkthrough is sufficient when:**
- The change is additive only (new test, new FP, ledger correction)
- The changed component has no cross-cutting dependencies
- The change is constrained to one file and one behavior

**Event storming is required when:**
- A Ship first exercises an ADR that has never been in production (e.g., ADR-005 in Ship-4)
- A change modifies how events are validated, routed, or flagged
- A new `shape_ref` is introduced that detection logic must recognize
- A change touches `ScopeResolver` or the pull filter path

**Signals that trigger deeper analysis:**
- A retro observation uses the word "noted" without an FP citation
- A ledger row is marked STABLE at Ship close without naming a code artifact
- A Ship sub-decision references an ADR §S position that has never been exercised
- A Flagged Position has been re-deferred twice without a new trigger condition

---

## 8. Clean Re-entry Point

**The system is safe to start Ship-4 when ALL of the following are true:**

1. SC-01 is fixed: `ConflictDetector` detects on all `household_observation/*` versions and a test confirms v2 detection.
2. SC-05 is corrected: `field_count_budget` ledger row is DEFERRED; a new FP exists for §S6 implementation.
3. SC-06 is closed: FP-001 temporal divergence test has been authored and passes.
4. SC-04 is promoted: Scope eval asymmetry is a Flagged Position with a concrete gate.
5. SC-07 is promoted: Alias cycle detection is a Flagged Position; merge guard has a cycle check.
6. FP-012 is decomposed: At least 4 individual FPs exist to replace it; FP-012 is marked SUPERSEDED.
7. ADR-005 walk is complete: All §S positions are classified as (met / first-in-Ship-4 / deferred-with-FP).
8. `docs/ships/ship-3-retro.md` is updated to reflect the corrected baseline (items 1–7 above noted as post-close corrections).

**No new Ship starts until these 8 conditions are verified.**
