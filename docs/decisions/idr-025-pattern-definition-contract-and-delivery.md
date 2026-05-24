---
id: idr-025
title: Pattern definition contract and delivery
status: active
date: 2026-05-24
phase: 4-prep
type: decision
reversal-cost: high
touches: [contracts, server/config, server/projection, server/integrity, mobile/data, mobile/domain]
superseded-by: ~
evolves: ADR-005 S4-S6, ADR-009 S1/S3, IDR-019, IDR-020
commit: ~
tags: [workflow, pattern-registry, contracts, configuration, phase-4]
---

# Pattern Definition Contract and Delivery

## Context

ADR-005 S5 requires platform pattern definitions to sync to devices as part of the atomic configuration package, while ADR-009 S3 classifies the pattern mechanism as platform-fixed, deployer-referenced, and not deployer-authored. IDR-020 decided that `activities[name].pattern` is a binding set, but did not settle the canonical file/data format for the platform-owned pattern definitions themselves.

The first Phase 4.4 implementation kept binding metadata in Java code and preserved only activity pattern bindings in the config package. That was enough for deploy-time binding validation, but it would let server and mobile projection implementations drift once executable transition specs are added.

## Decision

Pattern definitions are canonical platform contract artifacts under `contracts/patterns/`, validated by `contracts/pattern-definition.schema.json`.

The server loads those contract artifacts as its Pattern Registry source of truth. Deployer config may bind only refs, shape roles, participant roles, activation roles, and parameters through `activities[*].pattern`; deployer config must not define states, transitions, transition effects, or auto-maintained projection semantics.

The Config Packager emits the referenced platform pattern definitions into the atomic config package under a top-level `pattern_definitions` object. Mobile reads pattern definitions from the config package rather than hardcoding a separate registry. This keeps activity bindings and executable pattern definitions version-aligned under the existing two-slot config model.

Pattern refs remain versioned (`name/vN`). A semantic change to states, transitions, or projection behavior requires a new pattern ref version rather than mutating an existing definition.

## Alternatives Rejected

- **Java/Dart hardcoded registries only** - creates two sources of truth and contradicts the execution-plan projection-equivalence posture.
- **Put patterns under `contracts/shapes/`** - pattern definitions are workflow specs, not event payload schemas.
- **Separate pattern-definition sync endpoint** - weakens config atomicity and can mix activity bindings from one config version with definitions from another.
- **Deployer-authored transition tables in activity config** - violates ADR-005 S5 and ADR-009 S3 by turning L0 assembly into an inner platform.

## Phase 4 Quality Gates

- Every file in `contracts/patterns/` validates against `contracts/pattern-definition.schema.json`.
- Server Pattern Registry loads definitions from packaged contract resources, not from duplicated Java literals.
- Config packages include every referenced platform pattern definition under `pattern_definitions`.
- Mobile preserves and exposes packaged `pattern_definitions` from the active config slot.
- Existing pattern-binding validation continues to reject unknown refs, disabled refs, invalid composition, missing roles, and duplicate transition-bound shape ownership.
- No pattern state projection, `transition_violation`, resolver routing, or auto-resolution is introduced by this delivery slice.

## Consequences

- Phase 4.5 projection can build on a single machine-readable contract shared by server packaging and mobile runtime behavior.
- Pattern definition delivery is a cross-device contract and therefore high reversal-cost once projection consumes it.
- Platform-bundled event payload shapes remain separate under `contracts/shapes/`; any cleanup to load those shapes directly from contract files is a separate contract-hygiene task.

## Traces

- ADR: [ADR-005 S4-S6](../adrs/adr-005-state-progression.md), [ADR-009 S1/S3](../adrs/adr-009-platform-fixed-vs-deployer-configured.md)
- IDR: [IDR-019](idr-019-config-package.md), [IDR-020](idr-020-pattern-state-machine-representation.md)
- Files: `contracts/pattern-definition.schema.json`, `contracts/patterns/*.json`, `server/src/main/java/dev/datarun/server/config/PatternRegistry.java`, `server/src/main/java/dev/datarun/server/config/ConfigPackager.java`, `mobile/lib/data/config_store.dart`
