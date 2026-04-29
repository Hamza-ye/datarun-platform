# System Concerns

## SC-01

**Statement**: Identity conflict detection is silently absent for `household_observation/v2` (current shape) events. Live production gap.

**Affected Ships**: Ship-3 (introduced gap)

**Root Cause**: `ConflictDetector` version-pinned to v1 at Ship-1; not updated when v2 became the current shape at Ship-3; incorrectly triaged into mega-FP-012 instead of opened as its own FP.

**Risk Type**: Hidden Coupling + ADR Drift

---

## SC-02

**Statement**: FP-012 carries 5–6 distinct concerns with no milestone boundary; none of its sub-obligations has a forcing function.

**Affected Ships**: Ship-1 (origin), Ship-2, Ship-3 (accumulation)

**Root Cause**: ADR-004 deployer-authoring surface was deferred at Ship-1 and accumulated sub-obligations at each subsequent Ship close without decomposition or milestone assignment.

**Risk Type**: Deferred Complexity Debt

---

## SC-03

**Statement**: ADR-005 (state progression, pattern registry) has never been exercised in production code across Ships 1–3. All §S positions unvalidated.

**Affected Ships**: Ship-1, Ship-2, Ship-3

**Root Cause**: No Phase 1 scenario in Ships 1–3 required a workflow pattern. First exercise is Ship-4 (draft). All ADR-005 assumptions will surface together for the first time at Ship-4.

**Risk Type**: Architectural Risk

---

## SC-04

**Statement**: Scope evaluation asymmetry (push=event-time, pull=request-time) is a domain-correctness gap carried as an observation across 3 Ships.

**Affected Ships**: Ship-1 (noted), Ship-2, Ship-3 (unresolved)

**Root Cause**: Retro §3.3 notes the asymmetry but does not promote it to an FP; no gate assigned; no Ship re-evaluation.

**Risk Type**: Domain Misalignment

---

## SC-05

**Statement**: `field_count_budget` marked STABLE in concept ledger but ADR-004 §S6 atomicity enforcement is not implemented.

**Affected Ships**: Ship-3 (false STABLE claim)

**Root Cause**: Ledger close-out added concept as STABLE at Ship-3; the enforcement mechanism was "simulated" since Ship-1 and remains unbuilt; ledger rule 3 silently violated.

**Risk Type**: ADR Drift + Slice Integrity Issue

---

## SC-06

**Statement**: FP-001 temporal divergence test deferred for 3 consecutive Ships with no trigger evaluation and no progress.

**Affected Ships**: Ship-1, Ship-2, Ship-3

**Root Cause**: Trigger is conditional ("before the Ship that first requires role-action enforcement") but no Ship proactively evaluates whether it is the trigger Ship; FP stays perpetually "not yet relevant."

**Risk Type**: Deferred Complexity Debt

---

## SC-07

**Statement**: Alias cycle detection is absent; a cyclic merge graph produces infinite traversal with no remediation path due to append-only invariant.

**Affected Ships**: Ship-2 (introduced merge), Ship-3

**Root Cause**: Merge guard checks for duplicate events but not for cycles in the alias graph. The append-only invariant (ADR-001 §S1) means no correction path exists post-cycle.

**Risk Type**: Architectural Risk + High Irreversibility

---

## SC-08

**Statement**: Drift gate (`check-convergence.sh`) checks byte-identity of shape files but not behavioral conformance; green status overstates protection.

**Affected Ships**: Ship-2 (gate introduced), Ship-3

**Root Cause**: Gate design is file-hash-based; a semantically breaking schema change that preserves file content (e.g., field rename, constraint weakening) would pass the gate.

**Risk Type**: Constraint Violation (gap in gate design)

---

## SC-09

**Statement**: Pull-time scope filter uses request-time assignment; for historical event access (audits, corrections), this is wrong per domain requirements.

**Affected Ships**: Ship-1, Ship-2, Ship-3

**Root Cause**: `SyncController` pull path was implemented with request-time scope; Access Control scenario requires event-time authority attribution; the asymmetry was documented but not fixed.

**Risk Type**: Domain Misalignment

---

## SC-10

**Statement**: ADR-004 §S13 HTTP enforcement exists only as a unit test; runtime enforcement is not present.

**Affected Ships**: Ship-3 (ADR-004 §S13 first cited as "test-only")

**Root Cause**: ADR-004 specifies runtime enforcement of field-count budget at shape registration; the implementation validates in tests only; deployer-submitted shapes bypass the limit at runtime.

**Risk Type**: Constraint Violation
