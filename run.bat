@echo off
set JAVAFX_PATH=lib\javafx-sdk-21.0.8\lib
set JAVAFX_MODULES=javafx.controls,javafx.graphics
set GSON_PATH=lib\gson-2.10.1.jar

java --module-path "%JAVAFX_PATH%" --add-modules %JAVAFX_MODULES% -cp "%GSON_PATH%;bin" bootstrap.Main

if errorlevel 1 (
    echo [ERROR] Failed to start game
    pause
    exit /b 1
)
