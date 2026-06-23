@echo off
setlocal
cd /d "%~dp0"

set "LIBS=lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-nop.jar"
set "SRC=src"
set "BIN=bin"

if not exist "%BIN%" mkdir "%BIN%"

javac -cp "%LIBS%" -d "%BIN%" -sourcepath "%SRC%" ^
  "%SRC%\models\*.java" ^
  "%SRC%\exceptions\*.java" ^
  "%SRC%\controllers\*.java" ^
  "%SRC%\views\*.java"
if errorlevel 1 exit /b 1

if exist "%BIN%\resources" rmdir /s /q "%BIN%\resources"
xcopy "%SRC%\resources" "%BIN%\resources\" /E /I /Y >nul
if errorlevel 1 exit /b 1

java -cp "%BIN%;%LIBS%" MainFrame
if errorlevel 1 exit /b 1

endlocal
