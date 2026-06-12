# FD-PKT-001 - S06 Timing Decision For Candidate 1

Status: prepared workshop packet

Date: 2026-06-12

Assigned roles: Product Manager + steward accountability

Authority: none. This packet prepares a product/platform timing decision. It
does not authorize implementation, change CDL, BAR, NW, contracts, schemas,
APIs, event vocabulary, runtime behavior, or product scope by itself.

## 1. Purpose

Decide how S06/entity lifecycle is sequenced relative to Candidate 1 before
Candidate 1 product/spec freeze.

This is a timing and scope decision, not a protocol defense. The steward
accountability protects source order, negative boundaries, and stop conditions.
It must also keep real product pressure visible with route, owner, evidence
need, and decision point.

## 2. Starting Premise

Candidate 1 is S01-compatible:

- standalone structured capture remains the simplicity baseline;
- optional subject-linked capture may be included over existing `subject_ref`
  and subject-history support;
- missing-known-thing capture may keep work moving as an unpromoted
  candidate/capture artifact;
- Candidate 1 must not silently create canonical registry lifecycle state,
  discovered-unit lifecycle, active/inactive/retired truth, or merge/split UX.

S06/entity lifecycle remains a product-needed lane:

- maintained known things;
- discovered-unit stewardship;
- operational lifecycle vocabulary such as active, inactive, retired, closed,
  moved, verification-needed, or duplicate-candidate;
- registry update/review policy;
- merge/split UX and operating policy;
- lifecycle evidence and acceptance tests.

The architecture exploration says entity lifecycle is coherent but incomplete
at the platform-spec/product-policy layer. It should not be collapsed into one
large primitive. The pressure is split across subject identity lifecycle,
projection/read models, conflict/flag handling, assignment/access, shape/config
lifecycle, workflow/pattern state, access/sync, and reporting/freshness lanes.

## 3. Required Inputs

Read only the cited sections needed to answer the timing decision:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/baseline-acceptance-register.md` BAR-105 row
- `docs/agent-working-surface/platform-next-work-backlog.md` NW-021 and
  NW-036 rows
- `docs/scenarios/scenario-user-fit-packets/README.md`
- `docs/scenarios/scenario-user-fit-packets/s01-user-fit-entity-linked-capture.md`
- `docs/scenarios/scenario-user-fit-packets/s06-user-fit-maintaining-a-known-set-of-things.md`
- `docs/scenarios/scenario-user-fit-packets/scenario-user-fit-synthesis-across-s00-s01-s06-s06b-access-control-S19.md`
- `docs/scenarios/scenario-user-fit-packets/foundational-product-fit-readiness-and-validation-matrix.md`
- `docs/reviews/scenario-user-fit-packets-standing-review-and-playbook.md`
- `docs/reviews/first-deployment-workshop-control.md`
- `docs/reviews/first-deployment-workshop-stage-6-pressure-test.md`
- `docs/reviews/first-deployment-workshop-stage-7-delivery-plan.md`
- `docs/reviews/first-deployment-workshop-stage-8-task-packet-backlog.md`
- `.review/untracked-user-notes/exploration/000-consolidated-current-architecture-position-entity-lifecycle-and-related-lanes.md`

## 4. Decision Questions

Answer these in order:

1. Does the first deployment promise require maintaining known things over
   time, or only capturing records that may link to known things?
2. Is the Candidate 1 S01-compatible path enough: optional subject link plus
   unpromoted missing-known-thing capture for review?
3. Which lifecycle words are day-one product requirements rather than later
   registry polish: active, inactive, retired, closed, moved, verification,
   duplicate candidate, merge, split?
4. Where does the initial known set come from: operator import, setup owner
   entry, field discovery, external registry, or mixed process?
5. What operational risk is created if S06 waits until after Candidate 1
   product/spec freeze?
6. What product, architecture, test, and delivery risk is created if S06 moves
   before Candidate 1 implementation planning?
7. Which evidence is missing: SME validation, registry artifacts, lifecycle
   examples, duplicate review examples, merge/split policy, or tests?

## 5. Decision Options

| Option | Use when | Required follow-up |
|---|---|---|
| A. Candidate 1 first; S06 near-future milestone | First deployment can operate with S01-compatible linked capture, known-list lookup, and unpromoted candidate/missing-subject handling. | FD-PKT-002 may proceed, but it must include UX copy/tests preventing lifecycle overclaim. FD-PKT-101 remains visible as a named near-future S06/BAR-105 milestone with owner, evidence plan, and decision point. |
| B. Promote S06 before Candidate 1 implementation planning | First deployment needs maintained known things, lifecycle states, discovered-unit stewardship, or merge/split UX as part of the day-one product promise. | Start BAR-105/S06 successor decision before Candidate 1 implementation packets. No lifecycle implementation until the successor route defines scope, authority, contracts/code impact, tests, and stop conditions. |
| C. Run S06 discovery in parallel before implementation gate | Product dependency is plausible but unproven; Candidate 1 spec can proceed, but implementation freeze depends on early S06 evidence. | FD-PKT-002 can draft with a dependency marker. Product/SME discovery must answer the S06 questions before Candidate 1 implementation packet dispatch. |

Recommended starting stance for the workshop: treat Candidate 1 as
S01-compatible and keep it first for product/spec drafting unless Product
Manager evidence shows maintained known things are required for day one. That
does not demote S06; it keeps S06 visible as a timed decision lane instead of a
vague follow-up.

## 6. Required Output

Produce a short decision record with:

- chosen option: A, B, or C;
- product reason;
- Candidate 1 S01 boundary;
- S06 product need and milestone placement;
- architecture guardrails relied on;
- evidence still missing;
- tests or validation artifacts required;
- packet impacts for FD-PKT-002 through FD-PKT-005 and FD-PKT-101;
- stop/report conditions.

## 7. Stop Conditions

Stop and report if:

- Candidate 1 copy or UI state makes a candidate subject into canonical
  lifecycle truth;
- S06 is hidden under vague "later" wording without owner, milestone,
  evidence need, and decision point;
- lifecycle states, discovered-unit lifecycle, merge/split UX, or registry
  stewardship are implemented without BAR-105/S06 successor routing;
- Product Manager pressure is dismissed only because current baseline is
  deferred;
- steward/protocol language replaces the product timing decision instead of
  informing it.

## 8. Done Definition

FD-PKT-001 is done when the workshop has a recorded S06 timing decision that:

- preserves Candidate 1 as S01-compatible if it remains first;
- names the exact S06 dependency if S06 must move earlier;
- keeps S06 visible in the delivery plan either way;
- states the evidence and route needed before implementation;
- lets FD-PKT-002 proceed only with the chosen timing boundary.
