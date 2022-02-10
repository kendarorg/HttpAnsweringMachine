FROM ham.base:latest
RUN apk update
RUN apk upgrade

# Start LocalDNS
RUN mkdir -p /etc/service/simpledns \
    #&& mkdir -p /etc/app/simpledns/log \
    #&& mkdir -p /etc/service/simpledns/log \
    #&& echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/simpledns/log\n" > /etc/service/simpledns/log/run \
    #&& chmod +x /etc/service/simpledns/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/simpledns/simpledns.sh\n" > /etc/service/simpledns/run \
    && chmod +x /etc/service/simpledns/run

COPY data/*.* /etc/app/simpledns/
RUN chmod +x /etc/app/simpledns/*.sh