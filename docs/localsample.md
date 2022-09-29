
This demonstrates the creation of a HAM server with infrastructure on localhost
to test/verify applications

## 1: Download the last release

Download the two tar.gz, ham and ham-samples from [github releases](https://github.com/kendarorg/HttpAnsweringMachine/releases)
and extract them in the same directory

## 2: Starting the sample application

Go on the "calendar" directory and run "runcalendar.bat/sh"

This will start

* ham (localhost:80)
* be (localhost:8100) proxied by http://localhost/int/be.sample.test 
* gateway (localhost:8090) proxied by http://localhost/int/gateway.sample.test
* fe (localhost:8080)

## 3: Record some interaction

You can now check ham application going on http://localhost

* Going on [ham proxyes](http://localhost/proxy/index.html) you can verify that all proxies are ok if they don't work just "Refresh Status"
* Navigation on the [application](http://localhost:8080) you can try some interaction
* Then you can create a recording on the [recording page](http://localhost/plugins/recording) 
* Once you create the recording you can start recording!
* Go then on the [application](http://localhost:8080) and do some interaction
* And stop the recording!
* Now you will se all the calls on the just created recording

## 4: Go on with all the ways to play! 

Now you can start testing everything with [PACT](plugins/replayer/pact.md) 
or [NULL infrastructure tests](plugins/replayer/null.md)
or [UI tests](plugins/replayer/ui.md)
