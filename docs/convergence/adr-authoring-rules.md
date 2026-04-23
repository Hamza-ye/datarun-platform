# ADR Authoring Rules

> Status: **Active** (Phase 2+)
> Companion to [supersede-rules.md](supersede-rules.md)

An ADR is an authority-layer document. Architecture, implementation docs, IDRs,
phase specs, and code all conform *to* ADRs, not the other way around. If ADRs
reach downward into the layers that conform to them, the arrow reverses and
review becomes circular: the next Phase 4 audit cannot validate an ADR against
the architecture/ and implementation/ tree if the ADR itself cites those
documents as evidence.

This file captures the rules that keep the arrow pointing one way.

---

## Authority order (one-way)

    root docs (charter, ledger, convergence/)
        │
        ▼
    ADRs (decided)
        │
        ▼
    architecture/  (rewritten to conform — Phase 2/4)
        │
        ▼
    IDRs          (implementation-grade choices within ADR constraints)
        │
        ▼
    phase specs / implementation journals
        │
        ▼
    code (server/, mobile/, contracts/ consumer surfaces)

Each layer may cite **upward** freely. Citing **downward** across the ADR line
is forbidden in the body of an ADR — it couples the decision to artifacts the
decision is supposed to govern.

`contracts/` is special: its shape schemas, envelope schema, and flag catalog
are platform-level artifacts co-owned with ADRs. ADRs **may** reference
`contracts/*` paths (e.g. `contracts/shapes/`), because these are the
load-bearing contracts ADRs canonicalize.

---

## Forbidden in an ADR body

1. **IDR citations.** IDRs are downstream of ADRs. If an ADR needs a fact that
   is currently stated only in an IDR, lift the fact into the ADR (in domain
   terms) — do not cite the IDR.
2. **Phase numbers used as authority.** "Phase 3d audit caught this" is
   acceptable *provenance* in a Context paragraph if paraphrased domain-neutrally
   ("a subsequent audit caught this"). Phase numbers must not appear in
   Decision or Forbidden-pattern sections.
3. **Concrete file paths under `server/`, `mobile/`, or `admin/`.** Class
   names, file paths, table names, column names, migration versions, test
   class names. These are implementation vocabulary and will change.
4. **File counts or "~N files" statements.** They tell the reader nothing
   durable and date the ADR to a single moment.
5. **Technology-specific mechanism words in obligations.** "Server boot,"
   "Spring Boot startup," "Flutter app launch," "JVM initialization" — replace
   with platform-neutral language ("platform initialization," "registry
   initialization," "startup").

---

## Permitted in an ADR body

- References to other ADRs (upstream and downstream by ID).
- References to `contracts/*` paths.
- Section references into `docs/architecture/` as *context* (not as authority
  citations — the architecture tree conforms to ADRs, so citing it as proof of
  an ADR's claim is circular). Permitted form: "the cross-cutting rule defined
  in `cross-cutting.md` §1" when naming a rule the ADR ratifies.
- Historical motivation in domain terms: "during earlier implementation
  phases, drift occurred in four type values" ✓ ; "in Phase 1/2 the server
  code leaked four values into `events.type`" ✗.
- Architecture-grade mechanism names that are part of the platform's
  contract with deployers: "platform-bundled shape," "actor token,"
  "projection engine" — these are contracts, not file paths.

---

## Review checkpoints

Before marking an ADR **Decided**:

1. `grep -niE 'phase-?[0-9]|IDR-[0-9]|server/|mobile/|\.java|\.dart' <adr>` —
   expected: **zero hits** in Decision / Forbidden-pattern / Consequences.
   Context paragraph may have a small number of domain-phrased exceptions.
2. `grep -niE '~[0-9]+ files|events table|database|SQL|JVM|Spring|Flutter'
   <adr>` — expected: zero hits anywhere.
3. Traceability table: every cited document is either another ADR, a
   `contracts/*` path, or a domain-named architecture rule. No IDRs, no
   phase specs, no source files.

Applied retroactively to ADR-006 and ADR-007 at the close of round 1.

---

## When these rules were learned

During Phase 2 round 1 (ADR-007 commit `e231fbf`), a review caught that
ADR-007 §S5 cited IDR-013 for "platform-bundled shape treatment" and used
"server boot" as an obligation verb. ADR-006 §S3 named a concrete Java file
path. Both leaks had to be scrubbed in a follow-up commit. This rule file
encodes the lesson so round 2 does not repeat it.

The review also reinforced the reason: once Phase 4 begins, the architecture
tree and implementation docs will be rewritten *to conform to* the charter +
ADRs. If ADRs cite those documents as authority, the rewrite cannot proceed
without breaking citations in the ADRs themselves — a circular dependency that
freezes the system.
