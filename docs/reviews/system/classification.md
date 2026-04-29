# Final Classification

| Concern | Type | Severity | Reversibility | Owner |
|---|---|---|---|---|
| SC-01: Identity conflict detection absent for v2 events | Hidden Coupling | **Critical** | High | Software Engineer |
| SC-02: FP-012 mega-scope, no milestone, no forcing functions | Deferred Complexity Debt | **High** | High | Senior Project Manager |
| SC-03: ADR-005 zero production coverage; all §S unvalidated | Architectural Risk | **High** | Moderate | Software Architect |
| SC-04: Scope eval asymmetry (push=event-time, pull=request-time) | Domain Misalignment | **High** | Moderate | Data/Consistency Specialist |
| SC-05: `field_count_budget` STABLE but enforcement unimplemented | ADR Drift + Slice Integrity Issue | **High** | High | Software Engineer |
| SC-06: FP-001 temporal divergence test; 3-Ship deferral | Deferred Complexity Debt | **Medium** | High | Senior Project Manager |
| SC-07: Alias cycle detection absent; append-only precludes remediation | Architectural Risk | **High** | High (pre-cycle) / Low (post-cycle) | Software Architect |
| SC-08: Drift gate validates byte-identity only, not behavioral conformance | Constraint Violation | **Medium** | High | Software Architect |
| SC-09: Pull returns events outside actor's event-time scope | Domain Misalignment | **High** | Moderate | Data/Consistency Specialist |
| SC-10: ADR-004 §S13 HTTP enforcement test-only, not runtime | Constraint Violation | **Medium** | High | Software Engineer |
| C1-02: Scope asymmetry not promoted to FP (Ship-1) | Slice Integrity Issue | **Medium** | High | Data/Consistency Specialist |
| C1-03: ConflictDetector v1-only with no documentation of intent | Hidden Coupling | **Critical** | High | Software Engineer |
| C1-04: Test topology too degenerate for real deployment validation | Architectural Risk | **Medium** | High | Product/Domain Reality |
| C2-01: SubjectAliasProjector eager closure; no scalability assessment | Deferred Complexity Debt | **Medium** | Moderate | Data/Consistency Specialist |
| C2-02: FP-006 corrective split attribution undecided | Domain Misalignment | **Medium** | Medium | Product/Domain Reality |
| C2-03: Alias cycle constructible offline; no merge guard | Architectural Risk | **High** | High | Software Architect |
| C2-04: Drift gate byte-identity check; semantic drift undetected | Constraint Violation | **Medium** | High | Data/Consistency Specialist |
| C2-05: No test verifies `subjects_unmerged` rejection | Slice Integrity Issue | **Low** | High | Software Engineer |
| C3-01: v2 identity conflict detection gap folded into FP-012 instead of own FP | Hidden Coupling | **Critical** | High | Software Engineer |
| C3-02: FP-012 carries 5–6 distinct concerns; none individually gated | Deferred Complexity Debt | **High** | High | Senior Project Manager |
| C3-03: `field_count_budget` false STABLE claim | ADR Drift | **High** | High | Software Engineer |
| C3-04: Config deprecation wire format undefined; devices never told v1 deprecated | Domain Misalignment | **High** | Moderate | Product/Domain Reality |
| C3-05: ShapePayloadValidator dual filename convention undocumented | Slice Integrity Issue | **Low** | High | Software Engineer |
| C3-06: FP-001 third consecutive deferral without trigger evaluation | Deferred Complexity Debt | **Medium** | High | Senior Project Manager |

---

## Severity Summary

| Severity | Count | Concerns |
|---|---|---|
| Critical | 3 | SC-01, C1-03, C3-01 |
| High | 11 | SC-02, SC-03, SC-04, SC-05, SC-07, SC-09, C2-03, C3-02, C3-03, C3-04, SC-06 (marginal) |
| Medium | 8 | SC-06, SC-08, SC-10, C1-02, C1-04, C2-01, C2-02, C3-06 |
| Low | 2 | C2-05, C3-05 |

---

## Reversibility Summary

| Zone | Concerns | Notes |
|---|---|---|
| **Irreversible** (append-only) | SC-07 / C2-03 after a cycle is stored | Prevention is the only option; post-cycle the event stream cannot be corrected |
| **Hard to reverse** | SC-04 / SC-09 pull path change | Affects all sync clients; requires client compatibility handling |
| **Medium** | SC-03 (ADR-005), C2-02 (split attribution), C3-04 (wire format) | Schema or protocol changes needed but no stored data affected |
| **Easy** | All others | Code additions, test additions, text corrections, FP operations |

---

## Pre-Ship-4 Gate (from recovery-plan.md §8)

The following Critical and High items must be resolved before Ship-4 begins:

| # | Item | Type | Action |
|---|---|---|---|
| 1 | SC-01 / C1-03 / C3-01 | Critical | Fix ConflictDetector; test v2 detection |
| 2 | SC-05 / C3-03 | ADR Drift | Correct ledger; open FP |
| 3 | SC-06 / C3-06 | Deferred Debt | Author temporal divergence test |
| 4 | SC-04 / C1-02 | Domain Misalignment | Open FP for scope eval asymmetry |
| 5 | SC-07 / C2-03 | Architectural Risk | Open FP; add merge cycle guard |
| 6 | SC-02 / C3-02 | Deferred Debt | Decompose FP-012 |
| 7 | SC-03 | Architectural Risk | Walk ADR-005 §S positions |
