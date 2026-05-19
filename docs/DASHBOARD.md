# Platform Specification Snapshot

> [!NOTE]
> This dashboard owns no architecture, gap, acceptance, implementation, or remote-tracking state. It is a generated local snapshot from accepted baseline inputs plus the non-authoritative section manifest.
> Regenerate with `python3 scripts/generate-dashboard.py` after changing the baseline, gap register, outline, first-section citations, or manifest.
> Last regenerated: 2026-05-19 03:00:57

## Source Inputs

| Input | Standing In This Snapshot |
|---|---|
| [04 Architecture Baseline v0](platform-spec-kernels/professional-baseline/04-architecture-baseline-v0.md) | Accepted architecture baseline. |
| [05 Decision Gap Register](platform-spec-kernels/professional-baseline/05-decision-gap-register.md) | Canonical open-gap and open-decision register. |
| [07 Architecture Responsibility Map](platform-spec-kernels/professional-baseline/07-system-boundary-map.md) | Responsibility routing only. |
| [20 Platform-Spec Outline](platform-spec-kernels/professional-baseline/20-platform-spec-outline.md) | Draft outline from accepted baseline; source for section structure and blocker assessment. |
| [section-registry.yml](platform-spec-kernels/platform-spec/section-registry.yml) | Non-authoritative local manifest for paths, status labels, and candidate inputs. |
| [90 Gap Citations](platform-spec-kernels/platform-spec/sections/90-open-decisions-and-gap-register-citations.md) | Draft citation surface for first-section blockers and hold-backs; does not replace 05. |

## Snapshot Counts

| Metric | Value |
|---|---:|
| Outline sections | 16 |
| Manifest sections | 16 |
| Manifest status: planned | 8 |
| Manifest status: candidate-input | 1 |
| Manifest status: draft | 7 |
| Manifest status: accepted | 0 |
| Manifest status: deferred | 0 |
| Manifest status: hold-back | 0 |
| Manifest status: rejected | 0 |
| Target section files present | 8 of 16 |
| Candidate input files present | 2 of 2 |
| Canonical gap entries visible in 05 | 27 |
| First-section gap citations visible in 90 | 13 |

## Manifest Alignment

The manifest section numbers and titles match the current outline headings in `20-platform-spec-outline.md`.

## Section Manifest

| Section | Title | Status | Owner | Target File | Candidate Input | First Slice |
|---|---|---|---|---|---|---|
| 00 | Specification Source Authority | draft | professional-baseline source authority | [sections/00-specification-source-authority.md](platform-spec-kernels/platform-spec/sections/00-specification-source-authority.md) | - | yes |
| 01 | Core Definitions And Boundary Vocabulary | draft | professional-baseline source authority | [sections/01-core-definitions-and-boundary-vocabulary.md](platform-spec-kernels/platform-spec/sections/01-core-definitions-and-boundary-vocabulary.md) | - | yes |
| 02 | Event Log And Storage Model | draft | Event Log / Storage | [sections/02-event-log-and-storage-model.md](platform-spec-kernels/platform-spec/sections/02-event-log-and-storage-model.md) | - | yes |
| 03 | Event Envelope, Schema, And References | draft | Event Envelope / Schema | [sections/03-event-envelope-schema-and-references.md](platform-spec-kernels/platform-spec/sections/03-event-envelope-schema-and-references.md) | - | yes |
| 04 | Identity And Lineage | planned | Identity / Lineage | `sections/04-identity-and-lineage.md` (not created) | [sections/05-references-and-identity-lineage.md](platform-spec-kernels/platform-spec/sections/05-references-and-identity-lineage.md) | no |
| 05 | Assignment, Authority, And Sync | planned | Assignment / Authority / Sync | `sections/05-assignment-authority-and-sync.md` (not created) | [sections/07-assignment-authority-and-sync.md](platform-spec-kernels/platform-spec/sections/07-assignment-authority-and-sync.md) | no |
| 06 | Configuration And Parameterization | candidate-input | Configuration | [sections/06-configuration-and-parametrization.md](platform-spec-kernels/platform-spec/sections/06-configuration-and-parametrization.md) | - | no |
| 07 | Projection, Workflow, And Pattern Registry | planned | Projection / Workflow State | `sections/07-projection-workflow-and-pattern-registry.md` (not created) | - | no |
| 08 | Flags, Conflict Surfacing, And Resolution | planned | Flag / Resolution | `sections/08-flags-conflict-surfacing-and-resolution.md` (not created) | - | no |
| 09 | Local Data Lifecycle And Operational Constraints | planned | Local Data Lifecycle | `sections/09-local-data-lifecycle-and-operational-constraints.md` (not created) | - | no |
| 10 | Reporting, Aggregation, And Freshness | planned | Projection / Workflow State | `sections/10-reporting-aggregation-and-freshness.md` (not created) | - | no |
| 11 | Trigger And Reactivity | planned | Configuration | `sections/11-trigger-and-reactivity.md` (not created) | - | no |
| 12 | Import, Export, And External Compatibility | planned | Event Envelope / Schema | `sections/12-import-export-and-external-compatibility.md` (not created) | - | no |
| 90 | Open Decisions And Gap Register Citations | draft | professional-baseline source authority | [sections/90-open-decisions-and-gap-register-citations.md](platform-spec-kernels/platform-spec/sections/90-open-decisions-and-gap-register-citations.md) | - | yes |
| 91 | Rejected Alternatives | draft | professional-baseline source authority | [sections/91-rejected-alternatives.md](platform-spec-kernels/platform-spec/sections/91-rejected-alternatives.md) | - | yes |
| 92 | Source Basis And Change-Control Log | draft | professional-baseline source authority | [sections/92-change-control-log.md](platform-spec-kernels/platform-spec/sections/92-change-control-log.md) | - | no |

## First-Slice Focus

This is a snapshot of what the outline says can be drafted first. It is not acceptance or implementation approval.

| Section | Status | Snapshot Note |
|---|---|---|
| 00 | draft | Drafted, not accepted; no behavior authority until reviewed and accepted. |
| 01 | draft | Drafted for vocabulary only; must not close pattern inventory, process lifecycle, role taxonomy, or reference-serialization gaps. |
| 02 | draft | Drafted for append-only storage invariants; retention, archival, compaction, caching, and performance remain constrained or deferred. |
| 03 | draft | Drafted but only partially implementation-ready; schema/version/reference serialization and emission details remain blockers unless scoped out. |
| 90 | draft | Drafted as citation/routing surface only; 05 remains the canonical gap register. |
| 91 | draft | Drafted to expose forbidden paths; new rejections still require source or change-control record. |

## First-Section Gap Citations

These rows are read from the draft `90` section. `05` remains canonical.

| Gap From 05 | Applies To | Required Handling |
|---|---|---|
| Envelope type, shape ref, references, and parametrization boundary | `01`, `03`, `91` | Constraint. Preserve axis separation and do not add fields, type values, actor subclasses, or product classes. |
| Event schema and versioning tooling | `03` | Blocker for implementation-ready append; constraint for conceptual envelope drafting. |
| Final reference serialization and active emission sites | `03` | Blocker for implementation-ready append if canonical reference names, placement, cardinality, or emission sites are needed. |
| Process reference and process lifecycle semantics | `01`, `03` | Explicit deferral unless process lifecycle or active process-reference emission is included. |
| Structured import/export compatibility | `02`, `03`, `91` | Explicit deferral unless external exchange is included. |
| Projection performance and caching | `02` | Constraint. Do not specify cache/rebuild strategy as architecture. |
| Low-end device scale and offline performance | `02` | Constraint. Do not weaken event-log truth for performance pressure. |
| Retention and archival | `02`, `91` | Constraint; blocker only if deletion, redaction, archive policy, or canonical-history mutation is specified. |
| Operational actor vocabulary and operation-class routing | `01`, `91` | Constraint. Role labels remain product/deployer vocabulary unless formally changed. |
| Exact Pattern Registry inventory and formal schema | `01` | Explicit deferral. `01` may define `pattern` only as a term. |
| General flag semantics and domain conflict automation outside workflow | `91` | Hold back for later flag/conflict sections. |
| Alias-cycle enforcement and resolution semantics | `91` | Hold back for later identity/flag sections. |
| Subject-based scope, auditor access, shared-device actor scope, temporary authority, and authorization visibility details | `03`, `91` | Hold back unless actor/session/reference behavior is over-defined. |

## Canonical Gap Register Snapshot

<details>
<summary>Gap headings and classifications from 05</summary>

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
