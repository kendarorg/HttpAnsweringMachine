FROM ham.master:latest

COPY ./docker/external.json /etc/service/ham/app

RUN mkdir -p /etc/service/be \
    && mkdir -p /etc/service/be/log \
    && echo "#!/bin/bash\nexec /etc/service/be/be.sh\n" > /etc/service/be/run \
    && chmod +x /etc/service/be/run

COPY docker/be.sh /etc/service/be/
RUN chmod +x /etc/service/be/*.sh

RUN mkdir -p /etc/service/fe \
    && mkdir -p /etc/service/fe/log \
    && echo "#!/bin/bash\nexec /etc/service/bfee/fe.sh\n" > /etc/service/fe/run \
    && chmod +x /etc/service/be/run

COPY docker/fe.sh /etc/service/fe/
RUN chmod +x /etc/service/fe/*.sh
