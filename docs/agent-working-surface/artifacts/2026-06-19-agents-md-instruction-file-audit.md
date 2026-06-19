# AGENTS.md Instruction-File Audit

## 1. Isolation check

- Working directory for this audit runtime: `/home/hamza/agency-agents`.
- Target repository audited as evidence only: `/home/hamza/datarun-platform`.
- Target root `AGENTS.md` was **not** auto-loaded for this session because Codex was launched outside `/home/hamza/datarun-platform`; it was read explicitly as the subject of the audit. The target root `CLAUDE.md` exists but only says `Read [AGENTS.md](AGENTS.md)`.
- No repository source, test, workflow, or tracked documentation files were edited. `git status --short` in `/home/hamza/datarun-platform` returned no output before report writing.

Repository files read:

- `AGENTS.md`
- `CLAUDE.md` narrowly, to check cross-agent duplication/conflict
- `README.md`
- `docs/status.md`
- `docs/agent-working-surface/README.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/commit-workflow.md`
- `docs/documentation-organization.md`
- `server/pom.xml`
- `mobile/pubspec.yaml`
- `.review/untracked-user-notes/analysis/product-goal-pm-handoff-audit-2026-06-19.md`
- `.review/untracked-user-notes/analysis/test-ci-validation-strategy-audit-2026-06-19.md`

External sources checked:

- OpenAI Codex `AGENTS.md` guidance: Codex reads `AGENTS.md` before work, discovers root/nested files, closer files override broader guidance, and combined project instructions stop at a size limit ([OpenAI Developers](https://developers.openai.com/codex/guides/agents-md)).
- OpenAI Codex customization guidance: keep repo files focused on team/codebase rules; use skills for richer reusable workflows so they do not bloat default context ([OpenAI Developers](https://developers.openai.com/codex/concepts/customization)).
- OpenAI Codex prompting guidance: Codex works better with validation steps and smaller focused tasks ([OpenAI Developers](https://developers.openai.com/codex/prompting)).
- GitHub Copilot custom instructions: repository-wide `.github/copilot-instructions.md`, path-specific `.github/instructions/*.instructions.md`, and nearest `AGENTS.md` files are all supported; GitHub warns to avoid conflicting instruction sets ([GitHub Docs](https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/add-custom-instructions/add-repository-instructions)).
- GitHub Copilot coding-agent task guidance: agent tasks should be clear, scoped, and reviewable ([GitHub Docs](https://docs.github.com/en/copilot/tutorials/cloud-agent/get-the-best-results)).
- Claude Code best practices: provide executable verification, manage context aggressively, keep `CLAUDE.md` short and broadly applicable, put task-specific knowledge in skills, and use hooks for deterministic enforcement ([Anthropic Docs](https://code.claude.com/docs/en/best-practices)).
- AGENTS.md open-format guidance: use AGENTS.md for setup, tests, conventions, and nested subproject instructions ([agents.md](https://agents.md/)).
- Empirical context-file studies: configuration smells in AGENTS/CLAUDE files, AGENTS.md impact on coding agents, agent README maintenance patterns, and instruction-file impact on agentic PR outcomes ([Configuration smells](https://arxiv.org/abs/2606.15828), [Evaluating AGENTS.md](https://arxiv.org/abs/2602.11988), [Agent READMEs](https://arxiv.org/abs/2511.12884), [Instructions-as-Code](https://arxiv.org/abs/2606.13449), [AGENTS.md efficiency](https://arxiv.org/abs/2601.20404)).

## 2. Verdict

**Rewrite around product-slice implementation.**

The current root `AGENTS.md` is useful as a safety router, but it is optimized for architecture stewardship and protocol protection more than for day-to-day PC1/product-slice delivery. It is only 141 lines, so the issue is not raw length. The issue is salience: the default implementer packet routes fresh sessions toward the working surface, decision anchors, and CDL before it gives them a product-first path, a validation matrix, or clear server/mobile nested commands.

The right target is a shorter root file plus nested files:

- root `AGENTS.md`: product-slice startup, task packet contract, stop conditions, validation evidence rules, commit trace pointer;
- `server/AGENTS.md`: server commands and Spring/Postgres test notes;
- `mobile/AGENTS.md`: Flutter tests, analyzer status, Android compile command;
- `contracts/AGENTS.md`: contract roles, parity checks, and cross-runtime fixture guidance;
- steward-only guide: broad architecture routing, CDL slicing, gap classification, and dispatch rules.

This is not a "delete the guardrails" recommendation. The guardrails are valuable. They should stop unsafe work, not become the default mental model for every small implementation task.

## 3. What is working

Useful root-level content that should stay, in shorter form:

- `AGENTS.md:3` correctly says the file is a context router, not a request to read the whole documentation set.
- `AGENTS.md:7-14` gives a startup packet and names `docs/status.md` Current Routing. This is the right pattern, but it should be product-first and less architecture-heavy.
- `AGENTS.md:24` has the right stop behavior when sources disagree: stop and surface drift before implementing.
- `AGENTS.md:55-59` correctly distinguishes bounded implementer sessions and defines what a task packet should contain.
- `AGENTS.md:61-68` gives a compact repository map. This is useful root context if kept short.
- `AGENTS.md:83` gives the essential contract trigger: when data crosses server/mobile/process boundaries, check `contracts/`.
- `AGENTS.md:89-93` and `AGENTS.md:139-141` contain real stop conditions for dangerous boundary changes.
- `AGENTS.md:95-111` gives runnable baseline commands. The commands need to be completed and split, but build/test guidance belongs in agent instructions.
- `AGENTS.md:115-121` has good general implementer hygiene: prefer existing patterns, keep changes scoped, update status only when work begins or lands, leave unrelated dirty work alone.
- `AGENTS.md:125-141` points to the durable commit workflow and gives a minimal commit trace shape. The full flow should be linked, not restated.

## 4. What is causing stale/product-drift risk

| Offending line/section | Risk type | Why it matters | Recommended handling |
|---|---|---|---|
| `AGENTS.md:7-14`, default implementer packet | architecture over-priming; product-goal obscuring | The packet says implementers should read the agent working surface and decision-anchor layer by default. That makes ordinary PC1/product work start from architecture routing instead of user-visible outcome, active NW, touched files, and validation. This conflicts with the product audit finding that PM handoff is still too route-heavy. | Rewrite startup as product-first: task prompt, `docs/status.md` Current Routing, active NW row if named, PC1/product spec or PM handoff only when product/user-visible, touched code/tests/contracts. Route to decision-anchor/CDL only on triggers. |
| `AGENTS.md:13` names `docs/agent-working-surface` broadly | blind reference; context bloat | This is a directory-level reference with "especially" guidance, but no narrow "read only when..." condition for ordinary implementers. It can pull agents into BAR/backlog/artifacts/decision layers unnecessarily. | Replace with one root rule: "Open working-surface routing only when the task is an NW selection/status/gap/steward task or a boundary trigger fires." |
| `AGENTS.md:14` sends agents to the CDL README in the default packet | architecture over-priming | Even with "only when authority text is needed," putting the CDL in the default packet increases its salience for routine product/code tasks. The product audit says architecture vocabulary already makes PM/product work harder. | Move CDL route to a "when to escalate" section and to steward guide. Root should name exact triggers, not start ordinary agents near the ledger. |
| `AGENTS.md:16-23`, routed docs list | context bloat; blind reference | It says not to read everything, then names `docs/architecture/`, phase specs, scenarios, exploration archives, CDL, IDRs, and decision-anchor layer. Several are valid but too broad for root auto-load. | Keep only the principle in root. Move the detailed routing catalog to `docs/agent-working-surface/README.md` or steward guide. |
| `AGENTS.md:26-50`, CDL query-tool help block | steward-only rule; task-specific workflow leakage | A 25-line command-help block in an auto-loaded file spends root attention on rare architecture slicing. This is precisely the kind of specialized workflow that Codex/Claude guidance says to move to closer docs or skills. | Move to `docs/agent-working-surface/steward-session-guide.md` or the CDL README. Root can say: "Use `scripts/query_cdl.py` when a task explicitly routes to CDL." |
| `AGENTS.md:54`, "Architecture-steward sessions may read broadly..." | skill leakage; architecture over-priming | Broad-read steward behavior is not an always-needed implementer instruction. In an auto-loaded root file it normalizes broad exploration and protocol reconciliation for tasks that need a small diff. | Move to `docs/agent-working-surface/steward-session-guide.md`. Root should keep only "default sessions are implementer sessions unless the task says steward/audit/routing." |
| `AGENTS.md:70-83`, detailed contract taxonomy | contract-specific rule; context bloat | The contract summary is useful, but it is dense and changes as contracts evolve. It belongs near `contracts/`, where nested guidance will load for contract work and stay out of mobile/server-only sessions. | Move to `contracts/AGENTS.md`; root keeps a two-line trigger and a pointer. |
| `AGENTS.md:85-93`, architectural guardrails | conflicting instruction risk; product-goal obscuring | These guardrails are mostly correct, but all are framed as architecture authority. Ordinary product implementers need a simpler rule: ship the active slice unless the change touches explicit structural triggers. | Rewrite root as stop conditions; keep detailed route logic in gap playbook/steward guide. |
| `AGENTS.md:95-111`, build/test commands | verification gap | Missing current important gates from the test audit: `flutter analyze` and Android `:app:compileDebugKotlin`. It also does not distinguish server-only, mobile-only, contract, docs, and native-change gates. | Root should point to a validation matrix. Server/mobile commands move to nested files. Add current analyzer status: not a hard gate until green/baselined. |
| `AGENTS.md:117-119`, docs organization pointer | duplicated rule | This duplicates `docs/documentation-organization.md`, which already governs durable docs. It is useful as a pointer but too prominent inside root working practice. | Keep as a one-line pointer under "durable docs only"; details stay in documentation organization doc. |
| `AGENTS.md:123-141`, commit flow | duplicated rule; context bloat | The root repeats commit sequence and message/trailer rules already defined in `docs/commit-workflow.md`. It is accurate but should not compete with the durable workflow doc. | Root keeps: "If commits are authorized, follow `docs/commit-workflow.md`; include NW trailer and validation evidence." |
| Root has no product-first startup path | product-goal obscuring | After the product audit, the default instruction file should orient agents to the active product slice and PC1 outcome before architecture/protocol vocabulary. | Add "Product-first rule": describe the user-visible outcome and PC1/non-goal boundary before touching architecture docs, unless the task is explicitly steward/architecture. |
| Root has no explicit "do not choose next NW by yourself" rule | stop condition | `docs/status.md` says no active implementation gate and next evidence command is to select the next NW row before implementation. Root should make this a default stop condition. | Add stop: if no task/NW/active slice is selected and the request implies implementation, ask or route selection before coding. |
| Deterministic rules live mostly in prose | deterministic-rule-in-prose | `git diff --check`, analyzer status, Android compile, contract parity, and CI expectations should not rely only on agent memory. The test audit found CI only covers server. | Move enforceable checks into CI/hooks where possible; root only tells agents how to report evidence. |
| README line 15 comparison drift | stale/fossilized instruction | `README.md` still points to `architecture-rationale-and-routing-companion.md`, while `docs/status.md` says the old rationale companion is retired. This is not an AGENTS.md defect, but it shows why root should avoid naming stale broad docs. | Fix separately as documentation hygiene; do not add the retired companion to root. |

Direct answers to the specific checks:

- Yes, root says "do not read the whole documentation set" but then routes too broadly through `docs/agent-working-surface`, decision anchors, CDL, architecture docs, phase specs, scenarios, and exploration archives.
- Yes, architecture-sensitive routing feels mandatory for ordinary product/code tasks because it is in the default packet and steward split.
- "Architecture-steward sessions may read broadly" does **not** belong in the auto-loaded root. It belongs in a steward guide.
- Contract details should move closer to `contracts/`.
- Server/mobile build and test commands should move to `server/AGENTS.md` and `mobile/AGENTS.md`, with a short root matrix.
- Product-slice implementation needs a product-first startup path.
- Current root has too much authority protection relative to "ship and verify the active NW."
- Deterministic checks should be encoded in CI/hooks where possible; prose should describe evidence, not enforce mechanics.
- AGENTS.md duplicates `commit-workflow.md`, `documentation-organization.md`, and the working-surface README.
- Some references are blind or too broad, especially directory-level references.
- The main conflict with the product/test audits is priority: the root file over-optimizes architecture stewardship while the audits recommend PC1 product handoff, a product-level smoke gate, and clearer validation gates.

## 5. Section disposition table

| Current section | Keep / move / delete / rewrite | New home | Reason |
|---|---|---|---|
| Title and purpose (`AGENTS.md:1-3`) | Rewrite | root `AGENTS.md` | Keep the router idea, but state that default sessions optimize for bounded product-slice implementation and verification. |
| Start Here / default implementer packet (`5-15`) | Rewrite | root `AGENTS.md` | Make it product-first and task-first; remove default pull toward working-surface/decision/CDL docs. |
| Routed docs list (`16-24`) | Rewrite / move | root summary plus `docs/agent-working-surface/README.md` | Root needs only trigger rules. Detailed routing belongs in the working-surface router. |
| Slicing Canonical Decisions (`26-50`) | Move | `docs/agent-working-surface/steward-session-guide.md` or CDL README | Steward-only / architecture-sensitive tool usage should not load into every implementer session. |
| Steward, Implementer, Owner Split (`52-59`) | Split | root `AGENTS.md` plus steward guide | Keep implementer/task packet contract in root; move steward broad-read and dispatch behavior. |
| Repository Map (`61-68`) | Keep, trim | root `AGENTS.md` | Useful always-needed orientation. Keep terse. |
| Contracts Guidance (`70-83`) | Move | `contracts/AGENTS.md` | Contract-specific detail should load when working under `contracts/` or when a task triggers cross-boundary behavior. |
| Architectural Guardrails (`85-93`) | Rewrite | root stop conditions plus gap playbook | Keep high-risk stop conditions in root; route classification details through gap playbook/steward guide. |
| Build And Test (`95-111`) | Split / rewrite | root validation summary, `server/AGENTS.md`, `mobile/AGENTS.md` | Current commands are incomplete and not area-specific. Mobile analyzer/native compile guidance is missing. |
| Working Practice (`113-121`) | Keep, trim | root `AGENTS.md` | These are always-needed implementer rules. Keep concise and avoid duplicating docs organization details. |
| Commit And Progress Flow (`123-141`) | Move / condense | `docs/commit-workflow.md`; root pointer only | Durable flow already lives elsewhere. Root should give only the trigger and minimum evidence/trailer reminder. |
| Root `CLAUDE.md` pointer | Keep | root `CLAUDE.md` | Good cross-agent hygiene: one source, no duplicate CLAUDE rule set. |
| Missing GitHub Copilot instructions | Optional add | `.github/copilot-instructions.md` | If Copilot is used, create a short file that points to root AGENTS and avoids duplicate authority. |

## 6. Proposed root AGENTS.md target

Replacement outline only, capped under 80 lines:

```md
# Datarun Platform - Agent Instructions

Purpose: keep agent sessions focused on the active product/task slice, protect
accepted boundaries, and leave verifiable evidence. This is a router, not a
request to read the whole docs tree.

## Default Startup Packet

1. Read the user task and identify the active slice/NW if named.
2. Read `docs/status.md` Current Routing.
3. Read only the code, tests, contracts, or specs named by the task or touched files.
4. For user-visible/product work, anchor the change to the PC1 product goal,
   accepted product spec/PM handoff, and explicit non-goals before architecture routing.
5. If no active slice is selected and implementation is implied, stop and ask
   for the selected NW/product slice before coding.

## Task Packet Contract

A good task packet states: goal, files to read, accepted boundaries, forbidden
work, expected tests, commit/acceptance boundary, and stop conditions.

Default role is implementer. Use steward routing only when the task explicitly
asks for architecture/gap/status/backlog routing or a stop trigger fires.

## Product-First Rule

For ordinary implementation, describe the user/deployment outcome first, then
the implementation surface. Do not turn product labels into authority,
identity, scope, contract, or storage primitives.

## Architecture / Gap Routing Triggers

Stop and route through the gap playbook before implementation if the change
would affect envelope fields/types, stored event meaning, sync/access scope,
authority sources, durable workflow state, resolver truth, deployer config
power, retention/security promises, reporting/audit breadth, tenant/runtime
partitioning, or any blocked/deferred row in `docs/status.md`.

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only when a trigger fires or the task is a steward/routing task. Use CDL/IDRs
only when routed by status, task, code comments, contracts, or gap classification.

## Repository Map

- `contracts/` process/wire contracts and shared fixtures.
- `server/` Spring Boot backend.
- `mobile/` Flutter client.
- `docs/status.md` current routing and standing.
- `docs/specifications/` accepted product/platform behavior.
- `docs/agent-working-surface/` steward routing, backlog, BAR, prompts.

## Validation

Use the narrowest relevant focused test first, then the required full gate for
the touched surface. Report exact command, cwd, result, count/duration when
available, and skipped-gate rationale.

- Server: see `server/AGENTS.md`.
- Mobile: see `mobile/AGENTS.md`.
- Contracts: see `contracts/AGENTS.md`.
- Docs-only: run `git diff --check`.

`flutter analyze` is not a hard gate until the known issues are fixed or
baselined. Android compile is required for native/platform/auth/plugin changes.

## Working Rules

Prefer existing patterns. Keep changes scoped. Leave unrelated dirty work alone.
Update status/backlog/BAR only when the task authorizes that state change.
For durable docs, use `docs/documentation-organization.md`.

## Commits

Commit only when authorized. Follow `docs/commit-workflow.md`. For NW-owned
work include `NW: NW-###` and validation evidence. Do not mark work accepted
just because code was written.

## Steward Guidance

Broad reading, CDL slicing, gap classification, dispatch packets, and
architecture reconciliation live in
`docs/agent-working-surface/steward-session-guide.md`.
```

## 7. Proposed split

| File | Should exist? | Why it should exist | What it should contain | What should not go there |
|---|---:|---|---|---|
| `server/AGENTS.md` | Yes | Server validation and local setup are specific enough to load only for backend work. | Test DB prerequisite, `./mvnw -Dtest=... test`, full `./mvnw test`, CI `./mvnw verify` equivalence, test profile notes, server image verification pointer, concise Spring/Postgres gotchas. | Product/PM routing, CDL query help, full commit workflow, mobile commands. |
| `mobile/AGENTS.md` | Yes | Mobile has distinct Flutter, analyzer, and Android native gates that are missing from root. | Focused `flutter test`, full `flutter test`, `flutter analyze` current red/baseline rule, Android `mobile/android/gradlew :app:compileDebugKotlin`, fake/mock caution from the test audit. | Server DB setup, architecture taxonomy, broad app UX strategy, every widget-test convention. |
| `contracts/AGENTS.md` | Yes | Contract roles and parity checks are cross-runtime and should be close to the files they protect. | Contract file roles, schema/fixture parity commands, "contracts are not generated code" warning, shared fixture drift guidance, when server/mobile tests are both required. | Full list of architecture decisions, product scenario history, server/mobile implementation internals beyond contract impact. |
| `docs/agent-working-surface/steward-session-guide.md` | Yes | Broad reading, gap classification, CDL slicing, and dispatch are steward workflows, not default implementer startup. | Steward vs implementer roles, when broad reading is allowed, `scripts/query_cdl.py` usage, gap routing workflow, dispatch packet checklist, status/backlog/BAR reconciliation rules. | Ordinary build commands except as references to validation matrix; product-level instructions that should be in product handoff docs. |
| `.github/copilot-instructions.md` | Optional, recommended if Copilot is used | GitHub supports repository-wide Copilot instructions and separate AGENTS files. A short bridge prevents Copilot from missing the repo's instruction surface. | One-page bridge: "Read root `AGENTS.md`; for server/mobile/contracts use nested files; avoid conflicting personal/repo/org instructions; run/report validation." | Duplicated normative content from AGENTS, commit workflow, or gap playbook. |

## 8. Recommended NWs

| NW | Type | Priority | Why now | Files to touch | Acceptance criteria | Validation command | Stop condition |
|---|---|---:|---|---|---|---|---|
| Rewrite and split root agent instructions | docs/control-surface | P1 | Product and test audits both show the root should optimize for PC1/product-slice implementation and explicit validation, not broad architecture stewardship. | `AGENTS.md`, `server/AGENTS.md`, `mobile/AGENTS.md`, `contracts/AGENTS.md`, `docs/agent-working-surface/steward-session-guide.md`, `docs/agent-working-surface/README.md` | Root is under 80 lines, product-first, includes stop triggers and evidence rules, removes CDL help block and contract taxonomy, and nested files hold local commands. No product/platform behavior changes. | `git diff --check`; targeted grep for product-first startup, nested file pointers, and removed CDL help block from root. | Stop if the rewrite changes accepted authority, alters NW standing, or tries to resolve product priority instead of routing instructions. |
| Create validation command matrix for agents | docs/test-control | P1 | Test audit found good suites but partial CI, missing mobile CI, red analyzer, and manual Android compile. Agents need one durable matrix. | New `docs/agent-working-surface/validation-matrix.md` or equivalent, root/nested AGENTS pointers, maybe `docs/status.md` link | Matrix maps touched surface to focused/full commands, CI/manual status, blocking rule, evidence format, and known analyzer status. | `git diff --check`; optionally run no tests because this is docs-only unless commands are changed materially. | Stop if it claims `flutter analyze` is green before the issues are fixed/baselined. |
| Add product-first task packet / PM handoff router | docs/product-control | P1 | Product audit found PC1 clear but not PM self-serve. Root AGENTS should point implementers to a concise product packet when user-visible work is active. | Product handoff doc from product audit recommendation, `AGENTS.md`, possibly `docs/status.md` | A task packet can name user outcome, PC1 journey, in/out scope, validation expectation, and stop conditions without requiring CDL/BAR fluency. | `git diff --check`; review against product audit report. | Stop if the doc creates new product authority instead of summarizing accepted specs and explicit open questions. |
| Move deterministic checks toward CI/hooks | ci/tooling | P2 | Root prose cannot enforce analyzer, Android compile, whitespace, or contract parity. External guidance and the test audit favor executable verification. | `.github/workflows/`, optional hook/config files, validation matrix | Mobile CI exists or a staged plan exists; `git diff --check` and relevant test/analyze/compile gates are represented by automation or explicit manual fallback. | Workflow validation plus local `flutter test`; analyzer only blocks after green/baselined. | Stop if CI work tries to fix unrelated analyzer/code issues in the same NW without an explicit implementation scope. |
| Add Copilot instruction bridge | docs/agent-config | P3 | GitHub uses `.github/copilot-instructions.md` and can also use AGENTS. A bridge reduces cross-tool mismatch if Copilot is used. | `.github/copilot-instructions.md`, maybe `CLAUDE.md` remains pointer | Copilot file is short, non-conflicting, and points to root/nested AGENTS plus validation evidence expectations. | `git diff --check`. | Stop if GitHub Copilot is not part of Hamza's workflow or if the file duplicates full AGENTS content. |

## 9. Minimal safe change

The smallest change that would reduce product-stall risk without destabilizing the control system:

1. In root `AGENTS.md`, rewrite only the `Start Here` section.
2. Add a first rule: "Default sessions are product-slice implementer sessions unless the task explicitly says steward/audit/routing."
3. Replace default items 5-6 with: "Open decision-anchor/CDL/architecture docs only when a listed stop trigger fires, the task is a steward/routing task, or the active NW names them."
4. Add two missing mobile validation lines under Build/Test: `flutter analyze` is currently known-red and not a hard gate until fixed/baselined; Android compile command lives under `mobile/android/gradlew`.

Do not implement that as part of this audit. It is the safe first NW if Hamza wants a low-risk trim before the full split.
