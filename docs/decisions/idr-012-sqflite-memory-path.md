---
id: idr-012
title: sqflite in-memory database path sharing
status: active
date: 2026-04-18
phase: 1c
type: discovery
touches: [mobile/test]
commit: 0e22275
tags: [testing, mobile, sqflite]
---

# sqflite in-memory database path sharing

## Context

Mobile tests using sqflite for the EventStore.

## Finding

sqflite's `inMemoryDatabasePath` (`:memory:`) is shared across EventStore instances due to sqflite's single-instance caching by path. Tests using `:memory:` share state between test cases, causing cross-contamination.

## Resolution

Use unique temp file paths per test + `close()` + delete in `tearDown()`. EventStore constructor accepts optional `dbPath` for test injection.

## Consequences

- All mobile DB tests must use unique file paths, never `:memory:`
- Test teardown must close and delete the database file
