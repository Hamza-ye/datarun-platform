# Platform Spec Dashboard

> [!IMPORTANT]
> **How to sync:** Run `./scripts/sync.sh` (or `python3 scripts/generate-dashboard.py` for local-only) after any change to the registry or gap register.
> Last regenerated: 2026-05-12 16:59:00

## Execution Summary

| Metric | Value |
|---|---|
| Total Atoms | 15 |
| Accepted | 4 |
| Draft | 6 |
| Planned | 5 |
| Professional Baseline Gaps | 22 |
| Atomization Open Decisions | 33 |

## Governance Flow

| Layer | Source | Dashboard Role |
|---|---|---|
| Accepted baseline gap register | [05-decision-gap-register.md](platform-spec-kernels/professional-baseline/05-decision-gap-register.md) | Source-authority gap inventory (22 items). |
| Atomization control register | [90-open-decisions.md](platform-spec-kernels/platform-spec/atoms/90-open-decisions.md) | Spec-facing hold-backs and open decisions (33 items). |
| Draft atoms | `platform-spec-kernels/platform-spec/atoms/` | Carry relevant gaps without closing them. |
| Review and acceptance | Challenge Review, Integration Review, steward recommendation, approval | Promote only after evidence and explicit status update. |

## Active Atomization Risk Surface

Next recommended boundaries: Assignment / Authority / Sync, Configuration.

| Area | Status | Primary Route |
|---|---|---|
| External identity-provider authority | Hold-back | Assignment / Authority / Sync |
| Group-managed authorization | Hold-back | Assignment / Authority / Sync plus Configuration |
| Referent registration, attributes, and catalogs | Hold-back / platform-spec design gap | Event Envelope / Schema for reference contracts; Identity / Lineage for subject-continuity lifecycle; Configuration for shape/catalog definitions; Projection / Workflow State and Assignment / Authority / Sync for process, actor, and assignment lifecycles |
| Final reference serialization and active emission sites | Platform-spec detail gap | Event Envelope / Schema plus Identity / Lineage, Assignment / Authority / Sync, Configuration, Projection / Workflow State, and owning behavior atoms |
| Platform-bundled shape inventory | Platform-spec detail gap | Owning behavior atoms plus Event Envelope / Schema and Configuration |
| Operational actor vocabulary and operation-class routing | Platform-spec detail gap | Cross-boundary definitions plus Assignment / Authority / Sync, Configuration, and Projection / Workflow State |
| Shared-device multi-actor sessions | Architecture decision gap | Assignment / Authority / Sync |
| Auditor access and subject-based scope | Architecture decision gap | Assignment / Authority / Sync |
| Assessment visibility | Platform-spec / operational policy gap | Assignment / Authority / Sync plus Reporting / Aggregation |
| Cross-level distribution visibility | Architecture decision gap / operational policy | Assignment / Authority / Sync |
| Permission table and activity/context authority details | Platform-spec / operational policy gap | Assignment / Authority / Sync plus Configuration |
| Temporary authority, revocation, and offline grace policy | Operational policy gap with architecture trigger | Assignment / Authority / Sync plus Flag / Resolution |
| Onboarding and role-transition details | Operational policy plus implementation/tooling gap | Assignment / Authority / Sync plus Configuration and Local Data Lifecycle |
| Domain conflict automation outside workflow | Architecture decision gap | Configuration plus Flag / Resolution |
| Formal Pattern Registry schema | Platform-spec detail gap | Projection / Workflow State plus Configuration |
| Sync delivery mechanics | Implementation/tooling gap | Assignment / Authority / Sync |
| Event schema/versioning tooling | Implementation/tooling gap | Event Envelope / Schema plus Event Log / Storage and Configuration |
| Configuration versioning and stale-configuration reconciliation | Platform-spec / implementation tooling gap | Configuration plus Event Envelope / Schema and Assignment / Authority / Sync |
| Configuration authoring and deploy-time validation UX | Implementation/tooling gap | Configuration |

## Atom Registry

| ID | Title | Status | Batch | Boundary | Dependencies | Issue |
|---|---|---|---|---|---|---|
| SPEC-001 | [Spec Governance And Source Authority](platform-spec-kernels/platform-spec/atoms/01-spec-governance.md) | draft | 1A | Cross-boundary process | - | [#1](https://github.com/Hamza-ye/datarun-platform/issues/1) |
| SPEC-002 | [Glossary And Core Definitions](platform-spec-kernels/platform-spec/atoms/02-glossary-and-core-definitions.md) | accepted | 1B | Cross-boundary definitions | SPEC-001, SPEC-090, SPEC-091 | [#2](https://github.com/Hamza-ye/datarun-platform/issues/2) |
| SPEC-003 | [Event Log And Storage](platform-spec-kernels/platform-spec/atoms/03-event-log-storage.md) | accepted | 1B | Event Log / Storage | SPEC-001, SPEC-002, SPEC-090, SPEC-091 | [#3](https://github.com/Hamza-ye/datarun-platform/issues/3) |
| SPEC-004 | [Event Envelope And Schema](platform-spec-kernels/platform-spec/atoms/04-event-envelope-schema.md) | accepted | 1B | Event Envelope / Schema | SPEC-001, SPEC-002, SPEC-003, SPEC-090, SPEC-091 | [#4](https://github.com/Hamza-ye/datarun-platform/issues/4) |
| SPEC-005 | [References And Identity Lineage](platform-spec-kernels/platform-spec/atoms/05-references-and-identity-lineage.md) | accepted | 2 | Identity / Lineage | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-090, SPEC-091 | [#5](https://github.com/Hamza-ye/datarun-platform/issues/5) |
| SPEC-006 | [Configuration And Parameterization](platform-spec-kernels/platform-spec/atoms/06-configuration-and-parametrization.md) | draft | 2 | Configuration | SPEC-001, SPEC-002, SPEC-004, SPEC-005, SPEC-090, SPEC-091 | [#6](https://github.com/Hamza-ye/datarun-platform/issues/6) |
| SPEC-007 | [Assignment, Authority, And Sync](platform-spec-kernels/platform-spec/atoms/07-assignment-authority-and-sync.md) | draft | 2 | Assignment / Authority / Sync | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-005, SPEC-006, SPEC-090, SPEC-091 | [#7](https://github.com/Hamza-ye/datarun-platform/issues/7) |
| SPEC-008 | [Local Data Lifecycle](platform-spec-kernels/platform-spec/atoms/08-local-data-lifecycle.md) | planned | 4 | Local Data Lifecycle | SPEC-001, SPEC-003, SPEC-007, SPEC-090, SPEC-091 | [#8](https://github.com/Hamza-ye/datarun-platform/issues/8) |
| SPEC-009 | [Projections, Workflow, And Patterns](platform-spec-kernels/platform-spec/atoms/09-projections-workflow-and-patterns.md) | planned | 3 | Projection / Workflow State | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-005, SPEC-006, SPEC-007, SPEC-090, SPEC-091 | [#9](https://github.com/Hamza-ye/datarun-platform/issues/9) |
| SPEC-010 | [Conflict, Flag, And Resolution](platform-spec-kernels/platform-spec/atoms/10-conflict-flag-and-resolution.md) | planned | 3 | Flag / Resolution | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-005, SPEC-006, SPEC-007, SPEC-009, SPEC-090, SPEC-091 | [#10](https://github.com/Hamza-ye/datarun-platform/issues/10) |
| SPEC-011 | [Trigger And Reactivity](platform-spec-kernels/platform-spec/atoms/11-trigger-reactivity.md) | planned | 3 | Trigger / Reactivity | SPEC-001, SPEC-003, SPEC-006, SPEC-007, SPEC-009, SPEC-010, SPEC-090, SPEC-091 | [#15](https://github.com/Hamza-ye/datarun-platform/issues/15) |
| SPEC-012 | [Reporting, Aggregation, And Freshness](platform-spec-kernels/platform-spec/atoms/12-reporting-aggregation-and-freshness.md) | planned | 4 | Reporting / Aggregation | SPEC-001, SPEC-003, SPEC-005, SPEC-007, SPEC-009, SPEC-010, SPEC-090, SPEC-091 | [#11](https://github.com/Hamza-ye/datarun-platform/issues/11) |
| SPEC-090 | [Open Decisions And Hold-backs](platform-spec-kernels/platform-spec/atoms/90-open-decisions.md) | draft | 1A | Cross-boundary control register | SPEC-001 | [#12](https://github.com/Hamza-ye/datarun-platform/issues/12) |
| SPEC-091 | [Rejected Paths](platform-spec-kernels/platform-spec/atoms/91-rejected-paths.md) | draft | 1A | Cross-boundary control register | SPEC-001 | [#13](https://github.com/Hamza-ye/datarun-platform/issues/13) |
| SPEC-092 | [Change-Control Log](platform-spec-kernels/platform-spec/atoms/92-change-control-log.md) | draft | 1A | Cross-boundary process register | SPEC-001, SPEC-090, SPEC-091 | [#14](https://github.com/Hamza-ye/datarun-platform/issues/14) |

## Open Decisions / Gaps

<details>
<summary>Professional baseline gaps</summary>

| Gap | Classification |
|---|---|
| Domain Conflict Automation Outside Workflow | Architecture decision gap |
| Subject-Based Scope And Auditor Access | Architecture decision gap |
| Shared Device Actor Scope | Architecture decision gap |
| Temporary Authority And Offline Revocation Reconciliation | Operational policy gap with architecture decision trigger |
| Alias-Cycle Enforcement And Resolution Semantics | Architecture decision gap |
| Operational Actor Vocabulary And Operation-Class Routing | Platform-spec detail gap |
| Envelope Type, Shape Ref, And Parametrization Boundary | Platform-spec detail gap with control overlay now available |
| Exact Pattern Registry Inventory | Platform-spec detail gap |
| Formal Pattern Schema Format | Platform-spec detail gap |
| Source-Chain Traversal Limits | Platform-spec detail gap |
| Bounded Context Expression Details | Platform-spec detail gap |
| Projection Performance And Caching | Implementation/tooling gap |
| Event Schema And Versioning Tooling | Implementation/tooling gap |
| Structured Import Export Compatibility | Implementation/tooling gap with platform-spec compatibility constraint |
| Configuration Authoring And Deployment UX | Implementation/tooling gap |
| Auto-Resolution Authoring And Monitoring | Implementation/tooling gap |
| Sync Delivery Mechanics | Implementation/tooling gap |
| Retention And Archival | Operational policy gap |
| Setup Experience And Onboarding | Operational policy gap |
| Reporting And Aggregation | Operational policy gap |
| ADR-006-R Through ADR-009 Assessment | Completed later-source assessment |
| General Flag Semantics | Later-source assessment gap |

</details>

<details>
<summary>Atomization open decisions and hold-backs</summary>

| Area | Status | Primary Route |
|---|---|---|
| Cloud multi-tenancy and shared-runtime hosting | Hold-back | Deployment / Tenancy routing surface |
| Deployment identity in event envelopes | Hold-back | Event Envelope / Schema plus Deployment / Tenancy |
| External identity-provider authority | Hold-back | Assignment / Authority / Sync |
| Group-managed authorization | Hold-back | Assignment / Authority / Sync plus Configuration |
| Referent registration, attributes, and catalogs | Hold-back / platform-spec design gap | Event Envelope / Schema for reference contracts; Identity / Lineage for subject-continuity lifecycle; Configuration for shape/catalog definitions; Projection / Workflow State and Assignment / Authority / Sync for process, actor, and assignment lifecycles |
| Final reference serialization and active emission sites | Platform-spec detail gap | Event Envelope / Schema plus Identity / Lineage, Assignment / Authority / Sync, Configuration, Projection / Workflow State, and owning behavior atoms |
| Platform-bundled shape inventory | Platform-spec detail gap | Owning behavior atoms plus Event Envelope / Schema and Configuration |
| Operational actor vocabulary and operation-class routing | Platform-spec detail gap | Cross-boundary definitions plus Assignment / Authority / Sync, Configuration, and Projection / Workflow State |
| Shared-device multi-actor sessions | Architecture decision gap | Assignment / Authority / Sync |
| Auditor access and subject-based scope | Architecture decision gap | Assignment / Authority / Sync |
| Assessment visibility | Platform-spec / operational policy gap | Assignment / Authority / Sync plus Reporting / Aggregation |
| Cross-level distribution visibility | Architecture decision gap / operational policy | Assignment / Authority / Sync |
| Permission table and activity/context authority details | Platform-spec / operational policy gap | Assignment / Authority / Sync plus Configuration |
| Temporary authority, revocation, and offline grace policy | Operational policy gap with architecture trigger | Assignment / Authority / Sync plus Flag / Resolution |
| Onboarding and role-transition details | Operational policy plus implementation/tooling gap | Assignment / Authority / Sync plus Configuration and Local Data Lifecycle |
| General flag semantics beyond accepted workflow cases | Architecture decision gap | Flag / Resolution |
| Flag event identity, creation location, and resolution-event mapping | Architecture / platform-spec decision gap | Flag / Resolution plus Event Envelope / Schema |
| Alias-cycle read-side behavior and resolution semantics | Architecture decision gap | Identity / Lineage plus Flag / Resolution |
| Domain conflict automation outside workflow | Architecture decision gap | Configuration plus Flag / Resolution |
| Exact Pattern Registry inventory | Platform-spec detail gap | Projection / Workflow State |
| Formal Pattern Registry schema | Platform-spec detail gap | Projection / Workflow State plus Configuration |
| Source-chain traversal depth limits | Platform-spec detail gap | Flag / Resolution plus Projection / Workflow State |
| Sync delivery mechanics | Implementation/tooling gap | Assignment / Authority / Sync |
| Local purge/lifecycle rules for sensitive data | Platform-spec / operational policy gap | Local Data Lifecycle |
| Reporting freshness semantics | Platform-spec detail gap | Reporting / Aggregation |
| Retention and archival | Platform-spec / operational policy gap | Event Log / Storage plus Local Data Lifecycle |
| Structured import/export contracts | Platform-spec detail gap | Event Envelope / Schema plus Reporting / Aggregation |
| Projection optimization and caching | Implementation/tooling gap | Event Log / Storage plus Projection / Workflow State |
| Event schema/versioning tooling | Implementation/tooling gap | Event Envelope / Schema plus Event Log / Storage and Configuration |
| Projection compatibility across schema versions | Platform-spec / implementation tooling gap | Projection / Workflow State plus Event Envelope / Schema |
| Configuration versioning and stale-configuration reconciliation | Platform-spec / implementation tooling gap | Configuration plus Event Envelope / Schema and Assignment / Authority / Sync |
| Configuration authoring and deploy-time validation UX | Implementation/tooling gap | Configuration |
| Auto-resolution authoring and monitoring | Implementation/tooling gap | Flag / Resolution plus Reporting / Aggregation |

</details>
