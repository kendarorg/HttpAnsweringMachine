FROM alpine:latest
RUN apk update
RUN apk upgrade


# Add needed packages
RUN apk add --no-cache wget curl bash openssl tar ca-certificates openjdk11 runit openssh
ENV JAVA11_HOME /usr/lib/jvm/java-11-openjdk
RUN mkdir -p /etc/ssh
# Load certificates
COPY data/ca.crt /usr/local/share/ca-certificates/
COPY data/sshd_config /etc/ssh/
COPY data/startservice.sh /etc/startservice.sh
COPY data/basesetup.sh /etc/basesetup.sh
COPY data/DoSleep.java /etc/DoSleep.java

# Prepare certificates, runit space, generate ssh keys, setup ssh service, force root password
RUN chmod 777 /etc/basesetup.sh \
    && /etc/basesetup.sh

# Start everything
CMD ["runsvdir", "/etc/service"]

