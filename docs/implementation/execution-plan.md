# Execution Plan

> Actionable build sequence, quality gates, and operational practices for taking the Datarun architecture from documentation to production-grade code. Supplements [plan.md](plan.md) — that document defines *what* gets built (technology stack, module map, schemas, acceptance criteria); this document defines *how* to build it fast without losing quality.

---

## 1. Principles for Execution

| # | Principle | What it means in practice |
|---|-----------|--------------------------|
| E1 | **Working software over comprehensive scaffolding** | Every sub-phase ends with something that runs and can be demonstrated. No "set up the project structure" phases that produce empty directories. |
| E2 | **CI from first commit** | The platform repository has automated builds, tests, and linting from the first day. Quality is not bolted on — it's the default. |
| E3 | **Narrow before wide** | Each sub-phase touches one or two codebases, not three. Integration comes after each piece works independently. |
| E4 | **Contracts are tested, not trusted** | The JSON Schema contracts in `contracts/` are validated in both codebases automatically. A schema change that breaks a consumer fails CI before it merges. |
| E5 | **Deploy from day one** | The server is deployable via `docker compose up` from Phase 0a forward. There is no "works on my machine" period. |
| E6 | **Spike, then commit** | When a technical question blocks progress for more than 2 hours, extract it into a time-boxed spike (max 4 hours). Record the finding. Move on. |
| E7 | **Projection equivalence is non-negotiable** | The Projection Engine runs in Java (server) and Dart (device) — two independent implementations of the same logic. Drift between them is the single most dangerous class of bug in this system. Equivalence is enforced through property-based contract tests: given the same ordered event set, both engines must produce byte-identical JSON projection output. If a projection rule cannot be expressed such that both implementations produce identical output, the rule must be simplified until it can. |

---

## 2. Decision Authority

| Decision type | Who decides | How to recognize it |
|--------------|-------------|--------------------|
| Structural constraint | Already decided by ADR | Listed in `docs/architecture/primitives.md` with `[N-SX]` trace. Must not violate. |
| Strategy-protecting constraint | Already decided by ADR | Listed in primitives.md. Must not violate without escalation. |
| Initial strategy | Already decided by ADR | Listed in primitives.md. May propose revision with evidence, but default is follow. |
| Implementation-grade | Implementing agent | Listed in `docs/architecture/boundary.md` §3 (15 items). Choose freely within stated constraint boundary. |
| Outside | Nobody (excluded) | Listed in boundary.md §4 (8 items). If needed, escalate — may require architecture evolution. |
| Not listed anywhere | Stop and ask | Not every implementation detail is pre-classified. If unsure, escalate. |

---

## 3. Forbidden Patterns

Things that must **never** be done. Include in every implementation and review prompt.

| # | Forbidden | Why | Source |
|---|-----------|-----|--------|
| F1 | Add or modify envelope fields | Envelope finalized at 11 fields. Requires ADR-level decision. | [1-S1, 1-S5, 4-S1] |
| F2 | Create new event types | Type vocabulary is platform-fixed, closed, append-only. | [4-S3] |
| F3 | Write to events table outside Event Store module | Write-path discipline protects B→C escape hatch. | [1-ST2] |
| F4 | Modify or delete persisted events | Append-only is the foundational invariant. | [1-S1] |
| F5 | Skip contract tests | Contract tests are the backbone of system correctness. | §8 |
| F6 | Reject events for state staleness | Accept-and-flag — events are never rejected. Flag them. | [2-S14] |
| F7 | Create triggers that chain recursively | DAG max path 2, output type ≠ input type. | [4-S12] |
| F8 | Store state independently of projections | Projections are the only read path. No shadow state. | [1-S2] |
| F9 | Make merge/split available offline | Online-only, server-validated. | [2-S10] |
| F10 | Allow deployers to define scope logic | 3 platform-fixed scope types only. | [4-S7] |
| F11 | Schema changes without migration scripts | Every DDL change (server: Flyway migration, device: SQLite `onUpgrade` version) must be tracked. No ad-hoc `ALTER TABLE`. | Phase 0 convention |

---

## 4. Escalation Triggers

When work must **stop** rather than proceed:

| Trigger | Action |
|---------|--------|
| Task needs a capability not in the 15 IG items | Stop. May be outside the architecture. |
| Escape hatch trigger condition met (boundary.md §6) | Stop. Document evidence. Architecture-level decision required. |
| Performance threshold exceeded (>200ms projection, >50ms authority) | Stop. Escape hatch activation. Document measurements. |
| Need to share state between primitives outside contracted interfaces | Stop. Module boundary violation. Contract may need addition. |
| Implementation-grade decision in one module affects another | Stop. Cross-cutting decision. Needs coordination. |
| Unsure whether something is decided, implementation-grade, or outside | Stop. Classify before proceeding. |

### Mid-phase discovery protocol

When implementation reveals a design gap (e.g., entity references need richer structure, a new subject category emerges), follow this 3-stage protocol instead of ad-hoc redesign:

**Stage 1 — Classify.** Apply the decision boundary test from [boundary.md](../architecture/boundary.md): Does this change stored event data or cross-device contracts? If yes → architecture-grade, escalate. If no → implementation-grade, proceed to Stage 2.

**Stage 2 — Bound.** Time-box a spike (max 4 hours). Answer 3 questions: (1) Can existing primitives compose to handle this? (2) Does it affect contracts other modules depend on? (3) Does it change what's stored in the event envelope or event store? Most discoveries are **composition questions** — they resolve as new configuration patterns within existing architecture, not as new primitives.

**Stage 3 — Resolve.** Three possible outcomes:
- **Compose**: The discovery resolves within existing primitives and contracts. Document the composition in the decision log (§14) and continue.
- **Extend**: An implementation-grade item needs a choice that wasn't anticipated. Make the choice within the IG boundary, document it, continue.
- **Escalate**: The discovery affects stored data, contracts, or constraint boundaries. Stop. Document evidence. This requires architecture evolution before implementation continues.

---

## 5. Definition of Production Grade

"Production grade" for Datarun means:

| Dimension | Standard | How it's verified |
|-----------|----------|-------------------|
| **Correctness** | Every contract guarantee from [contracts.md](../architecture/contracts.md) exercised by at least one automated test | CI test suite |
| **Reliability** | Sync never loses events. Idempotency holds under retry. Offline capture always succeeds. | Dedicated sync & offline test scenarios |
| **Performance** | Form open < 300ms on reference device (Redmi 9A or equivalent). Projection rebuild < 200ms/subject. Sync of 100 events < 5s on 3G. | Benchmark suite run before each phase boundary |
| **Deployability** | Server starts from `docker compose up`. Mobile builds from `flutter build apk`. No manual steps. | CI pipeline includes build verification |
| **Security** | No sensitive data in logs. Sync endpoints reject malformed payloads. SQL injection impossible (parameterized queries only). | Security checklist at each phase boundary |
| **Maintainability** | Any single-concern change touches ≤ 3 files. Module boundaries match primitive boundaries. | Code review at phase boundaries |

These are gate criteria. Code that doesn't meet them doesn't pass the phase boundary — it gets fixed first.

---

## 6. Phase Specifications

Phase-specific sub-phase breakdowns, quality gates, acceptance criteria, technical specifications, and milestones are maintained in [`phases/`](phases/). Each phase file is created at the start of that phase and combines:

1. **Scope** — what gets built, module-level detail, schemas
2. **Design Decisions** — genuinely open implementation questions, with approach directions and skill callouts
3. **Reversibility Triage** — Lock/Lean/Leaf classification of every open decision (see §6.1)
4. **Sub-phases** — sequencing, deliverables, quality gates per sub-phase
5. **Technical specifications** — schemas, protocols, envelope details
6. **Acceptance criteria** — the gate for phase completion
7. **Milestones / Journal** — progress markers, quality gate results (decisions and discoveries are in [`docs/decisions/`](../decisions/))

| Phase | File | Status |
|-------|------|--------|
| Phase 0 | [phase-0.md](phases/phase-0.md) | Complete |
| Phase 1 | [phase-1.md](phases/phase-1.md) | Complete |
| Phase 2 | [phase-2.md](phases/phase-2.md) | Complete |
| Phase 3 | [phase-3.md](phases/phase-3.md) | Not started |
| Phase 4 | — | Not started |

Previous external references to "execution-plan.md §6" now resolve to the corresponding phase file. Phase 1–4 execution guidance remains in §7 as brief outlines; when each phase starts, a phase file is created with the expanded scope and sub-phase breakdown.

### 6.1 Reversibility Triage (required step)

Every phase file includes a **Reversibility Triage** section, written at phase start before the first DD is resolved. For completed phases, the triage is retroactive (documenting what was already decided). The triage classifies every open decision into three buckets:

| Bucket | Meaning | Action | Document as |
|--------|---------|--------|-------------|
| **Lock** | Reversal requires data migration, protocol change, or coordinated client+server rework. | Design carefully. This IS the march-forward work. Spend design energy here. | IDR with `reversal-cost: high` |
| **Lean** | Reversal is code-only — no data migration, no protocol change, no contract change. | Build the simplest thing that satisfies the contract. Protect the interface, not the implementation. | IDR with `reversal-cost: low` + one-sentence evolve-later trigger |
| **Leaf** | Pure UX, cosmetic, or internal implementation. Zero downstream impact. | Just build it. | Nothing — not a decision |

**The test**: "Can I change this with a PR that touches only one side (server OR mobile), and no data migration?" → Lean. Otherwise → Lock.

**Skill callouts**: When a Lock decision requires expertise beyond standard implementation, the triage notes the skill needed and which agent should review it. This prevents under-investing in high-stakes design.

The triage table lives in the phase file, between "Design Decisions" and "Sub-Phases". It takes 15 minutes to write and saves hours of agonizing over Leaf decisions or under-designing Lock decisions.

---

## 7. Phase 1–4: Execution Guidance

Phases 1–4 scope, primitives, and acceptance criteria remain as defined in [plan.md](plan.md) §4. This section adds execution structure.

### Phase 1: Identity & Integrity

**Sub-phases**:
- **1a: Conflict Detector server-side** — accept-and-flag pipeline, flag events, detect-before-act ordering. Test: two events for same subject with conflicting state → flag raised.
- **1b: Identity Resolver** — alias table, merge/split events, projection re-derivation. Test: merge two subjects → events re-attributed in projection.
- **1c: Mobile + Admin updates** — flag badges in Work List and Subject Detail. Admin flag resolution UI.

**Key risks**: Detect-before-act ordering correctness. Transitive closure performance for alias resolution.

**Spike candidates**: Source-chain traversal depth (IG-5). Projection rebuild performance with flagged-event exclusion.

---

### Phase 2: Authorization & Multi-Actor

**Sub-phases**:
- **2a: Scope Resolver** — assignment-based access, scope-containment test. Test: two actors with different scopes → different sync payloads.
- **2b: Sync protocol hardening** — auth tokens, scope-filtered pull, selective-retain on scope contraction.
- **2c: Multi-actor mobile** — per-actor sync sessions (IG-14). Deployment architecture: Docker Compose for dev, containerized server.

**Key risks**: Sync scope = access scope invariant under edge cases (scope change during offline period). Device sharing without watermark corruption.

**Critical decision**: Container orchestration (Docker Compose dev / Kubernetes prod decision). Resolve at start of Phase 2.

---

### Phase 3: Configuration

**Sub-phases**:
- **3a: Shape Registry full** — authoring, versioning, deprecation-only evolution. Deploy-time Validator with budget enforcement.
- **3b: Expression Evaluator** — grammar design (IG-15), both contexts (form L2, trigger L3). Config Packager with atomic delivery.
- **3c: Admin authoring UI** — shape authoring, activity authoring, expression authoring. Deployer workflow.

**Key risks**: Expression language grammar design (IG-15) — must be simple enough for deployers but expressive enough for L2/L3 needs within the 3-predicate budget. Config package atomicity under poor connectivity (partial download).

**Spike candidates**: Expression grammar (IG-15 — before building the evaluator). Config package format and delivery mechanism.

---

### Phase 4: Workflow & Policies

**Sub-phases**:
- **4a: Pattern Registry + state machine projection** — 4 patterns, state derivation, composition rule enforcement.
- **4b: Trigger Engine** — L3a event-reaction, L3b deadline-check, auto-resolution. Loop prevention guards.
- **4c: Command Validator + full integration** — advisory on-device warnings. End-to-end scenario validation (S04, S08 through full system).

**Key risks**: Trigger loop prevention (3 independent guards must all work). Auto-resolution policy correctness. Pattern composition rule enforcement at deploy time.

---

## 8. Testing Strategy

### Test pyramid

```
                    ┌─────────────┐
                    │  E2E Tests  │  ← Few. Full capture → sync → project → admin.
                    │  (scripted) │    Run before each phase boundary.
                    ├─────────────┤
                 ┌──┴─────────────┴──┐
                 │ Integration Tests │  ← Many. Server hits real PostgreSQL.
                 │                   │    Mobile hits real SQLite. Sync tests
                 │                   │    run mobile → server.
                 ├───────────────────┤
              ┌──┴───────────────────┴──┐
              │      Unit Tests         │  ← Most. Projection logic, form engine,
              │                         │    event assembly, expression evaluation.
              │                         │    No I/O, no database.
              └─────────────────────────┘
```

### Contract tests

The `contracts/` directory is the source of truth. Both codebases validate against it:

| What's tested | How |
|--------------|-----|
| Event envelope structure | `envelope.schema.json` validated in server (Java JSON Schema library) and mobile (Dart JSON Schema library) |
| Sync protocol | Server integration tests exercise push/pull contracts. Mobile sync tests exercise the same contracts from the client side. |
| Shape definitions | Shape JSON validated against `shape-format.schema.json` in both codebases |

Contract tests run in CI. A schema change that breaks a consumer fails the build. This is the primary defense against mobile/server divergence.

### Sync-specific test scenarios

Sync is the highest-risk integration point. Dedicated test scenarios:

| Scenario | What it validates |
|----------|-------------------|
| Happy path push/pull | Events transfer correctly, watermarks advance |
| Duplicate push | Same events pushed twice → no duplicates (idempotency) |
| Out-of-order arrival | Events arrive in non-device-seq order → correctly stored |
| Partial push failure | Network drops mid-push → retry succeeds → no duplicates |
| Large batch | 500 events in one sync → pagination works correctly |
| Empty sync | Nothing to push, nothing to pull → no errors |
| First-time sync | Device with watermark 0 → receives all server events |

### Per-contract test enumeration

If all 21 contract tests pass, the system composes correctly. Each contract gets at least one dedicated test:

| Contract | Test |
|----------|------|
| C1 (ES → ALL: immutable, complete envelope) | Write event → read back → all 11 fields present. Attempt update → rejected. Attempt delete → rejected. |
| C2 (ES → PE: complete stream available) | Write N events for subject → PE rebuilds → state = f(N events). Delete projection → rebuild → same state. |
| C3 (ES → CD: accepts all events) | Submit event with stale reference → event persists (not rejected). |
| C4 (PE → CD: projected state for conflict evaluation) | Subject with events → PE derives alias-resolved state including authority context and workflow state → CD receives this projected state for anomaly evaluation. |
| C5 (PE → EE: entity projection values) | Projection provides `payload.*`, `entity.*`, `context.*` values → EE evaluates against them. |
| C6 (PE → SR: authority context) | Projection derives authority from assignment timeline → SR reads correct scope. |
| C7 (IR → PE: alias resolution, transitive closure) | Merge event creates alias → IR provides single-hop lookup (retired_id → surviving_id) → PE re-attributes events to canonical ID. Transitive merge (A→B, B→C) → A resolves to C in one hop. |
| C8 (CD → PE: anomaly detection, flag events) | Anomaly detected → CD creates `ConflictDetected` event with source event ref, flag category (from 9-category catalog), designated resolver → event persisted → PE excludes flagged source event from projection state. |
| C9 (CD → TE: flagged events gated from triggers) | Event with unresolved flag → TE does NOT fire triggers for it. Flag resolved → TE can now evaluate trigger conditions against the event. Each flag carries `auto_eligible` or `manual_only` classification. |
| C10 (SR → sync: scope-filtered) | Actor assigned scope X → sync returns only scope-X events. Events from scope Y absent. |
| C11 (ShR → PE: shape versions available) | Shape v1 and v2 both loaded → PE routes projection logic per version. |
| C12 (ShR → EE: field definitions) | Shape field definitions available → EE resolves field references against them. |
| C13 (ShR → DtV: shape for validation) | Shape submitted → DtV checks 60-field budget → pass/fail with violation detail. |
| C14 (ShR → CP: shape for packaging) | Shape validated → CP includes in atomic config package. |
| C15 (PR → PE: pattern state derivation) | Pattern transitions → PE derives current state at every step. |
| C16 (PR → CD: transition violation detection) | Invalid transition → CD creates `transition_violation` flag. |
| C17 (EE → TE: boolean condition evaluation) | Expression with 3 predicates → TE evaluates → trigger fires or not. |
| C18 (PR → DtV: pattern composition rules) | 5 pattern composition rules and structural constraints available → DtV enforces at deploy time → invalid composition rejected with specific violation list. |
| C19 (DtV → CP: validated config only) | Valid config → DtV accepts → CP receives and packages. Invalid config → DtV rejects with specific violation list → CP never receives it. |
| C20 (CP → sync: atomic config) | Config v2 deployed → device syncs → device config is v2 (shapes + triggers consistent). No v1 shapes + v2 triggers. |
| C21 (TE → ES: trigger output events) | Trigger fires → exactly one output event written to ES with `system` actor identity and `source_event_ref` → output type ≠ input type → event enters normal pipeline (detection, projection, sync). |

Tests are written incrementally — each phase adds the contract tests for its primitives.

### Pipeline tests

Cross-cutting pipelines get dedicated end-to-end tests at the phase that activates them:

| Pipeline | What the test does | Phase |
|----------|--------------------|-------|
| **Accept-and-flag lifecycle** | Event arrives → flag created → excluded from state → resolved → state re-derived including event. Full cycle. | 1 |
| **Detect-before-act** | Flagged event arrives → verify no trigger fires → verify state machine doesn't advance → resolve flag → trigger now fires. | 4 |
| **Configuration delivery** | Author shape → validate (pass) → package → sync → device uses new shape. Author invalid shape → validate (fail) → never reaches device. | 3 |

### Architecture compliance checks

Enforced in CI where possible, reviewed in code review where not:

| Check | How enforced |
|-------|-------------|
| No writes outside Event Store (write-path discipline [1-ST2]) | Package/module dependency rule: only ES module has write access to events table |
| No event type outside the 6-type vocabulary [4-S3] | Enum/const in shared contracts — compiler enforces |
| Envelope completeness (11 fields) | JSON Schema validation on every write path |
| Shape validation against `shape_ref` | Validation layer before Event Store append |
| Trigger depth ≤ 2 [4-S12] | Deploy-time validator check + runtime guard |
| Projection equivalence (server ↔ device) [E7] | Property-based contract test: same ordered event set → server PE (Java) and device PE (Dart) produce byte-identical JSON projection output. Phase 0c onward. |

---

## 9. CI/CD Pipeline

### From first commit

```
┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Push /  │ →  │ Lint + Build │ →  │    Tests     │ →  │   Publish    │
│   PR     │    │  (all modules│    │  (unit +     │    │  (artifacts) │
│          │    │   that       │    │  integration)│    │              │
│          │    │   changed)   │    │              │    │              │
└──────────┘    └──────────────┘    └──────────────┘    └──────────────┘
```

| Stage | Server | Mobile | Admin |
|-------|--------|--------|-------|
| Lint | `./mvnw checkstyle:check` or equivalent | `flutter analyze` | `ng lint` |
| Build | `./mvnw package -DskipTests` | `flutter build apk --debug` | `ng build` |
| Unit tests | `./mvnw test` | `flutter test` | `ng test` |
| Integration tests | `./mvnw test` (real PostgreSQL via docker-compose or CI service container) | `flutter test integration_test/` | — |
| Contract validation | JSON Schema validation in test suite | JSON Schema validation in test suite | — |
| Artifacts | Docker image (server) | APK (debug) | Static files |

### Phase 0a CI (minimum viable)

- GitHub Actions (or equivalent)
- On push: build server, run tests (PostgreSQL via GitHub Actions service container), validate contracts
- On merge to main: build Docker image, push to registry

### Progressive CI additions

| Phase | Addition |
|-------|----------|
| 0b | Flutter build + test job |
| 0c | Admin build. E2E smoke test (server + mobile client script). |
| 2 | Deployment to staging environment. Multi-actor integration test. |
| 3 | Config validation tests. Expression evaluator fuzz tests. |

---

## 10. Development Environment

### Local setup (one command)

```bash
# Clone and start
git clone <repo> datarun-platform
cd datarun-platform
docker compose up -d    # PostgreSQL + server
cd mobile && flutter run # Mobile on connected device / emulator
```

`docker-compose.yml` includes:
- PostgreSQL 16 with the event table schema applied via init script
- Server (Spring Boot) with hot-reload for development
- Volume mounts for persistent dev data

### Reference device

All mobile performance testing against: **Xiaomi Redmi 9A** (or equivalent Android Go device: 2GB RAM, Helio G25, Android 10+). This device represents the lower bound of the target hardware from [constraints.md](../constraints.md).

If physical device unavailable, use Android emulator configured to match: 2GB RAM, 720x1600 resolution, throttled CPU.

---

## 11. Quality Gates at Phase Boundaries

Every phase boundary is a checkpoint. The project does not advance to the next phase until all gates pass.

| Gate | Phase 0 | Phase 1 | Phase 2 | Phase 3 | Phase 4 |
|------|---------|---------|---------|---------|---------|
| All acceptance criteria pass as tests | ✓ | ✓ | ✓ | ✓ | ✓ |
| Zero contract violations in CI | ✓ | ✓ | ✓ | ✓ | ✓ |
| Sync test suite green | ✓ | ✓ | ✓ | ✓ | ✓ |
| Performance within budget on reference device | ✓ | ✓ | ✓ | ✓ | ✓ |
| Security checklist reviewed | — | ✓ | ✓ | ✓ | ✓ |
| Code review (self-review or AI-assisted) | ✓ | ✓ | ✓ | ✓ | ✓ |
| E2E demo scenario documented/recorded | ✓ | ✓ | ✓ | ✓ | ✓ |
| Deployment works from clean checkout | ✓ | ✓ | ✓ | ✓ | ✓ |
| IDR coverage for non-trivial choices | ✓ | ✓ | ✓ | ✓ | ✓ |

### Security checklist (Phase 1+)

- [ ] Sync endpoints reject unauthenticated requests (Phase 2+)
- [ ] No SQL injection vectors (parameterized queries only)
- [ ] No sensitive data in server logs (event payloads not logged at INFO level)
- [ ] Device storage encrypted at rest (Flutter default + SQLite encryption evaluation)
- [ ] Sync transport over HTTPS only
- [ ] Input validation on all API endpoints (malformed JSON, oversized payloads, invalid envelope)

---

## 12. Module Spec Template

One document per primitive, produced before that primitive is implemented. Used for agent briefing — a focused extraction from the architecture, not the full architecture.

Each module spec contains:

| Section | Content | Source |
|---------|---------|--------|
| **Invariant** | The one thing that must never be violated | primitives.md |
| **Contracts provided** | What this module promises to consumers | contracts.md — outgoing contracts |
| **Contracts consumed** | What this module depends on | contracts.md — incoming contracts |
| **Decided constraints** | Structural + strategy-protecting positions that bind this module | primitives.md constraint tables |
| **Implementation-grade decisions** | What the implementer is free to choose | boundary.md IG items |
| **Interface definition** | Inputs, outputs, error conditions | Derived from contracts + constraints |
| **Test contract** | How to verify this module satisfies its obligations | Derived from contracts + invariant |

Module specs are produced at the start of each phase, before implementation begins:

| Phase | Module specs produced |
|-------|---------------------|
| Phase 0 | Event Store, Projection Engine (happy-path scope) |
| Phase 1 | Identity Resolver, Conflict Detector |
| Phase 2 | Scope Resolver |
| Phase 3 | Shape Registry (full), Expression Evaluator, Deploy-time Validator, Config Packager |
| Phase 4 | Pattern Registry, Trigger Engine |

Specs are NOT produced upfront — implementation-grade decisions in one phase may reveal constraints that affect the next phase's specs.

---

## 13. Risk Register

| Risk | Likelihood | Impact | Mitigation | Phase |
|------|-----------|--------|------------|-------|
| Flutter + SQLite offline sync is harder than expected | MEDIUM | HIGH | Phase 0a builds the server first — if mobile sync is hard, the server API is stable and testable via curl/scripts independently. Spike if blocked > 2 days. | 0b |
| Shape-driven form rendering is complex | LOW | MEDIUM | Phase 0 uses exactly 5 field types. Widget registry starts minimal. Expand only when new shapes demand it. | 0b |
| PostgreSQL BIGSERIAL watermarks have gaps on conflict | LOW | LOW | Gaps are fine — watermarks are used for "after this" ordering, not contiguous sequence. Document this. | 0a |
| Spring Boot startup time on constrained CI runners | LOW | LOW | Keep the server lean — no unnecessary Spring starters. CI uses GitHub Actions service containers. | 0a |
| Admin UI adds scope without clear need | MEDIUM | MEDIUM | Phase 0c: minimal read-only views. Full Angular SPA starts in Phase 3 when config authoring is needed. Consider server-rendered pages for Phase 0–2. | 0c |
| Contract drift between mobile and server | MEDIUM | HIGH | Contract tests in CI from Phase 0a. Both codebases validate against same JSON Schema files. | All |

---

## 14. Implementation Decisions

Implementation decisions and discoveries are recorded as **Implementation Decision Records (IDRs)** in [`docs/decisions/`](../decisions/). Milestones (progress markers) stay in phase spec files (`phases/phase-N.md`).

See [`docs/decisions/README.md`](../decisions/README.md) for the full convention, and [`docs/decisions/INDEX.md`](../decisions/INDEX.md) for the searchable cross-reference.

### IG Decision Index

Summary of implementation-grade decisions, traced to IG items from [boundary.md](../architecture/boundary.md).

| IG# | Decision | IDR | Phase |
|-----|----------|-----|-------|
| | | *(populated as IG items are resolved)* | |

### IDR Triggers

An IDR is **required** when any of these events occur:

1. An IG decision is made (also update the index above)
2. Mid-phase discovery reaches Stage 2 or 3 of the [discovery protocol](#mid-phase-discovery-protocol) (spike or escalation)
3. Something is tried and abandoned after >1 hour invested
4. An environment or tooling workaround is adopted that affects how others build or test
5. Any choice where "why not X?" might be asked later

A **milestone** (not an IDR) is recorded in the phase spec when a quality gate passes or fails at a sub-phase or phase boundary.

---

## 15. Relationship to Other Documents

| Document | Role |
|----------|------|
| [plan.md](plan.md) | **What** gets built: technology stack, module map, phase summaries, IG schedule, primitive-to-module map |
| This document | **How** it gets built: decision authority, forbidden patterns, escalation triggers, quality gates, CI/CD, testing strategy, module spec template, risk mitigation, IDR convention |
| [ux-model.md](ux-model.md) | **How the mobile app works**: screen topology, navigation, component contracts, progressive disclosure |
| [phases/](phases/) | **Phase-specific detail**: scope, sub-phase breakdowns, quality gates, technical specs, acceptance criteria, milestones |
| [Architecture](../architecture/) | **What the platform IS**: primitives, contracts, cross-cutting concerns, boundaries — the authoritative reference for all implementation decisions |
