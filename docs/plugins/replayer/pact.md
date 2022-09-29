//TODO: Pacts

What if you have to contextualize the pact? E.G.

* A search returns results (always different)
* You need to get the id of the first item returned and
* ...call with it another API

Let's explain how


* In the first "Post" script you should 
	* Retrieve the item id from the 'response'
	* Store it inside the [cache](replayer.md)
	* Remember to verify the [structure](replayer.md)
* In the "Pre" script of the API
	* Retrieve the value stored in the cache 
	* Modify the 'request' data accordingly
	* Now the modified request will be used to call the remote server
