#!/usr/bin/env python3
import yaml
import subprocess
import json
import os
from pathlib import Path

# Config
REPO = "Hamza-ye/datarun-platform"
PROJECT_ID = "PVT_kwHOAIMf1M4BXZW2"
PROJECT_NUMBER = 5
OWNER = "Hamza-ye"

# Field IDs from 'gh project field-list'
FIELDS = {
    'spec_id': 'PVTF_lAHOAIMf1M4BXZW2zhSmiqw',
    'batch': 'PVTF_lAHOAIMf1M4BXZW2zhSmiy4',
    'boundary': 'PVTF_lAHOAIMf1M4BXZW2zhSmkRA',
    'status': 'PVTSSF_lAHOAIMf1M4BXZW2zhSmdYw',
    'owner_role': 'PVTF_lAHOAIMf1M4BXZW2zhSmkrM',
    'review_state': 'PVTF_lAHOAIMf1M4BXZW2zhSmkxc',
    'source_file': 'PVTF_lAHOAIMf1M4BXZW2zhSmvSc',
    'change_control': 'PVTF_lAHOAIMf1M4BXZW2zhSmvTs',
    'blocks': 'PVTF_lAHOAIMf1M4BXZW2zhSmvXA',
    'gap': 'PVTF_lAHOAIMf1M4BXZW2zhSmwGI',
    'depends_on': 'PVTF_lAHOAIMf1M4BXZW2zhSmw98',
    'source_basis': 'PVTF_lAHOAIMf1M4BXZW2zhSmxRY',
    'github_issue_field': 'PVTF_lAHOAIMf1M4BXZW2zhSmxfE',
}

# Status Option IDs
STATUS_OPTIONS = {
    'planned': 'c5089ee5',
    'draft': '816b6859',
    'accepted': '589144e6',
    'deferred': '02e99a74',
    'hold-back': 'cf77e5e7',
    'rejected': '09b9361c'
}

def run_gh(args):
    cmd = ["gh"] + args
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error running gh {' '.join(args)}: {result.stderr}")
        return None
    return result.stdout.strip()

def main():
    # Load registry
    registry_path = Path("docs/platform-spec-kernels/platform-spec/section-registry.yml")
    if not registry_path.exists():
        print(f"Error: {registry_path} not found.")
        return

    with open(registry_path, 'r') as f:
        registry = yaml.safe_load(f)
    
    sections = registry.get('sections', [])
    updated_registry = False

    for section in sections:
        section_id = section['id']
        title = section['title']
        
        # 1. Create/Find Issue
        issue_number = section.get('github_issue')
        if not issue_number:
            print(f"Creating issue for {section_id}...")
            body = f"## {title}\n\n"
            body += f"**ID:** {section_id}\n"
            body += f"**Boundary:** {section['boundary']}\n"
            body += f"**Batch:** {section['batch']}\n\n"
            body += f"**Status:** {section['status']}\n\n"
            body += f"### Trace\n"
            body += f"- **File:** [docs/platform-spec-kernels/platform-spec/{section['file']}](https://github.com/{REPO}/blob/main/docs/platform-spec-kernels/platform-spec/{section['file']})\n"
            body += f"- **Basis:** {', '.join(section.get('source_basis', []))}\n"
            
            issue_url = run_gh(["issue", "create", "--repo", REPO, "--title", f"{section_id}: {title}", "--body", body])
            if issue_url and issue_url.startswith("https://"):
                issue_number = int(issue_url.split("/")[-1])
                section['github_issue'] = issue_number
                updated_registry = True
                print(f"Created issue #{issue_number}")
            else:
                print(f"Failed to create issue for {section_id}")
                continue
        else:
            print(f"Using existing issue #{issue_number} for {section_id}")

        # 2. Add to Project
        # We use the URL to add the issue to the project
        issue_url = f"https://github.com/{REPO}/issues/{issue_number}"
        item_json = run_gh(["project", "item-add", str(PROJECT_NUMBER), "--owner", OWNER, "--url", issue_url, "--format", "json"])
        if not item_json:
            continue
        
        item_id = json.loads(item_json)['id']

        # 3. Update Project Fields
        def update_field(field_key, value):
            if value is None: return
            field_id = FIELDS.get(field_key)
            if not field_id: return
            
            if field_key == 'status':
                opt_id = STATUS_OPTIONS.get(value.lower())
                if opt_id:
                    run_gh(["project", "item-edit", "--id", item_id, "--field-id", field_id, "--project-id", PROJECT_ID, "--single-select-option-id", opt_id])
                else:
                    # If status is not in our restricted list, maybe try text? (Usually single select fails if not in options)
                    pass
            else:
                # Text fields
                run_gh(["project", "item-edit", "--id", item_id, "--field-id", field_id, "--project-id", PROJECT_ID, "--text", str(value)])

        print(f"Syncing fields for {section_id}...")
        update_field('spec_id', section_id)
        update_field('batch', str(section['batch']))
        update_field('boundary', section['boundary'])
        update_field('status', section['status'])
        update_field('owner_role', section['owner_role'])
        update_field('source_file', section['file'])
        update_field('depends_on', ", ".join(section.get('depends_on', [])))
        update_field('blocks', ", ".join(section.get('blocks', [])))
        update_field('source_basis', ", ".join(section.get('source_basis', []))[:250])
        update_field('github_issue_field', str(issue_number))

    # Save registry if updated
    if updated_registry:
        with open(registry_path, 'w') as f:
            # We want to preserve the structure as much as possible
            yaml.dump(registry, f, sort_keys=False, default_flow_style=False)
        print("Updated section-registry.yml with issue numbers.")

if __name__ == "__main__":
    main()
