# Sync Protocol Contract

> Push/pull contract for event exchange between device and server. Watermark-based, idempotent, order-independent.

---

## Transport

- Protocol: HTTPS (REST/JSON)
- Content-Type: `application/json`
- All events conform to [envelope.schema.json](envelope.schema.json)

---

## Push — `POST /api/sync/push`

Device sends events to server. Server deduplicates by `id`, assigns `sync_watermark` on insert.
Production-auth mode requires `Authorization: Bearer <credential>`. Development
`dev-token` mode may allow unauthenticated push for local compatibility only.

### Request

```json
{
  "events": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "type": "capture",
      "shape_ref": "basic_capture/v1",
      "activity_ref": null,
      "subject_ref": { "type": "subject", "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7" },
      "actor_ref": { "type": "actor", "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479" },
      "device_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "device_seq": 1,
      "sync_watermark": null,
      "timestamp": "2026-04-16T10:30:00Z",
      "payload": { "name": "Site Alpha", "category": "urban", "notes": "Initial survey" }
    }
  ],
  "device_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "last_pull_watermark": 0
}
```

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `events` | array | yes | — | Events to push. |
| `device_id` | string (uuid) | no | — | Pushing device's identity. Used for concurrency detection bookkeeping. |
| `last_pull_watermark` | integer | no | 0 | The highest `sync_watermark` this device received in its most recent pull. Used for concurrency detection. If omitted, defaults to `0` (conservative — treats all existing events as potentially unseen). |

### Response — `200 OK`

```json
{
  "accepted": 1,
  "duplicates": 0,
  "flags_raised": 0
}
```

### Semantics

| Aspect | Behavior |
|--------|----------|
| Deduplication | By `id` (UUID). If event with same `id` already exists, it is counted as `duplicates`, not `accepted`. No error. |
| Watermark assignment | Server assigns `sync_watermark` (monotonically increasing BIGSERIAL) on insert. |
| Idempotency | Sending the same events twice produces the same result. `accepted + duplicates = len(events)`. |
| Ordering | Events may arrive in any order. Server does not require `device_seq` ordering. |
| Validation | Every event is validated against `envelope.schema.json`. Invalid events → `400 Bad Request` with details. Nothing persisted from the batch if any event is invalid. |
| Atomicity | All-or-nothing per request. Either all valid events are persisted, or none are (on validation failure). |
| Authenticated actor binding | When the bearer credential resolves to an actor, every client-pushed event must carry `actor_ref.id` equal to that actor UUID. Client-authored `system:*` actors, missing actor IDs, non-UUID actor IDs, or mismatched actor UUIDs reject the entire batch before persistence. |
| Concurrency detection | After persisting events (Tx1), the server evaluates each accepted event for concurrency conflicts (Tx2). For each event, `W_effective = min(server-assigned event.sync_watermark, last_pull_watermark)`. If the event's subject has events from other devices with `sync_watermark > W_effective`, a `concurrent_state_change` flag is raised. CD failure does not affect event persistence. |

### Error Responses

| Status | Condition | Body |
|--------|-----------|------|
| `400` | One or more events fail schema validation | `{ "error": "validation_failed", "details": [...] }` |
| `400` | One or more payloads fail shape validation | `{ "error": "shape_validation_failed", "details": [...] }` |
| `400` | One or more events fail authenticated actor binding | `{ "error": "actor_binding_failed", "details": [...] }` |
| `400` | Empty events array | `{ "error": "empty_batch" }` |
| `401` | Missing bearer token in production-auth mode | `{ "error": "missing_token" }` |
| `401` | Empty bearer token | `{ "error": "empty_token" }` |
| `401` | Invalid, revoked, or unmapped bearer credential | `{ "error": "invalid_token" }` |
| `500` | Server error | `{ "error": "internal_error" }` |

---

## Pull — `POST /api/sync/pull`

Device requests events it hasn't seen. Server returns events after the given watermark. Pull is actor-scoped and requires `Authorization: Bearer <actor_token>`.

### Request

```json
{
  "since_watermark": 0,
  "limit": 100,
  "device_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "config_version": 12
}
```

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `since_watermark` | integer | yes | — | Return events with `sync_watermark > since_watermark`. Use `0` for first sync. |
| `limit` | integer | no | 100 | Max events to return. Server may cap this. |
| `device_id` | string (uuid) | no | — | Pulling device's identity. When provided, the server records pull progress in `device_sync_state`. |
| `config_version` | integer | no | — | Config version currently known by the device. When provided with `device_id`, stored for sync/config observability. |

### Response — `200 OK`

```json
{
  "events": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "type": "capture",
      "shape_ref": "basic_capture/v1",
      "activity_ref": null,
      "subject_ref": { "type": "subject", "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7" },
      "actor_ref": { "type": "actor", "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479" },
      "device_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "device_seq": 1,
      "sync_watermark": 42,
      "timestamp": "2026-04-16T10:30:00Z",
      "payload": { "name": "Site Alpha", "category": "urban", "notes": "Initial survey" }
    }
  ],
  "latest_watermark": 42,
  "has_more": false,
  "config_version": 12
}
```

### Semantics

| Aspect | Behavior |
|--------|----------|
| Ordering | Events ordered by `sync_watermark` ascending. |
| Pagination | If `len(events) == limit`, more events may exist. Client should pull again with `since_watermark = latest_watermark`. |
| Completion | If `len(events) < limit`, client has all available events. |
| `latest_watermark` | The highest `sync_watermark` in the returned batch. Client stores this for next pull. If no events returned, equals `since_watermark`. |
| `has_more` | Server hint that the page filled the requested/capped limit. Clients may still use the `len(events) == limit` rule for compatibility. |
| `config_version` | Latest published config package version. Device compares it with local config and fetches `/api/sync/config` when newer. |
| Stability | Pulling the same watermark range returns the same events. Events are immutable and never deleted. |

### Error Responses

| Status | Condition | Body |
|--------|-----------|------|
| `400` | Missing or invalid `since_watermark` | `{ "error": "invalid_watermark" }` |
| `401` | Missing bearer token | `{ "error": "missing_token" }` |
| `401` | Empty bearer token | `{ "error": "empty_token" }` |
| `401` | Invalid or revoked bearer token | `{ "error": "invalid_token" }` |
| `500` | Server error | `{ "error": "internal_error" }` |

---

## Subject History Backfill — `POST /api/sync/subject-history`

Device requests a subject-bound historical page for projection repair after an actor receives a subject assignment whose prior event timeline is older than the device's normal live-sync watermark. This is a distinct backfill surface; it is not normal pull and requires `Authorization: Bearer <actor_token>`.

### Request

```json
{
  "subject_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "activity_ref": "malaria_case_activity",
  "cursor": 0,
  "limit": 100
}
```

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `subject_id` | string (uuid) | yes | — | Subject whose activity-specific history is requested. Merge aliases resolve to the canonical surviving subject for history selection. Split lineage is not aliasing. |
| `activity_ref` | string | yes | — | Activity key for the subject-level pattern state key. Domain events from other activities are not returned. |
| `cursor` | integer | no | 0 | Backfill page cursor. It is compared to event `sync_watermark`, but is independent of the device's normal `last_pull_watermark`. |
| `limit` | integer | no | 100 | Max events to return. Server may cap this. |

### Response — `200 OK`

```json
{
  "requested_subject_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "subject_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "activity_ref": "malaria_case_activity",
  "cursor": 0,
  "next_cursor": 42,
  "has_more": false,
  "events": [
    { "...full envelope..." }
  ]
}
```

### Semantics

| Aspect | Behavior |
|--------|----------|
| Authorization | Evaluated at request time on every page from the bearer token's current active assignments. If the actor is no longer assigned to the requested subject/activity, the page is rejected with `403`. |
| Pagination | Events are ordered by `sync_watermark` ascending. Clients continue with `cursor = next_cursor` while `has_more` is true. Retrying the same request is idempotent. |
| Live-sync isolation | Backfill does not call or mutate `/api/sync/pull`, does not update `device_sync_state`, and does not lower or rewrite the normal live-sync watermark. |
| Activity filtering | Domain events must match the requested `activity_ref`. Related conflict flags/resolutions are included only when their source event is in the requested subject/activity slice. |
| Assignment events | Assignment lifecycle events are returned only when their assignment scope explicitly names the requested subject alias group and is activity-unrestricted or includes the requested `activity_ref`. Broad geographic assignment history is not exposed as audit pull. |
| Merge aliases | Reads for a surviving subject or any retired alias include the surviving subject's events plus retired-alias events in that merge chain. |
| Split lineage | Split successors do not inherit source history. Backfill for an archived split source, when currently authorized, returns only source history. |
| Audit scope | The endpoint is subject-bound. It does not provide arbitrary historical reconstruction by actor, geography, activity, or time range. |

### Error Responses

| Status | Condition | Body |
|--------|-----------|------|
| `400` | Missing or invalid `subject_id` | `{ "error": "invalid_subject" }` |
| `400` | Missing or blank `activity_ref` | `{ "error": "invalid_activity" }` |
| `400` | Negative cursor | `{ "error": "invalid_cursor" }` |
| `401` | Missing bearer token | `{ "error": "missing_token" }` |
| `401` | Empty bearer token | `{ "error": "empty_token" }` |
| `401` | Invalid or revoked bearer token | `{ "error": "invalid_token" }` |
| `403` | Actor is not currently authorized for the subject/activity page | `{ "error": "subject_history_not_authorized" }` |
| `500` | Server error | `{ "error": "internal_error" }` |

---

## Read APIs

### List Subjects — `GET /api/subjects`

Returns subjects with their latest event summary (minimal projection).

### Response — `200 OK`

```json
{
  "subjects": [
    {
      "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "latest_event_type": "capture",
      "latest_timestamp": "2026-04-16T10:30:00Z",
      "event_count": 3
    }
  ]
}
```

### Subject Events — `GET /api/subjects/{id}/events`

Returns the full event timeline for one subject, ordered by `sync_watermark`.

### Response — `200 OK`

```json
{
  "subject_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "events": [
    { "...full envelope..." }
  ]
}
```

### Error Responses

| Status | Condition | Body |
|--------|-----------|------|
| `404` | Subject not found | `{ "error": "subject_not_found" }` |

---

## Invariants

1. **Events are never modified or deleted** (C1, ADR-1 S1)
2. **Deduplication is by `id`** — same UUID always resolves to same event
3. **`sync_watermark` is monotonically increasing** — committed event watermarks are ordered and never reused; sequence gaps are allowed
4. **Push is idempotent** — safe to retry on network failure
5. **Pull is stable** — same query returns same results (append-only store)
6. **Subject-history backfill is not live sync** — it uses an independent cursor and never mutates normal pull watermarks
