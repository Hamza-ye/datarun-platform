---
id: idr-006
title: Server-rendered admin via Thymeleaf
status: active
date: 2026-04-17
phase: 0c
type: decision
touches: [server/admin]
commit: 6295281
tags: [admin, ui]
---

# Server-rendered admin via Thymeleaf

## Context

Phase 0c spec says "minimal Angular app OR server-rendered pages (decide at start of 0c)." Need two read-only views: subject list and event timeline.

## Decision

Server-rendered HTML via Spring Boot + Thymeleaf. Two templates added to the existing server JAR.

## Alternatives Rejected

- **Angular SPA** — adds third codebase, Node/npm toolchain, CORS config, separate deployment for two read-only pages. Not justified at this stage.

## Consequences

- Thymeleaf is a Phase 0 expedient for admin visibility
- Future phases may replace with a proper admin frontend if richer interaction is needed
- Does not commit the project to Thymeleaf long-term
