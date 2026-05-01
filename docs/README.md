# Datarun — Documentation

> **Current authority**: Start with [charter.md](charter.md) for settled
> architecture and status, then [convergence/concept-ledger.md](convergence/concept-ledger.md),
> then the current Ship spec under [ships/](ships/). Older architecture,
> implementation-phase, and exploration docs explain lineage; they are not
> current truth unless the charter or a decided ADR re-promotes them.

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

For current work, read in this order:

1. **Settled state**: [Charter](charter.md)
2. **Classification inventory**: [Concept Ledger](convergence/concept-ledger.md)
3. **In-flight work**: current Ship spec under [Ships](ships/)
4. **Open verification debt**: [Flagged Positions](flagged-positions.md)
5. **Owning decisions**: relevant ADRs under [ADRs](adrs/)

For historical reconstruction, read in this order:

1. **Problem definition**: [Constraints](constraints.md) → [Scenarios](scenarios/README.md) → [Access Control](access-control-scenario.md)
2. **Viability analysis**: [Viability Assessment](viability-assessment.md)
3. **Design foundations**: [Principles](principles.md) → [Behavioral Patterns](behavioral_patterns.md)
4. **Exploration proof**: [Exploration index](exploration/) and ADR guides
5. **Initial ADR sequence**: [ADR-001](adrs/adr-001-offline-data-model.md) → [002](adrs/adr-002-identity-conflict.md) → [003](adrs/adr-003-authorization-sync.md) → [004](adrs/adr-004-configuration-boundary.md) → [005](adrs/adr-005-state-progression.md)
6. **Convergence ADRs**: [ADR-006](adrs/adr-006-flag-semantics.md) → [ADR-007](adrs/adr-007-envelope-type-closure.md) → [ADR-008](adrs/adr-008-envelope-reference-fields.md) → [ADR-009](adrs/adr-009-platform-fixed-vs-deployer-configured.md)

### By area


| Area                                            | Contents                                                                                                                    |
| ------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| [Charter](charter.md)                           | Current decided state and Ship cadence. Start here for settled truth.                                                       |
| [Concept Ledger](convergence/concept-ledger.md) | Classification inventory used by convergence and charter generation.                                                        |
| [Ships](ships/)                                 | Current scenario-driven execution cadence, specs, evidence, and retros.                                                     |
| [Flagged Positions](flagged-positions.md)       | Append-only register for deferred verification and quiet-decision markers.                                                  |
| [Scenarios](scenarios/README.md)                | 21 real-world operational situations the platform must support — domain perspective, no solution prescription              |
| [Constraints](constraints.md)                   | Operational context and boundaries — target users, connectivity, scale, data sensitivity                                   |
| [Access Control](access-control-scenario.md)    | Cross-cutting concern: who can see and do what, under what circumstances                                                    |
| [Principles](principles.md)                     | 7 working principles — all confirmed through 5 ADRs                                                                        |
| [Viability Assessment](viability-assessment.md) | Platform viability analysis — vision vs. use cases gap analysis, GO/NO-GO*(pre dates adrs, and implementation decisions)*  |
| [Behavioral Patterns](behavioral_patterns.md)   | 12 behavioral patterns extracted from scenarios — the first narrowing step                                                 |
| [Architecture](architecture/)                   | Historical/reference architecture surface. Do not use over the charter for current classification.                         |
| [ADRs](adrs/)                                   | Architecture Decision Records. ADR-006 through ADR-009 repair convergence-era classification drift.                        |
| [Exploration](exploration/)                     | Historical decision proof — event storms, stress tests, coherence audits.                                                  |
| [Experiments](experiments/)                     | Prototypes and scenario walk-throughs                                                                                       |
| [Implementation](implementation/)               | Historical phase-era implementation plans and IDRs. Current code work is Ship-driven.                                      |
| [Checkpoints](checkpoints/)                     | Historical project status snapshots.                                                                                        |
