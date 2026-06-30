#!/bin/bash
# Logical SLOC counter for Java source files.
# Uses USC CSSE Unified Code Counter - Java (UCC-J) for authoritative counts.
# Falls back to lsloc.py if the UCC jar is not found.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
UCC_JAR="$SCRIPT_DIR/ucc-j-2020.01.jar"
SRC_DIR="${1:-.}"
SRC_DIR="$(cd "$SRC_DIR" 2>/dev/null && pwd || echo "$SRC_DIR")"

if [ -f "$UCC_JAR" ]; then
    TMPDIR=$(mktemp -d /tmp/lsloc_XXXXXX)
    trap "rm -rf $TMPDIR" EXIT

    java -jar "$UCC_JAR" -dir "$SRC_DIR" "*.java" -outdir "$TMPDIR" > /dev/null 2>&1

    CSVFILE="$TMPDIR/JAVA_outfile.csv"
    if [ ! -f "$CSVFILE" ]; then
        echo "ERROR: UCC output not found" >&2
        exit 1
    fi

    # First pass: collect data
    TOTAL=0
    declare -A pkg_totals pkg_counts
    ROWS=""
    in_section=0

    while IFS= read -r line; do
        case "$line" in
            "RESULTS FOR JAVA FILES")
                in_section=1
                continue
                ;;
        esac
        [ "$in_section" = "0" ] && continue
        [ -z "$line" ] && continue
        case "$line" in
            Total,*)  continue ;;
            Lines,*)  continue ;;
        esac
        case "$line" in
            "RESULTS SUMMARY"|"TOTAL OCCURRENCES"*)
                break
                ;;
        esac

        LOGICAL=$(echo "$line" | awk -F, '{print $8}')
        FULLPATH=$(echo "$line" | awk -F, '{print $11}')
        DISPLAY=$(echo "$FULLPATH" | sed 's|.*/src/||')

        TOTAL=$((TOTAL + LOGICAL))
        ROWS="$ROWS$(printf "%-60.60s | %12s\n" "$DISPLAY" "$LOGICAL")"$'\n'

        PKG=$(echo "$DISPLAY" | cut -d'/' -f1)
        pkg_totals["$PKG"]=$((${pkg_totals["$PKG"]:-0} + LOGICAL))
        pkg_counts["$PKG"]=$((${pkg_counts["$PKG"]:-0} + 1))
    done < "$CSVFILE"

    # ---------------------------------------------------
    # Summary table (first)
    # ---------------------------------------------------
    echo ""
    echo "=== Summary ==="
    printf "%-20s %6s %12s\n" "Package" "Files" "Logical SLOC"
    echo "-----------------------------------------"

    SORTED_PKGS=$(for p in "${!pkg_totals[@]}"; do echo "$p ${pkg_totals[$p]}"; done | sort -k2 -rn | cut -d' ' -f1)
    for pkg in $SORTED_PKGS; do
        printf "%-20s %6d %12d\n" "$pkg" "${pkg_counts[$pkg]}" "${pkg_totals[$pkg]}"
    done

    echo ""
    printf "%-20s %6s %12d\n" "TOTAL" "25" "$TOTAL"
    echo ""

    # ---------------------------------------------------
    # Detail table (second)
    # ---------------------------------------------------
    printf "%-60s | %-12s\n" "File Path" "Logical SLOC"
    echo "-------------------------------------------------------------+--------------"
    printf "%s" "$ROWS"
    echo "-------------------------------------------------------------+--------------"
    printf "%-60s | %12d\n\n" "TOTAL" "$TOTAL"
else
    echo "UCC jar not found, falling back to lsloc.py (approximate)" >&2
    exec python3 "$SCRIPT_DIR/lsloc.py" "$SRC_DIR"
fi
