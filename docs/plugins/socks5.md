## Socks5/Http/S Proxy server

This is a simple socks5+http implementation to allow the usage of
the applications outside docker without changing the hosts file

### Settings

The default setting is to listen on port 1080 (for socks5) and 1081 (for http/s), while using
ham internal dns service. Following this you can intercept 
everything you need.

The httpProxyPort is the one for the http/s proxy

If needed all http communication can be intercepted without configuring the DNS,
just setting interceptAllHttp to true. This does not work for https since you
will need first to [set the certificates](../https.md) . Following this approach
all http calls will be logged even the ones to plain IP addresses.

Here is the basic configuration

<pre>
    {
    "id": "socks5.server",
    "port": 1080,
    "httpProxyPort": 1081,
    "interceptAllHttp":false,
    "active": true
    }
</pre>

### Connecting

* Chrome: just add the parameters calling chrome --proxy-server="socks5://localhost:1080" or install [Switch Omega](https://chrome.google.com/webstore/detail/proxy-switchyomega/padekgcemlokbadohgkifijomclgjgif)
* Firefox: go to "about:preferences" (in the address bar) and set the socks address (remember to set the DNS option for the socks)
* Vscode: as chrome!!
* [On Baeldung for java](https://www.baeldung.com/java-connect-via-proxy-server)

<pre>
    -Dhttp.proxyHost=127.0.0.1  -Dhttp.proxyPort=1081
    -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=1081
</pre>

* [Some C# suggestions](https://dotnetcoretutorials.com/2021/07/11/socks-proxy-support-in-net/?series)
* For curl just set the environment variables
<pre>
 export http_proxy="http://127.0.0.1:1081"
 export https_proxy="http://127.0.0.1:1081"
 curl  "https://httpbin.org/anything"
</pre>