# FD-PKT-101 S06 Entity Lifecycle Discovery And BAR-105 Successor Decision Seed

Status: prepared task-packet draft

Date: 2026-06-13

Authority: none. This packet prepares S06 discovery and a BAR-105 successor
decision seed. It does not authorize implementation, change CDL, BAR, NW,
contracts, schemas, APIs, runtime behavior, event vocabulary, or product scope
by itself.

## 1. Header

Packet ID: FD-PKT-101

Lane: S06/entity lifecycle discovery and BAR-105 successor-decision seed

Assigned role: Product Manager + steward accountability

Claim status: `needs-decision`; discovery/decision seed only. This packet does
not authorize implementation.

Objective: make the S06 product need visible before Candidate 1 implementation
dispatch, collect the evidence needed to decide whether first deployment needs
maintained known things, and seed the BAR-105 successor route without creating
entity-lifecycle behavior.

Authority and source order:

1. CDL/contracts remain authority where applicable. This packet did not open
   the full CDL and does not change contracts.
2. `AGENTS.md` and `docs/status.md` Current Routing define current repo
   posture and default guardrails.
3. `docs/agent-working-surface/first-deployment-task-packet-router.md`
   controls first-deployment packet sequencing.
4. BAR/NW define accepted, deferred, and future-decision standing.
5. The gap-routing playbook controls classification and escalation.
6. FD-PKT-001 is complete as Option C: S06 discovery runs before Candidate 1
   implementation dispatch.
7. FD-PKT-002 is Candidate 1 product/spec and UX validation context, not
   implementation authority.
8. Scenario, user-fit, workshop, review, and exploration files are
   product/problem evidence and routing context only.

Allowed files/contracts:

- Allowed write: this file only.
- Allowed source context: the files named in the FD-PKT-101 dispatch.
- Allowed contract stance: reference current envelope, sync, flag, shape,
  config package, platform payload, pattern, and fixture boundaries only as
  existing guardrails. No contract edits, new schema rows, new event vocabulary,
  or implementation authorization.

Commit boundary: docs-only packet creation. Do not edit code, contracts,
schemas, APIs, status, routers, backlogs, BAR/NW, CDL, workshop control files,
or runtime behavior. Do not commit.

## 2. Role Boundary

Product Manager + steward accountability decides or prepares:

- The S06 discovery problem statement, evidence plan, product pressure, and
  decision questions.
- Whether Candidate 1 can honestly stay S01-compatible without maintained
  known things in its first implementation slice.
- Which user/SME evidence is required before a BAR-105 successor decision.
- Candidate decision options and their product implications.
- Routing for BAR-105 successor work if S06 must be promoted.
- Stop conditions that prevent Candidate 1 copy, UX, or implementation packets
  from implying lifecycle truth.

Product Manager + steward accountability does not decide:

- Architecture primitives, event envelope fields, event types, scope
  mechanisms, runtime authority, resolver semantics, contracts, schemas, APIs,
  durable workflow state, or implementation shape.
- Canonical entity lifecycle, active/inactive/retired truth,
  discovered-unit lifecycle, registry stewardship, duplicate workflow, or
  merge/split UX.
- Production web admin auth, mobile OIDC/Keycloak login, token lifecycle,
  retention/security, reporting/export/import, custom/query scope, conflict
  automation, resolver reassignment, auto-resolution, or ops readiness.
- Implementation tasks, files to edit, test commands beyond planning
  expectations, release approval, or commits.

Operational/persona labels are acting contexts only. They must not become
actor identity categories, authority primitives, fixed product modules, config
namespaces, product-area boundaries, or implementation service boundaries.

## 3. Why S06 Is Product-Needed

The product pressure is concrete: first deployments may depend on a working
list of things that field teams recognize, select, update, and revisit over
time. Those things may be facilities, households, equipment, sites, areas,
service points, stock locations, groups, people, or other domain nouns.

Today the likely workaround is spreadsheets, paper lists, fragmented legacy
registries, or local field knowledge. That creates operational failure modes:
field users carry stale lists, create duplicates when they cannot find the
right thing, record against the wrong thing, or keep working after a thing has
closed, moved, changed name, or needs verification. Supervisors then need to
know what changed, whether the change is trustworthy, and whether current
views are fresh enough to act on.

Candidate 1 can validate basic capture, optional subject-linked capture,
offline save/sync, correction, freshness, and unresolved issue visibility. But
the moment product copy says "known thing," "candidate," "duplicate," "closed,"
"moved," "inactive," or "verified," users may reasonably infer maintained
registry behavior. FD-PKT-101 exists to decide whether that behavior is needed
before first implementation, and if not, to make the candidate-only boundary
obvious enough that users do not mistake Candidate 1 for lifecycle truth.

Owner: Product Manager for discovery and decision recommendation; steward
accountability for source order, BAR-105 routing, forbidden work, and stop
conditions.

Decision point: before Candidate 1 implementation dispatch.

## 4. Current Standing

- BAR-105 `S06/entity lifecycle` is `deferred`. Exit requires a product decision
  that promotes entity lifecycle with bounded scope and acceptance tests.
- NW-021 is `future_decision`: either keep S06 deferred or promote entity
  lifecycle through a bounded successor plan.
- NW-036 is `future_decision`: product/platform must choose whether to promote
  entity lifecycle, trigger/reporting expansion, auto-resolution, or analytics
  expansion; no implementation prompt exists until the promoted surface is
  bounded.
- FD-PKT-001 is complete as Option C. Candidate 1 product/spec may proceed as
  an S01-compatible slice, while S06 discovery runs in parallel before the
  Candidate 1 implementation gate.
- FD-PKT-002 is drafted as Candidate 1 product/spec and UX validation context.
  It may validate linked/unlinked/candidate language, but implementation remains
  blocked until FD-PKT-101 resolves or explicitly excludes the S06 dependency.
- Candidate 1 remains S01-compatible only if subject-linked capture means
  linking a record to an existing known thing, and missing-known-thing handling
  remains unpromoted candidate/capture evidence for review.

## 5. S06 Discovery Scope

FD-PKT-101 discovery should answer these areas without accepting behavior:

- Known thing definition: what real-world things matter for the first
  deployment, what users call them, and whether they behave as subjects,
  processes, assignments, activities, or something else.
- Initial known-set source: whether the starting list comes from import,
  setup-owner entry, an external registry, paper/spreadsheet cleanup, field
  discovery, or a mixed process.
- Field discovery semantics: when a worker cannot find a thing, whether the
  product needs candidate evidence only, a candidate subject for review, or
  canonical registry creation through a successor decision.
- Lifecycle vocabulary: which words users already use for no-longer-current
  things, such as inactive, closed, moved, retired, replaced, split, duplicate,
  pending verification, or needs review.
- Registry stewardship: who owns updates, verification, duplicate review,
  candidate promotion, deactivation, movement, closure, retirement, and
  sensitive changes.
- Duplicate handling: what duplicate cases occur, how they are discovered, who
  reviews them, what evidence reviewers need, and which duplicates block
  downstream action.
- Merge/split expectations: whether first deployment needs only duplicate
  review language, controlled merge/split governance, or no merge/split UX in
  the first implementation slice.
- Movement, closure, and retirement: how organizations handle things that move,
  close, become inactive, change identity cues, split into successors, or
  should no longer receive new work.
- Review/verification policy: which changes need review, who can verify, what
  verification means operationally, what happens offline, and how users
  distinguish "current view" from preserved history.

## 6. Non-S06 Boundaries

This packet does not cover:

- Reporting dashboards, report APIs, warehouses, export/import, aggregate
  access divergence, or production reporting freshness semantics.
- Custom/query scope, auditor access beyond assignment-derived access, special
  read/write bypasses, emergency override semantics, or new scope mechanisms.
- Retention/security/device lifecycle, expiry, decommissioning, sealed
  recovery, local encryption, no-local-retention, redaction, token/session
  retention, or regulatory erasure.
- Production web admin auth, online binding-admin UI/API, mobile OIDC/Keycloak
  login, token refresh/logout, secure storage, or IdP group/claim authority.
- Conflict automation, batch conflict handling, resolver reassignment,
  auto-resolution, pending-match derivations, or broad review queue product
  design.
- Ops readiness, TLS/secrets, backup/restore, migration rollback, monitoring,
  incident response, constrained-deployment runbooks, or staging rehearsal.
- Deployer-authored access logic, deployer-authored state machines, scripts,
  custom traversals, device-side triggers, expanded expression functions, or
  new pattern inventory.

## 7. Decision Questions

Product/SME questions:

1. What known things does the first deployment actually operate on?
2. What is the minimum useful known-set source for day one?
3. What do field users do today when the thing is missing, stale, renamed,
   duplicated, closed, moved, or ambiguous?
4. Which lifecycle words are already used operationally, and which are unsafe
   to freeze as generic product language?
5. Who may add, update, verify, deactivate, close, move, merge, split, or
   retire entries today?
6. Which registry changes require review before other teams can act on them?
7. Which duplicate cases are common, which are harmless, and which block
   downstream action?
8. What evidence would prove users understand candidate evidence versus
   canonical known-thing truth?

Platform/steward questions:

1. Can Candidate 1 remain honest with unpromoted candidate capture only?
2. If S06 is promoted, can the minimal behavior stay within current subject,
   event, projection, flag, assignment/access, sync, and config boundaries?
3. Would promotion require new envelope fields, event types, identity
   categories, scope mechanisms, authority sources, stored workflow state,
   trigger execution, or deployer-authored state machines?
4. Which contracts, code paths, fixtures, automated tests, and manual
   walkthroughs would be touched by a bounded successor?
5. Does a proposed lifecycle state belong in product vocabulary, ordinary
   configured capture, platform-spec behavior, operational policy, or formal
   architecture decision?

## 8. Candidate Decision Options

Option A: Keep S06 out of Candidate 1 implementation.

Implications:

- Candidate 1 can proceed only with S01-compatible subject-linked capture and
  unpromoted missing-known-thing/candidate evidence.
- Candidate 1 copy must avoid active/inactive/retired/closed/moved/verified
  truth and merge/split product promises.
- S06 remains a named near-future lane, not vague later work.

Required evidence:

- SME/user validation that candidate-only missing-known-thing handling is
  understandable and acceptable for first deployment.
- Vocabulary tests proving users do not interpret candidate/unlinked capture as
  registry creation or lifecycle truth.
- Examples showing first deployment can operate without lifecycle states.

Option B: Promote a minimal S06 successor decision before Candidate 1
implementation.

Implications:

- Candidate 1 implementation remains paused until the successor decision
  bounds known-set source, field discovery semantics, lifecycle vocabulary,
  stewardship, duplicate handling, and tests.
- The successor must state whether behavior stays within existing boundaries or
  requires formal architecture/platform decision.
- Candidate 1 may absorb only the explicitly promoted minimal behavior.

Required evidence:

- Product/SME examples proving maintained known things are required for first
  implementation, not only later roadmap value.
- A bounded acceptance checklist and impacted contract/code/test map.
- Steward classification showing no hidden envelope, event type, scope,
  authority, stored state, trigger, report/export, retention/security, or
  auth/admin/mobile-login expansion.

Option C: Split S06 into named successor packets.

Implications:

- FD-PKT-101 seeds multiple bounded packets instead of one broad entity
  lifecycle slice.
- Likely splits are known-set source and candidate semantics; lifecycle
  vocabulary and verification; duplicate review and merge/split governance;
  registry stewardship policy; and platform/contract impact assessment.
- Candidate 1 implementation remains blocked only on the split packet that
  affects Candidate 1 honesty.

Required evidence:

- A routed-lane register with owner, evidence, route, and decision point for
  each split.
- SME examples showing which split is needed before implementation and which
  can safely follow.
- FD-PKT-003 evidence gates that preserve the unresolved splits as stop
  conditions.

Option D: Keep full S06 deferred, but require a pre-release review gate.

Implications:

- Candidate 1 implementation can be prepared only after FD-PKT-003 proves the
  candidate-only boundary; release claims still cannot imply registry
  lifecycle.
- The first deployment plan must name the S06 pre-release checkpoint and the
  evidence that would trigger promotion.
- This is lower immediate scope, but higher risk if deployment users expect
  maintained known things.

Required evidence:

- Clear first-deployment wording that excludes lifecycle behavior.
- Product risk sign-off that known-set maintenance is not needed for the first
  deployment promise.
- A trigger list for revisiting BAR-105 before release, such as repeated SME
  demand for active/inactive state, duplicate stewardship, or candidate
  promotion.

## 9. Architecture/Steward Routing

BAR-105 successor route:

```txt
S06 product pressure
-> product/SME evidence
-> classify gaps with the gap-routing playbook
-> choose Option A, B, C, or D
-> if promoted, write a bounded successor packet
-> only then draft platform-spec or implementation packets
```

Formal architecture/platform decision is required if a proposal:

- adds or implies new envelope fields or event type values;
- changes stored event meaning, historical interpretation, or subject-lineage
  semantics;
- adds identity categories, new scope mechanisms, custom/query scope, or access
  outside assignment-derived access;
- moves authority, state, resolver truth, actor identity, or current registry
  truth into a new durable source;
- stores current workflow state or treats projections as source truth;
- rewrites historical subject references after merge or split;
- allows offline merge/split, deployer-authored lifecycle state machines,
  deployer-authored access logic, device-side triggers, or unbounded config
  expressiveness;
- implements auto-resolution, resolver reassignment, emergency override
  semantics, broad audit/history access, report/export/import surfaces, or
  retention/security behavior without the relevant successor route.

Stop immediately if Candidate 1 copy, UX, specs, tests, or implementation
packets turn candidate capture into canonical registry creation, active/inactive
truth, discovered-unit lifecycle, registry stewardship, duplicate workflow, or
merge/split UX before BAR-105 successor routing.

## 10. Evidence Needed

Product examples:

- At least two concrete registry examples from target-like deployments, naming
  the thing type, current artifact, owner, update path, offline behavior, and
  duplicate/lifecycle pain.
- Examples of current workarounds: spreadsheets, paper lists, government
  registries, local ledgers, WhatsApp lists, legacy databases, or program
  lists.

SME validation:

- Vocabulary sessions covering known thing, candidate, unlinked, needs review,
  duplicate, inactive, closed, moved, retired, verified, merged, and split.
- SME review of who may add/update/verify/deactivate/close/move/merge/split,
  and which actions require review.

Registry artifacts:

- Sample initial known-set artifacts, update forms, verification notes,
  duplicate review notes, deactivation/closure records, and current-view
  outputs.
- Evidence of whether the known set is imported, centrally created,
  field-created, or mixed.

Duplicate/merge/split examples:

- Obvious duplicate, ambiguous duplicate, harmless duplicate, wrong link,
  wrong merge, true split, movement/closure, and stale offline update examples.
- Reviewer evidence requirements for each example.

Lifecycle vocabulary tests:

- Comprehension checks for current view versus preserved history.
- Tests that candidate/unlinked language does not imply registry creation.
- Tests that inactive/closed/moved/retired words do not become accepted
  platform behavior unless routed.

FD-PKT-003 should expect:

- Claim-wording checks that banned lifecycle and production-readiness claims
  are absent from Candidate 1 surfaces unless routed.
- A manual missing-known-thing walkthrough proving users understand candidate
  evidence.
- A duplicate-suspected walkthrough proving users see review need without
  auto-merge, auto-resolution, or resolver reassignment.
- A stale/offline known-set walkthrough proving old work is preserved and
  review-visible without rejection or lifecycle truth.
- A release gate requiring FD-PKT-101 to choose or explicitly defer S06 before
  Candidate 1 implementation dispatch.

## 11. Downstream Packet Impacts

| Packet | Impact |
|---|---|
| FD-PKT-002 | Keep the explicit Option C dependency marker. Candidate 1 product/spec and UX validation may continue, but it must avoid lifecycle truth and test candidate versus canonical understanding. |
| FD-PKT-003 | Convert this packet into evidence gates, vocabulary tests, manual walkthroughs, scenario probes, release gates, and an implementation stop gate for S06-sensitive claims. |
| FD-PKT-004 | Mobile/offline validation must keep missing-known-thing capture unlinked/candidate unless S06 is promoted. Mobile copy must not imply registry creation, active/inactive truth, merge/split UX, mobile login, or retention/security behavior. |
| FD-PKT-005 | Assess whether Candidate 1 can stay adapter/view composition over existing constructs. Any shared S06 data model, stable view contract, contract change, or lifecycle state shape must route before implementation. |
| Later implementation packets | Remain blocked on the chosen S06 option if they touch known-set source, candidate promotion, lifecycle vocabulary, registry stewardship, duplicate handling, merge/split UX, or lifecycle tests. Implementation packets must name exact files/contracts, accepted constructs reused, forbidden work, expected tests, and stop conditions. |

## 12. Forbidden Work

- Do not implement S06 or authorize implementation.
- Do not edit code, contracts, schemas, APIs, status, routers, backlogs, BAR/NW,
  CDL, workshop control files, or runtime behavior.
- Do not commit.
- Do not create canonical entity lifecycle, active/inactive/retired truth,
  discovered-unit lifecycle, registry stewardship, duplicate handling workflow,
  merge/split UX, movement/closure/retirement behavior, or review/verification
  policy as accepted behavior.
- Do not add or imply new envelope fields, event types, scope mechanisms,
  identity categories, durable workflow state, report/export/import surfaces,
  retention/security behavior, production auth/admin/mobile login, conflict
  automation, resolver reassignment, or auto-resolution.
- Do not add or imply deployer-authored access logic, deployer-authored state
  machines, scripts, custom traversals, device-side triggers, expression
  function vocabulary, expanded `context.*` refs, or new pattern inventory.
- Do not make Candidate 1 anything other than S01-compatible unless a successor
  decision is explicitly required and completed before implementation.
- Do not bury S06 as vague later work without owner, evidence, route, and
  decision point.
- Do not turn persona labels into authority primitives.

## 13. Stop And Report Conditions

Stop and report if:

- Current routing, BAR/NW standing, FD-PKT-001, FD-PKT-002, scenario evidence,
  or the consolidated exploration note conflict in a way that changes this
  packet's boundary.
- Candidate 1 cannot honestly remain S01-compatible without maintained known
  things, lifecycle state, discovered-unit stewardship, registry stewardship,
  duplicate stewardship, merge/split UX, or lifecycle words.
- Product validation requires a contract, code, schema, API, runtime behavior,
  or test fixture change before S06 is routed.
- Any user-facing term is promoted into an event field, event type, scope
  mechanism, identity category, flag category, contract/schema field, durable
  workflow state, authority rule, or shared API meaning.
- Candidate/unlinked/missing-known-thing language implies canonical registry
  creation, active/inactive truth, verification truth, or automatic matching.
- Duplicate, merge, split, resolver, or review language implies auto-merge,
  auto-resolution, resolver reassignment, direct flag mutation, or bypass of
  exact designated-resolver semantics.
- Reporting/export, production auth/admin/mobile login, retention/security,
  custom/query scope, conflict automation, or ops readiness enters the S06
  decision as implementation scope.
- Persona labels harden into identity categories, fixed modules, access rules,
  config namespaces, product-area boundaries, or implementation service
  boundaries.
- Unrelated worktree changes appear; leave them alone and report them.

## 14. Done Definition

FD-PKT-101 is done when:

1. The packet exists at
   `docs/workshops/first-deployment/task-packets/fd-pkt-101-s06-entity-lifecycle-discovery.md`.
2. S06 is visible as product-needed work with owner, evidence, route, and
   decision point.
3. BAR-105/NW-021/NW-036 standing is represented without changing BAR/NW.
4. Candidate 1 Option C dependency is explicit and keeps Candidate 1
   S01-compatible unless a successor decision promotes minimal S06 before
   implementation.
5. Discovery scope covers known thing definition, known-set source, field
   discovery semantics, lifecycle vocabulary, registry stewardship, duplicate
   handling, merge/split expectations, movement/closure/retirement, and
   review/verification policy.
6. Non-S06 boundaries, forbidden work, stop conditions, evidence needs, and
   downstream packet impacts are explicit.
7. The file contains no deprecated first-deployment review path references.
8. `git diff --check` passes.
