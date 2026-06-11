# Architecture Stewardship Layer Assessment

## Verdict

CONDITIONAL GO

## Executive Summary

The `009` through `015` decision-anchor artifacts are directionally the right operational layer for decision consumption, vocabulary anchoring, gap routing, and change-control triage. They are much easier for future agents to consume than the raw ADR recovery chain, and their decision/vocabulary/gap structure preserves the main negative boundaries: no new envelope fields, no deployer-authored access logic, no deployer-authored state machines, no stored workflow truth, no field-level sensitivity, no device-side triggers, and no scenario pressure promoted into architecture.

They should not yet become the primary stewardship layer without patches.

The blocking issue is not a broad contradiction with ADR-001 through ADR-005. The chain is mostly coherent with its own stated recovery anchors, especially after the `015` count/classification patch. The problem is that the current repository source order has moved on: `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md` is the active architecture authority, `docs/agent-working-surface/baseline-acceptance-register.md` is the current implementation acceptance register, and `docs/agent-working-surface/platform-next-work-backlog.md` holds accepted and future-decision routing. The new artifacts are still anchored primarily to `.review/001.../002-phase0-decision-register.md` and `.review/001.../008-authoritative-architecture-map.md`, and they omit or stale-classify several accepted post-Phase-4 IDR/BAR/NW decisions.

Recommended adoption path:

1. Keep the CDL authoritative and BAR authoritative for implementation standing.
2. Promote `009` through `015` only as a derived consumable stewardship layer.
3. Patch the layer to cite CDL IDs, current BAR rows, and active IDRs/NW rows.
4. Fix stale role-action, production-auth, assignment-admin, subject-history, shared-device, and current access-exception routing.
5. Add an index or split for `013` so stable routing rules are not mixed with volatile known-gap examples.

After those patches, the layer can become the preferred first consumable for humans and agents, provided every structural claim still routes back to CDL and every "current baseline" claim routes back to BAR/backlog evidence.

## Source Files Inspected

Primary requested pass-chain files:

- `.review/001-architecture-decisions-listing-passes/001-recovery-strategy.md`
- `.review/001-architecture-decisions-listing-passes/002-phase0-decision-register.md`
- `.review/001-architecture-decisions-listing-passes/003-phase1-adr2-identity-conflict-recovery.md`
- `.review/001-architecture-decisions-listing-passes/004-phase2-adr3-auth-sync-recovery.md`
- `.review/001-architecture-decisions-listing-passes/005-phase3-adr4-config-boundary-recovery.md`
- `.review/001-architecture-decisions-listing-passes/006-phase4-adr5-state-progression-recovery.md`
- `.review/001-architecture-decisions-listing-passes/007-phase5-cross-lineage-vocabulary.md`
- `.review/001-architecture-decisions-listing-passes/008-authoritative-architecture-map.md`
- `.review/001-architecture-decisions-listing-passes/009-decision-anchor-extraction-charter.md`
- `.review/001-architecture-decisions-listing-passes/010-candidate-architecture-decision-inventory.md`
- `.review/001-architecture-decisions-listing-passes/011-core-architecture-decision-records.md`
- `.review/001-architecture-decisions-listing-passes/012-vocabulary-anchor-map.md`
- `.review/001-architecture-decisions-listing-passes/013-gap-routing-playbook.md`
- `.review/001-architecture-decisions-listing-passes/014-architecture-decision-coherence-audit.md`
- `.review/001-architecture-decisions-listing-passes/015-decision-anchor-correction-patch.md`

Current repo routing and authority files:

- `AGENTS.md`
- `docs/status.md`
- `docs/implementation/module-interfaces.md`
- `docs/agent-working-surface/README.md`
- `docs/agent-working-surface/baseline-acceptance-register.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/architecture-rationale-and-routing-companion.md`
- `docs/agent-working-surface/escape-hatch-register.md`
- `docs/architecture/adrs-decisions-canonical-ledger/README.md`
- `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md` through `scripts/query_cdl.py`

Decision, contract, and implementation-facing references checked where relevant:

- `contracts/flag-catalog.md`
- `docs/decisions/idr-021-role-action-enforcement-model.md`
- `docs/decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md`
- `docs/decisions/idr-024-multi-axis-assignment-containment.md`
- `docs/decisions/idr-025-pattern-definition-contract-and-delivery.md`
- `docs/decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md`
- `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
- `docs/decisions/idr-028-production-principal-binding-administration.md`
- `docs/decisions/idr-029-assignment-admin-command-capability.md`
- `docs/decisions/idr-030-shared-device-session-lifecycle.md`

## Consistency Findings

- Issue: The new layer's source hierarchy is not the current repo source hierarchy.
- File/section: `009-decision-anchor-extraction-charter.md` Context Capsule and section 2; `011` Context Capsule; `013` Context Capsule; `014` Source Hierarchy Audit; `015` Context Capsule.
- Conflicting or missing source: `docs/architecture/adrs-decisions-canonical-ledger/README.md` and CDL-000 state that the CDL is the single authoritative architecture decision surface. `docs/agent-working-surface/README.md` orders CDL first, then the rationale companion, escape hatch register, contracts, IDRs, and BAR for status. The new layer names `002` and `008` as primary/top-level anchors and does not route through CDL/BAR as current sources.
- Severity: blocking
- Classification: operational policy gap
- Recommended closure path: Patch the context capsules and reading rules to say the layer is derived and subordinate to CDL/BAR. Add CDL ID anchors to each DEC or a DEC-to-CDL map. Only after that, update `docs/agent-working-surface/README.md`, `docs/status.md`, and possibly `AGENTS.md` to point agents to the layer as a consumable index, not as authority.

- Issue: `011` is well structured, but it is not rigorously anchored to CDL row IDs.
- File/section: `011-core-architecture-decision-records.md`, all DEC records.
- Conflicting or missing source: CDL rows CDL-001 through CDL-056 are the current canonical anchors. `011` mostly cites `002`, `003-008`, and ADR-derived recovery anchors, not the current ledger IDs.
- Severity: blocking
- Classification: operational policy gap
- Recommended closure path: Add `CDL anchors:` to each DEC record, or create a separate compact DEC-to-CDL mapping table consumed by `011`, `012`, and `013`. Keep `002` and `008` as lineage, not current authority.

- Issue: Role-action routing is stale.
- File/section: `013-gap-routing-playbook.md` section 8.4; `012-vocabulary-anchor-map.md` "role permits action" and "role-action table artifact"; `011` DEC-AUTH-01 open follow-up.
- Conflicting or missing source: IDR-021 defines the canonical activity `roles` object, server-side `role_stale` evaluation, and mobile advisory-only behavior. IDR-023 narrows activity role-actions to `capture`, `review`, `alert`, `task_created`, and `task_completed`, and excludes `assignment_changed`. BAR-010 accepts config package delivery and validation evidence for role actions.
- Severity: blocking
- Classification: platform-spec detail gap
- Recommended closure path: Patch `013` section 8.4 to say the Phase 4 activity role-action artifact is accepted, while finer action vocabularies remain future detail. Patch `012` to remove "exact role-action artifact open" as a blanket statement. Patch `011` DEC-AUTH-01/DEC-CONFIG-08 to distinguish accepted activity role-action mapping from future refinements.

- Issue: Assignment administration authority is missing from the decision-anchor layer.
- File/section: `011` AUTH decisions; `012` AUTH vocabulary; `013` authorization/access gap routes.
- Conflicting or missing source: IDR-029 and NW-050 accept platform-owned `assignment_admin.create` and `assignment_admin.end` command capabilities outside `activities[*].roles`, backed by deployment-configured `assignment_admin_capabilities` and same-assignment command-plus-containment enforcement.
- Severity: blocking
- Classification: architecture decision gap
- Recommended closure path: Add an accepted current-baseline overlay under AUTH, or add/update a DEC-AUTH record for assignment-admin command capability as an accepted IDR-level extension. Route future assignment-admin changes through IDR-029/NW-050, not generic role-action table gaps.

- Issue: Production-auth authority is absent.
- File/section: `011` AUTH/IDENTITY decisions; `012` vocabulary map; `013` auth and access gap routes.
- Conflicting or missing source: BAR-104, IDR-027, IDR-028, NW-037, NW-038, and NW-040 accept explicit `(issuer, subject) -> actor_id` principal binding, OIDC/JWKS validation, deployment-managed binding provisioning, active binding projection/support rows, and group/claim/JWT `actor_id` non-authority.
- Severity: blocking
- Classification: architecture decision gap
- Recommended closure path: Add production-auth current-baseline anchors to `011`/`012`/`013`, probably as an AUTH/IDENTITY overlay. Preserve the negative boundary: IdP groups, roles, resource claims, and JWT `actor_id` remain non-authority without successor decision.

- Issue: Shared-device session lifecycle is stale-classified as implementation-only.
- File/section: `012-vocabulary-anchor-map.md` implementation/tooling exclusion map says shared-device storage partitioning is an implementation concern unless sync/access boundary changes; `013` does not route IDR-030/NW-055.
- Conflicting or missing source: IDR-030 and NW-055 accept a single-active-actor session model, drain-or-seal switching, server-resolved actor refresh/resume, actor-local mutable partitions, actor-scoped watermarks and subject-history cursors, and read-only shared immutable config blobs. BAR-106/NW-054 remains the route for expiry/decommission/recovery/security.
- Severity: blocking for primary-layer adoption; non-blocking for ADR-only recovery use
- Classification: platform-spec detail gap
- Recommended closure path: Patch `012` and `013` to distinguish accepted shared-device actor-session partitioning from still-open retention/security questions. Do not make shared-device partitions a new access authority source.

- Issue: Subject-history backfill acceptance is missing.
- File/section: `011` Sync/Auth/Projection records; `012` sync/projection vocabulary; `013` backfill/audit/pagination routes.
- Conflicting or missing source: BAR-004 and NW-025 accept separate subject-history backfill with independent cursor, per-page authorization, alias behavior, and no normal device watermark mutation. Status and module interfaces state it is not normal live-sync watermark rewrite.
- Severity: blocking for primary-layer adoption
- Classification: platform-spec detail gap
- Recommended closure path: Patch the layer to treat accepted subject-history backfill as current baseline while keeping broad audit/history pull and normal sync watermark rewrites deferred/future.

- Issue: Auto-resolution is architecturally accepted but execution status needs stronger current-baseline caveat.
- File/section: `011` DEC-WORKFLOW-07; `012` auto-resolution terms; `013` sections 8.8 and proposal examples.
- Conflicting or missing source: CDL-053 and CDL-054 accept auto-resolution as L3b policy and flag resolvability as platform-classified. BAR-102 marks auto-resolution execution deferred. IDR-026 and `contracts/flag-catalog.md` say `auto_eligible` does not itself make system the resolver, and active auto policies are not part of the current runtime enforcement pass.
- Severity: non-blocking
- Classification: platform-spec detail gap
- Recommended closure path: Keep DEC-WORKFLOW-07 as architecture, but patch `011`/`013` to explicitly separate accepted mechanism from deferred execution. Future auto-resolution implementation routes through BAR-102/NW-045 and must preserve exact resolver equality and event-emitting resolution.

- Issue: `014` and `015` do not leave a clear post-patch freeze state.
- File/section: `014` sections 19-23; `015` sections 8-11.
- Conflicting or missing source: `011`, `012`, and `013` now appear to carry the 36-count and classification split expected by `015`, but there is no final verification artifact saying the correction was applied and the layer is ready subject to current-state catch-up.
- Severity: non-blocking
- Classification: implementation/tooling gap
- Recommended closure path: Let this `016` serve as the assessment checkpoint. If the layer is patched later, add a short verification section or successor artifact that records `rg` checks for count, classification, closure-path, and stale wording.

## Catch-Up Plan for Existing CDL / Baseline / Backlog / IDR Artifacts

- Artifact: CDL (`docs/architecture/adrs-decisions-canonical-ledger/*`)
- Current role: Architecture authority and canonical decision ledger.
- Proposed new role: Keep as authoritative.
- Why: Current repo docs and CDL-000 explicitly make it the architecture decision source.
- Required patch: Add links from CDL README to the decision-anchor layer only after the layer has CDL ID mappings and current-state caveats.
- Risk if not patched: Agents may treat `.review` recovery files as authority and bypass CDL-000.

- Artifact: ADRs under `docs/adrs/`
- Current role: Retired/verifying lineage.
- Proposed new role: Keep as lineage/reference.
- Why: CDL is already validated against ADRs; the new layer can cite them through CDL/lineage rather than asking agents to reread ADRs.
- Required patch: None for adoption; optional pointer from ADR index to CDL and the new layer.
- Risk if not patched: Low, except agents may continue to over-read retired ADRs.

- Artifact: `.review/001.../002-phase0-decision-register.md` and `008-authoritative-architecture-map.md`
- Current role: Verification anchor and recovered architecture map for the new pass chain.
- Proposed new role: Keep as lineage/reference inside the decision-anchor package.
- Why: Useful for traceability, but not current repo authority over CDL/BAR.
- Required patch: Rename their role in later artifacts from "primary/current authority" to "recovery verification anchor" unless a formal source-order decision promotes them.
- Risk if not patched: Source-order drift and authority confusion.

- Artifact: `.review/001.../003` through `007`
- Current role: Supporting recovered ADR lineage and vocabulary rationale.
- Proposed new role: Keep as lineage/reference.
- Why: They explain why the derived layer says what it says.
- Required patch: Add an index from DEC records to specific lineage sections only where useful.
- Risk if not patched: Low; the main risk is context bloat.

- Artifact: `.review/001.../009` through `015`
- Current role: Draft derived decision/vocabulary/gap layer plus correction patch.
- Proposed new role: Keep and patch as the preferred consumable stewardship layer.
- Why: The format is useful and future-agent-friendly.
- Required patch: Add CDL anchors, current BAR/IDR/NW catch-up, stale route fixes, and source-order caveats.
- Risk if not patched: The layer will misroute active auth, assignment-admin, shared-device, subject-history, and current implementation-status questions.

- Artifact: `docs/agent-working-surface/baseline-acceptance-register.md`
- Current role: Current implementation acceptance truth.
- Proposed new role: Keep as authoritative for implementation standing.
- Why: It records acceptance evidence, tests, and deferred/future status.
- Required patch: Add backlinks to the decision-anchor layer only after catch-up, not before.
- Risk if not patched: Agents may infer implementation status from architecture records.

- Artifact: `docs/agent-working-surface/platform-next-work-backlog.md`
- Current role: Active next-work routing and accepted/future-decision route log.
- Proposed new role: Keep as authoritative routing/status backlog.
- Why: It contains accepted NW-037 through NW-056 facts missing from the new layer.
- Required patch: Add a pointer to the gap-routing layer after the layer incorporates current NW routes.
- Risk if not patched: Future work may ignore accepted successors and reopen settled routes.

- Artifact: `docs/decisions/` IDRs
- Current role: Active implementation decision records and current-decision supplements.
- Proposed new role: Keep as current decision provenance; merge durable outcomes into the layer.
- Why: IDR-021 through IDR-030 contain accepted decisions not represented in the ADR-only recovery layer.
- Required patch: Add a current-IDR overlay or extend `011`/`012`/`013` with accepted IDR facts.
- Risk if not patched: The layer remains pre-current for production auth, assignment administration, role-actions, pattern delivery, resolver routing, and shared devices.

- Artifact: `docs/agent-working-surface/architecture-rationale-and-routing-companion.md`
- Current role: Accepted non-authoritative rationale and routing companion.
- Proposed new role: Keep, then cross-link or partially merge with `013`.
- Why: It already covers irreversibility, current source order, and do-not-promote reminders that `013` needs.
- Required patch: Decide whether `013` supersedes portions of its routing workflow or links to it as companion context.
- Risk if not patched: Duplicate routing rules may diverge.

- Artifact: `docs/agent-working-surface/escape-hatch-register.md`
- Current role: Active measured-evolution routing register.
- Proposed new role: Keep as authoritative escape-hatch routing surface.
- Why: `013` should not silently activate escape hatches.
- Required patch: Cross-link from `013` architecture escalation triggers to relevant EH entries.
- Risk if not patched: Measured escape-hatch triggers can be mistaken for backlog permission.

- Artifact: Legacy Phase 4 review/evidence drafts
- Current role: Superseded drafts.
- Proposed new role: Supersede with pointer or archive.
- Why: Current working surface already warns not to reconstruct truth from chronology.
- Required patch: None unless adoption of the new layer requires an archive index.
- Risk if not patched: Low if current source-order docs remain clear.

- Artifact: Contracts under `contracts/`
- Current role: Process-boundary and runtime contract sources.
- Proposed new role: Keep as authoritative for cross-boundary shape/protocol/schema facts.
- Why: Architecture layers should route to contracts, not replace them.
- Required patch: Add contract anchors to relevant DEC/gap records where current runtime contracts exist.
- Risk if not patched: Agents may infer wire/schema details from prose.

## Missing Findings and Routing

- Finding: CDL-000 canonical surface rule is missing from the new layer.
- Source: CDL README and CDL-000.
- Does it map to accepted ADR / authoritative map? yes
- If yes: target artifact and patch location: `009` source hierarchy; `011` Context Capsule; `013` reading rules; optional DEC-to-CDL map.
- If no: route as a gap using the gap classification taxonomy: not applicable
- Validation needed: Verify every DEC has CDL anchors or a mapped CDL row group.
- Closure path: Operational/stewardship patch before source-order adoption.

- Finding: Production OIDC/JWT/Keycloak authority is accepted but absent.
- Source: BAR-104, IDR-027, IDR-028, NW-037, NW-038, NW-040.
- Does it map to accepted ADR / authoritative map? partial
- If yes: target artifact and patch location: `011` AUTH/IDENTITY overlay; `012` vocabulary for principal binding, actor context, group/claim non-authority; `013` production-auth gap routing.
- If no: route as a gap using the gap classification taxonomy: architecture decision gap if new authority beyond BAR-104 is proposed.
- Validation needed: Confirm no wording makes IdP groups, roles, resource claims, or JWT `actor_id` direct authority.
- Closure path: Merge accepted current decision into layer; route future online admin API/group-claim authority as successor decisions.

- Finding: Activity role-action mapping is accepted and should not be treated as wholly open.
- Source: IDR-021, IDR-023, BAR-010, NW-041.
- Does it map to accepted ADR / authoritative map? partial
- If yes: target artifact and patch location: `011` DEC-AUTH-01/DEC-CONFIG-08; `012` "role permits action"; `013` section 8.4.
- If no: route as a gap using the gap classification taxonomy: platform-spec detail gap for finer action vocabularies.
- Validation needed: Ensure `assignment_changed` remains outside `activities[*].roles`.
- Closure path: Patch stale "exact role-action tables are not defined" language.

- Finding: Assignment-admin command capability is accepted.
- Source: IDR-029 and NW-050.
- Does it map to accepted ADR / authoritative map? partial
- If yes: target artifact and patch location: `011` AUTH records or current-baseline overlay; `012` vocabulary; `013` assignment-admin/access gap routing.
- If no: route as a gap using the gap classification taxonomy: architecture decision gap only for authority changes beyond IDR-029.
- Validation needed: Preserve no new envelope fields/types, no assignment payload fields, no IdP claim authority, no activity role-action authority for assignment administration.
- Closure path: Add accepted IDR-029/NW-050 facts to the layer.

- Finding: Shared-device actor session and partition boundary is accepted and implemented.
- Source: IDR-030, NW-052, NW-055, BAR-104, BAR-106.
- Does it map to accepted ADR / authoritative map? partial
- If yes: target artifact and patch location: `012` implementation/tooling exclusion map; `013` shared-device/retention routes.
- If no: route as a gap using the gap classification taxonomy: operational policy or platform-spec detail for retention/security beyond IDR-030.
- Validation needed: Preserve that partitions are not new authority, do not rewrite normal watermarks, and do not add new scope mechanisms.
- Closure path: Patch current baseline distinction: session partitioning accepted; expiry/decommission/recovery/security remain NW-054/BAR-106.

- Finding: Subject-history backfill is accepted as a separate sync/projection repair surface.
- Source: BAR-004, FP-005 resolved status, NW-025, module interfaces.
- Does it map to accepted ADR / authoritative map? partial
- If yes: target artifact and patch location: `011` Sync/Auth/Projection records; `012` sync vocabulary; `013` backfill/audit routes.
- If no: route as a gap using the gap classification taxonomy: platform-spec detail gap for audit/history pull beyond current subject-history.
- Validation needed: Confirm no normal sync watermark mutation and per-page authorization.
- Closure path: Patch accepted subject-history backfill separately from future broad audit/historical pull.

- Finding: Config package contract hygiene and pattern definition delivery are accepted runtime contracts.
- Source: BAR-010, NW-031, NW-034, IDR-025, `contracts/config-package.schema.json`, `contracts/pattern-definition.schema.json`, `contracts/patterns/*.json`.
- Does it map to accepted ADR / authoritative map? yes
- If yes: target artifact and patch location: `011` DEC-CONFIG-08 and DEC-WORKFLOW-02; `012` config/pattern vocabulary.
- If no: route as a gap using the gap classification taxonomy: not applicable
- Validation needed: Keep platform pattern definitions platform-owned and delivered by config package; keep deployer shapes separate from platform payload contracts.
- Closure path: Add contract anchors and BAR evidence pointers.

- Finding: Platform payload schemas are runtime contracts, not deployer-authored shape rows.
- Source: BAR-005, FP-010 resolved status, `contracts/shapes/*.schema.json`.
- Does it map to accepted ADR / authoritative map? yes
- If yes: target artifact and patch location: `011` event/config payload boundary; `012` vocabulary; `013` config/schema routes.
- If no: route as a gap using the gap classification taxonomy: not applicable
- Validation needed: Verify no wording says platform payload schemas are deployer shape registry rows.
- Closure path: Add contract anchor and negative boundary.

- Finding: Current access-exception routing is more specific than the older known-gap table.
- Source: NW-049, NW-051.
- Does it map to accepted ADR / authoritative map? partial
- If yes: target artifact and patch location: `013` sections for auditor/query access, emergency/special writes, retention, and new scope mechanisms.
- If no: route as a gap using the gap classification taxonomy: architecture decision gap for broad audit/history or emergency writes; operational policy gap for ordinary current-scope posture.
- Validation needed: Preserve simple current-scope auditor visibility as ordinary assignments/config, and keep broad audit/history/emergency bypasses deferred.
- Closure path: Update known-gap examples to the current accepted split.

- Finding: Operational UX/product layering companion is accepted but non-authoritative.
- Source: NW-047 and `docs/agent-working-surface/operational-ux-layering-companion.md`.
- Does it map to accepted ADR / authoritative map? no
- If yes: target artifact and patch location: not applicable
- If no: route as a gap using the gap classification taxonomy: product/problem evidence gap or operational policy gap depending on the proposal.
- Validation needed: Ensure product/UX vocabulary never changes authority, contracts, or runtime semantics.
- Closure path: Cross-link from `013` product/problem routes.

## File Size / Indexing / Split Recommendation

- File: `011-core-architecture-decision-records.md`
- Problem: Large at 2822 lines, but the DEC records are cohesive and need to stay traceable as one decision corpus.
- Proposed structure: Leave as-is, add a compact table of contents/index at the top and add CDL anchors per record.
- What remains canonical: The DEC record text, once patched and subordinate to CDL.
- What becomes reference/example: Candidate disposition and long handoff details can remain reference sections.
- How cross-links should work: DEC index links to DEC sections, CDL IDs, vocabulary terms in `012`, and gap rules in `013`.

- File: `012-vocabulary-anchor-map.md`
- Problem: Useful but long at 1839 lines; open-front vocabulary mixes current accepted facts with older platform-evolution terms.
- Proposed structure: Leave as-is with a table of contents, then patch stale/current terms. Optionally split only if vocabulary churn becomes frequent.
- What remains canonical: Term-to-DEC ownership after patch.
- What becomes reference/example: Implementation/tooling exclusion and open-front vocabulary can become appendix sections.
- How cross-links should work: Each important term should link to primary DEC, current CDL IDs where structural, and relevant BAR/IDR rows where current status matters.

- File: `013-gap-routing-playbook.md`
- Problem: Long at 2665 lines and mixes stable routing rules with volatile known-gap examples. The known-gap section is already stale against IDR-021/029/030 and BAR-104/NW-055.
- Proposed structure: Split stable routing rules from volatile known-gap examples. Keep sections 1-7 and 9-14 as the stable playbook. Move section 8 known gaps to a `013-known-gap-routing-register.md` or equivalent. Keep proposal classification examples either in the stable file or in an examples appendix.
- What remains canonical: Allowed classifications, closure paths, escalation triggers, routing template, and decision tree.
- What becomes reference/example: Known-gap records and proposal examples.
- How cross-links should work: Stable rules link to known-gap IDs; known-gap entries link back to affected DEC/CDL/BAR/IDR/NW anchors and include last-reviewed date.

- File: `014-architecture-decision-coherence-audit.md`
- Problem: Audit record is long enough but stable as provenance.
- Proposed structure: Leave as-is.
- What remains canonical: Audit findings at time of pass 5.
- What becomes reference/example: Not applicable.
- How cross-links should work: Link to `015` correction patch and this `016` assessment.

- File: `015-decision-anchor-correction-patch.md`
- Problem: Patch instructions are useful but not final status.
- Proposed structure: Leave as-is as correction provenance; add no new split.
- What remains canonical: Historical correction instructions.
- What becomes reference/example: It should become reference after a final verification artifact or later patch lands.
- How cross-links should work: Link from `014` and any future freeze/adoption note.

- File: new quick-reference/index
- Problem: Future agents need a low-token entry point if this layer is adopted.
- Proposed structure: Add a short `README.md` or `000-decision-anchor-layer-index.md` in the package with source order, current status, file roles, and "do not use as authority without CDL/BAR" warning.
- What remains canonical: Source order and file roles.
- What becomes reference/example: Detailed rationale remains in `009` through `015`.
- How cross-links should work: Index links to DEC index, vocabulary map, stable gap playbook, known-gap register, CDL, BAR, and backlog.

## Proposed Patch Plan

1. Add source-order caveats.
   - Patch `009`, `011`, `012`, `013`, `014`, and `015` context capsules to say the layer is derived and subordinate to CDL/BAR.
   - Add "CDL is architecture authority; BAR is implementation acceptance authority" to the package entry point.

2. Add CDL anchoring.
   - Add `CDL anchors:` to each `011` DEC record, or create a DEC-to-CDL mapping section.
   - Add a verification command using `scripts/query_cdl.py` or `rg` to prove every DEC has at least one CDL anchor.

3. Patch stale current-state gaps.
   - Update role-action sections for IDR-021/023 and BAR-010.
   - Add assignment-admin command capability anchors for IDR-029/NW-050.
   - Add production-auth explicit principal-binding anchors for IDR-027/028 and BAR-104.
   - Add shared-device session/partition anchors for IDR-030/NW-055.
   - Add accepted subject-history backfill status for BAR-004.
   - Add config package, pattern definition delivery, config-package schema, and platform-payload contract anchors for BAR-005/BAR-010/NW-034/IDR-025.
   - Update access-exception known gaps with NW-049/NW-051 current routing.

4. Clarify deferred execution.
   - Keep auto-resolution as accepted architecture through CDL-053/054.
   - Add BAR-102 caveat that auto-resolution execution is deferred and must not be inferred as current runtime.
   - Keep resolver reassignment future-decision through BAR-103/IDR-026.

5. Split or index `013`.
   - Prefer splitting stable routing rules from volatile known-gap examples.
   - Add last-reviewed dates and source anchors to known-gap entries.

6. Add a package index.
   - Add a short source-order and file-role README for the decision-anchor package.
   - Include stop conditions and links to CDL, BAR, backlog, contracts, and module interfaces.

7. Only after the above, update active repo routers.
   - Patch `docs/agent-working-surface/README.md` and `docs/status.md` to include the decision-anchor layer as a preferred consumable, not as authority.
   - Patch `AGENTS.md` only if this becomes default implementer context. Otherwise keep it as an on-demand architecture-steward surface.

8. Verification checks after patching.
   - `rg -n "35 normalized|35 decision|WORKFLOW\\s*\\|\\s*6" .review/001-architecture-decisions-listing-passes`
   - `rg -n "Exact role-action tables are not defined|ADR-003 settles the access model but defers role-action" .review/001-architecture-decisions-listing-passes`
   - `rg -n "shared-device storage partitioning  \\| Implementation concern" .review/001-architecture-decisions-listing-passes/012-vocabulary-anchor-map.md`
   - `rg -n "BAR-104|IDR-027|IDR-028|IDR-029|IDR-030|BAR-102|BAR-103|BAR-004" .review/001-architecture-decisions-listing-passes`
   - Review all remaining "Platform evolution" usages and confirm they are not gap classification fields.
   - No code test suite is required for documentation-only routing patches unless contract text is changed.

## Risks and Non-Goals

- Do not replace CDL authority with `.review` prose.
- Do not use BAR/NW runtime evidence to create new architecture decisions.
- Do not promote implementation details, database schemas, APIs, queues, caches, SDKs, local storage mechanics, config syntax, UI, or product language into architecture.
- Do not close open fronts silently.
- Do not implement auto-resolution execution, resolver reassignment, general trigger execution, new scope mechanisms, online production binding-admin APIs, field-level sensitivity, expiry/decommissioning, broad audit/history pull, emergency overrides, or IdP group/claim authority through documentation wording.
- Do not make the S00 path require patterns, triggers, custom access code, auto-resolution policy, new envelope fields, or deployer-authored state machines.
- Do not split files only because they are long; split only where volatility and access patterns justify it.

## Final Recommendation

Use the new decision-anchor artifacts as the target operational layer, but only after a current-state catch-up patch. The strongest form is:

```txt
CDL = architecture authority
BAR = implementation acceptance authority
backlog = active route/status authority
decision-anchor layer = preferred derived consumable for DEC lookup, vocabulary anchoring, and gap routing
contracts = process-boundary/runtime contract authority
IDRs/NW artifacts = current accepted decision/evidence provenance until folded into the layer
```

That gives future agents a much better front door without losing the canonical authority and current implementation-status controls that the repo already relies on.
