FROM ham.master:latest

COPY ./docker/external.json /etc/app/ham/app/

RUN mkdir -p /etc/app/be \
    && mkdir -p /etc/service/be \
    && mkdir -p /etc/app/be/log \
    && mkdir -p /etc/service/be/log \
    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/be/log\n" > /etc/service/be/log/run \
    && chmod +x /etc/service/be/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/be/be.sh\n" > /etc/service/be/run \
    && chmod +x /etc/service/be/run

COPY ./docker/be.sh /etc/app/be/
COPY ./be/target/*.jar /etc/app/be/
RUN chmod +x /etc/app/be/*.sh


RUN mkdir -p /etc/app/be-users \
    && mkdir -p /etc/service/be-users \
    && mkdir -p /etc/app/be-users/log \
    && mkdir -p /etc/service/be-users/log \
    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/be-users/log\n" > /etc/service/be-users/log/run \
    && chmod +x /etc/service/be-users/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/be-users/be-users.sh\n" > /etc/service/be-users/run \
    && chmod +x /etc/service/be-users/run

COPY ./docker/be-users.sh /etc/app/be-users/
COPY ./docker/be.application.properties /etc/app/be-users/application.properties
COPY ./be-users/target/*.jar /etc/app/be-users/
RUN chmod +x /etc/app/be-users/*.sh



RUN mkdir -p /etc/app/fe \
    && mkdir -p /etc/service/fe \
    && mkdir -p /etc/app/fe/log \
    && mkdir -p /etc/service/fe/log \
    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/fe/log\n" > /etc/service/fe/log/run \
    && chmod +x /etc/service/fe/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/fe/fe.sh\n" > /etc/service/fe/run \
    && chmod +x /etc/service/fe/run

COPY ./docker/fe.sh /etc/app/fe/
COPY ./docker/fe.application.properties /etc/app/fe/application.properties
COPY ./fe/target/*.jar /etc/app/fe/
RUN chmod +x /etc/app/fe/*.sh
