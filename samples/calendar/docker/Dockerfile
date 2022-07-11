FROM ham.master:latest


RUN echo 3.0.7-SNAPSHOT

COPY ./docker/external.json /etc/app/ham/app/

RUN /etc/startservice.sh --app=be --run=/etc/app/be/run-be.sh --capturelogs
COPY ./docker/run-be.sh /etc/app/be/
COPY ./docker/application.properties.be /etc/app/be/application.properties
COPY ./be/target/*.jar /etc/app/be/
RUN chmod +x /etc/app/be/*.sh


RUN /etc/startservice.sh --app=gateway --run=/etc/app/gateway/run-gateway.sh --capturelogs
COPY ./docker/run-gateway.sh /etc/app/gateway/
COPY ./docker/application.properties.gateway /etc/app/gateway/application.properties
COPY ./gateway/target/*.jar /etc/app/gateway/
RUN chmod +x /etc/app/gateway/*.sh


RUN /etc/startservice.sh --app=fe --run=/etc/app/fe/run-fe.sh --capturelogs
COPY ./docker/run-fe.sh /etc/app/fe/
COPY ./docker/application.properties.fe /etc/app/fe/application.properties
COPY ./fe/target/*.jar /etc/app/fe/
RUN chmod +x /etc/app/fe/*.sh
