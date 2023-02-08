@echo off

set HAM_JAR=janus-driver-1.1.10-SNAPSHOT.jar
set CALENDAR_PATH=%~dp0
cd %CALENDAR_PATH%
REM Go to main path
cd ..
set ROOT_PATH=%cd%

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

echo Start it only when recording/replaying is started
pause

cd %CALENDAR_PATH%\calendar\scripts
benogen.bat
timeout /t 10 /nobreak
cd %ROOT_PATH%
