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
cd $CALENDAR_PATH/be
java   -cp "be-4.1.4.jar:janus-driver-1.1.10-SNAPSHOT.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file://$(pwd)/application.properties &
cd $START_LOCATION

