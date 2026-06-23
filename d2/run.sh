#!/bin/bash
cd "$(dirname "$0")"

LIBS="lib/sqlite-jdbc.jar:lib/slf4j-api.jar:lib/slf4j-nop.jar"
SRC="src"
BIN="bin"

mkdir -p "$BIN"
javac -cp "$LIBS" -d "$BIN" -sourcepath "$SRC" \
  "$SRC"/models/*.java \
  "$SRC"/exceptions/*.java \
  "$SRC"/controllers/*.java \
  "$SRC"/views/*.java

cp -r "$SRC"/resources "$BIN"/

java -cp "$BIN:$LIBS" MainFrame
