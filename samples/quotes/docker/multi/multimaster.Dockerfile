FROM ham.master:latest

RUN mkdir -p /etc/app/ham/app/jsplugins
COPY ./docker/multi/quotes.multi.external.json /etc/app/ham/app/
COPY ./docker/multi/master/Nasdaq.json /etc/app/ham/app/jsplugins/

