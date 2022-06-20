#!/bin/sh

echo "namserver 8.8.8.8" > /etc/resolv.conf
echo "namserver 8.8.4.4" >> /etc/resolv.conf
echo "namserver 127.0.0.11" >> /etc/resolv.conf
echo "options ndots:0" >> /etc/resolv.conf