---
id: idr-016
title: Actor identification — simple token table for Phase 2 (DD-4)
status: active
date: 2026-04-19
phase: 2a
type: decision
touches: [server/sync, server/admin, mobile/data]
superseded-by: ~
evolves: ~
commit: ~
tags: [authorization, authentication, sync, dd]
---

# Actor identification — simple token table for Phase 2 (DD-4)

## Context

Phase 1 has no authentication — a hardcoded admin actor UUID. Phase 2 requires the server to know which actor is pulling, to compute their scope-filtered payload. Without actor identification, scope-filtered sync cannot function.

This is not the "full auth mechanism" question. It's the narrower question: what's the minimum viable actor identification that unblocks scope-filtered sync?

## Decision

Simple `actor_tokens` table. Server generates a random token per registered actor. Device sends token in `Authorization: Bearer <token>` header. Server resolves token → actor_id.

```sql
CREATE TABLE actor_tokens (
    token      VARCHAR(64) PRIMARY KEY,
    actor_id   UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked    BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_actor_tokens_actor ON actor_tokens (actor_id);
```

Token generation: server-side `SecureRandom`, 32 bytes hex-encoded (64 chars). Created via admin API when provisioning an actor.

Pull/push resolution: `SELECT actor_id FROM actor_tokens WHERE token = :token AND revoked = FALSE`. 401 if no match.

### Migration path to Keycloak

The `Authorization: Bearer <token>` header convention is the same regardless of whether the token is a simple string or a JWT. When Keycloak is introduced (Phase 2c or Phase 3):

1. Sync endpoints switch from token-table lookup to JWT validation
2. Header format unchanged — mobile code doesn't change
3. Token table can coexist during migration (check JWT first, fall back to token table)

## Alternatives Rejected

- **Keycloak from day one** — adds Keycloak deployment, Flutter OIDC SDK, token refresh logic, offline token caching. Significant operational complexity for a testing phase. Scope-filtered sync is the Phase 2 goal, not production auth.
- **Device-registration flow** — ties token to device_id. Complicates shared-device support (IG-14, deferred). No benefit over simple actor token for Phase 2's single-actor-per-device target.

## Consequences

- Phase 2a/2b has no real security on sync endpoints (tokens are static, no expiry). Acceptable for development and early testing.
- Revocation is immediate (set `revoked = TRUE`). No token refresh needed.
- Admin API for token management: generate, list, revoke.

## Traces

- ADR: adr-003 (S2 — sync endpoint must identify actor)
- Constraint: IG-1 (implementation-grade choice)
- Files: server migration V4, SyncController, actor_tokens admin API
