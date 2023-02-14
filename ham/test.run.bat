@ECHO OFF

REM Initialize
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%
set HAM_VERSION=4.1.5

REM Retrieve the jar name
REM dir /b %SCRIPT_DIR%\target\*.jar > .temp.txt
REM set /p JAR_NAME=<.temp.txt
REM del /s /f /q .temp.txt 2>&1 1>NUL
set AGENT_PATH=%SCRIPT_DIR%/api.test/org.jacoco.agent-0.8.8-runtime.jar
set EXEC_PATH=%SCRIPT_DIR%/api.test/target/jacoco_run_starter.exec


REM https://www.jacoco.org/jacoco/trunk/doc/cli.html

REM Start the application
start /b java "-Dloader.path=%SCRIPT_DIR%/libs"  -Dloader.main=org.kendar.Main  ^
	  	"-Djsonconfig=%SCRIPT_DIR%\test.external.json" -Dham.tempdb=data\tmp^
	  	"-javaagent:%AGENT_PATH%=destfile=%EXEC_PATH%,includes=org.kendar.**" ^
		-jar "%SCRIPT_DIR%/app/target/app-%HAM_VERSION%.jar" org.springframework.boot.loader.PropertiesLauncher

ping 127.0.0.1 -n 15 > nul

mvn test