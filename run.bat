@echo off
set JAVAFX_PATH=lib\javafx-sdk-21.0.8\lib
set JAVAFX_MODULES=javafx.controls,javafx.graphics

echo ========================================
echo      TankBattle v2.0.0
echo ========================================

java --module-path %JAVAFX_PATH% --add-modules %JAVAFX_MODULES% -cp bin bootstrap.Main

if errorlevel 1 (
    echo [ERROR] Failed to start game
    pause
    exit /b 1
)
