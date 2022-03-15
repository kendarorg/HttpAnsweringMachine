FROM ham.client:latest

RUN /etc/startservice.sh --app=gateway --run=/etc/app/gateway/run-gateway.sh
COPY ./docker_multi/run-gateway.sh /etc/app/gateway/
COPY ./docker_multi/application.properties.gateway /etc/app/gateway/application.properties
COPY ./gateway/target/*.jar /etc/app/gateway/
RUN chmod +x /etc/app/gateway/*.sh

