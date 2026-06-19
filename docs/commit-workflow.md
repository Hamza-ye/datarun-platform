# Commit And Progress Workflow

Status: active engineering workflow standard

Authority: this document governs commit structure and progress-state
transitions for repository work. It does not grant implementation authority or
replace CDL, contracts, BAR, NW, tests, or review.

## Goal

Commits should make the project history readable as a sequence of real state
transitions:

```text
route
-> decide or specify when needed
-> implement and verify
-> accept standing
-> checkpoint only at a real handoff boundary
```

Atomic means one coherent outcome, not one file type and not necessarily one
commit per NW item.

## Commit Message

Use:

```text
<type>(<scope>): <imperative outcome>
```

Examples:

```text
docs(ops): route deployment readiness analysis
docs(auth): decide principal binding administration
docs(product): specify offline sync status
feat(mobile): add append-only correction flow
fix(sync): preserve successful sync freshness on failure
test(workflow): add coordinated campaign scenario probe
test(ops): rehearse backup restore
docs(status): accept append-only correction UX
docs(checkpoint): snapshot first-deployment iterations
docs(hygiene): consolidate operations documentation indexes
```

### Types

| Type | Use |
|---|---|
| `feat` | Adds user-visible or platform capability. |
| `fix` | Corrects behavior or a broken invariant. |
| `test` | Adds verification, scenario probes, rehearsal automation, or evidence without changing intended runtime behavior. |
| `refactor` | Changes implementation structure without changing intended behavior. |
| `docs` | Changes routing, decisions, specifications, policies, runbooks, status, checkpoints, or documentation-only evidence. |
| `build` | Changes build or packaging mechanics. |
| `ci` | Changes continuous-integration behavior. |
| `chore` | Repository maintenance that fits no more specific type and changes no product/platform behavior. |
| `perf` | Improves measured performance without changing semantics. |
| `revert` | Reverts a named prior commit. |

### Scopes

Use a stable domain or repository surface such as:

```text
auth, sync, mobile, config, workflow, integrity, identity, reporting, ops,
contracts, status, checkpoint, product, platform, review, hygiene, docs,
tooling, ci
```

Prefer the capability being changed over the directory name. Do not use an NW
number, agent name, workshop stage, `misc`, `changes`, or `wip` as the scope.

The subject should be concise, imperative, and describe the resulting state.
Do not put review narration, file lists, test logs, or Markdown links in the
subject.

## Traceability Trailer

For commits owned by an NW item, add a short trailer:

```text
NW: NW-###
```

When useful, add one concise evidence trailer:

```text
Evidence: flutter test (131 tests)
```

Keep detailed commands and results in the NW exit condition, BAR evidence, or
rehearsal record. A commit should normally have one primary NW. Multiple NW
trailers are a signal that the commit may need splitting.

## Default NW Commit Flow

### 1. Route

Use when a candidate becomes active work.

Commit:

```text
docs(<domain>): route <bounded outcome>
```

May include:

- the NW row set to `ready` or `in_review`;
- one bounded prompt;
- current-routing updates;
- gap or index updates needed to make the route discoverable.

Must not:

- mark the outcome `accepted`;
- include speculative implementation;
- create a checkpoint.

If the route already exists and is accurate, do not create a redundant routing
commit.

### 2. Decide, Specify, Or Define Policy

Use only when implementation depends on an accepted decision, specification,
or deployment-owner policy.

Examples:

```text
docs(auth): decide shared device sessions
docs(product): specify offline capture handoff
docs(platform): define reporting freshness semantics
docs(ops): define backup recovery policy
```

The durable output must use the canonical home from
`docs/documentation-organization.md`. If a human or steward must still accept
options, leave the NW `in_review`; do not phrase a proposal commit as an
accepted decision.

### 3. Implement And Verify

Use one or more independently reviewable behavior commits.

Commit code with:

- tests that prove the changed behavior;
- required migrations;
- inseparable contract changes;
- narrowly required implementation-boundary documentation.

Do not intentionally defer tests to a later commit. Do not update the NW to
`accepted` merely because code was written.

Examples:

```text
feat(auth): add assignment admin command capabilities
fix(mobile): keep advisory checks non-authoritative
test(sync): add stale offline authority scenario probe
```

Split implementation commits when the pieces can be reviewed, reverted, or
verified independently. Keep a migration and the code that requires it
together unless compatibility explicitly requires a staged rollout.

### 4. Accept

After required tests, inspection, review, and durable outputs are complete,
record current standing in a separate acceptance commit when the NW/BAR/status
state changes.

Commit:

```text
docs(status): accept <bounded outcome>
```

The acceptance commit should update only the surfaces materially changed:

- NW exit condition and status;
- `docs/status.md` Current Routing;
- BAR when baseline capability standing changes;
- specification or operations indexes;
- module interfaces when an implemented boundary changed;
- residual successor or deferral routes.

The acceptance commit must state:

- validation evidence;
- gap register touched: yes/no;
- artifact trace touched: yes/no when an artifact was created, retired, or reclassified;
- active control panel updated: yes/no.

It must cite the implementation/decision commit and exact evidence. It must not
hide behavior fixes that should have their own `feat`, `fix`, or `test` commit.

### 5. Checkpoint

Checkpoints are optional milestone snapshots, not routine NW closure.

Create one only when:

- a milestone or coherent wave has closed;
- a major lane selection or handoff is about to occur;
- a long pause or context reset needs a reliable restart point;
- the worktree is clean and accepted standing is already recorded.

Commit:

```text
docs(checkpoint): snapshot <milestone>
```

The checkpoint anchors the last accepted commit and reports current standing.
Do not combine new implementation, acceptance changes, or unresolved review
fixes into the checkpoint commit.

## Docs-Only Exception

A bounded docs-only outcome may combine the durable document and its acceptance
updates in one commit when:

- no separate human/steward option acceptance remains;
- no runtime or contract behavior changes;
- verification is documentation-only;
- the NW exit condition can record complete evidence without pretending a
  capability was implemented.

Otherwise use separate proposal/decision and acceptance commits.

This exception does not justify combining route, broad exploration, decision,
implementation, status acceptance, and checkpoint work into one commit.

## Review Wave

Use this flow for a formal review:

1. `docs(review): assess <surface>` only when the findings themselves are a
   durable output.
2. Correct each behavioral issue with the truthful type and scope:
   `fix`, `feat`, `test`, `refactor`, or a precise `docs` commit.
3. Rerun the affected evidence.
4. Update acceptance/status only after findings are resolved or explicitly
   routed.
5. Add a checkpoint only if the review closes a real milestone.

Avoid messages such as `address review comments`, `review fixes`, or
`cleanup after review`; they hide what changed.

## Documentation Hygiene

Use:

```text
docs(hygiene): <specific organization outcome>
```

only when meaning and accepted standing are unchanged.

Hygiene may:

- repair links and indexes;
- consolidate duplicate non-authoritative text;
- move or retire stale routing material with explicit links;
- standardize metadata or formatting.

If semantics, authority, policy, acceptance, or implementation behavior
changes, use the owning domain and normal route instead. For large moves,
separate mechanical movement from semantic edits when practical so history
remains reviewable.

## Mechanical Pre-Commit Flow

When the task authorizes a commit:

```bash
git status --short
git diff --check
# run targeted tests and broader tests required by the task
git diff -- <owned paths>
git add <explicit owned paths>
git diff --cached --check
git diff --cached --stat
git diff --cached
git commit -m "<type>(<scope>): <imperative outcome>" \
  -m "NW: NW-###"
git status --short
```

Rules:

- Stage explicit paths; do not use `git add .` in a dirty worktree.
- Do not stage unrelated user changes.
- Inspect the staged diff, not only the unstaged diff.
- Never commit secrets, local credentials, generated environment state, or
  rehearsal data containing sensitive values.
- Do not create an empty acceptance or checkpoint commit to satisfy a process
  shape.
- Do not amend, reorder, squash, or rewrite shared history unless explicitly
  requested.

## Task-Packet Commit Boundary

Every non-trivial prompt should state:

- expected commit role or sequence;
- proposed conventional commit subject(s);
- whether status acceptance is separate;
- tests/evidence required before each commit;
- files or surfaces allowed in each commit;
- whether a checkpoint is explicitly out of scope.

Avoid a blanket "one commit" instruction when the task spans different state
transitions. Route, behavior, acceptance, and checkpoint commits answer
different questions and should remain distinct by default.

Older task packets may contain narrower historical commit instructions. For
new work, this active standard governs unless the current task explicitly
records and justifies a different atomic boundary.

## Final Acceptance Check

Before closing an NW:

- the implementation or durable output commit exists;
- tests/evidence are complete;
- the NW exit condition links commits and evidence;
- status/BAR/index changes are limited to materially affected surfaces;
- residual work has a successor or explicit deferral;
- no checkpoint is created unless its trigger is met;
- the final worktree state is reported accurately.
