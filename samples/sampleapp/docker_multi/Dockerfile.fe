FROM ham.client:latest


RUN /etc/startservice.sh --app=fe --run=/etc/app/fe/run-fe.sh --capturelogs
COPY ./docker_multi/run-fe.sh /etc/app/fe/
COPY ./docker_multi/application.properties.fe /etc/app/fe/application.properties
COPY ./fe/target/*.jar /etc/app/fe/
RUN chmod +x /etc/app/fe/*.sh
