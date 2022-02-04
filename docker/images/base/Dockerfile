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
    && mkdir -p /etc/service

# Install sshd
RUN apk add --no-cache \
  openssh openrc \
  && ssh-keygen -A \
  && mkdir -p /root/.ssh \
  && mkdir -p /run/sshd \
  && chmod 0700 /root/.ssh

COPY data/sshd_config /etc/ssh/

RUN mkdir -p /etc/service/sshd \
    && mkdir -p /etc/service/sshd/log \
    && echo -e "#!/bin/bash\nexec /usr/sbin/sshd -D\n" > /etc/service/sshd/run \
    && chmod +x /etc/service/sshd/run

# Force root password
RUN mkdir -p /etc/service/rootforce \
    && mkdir -p /etc/service/rootforce/log \
    && echo -e "#!/bin/bash\necho '"'root:\$ROOT_PWD'"'|chpasswd\n" > /etc/service/rootforce/config \
    && chmod +x /etc/service/rootforce/config

# Start everythin
CMD ["runsvdir", "/etc/service","log"]

