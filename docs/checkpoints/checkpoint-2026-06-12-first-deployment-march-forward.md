# Checkpoint - 2026-06-12 - First-Deployment March-Forward

## 1. Bearing

- **Phase / altitude:** Post-Phase-4 stabilization; first-deployment workshop march-forward planning after the FD-PKT-001 packet landed.
- **Momentum:** Advancing. The workshop/user-fit surface now has a concrete timing decision instead of a diffuse architecture concern.
- **Last milestone:** Commit `2c4f8ce` added the FD-PKT-001 S06 timing decision packet and updated related first-deployment workshop and user-fit surfaces.
- **Current next action:** Run **FD-PKT-001 - S06 Timing Decision** as the next workshop action.
- **Product posture:** The steward protocol is a product clarity guardrail. It must keep product-needed lanes visible, not protect the protocol by making deferred product pressure disappear.

## 2. Standing Snapshot

- **Accepted baseline still in force:** BAR-001 through BAR-015 and BAR-104 remain the active accepted architecture baseline.
- **Runtime probe set still routed through the working surface:** NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, NW-033/S26, and NW-042/S22 remain the implementation-facing probe pool where routed.
- **First-deployment surface now has a timing gate:** FD-PKT-001 distinguishes Candidate 1 as S01-compatible from the broader S06/entity-lifecycle lane.
- **Candidate 1 is still viable without S06 promotion:** Candidate 1 may proceed as basic operational capture with optional subject-linked capture, using unpromoted missing-known-thing/candidate language and without making candidate subjects canonical lifecycle truth.
- **S06 remains product-needed work, not rejected work:** S06/entity lifecycle pressure stays visible through BAR-105/NW-021/NW-036 successor routing if FD-PKT-001 chooses promotion or parallel discovery.
- **Known deferred lanes remain separate:** Reporting/export, retention, scope expansion, production auth/admin/mobile login, conflict review queues, batch behavior, and automated conflict resolution are not silently bundled into Candidate 1.

## 3. Recent Movement

- `2c4f8ce` - Added the FD-PKT-001 S06 timing decision document and updated first-deployment workshop, user-fit packet, review/playbook, and gap-routing context.
- `56e373f` - Clarified in agent onboarding that stewardship protects product clarity rather than suppressing product pressure.
- `5390d6b` - Added workshop outputs for stages 6, 7, and 8, creating the immediate first-deployment planning context.
- `6ed92e7` - Clarified operational labels as scenario shortcuts, reducing the risk that persona labels become architecture categories.
- `97e671d` and `758bd79` - Refined user-fit packets and synthesis that feed Candidate 1 and adjacent deployment candidates.
- `b622a4d` / `f0c67e7` - Established the decision-anchor working-surface playbook and updated NW-057, keeping gap routing explicit.

## 4. Architecture Guardrails

- **Authority order:** CDL decisions, contracts, decision-anchor layer, BAR/NW records, operational UX companion, and scenario/user-fit evidence remain the source order.
- **No envelope changes:** Do not add event envelope fields or `type` values through FD-PKT work.
- **No durable workflow state:** Candidate 1 cannot introduce workflow state machines or lifecycle truth outside accepted event/projection authority.
- **Accept-and-flag remains the posture:** Structurally valid anomalies are accepted and flagged unless an accepted decision says otherwise.
- **Assignment-derived authority remains intact:** Subject access and authority remain event/projection-derived; IdP claims are identity inputs, not assignment authority.
- **Mobile stays advisory:** Mobile may guide capture and sync behavior, but it does not become the authority for unresolved identity or lifecycle decisions.
- **No S06 implementation without promotion:** S06/entity lifecycle implementation requires BAR-105/S06 successor authority before implementation planning.
- **No production-readiness overclaim:** Operational demos and first-deployment packets cannot imply turnkey production readiness without explicit ops, auth, retention, reporting, and conflict-review evidence.

## 5. Risk Pulse

- **Protocol overcorrecting into product suppression - elevated:** FD-PKT work must not frame S06 only as "blocked." It must state the product need, the timing choices, and the cost of each choice.
- **Candidate 1 absorbing S06 lifecycle - active:** Candidate 1 can capture missing-known-thing evidence, but must not make candidate subjects, lifecycle states, or canonical enrollment claims.
- **Deferred S06 discovered too late - active:** If the product need is material for first deployment, FD-PKT-001 should choose promotion or parallel discovery rather than postponing the lane until implementation friction appears.
- **First-deployment overclaim - active:** Candidate language must stay scoped to what the current architecture supports; production auth, admin hardening, reporting/export, and retention remain separate readiness lanes.
- **Persona/operational-label drift - reduced:** Recent review updates keep labels as product-facing shortcuts rather than architecture categories.
- **S01/S06 boundary confusion - reduced:** The new gap-routing and FD-PKT-001 packet make Candidate 1 S01-compatible while keeping S06 visible.

## 6. Scenario And Product Pressure

- **S00/S01/S19:** These remain the natural path for Candidate 1: simple operational capture, actor/role/assignment clarity, and advisory sync behavior.
- **Candidate 1:** Should be tested as a first-deployment fit packet, not as a lifecycle-platform expansion. Its success criteria should include user comprehension, safe capture, and honest escalation when the current baseline cannot decide identity/lifecycle truth.
- **S06:** This is a real product-pressure lane. It should be timed explicitly by FD-PKT-001 rather than hidden behind protocol language.
- **S22/S26:** Product pressure around operational continuity and reporting inputs may inform later packets, but neither should smuggle lifecycle triggers or reporting/export commitments into Candidate 1.
- **Operations/admin/auth/retention/reporting:** These are first-deployment-adjacent readiness needs. They should stay visible as separate packets or successor decisions, not be collapsed into the S06 timing decision.

## 7. Verification Ledger

- **Checkpoint basis:** Local docs review plus current commit anchor `2c4f8ce`.
- **Implementation tests:** Not run; this is a docs-only checkpoint with no server/mobile code changes.
- **Required lightweight verification after writing:** `git diff --check`, targeted trailing-whitespace scan for this checkpoint, and `git status --short`.
- **Drift watch:** If FD-PKT-001, the standing review/playbook, and gap-routing disagree about S01-vs-S06 timing, stop and reconcile before drafting FD-PKT-002.

## 8. March Orders

1. Run **FD-PKT-001 - S06 Timing Decision** next. Record whether the workshop chooses Option A (Candidate 1 first, S06 near-future), Option B (promote S06 before Candidate 1 implementation planning), or Option C (parallel S06 discovery before implementation gate).
2. Only after FD-PKT-001 is recorded, draft **FD-PKT-002 - Candidate 1 Product/Spec/UX Validation** against the chosen timing boundary. Include copy, tests, and acceptance language that make candidate-subject limits visible to product and field users.
3. Build the Candidate 1 evidence plan around real user comprehension and operational safety: what users believe they captured, what the system can truthfully decide, and when it must flag/escalate instead.
4. If FD-PKT-001 chooses Option B or C, open the BAR-105/S06 successor path as a decision or discovery packet before implementation planning. Keep it architecture-facing, but name the product pressure plainly.
5. Keep production-readiness lanes separated and visible: auth/admin/mobile login, retention, reporting/export, conflict review operations, and deployment support each need their own evidence path before production claims.
