# Sync Protocol Contract — Ship-1

> Push/pull + config contract for event exchange between a CHV device and the server.
> Watermark-based, idempotent, scope-filtered, bearer-token authenticated.
>
> **Scope**: this document describes the protocol as shipped for Ship-1 (scenarios
> S00 + S01 + S03). Subsequent ships may extend the contract; extensions must be
> additive and documented here in-place.

---

## Transport

- Protocol: HTTPS (REST/JSON), UTF-8
- Content-Type: `application/json`
- Authentication: `Authorization: Bearer <token>` on every `/api/sync/**` request.
  One token per actor (see §Bootstrap).
- All events conform to [envelope.schema.json](envelope.schema.json) (11 fields,
  envelope `type` closed at 6 values — ADR-007 §S1).

---

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `GET`  | `/api/sync/config` | Fetch the deployer config package visible to the caller (scope-filtered). |
| `POST` | `/api/sync/push`   | Submit locally-captured events. |
| `POST` | `/api/sync/pull`   | Receive events the caller is authorized to see, since a given watermark. |

All three require a valid bearer token. An unknown / missing / malformed token
returns `401 Unauthorized` with no body.

---

## Bootstrap

A device is provisioned by receiving an `actor_id` (UUID) and a bearer token
(opaque string, ≤128 chars) out-of-band. In the Ship-1 dev flow this is done
via `POST /dev/bootstrap` on the server, which seeds villages, actors, tokens,
and assignment events; production provisioning is out of scope for Ship-1.

The device persists `(actor_id, token, device_id, device_seq)` locally.
`device_id` is a UUID generated once on first launch (ADR-002 §S5).
`device_seq` is a monotonically increasing per-device counter assigned at
capture time, starting at 1.

---

## Config — `GET /api/sync/config`

Returns the deployer config the authenticated actor is allowed to see.

### Response — `200 OK`

```json
{
  "version": 1,
  "activities": [
    { "name": "household_observation" }
  ],
  "shapes": [
    {
      "shape_ref": "household_observation/v1",
      "schema": { "...JSON Schema Draft 2020-12..." }
    }
  ],
  "villages": [
    {
      "id": "b1a11111-1111-1111-1111-111111111111",
      "district_name": "Mirpur",
      "name": "Village-1"
    }
  ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `version` | integer | Package version. Fixed at `1` for Ship-1. |
| `activities` | array | Activities the caller can record against. Ship-1 ships one. |
| `shapes` | array | Full JSON Schema bodies for shapes referenced by those activities. |
| `villages` | array | Geographic scope instances **the caller is assigned to**. A CHV assigned to village-1 only sees village-1. Drives the in-app village picker; also answers "which subjects are reachable from this device". |

The config is read-only to devices. Ship-1 does not support shape versioning
on the wire; later ships will add a `shape_refs` index + delta semantics.

---

## Push — `POST /api/sync/push`

Device sends events to server. Server deduplicates by `id` and assigns
`sync_watermark` on insert.

### Request

```json
{
  "events": [
    {
      "id":              "550e8400-e29b-41d4-a716-446655440000",
      "type":            "capture",
      "shape_ref":       "household_observation/v1",
      "activity_ref":    "household_observation",
      "subject_ref":     { "type": "subject", "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7" },
      "actor_ref":       { "type": "actor",   "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479" },
      "device_id":       "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "device_seq":      1,
      "sync_watermark":  null,
      "timestamp":       "2026-04-24T10:30:00Z",
      "payload": {
        "household_name":         "Khan",
        "head_of_household_name": "Abdul Khan",
        "household_size":         5,
        "village_ref":            "b1a11111-1111-1111-1111-111111111111"
      }
    }
  ]
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `events` | yes | Non-empty array of full 11-field envelopes. Server assigns `sync_watermark`; client sends `null`. |

### Response — `200 OK`

```json
{
  "accepted":     1,
  "duplicates":   0,
  "flags_raised": 1
}
```

| Field | Type | Meaning |
|-------|------|---------|
| `accepted` | integer | Events inserted (new `id`). |
| `duplicates` | integer | Events dropped because `id` already existed (no error). |
| `flags_raised` | integer | Flag events emitted by the server during integrity detection on this batch. |

### Push semantics

| Aspect | Behavior |
|--------|----------|
| Batch validation | Every event is validated against the envelope schema and its declared `shape_ref`. **If any event fails, the whole batch is rejected (400) and nothing is persisted.** See §Validation and Retries below. |
| Dedup | By `id` (UUID). Duplicate `id` → counted in `duplicates`, not an error. |
| Watermark assignment | Server assigns `sync_watermark` (PostgreSQL `BIGSERIAL`, monotonic, no gaps guaranteed *within a dialect*). |
| Integrity detection | After insert, each newly-accepted `type=capture` event runs through server-side detection. Detection may emit 0..N `conflict_detected/v1` flag events (ADR-006 §S1/§S4; actor_ref = `system:conflict_detector/<category>`). Ship-1 categories: `scope_violation`, `identity_conflict`. |
| Ordering | Client may send events in any order. Server does not require `device_seq` ordering within a batch. |
| Idempotency | Re-posting the same batch produces the same result: `accepted + duplicates = len(events)`. `flags_raised` is 0 on retry (flags were already emitted). |
| Authorization | Caller's actor must match the envelope's `actor_ref.id` on captures. (Ship-1 enforces this only implicitly via the bootstrap flow; later ships make it explicit.) |

### Push errors

| Status | Body | Trigger |
|--------|------|---------|
| `400`  | `{ "error": "empty_batch" }` | `events` missing or empty. |
| `400`  | `{ "error": "validation_failed", "details": ["[event 0] envelope: ...", "[event 1] payload(shape/vN): ..."] }` | Any event in the batch fails envelope or payload validation. Whole batch is dropped. |
| `401`  | *(empty)* | Missing or invalid bearer token. |
| `500`  | `{ "error": "internal_error" }` | Unexpected server failure. |

### Validation and retries

Because a batch is **all-or-nothing on validation**, a client whose single
corrupt event is poisoning the batch must isolate it client-side (split and
retry) to make progress. Ship-1 does not currently return a partial-accept
mode; that is an explicit Ship-2 candidate if field experience demands it.

---

## Pull — `POST /api/sync/pull`

Device requests events it is authorized to see.

### Request

```json
{
  "since_watermark": 0,
  "limit": 100
}
```

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| `since_watermark` | no | 0 | Return events with `sync_watermark > since_watermark`. |
| `limit` | no | 100 | Max events in response. Server caps at 500. |

### Response — `200 OK`

```json
{
  "events": [ { "...full envelope..." } ],
  "latest_watermark": 42
}
```

### Pull semantics — scope filtering (ADR-003)

Pull is **always scope-filtered**. The server reconstructs the caller's active
geographic scope at request time by replaying `type=assignment_changed` events
whose `shape_ref` is `assignment_created/v1` or `assignment_ended/v1`. No scope
cache exists (Ship-1 FP-001 gate).

An event is returned to the caller iff one of the following holds:

| Event kind | Included when |
|---|---|
| `type=assignment_changed` | `payload.target_actor.id` equals the caller's `actor_id`. (The caller sees their own assignment history.) |
| `shape_ref=household_observation/v1` | `payload.village_ref` is in the caller's active geographic scope at *now*. |
| `shape_ref=conflict_detected/v1` | The flag's `subject_ref` references a household that is in the caller's active geographic scope (i.e., the subject's latest household_observation's `village_ref` is in scope). |
| (anything else) | Default-deny. Ship-1 emits no other event kinds on the wire. |

**Scope is evaluated at request time** (now), not at event time, for the
pull read side. On the push write side, `scope_violation` detection uses
event time (the capture's `timestamp`). This asymmetry is deliberate for
Ship-1; it may be revisited at Ship-3 when review workflows land.

Pagination: when the caller receives `len(events) == limit`, more events may
exist. The client should call again with `since_watermark = latest_watermark`.
The server over-fetches internally to fill the page after scope filtering, so
clients should not expect `latest_watermark == since_watermark + limit`.

### Pull errors

| Status | Body | Trigger |
|--------|------|---------|
| `400`  | `{ "error": "invalid_watermark" }` | `since_watermark` is negative or non-numeric. |
| `401`  | *(empty)* | Missing or invalid bearer token. |
| `500`  | `{ "error": "internal_error" }` | Unexpected server failure. |

---

## Server-emitted events

When the server emits a flag (integrity detection), the envelope it persists
and exposes on pull is fully formed per [envelope.schema.json](envelope.schema.json):

| Envelope field | Value on Ship-1 flags |
|---|---|
| `type` | `alert` |
| `shape_ref` | `conflict_detected/v1` |
| `actor_ref` | `{ "type": "actor", "id": "system:conflict_detector/<category>" }` (ADR-008 §S2) |
| `subject_ref` | Copied from the capture the flag is about. |
| `device_id` | `00000000-0000-0000-0000-000000000001` (reserved server device id). |
| `device_seq` | From PostgreSQL sequence `server_device_seq`. |
| `payload` | `{ category, resolvability, reason, source_event_id, related_event_ids?, related_subject_ids? }` |

Discrimination rule (F-A2, F-A4): any consumer logic that needs to recognise
an integrity flag **must branch on `shape_ref`**, not on `type`. Typing
integrity flags on `type` is a forbidden pattern.

---

## Invariants

1. **Events are never modified or deleted.** (ADR-001 §S1)
2. **Envelope is 11 fields, unchanged.** (ADR-001 §S3, ADR-008)
3. **Envelope `type` is closed at 6 values.** (ADR-007 §S1)
4. **Deduplication is by `id`.**
5. **`sync_watermark` is monotonic per insert** (PostgreSQL `BIGSERIAL`).
6. **Push is idempotent.** Same batch, same effect.
7. **Pull is always scope-filtered and bearer-authenticated.** No anonymous access.
8. **Accept-and-flag.** A validly-structured out-of-scope capture is accepted and flagged, never rejected. (ADR-006 §S1)

---

## Out of scope for Ship-1

These extensions are anticipated and intentionally not delivered:

- Partial-accept on push (per-event 400s instead of batch-level).
- Differential config delivery (`If-None-Match` / ETag / version diffs).
- Long-polling / streaming pull.
- Device-side flag creation push path.
- Alias chain expansion on pull (Ship-2; needed once merge/split lands).
- Subject-list projection endpoint (`/api/subjects`) — admin-only read surface,
  see `/admin/events` and `/admin/flags` for Ship-1.
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
| Concurrency detection | After persisting events (Tx1), the server evaluates each accepted event for concurrency conflicts (Tx2). For each event, `W_effective = min(event.sync_watermark, last_pull_watermark)`. If the event's subject has events from other devices with `sync_watermark > W_effective`, a `concurrent_state_change` flag is raised. CD failure does not affect event persistence. |

### Error Responses

| Status | Condition | Body |
|--------|-----------|------|
| `400` | One or more events fail schema validation | `{ "error": "validation_failed", "details": [...] }` |
| `400` | Empty events array | `{ "error": "empty_batch" }` |
| `500` | Server error | `{ "error": "internal_error" }` |

---

## Pull — `POST /api/sync/pull`

Device requests events it hasn't seen. Server returns events after the given watermark.

### Request

```json
{
  "since_watermark": 0,
  "limit": 100
}
```

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `since_watermark` | integer | yes | — | Return events with `sync_watermark > since_watermark`. Use `0` for first sync. |
| `limit` | integer | no | 100 | Max events to return. Server may cap this. |

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
  "latest_watermark": 42
}
```

### Semantics

| Aspect | Behavior |
|--------|----------|
| Ordering | Events ordered by `sync_watermark` ascending. |
| Pagination | If `len(events) == limit`, more events may exist. Client should pull again with `since_watermark = latest_watermark`. |
| Completion | If `len(events) < limit`, client has all available events. |
| `latest_watermark` | The highest `sync_watermark` in the returned batch. Client stores this for next pull. If no events returned, equals `since_watermark`. |
| Stability | Pulling the same watermark range returns the same events. Events are immutable and never deleted. |

### Error Responses

| Status | Condition | Body |
|--------|-----------|------|
| `400` | Missing or invalid `since_watermark` | `{ "error": "invalid_watermark" }` |
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
3. **`sync_watermark` is monotonically increasing** — no gaps, no reuse
4. **Push is idempotent** — safe to retry on network failure
5. **Pull is stable** — same query returns same results (append-only store)
