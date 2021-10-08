The system can intercept any kind of https request reading the content and 
elaborating it. To allow this some step must be made.

## Simple dns request

Let's suppose we want to intercept all requests to https://www.google.com

First should be added the DNS entry on the external.properties

    dns.resolve.1=www.google.com 127.0.0.1

If you are using a hosts file you should add to it the line

    www.google.com 127.0.0.1

Then we have to create a certificate for the website, editing the external.properties
adding the certificate generation. The data is 0 based as usual

    https.certificate.2=*.google.com
    https.certificate.3=google.com

Restart the application

Now all requests to https://www.google.com will be intercepted by the system

For more information on the DNS server go to the [specific page](dns.md)