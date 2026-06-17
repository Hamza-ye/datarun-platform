# NW-074 Agent Prompt: Drain Stale IDR-Era References

You are working in `/home/hamza/datarun-platform`.

## Goal

Audit and patch stale active references that would mislead future agents into
treating old IDR prose, phase notes, or implemented drift as the durable target
after accepted platform specs now exist.

This is documentation hygiene. It is not a new behavior extraction, not an
architecture decision, not implementation work, not a code semantics change, and
not a broad rewrite of historical provenance.

## Scope

NW-074 is selected only for surfaces whose durable behavior has already been
accepted:

- NW-068 configuration, expression, and config-package behavior:
  `docs/specifications/platform/expression-language.md`,
  `docs/specifications/platform/configuration-package-and-shapes.md`, and the
  narrow `/api/sync/config` contract note in `contracts/sync-protocol.md`;
- NW-069 assignment, scope, role-action, containment, and
  assignment-administration behavior:
  `docs/specifications/platform/assignment-scope-and-administration.md`;
- NW-070 production auth, principal binding, OIDC/JWKS, bearer-bound actor
  context, provisioning boundary, and group/claim non-authority:
  `docs/specifications/platform/production-auth-principal-binding.md`.

Do not broadly clean up shared-device, conflict/flag, or pattern/projection
references before NW-071, NW-072, or NW-073 are accepted. You may route those as
residual findings only.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/agent-working-surface/README.md`
6. `docs/agent-working-surface/prompts/README.md`
7. `docs/agent-working-surface/platform-next-work-backlog.md` NW-074 and the
   Post-NW-068 trigger map
8. `docs/agent-working-surface/artifacts/architecture-classification-drift-audit.md`
9. `docs/agent-working-surface/artifacts/idr-durable-surface-routing-audit.md`
10. `docs/specifications/platform/README.md`
11. `docs/specifications/platform/expression-language.md`
12. `docs/specifications/platform/configuration-package-and-shapes.md`
13. `docs/specifications/platform/assignment-scope-and-administration.md`
14. `docs/specifications/platform/production-auth-principal-binding.md`
15. `contracts/sync-protocol.md`
16. `docs/implementation/module-interfaces.md`
17. `docs/decisions/README.md`
18. `docs/decisions/INDEX.md`
19. Relevant IDRs only when a scan shows a material stale reference in an
    accepted NW-068 through NW-070 area:
    - `docs/decisions/idr-013-assignment-payload.md`
    - `docs/decisions/idr-014-materialized-path-locations.md`
    - `docs/decisions/idr-015-scope-filtered-sync-query.md`
    - `docs/decisions/idr-016-actor-token-table.md`
    - `docs/decisions/idr-017-shape-storage.md`
    - `docs/decisions/idr-018-expression-grammar.md`
    - `docs/decisions/idr-019-config-package.md`
    - `docs/decisions/idr-021-role-action-enforcement-model.md`
    - `docs/decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md`
    - `docs/decisions/idr-024-multi-axis-assignment-containment.md`
    - `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
    - `docs/decisions/idr-028-production-principal-binding-administration.md`
    - `docs/decisions/idr-029-assignment-admin-command-capability.md`

Use CDL slices only if a scanned reference appears to conflict with accepted
architecture authority. Do not read or rewrite the whole CDL.

## Scan

Use `rg` and inspect context before editing. At minimum, scan active and
historical docs for:

```bash
rg -n "Use IDR|use IDR|IDR-0(13|14|15|16|17|18|19|21|23|24|27|28|29)|contracts/expression\\.schema\\.json|decision boundary|durable target|parallel active specification|implementation provenance" docs
```

Also scan code and test comments for accepted NW-068 through NW-070 topics:

```bash
rg -n "IDR-0(13|14|15|16|17|18|19|21|23|24|27|28|29)|expression\\.schema|assignment_admin|OIDC|JWKS|principal binding|scope-filtered|role-action" server mobile contracts
```

Do not mechanically replace every match. Classify each candidate first.

## Classification

Patch only these:

- active routers, indexes, status, backlog, or module-interface text that still
  tells future agents to use IDR prose as the normative target where an accepted
  platform spec now exists;
- old IDR or phase text with a concrete false trace that will likely mislead a
  routed worker, such as a reference to a non-existent durable artifact;
- code or test comments that contradict the accepted durable spec or imply an
  old IDR is the current behavior authority;
- backlinks or indexes where accepted durable specs are not discoverable from a
  nearby active reference.

Leave these alone unless the stale reference is materially misleading:

- historical IDR rationale and original decision text;
- phase files recording what was implemented at that time;
- accepted evidence rows and old NW rows that correctly cite IDRs as historical
  inputs or implementation provenance;
- docs whose purpose is explicitly non-authoritative audit/provenance;
- conflict/flag, pattern/projection, and shared-device surfaces whose durable
  specs are still NW-071 through NW-073 candidates.

## Expected Output

Make the smallest documentation-only patch that drains the stale reference risk.
Likely valid outputs include:

- active status/router wording that points to accepted platform specs before
  IDR provenance for NW-068 through NW-070 areas;
- module-interface wording that names the accepted platform specs or contracts
  for durable behavior while preserving implemented-boundary intent;
- decisions README/INDEX notes that classify relevant IDRs as historical inputs
  after extraction;
- minimal supersession/correction notes in old IDRs only when a direct false
  trace or active-authority ambiguity would otherwise remain visible;
- NW-074 backlog/status fold-forward after review accepts the cleanup.

Do not create a new durable specification. Do not edit the accepted platform
specs unless you find a broken link or a narrow trace correction.

## Guardrails

- Do not change runtime code, schema files, fixtures, tests, contracts, CDL,
  BAR, operations policy/runbooks/rehearsal records, or
  `docs/documentation-organization.md` / `docs/commit-workflow.md`.
- Do not rewrite old IDRs or phase files wholesale.
- Do not delete accepted evidence or historical provenance.
- Do not mark NW-071, NW-072, or NW-073 accepted.
- Do not promote shared-device retention/security, conflict automation,
  resolver reassignment, auto-resolution, pattern traversal/reporting,
  trigger execution, new scope mechanisms, online production binding-admin APIs,
  IdP group/claim authority, mobile authoritative rejection, or new
  envelope/type behavior.
- Stop and report if a needed cleanup appears to require changing accepted
  behavior, architecture authority, contracts, BAR standing, or documentation
  standards.

## Verification

Run:

```bash
git diff --check
```

Also verify:

- changed paths are documentation-only unless you found a code/test comment
  that is materially misleading;
- `docs/documentation-organization.md` and `docs/commit-workflow.md` have no
  diff;
- NW-071 through NW-073 remain candidates;
- accepted specs remain indexed in `docs/specifications/platform/README.md`;
- no runtime behavior, contract semantics, schema, fixture, test expectation,
  BAR, CDL, or operations standing changed.

## Commit Flow

Use separate commits if commits are requested:

1. Route selected work:
   `docs(working-surface): route stale reference cleanup`
2. Land hygiene patch:
   `docs(hygiene): drain stale IDR-era references`
3. Accept standing if the cleanup is reviewed and complete:
   `docs(status): accept stale reference cleanup`

Include:

```text
NW: NW-074
```

Do not mark NW-074 accepted until the cleanup patch is reviewed, verified, and
folded forward.

## Final Report

Return:

- changed paths;
- stale references patched and why;
- findings deliberately left as residual routes for NW-071 through NW-073 or
  future decisions;
- verification command results;
- stop conditions, if any.
