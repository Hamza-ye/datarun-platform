#!/usr/bin/env python3
import yaml
import re
import datetime
from pathlib import Path

# Paths
REPO_ROOT = Path(__file__).parent.parent
REPO = "Hamza-ye/datarun-platform"
REGISTRY_PATH = REPO_ROOT / "docs/platform-spec-kernels/platform-spec/section-registry.yml"
GAP_REGISTER_PATH = REPO_ROOT / "docs/platform-spec-kernels/professional-baseline/05-decision-gap-register.md"
OPEN_DECISIONS_PATH = REPO_ROOT / "docs/platform-spec-kernels/platform-spec/sections/90-open-decisions-and-gap-register-citations.md"
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
    table_shape = None
    for line in content.splitlines():
        if line.startswith("| Area | Status | Primary Route |"):
            in_table = True
            table_shape = "area"
            continue
        if line.startswith("| Gap From `05` | Applies To | Required Handling |"):
            in_table = True
            table_shape = "gap"
            continue
        if in_table and line.startswith("|---"):
            continue
        if in_table and line.startswith("| "):
            cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
            if len(cells) >= 3 and table_shape == "area":
                decisions.append({
                    'area': cells[0],
                    'status': cells[1],
                    'route': cells[2],
                })
            elif len(cells) >= 3 and table_shape == "gap":
                decisions.append({
                    'area': cells[0],
                    'status': cells[2],
                    'route': cells[1],
                })
            continue
        if in_table:
            break

    return decisions

def section_by_id(sections):
    return {section.get('id'): section for section in sections}

def active_boundaries(registry, sections):
    sections_by_id = section_by_id(sections)
    boundaries = []
    for section_id in registry.get('next_recommended', []):
        section = sections_by_id.get(section_id)
        boundary = section.get('boundary') if section else None
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
    sections = registry.get('sections', [])
    gaps = parse_gaps(GAP_REGISTER_PATH)
    open_decisions = parse_open_decisions(OPEN_DECISIONS_PATH)
    focus_boundaries = active_boundaries(registry, sections)
    focus_decisions = decisions_for_boundaries(open_decisions, focus_boundaries)
    
    # Summary stats
    total_sections = len(sections)
    accepted_sections = len([s for s in sections if s.get('status') == 'accepted'])
    draft_sections = len([s for s in sections if s.get('status') == 'draft'])
    planned_sections = len([s for s in sections if s.get('status') == 'planned'])
    
    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    with open(DASHBOARD_PATH, 'w') as f:
        f.write("# Platform Spec Dashboard\n\n")
        f.write(f"> [!IMPORTANT]\n")
        f.write(f"> **How to sync:** Run `./scripts/sync.sh` (or `python3 scripts/generate-dashboard.py` for local-only) after any change to the registry or gap register.\n")
        f.write(f"> Last regenerated: {now}\n\n")
        
        f.write("## Execution Summary\n\n")
        f.write("| Metric | Value |\n")
        f.write("|---|---|\n")
        f.write(f"| Total Sections | {total_sections} |\n")
        f.write(f"| Accepted | {accepted_sections} |\n")
        f.write(f"| Draft | {draft_sections} |\n")
        f.write(f"| Planned | {planned_sections} |\n")
        f.write(f"| Professional Baseline Gaps | {len(gaps)} |\n")
        f.write(f"| Platform-Spec Open Decisions | {len(open_decisions)} |\n\n")

        f.write("## Governance Flow\n\n")
        f.write("| Layer | Source | Dashboard Role |\n")
        f.write("|---|---|---|\n")
        f.write(f"| Accepted baseline gap register | [05-decision-gap-register.md](platform-spec-kernels/professional-baseline/05-decision-gap-register.md) | Source-authority gap inventory ({len(gaps)} items). |\n")
        f.write(f"| Open-decision citations | [90-open-decisions-and-gap-register-citations.md](platform-spec-kernels/platform-spec/sections/90-open-decisions-and-gap-register-citations.md) | Spec-facing hold-backs and open decisions ({len(open_decisions)} items). |\n")
        f.write("| Draft sections | `platform-spec-kernels/platform-spec/sections/` | Carry relevant gaps without closing them. |\n")
        f.write("| Review and acceptance | Challenge Review, Integration Review, steward recommendation, approval | Promote only after evidence and explicit status update. |\n\n")

        if focus_boundaries:
            f.write("## Active Platform-Spec Risk Surface\n\n")
            f.write(f"Next recommended boundaries: {', '.join(focus_boundaries)}.\n\n")
            f.write("| Area | Status | Primary Route |\n")
            f.write("|---|---|---|\n")
            for decision in focus_decisions:
                f.write(f"| {decision['area']} | {decision['status']} | {decision['route']} |\n")
            f.write("\n")
        
        f.write("## Section Registry\n\n")
        f.write("| ID | Title | Status | Batch | Boundary | Dependencies | Issue |\n")
        f.write("|---|---|---|---|---|---|---|\n")
        for section in sections:
            deps = ", ".join(section.get('depends_on', [])) or "-"
            # Make link relative to docs root where DASHBOARD.md resides
            file_link = f"platform-spec-kernels/platform-spec/{section['file']}"
            issue_num = section.get('github_issue')
            issue_link = f"[#{issue_num}](https://github.com/{REPO}/issues/{issue_num})" if issue_num else "-"
            f.write(f"| {section['id']} | [{section['title']}]({file_link}) | {section['status']} | {section['batch']} | {section['boundary']} | {deps} | {issue_link} |\n")
        
        f.write("\n## Open Decisions / Gaps\n\n")
        f.write("<details>\n")
        f.write("<summary>Professional baseline gaps</summary>\n\n")
        f.write("| Gap | Classification |\n")
        f.write("|---|---|\n")
        for gap in gaps:
            f.write(f"| {gap['title']} | {gap['classification']} |\n")
        f.write("\n</details>\n")

        f.write("\n<details>\n")
        f.write("<summary>Platform-spec open decisions and hold-backs</summary>\n\n")
        f.write("| Area | Status | Primary Route |\n")
        f.write("|---|---|---|\n")
        for decision in open_decisions:
            f.write(f"| {decision['area']} | {decision['status']} | {decision['route']} |\n")
        f.write("\n</details>\n")

    print(f"Generated {DASHBOARD_PATH}")

if __name__ == "__main__":
    main()
