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

REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

echo Build calendar sample images
cd %ROOT_DIR%\samples\calendar\docker\multi
docker build --rm -t ham.sampleapp.multi -f multimaster.Dockerfile ..\..\
docker build  --rm -t ham.sampleapp.fe -f fe.Dockerfile ..\..\
docker build  --rm -t ham.sampleapp.be -f be.Dockerfile ..\..\
docker build  --rm -t ham.sampleapp.gateway -f gateway.Dockerfile ..\..\

cd %ROOT_DIR%\samples\calendar\docker\single
docker build -t ham.sampleapp.single -f Dockerfile ..\..\

echo Build quotes sample images
cd %ROOT_DIR%\samples\quotes\docker\multi
docker build  --rm -t ham.quotes.master -f multimaster.Dockerfile ..\..\
docker build  --rm -t ham.quotes.core -f core.Dockerfile ..\..\