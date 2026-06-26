# Product Journey And Slice Sequencing

Status: active product-planning guidance
Document type: agent_workflow
Owner: product steward
Authority: planning guidance only; creates no product behavior, platform
authority, implementation scope, or acceptance by itself

## Purpose

Develop Datarun as one coherent configurable system:

- the same product concepts;
- the same interaction grammar;
- reusable behavioral compositions;
- deployment-specific terminology and configuration;
- progressively delivered, usable journeys.

Scenarios provide product evidence and composition pressure. They must not
become separate domain products or silently redefine platform mechanisms.

## Operating Principles

1. Maintain one active Product Goal.
2. Start from user outcomes and journeys, not screens, APIs, tables, or
   existing tests.
3. Treat the product conceptual model as a validated product definition, not
   architecture authority.
4. Treat code and tests as behavioral evidence, not product semantics.
5. Keep one active implementation slice and small reviewable PRs.
6. Every increment must advance a defined journey and meet the shared Product
   Definition of Done.
7. Security, authorization, reliability, offline behavior, and operations are
   part of delivery where relevant, not postponed final phases.
8. Planning must produce an executable decision. Do not create prompts whose
   only outcome is another prompt.

## Minimum Product Increment Done

An increment is not done unless it has:

- stated journey and user outcome;
- acceptance criteria tied to the user-success view;
- validation evidence;
- security, authorization, and scope impact checked where relevant;
- offline, sync, and reliability impact checked where relevant;
- open assumptions classified;
- deferrals recorded with trigger and consequence;
- no fixture, test, or domain vocabulary promoted to product authority.

## Delivery Sequence

### 1. Establish The Product Goal

Define:

- target users and responsibilities;
- outcome to achieve;
- representative scenario families;
- why the goal matters now;
- measurable success;
- exclusions that genuinely protect focus.

The Product Goal remains stable while its backlog and implementation details
emerge.

### 2. Define Representative Journeys

For each selected journey, capture:

- initiating condition;
- user intent;
- actors and responsibilities;
- end-to-end steps;
- online and offline touchpoints;
- visible states and decisions;
- attention, failure, recovery, and correction paths;
- user-success view;
- unresolved assumptions;
- evidence already present.

Begin with a small representative portfolio: a simple scenario, a composite
scenario, and a materially different scenario that tests domain neutrality.

### 3. Derive And Stress-Test The Product Model

Extract candidate common concepts and interactions from the journeys.

For every journey step, map:

```text
user intent
product concept
user action
visible state
configured/domain label
reusable interaction pattern
supporting platform mechanism
existing evidence
missing capability
```

Classify every mismatch as:

1. Missing product or UI behavior.
2. Missing reusable interaction pattern.
3. Deployment-specific configuration.
4. Genuine platform-mechanism or architecture gap.
5. Unsupported assumption requiring evidence.

Do not force a journey into an unsuitable universal abstraction.

### 4. Define The Candidate Boundary

The product handoff must state:

- Product Goal contribution;
- journeys and user outcomes covered;
- usable increment being delivered;
- journey-specific acceptance criteria;
- shared Product Definition of Done;
- dependencies and known risks;
- evidence already available;
- capabilities explicitly deferred;
- ordered implementation slices.

### 5. Execute Progressive Vertical Slices

Each slice must be:

- independently reviewable;
- valuable toward the journey;
- testable through observable user behavior;
- integrated with existing increments;
- small enough for rapid feedback;
- explicit about assumptions and exclusions.

Agents receive the whole goal and journey context but implement only the
selected slice.

### 6. Review The Increment

After implementation:

- run automated gates;
- exercise the relevant journey;
- compare observed behavior with user-success criteria;
- record `PROVEN`, `PARTIAL`, `NOT_RUN`, or `FAILED`;
- inspect newly discovered gaps;
- adapt the backlog and conceptual model;
- accept, amend, park, or replace the route.

Passing technical tests alone does not prove journey or product fitness.

## Deferring Capabilities

A deferred item must record:

```text
Capability:
Related user and journey step:
Why it matters:
Current evidence:
Why it is not required in the present increment:
Consequence of deferral:
Dependency or trigger:
Expected reconsideration point:
```

`Future decision`, `not production`, and `out of scope` are insufficient by
themselves. Deferral must preserve the reason, impact, and wake-up condition.

## Agent Guardrails

Agents must not:

- infer product semantics from fixtures or passing tests;
- introduce domain-specific UI as platform vocabulary;
- create a new mechanism for an existing behavioral composition;
- treat an unselected capability as prohibited;
- bury discoveries in PR prose or historical artifacts;
- optimize a local implementation while losing the journey goal.

Any discovery affecting future work must become an ordered backlog item,
accepted gap, explicit deferral, or rejected option with rationale.

## Recommended Sequence After NW-165

1. Product Goal and representative journey portfolio

   Select the next goal and representative journeys. No implementation.

2. Journey/user-fit definition and evidence reconciliation

   Determine what PC1 and later work already prove, what remains partial, and
   what success requires.

3. Cross-scenario product conceptual model and interaction grammar

   Consolidate existing viability, behavioral-pattern, user-fit, and UX
   material. Stress-test it against representative and composite scenarios.

4. Product candidate handoff and ordered slice plan

   Select one usable journey increment and decompose it into small vertical
   slices.

5. Implementation wave

   Execute slices one at a time, review the resulting journey, and adapt.

This gives future agents enough strategic context to make coherent local
decisions while preventing another documentation-only planning loop.
