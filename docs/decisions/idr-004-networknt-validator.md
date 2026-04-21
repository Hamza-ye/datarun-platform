---
id: idr-004
title: networknt json-schema-validator for envelope validation
status: active
date: 2026-04-16
phase: 0a
type: decision
touches: [server/sync]
commit: 40bca48
tags: [validation, dependency]
---

# networknt json-schema-validator for envelope validation

## Context

Need to validate incoming events against `envelope.schema.json` (Draft 2020-12) in the push endpoint.

## Decision

`com.networknt:json-schema-validator:1.4.0`. Supports Draft 2020-12, integrates with Jackson ObjectMapper.

## Alternatives Rejected

- **Everit JSON Schema** — does not support Draft 2020-12
- **Justify** — less mature

## Traces

- Files: server push endpoint validation layer
