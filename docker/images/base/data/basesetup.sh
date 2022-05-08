#!/bin/sh

# Prepare certificates
chmod 655 /usr/local/share/ca-certificates/ca.crt
update-ca-certificates

# Setup runit
mkdir -p /etc/service
mkdir -p /etc/app
chmod +x /etc/startservice.sh

# Generate ssh keys
ssh-keygen -A
mkdir -p /root/.ssh
mkdir -p /run/sshd
chmod 0700 /root/.ssh

# Start sshd
/etc/startservice.sh --app=sshd --capturelogs --run=/usr/sbin/sshd

# Force root password
mkdir -p /etc/service/rootforce 
echo -e "#"'!'"/bin/bash\nexec 2>&1\necho \"root:\${ROOT_PWD}\"|chpasswd\nsleep infinity\n" > /etc/service/rootforce/run
chmod +x /etc/service/rootforce/run