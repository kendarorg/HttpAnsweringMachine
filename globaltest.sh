#!/bin/bash

export DOCKER_IP=192.168.1.40
export DOCKER_HOST=tcp://$DOCKER_IP:23750
export STARTING_PATH=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
export HAM_VERSION=4.2.0

. $STARTING_PATH/scripts/libs/version.sh

echo "[INFO] Compiling global test runner"
cd $STARTING_PATH/globaltest
mvn clean install package > /dev/null 2>&1

cd $STARTING_PATH/globaltest/globaltest-main/target

java -cp globaltest-$HAM_VERSION-jar-with-dependencies.jar org.kendar.globaltest.Main

cd $STARTING_PATH






