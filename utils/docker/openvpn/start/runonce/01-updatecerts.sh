#!/bin/sh

cd /

if [ ! -f /usr/local/share/ca-certificates/ca.crt ]; then
    touch /usr/local/share/ca-certificates/ca.crt
fi

if ! cmp /start/conf/ca.crt /usr/local/share/ca-certificates/ca.crt >/dev/null 2>&1
then
  cp /start/conf/ca.crt /usr/local/share/ca-certificates/
  chmod 655 /usr/local/share/ca-certificates/ca.crt
  update-ca-certificates
fi

chmod 777 *.sh
chmod -R 777 /start/services|true
chmod -R 777 /start/runonce|true