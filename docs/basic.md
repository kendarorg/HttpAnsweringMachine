## Basic Setup

Simply copy the main jar where you want with the "libs" folder at the same level. It works with Java 11!!

### external.properties

Prepare a basic configuration in an "external.properties" file in the same dir of the Jar

The name for which the server will respond

    localhost.name=www.local.test

The http/s configuration, this will be generated at runtime

    http.enabled=true
    https.enabled=true
    https.certificates.cnname=C=US,O=Local Development,CN=local.org
    https.certificate.0=*.local.test
    https.certificate.1=local.test

### Hosts file

Running locally you should map the DNS names to localhost in the hosts file.

Under Windows run notepad as administrator and add the following at the end of
"C:\Windows\System32\drivers\etc\hosts" file

    127.0.0.1 www.local.test

### Run and verify

Then run

    java "-Dloader.path=/start/services/answering/libs" \
        -Djdk.tls.acknowledgeCloseNotify=true \
        -Dloader.main=org.kendar.Main  \
        -jar app-1.0-SNAPSHOT.jar \
        org.springframework.boot.loader.PropertiesLauncher &

And go with any browser on [http://www.local.test/api/health](http://www.local.test/api/health)

You should see then an "OK" text

### Install the certificates on the system

You can then install the root certificate on the machine, first list the certificate you need
going on [http://www.local.test/api/certificates](http://www.local.test/api/certificates) that will
return a list of the available formats:

        [
            "ca.der",
            "ca.p12",
            "ca.cer",
            "ca.key",
            "ca.srl",
            "ca.pem",
            "ca.crt"
        ]

You can then download your preferred one and install on the OS and on the Browser downloading them from
[http://www.local.test/api/certificates/name](http://www.local.test/api/certificates/name) for example,
then yout can download the certificat file and install it as root CA.

For example on Windows you can download the crt [http://www.local.test/api/certificates/ca.crt](http://www.local.test/api/certificates/ca.crt).
Then you can unzip the file and install it as root CA. You can add it to the browsers directly.

Now you can browse to [https://www.local.test/api/health](https://www.local.test/api/health) (mind the
HTTPS! ) and you will not receive any security warning