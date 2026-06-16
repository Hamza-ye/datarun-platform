# IDR Durable Behavior Routing Audit

Status: non-authoritative routing artifact
Date: 2026-06-16
Source: owner request after architecture-classification drift audit; no NW
execution
Authority: none. This audit is subordinate to the CDL, contracts,
`docs/documentation-organization.md`, `docs/commit-workflow.md`, BAR, backlog,
and accepted implementation evidence.

## Scope

This audit treats active IDRs as evidence and recommends the common durable
surface for load-bearing behavior that should not remain only in IDR prose. It
does not edit IDRs, retire old docs, change
`docs/documentation-organization.md`, change `docs/commit-workflow.md`, change
CDL decisions, change contracts, change BAR standing, execute NW-067, or
create accepted platform specifications.

Old-document correctness is out of scope except where an IDR's role cannot be
classified without noting a visible routing issue. A later old-doc audit should
decide whether stale wording is corrected, superseded, or retired.

## Evidence Basis

Primary sources checked:

- `docs/exploration/archive/00-exploration-framework.md`
- focused ADR-4/ADR-5 exploration archive files on irreversibility,
  remaining-question resolution, and structural coherence
- `docs/architecture/adrs-decisions-canonical-ledger/adr-to-cdl-map.md`
- `docs/documentation-organization.md`
- `docs/commit-workflow.md`
- `docs/implementation/phases/phase-3.md`
- `docs/implementation/phases/phase-3d.md`
- `docs/implementation/phases/phase-3e.md`
- `docs/decisions/README.md`
- `docs/decisions/INDEX.md`
- `docs/decisions/idr-001` through `docs/decisions/idr-030`
- `docs/specifications/README.md`
- `docs/specifications/platform/README.md`
- `docs/operations/policies/README.md`
- `docs/operations/runbooks/README.md`
- `docs/status.md` Current Routing
- `docs/implementation/module-interfaces.md`
- `docs/agent-working-surface/baseline-acceptance-register.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `contracts/`
- `docs/agent-working-surface/artifacts/architecture-classification-drift-audit.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`

No tests were run. This is documentation routing work.

## Documentation-Practice Baseline

`docs/documentation-organization.md` is the governing standard for new durable
documentation outputs. It says durable docs are organized by role and authority,
not by workshop, milestone, agent, date, or NW number.

For this audit:

- This output belongs in `docs/agent-working-surface/artifacts/` because it is
  a non-binding routing result.
- Accepted exact platform behavior belongs in `docs/specifications/platform/`.
- Process/wire schema and protocol authority belongs in `contracts/`.
- Operational ownership, provisioning policy, retention posture, RPO/RTO,
  support, approval, and escalation belong in `docs/operations/policies/`.
- Repeatable operator commands belong in `docs/operations/runbooks/`.
- Implementation design and module boundaries belong in `docs/implementation/`.

The best-practice correction is therefore not a new "IDR boundary" or a new
decision-log category. The correction is to extract stable normative behavior
into the existing canonical homes above when adjacent work needs that behavior
as a durable target.

## Architectural Reframe

The exploration archive changes the audit question. The decisive question is
not "does the current code agree with the CDL?" or "what role did the IDR claim
when it was written?" The decisive question is:

> Did implementation turn an evolvable choice into a de facto contract that now
> needs a durable spec, contract, policy, or architecture home?

`00-exploration-framework.md` says assumptions must be explicit, scope bleed
must be detected, and decisions should be classified by permanence. It also
warns that only the irreversible part deserves full locking: stored state,
contract surface, and wrong-choice recovery are the tests.

Phase 3 proves the point. It classified shape storage, expression serialization,
and config package delivery as `Lock` decisions before implementation because
they are persisted, synced to mobile, and evaluated cross-platform. Those are
not safe to treat as ordinary IDR prose after the server, mobile, contracts, and
fixtures depend on them.

Phase 3e is the counterexample to protocol protection. The implementation had
normalized the wrong envelope-type interpretation into code, fixtures, tests,
and docs. The correction did not protect the implemented path; it chose the
architecturally correct position, moved the four strings to platform-bundled
shape names, tightened contracts, and added parity tests. The same standard
applies here: if exploration/CDL has the right position and implementation
created a conflicting or under-documented de facto contract, the right move is
to extract or correct the durable surface, not defend the document role.

## Audit Method

Each IDR was checked against four questions:

1. Did implementation make this behavior mandatory through stored state,
   contract schemas, shared fixtures, server/mobile agreement, operational
   procedure, or coordinated-upgrade cost?
2. If yes, is the de facto contract aligned with CDL and the exploration
   irreversibility position, or is it an implemented-path drift that must be
   corrected later?
3. Is the IDR carrying durable platform behavior, process/wire contract semantics,
   security/operations policy, or architecture-grade content?
4. If it is carrying durable content, which existing canonical home should own
   that content before new product/API/implementation work depends on it?

Priority labels:

- `extract-before-adjacent-work`: create or update the durable surface before the
  next meaningful change depends on that behavior.
- `extract-when-touched`: no immediate standalone migration required, but the IDR
  must not remain the only durable target when that surface changes.
- `no-extraction-now`: no de facto cross-component contract was found in this
  pass; leave the IDR as historical explanation unless later work touches the
  surface or the old-doc audit finds drift.

## Findings

1. Document role is not the classifier. An IDR can contain a correct platform
   position, a merely local implementation choice, or an implemented-path drift.
   Classify by irreversibility and current dependency, not by the IDR label.
2. `contracts/` already owns several machine/process surfaces: sync protocol,
   config package schema, deployer shape format, platform payload shapes, flag
   catalog, pattern definition schema, pattern definitions, and shared
   fixtures. These should remain canonical where they already exist.
3. `docs/specifications/platform/` is the common durable home currently missing
   for exact accepted behavior below CDL/contracts. It is the right target for
   expression semantics, config lifecycle, assignment/scope behavior,
   conflict/resolver behavior, pattern projection behavior, production auth
   boundary, and shared-device local-state behavior.
4. Some operational content currently lives in IDRs. Production binding
   provisioning and any future retention/security choices should use
   `docs/operations/policies/` and `docs/operations/runbooks/` where the
   question is owner process or executable procedure.
5. Phase 3's three `Lock` decisions are now de facto contracts: shape format,
   expression AST semantics, and config package delivery. Their durable home
   cannot be IDR prose alone.
6. Phase 3e is the model for correction: when implementation had made the
   wrong envelope-type path look real, the project corrected code/contracts/docs
   to the architectural position. Later extraction should use the same standard
   if any IDR-era implemented path conflicts with CDL/exploration.
7. A visible trace drift exists in IDR-018: it references
   `contracts/expression.schema.json`, but that file is not present. Current
   durable evidence appears to live in `contracts/config-package.schema.json`
   and `contracts/fixtures/expression-evaluation.json`. This audit does not
   fix that drift; a follow-up contract/platform-spec cleanup must decide
   whether an expression schema is required or whether the durable home is the
   platform expression spec plus shared fixtures and config-package schema.
8. Current accepted standing matters. BAR-010/BAR-011/BAR-006/BAR-104 and
   NW-040/NW-050/NW-055/NW-057 prove that several IDR-carried behaviors are
   already accepted runtime standing, not merely proposed routes.

## De Facto Contract Test

Treat an IDR-carried behavior as a de facto contract when any of these are now
true:

- it is persisted in events, config rows, support tables, mobile stores, or
  operation history;
- `contracts/` schemas, protocols, fixtures, or package keys require it;
- both server and mobile must implement the same semantics;
- BAR acceptance or backlog rows cite it as tested standing;
- changing it requires migration, compatibility handling, coordinated
  server/mobile release, data repair, or authority audit;
- operational procedures or deployment evidence depend on it being stable.

When this test is positive, the IDR's original document role is not relevant.
The behavior either already has the correct durable home, or it must be
extracted there before adjacent work relies on it.

## Current Standing Corrections

These current accepted surfaces prevent under-classifying implemented IDR-era
behavior as merely proposed:

- BAR-010 accepts config package delivery, including publish gating, endpoint
  auth/ETag behavior, monotonic atomic full snapshots, mobile two-slot
  promotion, unknown top-level package-key tolerance, and pull
  `config_version` discovery.
- BAR-011 and NW-057 accept the expression evaluator boundary, including shared
  fixtures, fail-closed evaluator behavior, and deploy-time invalidation of
  unknown `context.*` refs against the seven IDR-018 form-context properties.
- BAR-006 and `contracts/shapes/conflict_detected.schema.json` now make
  `designated_resolver` required for `conflict_detected/v1`; IDR-026's older
  "semantic requirement only" wording is historical drift for the old-doc
  audit.
- BAR-104 and NW-040 accept production principal binding and deployment-managed
  provisioning. IDR-016 is dev/evolved context for production, not a competing
  production-auth surface.
- NW-050 accepts IDR-029's assignment-admin command capability implementation:
  `assignment_admin.create` and `assignment_admin.end` are platform-owned
  commands outside `activities[*].roles`.
- NW-052/NW-055 accept and implement IDR-030's shared-device actor partition
  behavior. Expiry, decommissioning, sealed-partition recovery, token/session
  retention, local encryption, and broader retention/security remain NW-054 /
  BAR-106, not IDR-030.

## Right-Position Override

When exploration/CDL and implementation disagree, do not preserve the
implemented path just because it has code, fixtures, or tests. Phase 3e shows
the correct repair pattern:

1. identify the architecture position that should have won;
2. correct code/contracts/fixtures/docs to that position;
3. add parity or regression tests so the drift cannot re-enter;
4. mark old wording as superseded, corrected, or historical in a later old-doc
   audit.

This is why follow-up extraction should not merely copy IDR prose into
platform specs. It should restate the durable behavior in the correct home,
using CDL and exploration as the guardrail and implementation evidence only as
proof of what has become load-bearing.

## Recommended Durable Surfaces

These are existing homes from the documentation standard, not invented
surfaces:

| Surface | Use for durable behavior extraction | Notes |
|---|---|---|
| `contracts/` | JSON schema, protocol, fixture, process-boundary vocabulary, packaged wire shape. | Do not duplicate normative schema text into specs; link to contracts. |
| `docs/specifications/platform/` | Exact accepted behavior inside settled CDL/contracts. | Primary missing home for most high-reversal IDR content. |
| `docs/specifications/product/` | User-visible labels, journeys, warning copy, review queues, admin UX. | Use only when the question is what users see/do. |
| `docs/operations/policies/` | Deployment-owner choices, binding administration policy, retention posture, approval/escalation. | Policies state choices, not commands. |
| `docs/operations/runbooks/` | Repeatable operator procedures. | Procedures cite policy and evidence. |
| `docs/implementation/` | Module design, code patterns, dependency choices, performance/index rationale. | Keep low-level mechanics out of platform specs unless they are normative behavior. |

Recommended platform-spec atoms when a routed follow-up extracts IDR-carried
behavior:

- `expression-language`: IDR-018, BAR-011, expression fixtures, config package
  expression-rule schema.
- `configuration-package-and-shapes`: IDR-017/019 plus
  `contracts/shape-format.schema.json` and
  `contracts/config-package.schema.json`.
- `assignment-scope-and-administration`: IDR-013/014/015/023/024/029 plus
  assignment payload contracts and module boundaries.
- `conflict-flag-resolution`: IDR-021/022/026 plus flag catalog, conflict
  payload schemas, and BAR-006.
- `pattern-registry-and-projection`: IDR-020/025 plus pattern contracts and
  projection fixtures.
- `production-auth-principal-binding`: IDR-016/027/028 plus BAR-104,
  module boundaries, and operations surfaces.
- `shared-device-session-and-local-state`: IDR-030 plus BAR-104/BAR-106 and
  mobile/server sync boundaries.

Exact filenames and split boundaries should be selected by the follow-up route.
The point is the durable home and concern split, not these names as a new
taxonomy. Do not duplicate contract text into platform specs: `contracts/`
owns schemas, protocols, fixtures, and package keys; platform specs own exact
behavior those contracts cannot express.

## IDR Inventory

| IDR | De facto contract status | Durable content observed | Best durable home | Recommended action |
|---|---|---|---|---|
| IDR-001 test infrastructure | No cross-component platform contract found. | Local PostgreSQL test topology and VPN workaround. | `docs/implementation/` if a durable test-environment guide is needed; CI config for CI truth. | no-extraction-now |
| IDR-002 pg_idkit dev image | No cross-component platform contract found. | Development database image choice. | `docs/implementation/` or compose files if it becomes active setup guidance. | no-extraction-now |
| IDR-003 snake_case JSON | Mixed implementation/API convention. | Project-wide API JSON naming convention. | Contracts for schema-bound JSON; platform API/spec surface if public API behavior is documented. | extract-when-touched |
| IDR-004 networknt validator | No cross-component platform contract found beyond envelope validation already owned by contracts/code. | Envelope validation library selection. | `docs/implementation/` and code dependency files. | no-extraction-now |
| IDR-005 GitHub Actions CI | No platform contract found; CI process only. | CI database/service-container shape. | Workflow files; CI documentation only if durable CI operations are needed. | no-extraction-now |
| IDR-006 Thymeleaf admin | No production/admin platform contract found. | Minimal server-rendered admin UI choice. | Product/platform/admin specs only if production admin behavior is selected. | no-extraction-now; later old-doc audit may narrow status to dev-only/deprecated if active docs imply more |
| IDR-007 concurrency detection | Process/protocol plus detector rationale. | `last_pull_watermark`, per-event `W_effective`, sweep/idempotence behavior. | `contracts/sync-protocol.md` for protocol; platform sync/conflict spec for detector semantics. | extract-when-touched |
| IDR-008 server event producer | Platform behavior plus implementation. | Server `device_id`, `device_seq`, system actor authorship. | Platform event-authorship spec or module interface; contracts only if wire shape changes. | extract-when-touched |
| IDR-009 alias table | Platform identity/projection behavior plus implementation. | Rebuildable alias projection, transitive closure, advisory locking, event stream wins. | Platform identity/projection spec and module interface. | extract-when-touched |
| IDR-010 conflict detection intercept | Implementation pipeline with platform consequence. | Two-transaction accept-and-flag pipeline and deterministic flag identity. | Module interface for pipeline; platform flag spec for durable behavior. | extract-when-touched |
| IDR-011 identity conflict scope | No durable cross-component contract beyond current flag pipeline. | Manual-only identity conflict posture for early phase. | Platform/product decision only if auto-detection is selected later. | no-extraction-now |
| IDR-012 sqflite memory path | No platform contract found; test isolation only. | Mobile test isolation practice. | Mobile test guide if needed. | no-extraction-now |
| IDR-013 assignment payload | Contract/platform behavior. | Assignment payload semantics, assignment identity, scope composition, temporal flag ordering. | `contracts/shapes/assignment_*.schema.json`; platform assignment/scope spec. | extract-before-adjacent-work |
| IDR-014 materialized path locations | Platform scope behavior plus storage design. | Static location reference data, immutable write-time event `location_path`, controlled backfill. | Platform assignment/scope spec; implementation design for path/index mechanics. | extract-when-touched |
| IDR-015 scope-filtered sync query | Platform sync/access behavior plus performance design. | Pull categories, immutable `location_path`, assignment-event inclusion, system event filtering. | `contracts/sync-protocol.md`; platform sync/access spec; implementation design for indexes. | extract-before-adjacent-work |
| IDR-016 actor token table | Development auth mechanism was superseded for production by IDR-027/028 and BAR-104. | Static dev token table and migration path to production auth. | Implementation/dev setup docs; production auth spec owns production behavior. | no-extraction-now for production; later old-doc audit may mark it dev-only/evolved |
| IDR-017 shape storage | Contract/platform configuration behavior. | Shape DSL, field vocabulary, L1/L2 split, version lifecycle, flat-field lock. | `contracts/shape-format.schema.json`; platform configuration spec. | extract-before-adjacent-work |
| IDR-018 expression grammar | Accepted contract/platform expression behavior. | JSON AST, operators, seven accepted form-context refs, null handling, cross-platform evaluator fixtures, deploy-time invalid unknown `context.*` refs. | Platform expression spec; `contracts/config-package.schema.json`, fixtures, and expression schema/trace cleanup. | extract-before-adjacent-work |
| IDR-019 config package | Accepted contract/platform config delivery behavior with a process-boundary doc gap. | Atomic package shape, monotonic version, endpoint auth/ETag behavior, current/pending slots, unknown-key tolerance, pull `config_version` discovery. | `contracts/config-package.schema.json`; platform config package spec; `contracts/sync-protocol.md` or sibling process contract for endpoint/auth/ETag semantics. | extract-before-adjacent-work |
| IDR-020 pattern state representation | Platform workflow behavior. | Pattern binding object, projection-derived state, identity keys, transition flag behavior. | Platform pattern/projection spec; pattern contracts and fixtures. | extract-before-adjacent-work |
| IDR-021 role-action enforcement | Platform auth/config/flag behavior. | Activity action vocabulary, server detector semantics, mobile advisory boundary. | Platform assignment/auth spec; flag catalog for category semantics; config package contract for wire shape. | extract-before-adjacent-work |
| IDR-022 flag severity/domain uniqueness | Platform conflict/config behavior. | Severity overrides, uniqueness declaration semantics, detector order, target flag payload expectations. | `contracts/flag-catalog.md`, config/shape contracts, platform uniqueness/flag spec. | extract-before-adjacent-work |
| IDR-023 role-action domain boundary | Platform auth boundary. | `assignment_changed` excluded from activity role actions; assignment admin is separate online command domain. | Platform assignment/auth spec. | extract-before-adjacent-work |
| IDR-024 multi-axis containment | Structural/security-sensitive platform behavior. | Containment across geographic, subject-list, activity; bootstrap/root constraints; null-activity rules. | Platform/security authorization spec; CDL review if weakening is proposed. | extract-before-adjacent-work |
| IDR-025 pattern definition contract/delivery | Mostly already contract-routed. | Canonical `contracts/patterns`, package `pattern_definitions`, versioned refs. | Existing pattern contracts plus platform pattern delivery spec if human-readable behavior is needed. | extract-when-touched |
| IDR-026 resolver routing/single-writer | Accepted contract/platform conflict behavior. | Required `designated_resolver`, resolver identity function, canonical exact-resolver equality, unauthorized-resolution flagging. | `contracts/flag-catalog.md`, conflict payload schemas, platform conflict-resolution spec. | extract-before-adjacent-work |
| IDR-027 production auth principal binding | Platform/security auth behavior. | `(issuer, subject) -> actor_id`, bearer-bound push authorship, group/claim non-authority. | Platform production-auth spec; sync protocol where process shape is affected. | extract-before-adjacent-work |
| IDR-028 production principal-binding administration | Accepted platform/security plus operations policy/procedure. | Deployment-managed manifest provisioning, append-only operation history, active binding projection/support rows, create/rotate/deactivate/rebind, no direct IdP authority. | Platform production-auth spec; `docs/operations/policies/` for owner/process; runbooks for executable procedure. | extract-before-adjacent-work |
| IDR-029 assignment-admin command capability | Accepted platform/security authorization behavior. | Platform-owned `assignment_admin.create` / `assignment_admin.end`, role-to-command policy, same-assignment command-plus-containment, outside `activities[*].roles`. | Platform assignment-admin/security spec; config contract only if delivered to clients. | extract-before-adjacent-work |
| IDR-030 shared-device session lifecycle | Accepted platform/mobile/security local-state behavior. | Single active actor, per-actor partitions, sealed pending work, actor-scoped sync bookkeeping and local stores. | Platform shared-device/local-state spec; operations/security policy for retention/recovery via NW-054/BAR-106. | extract-before-adjacent-work |

## Extraction Order

Recommended order, based on current dependency pressure and future safety:

1. Configuration/expression/package:
   IDR-017, IDR-018, IDR-019. This is the shared server/mobile configuration
   substrate and is already exercised by contracts and fixtures.
2. Assignment/auth/security:
   IDR-013, IDR-015, IDR-021, IDR-023, IDR-024, IDR-027, IDR-028, IDR-029,
   IDR-030. These govern authority, sync scope, local actor state, and
   production authentication.
3. Conflict and pattern behavior:
   IDR-020, IDR-022, IDR-025, IDR-026. These govern projection truth, flag
   handling, and cross-platform workflow behavior.
4. Older implementation records:
   IDR-001 through IDR-012, IDR-014, and IDR-016 do not need standalone
   extraction unless adjacent work turns their behavior into a de facto contract
   or the old-doc audit finds conflict with the right architectural position.

This order is not a request to write all specs immediately. It is a routing
guard: when work touches one of these behaviors, create or update the durable
home before relying on the IDR as the implementation target.

## Stop Conditions For Follow-Up Work

Stop and route to architecture when extraction discovers that accepted behavior
would change:

- event envelope fields or type vocabulary;
- source-of-truth authority for assignment, resolver, actor identity, or
  workflow state;
- sync/access scope equivalence;
- process/wire contract shape;
- local/offline storage compatibility with migration-sensitive recovery;
- deployer config becoming code, dynamic query authority, state-machine
  authoring, resolver authority, or trigger execution.

Stop and route to operations when the question is owner choice, retention,
approval, evidence, support, RPO/RTO, provisioning process, or executable
operator procedure.

Stop without extraction when the IDR only explains a dependency, local test
workaround, implementation index, or historical rejected alternative and no
current de facto contract depends on it.

## Final Recommendation

Do not create a new durable IDR-like surface. IDR provenance is irrelevant to
classification: when implementation has made behavior mandatory across stored
state, contracts, fixtures, server/mobile code, or operations, the behavior
needs the correct durable home.

The common durable surface for most IDR-carried behavior extraction is
`docs/specifications/platform/`, with `contracts/` remaining authoritative for
machine/process-boundary schemas and protocols. Operational binding and
retention/security choices should route through `docs/operations/`.

This pass does not delete or rewrite IDRs. A later old-doc audit may supersede,
correct, or retire old wording where it conflicts with CDL/exploration or with
the selected durable home. The right next move is a small set of routed
platform-spec/contract/ops extraction tasks, starting with
configuration/expression/package and authority/security behavior before broader
operating-framework or product/API work depends on IDR text as normative.
