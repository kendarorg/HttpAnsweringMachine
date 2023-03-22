#!/bin/bash

export DOCKER_IP=192.168.1.40
export DOCKER_HOST=tcp://$DOCKER_IP:23750
export STARTING_PATH=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
export HAM_VERSION=4.2.1
export LOG_PATH=$STARTING_PATH

find $STARTING_PATH -name "*.sh" -exec chmod +x {} \;

. $STARTING_PATH/scripts/libs/version.sh

rm -f $STARTING_PATH/globaltest*.log
echo "[INFO] Compiling global test runner"
cd $STARTING_PATH/globaltest
mvn clean install package -DskipTests > /dev/null 2>&1

cd $STARTING_PATH/globaltest/globaltest-main/target

export currentUser=$(logname)

echo Using user $currentUser behind sudo
java -cp globaltest-main-$HAM_VERSION.jar org.kendar.globaltest.Main
cd $STARTING_PATH
chown -R $currentUser $STARTING_PATH








