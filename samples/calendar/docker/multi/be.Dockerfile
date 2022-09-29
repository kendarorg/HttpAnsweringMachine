FROM ham.client:latest

RUN /etc/startservice.sh --app=be --run=/etc/app/be/be.sh
COPY ./docker/multi/be.sh /etc/app/be/
COPY ./docker/multi/be.application.properties /etc/app/be/application.properties
COPY ./be/target/*.jar /etc/app/be/
RUN chmod +x /etc/app/be/*.sh


