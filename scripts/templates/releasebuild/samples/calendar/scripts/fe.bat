@echo off
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%
cd ..
set CALENDAR_PATH=%cd%

cd %CALENDAR_PATH%\fe
start java -jar fe-4.1.5.jar --spring.config.location=file:///%cd%\application.properties
timeout /t 10 /nobreak
cd %START_LOCATION%
