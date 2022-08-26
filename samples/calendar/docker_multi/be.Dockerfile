FROM ham.client:latest
RUN echo 3.0.8-SNAPSHOT

RUN /etc/startservice.sh --app=be --run=/etc/app/be/run-be.sh
COPY ./docker_multi/run-be.sh /etc/app/be/
COPY ./docker_multi/application.properties.be /etc/app/be/application.properties
COPY ./be/target/*.jar /etc/app/be/
RUN chmod +x /etc/app/be/*.sh


