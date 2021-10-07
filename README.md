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


* [Basic configuration](docs/basic.md)
* [Http/s hijack module](docs/https.md)
* [Proxy module](docs/proxy.md)
* [The phases](docs/lifecyvle.md)
* [Js plugins](docs/js.md)
* [Java plugins](docs/java.md)
* [Running in docker single instance](docs/dockersingle.md)
* [Running in docker](docs/docker.md)
