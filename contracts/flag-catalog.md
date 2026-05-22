# Flag Catalog

All flag categories raised by the integrity pipeline. Each flag is persisted as an event with `type = alert` and `shape_ref = conflict_detected/v1`; the `flag_category` field lives in the payload. See [ADR-006](../docs/adrs/adr-006-flag-semantics.md) for flag semantics and [ADR-007](../docs/adrs/adr-007-envelope-type-closure.md) for envelope-type mapping.

## Categories

| # | Category | Raised by | Resolvability | Designated Resolver | Phase |
|---|----------|-----------|---------------|---------------------|-------|
| 1 | `concurrent_state_change` | Identity CD | `manual_only` | system | 0 |
| 2 | `stale_reference` | Identity CD | `auto_eligible` | system | 1 |
| 3 | `identity_conflict` | Manual (admin) | `manual_only` | system | 1 |
| 4 | `scope_violation` | Auth CD | `manual_only` | broadest-scope actor | 2c |
| 5 | `temporal_authority_expired` | Auth CD | `auto_eligible` | broadest-scope actor | 2c |
| 6 | `role_stale` | Auth CD | `manual_only` | supervisor actor | 2c |
| 7 | `domain_uniqueness_violation` | Shape-declared uniqueness CD | `manual_only` | TBD | **Deferred — Phase 4 / IDR-022** |
| 8 | `transition_violation` | Pattern state-machine CD | `auto_eligible` | TBD | **Deferred — Phase 4 / IDR-020** |
| 9 | *reserved* | *reserved* | *reserved* | *reserved* | **Reserved — growth slot; do not claim without ADR amendment** |

This catalog matches the 9 categories defined in [`docs/architecture/boundary.md`](../docs/architecture/boundary.md) SG-2 and [`docs/architecture/cross-cutting.md`](../docs/architecture/cross-cutting.md) §7. Categories 7 and 8 are specified architecturally but their detectors land with Phase 4. Category 9 is an explicit growth slot — claiming it requires an ADR-level amendment, not an IDR.

## Detection Ordering

Phase 4 push processing order is:

1. **Structural envelope and shape validation** — runs before persistence. Invalid structure is rejected; state anomalies are not.
2. **Identity/lifecycle CD** — raises `concurrent_state_change` and `stale_reference`.
3. **Authorization CD** — raises `temporal_authority_expired`, `scope_violation`, and `role_stale`.
4. **Domain uniqueness CD** — raises `domain_uniqueness_violation`.
5. **Pattern transition CD** — raises `transition_violation`.

Within authorization CD, `temporal_authority_expired` remains before `scope_violation` so expired actors are not misclassified as scope violators. Phase 4 revises `role_stale`: it is an action-authority mismatch under IDR-021 role-action tables, not a flag for every role-label change. A role change is clean when both the horizon role and current role permit the attempted action.

## State Exclusion

All flagged events (any category) are excluded from state derivation in SubjectProjection. The exclusion is category-agnostic — any event targeted by a `shape_ref = conflict_detected/v1` event is excluded.

## Resolution

Flags are resolved via the admin UI (`/admin/flags`). Resolution options:

- **accepted** — the flagged event is accepted into state derivation (flag cleared).
- **rejected** — the flagged event is permanently excluded from state derivation (flag cleared, event stays in the store).
- **reclassified** — the flagged event has been reclassified to a different subject; requires a target `reclassified_subject_id`. Only valid for `identity_conflict` flags.

Authorship discriminates manual vs. auto resolution: manual resolutions are emitted with `type = review` by a human actor; auto resolutions (Phase 4) are emitted with `type = capture` by an actor whose `id` is prefixed `system:auto_resolution/...` (see ADR-007 §S2/F-A3/F-A5 and ADR-008 §S2). Auto-resolution for `temporal_authority_expired` is deferred to Phase 4 (Trigger Engine L3b).
