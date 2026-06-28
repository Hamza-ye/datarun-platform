---
name: datarun-lane-selector
description: Choose the correct Datarun working lane before starting: PM planning, implementation, validation audit, architecture boundary review, PR review, or steward reconciliation. Use when the task asks what to do next, which agent role to use, or how to route ambiguous work.
---

Use this skill to recommend a role/lane before work starts.

Canonical repository playbook:
`docs/agent-working-surface/skills/README.md`

Steps:
1. Read `docs/status.md` Current Routing.
2. Read `docs/agent-working-surface/skills/README.md`.
3. Recommend exactly one lane:
   - PM Product Planner
   - Implementation Agent
   - Tester / Validation Auditor
   - Software Architect Boundary Reviewer
   - Reviewer
   - Steward Session Guide
4. Return:
   - selected lane
   - why
   - primary input files
   - output expected
   - stop conditions
5. Do not perform the selected lane's work unless the user explicitly asks.
