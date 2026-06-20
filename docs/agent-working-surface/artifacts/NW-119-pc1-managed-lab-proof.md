# NW-119 PC1 Managed Lab Proof

Status: non-authoritative product-validation artifact
Document type: product_validation_artifact
Source: NW-119; NW-118 PC1 managed lab proof boundary; NW-117 PC1 internal synthetic demo proof; NW-111 PC1 synthetic demo walkthrough; PC1 PM handoff; accepted PC1 product spec
Authority: owner-review evidence artifact only; does not add product behavior, runtime behavior, validation policy, CI behavior, real-production approval, reporting scope, operational policy, or implementation standing
Last reviewed: 2026-06-21

## 1. Purpose

This artifact records the Product Candidate 1 managed lab proof selected by
NW-118.

PC1 remains:

```text
synthetic-demo-ready, not real-production-ready
```

The proof uses the NW-118 synthetic managed-lab boundary and reviews all 16
NW-111 sequences with synthetic/non-sensitive lab data only. It does not use
real users, real organizational data, production secrets, provider/region
commitments, support commitments, compliance/security approval, continuity
approval, or real-production go/no-go ownership.

## 2. Managed Lab Boundary Used

| Boundary item | Value used in NW-119 |
|---|---|
| Lab organization label | `Example Lab Organization` |
| Environment owner | Datarun deployment owner for the selected lab environment. |
| Operator/contact path | Owner-review lab operator through the repository/owner-review channel for this NW. |
| Data boundary | Synthetic/non-sensitive data only; no real users, no real organizational data, no customer data, and no production secrets. |
| Evidence owner | Product-validation / owner-review evidence agent. |
| Real-production standing | Not approved; still blocked behind NW-093 if real users or real organizational data appear. |

## 3. Evidence Source And Fixture

Managed-lab evidence source:

```text
docs-only owner-review evidence over the NW-118 lab boundary, the NW-111
synthetic walkthrough, and the NW-117 all-PASS internal synthetic proof
```

No live runtime lab command, production deployment rehearsal, operations
rehearsal, real-user walkthrough, real organizational data review, or CI/manual
environment gate was added by NW-119.

Synthetic fixture/data source:

| Fixture item | Synthetic lab label |
|---|---|
| Organization | `Example Lab Organization` |
| Setup | `Field Activity Setup` |
| Activity | `Site Visit` |
| Work item | `Assigned Visit` |
| Activity entry | `Visit Record` |
| Responsibility | `Assigned route / coverage` |
| Attention item | `Needs review` |

These labels remain product-proof labels only. They are not platform vocabulary
authority, contract terms, schema requirements, domain commitments, real
organizational data, or production evidence.

## 4. Sequence Proof

Classification vocabulary:

- `PASS`: the sequence is clear for the synthetic managed lab proof.
- `FRICTION`: the sequence is in PC1 scope but creates candidate follow-up
  pressure.
- `NOT_RUN`: the sequence was not reviewed in this proof.
- `OUT-OF-SCOPE`: the sequence requires work outside NW-119 or PC1 boundaries.

| Sequence | Classification | Evidence notes | Candidate follow-up pressure |
|---|---|---|---|
| 1. Enter Organization | PASS | The managed lab proof uses `Example Lab Organization`, preserving the accepted one-Organization/default-Workspace PC1 boundary with no tenant or workspace selector. | None. |
| 2. Sign in to web admin | PASS | The web-admin entry beat maps to accepted browser session and server-resolved actor context standing; no IdP claim, role, group, or UI label becomes authority. | None. |
| 3. Prepare setup draft | PASS | `Field Activity Setup` remains a draft setup before delivery to devices; the accepted JSON-first setup/config vertical is sufficient for the synthetic lab proof. | None. |
| 4. Validate setup | PASS | Validation remains framed as setup issues to fix or a validated candidate, not production approval or runtime event rejection. | None. |
| 5. Readiness review | PASS | Readiness review remains operational review context for the synthetic lab setup, not workflow-state truth, reporting approval, or production approval. | None. |
| 6. Approve setup | PASS | Approval remains bound to exact validated content; changed content would require validation and approval again. | None. |
| 7. Publish setup | PASS | Publish remains one atomic setup/config package under accepted config-package standing. | None. |
| 8. Assign responsibility | PASS | `Assigned route / coverage` explains assigned work without implying that UI labels, IdP groups, organization units, or product roles grant authority. | None. |
| 9. Field user signs in | PASS | The mobile sign-in beat maps to accepted server-resolved actor/session alignment for PC1 proof purposes; no real user identity or production IdP path is selected. | None. |
| 10. Field user gets work | PASS | Get-work/sync refreshes setup, responsibility, and visible assigned work inside accepted assignment-derived access boundaries. | None. |
| 11. Field user sees readiness/missing states | PASS | Ready-to-capture, missing setup/forms, missing assignment, retry, and syncing vocabulary remains clear enough for the synthetic managed lab proof. | None. |
| 12. Field user captures activity entry | PASS | The `Visit Record` capture beat stays inside configured field work and does not imply server acceptance while offline. | None. |
| 13. Field user sees saved-local/waiting-to-sync | PASS | The local/pending beat uses accepted saved-local, waiting-to-sync, syncing, synced, failed, and retry vocabulary. | None. |
| 14. Field user appends correction | PASS | The correction beat remains append-only and does not imply event mutation, durable correction linkage, or overwrite semantics. | None. |
| 15. Field user syncs | PASS | Sync remains actor/device-context scoped; the proof does not claim that every device, actor, or future lab environment is current. | None. |
| 16. Supervisor/reviewer sees latest synced/freshness/attention wording | PASS | The accepted NW-114 minimal operational view closes the final beat with latest visible synced work, product-safe freshness wording, and at most one read-only `Needs review` cue without reporting/export or conflict-workflow scope. | None. |

## 5. Overall Standing

All 16 NW-111 sequences are `PASS` for the synthetic managed lab proof.

No sequence is `FRICTION`, `NOT_RUN`, or `OUT-OF-SCOPE` for this evidence
packet. No candidate follow-up pressure is promoted. Setup polish, mobile
polish, assignment proof polish, vocabulary validation, smoke automation,
reporting, retention/security, conflict workflow, entity lifecycle,
tenant/control-plane work, and real-use preparation remain separate future
routes only if selected.

## 6. Data And Approval Confirmations

| Check | Result |
|---|---|
| Synthetic/non-sensitive lab data only | Yes. |
| Real users used | No. |
| Real organizational data used | No. |
| Customer data used | No. |
| Production secrets used | No. |
| Real-production approval granted | No. |
| Provider/region/jurisdiction selected | No. |
| Support commitment created | No. |
| Compliance/security approval granted | No. |
| Runtime implementation changed | No. |
| Product or platform specs changed | No. |
| Contracts, schemas, sync, or migrations changed | No. |
| Reporting/export/analytics scope added | No. |
| Retention/security promises added | No. |
| Conflict workflow or automation added | No. |
| Tenant/control-plane work added | No. |

## 7. Stop Conditions Checked

No stop condition fired.

NW-119 did not require or select:

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

## 8. Next Move Recommendation

Recommended next move:

```text
park
```

Rationale:

- Repeating the lab proof is not needed because all 16 sequences are `PASS`
  and no friction was recorded.
- Selecting NW-093 is not justified because no concrete real users, real
  organizational data, provider, region, jurisdiction, support commitment,
  compliance/security review, continuity requirement, or go/no-go trigger is
  active.
- Selecting a bounded polish/follow-up route is not justified because no setup,
  mobile, assignment, vocabulary, freshness, or attention friction was recorded
  in this proof.

Parking means there is no active successor prompt from NW-119. Future work
must be selected separately through the PM handoff/backlog. Real users or real
organizational data still require NW-093 first.

## 9. Validation Category

Docs-only product-validation / owner-review evidence.

Runtime tests are skipped because NW-119 changes only working-surface
artifacts, backlog/status trace, and artifact indexing. It changes no runtime
code, tests, contracts, schemas, migrations, CI behavior, validation policy,
product spec, platform spec, BAR, CDL, gap register, or mobile code.
