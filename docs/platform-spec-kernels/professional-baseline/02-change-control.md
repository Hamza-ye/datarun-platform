# Change Control For Post-Baseline Claims

This document defines how later ADRs, new analysis, or implementation pressure may affect the ADR-001 through ADR-005 baseline.

## Baseline Rule

ADR-001 through ADR-005 are the current baseline until explicitly changed.

Later material may inform, clarify, or challenge the baseline, but it does not supersede it automatically.

## Claim Classification

Every post-baseline claim must be classified before it can affect the platform spec.

### Consistent Elaboration

The claim adds detail without changing a closed baseline rule.

Allowed action:

- capture as a candidate platform-spec detail
- link to the baseline item it elaborates

### Open-Gap Closure Candidate

The claim addresses an item explicitly left open or deferred.

Allowed action:

- add to the decision gap register as a proposed closure
- require review before marking settled

### Deferred Implementation Or Spec Detail

The claim concerns tooling, UX, performance strategy, schema format, migration mechanics, or operational policy without changing architecture.

Allowed action:

- place in the deferred implementation/spec section
- do not promote to architecture baseline

### New Unauthorized Claim

The claim decides something that was not open and not previously authorized.

Allowed action:

- record as unauthorized
- do not add to baseline
- require new formal decision if the team wants it

### Conflict With Closed Baseline

The claim contradicts ADR-001 through ADR-005 closure.

Allowed action:

- record the conflict
- keep the baseline unchanged
- require formal reopen if the conflict is considered valid

### Valid Dispute

The claim exposes a serious defect in the baseline using already approved evidence or a newly accepted engineering constraint.

Allowed action:

- record as a formal dispute
- identify affected baseline items
- require explicit decision process before any baseline change

## ADR-006-R Through ADR-009

ADR-006-R through ADR-009 are post-convergence assessment material.

They must be evaluated through the classifications above.

Specific rule:

- ADR-006-R may supersede ADR-006 only inside the ADR-006 revision lineage.
- ADR-006-R does not supersede ADR-001 through ADR-005.
- ADR-007 through ADR-009 do not supersede ADR-001 through ADR-005.

## Flag-Specific Constraint

Flag-related claims must keep these boundaries separate:

- flag category creation
- conflict detection timing
- source-only cascade/projection lineage
- unresolved-flag state derivation
- flag resolution and auto-resolution
- general flag semantics

Do not merge these unless the accepted baseline or a later formal decision explicitly does so.

## Required Record For Any Change

Any accepted change to the baseline must record:

- affected baseline item
- source claim
- classification
- decision owner
- result
- reason the result does not silently override prior closure
