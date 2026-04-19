# Flag Catalog

All flag categories raised by the integrity pipeline. Each flag is persisted as a `conflict_detected` event with `flag_category` in the payload.

## Categories

| # | Category | Raised by | Resolvability | Designated Resolver | Phase |
|---|----------|-----------|---------------|---------------------|-------|
| 1 | `concurrent_state_change` | Identity CD | `manual_only` | system | 0 |
| 2 | `stale_reference` | Identity CD | `auto_eligible` | system | 1 |
| 3 | `identity_conflict` | Manual (admin) | `manual_only` | system | 1 |
| 4 | `scope_violation` | Auth CD | `manual_only` | broadest-scope actor | 2c |
| 5 | `temporal_authority_expired` | Auth CD | `auto_eligible` | broadest-scope actor | 2c |
| 6 | `role_stale` | Auth CD | `manual_only` | supervisor actor | 2c |

## Detection Ordering

Auth CD runs AFTER identity CD in the push pipeline. Within auth CD:

1. **`temporal_authority_expired`** — checked first. If the actor's assignment ended before the event was created, flag as expired (not scope violation).
2. **`scope_violation`** — checked against active assignments only. Event's subject location must be contained by at least one active assignment's geographic scope.
3. **`role_stale`** — checked last. Compares the actor's role at event creation time against current role. Flagged if role changed.

## State Exclusion

All flagged events (any category) are excluded from state derivation in SubjectProjection. The exclusion is category-agnostic — any event targeted by a `conflict_detected` event is excluded.

## Resolution

Flags are resolved via the admin UI (`/admin/flags`). Resolution options:
- **accept** — accept the flagged event into state derivation (removes the flag)
- **reject** — permanently exclude the flagged event
- **reclassify** — change the flag category (e.g., from `scope_violation` to `concurrent_state_change`)

Auto-resolution for `temporal_authority_expired` is deferred to Phase 4 (Trigger Engine L3b).
