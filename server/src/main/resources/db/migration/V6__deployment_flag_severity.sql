-- Phase 4.2: deployment-wide L0 flag severity overrides.

CREATE TABLE deployment_config (
    config_key  VARCHAR(100) PRIMARY KEY,
    config_json JSONB NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by  UUID
);
