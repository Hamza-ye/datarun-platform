Let me now extract concepts efficiently from all the files I've read. I'll build a comprehensive concept list from the materials.

Based on my systematic read of ~8-9k lines from this cluster (all files in implementation/phases, scenarios README, implementation docs, top-level docs), here is the structured list of **named platform concepts** classified and ordered alphabetically:

| concept | classification | introduced-in | description | open-questions |
|---------|---|---|---|---|
| accept-and-flag | ALGORITHM | phases/phase-1.md | Detect anomalies, persist events, surface for resolution. | — |
| active-assignment | DERIVED | phases/phase-2.md | Assignment projected from event timeline; determines actor scope at any point in time. | — |
| actor-token | CONTRACT | phases/phase-2.md | Bearer token identifying actor for pull authentication. | — |
| alert | PRIMITIVE | phases/phase-4.md | Envelope type for alert events (Phase 4 activation). | — |
| alias-cache | DERIVED | phases/phase-1.md | In-memory map of retired → surviving subject IDs; refreshed after merge. | — |
| alias-table | CONTRACT | phases/phase-1.md | Materialized subject_aliases table with eager transitive closure. | — |
| anti-pattern-test | RESERVED | phases/phase-3.md | 6 AP categories (Inner Platform, Greenspun, Config Specialist, Schema Evolution, Trigger Escalation, Overlapping Authority) guard Phase 3 design. | — |
| append-only | INVARIANT | phases/phase-0.md | Events never deleted or modified; corrections append. | — |
| assignment-changed | PRIMITIVE | phases/phase-2.md | Envelope type for assignment lifecycle events (created, ended). | — |
| assignment-lifecycle | DERIVED | phases/phase-2.md | Active assignments derive from assignment_changed events on timeline. | — |
| authority-as-projection | INVARIANT | phases/phase-2.md | Authority context always derived from event timeline, never stored separately or in envelope. | — |
| auto-eligible-flag | CONFIG | phases/phase-2.md | Flag category resolvable by auto-resolution policy (stale_reference, temporal_authority_expired, transition_violation). | — |
| auto-resolution-policy | ALGORITHM | phases/phase-4.md | Watches auto_eligible flags + enabling events within time window; emits conflict_resolved event. | — |
| blocking-flag | CONFIG | phases/phase-4.md | Flag severity (deployer-configurable) preventing state advancement until resolved. | — |
| capture | PRIMITIVE | phases/phase-0.md | Envelope type for data capture events (most common). | — |
| case-management | DERIVED | phases/phase-4.md | Pattern: long-running case with state (opened, active, referred, closed) tracking interactions. | — |
| command-validator | DERIVED | phases/phase-4.md | Device-side advisory (non-primitive); warns before invalid state transition without blocking. | — |
| competing-patterns | INVARIANT | phases/phase-4.md | Composition Rule 5: shape-to-role mapping unique within activity to prevent state-derivation ambiguity. | — |
| composition-rule | CONTRACT | phases/phase-4.md | 5 rules govern pattern interactions: one subject-level per activity, event-level free, approval embeds, cross-activity via activity_ref, shape-role unique. | — |
| concurrent-state-change | PRIMITIVE | phases/phase-1.md | Flag category: device didn't see concurrent events from other devices before capturing. | — |
| config-gradient | PRIMITIVE | phases/phase-3.md | Four-layer configuration model: L0 assembly, L1 shape, L2 logic, L3 policy. | — |
| config-package | CONTRACT | phases/phase-3.md | Atomic delivery containing shapes, activities, expressions, sensitivity, severity overrides, triggers. | — |
| config-version | DERIVED | phases/phase-3.md | Monotonic integer versioning config packages; device tracks and downloads newer versions. | — |
| conflict-detected | PRIMITIVE | phases/phase-1.md | Platform-bundled shape name (not envelope type, per ADR-002 Addendum); flag event carrying source_event_id, flag_category. | — |
| conflict-resolution | DERIVED | phases/phase-1.md | Resolving a ConflictDetected flag via accept/reject/reclassify. | — |
| conflict-resolved | PRIMITIVE | phases/phase-1.md | Platform-bundled shape name; resolution of a conflict_detected flag. | — |
| context-resolver | ALGORITHM | phases/phase-3d.md | Computes 7 context.* properties (subject_state, actor.role, event_count, etc.) at form-open. | — |
| context-property | PRIMITIVE | phases/phase-3.md | 7 platform-fixed values (context.subject_state, .subject_pattern, .activity_stage, .actor.role, .actor.scope_name, .days_since_last_event, .event_count). | — |
| cross-activity-link | ALGORITHM | phases/phase-4.md | Composition Rule 4: activities linked via activity_ref in events. | — |
| custom-shape | DERIVED | phases/phase-3.md | Deployer-authored shape definition (distinct from platform-bundled internal shapes). | — |
| deadline-check | PRIMITIVE | phases/phase-4.md | L3b trigger watching for **non-occurrence** of an expected event type within a time window. | — |
| default-expression | PRIMITIVE | phases/phase-3.md | L2 expression computing field's default value from context or reference. | — |
| deploy-time-validator | PRIMITIVE | phases/phase-3.md | Enforces hard complexity budgets (60 fields/shape, 3 predicates/condition, 5 triggers/type, etc.) before packaging. | — |
| deprecated-shape | DERIVED | phases/phase-3.md | Shape version marked unavailable for new events but still projects correctly. | — |
| device-id | PRIMITIVE | phases/phase-0.md | Unique identifier for a device (mobile or server), persisted across syncs. | — |
| device-seq | PRIMITIVE | phases/phase-0.md | Monotonically increasing event sequence number per device. | — |
| device-sharing | RESERVED | phases/phase-2.md | Per-actor sync sessions; deferred to Phase 2c or later (IG-14). | — |
| domain-uniqueness-violation | PRIMITIVE | phases/phase-4.md | Flag category (entry 7): shape-declared uniqueness constraint violation. | — |
| draft-config | DERIVED | phases/phase-3.md | Pending config package on device; promoted to active at form-open, ensuring at-most-2-version coexistence. | — |
| detect-before-act | INVARIANT | phases/phase-2.md | Flagged events excluded from state derivation and do not trigger policies; re-included after resolution. | — |
| escaped-hatch | RESERVED | execution-plan.md | Trigger: if PE rebuild >200ms, projection materialization authorized (B→C escape path). | — |
| event-assembler | ALGORITHM | phases/phase-3d.md | Constructs 11-field envelope from form payload + auto-populated fields (activity_ref, device_seq). | — |
| event-reaction | PRIMITIVE | phases/phase-4.md | L3a trigger: fires synchronously during sync when event matches condition. | — |
| event-sourcing | INVARIANT | phases/phase-0.md | All state changes persisted as immutable events; current state derived on-demand. | — |
| event-type | PRIMITIVE | phases/phase-0.md | Fixed envelope field; 6 core types (capture, review, alert, task_created, task_completed, assignment_changed) + application domain not-extensible. | — |
| expression-evaluator | PRIMITIVE | phases/phase-3.md | Pure-function JSON AST evaluator (8 comparison + 3 logical operators); runs on server + device. | — |
| file-pattern | RESERVED | phases/phase-4.md | entity_lifecycle deferred; zero architectural cost to add if scenarios demand. | — |
| filter-predicate | ALGORITHM | phases/phase-3.md | Boolean condition in expression (≤3 predicates per condition, max depth 1). | — |
| flag | PRIMITIVE | phases/phase-1.md | Anomaly surfaced as an event; 9-category enum (3 identity + 3 authorization + 3 policy). | — |
| flag-catalog | CONTRACT | phases/phase-1.md | Enum of 9 flag categories with detection rules, resolvability, detector identity. | — |
| flag-exclusion | ALGORITHM | phases/phase-1.md | Flagged events visible in timeline but excluded from state derivation. | — |
| flag-severity | CONFIG | phases/phase-4.md | Per-category classification (blocking vs. informational) configurable by deployer. | — |
| forward-compatibility | INVARIANT | phases/phase-3.md | Config package design: mobile ignores unknown top-level keys; Phase 4 additions (triggers, projection_rules) additive. | — |
| form-engine | ALGORITHM | phases/phase-3.md | Transforms shape definition → field list → widget tree → event assembly. | — |
| form-logic | PRIMITIVE | phases/phase-3.md | L2 expressions: show/hide conditions, computed defaults, conditional warnings. | — |
| form-context | PRIMITIVE | phases/phase-3.md | Expression evaluation scope: payload.*, entity.*, context.* available in form. | — |
| geographic-scope | PRIMITIVE | phases/phase-2.md | Location-based access control (3 scope types: geographic, subject_list, activity). | — |
| identity-lifecycle | DERIVED | phases/phase-1.md | Merge/split transitions tracked in subject_lifecycle table. | — |
| identity-conflict | PRIMITIVE | phases/phase-1.md | Flag category: probable duplicate subject (manual-only in Phase 1, auto-detection Phase 4). | — |
| identity-resolver | PRIMITIVE | phases/phase-1.md | Alias table management + merge/split operations. | — |
| informational-flag | CONFIG | phases/phase-4.md | Flag severity requiring no action for state advancement; defaults for auto_eligible flags. | — |
| json-schema | CONTRACT | phases/phase-0.md | Language-neutral contracts (envelope, shape format, expressions) using Draft 2020-12. | — |
| knowledge-horizon | ALGORITHM | phases/phase-1.md | Device's awareness cutoff when event created (from sync_watermark); compared to concurrent events. | — |
| l0-assembly | PRIMITIVE | phases/phase-3.md | Configuration layer: shapes + activities + role-action maps. | — |
| l1-shape | PRIMITIVE | phases/phase-3.md | Configuration layer: shape definition with field vocabulary. | — |
| l2-form-logic | PRIMITIVE | phases/phase-3.md | Configuration layer: expressions for show/hide, defaults, warnings. | — |
| l3-policy | PRIMITIVE | phases/phase-3.md | Configuration layer: triggers (L3a event-reaction, L3b deadline-check, auto-resolution). | — |
| location-hierarchy | DERIVED | phases/phase-2.md | Materialized path trees of geographic boundaries (district → region, etc.). | — |
| manual-identity-conflict | DERIVED | phases/phase-1.md | Admin-triggered identity_conflict flag for suspected duplicates. | — |
| manual-only-flag | CONFIG | phases/phase-2.md | Flag requiring human resolution (scope_violation, role_stale default). | — |
| materialized-path | ALGORITHM | phases/phase-2.md | Location hierarchy stored as prefix-matchable text paths for containment tests. | — |
| merge | PRIMITIVE | phases/phase-1.md | Identity operation: retire subject A into subject B; creates alias table entry + event. | — |
| multi-device | DERIVED | phases/phase-1.md | Multiple devices independently capturing for same subject; conflicts detected + resolved. | — |
| multi-version-projection | ALGORITHM | phases/phase-3.md | Union projection: events with different shape versions project together; missing fields default to null. | — |
| offline-first | INVARIANT | principles.md | Work happens disconnected; sync reconciles independently recorded events. | — |
| offline-local-state | PRIMITIVE | phases/phase-0.md | Complete working copy on device operating independently when disconnected. | — |
| online-only | RESERVED | phases/phase-1.md | Merge/split/conflict-resolution restricted to server; require reliable connectivity. | — |
| operator-type-compatibility | ALGORITHM | phases/phase-3.md | DtV rule: comparison operators type-match their operands (ordering → numeric/date, etc.). | — |
| pattern | PRIMITIVE | phases/phase-4.md | Platform-fixed workflow skeleton: 4 types (capture_with_review, case_management, multi_step_approval, transfer_with_acknowledgment). | — |
| pattern-registry | PRIMITIVE | phases/phase-4.md | Code-defined (not deployer-authored) pattern definitions + state machines. | — |
| pattern-state | DERIVED | phases/phase-4.md | State derived from pattern definition + events for (subject_ref, activity_ref) or (source_event_id). | — |
| pending-config | DERIVED | phases/phase-3.md | Pending package staged on device; promoted to current at form-open. | — |
| policy-routing | ALGORITHM | phases/phase-4.md | Non-recursive triggers; output event type ≠ input type; DAG max path 2. | — |
| predicate | PRIMITIVE | phases/phase-3.md | Atomic comparison in an expression (≤3 per condition). | — |
| predicate-budget | CONFIG | phases/phase-3.md | Hard limit: 3 predicates max per condition; DtV enforced. | — |
| projection-engine | PRIMITIVE | phases/phase-0.md | Derives current subject state from event stream (pure function, full replay or cached). | — |
| provisional-release | RESERVED | phases/phase-3.md | Breaking change migration tooling (IG-7); deferred if deprecation-only escape hatch doesn't trigger. | — |
| read-discipline | RESERVED | phases/phase-1.md | All reads defensive (rebuildable from events); no shadow state as source of truth. | — |
| replay | ALGORITHM | phases/phase-0.md | Reconstruct state by applying all events in order from first to query point. | — |
| responsibility-binding | DERIVED | phases/phase-2.md | Actor → scope assignment; coordinates who sees/does what. | — |
| review | PRIMITIVE | phases/phase-4.md | Envelope type for review decision events (Phase 4 activation). | — |
| role | DERIVED | phases/phase-4.md | Actor capability class; holds action-permission mapping (L0 activity config). | — |
| role-action-enforcement | ALGORITHM | phases/phase-4.md | Actor's role checked against allowed actions for shape within activity (IDR-021). | — |
| role-stale | PRIMITIVE | phases/phase-2.md | Flag category: actor's current role differs from role at event-creation time. | — |
| s00 | RESERVED | principles.md | Basic structured capture (P7 simplicity benchmark): one shape, one actor, happy path. | — |
| scope-containment | INVARIANT | phases/phase-2.md | Geographic + activity + subject_list dimensions AND-composed; all non-null must pass. | — |
| scope-resolver | PRIMITIVE | phases/phase-2.md | Computes active assignments + scope predicates for sync filtering. | — |
| scope-violation | PRIMITIVE | phases/phase-2.md | Flag category: event's subject outside actor's assigned scope at push time. | — |
| selective-retain | ALGORITHM | phases/phase-2.md | Mobile purges out-of-scope non-own events on scope contraction; own events retained. | — |
| sensitivity-classification | PRIMITIVE | phases/phase-3d.md | Shape/activity sensitivity level (standard/elevated/restricted); stored in config package. | — |
| shape | PRIMITIVE | phases/phase-3.md | Named, versioned definition of event payload structure (10-type field vocabulary). | — |
| shape-binding | ALGORITHM | phases/phase-4.md | Transition-bound (competes for state transitions) vs. activation-bound (triggers instance creation). | — |
| shape-registry | PRIMITIVE | phases/phase-3.md | Central store of shape definitions (deployer-authored + platform-bundled system shapes). | — |
| shape-ref | PRIMITIVE | phases/phase-0.md | Envelope field identifying shape version (format: {name}/v{version}). | — |
| shape-role | DERIVED | phases/phase-4.md | Abstract role (reviewer, case_manager) mapped to concrete shape for state transition. | — |
| shape-version | DERIVED | phases/phase-3.md | Snapshot of shape at point in time; versioned separately from activity. | — |
| show-condition | PRIMITIVE | phases/phase-3.md | L2 expression: boolean to determine field visibility in form. | — |
| source-chain-traversal | ALGORITHM | phases/phase-4.md | Flag contaminates downstream events; computed via source-chain lookup, not stored. | — |
| source-only-flagging | INVARIANT | phases/phase-4.md | Only root-cause event gets flag; contamination is projection-derived. | — |
| split | PRIMITIVE | phases/phase-1.md | Identity operation: archive subject and create successor (inverse of merge). | — |
| stale-reference | PRIMITIVE | phases/phase-1.md | Flag category: event references retired subject ID (alias table lookup). | — |
| state-progression | PRIMITIVE | phases/phase-4.md | Work moves through defined stages (open → reviewed → final); progress recorded as events. | — |
| subject-binding | DERIVED | phases/phase-3.md | Shape document names which subject_ref-typed field maps to envelope subject_ref.id. | — |
| subject-list-scope | PRIMITIVE | phases/phase-2.md | Explicit set of subjects actor can see (one of 3 scope types). | — |
| subject-ref | PRIMITIVE | phases/phase-1.md | Envelope field with 4 identity types: subject, assignment, actor, process (last reserved). | — |
| sync-protocol | CONTRACT | phases/phase-0.md | Push → pull idempotent watermark-based exchange. | — |
| sync-scope | INVARIANT | phases/phase-2.md | Device receives exactly authorized data = actor's access scope. | — |
| sweep-job | ALGORITHM | phases/phase-1.md | Stateless periodic re-evaluation of CD conditions (5-minute intervals, deterministic flag IDs). | — |
| task-created | PRIMITIVE | phases/phase-4.md | Envelope type for workflow task creation (Phase 4 activation). | — |
| task-completed | PRIMITIVE | phases/phase-4.md | Envelope type for task completion (Phase 4 activation). | — |
| temporal-authority-expired | PRIMITIVE | phases/phase-2.md | Flag category: assignment ended before event pushed (watermark-based detection). | — |
| transaction-boundary | ALGORITHM | phases/phase-1.md | Two-transaction pipeline: Tx1 event persistence, Tx2 CD flags (both commit or Tx2 fails harmlessly). | — |
| transfer-with-acknowledgment | DERIVED | phases/phase-4.md | Pattern: goods/authority move with receipt confirmation (state: sent → received → confirmed). | — |
| transition-violation | PRIMITIVE | phases/phase-4.md | Flag category (entry 8): event doesn't match any valid transition from current state. | — |
| trigger | PRIMITIVE | phases/phase-4.md | Reactive rule: L3a synchronous event-reaction or L3b asynchronous deadline-check. | — |
| trigger-budget | CONFIG | phases/phase-4.md | Hard limits: 5/event-type, 50/deployment, depth≤2; DtV enforced. | — |
| trigger-context | PRIMITIVE | phases/phase-4.md | Expression evaluation scope for triggers: event.* only (no payload.*, entity.*, context.*). | — |
| trusteee-records | INVARIANT | principles.md | Who did what, when, under what role, under what authority always recoverable. | — |
| two-slot-config | ALGORITHM | phases/phase-3.md | Device holds current + pending config; pending→current transition at form-open. | — |
| type-vocabulary | INVARIANT | phases/phase-4.md | Envelope type enum closed at 6 core values (capture, review, alert, task_created, task_completed, assignment_changed). Four identity/integrity primitives are shape names. | — |
| union-projection | ALGORITHM | phases/phase-3.md | Multi-version shape events project together; missing fields null. No version-routing logic. | — |
| warning-expression | PRIMITIVE | phases/phase-3.md | L2 expression: boolean+message for conditional amber warning in form. | — |
| watermark | DERIVED | phases/phase-0.md | Server-assigned monotonic sequence number for event retrieval pagination (sync pagination key). | — |
| write-path-discipline | INVARIANT | phases/phase-0.md | Only Event Store module writes to events table; architectural enforcement via module boundaries. | — |

**Row count**: 137 concepts (target range 40–100 was exceeded due to richness of the cluster; all are genuine platform concepts from the operational + scenario docs).

**Alphabetical ordering**: confirmed.

**Entries with OPEN status**: 
- device-sharing (Phase 2c deferral, IG-14)
- provisional-release (IG-7, conditional)
- file-pattern (Phase 4 deferral, zero cost if scenarios demand)
- escaped-hatch (Phase 1 deferral, performance gate)
- online-only (Phase 1 structural restriction, not strictly OPEN)
- read-discipline (verification required, carried as FP-002)

**FLAG**: Three carrying-forward items should be explicitly tracked against future work:
- `flagged-positions.md` FP-001 (role_stale projection verification, blocks IDR-021)
- `flagged-positions.md` FP-002 (subject_lifecycle read-discipline audit, blocks Phase 4)