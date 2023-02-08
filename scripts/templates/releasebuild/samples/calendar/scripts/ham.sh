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
cd $ROOT_PATH/ham

# Start the application
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java "-Dloader.path=$ROOT_PATH/ham/libs"  -Dloader.main=org.kendar.Main  \
	  	"-Djsonconfig=$CALENDAR_PATH/calendar.external.json" \
		  -jar "$ROOT_PATH/ham/$JAR_NAME" org.springframework.boot.loader.PropertiesLauncher
cd $ROOT_PATH

