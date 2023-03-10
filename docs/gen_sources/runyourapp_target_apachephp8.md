
## Prepare the application container<a id="preparehamcontainer"></a>

Create a directory, let's say "core" in the Dockerfile directory
that will contain all the source file for the application (html,php etc)

Prepare the Dockerfile. Note that the client images already handles all basic initializations
like DNS resolution, services initializations and certificates management.

<pre>
FROM ham.apache.php8:latest

# Copy the source files
COPY core/ /htdocs/
</pre>

And create the image

<pre>
docker build --rm -t testapp.app .
</pre>
