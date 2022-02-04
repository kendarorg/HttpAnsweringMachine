FROM ham.base:latest
RUN apk update
RUN apk upgrade

# Start HAM service
RUN mkdir -p /etc/service/ham \
    && mkdir -p /etc/service/ham/app \
    && mkdir -p /etc/service/ham/log \
    && echo -e "#!/bin/bash\nexec /etc/service/ham/ham.sh\n" > /etc/service/ham/run \
    && chmod +x /etc/service/ham/run

COPY data/ham.sh /etc/service/ham/
COPY data/app /etc/service/ham/app/
RUN chmod +x /etc/service/ham/*.sh