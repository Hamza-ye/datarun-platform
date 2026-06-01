# Agent Working Surface

Status: active working surface

This directory defines the default post-Phase-4 agent input surface. It exists to keep future sessions from reconstructing current truth from document chronology.

## Source Order

Use sources in this order:

1. `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md`
   - Role: architecture authority.
   - Use the README index, JSON catalog, or `scripts/query_cdl.py` to slice it.
2. `docs/milestone-review/phase-4-review/architecture-rationale-and-routing-companion.md`
   - Role: non-authoritative rationale, routing, test-intent, and change-classification companion.
   - CDL wins on decisions.
3. `docs/agent-working-surface/escape-hatch-register.md`
   - Role: active routing source for measured evolution paths.
   - It is not architecture authority and does not authorize implementation by itself.
4. `docs/README.md`, `docs/constraints.md`, and `docs/scenarios/README.md`
   - Role: vision, operational context, and scenario index.
5. `docs/scenarios/` and `docs/access-control-scenario.md`
   - Role: problem-space coverage.
6. `contracts/`
   - Role: current implementation-facing contracts: envelope, sync protocol, flag catalog, shapes, patterns, and shared fixtures.
7. Phase files and IDRs
   - Role: implementation history, design provenance, and verification leads.
   - Use only when routed by a touched surface, a baseline row, or a drift investigation.
8. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Role: current working status of what is accepted, candidate, deferred, or future-decision.

## Superseded Review Drafts

The Phase 4 evidence-pack, backlog, and escape-hatch consolidation drafts have been superseded by the active working-surface registers in this directory. Do not recreate those drafts as agent input, and do not use historical review-pack vocabulary as current implementation status.

## Agent Rule

Do not infer current truth from document chronology.

For architecture, use the CDL. For rationale and change routing, use the rationale companion. For measured evolution paths, use the escape-hatch register as routing context only. For problem-space pressure, use scenarios and access control. For contracts, use `contracts/`. For current implementation status, use the baseline acceptance register.

Historical phase files, IDRs, audits, and review packs are provenance unless the current task is explicitly routed there.

## Stop Conditions

Stop and report instead of implementing when a request would:

- add or imply new envelope fields or event `type` values;
- add durable workflow-state authority;
- rewrite normal sync watermarks or turn live sync into historical pull;
- promote production OIDC/JWT/group/claim authority without resolving FP-011;
- promote general trigger execution, auto-resolution, resolver reassignment, S06/entity lifecycle, field-level sensitivity, or new scope mechanisms without a successor decision;
- treat deployer configuration as code, scope logic, or state-machine authoring;
- claim an escape-hatch trigger without measured evidence and successor-decision routing.
