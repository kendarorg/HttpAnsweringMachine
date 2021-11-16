The system can intercept any kind of https request reading the content and 
elaborating it. To allow this some step must be made.

## Simple dns request

Let's suppose we want to intercept all requests to https://www.google.com

First should be added the DNS entry on the external.properties

    [{  "id":"dns",
        "active" : true,
        "resolved":[{
            "id"  : "0",
            "ip"  : "127.0.0.1"
            "dns" : "www.google.com"},

If you are using a hosts file you should add to it the line

    www.google.com 127.0.0.1

Then we have to create a certificate for the website, editing the external.properties
adding the certificate generation. The id must be unique

    [{  "id" : "ssl",
        "domains" : [{
                "id" :"a",
                "address":"*.google.com"},
            {
                "id" :"b",
                "address":"google.com"},

Restart the application

Now all requests to https://www.google.com will be intercepted by the system

For more information on the DNS server go to the [specific page](dns.md)
