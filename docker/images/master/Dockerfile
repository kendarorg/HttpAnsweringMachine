FROM ham.base:latest
RUN apk update
RUN apk upgrade

# Start HAM service
RUN mkdir -p /etc/service/ham \
    && mkdir -p /etc/app/ham \
    #&& mkdir -p /etc/service/ham/log \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/ham/ham.sh\n" > /etc/service/ham/run \
    && chmod +x /etc/service/ham/run

COPY data/ham.sh /etc/app/ham/
COPY data/app /etc/service/ham/app/
RUN chmod +x /etc/app/ham/*.sh