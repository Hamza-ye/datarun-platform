# ADR Exploration Cross-Reference

Checkpoint spine (2026-04-10 → 2026-04-14) mapped to ADR-002 through ADR-005 and exploration evidence. ADR-001 is context only; ADR-006+ excluded.

## Methodology evolution (from checkpoints)

| Date | Checkpoint | Exploration method | Decision character |
|------|------------|-------------------|-------------------|
| 2026-04-10 | [`checkpoint-2026-04-10.md`](../../checkpoints/checkpoint-2026-04-10.md) | 3-phase per ADR: event storm → stress test → classification ([`00-exploration-framework.md`](../../exploration/archive/00-exploration-framework.md)) | Infrastructure: identity + ordering on append-only events |
| 2026-04-12 | [`checkpoint-2026-04-12.md`](../../checkpoints/checkpoint-2026-04-12.md) | + **Irreversibility filter** before stress-test depth; ADR-3 course correction | Infrastructure: authority without envelope extension |
| 2026-04-13 | [`checkpoint-2026-04-13.md`](../../checkpoints/checkpoint-2026-04-13.md) | ADR-4 **4-session**: prior art → config-lens walkthroughs → irreversibility/stress → ADR | **Boundary judgment**: platform vs deployer (V2 / T2) |
| 2026-04-13 | [`checkpoint-2026-04-13-b.md`](../../checkpoints/checkpoint-2026-04-13-b.md) | ADR-5 three sessions + structural coherence | **Workflow composition** on closed envelope |
| 2026-04-14 | [`checkpoint-2026-04-14.md`](../../checkpoints/checkpoint-2026-04-14.md) | **Pass 3 consolidation** ([`23-consolidation-framework.md`](../../exploration/23-consolidation-framework.md)): harvest → boundaries → contracts → gaps → architecture description | Translate decisions into named parts and contracts (no new decisions) |

## Master matrix

| Checkpoint | ADR-002 | ADR-003 | ADR-004 | ADR-005 | Envelope fields | Open front #1 | Exploration files (002–005) |
|------------|---------|---------|---------|---------|-----------------|---------------|----------------------------|
| **2026-04-10** | DECIDED (14 S*) | NOT STARTED | NOT STARTED | NOT STARTED | **9** (+5 from ADR-1 base) | Begin ADR-003 exploration | `05`, `07`, `09` |
| **2026-04-12** | DECIDED | DRAFT (10 S*) | NOT STARTED | NOT STARTED | 9 (unchanged) | Configuration boundary (T2) — CRITICAL | + `10`, `11`, `12` |
| **2026-04-13** | DECIDED | DECIDED | DECIDED (14 S*) | NOT STARTED (6 Q queued) | **11** (+`shape_ref`, `activity_ref`) | ADR-005 workflow — CRITICAL | + `13`–`18` |
| **2026-04-13-b** | DECIDED | DECIDED | DECIDED | DECIDED (9 S*) | 11 (unchanged) | Platform spec / named primitives gap | + `19`–`21` |
| **2026-04-14** | DECIDED | DECIDED | DECIDED | DECIDED | 11 (closed) | Implementation planning (consolidation done) | + `22`–`28`, [`architecture/`](../../architecture/) |

\*Sub-decision counts per checkpoint decision boards.

### Envelope evolution (checkpoints + ADRs)

| Stage | Fields added | Cumulative count | Authority |
|-------|--------------|------------------|-----------|
| ADR-001 | `id`, `type`, `payload`, `timestamp` | 4 | [`adr-001-offline-data-model.md`](../../adrs/adr-001-offline-data-model.md) |
| ADR-002 | `subject_ref`, `actor_ref`, `device_id`, `device_seq`, `sync_watermark` | 9 | [`adr-002-identity-conflict.md`](../../adrs/adr-002-identity-conflict.md) |
| ADR-003 | (none — authority-as-projection) | 9 | [`adr-003-authorization-sync.md`](../../adrs/adr-003-authorization-sync.md) |
| ADR-004 | `shape_ref`, `activity_ref` | 11 | [`adr-004-configuration-boundary.md`](../../adrs/adr-004-configuration-boundary.md) |
| ADR-005 | (none — state is projection) | 11 | [`adr-005-state-progression.md`](../../adrs/adr-005-state-progression.md) |

## Per-ADR exploration map

### ADR-002 — Identity & Conflict

| Phase | Archive doc | Guide section | ADR traceability |
|-------|-------------|---------------|------------------|
| 1 Event storm | [`05-adr2-event-storm-identity.md`](../../exploration/archive/05-adr2-event-storm-identity.md) | [guide §doc 05](../../exploration/guide-adr-002.md) | S1–S2 taxonomy |
| 2 Stress test | [`07-adr2-phase2-stress-test-results.md`](../../exploration/archive/07-adr2-phase2-stress-test-results.md) | [guide §doc 07](../../exploration/guide-adr-002.md) | A5→S12, A2→S11, B6→S7 |
| 3 Classification | [`09-adr2-phase3-classification-results.md`](../../exploration/archive/09-adr2-phase3-classification-results.md) | [guide §doc 09](../../exploration/guide-adr-002.md) | Bucket 1→14 constraints |
| ADR | [`adr-002-identity-conflict.md`](../../adrs/adr-002-identity-conflict.md) | — | S1–S14 |

**Checkpoint closed:** 2026-04-10.

### ADR-003 — Authorization & Sync

| Phase | Archive doc | Guide section | ADR traceability |
|-------|-------------|---------------|------------------|
| 1 Policy scenarios | [`10-adr3-phase1-policy-scenarios.md`](../../exploration/archive/10-adr3-phase1-policy-scenarios.md) | [guide §doc 10](../../exploration/guide-adr-003.md) | S1–S2 assignment model |
| 2 Stress test | [`11-adr3-phase2-stress-test.md`](../../exploration/archive/11-adr3-phase2-stress-test.md) | [guide §doc 11](../../exploration/guide-adr-003.md) | 20 findings → filter |
| Correction | [`12-adr3-course-correction.md`](../../exploration/archive/12-adr3-course-correction.md) | [guide §doc 12](../../exploration/guide-adr-003.md) | Zero envelope fields |
| ADR | [`adr-003-authorization-sync.md`](../../adrs/adr-003-authorization-sync.md) | — | S1–S10 |

**Checkpoint drafted:** 2026-04-12. **Promoted DECIDED:** 2026-04-13.

### ADR-004 — Configuration Boundary

| Session | Archive doc | Guide section | ADR traceability |
|---------|-------------|---------------|------------------|
| 1 Scoping + prior art | [`13-adr4-session1-scoping.md`](../../exploration/archive/13-adr4-session1-scoping.md) | [guide §doc 13](../../exploration/guide-adr-004.md) | Four-layer gradient hypothesis |
| 2 Walkthroughs | [`14-adr4-session2-scenario-walkthrough.md`](../../exploration/archive/14-adr4-session2-scenario-walkthrough.md) | [guide §doc 14](../../exploration/guide-adr-004.md) | S00/S06/S09/S12/S20 |
| 3a Coherence | [`15-adr4-session3-part1-structural-coherence.md`](../../exploration/archive/15-adr4-session3-part1-structural-coherence.md) | [guide §doc 15](../../exploration/guide-adr-004.md) | Device vs server eval split |
| 3b Irreversibility | [`16-adr4-session3-part2-irreversibility-filter.md`](../../exploration/archive/16-adr4-session3-part2-irreversibility-filter.md) | [guide §doc 16](../../exploration/guide-adr-004.md) | Only `shape_ref` envelope-irreversible |
| 3c Stress | [`17-adr4-session3-part3-adversarial-stress-tests.md`](../../exploration/archive/17-adr4-session3-part3-adversarial-stress-tests.md) | [guide §doc 17](../../exploration/guide-adr-004.md) | S12 scoping |
| 3d Q resolution | [`18-adr4-session3-part4-remaining-q-resolution.md`](../../exploration/archive/18-adr4-session3-part4-remaining-q-resolution.md) | [guide §doc 18](../../exploration/guide-adr-004.md) | S1–S14 final |
| ADR | [`adr-004-configuration-boundary.md`](../../adrs/adr-004-configuration-boundary.md) | — | S1–S14 |

**Checkpoint closed:** 2026-04-13.

### ADR-005 — State Progression & Workflow

| Session | Archive doc | Guide section | ADR traceability |
|---------|-------------|---------------|------------------|
| 1 Scoping + storm | [`19-adr5-session1-scoping.md`](../../exploration/archive/19-adr5-session1-scoping.md) | [guide §doc 19](../../exploration/guide-adr-005.md) | Pattern Registry emergence |
| 2 Stress test | [`20-adr5-session2-stress-test.md`](../../exploration/archive/20-adr5-session2-stress-test.md) | [guide §doc 20](../../exploration/guide-adr-005.md) | Reject 7th type; composition rules |
| 3 Coherence | [`21-adr5-session3-part1-structural-coherence.md`](../../exploration/archive/21-adr5-session3-part1-structural-coherence.md) | [guide §doc 21](../../exploration/guide-adr-005.md) | Zero envelope; 11 primitives map |
| ADR | [`adr-005-state-progression.md`](../../adrs/adr-005-state-progression.md) | — | S1–S9 |
| Post (inventory) | [`28-pattern-inventory-walkthrough.md`](../../exploration/28-pattern-inventory-walkthrough.md) | — | Partial thickening of S5 inventory |

**Checkpoint closed:** 2026-04-13-b.

## Consolidation outputs (2026-04-14)

| Step | Document | Feeds Reach Brief |
|------|----------|-------------------|
| Primitives inventory | [`22-platform-primitives-inventory.md`](../../exploration/22-platform-primitives-inventory.md) | Layer 1 component list |
| Decision harvest | [`24-decision-harvest.md`](../../exploration/24-decision-harvest.md) | Sub-decision → component binding |
| Boundary mapping | [`25-boundary-mapping.md`](../../exploration/25-boundary-mapping.md) | Aligns with `07-system-boundary-map` |
| Contract extraction | [`26-contract-extraction.md`](../../exploration/26-contract-extraction.md) | Layer 1 interaction sketch |
| Gap identification | [`27-gap-identification.md`](../../exploration/27-gap-identification.md) | Thickness register inputs |

## Landscape (pre-ADR sequence)

| Doc | Role in strategy |
|-----|------------------|
| [`01-architecture-landscape.md`](../../exploration/archive/01-architecture-landscape.md) | Constraint filter, prior art, viable families — **superseded as authority**, retained for north-star lineage |
| [`04-decision-audit.md`](../../exploration/archive/04-decision-audit.md) | Pass 1→2 gate; ADR scope normalization |

## Quick navigation

| Question | Go to |
|----------|-------|
| What was decided? | ADR-002 … ADR-005 in [`adrs/`](../../adrs/) |
| Why was it decided? | [`architecture-reach-brief.md`](architecture-reach-brief.md) Layer 2 + guide-adr-00N |
| Proof / attack paths | guide → archive section cited in guide |
| What to build? | [`04-architecture-baseline-v0.md`](../professional-baseline/04-architecture-baseline-v0.md) |
| What's still open? | [`architecture-reach-brief.md`](architecture-reach-brief.md) Layer 3 + [`05-decision-gap-register.md`](../professional-baseline/05-decision-gap-register.md) |
