---
id: idr-001
title: Test infrastructure — docker-compose with host networking
status: active
date: 2026-04-16
phase: 0a
type: decision
touches: [server/test, ci]
commit: 40bca48
tags: [testing, docker, infrastructure]
---

# Test infrastructure — docker-compose with host networking

## Context

Testcontainers 1.21.0 bundles docker-java 3.4.2 which sends Docker API v1.32; Docker Engine 29.x requires ≥1.40. Connection fails before any container starts. Separately, Windscribe VPN modifies iptables rules, breaking Docker's DNAT/MASQUERADE chain for port-forwarded containers — TCP handshake completes but data never reaches the container.

## Decision

`docker-compose.test.yml` with `network_mode: host` and `PGPORT: "15432"`. Tests use `application-test.properties` pointing to `localhost:15432`. CI uses GitHub Actions service containers instead (no VPN issue there).

## Alternatives Rejected

- **Testcontainers** — Docker API version incompatible, version negotiation hardcoded
- **Standard port forwarding** — VPN breaks DNAT chain
- **Embedded H2** — different SQL dialect, can't test JSONB or real deduplication

## Consequences

- Tests run against real PostgreSQL (correct dialect, real JSONB)
- Dev environment requires host networking when VPN is active
- Tests use port 15432 locally, 5432 in CI

## Traces

- Constraint: E2 (CI from first commit), E5 (deploy from day one)
- Files: docker-compose.test.yml, application-test.properties
