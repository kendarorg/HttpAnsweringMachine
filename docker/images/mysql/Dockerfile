FROM kendarorg/ham.client:latest

ENV TIMEZONE=Europe/Paris
ENV MYSQL_ROOT_PASSWORD=root
ENV DATA_DIR=/var/lib/mysql

RUN apk add --no-cache mysql mysql-client \
    && addgroup mysql mysql 

RUN mkdir -p /etc/app/mysql
COPY ./config/mysql.sh /etc/app/mysql/
RUN chmod +x /etc/app/mysql/*.sh
RUN /etc/startservice.sh --app=mysql --run=/etc/app/mysql/mysql.sh
RUN sed -i -e "/^skip-networking/d" /etc/my.cnf.d/mariadb-server.cnf    \
    && sed -i -e "/^bind-address/d" /etc/my.cnf.d/mariadb-server.cnf \
    && echo skip-networking=0 >> /etc/my.cnf \
    && echo bind-address=0.0.0.0 >> /etc/my.cnf

RUN mkdir -p /etc/app/app/ddl
#COPY ./config/app.sh /etc/app/app/
COPY ./config/mysqlsetup.sh /etc/app/app/
COPY ./config/ddl/*.* /etc/app/app/ddl
RUN chmod +x /etc/app/app/*.sh \
    && /etc/startservice.sh --config --app=mysqlsetup --run=/etc/app/app/mysqlsetup.sh

