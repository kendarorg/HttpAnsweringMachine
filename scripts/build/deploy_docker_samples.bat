@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat
set DOCKER_LIB=%SCRIPT_DIR%\libs\docker.bat

echo This will publish the docker images for the application
echo from the local docker repo. Ctrl+C to exit
echo Target version: %HAM_VERSION%

pause

REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

set LOGIN=kendarorg
set ORG=kendarorg
echo Enter %LOGIN% password for %ORG%
call %UTILS_LIB% read_password PASSWORD
call %DOCKER_LIB% docker_login "%LOGIN%" "%PASSWORD%" "%ORG%"
set PASSWORD=none
call %DOCKER_LIB% docker_push "ham.sampleapp.be" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.sampleapp.fe" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.sampleapp.gateway" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.sampleapp.multi" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.sampleapp.single" "%HAM_VERSION%"

call %DOCKER_LIB% docker_push "ham.quotes.master" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.quotes.core" "%HAM_VERSION%"

call %DOCKER_LIB% docker_logout

REM Restore previous dir
cd %START_LOCATION%