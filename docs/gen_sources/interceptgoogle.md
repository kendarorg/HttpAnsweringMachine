
## Intercept Google!<a id="interceptgoogle_01"></a>

Go on the [certificates configuration page](http://www.local.test/certificates/index.html)
and add a new website with value www.google.com

<img src="../images/add_google_certificate.gif" width="200"/>

Add a new dns mapping on the [dns configuration](http://www.local.test/dns/index.html) with

* ip: 127.0.0.1
* dns: www.google.com

<img src="../images/add_google_dns.gif" width="200"/>

Restart the browser to be sure that all DNS caches are cleaned!

Go on https://www.google.com

When you click on the locker near the address you will see that the website
certificate is generated through "CN=root-cert"... OUR AUTHORITY :)

On Firefox

<img src="../images/google_fake_cert.gif" width="200"/>

Or on Android Chrome

<img src="../images/fake_google_certificate_android.gif" height="300"/>
