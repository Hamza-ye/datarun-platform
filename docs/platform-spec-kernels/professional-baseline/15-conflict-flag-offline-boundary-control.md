# Conflict Flag Offline Boundary Control

Status: Draft dependency-aware control overlay

This document prevents the accepted conflict, flag, and offline-default surfaces from collapsing into one broad implementation boundary. It does not re-decide ADR-001 through ADR-005, does not absorb ADR-006-R through ADR-009 as authority, and does not reuse pre-convergence primitive maps as final architecture.

The key correction is that `accept-and-flag`, `detect-before-act`, conflict detection, flag lifecycle, workflow state exclusion, authorization checks, and offline operation are related controls, not one subsystem. They exist because the platform principles created durable pressure, and ADR-001 through ADR-005 then validated narrower mechanisms.

## Source Basis

Primary inputs:

- `../00-extraction-state.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`
- `08-baseline-acceptance-check.md`
- `09-identity-boundary-control.md`
- `../../principles.md`

Later-source assessments used only as classified assessment material:

- `10-adr006r-flag-semantics-assessment.md`
- `11-adr007-envelope-type-assessment.md`
- `12-adr008-reference-fields-assessment.md`
- `13-adr009-duality-rule-assessment.md`
- `14-pattern-inventory-walkthrough-assessment.md`

Historical context only:

- `../../exploration/27-gap-identification.md`

`27-gap-identification.md` is useful here because it shows the earlier failure mode: broad contract coverage was treated as sufficient to proceed. Under the current baseline, it is context only. Its contract IDs, primitive names, and "zero gaps" conclusion are not authority.

## Authority Rule

`../../principles.md` supplies validated decision guidance, not detailed interface closure. The principles explain why these surfaces must exist. ADR-001 through ADR-005 decide which mechanisms are currently accepted. Later documents may clarify or challenge only through the existing assessment and change-control rules.

Therefore:

- A principle may justify keeping a control visible.
- A principle may not assign ownership to a component.
- A principle may not close an interface that ADR-001 through ADR-005 left open.
- A principle may not promote ADR-006-R through ADR-009 claims into baseline behavior.

## Principle-To-Control Mapping

| Principle Pressure | Accepted Mechanism | Primary Boundary | Control Meaning |
|---|---|---|---|
| P1: Offline is the default | client-generated IDs, local event creation, immutable sync, advisory command validation | Event Log / Storage; Event Envelope / Schema; Assignment / Authority / Sync | Offline capture cannot require complete global knowledge. Sync must reconcile later state knowledge without treating offline creation as exceptional. |
| P2: Configuration has boundaries | platform-fixed structural event types, bounded deployer policy surfaces, Pattern Registry mechanism | Configuration; Projection / Workflow State | Deployer policy may configure thresholds/severities/shape bindings, but must not define platform detection engines, structural event vocabulary, or arbitrary authority logic. |
| P3: Records are append-only | immutable event log, corrections and resolutions as events | Event Log / Storage; Flag / Resolution | Valid operational facts are preserved. Resolution changes interpretation/projection, not historical event identity. |
| P4: Patterns compose; exceptions do not | workflow patterns, composition rules, source-chain traversal | Projection / Workflow State; Flag / Resolution | Workflow conflict behavior must compose through platform-fixed patterns and references, not special-case process identity or ad hoc flag cascades. |
| P5: Conflict is surfaced, not silently resolved | accept-and-flag, single-writer resolution, rejection of last-write-wins | Flag / Resolution | State anomalies must be visible and resolvable. Automatic resolution is bounded to accepted eligible cases and must remain auditable. |
| P6: Authority is contextual and auditable | assignment-derived access, authority-as-projection, original-subject authorization | Assignment / Authority / Sync | Authorization conflicts are detected from assignment and sync knowledge, not from stored immutable authority snapshots or identity projection shortcuts. |
| P7: Simplest scenario stays simple | S00 avoids workflow patterns, flags, triggers, and special resolution in the happy path | All boundaries | Conflict/flag/offline machinery must be opt-in by scenario pressure and must not burden basic structured capture. |

## Control Vocabulary

Use these terms narrowly during atomization:

| Term | Control Definition | Not Allowed To Mean |
|---|---|---|
| structural validation | envelope, payload, schema, and platform-vocabulary checks required before accepting an event as structurally valid | state, authority, workflow, or domain correctness |
| accept-and-flag | validly structured work is accepted as immutable history and state anomalies are surfaced rather than silently repaired | accept malformed events; accept every online-only operation; bypass validation |
| conflict detection | evaluation of possible state, identity, authorization, workflow, or configured-domain anomaly using facts owned by other boundaries | one universal owner of all anomaly facts or all flag lifecycle |
| flag lifecycle | surfaced anomaly, resolver routing, resolution state, and accepted resolution effects | identity lifecycle, assignment validity, workflow state ownership, or reporting semantics |
| detect-before-act | checks run before downstream policy, trigger, workflow-state, or resolution effects rely on an event | reject-before-persist for all conflicts; device must know every global conflict offline |
| offline default | local work can be created without a network round trip, then synchronized and reconciled | every operation is offline-capable; server/global checks are unnecessary |
| advisory device validation | local warning or prevention UX based on available local/config state | canonical flag creation or global conflict closure |

## Dependency Split

| Responsibility | Owning Boundary | Cross-Boundary Contract | Forbidden Broadening |
|---|---|---|---|
| event structural validity | Event Envelope / Schema | accepted event envelope and schema contract | Do not turn state anomalies into structural invalidity unless baseline says so. |
| immutable acceptance | Event Log / Storage | append-only write contract | Do not mutate, delete, or rewrite accepted events to resolve conflicts. |
| subject lineage facts | Identity / Lineage | raw-reference and lineage-fact contract | Do not make Identity own general conflict lifecycle or domain matching policy. |
| authorization facts | Assignment / Authority / Sync | assignment projection and original-subject authorization contract | Do not store immutable `authority_context` or authorize through post-merge alias shortcuts. |
| sync knowledge and delivery scope | Assignment / Authority / Sync | immutable, idempotent, scope-filtered sync contract | Do not require devices to hold global state for ordinary offline capture. |
| deployer policy values | Configuration | bounded policy and atomic config package contract | Do not let deployers author arbitrary detector code or structural event semantics. |
| workflow transition validity | Projection / Workflow State | Pattern Registry and transition-evaluation contract | Do not reject invalid workflow transitions instead of accepting and flagging. |
| source flags and resolution | Flag / Resolution | flag lifecycle and resolution event contract | Do not absorb source facts, workflow state, authorization rules, or reporting UX into flags. |
| downstream trigger execution | Trigger / Reactivity | detect-before-act and event-store write contract | Do not let flagged or unresolved events create irreversible downstream work. |
| aggregate visibility | Reporting / Aggregation | projection, access-scope, and freshness contracts | Do not make reports the source of conflict truth or identity truth. |
| local retain/remove behavior | Local Data Lifecycle | scope-change lifecycle contract | Do not mutate central immutable history or weaken sensitive-data handling. |

## Accept-And-Flag Versus Structural Rejection

Accept-and-flag applies to validly structured events whose problem is state-based, authority-based, workflow-based, identity-lineage-based, or configured-domain-based.

It does not apply to malformed events, invalid envelope vocabulary, invalid structural type values, impossible schema references, or payloads that fail the accepted schema contract.

During atomization, every conflict rule must state which side it falls on:

- structural rejection before event acceptance
- accepted event plus flag or surfaced issue
- online-only operation rejected because the operation class requires authoritative validation
- deferred/open behavior that cannot be specified yet

This distinction protects P1 and P3 together: offline work remains possible and history remains append-only, but the platform does not become an unvalidated event sink.

## Detect-Before-Act Ordering

Detect-before-act is an ordering discipline for downstream effects. It means an event that may be invalid, stale, unauthorized, or transition-invalid must not trigger irreversible downstream policy, trigger, workflow-state, or resolution effects before the relevant checks have run at the point that owns those effects.

It does not mean:

- every conflict must be known on the device
- every detector runs before event persistence
- one detector owns all facts
- every flag category has the same blocking behavior
- workflow-specific unresolved-flag exclusion automatically becomes general flag semantics

Accepted closed effects:

- downstream policies do not fire before conflict/authorization checks on synced events
- unresolved ADR-005 workflow flags remain visible in timeline
- unresolved ADR-005 workflow flags are excluded from workflow state-machine evaluation until accepted resolution re-derives state
- invalid workflow transitions are accepted and flagged rather than rejected

Open or deferred effects:

- general flag-semantics blocking behavior beyond closed ADR-005 workflow cases
- domain conflict automation outside workflow
- source-chain traversal depth limits
- alias-cycle read-side and resolution behavior
- server-created flag default as a permanent invariant

## Offline-Default Effect

Offline-default changes where certainty can exist.

Devices can create structurally valid events with local knowledge, local configuration, scoped projections, and advisory validation. They cannot be required to know all concurrent events, all assignment changes, all cross-device state, all alias graph updates, or all server-only trigger outcomes.

Therefore:

- local command validation is advisory unless the operation is explicitly online-only
- ordinary field capture must not require a network round trip
- merge/split and conflict resolution remain online-only where the baseline requires authoritative validation
- sync-time/server-side processing must preserve raw references and enough causal knowledge to detect stale or conflicting work later
- late detection must surface anomalies without rewriting historical events
- server-only triggers must treat offline capture as eventually visible, not real-time input

This keeps P1 from weakening P5: offline creation is preserved, but conflicts caused by offline work are surfaced later instead of silently accepted as clean state.

## Flag / Resolution Narrowing

Flag / Resolution owns surfaced anomaly lifecycle after detection. It does not own every detector or every source fact.

Safe carry-forward:

- flag lifecycle is separate from Identity / Lineage, Assignment / Authority / Sync, and Projection / Workflow State
- detectors consume facts from source boundaries
- resolution effects are represented through auditable events/projections
- ADR-005 workflow flag behavior remains closed for workflow cases

Not safe to absorb yet:

- one unified flag catalog as accepted baseline
- `cycle_violation` as accepted baseline flag category
- request-time temporal anchoring as a general detector rule
- server-side flag creation as a permanent invariant
- source-only cascade as general flag semantics outside ADR-005 workflow cases
- auto-resolution for non-workflow or security-relevant conflicts without formal classification

## Iteration 27 Guardrail

The pre-convergence mapping treated these as broad cross-cutting concerns:

- envelope
- accept-and-flag
- detect-before-act
- configuration gradient
- delivery pipeline
- sync contract
- flag catalog
- aggregation interface

The current path keeps the useful insight but rejects the broad conclusion. These are control surfaces that constrain atomized specs. They are not proof that a single `Conflict Detector`, `Flag Catalog`, `Sync Contract`, or `Aggregation Interface` is already closed as an implementation boundary.

For platform-spec atomization, each surface must be rewritten as:

- accepted invariant
- owning boundary
- source facts consumed
- downstream effects blocked or allowed
- open gaps
- forbidden coupling

## Atomization Readiness Checks

Before writing a conflict/flag/offline spec atom, check:

1. Does the claim come from ADR-001 through ADR-005 closure, a classified later-source assessment, or only a principle?
2. Is the anomaly structural invalidity, state anomaly, authorization anomaly, workflow transition anomaly, identity-lineage anomaly, configured-domain anomaly, or reporting visibility issue?
3. Which boundary owns the facts used for detection?
4. Which boundary owns the flag or resolution lifecycle?
5. What downstream action is blocked by detect-before-act?
6. Can the device know enough offline, or is the behavior sync/server-side by construction?
7. Does the rule burden S00 or ordinary offline field capture?
8. Is the behavior already closed, an open-gap closure candidate, or deferred?

## Baseline Impact

No ADR-001 through ADR-005 baseline item should be changed by this overlay.

No patch is currently required to `07-system-boundary-map.md`. The existing map already routes:

- source facts to Identity / Lineage, Assignment / Authority / Sync, Projection / Workflow State, and Configuration
- lifecycle routing to Flag / Resolution
- offline sync scope to Assignment / Authority / Sync
- local retain/remove effects to Local Data Lifecycle
- trigger effects to Trigger / Reactivity

The overlay should be used before atomizing conflict, flag, sync, workflow, trigger, Pattern Registry, or reporting specs.

## Recommended Next Step

Use this overlay to draft the first platform-spec atomization plan around the cross-cutting ingestion and anomaly pipeline:

- structural event acceptance
- offline/local creation assumptions
- sync-time knowledge boundary
- detector source-fact routing
- detect-before-act effect ordering
- flag/resolution lifecycle handoff
- explicit hold-backs for general flag semantics, alias-cycle behavior, and domain conflict automation
