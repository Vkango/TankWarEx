@echo off
setlocal EnableDelayedExpansion

set JAVAFX_PATH=lib\javafx-sdk-21.0.8\lib
set JAVAFX_MODULES=javafx.controls,javafx.fxml,javafx.graphics
set GSON_PATH=lib\gson-2.10.1.jar

if exist bin (
    rmdir /s /q bin
)
mkdir bin

set SOURCES=
for /r "src" %%f in (*.java) do set SOURCES=!SOURCES! "%%f"

javac -encoding UTF-8 -d bin --module-path "%JAVAFX_PATH%" --add-modules %JAVAFX_MODULES% -cp "%GSON_PATH%" !SOURCES! 2>&1

if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!
pause