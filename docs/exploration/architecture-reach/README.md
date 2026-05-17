# Architecture Reach

Orientation layer for ADR-002 through ADR-005: named parts, strategic bets, interactions, and thickness—without re-reading the full exploration archive.

## Authority

This folder is **not** slice or atomization authority. Use it to understand *why* and *what was discovered*; use the professional baseline to build.

| Need | Read |
|------|------|
| Reach picture (parts, strategy, thickness) | [`architecture-reach-brief.md`](architecture-reach-brief.md) |
| Checkpoint ↔ ADR ↔ exploration index | [`adr-exploration-crossref.md`](adr-exploration-crossref.md) |
| Engineering constraints (no ADR narrative) | [`../professional-baseline/04-architecture-baseline-v0.md`](../professional-baseline/04-architecture-baseline-v0.md) |
| Closure kernels | [`../10-adr1-5-rest-state-closure-register.md`](../10-adr1-5-rest-state-closure-register.md) |
| Boundary routing for spec work | [`../professional-baseline/07-system-boundary-map.md`](../professional-baseline/07-system-boundary-map.md) |
| Open gaps after baseline | [`../professional-baseline/05-decision-gap-register.md`](../professional-baseline/05-decision-gap-register.md) |

**ADR-001** is upstream context only (append-only event log, minimum envelope). **ADR-006+** are out of scope for this folder.

## Contents

| File | Role |
|------|------|
| [`architecture-reach-brief.md`](architecture-reach-brief.md) | Four-layer reach document (north star, topology, per-ADR briefs, thickness register) |
| [`adr-exploration-crossref.md`](adr-exploration-crossref.md) | Dated checkpoint matrix cross-referencing ADRs 002–005 and archive explorations |

## Evidence trail

- ADRs: [`../../adrs/`](../../adrs/)
- Exploration guides: [`../../exploration/guide-adr-002.md`](../../exploration/guide-adr-002.md) … `guide-adr-005.md`
- Raw explorations: [`../../exploration/archive/`](../../exploration/archive/)
- Consolidation (Pass 3): [`../../exploration/24-decision-harvest.md`](../../exploration/24-decision-harvest.md) … [`26-contract-extraction.md`](../../exploration/26-contract-extraction.md)
- Historical structural catalog (superseded for primitive naming): [`../../architecture/primitives.md`](../../architecture/primitives.md) — see Reach Brief §Historical note