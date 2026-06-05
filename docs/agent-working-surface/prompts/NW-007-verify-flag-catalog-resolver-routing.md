# NW-007 Agent Prompt: Verify Flag Catalog And Resolver Routing

You are working in `/home/hamza/datarun-platform`.

## Goal

Verify BAR-006 and move it to `baseline_accepted` only if fresh code/runtime evidence proves the flag catalog and resolver-routing baseline.

The acceptance target:

```text
Each emitted flag has a designated_resolver.
Resolvability is platform-owned and not changed by severity overrides.
Only a conflict_resolved/v1 authored by the exact designated_resolver is canonical.
```

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/implementation/module-interfaces.md`
   - Read the `server/integrity` and contracts guard references.
4. `docs/agent-working-surface/README.md`
5. `docs/agent-working-surface/architecture-rationale-and-routing-companion.md`
6. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-006 and related rows BAR-005, BAR-013 only.
7. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-007 only.
8. `contracts/flag-catalog.md`
9. `contracts/shapes/conflict_detected.schema.json`
10. `contracts/shapes/conflict_resolved.schema.json`
11. `docs/decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md`
12. Use `scripts/query_cdl.py` only for CDL-015, CDL-028, and CDL-054.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/integrity/FlagCatalog.java
server/src/main/java/dev/datarun/server/integrity/ResolverRef.java
server/src/main/java/dev/datarun/server/integrity/ResolverRoutingService.java
server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java
server/src/main/java/dev/datarun/server/integrity/DomainUniquenessDetector.java
server/src/main/java/dev/datarun/server/integrity/TransitionViolationDetector.java
server/src/main/java/dev/datarun/server/integrity/ConflictResolutionService.java
server/src/main/java/dev/datarun/server/integrity/ConflictController.java
server/src/test/java/dev/datarun/server/integrity/FlagCatalogTest.java
server/src/test/java/dev/datarun/server/integrity/ConflictResolutionIntegrationTest.java
server/src/test/java/dev/datarun/server/integrity/ConflictDetectorIntegrationTest.java
server/src/test/java/dev/datarun/server/integrity/AuthFlagIntegrationTest.java
server/src/test/java/dev/datarun/server/integrity/DomainUniquenessIntegrationTest.java
server/src/test/java/dev/datarun/server/integrity/TransitionViolationIntegrationTest.java
```

## Verification Scope

Prove these points, by existing tests or focused new tests:

1. `FlagCatalog` exposes only known active categories and fixed resolvability.
2. Deployment severity overrides do not change fixed resolvability.
3. `conflict_detected/v1` payload validation requires `flag_category` and `designated_resolver`.
4. Runtime emitted flags include `designated_resolver` for:
   - `concurrent_state_change`
   - `stale_reference`
   - `identity_conflict`
   - `scope_violation`
   - `temporal_authority_expired`
   - `role_stale`
   - `domain_uniqueness_violation`
   - `transition_violation`
5. Existing unresolved flags for a source event preserve the first unresolved `designated_resolver`.
6. Resolution is canonical only when the resolution actor exactly matches the original flag's `designated_resolver`.
7. A non-designated resolution event is accepted for audit, does not clear the original flag, and emits a deterministic `scope_violation` against the unauthorized resolution event.
8. `auto_eligible` does not mean auto-resolution is implemented or that the system actor is the resolver without an explicit policy.

## Expected Work

Start with code inspection and existing targeted tests. If coverage is missing but behavior is correct, add focused tests in the existing test classes. If behavior is wrong, either make a narrowly scoped fix in the integrity module or stop and report if the fix would change architecture, contracts, resolver semantics, or deferred surfaces.

If verification passes:

- Update BAR-006 in `docs/agent-working-surface/baseline-acceptance-register.md` to `baseline_accepted`.
- Attach the exact command, date, and test classes in the BAR-006 code/runtime anchor.
- Mark NW-007 `accepted` in `docs/agent-working-surface/platform-next-work-backlog.md`.

If verification fails:

- Leave BAR-006 as `baseline_candidate`.
- Leave or move NW-007 to `blocked` only if the failure prevents verification from continuing.
- Add a precise follow-up backlog row with the failing behavior, file/test anchor, and exit condition.

## Targeted Tests

Start the test database:

```bash
docker compose -f docker-compose.test.yml up -d test-db
```

Run:

```bash
cd server
./mvnw -Dtest=FlagCatalogTest,ConflictResolutionIntegrationTest,ConflictDetectorIntegrationTest,AuthFlagIntegrationTest,DomainUniquenessIntegrationTest,TransitionViolationIntegrationTest test
```

If you add a focused test class, include it in the same targeted command. Do not run full Maven or Flutter suites unless the changes broaden beyond this integrity boundary.

## Guardrails

- Do not add flag categories.
- Do not change resolver semantics to make tests pass unless the existing implementation contradicts IDR-026/CDL.
- Do not implement auto-resolution.
- Do not implement resolver reassignment.
- Do not add production OIDC/JWT/group/claim authority.
- Do not change event envelope fields or `type` vocabulary.
- Do not change `contracts/flag-catalog.md` or platform payload schemas unless you first prove they are stale against the CDL and stop to report the drift.

## Commit Boundary

Use one commit if the slice lands:

```text
test(integrity): verify flag resolver baseline
```

The commit may include focused integrity tests plus BAR/backlog updates. It should not include unrelated scenario, sync, mobile, or architecture edits.

## Stop And Report

Stop and report if:

- IDR-026, CDL rows, contracts, and runtime code disagree.
- Acceptance would require auto-resolution, resolver reassignment, new flag categories, or envelope changes.
- Existing tests pass but do not actually prove exact resolver equality.
- A failure points to broader authority/sync behavior outside the integrity module.
