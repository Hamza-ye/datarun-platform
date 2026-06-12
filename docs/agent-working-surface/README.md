# Agent Working Surface

Status: active working surface

This directory defines the default post-Phase-4 agent input surface. It exists to keep future sessions from reconstructing current truth from document chronology.

Keep this README stable. Change it only when the durable source categories or routing rules change. Put current status, accepted slice evidence, next-work recommendations, and dated baseline standing in [docs/status.md](/docs/status.md), [baseline-acceptance-register.md](baseline-acceptance-register.md), and [platform-next-work-backlog.md](platform-next-work-backlog.md).

## Source Order

Use sources in this order:

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
6. [docs/agent-working-surface/operational-ux-layering-companion.md](/docs/agent-working-surface/operational-ux-layering-companion.md)
   - Role: non-authoritative operational UX/product vocabulary and layering guardrail for UI, reporting, workflow, and product-design slices.
   - Use it before concrete operational design/code; CDL, BAR, and contracts win on authority and accepted status.
7. [docs/agent-working-surface/escape-hatch-register.md](/docs/agent-working-surface/escape-hatch-register.md)
   - Role: active routing source for measured evolution paths.
   - It is not architecture authority and does not authorize implementation by itself.
8. [docs/README.md](/docs/README.md), [docs/constraints.md](/docs/constraints.md), and [docs/scenarios/README.md](/docs/scenarios/README.md)
   - Role: vision, operational context, and scenario index.
9. [docs/scenarios/](/docs/scenarios/) and [docs/access-control-scenario.md](/docs/access-control-scenario.md)
   - Role: problem-space coverage.
10. Phase files and IDRs
   - Role: implementation history, design provenance, and verification leads. Future work should route through the decision-anchor layer first; use IDRs only when explicitly routed.

## First-Deployment Planning Router

First-deployment product/spec/evidence work routes through
[first-deployment-task-packet-router.md](first-deployment-task-packet-router.md).
That file is an active coordination surface, not architecture authority and not
baseline acceptance. It makes the Stage 8 task-packet backlog and FD-PKT
decision records visible to new sessions without moving workshop outputs into
BAR/NW baseline truth. The workshop artifacts themselves live under
[docs/workshops/first-deployment/](/docs/workshops/first-deployment/README.md).

## Superseded Review Drafts

The Phase 4 evidence-pack, backlog, and escape-hatch consolidation drafts have been superseded by the active working-surface registers in this directory. Do not recreate those drafts as agent input, and do not use historical review-pack vocabulary as current implementation status.

## Agent Rule

Do not infer current truth from document chronology.

For architecture, use the CDL. For DEC anchors, gap routing, and future-work classification, use the decision-anchor layer. For operational UX/product vocabulary and layering, use the operational UX companion. For measured evolution paths, use the escape-hatch register as routing context only. For problem-space pressure, use scenarios and access control. For contracts, use [contracts/](/contracts/). For current implementation status, use the baseline acceptance register and platform next-work backlog.

Historical phase files, IDRs, audits, the retired rationale companion, and review packs are provenance unless the current task is explicitly routed there.

## Stop Conditions

Stop and report instead of implementing when a request would:

- add or imply new envelope fields or event `type` values;
- add durable workflow-state authority;
- rewrite normal sync watermarks or turn live sync into historical pull;
- promote IdP group/claim authority, online production binding-admin APIs, or other production-auth authority expansions without a successor decision;
- promote general trigger execution, auto-resolution, resolver reassignment, S06/entity lifecycle, field-level sensitivity, or new scope mechanisms without a successor decision;
- treat deployer configuration as code, scope logic, or state-machine authoring;
- claim an escape-hatch trigger without measured evidence and successor-decision routing.
