FROM ham.base:latest
RUN apk update
RUN apk upgrade

COPY data/*.* /etc/app/simpledns/
RUN chmod +x /etc/app/simpledns/*.sh

# Start LocalDNS
RUN /etc/startservice.sh --app=simpledns --capturelogs --run=/etc/app/simpledns/simpledns.sh