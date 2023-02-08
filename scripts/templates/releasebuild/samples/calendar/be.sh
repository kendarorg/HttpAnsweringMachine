#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR

HAM_JAR=janus-driver-1.1.10-SNAPSHOT.jar
CALENDAR_PATH=$(pwd)
cd $CALENDAR_PATH
# Go to main path
cd ..
ROOT_PATH=$(pwd)

function pause {
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}


echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

echo Start it only when recording/replaying is started
pause

# start fe
cd $CALENDAR_PATH/scripts
./be.sh
cd $START_LOCATION

