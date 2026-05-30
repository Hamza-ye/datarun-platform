# Implementation Decision Alignment Review

> Status: Review artifact  
> Authority checked against: `canonical-decision-ledger.md`  
> Reviewed scope: uploaded phase plans and IDRs listed in `Pasted markdown.md`, plus `canonical-decision-ledger.md`.
>
> Date: 2026-05-29

## 1. Review rule

`canonical-decision-ledger.md` is the only architectural authority for this review.

Old ADR references inside phase and IDR files are treated as stale provenance labels. They are not used to decide alignment. The review checks what the implementation decision actually says against the canonical ledger.

## 2. Inputs reviewed

Phase files:
at: `docs/implementation/phases/`

```text
phase-0.md
phase-1.md
phase-2.md
phase-3.md
phase-3d.md
phase-3e.md
phase-4.md
```

Implementation decision records:
at: `docs/decisions/`

```text
idr-008-server-event-producer.md
idr-009-alias-table.md
idr-010-conflict-detection-intercept.md
idr-011-identity-conflict-scope.md
idr-013-assignment-payload.md
idr-014-materialized-path-locations.md
idr-015-scope-filtered-sync-query.md
idr-017-shape-storage.md
idr-018-expression-grammar.md
idr-019-config-package.md
idr-020-pattern-state-machine-representation.md
idr-021-role-action-enforcement-model.md
idr-022-flag-severity-and-domain-uniqueness.md
idr-023-role-action-domain-boundary-and-assignment-administration.md
idr-024-multi-axis-assignment-containment.md
idr-025-pattern-definition-contract-and-delivery.md
idr-026-conflict-resolver-routing-and-single-writer-resolution.md
```

## 3. Executive verdict

The implementation decisions are broadly aligned with the canonical architecture.

No reviewed IDR reopens the event envelope, adds an illegal event type, makes workflow state canonical, lets deployers author state machines, syncs unauthorized data as normal behavior, or rejects valid state-stale events as the primary anomaly strategy.

The review found three material follow-up items:

1. `CDL-054` is missing two flag categories that implementation now treats as active: `temporal_authority_expired` and `role_stale`.
2. `CDL-015` over-specifies the deterministic flag UUID algorithm as UUIDv5, while the implementation documentation says the current implementation uses `UUID.nameUUIDFromBytes` with a stable salt. The architecture needs either a wording correction or a code migration.
3. `idr-010-conflict-detection-intercept.md` still states the old flag-id input `(source_event_id + flag_category)`. Later phase documentation records the corrected input `(source_event_id + shape_ref + flag_category)`. `idr-010` should be updated or marked superseded for that one line.

The review also found two conditional-alignment boundaries:

1. `events.location_path` is aligned only if treated as immutable write-time infrastructure metadata for each event. Location reparenting must not rewrite historical event `location_path` values.
2. Resolver routing is aligned only if every newly emitted flag carries `designated_resolver` at runtime and canonical resolution rejects non-designated authors, even if the JSON schema did not initially require the field.

## 4. Pass A — implementation decision inventory

| File          | Decision surface                                                                                                           |                                       Permanent surface touched | Initial risk                                                           |
| ------------- | -------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------: | ---------------------------------------------------------------------- |
| `phase-0.md`  | Core event loop: offline capture, local/server event stores, idempotent sync, basic projection.                            |                           Envelope, event store, sync protocol. | Low; aligns with canonical base.                                       |
| `phase-1.md`  | Conflict detection, identity resolver, alias projection, flag exclusion, resolver flow.                                    |              Flags, identity shapes, sync request, projections. | Medium; corrected by Phase 3e and Phase 4 resolver hardening.          |
| `phase-2.md`  | Assignment payload, geographic scope, scope-filtered sync, selective retention.                                            |  Assignment events, scope sync query, authorization projection. | Medium; `location_path` and temporal/role flags need boundary clarity. |
| `phase-3.md`  | Shape storage, expression grammar, config package, config validation.                                                      |          Config DB, config package, shape/expression contracts. | Low; bounded and deploy-time validated.                                |
| `phase-3d.md` | Close-out: `activity_ref`, sensitivity package surface, `context.*` resolver.                                              |                     Event stamping and config package surfaces. | Low; fills canonical surfaces.                                         |
| `phase-3e.md` | Envelope type vocabulary retrofit and platform-bundled shape registration.                                                 | Envelope schema, bundled shapes, consumer predicates, flag IDs. | Low after retrofit; critical correction pass.                          |
| `phase-4.md`  | Role-action, severity, uniqueness, pattern registry, pattern projection, transition flags, backfill, resolver enforcement. |                    Workflow/config/flag/authorization behavior. | Medium; mostly aligned with explicit boundaries.                       |
| `idr-008`     | Server as event producer.                                                                                                  |                  Server `device_id`, `device_seq`, `actor_ref`. | Low.                                                                   |
| `idr-009`     | Alias table with eager transitive closure.                                                                                 |                      Alias projection table, merge transaction. | Low.                                                                   |
| `idr-010`     | Conflict detection two-transaction intercept.                                                                              |                        Push pipeline and deterministic flag ID. | Medium; stale flag ID wording.                                         |
| `idr-011`     | Manual-only identity conflict detection in Phase 1.                                                                        |                                                 Detector scope. | Low.                                                                   |
| `idr-013`     | Assignment event payload.                                                                                                  |             Platform-bundled assignment shapes and scope model. | Medium; active flag categories need canonical ledger update.           |
| `idr-014`     | Static materialized-path locations.                                                                                        |           Location reference data and subject-location mapping. | Medium; historical scope immutability boundary needed.                 |
| `idr-015`     | Denormalized `location_path` on events for scoped pull.                                                                    |               Event table infrastructure column and sync query. | Medium; aligned if not treated as envelope or mutable history.         |
| `idr-017`     | Shape storage as versioned snapshots; L1/L2 split.                                                                         |                          Shape schema/config DB/mobile package. | Low.                                                                   |
| `idr-018`     | JSON AST expression grammar.                                                                                               |                  Expression DB/config package/mobile evaluator. | Low.                                                                   |
| `idr-019`     | Atomic config package via dedicated endpoint.                                                                              |                       Config package and sync config discovery. | Low.                                                                   |
| `idr-020`     | Pattern state-machine representation.                                                                                      |           Pattern binding config, projection, transition flags. | Low/medium; aligned by construction.                                   |
| `idr-021`     | Role-action enforcement model.                                                                                             |                   Activity role-action config and `role_stale`. | Medium; `role_stale` missing from `CDL-054`.                           |
| `idr-022`     | Flag severity and domain uniqueness.                                                                                       |                  Flag config, uniqueness constraints, detector. | Medium; categories need ledger completion.                             |
| `idr-023`     | Role-action boundary and assignment administration.                                                                        |                 Activity roles and assignment command boundary. | Low; closes ambiguity.                                                 |
| `idr-024`     | Multi-axis assignment containment.                                                                                         |                               Assignment command authorization. | Low/medium; security-sensitive but aligned.                            |
| `idr-025`     | Pattern definition contract and delivery.                                                                                  |               Pattern definition contract/package/mobile store. | Low.                                                                   |
| `idr-026`     | Resolver routing and single-writer resolution.                                                                             |                Flag payload semantics and resolution authority. | Medium; aligned if runtime-enforced.                                   |

## 5. Pass B — canonical constraint gates

| Gate | Verdict | Evidence from implementation review | Required action |
|---|---|---|---|
| G1. Event stream remains canonical; projections rebuildable. | Pass | Alias table, pattern state, projections, and workflow state are described as derived/rebuildable. | None. |
| G2. Valid state-stale events are accepted and flagged. | Pass | Conflict, auth role-action, domain uniqueness, and transition violations all use accept-and-flag. | None. |
| G3. Envelope remains exactly 11 conceptual fields. | Pass | Phase 3e and Phase 4 explicitly avoid new fields. `location_path` is stated as infrastructure metadata, not envelope. | Keep `location_path` out of API/envelope contracts. |
| G4. Envelope `type` remains exactly six values. | Pass after Phase 3e | Phase 3e removes identity/integrity fact names from the type enum and rewrites consumers to `shape_ref`. | Keep old phase prose from reintroducing type names. |
| G5. Domain facts use `shape_ref`, not `type`. | Pass after Phase 3e | Identity, conflict, resolution, merge, and split facts are shape-keyed. | None. |
| G6. Reference field != referent. | Pass | Assignment identity uses `subject_ref.id`; activity instance and `activity_ref` are separated in later phases. | None. |
| G7. Authority remains projection-derived; no `authority_context`. | Pass | Role-action reconstructs assignment authority at knowledge horizon; device checks are advisory. | None. |
| G8. Sync scope equals access scope; no sync-all-hide-later. | Pass with boundary | Normal pull remains request-time scoped. Subject-history backfill is separate and authorized per request/page. | Keep backfill separate from normal live sync. |
| G9. Scope mechanism platform-fixed; instances configured. | Pass | IDR-013/024 keep axes fixed: `geographic`, `subject_list`, `activity`. | None. |
| G10. Configuration bounded; no deployer-authored code/state machines. | Pass | Expression grammar is bounded; deployers bind platform patterns but do not define transitions. | None. |
| G11. Workflow state projection-derived. | Pass | Pattern state derives from events + pattern bindings; no durable authority table initially. | None. |
| G12. Flags/resolution preserve single-writer semantics. | Pass with condition | IDR-026 adds `designated_resolver` and exact resolver equality. | Ensure schema/runtime/tests enforce this for every new flag. |

## 6. Pass C — IDR-by-IDR findings

### `idr-008-server-event-producer.md`

Verdict: **Aligned**.

The server event producer model uses persistent server `device_id`, monotonic server `device_seq`, normal pull delivery, and `system:{source_type}/{source_id}` authorship. This matches canonical causal metadata and actor reference rules.

Required action: none.

### `idr-009-alias-table.md`

Verdict: **Aligned**.

The alias table is explicitly a rebuildable projection from `subjects_merged/v1` events. It preserves historical references, uses `type = capture` + `shape_ref = subjects_merged/v1`, and avoids a separate lifecycle authority table.

Required action: none.

### `idr-010-conflict-detection-intercept.md`

Verdict: **Aligned behavior, stale document line**.

The two-transaction push pipeline aligns with accept-and-flag because event persistence cannot be rolled back by detector failure. The sweep job is also aligned as an algorithmic repair path.

The stale line is deterministic flag ID derivation. The IDR says `(source_event_id + flag_category)`. Later phase documentation and canonical ledger require `source_event_id + shape_ref + flag_category`.

Required action: update `idr-010` or mark it superseded for deterministic flag ID derivation only.

### `idr-011-identity-conflict-scope.md`

Verdict: **Aligned**.

Manual-only duplicate-subject detection is a safe initial detector scope. The pipeline remains the same flag representation, so later auto-detection can be additive.

Required action: none.

### `idr-013-assignment-payload.md`

Verdict: **Aligned with canonical ledger amendment needed**.

Assignment identity via envelope `subject_ref.id` is aligned with typed reference contracts. Opaque roles and three-axis scope are aligned with assignment-based access and platform-fixed scope axes. Temporal detection avoiding `device_time` is aligned.

The implementation uses `temporal_authority_expired` as an active flag category and treats it as `auto_eligible`. The canonical ledger does not currently list this category in `CDL-054`.

Required action: add `temporal_authority_expired` to canonical flag resolvability classification.

### `idr-014-materialized-path-locations.md`

Verdict: **Conditionally aligned**.

Static `locations` reference data with materialized path is an implementation choice for the `geographic` scope mechanism. It does not violate the event model if treated as deployment/reference data, not operational event history.

Boundary risk: the IDR says paths are recomputed if a location is reparented. That is safe for `locations` and `subject_locations` reference state, but historical event authorization must not be reinterpreted by rewriting past event scope metadata.

Required action: add an explicit rule to IDR-014/015: reparenting must not rewrite `events.location_path` for historical events. Future events may use the new path; historical events retain the write-time scope context unless a separate audited migration decision says otherwise.

### `idr-015-scope-filtered-sync-query.md`

Verdict: **Conditionally aligned**.

`location_path` on the events table is acceptable as server-managed infrastructure metadata, not an envelope field. It supports sync scope = access scope and preserves the original-subject-scope rule if set at write time.

Boundary risk: the column is permanent stored metadata. It must stay subordinate to the event stream and must not become a mutable authorization reinterpretation surface.

Required action: document `events.location_path` as immutable after event insert except controlled backfill for events that were inserted before any subject location existed.

### `idr-017-shape-storage.md`

Verdict: **Aligned**.

Versioned shape snapshots, all versions valid forever, shape-level sensitivity, `subject_binding`, L1/L2 separation, and external expression rules all fit the configuration boundary.

Required action: none for architecture. The 10-type field vocabulary is a platform specification detail and should live in specs/IDRs, not necessarily the canonical architecture ledger.

### `idr-018-expression-grammar.md`

Verdict: **Aligned**.

The JSON AST is bounded, function-free, namespace-strict, and deploy-time validated. The grammar is concrete implementation of the canonical expression boundary.

Required action: none.

### `idr-019-config-package.md`

Verdict: **Aligned**.

Dedicated config endpoint, monotonic package version, full snapshot delivery, current/pending two-slot device model, and forward-compatible parsing align with atomic config delivery and at-most-two-version device strategy.

Required action: none.

### `idr-020-pattern-state-machine-representation.md`

Verdict: **Aligned**.

Pattern definitions are platform-bundled; deployer config binds shapes, roles, and parameters only. State is derived from events plus pattern binding. No `pattern_ref`, no `current_state`, no `subject_ref.type = process` activation. Transition violations are accepted and flagged.

Required action: none.

### `idr-021-role-action-enforcement-model.md`

Verdict: **Aligned with canonical ledger amendment needed**.

Server-side role-action checking is authoritative; device behavior is advisory. Disallowed offline work is accepted and flagged as `role_stale`, not rejected. Assignment role permissions remain activity-scoped config.

The implementation treats `role_stale` as an active `manual_only` flag category. The canonical ledger does not currently list `role_stale` in `CDL-054`.

Required action: add `role_stale` to canonical flag resolvability classification as `manual_only`.

### `idr-022-flag-severity-and-domain-uniqueness.md`

Verdict: **Aligned with canonical ledger amendment needed**.

Severity is separate from resolvability, deployment-wide, and bounded. Domain uniqueness is shape-declared, device-advisory/server-authoritative, and emits standard `conflict_detected/v1` flags. This aligns with shape-declared uniqueness and accept-and-flag.

The active severity/default table includes categories that canonical `CDL-054` omits: `role_stale` and `temporal_authority_expired`.

Required action: update the canonical flag category table so all active categories have resolvability.

### `idr-023-role-action-domain-boundary-and-assignment-administration.md`

Verdict: **Aligned**.

Excluding `assignment_changed` from `activities[*].roles` preserves the activity/work-action boundary and avoids making optional activity into a universal authorization anchor. Assignment lifecycle commands remain online authority administration.

Required action: none.

### `idr-024-multi-axis-assignment-containment.md`

Verdict: **Aligned**.

The decision hardens canonical assignment containment across all platform-fixed scope axes. It correctly prevents unioning unrelated creator assignments across axes, requires explicit bootstrap/root authority, and keeps null-activity semantics explicit without making `activity_ref` mandatory.

Required action: none, assuming implementation tests cover ordinary actor command binding and target-assignment authority for ending assignments.

### `idr-025-pattern-definition-contract-and-delivery.md`

Verdict: **Aligned**.

Pattern definitions are platform-owned contract artifacts, delivered atomically with config, and not authored by deployers. This strengthens platform-fixed pattern closure and keeps server/mobile projection behavior version-aligned.

Required action: none.

### `idr-026-conflict-resolver-routing-and-single-writer-resolution.md`

Verdict: **Aligned with enforcement condition**.

The resolver routing decision aligns with single-writer resolution: every flag designates one resolver, detector author and resolver are distinct, auto-eligible does not mean system-owned, and canonical resolution requires exact resolver equality.

Condition: `designated_resolver` must be present for every new emitted `conflict_detected/v1` flag at runtime. Legacy missing-resolver flags must not resolve canonically.

Required action: keep runtime tests that reject non-designated resolution and prevent request-body actor spoofing.

## 7. Phase-spec findings

### Phase 0

Verdict: **Aligned**.

Phase 0 exercises the basic event-store, sync, shape, and projection loop without overreaching into conflict, authorization, config, or workflow.

### Phase 1

Verdict: **Aligned after Phase 3e corrections**.

Phase 1 proves accept-and-flag, alias projection, flag exclusion, merge/split, and raw-reference stale detection. The phase file now records the corrected deterministic flag ID input including `shape_ref`.

### Phase 2

Verdict: **Aligned with flag-category ledger gap**.

Assignment payload, scope-filtered sync, and selective retain align with the canonical authorization model. The active categories `temporal_authority_expired` and `role_stale` need canonical ledger completion.

### Phase 3

Verdict: **Aligned**.

Shape storage, expression grammar, config package, sensitivity surface, and `context.*` resolution stay within bounded configuration. No deployer-authored code surface is introduced.

### Phase 3d

Verdict: **Aligned**.

Phase 3d closes missing surfaces: `activity_ref` auto-population, sensitivity package emission, and `context.*` resolver tests. These complete canonical surfaces rather than changing them.

### Phase 3e

Verdict: **Aligned and necessary**.

Phase 3e corrects earlier type-vocabulary drift by removing identity/integrity domain fact names from envelope `type`, registering platform-bundled shapes, and rewriting consumers to `shape_ref` predicates.

### Phase 4

Verdict: **Aligned with two amendment/cleanup items**.

Phase 4 explicitly preserves no new envelope fields, no new event types, no deployer-authored state machines, live sync boundaries, activity optionality, and assignment-administration separation. The implementation gates align with the canonical ledger.

Required follow-up:

1. Complete canonical flag category classification for `role_stale` and `temporal_authority_expired`.
2. Resolve the deterministic flag UUID algorithm wording mismatch between `CDL-015` and implementation docs.

## 8. Required fixes before relying on IDRs as agent implementation context

### Fix 1 — Amend `CDL-054` active flag category table

Current canonical table omits active categories used by implementation. Add:

```text
role_stale                  manual_only
temporal_authority_expired  auto_eligible
```

Keep severity separate from resolvability. Do not let deployers change manual-only versus auto-eligible classification.

### Fix 2 — Resolve `CDL-015` deterministic UUID wording

Current canonical wording says deterministic flag identity uses UUIDv5 with a named namespace. Implementation documentation says the current code uses `UUID.nameUUIDFromBytes` with a stable salt and rejected a formal v5 migration for Phase 3e.

Choose one:

```text
Option A — implementation migration:
Change code to formal UUIDv5 with DATARUN_FLAG_NS.

Option B — canonical wording correction:
Change CDL-015 to require deterministic UUID derivation over
(source_event_id, shape_ref, flag_category) using a stable platform namespace/salt,
without mandating formal UUIDv5.
```

Recommended: Option B, unless external/public API compatibility requires formal UUIDv5.

### Fix 3 — Patch `idr-010` deterministic flag ID line

Replace:

```text
(source_event_id + flag_category)
```

with:

```text
(source_event_id + shape_ref + flag_category)
```

or mark that part of `idr-010` as superseded by the Phase 3e retrofit.

### Fix 4 — Add historical scope immutability rule to `idr-014` / `idr-015`

Add:

```text
`events.location_path` is write-time infrastructure metadata.
Once an event is inserted, its location_path must not be rewritten due to later
location hierarchy reparenting or subject-location correction. Future events use
the current resolved path. Backfill is allowed only for events that had no
location_path at insertion because the subject location was not yet known, and
that backfill must be a controlled admin operation.
```

### Fix 5 — Keep resolver routing runtime-enforced

`designated_resolver` may have been introduced as a semantic requirement before schema parity was complete. The implementation must ensure:

```text
- every new conflict_detected/v1 flag has designated_resolver;
- canonical conflict_resolved/v1 requires exact resolver equality;
- request-body actor ids are not authority;
- resolver_unassigned cannot be impersonated to clear flags;
- legacy missing-resolver flags do not resolve canonically.
```

## 9. Canonical ledger amendment candidates

These are not implementation errors; they are places where implementation legitimately made concrete decisions after the canonical architecture ledger was produced.

| Candidate                                                                | Reason                                                                                      | Recommended treatment                                                                                             |
| ------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `role_stale` flag category                                               | Active implementation category, manual-only, severity-configurable.                         | Add to CDL-054.                                                                                                   |
| `temporal_authority_expired` flag category                               | Active implementation category, auto-eligible, severity-configurable.                       | Add to CDL-054.                                                                                                   |
| Deterministic flag UUID algorithm                                        | Canonical over-specifies formal UUIDv5; implementation uses stable deterministic name UUID. | Amend CDL-015 to require deterministic input tuple, not concrete library algorithm, unless formal v5 is required. |
| Historical `events.location_path` immutability                           | Needed to keep original-scope authorization stable under location hierarchy changes.        | Add to IDR-014/015; optionally add an implementation guardrail under sync/access spec.                            |
| Exact field vocabulary / pattern definition schema / config package JSON | Implementation/platform-spec decisions, not architecture conflicts.                         | Keep in implementation specs, not necessarily canonical architecture ledger.                                      |

## 10. Final classification

| Classification | Items |
|---|---|
| Aligned | IDR-008, IDR-009, IDR-011, IDR-017, IDR-018, IDR-019, IDR-020, IDR-023, IDR-024, IDR-025; Phase 0, Phase 3, Phase 3d, Phase 3e. |
| Aligned with cleanup | IDR-010, Phase 1. |
| Aligned with canonical ledger amendment needed | IDR-013, IDR-021, IDR-022, Phase 2, Phase 4. |
| Conditionally aligned | IDR-014, IDR-015, IDR-026. |
| Strict mismatch to resolve | `CDL-015` UUIDv5 wording versus implementation's stable `UUID.nameUUIDFromBytes` wording. |
| Fatal architectural drift | None found. |

## 11. Recommended next step

Do not re-run the whole architecture consolidation. Make a narrow patch:

1. `canonical-decision-ledger.patch-001.md` or direct edit to `canonical-decision-ledger.md` for `CDL-015` and `CDL-054`.
2. Small IDR cleanup patch for `idr-010`, `idr-014`, and `idr-015`.
3. Keep this review artifact as provenance only. The canonical ledger remains the agent-facing architecture surface after patching.

## Post-patch verification (2026-05-31)

- **Canonical ledger:** `CDL-015` and `CDL-054` were updated in the canonical ledger (see `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md`). I confirmed the deterministic flag identity pseudocode and the flag-category table include the expected entries.
- **IDR updates:** `idr-010-conflict-detection-intercept.md` has been patched to reflect the corrected deterministic ID input including `shape_ref` (see that file). Confirmed.
- **Remaining harmonization:** `idr-014` and `idr-015` still contain inconsistent wording about `events.location_path` (one references cascade recompute on reparenting; the other allows backfill for NULL-only events while claiming existing events are "never stale"). Recommend a small IDR harmonization patch to explicitly forbid rewriting historical `events.location_path` except for controlled backfill of events that lacked a location at insertion.
- **Next action:** apply the IDR harmonization patch for `idr-014`/`idr-015`, then re-run this verification and update the evidence pack and module-boundary notes.
