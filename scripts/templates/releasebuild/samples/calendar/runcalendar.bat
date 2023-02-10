@echo off
set ROOT_PATH=%cd%
set CALENDAR_PATH=%~dp0
cd %CALENDAR_PATH%

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example


Rem start ham
cd %CALENDAR_PATH%\scripts
ham.bat
timeout /t 10 /nobreak
be.bat
timeout /t 10 /nobreak
gateway.bat
timeout /t 10 /nobreak
fe.bat
timeout /t 10 /nobreak
cd %ROOT_PATH%

