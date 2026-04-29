# System Rhythm — Project Shepherd

## Intended Rhythm

Based on the domain → ADRs → convergence → ships ordering:

1. **Domain anchoring**: each Ship spec begins with scenario selection from the Phase 1 set (S00–S14, S22); the scenario defines the behavioral claim; architecture constrains the implementation.
2. **ADR-gated decisions**: any implementation choice not already decided by an ADR becomes a sub-decision in the Ship spec, with an explicit record of what was considered and why.
3. **FP discipline**: every deferral is promoted to a Flagged Position with a named gate; gate is re-evaluated at the start of the next eligible Ship.
4. **Convergence drift gate**: `check-convergence.sh` passes at close; ledger rows added only for new stable concepts backed by working code.
5. **Retro gate**: retro confirms every spec claim against code; contradictions become FPs, not observations.

## Actual Execution Pattern

| Ship | Scenario coverage | FP discipline | ADR gates | Ledger accuracy | Rhythm signal |
|---|---|---|---|---|---|
| Ship-1 | ✔ S00, S01, S03, S19 | ✖ Scope asymmetry not promoted | ✔ | ✔ | Mostly correct |
| Ship-2 | ✔ S06 lifecycle | ✔ FP-002, FP-007 closed; FP-006 opened | ✔ | ✔ | Correct |
| Ship-3 | ✔ S06b evolution | ✖ v2-current gap folded into FP-012; FP-001 deferred again | ✖ `field_count_budget` STABLE but unbuilt | ✖ | Degrading |

## Where Rhythm Broke

**Ship-1 §3.3 (scope asymmetry observation)**
Retro records the observation but does not promote it to an FP. Rule R-1 (observe → FP) not applied. Sets precedent: observations can remain observations.

**Ship-3 FP-012 triage**
A live production behavioral gap (v2 identity conflict detection) is folded into a mega-FP instead of being opened as its own item with a concrete, near-term gate. This is the clearest rhythm failure: a RESOLVE-eligible item was classified as DEFERRED.

**Ship-3 ledger close-out**
`field_count_budget` added as STABLE when the mechanism it names is not implemented. Ledger rule 3 (STABLE = classification unchanged AND no upstream change AND implementation is correct) is silently violated. The charter's drift gate status says PASS, but the underlying claim is false.

**FP-001 (all three Ships)**
The temporal divergence test has been deferred at every Ship close without a trigger-evaluation. The stated trigger ("before the Ship that first depends on role-action enforcement") has never been evaluated because no Ship proactively asked "does this Ship depend on role-action enforcement?"

## Repeating Drift Patterns

1. **Observation-not-FP**: A retro notes a gap, does not promote it to an FP, and the gap carries forward invisibly.
2. **Mega-FP accumulation**: Multiple distinct concerns accumulate under a single FP, removing forcing functions from each individual concern.
3. **Ledger ahead of code**: Concept ledger marks items STABLE before the code they describe is verified to work at those semantics.
4. **FP trigger never evaluated**: FPs with conditional triggers ("before the Ship that first requires X") are never actively evaluated; they wait for someone to notice.

## Verdict

**Degrading**

The rhythm is not broken — each Ship delivered its intended behavioral slice correctly, tests pass, and the convergence artifacts are maintained. But three process failures at Ship-3 close (mega-FP triage, false STABLE claim, observation-not-FP) represent a qualitative shift from the Ship-1/2 discipline. If the pattern continues for one more Ship, the overhead of untangling accumulated debt will exceed the overhead of pausing to clean up now.
