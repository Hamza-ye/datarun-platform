# Working-Surface Process Hygiene Assessment

Status: non-binding assessment
Document type: routing_artifact
Owner: Hamza
Source: 2026-06-17 documentation / working-surface hygiene assessment request
Authority: none; this artifact proposes cleanup routing only and does not change architecture, contracts, accepted behavior, code, BAR, backlog, or status standing.
Last reviewed: 2026-06-17
Supersedes: none
Related: `AGENTS.md`; `docs/status.md`; `docs/agent-working-surface/README.md`; `docs/agent-working-surface/decision-anchor-layer/README.md`; `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`; `docs/documentation-organization.md`; `docs/commit-workflow.md`

## A. Executive Finding

The working-surface process is mostly coherent, but it is over-distributed. The active model is clear when read in the intended order: `AGENTS.md` starts an agent, `docs/status.md` Current Routing gives current standing, the working-surface README gives source order, the decision-anchor layer classifies pressure, `docs/documentation-organization.md` chooses durable homes, `docs/commit-workflow.md` governs progress transitions, and BAR/NW record accepted standing and evidence. [confidence: high]

The main safety issue is not missing process. It is stale authority wording and repeated local summaries that can pull agents back into retired or provenance surfaces. The highest-risk example is `docs/architecture/README.md`, which still says ADRs remain decision authority even though the active routing surface and CDL README make the CDL the current architecture authority. [confidence: high]

The safest cleanup path is a sequence of small documentation-hygiene slices that clarify routing and links before rewriting normative process. No architecture, contracts, BAR, backlog, status standing, or code should change in the first slice. [confidence: high]

## B. Active Process-Authority Map

| Surface | Claimed authority or role | Category | Notes | Confidence |
|---|---|---|---|---|
| `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md` via `docs/architecture/adrs-decisions-canonical-ledger/README.md` | Single authoritative architecture decision source from Phase 4 closure forward. | Architecture authority | Use README index, JSON, or `scripts/query_cdl.py`; avoid loading the full CDL by default. | high |
| `contracts/` | Wire, schema, sync, flag, shape, pattern, and shared-fixture contracts. | Contract authority | First check when server/mobile/process-boundary data changes. | high |
| `AGENTS.md` | Fresh-session context router and implementer bootstrap. | Agent execution guidance | Should stay short and route to active surfaces rather than carry full process. | high |
| `docs/status.md` Current Routing | Low-token current standing and active-route bootstrap. | Status/current routing | The top section is active; lower historical sections are useful but can mislead if treated as current source order. | high |
| `docs/agent-working-surface/README.md` | Active post-Phase-4 agent working-surface router and source order. | Agent execution and routing guidance | Best home for stable agent operating model and source-order overview. | high |
| `docs/agent-working-surface/decision-anchor-layer/README.md` | Active stewardship routing surface for DEC anchors and gap routing. | Gap/decision routing | Explicitly subordinate to CDL, contracts, BAR/NW, and current code evidence. | high |
| `docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md` | DEC-to-CDL map plus accepted extension inputs. | Gap/decision routing aid | Not architecture authority; helps classify future work. | high |
| `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md` | Active routing rules, vocabulary guardrails, known gaps, and prompt checklist. | Gap routing | Owns pressure classification and closure-path selection. | high |
| `docs/agent-working-surface/baseline-acceptance-register.md` | Current baseline acceptance status after Phase 4. | BAR/accepted standing | Governs accepted/candidate/deferred/future-decision baseline capability standing. | high |
| `docs/agent-working-surface/platform-next-work-backlog.md` | Post-Phase-4 work routing and evidence trace. | Backlog routing | Routes selected work and exit evidence; not architecture authority. | high |
| `docs/documentation-organization.md` | Durable documentation homes, metadata, indexes, lifecycle, and supersession. | Documentation organization | Canonical home for where durable outputs belong. | high |
| `docs/commit-workflow.md` | Commit structure and progress-state transitions. | Commit/progress workflow | Does not grant implementation authority; governs route, decide/specify, implement, accept, checkpoint, and hygiene commits. | high |
| `docs/specifications/` | Accepted product and platform behavior below architecture/contracts. | Durable accepted behavior | Index says no accepted specs are indexed at root yet, though platform specs exist under subdirectories from NW-068. | medium |
| `docs/operations/` | Durable operational policies, runbooks, rehearsal plans, and records. | Operational policy/procedure/evidence | Subordinate to CDL, contracts, specs, and implemented behavior. | high |
| `docs/agent-working-surface/operational-ux-layering-companion.md` | Product/UX vocabulary and layering guardrail. | Product/UX routing companion | Explicitly non-authoritative; useful before UI/reporting/workflow/product-design slices. | high |
| `docs/agent-working-surface/escape-hatch-register.md` | Measured evolution-path routing register. | Evolution routing | Not architecture authority and not implementation permission. | high |
| `docs/agent-working-surface/artifacts/` | Non-binding routing/exploration artifacts. | Historical/provenance/routing material | Useful for successor selection; not final accepted homes. | high |
| `docs/agent-working-surface/prompts/` | Bounded execution packets. | Agent task execution input | Prompts are not decisions, specs, evidence, or acceptance by themselves. | high |
| `docs/README.md`, `docs/constraints.md`, `docs/principles.md`, `docs/scenarios/`, `docs/access-control-scenario.md` | Vision, constraints, principles, and problem-space evidence. | Product/problem context | They inform pressure and product fit, but do not override CDL/contracts/BAR. | high |
| `docs/architecture/README.md` | Architecture overview. | Architecture description / stale authority wording | Useful view document, but its ADR-authority wording conflicts with current CDL authority. | high |
| `docs/decisions/` IDRs | Existing IDR provenance and validation inputs; some accepted implementation decisions remain referenced by BAR/NW. | Historical/provenance and bounded decision inputs | Current routing warns not to rely on IDR prose as normative when durable extraction is needed. | medium |
| `.review/`, old review packs, checkpoints, phase chronology | Extraction chronology, audits, historical snapshots, and provenance. | Historical/provenance material | Do not route future work directly from these unless current routing explicitly requires it. | high |

## C. Scattered-Responsibility Map

| Responsibility | Current locations | Current pattern | Risk | Confidence |
|---|---|---|---|---|
| Agent role expectations | `AGENTS.md`; working-surface README; prompts README; commit workflow task-packet boundary | `AGENTS.md` separates steward and implementer behavior; prompts define bounded execution packets. | Agents may over-interpret named roles as authority owners rather than task lenses. | high |
| Source-reading order | `AGENTS.md`; `docs/status.md` Current Routing; working-surface README; decision-anchor README; selected prompts | Consistent intent: start narrow, slice CDL, open exact contracts/code/specs only when routed. | Duplication can drift; old prompts may point at retired surfaces. | high |
| Authority hierarchy | AGENTS guardrails; CDL README; working-surface README; decision-anchor README; BAR; backlog; documentation organization | CDL > contracts > active routing/BAR/NW/specs/ops by category. | `docs/architecture/README.md` and historical `docs/status.md` sections can imply ADR/IDR authority. | high |
| Gap/decision routing | decision-anchor README; gap-routing playbook; architecture-decision anchors; escape-hatch register | Pressure is classified before implementation. | Agents may skip classification when pressure looks implementation-shaped. | high |
| NW readiness | platform-next-work backlog; prompts README; gap-routing playbook implementation prompt checklist; `AGENTS.md` task-packet guidance | NW row plus bounded prompt for non-trivial work. | Readiness rules are spread but mostly aligned. | high |
| NW closure | platform-next-work backlog Work Item Trace; commit workflow final acceptance check; BAR evidence rule; `docs/status.md` Current Routing | Exit condition links durable output, evidence, successors, and status fold-forward. | Closure may be treated as prompt completion if agents skip backlog trace rules. | medium |
| Durable output homes | documentation-organization; working-surface README; backlog; prompts README; specs/operations indexes | `docs/documentation-organization.md` is canonical; others mirror it. | Repeated home lists are useful but can become stale. | high |
| Commit sequencing | commit-workflow; AGENTS Commit And Progress Flow; backlog Work Item Trace; prompt templates | Commit workflow is canonical and AGENTS summarizes. | Minimal; AGENTS may need to remain summary-only. | high |
| Status/BAR/backlog updates | `docs/status.md`; BAR; platform backlog; documentation organization index/link rules; commit workflow accept section | Standing changes only after evidence and only on materially affected surfaces. | Hygiene tasks can accidentally update standing because the update rules are nearby. | high |
| Documentation hygiene | documentation-organization; commit-workflow Documentation Hygiene; backlog NW-074; artifacts/prompts README | `docs(hygiene)` means meaning and accepted standing unchanged. | Broad cleanup can accidentally become semantic rewrite. | high |
| Conflict handling | AGENTS; working-surface README stop conditions; decision-anchor README source order; gap-routing playbook classification/re-test rules | Stop and report when active sources conflict; do not resolve by assumption. | Strong guidance exists, but stale authority text increases the chance of false conflicts. | high |

## D. Overlap/Conflict Register

| Topic | Documents | Classification | Finding | Source order to resolve | Confidence |
|---|---|---|---|---|---|
| CDL versus ADR authority | `docs/architecture/README.md`; CDL README; `AGENTS.md`; working-surface README; `docs/README.md` | conflicting guidance / stale guidance | `docs/architecture/README.md` says ADRs remain decision authority. Active surfaces say the CDL is the single architecture authority and old ADRs are provenance. | CDL README and `AGENTS.md`/working-surface routing win; architecture README should be corrected as subordinate overview. | high |
| IDR prose as active authority | `docs/status.md` historical sections; Current Routing; backlog NW-069 through NW-074; drift audits | stale guidance / candidate for consolidation | Top Current Routing warns before relying on IDR prose as normative, but lower historical sections still call many IDRs "active." | Current Routing, BAR/NW, contracts, accepted specs, and gap playbook classification re-test rule should govern. | high |
| Durable output homes repeated | documentation-organization; working-surface README; backlog; prompts README; specs/operations README | useful local reminder | Repetition is mostly aligned and helpful at point of use. | `docs/documentation-organization.md` remains canonical; other docs should be pointers. | high |
| Commit flow repeated | commit-workflow; AGENTS; backlog; prompts README | useful local reminder | The content is aligned and appropriately summarized for agents. | `docs/commit-workflow.md` remains canonical. | high |
| Prompt/artifact role | artifacts README; prompts README; backlog Work Item Trace; documentation organization | harmless repetition / useful local reminder | Multiple surfaces correctly say prompts/artifacts route work but are not durable accepted outputs. | Keep repeated reminders because agents often enter through prompts/artifacts. | high |
| Retired rationale companion | working-surface README; status; retired companion header; provenance index | resolved overlap | The companion exists but clearly redirects to the decision-anchor layer and is retired as an active surface. | Do not use retired companion except as provenance. | high |
| Problem-space labels | constraints; scenarios; access-control scenario; operational UX companion; gap playbook vocabulary guardrails | useful local reminder | Several docs warn that operational labels are not system identities, permissions, modules, or architecture. | Keep reminders near problem-space docs and UX companion. | high |
| First-deployment workshop chain | status; first-deployment router; backlog NW-058 | resolved overlap / historical material | Current docs say the workshop is closed and removed stage/role/gate chains should not be recreated. | Use first-deployment router and summary only when that lane is selected. | high |
| Escape hatch language | working-surface README; escape-hatch register; gap playbook | useful local reminder | Multiple surfaces say escape hatches require measured evidence and do not authorize implementation by themselves. | Keep concise reminders in routers; detailed trigger data belongs in escape-hatch register. | high |
| Specifications index standing | `docs/specifications/README.md`; status NW-068 | missing cross-link | Root specs README says no accepted specs are indexed "here yet"; status says NW-068 created indexed platform specs. This may be resolved in subdirectory indexes, but the root wording can confuse. | Check `docs/specifications/platform/README.md` before cleanup; root should not contradict accepted specs. | medium |
| Architecture description as active view | `docs/architecture/README.md`; working-surface README | misplaced authority | Architecture README is useful as an overview, but not an authority router. | Make it explicitly subordinate to CDL and current working-surface routes. | high |

## E. Reconstructed Operating Model

1. A pressure enters through a user request, scenario/product need, operations finding, code/test drift, or backlog row. The agent starts with `AGENTS.md`, `docs/status.md` Current Routing, and the bounded task packet. [confidence: high]

2. If the pressure is architecture-sensitive, the agent routes it through the decision-anchor layer and the gap-routing playbook: pressure -> vocabulary -> DEC anchor -> negative boundaries -> classification -> closure path -> bounded artifact. [confidence: high]

3. The classification determines the durable path: product/problem evidence, architecture decision, platform/product specification, implementation/tooling, or operational policy/procedure/evidence. Prompts and artifacts may route work, but do not replace durable outputs. [confidence: high]

4. A routed gap becomes an NW row when it is selected for visible work. The NW row records source, dependency, expected output, and exit condition; non-trivial work gets a bounded prompt with files to read, guardrails, forbidden work, tests, commit boundary, and stop conditions. [confidence: high]

5. Durable output home is chosen by `docs/documentation-organization.md`: CDL successor or delegated decision for architecture, `contracts/` for process/wire contracts, `docs/specifications/product/` or `docs/specifications/platform/` for accepted behavior, `docs/operations/` for policy/runbook/rehearsal, `docs/implementation/` for implementation design, and `artifacts/` for non-binding exploration. [confidence: high]

6. Evidence attaches through tests, code inspection, runtime probes, rehearsal records, accepted policies/runbooks, or verification summaries. The NW exit condition should link the evidence and any successor or deferral route. [confidence: high]

7. Acceptance is recorded only when exit conditions are met. BAR changes only for implementation capability standing; `docs/status.md` Current Routing summarizes currently relevant standing; backlog rows record accepted/deferred/future-decision work and evidence. [confidence: high]

8. Commits follow the progress transition being made: route, decide/specify/policy, implement and verify, accept, or checkpoint. Hygiene commits are only for organization changes that leave meaning and accepted standing unchanged. [confidence: high]

9. Agents limit scope by reading only routed surfaces, slicing CDL rather than reading it wholesale, opening exact contracts/code/specs named by the task, and stopping if sources conflict. [confidence: high]

## F. Agent Drift-Risk Map

| Drift risk | Why it can happen | Likely impact | Mitigation | Confidence |
|---|---|---|---|---|
| Treat historical docs as active authority | `docs/architecture/README.md` and lower `docs/status.md` sections retain old ADR/IDR authority language. | Wrong decision source; stale behavior preserved over CDL/contracts/specs. | First cleanup slice should correct authority labels and add "overview/provenance" wording. | high |
| Treat prompts/artifacts as durable outcomes | Prompts and artifacts often contain strong recommendations and exact guardrails. | Work may be considered accepted without specs, code, tests, ops evidence, or BAR/NW exit trace. | Keep prompt/artifact non-authority reminders; ensure backlog rows link actual durable outputs. | high |
| Over-read source material | Multiple routers point to architecture, scenarios, IDRs, phase specs, and audits under conditions. | Agents may spend context on chronology and revive outdated terms. | Keep start packets narrow; add "read only when routed" reminders at high-entry points. | high |
| Invent process roles | Historical workshop/role/gate packet chains and agent role names can look like a durable operating model. | AI agents may become implicit authority owners or add heavy process. | State that AI roles are task lenses; Hamza owns acceptance and authority decisions unless docs assign an existing source. | medium |
| Promote product/UX labels into architecture | Constraints, scenarios, access-control, and UX companion use human labels like coordinator, supervisor, auditor, work item, progress. | New identity categories, modules, config namespaces, event types, or scope mechanisms may be invented. | Keep local "labels are not architecture" reminders in problem-space and UX docs. | high |
| Change BAR/status/backlog during hygiene | Hygiene docs sit near acceptance and progress rules. | A non-binding cleanup could accidentally alter standing. | First cleanup should be docs(hygiene)-only with explicit no-standing-change boundary. | high |
| Implement directly from pressure | The project has detailed accepted behavior and tests, so implementation pressure can look clear before routing. | Architecture-sensitive changes may bypass gap classification. | Keep gap playbook's "route first" rule and prompt checklist in active startup path. | high |
| Preserve implemented drift as normative | Drift audits show implementation evidence can become load-bearing while still conflicting with right architecture. | Old implementation behavior may be canonized incorrectly. | Use classification re-test rule and durable behavior extraction rows before relying on IDR-era prose. | high |
| Treat escape hatches as permission | Escape hatches list allowed changes and triggers. | Agents may implement evolution without measured trigger evidence. | Keep "inactive until triggered" and evidence requirements visible. | high |
| Treat operations rehearsal evidence as production approval | Operations docs contain successful synthetic evidence. | Readiness claims may exceed actual proof. | Preserve distinctions among accepted procedure, synthetic rehearsal, partial NW-067, and real-production blockers. | high |

## G. Canonical-Home Recommendation

| Guidance type | Recommended canonical home | Point-of-use duplicates that should remain | Confidence |
|---|---|---|---|
| Agent startup / reading order | `AGENTS.md` for bootstrap; `docs/agent-working-surface/README.md` for stable source order | Short reminders in `docs/status.md` Current Routing and prompts. | high |
| Work-mode selection | `docs/agent-working-surface/README.md` plus `docs/agent-working-surface/prompts/README.md` for execution packets | Task-packet checklists in prompt files. | medium |
| Gap routing | `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md` | Short "route first" links in AGENTS, status, working-surface README, and prompts. | high |
| DEC anchors | `docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md` | Links from gap playbook and working-surface README. | high |
| Architecture authority | CDL and CDL README | Short "CDL wins" reminders in AGENTS, working-surface README, decision-anchor README, architecture README. | high |
| NW readiness / done criteria | `docs/agent-working-surface/platform-next-work-backlog.md` plus prompts README | Prompt-level acceptance boundaries and tests. | high |
| Documentation homes | `docs/documentation-organization.md` | Short canonical-home lists in working-surface README, backlog, prompts README, specs/operations README. | high |
| Commit and progress transitions | `docs/commit-workflow.md` | AGENTS summary and task-packet commit boundary. | high |
| Decision readiness | Gap-routing playbook and CDL successor/delegated decision process | Backlog row dependency fields and prompts. | medium |
| Agent task types | `AGENTS.md` for steward/implementer split; prompts README for execution-packet expectations | Individual prompt role/sequence instructions. | medium |
| Hygiene procedure | `docs/commit-workflow.md` Documentation Hygiene plus `docs/documentation-organization.md` lifecycle/supersession | NW-074 or future hygiene rows for bounded cleanup work. | high |
| Product/problem context | `docs/README.md`, `docs/constraints.md`, scenarios, access-control scenario | Operational UX companion reminders that labels are not authority. | high |
| Operational policy/procedure/evidence | `docs/operations/` indexes and files | Status Current Routing for currently relevant operations standing. | high |
| Non-binding exploration | `docs/agent-working-surface/artifacts/` | Backlog rows linking selected artifacts. | high |

## H. Items Not Safe To Canonize Yet

| Item | Why not safe yet | Needed confirmation or route | Confidence |
|---|---|---|---|
| A new consolidated "operating framework" document | Could create a parallel authority surface and restart the duplication problem. | Owner decision that a new framework is needed, plus a clear supersession plan. | high |
| AI-agent role taxonomy beyond steward/implementer/task packets | The solo-owner model uses agents as professional contributors, not authority owners. | Owner confirmation before adding any durable role taxonomy. | high |
| Old workshop stage/role/gate process | Current routing says the first-deployment workshop chain is closed and should not be recreated. | No canonization unless owner deliberately reopens a lightweight process with a new purpose. | high |
| `.review` workbench material | It is provenance and extraction chronology, not active routing. | Use only through the decision-anchor provenance index or a specific drift investigation. | high |
| Retired architecture rationale companion text | Stable roles are folded into the decision-anchor layer. | Do not revive; patch active routers if a missing stable rule is found. | high |
| IDR prose as normative durable behavior | Current routing says durable behavior extraction is needed before relying on scattered IDR-era notes. | Use NW-069 through NW-073 or a future bounded extraction route when needed. | high |
| Product/UX vocabulary as platform constructs | The UX companion is non-authoritative and explicitly forbids this. | Product/spec or architecture route depending on whether behavior crosses contracts/authority. | high |
| Broad NW-074 cleanup before owning specs exist | NW-074 depends on accepted durable specs from NW-068 and selected NW-069 through NW-073, except isolated stale traces. | Select specific extraction rows first or limit cleanup to isolated, non-semantic authority pointers. | high |
| Specifications root-index semantics | Root specs README may lag accepted subdirectory specs; exact state should be checked before changing. | Owner or steward should confirm whether root index should list subdirectory accepted specs. | medium |
| Independent human continuity model | Operations policy explicitly says this remains unproven and trigger-based. | Staffing/contract/regulatory/service-coverage trigger or owner decision. | high |

## I. Gradual Cleanup Plan

### Slice 1: Correct Stale Authority Pointers

Purpose: Remove the clearest false authority signal without changing meaning or standing. [confidence: high]

Affected docs: `docs/architecture/README.md`; optionally the headings/wording in the historical sections of `docs/status.md` that call ADR/IDR files "current authority" or "active" outside Current Routing. [confidence: high]

Risk level: low if limited to wording and links. [confidence: high]

Meaning change: no accepted behavior, architecture, BAR, backlog, contracts, or code changes; only route/link clarification. [confidence: high]

Required owner decision: confirm that `docs/architecture/README.md` should remain as a subordinate architecture overview rather than be retired or replaced. [confidence: medium]

Required successor NW: none for the narrow pointer correction; use NW-074 only if cleanup expands into broad IDR/reference draining. [confidence: high]

Verification/checks: `git diff --check`; inspect links; confirm no BAR/backlog/status standing changes except optional wording in historical labels. [confidence: high]

### Slice 2: Normalize Active Router Summaries

Purpose: Make `AGENTS.md`, working-surface README, decision-anchor README, and `docs/status.md` Current Routing point to canonical homes without carrying duplicate normative detail. [confidence: high]

Affected docs: `AGENTS.md`; `docs/agent-working-surface/README.md`; `docs/agent-working-surface/decision-anchor-layer/README.md`; `docs/status.md` Current Routing. [confidence: medium]

Risk level: low to medium because startup docs affect every agent session. [confidence: high]

Meaning change: should be routing/linking only. [confidence: high]

Required owner decision: confirm preferred minimal startup shape for solo-owner AI-agent work. [confidence: medium]

Required successor NW: create a bounded documentation-hygiene NW only if the edits are more than isolated link/wording fixes. [confidence: medium]

Verification/checks: compare source-order lists; confirm no new authority surface is introduced; `git diff --check`. [confidence: high]

### Slice 3: Add Process-Responsibility Index Instead Of New Process

Purpose: If needed, add a concise "where process guidance lives" index to the working-surface README or a small companion table, without duplicating rules. [confidence: medium]

Affected docs: preferably `docs/agent-working-surface/README.md`; possibly `docs/agent-working-surface/artifacts/README.md` only if artifact discoverability is part of the cleanup. [confidence: medium]

Risk level: medium because a new index can become another surface to maintain. [confidence: medium]

Meaning change: linking/routing only. [confidence: high]

Required owner decision: confirm that a process-responsibility index is worth maintaining. [confidence: high]

Required successor NW: optional hygiene NW if owner accepts the index. [confidence: medium]

Verification/checks: ensure every row links to an existing canonical home; no normative text copied. [confidence: high]

### Slice 4: Drain Misleading IDR-Era References

Purpose: After relevant durable specs exist, reduce references that make old IDR prose look like the durable target. [confidence: high]

Affected docs: active routers, status, module interfaces, prompts, old IDRs, and comments only where they materially mislead future work. [confidence: medium]

Risk level: medium to high if broad; low only for isolated stale traces. [confidence: high]

Meaning change: should be hygiene, but may uncover semantic drift requiring separate routing. [confidence: high]

Required owner decision: select NW-074 or a narrower successor; confirm which durable specs are accepted first. [confidence: high]

Required successor NW: NW-074 or a split successor after NW-069 through NW-073 as applicable. [confidence: high]

Verification/checks: affected-link audit; `rg` for "active IDR", "ADR authority", retired companion references; `git diff --check`; no deletion of accepted evidence. [confidence: high]

### Slice 5: Reconcile Specification Indexes

Purpose: Ensure specs root and subdirectory indexes accurately expose accepted product/platform specs. [confidence: medium]

Affected docs: `docs/specifications/README.md`; `docs/specifications/platform/README.md`; possibly `docs/status.md` Current Routing if current routes change. [confidence: medium]

Risk level: low if index-only. [confidence: medium]

Meaning change: no behavior change; discoverability only. [confidence: high]

Required owner decision: confirm desired root-index behavior. [confidence: medium]

Required successor NW: none if a small index correction; otherwise documentation hygiene row. [confidence: medium]

Verification/checks: inspect existing spec files and indexes; `git diff --check`. [confidence: high]

### Slice 6: Tighten Prompt Template Hygiene

Purpose: Prevent old prompt patterns from reopening retired surfaces or prescribing incorrect commit/standing changes. [confidence: medium]

Affected docs: `docs/agent-working-surface/prompts/README.md`; future prompt templates; only old prompts if materially misleading and selected by a cleanup route. [confidence: medium]

Risk level: medium because old prompts are provenance and should not be rewritten wholesale. [confidence: high]

Meaning change: future guidance only unless specific old prompts are patched. [confidence: medium]

Required owner decision: confirm whether old accepted prompts should remain untouched provenance except selected stale references. [confidence: high]

Required successor NW: likely NW-074 or a narrower prompt-hygiene row. [confidence: medium]

Verification/checks: sample current prompts; avoid broad churn; `git diff --check`. [confidence: high]

## J. First Recommended Slice

First recommended slice: a narrow authority-pointer hygiene pass. [confidence: high]

Purpose: Correct the highest-risk stale signal: `docs/architecture/README.md` should say it is a subordinate architecture overview and that the CDL is the current architecture authority; old ADR files are provenance. Optionally adjust `docs/status.md` historical labels so lower sections cannot be mistaken for current authority over the top Current Routing. [confidence: high]

Affected docs: `docs/architecture/README.md`; optional `docs/status.md` wording only in historical/detail sections. [confidence: high]

Risk level: low if no behavior or status semantics are changed. [confidence: high]

Meaning change: routing/link clarification only. [confidence: high]

Required owner decision: confirm whether the optional `docs/status.md` historical wording can be clarified in the same slice, or should be left for a separate status-history hygiene pass. [confidence: medium]

Required successor NW: none for `docs/architecture/README.md` pointer correction; use NW-074 only if expanding into broad stale IDR/reference cleanup. [confidence: high]

Verification/checks: `rg -n "ADRs remain|current authority|IDR-0[0-9].*active" docs/architecture/README.md docs/status.md`; `git diff --check`; manual review that no Current Routing, BAR, backlog status, contracts, or accepted behavior changed. [confidence: high]

## K. Owner Questions That Must Be Answered Before Any Cleanup

1. Should `docs/architecture/README.md` remain the canonical architecture overview, explicitly subordinate to the CDL, or should it be marked as historical until reconciled? [confidence: high] answer: explicitly subordinate to the CDL

2. For the first slice, should historical `docs/status.md` wording that calls IDRs "active" be clarified now, or should the first slice only fix `docs/architecture/README.md`? [confidence: medium] (yes, a small note: IDRs can stay but future works should not create idrs anymore, i don't want to leave a stale position in the working surface that might imply routing a decision or work to a new idr)

3. Do you want a small process-responsibility index in the working-surface README, or should the existing separate routers remain the only process map? [confidence: medium]

4. Should old accepted prompts remain untouched provenance unless selected by NW-074, even when they reference retired surfaces, or may isolated misleading prompt references be patched as hygiene? [confidence: medium]

5. Should the specs root README list accepted subdirectory specs directly, or is subdirectory indexing enough? [confidence: medium] (not important now)

6. For solo-owner operation, should docs say explicitly that AI agents can prepare analysis, implementation, verification, and recommendations, but Hamza remains the owner for acceptance of process/authority changes unless a document already assigns that authority? [confidence: high]
