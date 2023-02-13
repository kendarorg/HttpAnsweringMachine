@echo off
set HAM_VERSION=4.1.5
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%
cd ..
set CALENDAR_PATH=%cd%

cd %CALENDAR_PATH%\fe
start java -jar fe-%HAM_VERSION%.jar --spring.config.location=file:///%cd%\application.properties
cd %START_LOCATION%
