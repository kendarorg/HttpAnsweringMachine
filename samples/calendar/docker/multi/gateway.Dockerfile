FROM ham.client:latest

RUN /etc/startservice.sh --app=gateway --run=/etc/app/gateway/gateway.sh
COPY ./docker/multi/gateway.sh /etc/app/gateway/
COPY ./docker/multi/gateway.application.properties /etc/app/gateway/application.properties
COPY ./gateway/target/*.jar /etc/app/gateway/
RUN chmod +x /etc/app/gateway/*.sh

