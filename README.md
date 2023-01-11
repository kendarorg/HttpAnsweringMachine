## Motivation

How to test interdependent applications when you have no control over them. 

A mocking library could work, but they are intrusive and with specific configuration 
that interposes itself into the whole development chain.

Testing over the wire is difficult too, outages on dev environment and not preventable
errors can arise.

The AnsweringMachine is built to overcome this troubles

[Record/replay db calls in integration tests!](docs/generated/dbtest.md) :zap: NO DB REQUIRED TO REPLAY!

* [Contribute](CONTRIBUTING.md) it's easy :)
* [Docker Images on DockerHUB](https://hub.docker.com/u/kendarorg)
* Maven repos
  * [Release](https://www.kendar.org/maven2/releases)
  * [Snapshot](https://www.kendar.org/maven2/snapshots)
* [Swagger](https://www.kendar.org/swagger/?model=https://raw.githubusercontent.com/kendarorg/HttpAnsweringMachine/main/docs/map.json) 

## Ready-to-run examples

* [Recording interactions](docs/generated/localsample.md) 10 minutes
  * Run the sample application (1 fe, 2 be)
  * Record the interactions
* [Hacking google](docs/generated/googlehack.md) 10 minutes
  * Hijack DNS
  * Setup a fake root certificate authority
  * Bing-ify Google home page
* [Intercept your Android phone!](docs/generated/googlehack_android.md) 10 minutes
  * Hijack DNS
  * Setup a fake root certificate authority
  * Bing-ify Google home page
* [Fast recording/replaying](docs/generated/manualtestcalendar.md) 20 minutes
  * Run the sample application (1 fe, 2 be)
  * Run the front-end with a mocked back-end
  * Run the front-end with a gateway and a mocked back-end
* [Fast Automatic NullInfrastructure Test](docs/generated/automatictestcalendar.md) 30 minutes
  * Run the sample application (1 fe, 2 be)
  * Test a back-end in isolation
  * Verify match between template data
* [Simulating a real database](docs/generated/dbtest.md) 20 minutes
  * Record the db interactions
  * Shut down the real db
  * Run the application without db!
* **Dockerize any application** 20 minutes
  * Build a custom docker cage for your Java/.NET/Node/... wathever application
  * Access via Proxy or via VPN to experience the full environment
  * Custom path:
    * [Java 11](docs/generated/runyourapp_java11.md)
    * [Java 1.8](docs/generated/runyourapp_java8.md)
    * [Apache-php 8](docs/generated/runyourapp_apachephp8.md)

## Features

* Store logs and recordings on any Jdbc DB
* Default embedded h2 database
* Proxying of any kind of http/https calls
* Internal socks5/http/https proxy to run even dockerless
* Spy mode to log all traffic  
* Configurable Https hijacking with auto generated SSL certificates
* Redirection of services through other destinations  
* Java and Javascript plugins to intercept and modify any http/s call
* Record and replay api flows, with automatic stateful flows detection
* Custom oidc "authorize all" server
* Embedded web server with REST APIs
* All functions manageable via REST APIs
* Configurable DNS hijacking (when not using VPNs) or...generated hosts file
* Internal events system
* Support for brotli encoding and jackson-smile
* Multiple server ports configurable

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
* Can be used an alternative dns server on docker, supposed you add on the machines
connecting to it the [Special DNS Server](docs/dnsserver.md)

## History 

This project was born as a way to intercept http/s calls through a PHP application with 
apache, dnsmasq and openvpn on docker. To avoid changing all application configurations to
debug against real environments. Then i added an oidc server, and looking at other products
like Traffic Parrot or Hoverfly.io, the recording replaying was inserted as feature. To speed up stuffs it 
was then ported to Spring Boot and was added a true UI.

## Conventions

Notice that all configuration values are in fact -paths- inside the external.json file.
For example "global.logging.request.basic=true" means the following

<pre>
[
  {
    "id": "global",
    ...
    "logging": {
      "request": {
        "basic": true
...
</pre>

In case of arrays "ssl.domains[1].address=local.test" means the following

<pre>
[
  {
    "id": "ssl",
    ...
    "domains": [
      {
        ...
      },
      {
        ....
        "address": "local.test"
</pre>

## Configurations

* [Basic local configuration](docs/basic.md)
* [Logging](docs/logging.md)
* [Docker configuration](docs/docker/commons.md)
* [Database configuration](docs/database.md)

## Basic functions  

* [The phases](docs/lifecycle.md)
* [Proxy module](docs/proxy.md)
* [Database interceptor](docs/jdbc.md)
* [Http/s hijack module](docs/https.md)
* [Static pages](docs/static.md)
* [Dns server](docs/dns.md)
* [Events Queue](docs/events.md)
* [Utils](docs/utils.md)

## Plugins

* [OIDC](docs/plugins/oidc.md)
* [Replayer](docs/plugins/replayer/replayer.md)
* [Js filters](docs/plugins/js.md)
* [Socks5/http/https Proxy](docs/plugins/socks5.md)
* [Custom Java plugins](docs/plugins/java.md)
  * [Java filters](docs/plugins/java/jfilters.md)
  * [Java pages](docs/plugins/java/jstatic.md)
  * [Java server](docs/plugins/java/jserver.md)

## Utils

* [Special DNS Server](docs/dnsserver.md) Add DNS servers with their...DNS name
* [Scripts](docs/scripts.md) The content of the scripts dir

## Examples
  
* [Sample Application](docs/sample.md)
  * [Running on localhost](docs/generated/localsample.md)
  * [Running in docker single instance](docs/docker/single.md)
  * [Running in docker multiple instances](docs/docker/multi.md)
* [Running in docker with apache](docs/docker/quotes.md)
* [Recording session](docs/plugins/replayer/recording.md)
* [PACT test](docs/plugins/replayer/pact.md) To verify against changes on real server
* [NULL test](ddocs/plugins/replayer/null.md) To test without any infrastructure
* [Regression test](ddocs/plugins/replayer/simple.md) To test UI without any infrastructure
