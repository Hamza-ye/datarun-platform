# FD-PKT-001 Decision Record - S06 Timing For Candidate 1

Status: workshop decision record

Date: 2026-06-13

Assigned roles: Product Manager + steward accountability

Authority: none. This record completes the FD-PKT-001 timing decision for
planning control. It does not authorize implementation, change CDL, BAR, NW,
contracts, schemas, APIs, event vocabulary, runtime behavior, or product scope
by itself.

## 1. Chosen Option

**Option C - Run S06 discovery in parallel before implementation gate.**

Candidate 1 may proceed into product/spec and UX validation as an
S01-compatible slice, but Candidate 1 implementation packets must not dispatch
until early S06 discovery answers whether first deployment needs maintained
known things, lifecycle states, discovered-unit stewardship, or merge/split UX.

## 2. Role Flow Used

Product Manager decision:

- Candidate 1 remains the first product/spec path because the current
  first-deployment promise is reliable assigned capture, optional subject link,
  offline save/sync, correction basics, freshness, and unresolved issue
  visibility.
- S06 is a credible product dependency, not a protocol rejection. The product
  risk is high enough that waiting until after implementation packet dispatch
  would risk rework or false user promises.
- The product-safe timing is parallel discovery with a hard implementation
  gate.

Steward accountability:

- Candidate 1 is S01-compatible only while subject-linked or
  missing-known-thing flows remain unpromoted candidate/capture artifacts.
- S06/entity lifecycle remains BAR-105/NW-021/NW-036 successor work before
  lifecycle implementation.
- No lifecycle state, discovered-unit lifecycle, registry stewardship,
  merge/split UX, new scope mechanism, envelope/type change, durable workflow
  state, or production-readiness claim is authorized by this record.

## 3. Decision Question Answers

1. **Does the first deployment promise require maintaining known things over
   time?**
   Not proven. The known product promise requires capture that may link to
   known things and may surface missing-known-thing evidence. Maintained known
   things are plausible enough to require parallel discovery before
   implementation dispatch.

2. **Is the S01-compatible path enough?**
   Enough for FD-PKT-002 product/spec and UX validation. Not enough for
   implementation dispatch until S06 discovery confirms that candidate/missing
   known-thing handling is honest and sufficient.

3. **Which lifecycle words are day-one requirements?**
   Candidate 1 may use review-oriented words such as candidate,
   missing-known-thing, linked record, unlinked record, needs review, duplicate
   suspected, and latest synced. Words such as active, inactive, retired,
   closed, moved, verified lifecycle state, merge, and split are not day-one
   product truth unless FD-PKT-101 promotes them through BAR-105/S06 routing.

4. **Where does the initial known set come from?**
   Treat source as an open product question. Plausible sources are operator
   import, setup-owner entry, external registry, field discovery, or a mixed
   process. Field discovery can create candidate evidence only until S06 is
   routed.

5. **Risk if S06 waits until after Candidate 1 product/spec freeze?**
   Candidate 1 copy, UX, and data surfaces may quietly imply registry truth,
   lifecycle status, or duplicate/merge ownership that the accepted baseline
   does not support.

6. **Risk if S06 moves before Candidate 1 implementation planning?**
   The first deployment can stall on a large registry/lifecycle design before
   proving the smaller capture/offline/sync product path. It could also
   overdesign platform lifecycle before the product need is evidenced.

7. **Missing evidence?**
   SME validation, examples of the known set source, field discovery rate,
   duplicate handling examples, merge/split policy expectations, lifecycle
   vocabulary, registry stewardship owner, and tests/walkthroughs showing that
   users do not mistake candidate capture for lifecycle truth.

## 4. Candidate 1 Boundary

Candidate 1 may include:

- assigned configured capture;
- optional subject-linked capture over accepted subject refs/history;
- standalone capture;
- unpromoted missing-known-thing or candidate capture;
- append-only correction language;
- local save, waiting to sync, synced, failed sync, latest synced, and needs
  review states;
- unresolved issue visibility over existing flags/projections.

Candidate 1 must not include:

- canonical entity lifecycle;
- active/inactive/retired state truth;
- discovered-unit lifecycle;
- merge/split UX as a product promise;
- registry stewardship workflow;
- new event types, envelope fields, or scope mechanisms;
- production reporting, auth/admin/mobile login, retention/security, conflict
  automation, resolver reassignment, or auto-resolution.

## 5. S06 Placement

S06 becomes a parallel discovery/decision lane before Candidate 1
implementation dispatch.

Required follow-up: **FD-PKT-101 - S06/entity lifecycle discovery and
BAR-105 successor decision seed**.

FD-PKT-101 must answer:

- what a "known thing" is for first deployment;
- source and authority of the initial known set;
- whether field discovery creates a candidate, a registry item, or only review
  evidence;
- whether lifecycle words are needed for day one;
- who stewards duplicates, merges, splits, verification, movement, closure, or
  retirement;
- which contracts/code/tests would be touched if S06 is promoted.

## 6. Packet Impacts

| Packet | Impact |
|---|---|
| FD-PKT-002 | May proceed as Candidate 1 product/spec and UX validation, but must carry an explicit Option C dependency marker and candidate/lifecycle copy tests. |
| FD-PKT-003 | Must include evidence gates proving users understand candidate versus canonical known-thing state, plus a stop gate before implementation dispatch. |
| FD-PKT-004 | Must keep standalone mobile capture as unlinked/candidate evidence and prevent mobile copy from implying lifecycle truth. |
| FD-PKT-005 | Must assess whether Candidate 1 needs only adapter/view composition; any shared contract or S06 data model must route before implementation. |
| FD-PKT-006/007 | No change except production/constrained-deployment claims remain blocked until ops/staging evidence exists. |
| FD-PKT-101 | Opens now as the S06 discovery/decision follow-up under BAR-105 before Candidate 1 implementation dispatch. |

## 7. Required Evidence

- Product/SME walkthrough of standalone capture, subject-linked capture, and
  missing-known-thing capture.
- Vocabulary test for candidate, known thing, linked record, unlinked record,
  needs review, duplicate suspected, latest synced, and correction.
- Examples of known-set source and update ownership.
- Duplicate and merge/split examples, even if the result is "not in Candidate
  1."
- UX walkthrough proving users do not interpret candidate capture as registry
  lifecycle truth.
- Test plan mapping Candidate 1 assertions to current BAR/NW evidence and
  marking S06 assertions as gated.

## 8. Stop Conditions

Stop and report if:

- Candidate 1 copy or UI makes a candidate subject into canonical lifecycle
  truth;
- S06 is renamed as vague later work without owner, route, evidence need, and
  decision point;
- lifecycle states, discovered-unit lifecycle, merge/split UX, or registry
  stewardship are implemented before BAR-105/S06 successor routing;
- Product Manager pressure is dismissed only because current baseline is
  deferred;
- steward/protocol wording replaces the product timing decision instead of
  informing it;
- implementation packets dispatch before FD-PKT-101 answers the S06 dependency
  for Candidate 1.

## 9. Done Definition

FD-PKT-001 is complete with Option C.

FD-PKT-002 may start as product/spec and UX validation. Candidate 1
implementation remains blocked until FD-PKT-002 through FD-PKT-005 are gated
and FD-PKT-101 resolves the S06 dependency or explicitly keeps it out of the
implementation slice.
