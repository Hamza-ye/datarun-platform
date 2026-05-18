# Pre-Operations Decision Board

This folder holds pre-specification decision material.

It is intentionally separate from `../professional-baseline/`.

The professional-baseline folder records the accepted ADR-001 through ADR-005 baseline, responsibility routing, and later-source assessments. This folder records readiness assessment, stakeholder-facing briefs, and the small accepted-decision surface used for platform-spec section drafting planning.

## Start Here

| Need | Use |
|---|---|
| Current accepted pre-specification decisions | `04-accepted-pre-specification-decisions.md` |
| Readiness inputs | `00-baseline-validation-and-full-stack-readiness.md` |
| Decision process | `01-decision-board-operating-model.md` |
| Stakeholder rationale for deployment/tenancy | `02-deployment-tenancy-decision-brief.md` |
| Stakeholder rationale for authentication/actor mapping | `03-authentication-actor-mapping-decision-brief.md` |

## Files

- `00-baseline-validation-and-full-stack-readiness.md`: validation and readiness assessment used as the pre-operations input surface before platform-spec section drafting.
- `01-decision-board-operating-model.md`: lightweight role-separated decision process for product authority, architecture challenge, delivery sequencing, and AI drafting.
- `02-deployment-tenancy-decision-brief.md`: stakeholder brief for the deployment/tenancy decision; accepted outcome is `PREOP-001`.
- `03-authentication-actor-mapping-decision-brief.md`: stakeholder brief for the authentication/account-to-actor mapping decision; accepted outcome is `PREOP-002`.
- `04-accepted-pre-specification-decisions.md`: canonical accepted decision record for pre-specification decisions.

## Current Rule

Decision briefs explain rationale for stakeholders. They are not accepted architecture unless `04-accepted-pre-specification-decisions.md` explicitly marks their outcome accepted.

For future decision briefs, assign one outcome before affected platform-spec section drafting:

- accepted
- rejected
- deferred
- hold-back
- needs spike
- needs stakeholder input
- requires formal ADR/change

## Agent Surface

For platform-spec section drafting planning, use only:

1. `04-accepted-pre-specification-decisions.md`
2. `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
3. `../professional-baseline/16-operational-constraints-boundary-control.md`
4. `../professional-baseline/17-authorization-visibility-boundary-control.md`
5. `../product-alignment/README.md`

Open the stakeholder briefs only when the accepted record is not enough to understand the tradeoff.

## Documentation Hygiene

- Keep accepted decisions in `04-accepted-pre-specification-decisions.md`.
- Keep stakeholder rationale in briefs, but do not let old brief wording override the accepted record.
- Update README entrypoints when moving, accepting, or superseding docs.
- Prefer one canonical "current" file per concern; avoid repeating status notes across many files.
- When a reference becomes historical, say so explicitly instead of leaving it as an implied current instruction.
