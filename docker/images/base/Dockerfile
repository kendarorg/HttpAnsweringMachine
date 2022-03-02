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

RUN mkdir -p /etc/service/sshd \
    && mkdir -p /etc/app/sshd \
    && mkdir -p /etc/app/sshd/log \
    && mkdir -p /etc/service/sshd/log \
    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/sshd/log\n" > /etc/service/sshd/log/run \
    && chmod +x /etc/service/sshd/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /usr/sbin/sshd\n" > /etc/service/sshd/run \
    && chmod +x /etc/service/sshd/run

# Force root password
RUN mkdir -p /etc/service/rootforce \
    && echo -e "#!/bin/bash\nexec 2>&1\necho \"root:\${ROOT_PWD}\"|chpasswd\nsleep infinity\n" > /etc/service/rootforce/run \
    && chmod +x /etc/service/rootforce/run

# Start everythin
CMD ["runsvdir", "/etc/service"]

