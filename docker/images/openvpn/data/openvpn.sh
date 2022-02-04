#!/bin/bash
dnsServer=`ping  $DNS_HIJACK_SERVER| grep -Eo "([0-9]+\.?){4}"|head -n 1`
sed -i 's/push "dhcp-option DNS master.local.self"/push "dhcp-option DNS $dnsServer"/g' /etc/openvpn/openvpn.conf
/usr/local/bin/ovpn_run

sleep infinite