FROM ham.client:latest as builder

FROM php:8.0-apache

ENV DEBIAN_FRONTEND noninteractive
ENV JAVA11_HOME /usr/lib/jvm/java-11-openjdk-amd64
ENV RESOL_CONF /etc/resolvconf/resolv.conf.d/base

# https://kifarunix.com/make-permanent-dns-changes-on-resolv-conf-in-linux/
RUN apt update \
    && apt upgrade -y \
    && apt install wget curl bash openssl tar ca-certificates openjdk-11-jdk runit openssh-server iputils-ping apt-utils apt-transport-https -y \
    && mkdir -p /etc/app/simpledns \
    && mkdir -p etc/ssh

RUN apt-get install -y apt-utils debconf-utils dialog
RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections
RUN echo "resolvconf resolvconf/linkify-resolvconf boolean false" | debconf-set-selections
RUN apt-get update
RUN apt-get install -y resolvconf

RUN apt install vim bind9-dnsutils  -y

# Update certificates
COPY --from=builder /usr/local/share/ca-certificates/ca.crt /usr/local/share/ca-certificates/ca.crt
COPY --from=builder /etc/ssh/sshd_config /etc/ssh/sshd_config
COPY --from=builder /etc/*.sh /etc/
COPY --from=builder /etc/DoSleep.* /etc/
COPY --from=builder /etc/app/simpledns/simpledns*.* /etc/app/simpledns/

RUN sed -i "s%#POSTRESOLVCONF%resolvconf -u%g" $RESOL_CONF


COPY core/ /var/www/html/

# Prepare base setup and add apache to run
RUN chmod 777 /etc/*.sh \
    && /etc/basesetup.sh \
    && /etc/clientsetup.sh \
    && /etc/startservice.sh --app=apache --run=/usr/local/bin/apache2-foreground

# Start everything
CMD ["runsvdir", "/etc/service"]