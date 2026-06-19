# Contracts Agent Instructions

Use this file when touching `contracts/` or changing data that crosses
server/mobile/process boundaries.

## Contract Roles

- `envelope.schema.json` defines the closed event envelope shape and type
  vocabulary.
- `sync-protocol.md` records push/pull/config/subject-history protocol intent
  and invariants.
- `flag-catalog.md` records flag categories, severity, resolvability, and
  state-exclusion semantics.
- `shape-format.schema.json` defines deployer-authored form shape DSL.
- `config-package.schema.json` defines server-emitted/mobile-consumed config
  package shape.
- `pattern-definition.schema.json` and `patterns/*.json` define platform-owned
  pattern definitions.
- `shapes/*.schema.json` defines platform payload shapes.
- `fixtures/*.json` holds shared equivalence fixtures.

## Boundary Rules

contracts are not generated code. Do not assume every schema is runtime-validated
everywhere, and do not treat a contract edit as an implementation by itself.
Keep contract intent, server validation, mobile consumption, and shared fixtures
in sync.

Server plus mobile tests are both required when a contract change affects
sync/config payloads, envelope or event payload meaning, pattern/projection
fixtures, shared fixture parity, or data consumed across both runtimes.

## Fixture And Parity Guidance

When fixtures change, update the server and mobile tests that consume them in
the same slice unless the task explicitly routes one side as follow-up. Preserve
shared fixture names and meanings; add new fixtures instead of mutating old ones
when old behavior must remain provable.

## Evidence

Report the focused server contract/projection tests, focused mobile fixture or
projection tests, and the broader gates required by the task. If only contract
text changed, still run `git diff --check` and explain why runtime tests were
not required.
