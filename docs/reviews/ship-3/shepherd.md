# Ship-3 — Project Shepherd Review

## Intended Slice

- S06b shape evolution: additive evolution (household_observation/v1 → v2, new optional fields) and deprecation (v1 deprecated, v2 current)
- Multi-version load: server must store events under old and new shape_refs; projections must render both
- FP-009 closure: verify ConflictDetector is unchanged across shape evolution (v1-pinned remains correct for current detection scope)
- S19 offline constraint maintained: field devices may submit v1 events after v2 deprecation; server accepts and stores verbatim

## Actual Delivered Slice

- `household_observation/v2` shape added (additive: new optional fields)
- `ShapePayloadValidator` updated to load both v1 and v2 schemas (filename convention handling)
- Multi-version projection: per-request `shape_ref` routing, no cache (ADR-001 §S2 / ADR-004 §S10)
- Four new walkthroughs: W-6 (v2 happy path), W-7 (v1 deprecated, verbatim storage), W-8 (unknown shape_ref rejection), W-10 (backward-compat mixed-version admin render)
- FP-009 RESOLVED: ConflictDetector unchanged; v1-pinned direction asserted
- FP-010 OPEN: cross-version projection composition contract unspecified
- FP-011 OPEN: `household_observation` directory classification re-deferred (folds into FP-012)
- FP-012 OPEN: deployer-authoring surface growing (ADR-004 §S6 atomicity unbuilt, §S13 HTTP enforcement test-only, §S14 policies never built)
- FP-013 OPEN: config-package wire-versioning unspecified

## Alignment Check

| Item | Result | Note |
|---|---|---|
| S06b additive evolution | ✔ | v2 added, v1 coexists, verbatim storage confirmed |
| Multi-version projection | ✔ | Per-request routing, no cache |
| Accept v1 after deprecation | ✔ | W-7 confirms; offline constraint preserved |
| FP-009 resolution | ✔ | ConflictDetector unchanged; v1-current direction explicit |
| v2-current asymmetry named | ✔ | Retro documents that v2 captures bypass identity conflict detection |
| v2-current asymmetry formalized | ✖ | Folded into FP-012 gate (b)/(c); should be its own FP |
| FP-012 scope controlled | ✖ | FP-012 now carries 4+ sub-obligations across 3 Ships; no milestone boundary |
| ADR-004 §S6 atomicity built | ✖ | "Simulated not implemented" since Ship-1; still unbuilt at Ship-3 |
| FP-001 temporal divergence | ✖ | Third Ship without closure; no action taken |

## Drift Signals

- **v2-current asymmetry folded into FP-012 instead of opened as its own FP.** This is a behavioral gap (identity conflict detection silently skips non-v1 events) that is observable in production today. Folding it into a larger FP that requires a deployer-authoring surface to be built before it is addressed is inappropriate triage.
- **FP-012 has no milestone boundary.** It now carries: ADR-004 §S6 atomicity, §S13 HTTP enforcement, §S14 deployer policies, FP-009 deeper surface, FP-011 directory split, FP-013 wire versioning, and the v2-current asymmetry. This is a "Ship of Reckoning" masquerading as a flagged position.
- **Third consecutive Ship without FP-001 gate part 2.** The temporal divergence test is 3 Ships old and has no forcing function.

## Missing Elements

- Dedicated FP for v2-current identity conflict detection gap
- FP-012 milestone boundary (what must be built before it can close?)
- ConflictDetector test for multi-version detection behavior

## Verdict

**Aligned with drift signal**

S06b delivered correctly. FP-009 closed with evidence. Multi-version projection works. Drift is in triage quality: v2-current asymmetry is a production behavioral gap folded into a growing mega-FP, and FP-012's accreting scope with no milestone is a process failure, not a technical one.
