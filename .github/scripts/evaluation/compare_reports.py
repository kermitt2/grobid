#!/usr/bin/env python3
"""
Summarise the Markdown reports of an end-to-end evaluation, optionally against a baseline.

    compare_reports.py --reports reports/ [--baseline baseline/] > summary.md

Both directories hold the `report-<dataset>.md` files written by `jatsEval`. Each dataset
gets one table of the aggregated scores and, when a baseline is given, the list of the
fields whose F1 moved. A dataset missing from the baseline is reported on its own.
"""

import argparse
import glob
import os
import re
import sys

SECTIONS = ["Header metadata", "Citation metadata", "Fulltext structures"]
MATCHINGS = ["Strict", "Soft", "Levenshtein", "Ratcliff/Obershelp"]
MICRO = "all fields (micro avg.)"
MACRO = "all fields (macro avg.)"

# qualifiers of the instance-level lines, as spelled in the report
INSTANCE_MATCHINGS = {
    "strict": "Strict",
    "soft": "Soft",
    "Levenshtein": "Levenshtein",
    "RatcliffObershelp": "Ratcliff/Obershelp",
    "ObservedRatcliffObershelp": "Ratcliff/Obershelp",
}

INSTANCE_LINE = re.compile(r"^Instance-level (recall|precision|f-score):\s*([\d.]+)\s*\((\w+)\)")
CONTEXT_LINE = re.compile(r"^(Precision|Recall|fscore) citation contexts:\s*([\d.]+)")
DOCUMENTS_LINE = re.compile(r"^Evaluation on (\d+) random PDF files")


def to_number(cell):
    try:
        return float(cell)
    except ValueError:
        return None


def parse_report(path):
    """
    Return the scores of a report as
      fields:    {(section, matching): {label: f1}}
      instances: {(section, measure, matching): value}
      contexts:  {measure: value}
      documents: number of evaluated documents, from the first section stating it
    """
    report = {"fields": {}, "instances": {}, "contexts": {}, "documents": None}
    section = None
    matching = None
    table = None

    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line.startswith("## "):
                section, matching, table = line[3:].strip(), None, None
            elif line.startswith("#### "):
                title = line[5:].strip()
                matching = title.split(" Matching")[0] if " Matching" in title else None
                table = None
            elif line.startswith("**") and line.endswith("**"):
                # only the field-level table of a matching is read, not e.g. the
                # document-level ratio one that follows it
                table = line.strip("*") if matching else None
            elif line.startswith("|") and section and matching and table == "Field-level results":
                cells = [cell.strip().strip("*") for cell in line.strip("|").split("|")]
                if len(cells) == 5 and cells[0] and to_number(cells[3]) is not None:
                    report["fields"].setdefault((section, matching), {})[cells[0]] = to_number(cells[3])
            elif section and (m := INSTANCE_LINE.match(line)):
                key = (section, m.group(1), INSTANCE_MATCHINGS.get(m.group(3), m.group(3)))
                report["instances"][key] = float(m.group(2))
            elif m := CONTEXT_LINE.match(line):
                report["contexts"][m.group(1)] = float(m.group(2))
            elif report["documents"] is None and (m := DOCUMENTS_LINE.match(line)):
                report["documents"] = int(m.group(1))
    return report


def delta(value, base):
    if base is None:
        return ""
    diff = round(value - base, 2)
    if diff == 0:
        return " (=)"
    return f" ({diff:+.2f})"


def cell(value, base):
    if value is None:
        return "—"
    return f"{value:g}{delta(value, base)}"


def summary_rows(report, baseline):
    """Yield (row title, {matching: cell}) for the aggregated scores of a report."""

    def field(source, section, matching, label):
        return source["fields"].get((section, matching), {}).get(label) if source else None

    def instance(source, section, measure, matching):
        return source["instances"].get((section, measure, matching)) if source else None

    for section in SECTIONS:
        if not any(key[0] == section for key in report["fields"]):
            continue
        name = section.split()[0]
        for label, title in ((MICRO, "micro avg. F1"), (MACRO, "macro avg. F1")):
            yield f"{name}, {title}", {
                m: cell(field(report, section, m, label), field(baseline, section, m, label)) for m in MATCHINGS
            }
        # headers are scored on instance recall only, citations on the full P/R/F1
        measure = "f-score" if (section, "f-score", "Strict") in report["instances"] else "recall"
        if (section, measure, "Strict") in report["instances"]:
            title = "F1" if measure == "f-score" else "recall"
            yield f"{name}, instance-level {title}", {
                m: cell(instance(report, section, measure, m), instance(baseline, section, measure, m))
                for m in MATCHINGS
            }


def changed_fields(report, baseline):
    """Return the (section, matching, label, f1, baseline f1) whose F1 differs from the baseline."""
    changes = []
    for section in SECTIONS:
        for matching in MATCHINGS:
            fields = report["fields"].get((section, matching), {})
            base_fields = baseline["fields"].get((section, matching), {})
            for label in sorted(set(fields) | set(base_fields)):
                if label in (MICRO, MACRO):
                    continue
                value, base = fields.get(label), base_fields.get(label)
                if value != base:
                    changes.append((section, matching, label, value, base))
    return changes


def render_dataset(name, report, baseline, out):
    out.write(f"### {name}\n\n")
    if report["documents"]:
        out.write(f"Evaluated on {report['documents']} PDF")
        if baseline and baseline["documents"] and baseline["documents"] != report["documents"]:
            # a delta over different document sets also reflects which documents were covered
            out.write(f" (**baseline: {baseline['documents']} PDF**, the deltas are over different document sets)")
        out.write(".\n\n")
    if baseline is None:
        out.write("No baseline report for this dataset: scores are shown without deltas.\n\n")

    out.write("| | " + " | ".join(MATCHINGS) + " |\n")
    out.write("|---|" + "---|" * len(MATCHINGS) + "\n")
    for title, cells in summary_rows(report, baseline):
        out.write(f"| {title} | " + " | ".join(cells[m] for m in MATCHINGS) + " |\n")
    if "fscore" in report["contexts"]:
        base = baseline["contexts"].get("fscore") if baseline else None
        out.write(f"| Citation contexts, F1 | {cell(report['contexts']['fscore'], base)} |" + " |" * 3 + "\n")
    out.write("\n")

    if baseline is None:
        return
    changes = changed_fields(report, baseline)
    if not changes:
        out.write("No field-level F1 differs from the baseline.\n\n")
        return
    out.write(f"<details>\n<summary>Field-level F1 that differ from the baseline ({len(changes)})</summary>\n\n")
    out.write("| section | matching | field | F1 | baseline | delta |\n|---|---|---|---|---|---|\n")
    for section, matching, label, value, base in changes:
        diff = f"{value - base:+.2f}" if value is not None and base is not None else "—"
        out.write(
            f"| {section.split()[0]} | {matching} | {label} | "
            f"{'—' if value is None else f'{value:g}'} | {'—' if base is None else f'{base:g}'} | {diff} |\n"
        )
    out.write("\n</details>\n\n")


def load_reports(directory):
    reports = {}
    for path in sorted(glob.glob(os.path.join(directory, "report-*.md"))):
        name = os.path.basename(path)[len("report-"):-len(".md")]
        reports[name] = parse_report(path)
    return reports


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--reports", required=True, help="directory holding the report-<dataset>.md of the run")
    parser.add_argument("--baseline", help="directory holding the report-<dataset>.md to compare against")
    args = parser.parse_args()

    reports = load_reports(args.reports)
    if not reports:
        sys.exit(f"No report-*.md found under {args.reports}")
    baselines = load_reports(args.baseline) if args.baseline and os.path.isdir(args.baseline) else {}

    for name, report in reports.items():
        render_dataset(name, report, baselines.get(name), sys.stdout)


if __name__ == "__main__":
    main()
