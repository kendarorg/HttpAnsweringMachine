FROM ham.master:latest
RUN echo 4.0.2-SNAPSHOT

COPY ./docker/multi/calendar.multi.external.json /etc/app/ham/app/external.json

