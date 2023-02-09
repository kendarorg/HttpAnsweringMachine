#!/bin/bash
HAM_MAIN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
export LANG=en_US.UTF-8
export LC_ALL=$LANG

. $HAM_MAIN_DIR/scripts/libs/version.sh
. $HAM_MAIN_DIR/scripts/libs/runner.sh

function pause {
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

terminate_ham proxy.run.sh java HttpAnswering

cd $HAM_MAIN_DIR
cd scripts/build

if true; then
  rm -rf $HAM_MAIN_DIR/release/


  cd $HAM_MAIN_DIR
  cd scripts/build
  echo [INFO] BEG build_release
  ./build_release.sh
  echo [INFO] END build_release
  GREP_RESULT=$(grep --include=\*.log -rnw "$HAM_MAIN_DIR/release" -e 'ERROR')
  if [ -n "$GREP_RESULT" ]
  then
    echo "[ERROR] build_release"
    exit 1
  fi

  echo [INFO] BEG build_release_samples
  ./build_release_samples.sh
  echo [INFO] END build_release_samples
  GREP_RESULT=$(grep --include=\*.log -rnw "$HAM_MAIN_DIR/release" -e 'ERROR')
  if [ -n "$GREP_RESULT" ]
  then
    echo "[ERROR] build_release_samples"
    exit 1
  fi


  cd $HAM_MAIN_DIR/release
  find "$HAM_MAIN_DIR/release" -name "*.tar.gz" -type f -exec tar xzf {} \;


  cd $HAM_MAIN_DIR/release/ham
  chmod +x *.sh
  cd $HAM_MAIN_DIR/release/calendar
  chmod +x *.sh
  cd $HAM_MAIN_DIR/release/calendar/scripts
  chmod +x *.sh
  cd $HAM_MAIN_DIR/release/simpledns
  chmod +x *.sh

  echo [INFO] BEG ham/local.run.sh
  cd $HAM_MAIN_DIR/release/ham
  run_till_start 60 local.run.sh http://127.0.0.1/api/health
  terminate_app local.run.sh java HttpAnswering
  echo [INFO] END ham/local.run.sh

  echo [INFO] BEG ham/proxy.run.sh
  export http_proxy=http://127.0.0.1:1081
  cd $HAM_MAIN_DIR/release/ham
  run_till_start 60 proxy.run.sh http://www.local.test/api/health
  terminate_app proxy.run.sh java HttpAnswering
  unset http_proxy
  echo [INFO] END ham/proxy.run.sh

  echo [INFO] BEG calendar/scripts/be.sh
  cd $HAM_MAIN_DIR/release/calendar/scripts
  run_till_start 60 be.sh http://127.0.0.1:8100/api/v1/health
  terminate_app be.sh java HttpAnswering
  echo [INFO] END calendar/scripts/be.sh

  echo [INFO] BEG calendar/scripts/fe.sh
  cd $HAM_MAIN_DIR/release/calendar/scripts
  run_till_start 60 fe.sh http://127.0.0.1:8080/api/v1/health
  terminate_app fe.sh java HttpAnswering
  echo [INFO] END calendar/scripts/fe.sh

  echo [INFO] BEG calendar/scripts/gateway.sh
  cd $HAM_MAIN_DIR/release/calendar/scripts
  run_till_start 60 gateway.sh http://127.0.0.1:8090/api/v1/health
  terminate_app gateway.sh java HttpAnswering
  echo [INFO] END calendar/scripts/gateway.sh

echo [INFO] BEG calendar/scripts/ham.sh
cd $HAM_MAIN_DIR/release/calendar/scripts
export http_proxy=http://127.0.0.1:1081
run_till_start 60 ham.sh http://www.local.test/api/health
terminate_app ham.sh java HttpAnswering
unset http_proxy
echo [INFO] END calendar/scripts/ham.sh

  echo [INFO] BEG calendar/scripts/bedb.sh
  cd $HAM_MAIN_DIR/release/calendar
  run_till_start 60 rundb.sh  http://localhost:8082
  sleep 5
  cd $HAM_MAIN_DIR/release/calendar/scripts
  run_till_start 60 bedb.sh http://127.0.0.1:8100/api/v1/health
  terminate_app bedb.sh java HttpAnswering
  terminate_app bedb.sh java org.h2.tools.Server
  echo [INFO] END calendar/scripts/bedb.sh
  rm -rf $HAM_MAIN_DIR/release/calendar/data

  cd $HAM_MAIN_DIR/scripts/build
  ./build_docker.sh
  ./build_docker_samples.sh
  cd $HAM_MAIN_DIR/samples/calendar/hub_composer
  nohup docker-compose -f docker-compose-local.yml up 2>&1 > /dev/null &

  export http_proxy=http://$DOCKER_IP:1081
  wait_till_start 60 http://www.local.test/api/health
  wait_till_start 60 http://www.sample.test/api/v1/health
  wait_till_start 60 http://gateway.sample.test/api/v1/health
  wait_till_start 60 http://be.sample.test/api/v1/health
  unset http_proxy
  docker-compose -f docker-compose-local.yml down

  cd $HAM_MAIN_DIR/samples/quotes/hub_composer
  nohup docker-compose -f docker-compose-local.yml up 2>&1 > /dev/null &

  export http_proxy=http://$DOCKER_IP:1081
  wait_till_start 60 http://www.local.test/api/health
  wait_till_start 60 http://www.quotes.test/api/health/index.php
  unset http_proxy
  docker-compose -f docker-compose-local.yml down


  echo [INFO] BEG calendar/runcalendar.sh
  cd $HAM_MAIN_DIR/release/calendar
  export http_proxy=http://127.0.0.1:1081
  run_till_start 60 runcalendar.sh http://www.local.test/api/health
  wait_till_start 60 http://www.sample.test/api/v1/health
  wait_till_start 60 http://localhost/int/gateway.sample.test/api/v1/health
  wait_till_start 60 http://localhost/int/be.sample.test/api/v1/health
  terminate_app runcalendar.sh java HttpAnswering
  unset http_proxy
  echo [INFO] END calendar/runcalendar.sh
fi







