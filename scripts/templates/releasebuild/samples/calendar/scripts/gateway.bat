@echo off
set HAM_VERSION=4.2.2
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%
cd ..
set CALENDAR_PATH=%cd%

cd %CALENDAR_PATH%\gateway
IF "%RUN_INLINE%"=="" (
    start java -jar gateway-%HAM_VERSION%.jar --spring.config.location=file:///%cd%\application.properties
) else (
    start /b java -jar gateway-%HAM_VERSION%.jar --spring.config.location=file:///%cd%\application.properties
)
cd %START_LOCATION%
