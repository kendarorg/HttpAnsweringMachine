The system can act as a proxy/mod_rewrite (or API Gateway) for every request it receives

For example if you want to forward all requests to https://www.local.test/myapp to
https://localhost:8020/app then you can add a proxy entry inside the external.json
file. 

This module is always active by default

Each block must have a progressive id starting from 0. It's composed of three 
sections.

* id: a unique identifier string
* when: Upon receiving a request on this address/port/path
* then: Call the then address/port/path
* test: When the address is reachable use the proxy, when not reachable simply forward the request to when


      {[   "id" : "ssl",
            "proxy" : [{
                "id": "0",
                "when": "https://www.local.test/myapp",
                "where": "https://localhost:8020/app",
                "test": "localhost:8020"},

The test tries 

* ICMP ping request
* TCP Connection on port 7
* TCP Connection on specified port
* TCP Connection on 80 and 443

## Setup

* To enable on browsers [here](gen_sources/proxy.md)
* To enable on phones [here](gen_sources/proxy_android.md)
