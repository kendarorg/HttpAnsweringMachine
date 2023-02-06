#!/bin/bash

HAM_JAR=janus-driver-1.1.10-SNAPSHOT.jar
CALENDAR_PATH=$(pwd)
cd $CALENDAR_PATH
# Go to main path
cd ..
ROOT_PATH=$(pwd)

function pause{
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

function is_set { [[ $var ]]; echo $?; }

export DEBUG_AGENT=
is_set DO_DEBUG ; export DEBUG_AGENT=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5026
# "$DEBUG_AGENT"

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

echo Start it only when recording/replaying is started
pause

# start fe
cd $CALENDAR_PATH/fe
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java "$DEBUG_AGENT" -cp "be-4.1.4.jar;../janus-driver-1.1.10-SNAPSHOT.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file://$(pwd)/bedbhamnogen.application.properties &
cd $START_LOCATION

