---
id: idr-002
title: pg_idkit image for development PostgreSQL
status: active
date: 2026-04-16
phase: 0a
type: decision
touches: [server/test]
commit: 40bca48
tags: [database, docker]
---

# pg_idkit image for development PostgreSQL

## Context

Need PostgreSQL 16 with UUID generation support for development.

## Decision

`ghcr.io/vadosware/pg_idkit:0.2.3-pg16.2-alpine3.18-amd64` for dev docker-compose. CI uses `postgres:16-alpine` (UUID generation is client-side in current implementation).

## Consequences

- UUID generation available server-side in dev if needed later
- CI and dev use different images (minimal risk — same PostgreSQL 16 engine)
