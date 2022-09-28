@echo off

REM Initialize
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat
set DOCKER_LIB=%SCRIPT_DIR%\libs\docker.bat

echo This will build the docker images for the samples
echo and publish them on local docker. Ctrl+C to exit
echo Target version: %HAM_VERSION%

pause

REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR
set DOCKER_ROOT=%ROOT_DIR%\docker\images

echo NOT YET IMPLEMENTED
pause