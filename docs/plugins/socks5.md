## Socks5 Proxy server

This is a simple socks5 implementation to allow the usage of
the applications outside docker without changing the hosts file

### Settings

The default setting is to listen on port 1080, while using
ham internal dns service. Following this you can intercept 
everything you need.

Here is the basic configuration

    {
    "id": "socks5.server",
    "port": 1080,
    "active": true
    }
    
### Connecting

* Chrome: just add the parameters calling chrome --proxy-server="socks5://localhost:1080"
* Vscode: as chrome!!
* [On Baeldung for java](https://www.baeldung.com/java-connect-via-proxy-server)
* [Some C# suggestions](https://dotnetcoretutorials.com/2021/07/11/socks-proxy-support-in-net/?series)