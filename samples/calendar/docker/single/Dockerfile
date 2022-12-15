FROM ham.master:latest


RUN echo 4.0.2-SNAPSHOT

COPY ./docker/single/calendar.single.external.json /etc/app/ham/app/external.json

RUN /etc/startservice.sh --app=be --run=/etc/app/be/be.sh --capturelogs
COPY ./docker/single/be.sh /etc/app/be/
COPY ./docker/single/be.application.properties /etc/app/be/application.properties
COPY ./be/target/*.jar /etc/app/be/
RUN chmod +x /etc/app/be/*.sh


RUN /etc/startservice.sh --app=gateway --run=/etc/app/gateway/gateway.sh --capturelogs
COPY ./docker/single/gateway.sh /etc/app/gateway/
COPY ./docker/single/gateway.application.properties /etc/app/gateway/application.properties
COPY ./gateway/target/*.jar /etc/app/gateway/
RUN chmod +x /etc/app/gateway/*.sh


RUN /etc/startservice.sh --app=fe --run=/etc/app/fe/fe.sh --capturelogs
COPY ./docker/single/fe.sh /etc/app/fe/
COPY ./docker/single/fe.application.properties /etc/app/fe/application.properties
COPY ./fe/target/*.jar /etc/app/fe/
RUN chmod +x /etc/app/fe/*.sh
