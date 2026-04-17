-- V2__server_identity_and_device_sync.sql
-- Phase 1a: Server event production identity and device sync state tracking.

-- Server identity — persistent device_id for server-generated events.
-- Single-row table. Populated on first boot if SERVER_DEVICE_ID env var is not set.
CREATE TABLE server_identity (
    device_id UUID PRIMARY KEY
);

-- Monotonic sequence for server-generated events' device_seq.
-- Gaps from rollback are expected and harmless (S1: monotonically increasing, not gapless).
CREATE SEQUENCE server_device_seq START 1;

-- Device sync state — operational bookkeeping (audit, monitoring).
-- Concurrency detection uses min(event.sync_watermark, request.last_pull_watermark),
-- NOT this server-tracked state.
CREATE TABLE device_sync_state (
    device_id            UUID PRIMARY KEY,
    last_pull_watermark  BIGINT NOT NULL DEFAULT 0,
    last_pull_at         TIMESTAMPTZ
);
