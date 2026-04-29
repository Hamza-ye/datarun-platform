# Software Engineer — Assigned Concerns

## SC-01 — Identity conflict detection absent for v2 events (live production gap)

**Issue**: `ConflictDetector` constant `HOUSEHOLD_SHAPE = "household_observation/v1"` means all v2 captures bypass identity conflict detection. v2 is the current shape at Ship-3 close. This is not a future risk — it is active today.

**Risk if ignored**: Duplicate household registrations (same normalized name, same village, different subject_id) submitted under v2 are silently admitted. Field programs operating with v2 (which should be all of them post-Ship-3) have no duplicate detection.

**Action**:
- *Now*: Replace the single constant with a set of recognized household shapes. `ConflictDetector` should detect on all versions of `household_observation/*` (or a configurable prefix). Add a test for v2 identity conflict detection.
- *Later*: When FP-012 deployer surface is built, make the set of detection-eligible shapes configurable per deployer.

**Reversibility**: High. Constant → set change; additive behavior; existing v1 tests unaffected.

---

## SC-05 — `field_count_budget` STABLE but ADR-004 §S6 enforcement unimplemented

**Issue**: The concept ledger marks `field_count_budget` STABLE at Ship-3 close, but the enforcement mechanism (atomic validation at shape registration time, ADR-004 §S6) is not implemented. `ShapePayloadValidator` validates events against schemas but does not enforce a budget when schemas are registered.

**Risk if ignored**: Future agents reading the ledger will assume budget enforcement exists and build downstream logic on that assumption. Any deployer-submitted shape with excessive fields will be accepted at runtime, potentially exposing the performance floor noted in ADR-004 §S13.

**Action**:
- *Now*: Correct the ledger entry: `field_count_budget` should be DEFERRED until §S6 is implemented. Add a new FP for the implementation gap.
- *Later*: Implement §S6 atomicity as part of FP-012 decomposition (see pm.md SC-02).

**Reversibility**: High for ledger correction. Medium for implementation (requires schema registry change).

---

## SC-10 — ADR-004 §S13 HTTP enforcement exists only as a unit test

**Issue**: ADR-004 §S13 specifies that the field-count budget is enforced at runtime (HTTP boundary). The current implementation validates only in unit tests; a runtime HTTP request submitting a shape with too many fields bypasses the limit.

**Risk if ignored**: Deployers can register unbounded shapes. As the platform scales, unconstrained shapes produce unbounded event payloads, unbounded projection cost, and potential JSONB storage pathologies.

**Action**:
- *Now*: Add a runtime guard in `ShapePayloadValidator` (or the shape registration endpoint) that enforces the field budget at the HTTP boundary. The unit test becomes a test of the guard, not a substitute for it.
- *Later*: Confirm ADR-004 §S13 is satisfied in the Ship retro via a walkthrough that tries to register an over-budget shape and receives a 400.

**Reversibility**: High. Additive guard; no schema change.

---

## C3-03 — `field_count_budget` STABLE claim is false

*(Cross-reference: SC-05 — see above.)*

**Action**: Immediate: correct ledger entry to DEFERRED. Open replacement FP. This is a one-line ledger change.

**Reversibility**: High.

---

## C2-05 — No test verifies `subjects_unmerged` attempt is rejected

**Issue**: `SubjectsUnmerged` rejection is an ADR-002 invariant. No test verifies that an event with a `subjects_unmerged` shape_ref is rejected by `ShapePayloadValidator`. If the validator is changed, the invariant is silently dropped.

**Action**:
- *Now*: Add one negative test: push a `subjects_unmerged` event, assert 400 or validation failure.
- *Later*: Review whether other "must-never-be-admitted" shapes have similar test gaps.

**Reversibility**: High. Test addition only.

---

## C3-05 — `ShapePayloadValidator` dual filename convention is undocumented

**Issue**: The validator handles two naming conventions for shape schema files. A shape added under a third convention (e.g., dotted version suffix) would silently fail validation with an opaque "unknown shape_ref" error rather than a "schema file not found" error.

**Action**:
- *Now*: Add a comment in `ShapePayloadValidator` documenting the supported naming conventions and the expected error behavior for unsupported conventions.
- *Later*: Standardize on one naming convention and add a startup assertion that all bundled schemas match the convention.

**Reversibility**: High. Documentation and assertion; no behavioral change.
