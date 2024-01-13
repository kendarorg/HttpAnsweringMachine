#!/bin/sh

# Prepare certificates
chmod 655 /usr/local/share/ca-certificates/ca.crt
update-ca-certificates

ROOT_PWD="${ROOT_PWD:-root}"

# Force root password
mkdir -p /etc/service/rootforce 
echo "#"'!'"/bin/bash
exec 2>&1
echo \"root:\${ROOT_PWD}\"|chpasswd
/etc/DoSleep.sh" > /etc/service/rootforce/run
chmod +x /etc/service/rootforce/run