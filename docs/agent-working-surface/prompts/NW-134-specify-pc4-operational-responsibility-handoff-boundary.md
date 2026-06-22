# NW-134 - Specify PC4 Operational Responsibility Handoff Boundary

## Goal

Specify the prerequisite product/platform boundary for the Product Candidate 4
boundary selected by NW-133:

```text
Product Candidate 4 - Operational Responsibility Handoff
```

This is specification and routing work only. It must not implement PC4, change
runtime behavior, accept retention/security or offboarding promises, approve
real production, close PC2 live-lab proof debt, mutate the lab, or open
tenant/control-plane/runtime architecture work by drift.

## Why Now

NW-133 selected PC4 as one bounded operational responsibility handoff
candidate. That candidate touches handoff contents, successor-visible context,
late offline work treatment, subject-history repair, actor-local state, scoped
visibility, and user-facing caveats. Implementation must not start until those
boundaries are specified.

## Inputs

Read these surfaces first:

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/specifications/product/product-candidate-4-pm-handoff.md`
- `docs/specifications/product/product-candidate-1-pm-handoff.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/product/product-candidate-3-pm-handoff.md`
- `docs/specifications/platform/assignment-scope-and-administration.md`
- `docs/specifications/platform/shared-device-session-and-local-state.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/specifications/platform/scoped-operational-report-snapshot-boundary.md`
- `docs/scenarios/README.md`
- `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`
- `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`
- `docs/scenarios/27-logistics-distribution-composite.md`
- `docs/scenarios/19-offline-capture-and-sync.md`
- `docs/reviews/scenario-baseline-pressure-map.md`
- `docs/reviews/viability-closure-review.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/documentation-organization.md`

Use additional specific scenario files, product artifacts, code files, tests,
contracts, or platform specs only if the boundary comparison needs them. Do not
reopen broad architecture history unless a gap-routing trigger fires.

## Expected Output

Primary durable output:

```text
docs/specifications/platform/operational-responsibility-handoff-boundary.md
```

Use `docs/documentation-organization.md` and the platform specification index
requirements.

The output must include:

- exact PC4 handoff scope;
- accepted authority inputs and assignment boundaries;
- successor-visible current work and prior context;
- late offline work treatment after responsibility changes;
- subject-history, sync, projection, and actor-local state boundaries;
- unresolved attention, stale, incomplete, or unknown caveats;
- no-leakage and no-broad-audit/history expectations;
- product-safe wording guardrails;
- explicit non-goals;
- validation gates and known validation debt;
- Secure SDLC / security gates;
- reliability / operations gates;
- architecture escalation triggers;
- PC1, PC2, and PC3 parked synthetic-only standing;
- NW-093 production approval standing;
- NW-126 blocked PC2 live-lab proof debt standing;
- NW-044 broad reporting/import/export standing;
- NW-073 pattern registry/projection standing;
- NW-094 through NW-098 tenant/control-plane standing;
- not-selected-now table with triggers;
- whether a single implementation successor is ready, or why not.

Update:

- `docs/specifications/platform/README.md`
- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`

Update `docs/specifications/product/product-candidate-4-pm-handoff.md` only if
NW-134 changes the selected boundary wording or implementation readiness call.

## Candidate Boundaries To Compare

Compare only bounded versions of the PC4 handoff:

- assignment-only handoff context;
- successor start packet for current assigned work;
- late-offline-work handoff standing;
- supervisor/operator handoff caveat view;
- non-health logistics handoff example;
- campaign reassignment continuation example.

The selected boundary must remain one PC4 handoff, not a bundle.

## Guardrails

- Real users/data require NW-093 first.
- PC2 live browser proof requires NW-126 to be unblocked and accepted first.
- Retention/security/offboarding/local sensitivity promises require NW-054.
- New subject/query/custom scope or hidden sync/access scope requires NW-053.
- S06/entity lifecycle requires NW-021.
- Combined entity lifecycle, trigger execution, reporting, analytics, or broad
  future-surface expansion requires NW-036 or another explicitly selected
  bounded route.
- Broad reporting, structured export/import, warehouse, analytics, report API,
  report catalog, cadence, completion, completeness, or interoperability work
  requires NW-044.
- Queue/list/multi-item attention review routes through GAP-CONFLICT-01 before
  implementation.
- Conflict automation, batch workflow, resolver reassignment, or
  auto-resolution requires NW-045.
- Pattern traversal/reporting, pattern inventory expansion, workflow projection
  changes, or pattern API/product work requires NW-073.
- Managed control-plane or tenant-aware runtime work requires NW-094 through
  NW-098 as applicable.
- Handoff context must not become broad audit/history access, a reporting
  surface, retention/security policy, or a new sync scope.

## Forbidden Work

No runtime code, tests, contracts, schemas, migrations, CI, validation policy,
BAR, CDL, gap-register mutation, production approval, real users/data,
tenant-aware runtime implementation, reporting/import/export acceptance,
conflict automation/batch acceptance, conflict queue/list implementation,
pattern projection changes, entity lifecycle acceptance, retention/security
promises, live PC2 browser proof, lab mutation, or broad documentation cleanup.

Do not select NW-073 unless the chosen PC4 boundary actually depends on pattern
registry/projection behavior. Do not select NW-054 unless the chosen boundary
actually requires retention/security or offboarding promises.

## Validation

Run docs-only validation unless the selected output explicitly changes a
non-doc surface:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-134" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/specifications/platform/operational-responsibility-handoff-boundary.md
rg "Operational Responsibility Handoff" docs/specifications/platform/README.md
```

Add targeted grep/index checks required by the local convention.

Runtime tests should be skipped with rationale for docs-only specification
work.

## Stop Conditions

Stop and report if the work requires:

- choosing real users/data/provider/region/jurisdiction/support;
- changing production readiness standing;
- running PC2 live browser proof;
- touching the lab or retained PC2 environment state;
- making tenant-aware runtime decisions;
- changing auth/security authority;
- changing event semantics or repository architecture;
- changing validation gates;
- changing sync/access scope or introducing a new scope mechanism;
- turning normal sync or subject-history into broad audit/history pull;
- accepting retention/security/offboarding/local sensitivity promises;
- accepting broad reporting/import/export behavior;
- accepting conflict automation/batch behavior;
- accepting queue/list/multi-item review behavior;
- accepting pattern projection changes or pattern API/product behavior without
  NW-073;
- accepting entity lifecycle;
- accepting combined lifecycle/trigger/reporting/analytics expansion;
- reopening architecture decisions;
- using SPEC-* as active control.
