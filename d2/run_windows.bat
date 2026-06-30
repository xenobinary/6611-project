@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "LIBS=lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-nop.jar"
set "SRC=src"
set "BIN=bin"
set "SOURCES=sources.txt"

if not exist "%BIN%" mkdir "%BIN%"

(
  for /r "%SRC%\models" %%F in (*.java) do (
    set "SOURCE_FILE=%%F"
    echo "!SOURCE_FILE:\=/!"
  )
  for /r "%SRC%\controllers" %%F in (*.java) do (
    set "SOURCE_FILE=%%F"
    echo "!SOURCE_FILE:\=/!"
  )
  for /r "%SRC%\exceptions" %%F in (*.java) do (
    set "SOURCE_FILE=%%F"
    echo "!SOURCE_FILE:\=/!"
  )
  for /r "%SRC%\views" %%F in (*.java) do (
    set "SOURCE_FILE=%%F"
    echo "!SOURCE_FILE:\=/!"
  )
) > "%SOURCES%"

javac -cp "%LIBS%" -d "%BIN%" -sourcepath "%SRC%" @"%SOURCES%"
set "COMPILE_RESULT=%ERRORLEVEL%"
del "%SOURCES%" >nul 2>&1
if not "%COMPILE_RESULT%"=="0" exit /b %COMPILE_RESULT%

if exist "%BIN%\resources" rmdir /s /q "%BIN%\resources"
xcopy "%SRC%\resources" "%BIN%\resources\" /E /I /Y >nul
if errorlevel 1 exit /b 1

java -cp "%BIN%;%LIBS%" MainFrame
if errorlevel 1 exit /b 1

endlocal
