#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"

java -Dother.dns=127.0.0.11,${DNS_HIJACK_SERVER} -jar /etc/app/simpledns/simpledns-1.0-SNAPSHOT.jar