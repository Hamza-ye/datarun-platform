# Cross-Ship Reality Sweep — System Risk

## Cross-Ship Breakpoints

**XBP-1: ConflictDetector version pinning is a persistent, unaddressed structural gap.**
Introduced at Ship-1 (v1 only; only shape in existence). Ship-2: still correct (v1 only). Ship-3: v2 added as current shape; v1 deprecated; ConflictDetector still v1-only. The gap crossed from "not yet relevant" to "active production gap" at Ship-3 without any code change or gate activation.

**XBP-2: ADR-005 has never been exercised by any Ship-1 through Ship-3 code.**
ADR-005 defines state progression: projection-derived state machines, pattern registry, source-chain traversal, auto-resolution classification, composition rules. Not one of these §S positions is exercised in the implemented code at Ship-3 close. The charter claims ADR-005 is DECIDED and all its §S positions are STABLE concepts. In practice, ADR-005 is a specification with zero production coverage.

**XBP-3: The scope eval asymmetry (push=event-time, pull=request-time) has been carried across 3 Ships as an observation, not a risk.**
This is a domain-level correctness concern. The Constraints doc and Access Control scenario both require that access is contextual and auditable. A pull that uses request-time scope rather than event-time scope means historical events may be returned or withheld based on the actor's *current* scope, not their *then-current* scope. For audits, corrections, and case management — all in Phase 1 — this is a material gap.

**XBP-4: The deployer-authoring surface (ADR-004 §S6/§S13/§S14) has never been built across 3 Ships.**
ADR-004 was decided before Ship-1. Three Ships have passed with the deployer surface unbuilt. FP-012 was meant to gate this; instead it has grown to carry 6+ sub-obligations. The ADR's §S13 and §S14 positions are exercised only in unit tests, not in runtime code.

## Repeated Assumptions

- "This deferred item doesn't block this Ship" — true for each Ship individually, but the accumulation is not evaluated across Ships.
- "Ledger row added as STABLE" — used as a ship-close ritual even when the mechanism is partially or fully unimplemented.
- "FP trigger will be evaluated before it matters" — not evaluated proactively; waits for a Ship to be impacted.

## Deferred Complexity Chains

**Chain 1: Detection completeness**
`ConflictDetector` v1-only → v2 lands (Ship-3) → detection gap is live → gap folded into FP-012 → FP-012 requires deployer surface → deployer surface not scheduled → detection gap has no forcing function.

**Chain 2: State machine introduction (ADR-005)**
ADR-005 decided → Ship-1/2/3: no state machines built → Ship-4 is first exercise → ADR-005 §S positions are entirely unvalidated against production code → first time they are exercised, all gaps surface at once.

**Chain 3: Deployer surface debt**
ADR-004 §S6 atomicity "simulated" (Ship-1) → never built → FP-012 opened → FP-012 accumulates sub-obligations across Ships 2, 3 → no milestone → any Ship that requires deployer-authored shapes must either work around missing enforcement or build it ad-hoc.

## ADR Misapplication Signals

- **ADR-004 §S13**: Described as "HTTP enforcement test-only" — the ADR specifies runtime enforcement; tests are not a substitute.
- **ADR-001 §S2 (state as projection)**: `subject_lifecycle` table exists in migrations; the table is empty by choice (FP-002 option a), but the schema allows it to be populated. The discipline is behavioral, not structural.
- **Concept ledger rule 3**: STABLE requires "implementation correct." `field_count_budget` marked STABLE at Ship-3 close without an implementation of its enforcement mechanism.

## High Irreversibility Zones

**Zone 1: Event stream shape pinning.**
`ConflictDetector` reads `shape_ref` from stored events. If a future Ship changes shape naming conventions, existing stored events will no longer match the detector's patterns. The event store is append-only (ADR-001 §S1 INVARIANT); no remediation path exists other than re-deploying a new detector that handles both old and new naming.

**Zone 2: Alias graph.**
Once a merge event is stored, the alias is permanent (ADR-001 §S1). A cyclic merge, if admitted, produces a permanent cycle in the alias graph with no correction path. The only remediation is a `subject_split/v1` event — but split after a cycle creates two non-aliased subjects that may not reflect any real-world correction.

**Zone 3: `actor_ref` system format.**
`actor_ref = "system:{source_type}/{source_id}"` is an established contract (ADR-008 §S2). Any future server-side emitter that uses a different format will produce events that integrity logic cannot attribute. Old events are immutable; the format cannot be retrofitted.
