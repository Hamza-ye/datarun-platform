---
id: idr-018
title: Expression grammar — JSON AST with prefix-operator nodes
status: active
date: 2026-04-19
phase: 3b
type: decision
reversal-cost: high
touches: [server/config, mobile/domain, contracts]
superseded-by: ~
evolves: ~
commit: ~
tags: [configuration, expression, dd, grammar, cross-platform]
---

# Expression Grammar — JSON AST with Prefix-Operator Nodes

## Context

ADR-4 S11 establishes expressions as "operators + field refs, zero functions, two contexts" with a 3-predicate limit. DD-1 (IDR-017) resolved that expressions are external artifacts stored in `expression_rules.expression` as JSONB. The investigation brief (§DD-2) identifies the concrete AST node types as the open question. The serialization format is a Lock — persisted in DB, synced to mobile, evaluated cross-platform by both Java and Dart evaluators.

The grammar is intentionally small: max 3 leaf predicates combined with AND/OR. No recursion, no nesting beyond one logical operator wrapping leaf comparisons. A hand-written evaluator suffices.

## Decision

### AST Node Types

Every expression is a JSON object. There are exactly three node categories: **comparison** (leaf), **logical** (branch), and **reference/literal** (operand).

#### Comparison Nodes (Leaf Predicates)

A comparison node has the operator as its single key. Value is a 2-element array `[left, right]` (except `not_null` which takes a 1-element array).

```json
{ "eq": ["payload.service_availability", "full"] }
{ "neq": ["entity.facility_type", "community_health_post"] }
{ "gt": ["context.days_since_last_event", 90] }
{ "gte": ["payload.staff_present", 1] }
{ "lt": ["payload.staff_present", 3] }
{ "lte": ["payload.age_years", 5] }
{ "in": ["payload.service_availability", ["full", "partial"]] }
{ "not_null": ["payload.followup_notes"] }
```

**Operator semantics**:

| Operator | Operands | Semantics |
|----------|----------|-----------|
| `eq` | `[left, right]` | left == right (type-coerced: string/number/boolean) |
| `neq` | `[left, right]` | left != right |
| `gt` | `[left, right]` | left > right (numeric/date only) |
| `gte` | `[left, right]` | left >= right |
| `lt` | `[left, right]` | left < right |
| `lte` | `[left, right]` | left <= right |
| `in` | `[needle, haystack]` | needle is member of haystack array. haystack can be a literal array or a `multi_select` field reference. |
| `not_null` | `[operand]` | operand is neither null nor absent |

**Null handling**: Any comparison involving a null operand (field not filled, context property unavailable) evaluates to `false`. Exception: `not_null` explicitly tests for null. This is the safe default — hidden fields stay hidden, warnings don't fire on empty data.

#### Logical Nodes (Branch — max depth 1)

A logical node wraps an array of comparison nodes. No nesting of logical within logical.

```json
{ "and": [
    { "eq": ["payload.service_availability", "full"] },
    { "lt": ["payload.staff_present", 3] }
  ]
}

{ "or": [
    { "eq": ["payload.service_availability", "closed"] },
    { "gt": ["context.days_since_last_event", 90] }
  ]
}

{ "not": { "eq": ["payload.needs_followup", true] } }
```

**Constraints** (DtV-enforced):
- `and`/`or`: array of 2-3 comparison nodes. No nested `and`/`or`.
- `not`: single comparison node (not an array, not a logical node).
- Total predicate count per expression: max 3 (the `and`/`or` array length, or 1 for bare comparison/`not`).

#### Operand Types

Operands in comparison arrays are either **references** (strings with namespace prefix) or **literals**.

**References** (resolved at evaluation time):
- `payload.{field_name}` — current form field value (form context)
- `entity.{attribute}` — projected entity attribute (form context only)
- `context.{property}` — one of 7 platform-fixed properties (form context only)
- `event.{field_name}` — event payload field (trigger context only)

**Literals** (inline values):
- String: `"full"`, `"active"`
- Number: `90`, `3`, `12.5`
- Boolean: `true`, `false`
- Array: `["full", "partial"]` (for `in` operator haystack only)
- Null: `null` (for `eq`/`neq` null checks — but prefer `not_null`)

**Reference resolution is namespace-strict**: bare field names (without prefix) are invalid. DtV rejects them.

### Expression Rule Document Structure

The complete JSONB stored in `expression_rules.expression` differs by `rule_type`:

#### `show_condition` — produces boolean

```json
{
  "when": { "neq": ["entity.facility_type", "community_health_post"] }
}
```

Field is **visible** when `when` evaluates to `true`. Hidden when `false`. If evaluation fails (null reference), field defaults to **visible** (safe fallback — don't hide data entry points on error).

#### `warning` — produces boolean + message

```json
{
  "when": {
    "and": [
      { "eq": ["payload.service_availability", "full"] },
      { "lt": ["payload.staff_present", 3] }
    ]
  }
}
```

Warning is **shown** when `when` evaluates to `true`. The `message` text lives in the `expression_rules.message` column (not in the AST). Warning is non-blocking — worker can submit regardless.

#### `default` — produces a typed value

```json
{
  "value": { "gt": ["context.days_since_last_event", 90] }
}
```

The `value` node is evaluated and its result becomes the field's default. Output type depends on the expression:
- Comparison result → boolean (suitable for boolean fields)
- Reference → the referenced value's type (suitable for any matching field type)

For reference-as-value (copy a context property into a field):
```json
{
  "value": { "ref": "context.actor.scope_name" }
}
```

The `ref` node is a value expression that resolves a single reference and returns its value directly.

**DtV check**: The output type of a `default` expression must be compatible with the target field's type. A comparison (`gt`, `eq`, etc.) produces boolean — only valid for boolean fields. A `ref` node's resolved type must match the field type.

### Value Expression Nodes

Beyond conditions (which always produce booleans), the `default` rule type needs value expressions. The grammar adds one node type:

| Node | Format | Output |
|------|--------|--------|
| `ref` | `{ "ref": "namespace.property" }` | The resolved value of the reference (type depends on source) |
| Comparison | `{ "gt": [...] }` etc. | Boolean |

This keeps the grammar minimal. A `default` expression is either:
1. A comparison (boolean output — for boolean fields), or
2. A `ref` node (passthrough — copies a resolved value into the field)

No arithmetic. No string concatenation. No function calls. The evaluator is a value-lookup + comparison engine.

### Operator-Type Compatibility (DtV)

Not all operators work with all field types. DtV enforces:

| Operator | Valid left-operand types | Notes |
|----------|------------------------|-------|
| `eq`, `neq` | all | Universal equality |
| `gt`, `gte`, `lt`, `lte` | integer, decimal, date | Ordering requires comparable types |
| `in` | text, integer, select, multi_select | Set membership |
| `not_null` | all | Presence check |

**`multi_select` field as left operand**: When a `multi_select` field is the left operand of `eq`/`neq`, DtV rejects — use `in` instead. Specifically: `{ "in": ["vaccines", "payload.stockout_items"] }` checks if `"vaccines"` is in the selected array. The evaluator handles the reversed operand order for `in` when the haystack is a field reference.

### Context Properties (7 Fixed)

Form context (`context.*`):

| Property | Type | Source |
|----------|------|--------|
| `context.subject_state` | string | PE: subject lifecycle state (`active`/`archived`) |
| `context.subject_pattern` | string\|null | PE: active pattern name (null until Phase 4) |
| `context.activity_stage` | string\|null | PE: workflow stage (null until Phase 4) |
| `context.actor.role` | string | Assignment: actor's role for this activity |
| `context.actor.scope_name` | string | Assignment: actor's geographic scope name |
| `context.days_since_last_event` | integer | PE: days since last event for this subject+activity |
| `context.event_count` | integer | PE: total event count for this subject+activity |

All pre-resolved at form-open. Static during form fill (C5). Null-safe: if a property can't be computed, it resolves to null.

### Trigger Context (Phase 4 — Wired in Phase 3)

Trigger-context expressions use `event.*` references only:
```json
{ "eq": ["event.service_availability", "closed"] }
```

DtV rejects trigger-context expressions containing `entity.*`, `context.*`, or `payload.*` references. The evaluator implementation supports both contexts (form and trigger) — but triggers don't fire until Phase 4.

### Cross-Platform Evaluation Contract

Both evaluators (Java server, Dart mobile) must produce identical results for:
- Same expression + same resolved values → same boolean/value output
- Null handling: null in comparison → false (except `not_null`)
- Type coercion: string "3" compared to number 3 → coerce string to number, then compare. If coercion fails → false.
- `in` with null haystack → false
- Empty `and`/`or` → invalid (DtV rejects; evaluator returns false as defensive fallback)

**Equivalence test suite**: Shared JSON fixture of 30+ expression/value/expected-result triples. Both evaluators must pass identically (same pattern as E7 projection equivalence from Phase 1).

### Grammar Summary

```
Expression     = Condition | ValueExpr
Condition      = Comparison | Logical
Logical        = { "and": [Comparison, Comparison, ...Comparison?] }
               | { "or":  [Comparison, Comparison, ...Comparison?] }
               | { "not": Comparison }
Comparison     = { op: [Operand, Operand] }   // 2 operands
               | { "not_null": [Operand] }    // 1 operand
ValueExpr      = Condition                    // boolean output
               | { "ref": Reference }         // passthrough value
Operand        = Reference | Literal
Reference      = "payload.{name}" | "entity.{name}" | "context.{name}" | "event.{name}"
Literal        = string | number | boolean | null | string[]
op             = "eq" | "neq" | "gt" | "gte" | "lt" | "lte" | "in"
```

Max predicates per Condition: 3. Max nesting depth: 1 (logical wrapping comparisons). Zero functions. Zero recursion.

## Alternatives Rejected

- **Infix string syntax** (`payload.age > 18 AND context.subject_state = "active"`) — Requires a parser on both platforms. JSON is natively parsed everywhere. The grammar is too small to justify parser complexity.
- **Prefix/S-expression syntax** (`(and (> payload.age 18) (= context.state "active"))`) — Requires custom parser. Not JSON-native. Same objection as infix.
- **MongoDB-style query syntax** (`{"$and": [{"payload.age": {"$gt": 18}}, ...]}`) — More verbose. The `$` prefix convention is MongoDB-specific jargon. Our operators are cleaner without `$`.
- **Allowing nested logical operators** — The 3-predicate budget makes nesting pointless. `and(and(a, b), c)` = `and(a, b, c)`. Flat is simpler for both authors and evaluators.
- **Arithmetic/computation in value expressions** — Violates ADR-4 S11 "operators + field refs, zero functions." The evaluator is a comparison engine, not a calculator. If a deployer needs computed values, they belong in PE (projected attributes) or in the platform code.
- **`set` node for writing multiple field values** — Overly complex for Phase 3. A `default` expression targets one field (the field named in `expression_rules.field_name`). Multi-field side effects would blur L2's "no persistent side effects" constraint.

## Consequences

- The JSON AST format is a **Lock** — changing it requires data migration in both DB and mobile storage, plus two evaluator rewrites.
- Evaluator implementations are **Lean** — they sit behind the Lock format and can be refactored freely as long as they produce identical results for the same AST+values input.
- DD-3 (config package) will include expression rules serialized in this format within the `"expressions"` section of the package.
- DtV gains type-compatibility checks: validates operator usage against field types, and validates `default` expression output type against target field type.
- The `ref` value node is the only way to produce non-boolean defaults. This keeps the grammar minimal while supporting the "copy context value into field" pattern.
- Phase 4 trigger expressions use the same grammar with a restricted reference namespace (`event.*` only). No grammar extension needed.

## Traces

- ADR: ADR-4 S11 (operators + field refs, zero functions, two contexts), S13 (3-predicate budget)
- Constraint: C5 (PE → EE context resolution), C12 (ShR → EE field definitions), C17 (EE → TE boolean evaluation)
- Depends on: IDR-017 (expression_rules table, L1/L2 separation)
- Walk-through: [s01-facility-observation-config-pipeline.md](../walk-throughs/s01-facility-observation-config-pipeline.md) §Stage 3
- Files: `server/src/main/java/dev/datarun/server/config/ExpressionEvaluator.java`, `mobile/lib/domain/expression_evaluator.dart`, `contracts/expression.schema.json`
