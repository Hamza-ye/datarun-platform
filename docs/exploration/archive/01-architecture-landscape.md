> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-001**. For a navigated reading guide, see [guide-adr-001.md](../guide-adr-001.md).
# Architecture Landscape Exploration

> This document maps the viable architecture space for Datarun. It does not make decisions — it identifies the families of approach that survive the constraint filter, learns from prior art, reveals which decisions are coupled, and proposes a sequence for committing to them.

---

## 1. What the Constraints Rule Out

Five hard constraints from the vision and the constraints document shape the architecture space. Each eliminates broad families of approach.

**Offline-first on low-end Android** eliminates:
- Pure server-rendered web applications (no network means no app)
- Thin-client architectures that keep logic or state server-side
- Architectures that treat offline as a cache or graceful degradation — offline is the primary operating mode, not a fallback
- Anything that requires iOS or desktop as the primary field client

**Configurable, not coded** eliminates:
- Architectures where adding a new operational activity requires developer intervention in application code
- Hard-coded data models, workflows, or business rules in the client application
- Approaches where the client app must be rebuilt and redeployed for configuration changes

**Trustworthy records with full traceability** eliminates:
- Mutable-state architectures where records are updated in place (the "before" is lost)
- Last-write-wins as a default conflict resolution (silent data loss)
- Architectures where the audit trail is a separate, optional layer rather than inherent to the data model

**Tens of thousands of users, millions of records** eliminates:
- Architectures where every device carries the full dataset (the data doesn't fit)
- Sync approaches that transfer complete state rather than changes
- Single-database architectures without partitioning or sharding for the server tier

**Grows without breaking** eliminates:
- Schema-on-write without evolution support (adding a field breaks existing records)
- Monolithic workflow engines where adding a new pattern requires modifying existing ones
- Architectures where the configuration model is a fixed vocabulary rather than a composable one

**What survives**: The viable architecture must have a thick client (substantial logic and storage on-device), a metadata-driven configuration model (the client interprets configuration, not hard-coded behavior), an immutable or append-only data model (records are never silently overwritten), selective sync (devices carry only relevant data), and a composable configuration language (new patterns don't break old ones).

---

## 2. Prior Art — What Comparable Platforms Did and Where They Hit Walls

Four platforms operate in overlapping problem space. Each made different architectural bets. Understanding where they succeeded — and more importantly, where they hit walls — reveals what Datarun should do differently.

### DHIS2 — The Metadata-Driven Generalist

**What it is**: Open-source health information system used at national scale in 70+ countries. Server-side Java/Spring with a PostgreSQL backend. Android app is fully metadata-driven — all configuration flows from the server.

**Architecture approach**: Everything is metadata. Data elements, organization units, programs, indicators, validation rules — all defined as configuration objects in the database. The Android app downloads this metadata and interprets it locally to render forms, enforce access, and guide workflows. The data model is generic: every captured value is a (DataElement, Period, OrgUnit, Value) tuple.

**What it got right**:
- The metadata-driven Android app means new programs can be deployed without touching application code. This is the strongest existing proof that "configured not built" is achievable for field operations.
- The generic data model (DataElement as the atomic unit) allows DHIS2 to serve aggregate reporting, event capture, and longitudinal tracker programs with the same infrastructure.
- Sync is scoped to the user's assigned organization units and programs, keeping device data manageable.

**Where it hit walls**:
- **Configuration complexity becomes its own expertise**. Setting up a DHIS2 instance correctly requires deep understanding of metadata relationships, user permissions, sharing settings, and their interactions. The promise of "configured not built" shifts the expertise from developers to DHIS2 configuration specialists — a different skill set, but not a simpler one.
- **The generic data model is flat**. The (DataElement, Period, OrgUnit, Value) tuple works well for aggregate data, but longitudinal case tracking (Tracker) required a fundamentally different data model bolted on top. The two models coexist but don't compose — they are separate subsystems with separate sync, separate conflict handling, and separate analytics pipelines.
- **Offline sync conflicts are surfaced but not well-resolved**. Conflicting records enter an ERROR/WARNING state that blocks upload until manually fixed. The user must understand the conflict and modify the data. For a field worker with basic digital literacy, this is an unsolvable prompt.
- **Form size and org unit limits on devices**. Large forms or deep organizational hierarchies push against device memory constraints. The platform advises limiting org units below 250 per device — a significant constraint for large deployments.
- **Schema evolution is a server-side operation**. Changing metadata requires server access. Devices pick up changes on next sync. But there is no explicit handling of "work in progress under old schema" vs. "new work under new schema" — the expectation is that sync happens frequently enough that the gap doesn't matter. When it does matter (extended offline periods), the behavior is undefined.

**What Datarun can do differently**: Treat case/longitudinal tracking and aggregate reporting as compositions of the same primitives, not separate subsystems. Make conflict resolution an explicit, configurable policy (not a manual error-fix workflow). Handle schema evolution as a first-class concern with explicit version coexistence, not an assumption that sync keeps everyone current.

### CommCare — The Form-First Case Manager

**What it is**: Mobile data collection and case management platform deployed in 130+ countries, used by over 1 million frontline workers. Django/PostgreSQL backend with XForms-based mobile client.

**Architecture approach**: Two core abstractions: **Forms** (XForms that capture data through a questionnaire flow) and **Cases** (longitudinal records that track subjects across multiple form submissions). Forms produce data; cases accumulate it. The mobile client runs forms offline and syncs form submissions + case updates to the server. Asynchronous processing via Kafka populates secondary databases for reporting.

**What it got right**:
- The Form + Case duality is powerful for community health. Forms handle the capture experience (structured, guided, with skip logic and validation). Cases handle the continuity (a person registered once, followed over time through multiple form submissions).
- Offline-first from day one. The mobile client is opinionated about offline: all work happens locally, sync is a background operation.
- XLSForm authoring (spreadsheet-based form design) dramatically lowers the configuration barrier for non-developers.

**Where it hit walls**:
- **The form is the only interaction model**. Everything must be expressed as a form submission. Distribution tracking, multi-level approvals, event-triggered actions — all must be shoehorned into "fill out a form." This works for data collection but becomes awkward for coordination and workflow scenarios.
- **Questionnaire fragility**. Even small changes to a form structure can require large adjustments to the application configuration. This is the opposite of "grows without breaking" — the form schema and the application logic are tightly coupled.
- **Complex feature interactions**. Features like Display Only Forms, Select Parent First, End of Form Navigation, Form Linking, Child Modules, and Shadow Modules interact unpredictably. This complexity is inherent to trying to make a form engine do workflow.
- **Case sync at scale becomes expensive**. Cases are synced to devices based on ownership rules. For large case loads, the initial sync is slow and subsequent syncs must diff the full case state. The sync model was designed for individual workers with manageable case loads, not for supervisors who need cross-cutting visibility.
- **Offline data loss risk**. If a phone is lost or damaged before sync, all unsynced data is gone. There is no peer-to-peer failsafe or local backup mechanism.

**What Datarun can do differently**: Separate the capture experience (recording information) from the workflow experience (state progression, handoffs, approvals). Don't force everything through a form metaphor. Make configuration changes non-breaking — records captured under the old configuration remain valid, new records follow the new configuration, both coexist. Design sync for hierarchical visibility from the start, not as an afterthought.

### ODK / Kobo — The Capture Purist

**What it is**: Open-source data collection toolkit. ODK Collect (Android app) captures data via XForms, uploads to ODK Central (server). Kobo Toolbox adds a web-based form builder on top. Widely used for surveys, assessments, and monitoring.

**Architecture approach**: Single abstraction: the **form submission**. A form is defined as an XForm (XML spec with logic, constraints, and multi-language support). The mobile app downloads form definitions, presents them to users offline, captures responses, and uploads completed submissions. The server stores submissions and provides basic export/analysis.

**What it got right**:
- Extreme simplicity for the core use case. If your need is "go to the field, fill out a form, upload the data," ODK does this exceptionally well with minimal overhead.
- The XForms standard is well-documented, mature, and supported by multiple compatible tools.
- The separation of form definition from form presentation allows different clients to render the same form differently.

**Where it hit walls**:
- **No case management**. ODK captures discrete submissions. There is no built-in concept of a persistent subject that accumulates records over time. Each submission is a standalone document. ODK-X was created specifically to address this gap, but it is a fundamentally different tool, not an extension of ODK.
- **No workflow**. There is no state progression, review, approval, or assignment model. A submission is final when it is submitted. There is no "draft → reviewed → approved" pipeline.
- **Nested data structures are painful**. XForms supports repeating groups, but the mapping to relational storage is complex. Different tools handle the same nested structure differently, breaking interoperability.
- **Configuration is form-scoped**. Each form is configured independently. There is no shared schema, shared registry, or cross-form relationships. This means no scenario 13 (cross-flow linking), no scenario 06 (shared registry across activities), and no scenario 15 (multiple views of the same data).
- **XForms XML complexity**. Despite XLSForm simplification, advanced form logic still requires XML knowledge. The specification itself is a subset of W3C XForms with extensions — a "standard" that only the ODK ecosystem fully implements.

**What Datarun can do differently**: Start with the recognition that capture is one primitive, not the only primitive. Build identity, state progression, assignment, and cross-referencing as first-class concepts alongside capture, not as extensions bolted onto a form engine.

### OpenSRP — The Standards-First Task Engine

**What it is**: Open-source FHIR-native platform for community and facility health services. Built on Google's Android FHIR SDK with HAPI FHIR backend. Used in 14 countries with 150+ million patients registered.

**Architecture approach**: Health data standards (FHIR resources) as the data model. The client application renders UI from FHIR Questionnaire resources, stores data as FHIR resources locally (via Android FHIR SDK + SQLite), and syncs to a HAPI FHIR server. Configuration is delivered as FHIR Composition and Binary resources synced to the device. Task management follows the FHIR Task resource model: care plans generate tasks, tasks are assigned to workers, workers complete tasks through questionnaire submissions.

**What it got right**:
- FHIR-native means interoperability is structural, not an integration layer. Data can flow to any FHIR-compatible system without custom adapters. This is the closest existing approach to "one system that plays well with others."
- The task-driven model (care plan → task → questionnaire → outcome) provides a natural workflow structure that goes beyond pure data collection.
- Peer-to-peer sync for areas with no connectivity at all — devices can share data directly.

**Where it hit walls**:
- **Health-domain lock-in**. FHIR is a health data standard. Resources like Patient, Encounter, CarePlan, and Observation are health-specific. Using OpenSRP for logistics, agriculture, or humanitarian response would mean either misusing health resources as generic containers or building a parallel resource model — defeating the standards benefit.
- **Android 8.0+ minimum**. The dependency on Google's Android FHIR SDK requires API level 26. This excludes older devices that are common in low-resource settings.
- **Analytics require separate infrastructure**. On-device analytics are minimal. Meaningful reporting requires replicating data to a data warehouse (Cloud Healthcare API → BigQuery). The platform handles capture and task management but not operational oversight.
- **Configuration is still developer-adjacent**. While FHIR resources carry configuration, authoring those resources (care plans, questionnaire definitions, decision support logic) requires understanding of FHIR semantics. This is not "set up, not built" for a domain expert — it's "set up by a FHIR specialist."
- **The FHIR resource model is verbose and deep**. Representing simple operational data (a count, a status, a note) as full FHIR resources with extensions, codings, and references creates overhead that is defensible in health interoperability contexts but unnecessary for generic field operations.

**What Datarun can do differently**: Be domain-agnostic in its data model, not locked into health semantics. Achieve interoperability through structured export/import with standard mappings, not by adopting a domain-specific standard as the internal model. Keep the on-device data model lean — every byte matters on low-end devices with limited storage. Make analytics and oversight a first-class capability, not a "connect to BigQuery" afterthought.

### Cross-Cutting Lessons

| Problem | DHIS2 | CommCare | ODK | OpenSRP | What Datarun needs |
|---------|-------|----------|-----|---------|-------------------|
| Capture | Generic tuple | XForm submission | XForm submission | FHIR Questionnaire | Structured record tied to a defined shape, not a single mechanism |
| Identity | OrgUnit + TrackedEntity | Case | None | FHIR Patient/Group | Domain-agnostic identifiable subject with lifecycle |
| Workflow | Limited (Tracker stages) | Form-driven | None | Task from CarePlan | State progression as a first-class primitive, independent of capture |
| Offline | Metadata sync + local capture | Full offline forms + cases | Local form fill + upload | FHIR SDK local store + P2P | Complete local capability with selective sync and explicit conflict handling |
| Config model | Metadata objects | XLSForm + app config | XForms | FHIR resources | Composable, layered: simple for common patterns, expressive for complex ones |
| Schema evolution | Server-side metadata change, implicit versioning | Fragile — form changes break app | Form-scoped, no evolution | FHIR resource versioning | Explicit version coexistence, old and new shapes in parallel |
| Scale | National-level proven but complex | Individual worker scope, supervisor visibility is weak | Single-device, no hierarchy | 150M patients but analytics offloaded | Hierarchical visibility from the start, selective sync by assignment scope |
| Interop | FHIR/ADX adapters | Custom APIs | OpenRosa standard | FHIR-native | Structured export/import, not a domain-locked standard |

**The gap Datarun fills**: None of these platforms treat capture, identity, workflow, assignment, oversight, and cross-referencing as composable primitives within a single, domain-agnostic model. Each started with one strong primitive (DHIS2: metadata-driven capture; CommCare: form + case; ODK: form submission; OpenSRP: FHIR task) and bolted on others as the use cases expanded. The bolted-on parts are where they struggle. Datarun's opportunity is to design these primitives as peers from the start, composable into the full range of operational scenarios without one primitive dominating the architecture.

---

## 3. Viable Architecture Families

Three families survive the constraint filter. Each represents a different philosophy about where the "center" of the platform sits.

### Family A: Metadata-Driven Engine

**Philosophy**: The platform is a generic interpreter. All behavior — what to capture, how to track it, who can see it, what happens next — is defined as metadata (configuration objects). The client app downloads metadata and interprets it at runtime. The app code itself is unchanged across deployments; only the metadata differs.

**How it handles the five commitments**:
- *Offline*: Metadata is synced to the device. The engine interprets it locally. All operations happen against local data with local metadata. Strong fit.
- *Set up, not built*: This is the defining strength. New activities are purely metadata — no app changes, no redeployment. Strong fit.
- *Trustworthy records*: Orthogonal — the engine can implement immutable records regardless of the metadata model. Neutral.
- *One system*: The engine is one system by definition. All metadata follows the same schema. Strong fit.
- *Grows without breaking*: Depends on the metadata model's extensibility. If the metadata schema itself is composable (new metadata types can be added without changing the engine), strong fit. If the metadata schema is a fixed vocabulary, this breaks at the edges.

**Which primitives it serves well**: Structured Record, Schema/Shape Definition, Responsibility Binding, Hierarchical Visibility, Temporal Rhythm — all naturally expressed as metadata.

**Where it struggles**: State Progression and Condition-Based Triggers are hard to express as pure metadata without the metadata language becoming a programming language. Review/Judgment requires the engine to understand workflow semantics, not just data semantics. The configuration-boundary tension (T2 from viability) is most acute in this family.

**Precedent**: DHIS2 is the strongest example. Its metadata-driven approach works at national scale. Its limitations (configuration complexity, flat data model, bolted-on tracker) are instructive constraints, not disqualifications.

### Family B: Event-Sourced Operational Log

**Philosophy**: The platform records everything as a sequence of immutable events. The current state of any record, subject, or workflow is a projection computed from its event history. Events are the source of truth; views (current state, dashboards, reports) are derived.

**How it handles the five commitments**:
- *Offline*: Events are generated locally, stored in a local event log, and synced when connectivity returns. The server merges event streams. Conflict detection is structural — two events touching the same subject are visible as concurrent writes. Strong fit.
- *Set up, not built*: Depends on how event types and projection rules are defined. If these are configurable, strong fit. If they require code, weak.
- *Trustworthy records*: This is the defining strength. The event log IS the audit trail. Every action, correction, and decision is an event. Nothing is lost or overwritten. Strong fit.
- *One system*: Events follow a common envelope (who, what, when, where, type, payload). All operational activities produce events into the same stream. Strong fit.
- *Grows without breaking*: New event types can be added without affecting existing ones. New projections can be built from the same events. Old projections continue to work. Strong fit.

**Which primitives it serves well**: Structured Record (events), Identifiable Subject (event streams per subject), State Progression (state as event projection), Review/Judgment (review decisions as events), Transfer/Handoff (events at each transfer point), Cross-Reference/Link (events that reference other event streams).

**Where it struggles**: Temporal Rhythm and Responsibility Binding are not naturally event-sourced — they are structural configuration, not state derived from events. The "what should the device know about?" question (selective sync scope) is harder with events — do you sync the full event stream for relevant subjects, or only recent projections? Device storage for event logs may be expensive on low-end hardware. Schema/Shape Definition (what information to collect) is a configuration concern, not an event concern — this family needs to be combined with a configuration layer.

**Precedent**: Less common in the field operations space. The offline-first + event-sourcing pattern is well-documented in distributed systems literature (CQRS+ES, outbox pattern) but has not been deployed at the scale of DHIS2 or CommCare for field health work. This is both a risk (unproven at scale in this domain) and an opportunity (no incumbent has done it well).

### Family C: Composable Primitive Engine

**Philosophy**: The platform defines a small set of orthogonal primitives (record, subject, binding, progression, rhythm, trigger) and a composition model for combining them. Each operational activity is a declared composition: "this activity involves capturing records about known subjects, with assigned responsibility, on a weekly rhythm, with supervisor review." The engine interprets compositions.

**How it handles the five commitments**:
- *Offline*: Primitives operate locally. Sync is per-subject or per-scope. Compositions don't change the sync model — they change what the primitives do. Strong fit if the primitives themselves are offline-capable.
- *Set up, not built*: Configuration is composition. A new activity is "these primitives, composed this way." Strong fit — the vocabulary is constrained to the primitive set and their composition rules.
- *Trustworthy records*: Depends on the record primitive. If records are immutable with append semantics, strong fit. If records are mutable, weak.
- *One system*: All activities compose from the same primitives. The system is unified by its primitive vocabulary. Strong fit.
- *Grows without breaking*: New compositions don't affect existing ones. New primitives can be added to the vocabulary. Existing compositions continue to work because they only reference primitives they know about. Strong fit.

**Which primitives it serves well**: All 12 viability primitives are directly expressible — this family was designed around them.

**Where it struggles**: The composition model itself is the hard problem. How do primitives interact? If "record" and "progression" are composed, does the progression fire when a record is created? Is that a trigger primitive or an implicit composition rule? The risk is that the composition rules become a DSL that is effectively a programming language — the same T2 tension, expressed differently. Additionally, no existing platform uses this approach at scale — it is a theoretical architecture with no field-proven precedent.

**Precedent**: Conceptually similar to entity-component-system (ECS) patterns in game engines and Salesforce's metadata + object + flow composition model. But no field operations platform has taken this approach. The closest analogy is what Datarun's own viability assessment suggests — it describes 12 primitives in 6 groups, composable across scenarios.

### Comparison Matrix

| Criterion | A: Metadata Engine | B: Event-Sourced | C: Composable Primitives |
|-----------|-------------------|------------------|--------------------------|
| Offline-first | Strong | Strong | Strong (if primitives are) |
| Set up, not built | Defining strength | Needs config layer | Strong (composition IS config) |
| Trustworthy records | Neutral (can be added) | Defining strength | Depends on record primitive |
| One system | Strong | Strong | Strong |
| Grows without breaking | Depends on metadata extensibility | Strong (new event types) | Strong (new compositions) |
| Configuration boundary risk | High (metadata → language) | Medium (event types bounded) | High (composition rules → DSL) |
| Field-proven at scale | Yes (DHIS2) | No | No |
| Handles all 12 primitives | 8-9 naturally, 3-4 awkward | 9-10 naturally, 2-3 need config layer | 12 by design, 0 proven |

### The Honest Assessment

These families are not mutually exclusive. The most viable path is likely a **hybrid** that combines:

- **Event-sourced storage** for records and state (gives trustworthy records and offline conflict handling for free)
- **Metadata-driven configuration** for what to capture, who is responsible, how oversight works (gives "set up, not built")
- **Composable primitive vocabulary** for describing how activities combine capture, identity, workflow, and oversight (gives "one system" and "grows without breaking")

This hybrid is essentially: **a metadata-driven engine whose runtime state is event-sourced and whose configuration language is a constrained composition of defined primitives.**

The risk of this hybrid is complexity — three architectural ideas in one system. The mitigating factor is that each idea solves a specific commitment, and none of the commitments can be easily sacrificed. The question is not "which family?" but "how simply can these three ideas be combined?"

---

## 4. Primitive Realization Matrix

How would each of the 12 viability primitives be realized under each family? This reveals where each approach is natural and where it strains.

| Primitive | A: Metadata Engine | B: Event-Sourced | C: Composable Primitives | Hybrid |
|-----------|-------------------|------------------|--------------------------|--------|
| **Structured Record** | Metadata defines fields; engine renders capture form | Event with typed payload; shape is metadata | Record primitive with shape parameter | Metadata defines shape; capture produces immutable event |
| **Identifiable Subject** | Metadata object (like DHIS2 TrackedEntity) | Event stream keyed by subject ID | Subject primitive with identity rules | Subject is metadata; its history is an event stream |
| **Responsibility Binding** | Metadata assignment (user → scope) | Not naturally an event; configuration concern | Binding primitive (who → what) | Metadata assignment; changes are events |
| **Temporal Rhythm** | Metadata schedule (period type + expected cadence) | Not naturally an event; configuration concern | Rhythm primitive (cadence + expectation) | Metadata schedule; compliance checked by projection |
| **State Progression** | Awkward — metadata defines states but engine needs workflow semantics | Natural — state is projection of transition events | Progression primitive with declared stages | State transitions are events; stage definitions are metadata |
| **Hierarchical Visibility** | Metadata org hierarchy + sharing rules | Events carry org context; projections filter by scope | Visibility as primitive rule on other primitives | Org hierarchy is metadata; events inherit it; projections respect it |
| **Review / Judgment** | Metadata defines review step; engine triggers it | Review decision is an event; triggers next stage | Composition of progression + binding | Review is a transition event; the step is metadata |
| **Transfer / Handoff** | Metadata defines transfer parties; engine records exchange | Send/receive are paired events | Composition of progression + binding + record | Transfer is send-event + receive-event; parties are metadata |
| **Condition-Based Trigger** | Rules in metadata — T2 stress point: how expressive? | Events evaluated against rules — when? On sync? | Trigger primitive with condition parameter — same T2 stress | Rules are metadata; evaluated against event projections on sync |
| **Cross-Reference / Link** | Metadata link type; engine resolves | Events reference other event streams by ID | Link primitive between subject streams | Events carry references; metadata defines valid link types |
| **Schema / Shape Definition** | Core strength — metadata IS the schema definition | Not an event concern; needs metadata layer | Shape as parameter of record primitive | Metadata defines shapes; events carry shape version |
| **Offline Local State** | Metadata + data synced to device; local engine | Event log on device; projections computed locally | All primitives operate locally | Device has metadata + local event log; projects state locally |

**Key observations from the matrix**:

1. No single family handles all primitives naturally. Family A struggles with state progression and triggers. Family B struggles with configuration concerns (responsibility, rhythm, shape). Family C handles everything by definition but has zero proof.

2. The hybrid column shows the cleanest realization across all primitives. Metadata handles "what" (shapes, assignments, schedules, rules). Events handle "happened" (captures, transitions, transfers, reviews). Projections handle "current state" (visibility, compliance, conflicts).

3. The hardest primitive to realize is **Condition-Based Trigger** — it stresses the configuration boundary regardless of family. This confirms the viability assessment's Risk R3 (scenario 12 becoming a rules engine).

4. **Offline Local State** is the most cross-cutting — it demands that whichever approach is chosen, it must work entirely on-device. This is not a primitive to "add later" — it is a constraint on every other primitive.

---

## 5. Critical Decision Intersections

Some architectural decisions are independent — they can be made in any order. Others are coupled — choosing one constrains the options for another. Understanding the coupling determines the order of `/ade` sessions.

### Intersection 1: Offline Data Model x Schema Evolution

**The coupling**: The storage primitive (how records are stored on-device and on-server) directly constrains how schema evolution works.

- If records are mutable state objects, schema evolution must migrate existing records or maintain a compatibility layer.
- If records are immutable events/snapshots, each record carries its own schema version, and old and new shapes coexist naturally with no migration.
- If the device stores projections (current state), schema changes require recomputing projections from underlying events — but events may not be available on-device for long-offline workers.

**Decision dependency**: The offline data model must be decided before schema evolution can be designed.

### Intersection 2: Configuration Paradigm x Offline Constraint

**The coupling**: Whatever configuration model is chosen, it must be fully evaluable on-device without network.

- If configuration includes rules like "when condition X is met, do Y," the condition evaluation engine must run locally against local data.
- If configuration references server-side computed views (aggregates, analytics), those views must be projected locally or the configuration cannot reference them.
- If configuration changes during an offline period, the device runs old configuration until sync. The design must handle two configuration versions coexisting on the same device (old work continues, new work starts fresh).

**Decision dependency**: The offline data model determines what data is available locally. The configuration paradigm determines what can be evaluated against it. They must be designed together.

### Intersection 3: Identity Model x Conflict Resolution

**The coupling**: How subjects are identified determines what "conflicting records about the same thing" means.

- If identity is a single server-assigned ID, offline creation of new subjects requires pre-allocated ID pools (like DHIS2's reserved values) or client-generated IDs with server-side deduplication.
- If identity is client-generated (UUIDs), two people recording the same real-world thing will create two identities that must be merged later.
- The conflict resolution strategy depends on whether "same subject" can be detected automatically (matching rules) or requires human judgment.

**Decision dependency**: The identity model shapes what conflicts are possible. Conflict resolution must be designed for the identity model's specific failure modes.

### Intersection 4: Authorization Model x Offline Enforcement

**The coupling**: Access control rules must be enforceable on-device against potentially stale state.

- If authorization is role + context based (which it is, per the access control document), the device must know the user's current roles, their assigned scope, and the access rules — all potentially stale.
- If roles can change while offline (a person is reassigned centrally), the device enforces the old role until sync. Work done under the old role is valid but must be attributed correctly.
- If temporary access grants expire while offline, the device may continue to allow access after expiry. The reconciliation on sync must detect and surface this.

**Decision dependency**: The authorization model determines what information must be synced to devices and how its staleness is handled.

### Coupling Map

```
Offline Data Model ──────┬──── Schema Evolution
           │              │
           │              ├──── Configuration Paradigm
           │              │
           └──── Identity Model ──── Conflict Resolution
                          │
Authorization Model ──────┘
                 │
                 └──── Selective Sync Scope
```

The offline data model is the root of the dependency tree. Almost every other decision is downstream of it.

---

## 6. Decision Sequence for `/ade`

Based on the coupling analysis, decisions should be made in this order. Each `/ade` session takes the previous decision as a fixed input.

### ADR-1: Offline Data Model

**What to decide**: The storage primitive — how records are created, stored, and synced. Immutable snapshots vs. event log vs. mutable state. Client-generated IDs vs. server-allocated. Sync granularity (record-level, subject-level, event-level).

**Why first**: It is the root of the dependency tree. Every other decision is constrained by it. It is also the least reversible — changing the storage primitive after implementation would require a data migration.

**Inputs**: Constraints document (devices, connectivity, scale), viability primitives (Structured Record, Identifiable Subject, Offline Local State), Behavioral Pattern (P01 — Structured Recording, P11 — Shape Definition and Evolution, P12 — Offline-First Work), scenario 00 "what makes this hard" (corrections, duplicates), scenario 19 "what makes this hard" (conflicts, ordering, stale state).

### ADR-2: Identity and Conflict Resolution

**What to decide**: How subjects are identified (client-generated UUID, composite key, matching rules). What constitutes a conflict (same subject, same period, same author?). How conflicts are resolved (automatic policies, human triage, or both).

**Why second**: Directly constrained by ADR-1 (the storage primitive determines what "same record" means). Required input for ADR-3 (authorization needs identity) and ADR-4 (configuration references subjects).

**Inputs**: ADR-1 decision, scenario 01 "what makes this hard" (duplicate identities, ambiguous identity), scenario 06 "what makes this hard" (deactivated subjects still referenced), viability primitives (Identifiable Subject, Cross-Reference/Link), Behavioral Patterns (P02 — Subject Linkage, P10 — Cross-Reference).

### ADR-3: Authorization and Selective Sync

**What to decide**: How access rules are represented, how they are enforced on-device, how sync scope is determined (what data goes to which device), and how stale access rules are handled after sync.

**Why third**: Requires the identity model (ADR-2) to define what "scope" means. The data model (ADR-1) determines what can be synced. Access rules must be evaluable against local state.

**Inputs**: ADR-1 and ADR-2 decisions, access control document (all six principles + "where this gets hard"), constraints document (user tiers, connectivity profiles), viability primitives (Responsibility Binding, Hierarchical Visibility), Behavioral Patterns (P04 — Responsibility Binding, P05 — Hierarchical Visibility).

### ADR-4: Configuration Paradigm and Boundary

**What to decide**: How operational activities are defined (metadata vocabulary, composition rules). Where the configuration ceiling sits (what is configurable vs. what requires development). How configuration changes propagate and coexist with in-progress work.

**Why fourth**: Requires ADR-1 (configuration must be compatible with the storage primitive), ADR-2 (configuration references subjects), and ADR-3 (configuration is subject to access rules). This is the decision the viability assessment flagged as most consequential for the platform's character — but it cannot be made first because it depends on the three preceding decisions.

**Inputs**: ADR-1, ADR-2, ADR-3 decisions, viability tension T2 (configuration expressiveness vs. simplicity), viability risk R1 (configuration boundary collapse), scenario 06b "what makes this hard" (schema evolution while offline), all 12 viability primitives and proposed Behavioral Patterns (the configuration must be able to express compositions of them).

### ADR-5: State Progression and Workflow

**What to decide**: How work moves through stages (review, approval, handoff). Whether state machines are an explicit primitive or emergent from event patterns. How workflow interacts with offline (transitions made offline, confirmed on sync).

**Why fifth**: Requires the data model (ADR-1, transitions as events or state mutations), identity (ADR-2, workflows operate on subjects), and configuration (ADR-4, workflows are configured, not coded).

**Inputs**: ADR-1 through ADR-4 decisions, scenarios 04, 07, 08, 11, 14 (all workflow-heavy), viability primitives (State Progression, Review/Judgment, Transfer/Handoff), Behavioral Patterns (P08 — State Progression).

---

## Summary

The architecture space, after constraint filtering and prior art analysis, converges toward a hybrid approach: metadata-driven configuration, event-sourced or immutable storage, and composable primitive vocabulary. The specific balance between these three ideas — and how simply they can be combined — is what the `/ade` sessions must determine.

The decision sequence starts with the offline data model (the root of the dependency tree), not the configuration boundary (which, despite being the most visible commitment, depends on three prior decisions). Five ADRs, taken in order, should give the platform a complete architectural skeleton.
