
## Pact basics

* You should first record the interaction with an external system
* May be you can record the calls made by one of your systems stimulating the external

* Select then all the calls to the external system as "Stimulator"
  * Add on all the calls as "post" script the verification of the data. This is an example but you can go deeper

        var diffEngine = new org.kendar.xml.DiffInferrer();
        diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());
        if(expectedresponse.getStatusCode()!=response.getStatusCode()){
            throw "Expected status code "+expectedresponse.getStatusCode()+" but received "+response.getStatusCode();
        }
* Run the test!

If (of course) during this kind of test the data changes wildly and changes are propagated
through requests you can check the [JS Interception](replayer.md) section to use variables 
inside the bodies/query/headers

What if you have to contextualize the pact? E.G.

* A search returns results (always different)
* You need to get the id of the first item returned and


Let's explain how


* In the first "Post" script you should 
	* Retrieve the item id from the 'response'
	* Store it inside the [cache](replayer.md)
	* Remember to verify the [structure](replayer.md)
* In the "Pre" script of the API
	* Retrieve the value stored in the cache 
	* Modify the 'request' data accordingly
	* Now the modified request will be used to call the remote server
