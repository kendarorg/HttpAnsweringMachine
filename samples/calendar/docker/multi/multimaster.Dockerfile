FROM ham.master:latest
RUN echo 3.1.1

COPY ./docker/multi/calendar.multi.external.json /etc/app/ham/app/external.json

