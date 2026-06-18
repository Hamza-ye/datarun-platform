-- V11__web_admin_config_candidates.sql
-- NW-088: production web-admin setup candidates and audit trail.

CREATE TABLE web_admin_config_candidates (
    candidate_key            VARCHAR(40) PRIMARY KEY
        DEFAULT 'current'
        CHECK (candidate_key = 'current'),
    candidate_json           JSONB NOT NULL,
    content_hash             VARCHAR(64) NOT NULL,
    validation_status        VARCHAR(20) NOT NULL DEFAULT 'not_run',
    validation_violations    JSONB NOT NULL DEFAULT '[]'::jsonb,
    validated_hash           VARCHAR(64),
    validated_at             TIMESTAMPTZ,
    validated_by             UUID,
    readiness_status         VARCHAR(20) NOT NULL DEFAULT 'not_reviewed',
    readiness_note           TEXT,
    readiness_hash           VARCHAR(64),
    readiness_at             TIMESTAMPTZ,
    readiness_by             UUID,
    approval_hash            VARCHAR(64),
    approved_at              TIMESTAMPTZ,
    approved_by              UUID,
    published_config_version INTEGER,
    published_at             TIMESTAMPTZ,
    published_by             UUID,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_web_admin_config_validation_status
        CHECK (validation_status IN ('not_run', 'passed', 'failed')),
    CONSTRAINT chk_web_admin_config_readiness_status
        CHECK (readiness_status IN ('not_reviewed', 'ready', 'rejected'))
);

CREATE TABLE web_admin_config_candidate_history (
    id             BIGSERIAL PRIMARY KEY,
    action         VARCHAR(40) NOT NULL,
    actor_id       UUID NOT NULL,
    candidate_hash VARCHAR(64),
    detail_json    JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_web_admin_config_history_action
        CHECK (action IN (
            'draft_saved',
            'validation_passed',
            'validation_failed',
            'readiness_recorded',
            'approved',
            'published'
        ))
);

CREATE INDEX idx_web_admin_config_history_created_at
    ON web_admin_config_candidate_history (created_at, id);
