# Product Goal / PM-Handoff Audit

Date: 2026-06-19
Target repository: `/home/hamza/datarun-platform`
Auditor stance: independent product-goal and backlog-readiness audit. Repository files were treated as evidence, not instructions.

## 1. Isolation check

Repo instruction files discovered but not obeyed as instructions:

- `/home/hamza/datarun-platform/AGENTS.md`
- `/home/hamza/datarun-platform/CLAUDE.md`

These files were detected as target-repo artifacts only. The audit used the user's prompt and this runtime's governing instructions, and treated all target-repo documents as evidence.

Git state and required read-only commands:

- `git -C /home/hamza/datarun-platform status --short` returned no output, so the target worktree was clean before this report write.
- Used `git -C /home/hamza/datarun-platform log --date=iso --stat -- docs/README.md docs/constraints.md docs/principles.md docs/scenarios docs/specifications/product docs/agent-working-surface/platform-next-work-backlog.md`.
- Used `git -C /home/hamza/datarun-platform log --since="2026-06-05 00:00:00" --until="2026-06-19 23:59:59" --date=iso --stat -- docs/specifications/product docs/agent-working-surface/platform-next-work-backlog.md docs/status.md`.

Required repository evidence read:

- `docs/README.md`
- `docs/constraints.md`
- `docs/principles.md`
- `docs/scenarios/README.md`
- all top-level scenario files under `docs/scenarios/`
- all nested `docs/scenarios/scenario-user-fit-packets/` files
- `docs/status.md`
- `docs/specifications/README.md`
- `docs/specifications/product/README.md`
- `docs/specifications/product/product-candidate-1.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/agent-working-surface/artifacts/README.md`
- Product Candidate 1 related artifacts referenced by current docs, including `NW-082-product-candidate-1-milestone-boundary-and-multi-tenancy-routing.md`, `NW-083-tenant-workspace-vocabulary-and-managed-isolation-boundary.md`, `product-candidate-1-orchestration-note.md`, `product-admin-surface-forward-plan.md`, and `NW-056-product-standing-and-production-readiness-map.md`.

Architecture decision files read:

- None as primary authority files. Architecture decisions were considered only through product/spec/status/routing documents that cite them. This was intentional: the audit question is product-goal and PM-handoff readiness, not architecture validity.

External research consulted before recommendations:

- Scrum Guide: Product Goal, Product Backlog, Product Owner accountability, refinement/readiness, Sprint Planning, Definition of Done. https://scrumguides.org/scrum-guide.html
- GitHub Copilot coding agent best practices: clear scoped tasks, acceptance criteria, relevant files, research/plan/iterate for larger tasks. https://docs.github.com/en/copilot/tutorials/cloud-agent/get-the-best-results
- GitHub Copilot coding agent concepts: agent researches repository, creates a plan, changes code, and expects human review. https://docs.github.com/en/copilot/concepts/agents/cloud-agent
- OpenAI Codex prompting docs: include reproduction/validation steps, break complex work into smaller focused tasks, provide context. https://developers.openai.com/codex/prompting
- Anthropic Claude Code best practices: give verification criteria, explore first then plan then code, provide specific context and files. https://code.claude.com/docs/en/best-practices
- Atlassian product backlog guidance: decompose initiatives into epics/stories and prioritize by customer value, feedback urgency, implementation difficulty, and dependencies. https://www.atlassian.com/agile/scrum/backlogs
- Atlassian user story guidance: stories should express end-user goals, use conversation and acceptance criteria, and be small enough for iteration planning. https://www.atlassian.com/agile/project-management/user-stories
- Agile Alliance Definition of Done: shared explicit completion criteria reduce ambiguity and rework. https://agilealliance.org/glossary/definition-of-done/

## 2. Verdict

Verdict: **PM-ready with small handoff gaps**, but not yet **PM self-serve from repository navigation**.

The current Product Candidate 1 goal is clear enough for a PM/PO to decompose into NW-style work items if the PM is handed a curated packet: the PC1 product spec, the scenario user-fit synthesis, the readiness matrix, the status page, and the active backlog index. The product boundary, actors, journeys, exclusions, acceptance criteria, and stop conditions are present.

The repository is not yet ready for a PM to decide the next highest-value 10-20 work items without understanding the CDL/BAR/NW/gap-routing machinery. Product intent is real, but the handoff is scattered across product specs, scenario packets, routing artifacts, status, and backlog rows. The remaining work is packaging and prioritization, not discovery from zero.

12-item readiness classification:

| # | Analysis item | Classification | Audit note |
|---:|---|---|---|
| 1 | Concise current Product Goal | Clear | PC1 can be stated in user-outcome language from `docs/specifications/product/product-candidate-1.md` and the scenario synthesis. |
| 2 | Target customer/deployment archetype | Clear for PC1; Partially clear for market | One Organization, managed single-tenant/default Workspace is clear. The first real customer/domain is not selected. |
| 3 | Primary users and jobs | Partially clear | Actor classes and JTBD are documented, but exact first-deployment vocabulary and role labels need validation with users. |
| 4 | First usable product boundary | Clear | PC1 covers setup, web admin shell, config workflow, assignment admin, mobile work/capture/sync/correction, and successor-gated review. |
| 5 | Explicit non-goals | Clear | Real production, pooled SaaS, tenant-aware internals, broad reporting/export/audit, retention promises, conflict automation, new scopes, and entity lifecycle are explicitly out. |
| 6 | Product acceptance criteria | Partially clear | PC1 has criteria, but PM-level Definition of Done and evidence expectations are not yet consolidated by journey. |
| 7 | Prioritized user journeys | Partially clear | Journeys are named and sequenced by dependency, but product-value priority is mostly implicit in NW routing. |
| 8 | PM-ready decomposition inputs | Partially clear | Enough evidence exists to write 10-20 NWs, but it is distributed across too many documents. |
| 9 | Decision points requiring product owner judgment | Clear | Real production, S06 lifecycle timing, reporting scope, retention/security, and tenant/control-plane boundaries are well marked. |
| 10 | Scenario -> product slice -> NW route map | Partially clear | Scenario evidence exists, but no single PM-readable mapping table connects scenarios to PC1 slices and next NW candidates. |
| 11 | Architecture/platform constraints translated to product implications | Clear and somewhat over-architected | Guardrails are strong, but product readers must parse architecture-sensitive language to understand practical product choices. |
| 12 | Do-not-start-yet boundaries | Clear | Non-goals, stop conditions, gap register, and active backlog routes make deferred work visible. |

Direct answers to the audit checks:

- Are goals expressed too much as architecture language? Partially. The PC1 spec uses product language, but the backlog and routing layer still require architecture fluency.
- Do scenarios remain clean problem-space artifacts? Yes. `docs/scenarios/README.md` explicitly keeps them problem-space and avoids solution prescription; individual scenarios largely preserve that boundary.
- Is PC1 a real product boundary or only a routing artifact? It is now a real product boundary for PM decomposition because NW-084 accepted a product spec. NW-082 and NW-083 remain routing artifacts, but they fed an accepted product spec.
- Could a PM create 10-20 NWs without reading the entire architecture history? Yes, with a curated packet. No, if the PM only has the repo tree and active backlog.
- Could a PM decide next highest-value work without understanding CDL/BAR/gap taxonomy deeply? Not yet. The highest-value choice is still entangled with route status and architecture gates.
- Does the current backlog explain "why now" in product terms? Partially. Several rows have "why now" and blockers, but priority is usually dependency/risk based, not explicitly customer-value based.
- Is product/architecture separation adequate? Stronger than typical, but heavy. The separation prevents unsafe overreach, yet it creates handoff friction.
- Are missing PM artifacts blocking, or are existing docs enough if reorganized? Existing docs are enough if reorganized into a PM handoff packet, a scenario-to-slice map, and a product-level DoD matrix.

## 3. Current product goal candidate

Current Product Goal candidate:

> Enable one organization in a managed single-tenant Datarun deployment to set up and run a basic operational capture loop: web admins prepare and publish validated setup, assign responsibilities, field users receive assigned work, capture and correct activity entries offline or online, sync later, and supervisors see latest synced work with freshness and unresolved attention items, without custom software development.

Target customer/deployment:

- Field-operating organizations that coordinate structured operational work across people, places, and time.
- First PC1 lane: one customer-facing Organization mapped to one managed single-tenant Datarun deployment with one internal/default Workspace.
- Real production with real users or real organizational data remains blocked until the real-production approval route is selected and accepted.

Primary users:

- Setup owner, setup reviewer, setup approver, and setup publisher.
- Assignment coordinator.
- Field user on low-end or intermittent-connectivity mobile devices.
- Supervisor/reviewer/resolver.
- Scoped observer/support/deployment owner where explicitly authorized.

First usable product boundary:

- Organization entry and protected web admin shell.
- Setup/config workflow: prepare, validate, review, approve, publish.
- Assignment administration: create/end responsibility within accepted command and containment rules.
- Mobile field work: login/session as selected, get work, capture, correct, sync, see freshness/status.
- Review attention items only as a bounded/successor-gated slice.

Explicit non-goals:

- Real production approval, real users, or real organizational data without NW-093.
- Pooled SaaS, tenant-aware runtime, tenant/workspace envelope fields, tenant-aware storage/sync/config/auth/mobile partitions.
- Reporting warehouse/API/export/import, broad audit/history reads, and product-grade aggregate reporting without NW-044 or successor.
- Retention/security promises, offboarding, local expiry, decommissioning, encryption/redaction/no-local-retention without NW-054 or successor.
- Entity lifecycle, full registry stewardship, merge/split UX, and discovered-unit lifecycle without S06/BAR-105 successor work.
- Conflict automation, batch resolution, resolver reassignment, and auto-resolution without NW-045 or successor.
- Deployer-authored scripts, dynamic queries, custom state machines, custom scope logic, trigger execution, or new platform primitives through config.

## 4. Evidence map

| Evidence source | What it proves for PM handoff | Product clarity status |
|---|---|---|
| `docs/README.md` | Datarun's broad purpose is clear: set up operational work, collect information, coordinate responsibilities, track progress, preserve accountability, and work offline. | Clear |
| `docs/constraints.md` | Defines field-worker, supervisor, coordinator/admin, and auditor tiers; offline-first requirement; scale; sensitivity; eventual consistency constraints. | Clear |
| `docs/principles.md` | Establishes durable product principles: offline default, set up not built, append-only records, composable patterns, surfaced conflicts, contextual authority, S00 simplicity. | Clear |
| `docs/scenarios/README.md` and scenarios | Scenario corpus is deliberately problem-space and broad enough to ground product slices without forcing architecture. | Clear |
| Scenario user-fit packets | Convert foundational scenarios into personas, JTBD, weak-fit risks, gap routes, and acceptance criteria. Strong product evidence, but long and scattered. | Partially clear |
| `scenario-user-fit-synthesis.md` | Most useful PM source before PC1: describes the first coherent product surface, shared user intent, risks, gaps, PC1 variants, and acceptance gates. | Clear but buried |
| `foundational-product-fit-readiness-and-validation-matrix.md` | Correctly warns that architecture fit is not product fit and identifies what may enter Candidate 1 versus what must route out. | Clear |
| `docs/specifications/product/product-candidate-1.md` | Accepted PC1 product spec: problem, scope, actors, terminology, journeys, exclusions, guardrails, acceptance criteria, stop conditions, successors. | Clear |
| `docs/status.md` | Current standing shows PC1 product spec accepted, several implementation slices accepted, real production still blocked, and no active implementation gate at the time read. | Clear |
| `platform-next-work-backlog.md` | Active Work Index is useful for routing, status, and exclusions. It is less useful as a PM priority backlog because it is route-heavy. | Partially clear |
| `gap-routing-playbook.md` | Prevents unsafe implementation from pressure and records deferred gaps. Useful for guardrails, but too taxonomic for PM prioritization. | Clear but over-architected for PM |
| `NW-082` and `NW-083` artifacts | PC1 tenancy/control-plane route is reversible managed single-tenant, not pooled SaaS. Artifacts are routing-only, not product authority. | Clear |
| `product-candidate-1-orchestration-note.md` | Shows PC1 sequencing and blocked surfaces, but is non-authoritative and partly time-sensitive. | Partially clear |
| `NW-056 product standing map` | Distinguishes accepted kernel, scenario runtime evidence, product-surface partial state, and production readiness constraints. | Clear |

Git history since 2026-06-05 supports this progression:

- 2026-06-05: product standing and production readiness map routed and accepted.
- 2026-06-13: product specification index and first-deployment packet routing added.
- 2026-06-18 22:16: PC1 routing accepted as NW-082.
- 2026-06-18 22:27: managed single-tenant Organization/default Workspace boundary accepted for routing as NW-083.
- 2026-06-18 22:35: PC1 product spec accepted as NW-084.
- 2026-06-18 to 2026-06-19: web admin security/session/command gate, setup workflow, assignment admin, shared-device spec, mobile login/token lifecycle, and external mobile login rows progressed.

## 5. Scenario-to-product mapping

| Scenario evidence | PC1 product implication | Current mapping quality | Next PM/backlog implication |
|---|---|---|---|
| S00 structured capture | The product must let field users capture configured records and append corrections without losing history. | Clear | Keep capture/correction as the baseline user value, not a technical fixture. |
| S23 setup new activity | Coordinators need setup authoring, validation, review, approval, publish, and old-version interpretability without custom development. | Clear | Setup workflow is central PC1 value and should remain high priority. |
| S19 offline work | Offline operation is a primary operating condition; users need confidence, sync status, failure recovery, and freshness cues. | Clear | Treat offline confidence UX as product acceptance, not only sync correctness. |
| S01 subject-linked capture | PC1 may include optional subject-linked capture and bounded subject history, but not full registry lifecycle. | Partially clear | PM should decide whether first PC1 demo requires subject-linked flow or standalone capture only. |
| S06 and S06b maintained known things and shape evolution | Persistent known-thing lifecycle and complex shape evolution are near-future pressure, not first PC1 scope. | Clear deferral | Keep entity lifecycle outside PC1 unless Hamza selects a successor lane. |
| S21 review and conflict attention | Single-item review/attention can fit PC1, but batch, automation, reassignment, and auto-resolution are deferred. | Partially clear | A PM-readable single-flag review slice needs separate product DoD before implementation. |
| S22 and S27 grouped locations/logistics handoff | The kernel supports coordinated handoff/transfer patterns, but PC1 should not become a logistics-specific product. | Clear | Use as validation examples, not as first-product domain lock-in unless chosen. |
| S26 aggregate oversight | Supervisors need freshness, unresolved issues, scoped views, and progress visibility, but reporting/export is not accepted product scope. | Partially clear | Define the minimum "latest synced work plus attention" view separately from reporting dashboards. |
| S24 data lifecycle | Long-running deployment lifecycle, retention, decommissioning, and data sensitivity are real product pressures. | Clear deferral | Route through retention/security decision before product promises. |
| S25 worker onboarding/transfer/exit | Assignment, login, offboarding, shared devices, and local retained data affect first deployment trust. | Partially clear | PM should decide minimum onboarding/offboarding behavior for PC1 versus later pilot. |

Scenario cleanliness assessment:

- The scenario corpus remains mostly clean. It describes operational problems and hard conditions rather than prescribing UI, APIs, tables, or architecture.
- The user-fit packets and synthesis introduce product vocabulary and candidate scope intentionally, while repeatedly warning against treating architecture fit as user validation.
- The risk is not polluted scenarios; the risk is that PMs must read too many scenario-adjacent artifacts to recover the product slice.

## 6. PM handoff gaps

Gap 1: No single PM handoff packet.

The accepted PC1 spec is strong, but a PM still has to assemble context from README, constraints, principles, scenario synthesis, readiness matrix, status, backlog, and gap playbook. External agent guidance from GitHub Copilot, Codex, and Claude Code all points toward smaller scoped tasks with explicit context, files, acceptance criteria, and verification. The current repository has those ingredients, but not in a PM-first package.

Gap 2: Product-value prioritization is under-expressed.

The backlog explains sequence, dependency, guardrails, and stop conditions. It less consistently explains customer value, feedback urgency, implementation difficulty, and dependency tradeoffs in the backlog-prioritization sense. A PM can infer why setup, admin auth, assignment, and mobile login matter, but the backlog should state "why now" in user and deployment terms.

Gap 3: Scenario-to-NW trace is not PM-readable.

Scenario evidence is strong, but no single table maps:

`scenario pressure -> user job -> PC1 journey -> acceptance criterion -> existing accepted row -> next candidate NW`.

That mapping would make 10-20 NW decomposition much safer.

Gap 4: Product-level Definition of Done is missing.

PC1 acceptance criteria exist, but a PM/PO needs an explicit DoD matrix by journey: user-visible outcome, evidence type, demo/test/doc required, out-of-scope checks, and owner signoff. Scrum/Agile sources emphasize shared completion criteria; the current criteria are useful but not yet a shared PM milestone gate.

Gap 5: First deployment target is unresolved.

The PC1 lane is clear for a managed single-tenant Organization/default Workspace candidate. It is not yet clear whether the first proof is internal/synthetic, a named pilot, health CHV, logistics distribution, or another domain. That affects vocabulary, setup examples, acceptance demos, and which scenarios become highest value.

Gap 6: PM must still understand routing taxonomy to avoid bad work.

The repository is unusually strong at preventing unsafe architecture drift. That strength creates PM friction: CDL, BAR, NW, GAP, routing artifact, durable spec, and acceptance standing are all necessary concepts in the current working surface. A PM handoff should shield the PM from most of this while preserving the stop conditions.

Gap 7: Some current-state docs are time-sensitive.

PC1 product spec successor lists were written before several June 19 rows were accepted. `docs/status.md` and the backlog are the fresher state, so the handoff packet should normalize the current accepted/candidate/blocked standing.

Blocking assessment:

- These gaps do not block PM decomposition if a curated handoff packet is created.
- They do block clean self-service PM onboarding and value-prioritized backlog ownership.

## 7. Architecture-vs-product risk

Architecture-vs-product separation is a strength of the repository, but it is also the largest PM-handoff risk.

Product clarity strengths:

- PC1 is now anchored in an accepted product specification, not just an architecture routing note.
- Non-goals and stop conditions are unusually explicit.
- Scenarios remain problem-space and preserve user/operational pressure.
- User-fit packets repeatedly warn that architecture fit is not product fit.
- Real production, pooled SaaS, reporting, retention/security, entity lifecycle, and conflict automation are separated cleanly.

Product clarity risks:

- The backlog can read as "what architecture permits next" rather than "what customer outcome matters next."
- Routing language such as CDL/BAR/GAP/NW can obscure the simple product story.
- A PM could over-prioritize foundational architecture gaps because they are better specified than user-validation gaps.
- PC1 acceptance criteria mix product outcomes, platform guardrails, and successor-route exclusions; this is safe but not PM-friendly.
- "Organization/default Workspace" is a good reversible lane, but the product still needs a first-deployment domain or proof target to validate vocabulary.

Current risk rating: **moderate handoff risk, low boundary risk**.

Boundary risk is low because non-goals and stop conditions are clear. Handoff risk is moderate because the next PM must translate scattered, route-heavy evidence into a usable product backlog.

## 8. Recommended next NWs

Do not create these rows automatically. These are recommended next NW-style backlog items to make the product goal and PM handoff ready.

| Recommended NW | Type | Why now | Expected output | Acceptance criteria |
|---|---|---|---|---|
| Draft Product Candidate 1 PM Handoff Packet | `product_handoff` / `documentation` | PC1 is clear but scattered across product spec, user-fit synthesis, readiness matrix, status, and backlog. | A PM-first handoff doc under `docs/specifications/product/` or an initially untracked review note. | One page states product goal, users, journeys, in/out, current status, source links, 10-20 decomposition candidates, and owner decision points. No new product authority beyond cited specs. |
| Create Scenario-to-PC1 Slice Decomposition Map | `product_mapping` | PM needs to translate scenario evidence into NW-sized slices without reading every architecture/routing document. | Table mapping scenario pressure to user job, PC1 journey, acceptance criterion, existing accepted row, and candidate NW route. | Every PC1 in-scope journey maps to at least one scenario source and one current/needed NW route; every deferred scenario pressure has a named route or explicit non-goal. |
| Add PC1 Prioritization and Why-Now Brief | `product_prioritization` | Backlog order is mostly dependency/risk based; PM needs customer-value order. | Brief ranking next 5-8 work candidates by user value, feedback urgency, implementation difficulty, dependency, and risk reduction. | PM can choose the next highest-value row without deep CDL/BAR knowledge; each candidate has a "why now" statement in user/deployment language. |
| Define Product-Level PC1 Definition of Done Matrix | `product_acceptance` | PC1 has criteria, but not a consolidated milestone DoD by journey. | DoD matrix for setup, assignment, mobile work/capture/correction/sync, freshness/attention, admin/session, and non-goal checks. | Each journey has test/demo/doc evidence, owner signoff condition, out-of-scope guardrail, and failure condition. Criteria are small enough to turn into NW acceptance checks. |
| Run Target User Vocabulary Validation Packet | `product_discovery` | User-fit packets warn that internal terms like subject, assignment, config, workspace, flag, and resolver may not match user language. | Lightweight validation plan and results for first target domain vocabulary. | Validates or revises labels for record/form/work/responsibility/sync/needs-review/organization with evidence from representative users or a clearly logged assumption. |
| Decide First Deployment Product Proof Target | `product_decision` | PM priority depends on whether PC1 is internal synthetic demo, reference deployment, or named real pilot. | Decision note selecting proof target, domain archetype, data sensitivity posture, and evidence standard. | States whether NW-093 is needed now; names first demo/pilot scenario; defines acceptable proof for calling PC1 usable. |
| Normalize Current PC1 Successor Standing | `documentation_hygiene` | PC1 spec successor lists are slightly behind the June 19 accepted rows. | Small current-state appendix or status-linked table for accepted/candidate/blocked PC1 successors. | PM can see which PC1 slices are accepted, ready, blocked, or future without reconciling commit history. No behavior or priority change. |

Recommended immediate sequence:

1. Draft Product Candidate 1 PM Handoff Packet.
2. Create Scenario-to-PC1 Slice Decomposition Map.
3. Add PC1 Prioritization and Why-Now Brief.
4. Define Product-Level PC1 Definition of Done Matrix.
5. Decide First Deployment Product Proof Target.

## 9. Open questions for Hamza

1. Is PC1's immediate target an internal/synthetic reference candidate, or should PM planning orient toward a named real pilot organization? This determines whether NW-093 becomes near-term.

2. Which domain should drive first vocabulary validation: health CHV, logistics distribution, WASH/infrastructure, agriculture, humanitarian field operations, or a deliberately domain-neutral demo?

3. Should S06/entity lifecycle remain entirely outside PC1, or should PM keep a parallel near-future lane for maintained known things because first users will expect registry-like behavior?

4. What is the minimum reporting/oversight promise for PC1: latest synced scoped work plus freshness and unresolved attention only, or any aggregate dashboard/export behavior that would require NW-044?

5. What evidence should let PM/PO say "PC1 is usable": passing internal demo, synthetic end-to-end reference scenario, external user walkthrough, or real pilot data?

6. Who is the product owner for final tradeoffs between value priority and architecture route safety when those conflict?

7. Should PM-facing backlog rows keep the NW style, or should PM maintain a parallel product backlog view that links to NW rows only when work is selected?
