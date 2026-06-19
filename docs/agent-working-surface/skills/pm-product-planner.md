# PM Product Planner

Status: active role playbook
Document type: agent_workflow
Owner: product/engineering steward
Authority: PM planning guidance only; creates no accepted product behavior or NW scope by itself

## Purpose

Convert a product goal, Product Candidate handoff, owner decision, or scenario
pressure into candidate NW routes. Rank by user value, proof need, risk reduction,
dependency, and reversibility.

Use the current Product Candidate handoff named by `../../status.md`, the task,
or `../../specifications/product/README.md` as the primary planning surface.
For PC1 today, that is `product-candidate-1-pm-handoff.md`.

## Inputs

- Current Product Candidate handoff, especially candidate routes, owner
  decisions, and stop conditions.
- `../../status.md` Current Routing.
- `../platform-next-work-backlog.md` Active Work Index and related accepted rows.
- `../../specifications/product/README.md` product specification index.
- `../../specifications/product/product-candidate-handoff-template.md` Product
  Candidate handoff template.
- The accepted product spec named by status or task.
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
