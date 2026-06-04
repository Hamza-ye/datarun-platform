-- V9__auth_principal_binding_operations.sql
-- NW-040 / IDR-028: audited deployment-managed principal-binding provisioning.
-- Operation history is append-only; auth_principal_bindings remains the active
-- authentication lookup projection/support table.

CREATE TABLE auth_principal_binding_operations (
    id                         BIGSERIAL PRIMARY KEY,
    operation_id               TEXT NOT NULL UNIQUE,
    operation_hash             TEXT NOT NULL,
    manifest_version           TEXT NOT NULL,
    manifest_source            TEXT NOT NULL,
    manifest_content_hash      TEXT NOT NULL,
    applied_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    applied_by                 TEXT NOT NULL,
    issuer                     TEXT NOT NULL,
    subject                    TEXT NOT NULL,
    target_actor_id            UUID NOT NULL,
    desired_active             BOOLEAN NOT NULL,
    reason                     TEXT NOT NULL,
    previous_active_binding_id BIGINT,
    previous_actor_id          UUID,
    resulting_binding_id       BIGINT,
    changed                    BOOLEAN NOT NULL
);

CREATE INDEX idx_auth_principal_binding_operations_principal
    ON auth_principal_binding_operations (issuer, subject, id);

CREATE INDEX idx_auth_principal_binding_operations_manifest
    ON auth_principal_binding_operations (manifest_version, manifest_content_hash);
