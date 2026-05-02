# Platform Specification Kernel Working Draft

Status: Iteration 0 setup

This file stages atomic platform-specification kernels in one place until the approved source set reaches rest state. Sections may be rewritten, merged, split, or demoted during extraction. Final atomic files must not be created from these sections until the conflict checks and closure pass are complete.

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

No kernels extracted yet.

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
