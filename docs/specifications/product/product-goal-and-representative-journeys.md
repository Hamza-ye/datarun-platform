# Product Goal, Complete Scenario Portfolio, And Ordered Slice Roadmap

Status: active planning surface
Document type: product_planning
Owner: product steward
Source: NW-168
Authority: derived planning surface only; creates no accepted product behavior, platform authority, implementation scope, real-production approval, or architecture decision by itself. CDL and contracts remain authority; scenarios, user-fit packets, reviews, workshops, tests, runtime probes, and prior PRs are evidence.
Last reviewed: 2026-06-26
Supersedes: none
Related: docs/agent-working-surface/product-journey-and-slice-sequencing.md; docs/scenarios/README.md; docs/viability-assessment.md; docs/behavioral_patterns.md

## 1. Product Goal

Active Product Goal:

Make Datarun a coherent configurable field-operations system for local/on-prem
production use: coordinators can set up operational work, assign responsibility,
and evolve what is collected; field users can log in, receive scoped work,
capture and correct records offline, and sync safely; supervisors and
owner-operators can review standing, freshness, attention, handoff, and
traceability; and the same product concepts hold across the complete Phase 1
scenario space without becoming health, logistics, stock, campaign, household,
facility, or legacy-form-specific product vocabulary.

Target users and responsibilities:

- Coordinator/setup owner: define activities, known things, shapes,
  responsibilities, review expectations, simple warnings, and rollout changes.
- Field user: authenticate, receive assigned work, identify known things where
  relevant, capture work offline, correct mistakes where allowed, and understand
  local save/sync/review standing.
- Supervisor/reviewer: inspect scoped work, review or correct questionable
  records, handle stale/offline work, and interpret attention and freshness.
- Registry steward: keep the known set of things usable over time, including
  missing-known-thing, duplicate, inactive, moved, merged, split, and
  verification pressure when a selected slice needs those behaviors.
- Owner/operator/admin/support contact: run and support the local/on-prem
  deployment path while keeping production cutover, imports, retention/security,
  and real-use approval as explicit routed decisions.

User/deployment outcome:

The product backlog should cover the whole Phase 1 user problem first, then
deliver it in small production-usable increments. The roadmap must classify
what is already evidenced, what is partially ready, what is not run, and what
needs an authority or gap route before implementation. It must not treat
already-proven control lanes as the whole product.

Why this goal matters now:

NW-165 proved live mobile OIDC login against the local issuer. NW-167 accepted
the rule that future work starts from one Product Goal and journey/slice
sequencing. The first NW-168 draft selected S00/S19, S21/S26, and S27/S22 as a
minimal representative portfolio. Those lanes are valuable, but the pressure map
already says they have accepted runtime probe evidence. The stale pattern would
be to consolidate only those known-safe lanes and postpone S06/S06b again.

This amended NW-168 instead selects the complete Phase 1 work portfolio, using
`docs/scenarios/README.md` as the controlling map:

- Phase 1 core: S00 through S14 and S22.
- Cross-cutting: S19 offline-first work.
- Composite/product-thickening scenarios: S20 and S21.
- Post-Phase-4 thickening scenarios: S23 through S27.

S15, S16, and S18 remain Phase 2/deferred pressure from the README and
viability review. They are not part of this Phase 1 portfolio, but the roadmap
must not make later Phase 2 work impossible.

Measurable success signals:

- No Phase 1 scenario is omitted: S00-S14, S19, S20, S21, S22, S23, S24, S25,
  S26, and S27 are classified.
- S00/S19, S21/S26, and S27/S22 are treated as pressure-mapped/evidenced
  control lanes, not sufficient portfolio boundaries.
- S06/S06b is a primary milestone lane, tied to BAR-105/NW-021 where lifecycle
  implementation is needed, not a generic deferral.
- Every scenario or scenario group records user outcome, product concept
  pressure, evidence status, missing capability, milestone assignment, first
  production-usable slice candidate, and authority/gap route.
- The roadmap moves from complete portfolio and Definition of Done to small
  production-usable increments.
- No runtime code, contracts, schemas, CDL, BAR, gap register, validation
  policy, server/mobile implementation behavior, real-production approval,
  legacy data import, account import, submitted-record replay, or production
  cutover changes in NW-168.

Evidence status meanings:

- `PROVEN`: accepted baseline or runtime scenario evidence proves current
  constructs can run this pressure, though product fit may still be partial.
- `PARTIAL`: backing mechanisms or adjacent evidence exist, but the product
  journey, UX, policy, or production-usable slice is not fully proved.
- `NOT_RUN`: no accepted runtime or product proof for the scenario pressure.
- `FAILED`: evidence exists and failed. No selected Phase 1 row is currently
  classified `FAILED`.

## 2. Complete Scenario Portfolio

The portfolio below uses scenario files and user-fit packets as evidence. It
does not promote scenario labels, product labels, fixtures, tests, or domain
examples into platform authority.

| Scenario | User outcome | Product concept pressure | Current evidence status | Missing capability | Milestone | First production-usable slice candidate | Authority or gap route |
|---|---|---|---|---|---|---|---|
| S00 - Basic structured capture | A worker records known details once, preserves traceability, and can correct without erasing history. | Activity entry, information shape, local save, correction, trace/history. | PROVEN: NW-026/S00; NW-059 sync-status; NW-062 correction UX. Product wording remains PARTIAL. | Product-grade capture vocabulary, correction policy, duplicate review ergonomics. | M1 | Field user can log in, open assigned configured work, capture offline, sync, and append a correction. | Current contracts/BAR support kernel; correction authority/policy remains product/platform detail. |
| S01 - Entity-linked capture | A worker records about the correct known thing and preserves subject context over time. | Known thing, subject lookup, subject-linked activity entry, missing-known-thing path. | PARTIAL: subject refs, alias/history, and S01-compatible Candidate 1 evidence exist; full user fit not proved. | Lookup wording, subject confirmation cues, missing-known-thing/candidate handling. | M2 | Add subject-linked capture with a conservative missing-known-thing candidate artifact, not canonical lifecycle truth. | Accepted identity/sync contracts; if lifecycle/promotion is needed, BAR-105/NW-021. |
| S02 - Periodic reporting | Users know what recurring work is expected, missing, late, or submitted for a period. | Recurring obligation, period, freshness, missing work, report standing. | PARTIAL: S26/NW-033 proves scoped report inputs and freshness; recurring obligation product behavior is not fully proved. | Cadence model, due/missing language, old/offline report treatment, no automatic trigger creep. | M5 | Production report standing for one configured recurring activity with freshness and missing/pending labels. | NW-044 for broad reporting/API/export; BAR-101 if automatic trigger execution is requested. |
| S03 - Designated responsibility | People see and act only on assigned work, with historical responsibility traceable. | Assignment, scope, role/action, current responsibility, temporary coverage. | PROVEN for assignment/scope kernel through BAR-007, NW-050, NW-090, S22/NW-042 evidence; product setup UX PARTIAL. | Product-grade assignment setup, access-change communication, temporary coverage language. | M1/M3 | Coordinator creates a contained assignment; field user receives scoped work; supervisor sees scoped standing. | Accepted assignment/scope specs; NW-053 if new subject/query/custom scope is needed. |
| S04 - Supervisor review | A reviewer can judge another person's work and preserve the result. | Pending review, review decision, reviewer authority, attention item. | PARTIAL: S21/NW-029 proves capture-with-review mechanics; generic S04 product UX not complete. | Review queue/one-item grammar, return/reject/approve language, correction vs review boundaries. | M3 | One production review path over assigned captured work with exact resolver/authority guardrails. | Accepted flag/resolution behavior via NW-072; NW-045 for batch/automation/reassignment. |
| S05 - Supervision and audit visits | A supervisor plans or performs visits, records observations, and follows gaps. | Visit, assessment, temporal rhythm, review, follow-up attention. | PARTIAL: S21 evidence covers point-in-time supervisor assessment; planned visit cadence/follow-up is not complete. | Visit schedule/cadence, missed/late visit standing, follow-up semantics. | M3/M5 | Supervisor visit record with scoped review and visible freshness, without automatic escalation. | BAR-101/NW-045 only if trigger/automation is requested. |
| S06 - Maintaining a known set of things | The organization keeps a current, trustworthy list of facilities, equipment, people, areas, or other operational subjects. | Known thing, registry entry, lifecycle, verification, duplicate candidate, inactive/moved/retired, merge/split. | PARTIAL: subject identity, alias/history, merge/split foundations exist; generic registry lifecycle is not accepted product behavior. | User-facing registry model, missing-known-thing promotion, lifecycle states, duplicate review, edit authority. | M2 | Production-usable known-things baseline: lookup, candidate missing-known-thing, steward review, inactive/verification standing for one subject type. | BAR-105 / NW-021 before lifecycle implementation; contracts/CDL for identity authority. |
| S06b - Shape evolution | Records remain meaningful when required information changes over time and offline users submit old-shape work. | Shape version, old/new configuration, in-progress old-shape work, cross-version reporting. | PARTIAL: BAR-010, NW-032/S23, NW-034 prove config/package/version mechanics; product change lifecycle and reporting semantics are not complete. | Coordinator-facing change rules, shape diff/review tooling, in-progress behavior, cross-version report language. | M2/M5 | Shape evolution slice for one activity/known thing: deploy v2, accept old v1 offline work, show version-aware review/report standing. | Existing config contracts; NW-044 for broad reporting/API/export; architecture route if historical interpretation changes. |
| S07 - Resource distribution | Sender records dispatch, receiver confirms receipt, discrepancies remain visible. | Handoff, receipt, discrepancy, state progression, custody-like responsibility. | PARTIAL/PROVEN control evidence: S27/NW-030 proves transfer pattern; generic S07 production UX remains partial. | Product handoff language, discrepancy review UX, partial receipt rules. | M4 | One dispatch/receipt/discrepancy path over existing transfer pattern. | Accepted pattern/flag behavior; NW-045 for auto-resolution; NW-053 for custom custody scope. |
| S08 - Case management | A long-running situation remains trackable until resolution, with interactions and reopen/transfer pressure visible. | Ongoing work item, state progression, cross-reference, review, responsibility. | PARTIAL: pattern/projection foundations exist; generic case-management product is not proved. | Case lifecycle vocabulary, reopen/close policy, related activity links, handoff context. | M3/M5 | One long-running work item with manual review/correction and bounded prior context. | NW-073 if dependent pattern behavior must become durable; NW-045 for automation/batch conflict work. |
| S09 - Coordinated campaign | Time-bound work across many places can be assigned, tracked, and reviewed. | Campaign, grouped locations, responsibility, progress, freshness, handoff. | PARTIAL/PROVEN control evidence: S22/NW-042 proves constrained campaign composition with existing constructs. | Campaign product IA, progress display, grouped-location UX, no discovered-unit lifecycle drift. | M4 | One campaign/grouped-location workflow using existing assignment, capture, transfer, and freshness constructs. | BAR-105/NW-021 if discovered-unit lifecycle is needed; NW-053 for custom campaign scope. |
| S10 - Dynamic targeting | Changing conditions alter what needs attention or follow-up. | Conditional attention, target set, warning, follow-up work. | NOT_RUN for product implementation; expressions/patterns provide adjacent evidence only. | Trigger boundaries, target-set language, no device-side or deployer-scripted triggers. | M5 | Bounded conditional attention report/advisory that does not create automatic work. | BAR-101/CDL-042 before trigger execution; NW-053 if dynamic scope is requested. |
| S11 - Multi-step approval | Work moves through multiple human judgments without losing authority traceability. | Approval chain, level, reviewer authority, state progression. | PARTIAL: pattern/flag foundations exist; multi-step production UX not proved. | Approval-chain product grammar, level authority, return/reject behavior. | M3 | Two-step approval over one configured activity with exact authority and traceability. | Accepted pattern/flag behavior; NW-045 for automation/batch; architecture if new authority primitive is proposed. |
| S12 - Event-triggered actions | An observation can require follow-up attention under explicit delayed/offline semantics. | Triggered attention, delayed central visibility, escalation pressure. | NOT_RUN for trigger execution; scenario pressure exists and general trigger execution is deferred. | Server-side trigger policy, user-visible delayed trigger semantics, escalation rules. | M5 | Start with non-executing conditional attention surfaced to reviewer, not automatic action. | BAR-101/CDL-042 before runtime trigger execution; BAR-102 for auto-resolution. |
| S13 - Cross-flow linking | Separate activities can be related and interpreted together without new envelope fields by drift. | Related activity, cross-reference, subject/activity context. | PARTIAL: `subject_ref`, `activity_ref`, payload/config/projection paths exist; product linking UX not proved. | Link vocabulary, trace target, relation review, reporting interpretation. | M5 | Link two configured activities through accepted refs/payload and show scoped related context. | BAR-107 if new envelope fields are proposed; NW-044 if broad reporting/export is needed. |
| S14 - Multi-level distribution | Multi-hop movement through responsibility levels remains traceable with partial receipts and discrepancies. | Multi-hop handoff, hierarchy, receipt, discrepancy, progress. | PARTIAL/PROVEN control evidence: S27/NW-030 proves logistics handoff; full multi-level distribution product not complete. | Multi-level product IA, downstream continuation while discrepancy unresolved, scoped summaries. | M4 | Two-hop distribution slice with dispatch, receipt, discrepancy, and scoped standing. | NW-053 for custom scope; NW-045 for automated discrepancy cleanup. |
| S19 - Offline capture and sync | Work happens offline, syncs later, and preserves field-time versus central visibility. | Offline working set, local save, sync status, stale authority, freshness. | PROVEN: NW-025/S19; NW-059; shared-device/local-state evidence. Product support language remains PARTIAL. | Device-loss policy, long offline operating policy, retention/security, sync recovery support. | M1/M3/M5 | Included in M1 baseline and rechecked in every later slice as a DoD item. | NW-054 for retention/security/device lifecycle; operational policy for support. |
| S20 - CHV field operations | Composite field workday combines capture, subject context, responsibility, supplies, and follow-up. | Real-world field workflow composition, not a separate product module. | PARTIAL: constituent mechanisms and S21/S27 controls exist; complete S20 product journey not proved. | End-to-end field day IA, navigation, supplies/subject/review transitions. | M1/M3/M4 | Use as an end-to-end smoke across M1 plus later M3/M4 increments. | Consumes accepted slices; route S06/NW-021 or M4 if those pressures become necessary. |
| S21 - Supervisor visit and assessment | Supervisor reviews scoped field work and records point-in-time assessment. | Supervisor assessment, review, freshness, scoped visibility, attention. | PROVEN control lane: NW-029/S21; S26/NW-033 adjacent. Product UX remains PARTIAL. | Supervisor review IA, visit cadence, attention wording. | M3 | Production supervisor assessment and review path over assigned work. | Accepted flag/resolution and assignment specs; NW-045 for queue/batch/automation. |
| S22 - Coordinated distribution campaign across grouped locations | Campaign teams work across grouped locations with supply flow, reassignment, duplicate pressure, and freshness. | Grouped-location campaign, progress, discovered-unit pressure, handoff, scoped standing. | PROVEN control lane: NW-042/S22 using existing constructs. Product journey remains PARTIAL. | Campaign IA, discovered-unit lifecycle decision, supply-flow UI, progress/freshness language. | M4 | One campaign slice using current constructs, with explicit no-lifecycle or routed lifecycle boundary. | BAR-105/NW-021 for discovered-unit lifecycle; NW-053 for custom scope. |
| S23 - Configure new operational activity | Coordinator sets up work without commissioning software. | Setup, activity, shape, validation, publish, rollout, old/new config. | PROVEN: NW-032/S23; NW-088 config workflow exists. Product-grade editor/tooling PARTIAL. | Structured editor/diff, setup language, approval policy, safer authoring UX. | M1/M2 | M1 uses existing production config workflow; M2 adds shape-evolution-specific authoring/review. | Existing config contracts; architecture route if config becomes arbitrary code. |
| S24 - Long-running deployment data lifecycle | Deployment handles active data, retained history, local retention, audit, and sensitive data posture. | Data lifecycle, retention, decommission, audit reconstruction, sensitivity. | PARTIAL: selective retention/shared-device foundations exist; broader retention/security policy NOT_RUN. | Expiry/decommission, sealed recovery, local encryption, redaction/no-local-retention, audit policy. | M5 | Production posture note and minimal operator procedure for active/local data standing, no broad security claim. | NW-054/BAR-106 before retention/security promises; NW-044 for broad audit/reporting. |
| S25 - Worker onboarding, transfer, leave, and exit | Responsibility changes preserve continuity, stale work handling, and local/session boundaries. | Responsibility transfer, handoff, stale authority, offboarding, shared device. | PARTIAL: NW-025, NW-042, NW-050, NW-055, PC4 handoff evidence support parts; full worker lifecycle product not complete. | Account/login workflow, handoff UX, offboarding/decommission policy, retained local data. | M3 | Worker transfer slice: end old assignment, start successor, show current work and bounded context with stale caveats. | NW-054 for retention/offboarding; auth specs for principal binding; NW-053 if scope expands. |
| S26 - Operational reporting and aggregate oversight | Supervisors/coordinators see scoped progress, freshness, unresolved issues, and traceability. | Report view, freshness, unresolved treatment, scoped aggregate, drill-back. | PROVEN control lane: NW-033/S26; PC3 snapshot route. Product reporting breadth PARTIAL. | Stable report IA, recurrence semantics, export/import boundary, aggregate access rules. | M5 | One production scoped report for selected milestone data with freshness and unresolved issue treatment. | NW-044 for broad reporting/API/export/import; architecture route if aggregates bypass detail access. |
| S27 - Logistics distribution composite | Non-health distribution handoffs prove the product model is domain-neutral. | Handoff, discrepancy, multi-hop movement, domain label separation. | PROVEN control lane: NW-030/S27. Product language and full UX PARTIAL. | Logistics/custody wording, discrepancy review ergonomics, downstream continuation rules. | M4 | Domain-neutral distribution slice with dispatch, partial receipt, discrepancy, and resolution review. | NW-053 for custody scope; NW-045 for automation/auto-resolution. |

## 3. Evidence Standing And Staleness Correction

Already-proven control lanes:

- S00/S19: simple structured capture, offline/stale authority, sync, correction,
  and local status evidence.
- S21/S26: supervised review, scoped visibility, unresolved issue treatment,
  freshness, and report-input evidence.
- S27/S22: non-health transfer, discrepancy, scoped sync, coordinated campaign,
  subject-history handoff, and progress/freshness aggregation evidence.
- S23: setup/config evidence for bounded setup, validation, atomic package
  delivery, and old/new shape coexistence mechanics.

These lanes are not enough by themselves. They are the controls that future
implementation slices should keep passing while the missing portfolio areas
land.

Primary unresolved foundational lane:

- S06/S06b: known-set/entity lifecycle and shape evolution. Current architecture
  and runtime evidence support pieces of subject identity, lineage,
  subject-history, config delivery, and shape versioning. They do not yet create
  production-usable product behavior for registry lifecycle, missing-known-thing
  promotion, active/inactive or equivalent lifecycle, duplicate stewardship,
  user-facing merge/split UX, shape-change authoring rules, in-progress
  old-shape work, or cross-version reporting semantics. This is M2, not a
  generic deferral.

## 4. Ordered Milestone Roadmap

Milestones use order, not calendar dates. M0 is the only planning-only
milestone, because it directly creates the complete backlog and Definition of
Done needed for production-usable implementation slices. M1-M5 must produce
working system increments.

### M0 - Complete Product Portfolio And Evidence Map

Output:

- One product planning spec covering S00-S14, S19, S20-S27, and S22.
- Scenario evidence map with PROVEN/PARTIAL/NOT_RUN/FAILED standing.
- Ordered product backlog and product-level Definition of Done.
- Explicit first production-usable slice candidate per scenario group.

Usable increment:

- No runtime code. This is allowed only because it directly unblocks the
  production-usable roadmap and prevents implementation drift.

Current standing:

- This amended NW-168 is M0 entry evidence. NW-169 must complete the product
  model, interaction grammar, ordered backlog, and Definition of Done.

### M1 - Production-Usable Core Field Work Baseline

Covers:

- S00, S03, S19, S20, S23, with S21/S26/S27 controls retained as regression
  evidence.

Goal:

- Setup, assignment, mobile login, offline capture, sync, scoped review/standing
  for one real production-usable field-work path.

What is already evidenced:

- Mobile OIDC login through NW-165.
- Setup/config through NW-032 and production web-admin config through NW-088.
- Assignment administration through accepted assignment/admin command
  capabilities and production `/web-admin/assignments`.
- Offline capture/sync/correction through NW-025, NW-026, NW-059, and NW-062.
- Scoped standing/report inputs through NW-033 and PC3 snapshot evidence.

Remaining gaps:

- Tie these surfaces into one production-usable journey with product-safe
  language, operator setup steps, validation gate, and a real slice acceptance
  checklist.
- Decide what "production-usable" means without claiming full production
  cutover, real users/data, imports, or retention/security promises.

First production-usable slice candidate:

- One owner-operated local/on-prem field-work baseline: configure one activity,
  bind a real local actor, assign scoped work, log in on mobile, capture offline,
  sync, review scoped standing, and append a correction.

Definition of Done pressure:

- Uses accepted auth, config, assignment, sync, and correction behavior.
- Shows local save/sync/freshness without false live-truth claims.
- Includes narrow manual/ops evidence for the local deployment path.
- Does not select legacy import, submitted-record replay, or production cutover.

### M2 - Known Things And Shape Evolution

Covers:

- S01, S06, S06b.

Goal:

- Known-set/subject-linked work, missing-known-thing path,
  active/inactive-or-equivalent lifecycle, duplicate/review pressure, shape
  version evolution, and old records remaining meaningful.

What is already evidenced:

- Subject refs, aliases, merge/split foundations, subject-history backfill, and
  shape/version/config-package behavior have accepted baseline evidence.
- S23/NW-032 proves old/new setup mechanics at runtime.

Remaining gaps:

- Product-safe known-thing vocabulary.
- Registry lifecycle stance: active/inactive, moved, retired, replaced,
  duplicate candidate, verification, or a deliberately smaller equivalent.
- Missing-known-thing path and promotion/rejection.
- Duplicate review and merge/split UX boundaries.
- Shape-change authoring/diff/review behavior.
- In-progress old-shape work and cross-version reporting semantics.

First production-usable slice candidate:

- One known-things slice for a single subject type: lookup/select a known thing,
  record work against it, create an unpromoted missing-known-thing candidate
  offline, steward-review the candidate, mark inactive or needs-verification
  using the selected lifecycle vocabulary, deploy a v2 shape, accept old v1
  offline work, and show old/new records as meaningful without false
  comparability.

Authority/gap route:

- BAR-105/NW-021 is the route before implementing generic S06 lifecycle.
- Existing contracts and CDL govern identity, envelope, config-package, and
  shape-format boundaries.
- NW-044 is needed if cross-version reporting becomes broad reporting/API/export
  work.

### M3 - Review, Correction, Responsibility, And Attention

Covers:

- S04, S05, S08, S11, S21, S25.

Goal:

- Human judgment, correction, review, responsibility transfer, stale work, and
  attention items as production-usable product interactions.

What is already evidenced:

- S21/NW-029 proves scoped review mechanics and unresolved-flag exclusion.
- NW-072 records current conflict/flag and attention-read boundaries.
- PC4/NW-134 through NW-138 proves a bounded responsibility handoff surface.
- NW-025 proves stale offline authority acceptance/flagging.

Remaining gaps:

- Product grammar for review, correction, return/reject/accept, needs attention,
  blocked/incomplete/stale, resolver-unassigned, and trace targets.
- Responsibility-transfer and offboarding operating policy.
- Queue/list/batch/automation boundaries.
- Long-running work/case management behavior.

First production-usable slice candidate:

- One supervisor review and responsibility handoff increment: field worker
  submits assigned work, supervisor reviews and records judgment, stale/offline
  work is preserved with caveats, successor sees current work plus bounded prior
  context, and attention is resolved only through accepted resolver authority.

Authority/gap route:

- Accepted assignment, conflict/flag, shared-device, and handoff specs govern
  current behavior.
- NW-045 if batch, automation, auto-resolution, or resolver reassignment is
  selected.
- NW-054 for offboarding/decommissioning/local retention promises.

### M4 - Handoff, Distribution, Campaign, And Multi-Level Coordination

Covers:

- S07, S09, S14, S22, S27.

Goal:

- Movement, receipt, discrepancy, multi-hop responsibility, campaign/grouped
  location work, and coordination across people and places.

What is already evidenced:

- S27/NW-030 proves transfer-with-acknowledgment, discrepancy review,
  out-of-order accept-and-flag, scoped sync, and non-health vocabulary pressure.
- S22/NW-042 proves constrained campaign composition with assignments,
  subject-history handoff, duplicate flagging, transfer pattern, scoped sync,
  and progress/freshness aggregation over current constructs.

Remaining gaps:

- Product IA for campaign and multi-hop distribution.
- Discrepancy review ergonomics.
- Downstream continuation while discrepancy remains unresolved.
- Grouped-location/discovered-unit lifecycle boundary.
- Whether current scope axes are sufficient for custody/campaign pressure.

First production-usable slice candidate:

- One domain-neutral distribution/campaign slice: configure dispatch, receipt,
  discrepancy, and resolution activities; assign sender/receiver/supervisor
  responsibility; run offline dispatch/partial receipt; show scoped progress,
  unresolved discrepancy, and exact resolver review.

Authority/gap route:

- BAR-105/NW-021 if discovered-unit or canonical lifecycle behavior is needed.
- NW-053 if current geography/subject-list/activity/time scopes are
  insufficient.
- NW-045 if discrepancy automation or batch resolution is selected.

### M5 - Reporting, Recurrence, Conditional Work, And Cross-Flow Linking

Covers:

- S02, S10, S12, S13, S24, S26.

Goal:

- Recurring obligations, conditional attention, related activities, reporting
  freshness, cross-version/reporting semantics, lifecycle/retention posture, and
  no false live-truth claims.

What is already evidenced:

- S26/NW-033 proves scoped report inputs, freshness inputs, unresolved issue
  treatment, scoped inclusion/exclusion, and event drill-back without a broad
  reporting warehouse/API.
- PC3/NW-128 through NW-131 proves one scoped snapshot route.
- S06b/S23 evidence supports shape-version mechanics that reports must respect.

Remaining gaps:

- Recurring obligation product model.
- Conditional attention without trigger-engine drift.
- Cross-flow link product grammar.
- Cross-version report semantics.
- Device lifecycle, retention, local encryption/redaction/no-local-retention,
  and audit posture.
- Broad reporting/API/export/import boundary.

First production-usable slice candidate:

- One scoped operational report over a recurring configured activity: show due
  or missing standing, latest synced/freshness, unresolved issue treatment,
  related activity trace, and version-aware "not collected in this version"
  semantics, without triggering automatic work.

Authority/gap route:

- NW-044 before broad reporting/import-export/API/warehouse work.
- BAR-101/CDL-042 before runtime trigger execution.
- NW-054/BAR-106 for retention/security/device lifecycle claims.
- BAR-107 if cross-flow linking requires new envelope fields.

## 5. Product-Level Definition Of Done For Future Slices

Every M1-M5 implementation slice must:

- Deliver a working system increment usable in the local/on-prem production
  path, not only analysis or documentation.
- Name the scenario coverage and milestone.
- State user outcome, target users, acceptance criteria, and non-goals.
- Preserve S00 simplicity and S19 offline confidence.
- Reuse accepted contracts and CDL boundaries.
- Treat tests, scenarios, artifacts, PM handoffs, and runtime probes as
  evidence, not authority.
- Include validation evidence from the touched-surface matrix.
- Check security, authorization, sync/offline, freshness, attention, and
  operations impact where relevant.
- Record every deferral with reason, consequence, trigger, and route.
- Avoid domain vocabulary becoming shared Datarun vocabulary.
- Avoid implementation slices that only produce more planning unless that
  planning directly unblocks a production-usable increment.

## 6. What NW-169 Must Produce

NW-169 must not repeat the old minimal representative-journey pattern. It must
use this complete portfolio to produce:

- Complete product conceptual model across S00-S14, S19, S20-S27, and S22.
- Interaction grammar for setup, known things, assignment, offline capture,
  correction, review, attention, freshness, handoff, recurrence, reporting, and
  shape evolution.
- Ordered Product Backlog mapped to M1-M5.
- Product-level Definition of Done for production-usable increments.
- Scenario coverage matrix with PROVEN/PARTIAL/NOT_RUN/FAILED status.
- Mismatch classification using the NW-167 sequencing matrix.
- S06/S06b as a primary stress test of the model and backlog, not a deferred
  footnote.

NW-169 remains planning/consolidation only. It should not implement runtime
behavior, change contracts, change CDL, change BAR, change the gap register,
approve production, or create a product candidate handoff unless a later
selected route explicitly asks for that handoff.

## 7. Explicit Non-Selections

NW-168 does not select:

- runtime implementation;
- product candidate handoff;
- full product conceptual model acceptance;
- UI/component model acceptance;
- architecture/platform decision;
- contract/schema/sync change;
- BAR/CDL/gap-register change;
- real-production approval;
- domain-specific product vocabulary acceptance;
- legacy data import;
- account import;
- submitted-record replay;
- production cutover.

## 8. Deferrals And Wake-Up Conditions

Capability: Runtime implementation for M1-M5
Related user and journey step: All milestones after M0.
Why it matters: The roadmap must lead to production-usable increments, but
implementation must stay small-batch and separately selected.
Current evidence: This document creates the portfolio/roadmap. Runtime evidence
already exists for several control lanes.
Why it is not required now: NW-168 is docs-only M0 portfolio correction.
Consequence of deferral: No code or behavior changes can be inferred from this
PR.
Dependency or trigger: NW-169 completes the product model, Definition of Done,
and ordered backlog; then a specific production-usable slice is selected.
Expected reconsideration point: First post-NW-169 implementation/task packet.

Capability: Generic known-set/entity lifecycle implementation
Related user and journey step: M2 S01/S06/S06b known things and shape evolution.
Why it matters: The complete product model cannot avoid known things; many
Phase 1 scenarios depend on stable operational subjects.
Current evidence: Subject identity, alias/history, subject-history, merge/split
foundations, and shape versioning are evidenced. Generic registry lifecycle is
not accepted product behavior.
Why it is not required now: M2 must be selected as a production-usable slice
with BAR-105/NW-021 routing before implementation.
Consequence of deferral: M1 may use lifecycle-neutral subject-linked capture,
but it must not silently create active/inactive, candidate-promotion,
discovered-unit, merge/split UX, or lifecycle truth.
Dependency or trigger: Any selected slice needs maintained known things,
missing-known-thing promotion, inactive/retired/moved truth, duplicate
stewardship, discovered-unit lifecycle, or merge/split UX.
Expected reconsideration point: NW-169 backlog ordering, then M2 slice selection
through BAR-105/NW-021.

Capability: Shape evolution product behavior
Related user and journey step: M2 S06b and M5 reporting across versions.
Why it matters: "Set up, not built" must survive real operational change, old
records, and offline old-shape work.
Current evidence: BAR-010, NW-032/S23, NW-034, and current contracts prove much
of the mechanics.
Why it is not required now: Product rules for shape change, diff/review tooling,
in-progress work, and cross-version reporting need an ordered slice.
Consequence of deferral: Current setup evidence remains valid, but product
readiness for evolving long-running deployments remains PARTIAL.
Dependency or trigger: M2 or M5 slice needs shape-change user behavior,
cross-version reports, or breaking-change rules.
Expected reconsideration point: NW-169, then M2/M5 slice selection.

Capability: Broad reporting/import-export/API/warehouse
Related user and journey step: M5 S02/S13/S24/S26 reporting and cross-flow
interpretation.
Why it matters: Reports can leak access, hide uncertainty, or become new
authority if not routed.
Current evidence: NW-033/S26 and PC3 prove bounded scoped report/snapshot
behavior.
Why it is not required now: The complete portfolio needs reporting in the
roadmap, not a broad reporting implementation in NW-168.
Consequence of deferral: M5 starts with bounded scoped reporting; broader
reporting/export/import waits for the right route.
Dependency or trigger: Product work needs report APIs, warehouse/export/import,
broad drillback, or aggregate visibility beyond underlying detail access.
Expected reconsideration point: NW-169 classification; NW-044 when selected.

Capability: Trigger execution, automatic follow-up, auto-resolution, and batch
resolution
Related user and journey step: M5 S10/S12 and M3/M4 attention/discrepancy
flows.
Why it matters: Conditional work is real, but unbounded triggers/automation can
turn configuration into a programming platform or bypass human authority.
Current evidence: Expressions, patterns, flags, and manual resolver behavior are
evidenced; trigger execution and automation are deferred.
Why it is not required now: M5 can start with non-executing conditional
attention and manual review.
Consequence of deferral: No automatic work creation, escalation, auto-resolution,
or batch resolution is selected.
Dependency or trigger: A production-usable slice requires automatic triggered
work, automatic conflict cleanup, batch commands, or resolver reassignment.
Expected reconsideration point: NW-169 for classification; BAR-101/CDL-042,
NW-045, or BAR-102/BAR-103 routes when selected.

Capability: Retention/security/device lifecycle/offboarding
Related user and journey step: M3 S25 and M5 S24.
Why it matters: Production use needs honest local-data, device-loss,
offboarding, and sensitive-data posture.
Current evidence: Selective retention and shared-device/local-state behavior are
accepted, but broader retention/security promises are not.
Why it is not required now: NW-168 does not select cutover, real users/data, or
security/retention policy.
Consequence of deferral: No no-local-retention, redaction, encryption,
decommissioning, sealed recovery, or off-host recovery claim is made.
Dependency or trigger: A slice or cutover claim needs those guarantees.
Expected reconsideration point: NW-169 classification; NW-054/BAR-106 when
selected.

Capability: New scope mechanisms, custody scope, custom/query scope, broad audit
Related user and journey step: M2 known things, M4 custody/campaign work, M5
reporting/audit.
Why it matters: Scope changes affect access and sync authority.
Current evidence: Existing assignment axes and command containment cover current
proof lanes.
Why it is not required now: The roadmap can start with accepted geography,
subject-list, activity, temporal, and assignment-derived access.
Consequence of deferral: No custom scope, query-as-config, custody scope, or
broad audit/history access is selected.
Dependency or trigger: A selected slice cannot be expressed through accepted
scope axes or needs broad audit/query access.
Expected reconsideration point: NW-169 classification; NW-053 or NW-044 when
selected.

Capability: Real users/data, legacy account import, submitted-record replay, and
production cutover
Related user and journey step: Deployment after product model, roadmap, selected
production-usable slices, validation, and operations evidence.
Why it matters: These are go/no-go and operational readiness decisions.
Current evidence: NW-163, NW-164, and NW-165 record current real-use posture and
local auth/mobile proof. NW-166 remains pre-cutover hardening.
Why it is not required now: NW-168 is portfolio and roadmap correction.
Consequence of deferral: No real-production approval, real users/data, imports,
replay, or cutover claim can be inferred.
Dependency or trigger: Owner explicitly selects cutover preparation or an import
/ replay / real-use route.
Expected reconsideration point: After NW-169 and selected implementation slices,
or through NW-166/NW-093-style routes when triggered.

## 9. Recommended Successor

Select exactly one successor:

`NW-169 - Consolidate complete product model, interaction grammar, and ordered slice backlog`

NW-169 must use the complete portfolio and milestone roadmap above. It must
stress-test the model against S06/S06b as a primary unresolved foundational
case, not repeat only the already-proven S00/S19, S21/S26, and S27/S22 control
lanes.
