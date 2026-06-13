# Documentation Organization And Traceability

Status: active documentation standard

Authority: this document governs where new durable documentation outputs live
and how they remain traceable. It does not change architecture, contracts,
accepted platform behavior, or backlog standing.

Commit sequencing and progress-state transitions are governed by
`docs/commit-workflow.md`.

## Core Rule

Organize durable documents by their role and authority, not by workshop,
milestone, agent, date, or NW number.

```text
pressure and authority
-> NW row
-> bounded prompt
-> one canonical durable output
-> verification or rehearsal evidence
-> NW acceptance
-> fold-forward updates to materially affected active surfaces
```

Prompts and non-binding artifacts route work. They are not substitutes for
accepted specifications, policies, procedures, implementation, or evidence.

## Canonical Homes

| Output | Canonical home | Purpose |
|---|---|---|
| Architecture decision | CDL successor; delegated IDR when appropriate | Structural authority and durable decision rationale. |
| Process/wire contract | `contracts/` | Machine- or process-boundary schema and protocol authority. |
| Product specification | `docs/specifications/product/` | Accepted user-visible behavior, language, flows, and product acceptance criteria. |
| Platform specification | `docs/specifications/platform/` | Accepted exact platform behavior inside existing architecture and contract boundaries. |
| Implementation design or phase specification | `docs/implementation/` | Module design, engineering approach, phased delivery, and implementation acceptance. |
| Operational policy | `docs/operations/policies/` | Human or deployment-owner choices such as ownership, retention posture, RPO/RTO, support, approval, and escalation. |
| Runbook | `docs/operations/runbooks/` | Repeatable executable operating procedure with prerequisites, verification, failure handling, and rollback/forward-fix guidance. |
| Rehearsal plan or record | `docs/operations/rehearsals/` | Planned exercise or dated execution evidence for a runbook or recovery/incident scenario. |
| Product/problem evidence | `docs/scenarios/`, user-fit packets, or a routed research location | User need, journey, vocabulary, validation, and deployment pressure. |
| Non-binding exploration or routing result | `docs/agent-working-surface/artifacts/` | Bounded analysis that selects or defers a durable successor. |
| Execution packet | `docs/agent-working-surface/prompts/` | Files, guardrails, tests, stop conditions, and expected output for one NW slice. |
| Current standing and evidence route | BAR, NW backlog, `docs/status.md` | Current accepted/deferred standing and links to durable outputs. |
| Historical snapshot | `docs/checkpoints/` | Point-in-time project state; never the active source of truth. |

Existing documents are not moved merely to satisfy this taxonomy. When an
existing document is already the accepted canonical surface, keep it in place
and link it from the relevant index. New outputs should use the canonical homes
above unless the routing analysis records a specific reason not to.

## Specification Split

Use a product specification when the question is what users see, understand,
or accomplish. Use a platform specification when the question is exact
platform behavior under accepted architecture.

Examples:

| Question | Home |
|---|---|
| What sync states and recovery actions are shown to a field user? | Product specification. |
| How freshness and unresolved flags are calculated for an accepted read-side view? | Platform specification. |
| Does a new aggregate bypass detail-level access? | Architecture decision first. |
| What JSON crosses a process boundary? | `contracts/`. |
| How a service or UI is implemented? | `docs/implementation/` plus code/tests. |

A single NW may produce both product and platform specifications when both are
required, but each concern still has one canonical document. Cross-link them;
do not duplicate normative text.

## Operations Split

Keep these outputs separate:

| Type | Answers | Must contain |
|---|---|---|
| Policy | What the organization chooses and who owns it. | Scope, owner, decision, constraints, review trigger, exceptions/escalation, related authority. |
| Runbook | How an operator performs a repeatable procedure. | Preconditions, inputs, ordered steps, observable checks, failure/stop conditions, rollback or forward-fix posture, evidence to retain. |
| Rehearsal plan | What will be exercised and how success will be judged. | Runbook/version under test, environment, scenarios, injects, acceptance criteria, cleanup, evidence plan. |
| Rehearsal record | What actually happened. | Date, environment, commit/artifact versions, participants or operator role, commands/procedures used, observed results, failures, retained evidence, follow-up NW rows. |

Do not embed one-time rehearsal results in a runbook. Improve the reusable
runbook from the findings and keep the dated record as evidence.

## Required Document Header

Every new durable specification, policy, runbook, or rehearsal document starts
with:

```text
Status: draft | in_review | accepted | superseded | retired
Document type: product_spec | platform_spec | operational_policy | runbook | rehearsal_plan | rehearsal_record
Owner: <role or owning team>
Source: <NW row and prompt>
Authority: <CDL/DEC/IDR/contracts/BAR inputs, or "none; operates within ...">
Last reviewed: YYYY-MM-DD
Supersedes: <path or none>
Related: <durable docs, contracts, code, and evidence>
```

Use `accepted` only when the owning NW exit condition and required evidence are
complete. A document may be operationally active while remaining subordinate
to CDL, contracts, and accepted implementation standing.

## Naming

- Durable specifications and procedures use semantic kebab-case names:
  `offline-sync-status.md`, `production-deployment-runbook.md`,
  `backup-recovery-policy.md`.
- NW numbers belong in metadata and routing links, not durable filenames.
- Prompts and non-binding artifacts keep the `NW-###-...` prefix.
- Rehearsal records use
  `YYYY-MM-DD-<procedure>-<environment>.md`.
- Do not create a new directory per NW, workshop stage, agent role, deployment
  candidate, or review gate.

## Index And Link Rules

1. Every durable document is listed in the nearest directory `README.md`.
2. The index records status, owner, source NW, and supersession standing.
3. The NW row links the prompt, durable output, verification, and successors.
4. `docs/status.md` links only currently relevant routes, not every artifact.
5. BAR changes only when implementation capability standing changes.
6. DEC anchors and the gap playbook change only when their routing or accepted
   extension inputs materially change.
7. Use links instead of copying normative sections into multiple documents.

No accepted document should be discoverable only through git history, a
checkpoint, a prompt, or an artifact.

## Change And Supersession

- Clarifications that do not change meaning may edit an accepted document
  in place with a dated history note.
- Meaningful behavior or policy changes require an NW route and fresh
  acceptance evidence.
- Incompatible replacement creates a new document or explicit new version;
  mark the old document `superseded` and link both directions.
- Do not delete accepted evidence merely because a newer procedure exists.
- Historical workshops, reviews, and checkpoints remain provenance and must
  not become parallel active specifications.

## Acceptance Checklist

Before accepting a documentation-producing NW:

- the output classification is explicit;
- the durable file is in the canonical home;
- the nearest index lists it;
- the header names owner, source, authority, and review date;
- normative content exists in one place;
- verification or rehearsal evidence is linked;
- superseded documents are marked and cross-linked;
- the NW exit condition records the result and residual route;
- only materially affected active routers were updated;
- commit roles follow `docs/commit-workflow.md`;
- `git diff --check` passes.
