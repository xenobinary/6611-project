#!/usr/bin/env python3

"""
Pure-Python Java logical SLOC counter.

This script mirrors the UCC-J 2020.01 Java counting flow used in the project.
It does not invoke the UCC jar; instead it reproduces the relevant two-phase
algorithm directly:

1. Build a PSLOC-like stream from the source file.
2. Run the LSLOC counting rules over that stream.

For this codebase, the result matches UCC exactly.
"""

from __future__ import annotations

import os
import re
import sys


# From JavaLanguageProperties.java
LSLOC_KEYWORDS = ["if", "catch", "switch", "for", "while"]
EXCLUDE_KEYWORDS = ["else", "do", "try"]
EXCLUDE_CHARACTERS = ["{", "}", "(", ")"]
COMPILER_DIRECTIVES = ["package", "import"]
LINE_TERMINATOR = ";"
DATA_KEYWORDS = [
    "abstract", "ArrayList", "boolean", "byte", "char", "class", "const", "double", "enum",
    "extends", "final", "float", "HashMap", "HashSet", "implements", "int", "interface",
    "LinkedHashMap", "LinkedList", "long", "native", "operator", "private", "protected",
    "public", "short", "static", "String", "template", "TreeMap", "Vector", "void", "volatile",
]


def find_matching_paren(text: str, open_pos: int) -> int:
    """Return the index of the ')' matching the '(' at open_pos, or -1."""
    depth = 0
    for i in range(open_pos, len(text)):
        if text[i] == "(":
            depth += 1
        elif text[i] == ")":
            depth -= 1
            if depth == 0:
                return i
    return -1


def strip_comments_preserve_strings(text: str) -> str:
    """
    Remove // and /* */ comments while leaving string/char literals structurally intact.

    This matches the important CounterUtils behavior: comments disappear, but
    content inside string delimiters is not mistaken for comments.
    """
    out = []
    i = 0
    n = len(text)
    in_block_comment = False
    in_string = False
    string_delim = ""

    while i < n:
        char = text[i]
        nxt = text[i + 1] if i + 1 < n else ""

        if in_block_comment:
            if char == "*" and nxt == "/":
                in_block_comment = False
                i += 2
            else:
                out.append("\n" if char == "\n" else " ")
                i += 1
            continue

        if in_string:
            out.append(char)
            if char == "\\" and i + 1 < n:
                out.append(text[i + 1])
                i += 2
                continue
            if char == string_delim:
                in_string = False
                string_delim = ""
            i += 1
            continue

        if char in ('"', "'"):
            in_string = True
            string_delim = char
            out.append(char)
            i += 1
            continue

        if char == "/" and nxt == "/":
            while i < n and text[i] != "\n":
                out.append(" ")
                i += 1
            continue

        if char == "/" and nxt == "*":
            out.append(" ")
            out.append(" ")
            in_block_comment = True
            i += 2
            continue

        out.append(char)
        i += 1

    return "".join(out)


def delete_string_contents_keep_quotes(line: str) -> str:
    """
    Replace the inside of string/char literals with spaces but keep the quotes.

    Example:
        foo("bar", 'x') -> foo("   ", ' ')

    This matches the effective UCC input that comes out of CounterUtils before
    the PSLOC stream is post-processed.
    """
    chars = list(line)
    i = 0
    while i < len(chars):
        if chars[i] not in ('"', "'"):
            i += 1
            continue

        quote = chars[i]
        j = i + 1
        while j < len(chars):
            if chars[j] == "\\":
                j += 2
                continue
            if chars[j] == quote:
                for k in range(i + 1, j):
                    chars[k] = " "
                i = j + 1
                break
            j += 1
        else:
            break

    return "".join(chars)


def preprocess_physical_line(raw_line: str) -> str:
    """Apply the effective per-line preprocessing used before PSLOC splitting."""
    line = raw_line.strip()
    if not line:
        return ""
    return delete_string_contents_keep_quotes(line)


def split_into_psloc_lines(text: str) -> list[str]:
    """
    Reproduce JavaCounter.CountFilePSLOC post-processing.

    Important detail: UCC writes processed fragments directly to a PSLOC file
    without inserting an implicit newline between physical lines. Only explicit
    newlines created by its splitting logic separate PSLOC records. That is why
    this function builds a single stream first, then splits it at the end.
    """
    output_fragments: list[str] = []
    multi_line_flag = False
    multi_line_prefix = ""

    for raw_line in text.splitlines():
        line = preprocess_physical_line(raw_line)
        if not line:
            continue

        if multi_line_flag:
            line = multi_line_prefix + " " + line

        if multi_line_flag:
            multi_line_flag = False
            multi_line_prefix = ""

        undo_newlines = line.endswith(LINE_TERMINATOR)

        # Join multi-line LSLOC-keyword constructs exactly like UCC.
        for keyword in LSLOC_KEYWORDS:
            if not line:
                break
            close_pos = 0
            keyword_pattern = re.compile(r"\b" + re.escape(keyword) + r"\b")
            matcher = keyword_pattern.search(line)

            while close_pos >= 0 and matcher:
                open_pos = line.find("(", matcher.end())
                close_pos = find_matching_paren(line, open_pos)

                if close_pos != -1:
                    multi_line_flag = False
                    if keyword == "for":
                        middle = line[open_pos + 1:close_pos].strip().replace(LINE_TERMINATOR, "@")
                        line = line[:open_pos + 1] + middle + line[close_pos:].strip()
                    multi_line_prefix = ""
                else:
                    multi_line_flag = True
                    multi_line_prefix = line
                    line = ""
                    break

                matcher = keyword_pattern.search(line, max(close_pos, matcher.end())) if line else None

        if multi_line_flag or not line:
            continue

        # Core PSLOC splitting.
        line = line.replace(LINE_TERMINATOR, LINE_TERMINATOR + "\n")
        line = line.replace("{", "{\n")
        line = line.replace("}", "\n}\n")

        if line.endswith(":"):
            line += "\n"
        if line == "EXPORT":
            line += "\n"

        while "\n\n" in line:
            line = line.replace("\n\n", "\n")
        if line.startswith("\n"):
            line = line[1:]

        # Start LSLOC keywords on their own line.
        for keyword in LSLOC_KEYWORDS:
            if not line:
                break
            keyword_pattern = re.compile(r"\b" + re.escape(keyword) + r"\b")
            if keyword_pattern.search(line):
                line = keyword_pattern.sub("\n" + keyword, line)
                break

        line = re.sub(r"\bdo\b", "\ndo ", line)

        if line.strip() == "":
            line = ""

        if undo_newlines and line:
            line = line.replace("\n", "")
            line = line.replace(LINE_TERMINATOR, LINE_TERMINATOR + "\n")

        for compiler_directive in COMPILER_DIRECTIVES:
            if compiler_directive in line:
                line += "\n"
                break

        while "\n " in line:
            line = line.replace("\n ", "\n")

        if line.strip():
            output_fragments.append(line)

    psloc_stream = "".join(output_fragments)
    return [piece.strip() for piece in psloc_stream.split("\n") if piece.strip()]


def delete_compiler_directive(line: str) -> tuple[str, int]:
    trimmed = line.strip()
    for compiler_directive in COMPILER_DIRECTIVES:
        matcher = re.compile(re.escape(compiler_directive)).search(trimmed)
        if matcher and matcher.start() == 0:
            return "", 1
    return line, 0


def delete_loop_contents(line: str, keyword: str) -> str:
    pattern = re.compile(r"\b" + re.escape(keyword) + r"\b")
    matcher = pattern.search(line)
    if not matcher:
        return line

    open_pos = line.find("(", matcher.start())
    if open_pos == -1:
        return line

    close_pos = find_matching_paren(line, open_pos)
    if close_pos == -1:
        return line

    return line[:open_pos + 1] + line[close_pos:].strip()


def is_data_declaration(line: str) -> bool:
    trimmed = line.strip()
    if not trimmed.endswith(LINE_TERMINATOR):
        return False

    for keyword in DATA_KEYWORDS:
        matcher = re.compile(r"\b" + re.escape(keyword) + r"\b").search(trimmed)
        if matcher and matcher.start() == 0:
            return True
    return False


def count_and_remove_lsloc_keywords(line: str) -> tuple[str, int]:
    count = 0
    for keyword in LSLOC_KEYWORDS:
        pattern = re.compile(r"\b" + re.escape(keyword) + r"\b")
        while True:
            matcher = pattern.search(line)
            if not matcher:
                break
            line = line[:matcher.start()] + " " + line[matcher.end():]
            count += 1
    return line, count


def delete_exclude_keywords(line: str) -> str:
    for keyword in EXCLUDE_KEYWORDS:
        line = re.sub(r"\b" + re.escape(keyword) + r"\b", " ", line)
    return line


def delete_exclude_characters(line: str) -> str:
    for character in EXCLUDE_CHARACTERS:
        line = line.replace(character, " ")
    return line


def delete_line_if_comma_terminated(line: str) -> str:
    return "" if line.strip().endswith(",") else line


def delete_case_statement(line: str) -> str:
    if line == "case:":
        return ""

    matcher = re.search(r"\bcase\b", line)
    if not matcher:
        return line

    case_index = matcher.start()
    colon_index = line.find(":", case_index)
    if case_index != -1 and colon_index != -1 and case_index < colon_index:
        return (line[:case_index] + line[colon_index + 1:]).strip()

    return line


def delete_line_if_colon_terminated(line: str) -> str:
    return "" if line.strip().endswith(":") else line


def count_logical_fallback(source_text: str) -> int:
    """Count Java logical SLOC using the UCC-compatible pure Python path."""
    logical_lines = split_into_psloc_lines(strip_comments_preserve_strings(source_text))

    compiler_directive_count = 0
    data_declaration_count = 0
    lsloc_keyword_count = 0
    leftover_lsloc_count = 0

    for line in logical_lines:
        line = line.strip()
        if not line:
            continue

        line, compiler_hits = delete_compiler_directive(line)
        compiler_directive_count += compiler_hits
        if not line.strip():
            continue

        for keyword in LSLOC_KEYWORDS:
            line = delete_loop_contents(line, keyword)

        if is_data_declaration(line):
            data_declaration_count += 1
            continue

        line, keyword_hits = count_and_remove_lsloc_keywords(line)
        lsloc_keyword_count += keyword_hits
        line = delete_exclude_keywords(line)
        line = delete_exclude_characters(line)
        line = delete_line_if_comma_terminated(line)
        line = delete_case_statement(line)
        line = delete_line_if_colon_terminated(line)

        if line.strip():
            leftover_lsloc_count += 1

    return (
        compiler_directive_count
        + data_declaration_count
        + lsloc_keyword_count
        + leftover_lsloc_count
    )


def main() -> None:
    base_dir = sys.argv[1] if len(sys.argv) > 1 else "."

    java_files: list[str] = []
    for root, dirs, files in os.walk(base_dir):
        dirs[:] = [d for d in dirs if d not in ("test", "tests")]
        for name in files:
            if name.endswith(".java"):
                java_files.append(os.path.join(root, name))

    java_files.sort()


    total = 0
    pkg_totals: dict[str, int] = {}
    pkg_counts: dict[str, int] = {}
    rows: list[tuple[str, int]] = []
    for path in java_files:
        with open(path, encoding="utf-8", errors="replace") as fh:
            count = count_logical_fallback(fh.read())
        total += count
        rel = os.path.relpath(path, base_dir)
        pkg = rel.split(os.sep)[0] if os.sep in rel else ""
        pkg_totals[pkg] = pkg_totals.get(pkg, 0) + count
        pkg_counts[pkg] = pkg_counts.get(pkg, 0) + 1
        rows.append((rel, count))

    # Summary table (first)
    print("=== Summary ===")
    print(f"{'Package':<20} {'Files':>6} {'Logical SLOC':>12}")
    print("-" * 41)
    for pkg in sorted(pkg_totals, key=lambda p: pkg_totals[p], reverse=True):
        print(f"{pkg:<20} {pkg_counts[pkg]:>6} {pkg_totals[pkg]:>12}")
    print()
    print(f"{'TOTAL':<20} {sum(pkg_counts.values()):>6} {total:>12}")
    print()

    # Detail table (second)
    print(f"{'File Path':<60} | {'Logical SLOC':>12}")
    print("-" * 61 + "+" + "-" * 14)
    for rel, count in rows:
        print(f"{rel:<60.60} | {count:>12}")

    print("-" * 61 + "+" + "-" * 14)
    print(f"{'TOTAL':<60} | {total:>12}")
    print()


if __name__ == "__main__":
    main()
