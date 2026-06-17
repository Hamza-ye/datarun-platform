# Solo Developer AI-Agent Operating Framework

Status: non-authoritative operating artifact
Owner: solo developer
Source: user request, 2026-06-18
Authority: none; practical framework for day-to-day solo developer work
Related: `AGENTS.md`, `docs/agent-working-surface/README.md`, `docs/commit-workflow.md`, `docs/documentation-organization.md`

## Purpose

This manual defines a practical operating framework for a solo developer using
AI agents as professional contributors for planning, analysis, implementation,
review, risk identification, gap identification, decision routing, and objective
alignment under pressure.

It is intentionally lightweight. The goal is to keep daily work moving while
preserving professional discipline: clear work packages, explicit decisions,
bounded analysis, visible risks, and closure with evidence.

## NW Definition

`NW` means **Next Work**.

An NW is the core executable work unit in this framework:

> A bounded work package with a clear objective, scope, inputs, acceptance
> criteria, risks, owner, and closure state.

An NW is not just a task title. It is the smallest useful unit that can be
planned, executed, reviewed, and closed with evidence.

An NW should answer:

| Field | Meaning |
|---|---|
| ID | Stable identifier, such as `NW-081`. |
| Title | Short outcome-oriented name. |
| Type | What kind of work it is. |
| Objective | What must become true when this is complete. |
| Scope | What is included and excluded. |
| Inputs | Files, decisions, evidence, user goals, constraints, or prior work. |
| Owner | Usually the solo developer; AI agents are contributors/reviewers. |
| Acceptance criteria | Verifiable conditions for closure. |
| Risks/gaps | What could break, be unknown, or require routing. |
| Dependencies | What must exist or be decided first. |
| Tests/evidence | How completion will be proven. |
| State | Candidate, ready, in progress, blocked, accepted, deferred, or superseded. |

## Operating Principles

Use these principles to keep the framework professional without overbuilding it.

| Principle | Practical rule |
|---|---|
| One active work unit | Keep one primary NW active unless work is truly independent. |
| Route before building | If pressure could change architecture, contracts, policy, or product behavior, classify before implementation. |
| Evidence closes work | Code written, notes produced, or tests passing are not enough unless they satisfy the NW acceptance criteria. |
| AI agents advise and execute | Agents can analyze, propose, implement, and review; the solo developer owns acceptance and prioritization. |
| Prefer canonical homes | Decisions, specs, contracts, runbooks, and evidence should land in the right durable place, not inside chat history. |
| Avoid ceremony drift | Add process only when it prevents a real recurring failure. |
| Preserve reversibility | The more irreversible the change, the stronger the routing and review should be. |

## 1. Work Management Structure

### Work-Item Taxonomy

Use the type to decide how much routing, review, and evidence is needed.

| Type | Use when | Typical output | Closure evidence |
|---|---|---|---|
| `implementation` | Code or runtime behavior changes. | Code, tests, migrations, local docs if needed. | Tests, diff review, behavior evidence. |
| `bug_fix` | Existing intended behavior is broken. | Focused fix and regression test. | Failing case now passes; no unrelated behavior changed. |
| `refactor` | Structure changes without intended behavior change. | Code cleanup, module boundary improvement. | Tests unchanged/pass; behavior unchanged. |
| `technical_debt` | Known code/design weakness needs correction. | Fix, refactor, or deferred NW row. | Debt reduced or explicitly routed. |
| `analysis` | The right action is unclear. | Analysis note or routing artifact. | Recommendation with next route. |
| `architecture_decision` | Structural decision is needed before work. | Decision record or decision-log entry. | Decision accepted, rejected, or deferred. |
| `platform_spec` | Accepted behavior needs exact platform definition. | Platform specification. | Spec indexed and acceptance criteria clear. |
| `product_spec` | User-visible behavior needs definition. | Product specification. | Product outcome and acceptance criteria clear. |
| `contract_change` | Schema, API, protocol, or cross-boundary shape changes. | Contract update and compatibility notes. | Contract tests or compatibility evidence. |
| `operations_policy` | Human/process/deployment ownership choice is needed. | Policy document. | Owner, constraints, review triggers clear. |
| `runbook` | Repeatable procedure is needed. | Executable runbook. | Procedure reviewed or rehearsed. |
| `rehearsal` | Operational proof is needed. | Rehearsal plan/record. | Dated observed results. |
| `documentation_hygiene` | Docs route, index, or status needs cleanup. | Wording/link/index updates. | Diff is scoped; no behavior change. |
| `review` | Independent quality/risk check. | Findings list or approval. | Findings resolved, routed, or accepted as residual risk. |

### Decision Taxonomy

Not every choice needs a formal decision record. Use this taxonomy to avoid both
under-routing and over-planning.

| Decision type | Definition | Example | Artifact |
|---|---|---|---|
| Local implementation choice | Reversible code-level choice inside accepted boundaries. | Helper function name, small class split. | Commit/diff only. |
| Technical design choice | Meaningful implementation shape but no platform semantics change. | Cache strategy, module boundary, retry structure. | Implementation plan or design note. |
| Product behavior decision | User-visible behavior or language changes. | What status is shown for offline pending work. | Product spec or NW acceptance note. |
| Platform behavior decision | Exact accepted behavior inside existing architecture. | How freshness is calculated. | Platform spec. |
| Contract decision | Wire/schema/API/protocol compatibility changes. | Add a field to a response. | Contract update and tests. |
| Architecture decision | Structural authority, stored truth, event semantics, scope, or irreversible compatibility changes. | New event type, authority source, workflow state truth. | Decision log / architecture decision. |
| Operational decision | Human ownership, deployment, recovery, support, or security posture. | Backup RPO/RTO or incident owner. | Policy/runbook/rehearsal. |

### Backlog And Gap Taxonomy

Separate work from uncertainty. A backlog item is executable or can become
executable. A gap is something unknown, missing, or blocked.

| Category | Meaning | What to do |
|---|---|---|
| Backlog candidate | Possible work, not yet selected. | Keep short; refine only when likely. |
| Ready NW | Work can start with bounded inputs and acceptance criteria. | Execute or schedule. |
| Active NW | Currently being worked. | Protect focus; avoid expanding scope. |
| Product/problem gap | Need or user outcome is unclear. | Do discovery, scenario, or product analysis. |
| Architecture gap | Structural decision is missing. | Route to decision before implementation. |
| Platform-spec gap | Behavior is accepted in principle but underspecified. | Write/specify exact behavior. |
| Implementation/tooling gap | Engineering work needed inside accepted boundaries. | Plan/implement/test. |
| Operational policy gap | Human/process/security/deployment choice is missing. | Define policy or runbook. |
| Evidence gap | Claim lacks proof. | Run tests, inspect code, rehearse, or collect evidence. |
| Technical debt | Known weakness that may slow or risk future work. | Fix now if local; otherwise track as NW. |
| Risk | Possible future harm, not necessarily current work. | Add to risk register or NW risk field. |

### Readiness States

Use explicit states so work does not silently drift.

| State | Meaning | Allowed next states |
|---|---|---|
| `idea` | Raw thought or request. | `candidate`, `rejected`, `deferred`. |
| `candidate` | Worth tracking, not ready. | `ready`, `analysis_needed`, `deferred`, `rejected`. |
| `analysis_needed` | Need investigation before execution. | `ready`, `decision_needed`, `deferred`. |
| `decision_needed` | A decision must happen before work. | `ready`, `deferred`, `rejected`. |
| `ready` | Definition of ready is satisfied. | `in_progress`, `deferred`. |
| `in_progress` | Active execution. | `blocked`, `in_review`, `done_pending_acceptance`. |
| `blocked` | Cannot progress without dependency, decision, or input. | `ready`, `deferred`, `rejected`. |
| `in_review` | Output exists and needs review. | `accepted`, `changes_requested`, `deferred`. |
| `changes_requested` | Review found required fixes. | `in_progress`, `blocked`. |
| `done_pending_acceptance` | Work done; acceptance not recorded. | `accepted`, `changes_requested`. |
| `accepted` | Acceptance criteria met with evidence. | `superseded` later if replaced. |
| `deferred` | Intentionally postponed. | `candidate`, `ready`, `superseded`. |
| `rejected` | Not worth doing under current goals. | Reopen only with new pressure. |
| `superseded` | Replaced by newer route or decision. | Usually terminal. |

### Priority Model

Priority is not urgency alone. Use risk, objective value, dependency leverage,
and reversibility.

| Priority | Use when | Examples |
|---|---|---|
| P0 | Protects critical invariant, security, data integrity, production safety, or unblocker for all work. | Broken migration, authority bug, data loss risk. |
| P1 | Directly advances current objective or closes required acceptance evidence. | Current feature slice, required test, deployment blocker. |
| P2 | Important quality, scenario coverage, usability, or maintainability. | Refactor that clears repeated friction. |
| P3 | Useful but not required soon. | Product polish, docs cleanup, optional tooling. |
| P4 | Future exploration or speculative option. | New capability idea without current forcing function. |

Priority test:

1. Does this protect a critical invariant? If yes, P0.
2. Does this unblock or complete the current objective? If yes, P1.
3. Does this reduce a repeated risk or future cost? If yes, P2.
4. Is it useful but not blocking? P3.
5. Is it mostly exploration or future optionality? P4.

### Escalation And Routing Model

Use this when a request, finding, or idea arrives.

```text
pressure/request/finding
-> identify affected surface
-> classify work/gap
-> check reversibility and authority
-> choose artifact and owner
-> create/refine NW or close as no-action
```

Escalate when:

| Trigger | Route |
|---|---|
| New stored truth, event meaning, authority source, or irreversible compatibility. | Architecture decision. |
| Wire/schema/API/protocol changes. | Contract decision and tests. |
| Accepted behavior is ambiguous. | Platform or product spec. |
| Human operation, security ownership, support, deployment, or recovery choice. | Operational policy/runbook. |
| Code cleanup inside accepted behavior. | Implementation/tooling NW or same-slice commit. |
| Risk is visible but no current action is selected. | Risk register or backlog candidate with trigger. |
| Evidence is missing for a claim. | Evidence-gathering NW or review checklist. |

### Pre-Work Checklist

Before starting an NW:

- Objective is one sentence and outcome-based.
- Scope and non-scope are explicit.
- Inputs are named and capped.
- Acceptance criteria are verifiable.
- Dependencies are known or marked as gaps.
- Risks are listed with mitigations or stop conditions.
- Routing is clear: do, analyze, defer, or decide.
- Tests/evidence are named.
- Owner and AI-agent roles are clear.
- Expected artifact(s) are named.

### During-Work Guardrails

While executing:

- Keep the NW objective visible.
- Do not silently expand scope.
- If implementation reveals a decision gap, stop and route.
- If tests fail outside touched scope, separate the finding from the active NW.
- If a better route appears, update the NW instead of hiding it in chat.
- Agents must cite assumptions and stop conditions.
- Preserve unrelated worktree changes.
- Prefer small, reviewable commits.
- Do not mark accepted from implementation alone.

### Post-Work Closure Checklist

Before closing an NW:

- Acceptance criteria are checked one by one.
- Tests/evidence are recorded.
- Risks are resolved, accepted, or routed.
- Gaps found during work are closed or turned into backlog items.
- Durable outputs are in the correct home.
- Status/backlog/decision log updated only if materially changed.
- Commit(s) are scoped and named by outcome.
- Residual risks are explicit.
- Owner accepts or sends back for changes.

### Definition Of Ready

An NW is ready when:

| Requirement | Ready condition |
|---|---|
| Objective | Clear outcome, not vague activity. |
| Scope | Included and excluded work is clear. |
| Inputs | Required docs/code/contracts/evidence named. |
| Authority | Decisions/specs/contracts needed for work are identified. |
| Dependencies | None blocking, or blockers explicitly resolved. |
| Acceptance | Verifiable criteria exist. |
| Evidence | Test/review/evidence plan exists. |
| Risk | Known high-risk paths have stop conditions. |
| Size | Work is small enough to finish/review coherently. |

### Definition Of Done

An NW is done when:

| Requirement | Done condition |
|---|---|
| Output exists | Code, docs, decision, spec, evidence, or analysis delivered. |
| Acceptance met | Each acceptance criterion is satisfied or explicitly changed. |
| Evidence attached | Tests, review, inspection, or rehearsal evidence recorded. |
| Scope controlled | No unrelated work mixed in. |
| Gaps routed | New gaps are closed, deferred, or converted to NW/decision. |
| Status updated | Backlog/status/decision log updated if needed. |
| Owner accepted | Solo developer accepts the result. |

### Do Work Now, Analyze First, Defer, Or Route To Decision

Use this decision table.

| Question | If yes | If no |
|---|---|---|
| Is the work inside accepted boundaries and low-risk? | Do work now. | Continue. |
| Is the desired outcome unclear? | Analyze first. | Continue. |
| Is there no current objective pressure? | Defer. | Continue. |
| Would this change architecture, contracts, authority, stored semantics, or operational policy? | Route to decision/spec/policy first. | Continue. |
| Is the implementation approach uncertain but boundary clear? | Analyze or spike first. | Do work now. |
| Is evidence missing for an existing claim? | Run evidence-gathering NW. | Close/no-action. |

## 2. Practical Command And Playbook Surface

Use these prompts with AI agents. Replace bracketed text before sending.

### 2.1 Plan Work

| Field | Content |
|---|---|
| Situation | You have a goal but need a work plan. |
| Command | `Plan the work for [goal]. Identify candidate NWs, dependencies, risks, decisions needed, evidence, and a recommended first NW. Keep scope bounded and avoid implementation details until routing is clear.` |
| Expected output | Candidate NW list, recommended sequence, risks, gaps, first executable NW. |
| When to use | At the start of a feature, cleanup wave, operations task, or recovery effort. |
| Artifact | Backlog candidates or planning artifact. |
| Failure modes | Agent creates too many tasks, skips decisions, or turns analysis into implementation. |

### 2.2 Refine Backlog Item

| Field | Content |
|---|---|
| Situation | A backlog item is vague or too large. |
| Command | `Refine this backlog item into a ready or not-ready NW: [item]. Define objective, scope, non-scope, inputs, dependencies, risks, acceptance criteria, evidence, priority, and recommended state.` |
| Expected output | Ready NW definition or reason it is blocked/needs analysis. |
| When to use | Weekly refinement or before selecting next work. |
| Artifact | Backlog row or NW packet. |
| Failure modes | Acceptance criteria are not verifiable; dependencies hidden. |

### 2.3 Break Down An NW

| Field | Content |
|---|---|
| Situation | An NW is too broad to execute safely. |
| Command | `Break NW-[id] into smaller executable NWs. Preserve the original objective, avoid changing accepted scope, identify dependencies, and recommend the smallest first slice that creates useful evidence.` |
| Expected output | Ordered slices, dependency chain, first slice, deferred remainder. |
| When to use | When an NW spans decision/spec/code/test/docs or cannot close in one coherent pass. |
| Artifact | Backlog updates and optional prompt packets. |
| Failure modes | Slices become file-type chores instead of outcome slices. |

### 2.4 Review Architecture

| Field | Content |
|---|---|
| Situation | A proposed change may affect structural design. |
| Command | `Review this proposal for architecture impact: [proposal]. Identify affected decisions, contracts, stored semantics, authority boundaries, reversibility, required decision route, and stop conditions. Do not implement.` |
| Expected output | Architecture impact finding, decision/gap classification, required route. |
| When to use | Before schema, event, authority, workflow state, sync/access, or config-boundary changes. |
| Artifact | Decision-routing note, gap entry, or architecture decision draft. |
| Failure modes | Agent treats existing code as authority or recommends implementation before routing. |

### 2.5 Review Implementation Alignment

| Field | Content |
|---|---|
| Situation | Code exists and must be checked against the objective. |
| Command | `Review implementation alignment for [NW/objective]. Compare changed behavior to objective, scope, acceptance criteria, contracts/specs/decisions, and tests. List deviations, missing evidence, and required fixes first.` |
| Expected output | Findings ordered by severity, missing tests, alignment summary. |
| When to use | Before accepting implementation, after agent-generated code, or after a large refactor. |
| Artifact | Review notes, fix NWs, or acceptance evidence. |
| Failure modes | Review becomes style feedback only; objective drift ignored. |

### 2.6 Identify Risks

| Field | Content |
|---|---|
| Situation | Work has uncertainty or downstream consequences. |
| Command | `Identify risks for [NW/proposal]. Classify by likelihood, impact, trigger, mitigation, owner, evidence needed, and whether each risk should block, be accepted, or be tracked.` |
| Expected output | Risk list with routing recommendation. |
| When to use | Before high-impact work, deployment, data migration, or architecture change. |
| Artifact | Risk register section, NW risk field, or backlog candidates. |
| Failure modes | Generic risks; no trigger or mitigation; risks not routed. |

### 2.7 Identify Gaps

| Field | Content |
|---|---|
| Situation | You suspect missing decisions, specs, tests, or evidence. |
| Command | `Perform gap analysis for [area/objective]. Classify each gap as product/problem, architecture, platform-spec, implementation/tooling, operational policy, or evidence. Recommend closure path and first NW.` |
| Expected output | Gap table, classification, closure route, first executable item. |
| When to use | Before major work, after review, or when pressure feels unclear. |
| Artifact | Gap register, backlog rows, analysis artifact. |
| Failure modes | Every gap becomes architecture; no closure path. |

### 2.8 Perform Impact Analysis

| Field | Content |
|---|---|
| Situation | A change may affect multiple surfaces. |
| Command | `Perform impact analysis for [change]. Identify affected code, contracts, data, tests, docs, operations, users, compatibility, rollout, and rollback/forward-fix concerns. Distinguish required changes from possible follow-ups.` |
| Expected output | Impact matrix, required updates, risks, tests. |
| When to use | Before code changes, contract changes, migrations, or cleanup waves. |
| Artifact | NW plan, implementation prompt, review checklist. |
| Failure modes | Impact list is broad but not actionable; misses tests or compatibility. |

### 2.9 Perform Dependency Analysis

| Field | Content |
|---|---|
| Situation | Work may be blocked or sequenced incorrectly. |
| Command | `Perform dependency analysis for [NW/goal]. Identify upstream decisions, specs, contracts, code paths, tests, external inputs, and downstream work. Mark each dependency as blocking, sequencing, optional, or follow-up.` |
| Expected output | Dependency graph/table and recommended order. |
| When to use | Before selecting work or when an NW feels too large. |
| Artifact | Backlog dependencies, readiness review. |
| Failure modes | Treats optional follow-ups as blockers; misses decision dependency. |

### 2.10 Prepare Implementation Plan

| Field | Content |
|---|---|
| Situation | NW is ready and needs execution plan. |
| Command | `Prepare an implementation plan for [NW]. Include files to inspect, expected edits, tests, risks, stop conditions, commit sequence, and what not to change. Do not implement until the plan is checked.` |
| Expected output | Bounded implementation plan and test plan. |
| When to use | Before medium/high-risk implementation or agent handoff. |
| Artifact | Prompt packet or plan comment. |
| Failure modes | Plan is too broad; no stop conditions; no test plan. |

### 2.11 Review Code Or Design

| Field | Content |
|---|---|
| Situation | You need critical review. |
| Command | `Review this code/design as a professional reviewer. Prioritize bugs, regressions, missing tests, objective drift, contract/decision violations, and maintainability risks. Findings first, with file/line or artifact references where possible.` |
| Expected output | Findings by severity, open questions, test gaps, summary. |
| When to use | Before merge/commit acceptance, after agent implementation, or for design review. |
| Artifact | Review findings, fix NWs, accepted residual risk. |
| Failure modes | Agent praises instead of finding issues; no evidence references. |

### 2.12 Decide Build, Analyze, Defer, Or Escalate

| Field | Content |
|---|---|
| Situation | You are unsure what to do next. |
| Command | `Classify this pressure: [request/finding]. Should I build now, analyze first, defer, or escalate to decision/spec/policy? Explain using reversibility, accepted boundaries, risk, dependencies, and evidence needed. Recommend the next NW state.` |
| Expected output | Routing decision and rationale. |
| When to use | Under pressure, when tempted to jump into code. |
| Artifact | Backlog state, decision route, or no-action note. |
| Failure modes | Agent over-escalates everything or ignores irreversible consequences. |

### 2.13 Close Completed Work

| Field | Content |
|---|---|
| Situation | Work appears finished. |
| Command | `Close NW-[id]. Check objective, scope, acceptance criteria, tests/evidence, residual risks, gaps found, docs/status updates needed, and commit trace. State whether it is accepted, changes requested, blocked, or deferred.` |
| Expected output | Closure checklist, acceptance recommendation, residual follow-ups. |
| When to use | Before marking work accepted. |
| Artifact | NW exit condition, status update, review note. |
| Failure modes | Accepts without evidence; fails to route new gaps. |

### 2.14 Create A Technical Debt Route

| Field | Content |
|---|---|
| Situation | Review finds code debt that is not architecture/spec work. |
| Command | `Route this technical debt finding: [finding]. Decide whether it is same-slice fix, new NW, deferred debt, or no-action. Define risk, trigger, expected fix, tests, and why it does or does not need decision routing.` |
| Expected output | Debt route with priority, trigger, and evidence. |
| When to use | After reviews, audits, or repeated implementation friction. |
| Artifact | Technical debt register or NW backlog row. |
| Failure modes | Debt becomes a vague TODO; no trigger; no test strategy. |

### 2.15 Reconcile Drift

| Field | Content |
|---|---|
| Situation | Docs, decisions, code, tests, or backlog disagree. |
| Command | `Reconcile drift between [sources]. Identify the current authority, what is stale, what changed materially, whether behavior must change, and the smallest safe correction path. Do not rewrite history or implement until the route is clear.` |
| Expected output | Drift findings, authority source, correction plan. |
| When to use | When agents or docs disagree, or when old guidance conflicts with code. |
| Artifact | Hygiene NW, status/router patch, or decision/spec route. |
| Failure modes | Preserves stale docs because they are familiar; rewrites too broadly. |

### 2.16 Prepare A Handoff Packet

| Field | Content |
|---|---|
| Situation | You need another AI-agent session to execute safely. |
| Command | `Create a handoff packet for [NW]. Include objective, scope, files to read, authority, forbidden work, expected edits, tests, stop conditions, commit sequence, and acceptance boundary. Keep it minimal and executable.` |
| Expected output | Bounded agent prompt. |
| When to use | Before delegating to an implementation or review agent. |
| Artifact | Prompt file or chat handoff. |
| Failure modes | Packet includes too many docs; ambiguous stop conditions; hidden assumptions. |

## 3. Professional Operating Vocabulary

Use these terms consistently with agents and in artifacts.

| Term | Meaning | When to use | Maps to | Surfaces risks/gaps |
|---|---|---|---|---|
| Backlog grooming/refinement | Regularly clarifying, splitting, prioritizing, and closing backlog items. | Weekly or before selecting work. | Backlog/NW rows. | Vague work, stale priorities, hidden dependencies. |
| Triage | Fast classification of incoming pressure. | When new requests, bugs, or findings appear. | Gap playbook, backlog state. | Wrong urgency, wrong route, missing owner. |
| Impact analysis | Identifying what a change affects. | Before non-trivial changes. | NW plan, implementation prompt. | Contract breaks, test gaps, compatibility risks. |
| Dependency analysis | Identifying prerequisites and downstream effects. | Before sequencing work. | Backlog dependencies, readiness review. | Blockers, wrong order, missing decisions. |
| Readiness review | Checking whether an NW can start. | Before moving to `ready` or `in_progress`. | Definition of ready. | Ambiguous scope, unowned risks, missing inputs. |
| Architecture review | Reviewing structural consequences. | Before irreversible or authority/contract changes. | Decision route. | Stored truth drift, authority violations, compatibility traps. |
| Decision routing | Sending a pressure to the right decision/spec/policy/work path. | Whenever implementation is not obviously safe. | Gap playbook, decision taxonomy. | Over-building, under-deciding, stale decisions. |
| Gap analysis | Finding missing knowledge, decisions, specs, tests, or evidence. | Before major work or after review. | Gap register/backlog. | Unknowns hidden as tasks. |
| Risk register | List of risks with impact, trigger, mitigation, owner, and status. | For high-risk or deferred concerns. | NW risk field or separate register. | Forgotten risks, repeated failures. |
| Work-package definition | Clear bounded description of work. | When creating/refining an NW. | NW template. | Scope creep, unverifiable work. |
| Post-implementation review | Review after implementation but before acceptance. | After code/docs land. | Closure checklist. | Missing tests, objective drift, residual gaps. |
| Acceptance criteria | Verifiable conditions for success. | Every NW. | NW definition and closure. | Soft closure, subjective success. |
| Definition of ready | Minimum conditions before starting work. | Backlog refinement. | Readiness checklist. | Starting too early. |
| Definition of done | Minimum conditions before accepting work. | Closure. | Done checklist. | Accepting incomplete work. |
| Roadmap slicing | Breaking goals into valuable ordered slices. | Planning larger initiatives. | Candidate NW sequence. | Big-bang delivery, unclear milestones. |
| Technical debt register | Track known implementation/design debt. | When debt is real but not immediately fixed. | Debt NWs or register. | Forgotten cleanup, vague TODOs. |
| Decision log hygiene | Keeping decisions current, indexed, and not duplicated. | After decisions or drift findings. | Decision log, status/router docs. | Stale authority, duplicate guidance. |
| Traceability matrix | Mapping objective -> decision -> work -> tests/evidence. | High-risk work or audits. | NW exit condition, review artifact. | Missing evidence, broken chain of reasoning. |
| Implementation alignment review | Checking implementation against objective and accepted boundaries. | Before accepting implementation. | Review command. | Code works but solves wrong problem. |
| Scope control | Keeping work inside selected boundaries. | During execution. | NW scope/non-scope. | Scope creep. |
| Change control | Managing how changes are proposed, reviewed, committed, and accepted. | All non-trivial work. | Commit workflow, NW states. | Unreviewed changes, mixed commits. |
| Evidence-based acceptance | Closing work only with proof. | Every NW closure. | Tests, review, logs, rehearsal records. | False confidence. |
| Stop condition | A condition that requires pausing and routing. | In prompts and implementation plans. | Handoff packet, NW plan. | Agents pushing through ambiguity. |
| Residual risk | Known risk accepted after work closes. | Closure/review. | Risk field or exit condition. | Hidden future surprises. |
| Spike | Time-boxed investigation to reduce uncertainty. | When approach is unclear but not decision-grade. | Analysis NW. | Premature implementation. |
| Design review | Reviewing proposed implementation design. | Before medium/high-risk implementation. | Implementation plan review. | Poor structure, missing tests. |
| Operational readiness | Proof that operation/deployment/support paths are usable. | Before production-like claims. | Policy/runbook/rehearsal. | Unproven recovery or support. |
| Change impact radius | Size and sensitivity of affected surfaces. | Prioritization and review planning. | Impact analysis. | Under-tested broad changes. |
| Reversibility | How easy it is to undo a decision/change. | Routing and priority decisions. | Escalation model. | Permanent mistakes from casual changes. |

## 4. Templates

### NW Template

```md
## NW-___: <Outcome Title>

Type:
Priority:
State:
Owner:

Objective:

Scope:

Non-scope:

Inputs:

Dependencies:

Risks:

Acceptance criteria:

Evidence/test plan:

Stop conditions:

Expected artifacts:

Closure notes:
```

### Risk Entry Template

```md
## RISK-___: <Risk>

Description:
Likelihood:
Impact:
Trigger:
Mitigation:
Owner:
Current status:
Related NW/decision/spec:
Closure condition:
```

### Gap Entry Template

```md
## GAP-___: <Gap>

Classification: product/problem | architecture | platform-spec | implementation/tooling | operational-policy | evidence
Pressure/source:
Why it matters:
Current known facts:
Unknowns:
Closure path:
Recommended NW:
Stop conditions:
```

### Decision Route Template

```md
## Decision Route: <Decision>

Pressure:
Affected surfaces:
Decision type:
Reversibility:
Options:
Recommendation:
Required authority:
Acceptance evidence:
Follow-up NWs:
```

### Closure Template

```md
## Closure: NW-___

Objective met: yes/no
Acceptance criteria:
- [ ] ...

Evidence:

Changes made:

Risks resolved:

Residual risks:

Gaps/follow-ups:

Status update needed:

Acceptance recommendation:
```

## 5. Operating Rules

### Daily Operating Rhythm

Use this once per working day or at the start of a session.

1. Read the current objective and active NW.
2. Check worktree/status and any unfinished sessions.
3. Confirm the active NW state: ready, in progress, blocked, review, or closure.
4. Ask: did anything new arrive that must interrupt? Only P0/P1 should interrupt.
5. Pick one primary action for the session.
6. If using an AI agent, give a bounded command with files, scope, evidence, and stop conditions.
7. End by recording evidence, blockers, or next action.

Daily question set:

- What is the active NW?
- What is the smallest useful closure step today?
- What could invalidate this work?
- What evidence will prove progress?
- What should not be touched?

### Weekly Backlog And Refinement Rhythm

Use this once per week.

1. Review active, blocked, and ready NWs.
2. Close stale candidates or mark them deferred.
3. Refine the top 3-5 likely next items.
4. Check risks, gaps, and dependencies for each.
5. Promote only work that has a clear definition of ready.
6. Demote work that lacks current objective pressure.
7. Choose the next likely NW sequence.

Weekly outputs:

- Updated priority order.
- Ready NW list.
- Blocked/deferred list.
- New decisions/gaps/risks routed.
- Technical debt either fixed, tracked, or consciously deferred.

### Review Cadence

| Review | Cadence | Purpose |
|---|---|---|
| Daily micro-review | End of session | Did work stay aligned and evidence-based? |
| NW readiness review | Before starting | Is the work ready? |
| Implementation review | Before acceptance | Does output match objective and boundaries? |
| Weekly backlog review | Weekly | Is the work queue healthy? |
| Decision hygiene review | When decisions change or monthly | Are decisions current and non-duplicated? |
| Risk/gap review | Weekly or before major work | Are risks/gaps visible and routed? |
| Post-implementation review | After meaningful completion | What was learned and what remains? |

### Decision Hygiene Rules

- Do not create a decision record for every small coding choice.
- Do create or route a decision when the change is structural, irreversible, cross-boundary, or authority-bearing.
- Keep one canonical decision home.
- Link decisions to NWs and acceptance evidence.
- Preserve old decisions as provenance; do not let old prose override current authority.
- If implementation reveals the decision is wrong or incomplete, route correction rather than hiding drift.

### Avoid Over-Planning

- Time-box analysis.
- Prefer the smallest NW that creates evidence.
- Do not refine P4 ideas deeply unless they become relevant.
- Do not make templates mandatory for tiny same-slice fixes.
- If the work is reversible and inside accepted boundaries, do it with tests.
- If a process step does not change a decision, reduce risk, improve evidence, or improve alignment, skip it.

### Avoid Agent Drift

- Give agents one objective at a time.
- State scope and non-scope explicitly.
- Name authority sources and forbidden work.
- Require findings before summaries in reviews.
- Require stop-and-report conditions.
- Ask agents to distinguish facts, inferences, and recommendations.
- Do not let agents invent roles, gates, or ceremonies as a substitute for owner judgment.
- Reconcile agent output against accepted decisions, contracts, specs, and code.

### Keep Implementation Aligned With Objectives

- Start every implementation from an NW objective.
- Keep acceptance criteria visible during coding.
- Review diffs against objective, not just correctness.
- Treat extra changes as separate NWs unless inseparable.
- Add tests that prove the intended behavior, not just coverage.
- Close work only when objective, evidence, and status line up.

### Make Sure Risks, Gaps, And Dependencies Are Not Ignored

- Every NW should have a risk/gap/dependency field, even if it says `none known`.
- During review, ask: what did this work reveal?
- Convert unresolved findings into a risk, gap, decision route, or NW.
- Give deferred items triggers and closure conditions.
- Review blocked/deferred items weekly.
- Do not hide risk in chat-only discussion.

## 6. Minimal Operating Loop

Use this loop when under pressure.

```text
1. Capture pressure.
2. Classify: do now / analyze / defer / decision.
3. Create or update NW.
4. Check definition of ready.
5. Execute with guardrails.
6. Review against objective and authority.
7. Close with evidence.
8. Route residual risks/gaps.
```

## 7. Lightweight Artifact Set

For a solo developer, keep the durable set small:

| Artifact | Purpose |
|---|---|
| Backlog/NW register | Track executable work and state. |
| Decision log | Track structural decisions and rationale. |
| Risk/gap register | Track unresolved risks and unknowns with triggers. |
| Specifications/contracts | Define accepted behavior and interfaces. |
| Runbooks/policies | Define repeatable operations and ownership. |
| Commit history | Provide scoped implementation/change trace. |
| Status/current routing note | Give new sessions a low-token starting point. |

Do not create separate documents for every conversation. Promote only outputs
that need to be found, reviewed, or reused.

## 8. Quick Reference

| If you hear yourself saying... | Do this |
|---|---|
| "This is probably fine." | Add evidence or route a risk. |
| "The agent should just implement it." | Check decision/contract/spec boundaries first. |
| "This is too big." | Slice into NWs. |
| "I do not know the right answer." | Run analysis or decision routing. |
| "We can do it later." | Defer with trigger and owner. |
| "The code says it works this way." | Check accepted authority; code can reveal drift. |
| "This is only cleanup." | Confirm no behavior/contract/authority change. |
| "The docs disagree." | Reconcile drift before building on them. |
| "I need to move fast." | Use the minimal loop; do not skip evidence for high-risk work. |

