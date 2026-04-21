# Datarun Charter — Currently Decided State

> **This is the single source of truth for "what is settled."** Generated from
> `docs/convergence/concept-ledger.md`. Every claim cites the ADR that decided
> it. The drift gate (`scripts/check-convergence.sh`) fails any claim missing
> a cite or pointing to a superseded ADR.
>
> **Lifespan**: permanent. During convergence (Phase 0–3) this file is
> incrementally populated as ADRs land. At Phase 4 freeze, it becomes
> immutable except via new ADR + same-commit regeneration.

## Status

**Convergence phase**: 0 (drafting scaffolding, not yet inventoried)
**Active code phase**: paused — code work resumes at Phase 4 freeze
**Last ADR landed**: ADR-005 (pre-convergence; will be re-evaluated)

---

## Invariants

> One line per invariant. Each cites the ADR that established it.

<!-- Populated incrementally as ADRs land in Phase 2. Example shape:
- The event envelope has exactly N fields. (cite to owning ADR)
- Events are append-only; never modified, never deleted. (cite to owning ADR)
-->

---

## Primitives

> Load-bearing concepts the platform is built on. Each row points to its
> contract definition and owning ADR.

| primitive | contract | settled-by |
|---|---|---|
| _to be populated in Phase 2_ | | |

---

## Contracts

> Shared formats and interfaces.

| contract | location | settled-by |
|---|---|---|
| _to be populated in Phase 2_ | | |

---

## Cross-cutting rules

> Rules that apply everywhere (accept-and-flag, alias-resolve-on-read, etc.).

<!-- One bullet per rule, each citing its ADR. Populated in Phase 2. -->

---

## Forbidden patterns

> Replaces CLAUDE.md's F1–F15 list at Phase 4 freeze. Each forbidden pattern
> cites the ADR whose decision makes it forbidden.

<!-- Populated in Phase 2. -->

---

## Active phase

**Phase**: convergence
**Sub-phase**: 0 (inventory)
**Next milestone**: every named concept in the repo has a row in
`docs/convergence/concept-ledger.md`.

---

## Pointers

- Codebase map: `CLAUDE.md` (codebase map section only)
- Convergence protocol: `docs/convergence/protocol.md`
- Drift gate: `scripts/check-convergence.sh`
