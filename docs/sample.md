A couple of sample application are present in the samples. One with Spring Boot,
the other one with Apache

## Calendar application

To demonstrate the capabilities of HAM i built a three part application

### FE (front end)

* Spring Boot Web Application to store employees and appointments
* Based on the same confused Ajax/JQuery application of the HAM UI
* Contains the HTML files embedded in Spring
* Calls the gateway to execute actions

Can be used to

* Record the API calls and run the FE without ANY back-end
* Record everything and run everything without ANY server

### GATEWAY

* Spring boot based API
* Only reason to exists, to be a proxy for the Backend API

Can be used to

* Record the calls made by the gateway to the BE and build tests calling the GATEWAY apis without the BE in NULL test
* Record the calls made to the BE and run them to make a PACT test

### BE (back end)

* Store in a h2 in memory db the data
* Offers the main API to call

Can be used

* As a source to verify against PACT tests

## Quotes application

### Core (ham.quotes.master)

A simple php application to retrieve NASDAQ quotes.
Used to understand the inspection capabilities of the system.