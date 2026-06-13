# NW-066 Agent Prompt: Write Production Deployment Runbook

You are working in `/home/hamza/datarun-platform`.

## Goal

Write the executable operator runbook and reusable rehearsal plan for the
accepted NW-063 reference target using only accepted NW-064 policy and tested
NW-065 tooling.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/operations/runbooks/README.md`
6. `docs/operations/rehearsals/README.md`
7. NW-063 artifact
8. Accepted NW-064 policy
9. NW-065 implementation evidence and exact deployment assets
10. NW-066 backlog row

## Durable Outputs

Create and index:

```text
docs/operations/runbooks/production-deployment-runbook.md
docs/operations/rehearsals/production-deployment-rehearsal-plan.md
```

Follow the complete runbook and rehearsal outline in NW-063. Every risky step
must name prerequisites, exact commands/procedure, expected observable result,
stop condition, recovery posture, and retained evidence.

The migration section must distinguish:

- forward-only Flyway migration;
- application rollback only when schema compatibility is proven;
- database restore from a consistent backup/PITR target;
- forward fix when neither rollback nor restore is the accepted response.

## Guardrails

- Do not document `/admin`, `/admin/config`, or `/admin/dev` as production
  procedures.
- Do not invent commands, dry-run behavior, health signals, backup guarantees,
  or rollback support not implemented and tested by NW-065.
- Do not place one-time rehearsal results in the reusable runbook.
- Do not claim mobile OAuth/OIDC login, production web admin auth, NW-054
  security behavior, Kubernetes, or managed-provider support.

## Verification

Run `git diff --check`. Dry-read every command against the exact NW-065 assets
and verify all documents are indexed with required metadata.

## Commit Flow

Use a docs outcome commit and a separate status acceptance commit if review
changes standing:

```text
docs(ops): write production deployment runbook

NW: NW-066
```

## Stop And Report

Stop if any required procedure relies on development surfaces, direct
undocumented database writes, missing tooling, or an unaccepted policy value.
