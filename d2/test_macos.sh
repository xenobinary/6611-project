#!/bin/bash
set -e
cd "$(dirname "$0")"

SRC="src"
TEST="test"
BIN="bin"
TESTBIN="testbin"
LIBS="lib/sqlite-jdbc.jar:lib/slf4j-api.jar:lib/slf4j-nop.jar"
TESTLIBS="lib/junit-4.13.2.jar:lib/hamcrest-2.2.jar"

echo "=== Compiling main sources ==="
mkdir -p "$BIN"
cp -r "$SRC"/resources "$BIN"/
javac -cp "$LIBS" -d "$BIN" -sourcepath "$SRC" \
  "$SRC"/models/*.java \
  "$SRC"/exceptions/*.java \
  "$SRC"/controllers/*.java \
  "$SRC"/views/*.java

echo "=== Compiling tests ==="
mkdir -p "$TESTBIN"
javac -cp "$BIN:$LIBS:$TESTLIBS" -d "$TESTBIN" \
  -sourcepath "$SRC:$TEST" \
  "$TEST"/ModelTests.java \
  "$TEST"/IntegrationTests.java

echo ""
echo "=== Running Unit Tests (ModelTests) ==="
rm -f ~/.ibank.db
java -cp "$TESTBIN:$BIN:$LIBS:$TESTLIBS" \
  org.junit.runner.JUnitCore ModelTests

echo ""
echo "=== Running Integration Tests (IntegrationTests) ==="
rm -f ~/.ibank.db
java -cp "$TESTBIN:$BIN:$LIBS:$TESTLIBS" \
  org.junit.runner.JUnitCore IntegrationTests

echo ""
echo "=== All tests complete ==="
