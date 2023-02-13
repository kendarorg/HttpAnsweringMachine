@ECHO OFF

REM Initialize
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%

REM Retrieve the jar name
dir /b %SCRIPT_DIR%\*.jar > .temp.txt
set /p JAR_NAME=<.temp.txt
del /s /f /q .temp.txt 2>&1 1>NUL
set AGENT_PATH=%SCRIPT_DIR%/api.test/org.jacoco.agent-0.8.8-runtime.jar
set EXEC_PATH=%SCRIPT_DIR%/api.test/target/test_run_starter.exec





REM Start the application
call java "-Dloader.path=%SCRIPT_DIR%/libs"  -Dloader.main=org.kendar.Main  ^
	  	"-Djsonconfig=%SCRIPT_DIR%\test.external.json" -Dham.tempdb=data\tmp^
	  	"-javaagent:%AGENT_PATH%=destfile=%EXEC_PATH%,includes=org.kendar.**" ^
		-jar %JAR_NAME% org.springframework.boot.loader.PropertiesLauncher

ping 127.0.0.1 -n 15 > nul

mvn test