
### HTTP

When the call to www.google.com reach the internal http server the following happens:

* The request is translated to the internal format
* Starts the [filters lifecycle](../lifecycle.md) when a "blocking" filter is found the result is sent back directly to the client
* The "PRE" filters are called
* The real www.google.com is invoked and the result parsed
* The "POST" filters are called and the data is sent back to the client

### HTTPS

This works with http. But what happens with https and the certificates?

First we need a root CA. This can be downloaded directly from HAM and installed on any
system be it Java, .NET, browser, phone...etc

Given a root CA, HAM is able to generate "on the fly" the certificates configured with
the interface. With this approach is possible to see and parse the content of the https
requests.

This is really a Man In The Middle (MITM) attack!
