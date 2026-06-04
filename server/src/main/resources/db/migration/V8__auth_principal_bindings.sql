-- V8__auth_principal_bindings.sql
-- NW-037 / IDR-027: production auth principal-to-actor binding.
-- Authentication principals are lookup/supporting state only. Authority remains
-- derived from assignment events.

CREATE TABLE auth_principal_bindings (
    id             BIGSERIAL PRIMARY KEY,
    issuer         TEXT NOT NULL,
    subject        TEXT NOT NULL,
    actor_id       UUID NOT NULL,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deactivated_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_auth_principal_bindings_active_principal
    ON auth_principal_bindings (issuer, subject)
    WHERE active = TRUE;

CREATE INDEX idx_auth_principal_bindings_actor
    ON auth_principal_bindings (actor_id)
    WHERE active = TRUE;
