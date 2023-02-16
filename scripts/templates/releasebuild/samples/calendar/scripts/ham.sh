#!/bin/bash


HAM_VERSION=4.1.6
START_LOCATION=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR
cd ..
CALENDAR_PATH=$(pwd)
# Go to main path
cd ..
ROOT_PATH=$(pwd)

# start fe
cd $ROOT_PATH/ham


java "-Dloader.path=$ROOT_PATH/ham/libs"  -Dloader.main=org.kendar.Main  \
	  	"-Djsonconfig=$CALENDAR_PATH/calendar.external.json" \
		  -jar "$ROOT_PATH/ham/app-$HAM_VERSION.jar" org.springframework.boot.loader.PropertiesLauncher &
cd $START_LOCATION

