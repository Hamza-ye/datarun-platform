#!/usr/bin/env python3
import yaml
import re
import datetime
from pathlib import Path

# Paths
REPO_ROOT = Path(__file__).parent.parent
REPO = "Hamza-ye/datarun-platform"
REGISTRY_PATH = REPO_ROOT / "docs/platform-spec-kernels/platform-spec/atom-registry.yml"
GAP_REGISTER_PATH = REPO_ROOT / "docs/platform-spec-kernels/professional-baseline/05-decision-gap-register.md"
OPEN_DECISIONS_PATH = REPO_ROOT / "docs/platform-spec-kernels/platform-spec/atoms/90-open-decisions.md"
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

def parse_open_decisions(path):
    decisions = []
    if not path.exists():
        return decisions

    with open(path, 'r') as f:
        content = f.read()

    in_table = False
    for line in content.splitlines():
        if line.startswith("| Area | Status | Primary Route |"):
            in_table = True
            continue
        if in_table and line.startswith("|---"):
            continue
        if in_table and line.startswith("| "):
            cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
            if len(cells) >= 3:
                decisions.append({
                    'area': cells[0],
                    'status': cells[1],
                    'route': cells[2],
                })
            continue
        if in_table:
            break

    return decisions

def atom_by_id(atoms):
    return {atom.get('id'): atom for atom in atoms}

def active_boundaries(registry, atoms):
    atoms_by_id = atom_by_id(atoms)
    boundaries = []
    for atom_id in registry.get('next_recommended', []):
        atom = atoms_by_id.get(atom_id)
        boundary = atom.get('boundary') if atom else None
        if boundary and boundary not in boundaries:
            boundaries.append(boundary)
    return boundaries

def decisions_for_boundaries(open_decisions, boundaries):
    return [
        decision for decision in open_decisions
        if any(boundary in decision['route'] for boundary in boundaries)
    ]

def main():
    registry = load_yaml(REGISTRY_PATH)
    atoms = registry.get('atoms', [])
    gaps = parse_gaps(GAP_REGISTER_PATH)
    open_decisions = parse_open_decisions(OPEN_DECISIONS_PATH)
    focus_boundaries = active_boundaries(registry, atoms)
    focus_decisions = decisions_for_boundaries(open_decisions, focus_boundaries)
    
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
        f.write(f"| Professional Baseline Gaps | {len(gaps)} |\n")
        f.write(f"| Atomization Open Decisions | {len(open_decisions)} |\n\n")

        f.write("## Governance Flow\n\n")
        f.write("| Layer | Source | Dashboard Role |\n")
        f.write("|---|---|---|\n")
        f.write(f"| Accepted baseline gap register | [05-decision-gap-register.md](platform-spec-kernels/professional-baseline/05-decision-gap-register.md) | Source-authority gap inventory ({len(gaps)} items). |\n")
        f.write(f"| Atomization control register | [90-open-decisions.md](platform-spec-kernels/platform-spec/atoms/90-open-decisions.md) | Spec-facing hold-backs and open decisions ({len(open_decisions)} items). |\n")
        f.write("| Draft atoms | `platform-spec-kernels/platform-spec/atoms/` | Carry relevant gaps without closing them. |\n")
        f.write("| Review and acceptance | Challenge Review, Integration Review, steward recommendation, approval | Promote only after evidence and explicit status update. |\n\n")

        if focus_boundaries:
            f.write("## Active Atomization Risk Surface\n\n")
            f.write(f"Next recommended boundaries: {', '.join(focus_boundaries)}.\n\n")
            f.write("| Area | Status | Primary Route |\n")
            f.write("|---|---|---|\n")
            for decision in focus_decisions:
                f.write(f"| {decision['area']} | {decision['status']} | {decision['route']} |\n")
            f.write("\n")
        
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
        f.write("<details>\n")
        f.write("<summary>Professional baseline gaps</summary>\n\n")
        f.write("| Gap | Classification |\n")
        f.write("|---|---|\n")
        for gap in gaps:
            f.write(f"| {gap['title']} | {gap['classification']} |\n")
        f.write("\n</details>\n")

        f.write("\n<details>\n")
        f.write("<summary>Atomization open decisions and hold-backs</summary>\n\n")
        f.write("| Area | Status | Primary Route |\n")
        f.write("|---|---|---|\n")
        for decision in open_decisions:
            f.write(f"| {decision['area']} | {decision['status']} | {decision['route']} |\n")
        f.write("\n</details>\n")

    print(f"Generated {DASHBOARD_PATH}")

if __name__ == "__main__":
    main()
