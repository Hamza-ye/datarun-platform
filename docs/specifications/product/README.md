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
| [Product Candidate 1](product-candidate-1.md) | accepted | product steward | NW-084 | none |
| [Product Candidate 1 PM Handoff](product-candidate-1-pm-handoff.md) | active PM handoff surface; derived planning only | product steward | NW-106 | none |
| [Product Candidate Handoff Template](product-candidate-handoff-template.md) | active template; process structure only | product steward | NW-106 | none |
| [Product Candidate 2 PM Handoff](product-candidate-2-pm-handoff.md) | active PM handoff surface; derived planning only | product steward | NW-121 | none |
| [Product Candidate 3 PM Handoff](product-candidate-3-pm-handoff.md) | active PM handoff surface; derived planning only | product steward | NW-127 | none |

`product-candidate-1.md` remains the accepted Product Candidate 1 behavior
specification. `product-candidate-1-pm-handoff.md` is a derived PM planning
surface for PC1 route selection. `product-candidate-handoff-template.md`
provides reusable process structure only. `product-candidate-2-pm-handoff.md`
is a derived PM planning surface selecting the PC2 boundary; it does not accept
product behavior or implementation standing. `product-candidate-3-pm-handoff.md`
is a derived PM planning surface selecting the PC3 boundary; it does not accept
product behavior, implementation standing, or production approval.
