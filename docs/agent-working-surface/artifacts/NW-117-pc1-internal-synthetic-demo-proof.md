# NW-117 PC1 Internal Synthetic Demo Proof

Status: non-authoritative product-validation artifact
Document type: product_validation_artifact
Source: NW-117; NW-116 PC1 proof target decision; NW-115 PC1 post-NW-114 demo standing; NW-111 PC1 synthetic demo walkthrough; PC1 PM handoff; accepted PC1 product spec
Authority: owner-review evidence artifact only; does not add product behavior, runtime behavior, validation policy, CI behavior, real-production approval, reporting scope, operational policy, or implementation standing
Last reviewed: 2026-06-21

## 1. Purpose

This artifact records the Product Candidate 1 internal synthetic demo proof
selected by NW-116.

PC1 remains:

```text
synthetic-demo-ready, not real-production-ready
```

The proof reviews all 16 NW-111 sequences with synthetic/non-sensitive data
only. It does not run or approve a managed lab, use real users, use real
organizational data, approve real production, or create implementation scope.

## 2. Proof Boundary

| Boundary | Standing |
|---|---|
| Proof target | Internal synthetic demo. |
| Evidence type | Product-validation / owner-review evidence packet over the accepted NW-111 synthetic walkthrough and accepted post-NW-114 standing. |
| Fixture source | NW-111 domain-neutral demo fixture. |
| Data sensitivity | Synthetic/non-sensitive only. |
| Runtime change | None. |
| Real-production standing | Not approved; still blocked behind a later NW-093 route if real users or real organizational data appear. |

This packet is a reviewable proof artifact. It is not a runtime test report,
manual lab rehearsal, operations rehearsal, contract acceptance, or validation
policy.

## 3. Synthetic Fixture Used

| Fixture item | Synthetic label |
|---|---|
| Organization | `Example Organization` |
| Setup | `Field Activity Setup` |
| Activity | `Site Visit` |
| Work item | `Assigned Visit` |
| Activity entry | `Visit Record` |
| Responsibility | `Assigned route / coverage` |
| Attention item | `Needs review` |

These labels are product-proof labels only. They do not become platform
vocabulary authority, contract terms, schema requirements, domain commitments,
or evidence of real organizational use.

## 4. Sequence Proof

Classification vocabulary:

- `PASS`: the sequence is clear for internal synthetic owner review.
- `FRICTION`: the sequence is in PC1 scope but creates candidate follow-up
  pressure.
- `NOT_RUN`: the sequence was not reviewed in this proof.
- `OUT-OF-SCOPE`: the sequence requires work outside NW-117 or PC1 boundaries.

| Sequence | Classification | Evidence notes | Candidate follow-up pressure |
|---|---|---|---|
| 1. Enter Organization | PASS | The synthetic proof starts in `Example Organization`, matching the accepted one-Organization/default-Workspace PC1 boundary with no tenant or workspace selector. | None. |
| 2. Sign in to web admin | PASS | The web-admin entry beat maps to the accepted browser session and server-resolved actor context standing used by PC1. | None. |
| 3. Prepare setup draft | PASS | `Field Activity Setup` remains a draft setup before delivery to field devices; the JSON-first setup/config vertical remains acceptable for this internal synthetic proof. | None. |
| 4. Validate setup | PASS | Validation is framed as setup issues to fix or a validated candidate, not real-production approval. | None. |
| 5. Readiness review | PASS | Readiness review remains operational review context for the synthetic setup, not workflow-state truth or production approval. | None. |
| 6. Approve setup | PASS | Approval remains bound to exact validated content; changed content would require validation and approval again. | None. |
| 7. Publish setup | PASS | Publish remains one atomic setup/config package under accepted config-package standing. | None. |
| 8. Assign responsibility | PASS | `Assigned route / coverage` explains assigned work without implying that UI labels, IdP groups, or product roles grant authority. | None. |
| 9. Field user signs in | PASS | The mobile sign-in beat maps to accepted server-resolved actor/session alignment for PC1 proof purposes. | None. |
| 10. Field user gets work | PASS | Get-work/sync refreshes setup, responsibility, and visible assigned work inside accepted assignment-derived access boundaries. | None. |
| 11. Field user sees readiness/missing states | PASS | Ready-to-capture, missing setup/forms, missing assignment, retry, and syncing vocabulary remains clear enough for the internal synthetic proof. | None. |
| 12. Field user captures activity entry | PASS | The `Visit Record` capture beat stays inside configured field work and does not imply server acceptance while offline. | None. |
| 13. Field user sees saved-local/waiting-to-sync | PASS | The local/pending beat uses accepted saved-local, waiting-to-sync, syncing, synced, failed, and retry vocabulary. | None. |
| 14. Field user appends correction | PASS | The correction beat remains append-only and does not imply event mutation or overwrite semantics. | None. |
| 15. Field user syncs | PASS | Sync remains actor/device-context scoped; the proof does not claim that every device or actor is current. | None. |
| 16. Supervisor/reviewer sees latest synced/freshness/attention wording | PASS | The accepted NW-114 minimal operational view closes the synthetic demo final beat with latest visible synced work, product-safe freshness wording, and at most one read-only `Needs review` cue without reporting/export or conflict-workflow scope. | None for internal synthetic proof. Reporting freshness/completeness/drilldown remains future-only if selected separately. |

## 5. Overall Standing

All 16 NW-111 sequences are `PASS` for internal synthetic owner review.

No sequence is `FRICTION`, `NOT_RUN`, or `OUT-OF-SCOPE` for this proof packet.
No candidate follow-up pressure is promoted automatically. Setup polish, mobile
polish, assignment proof polish, vocabulary validation, smoke automation,
reporting, retention/security, conflict workflow, entity lifecycle, and
tenant/control-plane work remain separate future routes only if selected.

## 6. Data And Approval Confirmations

| Check | Result |
|---|---|
| Real users used | No. |
| Real organizational data used | No. |
| Domain-specific customer data used | No. |
| Managed lab proof run | No. |
| Real-production approval granted | No. |
| Runtime implementation changed | No. |
| Product or platform specs changed | No. |
| Contracts, schemas, sync, or migrations changed | No. |
| Reporting/export/analytics scope added | No. |
| Retention/security promises added | No. |
| Conflict workflow or automation added | No. |
| Tenant/control-plane work added | No. |

## 7. Owner Route Selection

Recommended owner route after this proof:

```text
proceed to managed lab proof boundary planning
```

Rationale:

- Repeating the internal synthetic demo is not needed because all 16 sequences
  pass with no recorded friction.
- Parking is not useful because the internal synthetic proof has produced
  owner-review evidence.
- NW-093 real-use preparation is not selected because no concrete real users,
  real organizational data, provider, region, jurisdiction, support
  commitment, compliance/security review, or go/no-go trigger is active.
- A managed lab proof is the next useful proof target, but it still needs a
  bounded planning packet before any lab run names environment ownership, lab
  data boundary, support/contact path, acceptance criteria, and stop
  conditions.

Selected successor:

```text
NW-118 - Decide PC1 managed lab proof boundary
```

NW-118 should remain product-planning / owner-decision work. It should not run
a lab, use real users or real organizational data, approve production, or
implement product/runtime changes.

## 8. Stop Conditions Checked

No stop condition fired.

NW-117 did not require or select:

- real users or real organizational data;
- real-production approval;
- reporting dashboards, exports, imports, warehouses, analytics, broad read
  APIs, completeness semantics, or drilldown;
- retention/security/offboarding promises;
- entity lifecycle;
- conflict automation, batch review, resolver reassignment, auto-resolution,
  flag reporting, or conflict workflow;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, sync, validation-policy, CI,
  BAR, CDL, or gap-register changes;
- runtime implementation, mobile code, or server/web-admin implementation.

## 9. Validation Category

Docs-only product-validation / owner-review evidence.

Runtime tests are skipped because NW-117 changes only working-surface
artifacts, prompt routing, backlog/status trace, and artifact indexing. It
changes no runtime code, tests, contracts, schemas, migrations, CI behavior,
validation policy, product spec, platform spec, BAR, CDL, gap register, or
mobile code.
