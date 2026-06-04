# Project Checkpoint - 2026-06-04 (Gap Baseline Assessment)

---

## 1. Bearing

- **Anchor commit**: `1b559da feat(auth): add principal binding provisioning`
- **Phase**: Post-Phase-4 stabilization.
- **Momentum**: `ADVANCING` - BAR-001 through BAR-015 and BAR-104 are accepted, and the current work is now baseline stewardship rather than feature implementation.
- **Last milestone**: NW-040 accepted deployment-managed production principal-binding provisioning and resolved FP-011/BAR-104 for the current production-auth gate.
- **Horizon**: make the remaining informal gaps visible enough to choose either a constrained S22 probe or a successor exploration slice without drifting into broad implementation.

This checkpoint assesses a pre-IDR informal gap list against current CDL/BAR/IDR standing. It confirms the user's correction that IDR-023 is in the right place: activity role-actions are only `capture`, `review`, `alert`, `task_created`, and `task_completed`; `assignment_changed` is assignment administration and was intentionally rejected from the activity-role vocabulary. The production/domain pressure is real, but it routes through assignment command authority, S25 worker lifecycle, production auth binding, reporting/aggregation, and future scope/access decisions.

## 2. Standing Snapshot

### Current Accepted Baseline

| Standing | Rows | Meaning |
|---|---|---|
| `baseline_accepted` | BAR-001 through BAR-015 | Envelope/type closure, append-only event store, sync/backfill, payload contracts, flag catalog/resolver routing, assignment/scope, retention, identity, config, expressions, patterns, domain/transition detection, projection equivalence, and historical location-path immutability are accepted with evidence. |
| `baseline_accepted` | BAR-104 | Production OIDC/JWT/Keycloak authority is accepted only through explicit `(issuer, subject) -> actor_id` binding, provider JWT validation, and deployment-managed binding provisioning. IdP groups/roles/claims remain non-authority. |
| `accepted scenario probes` | NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, NW-033/S26 | Runtime evidence exists for selected offline, capture, review, logistics, setup/config, and reporting pressures. |
| `accepted assessment` | NW-041 | This checkpoint classifies the informal gap list and adds S22 pressure-map routing without accepting new runtime capabilities. |
| `ready` | NW-042 | Constrained S22 coordinated distribution campaign probe is ready as runtime validation if the next slice should exercise accepted baseline rows. |
| `future_decision` | NW-043 through NW-046 | Access exceptions/assignment-admin authority, reporting aggregation/import-export, domain conflict automation/batch resolution, and generic flag cascade/pattern traversal reporting require exploration before implementation. |

### Deferred And Future-Decision Surface

| Surface | Status | Active route |
|---|---|---|
| General trigger execution | `deferred` | BAR-101; CDL-042. |
| Auto-resolution execution | `deferred` | BAR-102; CDL-053. |
| Resolver reassignment | `future_decision` | BAR-103; IDR-026 deferral. |
| Entity lifecycle | `deferred` | BAR-105; NW-021. |
| Field-level sensitivity/encryption/redaction | `future_decision` | BAR-106. |
| New envelope fields or event types | `future_decision` | BAR-107; FP-004 for assignment refs. |
| New scope mechanisms | `future_decision` | BAR-108; CDL-055. |
| Online binding-admin API/mobile OIDC UX/IdP group authority | `future_decision` | IDR-028/NW-040 explicitly defer these. |

## 3. Gap Assessment Table

| Item | Current status | Baseline classification | Affected baseline section or BAR/IDR | Risk if ignored | Recommended next action |
|---|---|---|---|---|---|
| Aggregation interface | `open` for product/API; current projection-based reporting semantics are accepted | Future decision or P3/P4 exploration | CDL-002, BAR-012, BAR-014, NW-033/S26; reporting warehouse extraction seam | Agents may invent a reporting API, warehouse, or aggregate truth table while only scenario-local aggregation has been proven. | Explore/reporting interface paths before implementation; keep current work read-side and rebuildable. |
| General flag semantics: catalog, detection timing, server-side vs mobile flag creation, cascade beyond CDL workflow slice | `resolved` for catalog/timing/server authority; `open` for generic downstream contamination indicators | Already routed plus exploration for cascade UI/reporting | BAR-006, BAR-013, CDL-003, CDL-004, CDL-051, IDR-022, IDR-026, `contracts/flag-catalog.md` | A worker may tape-fix by adding mobile-authored canonical flags or propagating dozens of downstream flags. | Treat server-emitted flags and source-only root flags as accepted; explore only the generic downstream indicator/reporting surface. |
| Domain conflict automation, pending match, flag location, batch resolution | `already routed` for detection/manual resolution; `open` for automation/batch/pending-match UX | Future decision | CDL-045, CDL-053, CDL-054, BAR-102, BAR-103, IDR-022, IDR-026 | A future agent may put automation into uniqueness rules or let batch UI bypass exact resolver equality. | Explore pending-match and batch-resolution paths; no implementation until it is clear whether this is auto-resolution, resolver reassignment, or admin UX. |
| Domain conflict rules | `resolved` for shape-declared uniqueness; `rejected` for arbitrary detector code | Already covered baseline concern; future decision for broader rule engine | BAR-013, IDR-022, CDL-045, BAR-107 if contract changes are proposed | Domain-specific conflict logic could become hidden deployer code or hard-coded platform semantics. | Keep current rule surface to shape-declared uniqueness; route broader domain conflict rules as platform evolution. |
| Reporting freshness, interoperability compatibility, and structured import/export | `resolved` for freshness/drill-back in current reporting probe; `open` for interop/import/export | Mixed: accepted runtime evidence plus future decision | NW-033/S26, BAR-012, BAR-014, CDL-002, CDL-006, CDL-037, BAR-106/BAR-107 if exports add contracts | Teams may mistake S26 evidence for an accepted reporting/export product boundary. | Make interop/import/export a separate exploration; do not combine with ordinary reporting probes. |
| Alias-cycle behavior | `resolved` | Already covered baseline concern | CDL-026, BAR-009, IDR-009, `IdentityResolverIntegrationTest` | If forgotten, identity projection fixes may allow cycles in immutable lineage. | No new work now; preserve existing lifecycle/acyclicity tests in identity slices. |
| Authoring format and projection merge across versions | `resolved` for shape/config version coexistence and pattern definition delivery; `unclear` for future multi-version projection merge semantics beyond current fixtures | Already covered baseline plus possible future exploration | BAR-010, BAR-012, BAR-014, IDR-019, IDR-025, CDL-039, CDL-049, CDL-050 | Agents may mutate pattern refs or reinterpret old events under new definitions. | Defer. If a new pattern version or projection merge rule is proposed, require contract fixtures and successor routing. |
| Pattern inventory/schema, traversal depth, reporting | `resolved` for current fixed inventory/schema/projection; `open` for inventory expansion and generic traversal/reporting APIs | Already routed plus future decision | BAR-012, BAR-014, IDR-025, CDL-047, CDL-049, CDL-050, CDL-051, EH-006 | Custom state machines or unbounded traversal may sneak in as reporting convenience. | Keep current four platform-owned patterns. Explore traversal/reporting depth only if a concrete scenario proves current projections insufficient. |
| Authorization staleness flag types/config, grace period, permission tables, subject scope, auditor role, shared device, sync mechanics | `resolved` for current staleness flags/severity/sync mechanics; `open` for grace periods, auditor/special access, shared-device model, and broader permission-table/admin model | Mixed: accepted baseline plus future decision | BAR-003, BAR-004, BAR-006, BAR-007, BAR-008, BAR-104, BAR-108, IDR-021, IDR-023, IDR-024, IDR-026, CDL-030, CDL-031, CDL-035, CDL-055 | Assignment/admin pressure may be incorrectly routed into activity actions or IdP claims. | Explore access-exception and assignment-admin authority paths separately from role-action vocabulary. |
| S22 coordinated distribution campaign and ITN walkthrough | `already routed` as high-value constrained scenario; `open` for deep pressure map/runtime probe | P2 runtime validation candidate with deferred edges | Existing scenario map row for S22, BAR-003/004/006/007/012/013/014, BAR-101, BAR-105, BAR-108 | Domain words like campaign, village completion, stock, and household registry may be mistaken for new core terms. | Add a constrained S22 pressure-map/probe slice using current constructs, while explicitly deferring discovered-unit lifecycle, automatic follow-up, and custom scope. |

## 4. Immediate Baseline Visibility

These items need to stay visible to stewards and prompt authors now:

| Item | Why visible now | Route |
|---|---|---|
| S22 constrained campaign/distribution probe | It is high product pressure and combines many accepted baseline rows without requiring a new activity action. | Create a bounded scenario-review/runtime-probe prompt, not a feature prompt. |
| Access exceptions and assignment-admin authority | IDR-023 intentionally leaves assignment administration outside activity role-actions. Auditor role, shared devices, grace periods, and special access are not accepted current behavior. | Exploration first; likely successor decision touching BAR-108 and assignment command authority. |
| Reporting/aggregation/import-export boundary | S26 proves current reporting semantics, not a general reporting API, interop contract, or import/export system. | Exploration first; may split into read-side reporting, structured export, and structured import decisions. |
| Domain conflict automation and batch resolution | Domain uniqueness detection is accepted, but automation/batch handling crosses auto-resolution/resolver reassignment/admin UX boundaries. | Exploration first; do not implement inside uniqueness detector. |
| Generic flag cascade indicators | CDL-051 accepts source-only flagging with computed downstream indicators, but the generic UI/API/reporting surface is not broadly accepted. | Keep source-only flags; explore indicators only with a concrete reporting/workflow need. |

## 5. Items Needing Exploration Before Routing

| Open front | Minimal paths to explore | CDL/BAR pressure |
|---|---|---|
| Aggregation/reporting/import-export interface | Path A: keep scenario-local read-side aggregation over projections. Path B: add a scoped report API backed by rebuildable projections. Path C: add a separate reporting warehouse/export pipeline. Path D: add structured import as event ingestion with provenance. | CDL-002, CDL-006, CDL-021, CDL-037, BAR-106, BAR-107. |
| Domain conflict automation/pending match/batch resolution | Path A: manual `domain_uniqueness_violation` review only. Path B: derived pending-match queue with no new truth. Path C: batch command that emits one normal resolution event per exact-resolver flag. Path D: L3b auto-resolution policy for narrow deterministic cases. | CDL-045, CDL-053, CDL-054, IDR-026, BAR-102, BAR-103. |
| Access exceptions, grace periods, auditor role, shared devices | Path A: model temporary authority as ordinary assignment events with no grace. Path B: add grace-period policy that changes flag severity/timing, not authority. Path C: add explicit auditor/special-access scope mechanism. Path D: define shared-device session/actor-switching rules. | CDL-030, CDL-031, CDL-035, CDL-055, BAR-104, BAR-108. |
| Pattern traversal/reporting depth | Path A: keep current per-pattern projection fields and S26-style aggregation. Path B: add bounded source-chain/downstream indicator projection. Path C: add new platform-fixed patterns under EH-006. Path D: reject deployer-authored traversal/state machines. | CDL-047, CDL-049, CDL-050, CDL-051, EH-006. |
| S22 full campaign execution | Path A: constrained runtime probe with known subjects plus discovered-unit captures. Path B: promote S06 entity lifecycle for discovered-unit registry maintenance. Path C: add read-side progress aggregation only. Path D: add automatic follow-up triggers. | BAR-101, BAR-105, BAR-108, NW-023 S22 row. |

## 6. Safely Deferred Or Already Covered

| Item | Disposition |
|---|---|
| Activity action vocabulary correction | Already decided by IDR-021/IDR-023. Keep only `capture`, `review`, `alert`, `task_created`, and `task_completed`; keep `assignment_changed` out. |
| Alias-cycle behavior | Accepted and guarded by CDL-026, BAR-009, IDR-009, and identity lifecycle tests. |
| Current flag catalog/detection timing/server-vs-mobile flag authority | Accepted. Mobile remains advisory; server emits canonical flags. |
| Current domain uniqueness rules | Accepted for shape-declared uniqueness only. Broader conflict policy remains future work. |
| Current reporting freshness/scope/drill-back semantics | Accepted by NW-033/S26, bounded to current projection/sync/flag metadata. |
| Current pattern schema/inventory/delivery | Accepted for bundled platform-owned patterns and config package delivery. Inventory expansion remains EH-006/future platform work. |
| Production principal binding | Accepted by BAR-104/NW-040; mobile OIDC login UX, online admin APIs, and IdP group/claim authority remain future decisions. |

## 7. S22 Pressure Map Addendum

This addendum applies `docs/reviews/scenario-baseline-pressure-map-protocol.md` to `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md` and `docs/walk-throughs/itn-distribution-campaign.md`.

| Field | Assessment |
|---|---|
| Scenario | S22 - Coordinated Distribution Campaign Across Grouped Locations. |
| Scenario class | `baseline_composite`. |
| Real-world goal | Teams visit grouped units during a time-bound campaign, record unit-level work, track related supply flow, and continue after reassignment/offline overlap. |
| Primary pressure | Scoped iteration over child subjects, assignment/reassignment, subject-history visibility, transfer-with-acknowledgment, duplicate/offline reconciliation, and read-side progress aggregation. |
| BAR rows touched | BAR-003, BAR-004, BAR-006, BAR-007, BAR-012, BAR-013, BAR-014. |
| Contracts touched | Sync protocol, assignment payload shapes, flag catalog, pattern definitions/fixtures, deployer shape/config package for campaign activities. |
| Deferred/future surfaces | BAR-101 for automatic follow-up; BAR-105 for full discovered-unit registry lifecycle; BAR-108 for custom campaign/custody scope; reporting warehouse seam if campaign dashboards require separate storage. |
| Current probe viability | `ready_with_constraints`. |
| Probe value | `high`. |

### Domain Terms To Translate

| ITN/scenario term | Current platform construct | Guardrail |
|---|---|---|
| Campaign | Deployment/configured activity set plus assignments and reporting view | Not a new event type or scope mechanism. |
| Village/location progress | Derived projection/aggregation over child-unit work plus optional human-authored status capture | Do not store workflow status as canonical mutable state. |
| Household/unit | Subject or subject-linked payload depending on deployment design | Full create/update/deactivate registry lifecycle remains BAR-105. |
| ITN stock handoff | `transfer_with_acknowledgment/v1` pattern over deployer shapes | No custom custody scope unless BAR-108 successor decision. |
| Completion/follow-up | Human-authored event plus read-side aggregation | Automatic missed-work/follow-up is BAR-101 trigger work. |
| Duplicate household/visit | Existing identity/domain uniqueness flags | No server rejection of structurally valid offline work. |

### Bounded Runtime Probe Shape

| Area | Probe expectation |
|---|---|
| Actors | Coordinator creates assignments; two field teams work overlapping grouped locations; supply actor records handoffs; supervisor reviews flags/progress. |
| Subjects | Parent location subject and unit/household subjects, with at least one newly discovered unit represented as capture/linkage only, not full lifecycle. |
| Expected events | `assignment_changed` for assignments; `capture` events for unit visits and optional human status; transfer-pattern `capture` events for dispatch/receipt/return; `alert` conflict flags; `review` resolution events. |
| Expected flags | `domain_uniqueness_violation` for duplicate unit/visit where configured; `role_stale` or `temporal_authority_expired` for stale authority; `transition_violation` for out-of-order transfer/status; no new category. |
| Sync/projection | Normal pull remains current-scope/watermark-based; subject-history supports handoff context; projections exclude unresolved flagged source events and re-include exact accepted resolutions. |
| Forbidden work | No new envelope fields/types, no activity action expansion, no entity lifecycle implementation, no trigger execution, no auto-resolution, no resolver reassignment, no new scope mechanism, no reporting warehouse. |

## 8. Recent Movement

| Commit | Meaning | Evidence |
|---|---|---|
| `1b559da` | NW-040 implemented and accepted deployment-managed principal-binding provisioning. | Full server suite passed at 312 tests; FP-011 resolved and BAR-104 accepted. |
| `305182f` | NW-038 added OIDC/JWKS provider validation behind explicit principal binding. | Full server suite passed at 307 tests in working-agent report. |
| `96c637a` | NW-037 added the principal actor binding foundation and mobile actor alignment. | Steward later fixed test isolation and verified full server/mobile suites. |
| `9815d81` | NW-033/S26 reporting and NW-034 schema hygiene were accepted before the auth series. | S26 runtime probe and config/shape schema tests accepted current bounded reporting/config evidence. |
| Current checkpoint pass | NW-041 through NW-046 routing added to the active backlog; status current routing now points at the gap assessment. | `git diff --check` clean after docs edits. |

## 9. Architecture Guardrails

| Guardrail | Checkpoint implication |
|---|---|
| CDL is architecture authority | This checkpoint routes pressure; it does not create a new CDL decision. |
| Activity role-actions are five work actions only | Keep assignment administration out of `activities[*].roles`; do not reintroduce `assignment_changed` as an activity action. |
| Events remain immutable truth and projections are rebuildable | Aggregation/reporting paths must not become canonical mutable state. |
| Structurally valid state/policy anomalies are accepted and flagged | Domain conflict, stale authority, and transition issues must not become sync rejection. |
| Server emits canonical flags; mobile is advisory | Mobile warning or local uniqueness checks cannot be authoritative conflict state. |
| Source-only flagging is the cascade model | Do not propagate root flags as new flags; compute downstream indicators in projections if needed. |
| Scope mechanism is platform-fixed | Auditor/shared device/campaign/custody scope changes need successor routing. |
| Production auth maps principal to actor only | IdP groups/claims remain non-authority unless a future decision changes that. |

## 10. Verification Ledger

This checkpoint is a documentation/stewardship analysis pass. No Maven or Flutter tests were run because no runtime code changed. `git diff --check` was run after writing the checkpoint.

## 11. March Orders

1. **Route a constrained S22 scenario probe.**
   - Why now: S22 is the most useful remaining composite that is not yet runtime-probed, and it can validate campaign/distribution pressure without changing activity actions.
   - Expected artifact: one bounded working-agent prompt from NW-042.
   - Scope: scenario test/probe only.
   - Stop condition: implementation requires entity lifecycle, triggers, auto-resolution, resolver reassignment, new scope, reporting warehouse, or envelope changes.

2. **Explore access exceptions and assignment-admin authority before implementation.**
   - Why now: auditor role, grace period, shared device, subject scope variants, and permission-table pressure are real but not activity-role actions.
   - Expected artifact: short exploration with path comparison and successor-decision recommendation.
   - Scope: assignment command authority, access exceptions, and sync implications.
   - Stop condition: proposed path treats IdP claims/groups, activity role maps, or request-body actor IDs as authority.

3. **Explore reporting/aggregation/import-export as a product boundary.**
   - Why now: S26 accepted freshness/drill-back semantics, but not a general reporting API, interop contract, or structured import/export.
   - Expected artifact: exploration splitting read-side reporting, export, and import/event-ingestion decisions.
   - Scope: contracts and rebuildable read models only.
   - Stop condition: proposal makes aggregate tables canonical or changes envelope/schema contracts without CDL authority.

4. **Explore domain conflict automation and batch resolution separately from uniqueness detection.**
   - Why now: current uniqueness flags are accepted, but batch resolution and pending-match UX could cross resolver and auto-resolution boundaries.
   - Expected artifact: path comparison across manual queue, derived pending-match queue, batch command emitting per-flag resolutions, and L3b auto-resolution.
   - Scope: conflict resolution/admin UX and policy routing.
   - Stop condition: batch operation bypasses exact designated resolver equality or mutates flags directly.

5. **Leave resolved items alone.**
   - Why now: alias cycles, current action vocabulary, server-side flag emission, current pattern delivery, and current production principal binding are accepted.
   - Expected artifact: no code; future prompts cite the accepted rows and forbid reopening them casually.
   - Scope: stewardship hygiene.
   - Stop condition: a new product requirement proves the accepted boundary is insufficient and names the affected CDL rows.
