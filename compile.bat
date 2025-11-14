@echo off
set JAVAFX_PATH=lib\javafx-sdk-21.0.8\lib
set JAVAFX_MODULES=javafx.controls,javafx.graphics

if exist bin (
    rmdir /s /q bin
)
mkdir bin

echo [1/2] Compiling all classes...
javac -encoding UTF-8 -d bin --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml src\game\engine\*.java src\game\config\*.java src\game\map\*.java src\game\map\entities\*.java src\game\rules\*.java src\ui\gui\*.java src\bootstrap\*.java 2>&1
if errorlevel 1 (
    echo [ERROR] Compilation failed
    goto :error
)

echo ========================================
echo [OK] Compilation successful!
echo ========================================
goto :end

:error
echo ========================================
echo [ERROR] Compilation failed!
echo ========================================
exit /b 1

:end
