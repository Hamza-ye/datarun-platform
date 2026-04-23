# Supersede Rules

> **Lifespan**: permanent. After Phase 4 freeze, this file moves to
> `docs/adrs/_supersede-rules.md` and continues to govern future ADRs.
>
> **Companion**: [adr-authoring-rules.md](adr-authoring-rules.md) — rules for
> what an ADR body may and may not cite (enforces the one-way authority arrow
> from ADRs down to implementation).

## Core rule

ADRs are **immutable** once their status is `DECIDED`. Any change to a decided
ADR's content is forbidden, with one exception: the header may be updated to
add a `Superseded-By: ADR-NNN-R` line.

## Why no Addenda

The Addendum pattern (we used it for ADR-002) creates two authoritative
documents for one decision area. Future agents read one, miss the other,
implement against the older view. The pattern is the single largest source of
drift in this repo's history. It is forbidden.

## Supersession types

### Full supersede
The new ADR replaces the entire prior ADR. Prior ADR header gets:
```
Status: DECIDED
Superseded-By: ADR-NNN-R (full supersede, see ADR-NNN-R for current decision)
```

### Partial supersede
The new ADR replaces specific sections of the prior ADR. Prior ADR header gets:
```
Status: DECIDED (partially superseded)
Superseded-By: ADR-NNN-R (sections §X, §Y; remaining sections still authoritative)
```

The prior ADR's body remains unchanged. Readers cite the prior ADR for
non-superseded sections and the new ADR for superseded sections.

## Naming

- Replacement ADRs use suffix `-R` (revision 1), `-R2` (revision 2), etc.
- Topological/draft order is not renumbered. Chronological numbers are
  preserved for git-history navigability.
- Example: ADR-002 Addendum (drift) → becomes ADR-002-R when convergence
  reaches the envelope-vocabulary concept. ADR-002 stays in repo with
  `Superseded-By: ADR-002-R` header line.

## When supersession is mandatory

Triggered automatically by the convergence protocol when a concept-ledger row's
classification changes between rounds. The ADR previously listed in `settled-by`
must be superseded by the ADR producing the new classification, full or partial.

## When supersession is optional

If a new ADR adds new concepts or refines orthogonal aspects of an existing
ADR's domain *without changing any decided position*, no supersession is
needed. The new ADR cites the prior ADR as a related/upstream decision.

## Cite discipline

- Charter claims cite the **current authoritative** ADR. If ADR-001 was
  superseded by ADR-001-R, the charter cites `[ADR-001-R §X]`, not
  `[ADR-001 §X]`.
- The drift gate (`scripts/check-convergence.sh`) fails any charter cite
  that points to a superseded ADR section.
- Exploration docs are annotated, not edited (per `annotation-conventions.md`).
  An exploration doc may still cite a superseded ADR in its body; the
  annotation `>>> RECLASSIFIED BY ADR-NNN-R` records the supersession.
