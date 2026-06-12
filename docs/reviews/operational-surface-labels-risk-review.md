# Operational Surface Labels Risk Review

Status: human review / workshop guardrail

Date: 2026-06-12

Role: Workshop Lead / Delivery Evidence Facilitator

Authority: none. This review does not change CDL, contracts, BAR, NW, status,
schemas, APIs, code, or implementation authority. It routes a product-risk
concern so future packets do not bury it.

## 1. Executive Verdict

**Classification:** Platform-spec drift risk.

The accepted architecture and active working surface do not define
coordinator/setup owner, field user, supervisor/reviewer, operator/admin,
support role, or auditor as architecture primitives. The scenario packets mostly
frame them correctly as personas by operational surface.

The risk is downstream. Candidate 1 and workshop language repeats these labels
as target users and journey owners. Without an explicit guardrail, a planner or
implementation agent could turn them into fixed product modules, hard role
categories, config namespaces, or implementation slices. That is more than a
wording-only risk because Candidate 1 platform-spec packets are the next
planning step.

No evidence found in this pass shows current code, contracts, CDL anchors, or
active BAR/NW standing hard-coding those persona labels.

## 2. Evidence Table

| Source | Current wording / pattern | Why it is risky | Correct classification | Recommended correction |
|---|---|---|---|---|
| `docs/scenarios/scenario-user-fit-packets/*` | Persona sections use labels such as field recorder, supervisor/reviewer, coordinator/administrator, and auditor/external reviewer. | Mostly safe because the heading says "personas by operational surface", but repeated nouns can be copied without the boundary. | Wording risk. | Preserve "operational surface" framing and add a shared translation rule in the README. |
| `docs/scenarios/scenario-user-fit-packets/scenario-user-fit-synthesis-across-s00-s01-s06-s06b-access-control-S19.md` | "A coordinator can...", "A field user...", "Supervisors and coordinators..." | Safe as product/problem evidence, but could become hard product areas when converted to Candidate 1 spec. | Platform-spec drift risk. | Convert to "acting as setup owner/field recorder/reviewer" in task packets and require authority backing. |
| `docs/scenarios/scenario-user-fit-packets/foundational-product-fit-readiness-and-validation-matrix.md` | Product-fit claims refer to setup owners, field workers, supervisors, coordinators, and auditors. | The matrix is already clear that architecture fit is not product validation, but it did not name persona hardening as a separate check. | Wording risk / product evidence gap. | Use validation questions to test the acting context and avoid fixed persona modules. |
| `docs/scenarios/scenario-user-fit-packets/access-control-focused-user-fit-packet.md` | Concrete exemplar names coordinator, worker, supervisor, and external auditor. | The packet correctly warns that auditor/query access is not baseline-accepted, but concrete role names can look like platform roles. | Operational-policy gap and architecture-decision gap for broad auditor/query access. | Keep ordinary assignment-derived access for simple current-scope visibility; route broad audit/query scope through GAP-AUTH-01/NW-053 style work. |
| `docs/workshops/first-deployment/control.md` | Stage 2 originally used target-user rows for coordinator/setup owner, field user, supervisor/reviewer, operator/admin, and support role. | This is the main downstream risk because it feeds Candidate 1 planning. | Platform-spec drift risk. | Recast as operational surface labels and state that authority is projected from actor, assignment, role, scope, time, and activity/context. |
| `docs/workshops/first-deployment/stage-3-ux.md` | Journey map uses coordinator, field user, and supervisor labels. | UX labels are appropriate, but task packets need backing constructs before implementation. | Implementation/tooling drift risk if unguarded. | Add UX boundary that persona labels are screen/validation language only. |
| `docs/workshops/first-deployment/stage-8-task-packet-backlog.md` | Packet header did not force agents to declare persona labels and backing authority. | Future implementers could receive a "Coordinator setup" or "Supervisor review" packet and infer a module. | Platform-spec drift risk. | Add required header field and stop condition for persona-label hardening. |

## 3. Boundary Clarification

Correct interpretation:

```txt
The labels are operational surfaces/persona lenses.
They are not actor identity categories.
They are not authority primitives.
They are not fixed product modules.
They are not hard UI boundaries.
They are not implementation service boundaries.
```

Authority should be described as:

```txt
actor + active assignment + role + scope + time + activity/context
-> available actions and visible data
-> projected operational surface
```

A person can act through different surfaces in different contexts if the
accepted authority model allows it. Actor identity answers who authored or is
authenticated. It does not answer what that person may see or do.

## 4. Candidate 1 Impact

Candidate 1 may use these labels for walkthroughs, validation scripts, and
screen copy. It should not create persona-bound platform modules such as
Coordinator Configuration, Supervisor Review Config, Auditor Console, or Field
Worker App.

Candidate 1 should include only the minimal operational capture surfaces backed
by existing accepted constructs. Setup, assignment, work list, capture, sync
state, correction, review, and freshness must remain derived from current
config, assignments, roles/actions, scope, projections, sync state, and flags.

Candidate 1 must not treat auditor/query access as settled if it exceeds
assignment-derived access. It must not treat admin/setup authority as fully
productized just because assignment-admin create/end exists. It must not encode
permanent coordinator, supervisor, auditor, or field-user identities.

## 5. Recommended Wording Replacement

Prefer:

```txt
Operational surface
Acting as setup owner
Acting as field recorder
Acting as reviewer
Acting with audit access
Current authority context
Assigned responsibility
Available actions in this context
Visible working set
```

Avoid:

```txt
Coordinator class
Supervisor module
Auditor role type
Field-user boundary
Coordinator configuration system
Supervisor review config
```

Task packets should use this pattern:

```txt
This packet uses "acting as reviewer" as product language only.
Backing authority: authenticated actor + active assignment + role/action
permission + scope + time + activity/context.
```

## 6. Gap Routing

| Unresolved issue | Classification | Closure path | Route |
|---|---|---|---|
| Persona labels harden into platform-spec categories during Candidate 1 drafting. | Platform-spec detail gap. | Platform-spec detailing. | Add the operational-label header and stop condition to every Candidate 1 packet. |
| Users may not understand acting context versus permanent role identity. | Product/problem evidence gap. | Product discovery / scenario thickening. | Validate wording in Candidate 1 UX walkthroughs. |
| Admin/setup authority may appear fully settled. | Operational policy gap plus platform-spec detail gap. | Operational policy definition plus platform-spec detailing. | NW-050 settles command capability, not production admin UX/auth or operating policy. |
| Broad auditor/query access beyond assignment-derived access. | Architecture decision gap. | Formal architecture decision. | GAP-AUTH-01/NW-053 style route if ordinary time-bounded assignments are insufficient. |
| Persona-specific screens become implementation modules. | Implementation/tooling gap. | Implementation/tooling design. | Require view-model/API assessment before implementation and keep UI composition over accepted constructs. |

## 7. Actionable Recommendations

| Recommendation | Applies to | Classification | Priority | Why |
|---|---|---|---|---|
| Add explicit operational/persona-label guardrail to the scenario packet README and gap-routing vocabulary guardrails. | Agent-readable routing surfaces. | Platform-spec detail gap. | High | Future agents need the rule before drafting specs or packets. |
| Recast Stage 2 target users as acting contexts in the workshop control file. | Workshop synthesis. | Wording risk. | High | This is the most likely source for Candidate 1 copy. |
| Add persona-label backing to the required Stage 8 packet header. | Future task packets. | Platform-spec drift risk. | High | Prevents implementers from receiving hard persona modules by accident. |
| Keep auditor/query access separate from ordinary assignment-derived visibility. | Access, reporting, audit planning. | Architecture decision gap if beyond assignments. | High | Avoids bypassing sync/access equivalence and assignment authority. |
| Validate wording with users before freezing Candidate 1 UX copy. | Product/UX validation. | Product/problem evidence gap. | Medium | The platform may be correct while the product language is confusing. |
| Do not create persona-named code modules, config namespaces, or API routes from Candidate 1 labels. | Implementation planning. | Implementation/tooling gap. | High | Keeps product surfaces derived from accepted authority and view models. |

## 8. Code And Contract Scan Addendum

The code scan did not find fixed product modules or API surfaces named
Coordinator Configuration, Supervisor Review Config, Auditor Console, Field
Worker App, or similar persona-bound modules.

It did find one real runtime authority-adjacent concern:

| Source | Finding | Classification | Routing |
|---|---|---|---|
| `server/src/main/java/dev/datarun/server/integrity/ResolverRoutingService.java` | `isStewardRole` first accepts a configured `review` action for the event activity, then falls back to role-name substrings: admin, supervisor, coordinator, steward, reviewer, manager, lead, resolver. | Implementation/tooling drift risk plus platform-spec detail gap. | Added `GAP-CONFLICT-03` for resolver-steward eligibility policy. Do not productize this fallback as authority; replace or constrain it only through a routed resolver-steward policy or successor decision. |
| `contracts/patterns/*.json` and pattern fixtures | Platform pattern definitions use participant-role slots such as reviewer, supervisor, assigned_worker, sender, and receiver. | Accepted contract vocabulary, not actor identity. | Do not mutate existing v1 pattern contracts casually. Future pattern additions should prefer neutral participant-slot names unless the domain meaning is platform-owned and routed. |
| Server/mobile tests and dev templates | Example assignment roles such as field_worker, supervisor, coordinator, case_manager, and handoff_lead appear in fixtures, tests, and dev-only forms. | Mostly acceptable examples. | Keep them as deployer-configured role strings. They become risky only if production behavior grants authority from the label rather than from assignment scope plus accepted config/policy. |
| `contracts/shapes/subjects_merged.schema.json`, `contracts/shapes/subject_split.schema.json`, and Java comments | Descriptions/comments mention coordinator as the human performing merge/split or flagging. | Wording risk only. | Prefer "authorized actor" in future cleanup, but no behavior currently depends on those words. |

The resolver-steward finding is not safe to patch mechanically in this pass.
IDR-026 requires a nearest eligible human steward when one exists, and current
tests rely on a root/admin fallback for some legacy or activity-null conflict
routes. Replacing the fallback cleanly likely needs one of these routed choices:

- platform-spec detailing that says eligible steward means configured review
  action when an activity exists and explicit no-human-route otherwise;
- a bounded resolver-steward policy using existing configuration;
- a successor decision if the fix introduces a new resolver capability,
  command, event shape, or authority primitive.

Until that route is chosen, future product/spec packets must not claim that
coordinator, supervisor, auditor, manager, or admin labels themselves grant
resolver authority.

## 9. Resulting Edits

This review added or supports guardrail edits in:

- `docs/scenarios/scenario-user-fit-packets/README.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/workshops/first-deployment/role-packets.md`
- `docs/workshops/first-deployment/control.md`
- `docs/workshops/first-deployment/stage-3-ux.md`
- `docs/workshops/first-deployment/stage-7-delivery-plan.md`
- `docs/workshops/first-deployment/stage-8-task-packet-backlog.md`
- `docs/reviews/operational-surface-labels-risk-review.md`

The next workshop action remains FD-PKT-001, but it now carries an additional
packet hygiene requirement: any persona label used in the S06 timing decision
must be stated as an acting context with authority backing.

FD-PKT-001 also carries the S01/S06 boundary requirement: Candidate 1 may stay
first as S01-compatible subject-linked capture, but S06/entity lifecycle must
remain visible with owner, milestone, evidence need, and BAR-105/S06 route
unless Product Manager evidence moves it before Candidate 1 implementation
planning.
