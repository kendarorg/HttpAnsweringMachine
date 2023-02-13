@echo off
set ROOT_PATH=%cd%
set CALENDAR_PATH=%~dp0
cd %CALENDAR_PATH%

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example


Rem start ham
cd %CALENDAR_PATH%\scripts
call ham.bat
ping 127.0.0.1 -n 10 > nul
call be.bat
ping 127.0.0.1 -n 10 > nul
call gateway.bat
ping 127.0.0.1 -n 10 > nul
call fe.bat
ping 127.0.0.1 -n 10 > nul
cd %ROOT_PATH%

