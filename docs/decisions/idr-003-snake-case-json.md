---
id: idr-003
title: snake_case JSON response convention
status: active
date: 2026-04-16
phase: 0a
type: decision
touches: [server, contracts, mobile/data]
commit: 40bca48
tags: [convention, api]
---

# snake_case JSON response convention

## Context

Java record fields use camelCase (`eventCount`), but the envelope schema uses snake_case (`sync_watermark`, `device_id`). API responses must match the envelope convention.

## Decision

`@JsonProperty` annotations on record fields for snake_case serialization. All JSON API responses use snake_case, matching the envelope schema.

## Alternatives Rejected

- **Global ObjectMapper config** — affects all serialization including internal
- **Separate DTO classes** — unnecessary indirection for records
- **Java snake_case fields** — violates Java naming conventions

## Consequences

- Every new API response type must use `@JsonProperty` for snake_case
- Mobile JSON parsing expects snake_case uniformly
- Convention is established project-wide from Phase 0

## Traces

- Files: SubjectSummary, all API response types
