# Canonical Decision Ledger

Status: **Accepted — canonical agent-facing decision surface**.

Purpose: provide one settled decision surface for the platform architecture. This file states current decisions directly. Earlier ADRs, extracted ledgers, addenda, and exploration notes are provenance only and are not active agent decision surfaces.

Use rule: agents and implementation/specification prompts should consume this file as the primary architectural decision input.

Normalization rule: this ledger uses **mechanism** for platform-owned semantics and **configuration instance** for deployer-authored objects. The term **primitive** is not used as a broad name for capability areas or modules; when a smaller irreducible construct is needed, it must be named through its actual contract, grammar, protocol, vocabulary, or platform-owned registry.

---

## 1. Authority

### CDL-000: Canonical surface rule

**Status:** Accepted  
**Classification:** Governance invariant

**Decision:**  
The final canonical decision ledger is the only agent-facing decision surface. Earlier ADRs, extracted ledgers, addenda, and exploration notes are provenance only after the final ledger is accepted.

**Rationale:**  
The platform accumulated multiple document layers during exploration, formalization, ledger extraction, and later canonicalization. Agents misread the system when those layers are all treated as active authority. The consolidated ledger removes that ambiguity by making one normalized surface authoritative.

**Rejected alternatives:**

- Keeping formal ADRs, extracted ledgers, and clarifier ADRs as parallel active sources.
- Adding cross-reference notes instead of consolidating decisions.
- Making agents infer current truth from document chronology.

**Binding constraints:**

- The final ledger must state current truth directly.
- Decision rows must not depend on following pointers to older documents.
- Provenance may exist outside the final ledger, but not as an agent decision surface.

**Guardrails:**

- Do not use old source chronology as a resolution mechanism for agents.
- Do not preserve superseded wording merely because it appeared earlier.

**Must not happen:**

- Agents must not need to compare ADRs, ledgers, addenda, and explorations to determine a current decision.

**Scope boundary:**  
This row governs documentation authority. It does not change the architecture itself.

**Downstream impact:**  
All specs, implementation prompts, and future agents should consume the final canonical ledger as their primary decision input.

---

## 2. Global invariants

### CDL-001: Operational truth lives in the immutable event stream

**Status:** Accepted  
**Classification:** Structural invariant

**Decision:**  
All operational writes enter through the append-only event store. Existing events are never modified or deleted. Corrections, reviews, state changes, identity changes, assignments, flags, and workflow resolutions are represented by additional events.

**Rationale:**  
Traceability requires the original action and every later correction or decision to remain inspectable. Mutable current-state storage loses history or pushes audit into a secondary structure that can diverge from primary data.

**Rejected alternatives:**

- Mutable records updated in place.
- Mutable records plus separate audit log as the canonical model.
- Deleting or overwriting incorrect records.
- Treating current-state tables as authoritative.

**Binding constraints:**

- Event stream is canonical.
- Corrections append.
- Reviews append.
- Status changes append.
- Identity changes append.
- Resolution decisions append.

**Guardrails:**

- Current-state optimization must not become source of truth.
- Repair path is event append plus projection rebuild, not direct state patch.

**Must not happen:**

- Existing events must not be overwritten.
- Existing events must not be deleted.
- Projection divergence must not redefine truth.

**Scope boundary:**  
This governs operational records. Non-event telemetry, logs, and metrics may exist outside the event stream but are not canonical operational truth.

**Downstream impact:**  
Identity, authorization, configuration, workflow, and conflict handling must all express state changes as events or derived projections.

---

### CDL-002: Projections are derived and rebuildable

**Status:** Accepted  
**Classification:** Structural invariant

**Decision:**  
Current state, read models, queues, indexes, aliases, workflow state, and reporting projections are derived from the event stream. Materialized projections may be maintained for performance, but they are rebuildable and non-canonical.

**Rationale:**  
Offline devices and large deployments need fast reads, but fast reads must not create a second source of truth. Rebuildability keeps the event stream authoritative while allowing performance-oriented views.

**Rejected alternatives:**

- Co-primary action log and current-state tables.
- Direct projection patching as a canonical repair.
- Workflow state stored as authoritative current state.

**Binding constraints:**

- Projections may be eager or lazy.
- If a projection disagrees with events, events win.
- Projection repair means rebuild from events.

**Guardrails:**

- Optimization is allowed; promotion to truth is not.
- Projection schemas may evolve, but event meaning must remain stable.

**Must not happen:**

- A projection must not be the only place where a decision, correction, or state change exists.

**Scope boundary:**  
This does not prohibit caches, indexes, denormalized tables, or materialized views. It prohibits making them authoritative over the event stream.

**Downstream impact:**  
Workflow state, alias resolution, authorization views, sync scopes, and flag queues are all projection concerns unless explicitly recorded as events.

---

### CDL-003: Valid state-stale events are accepted and flagged

**Status:** Accepted  
**Classification:** Invariant

**Decision:**  
A validly structured event is never rejected for state-based reasons. State anomalies are represented by appending flag events alongside the accepted source event.

State-based reasons include stale references, authorization drift, role drift, identity conflicts, concurrent state changes, domain uniqueness violations, and workflow transition violations.

**Rationale:**  
Offline-first operation means devices act with partial and stale knowledge. Rejecting structurally valid work at sync would lose field evidence and make offline operation unsafe. Accepting the event preserves the work; flagging surfaces uncertainty for review or automated resolution.

**Rejected alternatives:**

- Rejecting stale or anomalous events at sync.
- Mutating the source event into an error state.
- Storing anomaly records outside the event stream as the canonical representation.
- Last-write-wins resolution.

**Binding constraints:**

- Malformed envelopes are rejected by contract validation.
- Structurally valid events are stored.
- State anomalies appear as flag events.
- Flags do not mutate the source event.

**Guardrails:**

- Keep structural validation separate from state validation.
- Keep field evidence even when the event is questionable.

**Must not happen:**

- Offline work must not disappear because central state changed while the device was disconnected.
- A state anomaly must not be silently ignored.

**Scope boundary:**  
This invariant applies to state-based anomalies. It does not require accepting events with missing required envelope fields, invalid reference shapes, invalid UUIDs, or invalid payload/schema conformance.

**Downstream impact:**  
Conflict detection, authorization staleness, domain uniqueness, and workflow transition checks must emit flags rather than rejecting otherwise valid events.

---

### CDL-004: Detect-before-act governs policy and state participation

**Status:** Accepted  
**Classification:** Strategy-protecting invariant

**Decision:**  
Unresolved flagged events remain visible in timelines and audit views, but they do not trigger downstream policy execution and do not change derived workflow state. When a flag is resolved as accepted, projections re-derive state including the source event. When a flag is resolved as rejected, projections keep excluding the source event from current-state derivation.

**Rationale:**  
Flagged events have uncertain validity. Triggering policies or advancing state from uncertain data can create downstream consequences that are harder to reverse than the original flag.

**Rejected alternatives:**

- Letting flagged events trigger policies immediately.
- Letting flagged events advance workflow state before resolution.
- Hiding flagged events entirely from timelines.
- Forking permanent tentative and authoritative state models.

**Binding constraints:**

- Flagged events are visible.
- Flagged events are excluded from policy execution while unresolved.
- Flagged events are excluded from state-machine evaluation while unresolved.
- Resolution events control whether projections later include or exclude the source event.

**Guardrails:**

- Separate event visibility from state participation.
- Preserve audit even when excluding an event from current state.

**Must not happen:**

- A flagged transition must not silently move a case to a new current state.
- A flagged event must not fire L3 policy before resolution.

**Scope boundary:**  
This governs policy execution and workflow state participation. It does not remove flagged events from audit, timelines, or resolver queues.

**Downstream impact:**  
Trigger engines, projection engines, workflow views, and flag resolution flows must use the same participation semantics.

---

### CDL-005: Mechanisms and instances are classified separately

**Status:** Accepted  
**Classification:** invariant

**Decision:**  
When a platform concept has both a platform-owned mechanism and a deployer-authored instance surface, the mechanism and the instance are separate ledger concepts.

- Mechanism: platform-fixed construct, contract, grammar, protocol, closed vocabulary, or platform-owned registry.
- Instance: deployer-authored configuration shipped in a configuration package.

**Rationale:**  
A single classification hides either platform closure or deployer authoring. Scope, pattern, and activity all require this split: their mechanisms are platform controlled, while concrete deployed instances are authored by deployers.

**Rejected alternatives:**

- Classifying the whole concept by whichever side seems more important.
- Treating platform-fixed mechanisms as deployer configuration.
- Treating deployer-authored instances as platform mechanisms.

**Binding constraints:**

- Scope mechanism and scope instances are separate concepts.
- Pattern mechanism and pattern bindings/instances are separate concepts.
- Activity instance and `activity_ref` contract are separate concepts.
- Any future dual concept must be split the same way.

**Guardrails:**

- Do not collapse mechanism into instance.
- Do not let configuration redefine platform-owned semantics.

**Must not happen:**

- A security-critical platform mechanism must not become deployer-authored logic by classification accident.

**Scope boundary:**  
This is a classification and modeling rule. It does not decide each individual mechanism’s implementation.

**Downstream impact:**  
Future ledger rows and specs must classify mechanism and instance surfaces separately.

---

## 3. Canonical event envelope

### CDL-006: Canonical event envelope has eleven fields

**Status:** Accepted  
**Classification:** Structural contract

**Decision:**  
The canonical event envelope for the initial platform version contains exactly these conceptual fields:

```text
id
type
shape_ref
activity_ref
subject_ref
actor_ref
device_id
device_sequence
sync_watermark
device_time
payload
```

Implementation casing may vary, but the conceptual contract is fixed.

**Rationale:**  
The envelope carries the minimum permanent metadata needed for identity, causality, schema interpretation, activity correlation, authorship, audit, and payload validation without storing derived authority, workflow state, pattern membership, sensitivity, or scope.

**Rejected alternatives:**

- Adding `authority_context` to the envelope.
- Adding a top-level `assignment_ref` as authority context.
- Adding `pattern_ref`.
- Adding `workflow_state` or `current_state`.
- Adding `scope` or `sensitivity` fields.

**Binding constraints:**

- Required fields: `id`, `type`, `shape_ref`, `subject_ref`, `actor_ref`, `device_id`, `device_sequence`, `sync_watermark`, `device_time`, `payload`.
- Optional field: `activity_ref`.
- Derived concepts stay outside the envelope.

**Guardrails:**

- Add no envelope field merely because a projection or configuration lookup uses that concept.
- Treat envelope changes as architecture-grade.

**Must not happen:**

- The envelope must not become a dump of every runtime derivation.

**Scope boundary:**  
This row defines the event envelope. It does not define payload schemas; payload schema identity is carried by `shape_ref`.

**Downstream impact:**  
All clients, servers, sync processors, validators, projectors, and import/export tools must read and write this envelope consistently.

---

### CDL-007: Envelope `type` is a closed six-value vocabulary

**Status:** Accepted  
**Classification:** Structural invariant / closed vocabulary

**Decision:**  
Allowed `type` values are exactly:

```text
capture
review
alert
task_created
task_completed
assignment_changed
```

The vocabulary is closed. Adding another value is architecture-grade and requires a new platform decision. It is not a deployer option and not a normal extension surface.

**Rationale:**  
`type` selects the platform processing pipeline. Domain facts and shape-specific meaning are handled by `shape_ref`, not by expanding the pipeline vocabulary.

**Rejected alternatives:**

- Deployment-defined event types.
- Domain-specific structural types.
- `status_changed` as a seventh type.
- `escalation`, `register`, or `verify` as structural types.
- Using identity/integrity domain fact names as `type` values.

**Binding constraints:**

- Pipeline routing uses `type`.
- Domain discrimination uses `shape_ref`.
- Existing type values are never renamed or removed.
- No code path may emit a type outside the six-value set.

**Guardrails:**

- Add a type only when platform processing behavior is genuinely new.
- Express domain meaning through platform-bundled or deployer-defined shapes.

**Must not happen:**

- `conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`, or `status_changed` must not appear as envelope `type` values.

**Scope boundary:**  
This row governs the envelope `type` field only. It does not limit deployer-defined shapes.

**Downstream impact:**  
Consumers that need domain facts must filter by `shape_ref`; consumers that need pipeline routing may filter by `type`.

---

### CDL-008: `shape_ref` identifies payload schema and domain fact

**Status:** Accepted  
**Classification:** Structural contract

**Decision:**  
Every event carries mandatory `shape_ref` in the format:

```text
{shape_name}/v{version}
```

`shape_name` matches `[a-z][a-z0-9_]*`. `version` is a monotonically increasing positive integer.

**Rationale:**  
Events remain interpretable even when schemas evolve. The event permanently declares the shape version active when the payload was created. Shape references also distinguish domain facts within the closed six-type pipeline vocabulary.

**Rejected alternatives:**

- Unversioned shape references.
- Latest-schema interpretation of old payloads.
- Self-describing payloads as the primary schema mechanism.
- Domain meaning encoded in `type`.
- Server-hidden schema metadata.

**Binding constraints:**

- Every event has exactly one `shape_ref`.
- Shape versions remain available for historical interpretation.
- Projection logic handles coexistence across shape versions.
- Shape names are constrained so parsing is unambiguous.

**Guardrails:**

- Never reinterpret old events under a newer shape version.
- Do not use event `type` to identify domain meaning.

**Must not happen:**

- Events must not be stored without schema identity and version.
- A new shape version must not invalidate historical events.

**Scope boundary:**  
This row defines the event’s schema/domain reference. The authoring format and registry storage implementation can evolve separately.

**Downstream impact:**  
Shape registry, validators, projectors, imports, sync, and analytics must all resolve payload meaning through `shape_ref`.

---

### CDL-009: `activity_ref` is an optional contract field

**Status:** Accepted  
**Classification:** Structural contract

**Decision:**  
Events may carry `activity_ref`, an optional deployer-chosen identifier matching `[a-z][a-z0-9_]*`, or `null`. It references an activity instance, not an activity template or pattern.

When an event is captured within an activity context, the system auto-populates `activity_ref`. Events with no honest activity provenance may use `null`.

**Rationale:**  
Same-shape events may belong to different activities, campaigns, routines, or reporting contexts. `activity_ref` disambiguates those cases without forcing import tools or system events to fabricate provenance.

**Rejected alternatives:**

- Mandatory `activity_ref` for every event.
- No `activity_ref` field.
- Inferring activity from actor, timestamp, subject, or scope.
- `pattern_ref` as the activity/workflow discriminator.

**Binding constraints:**

- Human-authored events captured in activity UI are normally stamped with activity context.
- Imported or loose historical events may use `null`.
- Trigger outputs inherit activity context from their source when applicable.
- Activity identity is separate from shape identity and pattern identity.

**Guardrails:**

- `null` means unknown or not applicable, not infer freely.
- Same-shape multi-activity configurations require deploy-time warnings or validation.

**Must not happen:**

- Reports must not infer campaign or routine attribution from timestamp alone.
- Import tooling must not fabricate activity provenance.

**Scope boundary:**  
This row defines the reference field. The activity instance itself is deployer configuration and is classified separately.

**Downstream impact:**  
Projection, reporting, pattern mapping, and cross-activity linking can use `activity_ref` when context exists.

---

### CDL-010: Causal metadata is structural and device-scoped

**Status:** Accepted  
**Classification:** Structural contract

**Decision:**  
Every event carries `device_id`, `device_sequence`, and `sync_watermark`.

- `device_id` identifies a physical device or app installation, not a user account.
- `device_sequence` is a durable, monotonically increasing counter scoped to `device_id`.
- `sync_watermark` records the server state known when the event was created.
- Causal comparison is per subject; unrelated subjects are not causally ordered against each other.

**Rationale:**  
Offline devices need a causality model that does not depend on wall-clock time. Device-local sequence gives total order within one device. Sync watermark enables cross-device concurrency detection.

**Rejected alternatives:**

- Wall-clock ordering.
- Server-assigned sequence for offline events.
- In-memory or reset-on-restart sequence counters.
- Account-scoped `device_id`.
- Global cross-subject ordering.

**Binding constraints:**

- `(device_id, device_sequence)` is never reused.
- `device_sequence` increments before the event write.
- Sequence persistence survives reboot, restart, and crash.
- If persistence is lost, the device must re-provision with a new `device_id` before creating new events.

**Guardrails:**

- Actor identity and device identity remain separate.
- Shared devices require actor/session partitioning, not device ID reuse.

**Must not happen:**

- Two devices must not emit the same `(device_id, device_sequence)` pair.
- Reboot or reinstall must not cause sequence reuse under the same device namespace.

**Scope boundary:**  
This defines causal metadata. It does not define sync transport implementation.

**Downstream impact:**  
Conflict detection, workflow state derivation, and audit views use this metadata for causality and ordering.

---

### CDL-011: `device_time` is advisory only

**Status:** Accepted  
**Classification:** Structural guardrail

**Decision:**  
`device_time` is display and audit metadata. Ordering, causality, conflict detection, projection correctness, and protocol correctness must not depend on `device_time`.

**Rationale:**  
Device clocks are unreliable. If correctness depends on device time, clock drift can permanently corrupt ordering in immutable records.

**Rejected alternatives:**

- Ordering by wall clock.
- Conflict detection by event timestamp.
- Projection correctness based on timestamp order.

**Binding constraints:**

- Use `device_sequence` for intra-device ordering.
- Use `sync_watermark` for cross-device concurrency detection.
- Show implausible timestamps as audit/display anomalies, not as ordering truth.

**Guardrails:**

- Display ordering may use time with caveats; correctness must not.
- Never treat clock accuracy as a protocol invariant.

**Must not happen:**

- A bad device clock must not cause permanent logical misordering.

**Scope boundary:**  
This row does not prohibit displaying timestamps or using them in human-facing audit trails.

**Downstream impact:**  
All protocol and projection logic must avoid wall-clock dependence for correctness.

---

### CDL-012: No derived runtime concept is stored as an envelope field

**Status:** Accepted  
**Classification:** Structural guardrail

**Decision:**  
The envelope does not contain `authority_context`, top-level `assignment_ref` as authority context, `pattern_ref`, `workflow_state`, `current_state`, `status_changed`, `scope`, or `sensitivity`.

**Rationale:**  
These concepts are derivable from assignment timelines, configuration, pattern mappings, projections, or policy. Storing them in every event would over-commit the envelope and create second sources of truth.

**Rejected alternatives:**

- Storing authorization claims in each event.
- Storing pattern membership in each event.
- Storing current workflow state in each event.
- Storing sensitivity or scope directly in the envelope.

**Binding constraints:**

- Authority is reconstructed from assignments.
- Pattern membership is derived from shape/activity configuration.
- Workflow state is projection-derived.
- Sensitivity is configuration metadata.
- Scope is evaluated by platform access mechanisms.

**Guardrails:**

- Do not add envelope fields for values that can be derived safely.
- Treat envelope growth as architecture-grade.

**Must not happen:**

- Implementation must not smuggle derived concepts into the envelope under alternative field names.

**Scope boundary:**  
Derived values may be materialized in projections or read models. They are not part of the canonical event envelope.

**Downstream impact:**  
Specs must place authority, pattern, workflow, scope, and sensitivity logic outside the event envelope.

---

## 4. Closed vocabularies and platform-bundled shapes

### CDL-013: Domain discrimination uses `shape_ref`, not `type`

**Status:** Accepted  
**Classification:** Binding rule

**Decision:**  
Any code, spec, or agent that needs to identify the domain fact recorded by an event must inspect `shape_ref`, not `type`.

**Rationale:**  
`type` has only six pipeline values. Many domain facts share the same pipeline. For example, a data capture, identity merge, and automated resolution can all use `capture` while recording different domain facts through different shapes.

**Rejected alternatives:**

- Filtering identity or integrity events by `type`.
- Creating a new structural `type` for each domain fact.
- Inferring domain meaning from payload shape without `shape_ref`.

**Binding constraints:**

- Pipeline routing uses `type`.
- Domain discrimination uses `shape_ref`.
- Authorship discrimination uses `actor_ref`.

**Guardrails:**

- Keep pipeline, domain fact, and authorship orthogonal.

**Must not happen:**

- A consumer must not identify `conflict_detected/v1`, `subjects_merged/v1`, or other domain facts by envelope `type` alone.

**Scope boundary:**  
This governs event discrimination. It does not prohibit grouping by `type` for pipeline operations.

**Downstream impact:**  
Event consumers, projections, analytics, and sync processors must use `shape_ref` for domain-specific filtering.

---

### CDL-014: Platform-bundled integrity and identity shapes are contracts

**Status:** Accepted  
**Classification:** Platform contract

**Decision:**  
The platform registers and owns these integrity and identity shapes:

| Domain fact                            | Envelope `type` | `shape_ref`            | Authoring actor                      |
|----------------------------------------|-----------------|------------------------|--------------------------------------|
| Integrity detector raises a flag       | `alert`         | `conflict_detected/v1` | system detector actor                |
| Human reviewer resolves a flag         | `review`        | `conflict_resolved/v1` | human resolver                       |
| Auto-resolution policy resolves a flag | `capture`       | `conflict_resolved/v1` | `system:auto_resolution/{policy_id}` |
| Identity merge performed               | `capture`       | `subjects_merged/v1`   | authorized human or system actor     |
| Identity split performed               | `capture`       | `subject_split/v1`     | authorized human actor               |

These are not deployer-authored shapes and are not envelope `type` values.

**Rationale:**  
Integrity, resolution, and identity evolution are platform facts that must be represented on the same event stream as all other operational facts while preserving the closed type vocabulary.

**Rejected alternatives:**

- Extra envelope type values for each platform domain fact.
- Deployer-authored integrity/identity shapes.
- Hidden non-event surfaces for platform facts.

**Binding constraints:**

- Platform registers these shapes.
- Deployers cannot redefine or deprecate them.
- Consumers filter by `shape_ref`.
- The same `conflict_resolved/v1` domain fact may be authored manually or by policy; authorship determines pipeline type.

**Guardrails:**

- Do not let platform-owned domain facts drift into deployer schema space.
- Do not add extra type values for these facts.

**Must not happen:**

- `conflict_detected`, `conflict_resolved`, `subjects_merged`, or `subject_split` must not be emitted as envelope `type` values.

**Scope boundary:**  
This row lists currently fixed platform-bundled integrity and identity shapes. Other platform-bundled shapes may exist where platform mechanisms require them, but they must use the same `shape_ref` model.

**Downstream impact:**  
Flag queues, identity projectors, resolution workflows, analytics, and sync processors must treat these as platform contracts.

---

### CDL-015: Deterministic flag identity includes source, shape, and category

**Status:** Accepted  
**Classification:** Algorithmic contract

**Decision:**  
A deterministic flag event identity is derived from the source event id, the flag event `shape_ref`, and the flag category, under a named platform namespace/algorithm.

```code
flag_event_uuid = deterministic_uuid(
  algorithm = DATARUN_FLAG_ID_ALGORITHM_V1,
  namespace = DATARUN_FLAG_NS,
  name = source_event_id + "|" + flag_shape_ref + "|" + flag_category
)
```

**Rationale:**  
The same anomaly detected more than once should resolve to the same flag event identity. Including `shape_ref` prevents future platform-bundled flag shapes from colliding when they use the same source event and category.

**Rejected alternatives:**

- Random flag IDs for deterministic detector output.
- Hashing only source event and category.
- Mutable flag records keyed outside the event stream.

**Binding constraints:**

- Detector-created flag events use deterministic identity.
- `shape_ref` participates in identity derivation.
- Duplicate detection of the same anomaly is idempotent.

**Guardrails:**

- Do not create multiple unresolved flags for the same source/shape/category anomaly.

**Must not happen:**

- Re-running detection must not create duplicate flag events for the same anomaly.

**Scope boundary:**  
This governs deterministic flag creation. Human-authored resolution events use normal event identity semantics.

**Downstream impact:**  
Conflict detection, sync idempotency, and flag queues rely on deterministic flag identity.

---

## 5. Reference contracts

### CDL-016: Reference fields are contracts; referents are separate concepts

**Status:** Accepted  
**Classification:** Binding rule

**Decision:**  
A reference field and the object it points to are different ledger concepts. `subject_ref`, `actor_ref`, and `activity_ref` are envelope contracts. Subject, actor, assignment, process, and activity instances are classified separately according to their own semantics.

**Rationale:**  
Conflating a reference field with its referent causes classification errors. A field can be a platform contract while the referenced instance is deployer configuration or a domain concept.

**Rejected alternatives:**

- Classifying `activity_ref` as config because activity is config.
- Classifying `subject_ref` as a domain concept because Subject is a domain object.
- Treating reference grammar and referent lifecycle as the same decision.

**Binding constraints:**

- Reference field classification is based on envelope contract.
- Referent classification is based on the object being referenced.
- Future references must maintain this split.

**Guardrails:**

- Do not infer referent semantics from field classification.
- Do not infer field contract status from referent ownership.

**Must not happen:**

- A contract field must not become deployer-defined merely because its values refer to deployer-configured instances.

**Scope boundary:**  
This is a classification rule. Individual reference contracts are defined in adjacent rows.

**Downstream impact:**  
Specs and agents must classify `*_ref` rows separately from the referenced domain objects.

---

### CDL-017: `subject_ref` is a typed reference contract

**Status:** Accepted  
**Classification:** Structural contract

**Decision:**  
`subject_ref` is a typed reference of the form:

```text
{ type, id }
```

The `id` is a client-generated UUID. The `type` enum is closed at four values:

```text
subject
actor
process
assignment
```

`process` is reserved for future process/workflow-instance references and has no current emission site unless activated by a platform decision.

**Rationale:**  
Untyped UUID references become ambiguous as the platform references subjects, actors, processes, and assignments. Retrofitting type discrimination later would require reinterpreting stored events.

**Rejected alternatives:**

- Untyped UUID references.
- Inferring type from payload or event type.
- Separate incompatible reference mechanisms for each identity kind.

**Binding constraints:**

- Stored references carry type and id.
- Reference type enum is closed; extension is architecture-grade.
- UUID uniqueness is not real-world identity uniqueness.

**Guardrails:**

- Do not parse identity kind from shape name, event type, or payload context.
- Do not rely on UUID namespace alone for semantics.

**Must not happen:**

- A stored reference like `abc-123` must not require later reinterpretation to determine what it points to.

**Scope boundary:**  
This defines the reference contract. Subject, actor, process, and assignment object lifecycles are separate concepts.

**Downstream impact:**  
Identity resolution, authorization, projections, workflow, and sync filters use typed references consistently.

---

### CDL-018: `actor_ref` records human or system authorship

**Status:** Accepted  
**Classification:** Structural contract

**Decision:**  
`actor_ref` identifies who or what authored the event.

Human actors use UUID identity. System actors use:

```text
system:{source_type}/{source_id}
```

`source_type` is platform-evolvable. Current examples include `trigger` and `auto_resolution`. Consumers distinguish system authorship by the literal `system:` prefix.

**Rationale:**  
Every event must have traceable authorship, including system-generated events. System author types must grow as platform mechanisms grow without requiring a new envelope field.

**Rejected alternatives:**

- Anonymous system events.
- Separate `system_actor` envelope field.
- Closing `source_type` so every new system source requires architecture change.

**Binding constraints:**

- Every event has `actor_ref`.
- Human and system authorship share the same envelope field.
- `system:` prefix is the discriminator.
- `source_id` is stable within its source type.

**Guardrails:**

- Do not bypass provenance for generated events.
- Do not infer authorship from `type`.

**Must not happen:**

- System-generated events must not be anonymous.

**Scope boundary:**  
This defines authorship representation. It does not define authorization eligibility.

**Downstream impact:**  
Audit, resolution validity, analytics, and consumer routing can distinguish human and system-authored records.

---

## 6. Storage and sync model

### CDL-019: Atomic write unit is a typed immutable event

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
The atomic write unit is a typed immutable event. Each event records what happened with action-specific payload. Current state is computed by projection.

**Rationale:**  
Events represent captures, corrections, reviews, transfers, assignments, identity changes, flags, and resolutions naturally. Full-state snapshots duplicate data and make workflow transitions unnatural.

**Rejected alternatives:**

- Full immutable snapshots as primary model.
- Mutable current-state rows as primary model.
- Unified action log with co-primary materialized views.

**Binding constraints:**

- Every state change enters through the event store.
- Events are action-specific.
- Projections are derived.

**Guardrails:**

- Do not model workflow as repeated full-state replacement.
- Do not create dual-write truth between log and view.

**Must not happen:**

- State change must not bypass event creation.

**Scope boundary:**  
This row defines write granularity, not the full projection implementation.

**Downstream impact:**  
All downstream platform subsystems operate over immutable events.

---

### CDL-020: Client-generated UUIDs identify events, subjects, and records

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
Events, subjects, and records use client-generated UUIDs. Devices can mint identifiers offline without server coordination.

**Rationale:**  
Offline devices must create records and register subjects without a network roundtrip. Server-assigned IDs block offline creation; preallocated pools can be exhausted.

**Rejected alternatives:**

- Server-allocated sequential IDs.
- Preallocated ID pools.
- Treating UUID uniqueness as real-world deduplication.

**Binding constraints:**

- Offline record creation does not require server contact.
- Offline subject creation does not require server contact.
- Duplicate real-world subjects are resolved by identity mechanisms, not ID allocation.

**Guardrails:**

- Do not confuse identifier uniqueness with real-world identity uniqueness.
- Do not push fuzzy matching into storage ID generation.

**Must not happen:**

- A device must not be unable to create records because an ID pool is exhausted.

**Scope boundary:**  
This governs identifier allocation. It does not decide duplicate-subject resolution semantics.

**Downstream impact:**  
Identity resolution must tolerate duplicate real-world entities with different UUIDs.

---

### CDL-021: Sync transfers immutable events idempotently and by scope

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
Sync transfers immutable events the receiver has not yet seen, filtered by the receiver’s sync/access scope. Sync is idempotent, append-only, and order-independent at the transport layer.

**Rationale:**  
Immutable event sync allows devices and servers to exchange changes safely under intermittent connectivity. Re-receiving the same event is harmless, and the server never instructs devices to rewrite canonical event history.

**Rejected alternatives:**

- Syncing mutable current state as truth.
- Full dataset sync to every device.
- Server-side rewrite of device event history.
- Transfer of complete state instead of event changes.

**Binding constraints:**

- Events are the sync unit.
- Duplicate receipt is a no-op.
- Sync does not delete or modify stored events.
- Payload is filtered by access/sync scope.

**Guardrails:**

- Transport arrival order must not define logical order.
- Device datasets remain scoped.

**Must not happen:**

- A device must not be instructed to mutate canonical events during normal sync.

**Scope boundary:**  
This defines canonical sync unit and semantics. It does not define transport protocol, batching, retries, or compression.

**Downstream impact:**  
Selective sync, authorization, conflict detection, and offline projections operate on event availability.

---

## 7. Identity and conflict

### CDL-022: Duplicate real-world identity is resolved by identity mechanisms

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
If two offline devices create different UUIDs for the same real-world thing, both UUIDs remain valid stored identities. Duplicate detection, merge, and split are handled by identity mechanisms. Storage does not silently collapse duplicates.

**Rationale:**  
Client-generated UUIDs solve identifier allocation but not real-world identity matching. Duplicate subjects are expected under offline operation.

**Rejected alternatives:**

- Preventing duplicates through server-assigned IDs.
- Treating UUID collision prevention as subject deduplication.
- Silently merging likely duplicates.

**Binding constraints:**

- Duplicate subjects can coexist until resolved.
- Merge/split are additive events.
- Historical references remain unchanged.

**Guardrails:**

- Do not rewrite historical references when identities merge.
- Do not make storage responsible for fuzzy identity matching.

**Must not happen:**

- Duplicate subjects must not be silently collapsed.

**Scope boundary:**  
This governs duplicate real-world identity. It does not define matching algorithms or resolver UX.

**Downstream impact:**  
Projections, assignment, authorization, and workflow must tolerate unresolved duplicate identities.

---

### CDL-023: Identity merge is alias-in-projection, never re-reference

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
An identity merge is recorded as `shape_ref = subjects_merged/v1`. It creates an alias mapping from retired identity to surviving identity. No existing event is modified. Projections resolve aliases for read purposes.

**Rationale:**  
Historical events record what was known and referenced at the time. Rewriting them would destroy provenance and violate append-only semantics.

**Rejected alternatives:**

- Rewriting historical references to the surviving identity.
- Treating alias-resolved identity as if it had always been the only identity.
- Physical re-reference during merge.

**Binding constraints:**

- Merge records an alias event.
- Historical subject references remain as written.
- Projections may resolve retired IDs to survivors for reads.
- Transitive alias lookup is single-hop after closure update.

**Guardrails:**

- Do not normalize references before anomaly detection.
- Do not hide retired-ID provenance.

**Must not happen:**

- Merge must not mutate historical events.

**Scope boundary:**  
This defines merge semantics, not matching algorithms or merge UI.

**Downstream impact:**  
Conflict detection, authorization, sync, and projections must preserve raw references while offering alias-resolved reads.

---

### CDL-024: Split freezes history and archives the source

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
An identity split is recorded as `shape_ref = subject_split/v1`. The source identity becomes terminally archived. Historical events remain attributed to the source. New events go to successor identities. Post-split events against the archived source are accepted and flagged for attribution.

**Rationale:**  
Splitting cannot retroactively determine which successor every historical event belonged to. Preserving the source keeps history truthful and avoids destructive reclassification.

**Rejected alternatives:**

- Re-attributing historical events automatically.
- Reactivating the source after split.
- Rejecting all later references to archived sources.

**Binding constraints:**

- Split archives source identity.
- Archived source is terminal.
- Successor identities receive new events.
- Historical events stay with source.

**Guardrails:**

- Do not claim automatic attribution for pre-split history.
- Treat post-split stale references as flags, not rejection.

**Must not happen:**

- Historical events must not be rewritten into successors.

**Scope boundary:**  
This defines split identity semantics. Manual re-attribution workflows, if any, are optional workflow behavior.

**Downstream impact:**  
Projection, sync, and workflow must represent archived source and successor lineage explicitly.

---

### CDL-025: No unmerge operation exists

**Status:** Accepted  
**Classification:** Structural constraint / rejected alternative

**Decision:**  
The platform does not define an unmerge operation. A wrong merge is corrected by splitting the surviving identity and creating a successor for the wrongly merged entity.

**Rationale:**  
After a merge, new events may reference the surviving identity. A later unmerge cannot automatically decide which post-merge events belong to which identity without rewriting or guessing.

**Rejected alternatives:**

- First-class unmerge operation.
- Deleting or reversing a merge event.
- Automatic post-merge re-attribution.

**Binding constraints:**

- Merge correction uses split.
- Post-merge events remain attributed to the surviving identity unless an explicit later workflow handles attribution.
- No inverse merge event is part of the platform vocabulary.

**Guardrails:**

- Do not introduce inverse operations that require rewriting history.
- Do not promise automatic attribution after a bad merge.

**Must not happen:**

- An unmerge operation must not exist as a platform event shape or workflow construct.

**Scope boundary:**  
Manual review or domain-specific attribution workflow may exist, but it does not rewrite merge history.

**Downstream impact:**  
Identity correction tooling must use corrective split, not inverse merge.

---

### CDL-026: Identity lineage is acyclic by construction

**Status:** Accepted  
**Classification:** Structural invariant

**Decision:**  
Identity lineage is a directed acyclic graph enforced at write time.

- Merge operands must be active.
- Merge survivor must be active.
- Split archives the source.
- Archived identities cannot become active, be merged into, or be split again.

**Rationale:**  
If cyclic lineage is written into the immutable event stream, projections cannot repair it without ignoring canonical facts.

**Rejected alternatives:**

- Allowing arbitrary lineage events and repairing in projection.
- Reactivating archived identities.
- Merging successors back into archived sources.

**Binding constraints:**

- Write-time aggregate validation prevents cycles.
- Archived is terminal for lineage operations.
- Lineage graph remains a DAG.

**Guardrails:**

- Validate lineage before appending identity events.
- Treat acyclicity as event-store integrity, not display logic.

**Must not happen:**

- A lineage cycle must not be constructable.

**Scope boundary:**  
This governs identity lineage. It does not govern non-identity domain relationships.

**Downstream impact:**  
Alias tables, projections, sync, and resolver tools can assume acyclic lineage.

---

### CDL-027: Merge and split are online-only operations

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
Identity merge and identity split require server-validated online transactions.

**Rationale:**  
Merge/split affect global identity lineage and require authoritative validation against current lineage state. Offline identity restructuring can create cycles, stale operations, or inconsistent successor generation.

**Rejected alternatives:**

- Offline merge.
- Offline split.
- Device-only lineage validation.

**Binding constraints:**

- Server validates operands and lifecycle state.
- Server records lineage operation only after validation.
- Devices receive lineage events through sync.

**Guardrails:**

- Do not allow stale device projections to author global lineage changes.

**Must not happen:**

- Offline devices must not create identity lineage operations independently.

**Scope boundary:**  
This governs identity restructuring. Normal offline captures against existing identities remain allowed.

**Downstream impact:**  
Identity resolver UI and APIs must require connectivity for merge/split.

---

### CDL-028: Conflict resolution is single-writer and server-validated

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
A flag raised by `shape_ref = conflict_detected/v1` designates exactly one resolver. A manual `shape_ref = conflict_resolved/v1` resolution is canonical only if authored by that resolver through a server-validated transaction. Unauthorized resolution attempts may be accepted and flagged, but they are not canonical.

**Rationale:**  
Without a designated resolver and online validation, two offline reviewers can create contradictory immutable resolutions with no termination rule.

**Rejected alternatives:**

- Multiple canonical resolutions.
- First-sync-wins.
- Highest-authority-wins without explicit resolver designation.
- Offline human conflict resolution by default.

**Binding constraints:**

- Flag payload identifies resolver.
- Manual resolution requires server validation.
- Only designated resolver’s resolution is canonical.
- Unauthorized resolution attempts are visible but non-canonical.

**Guardrails:**

- Do not resolve by sync arrival order.
- Do not let conflict resolution create unbounded meta-conflict recursion.

**Must not happen:**

- Two resolution events must not both be canonical for one flag.

**Scope boundary:**  
This governs manual resolution. Auto-resolution follows the L3b policy rules and emits the same resolution shape through system authorship.

**Downstream impact:**  
Resolver queues, authorization, audit, and projections must enforce single-writer validity.

---

### CDL-029: Conflict detection uses raw references before alias projection

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
Conflict detection evaluates incoming events using original references as written. Alias resolution happens afterward in projection and read models.

**Rationale:**  
If alias resolution runs first, stale actions against retired identities can be silently absorbed into surviving identities, losing evidence that the actor acted against stale identity context.

**Rejected alternatives:**

- Alias-first conflict detection.
- Treating retired-ID references as already canonical.
- Hiding stale reference context inside projections.

**Binding constraints:**

- Detector sees raw `subject_ref`.
- Flags preserve original reference context.
- Projection may resolve aliases for reads.

**Guardrails:**

- Do not normalize identity references before anomaly detection.
- Do not let projections erase conflict provenance.

**Must not happen:**

- A stale event against a retired identity must not disappear into the survivor without a flag.

**Scope boundary:**  
This governs detector input. It does not prohibit alias-resolved read views.

**Downstream impact:**  
Conflict detection, authorization, audit, and identity projections must retain raw-reference awareness.

---

## 8. Authorization and selective sync

### CDL-030: Access is assignment-based

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
Access reduces to: the actor has an active assignment whose scope contains the target entity and whose role permits the intended action.

**Rationale:**  
Roles alone are too broad, and generic attributes are too unconstrained. Assignment provides a typed temporal grant binding actor, role/capability, scope, and validity period.

**Rejected alternatives:**

- Pure RBAC without scope.
- Pure geographic scope without role/capability.
- Device-only authorization.
- Hard-coded per-activity access logic.

**Binding constraints:**

- Assignments define who can act, where, and under which role/capability.
- Role-action compatibility is checked server-side.
- Assignment state participates in sync and authorization projections.

**Guardrails:**

- Do not collapse role and scope into one field.
- Do not create workflow-local authorization independent of assignments.

**Must not happen:**

- A worker must not gain access solely because data is locally present.

**Scope boundary:**  
This defines the authorization model. Concrete roles and permitted actions are configuration within platform rules.

**Downstream impact:**  
Sync, command validation, workflow role checks, and policy execution use assignment-derived authority.

---

### CDL-031: Sync scope equals access scope

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
A device receives exactly the data its current actor is authorized to access. Sync scope and access scope are the same boundary.

**Rationale:**  
Offline devices physically store synced data. Syncing broad data and hiding unauthorized records in UI is not adequate access control.

**Rejected alternatives:**

- Sync everything, hide in UI.
- Treat sync as performance-only and access as separate filtering.
- Deliver data first and enforce access later.

**Binding constraints:**

- Server computes sync payload from active assignments.
- Data outside access scope is not delivered as normal operation.
- Device UI filtering is not primary access control.

**Guardrails:**

- Treat sync filtering as a security boundary.
- Avoid broad local datasets on field devices.

**Must not happen:**

- Unauthorized data must not be placed on a device as normal design.

**Scope boundary:**  
This governs device data delivery. Server-side administrators or auditors may have separate authorized scopes.

**Downstream impact:**  
Device storage, projection completeness, sync filters, and access reviews depend on assignment-derived scope.

---

### CDL-032: Authority is projected, not stored in the envelope

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
The event envelope has no `authority_context` field. Authority is reconstructed from assignment timelines, actor identity, event metadata, causal metadata, and sync context.

**Rationale:**  
Authority is a projection over assignments and timing, not a permanent fact authored by the device. Storing a device-side authority assertion in every event over-commits the envelope and risks treating stale local belief as canonical.

**Rejected alternatives:**

- Single top-level `assignment_ref`.
- Variable-length `authority_context` assignment list.
- Device-authored authority claim as permanent event fact.

**Binding constraints:**

- Events do not store authority claims.
- Server reconstructs authority during sync/validation.
- Assignment history must be available to validate dependent work.

**Guardrails:**

- Do not validate only against current assignment state when event-time context matters.
- Do not treat upload order as authority truth.

**Must not happen:**

- A device’s belief about authority must not become canonical merely because it was written into an event.

**Scope boundary:**  
This does not prohibit projections that show authority context. It prohibits authority context as canonical envelope data.

**Downstream impact:**  
Sync ordering, assignment availability, authorization validation, and audit projections must support authority reconstruction.

---

### CDL-033: Authorization uses original subject scope, not post-merge scope

**Status:** Accepted  
**Classification:** Structural constraint

**Decision:**  
Authorization evaluates an event against the original `subject_ref` and scope context as written at event creation, not the post-merge surviving subject’s scope.

**Rationale:**  
A merge is identity resolution, not an authorization grant. Using survivor scope could turn a historically unauthorized action into an authorized one or expose data across scope boundaries.

**Rejected alternatives:**

- Authorizing historical work after alias resolution.
- Letting survivor scope determine authorization for retired-ID events.
- Treating scope-crossing merge as projection-only.

**Binding constraints:**

- Authorization uses raw subject reference.
- Merge projections preserve original scope context for authorization.
- Scope-crossing merges surface necessary flags or annotations.

**Guardrails:**

- Do not let identity projection erase authorization context.
- Do not authorize historical work against a subject the actor never had scope for.

**Must not happen:**

- A merge must not silently project authorized work into unauthorized scope.

**Scope boundary:**  
This governs authorization evaluation. Alias-resolved read views may still show merged subject state.

**Downstream impact:**  
Authorization, sync, resolver UI, and audit views must preserve original-reference scope context.

---

### CDL-034: Assignment creation enforces scope containment

**Status:** Accepted  
**Classification:** Strategy-protecting security constraint

**Decision:**  
An assignment creation command is server-validated so the new assignment scope is contained within the creating actor’s authorized scope.

```text
new_assignment.scope ⊆ creating_actor.assignment.scope
```

**Rationale:**  
Without containment, a compromised or misconfigured coordinator could grant broad access and cause the sync engine to deliver unauthorized data.

**Rejected alternatives:**

- Trusting coordinators to assign only within scope.
- Detecting privilege escalation after sync.
- Treating assignment escalation as a flag-only issue.

**Binding constraints:**

- Assignment creation is server-validated.
- Scope containment runs before the assignment change is accepted.
- Exceptions require explicit higher-authority role semantics.

**Guardrails:**

- Do not implement assignment creation as blind event append.
- Do not rely on review queues for privilege-escalation prevention.

**Must not happen:**

- An actor must not grant scope they do not possess.

**Scope boundary:**  
This governs assignment creation authority. It does not define the concrete UI for assignment management.

**Downstream impact:**  
Assignment APIs, sync, and access scope recomputation depend on this invariant.

---

### CDL-035: Authorization staleness is accepted, surfaced, and severity-controlled

**Status:** Accepted  
**Classification:** Initial strategy / strategy-protecting constraint

**Decision:**  
Work created under stale authorization state is accepted and flagged when anomalous. Flag severity controls whether the event blocks downstream policy or remains informational. Severity values and defaults are platform-governed; allowed deployment overrides occur within platform-defined rules.

**Rationale:**  
Offline devices may act under assignments that changed centrally while disconnected. Rejecting all stale-authority work loses evidence; ignoring it creates security and safety risks.

**Rejected alternatives:**

- Reject all stale-authorization work.
- Treat all stale-authorization work as valid without flagging.
- Let deployments invent unbounded severity semantics.

**Binding constraints:**

- Authorization anomalies produce flags.
- Blocking flags obey detect-before-act.
- Severity configuration does not redefine the meaning of the flag.

**Guardrails:**

- Do not let high-risk stale-role events flow unreviewed.
- Do not mutate source events to express severity.

**Must not happen:**

- Stale authorization must not be invisible.

**Scope boundary:**  
This governs handling of authorization drift. It does not define every deployment’s severity defaults.

**Downstream impact:**  
Flag queues, policy execution, projections, and deployment configuration must respect severity semantics.

---

### CDL-036: Projection location is tiered and evolvable

**Status:** Accepted  
**Classification:** Initial strategy

**Decision:**  
Projection location is tiered:

- Field workers use device-local projections from scoped events.
- Supervisors use hybrid local detail plus server-computed summaries.
- Coordinators use server-computed projections.

**Rationale:**  
Different roles have different data volumes, offline requirements, and visibility needs. Projection location can evolve without changing event structure.

**Rejected alternatives:**

- All projections only on device.
- All projections only on server.
- Treating projection location as event-schema decision.

**Binding constraints:**

- Device-local projections remain necessary for offline field work.
- Server projections support broader visibility.
- Projection freshness/staleness must be visible where relevant.

**Guardrails:**

- Do not make server summaries canonical over events.
- Do not hide projection freshness.

**Must not happen:**

- Supervisors or coordinators must not act on stale summaries without freshness metadata.

**Scope boundary:**  
This is an evolvable projection strategy, not an event contract.

**Downstream impact:**  
Client and server architecture must support multiple projection locations without changing event meaning.

---

### CDL-037: Scope contraction data handling is device retention policy

**Status:** Accepted  
**Classification:** Initial strategy / device policy boundary

**Decision:**  
When an actor's scope contracts, the sync engine does not mutate or delete canonical events. Device-side handling follows selective retention policy:

- The actor's own events remain retained for provenance.
- Other actors' events about now-out-of-scope subjects are candidates for device removal.
- Non-sensitive data may be retained where deployment policy allows.
- Sensitive personal data should use crash-safe, journaled selective purge.
- Retain-but-hide is not recommended for sensitive data because physically retained data remains accessible on compromised devices.

**Rationale:**  
Scope contraction changes what a device should continue to carry. It must not become a server instruction to alter event history. Device retention policy can vary by sensitivity and deployment risk without changing the event model.

**Rejected alternatives:**

- Server-directed deletion or mutation of event history.
- Retain-but-hide as the default for sensitive data.
- Treating scope contraction as an event-store deletion problem.
- Purging an actor's own historical events and losing provenance.

**Binding constraints:**

- Scope contraction never deletes canonical events from the server event stream.
- Device removal is a local retention policy, not an event-store mutation.
- Own events remain available for provenance unless a separate compliance mechanism is designed.
- Sensitive-data deployments should prefer selective purge over retain-but-hide.

**Guardrails:**

- Keep sync scope and local retention distinct.
- Do not treat UI hiding as sufficient protection for sensitive retained data.
- Device purge must be crash-safe and must not corrupt local sync state.

**Must not happen:**

- Scope contraction must not cause canonical event deletion.
- Sensitive out-of-scope data must not remain physically retained merely because the UI hides it.

**Scope boundary:**  
This governs device-side retention after scope contraction. It does not define regulatory erasure, field-level encryption, or server-side compliance deletion.

**Downstream impact:**  
Sync clients, device storage, sensitivity policy, and deployment configuration must distinguish local data retention from event-store truth.

---

## 9. Configuration boundary

### CDL-038: Configuration has a four-layer gradient and an L3-to-code ceiling

**Status:** Accepted  
**Classification:** Strategy-protecting boundary

**Decision:**  
Configuration is organized as:

```text
L0 Assembly
L1 Shape
L2 Logic
L3 Policy
```

Configuration stops at L3. Behavior beyond L3 requires platform code or a deliberate platform capability extension.

**Rationale:**  
The platform must be configurable without becoming an unbounded programming environment. A visible gradient lets simple deployments remain simple while advanced behavior has explicit ceilings.

**Rejected alternatives:**

- One flat “everything is metadata” layer.
- Full programming in configuration.
- Hard-coded operational activities.
- Letting L3 grow into arbitrary scripts.

**Binding constraints:**

- Simple capture should need only L0 + L1.
- L2 handles form-scoped logic.
- L3 handles bounded server-side policy.
- Beyond L3 is platform code.

**Guardrails:**

- Do not force simple capture into trigger/workflow configuration.
- Do not hide code-like behavior inside configuration.

**Must not happen:**

- Configuration must not become Turing-complete or an inner platform.

**Scope boundary:**  
This defines expressiveness boundaries, not authoring UI syntax.

**Downstream impact:**  
Configuration tooling, validation, and platform extension governance must reflect the gradient.

---

### CDL-039: Shapes are deployer-defined payload schemas with version coexistence

**Status:** Accepted  
**Classification:** Structural / strategy-protecting constraint

**Decision:**  
Deployers define shapes as typed payload schemas. Shapes evolve through explicit versions. Old and new shape versions coexist; events captured under old versions remain valid.

Default evolution is additive/deprecation-first. Breaking changes are exceptional and require explicit validation and acknowledgment.

**Rationale:**  
Deployments need to change data collection over time without invalidating offline work or historical records. Versioned shapes preserve interpretation while allowing growth.

**Rejected alternatives:**

- Schemaless payloads.
- Hard-coded data models.
- Removing or renaming fields as normal evolution.
- Treating old versions as invalid after a new version exists.

**Binding constraints:**

- Shape fields are typed.
- Shape validation runs on device and server.
- Historical shape versions remain interpretable.
- Projection logic handles version coexistence.

**Guardrails:**

- Prefer additive changes and deprecation over destructive mutation.
- Do not use latest schema to reinterpret old events.

**Must not happen:**

- Offline v1 work must not be rejected because v2 or v3 exists.

**Scope boundary:**  
This governs payload schemas. Platform-bundled shapes remain platform-owned contracts, not deployer-authored shapes.

**Downstream impact:**  
Form engines, validators, projections, imports, and analytics must support multiple shape versions.

---

### CDL-040: Shapes may be authored as deltas but are stored as full snapshots

**Status:** Accepted  
**Classification:** Initial strategy

**Decision:**  
Deployers may author a new shape version as a delta, but runtime shape registry entries are stored as complete snapshots.

**Rationale:**  
Delta authoring is ergonomic; full snapshots simplify device validation and avoid runtime dependency on parent shape chains.

**Rejected alternatives:**

- Storing only deltas.
- Requiring devices to reconstruct shape inheritance chains.
- Embedding shape definitions in every event.

**Binding constraints:**

- Every `shape_ref` resolves to a complete schema.
- Authoring format is distinct from runtime registry format.
- Devices can validate without unavailable parent deltas.

**Guardrails:**

- Do not confuse authoring convenience with canonical runtime representation.

**Must not happen:**

- Devices must not fail validation because a parent delta is unavailable.

**Scope boundary:**  
This is a runtime representation strategy, not a permanent authoring syntax.

**Downstream impact:**  
Config packaging and device registries should deliver complete schema snapshots.

---

### CDL-041: Configuration packages are atomic at sync

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
Configuration is delivered as an atomic package at sync. Devices apply a new package only after in-progress work under the previous configuration completes. Devices hold at most current and previous configuration versions for active work completion.

**Rationale:**  
Partial or mid-form configuration updates can invalidate work in progress or create inconsistent behavior across shapes, logic, triggers, and projection rules.

**Rejected alternatives:**

- Mid-form configuration mutation.
- Partial package delivery.
- Only latest config, breaking in-progress work.
- Unlimited obsolete config retention on device.

**Binding constraints:**

- Config package validates as a whole.
- Device applies packages atomically.
- Active work remains bound to its starting config.
- Runtime device retention is bounded to current + previous config.

**Guardrails:**

- Do not push inconsistent config subsets to field devices.
- Do not confuse limited device config retention with permanent shape registry retention.

**Must not happen:**

- Central config update must not invalidate a partially completed offline form.

**Scope boundary:**  
This governs sync-time config delivery. Authoring tools may support granular edits before package publication.

**Downstream impact:**  
Sync, device config storage, form sessions, and deploy-time validation must support atomic package semantics.

---

### CDL-042: L3 policy executes server-only

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
All L3 policy execution is server-only. This includes event-reaction policy and deadline/async policy. Devices use L2 logic for immediate form feedback, not L3 triggers.

**Rationale:**  
Device-side triggers create duplicate firing, divergent stale evaluation, and larger device engines. Server-only L3 keeps coordination effects authoritative and bounded.

**Rejected alternatives:**

- Device-side L3 trigger execution.
- Dual device/server trigger firing with deduplication.
- Device-created authoritative system events from stale local context.

**Binding constraints:**

- L3-generated events are server-created.
- Devices run form logic, local validation, event storage, and projections, not L3 trigger engines.
- Trigger outputs arrive through sync.

**Guardrails:**

- Use L2 for immediate warnings and form behavior.
- Use L3 for system-level coordination effects.

**Must not happen:**

- The same trigger must not fire once locally and once centrally.

**Scope boundary:**  
This does not prohibit future platform evolution to device-side pre-evaluation, but that is not current behavior and not a deployer knob.

**Downstream impact:**  
Trigger engine, scheduler, and generated event provenance remain server concerns.

---

### CDL-043: Expression language is bounded and non-programmatic

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
There is one bounded expression language with context-specific data access. It uses operators and field references only; it has no functions. Payload mapping uses static values and source references only. Projection rules are pure lookup tables.

**Rationale:**  
Expressions are needed for configuration, but functions, dynamic queries, transformations, and conditional projection algorithms push configuration toward programming.

**Rejected alternatives:**

- Function-rich expression language.
- Custom deployer-defined functions.
- Dynamic projection queries in expressions.
- Computed payload transformation logic.
- Conditional projection algorithms.

**Binding constraints:**

- L2 form expressions use approved form/entity/context scopes.
- L3 trigger expressions stay bounded.
- No functions or deployer-defined executable code.
- Payload mapping is constants + source references.
- Projection rules map values to values.

**Guardrails:**

- Treat adding helper functions as platform evolution, not casual syntax growth.
- Keep expressions inspectable.

**Must not happen:**

- Configuration specialists must not be writing hidden programs in expressions.

**Scope boundary:**  
This governs declarative configuration expressions, not platform code.

**Downstream impact:**  
Form engines, trigger engines, projection engines, and validators must enforce expression limits.

---

### CDL-044: Trigger and configuration complexity budgets are enforced at deploy time

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
Deploy-time validation enforces configuration consistency, dependency validity, trigger acyclicity, and hard complexity budgets.

Budgets:

```text
max 60 fields per shape
max 3 predicates per condition
max 5 triggers per event type
max 50 triggers per deployment
trigger DAG max path length 2
```

**Rationale:**  
Bounded configuration prevents hidden workflow engines, infinite trigger chains, and unmaintainable deployments. Deploy-time validation is the consistency gate; devices should not discover broken configuration at runtime.

**Rejected alternatives:**

- Runtime discovery of invalid configuration.
- Unlimited trigger chains.
- Trigger cycles.
- Soft-only complexity guidance.
- Partial package publication.

**Binding constraints:**

- Invalid references block deployment.
- Trigger graph is acyclic and depth-limited.
- Complexity budgets are hard unless changed by platform decision.
- Validation is whole-package.

**Guardrails:**

- Tooling should warn before limits are hit.
- Exceeding limits means decomposition or platform extension, not silent exception.

**Must not happen:**

- A field device must not receive a configuration package with broken references or cycles.

**Scope boundary:**  
This governs validation and platform limits. It does not define authoring UI details.

**Downstream impact:**  
Config tooling, deploy pipeline, and runtime devices depend on authoritative deploy-time validation.

---

### CDL-045: Domain uniqueness rules are shape-declared; resolution strategy is separate

**Status:** Accepted  
**Classification:** Initial strategy / boundary

**Decision:**  
Shapes may declare domain uniqueness constraints evaluated optimistically on device and authoritatively on server. Violations produce flag events. Domain-specific conflict resolution strategies are not part of uniqueness detection and require workflow/resolution policy.

**Rationale:**  
Deployment business rules such as “one visit per household per week” are not structural conflicts. They are domain constraints that can reuse the flag pipeline without changing the event model.

**Rejected alternatives:**

- Hard-coding domain conflicts into the structural conflict model.
- Letting deployers write arbitrary conflict detection code.
- Treating cross-event uniqueness as form-only validation.
- Automatically resolving domain conflicts inside uniqueness rules.

**Binding constraints:**

- Server check is authoritative.
- Device check is optimistic and may miss events not locally synced.
- Violations emit flags.
- Resolution policy remains separate.

**Guardrails:**

- Do not reject structurally valid offline work solely because a domain rule is violated.
- Do not smuggle workflow automation into shape uniqueness rules.

**Must not happen:**

- Domain uniqueness rules must not create a second anomaly representation outside flags.

**Scope boundary:**  
This decides detection, not automatic resolution or precedence logic.

**Downstream impact:**  
Shape registry, validation, conflict detection, and resolver workflows must support domain uniqueness flags.

---

### CDL-046: Sensitivity is shape/activity-level configuration

**Status:** Accepted  
**Classification:** Initial strategy

**Decision:**  
Sensitivity is configured at shape or activity level using three levels:

```text
standard
elevated
restricted
```

Sensitivity affects sync scope, retention policy, and audit level. It is not represented by a new event envelope field.

**Rationale:**  
Shape/activity-level sensitivity is coarse enough to be operationally tractable. Field-level sensitivity would require deeper payload, encryption, redaction, and compliance machinery.

**Rejected alternatives:**

- Field-level sensitivity in the initial model.
- Envelope-level sensitivity field.
- UI masking as the complete sensitivity model.

**Binding constraints:**

- Sensitivity metadata lives in configuration.
- Sync, retention, and audit may use sensitivity classification.
- Field-level regulatory handling is platform evolution.

**Guardrails:**

- Do not rely on hide-only behavior for restricted data on devices.
- Do not claim full regulatory erasure/de-identification from this mechanism alone.

**Must not happen:**

- Sensitive payload handling must not be implemented as ad hoc field rules.

**Scope boundary:**  
This does not solve full legal compliance, erasure, or encryption policy.

**Downstream impact:**  
Sync filters, retention policy, audit, and deployment configuration need sensitivity-aware behavior.

---

## 10. Workflow and state progression

### CDL-047: Workflow state is projection-derived, not stored or enforced by rejection

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
Workflow state is derived by projection from event history and pattern definitions. State is never stored as canonical current state in events. The platform does not reject offline events solely because they violate projected workflow state; it accepts and flags transition violations.

**Rationale:**  
Offline-first, append-only events, and accept-and-flag make rejection-based state machines incompatible with the platform. Derived state preserves evidence while surfacing invalid transitions.

**Rejected alternatives:**

- Enforced state machine that rejects invalid transitions.
- `current_state` or `workflow_state` stored in the envelope or payload as authoritative state.
- `status_changed` structural event type.
- Mutable workflow status table as source of truth.

**Binding constraints:**

- State machine evaluation is projection logic.
- Events remain accepted if structurally valid.
- Invalid transitions produce flags.
- `status_changed` is not a type value.

**Guardrails:**

- On-device command validation is advisory, not authoritative.
- State derivation must use event history and pattern rules.

**Must not happen:**

- Workflow must not become a mutable status column.

**Scope boundary:**  
This governs workflow state. It does not define every pattern skeleton.

**Downstream impact:**  
Projection engines, validators, workflow UI, and state-aware forms use derived state.

---

### CDL-048: `transition_violation` is a platform flag category

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
The detector evaluates incoming events against pattern-defined state-machine rules and emits a `transition_violation` flag category when an event is invalid under the current derived state.

**Rationale:**  
Workflow invalidity is a state anomaly distinct from identity, scope, stale reference, and concurrent state changes. It uses the same flag model and does not add an event type or envelope field.

**Rejected alternatives:**

- Reject invalid transitions.
- Treat transition violations as generic stale references.
- Add a workflow-specific structural event type.

**Binding constraints:**

- Transition flags use the normal flag representation.
- Source events remain stored.
- Detect-before-act applies.

**Guardrails:**

- Do not create a parallel workflow error stream.
- Do not let deployments invent arbitrary flag categories outside platform governance.

**Must not happen:**

- Transition invalidity must not be invisible.

**Scope boundary:**  
This defines the flag category, not every transition rule for every pattern.

**Downstream impact:**  
Conflict detection gains a workflow transition evaluation step.

---

### CDL-049: Pattern registry is platform-fixed; activities bind patterns

**Status:** Accepted  
**Classification:** Mechanism / config split

**Decision:**  
The platform provides a closed pattern registry: platform-owned workflow skeletons that deployers select and parameterize through activity configuration. Deployers do not author new pattern mechanisms.

**Rationale:**  
Patterns provide workflow state-machine skeletons without turning configuration into arbitrary state-machine authoring.

**Rejected alternatives:**

- Deployer-authored custom state machines.
- Hard-coded bespoke workflows per deployment.
- Pattern-free inference from shapes alone.

**Binding constraints:**

- Pattern mechanism is platform-fixed.
- Activity instances select and parameterize patterns.
- Adding a new pattern is platform evolution.
- Pattern roles and mappings are deployer configuration within platform pattern semantics.

**Guardrails:**

- Pattern parameters must not become a scripting language.
- Do not treat a concrete activity as a new pattern.

**Must not happen:**

- Deployers must not implement custom workflow engines in configuration.

**Scope boundary:**  
This defines pattern mechanism and activity binding. Exact production pattern catalog details belong to platform specification and platform releases, within this mechanism.

**Downstream impact:**  
Projection engine, configuration validation, and workflow UI use platform pattern definitions.

---

### CDL-050: Pattern composition is bounded and validated

**Status:** Accepted  
**Classification:** Initial strategy

**Decision:**  
Pattern composition follows these rules:

```text
1. One subject-level pattern per activity.
2. Event-level patterns compose freely.
3. Approval sub-flows are scoped to a specific submission event.
4. Cross-activity linking uses activity_ref or explicit references, not shared patterns.
5. Shape-to-pattern mapping is unique within an activity.
```

**Rationale:**  
Subject lifecycle state and event-level review/approval state are different state spaces. Composition remains tractable when those levels are distinct and shape ownership is unambiguous.

**Rejected alternatives:**

- Multiple competing subject-level state machines in one activity.
- Pattern nesting without scoping.
- Overlapping shape ownership by multiple patterns.
- Cross-activity spanning patterns.

**Binding constraints:**

- Deploy-time validation enforces subject-level and shape mapping constraints.
- Projection maintains subject-level and event-level state separately.
- Cross-activity relationships are explicit.

**Guardrails:**

- Do not let one event ambiguously advance two subject-level workflows.
- Keep review/approval state separate from subject lifecycle state.

**Must not happen:**

- Projection engine must not merge competing lifecycle state machines.

**Scope boundary:**  
This defines current composition rules. Future platform evolution may add pattern capabilities without changing stored events.

**Downstream impact:**  
Pattern registry, projection engine, and deploy-time validation must enforce composition.

---

### CDL-051: Source-only flagging is the workflow cascade model

**Status:** Accepted  
**Classification:** Initial strategy

**Decision:**  
Only the root-cause event receives a flag. Downstream contamination is computed through source-chain traversal in projections, not by propagating additional flags to every downstream event.

**Rationale:**  
Propagating flags multiplies resolver work without adding distinct decisions. Source-only flagging keeps the queue tied to root causes while preserving downstream visibility.

**Rejected alternatives:**

- Automatic flag propagation to all downstream events.
- Selective downstream propagation for state-changing events.
- Automatic invalidation of downstream events.

**Binding constraints:**

- Root flag is canonical decision point.
- Projection engine follows source references.
- Downstream views show upstream flagged status.
- Resolution of root flag updates computed downstream indicators.

**Guardrails:**

- Do not hide causal contamination.
- Do not explode the flag queue.

**Must not happen:**

- One root problem must not create dozens of redundant flags.

**Scope boundary:**  
This governs flag cascade representation. It does not define UI wording for downstream indicators.

**Downstream impact:**  
Projection engine must support source-chain traversal and downstream warning indicators.

---

### CDL-052: `context.*` is bounded form context

**Status:** Accepted  
**Classification:** Initial strategy

**Decision:**  
Form expressions may read a platform-fixed `context.*` namespace. Initial properties are:

```text
context.subject_state
context.subject_pattern
context.activity_stage
context.actor.role
context.actor.scope_name
context.days_since_last_event
context.event_count
```

Values are pre-resolved when the form opens, read-only, and static during form fill. Trigger expressions do not access `context.*`.

**Rationale:**  
Forms need workflow-aware behavior without granting expressions dynamic query capability. Pre-resolved context gives useful local facts while preserving expression bounds.

**Rejected alternatives:**

- No context namespace, forcing hidden fields.
- Dynamic projection queries in expressions.
- Deployer-defined custom context properties.
- Cross-subject or aggregate context access.

**Binding constraints:**

- Form context only.
- Platform-fixed vocabulary.
- Read-only and pre-resolved.
- No dynamic lookups.

**Guardrails:**

- Do not add arbitrary projection fields.
- Do not let `context.*` become a query language.

**Must not happen:**

- Deployers must not define `context.my_custom_field`.

**Scope boundary:**  
This defines initial context surface. Future context properties are platform evolution.

**Downstream impact:**  
Form engine and device projection cache must provide pre-resolved context safely.

---

### CDL-053: Auto-resolution is L3b policy and emits normal resolution events

**Status:** Accepted  
**Classification:** Initial strategy

**Decision:**  
Auto-resolution is a server-side L3b policy subtype. It observes eligible flags and emits a normal `shape_ref = conflict_resolved/v1` event. It does not mutate flags directly and does not bypass the event stream.

**Rationale:**  
Auto-resolution is asynchronous policy behavior. Reusing L3b avoids a separate engine while preserving auditability through normal event records.

**Rejected alternatives:**

- Separate auto-resolution engine.
- Auto-resolution as arbitrary deployer script.
- Direct mutation of flag state.
- Auto-resolution for every flag type.

**Binding constraints:**

- Runs server-side.
- Emits normal resolution event.
- Obeys trigger depth and L3b budgets.
- Human override remains possible where policy permits.

**Guardrails:**

- Do not bypass conflict resolution records.
- Do not create recursive resolution loops.

**Must not happen:**

- Auto-resolution must not silently mutate conflict state.

**Scope boundary:**  
This defines mechanism. Authoring UX for policies is tooling/specification.

**Downstream impact:**  
Trigger engine, flag queues, audit, and projection logic treat auto-resolution as normal event-stream behavior.

---

### CDL-054: Flag resolvability is platform-classified

**Status:** Accepted  
**Classification:** Strategy-protecting constraint

**Decision:**  
Each flag category has platform-defined resolvability:

```text
auto_eligible
manual_only
```

Initial classification:

| Flag category                 | Resolvability   |
| ----------------------------- | --------------- |
| `transition_violation`        | `auto_eligible` |
| `stale_reference`             | `auto_eligible` |
| `scope_violation`             | `manual_only`   |
| `identity_conflict`           | `manual_only`   |
| `concurrent_state_change`     | `manual_only`   |
| `domain_uniqueness_violation` | `manual_only`   |
| `role_stale`                  | `manual_only`   |
| `temporal_authority_expired`  | `auto_eligible` |
| *reserved*                    | *reserved*      |

Deployers may configure policy instances only within platform rules. They cannot make manual-only categories auto-resolvable.

**Rationale:**  
Some anomalies are safe to resolve automatically under bounded policy; others involve security, identity, or domain judgment.

**Rejected alternatives:**

- Deployer-controlled auto/manual classification.
- Auto-resolution for scope violations.
- Auto-resolution for identity conflicts.
- Auto-resolution for concurrent state changes.

**Binding constraints:**

- Platform owns resolvability classification.
- Deploy-time validation rejects policies targeting manual-only categories.
- Promotion of a category to auto-eligible is platform evolution.

**Guardrails:**

- Keep security-sensitive and identity-sensitive flags manual.
- Do not allow convenience to override integrity.

**Must not happen:**

- A deployment must not silently auto-dismiss unauthorized access or identity conflicts.

**Scope boundary:**  
This governs auto-resolution eligibility. Manual resolution workflows may still vary by deployment role and policy.

**Downstream impact:**  
Policy validation, trigger engine, and flag resolvers must enforce resolvability.

---

## 11. Platform-fixed vs deployer-configured split

### CDL-055: Scope mechanism is platform-fixed; scope instances are configuration

**Status:** Accepted  
**Classification:** Mechanism / config split

**Decision:**  
The scope mechanism is platform-fixed. Initial scope types are:

```text
geographic
subject_list
activity
```

Scope composition uses AND across non-null dimensions; null means unrestricted on that axis. The scope containment test is platform code. Concrete geographic trees, subject lists, activity grants, and temporal bounds are deployer configuration.

**Rationale:**  
Scope controls what data reaches devices. Custom deployer-defined containment logic is a data-leak risk.

**Rejected alternatives:**

- Deployment-defined custom scope types.
- Custom containment scripts.
- Geography-only scope forever.
- Classifying all scope concerns as either purely platform mechanism or purely deployer config.

**Binding constraints:**

- Scope type registry is closed at current values.
- New scope types require platform evolution.
- Deployers configure instances within platform scope types.

**Guardrails:**

- Do not put access-control containment in deployer scripts.
- Keep scope computation auditable and deterministic.

**Must not happen:**

- A configuration bug must not leak data by redefining containment semantics.

**Scope boundary:**  
This defines scope mechanism and instance split. It does not enumerate every possible geographic hierarchy or subject list implementation.

**Downstream impact:**  
Authorization, sync, assignment creation, and deployment configuration must enforce platform scope semantics.

---

### CDL-056: Activity is deployer configuration; `activity_ref` is a contract

**Status:** Accepted  
**Classification:** Config / contract split

**Decision:**  
An activity is a deployer-assembled L0 configuration instance composed from platform-provided components: shapes, patterns, roles, scope parameters, and policy bindings. `activity_ref` is the envelope contract that references an activity instance.

**Rationale:**  
The platform does not ship deployment activities. Deployers assemble activities using platform mechanisms. The field that points to an activity is still a platform envelope contract.

**Rejected alternatives:**

- Treating activity as a platform primitive.
- Treating `activity_ref` as config because activities are config.
- Letting activity definitions override platform processing semantics.

**Binding constraints:**

- Activities are authored and shipped in configuration packages.
- Activity identifiers follow the `activity_ref` grammar when referenced from events.
- Activities select and parameterize platform patterns; they do not create new pattern mechanisms.

**Guardrails:**

- Keep activity instance and activity reference distinct.
- Do not let activities redefine event type semantics.

**Must not happen:**

- An activity must not become a deployer-authored processing pipeline.

**Scope boundary:**  
This defines activity classification. Activity authoring UX and exact package syntax are specification/tooling concerns.

**Downstream impact:**  
Configuration packages, event stamping, projections, reports, and sync filters use activity instances and `activity_ref` consistently.

---

## 12. Rejected alternatives index

The following alternatives are rejected across the canonical model:

| Alternative | Canonical reason |
| --- | --- |
| Mutable current-state storage as source of truth | Loses or externalizes traceability. |
| Mutable state plus separate audit log | Audit can diverge from primary state. |
| Full snapshots as primary write model | Makes workflow transitions and long-running cases unnatural and duplicative. |
| Co-primary log and materialized view | Creates two sources of truth. |
| Server-assigned IDs for offline records | Blocks offline creation. |
| Preallocated ID pools | Can be exhausted offline and add complexity. |
| Last-write-wins | Causes silent data loss. |
| Rejecting state-stale events | Loses offline work. |
| Extra event `type` values for domain facts | Breaks closed type vocabulary. |
| `status_changed` structural type | State transition meaning is shape + pattern, not pipeline behavior. |
| `pattern_ref` in envelope | Pattern membership is configuration-derived. |
| `authority_context` in envelope | Authority is projection-derived from assignments. |
| `workflow_state` / `current_state` in events | Creates a second source of truth. |
| Alias-first conflict detection | Erases raw-reference anomaly context. |
| Rewriting references on merge | Violates immutability and provenance. |
| Unmerge operation | Cannot solve post-merge attribution without rewriting or guessing. |
| Offline merge/split | Risks stale global lineage changes. |
| Multiple canonical conflict resolutions | No termination rule. |
| Sync everything and hide unauthorized data | Places unauthorized data on devices. |
| Device-side authorization as final | Rooted/stale devices cannot be trusted as authority. |
| Deployer-authored access-control containment | Security-critical logic becomes configurable code. |
| Device-side L3 triggers | Duplicate firing and stale divergent evaluation. |
| Function-rich expression language | Turns configuration into programming. |
| Deployer-authored custom state machines | Violates platform pattern closure. |
| Automatic downstream flag propagation | Multiplies flags without adding decision value. |
| Auto-resolution for manual-only flags | Silently dismisses security, identity, or judgment-heavy anomalies. |

---

## 13. Must-not-happen index

| Must not happen                                                                                | Covered by                |
|------------------------------------------------------------------------------------------------|---------------------------|
| Agents compare old ADRs, ledgers, and explorations to infer current truth.                     | CDL-000                   |
| Existing events are overwritten or deleted.                                                    | CDL-001                   |
| Projections become authoritative over event history.                                           | CDL-002                   |
| Valid state-stale offline work is rejected.                                                    | CDL-003                   |
| Unresolved flagged events trigger policy or advance workflow state.                            | CDL-004                   |
| Platform mechanisms are treated as deployer-authored configuration.                            | CDL-005                   |
| Envelope grows hidden authority, pattern, state, scope, or sensitivity fields.                 | CDL-006, CDL-012          |
| Forbidden domain fact names appear as envelope `type` values.                                  | CDL-007, CDL-014          |
| Historical events are reinterpreted under newer shapes.                                        | CDL-008, CDL-039          |
| Activity attribution is guessed from timestamp/actor/scope.                                    | CDL-009                   |
| Wall-clock timestamps drive correctness.                                                       | CDL-011                   |
| Consumers identify domain facts by `type` alone.                                               | CDL-013                   |
| Duplicate real-world subjects are silently collapsed.                                          | CDL-022                   |
| Merge rewrites historical references.                                                          | CDL-023                   |
| Split reassigns historical events automatically.                                               | CDL-024                   |
| Unmerge exists as a platform operation.                                                        | CDL-025                   |
| Identity lineage cycles become constructable.                                                  | CDL-026                   |
| Offline devices create merge/split operations.                                                 | CDL-027                   |
| Two resolutions are canonical for one flag.                                                    | CDL-028                   |
| Retired-ID stale references disappear through alias projection.                                | CDL-029                   |
| Devices receive unauthorized data as normal sync behavior.                                     | CDL-031                   |
| Scope contraction deletes canonical event history or hides sensitive retained data only in UI. | CDL-037                   |
| Device-authored authority claims become canonical.                                             | CDL-032                   |
| Merge changes authorization scope of historical work.                                          | CDL-033                   |
| Actor grants scope they do not possess.                                                        | CDL-034                   |
| Configuration becomes an unbounded programming language.                                       | CDL-038, CDL-043, CDL-044 |
| L3 triggers fire on both device and server.                                                    | CDL-042                   |
| Workflow is implemented as mutable status.                                                     | CDL-047                   |
| Flag cascades create redundant downstream flags.                                               | CDL-051                   |
| `context.*` becomes a dynamic query language.                                                  | CDL-052                   |
| Auto-resolution mutates flag state directly.                                                   | CDL-053                   |
| Manual-only flags become auto-resolvable by deployment configuration.                          | CDL-054                   |

---

## 14. Deferred and platform-evolution boundary

The following are not current canonical decisions and must not be promoted accidentally:

| Boundary | Current treatment |
| --- | --- |
| New envelope fields | Architecture-grade platform decision. |
| New envelope `type` value | Architecture-grade platform decision. |
| New identity reference type beyond the closed enum | Architecture-grade platform decision. |
| New scope type | Platform evolution; not deployer configuration. |
| New pattern mechanism | Platform evolution; not deployer-authored. |
| Exact production pattern catalog and skeleton details | Platform specification and release process within pattern mechanism. |
| Field-level sensitivity, encryption, redaction, erasure | Platform evolution / compliance design. |
| Device-side flag pre-creation | Additive platform evolution, not deployer knob. |
| Device-side L3 trigger execution | Platform evolution requiring explicit design. |
| Conflict detector implementation details | Algorithmic implementation, constrained by accept-and-flag and flag representation. |
| Projection performance strategy | Implementation strategy; event stream remains canonical. |
| Regulatory erasure, field-level encryption, redaction, or legal de-identification after scope changes | Platform evolution / compliance design. |
| Human resolver UX | Product/specification. |
| Auto-resolution authoring UX | Product/specification. |
| Import tooling details | Implementation/specification; must preserve provenance honesty. |
| Configuration authoring syntax | Tooling/specification; runtime constraints remain canonical. |
