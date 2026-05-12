# Platform Spec Dashboard

> [!IMPORTANT]
> **How to sync:** Run `./scripts/sync.sh` (or `python3 scripts/generate-dashboard.py` for local-only) after any change to the registry or gap register.
> Last regenerated: 2026-05-12 03:10:38

## Execution Summary

| Metric | Value |
|---|---|
| Total Atoms | 15 |
| Accepted | 3 |
| Draft | 5 |
| Planned | 7 |
| Open Gaps | 22 |

## Atom Registry

| ID | Title | Status | Batch | Boundary | Dependencies | Issue |
|---|---|---|---|---|---|---|
| SPEC-001 | [Spec Governance And Source Authority](platform-spec-kernels/platform-spec/atoms/01-spec-governance.md) | draft | 1A | Cross-boundary process | - | [#1](https://github.com/Hamza-ye/datarun-platform/issues/1) |
| SPEC-002 | [Glossary And Core Definitions](platform-spec-kernels/platform-spec/atoms/02-glossary-and-core-definitions.md) | accepted | 1B | Cross-boundary definitions | SPEC-001, SPEC-090, SPEC-091 | [#2](https://github.com/Hamza-ye/datarun-platform/issues/2) |
| SPEC-003 | [Event Log And Storage](platform-spec-kernels/platform-spec/atoms/03-event-log-storage.md) | accepted | 1B | Event Log / Storage | SPEC-001, SPEC-002, SPEC-090, SPEC-091 | [#3](https://github.com/Hamza-ye/datarun-platform/issues/3) |
| SPEC-004 | [Event Envelope And Schema](platform-spec-kernels/platform-spec/atoms/04-event-envelope-schema.md) | accepted | 1B | Event Envelope / Schema | SPEC-001, SPEC-002, SPEC-003, SPEC-090, SPEC-091 | [#4](https://github.com/Hamza-ye/datarun-platform/issues/4) |
| SPEC-005 | [References And Identity Lineage](platform-spec-kernels/platform-spec/atoms/05-references-and-identity-lineage.md) | draft | 2 | Identity / Lineage | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-090, SPEC-091 | [#5](https://github.com/Hamza-ye/datarun-platform/issues/5) |
| SPEC-006 | [Configuration And Parameterization](platform-spec-kernels/platform-spec/atoms/06-configuration-and-parametrization.md) | planned | 2 | Configuration | SPEC-001, SPEC-002, SPEC-004, SPEC-090, SPEC-091 | [#6](https://github.com/Hamza-ye/datarun-platform/issues/6) |
| SPEC-007 | [Assignment, Authority, And Sync](platform-spec-kernels/platform-spec/atoms/07-assignment-authority-and-sync.md) | planned | 2 | Assignment / Authority / Sync | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-005, SPEC-006, SPEC-090, SPEC-091 | [#7](https://github.com/Hamza-ye/datarun-platform/issues/7) |
| SPEC-008 | [Local Data Lifecycle](platform-spec-kernels/platform-spec/atoms/08-local-data-lifecycle.md) | planned | 4 | Local Data Lifecycle | SPEC-001, SPEC-003, SPEC-007, SPEC-090, SPEC-091 | [#8](https://github.com/Hamza-ye/datarun-platform/issues/8) |
| SPEC-009 | [Projections, Workflow, And Patterns](platform-spec-kernels/platform-spec/atoms/09-projections-workflow-and-patterns.md) | planned | 3 | Projection / Workflow State | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-005, SPEC-006, SPEC-007, SPEC-090, SPEC-091 | [#9](https://github.com/Hamza-ye/datarun-platform/issues/9) |
| SPEC-010 | [Conflict, Flag, And Resolution](platform-spec-kernels/platform-spec/atoms/10-conflict-flag-and-resolution.md) | planned | 3 | Flag / Resolution | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-005, SPEC-006, SPEC-007, SPEC-009, SPEC-090, SPEC-091 | [#10](https://github.com/Hamza-ye/datarun-platform/issues/10) |
| SPEC-011 | [Trigger And Reactivity](platform-spec-kernels/platform-spec/atoms/11-trigger-reactivity.md) | planned | 3 | Trigger / Reactivity | SPEC-001, SPEC-003, SPEC-006, SPEC-007, SPEC-009, SPEC-010, SPEC-090, SPEC-091 | [#15](https://github.com/Hamza-ye/datarun-platform/issues/15) |
| SPEC-012 | [Reporting, Aggregation, And Freshness](platform-spec-kernels/platform-spec/atoms/12-reporting-aggregation-and-freshness.md) | planned | 4 | Reporting / Aggregation | SPEC-001, SPEC-003, SPEC-005, SPEC-007, SPEC-009, SPEC-010, SPEC-090, SPEC-091 | [#11](https://github.com/Hamza-ye/datarun-platform/issues/11) |
| SPEC-090 | [Open Decisions And Hold-backs](platform-spec-kernels/platform-spec/atoms/90-open-decisions.md) | draft | 1A | Cross-boundary control register | SPEC-001 | [#12](https://github.com/Hamza-ye/datarun-platform/issues/12) |
| SPEC-091 | [Rejected Paths](platform-spec-kernels/platform-spec/atoms/91-rejected-paths.md) | draft | 1A | Cross-boundary control register | SPEC-001 | [#13](https://github.com/Hamza-ye/datarun-platform/issues/13) |
| SPEC-092 | [Change-Control Log](platform-spec-kernels/platform-spec/atoms/92-change-control-log.md) | draft | 1A | Cross-boundary process register | SPEC-001, SPEC-090, SPEC-091 | [#14](https://github.com/Hamza-ye/datarun-platform/issues/14) |

## Open Decisions / Gaps

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
