FROM ham.base

RUN echo ham.client 4.1.2

# Setup localdns
RUN mkdir -p /etc/app/simpledns/
COPY data/*.* /etc/app/simpledns/
COPY data/clientsetup.sh /etc/clientsetup.sh
RUN chmod +x /etc/*.sh \
    && /etc/clientsetup.sh