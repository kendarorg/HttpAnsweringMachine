#!/bin/sh

# Initialize
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR

# Retrieve the jar name
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true

function is_set() { [[ $(eval echo "\${${1}+x}") ]]; }

export DEBUG_AGENT=
is_set DO_DEBUG ; export DEBUG_AGENT=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025
# "$DEBUG_AGENT"

# Start the application
java "-Dloader.path=$SCRIPT_DIR/libs"  -Dloader.main=org.kendar.Main  \
	  	"$DEBUG_AGENT" \
	  	"-Djsonconfig=$SCRIPT_DIR/test.external.json" \
		  -jar "$JAR_NAME" org.springframework.boot.loader.PropertiesLauncher
