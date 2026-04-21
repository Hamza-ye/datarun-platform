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

## By Component

### server/sync
- [idr-001](idr-001-test-infrastructure.md) — Test infrastructure
- [idr-004](idr-004-networknt-validator.md) — Schema validator
- [idr-007](idr-007-concurrency-detection.md) — Concurrency detection
- [idr-010](idr-010-conflict-detection-intercept.md) — CD intercept point
- [idr-015](idr-015-scope-filtered-sync-query.md) — Scope-filtered sync query
- [idr-016](idr-016-actor-token-table.md) — Actor token table

### server/integrity
- [idr-007](idr-007-concurrency-detection.md) — Concurrency detection
- [idr-010](idr-010-conflict-detection-intercept.md) — CD intercept point
- [idr-011](idr-011-identity-conflict-scope.md) — Identity conflict scope

### server/authorization
- [idr-013](idr-013-assignment-payload.md) — Assignment payload design
- [idr-014](idr-014-materialized-path-locations.md) — Materialized path locations
- [idr-015](idr-015-scope-filtered-sync-query.md) — Scope-filtered sync query
- [idr-016](idr-016-actor-token-table.md) — Actor token table

### server/identity
- [idr-008](idr-008-server-event-producer.md) — Server event producer
- [idr-009](idr-009-alias-table.md) — Alias table

### server/config
- [idr-017](idr-017-shape-storage.md) — Shape storage & versioning
- [idr-018](idr-018-expression-grammar.md) — Expression grammar
- [idr-019](idr-019-config-package.md) — Config package & delivery

### server/admin
- [idr-006](idr-006-thymeleaf-admin.md) — Thymeleaf admin

### mobile/data
- [idr-003](idr-003-snake-case-json.md) — snake_case JSON
- [idr-019](idr-019-config-package.md) — Config package (device-side management)

### mobile/domain
- [idr-018](idr-018-expression-grammar.md) — Expression evaluator
- [idr-009](idr-009-alias-table.md) — Alias table (device schema)

### mobile/test
- [idr-012](idr-012-sqflite-memory-path.md) — sqflite memory path

### contracts
- [idr-003](idr-003-snake-case-json.md) — snake_case JSON
- [idr-007](idr-007-concurrency-detection.md) — Concurrency detection
- [idr-013](idr-013-assignment-payload.md) — Assignment payload design

### ci
- [idr-001](idr-001-test-infrastructure.md) — Test infrastructure
- [idr-005](idr-005-ci-github-actions.md) — GitHub Actions
