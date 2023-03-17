#!/bin/sh

echo "namserver 127.0.0.1" > /etc/resolv.conf
echo "namserver 127.0.0.11" >> /etc/resolv.conf
echo "options ndots:0" >> /etc/resolv.conf

