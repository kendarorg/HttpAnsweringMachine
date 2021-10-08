The java plugins should implement the "org.kendar.http.FilteringClass" class,
and return an unique id.

They must be defined as Spring Boot @Component, and their jar and dependencies must be placed
inside the libs directory

## Class annotation

The HttpTypeFilter can be configured like the following. All this variables can be set with
the Spring Boot property style "${propertyfileindex}" and "${propertyfileindex:defaultvalue}"

* hostAddress: the exact address (or * for any)
* hostPattern: the Java regex for the host
* priority: the priority between the other filters
* name: a name for the filter
* blocking: true/false. When blocking the filter result will be sent directly to
  the output, when false all the subsequent filters will be executed

## Method annotations

On each method, whose signature must be the following
    
    boolean [methodName](Request req, Response res)

You should add the HttpMethodFilter annotation

* phase: the [phase](../../lifecycle.md) for the filter
* pathAddress: the exact path (or * for any)
* pathPattern: the Java regexp for the path
* method: the http method (or * for any)
* name: a specific name
* id: an unique identifier string  
* blocking: true/false. When blocking the filter result will be sent directly to
  the output, when false all the subsequent filters will be executed
  
When the method will return false (if not specified otherwise by the annotations) the call is blocking
else all the subsequent filters will be executed.

The following filter will intercept all the calls to 

    POST: www.google.com/test

..and run the filter. Obviously you should follow the
instruction on [Https hijacking module](../../https.md) to set up the dns.

<pre>

@Component
@HttpTypeFilter(
    hostAddress = "www.google.com")
public class GoogleFilter  implements FilteringClass {
    @Override
    public String getId() {
        return "GoogleFilter";
    }

    @HttpMethodFilter(
        phase = HttpFilterType.POST_RENDER,
        pathAddress ="/test",
        method = "POST",
        id="12354")
    public boolean record(Request req, Response res){

</pre>

Inside the function you can then elaborate the request and response as you please