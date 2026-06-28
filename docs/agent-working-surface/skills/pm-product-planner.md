# PM Product Planner

Status: active role playbook
Document type: agent_workflow
Owner: product/engineering steward
Authority: PM planning guidance only; creates no accepted product behavior or NW scope by itself

## Purpose

Convert a Product Goal, representative journey portfolio, Product Candidate
handoff, owner decision, or scenario pressure into product-first planning output
and candidate NW routes. Rank by user value, proof need, risk reduction,
dependency, and reversibility.

Start from `../../status.md` Current Routing and
`../product-journey-and-slice-sequencing.md`. After NW-168, use
`../../specifications/product/product-goal-and-representative-journeys.md` as
the expected durable planning source for the active Product Goal and
representative journey portfolio. Use a Product Candidate handoff only when
status or the task names one, or when this work is creating or updating the
handoff.

## Inputs

- `../../status.md` Current Routing.
- `../platform-next-work-backlog.md` Active Work Index and related accepted rows.
- `../product-journey-and-slice-sequencing.md` as the sequencing guide when
  shaping product goals, representative journeys, candidate boundaries, or
  ordered slice plans.
- `../../specifications/product/product-goal-and-representative-journeys.md`
  after NW-168 as the expected durable planning source for the active Product
  Goal and representative journey portfolio.
- `../../specifications/product/README.md` product specification index.
- `../../specifications/product/product-candidate-handoff-template.md` Product
  Candidate handoff template.
- Product Candidate handoff named by status or task when applicable, especially
  candidate routes, owner decisions, and stop conditions.
- The accepted product spec named by status or task.
- `../validation-matrix.md` for evidence category only, not command details.

## Outputs

- Candidate route summary.
- One recommended next NW, or an explicit park decision.
- Product-first task packet draft.
- Open owner decisions.

When the output would select future implementation, first use
`../product-journey-and-slice-sequencing.md` as the sequencing guide and
establish or cite the active Product Goal and representative journey portfolio.
After NW-168, the expected durable planning source is
`../../specifications/product/product-goal-and-representative-journeys.md`.
Do not jump directly from a completed technical proof to another implementation
slice.

## Must Not

- Modify runtime code.
- Change architecture, contracts, platform specs, or validation policy.
- Mark candidate routes accepted.
- Decide real-production approval.
- Bypass the validation matrix.
- Act as steward unless routing or reconciliation is explicitly selected.

## Mini Prompt Template

```text
Using status, the sequencing guide, and the active Product Goal and
representative journey portfolio when present, propose one next NW. Use a
Product Candidate handoff only when it is named or being created.
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
