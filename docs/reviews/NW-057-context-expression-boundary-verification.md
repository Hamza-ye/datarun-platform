# NW-057 Context Expression Boundary Verification

Status: accepted

Date: 2026-06-11

Authority note: this review records the accepted NW-057 verification outcome.
It does not change CDL authority, IDR status, contracts, schemas, APIs,
backlog priority, or the expression grammar ceiling. The accepted evidence is
recorded in BAR-011 and the NW backlog.

## Finding

IDR-018 defines `context.*` as one of seven platform-fixed form-context
properties. The rationale companion correctly preserves the reason: `context.*`
must remain pre-resolved, read-only, bounded, and non-query-like.

Before NW-057, baseline behavior mostly honored that boundary:

- mobile pre-resolves known context properties in `ContextResolver`;
- evaluators are pure, bounded, side-effect-free, and null-safe;
- expression tests cover invalid operators, depth, namespaces, and shared
  evaluator behavior;
- no expression surface has dynamic lookups, joins, aggregation, functions, or
  scripts.

The gap was narrower: deploy-time validation treated any `context.*` reference
as namespace-valid and did not enforce the seven-property whitelist. Unknown
context references then evaluated as missing/null rather than failing before
publication. Mobile config tests also preserved an example unknown context ref
as ordinary expression data.

## Why This Matters

This is not a current evidence failure for BAR-011 as written. BAR-011 accepted
the bounded expression grammar/evaluator behavior, not a specific deploy-time
context-property whitelist test.

It is still worth routing because future report views, admin/config UX, or
context-dependent APIs may rely on a closed context vocabulary. If they do, the
platform needs one explicit position before implementation:

- enforce the seven-property whitelist at deploy time; or
- intentionally tolerate unknown `context.*` refs as null/missing values and
  document that behavior where agents expect it.

Leaving the ambiguity informal would have made the rationale companion carry a
verification note, which is the wrong layer.

## Resolution

Owner: config/platform steward.

Decision: unknown `context.*` references are deploy-time invalid.

Implementation:

- `DeployTimeValidator` whitelists the seven IDR-018 form-context references
  for condition operands and default `ref` nodes.
- Mobile config-store tests use an accepted context reference in packaged
  expression examples instead of preserving an invalid unknown reference.
- Runtime evaluators remain null-safe for missing values; the boundary is
  enforced before config publication.

Evidence:

- `mvn -Dtest=DeployTimeValidatorTest test` passed (52 tests).
- `flutter test test/config_store_test.dart` passed (35 tests).

Preserved boundary: no functions, scripts, dynamic queries, aggregation, joins,
recursion, deployer-authored context namespace expansion, config-package
contract changes, runtime expression side effects, or new context properties.
