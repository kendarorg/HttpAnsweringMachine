#!/bin/bash
START_LOCATION=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR
cd ..
CALENDAR_PATH=$(pwd)

# start fe
cd $CALENDAR_PATH/be
java   -cp "be-4.1.5.jar:janus-driver-1.1.10-SNAPSHOT.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file://$(pwd)/bedbhamnogen.application.properties &
cd $START_LOCATION

