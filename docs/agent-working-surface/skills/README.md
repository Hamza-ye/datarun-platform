# Agent Skills

Status: active role-playbook index
Document type: agent_workflow
Owner: product/engineering steward
Source: NW-108; post-audit control-plane reset; AGENTS.md instruction-file audit; PM handoff; validation matrix
Authority: role routing and task-shaping guidance only; does not add product behavior, validation policy, architecture authority, implementation standing, or acceptance by itself
Last reviewed: 2026-06-20

## Purpose

These playbooks make the agent role explicit before work starts. They keep PM
planning, implementation, validation auditing, review, boundary review, and
steward reconciliation separate.

Default role remains Implementation Agent unless the task selects another role
or an architecture/gap stop trigger fires. Skills route work; they do not
override accepted specifications, contracts, status, backlog rows, the
validation matrix, or `AGENTS.md`.

## Role Selection

| Role | Use when | Primary inputs | Output | Must not do |
|---|---|---|---|---|
| PM Product Planner | Selecting or shaping product-facing planning or the next NW candidate. | Status, backlog, NW-167 sequencing guide, active Product Goal and representative journey portfolio when present, Product Candidate handoff only when named or being created, accepted product spec, validation matrix category. | Planning summary, recommended next NW or park decision, open owner decisions. | Accept scope, bypass validation, change runtime, jump to implementation without a selected implementation NW/task, or approve real production. |
| Tester / Validation Auditor | Mapping a diff or claimed acceptance to required evidence. | Validation matrix, diff, status/backlog claims, nested AGENTS when relevant. | Validation checklist, missing evidence, risk classification, suggested commands. | Invent product scope, make known-red gates hard blockers, or require future gates as current. |
| Software Architect Boundary Reviewer | A stop trigger fires or a task proposes runtime boundary change. | Root `AGENTS.md` triggers, gap playbook, status, touched specs/contracts/code. | Boundary verdict and minimal next route. | Run by default, block ordinary UI/copy work, or expand broad CDL without need. |
| Implementation Agent | One selected NW/task is ready to execute. | User task, active NW row, Current Routing, touched files, nested AGENTS, validation matrix. | Scoped diff, validation evidence, stop report if triggers fire. | Choose the next NW, act as steward, or mark acceptance from file changes alone. |
| Reviewer | Fresh-context PR/diff review is requested. | PR/diff, active NW row, status/backlog acceptance, validation matrix, PM handoff when product-facing. | Verdict, blocking issues, follow-ups, scope/evidence check. | Rewrite implementation, accept candidate routes, or over-escalate architecture. |
| Steward Session Guide | Routing, reconciliation, checkpoint, drift, or broad audit work is explicitly selected. | `../steward-session-guide.md`, status, backlog, gap playbook, accepted docs. | Reconciled route, dispatch packet, checkpoint, or drift finding. | Become default implementer or make artifacts authority by themselves. |

## Allowed Sequence

PM Product Planner -> selected implementation NW/task -> Implementation Agent -> Tester/Validation Auditor -> Reviewer -> Steward only for reconciliation/checkpoint

Software Architect Boundary Reviewer only when a stop trigger fires.

## Anti-Patterns

- using steward as default implementer
- using architect review for ordinary UI/product copy
- using implementation agent to choose next NW
- using tester to invent product scope
- using PM planner to bypass validation matrix
- using skills as authority over accepted docs

## Codex Skill Wrappers

These repository playbooks are durable role docs. Codex-discoverable wrappers
live under `.agents/skills/` and point back to these files. The wrappers are for
skill discovery only; they do not create separate authority or duplicate the
role rules.

## Common Evidence

- `../validation-matrix.md`
- `../../status.md`
- `../platform-next-work-backlog.md`
- `../product-journey-and-slice-sequencing.md`
- `../../specifications/product/product-goal-and-representative-journeys.md`
  after NW-168
- Product Candidate handoff named by status, task, or
  `../../specifications/product/README.md` only when applicable
- `../../specifications/product/product-candidate-handoff-template.md`
- `../../commit-workflow.md`
