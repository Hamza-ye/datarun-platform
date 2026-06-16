# Expression Language Behavior

Status: accepted
Document type: platform_spec
Owner: config/mobile verifier
Source: NW-068 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-068-extract-configuration-durable-behavior.md`
Authority: CDL-043 and CDL-052; BAR-011; NW-057; `contracts/fixtures/expression-evaluation.json`; `contracts/config-package.schema.json`; IDR-018 as historical decision input
Last reviewed: 2026-06-16
Supersedes: none
Related: `docs/specifications/platform/configuration-package-and-shapes.md`; `contracts/fixtures/expression-evaluation.json`; `contracts/config-package.schema.json`; `docs/decisions/idr-018-expression-grammar.md`; `server/src/main/java/dev/datarun/server/config/ExpressionEvaluator.java`; `server/src/main/java/dev/datarun/server/config/DeployTimeValidator.java`; `mobile/lib/domain/expression_evaluator.dart`; `mobile/test/expression_evaluator_test.dart`; `server/src/test/java/dev/datarun/server/config/ExpressionEvaluatorTest.java`; `server/src/test/java/dev/datarun/server/config/DeployTimeValidatorTest.java`

## Purpose

This specification records the accepted expression-language behavior used by
configuration publication, server evaluation, mobile form behavior, and shared
cross-platform fixtures.

It does not create architecture authority, change the grammar, or add a new
contract schema. It extracts the durable behavior from IDR-018, BAR-011,
NW-057, fixtures, and implementation evidence into the platform specification
surface.

## Durable Contract Decision

`contracts/expression.schema.json` is not added in NW-068.

The current durable expression surface is:

- this platform specification for expression semantics;
- `contracts/fixtures/expression-evaluation.json` for server/mobile evaluator
  equivalence examples and regression expectations;
- `contracts/config-package.schema.json` for the expression-rule envelope
  inside config packages.

Reason: the accepted contract already carries expression rules as package
content and verifies evaluator behavior through shared fixtures. Adding a new
machine schema would be a new contract artifact with its own validation and
compatibility obligations; NW-068 is a documentation extraction slice and does
not need that schema to preserve accepted behavior.

A future route should add `contracts/expression.schema.json` only when a
machine-readable AST contract is needed for external authoring, generated
validators, or independent process-boundary validation beyond the current
package schema and fixtures.

## Scope

Expressions are pure JSON AST values. They may read provided values and return a
boolean or a value. They do not read databases, call services, write state,
query dynamically, execute functions, recurse, loop, join, aggregate, or perform
device-side policy execution.

Form expressions may be evaluated on mobile. Server evaluators and validators
use the same semantic boundary for validation and parity. Trigger-context
expression parsing exists for platform-owned trigger surfaces, but general
trigger execution remains separately routed and is not enabled by this spec.

## Rule Types

Expression rules are packaged under `expressions` in the atomic config package.
The package schema owns the rule wrapper shape and allowed rule-type names.

Accepted rule behavior:

| Rule type | Expression member | Result |
|---|---|---|
| `show_condition` | `when` | Field is shown when the condition evaluates `true`; falsey evaluation hides it. Without a rule, the field is visible. |
| `warning` | `when` | Non-blocking warning is shown when the condition evaluates `true`; falsey evaluation shows no warning. |
| `default` | `value` | A value is applied only when the field has no current value and the expression returns a non-null compatible value. |

Warning message text is package rule metadata, not part of the AST. Defaults do
not create side effects beyond initial form value population.

## AST Semantics

The accepted AST is a JSON object with exactly one operator node at each
evaluation point. Accepted operator families:

- comparison operators: `eq`, `neq`, `gt`, `gte`, `lt`, `lte`, `in`,
  `not_null`;
- logical operators: `and`, `or`, `not`;
- value node: `ref`.

Comparison nodes take operand arrays. `not_null` takes one operand; the other
comparison operators take two operands. Logical `and` and `or` combine
comparison nodes; `not` wraps one comparison node. `ref` resolves one reference
and returns the referenced value.

Deploy-time validation enforces the grammar budget and operator compatibility.
Published configurations must not rely on runtime tolerance of malformed AST
nodes.

## References And Context

Accepted reference namespaces are:

- `payload.*` for current form payload values;
- `entity.*` for projected entity attributes in form context;
- `context.*` for fixed platform form context;
- `event.*` for trigger-context event payload values.

Bare field names are invalid. Trigger-context expressions must use `event.*`
only; `payload.*`, `entity.*`, and `context.*` are rejected in trigger context.

The accepted form-context vocabulary is closed to these seven refs:

```text
context.subject_state
context.subject_pattern
context.activity_stage
context.actor.role
context.actor.scope_name
context.days_since_last_event
context.event_count
```

Unknown `context.*` refs are deploy-time invalid. Runtime evaluators remain
null-safe for missing values, but config publication must use the fixed
whitelist. New context properties require a platform evolution route and
compatibility evidence.

## Evaluation Rules

Evaluation receives an AST plus a flat resolved-values map. It is deterministic
and side-effect free.

Accepted semantics:

- absent or null operands make comparisons evaluate `false`, except
  `not_null`, which explicitly tests presence;
- `eq` and `neq` use equality with accepted numeric/string coercion;
- ordering operators use deploy-time comparable field-type checks and evaluate
  false when runtime coercion fails;
- `in` checks membership in a literal array or a resolved array value such as a
  `multi_select` field;
- `ref` returns the resolved value or null;
- unknown operators, malformed nodes, invalid namespaces, and unsupported
  hidden-function-style nodes fail closed in runtime evaluators and must be
  rejected before publication when detected by deploy-time validation.

The shared fixture is the cross-platform regression source for exact evaluator
results. Server and mobile behavior must remain equivalent for every fixture
case.

## Publication Rules

Deploy-time validation must reject invalid expression rules before packaging.
Required checks include:

- rule type is one of `show_condition`, `warning`, or `default`;
- target field exists in the target shape;
- referenced payload fields exist and are compatible with the operator;
- unknown `context.*` refs are rejected against the seven-ref whitelist;
- bare refs and invalid namespaces are rejected;
- ordering and `in` operators are used only with compatible field types;
- `multi_select` equality misuse is rejected in favor of `in`;
- predicate-count and logical-depth budgets are enforced;
- default expression output is compatible with the target field type.

Only validated expression rules reach the config packager.

## Compatibility

The JSON AST is stored, packaged, and evaluated by both server and mobile. Any
grammar change, operator addition, context-property addition, or value-semantic
change requires a routed platform decision plus migration and server/mobile
compatibility evidence.

Evaluator implementation internals may change if the same AST and resolved
values produce the same fixture-backed results.

## Acceptance Evidence

BAR-011 is the baseline evidence for this specification. It records server and
mobile evaluator parity over `contracts/fixtures/expression-evaluation.json`,
fail-closed handling for malformed/unsupported expression shapes, and NW-057's
deploy-time invalidation of unknown `context.*` refs against the seven accepted
form-context properties.

The implementation evidence remains in the referenced server and mobile tests;
this specification is the durable behavior target, not a replacement for those
tests.

## Classification Of IDR-018 Details

| Detail | Durable classification |
|---|---|
| JSON AST, accepted operators, `ref` value node, rule types, reference namespaces, null-safe comparison, numeric/string coercion, no functions, no recursion | Accepted platform behavior. |
| Seven `context.*` properties and unknown-context deploy-time invalidation | Accepted platform behavior from IDR-018 plus NW-057. |
| Expression-rule wrapper in config packages | Existing contract authority in `contracts/config-package.schema.json`. |
| Cross-platform evaluator examples | Existing contract fixture authority in `contracts/fixtures/expression-evaluation.json`. |
| Java and Dart evaluator class structure | Implementation evidence only. |
| IDR-018 reference to `contracts/expression.schema.json` | Old-doc trace only for now; no such contract is added in NW-068. |
| IDR-018 prose implying show-condition null/error fallback to visible | Old-doc trace only; accepted behavior is falsey condition evaluation hides the field, while invalid configs are blocked at publish. |

## Non-Goals

This spec does not authorize deployer-authored functions, scripts, dynamic
queries, joins, aggregation, recursion, custom context namespaces, custom scope
logic, field-level side effects, trigger execution, or mobile authoritative
policy execution.

## Escalation Triggers

Route a successor platform or architecture decision before:

- adding operators, functions, recursion, loops, dynamic queries, or
  deployer-authored code;
- adding or changing `context.*` properties;
- changing null/coercion semantics;
- changing rule-type meaning or expression-package placement;
- introducing a standalone expression contract schema;
- using expressions as authority for sync scope, assignment containment,
  resolver routing, workflow truth, or event mutation.
