
### Recording

HAM is able to record all requests that are redirected to him, that means
all the calls relative to the DNS names redirected to the local http/s server.

#### DNS capture

With the DNS standard capture all the calls to http servers mapped in HAM dns and 
all the https calls (with the relative certificate added on configuration) are 
recorded

#### The "/int" special path

One special case are proxies with no DNS. They MUST have a path starting with "/int".

If i want to capture the calls from a local (no docker) server to another (no docker)
server. I can't rely on ["proxy rewrite"](../proxy.md) through dns but i have to let the local server
point to "http://localhost/int/[somepath]" and then add a proxy rewrite to the other server
like to "http://localhost:[otherserverlocalport]"

#### No http/s request

All the calls to server other than http/s can't be intercepted completely but you can 
see their DNS names on the "Resolved" DNSes page

