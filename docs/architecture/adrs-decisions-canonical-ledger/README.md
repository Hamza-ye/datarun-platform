# Canonical Decision Ledger - AI Index & Reading Guide
>
>[!IMPORTANT]
> The **[Canonical Decision Ledger](canonical-decision-ledger.md)** (CDL) is the single authoritative source of architectural decisions for the Datarun Platform, established and active at the closure of **[Phase 4: Workflow & Policies](../../implementation/phases/phase-4.md)** forward.
> It has been rigorously validated against the retired ADRs 001–009 and is 100% consistent with them. 
> Routing historical documentation references pointing to retired ADRs (001–009) to their corresponding active definitions here see: [adr-to-cdl-map.md](adr-to-cdl-map.md)
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
| CDL-000 | **Canonical surface rule** | *Governance invariant* | The final canonical decision ledger is the only agent-facing decision surface. Earlier ADRs, extracted ledgers, addenda, and ex... | [L13-51](canonical-decision-ledger.md#L13-L51) |

### 2. Global invariants

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-001 | **Operational truth lives in the immutable event stream** | *Structural invariant* | All operational writes enter through the append-only event store. Existing events are never modified or deleted. Corrections, r... | [L55-99](canonical-decision-ledger.md#L55-L99) |
| CDL-002 | **Projections are derived and rebuildable** | *Structural invariant* | Current state, read models, queues, indexes, aliases, workflow state, and reporting projections are derived from the event stre... | [L101-139](canonical-decision-ledger.md#L101-L139) |
| CDL-003 | **Valid state-stale events are accepted and flagged** | *Invariant* | A validly structured event is never rejected for state-based reasons. State anomalies are represented by appending flag events... | [L141-184](canonical-decision-ledger.md#L141-L184) |
| CDL-004 | **Detect-before-act governs policy and state participation** | *Strategy-protecting invariant* | Unresolved flagged events remain visible in timelines and audit views, but they do not trigger downstream policy execution and... | [L186-227](canonical-decision-ledger.md#L186-L227) |
| CDL-005 | **Mechanisms and instances are classified separately** | *invariant* | When a platform concept has both a platform-owned mechanism and a deployer-authored instance surface, the mechanism and the ins... | [L229-271](canonical-decision-ledger.md#L229-L271) |

### 3. Canonical event envelope

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-006 | **Canonical event envelope has eleven fields** | *Structural contract* | The canonical event envelope for the initial platform version contains exactly these conceptual fields:  ```text id type shape_... | [L275-331](canonical-decision-ledger.md#L275-L331) |
| CDL-007 | **Envelope `type` is a closed six-value vocabulary** | *Structural invariant / closed vocabulary* | Allowed `type` values are exactly:  ```text capture review alert task_created task_completed assignment_changed ```  The vocabu... | [L333-385](canonical-decision-ledger.md#L333-L385) |
| CDL-008 | **`shape_ref` identifies payload schema and domain fact** | *Structural contract* | Every event carries mandatory `shape_ref` in the format:  ```text {shape_name}/v{version} ```  `shape_name` matches `[a-z][a-z0... | [L387-435](canonical-decision-ledger.md#L387-L435) |
| CDL-009 | **`activity_ref` is an optional contract field** | *Structural contract* | Events may carry `activity_ref`, an optional deployer-chosen identifier matching `[a-z][a-z0-9_]*`, or `null`. It references an... | [L437-480](canonical-decision-ledger.md#L437-L480) |
| CDL-010 | **Causal metadata is structural and device-scoped** | *Structural contract* | Every event carries `device_id`, `device_sequence`, and `sync_watermark`.  - `device_id` identifies a physical device or app in... | [L482-529](canonical-decision-ledger.md#L482-L529) |
| CDL-011 | **`device_time` is advisory only** | *Structural guardrail* | `device_time` is display and audit metadata. Ordering, causality, conflict detection, projection correctness, and protocol corr... | [L531-569](canonical-decision-ledger.md#L531-L569) |
| CDL-012 | **No derived runtime concept is stored as an envelope field** | *Structural guardrail* | The envelope does not contain `authority_context`, top-level `assignment_ref` as authority context, `pattern_ref`, `workflow_st... | [L571-612](canonical-decision-ledger.md#L571-L612) |

### 4. Closed vocabularies and platform-bundled shapes

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-013 | **Domain discrimination uses `shape_ref`, not `type`** | *Binding rule* | Any code, spec, or agent that needs to identify the domain fact recorded by an event must inspect `shape_ref`, not `type`. | [L616-653](canonical-decision-ledger.md#L616-L653) |
| CDL-014 | **Platform-bundled integrity and identity shapes are contracts** | *Platform contract* | The platform registers and owns these integrity and identity shapes:  \| Domain fact                            \| Envelope `ty... | [L655-704](canonical-decision-ledger.md#L655-L704) |
| CDL-015 | **Deterministic flag identity includes source, shape, and category** | *Algorithmic contract* | A deterministic flag event identity is derived from the source event id, the flag event `shape_ref`, and the flag category, und... | [L706-751](canonical-decision-ledger.md#L706-L751) |

### 5. Reference contracts

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-016 | **Reference fields are contracts; referents are separate concepts** | *Binding rule* | A reference field and the object it points to are different ledger concepts. `subject_ref`, `actor_ref`, and `activity_ref` are... | [L755-793](canonical-decision-ledger.md#L755-L793) |
| CDL-017 | **`subject_ref` is a typed reference contract** | *Structural contract* | `subject_ref` is a typed reference of the form:  ```text { type, id } ```  The `id` is a client-generated UUID. The `type` enum... | [L795-848](canonical-decision-ledger.md#L795-L848) |
| CDL-018 | **`actor_ref` records human or system authorship** | *Structural contract* | `actor_ref` identifies who or what authored the event.  Human actors use UUID identity. System actors use:  ```text system:{sou... | [L850-897](canonical-decision-ledger.md#L850-L897) |

### 6. Storage and sync model

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-019 | **Atomic write unit is a typed immutable event** | *Structural constraint* | The atomic write unit is a typed immutable event. Each event records what happened with action-specific payload. Current state... | [L901-939](canonical-decision-ledger.md#L901-L939) |
| CDL-020 | **Client-generated UUIDs identify events, subjects, and records** | *Structural constraint* | Events, subjects, and records use client-generated UUIDs. Devices can mint identifiers offline without server coordination. | [L941-979](canonical-decision-ledger.md#L941-L979) |
| CDL-021 | **Sync transfers immutable events idempotently and by scope** | *Structural constraint* | Sync transfers immutable events the receiver has not yet seen, filtered by the receiver’s sync/access scope. Sync is idempotent... | [L981-1021](canonical-decision-ledger.md#L981-L1021) |

### 7. Identity and conflict

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-022 | **Duplicate real-world identity is resolved by identity mechanisms** | *Strategy-protecting constraint* | If two offline devices create different UUIDs for the same real-world thing, both UUIDs remain valid stored identities. Duplica... | [L1025-1063](canonical-decision-ledger.md#L1025-L1063) |
| CDL-023 | **Identity merge is alias-in-projection, never re-reference** | *Structural constraint* | An identity merge is recorded as `shape_ref = subjects_merged/v1`. It creates an alias mapping from retired identity to survivi... | [L1065-1104](canonical-decision-ledger.md#L1065-L1104) |
| CDL-024 | **Split freezes history and archives the source** | *Structural constraint* | An identity split is recorded as `shape_ref = subject_split/v1`. The source identity becomes terminally archived. Historical ev... | [L1106-1145](canonical-decision-ledger.md#L1106-L1145) |
| CDL-025 | **No unmerge operation exists** | *Structural constraint / rejected alternative* | The platform does not define an unmerge operation. A wrong merge is corrected by splitting the surviving identity and creating... | [L1147-1185](canonical-decision-ledger.md#L1147-L1185) |
| CDL-026 | **Identity lineage is acyclic by construction** | *Structural invariant* | Identity lineage is a directed acyclic graph enforced at write time.  - Merge operands must be active. - Merge survivor must be... | [L1187-1230](canonical-decision-ledger.md#L1187-L1230) |
| CDL-027 | **Merge and split are online-only operations** | *Strategy-protecting constraint* | Identity merge and identity split require server-validated online transactions. | [L1232-1269](canonical-decision-ledger.md#L1232-L1269) |
| CDL-028 | **Conflict resolution is single-writer and server-validated** | *Strategy-protecting constraint* | A flag raised by `shape_ref = conflict_detected/v1` designates exactly one resolver. A manual `shape_ref = conflict_resolved/v1... | [L1271-1311](canonical-decision-ledger.md#L1271-L1311) |
| CDL-029 | **Conflict detection uses raw references before alias projection** | *Structural constraint* | Conflict detection evaluates incoming events using original references as written. Alias resolution happens afterward in projec... | [L1313-1351](canonical-decision-ledger.md#L1313-L1351) |

### 8. Authorization and selective sync

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-030 | **Access is assignment-based** | *Structural constraint* | Access reduces to: the actor has an active assignment whose scope contains the target entity and whose role permits the intende... | [L1355-1394](canonical-decision-ledger.md#L1355-L1394) |
| CDL-031 | **Sync scope equals access scope** | *Structural constraint* | A device receives exactly the data its current actor is authorized to access. Sync scope and access scope are the same boundary. | [L1396-1434](canonical-decision-ledger.md#L1396-L1434) |
| CDL-032 | **Authority is projected, not stored in the envelope** | *Structural constraint* | The event envelope has no `authority_context` field. Authority is reconstructed from assignment timelines, actor identity, even... | [L1436-1474](canonical-decision-ledger.md#L1436-L1474) |
| CDL-033 | **Authorization uses original subject scope, not post-merge scope** | *Structural constraint* | Authorization evaluates an event against the original `subject_ref` and scope context as written at event creation, not the pos... | [L1476-1514](canonical-decision-ledger.md#L1476-L1514) |
| CDL-034 | **Assignment creation enforces scope containment** | *Strategy-protecting security constraint* | An assignment creation command is server-validated so the new assignment scope is contained within the creating actor’s authori... | [L1516-1558](canonical-decision-ledger.md#L1516-L1558) |
| CDL-035 | **Authorization staleness is accepted, surfaced, and severity-controlled** | *Initial strategy / strategy-protecting constraint* | Work created under stale authorization state is accepted and flagged when anomalous. Flag severity controls whether the event b... | [L1560-1598](canonical-decision-ledger.md#L1560-L1598) |
| CDL-036 | **Projection location is tiered and evolvable** | *Initial strategy* | Projection location is tiered:  - Field workers use device-local projections from scoped events. - Supervisors use hybrid local... | [L1600-1642](canonical-decision-ledger.md#L1600-L1642) |
| CDL-037 | **Scope contraction data handling is device retention policy** | *Initial strategy / device policy boundary* | When an actor's scope contracts, the sync engine does not mutate or delete canonical events. Device-side handling follows selec... | [L1644-1692](canonical-decision-ledger.md#L1644-L1692) |

### 9. Configuration boundary

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-038 | **Configuration has a four-layer gradient and an L3-to-code ceiling** | *Strategy-protecting boundary* | Configuration is organized as:  ```text L0 Assembly L1 Shape L2 Logic L3 Policy ```  Configuration stops at L3. Behavior beyond... | [L1696-1745](canonical-decision-ledger.md#L1696-L1745) |
| CDL-039 | **Shapes are deployer-defined payload schemas with version coexistence** | *Structural / strategy-protecting constraint* | Deployers define shapes as typed payload schemas. Shapes evolve through explicit versions. Old and new shape versions coexist;... | [L1747-1789](canonical-decision-ledger.md#L1747-L1789) |
| CDL-040 | **Shapes may be authored as deltas but are stored as full snapshots** | *Initial strategy* | Deployers may author a new shape version as a delta, but runtime shape registry entries are stored as complete snapshots. | [L1791-1828](canonical-decision-ledger.md#L1791-L1828) |
| CDL-041 | **Configuration packages are atomic at sync** | *Strategy-protecting constraint* | Configuration is delivered as an atomic package at sync. Devices apply a new package only after in-progress work under the prev... | [L1830-1870](canonical-decision-ledger.md#L1830-L1870) |
| CDL-042 | **L3 policy executes server-only** | *Strategy-protecting constraint* | All L3 policy execution is server-only. This includes event-reaction policy and deadline/async policy. Devices use L2 logic for... | [L1872-1910](canonical-decision-ledger.md#L1872-L1910) |
| CDL-043 | **Expression language is bounded and non-programmatic** | *Strategy-protecting constraint* | There is one bounded expression language with context-specific data access. It uses operators and field references only; it has... | [L1912-1954](canonical-decision-ledger.md#L1912-L1954) |
| CDL-044 | **Trigger and configuration complexity budgets are enforced at deploy time** | *Strategy-protecting constraint* | Deploy-time validation enforces configuration consistency, dependency validity, trigger acyclicity, and hard complexity budgets... | [L1956-2007](canonical-decision-ledger.md#L1956-L2007) |
| CDL-045 | **Domain uniqueness rules are shape-declared; resolution strategy is separate** | *Initial strategy / boundary* | Shapes may declare domain uniqueness constraints evaluated optimistically on device and authoritatively on server. Violations p... | [L2009-2049](canonical-decision-ledger.md#L2009-L2049) |
| CDL-046 | **Sensitivity is shape/activity-level configuration** | *Initial strategy* | Sensitivity is configured at shape or activity level using three levels:  ```text standard elevated restricted ```  Sensitivity... | [L2051-2097](canonical-decision-ledger.md#L2051-L2097) |

### 10. Workflow and state progression

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-047 | **Workflow state is projection-derived, not stored or enforced by rejection** | *Strategy-protecting constraint* | Workflow state is derived by projection from event history and pattern definitions. State is never stored as canonical current... | [L2101-2141](canonical-decision-ledger.md#L2101-L2141) |
| CDL-048 | **`transition_violation` is a platform flag category** | *Strategy-protecting constraint* | The detector evaluates incoming events against pattern-defined state-machine rules and emits a `transition_violation` flag cate... | [L2143-2181](canonical-decision-ledger.md#L2143-L2181) |
| CDL-049 | **Pattern registry is platform-fixed; activities bind patterns** | *Primitive / config split* | The platform provides a closed pattern registry: platform-owned workflow skeletons that deployers select and parameterize throu... | [L2183-2222](canonical-decision-ledger.md#L2183-L2222) |
| CDL-050 | **Pattern composition is bounded and validated** | *Initial strategy* | Pattern composition follows these rules:  ```text 1. One subject-level pattern per activity. 2. Event-level patterns compose fr... | [L2224-2271](canonical-decision-ledger.md#L2224-L2271) |
| CDL-051 | **Source-only flagging is the workflow cascade model** | *Initial strategy* | Only the root-cause event receives a flag. Downstream contamination is computed through source-chain traversal in projections,... | [L2273-2312](canonical-decision-ledger.md#L2273-L2312) |
| CDL-052 | **`context.*` is bounded form context** | *Initial strategy* | Form expressions may read a platform-fixed `context.*` namespace. Initial properties are:  ```text context.subject_state contex... | [L2314-2366](canonical-decision-ledger.md#L2314-L2366) |
| CDL-053 | **Auto-resolution is L3b policy and emits normal resolution events** | *Initial strategy* | Auto-resolution is a server-side L3b policy subtype. It observes eligible flags and emits a normal `shape_ref = conflict_resolv... | [L2368-2408](canonical-decision-ledger.md#L2368-L2408) |
| CDL-054 | **Flag resolvability is platform-classified** | *Strategy-protecting constraint* | Each flag category has platform-defined resolvability:  ```text auto_eligible manual_only ```  Initial classification:  \| Flag... | [L2410-2470](canonical-decision-ledger.md#L2410-L2470) |

### 11. Platform-fixed vs deployer-configured split

| ID | Title | Classification | Key Directives / Summary | Line Range |
| --- | --- | --- | --- | --- |
| CDL-055 | **Scope mechanism is platform-fixed; scope instances are configuration** | *Primitive / config split* | The scope mechanism is platform-fixed. Initial scope types are:  ```text geographic subject_list activity ```  Scope compositio... | [L2474-2521](canonical-decision-ledger.md#L2474-L2521) |
| CDL-056 | **Activity is deployer configuration; `activity_ref` is a contract** | *Config / contract split* | An activity is a deployer-assembled L0 configuration instance composed from platform-provided components: shapes, patterns, rol... | [L2523-2561](canonical-decision-ledger.md#L2523-L2561) |

