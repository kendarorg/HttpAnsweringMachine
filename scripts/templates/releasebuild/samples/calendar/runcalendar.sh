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


cd $CALENDAR_PATH/scripts

./ham.sh
./be.sh
./gateway.sh
./fe.sh

cd $ROOT_PATH

