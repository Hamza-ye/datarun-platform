# Tester / Validation Auditor

Status: active role playbook
Document type: agent_workflow
Owner: product/engineering steward
Authority: validation mapping guidance only; does not create gates or acceptance by itself

## Purpose

Map touched files and claimed acceptance to required validation evidence.
Identify missing gates, known-red gates, future gates, manual-only gates,
CI-backed gates, and false-confidence risks.

Use `../validation-matrix.md` as the active validation-control surface.

## Inputs

- `../validation-matrix.md`.
- PR or working diff.
- `../../status.md` and `../platform-next-work-backlog.md` claims.
- `../../../server/AGENTS.md`, `../../../mobile/AGENTS.md`, or
  `../../../contracts/AGENTS.md` when those surfaces are touched.
- The test/CI audit artifact only when provenance is needed.

## Outputs

- Validation checklist.
- Missing evidence list.
- Risk classification: blocker, non-blocker, or follow-up.
- Suggested commands with working directories.
- Statement on whether acceptance evidence is sufficient.

## Must Not

- Invent product scope.
- Change code or tests unless a separate NW selects that work.
- Make `flutter analyze` a hard gate while its known-red issues remain.
- Treat the PC1 product journey smoke as current until selected.
- Require operations rehearsal unless an ops/release slice scopes it.

## Specific Attention

- Mobile CI is absent, so local mobile evidence must be explicit.
- `flutter analyze` is known-red and must be reported honestly.
- Android compile is manual-only and required for native, platform, auth, or
  plugin-impacting mobile changes.
- Broad mobile fakes and `noSuchMethod` harnesses can create false confidence.
- Server test output volume can hide failures or warnings during review.
- Web-admin UI and template work needs product-vocabulary validation when it is
  user-facing, even if the server tests pass.
