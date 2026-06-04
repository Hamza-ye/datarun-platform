# NW-039 Agent Prompt: Decide Production Principal-Binding Administration

You are working in `/home/hamza/datarun-platform`.

## Goal

Choose and record the production administration path for
`auth_principal_bindings` after NW-037/NW-038.

The decision must answer:

```text
How are (issuer, subject) -> actor_id bindings created, rotated, deactivated,
audited, and bootstrapped in production without making IdP groups/claims direct
platform authority?
```

Recommended default: choose a deployment-managed binding provisioning path for
the first production slice. Do not choose an online admin API unless product
needs it now and the decision can define explicit admin authority and audit
semantics without using IdP groups/claims as authority.

## Files To Read

Read only this bounded packet first:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-037, NW-038, NW-039, and NW-040.
4. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-104 only.
5. `docs/flagged-positions.md`
   - Read FP-011 only.
6. `docs/implementation/module-interfaces.md`
   - Read `Authenticated Actor Resolver`, `Scope Resolver`, and any admin/auth
     boundary section.
7. `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
8. `docs/agent-working-surface/prompts/NW-035-evaluate-production-auth-fp-011-successor-phase.md`
   - Use as historical design context only.
9. Current code/schema:
   - `server/src/main/resources/db/migration/V8__auth_principal_bindings.sql`
   - `server/src/main/java/dev/datarun/server/authorization/AuthPrincipalBindingRepository.java`
   - `server/src/main/java/dev/datarun/server/authorization/ActorTokenController.java`
   - `server/src/main/java/dev/datarun/server/authorization/AuthenticatedActorResolver.java`
   - `server/src/main/resources/application.properties`
10. CDL slices by command, not broad file reading:
    - `python3 scripts/query_cdl.py --id CDL-018`
    - `python3 scripts/query_cdl.py --id CDL-030`
    - `python3 scripts/query_cdl.py --id CDL-031`
    - `python3 scripts/query_cdl.py --id CDL-032`
    - `python3 scripts/query_cdl.py --id CDL-034`
    - `python3 scripts/query_cdl.py --id CDL-035`

Open other files only when these routes name them.

## Authority And Guardrails

- IDR-027 is active: authentication principal identity maps through explicit
  `(issuer, subject) -> actor_id`.
- NW-038 added OIDC/JWKS token validation only; it did not define binding
  administration.
- FP-011 remains open until production binding administration/provisioning and
  any group/claim authority risk are resolved.
- Bindings are lookup/supporting state, not assignment, role, scope, resolver,
  or event authority facts.
- Authority remains assignment-derived. Binding a principal to an actor does not
  grant that actor data access or action authority.
- `/api/actors/**` remains development token-management only and must not become
  a production admin surface by accident.

## Decision Options To Evaluate

Evaluate these options explicitly:

1. **Deployment-managed binding manifest/importer** — recommended first path.
   - A deployment artifact or startup/admin-run command declares desired
     principal bindings.
   - Server applies create/rotate/deactivate operations with audit metadata.
   - No online admin API or IdP group authority is introduced.
2. **Manual SQL/DB migration only**.
   - Lowest code surface, but weak audit and rotation discipline unless tightly
     documented.
3. **Online binding admin API/UI**.
   - Higher product value, but requires explicit admin authority, audit trail,
     bootstrap/root semantics, and tests.
   - Do not choose this casually.
4. **IdP group/claim-driven binding or authority**.
   - Rejected unless a separate successor decision defines ordinary assignment
     or assignment-administration events before authority changes.

## Expected Output

Add or update a decision artifact. Prefer a new IDR if the choice affects schema
or operational authority, for example:

```text
docs/decisions/idr-028-production-principal-binding-administration.md
```

The decision must define:

- selected administration path;
- bootstrap mechanism;
- create, rotate, deactivate, and rebind semantics;
- audit metadata required;
- idempotency/concurrency behavior;
- whether bindings are append-only rows with active-state projection or mutable
  support rows;
- how accidental principal-to-wrong-actor binding is corrected;
- what is explicitly not authority;
- why groups/claims remain non-authority;
- whether FP-011 can be closed after the subsequent implementation slice or
  which gate remains open.

Update:

- `docs/decisions/INDEX.md` if a new IDR is added.
- `docs/status.md` Current Routing and What's Next.
- `docs/agent-working-surface/platform-next-work-backlog.md`:
  - mark NW-039 accepted only after the decision exists;
  - route NW-040 as the selected implementation slice.
- `docs/implementation/module-interfaces.md` only if the chosen boundary changes
  module ownership/forbidden responsibilities.

## Forbidden Work

- Do not implement the binding administration mechanism in NW-039 unless the
  user explicitly asks this agent to combine decision and implementation.
- Do not add online production admin APIs by default.
- Do not use IdP groups, realm roles, client roles, resource claims, or JWT
  `actor_id` claims as platform authority.
- Do not add envelope fields or event `type` values.
- Do not add new scope mechanisms, resolver reassignment, auto-resolution, or
  trigger execution.
- Do not modify mobile login UX.

## Expected Tests

If NW-039 is documentation/decision only:

```bash
git diff --check
```

If any code/schema is touched, add targeted tests for the touched behavior and
run the relevant server suite. Do not claim FP-011 closure without runtime tests.

## Commit Boundary

One documentation/decision commit is expected, for example:

```text
docs(auth): decide principal binding administration
```

If implementation work is needed, route it as NW-040 in a separate commit.

## Stop And Report

Stop and report if:

- Product requires binding or authority to be granted directly from IdP groups
  or claims.
- The selected path needs a production admin API but no admin authority model is
  available.
- Correcting a wrong binding seems to require rewriting events or changing
  historical `actor_ref`.
- The work appears to require new envelope fields, new event types, new scope
  mechanisms, resolver reassignment, auto-resolution, or mobile actor switching.
