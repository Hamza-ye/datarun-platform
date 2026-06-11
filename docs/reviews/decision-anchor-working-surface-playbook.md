# Decision Anchor Working Surface Playbook

Status: stewardship playbook / non-authoritative

Date: 2026-06-11

Authority note: this playbook does not change CDL authority, BAR status,
contracts, schemas, APIs, runtime behavior, backlog priority, or accepted
baseline standing. It records how a human steward should work with an agent
through the active working surface after the decision-anchor consolidation.

## 1. Outcome Of The Surface Review

The active decision-anchor surface should remain compact:

- `docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/agent-working-surface/decision-anchor-layer/provenance-index.md`

The untracked `.review/architecture-records-and-vocabularies/` candidate
directory was reviewed as audit material and rejected for direct promotion. Its
files still carried older `.review`/ADR-derived authority wording and stale gap
standing. Compatible claims were folded into the active surface only where they
matched CDL/BAR/NW standing. The directory does not need to be retained.

## 2. Working Surface Source Order

Use this order with working agents:

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/decision-anchor-layer/README.md`
5. `docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md`
6. `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
7. `docs/agent-working-surface/baseline-acceptance-register.md`
8. `docs/agent-working-surface/platform-next-work-backlog.md`
9. Exact contracts, IDRs, code, scenarios, or review artifacts named by the route

Do not ask agents to read broad chronology by default. Use CDL slices through
`scripts/query_cdl.py` when a route needs architecture details.

## 3. Human-To-Agent Prompt Shape

Use this packet for implementation or stewardship work:

```text
Goal:
<one bounded outcome>

Files to read:
AGENTS.md
docs/status.md Current Routing
docs/agent-working-surface/decision-anchor-layer/README.md
docs/agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md
docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md
<only exact BAR/NW/contract/code files needed>

Authority and guardrails:
CDL is architecture authority. Contracts govern wire/process boundaries.
BAR/NW govern accepted standing and evidence. Do not infer current truth from
review chronology.

Forbidden:
No new envelope fields or event type values. No durable workflow-state truth.
No normal sync watermark rewrite. No IdP claim/group authority. No new scope
mechanism. No mobile authoritative rejection. No direct promotion of `.review`
material.

Expected output:
<code/doc/artifact boundary>

Tests:
<targeted tests or docs-only verification>

Stop and report if:
The work conflicts with CDL/contracts/BAR/NW/code, needs a successor decision,
or depends on a deferred/future-decision surface.
```

## 4. Review Checklist For Agent Output

Before accepting an agent's patch:

- Confirm it changed only the named surfaces.
- Check any DEC anchor change maps to CDL rows.
- Check any known-gap change cites BAR/NW standing when it claims current status.
- Verify `.review` material was used only as provenance or audit input.
- Confirm no product term became platform authority.
- Confirm no deferred/future-decision route was implemented accidentally.
- Run `git diff --check` for docs-only changes.
- Run targeted Maven/Flutter tests only when code, contracts, or behavior changed.

## 5. Preferred Next Routes

Use these routes instead of broad cleanup:

| Pressure | Route |
|---|---|
| Retention, expiry, decommissioning, sealed recovery, local encryption, token/session retention | NW-054 / BAR-106 |
| Reporting APIs, dashboards, warehouse, export, import, interoperability | NW-044 |
| Domain conflict batch handling, pending-match UX, automation | NW-045 |
| Flag cascade indicators or pattern traversal reporting | NW-046 |
| Subject/query/custom scope, auditor scope, campaign/custody scope | NW-053 / BAR-108 |
| Production web admin auth, online binding admin, mobile OIDC login UX | product/platform successor from NW-056 |
| Config/admin UX, mobile UX, reporting vocabulary | NW-047 companion first, then one bounded product prompt |

## 6. Cleanup Rule

Untracked or unreferenced workbench drafts may be removed after review when:

- their durable compatible claims are already folded into the active surface;
- they are not linked from the committed provenance index;
- they would create a second active routing surface if retained;
- current BAR/NW/CDL standing supersedes their claims.

Do not remove committed provenance without a separate explicit cleanup request.
