#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo This will build a tar.gz with the sample applications. Ctrl+C to exit
echo Target version: $HAM_VERSION

pause

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

# Setup the target directory
echo Setup target dir
HAM_RELEASE_TARGET=$ROOT_DIR/release/$HAM_VERSION
rm -rf $HAM_RELEASE_TARGET || true

QUOTES_DIR=$ROOT_DIR/samples/quotes
mkdir -p $HAM_RELEASE_TARGET/quotes
cp -R $QUOTES_DIR/core $HAM_RELEASE_TARGET/quotes/

CALENDAR_DIR=$ROOT_DIR/samples/calendar
cd $CALENDAR_DIR
mvn clean install

echo Setup runner
cp -f $SCRIPT_DIR/templates/releasebuild/samples/*.* $HAM_RELEASE_TARGET/ 1>NUL


copy /y $SCRIPT_DIR/templates/releasebuild/samples/calendar/*.* $HAM_RELEASE_TARGET/calendar 1>NUL
copy /y $SCRIPT_DIR/templates/standalone/calendar.external.json $HAM_RELEASE_TARGET/calendar 1>NUL

echo Setup gateway
mkdir -p $HAM_RELEASE_TARGET/calendar/gateway
cd $CALENDAR_DIR/gateway/target/
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
cp -f $SCRIPT_DIR/templates/standalone/gateway.application.properties $HAM_RELEASE_TARGET/calendar/gateway/application.properties
cp -f $CALENDAR_DIR/gateway/target/gateway-*.jar $HAM_RELEASE_TARGET/calendar/gateway/
echo "#!/bin/bash" > $HAM_RELEASE_TARGET/calendar/gateway/run.sh
echo "java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/gateway/run.sh
echo "call java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/gateway/run.bat

echo Setup fe
mkdir -p $HAM_RELEASE_TARGET/calendar/fe
cd $CALENDAR_DIR/fe/target/
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
cp -f $SCRIPT_DIR/templates/standalone/fe.application.properties $HAM_RELEASE_TARGET/calendar/fe/application.properties
cp -f $CALENDAR_DIR/fe/target/fe-*.jar $HAM_RELEASE_TARGET/calendar/fe/
echo "#!/bin/bash" > $HAM_RELEASE_TARGET/calendar/fe/run.sh
echo "java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/fe/run.sh
echo "call java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/fe/run.bat

echo Setup be
mkdir -p $HAM_RELEASE_TARGET/calendar/be
cd $CALENDAR_DIR/be/target/
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
cp -f $SCRIPT_DIR/templates/standalone/be.application.properties $HAM_RELEASE_TARGET/calendar/be/application.properties
cp -f $CALENDAR_DIR/be/target/be-*.jar $HAM_RELEASE_TARGET/calendar/be/
echo "#!/bin/bash" > $HAM_RELEASE_TARGET/calendar/be/run.sh
echo "java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/be/run.sh
echo "call java -jar $JAR_NAME" >> $HAM_RELEASE_TARGET/calendar/be/run.bat

# Prepare the compressed file
echo Compress release file
cd $ROOT_DIR/release/$HAM_VERSION
tar -zcvf "$ROOT_DIR"/release/ham-samples-"$HAM_VERSION".tar.gz . >> "$ROOT_DIR"/release/ham-samples-"$HAM_VERSION".log 2>1

# Cleanup
echo Cleanup
rm -rf $HAM_RELEASE_TARGET || true

# Restore previous dir
cd $START_LOCATION
