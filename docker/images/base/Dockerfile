FROM alpine:latest
RUN apk update
RUN apk upgrade

RUN echo ham.base version-4.2.0

ENV JAVA11_HOME /usr/lib/jvm/java-11-openjdk

# Add needed packages
RUN apk add --no-cache bash openssl ca-certificates openjdk11 runit openssh \
    && rm -rf $JAVA11_HOME/demo \
    && rm -rf $JAVA11_HOME/jmods \
    && rm -rf $JAVA11_HOME/include \
    && rm -rf $JAVA11_HOME/man




RUN mkdir -p /etc/ssh
# Load certificates
COPY data/ca.crt /usr/local/share/ca-certificates/
COPY data/sshd_config /etc/ssh/
COPY data/startservice.sh /etc/startservice.sh
COPY data/basesetup.sh /etc/basesetup.sh
COPY data/DoSleep.java /etc/DoSleep.java

# Prepare certificates, runit space, generate ssh keys, setup ssh service, force root password
RUN chmod 777 /etc/basesetup.sh
RUN  /etc/basesetup.sh

# Start everything
CMD ["runsvdir", "/etc/service"]

