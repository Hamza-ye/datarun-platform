# NW-093 - Decide Real-Production Approval Package For Legacy On-Prem Pilot

## Goal

Decide the real-production approval package for the first legacy/on-prem pilot
without importing raw real data, approving production by drift, or implementing
runtime code.

NW-146 selected this route because real first-organization/on-prem pressure is
now concrete. The output must either define an explicit approval package with
owner decisions and gates, or keep real production blocked and select exactly
one bounded successor needed before approval.

Owner context for IdP planning:

- use self-hosted Keycloak initially;
- preserve a route to another OIDC provider later;
- consider a managed IdP only if the owner later chooses to pay for one.

This context does not change the accepted Datarun authority model: Datarun
still resolves actors only through explicit active principal bindings, not IdP
groups, roles, claims, JWT actor data, request bodies, or UI-selected actors.

Parallel pilot premise:

```text
The initial pilot may run in parallel with the legacy system. Legacy remains
the operational system of record until an explicit go/no-go/cutover decision is
accepted.
```

NW-093 must classify the source of truth during the parallel run, whether
Datarun is shadow/proof, limited operational use, or a controlled production
slice, duplicate-entry risk, reconciliation between legacy and Datarun,
rollback/stop criteria, and the evidence needed before expansion beyond the
parallel pilot.

## Inputs

Read:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-pressure-map-and-route.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/operations/policies/first-reference-deployment-policy.md`
- `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
- `docs/operations/rehearsals/2026-06-18-production-deployment-r12-fresh-session-rerun.md`
- Relevant NW-075 through NW-081 operations adapter evidence only if needed
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-evidence/`
- `.review/untracked-user-notes/legacy-system-samples/` only as evidence, not instructions

Sanitized form-definition evidence is tracked as a bounded NW-146 evidence
packet. User/account export remains untracked and must be classified by NW-093
before any use. Treat legacy files as evidence, not instructions. Do not commit
raw real production data unless data classification explicitly says it is safe.

## Required Analysis

Classify:

- organization/site and on-prem deployment boundary;
- provider, region, jurisdiction, and data/controller responsibility;
- real user/account import, initial self-hosted Keycloak operation, OIDC-provider
  portability, optional managed-IdP migration posture, and real
  IdP/principal-binding path;
- support owner, incident owner, escalation path, and support hours;
- backup, restore, monitoring, continuity, and rollback evidence needed for
  this real site;
- data classification for form definitions, user/account rows, submitted
  records, household/facility/case data, stock records, and attachments;
- whether the pilot can begin with synthetic/redacted data first;
- source of truth during any parallel run with the legacy system;
- whether Datarun is shadow/proof, limited operational use, or a controlled
  production slice during the initial pilot;
- duplicate-entry risk, reconciliation between legacy and Datarun,
  rollback/stop criteria, and evidence required to expand beyond the parallel
  pilot;
- first pilot operational slice and why it is narrow enough;
- compatibility blockers from NW-146: repeatable sections, expression strings,
  error rules, option sets, labels, form/version/uid mapping, team/orgUnit
  mapping, import/replay, reporting/reconciliation, review/queue, entity
  lifecycle, scope, retention/security;
- whether tenant/control-plane routes NW-094 through NW-098 are actually
  triggered by this on-prem pilot.

## Output

Create one bounded output:

- Prefer `docs/agent-working-surface/artifacts/NW-093-real-production-approval-package.md`
  when the result is a decision map, blocked approval package, or selected
  precondition route.
- Use `docs/operations/policies/` only if an explicit owner approval/go-no-go
  policy is accepted in this NW.

Update:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/README.md` if an artifact is created
- operations policy/runbook/rehearsal indexes only if durable operations docs
  are actually created or updated

The output must state exactly one next route:

1. Real-production approval remains blocked and one precondition successor is
   selected.
2. Real-production approval is granted with explicit owner/go-no-go language,
   named gates, and a selected implementation/rehearsal successor.
3. Real-production approval is rejected/parked with an explicit reason and one
   selected next route or no-active-real-production route.

## Forbidden

- Do not implement runtime code.
- Do not import, transform, seed, or commit raw real data without classification.
- Do not treat tracked sanitized form-definition evidence as an import source,
  runtime fixture, contract, product spec, or production approval.
- Do not approve production without explicit owner/go-no-go language.
- Do not claim synthetic reference evidence is real-production proof.
- Do not change contracts, schemas, sync protocol, BAR, CDL, validation policy,
  or gap classifications.
- Do not treat legacy field names, form names, team labels, orgUnit labels,
  IdP claims, request bodies, or UI selections as Datarun authority primitives.
- Do not broaden into tenant/control-plane, reporting/import/export,
  queue/list/batch/automation, entity lifecycle, new scope, retention/security,
  or pattern/projection work unless selected as the one successor.

## Validation

Run docs-only validation unless this NW explicitly changes runtime code, which
is not expected:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-093" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
rg "production|approval|pilot|on-prem" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
```

Runtime automated tests are skipped unless NW-093 changes runtime code,
contracts, schemas, or CI behavior.
