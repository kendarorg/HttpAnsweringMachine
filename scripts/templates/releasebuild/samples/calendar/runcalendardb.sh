#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR

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

read -p "Do you want to cleanup the database? " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
  mkdir -p $CALENDAR_PATH/data
  rm $CALENDAR_PATH/data/*.db || true
fi

pause

cd $ROOT_PATH/calendar/scripts

./ham.sh
./bedb.sh
./gateway.sh
./fe.sh

cd $START_LOCATION

