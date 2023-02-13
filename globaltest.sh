#!/bin/bash

export DOCKER_IP=192.168.1.40
export DOCKER_HOST=tcp://$DOCKER_IP:23750
export STARTING_PATH=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

. $STARTING_PATH/scripts/libs/version.sh

echo "[INFO] Compiliing global test runner"
cd $STARTING_PATH/globaltest
mvn clean install package > /dev/null 2>&1

cd $STARTING_PATH/globaltest/target

java -cp globaltest-1.0.0-jar-with-dependencies.jar org.kendar.globaltest.Main






