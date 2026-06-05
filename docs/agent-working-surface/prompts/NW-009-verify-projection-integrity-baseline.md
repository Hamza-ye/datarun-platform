# NW-009 Agent Prompt: Verify Projection And Integrity Baseline

You are working in `/home/hamza/datarun-platform`.

## Goal

Verify the server-side projection and integrity baseline across BAR-009, BAR-012, BAR-013, and the server/shared-fixture side of BAR-014.

Acceptance targets:

```text
BAR-009: identity merge/split and alias projection prove merge/split append events, no historical rewrite, rebuildable aliases, lifecycle checks, and raw-ref detection.
BAR-012: pattern registry and pattern-state projection prove platform-owned definitions, valid binding delivery, rebuildable projection, and unresolved-flag exclusion.
BAR-013: transition and domain-uniqueness detection prove accept-and-flag behavior, detector ordering, duplicate-basis exclusion, and legal accepted re-inclusion.
BAR-014: server/shared-fixture projection evidence is attached, but do not mark fully accepted unless mobile fixture evidence is also fresh.
```

Move a BAR row to `baseline_accepted` only when fresh code inspection or targeted runtime evidence supports that specific row. Partial acceptance is expected. NW-010 follows after this server projection slice is stable and should cover mobile selective retention, expressions, projection, pattern projection, and the mobile side of BAR-014.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/architecture-rationale-and-routing-companion.md`
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-009, BAR-012, BAR-013, BAR-014, and related accepted rows BAR-003, BAR-004, BAR-006, BAR-007.
6. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-009 and NW-010 only.
7. `docs/implementation/module-interfaces.md`
   - Read `Projection Engine`, `Identity Resolver`, `Conflict Detector`, `Pattern Registry`, and `Event Store` boundaries.
8. `docs/reviews/scenario-baseline-pressure-map.md`
   - Read only the safe progress call and rows for S21/S27 so you understand why broad scenario probes wait.
9. `contracts/flag-catalog.md`
10. `contracts/shapes/conflict_detected.schema.json`
11. `contracts/shapes/conflict_resolved.schema.json`
12. `contracts/shapes/subjects_merged.schema.json`
13. `contracts/shapes/subject_split.schema.json`
14. `contracts/pattern-definition.schema.json`
15. `contracts/patterns/*.json`
16. `contracts/fixtures/pattern-state-projection.json`
17. `contracts/fixtures/projection-equivalence.json`
18. `docs/decisions/idr-009-alias-table.md`
19. `docs/decisions/idr-020-pattern-state-machine-representation.md`
20. `docs/decisions/idr-022-flag-severity-and-domain-uniqueness.md`
21. `docs/decisions/idr-025-pattern-definition-contract-and-delivery.md`
22. `docs/decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md`
23. Use `scripts/query_cdl.py` only for CDL-002, CDL-003, CDL-004, CDL-022 through CDL-029, CDL-045, CDL-047 through CDL-051, and CDL-054 when an authority point needs clarification.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/identity/IdentityService.java
server/src/main/java/dev/datarun/server/identity/SubjectAliasProjection.java
server/src/main/java/dev/datarun/server/identity/IdentityLifecycleProjection.java
server/src/main/java/dev/datarun/server/subject/SubjectProjection.java
server/src/main/java/dev/datarun/server/subject/SubjectController.java
server/src/main/java/dev/datarun/server/config/PatternRegistry.java
server/src/main/java/dev/datarun/server/projection/PatternStateProjection.java
server/src/main/java/dev/datarun/server/integrity/DomainUniquenessDetector.java
server/src/main/java/dev/datarun/server/integrity/TransitionViolationDetector.java
server/src/main/java/dev/datarun/server/integrity/ConflictResolutionService.java
server/src/main/java/dev/datarun/server/event/EventRepository.java
server/src/test/java/dev/datarun/server/identity/IdentityResolverIntegrationTest.java
server/src/test/java/dev/datarun/server/subject/SubjectControllerIntegrationTest.java
server/src/test/java/dev/datarun/server/projection/ProjectionEquivalenceTest.java
server/src/test/java/dev/datarun/server/contracts/PatternDefinitionContractTest.java
server/src/test/java/dev/datarun/server/projection/PatternStateProjectionTest.java
server/src/test/java/dev/datarun/server/integrity/DomainUniquenessIntegrationTest.java
server/src/test/java/dev/datarun/server/integrity/TransitionViolationIntegrationTest.java
server/src/test/java/dev/datarun/server/integrity/ConflictResolutionIntegrationTest.java
server/src/test/resources/fixtures/projection-equivalence.json
server/src/test/resources/fixtures/pattern-state-projection.json
```

## Verification Scope

Prove these points by existing tests, focused new tests, or code inspection plus targeted test evidence:

1. Merge and split are additive events using platform payload shapes; historical event references are not rewritten.
2. Alias projection is rebuildable and canonical after merge/split chains.
3. Subject projection resolves aliases where expected while preserving raw historical references for detection/audit paths.
4. Split-lineage and lifecycle checks prevent split-as-alias or invalid lifecycle interpretation.
5. Pattern definitions are platform-owned resources loaded from `contracts/patterns` and validated against `contracts/pattern-definition.schema.json`.
6. Pattern-state projection is rebuildable from event history, active bindings, and pattern definitions; it does not create durable workflow-state authority.
7. Pattern-state projection excludes unresolved flagged source events and re-includes accepted/rejected flags according to current conflict semantics.
8. Transition detection accepts structurally valid events, emits `transition_violation` flags for invalid state progression, and never rejects solely because projected workflow state is invalid.
9. Domain uniqueness detection accepts structurally valid events, emits `domain_uniqueness_violation`, excludes unresolved duplicate bases, and re-includes legal accepted resolutions.
10. Detector ordering remains consistent with the flag catalog and does not convert manual-only categories into auto-resolution.
11. Server projection/equivalence fixtures pass and remain aligned with `contracts/fixtures/*.json`.
12. BAR-014 is not fully accepted unless both server and mobile shared-fixture evidence are current. In this slice, attach server-side evidence and leave the mobile side routed to NW-010 when needed.

## Expected Work

Start with code inspection and existing targeted tests. If coverage is missing but behavior is correct, add narrowly focused tests in existing server test classes. If behavior is wrong, make a minimal server-side fix only if it stays inside current identity, projection, pattern registry, or integrity boundaries.

If verification passes:

- Update each accepted BAR row in `docs/agent-working-surface/baseline-acceptance-register.md` with exact command, date, and evidence summary.
- Mark BAR-009, BAR-012, and BAR-013 `baseline_accepted` only if their individual exit conditions are met.
- For BAR-014, attach server/shared-fixture evidence. Keep it `baseline_candidate` unless fresh mobile evidence is also attached; otherwise state that NW-010 owns the remaining mobile side.
- Mark NW-009 `accepted` if the server-side projection/integrity slice lands and any remaining BAR-014 mobile dependency is explicitly routed to NW-010.
- Leave NW-010 `ready`; do not start the mobile slice.

If verification fails:

- Leave affected BAR rows as `baseline_candidate`.
- Leave NW-009 `ready` or add a precise follow-up backlog row with the failing behavior, file/test anchor, and exit condition.
- Do not hide partial success by accepting a broader row than the evidence supports.

## Targeted Tests

Start the test database:

```bash
docker compose -f docker-compose.test.yml up -d test-db
```

Run the server projection/integrity slice:

```bash
cd server
./mvnw -Dtest=IdentityResolverIntegrationTest,SubjectControllerIntegrationTest,ProjectionEquivalenceTest,PatternDefinitionContractTest,PatternStateProjectionTest,DomainUniquenessIntegrationTest,TransitionViolationIntegrationTest,ConflictResolutionIntegrationTest test
```

If this is too broad to diagnose failures, split the same classes into identity/projection and pattern/integrity groups. Do not run full Maven or Flutter suites unless the fix crosses into shared platform behavior. Do not run NW-010 mobile tests in this slice unless you are only confirming that a shared fixture change did not obviously break them and you still keep NW-010 as the formal mobile baseline handoff.

## Guardrails

- Do not change the event envelope or event `type` vocabulary.
- Do not add durable workflow-state tables or make workflow state authoritative outside projection.
- Do not reject structurally valid state/policy anomalies that current architecture says to accept and flag.
- Do not implement auto-resolution.
- Do not implement resolver reassignment.
- Do not implement trigger execution.
- Do not implement S06/entity lifecycle.
- Do not introduce production OIDC/JWT/group/claim authority.
- Do not add new flag categories or change flag resolvability.
- Do not change `contracts/flag-catalog.md`, platform payload schemas, pattern schemas, pattern definitions, or shared fixtures unless you first prove they are stale against CDL/current contracts and record the downstream NW-010 impact.
- Do not edit mobile code; mobile verification is NW-010.
- Do not start S00/S19/S21/S27 scenario runtime probes in this slice.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(projection): verify projection integrity baseline
```

The commit may include focused server tests, narrow server-side fixes, and BAR/backlog updates. It must not include unrelated scenario, mobile, auth-provider, trigger, entity-lifecycle, or architecture edits.

## Stop And Report

Stop and report if:

- CDL/IDR, contracts, fixtures, and runtime behavior disagree.
- Accepting one BAR row would require weakening another BAR row or an accepted guardrail.
- the needed fix requires new envelope fields/types, durable workflow state, auto-resolution, resolver reassignment, trigger execution, entity lifecycle, production auth authority, or new scope mechanisms.
- server and shared fixtures cannot be reconciled without mobile behavior changes that belong in NW-010.
- pattern projection evidence only proves one pattern but the BAR claim requires all bundled active patterns.
- tests pass but do not actually prove unresolved-flag exclusion, legal accepted re-inclusion, or alias rebuild behavior.
