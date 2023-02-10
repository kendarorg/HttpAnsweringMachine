@echo off
set HAM_VERSION=4.1.5
set JANUS_DRIVER_VERSION=1.1.10-SNAPSHOT
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%
cd ..
set CALENDAR_PATH=%cd%

cd %CALENDAR_PATH%\be
start java  -cp "be-%HAM_VERSION%.jar;janus-driver-%JANUS_DRIVER_VERSION%.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file:///%cd%\bedb.application.properties
cd %START_LOCATION%
