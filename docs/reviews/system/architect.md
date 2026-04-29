# Software Architect — Assigned Concerns

## SC-03 — ADR-005 completely unvalidated in production

**Issue**: ADR-005 defines state progression, pattern registry, source-chain traversal, and 5 composition rules. Zero of these are exercised by any code in Ships 1–3. The first production exercise is Ship-4 — meaning all ADR-005 assumptions will surface as unknowns simultaneously.

**Risk if ignored**: Ship-4 will discover ADR-005 gaps under implementation pressure with no prior validation. Corrections under pressure produce structural drift. The composition rules in particular (cross-activity linking via `activity_ref`, subject-level vs event-level patterns) have never been stress-tested.

**Action**:
- *Now*: Before Ship-4 spec lock, walk through at least one ADR-005 §S position against the existing event store and pull contract. Identify any §S position that cannot be satisfied by the current schema.
- *Later*: Author Ship-4 sub-decisions for each ADR-005 §S position that is first exercised in Ship-4.

**Reversibility**: Moderate. ADR-005 positions that conflict with the existing schema require either schema migration (hard) or ADR amendment (medium); both are easier before code is written than after.

---

## SC-07 — Alias cycle detection absent; append-only precludes remediation

**Issue**: `SubjectAliasProjector` traverses the alias graph without cycle detection. The append-only invariant means a stored cyclic merge event cannot be corrected — only papered over with a split, which creates a different structural problem.

**Risk if ignored**: A cycle is constructible offline (two devices each merge A→B from different perspectives without visibility into each other's merge). First occurrence causes `SubjectAliasProjector` to hang or overflow. No correction path exists post-storage.

**Action**:
- *Now*: Add cycle detection (visited-set) to `SubjectAliasProjector`. Add a merge guard that validates the proposed merge does not create a cycle by checking the existing alias graph before accepting the `subjects_merged/v1` event.
- *Later*: Document the cycle-prevention contract in `subjects_merged/v1` shape definition.

**Reversibility**: Low for stored events. High for code — cycle detection can be added without schema changes.

---

## SC-08 — Drift gate checks byte-identity, not behavioral conformance

**Issue**: `check-convergence.sh` validates that shape files have not changed by comparing hashes. A behavioral regression (field renamed, constraint weakened, optional field made required) that does not change the file passes the gate.

**Risk if ignored**: Behavioral drift in shape schemas goes undetected. The gate's PASS status is used as a convergence health signal; if the signal is misleading, architectural drift accumulates silently.

**Action**:
- *Now*: Document the gate's scope explicitly: "byte-identity only; behavioral conformance not covered." Add an FP for behavioral schema regression coverage.
- *Later*: Consider schema contract tests (validate that existing fixture events still pass against the current schema) as a complement to the byte-identity check.

**Reversibility**: High. Gate change is additive; existing check is not removed.

---

## C2-03 — Alias cycle constructible offline; no prevention in merge guard

*(Cross-reference: SC-07 — see above for full treatment.)*

**Issue**: The merge guard in the push path validates for duplicate events but not for graph cycles. Since field devices merge offline, two devices can each merge in a way that creates a cycle before either syncs.

**Risk if ignored**: Silent infinite loop in `SubjectAliasProjector` on first cycle encounter.

**Action**: Merge guard must query the current alias graph before accepting a merge event and reject if the merge would create a cycle. This is a server-side guard (push validation step), not a shape-level constraint.

**Reversibility**: High. Server-side guard is additive; shape is not changed.
