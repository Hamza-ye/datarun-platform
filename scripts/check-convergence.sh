#!/usr/bin/env bash
#
# Convergence drift gate.
#
# Runs in pre-commit and CI. During Phase 0–3 it reports counts; at Phase 4
# freeze it switches to strict mode (any failure = non-zero exit).
#
# Lifespan: permanent. Becomes a CI hard-fail at Phase 4 freeze.
#
# Checks (incremental — full enforcement at freeze):
#   1. Every charter claim that includes an ADR cite must point to a DECIDED,
#      non-superseded ADR (or the -R replacement if superseded).
#   2. Every concept-ledger `settled-by` cite must resolve to a DECIDED ADR.
#   3. Every `>>> OPEN-Q:` in any doc must have a matching `>>> CLOSED BY`,
#      `>>> STALE`, `>>> RECLASSIFIED BY`, or `>>> ABSORBED INTO` annotation
#      below it (Phase 4 freeze: count must be 0).
#
# Exit codes:
#   0 — pass (or report-only mode with no critical drift)
#   1 — drift detected; see output for which file/cite is wrong
#   2 — script error / missing dependency

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# Convergence phase is read from charter.md. In phase 4, gate is strict.
STRICT_MODE=0
if grep -qE '^\*\*Convergence phase\*\*:\s*4' docs/charter.md 2>/dev/null; then
    STRICT_MODE=1
fi

EXIT=0
report() { echo "  $*"; }
fail()   { echo "FAIL: $*"; EXIT=1; }
warn()   { echo "WARN: $*"; [[ $STRICT_MODE -eq 1 ]] && EXIT=1 || true; }

echo "== convergence drift gate =="
[[ $STRICT_MODE -eq 1 ]] && echo "(strict mode — Phase 4 freeze)" || echo "(report mode — pre-freeze)"

# --- Check 1: charter ADR cites resolve ---
echo
echo "[1/3] charter.md ADR cites"
if [[ ! -f docs/charter.md ]]; then
    fail "docs/charter.md missing"
else
    # Extract every [ADR-NNN...] cite from charter.md
    cites=$(grep -oE '\[ADR-[0-9]+(-R[0-9]*)?( §[^]]+)?\]' docs/charter.md | \
            sed -E 's/^\[//; s/\]$//' | sort -u || true)
    if [[ -z "$cites" ]]; then
        report "no ADR cites yet (expected during Phase 0–1)"
    else
        while IFS= read -r cite; do
            adr_id=$(echo "$cite" | grep -oE '^ADR-[0-9]+(-R[0-9]*)?')
            adr_num=$(echo "$adr_id" | sed -E 's/^ADR-//; s/-R[0-9]*$//')
            # Resolve to the canonical ADR file. Addenda are archival artifacts
            # under the convergence supersede rules (absorbed into successor
            # ADRs) and must not shadow the main ADR during cite resolution.
            adr_file=$(ls docs/adrs/adr-${adr_num}*.md 2>/dev/null | grep -v -- '-addendum-' | head -1 || true)
            if [[ -z "$adr_file" ]]; then
                fail "charter cites $cite but no matching adr file in docs/adrs/"
                continue
            fi
            # If cite is plain ADR-NNN (no -R), and the ADR has Superseded-By, fail
            if [[ "$adr_id" != *"-R"* ]] && grep -q '^Superseded-By:' "$adr_file"; then
                fail "charter cites $cite but $adr_file is Superseded-By another ADR (use the -R replacement)"
            fi
            # ADR must be DECIDED (accept plain or markdown-decorated blockquote form)
            if ! grep -qiE '^>?\s*Status:\s*\*{0,2}decided\*{0,2}' "$adr_file"; then
                fail "charter cites $cite but $adr_file is not Status: DECIDED"
            fi
        done <<< "$cites"
    fi
fi

# --- Check 2: concept-ledger settled-by cites resolve ---
echo
echo "[2/3] concept-ledger.md settled-by cites"
if [[ ! -f docs/convergence/concept-ledger.md ]]; then
    fail "docs/convergence/concept-ledger.md missing"
else
    # Extract settled-by cites only. Per ledger schema, settled-by is formatted
    # as `ADR-NNN §X` — the `§` anchor distinguishes real cites from prose
    # references (e.g. Phase 1 queue entries naming ADR-006..009 in summary).
    # Restrict to `|`-delimited table rows to further avoid prose matches.
    ledger_cites=$(grep -E '^\|' docs/convergence/concept-ledger.md | \
                   grep -oE 'ADR-[0-9]+(-R[0-9]*)? §' | \
                   grep -oE 'ADR-[0-9]+(-R[0-9]*)?' | sort -u || true)
    if [[ -z "$ledger_cites" ]]; then
        report "ledger has no ADR cites yet (expected during Phase 0)"
    else
        while IFS= read -r adr_id; do
            adr_num=$(echo "$adr_id" | sed -E 's/^ADR-//; s/-R[0-9]*$//')
            adr_file=$(ls docs/adrs/adr-${adr_num}*.md 2>/dev/null | grep -v -- '-addendum-' | head -1 || true)
            if [[ -z "$adr_file" ]]; then
                fail "ledger cites $adr_id but no matching adr file in docs/adrs/"
            elif ! grep -qiE '^>?\s*Status:\s*\*{0,2}decided\*{0,2}' "$adr_file"; then
                fail "ledger cites $adr_id but $adr_file is not Status: DECIDED"
            fi
        done <<< "$ledger_cites"
    fi
fi

# --- Check 3: open forward-refs in annotated docs ---
echo
echo "[3/3] open forward-refs (>>> OPEN-Q: without resolution)"
# Count `>>> OPEN-Q:` markers across all docs
open_count=$(grep -rEn '^>>> OPEN-Q:' docs/ 2>/dev/null | wc -l | tr -d ' ')
# Count resolution markers
closed_count=$(grep -rEn '^>>> (CLOSED BY|STALE|RECLASSIFIED BY|ABSORBED INTO)' docs/ 2>/dev/null | wc -l | tr -d ' ')

report "open: $open_count, resolved: $closed_count"

# Strict matching (line-number-aware) is Phase 2+ work. For now, compare counts:
# if open > resolved AND we're in Phase 4, that's a fail. Otherwise report only.
if [[ $STRICT_MODE -eq 1 ]] && (( open_count > closed_count )); then
    fail "Phase 4 freeze: $open_count open forward-refs vs $closed_count resolutions"
fi

echo
if [[ $EXIT -eq 0 ]]; then
    echo "PASS"
else
    echo "DRIFT DETECTED — fix before proceeding"
fi
exit $EXIT
