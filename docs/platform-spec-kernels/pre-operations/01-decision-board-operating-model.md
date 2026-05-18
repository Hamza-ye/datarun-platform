# Decision Board Operating Model

Status: Working operating model; not platform behavior authority

This document defines a small decision board for the solo-plus-AI project mode. It exists to separate authority, drafting, challenge, and delivery before platform-spec platform-spec section drafting.

The board is not a committee and not a source of architecture by itself. It is a repeatable way to decide what becomes architecture, what remains a hold-back, and what can be safely deferred.

## Why This Exists

At the current stage, the platform has a protected ADR-001 through ADR-005 baseline, assessed post-baseline material, and many documents. The main risk is not lack of ideas. The main risk is that implementation, AI drafting, or convenience silently turns into a long-term platform invariant.

The board prevents that by forcing each important choice through separate views:

- stakeholder value
- baseline and invariant impact
- delivery feasibility
- challenge and missing-risk review

## Roles

One person may wear several roles, but the roles must stay separate.

| Role | Played by | Authority | Main question |
|---|---|---|---|
| Product Owner | Hamza | Decides stakeholder value, deployment priority, and acceptable deferral | Is this the right thing for users, deployers, and long-term adoption? |
| Architecture Steward | AI-assisted first pass; human reviewer when available | Challenges baseline impact and coupling; does not decide alone | Does this preserve ADR-001 through ADR-005 and the professional-baseline controls? |
| Delivery Lead | Hamza | Decides sequencing, implementation feasibility, and delivery risk | Can this be built safely by a solo-ish team using AI agents? |
| Challenge Reviewer | AI agent or external reviewer | Finds weak assumptions, hidden stakeholder costs, and reversibility traps | What are we missing or underestimating? |
| Drafting Agent | AI agent | Drafts briefs, compares options, checks docs, prepares tickets | What structured material helps the owner decide? |

## Board-Worthy Decisions

Bring a decision to this board when it:

- touches the event envelope, event identity, actor identity, assignment, sync, authorization, tenancy, deployment partitioning, or configuration boundary
- could introduce a new invariant
- affects more than one stakeholder group
- would be expensive to reverse after implementation
- could cause AI agents or implementation code to reinterpret the baseline
- blocks platform-spec section drafting planning or first implementation sequencing

Do not use the board for ordinary implementation details, naming, minor UI copy, small refactors, or low-risk local design.

## Decision Intake

Each decision starts with this intake:

```md
Decision needed:
Why now:
Affected stakeholders:
Affected baseline areas:
Options:
Unknowns:
Owner:
Deadline or trigger:
```

## Decision Brief Template

Each board-worthy decision should produce a brief before acceptance:

```md
Decision:
Status:
Recommended option:

Options considered:

Product Owner view:
- Stakeholder benefit:
- Stakeholder harm:
- Adoption risk:
- First deployment need:

Architecture Steward view:
- Baseline impact:
- New invariants:
- Coupling risk:
- Reversibility:

Delivery Lead view:
- Implementation difficulty:
- Build order:
- Testing burden:
- Operational complexity:

Challenge Reviewer view:
- Hidden assumptions:
- Worst-case failure:
- What can be deferred:
- What needs stakeholder or domain input:

Proposed decision:
Explicit deferrals:
Reopen trigger:
Required follow-up:
```

## Allowed Outcomes

Every reviewed item must end in one of these outcomes:

| Outcome | Meaning |
|---|---|
| Accepted | The project may use this as the working decision. |
| Rejected | Do not use this path without reopening. |
| Deferred | Safe to postpone; record the trigger that would reopen it. |
| Hold-back | Do not decide now, but prevent platform-spec section drafting from accidentally deciding it. |
| Needs spike | Run a focused technical or product investigation. |
| Needs stakeholder input | Decision depends on real-world usage, policy, or adoption tradeoffs. |
| Requires formal ADR/change | The choice changes a baseline invariant or creates a new architecture-grade mechanism. |

Avoid vague outcomes such as "later" or "TBD" without a reopen trigger.

## Current Decision Queue

| Priority | Decision | Current action |
|---|---|---|
| Now | Deployment / tenancy decision | Review `02-deployment-tenancy-decision-brief.md` before affected platform-spec section drafting. |
| Now | Authentication / actor mapping decision | Review `03-authentication-actor-mapping-decision-brief.md` before authorization/sync platform-spec section drafting. |
| Next | Notifications / escalation routing | Route through Trigger / Reactivity and Flag / Resolution after the first two decisions. |
| Next | Admin / configuration surfaces | Route through Configuration and Assignment / Authority / Sync before implementation planning. |
| Later | Reporting, audit/export, retention, local lifecycle | Keep visible as deferred unless first deployment requires them. |

## AI Agent Use

AI agents may:

- draft decision briefs
- compare options against the baseline
- search for hidden coupling
- prepare challenge reviews
- turn accepted decisions into hold-backs, spec-plan items, or implementation tickets

AI agents must not:

- silently accept a baseline change
- decide stakeholder priority
- turn a recommendation into accepted architecture
- write final platform-spec sections from unaccepted briefs
- collapse product, architecture, and delivery judgment into one answer

## Operating Rhythm

Use this rhythm while platform-spec section drafting is being planned:

1. Maintain a small queue of board-worthy decisions.
2. Draft one or two briefs at a time.
3. Review each brief through the role views.
4. Record one allowed outcome.
5. Update the platform-spec section drafting plan, gap register, or hold-back list.
6. Only then draft affected final platform-spec sections.

## Immediate Rule

Before affected platform-spec section drafting, resolve or explicitly hold back:

- deployment/tenant context and whether it stays outside the event envelope
- authentication/account-to-actor mapping and whether groups are authority sources or provisioning/configuration helpers
