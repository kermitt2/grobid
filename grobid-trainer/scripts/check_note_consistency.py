#!/usr/bin/env python3
"""
TEI Note Consistency Checker

This script checks TEI files for internal consistency issues where the same content
appears as both note place="headnote" and note place="footnote" within the same document.

Usage: python check_note_consistency.py <tei_directory> [--verbose]
"""

import os
import sys
import re
import argparse
from collections import defaultdict
from pathlib import Path


def extract_note_content(xml_file_path):
    """
    Extract note content from a TEI XML file.
    Returns two dictionaries: headnotes and footnotes with content as keys and line numbers as values.
    """
    headnotes = defaultdict(list)
    footnotes = defaultdict(list)

    try:
        with open(xml_file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        for i, line in enumerate(lines, 1):
            # Match note tags with place attributes
            headnote_match = re.search(r'<note\s+place=["\']headnote["\']>(.*?)</note>', line, re.DOTALL)
            footnote_match = re.search(r'<note\s+place=["\']footnote["\']>(.*?)</note>', line, re.DOTALL)

            if headnote_match:
                content = headnote_match.group(1).strip()
                # Normalize whitespace for better matching
                normalized_content = normalize_content(content)
                headnotes[normalized_content].append(i)

            if footnote_match:
                content = footnote_match.group(1).strip()
                # Normalize whitespace for better matching
                normalized_content = normalize_content(content)
                footnotes[normalized_content].append(i)

    except Exception as e:
        print(f"Error processing {xml_file_path}: {e}")
        return headnotes, footnotes

    return headnotes, footnotes


def normalize_content(content):
    """
    Normalize content for better matching by removing extra whitespace and standardizing line breaks.
    """
    # Replace multiple whitespace with single space
    content = re.sub(r'\s+', ' ', content)
    # Remove leading/trailing whitespace
    content = content.strip()
    return content


def check_file_consistency(xml_file_path, verbose=False):
    """
    Check a single TEI file for internal consistency issues.
    Returns list of inconsistencies found.
    """
    headnotes, footnotes = extract_note_content(xml_file_path)
    inconsistencies = []

    # Find content that appears in both headnotes and footnotes
    common_content = set(headnotes.keys()) & set(footnotes.keys())

    for content in common_content:
        # Only consider meaningful content (not just line breaks or very short content)
        if len(content) > 5 and not content == "<lb/>":
            inconsistency = {
                'content': content,
                'headnote_lines': headnotes[content],
                'footnote_lines': footnotes[content],
                'headnote_count': len(headnotes[content]),
                'footnote_count': len(footnotes[content]),
                'total_count': len(headnotes[content]) + len(footnotes[content])
            }
            inconsistencies.append(inconsistency)

    # Sort by severity (total occurrences)
    inconsistencies.sort(key=lambda x: x['total_count'], reverse=True)

    return inconsistencies


def check_directory(tei_directory, verbose=False):
    """
    Check all TEI files in a directory for consistency issues.
    Returns dictionary with files and their inconsistencies.
    """
    tei_directory = Path(tei_directory)
    if not tei_directory.exists():
        print(f"Error: Directory {tei_directory} does not exist")
        return {}

    # Find all XML files
    xml_files = list(tei_directory.glob("*.xml"))
    print(f"Found {len(xml_files)} TEI XML files to check...")

    all_inconsistencies = {}
    files_with_issues = 0
    total_inconsistencies = 0

    for xml_file in xml_files:
        inconsistencies = check_file_consistency(xml_file, verbose)

        if inconsistencies:
            all_inconsistencies[str(xml_file)] = inconsistencies
            files_with_issues += 1
            total_inconsistencies += len(inconsistencies)

            if verbose:
                print(f"❌ {xml_file.name}: {len(inconsistencies)} inconsistency issues")
                for i, issue in enumerate(inconsistencies, 1):
                    print(f"   Issue {i}: '{issue['content'][:50]}{'...' if len(issue['content']) > 50 else ''}'")
                    print(f"      Headnotes: {issue['headnote_count']} (lines: {issue['headnote_lines'][:3]}{'...' if len(issue['headnote_lines']) > 3 else ''})")
                    print(f"      Footnotes: {issue['footnote_count']} (lines: {issue['footnote_lines'][:3]}{'...' if len(issue['footnote_lines']) > 3 else ''})")
        elif verbose:
            print(f"✅ {xml_file.name}: No inconsistency issues found")

    print(f"\nSummary:")
    print(f"- Total files checked: {len(xml_files)}")
    print(f"- Files with inconsistencies: {files_with_issues}")
    print(f"- Total inconsistency instances: {total_inconsistencies}")

    return all_inconsistencies


def generate_report(inconsistencies, output_file=None):
    """
    Generate a detailed report of inconsistencies.
    """
    if not inconsistencies:
        print("No inconsistencies found! All files are internally consistent.")
        return

    report_lines = []
    report_lines.append("# TEI Note Consistency Report")
    report_lines.append("=" * 50)
    report_lines.append(f"Generated on: {__import__('datetime').datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    report_lines.append(f"Total files with issues: {len(inconsistencies)}")
    report_lines.append("")

    # Sort files by severity
    sorted_files = sorted(inconsistencies.items(),
                          key=lambda x: sum(issue['total_count'] for issue in x[1]),
                          reverse=True)

    for file_path, file_inconsistencies in sorted_files:
        filename = Path(file_path).name
        total_issues = sum(issue['total_count'] for issue in file_inconsistencies)

        report_lines.append(f"## 📄 {filename}")
        report_lines.append(f"**Total inconsistency instances: {total_issues}**")
        report_lines.append("")

        for i, issue in enumerate(file_inconsistencies, 1):
            # Truncate very long content for readability
            content_display = issue['content']
            if len(content_display) > 100:
                content_display = content_display[:100] + "..."

            report_lines.append(f"### Issue {i}: {issue['total_count']} occurrences")
            report_lines.append(f"**Content:** `{content_display}`")
            report_lines.append(f"- **Headnotes:** {issue['headnote_count']} occurrences (lines: {', '.join(map(str, issue['headnote_lines']))})")
            report_lines.append(f"- **Footnotes:** {issue['footnote_count']} occurrences (lines: {', '.join(map(str, issue['footnote_lines']))})")
            report_lines.append("")

        report_lines.append("---")
        report_lines.append("")

    report_content = "\n".join(report_lines)

    if output_file:
        try:
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(report_content)
            print(f"Report saved to: {output_file}")
        except Exception as e:
            print(f"Error saving report to {output_file}: {e}")
    else:
        print("\n" + report_content)


def main():
    parser = argparse.ArgumentParser(
        description="Check TEI files for note consistency issues",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python check_note_consistency.py /path/to/tei/files
  python check_note_consistency.py /path/to/tei/files --verbose
  python check_note_consistency.py /path/to/tei/files --report consistency_report.md
        """
    )

    parser.add_argument(
        'tei_directory',
        help='Directory containing TEI XML files to check'
    )

    parser.add_argument(
        '--verbose', '-v',
        action='store_true',
        help='Show detailed output for each file'
    )

    parser.add_argument(
        '--report', '-r',
        help='Output file for detailed report (Markdown format)'
    )

    args = parser.parse_args()

    print("🔍 Starting TEI Note Consistency Check...")
    print(f"📁 Checking directory: {args.tei_directory}")

    inconsistencies = check_directory(args.tei_directory, args.verbose)

    if inconsistencies:
        generate_report(inconsistencies, args.report)

        # Show summary of most problematic files
        print(f"\n🚨 Most problematic files:")
        sorted_files = sorted(inconsistencies.items(),
                              key=lambda x: sum(issue['total_count'] for issue in x[1]),
                              reverse=True)

        for file_path, file_inconsistencies in sorted_files[:5]:
            filename = Path(file_path).name
            total_issues = sum(issue['total_count'] for issue in file_inconsistencies)
            print(f"   - {filename}: {total_issues} inconsistency instances")

    else:
        print("✅ All files are internally consistent!")


if __name__ == "__main__":
    main()