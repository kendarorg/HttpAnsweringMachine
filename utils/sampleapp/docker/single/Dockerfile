FROM ham.master:latest

COPY ./docker/single/external.json /etc/app/ham/app/

RUN mkdir -p /etc/app/be \
    && mkdir -p /etc/app/be/log \
    && mkdir -p /etc/service/be/log \
    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/service/be/log\n" > /etc/service/be/log/run \
    && chmod +x /etc/service/be/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/be/be.sh\n" > /etc/service/be/run \
    && chmod +x /etc/service/be/run

COPY ./docker/single/be.sh /etc/app/be/
COPY ./be/target/*.jar /etc/app/be/
RUN chmod +x /etc/app/be/*.sh

RUN mkdir -p /etc/app/fe \
    && mkdir -p /etc/app/fe/log \
    && mkdir -p /etc/service/fe/log \
    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/service/fe/log\n" > /etc/service/fe/log/run \
    && chmod +x /etc/service/fe/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/fe/fe.sh\n" > /etc/service/fe/run \
    && chmod +x /etc/service/fe/run

COPY ./docker/single/fe.sh /etc/app/fe/
COPY ./fe/target/*.jar /etc/app/fe/
RUN chmod +x /etc/app/fe/*.sh
