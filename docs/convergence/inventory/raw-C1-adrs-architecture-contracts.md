Based on my read-only review of Cluster C1 (ADRs, architecture, and contracts), here is the structured inventory of named platform concepts:

| concept | classification | introduced-in | description | open-questions |
|---------|---|---|---|---|
| accept-and-flag | ALGORITHM | adr-002-identity-conflict.md | Universal anomaly handling: events accepted, anomalies flagged, never rejected. | — |
| activity | CONFIG | adr-004-configuration-boundary.md | Named operation template defining shapes, roles, pattern bindings. | — |
| activity-ref | CONTRACT | adr-004-configuration-boundary.md | Optional envelope field referencing activity instance. Deployer-chosen identifier format. | — |
| actor | INVARIANT | adr-002-identity-conflict.md | Persistent identity category for person performing actions. Non-mergeable. | — |
| actor-ref | CONTRACT | adr-002-identity-conflict.md | Typed identity reference in envelope. `system:` prefix for system actors. | — |
| alias-mapping | DERIVED | adr-002-identity-conflict.md | Projection-derived merge resolution: `retired_id → surviving_id`. Single-hop lookup. | — |
| append-only-write | INVARIANT | adr-001-offline-data-model.md | All writes immutable after creation. Events never modified or deleted. | — |
| alert | CONTRACT | adr-004-configuration-boundary.md | Envelope type for system-detected anomalies requiring attention. Platform-fixed. | — |
| assignment | INVARIANT | adr-002-identity-conflict.md | Temporal identity category: actor bound to scope with start/end bounds. | — |
| assignment-changed | CONTRACT | adr-004-configuration-boundary.md | Envelope type signalling scope or role modification. Triggers sync scope recomputation. | — |
| assignment-created | CONFIG | shapes/assignment_created.schema.json | Shape: binds actor to scope with role. `assignment_changed` type envelope. | — |
| assignment-ended | CONFIG | shapes/assignment_ended.schema.json | Shape: terminates active assignment. `assignment_changed` type envelope. | — |
| authority-context | DERIVED | adr-003-authorization-sync.md | Projection derived from assignment timeline. Which assignment(s) authorized event. | Envelope field [3-S3 escape hatch](adr-003-authorization-sync.md) if reconstruction exceeds 50ms/event. |
| auto-eligible | FLAG | adr-005-state-progression.md | Flag resolvability: platform can auto-resolve without human judgment. 3 categories: stale_reference, temporal_authority_expired, transition_violation. | — |
| auto-resolution | ALGORITHM | adr-005-state-progression.md | L3b deadline-watch: resolves auto_eligible flags when enabling event arrives within window. | — |
| case-management | CONFIG | patterns.md | Long-running subject lifecycle: open → interact → resolve → close → reopen. | — |
| capture | CONTRACT | adr-004-configuration-boundary.md | Envelope type for primary data collection. System events also use capture type. | — |
| capture-with-review | CONFIG | patterns.md | Event-level review cycle: pending → accepted/returned. Captures tracked independently. | — |
| causal-ordering | ALGORITHM | adr-002-identity-conflict.md | `device_seq` (intra-device) + `sync_watermark` (cross-device concurrency). | — |
| command-validator | DERIVED | primitives.md | Optional component: warns users before invalid transitions. Not a primitive. | — |
| concurrent-state-change | FLAG | adr-002-identity-conflict.md | Flag: two actors changed same subject concurrently. manual_only. | — |
| config-packager | PRIMITIVE | adr-004-configuration-boundary.md | Deliverable: assembles shapes, activities, triggers, patterns atomically to device. | — |
| configuration-boundary | INVARIANT | adr-004-configuration-boundary.md | Four-layer gradient (L0 assembly, L1 shapes, L2 logic, L3 policy) separates configuration from code. | — |
| configuration-delivery-pipeline | ALGORITHM | adr-004-configuration-boundary.md | Author → validate → package → deliver → apply. Binary accept/reject gating. | — |
| conflict-detected | CONFIG | adr-002-addendum-type-vocabulary.md | Platform-bundled shape: alert-type flag event with flag_category, designated_resolver, reason. | — |
| conflict-detector | PRIMITIVE | adr-002-identity-conflict.md | Evaluates events for anomalies; raises ConflictDetected events, never rejects. | — |
| conflict-resolution | ALGORITHM | adr-002-identity-conflict.md | ConflictResolved event: accepted/rejected/reclassified. Single-writer designated resolver. | — |
| conflict-resolved | CONFIG | adr-002-addendum-type-vocabulary.md | Platform-bundled shape: review-type (human) or capture-type (auto) resolution event. | — |
| context-scope | DERIVED | adr-005-state-progression.md | Form evaluation: 7 properties (subject_state, actor.role, event_count, etc). Pre-resolved at form-open. | — |
| contract | INVARIANT | contracts.md | Guarantee between primitives. 21 total: input→output guarantees. | — |
| cross-cutting | INVARIANT | cross-cutting.md | 8 concerns spanning primitives: envelope, accept-and-flag, detect-before-act, gradient, delivery, sync, flags, aggregation. | — |
| deadline-check | CONFIG | adr-004-configuration-boundary.md | L3b trigger: watches for non-occurrence, fires after time window. Detects expected event not received. | — |
| deploy-time-validator | PRIMITIVE | adr-004-configuration-boundary.md | Configuration acceptance gate. Enforces 6 hard budgets before package reaches device. | — |
| designated-resolver | INVARIANT | adr-002-identity-conflict.md | Every flag has exactly one resolver. Prevents meta-conflicts from offline resolution. | — |
| detect-before-act | INVARIANT | adr-002-identity-conflict.md | Flagged events exclude from state derivation and policy execution. Extends to all flag types. | — |
| device-id | CONTRACT | adr-002-identity-conflict.md | Hardware-bound UUID. New device → new device_id. Scopes device_seq namespace. | — |
| device-sequence | CONTRACT | adr-002-identity-conflict.md | Monotonically increasing integer per device. Global uniqueness: (device_id, device_seq) never reused. | — |
| device-time | CONTRACT | adr-002-identity-conflict.md | Advisory-only timestamp. No ordering or correctness depends on it. ISO 8601 format. | — |
| domain-uniqueness-violation | FLAG | adr-004-configuration-boundary.md | Flag: shape-declared uniqueness rule violation. Scope, time period, action (warn/block) configured. | Phase 4 detector (ADR-004 S14, Phase 4/IDR-022). |
| event-envelope | CONTRACT | adr-001-offline-data-model.md | 11-field universal structure. Finalized: id, type, shape_ref, activity_ref, subject_ref, actor_ref, device_id, device_seq, sync_watermark, timestamp, payload. | — |
| event-id | CONTRACT | adr-001-offline-data-model.md | Client-generated UUID. Globally unique, used for deduplication across syncs. | — |
| event-level-pattern | CONFIG | adr-005-state-progression.md | Workflow pattern tracking individual event instances. Compose freely. PE state key: (source_event_id). | — |
| event-reaction-trigger | CONFIG | adr-004-configuration-boundary.md | L3a: synchronous, fires on single-event condition during sync. Creates exactly one output event. | — |
| event-ref | CONTRACT | adr-001-offline-data-model.md | Events reference prior events (corrections, reviews). Reference structure shaped by ADR-2, ADR-5. | — |
| event-store | PRIMITIVE | adr-001-offline-data-model.md | Sole write path. Persists immutable 11-field events. Append-only enforcement. | — |
| event-type | CONTRACT | adr-004-configuration-boundary.md | Enum(6): capture, review, alert, task_created, task_completed, assignment_changed. Platform-fixed, closed, append-only. | Reserved growth via ADR amendment. |
| expression-evaluator | PRIMITIVE | adr-004-configuration-boundary.md | Evaluates conditions (form L2, trigger L3) using operators + field references, zero functions. | — |
| expression-language | ALGORITHM | adr-004-configuration-boundary.md | Single language, two contexts. Form: payload.*, entity.*, context.*. Trigger: event.*. 3-predicate max. | — |
| flag | INVARIANT | flag-catalog.md | Anomaly surfacing mechanism. 9 categories, 2 resolvability classes, deployment-configurable severity. | — |
| flag-catalog | INVARIANT | flag-catalog.md | Unified 9-category register: identity, authorization, state, domain. Resolvability platform-fixed. | Catalog 9 reserved for future. |
| flag-category | CONTRACT | conflict_detected.schema.json | Enumerated anomaly type. One of 9: concurrent_state_change, stale_reference, identity_conflict, scope_violation, temporal_authority_expired, role_stale, domain_uniqueness_violation, transition_violation, +1 reserved. | — |
| flagged-event-exclusion | ALGORITHM | adr-005-state-progression.md | Flagged events visible in timeline, excluded from state derivation and policy execution. | — |
| four-layer-gradient | CONFIG | adr-004-configuration-boundary.md | L0 assembly (structural), L1 shapes (schema), L2 logic (form), L3 policy (persistent). | — |
| geographic-scope | CONFIG | adr-003-authorization-sync.md | Platform-fixed scope type: subject location within actor's area (hierarchy containment). | — |
| hardware-bound-identity | INVARIANT | adr-002-identity-conflict.md | device_id identifies physical device, not user. Same actor → same actor_ref, different device_ids. | — |
| identity-conflict | FLAG | adr-002-identity-conflict.md | Flag: potential duplicate subjects (same real-world entity, two UUIDs). manual_only. | — |
| identity-resolver | PRIMITIVE | adr-002-identity-conflict.md | Manages subject lifecycle: merge, split, alias resolution, lineage tracking (DAG). | — |
| identity-type-taxonomy | INVARIANT | adr-002-identity-conflict.md | 4 categories: subject (mergeable), actor (persistent), assignment (temporal), process (transient). | — |
| invariant | INVARIANT | primitives.md | Structural guarantee a primitive must always maintain. Examples: append-only, alias acyclicity, single-writer resolution. | — |
| lineage-graph | DERIVED | adr-002-identity-conflict.md | Identity merge/split history. DAG by construction. Merge operands active; archived terminal. | — |
| manual-only | FLAG | adr-005-state-progression.md | Flag resolvability: requires human judgment. 5 categories: identity_conflict, concurrent_state_change, scope_violation, role_stale, domain_uniqueness_violation. | — |
| merge-subject | ALGORITHM | adr-002-identity-conflict.md | Online-only operation. Creates SubjectsMerged event. Alias in projection, no event rewrite. | — |
| multi-step-approval | CONFIG | patterns.md | Subject-level or event-level N-level approval chain. Parameterized level count. Returns at any level restart at 1. | — |
| no-pattern-activity | CONFIG | patterns.md | Activity with `pattern: none`. No state derivation, transition evaluation, pattern flags. Minimal overhead. | — |
| order-independent-sync | INVARIANT | adr-001-offline-data-model.md | Events carry own ordering metadata. Arrival order does not determine logical order. | — |
| pattern-composition-rule | INVARIANT | adr-005-state-progression.md | 5 rules: one subject-level per activity, event-level compose freely, approval embeds, cross-activity via activity_ref, shape-pattern uniqueness. | — |
| pattern-registry | PRIMITIVE | adr-005-state-progression.md | Platform-fixed workflow definitions. Deployer selects, parameterizes at L0. Closed vocabulary. | Growth via platform evolution. |
| phase-3e-retrofit | RESERVED | adr-002-addendum-type-vocabulary.md | Phase 3e: platform-bundles 4 internal shapes (conflict_detected/v1, conflict_resolved/v1, subjects_merged/v1, subject_split/v1). Server boot registration. | — |
| process-identity | RESERVED | envelope.schema.json | Transient identity category (subject_ref type). Reserved for future workflow-instance refs. No current emission site. | Phase 4+ decision. |
| projection-derived-state | INVARIANT | adr-005-state-progression.md | Workflow state computed from events + pattern rules, never stored in payloads. Rebuildable. | — |
| projection-engine | PRIMITIVE | adr-001-offline-data-model.md | Derives current state from event stream. Projections always rebuildable. Events win on divergence. | — |
| raw-reference | ALGORITHM | adr-002-identity-conflict.md | Conflict detection uses original subject_ref as written. Alias resolution post-detection in projection. | — |
| resolvability-classification | INVARIANT | adr-005-state-progression.md | Platform-defined per flag type: auto_eligible or manual_only. Deployer-non-configurable. | — |
| review | CONTRACT | adr-004-configuration-boundary.md | Envelope type for human judgment on prior event. Source-linked. Review-status projected. | — |
| role-stale | FLAG | adr-003-authorization-sync.md | Flag: actor's role changed since event creation. Affects capability-restricted actions. manual_only. | — |
| scope-containment | ALGORITHM | adr-003-authorization-sync.md | Access control test: `actor.assignment.scope ⊇ subject.location`. Single scope-containment check. | — |
| scope-composition | CONFIG | adr-003-authorization-sync.md | Deployer combines platform-fixed scope types. AND composition: all non-null dimensions pass. | — |
| scope-equality | INVARIANT | adr-003-authorization-sync.md | Sync scope = access scope. Device receives exactly authorized data, no more, no less. | — |
| scope-resolver | PRIMITIVE | adr-003-authorization-sync.md | Determines sync payload and authorization. Geographic + subject_list + activity dimensions. | — |
| scope-violation | FLAG | adr-003-authorization-sync.md | Flag: potential unauthorized access. Subject location outside actor's assignment scope. manual_only. | — |
| sensitivity-classification | CONFIG | adr-004-configuration-boundary.md | Shape/activity property: standard, elevated, restricted. Affects sync filtering, retention, audit level. | — |
| shape | CONFIG | adr-004-configuration-boundary.md | Typed payload schema. Versioned (shape_name/vN). All versions valid forever. Deprecation-only evolution default. | — |
| shape-ref | CONTRACT | adr-004-configuration-boundary.md | Mandatory envelope field. Format: {shape_name}/v{version}. Every event self-describing. | — |
| shape-registry | PRIMITIVE | adr-004-configuration-boundary.md | Stores versioned schema definitions. All versions available forever. Multi-version projection support. | — |
| split-subject | ALGORITHM | adr-002-identity-conflict.md | Online-only operation. Creates SubjectSplit event. Archives source, new events to successors. | — |
| stale-reference | FLAG | adr-002-identity-conflict.md | Flag: entity updated after event creation. Often self-correcting. auto_eligible. | — |
| state-machine | CONFIG | adr-005-state-progression.md | Projection-derived subject/event lifecycle. Platform does not enforce — flags violations. Pattern-defined skeleton. | — |
| subject | INVARIANT | adr-002-identity-conflict.md | Persistent identity category for real-world entity. Mergeable, splittable, full lifecycle. | — |
| subject-level-pattern | CONFIG | adr-005-state-progression.md | Workflow pattern tracking subject lifecycle. One per activity maximum. PE state key: (subject_ref, activity_ref). | — |
| subject-list-scope | CONFIG | adr-003-authorization-sync.md | Platform-fixed scope type: explicit subject UUID list. Null means unrestricted. | — |
| subject-ref | CONTRACT | adr-002-identity-conflict.md | Typed identity reference in envelope: {type: subject|actor|assignment|process, id: UUID}. | — |
| subjects-merged | CONFIG | shapes/subjects_merged.schema.json | Shape: merge event payload (surviving_id, retired_id, reason). `capture` type. Platform-bundled. | — |
| subject-split | CONFIG | shapes/subject_split.schema.json | Shape: split event payload (source_id, successor_id, reason). `capture` type. Platform-bundled. | — |
| subject-split-legacy | RESERVED | adr-002-identity-conflict.md | Misnaming note: schema file `subject_split.schema.json` (singular) vs. ADR payload `SubjectSplit`. Use v1 singular naming. | — |
| sync-protocol | ALGORITHM | sync-protocol.md | Watermark-based idempotent push/pull. Events immutable, deduped by id, order-independent. Scope-filtered pull. | — |
| sync-scope | INVARIANT | adr-003-authorization-sync.md | Events device receives at sync. Equals access scope (S2). Filtered from active assignments. | — |
| sync-unit | INVARIANT | adr-001-offline-data-model.md | The immutable event. Sync transfers events receiver hasn't seen, filtered to scope. | — |
| sync-watermark | CONTRACT | adr-002-identity-conflict.md | Server-assigned on receipt (BIGSERIAL). Null until synced. Enables pull pagination, cross-device concurrency detection. | — |
| system-actor | ALGORITHM | adr-004-configuration-boundary.md | System-generated events use actor_ref.id format `system:{source_type}/{source_id}`. Traceable authorship. | — |
| task-completed | CONTRACT | adr-004-configuration-boundary.md | Envelope type for work item resolution. Source-linked to task_created. Deadline cancellation. | — |
| task-created | CONTRACT | adr-004-configuration-boundary.md | Envelope type for work item assignment. Deadline tracking, response watch. | — |
| temporal-authority-expired | FLAG | adr-003-authorization-sync.md | Flag: assignment ended before event creation. Timing overlap. auto_eligible. | — |
| transfer-with-acknowledgment | CONFIG | patterns.md | Two-party handoff: dispatch → receipt → discrepancy report → resolution. | — |
| transition-violation | FLAG | adr-005-state-progression.md | Flag: invalid state transition against pattern rules. Usually offline timing overlap. auto_eligible. | Phase 4 detector (ADR-005 S1, Phase 4/IDR-020). |
| trigger-engine | PRIMITIVE | adr-004-configuration-boundary.md | Server-only reactive processing. L3a event-reaction (sync), L3b deadline-check (async). Non-recursive, DAG max path 2. | — |
| workflow-pattern | CONFIG | adr-005-state-progression.md | Platform-fixed lifecycle template. Deployer selects and parameterizes. 4 initial patterns: capture_with_review, case_management, multi_step_approval, transfer_with_acknowledgment. | — |
| write-path-discipline | INVARIANT | adr-001-offline-data-model.md | All state changes via Event Store only. Projections never independently written. Escape hatch viable only if enforced. | — |