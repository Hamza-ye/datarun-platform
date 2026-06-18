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

An evaluator assessing product intent should read the problem-space sources first. An agent assessing current architecture or implementation status should use `AGENTS.md` and `docs/status.md` first; the active post-Phase-4 working surface is `docs/agent-working-surface/README.md`.

Before creating a new durable document, use
[Documentation Organization And Traceability](documentation-organization.md)
to select one canonical home and required acceptance trace.
For repository progress and commit boundaries, use
[Commit And Progress Workflow](commit-workflow.md).

1. **Problem definition**: [Constraints](constraints.md) → [Scenarios](scenarios/README.md) → [Access Control](access-control-scenario.md)
2. **Viability analysis**: [Viability Assessment](viability-assessment.md)
3. **Design foundations**: [Principles](principles.md) → [Behavioral Patterns](behavioral_patterns.md)
4. **Architecture description**: [Architecture](architecture/) — the consolidated reference: primitives, contracts, cross-cutting concerns, boundaries
5. **Architecture decisions**: [Canonical Decision Ledger](architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md) via its [README index](architecture/adrs-decisions-canonical-ledger/README.md). The old ADR files are provenance, not the active agent-facing authority.
6. **Rationale and routing**: [Decision Anchor Layer](agent-working-surface/decision-anchor-layer/README.md) — non-authoritative DEC anchors, gap classification, and closure routing; the CDL still wins on decisions.
7. **Product/platform specifications**: [Specifications](specifications/README.md) — accepted user-visible and exact platform behavior below architecture/contracts.
8. **Operations**: [Operations](operations/README.md) — policies, executable runbooks, and rehearsal evidence.
9. **Implementation**: [Implementation plan](implementation/plan.md) and [Module Interface Baseline](implementation/module-interfaces.md) — technology stack, module boundaries, phased build order, and implemented boundary map
10. **Decision reasoning** *(optional)*: [Exploration index](exploration/) — the event storms, stress tests, and coherence audits behind each ADR

### By area


| Area                                            | Contents                                                                                                                    |
| ------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| [Scenarios](scenarios/README.md)                | 27 real-world operational situations the platform must support, including post-Phase-4 thickening scenarios — domain perspective, no solution prescription |
| [Constraints](constraints.md)                   | Operational context and boundaries — target users, connectivity, scale, data sensitivity                                   |
| [Access Control](access-control-scenario.md)    | Cross-cutting concern: who can see and do what, under what circumstances                                                    |
| [Principles](principles.md)                     | 7 working principles — all confirmed through 5 ADRs                                                                        |
| [Viability Assessment](viability-assessment.md) | Platform viability analysis — vision vs. use cases gap analysis, GO/NO-GO*(pre dates adrs, and implementation decisions)*  |
| [Behavioral Patterns](behavioral_patterns.md)   | 12 behavioral patterns extracted from scenarios — the first narrowing step                                                 |
| [Architecture](architecture/)                   | Consolidated architecture description — 11 primitives, 21 contracts, 8 cross-cutting concerns, 29 boundary classifications |
| [Canonical Decision Ledger](architecture/adrs-decisions-canonical-ledger/) | Active architecture decision authority from Phase 4 closure forward |
| [Decision Anchor Layer](agent-working-surface/decision-anchor-layer/README.md) | Active non-authoritative DEC-anchor and gap-routing surface for steward work |
| [Specifications](specifications/README.md) | Accepted product behavior and platform-detail specifications under architecture/contracts |
| [Operations](operations/README.md) | Operational policies, executable runbooks, rehearsal plans, and dated evidence |
| [Documentation Organization](documentation-organization.md) | Canonical homes, metadata, indexes, lifecycle, and supersession for durable outputs |
| [Commit Workflow](commit-workflow.md) | Conventional commit taxonomy and route/implement/accept/checkpoint progress flow |
| [ADRs](adrs/)                                   | Retired/provenance Architecture Decision Records; use only when routed by current docs or a drift investigation            |
| [Exploration](exploration/)                     | Decision analyses grouped by ADR — event storms, stress tests, coherence audits                                            |
| [Experiments](experiments/)                     | Prototypes and scenario walk-throughs                                                                                       |
| [Implementation](implementation/)               | Technology stack, module boundaries, phased build order, IDRs                                                               |
| [Checkpoints](checkpoints/)                     | Periodic project status progress checkpoints                                                                                |

---

## Current Status

The project has completed its initial architecture sequence and Phase 4 implementation. Current implementation standing is tracked by [docs/status.md](status.md) and the post-Phase-4 working surface, especially the [Baseline Acceptance Register](agent-working-surface/baseline-acceptance-register.md), [Platform Next Work Backlog](agent-working-surface/platform-next-work-backlog.md), and [Module Interface Baseline](implementation/module-interfaces.md).

Current high-level standing:

1. Initial scenarios are defined: 27 scenario entries, including post-Phase-4 thickening scenarios.
2. Architecture authority is consolidated: the CDL is the active authority from Phase 4 closure forward.
3. Phase 4 is complete: workflow/policy implementation landed and the completion audit is green.
4. Accepted baseline standing includes BAR-001 through BAR-015 and BAR-104, including config package delivery and production OIDC/JWT/Keycloak authority.
5. Post-Phase-4 runtime evidence now covers S00, S19, S21, S22, S23, S26, and S27.
6. Assignment administration has moved beyond containment-only exposure: accepted behavior is now in [Assignment Scope And Administration](specifications/platform/assignment-scope-and-administration.md), including server-side `assignment_admin.create` / `assignment_admin.end` command capability and same-assignment containment.
7. Remaining production-shaping work should route through `docs/status.md`,
   the working-surface backlog, the decision-anchor layer, and the canonical
   specification/operations homes rather than legacy phase chronology.
