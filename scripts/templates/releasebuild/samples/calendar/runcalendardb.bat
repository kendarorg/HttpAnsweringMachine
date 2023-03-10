@echo off
set ROOT_PATH=%cd%
set CALENDAR_PATH=%~dp0
cd %CALENDAR_PATH%


choice /M "Do you want to cleanup the database?"

if %errorlevel% EQU 1 (
    md %CALENDAR_PATH%\data
    del /S /Q %CALENDAR_PATH%\data\*.db 2>&1 1>NUL
)

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example


pause

Rem start db
cd %CALENDAR_PATH%
start rundb.bat
REM Wait for startup
ping 127.0.0.1 -n 10 > nul

Rem start ham
cd %CALENDAR_PATH%\scripts
call ham.bat
ping 127.0.0.1 -n 10 > nul
call bedb.bat
ping 127.0.0.1 -n 10 > nul
call gateway.bat
ping 127.0.0.1 -n 10 > nul
call fe.bat
ping 127.0.0.1 -n 10 > nul
cd %ROOT_PATH%

