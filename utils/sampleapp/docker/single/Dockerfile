FROM ham.master:latest

COPY ./docker/single/external.json /etc/app/ham/app/

RUN mkdir -p /etc/app/be \
    && mkdir -p /etc/service/be \
    #&& mkdir -p /etc/app/be/log \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/be/be.sh\n" > /etc/service/be/run \
    && chmod +x /etc/service/be/run

COPY ./docker/single/be.sh /etc/app/be/
COPY ./be/target/*.jar /etc/app/be/
RUN chmod +x /etc/app/be/*.sh

RUN mkdir -p /etc/app/fe \
    && mkdir -p /etc/service/fe \
    #&& mkdir -p /etc/app/fe/log \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/fe/fe.sh\n" > /etc/service/fe/run \
    && chmod +x /etc/service/fe/run

COPY ./docker/single/fe.sh /etc/app/fe/
COPY ./fe/target/*.jar /etc/app/fe/
RUN chmod +x /etc/app/fe/*.sh
