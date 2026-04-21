> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-001**. For a navigated reading guide, see [guide-adr-001.md](../guide-adr-001.md).
# ADR-1 Exploration: Offline Data Model

> This document explores the decision space for the platform's foundational storage primitive — how records are created, stored on-device, and synced. It does not make the decision. It lays out what the constraints force, what remains open, and what each remaining option actually costs.

---

## Why This Is First

The architecture landscape's coupling analysis shows the offline data model at the root of every other architectural dependency:

```
Offline Data Model ──┬── Schema Evolution
         │           ├── Configuration Paradigm
         │           │
         └── Identity Model ── Conflict Resolution
                     │
Authorization Model ─┘
         │
         └── Selective Sync Scope
```

Every subsequent decision — how subjects are identified, how conflicts are resolved, how authorization is enforced offline, how configuration is expressed — is constrained by this one. It is also the least reversible: changing the storage primitive after implementation means data migration across thousands of devices at unpredictable connectivity.

---

## The Sub-Decisions

"Offline data model" is not a single choice. It decomposes into five coupled sub-decisions:

| # | Sub-decision | What it determines |
|---|---|---|
| **S1** | Record mutability | Can a stored record be modified in place, or are all writes append-only? |
| **S2** | Write granularity | What is the atomic unit of a write — a full record, a field-level change, or a typed action? |
| **S3** | Identity generation | How are records and subjects identified — server-assigned sequential IDs or client-generated UUIDs? |
| **S4** | Sync unit | What crosses the wire — current state, diffs, or operations? |
| **S5** | Conflict semantics | When independent writes touch the same subject, how are they detected and handled? |

These are coupled: the mutability model (S1) constrains write granularity (S2), which constrains the sync unit (S4), which constrains conflict semantics (S5). Identity generation (S3) is independently constrained by offline-first but intersects with conflict detection.

The exploration below works through each sub-decision, showing what the constraints force and what remains open.

---

## S1: Record Mutability — What the Constraints Force

Three project commitments bear directly on this:

- **V3 (Trustworthy records)**: "Every action is traceable: who did what, when, under what role, and in what context. Records stay meaningful even as the setup evolves."
- **P3 (Records are append-only)**: "Nothing is deleted or overwritten. Corrections append; they don't replace. The full history is always recoverable."
- **S00 edge case**: "The correction must be traceable — who changed what, when, and why — without erasing the original."

**Mutable-in-place records with a separate audit log** fail these constraints structurally. The audit log is a secondary structure — if it and the primary record diverge (through bugs, sync errors, or partial writes), there is no single source of truth. The audit trail is a bolt-on, not an inherent property of the data. This is exactly the wall DHIS2 hit: "Conflicting records enter an ERROR/WARNING state that blocks upload until manually fixed."

**Decision forced**: Records are append-only. Once written, a record is never modified or deleted. Corrections, reviews, status changes, and amendments all produce new records that reference earlier ones.

This eliminates mutable-state approaches. What remains is: what shape do these immutable records take?

---

## S2: Write Granularity — The Real Choice

Given append-only records, the question becomes: what is the atomic thing that gets appended?

Three options survive:

### Option A: Immutable Snapshots

Every write produces a complete, self-contained record of the subject's state at that moment. A correction creates a new snapshot that references the original. Current state = the latest snapshot in a chain.

**Example — basic capture (S00):**
```
{id: "abc-001", type: "observation", subject: null, 
 data: {temp: 38.5, cough: true}, shape_v: 3, 
 at: "2026-04-08T09:15Z", by: "chv-042"}
```

**Example — correction (S00 edge):**
```
{id: "abc-002", type: "observation", subject: null, 
 data: {temp: 37.5, cough: true}, shape_v: 3,
 at: "2026-04-08T10:30Z", by: "chv-042",
 corrects: "abc-001", reason: "misread thermometer"}
```

**Example — review (S04):**
```
{id: "abc-003", type: "observation", subject: null,
 data: {temp: 37.5, cough: true, status: "approved"}, shape_v: 3,
 at: "2026-04-08T14:00Z", by: "sup-007",
 supersedes: "abc-002"}
```

**What it gives:**
- Self-contained: every snapshot can be understood without traversing history
- Simple implementation: each snapshot is a row in SQLite. Current state = latest row per chain
- Schema version is per-snapshot — old and new shapes coexist naturally
- Each snapshot is independently valid and verifiable

**What it costs:**
- Data duplication: every status change, review, or correction copies the entire record. For a case (S08) with 15 interactions, that is 15 full copies of the case data
- The "current state" of an active subject requires finding the chain head — which means indexing correction/supersession chains
- Workflow transitions (S04, S07, S11) are unnatural: the meaningful thing is "reviewed and approved" but the snapshot model forces you to copy all the data to express that
- Branch handling: two offline corrections to the same snapshot produce two chain heads. The merge must produce a third snapshot that resolves both. What goes in its `data` field?

### Option B: Immutable Events

Every write records what happened — a typed action with only the relevant payload. Current state is a projection: apply all events for a subject in order.

**Example — basic capture (S00):**
```
{id: "evt-001", type: "record_captured", subject: null,
 data: {temp: 38.5, cough: true}, shape_v: 3,
 at: "2026-04-08T09:15Z", by: "chv-042"}
```

**Example — correction (S00 edge):**
```
{id: "evt-002", type: "field_corrected", ref: "evt-001",
 data: {field: "temp", was: 38.5, now: 37.5, reason: "misread thermometer"},
 at: "2026-04-08T10:30Z", by: "chv-042"}
```

**Example — review (S04):**
```
{id: "evt-003", type: "review_completed", ref: "evt-001",
 data: {decision: "approved", notes: null},
 at: "2026-04-08T14:00Z", by: "sup-007"}
```

**What it gives:**
- No data duplication: each event carries only the delta or the action-specific payload
- Workflow transitions are native: "reviewed," "transferred," "acknowledged" are distinct event types with their own payloads — they don't contort into full-state copies
- Full granular history: not just "what was the state" but "what happened" — who corrected which field, who approved when, who transferred to whom
- Temporal queries: "what was the state of this case on March 15?" — replay events up to that timestamp
- Richer conflict detection: two concurrent events on the same subject are visible as branches in the event stream, with full context of what each person did

**What it costs:**
- Current state requires projection: apply events in order to compute what something looks like _now_. This is a computation, not a lookup
- Projection must be maintained (materialized) on-device for performance — a CHV can't wait for event replay on every screen load
- Event ordering across devices is non-trivial: device clocks are unreliable, so wall-clock timestamps are insufficient for causal ordering. Needs at minimum a hybrid logical clock (HLC)
- Developer complexity: reading code that writes events + maintains projections is harder than code that writes rows
- Event schema versioning: events are immutable, but the projection logic must understand events from all schema versions. An event from shape_v2 and an event from shape_v3 apply to the same subject — the projector must handle both

### Option C: Unified Action Log

A pragmatic middle ground. Every write produces an immutable log entry with a common envelope. The entry carries enough payload to be self-describing, but the system does NOT commit to full replay-from-zero as a normal operation. Materialized views (current state) are maintained alongside the log as the primary read path.

**Example — same actions as above, same records as Option B.** The log entries look identical to events. The difference is operational:

- **Option B** (pure event-sourcing): the event log IS the database. Materialized views are caches. If they diverge, you throw them away and replay.
- **Option C** (action log): the log and the materialized views are co-primary. The log provides traceability and conflict detection. The views provide fast reads. Replay is a repair operation, not a fundamental guarantee.

**What it gives:**
- Same traceability as events — the log is append-only and complete
- Same sync model as events — log entries transfer, are idempotent, are immutable
- Simpler operational model: no event replay on device startup, no projection compaction, no snapshot-for-replay concerns
- The materialized view is a first-class citizen maintained by application logic, not a derived cache

**What it costs:**
- Dual-write coordination: every action writes to the log AND updates the materialized view. If these diverge (crash between writes, bug in update logic), the state is inconsistent
- Temporal queries are harder: "what was the state at time T?" requires either reverse-computing from the materialized view or forward-computing from the log — neither is free
- The system has two sources of truth instead of one. The log is canonical for traceability; the view is canonical for current state. This is an honest admission of pragmatism, not an architectural purity

---

## Running the Options Through the Hard Scenarios

The scenarios exist to stress-test. Here is what happens when each option meets the hardest cases.

### S00 — Basic Capture with Corrections

| Concern | Snapshots (A) | Events (B) | Action Log (C) |
|---------|--------------|------------|-----------------|
| Simple capture | One snapshot. Clean | One event + projection update. Clean | One log entry + view update. Clean |
| Correction | New snapshot referencing original. Both preserved | Correction event referencing original. Projection updated | Same as B |
| Out-of-order correction arrival | Correction snapshot may arrive before original. Server must buffer or accept out-of-order. Resolvable | Same — events may arrive out of order. Projection computation handles it since it considers all events | Same as B for log; view update may need recomputation if out-of-order |

**Verdict**: All three handle S00. None fails. Events (B/C) are slightly cleaner on corrections because they don't duplicate the full payload.

### S01 — Entity-Linked Capture (Duplicate Identities)

| Concern | Snapshots (A) | Events (B) | Action Log (C) |
|---------|--------------|------------|-----------------|
| Two UUIDs for same real-world thing | Two independent snapshot chains. Merge = new snapshot absorbing both chains | Two independent event streams. Merge = merge event linking streams | Same as B |
| Split/merge of subjects | New snapshots under new identity referencing old. Historical snapshots under old identity remain | Subject-lifecycle events (split, merge) that the projection interprets | Same as B |

**Verdict**: Identity merge/split is hard regardless of data model. Events have a slight edge — subject lifecycle changes are naturally expressed as events rather than as "new snapshot with all the data plus a merge marker."

### S06b — Schema Evolution While Offline

| Concern | Snapshots (A) | Events (B) | Action Log (C) |
|---------|--------------|------------|-----------------|
| Old shape records coexist with new shape | Each snapshot carries shape_v. No migration. Clean | Each event carries shape_v. Projection must handle both versions. Clean | Same as B |
| Device works under old shape while server has new | Valid — snapshot is valid under the shape that was active on-device. Server accepts both | Valid — same reasoning. Event payload was valid when created | Same as B |

**Verdict**: All three handle schema evolution cleanly because all three attach the shape version to the immutable record. No differentiation here.

### S04/S11 — Review and Multi-Step Approval

| Concern | Snapshots (A) | Events (B) | Action Log (C) |
|---------|--------------|------------|-----------------|
| Work progresses through stages | Each stage = new snapshot with full data + status. 5-step approval = 5 full copies | Each stage = new event with only the transition data. 5-step approval = 5 small events | Same as B |
| Offline review conflicts | Two reviewers approve offline. Two snapshot heads. Data inside may differ if one also corrected | Two concurrent review events. Clear conflict — both events visible, both carry only the review decision | Same as B |
| "Where is this in the approval chain?" | Find latest snapshot, check status. Requires indexing chain heads | Query projection for current status. Status is computed, not stored | Query materialized view. Direct lookup |

**Verdict**: Events (B/C) are significantly cleaner for workflows. Snapshots force full-data duplication at every transition. The meaningful information at each step ("approved by X with note Y") is small — snapshots pay a large tax generating full copies merely to express a status change.

### S07/S14 — Transfers and Multi-Level Distribution

| Concern | Snapshots (A) | Events (B) | Action Log (C) |
|---------|--------------|------------|-----------------|
| Multi-hop transfer tracking | Each hop = snapshot: {items, status: "at-district-3"}. Full copy each time | Each hop = event: {type: "transfer_received", by: "district-3", items: [...]}. Lightweight | Same as B |
| "Where is shipment X right now?" | Find latest snapshot. Direct | Query projection. Computed from events | Query materialized view. Direct |
| Discrepancy at one hop | Discrepancy snapshot referencing transfer snapshot. Both preserved | Discrepancy event referencing transfer event. More granular | Same as B |

**Verdict**: Events are more natural for tracking movement. Each hop is an action, not a new complete state.

### S08 — Case Management (Long-Running)

| Concern | Snapshots (A) | Events (B) | Action Log (C) |
|---------|--------------|------------|-----------------|
| Case with 20 interactions over 3 months | 20 full snapshots. Significant storage per case. On 500 active cases: 10,000 snapshots | 20 events with lightweight payloads. Maybe 60 if you count field-level changes. Much less storage | Same as B |
| "Show me the full case history" | Traverse chain chronologically. Each snapshot is self-contained — easy to display | Replay events chronologically. Natural timeline view | Read log entries chronologically. Same as B |
| Case reopened after resolution | New snapshot superseding the "resolved" snapshot | Reopen event. Projection restores active state | Same as B |

**Verdict**: Events scale better for active, long-running cases. Snapshots work but cost storage proportional to interaction count × record size.

### S19 — Offline Conflicts (The Hardest)

| Concern | Snapshots (A) | Events (B) | Action Log (C) |
|---------|--------------|------------|-----------------|
| Two CHVs capture contradictory data about same subject while offline | Two snapshot heads for same subject. Conflict detected by: >1 unresolved head per subject. Resolution: human creates a third snapshot marking both as resolved | Two concurrent events on same subject. Conflict detected by: concurrent events with overlapping scope (same fields, same time window). Resolution: resolution event that supersedes both | Same as B |
| A decision made offline conflicts with a decision made by someone who synced first | Snapshot with status "approved" arrives after a different "approved" snapshot already synced. Both are preserved. Supervisor resolves | Two approval events from different authors on same case. Both visible. Supervisor's resolution event records the outcome | Same as B |
| Ordering — apparent timeline vs. actual timeline | Snapshots carry `at` (when actually done). Arrival order at server differs. Server uses `at` for logical ordering, arrival timestamp for sync ordering | Events carry `at` + logical clock. Server merges and orders by causal sequence, not arrival time | Same as B, but view update must handle reordering |

**Verdict**: Both handle offline conflicts through the same fundamental mechanism — detecting concurrent writes and surfacing them. Events provide slightly better resolution context because each event describes what the person *did*, not what they *thought the state should be*.

**Critical observation**: Conflict detection in all three options requires the same information — "who wrote what about which subject at what time." The real question is whether the detection mechanism is structural (inherent to the model) or requires separate logic. With events, concurrent writes on the same subject are structurally visible. With snapshots, you need chain-head analysis to find forks.

---

## S3: Identity Generation — What Offline Forces

This sub-decision is tightly constrained:

**Server-allocated sequential IDs require a network roundtrip** to create a new record or register a new subject. Offline workers can't wait. Pre-allocation pools (as DHIS2 does with "reserved values") add complexity and failure modes — what if the pool runs out during extended offline periods?

**Client-generated UUIDs work offline** by definition. Every device can mint identifiers independently. The cost:
- No natural ordering (UUIDs aren't sequential)
- Duplicate real-world subjects get separate UUIDs (S01 edge case: same facility registered twice)
- UUIDs are 128 bits — larger than sequential integers, but storage impact is negligible at the scales involved

**Decision forced**: Client-generated UUIDs. The duplicate-identity problem (same real thing, two UUIDs) is a domain problem, not a storage problem — it requires detection rules and human resolution regardless of how IDs are generated.

---

## S4: Sync Unit — What Follows from the Above

Given append-only records with client-generated IDs, the sync unit is naturally the immutable record itself — whether that is a snapshot or an event.

Properties of the sync:
- **Idempotent**: receiving the same record twice is a no-op (it already exists, it's immutable)
- **Append-only**: the server never tells a device to delete or modify a record
- **Order-independent**: records can arrive in any order, because each carries its own timestamp and references
- **Scoped**: a device receives only records relevant to its assigned subjects and areas (determined by ADR-3)

This is consistent across all three options. The sync protocol is essentially: "give me all immutable records you have that I haven't seen yet, filtered to my scope." The difference is only in payload size per record (snapshots are larger, events are smaller).

---

## S5: Conflict Semantics — What Principle P5 Forces

P5 states: "Conflict is surfaced, not silently resolved." This eliminates:
- Last-write-wins (silent loss)
- Automatic merge without visibility (silent resolution)

What remains:
- **Detect and surface**: the system identifies concurrent writes on the same subject and presents them for human resolution
- **Automatic resolution where provably safe + human triage otherwise**: some conflicts are structurally resolvable (additive fields from different authors, non-overlapping data). Others require judgment. The system should distinguish them

This applies equally to all three options. The conflict semantics are a policy layer on top of the data model, not an intrinsic property of it.

**One nuance**: events provide richer conflict context. Two concurrent snapshots say "here are two versions of the state." Two concurrent events say "person A did X while person B did Y." The latter gives a resolver more to work with.

---

## Device Storage and Performance Realities

The constraints document says: low-end Android phones, field workers as the highest-volume users.

**Storage budget estimate**:
- Low-end Android: 8–16 GB total, realistically 500 MB–1 GB available for the app
- A CHV with 300 assigned subjects, 30 records/day, offline up to 30 days
- 30 records/day × 30 days = 900 new records per offline stretch
- Plus historical data for assigned subjects: ~300 subjects × ~20 records each = 6,000 historical records

| Data model | Storage per record (avg) | 900 new records | 6,000 historical | Total |
|---|---|---|---|---|
| Snapshots | 1–3 KB (full data per snapshot) | 0.9–2.7 MB | 6–18 MB | 7–21 MB |
| Events | 200–500 bytes (delta only) | 180–450 KB | 1.2–3 MB | 1.4–3.5 MB |
| Events + materialized views | 200–500 bytes (events) + 1–2 KB (per subject view) | 180–450 KB + 300–600 KB | 1.2–3 MB | 1.7–4 MB |

**Conclusion**: Storage is not a differentiator. At these scales, all options fit comfortably within the device budget. Even the most generous snapshot estimates (21 MB) are well under 1% of available storage.

**Performance budget**:
- Screen load: must feel instant (<200ms). This means current state must be pre-computed, not computed on demand
- Record creation: must feel instant. Writing an immutable record to SQLite is single-digit milliseconds
- Projection computation (events only): applying 20 events for a subject takes microseconds. Applying 200 events for a case with long history takes low milliseconds. Not a concern for individual subject loads
- Bulk projection rebuild (e.g., after sync receives 500 events): seconds, not minutes. Can run in background

**Conclusion**: Performance is not a differentiator either, provided events use materialized views for reads. Without materialized views, pure event-sourcing on a subject with hundreds of events could lag on low-end hardware — but materialized views are cheap (one row per subject, updated on each new event).

---

## What Each Option Actually Commits You To

Beyond the scenario-by-scenario analysis, each option defines a different long-term trajectory for the platform.

### Snapshots — what you're signing up for

You're signing up for a platform where **the record is the unit of truth**. Every snapshot is self-contained and meaningful on its own. History is a chain of complete records.

This means:
- Developers think in terms of "versions of a record" — each version is a complete document
- The capture and workflow models are unified — both produce snapshots. But this unity is forced: a review (which is semantically an action on someone else's work) must be expressed as "a new version of the work with a review stamp added"
- Storage grows with the breadth of each record × depth of its history
- The system is easy to reason about: "show me the current version of X" → find the chain head
- Cross-subject queries ("how many cases were approved today?") require indexing chain heads

The platform will feel like a **document versioning system** — Google Docs-style revision history applied to structured operational data.

### Events — what you're signing up for

You're signing up for a platform where **what happened is the unit of truth**. The current state of anything is always a derivation. History is native — it's the primary data, not a secondary concern.

This means:
- Developers think in terms of "things that happened to a subject" — each event is an action, not a state
- Capture and workflow are naturally separate event types: a "record_captured" event and a "review_completed" event are different shapes serving different purposes, but they compose on the same subject
- You must build and maintain projection infrastructure — the layer that turns events into current-state views. This is the recurring engineering cost
- Temporal queries ("what did this case look like on March 15?") are free — replay events up to that date
- The system is harder to reason about for developers unfamiliar with event-sourcing: "where does the current state live?" → "it's computed"

The platform will feel like an **operational timeline** — everything is a sequence of things that happened, and "current state" is a derived view of that sequence.

### Action Log (pragmatic hybrid) — what you're signing up for

You're signing up for a platform that **maintains two representations**: an append-only log for traceability and conflict detection, and materialized views for current state. Neither is fully subordinate to the other.

This means:
- Developers write to both the log and the view on every action. The application is the coordinator between these two stores
- Traceability comes from the log; query performance comes from the views
- You get most of the benefits of events (granular history, conflict detection, immutability) without the full commitment to event-sourcing (no replay-from-zero, no temporal queries as a guarantee)
- If log and views diverge, there is a repair procedure (replay from log) but it is not a guaranteed feature — it is a fallback
- The cognitive model is closer to traditional development: "write data, also log what you did"

The platform will feel like a **traditional application with an audit-grade transaction log** bolted in at the foundation level, not as an afterthought.

---

## The Honest Narrowing

### What is actually the same across all three

1. **Append-only storage** — forced by constraints. All three are append-only.
2. **Client-generated UUIDs** — forced by offline-first. All three use them.
3. **Idempotent sync of immutable records** — follows from append-only + UUIDs. All three sync the same way.
4. **Conflict detection** — concurrent writes surface for resolution. All three can do this.
5. **Schema version per record** — each record carries its shape version. All three handle schema evolution the same way.

### What is actually different

| Dimension | Snapshots (A) | Events (B) | Action Log (C) |
|---|---|---|---|
| **What gets stored** | Full state at each point | What happened at each point | What happened (log) + current state (view) |
| **Where current state lives** | Latest chain head (stored) | Projection (computed + cached) | Materialized view (stored, maintained) |
| **Natural fit for workflows** | Weak — forces full-copy for status changes | Strong — transitions are native events | Strong — inherits from event model |
| **Natural fit for capture** | Strong — a capture IS a snapshot | Strong — a capture IS an event | Strong — same as B |
| **Developer familiarity** | Moderate — document versioning is intuitive | Low — event-sourcing is a paradigm shift | Moderate — "save + log" is familiar |
| **Temporal queries** | Traverse chain (possible, not free) | Replay events (native, free) | Not guaranteed without effort |
| **Infrastructure overhead** | Low — one store, indexed chains | Medium — event store + projection layer | Medium — log store + view store + coordination |
| **Single source of truth** | Yes — the snapshot chain | Yes — the event log | No — two co-primary stores |

### The pivotal question

The choice between events and action log reduces to one question:

**Is "single source of truth" a requirement, or is "traceability + performance" sufficient?**

- If single source of truth matters — the system must be able to reconstruct any state from one canonical store — then **events (B)** is the answer. The projection layer is the cost.
- If pragmatic traceability is enough — every action is logged, current state is queryable, but full reconstruction from the log is a repair operation not a guaranteed capability — then **action log (C)** is the answer. Dual-write coordination is the cost.

Snapshots (A) remain viable only if the platform will never have significant workflow scenarios. Given that S04, S07, S08, S11, and S14 are all Phase 1, this is not the case.

---

## How This Constrains Downstream Decisions

Regardless of which option is chosen, the following is fixed for all downstream ADRs:

| Downstream decision | What this ADR constrains |
|---|---|
| **ADR-2: Identity** | Subject IDs are client-generated UUIDs. Duplicate detection is a domain-layer concern, not a storage concern. |
| **ADR-3: Auth/Sync** | Sync transfers immutable records filtered by scope. Auth rules must be evaluable against local state (materialized or projected). |
| **ADR-4: Configuration** | Configuration metadata (shapes, assignments, rules) lives alongside operational data but is a separate concern. Configuration changes produce their own immutable records. |
| **ADR-5: Workflow** | If events or action log: state transitions are distinct from captures. Workflow is a series of typed actions on a subject, not a series of full-state replacements. |

If **events (B)** is chosen, ADR-5 gets simpler (state machines project naturally from event streams) but ADR-4 gets harder (the configuration must define event types, projection rules, and how new event types compose with existing projections).

If **action log (C)** is chosen, ADR-4 and ADR-5 are both moderately complex (configuration defines log entry types + materialized view schemas, workflow is modeled in the view layer with log entries as the audit trail).

---

## Decision Inputs Summary

What the constraints forced (not open for decision):
- Append-only immutable records
- Client-generated UUIDs
- Idempotent, order-independent sync
- Conflict detection with human resolution as default
- Schema version attached to every record

What remains to decide:
1. **Snapshots vs. Events vs. Action Log** — the central choice
2. **If events or action log: materialized view maintenance strategy** — how views are kept current on-device and server
3. **Causal ordering mechanism** — hybrid logical clocks vs. simpler alternatives for establishing event order across devices

What does NOT need to be decided here (belongs in later ADRs):
- Conflict resolution policies (ADR-2)
- What gets synced to which devices (ADR-3)
- How configuration is expressed (ADR-4)
- How state machines work (ADR-5)
