#!/usr/bin/env python3
import os
import sys
import json
import argparse

WORKSPACE_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JSON_PATH = os.path.join(WORKSPACE_ROOT, "docs", "architecture", "adrs-decisions-canonical-ledger", "canonical-decision-ledger.json")

def load_decisions():
    if not os.path.exists(JSON_PATH):
        print(f"Error: Structured ledger JSON not found at {JSON_PATH}", file=sys.stderr)
        print("Please run 'python3 scripts/generate_cdl_metadata.py' first.", file=sys.stderr)
        sys.exit(1)
    with open(JSON_PATH, "r", encoding="utf-8") as f:
        return json.load(f)

def print_concise_table(decisions):
    if not decisions:
        print("No matching decisions found.")
        return
    print(f"{'ID':<8} | {'Title':<60} | {'Classification':<30}")
    print("-" * 104)
    for d in decisions:
        title = d["title"]
        if len(title) > 60:
            title = title[:57] + "..."
        print(f"{d['id']:<8} | {title:<60} | {d['classification']:<30}")

def print_full_decision(d):
    print("=" * 80)
    print(f"ID:             {d['id']}")
    print(f"Title:          {d['title']}")
    print(f"Category:       {d['category']}")
    print(f"Status:         {d['status']}")
    print(f"Classification: {d['classification']}")
    print(f"Lines:          {d['start_line']} - {d['end_line']}")
    print(f"Tags:           {', '.join(d['tags'])}")
    print("=" * 80)
    
    for section_name in ["Decision", "Rationale", "Rejected alternatives", 
                         "Binding constraints", "Guardrails", "Must not happen", 
                         "Scope boundary", "Downstream impact"]:
        content = d["sections"].get(section_name)
        if content:
            print(f"\n[{section_name}]")
            print(content)
    print("\n" + "=" * 80)

def main():
    parser = argparse.ArgumentParser(description="Query and slice the Datarun Canonical Decision Ledger.")
    parser.add_argument("--id", help="Retrieve details for a specific decision ID, or a list of IDs separated by commas/spaces (e.g. CDL-001,CDL-002)")
    parser.add_argument("--tag", help="Filter decisions by tag (e.g. sync, identity, envelope)")
    parser.add_argument("--category", help="Filter decisions by category (e.g. '3. Canonical event envelope')")
    parser.add_argument("--search", help="Search titles, decisions, constraints, and must-not-happen for text")
    parser.add_argument("--format", choices=["concise", "full", "json"], default=None, 
                        help="Output format (default: concise for lists, full for ID queries)")

    args = parser.parse_args()
    decisions = load_decisions()

    # Determine default format
    fmt = args.format
    if fmt is None:
        fmt = "full" if args.id else "concise"

    # Query by ID
    if args.id:
        target_ids = [tid.upper().strip() for tid in args.id.replace(",", " ").split() if tid.strip()]
        found = [d for d in decisions if d["id"] in target_ids]
        if not found:
            print(f"No decisions found with IDs: {', '.join(target_ids)}", file=sys.stderr)
            sys.exit(1)
        if fmt == "json":
            print(json.dumps(found if len(found) > 1 else found[0], indent=2))
        elif fmt == "concise":
            print_concise_table(found)
        else:
            for d in found:
                print_full_decision(d)
        return

    # Filter/Search
    results = decisions
    
    if args.tag:
        tag_query = args.tag.lower().strip()
        results = [d for d in results if any(tag_query in t.lower() for t in d["tags"])]

    if args.category:
        cat_query = args.category.lower().strip()
        results = [d for d in results if cat_query in d["category"].lower()]

    if args.search:
        search_query = args.search.lower().strip()
        filtered = []
        for d in results:
            text_pool = [
                d["title"].lower(),
                d["classification"].lower(),
                d["category"].lower()
            ]
            for sec_name, sec_val in d["sections"].items():
                text_pool.append(sec_val.lower())
            
            if any(search_query in t for t in text_pool):
                filtered.append(d)
        results = filtered

    # Output results
    if fmt == "json":
        print(json.dumps(results, indent=2))
    elif fmt == "full":
        for d in results:
            print_full_decision(d)
    else:
        print_concise_table(results)

if __name__ == "__main__":
    main()
