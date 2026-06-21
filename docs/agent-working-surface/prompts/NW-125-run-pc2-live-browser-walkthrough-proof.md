# NW-125 - Run PC2 Live Browser Walkthrough Proof

## Goal

Capture a live browser/manual walkthrough proof for Product Candidate 2 after
NW-124 explicitly recorded the PC2 proof beat as `NOT_RUN` for runtime/manual
inspection.

This is product-validation / owner-review evidence. It may inspect a runtime
environment manually, but it must not implement runtime code, build tooling,
schemas, CI, product specs, platform specs, BAR, CDL, or gap-register changes.

## User Value / Why Now

NW-124 honestly records that accepted PR #38 / NW-122 evidence supports the
one-item PC2 review loop, but no live browser/manual click-through happened.
The owner now requires that live walkthrough before considering PC2 closed.

The useful next evidence is therefore a bounded manual proof over one synthetic
PC2 item, not a patch to NW-124 and not a broad environment/build-tooling
cleanup.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/artifacts/NW-124-pc2-synthetic-walkthrough-proof.md`
- accepted PR #38 / NW-122 implementation and validation evidence from
  status/backlog
- `docs/agent-working-surface/validation-matrix.md`
- local/lab runtime setup files only as needed to attempt the walkthrough,
  such as `docker-compose.yml`, `server/Dockerfile`, `deploy/reference/`, and
  existing operations/runbook surfaces

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only if proof evidence creates pressure for reporting, queue/multi-item review,
automation, resolver reassignment, resolver eligibility broadening, real
production, retention/security, tenant/control-plane, contracts, sync/access,
or another gap-trigger surface.

Do not open broad architecture history unless a stop condition fires.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-125-pc2-live-browser-walkthrough-proof.md
```

The artifact should:

- state whether a live browser/manual runtime walkthrough actually happened;
- identify the environment used, such as local Docker, managed lab, or
  `NOT_RUN` with the precise blocker;
- name the synthetic/non-sensitive fixture or example used;
- preserve the one-item PC2 boundary from visible `Needs review` cue to one
  attention review page and one manual decision;
- classify each proof beat as `PASS`, `FRICTION`, `NOT_RUN`, or
  `OUT-OF-SCOPE`;
- record exact manual commands/actions that matter to the evidence, including
  runtime startup, login/session path, fixture/provisioning path, and cleanup
  when applicable;
- confirm exact designated-reviewer authority and no UI/body actor authority;
- confirm source work remains append-only and resolution is separate evidence;
- confirm no real users or real organizational data were used;
- confirm no real-production approval was granted;
- record any runtime/build/login/environment blocker as follow-up pressure,
  not as hidden proof success;
- recommend exactly one next route: park PC2, one small bounded polish, a
  standard implementation-tooling route, a bounded environment/provisioning
  route, NW-093 real-use preparation, or another explicitly bounded owner
  route.

## Docker / Build-Tooling Boundary

The Maven dependency-fetch failure observed during local
`docker compose -p datarun-nw124 up -d --build db server` is not PC2 product
behavior evidence by itself.

If Docker image build speed or repeated Maven downloads block NW-125, do not
modify `server/Dockerfile` inside NW-125. Record the blocker and recommend a
separate implementation-tooling NW.

An acceptable future tooling route may use a standard Docker BuildKit cache
mount for Maven's `.m2` repository if selected. That is build hygiene for the
server image build path, not bespoke PC2 product work, and it must prove that
the final runtime image behavior and packaged resources remain unchanged.

## Allowed Changes

- Add the NW-125 live walkthrough proof artifact.
- Update `docs/status.md`.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`.
- Update `docs/agent-working-surface/artifacts/README.md` after the artifact
  exists.
- Add one successor prompt only if NW-125 selects a concrete next route.
- Run local/lab runtime commands needed for manual evidence, using only
  synthetic/non-sensitive data.

## Forbidden Changes

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register changes, product spec changes, platform spec changes,
Dockerfile/build-tooling changes, real-production approval, reporting/export,
conflict queue/list workflow, batch review, resolver reassignment, automation,
resolver eligibility broadening, retention/security promises, entity
lifecycle, tenant/control-plane work, mobile code, or server/web-admin
implementation.

Do not use real users or real organizational data. If real-use preparation
becomes selected, stop and route through NW-093 before continuing.

## Acceptance Criteria

NW-125 is accepted only when:

- one PC2 live walkthrough proof artifact exists;
- the artifact states clearly whether live browser/manual inspection happened;
- each proof beat has clear standing;
- proof remains one work-linked attention item and does not become a queue,
  report, batch workflow, or broad conflict console;
- any environment/build/login blocker is recorded honestly rather than
  treated as proof;
- real-production standing remains blocked unless NW-093 is selected later;
- status/backlog reflect the resulting route and no active implementation gate
  is opened by accident;
- validation evidence follows the validation matrix for docs-only and
  manual/runtime evidence.

## Validation

Always run docs validation for the artifact/control-surface changes:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-125" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-125-pc2-live-browser-walkthrough-proof.md
grep -n "live browser" docs/agent-working-surface/artifacts/NW-125-pc2-live-browser-walkthrough-proof.md
```

If manual runtime inspection happens, the artifact must also record:

- exact startup/provisioning/login/walkthrough commands or actions;
- environment name and commit/image reference when available;
- result of opening the visible `Needs review` cue;
- result of one manual decision;
- cleanup state or intentionally retained local state.

Runtime automated tests are skipped unless NW-125 changes runtime code, which
is forbidden.

## Stop Conditions

Stop and report if the work requires:

- real users or real organizational data;
- real-production approval inside NW-125;
- runtime code, Dockerfile/build-tooling, schema, contract, migration, CI, or
  validation-policy changes;
- a new local dev-login shortcut, principal-binding shortcut, or auth bypass;
- reporting dashboards, exports, imports, warehouses, analytics, broad read
  APIs, completeness semantics, or drilldown;
- queue/list/multi-item review, broad conflict console, filters, batch review,
  resolver reassignment, automation, auto-resolution, or flag reporting;
- resolver eligibility broadening beyond exact stored `designated_resolver`
  equality for the opened item;
- retention/security/offboarding promises;
- entity lifecycle;
- tenant/control-plane work;
- BAR, CDL, or gap-register changes.

## Commit Boundary

Use a product-validation / owner-review evidence commit if NW-125 lands. Do not
combine NW-125 with runtime implementation, Dockerfile/build-tooling changes,
product-spec changes, platform-spec changes, validation-policy changes, CI
changes, BAR/CDL/gap-register updates, or unrelated cleanup.
