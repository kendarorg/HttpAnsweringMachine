FROM ham.base:latest
RUN apk update
RUN apk upgrade

# Start LocalDNS
RUN mkdir -p /etc/service/simpledns \
    && mkdir -p /etc/service/simpledns/log \
    && echo "#!/bin/bash\nexec /etc/service/simpledns/simpledns.sh\n" > /etc/service/simpledns/run \
    && chmod +x /etc/service/simpledns/run

COPY ./data/simpledns.sh /etc/service/simpledns/
RUN chmod +x /etc/service/simpledns/*.sh