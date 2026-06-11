# Decision Anchor Layer Index

## Context Capsule

* Artifact: `000-decision-anchor-layer-index.md`
* Status: Package entry point for the decision-anchor layer
* Mode: Routing and source-order index only; no redesign, no reopening, no new architecture decisions.
* Purpose:

  * Provide the low-token entry point for the decision-anchor package.
  * State current authority order before agents consume derived artifacts.
  * Route readers to the smallest artifact needed for architecture-sensitive work.
  * Preserve older recovery files as lineage/reference instead of active competing routers.

---

## 1. Authority Order

Use this order when reading or patching this package:

```txt
docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md
  = current architecture decision authority

contracts/
  = implementation-facing wire/process boundary authority for crossed boundaries

docs/agent-working-surface/baseline-acceptance-register.md
docs/agent-working-surface/platform-next-work-backlog.md
docs/decisions/
  = current validation, runtime evidence, and decision-provenance inputs during catch-up

.review/001-architecture-decisions-listing-passes/011-core-architecture-decision-records.md
.review/001-architecture-decisions-listing-passes/012-vocabulary-anchor-map.md
.review/001-architecture-decisions-listing-passes/013-gap-routing-playbook.md
  = derived operational stewardship layer after validation and catch-up

.review/001-architecture-decisions-listing-passes/002-phase0-decision-register.md
.review/001-architecture-decisions-listing-passes/008-authoritative-architecture-map.md
  = recovery verification lineage for the original extraction pass
```

This package does not override the CDL, contracts, or accepted runtime evidence. If this package conflicts with those sources, treat the package as stale and patch it through the catch-up plan.

---

## 2. What To Read

| Need | Read |
|---|---|
| Understand the catch-up sequence | `017-systematic-catchup-and-alignment-plan.md` |
| Check adoption findings and required patches | `016-stewardship-layer-assessment.md` |
| Understand extraction method and pass dependencies | `009-decision-anchor-extraction-charter.md` |
| Inspect historical candidate inventory | `010-candidate-architecture-decision-inventory.md` |
| Find stable architecture decision anchors | `011-core-architecture-decision-records.md` |
| Resolve vocabulary ownership and term collisions | `012-vocabulary-anchor-map.md` |
| Route a proposed gap or future work item | `013-gap-routing-playbook.md` |
| Check original coherence findings | `014-architecture-decision-coherence-audit.md` |
| Check first correction instructions/provenance | `015-decision-anchor-correction-patch.md` |

During catch-up, use `017` before patching downstream files. Patch `011` before `012`, and patch both before splitting or extending `013`.

---

## 3. Artifact Roles

### Active After Catch-Up

These are intended to become the stable operational stewardship surface:

```txt
000-decision-anchor-layer-index.md
011-core-architecture-decision-records.md
012-vocabulary-anchor-map.md
013-gap-routing-playbook.md
013-known-gap-routing-register.md
```

The known-gap register is the active lookup for current known gaps after the Wave 4 split.

### Provenance And Validation Trail

These should remain useful as reference material, but should not become parallel active surfaces:

```txt
009-decision-anchor-extraction-charter.md
010-candidate-architecture-decision-inventory.md
014-architecture-decision-coherence-audit.md
015-decision-anchor-correction-patch.md
016-stewardship-layer-assessment.md
017-systematic-catchup-and-alignment-plan.md
```

### Recovery Lineage

These explain the original extraction baseline and should remain as historical verification inputs:

```txt
001-recovery-strategy.md
002-phase0-decision-register.md
003-phase1-adr2-identity-conflict-recovery.md
004-phase2-adr3-auth-sync-recovery.md
005-phase3-adr4-config-boundary-recovery.md
006-phase4-adr5-state-progression-recovery.md
007-phase5-cross-lineage-vocabulary.md
008-authoritative-architecture-map.md
```

---

## 4. Catch-Up Rule

Do not update active repo routers to point here until the catch-up waves pass validation.

Until then:

* CDL remains architecture authority.
* `docs/agent-working-surface/README.md` remains the active working-surface router.
* BAR/NW/IDR artifacts remain validation and provenance inputs.
* This package is a derived alignment surface being stabilized.

The next bounded slice after this index is Wave 2 from `017`: patch `011` with CDL anchors and accepted durable IDR/BAR/NW extensions before changing downstream vocabulary or routing.
