# Implementation

> **Historical phase-era surface.** Current work is Ship-driven; start with
> [../charter.md](../charter.md), then [../convergence/concept-ledger.md](../convergence/concept-ledger.md),
> then the current Ship spec under [../ships/](../ships/). The files in this
> directory explain prior implementation choices and phase plans. They are not
> current architecture authority unless a Ship, ADR, or charter claim cites
> them.

This directory records the phase-era implementation plan: technology stack,
repository structure, module boundaries, build sequencing, phase specs, and
IDRs. Treat the old "11 primitives" and phase-boundary language as historical
scaffolding. For current classifications, use the charter and concept ledger.

---

## Reading Guide

| Document | Purpose |
|----------|---------|
| [plan.md](plan.md) | Historical technology stack, repository structure, primitive-to-module map, 5-phase build order, IG decision schedule, agent collaboration plan |
| [execution-plan.md](execution-plan.md) | Historical execution mechanics: decision authority, forbidden patterns, escalation triggers, quality gates, CI/CD pipeline, testing strategy, module spec template, risk mitigation, IDR convention |
| [ux-model.md](ux-model.md) | **How the mobile app works**: screen topology, navigation flow, component contracts, offline patterns, progressive disclosure |
| [phases/](phases/) | Historical phase specs: scope, sub-phase breakdowns, quality gates, technical specs, acceptance criteria, milestones |
| [decisions/](../decisions/) | Implementation Decision Records (IDRs): micro-decisions with context, alternatives, consequences, and code traces. Separate from ADRs. |

---

## Relationship to Architecture

Phase-era architecture intentionally left implementation-grade decisions where
multiple valid approaches existed within the decided constraints. Those choices
were recorded as [IDRs](../decisions/) when made.

Current rule: ADRs and the charter govern architecture. IDRs explain
implementation choices and remain useful when touching code that depends on
them, but they do not classify platform concepts and do not supersede ADRs,
the charter, or Ship retros.
