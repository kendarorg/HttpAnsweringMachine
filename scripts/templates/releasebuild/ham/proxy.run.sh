#!/bin/bash

# Initialize
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR

# Retrieve the jar name
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true



# Start the application
java "-Dloader.path=$SCRIPT_DIR/libs"  -Dloader.main=org.kendar.Main  \
	  	"-Djsonconfig=$SCRIPT_DIR/proxy.external.json" \
		  -jar "$JAR_NAME" org.springframework.boot.loader.PropertiesLauncher
