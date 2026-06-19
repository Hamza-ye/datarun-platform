# Mobile Analyzer Known Issues

Status: active known-red validation record
Document type: validation_control
Owner: product/engineering steward
Source: NW-109; 2026-06-19 Test / CI / Validation Strategy Audit; `flutter analyze` on 2026-06-20
Authority: analyzer standing record only; does not change lint policy, runtime behavior, tests, CI blocking rules, product scope, or acceptance by itself
Last reviewed: 2026-06-20

## Purpose

This record keeps the mobile analyzer standing visible while NW-109 makes the
green mobile gates repeatable in CI. `flutter analyze` remains known-red and
not blocking until it exits 0 or an accepted baseline exists.

## Current Command

```bash
cd /home/hamza/datarun-platform/mobile
flutter analyze
```

## Current Standing

Result on 2026-06-20: known-red, 7 issues found.

The command resolves dependencies, then reports the issues below. This is
expected evidence for NW-109 and must not be treated as a hard CI gate.

## Issue Summary

| File | Count | Rules |
|---|---:|---|
| `lib/data/event_store.dart` | 2 | `curly_braces_in_flow_control_structures` |
| `lib/data/projection_engine.dart` | 1 | `unnecessary_null_comparison` |
| `lib/presentation/widgets/widget_mapper.dart` | 1 | `deprecated_member_use` |
| `test/context_resolver_test.dart` | 1 | `use_null_aware_elements` |
| `test/form_engine_test.dart` | 1 | `unused_local_variable` |
| `test/selective_retain_test.dart` | 1 | `unnecessary_brace_in_string_interps` |

## Owner Route

Use a future analyzer cleanup or analyzer baseline NW before making
`flutter analyze` a blocking gate.

## Removal Condition

Remove this record when `flutter analyze` exits 0 or an accepted analyzer
baseline records the remaining issues, owner, date, and removal condition.
