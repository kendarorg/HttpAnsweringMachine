FROM ham.base

RUN echo 3.0.7-SNAPSHOT

# Setup localdns
RUN mkdir -p /etc/app/simpledns/
COPY data/*.* /etc/app/simpledns/
COPY data/clientsetup.sh /etc/clientsetup.sh
RUN chmod +x /etc/*.sh \
    && /etc/clientsetup.sh