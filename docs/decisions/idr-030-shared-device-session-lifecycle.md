---
id: idr-030
title: Shared-device session lifecycle
status: active
date: 2026-06-05
phase: post-phase-4-stabilization
type: decision
reversal-cost: high
touches: [mobile/data, mobile/sync, server/authorization, server/sync]
superseded-by: ~
evolves: IDR-027, IDR-028
commit: ~
tags: [authorization, authentication, sync, mobile, retention, shared-device]
---

# Shared-Device Session Lifecycle

## Context

Shared physical devices create pressure that is separate from assignment scope.
One tablet may be handed from actor A to actor B, but event authorship must
remain bound to the authenticated human actor and normal sync/access must remain
assignment-derived.

IDR-027 resolved production authentication through explicit
`(issuer, subject) -> actor_id` binding and rejected a single local store that
alternates actors without a shared-device design. NW-049 routed shared devices
as authentication/session lifecycle plus local retention partitioning, and
NW-051 kept special read/write access deferred. CDL-030, CDL-031, CDL-032,
CDL-037, and CDL-055 keep authority assignment-derived, sync scope equal to
access scope, authority out of the envelope, local retention separate from
server event history, and scope mechanisms platform-fixed.

## Decision

A shared physical device may support multiple actor sessions only when there is
one active actor session at a time and all actor-scoped local state is separated
by actor.

The minimum safe session switch model is:

1. Stop actor A capture, advisory evaluation, config promotion, normal pull,
   subject-history backfill, and push scheduling before switching away from A.
2. Drain A's unpushed events with A's credential when connectivity and the
   still-valid A session allow it.
3. If A cannot be drained, seal A's actor partition. Sealed data is not visible
   to B and is not pushed with B's credential.
4. Acquire B's credential and refresh `/api/auth/me` or resume a previously
   established B actor session whose actor id came from `/api/auth/me`.
5. Select or create B's actor-local partition before any B-authored event,
   normal pull, subject-history page, or config promotion occurs.
6. Present no actor-scoped local data until the switch is complete.

Unknown actors cannot become writable offline actors merely because a person
selects a name on the device. A writable session requires a server-resolved
actor context from `/api/auth/me` or a previously established actor-local
session whose credential remains valid for that actor. IdP groups, roles,
custom claims, and JWT `actor_id` claims remain non-authority.

## Local Storage Boundary

Local storage for actor-scoped mutable data is per actor, not one shared event
store that alternates actor ids. Purging on every switch is allowed only as a
deployment/security posture; it is not the platform minimum because it can lose
offline unpushed work and provenance. A shared store with UI filtering is
rejected.

Each actor partition owns:

- local event rows and pending-push queues;
- projections and advisory state derived from that actor's authorized local
  event set;
- normal pull watermark and `last_pull_watermark` used for concurrency
  detection;
- subject-history cursors;
- active/pending config package version state and promotion metadata;
- token/session material that remains available for that actor.

The same physical `device_id` may remain the device provenance value in event
envelopes, but sync progress must be logically actor-scoped. Server-side device
progress bookkeeping must not use a device-only watermark that lets actor A's
high watermark cause actor B to skip authorized data, or lets actor B lower or
rewrite actor A's normal live-sync watermark.

Deployment-wide immutable config package blobs may be cached outside an actor
partition only when they contain no actor-scoped data and are treated as
read-only package content. Actor-specific config observation, active/pending
version selection, and promotion state remain actor-local.

## Unpushed Events

Unpushed human-authored events belong to the actor partition where they were
created. They keep their original `actor_ref` and must only be pushed under a
credential that resolves to that same actor. They must not be reauthored,
rewritten, transferred into another actor's queue, or included in another
actor's batch.

If an actor switch happens while actor A has unpushed events:

- the implementation should push them before switch completion when A is online
  and authenticated;
- otherwise A's partition is sealed for later same-actor resume or a future
  explicitly decided recovery path;
- actor B may use the device only after B's partition is selected, with no
  visibility into A's sealed events;
- B's sync and config activity must not run background work over A's partition.

Abandoned sealed partitions, token expiry while events remain unpushed, and any
administrator recovery/export of another actor's local pending work are
retention/security questions. They require BAR-106 or a successor decision
before implementation as a product feature.

## Token And Session Handling

The active bearer credential is part of the active actor session. Switching away
from actor A removes A's credential from active request headers, in-memory sync
state, and active event assembly. Actor B's requests use only B's credential.

Token material retained for a signed-out actor, if any, is stored with that
actor's sealed partition and is unavailable to the next actor. It can be used
only to resume the same actor's session or complete same-actor sync. Retaining
or purging refresh tokens, local unlock factors, and sealed-session encryption
details are implementation and BAR-106 security choices, but they cannot allow
cross-actor request signing or data visibility.

## Auth/Session Versus BAR-106 Retention

This decision defines the auth/session and local partition boundary needed for
shared physical devices. It does not define a complete data expiry, sensitivity,
device decommissioning, local encryption, compliance erasure, or admin recovery
model.

BAR-106/NW-054 remains the route for:

- expiry of old actor partitions after transfer, leave, or exit;
- crash-safe sensitive-data purge policy;
- device retirement, remote wipe, or decommissioning;
- no-local-retention or redacted audit views;
- recovery of abandoned sealed partitions that contain unpushed events;
- regulatory erasure beyond local retention.

This split preserves CDL-037: local purge is device retention policy, not
canonical server event deletion, sync authority, or event mutation.

## Non-Authority Boundary

Shared-device support is not:

- a new geographic, subject-list, activity, query, custom, auditor, or emergency
  scope mechanism;
- IdP group, role, resource claim, custom claim, or JWT `actor_id` authority;
- mobile authoritative rejection of structurally valid state/policy anomalies;
- a normal live-sync watermark rewrite, reset, or history backfill shortcut;
- a broad audit/history read surface;
- emergency override write authority;
- resolver reassignment or auto-resolution;
- an event envelope field/type change.

Events remain authored by the authenticated actor for the active session.
Assignment-derived scope remains the only ordinary read/write authority.

## Implementation Guard Tests

Successor implementation must prove at least:

| Test | Required proof |
|---|---|
| Actor switch refresh | Switching to B calls `/api/auth/me` or resumes a prior B session whose actor id was server-resolved; local actor selection, IdP claims, and JWT `actor_id` cannot create authority. |
| Local partition isolation | After switching from A to B, B cannot see A's local events, projections, subject summaries, pending events, subject-history pages, config promotion state, or advisory state. |
| Unpushed event sealing | A's unpushed events remain in A's sealed partition, retain `actor_ref.id = A`, are absent from B's push batch, and are pushed only when a credential resolves to A. |
| Push binding rejection | If an A-authored event is attempted with B's credential, the server rejects the batch through existing authenticated actor binding before persistence. |
| Watermark isolation | A's normal pull watermark and `last_pull_watermark` do not cause B to skip data; B's watermark does not lower, rewrite, or corrupt A's watermark; subject-history cursors remain actor-local. |
| Token isolation | After switch, every push, pull, config, and subject-history request uses only the active actor's credential; prior-actor tokens are not present in active headers or background sync. |
| Crash-safe switch | An interrupted switch recovers to A active, B active, or no active actor; it never recovers to B with A's actor-scoped local data visible or pushable. |
| Config state isolation | Immutable package blobs may be shared, but active/pending config version and promotion state are per actor and cannot make B inherit A's sync/config observation state. |
| Retention split | Purging an actor partition never deletes canonical server events, rewrites event envelopes, or mutates normal live-sync watermarks. |

## Consequences

A successor implementation should add mobile storage/session changes and any
server `device_sync_state` actor-scoping needed to satisfy this decision. It
does not need new envelope fields, new envelope `type` values, new assignment
payload fields, new scope mechanisms, broad audit APIs, emergency write APIs,
or mobile authoritative rejection.

Existing single-actor mobile behavior remains a valid degenerate case when a
device has only one actor partition. Deployments that do not support shared
physical devices do not need a multi-actor login UX, but they still must not
author events as an actor different from the authenticated actor.

## Traces

- NW-049: access exceptions and shared-device scope exploration.
- NW-051: special read/write access boundary routing.
- NW-052: shared-device session lifecycle decision.
- BAR-104: production OIDC/JWT/Keycloak authority.
- BAR-106: local retention/security successor route.
- CDL-030, CDL-031, CDL-032, CDL-037, CDL-055.
- IDR-027, IDR-028.
