@echo off

set HAM_JAR=janus-driver-1.1.10-SNAPSHOT.jar
set CALENDAR_PATH=%~dp0
cd ..
set CALENDAR_PATH=%cd%
cd %CALENDAR_PATH%
REM Go to main path
cd ..
set ROOT_PATH=%cd%



cd %CALENDAR_PATH%\be
dir /b be*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java  -cp "be-4.1.5.jar;janus-driver-1.1.10-SNAPSHOT.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file:///%cd%\benogen.application.properties
REM start java -jar %JAR_NAME%  --spring.config.location=file:///%cd%\bedbham.application.properties
REM Wait for startup
timeout /t 10 /nobreak
cd %ROOT_PATH%
