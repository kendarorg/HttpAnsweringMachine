Situated into samples/sampleapp/docker_multi folder

This demonstrates the creation of a HAM server with infrastructure in multiple container 
plus an OpenVpn server to connect to it

To build the basic images you can find the docker/images/ImagesBuild.(bat|sh)

To build the sample you can use samples/sampleapp/docker_multi/ImagesBuild.(bat|sh)
To run the sample you can use samples/sampleapp/docker_multi/ImagesRun.(bat|sh)

## 1: Hosts file and DNS

The setup for the external.json will be like the [localsample](../localsample.md) but without the hosts file

## 2: Proxying

No proxy is needed here

## 3: Starting the sample application

Should create the service executable and docker images for all parts (be and fe) let's see the fe as an example.

### Scripts

Remember the ports!!!

    #!/bin/bash
    # run-fe.sh

    export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
    export PATH="${JAVA_HOME}/bin:${PATH}"

    cd /etc/app/fe
    java -jar -Dserver.port=80 /etc/app/fe/fe-1.0-SNAPSHOT.jar

Then the properties file

    # application.properties.fe
    server.port=80
    employee.location=http://gateway.sample.test
    appointment.location=http://gateway.sample.test

### Dockerfile

Should setup the dockerfile of the master with the external.json and set the master as base image

    FROM ham.master:latest
    COPY ./docker/external.json /etc/app/ham/app/

Then the fe docker file

    FROM ham.client:latest
    # Dockerfile.fe

    # Run the service and hide logs (we are on single)
    RUN /etc/startservice.sh --app=fe --run=/etc/app/fe/run-fe.sh --capturelogs
    # Copy the run script
    COPY ./docker/run-fe.sh /etc/app/fe/
    # Copy the properties
    COPY ./docker/application.properties.fe /etc/app/fe/application.properties
    # Copy the jar
    COPY ./fe/target/*.jar /etc/app/fe/
    # Make executable
    RUN chmod +x /etc/app/fe/*.sh

### Docker compose

This will be a bit harder.

* Configure a specific bridge network with its subnet
* Add the openvpn to access the whole thing
    * ports: to access via openvpn
    * cap_add: rights to mess with dns and network structure
    * privileged: rights to mess even more with the internal network and config
    * environment
        * DNS_HIJACK_SERVER: This will be the ham machine name. The DNS that the openvpn will really use
        * ROOT_PWD: the root password, to ssh
* Add the master image. The dns hijack is not needed because..it's itself
* Add the fe/be/gateway. Here is the example for fe only but on source there is everything
    * cap_add: rights to mess with dns and network structure
    * privileged: rights to mess even more with the internal network and config
    * environment
        * DNS_HIJACK_SERVER: This will be the ham machine name. The DNS that the openvpn will really use
        * ROOT_PWD: the root password, to ssh

version: "2"
networks:
  multisampleappnet:
    driver: bridge
    ipam:
      config:
        - subnet: 172.25.3.0/24
services:
  ham.sampleapp.multi.openvpn:
    container_name: ham.sampleapp.multi.openvpn
    privileged: true
    cap_add:
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:
      - 127.0.0.1
    ports:
      - "3000:1194/udp"
    networks:
      - multisampleappnet
    environment:
      - DNS_HIJACK_SERVER=ham.sampleapp.multi.master
      - ROOT_PWD=root
    image: ham.openvpn
    depends_on:
      - ham.sampleapp.multi.master
  ham.sampleapp.multi.master:
    container_name: ham.sampleapp.multi.master
    privileged: true
    environment:
      - ROOT_PWD=root
    cap_add:
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:
      - 127.0.0.1
    image: ham.sampleapp.multi
    networks:
      - multisampleappnet
    ports:
      - "11080:80"
      - "11443:443"
  ham.sampleapp.multi.gateway:
    ... as FE
  ham.sampleapp.multi.be:
    ... as FE
  ham.sampleapp.multi.fe:
    container_name: www.sample.test
    privileged: true
    environment:
      - DNS_HIJACK_SERVER=ham.sampleapp.multi.master
      - ROOT_PWD=root
    cap_add:
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:
      - 127.0.0.1
    image: ham.sampleapp.fe
    networks:
      - multisampleappnet
    depends_on:
      - ham.sampleapp.multi.master

## 5: Testing 

Load the configuration on docker/images/openvpn/mainuser.local.ovpn for your openvpn client and have fun!

Now you can start testing everything with [PACT](plugins/replayer/pact.md) 
or [NULL infrastructure tests](plugins/replayer/null.md)
or [UI tests](plugins/replayer/ui.md)