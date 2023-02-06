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

set DEBUG_AGENT=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5026
IF "%DO_DEBUG%"=="" set DEBUG_AGENT=

cd %CALENDAR_PATH%\be
dir /b be*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java %DEBUG_AGENT% -cp "be-4.1.4.jar;janus-driver-1.1.10-SNAPSHOT.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file:///%cd%\bedbhamnogen.application.properties
REM start java -jar %JAR_NAME%  --spring.config.location=file:///%cd%\bedbham.application.properties
REM Wait for startup
timeout /t 10 /nobreak
cd %ROOT_PATH%
