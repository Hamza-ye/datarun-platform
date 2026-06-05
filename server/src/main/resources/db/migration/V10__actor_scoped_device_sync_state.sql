-- NW-055 / IDR-030: shared physical devices must not share pull/config
-- progress across actor sessions. Existing rows predate this boundary and are
-- retained under the zero actor sentinel for bookkeeping only.

ALTER TABLE device_sync_state ADD COLUMN actor_id UUID;

UPDATE device_sync_state
SET actor_id = '00000000-0000-0000-0000-000000000000'::uuid
WHERE actor_id IS NULL;

ALTER TABLE device_sync_state ALTER COLUMN actor_id SET NOT NULL;

ALTER TABLE device_sync_state DROP CONSTRAINT device_sync_state_pkey;
ALTER TABLE device_sync_state ADD PRIMARY KEY (device_id, actor_id);
