FROM ham.client:latest as builder
FROM dre1080/alpine-apache-php8

# CLIENT SECTION
RUN apk add --no-cache bash openssl ca-certificates openjdk11 runit openssh
ENV JAVA11_HOME /usr/lib/jvm/java-11-openjdk

COPY --from=builder /usr/local/share/ca-certificates/ca.crt /usr/local/share/ca-certificates/ca.crt
COPY --from=builder /etc/ssh/sshd_config /etc/ssh/sshd_config
COPY --from=builder /etc/*.sh /etc/
COPY --from=builder /etc/DoSleep.* /etc/
COPY --from=builder /etc/app/simpledns/simpledns*.* /etc/app/simpledns/

RUN chmod 777 /etc/*.sh \
        && /etc/basesetup.sh \
        && /etc/clientsetup.sh

# SERVER SECTION

# Mimic the call made in dre1080/alpine-apache-php8
# ENTRYPOINT ["docker-entrypoint.sh"]
# CMD ["httpd", "-D", "FOREGROUND"]
# Then call the client setups
RUN echo "#"'!'"/bin/bash" >/etc/stap.sh \
    && echo "/usr/local/bin/docker-entrypoint.sh echo Starting ">> /etc/stap.sh \
    && echo "httpd -D FOREGROUND" >> /etc/stap.sh \
    && /etc/startservice.sh --app=apache --run=/etc/stap.sh

# Copy the source files
COPY core/ /app/public/

# Reset the entrypoint
ENTRYPOINT []
# Run as it should with runit
CMD ["runsvdir", "/etc/service"]