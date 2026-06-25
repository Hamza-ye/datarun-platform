# NW-157 - Select Next Pilot Behavior Slice From Accepted Proof Map

## Goal

Select the next pilot behavior slice from the NW-155 behavior proof map using
current accepted evidence, without letting fixture or domain vocabulary become
platform direction.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/artifacts/NW-155-reframe-pilot-behavioral-proof-path-and-pr63-classification.md`
- `docs/specifications/platform/scoped-configured-work-evidence-inspection-boundary.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- PR #63 accepted runtime standing

## Required Output

A short planning/control note or status/backlog routing update that selects
exactly one next behavior slice.

Candidate outcomes:

- explicit principal-binding/login path;
- local/on-prem operational preflight;
- another bounded behavior proof from the accepted map.

## Constraints

- Do not start implementation.
- Do not create a stock-specific UI route by default.
- Do not use domain vocabulary in active route names except when referring to
  fixture files or accepted historical evidence.
- Do not change contracts, schemas, sync protocol, CDL, BAR, gap register,
  runtime code, production approval, login/principal-binding implementation, or
  local/on-prem preflight implementation.
- Keep real users/data, account import, submitted-record import/replay,
  controlled operational use, and production cutover blocked unless a separate
  owner route selects them.

## Validation

- `git diff --check`
- targeted routing grep proving only the selected next behavior slice is active
