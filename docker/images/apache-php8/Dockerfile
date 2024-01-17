FROM ham.apache

RUN echo ham.apache-php8 version-4.3.1


# Setup apache and php
RUN apk add --no-cache \
    libavif --repository=https://dl-cdn.alpinelinux.org/alpine/edge/main/
RUN apk add --no-cache \
    php81-apache2 \
    php81-bcmath \
    php81-bz2 \
    php81-calendar \
    php81-common \
    php81-ctype \
    php81-curl \
    php81-dom \
    php81-gd \
    php81-iconv \
    php81-mbstring \
    php81-mysqli \
    php81-mysqlnd \
    php81-openssl \
    php81-pdo_mysql \
    php81-pdo_pgsql \
    php81-pdo_sqlite \
    php81-phar \
    php81-session \
    php81-xml \
    php81-pear \
    php81-xdebug --repository=https://dl-cdn.alpinelinux.org/alpine/edge/community/ \
    && mkdir -p docker/php/conf.d
    
# WAS ON --repository=http://dl-cdn.alpinelinux.org/alpine/edge/testing

RUN pear81 config-set php_ini /etc/php81/php.ini

COPY config/setupphp8.sh /etc/setupphp8.sh
COPY config/error_reporting.ini /etc/php81/conf.d/error_reporting.ini

RUN chmod +x /etc/*.sh \
	&& /etc/setupphp8.sh

