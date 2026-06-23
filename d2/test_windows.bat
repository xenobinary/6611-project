@echo off
setlocal
cd /d "%~dp0"

set "SRC=src"
set "TEST=test"
set "BIN=bin"
set "TESTBIN=testbin"
set "LIBS=lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-nop.jar"
set "TESTLIBS=lib\junit-4.13.2.jar;lib\hamcrest-2.2.jar"

echo === Compiling main sources ===
if not exist "%BIN%" mkdir "%BIN%"
if exist "%BIN%\resources" rmdir /s /q "%BIN%\resources"
xcopy "%SRC%\resources" "%BIN%\resources\" /E /I /Y >nul
if errorlevel 1 exit /b 1

javac -cp "%LIBS%" -d "%BIN%" -sourcepath "%SRC%" ^
  "%SRC%\models\*.java" ^
  "%SRC%\exceptions\*.java" ^
  "%SRC%\controllers\*.java" ^
  "%SRC%\views\*.java"
if errorlevel 1 exit /b 1

echo === Compiling tests ===
if not exist "%TESTBIN%" mkdir "%TESTBIN%"

javac -cp "%BIN%;%LIBS%;%TESTLIBS%" -d "%TESTBIN%" -sourcepath "%SRC%;%TEST%" ^
  "%TEST%\ModelTests.java" ^
  "%TEST%\IntegrationTests.java"
if errorlevel 1 exit /b 1

echo.
echo === Running Unit Tests (ModelTests) ===
if exist "%USERPROFILE%\.ibank.db" del /f /q "%USERPROFILE%\.ibank.db"
java -cp "%TESTBIN%;%BIN%;%LIBS%;%TESTLIBS%" org.junit.runner.JUnitCore ModelTests
if errorlevel 1 exit /b 1

echo.
echo === Running Integration Tests (IntegrationTests) ===
if exist "%USERPROFILE%\.ibank.db" del /f /q "%USERPROFILE%\.ibank.db"
java -cp "%TESTBIN%;%BIN%;%LIBS%;%TESTLIBS%" org.junit.runner.JUnitCore IntegrationTests
if errorlevel 1 exit /b 1

echo.
echo === All tests complete ===

endlocal
