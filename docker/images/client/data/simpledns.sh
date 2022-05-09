#!/bin/bash

export JAVA_HOME=${JAVA11_HOME}/
export PATH="${JAVA_HOME}/bin:${PATH}"

dnsServer=`ping -c 4 $DNS_HIJACK_SERVER|head -n 1| grep -Eo "([0-9]+\.?){4}"`


echo "namserver 127.0.0.1" > $RESOL_CONF
echo "namserver 127.0.0.11" >> $RESOL_CONF
echo "options ndots:0" >> $RESOL_CONF
cd /etc/app/simpledns/

#POSTRESOLVCONF

ls -lA | awk -F':[0-9]* ' '/:/{print $2}'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)

java -Dother.dns=${dnsServer} \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5015 \
  -jar /etc/app/simpledns/"$JAR_NAME"

#java -Dother.dns=127.0.0.11,${DNS_HIJACK_SERVER} \
#  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
#	-Djdk.tls.acknowledgeCloseNotify=true \
#  -jar /etc/app/simpledns/simpledns-2.1.3.jar