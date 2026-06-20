# NW-112 PC1 Demo Review And Successor Selection

Status: non-authoritative product-validation artifact
Document type: product_validation_artifact
Source: NW-112; NW-111 synthetic demo walkthrough; NW-110 smoke definition; PC1 PM handoff
Authority: review/selection artifact only; does not add product behavior, runtime behavior, validation policy, CI behavior, real-production approval, or implementation standing
Last reviewed: 2026-06-20

## 1. Purpose

This artifact reviews the NW-111 synthetic demo walkthrough step by step and
selects one bounded successor NW for the next Product Candidate 1 proof gap.

It is a document/product review only. It does not implement the successor,
approve real production, change product scope, or create architecture,
contract, runtime, validation-policy, or CI authority.

## 2. Review Method

The review used the NW-111 demo script, NW-110 smoke definition, PC1 PM
handoff, accepted PC1 product spec, active backlog, and validation matrix.

Each sequence is classified from the documents only:

- `CLEAR`: the sequence is understandable and maps to accepted support.
- `FRICTION`: the sequence is in PC1 scope but needs a bounded follow-up before
  it can serve as a reliable proof/demo step.
- `BLOCKED`: the sequence cannot proceed without an owner decision or accepted
  prerequisite.
- `OUT-OF-SCOPE`: the sequence requires work excluded from PC1 or the NW-112
  packet.

No runtime behavior, UI, test, or production evidence was inferred.

## 3. Step Review Table

| Sequence | Classification | Evidence | Candidate follow-up if needed |
|---|---|---|---|
| 1. Enter Organization | CLEAR | NW-111 maps the step to the accepted PC1 one-Organization/default-Workspace boundary; PC1 spec forbids tenant/workspace selectors. | None. |
| 2. Sign in to web admin | CLEAR | NW-111 maps the step to accepted web-admin browser session and command-gated shell standing. | None. |
| 3. Prepare setup draft | CLEAR | NW-111 maps draft setup to the accepted setup/config first vertical and PC1 setup journey; no actual demo evidence shows JSON editing blocking comprehension. | Setup/config structured-editor polish only if a walkthrough later proves JSON setup blocks comprehension. |
| 4. Validate setup | CLEAR | NW-111 and PC1 spec frame validation as setup issues or a validated candidate, not production approval. | None. |
| 5. Readiness review | CLEAR | NW-111 and PC1 spec frame readiness review as operational context only, not workflow-state truth or production approval. | None. |
| 6. Approve setup | CLEAR | NW-111 maps approval to exact validated content; PC1 spec requires changed content to validate and approve again. | None. |
| 7. Publish setup | CLEAR | NW-111 maps publish to the accepted atomic setup/config package standing. | None. |
| 8. Assign responsibility | CLEAR | NW-111 maps responsibility assignment to accepted assignment admin create/end workflow and PC1 responsibility language. | Assignment proof polish only if a walkthrough later shows responsibility wording is unclear. |
| 9. Field user signs in | CLEAR | NW-111 maps mobile sign-in to accepted mobile external login and server-resolved actor/session standing. | None. |
| 10. Field user gets work | CLEAR | NW-111 maps get-work/sync to accepted mobile get-work, assignment-derived access, and sync standing. | None. |
| 11. Field user sees readiness/missing states | CLEAR | NW-111 maps readiness/missing-state wording to accepted PC1 mobile readiness language. | Mobile readiness polish only if a walkthrough later shows the current wording is hard to follow. |
| 12. Field user captures activity entry | CLEAR | NW-111 maps capture to accepted mobile capture, offline/online work, and append-only event standing. | None. |
| 13. Field user sees saved-local/waiting-to-sync | CLEAR | NW-111 maps local pending state to accepted saved-local/waiting/syncing/synced/failed/retry vocabulary. | None. |
| 14. Field user appends correction | CLEAR | NW-111 maps correction to accepted append-only correction behavior, not record overwrite. | None. |
| 15. Field user syncs | CLEAR | NW-111 maps sync state to accepted mobile sync states and actor/device sync standing. | Smoke automation later only after the manual demo path is stable. |
| 16. Supervisor/reviewer sees latest synced/freshness/attention wording | FRICTION | NW-110 says a minimal operational view remains a successor candidate if current support is insufficient; the PC1 PM handoff says freshness/latest-synced language exists in narrow surfaces but the minimal operational freshness/attention view must be decided or deferred. | NW-113: define minimal scoped operational freshness/attention view. |

## 4. Observed Friction

The only concrete friction in the document review is sequence 16.

The setup, assignment, and mobile steps are understandable from accepted
standing and carry only conditional follow-ups if a hands-on walkthrough later
shows comprehension failures. The final supervisor/reviewer step is different:
it is part of the PC1 product goal, but the current documents explicitly keep
the minimal operational latest-synced/freshness/attention view as a successor
candidate and require care not to drift into reporting/export or conflict
operations.

No step is `BLOCKED`. No reviewed step currently requires real users/data,
real domain selection, architecture/gap routing, reporting/export,
retention/security promises, entity lifecycle, conflict automation, or
tenant/control-plane work.

## 5. Selected Successor

Selected successor:

```text
NW-113 - Define minimal scoped operational freshness/attention view
```

Backlog status: `ready`

Type: `product_validation / product_planning`

Priority: `P1`

User value: this closes the last PC1 demo proof gap by deciding the smallest
supervisor/reviewer observation needed after field sync, while preserving the
boundary that this is not reporting, export, analytics, broad audit, or
conflict operations.

Why now: NW-111 has already made the setup-to-sync script repeatable. The next
manual review needs to know whether the final freshness/attention beat can be
shown, deferred, or routed into one bounded implementation successor.

The selected NW-113 prompt is docs/product-planning only. It does not authorize
runtime implementation.

## 6. Why Not Other Candidates

| Candidate | Decision | Reason |
|---|---|---|
| Setup/config structured-editor polish | Not selected | NW-112 found no document evidence that JSON setup blocks the demo. Select this only if a walkthrough shows setup authoring blocks comprehension. |
| Mobile readiness/capture/correction polish | Not selected | Steps 10-15 map clearly to accepted mobile support. Select this only if a walkthrough shows current mobile states are hard to follow. |
| Assignment proof polish | Not selected | Sequence 8 maps clearly to accepted assignment admin and PC1 responsibility language. Select this only if responsibility wording fails a walkthrough. |
| Vocabulary validation packet | Not selected | The neutral fixture is understandable enough for the synthetic demo review. Domain vocabulary validation should wait until an owner selects a real validation context or examples. |
| Smoke automation later | Not selected | The packet explicitly says not to select smoke automation before the manual demo has been reviewed and the final proof boundary is stable. |
| Minimal freshness/attention view | Selected | It is the only observed friction, has high PC1 proof value, can be bounded without real production, and can be validated as docs-only planning before any implementation route. |

## 7. Successor Acceptance Boundary

NW-113 should exit with one non-authoritative product-validation artifact that:

- defines the smallest supervisor/reviewer observation needed for the PC1 demo
  final beat;
- states whether existing accepted standing is enough, the view should be
  deferred, or one implementation successor should be selected later;
- preserves the line between a scoped operational view and reporting/export,
  analytics, warehouse, broad audit, or conflict operations;
- avoids new product scope, architecture authority, contracts, schemas, sync
  behavior, retention/security promises, entity lifecycle, conflict
  automation, tenant/control-plane work, real users/data, and real-production
  approval;
- records validation as docs-only/product-validation unless a later NW
  separately authorizes implementation.

NW-113 must not implement the view.

## 8. Validation Category

Docs-only product-validation/selection.

Runtime tests are skipped because NW-112 changes only working-surface artifacts,
prompt routing, backlog/status trace, and artifact indexing. It changes no
runtime code, tests, contracts, schemas, migrations, CI behavior, validation
policy, product spec, platform spec, BAR, CDL, or gap register.

## 9. Stop Conditions

Stop and report before NW-113 or any successor work if the route requires:

- real domain or pilot selection;
- real users or real organizational data;
- product-scope change;
- runtime implementation inside NW-112;
- reporting dashboards, exports, imports, warehouses, analytics, or broad read
  APIs;
- retention/security promises;
- entity lifecycle;
- conflict automation, batch review, resolver reassignment, auto-resolution, or
  flag reporting;
- tenant/control-plane work;
- contract, schema, envelope, or sync changes;
- architecture/gap routing.
