# Logical SLOC Counting Scheme

This document explains how `eval/lsloc.py` calculates Java **logical SLOC** and why the implementation is structured the way it is.

The goal is **behavioral compatibility with UCC-J 2020.01**, not a generic regex estimate.

## Scope

The script counts logical SLOC for Java source files under a directory tree, excluding `test/` and `tests/` directories.

The final metric is the same one UCC reports in `JAVA_outfile.csv`:

```text
NumLSLOC = NumCompilerDirectives + NumDataDeclLog + lslocKeywordsCount + leftover_lsloc
```

Where:

- `NumCompilerDirectives`: lines starting with `package` or `import`
- `NumDataDeclLog`: `;`-terminated lines that start with a Java data keyword
- `lslocKeywordsCount`: occurrences of `if`, `catch`, `switch`, `for`, `while`
- `leftover_lsloc`: remaining non-empty logical lines after all deletions

## Why a simple regex is not enough

The first attempt used a regex like:

```bash
grep -E -o ";|\bif\s*\(|\bfor\s*\(|\bwhile\s*\("
```

That undercounted badly because UCC does much more than count semicolons and a few keywords.

Important UCC behaviors that a simple regex misses:

1. It builds a **PSLOC stream** first, then counts LSLOC from that transformed stream.
2. It **concatenates physical lines** unless its post-processing inserts explicit newlines.
3. It treats `if`, `for`, `while`, `catch`, `switch` specially by deleting their parenthesized contents before counting.
4. It distinguishes **data declarations** from general executable statements.
5. It deletes specific constructs such as:
   - `else`, `do`, `try`
   - `{`, `}`, `(`, `)`
   - `case ... :`
   - lines ending in `,`
   - lines ending in `:`

## High-level algorithm

The script follows two phases.

### Phase 1: Build PSLOC-like lines

This phase mirrors `JavaCounter.CountFilePSLOC()`.

Input: original Java source text

Output: a list of **PSLOC lines** that behave like the `_PSLOC` temp file UCC would create internally.

The steps are:

1. Strip comments while preserving string/char delimiters.
2. For each physical line:
   - trim whitespace
   - blank out the **contents** of string and char literals, but keep the quote delimiters
   - join continued `if/catch/switch/for/while` lines when `)` is missing
   - replace `;` inside `for (...)` with `@` so loop headers are not split incorrectly
   - insert explicit newlines after:
     - `;`
     - `{`
     - before and after `}`
   - force LSLOC keywords to start on their own line
   - treat `do` as a special keyword boundary
   - if the original physical line ended with `;`, undo brace-based newlines and keep only `;`-based newlines
   - add a newline after compiler directives
3. Append each processed physical line into one **continuous PSLOC stream**.
4. Split the final stream on `\n` to get PSLOC records.

### Why the PSLOC stream matters

This was the main source of mismatch during development.

UCC does **not** automatically insert a newline between processed physical lines.
It writes each transformed line directly into the PSLOC file and only the newlines created by its own splitting logic separate records.

That means code like this:

```java
s.execute("CREATE TABLE users ("
        + "card_number TEXT PRIMARY KEY,"
        + "pin_hash TEXT NOT NULL,");
```

does **not** become three PSLOC lines. It becomes one PSLOC line, because only the final physical line inserts the terminating newline.

This detail is essential for matching UCC exactly.

## Phase 2: Count LSLOC from PSLOC lines

This phase mirrors `JavaCounter.CountFileLSLOC()`.

For each PSLOC line:

1. **Compiler directives**
   - If the line starts with `package` or `import`, count one compiler directive and discard the line.

2. **Delete loop contents**
   - For each LSLOC keyword (`if`, `catch`, `switch`, `for`, `while`), delete the parenthesized contents associated with the keyword.

3. **Count data declarations**
   - If the line ends with `;` **and** starts with a Java data keyword, count one logical data declaration and discard the line.

4. **Count LSLOC keywords**
   - Count occurrences of `if`, `catch`, `switch`, `for`, `while`.
   - Remove those keywords from the line after counting.

5. **Delete exclude keywords**
   - Remove `else`, `do`, `try`.

6. **Delete exclude characters**
   - Remove `{`, `}`, `(`, `)`.

7. **Delete comma-terminated lines**
   - If the remaining line ends with `,`, discard it.

8. **Delete case statements**
   - Remove `case ... :` payloads.

9. **Delete colon-terminated lines**
   - If the remaining line ends with `:`, discard it.

10. **Count leftover logical lines**
    - If anything still remains on the line, count one leftover logical SLOC.

## Important keyword sets

These were copied from `JavaLanguageProperties.java`.

### LSLOC keywords

Used both for PSLOC joining and LSLOC keyword counting:

```text
if, catch, switch, for, while
```

### Exclude keywords

Deleted after LSLOC keyword counting:

```text
else, do, try
```

### Exclude characters

Deleted late in LSLOC processing:

```text
{ } ( )
```

### Compiler directives

```text
package, import
```

### Data keywords

These are used to decide whether a `;`-terminated line is a logical data declaration:

```text
abstract, ArrayList, boolean, byte, char, class, const, double, enum,
extends, final, float, HashMap, HashSet, implements, int, interface,
LinkedHashMap, LinkedList, long, native, operator, private, protected,
public, short, static, String, template, TreeMap, Vector, void, volatile
```

## Key debugging discoveries

Several non-obvious UCC behaviors were required to close the final gap.

### 1. The quote-cleanup block in `JavaCounter` is effectively overwritten

Inside `CountFilePSLOC()`, `JavaCounter` performs extra string cleanup on a local `line`, but later resets:

```java
line = ro.line;
```

So the PSLOC phase actually uses the `CounterUtils` result, not the later local quote-cleanup mutations.

That means the Python port must mirror the **effective** behavior, not every line of dead Java code.

### 2. PSLOC line joining is stream-based

This was the biggest cause of mismatch.

If processed physical lines are naively split and stored independently, multi-line expressions (especially SQL string concatenations and array initializers) are overcounted.

The correct behavior is to build one continuous PSLOC stream and split only on explicit inserted newlines.

### 3. DatabaseManager was the canary file

`DatabaseManager.java` had the largest mismatch during development because it contains:

- multi-line SQL string concatenations
- nested arrays
- many `;`-terminated builder lines
- many short helper methods

Once `DatabaseManager.java` matched, the full project matched.

## Validation

This implementation was validated against the current project UCC output:

```text
UCC logical SLOC:   1926
Python logical SLOC: 1926
Gap: 0
```

## Files

- `eval/lsloc.py` — pure-Python implementation
- `eval/lsloc.sh` — wrapper script (currently still supports UCC-jar usage / fallback flow as originally created)

## Recommended usage

Run from the project root:

```bash
python3 d2/eval/lsloc.py d2/src
```

or:

```bash
bash d2/eval/lsloc.sh d2/src
```

Both should report the same total for this project.
