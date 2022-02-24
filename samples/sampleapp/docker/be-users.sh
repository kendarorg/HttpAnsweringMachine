#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"

java -jar /etc/app/be-users/be-users-1.0-SNAPSHOT.jar