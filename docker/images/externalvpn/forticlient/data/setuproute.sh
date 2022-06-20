#!/bin/bash

# Flush all the rules in filter and nat tables
iptables --flush                         
iptables --table nat --flush

# Set up IP FORWARDing and Masquerading
iptables --table nat --append POSTROUTING --out-interface ppp0 -j MASQUERADE
# Assuming one NIC to local LAN
iptables --append FORWARD --in-interface eth0 -j ACCEPT         
