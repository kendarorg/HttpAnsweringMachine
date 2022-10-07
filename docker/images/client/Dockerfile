FROM ham.base

RUN echo ham.client 3.1.1

# Setup localdns
RUN mkdir -p /etc/app/simpledns/
COPY data/*.* /etc/app/simpledns/
COPY data/clientsetup.sh /etc/clientsetup.sh
RUN chmod +x /etc/*.sh \
    && /etc/clientsetup.sh