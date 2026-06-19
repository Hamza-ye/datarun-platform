# Working-Surface Artifacts

Status: non-authoritative artifact index

This directory stores bounded outputs from NW prompts when the output is useful routing context but not architecture authority.

Use this directory for:

- explorations;
- routing reports;
- deferral reports;
- non-binding decision-routing summaries;
- bounded stewardship notes that should remain available but should not clutter the working-surface root.

Do not use this directory for binding decisions, accepted platform
specifications, final operations procedures, or implementation evidence.
Exploration artifacts should recommend the correct durable home for accepted
follow-up work. If a slice selects platform behavior that future implementation
must follow, route it to the correct durable surface: platform specification,
contract, operation/policy document, implementation evidence, or a CDL
successor when canonical architecture authority is required. Do not assume an
IDR is the durable home.

Use `docs/documentation-organization.md` for the exact durable home of accepted
product/platform specifications, operational policies, runbooks, rehearsals,
contracts, and implementation evidence.

Current artifacts:

| artifact | source NW / source pressure | standing | durable successor / canonical home | current use | do-not-use-for |
|---|---|---|---|---|---|
| `2026-06-18-multi-tenancy-architecture-analysis.md` | Multi-tenancy architecture analysis pressure | Non-binding routing analysis | NW-094 through NW-098 if managed control-plane or tenant-aware runtime work becomes active | Evidence for control-plane and tenant-aware decision routing | Accepting tenant-aware internals, contracts, storage predicates, sync context, or product behavior |
| `architecture-classification-drift-audit.md` | 2026-06-16 architecture classification drift audit | Non-binding audit evidence | Gap playbook, DEC anchors, NW backlog, and any selected durable spec/decision route | Classification re-test evidence before relying on historical architecture prose | Architecture authority or accepted behavior by itself |
| `2026-06-19-agents-md-instruction-file-audit.md` | NW-104 post-audit AGENTS.md instruction-file audit | Non-binding audit evidence | Future AGENTS.md trim/split/control-surface NW if selected | Evidence for root/nested agent-instruction reset | Rewriting AGENTS.md, changing agent authority, or altering product/platform behavior by itself |
| `2026-06-19-product-goal-pm-handoff-audit.md` | NW-104 post-audit product goal / PM handoff audit | Non-binding audit evidence | Future PM handoff/product reset rows if selected | Evidence for product-goal clarity and PM-handoff reset | New product scope, accepted product behavior, or backlog priority by itself |
| `2026-06-19-test-ci-validation-strategy-audit.md` | NW-104 post-audit test/CI/validation audit | Non-binding audit evidence | Future validation matrix/mobile CI/analyzer reset rows if selected | Evidence for validation reset and CI/test gate gaps | CI policy, test gate authority, or runtime behavior by itself |
| `idr-durable-surface-routing-audit.md` | 2026-06-16 IDR durable-surface routing audit | Non-binding routing audit | Durable specs, contracts, module interfaces, gap playbook, and NW extraction rows | Evidence for routing scattered IDR-era behavior into durable homes | Treating IDR prose as current normative authority without an active route |
| `NW-043-assignment-admin-authority-exploration.md` | NW-043 assignment-admin authority exploration | Accepted non-binding exploration artifact | `docs/specifications/platform/assignment-scope-and-administration.md` plus accepted NW-048/NW-050 behavior | Historical routing for assignment-admin command-capability work | Broad production assignment-admin exposure or new authority source |
| `NW-049-access-exceptions-shared-device-scope-exploration.md` | NW-049 access-exceptions/shared-device routing | Accepted non-binding routing artifact | NW-051 through NW-054 and shared-device/local-state platform spec routes | Routing evidence for access exceptions, shared-device scope, and retention/security splits | Implementing special access, retention, or shared-device behavior directly |
| `NW-051-special-read-write-access-boundary-routing.md` | NW-051 special read/write access boundary | Accepted non-binding routing artifact | NW-053 and NW-054 for successor scope or retention/security decisions | Deferral evidence for broad audit/history, emergency write, and dynamic auditor access | Emergency bypass, broad audit API, or hidden sync scope authority |
| `NW-056-product-standing-and-production-readiness-map.md` | NW-056 product standing and production-readiness pressure | Non-binding readiness map | NW-063 through NW-067 and NW-075 through NW-081 operations evidence; NW-093 for real production | Historical readiness pressure and split between product standing and operations evidence | Current production-ready claim or clean-image evidence |
| `NW-063-production-deployment-ops-hardening-map.md` | NW-063 first reference deployment hardening map | Accepted non-binding routing map | NW-064 through NW-067, NW-075 through NW-081, operations policies/runbooks/rehearsals | Routing evidence for reference deployment policy, tooling, runbook, and rehearsal slices | Real-production approval or proof of backup/restore/monitoring by itself |
| `NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md` | NW-082 Product Candidate 1 routing | Accepted non-binding routing artifact | NW-083 managed-isolation route and NW-084 product specification | Milestone boundary and multi-tenancy route provenance | Product behavior, tenant architecture, implementation, or production readiness authority |
| `NW-083-tenant-workspace-vocabulary-and-managed-isolation-boundary.md` | NW-083 tenant/workspace vocabulary and managed-isolation boundary | Accepted routing-only artifact | NW-084 product specification and NW-094 through NW-098 if tenant/control-plane work is selected | Managed-isolation vocabulary and default Workspace routing | Tenant-aware runtime, storage, sync, auth, mobile partition, or contract acceptance |
| `product-admin-surface-forward-plan.md` | Product admin surface planning pressure | Non-authoritative forward plan | Accepted specs and implementation rows for web-admin auth, command gates, config, and assignment admin | Planning provenance for product admin sequencing | Current accepted status, authority, or implementation requirement |
| `product-candidate-1-orchestration-note.md` | Product Candidate 1 orchestration pressure | Non-authoritative coordination note | Selected PC1 NW rows and durable specs/implementation evidence | Planning provenance for PC1 sequencing | Claiming accepted product behavior or bypassing backlog selection |
| `solo-ai-agent-operating-framework.md` | Solo AI-agent operating-framework pressure | Non-authoritative artifact; not a parallel process authority | Future NW-selected extraction into existing homes only | Source for possible future process-rule extraction | Instructions, agent authority, or progress process outside the accepted working surfaces |

Planned artifacts are named by their active backlog row and prompt; do not list
them here until they land.
