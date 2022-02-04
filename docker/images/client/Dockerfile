FROM ham.base:latest
RUN apk update
RUN apk upgrade

# Start LocalDNS
RUN mkdir -p /etc/service/simpledns \
    && mkdir -p /etc/app/simpledns \
    #&& mkdir -p /etc/service/simpledns/log \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/simpledns/simpledns.sh\n" > /etc/service/simpledns/run \
    && chmod +x /etc/service/simpledns/run

COPY data/*.* /etc/app/simpledns/
RUN chmod +x /etc/app/simpledns/*.sh