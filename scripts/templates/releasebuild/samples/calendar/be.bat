@echo off
set ROOT_PATH=%cd%
set CALENDAR_PATH=%~dp0
cd %CALENDAR_PATH%

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

echo Start it only when recording/replaying is started
pause

cd %CALENDAR_PATH%\scripts
be.bat
timeout /t 10 /nobreak
cd %ROOT_PATH%
