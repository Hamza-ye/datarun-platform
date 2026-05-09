# Platform Spec Start Guide

Status: Orientation guide

Use this file when starting or resuming platform-spec work. It is a routing guide only. It does not add architecture, close gaps, or replace the baseline.

## The Short Rule

Do not read everything first.

Start with the smallest set of files needed to answer the current question, then route the work to the correct place:

- architecture rule: baseline and boundary-control files
- unresolved architecture: decision gap register and `atoms/90-open-decisions.md`
- decomposition rule: current atom file and atom template
- lookup and sequencing: `atom-registry.yml`
- rejected directions: `atoms/91-rejected-paths.md`
- accepted baseline changes: baseline change control, then `atoms/92-change-control-log.md`

## Source Roles

| File Or Area | Role | Must Not Become |
|---|---|---|
| `professional-baseline/` | Accepted architecture and guardrails | A draft playground |
| `professional-baseline/05-decision-gap-register.md` | Architectural gaps that remain unresolved | A place to casually close decisions |
| `professional-baseline/02-change-control.md` | Rule for changing accepted baseline authority | Optional ceremony |
| `platform-spec/atom-registry.yml` | Fast lookup for atom status, dependencies, and next work | Architecture authority |
| `platform-spec/atoms/*.md` | Small implementation-facing spec atoms | A second baseline |
| `platform-spec/atoms/90-open-decisions.md` | Platform-spec routing for open gaps and hold-backs | A hidden decision closer |
| `platform-spec/atoms/91-rejected-paths.md` | Forbidden couplings and rejected directions | A suggestion list |
| `platform-spec/atoms/92-change-control-log.md` | Trace of accepted changes, reopens, and disputes | The change-control rule itself |

## Normal Session Flow

1. Read this guide.
2. Read `atom-registry.yml`.
3. Read `process/01-atomization-operating-plan.md` only if the session needs procedure.
4. Read the selected atom and its direct dependencies.
5. Read only the source-basis files listed for that atom.
6. Draft or review one boundary at a time.
7. Update the registry only when atom metadata changes.
8. Commit with the project subject format and the required `Role:` and `Trace:` lines.

## When You Feel Lost

Use this routing table instead of opening every file.

| Situation | Go To |
|---|---|
| "What is the current next atom?" | `atom-registry.yml` |
| "What does this term mean?" | `atoms/02-glossary-and-core-definitions.md` |
| "Is this allowed architecture?" | baseline source-basis files listed for the atom |
| "Is this gap already unresolved?" | `professional-baseline/05-decision-gap-register.md`, then `atoms/90-open-decisions.md` |
| "Is this a rejected direction?" | `atoms/91-rejected-paths.md` |
| "Does this change accepted architecture?" | `professional-baseline/02-change-control.md` |
| "Where do I record the outcome?" | current atom, registry if metadata changed, and `atoms/92-change-control-log.md` only for accepted changes/reopens/disputes |

## Atom Lifecycle

Every atom should move through the same small loop:

1. Scope: one owner, one boundary, explicit non-scope.
2. Contract: inputs, outputs, invariants, and allowed extensions.
3. Guardrail review: forbidden couplings, rejected paths, and change-control triggers.
4. Integration review: dependencies, boundary crossings, and open gaps.
5. Outcome: accepted, draft with gaps, deferred, hold-back, needs spike, requires change control, or rejected.

Do not mark an atom accepted just because it is written. Acceptance means it survived the guardrail and integration reviews.

## Current Working Posture

The foundation is still in draft mode:

- `SPEC-001` governance is drafted.
- `SPEC-002` glossary is drafted and should stay definition-only.
- `SPEC-003` event log and storage is drafted and reconciled against the glossary.
- `SPEC-004` event envelope and schema is planned.

The next useful move is a focused design session for `SPEC-004`, not a broad expansion of the glossary.

## Drift Checks

Stop and route the work before continuing if a draft:

- changes an event-envelope field or field meaning
- adds structural event type values
- makes projections, queues, reports, or snapshots canonical truth
- stores immutable authority context
- makes account, group, tenant, deployment, user, or IdP claims direct authority
- lets deployers author arbitrary platform logic
- closes a listed gap without change control
- creates a second place where architecture authority must be maintained

