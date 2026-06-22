# Platform Specifications

Status: active platform-specification index

Use this directory for exact accepted platform behavior where architecture is
settled but implementation needs a stable behavioral target.

Do not restate schemas or protocols owned by `contracts/`, create architecture
authority, prescribe user-facing language, or store implementation-only design
here.

## Required Shape

Each platform specification should include:

- required metadata from
  [documentation-organization.md](../../documentation-organization.md);
- scope and accepted authority inputs;
- normative behavior and invariants;
- inputs, outputs, states, and error/failure behavior;
- compatibility and migration expectations when applicable;
- acceptance criteria and required evidence;
- explicit non-goals and architecture escalation triggers;
- related product specification, contracts, and implementation successors.

## Index

| Specification | Status | Owner | Source NW | Supersedes |
|---|---|---|---|---|
| [Assignment Scope And Administration](assignment-scope-and-administration.md) | accepted | authorization/sync verifier | NW-069 | none |
| [Configuration Package And Shape Lifecycle](configuration-package-and-shapes.md) | accepted | config verifier | NW-068 | none |
| [Conflict Flag Resolution And Attention Query Boundary](conflict-flag-resolution-and-attention-query-boundary.md) | accepted | integrity/platform verifier | NW-072 | none |
| [Expression Language Behavior](expression-language.md) | accepted | config/mobile verifier | NW-068 | none |
| [Mobile OIDC Login And Token Lifecycle](mobile-oidc-login-and-token-lifecycle.md) | accepted | mobile/security verifier | NW-085 | none |
| [Operational Responsibility Handoff Boundary](operational-responsibility-handoff-boundary.md) | accepted | handoff/platform verifier | NW-134 | none |
| [Production Auth Principal Binding](production-auth-principal-binding.md) | accepted | auth/security verifier | NW-070 | none |
| [Production Web Admin Authentication And Authority](production-web-admin-authentication-and-authority.md) | accepted | platform/security verifier | NW-079 | none |
| [Scoped Operational Report Snapshot Boundary](scoped-operational-report-snapshot-boundary.md) | accepted | reporting/platform verifier | NW-128 | none |
| [Shared-Device Session And Local State](shared-device-session-and-local-state.md) | accepted | mobile/sync verifier | NW-071 | none |
