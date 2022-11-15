FROM ham.master:latest
RUN echo 4.0.1

COPY ./docker/multi/calendar.multi.external.json /etc/app/ham/app/external.json

