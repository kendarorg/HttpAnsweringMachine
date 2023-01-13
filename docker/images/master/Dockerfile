FROM ham.base

RUN echo 4.1.2

COPY data/ham.sh /etc/app/ham/
COPY data/app /etc/app/ham/app/
RUN chmod +x /etc/app/ham/*.sh


# Start HAM service
RUN /etc/startservice.sh --app=ham --run=/etc/app/ham/ham.sh
