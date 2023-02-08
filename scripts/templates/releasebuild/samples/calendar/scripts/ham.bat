@echo off

set HAM_JAR=janus-driver-1.1.10-SNAPSHOT.jar
set CALENDAR_PATH=%~dp0
cd ..
set CALENDAR_PATH=%cd%
cd %CALENDAR_PATH%
REM Go to main path
cd ..
set ROOT_PATH=%cd%
Rem start ham
cd %ROOT_PATH%\ham
dir /b app*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt

REM Start the application
start java "-Dloader.path=%ROOT_PATH%\ham\libs"  -Dloader.main=org.kendar.Main  ^
	  	"-Djsonconfig=%CALENDAR_PATH%\calendar.external.json" ^
		-jar %ROOT_PATH%\ham\%JAR_NAME% org.springframework.boot.loader.PropertiesLauncher
