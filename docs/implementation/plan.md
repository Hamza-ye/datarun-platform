# Implementation Plan

> Technology stack, repository structure, module boundaries, and phased build order. Every choice traces to an architectural constraint or an operational reality from [constraints.md](../constraints.md).

---

## 1. Technology Stack

### Decided

| Component | Technology | Architectural trace |
|-----------|-----------|---------------------|
| **Mobile client** | Dart / Flutter | Offline-first on low-end Android is the core operational constraint. Flutter: mature SQLite support, single codebase, performant on 8–16GB devices. |
| **Device storage** | SQLite | The only mature embedded database for append-only event stores on Android. Used in the event spike ([experiments/s00-event-spike/](../experiments/s00-event-spike/)). |
| **Server** | Java / Spring Boot | Event processing pipeline (sync, conflict detection, triggers) suits Spring Boot's request-based model. Mature ecosystem for PostgreSQL, scheduling (L3b), REST. |
| **Server storage** | PostgreSQL | JSONB for event payloads. Strong indexing for projection queries. Advisory locks for sync coordination. Proven at the decided scale (millions of events, tens of thousands of actors). |
| **Admin UI** | Angular | Configuration authoring (shapes, activities, triggers), flag resolution, oversight views. Separate SPA — deployers and coordinators use laptops with reliable connectivity. |
| **Sync transport** | REST / JSON over HTTPS | Simplest protocol that satisfies SY-1 through SY-4. Idempotent event exchange works naturally with HTTP POST. 2G-friendly: no persistent connections, no binary framing required. gRPC deferred — adds complexity on low-end devices with marginal benefit for event-sized payloads. |
| **Shared contracts** | JSON Schema | Event envelope and shape definitions need language-neutral validation. JSON Schema is readable, validatable in both Dart and Java, and doubles as documentation. Shape definitions ARE schemas — this is not an addition, it's the natural format. |

### Deferred

| Component | Options | When to decide |
|-----------|---------|---------------|
| CI/CD pipeline | GitHub Actions, GitLab CI | Before first deployment (post-Phase 0) |
| Container orchestration | Docker Compose (dev), Kubernetes (prod) | Phase 2 (multi-actor requires realistic deployment) |
| Push notifications | FCM | Phase 4 (triggers produce alerts — need delivery to devices between syncs) |
| Monitoring / observability | OpenTelemetry, Prometheus | Phase 2 (meaningful only with multiple actors and scoped sync) |

---

## 2. Repository Structure

A **monorepo** for the platform code, separate from this documentation repository.

**Why monorepo**: Single developer + AI agents. Shared contracts must stay in sync across server/mobile/admin. Cross-cutting changes (envelope evolution, shape format) touch multiple codebases. Simpler dependency management during early phases.

**When to split**: If team size exceeds 3 people or CI times exceed 10 minutes. The module boundaries below are designed for clean separation if splitting becomes necessary.

```
datarun-platform/
│
├── contracts/                  # Language-neutral shared definitions
│   ├── envelope.schema.json    # Event envelope JSON Schema
│   ├── shape-format.schema.json # Shape definition format
│   ├── sync-protocol.md        # Sync endpoint contracts
│   └── flag-catalog.md         # 9 flag categories with codes
│
├── server/                     # Java / Spring Boot
│   ├── core/                   # Event Store, Projection Engine
│   ├── identity/               # Identity Resolver
│   ├── integrity/              # Conflict Detector
│   ├── authorization/          # Scope Resolver
│   ├── configuration/          # Shape Registry, Deploy-time Validator, Config Packager
│   ├── workflow/               # Pattern Registry, Trigger Engine, Expression Evaluator
│   └── sync/                   # Sync endpoints (REST)
│
├── mobile/                     # Dart / Flutter
│   ├── core/                   # Event Store (SQLite), Projection Engine
│   ├── config/                 # Config consumer (shapes, patterns, assignments)
│   ├── capture/                # Form rendering, Command Validator (advisory)
│   ├── expression/             # Expression Evaluator (form context)
│   └── sync/                   # Sync client
│
└── admin/                      # Angular
    ├── config/                 # Shape authoring, activity authoring, trigger authoring
    ├── oversight/              # Flag resolution, supervision views
    └── deployment/             # Config validation, packaging, deployment management
```

### Contracts module

The `contracts/` directory is the authoritative definition of what crosses process boundaries. Both `server/` and `mobile/` validate against these schemas. The `admin/` UI reads them for form generation.

This is NOT a code library. It's a set of JSON Schema files and protocol specifications. Each codebase generates or hand-writes its own types from these schemas.

---

## 3. Primitive-to-Module Map

The 11 primitives from the [Architecture Description](../architecture/primitives.md) split across device and server based on two architectural constraints: triggers execute server-only [4-S5], and flags are created server-side [SG-1]. Merge/split are online-only [2-S10].

| Primitive | Server | Device | Notes |
|-----------|:------:|:------:|-------|
| Event Store | ✓ | ✓ | PostgreSQL on server, SQLite on device. Same envelope, different storage engines. |
| Projection Engine | ✓ | ✓ | Server: full state (conflict detection, triggers, aggregation). Device: per-subject state (UI, form context). |
| Identity Resolver | ✓ | reads | Merge/split logic server-only. Device receives alias table via sync for projection lookup. |
| Conflict Detector | ✓ | — | Flags created server-side during sync processing. Device has no flag creation capability. |
| Scope Resolver | ✓ | — | Sync scope computed server-side. Device trusts its sync payload as complete. |
| Shape Registry | ✓ | reads | Authored/validated on server. Device receives shape definitions in config package. |
| Expression Evaluator | ✓ | ✓ | Server: trigger context (`event.*`). Device: form context (`payload.*`, `entity.*`, `context.*`). Same language, different available scopes. |
| Trigger Engine | ✓ | — | Server-only [4-S5]. Both L3a and L3b. |
| Deploy-time Validator | ✓ | — | Server-only. Config gate before packaging. |
| Config Packager | ✓ | — | Server-only. Produces atomic config packages for devices. |
| Pattern Registry | ✓ | reads | Pattern definitions are platform-fixed. Device receives parameterized instances in config package. |
| Command Validator | — | ✓ | Device-only advisory component. Warns before invalid submissions. |

**Shared kernel**: Event envelope structure, shape format, expression grammar. Defined once in `contracts/`, implemented in both codebases.

---

## 4. Build Phases

5 phases. Each produces a working, testable system. Each builds on the last. Phase boundaries are acceptance gates — the previous phase's deliverable must work before the next begins.

### Phase 0: Core Loop

**Goal**: S00 end-to-end — the P7 benchmark. "A field worker captures data offline, syncs, and the data appears on the server."

**Exercises**: ADR-1 (partially). SY-1, SY-2, SY-3, SY-4.

**Primitives built**:
- Event Store (device + server)
- Projection Engine (minimal: per-subject state derivation)
- Shape Registry (minimal: load shapes from config, no authoring UI)

**Contracts exercised**: C1 (event integrity), C2 (stream → projection), C11 (shape versions available).

**What's NOT in Phase 0**: No conflict detection, no identity resolution, no authorization, no triggers, no multi-actor. One user, one device, one shape, happy path only.

**Deliverable**: Capture → store locally → sync → server persists → server projects current state → admin can see submissions.

**Detailed scope**: [phases/phase-0.md](phases/phase-0.md)

---

### Phase 1: Identity & Integrity

**Goal**: Multi-subject, multi-device reality. "Two devices capture data about the same subject; conflicts are detected and surfaced for resolution."

**Exercises**: ADR-2 fully.

**Primitives built**:
- Identity Resolver (4 typed categories, alias resolution, merge/split)
- Conflict Detector (accept-and-flag pipeline, single-writer resolution, detect-before-act)
- Projection Engine (extended: alias resolution, flagged-event exclusion, source-chain traversal)

**Contracts exercised**: C3, C4, C7, C8.

**New cross-cutting concerns activated**: Accept-and-flag lifecycle, detect-before-act pipeline, unified flag catalog.

**Deliverable**: Two devices sync events about the same subject → server detects conflicts → flags surfaced in admin UI → resolver resolves → projection re-derived.

---

### Phase 2: Authorization & Multi-Actor

**Goal**: The system is usable by an organization. "A supervisor sees only their team's data. Sync delivers only authorized events."

**Exercises**: ADR-3 fully. SY-5, SY-8.

**Primitives built**:
- Scope Resolver (assignment-based access, scope-containment test, 3 scope types)
- Sync (extended: scope-filtered payload, selective-retain on scope contraction)

**Contracts exercised**: C6, C10.

**Architecture concerns addressed**: Sync scope = access scope. Authority-as-projection.

**Deliverable**: Multiple actors with different roles → each device receives only authorized data → supervisor views cross their team → scope changes reflected at next sync.

---

### Phase 3: Configuration

**Goal**: The system is domain-agnostic. "A deployer defines a new data shape, publishes it, and field workers capture data with it — no code change."

**Exercises**: ADR-4 fully. SY-7.

**Primitives built**:
- Shape Registry (full: authoring, versioning, deprecation-only evolution)
- Expression Evaluator (both contexts: form L2 + trigger L3)
- Deploy-time Validator (hard budgets, composition rules)
- Config Packager (atomic delivery, at-most-2 versions)
- Admin UI: config authoring (shapes, activities, expressions)

**Contracts exercised**: C5, C12, C13, C14, C19, C20.

**Cross-cutting concerns activated**: Four-layer gradient, configuration delivery pipeline.

**Deliverable**: Deployer authors a shape in admin UI → validator checks budgets → packager builds config → device receives at sync → field worker captures with new shape.

---

### Phase 4: Workflow & Policies

**Goal**: Full platform. "A deployer configures a review workflow; captures go through review; overdue items trigger alerts."

**Exercises**: ADR-5 fully. SY-6.

**Primitives built**:
- Pattern Registry (4 patterns: capture_with_review, case_management, multi_step_approval, transfer_with_acknowledgment)
- Trigger Engine (L3a event-reaction, L3b deadline-check, auto-resolution)
- Command Validator (advisory on-device)

**Contracts exercised**: C9, C15, C16, C17, C18, C21.

**Deliverable**: Deployer selects a workflow pattern and maps shapes to roles → field captures go through configured review flow → overdue reviews trigger alerts → auto-resolution handles timing overlaps.

---

## 5. Implementation-Grade Decision Schedule

The 15 implementation-grade items from [boundary.md](../architecture/boundary.md#3-implementation-grade-items) are scheduled across phases. Each is resolved within its phase — not before — following the last responsible moment principle.

| IG# | Item | Phase | Approach direction |
|-----|------|:-----:|-------------------|
| IG-1 | Projection rebuild strategy | 0 | Start with full replay. Escape hatch B→C evaluated against on-device performance in Phase 1. |
| IG-2 | Projection merge across schema versions | 3 | Route on `shape_ref` version in projection logic. Multi-version handling is a Phase 3 concern. |
| IG-3 | Batch resolution and flag grouping | 1 | Individual resolution events. Batch UX is admin UI design. |
| IG-4 | Sync pagination, priority, bandwidth | 0 | Start with simple watermark-based pagination. Optimize in Phase 2 when scale matters. |
| IG-5 | Source-chain traversal depth | 1 | Practical limit aligned with DAG max path 2 (trigger chains). Organic chains: measure before limiting. |
| IG-6 | `context.*` property caching | 3 | Compute at form-open. Cache invalidation: recompute on projection change. |
| IG-7 | Breaking change migration tooling | 3 | Deferred — deprecation-only is the default path. Only needed if escape hatch triggers. |
| IG-8 | Configuration authoring format | 3 | JSON for shape definitions (matches JSON Schema). Admin UI provides visual authoring. |
| IG-9 | Deploy-time validator UX | 3 | Violation list with specific budget/rule citations. Admin UI concern. |
| IG-10 | Pattern skeleton formal schemas | 4 | JSON format for pattern definitions. Platform ships with 4 built-in. |
| IG-11 | Pattern inventory (which ship) | 4 | 4 existence-proof patterns from [patterns.md](../architecture/patterns.md). Start with `capture_with_review`. |
| IG-12 | Auto-resolution policy authoring UX | 4 | Admin UI for L3b policies. Same authoring surface as deadline-check triggers. |
| IG-13 | Auto-resolution specific policies | 4 | Ship `temporal_authority_expired` and `stale_reference` auto-resolution by default. |
| IG-14 | Device sharing (multiple actors) | 2 | Per-actor sync sessions. Each actor has independent event store partition + sync watermark. |
| IG-15 | Expression language grammar/syntax | 3 | Minimal prefix notation or infix DSL. 3-predicate limit makes grammar trivial. Decide when building the evaluator. |

---

## 6. Phase Detailed Scopes

Phase detailed scopes — including module-level scope, schemas, acceptance criteria, sub-phase breakdowns, quality gates, and milestones — are maintained as individual files in [`phases/`](phases/). Each file is created at the start of its phase. Implementation decisions and discoveries are in [`docs/decisions/`](../decisions/).

| Phase | File | Status |
|-------|------|--------|
| Phase 0 | [phase-0.md](phases/phase-0.md) | Complete |
| Phase 1 | [phase-1.md](phases/phase-1.md) | Complete |
| Phase 2 | [phase-2.md](phases/phase-2.md) | Complete |
| Phase 3 | [phase-3.md](phases/phase-3.md) | Not started |
| Phase 4 | — | Not started |

Previous external references to "plan.md §6" now resolve to the corresponding phase file.

---

## 7. Agent Collaboration Plan

Skills and agents to maximize value at each phase. Ordered by when to engage.

### Phase 0

| Step | Agent | Value |
|------|-------|-------|
| Database schema review | **Database Optimizer** | Event store indexing, projection query patterns, SQLite vs PostgreSQL schema alignment |
| Sync endpoint contract | **Backend Architect** | REST API design, idempotency patterns, error handling, watermark assignment |
| Flutter app scaffold | **Mobile App Builder** | Offline-first patterns, SQLite integration, form rendering from schema |
| Phase 0 acceptance testing | **API Tester** | Sync edge cases: duplicate events, out-of-order arrival, large payloads |
| Code review at phase boundary | **Code Reviewer** | Correctness, write-path discipline adherence |

### Phase 1

| Step | Agent | Value |
|------|-------|-------|
| Conflict detection pipeline | **Software Architect** | Accept-and-flag state machine, flag lifecycle, detect-before-act ordering |
| Identity merge/split logic | **Backend Architect** | Alias resolution in projection, transitive closure, lineage DAG |

### Phase 2

| Step | Agent | Value |
|------|-------|-------|
| Authorization model | **Security Engineer** | Scope-containment implementation, selective sync, role/scope security review |
| Sync protocol hardening | **Security Engineer** | Transport security, auth tokens, device identity validation |
| Deployment architecture | **DevOps Automator** | Docker Compose (dev), deployment pipeline, database migrations |

### Phase 3

| Step | Agent | Value |
|------|-------|-------|
| Expression language grammar | **Software Architect** | DSL design within 3-predicate ceiling, evaluation engine |
| Config authoring UX | **UX Architect** | Admin UI for shape/activity/trigger authoring, deployer workflow |
| Deploy-time validation | **Backend Architect** | Budget enforcement, composition rule checking, error reporting |

### Phase 4

| Step | Agent | Value |
|------|-------|-------|
| State machine projection | **Software Architect** | Pattern-driven state derivation, composition rule enforcement at runtime |
| Trigger engine | **Backend Architect** | L3a/L3b execution, DAG depth enforcement, loop prevention |
| End-to-end scenario validation | **Code Reviewer** + **API Tester** | Walk S04, S08 through the full system |
