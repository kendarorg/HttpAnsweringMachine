#!/bin/bash
START_LOCATION=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR
cd ..
CALENDAR_PATH=$(pwd)
# start fe
cd $CALENDAR_PATH/gateway
java -jar "gateway-4.1.5.jar" --spring.config.location=file://$(pwd)/application.properties &
cd $START_LOCATION

