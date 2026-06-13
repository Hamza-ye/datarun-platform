# NW-064 Agent Prompt: Define First Reference Deployment Operations Policy

You are working in `/home/hamza/datarun-platform`.

## Goal

Create and obtain deployment-owner acceptance for the operational policy that
governs the NW-063 single-application-host reference deployment.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/operations/README.md`
6. `docs/operations/policies/README.md`
7. `docs/agent-working-surface/artifacts/NW-063-production-deployment-ops-hardening-map.md`
8. NW-064 backlog row

## Durable Output

Create and index:

```text
docs/operations/policies/first-reference-deployment-policy.md
```

The policy must name owners and accepted values for:

- application hosting, DNS/TLS, PostgreSQL, secrets, monitoring, release, and
  incident/support responsibilities;
- environment separation and access;
- RPO, RTO, backup frequency, retention, encryption, off-site posture, restore
  authority, and disaster declaration;
- secret storage, least privilege, rotation, emergency revocation, and
  evidence handling;
- SLO/service hours, capacity thresholds, alert ownership, incident severity,
  support escalation, communication, and evidence retention;
- release approval, maintenance windows, pre-migration backup, application
  rollback eligibility, database restore authority, and forward-fix posture;
- compliance/data-classification constraints known now and the review trigger
  for a real deployment.

Use explicit `TBD - owner acceptance required` values while proposing options.
Do not mark the document accepted until the named deployment owner has selected
the values.

## Guardrails

- Do not change runtime behavior, infrastructure, contracts, schemas, auth,
  mobile behavior, or NW-054 standing.
- Do not claim that backup, restore, monitoring, or rotation works before
  rehearsal.
- Do not define production web admin authority or mobile OAuth/OIDC login.
- Do not use policy prose to override append-only event truth, assignment
  authority, principal-binding rules, or sync/access semantics.

## Verification

Run `git diff --check`. Verify the policy header and operations index follow
`docs/documentation-organization.md`.

## Commit Flow

If owner selection is pending:

```text
docs(ops): propose first deployment operations policy

NW: NW-064
```

Leave NW-064 `in_review`.

After owner acceptance:

```text
docs(status): accept first deployment operations policy

NW: NW-064
```

## Stop And Report

Stop if an owner cannot be assigned, concrete RPO/RTO or recovery authority
cannot be selected, or a requested policy would require changing platform
semantics.
