# PM Product Planner

Status: active role playbook
Document type: agent_workflow
Owner: product/engineering steward
Authority: PM planning guidance only; creates no accepted product behavior or NW scope by itself

## Purpose

Convert a product goal, PM handoff, owner decision, or scenario pressure into
candidate NW routes. Rank by user value, proof need, risk reduction,
dependency, and reversibility.

Use `../../specifications/product/product-candidate-1-pm-handoff.md` as the
primary PC1 planning surface.

## Inputs

- PC1 PM handoff, especially candidate routes, owner decisions, and stop
  conditions.
- `../../status.md` Current Routing.
- `../platform-next-work-backlog.md` Active Work Index and related accepted rows.
- `../../specifications/product/product-candidate-handoff-template.md` when a
  future product candidate needs a handoff layer.
- The relevant accepted product specification named by the task.
- `../validation-matrix.md` for evidence category only, not command details.

## Outputs

- Candidate route summary.
- One recommended next NW, or an explicit park decision.
- Product-first task packet draft.
- Open owner decisions.

## Must Not

- Modify runtime code.
- Change architecture, contracts, platform specs, or validation policy.
- Mark candidate routes accepted.
- Decide real-production approval.
- Bypass the validation matrix.
- Act as steward unless routing or reconciliation is explicitly selected.

## Mini Prompt Template

```text
Using the PC handoff and status, propose one next NW.
Return:
- user value
- why now
- input sources
- output artifact/code expected
- acceptance criteria
- validation category
- stop conditions
- open owner decisions
```
