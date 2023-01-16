@echo off

set HAM_JAR=janus-driver-1.0.11-SNAPSHOT.jar
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
REM Start the application
start java "-Dloader.path=%ROOT_PATH%\ham\libs"  -Dloader.main=org.kendar.Main  ^
	  	-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025 ^
	  	"-Djsonconfig=%CALENDAR_PATH%\calendar.external.json" ^
		-jar %JAR_NAME% org.springframework.boot.loader.PropertiesLauncher
REM Wait for startup
timeout /t 10 /nobreak
cd %ROOT_PATH%





Rem start gateway
cd %CALENDAR_PATH%\gateway
dir /b gateway*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -jar %JAR_NAME% --spring.config.location=file:///%cd%\application.properties
REM Wait for startup
timeout /t 10 /nobreak
cd %ROOT_PATH%

Rem start fe
cd %CALENDAR_PATH%\fe
dir /b fe*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -jar %JAR_NAME% --spring.config.location=file:///%cd%\application.properties
REM Wait for startup
timeout /t 10 /nobreak
cd %ROOT_PATH%

echo Start it only when recording/replaying is started
pause

cd %CALENDAR_PATH%\be
dir /b be*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
start java -cp "be-4.1.3-SNAPSHOT.jar;janus-driver-1.1.0.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file:///%cd%\bedbham.application.properties
REM start java -jar %JAR_NAME%  --spring.config.location=file:///%cd%\bedbham.application.properties
REM Wait for startup
timeout /t 10 /nobreak
cd %ROOT_PATH%
