#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"

cd /etc/app/be
java -jar -Dserver.port=80 /etc/app/be/be-1.0-SNAPSHOT.jar