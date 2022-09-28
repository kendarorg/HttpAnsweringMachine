@echo off

REM Initialize
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0

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
call %DOCKER_LIB% docker_push "ham.base" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.master" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.client" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.openvpn" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.mysql" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.apache" "%HAM_VERSION%"
call %DOCKER_LIB% docker_push "ham.apache.php8" "%HAM_VERSION%"

call %DOCKER_LIB% docker_logout

REM Restore previous dir
cd %START_LOCATION%