# Operations Runbooks

Status: active runbook index

Runbooks are repeatable, executable procedures for an operator working against
a named supported deployment target.

A runbook must be testable. It should not hide policy choices, rely on
undocumented operator knowledge, or describe unsupported rollback/recovery as
if it exists.

## Required Shape

- required metadata from
  [documentation-organization.md](../../documentation-organization.md);
- supported environment and version assumptions;
- prerequisites, permissions, inputs, and safety checks;
- ordered procedure;
- expected observable result after each risky stage;
- stop conditions and incident/escalation route;
- rollback, restore, or forward-fix posture;
- evidence to retain;
- linked policy and latest rehearsal evidence.

## Index

| Runbook | Status | Owner | Source NW | Last rehearsed | Supersedes |
|---|---|---|---|---|---|
| [Production deployment](production-deployment-runbook.md) | `accepted` | Hamza | NW-066; NW-067 amendment | Not yet | None |
