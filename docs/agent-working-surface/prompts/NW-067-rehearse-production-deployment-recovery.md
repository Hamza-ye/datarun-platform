# NW-067 Agent Prompt: Rehearse Production Deployment And Recovery

You are working in `/home/hamza/datarun-platform`.

## Goal

Execute the accepted NW-066 runbook in a clean reference environment and retain
evidence for deployment, restore, upgrade, failure response, rotation, alert,
and operator handoff.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. Accepted NW-064 policy
5. Accepted NW-066 runbook and rehearsal plan
6. NW-065 release/deployment assets and implementation evidence
7. NW-067 backlog row

## Durable Output

Create and index a dated rehearsal record:

```text
docs/operations/rehearsals/YYYY-MM-DD-production-deployment-reference-environment.md
```

Execute every required NW-063 rehearsal scenario. Record environment, commit
and image digest, operator role, exact procedures, observed results, timings,
failures, deviations, evidence locations, cleanup, and follow-up NW rows.

## Acceptance

The rehearsal passes only if:

- a clean image installs with all contract resources;
- production validation rejects development defaults;
- migration and provisioning complete through supported tooling;
- device auth/config/push/pull smoke tests preserve accepted authority and
  sync behavior;
- backup restores into a clean environment within accepted RPO/RTO;
- upgrade and injected failure paths follow the accepted recovery posture;
- credential/JWKS rotation, alert delivery, incident triage, and operator
  handoff succeed;
- secrets and sensitive data are absent from committed evidence.

## Guardrails

- Use synthetic/non-sensitive data.
- Do not repair failures with ad hoc schema edits or development admin
  surfaces.
- Do not weaken policy or tests to obtain a pass.
- A partial or failed rehearsal must remain recorded as partial/failed and
  route corrections to separate NW items.

## Verification

Run all runbook checks, `git diff --check`, and inspect the evidence record for
secret leakage before commit.

## Commit Flow

Commit reusable runbook corrections separately from the dated evidence when
they are independently reviewable. Use:

```text
test(ops): rehearse production deployment recovery

NW: NW-067
```

Accept NW-067 only after the record states a truthful final result.

## Stop And Report

Stop and fail the rehearsal on contract-resource omission, unsafe migration
state, failed restore, authority drift, secret leakage, or unmet accepted
RPO/RTO.
