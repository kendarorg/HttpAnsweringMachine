#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"
export CLASSPATH=/etc/app/ham/app/libs

cd /etc/app/ham/app
#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005 -Dother.dns=127.0.0.11 \
#	"-Dloader.path=/etc/app/ham/app/libs" -Dloader.main=org.kendar.Main -jar app-1.0-SNAPSHOT.jar \
#	 org.springframework.boot.loader.PropertiesLauncher


java "-Dloader.path=/etc/app/ham/app/libs/"  -Dloader.main=org.kendar.Main  \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
	-jar app-1.0-SNAPSHOT.jar org.springframework.boot.loader.PropertiesLauncher
	
#-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \


#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005 -Dother.dns=127.0.0.11 \
#	-cp app-1.0-SNAPSHOT.jar:/etc/app/ham/app/libs -Dloader.main=org.kendar.Main \
#	 org.springframework.boot.loader.PropertiesLauncher
