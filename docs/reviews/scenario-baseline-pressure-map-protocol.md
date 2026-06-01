# Scenario-To-Baseline Pressure Map Protocol

Status: active review protocol

Authority: none. This protocol does not change architecture, implementation status, or scenario truth. It routes scenario pressure against the CDL, BAR, backlog, contracts, and deferred/future-decision registers.

## Purpose

Use this protocol to create a scenario-to-baseline pressure map before selecting scenario-grade runtime probes.

The map answers:

```text
Which scenarios pressure the accepted baseline?
Which scenarios expose unaccepted baseline candidates?
Which scenario steps require deferred or future-decision surfaces?
Which 2-4 scenarios are the best near-term probes without accidentally promoting deferred work?
```

## Source Order

Use the normal working-surface flow:

1. `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md`
   - Use `scripts/query_cdl.py`, the README index, or JSON catalog to slice.
2. `docs/milestone-review/phase-4-review/architecture-rationale-and-routing-companion.md`
   - Rationale and change routing only.
3. `docs/agent-working-surface/escape-hatch-register.md`
   - Measured evolution routes only.
4. `docs/README.md`, `docs/constraints.md`, and `docs/scenarios/README.md`, `docs/access-control-scenario.md`
   - Vision, Ambition, Core Commitments, operational context, scenario index, Who can see what, do what, and under what circumstances.
5. Selected `docs/scenarios/*.md` files.
6. `contracts/`
   - Envelope, sync, flag catalog, platform shapes, patterns, fixtures.
7. IDRs/phase files only when a selected scenario step routes there.
8. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Current accepted/candidate/deferred/future-decision standing.
9. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Current work ordering.

Do not use old review drafts as source truth.

## Output File

The pressure map should land as:

```text
docs/reviews/scenario-baseline-pressure-map.md
```

## Scope Rules

- This is a review/map, not implementation.
- Scenarios remain problem-space inputs.
- The map may recommend runtime probes, tests, backlog rows, or successor decisions.
- The map must not mark BAR rows accepted.
- The map must not promote deferred/future-decision work.
- The map must distinguish architecture support, code evidence, test evidence, and runtime scenario evidence.

## Scenario Classification

Classify each reviewed scenario with these fields:

| Field | Meaning |
|---|---|
| Scenario | Scenario number and title. |
| Scenario class | `simple_baseline`, `baseline_composite`, `phase2_pressure`, `future_decision_pressure`, `scenario_context_only`. |
| Primary pressure | The main platform capability or operational risk the scenario stresses. |
| BAR rows touched | Accepted/candidate/deferred/future-decision rows touched by the scenario. |
| Contracts touched | Envelope, sync, flag, shape, pattern, or fixture contracts touched. |
| Deferred surfaces touched | BAR-101 through BAR-108 or other deferred/future-decision surfaces. |
| Current probe viability | `ready_now`, `ready_with_constraints`, `verification_needed_first`, `defer`, `successor_decision_required`. |
| Probe value | `high`, `medium`, or `low`. |
| Why this matters | One sentence. |

## Pressure Labels

Use these labels consistently:

| Label | Meaning |
|---|---|
| `accepted_baseline_pressure` | Scenario step should already run against accepted BAR rows. |
| `candidate_baseline_pressure` | Scenario step maps to a BAR candidate that needs verification. |
| `deferred_surface_pressure` | Scenario step needs a deferred capability and must not be implemented now. |
| `future_decision_pressure` | Scenario step requires successor platform/architecture decision. |
| `contract_pressure` | Scenario step crosses process-boundary contracts. |
| `runtime_probe_candidate` | Scenario step is useful for a scenario-grade test or walkthrough now. |
| `documentation_only_pressure` | Scenario should stay as product context; no runtime probe now. |

## Evidence Levels

Use this ladder. Do not collapse levels.

| Evidence level | Meaning |
|---|---|
| `scenario_pressure` | The scenario asks for the behavior. |
| `architecture_supported` | CDL/IDR allows or requires the behavior. |
| `contract_defined` | Contract files define the process-boundary shape/protocol. |
| `code_evidence` | Current code appears to implement it. |
| `targeted_test_evidence` | Focused tests cover it. |
| `runtime_scenario_evidence` | A composed scenario walkthrough/test has exercised it. |
| `unsupported_currently` | Current platform does not support it. |
| `deferred_or_future` | Behavior is intentionally deferred or future-decision. |

## Selected Scenario Review Template

Use this structure for each scenario selected for deeper review:

```yaml
scenario:
  id:
  title:
  real_world_goal:
  scenario_class:
  primary_pressure:
  candidate_probe_value:

actors:
  - role:
    responsibility:
    current_authority_model:
    expected_scope:

subjects_or_real_world_things:
  - description:
    identity_risk:
    location_or_scope:
    lifecycle_pressure:

offline_windows:
  - actor:
    duration_or_condition:
    stale_risks:

activities_and_config:
  - activity_or_work:
    shapes_or_payloads:
    expressions:
    pattern_binding:
    config_pressure:

expected_events:
  - type:
    shape_ref:
    subject_ref:
    actor_ref:
    activity_ref:
    notes:

expected_flags:
  - category:
    reason:
    severity:
    resolvability:
    resolver_route:

sync_projection_retention:
  live_sync_expectation:
  subject_history_expectation:
  projection_expectation:
  local_retention_expectation:

deferred_or_future_surfaces:
  - surface:
    reason:
    route:
```

## Map Output Structure

The final map must use this structure:

```md
# Scenario-To-Baseline Pressure Map

## 1. Purpose And Method

## 2. Scenario Triage Table

| Scenario | Class | Primary pressure | BAR rows | Deferred/future surfaces | Probe viability | Probe value | Notes |
|---|---|---|---|---|---|---|---|

## 3. Recommended 2-4 Scenario Probes

| Rank | Scenario/probe | Why this one | BAR rows pressured | Forbidden scope | Expected evidence |
|---|---|---|---|---|---|

## 4. Selected Scenario Walkthroughs

Use the selected scenario review template, condensed to readable Markdown.

## 5. Deferred/Future Pressure Register

| Scenario | Pressure | Route | Why not now |
|---|---|---|---|

## 6. Test And Backlog Recommendations

| Recommendation | Type | Priority | Depends on | Exit condition |
|---|---|---|---|---|

## 7. Safe Progress Call

State what can proceed immediately, what should wait for NW-009/NW-010, and what needs successor decisions.
```

## Probe Selection Rubric

Pick 2-4 probes using these criteria:

| Criterion | Weight | Interpretation |
|---|---:|---|
| Touches accepted baseline | 3 | Prefer probes that validate what the platform claims as current baseline. |
| Exposes candidate BAR risk | 3 | Prefer probes that clarify remaining baseline candidates. |
| Crosses server/mobile/process boundary | 2 | Prefer probes that exercise contracts and sync/projection seams. |
| Avoids deferred/future promotion | 3 | Penalize probes that need triggers, entity lifecycle, production auth, field-level sensitivity, or new scope mechanisms. |
| Real operational value | 2 | Prefer scenarios that reflect believable field workflows. |
| Testability now | 2 | Prefer probes that can become targeted runtime tests without broad feature work. |
| Domain breadth | 1 | Include at least one non-health proof if feasible. |

Do not select only the most complex scenarios. A healthy first set should usually include:

- one simple baseline flow;
- one offline/sync/authority flow;
- one assignment/responsibility or review flow;
- one composite/non-health/reporting flow, if it does not require deferred work.

## Stop Conditions

Stop and report instead of completing the map when:

- a scenario step appears to require new envelope fields or event types;
- a selected probe would require general trigger execution, auto-resolution, resolver reassignment, production auth authority, entity lifecycle, field-level sensitivity, or a new scope mechanism;
- contracts and BAR disagree about current capability;
- the map cannot distinguish scenario pressure from implementation baseline;
- a scenario would require old review artifacts as current truth.
