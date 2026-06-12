# Workshops

Status: active planning surface index

This directory holds human workshop, candidate, and task-packet planning
artifacts. It is the home for product/spec/evidence coordination that should
not live in `docs/reviews/`.

Workshop artifacts are not architecture authority and do not authorize
implementation by themselves. Use them with the normal source order:

1. CDL and contracts for authority.
2. `docs/agent-working-surface/` for current standing, BAR/NW evidence, and
   routing.
3. Workshop files for product, UX, role, evidence, and dispatch control.
4. Scenario/user-fit packets as product/problem evidence only.

## Folder Pattern

Use one folder per workshop or candidate lane:

```txt
docs/workshops/<workshop-or-candidate>/
  README.md
  readiness-checklist.md
  control.md
  role-packets.md
  stage-*.md
  task-packets/
    <packet-id>-*.md
```

Use `task-packets/` for prepared packets, decision records, and future bounded
agent packet drafts. Do not put implementation prompts in this directory until
the packet has authority, files, tests, stop conditions, and a commit boundary.

## Active Workshops

| Workshop | Role |
|---|---|
| [first-deployment](first-deployment/README.md) | First-deployment product/spec/evidence workshop and FD-PKT routing. |
