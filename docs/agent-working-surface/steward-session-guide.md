# Steward Session Guide

Status: active steward workflow guide

This guide is for explicit steward, audit, routing, reconciliation, checkpoint,
or architecture/gap classification work. Steward is not the default
implementation role.

## When Steward Role Is Allowed

Use steward mode only when the user task or active NW asks for one of these:

- selecting, parking, or reconciling NW/BAR/status standing;
- classifying architecture-sensitive pressure through the gap playbook;
- reading broadly to prepare a bounded dispatch packet;
- auditing drift between accepted sources, code, tests, and status;
- creating a checkpoint after a real handoff or wave boundary.

Ordinary implementation stays product-slice-first under root `AGENTS.md`.

## Broad Reading Rules

Broad reading is allowed only to resolve routing, authority, or drift. Start with
`docs/status.md` Current Routing and
`docs/agent-working-surface/README.md` authority order, then open only the exact
sources needed for the classification or reconciliation.

Treat artifacts and historical notes as evidence, not authority, unless a
current route explicitly selects them. Do not read phase history, scenarios,
old IDRs, or the full CDL by default for implementation dispatch.

## Gap And CDL Routing

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
when a task or trigger needs gap classification. Use
`docs/architecture/adrs-decisions-canonical-ledger/README.md` to find relevant
CDL rows; prefer slices over full-ledger reads.

Useful CDL slicing commands from the repository root:

```bash
python3 scripts/query_cdl.py --id CDL-001 --format full
python3 scripts/query_cdl.py --tag sync --format concise
python3 scripts/query_cdl.py --search "assignment" --format concise
```

Do not create new IDRs by default. Route successor decisions through the active
decision-anchor layer unless the task names another accepted home.

## Dispatch Packet Checklist

A working-agent packet should state:

- repository path and branch;
- selected NW/product slice and goal;
- files to read and files explicitly out of scope;
- allowed changes and forbidden changes;
- accepted product/platform/contract boundaries;
- validation commands and evidence format;
- commit sequence and acceptance boundary;
- stop-and-report conditions.

Keep packets narrow enough that the implementer can finish without broad
architecture discovery.

## Reconciliation And Checkpoints

When reconciling status or backlog, update only surfaces materially affected by
the selected work. Record whether the gap register, artifact trace, BAR, active
control panel, contracts, runtime code, or product/platform behavior changed.

Use `docs/commit-workflow.md` for route, output, acceptance, review, hygiene,
and checkpoint commit boundaries. Create a checkpoint only after accepted
standing exists and the wave/handoff boundary is real; never use checkpoints as
routine NW closure.
