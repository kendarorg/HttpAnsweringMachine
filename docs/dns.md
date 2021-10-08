The application contains a full DNS Server. 

## On external properties

It can be enabled inside the external.properties with

    dns.enabled=true

### Extra servers

This dns server can be used to forward dns requests to other
dns servers, through the extraServers property

    dns.extraServers=8.8.8.8,my.dns.it

You can add dns servers ips or names. For example if you are in a docker instance
and want a custom dns server like "my.dns.it" you should do something like this

    dns.extraServers=127.0.0.11,my.dns.it

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
will result in a "domain not existent" response from the DNS server

    dns.blocker=wpad.*,*.trafficmanager.net

This default example (these should be added in every configurations) tells that domains
starting with "wpad." or ending with ".trafficmanager.net" will never start a real dns
request. Please notice that only two forms are allowd

* "[wetheaver]*" or starting with
* "*[wetheaver]" or ending with

### Local names resolution

An example is the following

    dns.resolve.0=@([0-9a-zA-Z]+).local.test 127.0.0.1
    dns.resolve.1=www.google.com 127.0.0.1
    dns.resolve.2=www.facebook.com 10.11.12.55

Here you can notice that the first regexp matches all domains of the form [name].local.test
and www.google.com.

Telling that the address is 127.0.0.1 (loopback interface) means that every DNS request for
those names will contain the address exposed by the local machine

Instead for the www.facebook.com DNS entry will be returned the exact ip set

## Extra servers, JVM Arguments

You can even add extra servers through the JVM arguments with the "other.dns" property 

    java ... -Dother.dns=127.0.0.11,main.local.self ...

This is used (for me) mainly inside docker containers