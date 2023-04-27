@echo off
set HAM_VERSION=4.3.0
set JANUS_DRIVER_VERSION=1.1.12-SNAPSHOT
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%
cd ..
set CALENDAR_PATH=%cd%

cd %CALENDAR_PATH%\be
IF "%RUN_INLINE%"=="" (
    start java  -cp "be-%HAM_VERSION%.jar;janus-driver-%JANUS_DRIVER_VERSION%.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file:///%cd%\application.properties
) else (
    start /b java  -cp "be-%HAM_VERSION%.jar;janus-driver-%JANUS_DRIVER_VERSION%.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file:///%cd%\application.properties
)
cd %START_LOCATION%
