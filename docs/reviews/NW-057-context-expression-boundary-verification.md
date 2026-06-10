# NW-057 Context Expression Boundary Verification

Status: review / routed verification note

Date: 2026-06-07

Authority note: this review does not change CDL authority, IDR status, BAR
status, contracts, schemas, APIs, runtime behavior, backlog priority, or
accepted baseline standing. It records a verification finding and routes it to
the working-surface backlog.

## Finding

IDR-018 defines `context.*` as one of seven platform-fixed form-context
properties. The rationale companion correctly preserves the reason: `context.*`
must remain pre-resolved, read-only, bounded, and non-query-like.

Current baseline behavior mostly honors that boundary:

- mobile pre-resolves known context properties in `ContextResolver`;
- evaluators are pure, bounded, side-effect-free, and null-safe;
- expression tests cover invalid operators, depth, namespaces, and shared
  evaluator behavior;
- no expression surface has dynamic lookups, joins, aggregation, functions, or
  scripts.

The gap is narrower: deploy-time validation currently treats any
`context.*` reference as namespace-valid and does not enforce the seven-property
whitelist. Unknown context references then evaluate as missing/null rather than
failing before publication. Mobile config tests also preserve an example
unknown context ref (`context.default_visit_type`) as ordinary expression data.

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

Leaving the ambiguity informal would make the rationale companion carry a
verification note, which is the wrong layer.

## Route

Owner: config/platform steward.

Trigger: before any NW-044 report-view/API, admin/config UX, or contract work
depends on context-property closure; otherwise treat as a small P3
contract-hygiene verification item.

Recommended backlog row: NW-057, "Verify context expression property
boundary".

Exit condition:

1. Decide whether unknown `context.*` references are deploy-time invalid or
   intentionally null-on-unknown.
2. If invalid, add deploy-time validation and tests for the fixed property
   whitelist.
3. If tolerated, update the relevant IDR/module/test documentation so the
   tolerance is explicit and not rediscovered as drift.
4. Preserve the existing expression grammar ceiling: no functions, scripts,
   dynamic queries, aggregation, joins, recursion, or deployer-authored context
   namespace expansion.
