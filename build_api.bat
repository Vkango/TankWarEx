@echo off
set JAVAFX_PATH=lib\javafx-sdk-21.0.8\lib
set JAVAFX_MODULES=javafx.controls,javafx.graphics

call compile.bat
if errorlevel 1 goto :error

if exist dist (
    rmdir /s /q dist
)
mkdir dist
mkdir dist\lib
mkdir dist\plugins

rem API includes game model, engine, map interfaces, rules interfaces.
rem Excludes UI and Bootstrap.
jar cvf dist\tankwar-api.jar -C bin game -C bin plugin

xcopy /s /y /q lib\javafx-sdk-21.0.8\lib\*.jar dist\lib\

echo @echo off > dist\run.bat
echo set JAVAFX_PATH=lib >> dist\run.bat
echo java --module-path %%JAVAFX_PATH%% --add-modules javafx.controls,javafx.fxml -jar tankwar.jar >> dist\run.bat

echo Build Complete!
goto :end

:error
echo [ERROR] Build failed.
pause

:end
