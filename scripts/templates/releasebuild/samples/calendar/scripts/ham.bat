@echo off
set HAM_VERSION=4.2.2
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%
cd ..
set CALENDAR_PATH=%cd%
cd ..
set ROOT_PATH=%cd%

cd %ROOT_PATH%\ham

REM Start the application
IF "%RUN_INLINE%"=="" (
    start java "-Dloader.path=%ROOT_PATH%\ham\libs"  -Dloader.main=org.kendar.Main  ^
	  	"-Djsonconfig=%CALENDAR_PATH%\calendar.external.json" ^
		-jar %ROOT_PATH%\ham\app-%HAM_VERSION%.jar org.springframework.boot.loader.PropertiesLauncher
) else (
    start /b java "-Dloader.path=%ROOT_PATH%\ham\libs"  -Dloader.main=org.kendar.Main  ^
    	  	"-Djsonconfig=%CALENDAR_PATH%\calendar.external.json" ^
    		-jar %ROOT_PATH%\ham\app-%HAM_VERSION%.jar org.springframework.boot.loader.PropertiesLauncher
)
cd %START_LOCATION%