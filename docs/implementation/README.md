# Implementation

> How the architecture gets built. Technology choices, module boundaries, build order, and phase specifications.

The [Architecture Description](../architecture/) defines **what** to build — 11 primitives, 21 contracts, 8 cross-cutting concerns. This directory defines **how** — technology stack, repository structure, and a phased build order that produces a working system at every phase boundary.

---

## Reading Guide

| Document | Purpose |
|----------|---------|
| [plan.md](plan.md) | **What** gets built: technology stack, repository structure, primitive-to-module mapping, 5-phase build order, IG decision schedule, agent collaboration plan |
| [execution-plan.md](execution-plan.md) | **How** it gets built: decision authority, forbidden patterns, escalation triggers, quality gates, CI/CD pipeline, testing strategy (21 contract tests + pipeline tests + compliance checks), module spec template, risk mitigation, IDR convention |
| [ux-model.md](ux-model.md) | **How the mobile app works**: screen topology, navigation flow, component contracts, offline patterns, progressive disclosure |
| [phases/](phases/) | **Phase specs**: scope, sub-phase breakdowns, quality gates, technical specs, acceptance criteria, milestones. One file per phase, created at phase start. |
| [decisions/](../decisions/) | **Why this way**: Implementation Decision Records (IDRs) — micro-decisions with context, alternatives, consequences, and code traces. Separate from ADRs (architecture-grade). |

---

## Relationship to Architecture

The architecture intentionally left 15 items at implementation-grade — decisions where multiple valid approaches exist within the decided constraints ([boundary.md § Implementation-Grade Items](../architecture/boundary.md#3-implementation-grade-items)). Those decisions are recorded as [IDRs](../decisions/) when they're made during implementation.

Implementation decisions follow the same discipline as architecture decisions: stated, traced, reversible where possible. The difference: architecture decisions constrain stored data and contracts across offline devices. Implementation decisions constrain code and tooling — they're reversible with engineering effort, not data migration.
