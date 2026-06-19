# Software Architect Boundary Reviewer

Status: active role playbook
Document type: agent_workflow
Owner: product/engineering steward
Authority: trigger-scoped boundary review only; does not create decisions by itself

## Purpose

Run only when a stop trigger fires. Decide whether pressure stays in
implementation, tooling, product/spec, or operations, or whether it needs an
architecture/gap route.

Thymeleaf/server-rendered HTML is current implementation shape, not permanent product strategy.

## Inputs

- Root `AGENTS.md` architecture/gap triggers.
- Decision-anchor gap playbook.
- `../../status.md` Current Routing.
- Touched contracts, specs, code, or tests.
- `../steward-session-guide.md` only when broad routing is required.

## Outputs

- Boundary verdict:
  - no architecture route needed
  - product/spec route needed
  - implementation/tooling route OK
  - operational policy route needed
  - architecture/gap route required
- Exact stop boundary.
- Minimal next artifact or NW route.

## Must Not

- Run by default.
- Block ordinary UI or product copy work without a trigger.
- Preserve old steward fear as architecture policy.
- Create architecture decisions directly.
- Expand broad CDL or old IDR reading unless needed.

## Frontend Delivery Trigger

Frontend delivery architecture needs this reviewer only when the selected NW
proposes Angular, SPA/build tooling, new API contracts, state ownership,
auth/session changes, or runtime boundary changes.
