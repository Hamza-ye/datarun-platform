# Phase 2: Authorization & Multi-Actor

> An organization's people see and act on only what's relevant to their responsibilities. "A supervisor sees only their team's data. Sync delivers only authorized events."

**Exercises**: ADR-3 fully (10 positions: S1–S10). Contracts C6, C10. IG-4, IG-14 (deferred — see §7).

**Primitives built**: Scope Resolver (assignment-based access, scope-containment test, 3 scope types). Projection Engine extended (authority reconstruction from assignment timeline).

**Contracts exercised**: C6 (PE → SR: authority context as projection), C10 (SR → sync: scope-filtered payload).

---

## 1. What Phase 2 Proves

Phase 2 proves the platform can enforce organizational access boundaries — that different people see different data, that sync delivers only what's authorized, and that authorization violations are detected rather than silently allowed.

It validates:
- Assignment events define who can see and act on what (S1)
- Sync delivers exactly the authorized data set — no more, no less (S2)
- Authority is reconstructed from the assignment event timeline, not stored separately (S3)
- Authorization uses the original `subject_ref`, not post-merge identity (S4)
- Assignment creation enforces scope containment — no privilege escalation (S5)
- Authorization staleness is detected and flagged, not silently accepted (S7, S9)
- Scope changes adjust the sync payload at next sync (S10)

**What Phase 2 does NOT include**: No configuration authoring (Phase 3). No triggers, workflows, or auto-resolution (Phase 4). No Config Packager. Shapes are still hardcoded. No deployer-facing assignment configuration UI — assignments are managed through admin API. Per-actor sync sessions for shared devices (IG-14) are deferred — Phase 2 targets single-actor-per-device deployments.

---

## 2. What's Settled (Not Design Questions)

These points are decided by ADRs. The phase spec records them for implementer reference. None are open for re-litigation.

| Point | Source | Implication |
|---|---|---|
| Access = scope-containment test: `actor.assignment.scope ⊇ subject.location` | ADR-3 S1 | Every access check reduces to this single predicate |
| Sync scope = access scope — device gets exactly authorized data | ADR-3 S2 | No device-side policy engine. Device trusts its sync payload. |
| Authority context is a projection from assignment events — no envelope field, no separate store | ADR-3 S3 | PE reconstructs authority at query time. Retreat path: add `authority_context` to envelope if >50ms/event. |
| Authorization uses original `subject_ref`, not post-merge identity | ADR-3 S4 | Merge is identity resolution, not authorization grant |
| Assignment creation validated server-side: `new.scope ⊆ creator.scope` | ADR-3 S5 | Prevents privilege escalation. Online-only (same precedent as merge/split: ADR-2 S10). |
| Conflict resolution online-only | ADR-3 S6 | Extends ADR-2 S10 |
| Detect-before-act extends to auth flags. Blocking vs. informational is deployment-configurable. | ADR-3 S7 | Defaults: `scope_violation` informational, `role_stale` blocking for capability-restricted actions, `temporal_authority_expired` informational |
| Tiered projection: field workers local, supervisors hybrid, coordinators server-only | ADR-3 S8 | Phase 2 validates the field worker + supervisor tiers |
| Watermark-based staleness handling. `temporal_authority_expired` is `auto_eligible`. | ADR-3 S9 | Auto-resolution mechanism deferred to Phase 4 (Trigger Engine L3b). Flags accumulate as `auto_eligible` but resolve manually until then. See §7. |
| Selective-retain on scope contraction: own events kept, others' out-of-scope events purged | ADR-3 S10 | Device-side policy, not sync instruction |
| Event type `assignment_changed` already in vocabulary | ADR-4 S3 | No new event type. Assignment shapes go under this existing type. F2 safe. |
| 3 platform-fixed scope types: `geographic`, `subject_list`, `activity`. AND composition. | ADR-4 S7 | No deployer-authored scope logic. All non-null dimensions must pass. |
| Supervisor scope IS geographic containment | ADR-3 S1 | A supervisor sees all subjects in their geographic area — not an explicit list of subordinates. Their team's work is visible because the team operates within the supervisor's area. Not a design question. |

---

## 3. Design Decisions — ALL RESOLVED

4 implementation design decisions, all resolved before building. Each has an IDR recording the decision and rationale.

### DD-1: Assignment Event Payload Design — RESOLVED → [idr-013](../../decisions/idr-013-assignment-payload.md)

**Resolved choices:**

1. **No `assignment_id` in payload.** Assignment identity is `subject_ref.id` on the envelope. `assignment_ended` targets the same assignment by sharing `subject_ref.id`.
2. **Single geographic UUID per assignment.** Non-contiguous areas → separate assignments. OR composition across assignments, AND within.
3. **No `ended_by` in `assignment_ended`.** The envelope's `actor_ref` already records who ended it.
4. **`role` is opaque string** in Phase 2 (deployer vocabulary in Phase 3).
5. **`valid_to: null`** = indefinite (active until explicitly ended).
6. **Sync rule E9**: assignment events targeting the pulling actor are ALWAYS included in sync regardless of geographic scope. The device PE needs them to know its own scope.
7. **Flag detection ordering**: `temporal_authority_expired` checked BEFORE `scope_violation`. Prevents mis-classifying expired-but-unaware actors as scope violators.
8. **No `timestamp` usage for temporal detection**: `valid_to` expiry is checked against current time at push, not the advisory `timestamp` field (ADR-2 S3).

See finalized payloads in §9.

---

### DD-2: Geographic Hierarchy Storage — RESOLVED → [idr-014](../../decisions/idr-014-materialized-path-locations.md)

**Static reference data** in a `locations` table with **materialized path** column. Containment test: `subject.path LIKE :actor_scope_path || '%'` (btree prefix match with `text_pattern_ops`).

Key choices:
- Materialized path over recursive CTE (SQLite compatibility, simpler, faster)
- Static table over event-sourced (hierarchy rarely changes, corrections need mutability)
- `subject_locations` table carries denormalized `path` from `locations` for direct prefix match
- Zero Phase 3 dependency

See schema in §9.

---

### DD-3: Scope-Filtered Sync Query Strategy — RESOLVED → [idr-015](../../decisions/idr-015-scope-filtered-sync-query.md)

**Denormalized `location_path` column on events table** (Option C), resolved at write time from subject_locations. Combined with a small CTE for actor-assignment reconstruction.

Key choices:
- `location_path` is infrastructure metadata (same status as `sync_watermark` — server-managed, not in envelope)
- ADR-3 S4 (alias-respects-original-scope) means denormalized path is never stale for existing events
- Write-time resolution: one PK lookup per event (sub-millisecond)
- Pull query: watermark-ordered index scan with per-row LIKE filter. PostgreSQL stops at page_size matches.
- Three query categories: subject events (geo+activity+subject_list), own assignment events (always, E9), system events in scope
- Covering index `(sync_watermark) INCLUDE (location_path, type, activity_ref)` avoids heap fetches during filtering
- Expression index on `payload->'target_actor'->>'id'` for assignment event lookup

Rejected:
- **CTE-only** — recomputes full scope chain every pull. Wasteful at coordinator scale.
- **Materialized scope cache** — stale cache = security breach (ADR-3 S2 violation). Cache invalidation on 3 triggers is fragile for a security-critical path.

See indexes and query structure in §9.

---

### DD-4: Actor Identification for Sync Endpoints — RESOLVED → [idr-016](../../decisions/idr-016-actor-token-table.md)

**Simple `actor_tokens` table** for Phase 2. Server generates a random token per actor. Device sends `Authorization: Bearer <token>`. Server resolves token → actor_id.

Migration to Keycloak (Phase 2c or Phase 3) is clean — header convention unchanged, JWT validation replaces token-table lookup.

See schema in §9.

---

## 4. Reversibility Triage (retroactive)

Classified after Phase 2 completion. See [execution-plan.md §6.1](../execution-plan.md#61-reversibility-triage-required-step) for the framework.

| Decision | Bucket | Why | Implemented as | Evolve-later trigger |
|----------|--------|-----|----------------|---------------------|
| DD-1: Assignment event payload | **Lock** | Persisted as events, synced to devices, projected for authorization. Payload structure is permanent. | IDR-013. `subject_ref.id` as assignment identity. Single geographic UUID. | — |
| DD-2: Geographic hierarchy (materialized path) | **Lock** | Schema (`locations` + `subject_locations`). Query pattern for all scope containment. Mobile receives via sync. | IDR-014. `text_pattern_ops` btree prefix match. | — |
| DD-3: Scope-filtered sync query | **Lock** | Schema change — denormalized `location_path` on events table. Write-time resolution. Covering index. | IDR-015. Option C. | — |
| DD-4: Actor token table | **Lean** | Migration to Keycloak planned. Header convention (`Authorization: Bearer`) unchanged — swap is behind the interface. | IDR-016. Random token lookup. | Multi-deployment OR >50 actors |
| Auth flag detection rules | **Lean** | Code-only. Same CD pipeline. Phase 3 refines `role_stale` with capability tables. | All role changes flagged (conservative). | Phase 3 role-action tables (by design) |
| Flag severity defaults | **Lean** | Deployment-configurable. Code-only change. | `scope_violation` informational, `role_stale` blocking, `temporal_authority_expired` informational. | Deployer feedback after first deployment |
| Selective-retain purge | **Lean** | Device-side code. No protocol change. Interface is "own events kept, others' out-of-scope purged." | Journaled purge on scope contraction. | — |
| Admin assignment UI | **Leaf** | Thymeleaf. No contract. | CRUD forms for assignments + location browser. | — |

**Skill callouts**: DD-3 required **Database Optimizer** review — query plan validation for scope-filtered pull at supervisor scale, covering index design for the watermark scan with per-row LIKE filter.

---

## 5. Sub-Phases

Sub-phases are vertical slices, not horizontal layers. Each produces a testable system that exercises the full path from assignment creation through scope-filtered sync.

### Phase 2a: Assignment Model + Basic Scope

**Focus**: Assignment events, geographic hierarchy, single-actor scope-filtered sync. The narrowest vertical slice that proves "an actor sees only their data."

**Prerequisites**: DD-1, DD-2 resolved.

**Deliverables**:
1. `contracts/` additions:
   - `assignment_created` shape schema (under `assignment_changed` type)
   - `assignment_ended` shape schema (under `assignment_changed` type)
   - Sync protocol update: actor identification in pull request
2. Server `authorization/` module (new):
   - Assignment event processing: persist `assignment_changed` events
   - Assignment management API: `POST /api/assignments` (creates `assignment_created` event), `POST /api/assignments/{id}/end` (creates `assignment_ended` event)
   - Scope-containment validation on create (S5): `new.scope ⊆ creator.scope`, server-validated, online-only
   - Scope Resolver: given actor_id → compute active assignments → return scope predicate
3. Database migrations:
   - `locations` table (hierarchy reference data) with seed data
   - `actor_tokens` table (DD-4 Option A) for actor identification
4. Server Projection Engine extended:
   - Authority reconstruction: from assignment event timeline, compute active assignments for any actor at any point in time
   - Active assignments projection maintained incrementally on assignment events
5. Server sync extended:
   - Pull endpoint requires actor identification (token → actor_id)
   - Pull query filtered by actor's scope (DD-3 — may use CTE initially, optimize later)
   - Geographic containment: subject's location within actor's assigned area subtree
6. Location hierarchy admin API: `POST /api/locations`, `GET /api/locations/tree`
7. Admin view extended: assignment list, assignment creation form, location hierarchy browser

**Quality gates**:
- [ ] Create assignment for Actor A scoped to District X → Actor A's pull returns only events for subjects in District X
- [ ] Create assignment for Actor B scoped to Region Y (parent of District X) → Actor B's pull returns events for subjects across all districts in Region Y, including District X
- [ ] Actor with no active assignment → pull returns empty set
- [ ] Assignment creation where new scope exceeds creator's scope → rejected (S5)
- [ ] Create assignment → end assignment → create new assignment with different scope → pull reflects new scope
- [ ] Events from Phase 0/1 (pre-assignment) → visible to actors whose scope contains the subject's location
- [ ] Assignment events have valid 11-field envelope, type = `assignment_changed`, validate against schema
- [ ] Contract test C6: PE provides authority context (active assignments) to SR for scope evaluation
- [ ] Contract test C10: pull returns exactly authorized events — no more, no less

---

### Phase 2b: Multi-Actor Scope + Supervisor Visibility

**Focus**: Multiple actors with different scope levels. Supervisor sees their team's work. `activity` and `subject_list` scope types. Performance validation.

**Prerequisites**: Phase 2a complete. DD-3 resolved (or initial CTE approach from 2a validated under load).

**Deliverables**:
1. Scope Resolver extended:
   - `activity` scope type: filter events by `event.activity_ref ∈ actor.permitted_activities`
   - `subject_list` scope type: filter events by `event.subject_ref.id ∈ actor.explicit_list`
   - AND composition: all non-null scope dimensions must pass
2. Supervisor scope computation:
   - Supervisor's geographic scope contains their team's work areas (by hierarchy containment)
   - Pull for supervisor returns union of all events in their geographic scope — includes own work + team's work
   - No explicit "subordinate list" — geographic containment is the mechanism (settled, §2)
3. Sync query optimization (if DD-3 identified a need beyond initial CTE):
   - Implement chosen strategy (materialized cache or pre-tagged events)
   - Performance benchmark: supervisor pull for 10K events in a 3-level hierarchy
4. Mobile sync extended:
   - Pull sends actor token in header
   - Device receives only scope-filtered events
   - Mobile PE processes `assignment_changed` events to know local actor's scope
   - UI: "my assignments" visibility, subject list filtered to assigned scope
5. Multi-actor end-to-end scenario:
   - 3 actors: field worker A (Village X), field worker B (Village Y), supervisor C (District containing X+Y)
   - A captures → syncs → C pulls → sees A's data
   - B captures → syncs → C pulls → sees A+B's data
   - A pulls → sees only Village X data (not B's Village Y data)

**Quality gates**:
- [ ] Field worker's pull excludes events outside their geographic scope
- [ ] Supervisor's pull includes events from all subordinate areas within their scope
- [ ] Actor with `activity` scope restriction → pull includes only events with matching `activity_ref`
- [ ] Actor with compound scope (geographic + activity) → both dimensions filter (AND)
- [ ] Performance: supervisor pull for 1000 subjects across 3-level hierarchy completes < 5s
- [ ] Mobile receives only authorized events → Work List shows only in-scope subjects
- [ ] Mobile PE reconstructs local actor's scope from assignment events
- [ ] Projection equivalence: same scoped event set → server PE and device PE produce identical output

---

### Phase 2c: Authorization Flags + Sync Hardening

**Focus**: 3 new flag categories in the Conflict Detector pipeline. Selective-retain on mobile. Production-grade auth evaluation.

**Prerequisites**: Phase 2b complete. DD-4 Keycloak investigation complete (or decision to defer full auth).

**Deliverables**:
1. Conflict Detector extended — 3 new flag categories:
   - `scope_violation`: event's subject is outside actor's active scope at push time
   - `role_stale`: actor's role changed between event creation and push
   - `temporal_authority_expired`: assignment ended before event pushed, actor didn't know
   - Detection runs during push processing (same pipeline as Phase 1 CD — same request, separate transaction)
   - Designated resolver: broadest-scope actor whose scope contains the flagged event's subject (ADR-2 S11)
2. Authorization flag detection rules (specification):
   - `scope_violation`: at push time, resolve actor's active assignments → none contains event's `subject_ref` location → flag. If actor has zero active assignments → flag all events in batch.
   - `role_stale`: actor's current role differs from role at `event.sync_watermark` time → flag only if the role change restricts capabilities (role-action tables not yet available — Phase 2 flags all role changes; Phase 3 config refines to capability-restricted only)
   - `temporal_authority_expired`: actor's relevant assignment has an `assignment_ended` event with `sync_watermark` < event's `sync_watermark` → flag. Mark as `auto_eligible` (auto-resolution deferred to Phase 4).
3. Detect-before-act enforcement for auth flags (S7):
   - Auth-flagged events excluded from state derivation (same mechanism as identity flags)
   - Auth-flagged events do not trigger future policies (when Trigger Engine arrives in Phase 4)
   - Default severity: `scope_violation` informational, `role_stale` blocking, `temporal_authority_expired` informational
4. Selective-retain on scope contraction (mobile — S10):
   - When actor's scope contracts (new assignment with narrower scope), next sync delivers events only within new scope
   - Mobile detects out-of-scope events from other actors → purge candidates
   - Own events always retained (regardless of scope change)
   - Purge is journaled (crash-safe)
5. Auth mechanism hardening (scope depends on DD-4 resolution):
   - If Keycloak: OIDC integration, JWT validation on sync endpoints, token refresh, offline token caching
   - If deferred: document the gap, ensure token table has revocation capability, add HTTPS enforcement
6. Admin view extended: auth flag list (alongside identity flags), resolution interface for auth flags
7. `contracts/flag-catalog.md` updated: 6 flag categories (3 identity + 3 authorization)

**Quality gates**:
- [ ] Actor captures event for out-of-scope subject → pushes → `scope_violation` flag raised
- [ ] Actor's assignment ends while offline → continues working → pushes → `temporal_authority_expired` flags on all events created after assignment end
- [ ] `temporal_authority_expired` flag carries `auto_eligible` resolvability
- [ ] Actor's role changes → pushes events created under old role → `role_stale` flag raised
- [ ] Auth-flagged events visible in timeline but excluded from state derivation (same as identity flags)
- [ ] Scope contracts → mobile pulls → out-of-scope events from other actors purged → own events retained
- [ ] Sync endpoints reject requests without valid actor identification (401)
- [ ] All 9 acceptance criteria (§8) pass
- [ ] Contract test C10 under flag conditions: flagged events still delivered (accept-and-flag), but flagged

---

## 6. Scope by Module

**contracts/**

- `shapes/assignment_created.schema.json` — assignment creation payload
- `shapes/assignment_ended.schema.json` — assignment termination payload
- `sync-protocol.md` updated — pull request requires actor identification
- `flag-catalog.md` updated — 6 categories (add `scope_violation`, `role_stale`, `temporal_authority_expired`)

**server/authorization/** (new)

- Scope Resolver: `ScopeResolver.isContained(assignment, subjectRef)` — the single scope-containment predicate (S1)
- Geographic containment: recursive location hierarchy query
- Subject-list containment: set membership
- Activity containment: set membership against `activity_ref`
- AND composition: all non-null dimensions must pass
- Assignment management: API endpoints for create/end, scope-containment validation (S5), online-only
- Active assignments projection: PE-derived, maintained incrementally

**server/core/** (extended)

- Projection Engine extended:
  - Authority reconstruction: given actor_id → active assignments at time T (from assignment event timeline)
  - Assignment events processed through standard PE pipeline (they're just events with `type: assignment_changed`)

**server/integrity/** (extended)

- Conflict Detector extended:
  - `scope_violation` detection: actor's scope at push time vs. event's subject location
  - `role_stale` detection: actor's role at event creation vs. role at push time
  - `temporal_authority_expired` detection: assignment ended before event pushed
  - Same pipeline architecture as Phase 1 (same request, separate transaction, deterministic flag IDs, sweep job)

**server/sync/** (extended)

- Pull: resolve actor from token → compute scope → filter events by scope → return paginated
- Scope filter integrated into pull query (CTE or chosen strategy from DD-3)
- Push: after event persistence + identity CD, run authorization CD checks

**server/admin/** (extended)

- Assignment management UI: create/end assignments, view active assignments per actor
- Location hierarchy browser: view/edit hierarchy
- Auth flag resolution UI (same pattern as identity flags)

**mobile/data/** (extended)

- Sync Service: sends actor token in pull request header
- PE extended: processes `assignment_changed` events → maintains local actor's scope knowledge
- Selective-retain: on scope contraction, identify and purge out-of-scope non-own events
- SQLite migration: `locations` table (received via sync as reference data)

**mobile/presentation/** (extended)

- Work List: filtered to subjects within local actor's scope
- Assignment info: "my assignments" display
- Scope-change indicator: visual feedback when scope changes at sync

---

## 7. Deferred Items

Items considered for Phase 2 but explicitly excluded. Each has a reason.

| Item | Why deferred | When |
|---|---|---|
| **Auto-resolution for `temporal_authority_expired`** | Auto-resolution is a Trigger Engine L3b sub-type (ADR-5 S9). The Trigger Engine is a Phase 4 primitive. Implementing it in Phase 2 would pull forward Phase 4 machinery for a single use case. Instead: flags accumulate as `auto_eligible`, resolved manually. The auto-resolution policy is the first thing Phase 4 implements. **Risk accepted**: manual resolution burden for `temporal_authority_expired` flags in deployments with frequent assignment changes. Revisit if >20% of Phase 2 flags are `temporal_authority_expired`. | Phase 4 |
| **Per-actor sync sessions (IG-14)** | Prevents watermark corruption on shared devices. Phase 2's goal is scope-filtered sync for single-actor devices. Shared-device support is an optimization. Including it risks scope creep. | Post-Phase 2 or Phase 3 |
| **Role-action permission tables** | ADR-4 defers to deployment configuration. Phase 2 treats `role` as an opaque label. Role-action permissions require the configuration pipeline (Phase 3). | Phase 3 |
| **Auditor access (cross-hierarchy, read-only)** | Boundary O-6: existing mechanism (broad scope assignment + read-only role). Needs role-action tables to restrict to read-only. | Phase 3 |
| **Full Keycloak integration** | May be deferred from 2c if simple token approach proves sufficient for testing. Keycloak adds operational complexity (deployment, token refresh, offline caching). Decision point: end of 2b. | Phase 2c or Phase 3 |

---

## 8. Acceptance Criteria

Phase 2 is complete when all of the following hold:

1. **Scope-filtered sync**: each actor's pull returns exactly the events their active assignments authorize — verified with 3+ actors at different scope levels
2. **Supervisor visibility**: supervisor pull returns events from all subordinate areas within their geographic scope
3. **Assignment lifecycle**: assignments can be created and ended via events, PE reconstructs authority correctly across the timeline
4. **Scope-containment enforcement**: assignment creation with scope exceeding creator's scope is rejected (S5)
5. **Authorization flags**: all 3 categories (`scope_violation`, `role_stale`, `temporal_authority_expired`) detected during push processing, with correct resolvability classification
6. **Selective-retain**: mobile purges out-of-scope non-own events on scope contraction, retains own events
7. **No regression**: all Phase 0 and Phase 1 tests pass. Identity flags still work. Projection equivalence holds.
8. **Actor identification**: sync endpoints reject unauthenticated requests
9. **Performance**: supervisor sync for 1000 subjects across 3-level hierarchy < 5s. Authority reconstruction < 50ms/event.

---

## 9. Technical Specifications

### Event Shapes for Assignments

Both shapes use event type `assignment_changed` (ADR-4 S3). This is NOT a new event type — it's an existing type getting its first shapes.

**Envelope for assignment events**:

| Field | `assignment_created` | `assignment_ended` |
|-------|---------------------|-------------------|
| `id` | Server-generated UUID | Server-generated UUID |
| `type` | `assignment_changed` | `assignment_changed` |
| `shape_ref` | `assignment_created/v1` | `assignment_ended/v1` |
| `activity_ref` | `null` | `null` |
| `subject_ref` | `{type: "assignment", id: <assignment_uuid>}` | `{type: "assignment", id: <assignment_uuid>}` |
| `actor_ref` | `{type: "actor", id: <coordinator>}` | `{type: "actor", id: <coordinator>}` |
| `device_id` | Server `device_id` | Server `device_id` |
| `device_seq` | Server's next seq | Server's next seq |
| `sync_watermark` | Assigned on insert | Assigned on insert |
| `timestamp` | Server clock | Server clock |
| `payload` | See below | See below |

Note: `subject_ref` uses identity type `assignment` (ADR-2: 4 typed categories). The assignment itself IS the subject of these events.

**`assignment_created` payload** (finalized — IDR-013):
```json
{
  "target_actor": {"type": "actor", "id": "uuid"},
  "role": "string",
  "scope": {
    "geographic": "uuid | null",
    "subject_list": ["uuid"] | null,
    "activity": ["string"] | null
  },
  "valid_from": "ISO-8601 datetime",
  "valid_to": "ISO-8601 datetime | null"
}
```

**`assignment_ended` payload** (finalized — IDR-013):
```json
{
  "reason": "string | null"
}
```

Note: No `assignment_id` — the assignment's identity is `subject_ref.id` on the envelope. No `ended_by` — the `actor_ref` on the envelope records who ended it.

### Database Schema Additions

**Migration V4** (Phase 2a):
```sql
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
```

### Mobile SQLite Schema Additions

**Version 3** (Phase 2b):
```sql
-- Location hierarchy (received via sync as reference data)
CREATE TABLE locations (
    id        TEXT PRIMARY KEY,
    name      TEXT NOT NULL,
    parent_id TEXT,
    level     TEXT NOT NULL,
    path      TEXT
);

-- Local actor scope (maintained by PE from assignment events)
CREATE TABLE local_assignments (
    assignment_id TEXT PRIMARY KEY,
    role          TEXT NOT NULL,
    geo_scope     TEXT,          -- location UUID
    subject_list  TEXT,          -- JSON array of UUIDs
    activity_list TEXT,          -- JSON array of activity_refs
    valid_from    TEXT NOT NULL,
    valid_to      TEXT,
    ended         INTEGER NOT NULL DEFAULT 0
);
```

### Sync Protocol Extension

**Pull** — `POST /api/sync/pull`

```
Request:  { last_watermark: N }
Headers:  Authorization: Bearer <actor_token>

Response: { events: [Event, ...], watermark: N, has_more: boolean }
```

Server resolves token → actor_id → active assignments → scope filter → return matching events.

**Sync rule E9**: Assignment events targeting the pulling actor (`payload->'target_actor'->>'id' = :actor_id`) are ALWAYS included regardless of geographic scope. The device PE needs them to know its own scope.

**Push** — `POST /api/sync/push` (extended)

```
Request:  { events: [...], device_id: "uuid", last_pull_watermark: N }
Headers:  Authorization: Bearer <actor_token>

Response: { accepted: N, duplicates: N, flags_raised: N }
```

Authorization CD runs after identity CD. Same pipeline, additional flag categories.

### Authorization Flag Detection (Specification)

Detection runs during push processing, after event persistence, in the same CD transaction as identity checks.

**Detection ordering**: `temporal_authority_expired` → `scope_violation` → `role_stale`. If an assignment expired and the actor pushed afterward (not knowing about the expiry), the flag is `temporal_authority_expired` (auto_eligible), not `scope_violation` (manual_only). Checking scope first would mis-classify because the expired assignment is no longer active.

| Flag | Input | Condition | Resolvability | Resolver |
|---|---|---|---|---|
| `scope_violation` | Event's `subject_ref` + actor's active assignments at push time | No active assignment's scope contains the event's subject location | `manual_only` | Broadest-scope actor containing the subject |
| `role_stale` | Actor's role at `event.sync_watermark` vs. current role | Role differs AND (Phase 2: always flag; Phase 3: only if capability-restricted) | `manual_only` | Actor's current supervisor |
| `temporal_authority_expired` | Assignment timeline for actor | Relevant assignment has `assignment_ended` with watermark between event creation and push, OR assignment's `valid_to` is past at push time | `auto_eligible` | Assigning coordinator |

**`conflict_detected` payload for auth flags** (same schema as Phase 1, new categories):
```json
{
  "source_event_id": "uuid",
  "flag_category": "scope_violation | role_stale | temporal_authority_expired",
  "resolvability": "manual_only | auto_eligible",
  "designated_resolver": {"type": "actor", "id": "uuid"},
  "context": {
    "actor_scope_at_push": "...",
    "assignment_id": "uuid",
    "reason": "string"
  }
}
```

---

## 10. Risks

| Risk | Mitigation | Revisit trigger |
|---|---|---|
| Authority reconstruction slow at scale | Per-actor assignment cache (server-side HashMap, refreshed on assignment events). Escape hatch: add `authority_context` to envelope (ADR-1 S5). | >50ms/event attributable to authority reconstruction |
| Scope-filtered sync query too slow for supervisors | DD-3 investigates. CTE may suffice for Phase 2 scale. Materialized scope table is the retreat. | Supervisor sync >5s for 1000 subjects |
| Manual resolution of `temporal_authority_expired` becomes burdensome | Monitor flag volume. Accelerate Phase 4 (auto-resolution) if needed. | >20% of all flags are `temporal_authority_expired` |
| Simple token auth insufficient for field testing | Upgrade to Keycloak earlier than planned. Token table supports revocation. | Security review flags unacceptable exposure |
| Subject-location mapping maintenance burden | Phase 3 config can auto-populate from shape fields. Phase 2: admin API. | >10% of subjects have stale/missing location |

---

## 11. Journal

**[2026-04-19] Phase 2a — Assignment Model + Basic Scope — COMPLETE**

51 total tests green (44 prior + 7 Phase 2a scope-filtered sync quality gates).

**Deliverables shipped:**
- `contracts/shapes/assignment_created.schema.json`, `assignment_ended.schema.json` — JSON Schema Draft 2020-12 payloads under existing `assignment_changed` type.
- `server/resources/db/migration/V4__authorization_tables.sql` — `locations` (materialized path), `actor_tokens`, `subject_locations`, `events.location_path` column, covering index `idx_events_scoped_pull`, expression index on assignment payload.
- `server/authorization/` — 12 source files: AssignmentService, ScopeResolver, ActiveAssignment, ActorTokenInterceptor/Repository/Controller, LocationRepository/Controller, SubjectLocationRepository, WebConfig, Location record.
- `server/event/EventRepository` extended — `resolveLocationPath()` denormalizes on insert, `findSinceScoped()` 3-category OR query (geo subjects + own assignments + system events), `findByType()`.
- `server/sync/SyncController` extended — token auth via interceptor (pull only), scope-filtered pull with unrestricted-geo fallback.
- `server/subject/SubjectProjection` — excludes `assignment_changed` from subject projection (bug found during integration).
- Admin templates: `assignment-list.html`, `assignment-create.html`, `location-list.html`. Nav links added to `subject-list.html`, `flag-list.html`.
- `server/test/authorization/ScopeFilteredSyncIntegrationTest` — 7 quality gate tests.

**Bugs found and fixed:**
1. **PGobject→String cast**: ScopeResolver's `queryForList` returns JSONB as PGobject, not String. Silent swallow in catch block masked the failure. Fixed: `.toString()` instead of String cast. Added warn-level logging to the catch.
2. **SubjectProjection counting assignment events**: The projection CTE's exclusion list (`NOT IN (...)`) didn't include `assignment_changed`, creating phantom subjects. Fixed: added to exclusion list.
3. **Unrestricted-geo empty pull**: Actors with null geographic scope (admin/root) got empty pull because `scopePaths` list was empty after filtering nulls, causing `findSinceScoped` to return only assignment events. Fixed: `hasUnrestrictedGeo` flag falls back to unscoped `findSince`.

**Test infrastructure decision:**
- Assignment seed event uses explicit `sync_watermark = 0` so it doesn't appear in pull results (`> 0`), preserving all existing test count assertions. ScopeResolver still finds it via type-based query.

**All 4 design decisions were pre-resolved (IDR-013 through IDR-016). No new IDRs created during implementation.**

**Quality gates verified:**
- [x] Actor scoped to District X → pull returns only District X events
- [x] Actor scoped to Region Y (parent) → pull returns events from all districts in Y
- [x] Actor with no active assignment → pull returns empty set
- [x] Assignment scope exceeding creator's scope → rejected (S5)
- [x] Create → end → new scope → pull reflects new scope
- [x] Assignment events have valid 11-field envelope
- [x] Contract C6: PE provides authority context to SR
- [x] Contract C10: pull returns exactly authorized events
- [x] No regression: all 44 Phase 0+1 tests pass

**[2026-04-19] Phase 2b — Multi-Actor Scope + Supervisor Visibility — COMPLETE**

80 total tests green: 58 server (51 prior + 7 Phase 2b), 22 mobile (20 prior + 2 Phase 2b).

**Deliverables shipped:**

Server:
- `server/sync/SyncController` extended — post-query filtering for `activity` and `subject_list` scope types. Geographic filtering remains at SQL level (most selective dimension); activity and subject_list filter in Java using `ActiveAssignment.containsActivity()` / `containsSubject()`. AND within assignment, OR across assignments.
- `server/test/authorization/MultiActorScopeIntegrationTest` — 7 quality gate tests covering the full Phase 2b scope.

Mobile:
- `mobile/data/device_identity.dart` — `actorToken` getter + `setActorToken()` for token storage via SharedPreferences.
- `mobile/data/sync_service.dart` — pull sends `Authorization: Bearer <token>` header. 401 handled. Processes `assignment_changed` events during pull via `EventStore.processAssignmentEvent()`.
- `mobile/data/event_store.dart` — DB version 3: `local_assignments` table. `processAssignmentEvent()` (created/ended). `getActiveAssignments()`.
- `mobile/data/projection_engine.dart` — `assignment_changed` added to `_isSystemEventType()` exclusion.
- `mobile/presentation/app_state.dart` — exposes `activeAssignments` list.
- `mobile/presentation/screens/work_list_screen.dart` — app bar shows current role(s).
- `mobile/test/projection_engine_test.dart` — 2 new tests: assignment exclusion + processAssignmentEvent lifecycle.

**Design approach — activity/subject_list filtering:**
Post-query Java filtering rather than extending SQL. `findSinceScoped` already filters geographically (most selective). Post-filter uses `containsActivity()` + `containsSubject()` directly on each assignment with OR-across/AND-within.

**No bugs found. No new IDRs.**

**Quality gates verified:**
- [x] Field worker's pull excludes events outside their geographic scope
- [x] Supervisor's pull includes events from all subordinate areas within their scope
- [x] Actor with `activity` scope restriction → pull includes only events with matching `activity_ref`
- [x] Actor with compound scope (geographic + activity) → both dimensions filter (AND)
- [x] Performance: supervisor pull for 100 subjects across 3-level hierarchy well within 5s budget
- [x] Mobile receives only authorized events → Work List shows only in-scope subjects
- [x] Mobile PE reconstructs local actor's scope from assignment events
- [x] Projection equivalence: scoped event set consistent between server and device PE
- [x] No regression: all 51 Phase 0+1+2a server tests pass, all 20 prior mobile tests pass

**[2026-04-19] Phase 2c — Auth Flags + Sync Hardening — COMPLETE**

90 total tests green: 64 server (58 prior + 6 Phase 2c), 26 mobile (22 prior + 4 Phase 2c).

**Deliverables shipped:**

Server:
- `server/integrity/ConflictDetector` extended — `evaluateAuth()` method (~120 lines). Three flag categories in spec-mandated detection order: `temporal_authority_expired` → `scope_violation` → `role_stale`. Constants for all three categories. Helper methods: `getAssignmentEndedWatermark()`, `findRoleAtWatermark()`, `findBroadestScopeActor()`, `findSupervisorActor()`, `isAssignmentEventType()`. Actors with no assignment history skip auth CD entirely (prevents false positives for pre-assignment actors and test scenarios).
- `server/sync/SyncController` extended — push pipeline calls `evaluateAuth()` after identity CD in Tx2. `extractActorId()` helper finds first non-"system" actor_ref UUID from batch. Auth CD errors caught and logged as warnings (non-fatal, same pattern as identity CD).
- `server/authorization/ScopeResolver` extended — `getAllAssignments(UUID actorId)` returns ALL assignments including ended ones (needed for temporal_authority_expired detection).
- `server/resources/templates/flag-list.html` — 3 new CSS classes (`.category-scope` pink, `.category-temporal` orange, `.category-role` teal). `th:classappend` updated for all 6 flag categories.
- `server/test/integrity/AuthFlagIntegrationTest` — 6 quality gate tests: scope_violation, temporal_authority_expired, auto_eligible resolvability, flagged-event state exclusion, temporal-before-scope ordering, role_stale detection.

Mobile:
- `mobile/data/event_store.dart` — `purgeOutOfScopeEvents(String ownDeviceId)`: queries non-own-device non-system events, extracts subject_id from JSON subject_ref, checks against active assignment subject_lists, batch deletes out-of-scope events.
- `mobile/data/sync_service.dart` — calls `purgeOutOfScopeEvents()` after pull phase when pulled > 0.
- `mobile/test/selective_retain_test.dart` — 4 tests: no-assignment keeps all, own-device never purged, out-of-scope other-device purged, system events never purged.

Contracts:
- `contracts/flag-catalog.md` — documents all 6 flag categories (3 identity: `concurrency`, `stale_reference`, `identity_conflict`; 3 authorization: `scope_violation`, `temporal_authority_expired`, `role_stale`) with detection ordering and resolvability classification.

**Bugs found and fixed:**
1. **MultiDeviceE2ETest regression**: After adding `evaluateAuth`, test actors without assignments got `scope_violation` flags. Root cause: auth CD ran on all actors including those with no assignment history. Fix: skip auth CD entirely when `allAssignments.isEmpty()`.
2. **UUID from JSON extraction**: `queryForList(..., UUID.class, ...)` fails because PostgreSQL JSON extraction returns text. Fix: use `String.class` and `UUID.fromString()`.
3. **DISTINCT + ORDER BY conflict**: PostgreSQL requires ORDER BY expressions in SELECT list when using DISTINCT. Fix: wrap in subquery (`SELECT ... FROM (SELECT DISTINCT ..., LENGTH(l.path) AS path_len ...) sub ORDER BY path_len`).

**No new IDRs.**

**Quality gates verified:**
- [x] Out-of-scope event → `scope_violation` flag raised
- [x] Assignment ends offline → `temporal_authority_expired` flags on subsequent events
- [x] `temporal_authority_expired` carries `auto_eligible` resolvability
- [x] Role change → events from old-role window detectable as `role_stale`
- [x] Auth-flagged events excluded from state derivation
- [x] Selective-retain: mobile purges out-of-scope non-own events, retains own events
- [x] Sync endpoints reject requests without valid actor identification (401) — verified in Phase 2b
- [x] Temporal checked before scope — prevents mis-classification
- [x] No regression: all 58 Phase 0+1+2a+2b server tests pass, all 22 prior mobile tests pass

---

**Phase 2 — COMPLETE.** 90 tests (64 server + 26 mobile). All 9 acceptance criteria (§8) verified. All 3 sub-phases shipped. 4 IDRs created (IDR-013 through IDR-016). 0 regressions.
