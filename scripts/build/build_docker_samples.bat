@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat
set DOCKER_LIB=%SCRIPT_DIR%\libs\docker.bat

echo This will build the docker images for the samples
echo and publish them on local docker. Ctrl+C to exit
echo Target version: %HAM_VERSION%

pause

set LOGIN=kendarorg
set ORG=kendarorg
echo Enter %LOGIN% password for %ORG%
call %UTILS_LIB% read_password PASSWORD
call %DOCKER_LIB% docker_login "%LOGIN%" "%PASSWORD%" "%ORG%"
set PASSWORD=none

REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

echo Build calendar sample images
cd %ROOT_DIR%\samples\calendar\docker\multi
docker build --rm -t ham.sampleapp.multi -f multimaster.Dockerfile ..\..\
call %DOCKER_LIB% docker_push "ham.sampleapp.multi" "%HAM_VERSION%"
docker build  --rm -t ham.sampleapp.fe -f fe.Dockerfile ..\..\
call %DOCKER_LIB% docker_push "ham.sampleapp.fe" "%HAM_VERSION%"
docker build  --rm -t ham.sampleapp.be -f be.Dockerfile ..\..\
call %DOCKER_LIB% docker_push "ham.sampleapp.be" "%HAM_VERSION%"
docker build  --rm -t ham.sampleapp.gateway -f gateway.Dockerfile ..\..\
call %DOCKER_LIB% docker_push "ham.sampleapp.gateway" "%HAM_VERSION%"

cd %ROOT_DIR%\samples\calendar\docker\single
docker build -t ham.sampleapp.single -f Dockerfile ..\..\
call %DOCKER_LIB% docker_push "ham.sampleapp.single" "%HAM_VERSION%"

echo Build quotes sample images
cd %ROOT_DIR%\samples\quotes\docker\multi
docker build  --rm -t ham.quotes.master -f multimaster.Dockerfile ..\..\
call %DOCKER_LIB% docker_push "ham.quotes.master" "%HAM_VERSION%"
docker build  --rm -t ham.quotes.core -f core.Dockerfile ..\..\
call %DOCKER_LIB% docker_push "ham.quotes.core" "%HAM_VERSION%"