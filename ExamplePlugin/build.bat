@echo off
setlocal enabledelayedexpansion

echo [INFO] Starting build process...

REM Clean previous build
if exist bin rmdir /s /q bin
if exist plugin.jar del plugin.jar

REM Create output directory
mkdir bin

REM Collect source files into a variable using for /f to handle spaces correctly
echo [INFO] Collecting source files...
set "source_files="
for /f "usebackq delims=" %%i in (`dir /s /b game\*.java 2^>nul`) do (
    REM Enclose each file path in double quotes to handle spaces and special characters
    set "source_files=!source_files! "%%i""
)

REM Check if any source files were found
if not defined source_files (
    echo [WARN] No .java files found in the 'game' directory.
    REM Cleanup temporary files if they exist
    if exist sources.txt del sources.txt
    REM You might want to exit or handle the no-files case differently
    REM exit /b 0 
) else (
    echo [INFO] Compiling sources...
    REM Compile using the collected file list, including current directory (.) in classpath for inter-dependent sources
    REM The source_files variable now contains paths correctly quoted
    javac -encoding UTF-8 -d bin -cp ".;lib/*" !source_files!

    if !errorlevel! neq 0 (
        echo [ERROR] Compilation failed!
        pause
        exit /b 1
    )
)

REM Copy META-INF resources (SPI configurations)
echo [INFO] Copying META-INF...
if exist META-INF (
    xcopy /s /e /y /i META-INF bin\META-INF > nul 2>&1
)

REM Create JAR
echo [INFO] Packaging plugin.jar...
jar cvf plugin.jar -C bin .

REM Cleanup temporary files
rmdir /s /q bin
REM del sources.txt REM sources.txt was not created in this version

echo [INFO] Build success! plugin.jar has been generated.
pause
endlocal