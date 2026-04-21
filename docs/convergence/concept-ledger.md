# Concept Ledger

> **Lifespan**: temporary scaffolding. Archived at Phase 4 freeze.
>
> The ledger is the single inventory of every named concept in the platform.
> The charter is mechanically derivable from it: each STABLE row produces one
> charter claim.

## Schema

Every named concept (primitive, invariant, contract, flag, algorithm, config
knob, derived value, reserved/deferred item) has exactly one row.

| field | meaning |
|---|---|
| `concept` | kebab-case canonical name. Unique. |
| `classification` | one of: PRIMITIVE / INVARIANT / CONTRACT / CONFIG / FLAG / ALGORITHM / DERIVED / RESERVED / OBSOLETE / OPEN |
| `settled-by` | ADR cite (`ADR-NNN §X`) or `—` if OPEN |
| `status` | OPEN / PROPOSED / STABLE / DEFERRED / SUPERSEDED |
| `introduced-in` | first doc that named the concept |
| `history` | append-only list of `(round, classification, settled-by)` tuples |
| `notes` | optional one-liner; blockers, FP cites, gotchas |

## Rules

1. **Append-only history.** A concept's classification can change across
   rounds, but the history column records every change. Never delete.
2. **Supersession trigger.** If a round changes a concept's classification, the
   ADR previously listed in `settled-by` is superseded per `supersede-rules.md`.
3. **STABLE definition.** A row is STABLE only after a full round in which its
   classification did not change AND no upstream concept it depends on changed.
4. **DEFERRED definition.** A row is DEFERRED when a deliberate decision is
   made not to settle it in this convergence pass. Must name the blocking
   condition (future ADR, future phase, external dependency).
5. **OBSOLETE definition.** A row is OBSOLETE when a concept turns out not to
   exist in the platform at all (was a misnamed thing, a duplicate, or
   abandoned). Row stays for traceability.
6. **Phase 4 freeze precondition.** Every row is STABLE, DEFERRED, or OBSOLETE.
   Zero rows in OPEN, PROPOSED, or SUPERSEDED status.

## Classifications — definitions

- **PRIMITIVE**: a load-bearing object/concept the platform is built on
  (Event, Subject, Actor, Device). Cannot be reduced to other concepts.
- **INVARIANT**: a property that always holds and which other code may rely on
  (envelope-has-11-fields, append-only-events).
- **CONTRACT**: a shared interface/format (envelope schema, sync protocol).
- **CONFIG**: a knob that varies per-deployment or per-tenant.
- **FLAG**: an integrity/auth signal raised by detection (scope_violation).
- **ALGORITHM**: a named computational procedure (conflict detection sweep,
  scope containment check).
- **DERIVED**: something computed from other concepts; not stored.
- **RESERVED**: named but not currently emitted/used (envelope `process` type).
- **OBSOLETE**: was a concept once; no longer exists.
- **OPEN**: classification not yet decided.

## Rows

> Phase 0 populates this section. Until then, this file is schema only.

| concept | classification | settled-by | status | introduced-in | history | notes |
|---|---|---|---|---|---|---|

<!-- Phase 0 inventory will fill rows here, one per named concept. -->
