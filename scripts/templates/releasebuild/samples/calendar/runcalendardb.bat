@echo off
set ROOT_PATH=%cd%
set CALENDAR_PATH=%~dp0
cd %CALENDAR_PATH%

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

choice /M "Do you want to cleanup the database?"



pause

if %errorlevel% EQU 1 (
    md %CALENDAR_PATH%\data
    del /S /Q %CALENDAR_PATH%\data\*.db 2>&1 1>NUL
)

Rem start db
cd %CALENDAR_PATH%
start rundb.bat
REM Wait for startup
timeout /t 10 /nobreak

Rem start ham
cd %CALENDAR_PATH%\scripts
ham.bat
timeout /t 10 /nobreak
bedb.bat
timeout /t 10 /nobreak
gateway.bat
timeout /t 10 /nobreak
fe.bat
timeout /t 10 /nobreak
cd %ROOT_PATH%

