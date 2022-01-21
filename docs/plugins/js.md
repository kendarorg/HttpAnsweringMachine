It is possible to add javascript plugins to intercept and/or modify the requests and responses

First should be defined a directory where to store the js plugins, relative to the jar

    jsfilter.path=jsplugins

## Directory structure

Inside this there is the following file structure.
Notice that the directory containing the libs must match the name of the json file

    myFilter.json
    myFilter
        lib.js

## Filter descriptor

The myFilter.json is the descriptor of the plugin. The various parts are

* method: the http method
* hostAddress: the exact host dns name (or * for any)
* hostRegexp: the Java regexp to match the host
* pathAddress: the exact path, started with "/" (or * for any)
* pathRegexp: the Java regexp to match the path
* phase: the [phase](docs/lifecyvle.md) for the filter
* requires: the list of files needed to run the plugin
* id: myFilter, aka the name of the file with the filter
* enabled: true/false
* blocking: true/false. When blocking the filter result will be sent directly to 
the output, when false all the subsequent filters will be executed
* priority: the priority of the filter
* method: the http method (or * for any)
* source: an array containing the filter source. This source will receive the parameters ''request'' and ''response''
This filter will intercept all the requests to

    GET js.test.org/test

...and run the runFilter function on it. Obviously you should follow the
instruction on [Https hijacking module](../https.md) to se tup the dns

<pre>
{
    "method" : "GET",
    "hostAddress" : "js.test.org",
    "hostRegexp" : "",
    "pathAddress": "/test",
    "pathRegexp": "",
    "phase": "PRE_RENDER",
    "requires": [
        "myhandler/lib.js",
        "myhandler/fuffa.js"
    ],
    "id": "uniqueIdentifier",
    "enabled": true,
    "blocking": true,
    "priority": 100,
    "source":[
        "if(request.headers['Host']=="www.test.com")response.statusCode=404;",
        "var result ={",
        "    request:request,",
        "    response:response,",
        "    continue:false",
        "};",
        "return result;"
    ]
}
</pre>

## Filter implementation

The source will be always wrapped automatically with this declaration

    function runFilter(request,response,utils){
    }

The request and response will be exactly the Request and Response java class used inside the
system.

The result must be an object containing

* request: the request even if not modified
* response: the response even if not modified
* continue: weather should continue (true) with the next filters. False will break the execution (running by the way all post render action) 

    Please notice that if returning true this means the filter is in fact 
    an object that changes the request, and the response will not be used

This filter will return a specific response test with the current data
<pre>
        "var today = new Date().toISOString();",
        "response.responseText = '{\"value\":\"This is a calculated javascript response\",\"date\":\"'+today+'\"}';",
        "response.headers['Content-Type']='application/json';",
        "response.statusCode = 200;",
        "request.headers['Host']='test.com';",
        "var result ={",
        "    request:request,",
        "    response:response,",
        "    continue:false",
        "};",
        "return result;"
</pre>

## Utils

### Events production

Js filters has the eventQueue parameter. This has the signature

  utils.handleEvent(String anyCaseJavaSimpleName,String serializedEvent);
  
Invoking it is possible to interact (forward only) with the rest of the framework
It is not possible to generate javascript event handlers :)

### Load file content

This will load an arbitrary file content based on the js plugins path

  var content = utils.loadFile(String relativePath,boolean binary);
  
Setting the binary flag will load the file content as Base64 encoded

### Exeternal calls

Using the standard request and response objects you can invoke any external api
through

  Response response = util.httpRequest(Request request);

The result will be a standard response object

### Load functions from file content

To load function and variables on global scope from strings (aka when you got tehm through loadTextFile)
you can load their content on the global scope like this:

  (1,eval)(fileContent);
  
After that the content of the file will be retrieved