The system can act as a proxy (or API Gateway) for every request it receives

For example if you want to forward all requests to https://www.local.test/myapp to
https://localhost:8020/app then you can add a proxy entry inside the external.properties
file. 

This module is always active by default

Each block must have a progressive id starting from 0. It's composed of three 
sections.

* id: a unique identifier string
* when: Upon receiving a request on this address/port/path
* then: Call the then address/port/path
* test: When the address is reachable use the proxy, when not reachable simply forward the 
request to when

<pre>
    simpleproxy.0.id=12345
    simpleproxy.0.when=https://www.local.test/myapp
    simpleproxy.0.where=https://localhost:8020/app
    simpleproxy.0.test=localhost:8020
</pre>