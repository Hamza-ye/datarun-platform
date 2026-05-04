# Ship-4 — Subject Timeline and State Progression (S08)

> **FORBIDDEN FOR PLATFORM-SPEC ATOMIZATION.**
> This is a legacy Ship draft. Do not use it as authority for the current
> platform-spec atomization path. Start at
> [`../platform-spec-kernels/professional-baseline/README.md`](../platform-spec-kernels/professional-baseline/README.md).
> The draft below is retained only for historical Ship/convergence context and
> must not override this warning.
>
> **Status**: spec DRAFT for user lock.
> **Opened**: 2026-05-02.
> **Tag target**: `ship-4`.
> **Parent**: [`ship-3.md`](ship-3.md) (tag `ship-3` does not move).
> **Code gate**: no code begins until this spec is locked by the user.
>
> This file re-authors the prior scratch `ship-4.md` from
> [`ship-4-section-6-draft.md`](ship-4-section-6-draft.md), with the accepted
> decisions applied: Ship-4 is S08-only; [FP-014](../flagged-positions.md#fp-014--scope-eval-pull-class-temporal-anchor-disambiguation)
> is a strategy-level fill-in under ADR-003, not ADR-003-R; and Ship-4 uses
> platform-agnostic language for the S08 surface.

## Vocabulary Rule

The platform concepts for Ship-4 are:

- **subject timeline**: the ordered event history for the subject under an
  ongoing situation;
- **ongoing situation**: the S08 operational instance that remains active
  across multiple interactions;
- **responsibility binding**: the current actor-to-subject operational
  responsibility used for subject-timeline reads;
- **state progression**: projection-derived state over the subject timeline.

Scenario prose may still say "case" because [S08](../scenarios/08-case-management.md)
is named that way. That wording is illustrative. Ship-4 does **not** freeze
`case management`, `case/v1`, `/admin/cases`, `case_state`, or `case_id` as
platform labels in this spec.

## 1. Scenario Delivered

Ship-4 delivers [S08](../scenarios/08-case-management.md) only: following an
ongoing situation over time until it resolves, including multiple interactions,
responsibility handoff, visibility into active/resolved state, and history of
who did what when.

The delivered slice is the state-progression half of S08:

| Surface | Ship-4 commitment |
|---|---|
| Ongoing situation capture | A deployer-configured event shape records the situation lifecycle as ordinary `type=capture` envelopes. State lives in payload, not in envelope `type`. |
| Subject timeline | Reads project the full ongoing-situation timeline for a subject by replaying events per request. No projection cache is introduced. |
| Responsibility handoff | Runtime assignment changes transfer responsibility from one actor to another. The successor can read the full subject timeline, including predecessor-authored events. |
| "Waiting too long" | A read-only projection query over current state and last activity time. No flag or trigger is emitted in Ship-4. |
| Offline constraint | Offline-late captures and assignment-end events are tested against event-time authority where the push path is involved. |

### Scenarios deliberately not delivered

- [S04](../scenarios/04-supervisor-review.md) and [S11](../scenarios/11-multi-step-approval.md):
  review, approval, corrections, and `type=review` first emission move to
  Ship-5.
- [S07](../scenarios/07-resource-distribution.md), [S09](../scenarios/09-campaign-execution.md),
  [S14](../scenarios/14-supply-receipt.md), and S13 cross-flow linking: Ship-6.
- [S02](../scenarios/02-reactive-followups.md), [S10](../scenarios/10-deadline-checks.md),
  and [S12](../scenarios/12-trigger-driven-actions.md): Ship-7 reactive layer.
- Mobile UI for subject-timeline workflows: deferred unless a future mobile
  sub-Ship makes it load-bearing.

### Delivery surface

Ship-4 uses scripted multi-actor HTTP walkthroughs against the real server,
following Ship-1 through Ship-3 precedent. Distinct bearer tokens stand in for
distinct actors. No Flutter/mobile client is exercised in this Ship.

### Composite coverage

| Composite | Ship-4 coverage |
|---|---|
| [S20 — CHV field operations](../scenarios/20-chv-field-operations.md) | Exercises bullet 4 (continuous activities / follow-ups over time) and bullet 5 (history of what was done, when, and by whom) through the subject timeline. Bullets 1-3 carry forward. |
| [S21 — CHV supervisor operations](../scenarios/21-chv-supervisor-operations.md) | Partial only: workload visibility can be read from projected active/resolved timelines. Review, approval, and corrective oversight carry to Ship-5. |
| [S05 — supervision / audit visits](../scenarios/05-supervision-audit-visits.md) | Not delivered. S05 is the natural future first exercise for FP-014's historical/audit pull class. |

## 2. ADRs Exercised

Ship-4 first-exercises [ADR-005](../adrs/adr-005-state-progression.md)'s
state-progression surface and first-exercises the runtime half of
[ADR-003 §S2](../adrs/adr-003-authorization-sync.md#s2).

| ADR §S | Ship-4 expectation |
|---|---|
| [ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns) | State is derived from subject-timeline events by projection. The current state is never stored on the envelope and is not maintained in a cache. |
| [ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry) | A platform-fixed state-progression pattern is loaded from a bundled definition and used by the projection engine. Deployer authoring remains deferred under FP-012. |
| [ADR-005 §S6 R1](../adrs/adr-005-state-progression.md#s6-pattern-composition-rules) | One subject-level pattern is bound to the S08 activity. |
| [ADR-005 §S6 R5](../adrs/adr-005-state-progression.md#s6-pattern-composition-rules) | Shape-to-pattern uniqueness is honored for the single Ship-4 binding. Deploy-time validation remains partial until FP-012 closes. |
| [ADR-003 §S2](../adrs/adr-003-authorization-sync.md#s2) | Runtime reassignment changes the next live-sync payload and updates responsibility binding for subject-timeline reads. |
| [ADR-003 §S3](../adrs/adr-003-authorization-sync.md#s3) | Push-path authority remains event-time anchored. Ship-4 must preserve the existing `capture.timestamp()` discipline. |
| [ADR-003 §S4](../adrs/adr-003-authorization-sync.md#s4-alias-respects-original-scope) | Alias-respects-original-scope remains inherited. Ship-4 adds no authority context field. |
| [ADR-007 §S1](../adrs/adr-007-envelope-type-closure.md#s1-the-envelope-type-vocabulary-is-closed-at-six-values) | No new envelope type. S08 lifecycle events are `type=capture`; scenario state is represented by shape + pattern. |
| [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) | The pattern mechanism is platform-fixed. The deployer-configured S08 shape/pattern instance is bundled for now under the existing FP-012 expedient. |

### ADRs explicitly not exercised

- ADR-005 §S1/§S2: `transition_violation` flag emission and flagged-event
  exclusion from state evaluation are not exercised because Ship-4 emits no
  transition flags.
- ADR-005 §S8/§S9: `context.*` expressions and auto-resolution remain Ship-7.
- ADR-007 `type=review` first emission remains Ship-5.
- ADR-004 config publication atomicity remains outside the slice; Ship-4 only
  extends the current bundled-resource expedient.

## 3. Structural Risks

| Risk | Position at risk | Ship-4 control |
|---|---|---|
| R1 — cached state progression | ADR-005 §S4 + ADR-001 §S2 | Subject-timeline state must be rebuilt from events per request. No `*_state` cache table or equivalent authoritative cache. |
| R2 — assignment-end events ignored | ADR-003 §S2/§S3 + P04 responsibility binding | FP-018 is in scope. Runtime assignment-end events must affect scope reconstruction. |
| R3 — scenario word becomes platform primitive | ADR-007 §S1 + F-A1/F-A2 | No new envelope type and no frozen "case" API/schema/DB vocabulary unless a later implementation plan explicitly marks it as scenario wording. |
| R4 — pull-class conflation | ADR-003 §S2 | FP-014 is in scope. Live-sync, historical/audit, and subject-timeline pulls keep distinct anchors. |
| R5 — responsibility handoff loses history | S08 + ADR-003 §S2 | The successor responsibility holder must read the full subject timeline across reassignment. |
| R6 — transition flags overclaimed | ADR-005 §S1/§S2 | Ship-4 may evaluate state transitions for projection, but it does not claim flag emission or flag-aware exclusion. |

## 4. Concept and Ledger Expectations

The retro should update the concept ledger using platform-agnostic rows. Names
below are conceptual, not mandated code identifiers:

| Concept | Classification | Expected status at retro | Cites |
|---|---|---|---|
| `ongoing_situation` | CONFIG instance shape | STABLE if implemented and drift-gated | ADR-009 §S1, ADR-004 §S10 |
| `subject_timeline` | DERIVED projection | STABLE | ADR-005 §S4, FP-014 |
| `responsibility_binding` | DERIVED projection | STABLE or retro-asserted | ADR-003 §S2/§S3, P04 |
| `state_progression_pattern` | PRIMITIVE mechanism | retro-asserted | ADR-005 §S5 |
| `assignment_end_consumption` | DERIVED scope reconstruction behavior | STABLE | ADR-003 §S2/§S3, FP-018 |

If implementation chooses concrete names, the retro must explain whether they
are platform concepts or scenario/example labels.

## 5. Flagged Positions

| FP | Ship-4 disposition |
|---|---|
| [FP-014](../flagged-positions.md#fp-014--scope-eval-pull-class-temporal-anchor-disambiguation) | **In scope.** Legacy classification notes treated this as a strategy-level fill-in, not ADR-003-R. Ship-4 must implement/prove the subject-timeline pull class while preserving live-sync request-time behavior. Historical/audit remains decided-unexercised until an audit endpoint exists. |
| [FP-018](../flagged-positions.md#fp-018--assignment_endedv1-validated-but-never-consumed-in-scope-reconstruction) | **In scope.** Ship-4 must consume `assignment_ended/v1` in scope reconstruction and pass the offline-late end-event gate. |
| [FP-005](../flagged-positions.md#fp-005--corrections-surface-is-unassigned-in-the-5-ship-map) | Carries to Ship-5 with review/corrections. |
| [FP-006](../flagged-positions.md#fp-006--s7s8-attribution-semantics-in-the-corrective-split-case) | Carries forward. Ship-4 does not exercise corrective split against an active ongoing situation. |
| [FP-008](../flagged-positions.md#fp-008--conflict_detected-payload-lacks-root_cause-trace-metadata) | Carries to the first Ship that emits stale/transition flags. Ship-4 emits no such flag. |
| [FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) | Expedient extended one Ship: bundled shape/pattern resource. Deployer authoring remains open. |
| [FP-016](../flagged-positions.md#fp-016--fixture-event-schema-regression-check-drift-gate-scope-expansion) | Optional pre-pay only: a pattern fixture replay is useful but not required to close FP-016. |
| [FP-017](../flagged-positions.md#fp-017--role_stale-detector-wiring-successor-to-fp-001) | Carries to Ship-5. FP-018 closure removes its recorded prerequisite debt. |

## 6. Slice

### 6.1 Locked sub-decisions for Ship-4

1. **Scope is S08-only.** No S04/S11 review, approval, or corrections work.
2. **Subject binding uses the existing `subject_ref` contract.** The ongoing
   situation is about a subject. Ship-4 does not introduce `subject_ref.type =
   "process"` or a new envelope reference field.
3. **Lifecycle events are captures.** No `case_opened`, `case_resolved`, or
   `status_changed` envelope type is added.
4. **State progression is projection-derived.** The current state is computed
   from events using a bundled pattern definition.
5. **Pattern format is a bundled JSON resource.** It is a platform-fixed
   mechanism plus first bundled instance. The deployer-authoring surface stays
   under FP-012.
6. **Runtime responsibility handoff is append-only.** Ending an assignment uses
   `assignment_ended/v1`; Ship-4 must not mutate prior `assignment_created/v1`
   events or rely on pre-filled `valid_to` as the production handoff mechanism.
7. **FP-014 routing is by pull class.** Live-sync remains request-time anchored;
   subject-timeline reads use subject/responsibility anchoring; historical/audit
   is decided but unimplemented.
8. **No transition/staleness flags.** "Waiting too long" is a query over the
   projection. `transition_violation` and stale-state flagging wait for later
   Ships.
9. **Concrete API, DB, and shape names are not locked here.** The implementation
   plan must use agnostic names or explicitly mark scenario/example vocabulary.

### 6.2 Minimum topology

Ship-4 walkthroughs require:

- at least 3 actors: opener, successor/progressor, coordinator;
- at least 3 villages or geographic scopes;
- at least 1 cross-village runtime reassignment;
- at least 1 responsibility handoff where the successor reads the full subject
  timeline after reassignment;
- at least 1 offline-late `assignment_ended/v1` event proving FP-018 gate 3.

### 6.3 Pattern skeleton

The bundled pattern must be sufficient to prove ADR-005 §S4/§S5/§S6 R1/R5:

- states: `open`, `progressed`, `resolved` or equivalent agnostic state names;
- initial state: open/equivalent;
- terminal state: resolved/equivalent;
- accepted transitions: open to progressed, progressed to progressed, open to
  resolved, progressed to resolved;
- state value read from the deployer-configured S08 payload field.

Names may be adjusted before implementation, but the behavioral skeleton above
is the Ship-4 contract.

### 6.4 Walkthroughs

Ship-4 continues the walkthrough numbering after Ship-3.

- **W-11 — Ongoing situation opened.** An in-scope actor opens an ongoing
  situation for an existing subject. The event is a `type=capture` envelope,
  validates against the S08 shape, persists, and projects to open/equivalent.
- **W-12 — State progresses by event replay.** A follow-up capture moves the
  projection to progressed/equivalent. The timeline read shows both events and
  proves per-request replay with no authoritative state cache.
- **W-13 — Runtime responsibility handoff.** A coordinator emits an append-only
  assignment end plus new assignment. Scope reconstruction before and after the
  end time returns the predecessor and successor respectively.
- **W-13b — Successor reads full subject timeline.** After handoff, the
  successor reads the subject timeline and sees predecessor-authored events
  from before reassignment. This first-exercises FP-014's subject-timeline pull
  class.
- **W-13c — Offline-late assignment end.** An `assignment_ended/v1` event arrives
  after its effective end time. Captures with timestamps after the end time and
  before arrival are checked against the ended scope and produce the expected
  scope violation. This closes FP-018 gate 3.
- **W-14 — Ongoing situation resolved.** A final capture moves projection to
  resolved/equivalent. Resolved and active listing behavior is derived from
  replay, not a stored state column.
- **W-15 — Out-of-scope opening rejected.** An actor attempts to open an ongoing
  situation for a subject outside active scope. The push path rejects it using
  event-time authority; no S08 event lands.
- **W-16 — Waiting-too-long query.** Two timelines are preloaded, one stale and
  one recent. The stale query returns only the stale timeline and emits no flag.

### 6.5 Out of scope

- Corrections, review, approval, and first `type=review` emission.
- Historical/audit endpoint implementation.
- `transition_violation`, stale-state, `role_stale`, or auto-resolution flags.
- Deployer authoring for shapes/patterns and atomic config publication.
- Multiple concurrent ongoing situations per subject unless implementation
  proves the S08 slice needs it and the user re-locks the spec.
- Subject deactivation while an ongoing situation is active.
- Mobile UI.

## 7. Parity Expectations

Ship-4 cannot tag with any `exercised-violated` parity row open.

| ADR | §S | Pre-Ship-4 | Post-Ship-4 target | Retro file |
|---|---|---|---|---|
| ADR-005 | §S1 | `decided-unexercised` | `decided-unexercised` | `docs/reviews/system/adr-005-parity.md` |
| ADR-005 | §S2 | `decided-unexercised` | `decided-unexercised` | `docs/reviews/system/adr-005-parity.md` |
| ADR-005 | §S3 | `exercised-met` partial | `exercised-met` partial | `docs/reviews/system/adr-005-parity.md` |
| ADR-005 | §S4 | `decided-unexercised` | `exercised-met` | `docs/reviews/system/adr-005-parity.md` |
| ADR-005 | §S5 | `decided-unexercised` | `exercised-met` | `docs/reviews/system/adr-005-parity.md` |
| ADR-005 | §S6 R1 | `decided-unexercised` | `exercised-met` | `docs/reviews/system/adr-005-parity.md` |
| ADR-005 | §S6 R2/R3/R4 | `decided-unexercised` | `decided-unexercised` | `docs/reviews/system/adr-005-parity.md` |
| ADR-005 | §S6 R5 | `decided-unexercised` | `exercised-met` partial | `docs/reviews/system/adr-005-parity.md` |
| ADR-005 | §S7 | `exercised-met` | `exercised-met` | `docs/reviews/system/adr-005-parity.md` |
| ADR-005 | §S8/§S9 | `decided-unexercised` | `decided-unexercised` | `docs/reviews/system/adr-005-parity.md` |
| ADR-003 | §S2 bootstrap | `exercised-met` | `exercised-met` | `docs/reviews/system/adr-003-parity.md` new at retro |
| ADR-003 | §S2 runtime | `decided-unexercised` | `exercised-met` | `docs/reviews/system/adr-003-parity.md` new at retro |
| ADR-003 | §S3 | `exercised-met` | `exercised-met` | `docs/reviews/system/adr-003-parity.md` new at retro |
| ADR-003 | §S4 | `exercised-met` | `exercised-met` | `docs/reviews/system/adr-003-parity.md` new at retro |
| ADR-003 | §S7 | `exercised-met` | `exercised-met` | `docs/reviews/system/adr-003-parity.md` new at retro |
| ADR-007 | §S1 `review` half | `decided-unexercised` | `decided-unexercised` | Ship-5 |
| ADR-009 | §S1 | `exercised-met` rule | `exercised-met` with new rows | existing ledger/retro |

## 8. Retro Criteria

The Ship-4 retro must show:

1. W-11 through W-16 pass against the real server via HTTP.
2. Prior Ship-1, Ship-1b, Ship-2, and Ship-3 walkthrough tests remain green.
3. Subject-timeline state is derived by per-request event replay; no
   authoritative state cache exists.
4. The state-progression pattern definition is loaded from the chosen bundled
   resource and used by projection.
5. Runtime `assignment_ended/v1` is consumed by scope reconstruction.
6. Offline-late assignment-end behavior passes FP-018 gate 3.
7. FP-014 subject-timeline pull behavior passes, while live-sync request-time
   behavior is preserved.
8. Push-path event-time authority remains unchanged.
9. No new envelope type is added.
10. Drift gate passes for any shape or pattern resources mirrored across
    `contracts/` and server resources.
11. ADR-005 parity rows are updated.
12. ADR-003 parity file is authored and includes runtime §S2 evidence.
13. A spec-conformance review has checked Ship-4 implementation against §6 and
    routed every `drift`, `missing`, `partial`, or `substrate_gap` finding.
14. The FP promotion sweep is complete: every carry-forward observation maps to
    an existing FP entry or a new FP is created before tag.
15. Concept ledger rows use the platform-agnostic vocabulary from this spec or
    explicitly justify any scenario-specific labels.
16. FP-014 and FP-018 resolution logs are updated if all gates pass.
17. `ship-4` tag is applied only after the above are true.

## 9. Commit Discipline

Ship-4 keeps the recent commit-history pattern: small, traceable commits whose
subject line identifies the work type, scope, and evidence target.
Implementation and tests carry scenario cites; ship-closeout docs carry ship
scope and evidence.

| Work kind | Commit subject form | Use in Ship-4 |
|---|---|---|
| Ship documentation | `docs(ship-4): ...` | Spec, retro, FP disposition, parity notes, walkthrough evidence. |
| ADR / architecture docs | `docs(adr): ...` or `docs(architecture): ...` | Only for architecture artifacts outside the Ship spec. FP-014 already uses `docs(architecture)` via the pull-class strategy doc. |
| Charter / ledger at normal ship closeout | `docs(ship-4): close-out retro — ..., ledger +N rows, charter regen` or separate `docs(ship-4): ... ledger ...` / `docs(ship-4): ... charter ...` commits | The common Ship pattern keeps ledger and charter changes under the Ship scope. Ship-1 and Ship-2 bundled them into closeout commits; Ship-3 split S06b ledger and charter commits but still used `docs(ship-3)`. |
| Exceptional recovery docs | `docs(charter): ...`, `docs(ledger): ...`, `docs(audit): ...` | Use only for cross-ship recovery or post-closeout correction work that is not cleanly part of a normal Ship commit cycle. The recent W2-B/W3/W4 charter/ledger subjects were exceptional closeout/recovery commits, not the default Ship pattern. |
| Implementation | `feat(ship-4): ... (S08[, FP-014/FP-018])` | Production code for state progression, subject-timeline reads, and assignment-end consumption. |
| Tests | `test(ship-4): ... (S08[, FP-014/FP-018])` | Walkthroughs and focused regression tests. |
| Skill/tooling | `chore(skill): ...` | Codex/orchestrator skill changes only. Keep separate from Ship-4 product work. |
| Generic docs | `docs: ...` | Reserve for cross-cutting documentation that does not belong to a narrower scope. Prefer scoped `docs(...)` when possible. |

Commit rules for this Ship:

1. Do not bundle docs, implementation, tests, and skill changes in one commit.
2. Every Ship-4 implementation/test commit cites `S08` and, where applicable,
   `FP-014`, `FP-018`, `ADR-003`, or `ADR-005` in the subject or body. This is
   the preserved part of the older `<type>(ship-N): S0X — ...` rule.
3. Use `docs(ship-4): ...` for spec/retro changes even when the content mentions
   ADRs or FPs. Use `docs(adr): ...` only when editing ADR files.
4. Put normal Ship-4 ledger and charter regeneration under `docs(ship-4)`,
   preferably at closeout with the retro. Do not use `docs(charter)` or
   `docs(ledger)` for routine Ship-4 closeout.
5. Keep closeout corrections traceable with the recent forms:
   `docs(ship-4): closeout Wn — ...`, `feat(ship-4): closeout G-n — ...`, or
   `test(ship-4): closeout G-n — ...` when the work comes from a named review
   wave or gap.
6. Before final tag, the retro must list the Ship-4 commits and map each to the
   walkthrough, FP, or ADR parity evidence it supports.

## 10. Hand-off to Ship-5

Ship-5 receives:

- S04 review and S11 approval;
- corrections and FP-005;
- first `type=review` emission and ADR-007 review-half parity;
- role-action enforcement and FP-017;
- any transition/staleness flagging that becomes load-bearing in the review
  slice.

## 11. Open Questions Before Build

No architectural question is currently blocking the spec. Before code begins,
the user must lock this draft or edit it. Implementation naming remains the main
engineering choice, and it must follow the vocabulary rule above.
