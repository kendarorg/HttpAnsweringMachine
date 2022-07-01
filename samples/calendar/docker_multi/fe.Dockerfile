FROM ham.client:latest
RUN echo 3.0.7-SNAPSHOT


RUN /etc/startservice.sh --app=fe --run=/etc/app/fe/run-fe.sh --capturelogs
COPY ./docker_multi/run-fe.sh /etc/app/fe/
COPY ./docker_multi/application.properties.fe /etc/app/fe/application.properties
COPY ./fe/target/*.jar /etc/app/fe/
RUN chmod +x /etc/app/fe/*.sh
