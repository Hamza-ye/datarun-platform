---
name: datarun-tester-validation-auditor
description: Map a Datarun diff, PR, or acceptance claim to required validation evidence using the validation matrix, with context-friendly Maven/server test handling and false-confidence checks.
---

# Datarun Tester / Validation Auditor

Use this skill to map touched files or acceptance claims to required evidence.
It does not create product scope, gates, or acceptance by itself.

## Required Inputs

1. `docs/agent-working-surface/validation-matrix.md`.
2. PR, working diff, or explicit acceptance claim.
3. `docs/status.md` and `docs/agent-working-surface/platform-next-work-backlog.md`
   when status/backlog acceptance is claimed.
4. `server/AGENTS.md`, `mobile/AGENTS.md`, or `contracts/AGENTS.md` when those
   surfaces are touched.
5. Current CI/check status when the user asks about CI or PR readiness.

## Outputs

- Validation checklist by touched surface.
- Missing evidence list.
- Risk classification: blocker, non-blocker, or follow-up.
- Suggested commands with working directories.
- Statement on whether acceptance evidence is sufficient.

## Evidence Rules

- Use the narrowest focused check first.
- Require the full gate only when the validation matrix or selected task
  requires it for the touched surface.
- Known-red and future gates must be reported honestly but cannot become hard
  blockers until accepted.
- Manual/ops evidence is required only when the selected NW scopes it.
- Do not mark implementation accepted because docs changed.
- Do not skip a relevant gate without recording rationale.

## Maven / CI Output

For Maven/server gates, follow root `AGENTS.md` and `server/AGENTS.md`: focused
runs may stream when small; noisy/full or CI-equivalent runs should be captured
to `/tmp` and summarized.

## Specific Attention

- Mobile full tests are CI-backed for matching paths; `flutter analyze` remains
  known-red unless current status says it has been fixed or baselined.
- Android compile is required for native, platform, auth, or plugin-impacting
  mobile changes.
- Broad mobile fakes and `noSuchMethod` harnesses can create false confidence.
- Server test output volume can hide failures or warnings during review.
- Web-admin UI/template work needs product-vocabulary validation when
  user-facing, even if server tests pass.
- CI red, absent CI for a touched surface, or long/noisy logs are validation
  operability risks; classify them instead of inventing product work.

## Must Not

- Do not invent product scope.
- Do not change code or tests unless a separate NW selects that work.
- Do not make `flutter analyze` a hard gate while known-red issues remain.
- Do not treat a future product journey smoke as current until selected.
- Do not require operations rehearsal unless an ops/release slice scopes it.
