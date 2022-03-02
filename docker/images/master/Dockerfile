FROM ham.base:latest
RUN apk update
RUN apk upgrade

# Start HAM service
RUN mkdir -p /etc/service/ham \
    && mkdir -p /etc/app/ham \
    #&& mkdir -p /etc/app/ham/log \
    #&& mkdir -p /etc/service/ham/log \
    #&& echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/ham/log\n" > /etc/service/ham/log/run \
    #&& chmod +x /etc/service/ham/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/ham/ham.sh\n" > /etc/service/ham/run \
    && chmod +x /etc/service/ham/run

COPY data/ham.sh /etc/app/ham/
COPY data/app /etc/app/ham/app/
RUN chmod +x /etc/app/ham/*.sh