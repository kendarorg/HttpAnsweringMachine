
<!--This file is autogenerated. Do not edit!-->
In this demo you will 

* Start locally HAM server
* Connect to it through proxy
* Intercept Google calls and replace the logo!

For more info [look here](../generated/googlehack_internals.md)!

## Download the last release<a id="quickinstall_01"></a>

Download the tar.gz, ham only from [github releases](https://github.com/kendarorg/HttpAnsweringMachine/releases)
and extract it

## Starting the sample application<a id="quickinstalllocal_02"></a>

Go on the "ham" directory and run "proxy.run.bat/sh"

This will start ham with all services

<img src="../images/start_sample_proxy.gif" width="500"/>

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


## Configure proxy<a id="proxy_01"></a>

Should set the proxy to 127.0.0.1 And port 1080 for socks5 or 1081 for http/https

<details>
  <summary>Click me for more explanations</summary>

* Chrome:
    * Install [Proxy Switch Omega](https://chrome.google.com/webstore/detail/proxy-switchyomega/padekgcemlokbadohgkifijomclgjgif)
    * Go to options
    * Add http and https proxy server with
        * Address: 127.0.0.1
        * Port 1081.
      
          <img alt="Ham Proxyes" src="../images/chrome_proxy.gif" width="500"/>
    * Select "proxy" from the extension menu and back to "direct" when you want to disconnect
    * 
      <img alt="Ham Proxyes" src="../images/chrome_proxy_switch.gif" width="100"/>
     
* Firefox
    * Navigate to [about:preferences](about:preferences)
    * Search for "proxy"
    * Click on "Settings"
    * Go to "Manual proxy Configuration"
    * Select the socks5 proxy
        * Address: 127.0.0.1
        * Port 1080
    * Check the "Proxy DNS when using SOCKS v5" flag
    * Clean the settings when needed
  
      <img alt="Ham Proxyes" src="../images/firefox_proxy.gif" width="500"/>
    
</details>


## Intercept Google!<a id="interceptgoogle_01"></a>

Go on the [certificates configuration page](http://www.local.test/certificates/index.html)
and add a new website with value www.google.com

<img src="../images/add_google_certificate.gif" width="500"/>

Add a new dns mapping on the [dns configuration](http://www.local.test/dns/index.html) with

* ip: 127.0.0.1
* dns: www.google.com

<img src="../images/add_google_dns.gif" width="500"/>

Restart the browser to be sure that all DNS caches are cleaned!

Go on https://www.google.com

When you click on the locker near the address you will see that the website
certificate is generated through "CN=root-cert"... OUR AUTHORITY :)

On Firefox

<img src="../images/google_fake_cert.gif" width="500"/>

Or on Android Chrome

<img src="../images/fake_google_certificate_android.gif" height="300"/>

## Bing-ify google!<a id="bingifygoogle_01"></a>

Go on the [js-filters plugin](http://www.local.test/plugins/jsfilter/index.html) and
create a "Google" filter.

* Phase: POST_CALL (change the content received)
* Host Address: www.google.com
* Path Address: /
* Script. Notice the "" added to the response text, this is just to force a cast from Java String to Javscript string
<pre>
var regex=/\/images\/branding\/[_a-zA-Z0-9]+\/[_a-zA-Z0-9]+\/[_a-zA-Z0-9]+\.png/gm;
var responseText = response.getResponseText()+"";
var changedText = responseText.replace(regex,'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c7/Bing_logo_%282016%29.svg/320px-Bing_logo_%282016%29.svg.png');
response.setResponseText(changedText);
return false;
</pre>

<img src="../images/google_bing_filter.gif" width="500"/>

Navigate to https://www.google.com with BING! logo :D

On Firefox

<img src="../images/google_bing.gif" width="500"/>

On Android

<img src="../images/google_bing_android.gif" height="300"/>
