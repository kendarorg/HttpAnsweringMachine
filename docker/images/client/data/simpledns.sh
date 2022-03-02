#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"
dnsServer=`ping -c 4 $DNS_HIJACK_SERVER|head -n 1| grep -Eo "([0-9]+\.?){4}"`


echo "namserver 127.0.0.1" > /etc/resolv.conf
echo "namserver 127.0.0.11" >> /etc/resolv.conf
echo "options ndots:0" >> /etc/resolv.conf

java -Dother.dns=${dnsServer} \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5015 \
  -jar /etc/app/simpledns/simpledns-1.0-SNAPSHOT.jar


#java -Dother.dns=127.0.0.11,${DNS_HIJACK_SERVER} \
#  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
#	-Djdk.tls.acknowledgeCloseNotify=true \
#  -jar /etc/app/simpledns/simpledns-1.0-SNAPSHOT.jar