# Senior Project Manager — Assigned Concerns

## SC-02 — FP-012 mega-scope with no milestone boundary

**Issue**: FP-012 carries ADR-004 §S6 atomicity, §S13 HTTP enforcement, §S14 deployer policies, FP-009 deeper surface, FP-011 directory classification, FP-013 wire versioning, and the v2-current detection gap. Each is a distinct concern requiring separate design, code, and test. No milestone boundary exists; no individual sub-obligation has a forcing function.

**Risk if ignored**: FP-012 becomes a permanent "background FP" — present at every Ship start, evaluated as "not this Ship," and never closed. Ships that depend on deployer-authored shapes (S09, S10-related) will either block on it or work around it ad-hoc.

**Action**:
- *Now*: Decompose FP-012 into individual FPs. Each new FP gets: a single concrete gate, a trigger condition, and a blocking designation. Close FP-012 as superseded.
- *Later*: Assign each decomposed FP to the earliest Ship that would benefit from closure.

**Reversibility**: High. FP decomposition does not change any code.

---

## SC-06 — FP-001 temporal divergence test deferred 3 Ships; trigger never evaluated

**Issue**: FP-001 gate part 2 (test that proves ScopeResolver cannot pass under a cache-based implementation) has been deferred at every Ship close since Ship-1 with the rationale "not in scope." No Ship has proactively checked whether it *is* the Ship that first depends on role-action enforcement.

**Risk if ignored**: When role-action enforcement lands, the unverified claim becomes load-bearing. Retrofitting the test at that point requires understanding the then-current scope resolver, which may have been extended. Testing debt at Ship-5+ is more expensive than testing debt at Ship-3.

**Action**:
- *Now*: Write the temporal divergence integration test. It is a bounded task (one test, ~2 hours). There is no valid "not this Ship" justification once a Ship exists where the test can be authored.
- *Later*: Add to pre-Ship checklist: "evaluate all OPEN FPs with trigger conditions against this Ship's scope."

**Reversibility**: High. Test addition only.

---

## C1-01 — FP-001 gate part 2 unauthored

*(Cross-reference: SC-06 — see above for full treatment.)*

**Issue**: The specific gate test — push event A (role X) → admin changes role to Y → push event B (pre-change watermark) → assert role_stale fires on B only — has never been authored. The retro explicitly marks it "not yet authored."

**Action**: Author the test in the next Ship. It does not require any new scenario coverage. Assign to engineering.

**Reversibility**: High.

---

## C3-06 — FP-001 third deferral; deferred FPs need proactive trigger evaluation

*(Cross-reference: SC-06 — see above.)*

**Issue**: The structural problem is not the test itself but the absence of a proactive trigger-evaluation step in the Ship-start process. FPs with conditional triggers are perpetually carried forward without anyone asking "does this Ship trigger this FP?"

**Action**:
- *Now*: Add to Ship-start checklist: for every OPEN FP with a conditional trigger, evaluate whether the current Ship scope meets the trigger condition.
- *Later*: Update `docs/flagged-positions.md` format to include a `Last evaluated:` field.

**Reversibility**: High. Process change only.
