@ECHO OFF

REM Initialize
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%

REM Retrieve the jar name
dir /b %SCRIPT_DIR%\*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
del /s /f /q .temp.txt 2>&1 1>NUL




REM Start the application
java   -jar %JAR_NAME%
