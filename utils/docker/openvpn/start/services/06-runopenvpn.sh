#!/bin/bash
#route add -net 10.0.0.0 netmask 255.0.0.0 gw 192.168.1.4

cd /usr/local/bin
./ovpn_run &
export lastPid=$!
exit $lastPid


