#!/bin/bash

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo [INFO] This will build a tar.gz with the sample applications. Ctrl+C to exit
echo [INFO] Target version: $HAM_VERSION


# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

# Setup the target directory
echo [INFO] Setup target dir
HAM_RELEASE_TARGET=$ROOT_DIR/release/$HAM_VERSION
rm -rf $HAM_RELEASE_TARGET || true

QUOTES_DIR=$ROOT_DIR/samples/quotes
mkdir -p $HAM_RELEASE_TARGET/quotes
mkdir -p $HAM_RELEASE_TARGET/calendar
cp -R $QUOTES_DIR/core $HAM_RELEASE_TARGET/quotes/

CALENDAR_DIR=$ROOT_DIR/samples/calendar
cd $CALENDAR_DIR
mvn clean install > "$ROOT_DIR"/release/ham-"$HAM_VERSION"-samples.log 2>&1

echo [INFO] Setup runner

mkdir -p $HAM_RELEASE_TARGET/calendar/scripts
mkdir -p $HAM_RELEASE_TARGET/calendar/be
mkdir -p $HAM_RELEASE_TARGET/calendar/bemongo
cp -f $SCRIPT_DIR/templates/releasebuild/samples/calendar/*.* $HAM_RELEASE_TARGET/calendar/ 2>&1 > /dev/null
cp -f $SCRIPT_DIR/templates/releasebuild/samples/calendar/scripts/*.* $HAM_RELEASE_TARGET/calendar/scripts/ 2>&1 > /dev/null
cp -f $SCRIPT_DIR/templates/standalone/calendar.external.json $HAM_RELEASE_TARGET/calendar/ 2>&1 > /dev/null

cp -f $ROOT_DIR/ham/plugin.replayer.jdbc/target/classes/lib/janus-driver*.jar $HAM_RELEASE_TARGET/calendar/be/ 2>&1 > /dev/null


echo [INFO] Setup gateway
mkdir -p $HAM_RELEASE_TARGET/calendar/gateway
cd $CALENDAR_DIR/gateway/target/
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
cp -f $SCRIPT_DIR/templates/standalone/gateway.application.properties $HAM_RELEASE_TARGET/calendar/gateway/application.properties
cp -f $CALENDAR_DIR/gateway/target/gateway-*.jar $HAM_RELEASE_TARGET/calendar/gateway/
echo "#!/bin/bash" > $HAM_RELEASE_TARGET/calendar/gateway/run.sh
echo "java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/gateway/run.sh
echo "call java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/gateway/run.bat

echo [INFO] Setup fe
mkdir -p $HAM_RELEASE_TARGET/calendar/fe
cd $CALENDAR_DIR/fe/target/
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
cp -f $SCRIPT_DIR/templates/standalone/fe.application.properties $HAM_RELEASE_TARGET/calendar/fe/application.properties
cp -f $CALENDAR_DIR/fe/target/fe-*.jar $HAM_RELEASE_TARGET/calendar/fe/
echo "#!/bin/bash" > $HAM_RELEASE_TARGET/calendar/fe/run.sh
echo "java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/fe/run.sh
echo "call java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/fe/run.bat

echo [INFO] Setup be
mkdir -p $HAM_RELEASE_TARGET/calendar/be
cd $CALENDAR_DIR/be/target/
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
cp -f $SCRIPT_DIR/templates/standalone/be.application.properties $HAM_RELEASE_TARGET/calendar/be/application.properties
cp -f $SCRIPT_DIR/templates/standalone/bedb.application.properties $HAM_RELEASE_TARGET/calendar/be/bedb.application.properties
cp -f $SCRIPT_DIR/templates/standalone/bedbham.application.properties $HAM_RELEASE_TARGET/calendar/be/bedbham.application.properties
cp -f $SCRIPT_DIR/templates/standalone/bedbhamnogen.application.properties $HAM_RELEASE_TARGET/calendar/be/bedbhamnogen.application.properties
cp -f $CALENDAR_DIR/be/target/be-*.jar $HAM_RELEASE_TARGET/calendar/be/
echo "#!/bin/bash" > $HAM_RELEASE_TARGET/calendar/be/run.sh
echo "java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/be/run.sh
echo "call java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/be/run.bat

echo [INFO] Setup bemongo
mkdir -p $HAM_RELEASE_TARGET/calendar/bemongo
cd $CALENDAR_DIR/bemongo/target/
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
cp -f $SCRIPT_DIR/templates/standalone/bemongo.application.properties $HAM_RELEASE_TARGET/calendar/bemongo/application.properties
cp -f $CALENDAR_DIR/bemongo/target/bemongo-*.jar $HAM_RELEASE_TARGET/calendar/bemongo/
echo "#!/bin/bash" > $HAM_RELEASE_TARGET/calendar/bemongo/run.sh
echo "java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/bemongo/run.sh
echo "call java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/bemongo/run.bat

# Prepare the compressed file
echo [INFO] Compress release file
cd $ROOT_DIR/release/$HAM_VERSION
tar -zcvf "$ROOT_DIR"/release/ham-samples-"$HAM_VERSION".tar.gz . >> "$ROOT_DIR"/release/ham-samples-"$HAM_VERSION".log 2>&1

# Restore previous dir
cd $START_LOCATION

