#FROM golang:alpine as builder
FROM alpine:latest as builder

ARG OPENFORTIVPN_VERSION=v1.17.1
#ARG GLIDER_VERSION=v0.9.2

#https://github.com/adrienverge/openfortivpn/archive/refs/tags/v1.17.1.tar.gz
RUN \
  apk add --no-cache \
    autoconf automake build-base ca-certificates curl git openssl-dev ppp tar && \
  #update-ca-certificates && \
  # build openfortivpn
  mkdir -p /usr/src/openfortivpn && \
  curl -sL https://github.com/adrienverge/openfortivpn/archive/refs/tags/${OPENFORTIVPN_VERSION}.tar.gz \
  | tar xz -C /usr/src/openfortivpn --strip-components=1 
 RUN  cd /usr/src/openfortivpn && \
  ./autogen.sh && \
  ./configure --prefix=/usr --sysconfdir=/etc && \
  make -j$(nproc) && \
  make install

#RUN \
  # build glider
#  mkdir -p /go/src/github.com/nadoo/glider && \
#  curl -sL https://github.com/nadoo/glider/archive/${GLIDER_VERSION}.tar.gz \
#    | tar xz -C /go/src/github.com/nadoo/glider --strip-components=1 && \
#  cd /go/src/github.com/nadoo/glider && \
#  go get -v ./...

#docker run --privileged --dns 8.8.8.8  --dns 10.136.49.21 --dns 10.110.49.21 --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH --name myvpn myvpn
#docker run --privileged --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH --name myvpn myvpn
FROM ham.base:latest

RUN echo openforti 4.1.1

COPY --from=builder /usr/bin/openfortivpn  /usr/bin/
#/go/bin/glider

# Block timezone request prompt
ENV DEBIAN_FRONTEND="noninteractive" TZ="Europe/Rome"

RUN apk add --no-cache  bash  iptables unzip ppp

# Update iptables
RUN echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf
#RUN update-alternatives --set iptables /usr/sbin/iptables-legacy

    
#COPY data/sshd_config /etc/ssh/
#RUN mkdir /etc/app/setuproute
COPY data/setuproute.sh /etc/app/setuproute/
RUN chmod +x /etc/app/setuproute/*


#COPY data/sshd_config /etc/ssh/
#COPY data/startservice.sh /etc/startservice.sh
#RUN chmod +x /etc/startservice.sh

# Setup the sshd
#RUN /etc/startservice.sh --app=sshd --capturelogs --run=/usr/sbin/sshd


# Setup the route
#RUN /etc/startservice.sh --app=setuproute --config --run=/etc/app/setuproute/setuproute.sh
    
#MUST RUN mknod /dev/ppp c 108 0
#openfortivpn connect-nexus2.lastminutegroup.com:443 -u edaros --set-dns=0 --pppd-use-peerdns=1
# Setup the basic dns servers
#RUN /etc/startservice.sh --app=dns --config --run=/etc/app/dns/dns.sh
#COPY data/dns.sh /etc/app/dns/dns.sh
#RUN chmod +x /etc/app/dns/dns.sh
    
# Force root password
#RUN mkdir -p /etc/service/rootforce \
#    && echo "#!/bin/bash" > /etc/service/rootforce/run \
#    && echo "exec 2>&1" >> /etc/service/rootforce/run \
#    && echo 'echo "root:\${ROOT_PWD}"|chpasswd' >> /etc/service/rootforce/run \
#    && echo "sleep infinity" >> /etc/service/rootforce/run \
#    && chmod +x /etc/service/rootforce/run 

# Start everythin
CMD ["runsvdir", "/etc/service"]

