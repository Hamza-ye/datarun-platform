# Canonical Decision Ledger - AI Index & Reading Guide
>
>[!IMPORTANT]
> The **[Canonical Decision Ledger](canonical-decision-ledger.md)** (CDL) is the single authoritative source of architectural decisions for the Datarun Platform, established and active at the closure of **[Phase 4: Workflow & Policies](../../implementation/phases/phase-4.md)** forward.
> It has been rigorously validated against the retired ADRs 001–009 and is 100% consistent with them.
>
>**Date:** 2026-05-27
>
## Machine Access & AI Slicing

To keep LLM context windows clean, do not read the full 2600+ line ledger for everyday development tasks. Instead, use the structured JSON catalog or the categorized index below.

### Formats Available

1. **Markdown Ledger (Authority):** [canonical-decision-ledger.md](canonical-decision-ledger.md)
2. **Structured JSON Index:** [canonical-decision-ledger.json](canonical-decision-ledger.json)
3. **Query CLI Tool:** Run `python3 scripts/query_cdl.py --help` to search/slice decisions by tags, ID, category, or search term.

### Querying Ledger via Command Line

An agent can run queries like:

```bash
# Find all decisions related to identity or merges
python3 scripts/query_cdl.py --tag identity

# Get details for a specific decision
python3 scripts/query_cdl.py --id CDL-021

# Search for decisions containing specific text
python3 scripts/query_cdl.py --search "watermark"
```

## Categorized Decision Catalog

### 1. Authority

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-000 | **Canonical surface rule** | *Governance invariant* | The final canonical decision ledger is the only agent-facing decision surface. Earlier ADRs, extracted ledgers, addenda, and ex... | [L15-53](canonical-decision-ledger.md#L15-L53) |

### 2. Global invariants

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-001 | **Operational truth lives in the immutable event stream** | *Structural invariant* | All operational writes enter through the append-only event store. Existing events are never modified or deleted. Corrections, r... | [L57-101](canonical-decision-ledger.md#L57-L101) |
| CDL-002 | **Projections are derived and rebuildable** | *Structural invariant* | Current state, read models, queues, indexes, aliases, workflow state, and reporting projections are derived from the event stre... | [L103-141](canonical-decision-ledger.md#L103-L141) |
| CDL-003 | **Valid state-stale events are accepted and flagged** | *Invariant* | A validly structured event is never rejected for state-based reasons. State anomalies are represented by appending flag events... | [L143-186](canonical-decision-ledger.md#L143-L186) |
| CDL-004 | **Detect-before-act governs policy and state participation** | *Strategy-protecting invariant* | Unresolved flagged events remain visible in timelines and audit views, but they do not trigger downstream policy execution and... | [L188-229](canonical-decision-ledger.md#L188-L229) |
| CDL-005 | **Mechanisms and instances are classified separately** | *invariant* | When a platform concept has both a platform-owned mechanism and a deployer-authored instance surface, the mechanism and the ins... | [L231-273](canonical-decision-ledger.md#L231-L273) |

### 3. Canonical event envelope

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-006 | **Canonical event envelope has eleven fields** | *Structural contract* | The canonical event envelope for the initial platform version contains exactly these conceptual fields:  ```text id type shape_... | [L277-333](canonical-decision-ledger.md#L277-L333) |
| CDL-007 | **Envelope `type` is a closed six-value vocabulary** | *Structural invariant / closed vocabulary* | Allowed `type` values are exactly:  ```text capture review alert task_created task_completed assignment_changed ```  The vocabu... | [L335-387](canonical-decision-ledger.md#L335-L387) |
| CDL-008 | **`shape_ref` identifies payload schema and domain fact** | *Structural contract* | Every event carries mandatory `shape_ref` in the format:  ```text {shape_name}/v{version} ```  `shape_name` matches `[a-z][a-z0... | [L389-437](canonical-decision-ledger.md#L389-L437) |
| CDL-009 | **`activity_ref` is an optional contract field** | *Structural contract* | Events may carry `activity_ref`, an optional deployer-chosen identifier matching `[a-z][a-z0-9_]*`, or `null`. It references an... | [L439-482](canonical-decision-ledger.md#L439-L482) |
| CDL-010 | **Causal metadata is structural and device-scoped** | *Structural contract* | Every event carries `device_id`, `device_sequence`, and `sync_watermark`.  - `device_id` identifies a physical device or app in... | [L484-531](canonical-decision-ledger.md#L484-L531) |
| CDL-011 | **`device_time` is advisory only** | *Structural guardrail* | `device_time` is display and audit metadata. Ordering, causality, conflict detection, projection correctness, and protocol corr... | [L533-571](canonical-decision-ledger.md#L533-L571) |
| CDL-012 | **No derived runtime concept is stored as an envelope field** | *Structural guardrail* | The envelope does not contain `authority_context`, top-level `assignment_ref` as authority context, `pattern_ref`, `workflow_st... | [L573-614](canonical-decision-ledger.md#L573-L614) |

### 4. Closed vocabularies and platform-bundled shapes

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-013 | **Domain discrimination uses `shape_ref`, not `type`** | *Binding rule* | Any code, spec, or agent that needs to identify the domain fact recorded by an event must inspect `shape_ref`, not `type`. | [L618-655](canonical-decision-ledger.md#L618-L655) |
| CDL-014 | **Platform-bundled integrity and identity shapes are contracts** | *Platform contract* | The platform registers and owns these integrity and identity shapes:  \| Domain fact                            \| Envelope `ty... | [L657-706](canonical-decision-ledger.md#L657-L706) |
| CDL-015 | **Deterministic flag identity includes source, shape, and category** | *Algorithmic contract* | A deterministic flag event identity is derived from the source event id, the flag event `shape_ref`, and the flag category, und... | [L708-753](canonical-decision-ledger.md#L708-L753) |

### 5. Reference contracts

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-016 | **Reference fields are contracts; referents are separate concepts** | *Binding rule* | A reference field and the object it points to are different ledger concepts. `subject_ref`, `actor_ref`, and `activity_ref` are... | [L757-795](canonical-decision-ledger.md#L757-L795) |
| CDL-017 | **`subject_ref` is a typed reference contract** | *Structural contract* | `subject_ref` is a typed reference of the form:  ```text { type, id } ```  The `id` is a client-generated UUID. The `type` enum... | [L797-850](canonical-decision-ledger.md#L797-L850) |
| CDL-018 | **`actor_ref` records human or system authorship** | *Structural contract* | `actor_ref` identifies who or what authored the event.  Human actors use UUID identity. System actors use:  ```text system:{sou... | [L852-899](canonical-decision-ledger.md#L852-L899) |

### 6. Storage and sync model

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-019 | **Atomic write unit is a typed immutable event** | *Structural constraint* | The atomic write unit is a typed immutable event. Each event records what happened with action-specific payload. Current state... | [L903-941](canonical-decision-ledger.md#L903-L941) |
| CDL-020 | **Client-generated UUIDs identify events, subjects, and records** | *Structural constraint* | Events, subjects, and records use client-generated UUIDs. Devices can mint identifiers offline without server coordination. | [L943-981](canonical-decision-ledger.md#L943-L981) |
| CDL-021 | **Sync transfers immutable events idempotently and by scope** | *Structural constraint* | Sync transfers immutable events the receiver has not yet seen, filtered by the receiver’s sync/access scope. Sync is idempotent... | [L983-1023](canonical-decision-ledger.md#L983-L1023) |

### 7. Identity and conflict

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-022 | **Duplicate real-world identity is resolved by identity mechanisms** | *Strategy-protecting constraint* | If two offline devices create different UUIDs for the same real-world thing, both UUIDs remain valid stored identities. Duplica... | [L1027-1065](canonical-decision-ledger.md#L1027-L1065) |
| CDL-023 | **Identity merge is alias-in-projection, never re-reference** | *Structural constraint* | An identity merge is recorded as `shape_ref = subjects_merged/v1`. It creates an alias mapping from retired identity to survivi... | [L1067-1106](canonical-decision-ledger.md#L1067-L1106) |
| CDL-024 | **Split freezes history and archives the source** | *Structural constraint* | An identity split is recorded as `shape_ref = subject_split/v1`. The source identity becomes terminally archived. Historical ev... | [L1108-1147](canonical-decision-ledger.md#L1108-L1147) |
| CDL-025 | **No unmerge operation exists** | *Structural constraint / rejected alternative* | The platform does not define an unmerge operation. A wrong merge is corrected by splitting the surviving identity and creating... | [L1149-1187](canonical-decision-ledger.md#L1149-L1187) |
| CDL-026 | **Identity lineage is acyclic by construction** | *Structural invariant* | Identity lineage is a directed acyclic graph enforced at write time.  - Merge operands must be active. - Merge survivor must be... | [L1189-1232](canonical-decision-ledger.md#L1189-L1232) |
| CDL-027 | **Merge and split are online-only operations** | *Strategy-protecting constraint* | Identity merge and identity split require server-validated online transactions. | [L1234-1271](canonical-decision-ledger.md#L1234-L1271) |
| CDL-028 | **Conflict resolution is single-writer and server-validated** | *Strategy-protecting constraint* | A flag raised by `shape_ref = conflict_detected/v1` designates exactly one resolver. A manual `shape_ref = conflict_resolved/v1... | [L1273-1313](canonical-decision-ledger.md#L1273-L1313) |
| CDL-029 | **Conflict detection uses raw references before alias projection** | *Structural constraint* | Conflict detection evaluates incoming events using original references as written. Alias resolution happens afterward in projec... | [L1315-1353](canonical-decision-ledger.md#L1315-L1353) |

### 8. Authorization and selective sync

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-030 | **Access is assignment-based** | *Structural constraint* | Access reduces to: the actor has an active assignment whose scope contains the target entity and whose role permits the intende... | [L1357-1396](canonical-decision-ledger.md#L1357-L1396) |
| CDL-031 | **Sync scope equals access scope** | *Structural constraint* | A device receives exactly the data its current actor is authorized to access. Sync scope and access scope are the same boundary. | [L1398-1436](canonical-decision-ledger.md#L1398-L1436) |
| CDL-032 | **Authority is projected, not stored in the envelope** | *Structural constraint* | The event envelope has no `authority_context` field. Authority is reconstructed from assignment timelines, actor identity, even... | [L1438-1476](canonical-decision-ledger.md#L1438-L1476) |
| CDL-033 | **Authorization uses original subject scope, not post-merge scope** | *Structural constraint* | Authorization evaluates an event against the original `subject_ref` and scope context as written at event creation, not the pos... | [L1478-1516](canonical-decision-ledger.md#L1478-L1516) |
| CDL-034 | **Assignment creation enforces scope containment** | *Strategy-protecting security constraint* | An assignment creation command is server-validated so the new assignment scope is contained within the creating actor’s authori... | [L1518-1560](canonical-decision-ledger.md#L1518-L1560) |
| CDL-035 | **Authorization staleness is accepted, surfaced, and severity-controlled** | *Initial strategy / strategy-protecting constraint* | Work created under stale authorization state is accepted and flagged when anomalous. Flag severity controls whether the event b... | [L1562-1600](canonical-decision-ledger.md#L1562-L1600) |
| CDL-036 | **Projection location is tiered and evolvable** | *Initial strategy* | Projection location is tiered:  - Field workers use device-local projections from scoped events. - Supervisors use hybrid local... | [L1602-1644](canonical-decision-ledger.md#L1602-L1644) |
| CDL-037 | **Scope contraction data handling is device retention policy** | *Initial strategy / device policy boundary* | When an actor's scope contracts, the sync engine does not mutate or delete canonical events. Device-side handling follows selec... | [L1646-1694](canonical-decision-ledger.md#L1646-L1694) |

### 9. Configuration boundary

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-038 | **Configuration has a four-layer gradient and an L3-to-code ceiling** | *Strategy-protecting boundary* | Configuration is organized as:  ```text L0 Assembly L1 Shape L2 Logic L3 Policy ```  Configuration stops at L3. Behavior beyond... | [L1698-1747](canonical-decision-ledger.md#L1698-L1747) |
| CDL-039 | **Shapes are deployer-defined payload schemas with version coexistence** | *Structural / strategy-protecting constraint* | Deployers define shapes as typed payload schemas. Shapes evolve through explicit versions. Old and new shape versions coexist;... | [L1749-1791](canonical-decision-ledger.md#L1749-L1791) |
| CDL-040 | **Shapes may be authored as deltas but are stored as full snapshots** | *Initial strategy* | Deployers may author a new shape version as a delta, but runtime shape registry entries are stored as complete snapshots. | [L1793-1830](canonical-decision-ledger.md#L1793-L1830) |
| CDL-041 | **Configuration packages are atomic at sync** | *Strategy-protecting constraint* | Configuration is delivered as an atomic package at sync. Devices apply a new package only after in-progress work under the prev... | [L1832-1872](canonical-decision-ledger.md#L1832-L1872) |
| CDL-042 | **L3 policy executes server-only** | *Strategy-protecting constraint* | All L3 policy execution is server-only. This includes event-reaction policy and deadline/async policy. Devices use L2 logic for... | [L1874-1912](canonical-decision-ledger.md#L1874-L1912) |
| CDL-043 | **Expression language is bounded and non-programmatic** | *Strategy-protecting constraint* | There is one bounded expression language with context-specific data access. It uses operators and field references only; it has... | [L1914-1956](canonical-decision-ledger.md#L1914-L1956) |
| CDL-044 | **Trigger and configuration complexity budgets are enforced at deploy time** | *Strategy-protecting constraint* | Deploy-time validation enforces configuration consistency, dependency validity, trigger acyclicity, and hard complexity budgets... | [L1958-2009](canonical-decision-ledger.md#L1958-L2009) |
| CDL-045 | **Domain uniqueness rules are shape-declared; resolution strategy is separate** | *Initial strategy / boundary* | Shapes may declare domain uniqueness constraints evaluated optimistically on device and authoritatively on server. Violations p... | [L2011-2051](canonical-decision-ledger.md#L2011-L2051) |
| CDL-046 | **Sensitivity is shape/activity-level configuration** | *Initial strategy* | Sensitivity is configured at shape or activity level using three levels:  ```text standard elevated restricted ```  Sensitivity... | [L2053-2099](canonical-decision-ledger.md#L2053-L2099) |

### 10. Workflow and state progression

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-047 | **Workflow state is projection-derived, not stored or enforced by rejection** | *Strategy-protecting constraint* | Workflow state is derived by projection from event history and pattern definitions. State is never stored as canonical current... | [L2103-2143](canonical-decision-ledger.md#L2103-L2143) |
| CDL-048 | **`transition_violation` is a platform flag category** | *Strategy-protecting constraint* | The detector evaluates incoming events against pattern-defined state-machine rules and emits a `transition_violation` flag cate... | [L2145-2183](canonical-decision-ledger.md#L2145-L2183) |
| CDL-049 | **Pattern registry is platform-fixed; activities bind patterns** | *Mechanism / config split* | The platform provides a closed pattern registry: platform-owned workflow skeletons that deployers select and parameterize throu... | [L2185-2224](canonical-decision-ledger.md#L2185-L2224) |
| CDL-050 | **Pattern composition is bounded and validated** | *Initial strategy* | Pattern composition follows these rules:  ```text 1. One subject-level pattern per activity. 2. Event-level patterns compose fr... | [L2226-2273](canonical-decision-ledger.md#L2226-L2273) |
| CDL-051 | **Source-only flagging is the workflow cascade model** | *Initial strategy* | Only the root-cause event receives a flag. Downstream contamination is computed through source-chain traversal in projections,... | [L2275-2314](canonical-decision-ledger.md#L2275-L2314) |
| CDL-052 | **`context.*` is bounded form context** | *Initial strategy* | Form expressions may read a platform-fixed `context.*` namespace. Initial properties are:  ```text context.subject_state contex... | [L2316-2368](canonical-decision-ledger.md#L2316-L2368) |
| CDL-053 | **Auto-resolution is L3b policy and emits normal resolution events** | *Initial strategy* | Auto-resolution is a server-side L3b policy subtype. It observes eligible flags and emits a normal `shape_ref = conflict_resolv... | [L2370-2410](canonical-decision-ledger.md#L2370-L2410) |
| CDL-054 | **Flag resolvability is platform-classified** | *Strategy-protecting constraint* | Each flag category has platform-defined resolvability:  ```text auto_eligible manual_only ```  Initial classification:  \| Flag... | [L2412-2472](canonical-decision-ledger.md#L2412-L2472) |

### 11. Platform-fixed vs deployer-configured split

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-055 | **Scope mechanism is platform-fixed; scope instances are configuration** | *Mechanism / config split* | The scope mechanism is platform-fixed. Initial scope types are:  ```text geographic subject_list activity ```  Scope compositio... | [L2476-2523](canonical-decision-ledger.md#L2476-L2523) |
| CDL-056 | **Activity is deployer configuration; `activity_ref` is a contract** | *Config / contract split* | An activity is a deployer-assembled L0 configuration instance composed from platform-provided components: shapes, patterns, rol... | [L2525-2563](canonical-decision-ledger.md#L2525-L2563) |

