# Data / Consistency Specialist — Assigned Concerns

## SC-04 — Scope evaluation asymmetry is a domain consistency gap

**Issue**: Push events are validated against the actor's scope at event time (replayed from assignment timeline). Pull responses are filtered against the actor's scope at request time. These two evaluation points use different temporal anchors for the same logical concept (actor scope). The Access Control scenario explicitly requires that authority is auditable and contextual — contextual means at-the-time, not at-the-request.

**Risk if ignored**: Historical event access (needed for corrections S04, case management S08, audits) will silently omit or include events based on the actor's *current* scope rather than the scope under which the work was performed. A CHV who was reassigned will have their historical records become inaccessible to them on pull. An auditor reconstructing a case will see a different event set depending on when they pull.

**Action**:
- *Now*: Open a Flagged Position for scope evaluation asymmetry with gate: "pull path must filter events using the actor's scope at event.timestamp, consistent with the push validation path."
- *Later*: Implement pull-time event-scope filtering using the same `ScopeResolver.activeGeographicScopes(actorId, at=event.timestamp)` call that push uses.

**Reversibility**: Medium. Pull path change affects all sync clients; requires client compatibility testing.

---

## SC-09 — Pull returns events outside actor's event-time scope

*(Cross-reference: SC-04 — same root cause.)*

**Issue**: An actor whose assignment changed since they last pulled will receive (or be denied) events based on their current scope. For S08 (case management, long-running), this produces a materially incorrect view — a CHV who picks up a case mid-stream would see only events captured while they were the assigned CHV, not the full case history.

**Action**: Same as SC-04. Scope the fix to the pull filter in `SyncController`.

**Reversibility**: Medium.

---

## C1-02 — Scope asymmetry not promoted to Flagged Position

**Issue**: Ship-1 retro §3.3 documents the asymmetry as an observation. No FP was opened. No gate was assigned. The observation has been carried silently for 3 Ships.

**Action**:
- *Now*: Promote to Flagged Position immediately (see SC-04 action).
- *Later*: Add to retro checklist: "any observation about behavioral asymmetry must be promoted to an FP or explicitly dismissed with a reason."

**Reversibility**: High. FP creation is additive.

---

## C2-01 — `SubjectAliasProjector` eager closure has no scalability assessment

**Issue**: `SubjectAliasProjector` performs a full transitive closure traversal on every read that returns merged subjects. With no cache and no budget, traversal cost grows with merge depth and merge count over the platform's lifetime. The Constraints doc targets millions of records and large-scale deployments.

**Risk if ignored**: At scale (many merges, long chains), pull requests become slow. The no-cache choice (FP-002 option a) is correct for consistency but was made without a documented performance analysis. At the point where performance becomes problematic, retrofitting a cache requires careful consistency reasoning that should be done before the problem is urgent.

**Action**:
- *Now*: Document the no-cache choice's performance assumptions in `SubjectAliasProjector` Javadoc (expected merge depth, expected call frequency).
- *Later*: Open a Flagged Position for alias traversal scalability with trigger: "before the Ship that introduces bulk pull endpoints or large-scale admin projections."

**Reversibility**: High for documentation. Medium for cache introduction (requires careful consistency contract definition).

---

## C2-04 — Drift gate validates byte-identity, not semantic conformance of shape schemas

*(Cross-reference: SC-08 — architect handles structural response; this covers data impact.)*

**Issue**: A shape schema that silently weakens a constraint (e.g., removes a required field, changes an enum) passes the drift gate but produces inconsistent data: events captured before the change have one schema contract; events captured after have a weaker one. The mixed-version admin render (W-10) tests that old and new events render correctly, but not that old events remain semantically valid under a changed schema.

**Action**:
- *Now*: Add fixture-based schema regression tests: load the existing event fixtures and validate them against the current schema. If they pass, the schema has not broken backward compatibility.
- *Later*: Make fixture-based validation part of the CI gate.

**Reversibility**: High. Test addition.
