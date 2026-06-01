# NW-023 Agent Prompt: Create Scenario-To-Baseline Pressure Map

You are working as a Datarun Platform architecture-steward agent, not as an implementer.

## Goal

Create `docs/reviews/scenario-baseline-pressure-map.md`.

The purpose is to triage relevant scenarios against the current baseline, deferred/future-decision surfaces, contracts, and next verification work, then recommend 2-4 scenario-grade probes.

This is a review/map only. Do not implement code. Do not change contracts. Do not mark BAR rows accepted.

## Required Reading

Start with the normal routing packet:

1. `AGENTS.md`
2. Top **Current Routing** section of `docs/status.md`
3. `docs/agent-working-surface/README.md`
4. `docs/reviews/scenario-baseline-pressure-map-protocol.md`
5. `docs/scenarios/README.md`
6. `docs/access-control-scenario.md`
7. `docs/agent-working-surface/baseline-acceptance-register.md`
8. `docs/agent-working-surface/platform-next-work-backlog.md`
9. `docs/reviews/viability-closure-review.md`
10. `docs/behavioral_patterns.md`

Then triage relevant scenarios. Read scenario files one at a time as needed; do not bulk-load the whole scenario folder into context.

Relevant scenario set:

- Core/foundational/structural: S00-S14
- Cross-cutting offline: S19
- Composite real-world: S20-S22
- Post-Phase-4 thickened scenarios: S23-S27
- Deferred extension mini-briefs: S15, S16, S18 as triage rows only unless the row needs a clear deferred/future route

Use CDL through the README index, JSON catalog, or `scripts/query_cdl.py` only when a scenario row needs authority clarification. Do not read the full CDL.

## Authority And Routing

Use this source order:

1. CDL is architecture authority.
2. Rationale companion is non-authoritative routing/rationale only.
3. Escape-hatch register is measured evolution routing only.
4. Scenarios define problem-space pressure, not implementation.
5. Contracts define implementation-facing process boundaries.
6. BAR defines current accepted/candidate/deferred/future-decision standing.
7. Backlog defines current work ordering.

Specific standing constraints:

- S12 remains active scenario pressure.
- General trigger execution is deferred under BAR-101.
- Future trigger execution is constrained to server-side L3 policy per CDL-042.
- S06/entity lifecycle remains deferred under BAR-105 unless successor decision work is explicitly selected.
- Do not promote S15/S16/S18 deferred extensions.
- Do not infer current truth from old review artifacts.

## Output Requirements

Create `docs/reviews/scenario-baseline-pressure-map.md` using the protocol’s required structure (read: `docs/reviews/scenario-baseline-pressure-map-protocol.md`):

1. Purpose And Method
2. Scenario Triage Table
3. Recommended 2-4 Scenario Probes
4. Selected Scenario Walkthroughs
5. Deferred/Future Pressure Register
6. Test And Backlog Recommendations
7. Safe Progress Call


Important sizing rule:

- Triage all relevant scenarios in the map.
- Deep-review only the selected 2-4 probe candidates.
- Do not compress a selected walkthrough so much that it loses operational meaning.
- If a selected walkthrough becomes too large for the map to remain agent-usable, create a linked file under `docs/reviews/scenario-pressure-probes/` for that selected scenario and keep the map as the index and recommendation surface.
- Do not create per-scenario files for non-selected scenarios.

## Triage Expectations

For each relevant scenario, classify:

- scenario class
- primary pressure
- BAR rows touched
- contracts touched, if any
- deferred/future surfaces touched
- current probe viability
- probe value
- one-sentence reason

Use the protocol labels exactly where applicable:

- `accepted_baseline_pressure`
- `candidate_baseline_pressure`
- `deferred_surface_pressure`
- `future_decision_pressure`
- `contract_pressure`
- `runtime_probe_candidate`
- `documentation_only_pressure`

Use evidence levels carefully. Do not collapse scenario pressure into implementation evidence.

## Probe Selection Guidance

Recommend 2-5 near-term probes.

A healthy set should usually include:

- one simple baseline flow;
- one offline/sync/authority flow;
- one assignment/responsibility/review flow;
- one composite, reporting, or non-health proof if it can avoid deferred work.

The selected probes should pressure the current baseline without accidentally requiring:

- general trigger execution;
- auto-resolution;
- resolver reassignment;
- production OIDC/JWT/group-claim authority;
- entity lifecycle;
- field-level sensitivity/encryption/redaction;
- new envelope fields/types;
- new scope mechanisms.

## Forbidden Work

Do not:

- edit code;
- edit contracts;
- edit BAR statuses except optionally adding a review-link note if clearly needed;
- mark NW-023 accepted unless the repository convention already requires that and the map is complete;
- promote deferred/future-decision surfaces;
- rewrite scenario language;
- treat scenarios as implementation specs;
- use legacy Phase 4 evidence-pack/backlog/escape-hatch drafts as current authority.

## Verification

Run:

```bash
git diff --check
rg -n "general trigger execution|auto-resolution|resolver reassignment|production OIDC|JWT|Keycloak|entity lifecycle|new envelope|new scope|field-level sensitivity" docs/reviews/scenario-baseline-pressure-map.md docs/reviews/scenario-pressure-probes docs/agent-working-surface/platform-next-work-backlog.md docs/agent-working-surface/baseline-acceptance-register.md