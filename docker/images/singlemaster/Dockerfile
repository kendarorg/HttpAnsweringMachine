FROM ham.master

RUN echo 3.0.7-SNAPSHOT

COPY data/*.* /etc/app/dns/
RUN chmod +x /etc/app/dns/*.sh

# Setup DNS
RUN /etc/startservice.sh --app=dns --capturelogs --config --run=/etc/app/dns/dns.sh