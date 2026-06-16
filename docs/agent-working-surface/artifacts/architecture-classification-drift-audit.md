# Architecture Classification Drift Audit

Status: non-authoritative routing artifact
Date: 2026-06-16
Source: owner request before NW-067; no NW execution
Authority: none. This audit is subordinate to the CDL, contracts, the decision
anchor layer, BAR, backlog, documentation standards, and accepted
implementation evidence. If this audit conflicts with the CDL, the CDL wins.
Scope: focused architecture-classification, routing-hygiene, and IDR
durable-behavior extraction scoping before the broader operating-framework
question. This does not promote or demote CDL decisions, execute NW-067,
redesign architecture, define an IDR boundary rule, or authorize implementation
from `future_decision` rows.

## Evidence Basis

Primary active sources checked:

- `docs/exploration/archive/00-exploration-framework.md`
- focused ADR-4/ADR-5 exploration archive files on irreversibility,
  remaining-question resolution, and structural coherence
- `docs/architecture/adrs-decisions-canonical-ledger/adr-to-cdl-map.md`
- `docs/implementation/phases/phase-3.md`
- `docs/implementation/phases/phase-3d.md`
- `docs/implementation/phases/phase-3e.md`
- `docs/architecture/adrs-decisions-canonical-ledger/README.md`
- `scripts/query_cdl.py` output for all CDL classifications and focused full
  slices for CDL-034 through CDL-054
- `docs/status.md`
- `docs/documentation-organization.md`
- `docs/agent-working-surface/README.md`
- `docs/agent-working-surface/decision-anchor-layer/README.md`
- `docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/agent-working-surface/baseline-acceptance-register.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/escape-hatch-register.md`
- `docs/decisions/README.md`, `docs/decisions/INDEX.md`, IDR-018, and
  IDR-020 through IDR-030
- `contracts/flag-catalog.md`, `contracts/sync-protocol.md`,
  `contracts/config-package.schema.json`, `contracts/shape-format.schema.json`,
  `contracts/pattern-definition.schema.json`, `contracts/patterns/*.json`, and
  `contracts/shapes/*.schema.json`
- `docs/flagged-positions.md` summary and relevant resolved/open FP context
- pre-NW-063 routing and assessment material: NW-043, NW-044, NW-049,
  NW-051, NW-053 through NW-057, the 2026-06-04 gap-baseline checkpoint, the
  2026-06-11 decision-anchor-standing checkpoint, the pattern-core boundary
  assessment, and provenance-only notes 004/006 on reporting/export and
  platform-spec detail closure
- provenance-only `.review/untracked-user-notes/notes/009-*` and `010-*`
  architecture-classification draft notes, verified against active sources

No tests were run. This is a documentation and routing audit only.

Inference rule: each project-specific conclusion is based on the active sources
above. Where the source does not directly say the conclusion, the assessment
uses "inference" wording and gives confidence.

Override rule: implementation evidence can prove that an evolvable choice has
become a de facto contract. It cannot prove that an architecturally wrong path
should be preserved. Phase 3e is the controlling example: code, fixtures, and
docs had normalized the wrong envelope-type path, and the repair corrected the
implementation/contracts/docs to the architectural position.

## 1. Original Classification Model

The current CDL classifications show three core classes plus adjacent split
classifications such as `Mechanism / config split`. The irreversibility filter
from the owner request is the right discriminator:

- stored-state impact: changing the decision would transform stored events,
  local stores, config rows, projections, or compatibility state;
- contract-surface impact: more than two independent components must agree on
  shape or meaning;
- wrong-choice recovery: recovery would need migration, protocol change, data
  repair, authority audit, or coordinated mobile/server upgrade.

| Class | Intended meaning | Belongs there | Does not belong there | Downstream work may depend on it when | Escalation trigger |
|---|---|---|---|---|---|
| Structural | A permanent invariant, contract, or source-of-truth boundary. | Event envelope, closed type vocabulary, reference contracts, append-only truth, sync/access equality, assignment-derived authority, client-minted IDs, identity lineage, projection rebuildability. | UI labels, product copy, deployment preferences, ordinary helper names, authoring conveniences. | The work implements, tests, validates, documents, or builds UI over the accepted boundary. | Any change to event meaning, envelope fields/types, reference shape, sync/access scope, authority source, workflow truth, or cross-process contract. |
| Strategy-protecting | A guardrail that prevents implementation/product pressure from undermining structural decisions. | Detect-before-act, bounded expressions, config/code ceiling, assignment containment, workflow projection discipline, platform-owned resolvability, server-only L3 policy. | Pure implementation layout, one-off operational steps, visible labels, ordinary UX affordances. | The work preserves the guardrail and names the negative boundary. | Refinement adds a durable source of authority/state, becomes a wire/stored/config contract, weakens the guardrail, or raises migration/coordinated-upgrade cost. |
| Initial strategy | A selected first posture inside accepted architecture, expected to evolve if the irreversibility filter stays green. | Projection location, selective local retention posture, shape delta authoring versus full runtime snapshots, domain uniqueness boundary, sensitivity levels, pattern composition, source-only cascade, current `context.*`, auto-resolution mechanism class. | Decisions already requiring stable event, schema, sync, authority, or multi-component agreement after data exists. | The NW states what remains evolvable and treats the row as guarded, not hidden architecture. | The strategy becomes mandatory through contracts, shared fixtures, offline stores, server/mobile compatibility, migration cost, or future decisions. |

Mixed CDL rows should be read literally, not flattened. Example: CDL-035 is
`Initial strategy / strategy-protecting constraint`; stale-work acceptance and
visibility are load-bearing guardrails, while exact deployment severity posture
remains more evolvable within platform rules.

Mechanism/config split rows are not a weaker classification. They prevent
deployer-authored instances from becoming platform mechanisms, and prevent
platform mechanisms from being smuggled into configuration.

## 2. Classification-To-Routing Map

| Decision class | Can IDRs refine it? | Can NWs implement it? | Can backlog depend on it? | Can gaps modify it? | Can implementation evidence challenge it? | Must go back to architecture review when | Safe at spec/implementation level when |
|---|---|---|---|---|---|---|---|
| Structural | Not decided here. Existing or future IDRs should be assessed later against the gap-routing class they actually serve. | Yes, after authority is accepted and with tests. | Yes, as a hard guardrail. | No. Gaps reveal drift or route successor decisions. | Yes, as contradiction/unimplemented-standing evidence, not authority. | Stored event meaning, envelope, vocabulary, reference, sync/access, authority, projection truth, or compatibility semantics change. | The work adds tests, packaging, validation, performance, UI, or adapters without semantic change. |
| Strategy-protecting | Not decided here. Later IDR assessment should decide whether the content belongs in platform spec, architecture, implementation, or should stop as an IDR role. | Yes, with guard tests and explicit forbidden work. | Yes, but row/prompt should name the guardrail. | Only by routing a successor decision or stronger spec. | Yes, especially if the guardrail is too weak or became contract-like. | Recovery would require migration, protocol change, new authority source, coordinated upgrade, or an exception to the guardrail. | The change is local enforcement, warning, validation, docs, or UI and the negative boundary remains true. |
| Initial strategy | Not decided here. If an IDR has made an initial strategy mandatory, classify that content in the later gap-routing pass. | Yes, if reversible or explicitly bounded to accepted baseline. | Yes, with guarded dependency wording. | Yes for spec/policy/implementation gaps; no if irreversibility is triggered. | Yes. Evidence is expected to challenge or split initial strategies. | The strategy becomes a required server/mobile/config/wire/local-store contract, affects stored state, or blocks future migration. | It remains replaceable implementation/policy detail under existing contracts. |
| Mechanism/config split | Not decided here. Later assessment should classify whether IDR content is mechanism, config, platform spec, contract, or implementation provenance. | Yes, if the split is preserved. | Yes, as a routing guard. | May clarify or split, not collapse. | Yes, if config starts behaving as code or authority. | Deployer config gains code, containment logic, state-machine authoring, resolver authority, triggers, or new scope semantics. | The change only adds authoring UX, validation, or packaged instances inside platform-owned mechanisms. |

## 3. Drift Audit

| Candidate | Original class | Current downstream usage | Evidence | Irreversibility implicated | Assessment | Recommended routing action | Confidence |
|---|---|---|---|---|---|---|---|
| CDL-035 authorization staleness | Initial strategy / strategy-protecting constraint | BAR/NW evidence treats stale offline work as accept-and-flag with mobile advisory behavior and category severity semantics. | CDL-035; IDR-021; NW-024; NW-025; BAR-013; flag catalog. | Stored/offline behavior via accepted events and flags; structural basis also covered by CDL-003, CDL-004, CDL-030, CDL-031. | Weak routing risk, not true drift. The mixed class is accurate but easy to misread. | Keep class. Future prompts should cite CDL-003/CDL-004 with CDL-035 when relying on stale-work acceptance. | High |
| CDL-036 projection location | Initial strategy | Device, server, and scenario reporting work use local/server projections without creating report authority. | CDL-036; BAR-014; NW-033; gap playbook GAP-PROJECTION-01/02. | Contract-surface only if reports bypass event access or freshness constraints. | Harmless downstream refinement. | Keep initial. Escalate only if aggregate/read APIs bypass event-level access or create new read authority. | High |
| CDL-037 scope contraction retention | Initial strategy / device policy boundary | Mobile selective retention is accepted; IDR-030/NW-055 adds actor partitions; NW-054 remains future decision for expiry/security. | CDL-037; BAR-008; IDR-030; NW-055; BAR-106/NW-054. | Local/offline stores and retention. Server deletion would trigger stored-state impact. | Guarded initial strategy. No canonical event drift because server truth is preserved and broader retention is deferred. | Keep canonical event truth structural elsewhere and keep local selective retention guarded. Route expiry, sealed recovery, encryption, no-local-retention, and erasure through NW-054/security/platform decision before product claims. | High |
| CDL-040 full runtime shape snapshots | Initial strategy | Config package and mobile/server behavior rely on complete shape snapshots and version coexistence. | CDL-040; BAR-010; NW-031; `contracts/config-package.schema.json`; `contracts/shape-format.schema.json`; IDR-017/019. | Contract-surface across config package/server/mobile. Wrong recovery may need coordinated handling. | Stale classification for runtime representation. Authoring-as-delta remains initial/tooling. | Decompose: keep authoring delta as initial; extract complete runtime snapshot delivery, coexistence, and resolver expectations into durable platform spec or contract notes under CDL-039/CDL-041. | Medium-high |
| CDL-045 domain uniqueness | Initial strategy / boundary | Shape DSL, config package, mobile advisory checks, server detector, flag catalog, and BAR-013 share one uniqueness model. | CDL-045; IDR-022; BAR-013; NW-034; `contracts/shape-format.schema.json`; flag catalog. | Contract-surface and offline/server coordination; stored events unchanged because violations are flags. | Stale/mixed classification. Detection representation is load-bearing; resolution policy remains separate. | Split durable home: declaration-to-standard-flag behavior, offline/server parity, and detector ordering belong in platform spec/contract; resolution automation remains NW-045. | Medium-high |
| CDL-046 sensitivity | Initial strategy | Config includes shape/activity sensitivity; BAR-106 routes field-level sensitivity, encryption, redaction, and retention. | CDL-046; BAR-008; BAR-106; NW-049; NW-054; config package schema. | Retention/security/reporting if used to change sync, storage, export, or redaction. | No current drift. Weak routing risk if product/security claims exceed coarse classification. | Keep initial. Do not use sensitivity as retention, reporting/export, redaction, encryption, or compliance behavior until BAR-106/NW-054 or a reporting/export decision routes that behavior. | High |
| CDL-050 pattern composition | Initial strategy | Pattern definition schema, packaged definitions, server/mobile projection equivalence, and deploy-time validation depend on current composition rules. | CDL-050; IDR-020; IDR-025; BAR-012; pattern contracts; NW-046. | Contract-surface across server/mobile/projection/config. Stored events can remain stable if pattern refs are versioned. | Load-bearing initial strategy. | Extract current composition, traversal, and projection-equivalence rules into a platform pattern contract/spec; future pattern-specific fields or traversal semantics require platform evolution. | Medium-high |
| CDL-051 source-only flagging | Initial strategy | Flag/projection model preserves root-cause flags; NW-046 is future decision for generic cascade indicators. | CDL-051; IDR-026; BAR-006/BAR-013/BAR-014; NW-046; flag catalog. | Projection semantics and possible stored flags if propagation is introduced. | Guarded and now load-bearing for root-cause flag semantics. Current use is coherent; future cascade work is the risk. | Re-test before NW-046. Downstream indicators may be platform-spec projections; emitted propagated flags or changed flag truth require architecture review. | Medium |
| CDL-052 `context.*` form context | Initial strategy | IDR-018 defines seven properties; NW-057 made unknown `context.*` deploy-time invalid; BAR-011 accepted tests. | CDL-052; IDR-018; NW-057; BAR-011; decision anchors. | Stored config and server/mobile evaluator compatibility. | Load-bearing closed vocabulary. No drift after NW-057 because the gap was explicitly resolved. | Extract the seven accepted properties and deploy-time-invalid behavior into validator-facing platform spec or contract-adjacent reference. New properties require platform-evolution/platform-spec route, not silent null tolerance. | High |
| CDL-053 auto-resolution | Initial strategy | Runtime execution remains deferred; IDR-026 prevents auto-eligible from meaning system-owned. | CDL-053; CDL-054; IDR-026; BAR-102; NW-045; flag catalog. | Would affect policy execution and resolver semantics if implemented. | No drift. Correctly deferred. | Keep initial/deferred. Do not implement until NW-045 or successor decision selects policy, trigger, resolver designation, and tests. | High |
| CDL-034 assignment containment | Strategy-protecting security constraint | IDR-024 expands containment across all axes; IDR-029/NW-050 requires command-capable assignment plus containment. | CDL-034; IDR-024; IDR-029; NW-050; BAR-007. | Authorization visibility, assignment events, sync/access safety. | Stale classification pressure toward structural/security invariant. No behavioral drift observed. | Split durable home: containment-before-append is structural/security; assignment command capability policy belongs in platform/security spec. Weakening containment is architecture-grade. | Medium-high |
| CDL-041 atomic config packages | Strategy-protecting constraint | Package schema, mobile two-slot promotion, pattern definitions, severity, sensitivity, roles, and shapes rely on atomic server/mobile package semantics. | CDL-041; BAR-010; IDR-019; IDR-025; config package schema. | Contract-surface and coordinated server/mobile upgrade. | Behaves contract-level in practice. Strategy-protecting still blocks drift, but routing should treat wire/promotion semantics as structural contract-level. | Extract atomic package wire shape, unknown-key tolerance, current/pending promotion, and referenced-definition delivery into contract/platform spec; authoring/publishing workflow remains platform or product spec. | Medium-high |
| CDL-043 expression language | Strategy-protecting constraint | IDR-018 JSON AST is persisted/synced cross-platform; NW-057 adds deploy-time `context.*` whitelist. | CDL-043; IDR-018; BAR-011; NW-057; expression fixtures. | Stored config and evaluator compatibility. | Boundary is sound; concrete AST serialization is contract-like. | Keep CDL-043 but formalize JSON AST, context whitelist, versioning, and migration posture in platform spec or contract-adjacent reference. Grammar changes require platform decision plus compatibility plan. | High |
| CDL-047 projection-derived workflow state | Strategy-protecting constraint | IDR-020, BAR-012, BAR-014, NW-062 rely on no stored current workflow state. | CDL-047; CDL-002; IDR-020; BAR-012/BAR-014. | Stored-state and recovery if durable state becomes truth. | Behaves structural, but covered by CDL-002 and guarded by CDL-047. | No immediate reclass. Treat as structural in routing. | High |
| CDL-054 flag resolvability | Strategy-protecting constraint | Resolvability is catalog/schema-visible and policy-critical; deployer overrides cannot change it. | CDL-054; flag catalog; conflict payload schemas; IDR-026; BAR-006. | Contract and resolver/policy compatibility. | Classification is acceptable only if read as a hard platform guardrail; risky if read as ordinary strategy. | Treat flag resolvability as a platform contract invariant. New categories or resolvability changes require successor decision, not deployment config or UX convenience. | Medium-high |
| NW-063 through NW-067 operations lane | Operational policy/tooling/runbook/rehearsal | Reference deployment work selects target, tooling, runbook, and rehearsal without changing platform semantics. | NW-063 through NW-067 rows; operations docs. | None unless tooling changes authority, contracts, sync, rollback, or retention semantics. | Correct routing. No architecture drift found. | Keep operations lane. Escalate only if deployment tooling proposes new semantic behavior. | High |

## 4. Load-Bearing Initial Strategies

| Initial-strategy row | Current role | Recommendation | Rationale |
|---|---|---|---|
| CDL-035 authorization staleness | Load-bearing but already mixed. | Keep mixed; pair with CDL-003/CDL-004/CDL-030/CDL-031 in prompts. | Structural acceptance/flagging is anchored elsewhere; severity details remain bounded strategy/policy. |
| CDL-036 projection location | Not structurally load-bearing. | Keep initial. | Projection placement can move if event truth, access constraints, and freshness metadata stay intact. |
| CDL-037 local retention | Guarded load-bearing local-store policy. | Keep local selective retention guarded; route expiry, sealed recovery, encryption, no-local-retention, and erasure through NW-054/security/platform decision. | Server event truth is structural elsewhere; local retention/security cannot be decided for implementation convenience. |
| CDL-040 shape full snapshots | Runtime representation is load-bearing. | Decompose and extract runtime snapshot delivery/coexistence to platform spec or contract notes. | Runtime complete snapshots are now server/mobile config behavior; delta authoring remains tooling/initial. |
| CDL-045 domain uniqueness | Load-bearing detection representation. | Split into platform-spec/contract detection semantics and future resolution automation. | Server/mobile/config/flag semantics now agree on detection and accept-and-flag behavior. |
| CDL-046 sensitivity | Not yet structurally load-bearing. | Keep initial under BAR-106/NW-054/reporting-export guard. | Stronger privacy, export, redaction, storage, or security behavior is explicitly deferred. |
| CDL-050 pattern composition | Load-bearing for current registry/projection. | Extract current composition/traversal/equivalence rules to platform pattern contract/spec. | Pattern definitions and projections are cross-component contract surfaces. |
| CDL-051 source-only flagging | Guarded load-bearing. | Re-test before NW-046; projection indicators are platform-spec, emitted propagated flags are architecture-grade. | Root-cause flag truth is now load-bearing; cascade work is not ordinary implementation. |
| CDL-052 `context.*` | Load-bearing closed vocabulary. | Extract accepted refs and invalid-unknown behavior to validator-facing platform spec/contract-adjacent reference. | NW-057 closed the ambiguity and added deploy-time invalid behavior. |
| CDL-053 auto-resolution | Not implemented. | Keep initial/deferred. | Mechanism class is accepted, but execution is BAR-102/NW-045. |

Do not promote a whole row merely because it is implemented. Promotion or split
is justified only when the irreversible part is shared through contracts,
stored config, mobile/server compatibility, authority, or offline/local-store
behavior.

## 5. Strategy-Protecting Decisions That Became Structural

| Decision | Structural pressure observed | Recommended handling |
|---|---|---|
| CDL-034 assignment containment | Wrong behavior stores assignment events that alter future sync/access. IDR-024 and IDR-029 make full-axis command-plus-containment central. | Split containment-before-append as structural/security and command-capability policy as platform/security spec. |
| CDL-041 atomic config package | Package shape and current/pending promotion are server/mobile compatibility surfaces. | Extract wire shape, unknown-key tolerance, promotion, and referenced-definition delivery to contract/platform spec; authoring workflow remains spec/tooling. |
| CDL-043 bounded expression language | Strategy row is sound, but IDR-018 JSON AST is persisted and synced across Java/Dart. | Formalize AST, context whitelist, versioning, and migration posture in platform spec/contract-adjacent reference. |
| CDL-047 workflow projection-derived state | Behaves structural, but CDL-002 already provides the structural invariant. | No reclassification needed. Keep workflow-specific guardrail. |
| CDL-048 transition violation category | Flag category and detector behavior are implemented and cataloged. | Keep if CDL-054/flag catalog are treated as contract guardrails; category semantic changes need review. |
| CDL-054 flag resolvability | Resolvability is catalog-visible and policy/security-critical. | Treat resolvability as platform contract invariant; category/resolvability changes need successor decision. |

No evidence was found that strategy-protecting decisions were casually weakened
by implementation. The main risk is stale wording or weak routing, not current
behavioral contradiction.

## 6. IDR And NW Routing-Class Check

This audit does not define an IDR boundary rule. Existing IDRs are treated as
evidence and as candidates for a later gap-routing-class assessment. That later
assessment should decide whether each IDR's durable content belongs as
platform-spec detail, architecture decision, contract, implementation
provenance, or should stop as a future IDR role and be extracted into another
durable surface.

| Case | Routing-class observation | Finding | Action |
|---|---|---|---|
| IDR-018 expression grammar | Later gap-class assessment needed; likely contract/platform-spec or implementation-provenance split. | It selected a high-reversal persisted/synced AST and is accepted/tested. | Do not decide IDR role here. Later reassessment should extract durable AST/context behavior to platform spec or contract-adjacent reference if that is the selected role. |
| IDR-020 pattern state representation | Later gap-class assessment needed; likely platform-spec/architecture-provenance split. | Preserves projection-derived state, no envelope field, no stored `current_state`, and accept-and-flag transitions. | Do not decide IDR role here. Extract durable projection-state semantics to pattern platform spec if IDR role conflicts. |
| IDR-021/023 role-action boundary | Later gap-class assessment needed; likely platform-spec detail for activity work-action vocabulary and implementation provenance for detection mechanics. | Preserves accept-and-flag, excludes assignment administration from activity roles, and keeps mobile advisory. | Do not decide IDR role here. Later extraction should separate action vocabulary from detector implementation evidence. |
| IDR-022 domain uniqueness | Later gap-class assessment needed; likely platform-spec/contract content. | It did not violate architecture, but turned an initial strategy into shared server/mobile/config/flag behavior. | Review/split CDL-045 classification and extract declaration/detection semantics to durable platform spec or contract. |
| IDR-024 assignment containment | Later gap-class assessment needed; likely architecture/security decision content plus implementation tests. | It closed a real privilege-escalation gap and implies CDL-034 is stronger than a directional guardrail. | Add classification guard or review CDL-034 wording; later extraction should separate structural containment from command-policy implementation. |
| IDR-025 pattern definition contract/delivery | Later gap-class assessment needed; contract content is already under `contracts/`. | Pattern definitions feed mobile/server projection. | Current behavior is coherent; future mechanism changes route by gap class, with durable behavior kept in contracts/platform spec. |
| IDR-026 resolver routing | Later gap-class assessment needed; likely platform-spec/architecture-sensitive conflict semantics. | Resolver equality and unauthorized-resolution behavior are accepted; reassignment and auto-resolution remain deferred. | Current behavior is coherent; later extraction should keep resolver equality in durable platform/architecture surface if IDR role conflicts. |
| IDR-027/028 production auth | Later gap-class assessment needed; likely architecture/security and operational-provisioning split. | Principal binding is authentication lookup only; IdP group/claim authority remains rejected. | Current behavior is coherent; future group/claim expansion routes by gap class and should not be justified by operational convenience. |
| IDR-029/NW-050 assignment-admin command capability | Later gap-class assessment needed; likely platform-spec/security policy plus implementation. | Adds platform-owned command names without envelope/payload/scope changes. | Current behavior is coherent; extract command-capability policy to platform/security spec if IDR role conflicts, and do not use it as domain work, resolver, or audit authority. |
| IDR-030/NW-055 shared-device partitions | Later gap-class assessment needed; likely local-store/security/platform-spec split. | Adds local partitions and actor-scoped sync bookkeeping while preserving assignment-derived authority and retention deferrals. | Current behavior is coherent; extract local partition semantics to platform/security spec if needed, while NW-054 remains route for expiry/decommission/sealed recovery/local encryption. |
| NW-024 through NW-033 scenario probes | Correctly implemented/probed accepted architecture. | Repeated "no new primitive/contract" clauses prevented accidental promotion. | Keep that pattern. |
| NW-057 context boundary | Correct gap handling. | It identified ambiguity, selected deploy-time invalid, and folded result into BAR/backlog/status. | Use as model for future initial-strategy hardening. |
| NW-063 through NW-067 operations | Correct operational policy/tooling/runbook/rehearsal lane. | No architecture decision required unless operational tooling changes accepted semantics. | Keep; do not let deployment convenience alter auth/contracts/sync/retention. |

Inference, confidence high: the current risk is not that accepted IDRs
contradict the CDL. The risk is unresolved document-role hygiene: some IDRs
carry durable platform-spec, contract, security, or architecture-sensitive
content and should later be assessed against the gap-routing classes. That
assessment should prefer the platform's durable future over the current code's
local fit: if an IDR is carrying platform-spec, contract, security, operations,
or architecture content, extract that content to the correct surface and stop the
future IDR role if it conflicts.

## 7. Gap-Routing Correction

No active source shows a clear architecture-grade concern incorrectly accepted
as ordinary implementation. The correction needed now is sharper trigger logic
for initial strategies, platform-future durable-surface extraction, and a deferred
IDR reassessment route.

| Route class | Trigger conditions | Durable output | Current notes |
|---|---|---|---|
| Architecture decision | Changes event meaning, envelope fields/types, identity references, sync/access scope, authority source, workflow truth, resolver authority, trigger execution, deployer configuration boundary, or stored-state/coordinated-upgrade risk. | CDL successor or explicitly architecture-grade decision artifact. | Required for new scope mechanisms, server deletion/redaction, emergency bypass, direct IdP authority, new envelope/type, mutable workflow truth. |
| Existing IDR reassessment | Later pass classifies IDR durable content against gap-routing classes: architecture, platform spec, product spec, contract, implementation provenance, operations, validation, or documentation cleanup. | No new durable output from this audit; future assessment artifact or targeted migrations. | Choose durable home by platform semantics and future safety, not current implementation convenience. If a role conflict exists, stop using that IDR role and extract durable content into the correct surface. |
| Platform-spec atom | Architecture is settled but exact platform behavior needs stable wording. | `docs/specifications/platform/`. | Good for config package lifecycle, expression/context vocabulary, uniqueness behavior, pattern composition/traversal, reporting freshness, and pattern inventory additions inside existing mechanisms. |
| Product-spec atom | User-visible behavior, labels, journeys, and acceptance criteria. | `docs/specifications/product/`. | Good for sync status, handoff UX, conflict queue UX, warning language, shared-device switch flow. |
| Implementation/tooling task | Code, tests, packaging, CLI, build, operator tooling, performance work preserving accepted semantics. | Prompt plus code/tests or implementation doc if needed. | NW-065 is a correct example. |
| Validation/test task | Baseline exists but lacks runtime evidence or guard tests. | BAR/backlog evidence and tests. | Use before promotion where evidence is uncertain. |
| Operational policy/procedure/rehearsal | Human ownership, support, RPO/RTO, backup, incident, runbook, rehearsal evidence. | `docs/operations/`. | NW-064/NW-066/NW-067 are correctly routed. |
| Documentation cleanup | Active routers conflict, stale wording misleads agents, or accepted output is not discoverable. | Patch router/index/status only when material. | Add classification re-test guardrails to the gap playbook; defer IDR role decisions to the later reassessment. |
| Backlog refinement | Future item is too broad, stale, or lacks stop conditions. | Backlog row/prompt update. | NW-044/045/046/053/054 should keep exact architecture triggers. |
| Deferred research | Product/security/operational pressure is real but insufficiently evidenced. | Scenario/user-fit/research note or artifact. | Use for broad audit, emergency override, dynamic cohorts, regulatory erasure until concrete deployment pressure exists. |

Current future-decision routing to preserve:

- NW-044 reporting/import-export: architecture if aggregates bypass event-level
  access, import emits canonical events, or export creates new compatibility or
  provenance contracts.
- NW-045 conflict automation/batch resolution: decision before implementation;
  preserve per-flag resolution events and exact resolver equality.
- NW-046 cascade/pattern traversal: platform-spec/implementation if projection
  indicator only; architecture if it emits propagated flags or new pattern
  mechanisms.

Pre-NW-063 assessment reinforces the same route: reporting/export/import,
scope, retention, pattern traversal, and expression/context behavior should be
split into durable platform/spec/contract atoms before implementation prompts
or public API/product commitments depend on them.
- NW-053 subject/query/custom scope: architecture decision for any new
  platform-owned scope mechanism; reject deployer query-as-authority.
- NW-054 retention/security: policy/security/platform decision; architecture
  if server event truth, normal watermarks, erasure/redaction, or cross-actor
  recovery authority changes.

## 8. Guardrail Proposal

Keep this checklist-based. Do not turn it into heavy governance.

Escalate to architecture when a proposal:

- changes stored event meaning or historical interpretation;
- adds envelope fields or type values;
- adds identity reference types;
- changes sync/access scope or normal watermark semantics;
- moves authority, resolver truth, actor identity, workflow state, or scope
  containment into a new durable source;
- turns local/offline storage behavior into a migration-sensitive compatibility
  rule;
- lets deployer config author code, containment logic, dynamic queries, state
  machines, resolver authority, or triggers;
- changes server/mobile wire contracts or stored config formats requiring
  coordinated upgrade;
- implements auto-resolution, resolver reassignment, broad audit/history
  access, emergency override, regulatory erasure/redaction, or new scope
  mechanisms.

Classification review checklist:

1. Name CDL rows and original classifications.
2. Mark stored-state, contract-surface, and wrong-choice-recovery impacts.
3. Mark whether downstream work treats the result as optional, guarded, or
   mandatory.
4. If an `Initial strategy` becomes mandatory, choose: keep guarded, decompose,
   reclass review, or open successor decision.
5. If a `Strategy-protecting` row becomes a wire/stored/config contract, record
   the exact contract surface and migration/compatibility trigger.
6. Choose the durable home by platform semantics and future safety, not local
   implementation convenience.

Irreversibility re-test:

- Would existing events, local stores, config rows, projections, or support
  tables need transformation if this changes later?
- Must server, mobile, contracts, fixtures, operator tooling, or external
  processes agree on shape or meaning?
- Would recovery require migration, protocol change, coordinated upgrade,
  data repair, or authority audit?

IDR reassessment note:

- Do not define an IDR boundary rule in this audit.
- Later, classify each active IDR's durable content against the gap-routing
  classes.
- If an IDR is carrying platform-spec, contract, operations, implementation
  provenance, or architecture content in conflict with the chosen role, stop
  that future IDR role and extract the durable content into the correct surface.

NW readiness checklist:

- Source row status is `ready`, not `future_decision`, unless the task is
  decision/routing only.
- Prompt names authority, classifications, forbidden work, tests, and stop
  conditions.
- Contract files are named if process/wire boundaries are touched.
- No deferred capability is silently implemented.

Post-NW closure checklist:

- Did the implementation create a new architecture-grade fact?
- Did an initial strategy become load-bearing?
- Did a strategy guardrail become contract-like?
- Did tests prove only the intended capability?
- Did BAR/backlog/status/DEC/gap playbook change only if materially affected?
- Are residual future decisions still explicit?

Backlog/gap routing checklist:

- Every `future_decision` row names the exact architecture/product/security
  input needed before implementation.
- Every exploration that recommends implementation creates or points to a
  successor implementation row.
- Accepted probes state which primitives they did not add.
- Operational policy/runbook work states it does not prove architecture or
  real-production standing beyond accepted evidence.

## 9. Output Table

| Item | Original class | Current observed role | Drift risk | Irreversibility test triggered | Recommended action | Confidence |
|---|---|---|---|---|---|---|
| CDL-035 authorization staleness | Initial / strategy-protecting | Baseline guard for stale offline work and severity | Low-medium | Stored/offline behavior via flags; covered by structural rows | Keep mixed; cite structural anchors with it | High |
| CDL-036 projection location | Initial | Evolving projection placement | Low | None unless reports bypass access/freshness | Keep initial | High |
| CDL-037 retention after scope contraction | Initial / device policy | Accepted selective retention plus shared-device partitions | Medium | Local stores; server deletion would trigger stored-state | Keep guarded; NW-054/security before expiry, sealed recovery, encryption, no-local-retention, or erasure claims | High |
| CDL-040 shape full snapshots | Initial | Runtime config-package expectation | Medium-high | Contract surface | Extract runtime snapshot delivery/coexistence into platform spec or contract notes; keep authoring delta initial | Medium-high |
| CDL-045 domain uniqueness | Initial / boundary | Server/mobile/config/flag contract-like behavior | Medium-high | Contract surface, offline/server coordination | Split to platform-spec/contract detection semantics and NW-045 resolution policy | Medium-high |
| CDL-046 sensitivity | Initial | Coarse config marker; deeper behavior deferred | Low-medium | Retention/security/export/redaction if promoted | Keep initial; BAR-106/NW-054/reporting-export guard | High |
| CDL-050 pattern composition | Initial | Pattern contract/projection rule | Medium-high | Contract surface | Extract current composition/traversal/equivalence into platform pattern contract/spec | Medium-high |
| CDL-051 source-only flagging | Initial | Projection/flag queue guard | Medium | Projection/stored flags if propagation | Re-test before NW-046; projection indicators platform-spec, emitted flags architecture | Medium |
| CDL-052 `context.*` | Initial | Closed deploy-time vocabulary after NW-057 | Medium | Stored config and evaluator compatibility | Extract accepted refs/unknown-invalid behavior to validator-facing platform spec/contract-adjacent reference | High |
| CDL-053 auto-resolution | Initial | Deferred mechanism | Low now, high if executed | Resolver/policy/stored resolution events | Keep deferred via NW-045 | High |
| CDL-034 assignment containment | Strategy-protecting | Core authorization command invariant | Medium-high | Authority/sync safety; assignment event impact | Split containment-before-append as structural/security; command-capability policy as platform/security spec | Medium-high |
| CDL-041 config package atomicity | Strategy-protecting | Server/mobile wire/package contract | Medium-high | Contract surface/coordinated upgrade | Extract wire shape/promotion/referenced-definition delivery to contract/platform spec | Medium-high |
| CDL-043 expression ceiling | Strategy-protecting | Grammar boundary plus persisted AST lock | Medium | Stored config/coordinated upgrade | Formalize AST/context/versioning/migration in platform spec or contract-adjacent reference | High |
| CDL-047 workflow state | Strategy-protecting | Structural no-stored-workflow-state guard | Medium | Stored state/recovery | Treat as structural in routing | High |
| CDL-054 flag resolvability | Strategy-protecting | Catalog/schema-visible policy contract | Medium | Contract and resolver/policy semantics | Treat as platform contract invariant; successor decision for category/resolvability changes | Medium-high |
| IDR-018 | IDR content pending later class assessment | Persisted/synced expression AST | Medium | Stored config/coordinated upgrade | Reassess later; future changes need contract/platform route | High |
| IDR-022 | IDR content pending later class assessment | Domain uniqueness contract-like extension | Medium-high | Contract/offline-server coordination | Reassess later; likely platform-spec/contract extraction | Medium-high |
| IDR-025 | IDR content pending later class assessment | Pattern definition contract authority | Medium | Contract/coordinated upgrade | Reassess later; current contract files remain authoritative for definitions | High |
| IDR-027/028 | IDR content pending later class assessment | Production auth authority extension | Medium | Authority source/support tables | Reassess later; future expansions route by gap class | High |
| IDR-030 | IDR content pending later class assessment | Local/offline partition authority | Medium | Local stored state/sync bookkeeping | Reassess later; route retention/security to NW-054 | High |
| NW-063 through NW-067 | Ops lane | Correctly non-architecture unless semantics change | Low | None now | Keep lane; escalate on semantic change | High |

## 10. Final Recommendation

Most CDL classifications are still sound. The structural rows around event
truth, envelope/type closure, references, sync/access equality,
assignment-derived authority, projection rebuildability, and identity lineage
remain the right anchors. Recent IDRs mostly refine accepted architecture
carefully, with negative boundaries and testable stop conditions. That does not
make the IDRs the right durable home for platform behavior.

The rows needing durable-surface extraction or reclassification review are not
protocol-protection issues; they are places where downstream work split an
original decision into a load-bearing part and an evolvable part:

- CDL-040: runtime full shape snapshots versus delta authoring convenience.
- CDL-045: uniqueness detection/flag representation versus resolution and
  declaration detail.
- CDL-050: current pattern composition contract versus future pattern
  evolution.
- CDL-041 and CDL-043: strategy-protecting boundaries whose concrete package or
  grammar surfaces now behave like contracts.
- CDL-034 and CDL-054: security/policy guardrails that should require
  architecture review before weakening.

The platform-future move is to create small durable platform-spec, contract, or
security-spec atoms for config package delivery, expression/context vocabulary,
domain uniqueness detection, pattern composition/traversal, reporting
participation/freshness, flag resolvability, and assignment containment before
product/API implementation depends on those behaviors. This is not a local-fit
or implementation-convenience recommendation.

Unsafe downstream routes:

- implementing new scope mechanisms, broad audit/history pull, emergency write
  bypass, auto-resolution execution, resolver reassignment, retention/erasure
  security behavior, expression grammar changes, pattern mechanism changes, or
  reporting/import-export paths that alter access, event provenance, or
  contracts without a successor decision;
- implementing from a `future_decision` backlog row without a decision/routing
  artifact;
- treating IDRs as final durable homes before the later gap-routing-class
  assessment decides whether to stop/extract that role.

Immediate guardrails to add before the broader operating-framework prompt:

- initial-strategy re-test rule: if accepted cross-component tests, contracts,
  fixtures, or local-store compatibility now depend on an initial strategy,
  route a split/promotion/guard review before implementation;
- local/offline-store irreversibility trigger covering actor partitions,
  cursors, watermarks, sealed partitions, expiry, and selective purge;
- best-platform routing check: choose the durable surface by platform semantics
  and future safety, not by current code shape;
- defer IDR role decisions: later assess active IDRs against the gap-routing
  classes and stop/extract any role that conflicts with the selected durable
  surface.

Do not edit the CDL itself from this audit alone. A narrow follow-up cleanup can
patch DEC/gap notes or open a CDL-classification review for CDL-040, CDL-045,
CDL-050, CDL-041, CDL-043, CDL-034, and CDL-054 if that becomes the selected
route.
