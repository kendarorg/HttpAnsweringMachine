In this demo you will 

* Start locally HAM server
* Start the sample application
* Connect to it through proxy
* Record the interactions of the sample application
* Intercept Google calls and replace the logo!

## Download the last release<a id="quickinstall_01"></a>

Download the two tar.gz, ham and ham-samples from [github releases](https://github.com/kendarorg/HttpAnsweringMachine/releases)
and extract them in the same directory

## Starting the sample application<a id="quickinstall_02"></a>

Go on the "calendar" directory and run "runcalendar.bat/sh"

This will start

* ham (localhost:80)
* be (localhost:8100) proxied by http://localhost/int/be.sample.test
* gateway (localhost:8090) proxied by http://localhost/int/gateway.sample.test
* fe (localhost:8080)

## Configure proxy<a id="proxy_01"></a>

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

## Record some interaction<a id="recordcalendar_01"></a>

You can now check ham application going on http://www.local.test

* Going on [ham proxyes](http://www.local.test/proxy/index.html) you can verify that all proxies are ok if they don't work just "Refresh Status"

![Ham Proxyes](../images/ham_proxies.gif)

* Navigation on the [application](http://www.sample.test) you can try some interaction

![Sample application](../images/calendar_employees.gif)

* Then you can create a recording on the [recording page](http://www.local.test/plugins/recording)

![Create recording](../images/create_recording.gif)

* Once you create the recording you can start recording!

![Start recording](../images/start_recording.gif)

* Go then on the [application](http://www.sample.test) and do some interaction
* And stop the recording!
* Now you will se all the calls on the just created recording

## Install SSL root certificate<a id="installcertificate_01"></a>

Download [the certificate](http://www.local.test/api/certificates/ca.der)
Open the zip file and install as "Root certificate authority"

* Firefox:
    * Go on Settings and search for certificates
    * Then "View certificates" and "Import"
    * Check "Trust to identify websites"
* Chrome:
    * Go on Settings and search for certificates
    * Open the "Security" and "Manage certificates" then "Import"
    * "Place all certificates in the following store" then "Browse"
    * Select the "Trusted Root Certification Authorities"


## Intercept Google!<a id="interceptgoogle_01"></a>

Go on the [certificates configuration page](http://www.local.test/certificates/index.html)
and add a new website with value www.google.com

Add a new dns mapping on the [dns configuration](http://www.local.test/dns/index.html) with

* ip: 127.0.0.1
* dns: www.google.com

Restart the browser to be sure that all DNS caches are cleaned!

Go on https://www.google.com

When you click on the locker near the address you will see that the website
certificate is generated through "CN=root-cert"... OUR AUTHORITY :)

## Bing-ify google!<a id="bingifygoogle_01"></a>

Go on the [js-filters plugin](http://www.local.test/plugins/jsfilter/index.html) and
create a "Google" filter.

* Phase: POST_CALL (change the content received)
* Host Address: www.google.com
* Path Address: empty the field
* Script. Notice the "" added to the response text, this is just to force a cast from Java String to Javscript string
<pre>
var regex=/\/images\/branding\/[_a-zA-Z0-9]+\/[_a-zA-Z0-9]+\/[_a-zA-Z0-9]+\.png/gm;
var responseText = response.getResponseText()+"";
var changedText = responseText.replace(regex,'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c7/Bing_logo_%282016%29.svg/320px-Bing_logo_%282016%29.svg.png');
response.setResponseText(changedText);
return false;
</pre>

Navigate to https://www.google.com with BING! logo :D
