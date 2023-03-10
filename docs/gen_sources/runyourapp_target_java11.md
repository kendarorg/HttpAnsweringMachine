
## Prepare the application container<a id="preparehamcontainer"></a>

Create a directory "testapp" and inside it

Create the startup file, testapp.sh

<pre>
#!/bin/sh

# Reach the application directory
cd /etc/app/testapp

# Set the java home and path
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"

# Run your jar. Here with the agentlib line to allow remote debugging
# and the server port for Spring Boot
java -jar -Dserver.port=80 \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
  /etc/app/testapp/app-1.0.0.jar
</pre>

Prepare the Dockerfile. Note that the client images already handles all basic initializations
like DNS resolution, services initializations and certificates management

<pre>
FROM ham.client:latest

# Update the image
# Create the application dir
# The client comes already with java 11 installed
RUN apk update && apk upgrade &&
    mkdir -p /etc/app/testapp

# Copy the jars
COPY /[jarAbsoluteDirTarget]/*.jar /etc/app/testapp/
# Copy the run script
COPY .testapp.sh /etc/app/testapp/

# Set the .sh as executable
# Start the application in foreground with runit
RUN chmod +x /etc/app/testapp/*.sh &&
    /etc/startservice.sh --app=testapp --run=/etc/app/testapp/testapp.sh
</pre>

And create the image

<pre>
docker build  -t testapp.app .
</pre>
