@ECHO OFF

set HAM_VERSION=4.3.1
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
REM start /b java "-Dloader.path=%SCRIPT_DIR%/libs"  -Dloader.main=org.kendar.Main  ^
REM 	  	"-Djsonconfig=%SCRIPT_DIR%\test.external.json" -Dham.tempdb=data\tmp^
REM 	  	"-javaagent:%AGENT_PATH%=destfile=%EXEC_PATH%,includes=org.kendar.**,dumponexit=true" ^
REM 		-jar "%SCRIPT_DIR%/app/target/app-%HAM_VERSION%.jar" org.springframework.boot.loader.PropertiesLauncher
REM 
REM ping 127.0.0.1 -n 15 > nul

mvn test -Dmaven.test.failure.ignore=true