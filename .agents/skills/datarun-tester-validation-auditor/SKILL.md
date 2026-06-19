---
name: datarun-tester-validation-auditor
description: Map a Datarun diff, PR, or acceptance claim to required validation evidence using the validation matrix. Use for missing gates, known-red gates, CI/manual evidence, and false-confidence risks.
---

Use this skill to map changed surfaces or claims to required validation evidence.

Canonical repository playbook:
`docs/agent-working-surface/skills/tester-validation-auditor.md`

Steps:
1. Read `docs/status.md` Current Routing.
2. Read the canonical playbook linked above.
3. Follow that playbook's inputs, outputs, and must-not rules.
4. Use `docs/agent-working-surface/validation-matrix.md` for validation evidence when applicable.
5. Stop if the task would change product scope, architecture authority, contracts, runtime behavior, CI, BAR, CDL, gap register, or artifact trace without an explicitly selected NW.
