# Project Checkpoint - 2026-06-11 (Decision Anchor Standing)

## 1. Bearing

- **Anchor commit**: `4a1fe6f docs(stewardship): promote decision-anchor routing surface`
- **Phase**: Post-Phase-4 stabilization, after decision-anchor surface promotion.
- **Momentum**: `ADVANCING` - the current platform baseline is accepted, and future work now has a smaller routing surface.
- **Last milestone**: `.review/001-architecture-decisions-listing-passes/` was committed as provenance, while the active future-work surface was reduced to `docs/agent-working-surface/decision-anchor-layer/`.
- **Horizon**: choose the next bounded route from the active backlog/gap register instead of reopening review chronology.

Standing point: the project is not waiting on another broad baseline acceptance
pass. BAR-001 through BAR-015 and BAR-104 remain the accepted baseline, and
`docs/status.md` says no baseline acceptance candidate is currently active.
The key change since the last checkpoint is stewardship hygiene: future
architecture-sensitive work should start from the active router, then the
decision-anchor layer, then BAR/NW evidence. The old rationale companion and
review extraction chronology are reference/provenance, not active surfaces.

The next useful move is a selected route, not more surface growth. The one
ready baseline-adjacent verification item is NW-057. Product/security/reporting
and scope questions remain valid, but they are future-decision or product route
work and should not be implemented directly from pressure.

## 2. Standing Snapshot

### Accepted Standing

| Standing | Rows or evidence | Meaning |
|---|---|---|
| `baseline_accepted` | BAR-001 through BAR-015 | Core envelope, append-only store, sync/backfill, payload contracts, flags, assignment/scope, mobile selective retention, identity, config, expressions, patterns, transition/domain detection, projection equivalence, and historical location-path immutability are accepted. |
| `baseline_accepted` | BAR-104 | Production provider-token authority is accepted only through explicit active `(issuer, subject) -> actor_id` binding, OIDC/JWKS validation, and deployment-managed binding provisioning. |
| `accepted runtime probes` | NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, NW-033/S26, NW-042/S22 | Current code has scenario-grade evidence for structured capture, offline/stale authority, supervisor review, logistics transfer, setup/config, reporting inputs, and coordinated distribution campaign pressure. |
| `accepted auth/admin evolution` | NW-048, NW-050, NW-052, NW-055 | Assignment-admin command capability and shared-device actor partitions are settled inside current authority guardrails. |
| `accepted product standing` | NW-056 | Product readiness is mapped: the kernel is accepted and operator-deployable with constraints, but product surfaces, runbooks, retention/security, reporting/export, and production admin/login UX remain routed work. |
| `active stewardship route` | `docs/agent-working-surface/decision-anchor-layer/` | `011` DEC anchors and `013` gap routing are now the compact route for future architecture-sensitive work. |
| `implementation boundary map` | `docs/implementation/module-interfaces.md` | Thin code-boundary index for implementers touching modules; not a DEC surface, gap register, rationale companion, or roadmap. |

### Candidate Or Ready Work

| Route | Status | What is actually waiting |
|---|---|---|
| NW-057 context expression property boundary | `ready` | Decide whether unknown `context.*` references are deploy-time invalid or intentionally null-on-unknown. If invalid, add whitelist validation/tests; if tolerated, document the behavior. |
| Baseline acceptance candidates | none active | No broad baseline catchup is waiting. Do not manufacture one unless BAR/NW evidence changes. |
| Final stewardship freeze/audit | optional stewardship task | Only needed if future work shows drift between the active decision-anchor layer, CDL, contracts, BAR/NW, and code. Keep it short and do not create another active companion. |

### Deferred Or Future-Decision Work

| Route | Current standing | Do not implement until |
|---|---|---|
| NW-054 / BAR-106 retention and device data boundary | `future_decision` | A security/platform decision selects expiry, decommissioning, sealed-partition recovery, local encryption, token/session retention, redaction/no-local-retention behavior, and server-history constraints. |
| NW-044 reporting aggregation and import/export | `future_decision` | A product/platform decision splits scoped report views, reporting warehouse/export, structured import, event ingestion, and interoperability. |
| NW-045 domain conflict automation and batch resolution | `future_decision` | A decision preserves exact designated-resolver equality, emits per-flag resolution events, and avoids direct flag mutation, resolver reassignment, or runtime auto-resolution without authority. |
| NW-046 flag cascade and pattern traversal reporting | `future_decision` | A bounded platform decision proves current projections are insufficient and preserves platform-fixed pattern inventory and rebuildable projections. |
| NW-053 / BAR-108 subject/query/custom scope | `future_decision` | A successor decision authorizes any new platform-owned scope mechanism beyond current geography, subject-list, activity, and temporal assignment axes. |
| Broad audit/history reads, emergency writes, dynamic auditor scope | deferred by NW-051 | Concrete product/security decision promotes them. Simple current-scope auditor visibility remains ordinary assignment/config posture only. |
| Online principal-binding admin APIs/UI, production web admin auth, mobile OIDC login UX | future product/platform decisions | A bounded auth/admin/session decision separates provider identity from platform authority and audit semantics. |
| Entity lifecycle, general triggers, auto-resolution, resolver reassignment, IdP group/claim authority, mobile authoritative rejection | future decisions | Explicit successor authority exists in CDL/IDR/BAR/NW routing. |

### Current Active Backlog Priorities

| Order | Route | Why this order is healthy |
|---|---|---|
| 1 | NW-057, if a small verification slice is desired | It is ready, narrow, and prevents `context.*` closure ambiguity before config UX or reporting work depends on it. |
| 2 | NW-054, if production/security pressure is next | Shared-device partitions are accepted, but exit/decommissioning/recovery/retention promises still need a decision. |
| 3 | Production admin auth or mobile OIDC/login decision, if productization is next | NW-056 says these are not implemented and should not infer authority from IdP claims or current dev admin paths. |
| 4 | NW-044, if reporting/export/import pressure is next | S26 proves report inputs only; durable report APIs, exports, warehouses, and imports need a boundary. |
| 5 | NW-045 or NW-046, if operations need conflict/review scale | Batch, automation, traversal, and cascade views touch resolver/workflow boundaries. |

## 3. Recent Movement

| Commit | Meaning | Evidence |
|---|---|---|
| `4a1fe6f` | Promoted the decision-anchor routing surface and committed `.review/001-architecture-decisions-listing-passes/` as provenance. | Active files are `decision-anchor-layer/README.md`, `architecture-decision-anchors.md`, `gap-routing-playbook.md`, and `provenance-index.md`; the old rationale companion is retired as active surface. |
| `2ae77f5` | Added NW-057 context expression boundary verification and routed it into the backlog. | `docs/reviews/NW-057-context-expression-boundary-verification.md` plus one NW row. |
| `79484f2` | Updated the now-retired rationale companion with pattern/projection boundary details before the active-surface reduction. | Useful as provenance only; future routing should patch `011` then `013` in the decision-anchor layer. |
| `496d0fc` | Added NW-044 reporting aggregation/import-export review material. | Reporting remains a future-decision route, not an accepted reporting product/API. |
| `d73786c` | Improved CDL query/index tooling. | Use CDL README/query slices instead of loading the full ledger by default. |
| `151b436` and `0021f2a` | Added and refined NW-056 product standing and production readiness map. | Product readiness is separated from accepted kernel capability and routed successor decisions. |

Removed or superseded active surfaces:

| Surface | Current role |
|---|---|
| `docs/agent-working-surface/architecture-rationale-and-routing-companion.md` | Retired as active surface; now a pointer to the decision-anchor layer. |
| `.review/001-architecture-decisions-listing-passes/` | Provenance/workbench only. Do not update active routers from raw `.review` artifacts. |
| `docs/implementation/module-interfaces.md` | Kept only as an implementer-facing module ownership/storage/test index; DEC and `013` own decision/routing questions. |
| Context capsules and handoff capsules | Intermediate material; keep out of the working surface unless indexed as provenance. |

## 4. Architecture Guardrails

### Source Order

| Order | Source | Role |
|---|---|---|
| 1 | CDL, sliced through its README/query tool | Architecture authority. |
| 2 | `contracts/` | Wire, schema, sync, flag, shape, pattern, and shared-fixture contracts. |
| 3 | `docs/agent-working-surface/decision-anchor-layer/README.md` | Active DEC anchors and gap routing. |
| 4 | BAR and platform NW backlog | Accepted standing, evidence, ready work, and future-decision routes. |
| 5 | Operational UX companion | Non-authoritative product vocabulary and UI layering guardrail. |
| 6 | Escape-hatch register | Inactive measured evolution paths, not implementation permission. |
| 7 | Scenarios, access-control scenario, phase files, and IDRs | Product pressure, provenance, and explicitly routed decision detail. |

### Non-Negotiables

| Guardrail | Practical meaning |
|---|---|
| No new envelope fields or event `type` values without successor architecture authority. | Product words, UI labels, or scenario terms do not become event vocabulary. |
| Events remain canonical; projections and report inputs are rebuildable. | Do not create durable workflow/reporting truth as a convenience path. |
| Structurally valid policy/state anomalies are accepted and flagged. | Mobile and UI can warn or stage, but must not become authoritative rejection surfaces. |
| Authority remains assignment/principal-binding derived. | IdP groups, roles, claims, JWT `actor_id`, request-body actor IDs, and UI-selected actors are not platform authority. |
| Scope mechanisms are platform-fixed. | Subject/query/custom scope, auditor scope, and campaign/custody scope require successor decisions. |
| Assignment administration remains command-capability plus same-assignment containment. | `assignment_changed` is not an activity role-action, and NW-050 does not grant domain work, audit/history, resolver, or override authority. |
| Shared-device partitions do not settle retention/security. | Expiry, decommissioning, sealed recovery, encryption, and token/session retention remain NW-054/BAR-106. |

### Escape Hatch And Auth Boundary

All escape hatches remain `inactive_until_triggered`; no measured trigger was
claimed in this checkpoint. If a future task claims one, stop ordinary
implementation and produce measurement evidence plus affected CDL rows.

Production auth is accepted for validated provider JWTs only after explicit
active principal binding lookup. That is not mobile OIDC login UX, not online
principal-binding admin authority, not web admin authentication, and not IdP
group/claim authority.

## 5. Risk Pulse

### New Or Elevated Risks

| Risk | Severity | Trigger | Mitigation | Needs backlog row |
|---|---:|---|---|---|
| Surface drift after promotion | B | Agents keep reading `.review` or the retired companion as active authority. | AGENTS, status, working-surface README, and decision-anchor README now route future work through the compact layer. | No. |
| Large-surface maintenance burden | B | Future work patches multiple companions or provenance docs. | Maintenance rule is `architecture-decision-anchors.md -> gap-routing-playbook.md`; old material stays reference-only. | No. |
| Product-readiness overclaim | B | Accepted kernel evidence is mistaken for turnkey product readiness. | NW-056 separates kernel, product surfaces, operator constraints, and not-started areas. | No, unless a concrete product slice is selected. |
| Context expression ambiguity | C | Config UX/reporting starts relying on closed `context.*` property vocabulary. | Run NW-057 before that dependency matters. | Already present. |
| Retention/security promise gap | A | Production/shared-device planning needs expiry, recovery, encryption, or decommissioning behavior. | Route NW-054 before implementing UI, policy, or data deletion behavior. | Already present. |

### Resolved Or De-Risked Items

| Item | Severity before | What changed | Residual risk |
|---|---:|---|---|
| Decision-anchor workbench sprawl | B | Active surface is reduced to DEC anchors, gap routing, and provenance index. | Future changes must avoid recreating companion sprawl. |
| IDR-first future routing | B | Durable IDR outcomes are folded into DEC anchors; future work routes to bounded artifacts through `013`. | Existing IDRs remain provenance and validation inputs when explicitly routed. |
| Product standing ambiguity | B | NW-056 maps what is kernel-accepted, product-partial, operator-constrained, blocked, or not started. | Successor product routes still need selection. |
| Old rationale companion authority confusion | B | The companion is retired as active surface and points to the new decision-anchor layer. | Search results may still find it; follow AGENTS/source order. |

## 6. Scenario And Product Pressure

| Pressure | Current classification | What is safe now | What remains routed |
|---|---|---|---|
| S00 structured capture | Accepted runtime evidence | Product UX can build over existing envelope/config/form constructs. | No event/type or mutable status additions. |
| S19 offline/stale authority | Accepted runtime evidence | UX can explain offline confidence, stale warnings, and sync recovery. | Mobile rejection authority and retention/decommission policy remain routed. |
| S21 supervisor review | Accepted runtime evidence | Single-flag/review UX can use exact resolver semantics. | Batch, reassignment, and auto-resolution route through NW-045/BAR-102/BAR-103. |
| S22 coordinated campaign | Accepted runtime evidence with no new primitives | Product IA can use campaign/handoff/progress vocabulary through NW-047. | Campaign scope, discovered-unit lifecycle, triggers, and custom scope remain future decisions. |
| S23 setup/config | Accepted runtime evidence | Config UX can improve authoring over existing shapes, activities, roles, expressions, severity, and packages. | Scripts, deployer state machines, new config-package contract sections, and production admin auth remain routed. |
| S26 reporting | Scenario-runtime evidenced only | Current scoped report inputs, freshness, flags, and drill-back semantics are proven in tests. | Reporting API, dashboard, warehouse, export/import, and broad audit/history access route through NW-044/NW-051. |
| S27 logistics transfer | Accepted runtime evidence | Product labels can describe dispatch, receipt, discrepancy, and transfer status. | Custody-specific platform scope or auto-resolution remains future decision. |
| S24/S25 lifecycle, retention, worker exit | Active pressure with partial support | Assignments, subject-history, stale flags, actor partitions, and selective retention cover pieces. | Exit/decommissioning, local expiry, recovery, encryption, and no-local-retention/redaction route through NW-054. |

## 7. Verification Ledger

| Verification | Result |
|---|---|
| Checkpoint prompt read | `.review/tasks/checkpoint.prompt.md` read and adapted for the decision-anchor surface. |
| Bootstrap routing read | `AGENTS.md`, `docs/status.md` Current Routing, active working-surface README, module-interface role, decision-anchor README, DEC anchors, and gap-routing playbook inspected. |
| Registers read | BAR, NW backlog, escape-hatch register, scenarios README, latest checkpoint, NW-056 artifact, and NW-057 verification note inspected. |
| Git anchor read | `git status --short` was clean before checkpoint work; `git log --oneline -10` and `git show --stat --summary HEAD` anchor this snapshot on `4a1fe6f`. |
| Runtime tests | Not run. This checkpoint is documentation-only and reuses existing BAR/NW evidence. |
| Diff hygiene | `git diff --check` passed after this file was written. |

## 8. March Orders

1. **Resume from the active router, not review chronology.**
   - Why now: the decision-anchor layer exists specifically to prevent another broad surface from becoming the working truth.
   - Expected artifact: a bounded task packet or selected NW route that names DEC anchors, guardrails, forbidden work, tests, and stop conditions.
   - Scope: `AGENTS.md`, `docs/status.md` Current Routing, relevant module interface only if implementation code/module behavior is touched, contracts/code touched, and `docs/agent-working-surface/decision-anchor-layer/README.md`.
   - Stop condition: the next task requires reading or patching raw `.review` files as if they were active authority.

2. **Run NW-057 if you want a small clean verification slice first.**
   - Why now: it is the only ready baseline-adjacent item and can prevent config/reporting UX from depending on an ambiguous `context.*` boundary.
   - Expected artifact: code/tests plus BAR/NW/status update, or a documentation update if null-on-unknown is intentionally preserved.
   - Scope: deploy-time expression validation, mobile/server expression docs/tests, and the fixed expression ceiling.
   - Stop condition: the work adds functions, scripts, dynamic queries, aggregation, joins, recursion, or deployer-authored context namespace expansion.

3. **Run NW-054 before making production data-retention promises.**
   - Why now: shared-device actor partitions are accepted, but exit, expiry, decommissioning, recovery, local encryption, redaction, and token/session retention are unresolved.
   - Expected artifact: security/platform decision or retention-boundary artifact.
   - Scope: local device data, actor partitions, selective retention, sealed partitions, sensitivity, tokens, and operational lifecycle.
   - Stop condition: the path deletes canonical server event history, rewrites normal sync watermarks, or allows cross-actor recovery without explicit security authority.

4. **If productization is next, choose one product route from NW-056.**
   - Why now: broad "make it product-ready" work would mix admin auth, mobile login, config UX, ops runbooks, and reporting decisions.
   - Expected artifact: one bounded prompt for production admin auth, mobile OIDC/login, web config UX, mobile navigation polish, or ops runbook.
   - Scope: one product surface at a time, using NW-047 vocabulary and preserving BAR/NW/contract authority.
   - Stop condition: the route treats IdP claims, dev admin paths, UI-selected actors, product labels, or provider groups as platform authority.

5. **Use NW-044, NW-045, or NW-046 only when that pressure is concrete.**
   - Why now: reporting/export/import, batch conflict handling, and pattern traversal are real needs, but each touches deeper platform boundaries.
   - Expected artifact: future-decision exploration or bounded platform/product decision.
   - Scope: one pressure front at a time.
   - Stop condition: the proposal creates canonical aggregate truth, bypasses resolver equality, adds runtime auto-resolution, adds new scope mechanisms, or introduces deployer-authored traversal/state machines.
