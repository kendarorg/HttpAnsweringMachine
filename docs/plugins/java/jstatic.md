The files for the static web can be embedded or in a directory.
The embedded resources are cached at startup!

Every static page group must implement "org.kendar.http.StaticWebFilter" 
with the following methods

* String getId(): To return an unique identifier
* String getPath(): To return where should find the pages

Some annotation must be added too, Component (for Spring Boot) and the HttpTypeFilter,
specifying the host that will answer. It supports properties replacement with the "$" 
sign syntax

    @Component
    @HttpTypeFilter(hostAddress = "${localhost.name}")

Of course you must register the [dns](../../dns.md) and the [certificate](../../https.md)

## Static web pages logic

Suppose the following directory structure with the address www.test.org

    index.html
    sub
        index.htm
        test.htm

Then calling

* https://www.test.org: return 200, index.html
* https://www.test.org/: return 200, index.html
* https://www.test.org/index.html: return 200, index.html
* https://www.test.org/sub: return 200, index.htm
* https://www.test.org/sub/: return 200, index.htm
* https://www.test.org/sub/index.htm: return 200, index.htm
* https://www.test.org/sub/test.htm: return 200, test.htm
* https://www.test.org/nonexisting.htm: return 400, and the path not found with a message

        IMPORTANT
        While calling https://www.test.org/sub the relative path seen by the html page will 
        be https://www.test.org but if you call https://www.test.org/sub/ the relative path 
        seen by the html page will be https://www.test.org/sub

## Implementations

### Real directory

Should return the path of the directory where are located the pages, if it's not an
absolute path will be relative to the main application jar

### Embedded resources

This will be cached at startup!

With a relative path and with a "*" prefix the StaticWebFilter will search for all
the resource files inside the StaticWebFilter implementation JAR. If you
create for example a StaticWebFilter with a "*web" path, this means that under 
"src/resources/web" there will be the HttpTypeFilter hostAddress root.

Suppose your hostAddress is "www.test.com" and a resources exists 
"src/resources/web/test.html" then calling "https://www.test.com/test.html" you'll get
the page!

