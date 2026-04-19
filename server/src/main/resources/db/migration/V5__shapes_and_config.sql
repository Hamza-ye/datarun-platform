-- V5__shapes_and_config.sql
-- Phase 3a: Configuration — shapes, activities, expression rules, config packages.
-- IDR-017 (shape storage), IDR-018 (expression rules), IDR-019 (config packages).

-- Shape definitions: versioned, immutable snapshots (IDR-017)
CREATE TABLE shapes (
    name        VARCHAR(100) NOT NULL,
    version     INTEGER NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'active',
    sensitivity VARCHAR(20) NOT NULL DEFAULT 'standard',
    schema_json JSONB NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (name, version),
    CONSTRAINT chk_shape_name CHECK (name ~ '^[a-z][a-z0-9_]*$'),
    CONSTRAINT chk_version_positive CHECK (version > 0),
    CONSTRAINT chk_status CHECK (status IN ('active', 'deprecated')),
    CONSTRAINT chk_sensitivity CHECK (sensitivity IN ('standard', 'elevated', 'restricted'))
);

CREATE INDEX idx_shapes_name ON shapes (name);
CREATE INDEX idx_shapes_status ON shapes (name, status);

-- Activity definitions: organizational grouping of shapes (IDR-017)
CREATE TABLE activities (
    name        VARCHAR(100) PRIMARY KEY,
    config_json JSONB NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'active',
    sensitivity VARCHAR(20) NOT NULL DEFAULT 'standard',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_activity_name CHECK (name ~ '^[a-z][a-z0-9_]*$'),
    CONSTRAINT chk_activity_status CHECK (status IN ('active', 'deprecated')),
    CONSTRAINT chk_activity_sensitivity CHECK (sensitivity IN ('standard', 'elevated', 'restricted'))
);

-- Expression rules: L2 form logic, external to shapes (IDR-017 L1/L2 separation, IDR-018)
CREATE TABLE expression_rules (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_ref VARCHAR(255) NOT NULL,
    shape_ref    VARCHAR(255) NOT NULL,
    field_name   VARCHAR(100) NOT NULL,
    rule_type    VARCHAR(20) NOT NULL,
    expression   JSONB NOT NULL,
    message      TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_rule_type CHECK (rule_type IN ('show_condition', 'default', 'warning')),
    CONSTRAINT uq_expression_rule UNIQUE (activity_ref, shape_ref, field_name, rule_type)
);

CREATE INDEX idx_expr_activity_shape ON expression_rules (activity_ref, shape_ref);

-- Config packages: immutable snapshots of published configuration (IDR-019)
CREATE TABLE config_packages (
    version      INTEGER PRIMARY KEY,
    package_json JSONB NOT NULL,
    published_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_by UUID
);

-- Config version tracking per device (IDR-019)
ALTER TABLE device_sync_state ADD COLUMN config_version INTEGER NOT NULL DEFAULT 0;
