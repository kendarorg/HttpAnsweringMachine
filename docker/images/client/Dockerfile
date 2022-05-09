FROM ham.base:latest

ENV RESOL_CONF /etc/resolv.conf

# Setup localdns
RUN mkdir -p /etc/app/simpledns/
COPY data/*.* /etc/app/simpledns/
COPY data/clientsetup.sh /etc/clientsetup.sh
RUN chmod +x /etc/*.sh \
    && /etc/clientsetup.sh