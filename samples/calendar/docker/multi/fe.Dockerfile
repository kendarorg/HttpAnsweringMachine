FROM ham.client:latest


RUN /etc/startservice.sh --app=fe --run=/etc/app/fe/fe.sh --capturelogs
COPY ./docker/multi/fe.sh /etc/app/fe/
COPY ./docker/multi/fe.application.properties /etc/app/fe/application.properties
COPY ./fe/target/*.jar /etc/app/fe/
RUN chmod +x /etc/app/fe/*.sh
