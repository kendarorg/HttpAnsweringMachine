#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR

CALENDAR_PATH=$(pwd)
cd $CALENDAR_PATH
# Go to main path
cd ..
ROOT_PATH=$(pwd)

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

function pause {
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}



pause

cd $ROOT_PATH/calendar/scripts

./ham.sh
./be.sh
./gateway.sh
./fe.sh

cd $START_LOCATION

