FROM alpine:latest
RUN apk update
RUN apk upgrade
RUN apk add --no-cache wget curl bash vim openssl ca-certificates tar cifs-utils

  # Install open JDK
RUN apk add --no-cache openjdk11

  # Install sshd and reset root
RUN apk add --no-cache \
  openssh openrc \
  && ssh-keygen -A \
  && mkdir /root/.ssh \
  && mkdir /run/sshd \
  && chmod 0700 /root/.ssh \
  && echo "root:root" | chpasswd

  # Prepare the first run
RUN mkdir -p /start/logs
RUN mkdir -p /start/services/answering/recording
RUN mkdir -p /start/services/answering/replayerdata
RUN mkdir -p /start/services/answering/jsplugins
RUN mkdir -p /start/services/answering/derbydata

COPY ./start /start
COPY ../../ham/app/target/*.jar /start/services/answering/
COPY ../../ham/libs/*.jar /start/services/answering/libs/

RUN chmod 777 /start/*.sh|true
RUN chmod 777 /start/runonce/*.sh|true
RUN chmod 777 /start/services/*.sh|true

CMD ./start/prestart.sh

