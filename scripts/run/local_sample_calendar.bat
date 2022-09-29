@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

REM Set the ham config
set JSON_CONFIG=%SCRIPT_DIR%\templates\standalone\calendar.external.json
REM Start ham
start %SCRIPT_DIR%\local.bat
REM Wait for startup
timeout /t 10 /nobreak

set CALENDAR_PATH=%ROOT_DIR%\samples\calendar

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

pause

Rem start be
cd %CALENDAR_PATH%\be\target
dir /b be*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -jar %JAR_NAME%
REM Wait for startup
timeout /t 10 /nobreak
cd %START_LOCATION%

Rem start gateway
cd %CALENDAR_PATH%\gateway\target
dir /b gateway*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -jar %JAR_NAME%
REM Wait for startup
timeout /t 10 /nobreak
cd %START_LOCATION%

Rem start fe
cd %CALENDAR_PATH%\fe\target
dir /b fe*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -jar %JAR_NAME%
REM Wait for startup
timeout /t 10 /nobreak
cd %START_LOCATION%


