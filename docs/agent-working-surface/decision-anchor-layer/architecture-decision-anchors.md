# Architecture Decision Anchors

Status: active DEC anchor surface

This is the active working-surface version of the `011` DEC layer. It maps working `DEC-*` anchors to current CDL rows and accepted extension inputs. It is not architecture authority; the CDL remains authoritative.

## DEC To CDL Map

| DEC ID | Current CDL anchors | Boundary |
|---|---|---|
| DEC-EVENT-01 | CDL-001, CDL-019 | Append-only event truth and immutable event write model. |
| DEC-EVENT-02 | CDL-019, CDL-020, CDL-021 | Typed immutable event as atomic write, idempotent sync, and client-minted identity unit. |
| DEC-EVENT-03 | CDL-006, CDL-010, CDL-011, CDL-012 | Eleven-field envelope, structural causal metadata, advisory device time, and no derived envelope fields. |
| DEC-EVENT-04 | CDL-007, CDL-013 | Closed envelope `type` vocabulary and `shape_ref` domain discrimination. |
| DEC-IDENTITY-01 | CDL-016, CDL-017, CDL-018, CDL-020 | Reference contracts, typed subject references, actor authorship, and client-generated IDs. |
| DEC-IDENTITY-02 | CDL-010, CDL-011, CDL-020 | Device-scoped causal metadata and advisory device time. |
| DEC-IDENTITY-03 | CDL-022, CDL-023, CDL-024, CDL-025, CDL-026, CDL-027 | Duplicate identity resolution, merge aliasing, split archival, no unmerge, acyclic lineage, and online-only merge/split. |
| DEC-IDENTITY-04 | CDL-002, CDL-029, CDL-033 | Projection order, raw-reference conflict detection, and authorization against original subject scope. |
| DEC-CONFLICT-01 | CDL-003, CDL-035 | Valid state-stale events are accepted and flagged; authorization staleness is surfaced. |
| DEC-CONFLICT-02 | CDL-004, CDL-029, CDL-048 | Detect-before-act, raw-reference detection, and transition violation flagging. |
| DEC-CONFLICT-03 | CDL-028, CDL-054 | Single-writer server-validated resolution and platform-classified resolvability. |
| DEC-CONFLICT-04 | CDL-014, CDL-015, CDL-048, CDL-054 | Platform-bundled flag shapes, deterministic flag identity, transition category, and resolvability. |
| DEC-AUTH-01 | CDL-030, CDL-055 | Assignment-based access and platform-fixed scope mechanism. |
| DEC-AUTH-02 | CDL-021, CDL-031 | Scope-filtered sync and sync/access equivalence. |
| DEC-AUTH-03 | CDL-030, CDL-032 | Assignment-derived access and authority not stored in the envelope. |
| DEC-AUTH-04 | CDL-034, CDL-055 | Scope-containment assignment creation and fixed scope mechanisms. |
| DEC-AUTH-05 | CDL-037 | Scope contraction is selective-retain/device policy, not canonical event mutation. |
| DEC-CONFIG-01 | CDL-008, CDL-013, CDL-014, CDL-039 | `shape_ref` contract, platform payload shapes, and deployer shape version coexistence. |
| DEC-CONFIG-02 | CDL-009, CDL-056 | Optional `activity_ref` contract and deployer activity configuration. |
| DEC-CONFIG-03 | CDL-018 | Human/system authorship contract. |
| DEC-CONFIG-04 | CDL-039, CDL-040 | Deployer shape model, version coexistence, and full-snapshot storage. |
| DEC-CONFIG-05 | CDL-005, CDL-038 | Mechanism/instance split and four-layer configuration gradient. |
| DEC-CONFIG-06 | CDL-043, CDL-052 | Bounded expression language and bounded `context.*` form context. |
| DEC-CONFIG-07 | CDL-042, CDL-044 | Server-only L3 policy and deploy-time complexity/dependency validation. |
| DEC-CONFIG-08 | CDL-041, CDL-042, CDL-044, CDL-046 | Atomic config packages, server-only policy, deploy-time validation, and sensitivity configuration. |
| DEC-WORKFLOW-01 | CDL-002, CDL-047 | Workflow state is projection-derived and rebuildable. |
| DEC-WORKFLOW-02 | CDL-049, CDL-056 | Platform-fixed Pattern Registry and deployer activity bindings. |
| DEC-WORKFLOW-03 | CDL-050 | Bounded pattern composition. |
| DEC-WORKFLOW-04 | CDL-004, CDL-048 | Detect-before-act and transition violation flagging. |
| DEC-WORKFLOW-05 | CDL-043, CDL-052 | Bounded expression language and closed `context.*` scope. |
| DEC-WORKFLOW-06 | CDL-051 | Source-only flagging and source-chain traversal. |
| DEC-WORKFLOW-07 | CDL-053, CDL-054 | Auto-resolution mechanism and platform resolvability classification. |
| DEC-PROJECTION-01 | CDL-002, CDL-047 | Rebuildable projections and workflow projection state. |
| DEC-PROJECTION-02 | CDL-002, CDL-030, CDL-031, CDL-036 | Reporting/read models derive from events and remain access/scope constrained. |
| DEC-BOUNDARY-01 | CDL-000, CDL-005, CDL-012 | Canonical surface rule, mechanism/instance split, and no derived envelope fields. |
| DEC-BOUNDARY-02 | CDL-000, CDL-005 | S00 simplicity is preserved as a routing guard under the canonical surface and mechanism/instance split. |

## Accepted Extension Inputs

These are folded durable outcomes from current BAR/NW/IDR evidence. They do not supersede the CDL.

| Area | Source anchors | Current standing | Target DEC area |
|---|---|---|---|
| Activity role-action model | IDR-021, IDR-023, NW-041 | Activity role-actions are `capture`, `review`, `alert`, `task_created`, and `task_completed`; `assignment_changed` is assignment administration, not activity work. | DEC-AUTH-01, DEC-AUTH-03, DEC-CONFIG-08, DEC-WORKFLOW-04 |
| Multi-axis assignment containment | IDR-024 | Assignment create/end containment applies across geographic, subject-list, and activity axes with explicit bootstrap/root semantics. | DEC-AUTH-04 |
| Pattern definition contract and delivery | IDR-025, BAR-010, NW-031 | Platform pattern definitions are canonical contract artifacts delivered in atomic config packages under `pattern_definitions`. | DEC-CONFIG-08, DEC-WORKFLOW-02 |
| Resolver routing and canonical resolution | IDR-026, FP-009 | Active conflict categories have designated-resolver routing; canonical resolution is exact designated-resolver equality; unauthorized resolutions are accepted but not canonical. | DEC-CONFLICT-03, DEC-CONFLICT-04 |
| Production auth principal binding | IDR-027, IDR-028, BAR-104, NW-037, NW-038, NW-040 | Production auth maps validated provider principals only through explicit active `(issuer, subject) -> actor_id` bindings; groups, roles, resource claims, and JWT `actor_id` are not platform authority. Binding provisioning is deployment-managed and audited. | DEC-CONFIG-03, DEC-AUTH-01, DEC-AUTH-03 |
| Assignment-admin command capability | IDR-029, NW-050 | `assignment_admin.create` and `assignment_admin.end` are platform-owned command capabilities outside `activities[*].roles`, evaluated from deployment-configured role-to-command policy plus same-assignment containment. | DEC-AUTH-04, DEC-CONFIG-08 |
| Shared-device actor partitions | IDR-030, NW-055 | A shared device has exactly one active server-resolved actor session; mutable local state, pending push, sync progress, subject-history cursors, token/session material, and config state are actor-partitioned. | DEC-AUTH-02, DEC-AUTH-05, DEC-PROJECTION-01 |
| Subject-history backfill | BAR-004, FP-005 | Subject-history backfill is a separate authorized repair surface with independent cursor pagination, per-page authorization, alias behavior, and no normal watermark mutation. | DEC-AUTH-02, DEC-AUTH-05, DEC-PROJECTION-01 |
| Platform payload contracts | BAR-005, FP-010 | Platform-owned payload schemas under `contracts/shapes/*.schema.json` are runtime contracts, not deployer shape rows and not activity-bindable form shapes. | DEC-CONFIG-01, DEC-CONFIG-04 |
| Config package schema hygiene | BAR-010, NW-034 | `contracts/config-package.schema.json` and `contracts/shape-format.schema.json` capture current wire/package hygiene while preserving forward-compatible unknown package keys. | DEC-CONFIG-04, DEC-CONFIG-08 |

## Current Caveats

* CDL-053/CDL-054 accept the auto-resolution mechanism class, but BAR-102 and IDR-026 keep runtime auto-resolution execution deferred until a successor policy/trigger slice.
* Resolver reassignment remains a future decision; IDR-026 preserves explicit no-human-route sentinel behavior rather than implicit reassignment.
* Online production binding-admin APIs, IdP group/claim authority, broad audit/history read surfaces, emergency override writes, new scope mechanisms, retention/decommissioning, and sealed-partition recovery remain future-decision routes unless separately promoted.
