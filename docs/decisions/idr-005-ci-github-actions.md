---
id: idr-005
title: CI via GitHub Actions with service containers
status: active
date: 2026-04-16
phase: 0a
type: decision
touches: [ci]
commit: 40bca48
tags: [ci, infrastructure]
---

# CI via GitHub Actions with service containers

## Context

CI from first commit (E2). Tests require real PostgreSQL.

## Decision

GitHub Actions workflow with `postgres:16-alpine` service container. Temurin JDK 17. Maven build + test.

## Consequences

- No Testcontainers in CI — GitHub Actions provides the database directly
- Tests use `localhost:5432` in CI, `localhost:15432` locally (see idr-001)

## Traces

- Constraint: E2 (CI from first commit)
