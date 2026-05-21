-- V3__subject_aliases_and_lifecycle.sql
-- Phase 1b: Identity Resolver — alias table.

-- Alias table for identity resolution (merge).
-- Materialized with eager transitive closure — contract C7 demands single-hop lookup.
-- retired_id → surviving_id mapping. All lookups are O(1).
CREATE TABLE subject_aliases (
    retired_id    UUID PRIMARY KEY,
    surviving_id  UUID NOT NULL,
    merged_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_no_self_alias CHECK (retired_id != surviving_id)
);
CREATE INDEX idx_aliases_surviving ON subject_aliases (surviving_id);
