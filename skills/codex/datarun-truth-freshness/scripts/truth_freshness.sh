#!/usr/bin/env bash
set -u

repo_root="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$repo_root" || exit 2

verdict="FRESH"
issues=()

section() {
  printf '\n== %s ==\n' "$1"
}

adr_id_from_path() {
  base="$(basename "$1" .md)"
  number="$(printf '%s\n' "$base" | sed -nE 's/^adr-0*([0-9]+).*/\1/p')"
  [ -z "$number" ] && return
  suffix="$(printf '%s\n' "$base" | grep -oE 'R[0-9]*$' || true)"
  if [ -n "$suffix" ]; then
    printf 'ADR-%03d-%s\n' "$number" "$suffix"
  else
    printf 'ADR-%03d\n' "$number"
  fi
}

add_issue() {
  issues+=("$1")
  if [ "$verdict" = "FRESH" ]; then
    verdict="MECHANICALLY-CLEAN-BUT-STALE"
  fi
}

section "1. Drift gate"
if bash scripts/check-convergence.sh; then
  drift_ok=1
else
  drift_ok=0
  verdict="DRIFT-GATE-FAIL"
fi

section "2. Dirty worktree"
dirty="$(git status --short)"
if [ -n "$dirty" ]; then
  printf '%s\n' "$dirty"
  authority_dirty="$(printf '%s\n' "$dirty" | grep -E 'docs/(charter|convergence|adrs|flagged-positions|ships)|contracts/flag-catalog|scripts/check-convergence' || true)"
  if [ -n "$authority_dirty" ]; then
    verdict="BLOCKED-BY-DIRTY-WORKTREE"
    issues+=("Authority files are dirty; freshness cannot be trusted without user direction.")
  fi
else
  printf 'clean\n'
fi

section "3. Last charter and ledger commits"
last_charter_commit="$(git log -n 1 --format='%h %s' -- docs/charter.md 2>/dev/null || true)"
last_charter_hash="$(printf '%s\n' "$last_charter_commit" | awk '{print $1}')"
last_ledger_commit="$(git log -n 1 --format='%h %s' -- docs/convergence/concept-ledger.md 2>/dev/null || true)"
printf 'charter: %s\n' "${last_charter_commit:-not found}"
printf 'ledger:  %s\n' "${last_ledger_commit:-not found}"

section "4. Commits since last charter change"
if [ -n "$last_charter_hash" ]; then
  commits_since_charter="$(git log --oneline "${last_charter_hash}..HEAD" || true)"
  if [ -n "$commits_since_charter" ]; then
    printf '%s\n' "$commits_since_charter" | sed 's/^/  /'
  else
    printf 'none\n'
  fi
else
  commits_since_charter=""
  printf 'no charter commit found\n'
fi

post_charter_truth_commits="$(printf '%s\n' "$commits_since_charter" | grep -E 'docs\(adr\)|docs\(contracts\)|docs\(ship-|docs\(ships\)|docs\(ledger\)|docs\(charter\)|docs\(audit\)|feat\(ship-|fix\(ship-|test\(ship-' || true)"
if [ -n "$post_charter_truth_commits" ]; then
  add_issue "Commits that can change current truth exist after the last charter update."
fi

section "5. Latest ADR activity"
git log --oneline --max-count=12 -- docs/adrs | sed 's/^/  /' || true
latest_adr_commit="$(git log -n 1 --format='%h %s' -- docs/adrs 2>/dev/null || true)"
printf 'latest ADR commit: %s\n' "${latest_adr_commit:-not found}"

section "6. ADR supersession check"
supersedes_lines="$(grep -RIn '^> Supersedes:' docs/adrs 2>/dev/null || true)"
if [ -n "$supersedes_lines" ]; then
  printf '%s\n' "$supersedes_lines"
else
  printf 'no Supersedes lines found\n'
fi

while IFS= read -r line; do
  [ -z "$line" ] && continue
  file="${line%%:*}"
  new_adr="$(adr_id_from_path "$file")"
  old_adr="$(printf '%s\n' "$line" | grep -oE 'ADR-[0-9]+' | head -n 1 || true)"
  [ -z "$old_adr" ] && continue

  if grep -q "$old_adr" docs/charter.md && ! grep -q "$new_adr" docs/charter.md; then
    add_issue "Charter still cites $old_adr while $file declares a supersession."
  fi

  old_file="$(
    ls docs/adrs/adr-"$(printf '%s' "$old_adr" | sed 's/ADR-//')"-*.md 2>/dev/null \
      | grep -v -- '-R' \
      | head -n 1 || true
  )"
  if [ -n "$old_file" ] && ! grep -q 'Superseded-By:' "$old_file"; then
    add_issue "$old_file lacks a Superseded-By header for supersession declared in $file."
  fi
done <<EOF
$supersedes_lines
EOF

section "7. Charter status excerpts"
grep -nE 'Active ship|Next action|Last ADR landed|Active FPs|Ledger state' docs/charter.md || true

latest_adr_name="$(printf '%s\n' "$latest_adr_commit" | grep -oE 'ADR-[0-9]+-R[0-9]*|ADR-[0-9]+' | head -n 1 || true)"
if [ -n "$latest_adr_name" ] && ! grep -q "$latest_adr_name" docs/charter.md; then
  add_issue "Latest ADR activity mentions $latest_adr_name, but charter does not mention it."
fi

section "8. FP register reflection"
fp_statuses="$(
  awk '
    /^## FP-[0-9]+/ { fp=$2 }
    /^\*\*Status\*\*:/ {
      status=$0
      sub(/^\*\*Status\*\*: */, "", status)
      sub(/ .*/, "", status)
      gsub(/\*/, "", status)
      if (fp != "" && status ~ /^(OPEN|IN_PROGRESS|RESOLVED|SUPERSEDED)$/) print fp " " status
    }
  ' docs/flagged-positions.md
)"
printf 'FP statuses in register:\n'
printf '%s\n' "$fp_statuses" | sed 's/^/  /'

resolved_fps="$(printf '%s\n' "$fp_statuses" | awk '$2 == "RESOLVED" { print $1 }')"
open_fps="$(printf '%s\n' "$fp_statuses" | awk '$2 == "OPEN" { print $1 }')"
superseded_fps="$(printf '%s\n' "$fp_statuses" | awk '$2 == "SUPERSEDED" { print $1 }')"
charter_fp_line="$(grep -E '^\*\*Active FPs\*\*:' docs/charter.md || true)"

missing_fps=()
while IFS= read -r fp; do
  [ -z "$fp" ] && continue
  if ! grep -q "$fp" docs/charter.md; then
    missing_fps+=("$fp")
    add_issue "$fp is OPEN in flagged-positions but absent from charter FP summary."
  fi
done <<EOF
$open_fps
EOF

while IFS= read -r fp; do
  [ -z "$fp" ] && continue
  if ! grep -q "$fp" docs/charter.md; then
    missing_fps+=("$fp")
    add_issue "$fp is SUPERSEDED in flagged-positions but absent from charter FP summary."
  fi
done <<EOF
$superseded_fps
EOF

while IFS= read -r fp; do
  [ -z "$fp" ] && continue
  if ! grep -q "$fp" docs/charter.md; then
    missing_fps+=("$fp")
    add_issue "$fp is RESOLVED in flagged-positions but absent from charter FP summary."
  fi
done <<EOF
$resolved_fps
EOF

if [ "${#missing_fps[@]}" -gt 0 ]; then
  printf 'missing from charter FP summary:\n'
  printf '  %s\n' "${missing_fps[@]}"
else
  printf 'all FP statuses mentioned in charter summary\n'
fi

if [ -n "$charter_fp_line" ]; then
  charter_fps="$(printf '%s\n' "$charter_fp_line" | grep -oE 'FP-[0-9]+[a-z]?' | sort -u)"
  actual_fps="$(printf '%s\n' "$fp_statuses" | awk '{ print $1 }' | sort -u)"
  while IFS= read -r fp; do
    [ -z "$fp" ] && continue
    if ! printf '%s\n' "$actual_fps" | grep -qx "$fp"; then
      add_issue "$fp is mentioned in charter FP summary but has no register entry."
    fi
  done <<EOF
$charter_fps
EOF
fi

section "9. Ledger count reflection"
ledger_counts="$(
  awk -F'|' '
    BEGIN { total=0 }
    index($0, "|") == 1 && NF >= 8 {
      status=$5
      gsub(/^ +| +$/, "", status)
      if (status ~ /^(OPEN|PROPOSED|STABLE|DEFERRED|SUPERSEDED|OBSOLETE|DISPUTED)$/) {
        counts[status]++
        total++
      }
    }
    END {
      printf "STABLE=%d DEFERRED=%d OBSOLETE=%d OPEN=%d PROPOSED=%d SUPERSEDED=%d DISPUTED=%d TOTAL=%d\n",
        counts["STABLE"], counts["DEFERRED"], counts["OBSOLETE"], counts["OPEN"],
        counts["PROPOSED"], counts["SUPERSEDED"], counts["DISPUTED"], total
    }
  ' docs/convergence/concept-ledger.md
)"
printf 'actual ledger counts: %s\n' "$ledger_counts"

stable_count="$(printf '%s\n' "$ledger_counts" | sed -nE 's/.*STABLE=([0-9]+).*/\1/p')"
deferred_count="$(printf '%s\n' "$ledger_counts" | sed -nE 's/.*DEFERRED=([0-9]+).*/\1/p')"
obsolete_count="$(printf '%s\n' "$ledger_counts" | sed -nE 's/.*OBSOLETE=([0-9]+).*/\1/p')"
open_count="$(printf '%s\n' "$ledger_counts" | sed -nE 's/.*OPEN=([0-9]+).*/\1/p')"
total_count="$(printf '%s\n' "$ledger_counts" | sed -nE 's/.*TOTAL=([0-9]+).*/\1/p')"

expected_ledger_phrase="${stable_count} STABLE / ${deferred_count} DEFERRED / ${obsolete_count} OBSOLETE / ${open_count} OPEN / ${total_count} total"
if ! grep -q "$expected_ledger_phrase" docs/charter.md; then
  add_issue "Charter ledger state does not match actual ledger counts ($expected_ledger_phrase)."
fi

section "10. Flag catalog concepts in ledger"
flag_rows="$(grep -E '^\| [0-9]+ \| `' contracts/flag-catalog.md 2>/dev/null | awk '!seen[$2]++' || true)"
if [ -z "$flag_rows" ]; then
  printf 'no flag catalog rows found\n'
else
  printf '%s\n' "$flag_rows"
fi

while IFS= read -r row; do
  [ -z "$row" ] && continue
  category="$(printf '%s\n' "$row" | sed -nE 's/^\| [0-9]+ \| `([^`]+)`.*/\1/p')"
  [ -z "$category" ] && continue
  hyphen_category="$(printf '%s' "$category" | tr '_' '-')"
  if ! grep -q "| $category |" docs/convergence/concept-ledger.md && ! grep -q "| $hyphen_category |" docs/convergence/concept-ledger.md; then
    add_issue "Flag catalog category $category is missing from concept-ledger."
  fi
done <<EOF
$flag_rows
EOF

section "11. Suggested close order"
if [ "${#issues[@]}" -eq 0 ]; then
  printf 'none; repo truth surfaces appear fresh\n'
else
  printf '1. Resolve drift-gate failure if present.\n'
  printf '2. Resolve dirty authority files or confirm they are the intended repair scope.\n'
  printf '3. Repair ADR supersession headers and charter cites.\n'
  printf '4. Add or update ledger rows/counts for new concepts.\n'
  printf '5. Reconcile charter FP summary with flagged-positions register.\n'
  printf '6. Rerun this script before Ship/framework work.\n'
fi

section "12. Issues"
if [ "${#issues[@]}" -eq 0 ]; then
  printf 'none\n'
else
  for issue in "${issues[@]}"; do
    printf -- '- %s\n' "$issue"
  done
fi

section "VERDICT"
if [ "$drift_ok" -eq 0 ]; then
  verdict="DRIFT-GATE-FAIL"
fi
printf '%s\n' "$verdict"
