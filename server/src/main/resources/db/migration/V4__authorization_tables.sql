-- V4__authorization_tables.sql
-- Phase 2a: Authorization & Multi-Actor — location hierarchy, actor tokens,
-- subject-location mapping, and denormalized scope metadata on events.

-- Location hierarchy (static reference data, materialized path — IDR-014)
CREATE TABLE locations (
    id        UUID PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    parent_id UUID REFERENCES locations(id),
    level     VARCHAR(50) NOT NULL,
    path      TEXT NOT NULL  -- materialized path, e.g. '/region1/district3/site7'
);
CREATE INDEX idx_locations_path ON locations (path text_pattern_ops);
CREATE INDEX idx_locations_parent ON locations (parent_id);

-- Actor tokens (simple auth — IDR-016)
CREATE TABLE actor_tokens (
    token      VARCHAR(64) PRIMARY KEY,
    actor_id   UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked    BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_actor_tokens_actor ON actor_tokens (actor_id);

-- Subject-to-location mapping
CREATE TABLE subject_locations (
    subject_id  UUID PRIMARY KEY,
    location_id UUID NOT NULL REFERENCES locations(id),
    path        TEXT NOT NULL  -- denormalized from locations.path
);
CREATE INDEX idx_subject_locations_path ON subject_locations (path text_pattern_ops);

-- Denormalized scope metadata on events (IDR-015)
-- Infrastructure metadata, same status as sync_watermark. Not in event envelope.
-- NULL for non-subject events (assignments, system events without location).
ALTER TABLE events ADD COLUMN location_path TEXT;

-- Primary pull query index: watermark-ordered with covering columns
CREATE INDEX idx_events_scoped_pull
    ON events (sync_watermark)
    INCLUDE (location_path, type, activity_ref)
    WHERE location_path IS NOT NULL;

-- Assignment event lookup by target actor
CREATE INDEX idx_events_assignment_actor
    ON events ((payload->'target_actor'->>'id'))
    WHERE type = 'assignment_changed';
