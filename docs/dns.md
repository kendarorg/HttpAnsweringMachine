The application contains a full DNS Server. 

When you change the DNS settings in some situation you need to restart the application using it:

* Browser: every time the browser goes to a page caches the DNS entry. Going onto a page and then changing the DNS makes mandatory restarting the browser
* Application: as with browser every major rest client implementation does caches DNS requests and therfore the container application, if called one of the changed names before we changed the DNS name, must be restarted  

## On external properties

It can be enabled inside the external.json with active

    [{  "id":"dns",
        "active": true,

### Special definition for "localhost/127.0.0.1" resolution

When running the DNS server search for the lan address of the machine on
which it runs. When you have multiple interfaces (like while on VPN) you
can set it to a fixed ip. When you write 127.0.0.1 on the dns resolved names
it will be translated to the following

    "forceLocalAddress" : "192.168.10.5",

### Extra servers

This dns server can be used to forward dns requests to other
dns servers, through the extraServers property

    [{  "id":"dns",
        "active": true,
        "extraServers":[{
            "id":"0",
            "ip":"8.8.8.8"},
          {
            "id":"1",
            "ip":"my.dns.it"}

You can add dns servers ips or names. For example if you are in a docker instance
and want a custom dns server like "my.dns.it" you should do something like this

    [{  "id":"dns",
        "active": true,
        "extraServers":[{
            "id":"0",
            "ip":"127.0.0.11"},
          {
            "id":"1",
            "ip":"my.dns.it"}

This means

* Add the default docker server (127.0.0.11)
* Add the docker instance my.dns.it to the dns servers and seek it with the previously 
  declared dns servers
  
Following this approach you don't need to know in advance the IP of the docker instance 
with the dns server

    IMPORTANT
    When the DNS requests are forwarded to the extra servers, the are made all UPPERCASE
    When an uppercase domain name is requested the answer will be always "no domain found"
    This is implemented to avoid DNS request infinite loops

### Blocker

You can block certain DNS requests to have less traffic. Requests to the blocked domain
will result in a "domain not existent" response from the DNS server.

The dns in this example are the ones used by Chrome and Windows for network sanity 
checks that simply are not useful outside them

    [{  "id":"dns",
        "active": true,
        "blocked":[
            "wpad.*",
            "*.trafficmanager.net"
    ....

This default example (these should be added in every configurations) tells that domains
starting with "wpad." or ending with ".trafficmanager.net" will never start a real dns
request. Please notice that only two forms are allowed.

* "[wetheaver]*" or starting with
* "*[wetheaver]" or ending with

### Local names resolution

An example is the following. Notice that the id must be unique

    [{  "id":"dns",
        "active": true,
        "resolved":[{
            "id"  : "0",
            "ip"  : "127.0.0.1"
            "dns" : "@([0-9a-zA-Z]+).local.test"},
          {
            "id"  : "a",
            "ip"  : "127.0.0.1"
            "dns" : "www.google.com"},
          {
            "id"  : "b",
            "ip"  : "10.0.0.1"
            "dns" : "www.facebook.com"},
    ...

Here you can notice that the first regexp matches all domains of the form [name].local.test
and www.google.com.

Telling that the address is 127.0.0.1 (loopback interface) means that every DNS request for
those names will contain the address exposed by the local machine

Instead for the www.facebook.com DNS entry will be returned the exact ip set

A further possibility is to configure a DNS entry with a name. This is useful when should
simply use the DNS as is without the need to intercept data: an example is an sql server:
locally i want to redirect to the address of the container instance

          {
            "id"  : "a",
            "ip"  : "running.docker.container.with.mysql"
            "dns" : "mysql.db.server.com"},

### Logging

Inside the global logger section you can add a special logger

    org.kendar.dns.DnsQueries

Whose values can be

* OFF: no log at all (default)
* DEBUG: show successful requests
* TRACE: show even unsuccessful requests

## Extra servers, JVM Arguments

You can even add extra servers through the JVM arguments with the "other.dns" property 

    java ... -Dother.dns=127.0.0.11,main.local.self ...

This is used (by me) mainly inside docker containers
