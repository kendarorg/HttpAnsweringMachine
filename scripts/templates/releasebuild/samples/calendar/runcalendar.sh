#!/bin/bash

CALENDAR_PATH=$(pwd)
cd $CALENDAR_PATH
# Go to main path
cd ..
ROOT_PATH=$(pwd)

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

function is_set { [[ $var ]]; echo $?; }
function pause{
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

export DEBUG_AGENT=
is_set DO_DEBUG ; export DEBUG_AGENT=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025
# "$DEBUG_AGENT"

pause

cd $ROOT_PATH/ham

# Start the application
cd $CALENDAR_PATH/be/target
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java "-Dloader.path=$ROOT_PATH/ham/libs"  -Dloader.main=org.kendar.Main  \
	  	"$DEBUG_AGENT" \
	  	"-Djsonconfig=$CALENDAR_PATH/calendar.external.json" \
		  -jar "$HAM_DIR/$JAR_NAME" org.springframework.boot.loader.PropertiesLauncher

cd $ROOT_PATH

export DEBUG_AGENT=
is_set DO_DEBUG ; export DEBUG_AGENT=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5026
# "$DEBUG_AGENT"

# start be
cd $CALENDAR_PATH/be
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java "$DEBUG_AGENT" -jar $JAR_NAME  --spring.config.location=file://$(pwd)/application.properties &
cd $START_LOCATION

# start gateway
cd $CALENDAR_PATH/gateway
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java -jar $JAR_NAME --spring.config.location=file://$(pwd)/application.properties &
cd $START_LOCATION

# start fe
cd $CALENDAR_PATH/fe
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java -jar $JAR_NAME --spring.config.location=file://$(pwd)/application.properties &
cd $START_LOCATION

