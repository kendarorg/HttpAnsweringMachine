@echo off

set CALENDAR_PATH=%~dp0
cd %CALENDAR_PATH%
REM Go to main path
cd ..
set ROOT_PATH=%cd%

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

pause

Rem start ham
cd %ROOT_PATH%\calenda\scripts
ham.bat
timeout /t 10 /nobreak
be.bat
timeout /t 10 /nobreak
gateway.bat
timeout /t 10 /nobreak
fe.bat
timeout /t 10 /nobreak
cd %ROOT_PATH%

