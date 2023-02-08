@echo off

set HAM_JAR=janus-driver-1.1.10-SNAPSHOT.jar
set CALENDAR_PATH=%~dp0
cd %CALENDAR_PATH%
REM Go to main path
cd ..
set ROOT_PATH=%cd%

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

choice /M "Do you want to cleanup the database?"



pause

if %errorlevel% EQU 1 (
    md %CALENDAR_PATH%\data
    del /S /Q %CALENDAR_PATH%\data\*.db 2>&1 1>NUL
)


Rem start db
cd %CALENDAR_PATH%
start rundb.bat
REM Wait for startup
timeout /t 10 /nobreak
cd %ROOT_PATH%

Rem start ham
cd %ROOT_PATH%\ham
dir /b app*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt



cd %ROOT_PATH%\scripts
ham.bat
timeout /t 10 /nobreak
gateway.bat
timeout /t 10 /nobreak
fe.bat
timeout /t 10 /nobreak
cd %ROOT_PATH%

echo Start it only when recording/replaying is started
pause

bedbham.bat
timeout /t 10 /nobreak
