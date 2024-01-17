
## Configure the application proxy<a id="appproxy_01"></a>

Here is configured for both http and https BUT only http traffic will be intercepted

### On the application

* [On Baeldung for java](https://www.baeldung.com/java-connect-via-proxy-server)

Just add to the java command to start the application the following:

<pre>
    -Dhttp.proxyHost=127.0.0.1  -Dhttp.proxyPort=1081
    -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=1081
</pre>

* [Some C# suggestions](https://dotnetcoretutorials.com/2021/07/11/socks-proxy-support-in-net/?series)

### On HAM configuration

First ensure that all the ports called by your application are set on the 
file httpproxy.external.json in the "http" section. In the example here the 
http listen on 80, 8081 and 8082

<pre>
  {
    "id": "http",
    "system": true,
    "active": true,
    "port": "80;8081;8082",
    "backlog": 50,
    "useCachedExecutor": true
  },
</pre>


