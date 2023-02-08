@echo off

set HAM_JAR=janus-driver-1.1.10-SNAPSHOT.jar
set CALENDAR_PATH=%~dp0
cd ..
set CALENDAR_PATH=%cd%
cd %CALENDAR_PATH%
REM Go to main path
cd ..
set ROOT_PATH=%cd%



cd %CALENDAR_PATH%\gateway
dir /b be*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -jar gateway-4.1.5.jar --spring.config.location=file:///%cd%\application.properties
REM Wait for startup
timeout /t 10 /nobreak
cd %ROOT_PATH%
