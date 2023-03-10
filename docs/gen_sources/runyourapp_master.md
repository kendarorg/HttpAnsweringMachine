
## Prepare the HAM container<a id="preparehamcontainer"></a>

Create a directory "master" and inside it

Prepare the configuration file, e.g. here you can find the
default template [external.json](files/external.json)

Just setup a "Dockerfile" like the following for the HAM master

<pre>
FROM ham.master:latest
# Copy the configuration
COPY .external.json /etc/app/ham/app/external.json
</pre>

And create the image

<pre>
docker build --rm -t testapp.master .
</pre>
