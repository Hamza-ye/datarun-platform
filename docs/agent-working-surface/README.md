# Agent Working Surface

Status: active working surface

This directory defines the default post-Phase-4 agent input surface. It exists to keep future sessions from reconstructing current truth from document chronology.

Keep this README stable. Change it only when the durable source categories or routing rules change. Put current status, accepted slice evidence, next-work recommendations, and dated baseline standing in [docs/status.md](/docs/status.md), [baseline-acceptance-register.md](baseline-acceptance-register.md), and [platform-next-work-backlog.md](platform-next-work-backlog.md).

## Authority And Routing Source Order

This is the stable order for resolving authority and routing after a task is
inside the working surface. It is not the minimal startup packet for every
implementer session; use `AGENTS.md` plus `docs/status.md` Current Routing for
startup, then open only the sources the task or touched surface requires.

Use these sources in this order when authority or routing needs to be resolved:

1. [docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md](/docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md)
   - Role: architecture authority.
   - Use the README index, JSON catalog, or `scripts/query_cdl.py` to slice it.
2. [contracts/](/contracts/)
   - Role: implementation-facing wire/process contracts: envelope, sync protocol, flag catalog, shapes, patterns, and shared fixtures.
3. [docs/agent-working-surface/decision-anchor-layer/README.md](decision-anchor-layer/README.md)
   - Role: active stewardship routing surface for DEC anchors and gap routing.
   - CDL and contracts win on authority; BAR/NW govern current implementation standing and evidence.
4. [docs/agent-working-surface/baseline-acceptance-register.md](/docs/agent-working-surface/baseline-acceptance-register.md)
   - Role: current working status of what is accepted, candidate, deferred, or future-decision.
5. [docs/agent-working-surface/platform-next-work-backlog.md](platform-next-work-backlog.md)
   - Role: accepted and future-decision work routing evidence.
6. [docs/specifications/](/docs/specifications/README.md)
   - Role: accepted product-facing and platform-detail behavior below CDL and contracts.
   - Open only the exact specification named by the task or NW route.
7. [docs/operations/](/docs/operations/README.md)
   - Role: accepted deployment-owner policy, executable runbooks, and rehearsal evidence.
   - Open only the exact policy, procedure, or rehearsal named by the task.
8. [docs/agent-working-surface/operational-ux-layering-companion.md](/docs/agent-working-surface/operational-ux-layering-companion.md)
   - Role: non-authoritative operational UX/product vocabulary and layering guardrail for UI, reporting, workflow, and product-design slices.
   - Use it before concrete operational design/code; CDL, BAR, and contracts win on authority and accepted status.
9. [docs/agent-working-surface/escape-hatch-register.md](/docs/agent-working-surface/escape-hatch-register.md)
   - Role: active routing source for measured evolution paths.
   - It is not architecture authority and does not authorize implementation by itself.
10. [docs/README.md](/docs/README.md), [docs/constraints.md](/docs/constraints.md), and [docs/scenarios/README.md](/docs/scenarios/README.md)
   - Role: vision, operational context, and scenario index.
11. [docs/scenarios/](/docs/scenarios/) and [docs/access-control-scenario.md](/docs/access-control-scenario.md)
   - Role: problem-space coverage.
12. Phase files and existing IDRs
   - Role: implementation history, design provenance, and verification leads. Future work should route through the decision-anchor layer first; use IDRs only when explicitly routed and do not make new IDRs the default decision home.

## Process Responsibility Index

This table is a routing index, not a second copy of the process rules.

| Responsibility | Canonical home |
|---|---|
| Agent startup and minimal reading order | `AGENTS.md` plus `docs/status.md` Current Routing. |
| Steward broad-reading, dispatch, and reconciliation rules | `steward-session-guide.md`. |
| Stable authority and routing source order | This README. |
| Architecture authority | The CDL via `docs/architecture/adrs-decisions-canonical-ledger/README.md`. |
| Architecture-sensitive pressure and gap classification | `decision-anchor-layer/gap-routing-playbook.md`. |
| DEC anchor mapping and accepted extension inputs | `decision-anchor-layer/architecture-decision-anchors.md`. |
| Current baseline standing and evidence | `baseline-acceptance-register.md` and `platform-next-work-backlog.md`. |
| Review findings and implementation/refactoring debt | Gap playbook classification first; `platform-next-work-backlog.md` for promoted work; code/tests and commit trace for bounded implementation fixes. |
| Durable output homes and metadata | `docs/documentation-organization.md`. |
| Commit and progress transitions | `docs/commit-workflow.md`. |
| Execution packets | `prompts/README.md` and the selected prompt. |
| Product/UX vocabulary guardrails | `operational-ux-layering-companion.md`. |
| Measured evolution paths | `escape-hatch-register.md`. |
| Historical gated-risk provenance | `docs/flagged-positions.md` only when a current route names an FP, the touched surface matches an FP topic, or a drift/provenance audit needs the old gate. |
| Other historical provenance | `decision-anchor-layer/provenance-index.md`, phase files, IDRs, checkpoints, and review packs only when routed. |

## Common-Process Mapping

The working surface uses repository-specific names, but the process shape is
ordinary:

| Repository term | Common process role |
|---|---|
| CDL | Architecture decision record / decision log. |
| Contracts | Interface and schema control. |
| Gap playbook | Intake triage and change classification. |
| BAR | Acceptance/status register for baseline capability standing. |
| NW backlog | Work backlog with source, dependency, exit condition, and evidence trace. |
| Specifications and operations docs | Canonical requirement, policy, procedure, and evidence homes. |
| Commit workflow | Change-control and traceability convention. |
| Flagged positions | Historical risk register/provenance, not active intake. |

## First-Deployment Router

First-deployment work routes through
[first-deployment-task-packet-router.md](first-deployment-task-packet-router.md).
The workshop has been consolidated into one summary and one implementation
task under
[docs/workshops/first-deployment/](/docs/workshops/first-deployment/README.md).
Do not recreate the removed stage/role/gate packet chain.

## Durable Output Router

Before creating a specification, policy, runbook, rehearsal record, or other
long-lived document, use
[docs/documentation-organization.md](/docs/documentation-organization.md).
When a task authorizes commits, use
[docs/commit-workflow.md](/docs/commit-workflow.md) for route, decision/spec,
implementation, acceptance, review, hygiene, and checkpoint boundaries.

The canonical homes for new outputs are:

- product specifications: `docs/specifications/product/`;
- platform specifications: `docs/specifications/platform/`;
- operational policies: `docs/operations/policies/`;
- executable runbooks: `docs/operations/runbooks/`;
- rehearsal plans and dated records: `docs/operations/rehearsals/`.

Prompts and `artifacts/` remain routing surfaces, not the final home for these
accepted outputs.

Use [steward-session-guide.md](steward-session-guide.md) only for explicit
steward, audit, routing, reconciliation, checkpoint, or architecture/gap
classification work.

## Superseded Review Drafts

The Phase 4 evidence-pack, backlog, and escape-hatch consolidation drafts have been superseded by the active working-surface registers in this directory. Do not recreate those drafts as agent input, and do not use historical review-pack vocabulary as current implementation status.

## Agent Rule

Do not infer current truth from document chronology.

For architecture, use the CDL. For DEC anchors, gap routing, and future-work
classification, use the decision-anchor layer. For accepted product/platform
behavior and operational procedure, use only the exact specification or
operations document routed by the task. For operational UX/product vocabulary
and layering, use the operational UX companion. For measured evolution paths,
use the escape-hatch register as routing context only. For problem-space
pressure, use scenarios and access control. For contracts, use
[contracts/](/contracts/). For current implementation status, use the baseline
acceptance register and platform next-work backlog.

Historical phase files, IDRs, audits, the retired rationale companion,
`docs/flagged-positions.md`, and review packs are provenance unless the current
task is explicitly routed there.

## Stop Conditions

Stop and report instead of implementing when a request would:

- add or imply new envelope fields or event `type` values;
- add durable workflow-state authority;
- rewrite normal sync watermarks or turn live sync into historical pull;
- promote IdP group/claim authority, online production binding-admin APIs, or other production-auth authority expansions without a successor decision;
- promote general trigger execution, auto-resolution, resolver reassignment, S06/entity lifecycle, field-level sensitivity, or new scope mechanisms without a successor decision;
- treat deployer configuration as code, scope logic, or state-machine authoring;
- claim an escape-hatch trigger without measured evidence and successor-decision routing.
