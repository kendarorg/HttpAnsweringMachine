@ECHO OFF

set HAM_VERSION=4.1.5
REM Initialize
set START_DIR=%~dp0
cd %START_DIR%
cd ..
cd ..
cd ham
set SCRIPT_DIR=%cd%

REM Retrieve the jar name
set AGENT_PATH=%SCRIPT_DIR%/api.test/org.jacoco.agent-0.8.8-runtime.jar
set EXEC_PATH=%SCRIPT_DIR%/api.test/target/test_run_starter.exec


REM https://www.jacoco.org/jacoco/trunk/doc/cli.html


REM Start the application
start /b java "-Dloader.path=%SCRIPT_DIR%/libs"  -Dloader.main=org.kendar.Main  ^
	  	"-Djsonconfig=%SCRIPT_DIR%\test.external.json" -Dham.tempdb=data\tmp^
	  	"-javaagent:%AGENT_PATH%=destfile=%EXEC_PATH%,includes=org.kendar.**,dumponexit=true" ^
		-jar "%SCRIPT_DIR%/app/target/app-%HAM_VERSION%.jar" org.springframework.boot.loader.PropertiesLauncher

ping 127.0.0.1 -n 15 > nul

mvn test