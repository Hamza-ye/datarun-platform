#!/usr/bin/env bash
set -e

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

echo "== Syncing GitHub Project and Issues =="
python3 scripts/sync-github.py

echo ""
echo "== Regenerating Local Dashboard =="
python3 scripts/generate-dashboard.py

echo ""
echo "Done. View the dashboard at docs/DASHBOARD.md"
