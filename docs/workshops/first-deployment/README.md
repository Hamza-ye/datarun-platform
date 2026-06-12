# First Deployment Workshop

Status: active product/spec/evidence workshop surface

Date opened: 2026-06-12

Authority: none. This folder coordinates first-deployment product, UX,
evidence, role, and task-packet planning. It does not change CDL, contracts,
BAR, NW, schemas, APIs, runtime behavior, or implementation authority.

## Entry Points

| File | Role |
|---|---|
| [readiness-checklist.md](readiness-checklist.md) | Pre-workshop readiness, source links, role input checks, and go/no-go conditions. |
| [control.md](control.md) | Active workshop control surface, stage outputs, lane register, decision log, and next action. |
| [role-packets.md](role-packets.md) | Reusable role prompts and boundaries for workshop participants. |
| [stage-3-ux.md](stage-3-ux.md) | UX and vocabulary bridge output. |
| [stage-4-software-architecture.md](stage-4-software-architecture.md) | System boundary and successor route output. |
| [stage-5-mobile.md](stage-5-mobile.md) | Mobile/offline feasibility output. |
| [stage-6-pressure-test.md](stage-6-pressure-test.md) | Reality/evidence pressure-test output. |
| [stage-7-delivery-plan.md](stage-7-delivery-plan.md) | Milestone, dependency, owner, and change-control output. |
| [stage-8-task-packet-backlog.md](stage-8-task-packet-backlog.md) | FD-PKT backlog and packet gate. |
| [task-packets/](task-packets/) | Prepared FD-PKT packets and decision records. |

## Current Packet State

| Packet | State | File |
|---|---|---|
| FD-PKT-001 | Complete as Option C: Candidate 1 product/spec may proceed while S06 discovery runs in parallel before implementation dispatch. | [fd-pkt-001-s06-timing-decision-record.md](task-packets/fd-pkt-001-s06-timing-decision-record.md) |
| FD-PKT-002 | Drafted: Candidate 1 product/spec and UX validation with an explicit S06 dependency marker. | [fd-pkt-002-candidate-1-product-spec-ux-validation.md](task-packets/fd-pkt-002-candidate-1-product-spec-ux-validation.md) |
| FD-PKT-101 | Drafted: S06/entity lifecycle discovery and BAR-105 successor-decision seed. | [fd-pkt-101-s06-entity-lifecycle-discovery.md](task-packets/fd-pkt-101-s06-entity-lifecycle-discovery.md) |

## Current March-Forward Rule

Candidate 1 implementation remains blocked until bounded task packets are
written and gated. The immediate planning path is:

1. Review/accept the FD-PKT-002 product/spec and UX validation packet.
2. Review/accept FD-PKT-101 for S06/entity lifecycle discovery before
   implementation dispatch.
3. Draft FD-PKT-003 evidence planning after FD-PKT-002 acceptance language is
   stable and FD-PKT-101 evidence needs are visible.

Do not collapse Candidate 1 with S06 lifecycle, production auth/admin/mobile
login, retention/security, reporting/import-export, conflict automation, custom
scope, or ops readiness.

## Active Router

New agent sessions should use
[docs/agent-working-surface/first-deployment-task-packet-router.md](../../agent-working-surface/first-deployment-task-packet-router.md)
for compact routing into this folder.
