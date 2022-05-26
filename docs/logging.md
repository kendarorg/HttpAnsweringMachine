## Default logging

Inside the global configuration you can find the logging configuration.
These works exactly like the standard Log4j log properties, define the
package or class and the logging level.

Here a map of the log levels:

![Log levels](loglevels.png)

### The configuration

The logPath variables sets the log file location, null means inside the 
main jar directory

The logRoundtripsPath sets the directory where to store the full static 
and dynamic requests. If null does not store anything

The logLevel is the global log level

    [{  "id": "global",
        ...
        "logging": {
            "logPath": null,
            "logRoundtripsPath": null,
            "logLevel": "INFO",
            "loggers": {
                "org.kendar.servers.http.Request":"DEBUG",

### Adding logs

You can add as much classes/paths as you like for logging
as you do in a normal proerties files e.g. to track all requests
to base HttpServer

    "com.sun.net.httpserver":"DEBUG"

## Special loggers

* org.kendar.servers.http.Request: To log the requests, just when they come to the server
    * OFF: logs nothing (default)
    * INFO: Show the request path
    * DEBUG: Show the request data with the first 100 chars of the request content
    * TRACE: Show the full request content
* org.kendar.servers.http.Response: To log the response, before sending it back
    * OFF: logs nothing (default)
    * DEBUG: Show the request data with the first 100 chars of the request content
    * TRACE: Show the full request content
* org.kendar.servers.http.StaticRequest
    * OFF: logs nothing (default)
    * DEBUG: Logs on file the static requests
* org.kendar.servers.http.DynamicRequest
    * OFF: logs nothing (default)
    * DEBUG: Logs on file the dynamic requests
* org.kendar.servers.http.InternalRequest
    * OFF: does not log internal requests
    * DEBUG: treat internal requests as any other
