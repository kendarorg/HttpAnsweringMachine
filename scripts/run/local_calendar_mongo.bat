@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

echo [INFO] You should configure the http and https proxy to
echo [INFO] localhost:1081 to appreciate the example

pause

REM Set the ham config
set TEMPLATES_LOCATION=%SCRIPT_DIR%\templates\standalone
set JSON_CONFIG=%TEMPLATES_LOCATION%\calendar.external.json
REM Start ham
start %SCRIPT_DIR%\run\local.bat
REM Wait for startup
ping 127.0.0.1 -n 10 > nul

set CALENDAR_PATH=%ROOT_DIR%\samples\calendar


Rem start fe
cd %CALENDAR_PATH%\fe\target
dir /b fe*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -jar %JAR_NAME% --spring.config.location=file:///%TEMPLATES_LOCATION%\fe.application.properties
REM Wait for startup
ping 127.0.0.1 -n 10 > nul
cd %START_LOCATION%

Rem start be
cd %CALENDAR_PATH%\bemongo\target
dir /b bemongo*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -jar %JAR_NAME% --spring.config.location=file:///%TEMPLATES_LOCATION%\bemongo.application.properties
REM Wait for startup
ping 127.0.0.1 -n 10 > nul
cd %START_LOCATION%

Rem start gateway
cd %CALENDAR_PATH%\gateway\target
dir /b gateway*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -jar %JAR_NAME% --spring.config.location=file:///%TEMPLATES_LOCATION%\gateway.application.properties
REM Wait for startup
ping 127.0.0.1 -n 10 > nul
cd %START_LOCATION%


