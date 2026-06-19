# Mobile Agent Instructions

Use this file for Flutter, Android, and mobile validation details when touching
`mobile/` or mobile-owned behavior.

## Flutter Tests

Focused test from `mobile/`:

```bash
flutter test test/<file>_test.dart
```

Full mobile test gate from `mobile/`:

```bash
flutter test
```

## Analyzer

`flutter analyze` is currently known-red from pre-existing issues and is not a
hard acceptance gate until those issues are fixed or explicitly baselined. Run
or report it only when the task asks for analyzer work or the local validation
packet requires current analyzer evidence.

## Android Compile

Native Android compile must run from `mobile/android`:

```bash
timeout 900s ./gradlew :app:compileDebugKotlin --console=plain --no-daemon --stacktrace
```

Run this for native Android, platform-channel, auth callback, plugin, Gradle,
manifest, or mobile build changes. It is also useful evidence for broad mobile
PRs when runtime cost is acceptable.

## Test Style

Prefer typed fakes, shared harnesses, or real local stores where practical.
Avoid broad `noSuchMethod` fakes when a real harness can prove the behavior with
similar effort. Keep widget tests focused on user-visible state transitions and
persisted effects rather than incidental copy unless copy is the acceptance
surface.

## Evidence

Report command, cwd, result, test count/duration when available, and any skipped
gate rationale. Pair focused tests with the full mobile gate for accepted mobile
behavior changes.

Use `docs/agent-working-surface/validation-matrix.md` for touched-surface gates
and acceptance evidence format.
