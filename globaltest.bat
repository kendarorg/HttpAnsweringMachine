@echo off

set DOCKER_IP=192.168.1.40
set DOCKER_HOST=tcp://%DOCKER_IP%:23750
set STARTING_PATH=%~dp0

call %STARTING_PATH%\scripts\libs\version.bat

echo [INFO] Compiliing global test runner
cd %STARTING_PATH%/globaltest
call mvn clean install package
echo [INFO] starting global test runner
cd %STARTING_PATH%/globaltest/target

call java -cp globaltest-1.0.0-jar-with-dependencies.jar org.kendar.globaltest.Main






