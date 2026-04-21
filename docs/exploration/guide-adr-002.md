# ADR-002 Exploration Guide: Identity & Conflict Resolution

> **For what was decided**: read [ADR-002](../adrs/adr-002-identity-conflict.md) — 14 sub-decisions (S1–S14).
> **For structural implications**: read [architecture/primitives.md](../architecture/primitives.md) (Identity Resolver, Conflict Detector).
> **Below**: how we got there — section-by-section navigation into the raw explorations.

---

## Source Documents

| Doc | Lines | What it covers |
|-----|------:|----------------|
| [05](archive/05-adr2-event-storm-identity.md) | 643 | Event storm across 5 scenarios, identity taxonomy, conflict taxonomy |
| [07](archive/07-adr2-phase2-stress-test-results.md) | 824 | 19 findings across 3 mechanisms, 3 combination scenarios, invariant survival |
| [09](archive/09-adr2-phase3-classification-results.md) | 528 | Structural/strategy/policy classification, ADR-2 decision skeleton |

---

## doc 05: Event Storm — Identity & Conflict

Five scenarios storm-tested to discover identity patterns:

- **§S01** (Acts 1-5): Entity-linked capture — subject registration, duplicate creation, merge, ambiguous identity. Produces the identity map with 4 discovery points.
- **§S06** (Acts 1-5): Registry lifecycle — attribute change, deactivation, split, schema evolution intersecting identity, bulk reclassification.
- **§S19** (Acts 1-5): Offline capture — concurrent observations, concurrent state changes, offline identity collision, stale state sync, conflict detection+resolution. **This is the hardest scenario.** Produces the causal ordering discovery (device_sequence + sync_watermark).
- **§S03** (Acts 1-4): User-based assignment — assignment creation, actor/subject identity intersection, assignment transfer, actor identity ambiguity.
- **§S07** (Acts 1-5): Resource distribution — shipment identity, dispatch/receipt confirmation, multi-level handoff, offline handoff.

Key synthesis sections:
- **§Identity Taxonomy**: The 4 types emerged — subject, actor, resource, event. Why exactly 4, not 3 or 5.
- **§Conflict Type Taxonomy**: Data conflicts, identity conflicts, workflow conflicts, scope conflicts — each with different detection and resolution needs.
- **§Aggregate Map Update**: How aggregates evolved after the storm — Event, Subject, Assignment, Shipment.
- **§Questions for ADR-2**: 13 open questions handed off to Phase 2 (stress test).

**Unique value not in ADR**: The detailed event storm acts — the step-by-step sequences showing how identity breaks emerge. The aggregate discovery process. The 13 questions that drove the stress test design.

---

## doc 07: Stress Test — 19 Findings Across 3 Mechanisms

Three mechanisms tested independently, then in combination:

### Mechanism A: Accept-and-Flag (findings A1–A6)
- **A1**: Flag created offline, syncs late — proves flags are server-side events, not device artifacts
- **A2**: Conflicting resolutions — proves single-writer per conflict (became [2-S11])
- **A3**: 50-flag backlog — proves batch resolution UX is needed, flags carry root-cause metadata
- **A4**: Subject merged between flag creation and review — proves flags need identity-change annotation
- **A5**: **Critical finding** — flagged event triggered downstream work before flag noticed. Proves conflict detection MUST run before policies fire (became [2-S12])
- **A6**: Unbounded flag growth — proves configurable auto-resolution for low-severity flags

### Mechanism B: Alias Table in Projection (findings B1–B6)
- **B1**: Full merge lifecycle while device offline — proves device-side projection handles aliases via sync
- **B2**: Events under retired_id after merge but before sync — proves events always accepted, stale_reference detection on sync
- **B3**: Transitive merges (A→B→C) — proves eager transitive closure needed (became idr-009 DD-3)
- **B4**: Split-then-merge acyclicity — proves archived-is-terminal invariant prevents cycles
- **B5**: Projection rebuild cost after merge (200 subjects) — proves incremental rebuild feasible
- **B6**: Unmerge — proves SubjectsUnmerged is unnecessary; corrective split handles wrong merges

### Mechanism C: Device-Sequence + Sync-Watermark (findings C1–C5)
- **C1**: Two events same watermark — proves concurrent-state-change requires human resolution (became idr-007 DD-1)
- **C2**: 200 events batch conflict detection — proves computational feasibility at scale
- **C3**: Device clock reset — proves device_time is advisory, device_sequence is structural
- **C4**: Phone sharing (same actor, two devices) — proves device_id is hardware-bound, not user-bound
- **C5**: S19 Act 2 conflict detection — proves the deactivation-then-observation scenario is correctly caught

### Combination Scenarios (Alpha, Beta, Gamma)
- **Alpha**: Assignment transfer during offline merge — compound conflict (identity + assignment)
- **Beta**: Split + concurrent offline observations — tests post-split event attribution
- **Gamma**: Shipment receipt offline with identity collision — tests resource identity + pending match

**Unique value not in ADR**: The 19 individual attack scenarios with step-by-step walkthroughs. The ADR has the resulting constraints; the stress test has the *proof sequences* showing each constraint is necessary. Critical for anyone auditing "why is [2-S12] a structural constraint?"

---

## doc 09: Classification — Structural vs. Strategy vs. Deferred

Every Phase 2 finding classified into one of four buckets:

- **§Bucket 1 — ADR-2 Constraints** (14 items): What's permanently locked — envelope fields, merge semantics, acyclicity, single-writer resolution, detection-before-policies. Each with the boundary test answer.
- **§Bucket 2 — ADR-2 Strategies** (12 items): Server-side logic protecting structural invariants — flag creation location, batch resolution metadata, incremental projection rebuild, cascading resolution approach. Can evolve without data migration.
- **§Bucket 3 — Deferred** (4 items): Sent to ADR-4/5 — downstream work invalidation, pending match patterns, conflict definition boundary.
- **§Bucket 4 — Accepted Risks** (5 items): Acknowledged and documented — concurrent state changes always need humans, batch detection is feasible, cross-device ordering is best-effort.
- **§Simplicity Validation**: S00 walked through the full constraint set to prove the simple case stays simple.
- **§ADR-2 Decision Skeleton**: The 14 sub-decisions (S1–S14) in draft form before ADR writing.

**Unique value not in ADR**: The individual classification reasoning for each finding — why B3 is a constraint but A1 is a strategy. The "close call" boundary judgments. The S00 simplicity walkthrough proving the constraints don't over-complicate the basic case.
