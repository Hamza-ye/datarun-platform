# Ship-2 — Project Shepherd Review

## Intended Slice

- S06 entity registry lifecycle: merge (two subjects → one with alias), split (one subject → two)
- S19 offline constraint maintained: merges are coordinator-level (reliable connectivity); split preserves history
- FP-002 resolution: `subject_lifecycle` discipline confirmed (projection cache or drop; no authoritative read path)
- FP-007: drift gate (`scripts/check-convergence.sh`) built and passing
- Alias projection: SubjectAliasProjector, eager transitive closure, admin UI renders merged subjects correctly

## Actual Delivered Slice

- `subjects_merged/v1` and `subject_split/v1` shapes implemented and validated via new walkthroughs
- `SubjectAliasProjector` added: eager transitive closure on read; no projection table (FP-002 option a confirmed)
- Drift gate (`check-convergence.sh`) built; 4 checks including shape-tree byte-identity; PASS at Ship-2 close
- FP-002 RESOLVED: `subject_lifecycle` determined to be derived-only (no cache), not an authoritative store
- FP-007 RESOLVED: drift gate built and confirmed working
- FP-006 OPENED: S7↔S8 attribution seam under corrective split — who authors the split event when correcting a merge error?

## Alignment Check

| Item | Result | Note |
|---|---|---|
| S06 merge delivered | ✔ | Alias events, transitive closure, no rewrite |
| S06 split delivered | ✔ | History frozen at split point |
| Append-only invariant | ✔ | No merge rewrite; `SubjectsUnmerged` explicitly rejected (ADR-002) |
| FP-002 resolution | ✔ | Option a chosen: no cache; projection on read |
| FP-007 resolution | ✔ | Drift gate built, 4 checks, PASS |
| FP-006 opened correctly | ✔ | Correctly deferred; S7↔S8 seam not exercisable in Ship-2 alone |
| FP-001 (temporal divergence) | ✖ | Not in scope for Ship-2 but not re-evaluated for closure; carried silently |
| Scope asymmetry (C1-02) | ✖ | Not promoted; no FP; not revisited |

## Drift Signals

- `SubjectAliasProjector` performs eager transitive closure on every request. At Ship-2 topology (few subjects), this is invisible. At scale (S06 + S08 + S09 = many merges over time), this becomes a read-time cost that grows unboundedly with no cache and no eviction strategy. The retro does not assess the scalability of this choice.
- FP-001 deferred again (second deferral). The pattern of "not in scope, carry forward" is established.

## Missing Elements

- Scalability assessment for eager transitive closure in `SubjectAliasProjector`
- Re-evaluation of whether scope asymmetry (C1-02) creates a problem for the merge/split pull path
- FP-001 gate part 2 still outstanding; no progress noted

## Verdict

**Aligned**

S06 lifecycle delivered cleanly. FP-002 and FP-007 closed with evidence. FP-006 correctly opened. No structural drift. Two carried items (FP-001, scope asymmetry) are not Ship-2 concerns but their recurrence without closure is a rhythm signal.
