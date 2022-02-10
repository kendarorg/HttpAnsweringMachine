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
adding the certificate generation. The id must be unique

    {[  "id" : "ssl",
        "domains" : [{
                "id" :"a",
                "address":"*.google.com"},
            {
                "id" :"b",
                "address":"google.com"},
    ...

Now you should enable the DNS server and add the names, as usual the number starts from 0

    [{  "id":"dns",
        "active" : true,
        "resolved":[{
            "id"  : "0",
            "ip"  : "127.0.0.1"
            "dns" : "www.google.com"},

Now every request will go through the system

## On a docker system

Supposing to have a running dockerfile instance in which runs the system (let's say main.local.self), you
should set the DNS to main.local.self for its installation see [Running in docker](docs/docker.md)  .

Of course when you do this you can't (easily) know in advance the address of main.local.self. For this reason
you can install on the machine needing to use the system a simple dns proxy. the simpledns.jar

To start it you can use the following command line to start the service. The other dns will call
the main ham server (192.168.1.2)

    java -Dother.dns=192.168.1.2 \
        -jar simpledns-1.0-SNAPSHOT.jar

This way you could start the image as with the single instance described before adding the 127.0.0.1 as
dns server. The simpledns will take care of resolving the value of main.local.self with the preceding
dns server.

Eventually you can find the ham server address from the name -before- calling the simple dns

    export hamContainerIp=`ping -c 4 $HAM_CONTAINER_NAME|head -n 1| grep -Eo "([0-9]+\.?){4}"`

passing the HAM_CONTAINER_NAME as an environment variable on compose or docker run
