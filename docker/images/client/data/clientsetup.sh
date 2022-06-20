#!/bin/sh

# Start simple dns client
chmod +x /etc/app/simpledns/*.sh
/etc/startservice.sh --app=simpledns --capturelogs --run=/etc/app/simpledns/simpledns.sh