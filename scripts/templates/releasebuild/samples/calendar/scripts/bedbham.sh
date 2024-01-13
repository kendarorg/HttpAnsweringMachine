#!/bin/bash

HAM_VERSION=4.3.0
JANUS_DRIVER_VERSION=1.1.12-SNAPSHOT
START_LOCATION=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR
cd ..
CALENDAR_PATH=$(pwd)

# start fe
cd $CALENDAR_PATH/be
java   -cp "be-$HAM_VERSION.jar:janus-driver-$JANUS_DRIVER_VERSION.jar" org.springframework.boot.loader.JarLauncher \
  --spring.config.location=file://$(pwd)/bedbham.application.properties &
cd $START_LOCATION

