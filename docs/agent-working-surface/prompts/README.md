# Working-Surface Prompts

Status: execution-packet index

Prompts are bounded instructions for an agent. They are not durable decisions,
specifications, operational procedures, implementation evidence, or acceptance
evidence by themselves.

Every prompt should name:

- the owning NW row;
- goal and exit target;
- capped files to read;
- authority and guardrails;
- expected durable output path;
- forbidden work;
- verification;
- backlog/status updates;
- commit role/sequence and acceptance boundary;
- stop-and-report conditions.

When the work lands, the NW row must link the durable output and verification.
Non-binding exploration may remain under `artifacts/`. Binding decisions,
accepted platform behavior, operations procedures, and implementation must be
promoted to their owning long-lived surfaces.

Use `docs/documentation-organization.md` to select those surfaces. In
particular:

- product specs: `docs/specifications/product/`;
- platform specs: `docs/specifications/platform/`;
- policies: `docs/operations/policies/`;
- runbooks: `docs/operations/runbooks/`;
- rehearsal plans and records: `docs/operations/rehearsals/`.

Commit instructions must follow `docs/commit-workflow.md`. Do not prescribe one
commit when the task spans routing, decision/specification, implementation,
acceptance, or checkpoint state transitions.
