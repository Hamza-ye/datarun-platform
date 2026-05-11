#!/usr/bin/env python3
import yaml
import re
import os
import datetime
from pathlib import Path

# Paths
REPO_ROOT = Path(__file__).parent.parent
REPO = "Hamza-ye/datarun-platform"
REGISTRY_PATH = REPO_ROOT / "docs/platform-spec-kernels/platform-spec/atom-registry.yml"
GAP_REGISTER_PATH = REPO_ROOT / "docs/platform-spec-kernels/professional-baseline/05-decision-gap-register.md"
DASHBOARD_PATH = REPO_ROOT / "docs/DASHBOARD.md"

def load_yaml(path):
    if not path.exists():
        return {}
    with open(path, 'r') as f:
        return yaml.safe_load(f)

def parse_gaps(path):
    gaps = []
    if not path.exists():
        return gaps
    
    with open(path, 'r') as f:
        content = f.read()
    
    # Simple extraction of gap headers and details
    # Look for ### Gap Title \n\n Classification: ...
    matches = re.finditer(r'### (.*?)\n\nClassification: (.*?)\n', content, re.MULTILINE)
    for m in matches:
        gaps.append({
            'title': m.group(1),
            'classification': m.group(2)
        })
    return gaps

def main():
    registry = load_yaml(REGISTRY_PATH)
    atoms = registry.get('atoms', [])
    gaps = parse_gaps(GAP_REGISTER_PATH)
    
    # Summary stats
    total_atoms = len(atoms)
    accepted_atoms = len([a for a in atoms if a.get('status') == 'accepted'])
    draft_atoms = len([a for a in atoms if a.get('status') == 'draft'])
    planned_atoms = len([a for a in atoms if a.get('status') == 'planned'])
    
    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    with open(DASHBOARD_PATH, 'w') as f:
        f.write("# Platform Spec Dashboard\n\n")
        f.write(f"> [!IMPORTANT]\n")
        f.write(f"> **How to sync:** Run `./scripts/sync.sh` (or `python3 scripts/generate-dashboard.py` for local-only) after any change to the registry or gap register.\n")
        f.write(f"> Last regenerated: {now}\n\n")
        
        f.write("## Execution Summary\n\n")
        f.write("| Metric | Value |\n")
        f.write("|---|---|\n")
        f.write(f"| Total Atoms | {total_atoms} |\n")
        f.write(f"| Accepted | {accepted_atoms} |\n")
        f.write(f"| Draft | {draft_atoms} |\n")
        f.write(f"| Planned | {planned_atoms} |\n")
        f.write(f"| Open Gaps | {len(gaps)} |\n\n")
        
        f.write("## Atom Registry\n\n")
        f.write("| ID | Title | Status | Batch | Boundary | Dependencies | Issue |\n")
        f.write("|---|---|---|---|---|---|---|\n")
        for atom in atoms:
            deps = ", ".join(atom.get('depends_on', [])) or "-"
            # Make link relative to docs root where DASHBOARD.md resides
            file_link = f"platform-spec-kernels/platform-spec/{atom['file']}"
            issue_num = atom.get('github_issue')
            issue_link = f"[#{issue_num}](https://github.com/{REPO}/issues/{issue_num})" if issue_num else "-"
            f.write(f"| {atom['id']} | [{atom['title']}]({file_link}) | {atom['status']} | {atom['batch']} | {atom['boundary']} | {deps} | {issue_link} |\n")
        
        f.write("\n## Open Decisions / Gaps\n\n")
        f.write("| Gap | Classification |\n")
        f.write("|---|---|\n")
        for gap in gaps:
            f.write(f"| {gap['title']} | {gap['classification']} |\n")

    print(f"Generated {DASHBOARD_PATH}")

if __name__ == "__main__":
    main()
