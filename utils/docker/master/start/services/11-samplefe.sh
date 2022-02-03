#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"

cd /start/services/samplefe
#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005 -Dother.dns=127.0.0.11 \
#	"-Dloader.path=/start/services/answering/libs" -Dloader.main=org.kendar.Main -jar app-1.0-SNAPSHOT.jar \
#	 org.springframework.boot.loader.PropertiesLauncher &


java -jar fe-1.0-SNAPSHOT.jar &
	 
	 
#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005 -Dother.dns=127.0.0.11 \
#	-cp app-1.0-SNAPSHOT.jar:/start/services/answering/libs -Dloader.main=org.kendar.Main \
#	 org.springframework.boot.loader.PropertiesLauncher &
	 
export lastPid=$!
exit $lastPid