# Operational Constraints Boundary Control

Status: Draft dependency-aware control overlay

This document preserves `../../constraints.md` as operational envelope authority without letting it become architecture or implementation authority. The constraints shaped the ADR path, but they do not decide mechanisms by themselves.

The core rule is: constraints define what the platform must work within; ADR-001 through ADR-005 define the accepted mechanisms currently used to satisfy those constraints; unresolved constraint pressure must be routed as explicit gaps rather than silently widening existing boundaries.

## Source Basis

Primary inputs:

- `../00-extraction-state.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `02-change-control.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`
- `08-baseline-acceptance-check.md`
- `15-conflict-flag-offline-boundary-control.md`
- `../../constraints.md`
- `../02-domain-requirement-kernels.md`

Later-source assessments used only as classified assessment material:

- `10-adr006r-flag-semantics-assessment.md`
- `11-adr007-envelope-type-assessment.md`
- `12-adr008-reference-fields-assessment.md`
- `13-adr009-duality-rule-assessment.md`

## Authority Rule

`../../constraints.md` is authoritative for operational conditions:

- user tiers differ materially
- field work is frequently offline
- deployments can be large and multi-activity
- data may be sensitive and jurisdictionally constrained
- structured interoperability must remain possible
- responsiveness expectations differ by work type

It is not authority for:

- storage architecture
- sync protocol mechanics
- access-control implementation
- reporting product model
- external integration protocols
- regulatory framework implementation
- latency service levels
- platform module boundaries

If a constraint creates pressure not closed by ADR-001 through ADR-005, route it to the gap register or atomization hold-back list.

## Constraint-To-Boundary Mapping

| Constraint Pressure | Accepted Baseline Mechanism | Primary Boundary | Open / Deferred Routing |
|---|---|---|---|
| field users are often offline | client-generated IDs, local event creation, immutable event sync | Event Log / Storage; Event Envelope / Schema; Assignment / Authority / Sync | sync delivery mechanics, local lifecycle, offline conflict reconciliation |
| supervisors/coordinators see delayed field state | projection-derived views and sync-visible state | Reporting / Aggregation | freshness metadata and reporting consistency surface |
| coordinators/admins have more reliable infrastructure | online-only coordinator operations allowed where baseline requires validation | Assignment / Authority / Sync; Configuration | setup/onboarding, permission details, configuration deployment UX |
| auditors may cut across hierarchy | assignment/scope mechanisms plus explicit auditor-access gap | Assignment / Authority / Sync | subject-based scope and auditor access |
| low-end Android and intermittent bandwidth | selective sync, scoped projections, deferred sync mechanics | Local Data Lifecycle; Assignment / Authority / Sync | low-end device scale risk, sync pagination/priority/bandwidth |
| many users and millions of records | immutable event log and projection rebuild discipline | Event Log / Storage | projection performance/caching and retention/archive policy |
| multiple concurrent activities | deployer-configured activities, `activity_ref`, bounded configuration | Configuration; Event Envelope / Schema | activity configuration details, Pattern Registry inventory/schema |
| personal/sensitive information | assignment-derived access, shape/activity sensitivity, local lifecycle caution | Assignment / Authority / Sync; Configuration; Local Data Lifecycle | sensitive-subject policy, retention, concrete purge/lifecycle rules |
| accountability and audit | append-only events, authority-as-projection, resolution events | Event Log / Storage; Assignment / Authority / Sync; Flag / Resolution | audit-export surfaces and reporting requirements |
| jurisdictional variation | bounded deployer policy values over platform mechanisms | Configuration | compliance policy configuration; formal decision if new access/lifecycle semantics are needed |
| structured exchange with external systems | stable event/envelope/schema direction and immutable record model | Event Envelope / Schema | structured import/export compatibility gap |
| immediate capture | local capture and event creation without network roundtrip | Event Log / Storage; Event Envelope / Schema | UI/performance implementation; no central visibility guarantee |
| opportunistic sync | immutable, idempotent, append-only, scope-filtered sync | Assignment / Authority / Sync | sync delivery mechanics |
| configuration changes on next sync | atomic config packages, immutable event validity, version coexistence | Configuration; Event Envelope / Schema | config versioning, stale-config reconciliation, authoring/deployment UX |

## Constraint Vocabulary

Use these terms narrowly during atomization:

| Term | Control Definition | Not Allowed To Mean |
|---|---|---|
| primary field operation | field-level capture, lookup, and decision work needed while disconnected | all coordinator/admin, merge/split, setup, reporting, or resolution operations |
| offline constraint | field work must not require a network roundtrip | every platform operation is offline-capable |
| oversight freshness | centrally visible state may lag field reality and must disclose age where it matters | real-time field feed or hidden sync delay |
| low-end device envelope | design and implementation must respect limited storage, compute, bandwidth, and literacy | weakening canonical event-log or projection-derived truth |
| compliance support | platform mechanisms that let deployers satisfy jurisdiction-specific obligations | built-in implementation of named legal regimes |
| interoperability compatibility | internal record structure must not block future structured import/export | Phase 1 real-time integration requirement |
| immediate capture | capture interaction should not wait for network or central validation | immediate central visibility or global conflict certainty |
| config propagation | changed configuration reaches devices on next sync and old in-progress work can complete under old rules | instant config invalidation of offline work |

## Overread Controls

### Offline Field Work

Constraint reading:

- Field workers must be able to capture, look up, and decide while disconnected.
- Sync makes field work centrally visible later.

Accepted baseline reading:

- Events, subjects, and records use client-generated IDs.
- Event sync is immutable, idempotent, append-only, order-independent, and scope-filtered.
- Device validation is advisory unless a specific operation is online-only.

Forbidden overread:

- Do not require complete global knowledge for ordinary capture.
- Do not infer that merge/split, conflict resolution, setup, or reporting must be offline-capable.
- Do not treat delayed central visibility as a platform defect.

### Scale And Low-End Devices

Constraint reading:

- Large deployments and low-end devices are normal conditions.
- Operational data accumulates and is rarely deleted.

Accepted baseline reading:

- The event log remains canonical.
- Projections and materialized views are derived.
- Local lifecycle and sync scope constrain what devices carry.

Forbidden overread:

- Do not replace event-log source of truth with snapshot-primary storage for performance.
- Do not hide projection or sync performance risk by moving canonical truth into local caches.
- Do not treat retain-and-hide as sufficient sensitive-data handling.

### Data Sensitivity And Compliance

Constraint reading:

- Data may include personal or sensitive information.
- Compliance requirements vary by jurisdiction.
- The platform should provide mechanisms, not per-deployment custom development.

Accepted baseline reading:

- Access is assignment-derived.
- Sync scope is access scope.
- Authority is projection-derived.
- Shape/activity sensitivity is configurable policy.
- Sensitive deployments require stronger local lifecycle handling than retain-and-hide.

Forbidden overread:

- Do not hard-code one regulatory framework into the core platform.
- Do not infer field-level sensitivity, arbitrary access-control logic, or stored immutable `authority_context`.
- Do not let compliance reporting bypass assignment/sync-scope constraints.

### Interoperability

Constraint reading:

- Structured record exchange with external systems must remain possible.
- Real-time integration is not required by this source.

Accepted baseline reading:

- The event envelope and shape/activity references provide stable internal structure.
- Events are immutable facts; projections, views, summaries, and exports remain derived artifacts.

Forbidden overread:

- Do not make the canonical event model depend on an external system's schema.
- Do not infer a Phase 1 API, real-time integration, or named data-standard requirement.
- Do not mutate historical events to satisfy export convenience.

### Responsiveness

Constraint reading:

- Capture must feel immediate.
- Sync is opportunistic.
- Oversight and reporting are eventually consistent.
- Configuration changes propagate on next sync.

Accepted baseline reading:

- Local capture is supported by offline event creation.
- Trigger and reporting visibility are sync-visible, not real-time.
- Atomic configuration packages and versioned shapes protect offline coexistence.

Forbidden overread:

- Do not present oversight/reporting as live unless later specification explicitly closes that surface.
- Do not force in-progress offline work under old configuration to retroactively follow new rules.
- Do not use `device_time` as protocol ordering to simulate live behavior.

## Atomization Readiness Checks

Before writing a spec atom affected by operational constraints, check:

1. Which constraint pressure is being preserved?
2. Is it closed by ADR-001 through ADR-005, or is it only an operational requirement?
3. Which boundary owns the accepted mechanism?
4. Which gap owns the unresolved part?
5. Does the claim overread "field operations" into all platform operations?
6. Does it turn compliance support into regulatory logic?
7. Does it turn interoperability compatibility into a real-time integration requirement?
8. Does it hide freshness, sync delay, or configuration-version coexistence?
9. Does it preserve S00 simplicity and ordinary offline capture?

## Baseline Impact

This overlay does not change ADR-001 through ADR-005 baseline behavior.

It does require one visibility update to the professional baseline: structured import/export compatibility should be explicit in the gap register and boundary routing. The constraint already exists in extracted domain kernels, but without a professional-baseline route it can be forgotten during atomization.

No new boundary is required. Structured exchange should route primarily through Event Envelope / Schema while consuming Event Log / Storage, Configuration, and Reporting / Aggregation outputs as needed.

## Recommended Next Step

Use this overlay alongside `15-conflict-flag-offline-boundary-control.md` to draft the first atomization plan. The plan should explicitly hold back:

- structured import/export contracts
- reporting freshness metadata
- concrete compliance policy configuration
- low-end device performance budgets
- sync delivery mechanics
- configuration versioning and stale-config reconciliation
