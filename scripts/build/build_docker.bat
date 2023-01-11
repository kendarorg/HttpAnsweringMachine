@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat
set DOCKER_LIB=%SCRIPT_DIR%\libs\docker.bat

echo This will build the docker images for the application
echo and publish them on local docker. Ctrl+C to exit
echo Target version: %HAM_VERSION%

pause

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

echo Building project %HAM_DIR%
call mvn install -DskipTests

cd %DOCKER_ROOT%\base
docker build --rm -t ham.base .

cd %DOCKER_ROOT%\client
mkdir -p data\app || true
del /S /Q data\app\*.*
copy /y %HAM_DIR%\simpledns\target\simpledns*.jar data\
docker build --rm -t ham.client .
call %UTILS_LIB% rm_rf data\app
del /S /Q data\simpledns*.jar

cd %DOCKER_ROOT%\openvpn
docker build --rm -t ham.openvpn .

cd %DOCKER_ROOT%\master
mkdir -p data\app
del /S /Q data\app\*.*  2>&1 1>NUL
mkdir -p data\app\libs
del /S /Q data\app\libs\*.*  2>&1 1>NUL
copy /y %HAM_DIR%\app\target\app*.jar data\app\
copy /y %HAM_LIBS_DIR%\*.jar data\app\libs\
docker build --rm -t ham.master .
call %UTILS_LIB% rm_rf data\app 2>&1 1>NUL

cd %DOCKER_ROOT%\singlemaster
docker build --rm -t ham.singlemaster .

cd %DOCKER_ROOT%\apache
docker build --rm -t ham.apache .

cd %DOCKER_ROOT%\apache-php8
docker build --rm -t ham.apache.php8 .

cd %DOCKER_ROOT%\mysql
docker build --rm -t ham.mysql .

echo Cleanup
cd %HAM_DIR%
call mvn clean -DskipTests > \dev\null 2>1

REM Restore previous dir
cd %START_LOCATION%
