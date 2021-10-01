## Motivation

How to test interdependent applications when you have no control over them. 

A mocking library could work, but they are intrusive and with specific configuration 
that interposes itself into the whole development chain.

Testing over the wire is difficult too, outages on dev environment and not preventable
errors can arise.

The AnsweringMachine is built to overcome this troubles

## Features

* Proxying of any kind of http/https calls
* Spy mode to log all traffic  
* Configurable Https hijacking with auto generated SSL certificates
* Redirection of services through other destinations  
* Java or Javascript plugins to intercept and modify any http/s call
* Record and replay api flows, with automatic stateful flows detection
* Custom oidc "authorize all" server
* Embedded derby server for any need
* Embedded web server with REST APIs
* All functions manageable via REST APIs
* Configurable DNS hijacking (when not using VPNs) or...generated hosts file

### Docker for development

* Configurable DNS hijacking
* Transparent access via OpenVpn to the internal network
* Debug docker applications directly
* Direct access to the main server via web interface

### Docker For CI

* Can run in a single docker container with applications
* Can be controlled via REST APIs to load scenarios (recordings)
* More configurable than wiremock
* Can use all the real configurations without changes via the Https/DNS hijacking

## History 

This project was born as a way to intercept http/s calls through a PHP application with 
apache, dnsmasq and openvpn on docker. To avoid changing all application configurations to
debug against real environments. Then i added an oidc server, and looking at other products
like Traffic Parrot the recording replaying was inserted as feature. To speed up stuffs it 
was then ported to Spring Boot and was added a true UI.

## Basic Setup

Simply copy the main jar where you want with the "libs" folder at the same level. It works with Java 11!!

### external.properties

Prepare a basic configuration in an "external.properties" file in the same dir of the Jar

The name for which the server will respond

    localhost.name=www.local.test

The http/s configuration, this will be generated at runtime

    http.enabled=true
    https.enabled=true
    https.certificates.cnname=C=US,O=Local Development,CN=local.org
    https.certificate.0=*.local.test
    https.certificate.1=local.test

### Hosts file

Running locally you should map the DNS names to localhost in the hosts file.

Under Windows run notepad as administrator and add the following at the end of 
"C:\Windows\System32\drivers\etc\hosts" file

    127.0.0.1 www.local.test

### Run and verify

Then run

    java "-Dloader.path=/start/services/answering/libs" \
        -Djdk.tls.acknowledgeCloseNotify=true \
        -Dloader.main=org.kendar.Main  \
        -jar app-1.0-SNAPSHOT.jar \
        org.springframework.boot.loader.PropertiesLauncher &

And go with any browser on [http://www.local.test/api/health](http://www.local.test/api/health)

You should see then an "OK" text

### Install the certificates on the system

You can then install the root certificate on the machine, first list the certificate you need 
going on [http://www.local.test/api/certificates](http://www.local.test/api/certificates) that will
return a list of the available formats:

    ["ca.der","ca.p12","ca.cer","ca.key","ca.srl","ca.pem","certificates","ca.crt"]

You can then download your preferred one and install on the OS and on the Browser downloading them from
[http://www.local.test/api/certificates/name](http://www.local.test/api/certificates/name) for example 
if you are on windows you can download the crt file and install it as root 
CA [http://www.local.test/api/certificates/ca.crt](http://www.local.test/api/certificates/ca.crt). You
will obtain a zip file containing the data.

Now you can browse to [https://www.local.test/api/health](https://www.local.test/api/health) (mind the 
HTTPS! ) and you will not receive any security warning

* [Proxy module](docs/proxy.md)
* [Running in docker single instance](docs/dockersingle.md)
* [Running in docker](docs/docker.md)
* [Http/s hijack module](docs/https.md)
* [Js plugins](docs/js.md)
