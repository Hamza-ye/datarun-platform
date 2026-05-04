# Datarun — Documentation

> **Current platform-spec entrypoint**:
> [`platform-spec-kernels/professional-baseline/README.md`](platform-spec-kernels/professional-baseline/README.md).
>
> For atomization work, do not start from `charter.md`, `flagged-positions.md`,
> `ships/`, convergence docs, architecture docs, implementation docs, or git
> history. Those surfaces are legacy/status/implementation context and can leak
> stale decisions into the current baseline.

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

### Reading Guide

For platform-spec atomization, read in this order:

1. **Operating model**: [Professional Baseline README](platform-spec-kernels/professional-baseline/README.md)
2. **Accepted baseline**: [Architecture Baseline v0](platform-spec-kernels/professional-baseline/04-architecture-baseline-v0.md)
3. **Open gaps**: [Decision Gap Register](platform-spec-kernels/professional-baseline/05-decision-gap-register.md)
4. **Boundary routing**: [System Boundary Map](platform-spec-kernels/professional-baseline/07-system-boundary-map.md)
5. **Control overlays**:
   [Identity](platform-spec-kernels/professional-baseline/09-identity-boundary-control.md),
   [Conflict/Flag/Offline](platform-spec-kernels/professional-baseline/15-conflict-flag-offline-boundary-control.md),
   and [Operational Constraints](platform-spec-kernels/professional-baseline/16-operational-constraints-boundary-control.md)

For domain and lineage background only, read:

1. **Problem definition**: [Constraints](constraints.md) → [Scenarios](scenarios/README.md) → [Access Control](access-control-scenario.md)
2. **Viability analysis**: [Viability Assessment](viability-assessment.md)
3. **Design foundations**: [Principles](principles.md) → [Behavioral Patterns](behavioral_patterns.md)
4. **Initial ADR sequence**: [ADR-001](adrs/adr-001-offline-data-model.md) → [002](adrs/adr-002-identity-conflict.md) → [003](adrs/adr-003-authorization-sync.md) → [004](adrs/adr-004-configuration-boundary.md) → [005](adrs/adr-005-state-progression.md)
5. **Assessed later ADRs**: use the professional-baseline assessments, not the later ADRs as automatic authority.

### By area


| Area                                            | Contents                                                                                                                    |
| ------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| [Professional Baseline](platform-spec-kernels/professional-baseline/README.md) | Current entrypoint for platform-spec atomization. |
| [Platform Spec Kernels](platform-spec-kernels/) | Extraction state, staging kernels, ADR-001 through ADR-005 closure register, and professional-baseline controls. |
| [Scenarios](scenarios/README.md)                | 21 real-world operational situations the platform must support — domain perspective, no solution prescription              |
| [Constraints](constraints.md)                   | Operational context and boundaries — target users, connectivity, scale, data sensitivity                                   |
| [Access Control](access-control-scenario.md)    | Cross-cutting concern: who can see and do what, under what circumstances                                                    |
| [Principles](principles.md)                     | 7 working principles — all confirmed through 5 ADRs                                                                        |
| [Viability Assessment](viability-assessment.md) | Platform viability analysis — vision vs. use cases gap analysis, GO/NO-GO*(pre dates adrs, and implementation decisions)*  |
| [Behavioral Patterns](behavioral_patterns.md)   | 12 behavioral patterns extracted from scenarios — the first narrowing step                                                 |
| [ADRs](adrs/)                                   | Decision records. For atomization, use them through the professional-baseline closure and assessment docs.                 |
| Legacy/status surfaces                          | `charter.md`, `flagged-positions.md`, `ships/`, convergence docs, architecture docs, implementation docs, experiments, and checkpoints are not atomization authority. |
