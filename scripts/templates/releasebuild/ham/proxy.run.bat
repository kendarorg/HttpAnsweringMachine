@ECHO OFF

REM Initialize
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%

REM Retrieve the jar name
dir /b %SCRIPT_DIR%\*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
del /s /f /q .temp.txt 2>&1 1>NUL




REM Start the application
java "-Dloader.path=%SCRIPT_DIR%/libs"  -Dloader.main=org.kendar.Main  ^
	  	"-Djsonconfig=%SCRIPT_DIR%\proxy.external.json" ^
		-jar %JAR_NAME% org.springframework.boot.loader.PropertiesLauncher
