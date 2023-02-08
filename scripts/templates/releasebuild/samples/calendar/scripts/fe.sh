#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR
cd ..

HAM_JAR=janus-driver-1.1.10-SNAPSHOT.jar
CALENDAR_PATH=$(pwd)
cd $CALENDAR_PATH
# Go to main path
cd ..
ROOT_PATH=$(pwd)



# start fe
cd $CALENDAR_PATH/fe
java -jar "fe-4.1.4.jar" --spring.config.location=file://$(pwd)/application.properties &
cd $START_LOCATION

