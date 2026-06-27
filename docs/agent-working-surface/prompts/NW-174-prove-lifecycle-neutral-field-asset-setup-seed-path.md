# NW-174 - Prove Lifecycle-Neutral Field-Asset Setup/Seed Path

Status: ready prompt
Document type: execution_packet
Owner: implementation agent
Source: NW-174 route selection after PR #76 / NW-173 acceptance; NW-172 product spec; NW-021 M2.1 decision-routing artifact
Authority: selected implementation/validation packet for the field-asset setup/seed proof only; creates no accepted lifecycle authority, candidate disposition, registry framework, import/export path, contract/schema/sync change, BAR/CDL/gap-register change, production cutover, real-user rollout, or real-data import/use by itself

## User-Visible Outcome

A setup owner can put a small initial set of known assets into the pilot using
accepted setup, provisioning, configuration, assignment, subject identity, and
scope mechanisms. A field user assigned to those assets can then find and select
only those authorized assets during capture.

This route exists because NW-173 proved lifecycle-neutral lookup and candidate
fallback behavior once authorized `field_asset` subjects are already present,
but did not prove how the pilot's initial known assets enter the system and
become authorized lookup choices.

## Selected Route

Pick route 1 from the M2 successor options: known-thing setup/seed path for
configured `field_asset`.

Route 2, candidate evidence review closure, is not selected because NW-173
already proved candidate evidence is review-visible through scoped geography
and activity. Select that route later only if NW-174 evidence shows reviewers
cannot inspect enough candidate evidence to choose a follow-up route.

Route 3, candidate promotion/rejection decision, is not selected because the
accepted evidence does not yet show the pilot needs disposition commands rather
than setup/seed proof and read-only candidate visibility.

## Files To Read

Read first:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-021,
  NW-171, NW-172, NW-173, and NW-174
- `docs/specifications/product/lifecycle-neutral-known-thing-lookup-and-candidate-capture.md`
- `docs/agent-working-surface/artifacts/NW-021-s06-known-things-lifecycle-decision-routing.md`
- `docs/agent-working-surface/validation-matrix.md`
- this prompt

Read accepted specs and code only as needed for touched surfaces:

- `docs/specifications/platform/configuration-package-and-shapes.md`
- `docs/specifications/platform/assignment-scope-and-administration.md`
- setup/provisioning/package material used by NW-171 field-check evidence
- server/mobile code and tests for subject identity, assignment subject lists,
  scoped sync/config, field-asset lookup, and configured-work evidence reads

Read nested `AGENTS.md` files before touching `server/`, `mobile/`,
`contracts/`, deployment, or operations surfaces under their scope.

Do not re-run the full NW-021 lifecycle analysis and do not read CDL/gap
surfaces unless a stop condition fires.

## Accepted Evidence To Preserve

Preserve the NW-173 boundary:

- mobile lookup lists only assigned `subject_list` assets for configured
  `field_asset` shapes;
- selected known assets use existing `subject_ref` type `subject`;
- failed lookup saves unpromoted candidate evidence on a generated
  submitted-record subject;
- candidate evidence payload intentionally omits the `field_asset` binding and
  does not become official lookup truth;
- server candidate `location_path` stamping is a review visibility projection
  only for the exact configured `field_asset` candidate fallback;
- reviewers see candidate evidence only through existing scoped
  geography/activity configured-work evidence;
- stale, offline, unavailable, and incomplete lookup states are marked
  honestly.

Preserve accepted setup and scope mechanisms. Prefer existing provisioning,
configuration package, assignment, subject identity, subject-location, and sync
paths over new runtime concepts.

## Required Work

Implement the smallest runtime or provisioning slice that proves the setup/seed
path for the pilot.

1. Identify the accepted setup/provisioning/configuration mechanism that creates
   the initial pilot `field_asset` subjects.
2. Seed or provision at least two lifecycle-neutral `field_asset` subjects with
   stable subject ids and product-safe display labels.
3. Connect the seeded assets to accepted scope mechanics, such as subject-list,
   activity, assignment, geography, and temporal standing, without inventing a
   new scope authority.
4. Use a configured activity/shape whose `subject_binding` is exactly
   `field_asset`.
5. Prove an assigned field user can find and select only authorized seeded
   assets.
6. Prove an out-of-scope actor cannot use lookup, sync, or review paths as a
   broad asset browser.
7. Prove selected-asset capture still survives save/sync as the selected
   existing `subject_ref`.
8. Prove failed-lookup candidate evidence remains unpromoted evidence only and
   does not enter known-asset lookup truth.
9. Show scoped reviewer visibility remains read-only evidence visibility, not
   promotion/rejection or lifecycle disposition.

If the existing runtime already supports the setup/seed path without code
changes, produce focused proof through provisioning material, tests, and status
evidence instead of adding code.

## Allowed Changes

Use only the narrowest changes needed for the selected proof:

- pilot setup/provisioning/package fixtures or seed material for configured
  `field_asset` subjects;
- assignment/bootstrap material needed to authorize those assets through
  accepted subject-list, activity, geography, and actor scope;
- focused server or mobile fixes only when current accepted mechanisms cannot
  execute the setup/seed proof as intended;
- focused tests for changed behavior;
- status/backlog acceptance updates after execution evidence exists.

## Forbidden Work

Do not implement or accept:

- full S06 lifecycle;
- candidate promotion or rejection;
- duplicate stewardship or duplicate resolution;
- merge/split UX;
- canonical lifecycle state;
- place lifecycle or place-like subject lifecycle;
- semantic `location` behavior;
- new `subject_ref` types;
- deployer-authored lifecycle state machines;
- new contracts, schemas, migrations, sync protocol behavior, event types, or
  envelope fields;
- assignment/scope expansion beyond accepted mechanisms;
- BAR/CDL/gap-register changes;
- broad registry browsing;
- registry framework;
- import/export path for known things;
- legacy account import, submitted-record replay, real-user rollout, real-data
  import/use, retention/security promise, Keycloak cutover hardening, or
  production cutover.

Do not treat `field_asset` as a platform primitive. It remains configured
product vocabulary over existing subject identity.

## Stop Conditions

Stop and route a narrow successor decision instead of continuing if the
setup/seed proof requires any of the following:

- a new source of subject authority or registry framework;
- import/export as the only way to seed known assets;
- a new assignment/scope mechanism or query-as-config authority;
- a new `subject_ref` type;
- contract, schema, event-envelope, sync protocol, or migration changes;
- semantic `location`, place lifecycle, lifecycle state, promotion/rejection,
  duplicate resolution, or merge/split behavior;
- review workflow changes beyond read-only scoped evidence visibility;
- BAR, CDL, or gap-register changes.

If no accepted setup/seed carrier can make initial `field_asset` subjects
visible through accepted assignment scope, stop and route the missing
field-asset setup/seed authority decision.

## Required Validation

Run the narrowest focused tests first, then full gates for touched surfaces from
`docs/agent-working-surface/validation-matrix.md`.

Expected focused proof:

- server/setup/sync test proving seeded assigned `field_asset` subjects are
  visible to the assigned actor and not visible to out-of-scope actors;
- mobile lookup test, if mobile is touched, proving seeded assigned assets are
  listed and selected asset save/sync preserves the selected existing
  `subject_ref`;
- negative test proving candidate evidence without `field_asset` does not
  become known-asset lookup truth;
- assertion or UI/read-surface proof that no promote/reject/lifecycle/duplicate
  commands appear.

Required docs/control validation for this route:

- `git diff --check`
- grep NW-174 in status, backlog, and this prompt

Runtime tests are skipped only if no runtime, test, provisioning, package,
server, mobile, contract, or deployment behavior changes.

## Successor Standing

This prompt selects no successor beyond NW-174 execution.

After NW-174, candidate evidence review closure may be selected only if the
proof shows reviewers cannot inspect enough evidence to choose a follow-up
route. Candidate promotion/rejection remains deferred until evidence shows the
pilot truly needs disposition commands.
