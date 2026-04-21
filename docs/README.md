# Datarun — Documentation

## What This Project Is

Datarun is an operations platform for organizations that need to **collect information, coordinate work, track progress, and maintain accountability** across people, places, and time — reliably, even in environments with poor or no connectivity.

---

## Ambition

Organizations running field operations — in health, logistics, agriculture, humanitarian response, and many other domains — face a recurring problem: they need to collect structured data, assign responsibilities, review work, track what happened, and adapt as things change. Every new program or initiative rebuilds these foundations from scratch, producing fragmented tools that don't talk to each other and can't be trusted for decisions.

Datarun's ambition is to **eliminate that rebuilding**. Instead of bespoke systems for each operational need, provide a shared platform that handles the common operational substrate — the recording, the coordination, the oversight, the traceability — so that teams can focus on the specifics of their work rather than the infrastructure underneath it.

---

## Vision

**A platform where operational work is set up, not built.**

* Teams describe what information they need to collect, who is responsible for what, what the expected rhythms and oversight structures are, and what should happen when certain conditions are met.
* The platform takes care of the rest: reliable data capture (including offline), consistent tracking, clear accountability, and trustworthy history.
* When operational needs evolve — new information to collect, new responsibilities, new oversight rules — teams adapt the setup without rebuilding foundations.
* The platform feels like one coherent system, not a collection of disconnected tools. The same concepts, the same contracts, the same ways of seeing what happened and what's pending — regardless of whether the work is a simple monthly report or a complex multi-level distribution campaign.

### Core Commitments


| Commitment                     | What it means                                                                                                                                                                          |
| -------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Works without connectivity** | People doing field work can capture and work with information even when disconnected. When they reconnect, what they did is reconciled with what others did in the meantime.           |
| **Set up, not built**          | Standing up a new operational activity should feel like configuration — defining what to collect, who's responsible, what the expected cadence is — not like software development.   |
| **Trustworthy records**        | Every action is traceable: who did what, when, under what role, and in what context. Records stay meaningful even as the setup evolves over time.                                      |
| **One system, not many**       | Whether someone is recording observations, reviewing someone's work, tracking a distribution, or following up on a case — it all feels like the same system with consistent patterns. |
| **Grows without breaking**     | Starting simple is easy. Adding complexity — new oversight layers, new coordination patterns, new types of work — doesn't require rethinking or rebuilding what already exists.      |

---

## Documentation Structure

### Reading guide

An evaluator assessing the architecture should read in this order:

1. **Problem definition**: [Constraints](constraints.md) → [Scenar   ios](scenarios/README.md) → [Access Control](access-control-scenario.md)
2. **Viability analysis**: [Viability Assessment](viability-assessment.md)
3. **Design foundations**: [Principles](principles.md) → [Behavioral Patterns](behavioral_patterns.md)
4. **Architecture description**: [Architecture](architecture/) — the consolidated reference: primitives, contracts, cross-cutting concerns, boundaries
5. **Architecture decisions**: [ADR-001](adrs/adr-001-offline-data-model.md) → [002](adrs/adr-002-identity-conflict.md) → [003](adrs/adr-003-authorization-sync.md) → [004](adrs/adr-004-configuration-boundary.md) → [005](adrs/adr-005-state-progression.md) *(dependency order — each builds on the last)*
6. **Implementation**: [Implementation plan](implementation/plan.md) — technology stack, module boundaries, phased build order
7. **Decision reasoning** *(optional)*: [Exploration index](exploration/) — the event storms, stress tests, and coherence audits behind each ADR

### By area


| Area                                            | Contents                                                                                                                    |
| ------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| [Scenarios](scenarios/README.md)                | 21 real-world operational situations the platform must support — domain perspective, no solution prescription              |
| [Constraints](constraints.md)                   | Operational context and boundaries — target users, connectivity, scale, data sensitivity                                   |
| [Access Control](access-control-scenario.md)    | Cross-cutting concern: who can see and do what, under what circumstances                                                    |
| [Principles](principles.md)                     | 7 working principles — all confirmed through 5 ADRs                                                                        |
| [Viability Assessment](viability-assessment.md) | Platform viability analysis — vision vs. use cases gap analysis, GO/NO-GO*(pre dates adrs, and implementation decisions)*  |
| [Behavioral Patterns](behavioral_patterns.md)   | 12 behavioral patterns extracted from scenarios — the first narrowing step                                                 |
| [Architecture](architecture/)                   | Consolidated architecture description — 11 primitives, 21 contracts, 8 cross-cutting concerns, 29 boundary classifications |
| [ADRs](adrs/)                                   | 5 Architecture Decision Records — the decided architecture                                                                 |
| [Exploration](exploration/)                     | Decision analyses grouped by ADR — event storms, stress tests, coherence audits                                            |
| [Experiments](experiments/)                     | Prototypes and scenario walk-throughs                                                                                       |
| [Implementation](implementation/)               | Technology stack, module boundaries, phased build order, IDRs                                                               |
| [Checkpoints](checkpoints/)                     | Periodic project status progress checkpoints                                                                                |

---

## Current Status

The project has completed its **initial architecture sequence**:

1. ✅ Scenarios defined — 21 domain-pure, solution-independent scenarios
2. ✅ Cross-cutting concerns identified — offline work, access control
3. ✅ Architectural constraint decisions — five ADRs decided
   - **[ADR-001: Offline Data Model](adrs/adr-001-offline-data-model.md)** — immutable events, append-only, client-generated UUIDs (*[Reading Guide](exploration/guide-adr-001.md)*)
   - **[ADR-002: Identity Model and Conflict Resolution](adrs/adr-002-identity-conflict.md)** — 4 identity types, accept-and-flag, alias merges (*[Reading Guide](exploration/guide-adr-002.md)*)
   - **[ADR-003: Authorization and Selective Sync](adrs/adr-003-authorization-sync.md)** — assignment-based access, sync=scope, authority-as-projection (*[Reading Guide](exploration/guide-adr-003.md)*)
   - **[ADR-004: Configuration Paradigm and Boundary](adrs/adr-004-configuration-boundary.md)** — typed shapes, platform-fixed types, four-layer gradient (*[Reading Guide](exploration/guide-adr-004.md)*)
   - **[ADR-005: State Progression and Workflow](adrs/adr-005-state-progression.md)** — projection-derived state machines, Pattern Registry, composition rules (*[Reading Guide](exploration/guide-adr-005.md)*)
4. ✅ Architecture Description — consolidated reference in [architecture/](architecture/) (11 primitives, 21 contracts, 8 cross-cutting concerns, 29 boundary classifications)
5. 🔄 [Implementation](implementation/README.md).
