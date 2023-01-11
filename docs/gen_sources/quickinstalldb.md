
## Download the last release<a id="quickinstall_01"></a>

Download the two tar.gz, ham and ham-samples from [github releases](https://github.com/kendarorg/HttpAnsweringMachine/releases)
and extract them in the same directory

## Starting the sample application<a id="quickinstall_02"></a>

Go on the "calendar" directory and run "runcalendardbproxy.bat/sh"

This will start

* H2 db server (tcp://localhost:9123, web console http://localhost:8082)
* ham (localhost:80)
* be (localhost:8100) proxied by http://localhost/int/be.sample.test
* gateway (localhost:8090) proxied by http://localhost/int/gateway.sample.test
* fe (localhost:8080)

First you should choose if you want to recreate the db go for Y the first time

<img src="../images/dbproxy_sample01_rebuild.gif" width="500"/>

Then you will have to wait a bit. Now DO NOT START THE BE!!

<img src="../images/dbproxy_sample02_rebuild.gif" width="500"/>
