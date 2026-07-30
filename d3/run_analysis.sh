#!/usr/bin/env bash
set -euo pipefail

D3_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$D3_DIR/.." && pwd)"
ANALYSIS_TMP="${TMPDIR:-/tmp}/ibank-d3-analysis"
UCC_DIR="$ANALYSIS_TMP/ucc"
CLASS_DIR="$ANALYSIS_TMP/classes"

mkdir -p "$UCC_DIR" "$CLASS_DIR" "$D3_DIR/eval"

(
  cd "$UCC_DIR"
  java -jar "$PROJECT_DIR/d2/eval/ucc-j-2020.01.jar" \
    -dir "$PROJECT_DIR/d2/src" '*.java'
)

cp "$UCC_DIR/JAVA_outfile.csv" "$D3_DIR/eval/ucc_sloc.csv"
cp "$UCC_DIR/outfile_cyclomatic_cplx.csv" \
  "$D3_DIR/eval/ucc_cyclomatic_complexity.csv"

javac -d "$CLASS_DIR" "$D3_DIR/tools/D3MetricsAnalyzer.java"
java -cp "$CLASS_DIR" D3MetricsAnalyzer \
  "$PROJECT_DIR/d2/src" "$D3_DIR/eval/oo_raw.csv"

python3 "$D3_DIR/tools/assemble_metrics.py" \
  --sloc "$D3_DIR/eval/ucc_sloc.csv" \
  --complexity "$D3_DIR/eval/ucc_cyclomatic_complexity.csv" \
  --oo "$D3_DIR/eval/oo_raw.csv" \
  --output-dir "$D3_DIR/eval"

python3 "$D3_DIR/tools/calculate_ucp.py"

(
  cd "$PROJECT_DIR/d2"
  ./test_macos.sh
)

echo "D3 analysis complete: $D3_DIR/eval"
