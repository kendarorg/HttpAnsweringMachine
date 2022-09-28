#!/bin/sh

# Initialize
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR

# Retrieve the jar name
ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)
rm tmp_txt || true

# Start the application
java  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025 \
		  -jar "$JAR_NAME"