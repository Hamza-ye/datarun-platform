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

**Convergence phase**: 2 (ADR drafting — round 1 in progress)
**Active code phase**: paused — code work resumes at Phase 4 freeze
**Last ADR landed**: ADR-007 (envelope type closure + integrity shape canonicalization)

---

## Invariants

> One line per invariant. Each cites the ADR that established it.

- **Accept-and-flag.** A validly-structured event is never rejected for state-based reasons; state anomalies surface as flag events, never as rejections or modifications. ([ADR-006 §S1](adrs/adr-006-flag-semantics.md))
- **Flag as canonical anomaly surface (event stream).** Flags are the canonical representation of state anomalies on the event stream; no parallel anomaly-record surface exists or is permitted on the event stream. Scoped to representation and emission — does not govern non-event-stream surfaces (telemetry, metrics, operational logs). ([ADR-006 §S2](adrs/adr-006-flag-semantics.md))
- **Server-side flag creation (default).** Flags are created server-side during sync processing; device-side creation is additively evolvable. ([ADR-006 §S4](adrs/adr-006-flag-semantics.md))
- **Envelope type vocabulary is closed at six values.** Allowed: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`. Extension is architecture-grade. `type` answers *which pipeline*; `shape_ref` answers *what fact*; `actor_ref` answers *who authored*. ([ADR-007 §S1](adrs/adr-007-envelope-type-closure.md); first-decision cite [ADR-004 §S3](adrs/adr-004-configuration-boundary.md))

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
| `conflict_detected/v1` (platform-bundled shape; integrity flag) | `contracts/shapes/` | [ADR-007 §S2](adrs/adr-007-envelope-type-closure.md) |
| `conflict_resolved/v1` (platform-bundled shape; spans `type=review` human, `type=capture` system) | `contracts/shapes/` | [ADR-007 §S2](adrs/adr-007-envelope-type-closure.md) |
| `subjects_merged/v1` (platform-bundled shape; `type=capture`) | `contracts/shapes/` | [ADR-007 §S2](adrs/adr-007-envelope-type-closure.md) |
| `subject_split/v1` (platform-bundled shape; `type=capture`) | `contracts/shapes/` | [ADR-007 §S2](adrs/adr-007-envelope-type-closure.md) |

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
