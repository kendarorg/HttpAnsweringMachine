It is possible to add javascript plugins to intercept and/or modify the requests and responses


## Filter descriptor

The filter contains various parts

* matcher: the type of matcher(s) that will be applied to filter
  * apimatcher: to match paths/addresses/methods
  * scriptmatcher: to match with a js script
  * querymatcher: to match db queries
* phase: the [phase](../docs/lifecyvle.md) for the filter
* id: myFilter, aka the name of the file with the filter
* type: what if the request matches
  * script: execute the script
  * body: directly return the body
* enabled: true/false
* blocking: true/false. When blocking the filter result will be sent directly to 
the output, when false all the subsequent filters will be executed
* priority: the priority of the filter
* source: for body type will be the data returned, for script the script executed

...and run the runFilter function on it. Obviously you should follow the
instruction on [Https hijacking module](../https.md) to se tup the dns

## Filter implementation

The source (for script type) will be always wrapped automatically with this declaration

    function runFilter(request,response,utils){
    }

The request and response will be exactly the Request and Response java class used inside the
system.

The result will be a boolean: weather should continue (true) with the next filters. False will break the execution (running by the way all post render action) 

The object modified inside the filter will be used directly! Beware!

    Please notice that if returning true this means the filter is in fact 
    an object that changes the request, and the response will not be used

This filter will return a specific response test with the current data
<pre>
        "var today = new Date().toISOString();",
        "response.setResponseText('{\"value\":\"This is a calculated javascript response\",\"date\":\"'+today+'\"}');",
        "response.putHeader('Content-Type','application/json');",
        "response.setStatusCode(200);",
        "request.putHeader('Host','test.com');",
        "return false;"
</pre>

## Matchers

### apimatcher

Contains

* method: the http method
* hostAddress: the exact host dns name (or * for any)
* hostRegexp: the Java regexp to match the host
* pathAddress: the exact path, started with "/" (or * for any)
* pathRegexp: the Java regexp to match the path

###  scriptmatcher

* Use a script to match the request, as per the script types the function wrapping the script is the following, 

     function runFilter(request,utils){ }

* When your script returns true, the matcher is successful

### querymatcher

* Allow to set the SQL query that will be matched
* The data can be retrieved by a db recording

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

### Call java functions

You can always add calls to native java objects and functions, simply create stuffs
with the FQDN, like this. This function instantiate the JAVA DiffInferrer, check
the JAVA Strings with the diff and then throws a JAVASCRIPT exception
when something unexpected happens

<pre>
    var diffEngine = new org.kendar.xml.DiffInferrer();
    diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());
    if(expectedresponse.getStatusCode()!=response.getStatusCode()){
        throw "Expected status code "+expectedresponse.getStatusCode()+" but received "+response.getStatusCode();
    }
</pre>


