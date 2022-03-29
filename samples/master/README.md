This is a basic image to run a local application
on the local docker

## Running

Add to your hosts file the line

	127.0.0.1 www.local.test
	
It will be as easy as working on the local machine

### Startup

After the setup simply run ImagesRun.sh/bat

If you have not changed the configuration of the environment but only your application
you will not need to rebuild the docker image (it's No by default) since the 
application startup code will use directly your local executable directory

### Applying code changes

When changing the code 

* Stop the docker container
* Rebuild the code
* Restart the docker container

### Debugging

Connect to the debug port with the remote debugger

## Setup
Minimal things to do (after copying it :P)

### ImagesRun.sh/bat

* Set YOUR_APP_NAME to your application name
* Set YOUR_APP_FULL_PATH to the path of the executable that you will run

### config/app.sh

Place the command line of your application after the "cd /app" line

The file will place you directly on the "YOUR_APP_FULL_PATH" directory on the container 
(that is btw mapped on the /app directory as you can see)

If you have a Java application that you would like to debug add the following to
the command line

	 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005

### config/external.dns.servers.json

Add the extra dns servers (e.g. the ones exposed by your vpn)

### Dockerfile

Add the portse (even debug ones) exposed by your application. For a standard java
application this would be

	EXPOSE 5005
	EXPOSE 8080