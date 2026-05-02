# Platform Specification Kernel Working Draft

Status: Iteration 13 staging index

This file is the staging index for platform-specification kernel extraction. The kernel sections have been split into temporary staging files for context control. Final atomic files must not be created from those sections until the conflict checks and closure pass are complete.

## Draft Discipline

Each kernel section must remain technical and narrow. It should specify one platform fact, interface, invariant, interaction rule, open issue, rejected alternative, or conditional validity rule.

Do not organize sections by ADR number. ADRs and exploration files are source anchors only.

Do not treat alternatives as options unless approved sources leave them open. Rejected alternatives are guardrails.

Do not use unapproved sources or memory to fill gaps.

## Kernel Section Template

```markdown
## Kernel: [precise technical name]

Status: Candidate | Settled | Open | Conditional | Rejected | Superseded
Kind: primitive | contract | invariant | algorithm | interaction-rule | configuration-boundary | forbidden-interpretation | open-question | rejected-alternative | conditional-validity

Specification statement:

Source basis:

Closure basis:

Scope:

Non-goals:

Forbidden interpretations:

Open edges:

Platform specification note:
```

## Staged Kernels


## Temporary Staging Files

The working draft has been split for context control. These files are temporary staging groups, not final atomic documents:

- `02-domain-requirement-kernels.md`: ground-truth, scenario-index, and early scenario requirement kernels.
- `03-behavioral-viability-principle-kernels.md`: behavioral-pattern, viability-assessment, and principle kernels.
- `04-architecture-lineage-kernels.md`: architecture-landscape and ADR-lineage kernels.
- `05-methodology-and-extraction-rules.md`: methodology and extraction-rule kernels.

New kernels should be added to the smallest relevant staging file. Final atomic files must not be created until rest state is reached.

## Pending Split Targets

Do not create final atomic files yet. Candidate future groups, to be validated after rest state:

- primitives
- contracts
- invariants
- algorithms
- configuration
- interactions
- forbidden-patterns
- open-questions
- rejected-alternatives
- conditional-validity
