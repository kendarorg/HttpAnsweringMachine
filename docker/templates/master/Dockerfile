FROM ham.singlemaster:latest
RUN echo 4.1.4

COPY ./config/*.json /etc/app/ham/app/
RUN mkdir -p /etc/app/app
COPY ./config/app.sh /etc/app/
RUN /etc/startservice.sh --app=app

# Ham interface
EXPOSE 80
EXPOSE 443
# Ham remote debug port
EXPOSE 5025
EXPOSE 1080

TODO: Add the ports exposed by your application
