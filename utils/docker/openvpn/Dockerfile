# Original credit: https://github.com/jpetazzo/dockvpn

FROM alpine:latest
RUN apk update
RUN apk upgrade
RUN apk add --no-cache wget curl bash vim openssl ca-certificates tar cifs-utils

# Install open JDK
RUN apk add --no-cache openjdk11

# Testing: pamtester
RUN echo "http://dl-cdn.alpinelinux.org/alpine/edge/testing/" >> /etc/apk/repositories && \
    apk add --update openvpn iptables bash easy-rsa openvpn-auth-pam google-authenticator pamtester libqrencode && \
    ln -s /usr/share/easy-rsa/easyrsa /usr/local/bin && \
    rm -rf /tmp/* /var/tmp/* /var/cache/apk/* /var/cache/distfiles/*

# Needed by scripts
ENV OPENVPN=/etc/openvpn
ENV EASYRSA=/usr/share/easy-rsa \
    EASYRSA_CRL_DAYS=3650 \
    EASYRSA_PKI=$OPENVPN/pki
    


VOLUME ["/etc/openvpn"]

# Internally uses port 1194/udp, remap using `docker run -p 443:1194/tcp`
EXPOSE 1194/udp


ADD ./bin /usr/local/bin
ADD ./etc /etc
RUN chmod a+x /usr/local/bin/*
RUN chmod a+x /usr/local/bin/*

# Add support for OTP authentication using a PAM module
ADD ./otp/openvpn /etc/pam.d/

RUN mkdir -p /start/logs
RUN mkdir -p /start/runonce
RUN mkdir -p /start/services


# Install sshd and reset root
RUN apk add --no-cache \ 
        openssh openrc \
    && ssh-keygen -A \
    && mkdir /root/.ssh \
    && chmod 0700 /root/.ssh \
    && echo "root:root" | chpasswd

COPY ./start /start

ENV DNS_HIJACK_SERVER "$DNS_HIJACK_SERVER"

RUN chmod 777 /start/*.sh|true
RUN chmod 777 /start/runonce/*.sh|true
RUN chmod 777 /start/services/*.sh|true

CMD ./start/startup.sh
#CMD top

