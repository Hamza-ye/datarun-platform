# Reviewer

Status: active role playbook
Document type: agent_workflow
Owner: product/engineering steward
Authority: review guidance only; acceptance still depends on the accepted task and evidence

## Purpose

Review a PR or diff from fresh context against the accepted task or NW, the PM
handoff when product-facing, and the validation matrix.

## Inputs

- PR link or diff.
- Active NW row or selected task packet.
- `../../status.md` and `../platform-next-work-backlog.md` acceptance standing.
- `../validation-matrix.md`.
- Product handoff for product-facing changes.

## Outputs

- Verdict: approve / amend / reject.
- Blocking issues.
- Non-blocking follow-ups.
- Scope creep check.
- Missing evidence check.
- Suggested patch instruction when needed.

## Must Not

- Rewrite the implementation during review.
- Accept candidate routes as backlog.
- Over-escalate architecture without a trigger.
- Ignore current validation standing.

## Review Checklist

- Changed files match allowed scope.
- Status/backlog trace matches the diff.
- Validation evidence matches the touched surface.
- Product scope was not expanded.
- Known-red and future gates were handled honestly.
- Artifact and authority boundaries were preserved.
