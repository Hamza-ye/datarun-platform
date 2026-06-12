# First Deployment Workshop Role Packets

Status: active human workshop packets

Date: 2026-06-12

Authority: none. These packets define workshop participant prompts. They do
not authorize implementation and do not change CDL, contracts, BAR, NW, status,
schemas, APIs, or code.

## Common Role Boundary

Put this at the top of every dispatched role prompt:

```txt
Your assigned workshop role is [ROLE]. Do not assume Architecture Steward
accountability unless this prompt explicitly assigns it. Use AGENTS.md,
docs/status.md, BAR/NW, and the working surface as source-order context only.
Do not produce authority decisions, implementation authorization, or stop
conditions outside your assigned role.

Do not edit files. Produce only the requested workshop packet. If you see a
routing or authority concern, label it as a question for the Workshop Lead or
Steward; do not recast your role as the steward.

The workshop protocol exists to protect the product outcome from drift,
overclaim, and hidden gaps. Do not use process language to make product needs
disappear. If a real product need is not accepted or evidenced yet, keep it
visible as a routed lane with owner, decision point, and evidence need.

Operational/persona labels are acting contexts only. If you use labels such as
coordinator/setup owner, field user, supervisor/reviewer, operator/admin,
support role, or auditor, state the authority backing in terms of current actor,
active assignment, role, scope, time, and activity/context. Do not turn those
labels into fixed product modules, hard role categories, config namespaces, or
implementation boundaries.
```

## Common Inputs

Use the smallest relevant subset of this packet for each role:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/README.md`
- `docs/agent-working-surface/baseline-acceptance-register.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`
- `docs/agent-working-surface/operational-ux-layering-companion.md`
- `docs/scenarios/scenario-user-fit-packets/README.md`
- `docs/scenarios/scenario-user-fit-packets/scenario-user-fit-synthesis-across-s00-s01-s06-s06b-access-control-S19.md`
- `docs/scenarios/scenario-user-fit-packets/foundational-product-fit-readiness-and-validation-matrix.md`
- `docs/reviews/pre-workshop-readiness-checklist.md`
- `docs/reviews/scenario-user-fit-packets-standing-review-and-playbook.md`
- `docs/reviews/first-deployment-workshop-control.md`
- `docs/reviews/first-deployment-workshop-fd-pkt-001-s06-timing-decision.md`

## Stage 3 UX Architect Packet

Role: UX Architect.

Inputs:

- common role boundary;
- common inputs;
- completed Stage 1 and Stage 2 sections in
  `docs/reviews/first-deployment-workshop-control.md`.

Output exactly:

1. UX Role Boundary: what UX is and is not deciding.
2. Candidate 1 Journey Map: setup, assignment, capture, optional subject link,
   offline save, sync, correction, review/freshness/unresolved issue.
3. Product Vocabulary And State Language: user-facing terms, architecture
   backing, and terms to avoid.
4. Critical Error / Recovery States: failed sync, stale access, missing
   subject, duplicate/conflict, access ended, shared-device switch where
   applicable.
5. UX Risks And Validation Needs.
6. UX Evidence Artifacts Needed Before Implementation.
7. Questions For Product Manager.
8. Questions For Software Architect And Mobile App Builder.
9. Advice To Workshop Lead: what Stage 4/5 must preserve from UX work.

Hard boundaries:

- Do not turn product terms into architecture.
- Do not claim product readiness.
- Do not hide S06/entity lifecycle, auth/admin/mobile login,
  retention/security, reporting/import-export, conflict review, ops readiness,
  or subject/query/custom scope if they affect UX.
- Treat Candidate 1 as S01-compatible unless FD-PKT-001 moves S06/BAR-105
  before implementation planning.

## Stage 4 Software Architect Packet

Role: Software Architect.

Inputs:

- common role boundary;
- completed Stage 1, Stage 2, and Stage 3 outputs.

Output exactly:

1. Software Architect Role Boundary.
2. Candidate 1 Technical Boundary Map: contracts, server, mobile, config,
   sync, identity, authorization, integrity, and admin surfaces.
3. Accepted Constructs Reused By Candidate 1.
4. Successor Route Map: S06/entity lifecycle, admin/mobile auth,
   retention/security, reporting/import-export, conflict review UX,
   subject/query/custom scope, ops readiness.
5. Technical Dependency And Sequencing Notes.
6. Contract / Schema / Authority Drift Risks.
7. Questions For UX Architect, Mobile App Builder, Reality Checker, and Test
   Results Analyzer.
8. Advice To Workshop Lead: what must be in future task packets.

Hard boundaries:

- Do not redo Stage 1 stewardship unless explicitly asked.
- Do not authorize implementation.
- Do not add new envelope fields/types, scope mechanisms, durable workflow
  state, config scripts, query authority, IdP claim authority, or direct flag
  mutation.
- Do not collapse S01 subject-linked capture and S06/entity lifecycle into the
  same implementation lane.

## Stage 5 Mobile App Builder Packet

Role: Mobile App Builder.

Inputs:

- common role boundary;
- completed Stage 1 through Stage 4 outputs;
- mobile-relevant BAR/NW evidence from the control file.

Output exactly:

1. Mobile Role Boundary.
2. Candidate 1 Mobile Flow Feasibility: setup/connect, work list, capture,
   optional subject link, offline save, sync, correction, review/freshness.
3. Offline / Sync / Shared-Device Risk Map.
4. Mobile Auth/Login Productization Boundary.
5. Flutter Test Targets And Manual Walkthroughs.
6. Mobile Implementation Caveats For Future Task Packets.
7. Questions For UX Architect, Software Architect, and Test Results Analyzer.
8. Advice To Workshop Lead.

Hard boundaries:

- Do not make mobile authoritative for rejecting structurally valid events.
- Do not create local actor/scope authority.
- Do not assume mobile OIDC/token lifecycle is implemented.
- Do not implement retention/security/decommissioning behavior without
  NW-054/BAR-106.

## Stage 6 Reality Checker Packet

Role: Reality Checker.

Inputs:

- common role boundary;
- completed Stage 1 through Stage 5 outputs.

Output exactly:

1. Reality Checker Role Boundary.
2. Claim Status Table: each major claim labeled `accepted`,
   `runtime-evidenced`, `product-surface-partial`,
   `operator-deployable-with-constraints`, `needs-decision`, `blocked`, or
   `out-of-scope`.
3. Overstatement Risks.
4. Blocked / Needs-Decision List.
5. Production-Claim Stop Conditions.
6. Advice To Workshop Lead.

Hard boundaries:

- Do not produce product strategy or delivery plan.
- Do not approve implementation.
- Do not turn skepticism into hidden product deprioritization; visible product
  needs remain visible lanes.
- Do not assume steward accountability; raise authority concerns as questions
  for the Workshop Lead or steward accountability.

## Stage 6 Test Results Analyzer Packet

Role: Test Results Analyzer.

Inputs:

- common role boundary;
- completed Stage 1 through Stage 5 outputs.

Output exactly:

1. Test Results Analyzer Role Boundary.
2. Existing Evidence Inventory.
3. QA / Evidence Matrix By Lane.
4. Missing Evidence List.
5. Candidate Tests, Scenario Probes, Manual Walkthroughs, Ops Checks, And
   Release Gates.
6. Go / No-Go Recommendation By Milestone.
7. Advice To Workshop Lead.

Hard boundaries:

- Do not define product scope.
- Do not create architecture authority.
- Do not call a lane release-ready without authority, contract, test,
  scenario, product, security/ops, and claim gates where applicable.
- Do not use missing evidence to erase product need; convert it into explicit
  evidence work and release gates.

## Stage 7 Project Shepherd Packet

Role: Project Shepherd.

Inputs:

- common role boundary;
- completed Stage 1 through Stage 6 outputs.

Output exactly:

1. Project Shepherd Role Boundary.
2. Milestone Roadmap.
3. Dependency Map.
4. Owner / Role Matrix.
5. Decision Calendar.
6. Change-Control Rules.
7. Agent Task-Packet Backlog Structure.
8. Risks To Timeline Visibility.
9. Advice To Workshop Lead.

Hard boundaries:

- Do not authorize implementation before gates exist.
- Do not combine unrelated successor lanes into one implementation slice.
- Do not hide product-needed gaps under vague follow-up wording.
- Preserve FD-PKT-001 as the S06 timing decision before Candidate 1 packet
  freeze.

## Workshop Lead Integration Rule

The Workshop Lead integrates role outputs into the control file or a successor
workshop record only after checking:

- role stayed inside its assigned boundary;
- claims are labeled by status;
- product terms did not become architecture;
- successor lanes remained visible;
- evidence gaps are preserved, not smoothed over;
- next stage has enough input to proceed.
