-- Ship-1 schema: events, actor auth, geographic seeds.
-- Scenarios S00 + S01 + S03. Fresh slate; no compatibility with pre-Ship code.

-- =====================================================================
-- events: append-only, envelope-shaped. The only write-path table.
-- =====================================================================
CREATE TABLE events (
    id               UUID        PRIMARY KEY,
    type             VARCHAR(32) NOT NULL
        CHECK (type IN ('capture','review','alert','task_created','task_completed','assignment_changed')),
    shape_ref        VARCHAR(128) NOT NULL,
    activity_ref     VARCHAR(128),
    subject_type     VARCHAR(16) NOT NULL
        CHECK (subject_type IN ('subject','actor','assignment','process')),
    subject_id       UUID        NOT NULL,
    actor_id         VARCHAR(255) NOT NULL,
    device_id        UUID        NOT NULL,
    device_seq       BIGINT      NOT NULL,
    sync_watermark   BIGSERIAL   NOT NULL UNIQUE,
    timestamp        TIMESTAMPTZ NOT NULL,
    payload          JSONB       NOT NULL,
    received_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (device_id, device_seq)
);

CREATE INDEX idx_events_watermark   ON events (sync_watermark);
CREATE INDEX idx_events_subject     ON events (subject_id);
CREATE INDEX idx_events_shape_ref   ON events (shape_ref);
CREATE INDEX idx_events_type        ON events (type);
CREATE INDEX idx_events_actor       ON events (actor_id);

-- =====================================================================
-- actor_tokens: bearer auth for /api/sync/pull and push.
-- Deployer-provisioned at device bootstrap. One token per actor (for Ship-1).
-- =====================================================================
CREATE TABLE actor_tokens (
    token       VARCHAR(128) PRIMARY KEY,
    actor_id    UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_actor_tokens_actor ON actor_tokens (actor_id);

-- =====================================================================
-- villages: deployer-seeded geographic tree. One level for Ship-1.
-- Assignments carry a village UUID in scope.geographic.
-- =====================================================================
CREATE TABLE villages (
    id             UUID         PRIMARY KEY,
    district_name  VARCHAR(255) NOT NULL,
    name           VARCHAR(255) NOT NULL
);

-- =====================================================================
-- server_device_seq: monotonic counter for server-emitted events (flags).
-- Server acts as a "device" with a fixed UUID; its event sequence is a
-- PostgreSQL SEQUENCE so it survives restart.
-- =====================================================================
CREATE SEQUENCE server_device_seq START 1;
