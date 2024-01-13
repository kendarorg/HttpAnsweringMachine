FROM alpine:latest
RUN apk update
RUN apk upgrade

RUN echo ham.simple.client version-4.3.0

RUN apk add --no-cache bash ca-certificates

# Load certificates
COPY data/ca.crt /usr/local/share/ca-certificates/
COPY data/basesetup.sh /etc/basesetup.sh

# Prepare certificates, runit space, generate ssh keys, setup ssh service, force root password
RUN chmod 777 /etc/basesetup.sh
RUN  /etc/basesetup.sh
RUN mkdir /app

CMD "/app/start.sh"