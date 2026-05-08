# Pre-Operations Decision Board

This folder holds pre-atomization decision material.

It is intentionally separate from `../professional-baseline/`.

The professional-baseline folder records the accepted ADR-001 through ADR-005 baseline, validated boundary controls, and later-source assessments. This folder records readiness assessment, stakeholder-facing briefs, and the small accepted-decision surface used for atomization planning.

## Start Here

| Need | Use |
|---|---|
| Current accepted pre-atomization guardrails | `04-accepted-pre-atomization-decisions.md` |
| Readiness inputs | `00-baseline-validation-and-full-stack-readiness.md` |
| Decision process | `01-decision-board-operating-model.md` |
| Stakeholder rationale for deployment/tenancy | `02-deployment-tenancy-decision-brief.md` |
| Stakeholder rationale for authentication/actor mapping | `03-authentication-actor-mapping-decision-brief.md` |

## Files

- `00-baseline-validation-and-full-stack-readiness.md`: validation and readiness assessment used as the pre-operations input surface before atomization.
- `01-decision-board-operating-model.md`: lightweight role-separated decision process for product authority, architecture challenge, delivery sequencing, and AI drafting.
- `02-deployment-tenancy-decision-brief.md`: stakeholder brief for the deployment/tenancy guardrail; accepted outcome is `PREOP-001`.
- `03-authentication-actor-mapping-decision-brief.md`: stakeholder brief for the authentication/account-to-actor mapping guardrail; accepted outcome is `PREOP-002`.
- `04-accepted-pre-atomization-decisions.md`: canonical accepted decision record for pre-atomization guardrails.

## Current Rule

Decision briefs explain rationale for stakeholders. They are not accepted architecture unless `04-accepted-pre-atomization-decisions.md` explicitly marks their outcome accepted.

For future decision briefs, assign one outcome before affected atomization:

- accepted
- rejected
- deferred
- hold-back
- needs spike
- needs stakeholder input
- requires formal ADR/change

## Agent Surface

For atomization planning, use only:

1. `04-accepted-pre-atomization-decisions.md`
2. `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
3. `../professional-baseline/16-operational-constraints-boundary-control.md`

Open the stakeholder briefs only when the accepted record is not enough to understand the tradeoff.

## Documentation Hygiene

- Keep accepted decisions in `04-accepted-pre-atomization-decisions.md`.
- Keep stakeholder rationale in briefs, but do not let old brief wording override the accepted record.
- Update README entrypoints when moving, accepting, or superseding docs.
- Prefer one canonical "current" file per concern; avoid repeating status notes across many files.
- When a reference becomes historical, say so explicitly instead of leaving it as an implied current instruction.
