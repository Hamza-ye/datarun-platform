# Identity Boundary Control

Status: Assessed boundary-routing input already routed into the baseline/spec path

This document records the assessed routing needed to keep ADR-002's broad exploration shape from becoming broad implementation coupling. It does not re-decide ADR-002, does not use ADR-006-R through ADR-009 as authority, and is not a current authority source. Durable authority and routing belong in `05-decision-gap-register.md`, `07-system-boundary-map.md`, and `20-platform-spec-outline.md`.

The key correction for implementation is that ADR-002's typed reference categories are not all the same kind of identity lifecycle. They emerged because platform events can be about, performed by, authorized through, or attached to different operational referents. A collected record may be about a person, a facility, a household, a resource movement, a campaign, a review, someone's work, or another operational process. That does not mean one "Subject" subsystem owns every referent.

## Source Basis

Primary inputs:

- `../10-adr1-5-rest-state-closure-register.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`
- `../../adrs/adr-002-identity-conflict.md`
- `../../exploration/archive/05-adr2-event-storm-identity.md`
- `../../exploration/archive/07-adr2-phase2-stress-test-results.md`
- `../../exploration/archive/09-adr2-phase3-classification-results.md`

Scenario context checked for referent pressure:

- `../../scenarios/07-resource-distribution.md`
- `../../scenarios/09-coordinated-campaign.md`
- `../../scenarios/14-multi-level-distribution.md`

## Routing Rule

ADR-002 creates a shared reference protocol and closes subject-lineage invariants. It does not authorize implementing one broad identity subsystem that owns actor authority, assignment scope, process workflows, shipment matching, campaign state, conflict lifecycle, or reporting identity views.

Platform-spec sections must split ADR-002 into responsibility slices that preserve later ADR dependencies.

## Accepted ADR-002 Core

The following remain accepted baseline and must not be weakened by this control pass:

- event references are typed
- event, subject, and record identifiers are client-generated UUIDs for offline creation
- `device_id`, `device_sequence`, and `sync_watermark` carry ordering/concurrency semantics
- `device_time` is advisory for display/audit only
- subject merge is alias-in-projection, never physical re-reference
- wrong merge is corrected by split, not unmerge
- split freezes history under the source identity
- subject lineage is acyclic by construction
- merge/split operations are online-only where the baseline requires server validation
- conflict detection uses raw event references before alias projection
- validly structured stale events are accepted and flagged, not rejected

## Reference Categories Are Not Ownership Categories

ADR-002 names four reference categories:

- `subject`
- `actor`
- `process`
- `assignment`

These are envelope/reference categories, not proof that one implementation component owns their full lifecycles.

The implementation-safe reading is:

- `subject`: a reference to the thing the event is primarily about, when the referent has subject-lineage semantics.
- `actor`: a reference to the person or system actor that authored/performed the event.
- `assignment`: a reference to the temporal authority/responsibility binding under which the actor acted.
- `process`: a reference to an operational process instance, such as a shipment, case, campaign, review cycle, or transfer chain, when the event is attached to that process.

Some domains may make a person, household, facility, route, batch, shipment, campaign, or piece of work the main referent of a record. The boundary question is not "is it a subject in ordinary language?" The boundary question is "which platform mechanism owns this referent's lifecycle and which reference contract lets other mechanisms point to it?"

## Responsibility Split

| Responsibility | Owning Boundary | Identity / Lineage Role | Forbidden Coupling |
|---|---|---|---|
| typed reference shape | Event Envelope / Schema | provides reference values for subject lineage to consume | Identity code must not redefine envelope fields |
| `device_id`, `device_sequence`, `sync_watermark` | Event Envelope / Schema | supplies ordering inputs for identity/conflict checks | Identity code must not treat device identity as actor identity |
| subject UUID continuity | Identity / Lineage | owns subject continuity and lifecycle facts | Must not own assignment, process, or workflow semantics |
| subject merge/split/corrective split | Identity / Lineage | owns lineage events, alias projection, and acyclicity | Must not rewrite historical event references |
| alias projection | Identity / Lineage | resolves retired subject IDs for read purposes | Must not run before raw-reference conflict detection |
| raw subject-reference preservation | Identity / Lineage | exposes original refs for audit and conflict checks | Must not hide what the actor actually referenced |
| duplicate-subject detection inputs | Identity / Lineage | supplies candidate lineage facts and raw refs | Must not own domain-specific matching policy by default |
| actor reference | Assignment / Authority / Sync | identity only supplies referenced actor ID, where needed | Must not make actor identity equal device identity |
| assignment reference and scope | Assignment / Authority / Sync | identity supplies refs; authority owns validity and scope | Must not let subject lineage decide access |
| merge/split authorization | Assignment / Authority / Sync | identity exposes operation preconditions | Must not embed authorization policy in lineage component |
| sync distribution of lineage events | Assignment / Authority / Sync | identity emits/consumes events; sync owns delivery scope | Must not assume all devices receive all lineage events |
| process identity for shipments/campaigns/cases | Projection / Workflow State | identity reference protocol allows pointing to process | Must not force process lifecycle into subject lineage |
| pending match | Projection / Workflow State | identity exposes unresolved-reference facts | Must not make shipment/campaign matching a core subject-lineage feature |
| `ConflictDetected` / `ConflictResolved` lifecycle | Flag / Resolution | identity supplies raw refs and lineage state used by detectors | Must not make identity own general flag semantics |
| detect-before-act processing order | Assignment / Authority / Sync plus Flag / Resolution | identity-dependent checks participate in the pipeline | Must not let identity checks trigger downstream policy directly |
| domain conflict rules | Configuration plus Flag / Resolution | identity provides referents and lifecycle state | Must not hard-code deployer business conflicts into lineage |
| reporting identity views | Reporting / Aggregation | consumes projected identity and lineage views | Must not become source of identity truth |

## Identity / Lineage Owns

- subject identity continuity where the referent has subject-lineage semantics
- subject lifecycle facts needed for merge, split, archive, and stale-reference detection
- `SubjectsMerged` semantics as alias-in-projection
- `SubjectSplit` semantics as frozen-history corrective lineage
- lineage acyclicity constraints
- alias projection over subject lineage events
- raw subject-reference preservation for conflict and audit

## Identity / Lineage Does Not Own

- actor provisioning or authentication
- device identity namespace
- assignment validity, role, scope, or authority
- sync delivery scope
- process state machines for shipments, campaigns, cases, reviews, or transfer chains
- pending-match workflows
- conflict resolution lifecycle
- general flag semantics
- deployer-configured domain matching policy
- reporting projections as source of truth

## Dependency-Aware ADR Boundaries

Do not break these later-ADR assumptions:

- ADR-003 assumes assignment-derived access and sync scope as access scope. Identity must expose original subject references and lineage facts without deciding access.
- ADR-003 assumes authority is projection-derived. Identity must not add stored `authority_context` or make lineage events carry immutable authority snapshots.
- ADR-004 assumes platform/deployer configuration boundaries. Identity must not turn domain matching or conflict rules into hard-coded identity behavior unless already platform-fixed.
- ADR-004 assumes deployers configure policy values over platform mechanisms. Identity must not let deployers author lineage algorithms.
- ADR-005 assumes workflow state is projection-derived. Identity must not store process current state or workflow state in identity records.
- ADR-005 assumes source-only flag lineage and detect-before-act composition. Identity must supply raw references and lifecycle facts without becoming the flag cascade owner.

## Candidate Specification Constraints

- Specify a small reference contract before any universal identity-service language.
- Keep subject lineage as its own bounded platform responsibility.
- Treat actor, assignment, and process references as references whose lifecycle owners live elsewhere.
- Expose identity facts through read-only query/projection interfaces: original reference, resolved subject ID, lineage state, archived/split/merged status.
- Require conflict detection language to consume both original references and current projected lineage state.
- Keep alias resolution out of the event store and out of envelope mutation.
- Keep process matching for shipments/campaigns/cases behind workflow or process-pattern boundaries.
- Keep assignment validity behind authorization/sync boundaries.
- Keep domain duplicate-detection policy configurable or separately specified; do not bury it inside subject lineage.

## Open Coupling Risks

- Process identity may get pulled into subject lineage because shipments, campaigns, cases, and reviews can also be "what the data is about."
- Assignment identity may get pulled into subject lineage because events often record who acted under what responsibility.
- Duplicate detection may become domain-specific matching hidden inside identity infrastructure.
- Conflict resolution may become coupled to identity because identity anomalies are common conflict sources.
- Reporting may pressure identity projections to become canonical current truth.
- Pending match may become a generic identity mechanism too early instead of a workflow/process capability.

## Routed Standing

The durable findings from this assessment have been routed into `05`, `07`, and `20`. During platform-spec drafting, carry them as section constraints and cite `05` whenever a section touches an open gap:

1. Keep the first identity spec section limited to reference contract plus subject-lineage contract.
2. Put actor/assignment authority in the authorization/sync spec.
3. Put process identity and pending match in workflow/process-pattern spec work.
4. Put conflict resolution lifecycle in flag/resolution spec work.
