FROM ham.client:latest

RUN mkdir -p /etc/app/be \
    && mkdir -p /etc/service/be \
    #&& mkdir -p /etc/app/be/log \
    #&& mkdir -p /etc/service/be/log \
    #&& echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/be/log\n" > /etc/service/be/log/run \
    #&& chmod +x /etc/service/be/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/be/be.sh\n" > /etc/service/be/run \
    && chmod +x /etc/service/be/run

COPY ./docker_multi/be.sh /etc/app/be/
COPY ./docker_multi/be.application.properties /etc/app/be/application.properties
COPY ./be/target/*.jar /etc/app/be/
RUN chmod +x /etc/app/be/*.sh


