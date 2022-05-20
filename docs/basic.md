## Basic Setup

Simply copy the main jar where you want with the "libs" folder at the same level. It works with Java 11!!

Just a note, you can include jsons with the followint syntax, using single or double quotes
The quotes are used to avoid messing with json formatting

The content of the target file will replace AS-IS the include statement

	"#include:/absolute/path/file.json"
	"#include:relative/to/external/json/path/file.json"

### Http and Https configuration

Prepare a basic configuration in an "external.json" file in the same dir of the Jar

The name for which the server will respond

    [{  "id":"global",
        "localAddress":"www.local.test",


The http/s configuration, this will be generated at runtime. For the https certificates see
the [Https configuration page](../https.md)

    [{  "id":"https",
        "active":true,
        ...},
    {  "id":"http",
        "active":true,
        ...},
    {  "id" : "ssl",
        "cname":"C=US,O=Local Development,CN=local.org",
        "domains" : [{
                "id" :"a",
                "address":"*.local.test"},
            {
                "id" :"b",
                "address":"local.test"},
    
### DNS Resolution

You have two options. Hosts file or the embedded dns server. [See the DNS page for further info](../dns.md)

#### Hosts

Under Windows run notepad as administrator and add the following at the end of
"C:\Windows\System32\drivers\etc\hosts" file, under *nix "/etc/hosts"

    127.0.0.1 www.local.test

#### Local DNS Server

First you need to enable the local dns on external.json file. Remind that this approach probably 
wont work if you are running a VPN client! It does not work (for me) with OpenVPN client and 
GlobalProtect. 


    [{  "id":"dns",
        "active": false,

### Run and verify

Then run the following command

    java "-Dloader.path=/start/services/answering/libs" \
        -Djdk.tls.acknowledgeCloseNotify=true \
        -Dloader.main=org.kendar.Main  \
        -jar app-2.1.3.jar \
        org.springframework.boot.loader.PropertiesLauncher

The loader.main is a specific Spring Boot variables to force the loading of
local application.properties file together with the PropertiesLauncher.

The loader path (that must be a full path) tells the application where to find
the external plugins to load

The jdk.tls.acknowledgeCloseNotify, is a fix for a specific problem with TLS that
can be encontered on several JDKs ( see on [Stackoverflow](https://stackoverflow.com/questions/54687831/changes-in-sslengine-usage-when-going-up-to-tlsv1-3) ) 

And go with any browser on [http://www.local.test/api/health](http://www.local.test/api/health)

You should see then an "OK" text

### Install the certificates on the system

You can then install the root certificate on the machine, first list the certificate you need
going on [http://www.local.test/api/certificates](http://www.local.test/api/certificates) that will
return a list of the available formats. The encrypted key pwd is "test":

        [
            "ca.der",
            "ca.encrypted.key",
            "ca.key"
        ]

You can then download your preferred one and install on the OS and on the Browser downloading them from
[http://www.local.test/api/certificates/NAME](http://www.local.test/api/certificates/NAME) for example,
then yout can download the certificae file and install it as trusted root CA.

#### Windows

For example on Windows you can download the der [http://www.local.test/api/certificates/ca.der](http://www.local.test/api/certificates/ca.der).
Then you can unzip the file and install it as trusted root CA.

#### Java

On java you can download the der  [http://www.local.test/api/certificates/ca.der](http://www.local.test/api/certificates/ca.der).
And import it into the main keystore

    keytool -import -trustcacerts -alias answeringMachineCa -file ca.der -cacerts -storepass changeit

#### Browser

For the browser you can follow the various browsers instruction and install as trusted root CA.

Now you can browse to [https://www.local.test/api/health](https://www.local.test/api/health) (mind the
HTTPS! ) and you will not receive any security warning.
