FROM ham.client:latest

COPY docker_multi/core/startapache.sh /etc/startapache.sh
COPY docker_multi/core/xdebug.ini /etc/php8/conf.d/docker-php-ext-xdebug.ini
COPY docker_multi/core/error_reporting.ini /etc/php8/conf.d/error_reporting.ini
COPY core/ /htdocs/

RUN apk --no-cache --update \
    add apache2 \
    apache2-ssl \
    curl \
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
    && mkdir -p docker/php/conf.d \
    && pear8 config-set php_ini /etc/php8/php.ini \
    && chmod +x /etc/*.sh \
    && /etc/startservice.sh --app=apache --run=/etc/startapache.sh