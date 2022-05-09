FROM ham.base:latest
RUN apk update
RUN apk upgrade

COPY data/ham.sh /etc/app/ham/
COPY data/app /etc/app/ham/app/
RUN chmod +x /etc/app/ham/*.sh


# Start HAM service
RUN /etc/startservice.sh --app=ham --run=/etc/app/ham/ham.sh
