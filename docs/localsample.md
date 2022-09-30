
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

## 3: Configure proxy

* Chrome:
  * Install [Proxy Switch Omega](https://chrome.google.com/webstore/detail/proxy-switchyomega/padekgcemlokbadohgkifijomclgjgif)
  * Go to options
  * Add http and https proxy server with 
    * Address: 12.0.0.1
    * Port 1081
  * Select "proxy" from the extension menu and back to "direct" when you want to disconnect
* Firefox
  * Navigate to [about:preferences](about:preferences)
  * Search for "proxy"
  * Click on "Settings"
  * Go to "Manual proxy Configuration"
  * Select the socks5 proxy
      * Address: 12.0.0.1
      * Port 1080
  * Check the "Proxy DNS when using SOCKS v5" flag
  * Clean the settings when needed

## 4: Record some interaction

You can now check ham application going on http://www.local.test

* Going on [ham proxyes](http://www.local.test/proxy/index.html) you can verify that all proxies are ok if they don't work just "Refresh Status"
* Navigation on the [application](http://www.sample.test) you can try some interaction
* Then you can create a recording on the [recording page](http://www.local.test/plugins/recording) 
* Once you create the recording you can start recording!
* Go then on the [application](http://www.sample.test) and do some interaction
* And stop the recording!
* Now you will se all the calls on the just created recording

## 5: Go on with all the ways to play! 

Now you can start testing everything with [PACT](plugins/replayer/pact.md) 
or [NULL infrastructure tests](plugins/replayer/null.md)
or [UI tests](plugins/replayer/ui.md)
