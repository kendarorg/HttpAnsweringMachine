All the available docker images are in docker/images, samples and on Docker Hub: [https://hub.docker.com/u/kendarorg](https://hub.docker.com/u/kendarorg)

## TLDR

To start at once installing the demo applications just download and run with "docker-compose up" the following composer files. The HAM server is at http://www.local.test

* [Calendar app](https://github.com/kendarorg/HttpAnsweringMachine/../HttpAnsweringMachine/raw/main/samples/calendar/hub_composer/docker-compose.yml). The application is at http://www.sample.test
* [Quotes app](https://github.com/kendarorg/HttpAnsweringMachine/../HttpAnsweringMachine/raw/main/samples/quotes/hub_composer/docker-compose.yml). The application is at http://ham.quotes.master

Then connect using as socks5 proxy [dockerip]:1080

* For chrome you can launch it (as ONLY instance): [chrome location]chrome "--proxy-server=socks5://dockerip:1080"


## Base (ham.base)

The basic docker image. With java 11 and all the certificates registered

Notice that the JVM is NOT set on PATH or JAVA_HOME so that you can add anything you like

### Parameters

ROOT_PWD: The root password, default to root

### Environment variables

JAVA11_HOME: The home of Java11

### Runit

Contains runit as service initializer. To add services to runit into the Dockerfiles
you have to run, before copying stuffs, the following

    RUN /etc/startservice.sh --options

The command takes the following options

* --app=APPNAME: where the service specific files will be stored
    * /etc/app/APPNAME: will contain the files for the service to start
    * /etc/service/APPNAME: will contain the runit configuration
    * /etc/app/APPNAME/APPNAME.sh: will be the entry point for the service
* --run=APPSH: the full path of the executable to run instead of the default /etc/app/APPNAME/APPNAME.sh
* --capturelogs: if this flag is set the log will not be shown on the stdout of the container but stored in /etc/app/APPNAME/logs
* --config: for run once services

The only problem is that there is no guarantee of order in runit, be warned!

### SSH Server

It contains an ssh server on standard port, whose password is set by default to root OR
following docker run/compose parameters


## Client (ham.client)

The base image for any client. Contains the setup of "simpledns"m debuggable on port 5015

This is used to force the dns of the docker machine to the localhost AND forwarded to HAM

You can add to it all that you want included new services/configurations or SDKs (java, net, wetheaver)

### Parameters

DNS_HIJACK_SERVER: The server where is running the HAM server

## Master (ham.master)

The base image for the ham server, debuggable on port 5025

All data is stored in /etc/app/ham/app

The "external.json" file should be set inside the /etc/app/ham/app/ directory to override the default sample configuration

## Openvpn (ham.openvpn)

This can be used to connect directly through OpenVpn to the internal HAM network

The configuration file is located in docker/images/mainuser.local.ovpn configure to access localhost via 
the exposed 3000 port

Does not need passwords and gives you full access to the internal network and dnses

## Apache (ham.apache)

Apache 2 server. Can copy the website data inside the container "/htdocs" directory

### Parameters

LOG_LEVEL: default to info


## Apache+php8 (ham.apache.php8)

Apache 2 server with php8 support. Can copy the website data inside the container "/htdocs" directory

### Parameters

LOG_LEVEL: default to info
PHP_MEMORY_LIMIT: default to 256M

## MySQL (ham.mysql)

mysql server+client

At startup creates the databases listed in MYSQL_DBS then iterate on 
the directory MYSQL_DATA looking for directories named like the database
and execute the founded scripts in alphabetical order.

A sample structure can be the following

  /etc
    /mysqldata
      /firstdb
        /00-setup.ddl
        /01-insert.sql
      /seconddb
        ...


### Parameters

MYSQL_USER: default to main
MYSQL_PASSWORD: default to main.
MYSQL_DBS: default to test. Can be a ";" separated list of db names
MYSQL_DATA: default to /etc/mysqldata