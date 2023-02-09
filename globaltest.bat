@echo off
set HAM_MAIN_DIR=%~dp0

call %HAM_MAIN_DIR%\scripts\libs\version.bat
set UTILS_LIB=%HAM_MAIN_DIR%\scripts\libs\utils.bat
set RUNNER_LIB=%HAM_MAIN_DIR%\scripts\libs\runner.bat



terminate_ham proxy.run.bat java HttpAnswering

cd %HAM_MAIN_DIR%
cd scripts/build

if "true" == "true" (
    call %UTILS_LIB% rm_rf %HAM_MAIN_DIR%/release/


  cd %HAM_MAIN_DIR%
  cd scripts/build
  echo [INFO] BEG build_release
  build_release.bat
  echo [INFO] END build_release
  set GREP_RESULT=$(grep --include=\*.log -rnw "%HAM_MAIN_DIR%/release" -e 'ERROR')
  if "%GREP_RESULT%" (
  )else(
    echo "[ERROR] build_release"
    exit 1
  )

  echo [INFO] BEG build_release_samples
  build_release_samples.bat
  echo [INFO] END build_release_samples
  set GREP_RESULT=$(grep --include=\*.log -rnw "%HAM_MAIN_DIR%/release" -e 'ERROR')
  if "%GREP_RESULT%" (
    )else(
      echo "[ERROR] build_release_samples"
      exit 1
    )
    
    
    cd %HAM_MAIN_DIR%/release
    find "%HAM_MAIN_DIR%/release" -name "*.tar.gz" -type f -exec tar xzf {} \;
    
    echo [INFO] BEG ham/local.run.bat
    cd %HAM_MAIN_DIR%/release/ham
    run_till_start 60 local.run.bat http://127.0.0.1/api/health
    terminate_app local.run.bat java HttpAnswering
    echo [INFO] END ham/local.run.bat
    
    echo [INFO] BEG ham/proxy.run.bat
    SET http_proxy=http://127.0.0.1:1081
    cd %HAM_MAIN_DIR%/release/ham
    run_till_start 60 proxy.run.bat http://www.local.test/api/health
    terminate_app proxy.run.bat java HttpAnswering
    set http_proxy=""
    echo [INFO] END ham/proxy.run.bat
    
    echo [INFO] BEG calendar/scripts/be.bat
    cd %HAM_MAIN_DIR%/release/calendar/scripts
    run_till_start 60 be.bat http://127.0.0.1:8100/api/v1/health
    terminate_app be.bat java HttpAnswering
    echo [INFO] END calendar/scripts/be.bat
    
    echo [INFO] BEG calendar/scripts/fe.bat
    cd %HAM_MAIN_DIR%/release/calendar/scripts
    run_till_start 60 fe.bat http://127.0.0.1:8080/api/v1/health
    terminate_app fe.bat java HttpAnswering
    echo [INFO] END calendar/scripts/fe.bat
    
    echo [INFO] BEG calendar/scripts/gateway.bat
    cd %HAM_MAIN_DIR%/release/calendar/scripts
    run_till_start 60 gateway.bat http://127.0.0.1:8090/api/v1/health
    terminate_app gateway.bat java HttpAnswering
    echo [INFO] END calendar/scripts/gateway.bat
    
    echo [INFO] BEG calendar/scripts/ham.bat
    cd %HAM_MAIN_DIR%/release/calendar/scripts
    SET http_proxy=http://127.0.0.1:1081
    run_till_start 60 ham.bat http://www.local.test/api/health
    terminate_app ham.bat java HttpAnswering
    set http_proxy=""
    echo [INFO] END calendar/scripts/ham.bat
    
    echo [INFO] BEG calendar/scripts/bedb.bat
    cd %HAM_MAIN_DIR%/release/calendar
    run_till_start 60 rundb.bat  http://localhost:8082
    timeout /t 5 /nobreak
    cd %HAM_MAIN_DIR%/release/calendar/scripts
    run_till_start 60 bedb.bat http://127.0.0.1:8100/api/v1/health
    terminate_app bedb.bat java HttpAnswering
    terminate_app bedb.bat java org.h2.tools.Server
    echo [INFO] END calendar/scripts/bedb.bat
    call %UTILS_LIB% rm_rf %HAM_MAIN_DIR%/release/calendar/data
    
    cd %HAM_MAIN_DIR%/scripts/build
    build_docker.bat
    build_docker_samples.bat
    cd %HAM_MAIN_DIR%/samples/calendar/hub_composer
    call docker-compose -f docker-compose-local.yml up 2>&1 > /dev/null &
    
    SET http_proxy=http://$DOCKER_IP:1081
    wait_till_start 60 http://www.local.test/api/health
    wait_till_start 60 http://www.sample.test/api/v1/health
    wait_till_start 60 http://gateway.sample.test/api/v1/health
    wait_till_start 60 http://be.sample.test/api/v1/health
    set http_proxy=""
    docker-compose -f docker-compose-local.yml down
    
    cd %HAM_MAIN_DIR%/samples/quotes/hub_composer
    call docker-compose -f docker-compose-local.yml up 2>&1 > /dev/null &
    
    SET http_proxy=http://$DOCKER_IP:1081
    wait_till_start 60 http://www.local.test/api/health
    wait_till_start 60 http://www.quotes.test/api/health/index.php
   set http_proxy=""
    docker-compose -f docker-compose-local.yml down
    
    
    echo [INFO] BEG calendar/runcalendar.bat
    cd %HAM_MAIN_DIR%/release/calendar
    SET http_proxy=http://127.0.0.1:1081
    run_till_start 60 runcalendar.bat http://www.local.test/api/health
    wait_till_start 60 http://www.sample.test/api/v1/health
    wait_till_start 60 http://localhost/int/gateway.sample.test/api/v1/health
    wait_till_start 60 http://localhost/int/be.sample.test/api/v1/health
    terminate_app runcalendar.bat java HttpAnswering
   set http_proxy=""
    echo [INFO] END calendar/runcalendar.bat
)







