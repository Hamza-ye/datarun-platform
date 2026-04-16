-- V1__create_events_table.sql
-- Event store: append-only, immutable. The foundation of Datarun.
-- Schema from plan.md §6. sync_watermark as BIGSERIAL for monotonic ordering.

CREATE TABLE events (
    id              UUID PRIMARY KEY,
    type            VARCHAR(50) NOT NULL,
    shape_ref       VARCHAR(255) NOT NULL,
    activity_ref    VARCHAR(255),
    subject_ref     JSONB NOT NULL,
    actor_ref       JSONB NOT NULL,
    device_id       UUID NOT NULL,
    device_seq      INTEGER NOT NULL,
    sync_watermark  BIGSERIAL,
    timestamp       TIMESTAMPTZ NOT NULL,
    payload         JSONB NOT NULL,
    received_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(device_id, device_seq)
);

CREATE INDEX idx_events_subject ON events ((subject_ref->>'id'));
CREATE INDEX idx_events_watermark ON events (sync_watermark);
