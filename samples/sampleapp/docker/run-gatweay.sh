#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"

cd /etc/app/gateway/

ls -lA | awk -F':[0-9]* ' '/:/{print $2}'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)

java -jar /etc/app/gateway/"$JAR_NAME"