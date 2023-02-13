@echo off

set LIB_SCRIPT_DIR=%~dp0\win64\

call:%~1 %~2 %~3 %~4 %~5 %~6 %~7 %~8 %~9 %~10
goto exit

set DOCKER_USERNAME=none
set DOCKER_LOGIN=none
set DOCKER_TOKEN=none
set DOCKER_ORG=none

:docker_login
if "%DOCKER_DEPLOY%" == "true" (
  set DOCKER_USERNAME=%~1
  set DOCKER_PASSWORD=%~2
  set DOCKER_ORG=%~3
  docker login -u "%DOCKER_USERNAME%" -p "%DOCKER_PASSWORD%"
  set DOCKER_TOKEN=none

  call %LIB_SCRIPT_DIR%curl -s -H "Content-Type: application/json" ^
    -X POST -d "{\"username\":\"%DOCKER_USERNAME%\",\"password\":\"%DOCKER_PASSWORD%\"}" "https://hub.docker.com/v2/users/login/" ^
    -o .tmp.txt
  call %LIB_SCRIPT_DIR%jq-win64 -r .token .tmp.txt > .tmp2.txt
  set /p DOCKER_TOKEN=<.tmp2.txt
  del /S /Q .tmp.txt 2>&1 1>NUL
  del /S /Q .tmp2.txt 2>&1 1>NUL
  set DOCKER_PASSWORD=none
  )
goto :eof

:docker_logout
    set DOCKER_PASSWORD=none
    set DOCKER_TOKEN=none
goto :eof

:docker_remove_tag
  REM set DOCKER_IMAGE_NAME=%~1
  REM set DOCKER_TAG=%~2
  REM call %LIB_SCRIPT_DIR%curl "https://hub.docker.com/v2/repositories/%DOCKER_ORG%/%DOCKER_IMAGE_NAME%/tags/%DOCKER_TAG%/" ^
  REM     -X DELETE ^
  REM     -H "Authorization: JWT %DOCKER_TOKEN%"
goto :eof


:docker_push
    if "%DOCKER_DEPLOY%" == "true" (
  set IMAGE_NAME=%~1
  set VERSION_NUMBER=%~2

  docker tag %IMAGE_NAME% %LOGIN%/%IMAGE_NAME%

  if NOT "%VERSION_NUMBER%"=="%VERSION_NUMBER:SNAPSHOT=%" (
    REM if [[ "%VERSION_NUMBER%" == *"snapshot"* ]] ;then
    echo [INFO] Pushing snapshot base image tag %IMAGE_NAME%
    call docker push %DOCKER_ORG%/%IMAGE_NAME%

    echo [INFO] Tagging image %IMAGE_NAME%
    call docker tag %DOCKER_ORG%/%IMAGE_NAME% %DOCKER_ORG%/%IMAGE_NAME%:v%VERSION_NUMBER%
    call docker push %DOCKER_ORG%/%IMAGE_NAME%:v%VERSION_NUMBER%

    call docker tag %DOCKER_ORG%/%IMAGE_NAME%:v%VERSION_NUMBER% %DOCKER_ORG%/%IMAGE_NAME%:snapshot
    call docker push %DOCKER_ORG%/%IMAGE_NAME%:snapshot
  ) else (
    echo [INFO] Pushing base image tag %IMAGE_NAME%
    call docker push %DOCKER_ORG%/%IMAGE_NAME%

    echo [INFO] Tagging image %IMAGE_NAME%
    call docker tag %DOCKER_ORG%/%IMAGE_NAME% %DOCKER_ORG%/%IMAGE_NAME%:v%VERSION_NUMBER%
    call docker push %DOCKER_ORG%/%IMAGE_NAME%:v%VERSION_NUMBER%

    call docker tag %DOCKER_ORG%/%IMAGE_NAME%:v%VERSION_NUMBER% %DOCKER_ORG%/%IMAGE_NAME%:latest
    call docker push %DOCKER_ORG%/%IMAGE_NAME%:latest

    call docker tag %DOCKER_ORG%/%IMAGE_NAME%:v%VERSION_NUMBER% %DOCKER_ORG%/%IMAGE_NAME%:snapshot
    call docker push %DOCKER_ORG%/%IMAGE_NAME%:snapshot
  )
  )
goto :eof



:exit
exit /b