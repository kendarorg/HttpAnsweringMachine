#!/bin/bash

dnsServer=`ping -c 4 $DNS_HIJACK_SERVER|head -n 1| grep -Eo "([0-9]+\.?){4}"`


export PROXY_DNS=$dnsServer
/socks5


#java -Dother.dns=127.0.0.11,${DNS_HIJACK_SERVER} \
#  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
#	-Djdk.tls.acknowledgeCloseNotify=true \
#  -jar /etc/app/simpledns/simpledns-1.0-SNAPSHOT.jar