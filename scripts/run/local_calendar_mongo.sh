#!/bin/bash

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

export TEMPLATES_LOCATION=$SCRIPT_DIR/templates/standalone
export JSON_CONFIG=$SCRIPT_DIR/templates/standalone/calendar.external.json

$SCRIPT_DIR/run/local.sh &

export CALENDAR_PATH=$ROOT_DIR/samples/calendar

echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

pause

# start be
cd $CALENDAR_PATH/bemongo/target
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java -jar $JAR_NAME  --spring.config.location=file://$TEMPLATES_LOCATION/bemongo.application.properties &
cd $START_LOCATION

# start gateway
cd $CALENDAR_PATH/gateway/target
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java -jar $JAR_NAME --spring.config.location=file://$TEMPLATES_LOCATION/gateway.application.properties &
cd $START_LOCATION

# start fe
cd $CALENDAR_PATH/fe/target
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java -jar $JAR_NAME --spring.config.location=file://$TEMPLATES_LOCATION/fe.application.properties &
cd $START_LOCATION