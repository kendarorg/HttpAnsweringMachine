FROM ham.master:latest
RUN echo 3.0.7-SNAPSHOT

COPY ./docker_multi/external.json /etc/app/ham/app/
RUN mkdir -p /etc/app/ham/app/jsplugins
COPY ./docker_multi/master/Nasdaq.json /etc/app/ham/app/jsplugins/

