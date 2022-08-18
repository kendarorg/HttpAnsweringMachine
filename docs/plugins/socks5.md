## Socks5 Proxy server

This is a simple socks5+http implementation to allow the usage of
the applications outside docker without changing the hosts file

### Settings

The default setting is to listen on port 1080 (for socks5) and 1081 (for http/s), while using
ham internal dns service. Following this you can intercept 
everything you need.

The httpProxyPort is the one for the http/s proxy

Here is the basic configuration

    {
    "id": "socks5.server",
    "port": 1080,
    "httpProxyPort": 1081,
    "active": true
    }
    
### Connecting

* Chrome: just add the parameters calling chrome --proxy-server="socks5://localhost:1080"
* Firefox: go to "about:preferences" (in the address bar) and set the socks address (remember to set the DNS option for socks)
* Vscode: as chrome!!
* [On Baeldung for java](https://www.baeldung.com/java-connect-via-proxy-server)
* [Some C# suggestions](https://dotnetcoretutorials.com/2021/07/11/socks-proxy-support-in-net/?series)
* For curl just set the environment variables
<pre>
 export http_proxy="http://127.0.0.1:9999"
 export https_proxy="http://127.0.0.1:9999"
 curl  "https://httpbin.org/anything"
</pre>