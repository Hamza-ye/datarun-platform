# Architecture Routing Checklist

Status: active checklist

Use this checklist before implementing any architecture-sensitive work item. It is a routing guard, not architecture authority. The CDL remains the authority.

## Required Inputs

- Scenario or operational pressure.
- CDL rows touched.
- Contracts touched, if any.
- Baseline Acceptance Register row or new backlog row.
- Explicit list of deferred surfaces that must not be promoted.

## Routing Questions

Answer each question before implementation:

| Question | Required response |
|---|---|
| Does this touch stored events or event interpretation? | If yes, identify CDL rows and migration/compatibility impact. |
| Does this touch the event envelope? | If yes, stop unless a successor CDL decision authorizes it. |
| Does this add or reinterpret envelope `type` values? | If yes, stop unless a successor CDL decision authorizes it. |
| Does this add config expressiveness? | Classify L0/L1/L2/L3; reject config-as-code or deployer-authored processing. |
| Does this create a new platform mechanism? | Route to platform decision unless CDL already authorizes it. |
| Does this alter sync visibility? | Prove sync scope still equals access scope. |
| Does this alter authority? | Prove authority remains assignment/event-derived and not auth-provider/group-derived. |
| Does this introduce runtime-discovered config failure? | Move failure to deploy-time validation or stop. |
| Does this change state participation or flag lifecycle? | Prove detect-before-act and unresolved-flag exclusion still hold. |
| Does this claim an escape-hatch trigger? | Require measured evidence, route through `docs/agent-working-surface/escape-hatch-register.md`, and stop ordinary implementation until the required successor decision or bounded plan exists. |
| Does this require a CDL successor decision? | If yes, do not implement as an ordinary ticket. |

## Forbidden Shortcuts

- No new envelope fields or type values without successor CDL authority.
- No mutable workflow status as canonical truth.
- No normal sync watermark rewrite to backfill history.
- No deployer-authored scope logic or state-machine transition tables.
- No production OIDC/JWT/group/claim authority without FP-011 resolution.
- No general trigger execution, auto-resolution execution, resolver reassignment, field-level sensitivity, or new scope mechanism by implication.
- No escape-hatch implementation from register presence alone; the register routes measured pressure but is not authority.

## Implementation Prompt Minimum

Every routed implementation prompt must include:

- Goal.
- Files to read, capped and justified.
- Authority and guardrails.
- Forbidden work.
- Expected implementation boundary.
- Targeted tests.
- Commit boundary.
- Stop-and-report conditions.

## Acceptance Rule

Do not move a Baseline Acceptance Register row to `baseline_accepted` using phase claims alone. Attach fresh code inspection, targeted test output, or runtime scenario evidence.
