FROM ham.client:latest
RUN apk update
RUN apk upgrade

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

ADD data/bin /usr/local/bin
ADD data/etc /etc
RUN chmod a+x /usr/local/bin/*

# Add support for OTP authentication using a PAM module
ADD data/otp/openvpn /etc/pam.d/

COPY data/openvpn.conf /etc/openvpn

# Start Openvpn
RUN mkdir -p /etc/service/openvpn \
    && mkdir -p /etc/service/openvpn/log \
    && echo -e "#!/bin/bash\nexec /usr/local/bin/ovpn_run\n" > /etc/service/openvpn/run \
    && chmod +x /etc/service/openvpn/run