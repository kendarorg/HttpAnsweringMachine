#!/bin/bash
dnsServer=`ping -c 4 $DNS_HIJACK_SERVER|head -n 1| grep -Eo "([0-9]+\.?){4}"`
sed -i "s%push \"dhcp-option DNS master.local.self\"%push \"dhcp-option DNS $dnsServer\"%g" /etc/openvpn/openvpn.conf
/usr/local/bin/ovpn_run

/etc/DoSleep.sh