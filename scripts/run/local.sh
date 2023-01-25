#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo This will run the local ham
echo Target version: $HAM_VERSION

pause


ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )
export HAM_DIR=$ROOT_DIR/ham/app/target
export HAM_LIBS_DIR=$ROOT_DIR/ham/libs

# Retrieve the jar name
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true

if [ -z ${JSON_CONFIG+x} ]; export JSON_CONFIG=$ROOT_DIR/ham/external.json;

export DEBUG_AGENT=
is_set DO_DEBUG ; export DEBUG_AGENT=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025
# "$DEBUG_AGENT"

# Start the application
java "-Dloader.path=$HAM_LIBS_DIR"  -Dloader.main=org.kendar.Main  \
	  	"$DEBUG_AGENT" \
	  	"-Djsonconfig=$JSON_CONFIG" \
		  -jar "$HAM_DIR/$JAR_NAME" org.springframework.boot.loader.PropertiesLauncher