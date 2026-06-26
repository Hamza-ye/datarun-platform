# Product Specifications

Status: active product-specification index

Use this directory for accepted user-visible behavior: terminology, journeys,
states, actions, recovery behavior, and product acceptance criteria.

Product handoff documents may also live here when they are explicitly marked as
derived planning surfaces. A handoff does not replace the accepted product
specification and does not add product behavior, implementation standing,
architecture authority, validation policy, or production approval.

Do not place product discovery, workshop output, UI implementation design,
platform mechanics, or operational policy here. Link those inputs and keep the
normative product behavior in one specification.

## Required Shape

Each product specification should include:

- required metadata from
  [documentation-organization.md](../../documentation-organization.md);
- user problem and actors;
- included and excluded behavior;
- user-facing states, actions, and language;
- product acceptance criteria;
- architecture/contract/platform guardrails;
- validation caveats and unresolved routes;
- related platform specification and implementation successors.

## Index

| Specification | Status | Owner | Source NW | Supersedes |
|---|---|---|---|---|
| [Product Goal, Complete Scenario Portfolio, And Ordered Slice Roadmap](product-goal-and-representative-journeys.md) | active planning surface; derived planning only | product steward | NW-168 | none |
| [Product Candidate 1](product-candidate-1.md) | accepted | product steward | NW-084 | none |
| [Product Candidate 1 PM Handoff](product-candidate-1-pm-handoff.md) | active PM handoff surface; derived planning only | product steward | NW-106 | none |
| [Product Candidate Handoff Template](product-candidate-handoff-template.md) | active template; process structure only | product steward | NW-106 | none |
| [Product Candidate 2 PM Handoff](product-candidate-2-pm-handoff.md) | active PM handoff surface; derived planning only | product steward | NW-121 | none |
| [Product Candidate 3 PM Handoff](product-candidate-3-pm-handoff.md) | active PM handoff surface; derived planning only | product steward | NW-127 | none |
| [Product Candidate 4 PM Handoff](product-candidate-4-pm-handoff.md) | active PM handoff surface; derived planning only | product steward | NW-133 | none |
| [Stock Operations Pilot PM Handoff](stock-operations-pilot-pm-handoff.md) | active PM handoff surface; derived planning only | product steward | NW-149 | none |

`product-goal-and-representative-journeys.md` is the active Product Goal,
complete scenario portfolio, and ordered slice roadmap surface after NW-168. It
is derived planning only and does not accept product behavior, implementation
standing, architecture authority, validation policy, product-candidate scope,
or production approval. `product-candidate-1.md` remains the accepted Product
Candidate 1 behavior specification. `product-candidate-1-pm-handoff.md` is a
derived PM planning surface for PC1 route selection.
`product-candidate-handoff-template.md` provides reusable process structure
only. `product-candidate-2-pm-handoff.md` is a derived PM planning surface
selecting the PC2 boundary; it does not accept product behavior or
implementation standing. `product-candidate-3-pm-handoff.md` is a derived PM
planning surface selecting the PC3 boundary; it does not accept product
behavior, implementation standing, or production approval.
`product-candidate-4-pm-handoff.md` is a derived PM planning surface selecting
the PC4 boundary; it does not accept product behavior, implementation standing,
architecture authority, retention/security promises, or production approval.
`stock-operations-pilot-pm-handoff.md` is a derived PM planning surface for the
local/on-prem stock operations pilot; it keeps stocktake line first, carries
PR #58 / NW-148 proof evidence forward, and selects NW-150 as the only next
implementation route without approving real users/data or production use.
