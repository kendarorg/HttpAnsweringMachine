## On a single docker instance

Supposed you can add the system as a second service on the docker image (see [Single Docker Installation](docs/dockersingle.md))

Using a docker system, you will need to add the 127.0.0.1 dns server to the docker machine
With docker command

    docker run --name [INSTANCENAME] --privileged ^
        --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH ^
        --dns=127.0.0.1 [IMAGENAME]

Or docker compose

<pre>
    version: "2"
    services:
      baseclient.local.self:
        container_name: baseclient
        build: './'
        privileged: true
        dns:
          - 127.0.0.1
</pre>

Then we have to create a certificate for the website, editing the external.properties
adding the certificate generation. The data is 0 based as usual

    https.certificate.2=*.google.com
    https.certificate.3=google.com

Now you should enable the DNS server and add the names, as usual the number starts from 0

    dns.active=true
    dns.resolve.1=www.google.com 127.0.0.1

Now every request will go through the system

## On a docker system

Supposing to have a running dockerfile instance in which runs the system (let's say main.local.self), you
should set the DNS to main.local.self for its installation see [Running in docker](docs/docker.md)  .

Of course when you do this you can't (easily) know in advance the address of main.local.self. For this reason
you can install on the machine needing to use the system a simple dns proxy. the simpledns.jar

To start it you can use the following command line to start the service

    java -Dother.dns=127.0.0.11,8.8.8.8,8.8.4.4,main.local.self \
        -jar simpledns-1.0-SNAPSHOT.jar

This way you could start the image as with the single instance described before adding the 127.0.0.1 as
dns server. The simpledns will take care of resolving the value of main.local.self with the preceding
dns server.

Notice the 127.0.0.11 address that is the default DNS address used inside the container
