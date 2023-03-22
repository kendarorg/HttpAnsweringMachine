@echo off

set DOCKER_IP=192.168.56.2
set DOCKER_HOST=tcp://%DOCKER_IP%:23750
set STARTING_PATH=%~dp0
set HAM_VERSION=4.2.1
set LOG_PATH=%STARTING_PATH%

call %STARTING_PATH%\scripts\libs\version.bat
del /S /Q %STARTING_PATH%\globaltest*.log 2>&1 1>NUL
echo [INFO] Compiling global test runner

set UTILS_TODEL_DIR=%STARTING_PATH%\globaltest\target\
IF exist %UTILS_TODEL_DIR% (
    del /s /f /q "%UTILS_TODEL_DIR%\*.*"  2>&1 1>NUL
    for /f %%f in ('dir /ad /b %UTILS_TODEL_DIR%\') do rd /s /q %UTILS_TODEL_DIR%\%%f  2>&1 1>NUL
    rmdir /S /Q %UTILS_TODEL_DIR%  2>&1 1>NUL
)

cd %STARTING_PATH%\globaltest
call mvn clean install package -DskipTests 2>&1 1>NUL
echo [INFO] starting global test runner
cd %STARTING_PATH%\globaltest\globaltest-main\target

call java -cp globaltest-main-%HAM_VERSION%.jar org.kendar.globaltest.Main
cd %STARTING_PATH%






