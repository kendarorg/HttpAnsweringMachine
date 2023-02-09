#!/bin/bash

ROOT_PATH=$(pwd)
CALENDAR_PATH=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $CALENDAR_PATH

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

# start db
cd $CALENDAR_PATH/
rundb.sh &

cd $CALENDAR_PATH/scripts

./ham.sh
./gateway.sh
./fe.sh

echo Start it only when recording/replaying is started
pause


./bedbham.sh

cd $ROOT_PATH

