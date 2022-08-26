FROM ham.master:latest
RUN echo 3.0.8-SNAPSHOT

COPY ./docker_multi/external.json /etc/app/ham/app/

