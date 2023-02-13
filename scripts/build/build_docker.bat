@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat
set DOCKER_LIB=%SCRIPT_DIR%\libs\docker.bat

echo [INFO] This will build the docker images for the application
echo [INFO] and publish them on local docker. Ctrl+C to exit
echo [INFO] Target version: %HAM_VERSION%


set LOGIN=kendarorg
set ORG=kendarorg
set PASSWORD=none

if "%DOCKER_DEPLOY%" == "true" (
pause
    echo Enter %LOGIN% password for %ORG%
    call %UTILS_LIB% read_password PASSWORD
    call %DOCKER_LIB% docker_login "%LOGIN%" "%PASSWORD%" "%ORG%"
)
set PASSWORD=none

REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR
set DOCKER_ROOT=%ROOT_DIR%\docker\images
set HAM_DIR=%ROOT_DIR%\ham
set HAM_LIBS_DIR=%HAM_DIR%\libs

REM Init variables
set ROOT_PWD=root
set HAM_DEBUG=false
set DNS_HIJACK_SERVER=THEDOCKERNAMEOFTHERUNNINGMASTER

cd %HAM_DIR%

cd %DOCKER_ROOT%\base
docker build --rm -t ham.base .
call %DOCKER_LIB% docker_push "ham.base" "%HAM_VERSION%"

cd %DOCKER_ROOT%\client
mkdir data\app || true
del /S /Q data\app\*.*
copy /y %HAM_DIR%\simpledns\target\simpledns*.jar data\
docker build --rm -t ham.client .
call %DOCKER_LIB% docker_push "ham.client" "%HAM_VERSION%"
call %UTILS_LIB% rm_rf data\app
del /S /Q data\simpledns*.jar

cd %DOCKER_ROOT%\openvpn
docker build --rm -t ham.openvpn .
call %DOCKER_LIB% docker_push "ham.openvpn" "%HAM_VERSION%"

cd %DOCKER_ROOT%\master
mkdir data\app
del /S /Q data\app\*.*  2>&1 1>NUL
mkdir data\app\libs
del /S /Q data\app\libs\*.*  2>&1 1>NUL
copy /y %HAM_DIR%\app\target\app*.jar data\app\
copy /y %HAM_LIBS_DIR%\*.jar data\app\libs\
docker build --rm -t ham.master .
call %DOCKER_LIB% docker_push "ham.master" "%HAM_VERSION%"
call %UTILS_LIB% rm_rf data\app 2>&1 1>NUL

cd %DOCKER_ROOT%\singlemaster
docker build --rm -t ham.singlemaster .
call %DOCKER_LIB% docker_push "ham.singlemaster" "%HAM_VERSION%"

cd %DOCKER_ROOT%\apache
docker build --rm -t ham.apache .
call %DOCKER_LIB% docker_push "ham.apache" "%HAM_VERSION%"

cd %DOCKER_ROOT%\apache-php8
docker build --rm -t ham.apache.php8 .
call %DOCKER_LIB% docker_push "ham.apache.php8" "%HAM_VERSION%"

cd %DOCKER_ROOT%\mysql
docker build --rm -t ham.mysql .
call %DOCKER_LIB% docker_push "ham.mysql" "%HAM_VERSION%"

REM Restore previous dir
cd %START_LOCATION%
