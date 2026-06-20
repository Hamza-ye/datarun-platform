# NW-110 - Define PC1 Product Journey Smoke

## Goal

Define one bounded, domain-neutral Product Candidate 1 smoke path that a PM or
reviewer can use to check the accepted setup-to-field-loop story before
selecting implementation polish.

The output is a PM-readable smoke-definition artifact, not code and not an
accepted product behavior change.

## User value / why now

The PC1 handoff shows that setup/config, assignment administration, mobile
login, mobile capture/sync/correction, and narrow freshness/attention language
exist as separate accepted or candidate surfaces. The next product step is to
make the smallest coherent proof path visible before choosing mobile polish,
setup polish, assignment polish, demo scripting, freshness/attention UI, or a
proof-target decision.

Use a deliberately domain-neutral synthetic PC1 walkthrough. Domain examples
may appear only as interchangeable validation fixtures for the same Datarun
concepts.

## Inputs

- `docs/status.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/specifications/product/product-candidate-1.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/prompts/README.md`
- `docs/commit-workflow.md`

Read only the accepted product spec, PM handoff, backlog/status rows, prompt
conventions, and validation guidance needed for this planning artifact. Do not
read CDL, old IDRs, broad architecture, phase history, or implementation
internals unless a stop condition fires.

## Allowed changes

- Create `docs/agent-working-surface/artifacts/NW-110-pc1-product-journey-smoke-definition.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md` only to
  record NW-110 progress or exit evidence.
- Update `docs/status.md` only for the active control panel and current routing
  after the smoke definition is complete or stopped.
- Update `docs/agent-working-surface/artifacts/README.md` only to add the
  NW-110 artifact trace row when the smoke-definition artifact lands.

## Forbidden changes

- Runtime code, tests, CI, contracts, schemas, migrations, mobile code, server
  code, product specs, platform specs, PM handoff, validation matrix, skills,
  AGENTS files, steward guide, BAR, CDL, or gap register.
- artifact trace changes except the single NW-110 artifact index row required
  when the smoke-definition artifact lands.
- Any product-scope acceptance, real-production approval, real-user/data claim,
  provider/region/jurisdiction/support decision, or managed control-plane work.
- Reporting dashboards, exports, imports, warehouses, aggregate analytics, broad
  read APIs, retention/security promises, entity lifecycle, conflict
  automation, batch resolution, resolver reassignment, tenant-aware runtime
  internals, new scopes, envelope fields/types, sync protocol changes, config
  schema changes, or new authority sources.

## Acceptance criteria

- The smoke definition names one domain-neutral synthetic setup-to-sync journey
  covering setup/config, assignment, mobile get-work/readiness, capture,
  correction, sync states, and latest-synced/freshness/attention wording.
- The smoke stays within accepted PC1 product language and explicit non-goals.
- The artifact identifies the user-visible steps, expected observations,
  existing accepted support, gaps that would become successor candidates, and
  validation evidence category.
- The artifact states that it is synthetic proof/planning evidence only and does
  not approve real production or accept runtime behavior.
- The backlog/status updates, if any, record docs-only validation and leave
  implementation selection to a later explicit NW.

## Validation

Docs-only/product-planning validation:

```bash
cd /home/hamza/datarun-platform
git status --short
git diff --check
grep -n "NW-110" docs/agent-working-surface/platform-next-work-backlog.md
grep -n "NW-110" docs/status.md
test -f docs/agent-working-surface/artifacts/NW-110-pc1-product-journey-smoke-definition.md
grep -n "Goal" docs/agent-working-surface/artifacts/NW-110-pc1-product-journey-smoke-definition.md
grep -n "Acceptance criteria" docs/agent-working-surface/artifacts/NW-110-pc1-product-journey-smoke-definition.md
grep -n "Stop conditions" docs/agent-working-surface/artifacts/NW-110-pc1-product-journey-smoke-definition.md
```

When the artifact lands, also validate:

```bash
grep -n "NW-110-pc1-product-journey-smoke-definition.md" docs/agent-working-surface/artifacts/README.md
```

Report skipped runtime-test rationale: this NW is planning/docs-only and does
not touch runtime behavior, contracts, mobile/server code, CI, or validation
policy.

## Stop conditions

Stop and report the exact decision needed if the work:

- needs Hamza or product ownership to choose a real domain, real pilot, real
  users/data, provider, region, jurisdiction, support path, or proof target;
- treats any domain example as product identity, platform vocabulary authority,
  architecture authority, or contract authority;
- changes product scope or accepted PC1 behavior;
- requires architecture/gap routing;
- requires reporting/export, retention/security, entity lifecycle, conflict
  automation, tenant/control-plane, contracts, runtime, CI, validation matrix,
  BAR, CDL, gap register, or artifact-trace changes;
- starts implementing code instead of defining the smoke path.

## Commit boundary

Expected commit:

```text
docs(product): define PC1 product journey smoke
NW: NW-110
```

Do not use an acceptance/checkpoint commit unless the NW exit condition is
actually complete and the status/backlog updates record the docs-only evidence.
