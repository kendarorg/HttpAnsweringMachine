@echo off
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%
cd ..
set CALENDAR_PATH=%cd%

cd %CALENDAR_PATH%\be
start java  -cp "be-4.1.5.jar;janus-driver-1.1.10-SNAPSHOT.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file:///%cd%\bedb.application.properties
timeout /t 10 /nobreak
cd %START_LOCATION%
