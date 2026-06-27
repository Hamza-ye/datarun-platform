---
name: datarun-reviewer
description: Review a Datarun PR or diff from fresh context against the selected NW/task, scope boundaries, product handoff when product-facing, validation evidence, and progress-staleness risks grounded in repository evidence.
---

# Datarun Reviewer

Use this skill for one PR, diff, or explicit review request. Start from fresh
repository evidence, not previous session memory.

## Required Inputs

1. PR link, branch, commit range, or working diff.
2. `docs/status.md` Current Routing.
3. Active NW row, selected task packet, or explicit user request.
4. `docs/agent-working-surface/platform-next-work-backlog.md` when the diff
   touches status, backlog, routing, or successor selection.
5. `docs/agent-working-surface/validation-matrix.md`.
6. Product handoff/spec when the change is product-facing.
7. Nested `server/AGENTS.md`, `mobile/AGENTS.md`, or `contracts/AGENTS.md`
   when those surfaces are touched.

## Review Method

Classify the issue before acting:

- product work;
- bug;
- architecture decision or contract/schema/protocol change;
- delivery-process issue;
- inherited debt;
- quality/security risk;
- recovery/operations issue;
- progress-staleness risk.

Use proven practice only as supporting evidence: official docs, standards
bodies, mature engineering practice, or empirical research. If proven practice
conflicts with current repo behavior, call out the conflict and propose the
least disruptive correction that restores product progress.

## Findings To Prioritize

Lead with concrete findings, ordered by severity:

- behavior bugs, broken tests, security/authority bugs, contract/schema drift,
  data-loss or stored-truth risk;
- scope creep beyond the selected NW/task;
- product-facing changes not anchored to accepted product goal/spec/handoff;
- missing focused/full validation for the touched surface;
- status/backlog/BAR claims that do not match the diff or evidence;
- stale successor selection, stale "blocked" wording, or stale artifact trace
  that could cause the next agent to choose the wrong work;
- review/process risks that are visible in the PR: broad mixed-scope diff,
  generated plan without executable checks, CI red or absent for touched
  surface, unreviewable test logs, or follow-ups buried in prose.

Do not invent process risks without evidence. If a risk is only hypothetical,
state it as a non-blocking watch item or omit it.

## Progress-Staleness Checks

Check these only when the diff or PR touches routing, status, backlog, docs,
validation claims, or successor selection:

- Current Routing still names the true active implementation/product slice.
- Accepted/closed rows have evidence and do not leave a hidden successor.
- Candidate, deferred, future-decision, and blocked rows keep their current
  standing unless the diff supplies an owner decision or trigger.
- "Blocked" names the exact action and evidence that makes progress impossible.
- Artifact trace matches actual touched files and durable outputs.
- The PR does not mark work accepted because files changed.
- Follow-ups are classified as current-slice fix, selected successor, candidate
  backlog row, explicit deferral with trigger, or rejected/not a risk.

If current process is unhealthy, recommend proven basics before new process:
reduce WIP, shrink scope, restore green CI/trunk health, close stale work, and
document only actionable facts.

## Outputs

Return:

- verdict: approve, amend, reject, or needs evidence;
- blocking findings with file/line references;
- non-blocking follow-ups only when they are evidence-backed;
- scope and progress-staleness check;
- validation sufficiency check;
- suggested smallest patch or routing correction when needed.

## Must Not

- Do not rewrite the implementation during review.
- Do not accept candidate routes as backlog.
- Do not over-escalate architecture without a trigger.
- Do not require future gates as current gates.
- Do not use broad process advice as a blocker unless it maps to a concrete PR
  risk and a least-disruptive correction.
