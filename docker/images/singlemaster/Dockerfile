FROM ham.master

RUN echo ham.singlemaster version-4.2.1

COPY data/*.* /etc/app/dns/
RUN chmod +x /etc/app/dns/*.sh

# Setup DNS
RUN /etc/startservice.sh --app=dns --capturelogs --config --run=/etc/app/dns/dns.sh