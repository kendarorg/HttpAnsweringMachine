This demonstrates the creation of a HAM server with infrastructure on localhost
to test/verify applications

## 1: Hosts file and DNS

Prepare the hosts file containing the needed entries to run the test

    # The ham server
    127.0.0.1   www.local.test
    # The fe
    127.0.0.1   www.sample.test
    # The api gateway
    127.0.0.1   gateway.sample.test
    # The fe
    127.0.0.1   be.sample.test

Same goes to the dns configuration in external.json adding the voices to the "resolved" stuff

The blocked domains are the one pinged obsessively by chrome/firefox/windows/edge to check
for the existence of websites.. utterly useless for our objectives

{
    "id": "dns",
    "system": true,
    "active": true,
    "port": 53,
    "logQueries": false,
    "extraServers": [],
    "blocked": [
      "wpad.*",
      "*.trafficmanager.net"
    ],
    "resolved": [
      {
        "id": "0",
        "dns": "www.local.test",
        "ip": "127.0.0.1"
      },
      {
        "id": "3",
        "dns": "www.sample.test",
        "ip": "127.0.0.1"
      },
      {
        "id": "4",
        "dns": "gateway.sample.test",
        "ip": "127.0.0.1"
      },
      {
        "id": "5",
        "dns": "be.sample.test",
        "ip": "127.0.0.1"
      }
    ]
  } 

## 2: Proxying

Then should setup the proxying to let HAM the responsibility to call the application
given the DNS names

Inside the external.json should set the proxy section should do like this. Notice that you can
set the test address to something like www.google.com to force everything to be always on

  {
    "id": "proxy",
    "system": true,
    "proxies": [
      {
        "id": "2",
        "when": "http://gateway.sample.test",
        "where": "http://127.0.0.1:8090",
        "test": "127.0.0.1:8090"
      },
      {
        "id": "3",
        "when": "http://be.sample.test",
        "where": "http://127.0.0.1:8100",
        "test": "127.0.0.1:8100"
      },
      {
        "id": "4",
        "when": "http://www.sample.test",
        "where": "http://127.0.0.1:8080",
        "test": "127.0.0.1:8080"
      }
    ]
  },

## 3: Starting the sample application

You can then start the projects into samples/sampleapp as standard java applications or with your IDE
e.g. 

    java -jar be-3.0.7-SNAPSHOT.jar

* be
* gateway
* fe

## 4: Starting HAM

To start HAM you should use the Spring Boot propery loader

    java "-Dloader.path=FULLPATHTOHAMROOT/libs/"  -Dloader.main=org.kendar.Main  \
        -jar app-2.1.3.jar org.springframework.boot.loader.PropertiesLauncher

Following this approach the libs files will be loaded as library

## 5: Testing 

Now you can start testing everything with [PACT](plugins/replayer/pact.md) 
or [NULL infrastructure tests](plugins/replayer/null.md)
or [UI tests](plugins/replayer/ui.md)
