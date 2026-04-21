# Decision Exploration

Working documents from the architecture exploration process. Each document captures the reasoning, event storms, stress tests, and coherence audits that led to an [ADR](../adrs/).

**These are process artifacts, not decisions.** The ADRs are the authoritative record of what was decided. Exploration documents show *how* and *why* — the alternatives considered, the scenarios walked through, the attacks survived.

---

## Reading Guides

Curated navigation into the raw explorations. Start here — each guide tells you what sections contain unique value not captured in the ADRs, so you can jump directly to the reasoning you need.

| Guide | ADR | Raw docs covered |
|-------|-----|-----------------|
| [guide-adr-001.md](guide-adr-001.md) | [ADR-001: Offline Data Model](../adrs/adr-001-offline-data-model.md) | 01–04 (prior art, storage primitives, forward projection, scoping audit) |
| [guide-adr-002.md](guide-adr-002.md) | [ADR-002: Identity & Conflict](../adrs/adr-002-identity-conflict.md) | 05, 07, 09 (event storm, 19 stress test findings, classification) |
| [guide-adr-003.md](guide-adr-003.md) | [ADR-003: Authorization & Sync](../adrs/adr-003-authorization-sync.md) | 10–12 (policy scenarios, 20 findings, irreversibility correction) |
| [guide-adr-004.md](guide-adr-004.md) | [ADR-004: Configuration Boundary](../adrs/adr-004-configuration-boundary.md) | 13–18 (prior art, 5 walkthroughs, coherence, attacks, Q resolution) |
| [guide-adr-005.md](guide-adr-005.md) | [ADR-005: State Progression](../adrs/adr-005-state-progression.md) | 19–21 (event storm, stress test, coherence audit) |

---

## Consolidation Documents (Active)

These bridge the gap between ADRs and the [Architecture Description](../architecture/). They remain active reference documents.

| Document | What it covers |
|----------|----------------|
| [22-platform-primitives-inventory.md](22-platform-primitives-inventory.md) | Structural primitive catalog, walkthrough reconciliation |
| [23-consolidation-framework.md](23-consolidation-framework.md) | 5-step methodology for producing the Architecture Description |
| [24-decision-harvest.md](24-decision-harvest.md) | 61 positions harvested (52 explicit + 4 documented strategies + 5 emergent) |
| [25-boundary-mapping.md](25-boundary-mapping.md) | 29 concerns classified (5 spec-grade, 15 impl-grade, 8 outside, 1 resolved) |
| [26-contract-extraction.md](26-contract-extraction.md) | 21 inter-primitive contracts (15 decided, 6 spec-grade) |
| [27-gap-identification.md](27-gap-identification.md) | Zero gaps across all 4 sub-steps — ready for Step 5 |
| [28-pattern-inventory-walkthrough.md](28-pattern-inventory-walkthrough.md) | 4 patterns formally specified; S00 no-pattern validated |

---

## Archive

Raw exploration documents (docs 00–21, ~14,500 lines total) have been moved to [archive/](archive/). Each carries a `⚠️ SUPERSEDED` banner pointing to its reading guide.

**When to access the archive:**
- When a reading guide points you to a specific section for unique reasoning
- When you need the full step-by-step proof sequence behind a specific ADR constraint
- When referred explicitly by another document or by the user

**When NOT to access the archive:**
- For understanding what was decided → use [ADRs](../adrs/)
- For understanding the architecture → use [architecture/](../architecture/)
- For implementation details → use [implementation/](../implementation/) and [decisions/](../decisions/)

| Archive doc | Cluster | Lines |
|-------------|---------|------:|
| [00-exploration-framework.md](archive/00-exploration-framework.md) | Methodology | 214 |
| [01-architecture-landscape.md](archive/01-architecture-landscape.md) | ADR-001 | 368 |
| [02-adr1-offline-data-model.md](archive/02-adr1-offline-data-model.md) | ADR-001 | 432 |
| [03-adr1-forward-projection.md](archive/03-adr1-forward-projection.md) | ADR-001 | 278 |
| [04-decision-audit.md](archive/04-decision-audit.md) | ADR-001 | 360 |
| [05-adr2-event-storm-identity.md](archive/05-adr2-event-storm-identity.md) | ADR-002 | 643 |
| [07-adr2-phase2-stress-test-results.md](archive/07-adr2-phase2-stress-test-results.md) | ADR-002 | 824 |
| [09-adr2-phase3-classification-results.md](archive/09-adr2-phase3-classification-results.md) | ADR-002 | 528 |
| [10-adr3-phase1-policy-scenarios.md](archive/10-adr3-phase1-policy-scenarios.md) | ADR-003 | 684 |
| [11-adr3-phase2-stress-test.md](archive/11-adr3-phase2-stress-test.md) | ADR-003 | 822 |
| [12-adr3-course-correction.md](archive/12-adr3-course-correction.md) | ADR-003 | 244 |
| [13-adr4-session1-scoping.md](archive/13-adr4-session1-scoping.md) | ADR-004 | 561 |
| [14-adr4-session2-scenario-walkthrough.md](archive/14-adr4-session2-scenario-walkthrough.md) | ADR-004 | 1157 |
| [15-adr4-session3-part1-structural-coherence.md](archive/15-adr4-session3-part1-structural-coherence.md) | ADR-004 | 923 |
| [16-adr4-session3-part2-irreversibility-filter.md](archive/16-adr4-session3-part2-irreversibility-filter.md) | ADR-004 | 469 |
| [17-adr4-session3-part3-adversarial-stress-tests.md](archive/17-adr4-session3-part3-adversarial-stress-tests.md) | ADR-004 | 505 |
| [18-adr4-session3-part4-remaining-q-resolution.md](archive/18-adr4-session3-part4-remaining-q-resolution.md) | ADR-004 | 594 |
| [19-adr5-session1-scoping.md](archive/19-adr5-session1-scoping.md) | ADR-005 | 698 |
| [20-adr5-session2-stress-test.md](archive/20-adr5-session2-stress-test.md) | ADR-005 | 559 |
| [21-adr5-session3-part1-structural-coherence.md](archive/21-adr5-session3-part1-structural-coherence.md) | ADR-005 | 638 |
