@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat

echo This will publish jars on the kendar maven repo. Ctrl+C to exit
echo Target version: %HAM_VERSION%

pause

REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

REM Deploys all jars on kendar mvn
cd %ROOT_DIR%\ham
echo Deploying ham
call mvn deploy

REM Restore previous dir
cd %START_LOCATION%