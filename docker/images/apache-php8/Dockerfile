FROM ham.apache

RUN echo ham.apache 4.0.2-SNAPSHOT

# Setup apache and php
RUN apk add --no-cache \
    php8-apache2 \
    php8-bcmath \
    php8-bz2 \
    php8-calendar \
    php8-common \
    php8-ctype \
    php8-curl \
    php8-dom \
    php8-gd \
    php8-iconv \
    php8-mbstring \
    php8-mysqli \
    php8-mysqlnd \
    php8-openssl \
    php8-pdo_mysql \
    php8-pdo_pgsql \
    php8-pdo_sqlite \
    php8-phar \
    php8-session \
    php8-xml \
    php8-pear \
    php8-xdebug \
    && mkdir -p docker/php/conf.d
    
RUN pear8 config-set php_ini /etc/php8/php.ini

COPY config/setupphp8.sh /etc/setupphp8.sh
COPY config/error_reporting.ini /etc/php8/conf.d/error_reporting.ini

RUN chmod +x /etc/*.sh \
	&& /etc/setupphp8.sh

