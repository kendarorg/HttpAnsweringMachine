#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"
export CLASSPATH=/etc/app/ham/app/libs

cd /etc/app/ham/app
#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005 -Dother.dns=127.0.0.11 \
#	"-Dloader.path=/etc/app/ham/app/libs" -Dloader.main=org.kendar.Main -jar app-2.1.3.jar \
#	 org.springframework.boot.loader.PropertiesLauncher

ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)

java "-Dloader.path=/etc/app/ham/app/libs/"  -Dloader.main=org.kendar.Main  \
  -jar "$JAR_NAME" org.springframework.boot.loader.PropertiesLauncher
	
#-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \


#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005 -Dother.dns=127.0.0.11 \
#	-cp app-2.1.3.jar:/etc/app/ham/app/libs -Dloader.main=org.kendar.Main \
#	 org.springframework.boot.loader.PropertiesLauncher
