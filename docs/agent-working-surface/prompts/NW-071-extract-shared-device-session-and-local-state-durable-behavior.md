# NW-071 Agent Prompt: Extract Shared-Device Session And Local-State Durable Behavior

You are working in `/home/hamza/datarun-platform`.

## Candidate 1 Reason

Product Candidate 1 is now moving from web-admin setup/assignment slices toward
external field-user mobile login, mobile operational vocabulary/navigation
polish, shared-device product language, tenant-lane visibility, conflict UI,
and reporting/export successors.

The selected next bounded route is NW-071 before NW-085. Downstream mobile
login/session work, shared-device UX, tenant-aware mobile partitioning, and
product copy now depend on durable platform authority for current shared-device
session and local-state behavior. They should not rely on IDR-era prose as the
normative source.

## Goal

Create an indexed platform specification for shared-device session and
local-state behavior.

This is a specification extraction task. It is not an implementation task, not
a mobile login task, not a mobile UX polish task, not a tenant-aware
partitioning task, not a retention/security policy task, and not an old-document
cleanup pass.

## Accepted Input

IDR-030, NW-052, and NW-055 behavior is already accepted and implemented as
provenance:

- single-active-actor shared-device sessions;
- drain-or-seal actor switching;
- `/api/auth/me` actor refresh or prior server-resolved actor-session resume;
- per-actor local mutable partitions;
- actor-scoped sync bookkeeping;
- immutable shared config blobs only as read-only non-actor data;
- sealed pending work staying in the original actor partition.

The durable authority for that behavior must move to a platform specification.
After this slice lands, IDR-030/NW-052/NW-055 should remain implementation
provenance and evidence, not the first normative route for future product,
mobile, tenant, or local-state copy work.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing and Recommended next move
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-071,
   NW-085, NW-054, NW-093, NW-094, NW-095, and accepted NW-052, NW-055,
   NW-084, NW-088, and NW-090
6. `docs/specifications/product/product-candidate-1.md`
7. `docs/agent-working-surface/artifacts/NW-049-access-exceptions-shared-device-scope-exploration.md`
8. `docs/decisions/idr-030-shared-device-session-lifecycle.md`
9. `docs/agent-working-surface/prompts/NW-052-decide-shared-device-session-lifecycle.md`
10. `docs/agent-working-surface/prompts/NW-055-implement-shared-device-actor-partitions.md`
11. `docs/implementation/module-interfaces.md` sections for Authenticated Actor
    Resolver, Sync Surfaces, Mobile Actor Session And Local Store, Event Store,
    Scope Resolver, and Config Packager
12. BAR-104 and BAR-106 in
    `docs/agent-working-surface/baseline-acceptance-register.md`
13. `contracts/sync-protocol.md` only to verify no sync protocol change is
    needed

Use CDL slices only if one of the sources above exposes a concrete authority
conflict. Do not read or rewrite the whole CDL.

## Expected Output

Create or update only the durable surfaces needed for this slice:

- platform specification under
  `docs/specifications/platform/shared-device-session-and-local-state.md`;
- `docs/specifications/platform/README.md` index entry;
- backlog/status fold-forward only after the platform specification lands,
  verification passes, and the route is accepted.

The platform spec must use the required document metadata from
`docs/documentation-organization.md` and should record accepted behavior for:

- single-active-actor sessions on a shared physical device;
- server-resolved actor session refresh and resume through `/api/auth/me` or a
  previously established server-resolved actor session;
- drain-or-seal switching away from actor A before actor B sees or creates
  actor-scoped local state;
- actor-local mutable partitions for events, pending push, projections,
  advisory state, local assignments, aliases, normal pull watermarks,
  `last_pull_watermark`, subject-history cursors, active/pending config state,
  and token/session material;
- actor-scoped server sync bookkeeping for normal pull/config observation;
- immutable shared config package blobs as read-only non-actor data only;
- sealed pending-work boundaries, including same-actor-only push/resume and no
  cross-actor reauthoring, transfer, visibility, or request signing;
- product-safe vocabulary for local state, such as saved locally, waiting to
  sync, active actor session, actor partition, sealed pending work, and shared
  setup package, without making retention/security promises.

## Required Decisions Inside The Slice

Decide and state explicitly:

- which behavior belongs in the new platform spec because contracts, module
  boundaries, product specs, and IDR provenance do not provide a durable enough
  normative route;
- which parts remain contract authority, especially existing sync protocol and
  event-envelope constraints;
- which parts remain implementation evidence only, such as exact SQLite table
  names, key names, Java/Dart helper structure, and migration mechanics;
- how IDR-030, NW-052, and NW-055 remain historical implementation provenance
  after extraction;
- whether any stale active references should receive a routing note now or
  should be left for a later hygiene row after NW-071 is accepted.

## Guardrails

- Do not change runtime code, mobile files, tests, JSON schemas, fixtures,
  contracts, BAR, CDL, operations evidence, product specs, old IDR text, or
  phase files in this slice.
- Do not implement mobile OIDC/login, refresh/logout, secure storage, token
  lifecycle, shared-device login UX, or mobile vocabulary/navigation polish.
  Coordinate NW-085 after NW-071 for mobile login/token lifecycle.
- Do not introduce tenant-aware runtime, storage, sync, config, local partition
  keys, pooled predicates, tenant/workspace selectors, or tenant-lane authority.
- Do not add envelope fields, envelope `type` values, sync protocol behavior,
  contract/schema changes, assignment payload fields, scope mechanisms,
  broad audit/history APIs, emergency override writes, resolver reassignment,
  auto-resolution, or mobile authoritative rejection.
- Do not decide expiry, device decommissioning, sealed-partition recovery,
  local encryption, refresh-token retention, token/session retention policy,
  no-local-retention views, erasure, redaction, sensitivity handling, local
  compliance retention, or real-production approval.
- Coordinate NW-054 if the spec needs retention/security claims beyond current
  actor partitioning and safe sealed pending-work boundaries.
- Do not use IdP groups, roles, resource claims, custom claims, JWT `actor_id`,
  UI-selected actors, product labels, or tenant/workspace copy as actor,
  assignment, sync, resolver, or local-state authority.

## Stop And Escalate

Stop and report instead of writing around the issue if the spec needs any of
these:

- tenant/workspace internals or tenant-aware local partition keys;
- IdP claim, group, role, or JWT `actor_id` actor authority;
- UI-selected actor authority or local-only writable offline actors;
- server event deletion, event mutation, erasure, or redaction;
- normal sync watermark rewrites, reset semantics, or audit/history pull;
- contract, schema, envelope, assignment payload, or sync protocol changes;
- retention/security promises beyond the current actor partition boundary;
- real-production approval, real users/data, provider/region/jurisdiction, or
  support commitment.

## Required Documentation Checks

- Follow `docs/documentation-organization.md` for the platform-spec home,
  metadata, semantic filename, and nearest README index entry.
- Confirm module-interface references are correct for Authenticated Actor
  Resolver, Sync Surfaces, Mobile Actor Session And Local Store, Event Store,
  Scope Resolver, and Config Packager.
- Add a stale IDR routing note only if an active surface would otherwise keep
  pointing implementers to IDR-030 as the normative home after the spec lands.
- Update backlog/status acceptance only after the durable spec is indexed,
  verified, and reviewed.

## Verification

Run:

```bash
git diff --check
```

Also verify that:

- the platform spec is indexed from `docs/specifications/platform/README.md`;
- `docs/documentation-organization.md` and `docs/commit-workflow.md` are not
  changed;
- links to IDR-030, NW-052, NW-055, product Candidate 1, module interfaces,
  contracts, and BAR evidence are valid by path search;
- no runtime code, mobile files, schema, fixture, test, BAR, CDL, operations
  evidence, contract, old IDR, phase file, or unrelated product spec diff
  exists.

## Commit Flow

Use separate commits for route, durable specification, and status acceptance if
commits are requested. Include:

```text
NW: NW-071
```

Do not mark NW-071 accepted until durable outputs and verification are
complete, reviewed, and folded forward.
