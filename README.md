## Motivation

How to test interdependent applications when you have no control over them. 

A mocking library could work, but they are intrusive and with specific configuration 
that interposes itself into the whole development chain.

Testing over the wire is difficult too, outages on dev environment and not preventable
errors can arise.

The AnsweringMachine is built to overcome this troubles

[Presentation and super quick start (Power Point)](https://github.com/kendarorg/HttpAnsweringMachine/../HttpAnsweringMachine/raw/main/docs/HAM.pptx)

:boom: New! [Docker Images on DockerHUB](https://hub.docker.com/u/kendarorg) :zap: 

## Features

* Proxying of any kind of http/https calls
* Internal socks5 proxy to run even dockerless
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
* Internal events system

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

## Hyper-Fast start

* Install JDK11 and maven
* On windows when checking out remember to set  git core.autocrlf to false
* If you want to try the docker version install
	* Docker
	* [OpenVPN client](https://openvpn.net/vpn-client)
* Make all running Make.sh/bat
	* Choose if you want to build the docker images
* Run everything starting Run.sh/bat
	* If you are running locally modify the hosts file adding the following

		127.0.0.1  www.local.test
		127.0.0.1  www.sample.test
		127.0.0.1  gateway.sample.test
		127.0.0.1  be.sample.test
	* If on docker 
		* Import the profile in docker/images/openvpn/mainuser.local.ovpn on OpenVPN
		* After the startup connect through the profile just installed
* Then navigate
	* [http://www.local.test](http://www.local.test) For the main UI
	* [http://www.sample.test](http://www.sample.test) For the sample application

## History 

This project was born as a way to intercept http/s calls through a PHP application with 
apache, dnsmasq and openvpn on docker. To avoid changing all application configurations to
debug against real environments. Then i added an oidc server, and looking at other products
like Traffic Parrot the recording replaying was inserted as feature. To speed up stuffs it 
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

## Basic functions  

* [The phases](docs/lifecycle.md)
* [Proxy module](docs/proxy.md)
* [Http/s hijack module](docs/https.md)
* [Static pages](docs/static.md)
* [Dns server](docs/dns.md)
* [Events Queue](docs/events.md)
* [Utils](docs/utils.md)

## Rest APIs

## Plugins

* [OIDC](docs/plugins/oidc.md)
* [Replayer](docs/plugins/replayer/replayer.md)
* [Js filters](docs/plugins/js.md)
* [Socks5 Proxy](docs/plugins/socks5.md)
* [Custom Java plugins](docs/plugins/java.md)
  * [Java filters](docs/plugins/java/jfilters.md)
  * [Java pages](docs/plugins/java/jstatic.md)
  * [Java server](docs/plugins/java/jserver.md)

## Utils

* [Special DNS Server](docs/dnsserver.md) Add DNS servers with their...DNS name

## Examples
  
* [Sample Application](docs/sample.md)
* [Running on localhost](docs/localsample.md)
* [Running in docker single instance](docs/docker/single.md)
* [Running in docker multiple instances](docs/docker/multi.md)
* [Recording session](docs/plugins/replayer/recording.md)
* [PACT test](docs/plugins/replayer/pact.md) To verify against changes on real server
* [NULL test](ddocs/plugins/replayer/null.md) To test without any infrastructure
* [UI test](ddocs/plugins/replayer/ui.md) To test UI without any infrastructure
