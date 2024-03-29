
<!--This file is autogenerated. Do not edit!-->
In this demo you will 

This tutorial explains how to setup a "cage" system for 
a Java 11 application
## Index

* [Prepare the HAM container](#preparehamcontainer)
* [Prepare the Application container](#prepareappcontainer)
* [Docker with proxy access](#dockerproxy) On the same machine (e.g. local docker or Docker Desktop)
    * [Write the compose](#dockerproxy_composer)
    * [Modify the configuration](#dockerproxy_setupconfig)
    * [Run it!](#dockerproxy_run)
* [Docker with VPN access](#dockervpn) When the client accessing the app is on a physically different machine or in a VM
* [What's next](#whatsnext)

Suppose you have an application named... **testapp**

## Prepare the HAM container<a id="preparehamcontainer"></a>

Create a directory "master" and inside it

Prepare the configuration file, e.g. here you can find the
default template [external.json](files/external.json)

Just setup a "Dockerfile" like the following for the HAM master

<pre>
FROM ham.master:latest
# Copy the configuration
COPY .external.json /etc/app/ham/app/external.json
</pre>

And create the image

<pre>
docker build --rm -t testapp.master .
</pre>

## Prepare the application container<a id="preparehamcontainer"></a>

Create a directory "testapp" and inside it

Create the startup file, testapp.sh

<pre>
#!/bin/sh

# Reach the application directory
cd /etc/app/testapp

# Set the java home and path
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"

# Run your jar. Here with the agentlib line to allow remote debugging
# and the server port for Spring Boot
java -jar -Dserver.port=80 \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
  /etc/app/testapp/app-1.0.0.jar
</pre>

Prepare the Dockerfile. Note that the client images already handles all basic initializations
like DNS resolution, services initializations and certificates management

<pre>
FROM ham.client:latest

# Update the image
# Create the application dir
# The client comes already with java 11 installed
RUN apk update && apk upgrade &&
    mkdir -p /etc/app/testapp

# Copy the jars
COPY /[jarAbsoluteDirTarget]/*.jar /etc/app/testapp/
# Copy the run script
COPY .testapp.sh /etc/app/testapp/

# Set the .sh as executable
# Start the application in foreground with runit
RUN chmod +x /etc/app/testapp/*.sh &&
    /etc/startservice.sh --app=testapp --run=/etc/app/testapp/testapp.sh
</pre>

And create the image

<pre>
docker build --rm -t testapp.app .
</pre>

## Docker compose for proxy access<a id="dockerproxy"></a>

### Docker compose<a id="dockerproxy_composer"></a>

Prepare a docker compose to connect to the application

<pre>
version: "2"
networks:
  testappnet:  # Setup a network for the system:
    driver: bridge
    ipam:
      config:
        - subnet: 172.25.7.0/24 # Define a subnet not already used
services:
  testapp.master:  # The HAM instance
    container_name: testapp.master # The DNS name of the instance
    privileged: true  # Needed for DNS hijacking
    environment:
      - ROOT_PWD=root
    cap_add:    # Needed for DNS hijacking
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:
      - 127.0.0.1
    image: testapp.master
    networks:
      - testappnet
    ports:
      - "5025:5025" # Ham debug port (optional)
      - "1080:1080" # Socks5 Proxy
      - "1081:1081" # Http/s Proxy
  testapp.app:  # The application
    container_name: www.testapp.com # The DNS name of the instance
    environment:
      - DNS_HIJACK_SERVER=testapp.master  # Use HAM as final DNS server
      - ROOT_PWD=root
    privileged: true # Needed for DNS hijacking
    cap_add: # Needed for DNS hijacking
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:  
      - 127.0.0.1
    image: testapp.app
    ports:
      - "8080:80" # To expose the application directly (optional)
    networks:
      - testappnet
    depends_on: # Wait for HAM to start
      - testapp.master
</pre>

### Adapt the configuration<a id="dockerproxy_setupconfig"></a>

To make the test "real" we will add a DNS and SSL entry to
the configuration.

In the section DNS of the "external.json" we tells that HAM should
intercept all requests to our application. They will then
be forwarded to the real instance

<pre>
[
  {
    "id": "dns",
    ...,
    "resolved": [
      ...,
      {
        "id": "123456",
        "dns": "www.testapp.com",
        "ip": "127.0.0.1"
      }
    ] ...
</pre>

In the section SSL of the "external.json" we tells that HAM should
build certificates for all testapp.com domains

<pre>
  {
    "id": "ssl",
    ...,
    "domains": [
      {
        "id": "123456",
        "address": "*.testapp.com"
      }
    ] ...

</pre>

### Run and connect!<a id="dockerproxy_run"></a>

Run the compose

<pre>
docker-compose up
</pre>

Setup the proxy on Firefox or Chrome (For the latter install [Switch Omega](https://chrome.google.com/webstore/detail/proxy-switchyomega/padekgcemlokbadohgkifijomclgjgif))
or for the application you are using to stimulate the application, like Postman

## Docker compose for vpn access<a id="dockervpn"></a>

First complete the common steps:

* [Prepare the HAM container](#preparehamcontainer)
* [Prepare the Application container](#prepareappcontainer)
* [Docker with proxy access](#dockerproxy) On the same machine (e.g. local docker or Docker Desktop)
    * [Write the compose](#dockerproxy_composer)
    * [Modify the configuration](#dockerproxy_setupconfig)

Install [OpenVpn Connect Client](https://openvpn.net/) on the client machine

Download the [connection script](https://raw.githubusercontent.com/kendarorg/HttpAnsweringMachine/main/docker/images/openvpn/mainuser.local.ovpn)
and change the following line with the address of the machine running docker.

<pre>
remote 127.0.0.1 3000 udp
</pre>

Install the .ovpn file in your OpenVPN connect

Add the following instance to the docker compose. There is a custom image ready for that :)

<pre>
  testapp.vpn:  # The application
    container_name: testapp.vpn # The DNS name of the instance
    environment:
      - DNS_HIJACK_SERVER=testapp.master  # Use HAM as final DNS server
      - ROOT_PWD=root
    privileged: true # Needed for DNS hijacking
    cap_add: # Needed for DNS hijacking
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:  
      - 127.0.0.1
    image: ham.openvpn/latest
    ports:
      - "3000:1194/udp" # To expose the OpenVpn port
    networks:
      - testappnet
    depends_on: # Wait for HAM to start
      - testapp.master
</pre>

Start the composer wait for the whole system to start and connect the client machine :)
Now you are completely inside the HAM cage!

## What's next<a id="whatsnext"></a>

Running the application you can now

* [Intercept the DNS calls](http://www.local.test/dsn/resolved.html) and add them as HAM resolved, with the local CA
* [Add custom Javascript filters and APIs](http://www.local.test/plugins/jsfilter)
* [Record/Replay/Test the app](http://www.local.test/plugins/recoding) automatically and without coding
* [Rewrite any url to anything](http://www.local.test/proxy)
* [Intercept ALL http/s interactions](http://www.local.test/logs) dynamically

