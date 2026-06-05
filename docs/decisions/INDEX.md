# Decision Index

> Regenerate with `scripts/build-idr-index.sh`.

## By Phase

| ID | Title | Status | Phase | Type | Tags |
|----|-------|--------|-------|------|------|
| [idr-001](idr-001-test-infrastructure.md) | Test infrastructure — docker-compose with host networking | active | 0a | decision | testing, docker, infrastructure |
| [idr-002](idr-002-pg-idkit-dev.md) | pg_idkit image for development PostgreSQL | active | 0a | decision | database, docker |
| [idr-003](idr-003-snake-case-json.md) | snake_case JSON response convention | active | 0a | decision | convention, api |
| [idr-004](idr-004-networknt-validator.md) | networknt json-schema-validator for envelope validation | active | 0a | decision | validation, dependency |
| [idr-005](idr-005-ci-github-actions.md) | CI via GitHub Actions with service containers | active | 0a | decision | ci, infrastructure |
| [idr-006](idr-006-thymeleaf-admin.md) | Server-rendered admin via Thymeleaf | active | 0c | decision | admin, ui |
| [idr-007](idr-007-concurrency-detection.md) | Concurrency detection via watermark (DD-1) | active | 1a | decision | sync, conflict, dd |
| [idr-008](idr-008-server-event-producer.md) | Server as event producer (DD-2) | active | 1a | decision | identity, server, dd |
| [idr-009](idr-009-alias-table.md) | Alias table with eager transitive closure (DD-3) | active | 1b | decision | identity, database, dd |
| [idr-010](idr-010-conflict-detection-intercept.md) | Conflict detection — same request, separate transactions (DD-4) | active | 1a | decision | conflict, pipeline, dd |
| [idr-011](idr-011-identity-conflict-scope.md) | Identity conflict detection — manual only in Phase 1 (DD-5) | active | 1 | decision | conflict, scope, dd |
| [idr-012](idr-012-sqflite-memory-path.md) | sqflite in-memory database path sharing | active | 1c | discovery | testing, mobile, sqflite |
| [idr-013](idr-013-assignment-payload.md) | Assignment event payload design (DD-1) | active | 2a | decision | authorization, assignment, payload, dd |
| [idr-014](idr-014-materialized-path-locations.md) | Geographic hierarchy — materialized path in static locations table (DD-2) | active | 2a | decision | authorization, geography, database, dd |
| [idr-015](idr-015-scope-filtered-sync-query.md) | Scope-filtered sync — denormalized location_path on events (DD-3) | active | 2a | decision | sync, authorization, performance, database, dd |
| [idr-016](idr-016-actor-token-table.md) | Actor identification — simple token table for Phase 2 (DD-4) | active | 2a | decision | authorization, authentication, sync, dd |
| [idr-017](idr-017-shape-storage.md) | Shape storage — versioned snapshots with L1/L2 separation (DD-1) | active | 3a | decision | configuration, shape, dd, storage, versioning |
| [idr-018](idr-018-expression-grammar.md) | Expression grammar — JSON AST with prefix-operator nodes (DD-2) | active | 3b | decision | configuration, expression, dd, grammar, cross-platform |
| [idr-019](idr-019-config-package.md) | Config package — atomic JSON delivery via dedicated endpoint (DD-3) | active | 3c | decision | configuration, package, dd, sync, delivery |
| [idr-020](idr-020-pattern-state-machine-representation.md) | Pattern state machine representation | active | 4-prep | decision | workflow, pattern-registry, projection, conflict, phase-4 |
| [idr-021](idr-021-role-action-enforcement-model.md) | Role-action enforcement model | active | 4-prep | decision | authorization, role-action, configuration, conflict, phase-4 |
| [idr-022](idr-022-flag-severity-and-domain-uniqueness.md) | Flag Severity + Domain Uniqueness | active | 4-prep | decision | configuration, flags, conflict, uniqueness, phase-4 |
| [idr-023](idr-023-role-action-domain-boundary-and-assignment-administration.md) | Role-action domain boundary and assignment administration | active | 4-prep | decision | authorization, role-action, assignment, configuration, phase-4 |
| [idr-024](idr-024-multi-axis-assignment-containment.md) | Multi-axis assignment containment | active | 4-prep | decision | authorization, assignment, scope, phase-4 |
| [idr-025](idr-025-pattern-definition-contract-and-delivery.md) | Pattern definition contract and delivery | active | 4-prep | decision | workflow, pattern-registry, contracts, configuration, phase-4 |
| [idr-026](idr-026-conflict-resolver-routing-and-single-writer-resolution.md) | Conflict resolver routing and single-writer resolution | active | 4-prep | decision | conflict, authorization, resolver-routing, phase-4 |
| [idr-027](idr-027-production-auth-principal-actor-binding.md) | Production auth principal-to-actor binding | active | post-phase-4-stabilization | decision | authorization, authentication, sync, production-auth, fp-011 |
| [idr-028](idr-028-production-principal-binding-administration.md) | Production principal-binding administration | active | post-phase-4-stabilization | decision | authorization, authentication, production-auth, provisioning, fp-011 |
| [idr-029](idr-029-assignment-admin-command-capability.md) | Assignment-admin command capability | active | post-phase-4-stabilization | decision | authorization, assignment, command-capability, configuration |
| [idr-030](idr-030-shared-device-session-lifecycle.md) | Shared-device session lifecycle | active | post-phase-4-stabilization | decision | authorization, authentication, sync, mobile, retention, shared-device |

## By Component

### server/sync
- [idr-001](idr-001-test-infrastructure.md) — Test infrastructure
- [idr-004](idr-004-networknt-validator.md) — Schema validator
- [idr-007](idr-007-concurrency-detection.md) — Concurrency detection
- [idr-010](idr-010-conflict-detection-intercept.md) — CD intercept point
- [idr-015](idr-015-scope-filtered-sync-query.md) — Scope-filtered sync query
- [idr-016](idr-016-actor-token-table.md) — Actor token table
- [idr-024](idr-024-multi-axis-assignment-containment.md) — Multi-axis assignment containment in sync/auth helpers
- [idr-026](idr-026-conflict-resolver-routing-and-single-writer-resolution.md) — Conflict resolution API actor binding
- [idr-027](idr-027-production-auth-principal-actor-binding.md) — Production auth principal binding and push actor binding
- [idr-030](idr-030-shared-device-session-lifecycle.md) — Shared-device actor-scoped sync progress and partitioning

### server/integrity
- [idr-007](idr-007-concurrency-detection.md) — Concurrency detection
- [idr-010](idr-010-conflict-detection-intercept.md) — CD intercept point
- [idr-011](idr-011-identity-conflict-scope.md) — Identity conflict scope
- [idr-020](idr-020-pattern-state-machine-representation.md) — Transition violation evaluation
- [idr-025](idr-025-pattern-definition-contract-and-delivery.md) — Packaged pattern definitions for transition evaluation
- [idr-021](idr-021-role-action-enforcement-model.md) — Role-action violation evaluation
- [idr-022](idr-022-flag-severity-and-domain-uniqueness.md) — Flag severity and domain uniqueness evaluation
- [idr-023](idr-023-role-action-domain-boundary-and-assignment-administration.md) — Role-action domain boundary
- [idr-024](idr-024-multi-axis-assignment-containment.md) — Null-activity authority semantics
- [idr-026](idr-026-conflict-resolver-routing-and-single-writer-resolution.md) — Resolver routing and canonical resolution enforcement
- [idr-027](idr-027-production-auth-principal-actor-binding.md) — Group/claim non-authority for resolver checks

### server/authorization
- [idr-013](idr-013-assignment-payload.md) — Assignment payload design
- [idr-014](idr-014-materialized-path-locations.md) — Materialized path locations
- [idr-015](idr-015-scope-filtered-sync-query.md) — Scope-filtered sync query
- [idr-016](idr-016-actor-token-table.md) — Actor token table
- [idr-021](idr-021-role-action-enforcement-model.md) — Role-action permission semantics
- [idr-023](idr-023-role-action-domain-boundary-and-assignment-administration.md) — Assignment administration boundary
- [idr-024](idr-024-multi-axis-assignment-containment.md) — Assignment creation/end containment
- [idr-026](idr-026-conflict-resolver-routing-and-single-writer-resolution.md) — Conflict API bearer-token actor binding
- [idr-027](idr-027-production-auth-principal-actor-binding.md) — Principal-to-actor binding for production auth foundation
- [idr-028](idr-028-production-principal-binding-administration.md) — Deployment-managed principal binding provisioning and audit
- [idr-029](idr-029-assignment-admin-command-capability.md) — Assignment-admin command capability policy
- [idr-030](idr-030-shared-device-session-lifecycle.md) — Shared-device authenticated actor session boundary

### server/identity
- [idr-008](idr-008-server-event-producer.md) — Server event producer
- [idr-009](idr-009-alias-table.md) — Alias table

### server/config
- [idr-017](idr-017-shape-storage.md) — Shape storage & versioning
- [idr-018](idr-018-expression-grammar.md) — Expression grammar
- [idr-019](idr-019-config-package.md) — Config package & delivery
- [idr-020](idr-020-pattern-state-machine-representation.md) — Pattern binding in config package
- [idr-025](idr-025-pattern-definition-contract-and-delivery.md) — Pattern definition contract and package delivery
- [idr-021](idr-021-role-action-enforcement-model.md) — Activity role-action mappings
- [idr-022](idr-022-flag-severity-and-domain-uniqueness.md) — Flag severity overrides and shape uniqueness constraints
- [idr-023](idr-023-role-action-domain-boundary-and-assignment-administration.md) — Activity role-action vocabulary boundary

### server/admin
- [idr-006](idr-006-thymeleaf-admin.md) — Thymeleaf admin
- [idr-026](idr-026-conflict-resolver-routing-and-single-writer-resolution.md) — Development-only flag-resolution boundary
- [idr-028](idr-028-production-principal-binding-administration.md) — Principal binding administration is deployment-managed, not an online admin API

### mobile/data
- [idr-003](idr-003-snake-case-json.md) — snake_case JSON
- [idr-019](idr-019-config-package.md) — Config package (device-side management)
- [idr-020](idr-020-pattern-state-machine-representation.md) — Pattern binding parsing
- [idr-025](idr-025-pattern-definition-contract-and-delivery.md) — Packaged pattern definition parsing
- [idr-021](idr-021-role-action-enforcement-model.md) — Role-action config parsing
- [idr-022](idr-022-flag-severity-and-domain-uniqueness.md) — Severity and uniqueness config parsing
- [idr-023](idr-023-role-action-domain-boundary-and-assignment-administration.md) — Role-action config boundary
- [idr-027](idr-027-production-auth-principal-actor-binding.md) — `/api/auth/me` actor alignment for event authorship
- [idr-029](idr-029-assignment-admin-command-capability.md) — Assignment-admin capability may be advisory-only on mobile
- [idr-030](idr-030-shared-device-session-lifecycle.md) — Per-actor local partitions and session switching

### mobile/domain
- [idr-018](idr-018-expression-grammar.md) — Expression evaluator
- [idr-009](idr-009-alias-table.md) — Alias table (device schema)
- [idr-020](idr-020-pattern-state-machine-representation.md) — Pattern state projection
- [idr-025](idr-025-pattern-definition-contract-and-delivery.md) — Pattern definition contract for projection
- [idr-021](idr-021-role-action-enforcement-model.md) — Advisory role-action gating
- [idr-022](idr-022-flag-severity-and-domain-uniqueness.md) — Advisory uniqueness checks and flag severity behavior
- [idr-023](idr-023-role-action-domain-boundary-and-assignment-administration.md) — Advisory role-action vocabulary boundary

### mobile/test
- [idr-012](idr-012-sqflite-memory-path.md) — sqflite memory path

### contracts
- [idr-003](idr-003-snake-case-json.md) — snake_case JSON
- [idr-007](idr-007-concurrency-detection.md) — Concurrency detection
- [idr-013](idr-013-assignment-payload.md) — Assignment payload design
- [idr-020](idr-020-pattern-state-machine-representation.md) — Pattern binding contract
- [idr-021](idr-021-role-action-enforcement-model.md) — Activity role-action contract
- [idr-022](idr-022-flag-severity-and-domain-uniqueness.md) — Flag severity and uniqueness contract
- [idr-023](idr-023-role-action-domain-boundary-and-assignment-administration.md) — Activity role-action vocabulary boundary
- [idr-024](idr-024-multi-axis-assignment-containment.md) — Assignment scope containment contract
- [idr-025](idr-025-pattern-definition-contract-and-delivery.md) — Pattern definition schema and canonical files
- [idr-026](idr-026-conflict-resolver-routing-and-single-writer-resolution.md) — Flag resolver routing catalog
- [idr-027](idr-027-production-auth-principal-actor-binding.md) — Sync push bearer/authorship contract

### ci
- [idr-001](idr-001-test-infrastructure.md) — Test infrastructure
- [idr-005](idr-005-ci-github-actions.md) — GitHub Actions
