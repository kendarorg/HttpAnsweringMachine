#!/bin/sh

HAM_JAR=janus-driver-1.1.5.jar
CALENDAR_PATH=$(pwd)
cd $CALENDAR_PATH
# Go to main path
cd ..
ROOT_PATH=$(pwd)

function is_set { [[ $var ]]; echo $? }
function pause{
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}


echo You should configure the http and https proxy to
echo localhost:1081 to appreciate the example

read -p "Do you want to cleanup the database? " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
  mkdir -p $CALENDAR_PATH/data
  rm $CALENDAR_PATH/data/*.db || true
fi

pause

cd $ROOT_PATH/ham

# start db
cd $CALENDAR_PATH/
rundb.sh &
cd $START_LOCATION

export DEBUG_AGENT=
is_set DO_DEBUG ; export DEBUG_AGENT=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025
# "$DEBUG_AGENT"

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



# start be
export DEBUG_AGENT=
is_set DO_DEBUG ; export DEBUG_AGENT=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5026
# "$DEBUG_AGENT"

cd $CALENDAR_PATH/be
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java "$DEBUG_AGENT" -jar $JAR_NAME -classpath $CALENDAR_PATH\$HAM_JAR --spring.config.location=file://$(pwd)/bedbham.application.properties &
cd $START_LOCATION

# start gateway
cd $CALENDAR_PATH/gateway
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java -jar $JAR_NAME --spring.config.location=file://$(pwd)/application.properties &
cd $START_LOCATION

echo Start it only when recording/replaying is started
pause

# start fe
cd $CALENDAR_PATH/fe
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true
java -cp "be-4.1.4.jar;../janus-driver-1.1.5.jar" org.springframework.boot.loader.JarLauncher --spring.config.location=file://$(pwd)/application.properties &
cd $START_LOCATION

