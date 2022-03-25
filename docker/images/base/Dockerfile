FROM alpine:latest
RUN apk update
RUN apk upgrade


# Add basic utils
RUN apk add --no-cache wget curl bash vim openssl tar cifs-utils

# Update certificates
RUN apk add --no-cache ca-certificates
COPY data/ca.crt /usr/local/share/ca-certificates/
RUN chmod 655 /usr/local/share/ca-certificates/ca.crt \
  && update-ca-certificates

# Install open JDK
RUN apk add --no-cache openjdk11

# Prepare runit
RUN apk add --no-cache runit \
    && mkdir -p /etc/service \
    && mkdir -p /etc/app

# Install sshd
RUN apk add --no-cache \
  openssh openrc \
  && ssh-keygen -A \
  && mkdir -p /root/.ssh \
  && mkdir -p /run/sshd \
  && chmod 0700 /root/.ssh

COPY data/sshd_config /etc/ssh/
COPY data/startservice.sh /etc/startservice.sh
RUN chmod +x /etc/startservice.sh

RUN /etc/startservice.sh --app=sshd --capturelogs --run=/usr/sbin/sshd

# Force root password
RUN mkdir -p /etc/service/rootforce \
    && echo -e "#!/bin/bash\nexec 2>&1\necho \"root:\${ROOT_PWD}\"|chpasswd\nsleep infinity\n" > /etc/service/rootforce/run \
    && chmod +x /etc/service/rootforce/run

# Start everythin
CMD ["runsvdir", "/etc/service"]

