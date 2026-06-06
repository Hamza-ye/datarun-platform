#!/usr/bin/env python3
import os
import re
import json

# Paths
WORKSPACE_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LEDGER_PATH = os.path.join(WORKSPACE_ROOT, "docs", "architecture", "adrs-decisions-canonical-ledger", "canonical-decision-ledger.md")
JSON_OUTPUT_PATH = os.path.join(WORKSPACE_ROOT, "docs", "architecture", "adrs-decisions-canonical-ledger", "canonical-decision-ledger.json")
README_OUTPUT_PATH = os.path.join(WORKSPACE_ROOT, "docs", "architecture", "adrs-decisions-canonical-ledger", "README.md")

def parse_ledger():
    if not os.path.exists(LEDGER_PATH):
        raise FileNotFoundError(f"Ledger file not found at {LEDGER_PATH}")

    with open(LEDGER_PATH, "r", encoding="utf-8") as f:
        lines = f.readlines()

    decisions = []
    current_category = "General"
    current_decision = None
    current_section = None
    section_content = []
    current_category_start = None
    current_category_content = []

    # Regex patterns
    category_pattern = re.compile(r"^##\s+(\d+\.\s+.*)")
    decision_pattern = re.compile(r"^###\s+(CDL-\d+):\s+(.*)")
    status_pattern = re.compile(r"^\*\*Status:\*\*\s*(.*)")
    classification_pattern = re.compile(r"^\*\*Classification:\*\*\s*(.*)")
    section_header_pattern = re.compile(r"^\*\*(Decision|Rationale|Rejected alternatives|Binding constraints|Guardrails|Must not happen|Scope boundary|Downstream impact):\*\*\s*(.*)")

    def save_current_section():
        nonlocal current_section, section_content, current_decision
        if current_decision and current_section:
            content_str = "".join(section_content).strip()
            # Clean up trailing spaces/newlines
            current_decision["sections"][current_section] = content_str
            current_section = None
            section_content = []

    def save_current_decision(end_line):
        nonlocal current_decision, decisions
        if current_decision:
            save_current_section()
            current_decision["end_line"] = end_line
            decisions.append(current_decision)
            current_decision = None

    def save_current_category(end_line):
        """If there is non-decision content directly under a category header, save it
        as a synthetic category entry so it appears in the JSON index."""
        nonlocal current_category, current_category_content, current_category_start, decisions
        if current_category and current_category_content:
            content_str = "".join(current_category_content).strip()
            if content_str:
                # Use the category start line to create a stable synthetic id
                if current_category_start:
                    cat_num = str(current_category_start).zfill(6)
                else:
                    cat_num = "000000"
                cat_id = f"CAT-{cat_num}"
                cat_entry = {
                    "id": cat_id,
                    "title": current_category,
                    "category": current_category,
                    "start_line": current_category_start,
                    "end_line": end_line,
                    "status": "Index",
                    "classification": "Index",
                    "sections": {"Content": content_str}
                }
                decisions.append(cat_entry)
        # reset category content buffer
        current_category_content = []
        current_category_start = None

    for i, line in enumerate(lines):
        line_num = i + 1  # 1-indexed

        # Check for category change
        cat_match = category_pattern.match(line)
        if cat_match:
            # Close any open decision and capture any category-level content
            save_current_decision(line_num - 1)
            save_current_category(line_num - 1)
            current_category = cat_match.group(1).strip()
            current_category_start = line_num
            current_category_content = []
            continue

        # Check for horizontal rule or end of decision block
        if line.strip() == "---" and current_decision:
            save_current_decision(line_num)
            continue

        # Check for decision header
        dec_match = decision_pattern.match(line)
        if dec_match:
            save_current_decision(line_num - 1)
            current_decision = {
                "id": dec_match.group(1).strip(),
                "title": dec_match.group(2).strip(),
                "category": current_category,
                "start_line": line_num,
                "end_line": None,
                "status": "Accepted",
                "classification": "Invariant",
                "sections": {}
            }
            current_section = None
            section_content = []
            continue

        if current_decision:
            # Parse status
            stat_match = status_pattern.match(line)
            if stat_match:
                current_decision["status"] = stat_match.group(1).strip().replace("  ", "")
                continue

            # Parse classification
            class_match = classification_pattern.match(line)
            if class_match:
                current_decision["classification"] = class_match.group(1).strip().replace("  ", "")
                continue

            # Parse section headers (e.g. Decision, Rationale)
            sec_match = section_header_pattern.match(line)
            if sec_match:
                save_current_section()
                current_section = sec_match.group(1)
                inline_content = sec_match.group(2).strip()
                if inline_content:
                    section_content = [inline_content + "\n"]
                else:
                    section_content = []
                continue

            # Accumulate content for the active section
            if current_section:
                section_content.append(line)
        else:
            # Not inside a decision: accumulate any category-level content so
            # sections like the index pages (Rejected alternatives, Must-not-happen,
            # Deferred boundary) are captured.
            if current_category is not None:
                current_category_content.append(line)

    # Save last decision if exists
    save_current_decision(len(lines))
    # Save any trailing category content
    save_current_category(len(lines))

    # Add tags based on category & title keywords
    for dec in decisions:
        tags = set()
        # Derive tags from category
        cat_lower = dec["category"].lower()
        if "invariant" in cat_lower:
            tags.add("invariant")
        if "envelope" in cat_lower:
            tags.add("envelope")
        if "sync" in cat_lower or "storage" in cat_lower:
            tags.add("sync")
            tags.add("storage")
        if "identity" in cat_lower or "conflict" in cat_lower:
            tags.add("identity")
            tags.add("conflict")
        if "authorization" in cat_lower:
            tags.add("authorization")
            tags.add("access-control")
        if "configuration" in cat_lower:
            tags.add("configuration")
        if "workflow" in cat_lower:
            tags.add("workflow")

        # Derive tags from title keywords
        title_lower = dec["title"].lower()
        keywords = ["envelope", "uuid", "sync", "watermark", "time", "payload", "shape", "activity",
                    "actor", "subject", "merge", "split", "flag", "unmerge", "conflict", "resolver",
                    "access", "scope", "policy", "expression", "trigger", "unique", "transition"]
        for kw in keywords:
            if kw in title_lower:
                tags.add(kw)
        
        # Add classification as tag
        tags.add(dec["classification"].lower().replace(" ", "-"))
        dec["tags"] = sorted(list(tags))

    return decisions

def generate_readme(decisions):
    readme_content = []
    readme_content.append("# Canonical Decision Ledger - AI Index & Reading Guide\n")
    readme_content.append(">\n")
    readme_content.append(">[!IMPORTANT]\n")
    readme_content.append("> The **[Canonical Decision Ledger](canonical-decision-ledger.md)** (CDL) is the single authoritative source of architectural decisions for the Datarun Platform, established and active at the closure of **[Phase 4: Workflow & Policies](../../implementation/phases/phase-4.md)** forward.\n")
    readme_content.append("> It has been rigorously validated against the retired ADRs 001–009 and is 100% consistent with them.\n")
    readme_content.append(">\n")
    readme_content.append(">**Date:** 2026-05-27\n")
    readme_content.append(">\n")
    readme_content.append("## Machine Access & AI Slicing\n\n")
    readme_content.append("To keep LLM context windows clean, do not read the full 2600+ line ledger for everyday development tasks. Instead, use the structured JSON catalog or the categorized index below.\n\n")
    readme_content.append("### Formats Available\n\n")
    readme_content.append("1. **Markdown Ledger (Authority):** [canonical-decision-ledger.md](canonical-decision-ledger.md)\n")
    readme_content.append("2. **Structured JSON Index:** [canonical-decision-ledger.json](canonical-decision-ledger.json)\n")
    readme_content.append("3. **Query CLI Tool:** Run `python3 scripts/query_cdl.py --help` to search/slice decisions by tags, ID(s), category, or search term.\n\n")
    
    readme_content.append("### Querying Ledger via Command Line\n\n")
    readme_content.append("An agent can run queries like:\n\n")
    readme_content.append("```bash\n# Find all decisions related to identity or merges\npython3 scripts/query_cdl.py --tag identity\n\n# Get details for a specific decision, or multiple decisions (comma/space-separated)\npython3 scripts/query_cdl.py --id CDL-021\npython3 scripts/query_cdl.py --id CDL-001,CDL-002\n\n# Search for decisions containing specific text\npython3 scripts/query_cdl.py --search \"watermark\"\n```\n\n")


    readme_content.append("## Categorized Decision Catalog\n\n")
    
    # Group decisions by category
    categories = {}
    for dec in decisions:
        cat = dec["category"]
        if cat not in categories:
            categories[cat] = []
        categories[cat].append(dec)

    for cat, dec_list in categories.items():
        readme_content.append(f"### {cat}\n\n")
        readme_content.append("| ID | Title | Classification | Key Directives / Summary | Line Range |\n")
        readme_content.append("| --- | --- | --- | --- | --- |\n")
        for dec in dec_list:
            decision_text = dec["sections"].get("Decision", "")
            # Truncate decision text for summary
            summary = decision_text.replace("\n", " ").strip()
            # Escape pipe characters so they don't break markdown tables
            summary = summary.replace("|", "\\|")
            if len(summary) > 130:
                summary = summary[:127]
                if summary.endswith("\\"):
                    summary = summary[:-1]
                summary = summary.strip() + "..."
            
            # Format lines link
            line_link = f"[L{dec['start_line']}-{dec['end_line']}](canonical-decision-ledger.md#L{dec['start_line']}-L{dec['end_line']})"
            readme_content.append(f"| {dec['id']} | **{dec['title']}** | *{dec['classification']}* | {summary} | {line_link} |\n")
        readme_content.append("\n")

    return "".join(readme_content)

def main():
    print(f"Parsing canonical ledger from: {LEDGER_PATH}")
    decisions = parse_ledger()
    print(f"Parsed {len(decisions)} decisions successfully.")

    # Write JSON output
    with open(JSON_OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(decisions, f, indent=2)
    print(f"Wrote structured JSON index to: {JSON_OUTPUT_PATH}")

    # Write Markdown README index
    readme_str = generate_readme(decisions)
    with open(README_OUTPUT_PATH, "w", encoding="utf-8") as f:
        f.write(readme_str)
    print(f"Wrote human-readable README index to: {README_OUTPUT_PATH}")

if __name__ == "__main__":
    main()
