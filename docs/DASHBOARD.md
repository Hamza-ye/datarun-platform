# Platform Spec Dashboard

> [!IMPORTANT]
> **How to sync:** Run `./scripts/sync.sh` (or `python3 scripts/generate-dashboard.py` for local-only) after any change to the registry or gap register.
> Last regenerated: 2026-05-19 02:17:26

## Execution Summary

| Metric | Value |
|---|---|
| Total Sections | 15 |
| Accepted | 0 |
| Draft | 9 |
| Planned | 5 |
| Professional Baseline Gaps | 27 |
| Platform-Spec Open Decisions | 13 |

## Governance Flow

| Layer | Source | Dashboard Role |
|---|---|---|
| Accepted baseline gap register | [05-decision-gap-register.md](platform-spec-kernels/professional-baseline/05-decision-gap-register.md) | Source-authority gap inventory (27 items). |
| Open-decision citations | [90-open-decisions-and-gap-register-citations.md](platform-spec-kernels/platform-spec/sections/90-open-decisions-and-gap-register-citations.md) | Spec-facing hold-backs and open decisions (13 items). |
| Draft sections | `platform-spec-kernels/platform-spec/sections/` | Carry relevant gaps without closing them. |
| Review and acceptance | Challenge Review, Integration Review, steward recommendation, approval | Promote only after evidence and explicit status update. |

## Section Registry

| ID | Title | Status | Batch | Boundary | Dependencies | Issue |
|---|---|---|---|---|---|---|
| SPEC-001 | [Specification Source Authority](platform-spec-kernels/platform-spec/sections/00-specification-source-authority.md) | draft | 1A | professional-baseline source authority | - | [#1](https://github.com/Hamza-ye/datarun-platform/issues/1) |
| SPEC-002 | [Core Definitions And Boundary Vocabulary](platform-spec-kernels/platform-spec/sections/01-core-definitions-and-boundary-vocabulary.md) | draft | 1B | professional-baseline source authority | SPEC-001, SPEC-090, SPEC-091 | [#2](https://github.com/Hamza-ye/datarun-platform/issues/2) |
| SPEC-003 | [Event Log And Storage Model](platform-spec-kernels/platform-spec/sections/02-event-log-and-storage-model.md) | draft | 1B | Event Log / Storage | SPEC-001, SPEC-002, SPEC-090, SPEC-091 | [#3](https://github.com/Hamza-ye/datarun-platform/issues/3) |
| SPEC-004 | [Event Envelope, Schema, And References](platform-spec-kernels/platform-spec/sections/03-event-envelope-schema-and-references.md) | draft | 1B | Event Envelope / Schema | SPEC-001, SPEC-002, SPEC-003, SPEC-090, SPEC-091 | [#4](https://github.com/Hamza-ye/datarun-platform/issues/4) |
| SPEC-005 | [References And Identity Lineage](platform-spec-kernels/platform-spec/sections/05-references-and-identity-lineage.md) | candidate-input | 2 | Identity / Lineage | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-090, SPEC-091 | [#5](https://github.com/Hamza-ye/datarun-platform/issues/5) |
| SPEC-006 | [Configuration And Parameterization](platform-spec-kernels/platform-spec/sections/06-configuration-and-parametrization.md) | draft | 2 | Configuration | SPEC-001, SPEC-002, SPEC-004, SPEC-005, SPEC-090, SPEC-091 | [#6](https://github.com/Hamza-ye/datarun-platform/issues/6) |
| SPEC-007 | [Assignment, Authority, And Sync](platform-spec-kernels/platform-spec/sections/07-assignment-authority-and-sync.md) | draft | 2 | Assignment / Authority / Sync | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-005, SPEC-006, SPEC-090, SPEC-091 | [#7](https://github.com/Hamza-ye/datarun-platform/issues/7) |
| SPEC-008 | [Local Data Lifecycle](platform-spec-kernels/platform-spec/sections/08-local-data-lifecycle.md) | planned | 4 | Local Data Lifecycle | SPEC-001, SPEC-003, SPEC-007, SPEC-090, SPEC-091 | [#8](https://github.com/Hamza-ye/datarun-platform/issues/8) |
| SPEC-009 | [Projections, Workflow, And Patterns](platform-spec-kernels/platform-spec/sections/09-projections-workflow-and-patterns.md) | planned | 3 | Projection / Workflow State | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-005, SPEC-006, SPEC-007, SPEC-090, SPEC-091 | [#9](https://github.com/Hamza-ye/datarun-platform/issues/9) |
| SPEC-010 | [Conflict, Flag, And Resolution](platform-spec-kernels/platform-spec/sections/10-conflict-flag-and-resolution.md) | planned | 3 | Flag / Resolution | SPEC-001, SPEC-002, SPEC-003, SPEC-004, SPEC-005, SPEC-006, SPEC-007, SPEC-009, SPEC-090, SPEC-091 | [#10](https://github.com/Hamza-ye/datarun-platform/issues/10) |
| SPEC-011 | [Trigger And Reactivity](platform-spec-kernels/platform-spec/sections/11-trigger-reactivity.md) | planned | 3 | Trigger / Reactivity | SPEC-001, SPEC-003, SPEC-006, SPEC-007, SPEC-009, SPEC-010, SPEC-090, SPEC-091 | [#15](https://github.com/Hamza-ye/datarun-platform/issues/15) |
| SPEC-012 | [Reporting, Aggregation, And Freshness](platform-spec-kernels/platform-spec/sections/12-reporting-aggregation-and-freshness.md) | planned | 4 | Reporting / Aggregation | SPEC-001, SPEC-003, SPEC-005, SPEC-007, SPEC-009, SPEC-010, SPEC-090, SPEC-091 | [#11](https://github.com/Hamza-ye/datarun-platform/issues/11) |
| SPEC-090 | [Open Decisions And Gap Register Citations](platform-spec-kernels/platform-spec/sections/90-open-decisions-and-gap-register-citations.md) | draft | 1A | professional-baseline source authority | SPEC-001 | [#12](https://github.com/Hamza-ye/datarun-platform/issues/12) |
| SPEC-091 | [Rejected Alternatives](platform-spec-kernels/platform-spec/sections/91-rejected-alternatives.md) | draft | 1A | professional-baseline source authority | SPEC-001 | [#13](https://github.com/Hamza-ye/datarun-platform/issues/13) |
| SPEC-092 | [Change-Control Log](platform-spec-kernels/platform-spec/sections/92-change-control-log.md) | draft | 1A | Cross-boundary process register | SPEC-001, SPEC-090, SPEC-091 | [#14](https://github.com/Hamza-ye/datarun-platform/issues/14) |

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
| Authorization Visibility And Role-Action Detail Surfaces | Platform-spec detail gap with architecture decision triggers |
| Envelope Type, Shape Ref, And Parametrization Boundary | Platform-spec detail gap with assessed source material available |
| Process Reference And Process Lifecycle Semantics | Platform-spec detail gap with architecture decision trigger |
| Exact Pattern Registry Inventory | Platform-spec detail gap |
| Formal Pattern Schema Format | Platform-spec detail gap |
| Source-Chain Traversal Limits | Platform-spec detail gap |
| Bounded Context Expression Details | Platform-spec detail gap |
| Domain-Agnostic Proof Gap | Product validation gap |
| Projection Performance And Caching | Implementation/tooling gap |
| Low-End Device Scale And Offline Performance | Implementation/tooling gap with architecture decision trigger |
| Event Schema And Versioning Tooling | Implementation/tooling gap |
| Structured Import Export Compatibility | Implementation/tooling gap with platform-spec compatibility constraint |
| Configuration Authoring And Deployment UX | Implementation/tooling gap |
| Auto-Resolution Authoring And Monitoring | Implementation/tooling gap |
| Sync Delivery Mechanics | Implementation/tooling gap |
| Retention And Archival | Operational policy gap |
| Sensitive Data Policy And Local Lifecycle | Operational policy gap with architecture decision trigger |
| Setup Experience And Onboarding | Operational policy gap |
| Reporting And Aggregation | Operational policy gap |
| ADR-006-R Through ADR-009 Assessment | Completed later-source assessment |
| General Flag Semantics | Later-source assessment gap |

</details>

<details>
<summary>Platform-spec open decisions and hold-backs</summary>

| Area | Status | Primary Route |
|---|---|---|
| Envelope type, shape ref, references, and parametrization boundary | Constraint. Preserve axis separation and do not add fields, type values, actor subclasses, or product classes. | `01`, `03`, `91` |
| Event schema and versioning tooling | Blocker for implementation-ready append; constraint for conceptual envelope drafting. | `03` |
| Final reference serialization and active emission sites | Blocker for implementation-ready append if canonical reference names, placement, cardinality, or emission sites are needed. | `03` |
| Process reference and process lifecycle semantics | Explicit deferral unless process lifecycle or active process-reference emission is included. | `01`, `03` |
| Structured import/export compatibility | Explicit deferral unless external exchange is included. | `02`, `03`, `91` |
| Projection performance and caching | Constraint. Do not specify cache/rebuild strategy as architecture. | `02` |
| Low-end device scale and offline performance | Constraint. Do not weaken event-log truth for performance pressure. | `02` |
| Retention and archival | Constraint; blocker only if deletion, redaction, archive policy, or canonical-history mutation is specified. | `02`, `91` |
| Operational actor vocabulary and operation-class routing | Constraint. Role labels remain product/deployer vocabulary unless formally changed. | `01`, `91` |
| Exact Pattern Registry inventory and formal schema | Explicit deferral. `01` may define `pattern` only as a term. | `01` |
| General flag semantics and domain conflict automation outside workflow | Hold back for later flag/conflict sections. | `91` |
| Alias-cycle enforcement and resolution semantics | Hold back for later identity/flag sections. | `91` |
| Subject-based scope, auditor access, shared-device actor scope, temporary authority, and authorization visibility details | Hold back unless actor/session/reference behavior is over-defined. | `03`, `91` |

</details>
