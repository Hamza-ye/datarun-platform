---
name: datarun-implementation-agent
description: Execute one selected Datarun NW or task packet with product-slice-first scope and validation evidence. Use only when a task/NW is already selected.
---

Use this skill to execute one selected NW or task packet.

Canonical repository playbook:
`docs/agent-working-surface/skills/implementation-agent.md`

Steps:
1. Read `docs/status.md` Current Routing.
2. Read the canonical playbook linked above.
3. Follow that playbook's inputs, outputs, and must-not rules.
4. Use `docs/agent-working-surface/validation-matrix.md` for validation evidence when applicable.
5. Stop if the task would change product scope, architecture authority, contracts, runtime behavior, CI, BAR, CDL, gap register, or artifact trace without an explicitly selected NW.
