#!/bin/sh

# Exit on non defined variables and on non zero exit codes
set -eu

TZ="${TZ:-UTC}"
PHP_MEMORY_LIMIT="${PHP_MEMORY_LIMIT:-256M}"

echo 'Updating configurations'

# Modify php memory limit and timezone
sed -i "s/memory_limit = .*/memory_limit = ${PHP_MEMORY_LIMIT}/" /etc/php8/php.ini
sed -i "s#^;date.timezone =\$#date.timezone = \"${TZ}\"#" /etc/php8/php.ini
