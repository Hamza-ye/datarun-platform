#!/usr/bin/env python3
from collections import Counter
import datetime
import re
from pathlib import Path

import yaml


REPO_ROOT = Path(__file__).parent.parent
DOCS_ROOT = REPO_ROOT / "docs"
KERNEL_ROOT = DOCS_ROOT / "platform-spec-kernels"
PLATFORM_SPEC_ROOT = KERNEL_ROOT / "platform-spec"

REGISTRY_PATH = PLATFORM_SPEC_ROOT / "section-registry.yml"
BASELINE_PATH = KERNEL_ROOT / "professional-baseline/04-architecture-baseline-v0.md"
GAP_REGISTER_PATH = KERNEL_ROOT / "professional-baseline/05-decision-gap-register.md"
RESPONSIBILITY_MAP_PATH = KERNEL_ROOT / "professional-baseline/07-system-boundary-map.md"
OUTLINE_PATH = KERNEL_ROOT / "professional-baseline/20-platform-spec-outline.md"
GAP_CITATIONS_PATH = PLATFORM_SPEC_ROOT / "sections/90-open-decisions-and-gap-register-citations.md"
DASHBOARD_PATH = DOCS_ROOT / "DASHBOARD.md"


def read_text(path):
    return path.read_text(encoding="utf-8") if path.exists() else ""


def load_yaml(path):
    if not path.exists():
        return {}
    return yaml.safe_load(path.read_text(encoding="utf-8")) or {}


def docs_link(label, path):
    if not path.exists():
        return f"`{label}` (not created)"
    rel = path.relative_to(DOCS_ROOT).as_posix()
    return f"[{label}]({rel})"


def section_path(path_text):
    return PLATFORM_SPEC_ROOT / path_text


def format_candidate_inputs(inputs):
    if not inputs:
        return "-"
    links = []
    for path_text in inputs:
        path = section_path(path_text)
        links.append(docs_link(path_text, path))
    return "<br>".join(links)


def parse_outline_sections(path):
    sections = {}
    for match in re.finditer(r"^### (\d{2})\. (.+)$", read_text(path), re.MULTILINE):
        number, title = match.groups()
        sections[number] = title.strip()
    return sections


def parse_gap_register(path):
    gaps = []
    content = read_text(path)
    matches = re.finditer(r"^### (.+?)\n\nClassification: (.+?)$", content, re.MULTILINE)
    for match in matches:
        gaps.append({
            "title": match.group(1).strip(),
            "classification": match.group(2).strip(),
        })
    return gaps


def parse_gap_citations(path):
    citations = []
    in_table = False
    for line in read_text(path).splitlines():
        if line.startswith("| Gap From `05` | Applies To | Required Handling |"):
            in_table = True
            continue
        if in_table and line.startswith("|---"):
            continue
        if in_table and line.startswith("| "):
            cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
            if len(cells) >= 3:
                citations.append({
                    "gap": cells[0],
                    "applies_to": cells[1],
                    "handling": cells[2],
                })
            continue
        if in_table:
            break
    return citations


def registry_sections(registry):
    return registry.get("sections", [])


def alignment_findings(outline_sections, sections):
    findings = []
    registry_by_number = {section.get("section"): section for section in sections}
    outline_numbers = set(outline_sections)
    registry_numbers = set(registry_by_number)

    for number in sorted(outline_numbers - registry_numbers):
        findings.append(f"Outline section {number} is missing from the manifest.")
    for number in sorted(registry_numbers - outline_numbers):
        findings.append(f"Manifest section {number} is not in the outline.")
    for number in sorted(outline_numbers & registry_numbers):
        manifest_title = registry_by_number[number].get("title")
        outline_title = outline_sections[number]
        if manifest_title != outline_title:
            findings.append(
                f"Section {number} title differs: outline `{outline_title}`, manifest `{manifest_title}`."
            )

    return findings


def file_counts(sections):
    targets = [section_path(section["file"]) for section in sections if section.get("file")]
    candidates = [
        section_path(candidate)
        for section in sections
        for candidate in section.get("candidate_inputs", [])
    ]
    return {
        "target_present": len([path for path in targets if path.exists()]),
        "target_total": len(targets),
        "candidate_present": len([path for path in candidates if path.exists()]),
        "candidate_total": len(candidates),
    }


def write_table_row(handle, cells):
    handle.write("| " + " | ".join(str(cell) for cell in cells) + " |\n")


def main():
    registry = load_yaml(REGISTRY_PATH)
    sections = registry_sections(registry)
    outline_sections = parse_outline_sections(OUTLINE_PATH)
    gaps = parse_gap_register(GAP_REGISTER_PATH)
    citations = parse_gap_citations(GAP_CITATIONS_PATH)
    status_counts = Counter(section.get("status", "unknown") for section in sections)
    findings = alignment_findings(outline_sections, sections)
    counts = file_counts(sections)
    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    with DASHBOARD_PATH.open("w", encoding="utf-8") as handle:
        handle.write("# Platform Specification Snapshot\n\n")
        handle.write("> [!NOTE]\n")
        handle.write("> This dashboard owns no architecture, gap, acceptance, implementation, or remote-tracking state. ")
        handle.write("It is a generated local snapshot from accepted baseline inputs plus the non-authoritative section manifest.\n")
        handle.write("> Regenerate with `python3 scripts/generate-dashboard.py` after changing the baseline, gap register, outline, first-section citations, or manifest.\n")
        handle.write(f"> Last regenerated: {now}\n\n")

        handle.write("## Source Inputs\n\n")
        handle.write("| Input | Standing In This Snapshot |\n")
        handle.write("|---|---|\n")
        write_table_row(handle, [
            docs_link("04 Architecture Baseline v0", BASELINE_PATH),
            "Accepted architecture baseline.",
        ])
        write_table_row(handle, [
            docs_link("05 Decision Gap Register", GAP_REGISTER_PATH),
            "Canonical open-gap and open-decision register.",
        ])
        write_table_row(handle, [
            docs_link("07 Architecture Responsibility Map", RESPONSIBILITY_MAP_PATH),
            "Responsibility routing only.",
        ])
        write_table_row(handle, [
            docs_link("20 Platform-Spec Outline", OUTLINE_PATH),
            "Draft outline from accepted baseline; source for section structure and blocker assessment.",
        ])
        write_table_row(handle, [
            docs_link("section-registry.yml", REGISTRY_PATH),
            "Non-authoritative local manifest for paths, status labels, and candidate inputs.",
        ])
        write_table_row(handle, [
            docs_link("90 Gap Citations", GAP_CITATIONS_PATH),
            "Draft citation surface for first-section blockers and hold-backs; does not replace 05.",
        ])
        handle.write("\n")

        handle.write("## Snapshot Counts\n\n")
        handle.write("| Metric | Value |\n")
        handle.write("|---|---:|\n")
        write_table_row(handle, ["Outline sections", len(outline_sections)])
        write_table_row(handle, ["Manifest sections", len(sections)])
        for status in registry.get("status_values", []):
            write_table_row(handle, [f"Manifest status: {status}", status_counts.get(status, 0)])
        unknown_statuses = sorted(set(status_counts) - set(registry.get("status_values", [])))
        if unknown_statuses:
            write_table_row(handle, ["Manifest status: unknown/unlisted", ", ".join(unknown_statuses)])
        write_table_row(handle, ["Target section files present", f"{counts['target_present']} of {counts['target_total']}"])
        write_table_row(handle, ["Candidate input files present", f"{counts['candidate_present']} of {counts['candidate_total']}"])
        write_table_row(handle, ["Canonical gap entries visible in 05", len(gaps)])
        write_table_row(handle, ["First-section gap citations visible in 90", len(citations)])
        handle.write("\n")

        handle.write("## Manifest Alignment\n\n")
        if findings:
            handle.write("| Finding |\n")
            handle.write("|---|\n")
            for finding in findings:
                write_table_row(handle, [finding])
        else:
            handle.write("The manifest section numbers and titles match the current outline headings in `20-platform-spec-outline.md`.\n")
        handle.write("\n")

        handle.write("## Section Manifest\n\n")
        handle.write("| Section | Title | Status | Owner | Target File | Candidate Input | First Slice |\n")
        handle.write("|---|---|---|---|---|---|---|\n")
        for section in sections:
            file_label = section.get("file", "-")
            file_value = docs_link(file_label, section_path(file_label)) if file_label != "-" else "-"
            write_table_row(handle, [
                section.get("section", "-"),
                section.get("title", "-"),
                section.get("status", "-"),
                section.get("primary_owner", "-"),
                file_value,
                format_candidate_inputs(section.get("candidate_inputs", [])),
                "yes" if section.get("first_slice_focus") else "no",
            ])
        handle.write("\n")

        handle.write("## First-Slice Focus\n\n")
        handle.write("This is a snapshot of what the outline says can be drafted first. It is not acceptance or implementation approval.\n\n")
        handle.write("| Section | Status | Snapshot Note |\n")
        handle.write("|---|---|---|\n")
        for section in sections:
            if section.get("first_slice_focus"):
                write_table_row(handle, [
                    section.get("section", "-"),
                    section.get("status", "-"),
                    section.get("note", "-"),
                ])
        handle.write("\n")

        handle.write("## First-Section Gap Citations\n\n")
        handle.write("These rows are read from the draft `90` section. `05` remains canonical.\n\n")
        handle.write("| Gap From 05 | Applies To | Required Handling |\n")
        handle.write("|---|---|---|\n")
        for citation in citations:
            write_table_row(handle, [
                citation["gap"],
                citation["applies_to"],
                citation["handling"],
            ])
        handle.write("\n")

        handle.write("## Canonical Gap Register Snapshot\n\n")
        handle.write("<details>\n")
        handle.write("<summary>Gap headings and classifications from 05</summary>\n\n")
        handle.write("| Gap | Classification |\n")
        handle.write("|---|---|\n")
        for gap in gaps:
            write_table_row(handle, [gap["title"], gap["classification"]])
        handle.write("\n</details>\n")

    print(f"Generated {DASHBOARD_PATH}")


if __name__ == "__main__":
    main()
