#!/bin/sh

HAM_JAR=janus-driver-1.1.2.jar
CALENDAR_PATH=$(pwd)
cd $CALENDAR_PATH
# Go to main path
cd ..
ROOT_PATH=$(pwd)

function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

echo Start it only when recording/replaying is started
pause

# start fe
cd $CALENDAR_PATH/fe
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java -cp "be-4.1.3-SNAPSHOT.jar;../janus-driver-1.1.2.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file://$(pwd)/application.properties &
cd $START_LOCATION

