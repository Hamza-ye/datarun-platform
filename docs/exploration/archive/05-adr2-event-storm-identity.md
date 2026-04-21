> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-002**. For a navigated reading guide, see [guide-adr-002.md](../guide-adr-002.md).
# ADR-2 Event Storm: Identity & Conflict Resolution

> **Phase**: 1 — Event Discovery
> **Method**: Event Storming (Brandolini), applied solo per scenario
> **Purpose**: Discover every domain event, command, aggregate, and identity touchpoint across the five scenarios selected for what they stress about subject identity.
> **Output consumers**: Phase 2 (Workflow Stress-Test), ADR-2 decision exploration

---

## Notation

| Color | Meaning |
|-------|---------|
| 🟠 **Orange** | Domain Event — something that happened, past tense, immutable |
| 🔵 **Blue** | Command — an intent to do something, triggers an event |
| 🟡 **Yellow** | Actor — who performs the command |
| 🟣 **Pink/Purple** | Aggregate — what validates and emits the event, owns the invariant |
| 🟤 **Lilac** | Policy / Reaction — "When X happens, then Y should follow" |
| 📖 **Green** | Read Model / Projection — what someone needs to see |
| 🔴 **Red** | Hot Spot — unresolved question, tension, or identity hazard |

Identity-specific annotations:

| Marker | Meaning |
|--------|---------|
| **⟐ CREATES** | An identity is brought into existence |
| **⟶ REFERENCES** | An existing identity is pointed at |
| **⟁ MERGES** | Two identities are collapsed into one |
| **⟂ SPLITS** | One identity becomes two or more |
| **⟳ MUTATES** | The identity's meaning or attributes change |
| **⚡ AMBIGUOUS** | Identity cannot be resolved with certainty |

---

## Platform Vision Context

Datarun is an operations platform for organizations collecting information, coordinating work, tracking progress, and maintaining accountability across people, places, and time — reliably, even without connectivity. The value proposition is: **one configured platform replaces bespoke tools for every operational need**. The core commitments are offline-first operation, set-up-not-built configuration, trustworthy append-only records, unified system feel, and graceful growth.

ADR-1 established: immutable events as the storage primitive, client-generated UUIDs, append-only writes, events as the sync unit, and projections as derived read models. ADR-2 must now decide how identity works on top of this foundation.

**What identity means for this platform**: Every event is *about* something — a subject. The subject is the real-world thing (person, place, piece of equipment, organizational unit) that the event describes. Identity is the mechanism by which events find their subject. If identity is wrong — two UUIDs for the same person, one UUID for two different facilities — every projection, every assignment, every conflict detection, every historical view is corrupted. Identity is the load-bearing axis of the data model.

---

## S01 — Entity-Linked Capture

**What this scenario stresses about identity**: The baseline question — what *is* a subject identity? How is it created? How does it survive duplicates, ambiguity, and offline ID generation?

### Timeline

```
    Field Worker A                    Field Worker B                       Server
    ─────────────                    ─────────────                       ──────
    Arrives at Facility X            Arrives at Facility X (same day)
    Does not find it in local list   Does not find it in local list
    Creates new subject              Creates new subject
      UUID: aaa-111                    UUID: bbb-222
    Records observation              Records observation
      linked to aaa-111               linked to bbb-222
    ...later syncs...                ...later syncs...
                                                                        Two subject streams
                                                                        for ONE real-world facility
```

### Event Storm

#### Act 1: Subject Registration (the happy path)

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **RegisterSubject** | "I want to create a new subject in my local registry" |
| 🟡 Actor | Field Worker | Operating on-device, possibly offline |
| 🟣 Aggregate | **Subject Registry** | Validates: required identity attributes are present (name, location, type). Mints a client-generated UUID. |
| 🟠 Event | **SubjectRegistered** | `{subject_id: UUID, attributes: {name, location, type, ...}, registered_by, registered_at_device_time}` |
| **⟐ CREATES** | A new subject identity is born | The UUID is the canonical identifier from this moment forward |

#### Act 2: Recording an Observation About a Known Subject

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **RecordObservation** | "I want to capture details about this specific subject" |
| 🟡 Actor | Field Worker | Selects subject from local registry |
| 🟣 Aggregate | **Subject Stream** | Validates: the referenced subject_id exists in the local registry. The observation payload matches the expected shape. |
| 🟠 Event | **ObservationRecorded** | `{event_id: UUID, subject_id: reference, payload: {...}, shape_version, recorded_by, recorded_at_device_time}` |
| **⟶ REFERENCES** | subject_id points at the identity created in Act 1 | This is the first link in the chain — every future observation extends this stream |

#### Act 3: Duplicate Subject Creation (the hard path)

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **RegisterSubject** (second device) | Same real-world facility, different worker, different UUID |
| 🟡 Actor | Field Worker B | Does not see Field Worker A's registration (offline or out of sync scope) |
| 🟣 Aggregate | **Subject Registry** (device B) | Validates the same way — required attributes present. Mints a new UUID. No knowledge of the duplicate. |
| 🟠 Event | **SubjectRegistered** | `{subject_id: UUID (different), attributes: {name_variant, location_variant, type, ...}}` |
| **⟐ CREATES** | A *second* identity for the *same* real-world thing | The system now has two canonical identities that both believe they are the sole representation |
| 🔴 **Hot Spot** | **Duplicate detection** | How is this detected? When? By whom? Options: (a) server-side matching rules on sync, (b) human review of "possible duplicates" queue, (c) both. The matching rules are fuzzy — name spelling, GPS proximity, phonetic similarity. No rule is 100% reliable. |

#### Act 4: Duplicate Detected and Merged

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **MergeSubjects** | "These two subjects are the same real-world thing — combine them" |
| 🟡 Actor | Coordinator / Data Manager | Someone with authority to make identity decisions (not a field worker) |
| 🟣 Aggregate | **Subject Identity Resolver** | Validates: both subject_ids exist. Determines which becomes the surviving ID (or creates a new canonical one). Validates that the actor has merge authority. |
| 🟠 Event | **SubjectsMerged** | `{surviving_id, retired_id, merged_by, merged_at, rationale}` |
| **⟁ MERGES** | retired_id is subsumed into surviving_id | All past events that referenced retired_id remain unchanged (immutability). Projections must now resolve retired_id → surviving_id. |
| 🟤 Policy | **Post-Merge Reindex** | When SubjectsMerged is processed, all projections that reference retired_id must include those events in the surviving_id's stream |
| 📖 Read Model | **Merged Subject History** | The projection for surviving_id now shows events from both streams, ordered by device_time, with provenance marking which events came from which original identity |
| 🔴 **Hot Spot** | **In-flight references** | What happens to devices that still have retired_id in their local registry? On next sync, they receive SubjectsMerged. Must they rewrite local references? Or does the projection layer silently resolve retired → surviving? If the field worker creates a NEW observation against retired_id before learning about the merge, is that event valid? |
| 🔴 **Hot Spot** | **Merge reversal** | What if the merge was wrong? Two facilities that looked alike but are actually different. Since events are immutable, the merge event cannot be deleted. A **SubjectsUnmerged** event would need to exist. But how do you un-project the combined stream? Every observation needs to be re-attributed. |

#### Act 5: Ambiguous Identity (cannot be resolved automatically)

| Step | Component | Detail |
|------|-----------|--------|
| 🟤 Policy | **Duplicate Candidate Flagged** | Server-side matching rules identify two subjects that *might* be the same but confidence is below threshold |
| 🟠 Event | **DuplicateCandidateIdentified** | `{subject_a, subject_b, confidence_score, matching_attributes, flagged_at}` |
| 📖 Read Model | **Pending Identity Review Queue** | Coordinators see pairs/groups of subjects that may be duplicates |
| **⚡ AMBIGUOUS** | Identity is uncertain | Both subjects continue to function independently until a human resolves it. Events continue to accumulate on both streams. |
| 🔴 **Hot Spot** | **Ambiguity duration** | How long can ambiguity persist? If two subjects are "maybe duplicates" for 6 months, they accumulate divergent histories. Merging them becomes progressively harder. Does the system apply any pressure to resolve ambiguity? |

### Identity Map — S01

```
┌──────────────────────────────────────────────────────────────┐
│  Subject Identity Lifecycle (S01)                            │
│                                                              │
│  ⟐ CREATED (RegisterSubject)                                │
│       │                                                      │
│       ▼                                                      │
│  ⟶ REFERENCED (RecordObservation, ×N)                       │
│       │                                                      │
│       ├──── normal path: accumulates events ──────────────▶  │
│       │                                                      │
│       ├──── duplicate path: ⚡ AMBIGUOUS ───▶ review queue   │
│       │                                        │             │
│       │                                        ▼             │
│       │                              ⟁ MERGED (surviving ID) │
│       │                                  or                  │
│       │                              ❌ NOT DUPLICATE         │
│       │                                  (both continue)     │
│       │                                                      │
│       └──── wrong merge: SubjectsUnmerged ──▶ ⟂ SPLITS      │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### Aggregates Discovered

| Aggregate | Invariant | New? |
|-----------|-----------|------|
| **Subject Registry** | Every subject has exactly one client-generated UUID. Required identity attributes are present at creation. | Refines "Subject Identity" from primitives map |
| **Subject Stream** | Every event in a stream references a valid subject_id. Events are ordered by device_time. | Refines Event Store per-subject view |
| **Subject Identity Resolver** | A merge produces exactly one surviving_id. All retired_ids map to exactly one surviving. The mapping is transitional (if A→B and B→C, then A→C). | **NEW** — not in primitives map |

---

## S06 — Registry Lifecycle

**What this scenario stresses about identity**: Identity through *mutation* — subjects that get deactivated, split, merged, reclassified. Historical references must survive these changes.

### Event Storm

#### Act 1: Subject Attribute Change

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **UpdateSubjectAttributes** | "This facility's details have changed" |
| 🟡 Actor | Coordinator / Data Manager | May require verification/approval |
| 🟣 Aggregate | **Subject Registry** | Validates: the subject_id exists. The attribute changes are valid per the current shape. |
| 🟠 Event | **SubjectAttributesUpdated** | `{subject_id, changed_fields: [{field, old_value, new_value}], updated_by, updated_at}` |
| **⟳ MUTATES** | The subject's descriptive attributes change, but its identity (UUID) does not | Critical distinction: the *name* of a facility can change without the *identity* changing |
| 📖 Read Model | **Subject History** | Projection shows current attributes + full changelog |

#### Act 2: Subject Deactivation

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **DeactivateSubject** | "This facility no longer operates" |
| 🟡 Actor | Coordinator | Authority required |
| 🟣 Aggregate | **Subject Registry** | Validates: subject exists, is currently active. Checks: are there in-progress activities referencing this subject? |
| 🟠 Event | **SubjectDeactivated** | `{subject_id, reason, deactivated_by, deactivated_at, effective_date}` |
| **⟳ MUTATES** | The subject is no longer active, but its identity persists | Historical events linked to this subject remain valid and viewable. The identity is *retired*, not *deleted*. |
| 🟤 Policy | **Block New Work on Deactivated** | When a subject is deactivated, new observations/captures against it should be prevented. But what about offline devices that don't know about the deactivation yet? |
| 🔴 **Hot Spot** | **Offline work against deactivated subjects** | A field worker synced yesterday. Today the subject is deactivated. Tomorrow the field worker captures an observation against it. The event is valid (it happened), but the subject is invalid (it shouldn't receive new events). This event will be "orphaned" — or must be accepted with a flag. Which? |

#### Act 3: Subject Split

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **SplitSubject** | "This facility has split into two separate facilities" |
| 🟡 Actor | Coordinator / Data Manager | |
| 🟣 Aggregate | **Subject Identity Resolver** | Validates: source subject exists. Creates two new subject_ids as successors. Establishes lineage. |
| 🟠 Event | **SubjectSplit** | `{source_id, successor_ids: [new_id_1, new_id_2], split_by, split_at, rationale, attribute_distribution: {new_id_1: {...}, new_id_2: {...}}}` |
| **⟂ SPLITS** | One identity becomes two | Historical events under source_id are NOT re-attributed. They remain as-is. New events go to successor_ids. The projection for source_id shows "split into X and Y on date Z." |
| 🔴 **Hot Spot** | **Which successor inherits what?** | If the source subject had 50 observations, do they all stay with source_id (frozen archive)? Or are some attributed to successor_1 and others to successor_2? If the latter, who decides and how? This is a *manual re-attribution* problem — extremely expensive at scale. |
| 🔴 **Hot Spot** | **Ongoing assignments** | If Field Worker A was assigned to the source subject, are they now assigned to both successors? One of them? Neither (requiring explicit re-assignment)? |

#### Act 4: Schema Evolution Intersecting with Identity

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **UpdateShapeDefinition** | "We now need to collect GPS coordinates for every facility" |
| 🟡 Actor | Coordinator / Configuration Manager | |
| 🟣 Aggregate | **Shape Registry** | Validates: the new shape version is valid. Old shapes are not deleted — they're versioned. |
| 🟠 Event | **ShapeVersionPublished** | `{shape_id, version, added_fields, removed_fields, published_by, published_at}` |
| 🟤 Policy | **Offline Shape Coexistence** | Devices that haven't synced continue using old shape. Events created under old shape are valid. Events under new shape are also valid. The subject's event stream now contains events under mixed shape versions. |
| 🔴 **Hot Spot** | **Identity attributes in the shape** | If the shape defines identity-bearing attributes (e.g., "facility name" becomes "facility name + GPS"), does this change what constitutes a "match" for duplicate detection? The matching rules are now inconsistent between old-shape and new-shape events. |

#### Act 5: Bulk Reclassification

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **BulkReclassifySubjects** | "All facilities in Region X are now Type B instead of Type A" |
| 🟡 Actor | Coordinator | |
| 🟣 Aggregate | **Subject Registry** (per subject) | Each reclassification is validated individually — the subject exists and the new classification is valid |
| 🟠 Event (×N) | **SubjectAttributesUpdated** (one per subject) | Bulk command produces N individual events. Each carries its own identity reference. The command is a batch; the events are atomic. |
| **⟳ MUTATES** (×N) | Many identities change descriptive attributes simultaneously | |
| 🔴 **Hot Spot** | **Bulk vs. offline collision** | If 200 subjects are reclassified, and a field worker offline has been recording observations about 10 of them under the old classification, the observations are valid but describe subjects whose classification has changed. Does the observation carry the classification at time of observation? Or does it inherit the subject's *current* classification? This is a **point-in-time vs. current-state** tension. |

### Identity Map — S06

```
┌──────────────────────────────────────────────────────────────┐
│  Subject Identity Lifecycle (S06)                            │
│                                                              │
│  ⟐ CREATED                                                  │
│       │                                                      │
│       ▼                                                      │
│  ⟳ MUTATED (attributes change, N times)                     │
│       │                                                      │
│       ├──── deactivation ──▶ identity persists, frozen       │
│       │                      historical refs survive         │
│       │                                                      │
│       ├──── split ──▶ ⟂ source archived                     │
│       │                  successors created (⟐ ×2)           │
│       │                  lineage recorded                    │
│       │                                                      │
│       ├──── merge ──▶ ⟁ (from S01)                          │
│       │                                                      │
│       └──── reclassification ──▶ ⟳ many simultaneously      │
│                                                              │
│  At every point: historical events remain linked to the      │
│  identity that was current when they were recorded.          │
└──────────────────────────────────────────────────────────────┘
```

### Aggregates Discovered

| Aggregate | Invariant | New? |
|-----------|-----------|------|
| **Subject Registry** | Subjects can be active, deactivated, or split. Deactivated subjects retain their identity. | Refined — adds lifecycle states |
| **Subject Identity Resolver** | Split produces lineage: source → successors. Merge produces lineage: retired → surviving. Lineage is transitive and acyclic. | Refined — adds split semantics |
| **Shape Registry** | Shape versions are ordered. Old shapes are never deleted. Events carry their shape version. | Confirmed from primitives map |

---

## S19 — Offline Capture & Sync

**What this scenario stresses about identity**: Identity under *adversarial conditions* — two devices, same subject, conflicting events, stale state. The most hostile environment for identity.

### Event Storm

#### Act 1: Concurrent Observation (Same Subject, Two Devices)

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **RecordObservation** (Device A) | Worker A records observation about subject X at 10:00 AM |
| 🔵 Command | **RecordObservation** (Device B) | Worker B records observation about subject X at 10:30 AM |
| 🟠 Event (A) | **ObservationRecorded** | `{event_id: aaa, subject_id: X, payload: {temp: 37.2}, recorded_at: 10:00}` |
| 🟠 Event (B) | **ObservationRecorded** | `{event_id: bbb, subject_id: X, payload: {temp: 38.5}, recorded_at: 10:30}` |
| **⟶ REFERENCES (×2)** | Both events point at subject X | Both are valid. Both contribute to X's history. Neither is a "conflict" in the traditional sense — they are *concurrent additive observations*. |
| 🔴 **Hot Spot** | **Is this a conflict?** | Two observations about the same subject, same day, different values. This is NOT necessarily a conflict — they may be different visits, different times. What makes it a conflict is domain-dependent: if the subject can only be visited once per day, this is a constraint violation. If multiple visits are valid, this is just accretion. **Identity alone does not determine conflict. Identity + business rules determine conflict.** |

#### Act 2: Concurrent State Change (Same Subject, Two Devices)

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **ChangeSubjectStatus** (Device A) | Worker A marks subject X as "inactive" at 11:00 AM |
| 🔵 Command | **RecordObservation** (Device B) | Worker B records observation about subject X at 11:30 AM |
| 🟠 Event (A) | **SubjectDeactivated** | `{subject_id: X, deactivated_at: 11:00}` |
| 🟠 Event (B) | **ObservationRecorded** | `{subject_id: X, payload: {...}, recorded_at: 11:30}` |
| 🔴 **Hot Spot** | **Temporal paradox** | Worker B's observation was recorded AFTER deactivation — but Worker B didn't know about the deactivation. The observation is factually valid (it was performed). The state transition is factually valid (it was authorized). These are *logically concurrent* — neither causally depends on the other. The system must accept both and surface the contradiction. |
| **⟶ REFERENCES** | Both point at subject X, but they assert contradictory things about X's validity | This is a genuine **identity state conflict**: one event says "X no longer exists as active," the other says "X was observed as existing." |

#### Act 3: Offline Identity Creation Collision

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **RegisterSubject** (Device A, offline) | Worker A registers "Clinic Amara" — gets UUID aaa-111 |
| 🔵 Command | **RegisterSubject** (Device B, offline) | Worker B registers "Amara Health Clinic" — gets UUID bbb-222 |
| **⟐ CREATES (×2)** | Two identities for the same real-world facility | Same as S01 Act 3, but now compounded by: both devices may generate observations against their respective IDs before sync |
| 🟠 Events (A) | SubjectRegistered + ObservationRecorded + ObservationRecorded | 3 events under aaa-111 |
| 🟠 Events (B) | SubjectRegistered + ObservationRecorded | 2 events under bbb-222 |
| 🔴 **Hot Spot** | **Merge complexity** | When these sync, the merge must reconcile 5 events across 2 identity streams. If the observations overlap in time, the projected history of the merged subject will show "two observations at the same time by different people" — which may or may not be a business rule violation. |

#### Act 4: Stale State Sync (Long Offline Duration)

| Step | Component | Detail |
|------|-----------|--------|
| 🟡 Actor | Field Worker | Has been offline for 5 days |
| State at disconnect | Subject X: active, Type A, Shape v1 | |
| Changes made centrally during offline: | Subject X: reclassified to Type B. Shape v2 published with new required field. Subject Y: deactivated. Subject Z: merged into Q. | |
| 🔵 Commands (offline) | RecordObservation(X), RecordObservation(X), RecordObservation(Y) | All valid per device state at disconnect |
| On sync: | 3 events arrive centrally against stale state | |
| 🔴 **Hot Spot** | **Stale identity references** | Observation against Y references a deactivated subject. Observations against X were under old shape v1 and old classification. Events are factually valid (P3: append-only). But the identity graph has changed underneath them. |
| 🟤 Policy | **Stale-State Reconciliation** | Events are accepted (immutability). But the projection must flag: "this event was recorded against state that has since changed." The flag is informational, not blocking. Someone (supervisor?) must review. |

#### Act 5: Conflict Detection and Resolution

| Step | Component | Detail |
|------|-----------|--------|
| 🟤 Policy | **Conflict Detector** | On sync, the system examines incoming events against the existing stream for each subject. Looks for: concurrent state changes, observations under stale state, constraint violations. |
| 🟠 Event | **ConflictDetected** | `{subject_id, conflicting_events: [event_a, event_b], conflict_type: "concurrent_state_change" | "stale_reference" | "duplicate_identity" | "constraint_violation", detected_at}` |
| 📖 Read Model | **Conflict Resolution Queue** | Shows subjects with unresolved conflicts, ordered by severity and age |
| 🔵 Command | **ResolveConflict** | "Keep both," "Pick one," "Supersede with new event," "Require field re-verification" |
| 🟡 Actor | Supervisor / Coordinator | Context-dependent — whoever has authority for the subject |
| 🟣 Aggregate | **Conflict Resolver** | Validates: the resolution is valid for the conflict type. The actor has resolution authority. |
| 🟠 Event | **ConflictResolved** | `{conflict_id, resolution_type, resolved_by, resolved_at, rationale, resulting_events: [...]}` |
| 🔴 **Hot Spot** | **Cascading conflicts** | Resolving one conflict may surface another. Merging subjects A and B might reveal that B had a conflict with C. The resolution graph can be deep. |

### Causal Ordering Discovery

This scenario reveals that **device_time alone is insufficient** to establish event ordering. Two events at "10:00 AM" on two devices with different clock drift are not truly concurrent — they just happen to have the same timestamp.

| Ordering Need | What It Must Express | Candidate Mechanism |
|---------------|---------------------|---------------------|
| "This event happened before that one on the same device" | Device-local total order | Monotonic device sequence number |
| "This event happened without knowledge of that one" | True concurrency | Vector clocks or Hybrid Logical Clocks (HLC) |
| "This event is a correction of that event" | Causal dependency | Explicit predecessor reference in event envelope |
| "This event was created under state version X" | Staleness detection | Last-known sync timestamp or state version watermark |

🔴 **Hot Spot**: HLC adds envelope complexity. Vector clocks grow with the number of devices. A simpler approach (device sequence + last-synced watermark) may be sufficient if the platform accepts that some concurrency is undetectable. **This is ADR-2's central mechanism decision.**

### Identity Map — S19

```
┌──────────────────────────────────────────────────────────────────┐
│  Identity Under Adversarial Conditions (S19)                     │
│                                                                  │
│  Normal:   ⟶ REFERENCES subject_id → events accrete             │
│                                                                  │
│  Conflict Types:                                                 │
│                                                                  │
│  1. ADDITIVE (both valid)                                        │
│     Two observations, same subject, no contradiction             │
│     → Accept both, no conflict                                   │
│                                                                  │
│  2. STATE CONFLICT (contradictory)                               │
│     Deactivation + observation, or two state changes             │
│     → Accept both, flag conflict, require resolution             │
│                                                                  │
│  3. IDENTITY COLLISION (from S01)                                │
│     Two UUIDs for same thing → duplicate candidate               │
│     → Flag for merge review                                      │
│                                                                  │
│  4. STALE REFERENCE                                              │
│     Event against outdated identity state                        │
│     → Accept event, annotate staleness, flag for review          │
│                                                                  │
│  Resolution is always an event itself (P3, P5)                   │
└──────────────────────────────────────────────────────────────────┘
```

### Aggregates Discovered

| Aggregate | Invariant | New? |
|-----------|-----------|------|
| **Conflict Detector** | Every pair of concurrent events on the same subject is evaluated. Conflicts are never silently resolved. Detection is deterministic given the same input. | Promoted from "Candidate" in primitives map |
| **Conflict Resolver** | Every resolution is an event. The resolution references the conflict it resolves. Only authorized actors can resolve. | **NEW** — resolution as distinct from detection |
| **Causal Ordering** (mechanism TBD) | The system can distinguish "before", "after", and "concurrent" for any two events on the same subject. | **NEW** — mechanism is ADR-2's decision |

---

## S03 — User-Based Assignment

**What this scenario stresses about identity**: The identity of *actors* (people, not subjects) and their relationship to subjects — a second identity axis orthogonal to subject identity.

### Event Storm

#### Act 1: Assignment Creation

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **AssignResponsibility** | "Worker A is now responsible for subjects in Zone 3" |
| 🟡 Actor | Coordinator / Supervisor | Someone with assignment authority |
| 🟣 Aggregate | **Assignment Registry** | Validates: the actor (worker) exists. The scope (zone/subject set) is valid. The assigner has authority. |
| 🟠 Event | **ResponsibilityAssigned** | `{assignment_id: UUID, actor_id, scope: {type: "zone", value: "zone-3"}, assigned_by, assigned_at, effective_from}` |
| **⟐ CREATES** | An *assignment identity* is born | The assignment itself is an identifiable thing — it can be referenced, changed, revoked |
| 🔴 **Hot Spot** | **Two identity axes** | actor_id is a person's identity. scope references subject identities (all subjects in zone-3). The assignment *binds* these two identity axes. If a subject moves between zones, does the assignment follow the subject or stay with the zone? |

#### Act 2: Actor Identity and Subject Identity Intersection

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **RecordObservation** | Worker A records about subject X in zone-3 |
| 🟣 Aggregate | **Subject Stream** (validation enhanced) | Validates: subject_id exists. **AND** the actor has an active assignment that covers this subject. |
| 🟠 Event | **ObservationRecorded** | `{..., recorded_by: actor_id, under_assignment: assignment_id}` |
| **⟶ REFERENCES (×2)** | subject_id (what was observed) AND actor_id + assignment_id (who was authorized to observe it) | The event sits at the intersection of two identity axes |
| 🔴 **Hot Spot** | **Assignment enforcement offline** | The device must know, without network, whether the current actor has a valid assignment covering the current subject. This means assignment data must be on-device and up-to-date (or accepted as potentially stale). |

#### Act 3: Assignment Transfer

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **TransferResponsibility** | "Worker A is leaving; Worker B takes over zone-3" |
| 🟡 Actor | Supervisor | |
| 🟣 Aggregate | **Assignment Registry** | Validates: old assignment exists and is active. New actor exists. |
| 🟠 Event | **ResponsibilityTransferred** | `{old_assignment_id, new_assignment_id: UUID, from_actor, to_actor, scope, transferred_by, transferred_at, effective_from}` |
| **⟳ MUTATES** (assignment) | The *binding* between actor and scope changes, but both the actor identities and the subject identities persist | Historical events remain attributed to the old actor under the old assignment. New events go to the new actor under the new assignment. |
| 🔴 **Hot Spot** | **Offline transfer gap** | Worker A goes offline, then responsibility is transferred to Worker B. Worker A continues working offline under their (now-revoked) assignment. Their events are valid (P3) but attributed to a revoked assignment. On sync: events accepted, flagged as "under revoked assignment," review required. |

#### Act 4: Actor Identity Ambiguity

| Step | Component | Detail |
|------|-----------|--------|
| 🔴 **Hot Spot** | **Same person, multiple roles** | A person might be a field worker in one activity and a supervisor in another. Their actor_id is the same, but their assignment scope differs. Events must capture not just WHO but IN WHAT CAPACITY. |
| 🔴 **Hot Spot** | **Actor identity vs. device identity** | An event records actor_id and device_id. If Worker A uses Worker B's phone (common in the field — phones break, get shared), the device_id and actor_id diverge. Which is authoritative for identity purposes? |

### Identity Map — S03

```
┌──────────────────────────────────────────────────────────────┐
│  Actor Identity × Subject Identity (S03)                     │
│                                                              │
│  ACTOR AXIS                 SUBJECT AXIS                     │
│  ──────────                 ────────────                     │
│  actor_id (person)          subject_id (thing)               │
│       │                          │                           │
│       └──── BOUND BY ────────────┘                           │
│              assignment_id                                   │
│              (scope + role + time)                            │
│                                                              │
│  Events sit at intersection:                                 │
│    {subject_id, actor_id, assignment_id}                     │
│                                                              │
│  Assignment lifecycle:                                       │
│    ⟐ Created → ⟳ Transferred → deactivated                 │
│    (parallels subject lifecycle from S06!)                    │
│                                                              │
│  Offline hazard: assignment revocation + continued work      │
│    → same pattern as subject deactivation + continued work   │
└──────────────────────────────────────────────────────────────┘
```

### Aggregates Discovered

| Aggregate | Invariant | New? |
|-----------|-----------|------|
| **Assignment Registry** | Every active assignment binds exactly one actor to exactly one scope. Scopes can overlap (one worker, multiple assignments). An assignment is temporally bounded (effective_from, optionally effective_until). | Refines "Assignment Resolver" from primitives map — now an aggregate, not just a resolver |
| **Actor Identity** | Every person has one canonical actor_id. One person can hold multiple assignments simultaneously. The actor_id is referenced by events independently of the assignment. | **NEW** — distinct from subject identity |

---

## S07 — Resource Distribution

**What this scenario stresses about identity**: Identity through *handoffs* — a resource changes custody but its identity must hold across the entire chain. Plus: the *shipment* itself becomes an identifiable thing.

### Event Storm

#### Act 1: Shipment Initiated

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **InitiateShipment** | "Send 500 units of supply X from warehouse to District A" |
| 🟡 Actor | Logistics Coordinator | |
| 🟣 Aggregate | **Shipment** | Validates: sender exists, recipient exists, supply item is valid, quantity is available |
| 🟠 Event | **ShipmentInitiated** | `{shipment_id: UUID, items: [{supply_id, quantity}], from: org_unit_id, to: org_unit_id, initiated_by, initiated_at}` |
| **⟐ CREATES** | A shipment identity is born | This is a *new kind* of subject — not a persistent real-world thing like a facility, but a transient thing that exists for the duration of a process |
| **⟶ REFERENCES** | supply_id (what's being sent), from/to org_unit_ids (sender/receiver identities) | The shipment event ties together multiple subject identities |
| 🔴 **Hot Spot** | **Supply identity** | Is "Supply X" a subject with its own identity? Or is it a *type* (an attribute), not an individual? 500 units of vaccine X are fungible — they don't have individual identities. But the *batch* might. The identity granularity depends on the domain. |

#### Act 2: Dispatch Confirmed

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **ConfirmDispatch** | "The shipment has left the warehouse" |
| 🟡 Actor | Warehouse Manager | |
| 🟣 Aggregate | **Shipment** | Validates: shipment exists, status is `initiated`, actor is from the sending org unit |
| 🟠 Event | **ShipmentDispatched** | `{shipment_id, dispatched_by, dispatched_at, actual_items: [{supply_id, quantity}]}` |
| **⟶ REFERENCES** | shipment_id — the identity holds across the state transition | |
| 🔴 **Hot Spot** | **Quantity mismatch at dispatch** | Initiated: 500 units. Dispatched: 480 units (20 were out of stock). The event records the actual quantity. The discrepancy is visible in the projection. Is this a conflict? No — it's a *legitimate variance*. But it must be traceable. |

#### Act 3: Receipt Confirmed (or Disputed)

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command | **ConfirmReceipt** | "The shipment arrived at District A" |
| 🟡 Actor | District Receiver | |
| 🟣 Aggregate | **Shipment** | Validates: shipment exists, status is `dispatched`, actor is from the receiving org unit |
| 🟠 Event | **ShipmentReceived** | `{shipment_id, received_by, received_at, received_items: [{supply_id, quantity, condition}]}` |
| **⟶ REFERENCES** | shipment_id — identity survives the handoff boundary | |
| 🔴 **Hot Spot** | **Receipt identity vs. dispatch identity** | Dispatched: 480 units. Received: 470 units (10 lost/damaged). Again, legitimate variance — not an identity problem. But what if the receiver reports receiving "Vaccine Y" when the shipment was "Vaccine X"? This is a *content identity mismatch* — the receiver and sender disagree about WHAT was sent. |

| Step | Component | Detail |
|------|-----------|--------|
| 🔵 Command (alt) | **DisputeShipment** | "What I received doesn't match what was sent" |
| 🟠 Event | **ShipmentDisputed** | `{shipment_id, dispute_type, reported_discrepancies, disputed_by, disputed_at}` |
| 🟤 Policy | **Dispute Escalation** | When ShipmentDisputed, notify the sender and a supervisor for resolution |

#### Act 4: Multi-Level Handoff (Central → Region → District)

| Step | Component | Detail |
|------|-----------|--------|
| 🟠 Event chain | ShipmentInitiated → ShipmentDispatched → **ShipmentReceived** (at region) → **ShipmentRedispatched** → **ShipmentReceived** (at district) | The shipment_id persists across ALL legs. Each handoff adds events to the same stream. |
| **⟶ REFERENCES (×N)** | The same shipment_id is referenced by events from 3 different org units | This is identity holding across *organizational boundaries*, not just time |
| 🔴 **Hot Spot** | **Sub-shipment identity** | Region receives 500 units, then splits into 3 shipments (200 to District A, 200 to B, 100 to C). Each sub-shipment needs its own identity (sub_shipment_id). But it must reference the parent shipment. This is **⟂ SPLITS applied to a transient identity** — same structural problem as S06 facility splits, but for shipments. |

#### Act 5: Offline Handoff

| Step | Component | Detail |
|------|-----------|--------|
| Scenario | Sender dispatches (online). Receiver is offline for 3 days. | |
| 🔵 Command (offline) | **ConfirmReceipt** | Receiver confirms receipt while offline, not knowing the exact shipment_id |
| 🔴 **Hot Spot** | **Referencing an identity you don't have** | If the receiver is offline and has never synced the ShipmentInitiated event, they don't know the shipment_id. They know "I received supplies." How do they link their receipt event to the shipment? Options: (a) pre-communicate shipment_id out-of-band (paper manifest), (b) create a "pending receipt" that is matched to a shipment on sync, (c) require sync before receipt. Option (c) violates P1. |
| **⚡ AMBIGUOUS** | The receipt exists but its link to a specific shipment is uncertain | This is identity ambiguity of a different kind — not "which subject is this about?" but "which process does this event belong to?" |

### Identity Map — S07

```
┌──────────────────────────────────────────────────────────────┐
│  Identity Through Handoffs (S07)                             │
│                                                              │
│  SHIPMENT IDENTITY (transient)       SUBJECT IDENTITIES      │
│  ─────────────────────────           ──────────────────       │
│  shipment_id                         from: org_unit_id        │
│       │                              to: org_unit_id          │
│       │                              supply_id (type or batch)│
│       │                                                      │
│       ├── Initiated (⟐)                                     │
│       ├── Dispatched (⟶)                                    │
│       ├── Received (⟶ across org boundary)                  │
│       ├── Redispatched (⟶)                                  │
│       ├── Sub-split (⟂) → child shipment_ids                │
│       └── Disputed (state conflict on content identity)      │
│                                                              │
│  Offline hazard: receipt without shipment_id knowledge        │
│    → pending-match pattern needed                            │
└──────────────────────────────────────────────────────────────┘
```

### Aggregates Discovered

| Aggregate | Invariant | New? |
|-----------|-----------|------|
| **Shipment** | A shipment has exactly one identity that persists across all handoff events. Handoffs are sequential (dispatch → receipt → redispatch → receipt). Quantities are tracked at each transition. | **NEW** — a process-scoped aggregate, different from persistent subjects |
| **Pending Match** (candidate) | An unlinked event (receipt without shipment_id) remains in a pending state until matched. Matching may be automatic (attribute similarity) or manual. | **NEW candidate** — may be a general pattern beyond shipments |

---

## Cross-Scenario Synthesis

### Identity Taxonomy Discovered

The five scenarios reveal that "identity" is not one thing. There are at least **four kinds of identity** the platform must handle:

| Identity Type | Examples | Created By | Lifecycle | Stressed By |
|---------------|----------|------------|-----------|-------------|
| **Subject Identity** | Facility, person, equipment, household | Client-generated UUID | Persistent — survives split, merge, deactivation | S01, S06 |
| **Actor Identity** | Field worker, supervisor, coordinator | Provisioned (not self-registered) | Persistent — survives role changes, transfers | S03 |
| **Process Identity** | Shipment, case, campaign instance | Client-generated UUID at initiation | Transient — exists for process duration | S07 |
| **Assignment Identity** | "Worker A responsible for Zone 3" | Created by authority | Temporal — has effective dates, can be revoked | S03 |

All four types share common needs: UUID-based, client-generatable, referenceable by events, survivable across sync boundaries. But they differ in lifecycle, mutability, and how they relate to each other.

### Conflict Type Taxonomy

| Conflict Type | Description | Detection | Resolution Authority | Scenario |
|---------------|-------------|-----------|---------------------|----------|
| **Concurrent Additive** | Two observations, same subject, no contradiction | Not a conflict — just accretion | None needed | S01, S19 |
| **Concurrent State Change** | Two events assert different state for same subject | Causality mechanism (HLC/vector) | Supervisor/Coordinator | S19, S06 |
| **Duplicate Identity** | Two UUIDs for same real-world thing | Matching rules (fuzzy) + human review | Data Manager/Coordinator | S01, S19 |
| **Stale Reference** | Event created against outdated state | Sync watermark comparison | Supervisor | S19, S06 |
| **Content Mismatch** | Parties disagree about what was transferred | Comparison of dispatch vs. receipt payloads | Supervisor + parties | S07 |
| **Revoked Authority** | Event recorded under revoked assignment | Assignment state comparison at sync | Supervisor | S03 |
| **Cross-Lifecycle** | Event against deactivated/split subject | Subject lifecycle state check at sync | Coordinator | S06, S19 |

### Aggregate Map Update (Post-Event-Storm)

| Aggregate | Invariant | Status | Emerged From |
|-----------|-----------|--------|-------------|
| Event Store | Events immutable, IDs unique, sole write path | **Settled** (ADR-1) | ADR-1 |
| Projection Engine | Current state = f(events), rebuildable | **Settled** (ADR-1) | ADR-1 |
| **Subject Registry** | Subjects have UUID identity, lifecycle states (active/deactivated/split), required attributes at creation | **Proposed** | S01 + S06 |
| **Subject Identity Resolver** | Manages merge/split lineage. Lineage is transitive and acyclic. Retired IDs resolve to surviving IDs. | **Proposed** | S01 + S06 |
| **Conflict Detector** | Concurrent events on same subject are always evaluated. Detection is deterministic. Conflicts are never silently resolved. | **Proposed** (promoted from Candidate) | S19 |
| **Conflict Resolver** | Every resolution is an event referencing the conflict. Only authorized actors resolve. | **Proposed** | S19 |
| **Assignment Registry** | Binds actor to scope with temporal bounds. Enforced on-device against local state. | **Proposed** (refined from Candidate) | S03 |
| **Actor Identity** | One canonical ID per person. Multiple simultaneous assignments. Referenced independently by events. | **Proposed** | S03 |
| **Shipment** | Process-scoped identity persisting across handoffs. Sequential state progression. | **Proposed** | S07 |
| **Causal Ordering** (mechanism TBD) | Distinguishes before/after/concurrent for events on same subject. | **Open** — central ADR-2 decision | S19 |
| Shape Registry | Shape versions ordered, old shapes preserved, events carry version. | **Confirmed** | S06 |
| Command Validator | No event written that violates lifecycle/auth rules. | **Confirmed** from prior walk-through | S06 + S03 |

---

## Questions for ADR-2

These questions emerged from the event storm and must be answered by the ADR-2 decision:

### Identity Model

1. **Identity granularity**: Is "Subject Identity" one aggregate that covers all four identity types? Or are Subject, Actor, Process, and Assignment separate aggregates with a shared identity protocol?
2. **Merge semantics**: When subjects merge, do events physically re-reference (violating immutability) or does the projection layer maintain an alias table? If alias table — where does it live, and how does it sync to devices?
3. **Split semantics**: When a subject splits, are historical events frozen under the source ID (becoming an archive), or are they re-attributable to successors? If re-attributable, by what mechanism?
4. **Cross-type references**: A shipment references org_units (subjects), an actor, and supply items. These are all different identity types. Is the referencing mechanism the same (UUID pointer) regardless of type?

### Conflict Resolution

5. **Conflict definition boundary**: Who defines what counts as a conflict? Is it platform-level (concurrent state changes are always conflicts) or configuration-level (deployers define conflict rules per activity)?
6. **Conflict resolution location**: Are conflicts resolved on-device (by the actor who encounters them on sync) or centrally (by a coordinator who sees a queue)? Or both, depending on conflict type?
7. **Cascading resolution**: If resolving conflict A surfaces conflict B, is this handled iteratively (resolve A, detect B, resolve B) or as a compound resolution?

### Causal Ordering

8. **Mechanism choice**: HLC vs. vector clocks vs. device-sequence + sync-watermark. What is the minimum mechanism that satisfies P5 (conflict is surfaced, not silently resolved)?
9. **Ordering scope**: Is causal ordering per-subject (events within one stream) or cross-subject (events across streams that might be causally related)?
10. **Clock trust**: Device clocks are unreliable. How much does the ordering mechanism depend on clock accuracy vs. logical ordering?

### Offline Resilience

11. **Post-merge sync**: When a device syncs after a merge event, does it receive the merge event and resolve locally? Or does the server rewrite the device's local state?
12. **Orphaned events**: Events against deactivated/merged/split subjects — accepted-and-flagged, or rejected? If accepted, what lifecycle do the flags follow?
13. **Pending match pattern**: Is the "receipt without shipment_id" problem specific to S07, or is it a general pattern (event that references an identity the device doesn't know about yet)?

---

## Output Handoff to Phase 2

This event storm provides the following inputs for the **Workflow Stress-Test** phase:

1. **Event catalog**: All domain events discovered across 5 scenarios, with their payloads and identity references
2. **Aggregate map**: 12 aggregates (settled, proposed, or open), each with a stated invariant
3. **Conflict taxonomy**: 7 conflict types with detection mechanisms and resolution authorities
4. **Identity taxonomy**: 4 identity types with distinct lifecycles
5. **13 open questions** that ADR-2 must answer
6. **Hot spots**: 20+ unresolved tension points marked with 🔴 throughout the document

Phase 2 should stress-test:
- Can the workflow agent break any of the stated invariants?
- Do the conflict types cover all possible concurrent operation scenarios?
- Are there workflow paths that create identity states not covered by the taxonomy?
- Do the aggregate boundaries survive when scenarios are combined (e.g., S03 assignment + S07 distribution + S19 offline)?
