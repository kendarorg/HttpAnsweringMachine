#!/bin/sh

# Prepare certificates
chmod 655 /usr/local/share/ca-certificates/ca.crt
update-ca-certificates

# Setup runit
mkdir -p /etc/service
mkdir -p /etc/app
chmod +x /etc/startservice.sh

ROOT_PWD="${ROOT_PWD:-root}"

# Generate ssh keys
ssh-keygen -A
mkdir -p /root/.ssh
mkdir -p /run/sshd
chmod 0700 /root/.ssh

# Create a "nice" sleep
cd /etc/
${JAVA11_HOME}/bin/javac DoSleep.java
echo "#"'!'"/bin/bash
cd /etc
${JAVA11_HOME}/bin/java DoSleep" > /etc/DoSleep.sh


chmod +x /etc/DoSleep.sh

# Start sshd
/etc/startservice.sh --app=sshd --capturelogs --run=/usr/sbin/sshd

# Force root password
mkdir -p /etc/service/rootforce 
echo "#"'!'"/bin/bash
exec 2>&1
echo \"root:\${ROOT_PWD}\"|chpasswd
/etc/DoSleep.sh" > /etc/service/rootforce/run
chmod +x /etc/service/rootforce/run