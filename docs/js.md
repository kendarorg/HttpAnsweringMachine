It is possible to add javascript plugins to intercept and/or modify the requests and responses

## Js Plugins

First should be defined a directory where to store the js plugins, relative to the jar

    jsfilter.path=jsplugins

### Directory structure

Inside this there is the following file structure

    myFilter.json
    myFilter
        entrypoint.js
        lib.js

### Filter descriptor

The myFilter.json is the descriptor of the plugin. The various parts are

* method: the http method
* hostAddress: the exact host dns name (or * for any)
* hostRegexp: the Java regexp to match the host
* pathAddress: the exact path, started with "/" (or * for any)
* pathRegexp: the Java regexp to match the path
* phase: the [phase](docs/lifecyvle.md) for the filter
* requires: the list of files needed to run the plugin
* id: an unique id
* enabled: true/false
* blocking: true/false. When blocking the filter result will be sent directly to 
the output, when false all the subsequent filters will be executed
* priority: the priority of the filter
* method: the http method (or * for any)

<pre>
{
    "method" : "GET",
    "hostAddress" : "js.test.org",
    "hostRegexp" : "",
    "pathAddress": "/test",
    "pathRegexp": "",
    "phase": "PRE_RENDER",
    "requires": [
        "myhandler/runner.js",
        "myhandler/fuffa.js"
    ],
    "id": "myhandler",
    "enabled": true,
    "blocking": true,
    "priority": 100
}
</pre>

### Example filter 

A unique function run filter must be present between all js files

    function runFilter(request,response){

The request and response will be exactly the Request and Response java class used inside the
system.

The result must be an object containing

* request: the request even if not modified
* response: the response even if not modified
* continue: weather should continue with the next filters

<pre>
function runFilter(request,response){
    var today = new Date().toISOString();
    response.responseText = '{"value":"This is a calculated javascript response","date":"'+today+'"}';
    response.headers["Content-Type"]="application/json";
    response.statusCode = 200;
    request.headers["Host"]="test.com";
    var result ={
        request:request,
        response:response,
        continue:false
    };
    return result;
}
</pre>