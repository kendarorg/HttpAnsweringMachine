@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat

echo This will run the application standalone from sources
echo Target version: %HAM_VERSION%

pause

REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR
set HAM_DIR=%ROOT_DIR%\ham\app\target
set HAM_LIBS_DIR=%ROOT_DIR%\ham\libs

REM Retrieve the jar name
dir /b "%HAM_DIR%\*.jar" > .temp.txt
set /p JAR_NAME=<.temp.txt
del /s /f /q .temp.txt 2>&1 1>NUL

IF "%JSON_CONFIG%"=="" set JSON_CONFIG=%ROOT_DIR%\ham\external.json

REM Start the application
java "-Dloader.path=%HAM_LIBS_DIR%"  -Dloader.main=org.kendar.Main  ^
	  	-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025 ^
	  	"-Djsonconfig=%JSON_CONFIG%" ^
		-jar %HAM_DIR%\%JAR_NAME% org.springframework.boot.loader.PropertiesLauncher