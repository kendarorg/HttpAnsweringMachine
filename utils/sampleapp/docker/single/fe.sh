#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"

java -jar /etc/app/fe/fe-1.0-SNAPSHOT.jar