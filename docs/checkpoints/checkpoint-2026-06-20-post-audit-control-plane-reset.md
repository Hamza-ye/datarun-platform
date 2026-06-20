# Post-Audit Control-Plane Reset Checkpoint

Status: accepted checkpoint
Date: 2026-06-20
Source: NW-104 through NW-109 post-audit reset wave
Authority: checkpoint only; does not add product behavior, architecture authority, validation policy, CI behavior, or implementation standing

## What Closed

- NW-104 captured the three post-audit reports as traceable non-authoritative evidence and froze product implementation until reset rows closed or were parked.
- NW-105 rewrote the root agent instructions into a product-slice-first implementer router and split server, mobile, and contracts guidance into nested AGENTS files.
- NW-106 created the Product Candidate 1 PM handoff layer and reusable future-candidate handoff template.
- NW-107 published the validation matrix as the active touched-surface validation and acceptance-evidence router.
- NW-108 added role-scoped durable playbooks and Codex-discoverable wrappers for PM planning, implementation, validation, review, and boundary/steward routing.
- NW-109 added the mobile CI/analyzer path, including CI-backed mobile tests and Android compile plus a known-red analyzer record.

## What Is Now True

- Audit reports are traceable non-authoritative artifacts.
- Root AGENTS is product-slice-first.
- Steward is no longer the default working role.
- The Product Candidate 1 PM handoff exists.
- The validation matrix exists.
- Role playbooks and Codex wrappers exist.
- Mobile `flutter test` and Android compile are CI-backed.
- `flutter analyze` remains known-red and non-blocking until fixed or baselined.

## What Is Not Changed

- Product scope.
- Runtime behavior.
- Architecture authority.
- Contracts.
- Real-production approval.
- Analyzer hard-gate standing.
- PC1 smoke gate standing.

## Next Selection Rule

- Use `docs/specifications/product/product-candidate-1-pm-handoff.md`.
- Use `docs/agent-working-surface/skills/pm-product-planner.md` or `.agents/skills/datarun-lane-selector`.
- Select one next product NW.
- Use `docs/agent-working-surface/validation-matrix.md` for acceptance evidence.
- Route to the gap playbook only when a stop trigger fires.

## Known Follow-Up Candidates

Candidate-only, not accepted rows:

- analyzer cleanup/baseline
- PC1 product journey smoke definition
- first proof target decision
- PM backlog view
- vocabulary validation packet
- product UI/structured editor polish if selected
- server log-volume reduction
- mobile fake/harness cleanup
- shared fixture/contract parity
