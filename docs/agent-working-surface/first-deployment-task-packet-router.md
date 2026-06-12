# First-Deployment Task-Packet Router

Status: active product/workshop routing surface

Date: 2026-06-13

Authority: none. This router does not change CDL, contracts, BAR, NW,
schemas, APIs, code, runtime behavior, or implementation authority. It exists
to make the first-deployment march-forward path visible to new agent sessions.

## Surface Decision

Do not move the Stage 8 Task-Packet Backlog into the Baseline Acceptance
Register.

Reason:

- BAR records accepted implementation standing and future-decision baseline
  status.
- The Stage 8 backlog is workshop dispatch control: product/spec/evidence
  packets, role ownership, gates, and stop conditions.
- Moving it into BAR would make product-planning packets look like accepted
  platform capability or implementation backlog truth.

Keep it referenced from the active working surface instead. New sessions should
enter through `AGENTS.md`, `docs/status.md` Current Routing, and this file when
the task is first-deployment planning or FD-PKT work.

## Source Order For First-Deployment Work

Use this order:

1. `AGENTS.md` and `docs/status.md` Current Routing.
2. `docs/agent-working-surface/README.md`.
3. `docs/agent-working-surface/decision-anchor-layer/README.md` when the work
   is architecture-sensitive.
4. `docs/agent-working-surface/baseline-acceptance-register.md` for accepted,
   deferred, and future-decision standing.
5. `docs/agent-working-surface/platform-next-work-backlog.md` for NW evidence
   and successor routes.
6. `docs/agent-working-surface/operational-ux-layering-companion.md` for
   product/UX vocabulary guardrails.
7. This router for first-deployment packet sequencing.
8. The cited workshop packet only:
   `docs/workshops/first-deployment/stage-8-task-packet-backlog.md` or
   the exact FD-PKT file named by the task.
9. Scenario/user-fit packets only as product/problem evidence.

Do not infer current truth from workshop chronology. If this router disagrees
with status, BAR, NW, the decision-anchor layer, contracts, or code evidence,
stop and reconcile the drift.

## Current Packet State

| Packet | Current state | Next use |
|---|---|---|
| FD-PKT-001 | Recorded 2026-06-13 as Option C: parallel S06 discovery before Candidate 1 implementation gate. | Use [task-packets/fd-pkt-001-s06-timing-decision-record.md](../workshops/first-deployment/task-packets/fd-pkt-001-s06-timing-decision-record.md). |
| FD-PKT-002 | Drafted for Candidate 1 product/spec and UX validation. | Review [fd-pkt-002-candidate-1-product-spec-ux-validation.md](../workshops/first-deployment/task-packets/fd-pkt-002-candidate-1-product-spec-ux-validation.md) before evidence planning. |
| FD-PKT-003 | Drafted for Candidate 1 evidence planning. | Review [fd-pkt-003-candidate-1-evidence-plan.md](../workshops/first-deployment/task-packets/fd-pkt-003-candidate-1-evidence-plan.md) before FD-PKT-004/005 dispatch. |
| FD-PKT-004 | Drafted for Candidate 1 mobile/offline validation. | Review [fd-pkt-004-candidate-1-mobile-offline-validation.md](../workshops/first-deployment/task-packets/fd-pkt-004-candidate-1-mobile-offline-validation.md) before mobile implementation dispatch. |
| FD-PKT-005 | Drafted for Candidate 1 view-model/contract assessment. | Review [fd-pkt-005-candidate-1-view-model-contract-assessment.md](../workshops/first-deployment/task-packets/fd-pkt-005-candidate-1-view-model-contract-assessment.md) before implementation dispatch. |
| FD-PKT-006/007 | Blocked for production/constrained-deployment claims. | Ops runbook and staging rehearsal planning only. |
| FD-PKT-101 | Drafted as S06 discovery/decision follow-up under BAR-105. | Review [fd-pkt-101-s06-entity-lifecycle-discovery.md](../workshops/first-deployment/task-packets/fd-pkt-101-s06-entity-lifecycle-discovery.md) before Candidate 1 implementation dispatch. |

## Current Gate State

Owner-role gate reviews are recorded at
`docs/workshops/first-deployment/gate-reviews/`.

Project Shepherd consolidation:
[`candidate-1-gate-consolidation.md`](../workshops/first-deployment/gate-reviews/candidate-1-gate-consolidation.md).

Consolidated status: `CONDITIONAL PACKET-DRAFT UNBLOCK`.

This status does not authorize implementation. It permits the next
non-implementation prerequisite: draft `FD-PKT-101A` to record minimal S06
honesty evidence and a Candidate 1 S06 disposition.

## FD-PKT-001 Role Flow

Assigned role: **Product Manager + steward accountability**.

Product Manager owns:

- the first-deployment product timing choice;
- whether the day-one promise needs maintained known things or only linked
  capture;
- which lifecycle words are day-one requirements versus later product polish;
- evidence needed from users, SMEs, registry artifacts, duplicate examples,
  merge/split policy, and source-of-known-set examples;
- the downstream product/spec dependency marker for FD-PKT-002.

Steward accountability owns:

- CDL/contracts/BAR/NW source order;
- Candidate 1 S01 boundary;
- S06/BAR-105 successor routing if lifecycle is promoted or required before
  implementation;
- forbidden-work and stop conditions;
- keeping S06 visible as product-needed work, not hidden behind protocol
  language.

Workshop Lead may record and integrate the result, but does not own the product
timing decision or architecture authority.

Reality Checker and Test Results Analyzer may later classify evidence and
claims, but they must not convert missing evidence into product silence.

## Implementation Dispatch Rule

Implementation remains blocked until an individual packet is written and
reviewed against the Stage 8 packet gate.

No implementation agent should receive the whole workshop record as a build
instruction. Each implementation packet must state:

- one lane;
- assigned role;
- authority and source order;
- exact files/contracts allowed;
- accepted constructs reused;
- excluded successor lanes;
- forbidden work;
- expected automated tests or validation evidence;
- manual walkthroughs where applicable;
- stop/report conditions;
- commit boundary.

## Current March-Forward Path

1. Draft and review `FD-PKT-101A` for minimal S06 honesty evidence and the
   Candidate 1 S06 disposition.
2. If `FD-PKT-101A` excludes, splits, or defers S06 in a way that keeps
   Candidate 1 honest, draft one bounded Candidate 1 implementation packet.
3. The implementation packet must name exact files/contracts/tests, accepted
   constructs reused, manual evidence, excluded successor lanes, forbidden
   work, stop conditions, and commit boundary.
4. Keep Candidate 1 implementation blocked until that implementation packet is
   reviewed and explicitly dispatched.
5. Keep production-readiness lanes separate: admin/mobile auth,
   retention/security, reporting/import-export, conflict review queues,
   subject/query/custom scope, and ops readiness.

## Stop Conditions

Stop and report if:

- Candidate 1 copy or UI makes candidate subjects canonical lifecycle truth;
- S06 is hidden as vague later work without owner, evidence, route, and
  decision point;
- lifecycle states, discovered-unit lifecycle, merge/split UX, or registry
  stewardship are implemented without BAR-105/S06 successor routing;
- a task packet combines Candidate 1 implementation with auth, reporting,
  retention/security, S06 lifecycle, conflict automation, or custom scope;
- persona labels become identity categories, authority primitives, config
  namespaces, product-area boundaries, or implementation service boundaries;
- production-readiness wording appears without ops/security/release evidence.
