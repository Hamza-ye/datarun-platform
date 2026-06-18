# NW-082 Agent Prompt: Decide Product Candidate 1 Boundary And Multi-Tenancy Routing

You are working in `/home/hamza/datarun-platform`.

## Goal

Produce the bounded Product Candidate 1 milestone decision and routing plan
before any implementation begins.

Exit target:

```text
Datarun has a concrete, backlog-visible Product Candidate 1 milestone route
that separates accepted standing, non-binding/deferred standing, multi-tenancy
decisions, implementation successors, evidence gates, operations gates, risks,
and stop conditions.
```

This is decision, routing, and specification-selection work. It is not an
implementation row and must not change runtime code, database schemas, API
contracts, event envelopes, sync behavior, auth behavior, mobile local storage,
web/admin UI, or deployment approval.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/checkpoints/checkpoint-2026-06-18-post-reference-deployment-rehearsal.md`
6. `docs/agent-working-surface/README.md`
7. `docs/agent-working-surface/platform-next-work-backlog.md` row NW-082, the
   Post-NW-068 trigger map, and rows NW-044 through NW-054, NW-067 through
   NW-081, and NW-071 through NW-073
8. `docs/agent-working-surface/baseline-acceptance-register.md`
9. `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`
10. `docs/agent-working-surface/artifacts/product-admin-surface-forward-plan.md`
11. `docs/workshops/first-deployment/README.md`
12. `docs/agent-working-surface/artifacts/2026-06-18-multi-tenancy-architecture-analysis.md`
13. `docs/agent-working-surface/decision-anchor-layer/README.md`
14. `docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md` better start through it's small index `docs/agent-working-surface/decision-anchor-layer/README.md` then slice the long `architecture-decision-anchors.md` with `scripts/query_cdl.py` (see below).
15. `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
16. `docs/specifications/README.md`
17. `docs/specifications/product/README.md`
18. `docs/specifications/platform/README.md`
19. `docs/specifications/platform/configuration-package-and-shapes.md`
20. `docs/specifications/platform/expression-language.md`
21. `docs/specifications/platform/assignment-scope-and-administration.md`
22. `docs/specifications/platform/production-auth-principal-binding.md`
23. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
24. `docs/operations/README.md`
25. `docs/operations/policies/first-reference-deployment-policy.md`
26. `docs/operations/runbooks/production-deployment-runbook.md`
27. `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
28. `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
29. `docs/operations/rehearsals/2026-06-18-production-deployment-r12-fresh-session-rerun.md`
30. `docs/implementation/module-interfaces.md`
31. Relevant contracts:
    - `contracts/envelope.schema.json`
    - `contracts/sync-protocol.md`
    - `contracts/config-package.schema.json`
    - `contracts/shape-format.schema.json`
    - `contracts/flag-catalog.md`
    - `contracts/shapes/assignment_created.schema.json`
    - `contracts/shapes/assignment_ended.schema.json`

Use CDL slices only for exact authority checks exposed by the routing work,
especially event/envelope closure, assignment-derived access, sync/access
equivalence, authority projection, fixed scope mechanisms, configuration
boundaries, sensitivity, and projection rebuildability. Use
`scripts/query_cdl.py`; do not read or rewrite the whole CDL.

## Questions To Answer

1. What is the current accepted standing for Product Candidate 1 across mobile,
   server/runtime, web/admin, auth, configuration/assignment administration,
   and deployment evidence?
2. What standing is non-binding, deferred, or future-decision and must not be
   silently absorbed into Product Candidate 1?
3. What is the first Product Candidate 1 milestone boundary, and what is
   explicitly out of scope?
4. Which decisions must land before implementation, especially for
   multi-tenancy, production web admin implementation, mobile login/session,
   retention/security, and real production approval?
5. Which specifications must land before implementation, and what is their
   correct durable home?
6. Which implementation slices can safely follow after those decisions/specs,
   in dependency order?
7. What tests, evidence gates, and deployment/operations gates are required
   before each milestone can be accepted?
8. What must remain deferred: reporting/export, conflict batch handling,
   resolver reassignment, auto-resolution, entity lifecycle, new scope
   mechanisms, retention/security, mobile OIDC login, online principal-binding
   admin, cross-tenant collaboration, pooled multi-tenant storage, and real
   production approval?

## Multi-Tenancy Starting Point

Treat
`docs/agent-working-surface/artifacts/2026-06-18-multi-tenancy-architecture-analysis.md`
as a non-binding routing input.

Preferred starting posture unless repository evidence contradicts it:

- tenant is the hard security and data-isolation boundary;
- organization may be the customer-facing name for tenant at first;
- workspace is the operational container for scenarios, workflows, config, and
  data;
- org units, teams, departments, and locations are not tenants;
- assignment scope is authorization inside a workspace, not tenant isolation;
- cross-tenant collaboration is unsupported by default unless separately
  routed;
- first implementation posture should prefer managed single-tenant deployment
  or bridge isolation before pooled multi-tenant storage;
- no tenant field is added to the event envelope in the first model.

## Expected Output

Produce the full Product Candidate 1 routing plan with these sections:

1. Executive summary
2. Authority and standing review
3. Product Candidate 1 scope
4. Multi-tenancy routing
5. Work sequence
6. Risk-managed routing
7. Recommended first actionable NW
8. Follow-up NW candidates
9. Files likely to change
10. Final recommendation

The work sequence must be a concrete ordered table. Each item must include:

- ID placeholder;
- title;
- type;
- priority;
- depends_on;
- required input files;
- intended durable output home;
- implementation impact;
- verification/evidence;
- successor tasks;
- stop conditions.

The risk table must include:

- risk;
- severity;
- trigger;
- mitigation;
- required route;
- whether it blocks Product Candidate 1.

## Durable Output Home

Choose the durable output home by classification:

- If the result is a non-binding milestone routing plan, create:
  `docs/agent-working-surface/artifacts/NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md`
- If the result selects accepted user-visible Product Candidate 1 behavior,
  create or route a product specification under `docs/specifications/product/`
  and index it from `docs/specifications/product/README.md`.
- If the result selects accepted platform behavior inside current
  architecture, create or route a platform specification under
  `docs/specifications/platform/` and index it from
  `docs/specifications/platform/README.md`.
- If the result changes process or wire contracts, route a `contracts/`
  successor explicitly.
- If the result changes structural architecture authority, route a CDL
  successor or explicitly selected architecture decision artifact.
- If the result changes deployment-owner choices, support, approval,
  notification, backup/restore, rehearsal, or real-production gates, route
  operations policy/runbook/rehearsal successors under `docs/operations/`.

Do not leave binding product/platform behavior only in the NW row, a prompt,
or a non-binding artifact. Do not add large normative content to BAR or the NW
backlog. Those registers should trace status, dependencies, evidence, and
exit conditions.

## Guardrails

- Do not treat non-binding artifacts as accepted architecture or product
  scope.
- Do not add tenant fields to event envelopes, new envelope fields, or new
  envelope `type` values.
- Do not use location hierarchy, org unit membership, IdP claims,
  UI-selected organization, or assignment scope as the tenant isolation
  boundary.
- Do not collapse tenant admin, workspace admin, assignment admin, activity
  roles, resolver authority, and deployment owner authority into one generic
  admin role.
- Do not claim real production readiness from synthetic rehearsal evidence.
- Do not introduce online production binding-admin APIs/UI.
- Do not bundle admin auth, web admin UX, mobile UX, multi-tenancy,
  reporting, retention/security, and deployment production approval into one
  implementation task.
- Do not create new first-deployment workshop stages, role packets, gate
  reviews, or prerequisite packets.
- Do not silently absorb NW-071, NW-072, NW-073, NW-044, NW-053, NW-054, or
  other deferred/future-decision rows into Product Candidate 1.

Stop and report if the work requires product owner input to accept a milestone
boundary, customer-facing terminology, real-production claim, tenant
commercial/account model, data residency posture, or support/continuity
commitment.

## Verification

Run:

```bash
git diff --check
```

Also verify:

- any new durable specification is indexed from the nearest README;
- any non-binding artifact clearly states its status and authority boundary;
- the NW-082 row links the prompt and selected output;
- no runtime code, schemas, fixtures, migrations, contracts, BAR, CDL, status
  acceptance, operations evidence, or unrelated docs changed unless the
  routing explicitly selected and justified that output;
- existing future-decision rows keep their separate standing unless the
  Product Candidate 1 plan promotes them as successors.

## Commit Flow

If commits are requested, use separate commits:

```text
docs(product): route product candidate 1 boundary
docs(product): decide product candidate 1 boundary
docs(status): accept product candidate 1 boundary
```

Use the NW trailer on commits owned by this route:

```text
NW: NW-082
```

Do not mark NW-082 accepted until the selected output exists, verification is
complete, and any required owner/product acceptance boundary is satisfied. Do
not start implementation successors until NW-082 is accepted or the needed
successor decision/spec row is separately selected.

## Stop And Report

Stop if the plan starts implementation, changes contracts, changes schemas,
claims production readiness, or makes multi-tenancy architecture binding
without a correctly routed durable output.
